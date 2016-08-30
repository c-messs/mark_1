package gov.hhs.cms.ff.fm.eps.ep.mappers;

import java.time.LocalDate;

import gov.cms.dsh.bem.HealthCoverageDatesType;
import gov.cms.dsh.bem.HealthCoverageType;
import gov.cms.dsh.bem.MemberType;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyMemberDatePO;

import org.junit.Test;

public class PolicyMemberDateMapperTest extends BaseMapperTest {

	private PolicyMemberDateMapper mapper = new PolicyMemberDateMapper();


	@Test
	public void test_mapFFMToEPS_NullEPS() {

		Long memId = Long.valueOf("1111");
		String name = "SON";
		boolean isSubscriber = false;
		MemberType expectedMember = makeMemberType(memId, name, isSubscriber);
		
		LocalDate expectedHcBBD = APR_1;
		LocalDate expectedHcBED = MAY_31;
		
		expectedMember.getHealthCoverage().add(new HealthCoverageType());
		expectedMember.getHealthCoverage().get(0).setHealthCoverageDates(makeHealthCoverageDatesType(expectedHcBBD, expectedHcBED));

		// Initials will not have any data in EPS, therefore
		PolicyMemberDatePO epsPO = null; 

		PolicyMemberDatePO po = mapper.mapFFMToEPS(expectedMember, epsPO);


		assertEquals("PolicyMemberStartDate", expectedHcBBD, po.getPolicyMemberStartDate());
		assertEquals("PolicyMemberEndDate", expectedHcBED, po.getPolicyMemberEndDate());
		assertTrue("isPolicyMemberChanged", po.isPolicyMemberChanged());
	}
	
	@Test
	public void test_mapFFMToEPS_NoChange() {

		Long memId = Long.valueOf("1111");
		String name = "SON";
		boolean isSubscriber = false;
		MemberType expectedMember = makeMemberType(memId, name, isSubscriber);

		LocalDate expectedHcBBD = APR_1;
		LocalDate expectedHcBED = MAY_31;
		boolean expectedIsChanged = false;
		
		expectedMember.getHealthCoverage().add(new HealthCoverageType());
		expectedMember.getHealthCoverage().get(0).setHealthCoverageDates(makeHealthCoverageDatesType(expectedHcBBD, expectedHcBED));

		PolicyMemberDatePO epsPO = new PolicyMemberDatePO();
		epsPO.setPolicyMemberStartDate(expectedHcBBD);
		epsPO.setPolicyMemberEndDate(expectedHcBED);
		

		PolicyMemberDatePO po = mapper.mapFFMToEPS(expectedMember, epsPO);

		assertEquals("PolicyMemberStartDate", expectedHcBBD, po.getPolicyMemberStartDate());
		assertEquals("PolicyMemberEndDate", expectedHcBED, po.getPolicyMemberEndDate());
		assertEquals("isPolicyMemberChanged", expectedIsChanged, po.isPolicyMemberChanged());
	}
	
	@Test
	public void test_mapFFMToEPS_ChangeHcBBD() {

		Long memId = Long.valueOf("1111");
		String name = "SON";
		boolean isSubscriber = false;
		MemberType expectedMember = makeMemberType(memId, name, isSubscriber);

		LocalDate expectedHcBBD = APR_15;
		LocalDate expectedHcBED = MAY_31;
		boolean expectedIsChanged = true;
		
		expectedMember.getHealthCoverage().add(new HealthCoverageType());
		expectedMember.getHealthCoverage().get(0).setHealthCoverageDates(makeHealthCoverageDatesType(expectedHcBBD, expectedHcBED));

		PolicyMemberDatePO epsPO = new PolicyMemberDatePO();
		epsPO.setPolicyMemberStartDate(APR_1);
		epsPO.setPolicyMemberEndDate(expectedHcBED);
		

		PolicyMemberDatePO po = mapper.mapFFMToEPS(expectedMember, epsPO);

		assertEquals("PolicyMemberStartDate", expectedHcBBD, po.getPolicyMemberStartDate());
		assertEquals("PolicyMemberEndDate", expectedHcBED, po.getPolicyMemberEndDate());
		assertEquals("isPolicyMemberChanged", expectedIsChanged, po.isPolicyMemberChanged());
	}
	
	@Test
	public void test_mapFFMToEPS_ChangeHcBED_null() {

		Long memId = Long.valueOf("1111");
		String name = "SON";
		boolean isSubscriber = false;
		MemberType expectedMember = makeMemberType(memId, name, isSubscriber);

		LocalDate expectedHcBBD = APR_1;
		LocalDate expectedHcBED = null;
		boolean expectedIsChanged = true;
		
		expectedMember.getHealthCoverage().add(new HealthCoverageType());
		expectedMember.getHealthCoverage().get(0).setHealthCoverageDates(makeHealthCoverageDatesType(expectedHcBBD, expectedHcBED));

		PolicyMemberDatePO epsPO = new PolicyMemberDatePO();
		epsPO.setPolicyMemberStartDate(APR_1);
		epsPO.setPolicyMemberEndDate(MAY_31);
		
		PolicyMemberDatePO po = mapper.mapFFMToEPS(expectedMember, epsPO);

		assertEquals("PolicyMemberStartDate", expectedHcBBD, po.getPolicyMemberStartDate());
		assertEquals("PolicyMemberEndDate", expectedHcBED, po.getPolicyMemberEndDate());
		assertEquals("isPolicyMemberChanged", expectedIsChanged, po.isPolicyMemberChanged());
	}
	
	@Test
	public void test_mapFFMToEPS_ChangeHcBED() {

		Long memId = Long.valueOf("1111");
		String name = "SON";
		boolean isSubscriber = false;
		MemberType expectedMember = makeMemberType(memId, name, isSubscriber);

		LocalDate expectedHcBBD = null;
		LocalDate expectedHcBED = MAR_31;
		boolean expectedIsChanged = true;
		
		expectedMember.getHealthCoverage().add(new HealthCoverageType());
		expectedMember.getHealthCoverage().get(0).setHealthCoverageDates(makeHealthCoverageDatesType(expectedHcBBD, expectedHcBED));

		PolicyMemberDatePO epsPO = new PolicyMemberDatePO();
		epsPO.setPolicyMemberStartDate(null);
		epsPO.setPolicyMemberEndDate(JUN_30);
		
		PolicyMemberDatePO po = mapper.mapFFMToEPS(expectedMember, epsPO);

		assertEquals("PolicyMemberStartDate", expectedHcBBD, po.getPolicyMemberStartDate());
		assertEquals("PolicyMemberEndDate", expectedHcBED, po.getPolicyMemberEndDate());
		assertEquals("isPolicyMemberChanged", expectedIsChanged, po.isPolicyMemberChanged());
	}
	
	@Test
	public void test_mapFFMToEPS_Empty_inbound() {

		Long memId = Long.valueOf("1111");
		String name = "SON";
		boolean isSubscriber = false;
		MemberType expectedMember = makeMemberType(memId, name, isSubscriber);

		PolicyMemberDatePO expectedPO = null;
		
		expectedMember.getHealthCoverage().add(new HealthCoverageType());
		expectedMember.getHealthCoverage().get(0).setHealthCoverageDates(new HealthCoverageDatesType());

		PolicyMemberDatePO epsPO = new PolicyMemberDatePO();
		epsPO.setPolicyMemberStartDate(APR_1);
		epsPO.setPolicyMemberEndDate(MAY_31);
		
		PolicyMemberDatePO po = mapper.mapFFMToEPS(expectedMember, epsPO);

		assertEquals("PolicyMemberDatePO", expectedPO, po);
		
	}



}
