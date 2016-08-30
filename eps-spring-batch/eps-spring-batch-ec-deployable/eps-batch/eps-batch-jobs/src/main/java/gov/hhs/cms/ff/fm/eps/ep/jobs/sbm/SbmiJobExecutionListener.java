package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm;

import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.JOBPARAMETER_PROCESSINGTYPE_SBMI;
import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.JOBPARAMETER_PROCESSTYPE;
import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.SBMI_JOB_NM;
import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.SBM_INGEST;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;

import com.accenture.foundation.common.exception.ApplicationException;

import gov.hhs.cms.ff.fm.eps.ep.StateProrationConfiguration;
import gov.hhs.cms.ff.fm.eps.ep.jobs.BaseJobExecutionListener;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMCache;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SbmConfigDao;

/**
 * This class serves as the Job Execution Listener for the SBMI Job to
 * perform certain pre and post activities for an execution of the Job.
 * 
 * @author eps
 *
 */
public class SbmiJobExecutionListener extends BaseJobExecutionListener {
	
	private static final Logger LOG = LoggerFactory.getLogger(SbmiJobExecutionListener.class);

	private Long jobId;	
	private String processingType;
	private SbmConfigDao sbmConfigDao;
	private SBMEvaluatePendingFiles sbmEvaluatePendingFiles;
	
	
	@Override
	public void beforeJob(JobExecution jobExecution) {
		this.jobId = jobExecution.getJobId();
		
		//Store job id in cache
		SBMCache.setJobExecutionId(jobId);
		
		processingType = jobExecution.getJobParameters().getString(JOBPARAMETER_PROCESSTYPE);
		
		//load ref data need for SBM processing
		loadRefData();
		
		if (StringUtils.isBlank(processingType) || (StringUtils.isNotBlank(processingType)
				&& processingType.equalsIgnoreCase(JOBPARAMETER_PROCESSINGTYPE_SBMI))) {
			
			//inserts batch process log record
			super.beforeJob(jobExecution);
			
			try {
				sbmEvaluatePendingFiles.evaluatePendingFiles(jobExecution.getId(), false);
			} catch (JAXBException | SQLException | IOException e) {
				LOG.error(e.getMessage());
				throw new ApplicationException(e.getMessage());
			}
		}
	}
	
	@Override
	public void afterJob(JobExecution jobExecution) {
		
		if( ! jobExecution.getStatus().isUnsuccessful()) {
			try {
				sbmEvaluatePendingFiles.evaluatePendingFiles(jobExecution.getId(), true);			
			} catch (JAXBException | SQLException | IOException e) {
				LOG.error(e.getMessage());
				throw new ApplicationException(e.getMessage());
			}
		}
		
		if (StringUtils.isBlank(processingType) || (StringUtils.isNotBlank(processingType)
				&& processingType.equalsIgnoreCase(JOBPARAMETER_PROCESSINGTYPE_SBMI))) {
			
			// updates batch process log
			super.afterJob(jobExecution); 
		}
	}
	
	private void loadRefData() {	
		
		//load error codes and description
		Map<String, String> errorCodeMap = sbmConfigDao.retrieveErrorCodesAndDescription();	
		
		if(MapUtils.isNotEmpty(errorCodeMap)) {
			SBMCache.getErrorCodeDescriptionMap().putAll(errorCodeMap);
		}
		LOG.info("Ref data ErrorCodeDescriptionMap loaded : {}", SBMCache.getErrorCodeDescriptionMap());
		
		//load state config data
		List<StateProrationConfiguration> stateConfigList = sbmConfigDao.retrieveSbmStates();
		
		for(StateProrationConfiguration stConfig: stateConfigList) {
			if(SBMCache.getStateProrationConfigMap().get(stConfig.getMarketYear()) == null) {
				SBMCache.getStateProrationConfigMap().put(stConfig.getMarketYear(), new HashMap<>());
			}
			
			SBMCache.getStateProrationConfigMap().get(stConfig.getMarketYear()).put(stConfig.getStateCd(), stConfig); 
		}
		
		LOG.info("Ref data StateProrationConfigMap loaded : {}", SBMCache.getStateProrationConfigMap());
		
		//load Business Rules
		loadBusinessRules();
		
		//load the Language Codes
		loadLanguageCodes();
		
		//load the Race-Ethnicity Codes
		loadRacEthnicityCodes();

	}
	
	/*
	 * load the State Business Rules
	 */
	private void loadBusinessRules() {
		
		//load businessRules
		List<String []> businessRules = sbmConfigDao.retrieveBusinessRules();
		
		businessRules.forEach(businessRule -> {
			
			String stateCd = businessRule[0];
			String bizRule = businessRule[1];
			
			if(SBMCache.getBusinessRulesMap().get(stateCd) == null) {
				SBMCache.getBusinessRulesMap().put(stateCd, new ArrayList<String>());
			}
			
			SBMCache.getBusinessRulesMap().get(stateCd).add(bizRule); 
		});
		
		LOG.info("Ref data BusinessRules Map loaded : {}", SBMCache.getBusinessRulesMap());	
	}

	
	/*
	 * Load the Language Codes to SBMCache
	 */
	private void loadLanguageCodes() {
		
		List<String> languageCodeList = sbmConfigDao.retrieveLanguageCodes();
		
		if(CollectionUtils.isNotEmpty(languageCodeList)) {
			
			SBMCache.getLanguageCodes().addAll(languageCodeList);
		}
	}

	/*
	 * load the Race-Ethnicity Codes to SBMCache
	 */
	private void loadRacEthnicityCodes() {

		List<String> racEthnicityCodeList = sbmConfigDao.retrieveRaceEthnicityCodes();
		
		if(CollectionUtils.isNotEmpty(racEthnicityCodeList)) {
			
			SBMCache.getRaceEthnicityCodes().addAll(racEthnicityCodeList);
		}
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
		
		if (StringUtils.isBlank(processingType) || (StringUtils.isNotBlank(processingType)
				&& processingType.equalsIgnoreCase(JOBPARAMETER_PROCESSINGTYPE_SBMI))) {
			return SBM_INGEST;
		}
		return SBMI_JOB_NM;
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

	/**
	 * @param sbmEvaluatePendingFiles the sbmEvaluatePendingFiles to set
	 */
	public void setSbmEvaluatePendingFiles(SBMEvaluatePendingFiles sbmEvaluatePendingFiles) {
		this.sbmEvaluatePendingFiles = sbmEvaluatePendingFiles;
	}

}
