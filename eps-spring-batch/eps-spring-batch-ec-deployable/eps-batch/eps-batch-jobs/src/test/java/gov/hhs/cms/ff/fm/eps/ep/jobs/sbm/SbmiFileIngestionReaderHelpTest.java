package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.test.util.ReflectionTestUtils;
import org.xml.sax.SAXException;

import gov.cms.dsh.sbmi.FileInformationType;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMErrorWarningCode;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus;
import gov.hhs.cms.ff.fm.eps.ep.jobs.CommonUtil;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMErrorDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileInfo;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileProcessingDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMSummaryAndFileInfoDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SBMFileCompositeDAO;

public class SbmiFileIngestionReaderHelpTest {
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
	private SbmiFileIngestionReaderHelp sbmiFileIngestionReaderHelp;
	private final DateTimeFormatter DTF_FILE = DateTimeFormatter.ofPattern("'D'yyMMdd'.T'HHmmssSSS");
	private final DateTimeFormatter DTF_ZIP = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
	private SBMFileProcessingDTO dto;
	private DateTimeFormatter zipFormatter = DateTimeFormatter.ofPattern(SBMConstants.FILENAME_ZIP_PATTERN);
	@Before
	public void setUp() throws IOException, ParserConfigurationException, SAXException {

		fileIngestionReader = new SbmiFileIngestionReader();
		fileIngestionReader.setEftFolder(new File(eftFolderPath));
		fileIngestionReader.setPrivateFolder(new File(privateFolderPath));
		fileIngestionReader.setZipFilesFolder(new File(zipFolderPath));
		fileIngestionReader.setProcessedFolder(processedFolder);
		sbmiFileIngestionReaderHelp = new SbmiFileIngestionReaderHelp();
        
		mockXmlValidator = EasyMock.createMock(SbmXMLValidator.class);
		mockSBMXMLValidatorHandle = EasyMock.createMock(SBMXMLValidatorHandle.class);
		mockFileCompositeDao = EasyMock.createMock(SBMFileCompositeDAO.class);
		mockFileValidator = EasyMock.createMock(SbmFileValidator.class);
		mockFileStatusHandler = EasyMock.createMock(SBMFileStatusHandler.class);


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
	/*
	@Test
	public void testReaderZip_InvalidSourceId() throws UnexpectedInputException, ParseException, NonTransientResourceException, Exception {
		FileUtils.copyFileToDirectory(new File("./src/test/resources/sbm/sbmTestFiles/zipFiles/SBMI.NY1.D160810.T100000007_4.T"), eftFolder);
	//	FileUtils.cleanDirectory(eftFolder);
		Long batchId = 2123L;
		dto = getAFileToProcessFromEFT();
		sbmiFileIngestionReaderHelp.processZipFiles(eftFolder, dto, zipFolder, privateFolder, processedFolder);
		assertNotNull("Reader should return dto", dto);
		
		List<SBMErrorDTO> zipErrors = dto.getErrorList();
		assertFalse("errors", zipErrors.isEmpty());
		
		SBMErrorDTO error = zipErrors.get(0);
		assertEquals("error code", "ER-011", error.getSbmErrorWarningTypeCd());
	}
	*/
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
		/*
		dto = getAFileToProcessFromEFT();

		File fileToValidate = dto.getSbmiFile();
		boolean isZip = false;

		if(!SbmHelper.isValidZip(fileToValidate)) {
			

			isZip = false;

		} else {
			isZip = true;
			//move to zipped folder
			sbmiFileIngestionReaderHelp.processZipFiles(eftFolder, dto, zipFolder, privateFolder, processedFolder); 
		}

		boolean isGzip = false;

		if(!isZip) {
			isGzip = SbmHelper.isGZipped(fileToValidate); 
		}

	//	if (isGzip) {
	//		processGzip(fileToValidate, dto);
	//	}
		
//		assertNotNull("Reader should return dto", dto);
//		SBMFileInfo actual = dto.getSbmFileInfo();
 * 
 */
	}
	
	@Test
	public void testValidateZipName_Valid() {

		String fileName="SRCID.EPS.SBMI.D140530.T145543452.T.IN";

		Boolean result = (Boolean)ReflectionTestUtils.invokeMethod(sbmiFileIngestionReaderHelp, "isZipEntryNameValid",fileName);
		assertTrue("isValidFileName", result);
	}

	@Test
	public void testValidateZipName_inValid() {

		String fileName="SBMI.EPS.D140530.T145543452.T.IN";

		Boolean result = (Boolean)ReflectionTestUtils.invokeMethod(sbmiFileIngestionReaderHelp, "isZipEntryNameValid", fileName);
		assertFalse("isValidFileName", result);
	}
/*
	private SBMFileProcessingDTO getAFileToProcessFromEFT() throws IOException {

		File fileFromEFT = null;
		File sbmiFile = null;
		String filename = "";
		long lastModifiedTimeMillis = 0;

		//try until no files in EFT
		while((fileFromEFT = getAFileFromEFT()) != null) {
			filename = fileFromEFT.getName();
			lastModifiedTimeMillis = fileFromEFT.lastModified();
			sbmiFile = lockFile(fileFromEFT);
			if(sbmiFile != null) {
				break;
			}	
			
		}		

		if(sbmiFile == null) {
			//no files in EFT
			
			return null;
		}

		SBMFileInfo fileInfo = createSBMFileInfo(filename, lastModifiedTimeMillis);

		SBMFileProcessingDTO sbmFileProcDto = new SBMFileProcessingDTO();
		sbmFileProcDto.setSbmFileInfo(fileInfo);
		sbmFileProcDto.setSbmiFile(sbmiFile);

		return sbmFileProcDto;
	}
	
	private File getAFileFromEFT() {
		List<File> filesList = CommonUtil.getFilesFromDir(eftFolder);
		
		if(CollectionUtils.isEmpty(filesList)) {
			return null;
		}		

		File fileFromEFT = filesList.get(0);
		
		return fileFromEFT;
	}
     
	private SBMFileInfo createSBMFileInfo(String filename, long lastModifiedTimeMillis) {

		SBMFileInfo fileInfo = new SBMFileInfo();	

		
		if(StringUtils.contains(filename, SBMConstants.ZIPD)) {
			//filename format: TradingPartnerID.AppId.FuncCode.Date.Time.EnvCode.Direction_ZIPDyyyyMMddTHHmmss
			String actualFilename = StringUtils.substringBefore(filename, SBMConstants.ZIPD);
			
			fileInfo.setSbmFileNm(actualFilename);
			String dateTimeString =  StringUtils.difference(actualFilename, filename);
			
			fileInfo.setFileLastModifiedDateTime(LocalDateTime.parse(dateTimeString, zipFormatter));
			fileInfo.setFunctionCd(SbmHelper.getFunctionCodeFromPreEFTFormat(actualFilename)); 
			fileInfo.setTradingPartnerId(SbmHelper.getTradingPartnerIdFromPreEFTFormat(actualFilename)); 				
		}
		else if(StringUtils.contains(filename, SBMConstants.GZIPD)) {
			//filename format: FuncCode.TradingPartnerID.Date.Time.EnvCode.Direction_GZIPDyyyyMMddTHHmmss
			String actualFilename = StringUtils.substringBefore(filename, SBMConstants.GZIPD);
			fileInfo.setSbmFileNm(actualFilename);			
			String dateTimeString =  StringUtils.difference(actualFilename, filename);
		//	fileInfo.setFileLastModifiedDateTime(LocalDateTime.parse(dateTimeString, gzipFormatter));
			fileInfo.setFunctionCd(SbmHelper.getFunctionCodeFromFile(actualFilename)); 
			fileInfo.setTradingPartnerId(SbmHelper.getTradingPartnerId(actualFilename)); 
		}
		else {
			//filename format: FuncCode.TradingPartnerID.Date.Time.EnvCode.Direction
			fileInfo.setSbmFileNm(filename);
			if(lastModifiedTimeMillis > 0) {
				fileInfo.setFileLastModifiedDateTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(lastModifiedTimeMillis), ZoneId.systemDefault()));
			}
			fileInfo.setFunctionCd(SbmHelper.getFunctionCodeFromFile(filename)); 
			fileInfo.setTradingPartnerId(SbmHelper.getTradingPartnerId(filename)); 
		}

		
		return fileInfo;
	}

	private File lockFile(File source) {
		File newFile = new File(privateFolder, source.getName() + SBMConstants.FILESUFFIX_LOCK);
		try {
			
			FileUtils.moveFile(source, newFile);
		} catch (IOException e) {
			
			return null;
		}

		return newFile;
	}
 */
}
