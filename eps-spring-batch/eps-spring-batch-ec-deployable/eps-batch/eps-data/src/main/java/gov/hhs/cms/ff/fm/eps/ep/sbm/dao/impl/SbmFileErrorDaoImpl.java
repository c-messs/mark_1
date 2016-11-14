package gov.hhs.cms.ff.fm.eps.ep.sbm.dao.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;

import gov.hhs.cms.ff.fm.eps.ep.dao.GenericEpsDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.SbmFileErrorDao;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmFileErrorPO;

/**
 * 
 * Implementation of SbmFileErrorDao 
 *
 */
public class SbmFileErrorDaoImpl extends GenericEpsDao<SbmFileErrorPO> implements SbmFileErrorDao {
	
	
	private String insertErrorListSql;

	@Override
	public void insertSbmFileErrorList(final List<SbmFileErrorPO> errList) {
		
		final String userId = super.userVO.getUserId();
		
		jdbcTemplate.batchUpdate(insertErrorListSql, new BatchPreparedStatementSetter() {

			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {

				SbmFileErrorPO po = errList.get(i);
				ps.setLong(1, po.getSbmFileInfoId());
				ps.setString(2, po.getSbmErrorWarningTypeCd());
				ps.setString(3, po.getElementInErrorNm());
				ps.setLong(4, po.getSbmFileErrorSeqNum());
				ps.setString(5, userId);
				ps.setString(6, userId);
			}
		
			@Override
			public int getBatchSize() {
				return errList.size();
			}
		});	
	}
	

	/**
	 * @param sql the insertErrorListSql to set
	 */
	public void setInsertErrorListSql(String sql) {
		this.insertErrorListSql = sql;
	}

}
