package gov.hhs.cms.ff.fm.eps.ep.services;

import gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentMaintenanceDTO;

/**
 * @author eps
 *
 */
public interface PolicyDataService {
	
	/**
	 * @param bemDTO
	 */
	public void saveBEM(BenefitEnrollmentMaintenanceDTO bemDTO);
	

	/**
	 * @param bemDTO
	 * @return
	 */
	public BenefitEnrollmentMaintenanceDTO getLatestBEMByPolicyId(BenefitEnrollmentMaintenanceDTO bemDTO);

}
