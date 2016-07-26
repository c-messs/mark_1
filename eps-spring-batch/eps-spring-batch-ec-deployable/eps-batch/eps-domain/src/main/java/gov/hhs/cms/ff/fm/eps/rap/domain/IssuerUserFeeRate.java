package gov.hhs.cms.ff.fm.eps.rap.domain;

import java.io.Serializable;
import java.math.BigDecimal;

import org.joda.time.DateTime;

/**
 * This class represents the IssuerUserFeeRate table
 * 
 * @author girish.padmanabhan
 *
 */
public class IssuerUserFeeRate implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String issuerUfStateCd;
	private DateTime issuerUfStartDate;
	private String insrncAplctnTypeCd;
	private BigDecimal issuerUfPercent;
	private String issuerUfCoverageYear;
	private BigDecimal issuerUfFlatrate;
	private DateTime issuerUfEndDate;
	
	/**
	 * @return the issuerUfStateCd
	 */
	public String getIssuerUfStateCd() {
		return issuerUfStateCd;
	}
	
	/**
	 * @param issuerUfStateCd the issuerUfStateCd to set
	 */
	public void setIssuerUfStateCd(String issuerUfStateCd) {
		this.issuerUfStateCd = issuerUfStateCd;
	}

	/**
	 * @return the issuerUfStartDate
	 */
	public DateTime getIssuerUfStartDate() {
		return issuerUfStartDate;
	}

	/**
	 * @param issuerUfStartDate the issuerUfStartDate to set
	 */
	public void setIssuerUfStartDate(DateTime issuerUfStartDate) {
		this.issuerUfStartDate = issuerUfStartDate;
	}

	/**
	 * @return the insrncAplctnTypeCd
	 */
	public String getInsrncAplctnTypeCd() {
		return insrncAplctnTypeCd;
	}

	/**
	 * @param insrncAplctnTypeCd the insrncAplctnTypeCd to set
	 */
	public void setInsrncAplctnTypeCd(String insrncAplctnTypeCd) {
		this.insrncAplctnTypeCd = insrncAplctnTypeCd;
	}

	/**
	 * @return the issuerUfPercent
	 */
	public BigDecimal getIssuerUfPercent() {
		return issuerUfPercent;
	}

	/**
	 * @param issuerUfPercent the issuerUfPercent to set
	 */
	public void setIssuerUfPercent(BigDecimal issuerUfPercent) {
		this.issuerUfPercent = issuerUfPercent;
	}

	/**
	 * @return the issuerUfCoverageYear
	 */
	public String getIssuerUfCoverageYear() {
		return issuerUfCoverageYear;
	}

	/**
	 * @param issuerUfCoverageYear the issuerUfCoverageYear to set
	 */
	public void setIssuerUfCoverageYear(String issuerUfCoverageYear) {
		this.issuerUfCoverageYear = issuerUfCoverageYear;
	}

	/**
	 * @return the issuerUfFlatrate
	 */
	public BigDecimal getIssuerUfFlatrate() {
		return issuerUfFlatrate;
	}

	/**
	 * @param issuerUfFlatrate the issuerUfFlatrate to set
	 */
	public void setIssuerUfFlatrate(BigDecimal issuerUfFlatrate) {
		this.issuerUfFlatrate = issuerUfFlatrate;
	}

	/**
	 * @return the issuerUfEndDate
	 */
	public DateTime getIssuerUfEndDate() {
		return issuerUfEndDate;
	}

	/**
	 * @param issuerUfEndDate the issuerUfEndDate to set
	 */
	public void setIssuerUfEndDate(DateTime issuerUfEndDate) {
		this.issuerUfEndDate = issuerUfEndDate;
	}


}
