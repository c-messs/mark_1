package gov.hhs.cms.ff.fm.eps.ep.dao;

import java.math.BigInteger;
import java.util.List;

import gov.hhs.cms.ff.fm.eps.ep.po.SbmPolicyStatusPO;

/**
 * @author j.radziewski
 *
 */
public interface SbmPolicyStatusDao extends PolicyStatusDao {
	
	
	/**
	 * @param statusList
	 */
	public void insertStagingPolicyStatusList(List<SbmPolicyStatusPO> statusList);
	
	
	/**
	 * Select EPS POLICYSTATUS records.
	 * @param policyVersionId
	 * @return
	 */
	public List<SbmPolicyStatusPO> getPolicyStatusListAsSbm(Long policyVersionId);
	
	
	/**
	 * Merge from Staging to EPS.
	 * @param sbmFileProcSumId
	 * @return
	 */
	public BigInteger mergePolicyStatus(final Long sbmFileProcSumId);
	
	
	
	/**
	 * @param sbmFileProcSumId
	 * @return
	 */
	public int deleteStaging(Long sbmFileProcSumId);

}
