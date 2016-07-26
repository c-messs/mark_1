package gov.hhs.cms.ff.fm.eps.ep.dao.mappers;

import gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentMaintenanceDTO;
import gov.hhs.cms.ff.fm.eps.rap.util.DataCommonUtil;

import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

/**
 * @author j.radziewski
 *
 */
public class BemDTORowMapper implements RowMapper<BenefitEnrollmentMaintenanceDTO> {
	
	private static final String TRANS_MSG_ID = "TRANSMSGID"; 

	@Override
	public BenefitEnrollmentMaintenanceDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		BenefitEnrollmentMaintenanceDTO dto = new BenefitEnrollmentMaintenanceDTO();
		
		if (rs.getLong(TRANS_MSG_ID) != 0) {
			dto.setTransMsgId(rs.getLong(TRANS_MSG_ID));
		}
		dto.setFileNm(rs.getString("FILENM"));
		dto.setFileNmDateTime(DataCommonUtil.convertToDateTime(rs.getTimestamp("FILENMDATETIME")));
		
		Clob clobFileInfo = rs.getClob("FILEINFOXML");
		dto.setFileInfoXml(clobFileInfo.getSubString(1, (int) clobFileInfo.length()));
		
		dto.setFileNmDateTime(DataCommonUtil.convertToDateTime(rs.getTimestamp("INDEXDATETIME")));
		
		Clob clobBemXml = rs.getClob("BEMXML");
		dto.setBemXml(clobBemXml.getSubString(1, (int) clobBemXml.length()));
		
		dto.setExchangeTypeCd(rs.getString("EXCHANGETYPECD"));
		dto.setSubscriberStateCd(rs.getString("SUBSCRIBERSTATECD"));
		dto.setPlanId(rs.getString("PLANID"));
		
		return dto;
	}
	

}
