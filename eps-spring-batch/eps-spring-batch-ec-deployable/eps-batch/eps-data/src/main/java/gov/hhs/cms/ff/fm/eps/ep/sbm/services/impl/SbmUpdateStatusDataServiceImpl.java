package gov.hhs.cms.ff.fm.eps.ep.sbm.services.impl;

import java.math.BigInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.hhs.cms.ff.fm.eps.ep.dao.SbmFileProcessingSummaryDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.SbmPolicyMemberDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.SbmPolicyMemberDateDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.SbmPolicyMemberVersionDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.SbmPolicyPremiumDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.SbmPolicyStatusDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.SbmPolicyVersionDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.StagingSbmFileDao;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SbmFileReversalDao;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SbmUpdateStatusDataService;
import gov.hhs.cms.ff.fm.eps.ep.vo.UserVO;

public class SbmUpdateStatusDataServiceImpl implements SbmUpdateStatusDataService  {

	private final static Logger LOG = LoggerFactory.getLogger(SbmUpdateStatusDataServiceImpl.class);

	private SbmPolicyVersionDao policyDao;
	private SbmPolicyPremiumDao premiumDao;
	private SbmPolicyStatusDao statusDao;

	private SbmPolicyMemberDao joinDao;

	private SbmPolicyMemberVersionDao memberDao;
	private SbmPolicyMemberDateDao dateDao;

	private StagingSbmFileDao stagingSbmFileDao;
	private SbmFileReversalDao sbmFileReversalDao;
	
	private SbmFileProcessingSummaryDao sbmFileProcSumDao;

	private UserVO userVO;

	@Override
	public void executeApproval(Long batchId, Long sbmFileProcSumId) {

		if (batchId != null) {

			userVO.setUserId(batchId.toString());
		} 

		if (sbmFileProcSumId != null) {

			BigInteger cntPolicy =  policyDao.mergePolicyVersion(sbmFileProcSumId);
			BigInteger cntPremium = premiumDao.mergePolicyPremium(sbmFileProcSumId);
			BigInteger cntStatus = statusDao.mergePolicyStatus(sbmFileProcSumId);

			BigInteger cntMember = memberDao.mergePolicyMemberVersion(sbmFileProcSumId);
			BigInteger cntLang = memberDao.mergeLang(sbmFileProcSumId);
			BigInteger cntRace = memberDao.mergeRace(sbmFileProcSumId);
			BigInteger cntAddr = memberDao.mergeAddr(sbmFileProcSumId);
			BigInteger cntDate = dateDao.mergePolicyMemberDate(sbmFileProcSumId);

			BigInteger cntJoin = joinDao.mergePolicyMember(sbmFileProcSumId);

			//TODO Remove or change to DEBUG after testing.
			LOG.info("\n\nTotal approved policy and member records MERGED from Staging to EPS: batchId: " + batchId + ", sbmFileProcSumId: " +sbmFileProcSumId +
					"\n     Policies: "  + cntPolicy + 
					"\n     Premiums: " + cntPremium +
					"\n     Statuses: " + cntStatus +
					"\n     Members : " + cntMember +
					"\n     Langs   : " + cntLang +
					"\n     Races   : " + cntRace +
					"\n     Addrs   : " + cntAddr +
					"\n     Dates   : " + cntDate +
					"\n     Joins   : " + cntJoin + "\n");
		}
		
		boolean isCmsAppovalReq = sbmFileProcSumDao.verifyCmsApprovalRequired(sbmFileProcSumId);
		
		// If CMS Approval is required, staging data will be deleted in SbmResponseCompositeDao.
		if (!isCmsAppovalReq) {
			deleteStagingData(sbmFileProcSumId);
		}		
	}
	
	
	@Override
	public void executeDisapproval(Long batchId, Long sbmFileProcSumId) {

		deleteStagingData(sbmFileProcSumId);
	}


	@Override
	public void executeFileReversal(Long batchId, Long sbmFileProcSumId) {

		LOG.info("executeFileReversal(" + batchId + ", " + sbmFileProcSumId + ")");
		
		if (batchId != null) {

			userVO.setUserId(batchId.toString());
		} 
		
		if (sbmFileProcSumId != null) {
			
			sbmFileReversalDao.backOutFile(sbmFileProcSumId);
		}

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
	 * @param stagingSbmFileDao the stagingSbmFileDao to set
	 */
	public void setStagingSbmFileDao(StagingSbmFileDao stagingSbmFileDao) {
		this.stagingSbmFileDao = stagingSbmFileDao;
	}

	/**
	 * @param sbmFileReversalDao the sbmFileReversalDao to set
	 */
	public void setSbmFileReversalDao(SbmFileReversalDao sbmFileReversalDao) {
		this.sbmFileReversalDao = sbmFileReversalDao;
	}
	
	/**
	 * @param sbmFileProcSumDao the sbmFileProcSumDao to set
	 */
	public void setSbmFileProcSumDao(SbmFileProcessingSummaryDao sbmFileProcSumDao) {
		this.sbmFileProcSumDao = sbmFileProcSumDao;
	}

	/**
	 * @param userVO the userVO to set
	 */
	public void setUserVO(UserVO userVO) {
		this.userVO = userVO;
	}


}
