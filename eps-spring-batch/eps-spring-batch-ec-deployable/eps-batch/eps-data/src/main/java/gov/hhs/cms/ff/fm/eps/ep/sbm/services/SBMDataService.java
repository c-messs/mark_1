package gov.hhs.cms.ff.fm.eps.ep.sbm.services;

import java.math.BigDecimal;

import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMPolicyDTO;

/**
 * @author j.radziewski
 *
 */
public interface SBMDataService {
	
	
	/**
	 * @param inboundPolicyDTO
	 * @return
	 */
	public SBMPolicyDTO performPolicyMatch(SBMPolicyDTO inboundPolicyDTO);
	
	/**
	 * Verify if the specific QHP Id exists for the Plan year
	 * 
	 * @param qhpId
	 * @param planYear
	 * @return
	 */
	public boolean checkQhpIdExistsForPolicyYear(String qhpId, String planYear);

	/**
	 * Retreive Metal level for the QHP Id
	 * 
	 * @param qhpId
	 * @param planYear
	 * @return
	 */
	public String getMetalLevelByQhpid(String qhpId, String planYear);

	/**
	 * Retrieve Csr Multiplier By Variant Id And Metal Level
	 * 
	 * @param variantID
	 * @param metal
	 * @param year
	 * @return
	 */
	BigDecimal getCsrMultiplierByVariantAndMetal(String variantID, String metal, String year);


	/**
	 * Retrieve Csr Multiplier By Variant Id
	 * 
	 * @param variantID
	 * @param year
	 * @return
	 */
	BigDecimal getCsrMultiplierByVariant(String variantID, String year);

}
