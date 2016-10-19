package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import gov.cms.dsh.sbmi.FileInformationType;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMErrorWarningCode;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMPolicyEnum;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMSchemaValidationRuleEnum;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMErrorDTO;
import junit.framework.TestCase;

@RunWith(JUnit4.class)
public class SbmXMLValidatorTest extends TestCase {
	
	private static final Logger LOG = LoggerFactory.getLogger(SbmXMLValidatorTest.class);
	private SbmXMLValidator sbmXMLValidator;
		
		
	@Before
	public void setUp() throws ParserConfigurationException, SAXException, IOException {
		sbmXMLValidator = new SbmXMLValidator();		
		sbmXMLValidator.setXsdFilePath("/xsd/SBMPolicy.xsd");
		LOG.info("XSD File: {}", sbmXMLValidator.getXsdFilePath());
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
	
	@Test
	public void testFileLevel_noErrors() throws SAXException, IOException, ParserConfigurationException {
		
		File xmlFile = new File("./src/test/resources/sbm/schemaErrors/SBMI_noSchemaErrors.xml");
		String xmlString = readFile(xmlFile);
		LOG.info("xmlString:\n{}", xmlString);
		 
		List<SBMErrorDTO> list = sbmXMLValidator.validateSchemaForFileInfo(12345L, xmlFile);
		Assert.assertTrue("No errors should exists.", list.isEmpty());
	}
	
	/**
	 * Test multiple policies no schema error
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	@Test
	public void testFileLevel_multiplePlcyNoErrors() throws SAXException, IOException, ParserConfigurationException {
		
		File xmlFile = new File("./src/test/resources/sbm/schemaErrors/SBMI_ManyPolicies_noSchemaErrors.xml");
		String xmlString = readFile(xmlFile);
		LOG.info("xmlString:\n{}", xmlString);
		 
		List<SBMErrorDTO> list = sbmXMLValidator.validateSchemaForFileInfo(12345L, xmlFile);
		Assert.assertTrue("No errors should exists.", list.isEmpty());
	}
	
	/**
	 * Test no record level errors
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	@Test
	public void testRecordLevel_noErrors() throws SAXException, IOException, ParserConfigurationException {
		
		File xmlFile = new File("./src/test/resources/sbm/schemaErrors/SBMI_noSchemaErrors.xml");
		String xmlString = readFile(xmlFile);
		LOG.info("xmlString:\n{}", xmlString);
		 
		List<SBMErrorDTO> list = sbmXMLValidator.validateSchemaForXPR(xmlString);
		Assert.assertTrue("No errors should exists.", list.isEmpty());
	}
		
	@Test
	public void testXMLErrors() throws SAXException, IOException {
		
		File xmlFile = new File("./src/test/resources/sbm/schemaErrors/SBMI_schemaErrors_2.xml");
		String xmlString = readFile(xmlFile);
		LOG.info("xmlString:\n{}", xmlString);
		
		List<SBMErrorDTO> errors = sbmXMLValidator.validateSchemaForXPR(xmlString);
		LOG.info("errorCount: {}", errors.size());
		for(SBMErrorDTO err: errors) {
			LOG.info("error: {}", err);
		}
		
		Assert.assertTrue(SBMPolicyEnum.COVERAGE_YEAR.getElementNm() + " - missing error ER-003", isErrorExists(errors, SBMPolicyEnum.COVERAGE_YEAR.getElementNm(), SBMErrorWarningCode.ER_003.getCode()));
		Assert.assertTrue(SBMPolicyEnum.APTC.getElementNm() + " - missing error ER-004", isErrorExists(errors, SBMPolicyEnum.APTC.getElementNm(), SBMErrorWarningCode.ER_009.getCode()));
		Assert.assertTrue(SBMPolicyEnum.ISS_ID.getElementNm() + " - missing error ER-004", isErrorExists(errors, SBMPolicyEnum.ISS_ID.getElementNm(), SBMErrorWarningCode.ER_006.getCode()));
		
	}
	
	@Test
	public void testSAXErrors() throws SAXException, IOException {
		
		File xmlFile = new File("./src/test/resources/sbm/schemaErrors/SBMI_schemaErrors_2.xml");
		String xmlString = readFile(xmlFile);
		LOG.info("xmlString:\n{}", xmlString);
		
		List<SBMErrorDTO> errors = sbmXMLValidator.validateSchemaForXPR(xmlString.replace("<p:Policy>", ""));
		LOG.info("errorCount: {}", errors.size());
		for(SBMErrorDTO err: errors) {
			LOG.info("error: {}", err);
		}
		
		Assert.assertEquals("error ER-003", errors.get(0).getSbmErrorWarningTypeCd(), SBMErrorWarningCode.ER_003.getCode());
		
	}
	
	/**
	 * 
	 * @throws SAXException
	 * @throws IOException
	 */
	@Test
	public void testFileInfo_shcemaErrors() throws SAXException, IOException {

		File xmlFile = new File("./src/test/resources/sbm/schemaErrors/SBMI_FileLevelErrors_6.xml");
		String xmlString = readFile(xmlFile);
		LOG.info("xmlString:\n{}", xmlString);

		List<SBMErrorDTO> errors = sbmXMLValidator.validateSchemaForFileInfo(123L, xmlFile);
		LOG.info("errorCount: {}", errors.size());
		for(SBMErrorDTO err: errors) {
			LOG.info("error: {}", err);
		}
		Assert.assertTrue("Errors should exist", errors.size() > 0);
		
		Assert.assertTrue(SBMPolicyEnum.TENANT_ID.getElementNm() + " - missing error ER-004", isErrorExists(errors, SBMPolicyEnum.TENANT_ID.getElementNm(), SBMErrorWarningCode.ER_004.getCode()));
		Assert.assertTrue(SBMPolicyEnum.ISS_ID.getElementNm() + " - missing error ER-004", isErrorExists(errors, SBMPolicyEnum.ISS_ID.getElementNm(), SBMErrorWarningCode.ER_004.getCode()));
		Assert.assertTrue(SBMPolicyEnum.ISS_ID.getElementNm() + " - missing error ER-004", isErrorExists(errors, SBMPolicyEnum.ISS_ID.getElementNm(), SBMErrorWarningCode.ER_006.getCode()));
		Assert.assertTrue(SBMPolicyEnum.ISS_FILE_SET_ID.getElementNm() + " - missing error ER-004", isErrorExists(errors, SBMPolicyEnum.ISS_FILE_SET_ID.getElementNm(), SBMErrorWarningCode.ER_004.getCode()));
		Assert.assertTrue(SBMPolicyEnum.FILE_NUM.getElementNm() + " - missing error ER-004", isErrorExists(errors, SBMPolicyEnum.FILE_NUM.getElementNm(), SBMErrorWarningCode.ER_004.getCode()));
		Assert.assertTrue(SBMPolicyEnum.TOT_ISS_FILES.getElementNm() + " - missing error ER-004", isErrorExists(errors, SBMPolicyEnum.TOT_ISS_FILES.getElementNm(), SBMErrorWarningCode.ER_004.getCode()));
		Assert.assertTrue(SBMPolicyEnum.COVERAGE_YEAR.getElementNm() + " - missing error ER-004", isErrorExists(errors, SBMPolicyEnum.COVERAGE_YEAR.getElementNm(), SBMErrorWarningCode.ER_004.getCode()));
		Assert.assertTrue(SBMPolicyEnum.FILE_CREATE_DT.getElementNm() + " - missing error ER-004", isErrorExists(errors, SBMPolicyEnum.FILE_CREATE_DT.getElementNm(), SBMErrorWarningCode.ER_004.getCode()));
		
		Assert.assertFalse(SBMPolicyEnum.FILE_NUM.getElementNm() + " - Error ER-009 should not exist", isErrorExists(errors, SBMPolicyEnum.FILE_NUM.getElementNm(), SBMErrorWarningCode.ER_009.getCode()));
		Assert.assertFalse(SBMPolicyEnum.TOT_ISS_FILES.getElementNm() + " - Error ER-009 should not exist", isErrorExists(errors, SBMPolicyEnum.TOT_ISS_FILES.getElementNm(), SBMErrorWarningCode.ER_009.getCode()));
		
	}
	
	@Test
	public void testFileInfo_shcemaErrors_er006() throws SAXException, IOException {

		File xmlFile = new File("./src/test/resources/sbm/schemaErrors/SBMI_FileLevelErrors_8.xml");
		String xmlString = readFile(xmlFile);
		LOG.info("xmlString:\n{}", xmlString);

		List<SBMErrorDTO> errors = sbmXMLValidator.validateSchemaForFileInfo(123L, xmlFile);
		LOG.info("errorCount: {}", errors.size());
		for(SBMErrorDTO err: errors) {
			LOG.info("error: {}", err);
		}
		Assert.assertTrue("Errors should exist", errors.size() > 0);
		
		Assert.assertTrue(SBMPolicyEnum.ISS_ID.getElementNm() + " - missing error ER-004", isErrorExists(errors, SBMPolicyEnum.ISS_ID.getElementNm(), SBMErrorWarningCode.ER_004.getCode()));
		Assert.assertFalse(SBMPolicyEnum.ISS_ID.getElementNm() + " - no error should exist for ER-006", isErrorExists(errors, SBMPolicyEnum.ISS_ID.getElementNm(), SBMErrorWarningCode.ER_006.getCode()));
		
		Assert.assertTrue(SBMPolicyEnum.ISS_FILE_SET_ID.getElementNm() + " - missing error ER-004", isErrorExists(errors, SBMPolicyEnum.ISS_FILE_SET_ID.getElementNm(), SBMErrorWarningCode.ER_004.getCode()));
		Assert.assertTrue(SBMPolicyEnum.ISS_FILE_SET_ID.getElementNm() + " - missing error ER-004", isErrorExists(errors, SBMPolicyEnum.ISS_FILE_SET_ID.getElementNm(), SBMErrorWarningCode.ER_006.getCode()));
		
	}
	
	@Test
	public void testFileLevel_noPolicyTag() throws SAXException, IOException {

		File xmlFile = new File("./src/test/resources/sbm/schemaErrors/SBMI_FileLevelErrors_1.xml");
		String xmlString = readFile(xmlFile);
		LOG.info("xmlString:\n{}", xmlString);

		List<SBMErrorDTO> errors = sbmXMLValidator.validateSchemaForFileInfo(123L, xmlFile);
		LOG.info("errorCount: {}", errors.size());
		for(SBMErrorDTO err: errors) {
			LOG.info("error: {}", err);
		}
		Assert.assertTrue("One error should exists", errors.size() == 1);
		Assert.assertTrue("Policy Tag missing error not found", isErrorExists(errors, SBMPolicyEnum.POLICY.getElementNm(), SBMErrorWarningCode.ER_001.getCode()));
	}
	
	@Test
	public void testFileLevel_noRecordControlNumber() throws SAXException, IOException {

		File xmlFile = new File("./src/test/resources/sbm/schemaErrors/SBMI_FileLevelErrors_2.xml");
		String xmlString = readFile(xmlFile);
		LOG.info("xmlString:\n{}", xmlString);

		List<SBMErrorDTO> errors = sbmXMLValidator.validateSchemaForFileInfo(123L, xmlFile);
		LOG.info("errorCount: {}", errors.size());
		for(SBMErrorDTO err: errors) {
			LOG.info("error: {}", err);
		}
		Assert.assertTrue("One error should exists", errors.size() == 1);
		Assert.assertTrue("RecordControlNumber error not found", isErrorExists(errors, SBMPolicyEnum.REC_CTRL_NUM.getElementNm(), SBMErrorWarningCode.ER_003.getCode()));
	}
	
	/**
	 * Tests 
	 * 		- Two errors for missing record control number for two policies
	 * 		- No error should be created for missing QHPId tag for third policy
	 * @throws SAXException
	 * @throws IOException
	 */
	@Test
	public void testFileLevel_multiplePlcyNoRecCtrlNum() throws SAXException, IOException {

		File xmlFile = new File("./src/test/resources/sbm/schemaErrors/SBMI_ManyPolicies_FileErrors.xml");
		String xmlString = readFile(xmlFile);
		LOG.info("xmlString:\n{}", xmlString);

		List<SBMErrorDTO> errors = sbmXMLValidator.validateSchemaForFileInfo(123L, xmlFile);
		LOG.info("errorCount: {}", errors.size());
		for(SBMErrorDTO err: errors) {
			LOG.info("error: {}", err);
		}
		Assert.assertTrue("Errors should exist", errors.size() == 2);
		Assert.assertTrue("RecordControlNumber error should exist", isErrorExists(errors, SBMPolicyEnum.REC_CTRL_NUM.getElementNm(), SBMErrorWarningCode.ER_003.getCode()));
		Assert.assertFalse("No error should exist for missing QHPId", isErrorExists(errors, SBMPolicyEnum.QHPID.getElementNm(), SBMErrorWarningCode.ER_003.getCode()));
	}
	
	/**
	 * Tests 
	 * 		- Error: Exceeds maximum frequency for FileInfo tag
	 * 		
	 * @throws SAXException
	 * @throws IOException
	 */
	@Test
	public void testFileLevel_duplicateFileInfo() throws SAXException, IOException {

		File xmlFile = new File("./src/test/resources/sbm/schemaErrors/SBMI_FileLevelErrors_4.xml");
		String xmlString = readFile(xmlFile);
		LOG.info("xmlString:\n{}", xmlString);

		List<SBMErrorDTO> errors = sbmXMLValidator.validateSchemaForFileInfo(123L, xmlFile);
		LOG.info("errorCount: {}", errors.size());
		for(SBMErrorDTO err: errors) {
			LOG.info("error: {}", err);
		}
		Assert.assertTrue("Errors should exist", errors.size() > 0);
		Assert.assertTrue("POLICY error should exist", isErrorExists(errors, SBMPolicyEnum.POLICY.getElementNm(), SBMErrorWarningCode.ER_001.getCode()));
		
	}
	
	/**
	 * Tests 
	 * 		- Missing FileInfo tag
	 * 		
	 * @throws SAXException
	 * @throws IOException
	 */
	@Test
	public void testFileLevel_missingFileInfo() throws SAXException, IOException {

		File xmlFile = new File("./src/test/resources/sbm/schemaErrors/SBMI_FileLevelErrors_3.xml");
		String xmlString = readFile(xmlFile);
		LOG.info("xmlString:\n{}", xmlString);

		List<SBMErrorDTO> errors = sbmXMLValidator.validateSchemaForFileInfo(123L, xmlFile);
		LOG.info("errorCount: {}", errors.size());
		for(SBMErrorDTO err: errors) {
			LOG.info("error: {}", err);
		}
		Assert.assertTrue("Errors should exist", errors.size() == 1);
		Assert.assertTrue("RecordControlNumber error should exist", isErrorExists(errors, SBMPolicyEnum.FILE_INFO.getElementNm(), SBMErrorWarningCode.ER_001.getCode()));
		
	}
	
	
	/**
	 * Tests 
	 * 		- Error: Missing FileId tag
	 * 		- Error: Missing FileSetId tag
	 * 		
	 * @throws SAXException
	 * @throws IOException
	 */
	
	@Test
	public void testFileLevel_missingFileId() throws SAXException, IOException {

		File xmlFile = new File("./src/test/resources/sbm/schemaErrors/SBMI_FileLevelErrors_5.xml");
		String xmlString = readFile(xmlFile);
		LOG.info("xmlString:\n{}", xmlString);

		List<SBMErrorDTO> errors = sbmXMLValidator.validateSchemaForFileInfo(123L, xmlFile);
		LOG.info("errorCount: {}", errors.size());
		for(SBMErrorDTO err: errors) {
			LOG.info("error: {}", err);
		}
		Assert.assertTrue("Errors should exist", errors.size() == 2);
		Assert.assertTrue("FileNumber error should exist", isErrorExists(errors, SBMPolicyEnum.FILE_ID.getElementNm(), SBMErrorWarningCode.ER_003.getCode()));
		Assert.assertTrue("FileCreateDateTime error should exist", isErrorExists(errors, SBMPolicyEnum.ISS_FILE_SET_ID.getElementNm(), SBMErrorWarningCode.ER_003.getCode()));
		
	}
	
	/**
	 * Tests 
	 * 		- valid xml
	 * 		
	 * 		
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException 
	 */
	
	@Test
	public void testFileLevel_validXML() throws SAXException, IOException, ParserConfigurationException {

		File xmlFile = new File("./src/test/resources/sbm/schemaErrors/SBMI_FileLevelErrors_7.xml");
		String xmlString = readFile(xmlFile);
		LOG.info("xmlString:\n{}", xmlString);

		List<SBMErrorDTO> errors = sbmXMLValidator.validateSchemaForFileInfo(123L, xmlFile);
		Assert.assertTrue("XML should be valid", sbmXMLValidator.isValidXML(xmlFile));
		
	}
	
//	@Test
//	public void testReadXSDElements() throws ParserConfigurationException, SAXException, IOException {
//		 // parse the document
//        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
//        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
//        Document doc = docBuilder.parse (new File("./src/test/resources/xsd/SBMPolicy.xsd")); 
//        NodeList list = doc.getElementsByTagName("xs:element"); 
//        
//        LOG.info("list size:{}", list.getLength());
//        //loop to print data
//        for(int i = 0 ; i < list.getLength(); i++)
//        {
//            Element first = (Element)list.item(i);
//            LOG.info("Element tag name :{}", first.getElementsByTagName("name"));
//            if(first.hasAttributes())
//            {
//                String nm = first.getAttribute("name"); 
//                System.out.println(nm); 
////                String nm1 = first.getAttribute("type"); 
////                System.out.println(nm1); 
//            }
//        }
//	}
	
	/**
	 * Tests 
	 * 		- Error: Missing FileId tag
	 * 		- Error: Missing FileSetId tag
	 * 		
	 * @throws SAXException
	 * @throws IOException
	 */
	
	@Test
	public void testFileLevel_invalidTag() throws SAXException, IOException {
	
		File xmlFile = new File("./src/test/resources/sbm/schemaErrors/SBMI_FileLevelErrors_7.xml");
		String xmlString = readFile(xmlFile);
		LOG.info("xmlString:\n{}", xmlString);

		List<SBMErrorDTO> errors = sbmXMLValidator.validateSchemaForFileInfo(123L, xmlFile);
		LOG.info("errorCount: {}", errors.size());
		for(SBMErrorDTO err: errors) {
			LOG.info("error: {}", err);
		}
		Assert.assertTrue("Errors should exist", errors.size() == 2);
		Assert.assertTrue("RecordControlNumber error should exist", isErrorExists(errors, "abc", SBMErrorWarningCode.ER_014.getCode()));
		Assert.assertTrue("RecordControlNumber error should exist", isErrorExists(errors, "def", SBMErrorWarningCode.ER_014.getCode()));
		
	}
	
	
	/**
	 * Test record level errors
	 * @throws SAXException
	 * @throws IOException
	 */
	@Test
	public void testRecordLevel_shcemaErrors() throws SAXException, IOException {

		File xmlFile = new File("./src/test/resources/sbm/schemaErrors/SBMI_RecordLevelErrors_1.xml");
		String xmlString = readFile(xmlFile);
		LOG.info("xmlString:\n{}", xmlString);

		List<SBMErrorDTO> errors = sbmXMLValidator.validateSchemaForXPR(xmlString);
		LOG.info("errorCount: {}", errors.size());
		for(SBMErrorDTO err: errors) {
			LOG.info("error: {}", err);
		}
		Assert.assertTrue("Errors should exist", errors.size() == 9);
		
		Assert.assertTrue("Expected: Invalid format error for QHPId", isErrorExists(errors, SBMPolicyEnum.QHPID.getElementNm(), SBMErrorWarningCode.ER_004.getCode()));
		Assert.assertTrue("Expected: Invalid format error for PolicyStartDate", isErrorExists(errors, SBMPolicyEnum.PSD.getElementNm(), SBMErrorWarningCode.ER_004.getCode()));
		Assert.assertTrue("Expected: Invalid format error for EffectuationIndicator", isErrorExists(errors, SBMPolicyEnum.EFF_IND.getElementNm(), SBMErrorWarningCode.ER_004.getCode()));
		Assert.assertTrue("Expected: Invalid format error for InsuranceLineCode", isErrorExists(errors, SBMPolicyEnum.INSUR_LINE_CD.getElementNm(), SBMErrorWarningCode.ER_004.getCode()));
		Assert.assertTrue("Expected: Invalid format error for MemberStartDate", isErrorExists(errors, SBMPolicyEnum.MEM_START_DATE.getElementNm(), SBMErrorWarningCode.ER_004.getCode()));
		Assert.assertTrue("Expected: Invalid format error for MonthlyTotalPremiumAmount", isErrorExists(errors, SBMPolicyEnum.TPA.getElementNm(), SBMErrorWarningCode.ER_004.getCode()));
		Assert.assertTrue("Expected: Amount value exceeds error for MonthlyAPTCAmount", isErrorExists(errors, SBMPolicyEnum.APTC.getElementNm(), SBMErrorWarningCode.ER_009.getCode()));
		Assert.assertTrue("Expected: Invalid format error for MonthlyAPTCAmount", isErrorExists(errors, SBMPolicyEnum.APTC.getElementNm(), SBMErrorWarningCode.ER_004.getCode()));
		Assert.assertTrue("Expected: Invalid format error for MonthlyCSRAmount", isErrorExists(errors, SBMPolicyEnum.CSR.getElementNm(), SBMErrorWarningCode.ER_004.getCode()));
	}
	
	@Test
	public void testRecordLevel_shcemaErrors_er006() throws SAXException, IOException {

		File xmlFile = new File("./src/test/resources/sbm/schemaErrors/SBMI_RecordLevelErrors_6.xml");
		String xmlString = readFile(xmlFile);
		LOG.info("xmlString:\n{}", xmlString);

		List<SBMErrorDTO> errors = sbmXMLValidator.validateSchemaForXPR(xmlString);
		LOG.info("errorCount: {}", errors.size());
		for(SBMErrorDTO err: errors) {
			LOG.info("error: {}", err);
		}
		
		Assert.assertTrue("Expected: Invalid format error for QHPId", isErrorExists(errors, SBMPolicyEnum.QHPID.getElementNm(), SBMErrorWarningCode.ER_004.getCode()));
		Assert.assertTrue("Expected: Invalid format error for QHPId", isErrorExists(errors, SBMPolicyEnum.QHPID.getElementNm(), SBMErrorWarningCode.ER_006.getCode()));
		Assert.assertTrue("Expected: Invalid format error for EX_ASSIGN_POL_ID", isErrorExists(errors, SBMPolicyEnum.EX_ASSIGN_POL_ID.getElementNm(), SBMErrorWarningCode.ER_004.getCode()));
		Assert.assertTrue("Expected: Invalid format error for EX_ASSIGN_POL_ID", isErrorExists(errors, SBMPolicyEnum.EX_ASSIGN_POL_ID.getElementNm(), SBMErrorWarningCode.ER_006.getCode()));
		Assert.assertTrue("Expected: Invalid format error for EX_ASSIGN_SUB_ID", isErrorExists(errors, SBMPolicyEnum.EX_ASSIGN_SUB_ID.getElementNm(), SBMErrorWarningCode.ER_004.getCode()));
		Assert.assertTrue("Expected: Invalid format error for EX_ASSIGN_SUB_ID", isErrorExists(errors, SBMPolicyEnum.EX_ASSIGN_SUB_ID.getElementNm(), SBMErrorWarningCode.ER_006.getCode()));
		Assert.assertTrue("Expected: Invalid format error for EX_ASSIGN_MEM_ID", isErrorExists(errors, SBMPolicyEnum.EX_ASSIGN_MEM_ID.getElementNm(), SBMErrorWarningCode.ER_004.getCode()));
		Assert.assertTrue("Expected: Invalid format error for EX_ASSIGN_MEM_ID", isErrorExists(errors, SBMPolicyEnum.EX_ASSIGN_MEM_ID.getElementNm(), SBMErrorWarningCode.ER_006.getCode()));
		Assert.assertTrue("Expected: Invalid format error for MEM_LAST_NM", isErrorExists(errors, SBMPolicyEnum.MEM_LAST_NM.getElementNm(), SBMErrorWarningCode.ER_004.getCode()));
		Assert.assertTrue("Expected: Invalid format error for MEM_LAST_NM", isErrorExists(errors, SBMPolicyEnum.MEM_LAST_NM.getElementNm(), SBMErrorWarningCode.ER_006.getCode()));
		}
	
	/**
	 * Test record level errors related to Member information
	 * @throws SAXException
	 * @throws IOException
	 */
	@Test
	public void testRecordLevel_memberErrors() throws SAXException, IOException {

		File xmlFile = new File("./src/test/resources/sbm/schemaErrors/SBMI_RecordLevelErrors_2.xml"); //SBMI_ProratedTagExceeded.xml
//		File xmlFile = new File("./src/test/resources/sbm/schemaErrors/SBMI_RecordLevelErrors_3.xml"); //SBMI_ProratedTagExceeded.xml
		String xmlString = readFile(xmlFile);
		LOG.info("xmlString:\n{}", xmlString);

		List<SBMErrorDTO> errors = sbmXMLValidator.validateSchemaForXPR(xmlString);
		LOG.info("errorCount: {}", errors.size());
		for(SBMErrorDTO err: errors) {
			LOG.info("error: {}", err);
		}
		Assert.assertTrue("Errors should exist", errors.size() > 0);
		
//		Assert.assertTrue("Expected: Invalid format error for QHPId", isErrorExists(errors, SBMPolicyEnum.QHPID.getElementNm(), SBMErrorWarningCode.ER_004.getCode()));
//		Assert.assertTrue("Expected: Invalid format error for PolicyStartDate", isErrorExists(errors, SBMPolicyEnum.PSD.getElementNm(), SBMErrorWarningCode.ER_004.getCode()));
//		Assert.assertTrue("Expected: Invalid format error for EffectuationIndicator", isErrorExists(errors, SBMPolicyEnum.EFF_IND.getElementNm(), SBMErrorWarningCode.ER_004.getCode()));
//		Assert.assertTrue("Expected: Invalid format error for InsuranceLineCode", isErrorExists(errors, SBMPolicyEnum.INSUR_LINE_CD.getElementNm(), SBMErrorWarningCode.ER_004.getCode()));
//		Assert.assertTrue("Expected: Invalid format error for MemberStartDate", isErrorExists(errors, SBMPolicyEnum.MEM_START_DATE.getElementNm(), SBMErrorWarningCode.ER_004.getCode()));
//		Assert.assertTrue("Expected: Invalid format error for MonthlyTotalPremiumAmount", isErrorExists(errors, SBMPolicyEnum.TPA.getElementNm(), SBMErrorWarningCode.ER_004.getCode()));
//		Assert.assertTrue("Expected: Amount value exceeds error for MonthlyAPTCAmount", isErrorExists(errors, SBMPolicyEnum.APTC.getElementNm(), SBMErrorWarningCode.ER_009.getCode()));
//		Assert.assertTrue("Expected: Invalid format error for MonthlyAPTCAmount", isErrorExists(errors, SBMPolicyEnum.APTC.getElementNm(), SBMErrorWarningCode.ER_004.getCode()));
//		Assert.assertTrue("Expected: Invalid format error for MonthlyCSRAmount", isErrorExists(errors, SBMPolicyEnum.CSR.getElementNm(), SBMErrorWarningCode.ER_004.getCode()));
	}
	
	/**
	 * Test prorated tag element exceeded
	 * @throws SAXException
	 * @throws IOException
	 */
	@Test
	public void testRecordLevel_proratedTagExceeded() throws SAXException, IOException {

		File xmlFile = new File("./src/test/resources/sbm/schemaErrors/SBMI_RecordLevelErrors_4.xml"); 
		String xmlString = readFile(xmlFile);
		LOG.info("xmlString:\n{}", xmlString);

		List<SBMErrorDTO> errors = sbmXMLValidator.validateSchemaForXPR(xmlString);
		LOG.info("errorCount: {}", errors.size());
		for(SBMErrorDTO err: errors) {
			LOG.info("error: {}", err);
		}
		Assert.assertTrue("Errors should exist", errors.size() == 2);
		
	//	Assert.assertTrue("Expected: Invalid format error for QHPId", isErrorExists(errors, SBMPolicyEnum.PRORATED_AMOUNTS.getElementNm(), SBMErrorWarningCode.ER_001.getCode()));
//		Assert.assertTrue("Expected: Invalid format error for PolicyStartDate", isErrorExists(errors, SBMPolicyEnum.APTC.getElementNm(), SBMErrorWarningCode.ER_003.getCode()));
		
	}
	
	@Test
	public void testFileInfoUnmarshal() throws JAXBException, IOException, XMLStreamException {
		
		File xmlFile = new File("./src/test/resources/sbm/schemaErrors/SBMI_noSchemaErrors.xml");
		String xmlString = readFile(xmlFile);
		LOG.info("xmlString:\n{}", xmlString);
		 
		FileInformationType fileInfo = sbmXMLValidator.unmarshallSBMIFileInfo(xmlFile);
		Assert.assertTrue("Unmarshall should return FileInformationType object", fileInfo != null);
		LOG.info("FileInformationType: {}", fileInfo);
	}
	
	@Test
	public void testFileInfoMarshal() throws JAXBException, IOException, XMLStreamException {
		
		File xmlFile = new File("./src/test/resources/sbm/schemaErrors/SBMI_noSchemaErrors.xml");
		String xmlString = readFile(xmlFile);
		LOG.info("xmlString:\n{}", xmlString);
		 
		FileInformationType fileInfo = sbmXMLValidator.unmarshallSBMIFileInfo(xmlFile);
		String fileInfoString = sbmXMLValidator.marshallFileInfo(fileInfo);
		
		Assert.assertTrue("Unmarshall should return FileInformationType object", fileInfoString != null);
		
	}
	
	/**
	 * Test record level errors
	 * @throws SAXException
	 * @throws IOException
	 */
//	@Test
	public void testRecordLevel_externalInputFile() throws SAXException, IOException {

		File xmlFile = new File("C:\\SBMFiles\\testFiles\\defects\\CA1.EPS.SBMI.D160707.T100000035_21838.T");
		String xmlString = readFile(xmlFile);
		LOG.info("xmlString:\n{}", xmlString);

		List<SBMErrorDTO> errors = sbmXMLValidator.validateSchemaForXPR(xmlString);
		LOG.info("errorCount: {}", errors.size());
		for(SBMErrorDTO err: errors) {
			LOG.info("error: {}", err);
		}
		
		Assert.assertTrue("Should be always true", isErrorExists(errors, SBMPolicyEnum.QHPID.getElementNm(), SBMErrorWarningCode.ER_004.getCode()));
		
	}
	
	public static boolean isErrorExists(List<SBMErrorDTO> errorList, String elementInError, String errorCode) {
		for(SBMErrorDTO error: errorList) {
			if(elementInError.equalsIgnoreCase(error.getElementInErrorNm()) 
					&& errorCode.equalsIgnoreCase(error.getSbmErrorWarningTypeCd())) {
				return true;
			}
		}
		
		return false;
	}
	

	@Test
	public void test_UnsupportedZipFormat() throws SAXException, IOException, ParserConfigurationException {

		File xmlFile = new File("./src/test/resources/sbm/schemaErrors/SBMI.VT0.D160809.T120000002.T");
		String xmlString = readFile(xmlFile);
		LOG.info("xmlString:\n{}", xmlString);

		boolean errorExist = sbmXMLValidator.isValidXML(xmlFile);
		LOG.info("errorExist: {}", errorExist);

		Assert.assertFalse("One error should exists", errorExist);
	}
	
	@Test
	public void test_UnsupportedFormat() throws SAXException, IOException, ParserConfigurationException {

		File xmlFile = new File("./src/test/resources/sbm/schemaErrors/SBMI_FileLevelErrors_InvalidSchema.xml");
		String xmlString = readFile(xmlFile);
		LOG.info("xmlString:\n{}", xmlString);

		boolean errorExist = sbmXMLValidator.isValidXML(xmlFile);
		LOG.info("errorExist: {}", errorExist);

		Assert.assertFalse("One error should exists", errorExist);
	}
	
	@Test
	public void test_fatalError() {
		SAXParseException saxException = new SAXParseException(
				"errorMsg", "publicId", "systemId", 100, 25);
		
		SBMCustomHandler handler = new SBMCustomHandler();
		LOG.info(handler.getCurrentElement());
		
		handler.fatalError(saxException);
		
		Assert.assertFalse("Schema errors list not empty", handler.getSchemaErrorList().isEmpty());
		
	}
	
	@Test
	public void test_warning() {
		SAXParseException saxException = new SAXParseException(
				"errorMsg", "publicId", "systemId", 100, 25);
		
		SBMCustomHandler handler = new SBMCustomHandler();
		LOG.info(handler.getCurrentElement());
		
		handler.warning(saxException);
		
		Assert.assertTrue("Schema errors list empty in case of warnings", handler.getSchemaErrorList().isEmpty());
		
	}
	
	@Test
	public void test_SBMSchemaValidationRuleEnum() {
		
		SBMSchemaValidationRuleEnum schemaValidationRuleEnum = SBMSchemaValidationRuleEnum.findRule("cvc-length-valid");
		Assert.assertEquals("SBMSchemaValidationRuleEnum", "cvc-length-valid", schemaValidationRuleEnum.getValue());
	}
	
}
