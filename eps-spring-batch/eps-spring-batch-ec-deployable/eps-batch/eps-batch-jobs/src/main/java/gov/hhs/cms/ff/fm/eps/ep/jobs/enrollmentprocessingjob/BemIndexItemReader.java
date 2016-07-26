/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob;

import static gov.hhs.cms.ff.fm.eps.ep.EPSConstants.JOBPARAMETER_ERL_JOB_TYPE;
import static gov.hhs.cms.ff.fm.eps.ep.EPSConstants.JOBPARAMETER_KEY_SOURCE;
import static gov.hhs.cms.ff.fm.eps.ep.EPSConstants.JOBPARAMETER_SOURCE_FFM;
import static gov.hhs.cms.ff.fm.eps.ep.EPSConstants.JOB_TYPE_PROCESSOR;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.jdbc.core.RowMapper;

// TODO: Auto-generated Javadoc
/**
 * The Class BemIndexItemReader.
 *
 * @author girish.padmanabhan
 * @param <T> the generic type
 */
public class BemIndexItemReader<T> extends JdbcCursorItemReader<T> {
	
	/** The Constant LOG. */
	private static final Logger LOG = LoggerFactory.getLogger(BemIndexItemReader.class);
	
	/** The job execution. */
	private JobExecution jobExecution;
	
	/** The data source. */
	private DataSource dataSource;
	
	/** The sql. */
	private String sql;
	
	/** The row mapper. */
	private RowMapper<T> rowMapper;

	/**
	 * Sets the job execution.
	 *
	 * @param jobExecution the jobExecution to set
	 */
	public void setJobExecution(JobExecution jobExecution) {
		this.jobExecution = jobExecution;
	}

	/**
	 * Public setter for the data source for injection purposes.
	 *
	 * @param dataSource the new data source
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		super.setDataSource(this.dataSource);
	}
	
	/**
	 * Set the RowMapper to be used for all calls to read().
	 *
	 * @param rowMapper the new row mapper
	 */
	public void setRowMapper(RowMapper<T> rowMapper) {
		this.rowMapper = rowMapper;
		super.setRowMapper(this.rowMapper);
	}

	/**
	 * Set the SQL statement to be used when creating the cursor. 
	 *
	 * @param sql the new sql
	 */
	public void setSql(String sql) {
		this.sql = sql;
		
		String source = jobExecution.getJobParameters().getString(JOBPARAMETER_KEY_SOURCE);
		
		if(source.equalsIgnoreCase(JOBPARAMETER_SOURCE_FFM)) {

			String jobType = jobExecution.getJobParameters().getString(JOBPARAMETER_ERL_JOB_TYPE);
			
			if (StringUtils.isNotBlank(jobType) && jobType.equalsIgnoreCase(JOB_TYPE_PROCESSOR)) {
				// Replace sql param with job id
				this.sql = sql.replace(":jobId", jobExecution.getJobId().toString());
			}
		}
		super.setSql(this.sql);
		
		LOG.debug("sql: " + super.getSql());
	}

}
