package gov.hhs.cms.ff.fm.eps.ep.po;

import java.math.BigDecimal;

public class SbmPolicyPremiumPO extends PolicyPremiumPO {
	
		private BigDecimal otherPaymentAmount1;
		private BigDecimal otherPaymentAmount2;
		/**
		 * @return the otherPaymentAmount1
		 */
		public BigDecimal getOtherPaymentAmount1() {
			return otherPaymentAmount1;
		}
		/**
		 * @param otherPaymentAmount1 the otherPaymentAmount1 to set
		 */
		public void setOtherPaymentAmount1(BigDecimal otherPaymentAmount1) {
			this.otherPaymentAmount1 = otherPaymentAmount1;
		}
		/**
		 * @return the otherPaymentAmount2
		 */
		public BigDecimal getOtherPaymentAmount2() {
			return otherPaymentAmount2;
		}
		/**
		 * @param otherPaymentAmount2 the otherPaymentAmount2 to set
		 */
		public void setOtherPaymentAmount2(BigDecimal otherPaymentAmount2) {
			this.otherPaymentAmount2 = otherPaymentAmount2;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((otherPaymentAmount1 == null) ? 0 : otherPaymentAmount1.hashCode());
			result = prime * result + ((otherPaymentAmount2 == null) ? 0 : otherPaymentAmount2.hashCode());
			return result;
		}
		/* 
		 * This methods uses "compareTo" for BigDecimals.
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			SbmPolicyPremiumPO other = (SbmPolicyPremiumPO) obj;
			if (otherPaymentAmount1 == null) {
				if (other.otherPaymentAmount1 != null)
					return false;
			} else if (other.otherPaymentAmount1 != null) {
				if (otherPaymentAmount1.compareTo(other.otherPaymentAmount1) != 0) {
					return false;
				} 
			} else {
				return false;
			}
			if (otherPaymentAmount2 == null) {
				if (other.otherPaymentAmount2 != null)
					return false;
			} else if (other.otherPaymentAmount2 != null) {
				if (otherPaymentAmount2.compareTo(other.otherPaymentAmount2) != 0) {
					return false;
				}
			} else {
				return false;
			}
			return true;
		}
		
		
	

}
