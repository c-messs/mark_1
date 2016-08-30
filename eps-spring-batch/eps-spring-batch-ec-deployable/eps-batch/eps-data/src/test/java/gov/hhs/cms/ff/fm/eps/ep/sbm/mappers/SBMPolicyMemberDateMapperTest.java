package gov.hhs.cms.ff.fm.eps.ep.sbm.mappers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import gov.cms.dsh.sbmi.PolicyMemberType;
import gov.cms.dsh.sbmi.PolicyMemberType.MemberDates;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmPolicyMemberDatePO;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;

public class SBMPolicyMemberDateMapperTest extends SBMBaseMapperTest {

	private SbmPolicyMemberDateMapper mapper = new SbmPolicyMemberDateMapper();


	@Test
	public void test_mapSbmToStaging() {

		LocalDate expectedMemSD1 = JAN_1;
		LocalDate expectedMemED1 = JAN_15;

		LocalDate expectedMemSD2 = JAN_16;
		LocalDate expectedMemED2 = JAN_31;

		boolean expectedIsChanged = true;
		PolicyMemberType member = new PolicyMemberType();

		MemberDates memDates1 = new MemberDates();
		memDates1.setMemberStartDate(DateTimeUtil.getXMLGregorianCalendar(expectedMemSD1));
		memDates1.setMemberEndDate(DateTimeUtil.getXMLGregorianCalendar(expectedMemED1));

		MemberDates memDates2 = new MemberDates();
		memDates2.setMemberStartDate(DateTimeUtil.getXMLGregorianCalendar(expectedMemSD2));
		memDates2.setMemberEndDate(DateTimeUtil.getXMLGregorianCalendar(expectedMemED2));

		member.getMemberDates().add(memDates1);
		member.getMemberDates().add(memDates2);

		List<SbmPolicyMemberDatePO> epsPOList = new ArrayList<SbmPolicyMemberDatePO>();

		List<SbmPolicyMemberDatePO> actualList = mapper.mapSbmToStaging(member, epsPOList);

		SbmPolicyMemberDatePO actual1 = actualList.get(0);
		SbmPolicyMemberDatePO actual2 = actualList.get(1);

		assertEquals("1) PolicyMemberStartDate", expectedMemSD1, actual1.getPolicyMemberStartDate());
		assertEquals("1) PolicyMemberEndDate", expectedMemED1, actual1.getPolicyMemberEndDate());
		assertEquals("1) PolicyMember Changed", expectedIsChanged, actual1.isPolicyMemberChanged());

		assertEquals("2) PolicyMemberStartDate", expectedMemSD2, actual2.getPolicyMemberStartDate());
		assertEquals("2) PolicyMemberEndDate", expectedMemED2, actual2.getPolicyMemberEndDate());
		assertEquals("2) PolicyMember Changed", expectedIsChanged, actual2.isPolicyMemberChanged());
	}

	@Test
	public void test_mapSBMToStaging_NoChange() {

		LocalDate expectedMemSD1 = JAN_1;
		LocalDate expectedMemED1 = JAN_15;

		LocalDate expectedMemSD2 = JAN_16;
		LocalDate expectedMemED2 = JAN_31;

		boolean expectedIsChanged = false;
		PolicyMemberType member = new PolicyMemberType();

		MemberDates memDates1 = new MemberDates();
		memDates1.setMemberStartDate(DateTimeUtil.getXMLGregorianCalendar(expectedMemSD1));
		memDates1.setMemberEndDate(DateTimeUtil.getXMLGregorianCalendar(expectedMemED1));

		MemberDates memDates2 = new MemberDates();
		memDates2.setMemberStartDate(DateTimeUtil.getXMLGregorianCalendar(expectedMemSD2));
		memDates2.setMemberEndDate(DateTimeUtil.getXMLGregorianCalendar(expectedMemED2));

		member.getMemberDates().add(memDates1);
		member.getMemberDates().add(memDates2);

		List<SbmPolicyMemberDatePO> epsPOList = new ArrayList<SbmPolicyMemberDatePO>();

		SbmPolicyMemberDatePO epsPO1 = new SbmPolicyMemberDatePO();
		epsPO1.setPolicyMemberStartDate(expectedMemSD1);
		epsPO1.setPolicyMemberEndDate(expectedMemED1);

		SbmPolicyMemberDatePO epsPO2 = new SbmPolicyMemberDatePO();
		epsPO2.setPolicyMemberStartDate(expectedMemSD2);
		epsPO2.setPolicyMemberEndDate(expectedMemED2);

		epsPOList.add(epsPO1);
		epsPOList.add(epsPO2);

		List<SbmPolicyMemberDatePO> actualList = mapper.mapSbmToStaging(member, epsPOList);

		SbmPolicyMemberDatePO actual1 = actualList.get(0);
		SbmPolicyMemberDatePO actual2 = actualList.get(1);

		assertEquals("1) PolicyMemberStartDate", expectedMemSD1, actual1.getPolicyMemberStartDate());
		assertEquals("1) PolicyMemberEndDate", expectedMemED1, actual1.getPolicyMemberEndDate());
		assertEquals("1) PolicyMember Changed", expectedIsChanged, actual1.isPolicyMemberChanged());

		assertEquals("2) PolicyMemberStartDate", expectedMemSD2, actual2.getPolicyMemberStartDate());
		assertEquals("2) PolicyMemberEndDate", expectedMemED2, actual2.getPolicyMemberEndDate());
		assertEquals("2) PolicyMember Changed", expectedIsChanged, actual2.isPolicyMemberChanged());
	}

	@Test
	public void test_mapSBMToStaging_Null_Inbound() {

		int expectedMemDatesListSize = 0;

		PolicyMemberType member = new PolicyMemberType();

		List<SbmPolicyMemberDatePO> epsPOList = new ArrayList<SbmPolicyMemberDatePO>();

		SbmPolicyMemberDatePO epsPO1 = new SbmPolicyMemberDatePO();
		epsPO1.setPolicyMemberStartDate(JAN_1);
		epsPO1.setPolicyMemberStartDate(JAN_15);

		SbmPolicyMemberDatePO epsPO2 = new SbmPolicyMemberDatePO();
		epsPO2.setPolicyMemberStartDate(JAN_16);
		epsPO2.setPolicyMemberStartDate(JAN_31);

		epsPOList.add(epsPO1);
		epsPOList.add(epsPO2);

		List<SbmPolicyMemberDatePO> actualList = mapper.mapSbmToStaging(member, epsPOList);

		assertEquals("MemberDates list size", expectedMemDatesListSize, actualList.size());

	}

}
