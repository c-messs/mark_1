package gov.hhs.cms.ff.fm.eps.ep.sbm.mappers;

import java.util.ArrayList;
import java.util.List;

import gov.hhs.cms.ff.fm.eps.ep.po.SbmFileErrorPO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMErrorDTO;

/**
 * @author j.radziewski
 *
 */
public class SbmFileErrorMapper {

	/**
	 * 
	 * @param sbmFileInfoId
	 * @param inboundErrorList
	 * @return poList
	 */
	public List<SbmFileErrorPO> mapSbmToEps(Long sbmFileInfoId, List<SBMErrorDTO> inboundErrorList) {

		List<SbmFileErrorPO> poList = new ArrayList<SbmFileErrorPO>();

		Long seqNum = Long.valueOf(1);

		for (SBMErrorDTO fileErr : inboundErrorList) {

			SbmFileErrorPO po = new SbmFileErrorPO();
			po.setSbmFileInfoId(sbmFileInfoId);
			po.setSbmErrorWarningTypeCd(fileErr.getSbmErrorWarningTypeCd());
			po.setElementInErrorNm(fileErr.getElementInErrorNm());
			po.setSbmFileErrorSeqNum(seqNum++);
			poList.add(po);	
		}
		return poList;
	}

	/**
	 * 
	 * @param inboundErrorList
	 * @return poList
	 */
	public List<SbmFileErrorPO> mapSbmToEps(List<SBMErrorDTO> inboundErrorList) {

		List<SbmFileErrorPO> poList = new ArrayList<SbmFileErrorPO>();

		Long seqNum = Long.valueOf(1);

		for (SBMErrorDTO fileErr : inboundErrorList) {

			SbmFileErrorPO po = new SbmFileErrorPO();
			po.setSbmFileInfoId(fileErr.getSbmFileInfoId());
			po.setSbmErrorWarningTypeCd(fileErr.getSbmErrorWarningTypeCd());
			po.setElementInErrorNm(fileErr.getElementInErrorNm());
			po.setSbmFileErrorSeqNum(seqNum++);
			poList.add(po);	
		}
		return poList;
	}

}
