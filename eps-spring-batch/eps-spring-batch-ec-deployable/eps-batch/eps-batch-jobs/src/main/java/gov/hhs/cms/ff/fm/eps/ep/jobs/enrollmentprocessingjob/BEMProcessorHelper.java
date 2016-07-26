package gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob;

import gov.cms.dsh.bem.BenefitEnrollmentMaintenanceType;
import gov.cms.dsh.bem.FileInformationType;
import gov.cms.dsh.bem.ObjectFactory;
import gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentMaintenanceDTO;
import gov.hhs.cms.ff.fm.eps.ep.EPSValidationRequest;
import gov.hhs.cms.ff.fm.eps.ep.XMLUtil;
import gov.hhs.cms.ff.fm.eps.ep.enums.EProdEnum;
import gov.hhs.cms.ff.fm.eps.ep.validation.EPSValidationService;

import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.accenture.foundation.common.exception.ApplicationException;
import com.accenture.foundation.common.exception.EnvironmentException;

/**
 * This class is used by the Processor in the Processing step to convert the input xml
 * file to java objects using JAXB unmarshelling.
 *
 * @author christopher.vaka
 */
public class BEMProcessorHelper {
	
	/** The Constant LOG. */
	private static final Logger LOG = LoggerFactory.getLogger(BEMProcessorHelper.class);
	
	/** The jaxb unmarshaller. */
	private Unmarshaller jaxbUnmarshaller;
	
	/** The eps validation service. */
	private EPSValidationService epsValidationService;
	
	/** The bem. */
	BenefitEnrollmentMaintenanceType bem =  new BenefitEnrollmentMaintenanceType();
	
	/**
	 * Constructor - Creates the unmarshaller
	 */
	public BEMProcessorHelper() {
        try {
        	JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
			jaxbUnmarshaller = jaxbContext.createUnmarshaller();
          
        } catch (JAXBException ex) {
        	LOG.error("EPROD-24 EPS JAXB Unmarshalling error (XML data to BEM).", ex);
			throw new EnvironmentException("EPROD-24 EPS JAXB Unmarshalling error (XML data to BEM).", ex);
        }
    }

	/**
	 * Implementation of the process method from ItemProcessor interface. 
	 * The method generates the Java representation of the xml that is read and
	 * returns to be passed to the next processor/writer 
	 *
	 * @param bemDTO the bem dto
	 * @return bemDTO
	 * @throws JAXBException 
	 * @throws XMLStreamException 
	 */
	@SuppressWarnings("unchecked")
	public BenefitEnrollmentMaintenanceDTO process(BenefitEnrollmentMaintenanceDTO bemDTO) throws JAXBException, XMLStreamException {
		
		try {

			StringReader sr = new StringReader(bemDTO.getBemXml());

			JAXBElement<BenefitEnrollmentMaintenanceType> root = (JAXBElement<BenefitEnrollmentMaintenanceType>) jaxbUnmarshaller.unmarshal(sr);
			bem = root.getValue();
			bemDTO.setBem(bem);

			FileInformationType fileInfotype = XMLUtil.getFileInfoTypeFromXml(bemDTO.getFileInfoXml());
			bemDTO.setFileInformation(fileInfotype);

			//Call validation Service to Perform validations on the BEM
			EPSValidationRequest epsValidationRequest = new EPSValidationRequest();
			epsValidationRequest.setBenefitEnrollmentMaintenance(bemDTO);
			
			epsValidationService.validateBEM(epsValidationRequest);
			
		} catch (NullPointerException npe) {
			//EPROD-22 Null Pointer Exception
			throw new ApplicationException(EProdEnum.EPROD_22.getCode(), npe);
			
		} catch (UnmarshalException ume) {
			//EPROD-03 XML File Invalid
			throw new ApplicationException(EProdEnum.EPROD_03.getCode(), ume);
		}
		
		return bemDTO;
	}

	/**
	 * Sets the eps validation service.
	 *
	 * @param epsValidationService the epsValidationService to set
	 */
	public void setEpsValidationService(EPSValidationService epsValidationService) {
		this.epsValidationService = epsValidationService;
	}

}
