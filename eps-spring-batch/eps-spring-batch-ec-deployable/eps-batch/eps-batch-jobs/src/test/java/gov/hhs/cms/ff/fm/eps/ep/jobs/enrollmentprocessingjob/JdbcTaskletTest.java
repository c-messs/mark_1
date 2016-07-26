/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.accenture.foundation.common.exception.ApplicationException;

/**
 * Test class for JdbcTasklet
 * 
 * @author girish.padmanabhan
 * 
 */
@ContextConfiguration(locations={"/test-batch-application-context.xml"})
@TestExecutionListeners( { DependencyInjectionTestExecutionListener.class })
@RunWith(SpringJUnit4ClassRunner.class)
public class JdbcTaskletTest extends TestCase {

	private JdbcTasklet jdbcTasklet;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	@Qualifier("erlBEMIndexSelect")
    private String sql;
	
	@Autowired
	@Qualifier("stagingPlanLockDeleteSql")
    private String sql2;
	
	private List<String> sqlArray = new ArrayList<String>(); 
	
	@Before
	public void setup() {
		jdbcTasklet = new JdbcTasklet();
		
		jdbcTasklet.setJdbcTemplate(jdbcTemplate);
		
		sqlArray.add(sql);
		sqlArray.add(sql2); 
		
		jdbcTasklet.setSqls(sqlArray);
	}

	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.JdbcTasklet#execute()}
	 * This method tests the sql execution of the Tasklet, which then returns a 'RepeatStatus' of 'FINISHED'.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testExecute_FFM() throws Exception {

		JobExecution jobEx = new JobExecution(9999L, getJobParams("ffm", "processor"));
		jobEx.setJobInstance(new JobInstance(9999L, "processor"));
		jdbcTasklet.setJobExecution(jobEx);
		
		RepeatStatus status = jdbcTasklet.execute(null, null);
		
		assertNotNull("RepeatStatus not null", status);
		assertEquals("RepeatStatus is FINISHED", RepeatStatus.FINISHED, status);
	}
	
	/**
	 * Invalid job type parameter value
	 * 
	 * @throws Exception
	 */
	@Test(expected=ApplicationException.class)
	public void testExecute_FFM_With_JobType_Invalid() throws Exception {

		JobExecution jobEx = new JobExecution(9999L, getJobParams("ffm", "VA"));
		jdbcTasklet.setJobExecution(jobEx);
		
		RepeatStatus status = jdbcTasklet.execute(null, null);
		
		assertNotNull("Exception expected via annotation", status);
		assertEquals("RepeatStatus is FINISHED", RepeatStatus.FINISHED, status);
	}
	
	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.JdbcTasklet#execute()}
	 * This method tests the sql execution of the Tasklet, which then returns a 'RepeatStatus' of 'FINISHED'.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testExecute_FFM_Null_JobType() throws Exception {

		JobExecution jobEx = new JobExecution(9999L, getJobParams("ffm", ""));
		jdbcTasklet.setJobExecution(jobEx);
		
		RepeatStatus status = jdbcTasklet.execute(null, null);
		
		assertNotNull("RepeatStatus not null", status);
		assertEquals("RepeatStatus is FINISHED", RepeatStatus.FINISHED, status);
	}
	
	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.JdbcTasklet#execute()}
	 * This method tests the sql execution of the Tasklet, which then returns a 'RepeatStatus' of 'FINISHED'.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testExecute_Non_FFM() throws Exception {

		JobExecution jobEx = new JobExecution(9999L, getJobParams("hub", ""));
		jdbcTasklet.setJobExecution(jobEx);
		
		RepeatStatus status = jdbcTasklet.execute(null, null);
		
		assertNotNull("RepeatStatus not null", status);
		assertEquals("RepeatStatus is FINISHED", RepeatStatus.FINISHED, status);
	}
	
	private JobParameters getJobParams(String source, String jobType) {
        final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
        params.put("source", new JobParameter(source));
        params.put("jobType", new JobParameter(jobType));
        return new JobParameters(params);
	}

}
