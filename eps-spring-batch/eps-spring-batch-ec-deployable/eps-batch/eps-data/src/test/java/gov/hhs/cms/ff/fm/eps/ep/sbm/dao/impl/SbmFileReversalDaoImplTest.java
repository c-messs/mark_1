package gov.hhs.cms.ff.fm.eps.ep.sbm.dao.impl;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import gov.hhs.cms.ff.fm.eps.ep.enums.EProdEnum;
import gov.hhs.cms.ff.fm.eps.ep.enums.PolicyStatus;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyStatusPO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMPolicyDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SbmFileReversalDao;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;
import gov.hhs.cms.ff.fm.eps.ep.util.sbm.TestDataSBMUtility;
import gov.hhs.cms.ff.fm.eps.ep.vo.UserVO;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/sbmi-data-config.xml", "classpath:/test-context-data.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class SbmFileReversalDaoImplTest extends BaseSBMDaoTest {

	@Autowired
	private SbmFileReversalDao sbmFileReversalDao;

	@Autowired
	private UserVO userVO;


	/* --Query to view results when defaultRollback = false
	 * 
	SELECT sfi.SBMFILEPROCESSINGSUMMARYID AS SUMID, pv.SBMTRANSMSGID AS TXNID, pv.POLICYVERSIONID AS PVID, pv.PRIORPOLICYVERSIONID AS PR_PVID
,  pv.EXCHANGEPOLICYID AS EXPOLID, pv.SUBSCRIBERSTATECD AS ST
, TO_CHAR(pv.MAINTENANCESTARTDATETIME, 'YYYY-MM-DD HH24:MI:SS.FF3') AS MSD, TO_CHAR(pv.MAINTENANCEENDDATETIME, 'YYYY-MM-DD HH24:MI:SS.FF3') AS MED
, (SELECT LISTAGG(ps.INSURANACEPOLICYSTATUSTYPECD, ',') WITHIN GROUP (ORDER BY ps.TRANSDATETIME ASC) FROM POLICYSTATUS ps WHERE ps.POLICYVERSIONID = pv.POLICYVERSIONID) AS STAT
, TO_CHAR(pv.POLICYSTARTDATE, 'MM-DD-YYYY') AS PSD, TO_CHAR(pv.POLICYENDDATE, 'MM-DD-YYYY') AS PED 
, pv.CREATEBY AS "CREATEBY", pv.LASTMODIFIEDBY AS LMODBY
, TO_CHAR(pv.CREATEDATETIME, 'YYYY-MM-DD HH24:MM:SS') AS CREATEDT
, TO_CHAR(pv.LASTMODIFIEDDATETIME, 'YYYY-MM-DD HH24:MM:SS') AS LASTMODDT
FROM POLICYVERSION pv
JOIN SBMTRANSMSG stm ON pv.SBMTRANSMSGID = stm.SBMTRANSMSGID
JOIN SBMFILEINFO sfi ON stm.SBMFILEINFOID = sfi.SBMFILEINFOID
ORDER BY  pv.POLICYVERSIONID DESC
;
	 */

	/**
	 * Single Policy version, J86.
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void test_backOutFile_J86() throws InterruptedException {

		Long id = TestDataSBMUtility.getRandomNumberAsLong(3);

		int expectedPolicyCnt = 2;
		String expectedCtBySBMi = "SBMI-" + id.toString();

		userVO.setUserId(expectedCtBySBMi);

		String stateCd = TestDataSBMUtility.getRandomSbmState();
		String tenantId = stateCd + "0";
		String sbmFileId = "FID-" + id;
		LocalDateTime msd = JAN_1_1am;
		LocalDateTime med = DateTimeUtil.HIGHDATE;
		String exchangePolicyId = "EXPOLID-" + id.toString();
		int cntMembers = 2;
		boolean isStaging = false;

		SBMPolicyDTO policyDTO = insertPolicyMinimumComplete(tenantId, sbmFileId, msd, med, exchangePolicyId,  cntMembers, isStaging, null);

		Long sbmFileProcSumId = policyDTO.getSbmFileProcSummaryId();

		String expectedCtByBKO = "InsertPVj86";//"BKO-" + id.toString();
		
		Thread.sleep(1055);

		// Set user and run.
		userVO.setUserId(expectedCtByBKO);

		sbmFileReversalDao.backOutFile(sbmFileProcSumId);
		
		// Confirm Policy and Statuses.
		
		Map<String, Object> status1;
		Map<String, Object> status2;
		Map<String, Object> status3;
		

		// Get all (both in this case) for the exchangePolicyId
		String sql = "SELECT * FROM POLICYVERSION WHERE EXCHANGEPOLICYID = '" + exchangePolicyId +
				"' AND SUBSCRIBERSTATECD = '" + stateCd + "' ORDER BY MAINTENANCESTARTDATETIME ASC";

		// Confirm there are 2.
		List<Map<String, Object>> actualList = jdbc.queryForList(sql);
		assertEquals("POLICYVERSION list size", expectedPolicyCnt, actualList.size());


		// Confirm Policy 1 data
		// Row 1 is the original policy with status of 2 changed to 5, and had MED updated from HIGHDATE
		Map<String, Object> row1 = actualList.get(0);

		assertEquals("ExchangePolicyId", policyDTO.getPolicy().getExchangeAssignedPolicyId(), row1.get("EXCHANGEPOLICYID"));
		assertNotSame("MaintenanceEndDate", Timestamp.valueOf(DateTimeUtil.HIGHDATE),  (Timestamp) row1.get("MAINTENANCEENDDATETIME"));
		assertNull("PriorPolicyVersionId should still be null", row1.get("PRIORPOLICYVERSIONID"));

		BigDecimal pvId1 = (BigDecimal) row1.get("POLICYVERSIONID");
		sql = "SELECT * FROM POLICYSTATUS WHERE POLICYVERSIONID = " + pvId1 + " ORDER BY TRANSDATETIME ASC";

		// Confirm Policy 1 statuses
		List<Map<String, Object>> actualStatusList1 = jdbc.queryForList(sql);
		assertEquals("POLICYSTATUS list size for 1st policy", 2, actualStatusList1.size());

		status1 = actualStatusList1.get(0);
		status2 = actualStatusList1.get(1);

		assertEquals("Policy 1, original status", PolicyStatus.EFFECTUATED_2.getValue(), (String) status1.get("INSURANACEPOLICYSTATUSTYPECD")); 
		assertEquals("Policy 1, Reversed status", PolicyStatus.SBMVOID_6.getValue(), (String) status2.get("INSURANACEPOLICYSTATUSTYPECD")); 

		boolean isTimesSame = false;

		assertSysData(row1, expectedCtBySBMi, "NewUpdatepv", isTimesSame);


		// Confirm Policy 2 data

		// Policy 2 is the new created policy with status of 2,6,5 and MED == HIGHDATE
		Map<String, Object> row2 = actualList.get(1);

		// New policy has same values, but MED of HIGHDATE
		assertEquals("ExchangePolicyId", policyDTO.getPolicy().getExchangeAssignedPolicyId(), row2.get("EXCHANGEPOLICYID"));
		assertEquals("MaintenanceEndDate", Timestamp.valueOf(DateTimeUtil.HIGHDATE),  (Timestamp) row2.get("MAINTENANCEENDDATETIME"));
		assertEquals("PriorPolicyVersionId for Policy 2 should equal PolicyVersionId of Policy 1", new BigDecimal(policyDTO.getPolicyVersionId()), (BigDecimal) row2.get("PRIORPOLICYVERSIONID"));
		// Get statuses for Policy 2
		BigDecimal pvId2 = (BigDecimal) row2.get("POLICYVERSIONID");
		sql = "SELECT * FROM POLICYSTATUS WHERE POLICYVERSIONID = " + pvId2 + " ORDER BY TRANSDATETIME ASC";

		// Confirm Policy 2 statuses (should be 2,6,5)
		List<Map<String, Object>> actualStatusList2 = jdbc.queryForList(sql);
		assertEquals("POLICYSTATUS list size for 1st policy", 3, actualStatusList2.size());

		status1 = actualStatusList2.get(0);
		status2 = actualStatusList2.get(1);
		status3 = actualStatusList2.get(2);

		assertEquals("Policy 2, original status", PolicyStatus.EFFECTUATED_2.getValue(), (String) status1.get("INSURANACEPOLICYSTATUSTYPECD")); 
		assertEquals("Policy 2, Reversed status", PolicyStatus.SBMVOID_6.getValue(), (String) status2.get("INSURANACEPOLICYSTATUSTYPECD")); 
		assertEquals("Policy 2, Latest status", PolicyStatus.SUPERSEDED_5.getValue(), (String) status3.get("INSURANACEPOLICYSTATUSTYPECD")); 		

		isTimesSame = true;
		assertSysData(row2, expectedCtByBKO, "InsertPVj86", isTimesSame);
	}


	/**
	 * Multiple existing Policy version, J83.
	 * @throws InterruptedException
	 */
	@Test
	public void test_backOutFile_J83() throws InterruptedException {

		Long id = TestDataSBMUtility.getRandomNumberAsLong(3);

		int expectedPolicyCnt = 3;
		String expectedCtBy1 = "SBMI-1-" + id.toString();
		String expectedCtBy2 = "SBMI-2-" + id.toString();
		String expectedLmByBKO = "NewUpdatepv";//"BKO-" + id.toString();

		userVO.setUserId(expectedCtBy1);
		String stateCd = TestDataSBMUtility.getRandomSbmState();
		String tenantId = stateCd + "0";
		String sbmFileId = "FID-" + id;
		LocalDateTime msd = JAN_1_1am;
		LocalDateTime med = FEB_1_2am;
		String exchangePolicyId = "EXPOLID-" + id.toString();
		int cntMembers = 2;
		boolean isStaging = false;

		// Set up and insert Policy 1
		List<PolicyStatusPO> statusPOList = new ArrayList<PolicyStatusPO>();
		statusPOList.add(makePolicyStatusPO(JAN_1_1am, PolicyStatus.EFFECTUATED_2));
		// for set up, the existing PV1 will have been modified by the job that created PV2, 
		String lastModBy2 = expectedCtBy2;
		SBMPolicyDTO policyDTO = insertPolicyMinimumComplete(tenantId, sbmFileId, msd, med, exchangePolicyId, cntMembers, isStaging, null, lastModBy2);
		Long priorPvId = policyDTO.getPolicyVersionId();

		Thread.sleep(1005);

		// Set up and insert Policy 2
		userVO.setUserId(expectedCtBy2);
		msd = FEB_1_2am.plusNanos(1000000);
		med = DateTimeUtil.HIGHDATE;
		// both CreateBy and LastModby will be the same for this policy
		SBMPolicyDTO policyDTO2 = insertPolicyMinimumComplete(tenantId, sbmFileId, msd, med, exchangePolicyId, cntMembers, isStaging, priorPvId);

		Long sbmFileProcSumId2 = policyDTO2.getSbmFileProcSummaryId();
		
		Thread.sleep(1005);

		// Set BKO user and run.
		userVO.setUserId(expectedLmByBKO);
		
		sbmFileReversalDao.backOutFile(sbmFileProcSumId2);
		
		// Confirm Policy and statues.

		String sql = "SELECT * FROM POLICYVERSION WHERE EXCHANGEPOLICYID = '" + exchangePolicyId +
				"' AND SUBSCRIBERSTATECD = '" + stateCd + "' ORDER BY MAINTENANCESTARTDATETIME ASC";

		List<Map<String, Object>> actualList = jdbc.queryForList(sql);
		assertEquals("POLICYVERSION list size", expectedPolicyCnt, actualList.size());
		
		Map<String, Object> status1;
		Map<String, Object> status2;
		Map<String, Object> status3;
		
		boolean isTimesSame = false;

		// Confirm Policy 1 data
		// Row 1 is the orginal policy with status of 2
		Map<String, Object> row1 = actualList.get(0);

		assertEquals("ExchangePolicyId", policyDTO.getPolicy().getExchangeAssignedPolicyId(), row1.get("EXCHANGEPOLICYID"));

		BigDecimal pvId1 = (BigDecimal) row1.get("POLICYVERSIONID");
		sql = "SELECT * FROM POLICYSTATUS WHERE POLICYVERSIONID = " + pvId1 + " ORDER BY TRANSDATETIME ASC";

		// Confirm Policy 1 statuses and data
		// Policy 1 status should still remain list size = 1 and EFF.  Basically no change to any data.
		List<Map<String, Object>> actualStatusList1 = jdbc.queryForList(sql);
		assertEquals("POLICYSTATUS list size for 1st policy", 1, actualStatusList1.size());
		status1 = actualStatusList1.get(0);
		assertEquals("Policy 1, original status", PolicyStatus.EFFECTUATED_2.getValue(), (String) status1.get("INSURANACEPOLICYSTATUSTYPECD")); 


		// Confirm Policy 2 data
		//
		Map<String, Object> row2 = actualList.get(1);

		// Second policy: MED no longer HIGHDATE, should have 2,6 status
		assertEquals("ExchangePolicyId", policyDTO.getPolicy().getExchangeAssignedPolicyId(), row2.get("EXCHANGEPOLICYID"));
		assertNotSame("MaintenanceEndDate", Timestamp.valueOf(DateTimeUtil.HIGHDATE),  (Timestamp) row2.get("MAINTENANCEENDDATETIME"));
		assertSysData(row2, expectedCtBy2, expectedLmByBKO, isTimesSame);


		BigDecimal pvId2 = (BigDecimal) row2.get("POLICYVERSIONID");
		sql = "SELECT * FROM POLICYSTATUS WHERE POLICYVERSIONID = " + pvId2 + " ORDER BY TRANSDATETIME ASC";

		// Confirm Policy 2 statuses (Should be 2,6)
		List<Map<String, Object>> actualStatusList2 = jdbc.queryForList(sql);
		assertEquals("POLICYSTATUS list size for 1st policy", 2, actualStatusList2.size());

		status1 = actualStatusList2.get(0);
		status2 = actualStatusList2.get(1);

		assertEquals("Policy 2, original status", PolicyStatus.EFFECTUATED_2.getValue(), (String) status1.get("INSURANACEPOLICYSTATUSTYPECD")); 
		assertEquals("Policy 2, Reversed status", PolicyStatus.SBMVOID_6.getValue(), (String) status2.get("INSURANACEPOLICYSTATUSTYPECD")); 

	    isTimesSame = false;
		assertSysData(row2, expectedCtBy2, expectedLmByBKO, isTimesSame);

		isTimesSame = false;
		assertSysData(row1, expectedCtBy1, "UpdateLatestpvj83", isTimesSame);
		
	}
	
	
	@Test
	public void test_updatePolicyStatus_Exception() { 
		
		Class<?> expectedEx = com.accenture.foundation.common.exception.ApplicationException.class;
		String expectedCd = EProdEnum.EPROD_10.getCode();
		Long sbmFileProcSumId = null;
		try {
			ReflectionTestUtils.invokeMethod(sbmFileReversalDao, "updatePolicyStatus", sbmFileProcSumId);
		} catch (Exception ex) {
			assertEquals("Exception thrown", expectedEx, ex.getClass());
			assertEquals("EPROD", expectedCd, ex.getMessage());
		}
	}
	
	@Test
	public void test_copyPrecedingPolicyVersion_Exception() { 
		
		Class<?> expectedEx = com.accenture.foundation.common.exception.ApplicationException.class;
		String expectedCd = EProdEnum.EPROD_10.getCode();
		Long sbmFileProcSumId = null;
		try {
			ReflectionTestUtils.invokeMethod(sbmFileReversalDao, "copyPrecedingPolicyVersion", sbmFileProcSumId);
		} catch (Exception ex) {
			assertEquals("Exception thrown", expectedEx, ex.getClass());
			assertEquals("EPROD", expectedCd, ex.getMessage());
		}
	}
	
	@Test
	public void test_copyPolicyVersion_Exception() { 
		
		Class<?> expectedEx = com.accenture.foundation.common.exception.ApplicationException.class;
		String expectedCd = EProdEnum.EPROD_10.getCode();
		Long sbmFileProcSumId = null;
		try {
			ReflectionTestUtils.invokeMethod(sbmFileReversalDao, "copyPolicyVersion", sbmFileProcSumId);
		} catch (Exception ex) {
			assertEquals("Exception thrown", expectedEx, ex.getClass());
			assertEquals("EPROD", expectedCd, ex.getMessage());
		}
	}
	
	@Test
	public void test_copyPrecedingStatus_Exception() { 
		
		Class<?> expectedEx = com.accenture.foundation.common.exception.ApplicationException.class;
		String expectedCd = EProdEnum.EPROD_10.getCode();
		Long sbmFileProcSumId = null;
		try {
			ReflectionTestUtils.invokeMethod(sbmFileReversalDao, "copyPrecedingStatus", sbmFileProcSumId);
		} catch (Exception ex) {
			assertEquals("Exception thrown", expectedEx, ex.getClass());
			assertEquals("EPROD", expectedCd, ex.getMessage());
		}
	}
	
	@Test
	public void test_copyPolicyPremiums_Exception() { 
		
		Class<?> expectedEx = com.accenture.foundation.common.exception.ApplicationException.class;
		String expectedCd = EProdEnum.EPROD_10.getCode();
		Long sbmFileProcSumId = null;
		try {
			ReflectionTestUtils.invokeMethod(sbmFileReversalDao, "copyPolicyPremiums", sbmFileProcSumId);
		} catch (Exception ex) {
			assertEquals("Exception thrown", expectedEx, ex.getClass());
			assertEquals("EPROD", expectedCd, ex.getMessage());
		}
	}
	
	@Test
	public void test_copyPolicyMembers_Exception() { 
		
		Class<?> expectedEx = com.accenture.foundation.common.exception.ApplicationException.class;
		String expectedCd = EProdEnum.EPROD_10.getCode();
		Long sbmFileProcSumId = null;
		try {
			ReflectionTestUtils.invokeMethod(sbmFileReversalDao, "copyPolicyMembers", sbmFileProcSumId);
		} catch (Exception ex) {
			assertEquals("Exception thrown", expectedEx, ex.getClass());
			assertEquals("EPROD", expectedCd, ex.getMessage());
		}
	}
	
	@Test
	public void test_updateMaintDateTimeForVoid_Exception() { 
		
		Class<?> expectedEx = com.accenture.foundation.common.exception.ApplicationException.class;
		String expectedCd = EProdEnum.EPROD_10.getCode();
		Long sbmFileProcSumId = null;
		try {
			ReflectionTestUtils.invokeMethod(sbmFileReversalDao, "updateMaintDateTimeForVoid", sbmFileProcSumId);
		} catch (Exception ex) {
			assertEquals("Exception thrown", expectedEx, ex.getClass());
			assertEquals("EPROD", expectedCd, ex.getMessage());
		}
	}
	
	@Test
	public void test_updateMaintDateTimeForPreceding_Exception() { 
		
		Class<?> expectedEx = com.accenture.foundation.common.exception.ApplicationException.class;
		String expectedCd = EProdEnum.EPROD_10.getCode();
		Long sbmFileProcSumId = null;
		try {
			ReflectionTestUtils.invokeMethod(sbmFileReversalDao, "updateMaintDateTimeForPreceding", sbmFileProcSumId);
		} catch (Exception ex) {
			assertEquals("Exception thrown", expectedEx, ex.getClass());
			assertEquals("EPROD", expectedCd, ex.getMessage());
		}
	}

	@Test
	public void test_copyStatusHistory_Exception() { 
		
		Class<?> expectedEx = com.accenture.foundation.common.exception.ApplicationException.class;
		String expectedCd = EProdEnum.EPROD_10.getCode();
		Long sbmFileProcSumId = null;
		try {
			ReflectionTestUtils.invokeMethod(sbmFileReversalDao, "copyStatusHistory", sbmFileProcSumId);
		} catch (Exception ex) {
			assertEquals("Exception thrown", expectedEx, ex.getClass());
			assertEquals("EPROD", expectedCd, ex.getMessage());
		}
	}

}
