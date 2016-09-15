package gov.hhs.cms.ff.fm.eps.ep.sbm;

import org.xml.sax.SAXParseException;

/**
 * Holds XML Schema Error
 *
 */
public class XMLSchemaError {
	
	private SAXParseException exception;
	private String elementLocalName;
	private String qName;
	
	/**
	 * Constructor
	 * @param exception
	 * @param elementLocalName
	 * @param qName
	 */
	public XMLSchemaError(SAXParseException exception, String elementLocalName, String qName) {
		super();
		this.exception = exception;
		this.elementLocalName = elementLocalName;
		this.qName = qName;
	}
	
	/**
	 * Constructor
	 * @param exception
	 * @param elementLocalName
	 */
	public XMLSchemaError(SAXParseException exception, String elementLocalName) {
		super();
		this.exception = exception;
		this.elementLocalName = elementLocalName;
	}

	/**
	 * @return the elementLocalName
	 */
	public String getElementLocalName() {
		return elementLocalName;
	}
	/**
	 * @param elementLocalName the elementLocalName to set
	 */
	public void setElementLocalName(String elementLocalName) {
		this.elementLocalName = elementLocalName;
	}
	/**
	 * @return the qName
	 */
	public String getqName() {
		return qName;
	}
	/**
	 * @param qName the qName to set
	 */
	public void setqName(String qName) {
		this.qName = qName;
	}

	/**
	 * @return the exception
	 */
	public SAXParseException getException() {
		return exception;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("XMLSchemaError [exception=").append(exception).append(", elementLocalName=")
				.append(elementLocalName).append(", qName=").append(qName).append("]");
		return builder.toString();
	}
	
}
