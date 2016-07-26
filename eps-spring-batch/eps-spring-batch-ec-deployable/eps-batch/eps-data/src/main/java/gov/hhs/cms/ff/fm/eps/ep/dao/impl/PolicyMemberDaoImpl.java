package gov.hhs.cms.ff.fm.eps.ep.dao.impl;

import gov.hhs.cms.ff.fm.eps.ep.dao.GenericEpsDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.PolicyMemberDao;
import gov.hhs.cms.ff.fm.eps.ep.enums.EpsEntityNames;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyMemberPO;

import java.util.List;

import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

/**
 * @author eps
 *
 */
public class PolicyMemberDaoImpl extends GenericEpsDao<PolicyMemberPO> implements PolicyMemberDao {
	
	/**
	 * 
	 */
	public PolicyMemberDaoImpl() {
		
	}


	/**
	 * @param simpleJdbcInsert
	 */
	public void setSimpleJdbcInsert(SimpleJdbcInsert simpleJdbcInsert) {
		
		super.simpleJdbcInsert = simpleJdbcInsert;
		simpleJdbcInsert.withTableName(EpsEntityNames.POLICY_MEMBER_ENTITY_NAME.getValue());

	}


	/* (non-Javadoc)
	 * @see gov.hhs.cms.ff.fm.eps.ep.dao.PolicyMemberDao#insertPolicyMemberList(java.util.List)
	 */
	@Override
	public void insertPolicyMembers(List<PolicyMemberPO> poList) {
		
		simpleJdbcInsert.executeBatch(getEpsBeanPropertySqlParameterSourceArrayFromList(poList));

	}
	


}
