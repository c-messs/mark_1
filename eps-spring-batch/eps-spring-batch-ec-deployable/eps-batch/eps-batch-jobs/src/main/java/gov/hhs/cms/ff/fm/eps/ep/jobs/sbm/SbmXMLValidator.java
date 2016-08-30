package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm;

import static gov.hhs.cms.ff.fm.eps.ep.enums.SBMPolicyEnum.EX_ASSIGN_MEM_ID;
import static gov.hhs.cms.ff.fm.eps.ep.enums.SBMPolicyEnum.MEMBER_INFO;
import static gov.hhs.cms.ff.fm.eps.ep.enums.SBMPolicyEnum.REC_CTRL_NUM;
import static gov.hhs.cms.ff.fm.eps.ep.enums.SBMSchemaValidationRuleEnum.CVC_COMPLEX_TYPE_24A;
import static gov.hhs.cms.ff.fm.eps.ep.enums.SBMSchemaValidationRuleEnum.CVC_COMPLEX_TYPE_24B;
import static gov.hhs.cms.ff.fm.eps.ep.enums.SBMSchemaValidationRuleEnum.CVC_COMPLEX_TYPE_24D;
import static gov.hhs.cms.ff.fm.eps.ep.enums.SBMSchemaValidationRuleEnum.CVC_LENGTH_VALID;
import static gov.hhs.cms.ff.fm.eps.ep.enums.SBMSchemaValidationRuleEnum.CVC_MAXINCLUSIVE_VALID;
import static gov.hhs.cms.ff.fm.eps.ep.enums.SBMSchemaValidationRuleEnum.CVC_MAXLENGTH_VALID;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import com.accenture.foundation.common.exception.EnvironmentException;

import gov.cms.dsh.sbmi.Enrollment;
import gov.cms.dsh.sbmi.FileInformationType;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMErrorWarningCode;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMPolicyEnum;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMSchemaValidationRuleEnum;
import gov.hhs.cms.ff.fm.eps.ep.jobs.CommonUtil;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMErrorDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMScemaErrorsDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SbmXMLSchemaError;
import gov.hhs.cms.ff.fm.eps.ep.sbm.XMLSchemaError;

public class SbmXMLValidator {
	
	private static final Logger LOG = LoggerFactory.getLogger(SbmXMLValidator.class);
	private static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
	//private static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
	private static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
	
	private String xsdFilePath;
	
	private File xsdFile;
	
	private Map<String, String> validXMLElementMap = new HashMap<>();
	private Map<String, String> xmlParentTagsMap = new HashMap<>();
	private Map<String, String> xmlOptionalElementsMap = new HashMap<>();
	private Map<String, String> xmlElementAmountsMap = new HashMap<>();
	
	private Unmarshaller sbmiFileInfoUnmarshaller;
	
	private Marshaller enrollmentMarshaller;
	
	
	public SbmXMLValidator() {
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
	 * Checks if is valid xml.
	 *
	 * @param file the file
	 * @return true, if is valid xml
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ParserConfigurationException the parser configuration exception
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
		}
		return valid;
	}
	
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
	
		
	private SBMScemaErrorsDTO validateXMLForFileInfo(File sbmiFile) throws SAXException, IOException, ParserConfigurationException  {

		InputSource is = new InputSource(new FileInputStream(sbmiFile));
		SBMScemaErrorsDTO dto = new SBMScemaErrorsDTO();
				
		List<SbmXMLSchemaError> schemaErrors = new ArrayList<>();	
		Map<String, Integer> elementCount = new HashMap<>();
		
		SAXParserFactory parserFactory = SAXParserFactory.newInstance();
		parserFactory.setValidating(true);
		parserFactory.setNamespaceAware(true);		
		SAXParser parser = parserFactory.newSAXParser();	
		parser.setProperty(JAXP_SCHEMA_LANGUAGE, XMLConstants.W3C_XML_SCHEMA_NS_URI);
		parser.setProperty(JAXP_SCHEMA_SOURCE, xsdFile);
		XMLReader reader = parser.getXMLReader();
				
		SBMCustomHandler handler = new SBMCustomHandler() {
			
			private boolean isFileInfoTag = false;
			private boolean isInsidePolicyTag = false;
			
			@Override
			public void startElement(String uri, String localName, String qName, Attributes attributes) {
				LOG.debug("Start: uri {},	localName {},	qName {},	attributes {}", uri, localName, qName, attributes);
								
				schemaErrors.addAll(getSchemaErrorList());				
				getSchemaErrorList().clear();				
				setCurrentElement(localName);
				
				if(SBMPolicyEnum.FILE_INFO.getElementNm().equalsIgnoreCase(localName)) {
					isFileInfoTag = true;
				}
				else if(SBMPolicyEnum.POLICY.getElementNm().equalsIgnoreCase(localName)) {
					isInsidePolicyTag = true;
					elementCount.put(localName, 1);  // set to 1 if any policy is found
				}
				
				if(isFileInfoTag) {
					if(elementCount.get(localName) == null) {
						elementCount.put(localName, 0);
					}				
					
					elementCount.put(localName, elementCount.get(localName) + 1);	
				}
			}
			
			@Override
			public void endElement(String uri, String localName, String qName) throws SAXException {
				setCurrentElement(null);
				LOG.debug("END: uri {},	localName {},	qName {}", uri, localName, qName);
				if(SBMPolicyEnum.FILE_INFO.getElementNm().equalsIgnoreCase(localName)) {
					isFileInfoTag = false;
				}
				else if(SBMPolicyEnum.POLICY.getElementNm().equalsIgnoreCase(localName)) {
					isInsidePolicyTag = false;
				}
				
			}
			
			@Override
			public void fatalError(SAXParseException exception) {				
				super.fatalError(exception);
			}
			
			@Override
			public void warning(SAXParseException exception) {
				super.warning(exception);
			}
			
			@Override
			public void error(SAXParseException exception) {
				
				if(isInsidePolicyTag) {
					//Skip all errors with in policy except 'RecordControlNumber' is missing and invalid xml element
					if(exception.getMessage().contains(CVC_COMPLEX_TYPE_24A.getValue())) {	
						String errorMessage = exception.getMessage();
						String missingElement = errorMessage.substring(errorMessage.lastIndexOf("\":") + 2, errorMessage.indexOf("}"));
						//get element found
						String subStr = errorMessage.substring(errorMessage.indexOf(": ") + 1, errorMessage.indexOf("'."));
						String elementFound = subStr.substring(subStr.indexOf("'") + 1);
						if(elementFound.indexOf(":") != -1) {
							elementFound = elementFound.substring(elementFound.indexOf(":") + 1);
						}
						
						//create error if element is not valid, this is file level error.
						if( ! validXMLElementMap.containsKey(elementFound)) {
							super.error(exception);
						}
						else if(CommonUtil.isStringMatched(REC_CTRL_NUM.getElementNm(), missingElement)) {
							super.error(exception);
						}
					}									
					
					return;
				}
				
				super.error(exception);
			}			
			
			@Override
			public void endDocument() throws SAXException {
				schemaErrors.addAll(getSchemaErrorList());				
				getSchemaErrorList().clear();		
			}
			
		};
		
		reader.setContentHandler(handler);
		
		//set custom error handler to collect all errors				
		reader.setErrorHandler(handler);	
		reader.parse(is);	
		
		LOG.info("elementCounts: {}", elementCount);
		LOG.info("schemaErrors Count: {};  {}", schemaErrors.size(), schemaErrors);
		
		dto.getSchemaErrorsList().addAll(schemaErrors);
				
		return dto;
	}
	
	private SBMScemaErrorsDTO validateXMLforXPR(String sbmiXMLSnippet) throws SAXException, IOException, ParserConfigurationException  {

		boolean errorsExists = false;
		InputSource is = new InputSource(new StringReader(sbmiXMLSnippet));
		SBMScemaErrorsDTO dto = new SBMScemaErrorsDTO();
		
		List<SbmXMLSchemaError> schemaErrors = new ArrayList<>();	
		
		SAXParserFactory parserFactory = SAXParserFactory.newInstance();
		parserFactory.setValidating(true);
		parserFactory.setNamespaceAware(true);		
		SAXParser parser = parserFactory.newSAXParser();	
		parser.setProperty(JAXP_SCHEMA_LANGUAGE, XMLConstants.W3C_XML_SCHEMA_NS_URI);
		parser.setProperty(JAXP_SCHEMA_SOURCE, xsdFile);
		XMLReader reader = parser.getXMLReader();
		
		//
		try {
			reader.setErrorHandler(new ErrorHandler() {
				
				@Override
				public void warning(SAXParseException exception) throws SAXException {
					throw exception;					
				}
				
				@Override
				public void fatalError(SAXParseException exception) throws SAXException {
					throw exception;					
				}
				
				@Override
				public void error(SAXParseException exception) throws SAXException {
					throw exception;					
				}
			});
			
			reader.parse(is);
		}
		catch(Exception e) {
			errorsExists = true;
			LOG.info("Errors exists :{}", e.getMessage());
			LOG.info("Parsing for schema errors");
		}
		
		if(!errorsExists) {
			LOG.info("Schema passed.");
			return dto;
		}
		
		is = new InputSource(new StringReader(sbmiXMLSnippet));
		SBMCustomHandler handler = new SBMCustomHandler() {
			
			@Override
			public void startElement(String uri, String localName, String qName, Attributes attributes) {
				LOG.debug("Start: uri {},	localName {},	qName {},	attributes {}", uri, localName, qName, attributes);
								
				schemaErrors.addAll(getSchemaErrorList());				
				getSchemaErrorList().clear();				
				setCurrentElement(localName);
				
				if(EX_ASSIGN_MEM_ID.getElementNm().equalsIgnoreCase(localName)) {
					setExchangeAssignedMemberIdTag(true);
				}
						
			}
			
			@Override
			public void characters(char[] ch, int start, int length) throws SAXException {
				if(isExchangeAssignedMemberIdTag()) {
					setExchangeAssignedMemberId(new String(ch, start, length));
					LOG.debug("exchangeAssignedMemberId: {}", getExchangeAssignedMemberId());
				}
			}
			
			@Override
			public void endElement(String uri, String localName, String qName) throws SAXException {
				setCurrentElement(null);
				LOG.debug("END: uri {},	localName {},	qName {}", uri, localName, qName);
				
				
				if(EX_ASSIGN_MEM_ID.getElementNm().equalsIgnoreCase(localName)) {
					setExchangeAssignedMemberIdTag(false);
				}
				
				if(MEMBER_INFO.getElementNm().equalsIgnoreCase(localName)) {
					setExchangeAssignedMemberId(null);
				}
			}
			
			@Override
			public void endDocument() throws SAXException {
				schemaErrors.addAll(getSchemaErrorList());				
				getSchemaErrorList().clear();		
			}
			
		};
		
		reader.setContentHandler(handler);
		
		//set custom error handler to collect all errors				
		reader.setErrorHandler(handler);	
		reader.parse(is);	
		
		LOG.info("schemaErrors Count: {};  {}", schemaErrors.size(), schemaErrors);
		
		dto.getSchemaErrorsList().addAll(schemaErrors);
		
		return dto;
	}
	
	/**
	 * Validates sbmiXMLSnippet with XSD and returns schema errors.
	 * @param sbmTransMsgId
	 * @param sbmiXMLSnippet
	 * @return
	 * @throws IOException
	 */
	public List<SBMErrorDTO> validateSchemaForXPR(String sbmiXMLSnippet) throws IOException {
		
		List<SBMErrorDTO> sbmFileErrorList = new ArrayList<>();		
		
		try {
			SBMScemaErrorsDTO dto = validateXMLforXPR(sbmiXMLSnippet); 
			sbmFileErrorList.addAll(parseSchemaErrors(dto));
		} catch (SAXException e) {			
			LOG.info("Unable to perform schema validation: " + e.getMessage());
			// create error with generic error code
			sbmFileErrorList.add(SbmHelper.createErrorLog(null, SBMErrorWarningCode.ER_003.getCode(), e.getMessage()));
		} catch (ParserConfigurationException e) {			
			LOG.info("Unable to perform schema validation: " + e.getMessage());
			// create error with generic error code
			sbmFileErrorList.add(SbmHelper.createErrorLog(null, SBMErrorWarningCode.ER_003.getCode(), e.getMessage()));
		}
								
		return sbmFileErrorList;		
	}
	
	public List<SBMErrorDTO> validateSchemaForFileInfo(Long sbmFileInfoId, File sbmiFile) throws IOException {
		
		List<SBMErrorDTO> sbmFileErrorList = new ArrayList<>();		
		
		try {
			SBMScemaErrorsDTO dto = validateXMLForFileInfo(sbmiFile); 
			sbmFileErrorList.addAll(parseSchemaErrors(dto));
		} catch (SAXException e) {			
			LOG.info("Unable to perform schema validation: " + e.getMessage());
			// create error with generic error code
			sbmFileErrorList.add(SbmHelper.createErrorLog(null, SBMErrorWarningCode.ER_998.getCode(), e.getMessage()));
		} catch (ParserConfigurationException e) {			
			LOG.info("Unable to perform schema validation: " + e.getMessage());
			// create error with generic error code
			sbmFileErrorList.add(SbmHelper.createErrorLog(null, SBMErrorWarningCode.ER_998.getCode(), e.getMessage()));
		}
				
		return sbmFileErrorList;		
	}
	
	/*
	 * Parsing schema errors is same for file level or record level and this can be separated in future if need arises.
	 */
	public List<SBMErrorDTO> parseSchemaErrors(SBMScemaErrorsDTO dto) {
		
		List<SbmXMLSchemaError> schemaErrors = dto.getSchemaErrorsList();
		List<SBMErrorDTO> sbmErrors = new ArrayList<>();
		List<SbmXMLSchemaError> errorsNotIdentified = new ArrayList<>();
		Map<String, String> errorElementMap = new HashMap<>();
		
		String errorMessage;
		String errorType;
				
		for(SbmXMLSchemaError error: schemaErrors) {
			
			errorMessage = error.getException().getMessage();
			try {
				errorType = errorMessage.substring(0, errorMessage.indexOf(": "));
			}
			catch(IndexOutOfBoundsException e) {
				errorType = null;
			}
			
			if(errorType == null) {
				sbmErrors.add(SbmHelper.createErrorLog(null, SBMErrorWarningCode.ER_998.getCode()));
			}	
			else if(errorType.equalsIgnoreCase(SBMSchemaValidationRuleEnum.CVC_TYPE_313.getValue())) {		
				//Error Pattern:  cvc-type.3.1.3: The value 'xyz' of element 'p:MonthlyTotalPremiumAmount' is not valid.
				
				//In some instances like empty value error element is not correct so parse the errorMessage for error element
				String element = StringUtils.substringBetween(errorMessage, "element '", "' is");			
				if(StringUtils.contains(element, ":")) {
					element = StringUtils.substringAfter(element, ":");
				}
				LOG.info("elementname parsed from errorMessag:{}",  element);
				LOG.info("from Object error.getElementLocalName():{}, error.getqName():{}",  error.getElementLocalName(), error.getqName());
				if( ! CommonUtil.isStringMatched(error.getElementLocalName(), element)) {
					LOG.info("Both string not matched");
					error.setElementLocalName(element);
				}
												
				sbmErrors.add(SbmHelper.createErrorWithMemId(element, SBMErrorWarningCode.ER_004.getCode(), error.getExchangeAssignedMemberId()));
				errorElementMap.put(element, element);
				continue;
			}
			else if(errorType.equalsIgnoreCase(CVC_COMPLEX_TYPE_24A.getValue())) {				
				try {
					//Error Pattern: cvc-complex-type.2.4.a: Invalid content was found starting with element 'p:IssuerFileInformation'. One of '{"http://sbmi.dsh.cms.gov":CoverageYear}' is expected.
					
					String missingElement = errorMessage.substring(errorMessage.lastIndexOf("\":") + 2, errorMessage.indexOf("}"));
					
					//get element found
					String subStr = errorMessage.substring(errorMessage.indexOf(": ") + 1, errorMessage.indexOf("'."));
					String elementFound = subStr.substring(subStr.indexOf("'") + 1);
					if(elementFound.indexOf(":") != -1) {  //remove namespace prefix if exists
						elementFound = elementFound.substring(elementFound.indexOf(":") + 1);
					}
					
					//create error if element is not valid, this is file level error.
					if( ! validXMLElementMap.containsKey(elementFound)) {
						sbmErrors.add(SbmHelper.createErrorLog(elementFound, SBMErrorWarningCode.ER_014.getCode()));
						continue;
					}
										
					//create error - 'Element missing or out of sequence'	
					if( ! xmlOptionalElementsMap.containsKey(missingElement)) {
						//create error for the expected element
						String errorCode = SBMErrorWarningCode.ER_003.getCode();
						if(xmlParentTagsMap.containsKey(missingElement)) {
							errorCode = SBMErrorWarningCode.ER_001.getCode();
						}

						sbmErrors.add(SbmHelper.createErrorWithMemId(missingElement, errorCode, error.getExchangeAssignedMemberId()));
						errorElementMap.put(missingElement, missingElement);
					}
					else {
						//create error for the element found
						String errorCode = SBMErrorWarningCode.ER_003.getCode();
						if(xmlParentTagsMap.containsKey(elementFound)) {
							errorCode = SBMErrorWarningCode.ER_001.getCode();
						}

						sbmErrors.add(SbmHelper.createErrorWithMemId(elementFound, errorCode, error.getExchangeAssignedMemberId()));
						errorElementMap.put(elementFound, elementFound);
					}
					
					LOG.info("subStr:{}	elementFound:{}",  subStr, elementFound);
				}
				catch(IndexOutOfBoundsException e) {
					LOG.info("Unexpected Exception occured while extracting element name from schema error message: {}",  errorMessage);
					LOG.info("Creating Error - ER-998 for this schema error");
					SBMErrorDTO errorLog = SbmHelper.createErrorLog(null, SBMErrorWarningCode.ER_998.getCode(), errorMessage);
					sbmErrors.add(errorLog);
				}
				
				continue;
			}
			else if(errorType.equalsIgnoreCase(CVC_COMPLEX_TYPE_24B.getValue())) {
				//Error Pattern: Schema 'error': cvc-complex-type.2.4.b: The content of element 'p:Enrollment' is not complete. One of '{"http://sbmi.dsh.cms.gov":Policy}' is expected.				
				String missingElement = errorMessage.substring(errorMessage.lastIndexOf("\":") + 2, errorMessage.indexOf("}"));

				//create error - 'Required Element missing'
				String errorCode = SBMErrorWarningCode.ER_003.getCode();
				if(xmlParentTagsMap.containsKey(missingElement)) {
					errorCode = SBMErrorWarningCode.ER_001.getCode();
				}
				
				sbmErrors.add(SbmHelper.createErrorWithMemId(missingElement, errorCode, error.getExchangeAssignedMemberId()));
				errorElementMap.put(missingElement, missingElement);
			}	
			else if(errorType.equalsIgnoreCase(CVC_COMPLEX_TYPE_24D.getValue())) {
				
				try {
					//Error Pattern: cvc-complex-type.2.4.d: Invalid content was found starting with element 'p:ProratedAmount'. No child element is expected at this point.
					
					//get element found
					String subStr = errorMessage.substring(errorMessage.indexOf(": ") + 1, errorMessage.indexOf("'."));
					String elementFound = subStr.substring(subStr.indexOf("'") + 1);
					if(elementFound.indexOf(":") != -1) {  //remove namespace prefix if exists
						elementFound = elementFound.substring(elementFound.indexOf(":") + 1);
					}
					
					//create error if element is not valid, this is file level error.
					if( ! validXMLElementMap.containsKey(elementFound)) {
						sbmErrors.add(SbmHelper.createErrorLog(elementFound, SBMErrorWarningCode.ER_014.getCode()));
						continue;
					}
										
					//create error - 'Element missing or out of sequence'
					String errorCode = SBMErrorWarningCode.ER_003.getCode();
					if(xmlParentTagsMap.containsKey(elementFound)) {
						errorCode = SBMErrorWarningCode.ER_001.getCode();
					}
										
					sbmErrors.add(SbmHelper.createErrorWithMemId(elementFound, errorCode, error.getExchangeAssignedMemberId()));
					errorElementMap.put(elementFound, elementFound);
					
					LOG.info("subStr:{}	elementFound:{}",  subStr, elementFound);
				}
				catch(IndexOutOfBoundsException e) {
					LOG.info("Unexpected Exception occured while extracting element name from schema error message: {}",  errorMessage);
					LOG.info("Creating Error - ER-998 for this schema error");
					SBMErrorDTO errorLog = SbmHelper.createErrorLog(null, SBMErrorWarningCode.ER_998.getCode(), errorMessage);
					sbmErrors.add(errorLog);
				}
				
				continue;
			}
			else if(errorType.equalsIgnoreCase(CVC_MAXINCLUSIVE_VALID.getValue())) {
				//error pattern- Schema 'error': cvc-maxInclusive-valid: Value '199999999.99' is not facet-valid with respect to maxInclusive '99999999.99' for type 'AmountSimpleType'.
				if(xmlElementAmountsMap.containsKey(error.getElementLocalName())) {
					String providedValue = errorMessage.substring(errorMessage.lastIndexOf("Value '") + 7, errorMessage.indexOf("' is"));
					sbmErrors.add(SbmHelper.createErrorLog(error.getElementLocalName(), SBMErrorWarningCode.ER_009.getCode(), providedValue, SBMConstants.ERRORMSG_AMOUNT));	
					errorElementMap.put(error.getElementLocalName(), error.getElementLocalName());
				}
			}
			else if(errorType.equalsIgnoreCase(CVC_LENGTH_VALID.getValue())) {
				// Error pattern: cvc-length-valid: Value '123456789' with length = '9' is not facet-valid with respect to length '5' for type '#AnonType_IssuerIdIssuerFileInformationFileInformationType'.
				String providedValue = StringUtils.substringBetween(errorMessage, "Value '", "' with");
				if(StringUtils.equalsIgnoreCase(SBMPolicyEnum.ISS_ID.getElementNm(), error.getElementLocalName()) && StringUtils.length(providedValue) > 5) {
					sbmErrors.add(SbmHelper.createErrorWithMemId(error.getElementLocalName(), SBMErrorWarningCode.ER_006.getCode(), error.getExchangeAssignedMemberId()));
					errorElementMap.put(error.getElementLocalName(), error.getElementLocalName());
				}
				continue;
			}  
			else if(errorType.equalsIgnoreCase(CVC_MAXLENGTH_VALID.getValue())) {
				// Error pattern: cvc-maxLength-valid: Value '111111111111111111111112345678' with length = '130' is not facet-valid with respect to maxLength '10' for type '#AnonType_IssuerFileSetIdIssuerFileSetIssuerFileInformationFileInformationType'.
				sbmErrors.add(SbmHelper.createErrorWithMemId(error.getElementLocalName(), SBMErrorWarningCode.ER_006.getCode(), error.getExchangeAssignedMemberId()));
				errorElementMap.put(error.getElementLocalName(), error.getElementLocalName());
				continue;
			}
			else {
				errorsNotIdentified.add(error);
			}		
			
		}	
		
		LOG.info("Errors not identified (before filtering):{}", errorsNotIdentified);
		
		if( sbmErrors.isEmpty() && CollectionUtils.isNotEmpty(errorsNotIdentified)) {
			// create ER-998 error if no error created on the element			
			for( XMLSchemaError err : errorsNotIdentified) {
				SBMErrorDTO errorLog = SbmHelper.createErrorLog(err.getElementLocalName(), SBMErrorWarningCode.ER_998.getCode(), err.getException().getMessage());
				sbmErrors.add(errorLog);
				LOG.info("ER-998 is created: {}", errorLog);
			}		
		}
		
		return sbmErrors;
	}
		

	/**
	 * @return the xsdFilePath
	 */
	public String getXsdFilePath() {
		return xsdFilePath;
	}

	/**
	 * @param xsdFilePath the xsdFilePath to set
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public void setXsdFilePath(String xsdFilePath) throws ParserConfigurationException, SAXException, IOException {
		LOG.info("XSD file path: {}", xsdFilePath);
		this.xsdFilePath = xsdFilePath;
		
		BufferedInputStream schemaFileStream = new BufferedInputStream(this.getClass().getResourceAsStream(xsdFilePath));
		File localXSDFile = new File("SBMPolicy_copy.xsd");
		FileUtils.copyInputStreamToFile(schemaFileStream, localXSDFile);	
				
		this.xsdFile = localXSDFile;
		LOG.info("xsdFile: {}", xsdFile);	
		loadXMLElementsFromXSD();
	}
	
	private void loadXMLElementsFromXSD() throws ParserConfigurationException, SAXException, IOException {
		
		LOG.info("Extracting xml elements from xsd");
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		Document doc = docBuilder.parse(xsdFile); 
		NodeList list = doc.getElementsByTagName("xs:element"); 

		for(int i = 0 ; i < list.getLength(); i++)  {
			Element element = (Element)list.item(i);          
			if(element.hasAttributes()) {
				String elementName = element.getAttribute("name"); 
				validXMLElementMap.put(elementName, elementName);
//				LOG.debug("xml elements: {}, minOccurs:{}, minOccurs:{}", elementName, CommonUtil.isStringMatched(element.getAttribute("minOccurs"), "0") , element.getAttribute("minOccurs"));
				if(CommonUtil.isStringMatched(element.getAttribute("minOccurs"), "0")) {
					xmlOptionalElementsMap.put(elementName, elementName);
				}
			}
		}
		
		LOG.info("validXMLElementMap: {}", validXMLElementMap);
		LOG.info("xmlOptionalElementsMap: {}", xmlOptionalElementsMap);
	}
	
	
	
}

class SBMCustomHandler extends DefaultHandler {
	
	private static final Logger LOG = LoggerFactory.getLogger(SBMCustomHandler.class);
	
	private List<SbmXMLSchemaError> schemaErrorList = new ArrayList<>();
	private String currentElement;
	private boolean isExchangeAssignedMemberIdTag = false;
	private String exchangeAssignedMemberId; 
	
	@Override
	public void startDocument() {				
		schemaErrorList.clear();
		currentElement = null;
	}
					
	@Override
	public void warning(SAXParseException exception)  {
		LOG.debug("Schema 'warning': " + exception.getMessage());
		//TODO should this be logged error?
		//schemaErrorList.add(new XMLSchemaError(exception, currentElement));
	}
	
	@Override
	public void fatalError(SAXParseException exception) {
		LOG.debug("Schema 'fatalError': " + exception.getMessage());
		schemaErrorList.add(new SbmXMLSchemaError(exception, currentElement, exchangeAssignedMemberId));		
	}
	
	@Override
	public void error(SAXParseException exception) {
		LOG.debug("Schema 'error': " + exception.getMessage());				
		schemaErrorList.add(new SbmXMLSchemaError(exception, currentElement, exchangeAssignedMemberId));			
	}
	
	/**
	 * @return the currentElement
	 */
	public String getCurrentElement() {
		return currentElement;
	}

	/**
	 * @param currentElement the currentElement to set
	 */
	public void setCurrentElement(String currentElement) {
		this.currentElement = currentElement;
	}

	/**
	 * @return the schemaErrorList
	 */
	public List<SbmXMLSchemaError> getSchemaErrorList() {
		return schemaErrorList;
	}

	/**
	 * @return the isExchangeAssignedMemberIdTag
	 */
	public boolean isExchangeAssignedMemberIdTag() {
		return isExchangeAssignedMemberIdTag;
	}

	/**
	 * @param isExchangeAssignedMemberIdTag the isExchangeAssignedMemberIdTag to set
	 */
	public void setExchangeAssignedMemberIdTag(boolean isExchangeAssignedMemberIdTag) {
		this.isExchangeAssignedMemberIdTag = isExchangeAssignedMemberIdTag;
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