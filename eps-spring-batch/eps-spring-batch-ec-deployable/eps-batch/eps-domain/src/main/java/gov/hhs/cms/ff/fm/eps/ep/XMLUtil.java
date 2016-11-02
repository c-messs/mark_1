/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep;

import gov.cms.dsh.bem.FileInformationType;

import java.io.StringReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * @author girish.padmanabhan
 *
 */
public class XMLUtil {
	
	/**
	 * @param fileInfoXml
	 * @return
	 * @throws XMLStreamException
	 */
	public static FileInformationType getFileInfoTypeFromXml(String fileInfoXml) throws XMLStreamException {
		
		FileInformationType fileInformationType = new FileInformationType();
		XMLInputFactory xmlif = XMLInputFactory.newInstance();
		
		String tagContent = null;
		XMLStreamReader xmlr=null;
		
		try {
			xmlr = xmlif.createXMLStreamReader(new StringReader(fileInfoXml));
			int event = xmlr.getEventType();
			boolean isFileInfoEndTag = false;
			while (xmlr.hasNext() && !isFileInfoEndTag) {
				event = xmlr.next();
				switch (event) {
				case XMLStreamConstants.CHARACTERS:
					tagContent = xmlr.getText().trim();
					break;
				case XMLStreamConstants.END_ELEMENT:
					if ("FileInformation".equals(xmlr.getLocalName())) {
						isFileInfoEndTag = true;
						break;
					}				
					if(localNameFound(tagContent,xmlr,fileInformationType)){
						break;
					}
					
				}
			}			
			
		} finally {
			if(xmlr != null) {
				xmlr.close();
			}
		}
		return fileInformationType;
	}

	private static boolean localNameFound(String tagContent, XMLStreamReader xmlr,
			FileInformationType fileInformationType) {
		if ("GroupSenderID".equals(xmlr.getLocalName())) {
			fileInformationType.setGroupSenderID(tagContent);
			return true;
		}
		if ("GroupReceiverID".equals(xmlr.getLocalName())) {
			fileInformationType.setGroupReceiverID(tagContent);
			return true;
		}
		if ("GroupControlNumber".equals(xmlr.getLocalName())) {
			fileInformationType.setGroupControlNumber(tagContent);
			return true;
		}				
		if ("VersionNumber".equals(xmlr.getLocalName())) {
			fileInformationType.setVersionNumber(tagContent);
			return true;
		}
	
		return false;
	}

}
