package gov.hhs.cms.ff.fm.eps.ep.dao.mappers;


import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import gov.hhs.cms.ff.fm.eps.ep.po.SbmPolicyVersionPO;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;

/**
 * @author j.radziewski
 *
 */
public class SbmPolicyVersionRowMapper implements RowMapper<SbmPolicyVersionPO> {

	@Override
	public SbmPolicyVersionPO mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		SbmPolicyVersionPO po = new SbmPolicyVersionPO();

		po.setPolicyVersionId(rs.getLong("POLICYVERSIONID"));
		po.setSubscriberStateCd(rs.getString("SUBSCRIBERSTATECD"));
		po.setExchangePolicyId(rs.getString("EXCHANGEPOLICYID"));
		// Though DATE type in Oracle, need to get as Timestamp to return seconds,
		// otherwise policyMatch on HIGHDATE will fail.
		po.setMaintenanceStartDateTime(rs.getTimestamp("MAINTENANCESTARTDATETIME").toLocalDateTime());
		po.setMaintenanceEndDateTime(rs.getTimestamp("MAINTENANCEENDDATETIME").toLocalDateTime());
		po.setIssuerPolicyId(rs.getString("ISSUERPOLICYID"));
		po.setIssuerHiosId(rs.getString("ISSUERHIOSID"));
		po.setIssuerSubscriberID(rs.getString("ISSUERSUBSCRIBERID"));
		po.setExchangeAssignedSubscriberID(rs.getString("EXCHANGEASSIGNEDSUBSCRIBERID"));
		po.setTransControlNum(rs.getString("TRANSCONTROLNUM"));
		po.setSourceExchangeId(rs.getString("SOURCEEXCHANGEID"));
		po.setPlanID(rs.getString("PLANID"));
		po.setX12InsrncLineTypeCd(rs.getString("X12INSRNCLINETYPECD"));
		po.setTransMsgID(rs.getLong("SBMTRANSMSGID"));
		po.setPolicyStartDate(rs.getDate("POLICYSTARTDATE").toLocalDate());
		po.setPolicyEndDate(DateTimeUtil.getLocalDateFromSqlDate(rs.getDate("POLICYENDDATE")));

		return po;
	}

}
