/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.vo;


/**
 * @author zachary.hall
 *
 */
public class PolicyVersionSearchCriteriaVO {
	
	private String exchangePolicyId;
	private String subscriberStateCd;

	/**
	 * @return the exchangePolicyId
	 */
	public String getExchangePolicyId() {
		return exchangePolicyId;
	}

	/**
	 * @param exchangePolicyId the exchangePolicyId to set
	 */
	public void setExchangePolicyId(String exchangePolicyId) {
		this.exchangePolicyId = exchangePolicyId;
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

	/**
	 * @return
	 */
	public String getLogMessage() {

		String str = " exchangePolicyId=" + exchangePolicyId +
				", subscriberStateCd=" + subscriberStateCd;
		return str;
	}
	
}
