package gov.hhs.cms.ff.fm.eps.ep.sbm.mappers;

import gov.cms.dsh.sbmi.FileInformationType;
import gov.cms.dsh.sbmi.PolicyType;
import gov.cms.dsh.sbmr.PolicyErrorType;
import gov.hhs.cms.ff.fm.eps.ep.enums.SbmTransMsgStatus;
import gov.hhs.cms.ff.fm.eps.ep.enums.TxnMessageDirectionType;
import gov.hhs.cms.ff.fm.eps.ep.enums.TxnMessageType;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmTransMsgPO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMPolicyDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SbmDataUtil;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;

/**
 * @author j.radziewski
 *
 */
public class SbmTransMsgMapper {

	/**
	 * 
	 * @param inboundPolicyDTO
	 * @return SbmTransMsgPO
	 */
	public SbmTransMsgPO mapSBMToEPS(SBMPolicyDTO inboundPolicyDTO) {

		SbmTransMsgPO po = new SbmTransMsgPO();

		FileInformationType fileInfoType = inboundPolicyDTO.getFileInfo();

		if (fileInfoType != null) {

			po.setTransMsgDateTime(DateTimeUtil.getLocalDateTimeFromXmlGC(fileInfoType.getFileCreateDateTime()));
			po.setSubscriberStateCd(SbmDataUtil.getStateCd(fileInfoType.getTenantId()));
		}
		po.setMsg(inboundPolicyDTO.getPolicyXml());
		po.setTransMsgDirectionTypeCd(TxnMessageDirectionType.INBOUND.getValue());
		po.setTransMsgTypeCd(TxnMessageType.MSG_SBMI.getValue());
		po.setSbmFileInfoId(inboundPolicyDTO.getSbmFileInfoId());

		PolicyType policy = inboundPolicyDTO.getPolicy();

		if (policy != null) {

			po.setRecordControlNum(policy.getRecordControlNumber());
			po.setPlanId(policy.getQHPId());
			po.setExchangeAssignedPolicyId(policy.getExchangeAssignedPolicyId());
			po.setExchangeAssignedSubscriberId(policy.getExchangeAssignedSubscriberId());
		}

		SbmTransMsgStatus status = determineSbmTransMsgStatus(inboundPolicyDTO);
		po.setSbmTransMsgProcStatusTypeCd(status.getCode());
		return po;
	}

	/*
	 * determine SbmTransMsg Status
	 */
	private SbmTransMsgStatus determineSbmTransMsgStatus(SBMPolicyDTO inboundPolicyDTO) {

		SbmTransMsgStatus status = null;

		if (inboundPolicyDTO.isErrorFlag() || !inboundPolicyDTO.getSchemaErrorList().isEmpty()) {
			status = SbmTransMsgStatus.REJECTED;
		} else {
			// At this point the ErrorList can only contain warnings.
			// If there are warnings, then that might mean EPS changed some data (like CSR, langCd, etc)
			// Determine if those warnings are due to an EPS change or some other warning.
			if (inboundPolicyDTO.getErrorList().isEmpty()) {
				status = SbmTransMsgStatus.ACCEPTED_WITH_SBM_CHANGE;
			} else if (SbmDataUtil.hasWarningFromEPSChange(inboundPolicyDTO)) {
				status = SbmTransMsgStatus.ACCEPTED_WITH_EPS_CHANGE;
			} else {
				status = SbmTransMsgStatus.ACCEPTED_WITH_SBM_CHANGE;
			}
		}
		return status;
	}
	
	


	/**
	 * 
	 * @param po
	 * @return policyError
	 */
	public PolicyErrorType mapEpsToSbmr(SbmTransMsgPO po) {

		PolicyErrorType policyError = new PolicyErrorType();

		policyError.setRecordControlNumber(po.getRecordControlNum());
		policyError.setQHPId(po.getPlanId());
		policyError.setExchangeAssignedPolicyId(po.getExchangeAssignedPolicyId());
		policyError.setExchangeAssignedSubscriberId(po.getExchangeAssignedSubscriberId());

		return policyError;
	}

}
