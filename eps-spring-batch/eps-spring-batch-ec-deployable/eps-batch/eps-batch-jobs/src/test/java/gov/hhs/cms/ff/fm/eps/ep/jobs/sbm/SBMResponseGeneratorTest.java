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
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.cms.dsh.sbmr.FileAcceptanceRejection;
import gov.cms.dsh.sbmr.FileInformationType;
import gov.cms.dsh.sbmr.SBMIPROCSUMType;
import gov.cms.dsh.sbmr.SBMIPROCSUMType.FinalRecordsProcessedSummary;
import gov.hhs.cms.ff.fm.eps.dispatcher.EFTDispatchDriver;
import gov.hhs.cms.ff.fm.eps.dispatcher.EFTDispatcherDAO;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMErrorWarningCode;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus;
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

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/sbmi-batch-context.xml")
public class SBMResponseGeneratorTest extends TestCase {

	private SBMResponseGenerator sbmrGenerator;
	private SBMFileCompositeDAO fileCompositeDaoMock;
	private SbmResponseCompositeDao sbmResponseDaoMock;
	private EFTDispatchDriver eftDispatcher;
	private SbmUpdateStatusDataService mockUpdateStatusDataService;

	@Autowired
	private EFTDispatcherDAO eftDispatcherDao;

	@Before
	public void setUp() {
		sbmrGenerator = new SBMResponseGenerator();

		fileCompositeDaoMock = Mockito.mock(SBMFileCompositeDAO.class);
		sbmResponseDaoMock = Mockito.mock(SbmResponseCompositeDao.class);
		mockUpdateStatusDataService = Mockito.mock(SbmUpdateStatusDataService.class);

		eftDispatcher = new EFTDispatchDriver();
		eftDispatcher.setBatchProps("/SBMFiles/outbound/");
		eftDispatcher.setEftDispatcherDao(eftDispatcherDao);


		sbmrGenerator.setEftDispatcher(eftDispatcher);
		sbmrGenerator.setFileCompositeDao(fileCompositeDaoMock);
		sbmrGenerator.setSbmResponseDao(sbmResponseDaoMock);
		sbmrGenerator.setUpdateStatusDataService(mockUpdateStatusDataService);

		sbmrGenerator.setEnvironmentCodeSuffix("T");
	}

	@Test
	public void testSBMRGeneration() throws SQLException, IOException, JAXBException {

		//for DAO calls		
		SbmResponseDTO dto = new SbmResponseDTO();		
		dto.setXprErrorsExist(true);
		dto.setSbmr(createFileAcceptanceRejection());
		dto.setTotalRecordsProcessed(new BigDecimal(100));
		dto.setTotalRecordsRejected(new BigDecimal(3));

		SBMSummaryAndFileInfoDTO summaryDto = new SBMSummaryAndFileInfoDTO();
		summaryDto.setErrorThresholdPercent(new BigDecimal(5));
		summaryDto.setSbmFileStatusType(SBMFileStatus.IN_PROCESS);

		SBMFileInfo fileInfo = new SBMFileInfo();
		fileInfo.setTradingPartnerId("CA01");
		summaryDto.getSbmFileInfoList().add(fileInfo);

		dto.setSbmSummaryAndFileInfo(summaryDto);

		Mockito.when(sbmResponseDaoMock.generateSBMR(Mockito.anyLong())).thenReturn(dto);

		sbmrGenerator.generateSBMRWithPolicyErrors(123L, 11223L);

		Assert.assertNotNull("Should not be null", dto.getSbmr());
		for( FileInformationType sbmrFileInfo: dto.getSbmr().getSBMIFileInfo()) {
			Assert.assertTrue("Incorrect status value", SBMFileStatus.ACCEPTED_WITH_ERRORS.getName().equals(sbmrFileInfo.getFileProcessingStatus()));
		}

	}

	@Test
	public void test_generateSBMRWithPolicyErrors_ER_057() throws SQLException, IOException, JAXBException {

		SBMErrorWarningCode expectedError = SBMErrorWarningCode.ER_057;
		SBMFileStatus expectedStatus = SBMFileStatus.REJECTED;

		//for DAO calls		
		SbmResponseDTO mockResponseDTO = new SbmResponseDTO();		
		mockResponseDTO.setXprErrorsExist(true);
		mockResponseDTO.setSbmr(createFileAcceptanceRejection());
		mockResponseDTO.setTotalRecordsProcessed(new BigDecimal(100));
		mockResponseDTO.setTotalRecordsRejected(new BigDecimal(3));

		SBMSummaryAndFileInfoDTO mockSummaryDTO = new SBMSummaryAndFileInfoDTO();
		// Do not set errThresholdPct
		mockSummaryDTO.setSbmFileStatusType(SBMFileStatus.IN_PROCESS);

		SBMFileInfo mockFileInfo = new SBMFileInfo();
		mockFileInfo.setTradingPartnerId("CA01");
		mockSummaryDTO.getSbmFileInfoList().add(mockFileInfo);

		mockResponseDTO.setSbmSummaryAndFileInfo(mockSummaryDTO);

		Mockito.when(sbmResponseDaoMock.generateSBMR(Mockito.anyLong())).thenReturn(mockResponseDTO);

		sbmrGenerator.generateSBMRWithPolicyErrors(123L, 11223L);

		Assert.assertNotNull("SbmResponseDTO should not be null", mockResponseDTO.getSbmr());

		List<FileInformationType> actualList = mockResponseDTO.getSbmr().getSBMIFileInfo();

		assertEquals("sbmFileInfoList list size", 1, actualList.size());

		FileInformationType actual = actualList.get(0);

		assertEquals("File Error", expectedError.getCode(), actual.getFileError().getErrorCode());
		assertEquals("File Status", expectedStatus.getName(), actual.getFileProcessingStatus());		
	}

	@Test
	public void test_generateSBMRWithPolicyErrors_XPR_Errors() throws SQLException, IOException, JAXBException {

		SBMErrorWarningCode expectedError = null;
		SBMFileStatus expectedStatus = SBMFileStatus.ACCEPTED_WITH_ERRORS;

		//for DAO calls		
		SbmResponseDTO mockResponseDTO = new SbmResponseDTO();		
		mockResponseDTO.setXprErrorsExist(true);
		mockResponseDTO.setSbmr(createFileAcceptanceRejection());
		mockResponseDTO.setTotalRecordsProcessed(new BigDecimal(100));
		mockResponseDTO.setTotalRecordsRejected(new BigDecimal(3));

		SBMSummaryAndFileInfoDTO mockSummaryDTO = new SBMSummaryAndFileInfoDTO();
		mockSummaryDTO.setErrorThresholdPercent(new BigDecimal("99.99"));
		mockSummaryDTO.setSbmFileStatusType(SBMFileStatus.IN_PROCESS);

		SBMFileInfo mockFileInfo = new SBMFileInfo();
		mockFileInfo.setTradingPartnerId("CA01");
		mockSummaryDTO.getSbmFileInfoList().add(mockFileInfo);

		mockResponseDTO.setSbmSummaryAndFileInfo(mockSummaryDTO);

		Mockito.when(sbmResponseDaoMock.generateSBMR(Mockito.anyLong())).thenReturn(mockResponseDTO);

		sbmrGenerator.generateSBMRWithPolicyErrors(123L, 11223L);

		Assert.assertNotNull("SbmResponseDTO should not be null", mockResponseDTO.getSbmr());

		List<FileInformationType> actualList = mockResponseDTO.getSbmr().getSBMIFileInfo();

		assertEquals("sbmFileInfoList list size", 1, actualList.size());

		FileInformationType actual = actualList.get(0);

		assertEquals("File Error", expectedError, actual.getFileError());
		assertEquals("File Status", expectedStatus.getName(), actual.getFileProcessingStatus());		
	}

	@Test
	public void test_generateSBMRWithPolicyErrors_XPR_Warnings() throws SQLException, IOException, JAXBException {

		SBMErrorWarningCode expectedError = null;
		SBMFileStatus expectedStatus = SBMFileStatus.ACCEPTED_WITH_WARNINGS;

		//for DAO calls		
		SbmResponseDTO mockResponseDTO = new SbmResponseDTO();		
		mockResponseDTO.setXprErrorsExist(false);
		mockResponseDTO.setXprWarningsExist(true);
		mockResponseDTO.setSbmr(createFileAcceptanceRejection());
		mockResponseDTO.setTotalRecordsProcessed(new BigDecimal(100));
		mockResponseDTO.setTotalRecordsRejected(new BigDecimal(3));

		SBMSummaryAndFileInfoDTO mockSummaryDTO = new SBMSummaryAndFileInfoDTO();
		mockSummaryDTO.setErrorThresholdPercent(new BigDecimal("99.99"));
		mockSummaryDTO.setSbmFileStatusType(SBMFileStatus.IN_PROCESS);

		SBMFileInfo mockFileInfo = new SBMFileInfo();
		mockFileInfo.setTradingPartnerId("CA01");
		mockSummaryDTO.getSbmFileInfoList().add(mockFileInfo);

		mockResponseDTO.setSbmSummaryAndFileInfo(mockSummaryDTO);

		Mockito.when(sbmResponseDaoMock.generateSBMR(Mockito.anyLong())).thenReturn(mockResponseDTO);

		Long sbmFileProcSumId = Long.valueOf(9999001);
		Long jobExecId = Long.valueOf(9001);

		sbmrGenerator.generateSBMRWithPolicyErrors(sbmFileProcSumId, jobExecId);

		Assert.assertNotNull("SbmResponseDTO should not be null", mockResponseDTO.getSbmr());

		List<FileInformationType> actualList = mockResponseDTO.getSbmr().getSBMIFileInfo();

		assertEquals("sbmFileInfoList list size", 1, actualList.size());

		FileInformationType actual = actualList.get(0);

		assertEquals("File Error", expectedError, actual.getFileError());
		assertEquals("File Status", expectedStatus.getName(), actual.getFileProcessingStatus());		
	}


	@Test
	public void test_generateSBMRWithPolicyErrors_APP() throws SQLException, IOException, JAXBException {

		SBMErrorWarningCode expectedError = null;
		SBMFileStatus expectedStatus = SBMFileStatus.APPROVED;

		//for DAO calls		
		SbmResponseDTO mockResponseDTO = new SbmResponseDTO();		
		mockResponseDTO.setXprErrorsExist(false);
		mockResponseDTO.setXprWarningsExist(false);
		mockResponseDTO.setSbmr(createFileAcceptanceRejection());
		mockResponseDTO.setTotalRecordsProcessed(new BigDecimal(100));
		mockResponseDTO.setTotalRecordsRejected(new BigDecimal(3));

		SBMSummaryAndFileInfoDTO mockSummaryDTO = new SBMSummaryAndFileInfoDTO();
		mockSummaryDTO.setErrorThresholdPercent(new BigDecimal("99.99"));
		mockSummaryDTO.setSbmFileStatusType(SBMFileStatus.IN_PROCESS);
		mockSummaryDTO.setCmsApprovalRequiredInd(N);

		SBMFileInfo mockFileInfo = new SBMFileInfo();
		mockFileInfo.setTradingPartnerId("CA01");
		mockSummaryDTO.getSbmFileInfoList().add(mockFileInfo);

		mockResponseDTO.setSbmSummaryAndFileInfo(mockSummaryDTO);
		
		Long sbmFileProcSumId = Long.valueOf(9999001);
		Long jobExecId = Long.valueOf(9001);
		
		Mockito.doNothing().when(mockUpdateStatusDataService).executeApproval(Mockito.anyLong(), Mockito.anyLong());

		Mockito.when(sbmResponseDaoMock.generateSBMR(Mockito.anyLong())).thenReturn(mockResponseDTO);
		
		sbmrGenerator.generateSBMRWithPolicyErrors(jobExecId, sbmFileProcSumId);

		Assert.assertNotNull("SbmResponseDTO should not be null", mockResponseDTO.getSbmr());

		List<FileInformationType> actualList = mockResponseDTO.getSbmr().getSBMIFileInfo();

		assertEquals("sbmFileInfoList list size", 1, actualList.size());

		FileInformationType actual = actualList.get(0);

		assertEquals("File Error", expectedError, actual.getFileError());
		assertEquals("File Status", expectedStatus.getName(), actual.getFileProcessingStatus());		
	}
	
	
	@Test
	public void test_generateSBMRWithPolicyErrors_APE() throws SQLException, IOException, JAXBException {

		SBMErrorWarningCode expectedError = null;
		SBMFileStatus expectedStatus = SBMFileStatus.APPROVED_WITH_ERRORS;

		//for DAO calls		
		SbmResponseDTO mockResponseDTO = new SbmResponseDTO();		
		mockResponseDTO.setXprErrorsExist(true);
		mockResponseDTO.setXprWarningsExist(false);
		mockResponseDTO.setSbmr(createFileAcceptanceRejection());
		mockResponseDTO.setTotalRecordsProcessed(new BigDecimal(100));
		mockResponseDTO.setTotalRecordsRejected(new BigDecimal(3));

		SBMSummaryAndFileInfoDTO mockSummaryDTO = new SBMSummaryAndFileInfoDTO();
		mockSummaryDTO.setErrorThresholdPercent(new BigDecimal("99.99"));
		mockSummaryDTO.setSbmFileStatusType(SBMFileStatus.IN_PROCESS);
		mockSummaryDTO.setCmsApprovalRequiredInd(N);

		SBMFileInfo mockFileInfo = new SBMFileInfo();
		mockFileInfo.setTradingPartnerId("CA01");
		mockSummaryDTO.getSbmFileInfoList().add(mockFileInfo);

		mockResponseDTO.setSbmSummaryAndFileInfo(mockSummaryDTO);
		
		Long sbmFileProcSumId = Long.valueOf(9999002);
		Long jobExecId = Long.valueOf(9002);
		
		Mockito.doNothing().when(mockUpdateStatusDataService).executeApproval(Mockito.anyLong(), Mockito.anyLong());

		Mockito.when(sbmResponseDaoMock.generateSBMR(Mockito.anyLong())).thenReturn(mockResponseDTO);
		
		sbmrGenerator.generateSBMRWithPolicyErrors(jobExecId, sbmFileProcSumId);

		Assert.assertNotNull("SbmResponseDTO should not be null", mockResponseDTO.getSbmr());

		List<FileInformationType> actualList = mockResponseDTO.getSbmr().getSBMIFileInfo();

		assertEquals("sbmFileInfoList list size", 1, actualList.size());

		FileInformationType actual = actualList.get(0);

		assertEquals("File Error", expectedError, actual.getFileError());
		assertEquals("File Status", expectedStatus.getName(), actual.getFileProcessingStatus());		
	}
	
	@Test
	public void test_generateSBMRWithPolicyErrors_APW() throws SQLException, IOException, JAXBException {

		SBMErrorWarningCode expectedError = null;
		SBMFileStatus expectedStatus = SBMFileStatus.APPROVED_WITH_WARNINGS;

		//for DAO calls		
		SbmResponseDTO mockResponseDTO = new SbmResponseDTO();		
		mockResponseDTO.setXprErrorsExist(false);
		mockResponseDTO.setXprWarningsExist(true);
		mockResponseDTO.setSbmr(createFileAcceptanceRejection());
		mockResponseDTO.setTotalRecordsProcessed(new BigDecimal(100));
		mockResponseDTO.setTotalRecordsRejected(new BigDecimal(3));

		SBMSummaryAndFileInfoDTO mockSummaryDTO = new SBMSummaryAndFileInfoDTO();
		mockSummaryDTO.setErrorThresholdPercent(new BigDecimal("99.99"));
		mockSummaryDTO.setSbmFileStatusType(SBMFileStatus.IN_PROCESS);
		mockSummaryDTO.setCmsApprovalRequiredInd(N);

		SBMFileInfo mockFileInfo = new SBMFileInfo();
		mockFileInfo.setTradingPartnerId("CA01");
		mockSummaryDTO.getSbmFileInfoList().add(mockFileInfo);

		mockResponseDTO.setSbmSummaryAndFileInfo(mockSummaryDTO);
		
		Long sbmFileProcSumId = Long.valueOf(9999003);
		Long jobExecId = Long.valueOf(9003);
		
		Mockito.doNothing().when(mockUpdateStatusDataService).executeApproval(Mockito.anyLong(), Mockito.anyLong());

		Mockito.when(sbmResponseDaoMock.generateSBMR(Mockito.anyLong())).thenReturn(mockResponseDTO);
		
		sbmrGenerator.generateSBMRWithPolicyErrors(jobExecId, sbmFileProcSumId);

		Assert.assertNotNull("SbmResponseDTO should not be null", mockResponseDTO.getSbmr());

		List<FileInformationType> actualList = mockResponseDTO.getSbmr().getSBMIFileInfo();

		assertEquals("sbmFileInfoList list size", 1, actualList.size());

		FileInformationType actual = actualList.get(0);

		assertEquals("File Error", expectedError, actual.getFileError());
		assertEquals("File Status", expectedStatus.getName(), actual.getFileProcessingStatus());		
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
		summaryDto.setSbmFileStatusType(SBMFileStatus.IN_PROCESS);

		SBMFileInfo fileInfo = new SBMFileInfo();
		fileInfo.setTradingPartnerId("CA01");
		summaryDto.getSbmFileInfoList().add(fileInfo);

		dto.setSbmSummaryAndFileInfo(summaryDto);

		Mockito.when(sbmResponseDaoMock.generateUpdateStatusSBMR(Mockito.anyLong())).thenReturn(dto);

		SBMUpdateStatusRecordDTO updateStatusDto = new SBMUpdateStatusRecordDTO();
		updateStatusDto.setNewFileSatus(SBMFileStatus.APPROVED);
		updateStatusDto.setSbmFileProcSumId(9999L);
		
		Long jobId = 88888L;

		sbmrGenerator.generateSBMRForUpdateStatus(updateStatusDto, jobId);

		Assert.assertNotNull("Should not be null", dto.getSbmr());
		for( FileInformationType sbmrFileInfo: dto.getSbmr().getSBMIFileInfo()) {
			Assert.assertTrue("Incorrect status value", SBMFileStatus.APPROVED.getName().equals(sbmrFileInfo.getFileProcessingStatus()));
		}
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
	
	/*
	public void generateSBMSForAllSBMInfos(SBMSummaryAndFileInfoDTO summaryDto, List<SBMErrorDTO> errorList) throws JAXBException, SQLException, IOException {

		LOG.info("Genrating SBMS for all SBMFileInfos for SbmFileProcSumId: {}", summaryDto.getSbmFileProcSumId());
						
		for( SBMFileInfo fileInfo : summaryDto.getSbmFileInfoList()) {
			
			if(fileInfo.isRejectedInd()) {
				// skip rejected files
				continue;
			}
						
			LOG.info("Genrating SBMS for sbmFileInfoId:{}", fileInfo.getSbmFileInfoId());

			String sbmsXML = createSbmsXml(fileInfo, errorList, summaryDto.getSbmFileStatusType());

			//dispatch sbms to eft
			String filename = EFTDispatchDriver.getFileID(SBMConstants.FUNCTION_CODE_SBMS);
			Long physicalDocId = eftDispatcher.saveDispatchContent(sbmsXML.getBytes(), filename, SBMConstants.FUNCTION_CODE_SBMS, environmentCodeSuffix, fileInfo.getTradingPartnerId(), 0, null, TARGET_EFT_APPLICATION_TYPE);
			LOG.info("SBMS PhysicalDocumentId: {} for sbmFileInfoId: {}", physicalDocId, fileInfo.getSbmFileInfoId());	

			//create entries in SBMResponse	table
			sbmResponseDao.createSBMResponseRecord(summaryDto.getSbmFileProcSumId(), fileInfo.getSbmFileInfoId(), physicalDocId, STATUS);
		}
	}
	*/
	
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

	private FileAcceptanceRejection createFileAcceptanceRejection() {
		FileAcceptanceRejection sbmr = new FileAcceptanceRejection();

		sbmr.setSBMIPROCSUM(new SBMIPROCSUMType());		
		sbmr.getSBMIPROCSUM().setFinalRecordsProcessedSummary(new FinalRecordsProcessedSummary());
		sbmr.getSBMIFileInfo().add(new FileInformationType());

		return sbmr;
	}


	/**
	 * @return the eftDispatcherDao
	 */
	public EFTDispatcherDAO getEftDispatcherDao() {
		return eftDispatcherDao;
	}

	/**
	 * @param eftDispatcherDao the eftDispatcherDao to set
	 */
	public void setEftDispatcherDao(EFTDispatcherDAO eftDispatcherDao) {
		this.eftDispatcherDao = eftDispatcherDao;
	}

}
