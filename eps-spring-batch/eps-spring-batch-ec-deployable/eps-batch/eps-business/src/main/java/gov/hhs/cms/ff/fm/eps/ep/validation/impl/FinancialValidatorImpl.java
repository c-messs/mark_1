package gov.hhs.cms.ff.fm.eps.ep.validation.impl;

import gov.cms.dsh.bem.AdditionalInfoType;
import gov.cms.dsh.bem.MemberType;
import gov.hhs.cms.ff.fm.eps.ep.BEMDataUtil;
import gov.hhs.cms.ff.fm.eps.ep.util.EpsDateUtils;
import gov.hhs.cms.ff.fm.eps.ep.validation.FinancialValidator;

import java.util.HashMap;
import java.util.Map;

import javax.xml.datatype.XMLGregorianCalendar;

import org.joda.time.DateTime;

/**
 * @author j.radziewski
 * 
 *
 */
public class FinancialValidatorImpl implements FinancialValidator {

	@Override
	public Map<DateTime, AdditionalInfoType> processInboundPremiums(MemberType inboundSubscriber) {

		Map<DateTime, AdditionalInfoType> inboundPremiums = new HashMap<DateTime, AdditionalInfoType>();
		// Determine the system selected effective start date for this subscriber loop.
		DateTime sysSelESD = determineSystemSelectedEffectiveStartDate(inboundSubscriber);

		if (sysSelESD != null) {
			AdditionalInfoType epsPremiumNew = createEpsPremium(sysSelESD, inboundSubscriber);
			inboundPremiums.put(sysSelESD, epsPremiumNew);	
		}
		
		return inboundPremiums;
	}
	
	/**
	 * Determines the "System Selected" EffectiveStartDate (ESD) which is the max ESD 
	 * among the inbound Key Financial elements of AdditionalInfo list
	 *  
	 * @param subscriber
	 * @return sysSelESD
	 */
	@Override
	public DateTime determineSystemSelectedEffectiveStartDate(MemberType subscriber) {

		DateTime sysSelESD = null;
		
		AdditionalInfoType inboundKeyFinancialAmount = BEMDataUtil.getFinancialElements(subscriber.getAdditionalInfo());

		if (inboundKeyFinancialAmount != null) {
			sysSelESD = EpsDateUtils.getDateTimeFromXmlGC(
					inboundKeyFinancialAmount.getEffectiveStartDate());
		}
		return sysSelESD;
	}

	/**
	 * Creates a "flat" AdditionalInfoType premium record, or "timeslice"
	 * @param sysSelESD
	 * @param inboundSubscriber
	 * @return premium
	 */
	private AdditionalInfoType createEpsPremium(DateTime sysSelESD, MemberType inboundSubscriber) {

		AdditionalInfoType epsPremium = new AdditionalInfoType();

		updateEpsPremium(sysSelESD, inboundSubscriber, epsPremium);

		return epsPremium;
	}

	/**
	 * Updates a "flat" AdditionalInfoType premium record, or "timeslice"
	 * @param sysSelESD
	 * @param inboundSubscriber
	 * @param premium
	 */
	private void updateEpsPremium(DateTime sysSelESD, MemberType inboundSubscriber, AdditionalInfoType epsPremium) {

		epsPremium.setEffectiveStartDate(EpsDateUtils.getXMLGregorianCalendar(sysSelESD));

		if (isEffectiveEndDatePresent(inboundSubscriber))  {

			epsPremium.setEffectiveEndDate(getEffectiveEndDate(inboundSubscriber));

		} else {
			// "Clear" EPS EED. This will take care of null inbound EED with existing EPS EED.
			epsPremium.setEffectiveEndDate(null);
		}
		// "Clear" EPS amounts.  This will take care of "null" replacements
		clearEpsPremium(epsPremium);

		for (AdditionalInfoType ait : inboundSubscriber.getAdditionalInfo()) {

			// Key Financial Elements (KFE)
			if (ait.getAPTCAmount() != null) {
				epsPremium.setAPTCAmount(ait.getAPTCAmount());
			}
			if (ait.getCSRAmount() != null) {
				epsPremium.setCSRAmount(ait.getCSRAmount());
			}
			if (ait.getTotalPremiumAmount() != null) {
				epsPremium.setTotalPremiumAmount(ait.getTotalPremiumAmount());
			}
			if (ait.getTotalIndividualResponsibilityAmount() != null) {
				epsPremium.setTotalIndividualResponsibilityAmount(ait.getTotalIndividualResponsibilityAmount());
			}
			
			// Non-Key Financial Elements only get overwritten if in inbound, otherwise "carry-over" 
			if (ait.getRatingArea() != null) {
				epsPremium.setRatingArea(ait.getRatingArea());
			}
			
			//Prorated Amounts
			updateProratedAmounts(ait, epsPremium);		
		}
	}
	
	private void clearEpsPremium(AdditionalInfoType epsPremium) {
		
		epsPremium.setAPTCAmount(null);
		epsPremium.setCSRAmount(null);
		epsPremium.setTotalPremiumAmount(null);
		epsPremium.setTotalIndividualResponsibilityAmount(null);
	}
	
	private void updateProratedAmounts(AdditionalInfoType ait, AdditionalInfoType epsPremium) {
		
		if (ait.getProratedAppliedAPTCAmount() != null) {
			epsPremium.setProratedAppliedAPTCAmount(ait.getProratedAppliedAPTCAmount());
		}	
		if (ait.getProratedCSRAmount() != null) {
			epsPremium.setProratedCSRAmount(ait.getProratedCSRAmount());
		}	
		if (ait.getProratedMonthlyPremiumAmount() != null) {
			epsPremium.setProratedMonthlyPremiumAmount(ait.getProratedMonthlyPremiumAmount());
		}	
		if (ait.getProratedIndividualResponsibleAmount() != null) {
			epsPremium.setProratedIndividualResponsibleAmount(ait.getProratedIndividualResponsibleAmount());
		}		
		
	}

	/**
	 * Identify whether EffectiveEndDate has been provided on any single key financial element
	 * @param inboundSubscriber
	 * @return
	 */
	private boolean isEffectiveEndDatePresent(MemberType inboundSubscriber) {

		boolean isEEDPresent = false;
		for (AdditionalInfoType ait : inboundSubscriber.getAdditionalInfo()) {
			if (BEMDataUtil.isKeyFinancialElement(ait)) {
				if (ait.getEffectiveEndDate() != null) {
					isEEDPresent = true;
					break;
				}
			}
		}
		return isEEDPresent;
	}

	/**
	 * Extracts and returns the EffectiveEndDate from the first key financial element
	 * @param inboundSubscriber
	 * @return
	 */
	private XMLGregorianCalendar getEffectiveEndDate(MemberType inboundSubscriber) {

		XMLGregorianCalendar eed = null;
		for (AdditionalInfoType ait : inboundSubscriber.getAdditionalInfo()) {
			if (BEMDataUtil.isKeyFinancialElement(ait)) {
				if (ait.getEffectiveEndDate() != null) {
					eed = ait.getEffectiveEndDate();
					break;
				}
			}
		}
		return eed;
	}

}
