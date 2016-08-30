package gov.hhs.cms.ff.fm.eps.ep.dao.mappers;

import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMPolicyDTO;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;

/**
 * @author j.radziewski
 *
 */
public class SbmPolicyDTORowMapper implements RowMapper<SBMPolicyDTO> {
	
	@Override
	public SBMPolicyDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		SBMPolicyDTO dto = new SBMPolicyDTO();
		
		Clob clobXprXml = rs.getClob("XPRXML");
		dto.setPolicyXml(clobXprXml.getSubString(1, (int) clobXprXml.length()));
		
		dto.setProcessingGroupid(rs.getLong("PROCESSINGGROUPID"));
		dto.setStagingSbmPolicyid(rs.getLong("STAGINGSBMPOLICYID"));
		dto.setSbmFileInfoId(rs.getLong("SBMFILEINFOID"));
		dto.setSbmFileProcSummaryId(rs.getLong("SBMFILEPROCESSINGSUMMARYID"));
		dto.setFileProcessDateTime(DateTimeUtil.getLocalDateTimeFromSqlTimestamp(rs.getTimestamp("CREATEDATETIME")));
		
		return dto;
	}
	

}
