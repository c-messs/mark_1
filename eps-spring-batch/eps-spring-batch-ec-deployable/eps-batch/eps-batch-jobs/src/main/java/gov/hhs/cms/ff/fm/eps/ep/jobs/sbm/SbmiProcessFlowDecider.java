/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm;

import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.CONTINUE;
import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.JOBPARAMETER_PROCESSINGTYPE_SBMI;
import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.JOBPARAMETER_PROCESSTYPE;
import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.JOB_EXIT_CODE;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author rajesh.talanki
 *
 */
public class SbmiProcessFlowDecider implements JobExecutionDecider {

	private static final Logger LOG = LoggerFactory.getLogger(SbmiProcessFlowDecider.class);
	
	public static final String FLOWEXECUTION_XPR = "XPR";
	public static final String FLOWEXECUTION_EXTRACT = "EXTRACT";
	public static final String FLOWEXECUTION_FILE= "FILE";
	public static final String FLOWEXECUTION_COMPLETE = "COMPLETE";
	
	private JdbcTemplate jdbcTemplate;
	private String stagingXprCountSelect;
	private String pendingExtractCountSelect;
			
	/* (non-Javadoc)
	 * @see org.springframework.batch.core.job.flow.JobExecutionDecider#decide(org.springframework.batch.core.JobExecution, org.springframework.batch.core.StepExecution)
	 */
	@Override
	public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
		
		String processingType = jobExecution.getJobParameters().getString(JOBPARAMETER_PROCESSTYPE);
		LOG.info("Job input parameter {} : {} ", JOBPARAMETER_PROCESSTYPE, processingType);
		
		if (StringUtils.isBlank(processingType) 
				|| processingType.equalsIgnoreCase(JOBPARAMETER_PROCESSINGTYPE_SBMI)) {
		
			int stagingXprCount = jdbcTemplate.queryForObject(stagingXprCountSelect, Integer.class);
			
			LOG.debug("stagingXprCount for Sbmi JobType: " + stagingXprCount);
			
			if (stagingXprCount > 0) {
				LOG.info("Continuing to process next XPR");
	        	return new FlowExecutionStatus(FLOWEXECUTION_XPR);
	        }
			
			int pendingExtractCount = jdbcTemplate.queryForObject(pendingExtractCountSelect, Integer.class);
			
			LOG.debug("pending extract Count for Sbmi JobType: " + pendingExtractCount);
			
			if (pendingExtractCount > 0) {
				LOG.info("Continuing to process next SUMMARY EXTRACT");
	        	return new FlowExecutionStatus(FLOWEXECUTION_EXTRACT);
	        }
			
			//Continue to FileIngestion step if job exit code is 'CONTINUE'
			Object jobExitCode = stepExecution.getJobExecution().getExecutionContext().get(JOB_EXIT_CODE);
			LOG.info("jobExitCode: {}", jobExitCode);
			
			if( (jobExitCode instanceof String) && CONTINUE.equalsIgnoreCase((String)jobExitCode)) {
				LOG.info("Continuing FileIngestion");
				return new FlowExecutionStatus(FLOWEXECUTION_FILE);				
			}
					
		}
		return new FlowExecutionStatus(FLOWEXECUTION_COMPLETE);
	}
	
	/**
	 * @param jdbcTemplate the jdbcTemplate to set
	 */
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * @param stagingXprCountSelect the stagingXprCountSelect to set
	 */
	public void setStagingXprCountSelect(String stagingXprCountSelect) {
		this.stagingXprCountSelect = stagingXprCountSelect;
	}

	/**
	 * @param pendingExtractCountSelect the pendingExtractCountSelect to set
	 */
	public void setPendingExtractCountSelect(String pendingExtractCountSelect) {
		this.pendingExtractCountSelect = pendingExtractCountSelect;
	}
	
}
