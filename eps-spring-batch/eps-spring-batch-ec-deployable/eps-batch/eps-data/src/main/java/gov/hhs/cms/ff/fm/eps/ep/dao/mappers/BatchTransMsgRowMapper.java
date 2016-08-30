package gov.hhs.cms.ff.fm.eps.ep.dao.mappers;

import gov.hhs.cms.ff.fm.eps.ep.po.BatchTransMsgPO;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

/**
 * @author j.radziewski
 *
 */
public class BatchTransMsgRowMapper implements RowMapper<BatchTransMsgPO> {

	private static final String SOURCE_VERSION_ID = "SOURCEVERSIONID";
	
	@Override
	public BatchTransMsgPO mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		BatchTransMsgPO po = new BatchTransMsgPO();
		
		po.setTransMsgId(rs.getLong("TRANSMSGID"));
		po.setBatchId(rs.getLong("BATCHID"));
		po.setProcessedToDbStatusTypeCd(rs.getString("PROCESSEDTODBSTATUSTYPECD"));
		po.setExchangePolicyId(rs.getString("EXCHANGEPOLICYID"));
		po.setSubscriberStateCd(rs.getString("SUBSCRIBERSTATECD"));
		po.setIssuerHiosId(rs.getString("ISSUERHIOSID"));
		
		if (rs.getLong(SOURCE_VERSION_ID) != 0) {
			po.setSourceVersionId(rs.getLong(SOURCE_VERSION_ID));
		}
			po.setSourceVersionDateTime(DateTimeUtil.getLocalDateTimeFromSqlTimestamp(rs.getTimestamp("SOURCEVERSIONDATETIME")));
		po.setTransMsgSkipReasonTypeCd(rs.getString("TRANSMSGSKIPREASONTYPECD"));
		po.setTransMsgSkipReasonDesc(rs.getString("TRANSMSGSKIPREASONDESC"));
		
		return po;
	}
}
