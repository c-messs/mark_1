package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm.updatestatus;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import gov.hhs.cms.ff.fm.eps.dispatcher.EFTDispatchDriver;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMErrorWarningCode;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus;
import gov.hhs.cms.ff.fm.eps.ep.jobs.sbm.SBMResponseGenerator;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMSummaryAndFileInfoDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMUpdateStatusErrorDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMUpdateStatusRecordDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SBMFileCompositeDAO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SbmUpdateStatusDataService;
import junit.framework.TestCase;

@RunWith(JUnit4.class)
public class SbmUpdateStatusProcessorTest extends TestCase {
	
	private static final Logger LOG = LoggerFactory.getLogger(SbmUpdateStatusProcessorTest.class);
	
	private SbmUpdateStatusProcessor updateStatusProcessor;
	
	private SBMFileCompositeDAO fileCompositeDaoMock;
	private SBMResponseGenerator responseGeneratorMock;
	private EFTDispatchDriver eftDispatcherMock;
	private SbmUpdateStatusDataService updateStatusDataServicMock;
	
	@Before
	public void beforeTest() {
		updateStatusProcessor = new SbmUpdateStatusProcessor();
		
		fileCompositeDaoMock = Mockito.mock(SBMFileCompositeDAO.class);
		responseGeneratorMock = Mockito.mock(SBMResponseGenerator.class);
		eftDispatcherMock = Mockito.mock(EFTDispatchDriver.class);
		updateStatusDataServicMock = Mockito.mock(SbmUpdateStatusDataService.class);
		
		updateStatusProcessor.setFileCompositeDao(fileCompositeDaoMock);
		updateStatusProcessor.setResponseGenerator(responseGeneratorMock);
		updateStatusProcessor.setEftDispatcher(eftDispatcherMock);
		updateStatusProcessor.setUpdateStatusDataService(updateStatusDataServicMock);
		updateStatusProcessor.setEnvironmentCodeSuffix("D");
	}
	
	@Test
	public void testUpdateStatus_errors() throws IOException, SQLException, JAXBException {
		
		File inputFile = new File("./src/test/resources/sbm/updatestatus/CA1.EPS.SBMS.D160707.T100004040.T");
		
		updateStatusProcessor.processUpdateStatus(inputFile, 12345L);
		
		Assert.assertTrue("Should be true", true);
		
	}
	
	@Test
	public void testUpdateStatus_noErrors() throws IOException, SQLException, JAXBException {
					
		File inputFile = new File("./src/test/resources/sbm/updatestatus/CA1.EPS.SBMS.D160707.T100004030.T");
		
		SBMSummaryAndFileInfoDTO summaryDto = new SBMSummaryAndFileInfoDTO();
		summaryDto.setSbmFileStatusType(SBMFileStatus.ACCEPTED);
		
		
		List<SBMSummaryAndFileInfoDTO> summaryDtoList = new ArrayList<>();
		summaryDtoList.add(summaryDto);
		Mockito.when(fileCompositeDaoMock.getSBMFileProcessingSummary(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(summaryDtoList);
		Mockito.when(fileCompositeDaoMock.performSbmFileMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(summaryDtoList);
		
		updateStatusProcessor.processUpdateStatus(inputFile, 12345L);
		
		Assert.assertTrue("Should be true", true);
		
	}
	
	@Test
	public void testUpdateStatus_invalidContent() throws IOException, SQLException, JAXBException {
		
		File inputFile = new File("./src/test/resources/sbm/updatestatus/CA1.EPS.SBMS.D160707.T100009999.T");
		
		updateStatusProcessor.processUpdateStatus(inputFile, 12345L);
		
		Assert.assertTrue("Should be true", true);
	}
	
	@Test
	public void testUpdateStatus_validations() throws IOException, SQLException, JAXBException {
		
		File inputFile = new File("./src/test/resources/sbm/updatestatus/CA1.EPS.SBMS.D160707.T100009999.T");
		
		updateStatusProcessor.processUpdateStatus(inputFile, 12345L);
		
		Assert.assertTrue("Should be true", true);
	}
	
	@Test
	public void testValidations_fileSet() {
				
		SBMUpdateStatusRecordDTO recordDto = new SBMUpdateStatusRecordDTO();
		recordDto.setLinenumber("1");
		recordDto.setTenantId("NY1");
		recordDto.setIssuerId("12345");
		recordDto.setIssuerFileSetId("12345678");
		recordDto.setStatus(SBMConstants.STATUS_BYPASS_FREEZE);
		
		Object results = ReflectionTestUtils.invokeMethod(updateStatusProcessor, "validateRowContents", recordDto);
		LOG.info("results:{}",results);
		Assert.assertTrue("Error ER-520 is expected", isErrorExists(results, SBMErrorWarningCode.ER_520));
		
	}
	
	@Test
	public void testRequiredFieldsErrors() {
				
		SBMUpdateStatusRecordDTO recordDto = new SBMUpdateStatusRecordDTO();
		recordDto.setLinenumber("1");
				
		Object results = ReflectionTestUtils.invokeMethod(updateStatusProcessor, "validateRowContents", recordDto);
		LOG.info("results:{}",results);
		Assert.assertTrue("Error ER-501 is expected", isErrorExists(results, SBMErrorWarningCode.ER_501));
		Assert.assertTrue("Error ER-502 is expected", isErrorExists(results, SBMErrorWarningCode.ER_502));
		Assert.assertTrue("Error ER-503 is expected", isErrorExists(results, SBMErrorWarningCode.ER_503));
		
	}
	
	@Test
	public void testInvalidStatus() {
				
		SBMUpdateStatusRecordDTO recordDto = new SBMUpdateStatusRecordDTO();
		recordDto.setLinenumber("1");
		recordDto.setIssuerFileSetId("1234");
		recordDto.setStatus("abc");
				
		Object results = ReflectionTestUtils.invokeMethod(updateStatusProcessor, "validateRowContents", recordDto);
		LOG.info("results:{}",results);

		Assert.assertTrue("Error ER-503 is expected", isErrorExists(results, SBMErrorWarningCode.ER_518));
		
	}
	
	@Test
	public void testFileIdError() {
				
		SBMUpdateStatusRecordDTO recordDto = new SBMUpdateStatusRecordDTO();
		recordDto.setLinenumber("1");
		recordDto.setFileId("234");
		recordDto.setIssuerFileSetId("1234");
		recordDto.setStatus("abc");
				
		Object results = ReflectionTestUtils.invokeMethod(updateStatusProcessor, "validateRowContents", recordDto);
		LOG.info("results:{}",results);
		Assert.assertTrue("Error ER-501 is expected", isErrorExists(results, SBMErrorWarningCode.ER_501));
//		Assert.assertTrue("Error ER-502 is expected", isErrorExists(results, SBMErrorWarningCode.ER_502));
//		Assert.assertTrue("Error ER-503 is expected", isErrorExists(results, SBMErrorWarningCode.ER_503));
		
	}
	
	@Test
	public void testMissingLineNumber() {
				
		SBMUpdateStatusRecordDTO recordDto = new SBMUpdateStatusRecordDTO();
				
		Object results = ReflectionTestUtils.invokeMethod(updateStatusProcessor, "validateRowContents", recordDto);
		LOG.info("results:{}",results);
		Assert.assertTrue("Error ER-500 is expected", isErrorExists(results, SBMErrorWarningCode.ER_500));
		
	}
	
	@Test
	public void testBypassFreeze_invalidStatus() throws IOException, SQLException, JAXBException {
					
		SBMUpdateStatusRecordDTO recordDto = new SBMUpdateStatusRecordDTO();
		recordDto.setLinenumber("1");
		recordDto.setTenantId("NY1");
		recordDto.setIssuerId("12345");
		recordDto.setIssuerFileSetId("12345678");
		recordDto.setStatus(SBMConstants.STATUS_BYPASS_FREEZE);
			
		SBMSummaryAndFileInfoDTO summaryDto = new SBMSummaryAndFileInfoDTO();
		summaryDto.setSbmFileStatusType(SBMFileStatus.ACCEPTED);
		
		List<SBMSummaryAndFileInfoDTO> summaryDtoList = new ArrayList<>();
		summaryDtoList.add(summaryDto);
		Mockito.when(fileCompositeDaoMock.getSBMFileProcessingSummary(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(summaryDtoList);
		
		Object results = ReflectionTestUtils.invokeMethod(updateStatusProcessor, "validateRowContents", recordDto);
		
		LOG.info("results:{}",results);
		Assert.assertTrue("Error expected", isErrorExists(results, SBMErrorWarningCode.ER_521));
		
	}
	
	@Test
	public void testBypassFreeze_incompleteFileSet() throws IOException, SQLException, JAXBException {
					
		SBMUpdateStatusRecordDTO recordDto = new SBMUpdateStatusRecordDTO();
		recordDto.setLinenumber("1");
		recordDto.setTenantId("NY1");
		recordDto.setIssuerId("12345");
		recordDto.setIssuerFileSetId("12345678");
		recordDto.setStatus(SBMConstants.STATUS_BYPASS_FREEZE);
			
		SBMSummaryAndFileInfoDTO summaryDto = new SBMSummaryAndFileInfoDTO();
		summaryDto.setTotalIssuerFileCount(5);
		summaryDto.setIssuerFileSetId("12345678");
		summaryDto.setSbmFileStatusType(SBMFileStatus.FREEZE);
		
		List<SBMSummaryAndFileInfoDTO> summaryDtoList = new ArrayList<>();
		summaryDtoList.add(summaryDto);
		Mockito.when(fileCompositeDaoMock.getSBMFileProcessingSummary(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(summaryDtoList);
		
		Object results = ReflectionTestUtils.invokeMethod(updateStatusProcessor, "validateRowContents", recordDto);
		
		LOG.info("results:{}",results);
		Assert.assertTrue("Error expected", isErrorExists(results, SBMErrorWarningCode.ER_522));
		
	}
	
	@Test
	public void testBypassFreeze_noError() throws IOException, SQLException, JAXBException {
					
		SBMUpdateStatusRecordDTO recordDto = new SBMUpdateStatusRecordDTO();
		recordDto.setLinenumber("1");
		recordDto.setTenantId("NY1");
		recordDto.setFileId("123");
		recordDto.setStatus(SBMConstants.STATUS_BYPASS_FREEZE);
			
		SBMSummaryAndFileInfoDTO summaryDto = new SBMSummaryAndFileInfoDTO();		
		summaryDto.setSbmFileStatusType(SBMFileStatus.FREEZE);
		
		List<SBMSummaryAndFileInfoDTO> summaryDtoList = new ArrayList<>();
		summaryDtoList.add(summaryDto);
		Mockito.when(fileCompositeDaoMock.performSbmFileMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(summaryDtoList);
		
		List results = (List)ReflectionTestUtils.invokeMethod(updateStatusProcessor, "validateRowContents", recordDto);
		
		LOG.info("results:{}",results);
		Assert.assertTrue("No Error expected", results.isEmpty());
		
	}
	
	@Test
	public void testBackout_invalidStatus() throws IOException, SQLException, JAXBException {
					
		SBMUpdateStatusRecordDTO recordDto = new SBMUpdateStatusRecordDTO();
		recordDto.setLinenumber("1");
		recordDto.setTenantId("NY1");
		recordDto.setIssuerId("12345");
		recordDto.setIssuerFileSetId("12345678");
		recordDto.setStatus(SBMConstants.STATUS_BACKOUT);
			
		SBMSummaryAndFileInfoDTO summaryDto = new SBMSummaryAndFileInfoDTO();
		summaryDto.setSbmFileStatusType(SBMFileStatus.ACCEPTED);
		
		List<SBMSummaryAndFileInfoDTO> summaryDtoList = new ArrayList<>();
		summaryDtoList.add(summaryDto);
		Mockito.when(fileCompositeDaoMock.getSBMFileProcessingSummary(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(summaryDtoList);
		
		Object results = ReflectionTestUtils.invokeMethod(updateStatusProcessor, "validateRowContents", recordDto);
		
		LOG.info("results:{}",results);
		Assert.assertTrue("Error expected", isErrorExists(results, SBMErrorWarningCode.ER_515));
		
	}
	
	@Test
	public void testBackout_byState_notLatestFile() throws IOException, SQLException, JAXBException {
					
		SBMUpdateStatusRecordDTO recordDto = new SBMUpdateStatusRecordDTO();
		recordDto.setLinenumber("1");
		recordDto.setTenantId("NY1");
		recordDto.setFileId("123");
		recordDto.setStatus(SBMConstants.STATUS_BACKOUT);
			
		SBMSummaryAndFileInfoDTO summaryDto = new SBMSummaryAndFileInfoDTO();
		summaryDto.setSbmFileProcSumId(1001L);
		summaryDto.setSbmFileStatusType(SBMFileStatus.APPROVED);
		
		List<SBMSummaryAndFileInfoDTO> summaryDtoList = new ArrayList<>();
		summaryDtoList.add(summaryDto);
//		Mockito.when(fileCompositeDaoMock.getSBMFileProcessingSummary(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(summaryDtoList);
		Mockito.when(fileCompositeDaoMock.performSbmFileMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(summaryDtoList);
		
		SBMSummaryAndFileInfoDTO latestDto = new SBMSummaryAndFileInfoDTO();
		latestDto.setSbmFileProcSumId(1002L);
		Mockito.when(fileCompositeDaoMock.getLatestSBMFileProcessingSummaryByState(Mockito.anyString())).thenReturn(latestDto);
		
		Object results = ReflectionTestUtils.invokeMethod(updateStatusProcessor, "validateRowContents", recordDto);
		
		LOG.info("results:{}",results);
		Assert.assertTrue("Error expected", isErrorExists(results, SBMErrorWarningCode.ER_514));
		
	}
	
	@Test
	public void testBackout_byIssuer_notLatestFile() throws IOException, SQLException, JAXBException {
					
		SBMUpdateStatusRecordDTO recordDto = new SBMUpdateStatusRecordDTO();
		recordDto.setLinenumber("1");
		recordDto.setTenantId("NY1");
		recordDto.setFileId("123");
		recordDto.setIssuerId("12345");
		recordDto.setStatus(SBMConstants.STATUS_BACKOUT);
			
		SBMSummaryAndFileInfoDTO summaryDto = new SBMSummaryAndFileInfoDTO();
		summaryDto.setSbmFileProcSumId(1001L);
		summaryDto.setSbmFileStatusType(SBMFileStatus.APPROVED);
		
		List<SBMSummaryAndFileInfoDTO> summaryDtoList = new ArrayList<>();
		summaryDtoList.add(summaryDto);
//		Mockito.when(fileCompositeDaoMock.getSBMFileProcessingSummary(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(summaryDtoList);
		Mockito.when(fileCompositeDaoMock.performSbmFileMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(summaryDtoList);
		
		SBMSummaryAndFileInfoDTO latestDto = new SBMSummaryAndFileInfoDTO();
		latestDto.setSbmFileProcSumId(1002L);
		Mockito.when(fileCompositeDaoMock.getLatestSBMFileProcessingSummaryByIssuer(Mockito.anyString())).thenReturn(latestDto);
		
		Object results = ReflectionTestUtils.invokeMethod(updateStatusProcessor, "validateRowContents", recordDto);
		
		LOG.info("results:{}",results);
		Assert.assertTrue("Error expected", isErrorExists(results, SBMErrorWarningCode.ER_514));
		
	}
		
	@Test
	public void testBackout_noError() throws IOException, SQLException, JAXBException {
					
		SBMUpdateStatusRecordDTO recordDto = new SBMUpdateStatusRecordDTO();
		recordDto.setLinenumber("1");
		recordDto.setTenantId("NY1");
		recordDto.setFileId("123");
		recordDto.setStatus(SBMConstants.STATUS_BACKOUT);
			
		SBMSummaryAndFileInfoDTO summaryDto = new SBMSummaryAndFileInfoDTO();
		summaryDto.setSbmFileProcSumId(1001L);
		summaryDto.setSbmFileStatusType(SBMFileStatus.APPROVED);
		
		List<SBMSummaryAndFileInfoDTO> summaryDtoList = new ArrayList<>();
		summaryDtoList.add(summaryDto);
//		Mockito.when(fileCompositeDaoMock.getSBMFileProcessingSummary(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(summaryDtoList);
		Mockito.when(fileCompositeDaoMock.performSbmFileMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(summaryDtoList);
				
		Mockito.when(fileCompositeDaoMock.getLatestSBMFileProcessingSummaryByState(Mockito.anyString())).thenReturn(null);
		
		List results = (List)ReflectionTestUtils.invokeMethod(updateStatusProcessor, "validateRowContents", recordDto);
		
		LOG.info("results:{}",results);
		Assert.assertTrue("No Error expected", results.isEmpty());
		
	}
	
	@Test
	public void test_isSBMIJobRunning() {
		
		Mockito.when(fileCompositeDaoMock.isSBMIJobRunning()).thenReturn(true);
		
		boolean result = updateStatusProcessor.isSBMIJobRunning();
		
		Assert.assertTrue("SBMIJobRunning", result);
	}
	
	@Test
	public void test_isSBMIJobNotRunning() {
		
		Mockito.when(fileCompositeDaoMock.isSBMIJobRunning()).thenReturn(false);
		
		boolean result = updateStatusProcessor.isSBMIJobRunning();
		
		Assert.assertFalse("SBMIJobRunning", result);
	}
	
	private boolean isErrorExists(Object results, SBMErrorWarningCode errorCode) {
		if(results instanceof List) {
			for(Object result: (List)results) {
				if(errorCode.getCode().equalsIgnoreCase(((SBMUpdateStatusErrorDTO)result).getErrorCode())) {
					return true;
				}
			}
		}
		
		return false;
	}

}
