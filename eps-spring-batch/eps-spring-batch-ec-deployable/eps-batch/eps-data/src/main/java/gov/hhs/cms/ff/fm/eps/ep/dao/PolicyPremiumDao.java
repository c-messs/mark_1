package gov.hhs.cms.ff.fm.eps.ep.dao;

import gov.hhs.cms.ff.fm.eps.ep.po.PolicyPremiumPO;

import java.util.List;


/**
 * @author eps
 *
 */
public interface PolicyPremiumDao {
	
	/**
	 * @param premiums
	 */
	public void insertPolicyPremiumList(List<PolicyPremiumPO> premiums);
		
}
