package gov.hhs.cms.ff.fm.eps.ep.sbm.services;

import java.math.BigDecimal;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import gov.cms.dsh.sbmi.PolicyMemberType;
import gov.cms.dsh.sbmi.PolicyType;
import gov.hhs.cms.ff.fm.eps.ep.enums.SbmTransMsgStatus;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileProcessingDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMPolicyDTO;
import gov.hhs.cms.ff.fm.eps.ep.util.sbm.TestDataSBMUtility;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/sbmi-data-config.xml", "classpath:/test-context-data.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class SbmDataServiceImplTest extends BaseSbmServicesTest {

	@Autowired
	private SBMDataService sbmDataService;

	@Test
	public void test_Config() {
		
		assertNotNull("sbmDataService should not be null", sbmDataService);
	}

	@Test
	public void test_performPolicyMatch() {

		String state = TestDataSBMUtility.getRandomSbmState();
		String tenantId = state + "0";
		String exchangePolicyId = "EXPOLID-" + TestDataSBMUtility.getRandomNumberAsString(7);
		String issuerHiosId = TestDataSBMUtility.getRandomNumberAsString(5);
		String qhpId = TestDataSBMUtility.makeQhpId(issuerHiosId, tenantId).substring(0,  14);
		String sbmFileId = "FID-12345";
		SBMFileProcessingDTO fileDTO = insertParentFileRecords(tenantId, sbmFileId); 
		
		Long sbmFileInfoId = fileDTO.getSbmFileInfo().getSbmFileInfoId();
		SbmTransMsgStatus status = SbmTransMsgStatus.ACCEPTED_WITH_CHANGES;
		
		Long sbmTransMsgId = insertSbmTransMsg(sbmFileInfoId, state, exchangePolicyId, status);
		
		String expectedExAssMemId1 = TestDataSBMUtility.getRandomNumberAsString(6);
		String expectedExAssMemId2 = TestDataSBMUtility.getRandomNumberAsString(6);

		Long expectedPvId = insertPolicyVersion(JAN_1_1am, JAN_1, DEC_31, state, exchangePolicyId, qhpId, sbmTransMsgId);
		
		Long pmvId1 = insertPolicyMemberVersion(state, expectedExAssMemId1, JAN_1_1am);
		Long pmvId2 = insertPolicyMemberVersion(state, expectedExAssMemId2, JAN_1_1am);
		
		insertPolicyMember(state, expectedPvId, pmvId1);
		insertPolicyMember(state, expectedPvId, pmvId2);

		SBMPolicyDTO inboundPolicyDTO = new SBMPolicyDTO();
		inboundPolicyDTO.setBatchId(Long.valueOf("777777"));
		inboundPolicyDTO.setFileInfo(TestDataSBMUtility.makeFileInformationType(tenantId, issuerHiosId, TestDataSBMUtility.FILES_ONE_PER_ISSUER));
		inboundPolicyDTO.setPolicy(TestDataSBMUtility.makePolicyType(tenantId, exchangePolicyId));
		inboundPolicyDTO.setSbmFileInfoId(sbmFileInfoId);
	
		SBMPolicyDTO actual = sbmDataService.performPolicyMatch(inboundPolicyDTO);

		assertNotNull("SBMPolicyDTO", actual);
		assertEquals("PolicyVersionId", expectedPvId, actual.getPolicyVersionId());
		
		PolicyType actualPolicy = actual.getPolicy();
		 
		assertEquals("QHP Id", qhpId, actualPolicy.getQHPId());
		assertEquals("InsuranceLineCode","HLT", actualPolicy.getInsuranceLineCode());	
		
		PolicyMemberType member1 = actual.getPolicy().getMemberInformation().get(0);
		PolicyMemberType member2 = actual.getPolicy().getMemberInformation().get(1);
		
		assertEquals("ExchangeAssignedMemberId 1", expectedExAssMemId1, member1.getExchangeAssignedMemberId());
		assertEquals("ExchangeAssignedMemberId 2", expectedExAssMemId2, member2.getExchangeAssignedMemberId());
	}
	
	@Test
	public void test_performPolicyMatch_NoMatch() {

		String state = TestDataSBMUtility.getRandomSbmState();
		String tenantId = state + "0";
		String exchangePolicyId = "EXPOLID-" + TestDataSBMUtility.getRandomNumberAsString(7);
		String issuerHiosId = TestDataSBMUtility.getRandomNumberAsString(5);
		
		SBMPolicyDTO inboundPolicyDTO = new SBMPolicyDTO();
		inboundPolicyDTO.setBatchId(Long.valueOf("777777"));
		inboundPolicyDTO.setFileInfo(TestDataSBMUtility.makeFileInformationType(tenantId, issuerHiosId, TestDataSBMUtility.FILES_ONE_PER_ISSUER));
		inboundPolicyDTO.setPolicy(TestDataSBMUtility.makePolicyType(tenantId, exchangePolicyId));
		SBMPolicyDTO actual = sbmDataService.performPolicyMatch(inboundPolicyDTO);

		assertNotNull("SBMPolicyDTO", actual);
		assertNull("PolicyVersionId", actual.getPolicyVersionId());
	}
	
	
	@Test
	public void test_checkQhpIdExistsForPolicyYear() {
		
		boolean expected = false;
		String state = TestDataSBMUtility.getRandomSbmState();
		String tenantId = state + "0";
		String issuerHiosId = TestDataSBMUtility.getRandomNumberAsString(5);
		String qhpId = TestDataSBMUtility.makeQhpId(issuerHiosId, tenantId).substring(0,  14);
		boolean actual = sbmDataService.checkQhpIdExistsForPolicyYear(qhpId, String.valueOf(YEAR));
		assertEquals("checkQhpIdExistsForPolicyYear", expected, actual);	
	}

	@Test
	public void test_getMetalLevelByQhpid() {
		
		String expected = null;
		String state = TestDataSBMUtility.getRandomSbmState();
		String tenantId = state + "0";
		String issuerHiosId = TestDataSBMUtility.getRandomNumberAsString(5);
		String qhpId = TestDataSBMUtility.makeQhpId(issuerHiosId, tenantId).substring(0,  14);
		String actual = sbmDataService.getMetalLevelByQhpid(qhpId, String.valueOf(YEAR));
		assertEquals("getMetalLevelByQhpid", expected, actual);	
	}
	
	@Test
	public void test_getCsrMultiplierByVariantAndMetal() {
		
		String expectedMsg = "Incorrect result size: expected 1, actual 0";
		BigDecimal expected = null;
		BigDecimal actual = null;
		String variantId = "01";
		String metal = "XXXX";
		try {
			sbmDataService.getCsrMultiplierByVariantAndMetal(variantId, metal, String.valueOf(YEAR));
		} catch (EmptyResultDataAccessException erdaEx) {
			assertEquals("Error Msg", expectedMsg, erdaEx.getMessage());
		}
		assertEquals("getCsrMultiplierByVariantAndMetal", expected, actual);	
	}
	
	@Test
	public void test_getCsrMultiplierByVariant() {
		
		String expectedMsg = "Incorrect result size: expected 1, actual 0";
		BigDecimal expected = null;
		BigDecimal actual = null;
		String variantId = "02";
		try {
			actual = sbmDataService.getCsrMultiplierByVariant(variantId, String.valueOf(YEAR));
		} catch (EmptyResultDataAccessException erdaEx) {
			assertEquals("Error Msg", expectedMsg, erdaEx.getMessage());
		}
		assertEquals("getCsrMultiplierByVariant", expected, actual);	
	}

}
