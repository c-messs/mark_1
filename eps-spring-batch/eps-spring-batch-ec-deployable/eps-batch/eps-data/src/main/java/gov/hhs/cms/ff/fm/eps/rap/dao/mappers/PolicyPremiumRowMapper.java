/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.rap.dao.mappers;

import gov.hhs.cms.ff.fm.eps.rap.domain.PolicyPremium;
import gov.hhs.cms.ff.fm.eps.rap.util.DataCommonUtil;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

/**
 * @author rajesh.talanki
 *
 */
public class PolicyPremiumRowMapper implements RowMapper<PolicyPremium> {

	@Override
	public PolicyPremium mapRow(ResultSet rs, int rowNum) throws SQLException {

		PolicyPremium premiumRec = new PolicyPremium();
		premiumRec.setPolicyVersionId(rs.getLong("policyVersionId"));
		premiumRec.setEffectiveStartDate(DataCommonUtil.convertToDateTime(rs.getDate("effectiveStartDate")));
		premiumRec.setEffectiveEndDate(DataCommonUtil.convertToDateTime(rs.getDate("effectiveEndDate")));
		premiumRec.setAptcAmount(rs.getBigDecimal("aptcAmount"));
		premiumRec.setCsrAmount(rs.getBigDecimal("csrAmount"));
		premiumRec.setTotalPremiumAmount(rs.getBigDecimal("totalPremiumAmount"));
		premiumRec.setProratedAptcAmount(rs.getBigDecimal("proratedAptcAmount"));
		premiumRec.setProratedCsrAmount(rs.getBigDecimal("proratedCsrAmount"));
		premiumRec.setProratedPremiumAmount(rs.getBigDecimal("proratedPremiumAmount"));

		return premiumRec;
	}

}
