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
	 * @param sbmFileProcSumId
	 * @return
	 */
	public SbmResponseDTO generateSBMR(Long sbmFileProcSumId);
		
	
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

	/**
	 * @param sbmFileProcSumId
	 * @return
	 */
	public SbmResponseDTO generateUpdateStatusSBMR(Long sbmFileProcSumId);	
}
