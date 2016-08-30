package gov.hhs.cms.ff.fm.eps.ep.sbm.dao.impl;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;

import com.accenture.foundation.common.exception.ApplicationException;

import gov.hhs.cms.ff.fm.eps.ep.dao.GenericEpsDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.SbmPolicyStatusDao;
import gov.hhs.cms.ff.fm.eps.ep.enums.EProdEnum;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyStatusPO;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmPolicyStatusPO;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;

/**
 * @author j.radziewski
 *
 */
public class SbmPolicyStatusDaoImpl extends GenericEpsDao<SbmPolicyStatusPO> implements SbmPolicyStatusDao {

	private final static Logger LOG = LoggerFactory.getLogger(SbmPolicyStatusDaoImpl.class);
	
	private String selectStatusListSql;
	private String insertStatusListSql;
	private String mergeStatusSql;
	private String selectStatusLatestSql;
	private String deleteStagingStatusSql;
	
    
	
	public SbmPolicyStatusDaoImpl() {

		this.rowMapper = new SbmPolicyStatusRowMapper();
	}


	@Override
	public void insertStagingPolicyStatusList(List<SbmPolicyStatusPO> statusList) {
		
		jdbcTemplate.batchUpdate(insertStatusListSql, 
				new BatchPreparedStatementSetter() {

			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {

				SbmPolicyStatusPO po = statusList.get(i);
				ps.setLong(1, po.getPolicyVersionId());
				ps.setTimestamp(2, DateTimeUtil.getSqlTimestamp(po.getTransDateTime()));
				ps.setString(3, po.getInsuranacePolicyStatusTypeCd());
				ps.setString(4, userVO.getUserId());
				ps.setString(5, userVO.getUserId());
			}
			@Override
			public int getBatchSize() {
				return statusList.size();
			}
		});	
	}
	
	
	@Override
	public List<SbmPolicyStatusPO> getPolicyStatusListAsSbm(Long policyVersionId) {
		
		return (List<SbmPolicyStatusPO>) jdbcTemplate.query(selectStatusListSql, rowMapper, policyVersionId);
	}
	
	
	@Override
	public BigInteger mergePolicyStatus(Long sbmFileProcSumId) {
		
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
					ps = con.prepareStatement(mergeStatusSql);

					ps.setString(1, userVO.getUserId());
					ps.setString(2, userVO.getUserId());
					ps.setLong(3, sbmFileProcSumId);
					
					return ps;

				} catch (Exception e) {
					try {
						if (ps != null) {
							ps.close();
						}
					} catch (SQLException e1) {
						LOG.error("Failed to close prepared statement in SbmPolicyStatusDaoImpl caused by: " + e.toString());
					}
					throw new ApplicationException(EProdEnum.EPROD_10.getCode(), e);
				}
			}
		});
		return BigInteger.valueOf(count);
	}
	
	@Override
	public int deleteStaging(Long sbmFileProcSumId) {

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
					ps = con.prepareStatement(deleteStagingStatusSql);

					ps.setLong(1, sbmFileProcSumId);

					return ps;

				} catch (Exception e) {
					try {
						if (ps != null) {
							ps.close();
						}
					} catch (SQLException e1) {
						LOG.error("Failed to close prepared statement in SbmPolicyStatusDaoImpl caused by: " + e.toString());
					}
					throw new ApplicationException(EProdEnum.EPROD_10.getCode(), e);
				}
			}
		});
		return count;
	}
	
	@Override
	public SbmPolicyStatusPO getPolicyStatusLatest(Long policyVersionId) {

		return (SbmPolicyStatusPO) jdbcTemplate.query(selectStatusLatestSql, rowMapper, policyVersionId);
	}
	
	@Override
	public List<PolicyStatusPO> getPolicyStatusList(Long policyVersionId) {
		
		return null;
	}
	
	static private class SbmPolicyStatusRowMapper implements RowMapper<SbmPolicyStatusPO> {

		public SbmPolicyStatusPO mapRow(ResultSet rs, int rowNum) throws SQLException {

			SbmPolicyStatusPO po = new SbmPolicyStatusPO();

			po.setTransDateTime(DateTimeUtil.getLocalDateTimeFromSqlTimestamp(rs.getTimestamp("TRANSDATETIME")));
			po.setInsuranacePolicyStatusTypeCd(rs.getString("INSURANACEPOLICYSTATUSTYPECD"));			

			return po;
		}
	}
	
	
	/**
	 * @param selectStatusListSql the selectStatusListSql to set
	 */
	public void setSelectStatusListSql(String sql) {
		this.selectStatusListSql = sql;
	}

	/**
	 * @param insertStatusListSql the insertStatusListSql to set
	 */
	public void setInsertStatusListSql(String sql) {
		this.insertStatusListSql = sql;
	}

	@Override
	public void insertPolicyStatusList(List<PolicyStatusPO> psl) {
		;
	}

	/**
	 * @param mergeStatusSql the mergeStatusSql to set
	 */
	public void setMergeStatusSql(String mergeStatusSql) {
		this.mergeStatusSql = mergeStatusSql;
	}


	/**
	 * @param selectStatusLatestSql the selectStatusLatestSql to set
	 */
	public void setSelectStatusLatestSql(String selectStatusLatestSql) {
		this.selectStatusLatestSql = selectStatusLatestSql;
	}


	/**
	 * @param deleteStagingStatusSql the deleteStagingStatusSql to set
	 */
	public void setDeleteStagingStatusSql(String deleteStagingStatusSql) {
		this.deleteStagingStatusSql = deleteStagingStatusSql;
	}

	
}
