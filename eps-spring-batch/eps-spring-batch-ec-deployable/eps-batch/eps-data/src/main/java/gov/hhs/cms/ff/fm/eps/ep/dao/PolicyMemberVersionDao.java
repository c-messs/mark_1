package gov.hhs.cms.ff.fm.eps.ep.dao;

import java.util.List;

import gov.hhs.cms.ff.fm.eps.ep.po.PolicyMemberVersionPO;


/**
 * @author eps
 *
 */
public interface PolicyMemberVersionDao {

	/**
	 * Updates the previous PolicyMemberVersion MAINTENANCEENDDATETIME from HIGHDATE to 1 millisecond less
	 * than the PolicyMemberVersion's to be inserted MAINTENANCESTARTDATETIME using the PolicyMemberVersionId
	 * set in the PolicyVersionPO, which is from the previous version.  The new PolicyMemberVersion is then inserted
	 * and the new policyMemberVersionId is returned.
	 * @param pmv
	 * @return
	 */
	public Long insertPolicyMemberVersion(PolicyMemberVersionPO pmv);

	
	/**
	 * Get ALL PolicyMemberVersions for a policy by PolicyVersionId (PV JOIN PM JOIN PMV).
	 * @param policyVersionId
	 * @param subscriberStateCd
	 * @return
	 */
	public List<PolicyMemberVersionPO> getPolicyMemberVersions(Long policyVersionId, String subscriberStateCd);
	
}
