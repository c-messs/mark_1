package gov.hhs.cms.ff.fm.eps.ep.services.impl;


import gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentRequestDTO;
import gov.hhs.cms.ff.fm.eps.ep.dao.TransMsgDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.TransMsgFileInfoDao;
import gov.hhs.cms.ff.fm.eps.ep.mappers.TransMsgFileInfoMapper;
import gov.hhs.cms.ff.fm.eps.ep.po.TransMsgFileInfoPO;
import gov.hhs.cms.ff.fm.eps.ep.services.TransMsgFileInfoCompositeDAO;
import gov.hhs.cms.ff.fm.eps.ep.vo.UserVO;

/**
 * @author eps
 *
 */
public class TransMsgFileInfoCompositeDAOImpl implements TransMsgFileInfoCompositeDAO {

	private TransMsgFileInfoMapper transMsgFileInfoMapper;
	private TransMsgFileInfoDao transMsgFileInfoDao;
	private TransMsgDao transMsgDao;
	private UserVO userVO;

	/** 
	 * Save and Return file info id
	 * @param berDTO
	 * @return transMsgFileInfoId
	 */
	public Long saveFileInfo(BenefitEnrollmentRequestDTO berDTO) {

		Long transMsgFileInfoId = null;

		if (berDTO.getBatchId() != null) {

			userVO.setUserId(berDTO.getBatchId().toString());
		} 

		TransMsgFileInfoPO fileInfoPO = transMsgFileInfoMapper.mapDTOToVO(berDTO);

		transMsgFileInfoId = transMsgFileInfoDao.insertTransMsgFileInfo(fileInfoPO, berDTO.getBerXml());
		
		return transMsgFileInfoId;
	}
	

	@Override
	public void saveTransMsg(BenefitEnrollmentRequestDTO berDTO) {

		transMsgDao.extractTransMsg(berDTO);
	}
	

	/**
	 * @param transMsgFileInfoMapper
	 */
	public void setTransMsgFileInfoMapper(TransMsgFileInfoMapper transMsgFileInfoMapper) {
		
		this.transMsgFileInfoMapper = transMsgFileInfoMapper;
	}

	/**
	 * @param transMsgFileInfoDao
	 */
	public void setTransMsgFileInfoDao(TransMsgFileInfoDao transMsgFileInfoDao) {
		
		this.transMsgFileInfoDao = transMsgFileInfoDao;
	}


	public void setUserVO(UserVO userVO) {
		
		this.userVO = userVO;
	}


	/**
	 * @param transMsgDao the transMsgDao to set
	 */
	public void setTransMsgDao(TransMsgDao transMsgDao) {
		
		this.transMsgDao = transMsgDao;
	}

}
