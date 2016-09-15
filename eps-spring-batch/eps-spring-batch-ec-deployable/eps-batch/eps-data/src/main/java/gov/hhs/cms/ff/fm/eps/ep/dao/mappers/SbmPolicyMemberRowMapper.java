package gov.hhs.cms.ff.fm.eps.ep.dao.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import gov.hhs.cms.ff.fm.eps.ep.po.SbmPolicyMemberPO;

/**
 * @author j.radziewski
 *
 */
public class SbmPolicyMemberRowMapper implements RowMapper<SbmPolicyMemberPO> {

	@Override
	public SbmPolicyMemberPO mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		SbmPolicyMemberPO po = new SbmPolicyMemberPO();
		
		po.setPolicyVersionId(rs.getLong("MISSINGPOLICYVERSIONID"));
		po.setPolicyMemberVersionId(rs.getLong("MISSINGPOLICYMEMBERVERSIONID"));
		
		return po;
	}

}
