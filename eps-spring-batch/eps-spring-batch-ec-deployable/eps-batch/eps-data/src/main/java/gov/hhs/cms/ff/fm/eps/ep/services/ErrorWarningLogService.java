package gov.hhs.cms.ff.fm.eps.ep.services;

import java.util.List;

import gov.hhs.cms.ff.fm.eps.ep.ErrorWarningLogDTO;
import gov.hhs.cms.ff.fm.eps.ep.po.ErrorWarningLogPO;

/**
 * @author david.heller
 *
 */
public interface ErrorWarningLogService {
	
	/**
	 * @param errWarnLogDTOList
	 */
	public void saveErrorWarningLogs(List<ErrorWarningLogDTO> errWarnLogDTOList);
	
	
	/**
	 * @param transMsgId
	 * @return
	 */
	public List<ErrorWarningLogPO> getErrorWarningLogByTransMsgId(Long transMsgId);

}
