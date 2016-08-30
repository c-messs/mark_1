package gov.hhs.cms.ff.fm.eps.ep.services.impl;

import gov.cms.dsh.bem.BenefitEnrollmentMaintenanceType;
import gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentMaintenanceDTO;
import gov.hhs.cms.ff.fm.eps.ep.dao.PolicyPremiumDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.PolicyStatusDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.PolicyVersionDao;
import gov.hhs.cms.ff.fm.eps.ep.enums.ExchangeType;
import gov.hhs.cms.ff.fm.eps.ep.mappers.PolicyPremiumMapper;
import gov.hhs.cms.ff.fm.eps.ep.mappers.PolicyStatusMapper;
import gov.hhs.cms.ff.fm.eps.ep.mappers.PolicyVersionMapper;
import gov.hhs.cms.ff.fm.eps.ep.po.GenericPolicyPO;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyPremiumPO;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyStatusPO;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyVersionPO;
import gov.hhs.cms.ff.fm.eps.ep.services.MemberDataDAO;
import gov.hhs.cms.ff.fm.eps.ep.services.PolicyDataService;
import gov.hhs.cms.ff.fm.eps.ep.services.PolicyMatchService;
import gov.hhs.cms.ff.fm.eps.ep.vo.UserVO;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author eps
 *
 */
public class FFMDataServiceImpl implements PolicyDataService {

	private static Logger log = LoggerFactory.getLogger(FFMDataServiceImpl.class);

	private PolicyVersionMapper policyVersionMapper;
	private PolicyPremiumMapper policyPremiumMapper;
	private PolicyStatusMapper policyStatusMapper;

	private PolicyVersionDao policyVersionDao;
	private PolicyPremiumDao policyPremiumDao;
	private PolicyStatusDao policyStatusDao;

	private MemberDataDAO memberDAO;

	private PolicyMatchService policyMatchService;
	
	private UserVO userVO;


	@Override
	public void saveBEM(BenefitEnrollmentMaintenanceDTO bemDTO) {
		
		if(bemDTO.getBatchId() != null) {
			userVO.setUserId(bemDTO.getBatchId().toString());
		} 

		BenefitEnrollmentMaintenanceType bem = bemDTO.getBem();

		List<GenericPolicyPO<?>> genPOList = new ArrayList<GenericPolicyPO<?>>();

		PolicyVersionPO polVer = null;
		Long policyVersionId = null;

		List<PolicyPremiumPO> inboundPremiumList = null;
		List<PolicyStatusPO> inboundStatusList = null;

		List<PolicyStatusPO> epsStatusList = new ArrayList<PolicyStatusPO>();

		PolicyVersionPO pvEps = null;
		Long pvIdEps = null;

		if (bemDTO.getPolicyVersionId() == null) {

			pvEps = new PolicyVersionPO();
			// Needed for inserting a new policy, as to NOT try to UPDATE
			// the MaintenanceEndDateTime of a "latest" policy that does not exist EPS.
			pvEps.setPreviousPolicyVersionId(null);
		} else {
			// Get latest policy to pass into mapper to merge with inbound VO data.
			pvEps = policyVersionDao.getPolicyVersionById(bemDTO.getPolicyVersionId(), bemDTO.getSubscriberStateCd());

			// Since not initial, will allow UPDATE of the MaintenanceEndDateTime of the "latest" policy already in EPS.
			// The PreviousPolicyVersionId was queried in PolicyMatch.
			pvEps.setPreviousPolicyVersionId(bemDTO.getPolicyVersionId());
		}

		// Get all policy status data of the "latest" EPS policy.
		// If pvId == null, then new policy.
		if (pvEps.getPolicyVersionId() != null) {
			pvIdEps = pvEps.getPolicyVersionId();
			epsStatusList = policyStatusDao.getPolicyStatusList(pvIdEps);
		} 

		polVer = policyVersionMapper.mapFFMToEPS(bem);
		polVer.setPreviousPolicyVersionId(pvIdEps);
		polVer.setTransMsgID(bemDTO.getTransMsgId());

		inboundPremiumList = policyPremiumMapper.mapFFMToEPS(bemDTO);
		inboundStatusList = policyStatusMapper.mapFFMToEPS(bem, epsStatusList);

		genPOList.add(polVer);
		genPOList.addAll(inboundPremiumList);
		genPOList.addAll(inboundStatusList);

		setSystemData(genPOList);

		// Insert new policy version and use returned id for other policy POs.
		policyVersionId = policyVersionDao.insertPolicyVersion(polVer);

		setPolicyVersionIdForAllPOs(genPOList, policyVersionId);

		policyPremiumDao.insertPolicyPremiumList(inboundPremiumList);
		policyStatusDao.insertPolicyStatusList(inboundStatusList);
		
		bemDTO.setSubscriberStateCd(polVer.getSubscriberStateCd());
		memberDAO.processMembers(policyVersionId, polVer.getMaintenanceStartDateTime(), bemDTO);
	}


	/**
	 * Retrieves Existing Policy data by policy id and subscriber state code
	 * @param bemDTO
	 * @return
	 */
	@Override
	public BenefitEnrollmentMaintenanceDTO getLatestBEMByPolicyId(BenefitEnrollmentMaintenanceDTO inboundBemDTO) {

		BenefitEnrollmentMaintenanceDTO epsBemDTO = new BenefitEnrollmentMaintenanceDTO();
		BenefitEnrollmentMaintenanceType epsBem = new BenefitEnrollmentMaintenanceType();

		log.debug("Calling performPolicyMatch for:  " + inboundBemDTO.getBemLogInfo());

		PolicyVersionPO polVerPO = policyMatchService.performPolicyMatchByPolicyId(inboundBemDTO);

		if(polVerPO != null) {

			Long policyVersionId = polVerPO.getPolicyVersionId();

			epsBem = policyVersionMapper.mapPOToVO(epsBem, polVerPO);

			epsBemDTO.setBem(epsBem);
			epsBemDTO.setExchangeTypeCd(ExchangeType.FFM.getValue());
			epsBemDTO.setPolicyVersionId(policyVersionId);
			epsBemDTO.setSubscriberStateCd(polVerPO.getSubscriberStateCd());
			epsBemDTO.setSourceVersionId(polVerPO.getSourceVersionId());
			epsBemDTO.setSourceVersionDateTime(polVerPO.getSourceVersionDateTime());
			epsBemDTO.setMarketplaceGroupPolicyId(polVerPO.getMarketplaceGroupPolicyId());
		}
		return epsBemDTO;
	}



	/**
	 *  Method to set the maintenance columns for the db
	 * @param poList
	 * @param userVO
	 */
	private void setSystemData(List<GenericPolicyPO<?>> poList) {

		for(GenericPolicyPO<?> po : poList) {
			po.setCreateDateTime(LocalDate.now());
			po.setLastModifiedDateTime(LocalDate.now());
		}
	}

	/**
	 * Method to set all policyVersionIds in all member POs.
	 * @param poList
	 * @param policyMemberVersionId
	 */
	private void setPolicyVersionIdForAllPOs(List<GenericPolicyPO<?>> poList, Long policyVersionId) {

		for(GenericPolicyPO<?> po : poList) {
			po.setPolicyVersionId(policyVersionId);
		}
	}

	public void setPolicyVersionMapper(PolicyVersionMapper policyVersionMapper) {
		this.policyVersionMapper = policyVersionMapper;
	}

	public void setPolicyPremiumMapper(PolicyPremiumMapper policyPremiumMapper) {
		this.policyPremiumMapper = policyPremiumMapper;
	}

	public void setPolicyStatusMapper(PolicyStatusMapper policyStatusMapper) {
		this.policyStatusMapper = policyStatusMapper;
	}

	// Repositories 

	public void setPolicyVersionDao(
			PolicyVersionDao policyVersionDao) {
		this.policyVersionDao = policyVersionDao;
	}

	public void setPolicyPremiumDao(
			PolicyPremiumDao policyPremiumDao) {
		this.policyPremiumDao = policyPremiumDao;
	}

	public void setPolicyStatusDao(PolicyStatusDao policyStatusDao) {
		this.policyStatusDao = policyStatusDao;
	}

	public void setPolicyMatchService(PolicyMatchService policyMatchService) {
		this.policyMatchService = policyMatchService;
	}

	public void setMemberDAO(MemberDataDAO memberDataDAO) {
		this.memberDAO = memberDataDAO;
	}
	
	public void setUserVO(UserVO userVO) {
		this.userVO = userVO;
	}

}
