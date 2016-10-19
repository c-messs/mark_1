package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
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

import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMErrorDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileProcessingDTO;
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
	private SbmXMLValidator xmlValidator;
	private SBMFileCompositeDAO fileCompositeDaoMock;
	private SbmFileValidator fileValidator;
	private SBMFileStatusHandler fileSatusHandler;
	
	@Before
	public void setUp() throws IOException, ParserConfigurationException, SAXException {
		fileIngestionReader = new SbmiFileIngestionReader();
		fileIngestionReader.setEftFolder(new File(eftFolderPath));
		fileIngestionReader.setPrivateFolder(new File(privateFolderPath));
		fileIngestionReader.setZipFilesFolder(new File(zipFolderPath));
		fileIngestionReader.setProcessedFolder(processedFolder);
		
		fileCompositeDaoMock = Mockito.mock(SBMFileCompositeDAO.class);
		xmlValidator = new SbmXMLValidator();
		xmlValidator.setXsdFilePath("/xsd/SBMPolicy.xsd");
		fileValidator = new SbmFileValidator();
		fileValidator.setFileCompositeDao(fileCompositeDaoMock);
		fileValidator.setCoverageYear(2017);
		
		fileIngestionReader.setXmlValidator(xmlValidator);
		fileIngestionReader.setFileValidator(fileValidator);
		fileIngestionReader.setFileCompositeDao(fileCompositeDaoMock);
		
		fileSatusHandler = new SBMFileStatusHandler();
		fileIngestionReader.setFileSatusHandler(fileSatusHandler);
		
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
		FileUtils.copyFileToDirectory(new File("./src/test/resources/sbm/schemaErrors/SBMI_FileLevelErrors_6.xml"), eftFolder);
		
		//Mockito.when(fileCompositeDaoMock.getFileStatus(Mockito.anyString())).thenReturn(null);
		Long batchId = 2123L;
		SBMFileProcessingDTO dto = fileIngestionReader.read(batchId);
		
		Assert.assertNotNull("Reader should return dto", dto);
		
		while(dto != null) {
			dto = fileIngestionReader.read(batchId);
		}
	}
	
	@Test
	public void testReaderZip() throws UnexpectedInputException, ParseException, NonTransientResourceException, Exception {
		FileUtils.copyFileToDirectory(new File("./src/test/resources/sbm/sbmTestFiles/zipFiles/SBMI.NY1.D160810.T100000007.T"), eftFolder);
		
		Long batchId = 2123L;
		SBMFileProcessingDTO dto = fileIngestionReader.read(batchId);
		
		assertNotNull("Reader should return dto", dto);
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
	
	@Test
	public void testValidateZipName_Valid() {
		
		String fileName="SRCID.EPS.SBMI.D140530.T145543452.T.IN";
		
		Boolean ressult = (Boolean)ReflectionTestUtils.invokeMethod(fileIngestionReader, "isZipEntryNameValid", fileName);
		assertTrue("isValidFileName", ressult);
	}
	
	@Test
	public void testValidateZipName_inValid() {
		
		String fileName="SBMI.EPS.D140530.T145543452.T.IN";
		
		Boolean ressult = (Boolean)ReflectionTestUtils.invokeMethod(fileIngestionReader, "isZipEntryNameValid", fileName);
		assertFalse("isValidFileName", ressult);
	}
	
	@Test
	public void testReaderGZip() throws UnexpectedInputException, ParseException, NonTransientResourceException, Exception {
		FileUtils.copyFileToDirectory(new File("./src/test/resources/sbm/sbmTestFiles/zipFiles/WA1.EPS.SBMI.D160810.T200000007.T.IN"), eftFolder);
		
		Long batchId = 2123L;
		SBMFileProcessingDTO dto = fileIngestionReader.read(batchId);
		
		assertNotNull("Reader should return dto", dto);
	}

	
//	@Test
//	public void testGetAFileToProcessFromEFT() {
////		ReflectionTestUtils.invokeMethod(reader, "generatePaymentAdviceError", actualDTOList, is835);
//		//TODO
//	}
	
	
}
