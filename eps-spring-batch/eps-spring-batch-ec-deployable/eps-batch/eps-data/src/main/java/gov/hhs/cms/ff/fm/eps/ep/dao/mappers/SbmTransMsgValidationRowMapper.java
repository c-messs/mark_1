package gov.hhs.cms.ff.fm.eps.ep.dao.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import gov.hhs.cms.ff.fm.eps.ep.po.SbmTransMsgValidationPO;

/**
 * @author j.radziewski
 *
 */
public class SbmTransMsgValidationRowMapper implements RowMapper<SbmTransMsgValidationPO> {

	@Override
	public SbmTransMsgValidationPO mapRow(ResultSet rs, int rowNum) throws SQLException {

		SbmTransMsgValidationPO po = new SbmTransMsgValidationPO();
		
		po.setSbmTransMsgId(rs.getLong("SBMTRANSMSGID"));
		po.setElementInErrorNm(rs.getString("ELEMENTINERRORNM"));
		po.setValidationSeqNum(rs.getLong("VALIDATIONSEQUENCENUM"));
		po.setSbmErrorWarningTypeCd(rs.getString("SBMERRORWARNINGTYPECD"));
		po.setExchangeAssignedMemberId(rs.getString("EXCHANGEASSIGNEDMEMBERID"));
		
		return po;
	}

}
