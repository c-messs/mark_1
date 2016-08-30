package gov.hhs.cms.ff.fm.eps.ep.sbm.services.impl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.cms.dsh.sbmr.FileAcceptanceRejection;
import gov.cms.dsh.sbmr.FileInformationType;
import gov.cms.dsh.sbmr.PolicyErrorType;
import gov.hhs.cms.ff.fm.eps.ep.dao.SbmFileInfoDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.SbmFileProcessingSummaryDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.SbmFileSummaryMissingPolicyDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.SbmPolicyMemberDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.SbmPolicyMemberDateDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.SbmPolicyMemberVersionDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.SbmPolicyPremiumDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.SbmPolicyStatusDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.SbmPolicyVersionDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.SbmResponseDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.SbmTransMsgAdditionalErrorInfoDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.SbmTransMsgDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.SbmTransMsgValidationDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.StagingSbmFileDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.StagingSbmGroupLockDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.StagingSbmPolicyDao;
import gov.hhs.cms.ff.fm.eps.ep.enums.PolicyStatus;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMResponsePhaseTypeCode;
import gov.hhs.cms.ff.fm.eps.ep.enums.SbmTransMsgStatus;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmFileInfoPO;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmFileProcessingSummaryPO;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmFileSummaryMissingPolicyData;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmResponsePO;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmTransMsgAdditionalErrorInfoPO;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmTransMsgPO;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmTransMsgValidationPO;
import gov.hhs.cms.ff.fm.eps.ep.po.StagingSbmGroupLockPO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMCache;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMSummaryAndFileInfoDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SbmDataUtil;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SbmResponseDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.mappers.SbmFileInfoMapper;
import gov.hhs.cms.ff.fm.eps.ep.sbm.mappers.SbmFileProcessingSummaryMapper;
import gov.hhs.cms.ff.fm.eps.ep.sbm.mappers.SbmFileSummaryMissingPolicyMapper;
import gov.hhs.cms.ff.fm.eps.ep.sbm.mappers.SbmTransMsgAdditionalErrorInfoMapper;
import gov.hhs.cms.ff.fm.eps.ep.sbm.mappers.SbmTransMsgMapper;
import gov.hhs.cms.ff.fm.eps.ep.sbm.mappers.SbmTransMsgValidationMapper;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SbmResponseCompositeDao;
import gov.hhs.cms.ff.fm.eps.ep.vo.UserVO;

public class SbmResponseCompositeDaoImpl implements SbmResponseCompositeDao {

	private final static Logger LOG = LoggerFactory.getLogger(SbmResponseCompositeDaoImpl.class);

	private SbmFileProcessingSummaryDao sbmFileProcSumDao;
	private SbmFileInfoDao sbmFileInfoDao;
	private SbmTransMsgDao sbmTransMsgDao;
	private SbmTransMsgValidationDao sbmTransMsgValidationDao;
	private SbmTransMsgAdditionalErrorInfoDao sbmTransMsgAddlErrInfoDao;
	private SbmFileSummaryMissingPolicyDao sbmFileSumMissingPolicyDao;

	private StagingSbmFileDao stagingSbmFileDao;
	private StagingSbmPolicyDao stagingSbmPolicyDao;
	private StagingSbmGroupLockDao stagingSbmGroupLockDao;

	private SbmPolicyVersionDao policyDao;
	private SbmPolicyPremiumDao premiumDao;
	private SbmPolicyStatusDao statusDao;

	private SbmPolicyMemberDao joinDao;

	private SbmPolicyMemberVersionDao memberDao;
	private SbmPolicyMemberDateDao dateDao;

	private SbmFileProcessingSummaryMapper sbmFileProcSumMapper;
	private SbmFileInfoMapper sbmFileInfoMapper;
	private SbmTransMsgMapper sbmTransMsgMapper;
	private SbmTransMsgValidationMapper sbmTransMsgValidationMapper;
	private SbmTransMsgAdditionalErrorInfoMapper sbmTransMsgAddlErrInfoMapper;
	private SbmFileSummaryMissingPolicyMapper sbmFileSumMissingPolicyMapper;

	private SbmResponseDao sbmResponseDao;

	private UserVO userVO;


	@Override
	public SbmResponseDTO generateSBMR(Long sbmFileProcSumId) {

		SbmResponseDTO responseDTO = new SbmResponseDTO();
		List<SbmFileSummaryMissingPolicyData> missingPolicyDataList = null;

		if (SBMCache.getJobExecutionId() != null) {
			userVO.setUserId(SBMCache.getJobExecutionId().toString());
		}

		SbmFileProcessingSummaryPO epsSummaryPO = sbmFileProcSumDao.selectSbmFileProcessingSummary(sbmFileProcSumId);
		
		//TODO add condition to only insert missing policies when not backing out.

		// First, find/extract missing policies that are in the files in StagingSbmFile but 
		// not in EPS PolicyVersion.  Then insert the ids into SbmFileSummaryMissingPolicy table.
		sbmFileSumMissingPolicyDao.findAndInsertMissingPolicy(epsSummaryPO);
		sbmFileSumMissingPolicyDao.findAndInsertMissingMember(epsSummaryPO);

		// Retrieve the missing policy data for the outbound response and for Termination count determination.
		missingPolicyDataList = sbmFileSumMissingPolicyDao.selectMissingPolicyList(sbmFileProcSumId);

		getMissingPolicyTotals(epsSummaryPO, missingPolicyDataList);

		getSummaryTotals(epsSummaryPO);

		List<SbmFileInfoPO> filePOList = sbmFileInfoDao.getSbmFileInfoList(sbmFileProcSumId);
		epsSummaryPO.setTotalIssuerFileCount(filePOList.size());

		boolean isAccepted = false;
		
		FileAcceptanceRejection fileAR = sbmFileProcSumMapper.mapEpsToSbmr(epsSummaryPO, isAccepted, missingPolicyDataList);
		fileAR.getMissingPolicy().addAll(sbmFileSumMissingPolicyMapper.mapEpsToSbmr(missingPolicyDataList));


		for (SbmFileInfoPO filePO : filePOList) {

			FileInformationType fileInfo = sbmFileInfoMapper.mapEpsToSbmr(filePO, epsSummaryPO.getSbmFileStatusTypeCd());

			List<SbmTransMsgPO> sbmTransMsgPOList = sbmTransMsgDao.selectSbmTransMsg(filePO.getSbmFileInfoId());

			for (SbmTransMsgPO sbmTransMsgPO : sbmTransMsgPOList) {

				PolicyErrorType policyError = sbmTransMsgMapper.mapEpsToSbmr(sbmTransMsgPO);

				List<SbmTransMsgValidationPO> validationPOList = sbmTransMsgValidationDao.selectValidation(sbmTransMsgPO.getSbmTransMsgId());
				
				if(CollectionUtils.isEmpty(validationPOList)) {
					//No validation errors
					continue;
				}

				for (SbmTransMsgValidationPO valPO : validationPOList) {

					PolicyErrorType.Error err = sbmTransMsgValidationMapper.mapEpsToSbmr(valPO);

					List<SbmTransMsgAdditionalErrorInfoPO> addlErrInfoPOList = sbmTransMsgAddlErrInfoDao.selectSbmTransMsgAddlErrInfo(valPO);

					err.getAdditionalErrorInfo().addAll(sbmTransMsgAddlErrInfoMapper.mapEpsToSbmr(addlErrInfoPOList));

					policyError.getError().add(err);
				}
				fileInfo.getSBMIDetail().add(policyError);
			}

			fileAR.getSBMIFileInfo().add(fileInfo);
		}

		SBMSummaryAndFileInfoDTO summaryDTO = sbmFileProcSumMapper.mapEpsToSbm(epsSummaryPO);

		summaryDTO.getSbmFileInfoList().addAll(sbmFileInfoMapper.mapEpsToSbm(filePOList));

		responseDTO.setSbmr(fileAR);
		responseDTO.setSbmSummaryAndFileInfo(summaryDTO);
		responseDTO.setXprErrorsExist(sbmTransMsgValidationDao.verifyXprErrorsExist(sbmFileProcSumId));
		responseDTO.setXprWarningsExist(sbmTransMsgValidationDao.verifyXprWarningsExist(sbmFileProcSumId));
		responseDTO.setTotalRecordsProcessed(new BigDecimal(epsSummaryPO.getTotalRecordProcessedCnt()));
		responseDTO.setTotalRecordsRejected(new BigDecimal(epsSummaryPO.getTotalRecordRejectedCnt()));

		sbmFileProcSumDao.updateSbmFileProcessingSummary(epsSummaryPO);

		return responseDTO;
	}



	/* Retrieve the Summary from EPS, compute Approval totals (if approved) and generate FileAcceptanceRejection.
	 * @see gov.hhs.cms.ff.fm.eps.ep.sbm.services.SbmResponseCompositeDao#generateUpdateStatusSBMR(java.lang.Long)
	 */
	@Override
	public SbmResponseDTO generateUpdateStatusSBMR(Long sbmFileProcSumId) {

		SbmResponseDTO responseDTO = new SbmResponseDTO();
		List<SbmFileSummaryMissingPolicyData> missingPolicyDataList = null;

		if (SBMCache.getJobExecutionId() != null) {
			userVO.setUserId(SBMCache.getJobExecutionId().toString());
		}

		// Retrieve the complete summary for this set of file(s).
		SbmFileProcessingSummaryPO epsSummaryPO = sbmFileProcSumDao.selectSbmFileProcessingSummary(sbmFileProcSumId);

		boolean isAccepted = determineAccepted(epsSummaryPO);
		boolean isBackOut = determineBackOut(epsSummaryPO);

		// Only get totals if the set of file(s) has been accepted.
		if (isAccepted) {

			getApprovalTotals(epsSummaryPO);
		}

		// Now all counts and missing policies are retrieved and set in PO and data list, map them to outbound.
		FileAcceptanceRejection fileAR = sbmFileProcSumMapper.mapEpsToSbmr(epsSummaryPO, isAccepted, missingPolicyDataList);
		fileAR.getMissingPolicy().addAll(sbmFileSumMissingPolicyMapper.mapEpsToSbmr(missingPolicyDataList));

		List<SbmFileInfoPO> filePOList = sbmFileInfoDao.getSbmFileInfoList(sbmFileProcSumId);

		// Map fileInfo to outbound
		for (SbmFileInfoPO filePO : filePOList) {

			FileInformationType fileInfo = sbmFileInfoMapper.mapEpsToSbmr(filePO, epsSummaryPO.getSbmFileStatusTypeCd());

			fileAR.getSBMIFileInfo().add(fileInfo);
		}

		// Also, map response data to DTO for further processing in batch.
		SBMSummaryAndFileInfoDTO summaryDTO = sbmFileProcSumMapper.mapEpsToSbm(epsSummaryPO);
		summaryDTO.getSbmFileInfoList().addAll(sbmFileInfoMapper.mapEpsToSbm(filePOList));

		responseDTO.setSbmr(fileAR);
		responseDTO.setSbmSummaryAndFileInfo(summaryDTO);
		responseDTO.setXprErrorsExist(sbmTransMsgValidationDao.verifyXprErrorsExist(sbmFileProcSumId));
		responseDTO.setXprWarningsExist(sbmTransMsgValidationDao.verifyXprWarningsExist(sbmFileProcSumId));	

		sbmFileProcSumDao.updateSbmFileProcessingSummary(epsSummaryPO);

		// Since nothing written to Staging for BKOs, no need to attempt to delete.
		if (!isBackOut) {
			deleteStagingData(sbmFileProcSumId);
		}

		return responseDTO;
	}


	/**
	 * Delete File, Policy and Member data.
	 * @param sbmFileProcSumId
	 */
	private void deleteStagingData(Long sbmFileProcSumId) {

		int cntDate = dateDao.deleteStaging(sbmFileProcSumId);
		int cntMember = memberDao.deleteStaging(sbmFileProcSumId);
		int cntJoin = joinDao.deleteStaging(sbmFileProcSumId);
		int cntStatus = statusDao.deleteStaging(sbmFileProcSumId);
		int cntPremium = premiumDao.deleteStaging(sbmFileProcSumId);
		int cntPolicy = policyDao.deleteStaging(sbmFileProcSumId);

		int countFile = stagingSbmFileDao.deleteStagingSbmFile(sbmFileProcSumId);

		//TODO Remove or change to DEBUG after testing.
		LOG.info("\n\nTotal approved policy and member records DELETED from Staging.  sbmFileProcSumId: " + sbmFileProcSumId +
				"\n     Policies: "  + cntPolicy + 
				"\n     Premiums: " + cntPremium +
				"\n     Statuses: " + cntStatus +
				"\n     Members : " + cntMember +
				"\n     Dates   : " + cntDate +
				"\n     Joins   : " + cntJoin + "\n");

		LOG.info("\nTotal files DELETED from StagingSbmFile.   sbmFileProcSumId: " + sbmFileProcSumId +
				"\n     Files: " + countFile + "\n");
	}


	private void getSummaryTotals(SbmFileProcessingSummaryPO epsPO) {

		Long sbmFileProcSumId = epsPO.getSbmFileProcSumId();

		epsPO.setTotalRecordProcessedCnt(stagingSbmFileDao.selectPolicyCount(sbmFileProcSumId));
		epsPO.setTotalRecordRejectedCnt(sbmTransMsgDao.selectRejectCount(sbmFileProcSumId));
	
	}

	private void getApprovalTotals(SbmFileProcessingSummaryPO epsPO) {

		Long sbmFileProcSumId = epsPO.getSbmFileProcSumId();
		String stateCd = SbmDataUtil.getStateCd(epsPO.getTenantId());

		int matchingANC = sbmTransMsgDao.selectMatchCount(sbmFileProcSumId, SbmTransMsgStatus.ACCEPTED_NO_CHANGE);
		int matchingACC = sbmTransMsgDao.selectMatchCount(sbmFileProcSumId, SbmTransMsgStatus.ACCEPTED_WITH_CHANGES);
		int matchingCorrected = sbmTransMsgDao.selectMatchCountCorrected(sbmFileProcSumId);

		epsPO.setTotalPolicyApprovedCnt(matchingACC + matchingANC);
		epsPO.setMatchingPlcNoChangeCnt(matchingANC);
		epsPO.setMatchingPlcChgApplCnt(matchingANC - matchingCorrected);
		epsPO.setMatchingPlcCorrectedChgApplCnt(matchingCorrected);

		// New policies, ones that did not "policy match"
		epsPO.setNewPlcCreatedAsSentCnt(sbmTransMsgDao.selectNoMatchCount(sbmFileProcSumId, SbmTransMsgStatus.ACCEPTED_NO_CHANGE));
		epsPO.setNewPlcCreatedCorrectionApplCnt(sbmTransMsgDao.selectNoMatchCount(sbmFileProcSumId, SbmTransMsgStatus.ACCEPTED_WITH_CHANGES));

		BigInteger countEffectuated = policyDao.selectPolicyCountByStatus(sbmFileProcSumId, stateCd, PolicyStatus.EFFECTUATED_2);
		epsPO.setEffectuatedPolicyCount(countEffectuated.intValue());
	}

	private void getMissingPolicyTotals(SbmFileProcessingSummaryPO epsPO, List<SbmFileSummaryMissingPolicyData> missingPolicyDataList) {

		int notSubmittedEffectuatedCnt = 0;
		int notSubmittedCancelledCnt = 0;
		int notSubmittedTerminatedCnt = 0;

		if (missingPolicyDataList != null) {
			for (SbmFileSummaryMissingPolicyData missingPolicyData : missingPolicyDataList) {

				if (PolicyStatus.EFFECTUATED_2.equals(missingPolicyData.getPolicyStatus())) {

					if (determineEffectuatedPolicyTerminated(missingPolicyData.getPolicyEndDate())) {
						notSubmittedTerminatedCnt++;
					} else {
						notSubmittedEffectuatedCnt++;
					}

				} else if (PolicyStatus.CANCELLED_3.equals(missingPolicyData.getPolicyStatus())) {
					notSubmittedCancelledCnt++;
				}
			}
		}
		int totalPreviousPoliciesNotSubmit = notSubmittedEffectuatedCnt + notSubmittedCancelledCnt + notSubmittedTerminatedCnt;
		epsPO.setTotalPreviousPoliciesNotSubmit(totalPreviousPoliciesNotSubmit);
		epsPO.setNotSubmittedEffectuatedCnt(notSubmittedEffectuatedCnt);
		epsPO.setNotSubmittedCancelledCnt(notSubmittedCancelledCnt);
		epsPO.setNotSubmittedTerminatedCnt(notSubmittedTerminatedCnt);
	}


	private boolean determineAccepted(SbmFileProcessingSummaryPO epsPO) {

		boolean isAccepted = false;
		SBMFileStatus status = SBMFileStatus.getEnum(epsPO.getSbmFileStatusTypeCd());

		if (status.equals(SBMFileStatus.ACCEPTED) 
				|| status.equals(SBMFileStatus.ACCEPTED_WITH_ERRORS) 
				||status.equals(SBMFileStatus.ACCEPTED_WITH_WARNINGS)) {
			isAccepted = true;
		}
		return isAccepted;
	}

	private boolean determineBackOut(SbmFileProcessingSummaryPO epsPO) {

		boolean isBackOut = false;
		SBMFileStatus status = SBMFileStatus.getEnum(epsPO.getSbmFileStatusTypeCd());

		if (status.equals(SBMFileStatus.BACKOUT)) {
			isBackOut = true;
		}
		return isBackOut;
	}

	/**
	 *  Policy termination determination	FR-FM-PP-SBMI-541
	 *	System shall identify an effectuated policy as terminated when 
	 *  the end date month of the policy is less than or equal to the 
	 *	processing month of SBMI ingestion job.
	 * @param policyEndDate (of Effectuated Policy)
	 * @return
	 */
	private boolean determineEffectuatedPolicyTerminated(LocalDate policyEndDate) {

		boolean isTerm = false;

		if (policyEndDate != null) {

			if (policyEndDate.getMonthValue() <= LocalDate.now().getMonthValue()) {
				isTerm = true;
			}
		}
		return isTerm;
	}


	@Override
	public void createSBMResponseRecord(Long  sbmFileProcSumId, Long sbmFileInfoId, Long physicalDocumentId, SBMResponsePhaseTypeCode responseCd) {

		SbmResponsePO po = new SbmResponsePO();
		po.setSbmFileProcSumId(sbmFileProcSumId);
		po.setSbmFileInfoId(sbmFileInfoId);
		po.setPhysicalDocumentId(physicalDocumentId);
		if (responseCd != null) {
			po.setResponseCd(responseCd.getValue());
		}

		sbmResponseDao.insertSbmResponse(po);
	}

	@Override
	public boolean isRecExistsInStagingSBMPolicy(Long sbmFileProcSumId) {

		boolean isRecExist = stagingSbmPolicyDao.verifySbmStagingPolicyExist(sbmFileProcSumId);
		return isRecExist;
	}

	@Override
	public boolean lockSummaryIdForSBMR(Long sbmFileProcSumId, Long batchId) {

		return stagingSbmGroupLockDao.updateStagingGroupLock(sbmFileProcSumId, batchId);
	}

	@Override
	public void removeLockOnSummaryIdForSBMR(Long sbmFileProcSumId, Long batchId) {

		stagingSbmGroupLockDao.deleteStagingGroupLock(sbmFileProcSumId, batchId);
	}

	@Override
	public List<Long> retrieveSummaryIdsReadyForSBMR() {

		List<Long> sbmFileProcSumIdList = new ArrayList<Long>();

		List<StagingSbmGroupLockPO> poList = stagingSbmGroupLockDao.selectStagingGroupLockSbmr();

		for (StagingSbmGroupLockPO po : poList) {
			sbmFileProcSumIdList.add(po.getSbmFileProcSumId());
		}

		return sbmFileProcSumIdList ;
	}


	@Override
	public List<Long> getSummaryIdsForSBMRFromStagingSBMGroupLock(Long batchId) {

		List<Long> sbmFileProcSumIdList = new ArrayList<Long>();

		List<StagingSbmGroupLockPO> poList = stagingSbmGroupLockDao.selectStagingGroupLockZero(batchId);

		for (StagingSbmGroupLockPO po : poList) {
			sbmFileProcSumIdList.add(po.getSbmFileProcSumId());
		}

		return sbmFileProcSumIdList ;
	}



	/**
	 * @param sbmFileProcSumDao the sbmFileProcSumDao to set
	 */
	public void setSbmFileProcSumDao(SbmFileProcessingSummaryDao sbmFileProcSumDao) {
		this.sbmFileProcSumDao = sbmFileProcSumDao;
	}



	/**
	 * @param sbmFileInfoDao the sbmFileInfoDao to set
	 */
	public void setSbmFileInfoDao(SbmFileInfoDao sbmFileInfoDao) {
		this.sbmFileInfoDao = sbmFileInfoDao;
	}



	/**
	 * @param sbmTransMsgDao the sbmTransMsgDao to set
	 */
	public void setSbmTransMsgDao(SbmTransMsgDao sbmTransMsgDao) {
		this.sbmTransMsgDao = sbmTransMsgDao;
	}



	/**
	 * @param sbmTransMsgValidationDao the sbmTransMsgValidationDao to set
	 */
	public void setSbmTransMsgValidationDao(SbmTransMsgValidationDao sbmTransMsgValidationDao) {
		this.sbmTransMsgValidationDao = sbmTransMsgValidationDao;
	}



	/**
	 * @param sbmTransMsgAddlErrInfoDao the sbmTransMsgAddlErrInfoDao to set
	 */
	public void setSbmTransMsgAddlErrInfoDao(SbmTransMsgAdditionalErrorInfoDao sbmTransMsgAddlErrInfoDao) {
		this.sbmTransMsgAddlErrInfoDao = sbmTransMsgAddlErrInfoDao;
	}



	/**
	 * @param sbmFileSumMissingPolicyDao the sbmFileSumMissingPolicyDao to set
	 */
	public void setSbmFileSumMissingPolicyDao(SbmFileSummaryMissingPolicyDao sbmFileSumMissingPolicyDao) {
		this.sbmFileSumMissingPolicyDao = sbmFileSumMissingPolicyDao;
	}



	/**
	 * @param stagingSbmFileDao the stagingSbmFileDao to set
	 */
	public void setStagingSbmFileDao(StagingSbmFileDao stagingSbmFileDao) {
		this.stagingSbmFileDao = stagingSbmFileDao;
	}



	/**
	 * @param stagingSbmPolicyDao the stagingSbmPolicyDao to set
	 */
	public void setStagingSbmPolicyDao(StagingSbmPolicyDao stagingSbmPolicyDao) {
		this.stagingSbmPolicyDao = stagingSbmPolicyDao;
	}



	/**
	 * @param stagingSbmGroupLockDao the stagingSbmGroupLockDao to set
	 */
	public void setStagingSbmGroupLockDao(StagingSbmGroupLockDao stagingSbmGroupLockDao) {
		this.stagingSbmGroupLockDao = stagingSbmGroupLockDao;
	}



	/**
	 * @param policyDao the policyDao to set
	 */
	public void setPolicyDao(SbmPolicyVersionDao policyDao) {
		this.policyDao = policyDao;
	}



	/**
	 * @param premiumDao the premiumDao to set
	 */
	public void setPremiumDao(SbmPolicyPremiumDao premiumDao) {
		this.premiumDao = premiumDao;
	}



	/**
	 * @param statusDao the statusDao to set
	 */
	public void setStatusDao(SbmPolicyStatusDao statusDao) {
		this.statusDao = statusDao;
	}



	/**
	 * @param joinDao the joinDao to set
	 */
	public void setJoinDao(SbmPolicyMemberDao joinDao) {
		this.joinDao = joinDao;
	}



	/**
	 * @param memberDao the memberDao to set
	 */
	public void setMemberDao(SbmPolicyMemberVersionDao memberDao) {
		this.memberDao = memberDao;
	}



	/**
	 * @param dateDao the dateDao to set
	 */
	public void setDateDao(SbmPolicyMemberDateDao dateDao) {
		this.dateDao = dateDao;
	}



	/**
	 * @param sbmFileProcSumMapper the sbmFileProcSumMapper to set
	 */
	public void setSbmFileProcSumMapper(SbmFileProcessingSummaryMapper sbmFileProcSumMapper) {
		this.sbmFileProcSumMapper = sbmFileProcSumMapper;
	}



	/**
	 * @param sbmFileInfoMapper the sbmFileInfoMapper to set
	 */
	public void setSbmFileInfoMapper(SbmFileInfoMapper sbmFileInfoMapper) {
		this.sbmFileInfoMapper = sbmFileInfoMapper;
	}



	/**
	 * @param sbmTransMsgMapper the sbmTransMsgMapper to set
	 */
	public void setSbmTransMsgMapper(SbmTransMsgMapper sbmTransMsgMapper) {
		this.sbmTransMsgMapper = sbmTransMsgMapper;
	}



	/**
	 * @param sbmTransMsgValidationMapper the sbmTransMsgValidationMapper to set
	 */
	public void setSbmTransMsgValidationMapper(SbmTransMsgValidationMapper sbmTransMsgValidationMapper) {
		this.sbmTransMsgValidationMapper = sbmTransMsgValidationMapper;
	}



	/**
	 * @param sbmTransMsgAddlErrInfoMapper the sbmTransMsgAddlErrInfoMapper to set
	 */
	public void setSbmTransMsgAddlErrInfoMapper(SbmTransMsgAdditionalErrorInfoMapper sbmTransMsgAddlErrInfoMapper) {
		this.sbmTransMsgAddlErrInfoMapper = sbmTransMsgAddlErrInfoMapper;
	}



	/**
	 * @param sbmFileSumMissingPolicyMapper the sbmFileSumMissingPolicyMapper to set
	 */
	public void setSbmFileSumMissingPolicyMapper(SbmFileSummaryMissingPolicyMapper sbmFileSumMissingPolicyMapper) {
		this.sbmFileSumMissingPolicyMapper = sbmFileSumMissingPolicyMapper;
	}



	/**
	 * @param sbmResponseDao the sbmResponseDao to set
	 */
	public void setSbmResponseDao(SbmResponseDao sbmResponseDao) {
		this.sbmResponseDao = sbmResponseDao;
	}


	/**
	 * @param userVO the userVO to set
	 */
	public void setUserVO(UserVO userVO) {
		this.userVO = userVO;
	}


}
