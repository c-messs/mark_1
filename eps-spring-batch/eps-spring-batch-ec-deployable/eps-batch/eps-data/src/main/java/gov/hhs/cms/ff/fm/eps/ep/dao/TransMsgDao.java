package gov.hhs.cms.ff.fm.eps.ep.dao;

import gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentRequestDTO;

/**
 * @author eps
 *
 */
public interface TransMsgDao {
	
	
	/**
	 * @param berDTO
	 * @return
	 */
	public void extractTransMsg(BenefitEnrollmentRequestDTO berDTO);

}
