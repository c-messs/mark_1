package gov.hhs.cms.ff.fm.eps.ep.sbm.mappers;

import java.util.ArrayList;
import java.util.List;

import gov.hhs.cms.ff.fm.eps.ep.po.SbmFileErrorAdditionalInfoPO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMErrorDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileProcessingDTO;

public class SbmFileErrorAdditionalInfoMapper {


	public List<SbmFileErrorAdditionalInfoPO> mapSbmToEps(SBMFileProcessingDTO inboundDTO) {

		List<SbmFileErrorAdditionalInfoPO> poList = new ArrayList<SbmFileErrorAdditionalInfoPO>();

		Long seqNum = Long.valueOf(1);

		for (SBMErrorDTO fileErrDTO : inboundDTO.getErrorList()) {

			for (String addlErrInfo : fileErrDTO.getAdditionalErrorInfoList()) {

				SbmFileErrorAdditionalInfoPO po = new SbmFileErrorAdditionalInfoPO();
				po.setSbmFileInfoId(inboundDTO.getSbmFileInfo().getSbmFileInfoId());
				po.setAdditionalErrorInfoText(addlErrInfo);
				po.setSbmFileErrorSeqNum(seqNum);
				poList.add(po);	
			}
			seqNum++;
		}
		return poList;
	}

}
