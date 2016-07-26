/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentMaintenanceDTO;
import gov.hhs.cms.ff.fm.eps.ep.EPSValidationRequest;
import gov.hhs.cms.ff.fm.eps.ep.ErrorWarningLogDTO;
import gov.hhs.cms.ff.fm.eps.ep.enums.EProdEnum;
import gov.hhs.cms.ff.fm.eps.ep.enums.ExchangeType;
import gov.hhs.cms.ff.fm.eps.ep.enums.ProcessedToDbInd;
import gov.hhs.cms.ff.fm.eps.ep.services.ErrorWarningLogService;
import gov.hhs.cms.ff.fm.eps.ep.services.PolicyDataService;
import gov.hhs.cms.ff.fm.eps.ep.services.TransMsgCompositeDAO;
import gov.hhs.cms.ff.fm.eps.ep.services.impl.FFMDataServiceImpl;
import gov.hhs.cms.ff.fm.eps.ep.validation.EPSValidationService;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Test class for BEMHandlerWriter
 * 
 * @author girish.padmanabhan
 * 
 */
@RunWith(JUnit4.class)
public class ErlBEMHandlerWriterTest extends TestCase {

	private ErlBEMHandlerWriter bEMHandlerWriter;
	private TransMsgCompositeDAO mockedTxnMsgService;
	private PolicyDataService mockedPolicyDataService;
	private ErrorWarningLogService mockErrorWarningLogService;
	private EPSValidationService mockEPSValidationService;
	private BEMProcessorHelper mockBEMProcessorHelper;
	
	@Before
	public void setup() {
		bEMHandlerWriter = new ErlBEMHandlerWriter();
		mockedPolicyDataService= createMock(FFMDataServiceImpl.class);
		mockedTxnMsgService = createMock(TransMsgCompositeDAO.class);
		mockErrorWarningLogService = createMock(ErrorWarningLogService.class);
		mockEPSValidationService = createMock(EPSValidationService.class);
		mockBEMProcessorHelper = createMock(BEMProcessorHelper.class);
		
		bEMHandlerWriter.setTxnMsgService(mockedTxnMsgService);
		bEMHandlerWriter.setPolicyDataService(mockedPolicyDataService);
		bEMHandlerWriter.setErrorWarningLogService(mockErrorWarningLogService);
		bEMHandlerWriter.setEpsValidationService(mockEPSValidationService);
		bEMHandlerWriter.setBemProcessorHelper(mockBEMProcessorHelper);
		bEMHandlerWriter.setJobId(999999L);
	}

	/**
	 * Test method for Save Transaction without validation errors
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.ErlBEMHandlerWriter#write(gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentMaintenanceDTO)}
	 * .
	 * @throws Exception
	 */
	@Test
	public void testWrite_success_NullErrors() throws Exception {
		expect(mockEPSValidationService.validateBEM(EasyMock.anyObject(EPSValidationRequest.class))).andReturn(null);
		replay(mockEPSValidationService);
		mockedPolicyDataService.saveBEM(EasyMock.anyObject(BenefitEnrollmentMaintenanceDTO.class));
		EasyMock.expectLastCall(); 
		replay(mockedPolicyDataService);
		mockedTxnMsgService.updateBatchTransMsg(EasyMock.anyObject(BenefitEnrollmentMaintenanceDTO.class), 
				EasyMock.anyObject(ProcessedToDbInd.class));
		EasyMock.expectLastCall();
		replay(mockedTxnMsgService);
		
		List<BenefitEnrollmentMaintenanceDTO> bems = createMockBem(9999999999990L);
		bEMHandlerWriter.write(bems);
		
		BenefitEnrollmentMaintenanceDTO bemDTO = bems.get(0);
		assertNotNull("BemTypes are not null after calling the writer", bems);
		assertEquals("TransMsgId in bemDTO is 9999999999990L", 9999999999990L, bemDTO.getTransMsgId().longValue());
	}
	
	@Test
	public void testWrite_success_EmptyErrors() throws Exception {
		expect(mockEPSValidationService.validateBEM(EasyMock.anyObject(EPSValidationRequest.class))).andReturn(null);
		replay(mockEPSValidationService);
		mockedPolicyDataService.saveBEM(EasyMock.anyObject(BenefitEnrollmentMaintenanceDTO.class));
		EasyMock.expectLastCall(); 
		replay(mockedPolicyDataService);
		
		mockedTxnMsgService.updateBatchTransMsg(EasyMock.anyObject(BenefitEnrollmentMaintenanceDTO.class), 
				EasyMock.anyObject(ProcessedToDbInd.class));
		EasyMock.expectLastCall();
		replay(mockedTxnMsgService);
		
		List<BenefitEnrollmentMaintenanceDTO> bems = createMockBem(9999999999990L);
		bems.get(0).setErrorList(new ArrayList<ErrorWarningLogDTO>());
		bEMHandlerWriter.write(bems);
		
		BenefitEnrollmentMaintenanceDTO bemDTO = bems.get(0);
		assertNotNull("BemTypes are not null after calling the writer", bems);
		assertEquals("TransMsgId in bemDTO is 9999999999990L", 9999999999990L, bemDTO.getTransMsgId().longValue());
	}
	
	@Test
	public void testWrite_success_Non_Null_BatchId() throws Exception {
		expect(mockEPSValidationService.validateBEM(EasyMock.anyObject(EPSValidationRequest.class))).andReturn(null);
		replay(mockEPSValidationService);
		mockedPolicyDataService.saveBEM(EasyMock.anyObject(BenefitEnrollmentMaintenanceDTO.class));
		EasyMock.expectLastCall(); 
		replay(mockedPolicyDataService);

		mockedTxnMsgService.updateBatchTransMsg(EasyMock.anyObject(BenefitEnrollmentMaintenanceDTO.class), 
				EasyMock.anyObject(ProcessedToDbInd.class));
		EasyMock.expectLastCall().anyTimes();
		replay(mockedTxnMsgService);
		
		List<BenefitEnrollmentMaintenanceDTO> bems = createMockBem(9999999999990L);
		bems.get(0).setErrorList(new ArrayList<ErrorWarningLogDTO>());
		bems.get(0).getErrorList().add(new ErrorWarningLogDTO());
		bems.get(0).getErrorList().get(0).setBatchId(Long.valueOf("888888"));
		bems.get(0).setBatchId(Long.valueOf("888888"));
		bEMHandlerWriter.write(bems);
		
		BenefitEnrollmentMaintenanceDTO bemDTO = bems.get(0);
		assertNotNull("BemTypes are not null after calling the writer", bems);
		assertEquals("TransMsgId in bemDTO is 9999999999990L", 9999999999990L, bemDTO.getTransMsgId().longValue());
	}
	
	@Test
	public void testWrite_success_NullBems() throws Exception {
		assertNotNull("bEMHandlerWriter", bEMHandlerWriter);
		List<BenefitEnrollmentMaintenanceDTO> bems = null;
		bEMHandlerWriter.write(bems);
	}
	
	/**
	 * Test method for Save Transaction with validation errors
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.ErlBEMHandlerWriter#write(gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentMaintenanceDTO)}
	 * .
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testWrite_withErrors_success() throws Exception {
		expect(mockEPSValidationService.validateBEM(EasyMock.anyObject(EPSValidationRequest.class))).andReturn(null);
		replay(mockEPSValidationService);
		mockErrorWarningLogService.saveErrorWarningLogs(EasyMock.anyObject(List.class));
		EasyMock.expectLastCall(); 
		replay(mockErrorWarningLogService);

		mockedTxnMsgService.updateBatchTransMsg(EasyMock.anyObject(BenefitEnrollmentMaintenanceDTO.class), 
				EasyMock.anyObject(ProcessedToDbInd.class));
		EasyMock.expectLastCall(); 
		replay(mockedTxnMsgService);
		
		List<BenefitEnrollmentMaintenanceDTO> bems = createMockBemWithErrorMsgs(9999999999990L);
		bEMHandlerWriter.write(bems);
		
		BenefitEnrollmentMaintenanceDTO bemDTO = bems.get(0);
		assertNotNull("BenefitEnrollmentMaintenanceDTO list of bems", bems);
		assertEquals("bemDTO TransmsgId", 9999999999990L, bemDTO.getTransMsgId().longValue());
	}
	
	/**
	 * Test method for Updating status of Duplicate or earlier version coming in for processing
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.ErlBEMHandlerWriter#write(gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentMaintenanceDTO)}
	 * .
	 * @throws Exception
	 */
	@Test
	public void testWrite_success_IgnoredVersionStatus() throws Exception {
		expect(mockEPSValidationService.validateBEM(EasyMock.anyObject(EPSValidationRequest.class))).andReturn(null);
		replay(mockEPSValidationService);
		
		mockedTxnMsgService.updateBatchTransMsg(
				EasyMock.anyObject(BenefitEnrollmentMaintenanceDTO.class), 
				EasyMock.anyObject(ProcessedToDbInd.class), EasyMock.anyObject(EProdEnum.class));
		EasyMock.expectLastCall();
		
		replay(mockedTxnMsgService);
		
		List<BenefitEnrollmentMaintenanceDTO> bems = createMockBem(9999999999990L);
		bems.get(0).setIgnore(true);
		bEMHandlerWriter.write(bems);
		
		BenefitEnrollmentMaintenanceDTO bemDTO = bems.get(0);
		assertNotNull("BemTypes are not null after calling the writer", bems);
		assertEquals("TransMsgId in bemDTO is 9999999999990L", 9999999999990L, bemDTO.getTransMsgId().longValue());
	}
	
	/**
	 * Test method for Updating status of skipped version coming in for processing
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.ErlBEMHandlerWriter#write(gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentMaintenanceDTO)}
	 * .
	 * @throws Exception
	 */
	@Test
	public void testWrite_success_SkippedVersionStatus() throws Exception {
		expect(mockEPSValidationService.validateBEM(EasyMock.anyObject(EPSValidationRequest.class))).andReturn(null);
		replay(mockEPSValidationService);
		
		expect(mockedTxnMsgService.updateSkippedVersion(EasyMock.anyObject(BenefitEnrollmentMaintenanceDTO.class), 
				EasyMock.anyObject(ProcessedToDbInd.class))).andReturn(null);

		mockedTxnMsgService.updateBatchTransMsg(EasyMock.anyObject(BenefitEnrollmentMaintenanceDTO.class), EasyMock.anyObject(ProcessedToDbInd.class));
		EasyMock.expectLastCall();
		
		expect(mockedTxnMsgService.updateLaterVersions(EasyMock.anyObject(BenefitEnrollmentMaintenanceDTO.class), 
				EasyMock.anyObject(ProcessedToDbInd.class))).andReturn(null);
		
		replay(mockedTxnMsgService);
		
		List<BenefitEnrollmentMaintenanceDTO> bems = createMockBem(9999999999990L);
		bems.get(0).setVersionSkippedInPast(true);
		bEMHandlerWriter.write(bems);
		
		BenefitEnrollmentMaintenanceDTO bemDTO = bems.get(0);
		assertNotNull("BemTypes are not null after calling the writer", bems);
		assertEquals("TransMsgId in bemDTO is 9999999999990L", 9999999999990L, bemDTO.getTransMsgId().longValue());
	}
	
	/**
	 * Test method for Save Transaction with Exception thrown
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.ErlBEMHandlerWriter#write(gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentMaintenanceDTO)}
	 * .
	 * @throws Exception
	 */
	@Test(expected=Exception.class)
	public void testWrite_exception() throws Exception {
		expect(mockEPSValidationService.validateBEM(EasyMock.anyObject(EPSValidationRequest.class))).andReturn(null);
		replay(mockEPSValidationService);
		mockedTxnMsgService.updateBatchTransMsg(EasyMock.anyObject(BenefitEnrollmentMaintenanceDTO.class), EasyMock.anyObject(ProcessedToDbInd.class));
		EasyMock.expectLastCall().andThrow(new Exception("Some exception"));
		replay(mockedTxnMsgService);
		
		assertNotNull("BEMHandlerWriter bean", bEMHandlerWriter);
		bEMHandlerWriter.write(createMockBem(9999999999991L));
	}
	
	private List<BenefitEnrollmentMaintenanceDTO> createMockBem(Long transMsgId) {
		List<BenefitEnrollmentMaintenanceDTO> bems = new ArrayList<BenefitEnrollmentMaintenanceDTO>();
		
		BenefitEnrollmentMaintenanceDTO bem1  = new BenefitEnrollmentMaintenanceDTO();
		bem1.setTransMsgId(transMsgId);
		bem1.setFileInfoXml("<XML>FILEINFO</XML>");
		bem1.setExchangeTypeCd(ExchangeType.SBM.getValue());
		bem1.setBemXml("<XML>BEM</XML>");
		bems.add(bem1);
		
		return bems;
	}
	
	private List<BenefitEnrollmentMaintenanceDTO> createMockBemWithErrorMsgs(Long transMsgId) {
		List<BenefitEnrollmentMaintenanceDTO> bems = new ArrayList<BenefitEnrollmentMaintenanceDTO>();
		
		BenefitEnrollmentMaintenanceDTO bem1  = new BenefitEnrollmentMaintenanceDTO();
		bem1.setTransMsgId(transMsgId);
		bem1.setExchangeTypeCd(ExchangeType.FFM.getValue());
		
		List <ErrorWarningLogDTO> errorList = new ArrayList<ErrorWarningLogDTO>();
		
		ErrorWarningLogDTO e1 = new ErrorWarningLogDTO();
		e1.setBizAppAckErrorCd("E028");
		e1.setProcessingErrorCd("XXX");
		e1.setErrorWarningDetailedDesc("Test e1");
		errorList.add(e1);
		
		ErrorWarningLogDTO e2 = new ErrorWarningLogDTO();
		e2.setBizAppAckErrorCd("E028");
		e2.setProcessingErrorCd("XXX");
		e2.setErrorWarningDetailedDesc("Test e2");
		errorList.add(e2);
		
		bem1.setErrorList(errorList);
		bems.add(bem1);
		
		return bems;
	}
	
}
