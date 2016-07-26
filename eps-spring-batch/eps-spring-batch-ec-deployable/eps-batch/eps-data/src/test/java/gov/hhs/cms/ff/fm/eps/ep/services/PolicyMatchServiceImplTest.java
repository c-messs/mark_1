package gov.hhs.cms.ff.fm.eps.ep.services;

import gov.cms.dsh.bem.BenefitEnrollmentMaintenanceType;
import gov.cms.dsh.bem.MemberType;
import gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentMaintenanceDTO;
import gov.hhs.cms.ff.fm.eps.ep.data.util.TestDataUtil;
import gov.hhs.cms.ff.fm.eps.ep.enums.EProdEnum;
import gov.hhs.cms.ff.fm.eps.ep.services.impl.FFMDataServiceImpl;
import gov.hhs.cms.ff.fm.eps.ep.services.impl.PolicyMatchServiceImpl;
import gov.hhs.cms.ff.fm.eps.ep.vo.PolicyVersionSearchCriteriaVO;

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
public class PolicyMatchServiceImplTest extends BaseServicesTest {

	@Autowired
	FFMDataServiceImpl policyDataService;

	@Autowired
	PolicyMatchServiceImpl policyMatchService;

	private final Long MEM_ID_OFFSET = Long.valueOf("1000000");
	private final String DAD = "DAD";
	private final String SON = "SON";


	private BenefitEnrollmentMaintenanceDTO insertBemForPolicyMatch(Long bemId, Long memId, Long transMsgId,
			String hiosId, String state, String exchangePolicyId) {

		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		BenefitEnrollmentMaintenanceType bem = TestDataUtil.makeBenefitEnrollmentMaintenanceType(bemId, exchangePolicyId);

		// make a Subscriber
		MemberType subscriber = TestDataUtil.makeMemberTypeSubscriber(memId, DAD, hiosId, state);
		MemberType member = TestDataUtil.makeMemberType(memId + MEM_ID_OFFSET, SON, false);

		bem.getMember().add(subscriber);
		bem.getMember().add(member);

		bemDTO.setTransMsgId(transMsgId);
		bemDTO.setBem(bem);

		//Save the BEM INITIAL
		policyDataService.saveBEM(bemDTO);

		// return to test for comparing for debugging.
		return bemDTO;
	}


	@Test
	public void testPolicyVersionSearchCriteriaVO_FFM() {

		assertNotNull("PolicyVersionMapper", policyDataService);

		Long bemId = TestDataUtil.getRandom3DigitNumber();
		Long memId = TestDataUtil.getRandom3DigitNumber();

		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();

		String expectedExchangePolicyId = "77777777";
		String expectedHiosId = "11111";
		String expectedStateCd = "TX";

		BenefitEnrollmentMaintenanceType expectedBem = TestDataUtil.makeBenefitEnrollmentMaintenanceType(bemId, expectedExchangePolicyId);
		MemberType expectedSubscriber = TestDataUtil.makeMemberTypeSubscriber(memId, "DAD", expectedHiosId, expectedStateCd);

		expectedBem.getMember().add(expectedSubscriber);
		bemDTO.setBem(expectedBem);

		PolicyVersionSearchCriteriaVO actualCriteria = policyMatchService.getSearchCriteriaPolicyId(bemDTO);
		

		assertNotNull("PolicyVersionSearchCriteriaVO", actualCriteria);
		assertNotNull("ExchangePolicyId (GroupPolicyNumber)",  actualCriteria.getExchangePolicyId());
		assertEquals("ExchangePolicyId (GroupPolicyNumber)", expectedExchangePolicyId, actualCriteria.getExchangePolicyId());
		assertNotNull("subscriberStateCd",  actualCriteria.getSubscriberStateCd());
		assertEquals("subscriberStateCd", expectedStateCd, 	actualCriteria.getSubscriberStateCd());	
		
		String logMsg = actualCriteria.getLogMessage();
		assertTrue("LogMsg contains ExchangePolicyId", logMsg.contains(actualCriteria.getExchangePolicyId()));
		assertTrue("LogMsg contains stateCd", logMsg.contains(actualCriteria.getSubscriberStateCd()));
	}


	/*
	 * Test Policy Matching by Exchange policy id and subscriber state code.
	 * 
	 * - Tests "Policy Match FOUND"
	 * - Tests "NO Policy Match" for each criteria field with incorrect data
	 * - Tests "NO Policy Match" for each criteria field with null data
	 */
	@Test
	public void test_policyMatchByPolicyId() throws InterruptedException {

		Long bemId = TestDataUtil.getRandomNumber(9);
		Long memId = bemId;

		String expectedExchangePolicyId = bemId.toString();
		String expectedHiosId = TestDataUtil.getRandomNumberAsString(5);
		String expectedStateCd = "TX";

		Long transMsgId = insertTransMsg();

		// Use bemInitial to compare for debugging
		BenefitEnrollmentMaintenanceDTO bemDTO_INI = insertBemForPolicyMatch(bemId, memId, transMsgId, expectedHiosId, expectedStateCd, expectedExchangePolicyId);
		assertNotNull("BEM Initial", bemDTO_INI.getBem());

		Thread.sleep(1500);

		// Make another BEM which will be search criteria using same Ids to which create same values.
		BenefitEnrollmentMaintenanceDTO inboundBemDTO = new BenefitEnrollmentMaintenanceDTO();
		BenefitEnrollmentMaintenanceType bem_EFF = TestDataUtil.makeBenefitEnrollmentMaintenanceType(bemId, expectedExchangePolicyId);

		MemberType memType = TestDataUtil.makeMemberType(memId, "DAD", true, expectedHiosId, expectedStateCd);
		bem_EFF.getMember().add(memType);
		inboundBemDTO.setBem(bem_EFF);

		BenefitEnrollmentMaintenanceDTO actualBemDTO = policyDataService.getLatestBEMByPolicyId(inboundBemDTO);

		assertNotNull("Matching BEM Found", actualBemDTO.getBem());

		// Now confirm each criteria arg, when different will NOT return a matching policy

		// Change the ExchangePolicyId
		inboundBemDTO.getBem().getPolicyInfo().setGroupPolicyNumber(expectedExchangePolicyId + "XXXX");
		actualBemDTO = policyDataService.getLatestBEMByPolicyId(inboundBemDTO);
		assertNull("Matching BEM NOT Found with different ExchangePolicyId (GroupPolicyNumber)", actualBemDTO.getBem());
		// Set to null
		inboundBemDTO.getBem().getPolicyInfo().setGroupPolicyNumber(null);
		actualBemDTO = policyDataService.getLatestBEMByPolicyId(inboundBemDTO);
		assertNull("Matching BEM NOT Found with NULL ExchangePolicyId (GroupPolicyNumber)", actualBemDTO.getBem());

		// Change the StateCd
		inboundBemDTO.getBem().getPolicyInfo().setGroupPolicyNumber(expectedExchangePolicyId);
		inboundBemDTO.getBem().getMember().get(0).getHealthCoverage().get(0).getHealthCoveragePolicyNumber().setContractCode(expectedHiosId + "CA"+ expectedExchangePolicyId);
		actualBemDTO = policyDataService.getLatestBEMByPolicyId(inboundBemDTO);
		assertNull("Matching BEM NOT Found with different ExchangePolicyId (GroupPolicyNumber)", actualBemDTO.getBem());
		// Set to null
		inboundBemDTO.getBem().getMember().get(0).getHealthCoverage().get(0).getHealthCoveragePolicyNumber().setContractCode(null);
		actualBemDTO = policyDataService.getLatestBEMByPolicyId(inboundBemDTO);
		assertNull("Matching BEM NOT Found with NULL ExchangePolicyId (GroupPolicyNumber)", actualBemDTO.getBem());
	}


	/*
	 * Test Policy Matching by Exchange policy id and subscriber state code.
	 * - Test EPROD-13
	 */
	@Test
	public void test_policyMatchByPolicyId_Exception() throws InterruptedException {

		String expectedEPROD = EProdEnum.EPROD_13.getCode();
		Long bemId = TestDataUtil.getRandomNumber(9);
		Long memId = bemId;

		String expectedExchangePolicyId = bemId.toString();
		String expectedHiosId = TestDataUtil.getRandomNumberAsString(5);
		String expectedStateCd = "TX";

		// Insert 2 of the same policy.
		Long transMsgId_1 = insertTransMsg();
		BenefitEnrollmentMaintenanceDTO bemDTO_1 = insertBemForPolicyMatch(bemId, memId, transMsgId_1, expectedHiosId, expectedStateCd, expectedExchangePolicyId);
		assertNotNull("BEM Initial", bemDTO_1.getBem());

		Thread.sleep(1500);

		// Since this bemDTO does not have a policyVersionId, the DAO will not set the MAINTENANCEENDDATETIME of the previous policy to 
		// one second less.  Both policies will have MAINTENANCEENDDATETIME == HIGHDATE.
		Long transMsgId_2 = insertTransMsg();
		BenefitEnrollmentMaintenanceDTO bemDTO_2 = insertBemForPolicyMatch(bemId, memId, transMsgId_2, expectedHiosId, expectedStateCd, expectedExchangePolicyId);
		assertNotNull("BEM Initial", bemDTO_2.getBem());

		Thread.sleep(1500);		

		// Make another BEM which will be search criteria using same Ids to which create same values.
		BenefitEnrollmentMaintenanceDTO inboundBemDTO = new BenefitEnrollmentMaintenanceDTO();
		BenefitEnrollmentMaintenanceType bem_EFF = TestDataUtil.makeBenefitEnrollmentMaintenanceType(bemId, expectedExchangePolicyId);

		MemberType memType = TestDataUtil.makeMemberType(memId, "DAD", true, expectedHiosId, expectedStateCd);
		bem_EFF.getMember().add(memType);
		inboundBemDTO.setBem(bem_EFF);

		BenefitEnrollmentMaintenanceDTO actualBemDTO = null;
		try {

			actualBemDTO = policyDataService.getLatestBEMByPolicyId(inboundBemDTO);

		} catch (ApplicationException appEx) {

			assertEquals("ApplicationException EPROD", expectedEPROD, appEx.getMessage());
		}

		assertNull("Matching BEM NOT Found and return BemDTO is null.", actualBemDTO);
	}

}
