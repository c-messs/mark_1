package gov.hhs.cms.ff.fm.eps.ep.sbm.mappers;

import java.util.ArrayList;
import java.util.List;

import gov.cms.dsh.sbmr.PolicyErrorType;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmTransMsgValidationPO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMCache;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMErrorDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMPolicyDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SbmErrWarningLogDTO;

public class SbmTransMsgValidationMapper {

	public List<SbmTransMsgValidationPO> mapSbmToEps(SBMPolicyDTO inboundPolicyDTO) {

		List<SbmTransMsgValidationPO> poList = new ArrayList<SbmTransMsgValidationPO>();

		Long seqNum = Long.valueOf(1);
		
		for (SBMErrorDTO err : inboundPolicyDTO.getSchemaErrorList()) {
			
			SbmTransMsgValidationPO po = new SbmTransMsgValidationPO();
			po.setSbmTransMsgId(inboundPolicyDTO.getSbmTransMsgId());
			po.setSbmErrorWarningTypeCd(err.getSbmErrorWarningTypeCd());
			po.setElementInErrorNm(err.getElementInErrorNm());
			po.setExchangeAssignedMemberId(err.getExchangeAssignedMemberId());
			po.setValidationSeqNum(seqNum++);
			poList.add(po);
		}
		
		for (SbmErrWarningLogDTO err : inboundPolicyDTO.getErrorList()) {
		
			SbmTransMsgValidationPO po = new SbmTransMsgValidationPO();
			po.setSbmTransMsgId(inboundPolicyDTO.getSbmTransMsgId());
			po.setSbmErrorWarningTypeCd(err.getErrorWarningTypeCd());
			po.setElementInErrorNm(err.getElementInError());
			po.setExchangeAssignedMemberId(err.getExchangeMemberId());
			po.setValidationSeqNum(seqNum++);
			poList.add(po);

		}

		return poList;	   
	}
	
	public PolicyErrorType.Error mapEpsToSbmr(SbmTransMsgValidationPO po) {
		
		PolicyErrorType.Error err = new PolicyErrorType.Error();
		
		err.setElementInError(po.getElementInErrorNm());
		err.setErrorCode(po.getSbmErrorWarningTypeCd());
		err.setExchangeAssignedMemberId(po.getExchangeAssignedMemberId());
		
		err.setErrorDescription(SBMCache.getErrorDescription(po.getSbmErrorWarningTypeCd()));
		
		return err;
		
	}

}
