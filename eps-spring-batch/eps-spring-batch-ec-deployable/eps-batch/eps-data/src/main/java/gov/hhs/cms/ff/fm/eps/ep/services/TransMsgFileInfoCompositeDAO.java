package gov.hhs.cms.ff.fm.eps.ep.services;

import gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentRequestDTO;

/**
 * @author girmay.weldemichael
 *
 */
public interface TransMsgFileInfoCompositeDAO {
	
	/**
	 * @param berDTO
	 * @return
	 */
	public Long saveFileInfo(BenefitEnrollmentRequestDTO berDTO);
	
	

	
	/**
	 * calls TransMsgDao.extractTransMsg.  Extracts BEM elements from XMLType in StageBer table
	 * and inserts TransMsgs one for one from BER XML data.  BerDTO contains arguments other
	 * than MSG XML. UserId is pulled directly from BatchId and not userVO.
	 * @param berDTO
	 */
	public void saveTransMsg(BenefitEnrollmentRequestDTO berDTO);
	
	
	
	
}
