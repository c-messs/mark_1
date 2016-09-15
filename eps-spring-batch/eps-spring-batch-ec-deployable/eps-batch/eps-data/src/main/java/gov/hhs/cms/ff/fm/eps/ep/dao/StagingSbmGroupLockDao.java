package gov.hhs.cms.ff.fm.eps.ep.dao;

import java.util.List;

import gov.hhs.cms.ff.fm.eps.ep.po.StagingSbmGroupLockPO;

/**
 * @author j.radziewski
 *
 */
public interface StagingSbmGroupLockDao {
	
	
	/**
	 * @return
	 */
	public List<StagingSbmGroupLockPO> selectSbmFileProcessingSummaryIdList();
	
	
	/**
	 * @param poList
	 */
	public void insertStagingSbmGroupLock(List<StagingSbmGroupLockPO> poList);
	
	
	/**
	 * @param batchId
	 * @return
	 */
	public List<StagingSbmGroupLockPO> selectStagingGroupLockZero(Long batchId);
	
	
	/**
	 * @return
	 */
	public List<StagingSbmGroupLockPO> selectStagingGroupLockSbmr();
	
	
	/**
	 * @param sbmFileProcSumId
	 * @param batchId
	 * @return
	 */
	public boolean updateStagingGroupLock(Long sbmFileProcSumId, Long batchId);
	
	
	/**
	 * @param sbmFileProcSumId
	 * @param batchId
	 * @return
	 */
	public int deleteStagingGroupLock(Long sbmFileProcSumId, Long batchId);

	
	/**
	 * @param batchId
	 * @return
	 */
	public boolean updateStagingGroupLockForExtract(Long batchId);

	
	/**
	 * @param batchId
	 * @return
	 */
	public int deleteStagingGroupLockForExtract(Long batchId);
	

}
