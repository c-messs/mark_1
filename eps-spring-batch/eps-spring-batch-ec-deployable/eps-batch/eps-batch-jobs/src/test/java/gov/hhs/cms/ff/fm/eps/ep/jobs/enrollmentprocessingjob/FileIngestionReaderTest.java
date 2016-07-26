package gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob;

import gov.hhs.cms.ff.fm.eps.ep.EPSFileIndex;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Unit test class for the FileIngestionReader.java
 * @author christopher.vaka
 * 
 */
@RunWith(JUnit4.class)
public class FileIngestionReaderTest extends TestCase {
	
	private FileIngestionReader fileIngestionReader; 
	
	private File inputDir;
	private List<File> inputFiles = new ArrayList<File>();
	private String xsd; 
	FileFilter xmlFilter;
	
	@Before
	public void setup() {
		fileIngestionReader = new FileIngestionReader();
		try {
			xsd = "/erl/BatchUpdateUtilitySchema.xsd";
			inputDir = new ClassPathResource("testfiles/").getFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		fileIngestionReader.setPrivateDirectory(inputDir);
		fileIngestionReader.setXsd(xsd);
		ReflectionTestUtils.setField(fileIngestionReader, "inputFiles", inputFiles);
		
		xmlFilter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile();
            }
		 };		
	}
	
	/**
	 * Test method for {@linkgov.cms.eps.sample.jobs.eps.EpsXsdValidator#read()}
	 * This test method tests the successful schema validation of the input xml
	 * against the specific xsd.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRead_success() throws Exception {
		inputFiles.add(new ClassPathResource("testfiles/1234567890.SCHMA.D140815.T090909000.T.IN").getFile());
		EPSFileIndex epsFileIndex = fileIngestionReader.read();
		assertTrue("input file xsd validation flag is true", epsFileIndex.isValid());
	}

	/**
	 * Test method for {@linkgov.cms.eps.sample.jobs.eps.EpsXsdValidator#read()}
	 * This test method tests read() method when input files is empty.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRead_noInputFiles() throws Exception {
		EPSFileIndex epsFileIndex = fileIngestionReader.read();
		assertNull("epsFileIndex is null", epsFileIndex);
	}
	
	/**
	 * Test method for {@linkgov.cms.eps.sample.jobs.eps.EpsXsdValidatort#read()}
	 * This test method tests the malformed xml validation of the input xml
	 * against the specific xsd.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRead_failure() throws Exception {
		inputFiles.add(new ClassPathResource("testfiles/1234567890.INVNONXM.D140815.T090909000.T.IN").getFile());
		EPSFileIndex epsFileIndex = fileIngestionReader.read();
		assertFalse("input file xsd validation flag is false", epsFileIndex.isValid());
	}
	
	/**
	 * Test method for {@linkgov.cms.eps.sample.jobs.eps.EpsXsdValidatort#read()}
	 * This test method tests the invalid name format of the input xml
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRead_failure_invalidFileName() throws Exception {
		inputFiles.add(new ClassPathResource("testfiles/1234567890.INV.SCHMA.D140815.T090909000.T.IN").getFile());
		EPSFileIndex epsFileIndex = fileIngestionReader.read();
		assertFalse("input file xsd validation flag is false", epsFileIndex.isValid());
	}
	
	/**
	 * Test method for {@linkgov.cms.eps.sample.jobs.eps.EpsXsdValidatort#read()}
	 * This test method tests the unsuccessful schema validation of the input xml
	 * against the specific xsd.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRead_schemaValidation_failure() throws Exception {
		inputFiles.add(new ClassPathResource("testfiles/1234567890.INVSCHMA.D140815.T090909000.T.IN").getFile());
		EPSFileIndex epsFileIndex = fileIngestionReader.read();
		assertFalse("input file xsd validation flag is false", epsFileIndex.isValid());
	}
	
	/**
	 * Test method for {@linkgov.cms.eps.sample.jobs.eps.EpsXsdValidatort#read()}
	 * This test method tests the unsuccessful schema validation of the input xml
	 * against the specific xsd.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRead_schemaValidation_bem_failure() throws Exception {
		inputFiles.add(new ClassPathResource("testfiles/1234567890.INVSCHBM.D140815.T090909000.T.IN").getFile());
		EPSFileIndex epsFileIndex = fileIngestionReader.read();
		assertFalse("input file xsd validation flag is false", epsFileIndex.isValid());
	}
	
	/**
	 * Test method for {@linkgov.cms.eps.sample.jobs.eps.EpsXsdValidatort#open()}
	 * This test method tests the open() method of the class when the file names in 
	 * the execution context is empty[first time].
	 * 
	 * @throws Exception
	 */
	@Test
	public void testOpen_null_inputFiles() {
		ExecutionContext ctx = new ExecutionContext();
		fileIngestionReader.open(ctx);
		assertNotNull("input file names list is not empty in execution context", ctx.get("INPUT_FILES"));
		assertEquals("inputFiles attribute value in context is same as what waas passed in", 
				ctx.get("INPUT_FILES"),
				ReflectionTestUtils.getField(fileIngestionReader, "inputFiles"));
		assertEquals("inputFiles in context is same as provided",
				Arrays.asList(inputDir.listFiles(xmlFilter)),
				ReflectionTestUtils.getField(fileIngestionReader, "inputFiles"));
	}
	
	/**
	 * Test method for {@linkgov.cms.eps.sample.jobs.eps.EpsXsdValidatort#open()}
	 * This test method tests the open() method of the ItemStream interface when the 
	 * file names in the execution context is not empty.
	 */
	@Test
	public void testOpen_with_inputFiles() {
		ExecutionContext ctx = new ExecutionContext();
		File [] dirFiles = inputDir.listFiles(xmlFilter);
		Collections.addAll(inputFiles, dirFiles);
		ctx.put("INPUT_FILES", inputFiles);
		fileIngestionReader.open(ctx);
		
		assertEquals("inputFiles in context is same as provided",
				inputFiles, ReflectionTestUtils.getField(fileIngestionReader, "inputFiles"));
	}
	
	/**
	 * Test method for {@linkgov.cms.eps.sample.jobs.eps.EpsXsdValidatort#open()}
	 * This test method tests the open() method of the ItemStream interface when the 
	 * input directory is invalid type.
	 * 
	 * @throws IOException 
	 */
	@Test(expected=java.lang.IllegalStateException.class)
	public void testOpen_with_inputDirInvalidType() throws IOException {
		ExecutionContext ctx = new ExecutionContext();
		ReflectionTestUtils.setField(fileIngestionReader, "privateDirectory", 
				new ClassPathResource("testfiles/1.IS834.D140530.T145543452.T.IN").getFile());
		fileIngestionReader.open(ctx);
		
		assertNotNull("Illegal State Exception expected via annotation", ctx);
	}
	
	/**
	 * Test method for {@linkgov.cms.eps.sample.jobs.eps.EpsXsdValidatort#update()}
	 * This test method tests the update() method of the ItemStream interface implementation
	 */
	@Test
	public void testUpdateExecutionContext() {
		ExecutionContext ctx = new ExecutionContext();
		fileIngestionReader.update(ctx);
		
		assertNotNull("value of INPUT_FILES key in context is not null", ctx.get("INPUT_FILES"));
		assertEquals("inputFiles in context is same as provided", inputFiles, ctx.get("INPUT_FILES"));
	}	
	
}
