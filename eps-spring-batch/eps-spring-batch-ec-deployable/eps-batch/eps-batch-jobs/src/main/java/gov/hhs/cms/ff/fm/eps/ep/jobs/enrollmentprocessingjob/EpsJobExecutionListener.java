package gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob;

import static gov.hhs.cms.ff.fm.eps.ep.EPSConstants.JOBPARAMETER_ERL_JOB_TYPE;
import static gov.hhs.cms.ff.fm.eps.ep.EPSConstants.JOBPARAMETER_KEY_SOURCE;
import static gov.hhs.cms.ff.fm.eps.ep.EPSConstants.JOBPARAMETER_SOURCE_FFM;
import gov.hhs.cms.ff.fm.eps.ep.jobs.BaseJobExecutionListener;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
/**
 * This class serves as the Job Execution Listener for the RAP Job to
 * perform certain pre and post activities for an execution of the RAP Job.
 * 
 * @author eps
 *
 */
public class EpsJobExecutionListener extends BaseJobExecutionListener implements JobExecutionListener {

	private static final String EPS_JOB_NM = "enrollmentProcessingBatchJob";
	private static final String ERL_FILEINGEST_JOB = "ERLFILEINGEST";
	
	private String source;
	private String jobType;
	private Long jobId;

	@Override
	public void beforeJob(JobExecution jobExecution) {
		this.jobId = jobExecution.getJobId();
		
		source = jobExecution.getJobParameters().getString(JOBPARAMETER_KEY_SOURCE);
		jobType = jobExecution.getJobParameters().getString(JOBPARAMETER_ERL_JOB_TYPE);
		
		if(StringUtils.isNotBlank(source) && source.equalsIgnoreCase(JOBPARAMETER_SOURCE_FFM)
				&& StringUtils.isBlank(jobType)) {
			
	        String jobName = jobExecution.getJobInstance().getJobName();
	        setBlockConcurrentJobExecutionList(Arrays.asList(jobName));
			
			super.beforeJob(jobExecution);
		}
	}
	
	@Override
	public void afterJob(JobExecution jobExecution) {
		
		if(StringUtils.isNotBlank(source) && source.equalsIgnoreCase(JOBPARAMETER_SOURCE_FFM)
				&& StringUtils.isBlank(jobType)) {
			// updates batch log
			super.afterJob(jobExecution);
		}
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
		if(StringUtils.isNotBlank(source) && source.equalsIgnoreCase(JOBPARAMETER_SOURCE_FFM)
				&& StringUtils.isBlank(jobType)) {
			return ERL_FILEINGEST_JOB;
		}
		return EPS_JOB_NM;
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
