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
	 * @param stateCd
	 * @param issuerId
	 * @return
	 */
	public BigInteger selectPolicyCount(String stateCd, String issuerId);
	
	
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
}
