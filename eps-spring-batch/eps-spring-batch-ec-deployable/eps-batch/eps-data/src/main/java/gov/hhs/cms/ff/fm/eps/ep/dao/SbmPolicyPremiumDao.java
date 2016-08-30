package gov.hhs.cms.ff.fm.eps.ep.dao;

import java.math.BigInteger;
import java.util.List;

import gov.hhs.cms.ff.fm.eps.ep.po.SbmPolicyPremiumPO;

public interface SbmPolicyPremiumDao extends PolicyPremiumDao {
	
	
	/**
	 * @param policyVersionId
	 * @return
	 */
	public List<SbmPolicyPremiumPO> selectPolicyPremiumList(final Long policyVersionId);
	
	/**
	 * @param premiumList
	 */
	public void insertStagingPolicyPremiumList(final List<SbmPolicyPremiumPO> premiumList);
	
	/**
	 * @param sbmFileProcSumId
	 * @return
	 */
	public BigInteger mergePolicyPremium(final Long sbmFileProcSumId);
	
	
	/**
	 * @param sbmFileProcSumId
	 * @return
	 */
	public int deleteStaging(Long sbmFileProcSumId);

}
