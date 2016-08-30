package gov.hhs.cms.ff.fm.eps.ep.sbm.dao.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.springframework.jdbc.core.PreparedStatementSetter;

import gov.hhs.cms.ff.fm.eps.ep.dao.GenericEpsDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.SbmResponseDao;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmResponsePO;

public class SbmResponseDaoImpl extends GenericEpsDao<SbmResponsePO> implements SbmResponseDao {
	
	private String insertResponseSql;

	@Override
	public void insertSbmResponse(SbmResponsePO po) {

		jdbcTemplate.update(insertResponseSql, new PreparedStatementSetter() {
			public void setValues(PreparedStatement ps) throws SQLException {
				
				ps.setLong(1, po.getSbmFileProcSumId());
				
				if (po.getSbmFileInfoId() != null) {
					ps.setLong(2, po.getSbmFileInfoId());
				} else {
					ps.setObject(2,  null);
				}
				
				ps.setLong(3, po.getPhysicalDocumentId());
				ps.setString(4, po.getResponseCd());
				
				ps.setString(5, userVO.getUserId());
				ps.setString(6, userVO.getUserId());
			}
		});	
	}

	/**
	 * @param insertResponseSql the insertResponseSql to set
	 */
	public void setInsertResponseSql(String insertResponseSql) {
		this.insertResponseSql = insertResponseSql;
	}

}
