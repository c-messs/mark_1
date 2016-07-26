/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.retroactivepaymentsjob;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import gov.hhs.cms.ff.fm.eps.rap.dao.BatchProcessDAO;
import gov.hhs.cms.ff.fm.eps.rap.domain.BatchProcessLog;
import gov.hhs.cms.ff.fm.eps.rap.domain.RapConstants;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Test class for RAPJobExecutionListener
 * 
 * @author girish.padmanabhan
 * 
 */
@RunWith(JUnit4.class)
public class RAPJobExecutionListenerTest extends TestCase {

	private RapJobExecutionListener rapJobExecutionListener;
	private BatchProcessDAO mockBatchProcessDAO;
	private JdbcTemplate mockJdbcTemplate;
	
	@Before
	public void setup() {
	
		rapJobExecutionListener = new RapJobExecutionListener();

		mockBatchProcessDAO = EasyMock.createMock(BatchProcessDAO.class);
		rapJobExecutionListener.setBatchProcessDAO(mockBatchProcessDAO);
		
		mockJdbcTemplate = EasyMock.createMock(JdbcTemplate.class);
		rapJobExecutionListener.setJdbcTemplate(mockJdbcTemplate);
	}

	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.retroactivepaymentsjob.RapJobExecutionListener#beforeJob()}
	 * This method tests the beforeJob API of the listener.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRAPBeforeJob_success() throws Exception {

		expect(mockBatchProcessDAO.getJobInstanceForBatchProcess(EasyMock.anyString()))
		.andReturn(null).anyTimes();
		expect(mockBatchProcessDAO.getNextBatchBusinessIdSeq(EasyMock.anyString()))
		.andReturn(-1).anyTimes();

		mockBatchProcessDAO.insertBatchProcessLog(EasyMock.anyObject(BatchProcessLog.class));
		expectLastCall();
		
		replay(mockBatchProcessDAO);
		
		JobInstance jobInst = new JobInstance(9999L,"retroActivePaymentsJob");
		
		final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
	    params.put("test", new JobParameter(RapConstants.JOBPARAMETER_TYPE_RAP));
	    JobParameters jobParameters = new JobParameters(params);
	    
		JobExecution jobEx = new JobExecution(jobInst, jobParameters);
		jobEx.setStartTime(DateTime.now().toDate());
		
		rapJobExecutionListener.beforeJob(jobEx);

		assertNotNull("batchBusinessId", jobEx.getExecutionContext().getString("batchBusinessId"));
		assertTrue("Batch Business ID will be of format RAPRETRO%", jobEx.getExecutionContext().getString("batchBusinessId").startsWith("RAPRETRO"));
	}
	
	
	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.retroactivepaymentsjob.RapJobExecutionListener#beforeJob()}
	 * This method tests the beforeJob API of the listener.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRAPBeforeJobTypeNotSet_success() throws Exception {

		expect(mockBatchProcessDAO.getJobInstanceForBatchProcess(EasyMock.anyString()))
		.andReturn(null).anyTimes();
		expect(mockBatchProcessDAO.getNextBatchBusinessIdSeq(EasyMock.anyString()))
		.andReturn(-1).anyTimes();
		
		mockBatchProcessDAO.insertBatchProcessLog(EasyMock.anyObject(BatchProcessLog.class));
		expectLastCall();
		replay(mockBatchProcessDAO);

		JobInstance jobInst = new JobInstance(9999L,"retroActivePaymentsJob");
		
		final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
	    JobParameters jobParameters = new JobParameters(params);
	    
		JobExecution jobEx = new JobExecution(jobInst, jobParameters);
		jobEx.setStartTime(DateTime.now().toDate());
		
		rapJobExecutionListener.beforeJob(jobEx);
		
		assertNotNull("batchBusinessId", jobEx.getExecutionContext().getString("batchBusinessId"));
		assertTrue("Batch Business ID will be of format RAPRETRO%", jobEx.getExecutionContext().getString("batchBusinessId").startsWith("RAPRETRO"));
	}
	
	
	@SuppressWarnings("unchecked")
	@Test
	public void testRAPStageBeforeJob_success() throws Exception {

		expect(mockBatchProcessDAO.getJobInstanceForBatchProcess(EasyMock.anyString()))
		.andReturn(null).anyTimes();
		expect(mockBatchProcessDAO.getNextBatchBusinessIdSeq(EasyMock.anyString()))
		.andReturn(-1).anyTimes();

		
		
		EasyMock.expect(mockJdbcTemplate.update(EasyMock.anyString())).andReturn(1);
		EasyMock.expect(mockJdbcTemplate.queryForObject(
				EasyMock.anyString(), EasyMock.isA(Timestamp.class.getClass()))).andReturn(new Timestamp(System.currentTimeMillis()));

		replay(mockJdbcTemplate);
		
		
		mockBatchProcessDAO.insertBatchProcessLog(EasyMock.anyObject(BatchProcessLog.class));
		expectLastCall();
		
		replay(mockBatchProcessDAO);
		
		JobInstance jobInst = new JobInstance(9999L,"retroActivePaymentsJob");
		
		final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
	    params.put("test", new JobParameter(RapConstants.JOBPARAMETER_TYPE_RAPSTAGE));
	    JobParameters jobParameters = new JobParameters(params);
	    
		JobExecution jobEx = new JobExecution(jobInst, jobParameters);
		
		jobEx.setStartTime(DateTime.now().toDate());
		
		rapJobExecutionListener.setUpdatePendingRecordSql("UPDATE SOME DATA");
		rapJobExecutionListener.setSelectMaxPolicyMaintStartDateTime("SELECT SOME DATA");
	
		rapJobExecutionListener.beforeJob(jobEx);

		assertNotNull("batchBusinessId", jobEx.getExecutionContext().getString("batchBusinessId"));
		assertTrue("Batch Business ID will be of format RAPSTAGE%", jobEx.getExecutionContext().getString("batchBusinessId").startsWith("RAPRETRO"));
	}

	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.retroactivepaymentsjob.RapJobExecutionListener#beforeJob()}
	 * This method tests the beforeJob API of the listener when RAP job is already running.
	 * 
	 * @throws Exception
	 */
	@Test(expected=com.accenture.foundation.common.exception.ApplicationException.class)
	public void testStageJobBeforeJobWhenRapJobAlreadyRunning() throws Exception {
		List<String> blockConcurrentJobExecutionList = new ArrayList<String>();
		blockConcurrentJobExecutionList.add("aptcCsrUfRollupJob,aptcCsrUfIssuerTransitionJob");
		
		Map<String,List<String>> blockConcurrentJobExecutionMap = new HashMap<String,List<String>>();
		blockConcurrentJobExecutionMap.put("RAP", blockConcurrentJobExecutionList);
		
		rapJobExecutionListener.setBlockConcurrentJobExecutionMap(blockConcurrentJobExecutionMap);
		
		List<BatchProcessLog> runningJobs = new ArrayList<BatchProcessLog>();
		runningJobs.add(new BatchProcessLog());
		
		expect(mockBatchProcessDAO.getJobInstanceForBatchProcess(EasyMock.anyString()))
		.andReturn("retroActivePaymentsJob");
		replay(mockBatchProcessDAO);
		
		final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
	    params.put("type", new JobParameter("RAPSTAGE"));
	    JobParameters jobParameters = new JobParameters(params);
		
		JobInstance jobInst = new JobInstance(9999L,"retroActivePaymentsJob");
		JobExecution jobEx = new JobExecution(jobInst, jobParameters);
		jobEx.setStartTime(DateTime.now().toDate());
		rapJobExecutionListener.beforeJob(jobEx);

		assertNotNull("Application Exception expected via annotation", jobEx);
	}
	
	
	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.retroactivepaymentsjob.RapJobExecutionListener#beforeJob()}
	 * This method tests the beforeJob API of the listener when RAP job is already running.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRapBeforeJobWhenRapJobAlreadyRunning() throws Exception {
		
		
		List<String> blockConcurrentJobExecutionList = new ArrayList<String>();
		blockConcurrentJobExecutionList.add("aptcCsrUfRollupJob,aptcCsrUfIssuerTransitionJob");

		
		Map<String,List<String>> blockConcurrentJobExecutionMap = new HashMap<String,List<String>>();
		blockConcurrentJobExecutionMap.put("RAP", blockConcurrentJobExecutionList);
		
		rapJobExecutionListener.setBlockConcurrentJobExecutionMap(blockConcurrentJobExecutionMap);
		
		List<BatchProcessLog> runningJobs = new ArrayList<BatchProcessLog>();
		//runningJobs.add(new BatchProcessLog());

		expect(mockBatchProcessDAO.getNextBatchBusinessIdSeq(EasyMock.anyString()))
		.andReturn(-1).anyTimes();

		expect(mockBatchProcessDAO.getJobInstanceForBatchProcess(EasyMock.anyString()))
		.andReturn("retroActivePaymentsJob");
		
		expect(mockBatchProcessDAO.getJobsWithStartedStatus(EasyMock.anyString()))
		.andReturn(runningJobs);
		
		mockBatchProcessDAO.insertBatchProcessLog(EasyMock.anyObject(BatchProcessLog.class));
		
		expectLastCall();
		replay(mockBatchProcessDAO);
		
		
		final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
	    params.put("type", new JobParameter("RAP"));
	    JobParameters jobParameters = new JobParameters(params);
		
		JobInstance jobInst = new JobInstance(9999L,"retroActivePaymentsJob");
		JobExecution jobEx = new JobExecution(jobInst, jobParameters);
		jobEx.setStartTime(DateTime.now().toDate());
		rapJobExecutionListener.beforeJob(jobEx);

		assertNotNull("JobExecution cannot be null", jobEx);
	}
	
	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.retroactivepaymentsjob.RapJobExecutionListener#beforeJob()}
	 * This method tests the beforeJob API of the listener when there are dependent jobs already running.
	 * 
	 * @throws Exception
	 */
	@Test(expected=com.accenture.foundation.common.exception.ApplicationException.class)
	public void testBeforeJobWhenJobsAlreadyRunning() throws Exception {
		List<String> blockConcurrentJobExecutionList = new ArrayList<String>();
		blockConcurrentJobExecutionList.add("aptcCsrUfRollupJob,aptcCsrUfIssuerTransitionJob,retroActivePaymentsJob");
		rapJobExecutionListener.setBlockConcurrentJobExecutionList(blockConcurrentJobExecutionList);
		
		List<BatchProcessLog> runningJobs = new ArrayList<BatchProcessLog>();
		runningJobs.add(new BatchProcessLog());
		
		expect(mockBatchProcessDAO.getJobInstanceForBatchProcess(EasyMock.anyString()))
		.andReturn(null).anyTimes();
		expect(mockBatchProcessDAO.getJobsWithStartedStatus(EasyMock.anyString()))
		.andReturn(runningJobs);
		replay(mockBatchProcessDAO);
		
		JobInstance jobInst = new JobInstance(9999L,"retroActivePaymentsJob");
		JobExecution jobEx = new JobExecution(jobInst, null);
		jobEx.setStartTime(DateTime.now().toDate());
		rapJobExecutionListener.beforeJob(jobEx);

		assertNotNull("Application Exception expected via annotation", jobEx);
	}
	
	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.retroactivepaymentsjob.RapJobExecutionListener#beforeJob()}
	 * This method tests the beforeJob API of the listener when there are dependent jobs not already running.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRapBeforeJobWhenJobsNotAlreadyRunning() throws Exception {
		List<String> blockConcurrentJobExecutionList = new ArrayList<String>();
		blockConcurrentJobExecutionList.add("aptcCsrUfRollupJob,aptcCsrUfIssuerTransitionJob");
		rapJobExecutionListener.setBlockConcurrentJobExecutionList(blockConcurrentJobExecutionList);
		
		expect(mockBatchProcessDAO.getJobInstanceForBatchProcess(EasyMock.anyString()))
		.andReturn(null).anyTimes();
		expect(mockBatchProcessDAO.getJobsWithStartedStatus(EasyMock.anyString()))
		.andReturn(null);
		expect(mockBatchProcessDAO.getNextBatchBusinessIdSeq(EasyMock.anyString()))
		.andReturn(-1).anyTimes();
		mockBatchProcessDAO.insertBatchProcessLog(EasyMock.anyObject(BatchProcessLog.class));
		expectLastCall();
		replay(mockBatchProcessDAO);
		
		JobInstance jobInst = new JobInstance(9999L,"retroActivePaymentsJob");
		JobExecution jobEx = new JobExecution(jobInst, null);
		jobEx.setStartTime(DateTime.now().toDate());
		rapJobExecutionListener.beforeJob(jobEx);

		assertNotNull("batchBusinessId", jobEx.getExecutionContext().getString("batchBusinessId"));
	}
	
	
	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.retroactivepaymentsjob.RapJobExecutionListener#beforeJob()}
	 * This method tests the beforeJob API of the listener when there are dependent jobs not already running.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRapStageBeforeJobWhenJobsNotAlreadyRunning() throws Exception {
		List<String> blockConcurrentJobExecutionList = new ArrayList<String>();
		blockConcurrentJobExecutionList.add("aptcCsrUfRollupJob,aptcCsrUfIssuerTransitionJob");
		rapJobExecutionListener.setBlockConcurrentJobExecutionList(blockConcurrentJobExecutionList);
		
		expect(mockBatchProcessDAO.getJobInstanceForBatchProcess(EasyMock.anyString()))
		.andReturn(null).anyTimes();
		expect(mockBatchProcessDAO.getJobsWithStartedStatus(EasyMock.anyString()))
		.andReturn(null);
		expect(mockBatchProcessDAO.getNextBatchBusinessIdSeq(EasyMock.anyString()))
		.andReturn(-1).anyTimes();
		mockBatchProcessDAO.insertBatchProcessLog(EasyMock.anyObject(BatchProcessLog.class));
		expectLastCall();
		replay(mockBatchProcessDAO);
		
		final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
	    params.put("type", new JobParameter("RAP"));
	    JobParameters jobParameters = new JobParameters(params);
		
		JobInstance jobInst = new JobInstance(9999L,"retroActivePaymentsJob");
		JobExecution jobEx = new JobExecution(jobInst, jobParameters);
		jobEx.setStartTime(DateTime.now().toDate());
		rapJobExecutionListener.beforeJob(jobEx);

		assertNotNull("batchBusinessId", jobEx.getExecutionContext().getString("batchBusinessId"));
	}
	
	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.retroactivepaymentsjob.RapJobExecutionListener#beforeJob()}
	 * This method tests the beforeJob API of the listener when SQL Exception happens in BatchProcessLog insert.
	 * 
	 * @throws Exception
	 */
	@Test(expected=com.accenture.foundation.common.exception.EnvironmentException.class)
	public void testRapStageBeforeJob_SQLException_BatchProcessLog_Insert() throws Exception {

		
		EasyMock.expect(mockJdbcTemplate.update(EasyMock.anyString())).andReturn(1);
		EasyMock.expect(mockJdbcTemplate.queryForObject(
				EasyMock.anyString(), EasyMock.isA(Timestamp.class.getClass()))).andReturn(new Timestamp(System.currentTimeMillis()));

		replay(mockJdbcTemplate);
		
		expect(mockBatchProcessDAO.getJobInstanceForBatchProcess(EasyMock.anyString()))
		.andReturn(null).anyTimes();
		expect(mockBatchProcessDAO.getNextBatchBusinessIdSeq(EasyMock.anyString()))
		.andReturn(-1).anyTimes();
		mockBatchProcessDAO.insertBatchProcessLog(EasyMock.anyObject(BatchProcessLog.class));
		expectLastCall().andThrow(new SQLException());
		
		replay(mockBatchProcessDAO);
		

		
		final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
	    params.put("type", new JobParameter("RAPSTAGE"));
	    JobParameters jobParameters = new JobParameters(params);
		
		
		JobInstance jobInst = new JobInstance(9999L,"retroActivePaymentsJob");
		JobExecution jobEx = new JobExecution(jobInst, jobParameters);
		jobEx.setStartTime(DateTime.now().toDate());
		
		rapJobExecutionListener.beforeJob(jobEx);

		assertNotNull("EnvironmentException expected via annotation", jobEx);
	}
	
	
	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.retroactivepaymentsjob.RapJobExecutionListener#beforeJob()}
	 * This method tests the beforeJob API of the listener when SQL Exception happens in BatchProcessLog insert.
	 * 
	 * @throws Exception
	 */
	@Test(expected=com.accenture.foundation.common.exception.EnvironmentException.class)
	public void testRapBeforeJob_SQLException_BatchProcessLog_Insert() throws Exception {
	
		expect(mockBatchProcessDAO.getJobInstanceForBatchProcess(EasyMock.anyString()))
		.andReturn(null).anyTimes();
		expect(mockBatchProcessDAO.getNextBatchBusinessIdSeq(EasyMock.anyString()))
		.andReturn(-1).anyTimes();
		mockBatchProcessDAO.insertBatchProcessLog(EasyMock.anyObject(BatchProcessLog.class));
		expectLastCall().andThrow(new SQLException());
		
		replay(mockBatchProcessDAO);
		

		
		final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
	    params.put("type", new JobParameter("RAP"));
	    JobParameters jobParameters = new JobParameters(params);
		
		
		JobInstance jobInst = new JobInstance(9999L,"retroActivePaymentsJob");
		JobExecution jobEx = new JobExecution(jobInst, jobParameters);
		jobEx.setStartTime(DateTime.now().toDate());
		
		rapJobExecutionListener.beforeJob(jobEx);

		assertNotNull("EnvironmentException expected via annotation", jobEx);
	}
	
	
	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.retroactivepaymentsjob.RapJobExecutionListener#beforeJob()}
	 * This method tests the beforeJob API of the listener when SQL Exception happens in BatchProcessLog insert.
	 * 
	 * @throws Exception
	 */
	@Test(expected=com.accenture.foundation.common.exception.EnvironmentException.class)
	public void testRapStageBeforeJob_SQLException_checkJobInstanceForBatchProcess() throws Exception {

		expect(mockBatchProcessDAO.getNextBatchBusinessIdSeq(EasyMock.anyString()))
		.andReturn(-1).anyTimes();
		mockBatchProcessDAO.getJobInstanceForBatchProcess(EasyMock.anyString());
		expectLastCall().andThrow(new SQLException());
		
		replay(mockBatchProcessDAO);
		
		final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
	    params.put("type", new JobParameter("RAPSTAGE"));
	    JobParameters jobParameters = new JobParameters(params);
		
		
		JobInstance jobInst = new JobInstance(9999L,"retroActivePaymentsJob");
		JobExecution jobEx = new JobExecution(jobInst, jobParameters);
		jobEx.setStartTime(DateTime.now().toDate());
		
		rapJobExecutionListener.beforeJob(jobEx);

		assertNotNull("EnvironmentException expected via annotation", jobEx);
	}
	
	
	
	/**
	 * 
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.retroactivepaymentsjob.RapJobExecutionListener#beforeJob()}
	 * This method tests the beforeJob API of the listener when Batch Business Id sequence exceeds max for the date.
	 * 
	 * @throws Exception
	 */
	@Test(expected=com.accenture.foundation.common.exception.ApplicationException.class)
	public void testBeforeJob_Exception_BatchBusinessIdMax() throws Exception {

		expect(mockBatchProcessDAO.getJobInstanceForBatchProcess(EasyMock.anyString()))
		.andReturn(null).anyTimes();
		expect(mockBatchProcessDAO.getNextBatchBusinessIdSeq(EasyMock.anyString()))
		.andReturn(1000).anyTimes();
		
		replay(mockBatchProcessDAO);
		
		JobInstance jobInst = new JobInstance(9999L,"retroActivePaymentsJob");
		JobExecution jobEx = new JobExecution(jobInst, null);
		jobEx.setStartTime(DateTime.now().toDate());
		
		rapJobExecutionListener.beforeJob(jobEx);

		assertNotNull("ApplicationException expected via annotation", jobEx);
	}
	
	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.retroactivepaymentsjob.RapJobExecutionListener#afterJob()}
	 * This method tests the afterJob API of the listener.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRapAfterJob_success() throws Exception {

		mockBatchProcessDAO.updateBatchProcessLog(EasyMock.anyObject(BatchProcessLog.class));
		expectLastCall();
		replay(mockBatchProcessDAO);
		
		JobInstance jobInst = new JobInstance(9999L,"retroActivePaymentsJob");
		JobExecution jobEx = new JobExecution(jobInst, null);
		ExecutionContext ctx = new ExecutionContext();
		ctx.putString("batchBusinessId", "batchBusinessId");
		ctx.put("LASTPVD", DateTime.now());
		jobEx.setExecutionContext(ctx);
		jobEx.setStartTime(DateTime.now().toDate());
		jobEx.setEndTime(DateTime.now().plusHours(1).toDate());
		
		rapJobExecutionListener.afterJob(jobEx);

		assertNotNull("Method completed. Checking ExecutionContext", jobEx.getExecutionContext().getString("batchBusinessId"));
	}
	
	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.retroactivepaymentsjob.RapJobExecutionListener#afterJob()}
	 * This method tests the afterJob API of the listener.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testStageAfterJob_success() throws Exception {

		mockBatchProcessDAO.updateBatchProcessLog(EasyMock.anyObject(BatchProcessLog.class));
		expectLastCall();
		replay(mockBatchProcessDAO);
		
		final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
	    params.put("type", new JobParameter(RapConstants.JOBPARAMETER_TYPE_RAPSTAGE));
	    JobParameters jobParameters = new JobParameters(params);
		
		JobInstance jobInst = new JobInstance(9999L,"retroActivePaymentsJob");
		JobExecution jobEx = new JobExecution(jobInst, jobParameters);
		ExecutionContext ctx = new ExecutionContext();
		ctx.putString("batchBusinessId", "batchBusinessId");
		ctx.put("LASTPVD", DateTime.now());
		jobEx.setExecutionContext(ctx);
		jobEx.setStartTime(DateTime.now().toDate());
		jobEx.setEndTime(DateTime.now().plusHours(1).toDate());
		
		rapJobExecutionListener.afterJob(jobEx);

		assertNotNull("Method completed. Checking ExecutionContext", jobEx.getExecutionContext().getString("batchBusinessId"));
	}
	
	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.retroactivepaymentsjob.RapJobExecutionListener#afterJob()}
	 * This method tests the afterJob API of the listener.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testAfterJobWithErrorMessage() throws Exception {

		mockBatchProcessDAO.updateBatchProcessLog(EasyMock.anyObject(BatchProcessLog.class));
		expectLastCall();
		replay(mockBatchProcessDAO);
		
		JobInstance jobInst = new JobInstance(9999L,"retroActivePaymentsJob");
		JobExecution jobEx = new JobExecution(jobInst, null);
		ExecutionContext ctx = new ExecutionContext();
		ctx.putString("batchBusinessId", "batchBusinessId");
		ctx.putString("ErrorMesssage", "Error occured");
		jobEx.setExecutionContext(ctx);
		jobEx.setStartTime(DateTime.now().toDate());
		jobEx.setEndTime(DateTime.now().plusHours(1).toDate());
		
		rapJobExecutionListener.afterJob(jobEx);

		assertNotNull("Method completed. Checking ExecutionContext", jobEx.getExecutionContext().getString("batchBusinessId"));
	}
	
	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.retroactivepaymentsjob.RapJobExecutionListener#afterJob()}
	 * This method tests the afterJob API of the listener.
	 * 
	 * @throws Exception
	 */
	@Test(expected=com.accenture.foundation.common.exception.ApplicationException.class)
	public void testAfterJobWithException() throws Exception {

		mockBatchProcessDAO.updateBatchProcessLog(EasyMock.anyObject(BatchProcessLog.class));
		expectLastCall().andThrow(new SQLException());
		replay(mockBatchProcessDAO);
		
		expect(mockJdbcTemplate.update(EasyMock.anyString(), EasyMock.anyLong())).andReturn(0);
		replay(mockJdbcTemplate);
		
		JobInstance jobInst = new JobInstance(9999L,"retroActivePaymentsJob");
		JobExecution jobEx = new JobExecution(jobInst, null);
		ExecutionContext ctx = new ExecutionContext();
		ctx.putString("batchBusinessId", "batchBusinessId");
		ctx.putString("ErrorMesssage", "Error occured");
		jobEx.setExecutionContext(ctx);
		jobEx.setStartTime(DateTime.now().toDate());
		jobEx.setEndTime(DateTime.now().plusHours(1).toDate());
		jobEx.setStatus(BatchStatus.COMPLETED);
		
		
		rapJobExecutionListener.afterJob(jobEx);

		assertNotNull("Method completed. Checking ExecutionContext", jobEx.getExecutionContext().getString("batchBusinessId"));
	}
	
	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.retroactivepaymentsjob.RapJobExecutionListener#getBatchBusinessIdPrefix()}
	 * This method tests the getBatchBusinessIdPrefix() API of the listener.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGetBatchBusinessIdPrefixNull() {
		ReflectionTestUtils.setField(rapJobExecutionListener, "jobName", "AnyOtherJob");
		String jobPrefix = ReflectionTestUtils.invokeMethod(rapJobExecutionListener, "getBatchBusinessIdPrefix");
		
		assertNull("jobPrefix is null", jobPrefix);
	}
	
	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.retroactivepaymentsjob.RapJobExecutionListener#getBatchBusinessIdPrefix()}
	 * This method tests the getBatchBusinessIdPrefix() API of the listener.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGetBatchBusinessIdPrefixInvalidJobType() {
		ReflectionTestUtils.setField(rapJobExecutionListener, "jobName", "retroActivePaymentsJob");
		ReflectionTestUtils.setField(rapJobExecutionListener, "type", "AnotherJobType");
		String jobPrefix = ReflectionTestUtils.invokeMethod(rapJobExecutionListener, "getBatchBusinessIdPrefix");
		
		assertNull("jobPrefix is null", jobPrefix);
	}

}
