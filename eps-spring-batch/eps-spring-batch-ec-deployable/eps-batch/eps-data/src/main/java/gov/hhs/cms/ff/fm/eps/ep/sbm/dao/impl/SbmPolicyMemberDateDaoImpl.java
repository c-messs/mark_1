package gov.hhs.cms.ff.fm.eps.ep.sbm.dao.impl;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.PreparedStatementCreator;

import com.accenture.foundation.common.exception.ApplicationException;

import gov.hhs.cms.ff.fm.eps.ep.dao.GenericEpsDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.SbmPolicyMemberDateDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.mappers.SbmPolicyMemberDateRowMapper;
import gov.hhs.cms.ff.fm.eps.ep.enums.EProdEnum;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyMemberDatePO;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmPolicyMemberDatePO;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;

/**
 * 
 * Implementation of SbmPolicyMemberDateDao 
 *
 */
public class SbmPolicyMemberDateDaoImpl extends GenericEpsDao<SbmPolicyMemberDatePO> implements SbmPolicyMemberDateDao {
	
	private final static Logger LOG = LoggerFactory.getLogger(SbmPolicyMemberDateDaoImpl.class);

	private String selectDateListSql;
	private String insertDateListSql;
	private String mergeDateSql;
	private String deleteStagingDateSql;
	
	/**
	 * Constructor
	 */
	public SbmPolicyMemberDateDaoImpl() {

		this.rowMapper = new SbmPolicyMemberDateRowMapper();
	}
	
	@Override
	public List<SbmPolicyMemberDatePO> getPolicyMemberDate(Long policyVersionId) {
		
		return (List<SbmPolicyMemberDatePO>) jdbcTemplate.query(selectDateListSql, rowMapper, policyVersionId);
	}
	
	
	@Override
	public void insertStagingPolicyMemberDate(List<SbmPolicyMemberDatePO> dateList) {
		
		
		jdbcTemplate.batchUpdate(insertDateListSql, new BatchPreparedStatementSetter() {

			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {

				SbmPolicyMemberDatePO po = dateList.get(i);
				ps.setLong(1, po.getPolicyMemberVersionId());
				ps.setDate(2, DateTimeUtil.getSqlDate(po.getPolicyMemberStartDate()));
				ps.setDate(3, DateTimeUtil.getSqlDate(po.getPolicyMemberEndDate()));
				ps.setString(4, userVO.getUserId());
				ps.setString(5, userVO.getUserId());
			}
		
			@Override
			public int getBatchSize() {
				return dateList.size();
			}
		});	
	} 
	
	@Override
	public BigInteger mergePolicyMemberDate(Long sbmFileProcSumId) {
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
					ps = con.prepareStatement(mergeDateSql);

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
						LOG.error("Failed to close prepared statement in SbmPolicyMemberDateDaoImpl caused by: " + e.toString());
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
					ps = con.prepareStatement(deleteStagingDateSql);

					ps.setLong(1, sbmFileProcSumId);

					return ps;

				} catch (Exception e) {
					try {
						if (ps != null) {
							ps.close();
						}
					} catch (SQLException e1) {
						LOG.error("Failed to close prepared statement in SbmPolicyMemberDateDaoImpl caused by: " + e.toString());
					}
					throw new ApplicationException(EProdEnum.EPROD_10.getCode(), e);
				}
			}
		});
		return count;
	}
	

	
	
	/**
	 * @param selectDateListSql the selectDateListSql to set
	 */
	public void setSelectDateListSql(String sql) {
		this.selectDateListSql = sql;
	}
	/**
	 * @param insertDateListSql the insertDateListSql to set
	 */
	public void setInsertDateListSql(String sql) {
		this.insertDateListSql = sql;
	}
	
	/**
	 * @param mergeDateSql the mergeDateSql to set
	 */
	public void setMergeDateSql(String mergeDateSql) {
		this.mergeDateSql = mergeDateSql;
	}

	
	@Override
	public void insertPolicyMemberDates(List<PolicyMemberDatePO> poList) {
		// unimplemented method
		
	}
	@Override
	public List<PolicyMemberDatePO> getPolicyMemberDates(Long policyVersionId) {
		// unimplemented method
		return Collections.emptyList();
	}

	/**
	 * @param deleteStagingDateSql the deleteStagingDateSql to set
	 */
	public void setDeleteStagingDateSql(String deleteStagingDateSql) {
		this.deleteStagingDateSql = deleteStagingDateSql;
	}

	
}
