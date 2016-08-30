package gov.hhs.cms.ff.fm.eps.ep.sbm.services;

import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMPolicyDTO;

public interface SbmXprService {
	
	public void saveXprTransaction(SBMPolicyDTO policyDTO);
	
	public void saveXprSkippedTransaction(SBMPolicyDTO policyDTO);

}
