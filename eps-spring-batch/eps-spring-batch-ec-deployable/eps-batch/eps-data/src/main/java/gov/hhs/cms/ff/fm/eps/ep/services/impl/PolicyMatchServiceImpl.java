package gov.hhs.cms.ff.fm.eps.ep.services.impl;

import gov.cms.dsh.bem.MemberType;
import gov.hhs.cms.ff.fm.eps.ep.BEMDataUtil;
import gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentMaintenanceDTO;
import gov.hhs.cms.ff.fm.eps.ep.dao.PolicyVersionDao;
import gov.hhs.cms.ff.fm.eps.ep.enums.EProdEnum;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyVersionPO;
import gov.hhs.cms.ff.fm.eps.ep.services.PolicyMatchService;
import gov.hhs.cms.ff.fm.eps.ep.vo.PolicyVersionSearchCriteriaVO;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.accenture.foundation.common.exception.ApplicationException;

/**
 * Performs policy matching for inbound BEMs using extracted criteria from the that BEM.
 * A successful "Policy Match" will return one and only one BEM.
 *
 */
public class PolicyMatchServiceImpl implements PolicyMatchService {

	private static Logger log = LoggerFactory.getLogger(PolicyMatchServiceImpl.class);

	private PolicyVersionDao policyVersionDao;

	/*
	 * Performs policy matching based on the Exchange Assigned Policy id and Subscriber state code.
	 */
	@Override
	public PolicyVersionPO performPolicyMatchByPolicyId(BenefitEnrollmentMaintenanceDTO bemDTO) {

		PolicyVersionPO po = null;

		PolicyVersionSearchCriteriaVO criteria = getSearchCriteriaPolicyId(bemDTO);

		String logMsg = "groupPolicyNumber(exchangePolicyId)=" + criteria.getExchangePolicyId();
		logMsg += ", subscriberStateCd=" + criteria.getSubscriberStateCd(); 
		logMsg += ", " + bemDTO.getBemLogInfo();

		log.debug("Performing Policy Match for: " + logMsg);

		List<PolicyVersionPO> poList = policyVersionDao.findLatestPolicyVersion(criteria);

		// can never return null.
		if (poList.size() == 1) {
			po = poList.get(0);

		} else if (poList.size() > 1) {
			log.error(EProdEnum.EPROD_13.getLogMsg());
			throw new ApplicationException(EProdEnum.EPROD_13.getCode());
		}

		if (po != null) {

			logMsg = "Policy Match FOUND";

		} else {

			logMsg = "NO Policy Match";
		}
		log.debug(logMsg);

		return po;
	}



	/**
	 * Extracts ExchangePolicyID and StateCd search criteria from the inbound Bem in the BemDTO.
	 * @param bemDTO
	 * @return
	 */
	public PolicyVersionSearchCriteriaVO getSearchCriteriaPolicyId(BenefitEnrollmentMaintenanceDTO bemDTO) {

		PolicyVersionSearchCriteriaVO criteria = new PolicyVersionSearchCriteriaVO();
		criteria.setExchangePolicyId(BEMDataUtil.getExchangePolicyID(bemDTO.getBem()));
		MemberType subscriber = BEMDataUtil.getSubscriberMember(bemDTO.getBem());
		criteria.setSubscriberStateCd(BEMDataUtil.getSubscriberStateCode(subscriber));
		return criteria;
	}


	/**
	 * @param policyVersionDao the policyVersionDao to set
	 */
	public void setPolicyVersionDao(PolicyVersionDao policyVersionDao) {
		this.policyVersionDao = policyVersionDao;
	}

}

