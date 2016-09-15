package gov.hhs.cms.ff.fm.eps.ep.dao.impl;

import gov.hhs.cms.ff.fm.eps.ep.dao.EpsBeanPropertySqlParameterSource;
import gov.hhs.cms.ff.fm.eps.ep.dao.ErrorWarningLogDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.GenericEpsDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.mappers.ErrorWarningLogRowMapper;
import gov.hhs.cms.ff.fm.eps.ep.enums.EpsEntityNames;
import gov.hhs.cms.ff.fm.eps.ep.po.ErrorWarningLogPO;

import java.util.List;

import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

/**
 * @author eps
 *
 */
public class ErrorWarningLogDaoImpl extends GenericEpsDao<ErrorWarningLogPO> implements ErrorWarningLogDao {
	
	private String selectErrorsListByTransMsgId;
	
	/**
	 * Constructor, set rowMapper once.
	 */
	public ErrorWarningLogDaoImpl() {
		
		super.rowMapper = new ErrorWarningLogRowMapper();
	}

	/**
	 * @param sql
	 */
	public void setSelectErrorsListByTransMsgId(String sql) {
		
		this.selectErrorsListByTransMsgId = sql;
	}

	/**
	 * @param simpleJdbcInsert
	 */
	public void setSimpleJdbcInsert(SimpleJdbcInsert simpleJdbcInsert) {
		super.simpleJdbcInsert = simpleJdbcInsert;
		simpleJdbcInsert.withTableName(EpsEntityNames.ERROR_WARNING_LOG
				.getValue());
	}

	
	/* (non-Javadoc)
	 * @see gov.hhs.cms.ff.fm.eps.ep.dao.ErrorWarningLogDao#insertErrorWarningLogs(java.util.List)
	 */
	@Override
	public void insertErrorWarningLogs(List<ErrorWarningLogPO> errorWarningLogList) {
		SqlParameterSource[] array = new SqlParameterSource[errorWarningLogList.size()]; 
		for (int i = 0; i < errorWarningLogList.size(); i++) {
			ErrorWarningLogPO errorWarningLogPO = errorWarningLogList.get(i);
			errorWarningLogPO.setErrorWarningId(sequenceHelper.nextSequenceId("ErrorWarningSeq"));
			array[i] = new EpsBeanPropertySqlParameterSource(errorWarningLogPO, super.userVO);
		}
		simpleJdbcInsert.executeBatch(array);
	}


	/* (non-Javadoc)
	 * @see gov.hhs.cms.ff.fm.eps.ep.dao.ErrorWarningLogDao#getErrorWarningLogListByTransMsgId(java.lang.Long)
	 */
	@Override
	public List<ErrorWarningLogPO> getErrorWarningLogListByTransMsgId(Long transMsgId) {
		
		return (List<ErrorWarningLogPO>) this.jdbcTemplate.query(
				this.selectErrorsListByTransMsgId, rowMapper, transMsgId);
	}

}
