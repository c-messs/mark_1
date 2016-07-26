package gov.hhs.cms.ff.fm.eps.ep.dao.impl;

import gov.hhs.cms.ff.fm.eps.ep.dao.GenericEpsDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.PolicyMemberDateDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.mappers.PolicyMemberDateRowMapper;
import gov.hhs.cms.ff.fm.eps.ep.enums.EpsEntityNames;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyMemberDatePO;

import java.util.List;

import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

/**
 * @author eps
 *
 */
public class PolicyMemberDateDaoImpl extends GenericEpsDao<PolicyMemberDatePO> implements PolicyMemberDateDao {
	
	private String selectPolicyMemberDates;
	
	/**
	 * Constructor
	 */
	public PolicyMemberDateDaoImpl() {
		
		this.setRowMapper(new PolicyMemberDateRowMapper());
	}
	
	/**
	 * @param simpleJdbcInsert
	 */
	public void setSimpleJdbcInsert(SimpleJdbcInsert simpleJdbcInsert) {
		
		super.simpleJdbcInsert = simpleJdbcInsert;
		simpleJdbcInsert.withTableName(EpsEntityNames.POLICY_MEMBER_DATE_ENTITY_NAME.getValue());
	}

	@Override
	public List<PolicyMemberDatePO> getPolicyMemberDates(Long policyVersionId) {

		return (List<PolicyMemberDatePO>) jdbcTemplate.query(this.selectPolicyMemberDates, rowMapper, policyVersionId);
	}

	@Override
	public void insertPolicyMemberDates(List<PolicyMemberDatePO> poList) {
		
		simpleJdbcInsert.executeBatch(getEpsBeanPropertySqlParameterSourceArrayFromList(poList));		
	}

	/**
	 * @param sql the sql to set
	 */
	public void setSelectPolicyMemberDates(String sql) {
		this.selectPolicyMemberDates = sql;
	}

}
