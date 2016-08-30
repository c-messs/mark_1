package gov.hhs.cms.ff.fm.eps.dispatcher;

/**
 * The Class DispatchedPhysicalDocument.
 *
 * @author pankaj.samayam
 */
public class DispatchedPhysicalDocument {

	/** The physical document identifier. */
	private long physicalDocumentIdentifier;
	
	/** The dispached physical dcmnt file nm. */
	private String dispachedPhysicalDcmntFileNm;
	
	/** The failed dispatch ind. */
	private String failedDispatchInd;
	
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
	 * @param physicalDocumentIdentifier the physicalDocumentIdentifier to set
	 */
	public void setPhysicalDocumentIdentifier(long physicalDocumentIdentifier) {
		this.physicalDocumentIdentifier = physicalDocumentIdentifier;
	}
	
	/**
	 * Gets the dispached physical dcmnt file nm.
	 *
	 * @return the dispachedPhysicalDcmntFileNm
	 */
	public String getDispachedPhysicalDcmntFileNm() {
		return dispachedPhysicalDcmntFileNm;
	}
	
	/**
	 * Sets the dispached physical dcmnt file nm.
	 *
	 * @param dispachedPhysicalDcmntFileNm the dispachedPhysicalDcmntFileNm to set
	 */
	public void setDispachedPhysicalDcmntFileNm(String dispachedPhysicalDcmntFileNm) {
		this.dispachedPhysicalDcmntFileNm = dispachedPhysicalDcmntFileNm;
	}
	
	/**
	 * Gets the failed dispatch ind.
	 *
	 * @return the failedDispatchInd
	 */
	public String getFailedDispatchInd() {
		return failedDispatchInd;
	}
	
	/**
	 * Sets the failed dispatch ind.
	 *
	 * @param failedDispatchInd the failedDispatchInd to set
	 */
	public void setFailedDispatchInd(String failedDispatchInd) {
		this.failedDispatchInd = failedDispatchInd;
	}
}
