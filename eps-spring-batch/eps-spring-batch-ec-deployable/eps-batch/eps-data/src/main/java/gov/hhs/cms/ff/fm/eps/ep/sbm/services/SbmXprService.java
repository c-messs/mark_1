package gov.hhs.cms.ff.fm.eps.ep.sbm.services;

import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMPolicyDTO;


/**
 * @author j.radziewski
 *
 */
public interface SbmXprService {
	
	/**
	 * 
	 * @param policyDTO
	 */
	public void saveXprTransaction(SBMPolicyDTO policyDTO);
	
	/**
	 * 
	 * @param policyDTO
	 */
	public void saveXprSkippedTransaction(SBMPolicyDTO policyDTO);

}
