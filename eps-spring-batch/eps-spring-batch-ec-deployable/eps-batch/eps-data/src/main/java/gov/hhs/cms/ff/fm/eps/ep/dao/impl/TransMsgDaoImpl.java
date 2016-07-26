package gov.hhs.cms.ff.fm.eps.ep.dao.impl;

import gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentRequestDTO;
import gov.hhs.cms.ff.fm.eps.ep.dao.TransMsgDao;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;

/**
 * @author eps
 * 
 * Does NOT extend GenericEpsDao since NOT using SimpleJdbcInsert.  Instead
 * method extractTransMsg calls an XML extract from/insert into query from
 * StagingBer to TransMsg.
 *
 */
public class TransMsgDaoImpl implements TransMsgDao {

	private String extractTransMsgSQL;
	private JdbcTemplate jdbcTemplate;

	/**
	 * 
	 */
	public TransMsgDaoImpl() {

	}
	

	/* (non-Javadoc)
	 * @see gov.hhs.cms.ff.fm.eps.ep.dao.TransMsgDao#insertTransMsg(gov.hhs.cms.ff.fm.eps.ep.po.TransMsgPO)
	 */
	@Override
	public void extractTransMsg(final BenefitEnrollmentRequestDTO berDTO) {

		final String userId = berDTO.getBatchId() != null ? berDTO.getBatchId().toString() : null;

		jdbcTemplate.update(extractTransMsgSQL, new PreparedStatementSetter() {
			public void setValues(PreparedStatement ps) throws SQLException {
				ps.setObject(1, berDTO.getTxnMessageFileInfoId());
				ps.setString(2, berDTO.getTxnMessageDirectionType().getValue());
				ps.setString(3, berDTO.getTxnMessageType().getValue());
				ps.setString(4, userId);
				ps.setString(5, userId);
				ps.setObject(6, berDTO.getTxnMessageFileInfoId());		
			}
		});		
	}


	/**
	 * @param extractTransMsgSQL the extractTransMsgSQL to set
	 */
	public void setExtractTransMsgSQL(String sql) {
		
		this.extractTransMsgSQL = sql;
	}


	/**
	 * @param jdbcTemplate the jdbcTemplate to set
	 */
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		
		this.jdbcTemplate = jdbcTemplate;
	}

	
}