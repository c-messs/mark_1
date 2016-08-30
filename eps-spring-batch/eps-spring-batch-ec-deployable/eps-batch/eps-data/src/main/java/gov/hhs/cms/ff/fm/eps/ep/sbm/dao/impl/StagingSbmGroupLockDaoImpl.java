package gov.hhs.cms.ff.fm.eps.ep.sbm.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.PreparedStatementCreator;

import com.accenture.foundation.common.exception.ApplicationException;

import gov.hhs.cms.ff.fm.eps.ep.dao.GenericEpsDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.StagingSbmGroupLockDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.mappers.StagingSbmGroupLockRowMapper;
import gov.hhs.cms.ff.fm.eps.ep.enums.EProdEnum;
import gov.hhs.cms.ff.fm.eps.ep.po.StagingSbmGroupLockPO;

public class StagingSbmGroupLockDaoImpl extends GenericEpsDao<StagingSbmGroupLockPO> implements StagingSbmGroupLockDao {
	
	private final static Logger LOG = LoggerFactory.getLogger(StagingSbmGroupLockDaoImpl.class);
	

	private String selectStagingPolicySummaryIdSql;
	private String insertStagingGroupLockSql;
	private String selectStagingGroupLockZeroSql;
	private String selectStagingGroupLockSbmrSql;
	private String updateStagingGroupLockSql;
	private String updateStagingGroupLockForExtractSql;
	private String deleteStagingGroupLockSql;
	private String deleteStagingGroupLockForExtractSql;

	private JdbcTemplate jdbcTemplate;
	
	
	public StagingSbmGroupLockDaoImpl() {
		
		this.rowMapper = new StagingSbmGroupLockRowMapper();
	}
	
	@Override
	public List<StagingSbmGroupLockPO> selectSbmFileProcessingSummaryIdList() {
		
		return (List<StagingSbmGroupLockPO>) jdbcTemplate.query(selectStagingPolicySummaryIdSql, rowMapper);
	}

	@Override
	public void insertStagingSbmGroupLock(final List<StagingSbmGroupLockPO> poList) {
		
		jdbcTemplate.batchUpdate(insertStagingGroupLockSql, new BatchPreparedStatementSetter() {

			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {

				StagingSbmGroupLockPO po = poList.get(i);
				
				ps.setLong(1, po.getSbmFileProcSumId());
				ps.setLong(2, po.getProcessingGroupId());
				ps.setString(3, userVO.getUserId());
				ps.setString(4, userVO.getUserId());
			}
			@Override
			public int getBatchSize() {
				return poList.size();
			}
		});	
	}
	

	@Override
	public List<StagingSbmGroupLockPO> selectStagingGroupLockZero(Long batchId) {

		return (List<StagingSbmGroupLockPO>) jdbcTemplate.query(selectStagingGroupLockZeroSql, rowMapper, batchId);
	}
	
	
	@Override
	public List<StagingSbmGroupLockPO> selectStagingGroupLockSbmr() {

		return (List<StagingSbmGroupLockPO>) jdbcTemplate.query(selectStagingGroupLockSbmrSql, rowMapper);
	}
	
	
	@Override
	public boolean updateStagingGroupLock(final Long sbmFileProcSumId, final Long batchId) {
		
		return jdbcTemplate.execute(updateStagingGroupLockSql, new PreparedStatementCallback<Boolean>() {

			@Override
			public Boolean doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {

				ps.setLong(1, batchId);
				ps.setLong(2, sbmFileProcSumId);
			
				int rowsEffected = ps.executeUpdate();

				return (rowsEffected == 1);
			}
		});	
	}
	
	@Override
	public boolean updateStagingGroupLockForExtract(final Long batchId) {
		
		return jdbcTemplate.execute(updateStagingGroupLockForExtractSql, new PreparedStatementCallback<Boolean>() {

			@Override
			public Boolean doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {

				ps.setLong(1, batchId);
			
				int rowsEffected = ps.executeUpdate();

				return (rowsEffected == 1);
			}
		});	
	}


	@Override
	public int deleteStagingGroupLock(final Long sbmFileProcSumId, final Long batchId) {
		
		int count = jdbcTemplate.update(new PreparedStatementCreator() {
			/** 
			 * Creating a preparedStatement
			 * @param con
			 * @return preparedstatement
			 */
			public PreparedStatement createPreparedStatement(Connection con)
					throws ApplicationException {
				PreparedStatement ps = null;
				try {
					ps = con.prepareStatement(deleteStagingGroupLockSql);

					ps.setLong(1, sbmFileProcSumId);
					ps.setLong(2, batchId);

					return ps;

				} catch (Exception e) {
					try {
						if (ps != null) {
							ps.close();
						}
					} catch (SQLException e1) {
						LOG.error("Failed to close prepared statement in StagingSbmGroupLockDaoImpl caused by: " + e.toString());
					}
					throw new ApplicationException(EProdEnum.EPROD_10.getCode(), e);
				}
			}
		});
		return count;
	}
	
	@Override
	public int deleteStagingGroupLockForExtract(final Long batchId) {
		
		int count = jdbcTemplate.update(new PreparedStatementCreator() {
			/** 
			 * Creating a preparedStatement
			 * @param con
			 * @return preparedstatement
			 */
			public PreparedStatement createPreparedStatement(Connection con)
					throws ApplicationException {
				PreparedStatement ps = null;
				try {
					ps = con.prepareStatement(deleteStagingGroupLockForExtractSql);

					ps.setLong(1, batchId);

					return ps;

				} catch (Exception e) {
					try {
						if (ps != null) {
							ps.close();
						}
					} catch (SQLException e1) {
						LOG.error("Failed to close prepared statement in StagingSbmGroupLockDaoImpl caused by: " + e.toString());
					}
					throw new ApplicationException(EProdEnum.EPROD_10.getCode(), e);
				}
			}
		});
		return count;
	}

	/**
	 * @param selectStagingPolicySummaryIdSql the selectStagingPolicySummaryIdSql to set
	 */
	public void setSelectStagingPolicySummaryIdSql(String selectStagingPolicySummaryIdSql) {
		this.selectStagingPolicySummaryIdSql = selectStagingPolicySummaryIdSql;
	}

	/**
	 * @param insertStagingGroupLockSql the insertStagingGroupLockSql to set
	 */
	public void setInsertStagingGroupLockSql(String insertStagingGroupLockSql) {
		this.insertStagingGroupLockSql = insertStagingGroupLockSql;
	}

	/**
	 * @param selectStagingGroupLockZeroSql the selectStagingGroupLockZeroSql to set
	 */
	public void setSelectStagingGroupLockZeroSql(String selectStagingGroupLockZeroSql) {
		this.selectStagingGroupLockZeroSql = selectStagingGroupLockZeroSql;
	}

	/**
	 * @param selectStagingGroupLockSbmrSql the selectStagingGroupLockSbmrSql to set
	 */
	public void setSelectStagingGroupLockSbmrSql(String selectStagingGroupLockSbmrSql) {
		this.selectStagingGroupLockSbmrSql = selectStagingGroupLockSbmrSql;
	}

	/**
	 * @param updateStagingGroupLockSql the updateStagingGroupLockSql to set
	 */
	public void setUpdateStagingGroupLockSql(String updateStagingGroupLockSql) {
		this.updateStagingGroupLockSql = updateStagingGroupLockSql;
	}

	/**
	 * @param updateStagingGroupLockForExtractSql the updateStagingGroupLockForExtractSql to set
	 */
	public void setUpdateStagingGroupLockForExtractSql(String updateStagingGroupLockForExtractSql) {
		this.updateStagingGroupLockForExtractSql = updateStagingGroupLockForExtractSql;
	}

	/**
	 * @param deleteStagingGroupLockSql the deleteStagingGroupLockSql to set
	 */
	public void setDeleteStagingGroupLockSql(String deleteStagingGroupLockSql) {
		this.deleteStagingGroupLockSql = deleteStagingGroupLockSql;
	}

	/**
	 * @param deleteStagingGroupLockForExtractSql the deleteStagingGroupLockForExtractSql to set
	 */
	public void setDeleteStagingGroupLockForExtractSql(String deleteStagingGroupLockForExtractSql) {
		this.deleteStagingGroupLockForExtractSql = deleteStagingGroupLockForExtractSql;
	}

	/**
	 * @param jdbcTemplate the jdbcTemplate to set
	 */
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
}
