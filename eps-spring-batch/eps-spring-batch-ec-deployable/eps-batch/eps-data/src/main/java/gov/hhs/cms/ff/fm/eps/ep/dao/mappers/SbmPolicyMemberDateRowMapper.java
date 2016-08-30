package gov.hhs.cms.ff.fm.eps.ep.dao.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import gov.hhs.cms.ff.fm.eps.ep.po.SbmPolicyMemberDatePO;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;

public class SbmPolicyMemberDateRowMapper implements RowMapper<SbmPolicyMemberDatePO> {

	@Override
	public SbmPolicyMemberDatePO mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		SbmPolicyMemberDatePO po = new SbmPolicyMemberDatePO();

		po.setPolicyMemberVersionId(rs.getLong("POLICYMEMBERVERSIONID"));
		po.setPolicyMemberStartDate(DateTimeUtil.getLocalDateFromSqlDate(rs.getDate("POLICYMEMBERSTARTDATE")));
		po.setPolicyMemberEndDate(DateTimeUtil.getLocalDateFromSqlDate(rs.getDate("POLICYMEMBERENDDATE")));

		return po;		
	}
	
	

}
