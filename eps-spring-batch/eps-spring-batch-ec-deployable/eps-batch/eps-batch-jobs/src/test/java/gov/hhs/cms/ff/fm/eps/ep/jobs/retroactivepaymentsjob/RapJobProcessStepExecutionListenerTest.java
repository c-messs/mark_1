/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.retroactivepaymentsjob;


import java.util.LinkedHashMap;
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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;

import junit.framework.TestCase;

/**
 * @author shasidar.pabolu
 *
 */
@RunWith(JUnit4.class)
public class RapJobProcessStepExecutionListenerTest extends TestCase {

	RapJobProcessStepExecutionListener rapJobProcessStepExecutionListener;		
	private JdbcTemplate mockJdbcTemplate;
	
	
	@Before
	public void setUp() throws Exception {
	
		rapJobProcessStepExecutionListener = new RapJobProcessStepExecutionListener();
		
		mockJdbcTemplate= createMock(JdbcTemplate.class);		
		rapJobProcessStepExecutionListener.setJdbcTemplate(mockJdbcTemplate);
	}

	/**
	 * Test method for {@link gov.hhs.cms.ff.fm.eps.ep.jobs.retroactivepaymentsjob.RapJobProcessStepExecutionListener#beforeStep(org.springframework.batch.core.StepExecution)}.
	 */
	@Test
	public void testBeforeStep() {
		
		EasyMock.expect(mockJdbcTemplate.update(EasyMock.anyString(),EasyMock.anyLong())).andReturn(1).anyTimes();
		replay(mockJdbcTemplate);
		
		rapJobProcessStepExecutionListener.setLockStageIssuerId(null);
		
        final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
        JobParameters jobParameters = new JobParameters(params);
		JobExecution jobExecution = new JobExecution(101L, jobParameters);
        jobExecution.setJobInstance(new JobInstance(9999L, "retroActivePaymentsJob"));
        
    	StepExecution stepExecution = new StepExecution("rapProcessing", jobExecution);
	
        
        rapJobProcessStepExecutionListener.beforeStep(stepExecution);
        
        
        ExitStatus status = rapJobProcessStepExecutionListener.afterStep(stepExecution); 
        
        assertEquals("Step name is not modified","rapProcessing", stepExecution.getStepName());
        assertEquals("Exit Status is null",status,null);
	}
	
	/**
	 * Test method for {@link gov.hhs.cms.ff.fm.eps.ep.jobs.retroactivepaymentsjob.RapJobProcessStepExecutionListener#afterStep(org.springframework.batch.core.StepExecution)}.
	 */
	@Test
	public void testAfterStep() {
		
		EasyMock.expect(mockJdbcTemplate.update(EasyMock.anyString(),EasyMock.anyLong())).andReturn(1).anyTimes();
		replay(mockJdbcTemplate);
		
		rapJobProcessStepExecutionListener.setDeleteProcessedStageIssuerId(null);
			
        final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
        JobParameters jobParameters = new JobParameters(params);
		JobExecution jobExecution = new JobExecution(101L, jobParameters);
        jobExecution.setJobInstance(new JobInstance(9999L, "retroActivePaymentsJob"));
        
    	StepExecution stepExecution = new StepExecution("rapProcessing", jobExecution);
	    ExitStatus status = rapJobProcessStepExecutionListener.afterStep(stepExecution); 
        
        assertEquals("Exit Status is null",status,null);
	}

}
