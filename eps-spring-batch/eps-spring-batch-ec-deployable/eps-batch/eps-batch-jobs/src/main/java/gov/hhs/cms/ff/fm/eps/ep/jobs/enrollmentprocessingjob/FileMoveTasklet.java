/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob;

import gov.hhs.cms.ff.fm.eps.ep.enums.EProdEnum;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import com.accenture.foundation.common.exception.EnvironmentException;

/**
 * This class is a tasklet used to move files from a source directory to a
 * destination directory. The directory names and paths are passed in as properties
 * to the bean
 * 
 * @author girish.padmanabhan
 *
 */
public class FileMoveTasklet implements Tasklet {
	private static final Logger LOG = LoggerFactory.getLogger(FileMoveTasklet.class);
	private static final String TO  = " to ";
	
    private File sourceDirectory;
	private File destinationDirectory;
	
	/**
	 * This method iterates through the files in the source directory and moves them to 
	 * destination directory.
	 * 
	 * @return RepeatStatus
	 */
	@Override
	public RepeatStatus execute(StepContribution contribution,
			ChunkContext chunkContext) throws Exception {
		LOG.debug("ENTER execute() - Moving files from " +sourceDirectory+ TO +destinationDirectory);
		List <File> files = getFilesFromSourceDir();
		
		for (File file:files) {
			try {
				FileUtils.copyFileToDirectory(file, destinationDirectory);
				LOG.debug("File "+ file.getName() + " succesfully moved from " + sourceDirectory + TO + destinationDirectory);
				
				boolean fileDeleted = file.delete();
				if(!fileDeleted) {
					LOG.warn("Failed to delete file "+ file.getName() + " after copying to "+ destinationDirectory);
				}
					
			} catch (FileNotFoundException e) {
				LOG.warn(EProdEnum.EPROD_01.getLogMsg());
				throw e;
			} catch (IOException e) {
				LOG.warn(EProdEnum.EPROD_02.getLogMsg() + " - IO Exception occurred when moving "+file.getCanonicalPath()+ " to "+destinationDirectory);
				throw e;
			}
		}
		LOG.debug("EXIT execute() - Moving files from " +sourceDirectory+ TO +destinationDirectory+" complete");
		return RepeatStatus.FINISHED;
	}
	
	/**
	 * This method creates a list of files in the source directory.
	 * 
	 * @return List<File>
	 */
	List<File> getFilesFromSourceDir() {
        List<File> files = new ArrayList<File>();
        
        if (sourceDirectory.isDirectory()) {
        	//Create a Filename filter to identify filter files
        	File[] dirFiles = sourceDirectory.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isFile();
                }
            });
        	if(ArrayUtils.isNotEmpty(dirFiles)) {
        		Collections.addAll(files, dirFiles);
        	}
        } else {
            throw new EnvironmentException("EPROD-01 Service Access Failure, unexpected file type: sourceDirectory=" + sourceDirectory);
        }
        return files;
	}
	
	/**
	 * Set the sourceDirectory
	 * @param sourceDirectory
	 */
	public void setSourceDirectory(File sourceDirectory) {
		this.sourceDirectory = sourceDirectory;
	}

	/**
	 * Set the destinationDirectory
	 * @param destinationDirectory
	 */
	public void setDestinationDirectory(File destinationDirectory) {
		this.destinationDirectory = destinationDirectory;
	}

}
