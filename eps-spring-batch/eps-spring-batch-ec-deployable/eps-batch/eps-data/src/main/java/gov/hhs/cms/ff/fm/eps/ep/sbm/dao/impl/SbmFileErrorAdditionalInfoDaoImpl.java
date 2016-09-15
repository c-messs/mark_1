package gov.hhs.cms.ff.fm.eps.ep.sbm.dao.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;

import com.accenture.foundation.common.exception.ApplicationException;

import gov.hhs.cms.ff.fm.eps.ep.dao.GenericEpsDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.SbmFileErrorAdditionalInfoDao;
import gov.hhs.cms.ff.fm.eps.ep.enums.EProdEnum;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmFileErrorAdditionalInfoPO;

/**
 * 
 * Implementation of SbmFileErrorAdditionalInfoDao 
 *
 */
public class SbmFileErrorAdditionalInfoDaoImpl extends GenericEpsDao<SbmFileErrorAdditionalInfoPO> 
	implements SbmFileErrorAdditionalInfoDao {

	private String insertErrAddlInfoListSql;

	
	@Override
	public void insertSbmFileErrAddlInfoList(List<SbmFileErrorAdditionalInfoPO> errList) {

		
		final String userId = super.userVO.getUserId();
		
		int[] rowsAffected = jdbcTemplate.batchUpdate(insertErrAddlInfoListSql, new BatchPreparedStatementSetter() {

			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {

				SbmFileErrorAdditionalInfoPO po = errList.get(i);
				ps.setLong(1, po.getSbmFileInfoId());
				ps.setString(2, po.getAdditionalErrorInfoText());
				ps.setLong(3, po.getSbmFileErrorSeqNum());
				ps.setString(4, userId);
				ps.setString(5, userId);
			}
		
			@Override
			public int getBatchSize() {
				return errList.size();
			}
		});	
		
		if (rowsAffected.length < errList.size()) {
			throw new ApplicationException(EProdEnum.EPROD_10.getCode());
		}
	}


	/**
	 * @param sql the insertErrAddlInfoListSql to set
	 */
	public void setInsertErrAddlInfoListSql(String sql) {
		this.insertErrAddlInfoListSql = sql;
	}
	

}
