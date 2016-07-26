package gov.hhs.cms.ff.fm.eps.ep.dao.mappers;

import gov.hhs.cms.ff.fm.eps.ep.po.ErrorWarningLogPO;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

/**
 * @author j.radziewski
 *
 */
public class ErrorWarningLogRowMapper implements RowMapper<ErrorWarningLogPO> {
	
	// To not get Sonar Minors for "String literals should not be duplicated"
	private static final String TRANS_MSG_ID = "TRANSMSGID"; 
	private static final String TRANS_MSG_FILE_INFO_ID = "TRANSMSGFILEINFOID";
	private static final String BATCH_ID = "BATCHID";

	@Override
	public ErrorWarningLogPO mapRow(ResultSet rs, int rowNum) throws SQLException {

		ErrorWarningLogPO po = new ErrorWarningLogPO();

		po.setErrorWarningDetailedDesc(rs.getString("ERRORWARNINGDETAILEDDESC"));
		po.setBizAppAckErrorCd(rs.getString("BIZAPPACKERRORCD"));
		po.setErrorElement(rs.getString("ERRORELEMENT"));
		
		if (rs.getLong(TRANS_MSG_ID) != 0) {
			po.setTransMsgID(rs.getLong(TRANS_MSG_ID));
		}
		po.setProcessingErrorCd(rs.getString("PROCESSINGERRORCD"));

		if (rs.getLong(TRANS_MSG_FILE_INFO_ID) != 0) {
			po.setTransMsgFileInfoId(rs.getLong(TRANS_MSG_FILE_INFO_ID));
		}
		if (rs.getLong(BATCH_ID) != 0) {
			po.setBatchId(rs.getLong(BATCH_ID));
		}

		return po;
	}
}
