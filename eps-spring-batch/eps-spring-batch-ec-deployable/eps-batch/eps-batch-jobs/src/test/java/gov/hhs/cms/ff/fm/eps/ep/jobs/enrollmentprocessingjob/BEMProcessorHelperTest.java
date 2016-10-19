/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.resetToNice;
import static org.easymock.EasyMock.verify;
import gov.cms.dsh.bem.BenefitEnrollmentMaintenanceType;
import gov.cms.dsh.bem.BooleanIndicatorSimpleType;
import gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentMaintenanceDTO;
import gov.hhs.cms.ff.fm.eps.ep.EPSValidationRequest;
import gov.hhs.cms.ff.fm.eps.ep.enums.ExchangeType;
import gov.hhs.cms.ff.fm.eps.ep.validation.EPSValidationResponse;
import gov.hhs.cms.ff.fm.eps.ep.validation.EPSValidationService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.xml.stream.FactoryConfigurationError;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.XmlMappingException;

import com.accenture.foundation.common.exception.ApplicationException;

/**
 * Test class for bEMProcessorHelper
 * 
 * @author girish.padmanabhan
 * 
 */
@RunWith(JUnit4.class)
public class BEMProcessorHelperTest extends TestCase {

	BEMProcessorHelper bEMProcessorHelper;
	EPSValidationService mockEPSValidationService;
	
	@Before
	public void setup() throws FactoryConfigurationError, XmlMappingException, Exception {
		bEMProcessorHelper = new BEMProcessorHelper();
		mockEPSValidationService = createMock(EPSValidationService.class);
		bEMProcessorHelper.setEpsValidationService(mockEPSValidationService);
	}
	
	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.bEMProcessorHelper#process(gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentMaintenanceDTO)}
	 * This method tests the successful marshalling of BEM xml string and calling the validator for SBM.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testProcess_success_SBM() throws Exception {
		
		expect(mockEPSValidationService.validateBEM(EasyMock.anyObject(EPSValidationRequest.class))).andReturn(null);
		replay(mockEPSValidationService);
		
		BenefitEnrollmentMaintenanceDTO inputBem = createBem();
		setFileInfo(inputBem);
		BenefitEnrollmentMaintenanceDTO bemDto = bEMProcessorHelper.process(inputBem);
		
		BenefitEnrollmentMaintenanceType bem = bemDto.getBem();
		
		assertNotNull("BemType is not null in bemDTO", bem);
		assertEquals("BemType in bemDTO is an instance of class BenefitEnrollmentMaintenanceType", 
				bem.getClass(), BenefitEnrollmentMaintenanceType.class);
		assertEquals("ControlNumber value in BemType is 6678", 
				bem.getTransactionInformation().getControlNumber(),  "6678");
		assertEquals("Issuer name in BemType is Aviva Care Test", 
				bem.getIssuer().getName(),  "Aviva Care Test");
		assertEquals("Issuer TaxPayerIdentificationNumber in BemType is 699369001", 
				bem.getIssuer().getTaxPayerIdentificationNumber(),  "699369001");
		assertEquals("Subscriber Indicator for 1st membber in BemType is Y", 
				bem.getMember().get(0).getMemberInformation().getSubscriberIndicator(),  BooleanIndicatorSimpleType.Y);
	}

	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.bEMProcessorHelper#process(gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentMaintenanceDTO)}
	 * This method tests the successful marshalling of BEM xml string and calling the validator for FFM.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testProcess_success_FFM() throws Exception {
		
		EPSValidationResponse response = new EPSValidationResponse();
		expect(mockEPSValidationService.validateBEM(EasyMock.anyObject(EPSValidationRequest.class))).andReturn(response);
		replay(mockEPSValidationService);
		
		BenefitEnrollmentMaintenanceDTO inputBem = createBem();
		inputBem.setExchangeTypeCd(ExchangeType.FFM.getValue());
		setFileInfo(inputBem);
		BenefitEnrollmentMaintenanceDTO bemDto = bEMProcessorHelper.process(inputBem);
		
		BenefitEnrollmentMaintenanceType bem = bemDto.getBem();
		
		assertNotNull("BemType is not null in bemDTO", bem);
		assertEquals("BemType in bemDTO is an instance of class BenefitEnrollmentMaintenanceType", 
				bem.getClass(), BenefitEnrollmentMaintenanceType.class);
		assertEquals("ControlNumber value in BemType is 6678", 
				bem.getTransactionInformation().getControlNumber(),  "6678");
		assertEquals("Issuer name in BemType is Aviva Care Test", 
				bem.getIssuer().getName(),  "Aviva Care Test");
		assertEquals("Issuer TaxPayerIdentificationNumber in BemType is 699369001", 
				bem.getIssuer().getTaxPayerIdentificationNumber(),  "699369001");
		assertEquals("Subscriber Indicator for 1st membber in BemType is Y", 
				bem.getMember().get(0).getMemberInformation().getSubscriberIndicator(),  BooleanIndicatorSimpleType.Y);
	}
	
	@Test
	public void testProcess_success_FFM_JAXB() throws Exception {	
		
		BenefitEnrollmentMaintenanceDTO inputBem = null;
		
		expect(mockEPSValidationService.validateBEM(EasyMock.anyObject(EPSValidationRequest.class)));
		
		int cntFilesToProcess = 10; 

		for (int i = 0; i < cntFilesToProcess; ++i) {
			
			resetToNice(mockEPSValidationService);
			
			inputBem = createBem();
			inputBem.setBatchId(-1L);
			inputBem.setExchangeTypeCd(ExchangeType.FFM.getValue());
			BenefitEnrollmentMaintenanceDTO bemDto = bEMProcessorHelper.process(inputBem);

			BenefitEnrollmentMaintenanceType bem = bemDto.getBem();

			assertNotNull("BemType is not null in bemDTO", bem);
			assertEquals("BemType in bemDTO is an instance of class BenefitEnrollmentMaintenanceType", 
					bem.getClass(), BenefitEnrollmentMaintenanceType.class);
			assertEquals("ControlNumber value in BemType is 6678", 
					bem.getTransactionInformation().getControlNumber(),  "6678");
			assertEquals("Issuer name in BemType is Aviva Care Test", 
					bem.getIssuer().getName(),  "Aviva Care Test");
			assertEquals("Issuer TaxPayerIdentificationNumber in BemType is 699369001", 
					bem.getIssuer().getTaxPayerIdentificationNumber(),  "699369001");
			assertEquals("Subscriber Indicator for 1st membber in BemType is Y", 
					bem.getMember().get(0).getMemberInformation().getSubscriberIndicator(),  BooleanIndicatorSimpleType.Y);
		}
	}
	
	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.bEMProcessorHelper#process(gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentMaintenanceDTO)}
	 * This method tests the successful marshalling of BEM xml string and calling the validator for FFM.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testProcess_success_FFM_invalid() throws Exception {
		
		expect(mockEPSValidationService.validateBEM(
				EasyMock.anyObject(EPSValidationRequest.class))).andReturn(null);
		replay(mockEPSValidationService);
		
		BenefitEnrollmentMaintenanceDTO inputBem = createBem();
		inputBem.setExchangeTypeCd(ExchangeType.FFM.getValue());
		setFileInfo(inputBem);
		BenefitEnrollmentMaintenanceDTO bemDto = bEMProcessorHelper.process(inputBem);
		
		assertNotNull("Returned bemDto is not null", bemDto);
	}
	
	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.bEMProcessorHelper#write(gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentMaintenanceDTO)}
	 * This test method expects the process() method to throw an Exception
	 * 
	 * @throws Exception
	 */
	@Test//(expected=Exception.class)
	public void testProcess_exception() throws Exception {
		
		try {
			expect(mockEPSValidationService.validateBEM(EasyMock.anyObject(EPSValidationRequest.class))).andThrow(new Exception());
			replay(mockEPSValidationService);
			
			BenefitEnrollmentMaintenanceDTO inputBem = createBem();
			assertNotNull("BenefitEnrollmentMaintenanceDTO inputBem", inputBem);
		
			bEMProcessorHelper.process(inputBem);
		} catch(Exception appEx) {
			assertTrue("Exception thrown", true);
		}
	}

	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.bEMProcessorHelper#write(gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentMaintenanceDTO)}
	 * This test method expects the process() method to throw an JAXB UnmarshalException
	 * 
	 * @throws Exception
	 */
	@Test//(expected=com.accenture.foundation.common.exception.ApplicationException.class)
	public void testProcess_JAXBException() throws Exception {
		expect(mockEPSValidationService.validateBEM(
				EasyMock.anyObject(EPSValidationRequest.class))).andReturn(null);
		replay(mockEPSValidationService);
		
		BenefitEnrollmentMaintenanceDTO inputBem = createBem();
		String bemXml = inputBem.getBemXml().replaceFirst("<BenefitEnrollmentMaintenance", "");
		inputBem.setBemXml(bemXml);
		
		assertNotNull("BenefitEnrollmentMaintenanceDTO inputBem", inputBem);
		
		try {
			bEMProcessorHelper.process(inputBem);
		} catch(ApplicationException appEx) {
			assertTrue("ApplicationException thrown", true);
		}
	}
	
	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.bEMProcessorHelper#write(gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentMaintenanceDTO)}
	 * This test method expects the process() method to throw an NullPointerException
	 * since BemXML is null.
	 * 
	 * @throws Exception
	 */
	@Test//(expected=com.accenture.foundation.common.exception.ApplicationException.class)
	public void testProcess_ApplicationException() throws Exception {
		
		BenefitEnrollmentMaintenanceDTO inputBem = new BenefitEnrollmentMaintenanceDTO();
		assertNotNull("BenefitEnrollmentMaintenanceDTO inputBem", inputBem);
		
		try {
			bEMProcessorHelper.process(inputBem);
		} catch(ApplicationException appEx) {
			assertTrue("ApplicationException thrown", true);
		}
	}
	
	/*
	 * private method to create input BEM DTO
	 */
	private BenefitEnrollmentMaintenanceDTO createBem() throws IOException {
		BenefitEnrollmentMaintenanceDTO bem = new BenefitEnrollmentMaintenanceDTO();
		
		File input = new ClassPathResource("testfiles/TestBEMString.xml").getFile();
		BufferedReader br = new BufferedReader(new FileReader(input));
		StringBuilder sb = new StringBuilder();
		String line = br.readLine();
        sb.append(line);
        br.close();

		bem.setBemXml(sb.toString());
		
		File inputFileInfo = new ClassPathResource("testfiles/TestFileInfoString.xml").getFile();
		BufferedReader brFileInfo = new BufferedReader(new FileReader(inputFileInfo));
		StringBuilder sbFileInfo = new StringBuilder();
		String lineFileInfo = brFileInfo.readLine();
		sbFileInfo.append(lineFileInfo);
		brFileInfo.close();
		bem.setFileInfoXml(sbFileInfo.toString());
		
		bem.setExchangeTypeCd(ExchangeType.SBM.getValue());
		
		return bem;
	}
	
	private void setFileInfo(BenefitEnrollmentMaintenanceDTO bemDTO) throws IOException {
		File input = new ClassPathResource("testfiles/TestFileInfoString.xml").getFile();
		BufferedReader br = new BufferedReader(new FileReader(input));
		StringBuilder sb = new StringBuilder();
		String line = br.readLine();
        sb.append(line);
        br.close();
        
        bemDTO.setFileInfoXml(sb.toString());
	}
	
	@After
	public void tearDown() {
	}
}
