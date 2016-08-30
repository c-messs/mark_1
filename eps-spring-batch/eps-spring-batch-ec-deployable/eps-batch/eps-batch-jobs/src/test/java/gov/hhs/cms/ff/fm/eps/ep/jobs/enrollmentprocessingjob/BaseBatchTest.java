package gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob;

import gov.cms.dsh.bem.BenefitEnrollmentMaintenanceType;
import gov.cms.dsh.bem.BooleanIndicatorSimpleType;
import gov.cms.dsh.bem.HealthCoveragePolicyNumberType;
import gov.cms.dsh.bem.HealthCoverageType;
import gov.cms.dsh.bem.MemberRelatedInfoType;
import gov.cms.dsh.bem.MemberType;
import gov.cms.dsh.bem.PolicyInfoType;
import gov.cms.dsh.bem.TransactionInformationType;
import gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentMaintenanceDTO;
import gov.hhs.cms.ff.fm.eps.ep.enums.PolicyStatus;
import gov.hhs.cms.ff.fm.eps.ep.enums.ProcessedToDbInd;
import gov.hhs.cms.ff.fm.eps.ep.enums.TxnMessageDirectionType;
import gov.hhs.cms.ff.fm.eps.ep.enums.TxnMessageType;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.jdbc.core.JdbcTemplate;


public abstract class BaseBatchTest extends TestCase {

	private static final Logger LOG = LoggerFactory.getLogger(BaseBatchTest.class);
	
	protected Long JOB_ID = -9999990L;
	protected final LocalDate DATE = LocalDate.now();
	protected final LocalDateTime DATETIME = LocalDateTime.now();
	protected final int YEAR = DATE.getYear();
	protected final SimpleDateFormat sdf = new SimpleDateFormat("'D'yyMMdd'.T'HHmmssSSS");
	protected final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("'D'yyMMdd'.T'HHmmssSSS");
	protected final DateFormatter dateFormatter = new DateFormatter("yyyy-MM-dd HH:mm:ss.SSS");
	
	protected final LocalDate JAN_1 = LocalDate.of(YEAR, 1, 1);
	protected final LocalDate JAN_15 = LocalDate.of(YEAR, 1, 15);
	protected final LocalDate JAN_31 = LocalDate.of(YEAR, 1, 31);
	protected final LocalDate FEB_1 = LocalDate.of(YEAR, 2, 1);
	protected final LocalDate FEB_MAX = DATE.with(TemporalAdjusters.lastDayOfMonth());
	protected final LocalDate MAR_1 = LocalDate.of(YEAR, 3, 1);
	protected final LocalDate MAR_31 = LocalDate.of(YEAR, 3, 31);
	protected final LocalDate APR_1 = LocalDate.of(YEAR, 4, 1);
	
	protected final LocalDateTime JAN_1_1am = LocalDateTime.of(YEAR, 1, 1, 1, 0, 0, 111111000);
	protected final LocalDateTime FEB_1_2am = LocalDateTime.of(YEAR, 2, 1, 2, 0, 0, 222222000);
	protected final LocalDateTime MAR_1_3am = LocalDateTime.of(YEAR, 3, 1, 3, 0, 0, 333333000);
	protected final LocalDateTime APR_1_4am = LocalDateTime.of(YEAR, 4, 1, 4, 0, 0, 444444000);
	protected final LocalDateTime JUN_1_1am = LocalDateTime.of(YEAR, 6, 1, 1, 0, 0, 666666000);

	protected final String SUBSCRIBER_STATE_CD = "AA";
	protected final String ISSUER_STATE_CD = "VA";
	protected final String EXPOLID_PREF = "EXPOLID-";
	protected final String INSRNC_APLCTN_TYPE_CD = "1";

	@Autowired
	private JdbcTemplate jdbc;

	@Value("${unitTest.root.erl}")
	protected String ROOT_FILE_PATH;

	@Value("${eps.manifestfolder.erl}")
	protected File manifestDir;	

	@Value("${eps.inputfolder.erl}")
	protected File inputDir;

	@Value("${eps.privatefolder.erl}")
	protected File privateDir;	

	@Value("${eps.processedfolder.erl}")
	protected File processedDir;	

	@Value("${eps.skippedfolder.erl}")
	protected File skippedDir;	

	@Value("${eps.invalidfolder.erl}")
	protected File invalidDir;

	@Value("${eps.manifestarchive.erl}")
	protected File archiveDir;

	@Override
	public void setUp() throws Exception {

	}

	@Override
	public void tearDown() throws Exception {

	}

	protected void makeERLDirectories() {

		manifestDir.mkdirs();
		inputDir.mkdirs();
		privateDir.mkdirs();
		processedDir.mkdirs();
		skippedDir.mkdirs();
		invalidDir.mkdirs();
		archiveDir.mkdirs();
	}

	protected void assertJobCompleted(JobExecution jobEx) {

		Long jobId = jobEx.getJobId();
		String jobType = jobEx.getJobParameters().getString("type");
		assertEquals("Exit Status for " + jobType + " jobId " + jobId.toString() + " is COMPLETED", ExitStatus.COMPLETED, jobEx.getExitStatus());
		assertEquals("Batch Status for " + jobType+ " value is COMPLETED", BatchStatus.COMPLETED, jobEx.getStatus());
	}

	/**
	 * Fails if expected manifest file was not moved and not present in "archived" directory.
	 * @param expectedList
	 */
	protected void assertArchiveFileList(List<String> expectedList, int expectedCount) {

		String[] actualList = archiveDir.list();

		assertEquals("Number of archived manifest files", expectedCount, actualList.length);

		for (int i = 0; i < actualList.length; ++i) {

			String actualManifestNumber = actualList[i].substring(9, actualList[i].lastIndexOf(".txt"));
			assertTrue(i + ") Archived File (Manifest Number): " + actualList[i] + " is contained in the list of "
					+ "expected manifest numbers.", expectedList.contains(actualManifestNumber)); 
		}
	}

	protected void assertInputDirectory(List<String> expectedList) {

		List<String> actualList = new ArrayList<String>();

		for (File file : inputDir.listFiles()) {
			if (file.isDirectory()) {
				actualList.add(file.getName());
			}
		}
		assertEquals("Number of archived manifest files", expectedList.size(), actualList.size());

		for (int i = 0; i < actualList.size(); ++i) {

			assertTrue(i + ") Input Directory (Manifest Number): '" + actualList.get(i) + "' is contained in the list of "
					+ "expected manifest directory numbers.", expectedList.contains(actualList.get(i))); 
		}
	}

	protected void assertMemberVersionCount(Long jobId, int expectedCount) {

		String sql = "SELECT COUNT (*) FROM POLICYMEMBERVERSION WHERE CREATEBY = '" + jobId + "'";
		Integer actualCount = getJdbc().queryForObject(sql, Integer.class);
		assertEquals("Policy Member Versions", expectedCount, actualCount.intValue());
	}

	protected void assertPolicyVersionCount(Long jobId, int expectedCount) {

		String sql = "SELECT COUNT (*) FROM POLICYVERSION WHERE CREATEBY = '" + jobId + "'";
		Integer actualCount = getJdbc().queryForObject(sql, Integer.class);
		assertEquals("Policy Versions", expectedCount, actualCount.intValue());
	}


	protected BenefitEnrollmentMaintenanceType makeBem(String versionNum, LocalDateTime versionDt, PolicyStatus policyStatus, String exchangePolicyId) {

		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setTransactionInformation(new TransactionInformationType());
		bem.getTransactionInformation().setPolicySnapshotVersionNumber(versionNum);
		bem.getTransactionInformation().setPolicySnapshotDateTime(DateTimeUtil.getXMLGregorianCalendar(versionDt));
		bem.getTransactionInformation().setCurrentTimeStamp(DateTimeUtil.getXMLGregorianCalendar(versionDt));

		bem.setPolicyInfo(new PolicyInfoType());
		bem.getPolicyInfo().setPolicyStatus(policyStatus.getValue());
		bem.getPolicyInfo().setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(versionDt));
		bem.getPolicyInfo().setMarketplaceGroupPolicyIdentifier("MPGPID");
		bem.getPolicyInfo().setGroupPolicyNumber(exchangePolicyId);
		
		return bem;
	}

	protected BenefitEnrollmentMaintenanceDTO makeBemDTO(Long batchId, Long transMsgId, String versionNum, LocalDateTime versionDt, PolicyStatus policyStatus, String exchangePolicyId) {

		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();

		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setTransactionInformation(new TransactionInformationType());
		bem.getTransactionInformation().setPolicySnapshotVersionNumber(versionNum);
		bem.getTransactionInformation().setPolicySnapshotDateTime(DateTimeUtil.getXMLGregorianCalendar(versionDt));
		
		bem.setPolicyInfo(new PolicyInfoType());
		bem.getPolicyInfo().setPolicyStatus(policyStatus.getValue());
		bem.getPolicyInfo().setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(versionDt));
		bem.getPolicyInfo().setMarketplaceGroupPolicyIdentifier("MPGPID");
		bem.getPolicyInfo().setGroupPolicyNumber(exchangePolicyId);
		
		bemDTO.setBem(bem);

		bemDTO.setTxnMessageDirectionType(TxnMessageDirectionType.INBOUND);
		bemDTO.setTxnMessageType(TxnMessageType.MSG_834);
		bemDTO.setBatchId(batchId);
		bemDTO.setTransMsgId(transMsgId);
		return bemDTO;
	} 

	protected MemberType makeSubscriber(String stateCd, String exchangePolicyId, String hiosId, DateTime effStart) {

		MemberType member = new MemberType();
		member.setMemberInformation(new MemberRelatedInfoType());
		member.getMemberInformation().setSubscriberIndicator(BooleanIndicatorSimpleType.Y);
		
		String contractCode = hiosId.concat(stateCd).concat(exchangePolicyId);

		HealthCoverageType hcType = new HealthCoverageType();
		hcType.setHealthCoveragePolicyNumber(new HealthCoveragePolicyNumberType());
		hcType.getHealthCoveragePolicyNumber().setContractCode(contractCode);
		member.getHealthCoverage().add(hcType);
		return member;
	}

	protected void clearDailyBEMIndexer() {
		jdbc.execute("DELETE FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM'");
	}



	protected Long insertTransMsgFileInfo(Long batchId) {

		Long transMsgFileInfoId = jdbc.queryForObject("SELECT TRANSMSGFILEINFOSEQ.NEXTVAL FROM DUAL", Long.class);
		jdbc.execute("INSERT INTO TRANSMSGFILEINFO (TRANSMSGFILEINFOID, FILEINFOXML, CREATEDATETIME, LASTMODIFIEDDATETIME, CREATEBY) " +
				"VALUES (" + transMsgFileInfoId + ", XMLType('<Test>Data for TransMsgFileInfoId: " + transMsgFileInfoId +
				"</Test>'), SYSDATE, SYSDATE, '" + batchId + "')");
		return transMsgFileInfoId;
	}

	protected Long insertTransMsgFileInfo(Long batchId, String exchangeType) {

		Long transMsgFileInfoId = jdbc.queryForObject("SELECT TRANSMSGFILEINFOSEQ.NEXTVAL FROM DUAL", Long.class);
		jdbc.execute("INSERT INTO TRANSMSGFILEINFO (TRANSMSGFILEINFOID, FILEINFOXML, GROUPSENDERID, CREATEDATETIME, LASTMODIFIEDDATETIME, CREATEBY, TRANSMSGORIGINTYPECD) " +
				"VALUES (" + transMsgFileInfoId + ", XMLType('<Test>Data for TransMsgFileInfoId: " + transMsgFileInfoId +
				"</Test>'), 'PLANID', SYSDATE, SYSDATE, '" + batchId + "','" + exchangeType + "')");
		return transMsgFileInfoId;
	}

	protected Long insertTransMsgFileInfo(Long batchId, String exchangeType, String fileInfo) {

		Long transMsgFileInfoId = jdbc.queryForObject("SELECT TRANSMSGFILEINFOSEQ.NEXTVAL FROM DUAL", Long.class);
		jdbc.execute("INSERT INTO TRANSMSGFILEINFO (TRANSMSGFILEINFOID, FILEINFOXML, GROUPSENDERID, CREATEDATETIME, LASTMODIFIEDDATETIME, CREATEBY, TRANSMSGORIGINTYPECD) " +
				"VALUES (" + transMsgFileInfoId + ", XMLType('" + fileInfo +
				"'), 'PLANID', SYSDATE, SYSDATE, '" + batchId + "','" + exchangeType + "')");
		return transMsgFileInfoId;
	}

	protected Long insertBemTransMsg(Long batchId, String exchangeType, String bemXml) {

		Long transMsgFileInfoId = insertTransMsgFileInfo(batchId, exchangeType);
		return insertBemTransMsg(batchId, TxnMessageDirectionType.INBOUND.getValue(), TxnMessageType.MSG_834.getValue(), transMsgFileInfoId, null, bemXml);
	}

	protected Long insertBemTransMsg(Long batchId, String exchangeType, String bemXml, String fileInfo) {

		Long transMsgFileInfoId = insertTransMsgFileInfo(batchId, exchangeType, fileInfo);
		return insertBemTransMsg(batchId, TxnMessageDirectionType.INBOUND.getValue(), TxnMessageType.MSG_834.getValue(), transMsgFileInfoId, null, bemXml);
	}

	protected Long insertTransMsg(Long batchId, String exchangeType) {

		Long transMsgFileInfoId = insertTransMsgFileInfo(batchId, exchangeType);
		return insertTransMsg(batchId, TxnMessageDirectionType.INBOUND.getValue(), TxnMessageType.MSG_834.getValue(), transMsgFileInfoId);
	}

	protected Long insertTransMsg(Long batchId) {

		Long transMsgFileInfoId = insertTransMsgFileInfo(batchId);
		return insertTransMsg(batchId, TxnMessageDirectionType.INBOUND.getValue(), TxnMessageType.MSG_834.getValue(), transMsgFileInfoId);
	}

	protected Long insertTransMsg(Long batchId, String dirType, String msgType) {

		return insertTransMsg(batchId, dirType, msgType, null);
	}

	protected Long insertTransMsg(Long batchId, String dirType, String msgType, Long transMsgFileInfoId) {

		return insertTransMsg(batchId, TxnMessageDirectionType.INBOUND.getValue(), TxnMessageType.MSG_834.getValue(), transMsgFileInfoId, null);
	}

	protected Long insertTransMsg(Long batchId, String dirType, String msgType, Long transMsgFileInfoId, Long parentId) {

		Long transMsgId = jdbc.queryForObject("SELECT TRANSMSGSEQ.NEXTVAL FROM DUAL", Long.class);
		String bemXmlStr = "<BenefitEnrollmentMaintenance xmlns="+"\"http://bem.dsh.cms.gov\">Test Data for TransMsgId: " + transMsgId +"</BenefitEnrollmentMaintenance>";

		jdbc.execute("INSERT INTO TRANSMSG (TRANSMSGID, TRANSMSGFILEINFOID, PARENTTRANSMSGID, MSG, " +
				"TRANSMSGDIRECTIONTYPECD, TRANSMSGTYPECD, CREATEBY) " +
				"VALUES (" + transMsgId + ", " + transMsgFileInfoId + ", " + parentId + ", XMLType('" + 
				bemXmlStr +"'), '" + dirType + "', '" + msgType + "'," + batchId + ")");
		return transMsgId;
	}

	protected Long insertBemTransMsg(Long batchId, String dirType, String msgType, Long transMsgFileInfoId, Long parentId, String bemXml) {

		Long transMsgId = jdbc.queryForObject("SELECT TRANSMSGSEQ.NEXTVAL FROM DUAL", Long.class);
		jdbc.execute("INSERT INTO TRANSMSG (TRANSMSGID, TRANSMSGFILEINFOID, PARENTTRANSMSGID, MSG, " +
				"TRANSMSGDIRECTIONTYPECD, TRANSMSGTYPECD, CREATEBY) " +
				"VALUES (" + transMsgId + ", " + transMsgFileInfoId + ", " + parentId + ", XMLType('" + bemXml + "'), '" + 
				dirType + "', '" + msgType + "'," + batchId + ")");
		return transMsgId;
	}

	protected void insertBatchTransMsg(Long transMsgId, Long batchId) {

		insertBatchTransMsg(transMsgId, batchId, null, null, null, null, null, null, null, null);
	}

	protected void insertBatchTransMsg(Long transMsgId, Long batchId, ProcessedToDbInd ind) {

		insertBatchTransMsg(transMsgId, batchId, ind, null, null, null, null, null, null, null);
	}

	protected void insertBatchTransMsg(Long transMsgId, Long batchId, ProcessedToDbInd ind, String stateCd, 
			String exchangePolicyId, String hiosId, String srcVerNum, LocalDateTime srcVerDt) {
		insertBatchTransMsg(transMsgId, batchId, ind, stateCd, exchangePolicyId, hiosId, srcVerNum, srcVerDt, null, null);
	}

	protected void insertBatchTransMsg(Long transMsgId, Long batchId, ProcessedToDbInd ind, String stateCd, 
			String exchangePolicyId, String hiosId, String srcVerNum, LocalDateTime srcVerDt, String skipEPROD, String skipReason) {

		String args = "INSERT INTO BATCHTRANSMSG (TRANSMSGID, BATCHID, CREATEBY";
		String values = ") VALUES (" + transMsgId + ", " + batchId + ", " + batchId;

		if (ind != null) {
			args += ", PROCESSEDTODBSTATUSTYPECD";
			values += ", '" + ind.getValue() + "'";
		}
		if (stateCd != null) {
			args += ", SUBSCRIBERSTATECD";
			values += ", '" + stateCd + "'";
		}
		if (exchangePolicyId != null) {
			args += ", EXCHANGEPOLICYID";
			values += ", '" + exchangePolicyId + "'";
		}
		if (hiosId != null) {
			args += ", ISSUERHIOSID";
			values += ", '" + hiosId + "'";
		}
		if (srcVerNum != null) {
			args += ", SOURCEVERSIONID";
			values += ", " + srcVerNum;
		}
		if (srcVerDt != null) {
			args += ", SOURCEVERSIONDATETIME";
			values += ", TO_TIMESTAMP('" + convertToDate(srcVerDt) + "', 'YYYY-MM-DD HH24:MI:SS.FF')";
		}
		if (skipEPROD != null) {
			args += ", TRANSMSGSKIPREASONTYPECD";
			values += ", '" + skipEPROD + "'";
		}
		if (skipReason != null) {
			args += ", TRANSMSGSKIPREASONDESC";
			values += ", '" + skipReason + "'";
		}
		String sql = args + values + ")";
		jdbc.execute(sql);
	}

	protected void insertDailyBemIndexer(Long transMsgId, String bemXml, String fileNm, LocalDateTime fileDt) {

		String sql = "INSERT INTO DAILYBEMINDEXER (BEMINDEXID, INDEXDATETIME, TRANSMSGID, FILENM, FILENMDATETIME, EXCHANGETYPECD)" +
				" VALUES(BEMINDEXSEQ.nextval, current_timestamp, " + transMsgId + ", '" + fileNm + "', " +
				" TO_DATE('" + convertToDate(fileDt) + "'), 'FFM' )";
		jdbc.execute(sql);
	}

	protected void insertMinRecordToDailyBemIndexer(Long transMsgId) {

		String sql = "INSERT INTO DAILYBEMINDEXER (BEMINDEXID, INDEXDATETIME, TRANSMSGID, EXCHANGETYPECD,BATCHRUNCONTROLID)" +
				" VALUES(BEMINDEXSEQ.nextval, current_timestamp, " + transMsgId + ", 'FFM','JUNIT' )";
		jdbc.execute(sql);
	}
	
	/**
	 * Converts String date from BER file name to SQL date
	 * @param fileName
	 * @return
	 */
	public static LocalDateTime getFileNameDateTime(String fileName) {
		
		Pattern p = Pattern.compile("\\w{1,10}\\.\\w{1,10}\\.D(\\d{6})\\.T(\\d{9})(\\.\\w?+)?(\\.\\w*+)?");
		Matcher m = p.matcher(fileName);
		if(m.find()) {
			String timeStr=m.group(2);
			String dateStr=m.group(1);
			String fileDate = dateStr+timeStr;
			DateTimeFormatter format = DateTimeFormatter.ofPattern("yyMMddHHmmssSSS");
			LocalDateTime fileNameDateTime = LocalDateTime.parse(fileDate, format);

			return fileNameDateTime;
		}
		LOG.warn("EPROD-18 Invalid file name: "+ fileName);
		return null;

	}


	private java.sql.Timestamp convertToDate(LocalDateTime localDateTime) {

		if(localDateTime != null) {
			return Timestamp.valueOf(localDateTime);
		}
		return null;
	}

	protected String makeFileName(DateTime fileDt) {

		String TPID = "9999999";
		String APID = "DSH";
		String FUNC = "IC834";
		String fileNm = null;
		String fileNmPref = TPID + "." + APID + "." + FUNC + ".";
		fileNm = fileNmPref + sdf.format(fileDt.toDate());
		fileNm = fileNm + ".T";
		return fileNm;
	}

	protected String makeFileNameERL(LocalDateTime fileDt) {

		return "9999999999.IC834." + fileDt.format(dateTimeFormatter) + ".T.IN";	
	}


	public void deleteTestData(Long batchId) throws Exception {

		Resource resource = new ClassPathResource("sql/TEST_Delete_Data_by_JobId.sql");
		File sqlScript = resource.getFile();
		String sql = null;

		BufferedReader br = new BufferedReader(new FileReader(sqlScript));
		try {
			String line = br.readLine();
			while (line != null) {
				sql = line.replaceAll("\\?;", batchId.toString());
				jdbc.execute(sql);
				line = br.readLine();
			}
		} finally {
			br.close();
		}
	}

	protected void deleteSetUpData(Long batchId) {
		String sql = "DELETE FROM BATCHTRANSMSG WHERE BATCHID = " + batchId;
		jdbc.execute(sql);
		sql = "DELETE FROM TRANSMSG WHERE CREATEBY = '" + batchId + "'";
		jdbc.execute(sql);
		sql = "DELETE FROM TRANSMSGFILEINFO WHERE CREATEBY = '" + batchId + "'";
		jdbc.execute(sql);
		sql = "DELETE FROM DAILYBEMINDEXER WHERE EXCHANGETYPECD='FFM'";
		jdbc.execute(sql);
		sql = "DELETE FROM BATCHPROCESSLOG WHERE JOBID=" + batchId;
		jdbc.execute(sql);
	}

	protected void deleteBatchData(StepExecution stepExec, Long jobId) {

		String sql = "DELETE FROM BATCH_STEP_EXECUTION_CONTEXT WHERE STEP_EXECUTION_ID =" + stepExec.getId();
		jdbc.execute(sql);
		Long jobExecId = stepExec.getJobExecutionId();
		sql = "DELETE FROM BATCH_STEP_EXECUTION WHERE JOB_EXECUTION_ID =" + jobExecId;
		jdbc.execute(sql);
		sql = "DELETE FROM BATCH_JOB_EXECUTION_PARAMS WHERE JOB_EXECUTION_ID =" + jobExecId;
		jdbc.execute(sql);
		sql = "DELETE FROM BATCH_JOB_EXECUTION_CONTEXT WHERE JOB_EXECUTION_ID =" + jobExecId;
		jdbc.execute(sql);
		sql = "DELETE FROM BATCH_JOB_EXECUTION WHERE JOB_EXECUTION_ID =" + jobExecId;
		jdbc.execute(sql);
		sql = "DELETE FROM BATCH_JOB_INSTANCE WHERE JOB_INSTANCE_ID =" + jobId;
		jdbc.execute(sql);
	}



	protected String retrieveFileContent(File file) throws Exception {

		BufferedReader br = new BufferedReader(new FileReader(file));
		StringBuilder sb = new StringBuilder();
		try {
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				line = br.readLine();
			}
		} finally {
			br.close();
		}

		return sb.toString();   
	}

	/**
	 * Inserts a minimal record into POLICYVERSION
	 *
	 * @return policyVersionId
	 */
	protected Long insertPolicyVersion(Long transMsgId, LocalDateTime maintStart, LocalDate policyStart, LocalDate policyEnd, 
			String stateCd, String exchangePolicyId, String hios, String planId, Integer sourceVersionId) {

		Long policyVersionId = jdbc.queryForObject("SELECT POLICYVERSIONSEQ.NEXTVAL FROM DUAL", Long.class);

		String sql = "INSERT INTO POLICYVERSION (POLICYVERSIONID, MAINTENANCESTARTDATETIME, MAINTENANCEENDDATETIME, " +
				"SUBSCRIBERSTATECD, ISSUERPOLICYID, ISSUERHIOSID, ISSUERSUBSCRIBERID, EXCHANGEPOLICYID, " +
				"POLICYSTARTDATE, POLICYENDDATE, PLANID, X12INSRNCLINETYPECD, " +
				"INSRNCAPLCTNTYPECD, TRANSMSGID, SOURCEVERSIONID, " + getSysArgs() + ") " + 
				"VALUES ("+ policyVersionId + ", " + toTimestampValue(maintStart) + ", " +
				"TO_TIMESTAMP('9999-12-31 23:59:59.999', 'YYYY-MM-DD HH24:MI:SS.FF'), '" + stateCd + "', 'P"+policyVersionId+"', "+
				"'" + hios + "', 'SCB" + policyVersionId +"', '" + exchangePolicyId + "', " +
				toDateValue(policyStart) + ", " + toDateValue(policyEnd) + ", " +
				"'" + planId + "', 'HLT', '" + INSRNC_APLCTN_TYPE_CD +"', " + transMsgId + ", " + sourceVersionId + ", " + getSysValues() + ")";
		jdbc.execute(sql);
		return policyVersionId;
	}

	/**
	 * Inserts a minimal record into POLICYPAYMENTTRANS with parentPolPayTransId and issLevelTransId
	 */
	protected Long insertPolicyPaymentTrans(Long policyVersionId, LocalDate coverageDate, LocalDateTime maintStart, String hios, String transPeriodCd, 
			BigDecimal paymentAmt, BigDecimal totPrem, String insrnceAplTypeCd, String exchangePolId) {

		Long policyPaymentTransId = jdbc.queryForObject("SELECT POLICYPAYMENTTRANSSEQ.NEXTVAL FROM DUAL", Long.class);

		String sql = "INSERT INTO  POLICYPAYMENTTRANS (POLICYPAYMENTTRANSID, POLICYVERSIONID, " +
				"FINANCIALPROGRAMTYPECD, TRANSPERIODTYPECD, ISSUERHIOSID, PAYMENTAMOUNT, TOTALPREMIUMAMOUNT, ISSUERSTATECD, " +
				"MAINTENANCESTARTDATETIME, COVERAGEDATE, " + getSysArgs() + ", SUBSCRIBERSTATECD, EXCHANGEPOLICYID, INSRNCAPLCTNTYPECD ";
		sql += ") VALUES (" + policyPaymentTransId +", " + policyVersionId + ", '" + "APTC" + "', '" + 
				transPeriodCd + "', '" + hios + "', " + paymentAmt + ", " + totPrem + ", '" + ISSUER_STATE_CD +
				"', " + toTimestampValue(maintStart) +", "  + toDateValue(coverageDate) + ", " + getSysValues() + ", '" + 
				SUBSCRIBER_STATE_CD + "', '" + exchangePolId + "', '" + insrnceAplTypeCd + "')";
		jdbc.execute(sql);

		return policyPaymentTransId;
	}

	/**
	 * Inserts a minimal record into PAYMENTPROCESSINGSTATUS
	 */
	protected void insertPaymentProcessingStatus(Long policyPaymentTransId, String payProcStatusCd, LocalDateTime statusDateTime) {

		String sql = "INSERT INTO PAYMENTPROCESSINGSTATUS (POLICYPAYMENTTRANSID, TRANSACTIONLOGTYPECD, " +
				"PAYMENTPROCSTATUSTYPECD, STATUSDATETIME, " + getSysArgs() + ") VALUES (" + policyPaymentTransId + ", '" +
				"7" + "', '" + payProcStatusCd +"', " +
				toTimestampValue(statusDateTime) +", "+ getSysValues() + ")";
		jdbc.execute(sql);
	}


	protected void insertCoveragePeriodPaid(LocalDate coverage, LocalDate enrollRecCutoff, LocalDate initiation) {

		String sql = "INSERT INTO COVERAGEPERIODPAID (COVERAGEDATE, ENROLLMENTRECORDCUTOFFDATETIME, " + 
				"INITIATIONDATETIME, " + getSysArgs() + ") VALUES (" + toDateValue(coverage) + ", " +
				toDateValue(enrollRecCutoff) + ", " + toDateValue(initiation) + ", " + getSysValues() + ")";
		jdbc.execute(sql);

	}

	protected void deleteBatchProcessLog()  {

		jdbc.execute("DELETE FROM BATCHPROCESSLOG WHERE CREATEBY = '" + JOB_ID + "'");
	}

	protected void deleteCoveragePeriodPaid()  {

		jdbc.execute("DELETE FROM COVERAGEPERIODPAID WHERE CREATEBY = '" + JOB_ID + "'");
	}

	protected void insertBatchRunControl(String paetIndicator) {

		String batchRunControlInsertSql = "MERGE INTO BATCHRUNCONTROL USING dual ON (BATCHRUNCONTROLID='JUNIT')"
				+ " WHEN NOT MATCHED THEN INSERT (BATCHRUNCONTROLID, HIGHWATERMARKSTARTDATETIME, HIGHWATERMARKENDDATETIME, BATCHSTARTDATETIME, "
				+ " PREAUDITEXTRACTCOMPLETIONIND, CREATEDATETIME, LASTMODIFIEDDATETIME, CREATEBY, LASTMODIFIEDBY,INGESTCOMPLETEIND) "
				+ " VALUES("
				+ "'JUNIT', SYSTIMESTAMP, SYSTIMESTAMP, SYSTIMESTAMP, '"+  paetIndicator + "' , SYSTIMESTAMP, SYSTIMESTAMP, '" + JOB_ID + "','" + JOB_ID + "','Y')";

		jdbc.execute(batchRunControlInsertSql);
	}

	private String getSysArgs() {
		return "CREATEDATETIME, LASTMODIFIEDDATETIME, CREATEBY, LASTMODIFIEDBY";
	}

	private String toDateValue(LocalDate localDate) {
		return " TO_DATE('" + java.sql.Date.valueOf(localDate) + "', 'YYYY-MM-DD HH24:MI:SS')";
	}

	private String getSysValues() {
		return toTimestampValue(DATETIME) + ", " + toTimestampValue(DATETIME) +", '" + JOB_ID +"', '" + JOB_ID +"'";
	}

	private String toTimestampValue(LocalDateTime ts) {
		return "TO_TIMESTAMP('" + Timestamp.valueOf(ts) + "', 'YYYY-MM-DD HH24:MI:SS.FF3')";
	}


	/**
	 * @return the jdbc
	 */
	public JdbcTemplate getJdbc() {
		return jdbc;
	}

}
