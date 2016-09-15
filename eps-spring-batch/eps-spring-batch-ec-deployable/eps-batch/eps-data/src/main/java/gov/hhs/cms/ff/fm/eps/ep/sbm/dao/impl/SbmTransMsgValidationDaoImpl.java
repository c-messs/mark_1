package gov.hhs.cms.ff.fm.eps.ep.sbm.dao.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;

import gov.hhs.cms.ff.fm.eps.ep.dao.GenericEpsDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.SbmTransMsgValidationDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.mappers.SbmTransMsgValidationRowMapper;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmTransMsgValidationPO;

/**
 * 
 * SbmTransMsgValidationDao implementation
 *
 */
public class SbmTransMsgValidationDaoImpl extends GenericEpsDao<SbmTransMsgValidationPO> implements SbmTransMsgValidationDao {

	private String insertErrorListSql;
	private String verifyXprErrorSql;
	private String verifyXprWarningSql;
	private String selectValidationSql;
	
	/**
	 * Constructor
	 */
	public SbmTransMsgValidationDaoImpl() {
		this.rowMapper = new SbmTransMsgValidationRowMapper();
	}

	@Override
	public void insertSbmTransMsgValidationList(List<SbmTransMsgValidationPO> errList) {

		final String userId = super.userVO.getUserId();

		jdbcTemplate.batchUpdate(insertErrorListSql, new BatchPreparedStatementSetter() {

			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {

				SbmTransMsgValidationPO po = errList.get(i);
				ps.setLong(1, po.getSbmTransMsgId());
				ps.setString(2, po.getSbmErrorWarningTypeCd());
				ps.setString(3, po.getElementInErrorNm());
				ps.setLong(4, po.getValidationSeqNum());
				
				String exchangeMemberId = po.getExchangeAssignedMemberId();
				if(StringUtils.isNotBlank(exchangeMemberId) && exchangeMemberId.length() > 50) {
					exchangeMemberId = null;
				}
				ps.setString(5, exchangeMemberId);
				ps.setString(6, userId);
				ps.setString(7, userId);	
			}

			@Override
			public int getBatchSize() {
				return errList.size();
			}
		});	
	}

	@Override
	public boolean verifyXprErrorsExist(Long sbmFileProcSumId) {

		int errorCnt = jdbcTemplate.queryForObject(verifyXprErrorSql, Integer.class, sbmFileProcSumId);

		return (errorCnt > 0);
	}

	@Override
	public boolean verifyXprWarningsExist(Long sbmFileProcSumId) {

		int errorCnt = jdbcTemplate.queryForObject(verifyXprWarningSql, Integer.class, sbmFileProcSumId);

		return (errorCnt > 0);
	}
	
	@Override
	public List<SbmTransMsgValidationPO> selectValidation(Long sbmTransMsgId) {
		
		return (List<SbmTransMsgValidationPO>) jdbcTemplate.query(selectValidationSql, new SbmTransMsgValidationRowMapper(), sbmTransMsgId);
	}



	/**
	 * @param insertErrorListSql the insertErrorListSql to set
	 */
	public void setInsertErrorListSql(String insertErrorListSql) {
		this.insertErrorListSql = insertErrorListSql;
	}

	/**
	 * @param verifyXprErrorSql the verifyXprErrorSql to set
	 */
	public void setVerifyXprErrorSql(String verifyXprErrorSql) {
		this.verifyXprErrorSql = verifyXprErrorSql;
	}

	/**
	 * @param verifyXprWarningSql the verifyXprWarningSql to set
	 */
	public void setVerifyXprWarningSql(String verifyXprWarningSql) {
		this.verifyXprWarningSql = verifyXprWarningSql;
	}

	/**
	 * @param selectValidationSql the selectValidationSql to set
	 */
	public void setSelectValidationSql(String selectValidationSql) {
		this.selectValidationSql = selectValidationSql;
	}

	

}
