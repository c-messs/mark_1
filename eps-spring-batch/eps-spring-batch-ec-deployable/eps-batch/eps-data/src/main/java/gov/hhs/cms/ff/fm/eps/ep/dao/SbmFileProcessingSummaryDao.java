package gov.hhs.cms.ff.fm.eps.ep.dao;

import java.util.List;

import gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmFileProcessingSummaryPO;

public interface SbmFileProcessingSummaryDao {
	
	
	/**
	 * @param sbmFileId
	 * @param tenantId
	 * @return
	 */
	public List<SbmFileProcessingSummaryPO> performSummaryMatch(String sbmFileId, String tenantId);
		
	/**
	 * @param sbmFileProcSumId
	 * @return
	 */
	public SbmFileProcessingSummaryPO selectSbmFileProcessingSummary(final Long sbmFileProcSumId);
		
	/**
	 * @param stateCd
	 * @param issuerId
	 * @return
	 */
	public SbmFileProcessingSummaryPO selectSbmFileProcessingSummaryLatest(String stateCd, String issuerId);	
	
	/**
	 * @param issuerId
	 * @param fileSetId
	 * @param tenantId
	 * @return
	 */
	public List<SbmFileProcessingSummaryPO> findSbmFileProcessingSummary(final String issuerId, final String fileSetId, final String tenantId);
		
	/**
	 * @param fileSetId
	 * @param fileNumber
	 * @return
	 */
	public List<SbmFileProcessingSummaryPO> findSbmFileProcessingSummary(String fileSetId, final int fileNumber);
		
	/**
	 * @param fileName
	 * @return
	 */
	public List<SbmFileProcessingSummaryPO> findSbmFileProcessingSummary(String fileName);
		
	/**
	 * @param status
	 * @return
	 */
	public List<SbmFileProcessingSummaryPO> findSbmFileProcessingSummary(SBMFileStatus status);
		
	/**
	 * WHERE sfps.SBMFILESTATUSTYPECD IN ('ACC', 'ACE', 'ACW', 'IPC')
	 * @param stateCd
	 * @param status
	 * @return
	 */
	public List<SbmFileProcessingSummaryPO> findSbmFileProcessingSummaryInProcess(String stateCd);
	
	/**
	 * Insert new SbmFileProcessingSummary record.
	 * @param po
	 */
	public Long insertSbmFileProcessingSummary(final SbmFileProcessingSummaryPO po);
	
	/**
	 * Update File Status (SbmFileProcessingSummary FileSet status) by sbmFileProcSumId
	 * @param sbmFileProcSumId
	 * @param fileStatus
	 */
	public boolean updateStatus(Long sbmFileProcSumId, SBMFileStatus fileStatus);
	
	/**
	 * @param jobName
	 * @return
	 */
	public boolean verifyJobRunning(final String jobName);
	
	
	/**
	 * @param po
	 */
	public void updateSbmFileProcessingSummary(final SbmFileProcessingSummaryPO po);
	

	/**
	 * @param sbmFileProcSumId
	 * @param indicator
	 * @return
	 */
	public boolean updateCmsApproved(final Long sbmFileProcSumId, final String indicator);
	
	
	
	/**
	 * @param sbmFileProcSumId
	 * @return
	 */
	public boolean verifyCmsApprovalRequired(final Long sbmFileProcSumId);
	
	

}
