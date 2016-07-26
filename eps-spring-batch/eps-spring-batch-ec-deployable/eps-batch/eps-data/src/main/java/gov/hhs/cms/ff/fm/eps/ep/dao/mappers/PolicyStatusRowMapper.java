package gov.hhs.cms.ff.fm.eps.ep.dao.mappers;

import gov.hhs.cms.ff.fm.eps.ep.po.PolicyStatusPO;
import gov.hhs.cms.ff.fm.eps.rap.util.DataCommonUtil;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

/**
 * @author j.radziewski
 *
 */
public class PolicyStatusRowMapper implements RowMapper<PolicyStatusPO> {

	@Override
	public PolicyStatusPO mapRow(ResultSet rs, int rowNum) throws SQLException {

		PolicyStatusPO po = new PolicyStatusPO();
		
		po.setTransDateTime(DataCommonUtil.convertToDateTime(rs.getTimestamp("TRANSDATETIME")));
		po.setInsuranacePolicyStatusTypeCd(rs.getString("INSURANACEPOLICYSTATUSTYPECD"));
		
		return po;
	}
}
