/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm.updatestatus;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.test.util.ReflectionTestUtils;

import com.accenture.foundation.common.exception.ApplicationException;

import gov.hhs.cms.ff.fm.eps.ep.jobs.util.SBMTestDataDBUtil;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants;
import junit.framework.TestCase;

/**
 * Test class for SbmUpdateStatusTasklet
 * 
 * @author girish.padmanabhan
 * 
 */
@RunWith(JUnit4.class)
public class SbmUpdateStatusTaskletTest extends TestCase {

	private static final String eftFolderPath = "./src/test/resources/sbm/readerTest/eftFolder";
	private static final String privateFolderPath = "./src/test/resources/sbm/readerTest/privateFolder";
	private static final String processedFolderPath = "./src/test/resources/sbm/readerTest/processedFilesFolder";

	private File eftFolder = new File(eftFolderPath);
	private File privateFolder = new File(privateFolderPath);
	private File processedFolder = new File(processedFolderPath);

	private SbmUpdateStatusTasklet sbmUpdateStatusTasklet;
	private SbmUpdateStatusProcessor mockSbmUpdateStatusProcessor;

	@Before
	public void setup() throws IOException {
		sbmUpdateStatusTasklet = new SbmUpdateStatusTasklet();

		mockSbmUpdateStatusProcessor= EasyMock.createMock(SbmUpdateStatusProcessor.class);
		sbmUpdateStatusTasklet.setUpdateStatusProcessor(mockSbmUpdateStatusProcessor);

		sbmUpdateStatusTasklet.setEftFolder(eftFolder);
		sbmUpdateStatusTasklet.setPrivateFolder(privateFolder);
		sbmUpdateStatusTasklet.setProcessedFolder(processedFolder);	

		eftFolder.mkdirs();		
		privateFolder.mkdirs();
		processedFolder.mkdirs();

		FileUtils.cleanDirectory(eftFolder);
		FileUtils.cleanDirectory(privateFolder);
		FileUtils.cleanDirectory(processedFolder);
	}

	@After
	public void tearDown() throws IOException {
		FileUtils.cleanDirectory(eftFolder);
		FileUtils.cleanDirectory(privateFolder);
		FileUtils.cleanDirectory(processedFolder);
	}

	@Test
	public void testExecute_NoFiles() throws Exception {

		expect(mockSbmUpdateStatusProcessor.isSBMIJobRunning()).andReturn(false);

		mockSbmUpdateStatusProcessor.processUpdateStatus(EasyMock.anyObject(File.class), EasyMock.anyLong());
		EasyMock.expectLastCall().anyTimes();

		replay(mockSbmUpdateStatusProcessor);


		JobExecution jobEx = new JobExecution(9999L, getJobParams("sbmUpdateStatus"));
		jobEx.setJobInstance(new JobInstance(9999L, "sbmUpdateStatus"));

		StepExecution stepExecution = new StepExecution("sbmUpdateStatus", jobEx);
		StepContext stepContext = new StepContext(stepExecution);
		ChunkContext chunkContext = new ChunkContext(stepContext);

		RepeatStatus status = sbmUpdateStatusTasklet.execute(null, chunkContext);

		assertNotNull("RepeatStatus not null", status);
		assertEquals("RepeatStatus is FINISHED", RepeatStatus.FINISHED, status);
	}

	@Test
	public void testExecute_EftFiles() throws Exception {

		expect(mockSbmUpdateStatusProcessor.isSBMIJobRunning()).andReturn(false);

		mockSbmUpdateStatusProcessor.processUpdateStatus(EasyMock.anyObject(File.class), EasyMock.anyLong());
		EasyMock.expectLastCall().anyTimes();

		replay(mockSbmUpdateStatusProcessor);

		FileUtils.copyFileToDirectory(new File("./src/test/resources/sbm/schemaErrors/SBMI_noSchemaErrors.xml"), eftFolder);

		JobExecution jobEx = new JobExecution(9999L, getJobParams("sbmUpdateStatus"));
		jobEx.setJobInstance(new JobInstance(9999L, "sbmUpdateStatus"));

		StepExecution stepExecution = new StepExecution("sbmUpdateStatus", jobEx);
		StepContext stepContext = new StepContext(stepExecution);
		ChunkContext chunkContext = new ChunkContext(stepContext);

		RepeatStatus status = sbmUpdateStatusTasklet.execute(null, chunkContext);

		assertNull("RepeatStatus not null", status);
	}

	@Test
	public void testExecute_Files() throws Exception {

		expect(mockSbmUpdateStatusProcessor.isSBMIJobRunning()).andReturn(false);

		mockSbmUpdateStatusProcessor.processUpdateStatus(EasyMock.anyObject(File.class), EasyMock.anyLong());
		EasyMock.expectLastCall().anyTimes();

		replay(mockSbmUpdateStatusProcessor);

		FileUtils.copyFileToDirectory(new File("./src/test/resources/sbm/schemaErrors/SBMI_noSchemaErrors.xml"), privateFolder);

		JobExecution jobEx = new JobExecution(9999L, getJobParams("sbmUpdateStatus"));
		jobEx.setJobInstance(new JobInstance(9999L, "sbmUpdateStatus"));

		StepExecution stepExecution = new StepExecution("sbmUpdateStatus", jobEx);
		StepContext stepContext = new StepContext(stepExecution);
		ChunkContext chunkContext = new ChunkContext(stepContext);

		RepeatStatus status = sbmUpdateStatusTasklet.execute(null, chunkContext);

		assertNull("RepeatStatus not null", status);
	}

	@Test//(expected=ApplicationException.class)
	public void testExecute_SbmiJobInProcess() throws Exception {

		expect(mockSbmUpdateStatusProcessor.isSBMIJobRunning()).andReturn(true);

		mockSbmUpdateStatusProcessor.processUpdateStatus(EasyMock.anyObject(File.class), EasyMock.anyLong());
		EasyMock.expectLastCall().anyTimes();

		replay(mockSbmUpdateStatusProcessor);


		JobExecution jobEx = new JobExecution(9999L, getJobParams("sbmUpdateStatus"));
		jobEx.setJobInstance(new JobInstance(9999L, "sbmUpdateStatus"));

		StepExecution stepExecution = new StepExecution("sbmUpdateStatus", jobEx);
		StepContext stepContext = new StepContext(stepExecution);
		ChunkContext chunkContext = new ChunkContext(stepContext);

		try {
			sbmUpdateStatusTasklet.execute(null, chunkContext);
		} catch(ApplicationException appEx) {
			assertTrue("ApplicationException thrown", true);
		}

		assertNotNull("Exception expected in annotation", jobEx);
	}

	@Test
	public void test_getAFileToProcess_T() throws InterruptedException, IOException {

		File expectedFile = null;
		sbmUpdateStatusTasklet.setEnvironmentCd(SBMConstants.FILE_ENV_CD_TEST);
		// from table SERVERENVIRONMENTTYPE
		String[] envCds = {SBMConstants.FILE_ENV_CD_PROD_R, SBMConstants.FILE_ENV_CD_PROD, SBMConstants.FILE_ENV_CD_TEST};
		String stateCd = SBMTestDataDBUtil.getRandomSbmState();
		String sourceId = SBMTestDataDBUtil.getRandomNumberAsString(3) + stateCd;

		for (int i = 0; i < envCds.length; ++i) {

			String fileName = SBMTestDataDBUtil.makeFileName(sourceId, envCds[i]);

			String content = "Any ol' content: " + i;

			// Load up various files into EFT folder.
			File file = new File(eftFolder + File.separator + fileName);
			BufferedWriter output = new BufferedWriter(new FileWriter(file));
			output.write(content);
			output.close();

			// In this case envCd='T' will get all files, so the first one is grabbed.
			if (i == 0) {
				expectedFile = file;
			}
			// Delay a little to get a different fileName since it is timestamp based.
			Thread.sleep(5);
		}
		// Confirm we only get the T (test) file and not the PROD or PROD-R file.
		File actualFile = (File) ReflectionTestUtils.invokeMethod(sbmUpdateStatusTasklet, "getAFileToProcess");
		assertEquals("file name with environmentCode 'T'", expectedFile.getName(), actualFile.getName());
	}

	@Test
	public void test_getAFileToProcess_P() throws InterruptedException, IOException {

		File expectedFile = null;
		sbmUpdateStatusTasklet.setEnvironmentCd(SBMConstants.FILE_ENV_CD_PROD);
		// from table SERVERENVIRONMENTTYPE
		String[] envCds = { "1A", "1B", "0", "T0", "T1", "T2", SBMConstants.FILE_ENV_CD_PROD_R, SBMConstants.FILE_ENV_CD_PROD};
		String stateCd = SBMTestDataDBUtil.getRandomSbmState();
		String sourceId = SBMTestDataDBUtil.getRandomNumberAsString(3) + stateCd;

		for (int i = 0; i < envCds.length; ++i) {

			String fileName = SBMTestDataDBUtil.makeFileName(sourceId, envCds[i]);

			String content = "Any ol' content: " + i;

			// Load up various files into EFT folder.
			File file = new File(eftFolder + File.separator + fileName);
			BufferedWriter output = new BufferedWriter(new FileWriter(file));
			output.write(content);
			output.close();

			if (i == (envCds.length - 1)) {
				expectedFile = file;
			}
			// Delay a little to get a different fileName since it is timestamp based.
			Thread.sleep(5);
		}
		// Confirm we only get the T (test) file and not the PROD or PROD-R file.
		File actualFile = (File) ReflectionTestUtils.invokeMethod(sbmUpdateStatusTasklet, "getAFileToProcess");
		assertEquals("file name with environmentCode 'P'", expectedFile.getName(), actualFile.getName());
	}


	/**
	 * Confirm NO production P files are pulled from folder when there all files from
	 * many different environments including some bogus ones (PP, PROD)
	 * @throws InterruptedException
	 * @throws IOException
	 */
	@Test
	public void test_getAFileToProcess_P_NoFiles() throws InterruptedException, IOException {

		File expectedFile = null;
		sbmUpdateStatusTasklet.setEnvironmentCd(SBMConstants.FILE_ENV_CD_PROD);
		// from table SERVERENVIRONMENTTYPE
		String[] envCds = { "1A", "1B", "0", "T0", "T1", "T2", SBMConstants.FILE_ENV_CD_PROD_R, "T", "PP", "PROD"};
		String stateCd = SBMTestDataDBUtil.getRandomSbmState();
		String sourceId = SBMTestDataDBUtil.getRandomNumberAsString(3) + stateCd;

		for (int i = 0; i < envCds.length; ++i) {

			String fileName = SBMTestDataDBUtil.makeFileName(sourceId, envCds[i]);

			String content = "Any ol' content: " + i;

			// Load up various files into EFT folder.
			File file = new File(eftFolder + File.separator + fileName);
			BufferedWriter output = new BufferedWriter(new FileWriter(file));
			output.write(content);
			output.close();

			// Delay a little to get a different fileName since it is timestamp based.
			Thread.sleep(5);
		}
		// Confirm we only get the T (test) file and not the PROD or PROD-R file.
		File actualFile = (File) ReflectionTestUtils.invokeMethod(sbmUpdateStatusTasklet, "getAFileToProcess");
		assertEquals("file with environmentCode 'P'", expectedFile, actualFile);
	}

	/**
	 * Confirm only the PROD-R file is pulled from folder when there all files from
	 * many different environments including some bogus ones (PP, PROD)
	 * @throws InterruptedException
	 * @throws IOException
	 */
	@Test
	public void test_getAFileToProcess_R() throws InterruptedException, IOException {

		File expectedFile = null;
		sbmUpdateStatusTasklet.setEnvironmentCd(SBMConstants.FILE_ENV_CD_PROD_R);
		// from table SERVERENVIRONMENTTYPE
		String[] envCds = { "1A", "1B", "0", "T0", "T1", "T2", "PROD-R", "PR", "T", SBMConstants.FILE_ENV_CD_PROD_R};
		String stateCd = SBMTestDataDBUtil.getRandomSbmState();
		String sourceId = SBMTestDataDBUtil.getRandomNumberAsString(3) + stateCd;

		for (int i = 0; i < envCds.length; ++i) {

			String fileName = SBMTestDataDBUtil.makeFileName(sourceId, envCds[i]);

			String content = "Any ol' content: " + i;

			// Load up various files into EFT folder.
			File file = new File(eftFolder + File.separator + fileName);
			BufferedWriter output = new BufferedWriter(new FileWriter(file));
			output.write(content);
			output.close();

			if (i == (envCds.length - 1)) {
				expectedFile = file;
			}
			// Delay a little to get a different fileName since it is timestamp based.
			Thread.sleep(5);
		}
		// Confirm we only get the T (test) file and not the PROD or PROD-R file.
		File actualFile = (File) ReflectionTestUtils.invokeMethod(sbmUpdateStatusTasklet, "getAFileToProcess");
		assertEquals("file with environmentCode 'R'", expectedFile.getName(), actualFile.getName());
	}



	private JobParameters getJobParams(String processingType) {
		final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
		return new JobParameters(params);
	}

}
