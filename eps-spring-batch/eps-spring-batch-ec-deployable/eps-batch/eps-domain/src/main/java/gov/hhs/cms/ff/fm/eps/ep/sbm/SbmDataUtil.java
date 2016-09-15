package gov.hhs.cms.ff.fm.eps.ep.sbm;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.cms.dsh.sbmi.FileInformationType;
import gov.cms.dsh.sbmi.PolicyMemberType;
import gov.cms.dsh.sbmi.PolicyType;

/**
 * @author j.radziewski
 * 
 * Utility class for extracting SBM specific data from Enrollment transactions.
 *
 */
public class SbmDataUtil {

	private static final Logger LOG = LoggerFactory.getLogger(SbmDataUtil.class);

	
	/**
	 * Extract StateCd from TenantId.
	 * @param tenantId
	 * @return
	 */
	public static String getStateCd(String tenantId) {

		String stateCd = null;
		if (tenantId != null) {
			if (tenantId.length() >= 2) {
				stateCd = tenantId.substring(0, 2);
			}
		}
		return stateCd;
	}

	/**
	 * Extract StateCd from SBM FileInformationType.
	 * @param fileInfoType
	 * @return
	 */
	public static String getStateCd(FileInformationType fileInfoType) {

		String stateCd = null;
		if (fileInfoType != null) {
			stateCd = getStateCd(fileInfoType.getTenantId());
		}
		return stateCd;
	}


	/**
	 * Extract IssuerId from QhpId.
	 * @param qhpId
	 * @return
	 */
	public static String getIssuerIdFromQhpId(String qhpId) {
		String issuerId = StringUtils.EMPTY;
		int HIOS_ID_INDEX = 5;

		if (qhpId.length() >= HIOS_ID_INDEX) {
			issuerId = qhpId.substring(0, HIOS_ID_INDEX);

		} else {
			LOG.debug("Invalid QHP ID, unable to extract HIOS ID for QHPID = " + qhpId);
		}
		return issuerId;
	}

	/**
	 * Method to get State code from QHP id. 
	 * @param qhpId
	 * @return stateCd
	 */
	public static String getStateCdFromQhpId(String qhpId) {
		String stateCd = StringUtils.EMPTY;

		if (qhpId.length() >= 7) {
			stateCd = qhpId.substring(5, 7);

		} else {
			LOG.debug("Invalid QHP ID, unable to extract State Code for QHPID = " + qhpId);
		}
		return stateCd;
	}

	/**
	 * Extract Tenant number from TenantId.
	 * @param tenantId
	 * @return
	 */
	public static String getTenantNum(String tenantId) {

		String num = null;
		if (tenantId != null) {
			if (tenantId.length() >= 3) {
				num = tenantId.substring(2, 3);
			}
		}
		return num;
	}

	/**
	 * Extract Tenant number from SBM FileInformationType.
	 * @param fileInfoType
	 * @return
	 */
	public static String getTenantNum(FileInformationType fileInfoType) {

		String num = null;
		if (fileInfoType != null) {
			num = getTenantNum(fileInfoType.getTenantId());
		}
		return num;
	}


	/**
	 * Extract IssuerId from SBM FileInformationType.
	 * @param fileInfoType
	 * @return
	 */
	public static String getIssuerId(FileInformationType fileInfoType) {

		String issuerId = null;
		if (fileInfoType != null) {
			if (fileInfoType.getIssuerFileInformation() != null) {
				issuerId = fileInfoType.getIssuerFileInformation().getIssuerId();
			}
		}
		return issuerId;
	}

	/**
	 * Determines if variant ID is a Non-CSR Variant (01)
	 * @param variantId
	 * @return isValidCsrVariant
	 */
	/**
	 * @param variantId
	 * @return
	 */
	public static boolean isNonCsrVariant(String variantId) {

		return (SBMConstants.VARIANT_ID_01.equals(variantId));
	}

	/**
	 * Determines if variant ID is a CSR Variant (02-06)
	 * @param variantId
	 * @return isCsrVariant
	 */
	public static boolean isCsrVariant(String variantId) {

		boolean isCsrVariant = false;
		if (SBMConstants.VARIANT_ID_02.equals(variantId) 
				|| SBMConstants.VARIANT_ID_03.equals(variantId) 
				|| SBMConstants.VARIANT_ID_04.equals(variantId) 
				|| SBMConstants.VARIANT_ID_05.equals(variantId)
				|| SBMConstants.VARIANT_ID_06.equals(variantId)) {
			isCsrVariant = true;
		}
		return isCsrVariant;
	}
	
	/**
	 * Extract PolicyMemberType from SBM PolicyType.
	 * @param policy
	 * @return
	 */
	public static PolicyMemberType getSubsciber(PolicyType policy) {
		
		PolicyMemberType subscriber = null;
		if (policy != null) {
			for (PolicyMemberType member : policy.getMemberInformation()) {
				if (member.getSubscriberIndicator() != null) {
					if (member.getSubscriberIndicator().equals("Y")) {
						subscriber = member;
					}
				}
			}
		}
		return subscriber;
	}


}
