package gov.hhs.cms.ff.fm.eps.ep.dao;

import gov.hhs.cms.ff.fm.eps.ep.vo.UserVO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

/**
 * @author eps
 *
 * @param <T>
 */
abstract public class GenericEpsDao<T> {

	protected SimpleJdbcInsert simpleJdbcInsert;
	protected JdbcTemplate jdbcTemplate;
	protected RowMapper<T> rowMapper;
	protected SequenceHelper sequenceHelper;
	protected UserVO userVO;

	/**
	 * @param jdbcTemplate
	 */
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	/**
	 * @param rowMapper
	 */
	public void setRowMapper(RowMapper<T> rowMapper) {
		this.rowMapper=rowMapper;
	}

	/**
	 * @param sequenceHelper
	 */
	public void setSequenceHelper(SequenceHelper sequenceHelper) {
		this.sequenceHelper = sequenceHelper;
	}
	
	/*
	 * @param pos
	 * @return
	 */
	protected EpsBeanPropertySqlParameterSource[] getEpsBeanPropertySqlParameterSourceArrayFromList(
			List<T> pos) {
		List<EpsBeanPropertySqlParameterSource> paramsList = 
				new ArrayList<EpsBeanPropertySqlParameterSource>();
		
		for (T po : pos) {
			paramsList.add(new EpsBeanPropertySqlParameterSource(po, userVO));
		}
		return paramsList.toArray(new EpsBeanPropertySqlParameterSource[paramsList.size()]);
	}
	
	protected int getIntValue(Integer i) {

		int intValue = 0;
		if (i != null) {
			intValue = i.intValue();
		}
		return intValue;
	}

	protected Long getLongValue(BigDecimal n) {

		Long lngValue = Long.valueOf(0);
		if (n != null) {
			lngValue = n.longValue();
		}
		return lngValue;
	}
	
	/**
	 * @param userVO
	 */
	public void setUserVO(UserVO userVO) {
		this.userVO = userVO;
	}

}
