package gov.hhs.cms.ff.fm.eps.ep.dao.mappers;

import gov.hhs.cms.ff.fm.eps.ep.po.PolicyVersionPO;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

/**
 * @author j.radziewski
 *
 * Map all rows except system columns.  This rowMapper is used for quieries that need all
 * data for merging.
 */
public class PolicyVersionRowMapper implements RowMapper<PolicyVersionPO> {

	private static final String SOURCE_VERSION_ID = "SOURCEVERSIONID";

	/* (non-Javadoc)
	 * @see org.springframework.jdbc.core.RowMapper#mapRow(java.sql.ResultSet, int)
	 */
	@Override
	public PolicyVersionPO mapRow(ResultSet rs, int rowNum) throws SQLException {

		PolicyVersionPO po = new PolicyVersionPO();

		po.setPolicyVersionId(rs.getLong("POLICYVERSIONID"));
		po.setSubscriberStateCd(rs.getString("SUBSCRIBERSTATECD"));
		po.setExchangePolicyId(rs.getString("EXCHANGEPOLICYID"));
		// Though DATE type in Oracle, need to get as Timestamp to return seconds,
		// otherwise policyMatch on HIGHDATE will fail.
		po.setMaintenanceStartDateTime(rs.getTimestamp("MAINTENANCESTARTDATETIME").toLocalDateTime());
		po.setMaintenanceEndDateTime(rs.getTimestamp("MAINTENANCEENDDATETIME").toLocalDateTime());
		po.setIssuerPolicyId(rs.getString("ISSUERPOLICYID"));
		po.setIssuerHiosId(rs.getString("ISSUERHIOSID"));
		po.setIssuerTaxPayerId(rs.getString("ISSUERTAXPAYERID"));
		po.setIssuerNm(rs.getString("ISSUERNM"));
		po.setIssuerSubscriberID(rs.getString("ISSUERSUBSCRIBERID"));
		po.setExchangeAssignedSubscriberID(rs.getString("EXCHANGEASSIGNEDSUBSCRIBERID"));
		po.setTransDateTime(DateTimeUtil.getLocalDateTimeFromSqlTimestamp(rs.getTimestamp("TRANSDATETIME")));
		po.setTransControlNum(rs.getString("TRANSCONTROLNUM"));
		po.setEligibilityStartDate(DateTimeUtil.getLocalDateFromSqlDate(rs.getDate("ELIGIBILITYSTARTDATE")));
		po.setEligibilityEndDate(DateTimeUtil.getLocalDateFromSqlDate(rs.getDate("ELIGIBILITYENDDATE")));
		po.setPremiumPaidToEndDate(DateTimeUtil.getLocalDateFromSqlDate(rs.getDate("PREMIUMPAIDTOENDDATE")));
		po.setLastPremiumPaidDate(DateTimeUtil.getLocalDateFromSqlDate(rs.getDate("LASTPREMIUMPAIDDATE")));
		po.setPlanID(rs.getString("PLANID"));
		po.setEmployerGroupNum(rs.getString("EMPLOYERGROUPNUM"));
		po.setX12InsrncLineTypeCd(rs.getString("X12INSRNCLINETYPECD"));
		po.setInsrncAplctnTypeCd(rs.getString("INSRNCAPLCTNTYPECD"));
		po.setEmployerIdentificationNum(rs.getString("EMPLOYERIDENTIFICATIONNUM"));
		po.setTransMsgID(rs.getLong("TRANSMSGID"));
		po.setChangeReportedDate(DateTimeUtil.getLocalDateFromSqlDate(rs.getDate("CHANGEREPORTEDDATE")));
		po.setX12CoverageLevelTypeCd(rs.getString("X12COVERAGELEVELTYPECD"));
		po.setPolicyStartDate(rs.getDate("POLICYSTARTDATE").toLocalDate());
		po.setPolicyEndDate(DateTimeUtil.getLocalDateFromSqlDate(rs.getDate("POLICYENDDATE")));
		Long sourcVersionId = rs.getLong(SOURCE_VERSION_ID);
		if (sourcVersionId != 0) {
			po.setSourceVersionId(rs.getLong(SOURCE_VERSION_ID));
		} 
		po.setSourceVersionDateTime(DateTimeUtil.getLocalDateTimeFromSqlTimestamp(rs.getTimestamp("SOURCEVERSIONDATETIME")));

		return po;
	}
}
