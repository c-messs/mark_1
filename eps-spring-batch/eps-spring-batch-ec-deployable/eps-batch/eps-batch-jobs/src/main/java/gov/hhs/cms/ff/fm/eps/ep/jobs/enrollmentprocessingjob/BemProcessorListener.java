package gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob;

import static gov.hhs.cms.ff.fm.eps.ep.EPSConstants.JOBPARAMETER_ERL_JOB_TYPE;
import static gov.hhs.cms.ff.fm.eps.ep.EPSConstants.JOBPARAMETER_KEY_SOURCE;
import static gov.hhs.cms.ff.fm.eps.ep.EPSConstants.JOBPARAMETER_SOURCE_FFM;
import static gov.hhs.cms.ff.fm.eps.ep.EPSConstants.MARKLOGIC_REC_COUNT;
import gov.hhs.cms.ff.fm.eps.ep.jobs.aop.ApplicationContextUtil;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * This is the listener class used for the Bem Extraction Step.
 * beforeStep() method - sets the source files to be read
 * afterStep() method - performs volume comparison for ERL
 * 
 * @author eps
 *
 */
public class BemProcessorListener implements StepExecutionListener  {
	private static final Logger LOG = LoggerFactory.getLogger(BemProcessorListener.class);
	
	private ApplicationContextUtil applicationContextUtil;
	private File sourceDirectory;
	private JdbcTemplate jdbcTemplate;
	private String erlBEMIndexDeleteAll;
	private String erlBEMIndexDeleteForManifest;
	private String erlBEMIndexManifestCountSelect;
	private String erlBatchRunControlUpdateIngestStatus;
	private String erlBatchRunControlId;
	private File invalidFilesDirectory;
	private Long jobId;
	
	/**
	 * This method sets he source files to be read
	 */
	@Override
	public void beforeStep(StepExecution stepExecution) {
		LOG.debug("BemProcessorListener.Before Step: " + stepExecution.getStepName());
		
		MultiResourceItemReader<?> multiFilesReader = 
				(MultiResourceItemReader<?>)applicationContextUtil.getBean("multiResourceReader");
		Resource[] resources = getFiles();
		multiFilesReader.setResources(resources);
		
		String source = stepExecution.getJobParameters().getString(JOBPARAMETER_KEY_SOURCE);
		String erlJobType = stepExecution.getJobParameters().getString(JOBPARAMETER_ERL_JOB_TYPE);
		
		if (JOBPARAMETER_SOURCE_FFM.equalsIgnoreCase(source) && StringUtils.isBlank(erlJobType)) {
			jdbcTemplate.update(erlBEMIndexDeleteForManifest, erlBatchRunControlId);
		}
	}
	
	/**
	 * This method performs volume comparison for ERL and logs the appropriate
	 * EPROD errors in case of mismatch between expected and actual counts
	 */
	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		String source = stepExecution.getJobParameters().getString(JOBPARAMETER_KEY_SOURCE);
		
		if (!JOBPARAMETER_SOURCE_FFM.equalsIgnoreCase(source)) {
			return null;
		}
		jobId = stepExecution.getJobExecution().getJobId();

		//Compare read count against write count
		int readCount = stepExecution.getReadCount();
		int writeCount = stepExecution.getWriteCount();
		
		LOG.debug("readCount: "+ readCount + ", writeCount: "+ writeCount);
		//writeCount = 10; //TODO - remove this - kept for simulating IOException on file move
		if(readCount != writeCount) {
			LOG.error("EPROD-26: Read and write counts mismatch - readCount: "+ readCount + ", writeCount: "+ writeCount);
			
			//Clearing BEM Index table entries for manual rerun
			clearErlBemIndex();
			
			return ExitStatus.FAILED;
		}
		
		//Compare with volume of records extracted from MarkLogic
		if (stepExecution.getJobExecution().getExecutionContext().get(MARKLOGIC_REC_COUNT) != null) { 
			
			int markLogicRecordCount = stepExecution.getJobExecution().getExecutionContext().getInt(MARKLOGIC_REC_COUNT);
			LOG.debug("markLogicRecordCount: "+ markLogicRecordCount);
			
			Object[] args = {erlBatchRunControlId};
			int bemIndexCount = jdbcTemplate.queryForObject(erlBEMIndexManifestCountSelect, Integer.class, args);
			
			if(bemIndexCount != markLogicRecordCount) {
				LOG.error("EPROD-27: Expected and actual counts mismatch for Manifest-"+ erlBatchRunControlId + "; bemIndexWriteCount: "+ bemIndexCount + ", markLogicRecordCount: "+ markLogicRecordCount);
				
				//Clearing BEM Index table entries and files for manual rerun
				clearStagingArea();
				
				return ExitStatus.FAILED;
			}
			
		}
		//Update status to Y.
		updateIngestStatus();
		
		return null;
	}

	private void updateIngestStatus()
	{
        LOG.debug("Updating Ingest Status to Y for Manifest : " + erlBatchRunControlId);
		
		jdbcTemplate.update(erlBatchRunControlUpdateIngestStatus,erlBatchRunControlId);
		
		LOG.debug("BEM Index Ingest Status updated to Y for Manifest : "+ erlBatchRunControlId);
	}
	
	/*
	 * Method to clear staging area prior to re-extract
	 */
	private void clearStagingArea() {
		clearErlBemIndex();
		clearFiles();
	}
	
	/*
	 * Moves the files from source directory to an invalid files directory
	 */
	private void clearFiles() {
		LOG.debug("Deleting the files from "+ sourceDirectory);
		try {
			Resource [] files = getFiles();
			for (Resource file : files) {
				FileUtils.moveToDirectory(file.getFile(), invalidFilesDirectory, false);
				LOG.debug("File "+ file.getFilename() + " moved from " + sourceDirectory + " to "+ invalidFilesDirectory);
			}
		} catch (IOException e) {
			String exMessage = "EPROD-28: Failed to clear private directory from ERL Staging area: "+ sourceDirectory;
			LOG.error(exMessage + "\n" + e.getMessage());
		}
	}

	/*
	 * Clears the Bem Index table
	 */
	private void clearErlBemIndex() {
		LOG.debug("Clearing BEM Index table entries for manual rerun");
		
		jdbcTemplate.update(erlBEMIndexDeleteAll, jobId, erlBatchRunControlId);
		
		LOG.debug("BEM Index deleted for batch id: "+ jobId);
	}

	/**
	 * Get the files from private directory to be set to the MultiResourceItemReader
	 */	
	private Resource [] getFiles() {
    	File[] dirFiles = sourceDirectory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
            	return pathname.isFile();
            }
        });
    	List<FileSystemResource> resources = new ArrayList<FileSystemResource>();
    	
    	if (dirFiles != null) {
			for (File file:dirFiles) {
				FileSystemResource fsResource = new FileSystemResource(file);
				resources.add(fsResource);
			}
    	}
		return resources.toArray(new Resource[resources.size()]);
	}

	/**
	 * @param sourceDirectory the sourceDirectory to set
	 */
	public void setSourceDirectory(File sourceDirectory) {
		this.sourceDirectory = sourceDirectory;
	}

	/**
	 * @param applicationContextUtil the applicationContextUtil to set
	 */
	public void setApplicationContextUtil(ApplicationContextUtil applicationContextUtil) {
		this.applicationContextUtil = applicationContextUtil;
	}

	/**
	 * @param jdbcTemplate the jdbcTemplate to set
	 */
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * @param erlBEMIndexDeleteAll the erlBEMIndexDeleteAll to set
	 */
	public void setErlBEMIndexDeleteAll(String erlBEMIndexDeleteAll) {
		this.erlBEMIndexDeleteAll = erlBEMIndexDeleteAll;
	}

	/**
	 * @param erlBEMIndexDeleteForManifest the erlBEMIndexDeleteForManifest to set
	 */
	public void setErlBEMIndexDeleteForManifest(String erlBEMIndexDeleteForManifest) {
		this.erlBEMIndexDeleteForManifest = erlBEMIndexDeleteForManifest;
	}

	/**
	 * @param erlBEMIndexManifestCountSelect the erlBEMIndexManifestCountSelect to set
	 */
	public void setErlBEMIndexManifestCountSelect(
			String erlBEMIndexManifestCountSelect) {
		this.erlBEMIndexManifestCountSelect = erlBEMIndexManifestCountSelect;
	}

	/**
	 * @param erlBatchRunControlId the erlBatchRunControlId to set
	 */
	public void setErlBatchRunControlId(String erlBatchRunControlId) {
		this.erlBatchRunControlId = erlBatchRunControlId;
	}

	/**
	 * @param invalidFilesDirectory the invalidFilesDirectory to set
	 */
	public void setInvalidFilesDirectory(File invalidFilesDirectory) {
		this.invalidFilesDirectory = invalidFilesDirectory;
	}

	/**
	 * @param erlBatchRunControlUpdateIngestStatus the erlBatchRunControlUpdateIngestStatus to set
	 */
	public void setErlBatchRunControlUpdateIngestStatus(
			String erlBatchRunControlUpdateIngestStatus) {
		this.erlBatchRunControlUpdateIngestStatus = erlBatchRunControlUpdateIngestStatus;
	}

}
