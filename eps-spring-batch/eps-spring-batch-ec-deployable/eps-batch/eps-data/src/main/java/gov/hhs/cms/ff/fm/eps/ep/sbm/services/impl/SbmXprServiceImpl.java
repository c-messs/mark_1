package gov.hhs.cms.ff.fm.eps.ep.sbm.services.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.cms.dsh.sbmi.PolicyMemberType;
import gov.hhs.cms.ff.fm.eps.ep.dao.SbmPolicyMemberDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.SbmPolicyMemberDateDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.SbmPolicyMemberVersionDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.SbmPolicyPremiumDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.SbmPolicyStatusDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.SbmPolicyVersionDao;
import gov.hhs.cms.ff.fm.eps.ep.enums.SbmTransMsgStatus;
import gov.hhs.cms.ff.fm.eps.ep.po.GenericPolicyMemberPO;
import gov.hhs.cms.ff.fm.eps.ep.po.GenericPolicyPO;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmPolicyMemberDatePO;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmPolicyMemberPO;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmPolicyMemberVersionPO;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmPolicyPremiumPO;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmPolicyStatusPO;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmPolicyVersionPO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMPolicyDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SbmDataUtil;
import gov.hhs.cms.ff.fm.eps.ep.sbm.mappers.SbmPolicyMemberDateMapper;
import gov.hhs.cms.ff.fm.eps.ep.sbm.mappers.SbmPolicyMemberVersionMapper;
import gov.hhs.cms.ff.fm.eps.ep.sbm.mappers.SbmPolicyPremiumMapper;
import gov.hhs.cms.ff.fm.eps.ep.sbm.mappers.SbmPolicyStatusMapper;
import gov.hhs.cms.ff.fm.eps.ep.sbm.mappers.SbmPolicyVersionMapper;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SbmTransMsgCompositeDao;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SbmXprService;
import gov.hhs.cms.ff.fm.eps.ep.vo.UserVO;

public class SbmXprServiceImpl implements SbmXprService {

	private final static Logger LOG = LoggerFactory.getLogger(SbmXprServiceImpl.class);

	private SbmTransMsgCompositeDao sbmTransMsgCompositeDao;

	private SbmPolicyVersionMapper policyVersionMapper;
	private SbmPolicyPremiumMapper premiumMapper;
	private SbmPolicyStatusMapper statusMapper;
	private SbmPolicyMemberVersionMapper pmvMapper;
	private SbmPolicyMemberDateMapper dateMapper;

	private SbmPolicyVersionDao sbmPolicyVersionDao;
	private SbmPolicyPremiumDao premiumDao;
	private SbmPolicyStatusDao statusDao;

	private SbmPolicyMemberDao joinDao;

	private SbmPolicyMemberVersionDao pmvDao;
	private SbmPolicyMemberDateDao dateDao;

	private UserVO userVO;


	@Override
	public void saveXprTransaction(SBMPolicyDTO policyDTO) {

		if (policyDTO.getBatchId() != null) {

			userVO.setUserId(policyDTO.getBatchId().toString());
		} 

		if (!policyDTO.isErrorFlag() && policyDTO.getSchemaErrorList().isEmpty()) {

			boolean isPolicyChanged = false;
			boolean isMemberChanged = false;

			String stateCd = SbmDataUtil.getStateCd(policyDTO.getFileInfo());

			List<GenericPolicyPO<?>> genPOList = new ArrayList<GenericPolicyPO<?>>();
			List<GenericPolicyMemberPO<?>> genMemPOList = new ArrayList<GenericPolicyMemberPO<?>>();

			SbmPolicyVersionPO inboundPvPO = null;
			List<SbmPolicyPremiumPO> inboundPremiumList = null;
			List<SbmPolicyStatusPO> inboundStatusList = null;
			SbmPolicyMemberVersionPO inboundPmv =  null;
			List<SbmPolicyMemberDatePO> inboundDateList = null;

			List<SbmPolicyMemberPO> inboundPMList = null;

			SbmPolicyVersionPO epsPvPO = null;
			List<SbmPolicyPremiumPO> epsPremiumList = null;
			List<SbmPolicyStatusPO> epsStatusList = null;
			List<SbmPolicyMemberVersionPO> epsPMVList =  null;
			List<SbmPolicyMemberDatePO> epsDateList = null;


			// PVID, if exists, from policyMatch.
			Long epsPvId = policyDTO.getPolicyVersionId();

			// Retrieve "latest" SBM policy and members.
			if (epsPvId != null) {

				epsPvPO = (SbmPolicyVersionPO) sbmPolicyVersionDao.getPolicyVersionById(epsPvId, stateCd);
				epsPremiumList = premiumDao.selectPolicyPremiumList(epsPvId);
				epsStatusList =  statusDao.getPolicyStatusListAsSbm(epsPvId);
				epsPMVList = pmvDao.getPolicyMemberVersions(stateCd, epsPvId);
				epsDateList = dateDao.getPolicyMemberDate(epsPvId);			
			}

			inboundPvPO = policyVersionMapper.mapSbmToStaging(policyDTO, epsPvPO);
			inboundPremiumList = premiumMapper.mapSbmToStaging(policyDTO.getSbmPremiums(), epsPremiumList);
			inboundStatusList = statusMapper.mapSbmToEps(policyDTO, epsStatusList);

			genPOList.add(inboundPvPO);
			genPOList.addAll(inboundPremiumList);
			genPOList.addAll(inboundStatusList);

			isPolicyChanged = determinePolicyChanged(genPOList);

			if (!isPolicyChanged) {
				// Only check if the policy has not changed, because a member might have.
				isMemberChanged = determineMemberChanged(policyDTO, epsPMVList, epsDateList);
			}


			if (isPolicyChanged || isMemberChanged) {

				Long sbmTransMsgId = sbmTransMsgCompositeDao.saveSbmTransMsg(policyDTO);
				inboundPvPO.setSbmTransMsgId(sbmTransMsgId);

				Long pvId = sbmPolicyVersionDao.insertStagingPolicyVersion(inboundPvPO);

				setPolicyVersionIdForAllPOs(genPOList, pvId);

				LOG.info("Saving Policy. " + policyDTO.getLogMsg());

				premiumDao.insertStagingPolicyPremiumList(inboundPremiumList);
				statusDao.insertStagingPolicyStatusList(inboundStatusList);

				inboundDateList = new ArrayList<SbmPolicyMemberDatePO>();
				inboundPMList = new ArrayList<SbmPolicyMemberPO>();

				for (PolicyMemberType inboundMember : policyDTO.getPolicy().getMemberInformation()) {

					// find inbound member in list of EPS policyMemberVersions from the "latest" EPS policy.
					SbmPolicyMemberVersionPO epsMember = findEpsMember(inboundMember, epsPMVList);
					inboundPmv = pmvMapper.mapSbmToStaging(inboundMember, epsMember);
					inboundPmv.setSbmTransMsgID(sbmTransMsgId);
					genMemPOList.add(inboundPmv);

					// find only the dates for this member.
					List<SbmPolicyMemberDatePO> epsMemberDates = findEpsMemberDates(epsMember, epsDateList);
					List<SbmPolicyMemberDatePO> memberDateList = dateMapper.mapSbmToStaging(inboundMember, epsMemberDates);
					genMemPOList.addAll(memberDateList);

					// Need to create a new PolicyMemberVersion is changed, otherwise use existing EPS PolicyMemberVersion.
					if (determineMemberChanged(genMemPOList)) {

						inboundPmv.setExchangePolicyId(inboundPvPO.getExchangePolicyId());
						inboundPmv.setSubscriberStateCd(stateCd);

						Long pmvId = pmvDao.insertStagingPolicyMemberVersion(inboundPmv);

						LOG.info("Member changed.  Inserting into staging using NEW policyMemberVersionId: " + pmvId);

						// Only add members from inbound. If an EPS member is not matched up with an inbound member,
						// that member version will NOT carry over to new policy.
						inboundPMList.add(createPolicyMemberPO(pvId, pmvId, stateCd));

						setPolicyMemberVersionIdForAllMemberPOs(genMemPOList, pmvId);

						if (memberDateList != null) {
							inboundDateList.addAll(memberDateList);
						}

					} else {

						LOG.info("Member did NOT change!  NOT inserting this member into staging.  Joining using EPS policyMemberVersionId: " + epsMember.getPolicyMemberVersionId());
						// If inbound member no different than EPS member, join the new policy Version to the existing "latest" EPS Member.
						inboundPMList.add(createPolicyMemberPO(pvId, epsMember.getPolicyMemberVersionId(), stateCd));
					}					

					genMemPOList.clear();
				}

				if (!inboundDateList.isEmpty()) {
					dateDao.insertStagingPolicyMemberDate(inboundDateList);
				}

				joinDao.insertStagingPolicyMember(inboundPMList);

			} else {

				LOG.info("Inbound Policy SAME as EPS policy. NO SbmTransMsg or Policy saved.");

				//TODO: determine if we need to update a status somewhere, or count?
			}
		} else {
			// Save SbmTransMsg (and errors) since there were errors.
			sbmTransMsgCompositeDao.saveSbmTransMsg(policyDTO);
		}
	}

	@Override
	public void saveXprSkippedTransaction(SBMPolicyDTO policyDTO) {

		sbmTransMsgCompositeDao.saveSbmTransMsg(policyDTO, SbmTransMsgStatus.SKIP);

	}

	private boolean determineMemberChanged(SBMPolicyDTO policyDTO, List<SbmPolicyMemberVersionPO> epsPMVList, List<SbmPolicyMemberDatePO> epsDateList) {

		boolean isAnyMemberChanged = false;

		List<GenericPolicyMemberPO<?>> genMemPOList = new ArrayList<GenericPolicyMemberPO<?>>();

		// Translate inbound policy members to member POs and determine if any member data has changed. 
		for (PolicyMemberType inboundMember : policyDTO.getPolicy().getMemberInformation()) {

			// find inbound member in list of EPS policyMemberVersions from the "latest" policy.
			SbmPolicyMemberVersionPO epsMember = findEpsMember(inboundMember, epsPMVList);
			genMemPOList.add(pmvMapper.mapSbmToStaging(inboundMember, epsMember));

			List<SbmPolicyMemberDatePO> epsMemDateList = findEpsMemberDates(epsMember, epsDateList);
			genMemPOList.addAll(dateMapper.mapSbmToStaging(inboundMember, epsMemDateList));

			if (determineMemberChanged(genMemPOList)) {
				isAnyMemberChanged = true;
				break;
			}
			genMemPOList.clear();
		}


		return isAnyMemberChanged;
	}


	/**
	 * Method to set all pvIds in all policy POs.
	 * @param poList
	 * @param pvId
	 */
	private void setPolicyVersionIdForAllPOs(List<GenericPolicyPO<?>> poList, Long pvId) {

		for(GenericPolicyPO<?> po : poList) {
			po.setPolicyVersionId(pvId);
		}
	}

	/**
	 * Determines if any POs of a policy have changed.  Meaning inbound data that is mapped to a PO
	 * is different than a PO with data from EPS after mapping.
	 *  - Inbound policy is NOT same as EPS policy value by value when isPolicyChanged == true.  
	 * @param poList
	 * @return
	 */
	private boolean determinePolicyChanged(List<GenericPolicyPO<?>> poList) {

		boolean isPolicyChanged = false;

		for(GenericPolicyPO<?> po : poList) {
			// If any value on policy changed, then it is not a duplicated policy.
			if(po.isPolicyChanged()) {
				isPolicyChanged = true;
				break;
			}
		}
		return isPolicyChanged;
	}

	/**
	 * Determines if any member POs of a member have changed.  Meaning inbound data that is mapped to a PO
	 * is different than a PO with data from EPS after mapping.  
	 * @param poList
	 * @return
	 */
	private boolean determineMemberChanged(List<GenericPolicyMemberPO<?>> poList) {

		boolean isPolicyMemberChanged = false;

		for(GenericPolicyMemberPO<?> po : poList) {
			if(po.isPolicyMemberChanged()) {
				isPolicyMemberChanged = true;
				break;
			}
		}
		return isPolicyMemberChanged;
	}

	/**
	 * From the inbound member, find the single SbmPolicyMemberVersionPO 
	 * from the list of "latest" EPS policy members.
	 * @param inboundMember
	 * @param epsList
	 * @return
	 */
	private  SbmPolicyMemberVersionPO findEpsMember(PolicyMemberType inboundMember, List<SbmPolicyMemberVersionPO> epsList) {

		SbmPolicyMemberVersionPO po = null;

		String exAssMemId = inboundMember.getExchangeAssignedMemberId();

		if (epsList != null) {
			for(SbmPolicyMemberVersionPO pmvEPS: epsList) {
				if(pmvEPS.getExchangeMemberID().equals(exAssMemId)) {
					po = pmvEPS;
					break;
				}
			}
		}
		return po;
	}


	/**
	 * Find the current EPS member's list of date POs for this member from the list of member dates from the 
	 * latest EPS policy. A member can have more that one memberDate.
	 * @param epsMember
	 * @param epsDateList
	 * @return
	 */
	private List<SbmPolicyMemberDatePO> findEpsMemberDates(SbmPolicyMemberVersionPO epsMember, List<SbmPolicyMemberDatePO> epsDateList) {

		List<SbmPolicyMemberDatePO> epsDatePOList = new ArrayList<SbmPolicyMemberDatePO>();

		if (epsDateList != null) {
			if (epsMember != null) {
				for (SbmPolicyMemberDatePO epsPO : epsDateList) {
					if (epsPO.getPolicyMemberVersionId().equals(epsMember.getPolicyMemberVersionId())) {
						epsDatePOList.add(epsPO);
					}
				}
			}
		}
		return epsDatePOList;
	}

	/**
	 * @param pvId
	 * @param policyMemberVersionId
	 * @param subscriberStateCd
	 * @return
	 */
	private SbmPolicyMemberPO createPolicyMemberPO(Long pvId, Long policyMemberVersionId, String subscriberStateCd) {

		SbmPolicyMemberPO po = new SbmPolicyMemberPO();

		po.setPolicyVersionId(pvId);
		po.setPolicyMemberVersionId(policyMemberVersionId);
		po.setSubscriberStateCd(subscriberStateCd);

		return po;
	}

	/**
	 * Method to set all policyMemberVersionIds in all POs of a member.
	 * @param poList
	 * @param pmvId
	 */
	private void setPolicyMemberVersionIdForAllMemberPOs(List<GenericPolicyMemberPO<?>> poList, Long pmvId) {

		for(GenericPolicyMemberPO<?> po : poList) {
			po.setPolicyMemberVersionId(pmvId);
		}
	}

	/**
	 * @param sbmTransMsgCompositeDao the sbmTransMsgCompositeDao to set
	 */
	public void setSbmTransMsgCompositeDao(SbmTransMsgCompositeDao sbmTransMsgCompositeDao) {
		this.sbmTransMsgCompositeDao = sbmTransMsgCompositeDao;
	}

	/**
	 * @param policyVersionMapper the policyVersionMapper to set
	 */
	public void setPolicyVersionMapper(SbmPolicyVersionMapper policyVersionMapper) {
		this.policyVersionMapper = policyVersionMapper;
	}

	/**
	 * @param premiumMapper the premiumMapper to set
	 */
	public void setPremiumMapper(SbmPolicyPremiumMapper premiumMapper) {
		this.premiumMapper = premiumMapper;
	}

	/**
	 * @param statusMapper the statusMapper to set
	 */
	public void setStatusMapper(SbmPolicyStatusMapper statusMapper) {
		this.statusMapper = statusMapper;
	}

	/**
	 * @param pmvMapper the pmvMapper to set
	 */
	public void setPmvMapper(SbmPolicyMemberVersionMapper pmvMapper) {
		this.pmvMapper = pmvMapper;
	}

	/**
	 * @param dateMapper the dateMapper to set
	 */
	public void setDateMapper(SbmPolicyMemberDateMapper dateMapper) {
		this.dateMapper = dateMapper;
	}

	/**
	 * @param sbmPolicyVersionDao the sbmPolicyVersionDao to set
	 */
	public void setSbmPolicyVersionDao(SbmPolicyVersionDao sbmPolicyVersionDao) {
		this.sbmPolicyVersionDao = sbmPolicyVersionDao;
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
	 * @param pmvDao the pmvDao to set
	 */
	public void setPmvDao(SbmPolicyMemberVersionDao pmvDao) {
		this.pmvDao = pmvDao;
	}

	/**
	 * @param dateDao the dateDao to set
	 */
	public void setDateDao(SbmPolicyMemberDateDao dateDao) {
		this.dateDao = dateDao;
	}

	/**
	 * @param userVO the userVO to set
	 */
	public void setUserVO(UserVO userVO) {
		this.userVO = userVO;
	}



}
