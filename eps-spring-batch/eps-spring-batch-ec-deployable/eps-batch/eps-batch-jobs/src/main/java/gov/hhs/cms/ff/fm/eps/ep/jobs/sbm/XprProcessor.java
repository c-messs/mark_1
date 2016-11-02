package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm;

import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.ERROR_DESC_INCORRECT_VALUE;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import com.accenture.foundation.common.exception.ApplicationException;
import com.accenture.foundation.common.exception.EnvironmentException;

import gov.cms.dsh.sbmi.FileInformationType;
import gov.cms.dsh.sbmi.FileInformationType.IssuerFileInformation;
import gov.cms.dsh.sbmi.PolicyType;
import gov.hhs.cms.ff.fm.eps.ep.SBMValidationRequest;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMErrorWarningCode;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMPolicyEnum;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMErrorDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMPolicyDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SBMFileCompositeDAO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.validation.SbmValidationService;
import gov.hhs.cms.ff.fm.eps.ep.sbm.validation.impl.SbmValidationUtil;

/**
 * This class is used by the Processor in the Processing step to convert the input xml
 * file to java objects using JAXB unmarshalling
 * 
 * @author christopher.vaka
 * 
 */
public class XprProcessor implements ItemProcessor<SBMPolicyDTO, SBMPolicyDTO> {

	private static final Logger LOG = LoggerFactory.getLogger(XprProcessor.class);

	private static final String INVALID_POLICY_ID = "INVALID9999999";
	
	private static Unmarshaller jaxbUnmarshaller;
	private static Unmarshaller fileInfoUnmarshaller;
	
	private SbmXMLValidator sbmXMLValidator;
	private SbmValidationService sbmValidationService;
	private SBMFileCompositeDAO sbmFileCompositeDao;
	private Long sbmFileProcSummaryId = 0L;
	private Long jobId;
	private String fileInfoXml;
	private String fileInfoXmlAsStr;
	private String fileInfoToValidate;

	PolicyType sbmPolicy =  new PolicyType();
	FileInformationType fileInfoType =  new FileInformationType();
	
	static {
        try {
        	JAXBContext jaxbContext = JAXBContext.newInstance(PolicyType.class);
			jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			
			JAXBContext fileInfoContext = JAXBContext.newInstance(FileInformationType.class);
			fileInfoUnmarshaller = fileInfoContext.createUnmarshaller();
          
        } catch (JAXBException ex) {
        	LOG.error("EPROD-24 EPS JAXB Unmarshalling error (XML data to PolicyType).", ex);
			throw new EnvironmentException("EPROD-24 EPS JAXB Unmarshalling error (XML data to PolicyType).", ex);
        }
    }

	/**
	 * Implementation of the process method from ItemProcessor interface. 
	 * The method generates the Java representation of the xml that is read and
	 * returns to be passed to the next processor/writer 
	 * 
	 * @throws Exception
	 */
	@Override
	public SBMPolicyDTO process(SBMPolicyDTO sbmPolicyDTO)
			throws Exception {
		
		try {
			if(sbmPolicyDTO.getBatchId() == null) {
				sbmPolicyDTO.setBatchId(jobId);
			}
			
			//Unmarshal FileInfo
			if (isNextFile(sbmPolicyDTO.getSbmFileProcSummaryId())) {
				
				fileInfoXml = sbmFileCompositeDao.getFileInfoTypeXml(sbmPolicyDTO.getSbmFileInfoId());
				
				fileInfoToValidate = fileInfoXml.substring(0, fileInfoXml.indexOf("</Enrollment>"));
				
				fileInfoXmlAsStr = fileInfoXml.substring(fileInfoXml.indexOf(SBMPolicyEnum.FILE_INFO.getElementNm()) - 1, 
						fileInfoXml.lastIndexOf(SBMPolicyEnum.FILE_INFO.getElementNm()) + SBMPolicyEnum.FILE_INFO.getElementNm().length() + 1);
				
				StringReader fileInfoStr = new StringReader(fileInfoXmlAsStr);
	
				JAXBElement<FileInformationType> rootFileInfo = (JAXBElement<FileInformationType>) fileInfoUnmarshaller.unmarshal(new StreamSource(fileInfoStr), FileInformationType.class);
				fileInfoType = rootFileInfo.getValue();
				
				String issuerId = getIssuerIdFromFileInfoXml(fileInfoXmlAsStr);
				
				if(fileInfoType.getIssuerFileInformation() == null) {
					fileInfoType.setIssuerFileInformation(new IssuerFileInformation());
				}
				fileInfoType.getIssuerFileInformation().setIssuerId(issuerId);
			}
			sbmPolicyDTO.setFileInfo(fileInfoType);
			sbmPolicyDTO.setFileInfoXml(fileInfoXmlAsStr);
			
			List<SBMErrorDTO> xprSchemaErrors = validateXprSchema(sbmPolicyDTO);
			
			if(CollectionUtils.isEmpty(xprSchemaErrors)) {
				sbmPolicyDTO = performUnmarshalValidate(xprSchemaErrors,sbmPolicyDTO);
				
			} 
			else {
				sbmPolicyDTO = performUnmarshalValidateNext(xprSchemaErrors,sbmPolicyDTO);
				
			}
			
		} catch (UnmarshalException ume) {
			//XML File Invalid
			throw new ApplicationException(SBMErrorWarningCode.SYSTEM_ERROR_999.getCode(), ume);
		}
		
		return sbmPolicyDTO;
	}

	private SBMPolicyDTO performUnmarshalValidateNext(List<SBMErrorDTO> xprSchemaErrors, SBMPolicyDTO sbmPolicyDTO) throws JAXBException {
		LOG.info("Schema errors found in Policy XPR");
		
		StringReader sbmPolicyStr = new StringReader(sbmPolicyDTO.getPolicyXml());
		
		try {
			JAXBElement<PolicyType> root = (JAXBElement<PolicyType>) jaxbUnmarshaller.unmarshal(new StreamSource(sbmPolicyStr), PolicyType.class);
			sbmPolicy = root.getValue();
			
			//Check for key fields
			if(SbmValidationUtil.hasRecordControlNumError(xprSchemaErrors)) {
				sbmPolicy.setRecordControlNumber(999999999);
			}
			
			if(SbmValidationUtil.hasQhpIdError(xprSchemaErrors)) {
				sbmPolicy.setQHPId(INVALID_POLICY_ID);
			}
			
			if(SbmValidationUtil.hasExchangePolicyIdError(xprSchemaErrors)) {
				sbmPolicy.setExchangeAssignedPolicyId(INVALID_POLICY_ID);
			}
			
			if(SbmValidationUtil.hasExchangeSubscriberIdError(xprSchemaErrors)) {
				sbmPolicy.setExchangeAssignedSubscriberId(INVALID_POLICY_ID);
			}
			
			sbmPolicyDTO.setPolicy(sbmPolicy);
			
		} catch (UnmarshalException ume) {
			//XML File Invalid
			LOG.info("Schema errors in Policy XPR");
		}
		
		sbmPolicyDTO.setSchemaErrorList(xprSchemaErrors);
		return sbmPolicyDTO;
	}

	private SBMPolicyDTO performUnmarshalValidate(List<SBMErrorDTO> xprSchemaErrors, SBMPolicyDTO sbmPolicyDTO) throws JAXBException {
		//Unmarshal Policy
		StringReader sbmPolicyStr = new StringReader(sbmPolicyDTO.getPolicyXml());

		JAXBElement<PolicyType> root = (JAXBElement<PolicyType>) jaxbUnmarshaller.unmarshal(new StreamSource(sbmPolicyStr), PolicyType.class);
		sbmPolicy = root.getValue();
		sbmPolicyDTO.setPolicy(sbmPolicy);
		
		if(SbmValidationUtil.isDuplicatePolicy(sbmPolicy.getExchangeAssignedPolicyId())) {
			
			sbmPolicyDTO.getErrorList().add(SbmValidationUtil.createErrorWarningLogDTO(
					"ExchangeAssignedPolicyId", SBMErrorWarningCode.ER_023.getCode(), ERROR_DESC_INCORRECT_VALUE));
			
			sbmPolicyDTO.setErrorFlag(true);
			
			LOG.info("Duplicate Policy {} ", sbmPolicy.getExchangeAssignedPolicyId());
			
			return sbmPolicyDTO;
		}
		
		//Call validation Service to Perform validations on the BEM
		SBMValidationRequest sbmValidationRequest = new SBMValidationRequest();
		sbmValidationRequest.setPolicyDTO(sbmPolicyDTO);
		
		LOG.info("Validating Policy XPR");
		
		sbmValidationService.validatePolicy(sbmValidationRequest);
	
		return sbmPolicyDTO;
	}

	/*
	 * Invoke Xpr Schema validator
	 */
    private List<SBMErrorDTO> validateXprSchema(SBMPolicyDTO sbmPolicyDTO) throws IOException {
    	LOG.info("Validating XPR Schema");
    	
    	if(StringUtils.isNotBlank(fileInfoToValidate)) {
    		
    		String xmlToValidate = fileInfoToValidate.concat(sbmPolicyDTO.getPolicyXml()).concat("</Enrollment>");
    		
    		return sbmXMLValidator.validateSchemaForXPR(xmlToValidate);
    	}
    	return Collections.emptyList();
	}
    

	private String getIssuerIdFromFileInfoXml(String fileInfoXmlAsStr) throws XMLStreamException {

		String issuerId = StringUtils.EMPTY;
		
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
					if (SBMPolicyEnum.FILE_INFO.getElementNm().equalsIgnoreCase(xmlr.getLocalName())) {
						isFileInfoEndTag = true;
						break;
					}
					if (SBMPolicyEnum.ISS_ID.getElementNm().equalsIgnoreCase(xmlr.getLocalName())) {
						issuerId = tagContent;
						isFileInfoEndTag = true;
						break;
					}
				default:
					break;
				}
			}			
		} finally {
			if(xmlr != null) {
				xmlr.close();
			}
		}
		return issuerId;
	}


	private boolean isNextFile(Long fileProcSummaryId) {
    	return !(sbmFileProcSummaryId.equals(fileProcSummaryId));
    }
    
	/**
	 * @param sbmValidationService the sbmValidationService to set
	 */
	public void setSbmValidationService(SbmValidationService sbmValidationService) {
		this.sbmValidationService = sbmValidationService;
	}


	/**
	 * @param sbmFileCompositeDao the sbmFileCompositeDao to set
	 */
	public void setSbmFileCompositeDao(SBMFileCompositeDAO sbmFileCompositeDao) {
		this.sbmFileCompositeDao = sbmFileCompositeDao;
	}

	/**
	 * @param sbmXMLValidator the sbmXMLValidator to set
	 */
	public void setSbmXMLValidator(SbmXMLValidator sbmXMLValidator) {
		this.sbmXMLValidator = sbmXMLValidator;
	}


	/**
	 * @param jobId the jobId to set
	 */
	public void setJobId(Long jobId) {
		this.jobId = jobId;
	}
}
