package gov.hhs.cms.ff.fm.eps.ep.dao.impl;

import gov.hhs.cms.ff.fm.eps.ep.dao.GenericEpsDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.PolicyStatusDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.mappers.PolicyStatusRowMapper;
import gov.hhs.cms.ff.fm.eps.ep.enums.EpsEntityNames;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyStatusPO;

import java.util.List;

import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

/**
 * @author eps
 *
 */
public class PolicyStatusDaoImpl extends GenericEpsDao<PolicyStatusPO> implements PolicyStatusDao {
	
	private String selectPolicyStatusList;

	
	/**
	 * Constructor, set rowMapper once.
	 */
	public PolicyStatusDaoImpl() {
		
		this.setRowMapper(new PolicyStatusRowMapper());
	}

	/**
	 * @param sql
	 */
	public void setSelectPolicyStatusList(String sql) {
	
		this.selectPolicyStatusList = sql;
	}

	/**
	 * @param simpleJdbcInsert
	 */
	public void setSimpleJdbcInsert(SimpleJdbcInsert simpleJdbcInsert) {
		super.simpleJdbcInsert = simpleJdbcInsert;
		simpleJdbcInsert.withTableName(
				EpsEntityNames.POLICY_STATUS_ENTITY_NAME.getValue());

	}

	/* (non-Javadoc)
	 * @see gov.hhs.cms.ff.fm.eps.ep.dao.PolicyStatusDao#getPolicyStatusList(java.lang.Long)
	 */
	@Override
	public List<PolicyStatusPO> getPolicyStatusList(Long policyVersionId) {

		return (List<PolicyStatusPO>) jdbcTemplate.query(
				this.selectPolicyStatusList, rowMapper, policyVersionId);
	}

	/* (non-Javadoc)
	 * @see gov.hhs.cms.ff.fm.eps.ep.dao.PolicyStatusDao#insertPolicyStatusList(java.util.List)
	 */
	@Override
	public void insertPolicyStatusList(List<PolicyStatusPO> psl) {
		
		simpleJdbcInsert.executeBatch(getEpsBeanPropertySqlParameterSourceArrayFromList(psl));
	}

}
