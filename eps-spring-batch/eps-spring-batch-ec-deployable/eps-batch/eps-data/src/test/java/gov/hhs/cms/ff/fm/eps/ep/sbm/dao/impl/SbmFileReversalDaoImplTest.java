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
import org.springframework.transaction.annotation.Transactional;

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

		Thread.sleep(1055);

		String expectedCtByBKO = "BKO-" + id.toString();
		String expectedLmByBKO = "BKO-" + id.toString();

		userVO.setUserId(expectedCtByBKO);

		sbmFileReversalDao.backOutFile(sbmFileProcSumId);

		// Get all (both in this case) for the exchangePolicyId
		String sql = "SELECT * FROM POLICYVERSION WHERE EXCHANGEPOLICYID = '" + exchangePolicyId +
				"' AND SUBSCRIBERSTATECD = '" + stateCd + "' ORDER BY MAINTENANCESTARTDATETIME ASC";

		// Confirm there are 2.
		List<Map<String, Object>> actualList = jdbc.queryForList(sql);
		assertEquals("POLICYVERSION list size", expectedPolicyCnt, actualList.size());


		// Confirm Policy 1 data
		// Row 1 is the orginal policy with status of 2 changed to 5, and had MED updated from HIGHDATE
		Map<String, Object> row1 = actualList.get(0);

		assertEquals("ExchangePolicyId", policyDTO.getPolicy().getExchangeAssignedPolicyId(), row1.get("EXCHANGEPOLICYID"));

		BigDecimal pvId1 = (BigDecimal) row1.get("POLICYVERSIONID");
		sql = "SELECT * FROM POLICYSTATUS WHERE POLICYVERSIONID = " + pvId1 + " ORDER BY TRANSDATETIME ASC";

		// Confirm Policy 1 statuses
		List<Map<String, Object>> actualStatusList1 = jdbc.queryForList(sql);
		// TODO fix status list, should be list size 2
		//assertEquals("POLICYSTATUS list size for 1st policy", 2, actualStatusList1.size());

		Map<String, Object> status1 = actualStatusList1.get(0);
		// TODO once list size is correct 
		//Map<String, Object> status2 = actualStatusList1.get(1);

		//assertEquals("Policy 1, original status", PolicyStatus.EFFECTUATED_2.getValue(), (String) status1.get("INSURANACEPOLICYSTATUSTYPECD")); 
		//assertEquals("Policy 1, Reversed status", "6", (String) status2.get("INSURANACEPOLICYSTATUSTYPECD")); 

		boolean isTimesSame = false;

		//TODO fix LastModifiedBy, not being set in reversal SQL.  The expected should be the BKO user.
		//assertSysData(row1, expectedCtBySBMi, expectedLmByBKO, isTimesSame);

		
		
		// Confirm Policy 2 data

		// Row 2 is the new created policy with status of 6, and MED == HIGHDATE
		Map<String, Object> row2 = actualList.get(1);

		// New policy has same values, but MED of HIGHDATE
		assertEquals("ExchangePolicyId", policyDTO.getPolicy().getExchangeAssignedPolicyId(), row2.get("EXCHANGEPOLICYID"));
		assertEquals("MaintenanceEndDate", Timestamp.valueOf(DateTimeUtil.HIGHDATE),  (Timestamp) row2.get("MAINTENANCEENDDATETIME"));


		BigDecimal pvId2 = (BigDecimal) row2.get("POLICYVERSIONID");
		sql = "SELECT * FROM POLICYSTATUS WHERE POLICYVERSIONID = " + pvId2 + " ORDER BY TRANSDATETIME ASC";

		// Confirm Policy 1 statuses
		List<Map<String, Object>> actualStatusList2 = jdbc.queryForList(sql);
		//assertEquals("POLICYSTATUS list size for 1st policy", 2, actualStatusList2.size());

		status1 = actualStatusList2.get(0);
		//status2 = actualStatusList2.get(1);

		//assertEquals("Policy 2, original status", PolicyStatus.EFFECTUATED_2.getValue(), (String) status1.get("INSURANACEPOLICYSTATUSTYPECD")); 
		//assertEquals("Policy 2, Reversed status", PolicyStatus.SUPERSEDED_5.getValue(), (String) status1.get("INSURANACEPOLICYSTATUSTYPECD")); 

		isTimesSame = true;
		assertSysData(row2, expectedCtByBKO, expectedLmByBKO, isTimesSame);

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
		String expectedLmBy = "BKO-" + id.toString();

		userVO.setUserId(expectedCtBy1);
		String stateCd = TestDataSBMUtility.getRandomSbmState();
		String tenantId = stateCd + "0";
		String sbmFileId = "FID-" + id;
		LocalDateTime msd = JAN_1_1am;
		LocalDateTime med = FEB_1_2am;
		String exchangePolicyId = "EXPOLID-" + id.toString();
		int cntMembers = 2;
		boolean isStaging = false;

		List<PolicyStatusPO> statusPOList = new ArrayList<PolicyStatusPO>();
		statusPOList.add(makePolicyStatusPO(JAN_1_1am, PolicyStatus.EFFECTUATED_2));
		// for set up, the existing PV1 will have been modified by the job that created PV2, 
		// so set it up this way.
		String lastModBy2 = expectedCtBy2;
		SBMPolicyDTO policyDTO = insertPolicyMinimumComplete(tenantId, sbmFileId, msd, med, exchangePolicyId, cntMembers, isStaging, null, lastModBy2);
		Long priorPvId = policyDTO.getPolicyVersionId();

		Thread.sleep(1005);

		userVO.setUserId(expectedCtBy2);

		msd = FEB_1_2am.plusNanos(1000000);
		med = DateTimeUtil.HIGHDATE;
		// both CreateBy and LastModby will be the same for this policy
		SBMPolicyDTO policyDTO2 = insertPolicyMinimumComplete(tenantId, sbmFileId, msd, med, exchangePolicyId, cntMembers, isStaging, priorPvId);

		Long sbmFileProcSumId2 = policyDTO2.getSbmFileProcSummaryId();

		userVO.setUserId(expectedLmBy);
		sbmFileReversalDao.backOutFile(sbmFileProcSumId2);

		String sql = "SELECT * FROM POLICYVERSION WHERE EXCHANGEPOLICYID = '" + exchangePolicyId +
				"' AND SUBSCRIBERSTATECD = '" + stateCd + "' ORDER BY MAINTENANCESTARTDATETIME ASC";

		List<Map<String, Object>> actualList = jdbc.queryForList(sql);
		assertEquals("POLICYVERSION list size", expectedPolicyCnt, actualList.size());

		// Row 1 is the orginal policy with status of 2 changed to 5, and had MED updated from HIGHDATE
		Map<String, Object> row1 = actualList.get(0);

		//assertSysData(row1, expectedCtBy1, expectedLmBy);

		// Row 2 is the new created policy with status of 6, and MED == HIGHDATE
		Map<String, Object> row2 = actualList.get(0);

		//assertSysData(row2, expectedCtBy2, expectedLmBy);

	}



}
