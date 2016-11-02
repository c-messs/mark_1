/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob;

import static gov.hhs.cms.ff.fm.eps.ep.EPSConstants.ERL_EXTRACT_FILE_PATH;
import static gov.hhs.cms.ff.fm.eps.ep.EPSConstants.MANIFEST_FILE;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

/**
 * This class is a tasklet used to archive the read extract manifest files after 
 * ERL ingestion. It also deletes the batch file directory which is empty after
 * the processing
 * 
 * @author girish.padmanabhan
 *
 */
public class ErlManifestFileCleanUpTasklet implements Tasklet {
	private static final Logger LOG = LoggerFactory.getLogger(ErlManifestFileCleanUpTasklet.class);
	
	private File destinationDirectory;
	
	/**
	 * This method gets the manifest file from job execution context and moves to 
	 * destination directory. It then gets the erl extract file path from 
	 * job execution context and deletes the directory.
	 * 
	 * @return RepeatStatus
	 */
	@Override
	public RepeatStatus execute(StepContribution contribution,
			ChunkContext chunkContext) throws Exception {
		
		String fileName = (String)chunkContext.getStepContext()
				.getJobExecutionContext().get(MANIFEST_FILE);
		
		if (StringUtils.isNotEmpty(fileName)) {
			File file = new File(fileName);
			
			LOG.debug("ENTER execute() - Moving file " + fileName + " to " + destinationDirectory);
			try {
				FileUtils.copyFileToDirectory(file, destinationDirectory);
				LOG.info("File "+ file.getName() + " succesfully moved to "+ destinationDirectory);
				
				boolean fileDeleted = file.delete();
				if(!fileDeleted) {
					LOG.warn("Failed to delete file "+ file.getName() + " after copying to "+ destinationDirectory);
				}
					
			} catch (FileNotFoundException e) {
				LOG.warn("EPROD-01: Service Access Failure. Manifest file not found.");
				throw e;
			} catch (IOException e) {
				LOG.warn("EPROD-02: Private Folder Write Incomplete - IO Exception occurred when moving "
						+ file.getCanonicalPath() + " to " + destinationDirectory);
				throw e;
			}
		}
		
		//Delete the empty extract directory
		deleteEmptyPth(chunkContext);
		
		LOG.debug("EXIT execute()");
		return RepeatStatus.FINISHED;
	}

	private void deleteEmptyPth(ChunkContext chunkContext) throws IOException {
		File extractFilesPath = (File)chunkContext.getStepContext()
				.getJobExecutionContext().get(ERL_EXTRACT_FILE_PATH);
		try {
			if(extractFilesPath != null && ArrayUtils.isEmpty(extractFilesPath.list())) {
				FileUtils.deleteDirectory(extractFilesPath);
			}
		} catch (IOException e) {
			LOG.warn("Unable to delete the empty extract directory "+ extractFilesPath);
			throw e;
		}		
		
	}

	/**
	 * Set the destinationDirectory
	 * 
	 * @param destinationDirectory
	 */
	public void setDestinationDirectory(File destinationDirectory) {
		this.destinationDirectory = destinationDirectory;
	}

}
