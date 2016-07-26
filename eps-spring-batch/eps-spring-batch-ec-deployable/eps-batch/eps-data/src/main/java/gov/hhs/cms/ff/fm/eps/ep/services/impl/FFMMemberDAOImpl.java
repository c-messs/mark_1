package gov.hhs.cms.ff.fm.eps.ep.services.impl;

import gov.cms.dsh.bem.MemberType;
import gov.hhs.cms.ff.fm.eps.ep.BEMDataUtil;
import gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentMaintenanceDTO;
import gov.hhs.cms.ff.fm.eps.ep.dao.PolicyMemberAddressDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.PolicyMemberDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.PolicyMemberDateDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.PolicyMemberVersionDao;
import gov.hhs.cms.ff.fm.eps.ep.mappers.PolicyMemberAddressMapper;
import gov.hhs.cms.ff.fm.eps.ep.mappers.PolicyMemberDateMapper;
import gov.hhs.cms.ff.fm.eps.ep.mappers.PolicyMemberVersionMapper;
import gov.hhs.cms.ff.fm.eps.ep.po.GenericPolicyMemberPO;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyMemberAddressPO;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyMemberDatePO;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyMemberPO;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyMemberVersionPO;
import gov.hhs.cms.ff.fm.eps.ep.services.MemberDataDAO;
import gov.hhs.cms.ff.fm.eps.ep.util.EpsDateUtils;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author eps
 *
 */
public class FFMMemberDAOImpl implements MemberDataDAO {

	private final static Logger LOG = LoggerFactory.getLogger(FFMMemberDAOImpl.class);

	private PolicyMemberVersionMapper policyMemberVersionMapper;
	private PolicyMemberAddressMapper policyMemberAddressMapper;
	private PolicyMemberDateMapper policyMemberDateMapper;

	private PolicyMemberDao policyMemberDao;

	private PolicyMemberVersionDao policyMemberVersionDao;
	private PolicyMemberAddressDao policyMemberAddressDao;
	private PolicyMemberDateDao policyMemberDateDao;

	@Override
	public void processMembers(Long pvId, DateTime pvMaintenanceStartDateTime, BenefitEnrollmentMaintenanceDTO bemDTO) {

		List<GenericPolicyMemberPO<?>> genPOList = new ArrayList<GenericPolicyMemberPO<?>>();

		List<PolicyMemberVersionPO> epsPMVList =  new ArrayList<PolicyMemberVersionPO>();
		List<PolicyMemberAddressPO> epsAddrList = new ArrayList<PolicyMemberAddressPO>();
		List<PolicyMemberDatePO> epsDateList = new ArrayList<PolicyMemberDatePO>();

		List<PolicyMemberPO> policyMemberList = new ArrayList<PolicyMemberPO>();
		List<PolicyMemberAddressPO> policyAddrList = new ArrayList<PolicyMemberAddressPO>();
		List<PolicyMemberDatePO> policyDateList = new ArrayList<PolicyMemberDatePO>();


		// Inbound BemDTO will have the "latest" EPS PolicyVersionId from PolicyMatch.  If null, then no policy in EPS
		Long epsPvId = bemDTO.getPolicyVersionId();
		String exchangePolicyId = BEMDataUtil.getExchangePolicyID(bemDTO.getBem());

		if (epsPvId != null) {
			// Retrieve ALL member data for the "latest" EPS Policy
			epsPMVList = policyMemberVersionDao.getPolicyMemberVersions(epsPvId, bemDTO.getSubscriberStateCd());
			epsAddrList = policyMemberAddressDao.getPolicyMemberAddress(epsPvId);
			epsDateList = policyMemberDateDao.getPolicyMemberDates(epsPvId);
		}

		for (MemberType inboundMember : bemDTO.getBem().getMember()) {

			// find inbound member in list of EPS policyMemberVersions from the "latest" policy.
			PolicyMemberVersionPO epsMember = findEpsMember(inboundMember, epsPMVList);
			PolicyMemberVersionPO inboundVerPO = policyMemberVersionMapper.mapFFMToEPS(inboundMember, epsMember);

			PolicyMemberAddressPO epsMemberAddr = null;
			PolicyMemberDatePO epsMemberDate = null;

			epsMemberAddr = findEpsMemberAddr(epsMember, epsAddrList);
			epsMemberDate = findEpsMemberDate(epsMember, epsDateList);

			PolicyMemberAddressPO inboundAddr = policyMemberAddressMapper.mapFFMToEPS(inboundMember, epsMemberAddr);
			PolicyMemberDatePO inboundDate = policyMemberDateMapper.mapFFMToEPS(inboundMember, epsMemberDate);

			genPOList.add(inboundVerPO);
			
			if (inboundAddr != null) {
				genPOList.add(inboundAddr);
			}
			if (inboundDate != null) {
				genPOList.add(inboundDate);
			}

			boolean isMemberChanged = determineMemberChanged(genPOList);

			if (isMemberChanged) {

				setSystemData(genPOList);
				inboundVerPO.setTransMsgID(bemDTO.getTransMsgId());
				// Set member MaintenanceStartDateTime same as policy.
				inboundVerPO.setMaintenanceStartDateTime(pvMaintenanceStartDateTime);
				inboundVerPO.setExchangePolicyId(exchangePolicyId);
				inboundVerPO.setSubscriberStateCd(bemDTO.getSubscriberStateCd());

				Long pmvId = policyMemberVersionDao.insertPolicyMemberVersion(inboundVerPO);

				LOG.debug("Member changed, using NEW policyMemberVersionId: " + pmvId);

				// Only add members from inbound. If an EPS member is not matched up with an inbound member,
				// that member version will NOT carry over to new policy.
				policyMemberList.add(createPolicyMemberPO(pvId, pmvId, bemDTO.getSubscriberStateCd()));

				setPolicyMemberVersionIdForAllMemberPOs(genPOList, pmvId);
				
				if (inboundAddr != null) {
					policyAddrList.add(inboundAddr);
				}
				if (inboundDate != null) {
					policyDateList.add(inboundDate);
				}

				genPOList.clear();

			} else {

				LOG.debug("Member did NOT change, using EPS policyMemberVersionId: " + epsMember.getPolicyMemberVersionId());
				// If inbound member no different than EPS member, join the new policy Version to the existing "latest" EPS Member.
				policyMemberList.add(createPolicyMemberPO(pvId, epsMember.getPolicyMemberVersionId(), bemDTO.getSubscriberStateCd()));
			}
		} // END for member


		if (!policyAddrList.isEmpty()) {
			policyMemberAddressDao.insertPolicyMemberAddressList(policyAddrList);
		}
		if (!policyDateList.isEmpty()) {
			policyMemberDateDao.insertPolicyMemberDates(policyDateList);
		}

		policyMemberDao.insertPolicyMembers(policyMemberList);
	}


	/**
	 * From the inbound member, find the single PolicyMemberVersionPO 
	 * from the list of "latest" EPS policy members.
	 * @param inboundMember
	 * @param epsList
	 * @return
	 */
	private  PolicyMemberVersionPO findEpsMember(MemberType inboundMember, List<PolicyMemberVersionPO> epsList) {

		PolicyMemberVersionPO po = null;

		String exAssMemId = BEMDataUtil.getExchangeAssignedMemberId(inboundMember);

		for(PolicyMemberVersionPO pmvEPS: epsList) {
			if(pmvEPS.getExchangeMemberID().equals(exAssMemId)) {
				po = pmvEPS;
				break;
			}
		}
		return po;
	}


	/**
	 * Find the current EPS member's address PO for this member from the list of member addresses from the 
	 * latest EPS policy.
	 * @param epsMember
	 * @param epsAddrList
	 * @return
	 */
	private PolicyMemberAddressPO findEpsMemberAddr(PolicyMemberVersionPO epsMember, List<PolicyMemberAddressPO> epsAddrList) {

		PolicyMemberAddressPO epsAddrPO = null;

		if (epsMember != null) {
			for (PolicyMemberAddressPO epsPO : epsAddrList) {
				if (epsPO.getPolicyMemberVersionId().equals(epsMember.getPolicyMemberVersionId())) {
					epsAddrPO = epsPO;
					break;
				}
			}
		}
		return epsAddrPO;
	}
	
	/**
	 * Find the current EPS member's date PO for this member from the list of member dates from the 
	 * latest EPS policy.
	 * @param epsMember
	 * @param epsDateList
	 * @return
	 */
	private PolicyMemberDatePO findEpsMemberDate(PolicyMemberVersionPO epsMember, List<PolicyMemberDatePO> epsDateList) {

		PolicyMemberDatePO epsDatePO = null;

		if (epsMember != null) {
			for (PolicyMemberDatePO epsPO : epsDateList) {
				if (epsPO.getPolicyMemberVersionId().equals(epsMember.getPolicyMemberVersionId())) {
					epsDatePO = epsPO;
					break;
				}
			}
		}
		return epsDatePO;
	}


	/**
	 * @param policyVersionId
	 * @param policyMemberVersionId
	 * @param subscriberStateCd
	 * @return
	 */
	private PolicyMemberPO createPolicyMemberPO(Long policyVersionId, Long policyMemberVersionId, String subscriberStateCd) {

		PolicyMemberPO po = new PolicyMemberPO();

		po.setPolicyVersionId(policyVersionId);
		po.setPolicyMemberVersionId(policyMemberVersionId);
		po.setSubscriberStateCd(subscriberStateCd);
		po.setCreateDateTime(EpsDateUtils.getCurrentDateTime());
		po.setLastModifiedDateTime(EpsDateUtils.getCurrentDateTime());

		return po;
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
	 *  Method to set the maintenance columns for the db
	 * @param poList
	 */
	private void setSystemData(List<GenericPolicyMemberPO<?>> poList) {

		for(GenericPolicyMemberPO<?> po : poList) {
			po.setCreateDateTime(EpsDateUtils.getCurrentDateTime());
			po.setLastModifiedDateTime(EpsDateUtils.getCurrentDateTime());
		}
	}

	/**
	 * Method to set all policyMemberVersionIds in all member POs.
	 * @param poList
	 * @param policyMemberVersionId
	 */
	private void setPolicyMemberVersionIdForAllMemberPOs(List<GenericPolicyMemberPO<?>> poList, Long policyMemberVersionId) {

		for(GenericPolicyMemberPO<?> po : poList) {
			po.setPolicyMemberVersionId(policyMemberVersionId);
		}
	}
	
	/**
	 * @param policyMemberVersionMapper the policyMemberVersionMapper to set
	 */
	public void setPolicyMemberVersionMapper(PolicyMemberVersionMapper policyMemberVersionMapper) {
		
		this.policyMemberVersionMapper = policyMemberVersionMapper;
	}
	/**
	 * @param policyMemberAddressMapper the policyMemberAddressMapper to set
	 */
	public void setPolicyMemberAddressMapper(PolicyMemberAddressMapper policyMemberAddressMapper) {
		
		this.policyMemberAddressMapper = policyMemberAddressMapper;
	}
	/**
	 * @param policyMemberDateMapper the policyMemberDateMapper to set
	 */
	public void setPolicyMemberDateMapper(PolicyMemberDateMapper policyMemberDateMapper) {
		
		this.policyMemberDateMapper = policyMemberDateMapper;
	}
	/**
	 * @param policyMemberVersionDao the policyMemberVersionDao to set
	 */
	public void setPolicyMemberVersionDao(PolicyMemberVersionDao policyMemberVersionDao) {
		
		this.policyMemberVersionDao = policyMemberVersionDao;
	}
	/**
	 * @param policyMemberDao
	 */
	public void setPolicyMemberDao(PolicyMemberDao policyMemberDao) {
		
		this.policyMemberDao = policyMemberDao;
	}
	/**
	 * @param policyMemberAddressDao the policyMemberAddressDao to set
	 */
	public void setPolicyMemberAddressDao(PolicyMemberAddressDao policyMemberAddressDao) {
		
		this.policyMemberAddressDao = policyMemberAddressDao;
	}
	/**
	 * @param policyMemberDateDao the policyMemberDateDao to set
	 */
	public void setPolicyMemberDateDao(PolicyMemberDateDao policyMemberDateDao) {
		
		this.policyMemberDateDao = policyMemberDateDao;
	}

}
