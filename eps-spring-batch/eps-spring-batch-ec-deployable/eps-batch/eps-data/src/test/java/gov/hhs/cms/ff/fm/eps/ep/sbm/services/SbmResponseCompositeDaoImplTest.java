package gov.hhs.cms.ff.fm.eps.ep.sbm.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import gov.cms.dsh.sbmi.Enrollment;
import gov.hhs.cms.ff.fm.eps.ep.enums.PolicyStatus;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMResponsePhaseTypeCode;
import gov.hhs.cms.ff.fm.eps.ep.enums.SbmTransMsgStatus;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmResponsePO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMCache;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMErrorDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileProcessingDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMSummaryAndFileInfoDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SbmResponseDTO;
import gov.hhs.cms.ff.fm.eps.ep.util.sbm.TestDataSBMUtility;
import gov.hhs.cms.ff.fm.eps.ep.vo.UserVO;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/sbmi-data-config.xml", "classpath:/test-context-data.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class SbmResponseCompositeDaoImplTest extends BaseSbmServicesTest {

	@Autowired
	private SbmResponseCompositeDao sbmResponseCompositeDao;
	
	@Autowired
	private UserVO userVO;


	@Test
	public void test_generateSBMR() {

		assertNotNull("SbmResponseCompositeDao is NOT null.", sbmResponseCompositeDao);

		SBMCache.setJobExecutionId(TestDataSBMUtility.getRandomNumberAsLong(3));

		String state = TestDataSBMUtility.getRandomSbmState();
		String sbmFileId = TestDataSBMUtility.getRandomNumberAsString(3);
		String tenantId = state + "0";
		SBMFileStatus fileStatus = SBMFileStatus.ACCEPTED;

		SBMFileProcessingDTO fileDTO = insertParentFileRecords(tenantId, sbmFileId, SBMFileStatus.IN_PROCESS);

		Long sbmFileInfoId = fileDTO.getSbmFileInfo().getSbmFileInfoId();
		String exchangePolicyId = "EXPOLID-" + sbmFileId;
		SbmTransMsgStatus status = SbmTransMsgStatus.ACCEPTED_WITH_CHANGES;


		Long sbmTransMsgId = insertSbmTransMsg(sbmFileInfoId, state, exchangePolicyId, status);

		SBMErrorDTO fileErrorDTO = TestDataSBMUtility.makeSBMErrorDTO(1);
		insertSbmTransMsgValidation(sbmTransMsgId, fileErrorDTO, 1);

		SbmResponseDTO responseDTO = sbmResponseCompositeDao.generateSBMR(fileDTO.getSbmFileProcSumId());

		assertNotNull("SbmResponseDTO", responseDTO);

		SBMSummaryAndFileInfoDTO summaryDTO = responseDTO.getSbmSummaryAndFileInfo();

		assertNotNull("SBMSummaryAndFileInfoDTO", summaryDTO);

		assertEquals("ErrorThresholdPercent", fileDTO.getErrorThresholdPercent(), summaryDTO.getErrorThresholdPercent());

		/*
		 * private SBMSummaryAndFileInfoDTO sbmSummaryAndFileInfo;
	private FileAcceptanceRejection sbmr;	
	private boolean xprErrorsExist;	
	private boolean xprWarningsExist;
	private SBMResponsePhaseTypeCode sbmResponsePhaseTypeCd; 
	private Long physicalDocumentId;

		 */

	}

	@Test
	public void test_generateUpdateStatusSBMR() {

		assertNotNull("SbmResponseCompositeDao is NOT null.", sbmResponseCompositeDao);

		SBMCache.setJobExecutionId(TestDataSBMUtility.getRandomNumberAsLong(3));

		String state = TestDataSBMUtility.getRandomSbmState();
		String sbmFileId = TestDataSBMUtility.getRandomNumberAsString(3);
		String tenantId = state + "0";

		SBMFileProcessingDTO fileDTO = insertParentFileRecords(tenantId, sbmFileId);

		Long sbmFileInfoId = fileDTO.getSbmFileInfo().getSbmFileInfoId();
		String exchangePolicyId = "EXPOLID-" + sbmFileId;
		SbmTransMsgStatus status = SbmTransMsgStatus.ACCEPTED_WITH_CHANGES;


		Long sbmTransMsgId = insertSbmTransMsg(sbmFileInfoId, state, exchangePolicyId, status);

		SBMErrorDTO fileErrorDTO = TestDataSBMUtility.makeSBMErrorDTO(1);
		insertSbmTransMsgValidation(sbmTransMsgId, fileErrorDTO, 1);

		SbmResponseDTO responseDTO = sbmResponseCompositeDao.generateUpdateStatusSBMR(fileDTO.getSbmFileProcSumId());

		assertNotNull("SbmResponseDTO", responseDTO);

	}

	@Test
	public void test_generateUpdateStatusSBMR_Cycle_1() {

		assertNotNull("SbmResponseCompositeDao is NOT null.", sbmResponseCompositeDao);

		SBMCache.setJobExecutionId(TestDataSBMUtility.getRandomNumberAsLong(3));

		String state = TestDataSBMUtility.getRandomSbmState();
		String sbmFileId = TestDataSBMUtility.getRandomNumberAsString(3);
		String tenantId = state + "0";

		SBMFileProcessingDTO fileDTO = insertParentFileRecords(tenantId, sbmFileId);

		Long sbmFileInfoId = fileDTO.getSbmFileInfo().getSbmFileInfoId();
		String exchangePolicyId = "EXPOLID-" + sbmFileId;
		SbmTransMsgStatus status = SbmTransMsgStatus.ACCEPTED_WITH_CHANGES;

		// add files into StagingSbmFile
		Long sbmTransMsgId = insertSbmTransMsg(sbmFileInfoId, state, exchangePolicyId, status);

		SBMErrorDTO fileErrorDTO = TestDataSBMUtility.makeSBMErrorDTO(1);
		insertSbmTransMsgValidation(sbmTransMsgId, fileErrorDTO, 1);

		SbmResponseDTO responseDTO = sbmResponseCompositeDao.generateUpdateStatusSBMR(fileDTO.getSbmFileProcSumId());

		assertNotNull("SbmResponseDTO", responseDTO);

	}

	@Test
	public void test_isRecExistsInStagingSBMPolicy() {

		
		boolean expected = true;
		String state = TestDataSBMUtility.getRandomSbmState();
		String sbmFileId = TestDataSBMUtility.getRandomNumberAsString(5);
		String tenantId = state + "0";
		Long batchId = TestDataSBMUtility.getRandomNumberAsLong(3);
		userVO.setUserId(batchId.toString());

		SBMFileProcessingDTO fileDTO = insertParentFileRecords(tenantId, sbmFileId);
		Long sbmFileProcSumId = fileDTO.getSbmFileProcSumId();
		Long sbmFileInfoId = fileDTO.getSbmFileInfo().getSbmFileInfoId();

		for (int i = 1; i < 5; ++i) {
			String policyXml = "<policy>Test policy: " + i + " for sbmFileId: " + sbmFileId + "</policy>";

			insertStagingSbmPolicy(sbmFileProcSumId, sbmFileInfoId, policyXml);
		}


		boolean actual = sbmResponseCompositeDao.isRecExistsInStagingSBMPolicy(sbmFileProcSumId);
		assertEquals("StagingSbmPolicy record exists", expected, actual);	
	}

	@Test
	public void test_isRecExistsInStagingSBMPolicy_false() {

		boolean expected = false;
		Long sbmFileProcSumId = TestDataSBMUtility.getRandomNumberAsLong(12);
		boolean actual = sbmResponseCompositeDao.isRecExistsInStagingSBMPolicy(sbmFileProcSumId);
		assertEquals("StagingSbmPolicy record exists", expected, actual);	
	}



	// The following test verify each count in SBMFILEPROCESSINGSUMMARY.

	@Test
	public void test_TotalIssuerFileCount() {

		int listSize = 3;
		BigDecimal expected_TOT = new BigDecimal(listSize);

		Long batchId = TestDataSBMUtility.getRandomNumberAsLong(3);
		SBMCache.setJobExecutionId(batchId);

		String stateCd = TestDataSBMUtility.getRandomSbmState();
		Long sbmFileId = TestDataSBMUtility.getRandomNumberAsLong(3);
		String tenantId = stateCd + "0";
		String issuerId = "88888";
		String fileSetId = "FSID-11";
		SBMFileStatus fileStatus = SBMFileStatus.ACCEPTED;

		Long sbmFileProcSumId = insertSBMFileProcessingSummary(tenantId, issuerId, fileSetId, fileStatus);

		for (int i = 1; i <= listSize; ++i) {

			insertSBMFileInfo(sbmFileProcSumId, sbmFileId + "00" + i , i);
		} 

		SbmResponseDTO responseDTO = sbmResponseCompositeDao.generateSBMR(sbmFileProcSumId);

		assertNotNull("SbmResponseDTO", responseDTO);

		String sql = "SELECT TOTALISSUERFILECNT FROM SBMFILEPROCESSINGSUMMARY WHERE SBMFILEPROCESSINGSUMMARYID = " + sbmFileProcSumId;

		List<Map<String, Object>> actualList = jdbc.queryForList(sql);
		assertEquals("SBMFILEPROCESSINGSUMMARY record list size", 1, actualList.size());

		Map<String, Object> actual = actualList.get(0);

		assertEquals("TotalIssuerFileCount", expected_TOT, actual.get("TOTALISSUERFILECNT"));
	}


	@Test
	public void test_TotalRecordRejectCnt() {

		SbmTransMsgStatus[] statuses = {SbmTransMsgStatus.REJECTED, SbmTransMsgStatus.ACCEPTED_NO_CHANGE, SbmTransMsgStatus.REJECTED};
		int expected_RJC = 2;

		Long batchId = TestDataSBMUtility.getRandomNumberAsLong(3);
		SBMCache.setJobExecutionId(batchId);

		String stateCd = TestDataSBMUtility.getRandomSbmState();
		String sbmFileId = TestDataSBMUtility.getRandomNumberAsString(3);
		String tenantId = stateCd + "0";

		SBMFileProcessingDTO fileDTO = insertParentFileRecords(tenantId, sbmFileId);

		Long sbmFileInfoId = fileDTO.getSbmFileInfo().getSbmFileInfoId();
		Long sbmFileProcSumId = fileDTO.getSbmFileProcSumId();
		String exchangePolicyIdPrefix = "EXPOLID-" + sbmFileId;
		String exchangePolicyId = null;

		for (int i = 0; i < statuses.length; ++i) {

			exchangePolicyId = exchangePolicyIdPrefix + "-" + i + "" + i + "" + i;
			insertSbmTransMsg(sbmFileInfoId, stateCd, exchangePolicyId, statuses[i]);
		}

		SbmResponseDTO responseDTO = sbmResponseCompositeDao.generateSBMR(sbmFileProcSumId);

		assertNotNull("SbmResponseDTO", responseDTO);

		String sql = "SELECT TOTALRECORDREJECTEDCNT FROM SBMFILEPROCESSINGSUMMARY WHERE SBMFILEPROCESSINGSUMMARYID = " + sbmFileProcSumId;

		List<Map<String, Object>> actualList = jdbc.queryForList(sql);
		assertEquals("SBMFILEPROCESSINGSUMMARY record list size", 1, actualList.size());

		Map<String, Object> actual = actualList.get(0);

		assertEquals("TotalRecordRejectCnt", new BigDecimal(expected_RJC), actual.get("TOTALRECORDREJECTEDCNT"));
	}


	@Test
	public void test_TotalRecordProcessedCnt() {

		SbmTransMsgStatus[] statuses = {SbmTransMsgStatus.REJECTED, SbmTransMsgStatus.ACCEPTED_NO_CHANGE, SbmTransMsgStatus.ACCEPTED_WITH_CHANGES};
		BigDecimal expected_TOT = new BigDecimal(statuses.length);

		Long batchId = TestDataSBMUtility.getRandomNumberAsLong(3);
		SBMCache.setJobExecutionId(batchId);

		String stateCd = TestDataSBMUtility.getRandomSbmState();
		Long sbmFileId = TestDataSBMUtility.getRandomNumberAsLong(3);
		String tenantId = stateCd + "0";
		String issuerId = "11111";
		String qhpId = TestDataSBMUtility.makeQhpId(issuerId, tenantId);

		SBMFileProcessingDTO fileDTO = insertParentFileRecords(tenantId, sbmFileId.toString());

		Long sbmFileInfoId = fileDTO.getSbmFileInfo().getSbmFileInfoId();
		Long sbmFileProcSumId = fileDTO.getSbmFileProcSumId();
		String exchangePolicyIdPrefix = "EXPOLID-" + sbmFileId;
		String exchangePolicyId = null;


		Enrollment enrollment = TestDataSBMUtility.makeEnrollment(sbmFileId, tenantId, YEAR, issuerId, TestDataSBMUtility.FILES_ONE_PER_ISSUER);

		for (int i = 0; i < statuses.length; ++i) {

			exchangePolicyId = exchangePolicyIdPrefix + "-" + i + "" + i + "" + i;
			enrollment.getPolicy().add(TestDataSBMUtility.makePolicyType(i, qhpId, exchangePolicyId));			
			insertSbmTransMsg(sbmFileInfoId, stateCd, exchangePolicyId, statuses[i]);
		}
		String sbmFileXML = TestDataSBMUtility.getEnrollmentAsXmlString(enrollment);

		// insert the Enrollment XML into staging		
		String sql = "INSERT INTO STAGINGSBMFILE (SBMXML, BATCHID, SBMFILEPROCESSINGSUMMARYID, SBMFILEINFOID) " +
				"VALUES (XMLTYPE('" + sbmFileXML + "'), " + batchId + ", " + sbmFileProcSumId + 
				", " + sbmFileInfoId + ")";

		jdbc.execute(sql);

		SbmResponseDTO responseDTO = sbmResponseCompositeDao.generateSBMR(sbmFileProcSumId);

		assertNotNull("SbmResponseDTO", responseDTO);

		sql = "SELECT TOTALRECORDPROCESSEDCNT FROM SBMFILEPROCESSINGSUMMARY WHERE SBMFILEPROCESSINGSUMMARYID = " + sbmFileProcSumId;

		List<Map<String, Object>> actualList = jdbc.queryForList(sql);
		assertEquals("SBMFILEPROCESSINGSUMMARY record list size", 1, actualList.size());

		Map<String, Object> actual = actualList.get(0);

		assertEquals("TotalRecordProcessedCnt", expected_TOT, actual.get("TOTALRECORDPROCESSEDCNT"));
	}



	@Test
	public void test_totalPreviousPoliciesNotSubmit_EFF_CAN_TERM() {

		PolicyStatus[] statuses = {PolicyStatus.EFFECTUATED_2, PolicyStatus.CANCELLED_3,  PolicyStatus.EFFECTUATED_2, 
				PolicyStatus.EFFECTUATED_2, PolicyStatus.CANCELLED_3, PolicyStatus.EFFECTUATED_2};

		// Note:System shall identify an effectuated policy as terminated when 
		//the end date month of the policy is less than or equal to the 
		//processing month of SBMI ingestion job, hence only 3 EFFs.
		BigDecimal expected_TOT = new BigDecimal(statuses.length);
		BigDecimal expected_EFF = new BigDecimal(3);
		BigDecimal expected_CAN = new BigDecimal(2);
		BigDecimal expected_TERM = new BigDecimal(1);

		Long batchId = TestDataSBMUtility.getRandomNumberAsLong(4);
		SBMCache.setJobExecutionId(batchId);

		String stateCd = TestDataSBMUtility.getRandomSbmState();
		Long sbmFileId = batchId;
		String tenantId = stateCd + "0";
		String issuerId = "11111";
		String qhpId = TestDataSBMUtility.makeQhpId(issuerId, tenantId);

		SBMFileProcessingDTO fileDTO = insertParentFileRecords(tenantId, sbmFileId.toString());

		Long sbmFileInfoId = fileDTO.getSbmFileInfo().getSbmFileInfoId();
		String exchangePolicyIdPrefix = "EXPOLID-" + sbmFileId;
		String exchangePolicyId = null;
		LocalDate psd = JAN_1;
		LocalDate ped = DEC_31;


		Enrollment enrollment = TestDataSBMUtility.makeEnrollment(sbmFileId, tenantId, YEAR, issuerId, TestDataSBMUtility.FILES_ONE_PER_ISSUER);

		for (int i = 0; i < statuses.length; ++i) {

			exchangePolicyId = exchangePolicyIdPrefix + "-" + i + "" + i + "" + i;

			// insert some existing EPS policies
			Long sbmTransMsgId = insertSbmTransMsg(sbmFileInfoId, stateCd, exchangePolicyId, SbmTransMsgStatus.ACCEPTED_NO_CHANGE);
			// for the first policy, which is EFFECTUATED_2, set the PolicyEndDAte < now (job run time) to trigger the TERM count.
			if (i == 0) {
				ped = LocalDate.now().minusDays(1);
			} else {
				ped = DEC_31;
			}
			Long pvId = insertPolicyVersion(JAN_1_1am, psd, ped, stateCd, exchangePolicyId, qhpId, sbmTransMsgId);
			insertPolicyStatus(pvId, JAN_1_1am, statuses[i]);
			// make some new imbound ones that do not match EPS (diff exchangePolicyId)
			enrollment.getPolicy().add(TestDataSBMUtility.makePolicyType(i, qhpId, "NEW-" + exchangePolicyId));	
		}

		sbmFileId++;
		batchId++;
		SBMFileProcessingDTO fileDTO2 = insertParentFileRecords(tenantId, sbmFileId.toString(), SBMFileStatus.ACCEPTED);

		Long sbmFileInfoId2 = fileDTO2.getSbmFileInfo().getSbmFileInfoId();
		Long sbmFileProcSumId2 = fileDTO2.getSbmFileProcSumId();

		String sbmFileXML = TestDataSBMUtility.getEnrollmentAsXmlString(enrollment);

		// insert the Enrollment XML into staging (with no policies for this test)		
		String sql = "INSERT INTO STAGINGSBMFILE (SBMXML, BATCHID, SBMFILEPROCESSINGSUMMARYID, SBMFILEINFOID) " +
				"VALUES (XMLTYPE('" + sbmFileXML + "'), " + batchId + ", " + sbmFileProcSumId2 + 
				", " + sbmFileInfoId2 + ")";

		jdbc.execute(sql);

		SbmResponseDTO responseDTO = sbmResponseCompositeDao.generateSBMR(sbmFileProcSumId2);

		assertNotNull("SbmResponseDTO", responseDTO);

		sql = "SELECT TOTALPREVIOUSPOLICIESNOTSUBMIT, NOTSUBMITTEDEFFECTUATEDCNT, NOTSUBMITTEDTERMINATEDCNT, NOTSUBMITTEDCANCELLEDCNT " +
				"FROM SBMFILEPROCESSINGSUMMARY WHERE SBMFILEPROCESSINGSUMMARYID = " + sbmFileProcSumId2;

		List<Map<String, Object>> actualList = jdbc.queryForList(sql);
		assertEquals("SBMFILEPROCESSINGSUMMARY record list size", 1, actualList.size());

		Map<String, Object> actual = actualList.get(0);

		assertEquals("TotalPreviousPoliciesNotSubmit", expected_TOT, actual.get("TOTALPREVIOUSPOLICIESNOTSUBMIT"));
		assertEquals("NotSubmittedEffectuatedCnt", expected_EFF, actual.get("NOTSUBMITTEDEFFECTUATEDCNT"));
		assertEquals("NotSubmittedCancelledCnt", expected_CAN, actual.get("NOTSUBMITTEDCANCELLEDCNT"));
		assertEquals("NotSubmittedTerminatedCnt", expected_TERM, actual.get("NOTSUBMITTEDTERMINATEDCNT"));
	}

	/*
	 * - TotalPolicyApprovedCnt
	 * - MatchingPlcNoChangeCnt;
	 * - MatchingPlcChgApplCnt
	 * - MatchingPlcCorrectedChgApplCnt
	 */
	@Test
	public void test_totalPolicyApproved_Matching() {

		SbmTransMsgStatus[] statuses = {
				SbmTransMsgStatus.ACCEPTED_WITH_CHANGES, SbmTransMsgStatus.ACCEPTED_NO_CHANGE, SbmTransMsgStatus.ACCEPTED_WITH_CHANGES, 
				SbmTransMsgStatus.ACCEPTED_NO_CHANGE, SbmTransMsgStatus.ACCEPTED_WITH_CHANGES, SbmTransMsgStatus.ACCEPTED_WITH_CHANGES, 
				SbmTransMsgStatus.ACCEPTED_NO_CHANGE, SbmTransMsgStatus.ACCEPTED_NO_CHANGE, 
		};

		BigDecimal expected_TOT = new BigDecimal(statuses.length);
		BigDecimal expected_ANC = new BigDecimal(4);
		BigDecimal expected_ACC = new BigDecimal(3);
		BigDecimal expected_ACC_Corrected = new BigDecimal(1);

		Long batchId = TestDataSBMUtility.getRandomNumberAsLong(4);
		SBMCache.setJobExecutionId(batchId);

		String stateCd = TestDataSBMUtility.getRandomSbmState();
		Long sbmFileId = batchId;
		String tenantId = stateCd + "0";
		String issuerId = "11111";
		String qhpId = TestDataSBMUtility.makeQhpId(issuerId, tenantId);

		SBMFileProcessingDTO fileDTO = insertParentFileRecords(tenantId, sbmFileId.toString(), SBMFileStatus.ACCEPTED_WITH_WARNINGS);

		Long sbmFileProcSumId = fileDTO.getSbmFileProcSumId();
		Long sbmFileInfoId = fileDTO.getSbmFileInfo().getSbmFileInfoId();
		String exchangePolicyIdPrefix = "EXPOLID-" + sbmFileId;
		String exchangePolicyId = null;
		LocalDate psd = JAN_1;
		LocalDate ped = DEC_31;


		for (int i = 0; i < statuses.length; ++i) {

			exchangePolicyId = exchangePolicyIdPrefix + "-" + i + "" + i + "" + i;
			Long sbmTransMsgId = insertSbmTransMsg(sbmFileInfoId, stateCd, exchangePolicyId, statuses[i]);
			Long priorPvId = insertPolicyVersion(JAN_1_1am, psd, ped, stateCd, exchangePolicyId, qhpId, sbmTransMsgId);
			insertPolicyVersion(JAN_2_2am, psd, ped, stateCd, exchangePolicyId, qhpId, sbmTransMsgId, priorPvId);

			if (i == 0) {

				List<SBMErrorDTO> errorDTOList = TestDataSBMUtility.makeSBMErrorDTOList(3);
				for (int j = 0; j < errorDTOList.size(); ++j) {
					insertSbmTransMsgValidation(sbmTransMsgId, errorDTOList.get(j), (j + 1));
				}
			}
		}

		SbmResponseDTO responseDTO = sbmResponseCompositeDao.generateUpdateStatusSBMR(sbmFileProcSumId);

		assertNotNull("SbmResponseDTO", responseDTO);

		String sql = "SELECT TOTALPOLICYAPPROVEDCNT, MATCHINGPLCNOCHGCNT, MATCHINGPLCCHGAPPLCNT, MATCHINGPLCCORRECTEDCHGAPPLCNT " +
				"FROM SBMFILEPROCESSINGSUMMARY WHERE SBMFILEPROCESSINGSUMMARYID = " + sbmFileProcSumId;

		List<Map<String, Object>> actualList = jdbc.queryForList(sql);
		assertEquals("SBMFILEPROCESSINGSUMMARY record list size", 1, actualList.size());

		Map<String, Object> actual = actualList.get(0);

		assertEquals("TotalPolicyApprovedCnt", expected_TOT, actual.get("TOTALPOLICYAPPROVEDCNT"));
		assertEquals("MatchingPlcNoChangeCnt", expected_ANC, actual.get("MATCHINGPLCNOCHGCNT"));
		assertEquals("MatchingPlcChgApplCnt", expected_ACC, actual.get("MATCHINGPLCCHGAPPLCNT"));
		assertEquals("MatchingPlcCorrectedChgApplCnt", expected_ACC_Corrected, actual.get("MATCHINGPLCCORRECTEDCHGAPPLCNT"));
	}

	/*
	 * - newPlcCreatedAsSentCnt;
	 * - newPlcCreatedCorrectionApplCnt
	 */
	@Test
	public void test_newPolicyAsSent() {

		SbmTransMsgStatus[] statuses = {
				SbmTransMsgStatus.ACCEPTED_WITH_CHANGES, SbmTransMsgStatus.ACCEPTED_NO_CHANGE, SbmTransMsgStatus.ACCEPTED_WITH_CHANGES, 
				SbmTransMsgStatus.ACCEPTED_NO_CHANGE, SbmTransMsgStatus.ACCEPTED_NO_CHANGE, SbmTransMsgStatus.ACCEPTED_WITH_CHANGES, 
				SbmTransMsgStatus.ACCEPTED_NO_CHANGE, SbmTransMsgStatus.ACCEPTED_NO_CHANGE, 
		};

		BigDecimal expected_ANC = new BigDecimal(5);
		BigDecimal expected_ACC_Corrected = new BigDecimal(3);

		Long batchId = TestDataSBMUtility.getRandomNumberAsLong(4);
		SBMCache.setJobExecutionId(batchId);

		String stateCd = TestDataSBMUtility.getRandomSbmState();
		Long sbmFileId = batchId;
		String tenantId = stateCd + "0";
		String issuerId = "66666";
		String qhpId = TestDataSBMUtility.makeQhpId(issuerId, tenantId);
		LocalDate psd = JAN_1;
		LocalDate ped = DEC_31;

		SBMFileProcessingDTO fileDTO = insertParentFileRecords(tenantId, sbmFileId.toString(), SBMFileStatus.ACCEPTED_WITH_WARNINGS);

		Long sbmFileProcSumId = fileDTO.getSbmFileProcSumId();
		Long sbmFileInfoId = fileDTO.getSbmFileInfo().getSbmFileInfoId();
		String exchangePolicyIdPrefix = "EXPOLID-" + sbmFileId;
		String exchangePolicyId = null;

		for (int i = 0; i < statuses.length; ++i) {

			exchangePolicyId = exchangePolicyIdPrefix + "-" + i + "" + i + "" + i;
			Long sbmTransMsgId = insertSbmTransMsg(sbmFileInfoId, stateCd, exchangePolicyId, statuses[i]);
			Long pvId = insertPolicyVersion(JAN_1_1am, psd, ped, stateCd, exchangePolicyId, qhpId, sbmTransMsgId);

			if (i == 0) {

				List<SBMErrorDTO> errorDTOList = TestDataSBMUtility.makeSBMErrorDTOList(3);
				for (int j = 0; j < errorDTOList.size(); ++j) {
					insertSbmTransMsgValidation(sbmTransMsgId, errorDTOList.get(j), (j + 1));
				}
			}
		}

		SbmResponseDTO responseDTO = sbmResponseCompositeDao.generateUpdateStatusSBMR(sbmFileProcSumId);

		assertNotNull("SbmResponseDTO", responseDTO);

		String sql = "SELECT NEWPLCCREATEDASSENTCNT, NEWPLCCREATEDCORRECTIONAPPLCNT " +
				"FROM SBMFILEPROCESSINGSUMMARY WHERE SBMFILEPROCESSINGSUMMARYID = " + sbmFileProcSumId;

		List<Map<String, Object>> actualList = jdbc.queryForList(sql);
		assertEquals("SBMFILEPROCESSINGSUMMARY record list size", 1, actualList.size());

		Map<String, Object> actual = actualList.get(0);



		assertEquals("NewPlcCreatedAsSentCnt", expected_ANC, actual.get("NEWPLCCREATEDASSENTCNT"));
		assertEquals("NewPlcCreatedCorrectionApplCnt", expected_ACC_Corrected, actual.get("NEWPLCCREATEDCORRECTIONAPPLCNT"));
	}

	@Test
	public void test_createSBMResponseRecord() {
		
		String state = TestDataSBMUtility.getRandomSbmState();
		String sbmFileId = TestDataSBMUtility.getRandomNumberAsString(3);
		String tenantId = state + "0";
		
		SBMFileProcessingDTO fileDTO = insertParentFileRecords(tenantId, sbmFileId);
		Long  sbmFileProcSumId = fileDTO.getSbmFileProcSumId();
		Long sbmFileInfoId = fileDTO.getSbmFileInfo().getSbmFileInfoId();
		Long physicalDocumentId = insertPhysicalDocument();
		SBMResponsePhaseTypeCode responseCd = SBMResponsePhaseTypeCode.FINAL;

		SbmResponsePO po = new SbmResponsePO();
		po.setSbmFileProcSumId(sbmFileProcSumId);
		po.setSbmFileInfoId(sbmFileInfoId);
		po.setPhysicalDocumentId(physicalDocumentId);
		if (responseCd != null) {
			po.setResponseCd(responseCd.getValue());
		}
		sbmResponseCompositeDao.createSBMResponseRecord(sbmFileProcSumId, sbmFileInfoId, physicalDocumentId, responseCd);
		
		String sql = "SELECT * FROM SBMRESPONSE WHERE SBMFILEPROCESSINGSUMMARYID = " + sbmFileProcSumId;

		List<Map<String, Object>> actualList = jdbc.queryForList(sql);
		assertEquals("SBMFILEPROCESSINGSUMMARY record list size", 1, actualList.size());

		Map<String, Object> actual = actualList.get(0);
	}


}
