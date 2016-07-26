/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.rap.dao;

import gov.hhs.cms.ff.fm.eps.rap.domain.IssuerUserFeeRate;
import gov.hhs.cms.ff.fm.eps.rap.domain.PolicyPremium;
import gov.hhs.cms.ff.fm.eps.rap.dto.PolicyDataDTO;
import gov.hhs.cms.ff.fm.eps.rap.dto.PolicyDetailDTO;
import gov.hhs.cms.ff.fm.eps.rap.dto.PolicyPaymentTransDTO;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import junit.framework.TestCase;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author girish.padmanabhan
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={ "classpath:/rap-data-config.xml", "classpath:/test-context-data.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class RapDaoTest extends TestCase {

	protected final DateFormatter sqlDf = new DateFormatter("yyyy-MM-dd HH:mm:ss.SSS");

	protected final DateTime DATETIME = new DateTime();
	// Set as a future year as to not intervene with other test data.
	private final int YEAR = DATETIME.getYear() + 4;
	
	private final String user = "RapDaoTest";

	private final String ISSUER_STATE_CD = "VA";
	private final String SUBSCRIBER_STATE_CD = "NC";
	private final String EXPOLID_PREF = "EXPOLID-";

	private DateTime AUG_1 = new DateTime(YEAR, 8, 1, 0, 0);
	private DateTime SEP_1 = new DateTime(YEAR, 9, 1, 0, 0);
	private DateTime OCT_1 = new DateTime(YEAR, 10, 1, 0, 0);
	private DateTime NOV_1 = new DateTime(YEAR, 11, 1, 0, 0);
	private DateTime DEC_1 = new DateTime(YEAR, 12, 1, 0, 0);

	private List<String> expectedIssuerList;
	private List<String> expectedIssuerList2;
	
	@Autowired
	private RapDao rapDao;
	
	@Autowired
	JdbcTemplate jdbcTemplate;

	@Before
	public void setUp()  {

		expectedIssuerList = new ArrayList<String>();

		expectedIssuerList.add("99991");
		expectedIssuerList.add("99992");
		expectedIssuerList.add("99993");
		expectedIssuerList.add("99994");

		expectedIssuerList2 = new ArrayList<String>();	
		expectedIssuerList2.add("88881");
		expectedIssuerList2.add("88882");
		expectedIssuerList2.add("88883");
		expectedIssuerList2.add("88884");

	}
	
	@Test
	public void TestRetrievePolicyPaymentData() {
		
		DateTime coverageDate = OCT_1;
		DateTime policyEndDate = NOV_1;
		String expectedPayProcStatusCd = "PCYC";
		Long transMsgId = insertTransMsg();
		Long expectedPolicyVersionId = insertPolicyVersion(transMsgId);
		insertPolicyStatus(expectedPolicyVersionId, "3");
		Long expectedPolicyPaymentTransId = insertPolicyPaymentTrans(expectedPolicyVersionId, coverageDate, policyEndDate, "P", expectedPayProcStatusCd);
		insertPolicyPremium(expectedPolicyVersionId);
		String expectedExhchangePolicyId = EXPOLID_PREF + expectedPolicyVersionId;
		
		PolicyDataDTO policyDataDTO = new PolicyDataDTO();
		policyDataDTO.setPolicyVersionId(expectedPolicyVersionId);
		policyDataDTO.setSubscriberStateCd(SUBSCRIBER_STATE_CD);
		policyDataDTO.setExchangePolicyId(expectedExhchangePolicyId);
		policyDataDTO.setIssuerHiosId("101");
		policyDataDTO.setPolicyStartDate(AUG_1);
		policyDataDTO.setPolicyStartDate(DEC_1);
		
		PolicyDetailDTO policyDetailDTO = rapDao.retrievePolicyPaymentData(policyDataDTO);
		
		List<PolicyPaymentTransDTO> actualPptDTOList = policyDetailDTO.getPolicyPayments();
	
		assertEquals("PolicyPaymentTransDTO list size", 1, actualPptDTOList.size());
		PolicyPaymentTransDTO actualPptDTO = actualPptDTOList.get(0);
		
		assertEquals("Pmt Trans id", expectedPolicyPaymentTransId, actualPptDTO.getPolicyPaymentTransId());
		assertEquals("FinancialProgramTypeCd", "APTC", actualPptDTO.getFinancialProgramTypeCd());
		assertEquals("paymentProcStatusTypeCd", "PCYC", actualPptDTO.getLastPaymentProcStatusTypeCd());
		assertEquals("PolicyVersionId", expectedPolicyVersionId.longValue(), actualPptDTO.getPolicyVersionId().longValue());
		assertEquals("Subscriber State", SUBSCRIBER_STATE_CD, actualPptDTO.getSubscriberStateCd());
		
		List<PolicyPremium> actualPremiumList = policyDetailDTO.getPolicyPremiums();
		assertEquals("PolicyPremium list size", 1, actualPremiumList.size());
		PolicyPremium actualPremium = actualPremiumList.get(0);
		assertEquals("TPA", BigDecimal.valueOf(100), actualPremium.getTotalPremiumAmount());
		
	}
	
	/**
	 * Tests method getUserFeeRateForAllStates
	 * - Create 5 records, 2 in bounds of date range and 3 out of bounds of the query.
	 * - Confirms only the 2 within range records are retrieved.
	 * @throws SQLException
	 */
	@Test
	public void TestGetUserFeeRateForAllStates() {

		DateTime asOfDate = new DateTime(OCT_1);

		String expectedState = "TX";
		String expectedYear =  String.valueOf(YEAR);
		String otherYear = String.valueOf((YEAR + 1));
		BigDecimal expectedFlatRate = BigDecimal.valueOf(22);

		// Create 5 records, 2 in bounds and 3 out of bounds of the query.
		insertIssuerUserFeeRate("MS", AUG_1, SEP_1, expectedYear, BigDecimal.valueOf(11), "1");
		insertIssuerUserFeeRate(expectedState, AUG_1, NOV_1, expectedYear, expectedFlatRate, "1");
		insertIssuerUserFeeRate(expectedState, SEP_1, DEC_1, expectedYear, expectedFlatRate, "1");
		insertIssuerUserFeeRate("PA", SEP_1, NOV_1, otherYear, BigDecimal.valueOf(33), "2");
		insertIssuerUserFeeRate("FL", AUG_1, DEC_1, otherYear, BigDecimal.valueOf(44), "3");

		List<IssuerUserFeeRate> iufRateList = rapDao.getUserFeeRateForAllStates(asOfDate, expectedYear);

		assertEquals("IssuerUserFeeRate list size", 2, iufRateList.size());

		int index = 0;
		for (IssuerUserFeeRate iufRate : iufRateList) {

			assertEquals(index + ": Issuer User Fee state cd", expectedState, iufRate.getIssuerUfStateCd());
			assertEquals(index + ": Issuer User Fee Coverage Year", expectedYear, iufRate.getIssuerUfCoverageYear());
			assertEquals(index + ": Issuer User Fee state cd", expectedFlatRate, iufRate.getIssuerUfFlatrate());
			index++;
		}
	}
	
	/**
	 * Inserts a minimal record into ISSUERUSERFEERATE.
	 */
	private void insertIssuerUserFeeRate(String state, DateTime startDate, DateTime endDate, String strYear, BigDecimal flatRate, String insrnPlnTypeCd)  {

		jdbcTemplate.execute("INSERT INTO ISSUERUSERFEERATE(ISSUERUFSTATECD, ISSUERUFSTARTDATE, INSRNCAPLCTNTYPECD, " +
				"ISSUERUFCOVERAGEYEAR, ISSUERUFFLATRATE, ISSUERUFENDDATE) " +
				"VALUES ('" + state + "', " + toDateValue(startDate) + ", '" + insrnPlnTypeCd + "'" + 
				", '" + strYear + "', " + flatRate + ", " + toDateValue(endDate) + ")");
	}
	
	/**
	 * Inserts a minimal record into TRANSMSG
	 *
	 * @return transMsgId
	 */
	private Long insertTransMsg() {

		Long transMsgId = jdbcTemplate.queryForObject("SELECT TRANSMSGSEQ.NEXTVAL FROM DUAL", Long.class);
		jdbcTemplate.execute("INSERT INTO TRANSMSG (TRANSMSGID, MSG, TRANSMSGDIRECTIONTYPECD, TRANSMSGTYPECD, " +
				"CREATEDATETIME, LASTMODIFIEDDATETIME) VALUES (" + transMsgId + ", XMLType('<RapDaoTest>Test Data for: " +
				transMsgId +"</RapDaoTest>'), '1', '1', SYSDATE, SYSDATE)");
		return transMsgId;
	}
	
	/**
	 * Inserts a minimal record into POLICYVERSION
	 *
	 * @return policyVersionId
	 */
	private Long insertPolicyVersion(Long transMsgId) {
		Long policyVersionId = jdbcTemplate.queryForObject("SELECT POLICYVERSIONSEQ.NEXTVAL FROM DUAL", Long.class);

		String sql = "INSERT INTO POLICYVERSION (POLICYVERSIONID, MAINTENANCESTARTDATETIME, MAINTENANCEENDDATETIME, " +
				"SUBSCRIBERSTATECD, ISSUERPOLICYID, ISSUERHIOSID, ISSUERSUBSCRIBERID, EXCHANGEPOLICYID, " +
				"POLICYSTARTDATE, POLICYENDDATE, PLANID, X12INSRNCLINETYPECD, " +
				"INSRNCAPLCTNTYPECD, TRANSMSGID, " + getSysArgs() + ") " + 
				"VALUES (PolicyVersionSeq.CURRVAL, TO_DATE('12/01/" + YEAR +"', 'mm/dd/yyyy'), " +
				"TO_DATE('9999-12-31 23:59:59', 'YYYY-MM-DD HH24:MI:SS'), '" + SUBSCRIBER_STATE_CD + "', 'P'||PolicyVersionSeq.CURRVAL, "+
				"101, 'SCB'||PolicyVersionSeq.CURRVAL, '" + EXPOLID_PREF + policyVersionId + "', " +
				toDateValue(SEP_1) + ", " + toDateValue(DEC_1) + ", " +
				"trim(to_char(101, '00009'))||'VAabcd', 'HLT', '1', " + transMsgId + ", " + getSysValues() + ")";
		jdbcTemplate.execute(sql);
		return policyVersionId;
	}
	
	/**
	 * Inserts a minimal record into POLICYSTATUS
	 */
	private void insertPolicyStatus(Long policyVersionId, String statusCd) {

		String sql = "INSERT INTO POLICYSTATUS (POLICYVERSIONID, TRANSDATETIME, " +
				"INSURANACEPOLICYSTATUSTYPECD, " + getSysArgs() + ") VALUES (" + policyVersionId + ", " + 
				" TO_DATE('12/01/" + YEAR +"', 'mm/dd/yyyy'), " + statusCd + ", " + getSysValues() + ")";
		jdbcTemplate.execute(sql);
	}
	
	/**
	 * Inserts a minimal record into POLICYPAYMENTTRANS
	 */
	private Long insertPolicyPaymentTrans(Long policyVersionId, DateTime coverageDate, DateTime policyEndDate, String transPeriodCd, String payProcStatusCd) {

		Long policyPaymentTransId = rapDao.getPolicyPaymentTransNextSeq();

		String sql = "INSERT INTO  POLICYPAYMENTTRANS (POLICYPAYMENTTRANSID, POLICYVERSIONID, " +
				"FINANCIALPROGRAMTYPECD,TRANSPERIODTYPECD,ISSUERHIOSID, ISSUERSTATECD, " +
				"MAINTENANCESTARTDATETIME, COVERAGEDATE, " + getSysArgs() + ",SUBSCRIBERSTATECD, EXCHANGEPOLICYID, " +
				"PAYMENTCOVERAGESTARTDATE, PAYMENTCOVERAGEENDDATE, LASTPAYMENTPROCSTATUSTYPECD) " +
				"VALUES (" + policyPaymentTransId +", " + policyVersionId + ", '" + "APTC" + "', '" + 
				transPeriodCd + "', '101', '" + ISSUER_STATE_CD +
				"', TO_DATE('12/01/" + YEAR + "', 'mm/dd/yyyy'), " + toDateValue(coverageDate) + ", " + getSysValues() + ", '" + 
				SUBSCRIBER_STATE_CD + "', '" + EXPOLID_PREF + policyVersionId + 
				"', " +toDateValue(coverageDate) + ", " + toDateValue(policyEndDate) + ", '" + payProcStatusCd +"')";

		jdbcTemplate.execute(sql);

		return policyPaymentTransId;
	}
	
	/**
	 * Inserts a minimal record into POLICYPREMIUM
	 */
	private void insertPolicyPremium(Long policyVersionId) {

		String sql = "INSERT INTO  POLICYPREMIUM (POLICYVERSIONID, " +
				"EFFECTIVESTARTDATE,TOTALPREMIUMAMOUNT,CSRAMOUNT,APTCAMOUNT, " +
				getSysArgs() + ") " +
				"VALUES (" + policyVersionId + ", " + 
				" TO_DATE('12/01/" + YEAR + "', 'mm/dd/yyyy'), " +
				new BigDecimal(100) + ", "+ new BigDecimal(50) + ", "+ new BigDecimal(50) +
				", "+ getSysValues() + ")";

		jdbcTemplate.execute(sql);

	}	
	
	private String getSysArgs() {

		return "CREATEDATETIME, LASTMODIFIEDDATETIME, CREATEBY, LASTMODIFIEDBY";
	}
	
	private String getSysValues() {

		return toTimestampValue(DATETIME) + ", " + toTimestampValue(DATETIME) +", '" + user + "', '" + user +"'";
	}
	
	private String toDateValue(DateTime dt) {
		return " TO_DATE('" + getSqlDate(dt) + "', 'YYYY-MM-DD HH24:MI:SS')";
	}
	
	private String toTimestampValue(DateTime ts) {

		return "TO_TIMESTAMP('" + sqlDf.print(ts.toDate(), Locale.US) + "', 'YYYY-MM-DD HH24:MI:SS.FF3')";
	}
	
	private java.sql.Date getSqlDate(DateTime dateTime) {
		if(dateTime != null) {
			return new java.sql.Date(dateTime.getMillis());
		}
		return null;
	}
}
