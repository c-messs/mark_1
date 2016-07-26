package gov.hhs.cms.ff.fm.eps.ep.dao;

import gov.hhs.cms.ff.fm.eps.ep.vo.UserVO;

import org.joda.time.DateTime;
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

		if (result != null && result instanceof DateTime) {
			return ((DateTime)result).toDate();
		} else if (paramName.equalsIgnoreCase("CREATEBY")) {
			return userVO.getUserId();
		} else if (paramName.equalsIgnoreCase("LASTMODIFIEDBY")) {
			return userVO.getUserId();
		}  else {
			return result;
		}
	}

}
