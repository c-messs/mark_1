package gov.hhs.cms.ff.fm.eps.dispatcher;

import org.joda.time.DateTime;

/**
 * The Class PhysicalDocument.
 *
 * @author pankaj.samayam
 */
public class PhysicalDocument {

	/** The physical document identifier. */
	private long physicalDocumentIdentifier;
	
	/** The physical document date time. */
	private DateTime physicalDocumentDateTime;
	
	/** The physical document type cd. */
	private String physicalDocumentTypeCd;
	
	/** The server environment type cd. */
	private String serverEnvironmentTypeCd;
	
	/** The physical document invalid cnd ind. */
	private String physicalDocumentInvalidCndInd;
	
	/** The issuer hios identifier. */
	private Integer issuerHiosIdentifier;
	
	/** The state postal code. */
	private String statePostalCode;
	
	/** The physical document apprvd ind. */
	private String physicalDocumentApprvdInd;
	
	/** The physical dcmnt dsptch type cd. */
	private String physicalDcmntDsptchTypeCd;
	
	/** The target eft application type cd. */
	private String targetEFTApplicationTypeCd;
	
	/** The physical document file name. */
	private String physicalDocumentFileName;

	/** The physical document byte array. */
	private byte[] physicalDocumentByteArray;

	/**
	 * Gets the physical document file name.
	 *
	 * @return the physicalDocumentFileName
	 */
	public String getPhysicalDocumentFileName() {
		return physicalDocumentFileName;
	}

	/**
	 * Sets the physical document file name.
	 *
	 * @param physicalDocumentFileName            the physicalDocumentFileName to set
	 */
	public void setPhysicalDocumentFileName(String physicalDocumentFileName) {
		this.physicalDocumentFileName = physicalDocumentFileName;
	}

	/**
	 * Gets the physical document identifier.
	 *
	 * @return the physicalDocumentIdentifier
	 */
	public long getPhysicalDocumentIdentifier() {
		return physicalDocumentIdentifier;
	}

	/**
	 * Sets the physical document identifier.
	 *
	 * @param physicalDocumentIdentifier            the physicalDocumentIdentifier to set
	 */
	public void setPhysicalDocumentIdentifier(long physicalDocumentIdentifier) {
		this.physicalDocumentIdentifier = physicalDocumentIdentifier;
	}

	/**
	 * Gets the physical document date time.
	 *
	 * @return the physicalDocumentDateTime
	 */
	public DateTime getPhysicalDocumentDateTime() {
		return physicalDocumentDateTime;
	}

	/**
	 * Sets the physical document date time.
	 *
	 * @param physicalDocumentDateTime            the physicalDocumentDateTime to set
	 */
	public void setPhysicalDocumentDateTime(DateTime physicalDocumentDateTime) {
		this.physicalDocumentDateTime = physicalDocumentDateTime;
	}

	/**
	 * Gets the physical document type cd.
	 *
	 * @return the physicalDocumentTypeCd
	 */
	public String getPhysicalDocumentTypeCd() {
		return physicalDocumentTypeCd;
	}

	/**
	 * Sets the physical document type cd.
	 *
	 * @param physicalDocumentTypeCd            the physicalDocumentTypeCd to set
	 */
	public void setPhysicalDocumentTypeCd(String physicalDocumentTypeCd) {
		this.physicalDocumentTypeCd = physicalDocumentTypeCd;
	}

	/**
	 * Gets the server environment type cd.
	 *
	 * @return the serverEnvironmentTypeCd
	 */
	public String getServerEnvironmentTypeCd() {
		return serverEnvironmentTypeCd;
	}

	/**
	 * Sets the server environment type cd.
	 *
	 * @param serverEnvironmentTypeCd            the serverEnvironmentTypeCd to set
	 */
	public void setServerEnvironmentTypeCd(String serverEnvironmentTypeCd) {
		this.serverEnvironmentTypeCd = serverEnvironmentTypeCd;
	}

	/**
	 * Gets the physical document invalid cnd ind.
	 *
	 * @return the physicalDocumentInvalidCndInd
	 */
	public String getPhysicalDocumentInvalidCndInd() {
		return physicalDocumentInvalidCndInd;
	}

	/**
	 * Sets the physical document invalid cnd ind.
	 *
	 * @param physicalDocumentInvalidCndInd            the physicalDocumentInvalidCndInd to set
	 */
	public void setPhysicalDocumentInvalidCndInd(
			String physicalDocumentInvalidCndInd) {
		this.physicalDocumentInvalidCndInd = physicalDocumentInvalidCndInd;
	}

	/**
	 * Gets the issuer hios identifier.
	 *
	 * @return the issuerHiosIdentifier
	 */
	public Integer getIssuerHiosIdentifier() {
		return issuerHiosIdentifier;
	}

	/**
	 * Sets the issuer hios identifier.
	 *
	 * @param issuerHiosIdentifier            the issuerHiosIdentifier to set
	 */
	public void setIssuerHiosIdentifier(Integer issuerHiosIdentifier) {
		this.issuerHiosIdentifier = issuerHiosIdentifier;
	}

	/**
	 * Gets the state postal code.
	 *
	 * @return the statePostalCode
	 */
	public String getStatePostalCode() {
		return statePostalCode;
	}

	/**
	 * Sets the state postal code.
	 *
	 * @param statePostalCode            the statePostalCode to set
	 */
	public void setStatePostalCode(String statePostalCode) {
		this.statePostalCode = statePostalCode;
	}

	/**
	 * Gets the physical document apprvd ind.
	 *
	 * @return the physicalDocumentApprvdInd
	 */
	public String getPhysicalDocumentApprvdInd() {
		return physicalDocumentApprvdInd;
	}

	/**
	 * Sets the physical document apprvd ind.
	 *
	 * @param physicalDocumentApprvdInd            the physicalDocumentApprvdInd to set
	 */
	public void setPhysicalDocumentApprvdInd(String physicalDocumentApprvdInd) {
		this.physicalDocumentApprvdInd = physicalDocumentApprvdInd;
	}

	/**
	 * Gets the physical dcmnt dsptch type cd.
	 *
	 * @return the physicalDcmntDsptchTypeCd
	 */
	public String getPhysicalDcmntDsptchTypeCd() {
		return physicalDcmntDsptchTypeCd;
	}

	/**
	 * Sets the physical dcmnt dsptch type cd.
	 *
	 * @param physicalDcmntDsptchTypeCd            the physicalDcmntDsptchTypeCd to set
	 */
	public void setPhysicalDcmntDsptchTypeCd(String physicalDcmntDsptchTypeCd) {
		this.physicalDcmntDsptchTypeCd = physicalDcmntDsptchTypeCd;
	}

	/**
	 * Gets the target eft application type cd.
	 *
	 * @return the targetEFTApplicationTypeCd
	 */
	public String getTargetEFTApplicationTypeCd() {
		return targetEFTApplicationTypeCd;
	}

	/**
	 * Sets the target eft application type cd.
	 *
	 * @param targetEFTApplicationTypeCd            the targetEFTApplicationTypeCd to set
	 */
	public void setTargetEFTApplicationTypeCd(String targetEFTApplicationTypeCd) {
		this.targetEFTApplicationTypeCd = targetEFTApplicationTypeCd;
	}

	/**
	 * Gets the physical document byte array.
	 *
	 * @return the physicalDocumentByteArray
	 */
	public byte[] getPhysicalDocumentByteArray() {
		return physicalDocumentByteArray.clone();
	}

	/**
	 * Sets the physical document byte array.
	 *
	 * @param physicalDocumentByteArray            the physicalDocumentByteArray to set
	 */
	public void setPhysicalDocumentByteArray(byte[] physicalDocumentByteArray) {
		this.physicalDocumentByteArray = physicalDocumentByteArray.clone();
	}
}
