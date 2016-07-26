package gov.hhs.cms.ff.fm.eps.ep.jobs.retroactivepaymentsjob;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.stream.FactoryConfigurationError;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.oxm.XmlMappingException;

@RunWith(JUnit4.class)
public class RAPLimitDeciderTest extends TestCase {
	
	private RAPLimitDecider RAPLimitDecider;	
	private JdbcTemplate mockJdbcTemplate;	
	private String rapStageIssuerIdSelect;
	
	@Before
	public void setup() throws FactoryConfigurationError, XmlMappingException, Exception {
		RAPLimitDecider = new RAPLimitDecider();
		RAPLimitDecider.setRapStageIssuerIdSelect(rapStageIssuerIdSelect);
		
		mockJdbcTemplate= createMock(JdbcTemplate.class);
		RAPLimitDecider.setJdbcTemplate(mockJdbcTemplate);
	}
	
	
	@Test
	public void testDecide_RAP_valid_Loop() throws Exception {
		
		EasyMock.expect(mockJdbcTemplate.queryForObject(
				rapStageIssuerIdSelect, Integer.class)).andReturn(new Integer(1));
		replay(mockJdbcTemplate);
		
        final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
        JobParameters jobParameters = new JobParameters(params);
		JobExecution jobExecution = new JobExecution(101L, jobParameters);
		
		FlowExecutionStatus flowStatusEnum = RAPLimitDecider.decide(jobExecution, null);
		
		assertNotNull("flowStatusEnum returned is not null", flowStatusEnum);
		assertEquals("flowStatusEnum returned is LOOP", "LOOP", flowStatusEnum.getName()); 
	}
	
	@Test
	public void testDecide_RAP_valid_Complete() throws Exception {
		
		EasyMock.expect(mockJdbcTemplate.queryForObject(
				rapStageIssuerIdSelect, Integer.class)).andReturn(new Integer(0));
		replay(mockJdbcTemplate);
		
        final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
        JobParameters jobParameters = new JobParameters(params);
		JobExecution jobExecution = new JobExecution(101L, jobParameters);
		
		FlowExecutionStatus flowStatusEnum = RAPLimitDecider.decide(jobExecution, null);
		
		assertNotNull("flowStatusEnum returned is not null", flowStatusEnum);
		assertEquals("flowStatusEnum returned is COMPLETE", "COMPLETE", flowStatusEnum.getName()); 
	}

}
