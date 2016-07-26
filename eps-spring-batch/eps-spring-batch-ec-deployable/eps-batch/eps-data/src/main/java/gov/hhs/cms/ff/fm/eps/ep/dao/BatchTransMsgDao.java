package gov.hhs.cms.ff.fm.eps.ep.dao;

import gov.hhs.cms.ff.fm.eps.ep.po.BatchTransMsgPO;

/**
 * @author eps
 *
 */
public interface BatchTransMsgDao {
	
	/**
	 * @param batchTransMsgPO
	 */
	public void insertBatchTransMsg(BatchTransMsgPO batchTransMsgPO);
	
	
	/**
	 * @param batchId
	 * @param transMsgId
	 * @return
	 */
	public BatchTransMsgPO getBatchTransMsgByPk(Long batchId, Long transMsgId);
	
	
	/**
	 * 
	 * @param po
	 * @return
	 */
	public Boolean updateBatchTransMsg(BatchTransMsgPO po);
	
	
	/**
	 * @param po
	 * @return
	 */
	public Integer getSkipCount(BatchTransMsgPO po);
	
	
	/**
	 * @param po
	 * @return
	 */
	public Integer updateSkippedVersion(BatchTransMsgPO po);
	
	/**
	 * 
	 * @param po
	 * @return
	 */
	public Integer updateLaterVersions(BatchTransMsgPO po);
	
	/**
	 * @param po
	 * @return
	 */
	public Integer getSkippedVersionCount(BatchTransMsgPO po);
		
}
