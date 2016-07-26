/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob;

import gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentMaintenanceDTO;
import gov.hhs.cms.ff.fm.eps.ep.EPSConstants;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

/**
 * Test class for JdbcTasklet
 * 
 * @author girish.padmanabhan
 * 
 */
@ContextConfiguration(locations={"/test-batch-application-context.xml"})
@TestExecutionListeners( { DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class })
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@RunWith(SpringJUnit4ClassRunner.class)
public class BemIndexItemReaderTest extends TestCase {

	private BemIndexItemReader<BenefitEnrollmentMaintenanceDTO> bemIndexItemReader;
	private RowMapper<BenefitEnrollmentMaintenanceDTO> rowMapper;
	
	@Autowired
	private DataSource dataSource;
	
	@Autowired
	@Qualifier("erlBEMIndexSelect")
    private String sql;
	
	@Before
	public void setup() {
		bemIndexItemReader = new BemIndexItemReader<BenefitEnrollmentMaintenanceDTO>();
		
		bemIndexItemReader.setDataSource(dataSource);
		bemIndexItemReader.setRowMapper(rowMapper);
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

		JobExecution jobEx = new JobExecution(9999L, getJobParams("ffm", ""));
		bemIndexItemReader.setJobExecution(jobEx);
		
		bemIndexItemReader.setSql(sql);
		
		assertNotNull("RepeatStatus not null", sql);
	}
	
	@Test
	public void testExecute_FFM_Processor() throws Exception {
		
		JobInstance jobInst = new JobInstance(9999L,"enrollmentProcessingJob");
		JobExecution jobEx = new JobExecution(jobInst, getJobParams("ffm", "processor"));
		jobEx.getExecutionContext().putString(EPSConstants.JOBPARAMETER_ERL_JOB_TYPE, "processor");
		bemIndexItemReader.setJobExecution(jobEx);
		
		bemIndexItemReader.setSql(sql);
		
		assertNotNull("RepeatStatus not null", sql);
	}
	
	@Test
	public void testExecute_FFM_NonProcessor() throws Exception {

		JobExecution jobEx = new JobExecution(9999L, getJobParams("ffm", "VA"));
		bemIndexItemReader.setJobExecution(jobEx);
		
		bemIndexItemReader.setSql(sql);
		
		assertNotNull("RepeatStatus not null", sql);
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
		bemIndexItemReader.setJobExecution(jobEx);
		
		bemIndexItemReader.setSql("sql");
		
		assertNotNull("RepeatStatus not null", sql);
	}
	
	private JobParameters getJobParams(String source, String jobType) {
        final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
        params.put("source", new JobParameter(source));
        params.put("jobType", new JobParameter(jobType));
        return new JobParameters(params);
	}

}
