package gov.hhs.cms.ff.fm.eps.ep.dao;

import java.math.BigInteger;
import java.util.List;

import gov.hhs.cms.ff.fm.eps.ep.po.SbmPolicyMemberVersionPO;

public interface SbmPolicyMemberVersionDao extends PolicyMemberVersionDao {
	

	/**
	 * @param subscriberStateCd
	 * @param policyVersionId
	 * @return
	 */
	public List<SbmPolicyMemberVersionPO> getPolicyMemberVersionsForPolicyMatch(String subscriberStateCd, Long policyVersionId);
	
	/**
	 * @param subscriberStateCd
	 * @param policyVersionId
	 * @return
	 */
	public List<SbmPolicyMemberVersionPO> getPolicyMemberVersions(String subscriberStateCd, Long policyVersionId);
	
	/**
	 * @param po
	 * @return
	 */
	public Long insertStagingPolicyMemberVersion(final SbmPolicyMemberVersionPO po);
	
	/**
	 * @param sbmFileProcSumId
	 */
	public BigInteger mergePolicyMemberVersion(final Long sbmFileProcSumId);
	
	/**
	 * @param sbmFileProcSumId
	 * @return
	 */
	public BigInteger mergeLang(final Long sbmFileProcSumId);
	
	/**
	 * @param sbmFileProcSumId
	 * @return
	 */
	public BigInteger mergeRace(final Long sbmFileProcSumId);
	
	/**
	 * @param sbmFileProcSumId
	 * @return
	 */
	public BigInteger mergeAddr(final Long sbmFileProcSumId);
	
	
	/**
	 * @param sbmFileProcSumId
	 * @return
	 */
	public int deleteStaging(Long sbmFileProcSumId);
	
}
