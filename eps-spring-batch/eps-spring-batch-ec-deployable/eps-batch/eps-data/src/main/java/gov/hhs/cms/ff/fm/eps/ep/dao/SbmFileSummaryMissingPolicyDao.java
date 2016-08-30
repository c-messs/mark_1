package gov.hhs.cms.ff.fm.eps.ep.dao;

import java.util.List;

import gov.hhs.cms.ff.fm.eps.ep.po.SbmFileProcessingSummaryPO;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmFileSummaryMissingPolicyData;

public interface SbmFileSummaryMissingPolicyDao {
	
	
	/**
	 * @param summaryPO
	 */
	public int findAndInsertMissingPolicy(SbmFileProcessingSummaryPO summaryPO);
	
	
	/**
	 * @param summaryPO
	 * @return
	 */
	public int findAndInsertMissingMember(SbmFileProcessingSummaryPO summaryPO);

	
	/**
	 * @param sbmFileProcSumId
	 * @return
	 */
	public List<SbmFileSummaryMissingPolicyData> selectMissingPolicyList(Long sbmFileProcSumId);
		
	
}
