package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.xml.sax.SAXException;

import gov.hhs.cms.ff.fm.eps.dispatcher.EFTDispatchDriver;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMErrorWarningCode;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMErrorDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileProcessingDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SBMFileCompositeDAO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SbmResponseCompositeDao;

public class SbmiFileIngestionTaskletTest {

	private SbmiFileIngestionTasklet tasklet;
	
	private static final String eftFolderPath = "./src/test/resources/sbm/readerTest/eftFolder";
	private static final String privateFolderPath = "./src/test/resources/sbm/readerTest/privateFolder";	
	private static final String zipFilesFolderPath = "./src/test/resources/sbm/readerTest/zipFiles";	
	private static final String processedFolderPath = "./src/test/resources/sbm/readerTest/processedFolder";
	private static final String invalidFolderPath = "./src/test/resources/sbm/readerTest/invalidFolder";
	
	private File eftFolder = new File(eftFolderPath);
	private File privateFolder = new File(privateFolderPath);	
	private File zipFilesFolder = new File(zipFilesFolderPath);	
	private File processedFolder = new File(processedFolderPath);
	private File invalidFolder = new File(invalidFolderPath);	
	
	
	private SbmXMLValidator xmlValidator;
	private SBMFileCompositeDAO fileCompositeDaoMock;
	private EFTDispatchDriver mockEFTDispatchDriver;
	private SbmResponseCompositeDao mockSbmResponseDao;
	private SbmFileValidator fileValidator;
	
	@Before
	public void setUp() throws ParserConfigurationException, SAXException, IOException {
		
		tasklet = new SbmiFileIngestionTasklet();
		SbmiFileIngestionReader fileIngestionReader;
		
		fileIngestionReader = new SbmiFileIngestionReader();
		fileIngestionReader.setEftFolder(new File(eftFolderPath));
		fileIngestionReader.setPrivateFolder(new File(privateFolderPath));
		fileIngestionReader.setZipFilesFolder(zipFilesFolder);
		
		fileCompositeDaoMock = Mockito.mock(SBMFileCompositeDAO.class);
		xmlValidator = new SbmXMLValidator();
		xmlValidator.setXsdFilePath("/xsd/SBMPolicy.xsd");
		fileValidator = new SbmFileValidator();
		fileValidator.setFileCompositeDao(fileCompositeDaoMock);
		fileValidator.setCoverageYear(2017);
		
		fileIngestionReader.setXmlValidator(xmlValidator);
		fileIngestionReader.setFileValidator(fileValidator);
		fileIngestionReader.setFileCompositeDao(fileCompositeDaoMock);
		
		eftFolder.mkdirs();		
		privateFolder.mkdirs();
		zipFilesFolder.mkdir();
		processedFolder.mkdir();
		invalidFolder.mkdir();
		
		FileUtils.cleanDirectory(eftFolder);
		FileUtils.cleanDirectory(privateFolder);
		FileUtils.cleanDirectory(zipFilesFolder);
		FileUtils.cleanDirectory(processedFolder);
		FileUtils.cleanDirectory(invalidFolder);
		
		
		//writer
		SbmiFileIngestionWriter writer = new SbmiFileIngestionWriter();
		writer.setFileCompositeDao(fileCompositeDaoMock);
		writer.setProcessedFolder(processedFolder);
		writer.setInvalidFolder(invalidFolder);
		
		SBMResponseGenerator responseGenerator = new SBMResponseGenerator();
		
		mockEFTDispatchDriver= createMock(EFTDispatchDriver.class);
		responseGenerator.setEftDispatcher(mockEFTDispatchDriver);
		
		mockSbmResponseDao = createMock(SbmResponseCompositeDao.class);
		responseGenerator.setSbmResponseDao(mockSbmResponseDao);
		
		writer.setResponseGenerator(responseGenerator);
		
		tasklet.setFileIngestionReader(fileIngestionReader);
		tasklet.setFileIngestionWriter(writer);
		
		SBMEvaluatePendingFiles sbmEvaluatePendingFiles = new SBMEvaluatePendingFiles();
		sbmEvaluatePendingFiles.setFileCompositeDao(fileCompositeDaoMock);
		tasklet.setSbmEvaluatePendingFiles(sbmEvaluatePendingFiles);
	}
	
	@After
	public void tearDown() throws IOException {
		FileUtils.cleanDirectory(eftFolder);
		FileUtils.cleanDirectory(privateFolder);
		FileUtils.cleanDirectory(zipFilesFolder);
		FileUtils.cleanDirectory(processedFolder);
		FileUtils.cleanDirectory(invalidFolder);
	}
	
	@Test
	public void testValidFile() throws Exception {
		
		expect(mockEFTDispatchDriver.saveDispatchContent((byte[])EasyMock.anyObject(), 
				EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyString(), 
				EasyMock.anyString(), EasyMock.anyInt(), EasyMock.anyString(), EasyMock.anyString())).andReturn(1L);
		replay(mockEFTDispatchDriver);
		
//		mockSbmResponseDao.createSBMResponseRecord(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong(), Mockito.any());
//		expectLastCall();
//		replay(mockSbmResponseDao);
		
		FileUtils.copyFileToDirectory(new File("./src/test/resources/sbm/schemaErrors/SBMI_noSchemaErrors.xml"), eftFolder);
		
		ChunkContext chkContext = new ChunkContext(new StepContext(new StepExecution("fileIngestionStep", new JobExecution(21001L))));		
		RepeatStatus status = tasklet.execute(Mockito.any(), chkContext);
		
		Assert.assertNotNull("Tasklet should not return null", status);		
		
	}
	
	@Test
	public void testInvalidFile() throws Exception {
		
		expect(mockEFTDispatchDriver.saveDispatchContent((byte[])EasyMock.anyObject(), 
				EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyString(), 
				EasyMock.anyString(), EasyMock.anyInt(), EasyMock.anyString(), EasyMock.anyString())).andReturn(1L);
		replay(mockEFTDispatchDriver);
		
//		mockSbmResponseDao.createSBMResponseRecord(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong(), Mockito.any());
//		expectLastCall();
//		replay(mockSbmResponseDao);
		
		FileUtils.copyFileToDirectory(new File("./src/test/resources/sbm/sbmTestFiles/CA1.EPS.SBMI.D160101.T090101002.T"), eftFolder);
		
		ChunkContext chkContext = new ChunkContext(new StepContext(new StepExecution("fileIngestionStep", new JobExecution(21001L))));		
		RepeatStatus status = tasklet.execute(Mockito.any(), chkContext);
		
		Assert.assertNotNull("Tasklet should not return null", status);		
		
	}
	
	@Test
	public void testFileExistsInProcessedFolder() throws Exception {		
		
		expect(mockEFTDispatchDriver.saveDispatchContent((byte[])EasyMock.anyObject(), 
				EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyString(), 
				EasyMock.anyString(), EasyMock.anyInt(), EasyMock.anyString(), EasyMock.anyString())).andReturn(1L);
		replay(mockEFTDispatchDriver);
		
//		mockSbmResponseDao.createSBMResponseRecord(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong(), Mockito.any());
//		expectLastCall();
//		replay(mockSbmResponseDao);
		
		FileUtils.copyFileToDirectory(new File("./src/test/resources/sbm/schemaErrors/SBMI_noSchemaErrors.xml"), eftFolder);
		
		//copy same file to processed folder
		FileUtils.copyFileToDirectory(new File("./src/test/resources/sbm/schemaErrors/SBMI_noSchemaErrors.xml"), processedFolder);
		
		ChunkContext chkContext = new ChunkContext(new StepContext(new StepExecution("fileIngestionStep", new JobExecution(21001L))));		
		RepeatStatus status = tasklet.execute(Mockito.any(), chkContext);
		
		Assert.assertNotNull("Tasklet should not return null", status);		
	}
	
	@Test
	public void testZipFile() throws Exception {
		
		expect(mockEFTDispatchDriver.saveDispatchContent((byte[])EasyMock.anyObject(), 
				EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyString(), 
				EasyMock.anyString(), EasyMock.anyInt(), EasyMock.anyString(), EasyMock.anyString())).andReturn(1L);
		replay(mockEFTDispatchDriver);
				
		FileUtils.copyFileToDirectory(new File("./src/test/resources/sbm/sbmTestFiles/zipFiles/SBMI.NY1.D160810.T100000007.T"), eftFolder);
		
		ChunkContext chkContext = new ChunkContext(new StepContext(new StepExecution("fileIngestionStep", new JobExecution(21001L))));		
		RepeatStatus status = tasklet.execute(Mockito.any(), chkContext);
		
		Assert.assertNotNull("Tasklet should not return null", status);		
		
	}
	
	@Test
	public void testZipFile_filename_Sourceid_Error() throws Exception {
		
		expect(mockEFTDispatchDriver.saveDispatchContent((byte[])EasyMock.anyObject(), 
				EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyString(), 
				EasyMock.anyString(), EasyMock.anyInt(), EasyMock.anyString(), EasyMock.anyString())).andReturn(1L);
		replay(mockEFTDispatchDriver);
				 
		FileUtils.copyFileToDirectory(new File("./src/test/resources/sbm/sbmTestFiles/zipFiles/SBMI.NY1.D160810.T100000007_2.T"), eftFolder);
		
		List<SBMErrorDTO> errorList = new ArrayList<>();
		
		Mockito.when(fileCompositeDaoMock.saveFileInfoAndErrors(Mockito.any())).thenAnswer(new Answer<Long>() {

			@Override
			public Long answer(InvocationOnMock invocation) throws Throwable {
				SBMFileProcessingDTO dto = (SBMFileProcessingDTO)invocation.getArguments()[0];
				errorList.addAll(dto.getErrorList()); //retrieve errors
				return 1L;
			}
		});
		
		ChunkContext chkContext = new ChunkContext(new StepContext(new StepExecution("fileIngestionStep", new JobExecution(21001L))));		
		tasklet.execute(Mockito.any(), chkContext);
		
		Assert.assertTrue("Errors should exists", CollectionUtils.isNotEmpty(errorList));	
		Assert.assertTrue("Error ER-063 should exist", isErrorExists(errorList, null, SBMErrorWarningCode.ER_063.getCode()));
		
	}
	
	@Test
	public void testZipFile_fileName_Error() throws Exception {
		
		expect(mockEFTDispatchDriver.saveDispatchContent((byte[])EasyMock.anyObject(), 
				EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyString(), 
				EasyMock.anyString(), EasyMock.anyInt(), EasyMock.anyString(), EasyMock.anyString())).andReturn(1L);
		replay(mockEFTDispatchDriver);
				 
		FileUtils.copyFileToDirectory(new File("./src/test/resources/sbm/sbmTestFiles/zipFiles/SBMI.NY1.D160810.T100000007_3.T"), eftFolder);
		
		List<SBMErrorDTO> errorList = new ArrayList<>();
		
		Mockito.when(fileCompositeDaoMock.saveFileInfoAndErrors(Mockito.any())).thenAnswer(new Answer<Long>() {

			@Override
			public Long answer(InvocationOnMock invocation) throws Throwable {
				SBMFileProcessingDTO dto = (SBMFileProcessingDTO)invocation.getArguments()[0];
				errorList.addAll(dto.getErrorList()); //retrieve errors
				return 1L;
			}
		});
		
		ChunkContext chkContext = new ChunkContext(new StepContext(new StepExecution("fileIngestionStep", new JobExecution(21001L))));		
		tasklet.execute(Mockito.any(), chkContext);
		
		Assert.assertTrue("Errors should exists", CollectionUtils.isNotEmpty(errorList));	
		Assert.assertTrue("Error ER-011 should exist", isErrorExists(errorList, null, SBMErrorWarningCode.ER_063.getCode()));
		
	}
	
	@Test
	public void test_NoFiles() throws Exception {
		
		expect(mockEFTDispatchDriver.saveDispatchContent((byte[])EasyMock.anyObject(), 
				EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyString(), 
				EasyMock.anyString(), EasyMock.anyInt(), EasyMock.anyString(), EasyMock.anyString())).andReturn(1L);
		replay(mockEFTDispatchDriver);
				 
		List<SBMErrorDTO> errorList = new ArrayList<>();
		
		Mockito.when(fileCompositeDaoMock.saveFileInfoAndErrors(Mockito.any())).thenAnswer(new Answer<Long>() {

			@Override
			public Long answer(InvocationOnMock invocation) throws Throwable {
				SBMFileProcessingDTO dto = (SBMFileProcessingDTO)invocation.getArguments()[0];
				errorList.addAll(dto.getErrorList()); //retrieve errors
				return 1L;
			}
		});
		
		ChunkContext chkContext = new ChunkContext(new StepContext(new StepExecution("fileIngestionStep", new JobExecution(21001L))));		
		tasklet.execute(Mockito.any(), chkContext);
		
		Assert.assertTrue("Errors should not exists", CollectionUtils.isEmpty(errorList));	
	}
	
	public static boolean isErrorExists(List<SBMErrorDTO> errorList, String elementInError, String errorCode) {
		for(SBMErrorDTO error: errorList) {
			if(errorCode.equalsIgnoreCase(error.getSbmErrorWarningTypeCd())) {
				return true;
			}
		}
		
		return false;
	}
	
}
