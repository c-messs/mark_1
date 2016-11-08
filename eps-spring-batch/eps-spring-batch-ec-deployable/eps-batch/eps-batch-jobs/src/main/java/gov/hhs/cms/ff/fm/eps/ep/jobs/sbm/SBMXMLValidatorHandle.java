package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.accenture.foundation.common.exception.EnvironmentException;

import com.sun.org.apache.xerces.internal.impl.io.MalformedByteSequenceException;
import gov.cms.dsh.sbmi.Enrollment;
import gov.cms.dsh.sbmi.FileInformationType;

import gov.hhs.cms.ff.fm.eps.ep.enums.SBMPolicyEnum;


/**
 * Validates input XML files.
 * 
 */

public class SBMXMLValidatorHandle {
	private static final Logger LOG = LoggerFactory.getLogger(SBMXMLValidatorHandle.class);
	private static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
	
	private static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
	
	private File xsdFile;
	
	private Map<String, String> xmlParentTagsMap = new HashMap<>();
	private Map<String, String> xmlElementAmountsMap = new HashMap<>();
	
	private Unmarshaller sbmiFileInfoUnmarshaller;
	
	private Marshaller enrollmentMarshaller;
	
	private static final String SCHEMA_MSG = "Unable to perform schema validation: ";
	/**
	 * Initiates JAXB
	 *
	 */
	public SBMXMLValidatorHandle() {
		
		try {
			JAXBContext sbmiFileInfoContext = JAXBContext.newInstance(FileInformationType.class);
			sbmiFileInfoUnmarshaller = sbmiFileInfoContext.createUnmarshaller();
			enrollmentMarshaller =  JAXBContext.newInstance(Enrollment.class).createMarshaller();
			 // format the XML output
			enrollmentMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		} catch (JAXBException e) {
			LOG.error("JAXB error.", e);
			throw new EnvironmentException("JAXB error.", e);
		}
		
		loadXMLTagsHashmap();
	}
	
	private void loadXMLTagsHashmap() {
		
		xmlParentTagsMap.put(SBMPolicyEnum.FILE_INFO.getElementNm(), SBMPolicyEnum.FILE_INFO.getElementNm());
		xmlParentTagsMap.put(SBMPolicyEnum.POLICY.getElementNm(), SBMPolicyEnum.POLICY.getElementNm());
		xmlParentTagsMap.put(SBMPolicyEnum.ISS_FILE_INFO.getElementNm(), SBMPolicyEnum.ISS_FILE_INFO.getElementNm());
		xmlParentTagsMap.put(SBMPolicyEnum.ISSUER_FILE_SET.getElementNm(), SBMPolicyEnum.ISSUER_FILE_SET.getElementNm());
		xmlParentTagsMap.put(SBMPolicyEnum.MEMBER_INFO.getElementNm(), SBMPolicyEnum.MEMBER_INFO.getElementNm());
		xmlParentTagsMap.put(SBMPolicyEnum.FINANCIAL_INFO.getElementNm(), SBMPolicyEnum.FINANCIAL_INFO.getElementNm());
		xmlParentTagsMap.put(SBMPolicyEnum.PRORATED_AMOUNTS.getElementNm(), SBMPolicyEnum.PRORATED_AMOUNTS.getElementNm());
		
		//amouts elements
		xmlElementAmountsMap.put(SBMPolicyEnum.TPA.getElementNm(), SBMPolicyEnum.TPA.getElementNm());
		xmlElementAmountsMap.put(SBMPolicyEnum.TIRA.getElementNm(), SBMPolicyEnum.TIRA.getElementNm());
		xmlElementAmountsMap.put(SBMPolicyEnum.APTC.getElementNm(), SBMPolicyEnum.APTC.getElementNm());
		xmlElementAmountsMap.put(SBMPolicyEnum.OPA1.getElementNm(), SBMPolicyEnum.OPA1.getElementNm());
		xmlElementAmountsMap.put(SBMPolicyEnum.OPA2.getElementNm(), SBMPolicyEnum.OPA2.getElementNm());
		xmlElementAmountsMap.put(SBMPolicyEnum.CSR.getElementNm(), SBMPolicyEnum.CSR.getElementNm());		
		xmlElementAmountsMap.put(SBMPolicyEnum.PRO_TPA.getElementNm(), SBMPolicyEnum.PRO_TPA.getElementNm());
		xmlElementAmountsMap.put(SBMPolicyEnum.PRO_APTC.getElementNm(), SBMPolicyEnum.PRO_APTC.getElementNm());
		xmlElementAmountsMap.put(SBMPolicyEnum.PRO_CSR.getElementNm(), SBMPolicyEnum.PRO_CSR.getElementNm());
				
	}

	/**
	 * Validates FileInformation element.
	 * @param sbmFileInfoId
	 * @param  sbmiFile
	 * @return sbmFileErrorList
	 * @throws IOException
	 */
	
	public boolean isValidXML(File file) throws IOException, ParserConfigurationException {

		URL xmlFileURL = file.toURI().toURL();
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(false);
		factory.setNamespaceAware(true);
		InputStream inputStream = xmlFileURL.openStream();

		boolean valid = false;
		try {
			SAXParser parser = factory.newSAXParser();
			XMLReader reader = parser.getXMLReader();
			reader.parse(new InputSource(inputStream));
			LOG.info(xmlFileURL + " is valid xml.");
			valid = true;
		} catch (SAXException e) {
			LOG.warn("Invalid XML:  "+ xmlFileURL + "\n\tReason: " + e.getMessage());
			valid = false;
			inputStream.close();
		} catch(@SuppressWarnings("restriction") MalformedByteSequenceException me) {
			LOG.warn("Malformed XML:  "+ xmlFileURL + "\n\tReason: " + me.getMessage());
			valid = false;
			inputStream.close();
		}
		return valid;
	}
	
	/**
	 * Returns XML string with FileInformation wrapped in Enrollment tag
	 * @param sbmiFile
	 * @throws JAXBException, IOException, XMLStreamException
	 * @return fileInfoType
	 */
	
	public FileInformationType unmarshallSBMIFileInfo(File sbmiFile) throws JAXBException, IOException, XMLStreamException {
		
		URL xmlFileURL = sbmiFile.toURI().toURL();
		InputStream inputStream = xmlFileURL.openStream();		
		
		XMLInputFactory xif = XMLInputFactory.newInstance();
        StreamSource xml = new StreamSource(inputStream);
        XMLStreamReader xsr = xif.createXMLStreamReader(xml);
        // Advance to the specific element.
        while(xsr.hasNext()) {
            if(xsr.isStartElement() && SBMPolicyEnum.FILE_INFO.getElementNm().equals(xsr.getLocalName())) {
                break;
            }
            xsr.next();
         }

        // Unmarshal from the XMLStreamReader that has been advanced
        FileInformationType fileInfoType = sbmiFileInfoUnmarshaller.unmarshal(xsr, FileInformationType.class).getValue();
        inputStream.close();
        
		return  fileInfoType;
	}
	
	/**
	 * Returns XML string with FileInformation wrapped in Enrollment tag
	 * @param fileInfoType
	 * @return
	 * @throws JAXBException
	 */
	public String marshallFileInfo(FileInformationType fileInfoType) throws JAXBException {
		
		StringWriter writer = new StringWriter();	
		
		Enrollment enrollment = new Enrollment(); // root element
		enrollment.setFileInformation(fileInfoType);
		
		enrollmentMarshaller.marshal(enrollment, writer);
		LOG.info("Marshalled FileInformationType to xml string with enrollment root tag:\n{}", writer.toString());
		return writer.toString();
	}
}
