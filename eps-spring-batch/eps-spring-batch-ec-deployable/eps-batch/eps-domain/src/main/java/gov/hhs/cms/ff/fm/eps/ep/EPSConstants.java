/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep;

/**
 * @author girish.padmanabhan
 *
 */
public class EPSConstants {

	/**
	 * 
	 */
	public static final String Y = "Y";
	/**
	 * 
	 */
	public static final String N = "N";
	
	//Constants used in Validation Service
	/**
	 * 
	 */
	public static final String JOBPARAMETER_KEY_SOURCE = "source";
	/**
	 * 
	 */
	public static final String JOBPARAMETER_SOURCE_HUB = "hub";

	/**
	 * 
	 */
	public static final String JOBPARAMETER_SOURCE_FFM = "ffm";
	
	/**
	 * ERL Constants
	 */
	public static final String MARKLOGIC_REC_COUNT = "MARKLOGIC_REC_COUNT";
	public static final String MANIFEST_FILE = "MANIFEST_FILE";
	public static final String ERL_EXTRACT_FILE_PATH = "ERL_EXTRACT_FILE_PATH";
	public static final String BATCH_RUNCONTROL_ID = "BATCH_RUNCONTROL_ID";
	public static final String JOB_ID = "JOBID";
	public static final String JOB_STATUS = "JOBSTATUS";
	public static final String JOB_STATUS_SUCCESS = "SUCCESS";
	public static final String BEGIN_HIGH_WATER_MARK = "BEGINHIGHWATERMARK";
	public static final String END_HIGH_WATER_MARK = "ENDHIGHWATERMARK";
	public static final String JOB_START_TIME = "JOBSTARTTIME";
	public static final String JOB_END_TIME = "JOBENDTIME";
	public static final String RECORD_COUNT = "RECORDCOUNT";
	public static final String PAETCOMPLETION = "PAETCOMPLETION";
	public static final String STATES_CLAUSE = "StatesClause";
	public static final String SQL_PARAMETER = "?";
	public static final String JOBPARAMETER_ERL_JOB_TYPE = "jobType";
	public static final String JOB_TYPE_PROCESSOR = "processor";
	public static final String CONTINUE_INGEST = "CONTINUE_INGEST";

	//Error Code and Description related constants
	public static final String ERCODE_INCORRECT_DATA = "E050";
	public static final String E050_DESC="Incorrect data";
	public static final String ERCODE_EPROD_13 = "EPROD-13";
	
}
