package gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob;

import static gov.hhs.cms.ff.fm.eps.ep.EPSConstants.JOBPARAMETER_KEY_SOURCE;
import static gov.hhs.cms.ff.fm.eps.ep.EPSConstants.JOBPARAMETER_SOURCE_FFM;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * This is the listener class used for the Erl Bem Handler Step.
 * beforeStep() method - locks the bem index data for the first available qhp id
 * afterStep() method - none
 * 
 * @author eps
 *
 */
public class ErlBemHandlerStepListener implements StepExecutionListener  {
	private static final Logger LOG = LoggerFactory.getLogger(ErlBemHandlerStepListener.class);
	
	private JdbcTemplate jdbcTemplate;
	private String stageDataSql;
	private String dataLockSql;
	private String postCleanUpSql;
	private String stageDataCleanUpSql;
	
	/**
	 * This method sets the lock on Bem Index records with the job id. 
	 */
	@Override
	public void beforeStep(StepExecution stepExecution) {
		LOG.debug("ErlBemHandlerStepListener.Before Step: " + stepExecution.getStepName());
		
		String source = stepExecution.getJobParameters().getString(JOBPARAMETER_KEY_SOURCE);
		
		if (JOBPARAMETER_SOURCE_FFM.equalsIgnoreCase(source)) {
			
			Long jobId = stepExecution.getJobExecution().getJobId();
			
			try {
				jdbcTemplate.update(stageDataSql, jobId);
				
				LOG.debug("Executed sql: " + stageDataSql + " for batchId: " + jobId);
				
				if(StringUtils.isNotEmpty(dataLockSql)) {
					jdbcTemplate.update(dataLockSql, jobId, jobId);
				}
				
			} catch(DuplicateKeyException e) {
				LOG.info("Got Unique Constraint Exception while inserting to StagingPlanLock for Job ID: " + jobId);
				LOG.info(e.getMessage());
			}
		}
	}
	
	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		LOG.debug("ErlBemHandlerStepListener.afterStep: " + stepExecution.getStepName());
		
		String source = stepExecution.getJobParameters().getString(JOBPARAMETER_KEY_SOURCE);
		
		if (JOBPARAMETER_SOURCE_FFM.equalsIgnoreCase(source)) {
			
			Long jobId = stepExecution.getJobExecution().getJobId();
			
			jdbcTemplate.update(postCleanUpSql, jobId);
			
			LOG.debug("Executed sql: " + postCleanUpSql + " for batchId: " + jobId);
			
			jdbcTemplate.update(stageDataCleanUpSql, jobId);
		}
		return null;
	}

	/**
	 * @param jdbcTemplate the jdbcTemplate to set
	 */
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * @param stageDataSql the stageDataSql to set
	 */
	public void setStageDataSql(String stageDataSql) {
		this.stageDataSql = stageDataSql;
	}

	/**
	 * @param dataLockSql the dataLockSql to set
	 */
	public void setDataLockSql(String dataLockSql) {
		this.dataLockSql = dataLockSql;
	}
	
	/**
	 * @param postCleanUpSql the postCleanUpSql to set
	 */
	public void setPostCleanUpSql(String postCleanUpSql) {
		this.postCleanUpSql = postCleanUpSql;
	}

	/**
	 * @param stageDataCleanUpSql the stageDataCleanUpSql to set
	 */
	public void setStageDataCleanUpSql(String stageDataCleanUpSql) {
		this.stageDataCleanUpSql = stageDataCleanUpSql;
	}

}
