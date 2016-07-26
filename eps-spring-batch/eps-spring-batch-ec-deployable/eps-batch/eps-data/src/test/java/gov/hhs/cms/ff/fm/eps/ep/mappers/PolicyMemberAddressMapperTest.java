package gov.hhs.cms.ff.fm.eps.ep.mappers;

import gov.cms.dsh.bem.AddressType;
import gov.cms.dsh.bem.MemberNameInfoType;
import gov.cms.dsh.bem.MemberType;
import gov.cms.dsh.bem.ResidentialAddressType;
import gov.hhs.cms.ff.fm.eps.ep.data.util.TestDataUtil;
import gov.hhs.cms.ff.fm.eps.ep.enums.AddressTypeEnum;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyMemberAddressPO;

import org.junit.Test;

public class PolicyMemberAddressMapperTest extends BaseMapperTest {


	private PolicyMemberAddressMapper mapper = new PolicyMemberAddressMapper();

	/*
	 * Tests mapping VO to PO.  Verifies all data being set in PO is equal to VO data.

	 * - Creates 1 inbound member loop.  
	 *    -Residential Address
	 * - Sets same EPS data (epsPO)
	 * - Confirms data did not change
	 */
	@Test
	public void test_mapFFMToEPS_NoChange() {

		Long memId = new Long(TestDataUtil.getRandom3DigitNumber());
		String name = "P_";

		MemberType expectedMember = makeSubscriber();
		expectedMember.setMemberNameInformation(TestDataUtil.makeMemberNameInfoType(memId + 3, name + "3"));
		ResidentialAddressType expectedResAddrType = expectedMember.getMemberNameInformation().getMemberResidenceAddress();


		PolicyMemberAddressPO epsPO = makePolicyMemberAddressPOFromAddressType(expectedResAddrType);
		epsPO.setX12addressTypeCd(AddressTypeEnum.RESIDENTIAL.getValue());

		PolicyMemberAddressPO po = mapper.mapFFMToEPS(expectedMember, epsPO);

		AddressType expectedAddr = null;

		assertEquals("X12addressTypeCd", AddressTypeEnum.RESIDENTIAL.getValue(), po.getX12addressTypeCd());

		expectedAddr = expectedMember.getMemberNameInformation().getMemberResidenceAddress();
		// CountyNm, Ignore - Not on the 834
		assertPolicyMemberAddressPO(expectedAddr, po);
		assertFalse("PolicyMemberChanged", po.isPolicyMemberChanged());
	}


	/*
	 * Tests mapping VO .
	 */
	@Test
	public void test_mapFFMToEPS_ChangeState() {
		
		String epsStateCd  = "TX";
		String epsZip = "666666";

		// inbound data will have 1 field different then PO data
		String expectedStateCd = "VA";
		String expectedZip = epsZip;

		MemberType expectedMember = makeSubscriber();
		expectedMember.setMemberNameInformation(new MemberNameInfoType());
		ResidentialAddressType resAddr = new ResidentialAddressType();
		resAddr.setPostalCode(expectedZip);
		resAddr.setStateCode(expectedStateCd);
		expectedMember.getMemberNameInformation().setMemberResidenceAddress(resAddr);

		PolicyMemberAddressPO epsPO = new PolicyMemberAddressPO();
		epsPO.setX12addressTypeCd(AddressTypeEnum.RESIDENTIAL.getValue());
		epsPO.setZipPlus4Cd(epsZip);
		epsPO.setStateCd(epsStateCd);

		PolicyMemberAddressPO actualPO = mapper.mapFFMToEPS(expectedMember, epsPO);

		String strType = AddressTypeEnum.RESIDENTIAL.getDescription();

		assertEquals("X12addressTypeCd", AddressTypeEnum.RESIDENTIAL.getValue(), actualPO.getX12addressTypeCd());
		assertEquals(strType + " ZipCode", expectedZip, actualPO.getZipPlus4Cd());
		assertEquals(strType + " StateCode", expectedStateCd, actualPO.getStateCd());
		assertTrue("PolicyMemberChanged", actualPO.isPolicyMemberChanged());		
	}


	/*
	 * Tests mapping VO to PO and merging existing PO data (db).
	 */
	@Test
	public void test_mapFFMToEPS_ChangeZip() {

		String epsStateCd  = "TX";
		String epsZip = "666666";

		// inbound data will have 1 field different then PO data
		String expectedStateCd = epsStateCd;
		String expectedZip = "77777";

		MemberType expectedMember = makeSubscriber();
		expectedMember.setMemberNameInformation(new MemberNameInfoType());
		ResidentialAddressType expectedAddr = new ResidentialAddressType();
		expectedAddr.setPostalCode(expectedZip);
		expectedAddr.setStateCode(expectedStateCd);
		expectedMember.getMemberNameInformation().setMemberResidenceAddress(expectedAddr);

		PolicyMemberAddressPO epsPO = new PolicyMemberAddressPO();
		epsPO.setX12addressTypeCd(AddressTypeEnum.RESIDENTIAL.getValue());
		// set PO data simulating data coming from EPS
		epsPO.setStateCd(epsStateCd);
		epsPO.setZipPlus4Cd(epsZip);

		PolicyMemberAddressPO actualPO = mapper.mapFFMToEPS(expectedMember, epsPO);

		assertPolicyMemberAddressPO(expectedAddr, actualPO);

		assertTrue("PolicyMemberChanged", actualPO.isPolicyMemberChanged());
	}


	/*
	 * Test mapping inbound non-supscriber to existing EPS data. 
	 * - An old policy will have complete address.  The new inbound policies will not for non-subscribers.
	 *   Therefore, the member will change wiping out all data except for StateCd.
	 */
	@Test
	public void test_mapFFMToEPS_NoChangeForStates_NonSubscriber() {

		String epsStateCd  = "TX";
		String epsZip = "666666";

		// inbound data will have 1 field different then PO data
		String expectedStateCd = epsStateCd;
		String expectedZip = null;

		MemberType expectedMember = new MemberType();
		expectedMember.setMemberNameInformation(new MemberNameInfoType());
		ResidentialAddressType expectedAddr = new ResidentialAddressType();
		expectedAddr.setPostalCode(expectedZip);
		expectedAddr.setStateCode(expectedStateCd);
		expectedMember.getMemberNameInformation().setMemberResidenceAddress(expectedAddr);

		PolicyMemberAddressPO epsPO = new PolicyMemberAddressPO();
		epsPO.setX12addressTypeCd(AddressTypeEnum.RESIDENTIAL.getValue());
		// set PO data simulating data coming from EPS
		epsPO.setStateCd(epsStateCd);
		epsPO.setZipPlus4Cd(epsZip);

		PolicyMemberAddressPO actualPO = mapper.mapFFMToEPS(expectedMember, epsPO);

		assertPolicyMemberAddressPO(expectedAddr, actualPO);

		assertTrue("PolicyMemberChanged", actualPO.isPolicyMemberChanged());
	}

	/*
	 * Tests mapping VO to PO.  Verifies all data being set in PO is equal to VO data.

	 * - Creates 3 inbound member loops (same member).  
	 *    1. Mailing Address
	 *    2. No addresses
	 *    3. Residential Address
	 * - Sets no EPS data (epsPO)
	 */
	@Test
	public void test_mapFFMToEPS() {

		Long memId = new Long(TestDataUtil.getRandom3DigitNumber());
		String name = "P_";

		MemberType expectedMember = makeSubscriber();
		expectedMember.setMemberNameInformation(TestDataUtil.makeMemberNameInfoType(memId, name + "3"));

		PolicyMemberAddressPO epsPO = new PolicyMemberAddressPO();

		PolicyMemberAddressPO po = mapper.mapFFMToEPS(expectedMember, epsPO);

		AddressType expectedAddr = null;
		expectedAddr = expectedMember.getMemberNameInformation().getMemberResidenceAddress();
		assertPolicyMemberAddressPO(expectedAddr, po);
		assertTrue("PolicyMemberChanged", po.isPolicyMemberChanged());
	}

	/*
	 * Tests mapping VO to PO.  Verifies all data being set in PO is equal to VO data.

	 * - Creates 3 inbound member loops with empty address info. (same member).  
	 *   
	 * - Sets no EPS data (epsPO)
	 * - Confirms data did not change
	 */
	@Test
	public void test_mapFFMToEPS_EmptyMember() {

		Long memId = new Long(TestDataUtil.getRandom3DigitNumber());
		String name = "P_";
		
		// no addresses
		MemberType expectedMember = new MemberType();

		MemberNameInfoType expectedMemNameInfo = TestDataUtil.makeMemberNameInfoType(memId + 3, name + "3");
		ResidentialAddressType expectedResAddrType = expectedMemNameInfo.getMemberResidenceAddress();

		PolicyMemberAddressPO epsPO = makePolicyMemberAddressPOFromAddressType(expectedResAddrType);
		epsPO.setX12addressTypeCd(AddressTypeEnum.RESIDENTIAL.getValue());

		PolicyMemberAddressPO po = mapper.mapFFMToEPS(expectedMember, epsPO);

		assertNull("PolicyMemberAddressPO", po);

	}


	@Test
	public void test_mapFFMToEPS_EmptyNameInfo() {

		Long memId = new Long(TestDataUtil.getRandom3DigitNumber());
		String name = "P_";
		
		// no addresses
		MemberType expectedMember = new MemberType();
		expectedMember.setMemberNameInformation(new MemberNameInfoType());

		MemberNameInfoType expectedMemNameInfo = TestDataUtil.makeMemberNameInfoType(memId + 3, name + "3");
		ResidentialAddressType expectedResAddrType = expectedMemNameInfo.getMemberResidenceAddress();

		PolicyMemberAddressPO epsPO = makePolicyMemberAddressPOFromAddressType(expectedResAddrType);
		epsPO.setX12addressTypeCd(AddressTypeEnum.RESIDENTIAL.getValue());

		PolicyMemberAddressPO po = mapper.mapFFMToEPS(expectedMember, epsPO);

		assertNull("PolicyMemberAddressPO", po);
	}


	private void assertPolicyMemberAddressPO(AddressType expected, PolicyMemberAddressPO actual) {

		assertEquals("X12addressTypeCd", AddressTypeEnum.RESIDENTIAL.getValue(), actual.getX12addressTypeCd());
		assertEquals("StateCode", expected.getStateCode(), actual.getStateCd());
		assertEquals("PostalCode", expected.getPostalCode(), actual.getZipPlus4Cd());
	}


	private PolicyMemberAddressPO makePolicyMemberAddressPOFromAddressType(AddressType addrType) {

		PolicyMemberAddressPO po = new PolicyMemberAddressPO();

		po.setStateCd(addrType.getStateCode());
		po.setZipPlus4Cd(addrType.getPostalCode());

		return po;		
	}

}

