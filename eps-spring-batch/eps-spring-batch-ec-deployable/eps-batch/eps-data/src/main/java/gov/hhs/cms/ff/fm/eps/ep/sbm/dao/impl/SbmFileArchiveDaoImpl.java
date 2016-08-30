package gov.hhs.cms.ff.fm.eps.ep.sbm.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.PreparedStatementCreator;

import com.accenture.foundation.common.exception.ApplicationException;

import gov.hhs.cms.ff.fm.eps.ep.dao.GenericEpsDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.SbmFileArchiveDao;
import gov.hhs.cms.ff.fm.eps.ep.enums.EProdEnum;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmFileArchivePO;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;

public class SbmFileArchiveDaoImpl extends GenericEpsDao<SbmFileArchivePO> implements SbmFileArchiveDao {

	private static final Logger LOG = LoggerFactory.getLogger(SbmFileArchiveDaoImpl.class);

	String insertArchiveSql;

	@Override
	public boolean saveFileToArchive(SbmFileArchivePO po) {

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
					ps = con.prepareStatement(insertArchiveSql);

					ps.setLong(1, po.getSbmFileInfoId());
					ps.setTimestamp(2, DateTimeUtil.getSqlTimestamp(po.getSbmFileCreateDateTime()));
					ps.setString(3, po.getSbmFileNm());
					ps.setString(4, po.getSbmFileId());
					if (po.getSbmFileNum() != null) {
						ps.setInt(5, po.getSbmFileNum());
					} else {
						ps.setObject(5, null);
					}
					ps.setString(6, po.getTradingPartnerId());
					ps.setString(7, po.getTenantNum());
					if (po.getCoverageYear() != null) {
						ps.setInt(8, po.getCoverageYear());
					} else {
						ps.setObject(8, null);
					}
					ps.setString(9, po.getIssuerFileSetId());
					ps.setString(10, po.getIssuerId());
					ps.setString(11, po.getSubscriberStateCd());					
					ps.setString(12, userVO.getUserId());
					ps.setString(13, userVO.getUserId());
					ps.setLong(14, po.getSbmFileInfoId());

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

		return (result == 1);
	}

	/**
	 * @param insertArchiveSql the insertArchiveSql to set
	 */
	public void setInsertArchiveSql(String sql) {
		this.insertArchiveSql = sql;
	}

}
