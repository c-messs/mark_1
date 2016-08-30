package gov.hhs.cms.ff.fm.eps.ep.dao.impl;

import gov.hhs.cms.ff.fm.eps.ep.dao.PolicyMemberVersionDao;
import gov.hhs.cms.ff.fm.eps.ep.enums.EProdEnum;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyMemberVersionPO;

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
public class PolicyMemberVersionDaoImplTest extends BaseDaoTest {
	
	
	@Autowired
	PolicyMemberVersionDao memberVersionDao;
	
	/*
	 *  Tests inserting an invalid value.
	 *  This test will fail if the column key constraint is dropped.
	 *  
	 *  EpsDataLogger will catch and throw the UncategorizedSQLException.
	 */
	@Test(expected=com.accenture.foundation.common.exception.ApplicationException.class)
	public void testPolicyMemberVersionDao_GenderCode_Exception() {
		
		assertNotNull("PolicyMemberVersionDao", memberVersionDao);
		PolicyMemberVersionPO expectedPo = super.makePolicyMemberVersion();
		expectedPo.setX12GenderTypeCd("X");
		memberVersionDao.insertPolicyMemberVersion(expectedPo);
	}
	
	/**
	 * Test XAK1POLICYMEMBERVERSION constraint.
	 * - EXCHANGEMEMBERID
     * - MAINTENANCESTARTDATETIME
     * - EXCHANGEPOLICYID
     * - SUBSCRIBERSTATECD
	 * 
	 */
	@Test
	public void test_Duplicate_EPROD_10() {

		String expectedCode = EProdEnum.EPROD_10.getCode();
		String exchangePolicyId = "444444";
		PolicyMemberVersionPO po1 = makePolicyMemberVersion("111111", APR_1_4am, exchangePolicyId, "NC");
		po1.setPolicyMemberFirstNm("BillyBob");
		insertPolicyMemberVersion(po1);

		// Attempt to insert with same ExchangePolicyId, SubscriberStateCd and MaintenanceStartDateTime
		// with other attribute different
		PolicyMemberVersionPO po2 = makePolicyMemberVersion("111111", APR_1_4am, exchangePolicyId, "NC");
		po2.setPolicyMemberFirstNm("BobbyJoe");
		String actualCode = null;
		try {
			memberVersionDao.insertPolicyMemberVersion(po2);
		} catch (ApplicationException appEx) {
			actualCode = appEx.getInformationCode();
		}
		assertEquals("ApplicationException thrown with", expectedCode, actualCode);
		
	}
	
	/**
	 * Test XAK1POLICYMEMBERVERSION constraint.
	 * - EXCHANGEMEMBERID
     * - MAINTENANCESTARTDATETIME will be 1 millisecond different.
     * - EXCHANGEPOLICYID
     * - SUBSCRIBERSTATECD
	 */
	@Test
	public void test_Near_Duplicate() {

		String exchangePolicyId = "444444";
		PolicyMemberVersionPO po1 = makePolicyMemberVersion("111111", APR_1_4am, exchangePolicyId, "NC");
		po1.setPolicyMemberFirstNm("BillyBob");
		insertPolicyMemberVersion(po1);

		// Attempt to insert with same ExchangePolicyId, SubscriberStateCd
		// but MaintenanceStartDateTime 1 millisecond (1000 nanos) greater.
		// with other attribute different
		PolicyMemberVersionPO po2 = makePolicyMemberVersion("111111", APR_1_4am.plusNanos(1000), exchangePolicyId, "NC");
		po2.setPolicyMemberFirstNm("BobbyJoe");
		Long pvId2 = memberVersionDao.insertPolicyMemberVersion(po2); 
		assertNotNull("policyMemberVersionId 2", pvId2);
	}

}
