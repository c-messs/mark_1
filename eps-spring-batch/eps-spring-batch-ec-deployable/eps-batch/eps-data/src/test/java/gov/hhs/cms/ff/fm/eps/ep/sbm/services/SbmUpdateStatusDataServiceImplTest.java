package gov.hhs.cms.ff.fm.eps.ep.sbm.services;

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

import gov.cms.dsh.sbmi.PolicyMemberType;
import gov.cms.dsh.sbmi.PolicyMemberType.MemberDates;
import gov.cms.dsh.sbmi.PolicyType;
import gov.hhs.cms.ff.fm.eps.ep.enums.PolicyStatus;
import gov.hhs.cms.ff.fm.eps.ep.enums.SbmTransMsgStatus;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileProcessingDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMPolicyDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMPremium;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;
import gov.hhs.cms.ff.fm.eps.ep.util.sbm.TestDataSBMUtility;
import gov.hhs.cms.ff.fm.eps.ep.vo.UserVO;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/sbmi-data-config.xml", "classpath:/test-context-data.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class SbmUpdateStatusDataServiceImplTest extends BaseSbmServicesTest {


	@Autowired
	private SbmUpdateStatusDataService sbmUpdateStatusDataService;

	@Autowired
	protected UserVO userVO;

	@Test
	public void test_executeApproval_Cycle_1() {

		Long batchId1 = 100000000 + TestDataSBMUtility.getRandomNumberAsLong(5);
		userVO.setUserId(batchId1.toString());
		String stateCd = TestDataSBMUtility.getRandomSbmState();
		String sbmFileId = TestDataSBMUtility.getRandomNumberAsString(3);
		String tenantId = stateCd + "0";
		String issuerId = "10000";
		String qhpId = TestDataSBMUtility.makeQhpId(issuerId, tenantId);

		SBMFileProcessingDTO fileDTO = insertParentFileRecords(tenantId, sbmFileId);
		Long sbmFileProcSumId = fileDTO.getSbmFileProcSumId();
		Long sbmFileInfoId = fileDTO.getSbmFileInfo().getSbmFileInfoId();

		String policyXml = "<policy>Test policy for sbmFileProcSumId: " + sbmFileProcSumId + "</policy>";

		insertStagingSbmPolicy(sbmFileProcSumId, sbmFileInfoId, policyXml);

		String exchangePolicyId = "EXPOLID-" + sbmFileId;
		SbmTransMsgStatus status = SbmTransMsgStatus.ACCEPTED_WITH_SBM_CHANGE;

		Long sbmTransMsgId = insertSbmTransMsg(sbmFileInfoId, stateCd, exchangePolicyId, status);
		int rcn = 1;

		SBMPolicyDTO policyDTO = new SBMPolicyDTO();
		policyDTO.setFileProcessDateTime(JAN_2_2am);
		policyDTO.setBatchId(batchId1);
		policyDTO.setFileInfo(TestDataSBMUtility.makeFileInformationType(tenantId, issuerId, TestDataSBMUtility.FILES_ONE_PER_ISSUER));
		policyDTO.setSbmTransMsgId(sbmTransMsgId);

		PolicyType policy = TestDataSBMUtility.makePolicyType(rcn, qhpId, exchangePolicyId);
		policyDTO.setPolicy(policy);

		SBMPremium premium = TestDataSBMUtility.makeSBMPremium(exchangePolicyId);

		// Insert a policy and stuff into Staging tables.
		boolean isStaging = true;
		Long stagPvId = insertPolicyVersion(JAN_2_2am, qhpId, policyDTO, isStaging);
		insertPolicyPremium(stagPvId, premium, isStaging);
		LocalDate esd = premium.getEffectiveEndDate().plusDays(1);
		LocalDate eed = esd.plusMonths(6);
		premium.setEffectiveStartDate(esd);
		premium.setEffectiveStartDate(eed);
		insertPolicyPremium(stagPvId, premium, isStaging);
		insertPolicyStatus(stagPvId, JAN_1_1am, PolicyStatus.EFFECTUATED_2, isStaging);
		insertPolicyStatus(stagPvId, JAN_1_1am.plusHours(2), PolicyStatus.CANCELLED_3, isStaging);
		insertPolicyStatus(stagPvId, JAN_2_2am, PolicyStatus.EFFECTUATED_2, isStaging);

		Long memIdPre = TestDataSBMUtility.getRandomNumberAsLong(3) * 1000;
		PolicyMemberType member1 = TestDataSBMUtility.makePolicyMemberType(exchangePolicyId, (memIdPre + 1), TestDataSBMUtility.MEM_NAMES[0], true);
		PolicyMemberType member2 = TestDataSBMUtility.makePolicyMemberType(exchangePolicyId, (memIdPre + 2), TestDataSBMUtility.MEM_NAMES[1], false);

		Long stagPmvId1 = insertPolicyMemberVersion(stateCd, JAN_1_1am, exchangePolicyId, member1, isStaging);
		Long stagPmvId2 = insertPolicyMemberVersion(stateCd, JAN_1_1am, exchangePolicyId, member2, isStaging);

		MemberDates mDts1 = member1.getMemberDates().get(0);
		MemberDates mDts2 = member2.getMemberDates().get(0);

		insertPolicyMemberDate(stagPmvId1, DateTimeUtil.getLocalDateFromXmlGC(mDts1.getMemberStartDate()), DateTimeUtil.getLocalDateFromXmlGC(mDts1.getMemberEndDate()), isStaging);
		insertPolicyMemberDate(stagPmvId2, DateTimeUtil.getLocalDateFromXmlGC(mDts2.getMemberStartDate()), DateTimeUtil.getLocalDateFromXmlGC(mDts2.getMemberEndDate()), isStaging);


		insertPolicyMember(stateCd, stagPvId, stagPmvId1, isStaging);
		insertPolicyMember(stateCd, stagPvId, stagPmvId2, isStaging);

		Long batchId2 = batchId1 + 1;

		sbmUpdateStatusDataService.executeApproval(batchId2, sbmFileProcSumId);


		List<Map<String, Object>> actualPolicyList = selectPolicyVersion(1, exchangePolicyId, stateCd);
		assertSysData("PolicyVersion", actualPolicyList.get(0), batchId2);

		List<Map<String, Object>> actualPremiumList = selectPolicyPremium(2, stagPvId);
		assertSysData("PolicyPremium", actualPremiumList.get(0), batchId2);
		assertSysData("PolicyPremium", actualPremiumList.get(1), batchId2);

		List<Map<String, Object>> actualStatusList = selectPolicyStatus(3, stagPvId);
		assertSysData("PolicyStatus", actualStatusList.get(0), batchId2);
		assertSysData("PolicyStatus", actualStatusList.get(1), batchId2);
		assertSysData("PolicyStatus", actualStatusList.get(2), batchId2);

		List<Map<String, Object>> actualPmvList = selectPolicyMemberVersion(2, stagPvId);
		assertSysData("PolicyMemberVersion", actualPmvList.get(0), batchId2);
		assertSysData("PolicyMemberVersion", actualPmvList.get(1), batchId2);

		//TODO confirm some values.

		// Lang, Race, and Addr, 1 record per member since SBM is 1 to 1
		selectLang(1, stagPmvId1);
		selectLang(1, stagPmvId2);

		selectRace(1, stagPmvId1);
		selectRace(1, stagPmvId2);

		selectAddr(1, stagPmvId1);
		selectAddr(1, stagPmvId2);


	}

	/**
	 * BatchId 1 - Cycle 1 Approval Job
	 * BatchId 2 - Cycle 2 SBMi Job
	 * BatchId 3 - Cycle 2 Approval Job
	 */
	@Test
	public void test_executeApproval_Cycle_2() {

		assertNotNull("SbmUpdateStatusDataService is NOT null.", sbmUpdateStatusDataService);

		Long batchId1 = 100000000 + TestDataSBMUtility.getRandomNumberAsLong(5);
		userVO.setUserId(batchId1.toString());
		String stateCd = TestDataSBMUtility.getRandomSbmState();
		String sbmFileId = TestDataSBMUtility.getRandomNumberAsString(3);
		String tenantId = stateCd + "0";
		String issuerId = "10000";
		String qhpId = TestDataSBMUtility.makeQhpId(issuerId, tenantId);

		SBMFileProcessingDTO fileDTO = insertParentFileRecords(tenantId, sbmFileId);
		Long sbmFileProcSumId = fileDTO.getSbmFileProcSumId();
		Long sbmFileInfoId = fileDTO.getSbmFileInfo().getSbmFileInfoId();

		String policyXml = "<policy>Test policy for sbmFileProcSumId: " + sbmFileProcSumId + "</policy>";

		insertStagingSbmPolicy(sbmFileProcSumId, sbmFileInfoId, policyXml);

		String exchangePolicyId = "EXPOLID-" + sbmFileId;
		SbmTransMsgStatus status = SbmTransMsgStatus.ACCEPTED_WITH_SBM_CHANGE;

		Long sbmTransMsgId = insertSbmTransMsg(sbmFileInfoId, stateCd, exchangePolicyId, status);
		int rcn = 1;

		SBMPolicyDTO policyDTO = new SBMPolicyDTO();
		policyDTO.setFileProcessDateTime(JAN_2_2am);
		policyDTO.setBatchId(batchId1);
		policyDTO.setFileInfo(TestDataSBMUtility.makeFileInformationType(tenantId, issuerId, TestDataSBMUtility.FILES_ONE_PER_ISSUER));
		policyDTO.setSbmTransMsgId(sbmTransMsgId);

		PolicyType policy = TestDataSBMUtility.makePolicyType(rcn, qhpId, exchangePolicyId);
		policyDTO.setPolicy(policy);

		SBMPremium premium = TestDataSBMUtility.makeSBMPremium(exchangePolicyId);

		// EPS tables

		// Insert a policy and stuff into EPS tables (non-Staging).
		boolean isStaging = false;
		Long epsPvId = insertPolicyVersion(JAN_1_1am, qhpId, policyDTO, isStaging);
		SBMPremium epsPremium = premium;
		insertPolicyPremium(epsPvId, epsPremium, isStaging);

		insertPolicyStatus(epsPvId, JAN_1_1am, PolicyStatus.EFFECTUATED_2, isStaging);
		insertPolicyStatus(epsPvId, JAN_1_1am.plusHours(2), PolicyStatus.CANCELLED_3, isStaging);

		Long memIdPre = TestDataSBMUtility.getRandomNumberAsLong(3) * 1000;
		PolicyMemberType member1 = TestDataSBMUtility.makePolicyMemberType(exchangePolicyId, (memIdPre + 1), TestDataSBMUtility.MEM_NAMES[0], true);
		PolicyMemberType member2 = TestDataSBMUtility.makePolicyMemberType(exchangePolicyId, (memIdPre + 2), TestDataSBMUtility.MEM_NAMES[1], false);

		Long epsPmvId1 = insertPolicyMemberVersion(stateCd, JAN_1_1am, exchangePolicyId, member1, isStaging);
		Long epsPmvId2 = insertPolicyMemberVersion(stateCd, JAN_1_1am, exchangePolicyId, member2, isStaging);

		// TODO Insert/Update data into StagingPolicyMemberVersion
		insertPolicyMemberAddress(epsPmvId1, stateCd, member1.getPostalCode());
		insertPolicyMemberAddress(epsPmvId2, stateCd, member2.getPostalCode());

		insertMemberPolicyRaceEthnicity(epsPmvId1, member1.getRaceEthnicityCode());
		insertMemberPolicyRaceEthnicity(epsPmvId2, member2.getRaceEthnicityCode());

		insertPolicyMemberLanguageAbility(epsPmvId1, member1.getLanguageCode(), member1.getLanguageQualifierCode());
		insertPolicyMemberLanguageAbility(epsPmvId2, member2.getLanguageCode(), member2.getLanguageQualifierCode());

		MemberDates mDts1 = member1.getMemberDates().get(0);
		MemberDates mDts2 = member2.getMemberDates().get(0);

		insertPolicyMemberDate(epsPmvId1, DateTimeUtil.getLocalDateFromXmlGC(mDts1.getMemberStartDate()), DateTimeUtil.getLocalDateFromXmlGC(mDts1.getMemberEndDate()), isStaging);
		insertPolicyMemberDate(epsPmvId2, DateTimeUtil.getLocalDateFromXmlGC(mDts2.getMemberStartDate()), DateTimeUtil.getLocalDateFromXmlGC(mDts2.getMemberEndDate()), isStaging);


		insertPolicyMember(stateCd, epsPvId, epsPmvId1, isStaging);
		insertPolicyMember(stateCd, epsPvId, epsPmvId2, isStaging);

		// STAGING tables

		// Insert a policy and stuff into STAGING tables.
		// Do not insert lang, race and zip.
		Long priorPvId = epsPvId;
		Long priorPmvId1 = epsPmvId1;
		Long priorPmvId2 = epsPmvId2;
		Long batchId2 = batchId1 + 1;
		userVO.setUserId(batchId1.toString());
		SBMFileProcessingDTO fileDTO2 = insertParentFileRecords(tenantId, sbmFileId);
		Long sbmFileProcSumId2 = fileDTO2.getSbmFileProcSumId();
		Long sbmFileInfoId2 = fileDTO2.getSbmFileInfo().getSbmFileInfoId();

		String policyXml2 = "<policy>Test Cycle 2 policy for sbmFileProcSumId2: " + sbmFileProcSumId2 + "</policy>";

		insertStagingSbmPolicy(sbmFileProcSumId, sbmFileInfoId, policyXml);

		Long sbmTransMsgId2 = insertSbmTransMsg(sbmFileInfoId2, stateCd, exchangePolicyId, status);

		SBMPolicyDTO policyDTO2 = new SBMPolicyDTO();
		policyDTO2.setFileProcessDateTime(JAN_2_2am);
		policyDTO2.setBatchId(batchId2);
		policyDTO2.setFileInfo(TestDataSBMUtility.makeFileInformationType(tenantId, issuerId, TestDataSBMUtility.FILES_ONE_PER_ISSUER));
		policyDTO2.setSbmTransMsgId(sbmTransMsgId2);
		// use the same policy.

		policyDTO2.setPolicy(policy);


		isStaging = true;
		Long stagPvId = insertPolicyVersion(JAN_2_2am, qhpId, policyDTO2, priorPvId, isStaging);
		SBMPremium stagPremium = premium;
		insertPolicyPremium(stagPvId, stagPremium, isStaging);
		insertPolicyStatus(stagPvId, JAN_1_1am, PolicyStatus.EFFECTUATED_2, isStaging);
		insertPolicyStatus(stagPvId, JAN_1_1am.plusHours(2), PolicyStatus.CANCELLED_3, isStaging);
		insertPolicyStatus(stagPvId, JAN_2_2am, PolicyStatus.EFFECTUATED_2, isStaging);

		PolicyMemberType stagMember1 = TestDataSBMUtility.makePolicyMemberType(exchangePolicyId, (memIdPre + 1), TestDataSBMUtility.MEM_NAMES[0], true);
		PolicyMemberType stagMember2 = TestDataSBMUtility.makePolicyMemberType(exchangePolicyId, (memIdPre + 2), TestDataSBMUtility.MEM_NAMES[1], false);

		Long stagPmvId1 = insertPolicyMemberVersion(stateCd, JAN_2_2am, exchangePolicyId, stagMember1, priorPmvId1, isStaging);
		Long stagPmvId2 = insertPolicyMemberVersion(stateCd, JAN_2_2am, exchangePolicyId, stagMember2, priorPmvId2, isStaging);

		MemberDates stagMDts1 = stagMember1.getMemberDates().get(0);
		MemberDates stagMDts2 = stagMember2.getMemberDates().get(0);

		insertPolicyMemberDate(stagPmvId1, DateTimeUtil.getLocalDateFromXmlGC(stagMDts1.getMemberStartDate()), DateTimeUtil.getLocalDateFromXmlGC(stagMDts1.getMemberEndDate()), isStaging);
		insertPolicyMemberDate(stagPmvId2, DateTimeUtil.getLocalDateFromXmlGC(stagMDts2.getMemberStartDate()), DateTimeUtil.getLocalDateFromXmlGC(stagMDts2.getMemberEndDate()), isStaging);

		insertPolicyMember(stateCd, stagPvId, stagPmvId1, isStaging);
		insertPolicyMember(stateCd, stagPvId, stagPmvId2, isStaging);


		// Cycle 2 Approval Job
		Long batchId3 = batchId2 + 1;

		sbmUpdateStatusDataService.executeApproval(batchId3, sbmFileProcSumId2);

		// stagPvId is now in EPS, so use staging Ids to verify data was merged to EPS tables.

		// 2 policies (Cycle 1 and 2) since matching on exchangePolicyId and stateCd
		List<Map<String, Object>> actualPolicyList = selectPolicyVersion(2, exchangePolicyId, stateCd);
		// Since ORDER BY POLICYVERSIONID ASC, the first policy will be from batchId 1 approval job.
		// that had MED and LastModified update to the Cycle 2 Approval Job.
		assertSysDataAfterUpdate("PolicyVersion", actualPolicyList.get(0), batchId1, batchId3);
		assertSysData("PolicyVersion", actualPolicyList.get(1), batchId3);
		
		
		// getting Cycle 2 Approval Job data by id.
		List<Map<String, Object>> actualPremiumList = selectPolicyPremium(1, stagPvId);
		assertSysData("PolicyPremium", actualPremiumList.get(0), batchId3);

		List<Map<String, Object>> actualStatusList = selectPolicyStatus(3, stagPvId);
		assertSysData("PolicyStatus", actualStatusList.get(0), batchId3);
		assertSysData("PolicyStatus", actualStatusList.get(1), batchId3);
		assertSysData("PolicyStatus", actualStatusList.get(2), batchId3);
		

		List<Map<String, Object>> actualPmvList = selectPolicyMemberVersion(2, stagPvId);

		//TODO confirm some values.

		// Lang, Race, and Addr, 1 record per member since SBM is 1 to 1
		selectLang(1, stagPmvId1);
		selectLang(1, stagPmvId2);

		selectRace(1, stagPmvId1);
		selectRace(1, stagPmvId2);

		selectAddr(1, stagPmvId1);
		selectAddr(1, stagPmvId2);

	}



	@Test
	public void test_executeApproval_Cycle_2_NoMemberChanges() {

		assertNotNull("SbmUpdateStatusDataService is NOT null.", sbmUpdateStatusDataService);

		Long batchId1 = 100000000 + TestDataSBMUtility.getRandomNumberAsLong(5);
		userVO.setUserId(batchId1.toString());
		String stateCd = TestDataSBMUtility.getRandomSbmState();
		String sbmFileId = TestDataSBMUtility.getRandomNumberAsString(3);
		String tenantId = stateCd + "0";
		String issuerId = "10000";
		String qhpId = TestDataSBMUtility.makeQhpId(issuerId, tenantId);

		SBMFileProcessingDTO fileDTO = insertParentFileRecords(tenantId, sbmFileId);
		Long sbmFileProcSumId = fileDTO.getSbmFileProcSumId();
		Long sbmFileInfoId = fileDTO.getSbmFileInfo().getSbmFileInfoId();

		String policyXml = "<policy>Test policy for sbmFileProcSumId: " + sbmFileProcSumId + "</policy>";

		insertStagingSbmPolicy(sbmFileProcSumId, sbmFileInfoId, policyXml);

		String exchangePolicyId = "EXPOLID-" + sbmFileId;
		SbmTransMsgStatus status = SbmTransMsgStatus.ACCEPTED_WITH_SBM_CHANGE;

		Long sbmTransMsgId = insertSbmTransMsg(sbmFileInfoId, stateCd, exchangePolicyId, status);
		int rcn = 1;

		SBMPolicyDTO policyDTO = new SBMPolicyDTO();
		policyDTO.setFileProcessDateTime(JAN_2_2am);
		policyDTO.setBatchId(batchId1);
		policyDTO.setFileInfo(TestDataSBMUtility.makeFileInformationType(tenantId, issuerId, TestDataSBMUtility.FILES_ONE_PER_ISSUER));
		policyDTO.setSbmTransMsgId(sbmTransMsgId);

		PolicyType policy = TestDataSBMUtility.makePolicyType(rcn, qhpId, exchangePolicyId);
		policyDTO.setPolicy(policy);

		SBMPremium premium = TestDataSBMUtility.makeSBMPremium(exchangePolicyId);

		// EPS tables

		// Insert a Cycle 1 policy and stuff into EPS tables (non-Staging).
		boolean isStaging = false;
		Long epsPvId = insertPolicyVersion(JAN_1_1am, qhpId, policyDTO, isStaging);
		SBMPremium epsPremium = premium;
		insertPolicyPremium(epsPvId, epsPremium, isStaging);

		insertPolicyStatus(epsPvId, JAN_1_1am, PolicyStatus.EFFECTUATED_2, isStaging);
		insertPolicyStatus(epsPvId, JAN_1_1am.plusHours(2), PolicyStatus.CANCELLED_3, isStaging);

		Long memIdPre = TestDataSBMUtility.getRandomNumberAsLong(3) * 1000;
		PolicyMemberType member1 = TestDataSBMUtility.makePolicyMemberType(exchangePolicyId, (memIdPre + 1), TestDataSBMUtility.MEM_NAMES[0], true);
		PolicyMemberType member2 = TestDataSBMUtility.makePolicyMemberType(exchangePolicyId, (memIdPre + 2), TestDataSBMUtility.MEM_NAMES[1], false);

		Long epsPmvId1 = insertPolicyMemberVersion(stateCd, JAN_1_1am, exchangePolicyId, member1, isStaging);
		Long epsPmvId2 = insertPolicyMemberVersion(stateCd, JAN_1_1am, exchangePolicyId, member2, isStaging);

		// TODO Insert/Update data into StagingPolicyMemberVersion
		insertPolicyMemberAddress(epsPmvId1, stateCd, member1.getPostalCode());
		insertPolicyMemberAddress(epsPmvId2, stateCd, member2.getPostalCode());

		insertMemberPolicyRaceEthnicity(epsPmvId1, member1.getRaceEthnicityCode());
		insertMemberPolicyRaceEthnicity(epsPmvId2, member2.getRaceEthnicityCode());

		insertPolicyMemberLanguageAbility(epsPmvId1, member1.getLanguageCode(), member1.getLanguageQualifierCode());
		insertPolicyMemberLanguageAbility(epsPmvId2, member2.getLanguageCode(), member2.getLanguageQualifierCode());

		MemberDates mDts1 = member1.getMemberDates().get(0);
		MemberDates mDts2 = member2.getMemberDates().get(0);

		insertPolicyMemberDate(epsPmvId1, DateTimeUtil.getLocalDateFromXmlGC(mDts1.getMemberStartDate()), DateTimeUtil.getLocalDateFromXmlGC(mDts1.getMemberEndDate()), isStaging);
		insertPolicyMemberDate(epsPmvId2, DateTimeUtil.getLocalDateFromXmlGC(mDts2.getMemberStartDate()), DateTimeUtil.getLocalDateFromXmlGC(mDts2.getMemberEndDate()), isStaging);


		insertPolicyMember(stateCd, epsPvId, epsPmvId1, isStaging);
		insertPolicyMember(stateCd, epsPvId, epsPmvId2, isStaging);

		// STAGING tables

		// Insert a policy and stuff into STAGING tables.
		// Only populate PolicyMember, since members did not change.  New PV2 will joined to DAD1 and MOM1
		Long priorPvId = epsPvId;
		Long priorPmvId1 = epsPmvId1;
		Long priorPmvId2 = epsPmvId2;
		Long batchId2 = batchId1 + 1;
		userVO.setUserId(batchId1.toString());
		SBMFileProcessingDTO fileDTO2 = insertParentFileRecords(tenantId, sbmFileId);
		Long sbmFileProcSumId2 = fileDTO2.getSbmFileProcSumId();
		Long sbmFileInfoId2 = fileDTO2.getSbmFileInfo().getSbmFileInfoId();

		String policyXml2 = "<policy>Test Cycle 2 policy for sbmFileProcSumId2: " + sbmFileProcSumId2 + "</policy>";

		insertStagingSbmPolicy(sbmFileProcSumId, sbmFileInfoId, policyXml);

		Long sbmTransMsgId2 = insertSbmTransMsg(sbmFileInfoId2, stateCd, exchangePolicyId, status);

		SBMPolicyDTO policyDTO2 = new SBMPolicyDTO();
		policyDTO2.setFileProcessDateTime(JAN_2_2am);
		policyDTO2.setBatchId(batchId2);
		policyDTO2.setFileInfo(TestDataSBMUtility.makeFileInformationType(tenantId, issuerId, TestDataSBMUtility.FILES_ONE_PER_ISSUER));
		policyDTO2.setSbmTransMsgId(sbmTransMsgId2);
		// use the same policy.

		policyDTO2.setPolicy(policy);


		isStaging = true;
		Long stagPvId = insertPolicyVersion(JAN_2_2am, qhpId, policyDTO2, priorPvId, isStaging);
		SBMPremium stagPremium = premium;
		insertPolicyPremium(stagPvId, stagPremium, isStaging);
		insertPolicyStatus(stagPvId, JAN_1_1am, PolicyStatus.EFFECTUATED_2, isStaging);
		insertPolicyStatus(stagPvId, JAN_1_1am.plusHours(2), PolicyStatus.CANCELLED_3, isStaging);
		insertPolicyStatus(stagPvId, JAN_2_2am, PolicyStatus.EFFECTUATED_2, isStaging);

		PolicyMemberType stagMember1 = TestDataSBMUtility.makePolicyMemberType(exchangePolicyId, (memIdPre + 1), TestDataSBMUtility.MEM_NAMES[0], true);
		PolicyMemberType stagMember2 = TestDataSBMUtility.makePolicyMemberType(exchangePolicyId, (memIdPre + 2), TestDataSBMUtility.MEM_NAMES[1], false);

		// Since member did not change, use the EPS pmvID to join new Cycle 2 policy to Cycle 1 member versions.
		insertPolicyMember(stateCd, stagPvId, epsPmvId1, isStaging);
		insertPolicyMember(stateCd, stagPvId, epsPmvId2, isStaging);


		sbmUpdateStatusDataService.executeApproval(batchId2, sbmFileProcSumId2);

		// stagPvId is now in EPS, so use staging Ids to verify data was merged to EPS tables.

		// 2 policies (Cycle 1 and 2) since matching on exchangePolicyId and stateCd
		List<Map<String, Object>> actualPolicyList = selectPolicyVersion(2, exchangePolicyId, stateCd);

		// getting Cycle 2 data by id.
		List<Map<String, Object>> actualPremiumList = selectPolicyPremium(1, stagPvId);

		List<Map<String, Object>> actualStatusList = selectPolicyStatus(3, stagPvId);

		List<Map<String, Object>> actualPmvList = selectPolicyMemberVersion(2, stagPvId);


		//TODO confirm some values.

	}

	@Test
	public void test_executeDisapproval_NoFiles() {

		Long batchId1 = 200000000 + TestDataSBMUtility.getRandomNumberAsLong(6);
		Long sbmFileProcSumId = TestDataSBMUtility.getRandomNumberAsLong(3);

		sbmUpdateStatusDataService.executeDisapproval(batchId1, sbmFileProcSumId);

		assertNotNull("Assert for asserting", sbmFileProcSumId);
	}


	@Test
	public void test_executeFileReversal() {

		Long batchId1 = 300000000 + TestDataSBMUtility.getRandomNumberAsLong(6);
		Long sbmFileProcSumId = TestDataSBMUtility.getRandomNumberAsLong(3);

		sbmUpdateStatusDataService.executeFileReversal(batchId1, sbmFileProcSumId);

		assertNotNull("Assert for asserting", sbmFileProcSumId);
	}

	private List<Map<String, Object>> selectPolicyVersion(int expectedListSize, String exchangePolicyId, String stateCd) {

		String sql = "SELECT * FROM POLICYVERSION WHERE EXCHANGEPOLICYID = '" + exchangePolicyId + "' AND " +
				"SUBSCRIBERSTATECD = '" + stateCd +"' ORDER BY POLICYVERSIONID ASC";

		List<Map<String, Object>> actualList = jdbc.queryForList(sql);
		assertEquals("POLICYVERSION list size for stateCd=" + stateCd + ", exchangePolicyId=" + exchangePolicyId, expectedListSize, actualList.size());
		return actualList;
	}

	private List<Map<String, Object>> selectPolicyPremium(int expectedListSize, Long policyVersionId) {

		String sql = "SELECT * FROM POLICYPREMIUM WHERE POLICYVERSIONID = " + policyVersionId + " ORDER BY EFFECTIVESTARTDATE ASC";

		List<Map<String, Object>> actualList = jdbc.queryForList(sql);
		assertEquals("POLICYPREMIUM list size for policyVersionId=" + policyVersionId, expectedListSize, actualList.size());
		return actualList;
	}

	private List<Map<String, Object>> selectPolicyStatus(int expectedListSize, Long policyVersionId) {

		String sql = "SELECT * FROM POLICYSTATUS WHERE POLICYVERSIONID = " + policyVersionId + " ORDER BY TRANSDATETIME DESC";

		List<Map<String, Object>> actualList = jdbc.queryForList(sql);
		assertEquals("POLICYSTATUS list size for policyVersionId=" + policyVersionId, expectedListSize, actualList.size());
		return actualList;
	}

	private List<Map<String, Object>> selectPolicyMemberVersion(int expectedListSize, Long policyVersionId) {

		String sql = "SELECT pm.POLICYMEMBERVERSIONID, pmv.SUBSCRIBERIND, pmv.ISSUERASSIGNEDMEMBERID, " +
				"pmv.EXCHANGEMEMBERID, pmv.MAINTENANCESTARTDATETIME, pmv.MAINTENANCEENDDATETIME, pmv.POLICYMEMBERDEATHDATE," +
				"pmv.POLICYMEMBERLASTNM, pmv.POLICYMEMBERFIRSTNM, pmv.POLICYMEMBERMIDDLENM, pmv.POLICYMEMBERSALUTATIONNM, " +
				"pmv.POLICYMEMBERSUFFIXNM, pmv.POLICYMEMBERSSN, pmv.EXCHANGEPOLICYID, pmv.SUBSCRIBERSTATECD, pmv.X12TOBACCOUSETYPECD, " +
				"pmv.POLICYMEMBERBIRTHDATE, pmv.X12GENDERTYPECD, pmv.INCORRECTGENDERTYPECD, pmv.SUBSCRIBERIND, " +
				"pmv.CREATEDATETIME, pmv.LASTMODIFIEDDATETIME, pmv.CREATEBY, pmv.LASTMODIFIEDBY FROM POLICYMEMBER pm " +
				"JOIN POLICYMEMBERVERSION pmv ON pm.POLICYMEMBERVERSIONID = pmv.POLICYMEMBERVERSIONID " +
				"WHERE pm.POLICYVERSIONID = " + policyVersionId;

		List<Map<String, Object>> actualList = jdbc.queryForList(sql);
		assertEquals("POLICYMEMBERVERSION list size for policyVersionId=" + policyVersionId, expectedListSize, actualList.size());
		return actualList;
	}

	private List<Map<String, Object>> selectLang(int expectedListSize, Long policyMemberVersionId) {

		String sql = "SELECT * FROM POLICYMEMBERLANGUAGEABILITY WHERE POLICYMEMBERVERSIONID = " + policyMemberVersionId;

		List<Map<String, Object>> actualList = jdbc.queryForList(sql);
		assertEquals("POLICYMEMBERLANGUAGEABILITY list size for policyMemberVersionId = " + policyMemberVersionId, expectedListSize, actualList.size());
		return actualList;
	}

	private List<Map<String, Object>> selectRace(int expectedListSize, Long policyMemberVersionId) {

		String sql = "SELECT * FROM MEMBERPOLICYRACEETHNICITY WHERE POLICYMEMBERVERSIONID = " + policyMemberVersionId;

		List<Map<String, Object>> actualList = jdbc.queryForList(sql);
		assertEquals("MEMBERPOLICYRACEETHNICITY list size for policyMemberVersionId = " + policyMemberVersionId, expectedListSize, actualList.size());
		return actualList;
	}

	private List<Map<String, Object>> selectAddr(int expectedListSize, Long policyMemberVersionId) {

		String sql = "SELECT * FROM POLICYMEMBERADDRESS WHERE POLICYMEMBERVERSIONID = " + policyMemberVersionId;

		List<Map<String, Object>> actualList = jdbc.queryForList(sql);
		assertEquals("POLICYMEMBERADDRESS list size for policyMemberVersionId = " + policyMemberVersionId, expectedListSize, actualList.size());
		return actualList;
	}




}
