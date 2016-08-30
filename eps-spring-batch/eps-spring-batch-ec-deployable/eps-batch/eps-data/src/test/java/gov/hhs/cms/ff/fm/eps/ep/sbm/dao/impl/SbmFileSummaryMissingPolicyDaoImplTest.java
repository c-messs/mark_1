package gov.hhs.cms.ff.fm.eps.ep.sbm.dao.impl;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import gov.hhs.cms.ff.fm.eps.ep.dao.SbmFileSummaryMissingPolicyDao;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmFileSummaryMissingPolicyData;
import gov.hhs.cms.ff.fm.eps.ep.vo.UserVO;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/sbmi-data-config.xml", "classpath:/test-context-data.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class SbmFileSummaryMissingPolicyDaoImplTest extends BaseSBMDaoTest {
	
	
	@Autowired
	private SbmFileSummaryMissingPolicyDao sbmFileSummaryMissingPolicyDao;
	
	@Autowired
	private UserVO userVO;
	
	
	@Test
	public void test_selectMissingPolicyList() {
		
		int expectedListSize = 0;
		assertNotNull("sbmFileSummaryMissingPolicyDao", sbmFileSummaryMissingPolicyDao);
		Long sbmFileProcSumId = 999999999L;
	    List<SbmFileSummaryMissingPolicyData> actualPOList = sbmFileSummaryMissingPolicyDao.selectMissingPolicyList(sbmFileProcSumId);
	    
	    assertEquals("SbmFileSummaryMissingPolicyData list size", expectedListSize, actualPOList.size());
	}
	
	
}
