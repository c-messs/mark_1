package gov.hhs.cms.ff.fm.eps.ep.sbm.dao.impl;

import static gov.hhs.cms.ff.fm.eps.rap.util.DataCommonUtil.convertToSqlTimestamp;

import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;

import gov.hhs.cms.ff.fm.eps.ep.dao.GenericEpsDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.SbmFileInfoDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.mappers.SBMFileInfoRowMapper;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmFileInfoPO;

/**
 * 
 * Implementation of SBMFileInfoDao 
 *
 */
public class SBMFileInfoDaoImpl extends GenericEpsDao<SbmFileInfoPO> implements SbmFileInfoDao {

	private String selectSbmFileInfoSql;
	private String selectSBMFileInfoSqlByFileSetId;
	private String selectFileStatusSql;
	private String selectFileInfoXmlSql;
	private String insertSBMFileInfoSql;

	/**
	 * Constructor
	 */
	public SBMFileInfoDaoImpl() {
		this.setRowMapper(new SBMFileInfoRowMapper());
	}


	@Override
	public Long insertSBMFileInfo(final SbmFileInfoPO po) {

		final String userId = super.userVO.getUserId();
		final Long sbmFileInfoId = sequenceHelper.nextSequenceId("SBMFILEINFOSEQ");

		jdbcTemplate.update(insertSBMFileInfoSql, new PreparedStatementSetter() {
			public void setValues(PreparedStatement ps) throws SQLException {
				ps.setLong(1, sbmFileInfoId);
				ps.setLong(2, po.getSbmFileProcessingSummaryId());
				ps.setString(3, po.getSbmFileNm());
				ps.setTimestamp(4, convertToSqlTimestamp(po.getSbmFileCreateDateTime()));
				ps.setString(5, po.getSbmFileId());
				if (po.getSbmFileNum() != null) {
					ps.setInt(6, po.getSbmFileNum()); 
				} else {
					ps.setObject(6, null);
				}
				ps.setString(7, po.getTradingPartnerId());
				ps.setString(8, po.getFunctionCd());

				if (po.getRejectedInd()) {
					ps.setString(9, "Y");
				} else {
					ps.setString(9, "N");
				}

				if (po.getFileInfoXML() != null) {
					Clob clob = ps.getConnection().createClob();
					clob.setString(1, po.getFileInfoXML());
					ps.setClob(10, clob);
				} else {
					ps.setObject(10, null);
				}

				ps.setString(11, userId);
				ps.setString(12, userId);
				ps.setTimestamp(13, convertToSqlTimestamp(po.getSbmFileLastModifiedDateTime()));
			}
		});	

		return sbmFileInfoId;
	}

	@Override
	public List<String> getFileStatusList(String fileName) {

		Object[] args = new Object[] {fileName};
		List<String> statusList = (List<String>) jdbcTemplate.queryForList(selectFileStatusSql, String.class, args);
		return statusList;
	}


	@Override
	public List<SbmFileInfoPO> performFileMatch(String fileSetId, int fileNumber) {

		Object[] args = new Object[] {fileSetId, fileNumber};

		return (List<SbmFileInfoPO>) jdbcTemplate.query(selectSBMFileInfoSqlByFileSetId, rowMapper, args);
	}


	@Override
	public String selectFileInfoXml(Long sbmFileInfoId) {

		String xml = (String) jdbcTemplate.queryForObject(selectFileInfoXmlSql, new FileInfoTypeXmlRowMapper(), sbmFileInfoId);
		return xml;
	}

	static private class FileInfoTypeXmlRowMapper implements RowMapper<String> {

		@Override
		public String mapRow(ResultSet rs, int rowNum) throws SQLException {

			String xml = null;
			Clob clob = rs.getClob(1);
			if (clob != null) {
				xml = clob.getSubString(1, (int) clob.length()).trim();
			}
			return xml;
		}
	}
	

	@Override
	public List<SbmFileInfoPO> getSbmFileInfoList(Long sbmFileProcSumId) {
		
		return (List<SbmFileInfoPO>) jdbcTemplate.query(selectSbmFileInfoSql, rowMapper, sbmFileProcSumId);
	}
	

	/**
	 * @param selectSbmFileInfoSql the selectSbmFileInfoSql to set
	 */
	public void setSelectSbmFileInfoSql(String selectSbmFileInfoSql) {
		this.selectSbmFileInfoSql = selectSbmFileInfoSql;
	}


	/**
	 * @param selectSBMFileInfoSqlByFileSetId the selectSBMFileInfoSqlByFileSetId to set
	 */
	public void setSelectSBMFileInfoSqlByFileSetId(String selectSBMFileInfoSqlByFileSetId) {
		this.selectSBMFileInfoSqlByFileSetId = selectSBMFileInfoSqlByFileSetId;
	}


	/**
	 * @param selectFileStatusSql the selectFileStatusSql to set
	 */
	public void setSelectFileStatusSql(String selectFileStatusSql) {
		this.selectFileStatusSql = selectFileStatusSql;
	}


	/**
	 * @param selectFileInfoXmlSql the selectFileInfoXmlSql to set
	 */
	public void setSelectFileInfoXmlSql(String selectFileInfoXmlSql) {
		this.selectFileInfoXmlSql = selectFileInfoXmlSql;
	}


	/**
	 * @param insertSBMFileInfoSql the insertSBMFileInfoSql to set
	 */
	public void setInsertSBMFileInfoSql(String insertSBMFileInfoSql) {
		this.insertSBMFileInfoSql = insertSBMFileInfoSql;
	}

}
