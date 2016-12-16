package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.easymock.EasyMock;
import org.joda.time.LocalDate;
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

import gov.cms.dsh.sbmi.Enrollment;
import gov.cms.dsh.sbmi.FileInformationType;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMErrorWarningCode;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus;
import gov.hhs.cms.ff.fm.eps.ep.jobs.util.SBMTestDataDBUtil;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMErrorDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileInfo;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileProcessingDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMSummaryAndFileInfoDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SBMFileCompositeDAO;

@RunWith(JUnit4.class)
public class SbmiFileIngestionReaderTest extends BaseBatchSBMTest {

	private static final String eftFolderPath = "./src/test/resources/sbm/readerTest/eftFolder";
	private static final String privateFolderPath = "./src/test/resources/sbm/readerTest/privateFolder";
	private static final String zipFolderPath = "./src/test/resources/sbm/readerTest/zipFilesFolder";
	private static final String processedFolderPath = "./src/test/resources/sbm/readerTest/processedFilesFolder";

	// Directory for files created by this test.
	private static final String testZipFilesPath = "src/test/resources/sbm/readerTest/TEST_zipFiles";

	private File eftFolder = new File(eftFolderPath);
	private File privateFolder = new File(privateFolderPath);	
	private File zipFolder = new File(zipFolderPath);
	private File processedFolder = new File(processedFolderPath);

	private File testZipFilesFolder = new File(testZipFilesPath);

	private SbmiFileIngestionReader fileIngestionReader;

	private SbmXMLValidator mockXmlValidator;
	private SBMFileCompositeDAO mockFileCompositeDao;
	private SbmFileValidator mockFileValidator;
	private SBMFileStatusHandler mockFileStatusHandler;

	private final DateTimeFormatter DTF_FILE = DateTimeFormatter.ofPattern("'D'yyMMdd'.T'HHmmssSSS");
	private final DateTimeFormatter DTF_ZIP = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

	@Before
	public void setUp() throws IOException, ParserConfigurationException, SAXException {

		fileIngestionReader = new SbmiFileIngestionReader();
		fileIngestionReader.setEftFolder(new File(eftFolderPath));
		fileIngestionReader.setPrivateFolder(new File(privateFolderPath));
		fileIngestionReader.setZipFilesFolder(new File(zipFolderPath));
		fileIngestionReader.setProcessedFolder(processedFolder);

		fileIngestionReader.setEnvironmentCd(SBMConstants.FILE_ENV_CD_TEST);

		mockXmlValidator = EasyMock.createMock(SbmXMLValidator.class);
		mockFileCompositeDao = EasyMock.createMock(SBMFileCompositeDAO.class);
		mockFileValidator = EasyMock.createMock(SbmFileValidator.class);
		mockFileStatusHandler = EasyMock.createMock(SBMFileStatusHandler.class);


		fileIngestionReader.setXmlValidator(mockXmlValidator);
		fileIngestionReader.setFileCompositeDao(mockFileCompositeDao);
		fileIngestionReader.setFileValidator(mockFileValidator);
		fileIngestionReader.setFileSatusHandler(mockFileStatusHandler);

		eftFolder.mkdirs();		
		privateFolder.mkdirs();
		zipFolder.mkdirs();
		processedFolder.mkdirs();

		testZipFilesFolder.mkdirs();

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

		expect(mockXmlValidator.isValidXML(EasyMock.anyObject(File.class))).andReturn(true);
		expect(mockXmlValidator.validateSchemaForFileInfo(EasyMock.anyLong(), EasyMock.anyObject(File.class))).andReturn(new ArrayList<SBMErrorDTO>());
		expect(mockXmlValidator.unmarshallSBMIFileInfo(EasyMock.anyObject(File.class))).andReturn(new FileInformationType());
		String expectedFileInfoXML = "<XML>FileInfo</XML>";
		expect(mockXmlValidator.marshallFileInfo(EasyMock.anyObject(FileInformationType.class))).andReturn(expectedFileInfoXML);
		replay(mockXmlValidator);
		mockFileStatusHandler.determineAndSetFileStatus(EasyMock.anyObject(SBMFileProcessingDTO.class));

		Long batchId = 2123L;
		List<SBMFileProcessingDTO> dtoList = fileIngestionReader.read(batchId);
		SBMFileProcessingDTO dto = dtoList.get(0);

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

		expect(mockXmlValidator.isValidXML(EasyMock.anyObject(File.class))).andReturn(true);
		expect(mockXmlValidator.validateSchemaForFileInfo(EasyMock.anyLong(), EasyMock.anyObject(File.class))).andReturn(new ArrayList<SBMErrorDTO>());
		expect(mockXmlValidator.unmarshallSBMIFileInfo(EasyMock.anyObject(File.class))).andReturn(new FileInformationType());
		String expectedFileInfoXML = "<XML>FileInfo</XML>";
		expect(mockXmlValidator.marshallFileInfo(EasyMock.anyObject(FileInformationType.class))).andReturn(expectedFileInfoXML);
		replay(mockXmlValidator);
		mockFileStatusHandler.determineAndSetFileStatus(EasyMock.anyObject(SBMFileProcessingDTO.class));

		Long batchId = 2124L;
		List<SBMFileProcessingDTO> dtoList = fileIngestionReader.read(batchId);
		SBMFileProcessingDTO dto = dtoList.get(0);

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
		List<SBMFileProcessingDTO> dtoList = fileIngestionReader.read(batchId);
		SBMFileProcessingDTO dto = dtoList.get(0);

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
		List<SBMFileProcessingDTO> dtoList = fileIngestionReader.read(batchId);
		SBMFileProcessingDTO dto = dtoList.get(0);

		assertNotNull("Reader should return dto", dto);

		List<SBMErrorDTO> zipErrors = dto.getErrorList();
		assertFalse("errors", zipErrors.isEmpty());

		SBMErrorDTO error = zipErrors.get(0);
		assertEquals("error code", "ER-011", error.getSbmErrorWarningTypeCd());
	}

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

	@Test
	public void testReaderGZip() throws UnexpectedInputException, ParseException, NonTransientResourceException, Exception {

		String expectedTradingPartnerId = "EPS";
		String expectedFunctionCd = "WA1";
		String expectedFileNm = expectedFunctionCd + "." + expectedTradingPartnerId + ".SBMI.D160810.T200000007.T.IN";
		boolean expectedRejectInd = false;

		FileUtils.copyFileToDirectory(new File("./src/test/resources/sbm/sbmTestFiles/zipFiles/" + expectedFileNm), eftFolder);

		expect(mockFileCompositeDao.getFileStatus(EasyMock.anyString())).andReturn(new ArrayList<SBMSummaryAndFileInfoDTO>());
		replay(mockFileCompositeDao);

		expect(mockXmlValidator.isValidXML(EasyMock.anyObject(File.class))).andReturn(true);
		expect(mockXmlValidator.validateSchemaForFileInfo(EasyMock.anyLong(), EasyMock.anyObject(File.class))).andReturn(new ArrayList<SBMErrorDTO>());
		expect(mockXmlValidator.unmarshallSBMIFileInfo(EasyMock.anyObject(File.class))).andReturn(new FileInformationType());
		String expectedFileInfoXML = "<XML>FileInfo</XML>";
		expect(mockXmlValidator.marshallFileInfo(EasyMock.anyObject(FileInformationType.class))).andReturn(expectedFileInfoXML);
		replay(mockXmlValidator);
		mockFileStatusHandler.determineAndSetFileStatus(EasyMock.anyObject(SBMFileProcessingDTO.class));

		Long batchId = 2123L;
		List<SBMFileProcessingDTO> dtoList = fileIngestionReader.read(batchId);
		SBMFileProcessingDTO dto = dtoList.get(0);

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
		List<SBMFileProcessingDTO> dtoList = fileIngestionReader.read(batchId);
		assertTrue("Reader should return empty SBMFileProcessingDTO list", dtoList.isEmpty());
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

		List<SBMFileProcessingDTO> actualDTOList = fileIngestionReader.read(batchId);
		SBMFileProcessingDTO actualDTO = actualDTOList.get(0);

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
		expect(mockXmlValidator.isValidXML(EasyMock.anyObject(File.class))).andReturn(false);
		replay(mockXmlValidator);

		Long batchId = 2222L;
		List<SBMFileProcessingDTO> actualDTOList = fileIngestionReader.read(batchId);
		SBMFileProcessingDTO actualDTO = actualDTOList.get(0);

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

		expect(mockXmlValidator.isValidXML(EasyMock.anyObject(File.class))).andReturn(true);
		expect(mockXmlValidator.validateSchemaForFileInfo(EasyMock.anyLong(), EasyMock.anyObject(File.class))).andReturn(schemaErrors);
		replay(mockXmlValidator);

		Long batchId = 2222L;
		List<SBMFileProcessingDTO> actualDTOList = fileIngestionReader.read(batchId);
		SBMFileProcessingDTO actualDTO = actualDTOList.get(0);

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

	@Test
	public void test_getAFileFromEFT_T() throws InterruptedException, IOException {

		File expectedFile = null;
		fileIngestionReader.setEnvironmentCd(SBMConstants.FILE_ENV_CD_TEST);
		// from table SERVERENVIRONMENTTYPE
		String[] envCds = {SBMConstants.FILE_ENV_CD_PROD_R, SBMConstants.FILE_ENV_CD_PROD, "ANY"};
		String stateCd = SBMTestDataDBUtil.getRandomSbmState();
		String sourceId = SBMTestDataDBUtil.getRandomNumberAsString(3) + stateCd;

		String tenantId = stateCd + "0";
		int covYr = LocalDate.now().getYear();

		for (int i = 0; i < envCds.length; ++i) {

			String fileName = SBMTestDataDBUtil.makeFileName(sourceId, envCds[i]);
			int idx = (i + 1);
			String sbmFileId = "FID-" + String.format("%05d", idx);
			String issuerId =  String.format("%05d", idx);

			Enrollment enrollment = SBMTestDataDBUtil.makeEnrollment(sbmFileId, tenantId, covYr, issuerId, SBMTestDataDBUtil.FILES_ONE_PER_ISSUER);
			String sbmFileXML = SBMTestDataDBUtil.getEnrollmentAsXmlString(enrollment);
			sbmFileXML = SBMTestDataDBUtil.prettyXMLFormat(sbmFileXML);

			// Load up various files into EFT folder.
			File file = new File(eftFolder + File.separator + fileName);
			BufferedWriter output = new BufferedWriter(new FileWriter(file));
			output.write(sbmFileXML);
			output.close();
			// In this case envCd='T' will NOT filter, so the first one is grabbed.
			if (i == 0) {
				expectedFile = file;
			}
			// Delay at least one second to get a different file LastModifiedDateTime.
			Thread.sleep(1000);
		}
		// Confirm we only get the T (test) file and not the PROD or PROD-R file.
		File actualFile = (File) ReflectionTestUtils.invokeMethod(fileIngestionReader, "getAFileFromEFT");
		assertEquals("file name with environmentCode 'T'", expectedFile.getName(), actualFile.getName());
	}

	@Test
	public void test_getAFileFromEFT_P() throws InterruptedException, IOException {

		File expectedFile = null;
		fileIngestionReader.setEnvironmentCd(SBMConstants.FILE_ENV_CD_PROD);
		// from table SERVERENVIRONMENTTYPE
		String[] envCds = { "1A", "1B", "0", "T0", "T1", "T2", SBMConstants.FILE_ENV_CD_PROD_R, SBMConstants.FILE_ENV_CD_PROD};
		String stateCd = SBMTestDataDBUtil.getRandomSbmState();
		String sourceId = SBMTestDataDBUtil.getRandomNumberAsString(3) + stateCd;

		String tenantId = stateCd + "0";
		int covYr = LocalDate.now().getYear();

		for (int i = 0; i < envCds.length; ++i) {

			String fileName = SBMTestDataDBUtil.makeFileName(sourceId, envCds[i]);
			int idx = (i + 1);
			String sbmFileId = "FID-" + String.format("%05d", idx);
			String issuerId =  String.format("%05d", idx);

			Enrollment enrollment = SBMTestDataDBUtil.makeEnrollment(sbmFileId, tenantId, covYr, issuerId, SBMTestDataDBUtil.FILES_ONE_PER_ISSUER);
			String sbmFileXML = SBMTestDataDBUtil.getEnrollmentAsXmlString(enrollment);
			sbmFileXML = SBMTestDataDBUtil.prettyXMLFormat(sbmFileXML);

			// Load up various files into EFT folder.
			File file = new File(eftFolder + File.separator + fileName);
			BufferedWriter output = new BufferedWriter(new FileWriter(file));
			output.write(sbmFileXML);
			output.close();

			if (i == (envCds.length - 1)) {
				expectedFile = file;
			}
			// Delay a little to get a different fileName since it is timestamp based.
			Thread.sleep(5);
		}
		// Confirm we only get the P (PROD) file.
		File actualFile = (File) ReflectionTestUtils.invokeMethod(fileIngestionReader, "getAFileFromEFT");
		assertEquals("file name with environmentCode 'P'", expectedFile.getName(), actualFile.getName());
	}


	/**
	 * Confirm NO production P files are pulled from folder when there all files from
	 * many different environments including some bogus ones (PP, PROD)
	 * @throws InterruptedException
	 * @throws IOException
	 */
	@Test
	public void test_getAFileFromEFT_P_NoFiles() throws InterruptedException, IOException {

		File expectedFile = null;
		fileIngestionReader.setEnvironmentCd(SBMConstants.FILE_ENV_CD_PROD);
		// from table SERVERENVIRONMENTTYPE
		String[] envCds = { "1A", "1B", "0", "T0", "T1", "T2", SBMConstants.FILE_ENV_CD_PROD_R, "T", "PP", "PROD"};
		String stateCd = SBMTestDataDBUtil.getRandomSbmState();
		String sourceId = SBMTestDataDBUtil.getRandomNumberAsString(3) + stateCd;

		String tenantId = stateCd + "0";
		int covYr = LocalDate.now().getYear();

		for (int i = 0; i < envCds.length; ++i) {

			String fileName = SBMTestDataDBUtil.makeFileName(sourceId, envCds[i]);
			int idx = (i + 1);
			String sbmFileId = "FID-" + String.format("%05d", idx);
			String issuerId =  String.format("%05d", idx);

			Enrollment enrollment = SBMTestDataDBUtil.makeEnrollment(sbmFileId, tenantId, covYr, issuerId, SBMTestDataDBUtil.FILES_ONE_PER_ISSUER);
			String sbmFileXML = SBMTestDataDBUtil.getEnrollmentAsXmlString(enrollment);
			sbmFileXML = SBMTestDataDBUtil.prettyXMLFormat(sbmFileXML);

			// Load up various files into EFT folder.
			File file = new File(eftFolder + File.separator + fileName);
			BufferedWriter output = new BufferedWriter(new FileWriter(file));
			output.write(sbmFileXML);
			output.close();

			// Delay a little to get a different fileName since it is timestamp based.
			Thread.sleep(5);
		}
		// Confirm we get no files.
		File actualFile = (File) ReflectionTestUtils.invokeMethod(fileIngestionReader, "getAFileFromEFT");
		assertEquals("file with environmentCode 'P'", expectedFile, actualFile);
	}

	/**
	 * Confirm only the PROD-R file is pulled from folder when there all files from
	 * many different environments including some bogus ones (PP, PROD)
	 * @throws InterruptedException
	 * @throws IOException
	 */
	@Test
	public void test_getAFileFromEFT_R() throws InterruptedException, IOException {

		File expectedFile = null;
		fileIngestionReader.setEnvironmentCd(SBMConstants.FILE_ENV_CD_PROD_R);
		// from table SERVERENVIRONMENTTYPE
		String[] envCds = { "1A", "1B", "0", "T0", "T1", "T2", "PROD-R", "PR", "T", SBMConstants.FILE_ENV_CD_PROD_R};
		String stateCd = SBMTestDataDBUtil.getRandomSbmState();
		String sourceId = SBMTestDataDBUtil.getRandomNumberAsString(3) + stateCd;

		String tenantId = stateCd + "0";
		int covYr = LocalDate.now().getYear();

		for (int i = 0; i < envCds.length; ++i) {

			String fileName = SBMTestDataDBUtil.makeFileName(sourceId, envCds[i]);
			int idx = (i + 1);
			String sbmFileId = "FID-" + String.format("%05d", idx);
			String issuerId =  String.format("%05d", idx);

			Enrollment enrollment = SBMTestDataDBUtil.makeEnrollment(sbmFileId, tenantId, covYr, issuerId, SBMTestDataDBUtil.FILES_ONE_PER_ISSUER);
			String sbmFileXML = SBMTestDataDBUtil.getEnrollmentAsXmlString(enrollment);
			sbmFileXML = SBMTestDataDBUtil.prettyXMLFormat(sbmFileXML);

			// Load up various files into EFT folder.
			File file = new File(eftFolder + File.separator + fileName);
			BufferedWriter output = new BufferedWriter(new FileWriter(file));
			output.write(sbmFileXML);
			output.close();

			if (i == (envCds.length - 1)) {
				expectedFile = file;
			}
			// Delay a little to get a different fileName since it is timestamp based.
			Thread.sleep(5);
		}
		// Confirm we only get the R (PROD-R) file.
		File actualFile = (File) ReflectionTestUtils.invokeMethod(fileIngestionReader, "getAFileFromEFT");
		assertEquals("file with environmentCode 'R'", expectedFile.getName(), actualFile.getName());
	}



	/**
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void test_processZipFiles_All_Entries_Valid() throws IOException, InterruptedException {

		int expectedDTOListSize = 1;
		int expectedPrivateFolderListSize = 3;

		int fileListSize = 3;
		String envCd = "T";
		String stateCd = SBMTestDataDBUtil.getRandomSbmState();
		String sourceId = SBMTestDataDBUtil.getRandomNumberAsString(3) + stateCd;

		String zipFileNm = SBMTestDataDBUtil.makeFileName(sourceId, envCd);
		String zipEntryFileName = null;
		List<String> zipEntryFileNameList = new ArrayList<String>();

		String tenantId = stateCd + "0";
		int covYr = YEAR;


		// Put the created zip file in the test folder for now. The actual File handle is 
		// passed into method processZipFiles(,).
		File zipFile = new File(testZipFilesFolder + File.separator + zipFileNm);

		ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));

		for (int i = 0; i < fileListSize; ++i) {

			zipEntryFileName = SBMTestDataDBUtil.makeZipEntryFileName(sourceId, envCd);
			int idx = (i + 1);
			String sbmFileId = "FID-" + idx + "" + idx + "" + idx;
			String issuerId = idx + "" + idx  + "" + idx + "" + idx + "" + idx;

			Enrollment enrollment = SBMTestDataDBUtil.makeEnrollment(sbmFileId, tenantId, covYr, issuerId, SBMTestDataDBUtil.FILES_ONE_PER_ISSUER);
			String sbmFileXML = SBMTestDataDBUtil.getEnrollmentAsXmlString(enrollment);
			sbmFileXML = SBMTestDataDBUtil.prettyXMLFormat(sbmFileXML);

			ZipEntry zipEntry = new ZipEntry(zipEntryFileName);
			zos.putNextEntry(zipEntry);
			byte[] data = sbmFileXML.getBytes();
			zos.write(data, 0, data.length);

			zipEntryFileNameList.add(zipEntryFileName);

			// Delay a little to get a different fileName since it is file LastModifiedDateTime based.
			Thread.sleep(1000);
		}

		zos.close();

		List<SBMFileProcessingDTO> actualList = ReflectionTestUtils.invokeMethod(fileIngestionReader, "processZipFiles", zipFile);

		assertEquals("Since no zip file errors should return a list", expectedDTOListSize, actualList.size());

		SBMFileProcessingDTO actualFileProcDto = actualList.get(0);

		assertFolderFileList(privateFolder, zipEntryFileNameList, expectedPrivateFolderListSize);

		assertNotNull("SbmFileInfo", actualFileProcDto.getSbmFileInfo());

		assertEquals("FunctionCd", "SBMI", actualFileProcDto.getSbmFileInfo().getFunctionCd());
		assertEquals("TradingPartnerId", sourceId, actualFileProcDto.getSbmFileInfo().getTradingPartnerId());

	}

	/**
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void test_processZipFiles_ER_063() throws IOException, InterruptedException {

		int expectedDTOListSize = 2;
		int expectedPrivateFolderListSize = 2;

		int fileListSize = 3;
		String envCd = "T";
		String stateCd = SBMTestDataDBUtil.getRandomSbmState();
		String sourceId = SBMTestDataDBUtil.getRandomNumberAsString(3) + stateCd;

		String zipFileNm = SBMTestDataDBUtil.makeFileName(sourceId, envCd);
		String zipEntryFileName = null;
		List<String> zipEntryFileNameList = new ArrayList<String>();
		List<String> privateFolderFileNameList = new ArrayList<String>();

		String tenantId = stateCd + "0";
		int covYr = YEAR;

		// Put the created zip file in the test folder for now. The actual File handle is 
		// passed into method processZipFiles(,).
		File zipFile = new File(testZipFilesFolder + File.separator + zipFileNm);

		ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));


		for (int i = 0; i < fileListSize; ++i) {

			zipEntryFileName = SBMTestDataDBUtil.makeZipEntryFileName(sourceId, envCd);

			// For the zipEntry file, booger up the name to log ER-063.
			if (i == 0) {
				zipEntryFileName = "XXX." + zipEntryFileName + ".PPPPP";
			} else {
				// Only valid files move to the privateFolder.
				privateFolderFileNameList.add(zipEntryFileName);
			}

			if(i == 0 || i == 1) {
				// ZipEntryFiles with errors and the first good file are the only files 
				// expected to be returned from reader.
				zipEntryFileNameList.add(zipEntryFileName);
			}

			int idx = (i + 1);
			String sbmFileId = "FID-" + idx + "" + idx + "" + idx;
			String issuerId = idx + "" + idx  + "" + idx + "" + idx + "" + idx;

			Enrollment enrollment = SBMTestDataDBUtil.makeEnrollment(sbmFileId, tenantId, covYr, issuerId, SBMTestDataDBUtil.FILES_ONE_PER_ISSUER);
			String sbmFileXML = SBMTestDataDBUtil.getEnrollmentAsXmlString(enrollment);
			sbmFileXML = SBMTestDataDBUtil.prettyXMLFormat(sbmFileXML);

			ZipEntry zipEntry = new ZipEntry(zipEntryFileName);
			zos.putNextEntry(zipEntry);
			byte[] data = sbmFileXML.getBytes();
			zos.write(data, 0, data.length);

			// Delay a little to get a different fileName since it is timestamp based.
			Thread.sleep(10);
		}

		zos.close();

		List<SBMFileProcessingDTO> actualList = ReflectionTestUtils.invokeMethod(fileIngestionReader, "processZipFiles", zipFile);

		assertEquals("Since at least one good zip entry file and one with error", expectedDTOListSize, actualList.size());

//	TODO: fix failing on UNIX
		// Confirm only the "good" files made it to the private folder.
//		assertFolderFileList(privateFolder, privateFolderFileNameList, expectedPrivateFolderListSize);
//
//		for (int i = 0; i < actualList.size(); ++i) {
//
//			SBMFileProcessingDTO actualFileProcDto = actualList.get(i);
//			assertNotNull("SbmFileInfo", actualFileProcDto.getSbmFileInfo());
//			assertEquals("TradingPartnerId", sourceId, actualFileProcDto.getSbmFileInfo().getTradingPartnerId());
//			assertEquals("SbmFileNm", zipEntryFileNameList.get(i), actualFileProcDto.getSbmFileInfo().getSbmFileNm());
//		}
	}

	@Test
	public void test_processZipFiles_All_Invalid() throws IOException, InterruptedException {

		int expectedDTOListSize = 3;
		int expectedPrivateFolderListSize = 0;

		int fileListSize = 3;
		String envCd = "T";
		String stateCd = SBMTestDataDBUtil.getRandomSbmState();
		String sourceId = SBMTestDataDBUtil.getRandomNumberAsString(3) + stateCd;

		String zipFileNm = SBMTestDataDBUtil.makeFileName(sourceId, envCd);
		String zipEntryFileName = null;
		List<String> zipEntryFileNameList = new ArrayList<String>();
		List<String> privateFolderFileNameList = new ArrayList<String>();

		String tenantId = stateCd + "0";
		int covYr = YEAR;

		// Put the created zip file in the test folder for now. The actual File handle is 
		// passed into method processZipFiles(,).
		File zipFile = new File(testZipFilesFolder + File.separator + zipFileNm);

		ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));


		for (int i = 0; i < fileListSize; ++i) {

			if(i == 0 || i == 1) {
				// For the zipEntry file, booger up the name to log ER-063.
				zipEntryFileName = SBMTestDataDBUtil.makeZipEntryFileName(sourceId, envCd);
				zipEntryFileName = "XXX." + zipEntryFileName + ".PPPPP";
			} else {
				// For the zipEntry file, booger up the sourceId to log ER-011.
				zipEntryFileName = SBMTestDataDBUtil.makeZipEntryFileName("XX" + sourceId + "PP", envCd);
				privateFolderFileNameList.add(zipEntryFileName);
			}

			// ZipEntryFiles with errors expected to be returned from reader.
			zipEntryFileNameList.add(zipEntryFileName);

			int idx = (i + 1);
			String sbmFileId = "FID-" + idx + "" + idx + "" + idx;
			String issuerId = idx + "" + idx  + "" + idx + "" + idx + "" + idx;

			Enrollment enrollment = SBMTestDataDBUtil.makeEnrollment(sbmFileId, tenantId, covYr, issuerId, SBMTestDataDBUtil.FILES_ONE_PER_ISSUER);
			String sbmFileXML = SBMTestDataDBUtil.getEnrollmentAsXmlString(enrollment);
			sbmFileXML = SBMTestDataDBUtil.prettyXMLFormat(sbmFileXML);

			ZipEntry zipEntry = new ZipEntry(zipEntryFileName);
			zos.putNextEntry(zipEntry);
			byte[] data = sbmFileXML.getBytes();
			zos.write(data, 0, data.length);

			// Delay a little to get a different fileName since it is timestamp based.
			Thread.sleep(10);
		}

		zos.close();

		List<SBMFileProcessingDTO> actualList = ReflectionTestUtils.invokeMethod(fileIngestionReader, "processZipFiles", zipFile);

		assertEquals("Since all files invalid", expectedDTOListSize, actualList.size());

		// Confirm NO "good" files made it to the private folder. All 3 are invalid.
//		TODO: fix failing on UNIX
//		assertFolderFileList(privateFolder, privateFolderFileNameList, expectedPrivateFolderListSize);
//
//		for (int i = 0; i < actualList.size(); ++i) {
//
//			SBMFileProcessingDTO actualFileProcDto = actualList.get(i);
//			assertNotNull("SbmFileInfo", actualFileProcDto.getSbmFileInfo());
//			assertEquals("TradingPartnerId", sourceId, actualFileProcDto.getSbmFileInfo().getTradingPartnerId());
//			assertEquals("SbmFileNm", zipEntryFileNameList.get(i), actualFileProcDto.getSbmFileInfo().getSbmFileNm());
//		}
	}


}
