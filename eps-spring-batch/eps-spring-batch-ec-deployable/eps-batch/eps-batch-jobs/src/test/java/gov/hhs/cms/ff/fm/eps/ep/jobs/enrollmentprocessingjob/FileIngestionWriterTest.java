/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob;

import gov.hhs.cms.ff.fm.eps.ep.EPSFileIndex;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

/**
 * Test class for FileIngestionWriter
 * 
 * @author girish.padmanabhan
 * 
 */
@ContextConfiguration(locations={"classpath:/test-context.xml"})
@TestExecutionListeners( { DependencyInjectionTestExecutionListener.class })
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@RunWith(SpringJUnit4ClassRunner.class)
public class FileIngestionWriterTest extends TestCase {

	private FileIngestionWriter fileIngestionWriter;
	
	@Value("./Test834files")
	private File rootDirectory;
	
	@Value("./Test834files/BenefitEnrollmentRequest.xml")
	private File berFile;
	
	@Value("./Test834files/invalid")
	private File invalidDirectory;
	
	@Before
	public void setup() throws IOException {
		fileIngestionWriter = new FileIngestionWriter();
		FileUtils.deleteQuietly(rootDirectory);
		invalidDirectory.mkdirs();
		berFile.createNewFile();

		fileIngestionWriter.setInvalidFilesDirectory(invalidDirectory);
	}

	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.FileIngestionWriter#write(gov.hhs.cms.ff.fm.eps.ep.EPSFileIndex)}
	 * This method tests the moving an invalid schema file to the invalid schema files directory 
	 * 
	 * @throws Exception
	 */
	@Test
	public void testWrite_invalidSchemaFile() throws Exception {
		fileIngestionWriter.write(mockInvalidSchemaFile());
		File [] dirFiles = invalidDirectory.listFiles();
		assertEquals("The file name in the Test834file directory is BenefitEnrollmentRequest.xml", 
				berFile.getName(), Arrays.asList(dirFiles).get(0).getName());
	}

	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.FileIngestionWriter#write(gov.hhs.cms.ff.fm.eps.ep.EPSFileIndex)}
	 * This method tests the result of moving an invalid schema file to the invalid schema files directory 
	 * when the directory doesn't exist, expecting an IOException
	 * 
	 * @throws Exception
	 */	
	@Test(expected=IOException.class)
	public void testWrite_ioexception() throws Exception {
		assertNotNull("invalid directory not null", invalidDirectory);
		FileUtils.deleteQuietly(invalidDirectory);
		fileIngestionWriter.write(mockInvalidSchemaFile());
	}
	
	/*
	 * private method to create input object to be passed to the test method.
	 * This method sets the file's isValid flag to false
	 */
	private List<EPSFileIndex> mockInvalidSchemaFile() throws IOException {
		List<EPSFileIndex> files = new ArrayList<EPSFileIndex>();
		
		EPSFileIndex fileIndex  = new EPSFileIndex();
		fileIndex.setValid(false);
		fileIndex.setFileName(berFile.getCanonicalPath());
		files.add(fileIndex);
		return files;
	}	
	
	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.FileIngestionWriter#write(gov.hhs.cms.ff.fm.eps.ep.EPSFileIndex)}
	 * This method tests the condition when the file schema is valid 
	 *  
	 * @throws Exception
	 */	
	@Test
	public void testWrite_validSchemaFile() throws Exception {
		fileIngestionWriter.write(mockValidSchemaFile());
		File [] dirFiles = invalidDirectory.listFiles();
		assertTrue("The files array is not empty", Arrays.asList(dirFiles).isEmpty());
	}	
	
	/*
	 * private method to create input object to be passed to the test method.
	 * This method sets the file's isValid flag to true.
	 */
	private List<EPSFileIndex> mockValidSchemaFile() throws IOException {
		List<EPSFileIndex> files = new ArrayList<EPSFileIndex>();
		
		EPSFileIndex fileIndex  = new EPSFileIndex();
		fileIndex.setValid(true);
		fileIndex.setFileName(berFile.getCanonicalPath());
		files.add(fileIndex);
		return files;
	}
	
	@After
	public void tearDown() {
		FileUtils.deleteQuietly(rootDirectory);
	}
}
