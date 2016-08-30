package gov.hhs.cms.ff.fm.eps.ep.dao;

import java.math.BigInteger;
import java.util.List;

import gov.hhs.cms.ff.fm.eps.ep.po.SbmPolicyStatusPO;

public interface SbmPolicyStatusDao extends PolicyStatusDao {
	
	
	/**
	 * @param statusList
	 */
	public void insertStagingPolicyStatusList(List<SbmPolicyStatusPO> statusList);
	
	
	/**
	 * @param policyVersionId
	 * @return
	 */
	public List<SbmPolicyStatusPO> getPolicyStatusListAsSbm(Long policyVersionId);
	
	
	/**
	 * @param sbmFileProcSumId
	 * @return
	 */
	public BigInteger mergePolicyStatus(final Long sbmFileProcSumId);
	
	
	/**
	 * @param policyVersionId
	 * @return
	 */
	public SbmPolicyStatusPO getPolicyStatusLatest(Long policyVersionId);
	
	
	/**
	 * @param sbmFileProcSumId
	 * @return
	 */
	public int deleteStaging(Long sbmFileProcSumId);

}
