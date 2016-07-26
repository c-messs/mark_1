/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob;

import static gov.hhs.cms.ff.fm.eps.ep.EPSConstants.MARKLOGIC_REC_COUNT;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import gov.hhs.cms.ff.fm.eps.ep.jobs.aop.ApplicationContextUtil;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Unit test class for the BemProcessorListener.java
 * @author girish.padmanabhan
 * 
 */
@ContextConfiguration(locations={"classpath:/test-context.xml"})
@TestExecutionListeners( { DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class })
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@RunWith(SpringJUnit4ClassRunner.class)
public class BemProcessorListenerTest extends TestCase {

	private static String ROOT_TEST_PATH = "./Test834files";
	private BemProcessorListener bemProcessorListener;
	
	@Value("./Test834files/private")
	private File sourceDirectory;
	
	@Value("./Test834files/invalid")
	private File invalidFilesDirectory;
	
	@Value("./Test834files/private/BenefitEnrollmentRequest.xml")
	private File berFile;
	
	@Autowired
	private String erlBEMIndexDeleteAll;
	
	@Autowired
	private String erlBEMIndexManifestCountSelect;
	
	ApplicationContextUtil mockApplicationContextUtil;
	JdbcTemplate mockJdbcTemplate;

	@Before
	public void setup() throws IOException {
		sourceDirectory.mkdirs();
		invalidFilesDirectory.mkdirs();
		berFile.createNewFile();
		mockApplicationContextUtil = createMock(ApplicationContextUtil.class);
		mockJdbcTemplate = createMock(JdbcTemplate.class);
		
		bemProcessorListener = new BemProcessorListener();
		bemProcessorListener.setApplicationContextUtil(mockApplicationContextUtil);
		bemProcessorListener.setJdbcTemplate(mockJdbcTemplate);
		bemProcessorListener.setSourceDirectory(sourceDirectory);
		bemProcessorListener.setInvalidFilesDirectory(invalidFilesDirectory);
		bemProcessorListener.setErlBEMIndexManifestCountSelect(erlBEMIndexManifestCountSelect);
	}

	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.BemProcessorListener#beforeStep()}
	 * This method will test the successful setting of the files in the private directory to the 
	 * MultiResourceItemReader 
	 * 
	 * @throws Exception
	 */
	@Test
	public void testBeforeStep_success() throws Exception {
		expect(mockApplicationContextUtil.getBean(EasyMock.anyString()))
				.andReturn(new MultiResourceItemReader<Object>());
		replay(mockApplicationContextUtil);
		
		StepExecution stepExecution = new StepExecution("bemProcesssor", null);
		bemProcessorListener.beforeStep(stepExecution);
		assertEquals("Step name is not modified","bemProcesssor", stepExecution.getStepName());
	}
	
	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.BemProcessorListener#afterStep()}
	 * This method will test the successful execution of afterStep() method with "source"
	 * parameter as "hub"
	 * 
	 * @throws Exception
	 */
	@Test
	public void testAfterStep_nonFFM_success() throws Exception {
		
		JobExecution jobExecution = createJobExecutionForNonFFM();
		StepExecution stepExecution = new StepExecution("bemProcesssor", jobExecution);
		
		ExitStatus stepStatus = bemProcessorListener.afterStep(stepExecution);
		
		assertEquals("step status is not FAILED", null, stepStatus);
	}

	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.BemProcessorListener#afterStep()}
	 * This method will test the successful execution of afterStep() method with 
	 * Marklogic record count as null
	 * 
	 * @throws Exception
	 */
	@Test
	public void testAfterStep_matchingCounts_success() throws Exception {
		
		JobExecution jobExecution = createJobExecution();
		//jobExecution.getExecutionContext().putInt(MARKLOGIC_REC_COUNT, 2);
		
		StepExecution stepExecution = new StepExecution("bemProcesssor", jobExecution);
		stepExecution.setReadCount(2);
		stepExecution.setWriteCount(2);
		
		ExitStatus stepStatus = bemProcessorListener.afterStep(stepExecution);
		
		assertEquals("step status is not FAILED", null, stepStatus);
	}
	
	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.BemProcessorListener#afterStep()}
	 * This method will test the successful execution of afterStep() method with 
	 * the read, write counts mismatch 
	 * 
	 * @throws Exception
	 */
	@Test
	public void testAfterStep_mismatch_writeCount() throws Exception {
		bemProcessorListener.setErlBEMIndexDeleteAll(erlBEMIndexDeleteAll);
		expect(mockJdbcTemplate.update(EasyMock.anyString(), EasyMock.anyLong(), EasyMock.anyString()))
				.andReturn(0);
		replay(mockJdbcTemplate);
		
		JobExecution jobExecution = createJobExecution();
		jobExecution.getExecutionContext().putInt(MARKLOGIC_REC_COUNT, 2);
		
		StepExecution stepExecution = new StepExecution("bemProcesssor", jobExecution);
		stepExecution.setReadCount(2);
		stepExecution.setWriteCount(1);
		
		ExitStatus stepStatus = bemProcessorListener.afterStep(stepExecution);
		
		assertEquals("step status is FAILED", ExitStatus.FAILED, stepStatus);
	}
	
	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.BemProcessorListener#afterStep()}
	 * This method will test the successful execution of afterStep() method with 
	 * the read count and Marklogic extract count mismatch 
	 * 
	 */
	@Test
	public void testAfterStep_mismatch_mlRecCount() {
		String erlBatchRunControlId = "0";
		ReflectionTestUtils.setField(bemProcessorListener, "erlBatchRunControlId", erlBatchRunControlId);
		
		Object[] args = {erlBatchRunControlId};
		expect(mockJdbcTemplate.queryForObject(
				erlBEMIndexManifestCountSelect, Integer.class, args)).andReturn(new Integer(2));
		
		expect(mockJdbcTemplate.update(EasyMock.anyString(), EasyMock.anyLong(), EasyMock.anyString())).andReturn(0);
		replay(mockJdbcTemplate);
		
		JobExecution jobExecution = createJobExecution();
		jobExecution.getExecutionContext().putInt(MARKLOGIC_REC_COUNT, 4);
		
		StepExecution stepExecution = new StepExecution("bemProcesssor", jobExecution);
		stepExecution.setReadCount(2);
		stepExecution.setWriteCount(2);
		
		ExitStatus stepStatus = bemProcessorListener.afterStep(stepExecution);
		
		assertEquals("step status is FAILED", ExitStatus.FAILED, stepStatus);
	}
	
	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.BemProcessorListener#afterStep()}
	 * This method will test the successful execution of afterStep() method with 
	 * all the read, write and Marklogic counts matching 
	 * 
	 * @throws Exception
	 */
	@Test
	public void testAfterStep_null_mlRecCount() throws Exception {
		String erlBatchRunControlId = "0";
		ReflectionTestUtils.setField(bemProcessorListener, "erlBatchRunControlId", erlBatchRunControlId);
		
		Object[] args = {erlBatchRunControlId};
		expect(mockJdbcTemplate.queryForObject(
				erlBEMIndexManifestCountSelect, Integer.class, args)).andReturn(new Integer(2));
		expect(mockJdbcTemplate.update(EasyMock.anyString(), EasyMock.anyString())).andReturn(new Integer(1));
		replay(mockJdbcTemplate);
		
		JobExecution jobExecution = createJobExecution();
		jobExecution.getExecutionContext().putInt(MARKLOGIC_REC_COUNT, 2);
		
		StepExecution stepExecution = new StepExecution("bemProcesssor", jobExecution);
		stepExecution.setReadCount(2);
		stepExecution.setWriteCount(2);
		
		ExitStatus stepStatus = bemProcessorListener.afterStep(stepExecution);
		
		assertEquals("step status is not FAILED", null, stepStatus);
	}
	
	private JobExecution createJobExecution() {
        final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
        params.put("source", new JobParameter("ffm"));
        JobExecution jobExecution = new JobExecution(9999L, new JobParameters(params));
        return jobExecution;
	}
	
	private JobExecution createJobExecutionForNonFFM() {
        final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
        params.put("source", new JobParameter("hub"));
        JobExecution jobExecution = new JobExecution(9999L, new JobParameters(params));
        return jobExecution;
	}

	@After
	public void tearDown() {
		FileUtils.deleteQuietly(new File(ROOT_TEST_PATH));
	}
}
