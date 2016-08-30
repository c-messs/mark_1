package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm;

import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.SBM_EXECUTION_REPORT;

import java.sql.SQLException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;

import gov.hhs.cms.ff.fm.eps.ep.jobs.BaseJobExecutionListener;

/**
 * This class serves as the Job Execution Listener for the SBM UpadteStatus Job to
 * perform certain pre and post activities for an execution of the Job.
 * 
 * @author eps
 *
 */
public class SbmExecutionReportJobListener extends BaseJobExecutionListener {
	
	private static final Logger LOG = LoggerFactory.getLogger(SbmExecutionReportJobListener.class);

	
	private Long jobId;	
	
	@Override
	public void beforeJob(JobExecution jobExecution) {
		
		super.beforeJob(jobExecution);
	}
	
	@Override
	public void afterJob(JobExecution jobExecution) {
		super.afterJob(jobExecution); 
	}
	
	/**
	 * check if there is any existing job ( with 'STARTED' status)
	 * @return
	 */
	public String checkJobInstanceForBatchProcess() throws SQLException{
         return  null;
	}
	
	/**
	 * Method to get the Batch Business Id Prefix
	 * 
	 * @return BatchBusinessIdPrefix
	 */
	public String getCodeForBatchBusinessId() {
		return getBatchBusinessIdPrefix();
	}

	/**
	 * This method returns Batch Business id prefix according to job name
	 * @return
	 */
	private String getBatchBusinessIdPrefix() {
		return SBM_EXECUTION_REPORT;
	}
	
	/**
	 * This method should return the Job run user id/name.
	 * 
	 * @return  runBy name
	 */
	public String getRunBy() {
		return jobId == null? StringUtils.EMPTY : jobId.toString();
	}

}
