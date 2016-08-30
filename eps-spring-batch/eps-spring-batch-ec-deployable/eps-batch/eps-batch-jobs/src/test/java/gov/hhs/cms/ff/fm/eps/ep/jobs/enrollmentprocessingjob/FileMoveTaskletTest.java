/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.util.ReflectionTestUtils;

import junit.framework.TestCase;

/**
 * Unit test class for the FileMoveTasklet.java
 * @author girish.padmanabhan
 * 
 */
@ContextConfiguration(locations={"classpath:/test-context.xml"})
@TestExecutionListeners( { DependencyInjectionTestExecutionListener.class })
@RunWith(SpringJUnit4ClassRunner.class)
public class FileMoveTaskletTest extends TestCase {

	private static String ROOT_TEST_PATH = "./Test834files";
	private FileMoveTasklet fileMoveTasklet;
	
	@Value("./Test834files/input")
	private File sourceDirectory;
	
	@Value("./Test834files/private")
	private File destinationDirectory;
	
	@Value("./Test834files/input/BenefitEnrollmentRequestSame.xml")
	private File berFileOne;
	
	@Value("./Test834files/private/BenefitEnrollmentRequestSame.xml")
	private File berFileTwo;
	
	@Value("./Test834files/input/BenefitEnrollmentRequestDifferent.xml")
	private File berFileThree;
	
	StepContribution contribution=null;
	ChunkContext chunkContext=null;

	@Before
	public void setup() throws IOException {
		fileMoveTasklet = new FileMoveTasklet();
		sourceDirectory.mkdirs();
		destinationDirectory.mkdirs();
		berFileOne.createNewFile();
		berFileTwo.createNewFile();
		berFileThree.createNewFile();
		
		fileMoveTasklet.setSourceDirectory(sourceDirectory);
		fileMoveTasklet.setDestinationDirectory(destinationDirectory);
	}

	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.FileMoveTasklet#execute()}
	 * This method will test the successful movement of the files from the 
	 * source directory to the destination directory 
	 * 
	 * @throws Exception
	 */
	@Test
	public void testExecute_success() throws Exception {
		RepeatStatus status = fileMoveTasklet.execute(contribution, chunkContext);
		assertEquals("Status of tasklet execution is FINISHED", RepeatStatus.FINISHED, status);
		assertEquals("SourceDirectory list length", 0, sourceDirectory.list().length);
		assertEquals("DdestinationDirectory list length", 2, destinationDirectory.list().length);
	}
	
	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.FileMoveTasklet#execute()}
	 * This method will test that EnvironmentException is thrown when sourcedirectory is not a directory
	 * 
	 * @throws Exception
	 */
	@Test(expected=com.accenture.foundation.common.exception.EnvironmentException.class)
	public void testExecute_sourceDirInvalidType() throws Exception {
		ReflectionTestUtils.setField(fileMoveTasklet, "sourceDirectory", 
				new ClassPathResource("testfiles/1.IS834.D140530.T145543452.T.IN").getFile());
		fileMoveTasklet.execute(contribution, chunkContext);
		
		assertNotNull("EnvironmentException expected via annotation", fileMoveTasklet);
	}
	
	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.FileMoveTasklet#execute()}
	 * This method will test the movement of the files from the 
	 * source directory to the destination directory when the source directory does
	 * not exist, resulting in IOException
	 * 
	 * @throws Exception
	 */
	@Test(expected=IOException.class)
	public void testExecute_ioexception() throws Exception {
		ReflectionTestUtils.setField(fileMoveTasklet, "destinationDirectory", new File(""));
		fileMoveTasklet.execute(contribution, chunkContext);
		assertNull("ChunkContext", chunkContext);
	}
	
	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.FileMoveTasklet#execute()}
	 * This method will test the movement of the files from the 
	 * source directory to the destination directory when the source directory does
	 * not exist, resulting in FileNotFoundException
	 * 
	 * @throws Exception
	 */
	@Test(expected=FileNotFoundException.class)
	public void testExecute_fileNotFoundException() throws Exception {
		sourceDirectory.delete();
		ReflectionTestUtils.setField(fileMoveTasklet, "destinationDirectory", new File(""));
		fileMoveTasklet.execute(contribution, chunkContext);
		assertNull("ChunkContext", chunkContext);
	}

	@After
	public void tearDown() {
		FileUtils.deleteQuietly(new File(ROOT_TEST_PATH));
	}
	
}
