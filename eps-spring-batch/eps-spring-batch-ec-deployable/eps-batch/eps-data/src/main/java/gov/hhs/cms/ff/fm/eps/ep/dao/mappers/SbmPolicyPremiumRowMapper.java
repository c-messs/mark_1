package gov.hhs.cms.ff.fm.eps.ep.dao.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import gov.hhs.cms.ff.fm.eps.ep.po.SbmPolicyPremiumPO;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;

/**
 * @author j.radziewski
 *
 */
public class SbmPolicyPremiumRowMapper implements RowMapper<SbmPolicyPremiumPO> {

	@Override
	public SbmPolicyPremiumPO mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		SbmPolicyPremiumPO po = new SbmPolicyPremiumPO();

		po.setEffectiveStartDate(DateTimeUtil.getLocalDateFromSqlDate(rs.getDate("EFFECTIVESTARTDATE")));
		po.setEffectiveEndDate(DateTimeUtil.getLocalDateFromSqlDate(rs.getDate("EFFECTIVEENDDATE"))); 
		po.setTotalPremiumAmount(rs.getBigDecimal("TOTALPREMIUMAMOUNT"));
		po.setOtherPaymentAmount1(rs.getBigDecimal("OTHERPAYMENTAMOUNT1"));
		po.setOtherPaymentAmount2(rs.getBigDecimal("OTHERPAYMENTAMOUNT2"));
		po.setExchangeRateArea(rs.getString("EXCHANGERATEAREA"));
		po.setIndividualResponsibleAmount(rs.getBigDecimal("INDIVIDUALRESPONSIBLEAMOUNT"));
		po.setCsrAmount(rs.getBigDecimal("CSRAMOUNT"));
		po.setAptcAmount(rs.getBigDecimal("APTCAMOUNT"));
		po.setProratedPremiumAmount(rs.getBigDecimal("PRORATEDPREMIUMAMOUNT"));
		po.setProratedAptcAmount(rs.getBigDecimal("PRORATEDAPTCAMOUNT"));
		po.setProratedCsrAmount(rs.getBigDecimal("PRORATEDCSRAMOUNT"));
		po.setInsrncPlanVariantCmptTypeCd(rs.getString("INSRNCPLANVARIANTCMPTTYPECD"));
		return po;	
	}

}
