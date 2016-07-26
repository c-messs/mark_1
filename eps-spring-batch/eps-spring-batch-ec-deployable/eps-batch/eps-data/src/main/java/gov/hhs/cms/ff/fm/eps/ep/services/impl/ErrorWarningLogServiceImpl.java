package gov.hhs.cms.ff.fm.eps.ep.services.impl;

import gov.hhs.cms.ff.fm.eps.ep.ErrorWarningLogDTO;
import gov.hhs.cms.ff.fm.eps.ep.dao.ErrorWarningLogDao;
import gov.hhs.cms.ff.fm.eps.ep.mappers.ErrorWarningLogMapper;
import gov.hhs.cms.ff.fm.eps.ep.po.ErrorWarningLogPO;
import gov.hhs.cms.ff.fm.eps.ep.services.ErrorWarningLogService;
import gov.hhs.cms.ff.fm.eps.ep.vo.UserVO;

import java.util.ArrayList;
import java.util.List;

/**
 * @author eps
 *
 */
public class ErrorWarningLogServiceImpl implements ErrorWarningLogService {

	private ErrorWarningLogMapper errorWarningLogMapper;

	private ErrorWarningLogDao errorWarningLogDao;

	private UserVO userVO;

	/**
	 * Save the Error Warning Logs
	 * @param errorWarningLogList
	 *  
	 */
	public void saveErrorWarningLogs(List<ErrorWarningLogDTO> errorWarningLogList) {

		List<ErrorWarningLogPO> errorWarningLogPOs = new ArrayList<ErrorWarningLogPO>();
		for (ErrorWarningLogDTO errWarnLogDTO : errorWarningLogList) {
			if (errWarnLogDTO.getBatchId() != null) {
				userVO.setUserId(errWarnLogDTO.getBatchId().toString());
			}
			ErrorWarningLogPO errWarnLogPO = errorWarningLogMapper.mapDTOToPO(errWarnLogDTO);
			errorWarningLogPOs.add(errWarnLogPO);
		}
		errorWarningLogDao.insertErrorWarningLogs(errorWarningLogPOs);
	}

	/** 
	 * Get Errors by TransMsgId
	 * @param transMsgId
	 * @return errors
	 */
	public List<ErrorWarningLogPO> getErrorWarningLogByTransMsgId(Long transMsgId)  {

		return  errorWarningLogDao.getErrorWarningLogListByTransMsgId(transMsgId);
	}

	/**
	 * @param errorWarningLogMapper
	 */
	public void setErrorWarningLogMapper(ErrorWarningLogMapper errorWarningLogMapper) {
		this.errorWarningLogMapper = errorWarningLogMapper;
	}

	/**
	 * @param errorWarningLogDao
	 */
	public void setErrorWarningLogDao(ErrorWarningLogDao errorWarningLogDao) {
		this.errorWarningLogDao = errorWarningLogDao;
	}


	public void setUserVO(UserVO userVO) {
		this.userVO = userVO;
	}

}
