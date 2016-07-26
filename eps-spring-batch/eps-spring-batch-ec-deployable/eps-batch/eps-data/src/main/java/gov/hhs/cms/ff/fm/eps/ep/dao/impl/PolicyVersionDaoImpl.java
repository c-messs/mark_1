package gov.hhs.cms.ff.fm.eps.ep.dao.impl;

import gov.hhs.cms.ff.fm.eps.ep.dao.EpsBeanPropertySqlParameterSource;
import gov.hhs.cms.ff.fm.eps.ep.dao.GenericEpsDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.PolicyVersionDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.mappers.PolicyMatchFFMRowMapper;
import gov.hhs.cms.ff.fm.eps.ep.dao.mappers.PolicyVersionRowMapper;
import gov.hhs.cms.ff.fm.eps.ep.enums.EProdEnum;
import gov.hhs.cms.ff.fm.eps.ep.enums.EpsEntityNames;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyVersionPO;
import gov.hhs.cms.ff.fm.eps.ep.vo.PolicyVersionSearchCriteriaVO;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import com.accenture.foundation.common.exception.ApplicationException;

/**
 * @author 
 *
 */
public class PolicyVersionDaoImpl extends GenericEpsDao<PolicyVersionPO> implements PolicyVersionDao {
	
	private static final Logger LOG = LoggerFactory.getLogger(PolicyVersionDaoImpl.class);

	private String selectLatestPolicyVersionByExchangePolicyIdAndSubscriberStCd;
	private String updatePolicyVersionDate;
	private String selectPolicyVersionByIdAndState;
	private String selectMaxPolicyMaintStartDateTime;

	private PolicyVersionRowMapper policyVersionRowMapper;
	private PolicyMatchFFMRowMapper  policyMatchFFMRowMapper;

	/**
	 * Constructor
	 */
	public PolicyVersionDaoImpl() {

		this.policyVersionRowMapper = new PolicyVersionRowMapper();
		this.policyMatchFFMRowMapper = new PolicyMatchFFMRowMapper();
	}

	/**
	 * @param simpleJdbcInsert
	 */
	public void setSimpleJdbcInsert(SimpleJdbcInsert simpleJdbcInsert) {
		super.simpleJdbcInsert = simpleJdbcInsert;
		simpleJdbcInsert
		.withTableName(EpsEntityNames.POLICY_VERSION_ENTITY_NAME
				.getValue());

	}

	private Long insertPolicy (final PolicyVersionPO po) throws DuplicateKeyException {
		Long pk = sequenceHelper.nextSequenceId("PolicyVersionSeq");
		po.setPolicyVersionId(pk);
		simpleJdbcInsert.execute(new EpsBeanPropertySqlParameterSource(po, super.userVO));
		return pk;

	}

	@Override
	public Long insertPolicyVersion(PolicyVersionPO policy)  {

		Long pvId = null;

		if (policy.getPreviousPolicyVersionId() != null) {

			DateTime msd = policy.getMaintenanceStartDateTime();
			final Timestamp msdMiunusOneMilli = new Timestamp(msd.getMillis() - 1l);
			final long prevId = policy.getPreviousPolicyVersionId();
			final String userId = userVO.getUserId();

			jdbcTemplate.execute(this.updatePolicyVersionDate, new PreparedStatementCallback<Boolean>() {
				@Override
				public Boolean doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
					ps.setTimestamp(1, msdMiunusOneMilli);
					ps.setString(2, userId);
					ps.setLong(3, prevId);
					return ps.execute();
				}
			});
		}
		try {
		
			pvId = insertPolicy(policy);

		}  catch (DuplicateKeyException dkex) {

			// If in the extremely rare condition, 2 different versions of the same policy are processed within 
			// the same millisecond, the second one will fail the following constraint:
			//     XAK1POLICYVERSION (EXCHANGEPOLICYID, SUBSCRIBERSTATECD, MAINTENANCESTARTDATETIME),
			// Take a quick nap, increment the inbound policy maintenanceStartDateTime by one millisecond, then try again.
			// If it fails again, throw ApplicationException and skip.
			LOG.info("\r\n    Attempting to process 2 versions of the same policy within 1 millisecond. " + 
					" \r\n    Incrementing the MAINTENANCESTARTDATETIME of second policy 1 millisecond and  will try to insert again.");
			try {
				Thread.sleep(1l);
				
			} catch (InterruptedException iex) {
				
				throw new ApplicationException(iex, EProdEnum.EPROD_10.getCode());
			}
			DateTime policyMSD = policy.getMaintenanceStartDateTime();
			policy.setMaintenanceStartDateTime(policyMSD.plusMillis(1));
			pvId = insertPolicy(policy);
		}
		return pvId;
	}



	/*
	 * Get latest policy match by Exchange Assigned Policy Id (GPN) and Subscriber state cd
	 */
	@Override
	public List<PolicyVersionPO> matchPolicyVersionByPolicyIdAndStateCd(PolicyVersionSearchCriteriaVO criteria) {

		Object[] args = new Object[] {
				criteria.getExchangePolicyId(),
				criteria.getSubscriberStateCd(),
		};

		List<PolicyVersionPO> poList = (List<PolicyVersionPO>) jdbcTemplate
				.query(this.selectLatestPolicyVersionByExchangePolicyIdAndSubscriberStCd, this.policyMatchFFMRowMapper, args);

		return poList;
	}


	@Override
	public PolicyVersionPO getPolicyVersionById(Long policyId, String subscriberStateCd) {
		return (PolicyVersionPO) jdbcTemplate.queryForObject(
				selectPolicyVersionByIdAndState, this.policyVersionRowMapper, policyId, subscriberStateCd);
	}


	@Override
	public DateTime getLatestPolicyMaintenanceStartDateTime() {

		Timestamp policyMaintStartDateTime = jdbcTemplate.queryForObject(selectMaxPolicyMaintStartDateTime, Timestamp.class);

		if(policyMaintStartDateTime != null) {
			return new DateTime(policyMaintStartDateTime);
		}

		return null;
	}

	/**
	 * @param sql the selectLatestPolicyVersionByExchangePolicyIdAndSubscriberStCd to set
	 */
	public void setSelectLatestPolicyVersionByExchangePolicyIdAndSubscriberStCd(String sql) {

		this.selectLatestPolicyVersionByExchangePolicyIdAndSubscriberStCd = sql;
	}


	public void setUpdatePolicyVersionDate(String sql) {

		this.updatePolicyVersionDate = sql;
	}

	/**
	 * @param selectPolicyVersionByIdAndState the selectPolicyVersionByIdAndState to set
	 */
	public void setSelectPolicyVersionByIdAndState(String sql) {

		this.selectPolicyVersionByIdAndState = sql;
	}

	/**
	 * @param selectMaxPolicyMaintStartDateTime the selectMaxPolicyMaintStartDateTime to set
	 */
	public void setSelectMaxPolicyMaintStartDateTime(String sql) {

		this.selectMaxPolicyMaintStartDateTime = sql;
	}

}
