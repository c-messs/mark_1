package gov.hhs.cms.ff.fm.eps.ep.dao.impl;

import gov.hhs.cms.ff.fm.eps.ep.dao.PolicyVersionDao;
import gov.hhs.cms.ff.fm.eps.ep.data.util.TestDataUtil;
import gov.hhs.cms.ff.fm.eps.ep.enums.EProdEnum;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyVersionPO;

import java.time.LocalDateTime;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.accenture.foundation.common.exception.ApplicationException;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/eps-data-config.xml", "classpath:/test-context-data.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class PolicyVersionDaoImplTest extends BaseDaoTest {


	@Autowired
	PolicyVersionDao policyVersionDao;


	/**
	 * Test XAK1POLICYVERSION constraint.
	 * -EXCHANGEPOLICYID
	 * -SUBSCRIBERSTATECD
	 * -MAINTENANCESTARTDATETIME
	 * 
	 */
	@Test
	public void test_Duplicate_Success() {

		String exchangePolicyId = TestDataUtil.getRandomNumberAsString(6);
		String subscriberStateCd = "TN";
		String issuerNm1 = "ISSUERNAME 1111";
		String issuerNm2 = "ISSUERNAME 2222";

		PolicyVersionPO po1 = makePolicyVersion(exchangePolicyId, MAR_1_3am, subscriberStateCd);
		po1.setIssuerNm(issuerNm1);
		Long pvId1 = policyVersionDao.insertPolicyVersion(po1);

		// Attempt to insert with same ExchangePolicyId, SubscriberStateCd and MaintenanceStartDateTime
		// with other attribute different
		PolicyVersionPO po2 = makePolicyVersion(exchangePolicyId, MAR_1_3am, subscriberStateCd);
		po2.setIssuerNm(issuerNm2);
		Long pvId2 = policyVersionDao.insertPolicyVersion(po2);

		PolicyVersionPO epsPO_1 = policyVersionDao.getPolicyVersionById(pvId1, subscriberStateCd);
		PolicyVersionPO epsPO_2 = policyVersionDao.getPolicyVersionById(pvId2, subscriberStateCd);

		assertNotNull("PolicyVersion 1", epsPO_1);
		assertNotNull("PolicyVersion 2", epsPO_2);

		assertEquals("ExchangePolicyId is same for both", epsPO_1.getExchangePolicyId(), epsPO_2.getExchangePolicyId());
		assertEquals("IssuerNm for PV1", issuerNm1, epsPO_1.getIssuerNm());
		assertEquals("IssuerNm for PV2", issuerNm2, epsPO_2.getIssuerNm());

		assertEquals("MSD for PV1 is 1000000 nanoseconds (1 millisecond) less tha PV2 MSD", epsPO_1.getMaintenanceStartDateTime(), epsPO_2.getMaintenanceStartDateTime().minusNanos(1000000));
	}

	/**
	 * Test Thread InterruptedException with EPROD-10 while sleeping after a constraint violation.
	 */
	@Test
	public void test_Duplicate_InterruptedException() {

		EProdEnum expectedEPROD = EProdEnum.EPROD_10;
		Long expectedPvId2 = null;
		String expectedExClsNm = InterruptedException.class.getName();
		
		String exchangePolicyId = TestDataUtil.getRandomNumberAsString(6);
		String subscriberStateCd = "TN";
		String issuerNm1 = "ISSUERNAME 1111";
		String issuerNm2 = "ISSUERNAME 2222";

		PolicyVersionPO po1 = makePolicyVersion(exchangePolicyId, MAR_1_3am, subscriberStateCd);
		po1.setIssuerNm(issuerNm1);
		policyVersionDao.insertPolicyVersion(po1);

		// Attempt to insert with same ExchangePolicyId, SubscriberStateCd and MaintenanceStartDateTime
		// with other attribute different
		PolicyVersionPO po2 = makePolicyVersion(exchangePolicyId, MAR_1_3am, subscriberStateCd);
		po2.setIssuerNm(issuerNm2);
		
		Thread.currentThread().interrupt();

		try {
			expectedPvId2 = policyVersionDao.insertPolicyVersion(po2);
		} catch (ApplicationException appEx) {
			assertEquals("ApplicationException EPROD code", expectedEPROD.getCode(), appEx.getInformationCode());
			assertEquals("ApplicationException's double nested exception", expectedExClsNm, appEx.getCause().getCause().getClass().getName());
		}

		assertNull("Returned PolicyVersionId is null.", expectedPvId2);
	}

	/**
	 * Test XAK1POLICYVERSION constraint.
	 * -EXCHANGEPOLICYID
	 * -SUBSCRIBERSTATECD
	 * -MAINTENANCESTARTDATETIME will be 1 millisecond different.
	 * 
	 */
	@Test
	public void test_Near_Duplicate() {

		String exchangePolicyId = "444444";
		String subscriberStateCd = "TN";
		String issuerNm1 = "ISSUERNAME 1111";
		String issuerNm2 = "ISSUERNAME 2222";
		PolicyVersionPO po1 = makePolicyVersion(exchangePolicyId, MAR_1_3am, subscriberStateCd);
		po1.setIssuerNm(issuerNm1);
		Long pvId1 = policyVersionDao.insertPolicyVersion(po1);

		// Attempt to insert with same ExchangePolicyId, SubscriberStateCd
		// but MaintenanceStartDateTime 1 millisecond greater.
		// with other attribute different
		PolicyVersionPO po2 = makePolicyVersion(exchangePolicyId, MAR_1_3am.plusNanos(1), subscriberStateCd);
		po2.setIssuerNm(issuerNm2);
		Long pvId2 = policyVersionDao.insertPolicyVersion(po2); 
		assertNotNull("policyVersionId 2", pvId2);

		PolicyVersionPO epsPO_1 = policyVersionDao.getPolicyVersionById(pvId1, subscriberStateCd);
		PolicyVersionPO epsPO_2 = policyVersionDao.getPolicyVersionById(pvId2, subscriberStateCd);

		assertNotNull("PolicyVersion 1", epsPO_1);
		assertNotNull("PolicyVersion 2", epsPO_2);

		assertEquals("ExchangePolicyId is same for both", epsPO_1.getExchangePolicyId(), epsPO_2.getExchangePolicyId());
		assertEquals("IssuerNm for PV1", issuerNm1, epsPO_1.getIssuerNm());
		assertEquals("IssuerNm for PV2", issuerNm2, epsPO_2.getIssuerNm());

		assertEquals("MSD for PV1 is 1 millisecond less tha PV2 MSD", epsPO_1.getMaintenanceStartDateTime(), epsPO_2.getMaintenanceStartDateTime().minusNanos(1000000));
	}


	@Test	
	public void test_getLatestPolicyMaintStartDateTime() throws InterruptedException {

		PolicyVersionPO po1 = makePolicyVersion("111001", JAN_1_1am.plusYears(3));
		PolicyVersionPO po2 = makePolicyVersion("222002", FEB_1_2am.plusYears(3));
		PolicyVersionPO po3 = makePolicyVersion("333003", MAR_1_3am.plusYears(3));
		PolicyVersionPO po4 = makePolicyVersion("444004", APR_1_4am.plusYears(3));

		insertPolicyVersion(po1);
		insertPolicyVersion(po4);
		insertPolicyVersion(po2);
		insertPolicyVersion(po3);

		LocalDateTime latestMSD = policyVersionDao.getLatestPolicyMaintenanceStartDateTime();
		assertNotNull("Policy Maintenance Start Date INITIAL", latestMSD);

		assertEquals("LatestPolicyMaintStartDateTime", po4.getMaintenanceStartDateTime(), latestMSD);
	}


	/**
	 * Tests for null when there are no policies in EPS.
	 * @throws InterruptedException
	 */
	@Test	
	public void test_getLatestPolicyMaintStartDateTime_NoPolicies() throws InterruptedException {

		int countPolicies = jdbc.queryForObject("SELECT COUNT(*) FROM POLICYVERSION", Integer.class);

		if (countPolicies == 0) {
			LocalDateTime latestMSD = policyVersionDao.getLatestPolicyMaintenanceStartDateTime();
			assertNull("No results for Policy Maintenance Start Date", latestMSD);
		} else {
			// In case some test data was left in shema.
			assertTrue("Existing test data policies in EPS", countPolicies > 0);
		}
	}
}
