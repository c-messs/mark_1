package gov.hhs.cms.ff.fm.eps.ep.dao;

import java.math.BigInteger;
import java.util.List;

import gov.hhs.cms.ff.fm.eps.ep.po.SbmPolicyMemberDatePO;

public interface SbmPolicyMemberDateDao extends PolicyMemberDateDao {
	
	
	/**
	 * @param policyVersionId
	 * @return
	 */
	public List<SbmPolicyMemberDatePO> getPolicyMemberDate(Long policyVersionId);
	
	/**
	 * @param po
	 * @return
	 */
	public void insertStagingPolicyMemberDate(final List<SbmPolicyMemberDatePO> poList);
	
	
	/**
	 * @param sbmFileProcSumId
	 * @return
	 */
	public BigInteger mergePolicyMemberDate(final Long sbmFileProcSumId);
	
	
	/**
	 * @param sbmFileProcSumId
	 * @return
	 */
	public int deleteStaging(Long sbmFileProcSumId);
	

}
