package gov.hhs.cms.ff.fm.eps.ep.dao.mappers;

import gov.hhs.cms.ff.fm.eps.ep.po.PolicyMemberDatePO;
import gov.hhs.cms.ff.fm.eps.rap.util.DataCommonUtil;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

/**
 * The Mapper class for PolicyMemberDate table to translate inbound BEM Member Dates data to EPS.
 *
 * @author EPS
 */
public class PolicyMemberDateRowMapper  implements RowMapper<PolicyMemberDatePO> {

	@Override
	public PolicyMemberDatePO mapRow(ResultSet rs, int rowNum) throws SQLException {

		PolicyMemberDatePO po = new PolicyMemberDatePO();

		po.setPolicyMemberVersionId(rs.getLong("POLICYMEMBERVERSIONID"));
		po.setPolicyMemberStartDate(DataCommonUtil.convertToDateTime(rs.getDate("POLICYMEMBERSTARTDATE")));
		po.setPolicyMemberEndDate(DataCommonUtil.convertToDateTime(rs.getDate("POLICYMEMBERENDDATE")));

		return po;		
	}

}
