package gov.hhs.cms.ff.fm.eps.rap.util;

import org.joda.time.DateTime;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;

/**
 * @author girish.padmanabhan
 *
 */
public class RapBeanPropertySqlParameterSource extends BeanPropertySqlParameterSource {

	/**
	 * Constructor
	 * @param object
	 */
	public RapBeanPropertySqlParameterSource(Object object) {
		super(object);
	}

	@Override
	public Object getValue(String paramName) {
		
		Object result = super.getValue(paramName);

		if (result != null && result instanceof DateTime) {
			return ((DateTime)result).toDate();
		} else {
			return result;
		}
	}

}
