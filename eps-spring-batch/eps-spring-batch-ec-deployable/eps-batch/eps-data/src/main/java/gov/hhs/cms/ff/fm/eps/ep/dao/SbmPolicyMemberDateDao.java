package gov.hhs.cms.ff.fm.eps.ep.dao;

import java.math.BigInteger;
import java.util.List;

import gov.hhs.cms.ff.fm.eps.ep.po.SbmPolicyMemberDatePO;

/**
 * @author j.radziewski
 *
 */
public interface SbmPolicyMemberDateDao extends PolicyMemberDateDao {
	
	
	/**
	 * @param policyVersionId
	 * @return
	 */
	public List<SbmPolicyMemberDatePO> getPolicyMemberDate(Long policyVersionId);
	
	/**
	 * Inserts a list of dates per member.
	 * @param poList
	 */
	public void insertStagingPolicyMemberDate(final List<SbmPolicyMemberDatePO> poList);
	
	
	/**
	 * Merge from Staging to EPS.
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
