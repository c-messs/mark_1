package gov.hhs.cms.ff.fm.eps.ep.sbm;

import org.xml.sax.SAXParseException;

public class SbmXMLSchemaError extends XMLSchemaError {
		
	private String exchangeAssignedMemberId; 

	public SbmXMLSchemaError(SAXParseException exception, String elementLocalName, String exchgAssignedMemberId) {
		super(exception, elementLocalName);
		this.exchangeAssignedMemberId = exchgAssignedMemberId;
	}
	
	/**
	 * @return the exchangeAssignedMemberId
	 */
	public String getExchangeAssignedMemberId() {
		return exchangeAssignedMemberId;
	}

	/**
	 * @param exchangeAssignedMemberId the exchangeAssignedMemberId to set
	 */
	public void setExchangeAssignedMemberId(String exchangeAssignedMemberId) {
		this.exchangeAssignedMemberId = exchangeAssignedMemberId;
	}
	

}
