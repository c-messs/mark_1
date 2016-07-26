package gov.hhs.cms.ff.fm.eps.ep.jobs.retroactivepaymentsjob;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.jdbc.core.JdbcTemplate;


/** 
 * This class serves as the RAP Job Process Step Execution Listener for the RAP Job to lock a particular 
 * issuer id and work on policy versions related to that isssuer id. * 
 * 
 * @author shashidar pabolu
 *
 */

public class RapJobProcessStepExecutionListener implements StepExecutionListener {

	private String lockStageIssuerId;
	private String deleteProcessedStageIssuerId;

	private JdbcTemplate jdbcTemplate;
	
	@Override
	public void beforeStep(StepExecution stepExecution) {
	
		Long jobId = stepExecution.getJobExecution().getJobId();
		jdbcTemplate.update(lockStageIssuerId,jobId);
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		// Delete the row from stage table once the issuer id has been processed.
		Long jobId = stepExecution.getJobExecution().getJobId();
		jdbcTemplate.update(deleteProcessedStageIssuerId,jobId);
		return null;
	}
	
	public void setLockStageIssuerId(String lockStageIssuerId) {
		this.lockStageIssuerId = lockStageIssuerId;
	}
	
	public void setDeleteProcessedStageIssuerId(String deleteProcessedStageIssuerId) {
		this.deleteProcessedStageIssuerId = deleteProcessedStageIssuerId;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}


}
