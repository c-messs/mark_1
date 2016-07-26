package gov.hhs.cms.ff.fm.eps.ep.dao;

import gov.hhs.cms.ff.fm.eps.ep.po.PolicyMemberDatePO;

import java.util.List;

/**
 * @author eps
 *
 */
public interface PolicyMemberDateDao {
	

	/**
	 * Select member dates by policyVersionId for all members of a policy version.
	 * @param policyVersionId
	 * @return
	 */
	public List<PolicyMemberDatePO> getPolicyMemberDates(Long policyVersionId);
	/**
	 * Insert all member dates for all members of a policy version.
	 * @param poList
	 */
	public void insertPolicyMemberDates(List<PolicyMemberDatePO> poList);

}
