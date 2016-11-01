package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm;

import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.Y;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

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
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMSummaryAndFileInfoDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMUpdateStatusRecordDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SbmResponseDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SBMFileCompositeDAO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SbmResponseCompositeDao;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SbmUpdateStatusDataService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/sbmi-batch-context.xml")
public class SBMResponseGenHandlerTest {

	
	private SBMResponseGenHandler responseGenHandler;
	private SBMFileCompositeDAO fileCompositeDaoMock;
	private SbmResponseCompositeDao sbmResponseDaoMock;
	private EFTDispatchDriver eftDispatcher;
	private SbmUpdateStatusDataService mockUpdateStatusDataService;
	private SBMResponseGenerator sbmrGenerator;
	@Before
	public void setUp() {
		responseGenHandler = new SBMResponseGenHandler();
		sbmrGenerator = new SBMResponseGenerator();
		fileCompositeDaoMock = Mockito.mock(SBMFileCompositeDAO.class);
		sbmrGenerator.setFileCompositeDao(fileCompositeDaoMock);
		
	}
	
	@Test
	public void testCreateSbmsXml() throws JAXBException  {
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
		SBMSummaryAndFileInfoDTO summaryDto = new SBMSummaryAndFileInfoDTO();
		summaryDto.setErrorThresholdPercent(new BigDecimal(5));
		summaryDto.setSbmFileStatusType(SBMFileStatus.IN_PROCESS);
        SBMFileInfo fileInfo = new SBMFileInfo();
		fileInfo.setTradingPartnerId("CA01");
		summaryDto.getSbmFileInfoList().add(fileInfo);
		String xmlString = responseGenHandler.createSbmsXml(mockFileInfo, errorList, summaryDto.getSbmFileStatusType());
		assertNotNull("XML Input ",xmlString);
	}
	
	@Test
	public void testSetFileSatus() {
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
		SBMUpdateStatusRecordDTO updateStatusDto = new SBMUpdateStatusRecordDTO();
		updateStatusDto.setNewFileSatus(SBMFileStatus.APPROVED);
		updateStatusDto.setSbmFileProcSumId(9999L);
		SBMFileStatus fileStatus = updateStatusDto.getNewFileSatus();
		FileInformationType fileinfo = responseGenHandler.setFileSatus(dto, fileStatus);
		assertEquals("FileStatus",fileinfo.getFileProcessingStatus(),updateStatusDto.getNewFileSatus().getName());
	}

	@Test
	public void testGenerateErrorThresholdExceeded() {
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
        responseGenHandler.generateErrorThresholdExceeded(dto, fileCompositeDaoMock);
	}   	
	private FileAcceptanceRejection createFileAcceptanceRejection() {
		FileAcceptanceRejection sbmr = new FileAcceptanceRejection();

		sbmr.setSBMIPROCSUM(new SBMIPROCSUMType());		
		sbmr.getSBMIPROCSUM().setFinalRecordsProcessedSummary(new FinalRecordsProcessedSummary());
		sbmr.getSBMIFileInfo().add(new FileInformationType());

		return sbmr;
	}
}
