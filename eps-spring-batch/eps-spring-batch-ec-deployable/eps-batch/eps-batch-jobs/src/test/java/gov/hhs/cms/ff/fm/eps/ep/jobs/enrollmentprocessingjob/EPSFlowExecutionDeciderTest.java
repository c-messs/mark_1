/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob;

import static gov.hhs.cms.ff.fm.eps.ep.EPSConstants.CONTINUE_INGEST;
import gov.hhs.cms.ff.fm.eps.ep.EPSConstants;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.stream.FactoryConfigurationError;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.oxm.XmlMappingException;

/**
 * Test class for EPSFlowExecutionDecider
 * 
 * @author girish.padmanabhan
 * 
 */
@RunWith(JUnit4.class)
public class EPSFlowExecutionDeciderTest extends TestCase {

	EPSFlowExecutionDecider epsFlowExecutionDecider;
	
	@Before
	public void setup() throws FactoryConfigurationError, XmlMappingException, Exception {
		epsFlowExecutionDecider = new EPSFlowExecutionDecider();
	}
	
	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.EPSFlowExecutionDecider#decide()}
	 * This method tests the flow execution decision when job parameter is "hub".
	 * 
	 */
	@Test
	public void testDecide_hub() throws Exception {
		
        final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
        params.put("source", new JobParameter("hub"));
        JobParameters jobParameters = new JobParameters(params);
		JobExecution jobExecution = new JobExecution(101L, jobParameters);
		
		FlowExecutionStatus flowStatusEnum = epsFlowExecutionDecider.decide(jobExecution, null);
		
		assertNotNull("flowStatusEnum returned is not null", flowStatusEnum);
		assertEquals("flowStatusEnum returned is HUB", "HUB", flowStatusEnum.getName()); 
	}
	
	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.EPSFlowExecutionDecider#decide()}
	 * This method tests the flow execution decision when job parameter is "hub".
	 * 
	 */
	@Test
	public void testDecide_NonFfm() throws Exception {
		
        final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
        params.put("source", new JobParameter("duuq"));
        JobParameters jobParameters = new JobParameters(params);
		JobExecution jobExecution = new JobExecution(101L, jobParameters);
		
		FlowExecutionStatus flowStatusEnum = epsFlowExecutionDecider.decide(jobExecution, null);
		
		assertNotNull("flowStatusEnum returned is not null", flowStatusEnum);
		assertEquals("flowStatusEnum returned is FFM", "FFM", flowStatusEnum.getName()); 
	}
	
	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.EPSFlowExecutionDecider#decide()}
	 * This method tests the flow execution decision when job parameter is "ffm".
	 * 
	 */
	@Test
	public void testDecide_ffm() throws Exception {
		
        final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
        params.put("source", new JobParameter("ffm"));
        params.put("jobType", new JobParameter(EPSConstants.JOB_TYPE_PROCESSOR));
        JobParameters jobParameters = new JobParameters(params);
		JobExecution jobExecution = new JobExecution(101L, jobParameters);
		
		FlowExecutionStatus flowStatusEnum = epsFlowExecutionDecider.decide(jobExecution, null);
		
		assertNotNull("flowStatusEnum returned is not null", flowStatusEnum);
		assertEquals("flowStatusEnum returned is FFM", "FFM", flowStatusEnum.getName()); 
	}
	
	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.EPSFlowExecutionDecider#decide()}
	 * This method tests the flow execution decision when job parameter is "ffm".
	 * 
	 */
	@Test
	public void testDecide_ffm_Extraction() throws Exception {
		
        final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
        params.put("source", new JobParameter("ffm"));
        JobParameters jobParameters = new JobParameters(params);
		JobExecution jobExecution = new JobExecution(101L, jobParameters);
		
		FlowExecutionStatus flowStatusEnum = epsFlowExecutionDecider.decide(jobExecution, null);
		
		assertNotNull("flowStatusEnum returned is not null", flowStatusEnum);
		assertEquals("flowStatusEnum returned is FFM_EXTRACTION", "FFM_EXTRACTION", flowStatusEnum.getName()); 
	}

	@Test
	public void testDecide_ffm_Extraction_PreAudit_Y() throws Exception {
		
        final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
        params.put("source", new JobParameter("ffm"));
        JobParameters jobParameters = new JobParameters(params);
		JobExecution jobExecution = new JobExecution(101L, jobParameters);
		jobExecution.getExecutionContext().putString(CONTINUE_INGEST, "Y");
		
		FlowExecutionStatus flowStatusEnum = epsFlowExecutionDecider.decide(jobExecution, null);
		
		assertNotNull("flowStatusEnum returned is not null", flowStatusEnum);
		assertEquals("flowStatusEnum returned is FFM_EXTRACTION", "FFM_EXTRACTION", flowStatusEnum.getName()); 
	}
	
	@Test
	public void testDecide_ffm_Extraction_PreAudit_N() throws Exception {
		
        final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
        params.put("source", new JobParameter("ffm"));
        JobParameters jobParameters = new JobParameters(params);
		JobExecution jobExecution = new JobExecution(101L, jobParameters);
		jobExecution.getExecutionContext().putString(CONTINUE_INGEST, "N");
		
		FlowExecutionStatus flowStatusEnum = epsFlowExecutionDecider.decide(jobExecution, null);
		
		assertNotNull("flowStatusEnum returned is not null", flowStatusEnum);
		assertEquals("flowStatusEnum returned is END_INGEST", "END_INGEST", flowStatusEnum.getName()); 
	}

}
