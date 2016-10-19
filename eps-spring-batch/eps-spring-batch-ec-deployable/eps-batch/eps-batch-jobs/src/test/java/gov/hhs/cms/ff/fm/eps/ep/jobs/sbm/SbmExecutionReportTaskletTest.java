/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMock;
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

import gov.hhs.cms.ff.fm.eps.dispatcher.EFTDispatchDriver;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMExecutionReportDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SbmExecutionReportDataService;
import junit.framework.TestCase;

/**
 * Test class for JdbcTasklet
 * 
 * @author girish.padmanabhan
 * 
 */
@RunWith(JUnit4.class)
public class SbmExecutionReportTaskletTest extends TestCase {

	private SbmExecutionReportTasklet sbmExecutionReportTasklet;
	private EFTDispatchDriver mockEFTDispatchDriver;
	private SbmExecutionReportDataService mockSbmExecutionReportDataService;
	
	@Before
	public void setup() {
		sbmExecutionReportTasklet = new SbmExecutionReportTasklet();
		
		mockEFTDispatchDriver= createMock(EFTDispatchDriver.class);
		sbmExecutionReportTasklet.setEftDispatcher(mockEFTDispatchDriver);
		
		mockSbmExecutionReportDataService= createMock(SbmExecutionReportDataService.class);
		sbmExecutionReportTasklet.setSbmExecutionReportDataService(mockSbmExecutionReportDataService);
		
		sbmExecutionReportTasklet.setEnvironmentCodeSuffix("TST");
	}

	/**
	 * valid job type parameter value
	 * 
	 * @throws Exception
	 */
	@Test
	public void testExecute_JobType_SBM() throws Exception {
		
		SBMExecutionReportDTO reportDto = new SBMExecutionReportDTO();
		reportDto.setCoverageYear(LocalDateTime.now().getYear());
		reportDto.setStateCd("VA");
		reportDto.setFileName("fileName");
		reportDto.setFileId("fileId");
		reportDto.setFileLoggedTimestamp(LocalDateTime.now());
		reportDto.setIssuerId("issuerId");
		reportDto.setIssuerFileSetId("issuerFileSetId");
		reportDto.setSbmFileNum(1);
		reportDto.setTotalFilesInFileset(3);
		reportDto.setFileStatus("APW");
		reportDto.setFileStatusTimestamp(LocalDateTime.now());
		
		List<SBMExecutionReportDTO> reportDtoList = Arrays.asList(reportDto);
		
		expect(mockSbmExecutionReportDataService.getSbmExecutionLog()).andReturn(reportDtoList);
		replay(mockSbmExecutionReportDataService);
		
		expect(mockEFTDispatchDriver.saveDispatchContent((byte[])EasyMock.anyObject(), 
				EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyString(), 
				EasyMock.anyString(), EasyMock.anyInt(), EasyMock.anyString(), EasyMock.anyString())).andReturn(1L);
		replay(mockEFTDispatchDriver);
		
		
		JobExecution jobEx = new JobExecution(9999L, getJobParams("sbmi"));
		jobEx.setJobInstance(new JobInstance(9999L, "sbmExecutionReport"));
		
		StepExecution stepExecution = new StepExecution("sbmExecutionReport", jobEx);
		StepContext stepContext = new StepContext(stepExecution);
		ChunkContext chunkContext = new ChunkContext(stepContext);
		
		RepeatStatus status = sbmExecutionReportTasklet.execute(null, chunkContext);
		
		assertNotNull("RepeatStatus not null", status);
		assertEquals("RepeatStatus is FINISHED", RepeatStatus.FINISHED, status);
	}
	
	private JobParameters getJobParams(String processingType) {
        final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
        params.put("processingType", new JobParameter(processingType));
        return new JobParameters(params);
	}

}
