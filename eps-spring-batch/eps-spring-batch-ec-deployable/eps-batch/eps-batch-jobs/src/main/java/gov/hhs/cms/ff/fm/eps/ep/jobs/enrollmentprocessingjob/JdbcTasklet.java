package gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob;

import static gov.hhs.cms.ff.fm.eps.ep.EPSConstants.JOBPARAMETER_ERL_JOB_TYPE;
import static gov.hhs.cms.ff.fm.eps.ep.EPSConstants.JOBPARAMETER_KEY_SOURCE;
import static gov.hhs.cms.ff.fm.eps.ep.EPSConstants.JOBPARAMETER_SOURCE_FFM;
import static gov.hhs.cms.ff.fm.eps.ep.EPSConstants.JOB_TYPE_PROCESSOR;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;

import com.accenture.foundation.common.exception.ApplicationException;

/**
 * Cutsom JDBC Tasklet to execute the sql
 * 
 * @author tomi vanek
 *
 */
public class JdbcTasklet implements Tasklet {
	private static final Logger LOG = LoggerFactory.getLogger(JdbcTasklet.class);
	
	private JdbcTemplate jdbcTemplate;
    private List<String> sqls;
    private JobExecution jobExecution;
	
	/** 
	 * @param contribution
	 * @param chunkContext
	 * @return RepeatStatus.FINISHED
	 * @throws Exception
	 */
    @Override
	public RepeatStatus execute(StepContribution contribution,
			ChunkContext chunkContext) throws Exception {
		
		String source = jobExecution.getJobParameters().getString(JOBPARAMETER_KEY_SOURCE);
		
		if(source.equalsIgnoreCase(JOBPARAMETER_SOURCE_FFM)) {
			
			String erlJobType = jobExecution.getJobParameters().getString(JOBPARAMETER_ERL_JOB_TYPE);
			
			LOG.info("erlJobType: " + erlJobType);
			
			if (StringUtils.isBlank(erlJobType)) {
				return RepeatStatus.FINISHED;
			}
			
			if (!StringUtils.trim(erlJobType).equalsIgnoreCase(JOB_TYPE_PROCESSOR)) {
				throw new ApplicationException("EPROD-99: No valid value supplied for job parameter 'jobType' for ERL Ingestion Job");
			}
			
			for (String sql : sqls) {
				
				if(sql.contains(":jobId")){
					sql = sql.replace(":jobId", jobExecution.getJobId().toString());
				}	
				
				LOG.info("sql: " + sql);
		 		jdbcTemplate.execute(sql);
	 		
			}
		}
		
        return RepeatStatus.FINISHED;
	}

	/**
	 * @param jdbcTemplate the jdbcTemplate to set
	 */
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * @param sqlArray the sqlArray to set
	 */
	public void setSqls(List<String> sqls) {
		this.sqls = sqls;
	}

	/**
	 * @param jobExecution the jobExecution to set
	 */
	public void setJobExecution(JobExecution jobExecution) {
		this.jobExecution = jobExecution;
	}

}
