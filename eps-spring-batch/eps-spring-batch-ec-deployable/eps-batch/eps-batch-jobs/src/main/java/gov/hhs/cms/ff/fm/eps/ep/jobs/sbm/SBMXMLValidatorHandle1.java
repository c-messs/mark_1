package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm;
import java.io.File;

import java.io.IOException;

import java.util.Map;



import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;



import com.sun.org.apache.xerces.internal.impl.io.MalformedByteSequenceException;

import gov.hhs.cms.ff.fm.eps.ep.jobs.CommonUtil;

/**
 * Validates input XML files.
 * 
 */

public class SBMXMLValidatorHandle1 {
	private static final Logger LOG = LoggerFactory.getLogger(SBMXMLValidatorHandle1.class);

	/**
	 * Loads XML Elements
	 * @param localXSDFile
	 * @param validXMLElementMap
	 * @param xmlOptionalElementsMap
	 * @throws ParserConfigurationException 
	 * @throws SAXException 
	 * @throws IOException
	 * @return void
	 */
    public void loadXMLElementsFromXSD(File localXSDFile, Map<String, String> validXMLElementMap, Map<String, String> xmlOptionalElementsMap) throws ParserConfigurationException, SAXException, IOException {
		
		LOG.info("Extracting xml elements from xsd");
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		Document doc = docBuilder.parse(localXSDFile); 
		NodeList list = doc.getElementsByTagName("xs:element"); 

		for(int i = 0 ; i < list.getLength(); i++)  {
			Element element = (Element)list.item(i);          
			if(element.hasAttributes()) {
				String elementName = element.getAttribute("name"); 
				validXMLElementMap.put(elementName, elementName);

				if(CommonUtil.isStringMatched(element.getAttribute("minOccurs"), "0")) {
					xmlOptionalElementsMap.put(elementName, elementName);
				}
			}
		}
		
		LOG.info("validXMLElementMap: {}", validXMLElementMap);
		LOG.info("xmlOptionalElementsMap: {}", xmlOptionalElementsMap);
	}
}
