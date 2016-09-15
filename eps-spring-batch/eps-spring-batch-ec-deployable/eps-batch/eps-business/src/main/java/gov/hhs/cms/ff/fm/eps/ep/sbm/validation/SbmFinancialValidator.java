package gov.hhs.cms.ff.fm.eps.ep.sbm.validation;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import gov.cms.dsh.sbmi.PolicyType;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMPremium;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SbmErrWarningLogDTO;

/**
 * @author j.radziewski
 * 
 * Validation interface for inbound transactions determined to be MAINTENANCE and of FINANCIAL_CHANGE.
 *
 */
public interface SbmFinancialValidator {
	
	/**
	 * Validate Policy Financial Information
	 * 
	 * @param policy
	 * @return List<SbmErrWarningLogDTO>
	 */
	public List<SbmErrWarningLogDTO> validateFinancialInfo(PolicyType policy);

	/**
	 * Processes inbound premiums for transactions where no current policy (and premiums) exist.
	 * @param policy
	 * @return inboundPremiums - Map<LocalDate, SBMPremium>
	 */
	public Map<LocalDate, SBMPremium> processInboundPremiums(PolicyType policy);
	

}
