package gov.hhs.cms.ff.fm.eps.ep.dao.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import gov.hhs.cms.ff.fm.eps.ep.po.PolicyVersionPO;
import gov.hhs.cms.ff.fm.eps.rap.util.DataCommonUtil;

import org.springframework.jdbc.core.RowMapper;

/**
 * @author j.radziewski
 *
 */
public class PolicyMatchFFMRowMapper implements RowMapper<PolicyVersionPO> {
	
	private static final String SOURCE_VERSION_ID = "SOURCEVERSIONID";

	@Override
	public PolicyVersionPO mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		PolicyVersionPO po = new PolicyVersionPO();
	
		po.setPolicyVersionId(rs.getLong("POLICYVERSIONID"));
		po.setSubscriberStateCd(rs.getString("SUBSCRIBERSTATECD"));
		po.setIssuerHiosId(rs.getString("ISSUERHIOSID"));
		po.setTransMsgID(rs.getLong("TRANSMSGID"));
		Long sourcVersionId = rs.getLong(SOURCE_VERSION_ID);
		if (sourcVersionId != 0) {
			po.setSourceVersionId(rs.getLong(SOURCE_VERSION_ID));
		} 
		po.setSourceVersionDateTime(DataCommonUtil.convertToDateTime(rs.getTimestamp("SOURCEVERSIONDATETIME")));
		
		po.setMarketplaceGroupPolicyId(rs.getString("MARKETPLACEGROUPPOLICYID"));
		
		return po;
	}

}
