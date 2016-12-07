package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm;

import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.N;
import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.Y;

import java.io.IOException;
import java.math.BigDecimal;
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
import org.springframework.test.util.ReflectionTestUtils;

import gov.cms.dsh.sbmr.FileAcceptanceRejection;
import gov.cms.dsh.sbmr.FileInformationType;
import gov.cms.dsh.sbmr.SBMIPROCSUMType;
import gov.cms.dsh.sbmr.SBMIPROCSUMType.FinalRecordsProcessedSummary;
import gov.hhs.cms.ff.fm.eps.dispatcher.EFTDispatchDriver;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMErrorWarningCode;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMErrorDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileInfo;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileProcessingDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMSummaryAndFileInfoDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMUpdateStatusRecordDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SbmResponseDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SBMFileCompositeDAO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SbmResponseCompositeDao;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SbmUpdateStatusDataService;
import junit.framework.TestCase;

@RunWith(JUnit4.class)
public class SBMResponseGeneratorTest extends TestCase {

	private SBMResponseGenerator sbmrGenerator;
	private SBMFileCompositeDAO fileCompositeDaoMock;
	private SbmResponseCompositeDao sbmResponseDaoMock;
	private EFTDispatchDriver mockEftDispatcher;
	private SbmUpdateStatusDataService mockUpdateStatusDataService;


	@Before
	public void setUp() {
		sbmrGenerator = new SBMResponseGenerator();

		fileCompositeDaoMock = Mockito.mock(SBMFileCompositeDAO.class);
		sbmResponseDaoMock = Mockito.mock(SbmResponseCompositeDao.class);
		mockUpdateStatusDataService = Mockito.mock(SbmUpdateStatusDataService.class);
		mockEftDispatcher = Mockito.mock(EFTDispatchDriver.class);

		sbmrGenerator.setEftDispatcher(mockEftDispatcher);
		sbmrGenerator.setFileCompositeDao(fileCompositeDaoMock);
		sbmrGenerator.setSbmResponseDao(sbmResponseDaoMock);
		sbmrGenerator.setUpdateStatusDataService(mockUpdateStatusDataService);

		sbmrGenerator.setEnvironmentCodeSuffix("T");
	}

	/**
	 * Basically, coverage only until refactor.
	 * @throws SQLException
	 * @throws IOException
	 * @throws JAXBException
	 */
	@Test
	public void test_generateSBMRWithPolicyErrors_HappyPath_RJC() throws SQLException, IOException, JAXBException {

		SBMErrorWarningCode expectedError = null;

		SbmResponseDTO mockSummaryDTO = new SbmResponseDTO();	
		mockSummaryDTO.setSbmSummaryAndFileInfo(new SBMSummaryAndFileInfoDTO());
		mockSummaryDTO.getSbmSummaryAndFileInfo().setErrorThresholdPercent(new BigDecimal("0.10"));
		mockSummaryDTO.getSbmSummaryAndFileInfo().setSbmFileStatusType(SBMFileStatus.IN_PROCESS);
		mockSummaryDTO.getSbmSummaryAndFileInfo().setCmsApprovalRequiredInd(Y);
		mockSummaryDTO.setTotalRecordsProcessed(new BigDecimal(100));
		mockSummaryDTO.setTotalRecordsRejected(new BigDecimal(60));

		SBMFileInfo mockFileInfo = new SBMFileInfo();
		mockFileInfo.setTradingPartnerId("CA01");
		mockSummaryDTO.getSbmSummaryAndFileInfo().getSbmFileInfoList().add(mockFileInfo);
		mockSummaryDTO.setXprErrorsExist(true);
		mockSummaryDTO.setXprWarningsExist(false);

		//for DAO calls		
		SbmResponseDTO mockSbmrDTO = new SbmResponseDTO();		
		mockSbmrDTO.setSbmr(createFileAcceptanceRejection());
		mockSbmrDTO.setSbmSummaryAndFileInfo(new SBMSummaryAndFileInfoDTO());		

		Long sbmFileProcSumId = Long.valueOf(9999003);
		Long jobExecId = Long.valueOf(9003);

		Mockito.when(sbmResponseDaoMock.getSummary(Mockito.anyLong())).thenReturn(mockSummaryDTO);

		Mockito.doNothing().when(mockUpdateStatusDataService).executeApproval(Mockito.anyLong(), Mockito.anyLong());

		Mockito.when(sbmResponseDaoMock.generateSBMR(Mockito.anyLong(), Mockito.anyLong())).thenReturn(mockSbmrDTO);

		Mockito.when(mockEftDispatcher.saveDispatchContent(Mockito.anyString().getBytes(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString())).thenReturn(Mockito.anyLong());

		sbmrGenerator.generateSBMRWithPolicyErrors(jobExecId, sbmFileProcSumId);

		Assert.assertNotNull("SbmResponseDTO should not be null", mockSbmrDTO.getSbmr());

		List<FileInformationType> actualList = mockSbmrDTO.getSbmr().getSBMIFileInfo();

		assertEquals("sbmFileInfoList list size", 1, actualList.size());

		FileInformationType actual = actualList.get(0);

		assertEquals("File Error", expectedError, actual.getFileError());
		// This is pointless now, since status update is db call.
		// TODO once method is further  refactored.
		//assertEquals("File Status", expectedStatus.getName(), actual.getFileProcessingStatus());		
	}


	/**
	 * Basically, coverage only until refactor.
	 * @throws SQLException
	 * @throws IOException
	 * @throws JAXBException
	 */
	@Test
	public void test_generateSBMRWithPolicyErrors_HappyPath() throws SQLException, IOException, JAXBException {

		SBMErrorWarningCode expectedError = null;

		SbmResponseDTO mockSummaryDTO = new SbmResponseDTO();	
		mockSummaryDTO.setSbmSummaryAndFileInfo(new SBMSummaryAndFileInfoDTO());
		mockSummaryDTO.getSbmSummaryAndFileInfo().setErrorThresholdPercent(new BigDecimal("99.99"));
		mockSummaryDTO.getSbmSummaryAndFileInfo().setSbmFileStatusType(SBMFileStatus.IN_PROCESS);
		mockSummaryDTO.getSbmSummaryAndFileInfo().setCmsApprovalRequiredInd(N);
		mockSummaryDTO.setTotalRecordsProcessed(new BigDecimal(100));
		mockSummaryDTO.setTotalRecordsRejected(new BigDecimal(3));

		SBMFileInfo mockFileInfo = new SBMFileInfo();
		mockFileInfo.setTradingPartnerId("CA01");
		mockSummaryDTO.getSbmSummaryAndFileInfo().getSbmFileInfoList().add(mockFileInfo);
		mockSummaryDTO.setXprErrorsExist(true);
		mockSummaryDTO.setXprWarningsExist(false);

		//for DAO calls		
		SbmResponseDTO mockSbmrDTO = new SbmResponseDTO();		
		mockSbmrDTO.setSbmr(createFileAcceptanceRejection());
		mockSbmrDTO.setSbmSummaryAndFileInfo(new SBMSummaryAndFileInfoDTO());		

		Long sbmFileProcSumId = Long.valueOf(9999002);
		Long jobExecId = Long.valueOf(9002);

		Mockito.when(sbmResponseDaoMock.getSummary(Mockito.anyLong())).thenReturn(mockSummaryDTO);

		Mockito.doNothing().when(mockUpdateStatusDataService).executeApproval(Mockito.anyLong(), Mockito.anyLong());

		Mockito.when(sbmResponseDaoMock.generateSBMR(Mockito.anyLong(), Mockito.anyLong())).thenReturn(mockSbmrDTO);

		Mockito.when(mockEftDispatcher.saveDispatchContent(Mockito.anyString().getBytes(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString())).thenReturn(Mockito.anyLong());

		sbmrGenerator.generateSBMRWithPolicyErrors(jobExecId, sbmFileProcSumId);

		Assert.assertNotNull("SbmResponseDTO should not be null", mockSbmrDTO.getSbmr());

		List<FileInformationType> actualList = mockSbmrDTO.getSbmr().getSBMIFileInfo();

		assertEquals("sbmFileInfoList list size", 1, actualList.size());

		FileInformationType actual = actualList.get(0);

		assertEquals("File Error", expectedError, actual.getFileError());
		// This is pointless now, since status update is db call.
		// TODO once method is further  refactored.
		//assertEquals("File Status", expectedStatus.getName(), actual.getFileProcessingStatus());		
	}


	@Test
	public void testUpdateStatusSBMRGeneration() throws SQLException, IOException, JAXBException {

		//for DAO calls		
		SbmResponseDTO dto = new SbmResponseDTO();		
		dto.setXprErrorsExist(true);
		dto.setSbmr(createFileAcceptanceRejection());
		dto.getSbmr().getSBMIPROCSUM().getFinalRecordsProcessedSummary().setTotalRecordsProcessed(100);
		dto.getSbmr().getSBMIPROCSUM().getFinalRecordsProcessedSummary().setTotalRecordsRejected(3);

		SBMSummaryAndFileInfoDTO summaryDto = new SBMSummaryAndFileInfoDTO();
		summaryDto.setErrorThresholdPercent(new BigDecimal(5));
		summaryDto.setSbmFileStatusType(SBMFileStatus.ACCEPTED);

		SBMFileInfo fileInfo = new SBMFileInfo();
		fileInfo.setTradingPartnerId("CA01");
		summaryDto.getSbmFileInfoList().add(fileInfo);

		dto.setSbmSummaryAndFileInfo(summaryDto);

		Mockito.when(sbmResponseDaoMock.generateSBMR(Mockito.anyLong(), Mockito.anyLong())).thenReturn(dto);

		SBMUpdateStatusRecordDTO updateStatusDto = new SBMUpdateStatusRecordDTO();
		updateStatusDto.setNewFileSatus(SBMFileStatus.APPROVED);
		updateStatusDto.setSbmFileProcSumId(9999L);

		Long jobId = 88888L;

		sbmrGenerator.generateSBMRForUpdateStatus(updateStatusDto, jobId);

		Assert.assertNotNull("Should not be null", dto.getSbmr());
	}


	@Test
	public void test_generateSBMSForAllSBMInfos() throws JAXBException, SQLException, IOException {

		SBMSummaryAndFileInfoDTO mockSummaryDTO = new SBMSummaryAndFileInfoDTO();
		mockSummaryDTO.setErrorThresholdPercent(new BigDecimal("99.99"));
		mockSummaryDTO.setSbmFileStatusType(SBMFileStatus.IN_PROCESS);
		mockSummaryDTO.setCmsApprovalRequiredInd(Y);

		SBMFileInfo mockFileInfo = new SBMFileInfo();
		mockFileInfo.setTradingPartnerId("CA01");
		mockSummaryDTO.getSbmFileInfoList().add(mockFileInfo);

		List<SBMErrorDTO> errorList = new ArrayList<SBMErrorDTO>();
		SBMErrorDTO errorDTO = new SBMErrorDTO();
		errorDTO.setSbmErrorWarningTypeCd(SBMErrorWarningCode.ER_001.getCode());
		errorDTO.setElementInErrorNm("MemberInformation");
		errorDTO.getAdditionalErrorInfoList().add("MemberInformation tag is missing");
		errorDTO.getAdditionalErrorInfoList().add("Minimum occurance violated");
		errorList.add(errorDTO);

		SBMErrorDTO errorDTO2 = new SBMErrorDTO();
		errorDTO2.setSbmErrorWarningTypeCd(SBMErrorWarningCode.ER_003.getCode());
		errorDTO2.setElementInErrorNm("IssuerAssignedPolicyID");
		errorDTO2.getAdditionalErrorInfoList().add("IssuerAssignedPolicyID Maximum occurrence violated");
		errorList.add(errorDTO2);

		sbmrGenerator.generateSBMSForAllSBMInfos(mockSummaryDTO, errorList);

		Assert.assertNotNull("mockSummaryDTO", mockSummaryDTO);
	}

	
	@Test
	public void test_generateSBMS() throws JAXBException, SQLException, IOException {

		SBMFileProcessingDTO fileProcDto = new SBMFileProcessingDTO();

		fileProcDto.setSbmFileStatusType(SBMFileStatus.IN_PROCESS);

		SBMFileInfo mockFileInfo = new SBMFileInfo();
		mockFileInfo.setTradingPartnerId("CA01");
		fileProcDto.setSbmFileInfo(mockFileInfo);

		List<SBMErrorDTO> errorList = new ArrayList<SBMErrorDTO>();
		SBMErrorDTO errorDTO = new SBMErrorDTO();
		errorDTO.setSbmErrorWarningTypeCd(SBMErrorWarningCode.ER_001.getCode());
		errorDTO.setElementInErrorNm("MemberInformation");
		errorDTO.getAdditionalErrorInfoList().add("MemberInformation tag is missing");
		errorDTO.getAdditionalErrorInfoList().add("Minimum occurance violated");
		errorList.add(errorDTO);

		SBMErrorDTO errorDTO2 = new SBMErrorDTO();
		errorDTO2.setSbmErrorWarningTypeCd(SBMErrorWarningCode.ER_003.getCode());
		errorDTO2.setElementInErrorNm("IssuerAssignedPolicyID");
		errorDTO2.getAdditionalErrorInfoList().add("IssuerAssignedPolicyID Maximum occurrence violated");
		errorList.add(errorDTO2);

		fileProcDto.getErrorList().addAll(errorList);

		sbmrGenerator.generateSBMS(fileProcDto);

		Assert.assertNotNull("fileProcDto", fileProcDto);
	}
	
	@Test
	public void test_approveAcceptedSummaries_APP() {
		
		SBMFileStatus expectedStatus = SBMFileStatus.APPROVED;
		
		Mockito.doNothing().when(mockUpdateStatusDataService).executeApproval(Mockito.anyLong(), Mockito.anyLong());
		
		Long jobId = Long.valueOf("222");
		Long sbmFileProcSumId = Long.valueOf("2222");
		SBMFileStatus fileStatus = SBMFileStatus.ACCEPTED;
		
		SBMFileStatus actualStatus = (SBMFileStatus) ReflectionTestUtils.invokeMethod(sbmrGenerator, "approveAcceptedSummaries", jobId, sbmFileProcSumId, fileStatus);

		assertEquals("SBMFileStatus", expectedStatus, actualStatus);
	}
	
	@Test
	public void test_approveAcceptedSummaries_APE() {
		
		SBMFileStatus expectedStatus = SBMFileStatus.APPROVED_WITH_ERRORS;
		
		Mockito.doNothing().when(mockUpdateStatusDataService).executeApproval(Mockito.anyLong(), Mockito.anyLong());
		
		Long jobId = Long.valueOf("222");
		Long sbmFileProcSumId = Long.valueOf("2222");
		SBMFileStatus fileStatus = SBMFileStatus.ACCEPTED_WITH_ERRORS;
		
		SBMFileStatus actualStatus = (SBMFileStatus) ReflectionTestUtils.invokeMethod(sbmrGenerator, "approveAcceptedSummaries", jobId, sbmFileProcSumId, fileStatus);

		assertEquals("SBMFileStatus", expectedStatus, actualStatus);
	}
	
	@Test
	public void test_approveAcceptedSummaries_APW() {
		
		SBMFileStatus expectedStatus = SBMFileStatus.APPROVED_WITH_WARNINGS;
		
		Mockito.doNothing().when(mockUpdateStatusDataService).executeApproval(Mockito.anyLong(), Mockito.anyLong());
		
		Long jobId = Long.valueOf("222");
		Long sbmFileProcSumId = Long.valueOf("2222");
		SBMFileStatus fileStatus = SBMFileStatus.ACCEPTED_WITH_WARNINGS;
		
		SBMFileStatus actualStatus = (SBMFileStatus) ReflectionTestUtils.invokeMethod(sbmrGenerator, "approveAcceptedSummaries", jobId, sbmFileProcSumId, fileStatus);

		assertEquals("SBMFileStatus", expectedStatus, actualStatus);
	}
	
	@Test
	public void test_approveAcceptedSummaries_RJC() {
		
		SBMFileStatus expectedStatus = SBMFileStatus.REJECTED;
		
		Mockito.doNothing().when(mockUpdateStatusDataService).executeApproval(Mockito.anyLong(), Mockito.anyLong());
		
		Long jobId = Long.valueOf("222");
		Long sbmFileProcSumId = Long.valueOf("2222");
		SBMFileStatus fileStatus = SBMFileStatus.REJECTED;
		
		SBMFileStatus actualStatus = (SBMFileStatus) ReflectionTestUtils.invokeMethod(sbmrGenerator, "approveAcceptedSummaries", jobId, sbmFileProcSumId, fileStatus);
		
		assertEquals("SBMFileStatus", expectedStatus, actualStatus);
	}
	
	@Test
	public void test_validateErrorThreshold_ACC() {
		
		SBMFileStatus expectedStatus = SBMFileStatus.ACCEPTED;
		SbmResponseDTO dto = new SbmResponseDTO();
		dto.setSbmSummaryAndFileInfo(new SBMSummaryAndFileInfoDTO());
		dto.getSbmSummaryAndFileInfo().setErrorThresholdPercent(new BigDecimal("10.0"));
		dto.setTotalRecordsProcessed(new BigDecimal("10000000"));
		dto.setTotalRecordsRejected(new BigDecimal("1"));
		
		SBMFileStatus actualStatus = (SBMFileStatus) ReflectionTestUtils.invokeMethod(sbmrGenerator, "validateErrorThreshold", dto);
		
		assertEquals("SBMFileStatus", expectedStatus, actualStatus);
	}
	
	@Test
	public void test_validateErrorThreshold_RJC() {
		
		SBMFileStatus expectedStatus = SBMFileStatus.REJECTED;
		SbmResponseDTO dto = new SbmResponseDTO();
		dto.setSbmSummaryAndFileInfo(new SBMSummaryAndFileInfoDTO());
		dto.getSbmSummaryAndFileInfo().setErrorThresholdPercent(new BigDecimal("50"));
		dto.setTotalRecordsProcessed(new BigDecimal("100"));
		dto.setTotalRecordsRejected(new BigDecimal("51"));
		
		SBMFileStatus actualStatus = (SBMFileStatus) ReflectionTestUtils.invokeMethod(sbmrGenerator, "validateErrorThreshold", dto);
		
		assertEquals("SBMFileStatus", expectedStatus, actualStatus);
	}
	
	@Test
	public void test_validateErrorThreshold_ACW() {
		
		SBMFileStatus expectedStatus = SBMFileStatus.ACCEPTED_WITH_WARNINGS;
		SbmResponseDTO dto = new SbmResponseDTO();
		dto.setSbmSummaryAndFileInfo(new SBMSummaryAndFileInfoDTO());
		dto.getSbmSummaryAndFileInfo().setErrorThresholdPercent(new BigDecimal("0"));
		dto.setTotalRecordsProcessed(new BigDecimal("0"));
		dto.setTotalRecordsRejected(new BigDecimal("0"));
		dto.setXprWarningsExist(true);
		
		SBMFileStatus actualStatus = (SBMFileStatus) ReflectionTestUtils.invokeMethod(sbmrGenerator, "validateErrorThreshold", dto);
		
		assertEquals("SBMFileStatus", expectedStatus, actualStatus);
	}
	
	@Test
	public void test_validateErrorThreshold_ACE() {
		
		SBMFileStatus expectedStatus = SBMFileStatus.ACCEPTED_WITH_ERRORS;
		SbmResponseDTO dto = new SbmResponseDTO();
		dto.setSbmSummaryAndFileInfo(new SBMSummaryAndFileInfoDTO());
		dto.getSbmSummaryAndFileInfo().setErrorThresholdPercent(null);
		dto.setTotalRecordsProcessed(new BigDecimal("100"));
		dto.setTotalRecordsRejected(new BigDecimal("0"));
		dto.setXprErrorsExist(true);
		
		SBMFileStatus actualStatus = (SBMFileStatus) ReflectionTestUtils.invokeMethod(sbmrGenerator, "validateErrorThreshold", dto);
		
		assertEquals("SBMFileStatus", expectedStatus, actualStatus);
	}	
	
	@Test
	public void test_logFileMsg() {
		
		String funcType = SBMConstants.FUNCTION_CODE_SBMS;
		Long sbmFileProcSumId =  Long.valueOf(9999003);
		Long physicalDocId =  Long.valueOf(8);
		String fileName = "YYMMDD.P";
		ReflectionTestUtils.invokeMethod(sbmrGenerator, "logFileMsg", funcType, sbmFileProcSumId, physicalDocId, fileName);
		assertEquals("Something (this is a test for the log msg)", true, true);
	}
	
	
	private FileAcceptanceRejection createFileAcceptanceRejection() {
		FileAcceptanceRejection sbmr = new FileAcceptanceRejection();

		sbmr.setSBMIPROCSUM(new SBMIPROCSUMType());		
		sbmr.getSBMIPROCSUM().setFinalRecordsProcessedSummary(new FinalRecordsProcessedSummary());
		sbmr.getSBMIFileInfo().add(new FileInformationType());

		return sbmr;
	}

}
