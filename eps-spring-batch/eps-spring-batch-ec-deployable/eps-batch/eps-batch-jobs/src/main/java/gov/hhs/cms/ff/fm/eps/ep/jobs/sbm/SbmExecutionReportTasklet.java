/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm;

import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.FUNCTION_CODE_SBMIS;
import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.RPT_DELIMITER;
import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.RPT_LINE_DELIMITER;
import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.TARGET_EFT_APPLICATION_TYPE;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import gov.hhs.cms.ff.fm.eps.dispatcher.EFTDispatchDriver;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMExecutionReportDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SbmExecutionReportDataService;

/**
 * @author rajesh.talanki
 *
 */
public class SbmExecutionReportTasklet implements Tasklet {
	
	private static final Logger LOG = LoggerFactory.getLogger(SbmExecutionReportTasklet.class);
	
	private SbmExecutionReportDataService sbmExecutionReportDataService;
	private EFTDispatchDriver eftDispatcher;
	private String environmentCodeSuffix;	
	
	/* (non-Javadoc)
	 * @see org.springframework.batch.core.step.tasklet.Tasklet#execute(org.springframework.batch.core.StepContribution, org.springframework.batch.core.scope.context.ChunkContext)
	 */
	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		
		Long jobExecId = chunkContext.getStepContext().getStepExecution().getJobExecutionId();
		
		LOG.info("Executing SBM Execution Log Report: batch {} at {} ", jobExecId, LocalDateTime.now());
		
		List<SBMExecutionReportDTO> reportDtoList = sbmExecutionReportDataService.getSbmExecutionLog();
		
		generateReport(reportDtoList);
		
		return RepeatStatus.FINISHED;
		
	}
	
	/*
	 * Generate the CMS SBM Execution Log Report to EFT
	 */
	private void generateReport(List<SBMExecutionReportDTO> reportDtoList) throws SQLException, IOException {
		
		LOG.info("Generating SBM Execution log for {} files ", reportDtoList.size());
		
		
		StringBuffer reportString = new StringBuffer();
		
		reportString.append("CoverageYear");
		reportString.append(RPT_DELIMITER + "State");
		reportString.append(RPT_DELIMITER + "FileName");
		reportString.append(RPT_DELIMITER + "FileID");
		reportString.append(RPT_DELIMITER + "FileLoggedTimestamp (in EPS)");
		reportString.append(RPT_DELIMITER + "IssuerID");
		reportString.append(RPT_DELIMITER + "IssuerFileSetId");
		reportString.append(RPT_DELIMITER + "FileNumber");
		reportString.append(RPT_DELIMITER + "TotalFilesInFileset");
		reportString.append(RPT_DELIMITER + "Status");
		reportString.append(RPT_DELIMITER + "FileStatusTimestamp");
		reportString.append(RPT_LINE_DELIMITER);
		
		reportDtoList.forEach(reportDto -> {
			
			reportString.append(reportDto.getCoverageYear() + RPT_DELIMITER 
					+ reportDto.getStateCd() + RPT_DELIMITER 
					+ reportDto.getFileName() + RPT_DELIMITER
					+ reportDto.getFileId() + RPT_DELIMITER 
					+ reportDto.getFileLoggedTimestamp() + RPT_DELIMITER
					+ reportDto.getIssuerId() + RPT_DELIMITER
					+ reportDto.getIssuerFileSetId() + RPT_DELIMITER
					+ reportDto.getFileNum() + RPT_DELIMITER
					+ reportDto.getTotalFilesInFileset() + RPT_DELIMITER
					+ reportDto.getFileStatus() + RPT_DELIMITER
					+ reportDto.getFileStatusTimestamp()
					+ RPT_LINE_DELIMITER);
		});
		
		LOG.info("CMS Error Report: " + reportString);
		
		String reportFilename = EFTDispatchDriver.getFileID(FUNCTION_CODE_SBMIS);
		
		Long physicalDocId = eftDispatcher.saveDispatchContent(
				reportString.toString().getBytes(), reportFilename, FUNCTION_CODE_SBMIS, environmentCodeSuffix, null, 0, null); 
		LOG.info("SBMIS PhysicalDocumentId: {} for CMS SBM Exceution report PhysicalDocumentId: {} ", physicalDocId, reportFilename);	
	}

	/**
	 * @param eftDispatcher the eftDispatcher to set
	 */
	public void setEftDispatcher(EFTDispatchDriver eftDispatcher) {
		this.eftDispatcher = eftDispatcher;
	}
	
	/**
	 * @param environmentCodeSuffix the environmentCodeSuffix to set
	 */
	public void setEnvironmentCodeSuffix(String environmentCodeSuffix) {
		this.environmentCodeSuffix = environmentCodeSuffix;
	}

	/**
	 * @param sbmExecutionReportDataService the sbmExecutionReportDataService to set
	 */
	public void setSbmExecutionReportDataService(SbmExecutionReportDataService sbmExecutionReportDataService) {
		this.sbmExecutionReportDataService = sbmExecutionReportDataService;
	}

}
