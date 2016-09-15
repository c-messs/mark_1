package gov.hhs.cms.ff.fm.eps.ep.sbm.mappers;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import gov.cms.dsh.sbmr.FileAcceptanceRejection;
import gov.cms.dsh.sbmr.SBMIPROCSUMType;
import gov.cms.dsh.sbmr.SBMIPROCSUMType.FinalRecordsProcessedSummary;
import gov.cms.dsh.sbmr.SBMIPROCSUMType.FinalRecordsProcessedSummary.TotalApproved;
import gov.cms.dsh.sbmr.SBMRHeaderType;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmFileProcessingSummaryPO;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmFileSummaryMissingPolicyData;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileProccessingSummary;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMSummaryAndFileInfoDTO;
import gov.hhs.cms.ff.fm.eps.ep.util.sbm.TestDataSBMUtility;

public class SbmFileProcessingSummaryMapperTest extends SBMBaseMapperTest {

	private SbmFileProcessingSummaryMapper mapper = new SbmFileProcessingSummaryMapper();


	@Test
	public void test_mapSbmToEps() {

		String tenantId = "RI0";
		String issuerId = "77777";
		String issuerFileSetId = "123777";
		SBMFileProccessingSummary expected = TestDataSBMUtility.makeSbmFileProcessingSummary(tenantId, issuerId, issuerFileSetId);

		SbmFileProcessingSummaryPO actual = mapper.mapSbmToEps(expected);

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
		assertEquals("CoverageYear", expected.getCoverageYear(), actual.getCoverageYear().intValue());
		assertEquals("SbmFileStatusTypeCd", expected.getSbmFileStatusType().getValue(), actual.getSbmFileStatusTypeCd());
	}

	@Test
	public void test_mapSbmToEps_Null_Status() {

		String tenantId = "RI0";
		String issuerId = "77777";
		String issuerFileSetId = "123777";
		SBMFileProccessingSummary expected = TestDataSBMUtility.makeSbmFileProcessingSummary(tenantId, issuerId, issuerFileSetId);
		expected.setSbmFileStatusType(null);
		SbmFileProcessingSummaryPO actual = mapper.mapSbmToEps(expected);

		assertEquals("SbmFileStatusTypeCd", expected.getSbmFileStatusType(), actual.getSbmFileStatusTypeCd());
	}

	@Test
	public void test_mapEpsToSbm() {

		String tenantId = "VT0";
		String issuerId = "66666";
		String issuerFileSetId = "123666";

		SbmFileProcessingSummaryPO expected = makeSbmFileProcessingSummaryPO(tenantId, issuerId, issuerFileSetId);
		SBMSummaryAndFileInfoDTO actual = mapper.mapEpsToSbm(expected);

		assertNotNull("SBMSummaryAndFileInfoDTO is NOT null", actual);
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
		assertEquals("CoverageYear", expected.getCoverageYear().intValue(), actual.getCoverageYear());
		assertEquals("SbmFileStatusTypeCd", expected.getSbmFileStatusTypeCd(), actual.getSbmFileStatusType().getValue());
	}

	@Test
	public void test_mapEpsToSbm_Null_Status() {

		String tenantId = "VT0";
		String issuerId = "66666";
		String issuerFileSetId = "123666";

		SbmFileProcessingSummaryPO expected = makeSbmFileProcessingSummaryPO(tenantId, issuerId, issuerFileSetId);
		expected.setSbmFileStatusTypeCd(null);
		SBMSummaryAndFileInfoDTO actual = mapper.mapEpsToSbm(expected);

		assertEquals("SbmFileStatusTypeCd", expected.getSbmFileStatusTypeCd(), actual.getSbmFileStatusType());
	}

	@Test
	public void test_mapEpsToSbm_EPS_null() {

		SbmFileProcessingSummaryPO expected = null;
		SBMSummaryAndFileInfoDTO actual = mapper.mapEpsToSbm(expected);
		assertEquals("SBMSummaryAndFileInfoDTO", expected, actual);
	}

	@Test
	public void test_mapEpsToSbm_List() {

		String tenantId = "VT0";
		String issuerId = "11111";
		String issuerFileSetId = "1111111";

		String tenantId2 = "CA0";
		String issuerId2 = "22222";
		String issuerFileSetId2 = "2222222";

		List<SbmFileProcessingSummaryPO> expectedList = new ArrayList<SbmFileProcessingSummaryPO>();
		SbmFileProcessingSummaryPO expected = makeSbmFileProcessingSummaryPO(tenantId, issuerId, issuerFileSetId);
		SbmFileProcessingSummaryPO expected2 = makeSbmFileProcessingSummaryPO(tenantId2, issuerId2, issuerFileSetId2);

		expectedList.add(expected);
		expectedList.add(expected2);

		List<SBMSummaryAndFileInfoDTO> actualList = mapper.mapEpsToSbm(expectedList);

		assertEquals("SBMSummaryAndFileInfoDTO list size", expectedList.size(), actualList.size());

		int i = 0;
		for (SBMSummaryAndFileInfoDTO actual : actualList) {
			expected = expectedList.get(i);
			assertEquals("IssuerFileSetId", expected.getIssuerFileSetId(), actual.getIssuerFileSetId());
			assertEquals("IssuerId", expected.getIssuerId(), actual.getIssuerId());
			assertEquals("TenantId", expected.getTenantId(), actual.getTenantId());
			i++;
		}
	}


	@Test
	public void test_mapEpsToSbmr() {

		String tenantId = "MN0";
		String issuerId = "33333";
		String issuerFileSetId = "123333";

		SbmFileProcessingSummaryPO expected = makeSbmFileProcessingSummaryPO(tenantId, issuerId, issuerFileSetId);
		expected.setSbmFileProcSumId(TestDataSBMUtility.getRandomNumberAsLong(3));
		
		List<SbmFileSummaryMissingPolicyData> missingPolicyDataList = new ArrayList<SbmFileSummaryMissingPolicyData>();

		FileAcceptanceRejection actual = mapper.mapEpsToSbmr(expected, false, missingPolicyDataList, 0);

		assertNotNull("FileAcceptanceRejection", actual);

		SBMRHeaderType actualHdr = actual.getSBMRHeader();
		assertNotNull("SBMRHeaderType", actualHdr);

		assertEquals("FileControlNumber", expected.getSbmFileProcSumId().intValue(), actualHdr.getFileControlNumber());
		assertNotNull("FileCreateDate", actualHdr.getFileCreateDate());
		assertEquals("IssuerFileSetId", expected.getIssuerFileSetId(), actualHdr.getIssuerFileSetId());
		assertEquals("IssuerId", expected.getIssuerId(), actualHdr.getIssuerId());
		assertEquals("TenantId", expected.getTenantId(), actualHdr.getTenantId());
		assertEquals("CoverageYear", expected.getCoverageYear().intValue(), actualHdr.getCoverageYear());
		assertEquals("TotalIssuerFiles", expected.getTotalIssuerFileCount(), actualHdr.getTotalIssuerFiles());
	}

	@Test
	public void test_mapEpsToSbmr_Update() {

		Long id = TestDataSBMUtility.getRandomNumberAsLong(3);
		String tenantId = "VT0";
		String issuerId = "4444";
		String issuerFileSetId = "123444";
		
		boolean isAccepted = true;

		SbmFileProcessingSummaryPO expected = makeSbmFileProcessingSummaryPO(tenantId, issuerId, issuerFileSetId);
		expected.setSbmFileProcSumId(id);

		int expectedTotalPreviousPoliciesNotSubmitted = 4;

		List<SbmFileSummaryMissingPolicyData> missingPolicyDataList = new ArrayList<SbmFileSummaryMissingPolicyData>();
		for (int i = 0; i < expectedTotalPreviousPoliciesNotSubmitted; ++i) {
			SbmFileSummaryMissingPolicyData missingPolicyData = new SbmFileSummaryMissingPolicyData();
			missingPolicyData.setExchangePolicyId("EXPOID-" + i);
			missingPolicyDataList.add(missingPolicyData);
		}
		
		int expectedTotalRecordProcessedCnt = 10;
		int expectedTotalRecordRejectedCnt = 4;
		int expectedEffectuatedPolicyCount = 5;
		int expectedMatchSame = expectedTotalRecordProcessedCnt - (expectedTotalRecordRejectedCnt + expected.getTotalPolicyApprovedCnt());
				
				
		expected.setTotalRecordProcessedCnt(expectedTotalRecordProcessedCnt);
		expected.setTotalRecordRejectedCnt(expectedTotalRecordRejectedCnt);
		expected.setEffectuatedPolicyCount(expectedEffectuatedPolicyCount);
		
		int expectedCountOfEffectuatedPoliciesCancelled = 0;
		
		FileAcceptanceRejection actual = mapper.mapEpsToSbmr(expected, isAccepted, missingPolicyDataList, expectedCountOfEffectuatedPoliciesCancelled);
		
		assertNotNull("FileAcceptanceRejection", actual);

		SBMIPROCSUMType actualSummary = actual.getSBMIPROCSUM();
		assertNotNull("SBMIPROCSUMType", actualSummary);
		
		assertEquals("TotalPreviousPoliciesNotSubmitted", expectedTotalPreviousPoliciesNotSubmitted, actualSummary.getTotalPreviousPoliciesNotSubmitted());
		
		FinalRecordsProcessedSummary actualFinalSum = actualSummary.getFinalRecordsProcessedSummary();
		assertNotNull("FinalRecordsProcessedSummary", actualFinalSum);
		
		assertEquals("CountOfEffectuatedPoliciesCancelled", expectedCountOfEffectuatedPoliciesCancelled, actualFinalSum.getCountOfEffectuatedPoliciesCancelled());
		
		TotalApproved actualTotAppr = actualFinalSum.getTotalApproved();
		assertNotNull("TotalApproved", actualTotAppr);
		
		assertEquals("CountOfEffectuatedPoliciesCancelled", expected.getTotalPolicyApprovedCnt().intValue(), actualTotAppr.getTotalPolicyRecordsApproved());
	}

}
