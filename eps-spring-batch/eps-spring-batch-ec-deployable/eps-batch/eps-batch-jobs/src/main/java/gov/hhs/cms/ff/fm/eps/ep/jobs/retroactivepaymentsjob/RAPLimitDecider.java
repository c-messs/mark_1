package gov.hhs.cms.ff.fm.eps.ep.jobs.retroactivepaymentsjob;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.jdbc.core.JdbcTemplate;


/**
 * This class serves as the Limit Decider to check whether any more issuerid's are 
 * present which need to be processed or there are no more left so that process 
 * can finish.
 * 
 * @author Shashiidar Pabolu
 *
 */

public class RAPLimitDecider implements JobExecutionDecider {
	
	private String 	rapStageIssuerIdSelect;
	private JdbcTemplate jdbcTemplate;
	
	/**
	 * Implementation of the interface method. Based on count returned by query
	 * the FlowExecutionStatus enum value as 'LOOP' OR 'COMPLETE'
	 */
	
	@Override
	public FlowExecutionStatus decide(JobExecution jobExecution,
			StepExecution stepExecution) {
		
		int pendingIssuerCount = jdbcTemplate.queryForObject(rapStageIssuerIdSelect, Integer.class);
					
		if (pendingIssuerCount > 0) {
        	return new FlowExecutionStatus("LOOP");
		}
        else 
        {
        	return new FlowExecutionStatus("COMPLETE");	
        }
		
	}

	public void setRapStageIssuerIdSelect(String rapStageIssuerIdSelect) {
		this.rapStageIssuerIdSelect = rapStageIssuerIdSelect;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

}
