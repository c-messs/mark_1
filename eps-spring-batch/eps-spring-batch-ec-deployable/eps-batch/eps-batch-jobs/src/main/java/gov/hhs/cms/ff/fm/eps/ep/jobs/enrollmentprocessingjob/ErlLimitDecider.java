/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob;

import static gov.hhs.cms.ff.fm.eps.ep.EPSConstants.JOBPARAMETER_ERL_JOB_TYPE;
import static gov.hhs.cms.ff.fm.eps.ep.EPSConstants.JOBPARAMETER_KEY_SOURCE;
import static gov.hhs.cms.ff.fm.eps.ep.EPSConstants.JOB_TYPE_PROCESSOR;
import gov.hhs.cms.ff.fm.eps.ep.EPSConstants;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.jdbc.core.JdbcTemplate;
/**
 * @author girish.padmanabhan
 *
 */
public class ErlLimitDecider implements JobExecutionDecider {
	private static final Logger LOG = LoggerFactory.getLogger(ErlLimitDecider.class);
	private static final String FLOWEXECUTIONSTATUS_LOOP = "LOOP";
	
	private JdbcTemplate jdbcTemplate;
	private String erlBEMIndexCountSelect;

	/**
	 * Implementation of the interface method. Based on job parameter, returns
	 * the FlowExecutionStatus enum value as 'HUB' or 'FFM'
	 */
	@Override
	public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
		
		String fileSource = jobExecution.getJobParameters().getString(JOBPARAMETER_KEY_SOURCE);
		LOG.debug("file Source: "+fileSource);
        
		if (fileSource.equalsIgnoreCase(EPSConstants.JOBPARAMETER_SOURCE_FFM)) {
			
			String erlJobType = jobExecution.getJobParameters().getString(JOBPARAMETER_ERL_JOB_TYPE);
			
			LOG.info("erlJobType: " + erlJobType);
			
			if (StringUtils.isNotBlank(erlJobType) && erlJobType.equalsIgnoreCase(JOB_TYPE_PROCESSOR)) {
				
				int bemIndexCount = jdbcTemplate.queryForObject(erlBEMIndexCountSelect, Integer.class);
			
				LOG.debug("bemIndexCount for erlJobType: " + bemIndexCount);
				
				if (bemIndexCount > 0) {
		        	return new FlowExecutionStatus(FLOWEXECUTIONSTATUS_LOOP);
		        }
			}
		}
		
        return FlowExecutionStatus.COMPLETED;
    }

	
	/**
	 * @param jdbcTemplate the jdbcTemplate to set
	 */
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * @param erlBEMIndexSelect the erlBEMIndexSelect to set
	 */
	public void setErlBEMIndexCountSelect(String erlBEMIndexCountSelect) {
		this.erlBEMIndexCountSelect = erlBEMIndexCountSelect;
	}
}
