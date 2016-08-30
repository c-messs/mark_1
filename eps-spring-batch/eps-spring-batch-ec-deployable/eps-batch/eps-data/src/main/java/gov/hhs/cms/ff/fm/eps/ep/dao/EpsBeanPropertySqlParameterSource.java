package gov.hhs.cms.ff.fm.eps.ep.dao;

import gov.hhs.cms.ff.fm.eps.ep.vo.UserVO;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;

/**
 * @author eps
 *
 */
public class EpsBeanPropertySqlParameterSource extends BeanPropertySqlParameterSource {

	private UserVO userVO;
	
	/**
	 * Constructor
	 * @param object
	 */
	public EpsBeanPropertySqlParameterSource(Object object) {
		super(object);
		this.userVO = new UserVO();
	}
	

	/**
	 * Constructor.
	 * @param object
	 * @param userVO
	 */
	public EpsBeanPropertySqlParameterSource(Object object, UserVO userVO) {
		super(object);
		if (userVO != null) {
			this.userVO = userVO;
		} else {
			this.userVO = new UserVO();
		}
	}


	@Override
	public Object getValue(String paramName) {
		
		Object result = super.getValue(paramName);

		if (result != null && result instanceof LocalDate) {
			return Date.valueOf((LocalDate) result);
		} else if (result != null && result instanceof LocalDateTime) {
			return Timestamp.valueOf((LocalDateTime) result);
		} else if (paramName.equalsIgnoreCase("CREATEBY")) {
			return userVO.getUserId();
		} else if (paramName.equalsIgnoreCase("LASTMODIFIEDBY")) {
			return userVO.getUserId();
		} else {
			return result;
		}
	}
}
