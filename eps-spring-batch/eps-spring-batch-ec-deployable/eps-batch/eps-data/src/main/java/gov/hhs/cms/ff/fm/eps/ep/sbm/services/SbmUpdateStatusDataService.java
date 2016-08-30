package gov.hhs.cms.ff.fm.eps.ep.sbm.services;

public interface SbmUpdateStatusDataService {
	
	
	/**
	 * @param batchId
	 * @param sbmFileProcSumId
	 */
	public void executeApproval(Long batchId, Long sbmFileProcSumId);
	
	
	/**
	 * @param batchId
	 * @param sbmFileProcSumId
	 */
	public void executeDisapproval(Long batchId, Long sbmFileProcSumId);
	
	
	
	/**
	 * @param batchId
	 * @param sbmFileProcSumId
	 */
	public void executeFileReversal(Long batchId, Long sbmFileProcSumId);

}
