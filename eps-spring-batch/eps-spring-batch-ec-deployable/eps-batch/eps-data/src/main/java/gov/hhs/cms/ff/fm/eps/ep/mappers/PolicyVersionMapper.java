package gov.hhs.cms.ff.fm.eps.ep.mappers;

import gov.cms.dsh.bem.BenefitEnrollmentMaintenanceType;
import gov.cms.dsh.bem.ExchangeCodeSimpleType;
import gov.cms.dsh.bem.HealthCoverageDatesType;
import gov.cms.dsh.bem.IssuerType;
import gov.cms.dsh.bem.MemberType;
import gov.cms.dsh.bem.TransactionInformationType;
import gov.hhs.cms.ff.fm.eps.ep.BEMDataUtil;
import gov.hhs.cms.ff.fm.eps.ep.enums.EProdEnum;
import gov.hhs.cms.ff.fm.eps.ep.enums.InsuranceApplicationType;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyVersionPO;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;

import java.time.LocalDateTime;

import com.accenture.foundation.common.exception.ApplicationException;
/**
 * @author EPS
 *
 */
public class PolicyVersionMapper {

	/**
	 * @param bem
	 * @return
	 */
	public PolicyVersionPO mapFFMToEPS(BenefitEnrollmentMaintenanceType bem) {

		PolicyVersionPO po = new PolicyVersionPO();

		if (bem != null) {

			IssuerType issuer = bem.getIssuer();

			MemberType subscriber = BEMDataUtil.getSubscriberMember(bem);
			
			po.setMarketplaceGroupPolicyId(BEMDataUtil.getMarketplaceGroupPolicyIdentifier(bem));
			po.setPolicyStartDate(BEMDataUtil.getPolicyStartDate(bem));
			po.setPolicyEndDate(BEMDataUtil.getPolicyEndDate(bem));
			po.setExchangePolicyId(BEMDataUtil.getExchangePolicyID(bem));

			setSubcriberAttributesToPo(po, subscriber);

			if (bem.getTransactionInformation() != null) {
				if (bem.getTransactionInformation().getCurrentTimeStamp() != null) {
					po.setTransDateTime(DateTimeUtil.getLocalDateTimeFromXmlGC(bem.getTransactionInformation().getCurrentTimeStamp()));
				}

				po.setTransControlNum(bem.getTransactionInformation().getControlNumber());
				po.setInsrncAplctnTypeCd(getInsuranceApplicationTypeCd(bem.getTransactionInformation()));

				if (bem.getTransactionInformation().getPolicySnapshotVersionNumber() != null) {
					try {
						po.setSourceVersionId(Long.valueOf(bem.getTransactionInformation().getPolicySnapshotVersionNumber()));
					} catch (NumberFormatException nfEx) {
						//Logically, this could never happen here, BatchTransMsgMapper will catch long before this point.
						throw new ApplicationException(EProdEnum.EPROD_99.getCode(), nfEx);
					}
				}
				po.setSourceVersionDateTime(DateTimeUtil.getLocalDateTimeFromXmlGC(bem.getTransactionInformation().getPolicySnapshotDateTime()));
			}

			// txnMessageID is set in PolicyDataService.saveBEM

			if (issuer != null) {
				if (issuer.getName() != null) {
					po.setIssuerNm(issuer.getName());
				}
				po.setIssuerTaxPayerId(issuer.getTaxPayerIdentificationNumber());
			}

			po.setMaintenanceStartDateTime(LocalDateTime.now());
			po.setMaintenanceEndDateTime(DateTimeUtil.HIGHDATE);
		}

		return po;
	}


	/*
	 * Sets the subscriber level attributes to PolicyversionPO
	 */
	private void setSubcriberAttributesToPo(PolicyVersionPO po, MemberType subscriber) {

		if (subscriber != null) {

			po.setSubscriberStateCd(BEMDataUtil.getSubscriberStateCode(subscriber));
			po.setIssuerPolicyId(BEMDataUtil.getInternalControlNumber(subscriber));
			po.setIssuerHiosId(BEMDataUtil.getIssuerHIOSID(subscriber));
			po.setIssuerSubscriberID(BEMDataUtil.getIssuerSubscriberID(subscriber));
			po.setExchangeAssignedSubscriberID(subscriber.getSubscriberID());
	        po.setEligibilityStartDate(BEMDataUtil.getEligibilityBeginDate(subscriber));
		    po.setEligibilityEndDate(BEMDataUtil.getEligibilityEndDate(subscriber));

			HealthCoverageDatesType healthCoverageDates = BEMDataUtil.getHealthCoverageDatesType(subscriber);
			if (healthCoverageDates != null) {
				po.setPremiumPaidToEndDate(DateTimeUtil.getLocalDateFromXmlGC(healthCoverageDates.getPremiumPaidToDateEnd()));
				po.setLastPremiumPaidDate(DateTimeUtil.getLocalDateFromXmlGC(healthCoverageDates.getLastPremiumPaidDate()));			
			}

			po.setPlanID(BEMDataUtil.getPlanID(subscriber));
			po.setX12InsrncLineTypeCd(BEMDataUtil.getInsuranceLineCode(subscriber));
		}
	}

	/*
	 * Get insuranceApplicationTypeCd
	 */
	private String getInsuranceApplicationTypeCd(TransactionInformationType transactionInformation) {

		String insuranceApplicationTypeCd = null;

		if (transactionInformation != null && transactionInformation.getExchangeCode() != null) {

			String exchangeCode = transactionInformation.getExchangeCode().value();

			if (ExchangeCodeSimpleType.INDIVIDUAL.value().equalsIgnoreCase(exchangeCode)) {
				insuranceApplicationTypeCd = InsuranceApplicationType.INDIVIDUAL.getValue();
			}
			if (ExchangeCodeSimpleType.SHOP.value().equalsIgnoreCase(exchangeCode)) {
				insuranceApplicationTypeCd = InsuranceApplicationType.SHOP.getValue();
			}
		}
		return insuranceApplicationTypeCd;
	}

	/**
	 * @param bemVO
	 * @param po
	 * @return BenefitEnrollmentMaintenanceVO
	 */
	public BenefitEnrollmentMaintenanceType mapPOToVO(BenefitEnrollmentMaintenanceType bem, PolicyVersionPO po) {
		

		IssuerType issuerType = new IssuerType();

		issuerType.setHIOSID(po.getIssuerHiosId());
		
		bem.setIssuer(issuerType);

		return bem;
	}
	
}
