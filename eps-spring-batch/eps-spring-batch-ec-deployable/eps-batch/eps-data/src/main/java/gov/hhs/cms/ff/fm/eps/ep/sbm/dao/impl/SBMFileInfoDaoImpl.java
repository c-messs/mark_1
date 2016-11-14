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
	
	/**
	 * SELECT_FILE_INFO
	 */
	private String selectSbmFileInfoSql;
	/**
	 * SELECT_FILE_INFO_BY_FILESETID_FILENUM
	 */
	private String selectSBMFileInfoSqlByFileSetId;
	/**
	 * SELECT_FILE_INFO_XML
	 */
	private String selectFileInfoXmlSql;
	/**
	 * INSERT_FILE_INFO, use when FILEINFOXML is not null.
	 */
	private String insertSBMFileInfoSql;
	/**
	 * INSERT_FILE_INFO_NULL, does not contain FILEINFOXML column and XMLTYPE(?) argument.
	 */
	private String insertSBMFileInfoNullSql;

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
		// If FILEINFOXML is null, do not attempt XMLTYPE(null).  Db default value is NULL.
		final String sql = po.getFileInfoXML() != null ? insertSBMFileInfoSql : insertSBMFileInfoNullSql;

		jdbcTemplate.update(sql, new PreparedStatementSetter() {
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
				ps.setString(10, userId);
				ps.setString(11, userId);
				ps.setTimestamp(12, convertToSqlTimestamp(po.getSbmFileLastModifiedDateTime()));

				// Only set if NOT null
				if (po.getFileInfoXML() != null) {
					Clob clob = ps.getConnection().createClob();
					clob.setString(1, po.getFileInfoXML());
					ps.setClob(13, clob);
				}
			}
		});	

		return sbmFileInfoId;
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


	/**
	 * @param insertSBMFileInfoNullSql
	 */
	public void setInsertSBMFileInfoNullSql(String insertSBMFileInfoNullSql) {
		this.insertSBMFileInfoNullSql = insertSBMFileInfoNullSql;
	}

}
