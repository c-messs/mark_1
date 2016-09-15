package gov.hhs.cms.ff.fm.eps.ep.sbm.mappers;

import gov.cms.dsh.sbmi.FileInformationType;
import gov.cms.dsh.sbmi.PolicyType;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyVersionPO;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmPolicyVersionPO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMPolicyDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SbmDataUtil;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;

/**
 * @author j.radziewski
 *
 */
public class SbmPolicyVersionMapper {

	/**
	 * @param inboundDTO
	 * @param epsPO
	 * @return SbmPolicyVersionPO
	 */
	public SbmPolicyVersionPO mapSbmToStaging(SBMPolicyDTO inboundDTO, SbmPolicyVersionPO epsPO) {

		SbmPolicyVersionPO po = new SbmPolicyVersionPO();

		FileInformationType fileInfoType = inboundDTO.getFileInfo();

		if (fileInfoType != null) {
			// TODO first segmentt of filename.
			po.setSourceExchangeId(fileInfoType.getTenantId());
			po.setTransDateTime(DateTimeUtil.getLocalDateTimeFromXmlGC(fileInfoType.getFileCreateDateTime()));
		}

		PolicyType policy = inboundDTO.getPolicy();

		if (policy != null) {

			po.setExchangePolicyId(policy.getExchangeAssignedPolicyId());
			po.setIssuerPolicyId(policy.getIssuerAssignedPolicyId());
			po.setSubscriberStateCd(SbmDataUtil.getStateCdFromQhpId(policy.getQHPId()));
			po.setIssuerHiosId(SbmDataUtil.getIssuerIdFromQhpId(policy.getQHPId()));
			po.setIssuerSubscriberID(policy.getIssuerAssignedSubscriberId());
			po.setExchangeAssignedSubscriberID(policy.getExchangeAssignedSubscriberId());
			po.setTransControlNum(String.valueOf(policy.getRecordControlNumber()));
			po.setPolicyStartDate(DateTimeUtil.getLocalDateFromXmlGC(policy.getPolicyStartDate()));
			po.setPolicyEndDate(DateTimeUtil.getLocalDateFromXmlGC(policy.getPolicyEndDate()));
			po.setPlanID(policy.getQHPId());
			po.setX12InsrncLineTypeCd(policy.getInsuranceLineCode());
			po.setPreviousPolicyVersionId(inboundDTO.getPolicyVersionId());
		}

		po.setSbmTransMsgId(inboundDTO.getSbmTransMsgId());

		if (epsPO != null) {
			if (epsPO.equals(po)) {
				po.setPolicyChanged(false);
			}
		}

		return po;
	}



	/**
	 * @param policy
	 * @param po
	 * @return
	 */
	public PolicyType mapEpsToSbm(PolicyVersionPO po) {

		PolicyType policy = new PolicyType();

		policy.setInsuranceLineCode(po.getX12InsrncLineTypeCd());
		policy.setQHPId(po.getPlanID());

		return policy;
	}

}
