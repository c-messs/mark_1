package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm;

import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.JOBPARAMETER_PROCESSINGTYPE_SBMI;
import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.JOBPARAMETER_PROCESSTYPE;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.jdbc.core.JdbcTemplate;

import gov.hhs.cms.ff.fm.eps.ep.dao.StagingSbmFileDao;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMCache;

/**
 * This is the listener class used for the Erl Bem Handler Step.
 * beforeStep() method - locks the bem index data for the first available qhp id
 * afterStep() method - none
 * 
 * @author eps
 *
 */
public class XprProcessingStepExecutionListener implements StepExecutionListener  {
	
	private static final Logger LOG = LoggerFactory.getLogger(XprProcessingStepExecutionListener.class);

	private static final CharSequence JOB_ID = ":jobId";
	
	private JdbcTemplate jdbcTemplate;
	private String processingType;
	private List<String> stageDataSqls;
	private List<String> postCleanUpSqls;
	private StagingSbmFileDao stagingSbmFileDao;
	
	/**
	 * This method sets the lock on Bem Index records with the job id. 
	 */
	@Override
	public void beforeStep(StepExecution stepExecution) {
		LOG.debug("XprProcessingStepExecutionListener.Before Step: " + stepExecution.getStepName());
		
		this.processingType = stepExecution.getJobExecution().getJobParameters().getString(JOBPARAMETER_PROCESSTYPE);
		
		if (StringUtils.isBlank(processingType) 
				|| JOBPARAMETER_PROCESSINGTYPE_SBMI.equalsIgnoreCase(processingType)) {
			
			String jobId = stepExecution.getJobExecution().getJobId().toString();
			
			for (String sql : stageDataSqls) {
				
				if(sql.contains(JOB_ID)) {
					sql = sql.replace(JOB_ID, jobId);
				}	
				
				LOG.debug("sql: " + sql);
		 		jdbcTemplate.execute(sql);
			}
			
			loadPolicyIdMap(jobId);
		}
	}
	
	private void loadPolicyIdMap(String jobId) {

		SBMCache.getPolicyIds().clear();
		
		List<String> policyIds = stagingSbmFileDao.getStagingPolicies(Long.valueOf(jobId));
		SBMCache.getPolicyIds().addAll(policyIds);
		
		LOG.info("SBMCache.PolicyIds {}: ", SBMCache.getPolicyIds());
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		LOG.debug("ErlBemHandlerStepListener.afterStep: " + stepExecution.getStepName());
		
		if (StringUtils.isBlank(processingType) 
				|| JOBPARAMETER_PROCESSINGTYPE_SBMI.equalsIgnoreCase(processingType)) {
			
			
			String jobId = stepExecution.getJobExecution().getJobId().toString();
			
			for (String sql : postCleanUpSqls) {
				
				if(sql.contains(JOB_ID)) {
					sql = sql.replace(JOB_ID, jobId);
				}	
				
				LOG.debug("sql: " + sql);
		 		jdbcTemplate.execute(sql);
			}
		}
		return null;
	}

	/**
	 * @param stageDataSqls the stageDataSqls to set
	 */
	public void setStageDataSqls(List<String> stageDataSqls) {
		this.stageDataSqls = stageDataSqls;
	}

	/**
	 * @param postCleanUpSqls the postCleanUpSqls to set
	 */
	public void setPostCleanUpSqls(List<String> postCleanUpSqls) {
		this.postCleanUpSqls = postCleanUpSqls;
	}

	/**
	 * @param jdbcTemplate the jdbcTemplate to set
	 */
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * @param stagingSbmFileDao the stagingSbmFileDao to set
	 */
	public void setStagingSbmFileDao(StagingSbmFileDao stagingSbmFileDao) {
		this.stagingSbmFileDao = stagingSbmFileDao;
	}

}
