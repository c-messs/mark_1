package gov.hhs.cms.ff.fm.eps.ep.dao.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import gov.hhs.cms.ff.fm.eps.ep.po.SbmFileInfoPO;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;

/**
 * @author j.radziewski
 *
 */
public class SBMFileInfoRowMapper implements RowMapper<SbmFileInfoPO> {

	@Override
	public SbmFileInfoPO mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		SbmFileInfoPO po = new SbmFileInfoPO();
		
		po.setSbmFileInfoId(rs.getLong("SBMFILEINFOID"));
	    po.setSbmFileNm(rs.getString("SBMFILENM"));
		po.setSbmFileId(rs.getString("SBMFILEID"));
		po.setSbmFileCreateDateTime(DateTimeUtil.getLocalDateTimeFromSqlTimestamp(rs.getTimestamp("SBMFILECREATEDATETIME")));
		if(rs.getInt("SBMFILENUM") != 0) {
			po.setSbmFileNum(rs.getInt("SBMFILENUM"));
		}
		po.setSbmFileProcessingSummaryId(rs.getLong("SBMFILEPROCESSINGSUMMARYID"));
		po.setTradingPartnerId(rs.getString("TRADINGPARTNERID"));
		po.setFunctionCd(rs.getString("FUNCTIONCD"));
		po.setRejectedInd(getBooleanVal(rs.getString("REJECTEDIND")));
		po.setCreateDateTime(DateTimeUtil.getLocalDateTimeFromSqlTimestamp(rs.getTimestamp("CREATEDATETIME")));
		
		return po;
	}
	
	/**
	 * Convert string value to boolean for values 'Y', 'N'.
	 *
	 * @param str String
	 * @return the boolean val
	 */
	private boolean getBooleanVal(String str) {

		if (str != null && str.equalsIgnoreCase("Y")) {
			return true;
		}

		return false;
	}

}
