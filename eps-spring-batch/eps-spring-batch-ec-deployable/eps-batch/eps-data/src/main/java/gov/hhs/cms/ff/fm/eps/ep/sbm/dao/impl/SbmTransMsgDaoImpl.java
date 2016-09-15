package gov.hhs.cms.ff.fm.eps.ep.sbm.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import com.accenture.foundation.common.exception.ApplicationException;

import gov.hhs.cms.ff.fm.eps.ep.dao.GenericEpsDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.SbmTransMsgDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.mappers.SbmTransMsgRowMapper;
import gov.hhs.cms.ff.fm.eps.ep.enums.EProdEnum;
import gov.hhs.cms.ff.fm.eps.ep.enums.SbmTransMsgStatus;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmTransMsgPO;

/**
 * 
 * SbmTransMsgDao implementation
 *
 */
public class SbmTransMsgDaoImpl extends GenericEpsDao<SbmTransMsgPO> implements SbmTransMsgDao {

	private final static Logger LOG = LoggerFactory.getLogger(SbmTransMsgDaoImpl.class);

	private String insertSbmTransMsgSql;
	private String selectSbmTransMsgSql;
	private String selectRejectCountSql;
	private String selectMatchCountSql;
	private String selectMatchCountCorrectedSql;
	private String selectNoMatchCountSql;

	/**
	 * Constructor
	 */
	public SbmTransMsgDaoImpl() {
		this.rowMapper = new SbmTransMsgRowMapper();
	}

	@Override
	public Long insertSbmTransMsg(final Long batchId, final Long stagingSbmPolicyId, final SbmTransMsgPO po) {


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
	public List<SbmTransMsgPO> selectSbmTransMsg(Long sbmFileInfo) {
		
		return (List<SbmTransMsgPO>) jdbcTemplate.query(selectSbmTransMsgSql, rowMapper, sbmFileInfo);
	}
	
	@Override
	public Integer selectRejectCount(Long sbmFileProcSumId) {
		
		return jdbcTemplate.queryForObject(selectRejectCountSql, new Object[] {sbmFileProcSumId}, Integer.class);
	}


	@Override
	public Integer selectMatchCount(Long sbmFileProcSumId, SbmTransMsgStatus status) {
		
		return jdbcTemplate.queryForObject(selectMatchCountSql, new Object[] {sbmFileProcSumId, status.getCode()}, Integer.class);
	}
	
	@Override
	public Integer selectMatchCountCorrected(Long sbmFileProcSumId) {

		return jdbcTemplate.queryForObject(selectMatchCountCorrectedSql, new Object[] {sbmFileProcSumId}, Integer.class);
	}

	@Override
	public Integer selectNoMatchCount(Long sbmFileProcSumId, SbmTransMsgStatus status) {
		
		return jdbcTemplate.queryForObject(selectNoMatchCountSql, new Object[] {sbmFileProcSumId, status.getCode()}, Integer.class);
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
	 * @param selectMatchCountSql the selectMatchCountSql to set
	 */
	public void setSelectMatchCountSql(String selectMatchCountSql) {
		this.selectMatchCountSql = selectMatchCountSql;
	}

	/**
	 * @param selectMatchCountCorrectedSql the selectMatchCountCorrectedSql to set
	 */
	public void setSelectMatchCountCorrectedSql(String selectMatchCountCorrectedSql) {
		this.selectMatchCountCorrectedSql = selectMatchCountCorrectedSql;
	}

	/**
	 * @param selectRejectCountSql the selectRejectCountSql to set
	 */
	public void setSelectRejectCountSql(String selectRejectCountSql) {
		this.selectRejectCountSql = selectRejectCountSql;
	}

	/**
	 * @param selectNoMatchCountSql the selectNoMatchCountSql to set
	 */
	public void setSelectNoMatchCountSql(String selectNoMatchCountSql) {
		this.selectNoMatchCountSql = selectNoMatchCountSql;
	}

	

}
