package gov.hhs.cms.ff.fm.eps.ep.sbm.dao.impl;

import java.util.ArrayList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import gov.hhs.cms.ff.fm.eps.ep.dao.SbmPolicyMemberDateDao;
import gov.hhs.cms.ff.fm.eps.ep.enums.EProdEnum;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyMemberDatePO;
 

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/sbmi-data-config.xml", "classpath:/test-context-data.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class SbmPolicyMemberDateDaoImplTest extends BaseSBMDaoTest {
	
	@Autowired
	private SbmPolicyMemberDateDao dateDao;
	
	@Test
	public void test_deleteStaging_Exception() { 

		Class<?> expectedEx = com.accenture.foundation.common.exception.ApplicationException.class;
		String expectedCd = EProdEnum.EPROD_10.getCode();
		Long sbmFileProcSumId = null;
		try {
			dateDao.deleteStaging(sbmFileProcSumId);
		} catch (Exception ex) {
			assertEquals("Exception thrown", expectedEx, ex.getClass());
			assertEquals("EPROD", expectedCd, ex.getMessage());
		}
	}
	
	@Test
	public void test_mergePolicyMemberDate_Exception() { 
		
		// Call unimplemented methods for coverage only.
		dateDao.insertPolicyMemberDates(new ArrayList<PolicyMemberDatePO>());
		dateDao.getPolicyMemberDates(Long.valueOf("99"));
		
		Class<?> expectedEx = com.accenture.foundation.common.exception.ApplicationException.class;
		String expectedCd = EProdEnum.EPROD_10.getCode();
		Long sbmFileProcSumId = null;
		try {
			dateDao.mergePolicyMemberDate(sbmFileProcSumId);
		} catch (Exception ex) {
			assertEquals("Exception thrown", expectedEx, ex.getClass());
			assertEquals("EPROD", expectedCd, ex.getMessage());
		}
	}
	
	

}
