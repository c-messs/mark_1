/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.easymock.EasyMock;
import org.joda.time.DateTime;
import org.junit.After;
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

import com.accenture.foundation.common.exception.ApplicationException;

import gov.hhs.cms.ff.fm.eps.ep.StateProrationConfiguration;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMCache;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SbmConfigDao;
import gov.hhs.cms.ff.fm.eps.rap.dao.BatchProcessDAO;
import gov.hhs.cms.ff.fm.eps.rap.domain.BatchProcessLog;
import junit.framework.TestCase;

/**
 * Test class for EpsJobExecutionListener.
 *
 * @author girish.padmanabhan
 */
@RunWith(JUnit4.class)
public class SbmiJobExecutionListenerTest extends TestCase {

	/** The eps job listener. */
	private SbmiJobExecutionListener sbmiJobListener;
	private SbmConfigDao mockSbmConfigDao;
	private BatchProcessDAO mockBatchProcessDAO;
	private SBMEvaluatePendingFiles mockSBMEvaluatePendingFiles;
	
	/**
	 * Setup.
	 */
	@Before
	public void setup() {
	
		sbmiJobListener = new SbmiJobExecutionListener();
		
		mockSbmConfigDao = EasyMock.createMock(SbmConfigDao.class);
		sbmiJobListener.setSbmConfigDao(mockSbmConfigDao);
		
		mockBatchProcessDAO = EasyMock.createMock(BatchProcessDAO.class);
		sbmiJobListener.setBatchProcessDAO(mockBatchProcessDAO);
		
		mockSBMEvaluatePendingFiles = EasyMock.createMock(SBMEvaluatePendingFiles.class);
		sbmiJobListener.setSbmEvaluatePendingFiles(mockSBMEvaluatePendingFiles);
	}

	/**
	 * This method tests the beforeJob API of the listener.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testBeforeJob_sbm() throws Exception {

		expect(mockSbmConfigDao.retrieveErrorCodesAndDescription()).andReturn(new HashMap<String, String>()).anyTimes();
		expect(mockSbmConfigDao.retrieveSbmStates()).andReturn(Collections.emptyList()).anyTimes();
		expect(mockSbmConfigDao.retrieveBusinessRules()).andReturn(Collections.emptyList()).anyTimes();
		expect(mockSbmConfigDao.retrieveLanguageCodes()).andReturn(Collections.emptyList()).anyTimes();
		expect(mockSbmConfigDao.retrieveRaceEthnicityCodes()).andReturn(Collections.emptyList()).anyTimes();
		replay(mockSbmConfigDao);
		
        final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
        params.put("processingType", new JobParameter("sbm"));
        JobParameters jobParameters = new JobParameters(params);
        
		JobInstance jobInst = new JobInstance(9999L,"sbmiJob");
		JobExecution jobEx = new JobExecution(jobInst, jobParameters);
		
		sbmiJobListener.beforeJob(jobEx);

		assertNotNull("processingType", jobEx.getJobParameters().getString("processingType"));
		assertEquals("processingType", "sbm", jobEx.getJobParameters().getString("processingType"));
		
		assertTrue("SBMCache", MapUtils.isEmpty(SBMCache.getErrorCodeDescriptionMap()));
		assertTrue("SBMCache", MapUtils.isEmpty(SBMCache.getStateProrationConfigMap()));
		assertTrue("SBMCache", MapUtils.isEmpty(SBMCache.getBusinessRulesMap()));
		assertTrue("SBMCache", CollectionUtils.isEmpty(SBMCache.getLanguageCodes()));
		assertTrue("SBMCache", CollectionUtils.isEmpty(SBMCache.getRaceEthnicityCodes()));
		
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
		
		StateProrationConfiguration stateConfig = new StateProrationConfiguration();
		stateConfig.setMarketYear(LocalDate.now().getYear());
		stateConfig.setStateCd("VA");
		List<StateProrationConfiguration> stateConfigs = (List<StateProrationConfiguration>) Arrays.asList(stateConfig);
		
		String [] rules = {"VA", "R001"};
		List<String[]> bizRules = new ArrayList<String[]>();
		bizRules.add(rules);
		
		expect(mockSbmConfigDao.retrieveErrorCodesAndDescription()).andReturn(errorCodes).anyTimes();
		expect(mockSbmConfigDao.retrieveSbmStates()).andReturn(stateConfigs).anyTimes();
		expect(mockSbmConfigDao.retrieveBusinessRules()).andReturn(bizRules).anyTimes();
		expect(mockSbmConfigDao.retrieveLanguageCodes()).andReturn(Arrays.asList("ENG")).anyTimes();
		expect(mockSbmConfigDao.retrieveRaceEthnicityCodes()).andReturn(Arrays.asList("TBD")).anyTimes();
		replay(mockSbmConfigDao);
		
        final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
        params.put("processingType", new JobParameter("sbm"));
        JobParameters jobParameters = new JobParameters(params);
        
		JobInstance jobInst = new JobInstance(9999L,"sbmiJob");
		JobExecution jobEx = new JobExecution(jobInst, jobParameters);
		
		sbmiJobListener.beforeJob(jobEx);

		assertNotNull("processingType", jobEx.getJobParameters().getString("processingType"));
		assertEquals("processingType", "sbm", jobEx.getJobParameters().getString("processingType"));
		
		assertTrue("SBMCache", MapUtils.isNotEmpty(SBMCache.getErrorCodeDescriptionMap()));
		assertTrue("SBMCache", MapUtils.isNotEmpty(SBMCache.getStateProrationConfigMap()));
		assertTrue("SBMCache", MapUtils.isNotEmpty(SBMCache.getBusinessRulesMap()));
		assertTrue("SBMCache", CollectionUtils.isNotEmpty(SBMCache.getLanguageCodes()));
		assertTrue("SBMCache", CollectionUtils.isNotEmpty(SBMCache.getRaceEthnicityCodes()));
	}
	
	@Test
	public void testBeforeJob_sbmi() throws Exception {

		expect(mockBatchProcessDAO.getNextBatchBusinessIdSeq(EasyMock.anyString()))
		.andReturn(-1).anyTimes();
		mockBatchProcessDAO.insertBatchProcessLog(EasyMock.anyObject(BatchProcessLog.class));
		expectLastCall();
		replay(mockBatchProcessDAO);
		
		mockSBMEvaluatePendingFiles.evaluatePendingFiles(EasyMock.anyLong(), EasyMock.anyBoolean());
		expectLastCall();
		replay(mockSBMEvaluatePendingFiles);
		
		expect(mockSbmConfigDao.retrieveErrorCodesAndDescription()).andReturn(new HashMap<String, String>()).anyTimes();
		expect(mockSbmConfigDao.retrieveSbmStates()).andReturn(Collections.emptyList()).anyTimes();
		expect(mockSbmConfigDao.retrieveBusinessRules()).andReturn(Collections.emptyList()).anyTimes();
		expect(mockSbmConfigDao.retrieveLanguageCodes()).andReturn(Collections.emptyList()).anyTimes();
		expect(mockSbmConfigDao.retrieveRaceEthnicityCodes()).andReturn(Collections.emptyList()).anyTimes();
		replay(mockSbmConfigDao);
		
        final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
        params.put("processingType", new JobParameter("sbmi"));
        JobParameters jobParameters = new JobParameters(params);
        
		JobInstance jobInst = new JobInstance(9999L,"sbmiJob");
		JobExecution jobEx = new JobExecution(jobInst, jobParameters);
		jobEx.setStartTime(DateTime.now().toDate());
		
		sbmiJobListener.beforeJob(jobEx);

		assertNotNull("processingType", jobEx.getJobParameters().getString("processingType"));
		assertEquals("processingType", "sbmi", jobEx.getJobParameters().getString("processingType"));
	}
	
	@Test(expected=ApplicationException.class)
	public void testBeforeJob_sbmi_Exception() throws Exception {

		expect(mockBatchProcessDAO.getNextBatchBusinessIdSeq(EasyMock.anyString()))
		.andReturn(-1).anyTimes();
		mockBatchProcessDAO.insertBatchProcessLog(EasyMock.anyObject(BatchProcessLog.class));
		expectLastCall();
		replay(mockBatchProcessDAO);
		
		mockSBMEvaluatePendingFiles.evaluatePendingFiles(EasyMock.anyLong(), EasyMock.anyBoolean());
		expectLastCall().andThrow(new SQLException("Something terrible happened"));
		replay(mockSBMEvaluatePendingFiles);
		
		expect(mockSbmConfigDao.retrieveErrorCodesAndDescription()).andReturn(new HashMap<String, String>()).anyTimes();
		expect(mockSbmConfigDao.retrieveSbmStates()).andReturn(Collections.emptyList()).anyTimes();
		expect(mockSbmConfigDao.retrieveBusinessRules()).andReturn(Collections.emptyList()).anyTimes();
		expect(mockSbmConfigDao.retrieveLanguageCodes()).andReturn(Collections.emptyList()).anyTimes();
		expect(mockSbmConfigDao.retrieveRaceEthnicityCodes()).andReturn(Collections.emptyList()).anyTimes();
		replay(mockSbmConfigDao);
		
        final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
        params.put("processingType", new JobParameter("sbmi"));
        JobParameters jobParameters = new JobParameters(params);
        
		JobInstance jobInst = new JobInstance(9999L,"sbmiJob");
		JobExecution jobEx = new JobExecution(jobInst, jobParameters);
		jobEx.setStartTime(DateTime.now().toDate());
		
		sbmiJobListener.beforeJob(jobEx);

		assertNotNull("processingType", jobEx.getJobParameters().getString("processingType"));
		assertEquals("processingType", "sbmi", jobEx.getJobParameters().getString("processingType"));
	}
	
	/**
	 * This method tests the afterJob API of the listener.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testAfterJob() throws Exception {
		mockSBMEvaluatePendingFiles.evaluatePendingFiles(EasyMock.anyLong(), EasyMock.anyBoolean());
		expectLastCall();
		replay(mockSBMEvaluatePendingFiles);
		
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
		
		sbmiJobListener.afterJob(jobEx);
		
		assertEquals("processingType", "sbm", jobEx.getJobParameters().getString("processingType"));
	}
	
	@Test(expected=ApplicationException.class)
	public void testAfterJob_Exception() throws Exception {
		mockSBMEvaluatePendingFiles.evaluatePendingFiles(EasyMock.anyLong(), EasyMock.anyBoolean());
		expectLastCall().andThrow(new SQLException("Something terrible happened"));
		replay(mockSBMEvaluatePendingFiles);
		
        final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
        params.put("processingType", new JobParameter("sbm"));
        JobParameters jobParameters = new JobParameters(params);
		
		JobInstance jobInst = new JobInstance(9999L,"sbmJob");
		JobExecution jobEx = new JobExecution(jobInst, jobParameters);
		
		sbmiJobListener.afterJob(jobEx);
		
		assertEquals("processingType", "sbm", jobEx.getJobParameters().getString("processingType"));

	}
	
	@Test
	public void testGetBatchBusinessIdPrefixAny() {
		ReflectionTestUtils.setField(sbmiJobListener, "processingType", "AnyOtherJob");
		String jobPrefix = ReflectionTestUtils.invokeMethod(sbmiJobListener, "getBatchBusinessIdPrefix");
		
		assertNotNull("jobPrefix is null", jobPrefix);
		assertEquals("jobPrefix is sbmIngestionBatchJob", "sbmIngestionBatchJob", jobPrefix);
	}
	
	@Test
	public void testGetBatchBusinessIdPrefixSbmi() {
		ReflectionTestUtils.setField(sbmiJobListener, "processingType", "sbmi");
		String jobPrefix = ReflectionTestUtils.invokeMethod(sbmiJobListener, "getBatchBusinessIdPrefix");
		
		assertNotNull("jobPrefix is null", jobPrefix);
		assertEquals("jobPrefix is SBMINGEST", "SBMINGEST", jobPrefix);
	}
	
	@Test
	public void testGetBatchBusinessIdPrefixNullJobType() {
		ReflectionTestUtils.setField(sbmiJobListener, "processingType", null);
		String jobPrefix = ReflectionTestUtils.invokeMethod(sbmiJobListener, "getBatchBusinessIdPrefix");
		
		assertNotNull("jobPrefix is null", jobPrefix);
		assertEquals("jobPrefix is SBMINGEST", "SBMINGEST", jobPrefix);
	}

	
	@After
	public void tearDown() {
		
		SBMCache.getErrorCodeDescriptionMap().clear();
		SBMCache.getStateProrationConfigMap().clear();
		SBMCache.getBusinessRulesMap().clear();
		SBMCache.getLanguageCodes().clear();
		SBMCache.getRaceEthnicityCodes();
	}

}
