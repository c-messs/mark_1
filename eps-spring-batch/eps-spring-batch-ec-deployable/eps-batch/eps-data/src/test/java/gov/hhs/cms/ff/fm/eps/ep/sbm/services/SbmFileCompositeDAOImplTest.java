package gov.hhs.cms.ff.fm.eps.ep.sbm.services;

import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
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
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMErrorDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileProccessingSummary;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileProcessingDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMSummaryAndFileInfoDTO;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;
import gov.hhs.cms.ff.fm.eps.ep.util.sbm.TestDataSBMUtility;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/sbmi-data-config.xml", "classpath:/test-context-data.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class SbmFileCompositeDAOImplTest extends BaseSbmServicesTest {


	@Autowired
	SBMFileCompositeDAO sbmFileCompositeDao;
	
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
	public void test_saveFileToStaging() throws JAXBException {

		int expectedListSize = 1;
		Long sbmFileId = TestDataSBMUtility.getRandomNumberAsLong(10);
		int covYr = YEAR;
		String issuerId = TestDataSBMUtility.getRandomNumberAsString(5);
		String sbmFileNm = "FILNAME-" + issuerId;
		int issuerFileType = TestDataSBMUtility.FILES_FILESET;
		String fileSetId = "FSID-" + TestDataSBMUtility.getRandomNumber(3);
		String tenantId = "NY0";
		SBMFileProcessingDTO inboundFileDTO = insertParentFileRecords(tenantId, sbmFileId.toString());
		
		Enrollment expectedEnrollment = TestDataSBMUtility.makeEnrollment(sbmFileId, tenantId, covYr, issuerId, issuerFileType);
		
		expectedEnrollment.getFileInformation().getIssuerFileInformation().setIssuerFileSet(TestDataSBMUtility.makeIssuerFileSet(fileSetId, 1, 10));
		inboundFileDTO.setBatchId(TestDataSBMUtility.getRandomNumberAsLong(8));

		String fileInfoXML = TestDataSBMUtility.getFileInfoTypeAsXmlString(expectedEnrollment.getFileInformation());
		inboundFileDTO.setFileInfoXML(fileInfoXML);
		inboundFileDTO.setFileInfoType(expectedEnrollment.getFileInformation());
		
		String sbmXML = TestDataSBMUtility.getEnrollmentAsXmlString(expectedEnrollment);
		inboundFileDTO.setSbmFileXML(sbmXML);
		
		inboundFileDTO.getSbmFileInfo().setSbmFileNm(sbmFileNm);
		inboundFileDTO.getSbmFileInfo().setTradingPartnerId("BOB");
		
		sbmFileCompositeDao.saveFileToStagingSBMFile(inboundFileDTO);


		// Go and get what was just put in to SBMFILEARCHIVE.
		// Note: StagingSbmFileDaoImplTest confirms and tests STAGINGSBMFILE insert.
		String sql = "SELECT ssa.SBMXML.GETCLOBVAL() AS SBMXML, ssa.SBMFILECREATEDATETIME, ssa.CREATEDATETIME, ssa.CREATEBY, " +
				"ssa.LASTMODIFIEDDATETIME, ssa.LASTMODIFIEDBY, ssa.SBMFILENM, ssa.SBMFILEID, ssa.SBMFILENUM, ssa.TRADINGPARTNERID, ssa.TENANTNUM, " +
				"ssa.COVERAGEYEAR, ssa.ISSUERFILESETID, ssa.ISSUERID, ssa.SUBSCRIBERSTATECD FROM SBMFILEARCHIVE ssa " +
				"WHERE ssa.SBMFILEINFOID = " + inboundFileDTO.getSbmFileInfo().getSbmFileInfoId();

		List<Map<String, Object>> actualList = jdbc.queryForList(sql);
		assertEquals("STAGINGSBMFILE record list size", expectedListSize, actualList.size());

		Map<String, Object> row = actualList.get(0);
		String strSbmXML = (String) row.get("SBMXML");
		assertNotNull("SBMXML", strSbmXML);

		StringReader sr = new StringReader(strSbmXML);
		JAXBElement<Enrollment> element = unmarshaller.unmarshal(new StreamSource(sr), Enrollment.class);
		Enrollment actualEnrollment = element.getValue();

		// Verify Enrollment (SBMXML)
		LocalDateTime expectedDateTime = DateTimeUtil.getLocalDateTimeFromXmlGC(expectedEnrollment.getFileInformation().getFileCreateDateTime());
		assertEquals("TenantId from SBMXML", tenantId, actualEnrollment.getFileInformation().getTenantId());
		assertEquals("CoverageYear from SBMXML", covYr, actualEnrollment.getFileInformation().getCoverageYear());
		assertEquals("IssuerId from SBMXML", issuerId, actualEnrollment.getFileInformation().getIssuerFileInformation().getIssuerId());
		
		// Verify Other columns
		assertEquals("FileCreateDateTime", Timestamp.valueOf(expectedDateTime),  (Timestamp) row.get("SBMFILECREATEDATETIME"));
		assertEquals("SbmFileNm", sbmFileNm, (String) row.get("SBMFILENM"));
		assertEquals("SbmFileId", sbmFileId.toString(), (String) row.get("SBMFILEID"));
		assertEquals("SbmFileNum", new BigDecimal(expectedEnrollment.getFileInformation().getIssuerFileInformation().getIssuerFileSet().getFileNumber()), 
				(BigDecimal) row.get("SBMFILENUM"));
		
		//TODO assertions for:
		//TRADINGPARTNERID, TENANTNUM, COVERAGEYEAR, ISSUERFILESETID, ISSUERID, SUBSCRIBERSTATECD
		
		assertEquals("CreateBy", inboundFileDTO.getBatchId().toString(), (String) row.get("CREATEBY"));
		assertEquals("LastModifiedBy", inboundFileDTO.getBatchId().toString(), (String) row.get("LASTMODIFIEDBY"));
		assertNotNull("CreateDateTime",  row.get("CREATEDATETIME"));
		assertNotNull("LastModifiedBy", row.get("LASTMODIFIEDBY"));
	}


	@Test
	public void test_saveSbmFileProcessingSummary() {

		String tenantId = "CA0";
		String issuerId = "99999";
		String issuerFileSetId = "123999";

		SBMFileProccessingSummary sbmFileProcSum = TestDataSBMUtility.makeSbmFileProcessingSummary(tenantId, issuerId, issuerFileSetId);

		Long sbmFileProcSumId = sbmFileCompositeDao.saveSbmFileProcessingSummary(sbmFileProcSum);
		assertNotNull("sbmFileProcSumId", sbmFileProcSumId);
		//TODO select by id and compare values

	}

	@Test
	public void test_getAllSBMFileInfos() {
		
		String tenantId = TestDataSBMUtility.getRandomSbmState() + "0";
		String sbmFileId = TestDataSBMUtility.getRandomNumberAsString(10);

		SBMFileProcessingDTO inboundFileDTO1 = insertParentFileRecords(tenantId, sbmFileId, SBMFileStatus.REJECTED);
		
		List<SBMSummaryAndFileInfoDTO> actualList = sbmFileCompositeDao.getAllSBMFileInfos(inboundFileDTO1.getSbmFileProcSumId());

		assertEquals("SBMSummaryAndFileInfoDTO list size", 1, actualList.size());

		SBMSummaryAndFileInfoDTO actual1 = actualList.get(0);
		assertEquals("1) SbmFileProcessingSummaryId", inboundFileDTO1.getSbmFileProcSumId(), actual1.getSbmFileProcSumId());
		assertEquals("1) SbmFileInfo List size", 1, actual1.getSbmFileInfoList().size());
		
	}
	
	
	@Test
	public void test_findSbmFileInfo() {
		
		String tenantId = TestDataSBMUtility.getRandomSbmState() + "0";
		int fileNum = TestDataSBMUtility.getRandomNumber(4); // max 4 digits for fileNum
		String sbmFileId = "FID-" + fileNum;
		String issuerFileSetId = "FSID-" + fileNum;
		String issuerId = "33333";
		
		SBMFileProcessingDTO inboundFileDTO1 = insertParentFileRecords(tenantId, sbmFileId, issuerFileSetId, fileNum, issuerId);
		insertSBMFileInfo(inboundFileDTO1.getSbmFileProcSumId(), sbmFileId, fileNum); 
		
		List<SBMSummaryAndFileInfoDTO> actualList = sbmFileCompositeDao.findSbmFileInfo(issuerFileSetId, fileNum);

		assertEquals("SBMSummaryAndFileInfoDTO list size", 1, actualList.size());

		SBMSummaryAndFileInfoDTO actual1 = actualList.get(0);
		assertEquals("1) SbmFileProcessingSummaryId", inboundFileDTO1.getSbmFileProcSumId(), actual1.getSbmFileProcSumId());
		assertEquals("1) SbmFileInfo List size", 2, actual1.getSbmFileInfoList().size());
		
	}

	@Test
	public void test_performSbmFileMatch() {

		String tenantId = TestDataSBMUtility.getRandomSbmState() + "0";
		String sbmFileId = TestDataSBMUtility.getRandomNumberAsString(10);

		SBMFileProcessingDTO inboundFileDTO1 = insertParentFileRecords(tenantId, sbmFileId, SBMFileStatus.REJECTED);
		SBMFileProcessingDTO inboundFileDTO2 = insertParentFileRecords(tenantId, sbmFileId, SBMFileStatus.IN_PROCESS);

		List<SBMSummaryAndFileInfoDTO> actualList = sbmFileCompositeDao.performSbmFileMatch(sbmFileId, tenantId);

		assertEquals("SBMSummaryAndFileInfoDTO list size", 2, actualList.size());

		SBMSummaryAndFileInfoDTO actual1 = actualList.get(0);
		assertEquals("1) SbmFileProcessingSummaryId", inboundFileDTO1.getSbmFileProcSumId(), actual1.getSbmFileProcSumId());
		assertEquals("1) SbmFileInfo List size", 1, actual1.getSbmFileInfoList().size());
		
		SBMSummaryAndFileInfoDTO actual2 = actualList.get(1);
		assertEquals("2) SbmFileProcessingSummaryId", inboundFileDTO2.getSbmFileProcSumId(), actual2.getSbmFileProcSumId());
		assertEquals("2) SbmFileInfo List size", 1, actual2.getSbmFileInfoList().size());
	}

	@Test
	public void test_performSbmFileMatch_by_fileSetId_fileNum() {

		String tenantId = "NY0";
		String sbmFileId = TestDataSBMUtility.getRandomNumberAsString(10);
		String fileSetId = "FSID-" + TestDataSBMUtility.getRandomNumberAsString(5);
		int fileNum = 1;
		String issuerId = "66601";

		SBMFileProcessingDTO inboundFileDTO = insertParentFileRecords(tenantId, sbmFileId, fileSetId, fileNum, issuerId);

		List<SBMSummaryAndFileInfoDTO> actualList = sbmFileCompositeDao.findSbmFileInfo(fileSetId, fileNum);

		assertEquals("SBMFileInfo list size", 1, actualList.size());

		SBMSummaryAndFileInfoDTO actual = actualList.get(0);
		assertEquals("SbmFileProcessingSummaryId", inboundFileDTO.getSbmFileProcSumId(), actual.getSbmFileProcSumId());
	//	assertEquals("SbmFileId", sbmFileId, actual.getSbmFileId());
	}
	
	@Test
	public void test_getFileStatus() {

		String tenantId = "NY0";
		String sbmFileId = TestDataSBMUtility.getRandomNumberAsString(10);
		String fileSetId = "FSID-" + TestDataSBMUtility.getRandomNumberAsString(5);
		int fileNum = 1;
		String fileName = "FILENAME-" + sbmFileId;
		String issuerId = "66666";

		SBMFileProcessingDTO inboundFileDTO = insertParentFileRecords(tenantId, sbmFileId, fileSetId, fileNum, issuerId);

		List<SBMSummaryAndFileInfoDTO> actualList = sbmFileCompositeDao.getFileStatus(fileName);

		assertEquals("SBMFileInfo list size", 1, actualList.size());

		SBMSummaryAndFileInfoDTO actual = actualList.get(0);
		assertEquals("SbmFileProcessingSummaryId", inboundFileDTO.getSbmFileProcSumId(), actual.getSbmFileProcSumId());
	//	assertEquals("SbmFileId", sbmFileId, actual.getSbmFileId());
	}
	
	
	
	@Test
	public void test_selectFileInfoXml() {
		
		String tenantId = "RI0";
		String sbmFileId = TestDataSBMUtility.getRandomNumberAsString(10);
		SBMFileProcessingDTO inboundFileDTO = insertParentFileRecords(tenantId, sbmFileId);
		
		String actual = sbmFileCompositeDao.getFileInfoTypeXml(inboundFileDTO.getSbmFileInfo().getSbmFileInfoId());
		
		assertNotNull("FileInfoXML should not be null", actual);
		assertTrue("FileInfoXML should contain sbmFileId", actual.indexOf(sbmFileId) > 0);
	}
	
	
	@Test
	public void test_getLatestSBMFileProcessingSummaryByIssuer() {
		
		String stateCd = TestDataSBMUtility.getRandomSbmState();
		String tenantId = stateCd + "0";
		String id =  TestDataSBMUtility.getRandomNumberAsString(5);
		String sbmFileId = "FID-" + id;
		String issuerFileSetId = "FSID-" + id;
		int fileNum = 1;
		String issuerId = id;
		
		insertParentFileRecords(tenantId, sbmFileId, issuerFileSetId, fileNum, issuerId);
		
		SBMSummaryAndFileInfoDTO actual = sbmFileCompositeDao.getLatestSBMFileProcessingSummaryByIssuer(issuerId);
		assertNotNull("SBMSummaryAndFileInfoDTO should not be null", actual);
	}
	
	@Test
	public void test_getLatestSBMFileProcessingSummaryByState() {
		
		String stateCd = TestDataSBMUtility.getRandomSbmState();
		String tenantId = stateCd + "0";
		String id =  TestDataSBMUtility.getRandomNumberAsString(5);
		String sbmFileId = "FID-" + id;
		String issuerFileSetId = "FSID-" + id;
		int fileNum = 1;
		String issuerId = id;
		
		insertParentFileRecords(tenantId, sbmFileId, issuerFileSetId, fileNum, issuerId);
		
		SBMSummaryAndFileInfoDTO actual = sbmFileCompositeDao.getLatestSBMFileProcessingSummaryByState(stateCd);
		assertNotNull("SBMSummaryAndFileInfoDTO should not be null", actual);
	}
	
	
	@Test
	public void test_getAllSBMFileProcessingSummary() {
		
		String stateCd = TestDataSBMUtility.getRandomSbmState();
		String tenantId = stateCd + "0";
		String id =  TestDataSBMUtility.getRandomNumberAsString(5);
		String sbmFileId = "FID-" + id;
		
		SBMFileStatus status1 = SBMFileStatus.BYPASS_FREEZE;
		SBMFileStatus status2 = SBMFileStatus.EXPIRED;
		SBMFileStatus status3 = SBMFileStatus.BACKOUT;
		
		insertParentFileRecords(tenantId, sbmFileId, status1);
		insertParentFileRecords(tenantId, sbmFileId, status2);
		insertParentFileRecords(tenantId, sbmFileId, status3);
		
		List<SBMSummaryAndFileInfoDTO> actualList = sbmFileCompositeDao.getAllSBMFileProcessingSummary(status3);
		assertEquals("SBMSummaryAndFileInfoDTO list size", true, actualList.size() > 0); // > Incase left over test data in db.
		
	}
	
	/**
	 * If this test fails, update or remove any jobs "sbmIngestionBatchJob" with JobStatusCd of STARTED.
	 */
	@Test
	public void test_isSBMIJobRunning() {
		
		boolean expected = false;
		boolean actual = sbmFileCompositeDao.isSBMIJobRunning();
		
		assertEquals("No SBMI jobs running", expected, actual);
	}
	
	@Test
	public void test_isSBMIJobRunning_true() {
		
		boolean expected = true;
		String batchBusId = "SBMIINGESTYYYYMMDD001";
		Long jobId = TestDataSBMUtility.getRandomNumberAsLong(7);
		String name = "sbmIngestionBatchJob";
		String status = "STARTED";
		insertBatchProcessLog(batchBusId, jobId, name, status); 
		
		boolean actual = sbmFileCompositeDao.isSBMIJobRunning();
		
		assertEquals("Yes, SBMI jobs running (in 'STARTED' job status)", expected, actual);
	}
	
	
	
	@Test
	public void test_saveFileInfoAndErrors() {
		 
		String qhpId = TestDataSBMUtility.makeQhpId("12345", "CA0");
		String state = "RI";
		String tenantId = state + "0";
		String issuerId = "88888";
		String fileSetId = "111888";
		SBMFileStatus fileStatus = SBMFileStatus.REJECTED;
		
		SBMFileProcessingDTO inboundFileDTO = new SBMFileProcessingDTO();

		Long sbmFileProcSumId = insertSBMFileProcessingSummary(tenantId, issuerId, fileSetId, fileStatus);
		inboundFileDTO.setSbmFileProcSumId(sbmFileProcSumId);
		inboundFileDTO.setFileInfoType(TestDataSBMUtility.makeFileInformationType(tenantId, issuerId, TestDataSBMUtility.FILES_FILESET));
		
		inboundFileDTO.setBatchId(Long.valueOf("8888888"));
		inboundFileDTO.getErrorList().addAll(TestDataSBMUtility.makeSBMErrorDTOList(2));
        inboundFileDTO.setSbmFileInfo(TestDataSBMUtility.makeSBMFileInfo("33", "3", SBMFileStatus.IN_PROCESS));

		Long sbmFileInfoId = sbmFileCompositeDao.saveFileInfoAndErrors(inboundFileDTO);
		
		assertNotNull("sbmFileInfoId", sbmFileInfoId);
	}
	
	@Test
	public void test_saveSBMFileErrors() {
		 
		assertNotNull("sbmFileCompositeDao", sbmFileCompositeDao);
		
		String qhpId = TestDataSBMUtility.makeQhpId("12345", "CA0");
		String state = "RI";
		String tenantId = state + "0";
		String issuerId = "88888";
		Long sbmFileId = TestDataSBMUtility.getRandomNumberAsLong(10);
		
		SBMFileProcessingDTO fileDTO = insertParentFileRecords(tenantId, sbmFileId.toString());
		
		List<SBMErrorDTO> errorList = TestDataSBMUtility.makeSBMErrorDTOList(3);
		
		for (SBMErrorDTO error : errorList) {
			error.setSbmFileInfoId(fileDTO.getSbmFileInfo().getSbmFileInfoId());
		}

		sbmFileCompositeDao.saveSBMFileErrors(errorList);
		
		assertNotNull("Assert for asserting", errorList);
	}
	
	
	@Test
	public void test_updateCMSApprovedInd() {
		
		String state = "VT";
		String tenantId = state + "0";
		String issuerId = "22277";
		String fileSetId = "222771";
		SBMFileStatus fileStatus = SBMFileStatus.ACCEPTED;
		Long sbmFileProcSumId = insertSBMFileProcessingSummary(tenantId, issuerId, fileSetId, fileStatus);
		
		String expected = "Y";
		
		sbmFileCompositeDao.updateCMSApprovedInd(sbmFileProcSumId, expected);
		
		String sql = "SELECT CMSAPPROVEDIND FROM SBMFILEPROCESSINGSUMMARY WHERE SBMFILEPROCESSINGSUMMARYID = " + sbmFileProcSumId;

		List<Map<String, Object>> actualList = jdbc.queryForList(sql);
		assertEquals("SBMFILEPROCESSINGSUMMARY record list size", 1, actualList.size());

		Map<String, Object> actual = actualList.get(0);

		assertEquals("TransMsgDirectionTypeCd", expected, actual.get("CMSAPPROVEDIND"));
	}
	
//	@Test
//	public void test_extractXprToStagingPolicy() {
//		
//		String stateCd = TestDataSBMUtility.getRandomSbmState();
//		String tenantId = stateCd + "0";
//		String id =  TestDataSBMUtility.getRandomNumberAsString(5);
//		String sbmFileId = "FID-" + id;
//		Long batchId = Long.valueOf(id);
//		
//		SBMFileProcessingDTO inboundFileDTO = insertParentFileRecords(tenantId, sbmFileId, SBMFileStatus.IN_PROCESS);
//		inboundFileDTO.setFileInfoType(TestDataSBMUtility.makeFileInformationType(tenantId));
//		inboundFileDTO.setBatchId(batchId);
//		
//		
//	}

}
