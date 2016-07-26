package gov.hhs.cms.ff.fm.eps.ep.jobs.retroactivepaymentsjob;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author shasidar.pabolu
 *
 */
public class RapStageProcessTasklet implements Tasklet {

	private static final Logger LOG = LoggerFactory
			.getLogger(RapStageProcessTasklet.class);

	private JdbcTemplate jdbcTemplate;
	private String loadRapStageSql;
		
	@Override
	public RepeatStatus execute(StepContribution arg0, ChunkContext arg1)
			throws Exception {	
     
		LOG.debug("loadRapStageSql: " + loadRapStageSql);
		Long jobId = arg1.getStepContext().getStepExecution().getJobExecution().getJobId();
		jdbcTemplate.update(loadRapStageSql,jobId,jobId);
	
		return RepeatStatus.FINISHED;
	}


	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void setLoadRapStageSql(String loadRapStageSql) {
		this.loadRapStageSql = loadRapStageSql;
	}

}
