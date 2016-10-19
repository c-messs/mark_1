/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.xml.stream.FactoryConfigurationError;

import org.apache.commons.collections.CollectionUtils;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.oxm.XmlMappingException;
import org.springframework.test.util.ReflectionTestUtils;

import gov.hhs.cms.ff.fm.eps.ep.SBMValidationRequest;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMPolicyEnum;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMCache;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMPolicyDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SBMFileCompositeDAO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.validation.SbmValidationService;
import junit.framework.TestCase;

/**
 * Test class for BEMExtractionProcessor
 * 
 * @author girish.padmanabhan
 * 
 */
@RunWith(JUnit4.class)
public class XprProcessorTest extends TestCase {

	private XprProcessor xprProcessor;
	private SbmValidationService mockSbmValidationService;
	private SBMFileCompositeDAO mockSbmFileCompositeDao;
	private SbmXMLValidator sbmXMLValidator;

	@Before
	public void setup() throws FactoryConfigurationError, XmlMappingException, Exception {
		xprProcessor = new XprProcessor();
		sbmXMLValidator = new SbmXMLValidator();
		sbmXMLValidator.setXsdFilePath("/xsd/SBMPolicy.xsd");
		xprProcessor.setSbmXMLValidator(sbmXMLValidator);
		
		mockSbmFileCompositeDao = createMock(SBMFileCompositeDAO.class);
		xprProcessor.setSbmFileCompositeDao(mockSbmFileCompositeDao);
		
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testProcess_success() throws Exception {
		
		mockSbmValidationService = createMock(SbmValidationService.class);
		xprProcessor.setSbmValidationService(mockSbmValidationService);
		
		mockSbmValidationService.validatePolicy(EasyMock.anyObject(SBMValidationRequest.class));
		EasyMock.expectLastCall();
		replay(mockSbmValidationService);
		
		File xmlFileInfo = new File("./src/test/resources/sbm/SBMI_FileInfo.xml");
		String fileInfoXmlString = readFile(xmlFileInfo);
		expect(mockSbmFileCompositeDao.getFileInfoTypeXml(EasyMock.anyLong())).andReturn(fileInfoXmlString);
		replay(mockSbmFileCompositeDao);
		
		String fileInfoString = "testString";
		ReflectionTestUtils.setField(xprProcessor, "fileInfoXml", fileInfoString);

		File xmlPolicy = new File("./src/test/resources/sbm/SBMI_Policy.xml");
		String xmlPolicyString = readFile(xmlPolicy);
		
		SBMPolicyDTO inputDto = new SBMPolicyDTO();
		inputDto.setPolicyXml(xmlPolicyString);
		
		xprProcessor.setJobId(999L);
		SBMPolicyDTO resultDto = xprProcessor.process(inputDto);

		assertTrue("No Schema errors", CollectionUtils.isEmpty(resultDto.getSchemaErrorList()));
		
		String expectedFileInfo = fileInfoXmlString.substring(fileInfoXmlString.indexOf(SBMPolicyEnum.FILE_INFO.getElementNm()) - 1, 
				fileInfoXmlString.lastIndexOf(SBMPolicyEnum.FILE_INFO.getElementNm()) + SBMPolicyEnum.FILE_INFO.getElementNm().length() + 1);

		assertTrue("Compare FileInfo XML string in DTO with the expected", 
				resultDto.getFileInfoXml().equals(expectedFileInfo));
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void testProcess_success_schemaErrors() throws Exception {
		
		mockSbmValidationService = createMock(SbmValidationService.class);
		xprProcessor.setSbmValidationService(mockSbmValidationService);
		
		mockSbmValidationService.validatePolicy(EasyMock.anyObject(SBMValidationRequest.class));
		EasyMock.expectLastCall();
		replay(mockSbmValidationService);
		
		File xmlFileInfo = new File("./src/test/resources/sbm/SBMI_FileInfo.xml");
		String fileInfoXmlString = readFile(xmlFileInfo);
		expect(mockSbmFileCompositeDao.getFileInfoTypeXml(EasyMock.anyLong())).andReturn(fileInfoXmlString);
		replay(mockSbmFileCompositeDao);
		
		String fileInfoString = "testString";
		ReflectionTestUtils.setField(xprProcessor, "fileInfoXml", fileInfoString);

		File xmlPolicy = new File("./src/test/resources/sbm/SBMI_Policy.xml");
		String xmlPolicyString = readFile(xmlPolicy);
		
		SBMPolicyDTO inputDto = new SBMPolicyDTO();
		inputDto.setPolicyXml(xmlPolicyString.substring(0, xmlPolicyString.indexOf("</Policy>")));
		
		SBMPolicyDTO resultDto = xprProcessor.process(inputDto);

		assertFalse("No Schema errors", CollectionUtils.isEmpty(resultDto.getSchemaErrorList()));
		
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void testProcess_success_schemaErrors_KeyFields() throws Exception {
		
		mockSbmValidationService = createMock(SbmValidationService.class);
		xprProcessor.setSbmValidationService(mockSbmValidationService);
		
		mockSbmValidationService.validatePolicy(EasyMock.anyObject(SBMValidationRequest.class));
		EasyMock.expectLastCall();
		replay(mockSbmValidationService);
		
		File xmlFileInfo = new File("./src/test/resources/sbm/SBMI_FileInfo.xml");
		String fileInfoXmlString = readFile(xmlFileInfo);
		expect(mockSbmFileCompositeDao.getFileInfoTypeXml(EasyMock.anyLong())).andReturn(fileInfoXmlString);
		replay(mockSbmFileCompositeDao);
		
		String fileInfoString = "testString";
		ReflectionTestUtils.setField(xprProcessor, "fileInfoXml", fileInfoString);

		File xmlPolicy = new File("./src/test/resources/sbm/SBMI_Policy_Errors.xml");
		String xmlPolicyString = readFile(xmlPolicy);
		
		SBMPolicyDTO inputDto = new SBMPolicyDTO();
		inputDto.setPolicyXml(xmlPolicyString);
		
		SBMPolicyDTO resultDto = xprProcessor.process(inputDto);

		assertFalse("No Schema errors", CollectionUtils.isEmpty(resultDto.getSchemaErrorList()));
		
	}
	
	@Test
	public void testProcess_success_duplicatePolicy() throws Exception {
		
		SBMCache.getPolicyIds().add("EXPOLICYID");
		SBMCache.getPolicyIds().add("EXPOLICYID");
		
		File xmlFileInfo = new File("./src/test/resources/sbm/SBMI_FileInfo.xml");
		String fileInfoXmlString = readFile(xmlFileInfo);
		expect(mockSbmFileCompositeDao.getFileInfoTypeXml(EasyMock.anyLong())).andReturn(fileInfoXmlString);
		replay(mockSbmFileCompositeDao);
		
		String fileInfoString = "testString";
		ReflectionTestUtils.setField(xprProcessor, "fileInfoXml", fileInfoString);

		File xmlPolicy = new File("./src/test/resources/sbm/SBMI_Policy.xml");
		String xmlPolicyString = readFile(xmlPolicy);
		
		SBMPolicyDTO inputDto = new SBMPolicyDTO();
		inputDto.setPolicyXml(xmlPolicyString);
		
		SBMPolicyDTO resultDto = xprProcessor.process(inputDto);

		assertTrue("Duplicate Policy error", CollectionUtils.isNotEmpty(resultDto.getErrorList()));
		
	}
	
	private String readFile(File file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		StringBuilder sb = new StringBuilder();
		String line;
		while( (line = reader.readLine()) != null) {
			sb.append(line);
			sb.append("\n");
		}
		
		reader.close();
		
		return sb.toString();
	}

	@After
	public void tearDown() {
		SBMCache.getPolicyIds().clear();
	}
}
