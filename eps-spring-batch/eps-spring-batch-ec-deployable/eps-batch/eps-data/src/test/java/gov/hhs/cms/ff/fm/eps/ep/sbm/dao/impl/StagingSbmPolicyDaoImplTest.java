package gov.hhs.cms.ff.fm.eps.ep.sbm.dao.impl;

import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
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
import gov.cms.dsh.sbmi.PolicyType;
import gov.hhs.cms.ff.fm.eps.ep.dao.StagingSbmPolicyDao;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileProcessingDTO;
import gov.hhs.cms.ff.fm.eps.ep.util.sbm.TestDataSBMUtility;
import gov.hhs.cms.ff.fm.eps.ep.vo.UserVO;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/sbmi-data-config.xml", "classpath:/test-context-data.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class StagingSbmPolicyDaoImplTest extends BaseSBMDaoTest {


	@Autowired
	StagingSbmPolicyDao stagingSbmPolicyDao;

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
	public void test_extractXprFromStagingSbmFile() throws JAXBException {

		final int GROUPINGSIZE = 5;
		int expectedListSize = 14;
		List<String> expectedQhpIds = new ArrayList<String>();
		List<String> expectedExchangePolicyIds = new ArrayList<String>();
		String state = TestDataSBMUtility.getRandomSbmState();
		String tenantId = state + "0";
		Long batchId = TestDataSBMUtility.getRandomNumberAsLong(8);

		Long fileId = TestDataSBMUtility.getRandomNumberAsLong(8);

		SBMFileProcessingDTO expectedDTO = insertParentFileRecords(tenantId, fileId.toString());

		// Make an enrollment with policies without members.
		int covYr = YEAR;
		String issuerId = TestDataSBMUtility.getRandomNumberAsString(5);
		Enrollment enrollment = TestDataSBMUtility.makeEnrollment(fileId, tenantId, covYr, issuerId, TestDataSBMUtility.FILES_ONE_PER_ISSUER);

		String exchangePolicyIdPrefix = TestDataSBMUtility.getRandomNumberAsString(9);
		for (int i = 1; i <= expectedListSize; ++i) {
			String qhpId = issuerId + tenantId + String.format("%04d", i) + "01";
			String exchangePolicyId = exchangePolicyIdPrefix + String.format("%03d", i);
			PolicyType policy = new PolicyType();
			policy.setExchangeAssignedPolicyId(exchangePolicyId);
			policy.setQHPId(qhpId);
			policy.setRecordControlNumber(i);
			enrollment.getPolicy().add(policy);//TestDataSBMUtility.makePolicyType(i, qhpId, exchangePolicyId));

			expectedQhpIds.add(qhpId);
			expectedExchangePolicyIds.add(exchangePolicyId);			
		}

		String sbmFileXML = TestDataSBMUtility.getEnrollmentAsXmlString(enrollment);

		// insert the Enrollment XML into staging		
		String sql = "INSERT INTO STAGINGSBMFILE (SBMXML, BATCHID, SBMFILEPROCESSINGSUMMARYID, SBMFILEINFOID) " +
				"VALUES (XMLTYPE('" + sbmFileXML + "'), " + batchId + ", " + expectedDTO.getSbmFileProcSumId() + 
				", " + expectedDTO.getSbmFileInfo().getSbmFileInfoId() + ")";

		jdbc.execute(sql);
		
		// insert the lock record into stagingGroupLock		
		String sqlLock = "INSERT INTO STAGINGSBMGROUPLOCK (SBMFILEPROCESSINGSUMMARYID, PROCESSINGGROUPID, BATCHID) " +
				"VALUES ('" + expectedDTO.getSbmFileProcSumId() + "', " + 1 + ", " + batchId + ")";

		jdbc.execute(sqlLock);

		UserVO userVO = new UserVO(batchId.toString());
		int cntRows = stagingSbmPolicyDao.extractXprFromStagingSbmFile(GROUPINGSIZE, state, userVO);

		assertEquals("row count", expectedListSize, cntRows);

		sql = "SELECT ssp.SBMPOLICYXML.GETCLOBVAL() AS XPRXML, ssp.PROCESSINGGROUPID, SUBSCRIBERSTATECD, CREATEBY, LASTMODIFIEDBY FROM STAGINGSBMPOLICY ssp WHERE " + 
				"ssp.SBMFILEPROCESSINGSUMMARYID = " + expectedDTO.getSbmFileProcSumId() + " ORDER BY STAGINGSBMPOLICYID ASC";

		List<Map<String, Object>> actualList = jdbc.queryForList(sql);
		assertEquals("STAGINGSBMFILE record list size", expectedListSize, actualList.size());


		for (int i = 1; i <= actualList.size(); ++i) {

			Map<String, Object> row = actualList.get(i-1);
			String msg = "Record " + i + ")  ";
			String strXprXML = (String) row.get("XPRXML");
			assertNotNull(msg + "XPRXML", strXprXML);

			StringReader sr = new StringReader(strXprXML);
			JAXBElement<PolicyType> element = unmarshaller.unmarshal(new StreamSource(sr), PolicyType.class);
			PolicyType actualXPR = element.getValue();

			BigDecimal actualGroupInd = (BigDecimal) row.get("PROCESSINGGROUPID");

			double value = ((double) i) / GROUPINGSIZE;
			double expectedGroupIndDbl = Math.ceil(value) +100;

			assertEquals(msg + "ProcessingGroupInd", expectedGroupIndDbl, actualGroupInd.doubleValue());

			assertEquals(msg + "QHPId from XPRXML", expectedQhpIds.get(i-1), actualXPR.getQHPId());
			assertEquals(msg + "ExchangePolicyId from XPRXML", expectedExchangePolicyIds.get(i-1), actualXPR.getExchangeAssignedPolicyId());

			assertEquals("SubscriberStateCd", state, (String) row.get("SUBSCRIBERSTATECD"));
			assertEquals("CreateBy", batchId.toString(), (String) row.get("CREATEBY"));
			assertEquals("LastModifiedBy", batchId.toString(), (String) row.get("LASTMODIFIEDBY"));

		}
	}
	
	@Test
	public void test_extractXprFromStagingSbmFile_One_Group() throws JAXBException {

		final int GROUPINGSIZE = 50000;
		int expectedListSize = 5;
		
		BigDecimal expectedGroupIndDbl = BigDecimal.valueOf(101);
		
		List<String> expectedQhpIds = new ArrayList<String>();
		List<String> expectedExchangePolicyIds = new ArrayList<String>();
		String state = TestDataSBMUtility.getRandomSbmState();
		String tenantId = state + "0";
		Long batchId = TestDataSBMUtility.getRandomNumberAsLong(8);

		Long fileId = TestDataSBMUtility.getRandomNumberAsLong(8);

		SBMFileProcessingDTO expectedDTO = insertParentFileRecords(tenantId, fileId.toString());

		// Make an enrollment with policies without members.
		int covYr = YEAR;
		String issuerId = TestDataSBMUtility.getRandomNumberAsString(5);
		Enrollment enrollment = TestDataSBMUtility.makeEnrollment(fileId, tenantId, covYr, issuerId, TestDataSBMUtility.FILES_ONE_PER_ISSUER);

		String exchangePolicyIdPrefix = TestDataSBMUtility.getRandomNumberAsString(9);
		for (int i = 1; i <= expectedListSize; ++i) {
			String qhpId = issuerId + tenantId + String.format("%04d", i) + "01";
			String exchangePolicyId = exchangePolicyIdPrefix + String.format("%03d", i);
			PolicyType policy = new PolicyType();
			policy.setExchangeAssignedPolicyId(exchangePolicyId);
			policy.setQHPId(qhpId);
			policy.setRecordControlNumber(i);
			enrollment.getPolicy().add(policy);//TestDataSBMUtility.makePolicyType(i, qhpId, exchangePolicyId));

			expectedQhpIds.add(qhpId);
			expectedExchangePolicyIds.add(exchangePolicyId);			
		}

		String sbmFileXML = TestDataSBMUtility.getEnrollmentAsXmlString(enrollment);

		// insert the Enrollment XML into staging		
		String sql = "INSERT INTO STAGINGSBMFILE (SBMXML, BATCHID, SBMFILEPROCESSINGSUMMARYID, SBMFILEINFOID) " +
				"VALUES (XMLTYPE('" + sbmFileXML + "'), " + batchId + ", " + expectedDTO.getSbmFileProcSumId() + 
				", " + expectedDTO.getSbmFileInfo().getSbmFileInfoId() + ")";

		jdbc.execute(sql);
		
		// insert the lock record into stagingGroupLock		
		String sqlLock = "INSERT INTO STAGINGSBMGROUPLOCK (SBMFILEPROCESSINGSUMMARYID, PROCESSINGGROUPID, BATCHID) " +
				"VALUES ('" + expectedDTO.getSbmFileProcSumId() + "', " + 1 + ", " + batchId + ")";

		jdbc.execute(sqlLock);

		UserVO userVO = new UserVO(batchId.toString());
		int cntRows = stagingSbmPolicyDao.extractXprFromStagingSbmFile(GROUPINGSIZE, state, userVO);

		assertEquals("row count", expectedListSize, cntRows);

		sql = "SELECT ssp.SBMPOLICYXML.GETCLOBVAL() AS XPRXML, ssp.PROCESSINGGROUPID, SUBSCRIBERSTATECD, CREATEBY, LASTMODIFIEDBY FROM STAGINGSBMPOLICY ssp WHERE " + 
				"ssp.SBMFILEPROCESSINGSUMMARYID = " + expectedDTO.getSbmFileProcSumId() + " ORDER BY STAGINGSBMPOLICYID ASC";

		List<Map<String, Object>> actualList = jdbc.queryForList(sql);
		assertEquals("STAGINGSBMFILE record list size", expectedListSize, actualList.size());


		for (int i = 1; i <= actualList.size(); ++i) {

			Map<String, Object> row = actualList.get(i-1);
			String msg = "Record " + i + ")  ";
			String strXprXML = (String) row.get("XPRXML");
			assertNotNull(msg + "XPRXML", strXprXML);

			StringReader sr = new StringReader(strXprXML);
			JAXBElement<PolicyType> element = unmarshaller.unmarshal(new StreamSource(sr), PolicyType.class);
			PolicyType actualXPR = element.getValue();

			BigDecimal actualGroupInd = (BigDecimal) row.get("PROCESSINGGROUPID");			

			assertEquals(msg + "ProcessingGroupInd", expectedGroupIndDbl, actualGroupInd);

			assertEquals(msg + "QHPId from XPRXML", expectedQhpIds.get(i-1), actualXPR.getQHPId());
			assertEquals(msg + "ExchangePolicyId from XPRXML", expectedExchangePolicyIds.get(i-1), actualXPR.getExchangeAssignedPolicyId());

			assertEquals("SubscriberStateCd", state, (String) row.get("SUBSCRIBERSTATECD"));
			assertEquals("CreateBy", batchId.toString(), (String) row.get("CREATEBY"));
			assertEquals("LastModifiedBy", batchId.toString(), (String) row.get("LASTMODIFIEDBY"));

		}
	}


}
