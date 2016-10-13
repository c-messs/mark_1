package gov.hhs.cms.ff.fm.eps.ep.dao.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import gov.hhs.cms.ff.fm.eps.ep.po.SbmPolicyMemberVersionPO;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;

/**
 * @author j.radziewski
 *
 */
public class SbmPolicyMemberVersionRowMapper implements RowMapper<SbmPolicyMemberVersionPO> {

	@Override
	public SbmPolicyMemberVersionPO mapRow(ResultSet rs, int rowNum) throws SQLException {

		SbmPolicyMemberVersionPO po = new SbmPolicyMemberVersionPO();

		po.setPolicyMemberVersionId(rs.getLong("POLICYMEMBERVERSIONID"));
		po.setSubscriberInd(rs.getString("SUBSCRIBERIND"));
		po.setIssuerAssignedMemberID(rs.getString("ISSUERASSIGNEDMEMBERID"));
		po.setExchangeMemberID(rs.getString("EXCHANGEMEMBERID"));
		po.setMaintenanceStartDateTime(rs.getTimestamp("MAINTENANCESTARTDATETIME").toLocalDateTime());
		po.setMaintenanceEndDateTime(rs.getTimestamp("MAINTENANCEENDDATETIME").toLocalDateTime());
		po.setPolicyMemberDeathDate(DateTimeUtil.getLocalDateFromSqlDate(rs.getDate("POLICYMEMBERDEATHDATE")));
		po.setPolicyMemberLastNm(rs.getString("POLICYMEMBERLASTNM"));
		po.setPolicyMemberFirstNm(rs.getString("POLICYMEMBERFIRSTNM"));
		po.setPolicyMemberMiddleNm(rs.getString("POLICYMEMBERMIDDLENM"));
		po.setPolicyMemberSalutationNm(rs.getString("POLICYMEMBERSALUTATIONNM"));
		po.setPolicyMemberSuffixNm(rs.getString("POLICYMEMBERSUFFIXNM"));
		po.setPolicyMemberSSN(rs.getString("POLICYMEMBERSSN"));
		po.setExchangePolicyId(rs.getString("EXCHANGEPOLICYID"));
		po.setSubscriberStateCd(rs.getString("SUBSCRIBERSTATECD"));
		po.setX12TobaccoUseTypeCode(rs.getString("X12TOBACCOUSETYPECD"));
		po.setPolicyMemberBirthDate(DateTimeUtil.getLocalDateFromSqlDate(rs.getDate("POLICYMEMBERBIRTHDATE")));
		po.setX12GenderTypeCd(rs.getString("X12GENDERTYPECD"));
		po.setIncorrectGenderTypeCode(rs.getString("INCORRECTGENDERTYPECD"));
		// TODO need to determine EPS to SBM mapping and vice versa for NonCoveredSubscriberInd.
		// Hard coded to 'N'.
		po.setNonCoveredSubscriberInd(rs.getString("NONCOVEREDSUBSCRIBERIND"));
		po.setX12LanguageCode(rs.getString("X12LANGUAGETYPECD"));
		po.setX12LanguageQualifierTypeCd(rs.getString("X12LANGUAGEQUALIFIERTYPECD"));
		po.setX12RaceEthnicityTypeCode(rs.getString("X12RACEETHNICITYTYPECD"));
		po.setZipPlus4Cd(rs.getString("ZIPPLUS4CD"));
	
		return po;		
	}
	
	

}
