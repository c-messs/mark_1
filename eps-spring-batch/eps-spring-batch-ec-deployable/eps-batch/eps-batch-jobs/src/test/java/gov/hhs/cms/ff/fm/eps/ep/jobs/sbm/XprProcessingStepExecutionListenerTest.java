/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.expect;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import gov.hhs.cms.ff.fm.eps.ep.dao.StagingSbmFileDao;
import gov.hhs.cms.ff.fm.eps.ep.sbm.dao.impl.StagingSbmFileDaoImpl;
import junit.framework.TestCase;

/**
 * Test class for XprProcessingStepExecutionListener
 * 
 * @author girish.padmanabhan
 * 
 */
@RunWith(JUnit4.class)
public class XprProcessingStepExecutionListenerTest extends TestCase {

	private XprProcessingStepExecutionListener xprProcessingStepListener;
	private StagingSbmFileDao mockStagingSbmFileDao;
	private JdbcTemplate mockJdbcTemplate;
	

	StepExecution stepExecution;
	
	List<String> stageDataSqls = Arrays.asList("stageDataSql :jobId");
	List<String> postCleanUpSqls = Arrays.asList("cleanUpGroup", "cleanUpGrpLock :jobId");
	
	@Before
	public void setup() {
		xprProcessingStepListener = new XprProcessingStepExecutionListener();
		
		mockJdbcTemplate = createMock(JdbcTemplate.class);
		xprProcessingStepListener.setJdbcTemplate(mockJdbcTemplate);
		
		mockStagingSbmFileDao = createMock(StagingSbmFileDaoImpl.class);
		xprProcessingStepListener.setStagingSbmFileDao(mockStagingSbmFileDao);
		
		xprProcessingStepListener.setStageDataSqls(stageDataSqls);
		xprProcessingStepListener.setPostCleanUpSqls(postCleanUpSqls);
		
		
		JobExecution jobEx = new JobExecution(-9999L, getJobParams("sbmi"));
		jobEx.setJobInstance(new JobInstance(-9999L, "xprProcessor"));
		
		stepExecution = new StepExecution("xprProcessing", jobEx);
	}

	@Test
	public void testExecute_beforeStep() throws Exception {
		mockJdbcTemplate.execute(EasyMock.anyString());
		EasyMock.expectLastCall(); 
		replay(mockJdbcTemplate);
		
		expect(mockStagingSbmFileDao.getStagingPolicies(EasyMock.anyLong())).andReturn(Arrays.asList(""));
		replay(mockStagingSbmFileDao);	
		
		xprProcessingStepListener.beforeStep(stepExecution);
		
		assertEquals("Step name is not modified","xprProcessing", stepExecution.getStepName());
	}
	
	@Test
	public void testExecute_beforeStep_Non_Sbmi() throws Exception {
		mockJdbcTemplate.execute(EasyMock.anyString());
		replay(mockJdbcTemplate);
		
		JobExecution jobEx = new JobExecution(-9999L, getJobParams("sbmr"));
		stepExecution = new StepExecution("xprProcessing", jobEx);
		xprProcessingStepListener.beforeStep(stepExecution);
		
		assertEquals("Step name is not modified","xprProcessing", stepExecution.getStepName());
	}
	
	@Test
	public void testExecute_afterStep() throws Exception {
		
		mockJdbcTemplate.execute(EasyMock.anyString());
		mockJdbcTemplate.execute(EasyMock.anyString());
		replay(mockJdbcTemplate);
		
		ExitStatus stepStatus = xprProcessingStepListener.afterStep(stepExecution);
		
		assertNull("step exit status", stepStatus);
	}
	
	@Test
	public void testExecute_afterStep_sbmi() throws Exception {
		
		mockJdbcTemplate.execute(EasyMock.anyString());
		mockJdbcTemplate.execute(EasyMock.anyString());
		replay(mockJdbcTemplate);
		
		ReflectionTestUtils.setField(xprProcessingStepListener, "processingType", "sbmi");
		
		JobExecution jobEx = new JobExecution(-9999L, getJobParams("sbmi"));
		jobEx.setJobInstance(new JobInstance(-9999L, "xprProcessor"));
		stepExecution = new StepExecution("xprProcessing", jobEx);
		
		ExitStatus stepStatus = xprProcessingStepListener.afterStep(stepExecution);
		
		assertNull("step exit status", stepStatus);
	}
	
	@Test
	public void testExecute_afterStep_sbmi_nullParam() throws Exception {
		
		mockJdbcTemplate.execute(EasyMock.anyString());
		mockJdbcTemplate.execute(EasyMock.anyString());
		replay(mockJdbcTemplate);
		
		JobExecution jobEx = new JobExecution(-9999L, getJobParams(null));
		jobEx.setJobInstance(new JobInstance(-9999L, "xprProcessor"));
		stepExecution = new StepExecution("xprProcessing", jobEx);
		
		ExitStatus stepStatus = xprProcessingStepListener.afterStep(stepExecution);
		
		assertNull("step exit status", stepStatus);
	}

	@Test
	public void testExecute_afterStep_Non_Sbmi() throws Exception {
		mockJdbcTemplate.execute(EasyMock.anyString());
		mockJdbcTemplate.execute(EasyMock.anyString());
		replay(mockJdbcTemplate);
		
		ReflectionTestUtils.setField(xprProcessingStepListener, "processingType", "sbmr");
		
		JobExecution jobEx = new JobExecution(-9999L, getJobParams("sbmr"));
		stepExecution = new StepExecution("xprProcessing", jobEx);
		ExitStatus stepStatus = xprProcessingStepListener.afterStep(stepExecution);
		
		assertNull("step exit status", stepStatus);
	}

	private JobParameters getJobParams(String processingType) {
        final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
        params.put("processingType", new JobParameter(processingType));
        return new JobParameters(params);
	}

}
