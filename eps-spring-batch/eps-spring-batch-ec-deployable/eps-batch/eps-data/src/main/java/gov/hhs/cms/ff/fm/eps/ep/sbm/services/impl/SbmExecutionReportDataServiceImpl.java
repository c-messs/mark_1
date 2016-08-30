package gov.hhs.cms.ff.fm.eps.ep.sbm.services.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMExecutionReportDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SbmDataUtil;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SbmExecutionReportDataService;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;

public class SbmExecutionReportDataServiceImpl implements SbmExecutionReportDataService  {
	
	private final static Logger LOG = LoggerFactory.getLogger(SbmExecutionReportDataServiceImpl.class);
	
	private JdbcTemplate jdbcTemplate;
	private String selectSbmExecutionLog;
	
	@Override
	public List<SBMExecutionReportDTO> getSbmExecutionLog() {
		
		LOG.info("retrieving Sbm Execution Log");

		List<SBMExecutionReportDTO> sbmExecutionLog = 
				(List<SBMExecutionReportDTO>) jdbcTemplate.query(selectSbmExecutionLog, new SBMExecutionReportRowMapper());
		
		return sbmExecutionLog;
	}

	static private class SBMExecutionReportRowMapper implements RowMapper<SBMExecutionReportDTO> {

		@Override
		public SBMExecutionReportDTO mapRow(ResultSet rs, int rowNum) throws SQLException {

			SBMExecutionReportDTO row = new SBMExecutionReportDTO();

			row.setCoverageYear(rs.getInt("COVERAGEYEAR"));
			row.setStateCd(SbmDataUtil.getStateCd(rs.getString("TENANTID")));
			row.setFileName((rs.getString("SBMFILENM")));
			row.setFileId(rs.getString("SBMFILEID"));
			row.setFileLoggedTimestamp(
					DateTimeUtil.getLocalDateTimeFromSqlTimestamp(rs.getTimestamp("CREATEDATETIME")));
			row.setIssuerId(rs.getString("ISSUERID"));
			row.setIssuerFileSetId(rs.getString("ISSUERFILESETID"));
			row.setSbmFileNum(rs.getInt("SBMFILENUM"));
			row.setTotalFilesInFileset(rs.getInt("TOTALISSUERFILECNT"));
			row.setFileStatus(SBMFileStatus.getEnum(rs.getString("SBMFILESTATUSTYPECD")).getName());
			String rejectedInd = rs.getString("REJECTEDIND");
			if(rejectedInd.equalsIgnoreCase("Y")) {
				row.setFileStatus(SBMFileStatus.REJECTED.getName());
			}
			row.setFileStatusTimestamp(
					DateTimeUtil.getLocalDateTimeFromSqlTimestamp(rs.getTimestamp("LASTMODIFIEDDATETIME")));

			return row;
		}
	}

	/**
	 * @param jdbcTemplate the jdbcTemplate to set
	 */
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * @param selectSbmExecutionLog the selectSbmExecutionLog to set
	 */
	public void setSelectSbmExecutionLog(String selectSbmExecutionLog) {
		this.selectSbmExecutionLog = selectSbmExecutionLog;
	}
}
