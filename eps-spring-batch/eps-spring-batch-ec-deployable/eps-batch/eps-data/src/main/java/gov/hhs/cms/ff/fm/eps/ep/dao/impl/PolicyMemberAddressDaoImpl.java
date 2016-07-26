package gov.hhs.cms.ff.fm.eps.ep.dao.impl;

import gov.hhs.cms.ff.fm.eps.ep.dao.GenericEpsDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.PolicyMemberAddressDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.mappers.PolicyMemberAddressRowMapper;
import gov.hhs.cms.ff.fm.eps.ep.enums.EpsEntityNames;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyMemberAddressPO;

import java.util.List;

import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

/**
 * @author eps
 *
 */
public class PolicyMemberAddressDaoImpl extends GenericEpsDao<PolicyMemberAddressPO> implements PolicyMemberAddressDao {

	private String selectPolicyMemberAddressByPolVerId;

	/**
	 * 
	 */
	public PolicyMemberAddressDaoImpl() {

		this.setRowMapper(new PolicyMemberAddressRowMapper());
	}


	@Override
	public List<PolicyMemberAddressPO> getPolicyMemberAddress(Long policyVersionId) {

		return (List<PolicyMemberAddressPO>) jdbcTemplate.query(selectPolicyMemberAddressByPolVerId, rowMapper, policyVersionId);
	}

	/**
	 * @param simpleJdbcInsert
	 */
	public void setSimpleJdbcInsert(SimpleJdbcInsert simpleJdbcInsert) {
		
		super.simpleJdbcInsert = simpleJdbcInsert;
		simpleJdbcInsert.includeSynonymsForTableColumnMetaData();
		simpleJdbcInsert.withTableName(EpsEntityNames.POLICY_MEMBER_ADDRESS_ENTITY_NAME.getValue());
	}

	/* (non-Javadoc)
	 * @see gov.hhs.cms.ff.fm.eps.ep.dao.PolicyMemberAddressDao#insertPolicyMemberAddressList(java.util.List, java.lang.Long)
	 */
	@Override
	public void insertPolicyMemberAddressList(List<PolicyMemberAddressPO> pmal) {
		
		simpleJdbcInsert.executeBatch(getEpsBeanPropertySqlParameterSourceArrayFromList(pmal));

	}

	/**
	 * @param sql
	 */
	public void setSelectPolicyMemberAddressByPolVerId(String sql) {

		this.selectPolicyMemberAddressByPolVerId = sql;
	}

}
