package gov.hhs.cms.ff.fm.eps.ep.services;

import gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentMaintenanceDTO;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyVersionPO;

/**
 * @author eps
 *
 */
public interface PolicyMatchService {
	
	/**
	 * @param bemDTO
	 * @return
	 */
	public PolicyVersionPO performPolicyMatchByPolicyId(BenefitEnrollmentMaintenanceDTO bemDTO);
	
	
}
