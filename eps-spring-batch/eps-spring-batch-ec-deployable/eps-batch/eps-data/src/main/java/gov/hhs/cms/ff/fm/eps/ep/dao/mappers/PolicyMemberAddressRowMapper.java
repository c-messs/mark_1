package gov.hhs.cms.ff.fm.eps.ep.dao.mappers;

import gov.hhs.cms.ff.fm.eps.ep.po.PolicyMemberAddressPO;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

/**
 * @author j.radziewski
 *
 */
public class PolicyMemberAddressRowMapper implements RowMapper<PolicyMemberAddressPO> {

	@Override
	public PolicyMemberAddressPO mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		PolicyMemberAddressPO po = new PolicyMemberAddressPO();
		
		po.setPolicyMemberVersionId(rs.getLong("POLICYMEMBERVERSIONID"));
		po.setX12addressTypeCd(rs.getString("X12ADDRESSTYPECD"));
		po.setStateCd(rs.getString("STATECD"));
		po.setZipPlus4Cd(rs.getString("ZIPPLUS4CD"));
			
		return po;
	}
}
