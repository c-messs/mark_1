package gov.hhs.cms.ff.fm.eps.ep.sbm.dao.impl;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;

import com.accenture.foundation.common.exception.ApplicationException;

import gov.hhs.cms.ff.fm.eps.ep.dao.GenericEpsDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.SbmPolicyMemberVersionDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.mappers.SbmPolicyMemberVersionRowMapper;
import gov.hhs.cms.ff.fm.eps.ep.enums.EProdEnum;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyMemberVersionPO;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmPolicyMemberVersionPO;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;

/**
 * 
 * Implementation of SbmPolicyMemberVersionDao 
 *
 */
public class SbmPolicyMemberVersionDaoImpl extends GenericEpsDao<SbmPolicyMemberVersionPO> implements SbmPolicyMemberVersionDao {
	
	private final static Logger LOG = LoggerFactory.getLogger(SbmPolicyMemberVersionDaoImpl.class);

	private String selectPMVListSql;
	private String selectPMVListPolicyMatchSql;
	
	private String insertStagingPMVListSql;
	
	private String mergePMVSql;
	private String mergeLangSql;
	private String mergeRaceSql;
	private String mergeAddrSql;
	
	private String deleteStagingPMVSql;

	/**
	 * Constructor
	 */
	public SbmPolicyMemberVersionDaoImpl() {

		this.rowMapper = new SbmPolicyMemberVersionRowMapper();
	}

	/**
	 * Returns PolicyMemberVersions For Policy Match
	 * @param subscriberStateCd
	 * @param policyVersionId
	 * @return List<SbmPolicyMemberVersionPO>
	 */
	public List<SbmPolicyMemberVersionPO> getPolicyMemberVersionsForPolicyMatch(String subscriberStateCd, Long policyVersionId) {
		return (List<SbmPolicyMemberVersionPO>) jdbcTemplate.query(
				selectPMVListPolicyMatchSql, new PolicyMatchSbmRowMapper(), subscriberStateCd, policyVersionId);
	}

	@Override
	public List<SbmPolicyMemberVersionPO> getPolicyMemberVersions(String subscriberStateCd, Long policyVersionId) {

		return (List<SbmPolicyMemberVersionPO>) jdbcTemplate.query(selectPMVListSql, rowMapper, subscriberStateCd, policyVersionId);
	}

	@Override
	public Long insertStagingPolicyMemberVersion(final SbmPolicyMemberVersionPO po) {

		final Long policyMemberVersionId = sequenceHelper.nextSequenceId("POLICYMEMBERVERSIONSEQ");

		jdbcTemplate.update(insertStagingPMVListSql, new PreparedStatementSetter() {
			public void setValues(PreparedStatement ps) throws SQLException {

				ps.setLong(1, policyMemberVersionId);
				ps.setString(2, po.getSubscriberInd());
				ps.setString(3, po.getIssuerAssignedMemberID());
				ps.setString(4, po.getExchangeMemberID());
				ps.setTimestamp(5, DateTimeUtil.getSqlTimestamp(LocalDateTime.now()));
				ps.setDate(6, DateTimeUtil.getSqlDate(po.getPolicyMemberDeathDate()));
				ps.setString(7, po.getPolicyMemberLastNm());
				ps.setString(8, po.getPolicyMemberFirstNm());
				ps.setString(9, po.getPolicyMemberMiddleNm());
				ps.setString(10, po.getPolicyMemberSalutationNm());
				ps.setString(11, po.getPolicyMemberSuffixNm());
				ps.setString(12, po.getPolicyMemberSSN());
				ps.setString(13, po.getExchangePolicyId());
				ps.setString(14, po.getSubscriberStateCd());
				ps.setString(15, po.getX12TobaccoUseTypeCode());
				ps.setDate(16, DateTimeUtil.getSqlDate(po.getPolicyMemberBirthDate()));
				ps.setString(17, po.getX12GenderTypeCd());
				ps.setString(18, po.getIncorrectGenderTypeCode());
				ps.setString(19, po.getNonCoveredSubscriberInd());
				ps.setString(20, po.getX12LanguageCode());
				ps.setString(21, po.getX12LanguageQualifierTypeCd());
				ps.setString(22, po.getX12RaceEthnicityTypeCode());
				ps.setString(23, po.getZipPlus4Cd());
				if (po.getPriorPolicyMemberVersionId() != null) {
					ps.setLong(24, po.getPriorPolicyMemberVersionId());
				} else {
					ps.setObject(24,  null);
				}
				ps.setString(25, userVO.getUserId());
				ps.setString(26, userVO.getUserId());				
				
				//TODO add SBMTRANSMSGID once db schema is altered. Already exists in PO.
			}
		});	

		return policyMemberVersionId;
	}
	
	@Override
	public BigInteger mergePolicyMemberVersion(Long sbmFileProcSumId) {
		
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
					ps = con.prepareStatement(mergePMVSql);

					ps.setLong(1, sbmFileProcSumId);
					ps.setLong(2, sbmFileProcSumId);
					ps.setString(3, userVO.getUserId());
					ps.setString(4, userVO.getUserId());
					ps.setString(5, userVO.getUserId());
					
					return ps;

				} catch (Exception e) {
					try {
						if (ps != null) {
							ps.close();
						}
					} catch (SQLException e1) {
						LOG.error("Failed to close prepared statement in SbmPolicyMemberVersionDaoImpl caused by: " + e.toString());
					}
					throw new ApplicationException(EProdEnum.EPROD_10.getCode(), e);
				}
			}
		});
		return BigInteger.valueOf(count);	
	}
	
	@Override
	public BigInteger mergeLang(Long sbmFileProcSumId) {
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
					ps = con.prepareStatement(mergeLangSql);

					ps.setString(1, userVO.getUserId());
					ps.setString(2, userVO.getUserId());
					ps.setLong(3, sbmFileProcSumId);
					
					return ps;

				} catch (Exception e) {
					try {
						if (ps != null) {
							ps.close();
						}
					} catch (SQLException e1) {
						LOG.error("Failed to close prepared statement in SbmPolicyMemberVersionDaoImpl caused by: " + e.toString());
					}
					throw new ApplicationException(EProdEnum.EPROD_10.getCode(), e);
				}
			}
		});
		return BigInteger.valueOf(count);
	}

	@Override
	public BigInteger mergeRace(Long sbmFileProcSumId) {
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
					ps = con.prepareStatement(mergeRaceSql);

					ps.setString(1, userVO.getUserId());
					ps.setString(2, userVO.getUserId());
					ps.setLong(3, sbmFileProcSumId);
					
					return ps;

				} catch (Exception e) {
					try {
						if (ps != null) {
							ps.close();
						}
					} catch (SQLException e1) {
						LOG.error("Failed to close prepared statement in SbmPolicyMemberVersionDaoImpl caused by: " + e.toString());
					}
					throw new ApplicationException(EProdEnum.EPROD_10.getCode(), e);
				}
			}
		});
		return BigInteger.valueOf(count);
	}

	@Override
	public BigInteger mergeAddr(Long sbmFileProcSumId) {
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
					ps = con.prepareStatement(mergeAddrSql);

					ps.setString(1, userVO.getUserId());
					ps.setString(2, userVO.getUserId());
					ps.setLong(3, sbmFileProcSumId);
					
					return ps;

				} catch (Exception e) {
					try {
						if (ps != null) {
							ps.close();
						}
					} catch (SQLException e1) {
						LOG.error("Failed to close prepared statement in SbmPolicyMemberVersionDaoImpl caused by: " + e.toString());
					}
					throw new ApplicationException(EProdEnum.EPROD_10.getCode(), e);
				}
			}
		});
		return BigInteger.valueOf(count);
	}
	
	@Override
	public int deleteStaging(Long sbmFileProcSumId) {

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
					ps = con.prepareStatement(deleteStagingPMVSql);

					ps.setLong(1, sbmFileProcSumId);

					return ps;

				} catch (Exception e) {
					try {
						if (ps != null) {
							ps.close();
						}
					} catch (SQLException e1) {
						LOG.error("Failed to close prepared statement in SbmPolicyMemberVersionDaoImpl caused by: " + e.toString());
					}
					throw new ApplicationException(EProdEnum.EPROD_10.getCode(), e);
				}
			}
		});
		return count;
	}



	@Override
	public Long insertPolicyMemberVersion(PolicyMemberVersionPO pmv) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<PolicyMemberVersionPO> getPolicyMemberVersions(Long policyVersionId, String subscriberStateCd) {
		// TODO Auto-generated method stub
		return Collections.emptyList();
	}


	static private class PolicyMatchSbmRowMapper implements RowMapper<SbmPolicyMemberVersionPO> {
		
		/**
		 * Maps Resultset
		 * @param rs
		 * @param rowNum
		 * @return po		 
		 */
		public SbmPolicyMemberVersionPO mapRow(ResultSet rs, int rowNum) throws SQLException {

			SbmPolicyMemberVersionPO po = new SbmPolicyMemberVersionPO();

			po.setExchangeMemberID(rs.getString("EXCHANGEMEMBERID"));

			return po;
		}
	}


	/**
	 * @param selectPMVListSql the selectPMVListSql to set
	 */
	public void setSelectPMVListSql(String selectPMVListSql) {
		this.selectPMVListSql = selectPMVListSql;
	}

	/**
	 * @param selectPMVListPolicyMatchSql the selectPMVListPolicyMatchSql to set
	 */
	public void setSelectPMVListPolicyMatchSql(String selectPMVListPolicyMatchSql) {
		this.selectPMVListPolicyMatchSql = selectPMVListPolicyMatchSql;
	}

	/**
	 * @param insertStagingPMVListSql the insertStagingPMVListSql to set
	 */
	public void setInsertStagingPMVListSql(String insertStagingPMVListSql) {
		this.insertStagingPMVListSql = insertStagingPMVListSql;
	}

	/**
	 * @param mergePMVSql the mergePMVSql to set
	 */
	public void setMergePMVSql(String mergePMVSql) {
		this.mergePMVSql = mergePMVSql;
	}

	/**
	 * @param mergeLangSql the mergeLangSql to set
	 */
	public void setMergeLangSql(String mergeLangSql) {
		this.mergeLangSql = mergeLangSql;
	}

	/**
	 * @param mergeRaceSql the mergeRaceSql to set
	 */
	public void setMergeRaceSql(String mergeRaceSql) {
		this.mergeRaceSql = mergeRaceSql;
	}

	/**
	 * @param mergeAddrSql the mergeAddrSql to set
	 */
	public void setMergeAddrSql(String mergeAddrSql) {
		this.mergeAddrSql = mergeAddrSql;
	}

	/**
	 * @param deleteStagingPMVSql the deleteStagingPMVSql to set
	 */
	public void setDeleteStagingPMVSql(String deleteStagingPMVSql) {
		this.deleteStagingPMVSql = deleteStagingPMVSql;
	}

}
