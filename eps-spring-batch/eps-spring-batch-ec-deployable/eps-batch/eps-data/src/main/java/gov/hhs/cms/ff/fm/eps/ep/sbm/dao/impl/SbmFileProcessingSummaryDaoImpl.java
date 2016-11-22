package gov.hhs.cms.ff.fm.eps.ep.sbm.dao.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;

import gov.hhs.cms.ff.fm.eps.ep.dao.GenericEpsDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.SbmFileProcessingSummaryDao;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmFileProcessingSummaryPO;

/**
 * @author j.radziewski
 *
 */
public class SbmFileProcessingSummaryDaoImpl extends GenericEpsDao<SbmFileProcessingSummaryPO> implements SbmFileProcessingSummaryDao {

	/**
	 * MATCH_FILE_PROC_SUM
	 */
	private String matchSbmFileProcSumSql;
	/**
	 * SELECT_FILE_PROC_SUM
	 */
	private String selectSbmFileProcSumSql;
	/**
	 * FIND_FILE_PROC_SUM
	 */
	private String findSbmFileProcSumSql;
    /**
     * FIND_FILE_PROC_SUM_FILENAME
     */
    private String findSbmFileProcSumFileNameSql;
	/**
	 * FIND_FILE_PROC_SUM_STATUS
	 */
	private String findSbmFileProcSumFileStatusSql;
	/**
	 * FIND_FILE_PROC_SUM_STATUS_STATECD
	 */
	private String findSbmFileProcSumFileStatusStateCdSql;
	/**
	 * INSERT_FILE_PROC_SUM
	 */
	private String insertSbmFileProcSumSql;
	/**
	 * UPDATE_FILE_PROC_SUM_STATUS
	 */
	private String updateSbmFileProcSumStatusSql;
	/**
	 * SELECT_FILE_PROC_SUM_LATEST_APPROVED_STATECD
	 */
	private String selectSbmFileProcSumLatestApprovedStateCdSql;
	/**
	 * SELECT_FILE_PROC_SUM_LATEST_APPROVED_ISSUERID
	 */
	private String selectSbmFileProcSumLatestApprovedIssuerIdSql;
	/**
	 * VERIFY_JOB_RUNNING
	 */
	private String verifyJobRunningSql;
	/**
	 * UPDATE_FILE_PROC_SUM
	 */
	private String updateSbmFileProcSumSql;
	/**
	 * UPDATE_FILE_PROC_SUM_CMS_APPROVED
	 */
	private String updateSbmFileProcSumCmsApprovedSql;
	/**
	 * VERIFY_CMS_APPROVAL_REQ
	 */
	private String verifyCmsApprovalReqSql;

	/**
	 * Constructor
	 */
	public SbmFileProcessingSummaryDaoImpl() {

		this.rowMapper = new SbmFileProcSumRowMapper();

	}


	@Override
	public List<SbmFileProcessingSummaryPO> performSummaryMatch(String sbmFileId, String tenantId) {

		return (List<SbmFileProcessingSummaryPO>) jdbcTemplate.query(matchSbmFileProcSumSql, rowMapper, sbmFileId, tenantId);
	}


	@Override
	public SbmFileProcessingSummaryPO selectSbmFileProcessingSummary(Long sbmFileProcSumId) {


		return (SbmFileProcessingSummaryPO) jdbcTemplate.queryForObject(selectSbmFileProcSumSql, rowMapper, sbmFileProcSumId);
	}

	@Override
	public SbmFileProcessingSummaryPO selectSbmFileProcessingSummaryLatestApproved(String stateCd, String issuerId) {

		SbmFileProcessingSummaryPO po = null;
		String arg = null;
		String sql = null;
		if (stateCd != null) {
			arg = stateCd + "%";
			sql = selectSbmFileProcSumLatestApprovedStateCdSql;
		} else {
			arg = issuerId;
			sql = selectSbmFileProcSumLatestApprovedIssuerIdSql;
		}
		List<SbmFileProcessingSummaryPO> poList =  (List<SbmFileProcessingSummaryPO>) jdbcTemplate.query(sql, rowMapper, arg);
		if (!poList.isEmpty()) {
			// ORDER BY LASTMODIFIEDDATETIME DESC
			po = poList.get(0);
		}
		return po;
	}



	@Override
	public List<SbmFileProcessingSummaryPO> findSbmFileProcessingSummary(String issuerId, String fileSetId, String tenantId) {

		return (List<SbmFileProcessingSummaryPO>) jdbcTemplate.query(findSbmFileProcSumSql, rowMapper, issuerId, fileSetId, tenantId);
	}

	@Override
	public List<SbmFileProcessingSummaryPO> findSbmFileProcessingSummary(String fileName) {

		return (List<SbmFileProcessingSummaryPO>) jdbcTemplate.query(findSbmFileProcSumFileNameSql, rowMapper, fileName);
	}

	@Override
	public List<SbmFileProcessingSummaryPO> findSbmFileProcessingSummary(SBMFileStatus status) {

		return (List<SbmFileProcessingSummaryPO>) jdbcTemplate.query(findSbmFileProcSumFileStatusSql, rowMapper, status.getValue());
	}

	@Override
	public List<SbmFileProcessingSummaryPO> findSbmFileProcessingSummaryInProcess(String stateCd) {

		return (List<SbmFileProcessingSummaryPO>) jdbcTemplate.query(findSbmFileProcSumFileStatusStateCdSql, rowMapper, stateCd + "%");
	}

	@Override
	public boolean updateStatus(Long sbmFileProcSumId, SBMFileStatus fileStatus) {

		return jdbcTemplate.execute(updateSbmFileProcSumStatusSql, new PreparedStatementCallback<Boolean>() {

			@Override
			public Boolean doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {

				ps.setString(1, fileStatus.getValue());
				ps.setString(2, userVO.getUserId());
				ps.setLong(3, sbmFileProcSumId);

				int rowsEffected = ps.executeUpdate();

				return (rowsEffected == 1);
			}
		});	
	}

	@Override
	public boolean verifyJobRunning(final String jobName) {

		int countRows = jdbcTemplate.execute(verifyJobRunningSql, new PreparedStatementCallback<Integer>() {

			@Override
			public Integer doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {

				ps.setString(1, jobName);

				int rowsEffected = ps.executeUpdate();

				return Integer.valueOf(rowsEffected);
			}
		});	
		return (countRows > 0);
	}



	@Override
	public Long insertSbmFileProcessingSummary(SbmFileProcessingSummaryPO po) {

		final Long sbmFileProcSumId = sequenceHelper.nextSequenceId("SBMFILEPROCESSINGSUMMARYSEQ");

		jdbcTemplate.update(insertSbmFileProcSumSql, new PreparedStatementSetter() {
			public void setValues(PreparedStatement ps) throws SQLException {
				ps.setLong(1, sbmFileProcSumId);
				ps.setString(2, po.getTenantId());
				ps.setString(3, po.getIssuerFileSetId());
				ps.setString(4, po.getIssuerId());
				ps.setInt(5, getIntValue(po.getTotalIssuerFileCount()));
				ps.setString(6, po.getCmsApprovalRequiredInd());
				ps.setString(7, po.getCmsApprovedInd());
				ps.setInt(8, getIntValue(po.getTotalPreviousPoliciesNotSubmit()));
				ps.setInt(9, getIntValue(po.getNotSubmittedEffectuatedCnt()));
				ps.setInt(10, getIntValue(po.getNotSubmittedTerminatedCnt()));
				ps.setInt(11, getIntValue(po.getNotSubmittedCancelledCnt()));
				ps.setInt(12, getIntValue(po.getTotalRecordProcessedCnt()));
				ps.setInt(13, getIntValue(po.getTotalRecordRejectedCnt()));
				ps.setInt(14, getIntValue(po.getTotalPolicyApprovedCnt()));
				ps.setInt(15, getIntValue(po.getMatchingPlcNoChangeCnt()));
				ps.setInt(16, getIntValue(po.getMatchingPlcChgApplCnt()));
				ps.setInt(17, getIntValue(po.getMatchingPlcCorrectedChgApplCnt()));
				ps.setInt(18, getIntValue(po.getNewPlcCreatedAsSentCnt()));
				ps.setInt(19, getIntValue(po.getNewPlcCreatedCorrectionApplCnt()));
				ps.setInt(20, getIntValue(po.getEffectuatedPolicyCount()));
				ps.setBigDecimal(21, po.getErrorThresholdPercent());
				ps.setInt(22, getIntValue(po.getCoverageYear()));
				ps.setString(23, po.getSbmFileStatusTypeCd());
				ps.setString(24, userVO.getUserId());
				ps.setString(25, userVO.getUserId());
			}
		});	
		return sbmFileProcSumId;
	}


	static private class SbmFileProcSumRowMapper implements RowMapper<SbmFileProcessingSummaryPO> {

		@Override
		public SbmFileProcessingSummaryPO mapRow(ResultSet rs, int rowNum) throws SQLException {

			SbmFileProcessingSummaryPO row = new SbmFileProcessingSummaryPO();

			row.setSbmFileProcSumId(rs.getLong("SBMFILEPROCESSINGSUMMARYID"));
			row.setTotalIssuerFileCount(rs.getInt("TOTALISSUERFILECNT"));
			row.setTotalRecordProcessedCnt(rs.getInt("TOTALRECORDPROCESSEDCNT"));
			row.setTotalRecordRejectedCnt(rs.getInt("TOTALRECORDREJECTEDCNT"));
			row.setErrorThresholdPercent(rs.getBigDecimal("ERRORTHRESHOLDPERCENT"));
			row.setTotalPreviousPoliciesNotSubmit(rs.getInt("TOTALPREVIOUSPOLICIESNOTSUBMIT"));
			row.setCmsApprovedInd(rs.getString("CMSAPPROVEDIND"));
			row.setNotSubmittedEffectuatedCnt(rs.getInt("NOTSUBMITTEDEFFECTUATEDCNT"));
			row.setNotSubmittedTerminatedCnt(rs.getInt("NOTSUBMITTEDTERMINATEDCNT"));
			row.setNotSubmittedCancelledCnt(rs.getInt("NOTSUBMITTEDCANCELLEDCNT"));
			row.setIssuerFileSetId(rs.getString("ISSUERFILESETID"));
			row.setIssuerId(rs.getString("ISSUERID"));
			row.setTenantId(rs.getString("TENANTID"));
			row.setCmsApprovalRequiredInd(rs.getString("CMSAPPROVALREQUIREDIND"));
			row.setTotalPolicyApprovedCnt(rs.getInt("TOTALPOLICYAPPROVEDCNT"));
			row.setMatchingPlcNoChangeCnt(rs.getInt("MATCHINGPLCNOCHGCNT"));
			row.setMatchingPlcChgApplCnt(rs.getInt("MATCHINGPLCCHGAPPLCNT"));
			row.setMatchingPlcCorrectedChgApplCnt(rs.getInt("MATCHINGPLCCORRECTEDCHGAPPLCNT"));
			row.setNewPlcCreatedAsSentCnt(rs.getInt("NEWPLCCREATEDASSENTCNT"));
			row.setNewPlcCreatedCorrectionApplCnt(rs.getInt("NEWPLCCREATEDCORRECTIONAPPLCNT"));
			row.setEffectuatedPolicyCount(rs.getInt("EFFECTUATEDPOLICYCNT"));
			row.setCoverageYear(rs.getInt("COVERAGEYEAR"));
			row.setSbmFileStatusTypeCd(rs.getString("SBMFILESTATUSTYPECD"));

			return row;
		}
	}

	@Override
	public void updateSbmFileProcessingSummary(final SbmFileProcessingSummaryPO po) {

		jdbcTemplate.update(updateSbmFileProcSumSql, new PreparedStatementSetter() {
			public void setValues(PreparedStatement ps) throws SQLException {

				ps.setInt(1, getIntValue(po.getTotalIssuerFileCount()));
				ps.setInt(2, getIntValue(po.getTotalPreviousPoliciesNotSubmit()));
				ps.setInt(3, getIntValue(po.getNotSubmittedEffectuatedCnt()));
				ps.setInt(4, getIntValue(po.getNotSubmittedTerminatedCnt()));
				ps.setInt(5, getIntValue(po.getNotSubmittedCancelledCnt()));
				ps.setInt(6, getIntValue(po.getTotalRecordProcessedCnt()));
				ps.setInt(7, getIntValue(po.getTotalRecordRejectedCnt()));
				ps.setInt(8, getIntValue(po.getTotalPolicyApprovedCnt()));
				ps.setInt(9, getIntValue(po.getMatchingPlcNoChangeCnt()));
				ps.setInt(10, getIntValue(po.getMatchingPlcChgApplCnt()));
				ps.setInt(11, getIntValue(po.getMatchingPlcCorrectedChgApplCnt()));
				ps.setInt(12, getIntValue(po.getNewPlcCreatedAsSentCnt()));
				ps.setInt(13, getIntValue(po.getNewPlcCreatedCorrectionApplCnt()));
				ps.setInt(14, getIntValue(po.getEffectuatedPolicyCount()));				
				ps.setString(15, userVO.getUserId());
				ps.setLong(16, po.getSbmFileProcSumId());
			}
		});	
	}


	@Override
	public boolean updateCmsApproved(final Long sbmFileProcSumId, final String indicator) {

		return jdbcTemplate.execute(updateSbmFileProcSumCmsApprovedSql, new PreparedStatementCallback<Boolean>() {

			@Override
			public Boolean doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {

				ps.setString(1, indicator);
				ps.setLong(2, sbmFileProcSumId);

				int rowsEffected = ps.executeUpdate();

				return (rowsEffected == 1);
			}
		});	

	}

	@Override
	public boolean verifyCmsApprovalRequired(Long sbmFileProcSumId) {

		boolean isCmsApprovalReq = true;
		String ind = jdbcTemplate.queryForObject(verifyCmsApprovalReqSql, String.class, sbmFileProcSumId);
		
		if (ind != null) {
			if (ind.equals("N")) {
				isCmsApprovalReq = false;
			}
		}
		return isCmsApprovalReq;
	}


	/**
	 * @param matchSbmFileProcSumSql the matchSbmFileProcSumSql to set
	 */
	public void setMatchSbmFileProcSumSql(String matchSbmFileProcSumSql) {
		this.matchSbmFileProcSumSql = matchSbmFileProcSumSql;
	}


	/**
	 * @param selectSbmFileProcSumSql the selectSbmFileProcSumSql to set
	 */
	public void setSelectSbmFileProcSumSql(String selectSbmFileProcSumSql) {
		this.selectSbmFileProcSumSql = selectSbmFileProcSumSql;
	}


	/**
	 * @param findSbmFileProcSumSql the findSbmFileProcSumSql to set
	 */
	public void setFindSbmFileProcSumSql(String findSbmFileProcSumSql) {
		this.findSbmFileProcSumSql = findSbmFileProcSumSql;
	}


	/**
	 * @param findSbmFileProcSumFileNameSql the findSbmFileProcSumFileNameSql to set
	 */
	public void setFindSbmFileProcSumFileNameSql(String findSbmFileProcSumFileNameSql) {
		this.findSbmFileProcSumFileNameSql = findSbmFileProcSumFileNameSql;
	}


	/**
	 * @param findSbmFileProcSumFileStatusSql the findSbmFileProcSumFileStatusSql to set
	 */
	public void setFindSbmFileProcSumFileStatusSql(String findSbmFileProcSumFileStatusSql) {
		this.findSbmFileProcSumFileStatusSql = findSbmFileProcSumFileStatusSql;
	}


	/**
	 * @param insertSbmFileProcSumSql the insertSbmFileProcSumSql to set
	 */
	public void setInsertSbmFileProcSumSql(String insertSbmFileProcSumSql) {
		this.insertSbmFileProcSumSql = insertSbmFileProcSumSql;
	}


	/**
	 * @param updateSbmFileProcSumStatusSql the updateSbmFileProcSumStatusSql to set
	 */
	public void setUpdateSbmFileProcSumStatusSql(String updateSbmFileProcSumStatusSql) {
		this.updateSbmFileProcSumStatusSql = updateSbmFileProcSumStatusSql;
	}


	/**
	 * @param findSbmFileProcSumFileStatusStateCdSql the findSbmFileProcSumFileStatusStateCdSql to set
	 */
	public void setFindSbmFileProcSumFileStatusStateCdSql(String findSbmFileProcSumFileStatusStateCdSql) {
		this.findSbmFileProcSumFileStatusStateCdSql = findSbmFileProcSumFileStatusStateCdSql;
	}


	/**
	 * @param selectSbmFileProcSumLatestApprovedStateCdSql the selectSbmFileProcSumLatestApprovedStateCdSql to set
	 */
	public void setSelectSbmFileProcSumLatestApprovedStateCdSql(String selectSbmFileProcSumLatestApprovedStateCdSql) {
		this.selectSbmFileProcSumLatestApprovedStateCdSql = selectSbmFileProcSumLatestApprovedStateCdSql;
	}


	/**
	 * @param selectSbmFileProcSumLatestApprovedIssuerIdSql the selectSbmFileProcSumLatestApprovedIssuerIdSql to set
	 */
	public void setSelectSbmFileProcSumLatestApprovedIssuerIdSql(String selectSbmFileProcSumLatestApprovedIssuerIdSql) {
		this.selectSbmFileProcSumLatestApprovedIssuerIdSql = selectSbmFileProcSumLatestApprovedIssuerIdSql;
	}	


	/**
	 * @param verifyJobRunningSql the verifyJobRunningSql to set
	 */
	public void setVerifyJobRunningSql(String verifyJobRunningSql) {
		this.verifyJobRunningSql = verifyJobRunningSql;
	}


	/**
	 * @param updateSbmFileProcSumSql the updateSbmFileProcSumSql to set
	 */
	public void setUpdateSbmFileProcSumSql(String updateSbmFileProcSumSql) {
		this.updateSbmFileProcSumSql = updateSbmFileProcSumSql;
	}


	/**
	 * @param updateSbmFileProcSumCmsApprovedSql the updateSbmFileProcSumCmsApprovedSql to set
	 */
	public void setUpdateSbmFileProcSumCmsApprovedSql(String updateSbmFileProcSumCmsApprovedSql) {
		this.updateSbmFileProcSumCmsApprovedSql = updateSbmFileProcSumCmsApprovedSql;
	}


	/**
	 * @param verifyCmsApprovalReqSql the verifyCmsApprovalReqSql to set
	 */
	public void setVerifyCmsApprovalReqSql(String verifyCmsApprovalReqSql) {
		this.verifyCmsApprovalReqSql = verifyCmsApprovalReqSql;
	}

}
