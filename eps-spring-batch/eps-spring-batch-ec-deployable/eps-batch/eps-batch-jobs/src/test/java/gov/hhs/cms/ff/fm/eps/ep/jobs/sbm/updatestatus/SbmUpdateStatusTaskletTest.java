/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm.updatestatus;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.io.File;
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

import com.accenture.foundation.common.exception.ApplicationException;

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
		
		mockSbmUpdateStatusProcessor= createMock(SbmUpdateStatusProcessor.class);
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
	
	@Test(expected=ApplicationException.class)
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
		
		RepeatStatus status = sbmUpdateStatusTasklet.execute(null, chunkContext);
		
		assertNotNull("Exception expected in annotation", status);
	}
	
	private JobParameters getJobParams(String processingType) {
        final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
        return new JobParameters(params);
	}

}
