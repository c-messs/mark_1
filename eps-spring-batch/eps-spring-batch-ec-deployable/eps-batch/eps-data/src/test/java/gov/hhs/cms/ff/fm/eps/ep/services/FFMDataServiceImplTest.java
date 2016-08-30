package gov.hhs.cms.ff.fm.eps.ep.services;


import gov.cms.dsh.bem.BooleanIndicatorSimpleType;
import gov.cms.dsh.bem.MemberType;
import gov.cms.dsh.bem.PolicyInfoType;
import gov.hhs.cms.ff.fm.eps.ep.BEMDataUtil;
import gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentMaintenanceDTO;
import gov.hhs.cms.ff.fm.eps.ep.dao.mappers.PolicyMemberAddressRowMapper;
import gov.hhs.cms.ff.fm.eps.ep.dao.mappers.PolicyMemberVersionRowMapper;
import gov.hhs.cms.ff.fm.eps.ep.dao.mappers.PolicyStatusRowMapper;
import gov.hhs.cms.ff.fm.eps.ep.dao.mappers.PolicyVersionRowMapper;
import gov.hhs.cms.ff.fm.eps.ep.data.util.TestDataUtil;
import gov.hhs.cms.ff.fm.eps.ep.data.util.TestEPSPre25Data;
import gov.hhs.cms.ff.fm.eps.ep.enums.EProdEnum;
import gov.hhs.cms.ff.fm.eps.ep.enums.PolicyStatus;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyMemberAddressPO;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyMemberDatePO;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyMemberPO;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyMemberVersionPO;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyStatusPO;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyVersionPO;
import gov.hhs.cms.ff.fm.eps.ep.services.impl.FFMDataServiceImpl;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.accenture.foundation.common.exception.ApplicationException;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/eps-data-config.xml", "classpath:/test-context-data.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class FFMDataServiceImplTest extends BaseServicesTest {

	@Autowired
	public JdbcTemplate jdbc;

	@Autowired
	private FFMDataServiceImpl policyDataService;

	@Value("${SELECT_POLICY_VERSION_BY_ID_AND_STATE}")
	private String pvSQL;

	@Value("${SELECT_POLICY_STATUS_LIST}")
	private String pvStatusSQL;

	@Value("${SELECT_PMV_BY_PVID_AND_STATE}")
	private String pmvSQL;

	@Value("${SELECT_PMV_ADDRESS}")
	private String pmvAddrSQL;




	/**
	 * Tests new member versions are created when all members change.
	 * - Insert an INITIAL policy
	 * - Insert an EFFECTUATED policy (all members will have NO changes)
	 * - Confirm the new EFFECTUATED policy is joined to each of the existing policy member versions
	 * - And, all existing members from policy version 1 are now joined to policy version 2.
	 */
	@Test
	public void test_PolicyToMemberVersions_EFFECTUATED() throws InterruptedException {

		String[] memberNames = {"DAD", "MOM", "SON", "DAU"};
		// make them the same. So test data matches.
		Long bemId = Long.valueOf("555550000") + TestDataUtil.getRandomNumber(4);
		Long policyVersionId_V1 = null;
		Long policyVersionId_V2 = null;
		String subscriberStateCd = null;

		String mgpi = bemId.toString();
		LocalDate psd = JAN_1;
		LocalDate ped = DEC_31;
		PolicyStatus policyStatus = PolicyStatus.INITIAL_1;
		String exchangePolicyId = TestDataUtil.getRandomNumber(9).toString();
		PolicyInfoType policyInfo = TestDataUtil.makePolicyInfoType(mgpi, psd, ped, policyStatus, exchangePolicyId);

		// Insert INITIAL policy
		BenefitEnrollmentMaintenanceDTO bemDTOInitial = makeBemDTO(bemId, memberNames, null, policyInfo);
		// Verify one member with null address.
		bemDTOInitial.getBem().getMember().get(3).getMemberNameInformation().setMemberResidenceAddress(null);
		assertNotNull("bemDTOInitial", bemDTOInitial);
		policyDataService.saveBEM(bemDTOInitial);

		Thread.sleep(SLEEP_INTERVAL_SEC);

		BenefitEnrollmentMaintenanceDTO bemDTOLatest = policyDataService.getLatestBEMByPolicyId(bemDTOInitial);
		assertNotNull("bemDTOLatest BEM", bemDTOLatest.getBem());

		assertNotNull("policyVersionId returned from getLatestBem", bemDTOLatest.getPolicyVersionId());
		policyVersionId_V1 =  bemDTOLatest.getPolicyVersionId();
		subscriberStateCd = bemDTOLatest.getSubscriberStateCd();

		// Insert an EFFECTUATED policy
		policyInfo.setPolicyStatus(PolicyStatus.EFFECTUATED_2.getValue());
		BenefitEnrollmentMaintenanceDTO bemDTO_EFF = makeBemDTO(bemId, memberNames, policyVersionId_V1, policyInfo);
		bemDTO_EFF.getBem().getMember().get(3).getMemberNameInformation().setMemberResidenceAddress(null);
		assertNotNull("bemDTO_EFF", bemDTO_EFF);
		bemDTO_EFF.setSubscriberStateCd(subscriberStateCd);
		policyDataService.saveBEM(bemDTO_EFF);

		BenefitEnrollmentMaintenanceDTO bemDTOLatest2 = policyDataService.getLatestBEMByPolicyId(bemDTO_EFF);
		assertNotNull("policyVersionId returned from getLatestBem the second time", bemDTOLatest2.getPolicyVersionId());
		policyVersionId_V2 =  bemDTOLatest2.getPolicyVersionId();

		List<PolicyMemberVersionPO> pmvPoList_V1 = jdbc.query(pmvSQL, new PolicyMemberVersionRowMapper(), policyVersionId_V1, bemDTOLatest.getSubscriberStateCd());
		List<PolicyMemberVersionPO> pmvPoList_V2 = jdbc.query(pmvSQL, new PolicyMemberVersionRowMapper(), policyVersionId_V2, bemDTOLatest2.getSubscriberStateCd());

		assertEquals("PolicyVersion 1 member list size", memberNames.length, pmvPoList_V1.size());
		assertEquals("PolicyVersion 2 member list size", memberNames.length, pmvPoList_V2.size());

		// Confirm ALL member versions from policy 1 are joined to policy 2 by retrieving PolicyMemberPOs (join table)
		long id1;
		long id2;
		int i = 0;

		for (PolicyMemberVersionPO po1: pmvPoList_V1) {
			id1 = po1.getPolicyMemberVersionId().longValue();
			boolean isFound = false;
			for (PolicyMemberVersionPO po2: pmvPoList_V2) {
				id2 = po2.getPolicyMemberVersionId().longValue();
				if (id1 == id2) {
					isFound = true;
					break;
				}
			}
			assertTrue(i + ": PolicyVersion 2 contains PolicyMemberVersionId " + id1 + " from PolicyVersion 1. ", isFound);
			i++;
		}

		// Confirm the members from PV2 have MAINTENANCEENDDATETIME set as the SAME as PV1.
		// Since NO new member version were created, PV2 will be joined to the old, or PV1 members.
		List<PolicyVersionPO> epsList = jdbc.query(pvSQL, new PolicyVersionRowMapper(), policyVersionId_V1, bemDTOLatest.getSubscriberStateCd());
		PolicyVersionPO epsPO_1 = epsList.get(0);
		LocalDateTime pv1_MSD = epsPO_1.getMaintenanceStartDateTime();

		List<PolicyMemberVersionPO> epsMemberList = jdbc.query(pmvSQL, new PolicyMemberVersionRowMapper(), policyVersionId_V2, subscriberStateCd);

		assertEquals("PolicyMemberVersions list size", memberNames.length, epsMemberList.size());

		for (PolicyMemberVersionPO epsMember : epsMemberList) {

			assertEquals(epsMember.getExchangeMemberID() + " MaintenanceEndDateTime compared to PV2 MaintenanceStartDateTime",
					pv1_MSD, epsMember.getMaintenanceStartDateTime());

		}

		// Confirm CREATBY and LASTMODIFIEDBY for both the policy and members.
		String createBy_PV1 = bemDTOInitial.getBatchId().toString();
		// PV1 lastModBy will be PV2's batchId, since the MAINTENANCENDDATETIME is UPDATEd from HIGHDATE to 1 millisecond less than now().
		String lastModBy_PV1 = bemDTO_EFF.getBatchId().toString();

		// All member versions were created by the INITIAL transaction, hence all members should
		// have a createBy and lastmodidifiedBy of the batchId of the transaction that created them.
		String createBy_PVM =  bemDTOInitial.getBatchId().toString();


		List<Map<String, Object>> actualPolicyList = selectPolicyCreateAndLastMod(policyVersionId_V1);

		assertEquals("actual Policy Create/LastMod list", 1, actualPolicyList.size());

		Map<String, Object> row = actualPolicyList.get(0);

		assertEquals("PolicyVersion CreateBy", createBy_PV1, row.get("CREATEBY"));
		assertEquals("PolicyVersion LastModifiedBy", lastModBy_PV1, row.get("LASTMODIFIEDBY"));
		Date actualCreateDT = (Date) row.get("CREATEDATETIME");
		Date actualLastModDT = (Date) row.get("LASTMODIFIEDDATETIME");

		List<Map<String, Object>> actualMemberList = selectMemberCreateAndLastMod(policyVersionId_V1);

		assertEquals("actual Member Create/LastMod list", memberNames.length, actualMemberList.size());

		String msg = "";
		i = 0;
		for (Map<String, Object> rec : actualMemberList) {
			msg = "Record " + (i + 1) +") ";
			assertEquals(msg + "PolicyMemberVersion CreateBy", createBy_PVM, rec.get("CREATEBY"));
			assertEquals(msg + "PolicyMemberVersion LastModifiedBy", createBy_PVM, rec.get("LASTMODIFIEDBY"));
			actualCreateDT = (Date) rec.get("CREATEDATETIME");
			actualLastModDT = (Date) rec.get("LASTMODIFIEDDATETIME");
			// Since member versions did not change, the lastmod DTs should be equal to the original createBy.
			assertEquals(msg + "CreateDateTime and LastModifiedDateTime are still equal", actualCreateDT, actualLastModDT);
			i++;
		}
	}


	/**
	 * Tests new member versions are created when all members change.
	 * - Insert an INITIAL policy
	 * - Insert an EFFECTUATED policy (all inbound members will have changes)
	 * - Confirm the new EFFECTUATED policy is joined to each of the existing policy member versions
	 * - And, all existing members from policy version 1 are now joined to policy version 2 that
	 *   were in inbound transaction.
	 */
	@Test
	public void test_PolicyToMemberVersions_EFFECTUATED_RemoveMember() throws InterruptedException {

		String[] memberNamesINI = {"DAD", "MOM", "SON", "DAU"};
		// make them the same. So test data matches.
		Long bemId = Long.valueOf("666660000") + TestDataUtil.getRandomNumber(4);;
		Long policyVersionId_V1 = null;
		Long policyVersionId_V2 = null;
		String subscriberStateCd = null;

		String mgpi = bemId.toString();
		LocalDate psd = JAN_1;
		LocalDate ped = DEC_31;
		PolicyStatus policyStatus = PolicyStatus.INITIAL_1;
		String exchangePolicyId = TestDataUtil.getRandomNumber(9).toString();
		PolicyInfoType policyInfo = TestDataUtil.makePolicyInfoType(mgpi, psd, ped, policyStatus, exchangePolicyId);


		// Insert INITIAL policy
		BenefitEnrollmentMaintenanceDTO bemDTOInitial = makeBemDTO(bemId, memberNamesINI, null, policyInfo);
		assertNotNull("bemDTOInitial", bemDTOInitial);
		policyDataService.saveBEM(bemDTOInitial);

		// Need to sleep at least 1 sec because CurrentTimestamp is only down to seconds, and will throw
		// contraint violation on PolicyStatus table since TransDateTime is a key field.
		Thread.sleep(SLEEP_INTERVAL_SEC);

		BenefitEnrollmentMaintenanceDTO bemDTOLatest = policyDataService.getLatestBEMByPolicyId(bemDTOInitial);
		assertNotNull("bemDTOLatest BEM", bemDTOLatest.getBem());


		assertNotNull("policyVersionId returned from getLatestBem", bemDTOLatest.getPolicyVersionId());
		policyVersionId_V1 =  bemDTOLatest.getPolicyVersionId();
		subscriberStateCd = bemDTOLatest.getSubscriberStateCd();

		// Insert an EFFECTUATED policy
		String[] memberNamesEFF = {"DAD", "MOM", "SON"};
		policyInfo.setPolicyStatus(PolicyStatus.EFFECTUATED_2.getValue());
		BenefitEnrollmentMaintenanceDTO bemDTO_EFF = makeBemDTO(bemId, memberNamesEFF, policyVersionId_V1, policyInfo);
		for (MemberType member : bemDTO_EFF.getBem().getMember()) {
			member.getMemberNameInformation().getMemberName().setMiddleName("CHANGED");
		}
		assertNotNull("bemDTO_EFF", bemDTO_EFF);
		bemDTO_EFF.setSubscriberStateCd(subscriberStateCd);
		policyDataService.saveBEM(bemDTO_EFF);

		BenefitEnrollmentMaintenanceDTO bemDTOLatest2 = policyDataService.getLatestBEMByPolicyId(bemDTO_EFF);
		assertNotNull("policyVersionId returned from getLatestBem the second time", bemDTOLatest2.getPolicyVersionId());
		policyVersionId_V2 =  bemDTOLatest2.getPolicyVersionId();

		List<PolicyMemberVersionPO> pmvPoList_V1 = jdbc.query(pmvSQL, new PolicyMemberVersionRowMapper(), policyVersionId_V1, bemDTOLatest.getSubscriberStateCd());
		List<PolicyMemberVersionPO> pmvPoList_V2 = jdbc.query(pmvSQL, new PolicyMemberVersionRowMapper(), policyVersionId_V2, bemDTOLatest2.getSubscriberStateCd());

		assertEquals("PolicyVersion 1 member list size", memberNamesINI.length, pmvPoList_V1.size());
		assertEquals("PolicyVersion 2 member list size", memberNamesEFF.length, pmvPoList_V2.size());

		// Confirm ALL member versions from policy 1 are joined to policy 2 by retrieving PolicyMemberVersionPOs (join table)
		long id1;
		long id2;
		int i = 0;

		for (PolicyMemberVersionPO po1: pmvPoList_V1) {
			id1 = po1.getPolicyMemberVersionId().longValue();
			boolean isFound = false;
			for (PolicyMemberVersionPO po2: pmvPoList_V2) {
				id2 = po2.getPolicyMemberVersionId().longValue();
				if (id1 == id2) {
					isFound = true;
					break;
				}
			}
			assertFalse(i + ": PolicyVersion 2 does NOT contain PolicyMemberVersionId " + id1 + " from PolicyVersion 1. ", isFound);
			i++;
		}
	}


	/**
	 * Tests new member versions are created when all members change.
	 * - Insert an INITIAL policy with all members NOT having address info.
	 * - Insert an EFFECTUATED policy with all members having addresses (all inbound members will have changes)
	 */
	@Test
	public void test_PolicyToMemberVersions_EFFECTUATED_AddAddresses() throws InterruptedException {

		String[] memberNames = {"DAD", "MOM", "BBY"};
		// make them the same. So test data matches.
		Long bemId = Long.valueOf("77770000") + TestDataUtil.getRandomNumber(4);;
		Long policyVersionId_V1 = null;
		Long policyVersionId_V2 = null;
		String subscriberStateCd = null;

		String mgpi = bemId.toString();
		LocalDate psd = JAN_1;
		LocalDate ped = DEC_31;
		PolicyStatus policyStatus = PolicyStatus.INITIAL_1;
		String exchangePolicyId = TestDataUtil.getRandomNumber(9).toString();
		PolicyInfoType policyInfo = TestDataUtil.makePolicyInfoType(mgpi, psd, ped, policyStatus, exchangePolicyId);


		// Insert INITIAL policy
		BenefitEnrollmentMaintenanceDTO bemDTOInitial = makeBemDTO(bemId, memberNames, null, policyInfo);
		// Initially members will not have addresses, so remove.
		for (MemberType member : bemDTOInitial.getBem().getMember()) {
			member.getMemberNameInformation().setMemberResidenceAddress(null);
		}
		assertNotNull("bemDTOInitial", bemDTOInitial);
		policyDataService.saveBEM(bemDTOInitial);

		Thread.sleep(SLEEP_INTERVAL_SEC);

		BenefitEnrollmentMaintenanceDTO bemDTOLatest = policyDataService.getLatestBEMByPolicyId(bemDTOInitial);
		assertNotNull("bemDTOLatest BEM", bemDTOLatest.getBem());


		assertNotNull("policyVersionId returned from getLatestBem", bemDTOLatest.getPolicyVersionId());
		policyVersionId_V1 =  bemDTOLatest.getPolicyVersionId();
		subscriberStateCd = bemDTOLatest.getSubscriberStateCd();

		// Insert an EFFECTUATED policy
		policyInfo.setPolicyStatus(PolicyStatus.EFFECTUATED_2.getValue());
		BenefitEnrollmentMaintenanceDTO bemDTO_EFF = makeBemDTO(bemId, memberNames, policyVersionId_V1, policyInfo);
		assertNotNull("bemDTO_EFF", bemDTO_EFF);
		bemDTO_EFF.setSubscriberStateCd(subscriberStateCd);
		// Since the EFF members will now have addresses, all members change.
		policyDataService.saveBEM(bemDTO_EFF);


		BenefitEnrollmentMaintenanceDTO bemDTOLatest2 = policyDataService.getLatestBEMByPolicyId(bemDTO_EFF);
		assertNotNull("policyVersionId returned from getLatestBem the second time", bemDTOLatest2.getPolicyVersionId());
		policyVersionId_V2 =  bemDTOLatest2.getPolicyVersionId();

		List<PolicyMemberAddressPO> addrList_V1 = jdbc.query(pmvAddrSQL, new PolicyMemberAddressRowMapper(), policyVersionId_V1);
		List<PolicyMemberAddressPO> addrList_V2 = jdbc.query(pmvAddrSQL, new PolicyMemberAddressRowMapper(), policyVersionId_V2);

		assertEquals("PolicyVersion 1 member list size", 0, addrList_V1.size());
		assertEquals("PolicyVersion 2 member list size", memberNames.length, addrList_V2.size());

		// Confirm the members from PV1 have MAINTENANCEENDDATETIME set to one second less than 
		// PV2 MAINTENANCEENDDATETIME.
		List<PolicyVersionPO> epsList = jdbc.query(pvSQL, new PolicyVersionRowMapper(), policyVersionId_V2, subscriberStateCd);
		PolicyVersionPO epsPO_2 = epsList.get(0);
		LocalDateTime pv2_MSD_Less1Milli = epsPO_2.getMaintenanceStartDateTime().minusNanos(1000000);

		List<PolicyMemberVersionPO> epsMemberList = jdbc.query(pmvSQL, new PolicyMemberVersionRowMapper(), policyVersionId_V1, subscriberStateCd);

		assertEquals("PolicyMemberVersions list size", memberNames.length, epsMemberList.size());

		for (PolicyMemberVersionPO epsMember : epsMemberList) {

			assertEquals(epsMember.getExchangeMemberID() + " MaintenanceEndDateTime compared to PV2 MaintenanceStartDateTime",
					pv2_MSD_Less1Milli, epsMember.getMaintenanceEndDateTime());
		}

		// Confirm CREATBY and LASTMODIFIEDBY for both the policy and members.
		String user_PV1 = bemDTOInitial.getBatchId().toString();
		// PV1 lastModBy will be PV2's batchId, since the MAINTENANCENDDATETIME is updated from HIGHDATE to 1 millisecond less than now().
		String user_PV2 = bemDTO_EFF.getBatchId().toString();


		List<Map<String, Object>> actualPolicyList = selectPolicyCreateAndLastMod(policyVersionId_V1);

		assertEquals("actual Policy Create/LastMod list", 1, actualPolicyList.size());

		Map<String, Object> row = actualPolicyList.get(0);

		assertEquals("PolicyVersion CreateBy", user_PV1, row.get("CREATEBY"));
		assertEquals("PolicyVersion LastModifiedBy", user_PV2, row.get("LASTMODIFIEDBY"));
		Date actualCreateDT = (Date) row.get("CREATEDATETIME");
		Date actualLastModDT = (Date) row.get("LASTMODIFIEDDATETIME");
	
		// New member versions were created by the EFF transaction, hence all members should
		// have a createBy and lastmodidifiedBy of the batchId of the transaction that created them.

		List<Map<String, Object>> actualMemberList_PV1 = selectMemberCreateAndLastMod(policyVersionId_V1);

		assertEquals("actual Member Create/LastMod list", memberNames.length, actualMemberList_PV1.size());

		String msg = "";
		int i = 0;
		for (Map<String, Object> rec : actualMemberList_PV1) {
			msg = "Record " + (i + 1) +") ";
			assertEquals(msg + "PolicyMemberVersion CreateBy", user_PV1, rec.get("CREATEBY"));
			assertEquals(msg + "PolicyMemberVersion LastModifiedBy", user_PV2, rec.get("LASTMODIFIEDBY"));
			i++;
		}

		List<Map<String, Object>> actualMemberList_PV2 = selectMemberCreateAndLastMod(policyVersionId_V2);

		assertEquals("actual Member Create/LastMod list", memberNames.length, actualMemberList_PV2.size());

		i = 0;
		for (Map<String, Object> rec : actualMemberList_PV2) {
			msg = "Record " + (i + 1) +") ";
			assertEquals(msg + "PolicyMemberVersion CreateBy", user_PV2, rec.get("CREATEBY"));
			assertEquals(msg + "PolicyMemberVersion LastModifiedBy", user_PV2, rec.get("LASTMODIFIEDBY"));
			actualCreateDT = (Date) rec.get("CREATEDATETIME");
			actualLastModDT = (Date) rec.get("LASTMODIFIEDDATETIME");
			// Since member versions are new, the lastmod DTs should be the same as createDT.
			assertEquals(msg + "CreateDateTime and LastModifiedDateTime are equal", actualCreateDT, actualLastModDT);
			i++;
		}
	}



	/**
	 * Tests new member versions are NOT created when one member of the policy
	 * is missing ExchangeAssignedMemberID.
	 */
	@Test
	public void test_saveBem_MissingExchangeAssignedMemberId()  {

		String expectedCode = EProdEnum.EPROD_10.getCode();
		String[] memberNamesINI = {"DAD", "MOM", "SON", "DAU"};
		// make them the same. So test data matches.
		Long bemId = Long.valueOf("888880000") + TestDataUtil.getRandomNumber(4);
		String mgpi = bemId.toString();
		LocalDate psd = JAN_1;
		LocalDate ped = DEC_31;
		PolicyStatus policyStatus = PolicyStatus.INITIAL_1;
		String exchangePolicyId = TestDataUtil.getRandomNumber(9).toString();
		PolicyInfoType policyInfo = TestDataUtil.makePolicyInfoType(mgpi, psd, ped, policyStatus, exchangePolicyId);

		BenefitEnrollmentMaintenanceDTO bemDTOInitial = makeBemDTO(bemId, memberNamesINI, null, policyInfo);
		// Insert  policy with one member missing ExchangeAssignedMemberID.
		bemDTOInitial.getBem().getMember().get(2).getMemberAdditionalIdentifier().setExchangeAssignedMemberID(null);
		assertNotNull("bemDTOInitial", bemDTOInitial);

		String actualCode = null;
		try {
			policyDataService.saveBEM(bemDTOInitial);
		} 
		catch (ApplicationException appEx) {

			actualCode = appEx.getInformationCode();
		}
		assertEquals("ApplicationException code for member without ExchangeAssignedMemberId", expectedCode, actualCode);
	}


	/**
	 * Tests new member versions are NOT created when one member of the policy
	 * has the same ExchangeAssignedMemberID as another member.
	 */
	@Test
	public void test_saveBem_DuplicateMember()  {

		String expectedCode = EProdEnum.EPROD_10.getCode();
		String[] memberNamesINI = {"DAD", "MOM", "SON", "DAU"};
		// make them the same. So test data matches.
		Long bemId = Long.valueOf("999990000") + TestDataUtil.getRandomNumber(4);
		String mgpi = bemId.toString();
		LocalDate psd = JAN_1;
		LocalDate ped = DEC_31;
		PolicyStatus policyStatus = PolicyStatus.INITIAL_1;
		String exchangePolicyId = TestDataUtil.getRandomNumber(9).toString();
		PolicyInfoType policyInfo = TestDataUtil.makePolicyInfoType(mgpi, psd, ped, policyStatus, exchangePolicyId);

		BenefitEnrollmentMaintenanceDTO bemDTOInitial = makeBemDTO(bemId, memberNamesINI, null, policyInfo);
		String exAssMemId_1 = bemDTOInitial.getBem().getMember().get(0).getMemberAdditionalIdentifier().getExchangeAssignedMemberID();
		// Insert  policy with one member missing ExchangeAssignedMemberID.
		bemDTOInitial.getBem().getMember().get(2).getMemberAdditionalIdentifier().setExchangeAssignedMemberID(exAssMemId_1);
		assertNotNull("bemDTOInitial", bemDTOInitial);

		String actualCode = null;
		try {
			policyDataService.saveBEM(bemDTOInitial);
		} 
		catch (ApplicationException appEx) {

			actualCode = appEx.getInformationCode();
		}
		assertEquals("ApplicationException code", expectedCode, actualCode);
	}



	/**
	 * Tests a new member version is NOT created when EFFECTUATing a pre-EPM_25.0 policy.  
	 * - Insert an INITIAL policy with mostly full policy and member version information to simulate pre-EPM_25.0 data.
	 * - Insert an EFFECTUATED policy calling EPM_25.0 code.
	 * - Confirm the same member version is joined to both the old and new policies since member data did not change.
	 *     old EPS data {A1, B1, C1, D1, E1} new inbound data {A1, D1}  <-- inbound data is not different than ESP though contains less.
	 * - Confirm the member version still has MaintenanceEndDateTime of HIGHDATE.
	 */
	@Test
	public void test_EFFECTUATED_Against_Pre25Data() throws InterruptedException {

		String[] memberNames = {"DAD"};
		// make them the same. So test data matches.
		Long bemId = Long.valueOf("77770000") + TestDataUtil.getRandomNumber(4);
		Long transMsgId_V1 = null;
		Long policyVersionId_V1 = null;
		Long policyVersionId_V2 = null;
		String subscriberStateCd = null;

		String mgpi = bemId.toString();
		LocalDate psd = JAN_1;
		LocalDate ped = DEC_31;
		PolicyStatus policyStatus = PolicyStatus.INITIAL_1;
		String exchangePolicyId = TestDataUtil.getRandomNumber(9).toString();
		PolicyInfoType policyInfo = TestDataUtil.makePolicyInfoType(mgpi, psd, ped, policyStatus, exchangePolicyId);

		// Make and INITIAL policy
		BenefitEnrollmentMaintenanceDTO bemDTOInitial = makeBemDTO(bemId, memberNames, null, policyInfo);
		assertNotNull("bemDTOInitial", bemDTOInitial);
		transMsgId_V1 = bemDTOInitial.getTransMsgId();
		// Get data from subscriber that was just made.
		MemberType subscriber = BEMDataUtil.getSubscriberMember(bemDTOInitial.getBem());
		subscriberStateCd = bemDTOInitial.getSubscriberStateCd();
		String subscriberId = BEMDataUtil.getSubscriberID(subscriber);

		// Manually insert a nearly FULL EPS policy.
		TestEPSPre25Data pre25Data = new TestEPSPre25Data();
		pre25Data.setJdbc(jdbc);

		PolicyVersionPO epsPV = pre25Data.makePolicyVersion(bemId, subscriberStateCd, exchangePolicyId, JAN_1_1am.minusMonths(1));
		epsPV.setExchangeAssignedSubscriberID(subscriberId);
		epsPV.setPolicyStartDate(psd);
		epsPV.setPolicyEndDate(ped);
		epsPV.setEligibilityStartDate(BEMDataUtil.getEligibilityBeginDate(subscriber));
		epsPV.setEligibilityEndDate(BEMDataUtil.getEligibilityEndDate(subscriber));
		// Add data no longer used in EPM_25.0
		epsPV.setPremiumPaidToEndDate(DateTimeUtil.getLocalDateFromXmlGC(BEMDataUtil.getHealthCoverageDatesType(subscriber).getPremiumPaidToDateEnd()));
		epsPV.setLastPremiumPaidDate(DateTimeUtil.getLocalDateFromXmlGC(BEMDataUtil.getHealthCoverageDatesType(subscriber).getLastPremiumPaidDate()));
		epsPV.setSourceVersionDateTime(JAN_1_1am);
		epsPV.setX12InsrncLineTypeCd("HLT");
		epsPV.setPlanID(BEMDataUtil.getPlanID(subscriber));
		epsPV.setTransMsgID(transMsgId_V1);

		policyVersionId_V1 = pre25Data.insertPolicyVersion(epsPV);

		PolicyMemberVersionPO epsPMV = pre25Data.makePolicyMemberVersion(BEMDataUtil.getExchangeAssignedMemberId(subscriber), JAN_1_1am.minusMonths(1), exchangePolicyId, subscriberStateCd);
		epsPMV.setTransMsgID(transMsgId_V1);
		epsPMV.setIssuerAssignedMemberID(subscriber.getMemberAdditionalIdentifier().getIssuerAssignedMemberID());
		epsPMV.setPolicyMemberEligStartDate(BEMDataUtil.getEligibilityBeginDate(subscriber));
		epsPMV.setPolicyMemberEligEndDate(BEMDataUtil.getEligibilityEndDate(subscriber));
		epsPMV.setSubscriberInd(BooleanIndicatorSimpleType.Y.value());
		epsPMV.setPolicyMemberBirthDate(DateTimeUtil.getLocalDateFromXmlGC(subscriber.getMemberNameInformation().getMemberDemographics().getBirthDate()));
		epsPMV.setPolicyMemberLastNm(subscriber.getMemberNameInformation().getMemberName().getLastName());
		epsPMV.setPolicyMemberFirstNm(subscriber.getMemberNameInformation().getMemberName().getFirstName());
		epsPMV.setPolicyMemberMiddleNm(subscriber.getMemberNameInformation().getMemberName().getMiddleName());
		epsPMV.setPolicyMemberSalutationNm(subscriber.getMemberNameInformation().getMemberName().getNamePrefix());
		epsPMV.setPolicyMemberSuffixNm(subscriber.getMemberNameInformation().getMemberName().getNameSuffix());
		epsPMV.setPolicyMemberSSN(subscriber.getMemberNameInformation().getMemberName().getSocialSecurityNumber());

		Long pmvId_V1 = pre25Data.insertPolicyMemberVersion(epsPMV);

		PolicyMemberAddressPO epsAddrPO = new PolicyMemberAddressPO();
		epsAddrPO.setPolicyMemberVersionId(pmvId_V1);
		epsAddrPO.setStateCd(subscriber.getMemberNameInformation().getMemberResidenceAddress().getStateCode());
		epsAddrPO.setZipPlus4Cd(subscriber.getMemberNameInformation().getMemberResidenceAddress().getPostalCode());
		pre25Data.insertPolicyMemberAddress(epsAddrPO);

		PolicyMemberDatePO epsDatePO = new PolicyMemberDatePO();
		epsDatePO.setPolicyMemberVersionId(pmvId_V1);
		// Add data that is moved to PolicyMemberDate table for EPM_25.0
		LocalDate hcBBD = BEMDataUtil.getBenefitBeginDate(subscriber);
		LocalDate hcBED = BEMDataUtil.getBenefitEndDate(subscriber);
		epsDatePO.setPolicyMemberStartDate(hcBBD);
		epsDatePO.setPolicyMemberEndDate(hcBED);
		pre25Data.insertPolicyMemberDate(epsDatePO);

		// Join pre25 member to pre25 policy.
		PolicyMemberPO polMemPO = pre25Data.makePolicyMember(policyVersionId_V1, pmvId_V1);
		pre25Data.insertPolicyMember(polMemPO);

		Thread.sleep(SLEEP_INTERVAL_SEC);

		// confirm the INITIAL pre25 full policy exists in EPS.
		BenefitEnrollmentMaintenanceDTO bemDTOLatest = policyDataService.getLatestBEMByPolicyId(bemDTOInitial);
		assertNotNull("bemDTOLatest BEM", bemDTOLatest.getBem());

		assertNotNull("policyVersionId returned from getLatestBem", bemDTOLatest.getPolicyVersionId());
		policyVersionId_V1 =  bemDTOLatest.getPolicyVersionId();
		subscriberStateCd = bemDTOLatest.getSubscriberStateCd();

		// Insert an EFFECTUATED policy (EPM_25.0)
		policyInfo.setPolicyStatus(PolicyStatus.EFFECTUATED_2.getValue());
		BenefitEnrollmentMaintenanceDTO bemDTO_EFF = makeBemDTO(bemId, memberNames, policyVersionId_V1, policyInfo);
		assertNotNull("bemDTO_EFF", bemDTO_EFF);
		bemDTO_EFF.setSubscriberStateCd(subscriberStateCd);

		policyDataService.saveBEM(bemDTO_EFF);

		// Retrieve the EFFECTUATED policy
		BenefitEnrollmentMaintenanceDTO bemDTOLatest2 = policyDataService.getLatestBEMByPolicyId(bemDTO_EFF);
		assertNotNull("policyVersionId returned from getLatestBem the second time", bemDTOLatest2.getPolicyVersionId());
		policyVersionId_V2 =  bemDTOLatest2.getPolicyVersionId();

		List<PolicyMemberVersionPO> pmvList_V1 = jdbc.query(pmvSQL, new PolicyMemberVersionRowMapper(), policyVersionId_V1, subscriberStateCd);
		List<PolicyMemberVersionPO> pmvList_V2 = jdbc.query(pmvSQL, new PolicyMemberVersionRowMapper(), policyVersionId_V2, subscriberStateCd);

		assertEquals("PolicyVersion 1 member list size", memberNames.length, pmvList_V1.size());
		assertEquals("PolicyVersion 2 member list size", memberNames.length, pmvList_V2.size());

		Long actualPmvId_V1 = pmvList_V1.get(0).getPolicyMemberVersionId();
		Long actualPmvId_V2 = pmvList_V2.get(0).getPolicyMemberVersionId();

		assertEquals("PolicyMemberVersionId is the same for both PV1 and PV2", actualPmvId_V1, actualPmvId_V2);

		// Confirm the member from PV1 has MAINTENANCEENDDATETIME set HIGHDATE, meaning
		// a new member version was NOT created.
		List<PolicyMemberVersionPO> epsMemberList = jdbc.query(pmvSQL, new PolicyMemberVersionRowMapper(), policyVersionId_V1, subscriberStateCd);

		assertEquals("PolicyMemberVersions list size", memberNames.length, epsMemberList.size());

		PolicyMemberVersionPO epsMember = epsMemberList.get(0);

		assertEquals(epsMember.getExchangeMemberID() + " MaintenanceEndDateTime compared to PV2 MaintenanceStartDateTime",
				DateTimeUtil.HIGHDATE, epsMember.getMaintenanceEndDateTime());

	}



	/**
	 * Tests policyStatuses are appended to latest EPS policy and are only
	 * added if different than previous.
	 * - Inbounds 4 bems:  INI, EFF, EFF, SUP
	 * - Confirms latest policy will end up with only 3 statuses.
	 * @throws InterruptedException 
	 */
	@Test
	public void test_saveBem_SUPERSEDED() throws InterruptedException  {

		// Since order by TransDateTime DESC, the "latest" EPS policy status will be last in.
		String[] expectedStatusList = {"5", "2", "1"};

		String[] memberNames = {"DAD", "MOM"};
		// make them the same. So test data matches.
		Long bemId = Long.valueOf("888880000") + TestDataUtil.getRandomNumber(4);
		Long policyVersionId_V1 = null;
		Long policyVersionId_V2 = null;
		Long policyVersionId_V3 = null;
		Long policyVersionId_V4 = null;

		String mgpi = bemId.toString();
		LocalDate psd = JAN_1;
		LocalDate ped = DEC_31;
		PolicyStatus policyStatus = PolicyStatus.INITIAL_1;
		String exchangePolicyId = TestDataUtil.getRandomNumber(9).toString();
		PolicyInfoType policyInfo = TestDataUtil.makePolicyInfoType(mgpi, psd, ped, policyStatus, exchangePolicyId);

		// Insert INITIAL policy
		BenefitEnrollmentMaintenanceDTO bemDTOInitial = makeBemDTO(bemId, memberNames, null, policyInfo);
		policyDataService.saveBEM(bemDTOInitial);

		Thread.sleep(SLEEP_INTERVAL_SEC);

		BenefitEnrollmentMaintenanceDTO bemDTOLatest1 = policyDataService.getLatestBEMByPolicyId(bemDTOInitial);
		assertNotNull("bemDTOLatest BEM", bemDTOLatest1.getBem());

		assertNotNull("policyVersionId returned from getLatestBem", bemDTOLatest1.getPolicyVersionId());
		policyVersionId_V1 =  bemDTOLatest1.getPolicyVersionId();

		// Insert an EFFECTUATED policy
		policyInfo.setPolicyStatus(PolicyStatus.EFFECTUATED_2.getValue());
		BenefitEnrollmentMaintenanceDTO bemDTO_EFF = makeBemDTO(bemId, memberNames, policyVersionId_V1, policyInfo);
		policyDataService.saveBEM(bemDTO_EFF);

		BenefitEnrollmentMaintenanceDTO bemDTOLatest2 = policyDataService.getLatestBEMByPolicyId(bemDTO_EFF);
		assertNotNull("policyVersionId returned from getLatestBem the second time", bemDTOLatest2.getPolicyVersionId());
		policyVersionId_V2 =  bemDTOLatest2.getPolicyVersionId();

		Thread.sleep(SLEEP_INTERVAL_SEC);

		// Insert another EFFECTUATED policy (Maintenance transaction)
		policyInfo.setPolicyStatus(PolicyStatus.EFFECTUATED_2.getValue());
		BenefitEnrollmentMaintenanceDTO bemDTO_EFF2 = makeBemDTO(bemId, memberNames, policyVersionId_V2, policyInfo);
		policyDataService.saveBEM(bemDTO_EFF2);

		BenefitEnrollmentMaintenanceDTO bemDTOLatest3 = policyDataService.getLatestBEMByPolicyId(bemDTO_EFF);
		assertNotNull("policyVersionId returned from getLatestBem the second time", bemDTOLatest3.getPolicyVersionId());
		policyVersionId_V3 =  bemDTOLatest3.getPolicyVersionId();

		Thread.sleep(SLEEP_INTERVAL_SEC);

		// Insert a SUPERSEDED policy
		policyInfo.setPolicyStatus(PolicyStatus.SUPERSEDED_5.getValue());
		BenefitEnrollmentMaintenanceDTO bemDTO_SUP = makeBemDTO(bemId, memberNames, policyVersionId_V3, policyInfo);
		policyDataService.saveBEM(bemDTO_SUP);

		BenefitEnrollmentMaintenanceDTO bemDTOLatest4 = policyDataService.getLatestBEMByPolicyId(bemDTO_EFF2);
		assertNotNull("policyVersionId returned from getLatestBem the third time", bemDTOLatest4.getPolicyVersionId());
		policyVersionId_V4 =  bemDTOLatest4.getPolicyVersionId();

		List<PolicyStatusPO> pvStatusList_V4 = jdbc.query(pvStatusSQL, new PolicyStatusRowMapper(), policyVersionId_V4);

		assertEquals("PolicyVersion 4 staus list size", 3, pvStatusList_V4.size());

		int i = 0;
		for (PolicyStatusPO po : pvStatusList_V4) {
			assertEquals(i + ": PolicyStatus", expectedStatusList[i++], po.getInsuranacePolicyStatusTypeCd());
		}

	}
}