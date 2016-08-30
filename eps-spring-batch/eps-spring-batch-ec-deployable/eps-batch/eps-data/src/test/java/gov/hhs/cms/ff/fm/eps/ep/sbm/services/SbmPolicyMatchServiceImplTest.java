package gov.hhs.cms.ff.fm.eps.ep.sbm.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import gov.hhs.cms.ff.fm.eps.ep.po.PolicyVersionPO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMPolicyDTO;
import gov.hhs.cms.ff.fm.eps.ep.util.sbm.TestDataSBMUtility;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/sbmi-data-config.xml", "classpath:/test-context-data.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class SbmPolicyMatchServiceImplTest extends BaseSbmServicesTest {

	@Autowired
	private SbmPolicyMatchService policyMatchService;


	@Test
	public void test_findLatestPolicy() {

		assertNotNull("policyMatchService should not be null.", policyMatchService);
		
	//TODO update by adding parentFileInfo and StagingSBMPOlicy since now new check constraint added TRANSMSGVALIDATION_197308900
//		String state = TestDataSBMUtility.getRandomSbmState();
//		String tenantId = state + "0";
//		String exchangePolicyId = "EXPOLID-" + TestDataSBMUtility.getRandomNumberAsString(7);
//		String issuerHiosId = TestDataSBMUtility.getRandomNumberAsString(5);
//		String planId = TestDataSBMUtility.makeQhpId(issuerHiosId, tenantId).substring(0,  14);
//		
//		SBMPolicyDTO inboundDTO = new SBMPolicyDTO();
//		inboundDTO.setFileInfo(TestDataSBMUtility.makeFileInformationType(tenantId));
//		inboundDTO.setPolicy(TestDataSBMUtility.makePolicyType(tenantId, exchangePolicyId));
//
//		insertPolicyVersion(JAN_1_1am, JAN_1, state, exchangePolicyId, issuerHiosId, planId);
//
//		PolicyVersionPO actual = policyMatchService.findLatestPolicy(inboundDTO);
//		
//		assertNotNull("PolicyVersionPO should not be null", actual);
//		assertEquals("PlanId", planId, actual.getPlanID());
	}

}
