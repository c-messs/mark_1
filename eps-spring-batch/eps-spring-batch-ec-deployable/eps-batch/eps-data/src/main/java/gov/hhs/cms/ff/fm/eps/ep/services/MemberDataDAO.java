package gov.hhs.cms.ff.fm.eps.ep.services;

import gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentMaintenanceDTO;

import java.time.LocalDateTime;

/**
 * @author eps
 *
 */
public interface MemberDataDAO {
	

	/**
	 * @param policyVersionId
	 * @param pvMaintenanceStartDateTime
	 * @param bemDTO
	 */
	public void processMembers(Long policyVersionId, LocalDateTime pvMaintenanceStartDateTime, BenefitEnrollmentMaintenanceDTO bemDTO);
	
	

}
