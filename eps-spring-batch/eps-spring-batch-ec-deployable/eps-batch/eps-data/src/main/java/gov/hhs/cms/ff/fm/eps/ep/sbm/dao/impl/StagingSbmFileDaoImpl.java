package gov.hhs.cms.ff.fm.eps.ep.sbm.dao.impl;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.PreparedStatementCreator;

import com.accenture.foundation.common.exception.ApplicationException;

import gov.hhs.cms.ff.fm.eps.ep.dao.GenericEpsDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.StagingSbmFileDao;
import gov.hhs.cms.ff.fm.eps.ep.enums.EProdEnum;
import gov.hhs.cms.ff.fm.eps.ep.po.StagingSbmFilePO;

/**
 * @author j.radziewski
 *
 */
public class StagingSbmFileDaoImpl extends GenericEpsDao<StagingSbmFilePO> implements StagingSbmFileDao {

	private static final Logger LOG = LoggerFactory.getLogger(StagingSbmFileDaoImpl.class);

	private String insertStagingSql;
	private String deleteStagingSql;
	private String selectStagingPolicyCountSql;
	private String selectStagingPolicies;

	@Override
	public boolean insertFileToStaging(final StagingSbmFilePO po) {

		int result = jdbcTemplate.update(new PreparedStatementCreator() {
			/** 
			 * Creating a preparedStatement
			 * @param con
			 * @return preparedstatement
			 */
			public PreparedStatement createPreparedStatement(Connection con)
					throws ApplicationException {
				PreparedStatement ps = null;
				try {
					ps = con.prepareStatement(insertStagingSql);

					ps.setCharacterStream(1, po.getSbmFileXMLStream());
					ps.setLong(2, po.getBatchId());
					ps.setLong(3, po.getSbmFileProcessingSummaryId());
					ps.setLong(4, po.getSbmFileInfoId());
					ps.setString(5, userVO.getUserId());
					ps.setString(6, userVO.getUserId());
					
					return ps;

				} catch (Exception e) {
					try {
						if (ps != null) {
							ps.close();
						}
					} catch (SQLException e1) {
						LOG.error("Failed to close prepared statement in StagingSbmFileDaoImpl caused by: " + e.toString());
					}
					throw new ApplicationException(EProdEnum.EPROD_10.getCode(), e);
				}
			}
		});
		try {
			if(po.getSbmFileXMLStream() != null) {
				po.getSbmFileXMLStream().close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return (result == 1);
	}


	@Override
	public int deleteStagingSbmFile(Long sbmFileProcSumId) {

		int count = jdbcTemplate.update(new PreparedStatementCreator() {
			/** 
			 * Creating a preparedStatement
			 * @param con
			 * @return preparedstatement
			 */
			public PreparedStatement createPreparedStatement(Connection con)
					throws ApplicationException {
				PreparedStatement ps = null;
				try {
					ps = con.prepareStatement(deleteStagingSql);

					ps.setLong(1, sbmFileProcSumId);

					return ps;

				} catch (Exception e) {
					try {
						if (ps != null) {
							ps.close();
						}
					} catch (SQLException e1) {
						LOG.error("Failed to close prepared statement in StagingSbmFileDaoImpl caused by: " + e.toString());
					}
					throw new ApplicationException(EProdEnum.EPROD_10.getCode(), e);
				}
			}
		});
		return count;
	}


	@Override
	public Integer selectPolicyCount(final Long sbmFileProcSumId) {

		return jdbcTemplate.queryForObject(selectStagingPolicyCountSql, new Object[] {sbmFileProcSumId}, Integer.class);
	}
	
	@Override
	public List<String> getStagingPolicies(final Long jobId) {

		Object[] args = new Object[] {jobId};
		List<String> stagingPolicyList = (List<String>) jdbcTemplate.queryForList(selectStagingPolicies, String.class, args);
		return stagingPolicyList;
	}


	/**
	 * @param insertStagingSql the insertStagingSql to set
	 */
	public void setInsertStagingSql(String insertStagingSql) {
		this.insertStagingSql = insertStagingSql;
	}

	/**
	 * @param deleteStagingSql the deleteStagingSql to set
	 */
	public void setDeleteStagingSql(String deleteStagingSql) {
		this.deleteStagingSql = deleteStagingSql;
	}

	/**
	 * @param selectStagingPolicyCountSql the selectStagingPolicyCountSql to set
	 */
	public void setSelectStagingPolicyCountSql(String selectStagingPolicyCountSql) {
		this.selectStagingPolicyCountSql = selectStagingPolicyCountSql;
	}

	/**
	 * @param selectStagingPolicies the selectStagingPolicies to set
	 */
	public void setSelectStagingPolicies(String selectStagingPolicies) {
		this.selectStagingPolicies = selectStagingPolicies;
	}

}
