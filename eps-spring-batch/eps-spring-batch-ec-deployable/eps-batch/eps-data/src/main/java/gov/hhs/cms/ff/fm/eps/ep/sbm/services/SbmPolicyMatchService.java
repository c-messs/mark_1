package gov.hhs.cms.ff.fm.eps.ep.sbm.services;


import gov.hhs.cms.ff.fm.eps.ep.po.PolicyVersionPO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMPolicyDTO;

public interface SbmPolicyMatchService {
	
	
	public PolicyVersionPO findLatestPolicy(SBMPolicyDTO inboundDTO);
	


}

