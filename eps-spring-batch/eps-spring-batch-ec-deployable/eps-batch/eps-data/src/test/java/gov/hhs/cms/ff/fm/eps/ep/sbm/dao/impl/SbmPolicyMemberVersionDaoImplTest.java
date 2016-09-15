package gov.hhs.cms.ff.fm.eps.ep.sbm.dao.impl;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import gov.hhs.cms.ff.fm.eps.ep.enums.EProdEnum;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyMemberVersionPO;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmPolicyMemberVersionPO;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;
import gov.hhs.cms.ff.fm.eps.ep.util.sbm.TestDataSBMUtility;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/sbmi-data-config.xml", "classpath:/test-context-data.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class SbmPolicyMemberVersionDaoImplTest extends BaseSBMDaoTest {
	
	@Autowired
	SbmPolicyMemberVersionDaoImpl sbmPolicyMemberVersionDao;
	
	@Test
	public void insertStagingPolicyMemberVersion() { 
		
		String id = TestDataSBMUtility.getRandomNumberAsString(5);
		String exchangeMemId = "EXASSID-" + id;
		LocalDateTime maintStart = FEB_1_2am;
		String exchangePolicyId = "EXPOLID-" + id;
		String subscriberStateCd = TestDataSBMUtility.getRandomSbmState();
		
		SbmPolicyMemberVersionPO expected = makeSbmPolicyMemberVersionPO(exchangeMemId, maintStart, exchangePolicyId, subscriberStateCd);
		
		Long pmvId = sbmPolicyMemberVersionDao.insertStagingPolicyMemberVersion(expected);
		
		String sql = "SELECT * FROM STAGINGPOLICYMEMBERVERSION WHERE POLICYMEMBERVERSIONID =" + pmvId;

		List<Map<String, Object>> actualList = jdbc.queryForList(sql);
		assertEquals("STAGINGPOLICYMEMBERVERSION record list size", 1, actualList.size());
		
		Map<String, Object> row = actualList.get(0);
		
		assertEquals("SbmFileNm", exchangeMemId, row.get("EXCHANGEMEMBERID"));
		assertNotNull("MaintenanceStartDateTime", (Timestamp) row.get("MAINTENANCESTARTDATETIME"));
		assertEquals("MaintenanceEndDateTime", DateTimeUtil.getSqlTimestamp(DateTimeUtil.HIGHDATE), (Timestamp) row.get("MAINTENANCEENDDATETIME"));
		assertEquals("ExchangePolicyId", exchangePolicyId, row.get("EXCHANGEPOLICYID"));
		assertEquals("SubscriberStateCd", subscriberStateCd, row.get("SUBSCRIBERSTATECD"));
		
		//POLICYMEMBERVERSIONID, SUBSCRIBERIND, ISSUERASSIGNEDMEMBERID, EXCHANGEMEMBERID, POLICYMEMBERDEATHDATE, POLICYMEMBERLASTNM, 
		//POLICYMEMBERFIRSTNM, POLICYMEMBERMIDDLENM, POLICYMEMBERSALUTATIONNM, POLICYMEMBERSUFFIXNM, POLICYMEMBERSSN, EXCHANGEPOLICYID, 
		//SUBSCRIBERSTATECD, X12TOBACCOUSETYPECD, POLICYMEMBERBIRTHDATE, X12GENDERTYPECD, INCORRECTGENDERTYPECD, NONCOVEREDSUBSCRIBERIND, 
		//X12LANGUAGETYPECD, X12LANGUAGEQUALIFIERTYPECD, X12RACEETHNICITYTYPECD, ZIPPLUS4CD, PRIORPOLICYMEMBERVERSIONID, CREATEBY, LASTMODIFIEDBY
		//SBMTRANSMSGID

	}
	
	@Test
	public void test_mergePolicyMemberVersion_Exception() { 
			
		Class<?> expectedEx = com.accenture.foundation.common.exception.ApplicationException.class;
		String expectedCd = EProdEnum.EPROD_10.getCode();
		Long sbmFileProcSumId = null;
		try {
			sbmPolicyMemberVersionDao.mergePolicyMemberVersion(sbmFileProcSumId);
		} catch (Exception ex) {
			assertEquals("Exception thrown", expectedEx, ex.getClass());
			assertEquals("EPROD", expectedCd, ex.getMessage());
		}
	}
	
	
	@Test
	public void test_mergeLang_Exception() { 
			
		Class<?> expectedEx = com.accenture.foundation.common.exception.ApplicationException.class;
		String expectedCd = EProdEnum.EPROD_10.getCode();
		Long sbmFileProcSumId = null;
		try {
			sbmPolicyMemberVersionDao.mergeLang(sbmFileProcSumId);
		} catch (Exception ex) {
			assertEquals("Exception thrown", expectedEx, ex.getClass());
			assertEquals("EPROD", expectedCd, ex.getMessage());
		}
	}
	
	
	@Test
	public void test_mergeRace_Exception() { 
			
		Class<?> expectedEx = com.accenture.foundation.common.exception.ApplicationException.class;
		String expectedCd = EProdEnum.EPROD_10.getCode();
		Long sbmFileProcSumId = null;
		try {
			sbmPolicyMemberVersionDao.mergeRace(sbmFileProcSumId);
		} catch (Exception ex) {
			assertEquals("Exception thrown", expectedEx, ex.getClass());
			assertEquals("EPROD", expectedCd, ex.getMessage());
		}
	}
	
	
	@Test
	public void test_mergeAddr_Exception() { 
			
		Class<?> expectedEx = com.accenture.foundation.common.exception.ApplicationException.class;
		String expectedCd = EProdEnum.EPROD_10.getCode();
		Long sbmFileProcSumId = null;
		try {
			sbmPolicyMemberVersionDao.mergeAddr(sbmFileProcSumId);
		} catch (Exception ex) {
			assertEquals("Exception thrown", expectedEx, ex.getClass());
			assertEquals("EPROD", expectedCd, ex.getMessage());
		}
	}
	
	
	@Test
	public void test_deleteStaging_Exception() { 
			
		Class<?> expectedEx = com.accenture.foundation.common.exception.ApplicationException.class;
		String expectedCd = EProdEnum.EPROD_10.getCode();
		Long sbmFileProcSumId = null;
		try {
			sbmPolicyMemberVersionDao.deleteStaging(sbmFileProcSumId);
		} catch (Exception ex) {
			assertEquals("Exception thrown", expectedEx, ex.getClass());
			assertEquals("EPROD", expectedCd, ex.getMessage());
		}
	}
	
	
	@Test
	public void test_insertPolicyMemberVersion() {
		Long expected = null;
		Long actual = sbmPolicyMemberVersionDao.insertPolicyMemberVersion(null);
		assertEquals("Not implemented should return null", expected, actual);
	}
	
	@Test
	public void test_getPolicyMemberVersions() {
		List<PolicyMemberVersionPO> expected = Collections.emptyList();
		List<PolicyMemberVersionPO> actualList = sbmPolicyMemberVersionDao.getPolicyMemberVersions(null, "CA");
		assertEquals("Not implemented should return null", expected, actualList);
	}	
	
}
