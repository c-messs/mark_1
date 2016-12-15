package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.xml.sax.SAXException;

import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileProcessingDTO;

public class SbmiFileIngestionTaskletTest {

	private SbmiFileIngestionTasklet tasklet;

	private SbmiFileIngestionReader mockFileIngestionReader;
	private SbmiFileIngestionWriter mockFileIngestionWriter; 
	private SBMEvaluatePendingFiles mockSbmEvaluatePendingFiles;

	@Before
	public void setUp() throws ParserConfigurationException, SAXException, IOException {

		tasklet = new SbmiFileIngestionTasklet();

	}

	@After
	public void tearDown() throws IOException {

		
	}

	@Test
	public void test_ValidFile() throws Exception {

		String expectedExitCd = SBMConstants.CONTINUE;

		JobExecution jobEx = new JobExecution(111111L);

		mockFileIngestionReader = EasyMock.createMock(SbmiFileIngestionReader.class);
		mockFileIngestionWriter = EasyMock.createMock(SbmiFileIngestionWriter.class);
		mockSbmEvaluatePendingFiles = EasyMock.createMock(SBMEvaluatePendingFiles.class);

		tasklet.setFileIngestionReader(mockFileIngestionReader);
		tasklet.setFileIngestionWriter(mockFileIngestionWriter);
		tasklet.setSbmEvaluatePendingFiles(mockSbmEvaluatePendingFiles);

		List<SBMFileProcessingDTO> mockDTOList = new ArrayList<SBMFileProcessingDTO>();
		mockDTOList.add(new SBMFileProcessingDTO());
		expect(mockFileIngestionReader.read(EasyMock.anyLong())).andReturn(mockDTOList);
		replay(mockFileIngestionReader);

		SBMFileProcessingDTO mockDTO = new SBMFileProcessingDTO();
		
		mockFileIngestionWriter.write(mockDTO);
		replay(mockFileIngestionWriter);

		ChunkContext chkContext = new ChunkContext(new StepContext(new StepExecution("fileIngestionStep", jobEx)));	
		StepExecution stepEx = new StepExecution("AnyStep", jobEx);
		StepContribution contribution = new StepContribution(stepEx);
		RepeatStatus status = tasklet.execute(contribution, chkContext);

		assertNotNull("Tasklet should not return null", status);
		assertEquals("RepeatStatus", RepeatStatus.FINISHED, status);
		assertEquals("Exit Code", expectedExitCd, jobEx.getExecutionContext().get(SBMConstants.JOB_EXIT_CODE));
		
		EasyMock.reset(mockFileIngestionReader);
		EasyMock.reset(mockFileIngestionWriter);
		EasyMock.reset(mockSbmEvaluatePendingFiles);

	}

	@Test
	public void test_InvalidFile() throws Exception {

		String expectedExitCd = SBMConstants.EXIT;

		Long jobId = Long.valueOf(222222);
		JobExecution jobEx = new JobExecution(jobId);

		mockFileIngestionReader = EasyMock.createMock(SbmiFileIngestionReader.class);
		mockSbmEvaluatePendingFiles = EasyMock.createMock(SBMEvaluatePendingFiles.class);

		tasklet.setFileIngestionReader(mockFileIngestionReader);
		tasklet.setSbmEvaluatePendingFiles(mockSbmEvaluatePendingFiles);

		List<SBMFileProcessingDTO> mockDTOList = new ArrayList<SBMFileProcessingDTO>();		
		expect(mockFileIngestionReader.read(EasyMock.anyLong())).andReturn(mockDTOList);
	
		mockSbmEvaluatePendingFiles.evaluateBypassFreeze(EasyMock.anyLong());
		mockSbmEvaluatePendingFiles.evaluateFreezeFiles(EasyMock.anyLong());
		mockSbmEvaluatePendingFiles.evaluateOnHoldFiles(EasyMock.anyLong());


		replay(mockSbmEvaluatePendingFiles, mockFileIngestionReader);

		ChunkContext chkContext = new ChunkContext(new StepContext(new StepExecution("fileIngestionStep", jobEx)));
		StepExecution stepEx = new StepExecution("AnyStep", jobEx);
		StepContribution contribution = new StepContribution(stepEx);
		RepeatStatus status = tasklet.execute(contribution, chkContext);

		assertNotNull("Tasklet should not return null", status);
		assertEquals("RepeatStatus", RepeatStatus.FINISHED, status);
		assertEquals("Exit Code", expectedExitCd, jobEx.getExecutionContext().get(SBMConstants.JOB_EXIT_CODE));
		
		EasyMock.reset(mockFileIngestionReader);
		EasyMock.reset(mockSbmEvaluatePendingFiles);

	}
}
