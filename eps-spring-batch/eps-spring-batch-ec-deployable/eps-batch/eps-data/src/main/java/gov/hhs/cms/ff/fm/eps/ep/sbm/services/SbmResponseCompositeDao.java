package gov.hhs.cms.ff.fm.eps.ep.sbm.services;

import java.util.List;

import gov.hhs.cms.ff.fm.eps.ep.enums.SBMResponsePhaseTypeCode;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SbmResponseDTO;

/**
 * @author j.radziewski
 *
 */
public interface SbmResponseCompositeDao {
	
	/**
	 * @param batchId
	 * @param sbmFileProcSumId
	 * @return
	 */
	public SbmResponseDTO generateSBMR(Long batchId, Long sbmFileProcSumId);
		
	
	/**
	 * @param sbmFileProcSumId
	 * @param sbmFileInfoId
	 * @param physicalDocumentId
	 * @param responseCd
	 */
	public void createSBMResponseRecord(Long  sbmFileProcSumId, Long sbmFileInfoId, Long physicalDocumentId, SBMResponsePhaseTypeCode responseCd);
	
	/**
	 * @param sbmFileProcSumId
	 * @return
	 */
	public boolean isRecExistsInStagingSBMPolicy(Long sbmFileProcSumId);
	
	/**
	 * @param sbmFileProcSumId
	 * @param batchId
	 * @return
	 */
	public boolean lockSummaryIdForSBMR(Long sbmFileProcSumId, Long batchId);
	
	/**
	 * @param sbmFileProcSumId
	 * @param batchId
	 */
	public void removeLockOnSummaryIdForSBMR(Long sbmFileProcSumId, Long batchId);
	
	/**
	 * Returns list of sbmFileProcSumIds ready for SBMR generation
	 * @return
	 */
	public List<Long> retrieveSummaryIdsReadyForSBMR();
	
	/**
	 * Returns list of sbmFileProcSumIds from StagingSBMGroupLock with groupId = 0 for the given batchId
	 * @param batchId
	 * @return
	 */
	public List<Long> getSummaryIdsForSBMRFromStagingSBMGroupLock(Long batchId);

	
	/** H1. Identify if there are additional policies found in EPS that belongs to the 
	 *  issuer or state that were not provided and not in Superseded or SBMIVoid status.
	 *  Insert any found missing policies.
	 *  
	 *  Note: Missing members are found in business validations (WR-010) and are inserted
	 *  into SBMTRANSMSGVALIDATION when saving policies and warnings for each policy. 
	 * @param batchId
	 * @param sbmFileProcSumId
	 * @return
	 */
	public void validateMissingPolicies(Long batchId, Long sbmFileProcSumId);
	
	
	/**
	 * Retrieve the summary for perform Threshold Validation.
	 *  - Includes whether or not if any errors exist and file info(s).
	 * @param sbmFileProcSumId
	 * @return
	 */
	public SbmResponseDTO getSummary(Long sbmFileProcSumId);
}
