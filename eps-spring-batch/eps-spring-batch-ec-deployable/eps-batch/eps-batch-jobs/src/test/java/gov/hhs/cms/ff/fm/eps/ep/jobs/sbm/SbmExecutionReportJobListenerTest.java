/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;

import java.util.LinkedHashMap;
import java.util.Map;

import org.easymock.EasyMock;
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

import gov.hhs.cms.ff.fm.eps.rap.dao.BatchProcessDAO;
import gov.hhs.cms.ff.fm.eps.rap.domain.BatchProcessLog;
import junit.framework.TestCase;

/**
 * Test class for SbmExecutionReportJobListener.
 *
 * @author girish.padmanabhan
 */
@RunWith(JUnit4.class)
public class SbmExecutionReportJobListenerTest extends TestCase {

	/** The eps job listener. */
	private SbmExecutionReportJobListener sbmExecutionReportJobListener;
	private BatchProcessDAO mockBatchProcessDAO;
	
	/**
	 * Setup.
	 */
	@Before
	public void setup() {
	
		sbmExecutionReportJobListener = new SbmExecutionReportJobListener();
		
		mockBatchProcessDAO = EasyMock.createMock(BatchProcessDAO.class);
		sbmExecutionReportJobListener.setBatchProcessDAO(mockBatchProcessDAO);
	}

	/**
	 * This method tests the beforeJob API of the listener.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testBeforeJob_sbm() throws Exception {
		
		expect(mockBatchProcessDAO.getNextBatchBusinessIdSeq(EasyMock.anyString()))
		.andReturn(-1).anyTimes();
		mockBatchProcessDAO.insertBatchProcessLog(EasyMock.anyObject(BatchProcessLog.class));
		expectLastCall();
		replay(mockBatchProcessDAO);
		
        final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
        JobParameters jobParameters = new JobParameters(params);
        
		JobInstance jobInst = new JobInstance(9999L,"sbmExecutionReportJob");
		JobExecution jobEx = new JobExecution(jobInst, jobParameters);
		jobEx.setStartTime(DateTime.now().toDate());
		
		sbmExecutionReportJobListener.beforeJob(jobEx);

		assertNotNull("jobInst", jobInst);
	}
	
	@Test
	public void testAfterJob() throws Exception {
		mockBatchProcessDAO.updateBatchProcessLog(EasyMock.anyObject(BatchProcessLog.class));
		expectLastCall();
		replay(mockBatchProcessDAO);
		
        final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
        JobParameters jobParameters = new JobParameters(params);
		
		JobInstance jobInst = new JobInstance(9999L,"sbmExecutionReportJob");
		JobExecution jobEx = new JobExecution(jobInst, jobParameters);
		ExecutionContext ctx = new ExecutionContext();
		ctx.putString("batchBusinessId", "batchBusinessId");
		jobEx.setExecutionContext(ctx);
		jobEx.setStartTime(DateTime.now().toDate());
		jobEx.setEndTime(DateTime.now().plusHours(1).toDate());
		
		sbmExecutionReportJobListener.afterJob(jobEx);
		
		assertNotNull("jobInst", jobInst);
	}
	
}
