/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.rap.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.accenture.foundation.common.codetable.CodeRecord;
import com.accenture.foundation.common.codetable.CodeRecordCriteria;
import com.accenture.foundation.common.codetable.CodeService;
import com.accenture.foundation.common.codetable.CodeServiceFactory;
import com.accenture.foundation.common.codetable.CodeType;
import com.accenture.foundation.common.codetable.Criteria;

/**
 * Helper class to get cached data loaded by Code Decode Service
 * 
 * @author girish.padmanabhan
 */
public class CodeDecodesHelper {
	private static final String STATEPRORATIONCONFIGURATION = "STATEPRORATIONCONFIGURATION"; 
	private static final String PRORATINGSTATES = "PRORATINGSTATES";
	
	private CodeService codeService;
	
	/*
	 * Get codeService
	 * @return codeService
	 */
	private CodeService getCodeService() {
		
		if(codeService == null) {
			codeService = CodeServiceFactory.getInstance().getCodeService();
		}
		return codeService;
	}
	
	/**
	 * Method to get DeCode by code type name and category
	 * 
	 * @param codeTypeName
	 * @param category
	 * @return codeRecord
	 */
	public CodeRecord getDecode(String codeTypeName, String category) {

		codeService = getCodeService();
		
		if (StringUtils.isNotEmpty(codeTypeName) && StringUtils.isNotEmpty(category)) {
			CodeType codeType = codeService.findCodeType(codeTypeName);
			if(codeType != null) {
				Criteria criteria = new CodeRecordCriteria(codeType.getName(), category);
				return codeType.findCodeRecord(criteria);
			}
		}
		return null;
	}
	
	/**
	 * Method to get list of decodes for code type and category
	 * 
	 * @param codeTypeName
	 * @param category
	 * @param code
	 * @return decodes
	 */
	public List<String> getDecodesList(String codeTypeName, String category, String code) {
		
		codeService = getCodeService();
		
		if (StringUtils.isNotEmpty(codeTypeName) && StringUtils.isNotEmpty(category)) {
			CodeType codeType = codeService.findCodeType(codeTypeName);
			
			if(codeType != null) {
				Criteria criteria = new CodeRecordCriteria(codeType.getName(), category, code);
				List<CodeRecord> records =  codeType.findCodeRecords(criteria);
				
				if (records != null) {
					List<String> decodes = new ArrayList<String>();
				
					for (CodeRecord record : records) {
						decodes.add(record.getDecode());
					}
					return decodes;
				}
			}
		}
		return Collections.emptyList();
	}

	/**
	 * Determine whether the state is prorating for the Payment Transaction year
	 * @param stateCode
	 * @param paymentTransYear
	 * @return
	 */
	public boolean isStateProrating(String stateCode, int paymentTransYear) {
		
		List<String> marketYears = getDecodesList(STATEPRORATIONCONFIGURATION, PRORATINGSTATES, stateCode);
		
		String paymentTransYearStr = String.valueOf(paymentTransYear);
		
		if(marketYears.contains(paymentTransYearStr)) {
			return true;
		}
		return false;
	}

	/**
	 * Set codeService
	 * @param codeService
	 */
	public void setCodeService(CodeService codeService) {
		this.codeService = codeService;
	}
	
}
