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
import gov.hhs.cms.ff.fm.eps.ep.po.StagingSbmFilePO;
import gov.hhs.cms.ff.fm.eps.ep.util.sbm.TestDataSBMUtility;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/sbmi-data-config.xml", "classpath:/test-context-data.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class StagingSbmFileDaoImplTest extends BaseSBMDaoTest {


	@Autowired
	StagingSbmFileDao stagingSbmFileDao;

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


}
