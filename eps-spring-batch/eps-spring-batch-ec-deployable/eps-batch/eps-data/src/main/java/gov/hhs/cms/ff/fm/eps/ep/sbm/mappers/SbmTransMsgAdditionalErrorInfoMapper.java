package gov.hhs.cms.ff.fm.eps.ep.sbm.mappers;

import java.util.ArrayList;
import java.util.List;

import gov.hhs.cms.ff.fm.eps.ep.po.SbmTransMsgAdditionalErrorInfoPO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMErrorDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMPolicyDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SbmErrWarningLogDTO;

public class SbmTransMsgAdditionalErrorInfoMapper {


	public List<SbmTransMsgAdditionalErrorInfoPO> mapSbmToEps(SBMPolicyDTO inboundPolicyDTO) {

		List<SbmTransMsgAdditionalErrorInfoPO> poList = new ArrayList<SbmTransMsgAdditionalErrorInfoPO>();

		Long seqNum = Long.valueOf(1);


		for (SBMErrorDTO err : inboundPolicyDTO.getSchemaErrorList()) {

			for (String addlErrInfo : err.getAdditionalErrorInfoList()) {

				SbmTransMsgAdditionalErrorInfoPO po = new SbmTransMsgAdditionalErrorInfoPO();
				po.setSbmTransMsgId(inboundPolicyDTO.getSbmTransMsgId());
				po.setAdditionalErrorInfoText(addlErrInfo);
				po.setValidationSeqNum(seqNum);
				poList.add(po);	
			}
			seqNum++;
		}

		for (SbmErrWarningLogDTO err : inboundPolicyDTO.getErrorList()) {

			for (String addlErrInfo : err.getErrorWarningDesc()) {

				SbmTransMsgAdditionalErrorInfoPO po = new SbmTransMsgAdditionalErrorInfoPO();
				po.setSbmTransMsgId(inboundPolicyDTO.getSbmTransMsgId());
				po.setAdditionalErrorInfoText(addlErrInfo);
				po.setValidationSeqNum(seqNum);
				poList.add(po);	
			}
			seqNum++;
		}

		return poList;
	}
	
	public List<String> mapEpsToSbmr(List<SbmTransMsgAdditionalErrorInfoPO> poList) {
		
		List<String> errList = new ArrayList<String>();
		
		for (SbmTransMsgAdditionalErrorInfoPO po : poList) {
			
			errList.add(po.getAdditionalErrorInfoText());
		}
		
		return errList;
	}

}
