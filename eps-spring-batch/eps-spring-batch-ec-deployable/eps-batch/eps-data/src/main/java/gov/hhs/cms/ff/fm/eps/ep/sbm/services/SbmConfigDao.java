package gov.hhs.cms.ff.fm.eps.ep.sbm.services;

import java.util.List;
import java.util.Map;

import gov.hhs.cms.ff.fm.eps.ep.StateProrationConfiguration;

public interface SbmConfigDao {

	/**
	 * Retrieve ALL, and only, SBM state configurations data.
	 * @return
	 */
	public List<StateProrationConfiguration> retrieveSbmStates();

	/**
	 * Get X12RaceEthnicityCode List for SBM
	 * @return
	 */
	public List<String> retrieveLanguageCodes();
	
	/**
	 * Get X12RaceEthnicityCode List for SBM
	 * @return
	 */
	public List<String> retrieveRaceEthnicityCodes();

	/**
	 * Get BusinessRules List for SBM
	 * @return
	 */
	public List<String[]> retrieveBusinessRules();

	/**
	 * Get Error Codes And Descriptions for SBM
	 * @return
	 */
	public Map<String, String> retrieveErrorCodesAndDescription();

}
