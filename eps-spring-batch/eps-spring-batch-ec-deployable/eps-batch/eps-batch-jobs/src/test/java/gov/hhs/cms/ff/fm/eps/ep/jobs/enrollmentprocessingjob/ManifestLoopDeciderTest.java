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
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

@ContextConfiguration(locations={"classpath:/test-context.xml"})
@TestExecutionListeners( { DependencyInjectionTestExecutionListener.class })
@RunWith(SpringJUnit4ClassRunner.class)
public class ManifestLoopDeciderTest extends TestCase{
	
	private static String ROOT_TEST_PATH = "./Test834files";
	private  ManifestLoopDecider  manifestLoopDecider;
	
	@Value("./Test834files/manifest")
	private File manifestDirectory;
	
	@Value("./Test834files/manifest/Manifest-1.txt")
	private File manifestFile;
	
	JobExecution jobExecution = null;
	StepExecution stepExecution =null;
	

	@Before
	public void setUp() throws Exception {
		
		manifestLoopDecider = new ManifestLoopDecider();
		manifestDirectory.mkdirs();
		manifestLoopDecider.setManifestDirectory(manifestDirectory);
		
		
		ExecutionContext ctx = new ExecutionContext();
		jobExecution = new JobExecution(9999L);
		jobExecution.setExecutionContext(ctx);
	
		
	}

	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.ManifestLoopDecide#decide()}
	 * This method tests the flow execution decision if files exists in the manifest directory.
	 * @throws IOException 
	 * 
	 */
	@Test
	public void testDecide_ManifestExists() throws IOException {
	
		if (!manifestFile.exists())
			manifestFile.createNewFile();
		
		FlowExecutionStatus flowStatusEnum = manifestLoopDecider.decide(jobExecution, null);
		assertNotNull("flowStatusEnum returned is not null", flowStatusEnum);
		assertEquals("flowStatusEnum returned is LOOP", "LOOP", flowStatusEnum.getName()); 
		
		manifestFile.delete();
	}
	
	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.ManifestLoopDecide#decide()}
	 * This method tests the flow execution decision if files do not exist in the manifest directory.
	 * 
	 */
	@Test
	public void testDecide_ManifestDoesnotExist() {
	
		if (manifestFile.exists())
			manifestFile.delete();
	
		FlowExecutionStatus flowStatusEnum = manifestLoopDecider.decide(jobExecution, null);
		assertNotNull("flowStatusEnum returned is not null", flowStatusEnum);
		assertEquals("flowStatusEnum returned is COMPLETED", "COMPLETED", flowStatusEnum.getName()); 
		
	}
	
	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.ManifestLoopDecide#decide()}
	 * This method tests the flow execution decision if manifest directory does not exist.
	 * 
	 */
	@Test
	public void testDecide_DirectoryDoesnotExist() {
	
		if (manifestFile.exists())
			manifestFile.delete();
		if (manifestDirectory.exists())
			manifestDirectory.delete();
	
		FlowExecutionStatus flowStatusEnum = manifestLoopDecider.decide(jobExecution, null);
		assertNotNull("flowStatusEnum returned is not null", flowStatusEnum);
		assertEquals("flowStatusEnum returned is COMPLETED", "COMPLETED", flowStatusEnum.getName()); 
		
	}
	
	@After
	public void tearDown() {
		FileUtils.deleteQuietly(new File(ROOT_TEST_PATH));
	}

}
