package gov.hhs.cms.ff.fm.eps.ep.jobs.retroactivepaymentsjob;

import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;

@RunWith(JUnit4.class)
public class RAPFlowExecutionDeciderTest extends TestCase {
	
	RAPFlowExecutionDecider rapFlowExecutionDecider;
	
	@Before
	public void setup()  {
		rapFlowExecutionDecider = new RAPFlowExecutionDecider();
	}
	
	
	@Test
	public void testDecide_Source_RAP() throws Exception {
		
        final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
        params.put("type", new JobParameter("RAP"));
        JobParameters jobParameters = new JobParameters(params);
		JobExecution jobExecution = new JobExecution(101L, jobParameters);
		
		FlowExecutionStatus flowStatusEnum = rapFlowExecutionDecider.decide(jobExecution, null);
		
		assertNotNull("flowStatusEnum returned is not null", flowStatusEnum);
		assertEquals("flowStatusEnum returned is RAP", "RAP", flowStatusEnum.getName()); 
	}
	
	@Test
	public void testDecide_Source_RAPSTAGE() throws Exception {
		
        final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
        params.put("type", new JobParameter("RAPSTAGE"));
        JobParameters jobParameters = new JobParameters(params);
		JobExecution jobExecution = new JobExecution(101L, jobParameters);
		
		FlowExecutionStatus flowStatusEnum = rapFlowExecutionDecider.decide(jobExecution, null);
		
		assertNotNull("flowStatusEnum returned is not null", flowStatusEnum);
		assertEquals("flowStatusEnum returned is RAPSTAGE", "RAPSTAGE", flowStatusEnum.getName()); 
	}
	
	@Test
	public void testDecide_Source_Null() throws Exception {
		
        final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
        JobParameters jobParameters = new JobParameters(params);
		JobExecution jobExecution = new JobExecution(101L, jobParameters);
		
		FlowExecutionStatus flowStatusEnum = rapFlowExecutionDecider.decide(jobExecution, null);
		
		assertNotNull("flowStatusEnum returned is not null", flowStatusEnum);
		assertEquals("flowStatusEnum returned is RAP", "RAP", flowStatusEnum.getName()); 
	}
	

}
