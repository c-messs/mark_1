package gov.hhs.cms.ff.fm.eps.ep.dao;

import java.math.BigInteger;

import gov.hhs.cms.ff.fm.eps.ep.enums.PolicyStatus;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmPolicyVersionPO;

/**
 * @author j.radziewski
 *
 */
public interface SbmPolicyVersionDao extends PolicyVersionDao {

	
	/**
	 * @param po
	 * @return
	 */
	public Long insertStagingPolicyVersion(final SbmPolicyVersionPO po);
	
	
	/**
	 * @param sbmFileProcSumId
	 * @return
	 */
	public BigInteger mergePolicyVersion(final Long sbmFileProcSumId);
	
	
	/**
	 * @param sbmFileProcSumId
	 * @param stateCd
	 * @param policyStatus
	 * @return
	 */
	public BigInteger selectPolicyCountByStatus(final Long sbmFileProcSumId, final String stateCd, PolicyStatus policyStatus);
	
	
	/**
	 * @param sbmFileProcSumId
	 * @return
	 */
	public int deleteStaging(Long sbmFileProcSumId);
	
	
	/**
	 * Select count of inbound SBM policies with CANCEL status that have an EPS status of EFFECTUATED.
	 * @param sbmFileProcSumId
	 * @param stateCd
	 * @return
	 */
	public BigInteger selectCountEffectuatedPoliciesCancelled(final Long sbmFileProcSumId, final String stateCd);
}
