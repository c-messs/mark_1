package gov.hhs.cms.ff.fm.eps.ep.sbm.services;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import gov.cms.dsh.sbmi.FileInformationType;
import gov.cms.dsh.sbmi.PolicyMemberType;
import gov.cms.dsh.sbmi.PolicyMemberType.MemberDates;
import gov.cms.dsh.sbmi.PolicyType;
import gov.hhs.cms.ff.fm.eps.ep.dao.mappers.SbmPolicyPremiumRowMapper;
import gov.hhs.cms.ff.fm.eps.ep.dao.mappers.SbmTransMsgRowMapper;
import gov.hhs.cms.ff.fm.eps.ep.dao.mappers.SbmTransMsgValidationRowMapper;
import gov.hhs.cms.ff.fm.eps.ep.enums.PolicyStatus;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMErrorWarningCode;
import gov.hhs.cms.ff.fm.eps.ep.enums.SbmTransMsgStatus;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmPolicyPremiumPO;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmTransMsgPO;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmTransMsgValidationPO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileProcessingDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMPolicyDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMPremium;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SbmErrWarningLogDTO;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;
import gov.hhs.cms.ff.fm.eps.ep.util.sbm.TestDataSBMUtility;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/sbmi-data-config.xml", "classpath:/test-context-data.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class SbmXprServiceImplTest extends  BaseSbmServicesTest {
	
	@Autowired
	private SbmXprService sbmXprCompositeDao;
	
	
	
	/**
	 * Tests inserting into Staging policy and member tables.
	 * Verifies
	 *   - StagingPolicyVersion 
	 *   - StagingPolicyPremium
	 */
	@Test
	public void test_savePolicyToStaging_Eps_null() {

		assertNotNull("sbmXprCompositeDao should not be null", sbmXprCompositeDao);
		
		int rcn = 6;
		Long batchId = 7700000000L + TestDataSBMUtility.getRandomNumberAsLong(4);
		String state = TestDataSBMUtility.SBM_STATES[TestDataSBMUtility.getRandomNumber(1)];
		String issuerId = "33333";
		String tenantId = state + "0";
		String qhpId = TestDataSBMUtility.makeQhpId(issuerId, tenantId);
		String exchangePolicyId = TestDataSBMUtility.getRandomNumberAsString(9);
		String fileId = TestDataSBMUtility.getRandomNumberAsString(6);
		
		Long memIdPre = TestDataSBMUtility.getRandomNumberAsLong(3) * 1000;

		SBMPolicyDTO policyDTO = new SBMPolicyDTO();
		policyDTO.setFileProcessDateTime(JAN_1_1am);
		policyDTO.setBatchId(batchId);
		policyDTO.setFileInfo(TestDataSBMUtility.makeFileInformationType(tenantId, issuerId, TestDataSBMUtility.FILES_ONE_PER_ISSUER));
		
		PolicyType policy = TestDataSBMUtility.makePolicyType(rcn, qhpId, exchangePolicyId);
		
		policy.getMemberInformation().add(TestDataSBMUtility.makePolicyMemberType(exchangePolicyId, (memIdPre + 1), TestDataSBMUtility.MEM_NAMES[0], true));
	    policy.getMemberInformation().add(TestDataSBMUtility.makePolicyMemberType(exchangePolicyId, (memIdPre + 2), TestDataSBMUtility.MEM_NAMES[1], false));
		  
		policyDTO.setPolicy(policy);
		
		String policyXml = TestDataSBMUtility.getPolicyAsXmlString(policy);
		policyDTO.setPolicyXml(policyXml);

		SBMPremium sbmPremium = TestDataSBMUtility.makeSBMPremium(exchangePolicyId);
		policyDTO.getSbmPremiums().put(sbmPremium.getEffectiveStartDate(), sbmPremium);

		SBMFileProcessingDTO fileDTO = insertParentFileRecords(tenantId, fileId);
		policyDTO.setSbmFileInfoId(fileDTO.getSbmFileInfo().getSbmFileInfoId());
		
		Long stagingSbmPolicyid = insertStagingSbmPolicy(fileDTO.getSbmFileProcSumId(), fileDTO.getSbmFileInfo().getSbmFileInfoId(), policyXml);
		policyDTO.setStagingSbmPolicyid(stagingSbmPolicyid);
		

		sbmXprCompositeDao.saveXprTransaction(policyDTO);

		// Confirm policy data, list size 1
		List<Map<String, Object>> actualList = selectStagingPolicyVersionLatest(1, exchangePolicyId, state);

		Map<String, Object> row = actualList.get(0);

		Long actualPolicyVersionId = ((BigDecimal) row.get("POLICYVERSIONID")).longValue();

		assertNotNull("MaintenanceStartDate", row.get("MAINTENANCESTARTDATETIME"));
		assertEquals("MaintenanceEndDate", DateTimeUtil.getSqlTimestamp(DateTimeUtil.HIGHDATE), (Timestamp) row.get("MAINTENANCEENDDATETIME"));
		assertEquals("ExchangPolicyId", exchangePolicyId, row.get("EXCHANGEPOLICYID"));
		assertEquals("SubscriberStateCd", state, row.get("SUBSCRIBERSTATECD"));

		PolicyType expectedPolicy = policyDTO.getPolicy();
		FileInformationType expectedFileInfoType = policyDTO.getFileInfo();

		assertEquals("IssuerAssignedPolicyId", expectedPolicy.getIssuerAssignedPolicyId(), row.get("ISSUERPOLICYID"));
		assertEquals("IssuerId", expectedFileInfoType.getIssuerFileInformation().getIssuerId(), row.get("ISSUERHIOSID"));
		assertEquals("IssuerAssignedPolicyId", expectedPolicy.getIssuerAssignedSubscriberId(), row.get("ISSUERSUBSCRIBERID"));
		assertEquals("ExchangeAssignedSubscriberId", expectedPolicy.getExchangeAssignedSubscriberId(), row.get("EXCHANGEASSIGNEDSUBSCRIBERID"));
		assertEquals("TransControlNum", String.valueOf(expectedPolicy.getRecordControlNumber()), row.get("TRANSCONTROLNUM"));
		//TODO		assertEquals("SourceExchangeId",, row.get("SOURCEEXCHANGEID"));
		assertEquals("PlanId/QhpId", expectedPolicy.getQHPId(), row.get("PLANID"));
		assertEquals("InsuranceLineCode", expectedPolicy.getInsuranceLineCode(), row.get("X12INSRNCLINETYPECD"));
		Timestamp psd =  (Timestamp) row.get("POLICYSTARTDATE");
		assertEquals("PolicyStartDate", DateTimeUtil.getLocalDateFromXmlGC(expectedPolicy.getPolicyStartDate()).format(dtFmtr), 
				psd.toLocalDateTime().format(dtFmtr));
		Timestamp ped =  (Timestamp) row.get("POLICYENDDATE");
		assertEquals("PolicyEndDate", DateTimeUtil.getLocalDateFromXmlGC(expectedPolicy.getPolicyEndDate()).format(dtFmtr), 
				ped.toLocalDateTime().format(dtFmtr));
		//assertEquals("SbmTransMsgId",new BigDecimal(sbmTransMsgId), (BigDecimal) row.get("SBMTRANSMSGID"));

		assertSysData(row, batchId);

		String sql = "SELECT * FROM STAGINGPOLICYPREMIUM WHERE POLICYVERSIONID = " + actualPolicyVersionId;

		SbmPolicyPremiumPO actual = (SbmPolicyPremiumPO) jdbc.queryForObject(sql, new SbmPolicyPremiumRowMapper());

		assertEquals("EffectiveStartDate", sbmPremium.getEffectiveStartDate(), actual.getEffectiveStartDate());
		assertEquals("EffectiveEndDate", sbmPremium.getEffectiveEndDate(), actual.getEffectiveEndDate());
		assertEquals("TotalPremiumAmount", sbmPremium.getTotalPremium().doubleValue(), actual.getTotalPremiumAmount().doubleValue());
		assertEquals("IndividualResponsibleAmount", sbmPremium.getIndividualResponsibleAmt().doubleValue(), 
				actual.getIndividualResponsibleAmount().doubleValue());
		assertEquals("RatingArea", sbmPremium.getRatingArea(), actual.getExchangeRateArea());
		assertEquals("APTC", sbmPremium.getAptc().doubleValue(), actual.getAptcAmount().doubleValue());
		assertEquals("CSR", sbmPremium.getCsr().doubleValue(), actual.getCsrAmount().doubleValue());
		
		// TODO test prorated amounts.
		//assertEquals("PRO-TPA", sbmPremium.getProratedPremium().doubleValue(), actual.getProratedPremiumAmount().doubleValue());
		//assertEquals("PRO-APTC", sbmPremium.getProratedAptc().doubleValue(), actual.getProratedAptcAmount().doubleValue());
		//assertEquals("PRO-CSR", sbmPremium.getProratedCsr().doubleValue(), actual.getProratedCsrAmount().doubleValue());
		// currently amounts are null.
		assertEquals("PRO-TPA", sbmPremium.getProratedPremium(), actual.getProratedPremiumAmount());
		assertEquals("PRO-APTC", sbmPremium.getProratedAptc(), actual.getProratedAptcAmount());
		assertEquals("PRO-CSR", sbmPremium.getProratedCsr(), actual.getProratedCsrAmount());
		
		assertEquals("CSRVariantId", sbmPremium.getCsrVariantId(), actual.getInsrncPlanVariantCmptTypeCd());
		assertEquals("OtherPayment1", sbmPremium.getOtherPayment1().doubleValue(), actual.getOtherPaymentAmount1().doubleValue());
		assertEquals("OtherPayment2", sbmPremium.getOtherPayment2().doubleValue(), actual.getOtherPaymentAmount2().doubleValue());

		assertNull("PRO-TIRA", actual.getProratedInddResponsibleAmount());

		assertTrue("isPolicyChanged", actual.isPolicyChanged());
		
		//TODO verify member data

	}

	/**
	 * Test NOT saving to staging when the next cycle policy is the same (value to value) as EPS.
	 * @throws InterruptedException
	 */
	@Test
	public void test_savePolicyToStaging_No_SBM_Change() throws InterruptedException {

		int rcn = 6;
		Long batchId = 6000000 + TestDataSBMUtility.getRandomNumberAsLong(4);
		userVO.setUserId(batchId.toString());
		String stateCd = TestDataSBMUtility.SBM_STATES[TestDataSBMUtility.getRandomNumber(1)];
		String issuerId = "44444";
		String tenantId = stateCd + "0";
		String qhpId = TestDataSBMUtility.makeQhpId(issuerId, tenantId);
		String exchangePolicyId = TestDataSBMUtility.getRandomNumberAsString(9);
		String fileId = "600" + TestDataSBMUtility.getRandomNumberAsString(3);
	
		Long memIdPre = TestDataSBMUtility.getRandomNumberAsLong(3) * 1000;

		// Make an EPS PolicyVersion (simulate existing policy from last month's job run)
		SBMPolicyDTO policyDTO = new SBMPolicyDTO();
		policyDTO.setFileProcessDateTime(JAN_1_1am);
		policyDTO.setBatchId(batchId);
		policyDTO.setFileInfo(TestDataSBMUtility.makeFileInformationType(tenantId, issuerId, TestDataSBMUtility.FILES_ONE_PER_ISSUER));
		
		PolicyType policy = TestDataSBMUtility.makePolicyType(rcn, qhpId, exchangePolicyId);
	   
		PolicyMemberType member1 = TestDataSBMUtility.makePolicyMemberType(exchangePolicyId, (memIdPre + 1), TestDataSBMUtility.MEM_NAMES[0], true);
		PolicyMemberType member2 = TestDataSBMUtility.makePolicyMemberType(exchangePolicyId, (memIdPre + 2), TestDataSBMUtility.MEM_NAMES[1], false);
	    policy.getMemberInformation().add(member1);
	    policy.getMemberInformation().add(member2);
		
		policyDTO.setPolicy(policy);
		
		String policyXml = TestDataSBMUtility.getPolicyAsXmlString(policy);
		policyDTO.setPolicyXml(policyXml);
		
		SBMPremium sbmPremium = TestDataSBMUtility.makeSBMPremium(exchangePolicyId);
		sbmPremium.setAptc(new BigDecimal(".5"));
		sbmPremium.setCsr(new BigDecimal("0"));
		policyDTO.getSbmPremiums().put(sbmPremium.getEffectiveStartDate(), sbmPremium);

		SBMFileProcessingDTO fileDTO = insertParentFileRecords(tenantId, fileId);
		Long sbmTransMsgId = insertSbmTransMsg(fileDTO.getSbmFileInfo().getSbmFileInfoId(), stateCd, exchangePolicyId, SbmTransMsgStatus.ACCEPTED_WITH_SBM_CHANGE);
		policyDTO.setSbmTransMsgId(sbmTransMsgId);

		Long pvId1 = insertPolicyVersion(JAN_1_1am, qhpId, policyDTO);
		insertPolicyPremium(pvId1, sbmPremium);
		insertPolicyStatus(pvId1, JAN_1_1am, PolicyStatus.EFFECTUATED_2);
		Long pmvId1 = insertPolicyMemberVersion(stateCd, JAN_1_1am, exchangePolicyId, member1);
		Long pmvId2 = insertPolicyMemberVersion(stateCd, JAN_1_1am, exchangePolicyId, member2);
		
		insertPolicyMemberAddress(pmvId1, stateCd, member1.getPostalCode());
		insertPolicyMemberAddress(pmvId2, stateCd, member2.getPostalCode());
		
		insertMemberPolicyRaceEthnicity(pmvId1, member1.getRaceEthnicityCode());
		insertMemberPolicyRaceEthnicity(pmvId2, member2.getRaceEthnicityCode());
		
		insertPolicyMemberLanguageAbility(pmvId1, member1.getLanguageCode(), member1.getLanguageQualifierCode());
		insertPolicyMemberLanguageAbility(pmvId2, member2.getLanguageCode(), member2.getLanguageQualifierCode());
		
		MemberDates mDts1 = member1.getMemberDates().get(0);
		MemberDates mDts2 = member2.getMemberDates().get(0);
		
		insertPolicyMemberDate(pmvId1, DateTimeUtil.getLocalDateFromXmlGC(mDts1.getMemberStartDate()), DateTimeUtil.getLocalDateFromXmlGC(mDts1.getMemberEndDate()));
		insertPolicyMemberDate(pmvId2, DateTimeUtil.getLocalDateFromXmlGC(mDts2.getMemberStartDate()), DateTimeUtil.getLocalDateFromXmlGC(mDts2.getMemberEndDate()));
		
		insertPolicyMember(stateCd, pvId1, pmvId1);
		insertPolicyMember(stateCd, pvId1, pmvId2);
		
		Thread.sleep(1005L);

		// Make another policy with all the same data.  No need to make parentFileInfo since this 
		// transaction should NOT be saved to sbmTransmsg.
		
		SBMPolicyDTO policyDTO2 = new SBMPolicyDTO();
		// Set PVID from previous policy (simulate policy match)
		policyDTO2.setFileProcessDateTime(JAN_2_2am);
		policyDTO2.setPolicyVersionId(pvId1);
		policyDTO2.setBatchId(batchId);
		policyDTO2.setFileInfo(TestDataSBMUtility.makeFileInformationType(tenantId, issuerId, TestDataSBMUtility.FILES_ONE_PER_ISSUER));
		
		// Make RCN different, since every cycle this policy may appear in different order in file.
		PolicyType policy2 = TestDataSBMUtility.makePolicyType(rcn + 8, qhpId, exchangePolicyId);
		
		policy2.getMemberInformation().add(TestDataSBMUtility.makePolicyMemberType(exchangePolicyId, (memIdPre + 1), TestDataSBMUtility.MEM_NAMES[0], true));
	    policy2.getMemberInformation().add(TestDataSBMUtility.makePolicyMemberType(exchangePolicyId, (memIdPre + 2), TestDataSBMUtility.MEM_NAMES[1], false));
		
		policyDTO2.setPolicy(policy2);
		
		String policyXml2 = TestDataSBMUtility.getPolicyAsXmlString(policy2);
		policyDTO2.setPolicyXml(policyXml2);
		
		// Set the same value of premiums. (.5 == .50, 0 == 0.00, etc) to test compareTo in overridden equals method.
		sbmPremium.setAptc(new BigDecimal(".50"));
		sbmPremium.setCsr(new BigDecimal("0.00"));
		policyDTO2.getSbmPremiums().put(sbmPremium.getEffectiveStartDate(), sbmPremium);


		// Attempt to save to Staging
		sbmXprCompositeDao.saveXprTransaction(policyDTO2);

		// Determine if it made it to Staging, list size should be 0.
		selectStagingPolicyVersionLatest(0, exchangePolicyId, stateCd);
		
		// Verify no SBMTransMsg was saved for the second policy.
		
		String sql = "SELECT * FROM SBMTRANSMSG WHERE EXCHANGEASSIGNEDPOLICYID = '" + exchangePolicyId + "'";

		List<SbmTransMsgPO> actualList = jdbc.query(sql, new SbmTransMsgRowMapper());
		
		assertEquals("SbmTransMsg list size", 1, actualList.size());

	}
	
	/**
	 * Test NOT saving to staging when the next cycle policy is the same after an EPS
	 * business validation change that results in the same (value to value) as EPS.
	 * An SbmtranMsg is saved along with the warning resulting from an system data modification.
	 * @throws InterruptedException
	 */
	@Test
	public void test_savePolicyToStaging_EPS_Change() throws InterruptedException {

		SbmTransMsgStatus expectedStatus1 = SbmTransMsgStatus.ACCEPTED_WITH_SBM_CHANGE;
		SbmTransMsgStatus expectedStatus2 = SbmTransMsgStatus.ACCEPTED_WITH_EPS_CHANGE;
		SBMErrorWarningCode expectedWarning = SBMErrorWarningCode.WR_004;
		int rcn = 6;
		Long batchId = 6000000 + TestDataSBMUtility.getRandomNumberAsLong(4);
		userVO.setUserId(batchId.toString());
		String stateCd = TestDataSBMUtility.SBM_STATES[TestDataSBMUtility.getRandomNumber(1)];
		String issuerId = "66666";
		String tenantId = stateCd + "0";
		String qhpId = TestDataSBMUtility.makeQhpId(issuerId, tenantId);
		String exchangePolicyId = TestDataSBMUtility.getRandomNumberAsString(9);
		String fileId = TestDataSBMUtility.getRandomNumberAsString(6);
		
		Long memIdPre = TestDataSBMUtility.getRandomNumberAsLong(3) * 1000;

		// Make an EPS PolicyVersion (simulate existing policy from last month's job run)
		SBMPolicyDTO policyDTO = new SBMPolicyDTO();
		policyDTO.setFileProcessDateTime(JAN_1_1am);
		policyDTO.setBatchId(batchId);
		policyDTO.setFileInfo(TestDataSBMUtility.makeFileInformationType(tenantId, issuerId, TestDataSBMUtility.FILES_ONE_PER_ISSUER));
		
		PolicyType policy = TestDataSBMUtility.makePolicyType(rcn, qhpId, exchangePolicyId);
	   
		PolicyMemberType member1 = TestDataSBMUtility.makePolicyMemberType(exchangePolicyId, (memIdPre + 1), TestDataSBMUtility.MEM_NAMES[0], true);
		PolicyMemberType member2 = TestDataSBMUtility.makePolicyMemberType(exchangePolicyId, (memIdPre + 2), TestDataSBMUtility.MEM_NAMES[1], false);
	    policy.getMemberInformation().add(member1);
	    policy.getMemberInformation().add(member2);
		
		policyDTO.setPolicy(policy);
		
		String policyXml = TestDataSBMUtility.getPolicyAsXmlString(policy);
		policyDTO.setPolicyXml(policyXml);
		
		SBMPremium sbmPremium = TestDataSBMUtility.makeSBMPremium(exchangePolicyId);
		sbmPremium.setAptc(new BigDecimal(".5"));
		sbmPremium.setCsr(new BigDecimal("0"));
		policyDTO.getSbmPremiums().put(sbmPremium.getEffectiveStartDate(), sbmPremium);

		SBMFileProcessingDTO fileDTO = insertParentFileRecords(tenantId, fileId);
		Long sbmTransMsgId = insertSbmTransMsg(fileDTO.getSbmFileInfo().getSbmFileInfoId(), stateCd, exchangePolicyId, expectedStatus1);
		policyDTO.setSbmTransMsgId(sbmTransMsgId);

		Long pvId1 = insertPolicyVersion(JAN_1_1am, qhpId, policyDTO);
		insertPolicyPremium(pvId1, sbmPremium);
		insertPolicyStatus(pvId1, JAN_1_1am, PolicyStatus.EFFECTUATED_2);
		Long pmvId1 = insertPolicyMemberVersion(stateCd, JAN_1_1am, exchangePolicyId, member1);
		Long pmvId2 = insertPolicyMemberVersion(stateCd, JAN_1_1am, exchangePolicyId, member2);
		
		insertPolicyMemberAddress(pmvId1, stateCd, member1.getPostalCode());
		insertPolicyMemberAddress(pmvId2, stateCd, member2.getPostalCode());
		
		insertMemberPolicyRaceEthnicity(pmvId1, member1.getRaceEthnicityCode());
		insertMemberPolicyRaceEthnicity(pmvId2, member2.getRaceEthnicityCode());
		
		insertPolicyMemberLanguageAbility(pmvId1, member1.getLanguageCode(), member1.getLanguageQualifierCode());
		insertPolicyMemberLanguageAbility(pmvId2, member2.getLanguageCode(), member2.getLanguageQualifierCode());
		
		MemberDates mDts1 = member1.getMemberDates().get(0);
		MemberDates mDts2 = member2.getMemberDates().get(0);
		
		insertPolicyMemberDate(pmvId1, DateTimeUtil.getLocalDateFromXmlGC(mDts1.getMemberStartDate()), DateTimeUtil.getLocalDateFromXmlGC(mDts1.getMemberEndDate()));
		insertPolicyMemberDate(pmvId2, DateTimeUtil.getLocalDateFromXmlGC(mDts2.getMemberStartDate()), DateTimeUtil.getLocalDateFromXmlGC(mDts2.getMemberEndDate()));
		
		insertPolicyMember(stateCd, pvId1, pmvId1);
		insertPolicyMember(stateCd, pvId1, pmvId2);
		
		Thread.sleep(1005L);

		// Make another policy with all the same data.  Will need to make parentFileInfo since this 
		// transaction should will be saved to sbmTransmsg.
		
		SBMFileProcessingDTO fileDTO2 = insertParentFileRecords(tenantId, fileId);
		
		SBMPolicyDTO policyDTO2 = new SBMPolicyDTO();
		// Set PVID from previous policy (simulate policy match)
		policyDTO2.setFileProcessDateTime(JAN_2_2am);
		policyDTO2.setPolicyVersionId(pvId1);
		policyDTO2.setBatchId(batchId);
		policyDTO2.setFileInfo(TestDataSBMUtility.makeFileInformationType(tenantId, issuerId, TestDataSBMUtility.FILES_ONE_PER_ISSUER));
		
		// Make RCN different, since every cycle this policy may appear in different order in file.
		PolicyType policy2 = TestDataSBMUtility.makePolicyType(rcn + 3, qhpId, exchangePolicyId);
		
		policy2.getMemberInformation().add(TestDataSBMUtility.makePolicyMemberType(exchangePolicyId, (memIdPre + 1), TestDataSBMUtility.MEM_NAMES[0], true));
	    policy2.getMemberInformation().add(TestDataSBMUtility.makePolicyMemberType(exchangePolicyId, (memIdPre + 2), TestDataSBMUtility.MEM_NAMES[1], false));
		
		policyDTO2.setPolicy(policy2);
		
		String policyXml2 = TestDataSBMUtility.getPolicyAsXmlString(policy2);
		policyDTO2.setPolicyXml(policyXml2);
		
		policyDTO2.getSbmPremiums().put(sbmPremium.getEffectiveStartDate(), sbmPremium);
		
		// Add a warning to simulate that EPS changed some data. WR-004 thru WR-009
		SbmErrWarningLogDTO errWarnDTO = new SbmErrWarningLogDTO();
		errWarnDTO.setErrorWarningTypeCd(expectedWarning.getCode());

		policyDTO2.getErrorList().add(errWarnDTO);
		
		// SbmTransMsg needs a StagingSbmPolicyid.
		Long stagingSbmPolicyId = insertStagingSbmPolicy(fileDTO2.getSbmFileProcSumId(), fileDTO2.getSbmFileInfo().getSbmFileInfoId(), policyXml2);

		policyDTO2.setSbmFileInfoId(fileDTO2.getSbmFileProcSumId());
		policyDTO2.setSbmFileInfoId(fileDTO2.getSbmFileInfo().getSbmFileInfoId());
		policyDTO2.setStagingSbmPolicyid(stagingSbmPolicyId);
		
		// Attempt to save to Staging
		sbmXprCompositeDao.saveXprTransaction(policyDTO2);

		// Determine if it made it to Staging, list size should be 0.
		// Since policy is same as EPS latest, policy2 should NOT be staging.
		selectStagingPolicyVersionLatest(0, exchangePolicyId, stateCd);
		
		String sql = "SELECT * FROM SBMTRANSMSG WHERE EXCHANGEASSIGNEDPOLICYID = '" + exchangePolicyId + "' ORDER BY SBMTRANSMSGID ASC";
		
		List<SbmTransMsgPO> actualList = jdbc.query(sql, new SbmTransMsgRowMapper());
		
		// Since policyDTO2 has warning due to EPS data change, an SBMTransMsg will be created.
		assertEquals("SbmTransMsg list size", 2, actualList.size());

		SbmTransMsgPO actual2 = actualList.get(1);
		
		assertEquals("SbmTransMsgProcStatusTypeCd",expectedStatus2.getCode(), actual2.getSbmTransMsgProcStatusTypeCd());
		
		// Confirm the warning was saved.
		sql = "SELECT * FROM SBMTRANSMSGVALIDATION WHERE SBMTRANSMSGID = " + actual2.getSbmTransMsgId();
		List<SbmTransMsgValidationPO> actualWarningList = jdbc.query(sql, new SbmTransMsgValidationRowMapper());
		assertEquals("SbmTransMsgValidation (errorWarning) list size", 1, actualWarningList.size());
		assertEquals("SbmErrorWarningTypeCd", expectedWarning.getCode(), actualWarningList.get(0).getSbmErrorWarningTypeCd());
		
	}
	
	@Test
	public void test_savePolicyToStaging_Errors() throws InterruptedException {

		SbmTransMsgStatus expectedStatus = SbmTransMsgStatus.REJECTED;
		SBMErrorWarningCode expectedError = SBMErrorWarningCode.ER_014;
		int rcn = 6;
		Long batchId = 6000000 + TestDataSBMUtility.getRandomNumberAsLong(4);
		userVO.setUserId(batchId.toString());
		String stateCd = TestDataSBMUtility.SBM_STATES[TestDataSBMUtility.getRandomNumber(1)];
		String issuerId = "66666";
		String tenantId = stateCd + "0";
		String qhpId = TestDataSBMUtility.makeQhpId(issuerId, tenantId);
		String exchangePolicyId = TestDataSBMUtility.getRandomNumberAsString(9);
		String fileId = TestDataSBMUtility.getRandomNumberAsString(6);
		
		SBMFileProcessingDTO fileDTO = insertParentFileRecords(tenantId, fileId);
		
		SBMPolicyDTO policyDTO = new SBMPolicyDTO();
		PolicyType policy = TestDataSBMUtility.makePolicyType(rcn, qhpId, exchangePolicyId);
		policyDTO.setPolicy(policy);
		String policyXml = TestDataSBMUtility.getPolicyAsXmlString(policy);
		policyDTO.setPolicyXml(policyXml);
		
		// Add a error to reject policy
		SbmErrWarningLogDTO errWarnDTO = new SbmErrWarningLogDTO();
		errWarnDTO.setErrorWarningTypeCd(expectedError.getCode());

		policyDTO.getErrorList().add(errWarnDTO);
		
		// SbmTransMsg needs a StagingSbmPolicyid.
		// Set any XML since not relevant for this test.
		Long stagingSbmPolicyId = insertStagingSbmPolicy(fileDTO.getSbmFileProcSumId(), fileDTO.getSbmFileInfo().getSbmFileInfoId(), "<Policy>" + exchangePolicyId + "</Policy>");

		policyDTO.setSbmFileInfoId(fileDTO.getSbmFileProcSumId());
		policyDTO.setSbmFileInfoId(fileDTO.getSbmFileInfo().getSbmFileInfoId());
		policyDTO.setStagingSbmPolicyid(stagingSbmPolicyId);
		policyDTO.setErrorFlag(true);
		
		// Attempt to save to Staging
		sbmXprCompositeDao.saveXprTransaction(policyDTO);

		// Determine if it made it to Staging, list size should be 0.
		// Since policy is same as EPS latest, policy2 should NOT be staging.
		selectStagingPolicyVersionLatest(0, exchangePolicyId, stateCd);
		
		String sql = "SELECT * FROM SBMTRANSMSG WHERE EXCHANGEASSIGNEDPOLICYID = '" + exchangePolicyId + "' ORDER BY SBMTRANSMSGID ASC";
		
		List<SbmTransMsgPO> actualList = jdbc.query(sql, new SbmTransMsgRowMapper());
		
		// Since policyDTO2 has warning due to EPS data change, an SBMTransMsg will be created.
		assertEquals("SbmTransMsg list size", 1, actualList.size());

		SbmTransMsgPO actual = actualList.get(0);
		
		assertEquals("SbmTransMsgProcStatusTypeCd",expectedStatus.getCode(), actual.getSbmTransMsgProcStatusTypeCd());
		
		// Confirm the warning was saved.
		sql = "SELECT * FROM SBMTRANSMSGVALIDATION WHERE SBMTRANSMSGID = " + actual.getSbmTransMsgId();
		List<SbmTransMsgValidationPO> actualWarningList = jdbc.query(sql, new SbmTransMsgValidationRowMapper());
		assertEquals("SbmTransMsgValidation (errorWarning) list size", 1, actualWarningList.size());
		assertEquals("SbmErrorWarningTypeCd", expectedError.getCode(), actualWarningList.get(0).getSbmErrorWarningTypeCd());
		
	}
	
	
	
	/**
	 * Only one member changes in Cycle 2, therefore only 1 of the 2 members should be
	 * inserted into StagingPolicyMemberVersion.  But, 2 member join records should be
	 * in StagingPolicyMember where one member is using the existing EPS PolicyMemberVersionId.
	 * @throws InterruptedException 
	 */
	@Test
	public void test_savePolicyToStaging_Cycle_2() throws InterruptedException {
		
		SbmTransMsgStatus expectedStatus = SbmTransMsgStatus.ACCEPTED_WITH_SBM_CHANGE;
		int rcn = 6;
		Long batchId = 6000000 + TestDataSBMUtility.getRandomNumberAsLong(4);
		userVO.setUserId(batchId.toString());
		String stateCd = TestDataSBMUtility.SBM_STATES[TestDataSBMUtility.getRandomNumber(1)];
		String issuerId = "44444";
		String tenantId = stateCd + "0";
		String qhpId = TestDataSBMUtility.makeQhpId(issuerId, tenantId);
		String exchangePolicyId = TestDataSBMUtility.getRandomNumberAsString(9);
		String fileId = TestDataSBMUtility.getRandomNumberAsString(6);
		
		Long memIdPre = TestDataSBMUtility.getRandomNumberAsLong(3) * 1000;

		// Make an EPS PolicyVersion (simulate existing policy from last month's job run)
		SBMPolicyDTO policyDTO = new SBMPolicyDTO();
		policyDTO.setFileProcessDateTime(JAN_1_1am);
		policyDTO.setBatchId(batchId);
		policyDTO.setFileInfo(TestDataSBMUtility.makeFileInformationType(tenantId, issuerId, TestDataSBMUtility.FILES_ONE_PER_ISSUER));
		
		PolicyType policy = TestDataSBMUtility.makePolicyType(rcn, qhpId, exchangePolicyId);
	   
		PolicyMemberType member1 = TestDataSBMUtility.makePolicyMemberType(exchangePolicyId, (memIdPre + 1), TestDataSBMUtility.MEM_NAMES[0], true);
		PolicyMemberType member2 = TestDataSBMUtility.makePolicyMemberType(exchangePolicyId, (memIdPre + 2), TestDataSBMUtility.MEM_NAMES[1], false);
	    policy.getMemberInformation().add(member1);
	    policy.getMemberInformation().add(member2);
		
		policyDTO.setPolicy(policy);
		
		String policyXml = TestDataSBMUtility.getPolicyAsXmlString(policy);
		policyDTO.setPolicyXml(policyXml);
		
		SBMPremium sbmPremium = TestDataSBMUtility.makeSBMPremium(exchangePolicyId);
		sbmPremium.setAptc(new BigDecimal(".5"));
		sbmPremium.setCsr(new BigDecimal("0"));
		policyDTO.getSbmPremiums().put(sbmPremium.getEffectiveStartDate(), sbmPremium);

		SBMFileProcessingDTO fileDTO = insertParentFileRecords(tenantId, fileId);
		Long sbmTransMsgId = insertSbmTransMsg(fileDTO.getSbmFileInfo().getSbmFileInfoId(), stateCd, exchangePolicyId, expectedStatus);
		policyDTO.setSbmTransMsgId(sbmTransMsgId);

		Long pvId1 = insertPolicyVersion(JAN_1_1am, qhpId, policyDTO);
		insertPolicyPremium(pvId1, sbmPremium);
		insertPolicyStatus(pvId1, JAN_1_1am, PolicyStatus.EFFECTUATED_2);
		Long pmvId1 = insertPolicyMemberVersion(stateCd, JAN_1_1am, exchangePolicyId, member1);
		Long pmvId2 = insertPolicyMemberVersion(stateCd, JAN_1_1am, exchangePolicyId, member2);
		
		insertPolicyMemberAddress(pmvId1, stateCd, member1.getPostalCode());
		insertPolicyMemberAddress(pmvId2, stateCd, member2.getPostalCode());
		
		insertMemberPolicyRaceEthnicity(pmvId1, member1.getRaceEthnicityCode());
		insertMemberPolicyRaceEthnicity(pmvId2, member2.getRaceEthnicityCode());
		
		insertPolicyMemberLanguageAbility(pmvId1, member1.getLanguageCode(), member1.getLanguageQualifierCode());
		insertPolicyMemberLanguageAbility(pmvId2, member2.getLanguageCode(), member2.getLanguageQualifierCode());
		
		MemberDates mDts1 = member1.getMemberDates().get(0);
		MemberDates mDts2 = member2.getMemberDates().get(0);
		
		insertPolicyMemberDate(pmvId1, DateTimeUtil.getLocalDateFromXmlGC(mDts1.getMemberStartDate()), DateTimeUtil.getLocalDateFromXmlGC(mDts1.getMemberEndDate()));
		insertPolicyMemberDate(pmvId2, DateTimeUtil.getLocalDateFromXmlGC(mDts2.getMemberStartDate()), DateTimeUtil.getLocalDateFromXmlGC(mDts2.getMemberEndDate()));
		
		insertPolicyMember(stateCd, pvId1, pmvId1);
		insertPolicyMember(stateCd, pvId1, pmvId2);
		
		Thread.sleep(1005L);

		// Make another policy with all the same data except for one member changing (MOM).  
		
		SBMPolicyDTO policyDTO2 = new SBMPolicyDTO();
		// Set PVID from previous policy (simulate policy match)
		policyDTO2.setFileProcessDateTime(JAN_2_2am);
		policyDTO2.setPolicyVersionId(pvId1);
		policyDTO2.setBatchId(batchId);
		policyDTO2.setFileInfo(TestDataSBMUtility.makeFileInformationType(tenantId, issuerId, TestDataSBMUtility.FILES_ONE_PER_ISSUER));
		
		// Make RCN different, since every cycle this policy may appear in different order in file.
		PolicyType policy2 = TestDataSBMUtility.makePolicyType(rcn-1, qhpId, exchangePolicyId);
		
		policy2.getMemberInformation().add(TestDataSBMUtility.makePolicyMemberType(exchangePolicyId, (memIdPre + 1), TestDataSBMUtility.MEM_NAMES[0], true));
		
		PolicyMemberType mom = TestDataSBMUtility.makePolicyMemberType(exchangePolicyId, (memIdPre + 2), TestDataSBMUtility.MEM_NAMES[1], false);
		 // Change one piece of data for MOM to trigger a member change.
		mom.setMemberMiddleName("MOM-NEW-MID");
	    policy2.getMemberInformation().add(mom);
	    
		policyDTO2.setPolicy(policy2);
		
		String policyXml2 = TestDataSBMUtility.getPolicyAsXmlString(policy2);
		policyDTO2.setPolicyXml(policyXml2);
		
		// Set the same value of premiums. (.5 == .50, 0 == 0.00, etc) to test compareTo in overridden equals method.
		sbmPremium.setAptc(new BigDecimal(".50"));
		sbmPremium.setCsr(new BigDecimal("0.00"));
		policyDTO2.getSbmPremiums().put(sbmPremium.getEffectiveStartDate(), sbmPremium);

		// Make parent file info since this transaction should be saved to SbmTransMsg.
		SBMFileProcessingDTO fileDTO2 = insertParentFileRecords(tenantId, fileId + "2");
		policyDTO2.setSbmFileInfoId(fileDTO2.getSbmFileInfo().getSbmFileInfoId());
		
		Long stagingSbmPolicyid = insertStagingSbmPolicy(fileDTO2.getSbmFileProcSumId(), fileDTO2.getSbmFileInfo().getSbmFileInfoId(), policyXml2);
		policyDTO2.setStagingSbmPolicyid(stagingSbmPolicyid);

		// Attempt to save to Staging
		sbmXprCompositeDao.saveXprTransaction(policyDTO2);

		// Determine if it made it to Staging, list size should be 2 (1 for set up and 1 for second policy).
		List<Map<String, Object>> actualList = selectStagingPolicyVersionLatest(1, exchangePolicyId, stateCd);
		
		//verifySbmTransMsgStatus(stateCd, exchangePolicyId, expectedStatus);
		
		Map<String, Object> row = actualList.get(0);
		
		BigDecimal actualStagingPvId = (BigDecimal) row.get("POLICYVERSIONID");
		
		verifySbmPolicyMember(2, actualStagingPvId.longValue(), stateCd);
		
		//TODO Add more assertions.
		assertNotNull("Assert for asserting", policyDTO2);
	}
	
	private List<Map<String, Object>> selectStagingPolicyVersionLatest(int expectedListSize, String exchangePolicyId, String stateCd) {

		String sql = "SELECT POLICYVERSIONID, MAINTENANCESTARTDATETIME, MAINTENANCEENDDATETIME, SUBSCRIBERSTATECD, " + 
				"ISSUERPOLICYID, ISSUERHIOSID, ISSUERSUBSCRIBERID, EXCHANGEPOLICYID, EXCHANGEASSIGNEDSUBSCRIBERID, " +
				"TRANSCONTROLNUM, SOURCEEXCHANGEID, PLANID, X12INSRNCLINETYPECD, POLICYSTARTDATE, POLICYENDDATE, " +
				"SBMTRANSMSGID, CREATEDATETIME, LASTMODIFIEDDATETIME, CREATEBY, LASTMODIFIEDBY " +
				"FROM STAGINGPOLICYVERSION WHERE EXCHANGEPOLICYID = '" + exchangePolicyId + "' AND " +
				"SUBSCRIBERSTATECD = '" + stateCd +"' AND MAINTENANCEENDDATETIME > TO_TIMESTAMP('9999-12-31', 'YYYY-MM-DD')";

		List<Map<String, Object>> actualList = jdbc.queryForList(sql);
		assertEquals("STAGINGPOLICYVERSION list size for stateCd=" + stateCd + ", exchangePolicyId=" + exchangePolicyId, expectedListSize, actualList.size());
		return actualList;
	}
	
	private List<Map<String, Object>> verifySbmPolicyMember(int expectedListSize, Long pvId, String stateCd) {

		String sql = "SELECT * FROM STAGINGPOLICYMEMBER WHERE POLICYVERSIONID = " + pvId + " AND SUBSCRIBERSTATECD = '" + stateCd +"'";

		List<Map<String, Object>> actualList = jdbc.queryForList(sql);
		assertEquals("STAGINGPOLICYMEMBER list size for stateCd=" + stateCd , expectedListSize, actualList.size());
		return actualList;
	}
	



}
