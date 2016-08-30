package gov.hhs.cms.ff.fm.eps.ep.dao.impl;

import gov.hhs.cms.ff.fm.eps.ep.dao.EpsBeanPropertySqlParameterSource;
import gov.hhs.cms.ff.fm.eps.ep.dao.GenericEpsDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.PolicyMemberVersionDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.mappers.PolicyMemberVersionRowMapper;
import gov.hhs.cms.ff.fm.eps.ep.enums.EpsEntityNames;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyMemberVersionPO;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

/**
 * @author eps
 *
 */
public class PolicyMemberVersionDaoImpl extends GenericEpsDao<PolicyMemberVersionPO> implements PolicyMemberVersionDao {

	private String updatePolicyMemberVersion;
	private String selectPolicyMemberVersionsByPolVerIdAndState;

	/**
	 * 
	 */
	public PolicyMemberVersionDaoImpl() {

		this.setRowMapper(new PolicyMemberVersionRowMapper());
	}

	/**
	 * @param simpleJdbcInsert
	 */
	public void setSimpleJdbcInsert(SimpleJdbcInsert simpleJdbcInsert) {
		
		super.simpleJdbcInsert = simpleJdbcInsert;
		simpleJdbcInsert.withTableName(EpsEntityNames.POLICY_MEMBER_VERSION_ENTITY_NAME.getValue());
	}

	/**
	 * @param po
	 * @return
	 * @throws DuplicateKeyException
	 */
	private Long insertPO(final PolicyMemberVersionPO po) throws DuplicateKeyException {
		
		Long pk = sequenceHelper.nextSequenceId("PolicyMemberVersionSeq");
		po.setPolicyMemberVersionId(pk);
		simpleJdbcInsert.execute(new EpsBeanPropertySqlParameterSource(po, super.userVO));
		return pk;

	}

	/* (non-Javadoc)
	 * @see gov.hhs.cms.ff.fm.eps.ep.dao.PolicyMemberVersionDao#insertPolicyMemberVersion(gov.hhs.cms.ff.fm.eps.ep.po.PolicyMemberVersionPO)
	 */
	@Override
	public Long insertPolicyMemberVersion(final PolicyMemberVersionPO member) {

		Long pmvId = null;
		
		if (member.getPolicyMemberVersionId() != null) {

			final LocalDateTime msd = member.getMaintenanceStartDateTime();
			final long prevId = member.getPolicyMemberVersionId();
			final String userId = userVO.getUserId();
			
			jdbcTemplate.execute(this.updatePolicyMemberVersion, new PreparedStatementCallback<Boolean>() {
				@Override
				public Boolean doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
					ps.setTimestamp(1, Timestamp.valueOf(msd.minusNanos(1000000)));
					ps.setString(2, userId);
					ps.setLong(3, prevId);
					return ps.execute();
				}
			});
		}
		pmvId = insertPO(member);
		return pmvId;

	}


	@Override
	public List<PolicyMemberVersionPO> getPolicyMemberVersions(Long policyVersionId, String subscriberStateCd) {

		return (List<PolicyMemberVersionPO>) jdbcTemplate.query(
				this.selectPolicyMemberVersionsByPolVerIdAndState, rowMapper, policyVersionId, subscriberStateCd);
	}

	public void setSelectPolicyMemberVersionsByPolVerIdAndState(String sql) {

		this.selectPolicyMemberVersionsByPolVerIdAndState = sql;
	}
	
	/**
	 * @param sql
	 */
	public void setUpdatePolicyMemberVersion(String sql) {
		
		this.updatePolicyMemberVersion = sql;
	}

}
