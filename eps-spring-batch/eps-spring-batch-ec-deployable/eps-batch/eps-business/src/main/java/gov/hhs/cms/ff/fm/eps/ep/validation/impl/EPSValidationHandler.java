package gov.hhs.cms.ff.fm.eps.ep.validation.impl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.accenture.foundation.common.exception.ApplicationException;

import gov.cms.dsh.bem.AdditionalInfoType;
import gov.cms.dsh.bem.BenefitEnrollmentMaintenanceType;
import gov.cms.dsh.bem.MemberType;
import gov.hhs.cms.ff.fm.eps.ep.BEMDataUtil;
import gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentMaintenanceDTO;
import gov.hhs.cms.ff.fm.eps.ep.enums.EProdEnum;
import gov.hhs.cms.ff.fm.eps.ep.services.PolicyDataService;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;
import gov.hhs.cms.ff.fm.eps.ep.validation.FinancialValidator;
/**
 * The Validator Handle class implementation for EPS processing.
 * 
 * @author girish.padmanabhan
 *
 */
public class EPSValidationHandler {

	private static final Logger LOG = LoggerFactory.getLogger(EPSValidationHandler.class);
	
	protected static final LocalDate DATE = LocalDate.now();
	protected static final LocalDateTime DATETIME = LocalDateTime.now();
	protected static final int YEAR = DATE.getYear();
		
	
	protected final LocalDate febMAX = DATE.with(TemporalAdjusters.lastDayOfMonth());
	
	
	
	/**
	 * Validates extraction  status
	 * @param bemDTO
	 * @param bemDTO
	 */
	public void validateExtractionStatus(BenefitEnrollmentMaintenanceDTO bemDTO) {
		BenefitEnrollmentMaintenanceType bem = bemDTO.getBem();

		if (bem != null && bem.getExtractionStatus() != null) {

			BigInteger extractionStatus = bem.getExtractionStatus().getExtractionStatusCode();

			if (extractionStatus.intValue() == 1) {
				//Extraction Error
				EProdEnum eProdError = EProdEnum.EPROD_31;

				String logMsg = "Extraction Error: " + bem.getExtractionStatus().getExtractionStatusText();
				LOG.warn(logMsg);
				throw new ApplicationException(logMsg, eProdError.getCode());
			}
		}
		
	}
	/**
	 * Invoke ApplicationException in case of total premium amount is zero
	 * @param bemDTO
	 * @return void
	 */
	public BigDecimal validateZeroPremium(BenefitEnrollmentMaintenanceDTO bemDTO) {
		BigDecimal totalPremiumAmount = BEMDataUtil.getTotalPremiumAmount(BEMDataUtil.getSubscriberMember(bemDTO.getBem()));

		if (totalPremiumAmount != null && totalPremiumAmount.compareTo(BigDecimal.ZERO) == 0) {

			//EPROD-37: $0 Total Premium Amount
			EProdEnum eProdError = EProdEnum.EPROD_37;

			String logMsg = eProdError.getLogMsg();
			LOG.warn(logMsg);
			throw new ApplicationException(logMsg, eProdError.getCode());
		}
		 return totalPremiumAmount;
	}

	/**
	 * Invoke PolicyDataService to retrieve the latest policy version from DBzz
	 * @param bemDTO
	 * @param policyDataService 
	 * @return dbBemDto
	 */
   	public BenefitEnrollmentMaintenanceDTO performPolicyMatch(BenefitEnrollmentMaintenanceDTO bemDTO, PolicyDataService policyDataService) {

		BenefitEnrollmentMaintenanceDTO dbBemDto = policyDataService.getLatestBEMByPolicyId(bemDTO);

		if(dbBemDto != null) {

			LOG.debug("Matching Policy found: " + dbBemDto.getPolicyVersionId());
			bemDTO.setPolicyVersionId(dbBemDto.getPolicyVersionId());
			bemDTO.setSubscriberStateCd(dbBemDto.getSubscriberStateCd());
		}

		return dbBemDto;
	}
	

   	/**
	 * Processes inbound premiums
	 * @param bemDTO
	 * @param financialValidator 
	 * @return dbBemDto
	 */
	public Map<LocalDate, AdditionalInfoType> processInboundPremiums(
			BenefitEnrollmentMaintenanceDTO bemDTO, FinancialValidator financialValidator) {

		Map<LocalDate, AdditionalInfoType> inboundPremiums = new LinkedHashMap<LocalDate, AdditionalInfoType>();

		BenefitEnrollmentMaintenanceType bem = bemDTO.getBem();

		List<MemberType> inboundSubscribers = BEMDataUtil.getSubscriberOccurrances(bem);

		//Iterate through each subscriber loop in the bem - first subscriber loop will contain the subscriber attributes
		//along with monthly financial amounts. Subsequent subscriber loops if present (max of 3) will contain the prorated financial amounts
		for (MemberType inboundSubscriber : inboundSubscribers) {

			Map<LocalDate, AdditionalInfoType> tempMap = financialValidator.processInboundPremiums(inboundSubscriber);

			LocalDate premiumEsd = financialValidator.determineSystemSelectedEffectiveStartDate(inboundSubscriber);
			//This check handles the future situation when FFM E&E aligns CIPP(Monthly Premium) Start Date with the IPP Policy Start Date
			if(inboundPremiums.containsKey(premiumEsd)) {

				AdditionalInfoType nonProratedPremium = inboundPremiums.get(premiumEsd);
				inboundPremiums.remove(premiumEsd);

				LocalDate newKey = DateTimeUtil.getLocalDateFromXmlGC(tempMap.get(premiumEsd).getEffectiveEndDate()).plusDays(1);

				inboundPremiums.put(newKey, nonProratedPremium);
			}

			//Create the Map of Premium records to be processed
			inboundPremiums.putAll(tempMap);
		}

		if (!inboundPremiums.isEmpty()) {
			processProratedAmounts(inboundPremiums, bemDTO.getBem());
		}
		//Remove prorated amounts subscriber loops from Bem since it should not be persisted in db
		if(inboundSubscribers.size() > 1) {
			removeProratedSubscribers(bem);
		}

		return inboundPremiums;
	}
     
	
	/*
	 * Get the prorated amounts. The inbound Bem is assumed to have 1 to 3 subscriber loops. 2nd and 3rd if present will carry the prorated amounts
	 */
	
	private void processProratedAmounts(Map<LocalDate, AdditionalInfoType> inboundPremiums, BenefitEnrollmentMaintenanceType bem) {

		LocalDate policyStartDate = BEMDataUtil.getPolicyStartDate(bem);
		LocalDate policyEndDate = BEMDataUtil.getPolicyEndDate(bem);

		if(inboundPremiums.size() > 1) {
			LOG.info("Multiple subscribers present - process prorated amounts from 2nd and 3rd subscriber loops"); 

			//Prevent Overlapping EPS Policy Premium Records for Policies effective <1 Month

			//Identify if any subscriber loop with Prorated amounts has EffectiveStartDate and EffectiveEndDate identical to Policy Start AND End Date
			Iterator<Map.Entry<LocalDate, AdditionalInfoType>> iterator = inboundPremiums.entrySet().iterator();
			Map.Entry<LocalDate, AdditionalInfoType> firstSubscriberPremium = iterator.next();

			LOG.info("first subscriber key: " + firstSubscriberPremium.getKey());

			boolean hasIdenticalDates = false;

			while(iterator.hasNext()){

				Map.Entry<LocalDate, AdditionalInfoType> subscriberPremium = iterator.next();
				AdditionalInfoType additionalInfo = subscriberPremium.getValue();
				LocalDate dateKey = subscriberPremium.getKey();

				//Remove the non-prorated premium which is after policy end date 
				//(This corresponds to removing non prorated monthly premium record for 1/15-2/15 type of scenario)
				// Add null check for PED.  Cannot be null for java.time.localDate else NPE.
				if (policyEndDate != null) {
					if(dateKey.isAfter(policyEndDate)) {
						iterator.remove();
						continue;
					}
				}

				LOG.info("subscriber esd: " + additionalInfo.getEffectiveStartDate());

				LocalDate premiumStartDt = DateTimeUtil.getLocalDateFromXmlGC(additionalInfo.getEffectiveStartDate());
				LocalDate premiumEndDt = DateTimeUtil.getLocalDateFromXmlGC(additionalInfo.getEffectiveEndDate());

				if(premiumStartDt.isEqual(policyStartDate) && premiumEndDt.isEqual(policyEndDate)) {
					hasIdenticalDates = true;
					break;
				}

				//Identify if any subscriber loop with prorated amounts has EffectiveStartDate and EffectiveEndDate identical to those in other subscriber loop (without prorated amounts)
				LocalDate nonProratedEsd = DateTimeUtil.getLocalDateFromXmlGC(firstSubscriberPremium.getValue().getEffectiveStartDate());
				LocalDate nonProratedEed = DateTimeUtil.getLocalDateFromXmlGC(firstSubscriberPremium.getValue().getEffectiveEndDate());

				if(premiumStartDt.isEqual(nonProratedEsd) && premiumEndDt.isEqual(nonProratedEed)) {
					hasIdenticalDates = true;
					break;
				}
			}

			//Prorated Start & End Dates identical to policy/non-prorated subscriber loop
			if(hasIdenticalDates) {
				//Ignore the subscriber loop without prorated amounts and only process the subscriber loop with Prorated Amounts
				inboundPremiums.remove(firstSubscriberPremium.getKey());

			} else {
				//EPS Policy-Premium Record Date Alignment
				performPremiumDateAlignment(inboundPremiums);
			}

		} else { //Single subscriber
			LOG.debug("Single subscriber - Setting policy start and end dates to premium eff start and end dates");

			//Modify EffectiveStartDate and EffectiveEndDate of Policy Premium record to align with the PolicyStartDate and Policy EndDate
			Map.Entry<LocalDate,AdditionalInfoType> entry = inboundPremiums.entrySet().iterator().next();

			LocalDate premiumStartDt = entry.getKey();

			AdditionalInfoType premiumRec = entry.getValue();
			premiumRec.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(policyStartDate));
			premiumRec.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(policyEndDate));

			inboundPremiums.remove(premiumStartDt);
			inboundPremiums.put(policyStartDate, premiumRec);
		}

	}
    
	/*
	 * Remove Prorated Subscriber loops from Bem
	 */
	
	private void removeProratedSubscribers(BenefitEnrollmentMaintenanceType bem) {

		if (bem != null) {
			Iterator<MemberType> iterator = bem.getMember().iterator();
			int subscriberIndex = 0;

			while(iterator.hasNext()) {
				MemberType member = iterator.next();

				//Keep only the first subscriber loop and remove subsequent ones, since amounts have been read and is not to be persisted in db
				if(BEMDataUtil.getIsSubscriber(member)) {
					subscriberIndex++;

					if(subscriberIndex > 1) {
						iterator.remove();
					}					
				}
			}
		}
	}
    
	/*
	 * EPS Policy-Premium Records Date Alignment
	 */
	
	private void performPremiumDateAlignment(Map<LocalDate, AdditionalInfoType> inboundPremiums) {

		Iterator<Map.Entry<LocalDate, AdditionalInfoType>> iterator = inboundPremiums.entrySet().iterator();

		AdditionalInfoType firstSubscriberPremium = iterator.next().getValue();

		LocalDate nonProratedEsd = DateTimeUtil.getLocalDateFromXmlGC(firstSubscriberPremium.getEffectiveStartDate());

		while(iterator.hasNext()){

			AdditionalInfoType additionalInfo = iterator.next().getValue();

			LocalDate premiumStartDt = DateTimeUtil.getLocalDateFromXmlGC(additionalInfo.getEffectiveStartDate());
			LocalDate premiumEndDt = DateTimeUtil.getLocalDateFromXmlGC(additionalInfo.getEffectiveEndDate());

			if(!premiumStartDt.isAfter(nonProratedEsd)) {
				//For any PolicyPremium record where the preceding record has prorated amounts, modify EffectiveStartDate of the selected record to be equal to the EffectiveEndDate of preceding record + 1 calendar day
				firstSubscriberPremium.setEffectiveStartDate(
						DateTimeUtil.getXMLGregorianCalendar(premiumEndDt.plusDays(1)));
			} else {
				//For any PolicyPremium record where the trailing record has prorated amounts, modify EffectiveEndDate of the selected record to be equal to the EffectiveStartDate of the trailing record - 1 calendar day
				firstSubscriberPremium.setEffectiveEndDate(
						DateTimeUtil.getXMLGregorianCalendar(premiumStartDt.minusDays(1)));
			}
		}
	}

	


}
