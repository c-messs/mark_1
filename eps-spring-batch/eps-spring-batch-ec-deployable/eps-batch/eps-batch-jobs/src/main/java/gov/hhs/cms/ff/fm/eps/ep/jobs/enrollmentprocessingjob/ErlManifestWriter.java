/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob;

import gov.hhs.cms.ff.fm.eps.ep.BatchRunControl;
import gov.hhs.cms.ff.fm.eps.ep.EPSConstants;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.item.ItemWriter;

/**
 * This is the writer class to write the erl extract files to privaatee directory
 * based on the Manifest file information 
 * 
 * @author girish.padmanabhan
 *
 */
public class ErlManifestWriter implements ItemWriter<BatchRunControl> {
	
	private static final Logger LOG = LoggerFactory.getLogger(FileIngestionWriter.class);
	
	private JobExecution jobExecutionContext;
	private File sourceDirectory;
	private File processingDirectory;

	/**
	 * Implementation of the write method from ItemWriter interface. The method  
	 * moves the erl extract files to the processing directory. It gets the path 
	 * of the source file from the BatchRunControl object passed to the writer.
	 * 
	 * It also stores the marklogic record count attribute into job execution context.
	 *   
	 * @throws IOException 
	 */
	@Override
	public void write(List<? extends BatchRunControl> manifestInfoList) throws IOException {
		LOG.info("ENTER write()");
		
		for (BatchRunControl manifestInfo : manifestInfoList) {
			
			if(manifestInfo.getBatchRunControlId() != null) {
				
				File extractFilesPath = new File(sourceDirectory
						.getCanonicalPath().concat(File.separator)
						.concat(manifestInfo.getBatchRunControlId()));
				LOG.info("erl extractFilesPath: " + extractFilesPath.getAbsolutePath());
				
				File[] dirFiles = extractFilesPath.listFiles();
				
				if (dirFiles != null) {
					for (File file:dirFiles) {
						LOG.debug("erl extractFilesPath: " + extractFilesPath.getAbsolutePath());
						try {
							FileUtils.moveToDirectory(file, processingDirectory, false);
							
						} catch (IOException e) {
							LOG.warn("EPROD-02: Private Folder Write Incomplete - IO Exception occurred when moving "
									+ file.getCanonicalPath() + " to " + processingDirectory);
							throw e;
						}
					}
				}
				jobExecutionContext.getExecutionContext().put(
						EPSConstants.ERL_EXTRACT_FILE_PATH, extractFilesPath);
				jobExecutionContext.getExecutionContext().putInt(
						EPSConstants.MARKLOGIC_REC_COUNT, manifestInfo.getRecordCountQuantity());
				jobExecutionContext.getExecutionContext().put(
						EPSConstants.BATCH_RUNCONTROL_ID, manifestInfo.getBatchRunControlId());
			}
		}
		
		LOG.info("EXIT write()");
	}

	/**
	 * @param sourceDirectory the sourceDirectory to set
	 */
	public void setSourceDirectory(File sourceDirectory) {
		this.sourceDirectory = sourceDirectory;
	}

	/**
	 * @param processingDirectory the processingDirectory to set
	 */
	public void setProcessingDirectory(File processingDirectory) {
		this.processingDirectory = processingDirectory;
	}

	/**
	 * @param jobExecutionContext the jobExecutionContext to set
	 */
	public void setJobExecutionContext(JobExecution jobExecutionContext) {
		this.jobExecutionContext = jobExecutionContext;
	}

}
