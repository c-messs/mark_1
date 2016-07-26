/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.retroactivepaymentsjob;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;

import gov.hhs.cms.ff.fm.eps.ep.jobs.retroactivepaymentsjob.RapStageProcessTasklet;

import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author shasidar.pabolu
 *
 */
@RunWith(JUnit4.class)
public class RapStageProcessTaskletTest extends TestCase {

	RapStageProcessTasklet rapStageProcessTasklet;		
	private JdbcTemplate mockJdbcTemplate;
	private String loadRapStageSql;
	
	/**
	 * 
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		
		rapStageProcessTasklet = new RapStageProcessTasklet();
		
		mockJdbcTemplate= createMock(JdbcTemplate.class);		
		rapStageProcessTasklet.setJdbcTemplate(mockJdbcTemplate);
	
	}
	
	
	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.JdbcTasklet#execute()}
	 * This method tests the sql execution of the Tasklet, which then returns a 'RepeatStatus' of 'FINISHED'.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testExecute_Success() throws Exception {

		loadRapStageSql = "";
		rapStageProcessTasklet.setLoadRapStageSql(loadRapStageSql);
		
		EasyMock.expect(mockJdbcTemplate.update(EasyMock.anyString(),EasyMock.anyLong(),EasyMock.anyLong())).andReturn(1);
		replay(mockJdbcTemplate);
		
        final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
        JobParameters jobParameters = new JobParameters(params);
		JobExecution jobExecution = new JobExecution(101L, jobParameters);
        jobExecution.setJobInstance(new JobInstance(9999L, "retroActivePaymentsJob"));
        
		
		StepExecution stepExecution = new StepExecution("rapProcessing", jobExecution);
		StepContext stepContext = new StepContext(stepExecution);
		ChunkContext chunkContext = new ChunkContext(stepContext);
		
		RepeatStatus status = rapStageProcessTasklet.execute(null, chunkContext);
		
		assertNotNull("RepeatStatus not null", status);
		assertEquals("RepeatStatus is FINISHED", RepeatStatus.FINISHED, status);
	}


	
}
