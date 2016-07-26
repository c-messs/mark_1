package gov.hhs.cms.ff.fm.eps.ep.validation;

import gov.cms.dsh.bem.AdditionalInfoType;
import gov.cms.dsh.bem.MemberType;

import java.util.Map;

import org.joda.time.DateTime;

/**
 * @author j.radziewski
 * 
 * Validation interface for inbound transactions determined to be MAINTENANCE and of FINANCIAL_CHANGE.
 *
 */
public interface FinancialValidator {
	

	/**
	 * Processes inbound premiums for transactions where no current policy (and premiums) exist.
	 * @param inboundSubscriber
	 * @return inboundPremiums
	 */
	public Map<DateTime, AdditionalInfoType> processInboundPremiums(MemberType inboundSubscriber);
	
	/**
	 * Determines the "System Selected" EffectiveStartDate (ESD) which is the max ESD 
	 * among the inbound Key Financial elements of AdditionalInfo list
	 *  
	 * @param subscriber
	 * @return sysSelESD
	 */
	public DateTime determineSystemSelectedEffectiveStartDate(MemberType subscriber);

}
