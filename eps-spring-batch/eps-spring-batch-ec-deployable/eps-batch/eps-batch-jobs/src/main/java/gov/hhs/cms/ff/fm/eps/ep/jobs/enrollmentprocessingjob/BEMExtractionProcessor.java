package gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob;

import gov.cms.dsh.bem.FileInformationType;
import gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentRequestDTO;
import gov.hhs.cms.ff.fm.eps.ep.EPSConstants;
import gov.hhs.cms.ff.fm.eps.ep.enums.ExchangeType;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDateTime;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.item.ItemProcessor;

import com.accenture.foundation.common.exception.EnvironmentException;

/**
 * @author eps
 *
 */
public class BEMExtractionProcessor implements ItemProcessor<BenefitEnrollmentRequestDTO, BenefitEnrollmentRequestDTO> {

	private static final Logger LOG = LoggerFactory.getLogger(BEMExtractionProcessor.class);

	private Marshaller marshaller;
	private JAXBIntrospector introspector;
    private boolean isFileInfoEndTag = false;
	String filePath;
	String fileInfoXml;
	String berXML;
	LocalDateTime fileNameTimeStamp;
	String exchangeType;
	String stateCode;
	JobParameters jobParameters;
	String source;
	String qhpId;

	/**
	 * Constructor
	 */
	public BEMExtractionProcessor() {
		try {
			JAXBContext jc = JAXBContext.newInstance(String.class, FileInformationType.class);
			introspector = jc.createJAXBIntrospector();
			marshaller = jc.createMarshaller();
		} catch (JAXBException ex) {
			LOG.error("EPROD-24 EPS JAXB Marshalling error (XML data to BEM).", ex);
			throw new EnvironmentException("EPROD-24 EPS JAXB Marshalling error (XML data to BEM).", ex);
		}
	}

	/**
	 * @param filePath
	 */
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	@Override
	public BenefitEnrollmentRequestDTO process(BenefitEnrollmentRequestDTO berDTO) throws Exception {

		if (!berDTO.isInsertFileInfo()) {
			berDTO.setFileInfoXml(fileInfoXml);
			berDTO.setBerXml(berXML);
			berDTO.setFileNmDateTime(fileNameTimeStamp);
			berDTO.setExchangeTypeCd(exchangeType);
			berDTO.setSubscriberStateCd(stateCode);
			berDTO.setPlanId(qhpId);
			return berDTO;
		}
		String file = berDTO.getFileNm();
		FileInformationType fileInformationType = new FileInformationType();

		XMLInputFactory xmlif = XMLInputFactory.newInstance();
		FileInputStream fisxml = new FileInputStream(filePath+file);
		berXML = IOUtils.toString(fisxml, "UTF-8");	
		
		FileInputStream fis = new FileInputStream(filePath+file);
		String tagContent = null;
		XMLStreamReader xmlr=null;
		try {
			xmlr = xmlif.createXMLStreamReader(fis);

			int event = xmlr.getEventType();
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
					fillInFileInfo(fileInformationType,tagContent,xmlr);
					break;
				}
			}
			return  performUpdateBenefit(berDTO,fileInformationType); 
		} finally {
			performCloseResource(fisxml,fis,xmlr);
			
		}
	}

	private void performCloseResource(FileInputStream fisxml, FileInputStream fis, XMLStreamReader xmlr)
			throws IOException,XMLStreamException{
		if(fisxml != null) {
			fisxml.close();
		}
		if(fis != null) {
			fis.close();
		}
		if(xmlr != null) {
			xmlr.close();
		}
		
	}

	private BenefitEnrollmentRequestDTO performUpdateBenefit(
			BenefitEnrollmentRequestDTO berDTO, FileInformationType fileInformationType) throws JAXBException{
		StringWriter fileInfoStr = new StringWriter();
        
		if (introspector.getElementName(fileInformationType) == null) {
			JAXBElement<FileInformationType> jaxbElement = 
					new JAXBElement<FileInformationType>(new QName("FileInformation"), FileInformationType.class, fileInformationType);
			marshaller.marshal(jaxbElement, fileInfoStr);
		} else {
			marshaller.marshal(fileInformationType, fileInfoStr);
		}
		fileInfoXml = fileInfoStr.toString();

		berDTO.setFileInformation(fileInformationType);
		berDTO.setFileInfoXml(fileInfoXml);
        berDTO.setBerXml(berXML);
		berDTO.setSubscriberStateCd(setStateCode(berDTO));

		qhpId = fileInformationType.getGroupSenderID();
		berDTO.setPlanId(qhpId);
		LOG.debug("qhpId: "+qhpId);

		if(StringUtils.isNotBlank(source) && source.equalsIgnoreCase(EPSConstants.JOBPARAMETER_SOURCE_FFM)) {
			exchangeType = ExchangeType.FFM.getValue();
			berDTO.setExchangeTypeCd(exchangeType);
		} 
		return berDTO;
	
	}

	private void fillInFileInfo(FileInformationType fileInformationType, String tagContent, XMLStreamReader xmlr) {
		if ("GroupSenderID".equals(xmlr.getLocalName())) {
			fileInformationType.setGroupSenderID(tagContent);
		}	
		else if ("GroupReceiverID".equals(xmlr.getLocalName())) {
			fileInformationType.setGroupReceiverID(tagContent);
			
		}
		else if ("GroupControlNumber".equals(xmlr.getLocalName())) {
			fileInformationType.setGroupControlNumber(tagContent);
			
		}
		else if ("GroupTimeStamp".equals(xmlr.getLocalName())) {
			fileInformationType.setGroupTimeStamp(DateTimeUtil.getXMLGregorianCalendar(tagContent));
			
		}					
		else if ("VersionNumber".equals(xmlr.getLocalName())) {
			fileInformationType.setVersionNumber(tagContent);
		}
		
	}

	/*
	 * Get State code from BER
	 *  
	 * @param bemDTO
	 * @return
	 */
	private String setStateCode(BenefitEnrollmentRequestDTO bemDTO) {

		stateCode = bemDTO.getFileInformation().getGroupReceiverID().substring(0, 2);
		return stateCode;
	}

	/**
	 * @param jobParameters the jobParameters to set
	 */
	public void setJobParameters(JobParameters jobParameters) {
		this.jobParameters = jobParameters;
		this.source = jobParameters.getString(EPSConstants.JOBPARAMETER_KEY_SOURCE);
	}
}

