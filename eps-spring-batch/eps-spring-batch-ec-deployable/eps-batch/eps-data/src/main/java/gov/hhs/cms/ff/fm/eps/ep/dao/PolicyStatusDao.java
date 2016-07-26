package gov.hhs.cms.ff.fm.eps.ep.dao;

import gov.hhs.cms.ff.fm.eps.ep.po.PolicyStatusPO;

import java.util.List;


/**
 * @author eps
 *
 */
public interface PolicyStatusDao {
	
	/**
	 * @param psl
	 * @param policyVersionId
	 */
	public void insertPolicyStatusList(List<PolicyStatusPO> psl);
	
	/**
	 * @param policyVersionId
	 * @return
	 */
	public List <PolicyStatusPO> getPolicyStatusList(Long policyVersionId);

}
