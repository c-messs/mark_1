package gov.hhs.cms.ff.fm.eps.ep.dao;

import gov.hhs.cms.ff.fm.eps.ep.vo.UserVO;

public interface StagingSbmPolicyDao {
	
	
	/**
	 * @param groupSize
	 * @param stateCd
	 * @param userVO
	 */
	public int extractXprFromStagingSbmFile(final Integer groupSize, final String stateCd, final UserVO userVO);
		
	
	/**
	 * @param sbmFileProcSumId
	 * @return
	 */
	public boolean verifySbmStagingPolicyExist(final Long sbmFileProcSumId);
	
	

}
