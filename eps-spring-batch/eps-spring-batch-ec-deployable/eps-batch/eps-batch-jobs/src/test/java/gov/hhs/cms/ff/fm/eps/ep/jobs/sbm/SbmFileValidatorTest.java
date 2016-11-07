package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.cms.dsh.sbmi.FileInformationType;
import gov.cms.dsh.sbmi.FileInformationType.IssuerFileInformation;
import gov.cms.dsh.sbmi.FileInformationType.IssuerFileInformation.IssuerFileSet;
import gov.hhs.cms.ff.fm.eps.ep.StateProrationConfiguration;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMErrorWarningCode;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMPolicyEnum;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMCache;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMErrorDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileInfo;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileProcessingDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMSummaryAndFileInfoDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SBMFileCompositeDAO;
import junit.framework.TestCase;

/**
 * 
 * @author rajesh.talanki
 *
 */
@RunWith(JUnit4.class)
public class SbmFileValidatorTest extends TestCase {
	
	private static final Logger LOG = LoggerFactory.getLogger(SbmFileValidatorTest.class);
	
	public static final String ISSUER_FILE_SET_ID = "IssFileSetId123";

	private SbmFileValidator sbmFileValidator;
	private SBMFileCompositeDAO fileCompositeDao;
	private StateProrationConfiguration mockStateConfig;
	
	@Before
	public void setUp() {
		//mockStateConfig=EasyMock.create(StateProrationConfiguration.class);
		sbmFileValidator = new SbmFileValidator();
		fileCompositeDao = Mockito.mock(SBMFileCompositeDAO.class);
		//sbmCache = Mockito.mock(SBMCache.class);
		sbmFileValidator.setFileCompositeDao(fileCompositeDao);
		sbmFileValidator.setCoverageYear(LocalDate.now().getYear());
		
		SBMFileProcessingDTO dto = new SBMFileProcessingDTO();
		dto.setSbmFileInfo(new SBMFileInfo());
		
	}
	
	/**
	 * Test invalid coverage year
	 */
	@Test
	public void testCoveragYear_invalid() {
		
		SBMFileProcessingDTO dto = createSBMFileProcessingDTO();
		dto.getFileInfoType().setCoverageYear(1900);		
		sbmFileValidator.setCoverageYear(2016);
		
		sbmFileValidator.validate(dto);
		LOG.info("Errors:{}", dto.getErrorList());
		
		Assert.assertTrue("Errors expected", dto.getErrorList().size() > 0);		
		Assert.assertTrue("ER-015 error expected", isErrorExists(dto.getErrorList(), SBMPolicyEnum.COVERAGE_YEAR.getElementNm(), SBMErrorWarningCode.ER_015.getCode()));
		
		
	}
	
	/**
	 * Test coverage year is not in future
	 */
	@Test
	public void testCoveragYear_future() {
		SBMFileProcessingDTO dto = createSBMFileProcessingDTO();
		
		int coverageYear = 2016;
		dto.getFileInfoType().setCoverageYear(coverageYear + 2);		
		sbmFileValidator.setCoverageYear(coverageYear);
		
		sbmFileValidator.validate(dto);
		LOG.info("Errors:{}", dto.getErrorList());
		
		Assert.assertTrue("Errors expected", dto.getErrorList().size() > 0);		
		Assert.assertTrue("ER-016 error expected", isErrorExists(dto.getErrorList(), SBMPolicyEnum.COVERAGE_YEAR.getElementNm(), SBMErrorWarningCode.ER_016.getCode()));
		
		
	}
	
	/**
	 * Test coverage year in with current year + 1
	 */
	@Test
	public void testCoveragYear_nextYear() {
		SBMFileProcessingDTO dto = createSBMFileProcessingDTO();
		
		LocalDate now = LocalDate.now();
				
		sbmFileValidator.setCoverageYear(now.getYear());
		dto.getFileInfoType().setCoverageYear(now.getYear() + 1);		
		
		sbmFileValidator.validate(dto);
		LOG.info("Errors:{}", dto.getErrorList());
				
		if(now.getMonthValue() == 11 || now.getMonthValue() == 12) {
			Assert.assertTrue("Errors expected", dto.getErrorList().size() == 0);
		}
		else {		
			Assert.assertTrue("Errors expected", dto.getErrorList().size() > 0);
			Assert.assertTrue("ER-017 error expected", isErrorExists(dto.getErrorList(), SBMPolicyEnum.COVERAGE_YEAR.getElementNm(), SBMErrorWarningCode.ER_017.getCode()));
		}
		
	}
	
	/**
	 * Test fielId and tenantId not exists
	 */
	@Test
	public void testFileIdAndTenantId() {
		
		SBMFileProcessingDTO dto = createSBMFileProcessingDTO();		
		
		sbmFileValidator.validate(dto);
		LOG.info("Errors:{}", dto.getErrorList());
				
		Assert.assertFalse("ER-018 error not expected", isErrorExists(dto.getErrorList(), SBMPolicyEnum.FILE_ID.getElementNm(), SBMErrorWarningCode.ER_018.getCode()));
				
	}
	
	/**
	 * Test fielId and tenantId already exists
	 */
	@Test
	public void testFileIdAndTenantId_alreadyExists() {
		SBMFileProcessingDTO dto = createSBMFileProcessingDTO();
		
		//for DAO calls
		SBMSummaryAndFileInfoDTO summaryDto = new SBMSummaryAndFileInfoDTO();
		summaryDto.setSbmFileProcSumId(123L);
		summaryDto.setSbmFileStatusType(SBMFileStatus.APPROVED);
		
		SBMFileInfo fileInfo = new SBMFileInfo();
		fileInfo.setRejectedInd(false);
		summaryDto.getSbmFileInfoList().add(fileInfo);	
		
		List<SBMSummaryAndFileInfoDTO> summaryDtoList = new ArrayList<>();
		summaryDtoList.add(summaryDto);
		
		Mockito.when(fileCompositeDao.performSbmFileMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(summaryDtoList);
		
		sbmFileValidator.validate(dto);
		LOG.info("Errors:{}", dto.getErrorList());
		
		Assert.assertTrue("Errors expected", dto.getErrorList().size() > 0);		
		Assert.assertTrue("ER-018 error expected", isErrorExists(dto.getErrorList(), SBMPolicyEnum.FILE_ID.getElementNm(), SBMErrorWarningCode.ER_018.getCode()));
				
	}
	
	/**
	 * Test fielId and tenantId already exists
	 */
	@Test
	public void testFileIdAndTenantId_alreadyExistsDAPPVStatus() {
		SBMFileProcessingDTO dto = createSBMFileProcessingDTO();
		
		SBMSummaryAndFileInfoDTO summaryDto = new SBMSummaryAndFileInfoDTO();
		summaryDto.setSbmFileProcSumId(123L);
		summaryDto.setSbmFileStatusType(SBMFileStatus.DISAPPROVED);	
		
		SBMFileInfo fileInfo = new SBMFileInfo();
		fileInfo.setRejectedInd(false);
		summaryDto.getSbmFileInfoList().add(fileInfo);	
		List<SBMSummaryAndFileInfoDTO> summaryDtoList = new ArrayList<>();
		summaryDtoList.add(summaryDto);
						
		Mockito.when(fileCompositeDao.performSbmFileMatch(Mockito.anyString(), Mockito.anyString())).thenReturn(summaryDtoList);
		
		sbmFileValidator.validate(dto);
		LOG.info("Errors:{}", dto.getErrorList());
		
		Assert.assertFalse("ER-018 error expected", isErrorExists(dto.getErrorList(), SBMPolicyEnum.FILE_ID.getElementNm(), SBMErrorWarningCode.ER_018.getCode()));
				
	}
	
	/**
	 * Test filenumber already exists
	 */
	@Test
	public void testFileNumberExists() {
		
		SBMFileProcessingDTO dto = createSBMFileProcessingDTO();
		
		IssuerFileSet ifs = new IssuerFileSet();
		ifs.setIssuerFileSetId("IssFileSetId123");
		ifs.setFileNumber(2);
		ifs.setTotalIssuerFiles(5);
		IssuerFileInformation issInfo = new IssuerFileInformation();
		issInfo.setIssuerFileSet(ifs);
		dto.getFileInfoType().setIssuerFileInformation(issInfo);
		
		//for DAO call
		SBMSummaryAndFileInfoDTO summaryDto = new SBMSummaryAndFileInfoDTO();
		summaryDto.setSbmFileProcSumId(123L);
		summaryDto.setSbmFileStatusType(SBMFileStatus.APPROVED);
		
		SBMFileInfo fileInfo = new SBMFileInfo();
		fileInfo.setRejectedInd(false);
		summaryDto.getSbmFileInfoList().add(fileInfo);	
		
		List<SBMSummaryAndFileInfoDTO> summaryDtoList = new ArrayList<>();
		summaryDtoList.add(summaryDto);
				
				
		Mockito.when(fileCompositeDao.findSbmFileInfo(Mockito.anyString(), Mockito.anyInt())).thenReturn(summaryDtoList);
		
		sbmFileValidator.validate(dto);
		LOG.info("Errors:{}", dto.getErrorList());
		
		Assert.assertTrue("Errors expected", dto.getErrorList().size() > 0);		
		Assert.assertTrue("Error ER-019 expected", isErrorExists(dto.getErrorList(), SBMPolicyEnum.FILE_NUM.getElementNm(), SBMErrorWarningCode.ER_019.getCode()));
				
	}
	
	/**
	 * Test filenumber already exists
	 */
	@Test
	public void testFileNumberExistsWithRejectedStatus() {
		
		SBMFileProcessingDTO dto = createSBMFileProcessingDTO();
		
		IssuerFileSet ifs = new IssuerFileSet();
		ifs.setIssuerFileSetId("IssFileSetId123");
		ifs.setFileNumber(2);
		ifs.setTotalIssuerFiles(5);
		IssuerFileInformation issInfo = new IssuerFileInformation();
		issInfo.setIssuerFileSet(ifs);
		dto.getFileInfoType().setIssuerFileInformation(issInfo);
		
		//for DAO call
		SBMSummaryAndFileInfoDTO summaryDto = new SBMSummaryAndFileInfoDTO();
		summaryDto.setSbmFileProcSumId(123L);
		summaryDto.setSbmFileStatusType(SBMFileStatus.APPROVED);	
		
		SBMFileInfo fileInfo = new SBMFileInfo();
		fileInfo.setRejectedInd(true);
		summaryDto.getSbmFileInfoList().add(fileInfo);	
		
		List<SBMSummaryAndFileInfoDTO> summaryDtoList = new ArrayList<>();
		summaryDtoList.add(summaryDto);
		
		Mockito.when(fileCompositeDao.findSbmFileInfo(Mockito.anyString(), Mockito.anyInt())).thenReturn(summaryDtoList);
		
		sbmFileValidator.validate(dto);
		LOG.info("Errors:{}", dto.getErrorList());		
	
		Assert.assertFalse("Error ER-019 not expected", isErrorExists(dto.getErrorList(), SBMPolicyEnum.FILE_NUM.getElementNm(), SBMErrorWarningCode.ER_019.getCode()));
				
	}
	
	/**
	 * Test filenumber exceeds total issuer file count
	 */
	@Test
	public void testFileNumberExceeds() {
		
		SBMFileProcessingDTO dto = createSBMFileProcessingDTO();
		
		IssuerFileSet ifs = new IssuerFileSet();
		ifs.setIssuerFileSetId(ISSUER_FILE_SET_ID);
		ifs.setFileNumber(7);
		ifs.setTotalIssuerFiles(5);
		IssuerFileInformation issInfo = new IssuerFileInformation();
		issInfo.setIssuerFileSet(ifs);
		dto.getFileInfoType().setIssuerFileInformation(issInfo);
		
		//mock
		SBMSummaryAndFileInfoDTO summaryDto = new SBMSummaryAndFileInfoDTO();
		summaryDto.setSbmFileProcSumId(1234L);
		summaryDto.setIssuerFileSetId(ISSUER_FILE_SET_ID);
		summaryDto.setTotalIssuerFileCount(5);
		dto.setFileProcSummaryFromDB(summaryDto);
		
		sbmFileValidator.validate(dto);
		LOG.info("Errors:{}", dto.getErrorList());
		
		Assert.assertTrue("Errors expected", dto.getErrorList().size() > 0);		
		Assert.assertTrue("Error ER-021 expected", isErrorExists(dto.getErrorList(), SBMPolicyEnum.FILE_NUM.getElementNm(), SBMErrorWarningCode.ER_021.getCode()));
				
	}
	
	/**
	 * Test totalIssuerFileCount not correct
	 */
	@Test
	public void testTotalIssuerFileCount() {
		
		SBMFileProcessingDTO dto = createSBMFileProcessingDTO();
		
		IssuerFileSet ifs = new IssuerFileSet();
		ifs.setIssuerFileSetId(ISSUER_FILE_SET_ID);
		ifs.setFileNumber(3);
		ifs.setTotalIssuerFiles(5);
		IssuerFileInformation issInfo = new IssuerFileInformation();
		issInfo.setIssuerFileSet(ifs);
		dto.getFileInfoType().setIssuerFileInformation(issInfo);
		
		//FileSet exists	
		SBMSummaryAndFileInfoDTO summaryDto = new SBMSummaryAndFileInfoDTO();
		summaryDto.setSbmFileProcSumId(1234L);
		summaryDto.setIssuerFileSetId(ISSUER_FILE_SET_ID);
		summaryDto.setTotalIssuerFileCount(6);
		dto.setFileProcSummaryFromDB(summaryDto);
		
		sbmFileValidator.validate(dto);
		LOG.info("Errors:{}", dto.getErrorList());
		
		Assert.assertTrue("Errors expected", dto.getErrorList().size() > 0);		
		Assert.assertTrue("Error ER-020 expected", isErrorExists(dto.getErrorList(), SBMPolicyEnum.TOT_ISS_FILES.getElementNm(), SBMErrorWarningCode.ER_020.getCode()));
				
	}
	
	/**
	 * Test no error created when first file in fileSet received
	 */
	@Test
	public void testTotalIssuerFileCount_noError() {
		
		SBMFileProcessingDTO dto = createSBMFileProcessingDTO();
		
		IssuerFileSet ifs = new IssuerFileSet();
		ifs.setIssuerFileSetId(ISSUER_FILE_SET_ID);
		ifs.setFileNumber(3);
		ifs.setTotalIssuerFiles(5);
		IssuerFileInformation issInfo = new IssuerFileInformation();
		issInfo.setIssuerFileSet(ifs);
		dto.getFileInfoType().setIssuerFileInformation(issInfo);
		
		//FileSet exists		
		dto.setIssuerFileSetId(ISSUER_FILE_SET_ID);
		dto.setTotalIssuerFileCount(6);
		
		sbmFileValidator.validate(dto);
		LOG.info("Errors:{}", dto.getErrorList());
				
		Assert.assertFalse("Error ER-020 not expected", isErrorExists(dto.getErrorList(), SBMPolicyEnum.TOT_ISS_FILES.getElementNm(), SBMErrorWarningCode.ER_020.getCode()));
				
	}
	
//	/**
//	 * Test State is allowed for SBM submission
//	 */
//	@Test
//	public void testStateAllowedForSBM() {
//		SBMFileProcessingDTO dto = createSBMFileProcessingDTO();			
//		sbmFileValidator.setCoverageYear(2016);
//		
//		StateProrationConfiguration stConfig = new StateProrationConfiguration();
//		stConfig.setMarketYear("2016");
//		stConfig.setStateCd("CA");
//		
//		Map<String, StateProrationConfiguration> stMap = new HashMap<>();
//		stMap.put("CA", stConfig);
//		
//		SBMCache.getStateProrationConfigMap().put(2016, stMap);
//		
//		sbmFileValidator.validate(dto);
//		LOG.info("Errors:{}", dto.getErrorList());
//		
//		Assert.assertTrue("No errors expcected", dto.getErrorList().size() == 0);		
////		Assert.assertTrue("ER-015 error expected", isErrorExists(dto.getErrorList(), SBMPolicyEnum.COVERAGE_YEAR.getElementNm(), SBMErrorWarningCode.ER_015.getCode()));		
//	}
	
//	/**
//	 * Test State is not allowed for SBM submission
//	 */
//	@Test
//	public void testStateNotAllowedForSBM() {
//		SBMFileProcessingDTO dto = createSBMFileProcessingDTO();			
//		sbmFileValidator.setCoverageYear(2016);
//		SBMCache.getStateProrationConfigMap().clear();
//		sbmFileValidator.validate(dto);
//		LOG.info("Errors:{}", dto.getErrorList());
//		
//		Assert.assertTrue("Errors expcected", dto.getErrorList().size() > 0);		
//		Assert.assertTrue("ER-997 error expected", isErrorExists(dto.getErrorList(), null, SBMErrorWarningCode.ER_997.getCode()));		
//	}
	
	private SBMFileProcessingDTO createSBMFileProcessingDTO() {
		SBMFileProcessingDTO dto = new SBMFileProcessingDTO();
		dto.setSbmFileInfo(new SBMFileInfo());	
		dto.getSbmFileInfo().setSbmFileNm("TestFile.P");
		FileInformationType fileInfoType = new FileInformationType();	
		fileInfoType.setCoverageYear(LocalDate.now().getYear());
		fileInfoType.setTenantId("CA0");
		fileInfoType.setFileId("FileId123");
		dto.setFileInfoType(fileInfoType);
		
		return dto;
	}
	
	private boolean isErrorExists(List<SBMErrorDTO> errorList, String elementInError, String errorCode) {
		for(SBMErrorDTO error: errorList) {
			if(elementInError == null) {
				if(errorCode.equalsIgnoreCase(error.getSbmErrorWarningTypeCd())) {
					return true;
				}
			}
			else if(elementInError.equalsIgnoreCase(error.getElementInErrorNm()) 
					&& errorCode.equalsIgnoreCase(error.getSbmErrorWarningTypeCd())) {
				return true;
			}
		}
		
		return false;
	}
}
