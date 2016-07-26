package gov.hhs.cms.ff.fm.eps.rap.dao.mappers;

import gov.hhs.cms.ff.fm.eps.rap.domain.IssuerUserFeeRate;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

/**
 * @author eps
 *
 */
public class UserFeeRateRowMapper implements RowMapper<IssuerUserFeeRate> {

	@Override
	public IssuerUserFeeRate mapRow(ResultSet rs, int rowNum)
			throws SQLException {
		
		IssuerUserFeeRate uf = new IssuerUserFeeRate();
		uf.setIssuerUfStateCd(rs.getString("ISSUERUFSTATECD"));
		uf.setInsrncAplctnTypeCd(rs.getString("INSRNCAPLCTNTYPECD"));
		uf.setIssuerUfFlatrate(rs.getBigDecimal("ISSUERUFFLATRATE"));
		uf.setIssuerUfPercent(rs.getBigDecimal("ISSUERUFPERCENT"));
		uf.setIssuerUfCoverageYear(rs.getString("ISSUERUFCOVERAGEYEAR"));
		
		return uf;
	}

}
