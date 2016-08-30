package gov.hhs.cms.ff.fm.eps.ep.sbm.dao.impl;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

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
	
}
