package gov.hhs.cms.ff.fm.eps.rap.service.impl;

import static gov.hhs.cms.ff.fm.eps.rap.domain.RapConstants.COVERAGEPERIODPAID;
import static gov.hhs.cms.ff.fm.eps.rap.domain.RapConstants.ERC;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.accenture.foundation.common.codetable.CodeRecord;
import com.accenture.foundation.common.exception.ApplicationException;

import gov.hhs.cms.ff.fm.eps.rap.dao.RapDao;
import gov.hhs.cms.ff.fm.eps.rap.domain.IssuerUserFeeRate;
import gov.hhs.cms.ff.fm.eps.rap.domain.RapConstants;
import gov.hhs.cms.ff.fm.eps.rap.dto.PolicyDataDTO;
import gov.hhs.cms.ff.fm.eps.rap.util.CodeDecodesHelper;
import gov.hhs.cms.ff.fm.eps.rap.util.RapProcessingHelper;


/**
 * This is the handler class for the Retro Active Payment Processing.
 * @author mark.finkelshteyn
 *
 */

public class RapProcessServiceHandler {
	/*
	 * Determine the ERC of the latest Payment Month
	 */
	
	private CodeDecodesHelper codeDecodesHelper;
	private RapDao rapDao;
	private static final Logger LOGGER = LoggerFactory.getLogger(RapProcessServiceHandler.class);
	
	/**
	 * This method retrieves data from COVERAGEPERIODPAID table
	 *@param codeDecodesHelper
	 *@return currentPmtMonth
	 */
	public List<DateTime> getPaymentMonthERC(CodeDecodesHelper codeDecodesHelper) {

		CodeRecord record = codeDecodesHelper.getDecode(COVERAGEPERIODPAID, ERC);
		
		if(record==null){
			String message = RapConstants.ERRORCODE_E9004+": Process Execution Error - COVERAGEPERIODPAID table is empty";
			LOGGER.error(message);
			throw new ApplicationException(message);
		}
		
		List<DateTime> currentPmtMonth = new ArrayList<DateTime>();
		DateTime pmtMonth = null;
		DateTime ercDate = null;
		
		String code = record.getCode();
		if(code != null) {
			LOGGER.info("cpm: "+ code);
		
			pmtMonth = RapProcessingHelper.getDateTimeFromString(code);
			LOGGER.info("paymentMonth: "+ pmtMonth);
			currentPmtMonth.add(pmtMonth);
		}
		
		String decode = record.getDecode();
		if(decode != null) {
			LOGGER.info("erc: "+ decode);
			ercDate = RapProcessingHelper.getDateTimeFromString(decode);
			LOGGER.info("ercDate: "+ ercDate);
			currentPmtMonth.add(ercDate);
		}
		return currentPmtMonth;
	}
	/**
	 * Handler method to find Free Rate
	 *@param coverageDate 
	 *@param policy
	 *@param rapDao
	 *@return boolean
	 */ 
     public boolean isUserFeeRateExists(DateTime coverageDate, PolicyDataDTO policy, RapDao rapDao) {
		
		IssuerUserFeeRate rate = RapProcessingHelper.getUserFeeRateForRetroCoverageDate(
				coverageDate, RapProcessingHelper.getStateCode(policy.getPlanId()), policy.getInsrncAplctnTypeCd(), rapDao);
		if(rate != null) {
			LOGGER.info("UF Rate found for policy:{}", policy.getPolicyVersionId());
			return true;
		}
		LOGGER.info("No UF Rate found for policy:{}", policy.getPolicyVersionId());
		return false;
	 }	
	
}
