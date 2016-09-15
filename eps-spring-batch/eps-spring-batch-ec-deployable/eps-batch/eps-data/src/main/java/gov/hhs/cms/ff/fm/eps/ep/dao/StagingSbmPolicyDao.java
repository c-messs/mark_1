package gov.hhs.cms.ff.fm.eps.ep.dao;

import gov.hhs.cms.ff.fm.eps.ep.vo.UserVO;

/**
 * @author j.radziewski
 *
 */
public interface StagingSbmPolicyDao {
	
	
	
	/**
	 * XML Extract each Policy from STAGINGSBMFILE SBMXML to STAGINGSBMPOLICY.
	 * For each Policy in the file (content as SBMXML) results in one XPR, or STAGINGSBMPOLICY, record.
	 * @param groupSize
	 * @param stateCd
	 * @param userVO
	 * @return
	 */
	public int extractXprFromStagingSbmFile(final Integer groupSize, final String stateCd, final UserVO userVO);
		
	
	/**
	 * @param sbmFileProcSumId
	 * @return
	 */
	public boolean verifySbmStagingPolicyExist(final Long sbmFileProcSumId);
	
	

}
