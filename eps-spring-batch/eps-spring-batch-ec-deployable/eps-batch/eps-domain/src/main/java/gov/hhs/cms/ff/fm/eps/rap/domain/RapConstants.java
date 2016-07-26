package gov.hhs.cms.ff.fm.eps.rap.domain;

/**
 * This class holds the constants used in RAP Batch
 * @author girish.padmanabhan
 *
 */
public class RapConstants {

	// job params keys
	public static final String RUNBY = "RunBy";
	public static final int MAX_BUSINESS_ID_SEQ_NUM = 999;
	public static final String JOB_EXECUTION_ID = "jobExecutionId";
	public static final String LASTPOLICYVERSIONDATE  = "LASTPVD";
	public static final String JOB_ID = "jobId";
	public static final String BATCH_BUSINESS_ID = "batchBusinessId";
		
	//APTC jobs.  -- BATCHBUSINESS ID PREFIX
	public static final String RAP_PROCESS = "RAPRETRO";
	public static final String RAP_PROCESS_STAGE = "RAPSTAGE";

	//APTC spring batch job-ids
	public static final String RAP_RETRO_JOB = "retroActivePaymentsJob";
	public static final String RAP_RETRO_STAGE_JOB = "retroActivePaymentsStageJob";
	
	/**
	 * 
	 */
	public static final String JOBPARAMETER_KEY_RAPJOB_TYPE = "type";
	
	/**
	 * 
	 */
	public static final String JOBPARAMETER_TYPE_RAP= "RAP";
	
	/**
	 * 
	 */
	public static final String JOBPARAMETER_TYPE_RAPSTAGE= "RAPSTAGE";
	
	
	//Logger name
	public static final String RAP_LOGGER = "RapLog";
	public static final String RAP_STAGE_LOGGER = "RapStageLog";
	
	//Error Codes
	public static final String ERRORCODE_E9004 = "E9004";
	
	public static final String ERR_MSG_UPDATE_BATCH_PROCESS_LOG = "Error occured while updating BatchProcessLog record ";
	
	//Financial Program Type Codes
	public static final String APTC = "APTC";
	public static final String CSR = "CSR";
	public static final String UF = "UF";

	//TransPeriodType codes
	public static final String TRNASPERIOD_PROSPECTIVE = "P";
	public static final String TRANSPERIOD_RETROACTIVE = "R";
	public static final String STATUS_PENDING_CYCLE = "PCYC";
	public static final String STATUS_REPLACED = "REPL";
	public static final String STATUS_PENDING_APPROVAL = "PAPPV";
	public static final String STATUS_APPROVED = "APPV";
	public static final String STATUS_NOISE = "NOISE";
	//Other constants
	public static final String COVERAGEPERIODPAID = "COVERAGEPERIODPAID";
	public static final String ERC = "ERC";
	public static final String HIGH_DATE = "9999-12-31";

	

}
