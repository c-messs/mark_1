package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm;

import static gov.hhs.cms.ff.fm.eps.ep.jobs.sbm.SbmiProcessFlowDecider.FLOWEXECUTION_COMPLETE;
import static gov.hhs.cms.ff.fm.eps.ep.jobs.sbm.SbmiProcessFlowDecider.FLOWEXECUTION_EXTRACT;
import static gov.hhs.cms.ff.fm.eps.ep.jobs.sbm.SbmiProcessFlowDecider.FLOWEXECUTION_FILE;
import static gov.hhs.cms.ff.fm.eps.ep.jobs.sbm.SbmiProcessFlowDecider.FLOWEXECUTION_XPR;
import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.JOBPARAMETER_PROCESSTYPE;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;

import java.util.LinkedHashMap;
import java.util.Map;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author rajesh.talanki
 *
 */
public class SbmiProcessFlowDeciderTest {
	
	private SbmiProcessFlowDecider sbmiFlowDecider;
	
	private JdbcTemplate mockJdbcTemplate;
	
	private String stagingXprCountSelect;
	private String pendingExtractCountSelect;
		
	@Before
	public void setUp() {
		sbmiFlowDecider = new SbmiProcessFlowDecider();
		
		mockJdbcTemplate= createMock(JdbcTemplate.class);
		sbmiFlowDecider.setJdbcTemplate(mockJdbcTemplate);
		
		sbmiFlowDecider.setStagingXprCountSelect(stagingXprCountSelect);
		sbmiFlowDecider.setPendingExtractCountSelect(pendingExtractCountSelect);
	}
	
	@Test
	public void testXPRFlow() {
		
		EasyMock.expect(mockJdbcTemplate.queryForObject(
				stagingXprCountSelect, Integer.class)).andReturn(new Integer(1));
		replay(mockJdbcTemplate);
		
		final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
        params.put(JOBPARAMETER_PROCESSTYPE, new JobParameter("sbmi"));
        JobParameters jobParameters = new JobParameters(params);
		JobExecution jobExecution = new JobExecution(101L, jobParameters);
		
		FlowExecutionStatus flowExecutionStatus = sbmiFlowDecider.decide(jobExecution, null);
		
		Assert.assertNotNull("flowExecutionStatus returned is cannot be null", flowExecutionStatus);
		Assert.assertEquals("flowExecutionStatus returned is XPR", FLOWEXECUTION_XPR, flowExecutionStatus.getName()); 
	}
	
	@Test
	public void testXPRExtractFlow() {
		
		EasyMock.expect(mockJdbcTemplate.queryForObject(
				stagingXprCountSelect, Integer.class)).andReturn(new Integer(0));
		EasyMock.expect(mockJdbcTemplate.queryForObject(
				pendingExtractCountSelect, Integer.class)).andReturn(new Integer(1));
		replay(mockJdbcTemplate);
		
		final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
        params.put(JOBPARAMETER_PROCESSTYPE, new JobParameter("sbmi"));
        JobParameters jobParameters = new JobParameters(params);
		JobExecution jobExecution = new JobExecution(101L, jobParameters);
		
		FlowExecutionStatus flowExecutionStatus = sbmiFlowDecider.decide(jobExecution, null);
		
		Assert.assertNotNull("flowExecutionStatus returned is cannot be null", flowExecutionStatus);
		Assert.assertEquals("flowExecutionStatus returned is XPR EXTRACT", FLOWEXECUTION_EXTRACT, flowExecutionStatus.getName()); 
	}
	
	@Test
	public void testFileIngestFlow() {
		
		EasyMock.expect(mockJdbcTemplate.queryForObject(
				stagingXprCountSelect, Integer.class)).andReturn(new Integer(0));
		EasyMock.expect(mockJdbcTemplate.queryForObject(
				pendingExtractCountSelect, Integer.class)).andReturn(new Integer(0));
		replay(mockJdbcTemplate);
		
		ExecutionContext ctx = new ExecutionContext();
		ctx.put("JOB_EXIT_CODE", "CONTINUE");
		
		final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
        params.put(JOBPARAMETER_PROCESSTYPE, new JobParameter("sbmi"));
        JobParameters jobParameters = new JobParameters(params);
		JobExecution jobExecution = new JobExecution(101L, jobParameters);
		jobExecution.setExecutionContext(ctx);
		
		StepExecution stepExecution = new StepExecution("xprExtraction", jobExecution);
		
		FlowExecutionStatus flowExecutionStatus = sbmiFlowDecider.decide(jobExecution, stepExecution);
		
		Assert.assertNotNull("flowExecutionStatus returned is cannot be null", flowExecutionStatus);
		Assert.assertEquals("flowExecutionStatus returned is FILE", FLOWEXECUTION_FILE, flowExecutionStatus.getName()); 
	}
	
	@Test
	public void testFileIngestFlow_SomeValue() {
		
		EasyMock.expect(mockJdbcTemplate.queryForObject(
				stagingXprCountSelect, Integer.class)).andReturn(new Integer(0));
		EasyMock.expect(mockJdbcTemplate.queryForObject(
				pendingExtractCountSelect, Integer.class)).andReturn(new Integer(0));
		replay(mockJdbcTemplate);
		
		ExecutionContext ctx = new ExecutionContext();
		ctx.put("JOB_EXIT_CODE", "DISCONTINUE");
		
		final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
        JobParameters jobParameters = new JobParameters(params);
		JobExecution jobExecution = new JobExecution(101L, jobParameters);
		jobExecution.setExecutionContext(ctx);
		
		StepExecution stepExecution = new StepExecution("xprExtraction", jobExecution);
		
		FlowExecutionStatus flowExecutionStatus = sbmiFlowDecider.decide(jobExecution, stepExecution);
		
		Assert.assertNotNull("flowExecutionStatus returned is cannot be null", flowExecutionStatus);
		Assert.assertEquals("flowExecutionStatus returned is FILE", FLOWEXECUTION_COMPLETE, flowExecutionStatus.getName()); 
	}
	
	@Test
	public void testFileIngestFlow_null() {
		
		EasyMock.expect(mockJdbcTemplate.queryForObject(
				stagingXprCountSelect, Integer.class)).andReturn(new Integer(0));
		EasyMock.expect(mockJdbcTemplate.queryForObject(
				pendingExtractCountSelect, Integer.class)).andReturn(new Integer(0));
		replay(mockJdbcTemplate);
		
		ExecutionContext ctx = new ExecutionContext();
		
		final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
        JobParameters jobParameters = new JobParameters(params);
		JobExecution jobExecution = new JobExecution(101L, jobParameters);
		jobExecution.setExecutionContext(ctx);
		
		StepExecution stepExecution = new StepExecution("xprExtraction", jobExecution);
		
		FlowExecutionStatus flowExecutionStatus = sbmiFlowDecider.decide(jobExecution, stepExecution);
		
		Assert.assertNotNull("flowExecutionStatus returned is cannot be null", flowExecutionStatus);
		Assert.assertEquals("flowExecutionStatus returned is FILE", FLOWEXECUTION_COMPLETE, flowExecutionStatus.getName()); 
	}
	
	@Test
	public void testDefaultFlow_someValue() {
		final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
        params.put(JOBPARAMETER_PROCESSTYPE, new JobParameter("abcd"));
        JobParameters jobParameters = new JobParameters(params);
		JobExecution jobExecution = new JobExecution(101L, jobParameters);
		
		FlowExecutionStatus flowExecutionStatus = sbmiFlowDecider.decide(jobExecution, null);
		
		Assert.assertNotNull("flowExecutionStatus returned is cannot be null", flowExecutionStatus);
		Assert.assertEquals("flowExecutionStatus returned is COMPLETE", FLOWEXECUTION_COMPLETE, flowExecutionStatus.getName()); 
	}
	
}
