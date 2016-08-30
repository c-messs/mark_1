package gov.hhs.cms.ff.fm.eps.ep.dao;

import java.math.BigInteger;
import java.util.List;

import gov.hhs.cms.ff.fm.eps.ep.po.SbmPolicyMemberPO;

/**
 * @author j.radziewski
 *
 */
public interface SbmPolicyMemberDao extends PolicyMemberDao {
	
	/**
	 * @param po
	 * @return
	 */
	public void insertStagingPolicyMember(final List<SbmPolicyMemberPO> poList);
	
	
	/**
	 * @param sbmFileProcSumId
	 */
	public BigInteger mergePolicyMember(final Long sbmFileProcSumId);
	
	
	/**
	 * @param sbmFileProcSumId
	 * @return
	 */
	public List<SbmPolicyMemberPO> findMissingPolicyAndMember(final Long sbmFileProcSumId);
	
	
	/**
	 * @param sbmFileProcSumId
	 * @return
	 */
	public int deleteStaging(Long sbmFileProcSumId);

}
