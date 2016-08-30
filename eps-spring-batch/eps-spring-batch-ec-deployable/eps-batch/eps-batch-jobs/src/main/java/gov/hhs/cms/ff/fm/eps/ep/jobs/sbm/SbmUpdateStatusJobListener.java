package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm;

import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.SBM_UPDATE_STATUS;

import java.sql.SQLException;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;

import gov.hhs.cms.ff.fm.eps.ep.jobs.BaseJobExecutionListener;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMCache;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SbmConfigDao;

/**
 * This class serves as the Job Execution Listener for the SBM UpadteStatus Job to
 * perform certain pre and post activities for an execution of the Job.
 * 
 * @author eps
 *
 */
public class SbmUpdateStatusJobListener extends BaseJobExecutionListener {
	
	private static final Logger LOG = LoggerFactory.getLogger(SbmUpdateStatusJobListener.class);

	private SbmConfigDao sbmConfigDao;
	
	private Long jobId;	
	
	@Override
	public void beforeJob(JobExecution jobExecution) {
		
		this.jobId = jobExecution.getJobId();
		//Store job id in cache
		SBMCache.setJobExecutionId(jobId);
		
		//load ref data need for SBM processing
		loadRefData();
		
		super.beforeJob(jobExecution);
		
	}
	
	@Override
	public void afterJob(JobExecution jobExecution) {
		super.afterJob(jobExecution); 
	}
	
	private void loadRefData() {	
		
		//load error codes and description
		Map<String, String> errorCodeMap = sbmConfigDao.retrieveErrorCodesAndDescription();	
		
		if(MapUtils.isNotEmpty(errorCodeMap)) {
			SBMCache.getErrorCodeDescriptionMap().putAll(errorCodeMap);
		}
		LOG.info("Ref data ErrorCodeDescriptionMap loaded : {}", SBMCache.getErrorCodeDescriptionMap());
		
	}
	
	/**
	 * check if there is any existing job ( with 'STARTED' status)
	 * @return
	 */
	public String checkJobInstanceForBatchProcess() throws SQLException{
         return  null;
	}
	
	/**
	 * Method to get the Batch Business Id Prefix
	 * 
	 * @return BatchBusinessIdPrefix
	 */
	public String getCodeForBatchBusinessId() {
		return getBatchBusinessIdPrefix();
	}

	/**
	 * This method returns Batch Business id prefix according to job name
	 * @return
	 */
	private String getBatchBusinessIdPrefix() {
		return SBM_UPDATE_STATUS;
	}
	
	/**
	 * This method should return the Job run user id/name.
	 * 
	 * @return  runBy name
	 */
	public String getRunBy() {
		return jobId == null? StringUtils.EMPTY : jobId.toString();
	}

	/**
	 * @param sbmConfigDao the sbmConfigDao to set
	 */
	public void setSbmConfigDao(SbmConfigDao sbmConfigDao) {
		this.sbmConfigDao = sbmConfigDao;
	}

}
