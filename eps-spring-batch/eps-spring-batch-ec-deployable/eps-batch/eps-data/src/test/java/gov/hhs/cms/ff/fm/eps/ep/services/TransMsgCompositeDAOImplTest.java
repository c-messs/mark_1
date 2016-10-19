package gov.hhs.cms.ff.fm.eps.ep.services;


import gov.cms.dsh.bem.BenefitEnrollmentMaintenanceType;
import gov.cms.dsh.bem.BooleanIndicatorSimpleType;
import gov.cms.dsh.bem.HealthCoveragePolicyNumberType;
import gov.cms.dsh.bem.HealthCoverageType;
import gov.cms.dsh.bem.MemberRelatedInfoType;
import gov.cms.dsh.bem.MemberType;
import gov.cms.dsh.bem.PolicyInfoType;
import gov.cms.dsh.bem.TransactionInformationType;
import gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentMaintenanceDTO;
import gov.hhs.cms.ff.fm.eps.ep.data.util.TestDataUtil;
import gov.hhs.cms.ff.fm.eps.ep.enums.EProdEnum;
import gov.hhs.cms.ff.fm.eps.ep.enums.ProcessedToDbInd;
import gov.hhs.cms.ff.fm.eps.ep.po.BatchTransMsgPO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import com.accenture.foundation.common.exception.ApplicationException;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/eps-data-config.xml", "classpath:/test-context-data.xml"})
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class TransMsgCompositeDAOImplTest extends BaseServicesTest {


	@Test
	public void testUpdateBatchTransMsg() throws InterruptedException {


		Long transMsgId = insertTransMsg();
		Long batchId = TestDataUtil.getRandomNumber(9);
		Long expectedBatchId = batchId;  // Same batchId creates and then updates.
		insertBatchTransMsg(transMsgId, batchId);

		ProcessedToDbInd expectedInd = ProcessedToDbInd.S;
		EProdEnum expectedEProd = EProdEnum.EPROD_10;

		// So lastModDT will be different for test.
		Thread.sleep(SLEEP_INTERVAL_SEC);

		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		bemDTO.setTransMsgId(transMsgId);
		bemDTO.setBatchId(batchId);

		transMsgService.updateBatchTransMsg(bemDTO, expectedInd, expectedEProd.getCode(), expectedEProd.getLongNm());

		String sql = "SELECT * FROM BATCHTRANSMSG btm WHERE TRANSMSGID = " + transMsgId + " AND BATCHID = "  + batchId;

		List<Map<String, Object>> actualList = jdbc.queryForList(sql);

		assertEquals("actual BatchTransMsg row", 1, actualList.size());

		Map<String, Object> row = actualList.get(0);

		assertEquals("TransMsgId",new BigDecimal(transMsgId), row.get("TRANSMSGID"));
		assertEquals("BatchId",new BigDecimal(batchId), row.get("BATCHID"));
		assertEquals("ProcessedToDbInd", expectedInd.getValue(), row.get("PROCESSEDTODBSTATUSTYPECD"));
		assertEquals("TransMsgSkipReasonTypeCd", expectedEProd.getCode(), row.get("TRANSMSGSKIPREASONTYPECD"));
		assertEquals("TransMsgSkipReasonDesc", expectedEProd.getLongNm(), row.get("TRANSMSGSKIPREASONDESC"));

		Date createDT = (Date) row.get("createDateTime");
		Date lstModDT = (Date) row.get("lastModifiedDateTime");
		assertNotNull("CreateDateTime", createDT);
		assertNotNull("LastModifiedDateTime", lstModDT);
		assertEquals("CreateBy", batchId.toString(), row.get("CREATEBY"));
		assertEquals("LastModifiedBy", expectedBatchId.toString() , row.get("LASTMODIFIEDBY"));
		assertTrue("The LastModifiedDateTime after CreateDateTime", (lstModDT.getTime() > createDT.getTime()));
	}


	@Test
	public void testUpdateBatchTransMsg_EProdEnum() throws InterruptedException {

		EProdEnum expectedEProd = EProdEnum.EPROD_33;
		ProcessedToDbInd expectedInd = ProcessedToDbInd.S;
		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		Long transMsgId = insertTransMsg();
		bemDTO.setTransMsgId(transMsgId);
		Long batchId = TestDataUtil.getRandomNumber(8);
		Long expectedBatchId = batchId;  // Same batchId creates and then updates.

		bemDTO.setBatchId(batchId);

		insertBatchTransMsg(transMsgId, batchId);

		// So lastModDT will be different for test.
		Thread.sleep(SLEEP_INTERVAL_SEC);

		transMsgService.updateBatchTransMsg(bemDTO, expectedInd, expectedEProd);

		String sql = "SELECT * FROM BATCHTRANSMSG btm WHERE TRANSMSGID = " + transMsgId + " AND BATCHID = "  + batchId;

		List<Map<String, Object>> actualList = jdbc.queryForList(sql);

		assertEquals("actual BatchTransMsg row", 1, actualList.size());

		Map<String, Object> row = actualList.get(0);

		assertEquals("TransMsgId",new BigDecimal(transMsgId), row.get("TRANSMSGID"));
		assertEquals("BatchId",new BigDecimal(batchId), row.get("BATCHID"));
		assertEquals("ProcessedToDbInd", expectedInd.getValue(), row.get("PROCESSEDTODBSTATUSTYPECD"));
		assertEquals("TransMsgSkipReasonTypeCd", expectedEProd.getCode(), row.get("TRANSMSGSKIPREASONTYPECD"));
		assertEquals("TransMsgSkipReasonDesc", expectedEProd.getLongNm(), row.get("TRANSMSGSKIPREASONDESC"));

		Date createDT = (Date) row.get("createDateTime");
		Date lstModDT = (Date) row.get("lastModifiedDateTime");
		assertNotNull("CreateDateTime", createDT);
		assertNotNull("LastModifiedDateTime", lstModDT);
		assertEquals("CreateBy", batchId.toString(), row.get("CREATEBY"));
		assertEquals("LastModifiedBy", expectedBatchId.toString() , row.get("LASTMODIFIEDBY"));
		assertTrue("The LastModifiedDateTime after CreateDateTime", (lstModDT.getTime() > createDT.getTime()));
	}

	/*
	 *  Tests attempting to UPDATE a record in BatchTransMsg table that does
	 *  not exist.  Since, record does not exist for this batchId/transMsgId 
	 *  a new record will be INSERTED.  
	 *  - Simulates a Bem that had been skipped in a previous batch job and is
	 *    being re-run in another batch job.
	 */
	@Test
	public void testUpdateBatchTransMsg_ForBatchTransMsgDNE_EProdEnum() {

		EProdEnum expectedEProd = EProdEnum.EPROD_35;
		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		Long batchId = TestDataUtil.getRandomNumber(8);
		Long expectedBatchId = batchId;  // Same batchId creates since nothing to update.
		bemDTO.setBatchId(batchId);
		ProcessedToDbInd expectedInd = ProcessedToDbInd.I;
		Long transMsgId = insertTransMsg();
		bemDTO.setTransMsgId(transMsgId);
		transMsgService.updateBatchTransMsg(bemDTO, expectedInd, expectedEProd);

		String sql = "SELECT * FROM BATCHTRANSMSG btm WHERE TRANSMSGID = " + transMsgId + " AND BATCHID = "  + batchId;

		List<Map<String, Object>> actualList = jdbc.queryForList(sql);

		assertEquals("actual BatchTransMsg row", 1, actualList.size());

		Map<String, Object> row = actualList.get(0);

		assertEquals("TransMsgId",new BigDecimal(transMsgId), row.get("TRANSMSGID"));
		assertEquals("BatchId",new BigDecimal(batchId), row.get("BATCHID"));
		assertEquals("ProcessedToDbInd", expectedInd.getValue(), row.get("PROCESSEDTODBSTATUSTYPECD"));
		assertEquals("TransMsgSkipReasonTypeCd", expectedEProd.getCode(), row.get("TRANSMSGSKIPREASONTYPECD"));
		assertEquals("TransMsgSkipReasonDesc", expectedEProd.getLongNm(), row.get("TRANSMSGSKIPREASONDESC"));

		Date createDT = (Date) row.get("createDateTime");
		Date lstModDT = (Date) row.get("lastModifiedDateTime");
		assertNotNull("CreateDateTime", createDT);
		assertNotNull("LastModifiedDateTime", lstModDT);
		assertEquals("CreateBy", batchId.toString(), row.get("CREATEBY"));
		assertEquals("LastModifiedBy", expectedBatchId.toString() , row.get("LASTMODIFIEDBY"));
		assertEquals("The LastModifiedDateTime and CreateDateTime are the same", lstModDT.getTime(), createDT.getTime());
	}


	/*
	 * Tests throwing NullPointerException when BatchId is null.
	 * 
	 */
	@Test(expected=java.lang.Exception.class)
	public void testUpdateBatchTransMsg_Exception_Null_BatchId() {

		assertNotNull("TransMsgCompositeDAO", transMsgService);
		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		bemDTO.setTransMsgId(Long.valueOf("888888"));
		
		transMsgService.updateBatchTransMsg(bemDTO, ProcessedToDbInd.S, "EPROD-10", "Skippable error desc.");
	}

	/*
	 * Tests throwing NullPointerException when BatchId is null.
	 * 
	 */
	@Test(expected=java.lang.Exception.class)
	public void testUpdateBatchTransMsg_Exception_Null_TransMsgId() {

		assertNotNull("TransMsgCompositeDAO", transMsgService);
		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		bemDTO.setBatchId(Long.valueOf("777777"));
		
		transMsgService.updateBatchTransMsg(bemDTO, ProcessedToDbInd.Y);
	}

	/*
	 *  Tests UPDATING a record in BatchTransMsg table that does exist.  
	 */
	@Test
	public void testUpdateBatchTransMsg_AllIndicators() {

		ProcessedToDbInd expectedInd = null;
		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		bemDTO.setBatchId(BATCH_ID);
		Long transMsgId = null;
		BatchTransMsgPO actualPO = null;
		ProcessedToDbInd[] inds = {ProcessedToDbInd.S, ProcessedToDbInd.Y};
		assertTrue("ProcessedToDbInd enums size", inds.length > 0);
		for (int i = 0; i < inds.length; ++i) {
			transMsgId = insertTransMsg();
			bemDTO.setTransMsgId(transMsgId);
			expectedInd = inds[i];
			// Update it
			transMsgService.updateBatchTransMsg(bemDTO, expectedInd);
			// Now get it
			actualPO = transMsgService.getBatchTransMsg(BATCH_ID, transMsgId);
			assertNotNull("BatchTransMsgPO", actualPO);
			assertEquals("BatchId", BATCH_ID, actualPO.getBatchId());
			assertEquals("TransMsgId", transMsgId, actualPO.getTransMsgId());
			assertEquals("ProccessedToDbIndicator", expectedInd.getValue(), actualPO.getProcessedToDbStatusTypeCd());
		}	
	}

	/*
	 *  Tests attempting to UPDATE a record in BatchTransMsg table that does
	 *  not exist.  Since, record does not exist for this batchId/transMsgId 
	 *  a new record will be INSERTED.  
	 *  - Simulates a Bem that had been skipped in a previous batch job and is
	 *    being re-run in another batch job.
	 */
	@Test
	public void testUpdateBatchTransMsg_ForBatchTransMsgDNE() {

		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		bemDTO.setBatchId(BATCH_ID);
		ProcessedToDbInd expectedInd = ProcessedToDbInd.Y;
		Long transMsgId = insertTransMsg();
		bemDTO.setTransMsgId(transMsgId);
		transMsgService.updateBatchTransMsg(bemDTO, expectedInd);

		BatchTransMsgPO po = transMsgService.getBatchTransMsg(BATCH_ID, transMsgId);

		assertNotNull("BatchTransMsgPO", po);
		assertEquals("BatchId", BATCH_ID, po.getBatchId());
		assertEquals("TransMsgId", transMsgId, po.getTransMsgId());
		assertEquals("ProccessedToDbIndicator",expectedInd.getValue(), po.getProcessedToDbStatusTypeCd());
	}


	@Test
	public void testUpdateProccessedToDbIndicator_AllIndicatorsAndNull() {

		ProcessedToDbInd expectedInd = null;
		Long expectedBatchId = TestDataUtil.getRandomNumber(8);
		Long expectedTransMsgId = null;
		BatchTransMsgPO actualPO = null;
		ProcessedToDbInd[] inds = ProcessedToDbInd.values();
		assertTrue("ProcessedToDbInd enums size", inds.length > 0);
		for (int i = 0; i < inds.length + 1; ++i) {
			expectedTransMsgId = insertTransMsg();
			if (i < inds.length) {
				insertBatchTransMsg(expectedTransMsgId, expectedBatchId);
				expectedInd = inds[i];
			} else {
				//Dont insert batchTransMsg to test !isSuccess in updateProcessedToDbIndicator.
				expectedInd = null;
			}
			// Update it
			transMsgService.updateProcessedToDbIndicator(expectedBatchId, expectedTransMsgId, expectedInd);
			// Now get it
			actualPO = transMsgService.getBatchTransMsg(expectedBatchId, expectedTransMsgId);
			assertNotNull(expectedInd + ": BatchTransMsgPO", actualPO);
			assertEquals(expectedInd + ": BatchId", expectedBatchId, actualPO.getBatchId());
			assertEquals(expectedInd + ": TransMsgId", expectedTransMsgId, actualPO.getTransMsgId());
			if (expectedInd != null) {
				assertEquals(expectedInd + ": ProccessedToDbIndicator", expectedInd.getValue(), actualPO.getProcessedToDbStatusTypeCd());
			} else {
				assertNull("ProccessedToDbIndicator is null in EPS", actualPO.getProcessedToDbStatusTypeCd());
			}
		}
	}


	@Test(expected=com.accenture.foundation.common.exception.ApplicationException.class)
	public void testGetSkippedTransMsgCount_Exception() {

		assertNotNull("transMsgService", transMsgService);
		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		Long transMsgId = insertTransMsg();
		// Insert a batchTrans without policy attributes.
		insertBatchTransMsg(transMsgId, BATCH_ID, ProcessedToDbInd.Y);

		bemDTO.setTransMsgId(transMsgId);
		bemDTO.setBatchId(BATCH_ID);

		transMsgService.getSkippedTransMsgCount(bemDTO);
	}

	@Test
	public void testGetSkippedTransMsgCount_0() {

		Integer expected = 0;
		String stateCd = "VA";
		String exchangePolicyId = TestDataUtil.getRandomNumberAsString(8);
		String hiosId = exchangePolicyId.substring(0, 5);
		int versionNum = TestDataUtil.getRandom3DigitNumber().intValue();
		String versionNumStr = versionNum + "";

		// Insert a transMsg and Batch and increment versionId
		Long transMsgId = insertTransMsg();
		insertBatchTransMsg(transMsgId, BATCH_ID);
		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setTransactionInformation(new TransactionInformationType());
		bem.getTransactionInformation().setPolicySnapshotVersionNumber(versionNumStr);
		bem.setPolicyInfo(new PolicyInfoType());
		bem.getPolicyInfo().setGroupPolicyNumber(exchangePolicyId);
		bem.getMember().add(makeSubscriber(stateCd, exchangePolicyId, hiosId));
		bemDTO.setBem(bem);
		bemDTO.setTransMsgId(transMsgId);
		bemDTO.setBatchId(BATCH_ID);		

		Integer actual = transMsgService.getSkippedTransMsgCount(bemDTO);

		assertEquals("Skipped count", expected, actual);
	}

	@Test
	public void testGetSkippedTransMsgCount_1() {

		Integer expected = 1;

		String stateCd = "VA";
		String exchangePolicyId = TestDataUtil.getRandomNumberAsString(8);
		String hiosId = exchangePolicyId.substring(0, 5);
		int versionNum = TestDataUtil.getRandom3DigitNumber().intValue();
		String versionNumStr = versionNum + "";
		LocalDateTime versionDt = APR_1_4am;


		// Insert a batchTrans's with same policy info
		Long transMsgId = insertTransMsg();
		insertBatchTransMsg(transMsgId, BATCH_ID, ProcessedToDbInd.Y, stateCd, exchangePolicyId, hiosId, versionNumStr, versionDt);
		// Insert a skipped batchTransMsg and increment versionId.
		transMsgId = insertTransMsg();
		versionNum++;
		versionNumStr = versionNum +"";
		insertBatchTransMsg(transMsgId, BATCH_ID, ProcessedToDbInd.S, stateCd, exchangePolicyId, hiosId, versionNumStr, versionDt);

		// Insert a transMsg and Batch and increment versionId
		transMsgId = insertTransMsg();
		insertBatchTransMsg(transMsgId, BATCH_ID);
		versionNum++;
		versionNumStr = versionNum +"";
		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setTransactionInformation(new TransactionInformationType());
		bem.getTransactionInformation().setPolicySnapshotVersionNumber(versionNumStr);
		bem.setPolicyInfo(new PolicyInfoType());
		bem.getPolicyInfo().setGroupPolicyNumber(exchangePolicyId);
		bem.getMember().add(makeSubscriber(stateCd, exchangePolicyId, hiosId));
		bemDTO.setBem(bem);
		bemDTO.setTransMsgId(transMsgId);
		bemDTO.setBatchId(BATCH_ID);		

		Integer actual = transMsgService.getSkippedTransMsgCount(bemDTO);

		assertEquals("Skipped count", expected, actual);
	}

	@Test
	public void testUpdateSkippedVersion_1() {

		Integer expected = 1;

		String stateCd = "VA";
		String exchangePolicyId = TestDataUtil.getRandomNumberAsString(8);
		String hiosId = exchangePolicyId.substring(0, 5);
		int versionNum = TestDataUtil.getRandom3DigitNumber().intValue();
		String versionNumStr = versionNum + "";
		LocalDateTime versionDt = APR_1_4am;

		// Insert a batchTrans's with same policy info
		Long transMsgId = insertTransMsg();
		insertBatchTransMsg(transMsgId, 9999999999998L, ProcessedToDbInd.Y, stateCd, exchangePolicyId, hiosId, versionNumStr, versionDt);
		// Insert a skipped batchTransMsg and increment versionId.
		transMsgId = insertTransMsg();
		versionNum++;
		versionNumStr = versionNum +"";
		insertBatchTransMsg(transMsgId, 9999999999998L, ProcessedToDbInd.S, stateCd, exchangePolicyId, hiosId, versionNumStr, versionDt);

		// Insert a transMsg and Batch and increment versionId
		transMsgId = insertTransMsg();
		insertBatchTransMsg(transMsgId, BATCH_ID);
		//DO NOT increment the versionId for this bem, so it will find the skipped one with the same versionId.
		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setTransactionInformation(new TransactionInformationType());
		bem.getTransactionInformation().setPolicySnapshotVersionNumber(versionNumStr);
		bem.setPolicyInfo(new PolicyInfoType());
		bem.getPolicyInfo().setGroupPolicyNumber(exchangePolicyId);
		bem.getMember().add(makeSubscriber(stateCd, exchangePolicyId, hiosId));
		bemDTO.setBem(bem);
		bemDTO.setTransMsgId(transMsgId);
		bemDTO.setBatchId(BATCH_ID);		

		Integer actual = transMsgService.updateSkippedVersion(bemDTO, ProcessedToDbInd.D);

		assertEquals("Updated count", expected, actual);

		//Test updating again now that it has a status of D.
		expected = 0;
		actual = transMsgService.updateSkippedVersion(bemDTO, null);
		assertEquals("Updated count", expected, actual);
	}

	@Test
	public void testUpdateSkippedVersion_Verify_LastModDT() throws InterruptedException {

		Integer expected = 1;
		ProcessedToDbInd expectedInd = ProcessedToDbInd.D;

		String stateCd = "VA";
		String exchangePolicyId = TestDataUtil.getRandomNumberAsString(8);
		String hiosId = exchangePolicyId.substring(0, 5);
		int versionNum = TestDataUtil.getRandom3DigitNumber().intValue();
		String versionNumStr = versionNum + "";
		LocalDateTime versionDt = APR_1_4am;

		// Insert a batchTrans's with same policy info
		Long batchId_Original = TestDataUtil.getRandomNumber(9);
		Long transMsgId_Original = insertTransMsg();
		insertBatchTransMsg(transMsgId_Original, batchId_Original, ProcessedToDbInd.S, stateCd, exchangePolicyId, hiosId, versionNumStr, versionDt);

		// Insert a transMsg and Batch and increment versionId
		Long transMsgId = insertTransMsg();
		Long batchId = batchId_Original + 1 ; // add to 1 to simulate next batchId or a future one.
		insertBatchTransMsg(transMsgId, batchId);
		//DO NOT increment the versionId for this bem, so it will find the skipped one with the same versionId.
		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setTransactionInformation(new TransactionInformationType());
		bem.getTransactionInformation().setPolicySnapshotVersionNumber(versionNumStr);
		bem.setPolicyInfo(new PolicyInfoType());
		bem.getPolicyInfo().setGroupPolicyNumber(exchangePolicyId);
		bem.getMember().add(makeSubscriber(stateCd, exchangePolicyId, hiosId));
		bemDTO.setBem(bem);
		bemDTO.setTransMsgId(transMsgId);
		bemDTO.setBatchId(batchId);		

		// So lastModDT will be different for test since stored only to seconds (DATE).
		Thread.sleep(SLEEP_INTERVAL_SEC);

		Integer actual = transMsgService.updateSkippedVersion(bemDTO, ProcessedToDbInd.D);

		assertEquals("Updated count", expected, actual);

		String sql = "SELECT * FROM BATCHTRANSMSG btm WHERE TRANSMSGID = " + transMsgId_Original + " AND BATCHID = "  + batchId_Original;

		List<Map<String, Object>> actualList = jdbc.queryForList(sql);

		assertEquals("actual BatchTransMsg row", 1, actualList.size());

		Map<String, Object> row = actualList.get(0);

		assertEquals("TransMsgId",new BigDecimal(transMsgId_Original), row.get("TRANSMSGID"));
		assertEquals("BatchId",new BigDecimal(batchId_Original), row.get("BATCHID"));
		assertEquals("ProcessedToDbInd", expectedInd.getValue(), row.get("PROCESSEDTODBSTATUSTYPECD"));
		assertNull("TransMsgSkipReasonTypeCd", row.get("TRANSMSGSKIPREASONTYPECD"));
		assertNull("TransMsgSkipReasonDesc", row.get("TRANSMSGSKIPREASONDESC"));

		Date createDT = (Date) row.get("createDateTime");
		Date lstModDT = (Date) row.get("lastModifiedDateTime");
		assertNotNull("CreateDateTime", createDT);
		assertNotNull("LastModifiedDateTime", lstModDT);
		assertEquals("CreateBy", batchId_Original.toString(), row.get("CREATEBY"));
		assertEquals("LastModifiedBy", batchId.toString() , row.get("LASTMODIFIEDBY"));
		assertTrue("The LastModifiedDateTime after CreateDateTime", (lstModDT.getTime() > createDT.getTime()));

	}


	@Test
	public void testUpdateLaterVersions() throws InterruptedException {

		Integer expected = 1;
		ProcessedToDbInd expectedInd = ProcessedToDbInd.R;

		String stateCd = "VA";
		String exchangePolicyId = TestDataUtil.getRandomNumberAsString(8);
		String hiosId = exchangePolicyId.substring(0, 5);
		int versionNum = 1;
		String versionNumStr = versionNum + "";
		LocalDateTime versionDt = APR_1_4am;

		// Insert a batchTrans's with same policy info
		Long batchId_Original = TestDataUtil.getRandomNumber(9);
		Long transMsgId_1 = insertTransMsg();
		insertBatchTransMsg(transMsgId_1, batchId_Original, ProcessedToDbInd.D, stateCd, exchangePolicyId, hiosId, versionNumStr, versionDt);

		// So lastModDT will be different for test since stored only to seconds (DATE).
		Thread.sleep(SLEEP_INTERVAL_SEC);

		// Insert a skipped batchTransMsg and increment versionId.
		Long transMsgId_2 = insertTransMsg();
		versionNum++;
		versionNumStr = versionNum +"";
		insertBatchTransMsg(transMsgId_2, batchId_Original, ProcessedToDbInd.S, stateCd, exchangePolicyId, hiosId, versionNumStr, versionDt);

		// So lastModDT will be different for test since stored only to seconds (DATE).
		Thread.sleep(SLEEP_INTERVAL_SEC);

		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setTransactionInformation(new TransactionInformationType());
		--versionNum;
		versionNumStr = versionNum +"";
		bem.getTransactionInformation().setPolicySnapshotVersionNumber(versionNumStr);
		bem.setPolicyInfo(new PolicyInfoType());
		bem.getPolicyInfo().setGroupPolicyNumber(exchangePolicyId);
		bem.getMember().add(makeSubscriber(stateCd, exchangePolicyId, hiosId));
		bemDTO.setBem(bem);
		bemDTO.setTransMsgId(transMsgId_1);
		Long batchId = batchId_Original + 1;
		bemDTO.setBatchId(batchId);		

		Integer actual = transMsgService.updateLaterVersions(bemDTO, ProcessedToDbInd.R);

		assertEquals("Updated count", expected, actual);

		// Confirm the "skipped" transaction from the original batch gets set to 'R' from this batch run.
		String sql = "SELECT * FROM BATCHTRANSMSG btm WHERE TRANSMSGID = " + transMsgId_2 + " AND BATCHID = "  + batchId_Original;

		List<Map<String, Object>> actualList = jdbc.queryForList(sql);

		assertEquals("actual BatchTransMsg row", 1, actualList.size());

		Map<String, Object> row = actualList.get(0);

		assertEquals("TransMsgId",new BigDecimal(transMsgId_2), row.get("TRANSMSGID"));
		assertEquals("BatchId",new BigDecimal(batchId_Original), row.get("BATCHID"));
		assertEquals("ProcessedToDbInd", expectedInd.getValue(), row.get("PROCESSEDTODBSTATUSTYPECD"));
		assertNull("TransMsgSkipReasonTypeCd", row.get("TRANSMSGSKIPREASONTYPECD"));
		assertNull("TransMsgSkipReasonDesc", row.get("TRANSMSGSKIPREASONDESC"));

		Date createDT = (Date) row.get("createDateTime");
		Date lstModDT = (Date) row.get("lastModifiedDateTime");
		assertNotNull("CreateDateTime", createDT);
		assertNotNull("LastModifiedDateTime", lstModDT);
		assertEquals("CreateBy", batchId_Original.toString(), row.get("CREATEBY"));
		assertEquals("LastModifiedBy", batchId.toString() , row.get("LASTMODIFIEDBY"));
		assertTrue("The LastModifiedDateTime after CreateDateTime", (lstModDT.getTime() > createDT.getTime()));
	}

	@Test
	public void testGetSkippedVersionCount_0() {

		Integer expected = 0;

		String stateCd = "VA";
		String exchangePolicyId = TestDataUtil.getRandomNumberAsString(8);
		String hiosId = exchangePolicyId.substring(0, 5);
		int versionNum = TestDataUtil.getRandom3DigitNumber().intValue();
		String versionNumStr = versionNum + "";
		LocalDateTime versionDt = APR_1_4am;

		// Insert a batchTrans's with same policy info
		Long transMsgId = insertTransMsg();
		insertBatchTransMsg(transMsgId, BATCH_ID, ProcessedToDbInd.Y, stateCd, exchangePolicyId, hiosId, versionNumStr, versionDt);
		// Insert a skipped batchTransMsg and increment versionId.
		transMsgId = insertTransMsg();
		versionNum++;
		versionNumStr = versionNum +"";
		insertBatchTransMsg(transMsgId, BATCH_ID, ProcessedToDbInd.Y, stateCd, exchangePolicyId, hiosId, versionNumStr, versionDt);

		// Insert a transMsg and Batch and increment versionId
		transMsgId = insertTransMsg();
		insertBatchTransMsg(transMsgId, BATCH_ID);
		//DO NOT increment the versionId for this bem, so it will find the skipped one with the same versionId.
		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setTransactionInformation(new TransactionInformationType());
		bem.getTransactionInformation().setPolicySnapshotVersionNumber(versionNumStr);
		bem.setPolicyInfo(new PolicyInfoType());
		bem.getPolicyInfo().setGroupPolicyNumber(exchangePolicyId);
		bem.getMember().add(makeSubscriber(stateCd, exchangePolicyId, hiosId));
		bemDTO.setBem(bem);
		bemDTO.setTransMsgId(transMsgId);
		bemDTO.setBatchId(1L);		

		Integer actual = transMsgService.getSkippedVersionCount(bemDTO);

		assertEquals("Updated count", expected, actual);

	}


	@Test
	public void testGetSkippedVersionCount_1() {

		Integer expected = 1;

		String stateCd = "VA";
		String exchangePolicyId = TestDataUtil.getRandomNumberAsString(8);
		String hiosId = exchangePolicyId.substring(0, 5);
		int versionNum = TestDataUtil.getRandom3DigitNumber().intValue();
		String versionNumStr = versionNum + "";
		LocalDateTime versionDt = APR_1_4am;

		// Insert a batchTrans's with same policy info
		Long transMsgId = insertTransMsg();
		insertBatchTransMsg(transMsgId, BATCH_ID, ProcessedToDbInd.Y, stateCd, exchangePolicyId, hiosId, versionNumStr, versionDt);
		// Insert a skipped batchTransMsg and increment versionId.
		transMsgId = insertTransMsg();
		versionNum++;
		versionNumStr = versionNum +"";
		insertBatchTransMsg(transMsgId, BATCH_ID, ProcessedToDbInd.S, stateCd, exchangePolicyId, hiosId, versionNumStr, versionDt);

		// Insert a transMsg and Batch and increment versionId
		transMsgId = insertTransMsg();
		insertBatchTransMsg(transMsgId, BATCH_ID);
		//DO NOT increment the versionId for this bem, so it will find the skipped one with the same versionId.
		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setTransactionInformation(new TransactionInformationType());
		bem.getTransactionInformation().setPolicySnapshotVersionNumber(versionNumStr);
		bem.setPolicyInfo(new PolicyInfoType());
		bem.getPolicyInfo().setGroupPolicyNumber(exchangePolicyId);
		bem.getMember().add(makeSubscriber(stateCd, exchangePolicyId, hiosId));
		bemDTO.setBem(bem);
		bemDTO.setTransMsgId(transMsgId);
		bemDTO.setBatchId(1L);		

		Integer actual = transMsgService.getSkippedVersionCount(bemDTO);

		assertEquals("Updated count", expected, actual);
	}

	@Test
	public void testVerifyInputCriteria_EPROD30() {

		String expectedError = "EPROD-30";
		String actualError = null;

		BatchTransMsgPO po = new BatchTransMsgPO();
		// Set for coverage
		po.setCreateBy("unitTest");
		po.setLastModifiedBy("unitTest");

		try {
			ReflectionTestUtils.invokeMethod(transMsgService, "verifyInputCriteria", po);
		} catch(ApplicationException appEx) {
			assertTrue("ApplicationException thrown for missing  from exchangePolicyId", true);
			actualError = appEx.getMessage().substring(0, expectedError.length());
		}
		assertEquals("Error", expectedError, actualError);
	}

	@Test
	public void testVerifyInputCriteria_EPROD31() {

		String expectedError = "EPROD-31";
		String actualError = null;

		BatchTransMsgPO po = new BatchTransMsgPO();
		po.setExchangePolicyId(TestDataUtil.getRandomNumberAsString(9));

		try {
			ReflectionTestUtils.invokeMethod(transMsgService, "verifyInputCriteria", po);
		} catch(ApplicationException appEx) {
			assertTrue("ApplicationException thrown for missing  from SubscriberStateCd", true);
			actualError = appEx.getMessage().substring(0, expectedError.length());
		}
		assertEquals("Error", expectedError, actualError);
	}

	@Test
	public void testVerifyInputCriteria_EPROD32() {

		String expectedError = "EPROD-32";
		String actualError = null;

		BatchTransMsgPO po = new BatchTransMsgPO();
		po.setExchangePolicyId(TestDataUtil.getRandomNumberAsString(9));
		po.setSubscriberStateCd("VA");

		try {
			ReflectionTestUtils.invokeMethod(transMsgService, "verifyInputCriteria", po);
		} catch(ApplicationException appEx) {
			assertTrue("ApplicationException thrown for missing  from IssuerHiosID (ContractCode)", true);
			actualError = appEx.getMessage().substring(0, expectedError.length());
		}
		assertEquals("Error", expectedError, actualError);
	}

	@Test
	public void testVerifyInputCriteria_EPROD33() {

		String expectedError = "EPROD-33";
		String actualError = null;

		BatchTransMsgPO po = new BatchTransMsgPO();
		po.setExchangePolicyId(TestDataUtil.getRandomNumberAsString(9));
		po.setSubscriberStateCd("VA");
		po.setIssuerHiosId(TestDataUtil.getRandomNumberAsString(5));

		try {
			ReflectionTestUtils.invokeMethod(transMsgService, "verifyInputCriteria", po);
		} catch(ApplicationException appEx) {
			assertTrue("ApplicationException thrown for missing  from PolicySnapshotVersionNumber (SourceVersionId)", true);
			actualError = appEx.getMessage().substring(0, expectedError.length());
		}
		assertEquals("Error", expectedError, actualError);
	}


	private MemberType makeSubscriber(String stateCd, String exchangePolicyId, String hiosId) {

		String contractCode = hiosId.concat(stateCd).concat(exchangePolicyId);

		MemberType member = new MemberType();
		member.setMemberInformation(new MemberRelatedInfoType());
		member.getMemberInformation().setSubscriberIndicator(BooleanIndicatorSimpleType.Y);

		HealthCoverageType hcType = new HealthCoverageType();
		hcType.setHealthCoveragePolicyNumber(new HealthCoveragePolicyNumberType());
		hcType.getHealthCoveragePolicyNumber().setContractCode(contractCode);
		member.getHealthCoverage().add(hcType);
		return member;
	}

}

