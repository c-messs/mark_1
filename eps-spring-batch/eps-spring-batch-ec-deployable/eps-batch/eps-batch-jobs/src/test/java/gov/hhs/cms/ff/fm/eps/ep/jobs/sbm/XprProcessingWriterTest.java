/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.item.ExecutionContext;

import gov.cms.dsh.sbmi.PolicyType;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMPolicyDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SbmErrWarningLogDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SbmXprService;
import junit.framework.TestCase;

/**
 * Test class for xprProcessingWriter
 * 
 * @author girish.padmanabhan
 * 
 */
@RunWith(JUnit4.class)
public class XprProcessingWriterTest extends TestCase {

	private XprProcessingWriter xprProcessingWriter;
	private JobExecution jobExecution;
	private SbmXprService mockedSbmXprService;
	
	@Before
	public void setup() {
		xprProcessingWriter = new XprProcessingWriter();
		mockedSbmXprService= createMock(SbmXprService.class);
		xprProcessingWriter.setSbmXprService(mockedSbmXprService);
		
		ExecutionContext ctx = new ExecutionContext();
		jobExecution = new JobExecution(9999L);
		jobExecution.setExecutionContext(ctx);
		xprProcessingWriter.setJobExecutionContext(jobExecution);
	}
	
	@Test
	public void testWrite_success_NoErrors() throws Exception {
		mockedSbmXprService.saveXprTransaction(EasyMock.anyObject(SBMPolicyDTO.class));
		EasyMock.expectLastCall(); 
		replay(mockedSbmXprService);
		
		List<SBMPolicyDTO> policies = createMockPolicy(9999999999990L);
		policies.get(0).getErrorList().addAll(new ArrayList<SbmErrWarningLogDTO>());
		xprProcessingWriter.write(policies);
		
		SBMPolicyDTO sbmPolicyDTO = policies.get(0);
		assertNotNull("BemTypes are not null after calling the writer", policies);
		assertEquals("TransMsgId in bemDTO is 9999999999990L", 9999999999990L, sbmPolicyDTO.getSbmTransMsgId().longValue());
	}

	@Test
	public void testWrite_success_Null_Policies() throws Exception {
		
		assertNotNull("xprProcessingWriter", xprProcessingWriter);
		List<SBMPolicyDTO> policies = null;
		xprProcessingWriter.write(policies);
	}
	
	@Test
	public void testWrite_withErrors_success() throws Exception {
		
		List<SBMPolicyDTO> policies = createMockPolicyWithErrorMsgs(9999999999990L);
		policies.get(0).setErrorFlag(true);
		xprProcessingWriter.write(policies);
		
		SBMPolicyDTO sbmPolicyDTO = policies.get(0);
		assertNotNull("sbmPolicyDTO list of bems", policies);
		assertEquals("sbmPolicyDTO TransmsgId", 9999999999990L, sbmPolicyDTO.getSbmTransMsgId().longValue());
	}
	
	private List<SBMPolicyDTO> createMockPolicy(Long transMsgId) {
		List<SBMPolicyDTO> policies = new ArrayList<SBMPolicyDTO>();
		
		SBMPolicyDTO policy1  = new SBMPolicyDTO();
		
		policy1.setSbmTransMsgId(transMsgId);
		
		PolicyType policy = new PolicyType();
		policy.setExchangeAssignedPolicyId(transMsgId.toString());
		policy.setQHPId(RandomStringUtils.randomAlphanumeric(14));
		policy1.setPolicy(policy);
		policy1.setSbmFileProcSummaryId(9999L);
		
		policies.add(policy1);
		
		return policies;
	}
	
	
	
	private List<SBMPolicyDTO> createMockPolicyWithErrorMsgs(Long transMsgId) {
		List<SBMPolicyDTO> policies = new ArrayList<SBMPolicyDTO>();
		
		SBMPolicyDTO policy1  = new SBMPolicyDTO();
		policy1.setSbmTransMsgId(transMsgId);
		
		PolicyType policy = new PolicyType();
		policy.setExchangeAssignedPolicyId(transMsgId.toString());
		policy1.setPolicy(policy);
		policy1.setSbmFileProcSummaryId(9999L);
		
		List <SbmErrWarningLogDTO> errorList = new ArrayList<SbmErrWarningLogDTO>();
		
		SbmErrWarningLogDTO e1 = new SbmErrWarningLogDTO();
		e1.setErrorWarningTypeCd("E028");
		e1.setErrorWarningDesc(Arrays.asList("Test err1"));
		errorList.add(e1);
		
		SbmErrWarningLogDTO e2 = new SbmErrWarningLogDTO();
		e2.setErrorWarningTypeCd("E028");
		e2.setErrorWarningDesc(Arrays.asList("Test err2"));
		errorList.add(e2);
		
		policy1.getErrorList().addAll(errorList);
		policies.add(policy1);
		
		return policies;
	}
	

}
