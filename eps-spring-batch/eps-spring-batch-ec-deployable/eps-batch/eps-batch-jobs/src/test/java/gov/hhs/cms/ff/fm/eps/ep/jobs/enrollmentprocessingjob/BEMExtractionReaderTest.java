package gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob;
/**
 * 
 */
import gov.cms.dsh.bem.BenefitEnrollmentMaintenanceType;
import gov.cms.dsh.bem.BenefitEnrollmentRequest;
import gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentMaintenanceDTO;
import gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentRequestDTO;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.item.xml.StaxUtils;
import org.springframework.batch.item.xml.stax.DefaultFragmentEventReader;
import org.springframework.batch.item.xml.stax.FragmentEventReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.oxm.XmlMappingException;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Test class for BEMExtractionReader
 * 
 * @author girish.padmanabhan
 * 
 */
@ContextConfiguration(locations={"/test-context.xml"})
@TestExecutionListeners( { DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class })
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@RunWith(SpringJUnit4ClassRunner.class)
public class BEMExtractionReaderTest extends TestCase {

	private Resource resourceFile;
	private FragmentEventReader fragmentReader;
	private XMLEventReader eventReader;
	private BenefitEnrollmentMaintenanceType expectedBEM;
	private	BEMExtractionReader<BenefitEnrollmentMaintenanceDTO> xmlItemReader;

	@Autowired
	@Qualifier("bemUnMarshallerTest")
	private Jaxb2Marshaller unmarshaller;

	@Before
	public void setup() throws FactoryConfigurationError, XmlMappingException, Exception {

		xmlItemReader = new BEMExtractionReader<BenefitEnrollmentMaintenanceDTO>();
	}

	@Test
	public void testDoRead_success() throws Exception {

			resourceFile = new ClassPathResource("testfiles/epsTestValidSchema.xml");
			ReflectionTestUtils.setField(xmlItemReader, "aResource", resourceFile);
			xmlItemReader.setFragmentRootElementName("BenefitEnrollmentRequest");
			eventReader = XMLInputFactory.newInstance().createXMLEventReader(resourceFile.getInputStream());
			fragmentReader = new DefaultFragmentEventReader(eventReader);

			BenefitEnrollmentRequest ber = (BenefitEnrollmentRequest) unmarshaller.unmarshal(StaxUtils.getSource(fragmentReader));
			expectedBEM = ber.getBenefitEnrollmentMaintenance().get(0);

			xmlItemReader.setResource(resourceFile);
			xmlItemReader.setUnmarshaller(unmarshaller);
			ReflectionTestUtils.invokeMethod(xmlItemReader, "doOpen");

			BenefitEnrollmentRequestDTO berDto = xmlItemReader.doRead();
			BenefitEnrollmentRequest berRequest =  berDto.getBer();
			
			assertNotNull("BemDTO from reader", berDto);
			BenefitEnrollmentMaintenanceType actualBEM = berRequest.getBenefitEnrollmentMaintenance().get(0);
			assertNotNull("BEM from BemDT from reader", actualBEM);
			assertEquals("Issuer Name", expectedBEM.getIssuer().getName(), actualBEM.getIssuer().getName());
			assertEquals("Member list size", expectedBEM.getMember().size(), actualBEM.getMember().size());		

	}
	

	@Test
	public void testDoReadTrimming_success() throws Exception {

			resourceFile = new ClassPathResource("testfiles/epsTestValidSchema.xml");
			ReflectionTestUtils.setField(xmlItemReader, "aResource", resourceFile);
			xmlItemReader.setFragmentRootElementName("BenefitEnrollmentRequest");
			eventReader = XMLInputFactory.newInstance().createXMLEventReader(resourceFile.getInputStream());
			fragmentReader = new DefaultFragmentEventReader(eventReader);

			BenefitEnrollmentRequest ber = (BenefitEnrollmentRequest) unmarshaller.unmarshal(StaxUtils.getSource(fragmentReader));
			expectedBEM = ber.getBenefitEnrollmentMaintenance().get(0);

			xmlItemReader.setResource(resourceFile);
			xmlItemReader.setUnmarshaller(unmarshaller);
			ReflectionTestUtils.invokeMethod(xmlItemReader, "doOpen");

			BenefitEnrollmentRequestDTO berDto = xmlItemReader.doRead();

			assertNotNull("BemDTO from reader", berDto);
			BenefitEnrollmentRequest berRequest =  berDto.getBer();
			System.out.println(berRequest.getBenefitEnrollmentMaintenance());
			
			for (int i = 0; i < berRequest.getBenefitEnrollmentMaintenance().size(); i++) {
				BenefitEnrollmentMaintenanceType actualBEM = berRequest.getBenefitEnrollmentMaintenance().get(i);
				assertNotNull("BEM from BemDT from reader", actualBEM);
				assertTrue("preceding white space trimmed", !actualBEM.getIssuer().getName().startsWith(" "));
				assertTrue("trailing white space trimmed", !actualBEM.getIssuer().getName().endsWith(" "));
				assertTrue("preceding white space trimmed", !actualBEM.getIssuer().getTaxPayerIdentificationNumber().startsWith(" "));
				assertTrue("trailing white space trimmed", !actualBEM.getIssuer().getTaxPayerIdentificationNumber().endsWith(" "));
			}
			

	}
}