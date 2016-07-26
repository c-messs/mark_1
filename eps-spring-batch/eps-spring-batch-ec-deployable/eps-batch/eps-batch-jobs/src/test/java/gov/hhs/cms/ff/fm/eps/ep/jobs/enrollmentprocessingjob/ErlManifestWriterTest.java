/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob;

import gov.hhs.cms.ff.fm.eps.ep.BatchRunControl;

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
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Value;
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
@RunWith(SpringJUnit4ClassRunner.class)
public class ErlManifestWriterTest extends TestCase {

	private ErlManifestWriter erlManifestWriter;
	
	@Value("./Test834files")
	private File sourceDirectory;
	
	@Value("./Test834files/9999")
	private File inputDir;
	
	@Value("./Test834files/9999/BenefitEnrollmentRequest.xml")
	private File berFile;
	
	@Value("./Test834files/processing")
	private File processingDirectory;
	
	@Before
	public void setup() throws IOException {
		
		erlManifestWriter = new ErlManifestWriter();
		FileUtils.deleteQuietly(sourceDirectory);
		processingDirectory.mkdirs();
		inputDir.mkdirs();
		berFile.createNewFile();
		
		erlManifestWriter.setProcessingDirectory(processingDirectory);
		erlManifestWriter.setSourceDirectory(sourceDirectory);
		
		ExecutionContext ctx = new ExecutionContext();
		JobExecution jobExecution = new JobExecution(9999L);
		jobExecution.setExecutionContext(ctx);
		erlManifestWriter.setJobExecutionContext(jobExecution);
	}

	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.ErlManifestWriter#write(gov.hhs.cms.ff.fm.eps.ep.BatchRunControl)}
	 * This method tests the moving the file to the processing directory 
	 * 
	 * @throws Exception
	 */
	@Test
	public void testWrite_moveFileToProcessing() throws Exception {
		erlManifestWriter.write(createBatchRunControlList());
		File [] dirFiles = processingDirectory.listFiles();
		assertEquals("The file name in the processing directory is BenefitEnrollmentRequest.xml", 
				berFile.getName(), Arrays.asList(dirFiles).get(0).getName());
	}

	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.ErlManifestWriter#write(gov.hhs.cms.ff.fm.eps.ep.BatchRunControl)}
	 * This method tests the moving the file to the processing directory when batch run control id is null
	 * 
	 * @throws Exception
	 */
	@Test
	public void testWrite_nullBatchRunControlId() throws Exception {
		List<BatchRunControl> batchRunControls = new ArrayList<BatchRunControl>();
		BatchRunControl batchRunControl  = new BatchRunControl();
		batchRunControls.add(batchRunControl);
		
		erlManifestWriter.write(batchRunControls);
		File [] dirFiles = processingDirectory.listFiles();
		
		assertEquals("Processing Directory is empty", dirFiles.length, 0);
		
	}
	
	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.ErlManifestWriter#write(gov.hhs.cms.ff.fm.eps.ep.BatchRunControl)}
	 * This method tests the result of moving the file to the processing directory 
	 * when the directory doesn't exist, expecting an IOException
	 * 
	 * @throws Exception
	 */	
	@Test(expected=IOException.class)
	public void testWrite_ioexception() throws Exception {
		assertNotNull("invalid directory not null", processingDirectory);
		FileUtils.deleteQuietly(processingDirectory);
		erlManifestWriter.write(createBatchRunControlList());
	}
	
	/*
	 * private method to create input object to be passed to the test method.
	 */
	private List<BatchRunControl> createBatchRunControlList() throws IOException {
		List<BatchRunControl> batchRunControls = new ArrayList<BatchRunControl>();
		
		BatchRunControl batchRunControl  = new BatchRunControl();
		batchRunControl.setBatchRunControlId("9999");
		batchRunControl.setRecordCountQuantity(10);
		batchRunControls.add(batchRunControl);
		
		return batchRunControls;
	}	
	
	
	@After
	public void tearDown() {
		FileUtils.deleteQuietly(sourceDirectory);
	}
}
