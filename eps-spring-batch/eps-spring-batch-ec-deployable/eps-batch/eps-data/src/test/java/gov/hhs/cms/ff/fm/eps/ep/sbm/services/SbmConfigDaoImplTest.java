package gov.hhs.cms.ff.fm.eps.ep.sbm.services;

import java.util.ArrayList;
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

import gov.hhs.cms.ff.fm.eps.ep.StateProrationConfiguration;
import gov.hhs.cms.ff.fm.eps.ep.util.sbm.TestDataSBMUtility;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/sbmi-data-config.xml", "classpath:/test-context-data.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class SbmConfigDaoImplTest extends BaseSbmServicesTest {

	
	@Autowired
	private  SbmConfigDao sbmConfigDao;

	@Test
	public void test_retrieveSbmStates() {

		assertNotNull("sbmConfigDao should not be null", sbmConfigDao);
		
	    List<String> expectedList = new ArrayList<String>();
	    Collections.addAll(expectedList, TestDataSBMUtility.SBM_STATES);
		
		List<StateProrationConfiguration> actualList = sbmConfigDao.retrieveSbmStates();
		
		assertNotNull("StateProrationConfiguration should NEVER be null.", actualList);
		
		for (StateProrationConfiguration state : actualList) {
			
			String msg  = state.getStateCd() + ": ";
			assertTrue(msg + "NOT found in expected SBM state list.", expectedList.contains(state.getStateCd()));
			assertEquals(msg + " sbmId", true, state.isSbmInd());
			assertTrue(msg + " ProrationTypeCd=" + state.getProrationTypeCd() + ", should be (0, 1, 2)", 
					state.getProrationTypeCd().equals("0") || state.getProrationTypeCd().equals("1") || state.getProrationTypeCd().equals("2"));
	// TODO add %'s to EPS
			//assertNotNull(msg + "ErrorThresholdPercent", state.getErrorThresholdPercent());
	
		}
		// Maintain alignment of TestDataSBMUtility.SBM_STATES to match EPS SBM STATEPRORATIONCONFIGURATION table.
		// The test state list is used in other tests.
		
		String sql = "SELECT STATECD FROM STATEPRORATIONCONFIGURATION WHERE SBMIND = 'Y' AND MARKETYEAR = '" + YEAR + "'";
		
		List<Map<String, Object>> rowList = jdbc.queryForList(sql);
	
		//assertEquals("SBM States with MARKETYEAR " + YEAR, TestDataSBMUtility.SBM_STATES.length, rowList.size());
	}
	
	@Test
	public void test_retrieveSbmBizRules() {

		String sql = "SELECT SBMBUSINESSRULETYPECD FROM SBMBUSINESSRULECONFIGURATION WHERE STATECD = '" + TestDataSBMUtility.SBM_STATES[0] + "'";
		
		List<String> rowList = jdbc.queryForList(sql, String.class);
		
	    List<String[]> expectedList = new ArrayList<String[]>();
	    
	    String bizRuleExp = rowList.get(0);
	    
	    String [] ruleConfig = {TestDataSBMUtility.SBM_STATES[0], bizRuleExp};
	    Collections.addAll(expectedList, ruleConfig);
		
		List<String []> actualList = sbmConfigDao.retrieveBusinessRules();
		
		assertNotNull("Business Rules.", actualList);
		
		boolean found = false;
		for(String [] businessRule : actualList) {
			
			String stateCd = businessRule[0];
			String bizRule = businessRule[1];
			
			if(stateCd.equals(TestDataSBMUtility.SBM_STATES[0]) && bizRule.equals(bizRuleExp)) {
				
				found = true;
				
				System.out.println("Retrieved business Rule R001 for " + TestDataSBMUtility.SBM_STATES[0]);
				break;
			} 
		}
		if(found) {
			assertTrue("Retrieved business Rule R001 for " + TestDataSBMUtility.SBM_STATES[0], true);
		}
	}
	
	//TODO - Uncomment when Ref data available
	//@Test
//	public void test_retrieveLanguageCodes() {
//		
//		String sql = "SELECT X12LANGUAGETYPECD FROM X12LANGUAGETYPE WHERE X12LANGUAGEQUALIFIERTYPECD IN('LD', 'LE')";
//		
//		List<String> rowList = jdbc.queryForList(sql, String.class);
//		
//	    String languageCodeExp = rowList.get(0);
//	    
//		List<String> actualList = sbmConfigDao.retrieveLanguageCodes();
//		
//		assertNotNull("LanguageCode", actualList);
//		
//		
//		if(actualList.contains(languageCodeExp)) {
//			assertTrue("Retrieved LanguageCode " + languageCodeExp, true);
//		}
//	}

}
