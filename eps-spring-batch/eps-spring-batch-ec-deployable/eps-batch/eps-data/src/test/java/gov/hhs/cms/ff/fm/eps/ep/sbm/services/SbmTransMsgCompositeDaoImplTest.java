package gov.hhs.cms.ff.fm.eps.ep.sbm.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import gov.cms.dsh.sbmi.PolicyType;
import gov.hhs.cms.ff.fm.eps.ep.enums.SbmTransMsgStatus;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileProcessingDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMPolicyDTO;
import gov.hhs.cms.ff.fm.eps.ep.util.sbm.TestDataSBMUtility;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/sbmi-data-config.xml", "classpath:/test-context-data.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class SbmTransMsgCompositeDaoImplTest extends BaseSbmServicesTest {


	@Autowired
	SbmTransMsgCompositeDao sbmTransMsgCompositeDao;

	@Test
	public void test_saveSbmTransMsg() {

		assertNotNull("SbmTransMsgCompositeDao is NOT null.", sbmTransMsgCompositeDao);
		int rcn = 4; 
		String qhpId = TestDataSBMUtility.makeQhpId("12345", "CA0");
		String exchangePolicyId = "88889999";
		int expectedListSize = 3;
		String state = "RI";
		String tenantId = state + "0";
		String sbmFileId = "FID-" + TestDataSBMUtility.getRandomNumberAsString(4);
		
		SBMPolicyDTO inboundPolicyDTO = new SBMPolicyDTO();
		PolicyType policy = TestDataSBMUtility.makePolicyType(rcn, qhpId, exchangePolicyId);
		inboundPolicyDTO.setPolicy(policy);
		inboundPolicyDTO.setBatchId(TestDataSBMUtility.getRandomNumberAsLong(4));
		
		String policyXml = TestDataSBMUtility.getPolicyAsXmlString(policy);
		inboundPolicyDTO.setPolicyXml(policyXml);
		
		// make schema errors
		for (int i = 0; i < expectedListSize; ++i) {
			inboundPolicyDTO.getSchemaErrorList().add(TestDataSBMUtility.makeSBMErrorDTO(i));
		}

		// make validation errors and warnings
		for (int i = 0; i < expectedListSize; ++i) {
			inboundPolicyDTO.getErrorList().add(TestDataSBMUtility.makeSbmErrWarningLogDTO(i));
		}
		
		SBMFileProcessingDTO parentDTO = insertParentFileRecords(tenantId, sbmFileId);
		Long sbmFileProcSumId = parentDTO.getSbmFileProcSumId();
		Long sbmFileInfoId = parentDTO.getSbmFileInfo().getSbmFileInfoId();
		Long stagingPolicyId   = insertStagingSbmPolicy(sbmFileProcSumId, sbmFileInfoId, policyXml);
		Long sbmTransMsgId = insertSbmTransMsg(sbmFileInfoId, state, exchangePolicyId, SbmTransMsgStatus.ACCEPTED_WITH_EPS_CHANGE);
		
		inboundPolicyDTO.setSbmTransMsgId(sbmTransMsgId);
		inboundPolicyDTO.setSbmFileInfoId(sbmFileInfoId);
		inboundPolicyDTO.setStagingSbmPolicyid(stagingPolicyId);
		
		sbmTransMsgCompositeDao.saveSbmTransMsg(inboundPolicyDTO);
	}


}

