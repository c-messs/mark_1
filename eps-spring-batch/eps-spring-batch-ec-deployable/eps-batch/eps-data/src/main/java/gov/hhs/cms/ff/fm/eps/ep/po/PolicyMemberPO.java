package gov.hhs.cms.ff.fm.eps.ep.po;

/**
 * @author eps
 *
 */
public class PolicyMemberPO extends GenericPolicyMemberPO<PolicyMemberPO> {

	private Long policyVersionId;
	private String subscriberStateCd;


	public Long getPolicyVersionId() {
		return policyVersionId;
	}
	public void setPolicyVersionId(Long policyVersionId) {
		this.policyVersionId = policyVersionId;
	}
	/**
	 * @return the subscriberStateCd
	 */
	public String getSubscriberStateCd() {
		return subscriberStateCd;
	}
	/**
	 * @param subscriberStateCd the subscriberStateCd to set
	 */
	public void setSubscriberStateCd(String subscriberStateCd) {
		this.subscriberStateCd = subscriberStateCd;
	}

}