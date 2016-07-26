package gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob;

import java.io.File;
import java.io.FileFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;


/**
 * @author SP
 *
 */
public class ManifestLoopDecider implements JobExecutionDecider {
	
	private static final Logger LOG = LoggerFactory.getLogger(ErlLimitDecider.class);
	private static final String FLOWEXECUTIONSTATUS_LOOP = "LOOP";
	private File manifestDirectory;	

	/**
	 * Implementation of the interface method. Based on existence of manifest files, returns
	 * the FlowExecutionStatus enum value as 'LOOP' or 'COMPLETED'
	 */
	@Override
	public FlowExecutionStatus decide(JobExecution jobExecution,
			StepExecution stepExecution) {
		
		/*
		 * Get the list of files in the manifest directory.
		 */
		File[] dirFiles = manifestDirectory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
            	return pathname.isFile();
            }
        });
		
		LOG.debug("dirFiles " + (dirFiles==null? "No more manifest files": dirFiles.length));
		
		if (dirFiles != null && dirFiles.length > 0) {			
			
			return new  FlowExecutionStatus(FLOWEXECUTIONSTATUS_LOOP);
		}
		
		LOG.info("No More Manifest Files in " + manifestDirectory);
		return FlowExecutionStatus.COMPLETED;
	}

	/**
	 * @param manifestDirectory the manifestDirectory to look for files.
	 */
	public void setManifestDirectory(File manifestDirectory) {
		this.manifestDirectory = manifestDirectory;
	}

}
