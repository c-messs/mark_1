package gov.hhs.cms.ff.fm.eps.ep.dao;

import java.math.BigInteger;
import java.util.List;

import gov.hhs.cms.ff.fm.eps.ep.po.SbmPolicyPremiumPO;

/**
 * @author j.radziewski
 *
 */
public interface SbmPolicyPremiumDao extends PolicyPremiumDao {
	
	
	/**
	 * Select EPS POLICYPREMIUM records.
	 * @param policyVersionId
	 * @return
	 */
	public List<SbmPolicyPremiumPO> selectPolicyPremiumList(final Long policyVersionId);
	
	/**
	 * @param premiumList
	 */
	public void insertStagingPolicyPremiumList(final List<SbmPolicyPremiumPO> premiumList);
	
	/**
	 *  Merge from Staging to EPS.
	 * @param sbmFileProcSumId
	 * @return count rows affected.
	 */
	public BigInteger mergePolicyPremium(final Long sbmFileProcSumId);
	
	
	/**
	 * @param sbmFileProcSumId
	 * @return count rows affected.
	 */
	public int deleteStaging(Long sbmFileProcSumId);

}
