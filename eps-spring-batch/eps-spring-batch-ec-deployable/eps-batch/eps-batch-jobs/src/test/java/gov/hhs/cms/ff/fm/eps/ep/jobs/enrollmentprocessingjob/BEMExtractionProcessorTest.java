/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob;

import gov.cms.dsh.bem.BenefitEnrollmentMaintenanceType;
import gov.cms.dsh.bem.BenefitEnrollmentRequest;
import gov.cms.dsh.bem.FileInformationType;
import gov.cms.dsh.bem.TransactionInformationType;
import gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentRequestDTO;
import gov.hhs.cms.ff.fm.eps.ep.enums.ExchangeType;
import gov.hhs.cms.ff.fm.eps.ep.util.EpsDateUtils;

import java.io.File;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;

import junit.framework.TestCase;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.XmlMappingException;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Test class for BEMExtractionProcessor
 * 
 * @author girish.padmanabhan
 * 
 */
@RunWith(JUnit4.class)
public class BEMExtractionProcessorTest extends TestCase {

	BenefitEnrollmentMaintenanceType bem;
	BEMExtractionProcessor bEMExtractionProcessor;
	Jaxb2Marshaller unmarshaller;
	String fileName;
	String filePath;
	
	@Before
	public void setup() throws FactoryConfigurationError, XmlMappingException, Exception {
		bEMExtractionProcessor = new BEMExtractionProcessor();
		unmarshaller = new Jaxb2Marshaller();
		
		fileName = "1234.FFM.IC834.D140502.T185506987.T.IN";
		filePath = new ClassPathResource("testfiles/").getFile().getCanonicalPath().concat(File.separator);
		bEMExtractionProcessor.setFilePath(filePath);
	}
	
	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.BEMExtractionProcessor#process(gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentMaintenanceDTO)}
	 * This method tests the successful reading of File Information section from the BER xml
	 * when the insert file info flag is false (alternate path).
	 * 
	 * @throws Exception
	 */
	@Test
	public void testProcess_success_alternate() throws Exception {
		String fileInfoString = "testString";
		ReflectionTestUtils.setField(bEMExtractionProcessor, "fileInfoXml", fileInfoString);
		ReflectionTestUtils.setField(bEMExtractionProcessor, "fileNameTimeStamp", DateTime.parse("2014-05-02T18:55:06.987"));
		ReflectionTestUtils.setField(bEMExtractionProcessor, "exchangeType", ExchangeType.FFM.getValue());
		
		BenefitEnrollmentRequestDTO inputBer = new BenefitEnrollmentRequestDTO();
		inputBer.setInsertFileInfo(false);
		BenefitEnrollmentRequestDTO berDto = bEMExtractionProcessor.process(inputBer);
		
		assertTrue("Compare FileInfo XML string in BemDTO with the expected", 
				fileInfoString.equals(berDto.getFileInfoXml()));
		assertEquals("Compare timestaamp from File name  in BemDTO with the expected", 
				DateTime.parse("2014-05-02T18:55:06.987"), berDto.getFileNmDateTime());
		assertEquals("ExchangeType value in bemDTO is FFM", 
				ExchangeType.FFM.getValue(), berDto.getExchangeTypeCd());
	}
	
	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.BEMExtractionProcessor#process(gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentMaintenanceDTO)}
	 * This method tests the successful setting off Exchange type to FFM.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testProcess_success_FFM() throws Exception {
		
		ReflectionTestUtils.setField(bEMExtractionProcessor, "source", "ffm");
		
		BenefitEnrollmentRequestDTO inputBer = createBer();
		BenefitEnrollmentRequestDTO berDto = bEMExtractionProcessor.process(inputBer);
		FileInformationType expectedFileInfo = getFileInfoType();
		
		assertTrue("Compare FileInformationType in BerDTO with the expected", 
				compareFileInfo(expectedFileInfo, berDto.getFileInformation())); //Field compare
		assertTrue("Compare FileInfo XML string in BerDTO with the expected", 
				createFileInfoXMLString(expectedFileInfo).equals(berDto.getFileInfoXml()));
		assertEquals("ExchangeType value in berDTO is FFM", 
				ExchangeType.FFM.getValue(), berDto.getExchangeTypeCd()); 
	}
	
	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.BEMExtractionProcessor#process(gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentMaintenanceDTO)}
	 * This method tests the source parameter value being empty.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testProcess_empty_source() throws Exception {
		
		ReflectionTestUtils.setField(bEMExtractionProcessor, "source", "");
		
		BenefitEnrollmentRequestDTO inputBer = createBer();
		BenefitEnrollmentRequestDTO berDto = bEMExtractionProcessor.process(inputBer);
		FileInformationType expectedFileInfo = getFileInfoType();
		
		assertTrue("Compare FileInformationType in BerDTO with the expected", 
				compareFileInfo(expectedFileInfo, berDto.getFileInformation())); //Field compare
		assertTrue("Compare FileInfo XML string in BerDTO with the expected", 
				createFileInfoXMLString(expectedFileInfo).equals(berDto.getFileInfoXml()));
		assertEquals("ExchangeType value in berDTO is blank", 
				null, berDto.getExchangeTypeCd()); 
	}
	
	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.BEMExtractionProcessor#process(gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentMaintenanceDTO)}
	 * This method tests the source parameter value being empty.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testProcess_empty_other() throws Exception {
		
		ReflectionTestUtils.setField(bEMExtractionProcessor, "source", "DUMMY");
		
		BenefitEnrollmentRequestDTO inputBer = createBer();
		BenefitEnrollmentRequestDTO berDto = bEMExtractionProcessor.process(inputBer);
		FileInformationType expectedFileInfo = getFileInfoType();
		
		assertTrue("Compare FileInformationType in BerDTO with the expected", 
				compareFileInfo(expectedFileInfo, berDto.getFileInformation())); //Field compare
		assertTrue("Compare FileInfo XML string in BerDTO with the expected", 
				createFileInfoXMLString(expectedFileInfo).equals(berDto.getFileInfoXml()));
		assertEquals("ExchangeType value in berDTO is blank", 
				null, berDto.getExchangeTypeCd()); 
	}
	
	/*
	 * private method to create input BEM DTO
	 */
	private BenefitEnrollmentRequestDTO createBer() {
		BenefitEnrollmentRequestDTO berDto = new BenefitEnrollmentRequestDTO();
		berDto.setInsertFileInfo(true);
		berDto.setFileNm(fileName);
		
		TransactionInformationType transactionInfo = new TransactionInformationType();
		transactionInfo.setCurrentTimeStamp(
				EpsDateUtils.getXMLGregorianCalendar(DateTime.parse("2014-05-02T18:55:06.987")));
		BenefitEnrollmentMaintenanceType bemType = new BenefitEnrollmentMaintenanceType();
		bemType.setTransactionInformation(transactionInfo);
		
		BenefitEnrollmentRequest ber  = new BenefitEnrollmentRequest();
		ber.getBenefitEnrollmentMaintenance().add(bemType);
		
		berDto.setBer(ber);
		
		return berDto;
	}

	/*
	 * private method to create expected FileInformationType object
	 */
	private FileInformationType getFileInfoType() {
		FileInformationType fileInfoType = new FileInformationType();
		fileInfoType.setGroupSenderID("12345TT0012001");
		fileInfoType.setGroupReceiverID("MI0");
		fileInfoType.setGroupControlNumber("3592");
		fileInfoType.setGroupTimeStamp(EpsDateUtils.getXMLGregorianCalendar("2014-02-03T18:05:00.000-05:00"));
		fileInfoType.setVersionNumber("23");
		
		return fileInfoType;
	}
	
	/*
	 * private method to compare expected FileInformationType object with actual
	 */
	private boolean compareFileInfo(FileInformationType expected, FileInformationType actual) {
		if (expected.getGroupSenderID().equals(actual.getGroupSenderID())
				&& expected.getGroupReceiverID().equals(actual.getGroupReceiverID())
				&& expected.getGroupControlNumber().equals(actual.getGroupControlNumber())
				&& expected.getGroupTimeStamp().equals(actual.getGroupTimeStamp())
				&& expected.getVersionNumber().equals(actual.getVersionNumber())
				) {
			return true;
		}
		return false;
	}

	/*
	 * private method to create expected FileInformationType xml string
	 */
	private String createFileInfoXMLString(FileInformationType value) throws FactoryConfigurationError, XmlMappingException, Exception {
		JAXBContext jc = JAXBContext.newInstance(FileInformationType.class);
		Marshaller marshaller = jc.createMarshaller();
		StringWriter sw = new StringWriter();
		
		JAXBElement<FileInformationType> jaxbElement = new JAXBElement<FileInformationType>(new QName("FileInformation"), FileInformationType.class, value);
		marshaller.marshal(jaxbElement, sw);
		return sw.toString();
	}
	
	@After
	public void tearDown() {
	}
}
