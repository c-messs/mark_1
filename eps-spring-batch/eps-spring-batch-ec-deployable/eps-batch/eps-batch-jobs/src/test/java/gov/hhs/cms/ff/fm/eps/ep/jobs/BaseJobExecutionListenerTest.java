/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import gov.hhs.cms.ff.fm.eps.rap.dao.BatchProcessDAO;
import gov.hhs.cms.ff.fm.eps.rap.domain.BatchProcessLog;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.item.ExecutionContext;

import com.accenture.foundation.common.exception.ApplicationException;
import com.accenture.foundation.common.exception.EnvironmentException;

/**
 * Test class for BaseJobExecutionListener
 * 
 * @author girish.padmanabhan
 * 
 */
@RunWith(JUnit4.class)
public class BaseJobExecutionListenerTest extends TestCase {

	private BaseJobExecutionListener epsJobListener;
	private BatchProcessDAO mockBatchProcessDAO;
	
	@Before
	public void setup() {
	
		epsJobListener = new BaseJobExecutionListener();

		mockBatchProcessDAO = createMock(BatchProcessDAO.class);
		epsJobListener.setBatchProcessDAO(mockBatchProcessDAO);
		
	}

	/**
	 * This method tests the beforeJob API of the listener.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testBeforeJob_success() throws Exception {

		expect(mockBatchProcessDAO.getJobInstanceForBatchProcess(EasyMock.anyString()))
		.andReturn(null).anyTimes();
		expect(mockBatchProcessDAO.getNextBatchBusinessIdSeq(EasyMock.anyString()))
		.andReturn(-1).anyTimes();
		mockBatchProcessDAO.insertBatchProcessLog(EasyMock.anyObject(BatchProcessLog.class));
		expectLastCall();
		
		replay(mockBatchProcessDAO);
		
		JobInstance jobInst = new JobInstance(9999L,"enrollmentProcessingJob");
		JobExecution jobEx = new JobExecution(jobInst, null);
		jobEx.setStartTime(DateTime.now().toDate());
		
		epsJobListener.beforeJob(jobEx);

		assertNotNull("batchBusinessId", jobEx.getExecutionContext().getString("batchBusinessId"));
	}

	/**
	 * This method tests the beforeJob API of the listener when the job is already running.
	 * 
	 * @throws Exception
	 */
	@Test//(expected=com.accenture.foundation.common.exception.ApplicationException.class)
	public void testBeforeJobWhenJobAlreadyRunning() throws Exception {
		List<String> blockConcurrentJobExecutionList = new ArrayList<String>();
		blockConcurrentJobExecutionList.add("epsJob");
		epsJobListener.setBlockConcurrentJobExecutionList(blockConcurrentJobExecutionList);
		
		List<BatchProcessLog> runningJobs = new ArrayList<BatchProcessLog>();
		runningJobs.add(new BatchProcessLog());
		
		expect(mockBatchProcessDAO.getJobInstanceForBatchProcess(EasyMock.anyString()))
		.andReturn("epsJob");
		replay(mockBatchProcessDAO);
		
		JobInstance jobInst = new JobInstance(9999L,"epsJob");
		JobExecution jobEx = new JobExecution(jobInst, null);
		jobEx.setStartTime(DateTime.now().toDate());
		
		try {
			epsJobListener.beforeJob(jobEx);
		} catch(ApplicationException appEx) {
			assertTrue("ApplicationException thrown", true);
		}

		assertNotNull("Application Exception expected via annotation", jobEx);
	}
	
	/**
	 * This method tests the beforeJob API of the listener when there are dependent jobs already running.
	 * 
	 * @throws Exception
	 */
	@Test//(expected=com.accenture.foundation.common.exception.ApplicationException.class)
	public void testBeforeJobWhenJobsAlreadyRunning() throws Exception {
		List<String> blockConcurrentJobExecutionList = new ArrayList<String>();
		blockConcurrentJobExecutionList.add("invoicesJob");
		epsJobListener.setBlockConcurrentJobExecutionList(blockConcurrentJobExecutionList);
		
		List<BatchProcessLog> runningJobs = new ArrayList<BatchProcessLog>();
		runningJobs.add(new BatchProcessLog());
		
		expect(mockBatchProcessDAO.getJobInstanceForBatchProcess(EasyMock.anyString()))
		.andReturn(null).anyTimes();
		expect(mockBatchProcessDAO.getJobsWithStartedStatus(EasyMock.anyString()))
		.andReturn(runningJobs);
		replay(mockBatchProcessDAO);
		
		JobInstance jobInst = new JobInstance(9999L,"epsJob");
		JobExecution jobEx = new JobExecution(jobInst, null);
		jobEx.setStartTime(DateTime.now().toDate());
		
		try {
			epsJobListener.beforeJob(jobEx);
		} catch(ApplicationException appEx) {
			assertTrue("ApplicationException thrown", true);
		}

		assertNotNull("Application Exception expected via annotation", jobEx);
	}
	
	/**
	 * This method tests the beforeJob API of the listener when the dependent jobs are not running.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testBeforeJobWhenJobsNotAlreadyRunning() throws Exception {
		List<String> blockConcurrentJobExecutionList = new ArrayList<String>();
		blockConcurrentJobExecutionList.add("invoicesJob");
		epsJobListener.setBlockConcurrentJobExecutionList(blockConcurrentJobExecutionList);
		
		expect(mockBatchProcessDAO.getJobInstanceForBatchProcess(EasyMock.anyString()))
		.andReturn(null).anyTimes();
		expect(mockBatchProcessDAO.getJobsWithStartedStatus(EasyMock.anyString()))
		.andReturn(null);
		expect(mockBatchProcessDAO.getNextBatchBusinessIdSeq(EasyMock.anyString()))
		.andReturn(-1).anyTimes();
		mockBatchProcessDAO.insertBatchProcessLog(EasyMock.anyObject(BatchProcessLog.class));
		expectLastCall();
		replay(mockBatchProcessDAO);
		
		JobInstance jobInst = new JobInstance(9999L,"epsJob");
		JobExecution jobEx = new JobExecution(jobInst, null);
		jobEx.setStartTime(DateTime.now().toDate());
		epsJobListener.beforeJob(jobEx);

		assertNotNull("batchBusinessId", jobEx.getExecutionContext().getString("batchBusinessId"));
	}
	
	/**
	 * This method tests the beforeJob API of the listener when SQL Exception happens.
	 * 
	 * @throws Exception
	 */
	@Test//(expected=com.accenture.foundation.common.exception.EnvironmentException.class)
	public void testBeforeJobSQLException() throws Exception {
		List<String> blockConcurrentJobExecutionList = new ArrayList<String>();
		blockConcurrentJobExecutionList.add("invoicesJob");
		epsJobListener.setBlockConcurrentJobExecutionList(blockConcurrentJobExecutionList);
		
		List<BatchProcessLog> runningJobs = new ArrayList<BatchProcessLog>();
		runningJobs.add(new BatchProcessLog());
		
		expect(mockBatchProcessDAO.getJobInstanceForBatchProcess(EasyMock.anyString()))
		.andThrow(new SQLException());
		replay(mockBatchProcessDAO);
		
		JobInstance jobInst = new JobInstance(9999L,"epsJob");
		JobExecution jobEx = new JobExecution(jobInst, null);
		jobEx.setStartTime(DateTime.now().toDate());
		
		try {
			epsJobListener.beforeJob(jobEx);
		} catch(EnvironmentException appEx) {
			assertTrue("EnvironmentException thrown", true);
		}
		
		assertNotNull("EnvironmentException expected via annotation", jobEx);
	}
	
	/**
	 * This method tests the beforeJob API of the listener when SQL Exception happens in BatchProcessLog insert.
	 * 
	 * @throws Exception
	 */
	@Test//(expected=com.accenture.foundation.common.exception.EnvironmentException.class)
	public void testBeforeJob_SQLException_BatchProcessLog_Insert() throws Exception {

		expect(mockBatchProcessDAO.getJobInstanceForBatchProcess(EasyMock.anyString()))
		.andReturn(null).anyTimes();
		expect(mockBatchProcessDAO.getNextBatchBusinessIdSeq(EasyMock.anyString()))
		.andReturn(-1).anyTimes();
		mockBatchProcessDAO.insertBatchProcessLog(EasyMock.anyObject(BatchProcessLog.class));
		expectLastCall().andThrow(new SQLException());
		
		replay(mockBatchProcessDAO);
		
		JobInstance jobInst = new JobInstance(9999L,"epsJob");
		JobExecution jobEx = new JobExecution(jobInst, null);
		jobEx.setStartTime(DateTime.now().toDate());
		
		try {
			epsJobListener.beforeJob(jobEx);
		} catch(EnvironmentException appEx) {
			assertTrue("EnvironmentException thrown", true);
		}

		assertNotNull("EnvironmentException expected via annotation", jobEx);
	}
	
	/**
	 * This method tests the beforeJob API of the listener when Batch Business Id sequence exceeds max for the date.
	 * 
	 * @throws Exception
	 */
	@Test//(expected=com.accenture.foundation.common.exception.ApplicationException.class)
	public void testBeforeJob_Exception_BatchBusinessIdMax() throws Exception {

		expect(mockBatchProcessDAO.getJobInstanceForBatchProcess(EasyMock.anyString()))
		.andReturn(null).anyTimes();
		expect(mockBatchProcessDAO.getNextBatchBusinessIdSeq(EasyMock.anyString()))
		.andReturn(1000).anyTimes();
		
		replay(mockBatchProcessDAO);
		
		JobInstance jobInst = new JobInstance(9999L,"epsJob");
		JobExecution jobEx = new JobExecution(jobInst, null);
		jobEx.setStartTime(DateTime.now().toDate());
		
		try {
			epsJobListener.beforeJob(jobEx);
		} catch(ApplicationException appEx) {
			assertTrue("ApplicationException thrown", true);
		}
		
		assertNotNull("ApplicationException expected via annotation", jobEx);
	}
	
	/**
	 * This method tests the afterJob API of the listener.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testAfterJob_success() throws Exception {

		mockBatchProcessDAO.updateBatchProcessLog(EasyMock.anyObject(BatchProcessLog.class));
		expectLastCall();
		replay(mockBatchProcessDAO);
		
		JobInstance jobInst = new JobInstance(9999L,"epsJob");
		JobExecution jobEx = new JobExecution(jobInst, null);
		ExecutionContext ctx = new ExecutionContext();
		ctx.putString("batchBusinessId", "batchBusinessId");
		jobEx.setExecutionContext(ctx);
		jobEx.setStartTime(DateTime.now().toDate());
		jobEx.setEndTime(DateTime.now().plusHours(1).toDate());
		
		epsJobListener.afterJob(jobEx);

		assertNotNull("Method completed. Checking ExecutionContext", jobEx.getExecutionContext().getString("batchBusinessId"));
	}
	
	/**
	 * This method tests the afterJob API of the listener.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testAfterJobWithErrorMessage() throws Exception {

		mockBatchProcessDAO.updateBatchProcessLog(EasyMock.anyObject(BatchProcessLog.class));
		expectLastCall();
		replay(mockBatchProcessDAO);
		
		JobInstance jobInst = new JobInstance(9999L,"epsJob");
		JobExecution jobEx = new JobExecution(jobInst, null);
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
	 * This method tests the afterJob API of the listener.
	 * 
	 * @throws Exception
	 */
	@Test//(expected=com.accenture.foundation.common.exception.ApplicationException.class)
	public void testAfterJobWithException() throws Exception {

		mockBatchProcessDAO.updateBatchProcessLog(EasyMock.anyObject(BatchProcessLog.class));
		expectLastCall().andThrow(new SQLException());
		replay(mockBatchProcessDAO);
		
		JobInstance jobInst = new JobInstance(9999L,"epsJob");
		JobExecution jobEx = new JobExecution(jobInst, null);
		ExecutionContext ctx = new ExecutionContext();
		ctx.putString("batchBusinessId", "batchBusinessId");
		ctx.putString("ErrorMesssage", "Error occured");
		jobEx.setExecutionContext(ctx);
		jobEx.setStartTime(DateTime.now().toDate());
		jobEx.setEndTime(DateTime.now().plusHours(1).toDate());
		
		try {
			epsJobListener.afterJob(jobEx);
		} catch(ApplicationException appEx) {
			assertTrue("ApplicationException thrown", true);
		}
		
		assertNotNull("Method completed. Checking ExecutionContext", jobEx.getExecutionContext().getString("batchBusinessId"));
	}

}
