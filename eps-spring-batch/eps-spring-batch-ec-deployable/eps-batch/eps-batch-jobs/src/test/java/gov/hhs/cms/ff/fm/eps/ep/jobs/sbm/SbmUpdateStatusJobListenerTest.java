/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
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

import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMCache;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SbmConfigDao;
import gov.hhs.cms.ff.fm.eps.rap.dao.BatchProcessDAO;
import gov.hhs.cms.ff.fm.eps.rap.domain.BatchProcessLog;
import junit.framework.TestCase;

/**
 * Test class for SbmUpdateStatusJobListener.
 *
 * @author girish.padmanabhan
 */
@RunWith(JUnit4.class)
public class SbmUpdateStatusJobListenerTest extends TestCase {

	/** The eps job listener. */
	private SbmUpdateStatusJobListener sbmUpdateStatusJobListener;
	private SbmConfigDao mockSbmConfigDao;
	private BatchProcessDAO mockBatchProcessDAO;
	
	/**
	 * Setup.
	 */
	@Before
	public void setup() {
	
		sbmUpdateStatusJobListener = new SbmUpdateStatusJobListener();
		
		mockSbmConfigDao = EasyMock.createMock(SbmConfigDao.class);
		sbmUpdateStatusJobListener.setSbmConfigDao(mockSbmConfigDao);
		
		mockBatchProcessDAO = EasyMock.createMock(BatchProcessDAO.class);
		sbmUpdateStatusJobListener.setBatchProcessDAO(mockBatchProcessDAO);
	}

	/**
	 * This method tests the beforeJob API of the listener.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testBeforeJob_sbm() throws Exception {

		expect(mockSbmConfigDao.retrieveErrorCodesAndDescription())
		.andReturn(new HashMap<String, String>()).anyTimes();
		replay(mockSbmConfigDao);
		
		expect(mockBatchProcessDAO.getNextBatchBusinessIdSeq(EasyMock.anyString()))
		.andReturn(-1).anyTimes();
		mockBatchProcessDAO.insertBatchProcessLog(EasyMock.anyObject(BatchProcessLog.class));
		expectLastCall();
		replay(mockBatchProcessDAO);
		
        final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
        params.put("processingType", new JobParameter("sbm"));
        JobParameters jobParameters = new JobParameters(params);
        
		JobInstance jobInst = new JobInstance(9999L,"sbmiJob");
		JobExecution jobEx = new JobExecution(jobInst, jobParameters);
		jobEx.setStartTime(DateTime.now().toDate());
		
		sbmUpdateStatusJobListener.beforeJob(jobEx);

		assertTrue("SBMCache", MapUtils.isEmpty(SBMCache.getErrorCodeDescriptionMap()));
	}
	
	/**
	 * This method tests the beforeJob API of the listener.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testBeforeJob_sbm_loadRefData() throws Exception {

		Map<String, String> errorCodes = new HashMap<String, String>();
		errorCodes.put("ER-01", "Dummy error");
		
		expect(mockSbmConfigDao.retrieveErrorCodesAndDescription()).andReturn(errorCodes).anyTimes();
		replay(mockSbmConfigDao);
		
		expect(mockBatchProcessDAO.getNextBatchBusinessIdSeq(EasyMock.anyString()))
		.andReturn(-1).anyTimes();
		mockBatchProcessDAO.insertBatchProcessLog(EasyMock.anyObject(BatchProcessLog.class));
		expectLastCall();
		replay(mockBatchProcessDAO);
		
        final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
        params.put("processingType", new JobParameter("sbm"));
        JobParameters jobParameters = new JobParameters(params);
        
		JobInstance jobInst = new JobInstance(9999L,"sbmiJob");
		JobExecution jobEx = new JobExecution(jobInst, jobParameters);
		jobEx.setStartTime(DateTime.now().toDate());
		
		sbmUpdateStatusJobListener.beforeJob(jobEx);

		assertTrue("SBMCache", MapUtils.isNotEmpty(SBMCache.getErrorCodeDescriptionMap()));
	}
	
	@Test
	public void testAfterJob() throws Exception {
		mockBatchProcessDAO.updateBatchProcessLog(EasyMock.anyObject(BatchProcessLog.class));
		expectLastCall();
		replay(mockBatchProcessDAO);
		
        final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
        params.put("processingType", new JobParameter("sbm"));
        JobParameters jobParameters = new JobParameters(params);
		
		JobInstance jobInst = new JobInstance(9999L,"sbmJob");
		JobExecution jobEx = new JobExecution(jobInst, jobParameters);
		ExecutionContext ctx = new ExecutionContext();
		ctx.putString("batchBusinessId", "batchBusinessId");
		jobEx.setExecutionContext(ctx);
		jobEx.setStartTime(DateTime.now().toDate());
		jobEx.setEndTime(DateTime.now().plusHours(1).toDate());
		
		sbmUpdateStatusJobListener.afterJob(jobEx);
		
		assertEquals("processingType", "sbm", jobEx.getJobParameters().getString("processingType"));

	}
	
}
