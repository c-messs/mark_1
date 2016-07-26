package gov.hhs.cms.ff.fm.eps.ep.services;

import gov.cms.dsh.bem.GenderCodeSimpleType;
import gov.cms.dsh.bem.MemberType;
import gov.hhs.cms.ff.fm.eps.ep.data.util.TestDataUtil;
import gov.hhs.cms.ff.fm.eps.ep.enums.AddressTypeEnum;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyMemberAddressPO;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyMemberDatePO;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyMemberVersionPO;
import gov.hhs.cms.ff.fm.eps.ep.services.impl.FFMMemberDAOImpl;
import gov.hhs.cms.ff.fm.eps.ep.vo.UserVO;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author j.radziewski
 * 
 * Test all methods of FFMMemberDaoImpl except for processMembers(), which are include in 
 * FFMDataServiceImplTest.  
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/eps-data-config.xml", "classpath:/test-context-data.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class MemberDataDAOImplTest extends BaseServicesTest {

	@Autowired
	FFMMemberDAOImpl memberDataDAO;

	UserVO userVO = new UserVO("");


	@Test
	public void test_findEpsMember() {

		Long memId = Long.valueOf("1111");
		String name = "DAD";
		boolean isSubscriber = true;
		MemberType inboundMember = TestDataUtil.makeMemberType(memId, name, isSubscriber);

		List<PolicyMemberVersionPO> epsList = new ArrayList<PolicyMemberVersionPO>();
		PolicyMemberVersionPO pmvEPS = new PolicyMemberVersionPO();
		pmvEPS.setExchangeMemberID(TestDataUtil.makeExchangeAssignedMemberID(memId, name));
		epsList.add(pmvEPS);

		PolicyMemberVersionPO actual = (PolicyMemberVersionPO) ReflectionTestUtils.invokeMethod(memberDataDAO, "findEpsMember", new Object[] {inboundMember, epsList});

		assertNotNull("PolicyMemberVersionPO found", actual);
		assertEquals("", inboundMember.getMemberAdditionalIdentifier().getExchangeAssignedMemberID(), actual.getExchangeMemberID());
	}

	@Test
	public void test_findEpsMember_EmptyEPS() {

		Long memId = Long.valueOf("1111");
		String name = "DAD";
		boolean isSubscriber = true;
		MemberType inboundMember = TestDataUtil.makeMemberType(memId, name, isSubscriber);

		List<PolicyMemberVersionPO> epsList = new ArrayList<PolicyMemberVersionPO>();

		PolicyMemberVersionPO actual = (PolicyMemberVersionPO) ReflectionTestUtils.invokeMethod(memberDataDAO, "findEpsMember", new Object[] {inboundMember, epsList});

		assertNull("PolicyMemberVersionPO NOTfound", actual);
	}

	@Test
	public void test_findEpsMember_NoFound() {

		Long memId = Long.valueOf("1111");
		String name = "DAD";
		boolean isSubscriber = true;
		MemberType inboundMember = TestDataUtil.makeMemberType(memId, name, isSubscriber);

		memId++;
		List<PolicyMemberVersionPO> epsList = new ArrayList<PolicyMemberVersionPO>();
		PolicyMemberVersionPO pmvEPS1 = new PolicyMemberVersionPO();
		pmvEPS1.setExchangeMemberID(TestDataUtil.makeExchangeAssignedMemberID(memId, "MOM"));
		epsList.add(pmvEPS1);

		memId++;
		PolicyMemberVersionPO pmvEPS2 = new PolicyMemberVersionPO();
		pmvEPS2.setExchangeMemberID(TestDataUtil.makeExchangeAssignedMemberID(memId, "SON"));
		epsList.add(pmvEPS2);

		memId++;
		PolicyMemberVersionPO pmvEPS3 = new PolicyMemberVersionPO();
		pmvEPS3.setExchangeMemberID(TestDataUtil.makeExchangeAssignedMemberID(memId, "DAU"));
		epsList.add(pmvEPS3);

		PolicyMemberVersionPO actual = (PolicyMemberVersionPO) ReflectionTestUtils.invokeMethod(memberDataDAO, "findEpsMember", new Object[] {inboundMember, epsList});

		assertNull("PolicyMemberVersionPO NOT found", actual);
	}




	@Test
	public void test_findEpsMemberAddr_NullMember() {

		PolicyMemberAddressPO expected = null;
		PolicyMemberVersionPO epsMember = null;
		List<PolicyMemberAddressPO> epsList = new ArrayList<PolicyMemberAddressPO>();
		PolicyMemberAddressPO actual = (PolicyMemberAddressPO) ReflectionTestUtils.invokeMethod(memberDataDAO, "findEpsMemberAddr", new Object[] {epsMember, epsList});
		assertEquals("PolicyMemberAddressPO", expected, actual);
	}

	@Test
	public void test_findEpsMemberAddr_NoMatch() {

		PolicyMemberVersionPO epsMember = makePolicyMemberVersionPOMinimum(Long.valueOf("000000"));
		PolicyMemberAddressPO expected = null;
		List<PolicyMemberAddressPO> epsList = new ArrayList<PolicyMemberAddressPO>();
		epsList.add(makePolicyMemberAddressPO(Long.valueOf("111111"), "VA", "22003"));
		epsList.add(makePolicyMemberAddressPO(Long.valueOf("222222"), "VA", "22003"));
		epsList.add(makePolicyMemberAddressPO(Long.valueOf("333333"), "VA", "22003"));
		PolicyMemberAddressPO actual = (PolicyMemberAddressPO) ReflectionTestUtils.invokeMethod(memberDataDAO, "findEpsMemberAddr", new Object[] {epsMember, epsList});
		assertEquals("PolicyMemberAddressPO", expected, actual);
	}


	@Test
	public void test_findEpsMemberAddr() {

		PolicyMemberVersionPO epsMember = makePolicyMemberVersionPOMinimum(Long.valueOf("222222"));
		PolicyMemberAddressPO expected = makePolicyMemberAddressPO(epsMember.getPolicyMemberVersionId(), "VA", "22003");
		List<PolicyMemberAddressPO> epsList = new ArrayList<PolicyMemberAddressPO>();
		epsList.add(makePolicyMemberAddressPO(Long.valueOf("111111"), "VA", "22003"));
		epsList.add(makePolicyMemberAddressPO(Long.valueOf("222222"), "VA", "22003"));
		epsList.add(makePolicyMemberAddressPO(Long.valueOf("333333"), "VA", "22003"));
		PolicyMemberAddressPO actual = (PolicyMemberAddressPO) ReflectionTestUtils.invokeMethod(memberDataDAO, "findEpsMemberAddr", new Object[] {epsMember, epsList});
		assertEquals("PolicyMemberAddressPO", expected, actual);
	}


	@Test
	public void test_findEpsMemberDate_NullMember() {

		PolicyMemberDatePO expected = null;
		PolicyMemberVersionPO epsMember = null;
		List<PolicyMemberDatePO> epsList = new ArrayList<PolicyMemberDatePO>();
		PolicyMemberDatePO actual = (PolicyMemberDatePO) ReflectionTestUtils.invokeMethod(memberDataDAO, "findEpsMemberDate", new Object[] {epsMember, epsList});
		assertEquals("PolicyMemberDatePO", expected, actual);
	}

	@Test
	public void test_findEpsMemberDate_NoMatch() {

		PolicyMemberVersionPO epsMember = makePolicyMemberVersionPOMinimum(Long.valueOf("000000"));
		PolicyMemberDatePO expected = null;
		List<PolicyMemberDatePO> epsList = new ArrayList<PolicyMemberDatePO>();
		epsList.add(makePolicyMemberDatePO(Long.valueOf("111111"), JAN_1, DEC_31));
		epsList.add(makePolicyMemberDatePO(Long.valueOf("222222"), JAN_1, DEC_31));
		epsList.add(makePolicyMemberDatePO(Long.valueOf("333333"), JAN_1, DEC_31));
		PolicyMemberDatePO actual = (PolicyMemberDatePO) ReflectionTestUtils.invokeMethod(memberDataDAO, "findEpsMemberDate", new Object[] {epsMember, epsList});
		assertEquals("PolicyMemberDatePO", expected, actual);
	}


	@Test
	public void test_findEpsMemberDate() {

		PolicyMemberVersionPO epsMember = makePolicyMemberVersionPOMinimum(Long.valueOf("222222"));
		PolicyMemberDatePO expected = makePolicyMemberDatePO(epsMember.getPolicyMemberVersionId(), APR_1, APR_20);
		List<PolicyMemberDatePO> epsList = new ArrayList<PolicyMemberDatePO>();
		epsList.add(makePolicyMemberDatePO(Long.valueOf("111111"), JAN_1, DEC_31));
		epsList.add(makePolicyMemberDatePO(Long.valueOf("222222"), APR_1, APR_20));
		epsList.add(makePolicyMemberDatePO(Long.valueOf("333333"),  JAN_1, DEC_31));
		PolicyMemberDatePO actual = (PolicyMemberDatePO) ReflectionTestUtils.invokeMethod(memberDataDAO, "findEpsMemberDate", new Object[] {epsMember, epsList});
		assertEquals("PolicyMemberDatePO", expected, actual);
	}




	private PolicyMemberVersionPO makePolicyMemberVersionPOMinimum(Long pmvId) {

		PolicyMemberVersionPO po = new PolicyMemberVersionPO();
		po.setPolicyMemberVersionId(pmvId);
		po.setTransMsgID(TRANS_MSG_ID);
		po.setX12GenderTypeCd(GenderCodeSimpleType.M.value());

		return po;
	}

	private PolicyMemberAddressPO makePolicyMemberAddressPO(Long pmvId, String stateCd, String zip) {

		PolicyMemberAddressPO po = new PolicyMemberAddressPO();

		po.setPolicyMemberVersionId(pmvId);
		po.setX12addressTypeCd(AddressTypeEnum.RESIDENTIAL.getValue());
		po.setStateCd(stateCd);
		po.setZipPlus4Cd(zip);

		return po;
	}


	private PolicyMemberDatePO makePolicyMemberDatePO(Long pmvId, DateTime pmsd, DateTime pmed) {

		PolicyMemberDatePO po = new PolicyMemberDatePO();

		po.setPolicyMemberVersionId(pmvId);
		po.setPolicyMemberStartDate(pmsd);
		po.setPolicyMemberEndDate(pmed);

		return po;
	}




}
