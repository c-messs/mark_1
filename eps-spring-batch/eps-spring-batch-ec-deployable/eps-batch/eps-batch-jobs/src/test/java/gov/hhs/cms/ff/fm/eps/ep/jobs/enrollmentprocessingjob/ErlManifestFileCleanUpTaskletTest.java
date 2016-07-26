/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

/**
 * Unit test class for the FileMoveTasklet.java
 * @author girish.padmanabhan
 * 
 */
@ContextConfiguration(locations={"classpath:/test-context.xml"})
@TestExecutionListeners( { DependencyInjectionTestExecutionListener.class })
@RunWith(SpringJUnit4ClassRunner.class)
public class ErlManifestFileCleanUpTaskletTest extends TestCase {

	private static String ROOT_TEST_PATH = "./Test834files";
	private ErlManifestFileCleanUpTasklet erlManifestFileCleanUpTasklet;
	
	@Value("./Test834files/manifest")
	private File manifestDirectory;
	
	@Value("./Test834files/archive")
	private File destinationDirectory;
	
	@Value("./Test834files/extract")
	private File extractDirectory;
	
	@Value("./Test834files/manifest/Manifest-1.txt")
	private File manifestFile;
	
	@Value("./Test834files/extract/extractFile.xml")
	private File extractFile;
	
	StepContribution contribution=null;
	ChunkContext chunkContext=null;

	@Before
	public void setup() throws IOException {
		erlManifestFileCleanUpTasklet = new ErlManifestFileCleanUpTasklet();
		manifestDirectory.mkdirs();
		extractDirectory.mkdirs();
		destinationDirectory.mkdirs();
		manifestFile.createNewFile();

		erlManifestFileCleanUpTasklet.setDestinationDirectory(destinationDirectory);

		ExecutionContext ctx = new ExecutionContext();
		ctx.putString("MANIFEST_FILE", manifestFile.getAbsolutePath());
		ctx.put("ERL_EXTRACT_FILE_PATH", extractDirectory);
		JobExecution jobExecution = new JobExecution(9999L);
		jobExecution.setExecutionContext(ctx);
		StepExecution stepExecution = new StepExecution("erlManifestFileCleanUpTasklet", jobExecution);
		StepContext stepContext = new StepContext(stepExecution);
		chunkContext = new ChunkContext(stepContext);
	}

	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.ErlManifestFileCleanUpTasklet#execute()}
	 * This method will test the successful movement of the files from the 
	 * manifest directory to the destination directory, and the deletion of the extract directory 
	 * 
	 * @throws Exception
	 */
	@Test
	public void testExecute_success() throws Exception {
		
		RepeatStatus status = erlManifestFileCleanUpTasklet.execute(contribution, chunkContext);
		assertEquals("Status of tasklet execution is FINISHED", RepeatStatus.FINISHED, status);
		assertEquals("Manifest Directory list length", 0, manifestDirectory.list().length);
		assertEquals("DestinationDirectory list length", 1, destinationDirectory.list().length);
	}
	
	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.ErlManifestFileCleanUpTasklet#execute()}
	 * This method will test the successful movement of the files from the 
	 * manifest directory to the destination directory, and the deletion of the extract directory 
	 * 
	 * @throws Exception
	 */
	@Test
	public void testExecute_extractDirNotEmpty() throws Exception {
		extractFile.createNewFile();
		
		RepeatStatus status = erlManifestFileCleanUpTasklet.execute(contribution, chunkContext);
		assertEquals("Status of tasklet execution is FINISHED", RepeatStatus.FINISHED, status);
		assertEquals("Manifest Directory list length", 0, manifestDirectory.list().length);
		assertEquals("DestinationDirectory list length", 1, destinationDirectory.list().length);
		assertEquals("Extract Directory list length", 1, extractDirectory.list().length);
	}
	
	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.ErlManifestFileCleanUpTasklet#execute()}
	 * This method will test the FileNotFoundException upon movement of the files from the 
	 * manifest directory to the destination directory, and the deletion of the extract directory 
	 * 
	 * @throws Exception
	 */
	@Test(expected=java.io.FileNotFoundException.class)
	public void testExecute_sourceFileNotExists() throws Exception {
		manifestFile.delete();
		RepeatStatus status = erlManifestFileCleanUpTasklet.execute(contribution, chunkContext);
		assertNull("Expecting FileNotFoundException via annotation", status);
	}
	
	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.ErlManifestFileCleanUpTasklet#execute()}
	 * This method will test the successful movement of the files from the 
	 * manifest directory to the destination directory, and the deletion of the extract directory 
	 * 
	 * @throws Exception
	 */
	@Test
	public void testExecute_nullManifestAndInput() throws Exception {
		ExecutionContext ctx = new ExecutionContext();
		JobExecution jobExecution = new JobExecution(9999L);
		jobExecution.setExecutionContext(ctx);
		StepExecution stepExecution = new StepExecution("erlManifestFileCleanUpTasklet", jobExecution);
		StepContext stepContext = new StepContext(stepExecution);
		ChunkContext chunkContext1 = new ChunkContext(stepContext);
		
		RepeatStatus status = erlManifestFileCleanUpTasklet.execute(contribution, chunkContext1);
		
		assertEquals("Status of tasklet execution is FINISHED", RepeatStatus.FINISHED, status);
		assertEquals("SourceDirectory list length", 1, manifestDirectory.list().length);
		assertEquals("DdestinationDirectory list length", 0, destinationDirectory.list().length);
	}
	
	@After
	public void tearDown() {
		FileUtils.deleteQuietly(new File(ROOT_TEST_PATH));
	}
	
}
