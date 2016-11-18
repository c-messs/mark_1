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
	 * Since policies from the inbound file cannot be determined until after approval,
	 * find the policies (by file) by looking for what is not in the inbound file compared
	 * to latest policies from EPS.
	 * @param summaryPO
	 * @return
	 */
	public int findAndInsertMissingPolicy(SbmFileProcessingSummaryPO summaryPO);
	
	
	/** 
	 * Selects data from POLICYVERSION and POLICYSTATUS based on PolicyVersionId from table SBMFILESUMMARYMISSSINGPOLICYDATA.
	 * @param sbmFileProcSumId
	 * @return
	 */
	public List<SbmFileSummaryMissingPolicyData> selectMissingPolicyList(Long sbmFileProcSumId);
	
		
	
}
