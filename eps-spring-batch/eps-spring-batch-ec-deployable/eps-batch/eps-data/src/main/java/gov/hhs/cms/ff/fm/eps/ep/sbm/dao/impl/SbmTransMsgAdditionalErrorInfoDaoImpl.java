package gov.hhs.cms.ff.fm.eps.ep.sbm.dao.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;

import com.accenture.foundation.common.exception.ApplicationException;

import gov.hhs.cms.ff.fm.eps.ep.dao.GenericEpsDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.SbmTransMsgAdditionalErrorInfoDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.mappers.SbmTransMsgAdditionalErrorInfoRowMapper;
import gov.hhs.cms.ff.fm.eps.ep.enums.EProdEnum;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmTransMsgAdditionalErrorInfoPO;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmTransMsgValidationPO;

/**
 * 
 *SbmTransMsgAdditionalErrorInfoDao implementation
 *
 */
public class SbmTransMsgAdditionalErrorInfoDaoImpl extends GenericEpsDao<SbmTransMsgAdditionalErrorInfoPO> 
implements SbmTransMsgAdditionalErrorInfoDao {

	private String insertErrAddlInfoListSql;
	private String selectErrAddlInfoSql;

	/**
	 * Constructor
	 */
	public SbmTransMsgAdditionalErrorInfoDaoImpl() {
		
		this.rowMapper = new SbmTransMsgAdditionalErrorInfoRowMapper();
	}
	

	@Override
	public void insertSbmTransMsgAddlErrInfoList(List<SbmTransMsgAdditionalErrorInfoPO> errList) {


		int[] rowsAffected = jdbcTemplate.batchUpdate(insertErrAddlInfoListSql, new BatchPreparedStatementSetter() {

			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {

				SbmTransMsgAdditionalErrorInfoPO po = errList.get(i);
				ps.setLong(1, po.getSbmTransMsgId());
				ps.setString(2, po.getAdditionalErrorInfoText());
				ps.setLong(3, po.getValidationSeqNum());
				ps.setString(4, userVO.getUserId());
				ps.setString(5, userVO.getUserId());
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

	@Override
	public List<SbmTransMsgAdditionalErrorInfoPO> selectSbmTransMsgAddlErrInfo(final SbmTransMsgValidationPO valPO) {

		return (List<SbmTransMsgAdditionalErrorInfoPO>) jdbcTemplate.query(selectErrAddlInfoSql, rowMapper, 
				valPO.getSbmTransMsgId(), valPO.getValidationSeqNum());
	}



	/**
	 * @param sql the insertErrAddlInfoListSql to set
	 */
	public void setInsertErrAddlInfoListSql(String sql) {
		this.insertErrAddlInfoListSql = sql;
	}


	/**
	 * @param selectErrAddlInfoSql the selectErrAddlInfoSql to set
	 */
	public void setSelectErrAddlInfoSql(String selectErrAddlInfoSql) {
		this.selectErrAddlInfoSql = selectErrAddlInfoSql;
	}






}
