package gov.hhs.cms.ff.fm.eps.ep.sbm.mappers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import gov.cms.dsh.sbmr.FileAcceptanceRejection;
import gov.cms.dsh.sbmr.SBMIPROCSUMType;
import gov.cms.dsh.sbmr.SBMIPROCSUMType.FinalRecordsProcessedSummary;
import gov.cms.dsh.sbmr.SBMIPROCSUMType.FinalRecordsProcessedSummary.TotalApproved;
import gov.cms.dsh.sbmr.SBMRHeaderType;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmFileProcessingSummaryPO;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmFileSummaryMissingPolicyData;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileProccessingSummary;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMSummaryAndFileInfoDTO;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;

/**
 * @author j.radziewski
 *
 */
public class SbmFileProcessingSummaryMapper {

	/**
	 * 
	 * @param sbmFileProcSum
	 * @return SbmFileProcessingSummaryPO
	 */
	public SbmFileProcessingSummaryPO mapSbmToEps(SBMFileProccessingSummary sbmFileProcSum) {

		SbmFileProcessingSummaryPO po = new SbmFileProcessingSummaryPO();

		po.setSbmFileProcSumId(sbmFileProcSum.getSbmFileProcSumId());
		po.setTenantId(sbmFileProcSum.getTenantId());
		po.setIssuerFileSetId(sbmFileProcSum.getIssuerFileSetId());
		po.setIssuerId(sbmFileProcSum.getIssuerId());
		po.setTotalIssuerFileCount(sbmFileProcSum.getTotalIssuerFileCount());
		po.setCmsApprovalRequiredInd(sbmFileProcSum.getCmsApprovalRequiredInd());
		po.setCmsApprovedInd(sbmFileProcSum.getCmsApprovedInd());
		po.setTotalPreviousPoliciesNotSubmit(sbmFileProcSum.getTotalPreviousPoliciesNotSubmit());
		po.setNotSubmittedEffectuatedCnt(sbmFileProcSum.getNotSubmittedEffectuatedCnt());
		po.setNotSubmittedTerminatedCnt(sbmFileProcSum.getNotSubmittedTerminatedCnt());
		po.setNotSubmittedCancelledCnt(sbmFileProcSum.getNotSubmittedCancelledCnt());
		po.setTotalRecordProcessedCnt(sbmFileProcSum.getTotalRecordProcessedCnt());
		po.setTotalRecordRejectedCnt(sbmFileProcSum.getTotalRecordRejectedCnt());
		po.setTotalPolicyApprovedCnt(sbmFileProcSum.getTotalPolicyApprovedCnt());
		po.setMatchingPlcNoChangeCnt(sbmFileProcSum.getMatchingPlcNoChangeCnt());
		po.setMatchingPlcChgApplCnt(sbmFileProcSum.getMatchingPlcChgApplCnt());
		po.setMatchingPlcCorrectedChgApplCnt(sbmFileProcSum.getMatchingPlcCorrectedChgApplCnt());
		po.setNewPlcCreatedAsSentCnt(sbmFileProcSum.getNewPlcCreatedAsSentCnt());
		po.setNewPlcCreatedCorrectionApplCnt(sbmFileProcSum.getNewPlcCreatedCorrectionApplCnt());
		po.setEffectuatedPolicyCount(sbmFileProcSum.getEffectuatedPolicyCount());
		po.setErrorThresholdPercent(sbmFileProcSum.getErrorThresholdPercent());
		po.setCoverageYear(sbmFileProcSum.getCoverageYear());
		if (sbmFileProcSum.getSbmFileStatusType() != null) {
			po.setSbmFileStatusTypeCd(sbmFileProcSum.getSbmFileStatusType().getValue());
		}

		return po;
	}

	/**
	 * 
	 * @param po
	 * @return SBMSummaryAndFileInfoDTO
	 */
	public SBMSummaryAndFileInfoDTO mapEpsToSbm(SbmFileProcessingSummaryPO po) {

		SBMSummaryAndFileInfoDTO summaryDTO = null;

		if (po != null) {
			summaryDTO = new SBMSummaryAndFileInfoDTO();
			summaryDTO.setSbmFileProcSumId(po.getSbmFileProcSumId());
			summaryDTO.setTenantId(po.getTenantId());
			summaryDTO.setIssuerFileSetId(po.getIssuerFileSetId());
			summaryDTO.setIssuerId(po.getIssuerId());
			summaryDTO.setTotalIssuerFileCount(po.getTotalIssuerFileCount());
			summaryDTO.setCmsApprovalRequiredInd(po.getCmsApprovalRequiredInd());
			summaryDTO.setCmsApprovedInd(po.getCmsApprovedInd());
			summaryDTO.setTotalPreviousPoliciesNotSubmit(po.getTotalPreviousPoliciesNotSubmit());
			summaryDTO.setNotSubmittedEffectuatedCnt(po.getNotSubmittedEffectuatedCnt());
			summaryDTO.setNotSubmittedTerminatedCnt(po.getNotSubmittedTerminatedCnt());
			summaryDTO.setNotSubmittedCancelledCnt(po.getNotSubmittedCancelledCnt());
			summaryDTO.setTotalRecordProcessedCnt(po.getTotalRecordProcessedCnt());
			summaryDTO.setTotalRecordRejectedCnt(po.getTotalRecordRejectedCnt());
			summaryDTO.setTotalPolicyApprovedCnt(po.getTotalPolicyApprovedCnt());
			summaryDTO.setMatchingPlcNoChangeCnt(po.getMatchingPlcNoChangeCnt());
			summaryDTO.setMatchingPlcChgApplCnt(po.getMatchingPlcChgApplCnt());
			summaryDTO.setMatchingPlcCorrectedChgApplCnt(po.getMatchingPlcCorrectedChgApplCnt());
			summaryDTO.setNewPlcCreatedAsSentCnt(po.getNewPlcCreatedAsSentCnt());
			summaryDTO.setNewPlcCreatedCorrectionApplCnt(po.getNewPlcCreatedCorrectionApplCnt());
			summaryDTO.setEffectuatedPolicyCount(po.getEffectuatedPolicyCount());
			summaryDTO.setErrorThresholdPercent(po.getErrorThresholdPercent());
			summaryDTO.setCoverageYear(po.getCoverageYear());
			if (po.getSbmFileStatusTypeCd() != null) {
				summaryDTO.setSbmFileStatusType(SBMFileStatus.getEnum(po.getSbmFileStatusTypeCd()));
			}
		}

		return summaryDTO;
	}


	/**
	 * @param poList
	 * @return summaryDTOList
	 */
	public List<SBMSummaryAndFileInfoDTO> mapEpsToSbm(List<SbmFileProcessingSummaryPO> poList) {

		List<SBMSummaryAndFileInfoDTO> summaryDTOList = new ArrayList<SBMSummaryAndFileInfoDTO>();

		for (SbmFileProcessingSummaryPO po : poList) {

			SBMSummaryAndFileInfoDTO summaryDTO = mapEpsToSbm(po);

			summaryDTOList.add(summaryDTO);
		}

		return summaryDTOList;
	}


	/**
	 * @param po
	 * @param isApproved
	 * @param missingPolicyDataList
	 * @param cntEffPoliciesCancelled
	 * @return FileAcceptanceRejection
	 */
	public FileAcceptanceRejection mapEpsToSbmr(SbmFileProcessingSummaryPO po, boolean isApproved, List<SbmFileSummaryMissingPolicyData> missingPolicyDataList, int cntEffPoliciesCancelled) {

		FileAcceptanceRejection fileAR = new FileAcceptanceRejection();

		SBMRHeaderType hdr = new SBMRHeaderType();

		hdr.setFileControlNumber(po.getSbmFileProcSumId().intValue());
		hdr.setFileCreateDate(DateTimeUtil.getXMLGregorianCalendar(LocalDateTime.now()));
		hdr.setTenantId(po.getTenantId());
		hdr.setCoverageYear(po.getCoverageYear());
		hdr.setIssuerId(po.getIssuerId());
		
		// Only show (outbound SBMR) TotalIssuerFiles if there is an IssuerFileSet.  Do not show for Issuer Only and StateWide files.
		if(po.getIssuerFileSetId() != null) {
			hdr.setIssuerFileSetId(po.getIssuerFileSetId());
			hdr.setTotalIssuerFiles(po.getTotalIssuerFileCount());
		}

		fileAR.setSBMRHeader(hdr);

		SBMIPROCSUMType summary = new SBMIPROCSUMType();

		if (missingPolicyDataList != null) {

			summary.setTotalPreviousPoliciesNotSubmitted(missingPolicyDataList.size());
		}

		summary.setNotSubmittedEffectuated(po.getNotSubmittedEffectuatedCnt());
		summary.setNotSubmittedTerminated(po.getNotSubmittedTerminatedCnt());
		summary.setNotSubmittedCancelled(po.getNotSubmittedCancelledCnt());

		FinalRecordsProcessedSummary finalSummary = new FinalRecordsProcessedSummary();

		finalSummary.setTotalRecordsProcessed(po.getTotalRecordProcessedCnt());
		finalSummary.setTotalRecordsRejected(po.getTotalRecordRejectedCnt());
		finalSummary.setCountOfEffectuatedPoliciesCancelled(cntEffPoliciesCancelled);

		if (isApproved) {

			TotalApproved totAppr = new TotalApproved();

			//Number of records that passed XSD and business logic validation (with Warnings only- no Errors)
			totAppr.setTotalPolicyRecordsApproved(po.getTotalPolicyApprovedCnt());

			//Number of matching records in which no changes were applied to EPS due to no changes to the record since last SBMI submission
			// Meaning, this is the count of XPRs that were the same as a previous cycle and were not rejected.
			int matchPolicyNoChangeCnt = (po.getTotalRecordProcessedCnt() - (po.getTotalRecordRejectedCnt() + po.getTotalPolicyApprovedCnt()));
			totAppr.setMatchingPoliciesNoChangeRequired(matchPolicyNoChangeCnt);

			totAppr.setMatchingPoliciesChangeApplied(po.getMatchingPlcChgApplCnt());
			totAppr.setMatchingPoliciesCorrectedChangeApplied(po.getMatchingPlcCorrectedChgApplCnt());
			totAppr.setNewPoliciesCreatedAsSent(po.getNewPlcCreatedAsSentCnt());
			totAppr.setNewPoliciesCreatedWithCorrectionApplied(po.getNewPlcCreatedCorrectionApplCnt());

			finalSummary.setTotalApproved(totAppr);
		}

		summary.setFinalRecordsProcessedSummary(finalSummary);

		fileAR.setSBMIPROCSUM(summary);

		return fileAR;
	}





}
