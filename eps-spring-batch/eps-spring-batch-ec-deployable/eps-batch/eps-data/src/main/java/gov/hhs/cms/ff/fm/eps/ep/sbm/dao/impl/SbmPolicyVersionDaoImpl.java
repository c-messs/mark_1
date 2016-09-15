package gov.hhs.cms.ff.fm.eps.ep.sbm.dao.impl;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;

import com.accenture.foundation.common.exception.ApplicationException;

import gov.hhs.cms.ff.fm.eps.ep.dao.GenericEpsDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.SbmPolicyVersionDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.mappers.SbmPolicyVersionRowMapper;
import gov.hhs.cms.ff.fm.eps.ep.enums.EProdEnum;
import gov.hhs.cms.ff.fm.eps.ep.enums.PolicyStatus;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyVersionPO;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmPolicyVersionPO;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;
import gov.hhs.cms.ff.fm.eps.ep.vo.PolicyVersionSearchCriteriaVO;

/**
 * @author j.radziewski
 *
 */
public class SbmPolicyVersionDaoImpl extends GenericEpsDao<SbmPolicyVersionPO> implements SbmPolicyVersionDao {

	private final static Logger LOG = LoggerFactory.getLogger(SbmPolicyVersionDaoImpl.class);
	
	private String selectPolicyMatchSql;
	private String selectPVSql;
	private String insertStagingPVSql;
	private String mergePVSql;
	private String selectPolicyCountByStatusSql;
	private String selectPolicyCountCancelledSql;
	private String deleteStagingPVSql;
	
	/**
	 * Constructor
	 */
	public SbmPolicyVersionDaoImpl() {
		this.rowMapper = new SbmPolicyVersionRowMapper();
	}

	@Override
	public List<PolicyVersionPO> findLatestPolicyVersion(PolicyVersionSearchCriteriaVO criteria) {
		Object[] args = new Object[] {
				criteria.getSubscriberStateCd(),
				criteria.getExchangePolicyId()
		};

		List<PolicyVersionPO> poList = (List<PolicyVersionPO>) jdbcTemplate
				.query(selectPolicyMatchSql, new PolicyMatchSbmRowMapper(), args);

		return poList;
	}
	
	@Override
	public SbmPolicyVersionPO getPolicyVersionById(Long policyVersionId, String subscriberStateCd) {
		
		return (SbmPolicyVersionPO) jdbcTemplate.queryForObject(selectPVSql, rowMapper, subscriberStateCd, policyVersionId);
	}

	@Override
	public Long insertStagingPolicyVersion(final SbmPolicyVersionPO po) {

		final String userId = super.userVO.getUserId();
		final Long policyVersionId = sequenceHelper.nextSequenceId("POLICYVERSIONSEQ");

		jdbcTemplate.update(insertStagingPVSql, new PreparedStatementSetter() {
			public void setValues(PreparedStatement ps) throws SQLException {
				ps.setLong(1, policyVersionId);
				ps.setString(2, po.getSubscriberStateCd());
				ps.setString(3, po.getExchangePolicyId());
				ps.setTimestamp(4, DateTimeUtil.getSqlTimestamp(LocalDateTime.now()));
				ps.setTimestamp(5, DateTimeUtil.getSqlTimestamp(DateTimeUtil.HIGHDATE));
				ps.setString(6, po.getIssuerPolicyId());
				ps.setString(7, po.getIssuerHiosId());
				ps.setString(8, po.getIssuerSubscriberID());
				ps.setString(9, po.getExchangeAssignedSubscriberID());
				ps.setString(10, po.getTransControlNum());
				ps.setString(11, po.getSourceExchangeId());
				ps.setString(12, po.getPlanID());
				ps.setString(13, po.getX12InsrncLineTypeCd());
				ps.setLong(14, po.getSbmTransMsgId());
				ps.setDate(15, DateTimeUtil.getSqlDate(po.getPolicyStartDate()));				
				ps.setDate(16, DateTimeUtil.getSqlDate(po.getPolicyEndDate()));
				if (po.getPreviousPolicyVersionId() != null) {
					ps.setLong(17, po.getPreviousPolicyVersionId());
				} else {
					ps.setObject(17,  null);
				}
				ps.setString(18, userId);
				ps.setString(19, userId);
			}
		});	

		return policyVersionId;
	}
	
	
	@Override
	public BigInteger mergePolicyVersion(final Long sbmFileProcSumId) {

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
					ps = con.prepareStatement(mergePVSql);
					ps.setLong(1, sbmFileProcSumId);
					ps.setLong(2, sbmFileProcSumId);
					ps.setString(3, userVO.getUserId());
					ps.setString(4, userVO.getUserId());
					ps.setString(5, userVO.getUserId());
					
					return ps;

				} catch (Exception e) {
					try {
						if (ps != null) {
							ps.close();
						}
					} catch (SQLException e1) {
						LOG.error("Failed to close prepared statement in SbmPolicyVersionDaoImpl caused by: " + e.toString());
					}
					throw new ApplicationException(EProdEnum.EPROD_10.getCode(), e);
				}
			}
		});
		return BigInteger.valueOf(count);
		
	}
	
	
	@Override
	public BigInteger selectPolicyCountByStatus(final Long sbmFileProcSumId, final String stateCd, PolicyStatus policyStatus) {
		
		return jdbcTemplate.queryForObject(selectPolicyCountByStatusSql, new Object[] {sbmFileProcSumId, stateCd, policyStatus.getValue()}, BigInteger.class);
	}
	
	
	@Override
	public BigInteger selectCountEffectuatedPoliciesCancelled(Long sbmFileProcSumId, String stateCd) {

		return jdbcTemplate.queryForObject(selectPolicyCountCancelledSql, new Object[] {sbmFileProcSumId, stateCd}, BigInteger.class);
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
					ps = con.prepareStatement(deleteStagingPVSql);

					ps.setLong(1, sbmFileProcSumId);

					return ps;

				} catch (Exception e) {
					try {
						if (ps != null) {
							ps.close();
						}
					} catch (SQLException e1) {
						LOG.error("Failed to close prepared statement in SbmPolicyVersionDaoImpl caused by: " + e.toString());
					}
					throw new ApplicationException(EProdEnum.EPROD_10.getCode(), e);
				}
			}
		});
		return count;
	}
	

	@Override
	public Long insertPolicyVersion(PolicyVersionPO pv) {
		// TODO Auto-generated method stub
		return null;
	}

	

	@Override
	public LocalDateTime getLatestPolicyMaintenanceStartDateTime() {
		// TODO Auto-generated method stub
		return null;
	}
	

	static private class PolicyMatchSbmRowMapper implements RowMapper<PolicyVersionPO> {

		/**
		 * @param rs
		 * @param rowNum
		 * @return po
		 */
		public PolicyVersionPO mapRow(ResultSet rs, int rowNum) throws SQLException {

			PolicyVersionPO po = new PolicyVersionPO();

			po.setPolicyVersionId(rs.getLong("POLICYVERSIONID"));
			po.setPlanID(rs.getString("PLANID"));
			po.setX12InsrncLineTypeCd(rs.getString("X12INSRNCLINETYPECD"));

			return po;
		}
	}

	/**
	 * @param selectPolicyMatchSql the selectPolicyMatchSql to set
	 */
	public void setSelectPolicyMatchSql(String selectPolicyMatchSql) {
		this.selectPolicyMatchSql = selectPolicyMatchSql;
	}

	/**
	 * @param selectPVSql the selectPVSql to set
	 */
	public void setSelectPVSql(String selectPVSql) {
		this.selectPVSql = selectPVSql;
	}

	/**
	 * @param insertStagingPVSql the insertStagingPVSql to set
	 */
	public void setInsertStagingPVSql(String insertStagingPVSql) {
		this.insertStagingPVSql = insertStagingPVSql;
	}

	/**
	 * @param mergePVSql the mergePVSql to set
	 */
	public void setMergePVSql(String mergePVSql) {
		this.mergePVSql = mergePVSql;
	}

	/**
	 * @param selectPolicyCountByStatusSql the selectPolicyCountByStatusSql to set
	 */
	public void setSelectPolicyCountByStatusSql(String selectPolicyCountByStatusSql) {
		this.selectPolicyCountByStatusSql = selectPolicyCountByStatusSql;
	}

	/**
	 * @param deleteStagingPVSql the deleteStagingPVSql to set
	 */
	public void setDeleteStagingPVSql(String deleteStagingPVSql) {
		this.deleteStagingPVSql = deleteStagingPVSql;
	}

	/**
	 * @param selectPolicyCountCancelledSql the selectPolicyCountCancelledSql to set
	 */
	public void setSelectPolicyCountCancelledSql(String selectPolicyCountCancelledSql) {
		this.selectPolicyCountCancelledSql = selectPolicyCountCancelledSql;
	}

}
