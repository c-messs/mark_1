/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob;

import static gov.hhs.cms.ff.fm.eps.ep.EPSConstants.CONTINUE_INGEST;
import static gov.hhs.cms.ff.fm.eps.ep.EPSConstants.MANIFEST_FILE;
import static gov.hhs.cms.ff.fm.eps.ep.EPSConstants.N;
import static gov.hhs.cms.ff.fm.eps.ep.EPSConstants.Y;
import gov.hhs.cms.ff.fm.eps.ep.BatchRunControl;
import gov.hhs.cms.ff.fm.eps.ep.EPSConstants;
import gov.hhs.cms.ff.fm.eps.ep.dao.BatchRunControlDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.PolicyVersionDao;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.item.ItemReader;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * This class is the spring reader class to read the ERL extract manifest file
 * and pass on the manifest information to the Writer 
 * 
 * @author girish.padmanabhan
 *
 */
public class ErlManifestFileReader implements ItemReader<BatchRunControl> {
	private static final Logger LOG = LoggerFactory.getLogger(ErlManifestFileReader.class);
	
	private static final String SEPARATOR_CHAR = "=";
	
	private JobExecution jobExecutionContext;
	private BatchRunControlDao batchRunControlDao;
	private PolicyVersionDao policyVersionDao;
	private String erlBEMIndexCount;
	private JdbcTemplate jdbcTemplate;
	private File manifestDirectory;
	private Map<String, String> manifestDataMap = new HashMap<String, String>();
	private String manifestFileName=StringUtils.EMPTY;
	private boolean isManifestRead;
	
	/**
	 * This method is the interface method implementation
	 * Reads the ERL manifest file and creates the BatchRunControl object
	 * using the lines and passes on to writer
	 */
	@Override
	public BatchRunControl read() throws Exception {
		BatchRunControl manifestInfo = null;
		LOG.info("ENTER read()");
		
		File manifestFile = getManifestFile();
		isManifestRead =  isSameFile(manifestFile);
		
		boolean preAuditIngestComplete =  getPreAuditIngestStatus();
		
		if(!preAuditIngestComplete) {
			
			LOG.info("EPROD-99: Ending ERL File Ingest Job Due to Incomplete Pre-Audit Cutoff Ingestion");
			
			jobExecutionContext.getExecutionContext().putString(CONTINUE_INGEST, N);
			return null;
		}
		
		if(manifestFile != null && !isManifestRead) {	
			
			manifestDataMap.clear();
			
			try {
				for (String line : FileUtils.readLines(manifestFile)) {
					LOG.info("line: " + line);
					String[] manifestEntry = line.split(SEPARATOR_CHAR);
					manifestDataMap.put(manifestEntry[0].toUpperCase(Locale.ENGLISH), manifestEntry[1].trim());
				}
			} catch (IOException e) {
				LOG.warn("EPROD-01: Service Access Failure. Unable to read manifest file " 
						+ manifestFile.getCanonicalPath());
				throw e;
			}
			
			if (!(manifestDataMap.containsKey(EPSConstants.JOB_STATUS) && manifestDataMap
					.get(EPSConstants.JOB_STATUS).equalsIgnoreCase(EPSConstants.JOB_STATUS_SUCCESS))) {
				
				LOG.info("EPROD-99: Ending ERL File Ingest Job Due to Incomplete Manifest file");
				
				jobExecutionContext.getExecutionContext().putString(CONTINUE_INGEST, N);
				return null;
			}
			
			manifestInfo = new BatchRunControl();
			manifestInfo.setBatchRunControlId(manifestDataMap.get(EPSConstants.JOB_ID));
			
			// Manifest dates contain timezone and do not parse with just LocalDateTime, hence ZonedDateTime parsing.
			ZonedDateTime begHwmkZdt = ZonedDateTime.parse(manifestDataMap.get(EPSConstants.BEGIN_HIGH_WATER_MARK));
			ZonedDateTime endHwmkZdt = ZonedDateTime.parse(manifestDataMap.get(EPSConstants.END_HIGH_WATER_MARK));
			ZonedDateTime jobStartZdt = ZonedDateTime.parse(manifestDataMap.get(EPSConstants.JOB_START_TIME));
			ZonedDateTime jobEndZdt = ZonedDateTime.parse(manifestDataMap.get(EPSConstants.JOB_END_TIME));

			manifestInfo.setHighWaterMarkStartDateTime(begHwmkZdt.toLocalDateTime());
			manifestInfo.setHighWaterMarkEndDateTime(endHwmkZdt.toLocalDateTime());
			manifestInfo.setBatchStartDateTime(jobStartZdt.toLocalDateTime());
			manifestInfo.setBatchEndDateTime(jobEndZdt.toLocalDateTime());
			
			manifestInfo.setRecordCountQuantity(
					Integer.valueOf(manifestDataMap.get(EPSConstants.RECORD_COUNT)));
			manifestInfo.setPreAuditExtractCompletionInd(
					manifestDataMap.get(EPSConstants.PAETCOMPLETION));
			manifestInfo.setJobInstanceId(jobExecutionContext.getJobId());
			
			isManifestRead = true;
			manifestFileName = manifestFile.getName();
			
			jobExecutionContext.getExecutionContext().putString(
						MANIFEST_FILE, manifestFile.getAbsolutePath());
			jobExecutionContext.getExecutionContext().putString(CONTINUE_INGEST, Y);

		} 
		LOG.info("EXIT read()");
		
		return manifestInfo;
	}
	
	/*
	 * Get Pre-Audit Ingest status
	 */
	private boolean getPreAuditIngestStatus() {
		
		if (!isManifestRead) {
			
			String preAuditExtractInd = batchRunControlDao.getPreAuditExtractStatus();
			
			if (StringUtils.isNotBlank(preAuditExtractInd) && preAuditExtractInd.equalsIgnoreCase(Y)) {
					
				if(isPreAuditDataProcessed()) {
					//Query PV And log PVD
					LocalDateTime maxPolicyMaintDateTime = policyVersionDao.getLatestPolicyMaintenanceStartDateTime();
					
					LOG.info("Max Policy MaintenanceStartDateTime in EPS for the latest pre-Audit ingest: " + maxPolicyMaintDateTime);
					
					if(maxPolicyMaintDateTime == null) {
						return false;
					}
					
				} else {
					return false;
				}
			}
		}
		return true;
	}

	/*
	 * 
	 */
	private boolean isPreAuditDataProcessed() {
		
		int bemIndexCount = jdbcTemplate.queryForObject(erlBEMIndexCount, Integer.class);
		
		LOG.debug("bemIndexCount for erl PreAudit: " + bemIndexCount);
		
		if (bemIndexCount > 0) {
        	return false;
        }
		
		return true;
	}

	/**
	 * Get the file from the given manifest file path
	 */	
	private File getManifestFile() {
    	File[] dirFiles = manifestDirectory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
            	return pathname.isFile();
            }
        });
    	
		File file = null;
		if (dirFiles != null && dirFiles.length > 0) {
			Arrays.sort(dirFiles, new Comparator<File>() {
				/**
				 * @param file0
				 * @param file1
				 * @return
				 */
				public int compare(File file0, File file1) {
					
					String name0 = file0.getName();
					String name1 = file1.getName();
					
					int numName0 = Integer.parseInt(name0.substring(name0.indexOf('-')+1, name0.indexOf('.')));
					int numName1 = Integer.parseInt(name1.substring(name1.indexOf('-')+1, name1.indexOf('.')));
					
					return numName0 - numName1;
				}
			});
			
			file = dirFiles[0];
			LOG.info("manifest file: "+ file.getPath());
			
		} else {
			LOG.info("No manifest file present ");
		}
		return file;
	}

    private boolean isSameFile(File manifestFile) {
    	
    	if(StringUtils.isBlank(manifestFileName)) {
    		return false;
    	}
    	
    	return manifestFile == null || (manifestFile.getName().equals(manifestFileName));
    }
    
	/**
	 * @param manifestDirectory the manifestDirectory to set
	 */
	public void setManifestDirectory(File manifestDirectory) {
		this.manifestDirectory = manifestDirectory;
	}

	/**
	 * @param jobExecutionContext the jobExecutionContext to set
	 */
	public void setJobExecutionContext(JobExecution jobExecutionContext) {
		this.jobExecutionContext = jobExecutionContext;
	}

	/**
	 * @param batchRunControlDao the batchRunControlDao to set
	 */
	public void setBatchRunControlDao(BatchRunControlDao batchRunControlDao) {
		this.batchRunControlDao = batchRunControlDao;
	}

	/**
	 * @param policyVersionDao the policyVersionDao to set
	 */
	public void setPolicyVersionDao(PolicyVersionDao policyVersionDao) {
		this.policyVersionDao = policyVersionDao;
	}

	/**
	 * @param erlBEMIndexCount the erlBEMIndexCount to set
	 */
	public void setErlBEMIndexCount(String erlBEMIndexCount) {
		this.erlBEMIndexCount = erlBEMIndexCount;
	}

	/**
	 * @param jdbcTemplate the jdbcTemplate to set
	 */
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

}
