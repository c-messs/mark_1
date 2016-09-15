package gov.hhs.cms.ff.fm.eps.ep.sbm.services;

import gov.hhs.cms.ff.fm.eps.ep.po.PolicyVersionPO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMPolicyDTO;

/**
 * @author j.radziewski
 *
 */
public interface SbmPolicyMatchService {
	
	/**
	 * 
	 * @param inboundDTO
	 * @return PolicyVersionPO
	 */
	public PolicyVersionPO findLatestPolicy(SBMPolicyDTO inboundDTO);

}

