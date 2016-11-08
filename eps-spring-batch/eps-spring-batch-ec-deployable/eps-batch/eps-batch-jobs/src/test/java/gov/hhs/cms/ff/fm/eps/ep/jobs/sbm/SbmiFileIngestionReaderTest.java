package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.test.util.ReflectionTestUtils;
import org.xml.sax.SAXException;

import gov.cms.dsh.sbmi.FileInformationType;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMErrorWarningCode;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMErrorDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileInfo;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileProcessingDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMSummaryAndFileInfoDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SBMFileCompositeDAO;
import junit.framework.TestCase;

@RunWith(JUnit4.class)
public class SbmiFileIngestionReaderTest extends TestCase {

	private static final String eftFolderPath = "./src/test/resources/sbm/readerTest/eftFolder";
	private static final String privateFolderPath = "./src/test/resources/sbm/readerTest/privateFolder";
	private static final String zipFolderPath = "./src/test/resources/sbm/readerTest/zipFilesFolder";
	private static final String processedFolderPath = "./src/test/resources/sbm/readerTest/processedFilesFolder";

	private File eftFolder = new File(eftFolderPath);
	private File privateFolder = new File(privateFolderPath);	
	private File zipFolder = new File(zipFolderPath);
	private File processedFolder = new File(processedFolderPath);

	private SbmiFileIngestionReader fileIngestionReader;

	private SbmXMLValidator mockXmlValidator;
	private SBMFileCompositeDAO mockFileCompositeDao;
	private SbmFileValidator mockFileValidator;
	private SBMFileStatusHandler mockFileStatusHandler;
	private SBMXMLValidatorHandle mockSBMXMLValidatorHandle;
	private final DateTimeFormatter DTF_FILE = DateTimeFormatter.ofPattern("'D'yyMMdd'.T'HHmmssSSS");
	private final DateTimeFormatter DTF_ZIP = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
	
	@Before
	public void setUp() throws IOException, ParserConfigurationException, SAXException {

		fileIngestionReader = new SbmiFileIngestionReader();
		fileIngestionReader.setEftFolder(new File(eftFolderPath));
		fileIngestionReader.setPrivateFolder(new File(privateFolderPath));
		fileIngestionReader.setZipFilesFolder(new File(zipFolderPath));
		fileIngestionReader.setProcessedFolder(processedFolder);


		mockXmlValidator = EasyMock.createMock(SbmXMLValidator.class);
		mockFileCompositeDao = EasyMock.createMock(SBMFileCompositeDAO.class);
		mockFileValidator = EasyMock.createMock(SbmFileValidator.class);
		mockFileStatusHandler = EasyMock.createMock(SBMFileStatusHandler.class);
		mockSBMXMLValidatorHandle = EasyMock.createMock(SBMXMLValidatorHandle.class);

		fileIngestionReader.setXmlValidator(mockXmlValidator);
		fileIngestionReader.setFileCompositeDao(mockFileCompositeDao);
		fileIngestionReader.setFileValidator(mockFileValidator);
		fileIngestionReader.setFileSatusHandler(mockFileStatusHandler);
		fileIngestionReader.setSbmxmlValidatorHandle(mockSBMXMLValidatorHandle);
		eftFolder.mkdirs();		
		privateFolder.mkdirs();
		zipFolder.mkdirs();
		processedFolder.mkdirs();

		FileUtils.cleanDirectory(eftFolder);
		FileUtils.cleanDirectory(privateFolder);
		FileUtils.cleanDirectory(zipFolder);
		FileUtils.cleanDirectory(processedFolder);
	}

	@After
	public void tearDown() throws IOException {
		FileUtils.cleanDirectory(eftFolder);
		FileUtils.cleanDirectory(privateFolder);
		FileUtils.cleanDirectory(zipFolder);
		FileUtils.cleanDirectory(processedFolder);
	}

	@Test
	public void testReaderNoErrors() throws UnexpectedInputException, ParseException, NonTransientResourceException, Exception {

		FileUtils.copyFileToDirectory(new File("./src/test/resources/sbm/schemaErrors/SBMI_noSchemaErrors.xml"), eftFolder);

		expect(mockFileCompositeDao.getFileStatus(EasyMock.anyString())).andReturn(new ArrayList<SBMSummaryAndFileInfoDTO>());
		replay(mockFileCompositeDao);

		expect(mockSBMXMLValidatorHandle.isValidXML(EasyMock.anyObject(File.class))).andReturn(true);
		expect(mockXmlValidator.validateSchemaForFileInfo(EasyMock.anyLong(), EasyMock.anyObject(File.class))).andReturn(new ArrayList<SBMErrorDTO>());
		expect(mockSBMXMLValidatorHandle.unmarshallSBMIFileInfo(EasyMock.anyObject(File.class))).andReturn(new FileInformationType());
		String expectedFileInfoXML = "<XML>FileInfo</XML>";
		expect(mockSBMXMLValidatorHandle.marshallFileInfo(EasyMock.anyObject(FileInformationType.class))).andReturn(expectedFileInfoXML);
		replay(mockXmlValidator);
		replay(mockSBMXMLValidatorHandle);
		mockFileStatusHandler.determineAndSetFileStatus(EasyMock.anyObject(SBMFileProcessingDTO.class));

		Long batchId = 2123L;
		SBMFileProcessingDTO dto = fileIngestionReader.read(batchId);

		assertNotNull("Reader should return dto", dto);
		assertEquals("FileInfoXML", expectedFileInfoXML, dto.getFileInfoXML());
		assertEquals("RejectedInd", false, dto.getSbmFileInfo().isRejectedInd());

	}

	@Test
	public void testReaderZip() throws UnexpectedInputException, ParseException, NonTransientResourceException, Exception {
		
		FileUtils.copyFileToDirectory(new File("./src/test/resources/sbm/sbmTestFiles/zipFiles/SBMI.NY1.D160810.T100000007.T"), eftFolder);

		String expectedTradingPartnerId = "NY1";
		String expectedFunctionCd = "SBMI";
		String expectedFileNm = "NY1.EPS.SBMI.D160810.T100000007.T";
		boolean expectedRejectInd = false;
		
		expect(mockFileCompositeDao.getFileStatus(EasyMock.anyString())).andReturn(new ArrayList<SBMSummaryAndFileInfoDTO>());
		replay(mockFileCompositeDao);

		expect(mockSBMXMLValidatorHandle.isValidXML(EasyMock.anyObject(File.class))).andReturn(true);
		expect(mockXmlValidator.validateSchemaForFileInfo(EasyMock.anyLong(), EasyMock.anyObject(File.class))).andReturn(new ArrayList<SBMErrorDTO>());
		expect(mockSBMXMLValidatorHandle.unmarshallSBMIFileInfo(EasyMock.anyObject(File.class))).andReturn(new FileInformationType());
		String expectedFileInfoXML = "<XML>FileInfo</XML>";
		expect(mockSBMXMLValidatorHandle.marshallFileInfo(EasyMock.anyObject(FileInformationType.class))).andReturn(expectedFileInfoXML);
		replay(mockXmlValidator);
		replay(mockSBMXMLValidatorHandle);
		mockFileStatusHandler.determineAndSetFileStatus(EasyMock.anyObject(SBMFileProcessingDTO.class));

		Long batchId = 2124L;
		SBMFileProcessingDTO dto = fileIngestionReader.read(batchId);

		assertNotNull("Reader should return dto", dto);
		SBMFileInfo actual = dto.getSbmFileInfo();
		
		assertEquals("TradingPartnerId", expectedTradingPartnerId , actual.getTradingPartnerId());
		assertEquals("FunctionCd", expectedFunctionCd , actual.getFunctionCd());
		assertEquals("FileNm", expectedFileNm , actual.getSbmFileNm());
		assertNotNull("FileLastModifiedDateTime", actual.getFileLastModifiedDateTime());
		assertEquals("RejectInd", expectedRejectInd , actual.isRejectedInd());
	}

	@Test
	public void testReaderZip_InvalidFileName() throws UnexpectedInputException, ParseException, NonTransientResourceException, Exception {
		FileUtils.copyFileToDirectory(new File("./src/test/resources/sbm/sbmTestFiles/zipFiles/SBMI.NY1.D160810.T100000007_2.T"), eftFolder);

		Long batchId = 2123L;
		SBMFileProcessingDTO dto = fileIngestionReader.read(batchId);

		assertNotNull("Reader should return dto", dto);

		List<SBMErrorDTO> zipErrors = dto.getErrorList();
		assertFalse("errors", zipErrors.isEmpty());

		SBMErrorDTO error = zipErrors.get(0);
		assertEquals("error code", "ER-063", error.getSbmErrorWarningTypeCd());
	}

	@Test
	public void testReaderZip_InvalidSourceId() throws UnexpectedInputException, ParseException, NonTransientResourceException, Exception {
		FileUtils.copyFileToDirectory(new File("./src/test/resources/sbm/sbmTestFiles/zipFiles/SBMI.NY1.D160810.T100000007_4.T"), eftFolder);

		Long batchId = 2123L;
		SBMFileProcessingDTO dto = fileIngestionReader.read(batchId);

		assertNotNull("Reader should return dto", dto);

		List<SBMErrorDTO> zipErrors = dto.getErrorList();
		assertFalse("errors", zipErrors.isEmpty());

		SBMErrorDTO error = zipErrors.get(0);
		assertEquals("error code", "ER-011", error.getSbmErrorWarningTypeCd());
	}
    /*MF
	@Test
	public void testValidateZipName_Valid() {

		String fileName="SRCID.EPS.SBMI.D140530.T145543452.T.IN";

		Boolean result = (Boolean)ReflectionTestUtils.invokeMethod(fileIngestionReader, "isZipEntryNameValid", fileName);
		assertTrue("isValidFileName", result);
	}

	@Test
	public void testValidateZipName_inValid() {

		String fileName="SBMI.EPS.D140530.T145543452.T.IN";

		Boolean result = (Boolean)ReflectionTestUtils.invokeMethod(fileIngestionReader, "isZipEntryNameValid", fileName);
		assertFalse("isValidFileName", result);
	}
*/
	@Test
	public void testReaderGZip() throws UnexpectedInputException, ParseException, NonTransientResourceException, Exception {

		String expectedTradingPartnerId = "EPS";
		String expectedFunctionCd = "WA1";
		String expectedFileNm = expectedFunctionCd + "." + expectedTradingPartnerId + ".SBMI.D160810.T200000007.T.IN";
		boolean expectedRejectInd = false;
		
		FileUtils.copyFileToDirectory(new File("./src/test/resources/sbm/sbmTestFiles/zipFiles/" + expectedFileNm), eftFolder);

		expect(mockFileCompositeDao.getFileStatus(EasyMock.anyString())).andReturn(new ArrayList<SBMSummaryAndFileInfoDTO>());
		replay(mockFileCompositeDao);

		expect(mockSBMXMLValidatorHandle.isValidXML(EasyMock.anyObject(File.class))).andReturn(true);
		expect(mockXmlValidator.validateSchemaForFileInfo(EasyMock.anyLong(), EasyMock.anyObject(File.class))).andReturn(new ArrayList<SBMErrorDTO>());
		expect(mockSBMXMLValidatorHandle.unmarshallSBMIFileInfo(EasyMock.anyObject(File.class))).andReturn(new FileInformationType());
		String expectedFileInfoXML = "<XML>FileInfo</XML>";
		expect(mockSBMXMLValidatorHandle.marshallFileInfo(EasyMock.anyObject(FileInformationType.class))).andReturn(expectedFileInfoXML);
		replay(mockXmlValidator);
		replay(mockSBMXMLValidatorHandle);
		mockFileStatusHandler.determineAndSetFileStatus(EasyMock.anyObject(SBMFileProcessingDTO.class));

		Long batchId = 2123L;
		SBMFileProcessingDTO dto = fileIngestionReader.read(batchId);

		assertNotNull("Reader should return dto", dto);
		SBMFileInfo actual = dto.getSbmFileInfo();
		
		assertEquals("TradingPartnerId", expectedTradingPartnerId , actual.getTradingPartnerId());
		assertEquals("FunctionCd", expectedFunctionCd , actual.getFunctionCd());
		assertEquals("FileNm", expectedFileNm , actual.getSbmFileNm());
		assertNotNull("FileLastModifiedDateTime", actual.getFileLastModifiedDateTime());
		assertEquals("FileNm", expectedRejectInd , actual.isRejectedInd());
	}
	
	
	@Test
	public void test_No_Files() throws UnexpectedInputException, ParseException, NonTransientResourceException, Exception {

		Long batchId = 2123L;
		SBMFileProcessingDTO dto = fileIngestionReader.read(batchId);
		assertNull("Reader should return null", dto);
	}
	
	
	@Test
	public void test_REJECTED_ER_012() throws UnexpectedInputException, ParseException, NonTransientResourceException, Exception {

		boolean expectedRejectInd = true;
		SBMFileStatus expectedFileStatus = SBMFileStatus.REJECTED;
		SBMErrorWarningCode expectedError = SBMErrorWarningCode.ER_012;
		
		// Use any valid file for this test
		FileUtils.copyFileToDirectory(new File("./src/test/resources/sbm/schemaErrors/SBMI_noSchemaErrors.xml"), eftFolder);
		
		List<SBMSummaryAndFileInfoDTO> dtoList = new ArrayList<SBMSummaryAndFileInfoDTO>();
		SBMSummaryAndFileInfoDTO dto = new SBMSummaryAndFileInfoDTO();
		
		dto.setSbmFileStatusType(SBMFileStatus.ACCEPTED_WITH_WARNINGS);
		SBMFileInfo sbmFileInfo = new SBMFileInfo();
		sbmFileInfo.setRejectedInd(false);
		dto.getSbmFileInfoList().add(sbmFileInfo);
		dtoList.add(dto);

		expect(mockFileCompositeDao.getFileStatus(EasyMock.anyString())).andReturn(dtoList);
		replay(mockFileCompositeDao);

		Long batchId = 2222L;
		SBMFileProcessingDTO actualDTO = fileIngestionReader.read(batchId);

		assertNotNull("Reader should return dto", actualDTO);
		
		List<SBMErrorDTO> errorList = actualDTO.getErrorList();
		
		assertEquals("ErrorList size", 1, errorList.size());
		SBMErrorDTO actualError = errorList.get(0);
		assertEquals("SbmErrorWarningTypeCd", expectedError.getCode(), actualError.getSbmErrorWarningTypeCd());
		
		assertEquals("SbmFileStatusType", expectedFileStatus , actualDTO.getSbmFileStatusType());
		
		SBMFileInfo actual = actualDTO.getSbmFileInfo();
		assertEquals("RejectInd", expectedRejectInd , actual.isRejectedInd());
	}
	
	
	@Test
	public void test_REJECTED_ER_013() throws UnexpectedInputException, ParseException, NonTransientResourceException, Exception {

		boolean expectedRejectInd = true;
		SBMFileStatus expectedFileStatus = SBMFileStatus.REJECTED;
		SBMErrorWarningCode expectedError = SBMErrorWarningCode.ER_013;
		
		// Use any valid file for this test
		FileUtils.copyFileToDirectory(new File("./src/test/resources/sbm/schemaErrors/SBMI_noSchemaErrors.xml"), eftFolder);
		
		List<SBMSummaryAndFileInfoDTO> dtoList = new ArrayList<SBMSummaryAndFileInfoDTO>();
		SBMSummaryAndFileInfoDTO dto = new SBMSummaryAndFileInfoDTO();
		
		dto.setSbmFileStatusType(SBMFileStatus.REJECTED);
		SBMFileInfo sbmFileInfo = new SBMFileInfo();
		sbmFileInfo.setRejectedInd(false);
		dto.getSbmFileInfoList().add(sbmFileInfo);
		dtoList.add(dto);

		expect(mockFileCompositeDao.getFileStatus(EasyMock.anyString())).andReturn(dtoList);
		replay(mockFileCompositeDao);
		
		// Set isValid to return false.
		expect(mockSBMXMLValidatorHandle.isValidXML(EasyMock.anyObject(File.class))).andReturn(false);
		replay(mockSBMXMLValidatorHandle);

		Long batchId = 2222L;
		SBMFileProcessingDTO actualDTO = fileIngestionReader.read(batchId);

		assertNotNull("Reader should return dto", actualDTO);
		
		List<SBMErrorDTO> errorList = actualDTO.getErrorList();
		
		assertEquals("ErrorList size", 1, errorList.size());
		SBMErrorDTO actualError = errorList.get(0);
		assertEquals("SbmErrorWarningTypeCd", expectedError.getCode(), actualError.getSbmErrorWarningTypeCd());
		
		assertEquals("SbmFileStatusType", expectedFileStatus , actualDTO.getSbmFileStatusType());
		
		SBMFileInfo actual = actualDTO.getSbmFileInfo();
		assertEquals("RejectInd", expectedRejectInd , actual.isRejectedInd());
	}
	
	@Test
	public void test_REJECTED_Schema_Errors() throws UnexpectedInputException, ParseException, NonTransientResourceException, Exception {

		boolean expectedRejectInd = true;
		SBMFileStatus expectedFileStatus = SBMFileStatus.REJECTED;
		
		// Use any valid file for this test
		FileUtils.copyFileToDirectory(new File("./src/test/resources/sbm/schemaErrors/SBMI_noSchemaErrors.xml"), eftFolder);
		
		List<SBMSummaryAndFileInfoDTO> dtoList = new ArrayList<SBMSummaryAndFileInfoDTO>();
		
		List<SBMErrorDTO> schemaErrors = new ArrayList<SBMErrorDTO>();
		schemaErrors.add(new SBMErrorDTO());
		schemaErrors.add(new SBMErrorDTO());
		schemaErrors.add(new SBMErrorDTO());
		
		expect(mockFileCompositeDao.getFileStatus(EasyMock.anyString())).andReturn(dtoList);
		replay(mockFileCompositeDao);
		
		expect(mockSBMXMLValidatorHandle.isValidXML(EasyMock.anyObject(File.class))).andReturn(true);
		expect(mockXmlValidator.validateSchemaForFileInfo(EasyMock.anyLong(), EasyMock.anyObject(File.class))).andReturn(schemaErrors);
		replay(mockXmlValidator);
        replay(mockSBMXMLValidatorHandle);
		Long batchId = 2222L;
		SBMFileProcessingDTO actualDTO = fileIngestionReader.read(batchId);

		assertNotNull("Reader should return dto", actualDTO);
		
		List<SBMErrorDTO> errorList = actualDTO.getErrorList();
		
		assertEquals("ErrorList size", schemaErrors.size(), errorList.size());
		
		
		assertEquals("SbmFileStatusType", expectedFileStatus , actualDTO.getSbmFileStatusType());
		
		SBMFileInfo actual = actualDTO.getSbmFileInfo();
		assertEquals("RejectInd", expectedRejectInd , actual.isRejectedInd());
	}
	
	@Test
	public void test_createSBMFileInfo_ZIPD() {
		
	    String expectedTradingPartnerId = "WA1";
		String expectedFunctionCd = "SBMI";
		LocalDateTime dtFile = LocalDateTime.now().minusDays(5).minusHours(1);
		LocalDateTime dtNow = LocalDateTime.of(2016, 10, 25, 11, 22, 33);
		String fileDateTime =  dtFile.format(DTF_FILE);
		String expectedFileNm = "WA1.EPS.SBMI." + fileDateTime + ".T";
		String fileNm = expectedFileNm + SBMConstants.ZIPD + dtNow.format(DTF_ZIP);
		
		ZonedDateTime zdt = ZonedDateTime.now();
		long lastModifiedTimeMillis = zdt.toInstant().toEpochMilli();
		
		SBMFileInfo actual = (SBMFileInfo) ReflectionTestUtils.invokeMethod(fileIngestionReader, "createSBMFileInfo", fileNm, lastModifiedTimeMillis);
		
		assertEquals("TradingPartnerId", expectedTradingPartnerId , actual.getTradingPartnerId());
		assertEquals("FunctionCd", expectedFunctionCd , actual.getFunctionCd());
		assertEquals("FileNm", expectedFileNm , actual.getSbmFileNm());
		assertNotNull("FileLastModifiedDateTime", actual.getFileLastModifiedDateTime());
		assertEquals("FileNm", expectedFileNm , actual.getSbmFileNm());
	}
	
	@Test
	public void test_createSBMFileInfo_GZIPD() {
		
	    String expectedTradingPartnerId = "EPS";
		String expectedFunctionCd = "WA1";
		LocalDateTime dtFile = LocalDateTime.now().minusDays(5).minusHours(1);
		LocalDateTime dtNow = LocalDateTime.of(2016, 10, 25, 11, 22, 33);
		String fileDateTime =  dtFile.format(DTF_FILE);
		String expectedFileNm = "WA1.EPS.SBMI." + fileDateTime + ".T";
		String fileNm = expectedFileNm + SBMConstants.GZIPD + dtNow.format(DTF_ZIP);
		
		ZonedDateTime zdt = ZonedDateTime.now();
		long lastModifiedTimeMillis = zdt.toInstant().toEpochMilli();
		
		SBMFileInfo actual = (SBMFileInfo) ReflectionTestUtils.invokeMethod(fileIngestionReader, "createSBMFileInfo", fileNm, lastModifiedTimeMillis);
		
		assertEquals("TradingPartnerId", expectedTradingPartnerId , actual.getTradingPartnerId());
		assertEquals("FunctionCd", expectedFunctionCd , actual.getFunctionCd());
		assertEquals("FileNm", expectedFileNm , actual.getSbmFileNm());
		assertNotNull("FileLastModifiedDateTime", actual.getFileLastModifiedDateTime());
		assertEquals("FileNm", expectedFileNm , actual.getSbmFileNm());
		
	}


}
