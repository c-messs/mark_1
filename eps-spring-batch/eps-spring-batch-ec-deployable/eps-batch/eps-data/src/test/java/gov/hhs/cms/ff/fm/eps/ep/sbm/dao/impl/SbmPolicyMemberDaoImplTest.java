package gov.hhs.cms.ff.fm.eps.ep.sbm.dao.impl;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import gov.hhs.cms.ff.fm.eps.ep.enums.EProdEnum;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyMemberPO;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/sbmi-data-config.xml", "classpath:/test-context-data.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class SbmPolicyMemberDaoImplTest extends BaseSBMDaoTest {
	
	@Autowired
	SbmPolicyMemberDaoImpl sbmPolicyMemberDao;
	
	
	@Test
	public void test_mergePolicyMember_Exception() { 
		
		// Call unimplemented method for coverage only.
		sbmPolicyMemberDao.insertPolicyMembers(new ArrayList<PolicyMemberPO>());
			
		Class<?> expectedEx = com.accenture.foundation.common.exception.ApplicationException.class;
		String expectedCd = EProdEnum.EPROD_10.getCode();
		Long sbmFileProcSumId = null;
		try {
			sbmPolicyMemberDao.mergePolicyMember(sbmFileProcSumId);
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
			sbmPolicyMemberDao.deleteStaging(sbmFileProcSumId);
		} catch (Exception ex) {
			assertEquals("Exception thrown", expectedEx, ex.getClass());
			assertEquals("EPROD", expectedCd, ex.getMessage());
		}
	}

}
