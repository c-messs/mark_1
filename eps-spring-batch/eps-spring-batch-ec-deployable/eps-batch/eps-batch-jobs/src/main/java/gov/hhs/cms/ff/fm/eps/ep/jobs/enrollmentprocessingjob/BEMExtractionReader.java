package gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob;

import gov.cms.dsh.bem.BenefitEnrollmentRequest;
import gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentRequestDTO;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.core.io.Resource;

import com.accenture.foundation.common.exception.EnvironmentException;

/**
 * This class is used as the reader for the BEM-extraction step of the batch job.
 * This reader read the 'BenefitEnrollmentRequest' xml files from configured directory and
 * marshals the BenefitEnrollmentMaintenance elements in the file to string and passes to
 * processor/writer.
 *
 * @author christopher.vaka
 * 
 * @param <T>
 */
public class BEMExtractionReader<T> extends StaxEventItemReader<Object>  {
	
	private static final Logger LOG = LoggerFactory.getLogger(BEMExtractionReader.class);
	private static Marshaller marshaller;
	
	private Resource aResource;
	private String sourceFileName="";
	
	static {
        try {
        	JAXBContext jaxbContext = JAXBContext.newInstance(String.class, BenefitEnrollmentRequest.class);
    		marshaller = jaxbContext.createMarshaller();
          
        } catch (JAXBException ex) {
        	LOG.error("EPROD-24 EPS JAXB Marshalling error (BEM to XML).", ex);
			throw new EnvironmentException("EPROD-24 EPS JAXB Marshalling error (BEM to XML).", ex);
        }
    }
	
	/**
	 * The Constructor
	 */
	public BEMExtractionReader(){
		super();
	}
	
	@Override
	public void setResource(Resource resource) {
		super.setResource(resource);
		this.aResource=resource;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public BenefitEnrollmentRequestDTO doRead() throws Exception {
    	
    	super.setStrict(false);
		T jaxbElem = (T) super.doRead();
		BenefitEnrollmentRequestDTO item = null;
		if (jaxbElem != null) {
			item = new BenefitEnrollmentRequestDTO();
		} else {
			return (BenefitEnrollmentRequestDTO) item;
		}
		
		StringWriter stringWriter = new StringWriter();
		marshaller.marshal(jaxbElem, stringWriter);
		item.setBer((BenefitEnrollmentRequest)jaxbElem);
		item.setBerXml(stringWriter.toString());
		item.setFileNm(aResource.getFilename());
		item.setInsertFileInfo(isNextFile());
		sourceFileName = aResource.getFilename();

		return item;
    }
    
    private boolean isNextFile() {
    	return !(aResource.getFilename().equals(sourceFileName));
    }
    
}
