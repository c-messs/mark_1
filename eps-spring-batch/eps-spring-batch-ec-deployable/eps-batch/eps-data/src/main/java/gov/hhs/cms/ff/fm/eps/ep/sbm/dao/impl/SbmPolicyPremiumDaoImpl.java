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
import gov.hhs.cms.ff.fm.eps.ep.dao.SbmPolicyPremiumDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.mappers.SbmPolicyPremiumRowMapper;
import gov.hhs.cms.ff.fm.eps.ep.enums.EProdEnum;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyPremiumPO;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmPolicyPremiumPO;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;

/**
 * 
 * Implementation of SbmPolicyPremiumDao 
 *
 */
public class SbmPolicyPremiumDaoImpl extends GenericEpsDao<SbmPolicyPremiumPO> implements SbmPolicyPremiumDao {

	private final static Logger LOG = LoggerFactory.getLogger(SbmPolicyPremiumDaoImpl.class);
	
	private String selectPremiumListSql;
	private String insertPremiumListSql;
	private String mergePremiumSql;
	private String deleteStagingPremiumSql;


	/**
	 * Constructor
	 */
	public SbmPolicyPremiumDaoImpl() {

		this.setRowMapper(new SbmPolicyPremiumRowMapper());
	}

	@Override
	public void insertStagingPolicyPremiumList(final List<SbmPolicyPremiumPO> premiumList) {

		final String userId = super.userVO.getUserId();
		jdbcTemplate.batchUpdate(insertPremiumListSql, 
				new BatchPreparedStatementSetter() {

			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {

				SbmPolicyPremiumPO po = premiumList.get(i);
				ps.setLong(1, po.getPolicyVersionId());
				ps.setDate(2, DateTimeUtil.getSqlDate(po.getEffectiveStartDate()));
				ps.setDate(3, DateTimeUtil.getSqlDate(po.getEffectiveEndDate()));
				ps.setBigDecimal(4, po.getTotalPremiumAmount());
				ps.setBigDecimal(5, po.getOtherPaymentAmount1());
				ps.setBigDecimal(6, po.getOtherPaymentAmount2());
				ps.setString(7, po.getExchangeRateArea());
				ps.setBigDecimal(8, po.getIndividualResponsibleAmount());
				ps.setBigDecimal(9, po.getCsrAmount());
				ps.setBigDecimal(10, po.getAptcAmount());
				ps.setBigDecimal(11, po.getProratedPremiumAmount());
				ps.setBigDecimal(12, po.getProratedAptcAmount());
				ps.setBigDecimal(13, po.getProratedCsrAmount());
				ps.setString(14, po.getInsrncPlanVariantCmptTypeCd());
				ps.setString(15, userId);
				ps.setString(16, userId);
			}
			@Override
			public int getBatchSize() {
				return premiumList.size();
			}
		});	

	}

	@Override
	public List<SbmPolicyPremiumPO> selectPolicyPremiumList(Long policyVersionId) {

		return (List<SbmPolicyPremiumPO>) jdbcTemplate.query(selectPremiumListSql, rowMapper, policyVersionId);
	}


	@Override
	public BigInteger mergePolicyPremium(Long sbmFileProcSumId) {
		
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
					ps = con.prepareStatement(mergePremiumSql);

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
						LOG.error("Failed to close prepared statement in SbmPolicyPremiumDaoImpl caused by: " + e.toString());
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
					ps = con.prepareStatement(deleteStagingPremiumSql);

					ps.setLong(1, sbmFileProcSumId);

					return ps;

				} catch (Exception e) {
					try {
						if (ps != null) {
							ps.close();
						}
					} catch (SQLException e1) {
						LOG.error("Failed to close prepared statement in SbmPolicyPremiumDaoImpl caused by: " + e.toString());
					}
					throw new ApplicationException(EProdEnum.EPROD_10.getCode(), e);
				}
			}
		});
		return count;
	}
	
	@Override
	public void insertPolicyPremiumList(List<PolicyPremiumPO> premiums) {
		// Unimplemented for SBM
	}

	/**
	 * @param selectPremiumListSql the selectPremiumListSql to set
	 */
	public void setSelectPremiumListSql(String selectPremiumListSql) {
		this.selectPremiumListSql = selectPremiumListSql;
	}

	/**
	 * @param insertPremiumListSql the insertPremiumListSql to set
	 */
	public void setInsertPremiumListSql(String insertPremiumListSql) {
		this.insertPremiumListSql = insertPremiumListSql;
	}

	/**
	 * @param mergePremiumSql the mergePremiumSql to set
	 */
	public void setMergePremiumSql(String mergePremiumSql) {
		this.mergePremiumSql = mergePremiumSql;
	}

	/**
	 * @param deleteStagingPremiumSql the deleteStagingPremiumSql to set
	 */
	public void setDeleteStagingPremiumSql(String deleteStagingPremiumSql) {
		this.deleteStagingPremiumSql = deleteStagingPremiumSql;
	}

}
