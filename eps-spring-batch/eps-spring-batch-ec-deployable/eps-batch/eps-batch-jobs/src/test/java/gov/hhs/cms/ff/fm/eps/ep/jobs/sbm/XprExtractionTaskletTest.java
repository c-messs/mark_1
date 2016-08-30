/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.repeat.RepeatStatus;

import gov.hhs.cms.ff.fm.eps.ep.dao.StagingSbmGroupLockDao;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileProcessingDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SBMFileCompositeDAO;
import junit.framework.TestCase;

/**
 * Test class for JdbcTasklet
 * 
 * @author girish.padmanabhan
 * 
 */
@RunWith(JUnit4.class)
public class XprExtractionTaskletTest extends TestCase {

	private XprExtractionTasklet xprExtractionTasklet;
	private SBMFileCompositeDAO mockSBMFileCompositeDAO;
	private StagingSbmGroupLockDao mockStagingSbmGroupLockDao;
	
	StepExecution stepExecution;
	
	@Before
	public void setup() {
		xprExtractionTasklet = new XprExtractionTasklet();
		
		mockSBMFileCompositeDAO = createMock(SBMFileCompositeDAO.class);
		xprExtractionTasklet.setSbmFileCompositeDAO(mockSBMFileCompositeDAO);
		
		mockStagingSbmGroupLockDao = createMock(StagingSbmGroupLockDao.class);
		xprExtractionTasklet.setStagingSbmGroupLockDao(mockStagingSbmGroupLockDao);
		
		xprExtractionTasklet.setXprProcessingGroupSize(10);
		
		JobExecution jobEx = new JobExecution(-9999L, getJobParams("sbmi"));
		jobEx.setJobInstance(new JobInstance(-9999L, "xprExtraction"));
		
		stepExecution = new StepExecution("xprProcessing", jobEx);
	}

	/**
	 * valid job type parameter value
	 * 
	 * @throws Exception
	 */
	@Test
	public void testExecute_JobType_SBM() throws Exception {
		
		mockSBMFileCompositeDAO.extractXprToStagingPolicy(EasyMock.anyObject(SBMFileProcessingDTO.class));
		EasyMock.expectLastCall(); 
		replay(mockSBMFileCompositeDAO);
		
		expect(mockStagingSbmGroupLockDao.deleteStagingGroupLockForExtract(EasyMock.anyLong())).andReturn(1);
		replay(mockStagingSbmGroupLockDao);

		JobExecution jobEx = new JobExecution(9999L, getJobParams("sbmi"));
		jobEx.setJobInstance(new JobInstance(9999L, "sbmi"));
		xprExtractionTasklet.setJobExecution(jobEx);
		
		RepeatStatus status = xprExtractionTasklet.execute(null, null);
		
		assertNotNull("RepeatStatus not null", status);
		assertEquals("RepeatStatus is FINISHED", RepeatStatus.FINISHED, status);
	}
	
	/**
	 * Invalid job type parameter value
	 * 
	 * @throws Exception
	 */
	@Test//(expected=ApplicationException.class)
	public void testExecute_With_JobType_Invalid() throws Exception {
		
		mockSBMFileCompositeDAO.extractXprToStagingPolicy(EasyMock.anyObject(SBMFileProcessingDTO.class));
		EasyMock.expectLastCall(); 
		replay(mockSBMFileCompositeDAO);
		
		expect(mockStagingSbmGroupLockDao.deleteStagingGroupLockForExtract(EasyMock.anyLong())).andReturn(1);
		replay(mockStagingSbmGroupLockDao);

		JobExecution jobEx = new JobExecution(9999L, getJobParams("abcd"));
		xprExtractionTasklet.setJobExecution(jobEx);
		
		RepeatStatus status = xprExtractionTasklet.execute(null, null);
		
		assertNotNull("Exception expected via annotation", status);
		assertEquals("RepeatStatus is FINISHED", RepeatStatus.FINISHED, status);
	}
	
	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.JdbcTasklet#execute()}
	 * This method tests the sql execution of the Tasklet, which then returns a 'RepeatStatus' of 'FINISHED'.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testExecute_Null_JobType() throws Exception {

		JobExecution jobEx = new JobExecution(9999L, getJobParams(null));
		xprExtractionTasklet.setJobExecution(jobEx);
		
		RepeatStatus status = xprExtractionTasklet.execute(null, null);
		
		assertNotNull("RepeatStatus not null", status);
		assertEquals("RepeatStatus is FINISHED", RepeatStatus.FINISHED, status);
	}

	@Test
	public void testExecute_Non_SBM() throws Exception {

		JobExecution jobEx = new JobExecution(9999L, getJobParams("xpr"));
		xprExtractionTasklet.setJobExecution(jobEx);
		
		RepeatStatus status = xprExtractionTasklet.execute(null, null);
		
		assertNotNull("RepeatStatus not null", status);
		assertEquals("RepeatStatus is FINISHED", RepeatStatus.FINISHED, status);
	}
	
	@Test
	public void testExecute_beforeStep() throws Exception {
		
		expect(mockStagingSbmGroupLockDao.updateStagingGroupLockForExtract(EasyMock.anyLong())).andReturn(true);
		replay(mockStagingSbmGroupLockDao);
		
		xprExtractionTasklet.beforeStep(stepExecution);
		
		assertEquals("Step name is not modified","xprProcessing", stepExecution.getStepName());
	}
	
	@Test
	public void testExecute_afterStep() throws Exception {
		
		ExitStatus stepStatus = xprExtractionTasklet.afterStep(stepExecution);
		
		assertNull("step exit status", stepStatus);
	}
	
	private JobParameters getJobParams(String processingType) {
        final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
        params.put("processingType", new JobParameter(processingType));
        return new JobParameters(params);
	}

}
