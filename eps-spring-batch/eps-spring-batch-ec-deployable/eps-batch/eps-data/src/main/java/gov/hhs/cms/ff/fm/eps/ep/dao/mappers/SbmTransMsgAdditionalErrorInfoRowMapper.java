package gov.hhs.cms.ff.fm.eps.ep.dao.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import gov.hhs.cms.ff.fm.eps.ep.po.SbmTransMsgAdditionalErrorInfoPO;

public class SbmTransMsgAdditionalErrorInfoRowMapper implements RowMapper<SbmTransMsgAdditionalErrorInfoPO> {

	@Override
	public SbmTransMsgAdditionalErrorInfoPO mapRow(ResultSet rs, int rowNum) throws SQLException {

		SbmTransMsgAdditionalErrorInfoPO po = new SbmTransMsgAdditionalErrorInfoPO();
		
		po.setAdditionalErrorInfoText(rs.getString("ADDITIONALERRORINFOTEXT"));
		
		return po;
	}

}
