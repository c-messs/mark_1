package gov.hhs.cms.ff.fm.eps.ep.jobs.retroactivepaymentsjob;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import gov.hhs.cms.ff.fm.eps.ep.jobs.BaseJobExecutionListener;
import gov.hhs.cms.ff.fm.eps.rap.domain.BatchProcessLog;
import gov.hhs.cms.ff.fm.eps.rap.domain.RapConstants;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.jdbc.core.JdbcTemplate;

import com.accenture.foundation.common.exception.ApplicationException;
import com.accenture.foundation.common.exception.EnvironmentException;

/**
 * This class serves as the Job Execution Listener for the RAP Job to
 * perform certain pre and post activities for an execution of the RAP Job.
 * 
 * @author eps
 *
 */
public class RapJobExecutionListener extends BaseJobExecutionListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(RapConstants.RAP_LOGGER);

	private String updatePendingRecordSql;
	private String selectMaxPolicyMaintStartDateTime;	
	private JdbcTemplate jdbcTemplate;
	private String jobName;
	private Long jobId;	
	private String type;
	private Map<String,List<String>> blockConcurrentJobExecutionMap;

	
	@Override
	public void beforeJob(JobExecution jobExecution) {
		this.jobId = jobExecution.getJobId();
				
        jobName = jobExecution.getJobInstance().getJobName();
        type = jobExecution.getJobParameters().getString(RapConstants.JOBPARAMETER_KEY_RAPJOB_TYPE);
        
        // If source is not passsed consider it as a RAP run.
		if (type == null) {
			type = RapConstants.JOBPARAMETER_TYPE_RAP;
		}

		if(!MapUtils.isEmpty(blockConcurrentJobExecutionMap)) {
			List<String> list = (List<String>) blockConcurrentJobExecutionMap.get(type);
			super.blockConcurrentJobExecutionList = list;
		}
		
        LOGGER.info("job id: " + jobExecution.getJobId());
		
        try {
        	// Check JOB Instance status .
        	if (RapConstants.JOBPARAMETER_TYPE_RAPSTAGE.equalsIgnoreCase(type)) {            	
            	 //Check JOB Instance status in case of RAP .
                String batchBusinessId = checkJobInstanceForBatchProcess();
                
                if(StringUtils.isNotBlank(batchBusinessId)) {
                 throw new ApplicationException(getErrorMsgForJobInstanceRunning(jobExecution, batchBusinessId));
                }
            }
        	
            //check if this job should not execute concurrently with other jobs
            List<BatchProcessLog> jobsCurrenltyRunning = getJobsCurrentRunning();
            if(!jobsCurrenltyRunning.isEmpty()) {            	
           		throw new ApplicationException(getErrorMsgForRunningJobs(jobsCurrenltyRunning));            	  
            }       		

       } catch (SQLException e) {
            throw new EnvironmentException(RapConstants.ERRORCODE_E9004+": "+"Job Instance check failed ", e);
       }
	
    	//Get max policy from the batch process log from RAP Process records if it is a stage run.
		if (RapConstants.JOBPARAMETER_TYPE_RAPSTAGE.equalsIgnoreCase(type)) { 
			
			Timestamp maxPolicyMaintStartDateTime = jdbcTemplate.queryForObject(selectMaxPolicyMaintStartDateTime,Timestamp.class);
			jobExecution.getExecutionContext().put(RapConstants.LASTPOLICYVERSIONDATE, new DateTime(maxPolicyMaintStartDateTime.getTime()));  
			
			// Update pending records from previous run.
			jdbcTemplate.update(updatePendingRecordSql);
		}
        
		// writes into batch log
		writeToBatchProcessLog(jobExecution);
	}
	
	
	/**
	 * This method updates batch process information in BatchProcessLog table.
	 * 
	 * @param jobExecution
	 */
	protected void updateBatchStatus(JobExecution jobExecution) {
		 
		BatchProcessLog batchProcessLog = new BatchProcessLog();
		batchProcessLog.setBatchBusinessId(jobExecution.getExecutionContext().getString(RapConstants.BATCH_BUSINESS_ID));
		batchProcessLog.setJobId(jobExecution.getJobId());
		batchProcessLog.setJobNm(jobExecution.getJobInstance().getJobName());
		batchProcessLog.setJobStatusCd(jobExecution.getStatus().toString());
		batchProcessLog.setStartDateTime(new DateTime(jobExecution.getStartTime().getTime()));
		batchProcessLog.setEndDateTime(new DateTime(jobExecution.getEndTime().getTime()));
		batchProcessLog.setJobParameterText(getJobParameterText());
		batchProcessLog.setRunByNm(getRunBy());
		
		if(jobExecution.getExecutionContext().get(RapConstants.LASTPOLICYVERSIONDATE) != null){
			DateTime lastpolicyVersionDate = (DateTime)jobExecution.getExecutionContext().get(RapConstants.LASTPOLICYVERSIONDATE);
			batchProcessLog.setLastpolicymaintstartDateTime(lastpolicyVersionDate);
		}
		try {
		   //update Batch Process status  after finishing the job
			batchProcessDAO.updateBatchProcessLog(batchProcessLog);
		     
			 LOGGER.info("  Batch Status {}", batchProcessLog.toString());
			
			 String errorMsg =(String) jobExecution.getExecutionContext().get("ErrorMesssage");
			 
			 if (errorMsg != null) {
                 LOGGER.info("  Batch Process Error:  {}", errorMsg);
             }
			 
	     } catch (Exception e) {              
	          throw new ApplicationException(ERR_MSG_UPDATE_BATCH_PROCESS_LOG, e);
	     }
	}
		
	/**
	 * Method to get the Batch Business Id Prefix
	 * 
	 * @return BatchBusinessIdPrefix
	 */
	public String getCodeForBatchBusinessId() {
		return getBatchBusinessIdPrefix();
	}

	/*
	 * This method returns Batch Business id prefix according to job name
	 * @return
	 */
	private String getBatchBusinessIdPrefix() {
		
		if (jobName.equals(RapConstants.RAP_RETRO_JOB)) {
			if (RapConstants.JOBPARAMETER_TYPE_RAP.equals(type)) {
				return RapConstants.RAP_PROCESS;
			} else if (RapConstants.JOBPARAMETER_TYPE_RAPSTAGE.equals(type)) {
				return RapConstants.RAP_PROCESS_STAGE;
			}
		}
		return null;
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
	 * @param jdbcTemplate the jdbcTemplate to set
	 */
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * 
	 * @param updatePendingRecordSql
	 */
	public void setUpdatePendingRecordSql(String updatePendingRecordSql) {
		this.updatePendingRecordSql = updatePendingRecordSql;
	}

	/**
	 * 
	 * @param selectMaxPolicyMaintStartDateTime
	 */
	public void setSelectMaxPolicyMaintStartDateTime(
			String selectMaxPolicyMaintStartDateTime) {
		this.selectMaxPolicyMaintStartDateTime = selectMaxPolicyMaintStartDateTime;
	}

	/**
	 * 
	 * @param blockConcurrentJobExecutionMap
	 */
	public void setBlockConcurrentJobExecutionMap(Map<String,List<String>> blockConcurrentJobExecutionMap) {
		this.blockConcurrentJobExecutionMap = blockConcurrentJobExecutionMap;
	}
}
