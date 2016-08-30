package gov.hhs.cms.ff.fm.eps.rap.util;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
	 * Converts java.util.Date to java.localDate
	 * @param date
	 * @return LocalDate or null(if given date is null)
	 */
	public static LocalDate convertToDate(Date date) {
		
		LocalDate dt = null;
		
		if(date != null) {
			dt = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		}
		
		return dt;
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
	
	/**
	 * Converts java.util.Date to org.joda.time.DateTime
	 * @param date
	 * @return DateTime or null(if given date is null)
	 */
	public static Timestamp convertToSqlTimestamp(LocalDateTime dateTime) {
		
		if(dateTime != null) {
			return Timestamp.valueOf(dateTime);
		}
		
		return null;
	}
	
}
