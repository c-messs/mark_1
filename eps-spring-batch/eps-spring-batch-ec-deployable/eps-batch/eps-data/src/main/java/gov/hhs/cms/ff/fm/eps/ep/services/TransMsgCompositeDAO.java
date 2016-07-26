/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.services;

import gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentMaintenanceDTO;
import gov.hhs.cms.ff.fm.eps.ep.enums.EProdEnum;
import gov.hhs.cms.ff.fm.eps.ep.enums.ProcessedToDbInd;
import gov.hhs.cms.ff.fm.eps.ep.po.BatchTransMsgPO;

/**
 * @author 
 *
 */
public interface TransMsgCompositeDAO {
	
	
	
	/**
	 * Updates the processing status of a transaction (bemDTO).
	 * @param bemDTO
	 * @param ind
	 */
	public void updateBatchTransMsg(BenefitEnrollmentMaintenanceDTO bemDTO, ProcessedToDbInd ind);

	
	/**
	 *  Updates the processing status of the transaction (bemDTO).
	 * @param bemDTO
	 * @param ind
	 * @param skipReasonCd
	 * @param skipReasonDesc
	 */
	public void updateBatchTransMsg(BenefitEnrollmentMaintenanceDTO bemDTO, ProcessedToDbInd ind, String skipReasonCd, String skipReasonDesc);
	
	
	/**
	 * @param bemDTO
	 * @param ind
	 * @param eProd
	 */
	public void updateBatchTransMsg(BenefitEnrollmentMaintenanceDTO bemDTO, ProcessedToDbInd ind, EProdEnum eProd);
	
	
	/**
	 * Updates the processing status of a transaction.  Use when the status of a previous transaction
	 * needs to be updated.
	 *  
	 * @param batchId
	 * @param transMsgId
	 * @param ind
	 */
	public void updateProcessedToDbIndicator(Long batchId, Long transMsgId, ProcessedToDbInd ind);
	
	
	/**
	 * @param batchId
	 * @param transMsgId
	 * @return
	 */
	public BatchTransMsgPO getBatchTransMsg(Long batchId, Long transMsgId);
	
	
	/**
	 * @param bemDTO
	 * @return
	 */
	public Integer getSkippedTransMsgCount(BenefitEnrollmentMaintenanceDTO bemDTO);
	
	
	/**
	 * @param bemDTO
	 * @param ind
	 * @return
	 */
	public Integer updateSkippedVersion(BenefitEnrollmentMaintenanceDTO bemDTO, ProcessedToDbInd ind);
	
	/**
	 * 
	 * @param bemDTO
	 * @param ind
	 * @return
	 */
	public Integer updateLaterVersions(BenefitEnrollmentMaintenanceDTO bemDTO, ProcessedToDbInd ind);
	
	/**
	 * @param bemDTO
	 * @return
	 */
	public Integer getSkippedVersionCount(BenefitEnrollmentMaintenanceDTO bemDTO);
	
} 
