package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm;

import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.JOBPARAMETER_PROCESSINGTYPE_SBMI;
import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.JOBPARAMETER_PROCESSTYPE;

import org.apache.commons.lang.StringUtils;
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

import gov.hhs.cms.ff.fm.eps.ep.dao.StagingSbmGroupLockDao;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileProcessingDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SBMFileCompositeDAO;

/**
 * Cutsom JDBC Tasklet to execute the sql
 * 
 * @author tomi vanek
 *
 */
public class XprExtractionTasklet implements Tasklet, StepExecutionListener {
	private static final Logger LOG = LoggerFactory.getLogger(XprExtractionTasklet.class);
	
    private JobExecution jobExecution;
    private SBMFileCompositeDAO sbmFileCompositeDAO;
    private StagingSbmGroupLockDao stagingSbmGroupLockDao;
    private Integer xprProcessingGroupSize;
    private Long jobId;
	
	/** 
	 * @param contribution
	 * @param chunkContext
	 * @return RepeatStatus.FINISHED
	 * @throws Exception
	 */
    @Override
	public RepeatStatus execute(StepContribution contribution,
			ChunkContext chunkContext) throws Exception {
		
		String processingType = jobExecution.getJobParameters().getString(JOBPARAMETER_PROCESSTYPE);
		
		LOG.info("processingType: " + processingType);
		
		if(StringUtils.isBlank(processingType) 
				|| processingType.equalsIgnoreCase(JOBPARAMETER_PROCESSINGTYPE_SBMI)) {
			
			SBMFileProcessingDTO sbmFileProcessingDTO = new SBMFileProcessingDTO();
			sbmFileProcessingDTO.setBatchId(jobId);
			sbmFileProcessingDTO.setXprProcGroupSize(xprProcessingGroupSize);
			
			LOG.info("extracting Xpr To Staging Policy: ");
			sbmFileCompositeDAO.extractXprToStagingPolicy(sbmFileProcessingDTO);

			LOG.info("Succesfully extracted Xpr To Staging Policy: ");
		}
		
		stagingSbmGroupLockDao.deleteStagingGroupLockForExtract(jobId);
		LOG.info("Removed lock for extract by batch id:{} ", jobId);
		
        return RepeatStatus.FINISHED;
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		
		jobId = stepExecution.getJobExecution().getJobId();
		
		if(stagingSbmGroupLockDao.updateStagingGroupLockForExtract(jobId)) {
			LOG.info("Lock acquired for Extracting XPRs by batch id:{}", jobId);
			return;
		}
	}
	
	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		// Auto-generated method stub
		return null;
	}
    
	/**
	 * @param sbmFileCompositeDAO the sbmFileCompositeDAO to set
	 */
	public void setSbmFileCompositeDAO(SBMFileCompositeDAO sbmFileCompositeDAO) {
		this.sbmFileCompositeDAO = sbmFileCompositeDAO;
	}

	/**
	 * @param stagingSbmGroupLockDao the stagingSbmGroupLockDao to set
	 */
	public void setStagingSbmGroupLockDao(StagingSbmGroupLockDao stagingSbmGroupLockDao) {
		this.stagingSbmGroupLockDao = stagingSbmGroupLockDao;
	}

	/**
	 * @param jobExecution the jobExecution to set
	 */
	public void setJobExecution(JobExecution jobExecution) {
		this.jobExecution = jobExecution;
	}

	/**
	 * @param xprProcessingGroupSize the xprProcessingGroupSize to set
	 */
	public void setXprProcessingGroupSize(Integer xprProcessingGroupSize) {
		this.xprProcessingGroupSize = xprProcessingGroupSize;
	}

	/**
	 * @param jobId the jobId to set
	 */
	public void setJobId(Long jobId) {
		this.jobId = jobId;
	}

}
