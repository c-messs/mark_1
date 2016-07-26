package gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob;

import gov.cms.dsh.bem.FileInformationType;
import gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentRequestDTO;
import gov.hhs.cms.ff.fm.eps.ep.EPSConstants;
import gov.hhs.cms.ff.fm.eps.ep.enums.ExchangeType;
import gov.hhs.cms.ff.fm.eps.ep.util.EpsDateUtils;

import java.io.FileInputStream;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
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

	String filePath;
	String fileInfoXml;
	String berXML;
	DateTime fileNameTimeStamp;
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
		boolean isFileInfoEndTag = false;
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
					if ("GroupSenderID".equals(xmlr.getLocalName())) {
						fileInformationType.setGroupSenderID(tagContent);
						break;
					}
					if ("GroupReceiverID".equals(xmlr.getLocalName())) {
						fileInformationType.setGroupReceiverID(tagContent);
						break;
					}
					if ("GroupControlNumber".equals(xmlr.getLocalName())) {
						fileInformationType.setGroupControlNumber(tagContent);
						break;
					}
					if ("GroupTimeStamp".equals(xmlr.getLocalName())) {
						fileInformationType.setGroupTimeStamp(EpsDateUtils.getXMLGregorianCalendar(tagContent));
						break;
					}					
					if ("VersionNumber".equals(xmlr.getLocalName())) {
						fileInformationType.setVersionNumber(tagContent);
						break;
					}
					break;
				default:
					;
				}
			}

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
			
		} finally {
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
		return berDTO;
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

