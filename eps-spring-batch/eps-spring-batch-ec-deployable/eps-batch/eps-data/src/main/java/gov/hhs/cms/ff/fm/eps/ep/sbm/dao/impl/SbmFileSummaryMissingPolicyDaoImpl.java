package gov.hhs.cms.ff.fm.eps.ep.sbm.dao.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;

import gov.hhs.cms.ff.fm.eps.ep.dao.GenericEpsDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.SbmFileSummaryMissingPolicyDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.mappers.SbmFileSummaryMissingPolicyRowMapper;
import gov.hhs.cms.ff.fm.eps.ep.enums.PolicyStatus;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmFileProcessingSummaryPO;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmFileSummaryMissingPolicyData;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmFileSummaryMissingPolicyPO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SbmDataUtil;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;

/**
 * 
 * Implementation of SbmFileSummaryMissingPolicyDao 
 *
 */
public class SbmFileSummaryMissingPolicyDaoImpl extends GenericEpsDao<SbmFileSummaryMissingPolicyPO> implements SbmFileSummaryMissingPolicyDao {

	/**
	 * FIND_INSERT_MISSING_POLICY_BY_ISSUER
	 */
	private String findInsertMissingPolicyByIssuerSql;
	/**
	 * FIND_INSERT_MISSING_POLICY_BY_STATE
	 */
	private String findInsertMissingPolicyByStateSql;
	/**
	 * SELECT_MISSING_POLICY_DATA
	 */
	private String selectMissingPolicyDataSql;

	private SbmMissingPolicyRowMapper missingPolicyRowMapper;


	/**
	 * Constructor
	 */
	public SbmFileSummaryMissingPolicyDaoImpl() {

		this.rowMapper = new SbmFileSummaryMissingPolicyRowMapper();
		missingPolicyRowMapper = new SbmMissingPolicyRowMapper();
	}	


	@Override
	public int findAndInsertMissingPolicy(SbmFileProcessingSummaryPO summaryPO) {

		int recordCnt = 0;
		String stateCd = SbmDataUtil.getStateCd(summaryPO.getTenantId());

		if (summaryPO.getIssuerId() != null) {
			
			recordCnt = findAndInsertMissingPolicyByIssuer(summaryPO.getSbmFileProcSumId(), stateCd, summaryPO.getIssuerId(), summaryPO.getCoverageYear());
		} else {
			
			recordCnt = findAndInsertMissingPolicyByState(summaryPO.getSbmFileProcSumId(), stateCd, summaryPO.getCoverageYear());
		}
		return recordCnt;
	}
	
	

	private int findAndInsertMissingPolicyByIssuer(final Long sbmFileProcSumId, final String stateCd, final String issuerId, int coverageYear) {

		int[] rowsAffected = jdbcTemplate.batchUpdate(findInsertMissingPolicyByIssuerSql, new BatchPreparedStatementSetter() {

			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {

				ps.setLong(1, sbmFileProcSumId);
				ps.setString(2, userVO.getUserId());
				ps.setString(3, userVO.getUserId());
				ps.setLong(4, sbmFileProcSumId);
				ps.setString(5, stateCd);
				ps.setString(6, issuerId);
				ps.setInt(7, coverageYear);
			}

			@Override
			public int getBatchSize() {
				return 1;
			}
		});	

		return rowsAffected.length;		
	}

	private int findAndInsertMissingPolicyByState(final Long sbmFileProcSumId, final String stateCd, int coverageYear) {

		int[] rowsAffected = jdbcTemplate.batchUpdate(findInsertMissingPolicyByStateSql, new BatchPreparedStatementSetter() {

			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {

				ps.setLong(1, sbmFileProcSumId);
				ps.setString(2, userVO.getUserId());
				ps.setString(3, userVO.getUserId());
				ps.setLong(4, sbmFileProcSumId);
				ps.setString(5, stateCd);
				ps.setInt(6, coverageYear);
			}

			@Override
			public int getBatchSize() {
				return 1;
			}
		});	

		return rowsAffected.length;		
	}


	@Override
	public List<SbmFileSummaryMissingPolicyData> selectMissingPolicyList(Long sbmFileProcSumId) {

		return (List<SbmFileSummaryMissingPolicyData>) jdbcTemplate.query(selectMissingPolicyDataSql, missingPolicyRowMapper, sbmFileProcSumId);
	}


	static private class SbmMissingPolicyRowMapper implements RowMapper<SbmFileSummaryMissingPolicyData> {
		
		/**
		 * Creates SbmFileSummaryMissingPolicyData
		 * @param rs
		 * @param rowNum
		 * @return missingPolicyData
		 */
		public SbmFileSummaryMissingPolicyData mapRow(ResultSet rs, int rowNum) throws SQLException {

			SbmFileSummaryMissingPolicyData missingPolicyData = new SbmFileSummaryMissingPolicyData();

			missingPolicyData.setMissingPolicyVersionId(rs.getLong("MISSINGPOLICYVERSIONID"));
			missingPolicyData.setExchangePolicyId(rs.getString("EXCHANGEPOLICYID"));
			missingPolicyData.setPlanId(rs.getString("PLANID"));
			missingPolicyData.setPolicyStatus(PolicyStatus.getEnum(rs.getString("INSURANACEPOLICYSTATUSTYPECD")));

			missingPolicyData.setPolicyEndDate(DateTimeUtil.getLocalDateFromSqlDate(rs.getDate("POLICYENDDATE")));

			return missingPolicyData;
		}
	}



	/**
	 * @param findInsertMissingPolicyByIssuerSql the findInsertMissingPolicyByIssuerSql to set
	 */
	public void setFindInsertMissingPolicyByIssuerSql(String findInsertMissingPolicyByIssuerSql) {
		this.findInsertMissingPolicyByIssuerSql = findInsertMissingPolicyByIssuerSql;
	}


	/**
	 * @param findInsertMissingPolicyByStateSql the findInsertMissingPolicyByStateSql to set
	 */
	public void setFindInsertMissingPolicyByStateSql(String findInsertMissingPolicyByStateSql) {
		this.findInsertMissingPolicyByStateSql = findInsertMissingPolicyByStateSql;
	}


	/**
	 * @param selectMissingPolicyDataSql the selectMissingPolicyDataSql to set
	 */
	public void setSelectMissingPolicyDataSql(String selectMissingPolicyDataSql) {
		this.selectMissingPolicyDataSql = selectMissingPolicyDataSql;
	}


	/**
	 * @param missingPolicyRowMapper the missingPolicyRowMapper to set
	 */
	public void setMissingPolicyRowMapper(SbmMissingPolicyRowMapper missingPolicyRowMapper) {
		this.missingPolicyRowMapper = missingPolicyRowMapper;
	}

}
