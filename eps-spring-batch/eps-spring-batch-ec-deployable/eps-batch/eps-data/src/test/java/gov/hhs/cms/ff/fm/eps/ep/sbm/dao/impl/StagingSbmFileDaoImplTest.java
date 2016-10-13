package gov.hhs.cms.ff.fm.eps.ep.sbm.dao.impl;

import java.io.StringReader;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import gov.cms.dsh.sbmi.Enrollment;
import gov.hhs.cms.ff.fm.eps.ep.dao.StagingSbmFileDao;
import gov.hhs.cms.ff.fm.eps.ep.enums.EProdEnum;
import gov.hhs.cms.ff.fm.eps.ep.po.StagingSbmFilePO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileProcessingDTO;
import gov.hhs.cms.ff.fm.eps.ep.util.sbm.TestDataSBMUtility;
import gov.hhs.cms.ff.fm.eps.ep.vo.UserVO;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/sbmi-data-config.xml", "classpath:/test-context-data.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class StagingSbmFileDaoImplTest extends BaseSBMDaoTest {


	@Autowired
	private StagingSbmFileDao stagingSbmFileDao;
	
	@Autowired
	private UserVO userVO;

	private static Unmarshaller unmarshaller;

	static {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(String.class, Enrollment.class);
			unmarshaller = jaxbContext.createUnmarshaller();

		} catch (JAXBException ex) {
			System.out.println("Unable to create Unmarshaller for unit test.  " +  ex.getMessage());
		}
	}

	@Test
	public void test_insertFile() throws JAXBException {

		assertNotNull("sbmFileInfoDao", stagingSbmFileDao);
		int expectedListSize = 1;
		Long fileId = TestDataSBMUtility.getRandomNumberAsLong(8);
		String tenantId = "TX0";
		int covYr = YEAR;
		String issuerId = TestDataSBMUtility.getRandomNumberAsString(5);
		Enrollment enrollment = TestDataSBMUtility.makeEnrollment(fileId, tenantId, covYr, issuerId, TestDataSBMUtility.FILES_ONE_PER_ISSUER);

		String exchangePolicyId = TestDataSBMUtility.getRandomNumberAsString(9);
		for (int i = 1; i < 5; ++i) {
			String qhpId = issuerId + tenantId + String.format("%04d", i) + "01";
			enrollment.getPolicy().add(TestDataSBMUtility.makePolicyType(i, qhpId, exchangePolicyId + String.format("%03d", i)));
		}

		String expectedSbmXML = TestDataSBMUtility.getEnrollmentAsXmlString(enrollment);
		StagingSbmFilePO expected = new StagingSbmFilePO();
		expected.setSbmXML(expectedSbmXML);
		Long batchId = TestDataSBMUtility.getRandomNumberAsLong(8);
		expected.setBatchId(batchId);
		userVO.setUserId(batchId.toString());

		Long sbmFileProcSumId = insertSBMFileProcessingSummary(tenantId);
		expected.setSbmFileProcessingSummaryId(sbmFileProcSumId);
		Long sbmFileInfoId = insertSBMFileInfo(sbmFileProcSumId, "FILEID-" + fileId.toString());
		expected.setSbmFileInfoId(sbmFileInfoId);


		stagingSbmFileDao.insertFileToStaging(expected);

		// Go and get what was just put in.
		String sql = "SELECT ssf.SBMXML.GETCLOBVAL() AS SBMXML, CREATEBY, LASTMODIFIEDBY FROM STAGINGSBMFILE ssf " +
				"WHERE ssf.BATCHID = " + batchId + " AND ssf.SBMFILEINFOID = " + sbmFileInfoId +
				" AND ssf.SBMFILEPROCESSINGSUMMARYID = " + sbmFileProcSumId;

		List<Map<String, Object>> actualList = jdbc.queryForList(sql);
		assertEquals("STAGINGSBMFILE record list size", expectedListSize, actualList.size());

		Map<String, Object> row = actualList.get(0);
		String strSbmXML = (String) row.get("SBMXML");
		assertNotNull("SBMXML", strSbmXML);

		StringReader sr = new StringReader(strSbmXML);
		JAXBElement<Enrollment> element = unmarshaller.unmarshal(new StreamSource(sr), Enrollment.class);
		Enrollment actualEnrollment = element.getValue();

		assertEquals("TenantId from SBMXML", tenantId, actualEnrollment.getFileInformation().getTenantId());
		assertEquals("CoverageYear from SBMXML", covYr, actualEnrollment.getFileInformation().getCoverageYear());
		assertEquals("IssuerId from SBMXML", issuerId, actualEnrollment.getFileInformation().getIssuerFileInformation().getIssuerId());

		assertEquals("CreateBy", batchId.toString(), (String) row.get("CREATEBY"));
		assertEquals("LastModifiedBy", batchId.toString(), (String) row.get("LASTMODIFIEDBY"));

	}


	@Test
	public void test_deleteStagingSbmFile_Exception() { 

		Class<?> expectedEx = com.accenture.foundation.common.exception.ApplicationException.class;
		String expectedCd = EProdEnum.EPROD_10.getCode();
		Long sbmFileProcSumId = null;
		try {
			stagingSbmFileDao.deleteStagingSbmFile(sbmFileProcSumId);
		} catch (Exception ex) {
			assertEquals("Exception thrown", expectedEx, ex.getClass());
			assertEquals("EPROD", expectedCd, ex.getMessage());
		}
	}


	@Test
	public void test_insertFileToStaging_Exception() { 

		Class<?> expectedEx = com.accenture.foundation.common.exception.ApplicationException.class;
		String expectedCd = EProdEnum.EPROD_10.getCode();
		StagingSbmFilePO po = null;
		try {
			stagingSbmFileDao.insertFileToStaging(po);
		} catch (Exception ex) {
			assertEquals("Exception thrown", expectedEx, ex.getClass());
			assertEquals("EPROD", expectedCd, ex.getMessage());
		}
	}



//	@Test
//	public void test_getStagingPolicies() {
//
//		int expectedListSize = 3;
//
//		String tenantId = TestDataSBMUtility.getRandomSbmState() + "0";		
//		Long fileId = TestDataSBMUtility.getRandomNumberAsLong(8);
//		Long batchId = TestDataSBMUtility.getRandomNumberAsLong(4);
//		
//		SBMFileProcessingDTO fileDTO = insertParentFileRecords(tenantId, fileId.toString());
//		Long sbmFileProcSumId = fileDTO.getSbmFileProcSumId();
//
//		Enrollment enrollment = new Enrollment();
//
//		enrollment.getPolicy().add(TestDataSBMUtility.makePolicyType(tenantId, "EXPOLID-1111"));
//		enrollment.getPolicy().add(TestDataSBMUtility.makePolicyType(tenantId, "EXPOLID-2222"));
//		enrollment.getPolicy().add(TestDataSBMUtility.makePolicyType(tenantId, "EXPOLID-3333"));
//
//		String sbmFileXML = TestDataSBMUtility.getEnrollmentAsXmlString(enrollment);
//
//		// set up some data, insert the Enrollment XML into staging		
//		String sql = "INSERT INTO STAGINGSBMFILE (SBMXML, BATCHID, SBMFILEPROCESSINGSUMMARYID, SBMFILEINFOID) " +
//				"VALUES (XMLTYPE('" + sbmFileXML + "'), " + batchId + ", " + sbmFileProcSumId + 
//				", " + fileDTO.getSbmFileInfo().getSbmFileInfoId() + ")";
//		jdbc.execute(sql);
//		
//		sql = "INSERT INTO STAGINGSBMGROUPLOCK (SBMFILEPROCESSINGSUMMARYID, PROCESSINGGROUPID, BATCHID) " +
//				"VALUES ('" + sbmFileProcSumId + "', " + 101 + ", " + batchId + ")";
//		jdbc.execute(sql);
//
//		List<String> actualList = stagingSbmFileDao.getStagingPolicies(batchId);
//		
//		assertEquals("count POLICIES extracted STAGINGSBMFILE record", expectedListSize, actualList.size());
//
//
//	}

}
