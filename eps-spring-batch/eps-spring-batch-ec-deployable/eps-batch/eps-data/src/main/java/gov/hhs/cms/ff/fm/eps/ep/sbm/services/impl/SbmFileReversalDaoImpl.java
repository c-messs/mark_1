package gov.hhs.cms.ff.fm.eps.ep.sbm.services.impl;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;

import com.accenture.foundation.common.exception.ApplicationException;

import gov.hhs.cms.ff.fm.eps.ep.dao.GenericEpsDao;
import gov.hhs.cms.ff.fm.eps.ep.enums.EProdEnum;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SbmFileReversalDao;

public class SbmFileReversalDaoImpl extends GenericEpsDao implements SbmFileReversalDao {

	private final static Logger LOG = LoggerFactory.getLogger(SbmFileReversalDaoImpl.class);

	protected JdbcTemplate jdbcTemplate;
	private String voidPolicyStatusSql;
	private String copyPrecedingPolicyVersionSql;
	private String copyPolicyVersionSql;
	private String copyPrecedingStatusSql;
	private String copyPolicyPremiumsSql;
	private String copyPolicyMembersSql;
	private String updateMaintDateTimesSql;

	@Override
	public void backOutFile(Long fileProcSummaryId) {

		int cntStatus = updatePolicyStatus(fileProcSummaryId);

		//Multiple Policy Versions J83
		int cntPrecedingPolicy = copyPrecedingPolicyVersion(fileProcSummaryId);

		//Single Policy version J86
		int cntPolicy = copyPolicyVersion(fileProcSummaryId);
		
		//Policy Status update for J83 (Multiple prior versions)
		int cntPrecedingStatus = copyPrecedingStatus(fileProcSummaryId);

		//Update maintenance timestamps for J83 and J86
		int cntMEDUpdates = updateMaintDateTime(fileProcSummaryId);

		int cntPremium = copyPolicyPremiums(fileProcSummaryId);
		int cntJoin = copyPolicyMembers(fileProcSummaryId);

		//TODO Remove or change to DEBUG after testing.
		LOG.info("\n\nTotal policy records effected by Reversal (BKO) from EPS.  sbmFileProcSumId: " + fileProcSummaryId +
				"\n     PrecedingPolicies (J83)      : " + cntPrecedingPolicy + 
				"\n     Policies (modified, J86)     : " + cntPolicy +
				"\n     Copied Preceding Status (J86): " + cntPrecedingStatus +	
				"\n     MED Updates                  : " + cntMEDUpdates +
				"\n     Premiums                     : " + cntPremium +
				"\n     Statuses                     : " + cntStatus +
				"\n     Joins                        : " + cntJoin + "\n");

	}

	private int updatePolicyStatus(Long fileProcSummaryId) {

		int cntRecord = jdbcTemplate.update(new PreparedStatementCreator() {
			/** 
			 * Creating a preparedStatement
			 * @param con
			 * @return preparedstatement
			 */
			public PreparedStatement createPreparedStatement(Connection con)
					throws ApplicationException {
				PreparedStatement ps = null;
				try {
					ps = con.prepareStatement(voidPolicyStatusSql);

					ps.setString(1, userVO.getUserId());
					ps.setString(2, userVO.getUserId());
					ps.setLong(3, fileProcSummaryId);

					return ps;

				} catch (Exception e) {
					try {
						if (ps != null) {
							ps.close();
						}
					} catch (SQLException e1) {
						LOG.error("Failed to close prepared statement caused by: " + e.toString());
					}
					throw new ApplicationException(EProdEnum.EPROD_10.getCode(), e);
				}
			}
		});
		return cntRecord;
	}

	private int copyPrecedingPolicyVersion(Long fileProcSummaryId) {

		int cntRecord = jdbcTemplate.update(new PreparedStatementCreator() {
			/** 
			 * Creating a preparedStatement
			 * @param con
			 * @return preparedstatement
			 */
			public PreparedStatement createPreparedStatement(Connection con)
					throws ApplicationException {
				PreparedStatement ps = null;
				try {
					ps = con.prepareStatement(copyPrecedingPolicyVersionSql);

					ps.setString(1, userVO.getUserId());
					ps.setString(2, userVO.getUserId());
					ps.setLong(3, fileProcSummaryId);

					return ps;

				} catch (Exception e) {
					try {
						if (ps != null) {
							ps.close();
						}
					} catch (SQLException e1) {
						LOG.error("Failed to close prepared statement caused by: " + e.toString());
					}
					throw new ApplicationException(EProdEnum.EPROD_10.getCode(), e);
				}
			}
		});
		return cntRecord;
	}

	private int copyPolicyVersion(Long fileProcSummaryId) {

		int cntRecord = jdbcTemplate.update(new PreparedStatementCreator() {

			/** 
			 * Creating a preparedStatement
			 * @param con
			 * @return preparedstatement
			 */
			public PreparedStatement createPreparedStatement(Connection con)
					throws ApplicationException {
				PreparedStatement ps = null;
				try {
					ps = con.prepareStatement(copyPolicyVersionSql);

					ps.setString(1, userVO.getUserId());
					ps.setString(2, userVO.getUserId());
					ps.setString(3, userVO.getUserId());
					ps.setString(4, userVO.getUserId());
					ps.setLong(5, fileProcSummaryId);

					return ps;

				} catch (Exception e) {
					try {
						if (ps != null) {
							ps.close();
						}
					} catch (SQLException e1) {
						LOG.error("Failed to close prepared statement caused by: " + e.toString());
					}
					throw new ApplicationException(EProdEnum.EPROD_10.getCode(), e);
				}
			}
		});
		return cntRecord;
	}
	
	private int copyPrecedingStatus(Long fileProcSummaryId) {

		int cntRecord = jdbcTemplate.update(new PreparedStatementCreator() {
			/** 
			 * Creating a preparedStatement
			 * @param con
			 * @return preparedstatement
			 */
			public PreparedStatement createPreparedStatement(Connection con)
					throws ApplicationException {
				PreparedStatement ps = null;
				try {
					ps = con.prepareStatement(copyPrecedingStatusSql);

					ps.setString(1, userVO.getUserId());
					ps.setString(2, userVO.getUserId());
					ps.setLong(3, fileProcSummaryId);

					return ps;

				} catch (Exception e) {
					try {
						if (ps != null) {
							ps.close();
						}
					} catch (SQLException e1) {
						LOG.error("Failed to close prepared statement caused by: " + e.toString());
					}
					throw new ApplicationException(EProdEnum.EPROD_10.getCode(), e);
				}
			}
		});
		return cntRecord;
	}

	private int copyPolicyPremiums(Long fileProcSummaryId) {

		int cntRecord = jdbcTemplate.update(new PreparedStatementCreator() {
			/** 
			 * Creating a preparedStatement
			 * @param con
			 * @return preparedstatement
			 */
			public PreparedStatement createPreparedStatement(Connection con)
					throws ApplicationException {
				PreparedStatement ps = null;
				try {
					ps = con.prepareStatement(copyPolicyPremiumsSql);

					ps.setString(1, userVO.getUserId());
					ps.setString(2, userVO.getUserId());
					ps.setLong(3, fileProcSummaryId);

					return ps;

				} catch (Exception e) {
					try {
						if (ps != null) {
							ps.close();
						}
					} catch (SQLException e1) {
						LOG.error("Failed to close prepared statement caused by: " + e.toString());
					}
					throw new ApplicationException(EProdEnum.EPROD_10.getCode(), e);
				}
			}
		});
		return cntRecord;
	}

	private int copyPolicyMembers(Long fileProcSummaryId) {

		int cntRecord = jdbcTemplate.update(new PreparedStatementCreator() {
			/** 
			 * Creating a preparedStatement
			 * @param con
			 * @return preparedstatement
			 */
			public PreparedStatement createPreparedStatement(Connection con)
					throws ApplicationException {
				PreparedStatement ps = null;
				try {
					ps = con.prepareStatement(copyPolicyMembersSql);

					ps.setString(1, userVO.getUserId());
					ps.setString(2, userVO.getUserId());
					ps.setLong(3, fileProcSummaryId);

					return ps;

				} catch (Exception e) {
					try {
						if (ps != null) {
							ps.close();
						}
					} catch (SQLException e1) {
						LOG.error("Failed to close prepared statement caused by: " + e.toString());
					}
					throw new ApplicationException(EProdEnum.EPROD_10.getCode(), e);
				}
			}
		});
		return cntRecord;
	}

	private int updateMaintDateTime(Long fileProcSummaryId) {

		int cntRecord = jdbcTemplate.update(new PreparedStatementCreator() {

			/** 
			 * Creating a preparedStatement
			 * @param con
			 * @return preparedstatement
			 */
			public PreparedStatement createPreparedStatement(Connection con)
					throws ApplicationException {
				PreparedStatement ps = null;
				try {
					ps = con.prepareStatement(updateMaintDateTimesSql);

					ps.setLong(1, fileProcSummaryId);

					return ps;

				} catch (Exception e) {
					try {
						if (ps != null) {
							ps.close();
						}
					} catch (SQLException e1) {
						LOG.error("Failed to close prepared statement caused by: " + e.toString());
					}
					throw new ApplicationException(EProdEnum.EPROD_10.getCode(), e);
				}
			}
		});
		return cntRecord;

	}

	/**
	 * @param jdbcTemplate the jdbcTemplate to set
	 */
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * @param voidPolicyStatusSql the voidPolicyStatusSql to set
	 */
	public void setVoidPolicyStatusSql(String voidPolicyStatusSql) {
		this.voidPolicyStatusSql = voidPolicyStatusSql;
	}

	/**
	 * @param copyPrecedingPolicyVersionSql the copyPrecedingPolicyVersionSql to set
	 */
	public void setCopyPrecedingPolicyVersionSql(String copyPrecedingPolicyVersionSql) {
		this.copyPrecedingPolicyVersionSql = copyPrecedingPolicyVersionSql;
	}

	/**
	 * @param copyPolicyVersionSql the copyPolicyVersionSql to set
	 */
	public void setCopyPolicyVersionSql(String copyPolicyVersionSql) {
		this.copyPolicyVersionSql = copyPolicyVersionSql;
	}

	/**
	 * @param copyPrecedingStatusSql the copyPrecedingStatusSql to set
	 */
	public void setCopyPrecedingStatusSql(String copyPrecedingStatusSql) {
		this.copyPrecedingStatusSql = copyPrecedingStatusSql;
	}

	/**
	 * @param copyPolicyPremiumSql the copyPolicyPremiumSql to set
	 */
	public void setCopyPolicyPremiumsSql(String copyPolicyPremiumsSql) {
		this.copyPolicyPremiumsSql = copyPolicyPremiumsSql;
	}

	/**
	 * @param copyPolicyMemberSql the copyPolicyMemberSql to set
	 */
	public void setCopyPolicyMembersSql(String copyPolicyMembersSql) {
		this.copyPolicyMembersSql = copyPolicyMembersSql;
	}

	/**
	 * @param updateMaintDateTimesSql the updateMaintDateTimesSql to set
	 */
	public void setUpdateMaintDateTimesSql(String updateMaintDateTimesSql) {
		this.updateMaintDateTimesSql = updateMaintDateTimesSql;
	}

}
