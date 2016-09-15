package gov.hhs.cms.ff.fm.eps.ep.sbm.dao.impl;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.PreparedStatementCreator;

import com.accenture.foundation.common.exception.ApplicationException;

import gov.hhs.cms.ff.fm.eps.ep.dao.GenericEpsDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.SbmPolicyMemberDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.mappers.SbmPolicyMemberRowMapper;
import gov.hhs.cms.ff.fm.eps.ep.enums.EProdEnum;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyMemberPO;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmPolicyMemberPO;

/**
 * @author j.radziewski
 *
 */
public class SbmPolicyMemberDaoImpl extends GenericEpsDao<SbmPolicyMemberPO> implements SbmPolicyMemberDao {
	
	private final static Logger LOG = LoggerFactory.getLogger(SbmPolicyMemberDaoImpl.class);
	
	private String insertPMListSql;
	private String mergePMSql;
	private String deleteStagingPMSql;
	
	/**
	 * Constructor
	 */
	public SbmPolicyMemberDaoImpl() {
		
		this.rowMapper = new SbmPolicyMemberRowMapper();
	}

	@Override
	public void insertStagingPolicyMember(List<SbmPolicyMemberPO> pmList) {
		
		jdbcTemplate.batchUpdate(insertPMListSql, new BatchPreparedStatementSetter() {

			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {

				SbmPolicyMemberPO po = pmList.get(i);
				ps.setLong(1, po.getPolicyVersionId());
				ps.setLong(2, po.getPolicyMemberVersionId());
				ps.setString(3, po.getSubscriberStateCd());
				ps.setString(4, userVO.getUserId());
				ps.setString(5, userVO.getUserId());
			}
		
			@Override
			public int getBatchSize() {
				return pmList.size();
			}
		});	
		
	}
	
	@Override
	public BigInteger mergePolicyMember(Long sbmFileProcSumId) {

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
					ps = con.prepareStatement(mergePMSql);

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
						LOG.error("Failed to close prepared statement in SbmPolicyMemberDaoImpl caused by: " + e.toString());
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
					ps = con.prepareStatement(deleteStagingPMSql);

					ps.setLong(1, sbmFileProcSumId);

					return ps;

				} catch (Exception e) {
					try {
						if (ps != null) {
							ps.close();
						}
					} catch (SQLException e1) {
						LOG.error("Failed to close prepared statement in SbmPolicyMemberDaoImpl caused by: " + e.toString());
					}
					throw new ApplicationException(EProdEnum.EPROD_10.getCode(), e);
				}
			}
		});
		return count;
	}

	
	
	@Override
	public void insertPolicyMembers(List<PolicyMemberPO> poList) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @param insertPMListSql the insertPMListSql to set
	 */
	public void setInsertPMListSql(String sql) {
		this.insertPMListSql = sql;
	}

	/**
	 * @param mergePMSql the mergePMSql to set
	 */
	public void setMergePMSql(String mergePMSql) {
		this.mergePMSql = mergePMSql;
	}

	/**
	 * @param deleteStagingPMSql the deleteStagingPMSql to set
	 */
	public void setDeleteStagingPMSql(String deleteStagingPMSql) {
		this.deleteStagingPMSql = deleteStagingPMSql;
	}



}
