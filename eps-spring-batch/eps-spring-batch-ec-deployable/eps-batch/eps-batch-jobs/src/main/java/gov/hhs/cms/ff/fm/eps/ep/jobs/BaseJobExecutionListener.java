package gov.hhs.cms.ff.fm.eps.ep.jobs;

import gov.hhs.cms.ff.fm.eps.rap.dao.BatchProcessDAO;
import gov.hhs.cms.ff.fm.eps.rap.domain.BatchProcessLog;
import gov.hhs.cms.ff.fm.eps.rap.domain.RapConstants;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

import com.accenture.foundation.common.exception.ApplicationException;
import com.accenture.foundation.common.exception.EnvironmentException;
import com.accenture.foundation.common.utilities.DateTimeUtils;
/**
 * This class serves as the Job Execution Listener for the RAP Job to
 * perform certain pre and post activities for an execution of the RAP Job.
 * 
 * @author eps
 *
 */
public class BaseJobExecutionListener implements JobExecutionListener {

	protected static final Logger LOGGER = LoggerFactory.getLogger(BaseJobExecutionListener.class);
	protected static final String ERR_MSG_UPDATE_BATCH_PROCESS_LOG = "Error occured while updating BatchProcessLog record ";
	protected static final String BATCH_BUSINESS_ID = "batchBusinessId";

	protected BatchProcessDAO batchProcessDAO;	
	protected List<String> blockConcurrentJobExecutionList;
	
	@Override
	public void afterJob(JobExecution jobExecution) {
		// updates batch log
		updateBatchStatus(jobExecution);
	}

	@Override
	public void beforeJob(JobExecution jobExecution) {
		LOGGER.info("job id: " + jobExecution.getJobId());
		// Check JOB Instance status .

        try {
            //Check JOB Instance status .
            String batchBusinessId = checkJobInstanceForBatchProcess();

            if (StringUtils.isNotBlank(batchBusinessId)) {
                 throw new ApplicationException(getErrorMsgForJobInstanceRunning(jobExecution, batchBusinessId));
            }
            
            //check if this job should not execute concurrently with other jobs
            List<BatchProcessLog> jobsCurrenltyRunning = getJobsCurrentRunning();
            if(!jobsCurrenltyRunning.isEmpty()) {            	
           		throw new ApplicationException(getErrorMsgForRunningJobs(jobsCurrenltyRunning));            	  
            }       		

       } catch (SQLException e) {
            throw new EnvironmentException(RapConstants.ERRORCODE_E9004+": "+"Job Instance check failed ", e);
       }
		
		// writes into batch log
		writeToBatchProcessLog(jobExecution);
	}
	
	/**
	 * This method writes batch process information to BatchProcessLog table.
	 * 
	 * @param jobExecution
	 */
	protected void writeToBatchProcessLog(JobExecution jobExecution) {
		 
		BatchProcessLog batchProcessLog = new BatchProcessLog();
		String batchBusinessId = createBatchBusinessId();		
		batchProcessLog.setBatchBusinessId(batchBusinessId);
		batchProcessLog.setJobId(jobExecution.getJobId());
		batchProcessLog.setJobNm(jobExecution.getJobInstance().getJobName());
		batchProcessLog.setJobStatusCd(jobExecution.getStatus().toString());
		batchProcessLog.setStartDateTime(new DateTime(jobExecution.getStartTime().getTime()));
		batchProcessLog.setJobParameterText(getJobParameterText());
		batchProcessLog.setRunByNm(getRunBy());
		// add batchBusinessId to context
		jobExecution.getExecutionContext().putString(BATCH_BUSINESS_ID, batchBusinessId);
 
		try {
		     //Insert Batch Process log before the starting the steps
			batchProcessDAO.insertBatchProcessLog(batchProcessLog);
			LOGGER.info("batchProcessLog: {}", batchProcessLog.toString());

		} catch (Exception e) {        
			throw new EnvironmentException("Job Parameters are invalid ", e);
		}
	}
	
	protected String getErrorMsgForJobInstanceRunning(JobExecution jobExecution, String batchBusinessId) {
		 return RapConstants.ERRORCODE_E9004+": "+jobExecution.getJobInstance().getJobName() + " Instance already running with 'STARTED' status. Please wait until "
				+ batchBusinessId+" completes execution. If "
               + jobExecution.getJobInstance().getJobName()
               + " was Terminated/Aborted earlier, then update JOBSTATUSCD TO 'ABANDONED' in BATCHPROCESSLOG table for " 
               + batchBusinessId + " to Ignore this issue.";
	}
	
	protected String getErrorMsgForRunningJobs(List<BatchProcessLog> currentlyRunningJobs)  {
		StringBuilder sb = new StringBuilder();
		sb.append(RapConstants.ERRORCODE_E9004);
		sb.append(": This job cannot proceed as following job instances are running and are in STARTED status,")
		.append(" please wait for these jobs to finish");
		
		for(BatchProcessLog item: currentlyRunningJobs) {
			sb.append("\nBatchBusinessId: "+item.getBatchBusinessId())
			.append("	Status: "+item.getJobStatusCd());
		}
		
		sb.append("\nIf any of these jobs are Aborted/Terminated, then update JOBSTATUSCD TO 'ABANDONED' in BATCHPROCESSLOG table ")
		.append("for the respective BatchBusinessId to Ignore this issue.\n");
		
		return sb.toString();
	}
	
	protected List<BatchProcessLog> getJobsCurrentRunning() {
		List<BatchProcessLog> currentlyRunningJobs = new ArrayList<BatchProcessLog>();
		
		if(CollectionUtils.isEmpty(blockConcurrentJobExecutionList)) {
			return currentlyRunningJobs;
		}		
		for(String item: blockConcurrentJobExecutionList) {
			String[] jobs = item.split(",");
			List<String> jobsList = new ArrayList<String>();
			CollectionUtils.addAll(jobsList, jobs);
			String jobNamesStr = CommonUtil.buildListToString(jobsList);
			LOGGER.info("jobNamesStr: ", jobNamesStr);
			
			List<BatchProcessLog> runningJobs = batchProcessDAO.getJobsWithStartedStatus(jobNamesStr);
			
			if(CollectionUtils.isNotEmpty(runningJobs)) {
				currentlyRunningJobs.addAll(runningJobs);
			}			
		}
		
		return currentlyRunningJobs;		
	}
	
	/**
	 * This method updates batch process information in BatchProcessLog table.
	 * 
	 * @param jobExecution
	 */
	protected void updateBatchStatus(JobExecution jobExecution) {
		 
		BatchProcessLog batchProcessLog = new BatchProcessLog();
		batchProcessLog.setBatchBusinessId(jobExecution.getExecutionContext().getString(BATCH_BUSINESS_ID));
		batchProcessLog.setJobId(jobExecution.getJobId());
		batchProcessLog.setJobNm(jobExecution.getJobInstance().getJobName());
		batchProcessLog.setJobStatusCd(jobExecution.getStatus().toString());
		batchProcessLog.setStartDateTime(new DateTime(jobExecution.getStartTime().getTime()));
		batchProcessLog.setEndDateTime(new DateTime(jobExecution.getEndTime().getTime()));
		batchProcessLog.setJobParameterText(getJobParameterText());
		batchProcessLog.setRunByNm(getRunBy());

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
	 * creates batch business id by using JOB prefix ,date & sequence number combination 
	 * @return
	 */
	protected String createBatchBusinessId() {
	     
		DateTime dateTime = DateTimeUtils.currentDateTime();		
		DateTimeFormatter dateFormat = DateTimeFormat.forPattern("YYYYMMdd");
		String searchStr = new StringBuilder().append(getCodeForBatchBusinessId())
									.append(dateFormat.print(dateTime)).toString();
		int seq = batchProcessDAO.getNextBatchBusinessIdSeq(searchStr);		
		if(seq > RapConstants.MAX_BUSINESS_ID_SEQ_NUM) {			
			throw new ApplicationException("Business Id number sequence reached to Maximum number: 999  for date(YYYYMMDD):"+ dateFormat.print(dateTime));
		}

		String retVal = searchStr + String.format(Locale.US, "%03d", seq);		
		LOGGER.info("BatchBusinessid:{}", retVal);
		
		return retVal;
	}	
	
	
	/**
	 * check if there is any existing job ( with 'STARTED' status)
	 * @return
	 * @throws SQLException
	 */
	public String checkJobInstanceForBatchProcess() throws SQLException{
         return  batchProcessDAO.getJobInstanceForBatchProcess(getCodeForBatchBusinessId());
	}

	/**
	 * Method to get the Batch Business Id Prefix
	 * 
	 * @return BatchBusinessIdPrefix
	 */
	public String getCodeForBatchBusinessId() {
		return StringUtils.EMPTY;
	}
	
	/**
	 * Override this method to return job parameters specific to the job. Caller can return 
	 * job parameters as string in key=value with comma delimiter format
	 * @return
	 */
	public String getJobParameterText() {
		return StringUtils.EMPTY;
	}

	/**
	 * This method should return the Job run user id/name.
	 * 
	 * @return  runBy name
	 */
	public String getRunBy() {
		return StringUtils.EMPTY;
	}

	/**
	 * @param batchProcessDAO the batchProcessDAO to set
	 */
	public void setBatchProcessDAO(BatchProcessDAO batchProcessDAO) {
		this.batchProcessDAO = batchProcessDAO;
	}    
	
	/**
	 * @param blockConcurrentJobExecutionList the blockConcurrentJobExecutionList to set
	 */
	public void setBlockConcurrentJobExecutionList(
			List<String> blockConcurrentJobExecutionList) {
		this.blockConcurrentJobExecutionList = blockConcurrentJobExecutionList;
	}  
	
}
