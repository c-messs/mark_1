/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm.updatestatus;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import com.accenture.foundation.common.exception.ApplicationException;

import gov.hhs.cms.ff.fm.eps.ep.jobs.CommonUtil;

/**
 * @author rajesh.talanki
 *
 */
public class SbmUpdateStatusTasklet implements Tasklet {
	
	private static final Logger LOG = LoggerFactory.getLogger(SbmUpdateStatusTasklet.class);
	
	
	private File eftFolder;
	private File privateFolder;
	private File processedFolder;	
	private SbmUpdateStatusProcessor updateStatusProcessor;
	
	
	/* (non-Javadoc)
	 * @see org.springframework.batch.core.step.tasklet.Tasklet#execute(org.springframework.batch.core.StepContribution, org.springframework.batch.core.scope.context.ChunkContext)
	 */
	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		
		Long jobExecId = chunkContext.getStepContext().getStepExecution().getJobExecutionId();
		
		//check if SBMI job is running then	fails the job with EPROD-39 error
		if(updateStatusProcessor.isSBMIJobRunning()) {
			throw new ApplicationException("EPROD-39 Unable to execute batch job request. Another essential batch job is currently in progress.");
		}
		
		File fileToProcess = getAFileToProcess();
		if(fileToProcess == null) {
			LOG.info("No files to process.");
			return RepeatStatus.FINISHED;
		}
		
		updateStatusProcessor.processUpdateStatus(fileToProcess, jobExecId);
		
		//move file to processed folder
		FileUtils.moveFileToDirectory(fileToProcess, processedFolder, false);
		
		return null;
	}
	
	
	private File getAFileToProcess() throws IOException {
		
		File fileToProcess = null;
		
		List<File> filesList = CommonUtil.getFilesFromDir(privateFolder);
		LOG.info("Files in {}: {}", privateFolder, filesList);
				
		if(CollectionUtils.isNotEmpty(filesList)) {
			LOG.info("Files in {}: {}", privateFolder, filesList);
			if(CollectionUtils.isNotEmpty(filesList)) {
				fileToProcess = filesList.get(0);
			}				
		}
		else {
			filesList = CommonUtil.getFilesFromDir(eftFolder);
			LOG.info("Files in {}: {}", eftFolder, filesList);
			if(CollectionUtils.isNotEmpty(filesList)) {
				File fileFromEft = filesList.get(0);
				fileToProcess = new File(privateFolder, fileFromEft.getName());

				LOG.info("Moving file from {} to {}", fileFromEft, fileToProcess);
				FileUtils.moveFile(fileFromEft, fileToProcess);
			}
		}	
		
		LOG.info("Returning {}", fileToProcess);
		return fileToProcess;
	}

	/**
	 * @param eftFolder the eftFolder to set
	 */
	public void setEftFolder(File eftFolder) {
		this.eftFolder = eftFolder;
	}

	/**
	 * @param privateFolder the privateFolder to set
	 */
	public void setPrivateFolder(File privateFolder) {
		this.privateFolder = privateFolder;
	}

	/**
	 * @param processedFolder the processedFolder to set
	 */
	public void setProcessedFolder(File processedFolder) {
		this.processedFolder = processedFolder;
	}

	/**
	 * @param updateStatusProcessor the updateStatusProcessor to set
	 */
	public void setUpdateStatusProcessor(SbmUpdateStatusProcessor updateStatusProcessor) {
		this.updateStatusProcessor = updateStatusProcessor;
	}
	
}
