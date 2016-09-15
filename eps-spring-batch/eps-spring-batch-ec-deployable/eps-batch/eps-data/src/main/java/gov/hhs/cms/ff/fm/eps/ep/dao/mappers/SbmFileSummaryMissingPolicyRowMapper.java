package gov.hhs.cms.ff.fm.eps.ep.dao.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import gov.hhs.cms.ff.fm.eps.ep.po.SbmFileSummaryMissingPolicyPO;

/**
 * @author j.radziewski
 *
 */
public class SbmFileSummaryMissingPolicyRowMapper implements RowMapper<SbmFileSummaryMissingPolicyPO>{

	@Override
	public SbmFileSummaryMissingPolicyPO mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		SbmFileSummaryMissingPolicyPO po = new SbmFileSummaryMissingPolicyPO();
		
		po.setMissingPolicyVersionId(rs.getLong("MISSINGPOLICYVERSIONID"));
		
		return po;
	}

}
