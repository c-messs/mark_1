/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep;

import gov.cms.dsh.bem.AdditionalInfoType;
import gov.cms.dsh.bem.BenefitEnrollmentMaintenanceType;
import gov.cms.dsh.bem.BooleanIndicatorSimpleType;
import gov.cms.dsh.bem.HealthCoverageDatesType;
import gov.cms.dsh.bem.HealthCoveragePolicyNumberType;
import gov.cms.dsh.bem.HealthCoverageType;
import gov.cms.dsh.bem.MemberType;
import gov.hhs.cms.ff.fm.eps.ep.enums.PolicyStatus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

/**
 * This is a utility class to extract various specific information types
 * from the BenefitEnrollment transaction
 * 
 * @author girish.padmanabhan
 */
public class BEMDataUtil {

	private static final int VARIANT_ID_BEG_INDEX = 14;
	private static final int VARIANT_ID_END_INDEX = 16;



	/**
	 * Utility method to get the Benefit Begin Date
	 * @param member
	 * @return
	 */
	public static DateTime getBenefitBeginDate(MemberType member) {
		DateTime benefitBeginDate = null;

		if (member != null) {
			for (HealthCoverageType healthCoverage : member.getHealthCoverage()) {
				HealthCoverageDatesType healthCoverageDates = healthCoverage.getHealthCoverageDates();
				if (healthCoverageDates != null) {
					XMLGregorianCalendar hcBBD = healthCoverageDates.getBenefitBeginDate();
					if (hcBBD != null) {
						benefitBeginDate = new DateTime(hcBBD.toGregorianCalendar().getTime());
						break;
					}
				}
			}
		}
		return benefitBeginDate;
	}


	/**
	 * Utility method to get the Benefit Begin Date
	 * @param member
	 * @return
	 */
	public static DateTime getBenefitEndDate(MemberType member) {
		DateTime benefitEndDate = null;

		if (member != null) {
			for (HealthCoverageType healthCoverage : member.getHealthCoverage()) {
				HealthCoverageDatesType healthCoverageDates = healthCoverage.getHealthCoverageDates();
				if (healthCoverageDates != null) {
					XMLGregorianCalendar hcBED = healthCoverageDates.getBenefitEndDate();
					if (hcBED != null) {
						benefitEndDate = new DateTime(hcBED.toGregorianCalendar().getTime());
						break;
					}
				}
			}
		}
		return benefitEndDate;
	}



	/**
	 * Utility method to get the MarketplaceGroupPolicyIdentifier from BEMs PolicyInfo.
	 * @param bem
	 * @return policyStart
	 */
	public static String getMarketplaceGroupPolicyIdentifier(BenefitEnrollmentMaintenanceType bem) {

		String mgpId = null;
		if (bem != null) {
			if (bem.getPolicyInfo() != null) {
				mgpId = bem.getPolicyInfo().getMarketplaceGroupPolicyIdentifier();
			}
		}
		return mgpId;			
	}

	/**
	 * Utility method to get the Policy Start Date from BEMs PolicyInfo.
	 * @param bem
	 * @return policyStart
	 */
	public static DateTime getPolicyStartDate(BenefitEnrollmentMaintenanceType bem) {

		DateTime policyStart = null;
		if (bem != null) {
			if (bem.getPolicyInfo() != null) {
				XMLGregorianCalendar xmlPSD = bem.getPolicyInfo().getPolicyStartDate();
				if (xmlPSD != null) {
					policyStart = new DateTime(xmlPSD.toGregorianCalendar().getTime());
				}
			}
		}
		return policyStart;			
	}


	/**
	 * Utility method to get the Policy End Date from PolicyInfo.
	 * @param bem
	 * @return policyEnd
	 */
	public static DateTime getPolicyEndDate(BenefitEnrollmentMaintenanceType bem) {

		DateTime policyEnd = null;
		if (bem != null) {
			if (bem.getPolicyInfo() != null) {
				XMLGregorianCalendar xmlPED = bem.getPolicyInfo().getPolicyEndDate();
				if (xmlPED != null) {
					policyEnd = new DateTime(xmlPED.toGregorianCalendar().getTime());
				}
			}
		}
		return policyEnd;		
	}

	/**
	 *  Utility method to get the EligibilityBeginDate from a member.
	 * @param member
	 * @return eligibilityBeginDate
	 */
	public static DateTime getEligibilityBeginDate(MemberType member) {
		DateTime eligibilityBegin = null;

		if (member != null) {
			if(member.getMemberRelatedDates() != null) {
				XMLGregorianCalendar eligibilityBeginXml = member.getMemberRelatedDates().getEligibilityBeginDate();

				if (eligibilityBeginXml != null)  {
					eligibilityBegin = new DateTime(eligibilityBeginXml.toGregorianCalendar().getTime());
				}
			}
		}
		return eligibilityBegin;
	}

	/**
	 *  Utility method to get the EligibilityEndDate from a member.
	 * @param member
	 * @return eligibilityEnd
	 */
	public static DateTime getEligibilityEndDate(MemberType member) {

		DateTime eligibilityEnd = null;

		if (member != null) {
			if(member.getMemberRelatedDates() != null) {
				XMLGregorianCalendar eligibilityEndXml = member.getMemberRelatedDates().getEligibilityEndDate();

				if (eligibilityEndXml != null)  {
					eligibilityEnd = new DateTime(eligibilityEndXml.toGregorianCalendar().getTime());
				}
			}
		}
		return eligibilityEnd;
	}

	/**
	 * Utility method to get the FIRST subscriber member in the BEM.
	 * @param bem
	 * @return subscriber member
	 */
	public static MemberType getSubscriberMember(BenefitEnrollmentMaintenanceType bem) {
		MemberType subscriberMember = null;

		if (bem != null) {
			List<MemberType> memberList = bem.getMember();
			for (MemberType member : memberList) {
				if (getIsSubscriber(member)) {
					subscriberMember = member;
					break;
				}
			}
		}
		return subscriberMember;
	}

	/**
	 * Utility method to determine if member is a subscriber
	 * @param member
	 * @return isSubscriber
	 */
	public static boolean getIsSubscriber(MemberType member) {

		boolean isSubcriber = false;

		if (member != null) {
			if (member.getMemberInformation() != null) {
				if (member.getMemberInformation().getSubscriberIndicator() != null) {
					if (member.getMemberInformation().getSubscriberIndicator().value().equalsIgnoreCase(
							BooleanIndicatorSimpleType.Y.value())) {
						isSubcriber = true;
					}
				}
			}
		}
		return isSubcriber;
	}

	/**
	 * Utility method to get the health coverage type
	 * @param member
	 * @return health coverage type
	 */
	public static HealthCoverageType getHealthCoverageType(MemberType member) {
		HealthCoverageType healthCoverage = null;
		if (member != null) {
			List<HealthCoverageType> healthCoverageList = member.getHealthCoverage();
			if (CollectionUtils.isNotEmpty(healthCoverageList)) {
				healthCoverage = healthCoverageList.get(0);
			}
		}
		return healthCoverage;
	}

	/**
	 * @param member
	 * @return HealthCoverageDatesType
	 */
	public static HealthCoverageDatesType getHealthCoverageDatesType(MemberType member) {

		HealthCoverageDatesType healthCoverageDates = null;

		HealthCoverageType healthCoverage = getHealthCoverageType(member);

		if (healthCoverage != null) {

			healthCoverageDates = healthCoverage.getHealthCoverageDates();
		}
		return healthCoverageDates;
	}

	/**
	 * Utility method to get the insurance line code
	 * @param subscriberMember
	 * @return insurance line code
	 */
	public static String getInsuranceLineCode(MemberType subscriberMember) {

		String insuranceLineCode = null;

		HealthCoverageType healthCoverage = getHealthCoverageType(subscriberMember);

		if(healthCoverage != null && healthCoverage.getHealthCoverageInformation() != null) {
			if (healthCoverage.getHealthCoverageInformation().getInsuranceLineCode() != null) {
				insuranceLineCode = healthCoverage.getHealthCoverageInformation().getInsuranceLineCode().value();
			}
		}
		return insuranceLineCode;
	}

	/**
	 * Utility method to get the health coverage policy number
	 * @param member
	 * @return health coverage policy number
	 */
	public static HealthCoveragePolicyNumberType getHealthCoveragePolicyNumber(MemberType member) {
		HealthCoveragePolicyNumberType healthCoveragePolicyNumber = null;
		HealthCoverageType healthCoverage = getHealthCoverageType(member);
		if (healthCoverage != null) {
			healthCoveragePolicyNumber = healthCoverage.getHealthCoveragePolicyNumber();
		}
		return healthCoveragePolicyNumber;
	}

	/**
	 * Utility method to get the contract code
	 * @param member
	 * @return contract code
	 */
	public static String getContractCode(MemberType member) {
		String contractCode = null;
		if (member != null) {
			HealthCoveragePolicyNumberType healthCoveragePolicyNumber = getHealthCoveragePolicyNumber(member);
			if (healthCoveragePolicyNumber != null &&  StringUtils.isNotBlank(healthCoveragePolicyNumber.getContractCode())) {
				contractCode = healthCoveragePolicyNumber.getContractCode();
			}
		}
		return contractCode;
	}


	/**
	 * Utility method to get the variantId (EPS InsurancePlanVariantCode) from the member.
	 * @param subscriber
	 * @return variantId
	 */
	public static String getVariantId(MemberType subscriber) {

		String variantId = null;
		String contractCode = getContractCode(subscriber);
		if (StringUtils.isNotBlank(contractCode)) {
			if (contractCode.length() == VARIANT_ID_END_INDEX) {
				variantId = contractCode.substring(VARIANT_ID_BEG_INDEX, VARIANT_ID_END_INDEX);
			}
		}
		return variantId;
	}


	/**
	 * Utility method to get the Total Premium Amount
	 * @param member
	 * @return total premium amount
	 */
	public static BigDecimal getTotalPremiumAmount(MemberType member) {
		BigDecimal tpa = null;
		if (member != null) {
			for (AdditionalInfoType ait : member.getAdditionalInfo()) {
				if (ait.getTotalPremiumAmount() != null) {
					tpa = ait.getTotalPremiumAmount();
					break;
				}
			}
		}
		return tpa;
	}

	/**
	 * @param addlInfoList
	 */
	public static void sortAdditionalInfos(List<AdditionalInfoType> addlInfoList) {

		if (addlInfoList.size() > 1) {
			// Sort the list by EffectiveStartDate in order to find each "time slice"
			Collections.sort(addlInfoList, new Comparator<AdditionalInfoType>() {
				/**
				 * @param ait0
				 * @param ait1
				 * @return
				 */
				public int compare(AdditionalInfoType ait0, AdditionalInfoType ait1) {
					XMLGregorianCalendar dt0 = ait0.getEffectiveStartDate();
					XMLGregorianCalendar dt1 = ait1.getEffectiveStartDate();
					if (dt0 == null) {
						return 1;
					} else if (dt1 == null) {
						return -1;
					}
					return dt0.compare(dt1);
				}
			});
		}
	}


	/**
	 * Extracts and returns the ExchangeAssignedMemberId for a member.
	 * @param member
	 * @return id
	 */
	public static String getExchangeAssignedMemberId(MemberType member) {

		String id = null;
		if (member != null) {
			if (member.getMemberAdditionalIdentifier() != null) {
				id = member.getMemberAdditionalIdentifier().getExchangeAssignedMemberID();
			}
		}
		return id;
	}

	/**
	 * Extracts all "member loops" for the subscriber.
	 * 
	 * @param bem
	 * @return List<MemberType>
	 */
	public static List<MemberType> getSubscriberOccurrances(BenefitEnrollmentMaintenanceType bem) {

		List<MemberType> subscriberEntries = new ArrayList<MemberType>();

		if (bem != null) {
			for (MemberType member : bem.getMember()) {
				if (getIsSubscriber(member)) {
					subscriberEntries.add(member);
				}
			}
		}
		return subscriberEntries;
	}

	/**
	 * Extracts and returns the Additional Info related to Financial Amounts 
	 * identified as key financial elements.
	 * 
	 * @param additionalInfoTypes
	 * @return
	 */
	public static AdditionalInfoType getFinancialElements(List<AdditionalInfoType> additionalInfoTypes) {

		if(CollectionUtils.isNotEmpty(additionalInfoTypes)) {

			for(AdditionalInfoType additionalInfo : additionalInfoTypes) {
				if (isKeyFinancialElement(additionalInfo)) {
					return additionalInfo;
				}				
			}
		}
		return null;
	}

	/**
	 * Determines if the AdditionalInfoType is a Key Financial Element based on which
	 * attribute is set. 
	 * @param ait
	 * @return
	 */
	public static boolean isKeyFinancialElement(AdditionalInfoType ait) {
		boolean isKeyElem = false;

		if (ait.getAPTCAmount() != null || ait.getTotalIndividualResponsibilityAmount() != null
				|| ait.getTotalPremiumAmount() != null || ait.getCSRAmount() != null) {
			isKeyElem = true;
		}
		return isKeyElem;
	}

	/**
	 * Determines if a member has any Key Financial Elements present in AdditionalInfoType list. 
	 * @param member
	 * @return
	 */
	public static boolean isKeyFinancialElementPresent(MemberType member) {

		boolean isKeyElemPresent = false;

		if (member != null) {
			for (AdditionalInfoType ait : member.getAdditionalInfo()) {
				if (isKeyFinancialElement(ait)) {
					isKeyElemPresent = true;
					break;
				}
			}
		}
		return isKeyElemPresent;
	}

	/**
	 * Utility method to get the SubscriberID of a member.
	 * Returns only if SubscriberId "isNotBlank" 
	 * @param member
	 * @return group policy number
	 */
	public static String getSubscriberID(MemberType member) {
		String subscriberID = null;
		if (member != null) {
			if (member.getSubscriberID() != null &&  StringUtils.isNotBlank(member.getSubscriberID())) {
				subscriberID = member.getSubscriberID();
			}
		}
		return subscriberID;
	}


	/**
	 * Get PolicyStatus (Enum) from BemDTO -> Bem -> PolicyInfoType
	 * @param bemDTO
	 * @return
	 */
	public static PolicyStatus getPolicyStatus(BenefitEnrollmentMaintenanceDTO bemDTO) {

		return getPolicyStatus(bemDTO.getBem());
	}

	/**
	 *  Get PolicyStatus (Enum) from Bem -> PolicyInfoType
	 * @param bem
	 * @return
	 */
	public static PolicyStatus getPolicyStatus(BenefitEnrollmentMaintenanceType bem) {

		PolicyStatus status = null;
		if (bem != null && bem.getPolicyInfo() != null ) {
			status = PolicyStatus.getEnum(bem.getPolicyInfo().getPolicyStatus());
		}
		return status;
	}

	/**
	 * Get CurrentTimeStamp from Bem.
	 * @param bem
	 * @return
	 */
	public static XMLGregorianCalendar getCurrentTimeStamp(BenefitEnrollmentMaintenanceType bem) {

		XMLGregorianCalendar currentTimeStamp = null;
		if (bem != null) {
			if (bem.getTransactionInformation() != null) {
				currentTimeStamp = bem.getTransactionInformation().getCurrentTimeStamp();
			}
		}
		return currentTimeStamp;
	}

	/**
	 * Get subscriberStateCd from member's contractCode, if they are a subscriber
	 * @param subscriber
	 * @return String
	 */
	public static String getSubscriberStateCode(MemberType subscriber) {

		String subscriberStateCode = null;

		if (BEMDataUtil.getIsSubscriber(subscriber)) {
			String contractCode = BEMDataUtil.getContractCode(subscriber);

			if (StringUtils.isNotBlank(contractCode) && contractCode.length() > 7) {
				subscriberStateCode = contractCode.substring(5, 7);
			}
		}

		return subscriberStateCode;
	}

	/**
	 * Extract InternalControlNumber (EPS IssuerPolicyID) from member.
	 * @param subscriber
	 * @return String
	 */
	public static String getInternalControlNumber(MemberType subscriber) {

		String icn = null;

		HealthCoveragePolicyNumberType hcPolicyNum = getHealthCoveragePolicyNumber(subscriber);

		if (hcPolicyNum != null) {
			icn = hcPolicyNum.getInternalControlNumber();
		}
		return icn;
	}

	/**
	 * @param subscriber
	 * @return String
	 */
	public static String getIssuerSubscriberID(MemberType subscriber) {

		String issuerSubscriberID = null;
		if (subscriber != null) {
			if (subscriber.getMemberAdditionalIdentifier() != null) {
				issuerSubscriberID = subscriber.getMemberAdditionalIdentifier().getIssuerAssignedSubscriberID();
			}
		}
		return issuerSubscriberID;
	}

	/**
	 * @param subscriber
	 * @return String
	 */
	public static String getIssuerHIOSID(MemberType subscriber) {

		String issuerHIOSID = null;

		String contractCode = BEMDataUtil.getContractCode(subscriber);

		if (contractCode != null) {
			if (contractCode.length() > 5) {
				issuerHIOSID = contractCode.substring(0, 5);
			}
		}
		return issuerHIOSID;
	}


	/**
	 * Gets ExchangePolicyId (GroupPolicyNumber) from BEM PolicyInfo
	 * @param bem
	 * @return String
	 */
	public static String getExchangePolicyID(BenefitEnrollmentMaintenanceType bem) {

		String exchangePolicyID = null;
		if (bem != null) {
			if (bem.getPolicyInfo() != null) {
				exchangePolicyID = bem.getPolicyInfo().getGroupPolicyNumber();
			}
		}
		return exchangePolicyID;
	}

	/** Extract EPS Plan Id from HC contrateCode
	 * @param subscriber
	 * @return  String
	 */
	public static String getPlanID(MemberType subscriber) {

		String planID = null;

		String contractCode = BEMDataUtil.getContractCode(subscriber);

		if (contractCode != null) {
			if (contractCode.length() > 14) {
				planID = contractCode.substring(0, 14);
			}
		}

		return planID;
	}


}
