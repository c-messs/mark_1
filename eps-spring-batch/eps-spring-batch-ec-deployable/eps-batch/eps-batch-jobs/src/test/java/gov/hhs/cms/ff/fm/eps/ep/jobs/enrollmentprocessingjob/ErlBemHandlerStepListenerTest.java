/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Test class for JdbcTasklet
 * 
 * @author girish.padmanabhan
 * 
 */
@ContextConfiguration(locations={"/test-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class ErlBemHandlerStepListenerTest extends TestCase {

	private ErlBemHandlerStepListener erlBemHandlerStepListener;
	
	private JdbcTemplate mockJdbcTemplate;
	
	@Autowired
	@Qualifier("qhpLockInsertSql")
    private String stageDataSql;
	
	@Autowired
	@Qualifier("erlBEMIndexPostCleanup")
    private String postCleanUpSql;
	
	@Autowired
	@Qualifier("qhpLockDeleteSql")
    private String stageDataCleanUpSql;
	
	@Autowired
	@Qualifier("reprocessQhpLockSql")
	private String dataLockSql;
	
	StepExecution stepExecution;
	
	@Before
	public void setup() {
		erlBemHandlerStepListener = new ErlBemHandlerStepListener();
		
		mockJdbcTemplate = createMock(JdbcTemplate.class);
		erlBemHandlerStepListener.setJdbcTemplate(mockJdbcTemplate);
		erlBemHandlerStepListener.setStageDataSql(stageDataSql);
		erlBemHandlerStepListener.setDataLockSql(dataLockSql);
		erlBemHandlerStepListener.setPostCleanUpSql(postCleanUpSql);
		erlBemHandlerStepListener.setStageDataCleanUpSql(stageDataCleanUpSql);
		
		JobExecution jobEx = new JobExecution(-9999L, getJobParams("ffm", "processor"));
		jobEx.setJobInstance(new JobInstance(-9999L, "processor"));
		
		stepExecution = new StepExecution("erlBemHandlerProcesssor", jobEx);
	}

	@Test
	public void testExecute_beforeStep() throws Exception {
		expect(mockJdbcTemplate.update(EasyMock.anyString(), EasyMock.anyLong())).andReturn(0);
		expect(mockJdbcTemplate.update(EasyMock.anyString(), EasyMock.anyLong(), EasyMock.anyLong())).andReturn(0);
		replay(mockJdbcTemplate);
		
		erlBemHandlerStepListener.beforeStep(stepExecution);
		
		assertEquals("Step name is not modified","erlBemHandlerProcesssor", stepExecution.getStepName());
	}
	
	@Test
	public void testExecute_beforeStep_Non_FFM() throws Exception {
		expect(mockJdbcTemplate.update(EasyMock.anyString(), EasyMock.anyLong())).andReturn(0);
		replay(mockJdbcTemplate);
		
		JobExecution jobEx = new JobExecution(-9999L, getJobParams("hub", "processor"));
		stepExecution = new StepExecution("erlBemHandlerProcesssor", jobEx);
		erlBemHandlerStepListener.beforeStep(stepExecution);
		
		assertEquals("Step name is not modified","erlBemHandlerProcesssor", stepExecution.getStepName());
	}
	
	@Test
	public void testExecute_beforeStep_DuplicatekeyException() throws Exception {
		expect(mockJdbcTemplate.update(EasyMock.anyString(), EasyMock.anyLong())).andThrow(new DuplicateKeyException("Testing Duplicate key Exception"));
		replay(mockJdbcTemplate);
		
		erlBemHandlerStepListener.beforeStep(stepExecution);
		
		assertNotNull("Duplicate Key Exception is Handled Gracefully", stepExecution.getStepName());
	}
	
	@Test
	public void testExecute_afterStep() throws Exception {
		expect(mockJdbcTemplate.update(EasyMock.anyString(), EasyMock.anyLong())).andReturn(0).anyTimes();
		replay(mockJdbcTemplate);
		
		ExitStatus stepStatus = erlBemHandlerStepListener.afterStep(stepExecution);
		
		assertNull("step exit status", stepStatus);
	}

	@Test
	public void testExecute_afterStep_Non_FFM() throws Exception {
		expect(mockJdbcTemplate.update(EasyMock.anyString(), EasyMock.anyLong())).andReturn(0);
		replay(mockJdbcTemplate);
		
		JobExecution jobEx = new JobExecution(-9999L, getJobParams("hub", "processor"));
		stepExecution = new StepExecution("erlBemHandlerProcesssor", jobEx);
		ExitStatus stepStatus = erlBemHandlerStepListener.afterStep(stepExecution);
		
		assertNull("step exit status", stepStatus);
	}
	

	private JobParameters getJobParams(String source, String jobType) {
        final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
        params.put("source", new JobParameter(source));
        params.put("jobType", new JobParameter(jobType));
        return new JobParameters(params);
	}

}
