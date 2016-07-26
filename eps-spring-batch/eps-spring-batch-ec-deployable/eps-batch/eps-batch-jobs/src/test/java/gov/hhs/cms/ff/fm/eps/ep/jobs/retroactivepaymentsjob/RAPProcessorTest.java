/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.retroactivepaymentsjob;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import gov.hhs.cms.ff.fm.eps.rap.domain.RapConstants;
import gov.hhs.cms.ff.fm.eps.rap.dto.PolicyDataDTO;
import gov.hhs.cms.ff.fm.eps.rap.dto.PolicyPaymentTransDTO;
import gov.hhs.cms.ff.fm.eps.rap.service.RAPProcessingRequest;
import gov.hhs.cms.ff.fm.eps.rap.service.RAPProcessingResponse;
import gov.hhs.cms.ff.fm.eps.rap.service.RapProcessingService;

import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.FactoryConfigurationError;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.batch.core.JobExecution;
import org.springframework.oxm.XmlMappingException;

/**
 * Test class for RAPProcessor
 * 
 * @author girish.padmanabhan
 * 
 */
@RunWith(JUnit4.class)
public class RAPProcessorTest extends TestCase {

	RAPProcessor rapProcessor;
	RapProcessingService mockRapProcessingService;
	JobExecution jobExecution;
	
	@Before
	public void setup() throws FactoryConfigurationError, XmlMappingException, Exception {
		rapProcessor = new RAPProcessor();
		mockRapProcessingService = createMock(RapProcessingService.class);
		rapProcessor.setRapService(mockRapProcessingService);
		
		jobExecution = new JobExecution(9999L);
		rapProcessor.setJobExecution(jobExecution);
	}
	
	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.retroactivepaymentsjob.RAPProcessor#process(PolicyDataDTO)}
	 * This method tests the successful invocation of processor.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testProcess_success() throws Exception {
		
		RAPProcessingResponse response = new RAPProcessingResponse();
		List<PolicyPaymentTransDTO> pmtTrans = new ArrayList<PolicyPaymentTransDTO>();
		pmtTrans.add(new PolicyPaymentTransDTO());
		response.setPolicyPaymentTransactions(pmtTrans);
		
		expect(mockRapProcessingService.processRetroActivePayments(EasyMock.anyObject(RAPProcessingRequest.class))).andReturn(response);
		replay(mockRapProcessingService);
		
		PolicyDataDTO policy = new PolicyDataDTO();
		DateTime lastPolicyVersionDt = DateTime.now();
		policy.setMaintenanceStartDateTime(lastPolicyVersionDt);
		List<PolicyPaymentTransDTO> pmts = rapProcessor.process(policy);
		
		assertNotNull("Response List is not null", pmts);
		assertEquals("Response List size is 1", 1, pmts.size());
		assertEquals("lastPolicyVersionDt", lastPolicyVersionDt, 
				jobExecution.getExecutionContext().get(RapConstants.LASTPOLICYVERSIONDATE));
	}
	

	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.retroactivepaymentsjob.RAPProcessor#process(PolicyDataDTO)}
	 * This test method expects the process() method to throw an Exception
	 * 
	 * @throws Exception
	 */
	@Test
	(expected=Exception.class)
	public void testProcess_exception() throws Exception {
		expect(mockRapProcessingService.processRetroActivePayments(EasyMock.anyObject(RAPProcessingRequest.class))).andThrow(new Exception());
		replay(mockRapProcessingService);
		assertNotNull("RAPProcessor", rapProcessor);
		rapProcessor.process(new PolicyDataDTO());
	}
	
	@After
	public void tearDown() {
	}
	
	
	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.retroactivepaymentsjob.RAPProcessor#process(PolicyDataDTO)}
	 * This method tests the successful invocation of processor.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testProcess_LPVD_After() throws Exception {
		
		RAPProcessingResponse response = new RAPProcessingResponse();
		List<PolicyPaymentTransDTO> pmtTrans = new ArrayList<PolicyPaymentTransDTO>();
		pmtTrans.add(new PolicyPaymentTransDTO());
		response.setPolicyPaymentTransactions(pmtTrans);
	
		expect(mockRapProcessingService.processRetroActivePayments(EasyMock.anyObject(RAPProcessingRequest.class))).andReturn(response);
		replay(mockRapProcessingService);
		
		PolicyDataDTO policy = new PolicyDataDTO();
		DateTime lastPolicyVersionDt = DateTime.now();
		DateTime contextLastPolicyVersionDt = lastPolicyVersionDt.plusDays(2);
		
		jobExecution.getExecutionContext().put(RapConstants.LASTPOLICYVERSIONDATE, contextLastPolicyVersionDt);
		
		policy.setMaintenanceStartDateTime(lastPolicyVersionDt);
		List<PolicyPaymentTransDTO> pmts = rapProcessor.process(policy);
		
		assertNotNull("Response List is not null", pmts);
		assertEquals("Response List size is 1", 1, pmts.size());
		assertEquals("lastPolicyVersionDt", contextLastPolicyVersionDt, 
				jobExecution.getExecutionContext().get(RapConstants.LASTPOLICYVERSIONDATE));
	}
	
	
	@Test
	public void testProcess_LPVD_Before() throws Exception {
		
		RAPProcessingResponse response = new RAPProcessingResponse();
		List<PolicyPaymentTransDTO> pmtTrans = new ArrayList<PolicyPaymentTransDTO>();
		pmtTrans.add(new PolicyPaymentTransDTO());
		response.setPolicyPaymentTransactions(pmtTrans);
		
		expect(mockRapProcessingService.processRetroActivePayments(EasyMock.anyObject(RAPProcessingRequest.class))).andReturn(response);
		replay(mockRapProcessingService);
		
		PolicyDataDTO policy = new PolicyDataDTO();
		DateTime lastPolicyVersionDt = DateTime.now();
		policy.setMaintenanceStartDateTime(lastPolicyVersionDt);
		
	    DateTime contextLastPolicyVersionDt = lastPolicyVersionDt.minusDays(2);
		
		jobExecution.getExecutionContext().put(RapConstants.LASTPOLICYVERSIONDATE, contextLastPolicyVersionDt);
		
		List<PolicyPaymentTransDTO> pmts = rapProcessor.process(policy);
		
		assertNotNull("Response List is not null", pmts);
		assertEquals("Response List size is 1", 1, pmts.size());
		assertEquals("lastPolicyVersionDt", lastPolicyVersionDt, 
				jobExecution.getExecutionContext().get(RapConstants.LASTPOLICYVERSIONDATE));
	}

}
