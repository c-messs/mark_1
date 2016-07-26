package gov.hhs.cms.ff.fm.eps.ep.jobs.retroactivepaymentsjob;

import gov.hhs.cms.ff.fm.eps.ep.enums.PolicyStatus;
import gov.hhs.cms.ff.fm.eps.ep.enums.TxnMessageDirectionType;
import gov.hhs.cms.ff.fm.eps.ep.enums.TxnMessageType;
import gov.hhs.cms.ff.fm.eps.rap.dao.mappers.PolicyPaymentsRowMapper;
import gov.hhs.cms.ff.fm.eps.rap.domain.PolicyPaymentTrans;
import gov.hhs.cms.ff.fm.eps.rap.domain.PolicyPremium;
import gov.hhs.cms.ff.fm.eps.rap.dto.PolicyDataDTO;
import gov.hhs.cms.ff.fm.eps.rap.dto.PolicyPaymentTransDTO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import junit.framework.TestCase;

import org.joda.time.DateTime;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.jdbc.core.JdbcTemplate;

public abstract class BaseRapBatchTest  extends TestCase {

	protected final DateTime DATETIME = new DateTime();
	
	/**
	 * DATETIME.withTimeAtStartOfDay() where time is 00:00:00
	 */
	protected final DateTime DATE = DATETIME.withTimeAtStartOfDay();

	protected final SimpleDateFormat sdf = new SimpleDateFormat("'D'yyMMdd'.T'HHmmssSSS");
	protected final DateFormatter sqlDf = new DateFormatter("yyyy-MM-dd HH:mm:ss.SSS");

	protected final int YEAR = DATETIME.getYear(); 

	protected final DateTime JAN_1 = new DateTime(YEAR, 1, 1, 0, 0);
	protected final DateTime JAN_15 = new DateTime(YEAR, 1, 15, 0, 0);
	protected final DateTime JAN_16 = new DateTime(YEAR, 1, 16, 0, 0);
	protected final DateTime JAN_20 = new DateTime(YEAR, 1, 20, 0, 0);
	protected final DateTime JAN_28 = new DateTime(YEAR, 1, 28, 0, 0);
	protected final DateTime JAN_31 = new DateTime(YEAR, 1, 31, 0, 0);
	protected final DateTime FEB_1 = new DateTime(YEAR, 2, 1, 0, 0);
	protected final DateTime FEB_11 = new DateTime(YEAR, 2, 11, 0, 0);
	protected final DateTime FEB_12 = new DateTime(YEAR, 2, 12, 0, 0);
	protected final DateTime FEB_13 = new DateTime(YEAR, 2, 13, 0, 0);
	protected final DateTime FEB_14 = new DateTime(YEAR, 2, 13, 0, 0);
	protected final DateTime FEB_MAX = new DateTime(YEAR, 2, FEB_1.dayOfMonth().getMaximumValue(), 0, 0);
	protected final DateTime MAR_1 = new DateTime(YEAR, 3, 1, 0, 0);
	protected final DateTime MAR_15 = new DateTime(YEAR, 3, 15, 0, 0);
	protected final DateTime MAR_31 = new DateTime(YEAR, 3, 31, 0, 0);
	protected final DateTime APR_1 = new DateTime(YEAR, 4, 1, 0, 0);
	protected final DateTime APR_10 = new DateTime(YEAR, 4, 10, 0, 0);
	protected final DateTime APR_11 = new DateTime(YEAR, 4, 11, 0, 0);
	protected final DateTime APR_30 = new DateTime(YEAR, 4, 30, 0, 0);
	protected final DateTime MAY_1 = new DateTime(YEAR, 5, 1, 0, 0);
	protected final DateTime JUN_1 = new DateTime(YEAR, 6, 1, 0, 0);
	protected final DateTime JUN_30 = new DateTime(YEAR, 6, 30, 0, 0);

	protected final DateTime DEC_31 = new DateTime(YEAR, 12, 31, 0, 0);

	protected final DateTime HIGHDATE = new DateTime(9999, 12, 31, 23, 59, 59, 999);

	public final String USER_PREFIX = "RapJobTest-";


	@Autowired
	protected JdbcTemplate jdbc;

	@Override
	public void setUp() throws Exception {

		
	}

	@Override
	public void tearDown() throws Exception {


	}


	protected Long insertPolicyVersionForRap(Long batchId, DateTime transDateTime, PolicyDataDTO pdDTO, List<PolicyPremium> epsPremiums, DateTime maintEnd, 
			PolicyStatus[] statuses, int policyCnt) {

		Long transMsgId = insertTransMsg(batchId);
		Long policyVersionId = insertPolicyVersion(batchId, transMsgId, pdDTO, maintEnd);
		// Insert all the previous statuses for each new policy. 
		DateTime uniqueTransDateTime = transDateTime;
		for (int i = 0; i <= policyCnt; ++i) {
			uniqueTransDateTime = uniqueTransDateTime.plusSeconds(10);
			insertPolicyStatus(batchId, uniqueTransDateTime, policyVersionId, statuses[i].getValue());
		}
		for (PolicyPremium premium : epsPremiums) {
			insertPolicyPremium(batchId, policyVersionId, premium);
		}
		return policyVersionId;
	}

	protected Long insertPolicyVersionForRap(Long batchId, DateTime transDateTime, PolicyDataDTO pdDTO, List<PolicyPremium> epsPremiums, DateTime maintEnd, 
			PolicyStatus status) {

		PolicyStatus[] statuses = new PolicyStatus[1];
		statuses[0] = status;
		return insertPolicyVersionForRap(batchId, transDateTime, pdDTO, epsPremiums, maintEnd, statuses, 0);
	}

	/**
	 * Inserts a minimal record into POLICYSTATUS
	 */
	private void insertPolicyStatus(Long batchId, DateTime transDateTime, Long policyVersionId, String statusCd) {

		String sql = "INSERT INTO POLICYSTATUS (POLICYVERSIONID, TRANSDATETIME, " +
				"INSURANACEPOLICYSTATUSTYPECD, " + getSysArgs() + ") VALUES (" + policyVersionId + ", " + 
				toTimestampValue(transDateTime) +", " + statusCd + ", " + makeSysValues(batchId) + ")";
		jdbc.execute(sql);
	}

	private void insertPolicyPremium(Long batchId, Long policyVersionId, PolicyPremium epsPremium) {

		String eedArg = (epsPremium.getEffectiveEndDate() != null) ? "EFFECTIVEENDDATE, " : "";

		String sql = "INSERT INTO POLICYPREMIUM (POLICYVERSIONID, EFFECTIVESTARTDATE, " + eedArg +
				"TOTALPREMIUMAMOUNT, CSRAMOUNT, APTCAMOUNT, PRORATEDPREMIUMAMOUNT, PRORATEDAPTCAMOUNT, " +
				"PRORATEDCSRAMOUNT, " + getSysArgs() + ") VALUES (" + policyVersionId + ", " + 
				toDateValue(epsPremium.getEffectiveStartDate()) + ", ";
		if (epsPremium.getEffectiveEndDate() != null) {
			sql +=  toDateValue(epsPremium.getEffectiveEndDate()) + ", ";
		}
		sql += epsPremium.getTotalPremiumAmount() + ", " + epsPremium.getCsrAmount() + ", " + 
				epsPremium.getAptcAmount() + ", " + epsPremium.getProratedPremiumAmount() + ", " +
				epsPremium.getProratedAptcAmount() + ", " + epsPremium.getProratedCsrAmount() + ", " +
				makeSysValues(batchId) + ")";
		jdbc.execute(sql);
	}

	private Long insertTransMsgFileInfo(Long batchId) {

		Long transMsgFileInfoId = jdbc.queryForObject("SELECT TRANSMSGFILEINFOSEQ.NEXTVAL FROM DUAL", Long.class);
		jdbc.execute("INSERT INTO TRANSMSGFILEINFO (TRANSMSGFILEINFOID, FILEINFOXML, " + getSysArgs() + ") " + 
				"VALUES (" + transMsgFileInfoId + ", XMLType('<Test>Data for TransMsgFileInfoId: " + transMsgFileInfoId +
				"</Test>'), " + makeSysValues(batchId) + ")");
		return transMsgFileInfoId;
	}

	private Long insertTransMsg(Long batchId) {

		Long transMsgFileInfoId = insertTransMsgFileInfo(batchId);
		return insertTransMsg(batchId, TxnMessageDirectionType.INBOUND.getValue(), TxnMessageType.MSG_834.getValue(), transMsgFileInfoId, null);
	}

	private Long insertTransMsg(Long batchId, String dirType, String msgType, Long transMsgFileInfoId, Long parentId) {

		Long transMsgId = jdbc.queryForObject("SELECT TRANSMSGSEQ.NEXTVAL FROM DUAL", Long.class);
		String bemXmlStr = "<BenefitEnrollmentMaintenance xmlns="+"\"http://bem.dsh.cms.gov\">Test Data for TransMsgId: " + transMsgId +"</BenefitEnrollmentMaintenance>";

		jdbc.execute("INSERT INTO TRANSMSG (TRANSMSGID, TRANSMSGFILEINFOID, PARENTTRANSMSGID, MSG, " +
				"TRANSMSGDIRECTIONTYPECD, TRANSMSGTYPECD, " + getSysArgs() + ") " + 
				"VALUES (" + transMsgId + ", " + transMsgFileInfoId + ", " + parentId + ", XMLType('" + 
				bemXmlStr +"'), '" + dirType + "', '" + msgType + "'," + makeSysValues(batchId) + ")");
		return transMsgId;
	}

	/**
	 * Inserts a minimal record into POLICYVERSION
	 *
	 * @return policyVersionId
	 */
	protected Long insertPolicyVersion(Long batchId, Long transMsgId, PolicyDataDTO pdDTO, DateTime maintEnd) {

		Long policyVersionId = jdbc.queryForObject("SELECT POLICYVERSIONSEQ.NEXTVAL FROM DUAL", Long.class);

		String sql = "INSERT INTO POLICYVERSION (POLICYVERSIONID, MAINTENANCESTARTDATETIME, MAINTENANCEENDDATETIME, " +
				"SUBSCRIBERSTATECD, ISSUERPOLICYID, ISSUERHIOSID, ISSUERSUBSCRIBERID, EXCHANGEPOLICYID, " +
				"POLICYSTARTDATE, POLICYENDDATE, PLANID, X12INSRNCLINETYPECD, " +
				"INSRNCAPLCTNTYPECD, TRANSMSGID, " + getSysArgs() + ") " + 
				"VALUES ("+ policyVersionId + ", " + toTimestampValue(pdDTO.getMaintenanceStartDateTime()) + ", " +
				toTimestampValue(maintEnd) + ", '" + pdDTO.getSubscriberStateCd() + "', 'P" + policyVersionId+"', "+
				"'" + pdDTO.getIssuerHiosId() + "', 'SCB" + policyVersionId +"', '" + pdDTO.getExchangePolicyId() + "', " +
				toDateValue(pdDTO.getPolicyStartDate()) + ", " + toDateValue(pdDTO.getPolicyEndDate()) + ", " +
				"'" + pdDTO.getPlanId() + "', 'HLT', '" + pdDTO.getInsrncAplctnTypeCd() +"', " + transMsgId + ", " + makeSysValues(batchId) + ")";
		jdbc.execute(sql);
		return policyVersionId;
	}


	/**
	 * Inserts a minimal record into APPROVEDISSUER
	 */
	protected void insertApprovedIssuer(Long batchId, String issuerId, DateTime esd) {

		String sql = "INSERT INTO APPROVEDISSUER (ISSUERID, EFFECTIVESTARTDATE, " + getSysArgs() + ") " +
				"VALUES ('" + issuerId + "', " + toDateValue(esd) +", " + makeSysValues(batchId) + ")";
		jdbc.execute(sql);
	}


	protected void insertCoveragePeriodPaid(Long batchId, DateTime coverage, DateTime enrollRecCutoff, DateTime initiation) {

		String sql = "INSERT INTO COVERAGEPERIODPAID (COVERAGEDATE, ENROLLMENTRECORDCUTOFFDATETIME, " + 
				"INITIATIONDATETIME, " + getSysArgs() + ") VALUES (" + toDateValue(coverage) + ", " +
				toDateValue(enrollRecCutoff) + ", " + toDateValue(initiation) + ", " + makeSysValues(batchId) + ")";
		jdbc.execute(sql);
	}


	/**
	 * Inserts a minimal parent record into BATCHPROCESSLOG.
	 */
	protected void insertBatchProcessLog(String whichJob, String batchBusinessId, Long batchId, DateTime start, BatchStatus jobStatusCd, DateTime lastPolMaintStart) {

		String sql  = "INSERT INTO BATCHPROCESSLOG (BATCHBUSINESSID, JOBID, JOBNM, STARTDATETIME, JOBSTATUSCD, LASTPOLICYMAINTSTARTDATETIME, " +
				getSysArgs() + ") VALUES ('" + batchBusinessId + "'," + batchId + ", '" + whichJob +"'," +
				toDateValue(start) + ", '" + jobStatusCd.name() + "'," + toDateValue(lastPolMaintStart) + ", " + makeSysValues(batchId) + ")";
		jdbc.execute(sql);
	}


	/**
	 * Inserts a minimal record into ISSUERUSERFEERATE.
	 */
	protected void insertIssuerUserFeeRate(Long batchId, String state, String insrnPlnTypeCd, int coverageYear, BigDecimal flatRate,  DateTime startDate, DateTime endDate)  {

		jdbc.execute("INSERT INTO ISSUERUSERFEERATE(ISSUERUFSTATECD, INSRNCAPLCTNTYPECD, " +
				"ISSUERUFCOVERAGEYEAR, ISSUERUFFLATRATE, ISSUERUFSTARTDATE, ISSUERUFENDDATE, " + getSysArgs() + ") " +
				"VALUES ('" + state + "', '" + insrnPlnTypeCd + "'" + 
				", '" + coverageYear + "', " + flatRate + ", " + toDateValue(startDate) + ", " +  toDateValue(endDate) + ", " +
				makeSysValues(batchId) + ")");
	}


	/**
	 * Inserts a minimal record into POLICYPAYMENTTRANS
	 */
	protected Long insertPolicyPaymentTrans(Long batchId, PolicyPaymentTrans ppt) {

		Long policyPaymentTransId = jdbc.queryForObject("SELECT POLICYPAYMENTTRANSSEQ.NEXTVAL FROM DUAL", Long.class);

		String sql = "INSERT INTO  POLICYPAYMENTTRANS (POLICYPAYMENTTRANSID, POLICYVERSIONID, " +
				"FINANCIALPROGRAMTYPECD, TRANSPERIODTYPECD, ISSUERHIOSID, PAYMENTAMOUNT, TOTALPREMIUMAMOUNT, ISSUERSTATECD, " +
				"MAINTENANCESTARTDATETIME, COVERAGEDATE, SUBSCRIBERSTATECD, EXCHANGEPOLICYID, " + 
				"PAYMENTCOVERAGESTARTDATE, PAYMENTCOVERAGEENDDATE, LASTPAYMENTPROCSTATUSTYPECD, " + getSysArgsWithDateTime() + ") " +
				"VALUES (" + policyPaymentTransId +", " + ppt.getPolicyVersionId() + ", '" + ppt.getFinancialProgramTypeCd() + "', '" + 
				ppt.getTransPeriodTypeCd() + "', '" + ppt.getIssuerHiosId() + "', " + ppt.getPaymentAmount() + ", " + 
				ppt.getTotalPremiumAmount() + ", '" + ppt.getIssuerStateCd() + "', " + toDateValue(ppt.getMaintenanceStartDateTime()) + ", " +
				toDateValue(ppt.getCoverageDate()) + ", '" + ppt.getSubscriberStateCd() + "', '" + ppt.getExchangePolicyId() + "', " + 
				toDateValue(ppt.getPaymentCoverageStartDate()) + ", " + toDateValue(ppt.getPaymentCoverageEndDate()) + ", '" + ppt.getLastPaymentProcStatusTypeCd() + "', " +
				makeSysValues(batchId) + ", " + toDateValue(new DateTime()) + ", " + toDateValue(new DateTime()) + ")";
		jdbc.execute(sql);

		return policyPaymentTransId;
	}


	protected void assertJobCompleted(JobExecution jobEx) {

		Long jobId = jobEx.getJobId();
		String jobType = jobEx.getJobParameters().getString("type");
		assertEquals("Exit Status for " + jobType + " jobId " + jobId.toString() + " is COMPLETED", ExitStatus.COMPLETED, jobEx.getExitStatus());
		assertEquals("Batch Status for " + jobType+ " value is COMPLETED", BatchStatus.COMPLETED, jobEx.getStatus());
	}


	protected void assertStagingRapIssuer(Long jobId, String hiosId, int expectedRecordCnt) {

		Long actualPvq = null;
		if (expectedRecordCnt > 0) {
			String sql = "SELECT POLICYVERSIONQUANTITY FROM STAGINGRAPISSUER WHERE ISSUERHIOSID = '" + 
					hiosId + "' AND LOADJOBID = " + jobId.toString(); 
			try {
				actualPvq = jdbc.queryForObject(sql, Long.class);
			} finally {
				int actualCnt = (actualPvq != null) ? actualPvq.intValue() : 0;
				assertEquals("IssuerHiosId " + hiosId + ", POLICYVERSIONQUANTITY", expectedRecordCnt, actualCnt);
			}
		} else {
			String sql = "SELECT COUNT(*) FROM STAGINGRAPISSUER WHERE ISSUERHIOSID = '" +
					hiosId + "' AND LOADJOBID = " + jobId.toString();
			Double actualCount = jdbc.queryForObject(sql, Double.class);
			assertEquals("No record inserted for ISSUERHIOSID=" + hiosId,  expectedRecordCnt, actualCount.intValue());
		}
	}

	protected void assertPolicyPaymentTrans(String batchBusinessId, List<PolicyPaymentTransDTO> expectedPaymentList) {

		String sql = "SELECT * FROM POLICYPAYMENTTRANS WHERE LASTMODIFIEDBY = '" + batchBusinessId + "' ORDER BY POLICYPAYMENTTRANSID ASC";
		List<PolicyPaymentTransDTO> actualList = jdbc.query(sql, new PolicyPaymentsRowMapper());

		assertTrue("POLICYPAYMENTTRANS records are not empty", !actualList.isEmpty());
		assertEquals("EXPECTED number of POLICYPAYMENTTRANS records versus ACTUAL number.", expectedPaymentList.size(), actualList.size());

		String msg = "POLICYPAYMENTTRANS Record ";

		int i = 0;
		for (PolicyPaymentTransDTO actual : actualList) {

			PolicyPaymentTransDTO expectedPayment = expectedPaymentList.get(i++);

			assertEquals(msg + i + ": PolicyVersionId", expectedPayment.getPolicyVersionId(), actual.getPolicyVersionId());
			assertEquals(msg + i + ": ExchangePolicyId", expectedPayment.getExchangePolicyId(), actual.getExchangePolicyId());
			assertEquals(msg + i + ": TransPeriodTypeCd", expectedPayment.getTransPeriodTypeCd(), actual.getTransPeriodTypeCd());
			assertEquals(msg + i + ": CoverageDate", expectedPayment.getCoverageDate(), actual.getCoverageDate());
			assertEquals(msg + i + ": FinancialProgramTypeCd", expectedPayment.getFinancialProgramTypeCd(), actual.getFinancialProgramTypeCd());
			assertEquals(msg + i + ": PaymentAmount", expectedPayment.getPaymentAmount(), actual.getPaymentAmount());
			assertEquals(msg + i + ": TotalPremiumAmount", expectedPayment.getTotalPremiumAmount(), actual.getTotalPremiumAmount());
			assertEquals(msg + i + ": LastPaymentProcStatusTypeCd", expectedPayment.getLastPaymentProcStatusTypeCd(), actual.getLastPaymentProcStatusTypeCd());
			assertEquals(msg + i + ": ParentPolicyPaymentTransId", expectedPayment.getParentPolicyPaymentTransId(), actual.getParentPolicyPaymentTransId());
		}
	}


	protected PolicyDataDTO makePolicyDataDTO(String hiosId, String exPolId, String state, DateTime policyStart, DateTime policyEnd, DateTime maintStart,
			PolicyStatus status) {

		PolicyDataDTO pdDTO = new PolicyDataDTO();

		pdDTO.setSubscriberStateCd(state);
		pdDTO.setIssuerHiosId(hiosId);
		String planId = hiosId + state + "0" + hiosId.substring(0, 4) + "01";
		pdDTO.setPlanId(planId);
		pdDTO.setInsrncAplctnTypeCd(status.getValue());
		pdDTO.setExchangePolicyId(exPolId.toString());
		pdDTO.setPolicyStartDate(policyStart);
		pdDTO.setPolicyEndDate(policyEnd);
		pdDTO.setMaintenanceStartDateTime(maintStart);

		return pdDTO;
	}


	protected PolicyPremium makePolicyPremium(DateTime esd, DateTime eed, BigDecimal aptc, BigDecimal tpa, BigDecimal csr) {

		PolicyPremium pp = new PolicyPremium();
		pp.setEffectiveStartDate(esd);
		pp.setEffectiveEndDate(eed);
		pp.setTotalPremiumAmount(tpa);
		pp.setAptcAmount(aptc);
		pp.setCsrAmount(csr);

		return pp;		
	}


	protected PolicyPaymentTrans makePolicyPaymentTrans(Long policyVersionId, String exPolId, DateTime initiation, String transPeriodTypeCd, 
			String status, String issuerHiosId, String state) {

		PolicyPaymentTrans ppt = new PolicyPaymentTrans();
		ppt.setPolicyVersionId(policyVersionId);
		ppt.setExchangePolicyId(exPolId);
		ppt.setTransPeriodTypeCd(transPeriodTypeCd);
		ppt.setMaintenanceStartDateTime(initiation);
		ppt.setLastPaymentProcStatusTypeCd(status);
		ppt.setIssuerHiosId(issuerHiosId);
		ppt.setIssuerStateCd(state);
		ppt.setSubscriberStateCd(state);
		return ppt;
	}

	protected PolicyPaymentTransDTO makePolicyPaymentTransDTO(Long policyVersionId, String exPolId, String transPeriodTypeCd, DateTime coverage,
			String finProgTypeCd, BigDecimal paymentAmount, BigDecimal tpa, String status, String issuerHiosId) {

		return makePolicyPaymentTransDTO(policyVersionId, exPolId, transPeriodTypeCd, coverage, finProgTypeCd, paymentAmount, 
				tpa, status, issuerHiosId, null);
	}

	protected PolicyPaymentTransDTO makePolicyPaymentTransDTO(Long policyVersionId, String exPolId, String transPeriodTypeCd, DateTime coverage,
			String finProgTypeCd, BigDecimal paymentAmount, BigDecimal tpa, String status, String issuerHiosId, Long parentPptId) {

		PolicyPaymentTransDTO pptDTO = new PolicyPaymentTransDTO();
		pptDTO.setPolicyVersionId(policyVersionId);
		pptDTO.setExchangePolicyId(exPolId);
		pptDTO.setTransPeriodTypeCd(transPeriodTypeCd);
		pptDTO.setCoverageDate(coverage);
		pptDTO.setFinancialProgramTypeCd(finProgTypeCd);
		pptDTO.setPaymentAmount(paymentAmount);
		pptDTO.setTotalPremiumAmount(tpa);
		pptDTO.setLastPaymentProcStatusTypeCd(status);
		pptDTO.setIssuerHiosId(issuerHiosId);
		pptDTO.setParentPolicyPaymentTransId(parentPptId);
		return pptDTO;
	}


	/**
	 * Deletes data created by job.
	 * @param batchBusinessId
	 * @throws Exception
	 */
	protected void deleteRAPTestData(String batchBusinessId) throws Exception {

		Resource resource = new ClassPathResource("sql/TEST_Delete_RAP_Data.sql");
		File sqlScript = resource.getFile();
		String sql = null;

		BufferedReader br = new BufferedReader(new FileReader(sqlScript));
		try {
			String line = br.readLine();
			while (line != null) {
				if (line.indexOf("?;") != -1) {
					sql = line.replaceAll("\\?;", "'" + batchBusinessId + "'");
				} else {
					sql = line;
				}
				jdbc.execute(sql);
				line = br.readLine();
			}
		} finally {
			br.close();
		}
	}

	protected void deleteStagingRapIssuer(Long jobId) {

		String sql = "DELETE FROM STAGINGRAPISSUER WHERE LOADJOBID = " + jobId;
		jdbc.execute(sql);		
	}

	protected void deleteBatchProcessLog(Long jobId) {

		String sql = "DELETE FROM BATCHPROCESSLOG WHERE JOBID = " + jobId;
		jdbc.execute(sql);
	}


	private String getSysArgs() {
		return "CREATEBY, LASTMODIFIEDBY";
	}

	private String getSysArgsWithDateTime() {
		return "CREATEBY, LASTMODIFIEDBY, CREATEDATETIME, LASTMODIFIEDDATETIME";
	}

	private String toDateValue(DateTime dt) {
		String sql = null;
		if (dt != null) {
			sql = " TO_DATE('" + getSqlDate(dt) + "', 'YYYY-MM-DD HH24:MI:SS')";
		}
		return sql;
	}
	
	private java.sql.Date getSqlDate(DateTime dateTime) {
		if(dateTime != null) {
			return new java.sql.Date(dateTime.getMillis());
		}
		return null;
	}

	private String toTimestampValue(DateTime ts) {

		return "TO_TIMESTAMP('" + sqlDf.print(ts.toDate(), Locale.US) + "', 'YYYY-MM-DD HH24:MI:SS.FF3')";
	}

	private String makeSysValues(Long batchId) {
		return " '" + USER_PREFIX + batchId +"', '" + USER_PREFIX + batchId +"'";
	}


	protected Long getRandomNumber(int digits) {

		double dblDigits = (double) digits;
		double min = Math.pow(10.0, dblDigits - 1);
		double max = Math.pow(10.0, dblDigits) - 1;
		int randNum = (int) Math.round(Math.random() * (max - min) + min);
		return Long.valueOf(randNum);
	}

	/**
	 * @return the jdbc
	 */
	public JdbcTemplate getJdbc() {
		return jdbc;
	}

}
