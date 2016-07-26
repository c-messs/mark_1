package gov.hhs.cms.ff.fm.eps.ep.dao;

import java.util.List;

import gov.hhs.cms.ff.fm.eps.ep.po.ErrorWarningLogPO;

/**
 * @author eps
 *
 */
public interface ErrorWarningLogDao {
	
	
	/**
	 * @param TransMsgId
	 * @return
	 */
	public List<ErrorWarningLogPO> getErrorWarningLogListByTransMsgId(Long TransMsgId);
	
	/**
	 * @param errorWarningLogList
	 */
	public void insertErrorWarningLogs(List<ErrorWarningLogPO> errorWarningLogList);

}
