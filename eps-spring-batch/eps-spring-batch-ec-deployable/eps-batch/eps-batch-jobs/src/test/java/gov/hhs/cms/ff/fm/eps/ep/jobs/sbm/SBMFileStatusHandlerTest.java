package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.test.util.ReflectionTestUtils;
import org.xml.sax.SAXException;

import gov.cms.dsh.sbmi.FileInformationType;
import gov.cms.dsh.sbmi.FileInformationType.IssuerFileInformation;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileInfo;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileProcessingDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMSummaryAndFileInfoDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SBMFileCompositeDAO;
import junit.framework.TestCase;

@RunWith(JUnit4.class)
public class SBMFileStatusHandlerTest extends TestCase {
	
	private SBMFileStatusHandler fileSatusHandler;
	
	private SBMFileCompositeDAO mockSbmFileCompositeDao;
	
	@Before
	public void setUp() throws IOException, ParserConfigurationException, SAXException {
		
		fileSatusHandler = new SBMFileStatusHandler();
		
		mockSbmFileCompositeDao = Mockito.mock(SBMFileCompositeDAO.class);
		
		fileSatusHandler.setFileCompositeDao(mockSbmFileCompositeDao);
		
	}
	
	@After
	public void tearDown() throws IOException {
	}
	
	@Test
	public void test_determineAndSetFileStatus() throws UnexpectedInputException, ParseException, NonTransientResourceException, Exception {
		
		SBMFileProcessingDTO dto = new SBMFileProcessingDTO();
		
		FileInformationType fileInfoType = new FileInformationType();
		dto.setFileInfoType(fileInfoType);
		
		SBMFileInfo fileInfo = new SBMFileInfo();
		fileInfo.setFileLastModifiedDateTime(LocalDateTime.now());
		dto.setSbmFileInfo(fileInfo);
		
		fileSatusHandler.setFreezePeriodStartDay(2);
		fileSatusHandler.setFreezePeriodEndDay(10);
		
		fileSatusHandler.determineAndSetFileStatus(dto);
		
		Assert.assertNotNull("Reader should return dto", dto);
	}
	
	
	@Test
	public void test_Inbound_State_EPS_FileSet() {
		
		boolean expected = true;
		
		String tenantId = "CT0";
		String issuerId = "11111";
		
		FileInformationType fileInfoType = new FileInformationType();
		fileInfoType.setTenantId(tenantId);
		
		// Build data from EPS for mock call.
		List<SBMSummaryAndFileInfoDTO> summaryList = new ArrayList<SBMSummaryAndFileInfoDTO>();
		
		SBMSummaryAndFileInfoDTO issuerFileSet_1_of_2 = new SBMSummaryAndFileInfoDTO();
		issuerFileSet_1_of_2.setIssuerId(issuerId);
		issuerFileSet_1_of_2.setSbmFileStatusType(SBMFileStatus.ACCEPTED);
		
		SBMSummaryAndFileInfoDTO issuerFileSet_2_of_2 = new SBMSummaryAndFileInfoDTO();
		issuerFileSet_2_of_2.setIssuerId(issuerId);
		issuerFileSet_2_of_2.setSbmFileStatusType(SBMFileStatus.IN_PROCESS);
		
		summaryList.add(issuerFileSet_1_of_2);
		summaryList.add(issuerFileSet_2_of_2);
		
		Mockito.when(mockSbmFileCompositeDao.getAllInProcessOrPendingApprovalForState(Mockito.anyString())).thenReturn(summaryList);
		
		Boolean actual = (Boolean) ReflectionTestUtils.invokeMethod(fileSatusHandler, "determineFileProcessing", fileInfoType);
		
		assertEquals("isFileProcessing", expected, actual.booleanValue());
	}
	
	
	
	
	@Test
	public void test_Inbound_Issuer_EPS_Same_Issuer_ACW() {
		
		boolean expected = true;
		
		String tenantId = "CT0";
		String issuerId = "22222";
		SBMFileStatus status = SBMFileStatus.ACCEPTED_WITH_WARNINGS;
		
		FileInformationType fileInfoType = new FileInformationType();
		fileInfoType.setTenantId(tenantId);
		fileInfoType.setIssuerFileInformation(new IssuerFileInformation());
		fileInfoType.getIssuerFileInformation().setIssuerId(issuerId);
					
		// Build data from EPS for mock call.
		List<SBMSummaryAndFileInfoDTO> summaryList = new ArrayList<SBMSummaryAndFileInfoDTO>();
		
		SBMSummaryAndFileInfoDTO issuerFile = new SBMSummaryAndFileInfoDTO();
		issuerFile.setIssuerId(issuerId);
		issuerFile.setSbmFileStatusType(status);
		
		summaryList.add(issuerFile);
		
		Mockito.when(mockSbmFileCompositeDao.getAllInProcessOrPendingApprovalForState(Mockito.anyString())).thenReturn(summaryList);
		
		Boolean actual = (Boolean) ReflectionTestUtils.invokeMethod(fileSatusHandler, "determineFileProcessing", fileInfoType);
		
		assertEquals("isFileProcessing", expected, actual.booleanValue());
	}
	
	
	@Test
	public void test_Inbound_Issuer_EPS_State_ACE() {
		
		boolean expected = true;
		
		String tenantId = "VT0";
		String issuerId = "33333";
		SBMFileStatus status = SBMFileStatus.ACCEPTED_WITH_ERRORS;
		
		FileInformationType fileInfoType = new FileInformationType();
		fileInfoType.setTenantId(tenantId);
		fileInfoType.setIssuerFileInformation(new IssuerFileInformation());
		fileInfoType.getIssuerFileInformation().setIssuerId(issuerId);
		//fileInfoType.getIssuerFileInformation().setIssuerFileSet(new IssuerFileSet());
				
		// Build data from EPS for mock call.
		List<SBMSummaryAndFileInfoDTO> summaryList = new ArrayList<SBMSummaryAndFileInfoDTO>();
		SBMSummaryAndFileInfoDTO stateFile = new SBMSummaryAndFileInfoDTO();
		stateFile.setTenantId(tenantId);
		stateFile.setSbmFileStatusType(status);
		
		summaryList.add(stateFile);
		
		Mockito.when(mockSbmFileCompositeDao.getAllInProcessOrPendingApprovalForState(Mockito.anyString())).thenReturn(summaryList);
		
		Boolean actual = (Boolean) ReflectionTestUtils.invokeMethod(fileSatusHandler, "determineFileProcessing", fileInfoType);
		
		assertEquals("isFileProcessing", expected, actual.booleanValue());
	}
	
	@Test
	public void test_Inbound_Issuer_EPS_Diff_Issuer_IPC() {
		
		boolean expected = false;
		
		String tenantId = "VT0";
		String issuerId = "55555";
		String issuerIdInProcess = "44444";
		SBMFileStatus status = SBMFileStatus.IN_PROCESS;
		
		FileInformationType fileInfoType = new FileInformationType();
		fileInfoType.setTenantId(tenantId);
		fileInfoType.setIssuerFileInformation(new IssuerFileInformation());
		fileInfoType.getIssuerFileInformation().setIssuerId(issuerId);
		//fileInfoType.getIssuerFileInformation().setIssuerFileSet(new IssuerFileSet());
				
		// Build data from EPS for mock call.
		List<SBMSummaryAndFileInfoDTO> summaryList = new ArrayList<SBMSummaryAndFileInfoDTO>();
		SBMSummaryAndFileInfoDTO issuerFile = new SBMSummaryAndFileInfoDTO();
		issuerFile.setTenantId(tenantId);
		issuerFile.setSbmFileStatusType(status);
		issuerFile.setIssuerId(issuerIdInProcess);
		
		summaryList.add(issuerFile);
		
		Mockito.when(mockSbmFileCompositeDao.getAllInProcessOrPendingApprovalForState(Mockito.anyString())).thenReturn(summaryList);
		
		Boolean actual = (Boolean) ReflectionTestUtils.invokeMethod(fileSatusHandler, "determineFileProcessing", fileInfoType);
		
		assertEquals("isFileProcessing", expected, actual.booleanValue());
	}
	
	
	@Test
	public void test_Inbound_State_EPS_Issuers() {
		
		boolean expected = true;
		
		String tenantId = "CT0";
		String issuerId_1 = "11111";
		String issuerId_2 = "22222";
		
		FileInformationType fileInfoType = new FileInformationType();
		fileInfoType.setTenantId(tenantId);
		
		// Build data from EPS for mock call.
		List<SBMSummaryAndFileInfoDTO> summaryList = new ArrayList<SBMSummaryAndFileInfoDTO>();
		
		SBMSummaryAndFileInfoDTO issuerFile_1 = new SBMSummaryAndFileInfoDTO();
		issuerFile_1.setTenantId(tenantId);
		issuerFile_1.setSbmFileStatusType(SBMFileStatus.ACCEPTED_WITH_WARNINGS);
		issuerFile_1.setIssuerId(issuerId_1);
		
		SBMSummaryAndFileInfoDTO issuerFile_2 = new SBMSummaryAndFileInfoDTO();
		issuerFile_2.setTenantId(tenantId);
		issuerFile_2.setSbmFileStatusType(SBMFileStatus.IN_PROCESS);
		issuerFile_2.setIssuerId(issuerId_2);
		
		summaryList.add(issuerFile_1);
		summaryList.add(issuerFile_2);
		
		Mockito.when(mockSbmFileCompositeDao.getAllInProcessOrPendingApprovalForState(Mockito.anyString())).thenReturn(summaryList);
		
		Boolean actual = (Boolean) ReflectionTestUtils.invokeMethod(fileSatusHandler, "determineFileProcessing", fileInfoType);
		
		assertEquals("isFileProcessing", expected, actual.booleanValue());
	}
	
	
	@Test
	public void test_Inbound_State_EPS_None() {
		
		boolean expected = false;
		
		String tenantId = "CT0";
		
		FileInformationType fileInfoType = new FileInformationType();
		fileInfoType.setTenantId(tenantId);
		
		// Build data from EPS for mock call.
		List<SBMSummaryAndFileInfoDTO> summaryList = new ArrayList<SBMSummaryAndFileInfoDTO>();
		
		Mockito.when(mockSbmFileCompositeDao.getAllInProcessOrPendingApprovalForState(Mockito.anyString())).thenReturn(summaryList);
		
		Boolean actual = (Boolean) ReflectionTestUtils.invokeMethod(fileSatusHandler, "determineFileProcessing", fileInfoType);
		
		assertEquals("isFileProcessing", expected, actual.booleanValue());
	}
	
	@Test
	public void test_Inbound_Issuer_EPS_None() {
		
		boolean expected = false;
		
		String tenantId = "CT0";
		String issuerId = "88888";
		
		FileInformationType fileInfoType = new FileInformationType();
		fileInfoType.setTenantId(tenantId);
		fileInfoType.setIssuerFileInformation(new IssuerFileInformation());
		fileInfoType.getIssuerFileInformation().setIssuerId(issuerId);
		
		// Build data from EPS for mock call.
		List<SBMSummaryAndFileInfoDTO> summaryList = new ArrayList<SBMSummaryAndFileInfoDTO>();
		
		Mockito.when(mockSbmFileCompositeDao.getAllInProcessOrPendingApprovalForState(Mockito.anyString())).thenReturn(summaryList);
		
		Boolean actual = (Boolean) ReflectionTestUtils.invokeMethod(fileSatusHandler, "determineFileProcessing", fileInfoType);
		
		assertEquals("isFileProcessing", expected, actual.booleanValue());
	}

		
}
