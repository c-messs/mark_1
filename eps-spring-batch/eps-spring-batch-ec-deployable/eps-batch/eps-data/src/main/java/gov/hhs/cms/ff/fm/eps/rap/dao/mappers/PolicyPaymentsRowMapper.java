/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.rap.dao.mappers;

import gov.hhs.cms.ff.fm.eps.rap.dto.PolicyPaymentTransDTO;
import gov.hhs.cms.ff.fm.eps.rap.util.DataCommonUtil;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

/**
 * @author {prasad.ghanta}
 *
 */
public class PolicyPaymentsRowMapper implements RowMapper<PolicyPaymentTransDTO> {

	@Override
	public PolicyPaymentTransDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		PolicyPaymentTransDTO dto = new PolicyPaymentTransDTO();
		dto.setPolicyPaymentTransId(rs.getLong("policyPaymentTransId")); 
		dto.setFinancialProgramTypeCd(rs.getString("financialProgramTypeCd"));
		dto.setPaymentAmount(rs.getBigDecimal("paymentAmount"));
		dto.setPolicyVersionId(rs.getLong("policyVersionId"));
		dto.setCoverageDate(DataCommonUtil.convertToDateTime(rs.getDate("coverageDate")));
		dto.setTotalPremiumAmount(rs.getBigDecimal("totalPremiumAmount"));
		dto.setTransPeriodTypeCd(rs.getString("transPeriodTypeCd"));
		dto.setExchangePolicyId(rs.getString("exchangePolicyId")); 
		dto.setSubscriberStateCd(rs.getString("subscriberStateCd"));
		dto.setPaymentCoverageStartDate(DataCommonUtil.convertToDateTime(rs.getDate("paymentCoverageStartDate")));
		dto.setPaymentCoverageEndDate(DataCommonUtil.convertToDateTime(rs.getDate("paymentCoverageEndDate")));
		dto.setLastPaymentProcStatusTypeCd(rs.getString("lastPaymentProcStatusTypeCd"));
		
		Integer prorationDaysOfCoverageNum = rs.getInt("prorationDaysOfCoverageNum");
		dto.setProrationDaysOfCoverageNum(prorationDaysOfCoverageNum == 0 ? null : prorationDaysOfCoverageNum);
		
		Long parentPolicyPaymentTransId = rs.getLong("parentPolicyPaymentTransId");
		dto.setParentPolicyPaymentTransId(parentPolicyPaymentTransId == 0 ? null : parentPolicyPaymentTransId);
		
		return dto;
	}

}
