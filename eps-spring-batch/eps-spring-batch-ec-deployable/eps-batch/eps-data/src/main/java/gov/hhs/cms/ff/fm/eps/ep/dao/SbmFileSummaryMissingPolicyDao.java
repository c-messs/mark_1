package gov.hhs.cms.ff.fm.eps.ep.dao;

import java.util.List;

import gov.hhs.cms.ff.fm.eps.ep.po.SbmFileProcessingSummaryPO;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmFileSummaryMissingPolicyData;

/**
 * @author j.radziewski
 *
 */
public interface SbmFileSummaryMissingPolicyDao {
	
	
	/**
	 * @param summaryPO
	 * @return
	 */
	public int findAndInsertMissingPolicy(SbmFileProcessingSummaryPO summaryPO);
	
	
	/**
	 * Finds the latest EPS members that are not present in STAGINGSBMFILE based on ExchangeAssignedMemberId.
	 * @param summaryPO
	 * @return
	 */
	public int findAndInsertMissingMember(SbmFileProcessingSummaryPO summaryPO);

	
	/** 
	 * Selects data from POLICYVERSION and POLICYSTATUS based on PolicyVersionId from table SBMFILESUMMARYMISSSINGPOLICYDATA.
	 * @param sbmFileProcSumId
	 * @return
	 */
	public List<SbmFileSummaryMissingPolicyData> selectMissingPolicyList(Long sbmFileProcSumId);
		
	
}
