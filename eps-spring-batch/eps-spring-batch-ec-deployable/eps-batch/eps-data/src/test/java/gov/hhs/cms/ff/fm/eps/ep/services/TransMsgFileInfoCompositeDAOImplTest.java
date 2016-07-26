package gov.hhs.cms.ff.fm.eps.ep.services;

import gov.cms.dsh.bem.BenefitEnrollmentMaintenanceType;
import gov.cms.dsh.bem.BenefitEnrollmentRequest;
import gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentRequestDTO;
import gov.hhs.cms.ff.fm.eps.ep.data.util.TestDataUtil;
import gov.hhs.cms.ff.fm.eps.ep.enums.ExchangeType;
import gov.hhs.cms.ff.fm.eps.ep.services.impl.TransMsgFileInfoCompositeDAOImpl;
import gov.hhs.cms.ff.fm.eps.ep.util.EpsDateUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;



@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/eps-data-config.xml", "classpath:/test-context-data.xml"})
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class TransMsgFileInfoCompositeDAOImplTest extends BaseServicesTest {

	@Autowired
	private TransMsgFileInfoCompositeDAOImpl transMsgFileInfoService; 

	@Autowired
	public JdbcTemplate jdbc;


	/*
	 * Tests saveFileInfo in TransMsgFileInfoCompositeDAO.
	 *  - Tests DTO to PO to DB back to PO
	 */
	@Test
	public void testSaveFileInfo_HappyPath() throws Exception {

		BenefitEnrollmentRequestDTO berDTO = new BenefitEnrollmentRequestDTO();
		BenefitEnrollmentRequest ber = new BenefitEnrollmentRequest();
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		
		ber.getBenefitEnrollmentMaintenance().add(bem);
		berDTO.setBer(ber);

		Long id = TestDataUtil.getRandomNumber(9);
		String groupSenderId = id.toString();

		berDTO.setFileInformation(TestDataUtil.makeFileInformationType(id, groupSenderId));
		berDTO.setFileInfoXml(TestDataUtil.makeFileInformationTypeAsStringXML(id));
		berDTO.setBatchId(id);
		berDTO.setBerXml(TestDataUtil.getBerXMLAsString(ber));

		// for coverage
		JAXBElement<BenefitEnrollmentMaintenanceType> bemJaxB = 
				new JAXBElement<BenefitEnrollmentMaintenanceType>(new QName("BenefitEnrollmentMaintenance"), 
						BenefitEnrollmentMaintenanceType.class, new BenefitEnrollmentMaintenanceType());
		berDTO.setJaxbElement(bemJaxB);

		System.out.print("\n\n" + berDTO.toString()+"\n\n");
		assertNotNull("berDTO.toString method (for coverage)", berDTO.toString());

		Long transMsgFileInfoId = transMsgFileInfoService.saveFileInfo(berDTO);

		String sql = "select e.TRANSMSGFILEINFOID, e.fileInfoXml.getClobval() AS poXML, e.CREATEDATETIME,e.LASTMODIFIEDDATETIME "
				+ "from TRANSMSGFILEINFO e where TransMsgFileInfoId = " + transMsgFileInfoId;

		List<Map<String, Object>> actualList = jdbc.queryForList(sql);
		
		assertEquals("actual TransMsgFileInfoPO row insert", 1, actualList.size());

		Map<String, Object> row = actualList.get(0);

		assertNotNull("transMsgFileInfoId", row.get("transMsgFileInfoId"));
        assertEquals("FileInfoXml", TestDataUtil.prettyFormat(berDTO.getFileInfoXml()),  TestDataUtil.prettyFormat((String) row.get("poXML")));
     
		// Create and Modified stuff created in mappers.
		assertNotNull("CreateDateTime", (Date) row.get("createDateTime"));
		assertNotNull("LastModifiedDateTime", (Date) row.get("lastModifiedDateTime"));		
	}


	/*
	 * Tests saveFileInfo in TransMsgFileInfoCompositeDAO.
	 *  - Tests DTO to PO to DB back to PO
	 *  - Tests data set in ALL columns
	 */
	@Test
	public void testSaveFileInfo_AllData() {

		Long bemId = TestDataUtil.getRandomNumber(9);
		String gpn = "66666666";
		
		BenefitEnrollmentRequestDTO berDTO = new BenefitEnrollmentRequestDTO();
		BenefitEnrollmentRequest ber = new BenefitEnrollmentRequest();
		BenefitEnrollmentMaintenanceType bem = TestDataUtil.makeBenefitEnrollmentMaintenanceType(bemId, gpn);
		
		ber.getBenefitEnrollmentMaintenance().add(bem);
		berDTO.setBer(ber);
		berDTO.setExchangeTypeCd(ExchangeType.FFM.getValue());

		Long transMsgFileInfoId = null;
		Long id = TestDataUtil.getRandomNumber(9);

		berDTO.setFileInformation(TestDataUtil.makeFileInformationType(id, id.toString()));
		berDTO.setFileInfoXml(TestDataUtil.makeFileInformationTypeAsStringXML(id));
		berDTO.setBatchId(id);
		berDTO.setFileNm("fileNn" + id + ".xml");
		
		berDTO.setBerXml(TestDataUtil.getBerXMLAsString(ber));

		transMsgFileInfoId = transMsgFileInfoService.saveFileInfo(berDTO);

		// Retrieve data just inserted
		
		String sql = "select e.TRANSMSGFILEINFOID, e.fileInfoXml.getClobval() AS poXML, e.GROUPSENDERID,e.GROUPRECEIVERID,e.FILENM, "
				+ "e.GROUPTIMESTAMPDATETIME,e.GROUPCONTROLNUM,e.VERSIONNUM, e.transMsgOriginTypeCd, e.CREATEDATETIME,e.LASTMODIFIEDDATETIME,e.CREATEBY,e.LASTMODIFIEDBY "
				+"from TRANSMSGFILEINFO e where TransMsgFileInfoId = " + transMsgFileInfoId;

		List<Map<String, Object>> actualList = jdbc.queryForList(sql);
		
		assertEquals("actual TransMsgFileInfoPO row insert", 1, actualList.size());

		Map<String, Object> row = actualList.get(0);

		// Test DTO data against retrieved PO data from DB
		assertNotNull("transMsgFileInfoId", row.get("transMsgFileInfoId"));
        assertEquals("FileInfoXml", TestDataUtil.prettyFormat(berDTO.getFileInfoXml()),  TestDataUtil.prettyFormat((String) row.get("poXML")));
        
		assertEquals("GroupSenderID", berDTO.getFileInformation().getGroupSenderID(), (String) row.get("GroupSenderId"));
		assertEquals("GroupReceiverID", berDTO.getFileInformation().getGroupReceiverID(), (String) row.get("GroupReceiverId"));
		assertEquals("FileName", berDTO.getFileNm(), (String) row.get("fileNm"));
		Date gtsDt = (Date) row.get("groupTimestampDateTime");
		assertEquals("GroupTimestampDateTime", EpsDateUtils.getDateTimeFromXmlGC(berDTO.getFileInformation().getGroupTimeStamp()),
				new DateTime(gtsDt.getTime()));
		assertEquals("GroupControlNum", berDTO.getFileInformation().getGroupControlNumber(), (String) row.get("groupControlNum"));
		assertEquals("VersionNumber", berDTO.getFileInformation().getVersionNumber(), (String) row.get("VersionNum"));
		assertEquals("transMsgOriginTypeCd", berDTO.getExchangeTypeCd(), (String) row.get("transMsgOriginTypeCd"));

		// Create and Modified stuff created in mappers.
		assertNotNull("CreateDateTime", (Date) row.get("createDateTime"));
		assertNotNull("LastModifiedDateTime", (Date) row.get("lastModifiedDateTime"));
		assertEquals("CreateBy (batchId)", id.toString(), (String) row.get("CreateBy"));
		assertEquals("LastModifiedBy (batchId)", id.toString(), (String) row.get("LastModifiedBy"));
	}

	@Test
	public void testFileInfo_NullBatchId() {

		Long transMsgFileInfoId = null;
		BenefitEnrollmentRequestDTO berDTO = new BenefitEnrollmentRequestDTO();
		berDTO.setBerXml("<BER/>");
		Long id = TestDataUtil.getRandom3DigitNumber();
		berDTO.setFileInfoXml(getXmlString(id.toString()));
		transMsgFileInfoId =  transMsgFileInfoService.saveFileInfo(berDTO);
		assertNotNull("transMsgFileInfoId", transMsgFileInfoId);
	} 


	/*
	 * Tests calling saveFileInfo and attempting to insert actual data into EPS
	 * with data set to null in a non-nullable field, which is of XMLTYPE in the table.
	 *  - If data is null, test should throw DataIntegrityViolationException since FILEINFOXML is set to "NULL = NO" in db.
	 */
	@Test(expected=com.accenture.foundation.common.exception.ApplicationException.class)
	public void testSaveFileInfo_Exception_NonNullableField() {

		BenefitEnrollmentRequestDTO berDTO = new BenefitEnrollmentRequestDTO();

		Long id = TestDataUtil.getRandomNumber(9);

		berDTO.setFileInformation(TestDataUtil.makeFileInformationType(id, id.toString()));
		// Set FileInfoXml (not nullable in db to throw SQLException)
		berDTO.setFileInfoXml(null);
		berDTO.setBatchId(id);
		berDTO.setFileNm("fileNn" + id + ".xml");
		assertNotNull("BemDTO not null", berDTO);
		Long transMsgFileInfoId = transMsgFileInfoService.saveFileInfo(berDTO);
		assertNull("transMsgFileInfoId", transMsgFileInfoId);

	}


	/*
	 * Tests calling saveFileInfo and attempting to insert actual data into EPS
	 * with data that is NOT XML.
	 *  - If data is NOT XML, test should throw UncategorizedSQLException since FILEINFOXML is of XMLTYPE in db.
	 */
	@Test(expected=com.accenture.foundation.common.exception.ApplicationException.class)
	public void testSaveFileInfo_Exception_XMLDataType() {

		BenefitEnrollmentRequestDTO berDTO = new BenefitEnrollmentRequestDTO();

		Long id = TestDataUtil.getRandomNumber(9);

		berDTO.setFileInformation(TestDataUtil.makeFileInformationType(id, id.toString()));
		// Set FileInfoXml to something other than XML
		berDTO.setFileInfoXml("?<THIS IS NOT XML/>!");
		berDTO.setBatchId(id);
		berDTO.setFileNm("fileNn" + id + ".xml");
		assertNotNull(
				"This assert is to avoid Sonar violation. The real assert on the expected result is in annotation!"
				, transMsgFileInfoService);
		transMsgFileInfoService.saveFileInfo(berDTO);
	}
	
	
	private String getXmlString(String strNum) {

		String xml = "<BenefitEnrollmentRequest><BenefitEnrollmentMaintenance>" +
				"<ControlNumber>" + strNum + "</ControlNumber>" +
				"<CurrentTimeStamp>" + getCurrentSqlTimeStamp() + "</CurrentTimeStamp>" +
				"</BenefitEnrollmentMaintenance></BenefitEnrollmentRequest>";
		return xml;
	}


}
