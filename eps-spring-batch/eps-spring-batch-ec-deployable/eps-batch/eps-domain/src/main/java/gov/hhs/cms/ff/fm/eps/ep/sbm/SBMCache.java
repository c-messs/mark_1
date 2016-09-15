package gov.hhs.cms.ff.fm.eps.ep.sbm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import gov.hhs.cms.ff.fm.eps.ep.StateProrationConfiguration;


/**
 * Cache to hold data for SBM batch.
 * @author girish.padmanabhan
 *
 */
public class SBMCache {
	
	/** The QHP ID - paln year map. */
	private static Map<String, List<String>> qhpIdMap = new HashMap<String, List<String>>();
	
	/** The State - Business Rules map. */
	private static Map<String, List<String>> businessRulesMap = new HashMap<String, List<String>>();
	
	/**
	 * StateProrationConfigurationMap Structure Map<coverageYear, Map<stateCd, StateProrationConfiguration>> 
	 */
	private static Map<Integer, Map<String, StateProrationConfiguration>> stateProrationConfigMap = new HashMap<>();
	
	private static Map<String, String> errorCodeDescriptionMap = new HashMap<>();
	
	private static List<String> raceEthnicityCodes = new ArrayList<String>();
	
	private static List<String> languageCodes = new ArrayList<String>();
	
	private static List<String> policyIds = new ArrayList<String>();
	
	private static Long jobExecutionId;
	
	/**
	 * Instantiates a new SBMCache.
	 */
	private SBMCache() {
		super();
	}
	
	/**
	 * 
	 * @param qhpId
	 * @return
	 */
/*	public static List<String> getPlanYearsFromQhpMap(String qhpId) {
		
		if(qhpIdMap.containsKey(qhpId)) {
			return qhpIdMap.get(qhpId);
		}
		return Collections.emptyList();
	}*/
	
	/**
	 * 
	 * @param qhpId
	 * @param planYear
	 * @return boolean
	 */
	public static boolean doesQhpExistForPlanYear(String qhpId, String planYear) {
		
		if(qhpIdMap.containsKey(qhpId)) {
			List<String> years = qhpIdMap.get(qhpId);
			
			if(CollectionUtils.isNotEmpty(years) && years.contains(planYear)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Adds the to issuer run info map.
	 *
	 * @param qhpId String
	 * @param planYear String
	 */
	public static void addToQhpIdMap(String qhpId, String planYear) {
		
		if(qhpIdMap.containsKey(qhpId)) {
			qhpIdMap.get(qhpId).add(planYear);
		
		} else {
			List <String> years = new ArrayList<String>();
			years.add(planYear);
			qhpIdMap.put(qhpId, years);
		}
	}

	/**
	 * @return the businessRulesMap
	 */
	public static Map<String, List<String>> getBusinessRulesMap() {
		return businessRulesMap;
	}

	/**
	 * 
	 * @param stateCd
	 * @return
	 */
	public static List<String> getBusinessRules(String stateCd) {
		
		if(!businessRulesMap.containsKey(stateCd)) {
			businessRulesMap.put(stateCd, new ArrayList<String>());
		}
		return businessRulesMap.get(stateCd);
	}

	/**
	 * @return the stateProrationConfigMap
	 */
	public static Map<Integer, Map<String, StateProrationConfiguration>> getStateProrationConfigMap() {
		return stateProrationConfigMap;
	}

	/**
	 * Returns StateProrationConfiguration for the given coverageYear and stateCd if exists, otherwise returns null
	 * @param coverageYear
	 * @param stateCd
	 * @return StateProrationConfiguration
	 */
	public static StateProrationConfiguration getStateProrationConfiguration(Integer coverageYear, String stateCd) {
		
		if(stateProrationConfigMap.get(coverageYear) != null) {
			return stateProrationConfigMap.get(coverageYear).get(stateCd);
		}
		
		return null;
	}

	/**
	 * @return the errorCodeDescriptionMap
	 */
	public static Map<String, String> getErrorCodeDescriptionMap() {
		return errorCodeDescriptionMap;
	}
	
	/**
	 * Returns description for the given errorCd
	 * @param errorCd
	 * @return
	 */
	public static String getErrorDescription(String errorCd) {
		return errorCodeDescriptionMap.get(errorCd);
	}

	/**
	 * @return the raceEthnicityCodes
	 */
	public static List<String> getRaceEthnicityCodes() {
		return raceEthnicityCodes;
	}

	/**
	 * @return the languageCodes
	 */
	public static List<String> getLanguageCodes() {
		return languageCodes;
	}

	/**
	 * @return the policyIds
	 */
	public static List<String> getPolicyIds() {
		return policyIds;
	}

	/**
	 * @return the jobExecutionId
	 */
	public static Long getJobExecutionId() {
		return jobExecutionId;
	}

	/**
	 * @param jobExecutionId the jobExecutionId to set
	 */
	public static void setJobExecutionId(Long newJobExecutionId) {
		jobExecutionId = newJobExecutionId;
	}

}
