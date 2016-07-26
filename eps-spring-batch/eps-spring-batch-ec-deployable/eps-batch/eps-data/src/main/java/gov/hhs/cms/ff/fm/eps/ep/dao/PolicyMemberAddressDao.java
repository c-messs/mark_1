package gov.hhs.cms.ff.fm.eps.ep.dao;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyMemberAddressPO;

import java.util.List;



/**
 * @author eps
 *
 */
public interface PolicyMemberAddressDao {
	
	/**
	 * SELECT PolicyMemberAddress records for ALL members of a policy.
	 * @param policyVersionId
	 * @return
	 */
	public List<PolicyMemberAddressPO> getPolicyMemberAddress(Long policyVersionId);
	/**
	 * @param pmal
	 */
	public void insertPolicyMemberAddressList(List<PolicyMemberAddressPO> pmal);
	

}
