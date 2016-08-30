package gov.hhs.cms.ff.fm.eps.ep.dao;

import java.util.List;

import gov.hhs.cms.ff.fm.eps.ep.po.StagingSbmFilePO;

public interface StagingSbmFileDao {
	
	
	/**
	 * @param po
	 * @return
	 */
	public boolean insertFileToStaging(final StagingSbmFilePO po);
	
	
	/**
	 * @param sbmFileProcSumId
	 * @return
	 */
	public int deleteStagingSbmFile(final Long sbmFileProcSumId);
	
	
	/**
	 * @param sbmFileProcSumId
	 * @return
	 */
	public Integer selectPolicyCount(Long sbmFileProcSumId);


	/**
	 * @param sbmFileProcSumId
	 * @return
	 */
	public List<String> getStagingPolicies(Long sbmFileProcSumId);


}
