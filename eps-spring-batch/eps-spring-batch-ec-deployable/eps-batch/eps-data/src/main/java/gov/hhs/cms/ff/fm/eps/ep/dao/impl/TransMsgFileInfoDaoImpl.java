package gov.hhs.cms.ff.fm.eps.ep.dao.impl;

import gov.hhs.cms.ff.fm.eps.ep.dao.EpsBeanPropertySqlParameterSource;
import gov.hhs.cms.ff.fm.eps.ep.dao.GenericEpsDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.TransMsgFileInfoDao;
import gov.hhs.cms.ff.fm.eps.ep.enums.EProdEnum;
import gov.hhs.cms.ff.fm.eps.ep.enums.EpsEntityNames;
import gov.hhs.cms.ff.fm.eps.ep.po.TransMsgFileInfoPO;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import com.accenture.foundation.common.exception.ApplicationException;

/**
 * @author 
 *
 */
public class TransMsgFileInfoDaoImpl extends GenericEpsDao<TransMsgFileInfoPO> implements TransMsgFileInfoDao {

	private static final Logger LOG = LoggerFactory.getLogger(TransMsgFileInfoDaoImpl.class);
	private String insertStagingBER;

	/**
	 * @param simpleJdbcInsert
	 */
	public void setSimpleJdbcInsert(SimpleJdbcInsert simpleJdbcInsert) {
		super.simpleJdbcInsert = simpleJdbcInsert;
		simpleJdbcInsert
		.withTableName(EpsEntityNames.TRANS_MSG_FILE_INFO_ENTITY_NAME
				.getValue());
	}


	@Override
	public Long insertTransMsgFileInfo(final TransMsgFileInfoPO tmfiPO, final String berXml) {

		final Long pk = sequenceHelper.nextSequenceId("TransMsgFileInfoSeq");
		tmfiPO.setTransMsgFileInfoId(pk);
		this.simpleJdbcInsert.execute(new EpsBeanPropertySqlParameterSource(tmfiPO, super.userVO));

		jdbcTemplate.update(new PreparedStatementCreator() {
			/** 
			 * Creating a preparedStatement
			 * @param con
			 * @return preparedstatement
			 */
			public PreparedStatement createPreparedStatement(Connection con)
					throws ApplicationException {
				PreparedStatement ps = null;
				try {
					ps = con.prepareStatement(insertStagingBER);

					Clob clob = ps.getConnection().createClob();
					clob.setString(1, berXml);

					ps.setLong(1, pk);
					ps.setClob(2, clob);

					return ps;

				} catch (Exception e) {
					try {
						if (ps != null) {
							ps.close();
						}
					} catch (SQLException e1) {
						LOG.error("Failed to close prepared statement in TransMsgDaoImpl caused by: "+e.toString());
					}
					throw new ApplicationException(EProdEnum.EPROD_10.getCode(), e);
				}
			}
		});
		return pk;
	}


	public void setInsertStagingBER(String insertStagingBER) {
		this.insertStagingBER = insertStagingBER;
	}

}
