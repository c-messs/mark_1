package gov.hhs.cms.ff.fm.eps.ep.sbm.services;

import java.util.List;
import java.util.Map;

import gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMErrorDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileProccessingSummary;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileProcessingDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMSummaryAndFileInfoDTO;

public interface SBMFileCompositeDAO {
	
	
	/**
	 * Save SBMFileInfo, SBMFileError and SBMFileErrorAdditionalInfo then returns SBMFileInfoId
	 * @param inboundFileDTO
	 * @return return SBMFileInfoId
	 */
	public Long saveFileInfoAndErrors(SBMFileProcessingDTO inboundFileDTO);
	
	/**
	 * Save File to StagingSBMFile
	 * @param inboundFileDTO
	 */
	public void saveFileToStagingSBMFile(SBMFileProcessingDTO inboundFileDTO);
	
	
	public void extractXprToStagingPolicy(SBMFileProcessingDTO inboundFileDTO);
	
	/**
	 * Inserts a record into StagingSbmGroupLock for Extract process for the given sbmFileProcSumId
	 * @param sbmFileProcSumId
	 */
	public void insertStagingSbmGroupLockForExtract(Long sbmFileProcSumId);
	
	/**
	 * Check if given filename already exists in SBMFileInfo table
	 * @param filename
	 * @return
	 */
	public List<SBMSummaryAndFileInfoDTO> getFileStatus(String filename);
	
		
	/**
	 * Return SBMFileInfo list that matches with given fileId and tenantId	
	 * @param fileId
	 * @param tenantId
	 * @return
	 */
	public List<SBMSummaryAndFileInfoDTO> performSbmFileMatch(String sbmFileId, String tenantId);
	
		
	/**
	 * @param fileSetId
	 * @param fileNumber
	 * @return
	 */
	public List<SBMSummaryAndFileInfoDTO> findSbmFileInfo(String fileSetId, int fileNumber);
	
	/**
	 * Get SBMFileProcessingSummary for the given fileSetId
	 * @param fileSetId
	 * @return
	 */
	public List<SBMSummaryAndFileInfoDTO> getSBMFileProcessingSummary(String issuerId, String fileSetId, String tenantId);
	
	/**
	 * 
	 * @param fileStatus
	 * @return
	 */
	public List<SBMSummaryAndFileInfoDTO> getAllSBMFileProcessingSummary(SBMFileStatus fileStatus);
	
	/**
	 * Returns list of SBMSummaryAndFileInfoDTO that are in InProcess or pending approval (ACCEPTED, ACCEPTED WITH WARNINGS or ACCEPTED WITH ERRORS)
	 * for the given stateCode.
	 * @param stateCode
	 * @return
	 */
	public List<SBMSummaryAndFileInfoDTO> getAllInProcessOrPendingApprovalForState(String stateCode);
	
	/**
	 * Returns latest file processing summary for the give issuerId. Returns null if nothing exist
	 * @param issuerId
	 * @return
	 */
	public SBMSummaryAndFileInfoDTO getLatestSBMFileProcessingSummaryByIssuer(String issuerId);
	
	/**
	 * Returns latest file processing summary for state. Returns null if nothing exist
	 * @param stateCode
	 * @return
	 */
	public SBMSummaryAndFileInfoDTO getLatestSBMFileProcessingSummaryByState(String stateCode);
	
	
	/**
	 * Save SBMFileProcessingSummary and return sbmFileProcSumId
	 */
	public Long saveSbmFileProcessingSummary(SBMFileProccessingSummary sbmFileProccessingSummary);
	
	/**
	 * Returns all SBMFileInfo for the given sbmFileProcSumId 
	 * @param sbmFileProcSumId
	 * @return
	 */
	public List<SBMSummaryAndFileInfoDTO> getAllSBMFileInfos(Long sbmFileProcSumId);
	
	/**
	 * Update file status for the given sbmFileInfoIdList
	 * @param sbmFileProcSumId
	 * @param fileStatus
	 */
	public void updateFileStatus(Long sbmFileProcSumId, SBMFileStatus fileStatus);
	
	/**
	 * Update CMSApprovedInd
	 * @param sbmFileProcSumId
	 * @param cmsApprovedInd
	 */
	public void updateCMSApprovedInd(Long sbmFileProcSumId, String cmsApprovedInd);
	
	/**
	 * Save SBMFileErrors
	 * @param errorList
	 */
	public void saveSBMFileErrors(List<SBMErrorDTO> errorList);
	
	/**
	 * Returns Map of errorCd and description for all error codes 
	 * @return Map of errorCd and description
	 */
	public Map<String, String> getAllErrorCodesAndDescription();
	
	public String getFileInfoTypeXml(Long sbmFileInfoId);
	
	/**
	 * Returns true if SBMI Job is running, otherwise returns false
	 * @return
	 */
	public boolean isSBMIJobRunning();	
	
}
