package gov.hhs.cms.ff.fm.eps.ep.services;

import org.joda.time.DateTime;

import gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentMaintenanceDTO;

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
	public void processMembers(Long policyVersionId, DateTime pvMaintenanceStartDateTime, BenefitEnrollmentMaintenanceDTO bemDTO);
	
	

}
