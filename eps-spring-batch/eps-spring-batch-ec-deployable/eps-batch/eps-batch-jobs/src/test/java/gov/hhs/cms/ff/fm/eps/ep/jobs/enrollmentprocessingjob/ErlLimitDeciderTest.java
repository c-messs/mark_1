/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import gov.hhs.cms.ff.fm.eps.ep.EPSConstants;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.stream.FactoryConfigurationError;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.oxm.XmlMappingException;

/**
 * Test class for EPSFlowExecutionDecider
 * 
 * @author girish.padmanabhan
 * 
 */
@RunWith(JUnit4.class)
public class ErlLimitDeciderTest extends TestCase {

	private ErlLimitDecider erlLimitDecider;
	
	private JdbcTemplate mockJdbcTemplate;
	
	private String erlBEMIndexCountSelect;
	
	@Before
	public void setup() throws FactoryConfigurationError, XmlMappingException, Exception {
		erlLimitDecider = new ErlLimitDecider();
		erlLimitDecider.setErlBEMIndexCountSelect(erlBEMIndexCountSelect);
		
		mockJdbcTemplate= createMock(JdbcTemplate.class);
		erlLimitDecider.setJdbcTemplate(mockJdbcTemplate);
	}
	
	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.ErlLimitDecider#decide()}
	 * This method tests the flow execution decision when job parameter is "hub".
	 * 
	 */
	@Test
	public void testDecide_hub() throws Exception {
		
        final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
        params.put("source", new JobParameter("hub"));
        JobParameters jobParameters = new JobParameters(params);
		JobExecution jobExecution = new JobExecution(101L, jobParameters);
		
		FlowExecutionStatus flowStatusEnum = erlLimitDecider.decide(jobExecution, null);
		
		assertNotNull("flowStatusEnum returned is not null", flowStatusEnum);
		assertEquals("flowStatusEnum returned is COMPLETED", FlowExecutionStatus.COMPLETED.getName(), flowStatusEnum.getName()); 
	}
	
	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.ErlLimitDecider#decide()}
	 * This method tests the flow execution decision when job parameter is "ffm".
	 * 
	 */
	@Test
	public void testDecide_ffm() throws Exception {
		
        final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
        params.put("source", new JobParameter("ffm"));
        JobParameters jobParameters = new JobParameters(params);
		JobExecution jobExecution = new JobExecution(101L, jobParameters);
		
		FlowExecutionStatus flowStatusEnum = erlLimitDecider.decide(jobExecution, null);
		
		assertNotNull("flowStatusEnum returned is not null", flowStatusEnum);
		assertEquals("flowStatusEnum returned is COMPLETED", FlowExecutionStatus.COMPLETED.getName(), flowStatusEnum.getName()); 
	}
	
	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.ErlLimitDecider#decide()}
	 * This method tests the flow execution decision when job parameter is "ffm".
	 * 
	 */
	@Test
	public void testDecide_ffm_jobType_null() throws Exception {
		
        final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
        params.put("source", new JobParameter("ffm"));
        JobParameters jobParameters = new JobParameters(params);
		JobExecution jobExecution = new JobExecution(101L, jobParameters);
		
		FlowExecutionStatus flowStatusEnum = erlLimitDecider.decide(jobExecution, null);
		
		assertNotNull("flowStatusEnum returned is not null", flowStatusEnum);
		assertEquals("flowStatusEnum returned is COMPLETED", FlowExecutionStatus.COMPLETED.getName(), flowStatusEnum.getName()); 
	}
	
	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.ErlLimitDecider#decide()}
	 * This method tests the flow execution decision when job parameter is "ffm".
	 * 
	 */
	@Test
	public void testDecide_ffm_jobType_valid() throws Exception {
		
		EasyMock.expect(mockJdbcTemplate.queryForObject(
				erlBEMIndexCountSelect, Integer.class)).andReturn(new Integer(0));
		replay(mockJdbcTemplate);
		
        final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
        params.put("source", new JobParameter("ffm"));
        params.put("jobType", new JobParameter(EPSConstants.JOB_TYPE_PROCESSOR));
        JobParameters jobParameters = new JobParameters(params);
		JobExecution jobExecution = new JobExecution(101L, jobParameters);
		
		FlowExecutionStatus flowStatusEnum = erlLimitDecider.decide(jobExecution, null);
		
		assertNotNull("flowStatusEnum returned is not null", flowStatusEnum);
		assertEquals("flowStatusEnum returned is COMPLETED", FlowExecutionStatus.COMPLETED.getName(), flowStatusEnum.getName()); 
	}
	
	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.ErlLimitDecider#decide()}
	 * This method tests the flow execution decision when job parameter is "ffm".
	 * 
	 */
	@Test
	public void testDecide_ffm_jobType_valid_Loop() throws Exception {
		
		EasyMock.expect(mockJdbcTemplate.queryForObject(
				erlBEMIndexCountSelect, Integer.class)).andReturn(new Integer(1));
		replay(mockJdbcTemplate);
		
        final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
        params.put("source", new JobParameter("ffm"));
        params.put("jobType", new JobParameter(EPSConstants.JOB_TYPE_PROCESSOR));
        JobParameters jobParameters = new JobParameters(params);
		JobExecution jobExecution = new JobExecution(101L, jobParameters);
		
		FlowExecutionStatus flowStatusEnum = erlLimitDecider.decide(jobExecution, null);
		
		assertNotNull("flowStatusEnum returned is not null", flowStatusEnum);
		assertEquals("flowStatusEnum returned is LOOP", "LOOP", flowStatusEnum.getName()); 
	}

	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.ErlLimitDecider#decide()}
	 * This method tests the flow execution decision when job parameter is "ffm".
	 * 
	 */
	@Test
	public void testDecide_ffm_jobType_invalid() throws Exception {
		
        final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
        params.put("source", new JobParameter("ffm"));
        params.put("jobType", new JobParameter("none"));
        JobParameters jobParameters = new JobParameters(params);
		JobExecution jobExecution = new JobExecution(101L, jobParameters);
		
		FlowExecutionStatus flowStatusEnum = erlLimitDecider.decide(jobExecution, null);
		
		assertNotNull("flowStatusEnum returned is not null", flowStatusEnum);
		assertEquals("flowStatusEnum returned is COMPLETED", FlowExecutionStatus.COMPLETED.getName(), flowStatusEnum.getName()); 
	}

}
