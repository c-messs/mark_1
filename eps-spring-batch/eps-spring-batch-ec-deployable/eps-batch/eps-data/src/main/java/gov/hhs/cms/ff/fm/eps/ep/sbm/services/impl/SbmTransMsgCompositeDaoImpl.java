package gov.hhs.cms.ff.fm.eps.ep.sbm.services.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.hhs.cms.ff.fm.eps.ep.dao.SbmTransMsgDao;
import gov.hhs.cms.ff.fm.eps.ep.enums.SbmTransMsgStatus;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmTransMsgAdditionalErrorInfoPO;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmTransMsgPO;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmTransMsgValidationPO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMPolicyDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.dao.impl.SbmTransMsgAdditionalErrorInfoDaoImpl;
import gov.hhs.cms.ff.fm.eps.ep.sbm.dao.impl.SbmTransMsgValidationDaoImpl;
import gov.hhs.cms.ff.fm.eps.ep.sbm.mappers.SbmTransMsgAdditionalErrorInfoMapper;
import gov.hhs.cms.ff.fm.eps.ep.sbm.mappers.SbmTransMsgMapper;
import gov.hhs.cms.ff.fm.eps.ep.sbm.mappers.SbmTransMsgValidationMapper;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SbmTransMsgCompositeDao;
import gov.hhs.cms.ff.fm.eps.ep.vo.UserVO;

public class SbmTransMsgCompositeDaoImpl implements SbmTransMsgCompositeDao {

	private final static Logger LOG = LoggerFactory.getLogger(SbmTransMsgCompositeDaoImpl.class);

	private SbmTransMsgMapper sbmTransMsgMapper;
	private SbmTransMsgValidationMapper sbmTransMsgValidationMapper;
	private SbmTransMsgAdditionalErrorInfoMapper sbmTransMsgAddlErrInfoMapper;

	private SbmTransMsgDao sbmTransMsgDao;
	private SbmTransMsgValidationDaoImpl sbmTransMsgValidationDao;
	private SbmTransMsgAdditionalErrorInfoDaoImpl sbmTransMsgAddlErrInfoDao;

	private UserVO userVO;

	@Override
	public Long saveSbmTransMsg(SBMPolicyDTO policyDTO) {

		return saveSbmTransMsg(policyDTO, null);
	}

	@Override
	public Long saveSbmTransMsg(SBMPolicyDTO policyDTO, SbmTransMsgStatus status) {
		
		if (policyDTO.getBatchId() != null) {
			userVO.setUserId(policyDTO.getBatchId().toString());
		} 

		SbmTransMsgPO po = sbmTransMsgMapper.mapSBMToEPS(policyDTO);
		
		if(SbmTransMsgStatus.SKIP.equals(status)) {
			po.setSbmTransMsgProcStatusTypeCd(SbmTransMsgStatus.SKIP.getCode());
		}
		
		LOG.info("Saving SbmTransMsg: " + policyDTO.getLogMsg() + ", statusCd=" + po.getSbmTransMsgProcStatusTypeCd());

		Long sbmTransMsgId = sbmTransMsgDao.insertSbmTransMsg(policyDTO.getBatchId(), policyDTO.getStagingSbmPolicyid(), po);
		policyDTO.setSbmTransMsgId(sbmTransMsgId);

		saveSbmErrorsAndWarnings(policyDTO);

		return sbmTransMsgId;
	}
	


	private void saveSbmErrorsAndWarnings(SBMPolicyDTO inboundPolicyDTO) {

		List<SbmTransMsgValidationPO> errPoList = sbmTransMsgValidationMapper.mapSbmToEps(inboundPolicyDTO);

		if (!errPoList.isEmpty()) {
			LOG.info("Saving Validation Errors: "  + inboundPolicyDTO.getLogMsg());
			sbmTransMsgValidationDao.insertSbmTransMsgValidationList(errPoList);
		}

		List<SbmTransMsgAdditionalErrorInfoPO> errAddlInfoPoList = sbmTransMsgAddlErrInfoMapper.mapSbmToEps(inboundPolicyDTO);

		if (!errAddlInfoPoList.isEmpty()) {
			LOG.info("Saving Validation AdditionalInfo Errors: "  + inboundPolicyDTO.getLogMsg());
			sbmTransMsgAddlErrInfoDao.insertSbmTransMsgAddlErrInfoList(errAddlInfoPoList);
		}
	}


	/**
	 * @param sbmTransMsgMapper the sbmTransMsgMapper to set
	 */
	public void setSbmTransMsgMapper(SbmTransMsgMapper sbmTransMsgMapper) {
		this.sbmTransMsgMapper = sbmTransMsgMapper;
	}


	/**
	 * @param sbmTransMsgValidationMapper the sbmTransMsgValidationMapper to set
	 */
	public void setSbmTransMsgValidationMapper(SbmTransMsgValidationMapper sbmTransMsgValidationMapper) {
		this.sbmTransMsgValidationMapper = sbmTransMsgValidationMapper;
	}


	/**
	 * @param sbmTransMsgAddlErrInfoMapper the sbmTransMsgAddlErrInfoMapper to set
	 */
	public void setSbmTransMsgAddlErrInfoMapper(SbmTransMsgAdditionalErrorInfoMapper sbmTransMsgAddlErrInfoMapper) {
		this.sbmTransMsgAddlErrInfoMapper = sbmTransMsgAddlErrInfoMapper;
	}


	/**
	 * @param sbmTransMsgDao the sbmTransMsgDao to set
	 */
	public void setSbmTransMsgDao(SbmTransMsgDao sbmTransMsgDao) {
		this.sbmTransMsgDao = sbmTransMsgDao;
	}


	/**
	 * @param sbmTransMsgValidationDao the sbmTransMsgValidationDao to set
	 */
	public void setSbmTransMsgValidationDao(SbmTransMsgValidationDaoImpl sbmTransMsgValidationDao) {
		this.sbmTransMsgValidationDao = sbmTransMsgValidationDao;
	}


	/**
	 * @param sbmTransMsgAddlErrInfoDao the sbmTransMsgAddlErrInfoDao to set
	 */
	public void setSbmTransMsgAddlErrInfoDao(SbmTransMsgAdditionalErrorInfoDaoImpl sbmTransMsgAddlErrInfoDao) {
		this.sbmTransMsgAddlErrInfoDao = sbmTransMsgAddlErrInfoDao;
	}


	/**
	 * @param userVO the userVO to set
	 */
	public void setUserVO(UserVO userVO) {
		this.userVO = userVO;
	}
	
}
