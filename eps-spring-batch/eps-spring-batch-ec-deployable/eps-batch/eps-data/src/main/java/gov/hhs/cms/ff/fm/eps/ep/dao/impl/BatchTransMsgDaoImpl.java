package gov.hhs.cms.ff.fm.eps.ep.dao.impl;

import gov.hhs.cms.ff.fm.eps.ep.dao.BatchTransMsgDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.EpsBeanPropertySqlParameterSource;
import gov.hhs.cms.ff.fm.eps.ep.dao.GenericEpsDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.mappers.BatchTransMsgRowMapper;
import gov.hhs.cms.ff.fm.eps.ep.enums.EpsEntityNames;
import gov.hhs.cms.ff.fm.eps.ep.po.BatchTransMsgPO;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

/**
 * @author eps
 *
 */
public class BatchTransMsgDaoImpl extends GenericEpsDao<BatchTransMsgPO> implements
		BatchTransMsgDao {

	private String selectBatchTransMsgByPk;
	private String updateProcessToDbStatus;
	private String selectSkippedVersion;
	private String updateSkippedVersion;
	private String getSkippedVersionCount;
	private String updateLaterVersions;

	/**
	 * @param simpleJdbcInsert
	 */
	public void setSimpleJdbcInsert(SimpleJdbcInsert simpleJdbcInsert) {
		this.simpleJdbcInsert = simpleJdbcInsert;
		simpleJdbcInsert
				.withTableName(EpsEntityNames.BATCH_TRANS_MSG_ENTITY_NAME
						.getValue());
	}

	/**
	 * Constructor
	 */
	public BatchTransMsgDaoImpl() {
		
		setRowMapper(new BatchTransMsgRowMapper());
	}

	public void setSelectBatchTransMsgByPk(String sql) {
		this.selectBatchTransMsgByPk = sql;
	}

	public void setUpdateProcessToDbStatus(String sql) {
		this.updateProcessToDbStatus = sql;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gov.hhs.cms.ff.fm.eps.ep.dao.BatchTransMsgDao#insertBatchTransMsg(gov
	 * .hhs.cms.ff.fm.eps.ep.po.BatchTransMsgPO)
	 */
	@Override
	public void insertBatchTransMsg(BatchTransMsgPO batchTransMsgPO) {

		simpleJdbcInsert.execute(new EpsBeanPropertySqlParameterSource(batchTransMsgPO, super.userVO));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gov.hhs.cms.ff.fm.eps.ep.dao.BatchTransMsgDao#getBatchTransMsgByPk(java
	 * .lang.Long, java.lang.Long)
	 */
	@Override
	public BatchTransMsgPO getBatchTransMsgByPk(Long batchId, Long transMsgId) {

		return (BatchTransMsgPO) jdbcTemplate.queryForObject(
				selectBatchTransMsgByPk, rowMapper, batchId, transMsgId);
	}

	
	/* (non-Javadoc)
	 * @see gov.hhs.cms.ff.fm.eps.ep.dao.BatchTransMsgDao#updateBatchTransMsg(gov.hhs.cms.ff.fm.eps.ep.po.BatchTransMsgPO)
	 */
	@Override
	public Boolean updateBatchTransMsg(final BatchTransMsgPO po) {
		
		return jdbcTemplate.execute(updateProcessToDbStatus, new PreparedStatementCallback<Boolean>() {

			@Override
			public Boolean doInPreparedStatement(PreparedStatement ps)
					throws SQLException, DataAccessException {
				if (po.getProcessedToDbStatusTypeCd() != null) {
					ps.setString(1, po.getProcessedToDbStatusTypeCd());
				} else {
					ps.setString(1, null);
				}
				ps.setString(2, po.getTransMsgSkipReasonTypeCd());
				ps.setString(3, po.getTransMsgSkipReasonDesc());
				// LASTMODIFIEDBY, by this batch
				ps.setString(4, po.getBatchId().toString());
				ps.setLong(5, po.getTransMsgId());
				ps.setLong(6, po.getBatchId());
				int rowsEffected = ps.executeUpdate();

				return (rowsEffected == 1);
			}
		});
	}
	
	
	@Override
	public Integer getSkipCount(final BatchTransMsgPO po) {
		
		return jdbcTemplate.queryForObject(selectSkippedVersion,
				new Object[] {po.getSubscriberStateCd(), po.getExchangePolicyId(), po.getIssuerHiosId(), po.getSourceVersionId()},
				Integer.class);
	}

	@Override
	public Integer updateSkippedVersion(final BatchTransMsgPO po) {
		
		final String userId = super.userVO.getUserId();

		return jdbcTemplate.execute(updateSkippedVersion, new PreparedStatementCallback<Integer>() {

			@Override
			public Integer doInPreparedStatement(PreparedStatement ps)
					throws SQLException, DataAccessException {
				if (po.getProcessedToDbStatusTypeCd() != null) {
					ps.setString(1, po.getProcessedToDbStatusTypeCd());
				} else {
					ps.setString(1, null);
				}
				ps.setString(2, userId);
				ps.setString(3, po.getSubscriberStateCd());
				ps.setString(4, po.getExchangePolicyId());
				ps.setString(5, po.getIssuerHiosId());
				ps.setLong(6, po.getSourceVersionId());
				ps.setLong(7, po.getBatchId());
				
				int rowsEffected = ps.executeUpdate();

				return Integer.valueOf(rowsEffected);
			}
		});
	}
	
	@Override
	public Integer updateLaterVersions(final BatchTransMsgPO po) {
		
		final String userId = super.userVO.getUserId();

		return jdbcTemplate.execute(updateLaterVersions, new PreparedStatementCallback<Integer>() {

			@Override
			public Integer doInPreparedStatement(PreparedStatement ps)
					throws SQLException, DataAccessException {
		
				ps.setString(1, po.getProcessedToDbStatusTypeCd());
				ps.setString(2, userId);
				ps.setString(3, po.getSubscriberStateCd());
				ps.setString(4, po.getExchangePolicyId());
				ps.setString(5, po.getIssuerHiosId());
				ps.setLong(6, po.getSourceVersionId());
				
				int rowsEffected = ps.executeUpdate();

				return Integer.valueOf(rowsEffected);
			}
		});
	}
	
	@Override
	public Integer getSkippedVersionCount(final BatchTransMsgPO po) {
		
		return jdbcTemplate.queryForObject(getSkippedVersionCount,
				new Object[] {po.getSubscriberStateCd(), po.getExchangePolicyId(), po.getIssuerHiosId(), po.getSourceVersionId(), po.getBatchId()},
				Integer.class);
	}


	/**
	 * @param selectSkippedVersion the selectSkippedVersion to set
	 */
	public void setSelectSkippedVersion(String selectSkippedVersion) {
		this.selectSkippedVersion = selectSkippedVersion;
	}


	/**
	 * @param updateSkippedVersion the updateSkippedVersion to set
	 */
	public void setUpdateSkippedVersion(String updateSkippedVersion) {
		this.updateSkippedVersion = updateSkippedVersion;
	}

	/**
	 * @param getSkippedVersionCount the getSkippedVersionCount to set
	 */
	public void setGetSkippedVersionCount(String getSkippedVersionCount) {
		this.getSkippedVersionCount = getSkippedVersionCount;
	}

	/**
	 * @param updateLaterVersions the updateLaterVersions to set
	 */
	public void setUpdateLaterVersions(String updateLaterVersions) {
		this.updateLaterVersions = updateLaterVersions;
	}
	
}
