package gov.hhs.cms.ff.fm.eps.ep.dao.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import gov.hhs.cms.ff.fm.eps.ep.po.SbmTransMsgPO;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;

public class SbmTransMsgRowMapper implements RowMapper<SbmTransMsgPO> {

	@Override
	public SbmTransMsgPO mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		SbmTransMsgPO po = new SbmTransMsgPO();
	    
		po.setSbmTransMsgId(rs.getLong("SBMTRANSMSGID"));
		po.setTransMsgDateTime(DateTimeUtil.getLocalDateTimeFromSqlTimestamp(rs.getTimestamp("TRANSMSGDATETIME")));
		po.setSubscriberStateCd(rs.getString("SUBSCRIBERSTATECD"));
		po.setRecordControlNum(rs.getInt("RECORDCONTROLNUM"));
		po.setPlanId(rs.getString("PLANID"));
		po.setSbmTransMsgProcStatusTypeCd(rs.getString("SBMTRANSMSGPROCSTATUSTYPECD"));
		po.setExchangeAssignedPolicyId(rs.getString("EXCHANGEASSIGNEDPOLICYID"));
		po.setExchangeAssignedSubscriberId(rs.getString("EXCHANGEASSIGNEDSUBSCRIBERID"));
		
		return po;
	}
}
