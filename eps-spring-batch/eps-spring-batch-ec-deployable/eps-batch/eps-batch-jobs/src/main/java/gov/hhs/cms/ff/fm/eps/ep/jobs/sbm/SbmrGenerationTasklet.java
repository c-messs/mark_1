/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SbmResponseCompositeDao;

/**
 * @author rajesh.talanki
 *
 */
public class SbmrGenerationTasklet implements Tasklet, StepExecutionListener {

	private static final Logger LOG = LoggerFactory.getLogger(SbmrGenerationTasklet.class);
	
	private SBMResponseGenerator sbmResponseGenerator;
	private SbmResponseCompositeDao sbmResponseDao;
	
	@Override
	public void beforeStep(StepExecution stepExecution) {
		
		Object summaryId = stepExecution.getJobExecution().getExecutionContext().get(SBMConstants.CURRENT_PROCESSING_SUMMARY_ID);
		Long batchId = stepExecution.getJobExecution().getJobId();
		
		if(summaryId instanceof Long ) {
			Long currentProcessingId = (Long) summaryId;
			if(sbmResponseDao.isRecExistsInStagingSBMPolicy(currentProcessingId)) {
				LOG.info("Not ready for SBMR; smbFileProcSummaryId:{}", currentProcessingId); 
			}
			else {
				//All XPRs for the summaryId is completed 
				if(sbmResponseDao.lockSummaryIdForSBMR(currentProcessingId, batchId)) {
					LOG.info("Lock acquired for SBMR generation; smbFileProcSummaryId:{}", currentProcessingId);
				}
				else {
					LOG.info("Couldn't acquire lock for SBMR generation; smbFileProcSummaryId:{}", currentProcessingId);
				}
			}
			return;
		}
		
		//get summaryIds ready for SBMR generation and lock one  
		List<Long> sbmFileProcSumList = sbmResponseDao.retrieveSummaryIdsReadyForSBMR();
		LOG.info("smbFileProcSummaryIds ready for SBMR:{}", sbmFileProcSumList);
		for(Long sbmFileProcSumId: sbmFileProcSumList) {
			if(sbmResponseDao.lockSummaryIdForSBMR(sbmFileProcSumId, batchId)) {
				LOG.info("Lock acquired for SBMR generation; smbFileProcSummaryId:{}", sbmFileProcSumId);
				return;
			}
			else {
				LOG.info("Couldn't acquire lock for SBMR generation; smbFileProcSummaryId:{}", sbmFileProcSumId);
			}
		}		
	}
	
	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		
		JobExecution jobExec = chunkContext.getStepContext().getStepExecution().getJobExecution();
		Long batchId = jobExec.getJobId();		
			
		List<Long> summaryIdList = sbmResponseDao.getSummaryIdsForSBMRFromStagingSBMGroupLock(batchId);
		LOG.info("smbFileProcSummaryIds for SBMR:{}", summaryIdList); 
		
		for(Long smbFileProcSummaryId : summaryIdList) {			
			LOG.info("Genrating SBMR for smbFileProcSummaryId:{}", smbFileProcSummaryId);
			sbmResponseGenerator.generateSBMRWithPolicyErrors(smbFileProcSummaryId, batchId);
			sbmResponseDao.removeLockOnSummaryIdForSBMR(smbFileProcSummaryId, batchId);
			LOG.info("SBMR generated for smbFileProcSummaryId:{}", smbFileProcSummaryId);
		}		
		
		jobExec.getExecutionContext().remove(SBMConstants.CURRENT_PROCESSING_SUMMARY_ID);
		
		return RepeatStatus.FINISHED;
	}
		
	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		// Auto-generated method stub
		return null;
	}
	
	/**
	 * @param sbmResponseGenerator the sbmResponseGenerator to set
	 */
	public void setSbmResponseGenerator(SBMResponseGenerator sbmResponseGenerator) {
		this.sbmResponseGenerator = sbmResponseGenerator;
	}

	/**
	 * @param sbmResponseDao the sbmResponseDao to set
	 */
	public void setSbmResponseDao(SbmResponseCompositeDao sbmResponseDao) {
		this.sbmResponseDao = sbmResponseDao;
	}
	
}
