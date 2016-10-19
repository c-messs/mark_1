package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm;

import static gov.hhs.cms.ff.fm.eps.ep.jobs.sbm.SbmiProcessTypeFlowExecutionDecider.FLOWEXECUTIONSTATUS_SBMI;
import static gov.hhs.cms.ff.fm.eps.ep.jobs.sbm.SbmiProcessTypeFlowExecutionDecider.FLOWEXECUTIONSTATUS_SBMR;
import static gov.hhs.cms.ff.fm.eps.ep.jobs.sbm.SbmiProcessTypeFlowExecutionDecider.FLOWEXECUTIONSTATUS_XPR;
import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.JOBPARAMETER_PROCESSTYPE;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;

import junit.framework.TestCase;

/**
 * @author rajesh.talanki
 *
 */
@RunWith(JUnit4.class)
public class SbmiProcessTypeFlowExecutionDeciderTest extends TestCase {
	
	private SbmiProcessTypeFlowExecutionDecider sbmFlowDecider;
		
	@Before
	public void setUp() {
		sbmFlowDecider = new SbmiProcessTypeFlowExecutionDecider();
	}
	
	@Test
	public void testXPRFlow() {
		final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
        params.put(JOBPARAMETER_PROCESSTYPE, new JobParameter("xpr"));
        JobParameters jobParameters = new JobParameters(params);
		JobExecution jobExecution = new JobExecution(101L, jobParameters);
		
		FlowExecutionStatus flowExecutionStatus = sbmFlowDecider.decide(jobExecution, null);
		
		Assert.assertNotNull("flowExecutionStatus returned is cannot be null", flowExecutionStatus);
		Assert.assertEquals("flowExecutionStatus returned is not XPR", FLOWEXECUTIONSTATUS_XPR, flowExecutionStatus.getName()); 
	}
	
	@Test
	public void testSBMRFlow() {
		final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
        params.put(JOBPARAMETER_PROCESSTYPE, new JobParameter("sbmr"));
        JobParameters jobParameters = new JobParameters(params);
		JobExecution jobExecution = new JobExecution(101L, jobParameters);
		
		FlowExecutionStatus flowExecutionStatus = sbmFlowDecider.decide(jobExecution, null);
		
		Assert.assertNotNull("flowExecutionStatus returned is cannot be null", flowExecutionStatus);
		Assert.assertEquals("flowExecutionStatus returned is not SBMR", FLOWEXECUTIONSTATUS_SBMR, flowExecutionStatus.getName()); 
	}
	
	@Test
	public void testDefaultFlow_nullValue() {
		final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();       
        JobParameters jobParameters = new JobParameters(params);
		JobExecution jobExecution = new JobExecution(101L, jobParameters);
		
		FlowExecutionStatus flowExecutionStatus = sbmFlowDecider.decide(jobExecution, null);
		
		Assert.assertNotNull("flowExecutionStatus returned is cannot be null", flowExecutionStatus);
		Assert.assertEquals("flowExecutionStatus returned is not SBMI", FLOWEXECUTIONSTATUS_SBMI, flowExecutionStatus.getName()); 
	}
	
	@Test
	public void testDefaultFlow_someValue() {
		final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
        params.put(JOBPARAMETER_PROCESSTYPE, new JobParameter("abcd"));
        JobParameters jobParameters = new JobParameters(params);
		JobExecution jobExecution = new JobExecution(101L, jobParameters);
		
		FlowExecutionStatus flowExecutionStatus = sbmFlowDecider.decide(jobExecution, null);
		
		Assert.assertNotNull("flowExecutionStatus returned is cannot be null", flowExecutionStatus);
		Assert.assertEquals("flowExecutionStatus returned is not SBMI", FLOWEXECUTIONSTATUS_SBMI, flowExecutionStatus.getName()); 
	}
	
}
