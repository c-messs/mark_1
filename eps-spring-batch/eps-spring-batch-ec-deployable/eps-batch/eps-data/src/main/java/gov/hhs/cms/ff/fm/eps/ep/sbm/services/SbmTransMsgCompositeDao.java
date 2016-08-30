package gov.hhs.cms.ff.fm.eps.ep.sbm.services;

import gov.hhs.cms.ff.fm.eps.ep.enums.SbmTransMsgStatus;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMPolicyDTO;

/**
 * @author j.radziewski
 *
 */
public interface SbmTransMsgCompositeDao {
	
	/**
	 * @param policyDTO
	 * @return
	 */
	public Long saveSbmTransMsg(SBMPolicyDTO policyDTO);
	
	/**
	 * @param policyDTO
	 * @param status
	 * @return
	 */
	public Long saveSbmTransMsg(SBMPolicyDTO policyDTO, SbmTransMsgStatus status);
	

}
