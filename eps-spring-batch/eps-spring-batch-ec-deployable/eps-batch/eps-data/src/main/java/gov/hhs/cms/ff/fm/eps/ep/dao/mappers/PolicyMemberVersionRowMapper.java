package gov.hhs.cms.ff.fm.eps.ep.dao.mappers;

import gov.hhs.cms.ff.fm.eps.ep.po.PolicyMemberVersionPO;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

/**
 * @author j.radziewski
 *
 */
public class PolicyMemberVersionRowMapper implements RowMapper<PolicyMemberVersionPO> {

	@Override
	public PolicyMemberVersionPO mapRow(ResultSet rs, int rowNum) throws SQLException {

		PolicyMemberVersionPO po = new PolicyMemberVersionPO();

		po.setPolicyMemberVersionId(rs.getLong("POLICYMEMBERVERSIONID"));
		po.setExchangePolicyId(rs.getString("EXCHANGEPOLICYID"));
		po.setSubscriberStateCd(rs.getString("SUBSCRIBERSTATECD"));
		po.setExchangeMemberID(rs.getString("EXCHANGEMEMBERID"));
		po.setMaintenanceStartDateTime(rs.getTimestamp("MAINTENANCESTARTDATETIME").toLocalDateTime());
		po.setMaintenanceEndDateTime(rs.getTimestamp("MAINTENANCEENDDATETIME").toLocalDateTime());
		po.setIssuerAssignedMemberID(rs.getString("ISSUERASSIGNEDMEMBERID"));
		po.setPolicyMemberEligStartDate(DateTimeUtil.getLocalDateFromSqlDate(rs.getDate("POLICYMEMBERELIGSTARTDATE")));
		po.setPolicyMemberEligEndDate(DateTimeUtil.getLocalDateFromSqlDate(rs.getDate("POLICYMEMBERELIGENDDATE")));
		po.setSubscriberInd(rs.getString("SUBSCRIBERIND"));
		po.setPolicyMemberBirthDate(DateTimeUtil.getLocalDateFromSqlDate(rs.getDate("POLICYMEMBERBIRTHDATE")));
		po.setPolicyMemberLastNm(rs.getString("POLICYMEMBERLASTNM"));
		po.setPolicyMemberFirstNm(rs.getString("POLICYMEMBERFIRSTNM"));
		po.setPolicyMemberMiddleNm(rs.getString("POLICYMEMBERMIDDLENM"));
		po.setPolicyMemberSalutationNm(rs.getString("POLICYMEMBERSALUTATIONNM"));
		po.setPolicyMemberSuffixNm(rs.getString("POLICYMEMBERSUFFIXNM"));
		po.setPolicyMemberSSN(rs.getString("POLICYMEMBERSSN"));
		po.setTransMsgID(rs.getLong("TRANSMSGID"));
		po.setX12GenderTypeCd(rs.getString("X12GENDERTYPECD"));

		return po;		
	}
}
