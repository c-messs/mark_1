package gov.hhs.cms.ff.fm.eps.ep.dao.impl;

import gov.hhs.cms.ff.fm.eps.ep.dao.GenericEpsDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.PolicyPremiumDao;
import gov.hhs.cms.ff.fm.eps.ep.enums.EpsEntityNames;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyPremiumPO;

import java.util.List;

import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

/**
 * @author eps
 *
 */
public class PolicyPremiumDaoImpl extends GenericEpsDao<PolicyPremiumPO> implements PolicyPremiumDao {

	/**
	 * 
	 */
	public PolicyPremiumDaoImpl() {
		
	}

	/**
	 * @param simpleJdbcInsert
	 */
	public void setSimpleJdbcInsert(SimpleJdbcInsert simpleJdbcInsert) {
		super.simpleJdbcInsert = simpleJdbcInsert;
		simpleJdbcInsert.withTableName(
				EpsEntityNames.POLICY_PREMIUM_ENTITY_NAME.getValue());

	}


	/* (non-Javadoc)
	 * @see gov.hhs.cms.ff.fm.eps.ep.dao.PolicyPremiumDao#insertPolicyPremiumList(java.util.List)
	 */
	@Override
	public void insertPolicyPremiumList(List<PolicyPremiumPO> ppl) {

		simpleJdbcInsert.executeBatch(getEpsBeanPropertySqlParameterSourceArrayFromList(ppl));
	}

}
