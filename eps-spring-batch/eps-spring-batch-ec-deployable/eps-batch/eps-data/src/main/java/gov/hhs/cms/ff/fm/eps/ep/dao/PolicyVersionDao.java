package gov.hhs.cms.ff.fm.eps.ep.dao;

import gov.hhs.cms.ff.fm.eps.ep.po.PolicyVersionPO;
import gov.hhs.cms.ff.fm.eps.ep.vo.PolicyVersionSearchCriteriaVO;

import java.util.List;

import org.joda.time.DateTime;


/**
 * @author eps
 *
 */
public interface PolicyVersionDao {

	/**
	 * @param PolicyVersionId
	 * @param subscriberStateCd
	 * @return
	 */
	public PolicyVersionPO getPolicyVersionById(Long PolicyVersionId, String subscriberStateCd);
	
	/**
	 * Updates the previous PolicyVersion MAINTENANCEENDDATETIME from HIGHDATE to 1 millisecond less
	 * than the PolicyVersion's to be inserted MAINTENANCESTARTDATETIME using the previousPolicyVersionId
	 * set in the PolicyVersionPO.  The new PolicyVersion is then inserted.
	 * 
	 * @param pv
	 * @return
	 */
	public Long insertPolicyVersion(PolicyVersionPO pv);
	

	/**
	 * @param criteria
	 * @return
	 */
	public List<PolicyVersionPO> matchPolicyVersionByPolicyIdAndStateCd(PolicyVersionSearchCriteriaVO criteria);
	
	
	/**
	 * 
	 * @return
	 */
	public DateTime getLatestPolicyMaintenanceStartDateTime();
	
}
