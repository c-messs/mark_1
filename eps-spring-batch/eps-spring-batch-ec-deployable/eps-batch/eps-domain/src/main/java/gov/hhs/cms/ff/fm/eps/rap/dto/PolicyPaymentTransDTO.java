package gov.hhs.cms.ff.fm.eps.rap.dto;

import gov.hhs.cms.ff.fm.eps.rap.domain.PolicyPaymentTrans;

import org.joda.time.DateTime;

/**
 * 
 * @author rajesh.talanki
 *
 */
public class PolicyPaymentTransDTO extends PolicyPaymentTrans {
	
	private static final long serialVersionUID = 1L;	
	private String paymentProcStatusTypeCd;	
	private DateTime statusDateTime;
	private boolean updateStatusRec;
	private boolean endOfTransactionReached;
		
	/**
	 * @return the paymentProcStatusTypeCd
	 */
	public String getPaymentProcStatusTypeCd() {
		return paymentProcStatusTypeCd;
	}

	/**
	 * @param paymentProcStatusTypeCd the paymentProcStatusTypeCd to set
	 */
	public void setPaymentProcStatusTypeCd(String paymentProcStatusTypeCd) {
		this.paymentProcStatusTypeCd = paymentProcStatusTypeCd;
	}

	/**
	 * @return the statusDateTime
	 */
	public DateTime getStatusDateTime() {
          return statusDateTime;
	}

	/**
	 * @param statusDateTime the statusDateTime to set
	 */
	public void setStatusDateTime(DateTime statusDateTime) {
		this.statusDateTime = statusDateTime;
	}

	/**
	 * @return the updateStatusRec
	 */
	public boolean isUpdateStatusRec() {
		return updateStatusRec;
	}

	/**
	 * @param updateStatusRec the updateStatusRec to set
	 */
	public void setUpdateStatusRec(boolean updateStatusRec) {
		this.updateStatusRec = updateStatusRec;
	}

	/**
	 * @return the endOfTransactionReached
	 */
	public boolean isEndOfTransactionReached() {
		return endOfTransactionReached;
	}

	/**
	 * @param endOfTransactionReached the endOfTransactionReached to set
	 */
	public void setEndOfTransactionReached(boolean endOfTransactionReached) {
		this.endOfTransactionReached = endOfTransactionReached;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return super.toString() 
				+"PolicyPaymentTransDTO [paymentProcStatusTypeCd="
				+ paymentProcStatusTypeCd + ", statusDateTime="
				+ statusDateTime 
				+ ", updateStatusRec=" + updateStatusRec
				+ ", endOfTransactionReached=" + endOfTransactionReached + "]";
	}

}
