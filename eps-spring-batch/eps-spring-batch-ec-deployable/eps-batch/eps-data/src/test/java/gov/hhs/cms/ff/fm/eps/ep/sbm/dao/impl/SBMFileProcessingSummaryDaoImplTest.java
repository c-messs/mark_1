package gov.hhs.cms.ff.fm.eps.ep.sbm.dao.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import gov.hhs.cms.ff.fm.eps.ep.dao.SbmFileProcessingSummaryDao;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmFileProcessingSummaryPO;
import gov.hhs.cms.ff.fm.eps.ep.util.sbm.TestDataSBMUtility;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/sbmi-data-config.xml", "classpath:/test-context-data.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class SBMFileProcessingSummaryDaoImplTest extends BaseSBMDaoTest {

	@Autowired
	private SbmFileProcessingSummaryDao sbmFileProcessingSummaryDao;

	private String userId = "SBMFileProcSumDaoTest";


	@Test
	public void test_insertSbmFileProcessingSummary() {
		
		userVO.setUserId(userId);
		String expectedTenantId = "CT0";
		String expectedIsserId = TestDataSBMUtility.getRandomNumberAsString(5);
		String expectedFileSetId = TestDataSBMUtility.getRandomNumberAsString(7);
		assertNotNull("sbmFileProcessingSummaryDao is NOT null (configuration)", sbmFileProcessingSummaryDao);
		
		SbmFileProcessingSummaryPO expected = makeSbmFileProcessingSummaryPO(expectedTenantId, expectedIsserId, expectedFileSetId);
		
	    Long sbmFileProcSumId = sbmFileProcessingSummaryDao.insertSbmFileProcessingSummary(expected);
	    	
	    SbmFileProcessingSummaryPO actual = sbmFileProcessingSummaryDao.selectSbmFileProcessingSummary(sbmFileProcSumId);

		assertNotNull("SBMFileProcessingSummaryPO is NOT null", actual);
		assertEquals("TotalIssuerFileCnt", expected.getTotalIssuerFileCount(), actual.getTotalIssuerFileCount());
		assertEquals("TotalRecordProcessedCount", expected.getTotalRecordProcessedCnt(), actual.getTotalRecordProcessedCnt());
		assertEquals("TotalRecordRejectedCount", expected.getTotalRecordRejectedCnt(), actual.getTotalRecordRejectedCnt());
		assertEquals("ErrorThresholdPercent", expected.getErrorThresholdPercent(), actual.getErrorThresholdPercent());
		assertEquals("TotalPreviousPoliciesNotSubmit", expected.getTotalPreviousPoliciesNotSubmit(), actual.getTotalPreviousPoliciesNotSubmit());
		assertEquals("CmsApprovedInd", expected.getCmsApprovedInd(), actual.getCmsApprovedInd());
		assertEquals("NotSubmittedEffectuatedCount", expected.getNotSubmittedEffectuatedCnt(), actual.getNotSubmittedEffectuatedCnt());
		assertEquals("NotSubmittedTerminatedCount", expected.getNotSubmittedTerminatedCnt(), actual.getNotSubmittedTerminatedCnt());
		assertEquals("NotSubmittedCancelledCount", expected.getNotSubmittedCancelledCnt(), actual.getNotSubmittedCancelledCnt());
		assertEquals("IssuerFileSetId", expected.getIssuerFileSetId(), actual.getIssuerFileSetId());
		assertEquals("IssuerId", expected.getIssuerId(), actual.getIssuerId());
		assertEquals("TenantId", expected.getTenantId(), actual.getTenantId());
		assertEquals("CmsApprovalRequiredInd", expected.getCmsApprovalRequiredInd(), actual.getCmsApprovalRequiredInd());
		assertEquals("TotalPolicyApprovedCount", expected.getTotalPolicyApprovedCnt(), actual.getTotalPolicyApprovedCnt());
		assertEquals("MatchingPlcNoChangeCnt", expected.getMatchingPlcNoChangeCnt(), actual.getMatchingPlcNoChangeCnt());
		assertEquals("MatchingPlcChgApplCnt", expected.getMatchingPlcChgApplCnt(), actual.getMatchingPlcChgApplCnt());
		assertEquals("MatchingPlcCorrectedChgApplCnt", expected.getMatchingPlcCorrectedChgApplCnt(), actual.getMatchingPlcCorrectedChgApplCnt());
		assertEquals("NewPolicyCreatedAsSentCnt", expected.getNewPlcCreatedAsSentCnt(), actual.getNewPlcCreatedAsSentCnt());
		assertEquals("NewPolicyCreatedCorrectionApplCnt", expected.getNewPlcCreatedCorrectionApplCnt(), actual.getNewPlcCreatedCorrectionApplCnt());
		assertEquals("EffectuatedPolicyCount", expected.getEffectuatedPolicyCount(), actual.getEffectuatedPolicyCount());
		assertEquals("CoverageYear", expected.getCoverageYear(), actual.getCoverageYear());
		
		
	}

	
}
