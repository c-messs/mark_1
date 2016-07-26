/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob;

import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Test class for EpsJobExecutionListener.
 *
 * @author girish.padmanabhan
 */
@RunWith(JUnit4.class)
public class EpsJobExecutionListenerTest extends TestCase {

	/** The eps job listener. */
	private EpsJobExecutionListener epsJobListener;
	
	/**
	 * Setup.
	 */
	@Before
	public void setup() {
	
		epsJobListener = new EpsJobExecutionListener();
	}

	/**
	 * This method tests the beforeJob API of the listener.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testBeforeJob_ffm() throws Exception {

        final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
        params.put("source", new JobParameter("ffm"));
        params.put("jobType", new JobParameter("Junit test"));
        JobParameters jobParameters = new JobParameters(params);
        
		JobInstance jobInst = new JobInstance(9999L,"epsJob");
		JobExecution jobEx = new JobExecution(jobInst, jobParameters);
		
		epsJobListener.beforeJob(jobEx);

		assertEquals("job source", "ffm", jobEx.getJobParameters().getString("source"));
		assertNotNull("jobType", jobEx.getJobParameters().getString("jobType"));
	}
	
	@Test
	public void testBeforeJob_non_ffm() throws Exception {

        final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
        params.put("source", new JobParameter("hub"));
        JobParameters jobParameters = new JobParameters(params);
        
		JobInstance jobInst = new JobInstance(9999L,"epsJob");
		JobExecution jobEx = new JobExecution(jobInst, jobParameters);
		
		epsJobListener.beforeJob(jobEx);

		assertEquals("job source", "hub", jobEx.getJobParameters().getString("source"));
	}

	/**
	 * This method tests the afterJob API of the listener.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testAfterJobWith_ffm() throws Exception {

        final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
        params.put("source", new JobParameter("ffm"));
        params.put("jobType", new JobParameter("Junit test"));
        JobParameters jobParameters = new JobParameters(params);
		
		JobInstance jobInst = new JobInstance(9999L,"epsJob");
		JobExecution jobEx = new JobExecution(jobInst, jobParameters);
		ExecutionContext ctx = new ExecutionContext();
		ctx.putString("batchBusinessId", "batchBusinessId");
		ctx.putString("ErrorMesssage", "Error occured");
		jobEx.setExecutionContext(ctx);
		jobEx.setStartTime(DateTime.now().toDate());
		jobEx.setEndTime(DateTime.now().plusHours(1).toDate());
		
		epsJobListener.afterJob(jobEx);

		assertNotNull("Method completed. Checking ExecutionContext", jobEx.getExecutionContext().getString("batchBusinessId"));
	}
	
	@Test
	public void testAfterJobWith_non_ffm() throws Exception {

        final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
        params.put("source", new JobParameter("ffm"));
        params.put("jobType", new JobParameter("Junit test"));
        JobParameters jobParameters = new JobParameters(params);
		
		JobInstance jobInst = new JobInstance(9999L,"epsJob");
		JobExecution jobEx = new JobExecution(jobInst, jobParameters);
		ExecutionContext ctx = new ExecutionContext();
		ctx.putString("batchBusinessId", "batchBusinessId");
		ctx.putString("ErrorMesssage", "Error occured");
		jobEx.setExecutionContext(ctx);
		jobEx.setStartTime(DateTime.now().toDate());
		jobEx.setEndTime(DateTime.now().plusHours(1).toDate());
		
		epsJobListener.afterJob(jobEx);

		assertNotNull("Method completed. Checking ExecutionContext", jobEx.getExecutionContext().getString("batchBusinessId"));
	}
	
	/**
	 * This method tests the getBatchBusinessIdPrefix() API of the listener.
	 */
	@Test
	public void testGetBatchBusinessIdPrefixDefault() {
		ReflectionTestUtils.setField(epsJobListener, "source", "ffm");
		ReflectionTestUtils.setField(epsJobListener, "jobType", "processor");
		
		String jobPrefix = ReflectionTestUtils.invokeMethod(epsJobListener, "getBatchBusinessIdPrefix");
		
		assertEquals("jobPrefix is null", "enrollmentProcessingBatchJob", jobPrefix);
	}
	
	@Test
	public void testGetBatchBusinessIdPrefix_ffm_JobType_null() {
		ReflectionTestUtils.setField(epsJobListener, "source", "ffm");
		ReflectionTestUtils.setField(epsJobListener, "jobType", null);
		
		String jobPrefix = ReflectionTestUtils.invokeMethod(epsJobListener, "getBatchBusinessIdPrefix");
		
		assertEquals("jobPrefix is null", "ERLFILEINGEST", jobPrefix);
	}
	
	@Test
	public void testGetBatchBusinessIdPrefix_non_ffm() {
		ReflectionTestUtils.setField(epsJobListener, "source", "hub");
		String jobPrefix = epsJobListener.getCodeForBatchBusinessId();
		
		assertEquals("jobPrefix is enrollmentProcessingBatchJob", "enrollmentProcessingBatchJob", jobPrefix);
	}
	
	@Test
	public void testGetBatchBusinessIdPrefix_null_source() {
		String jobPrefix = epsJobListener.getCodeForBatchBusinessId();
		
		assertEquals("jobPrefix is enrollmentProcessingBatchJob", "enrollmentProcessingBatchJob", jobPrefix);
	}

}
