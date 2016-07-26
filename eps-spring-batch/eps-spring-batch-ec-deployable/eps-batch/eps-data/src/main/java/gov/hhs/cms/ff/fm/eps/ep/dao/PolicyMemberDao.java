package gov.hhs.cms.ff.fm.eps.ep.dao;

import gov.hhs.cms.ff.fm.eps.ep.po.PolicyMemberPO;

import java.util.List;

/**
 * @author eps
 *
 */
public interface PolicyMemberDao {
	
	/**
	 * Insert all policyMembers (join table) for a policy version.
	 * @param poList
	 */
	public void insertPolicyMembers(List<PolicyMemberPO> poList);

}
