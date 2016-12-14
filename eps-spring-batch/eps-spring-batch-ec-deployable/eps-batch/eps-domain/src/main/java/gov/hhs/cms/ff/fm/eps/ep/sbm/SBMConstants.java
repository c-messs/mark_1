package gov.hhs.cms.ff.fm.eps.ep.sbm;

/**
 * 
 * @author rajesh.talanki
 *
 */
public class SBMConstants {
	
	/** Constant for Job Param Name.	 */
	public static final String JOBPARAMETER_PROCESSTYPE = "processingType";
	
	//job context parameter
	public static final String JOB_EXIT_CODE = "JOB_EXIT_CODE";
	//job context parameter values
	public static final String CONTINUE = "CONTINUE";
	public static final String EXIT = "EXIT";
	
	/** Constant for File suffix for .lock .	 */
	public static final String FILESUFFIX_LOCK = ".lock";
	public static final String ZIPD = "_ZIPD";
	public static final String GZIPD = "_GZIPD";
	public static final String FILENAME_ZIP_PATTERN = "'"+ ZIPD +"'yyyyMMdd'T'HHmmss";
	public static final String FILENAME_GZIP_PATTERN =  "'"+ GZIPD +"'yyyyMMdd'T'HHmmss";
	
	public static final String FUNCTION_CODE_SBMR = "SBMR";
	public static final String FUNCTION_CODE_SBMS = "SBMS";
	public static final String FUNCTION_CODE_SBMAR = "SBMAR";
	public static final String FUNCTION_CODE_SBMIS = "SBMIS";
	public static final String TARGET_EFT_APPLICATION_TYPE = "EPS";
	
	/**
	 * Though there are many environments, production (PROD) files will always be 'P'
	 */
	public static final String FILE_ENV_CD_PROD = "P";
	/**
	 * PROD-R files will always be 'R'
	 */
	public static final String FILE_ENV_CD_PROD_R = "R";
	/**
	 * ALL environments other than Production and PROD-R will be 'T' (dev, test, impl, etc.)
	 */
	public static final String FILE_ENV_CD_TEST = "T";
	
	public static final String JOBPARAMETER_PROCESSINGTYPE_SBMI = "sbmi";
	public static final String SBMI_JOB_NM = "sbmIngestionBatchJob";
	public static final String SBM_INGEST = "SBMINGEST";
	public static final String SBM_UPDATE_STATUS = "SBMUPDATESTATUS";
	public static final String SBM_EXECUTION_REPORT = "SBMEXECUTIONREPORT";
	
	public static final String CURRENT_PROCESSING_SUMMARY_ID = "currentProcessingSummaryId";
	
	public static final String ERROR_DESC_INCORRECT_VALUE = "incorrect value provided ";
	
	public static final String ERROR_INFO_EXPECTED_VALUE_TXT = "expected value: ";
	
	public static final String ERRORMSG_AMOUNT = "expected value: No more than 99999999.99";
	public static final String ERRORMSG_COVERAGEYEAR = "expected value: 2017-2999";
	public static final String ERRORMSG_EXPECTED_VALUE = "expected value: ";
	
	//groupIds reserved for certain actions
	public static final long GROUP_ID_EXTRACT = 1;
	
	//languageQualifierCode
	public static final String LANGUAGE_QUALIFIER_LD = "LD";
	public static final String LANGUAGE_QUALIFIER_LE = "LE";
	
	public static final String Y = "Y";
	public static final String N = "N";
	
	// Variant ID for CSR Calculation
	public static final String VARIANT_ID_00 = "00";
	public static final String VARIANT_ID_01 = "01";
	public static final String VARIANT_ID_02 = "02";
	public static final String VARIANT_ID_03 = "03";
	public static final String VARIANT_ID_04 = "04";
	public static final String VARIANT_ID_05 = "05";
	public static final String VARIANT_ID_06 = "06";
	
	//SBM Update Status
	public static final String STATUS_APPROVE = "Approve";
	public static final String STATUS_DISAPPROVED = "Disapproved";
	public static final String STATUS_BYPASS_FREEZE = "Bypass Freeze";
	public static final String STATUS_BACKOUT = "Backout";
	
	//SBM Update Status Headers
	public static final String HDR_LINE_NUMBER = "Line Number";
	public static final String HDR_TENANT_ID = "TenantID";
	public static final String HDR_ISSUER_ID = "IssuerID";
	public static final String HDR_FILEID = "FileID";
	public static final String HDR_ISSUER_FILESET_ID = "IssuerFileSetID";
	public static final String HDR_STATUS = "Status";
	
	//Error report 
	public static final String RPT_DELIMITER = ",";
	public static final String RPT_LINE_DELIMITER =  "\n";
	public static final String PROCESS_NAME = "ProcessName";
	public static final String PROCESS_NAME_VAL = "CMS Approval/Disapproval/Bypass Freeze/File Reversal Error Report";
	public static final String INPUT_FILE_NAME = "InputFileName";
	public static final String BATCH_RUN_DATE = "BatchRunDate";
	public static final String NUM_OF_ERRORS = "NumberOfErrors";
	public static final String ERR_RPT_HEADER = "LineNumber,ErrorCode,ErrorDescription,FileId,IssuerFileSetId\n";

}
