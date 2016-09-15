package gov.hhs.cms.ff.fm.eps.ep.sbm.mappers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

import gov.hhs.cms.ff.fm.eps.ep.enums.PolicyStatus;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmFileInfoPO;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmFileProcessingSummaryPO;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmPolicyStatusPO;
import gov.hhs.cms.ff.fm.eps.ep.util.sbm.TestDataSBMUtility;
import junit.framework.TestCase;

public abstract class SBMBaseMapperTest extends TestCase {

	protected final LocalDate DATE = LocalDate.now();
	protected final LocalDateTime DATETIME = getLocalDateTime();
	protected final int YEAR = DATETIME.getYear();

	protected final LocalDate JAN_1 = LocalDate.of(YEAR, 1, 1);
	protected final LocalDate JAN_15 = LocalDate.of(YEAR, 1, 15);
	protected final LocalDate JAN_16 = LocalDate.of(YEAR, 1, 16);
	protected final LocalDate JAN_31 = LocalDate.of(YEAR, 1, 31);
	protected final LocalDate FEB_1 = LocalDate.of(YEAR, 2, 1);
	protected final LocalDate FEB_MAX = DATE.with(TemporalAdjusters.lastDayOfMonth());
	protected final LocalDate MAR_1 = LocalDate.of(YEAR, 3, 1);
	protected final LocalDate MAR_14 = LocalDate.of(YEAR, 3, 14);
	protected final LocalDate MAR_15 = LocalDate.of(YEAR, 3, 15);
	protected final LocalDate MAR_31 = LocalDate.of(YEAR, 3, 31);
	protected final LocalDate APR_1 = LocalDate.of(YEAR, 4, 1);
	protected final LocalDate APR_20 = LocalDate.of(YEAR, 4, 20);
	protected final LocalDate JUN_30 = LocalDate.of(YEAR, 6, 30);
	protected final LocalDate DEC_31 = LocalDate.of(YEAR, 12, 31);

	protected final LocalDateTime JAN_1_1am = LocalDateTime.of(YEAR, 1, 1, 1, 0, 0, 111111000);
	protected final LocalDateTime FEB_1_2am = LocalDateTime.of(YEAR, 2, 1, 2, 0, 0, 222222000);
	protected final LocalDateTime MAR_1_3am = LocalDateTime.of(YEAR, 3, 1, 3, 0, 0, 333333000);
	protected final LocalDateTime APR_1_4am = LocalDateTime.of(YEAR, 4, 1, 4, 0, 0, 444444000);


	private  LocalDateTime getLocalDateTime() {

		return LocalDateTime.now();
	}


	protected SbmFileProcessingSummaryPO makeSbmFileProcessingSummaryPO(String tenantId, String issuerId, String issuerFileSetId) {

		SbmFileProcessingSummaryPO po = new SbmFileProcessingSummaryPO();
		po.setTenantId(tenantId);
		po.setIssuerId(issuerId);
		po.setIssuerFileSetId(issuerFileSetId);
		po.setCmsApprovedInd("Y");
		po.setCmsApprovalRequiredInd("N");
		po.setTotalIssuerFileCount(TestDataSBMUtility.getRandomNumber(3));
		po.setTotalRecordProcessedCnt(TestDataSBMUtility.getRandomNumber(4));
		po.setTotalRecordRejectedCnt(TestDataSBMUtility.getRandomNumber(4));
		po.setErrorThresholdPercent(new BigDecimal(".1"));
		po.setTotalPreviousPoliciesNotSubmit(TestDataSBMUtility.getRandomNumber(4));
		po.setNotSubmittedEffectuatedCnt(TestDataSBMUtility.getRandomNumber(4));
		po.setNotSubmittedTerminatedCnt(TestDataSBMUtility.getRandomNumber(4));
		po.setNotSubmittedCancelledCnt(TestDataSBMUtility.getRandomNumber(4));
		po.setTotalPolicyApprovedCnt(TestDataSBMUtility.getRandomNumber(4));
		po.setMatchingPlcNoChangeCnt(TestDataSBMUtility.getRandomNumber(4));
		po.setMatchingPlcChgApplCnt(TestDataSBMUtility.getRandomNumber(4));
		po.setMatchingPlcCorrectedChgApplCnt(TestDataSBMUtility.getRandomNumber(4));
		po.setNewPlcCreatedAsSentCnt(TestDataSBMUtility.getRandomNumber(2));
		po.setNewPlcCreatedCorrectionApplCnt(TestDataSBMUtility.getRandomNumber(2));
		po.setEffectuatedPolicyCount(TestDataSBMUtility.getRandomNumber(7));
		po.setCoverageYear(YEAR);
		po.setSbmFileStatusTypeCd(SBMFileStatus.IN_PROCESS.getValue());
		return po;
	}


	protected SbmFileInfoPO makeSBMFileInfoPO(Long sbmFileProcSumId) {

		SbmFileInfoPO po = new SbmFileInfoPO();
		po.setSbmFileProcessingSummaryId(sbmFileProcSumId);
		po.setSbmFileNm("FILENM-" + sbmFileProcSumId.toString());
		po.setSbmFileCreateDateTime(LocalDateTime.now());
		po.setSbmFileId("FILEID-" + sbmFileProcSumId.toString());
		po.setSbmFileNum(Integer.valueOf(1));
		// VARCHAR2(10 BYTE)
		po.setIssuerFileSetId("FILESETID");
		po.setTradingPartnerId("TRADEPARTNR-" + sbmFileProcSumId.toString());
		po.setFunctionCd("FUNC_CD-" + sbmFileProcSumId.toString());
		return po;
	}

	protected SbmPolicyStatusPO makeSbmPolicyStatusPO(LocalDateTime transDateTime, PolicyStatus status) {

		SbmPolicyStatusPO po = new SbmPolicyStatusPO();

		po.setTransDateTime(transDateTime);
		po.setInsuranacePolicyStatusTypeCd(status.getValue());

		return po;

	}


}
