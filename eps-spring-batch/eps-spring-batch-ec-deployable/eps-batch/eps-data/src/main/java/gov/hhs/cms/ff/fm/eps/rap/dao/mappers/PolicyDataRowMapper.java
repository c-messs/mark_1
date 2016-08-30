/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.rap.dao.mappers;

import static gov.hhs.cms.ff.fm.eps.rap.domain.RapConstants.HIGH_DATE;
import gov.hhs.cms.ff.fm.eps.rap.dto.PolicyDataDTO;
import gov.hhs.cms.ff.fm.eps.rap.util.DataCommonUtil;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.joda.time.DateTime;
import org.springframework.jdbc.core.RowMapper;

/**
 * @author rajesh.talanki
 *
 */
public class PolicyDataRowMapper implements RowMapper<PolicyDataDTO> {

	@Override
	public PolicyDataDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		PolicyDataDTO dto = new PolicyDataDTO();
		dto.setPolicyVersionId(rs.getLong("POLICYVERSIONID"));
		dto.setMarketplaceGroupPolicyId(rs.getString("MARKETPLACEGROUPPOLICYID"));
		dto.setExchangePolicyId(rs.getString("EXCHANGEPOLICYID"));
		dto.setSubscriberStateCd(rs.getString("SUBSCRIBERSTATECD"));
		dto.setIssuerHiosId(rs.getString("ISSUERHIOSID"));	
		dto.setInsrncAplctnTypeCd(rs.getString("INSRNCAPLCTNTYPECD"));
		dto.setPlanId(rs.getString("PLANID"));	
		dto.setPolicyStatus(rs.getString("INSURANACEPOLICYSTATUSTYPECD"));
		dto.setPolicyStartDate(DataCommonUtil.convertToDateTime(rs.getDate("POLICYSTARTDATE")));
		
		Date policyEndDate = rs.getDate("POLICYENDDATE");
		dto.setPolicyEndDate(
				policyEndDate != null ? DataCommonUtil.convertToDateTime(policyEndDate) : new DateTime(HIGH_DATE));
		
		Timestamp policyVersionTs = rs.getTimestamp("MAINTENANCESTARTDATETIME");
		if (policyVersionTs != null) {
			dto.setMaintenanceStartDateTime(new DateTime(policyVersionTs.getTime()));
		}
		
		dto.setIssuerStartDate(DataCommonUtil.convertToDateTime(rs.getDate("ISSUEREFFECTIVEDATE")));
		
		return dto;
	}

}
