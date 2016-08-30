package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.cms.dsh.sbmr.FileAcceptanceRejection;
import gov.cms.dsh.sbmr.FileInformationType;
import gov.cms.dsh.sbmr.SBMIPROCSUMType;
import gov.cms.dsh.sbmr.SBMIPROCSUMType.FinalRecordsProcessedSummary;
import gov.hhs.cms.ff.fm.eps.dispatcher.EFTDispatchDriver;
import gov.hhs.cms.ff.fm.eps.dispatcher.EFTDispatcherDAO;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileInfo;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMSummaryAndFileInfoDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMUpdateStatusRecordDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SbmResponseDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SBMFileCompositeDAO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SbmResponseCompositeDao;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/sbmi-batch-context.xml")
public class SBMResponseGeneratorTest {
	
	private static final Logger LOG = LoggerFactory.getLogger(SBMResponseGeneratorTest.class);
	
	private SBMResponseGenerator sbmrGenerator;
	private SBMFileCompositeDAO fileCompositeDaoMock;
	private SbmResponseCompositeDao sbmResponseDaoMock;
	private EFTDispatchDriver eftDispatcher;
	
	@Autowired
	private EFTDispatcherDAO eftDispatcherDao;
	
	@Before
	public void setUp() {
		sbmrGenerator = new SBMResponseGenerator();
		
		fileCompositeDaoMock = Mockito.mock(SBMFileCompositeDAO.class);
		sbmResponseDaoMock = Mockito.mock(SbmResponseCompositeDao.class);
		
		eftDispatcher = new EFTDispatchDriver();
		eftDispatcher.setBatchProps("/SBMFiles/outbound/");
		eftDispatcher.setEftDispatcherDao(eftDispatcherDao);
		
		
		sbmrGenerator.setEftDispatcher(eftDispatcher);
		sbmrGenerator.setFileCompositeDao(fileCompositeDaoMock);
		sbmrGenerator.setSbmResponseDao(sbmResponseDaoMock);

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
		
		sbmrGenerator.generateSBMRForUpdateStatus(updateStatusDto);
		
		Assert.assertNotNull("Should not be null", dto.getSbmr());
		for( FileInformationType sbmrFileInfo: dto.getSbmr().getSBMIFileInfo()) {
			Assert.assertTrue("Incorrect status value", SBMFileStatus.APPROVED.getName().equals(sbmrFileInfo.getFileProcessingStatus()));
		}
		
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
