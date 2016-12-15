package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm;

import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.CONTINUE;
import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.EXIT;
import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.JOB_EXIT_CODE;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileProcessingDTO;

/**
 * 
 * @author rajesh.talanki
 *
 */
public class SbmiFileIngestionTasklet implements Tasklet {

	private static final Logger LOG = LoggerFactory.getLogger(SbmiFileIngestionTasklet.class);

	private SbmiFileIngestionReader fileIngestionReader;
	private SbmiFileIngestionWriter fileIngestionWriter;
	private SBMEvaluatePendingFiles sbmEvaluatePendingFiles;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {		

		JobExecution jobExec = chunkContext.getStepContext().getStepExecution().getJobExecution();
		Long jobId = jobExec.getJobId();

		//read one file and write
		List<SBMFileProcessingDTO> dtoList = fileIngestionReader.read(jobId);

		if (!dtoList.isEmpty()) {

			jobExec.getExecutionContext().put(JOB_EXIT_CODE, CONTINUE);		
			
			for (SBMFileProcessingDTO dto : dtoList) {
		
				fileIngestionWriter.write(dto);
			}

		} else {
			//no files to process so evaluate all existing files			
			//evaluate all Bypass Freeze files
			sbmEvaluatePendingFiles.evaluateBypassFreeze(jobId);

			//evaluate all Freeze files
			sbmEvaluatePendingFiles.evaluateFreezeFiles(jobId);

			//evaluate all On-Hold files
			sbmEvaluatePendingFiles.evaluateOnHoldFiles(jobId);

			jobExec.getExecutionContext().put(JOB_EXIT_CODE, EXIT);
		}

		LOG.info("Value set for jobExitCode: {}", jobExec.getExecutionContext().get(JOB_EXIT_CODE));

		return RepeatStatus.FINISHED;
	}


	/**
	 * @param fileIngestionReader the fileIngestionReader to set
	 */
	public void setFileIngestionReader(SbmiFileIngestionReader fileIngestionReader) {
		this.fileIngestionReader = fileIngestionReader;
	}

	/**
	 * @param fileIngestionWriter the fileIngestionWriter to set
	 */
	public void setFileIngestionWriter(SbmiFileIngestionWriter fileIngestionWriter) {
		this.fileIngestionWriter = fileIngestionWriter;
	}

	/**
	 * @param sbmEvaluatePendingFiles the sbmEvaluatePendingFiles to set
	 */
	public void setSbmEvaluatePendingFiles(SBMEvaluatePendingFiles sbmEvaluatePendingFiles) {
		this.sbmEvaluatePendingFiles = sbmEvaluatePendingFiles;
	}


}
