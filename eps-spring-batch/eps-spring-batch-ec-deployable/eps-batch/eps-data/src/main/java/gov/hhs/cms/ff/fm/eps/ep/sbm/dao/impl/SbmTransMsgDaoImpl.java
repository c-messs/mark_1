package gov.hhs.cms.ff.fm.eps.ep.sbm.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import com.accenture.foundation.common.exception.ApplicationException;

import gov.hhs.cms.ff.fm.eps.ep.dao.GenericEpsDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.SbmTransMsgDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.mappers.SbmTransMsgRowMapper;
import gov.hhs.cms.ff.fm.eps.ep.enums.EProdEnum;
import gov.hhs.cms.ff.fm.eps.ep.enums.SbmTransMsgStatus;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmTransMsgCountData;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmTransMsgPO;

/**
 * 
 * SbmTransMsgDao implementation
 *
 */
public class SbmTransMsgDaoImpl extends GenericEpsDao<SbmTransMsgPO> implements SbmTransMsgDao {

	private final static Logger LOG = LoggerFactory.getLogger(SbmTransMsgDaoImpl.class);

	/**
	 * INSERT_SBMTRANSMSG
	 */
	private String insertSbmTransMsgSql;
	/**
	 * SELECT_SBMTRANSMSG
	 */
	private String selectSbmTransMsgSql;
	/**
	 * SELECT_SBMTRANSMSG_REJECT_COUNT
	 */
	private String selectRejectCountSql;
	
	/**
	 * SELECT_SBMTRANSMSG_COUNTS
	 */
	private String selectSbmTransMsgCountsSql;

	
	/**
	 * Constructor
	 */
	public SbmTransMsgDaoImpl() {
		this.rowMapper = new SbmTransMsgRowMapper();
	}

	@Override
	public Long insertSbmTransMsg(final Long stagingSbmPolicyId, final SbmTransMsgPO po) {


		KeyHolder generatedKeyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(new PreparedStatementCreator() {

			/** 
			 * Creating a preparedStatement
			 * @param con
			 * @return preparedstatement
			 */
			public PreparedStatement createPreparedStatement(Connection con)
					throws ApplicationException {
				PreparedStatement ps = null;
				try {
					ps = con.prepareStatement(insertSbmTransMsgSql, new String[] { "SBMTRANSMSGID" });

					if (po.getTransMsgDateTime() != null) {
						ps.setTimestamp(1, Timestamp.valueOf(po.getTransMsgDateTime()));
					} else {
						ps.setObject(1, null);
					}
					ps.setString(2, po.getTransMsgDirectionTypeCd());
					ps.setString(3, po.getTransMsgTypeCd());
					ps.setLong(4, po.getSbmFileInfoId());
					ps.setString(5, po.getSubscriberStateCd());

					if (po.getRecordControlNum() != null) {
						ps.setInt(6, po.getRecordControlNum());
					} else {
						ps.setObject(6, null);
					}
					ps.setString(7, po.getPlanId());
					ps.setString(8, po.getSbmTransMsgProcStatusTypeCd());
					ps.setString(9, po.getExchangeAssignedPolicyId());
					ps.setString(10, po.getExchangeAssignedSubscriberId());
					ps.setString(11, userVO.getUserId());
					ps.setString(12, userVO.getUserId());
					ps.setLong(13, po.getSbmFileInfoId());
					ps.setLong(14, stagingSbmPolicyId);

					return ps;

				} catch (Exception e) {
					try {
						if (ps != null) {
							ps.close();
						}
					} catch (SQLException e1) {
						//just log ps.close() exception, original exception to be thrown
						LOG.error("Failed to close prepared statement in SbmTransMsgDaoImpl caused by: " + e1.toString());
					}
					throw new ApplicationException(EProdEnum.EPROD_10.getCode(), e);
				}
			}
		}, generatedKeyHolder);
		return generatedKeyHolder.getKey().longValue();
	}


	@Override
	public List<SbmTransMsgCountData> selectSbmTransMsgCounts(Long sbmFileProcSumId, String stateCd) {

		List<SbmTransMsgCountData> dataList = jdbcTemplate.query(selectSbmTransMsgCountsSql, 
				new SbmTransMsgCountDataRowMapper(), sbmFileProcSumId, stateCd);
		return dataList;
	}


	static private class SbmTransMsgCountDataRowMapper implements RowMapper<SbmTransMsgCountData> {

		/**
		 * Creates SbmTransMsgCountData
		 * @param rs
		 * @param rowNum
		 * @return missingPolicyData
		 */
		public SbmTransMsgCountData mapRow(ResultSet rs, int rowNum) throws SQLException {

			SbmTransMsgCountData cntData = new SbmTransMsgCountData();

			cntData.setStatus(SbmTransMsgStatus.getEnum(rs.getString("SBMTRANSMSGPROCSTATUSTYPECD")));
			cntData.setHasPolicyVersionId(rs.getBoolean("HASPOLICYVERSIONID"));
			cntData.setHasPriorPolicyVersionId(rs.getBoolean("HASPRIORPOLICYVERSIONID"));
			cntData.setCountStatus(rs.getInt("CNT_STATUS"));

			return cntData;
		}
	}



	@Override
	public List<SbmTransMsgPO> selectSbmTransMsg(Long sbmFileInfo) {

		return (List<SbmTransMsgPO>) jdbcTemplate.query(selectSbmTransMsgSql, rowMapper, sbmFileInfo);
	}

	@Override
	public Integer selectRejectCount(Long sbmFileProcSumId, String stateCd) {

		return jdbcTemplate.queryForObject(selectRejectCountSql, new Object[] {sbmFileProcSumId, stateCd}, Integer.class);
	}


	/**
	 * @param insertSbmTransMsgSql the insertSbmTransMsgSql to set
	 */
	public void setInsertSbmTransMsgSql(String insertSbmTransMsgSql) {
		this.insertSbmTransMsgSql = insertSbmTransMsgSql;
	}

	/**
	 * @param selectSbmTransMsgSql the selectSbmTransMsgSql to set
	 */
	public void setSelectSbmTransMsgSql(String selectSbmTransMsgSql) {
		this.selectSbmTransMsgSql = selectSbmTransMsgSql;
	}

	/**
	 * @param selectRejectCountSql the selectRejectCountSql to set
	 */
	public void setSelectRejectCountSql(String selectRejectCountSql) {
		this.selectRejectCountSql = selectRejectCountSql;
	}

	/**
	 * @param selectSbmTransMsgCountsSql the selectSbmTransMsgCountsSql to set
	 */
	public void setSelectSbmTransMsgCountsSql(String selectSbmTransMsgCountsSql) {
		this.selectSbmTransMsgCountsSql = selectSbmTransMsgCountsSql;
	}

}
