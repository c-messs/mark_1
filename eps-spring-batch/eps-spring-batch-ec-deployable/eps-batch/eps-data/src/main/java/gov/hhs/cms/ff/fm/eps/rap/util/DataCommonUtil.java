package gov.hhs.cms.ff.fm.eps.rap.util;

import java.util.Date;

import org.joda.time.DateTime;

/**
 * 
 * Common Util class
 *
 */
public class DataCommonUtil {
	
	/**
	 * Converts java.util.Date to org.joda.time.DateTime
	 * @param date
	 * @return DateTime or null(if given date is null)
	 */
	public static DateTime convertToDateTime(Date date) {
		
		if(date != null) {
			return new DateTime(date.getTime());
		}
		
		return null;
	}
	
	/**
	 * 
	 * @param dateTime DateTime
	 * @return Date
	 */
	public static java.sql.Date convertToDate(DateTime dateTime) {
		
		if(dateTime != null) {
			return new java.sql.Date(dateTime.getMillis());
		}
		
		return null;
	}
	
}
