package gov.hhs.cms.ff.fm.eps.ep.validation;

import gov.cms.dsh.bem.AdditionalInfoType;
import gov.cms.dsh.bem.MemberType;
import gov.hhs.cms.ff.fm.eps.ep.util.EpsDateUtils;
import gov.hhs.cms.ff.fm.eps.ep.validation.impl.FinancialValidatorImpl;

import java.math.BigDecimal;
import java.util.Map;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * @author j.radziewski
 *
 * Tests FinancialValidator.
 */
@RunWith(JUnit4.class)
public class FinancialValidatorImplTest extends BaseValidationTest {

	private FinancialValidatorImpl financialValidator;

	@Before
	public void setup() throws Exception {

		financialValidator = new FinancialValidatorImpl();
	}

	@Test
	public void test_createEpsPremium() {

		DateTime sysSelESD = MAR_15;

		DateTime esd = JAN_1;
		DateTime eed = null;
		BigDecimal aptc = new BigDecimal("11.11");
		BigDecimal csr = new BigDecimal("22.22");
		BigDecimal tpa = new BigDecimal("33.33");
		BigDecimal tira = new BigDecimal("44.44");
		String ra = "RA-NEW";

		// Expected data after replacement, Record 1
		DateTime expectedESD = sysSelESD;
		DateTime expectedEED = null;
		BigDecimal expectedAPTC = aptc;
		BigDecimal expectedCSR = csr;
		BigDecimal expectedTPA = tpa;
		BigDecimal expectedTIRA = tira;
		String expectedRA = ra;

		MemberType inboundSubscriber = makeMemberType("3333");
		
		AdditionalInfoType ait = new AdditionalInfoType();
		
		ait.setEffectiveStartDate(EpsDateUtils.getXMLGregorianCalendar(esd));
		ait.setEffectiveEndDate(EpsDateUtils.getXMLGregorianCalendar(eed));
		
		ait.setTotalPremiumAmount(tpa);
		ait.setTotalIndividualResponsibilityAmount(tira);
		ait.setAPTCAmount(aptc); 
		ait.setCSRAmount(csr);
		ait.setRatingArea(ra); 
		
		inboundSubscriber.getAdditionalInfo().add(ait);
		
		AdditionalInfoType epsPremium = (AdditionalInfoType) ReflectionTestUtils.invokeMethod(financialValidator, "createEpsPremium", sysSelESD, inboundSubscriber);

		assertEquals("ESD", expectedESD, EpsDateUtils.getDateTimeFromXmlGC(epsPremium.getEffectiveStartDate()));
		assertEquals("EED", expectedEED, EpsDateUtils.getDateTimeFromXmlGC(epsPremium.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC, epsPremium.getAPTCAmount());
		assertEquals("CSR", expectedCSR, epsPremium.getCSRAmount());
		assertEquals("TPA", expectedTPA, epsPremium.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA, epsPremium.getTotalIndividualResponsibilityAmount());
		assertEquals("RA", expectedRA, epsPremium.getRatingArea());
	}
	
	@Test
	public void test_processInboundPremiums_singleSubscriber() {

		DateTime esd = JAN_1;
		DateTime eed = null;
		BigDecimal aptc = new BigDecimal("11.11");
		BigDecimal csr = new BigDecimal("22.22");
		BigDecimal tpa = new BigDecimal("33.33");
		BigDecimal tira = new BigDecimal("44.44");
		String ra = "RA-NEW";

		// Expected data after replacement, Record 1
		DateTime expectedESD = esd;
		DateTime expectedEED = null;
		BigDecimal expectedAPTC = aptc;
		BigDecimal expectedCSR = csr;
		BigDecimal expectedTPA = tpa;
		BigDecimal expectedTIRA = tira;
		String expectedRA = ra;

		MemberType inboundSubscriber = makeMemberType("3333");
		
		AdditionalInfoType ait = new AdditionalInfoType();
		
		ait.setEffectiveStartDate(EpsDateUtils.getXMLGregorianCalendar(esd));
		ait.setEffectiveEndDate(EpsDateUtils.getXMLGregorianCalendar(eed));
		
		ait.setTotalPremiumAmount(tpa);
		ait.setTotalIndividualResponsibilityAmount(tira);
		ait.setAPTCAmount(aptc); 
		ait.setCSRAmount(csr);
		ait.setRatingArea(ra); 
		
		inboundSubscriber.getAdditionalInfo().add(ait);
		
		Map<DateTime, AdditionalInfoType> epsPremiums = financialValidator.processInboundPremiums(inboundSubscriber);
		
		AdditionalInfoType epsPremium = (AdditionalInfoType)epsPremiums.get(esd);

		assertEquals("ESD", expectedESD, EpsDateUtils.getDateTimeFromXmlGC(epsPremium.getEffectiveStartDate()));
		assertEquals("EED", expectedEED, EpsDateUtils.getDateTimeFromXmlGC(epsPremium.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC, epsPremium.getAPTCAmount());
		assertEquals("CSR", expectedCSR, epsPremium.getCSRAmount());
		assertEquals("TPA", expectedTPA, epsPremium.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA, epsPremium.getTotalIndividualResponsibilityAmount());
		assertEquals("RA", expectedRA, epsPremium.getRatingArea());
	}
	
	@Test
	public void test_processInboundPremiums_singleSubscriber_No_Premiums() {

		DateTime esd = null;
		DateTime eed = null;
		BigDecimal aptc = new BigDecimal("11.11");
		BigDecimal csr = new BigDecimal("22.22");
		BigDecimal tpa = new BigDecimal("33.33");
		BigDecimal tira = new BigDecimal("44.44");
		String ra = "RA-NEW";

		MemberType inboundSubscriber = makeMemberType("3333");
		
		AdditionalInfoType ait = new AdditionalInfoType();
		
		ait.setEffectiveStartDate(EpsDateUtils.getXMLGregorianCalendar(esd));
		ait.setEffectiveEndDate(EpsDateUtils.getXMLGregorianCalendar(eed));
		
		ait.setTotalPremiumAmount(tpa);
		ait.setTotalIndividualResponsibilityAmount(tira);
		ait.setAPTCAmount(aptc); 
		ait.setCSRAmount(csr);
		ait.setRatingArea(ra); 
		
		inboundSubscriber.getAdditionalInfo().add(ait);
		
		Map<DateTime, AdditionalInfoType> epsPremiums = financialValidator.processInboundPremiums(inboundSubscriber);
		
		assertTrue("No Premium Records", epsPremiums.isEmpty());
		
		AdditionalInfoType epsPremium = (AdditionalInfoType)epsPremiums.get(esd);
		
		assertNull("No Premium Records", epsPremium);
	}

	@Test
	public void test_updateEpsPremium() {

		DateTime sysSelESD = MAR_15;

		DateTime esd = JAN_1;
		DateTime eed = DEC_31;
		BigDecimal aptc = new BigDecimal("11.11");
		BigDecimal csr = new BigDecimal("22.22");
		BigDecimal tpa = new BigDecimal("33.33");
		BigDecimal tira = new BigDecimal("44.44");
		String ra = "RA-NEW";

		// Expected data after replacement, Record 1
		DateTime expectedESD = sysSelESD;
		DateTime expectedEED = DEC_31;
		BigDecimal expectedAPTC = aptc;
		BigDecimal expectedCSR = csr;
		BigDecimal expectedTPA = tpa;
		BigDecimal expectedTIRA = tira;
		String expectedRA = ra;

		MemberType inboundSubscriber = makeMemberType("3333");
		
		AdditionalInfoType ait = new AdditionalInfoType();
		
		ait.setEffectiveStartDate(EpsDateUtils.getXMLGregorianCalendar(esd));
		ait.setEffectiveEndDate(EpsDateUtils.getXMLGregorianCalendar(eed));
		
		ait.setTotalPremiumAmount(tpa);
		ait.setTotalIndividualResponsibilityAmount(tira);
		ait.setAPTCAmount(aptc); 
		ait.setCSRAmount(csr);
		ait.setRatingArea(ra); 
		
		inboundSubscriber.getAdditionalInfo().add(ait);
		
		AdditionalInfoType epsPremium = makeAdditionalInfoType(JAN_1, DEC_31);
		epsPremium.setAPTCAmount(new BigDecimal("1"));
		epsPremium.setCSRAmount(new BigDecimal("2"));
		epsPremium.setTotalPremiumAmount(new BigDecimal("3"));
		epsPremium.setTotalIndividualResponsibilityAmount(new BigDecimal("4"));
		epsPremium.setRatingArea("RA-OLD");

		ReflectionTestUtils.invokeMethod(financialValidator, "updateEpsPremium", sysSelESD, inboundSubscriber, epsPremium);

		assertEquals("ESD", expectedESD, EpsDateUtils.getDateTimeFromXmlGC(epsPremium.getEffectiveStartDate()));
		assertEquals("EED", expectedEED, EpsDateUtils.getDateTimeFromXmlGC(epsPremium.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC, epsPremium.getAPTCAmount());
		assertEquals("CSR", expectedCSR, epsPremium.getCSRAmount());
		assertEquals("TPA", expectedTPA, epsPremium.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA, epsPremium.getTotalIndividualResponsibilityAmount());
		assertEquals("RA", expectedRA, epsPremium.getRatingArea());
	}
	
	@Test
	public void test_updateEpsPremium_EED_null() {

		DateTime sysSelESD = MAR_15;

		DateTime esd = JAN_1;
		DateTime eed = null;
		BigDecimal aptc = new BigDecimal("11.11");
		BigDecimal csr = new BigDecimal("22.22");
		BigDecimal tpa = new BigDecimal("33.33");
		BigDecimal tira = new BigDecimal("44.44");
		String ra = "RA-NEW";

		// Expected data after replacement, Record 1
		DateTime expectedESD = sysSelESD;
		DateTime expectedEED = null;
		BigDecimal expectedAPTC = aptc;
		BigDecimal expectedCSR = csr;
		BigDecimal expectedTPA = tpa;
		BigDecimal expectedTIRA = tira;
		String expectedRA = ra;

		MemberType inboundSubscriber = makeMemberType("3333");
		
		AdditionalInfoType ait = new AdditionalInfoType();
		
		ait.setEffectiveStartDate(EpsDateUtils.getXMLGregorianCalendar(esd));
		ait.setEffectiveEndDate(EpsDateUtils.getXMLGregorianCalendar(eed));
		
		ait.setTotalPremiumAmount(tpa);
		ait.setTotalIndividualResponsibilityAmount(tira);
		ait.setAPTCAmount(aptc); 
		ait.setCSRAmount(csr);
		ait.setRatingArea(ra); 
		
		inboundSubscriber.getAdditionalInfo().add(ait);
		
		AdditionalInfoType epsPremium = makeAdditionalInfoType(JAN_1, null);
		epsPremium.setAPTCAmount(new BigDecimal("1"));
		epsPremium.setCSRAmount(new BigDecimal("2"));
		epsPremium.setTotalPremiumAmount(new BigDecimal("3"));
		epsPremium.setTotalIndividualResponsibilityAmount(new BigDecimal("4"));
		epsPremium.setRatingArea("RA-OLD");

		ReflectionTestUtils.invokeMethod(financialValidator, "updateEpsPremium", sysSelESD, inboundSubscriber, epsPremium);

		assertEquals("ESD", expectedESD, EpsDateUtils.getDateTimeFromXmlGC(epsPremium.getEffectiveStartDate()));
		assertEquals("EED", expectedEED, EpsDateUtils.getDateTimeFromXmlGC(epsPremium.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC, epsPremium.getAPTCAmount());
		assertEquals("CSR", expectedCSR, epsPremium.getCSRAmount());
		assertEquals("TPA", expectedTPA, epsPremium.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA, epsPremium.getTotalIndividualResponsibilityAmount());
		assertEquals("RA", expectedRA, epsPremium.getRatingArea());
	}
	
	@Test
	public void test_updateEpsPremium_NonKFE_CarryOver() {

		DateTime sysSelESD = MAR_15;

		DateTime esd = JAN_1;
		DateTime eed = null;
		BigDecimal aptc = new BigDecimal("11.11");
		BigDecimal csr = new BigDecimal("22.22");
		BigDecimal tpa = new BigDecimal("33.33");
		BigDecimal tira = new BigDecimal("44.44");
		String ra = "RA-NEW";

		// Expected data after replacement, Record 1
		DateTime expectedESD = sysSelESD;
		DateTime expectedEED = null;
		BigDecimal expectedAPTC = aptc;
		BigDecimal expectedCSR = csr;
		BigDecimal expectedTPA = tpa;
		BigDecimal expectedTIRA = tira;
		String expectedRA = ra;

		MemberType inboundSubscriber = makeMemberType("3333");
		
		AdditionalInfoType ait = new AdditionalInfoType();
		
		ait.setEffectiveStartDate(EpsDateUtils.getXMLGregorianCalendar(esd));
		ait.setEffectiveEndDate(EpsDateUtils.getXMLGregorianCalendar(eed));
		
		ait.setTotalPremiumAmount(tpa);
		ait.setTotalIndividualResponsibilityAmount(tira);
		ait.setAPTCAmount(aptc); 
		ait.setCSRAmount(csr);
		ait.setRatingArea(ra); 
		
		inboundSubscriber.getAdditionalInfo().add(ait);
		
		AdditionalInfoType epsPremium = makeAdditionalInfoType(JAN_1, null);
		epsPremium.setAPTCAmount(new BigDecimal("1"));
		epsPremium.setCSRAmount(new BigDecimal("2"));
		epsPremium.setTotalPremiumAmount(new BigDecimal("3"));
		epsPremium.setTotalIndividualResponsibilityAmount(new BigDecimal("4"));
		epsPremium.setRatingArea("RA-OLD");

		ReflectionTestUtils.invokeMethod(financialValidator, "updateEpsPremium", sysSelESD, inboundSubscriber, epsPremium);

		assertEquals("ESD", expectedESD, EpsDateUtils.getDateTimeFromXmlGC(epsPremium.getEffectiveStartDate()));
		assertEquals("EED", expectedEED, EpsDateUtils.getDateTimeFromXmlGC(epsPremium.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC, epsPremium.getAPTCAmount());
		assertEquals("CSR", expectedCSR, epsPremium.getCSRAmount());
		assertEquals("TPA", expectedTPA, epsPremium.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA, epsPremium.getTotalIndividualResponsibilityAmount());
		assertEquals("RA", expectedRA, epsPremium.getRatingArea());
	}
	
	@Test
	public void test_updateEpsPremium_KFE_Null_Replace() {

		DateTime sysSelESD = MAR_15;

		DateTime esd = JAN_1;
		DateTime eed = null;
		BigDecimal aptc = new BigDecimal("11.11");
		BigDecimal csr = new BigDecimal("22.22");
		BigDecimal tpa = new BigDecimal("33.33");
		String ra = "RA-NEW";

		// Expected data after replacement, Record 1
		DateTime expectedESD = sysSelESD;
		DateTime expectedEED = null;
		BigDecimal expectedAPTC = aptc;
		BigDecimal expectedCSR = csr;
		BigDecimal expectedTPA = tpa;
		BigDecimal expectedTIRA = null;
		String expectedRA = ra;

		MemberType inboundSubscriber = makeMemberType("3333");
		
		AdditionalInfoType ait = new AdditionalInfoType();
		
		ait.setEffectiveStartDate(EpsDateUtils.getXMLGregorianCalendar(esd));
		ait.setEffectiveEndDate(EpsDateUtils.getXMLGregorianCalendar(eed));
		
		ait.setAPTCAmount(aptc); 
		ait.setTotalPremiumAmount(tpa);
		ait.setCSRAmount(csr);
		ait.setRatingArea(ra); 
		
		inboundSubscriber.getAdditionalInfo().add(ait);
		
		AdditionalInfoType epsPremium = makeAdditionalInfoType(JAN_1, null);
		epsPremium.setAPTCAmount(new BigDecimal("1"));
		epsPremium.setCSRAmount(new BigDecimal("2"));
		epsPremium.setTotalPremiumAmount(new BigDecimal("3"));
		epsPremium.setTotalIndividualResponsibilityAmount(new BigDecimal("4"));
		epsPremium.setRatingArea("RA-OLD");

		ReflectionTestUtils.invokeMethod(financialValidator, "updateEpsPremium", sysSelESD, inboundSubscriber, epsPremium);

		assertEquals("ESD", expectedESD, EpsDateUtils.getDateTimeFromXmlGC(epsPremium.getEffectiveStartDate()));
		assertEquals("EED", expectedEED, EpsDateUtils.getDateTimeFromXmlGC(epsPremium.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC, epsPremium.getAPTCAmount());
		assertEquals("CSR", expectedCSR, epsPremium.getCSRAmount());
		assertEquals("TPA", expectedTPA, epsPremium.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA, epsPremium.getTotalIndividualResponsibilityAmount());
		assertEquals("RA", expectedRA, epsPremium.getRatingArea());
	}
	
	@Test
	public void test_updateEpsPremium_Null_Amounts() {

		DateTime sysSelESD = MAR_15;

		DateTime esd = JAN_1;
		DateTime eed = null;
		BigDecimal aptc = null;
		BigDecimal csr = null;
		BigDecimal tpa = null;
		String ra = null;

		// Expected data after replacement, Record 1
		DateTime expectedESD = sysSelESD;
		DateTime expectedEED = null;
		BigDecimal expectedAPTC = aptc;
		BigDecimal expectedCSR = csr;
		BigDecimal expectedTPA = tpa;
		BigDecimal expectedTIRA = null;
		String expectedRA = "RA-OLD";

		MemberType inboundSubscriber = makeMemberType("3333");
		
		AdditionalInfoType ait = new AdditionalInfoType();
		
		ait.setEffectiveStartDate(EpsDateUtils.getXMLGregorianCalendar(esd));
		ait.setEffectiveEndDate(EpsDateUtils.getXMLGregorianCalendar(eed));
		
		ait.setAPTCAmount(aptc); 
		ait.setTotalPremiumAmount(tpa);
		ait.setCSRAmount(csr);
		ait.setRatingArea(ra); 
		
		inboundSubscriber.getAdditionalInfo().add(ait);
		
		AdditionalInfoType epsPremium = makeAdditionalInfoType(JAN_1, null);
		epsPremium.setAPTCAmount(new BigDecimal("1"));
		epsPremium.setCSRAmount(new BigDecimal("2"));
		epsPremium.setTotalPremiumAmount(new BigDecimal("3"));
		epsPremium.setTotalIndividualResponsibilityAmount(new BigDecimal("4"));
		epsPremium.setRatingArea("RA-OLD");

		ReflectionTestUtils.invokeMethod(financialValidator, "updateEpsPremium", sysSelESD, inboundSubscriber, epsPremium);

		assertEquals("ESD", expectedESD, EpsDateUtils.getDateTimeFromXmlGC(epsPremium.getEffectiveStartDate()));
		assertEquals("EED", expectedEED, EpsDateUtils.getDateTimeFromXmlGC(epsPremium.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC, epsPremium.getAPTCAmount());
		assertEquals("CSR", expectedCSR, epsPremium.getCSRAmount());
		assertEquals("TPA", expectedTPA, epsPremium.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA, epsPremium.getTotalIndividualResponsibilityAmount());
		assertEquals("RA", expectedRA, epsPremium.getRatingArea());
	}
	
	@Test
	public void test_updateProratedAmounts() {

		BigDecimal proratedAptc = new BigDecimal("11.11");
		BigDecimal proratedCsr = new BigDecimal("22.22");
		BigDecimal proratedTpa = new BigDecimal("33.33");
		BigDecimal proratedTira = new BigDecimal("0.00");

		// Expected data after replacement, Record 1
		BigDecimal expectedProratedAPTC = proratedAptc;
		BigDecimal expectedProratedCSR = proratedCsr;
		BigDecimal expectedProratedTPA = proratedTpa;
		BigDecimal expectedProratedTIRA = proratedTira;

		AdditionalInfoType ait = new AdditionalInfoType();
		
		ait.setProratedAppliedAPTCAmount(proratedAptc); 
		ait.setProratedMonthlyPremiumAmount(proratedTpa);
		ait.setProratedCSRAmount(proratedCsr);
		ait.setProratedIndividualResponsibleAmount(proratedTira); 
		
		AdditionalInfoType epsPremium = makeAdditionalInfoType(JAN_1, null);
		epsPremium.setAPTCAmount(new BigDecimal("1"));
		epsPremium.setCSRAmount(new BigDecimal("2"));
		epsPremium.setTotalPremiumAmount(new BigDecimal("3"));
		epsPremium.setTotalIndividualResponsibilityAmount(new BigDecimal("4"));

		ReflectionTestUtils.invokeMethod(financialValidator, "updateProratedAmounts", ait, epsPremium);

		assertEquals("Prorated APTC", expectedProratedAPTC, epsPremium.getProratedAppliedAPTCAmount());
		assertEquals("Prorated CSR", expectedProratedCSR, epsPremium.getProratedCSRAmount());
		assertEquals("Prorated TPA", expectedProratedTPA, epsPremium.getProratedMonthlyPremiumAmount());
		assertEquals("Prorated TIRA", expectedProratedTIRA, epsPremium.getProratedIndividualResponsibleAmount());
	}

	@Test
	public void test_isEffectiveEndDatePresent_true() {

		boolean expected = true;

		DateTime esd = JAN_1;
		DateTime eed = DEC_31;
		BigDecimal anyAmt = new BigDecimal("9.99");

		MemberType inboundSubscriber = makeMemberType("3333");
		
		AdditionalInfoType ait = new AdditionalInfoType();
		
		ait.setEffectiveStartDate(EpsDateUtils.getXMLGregorianCalendar(esd));
		ait.setEffectiveEndDate(EpsDateUtils.getXMLGregorianCalendar(eed));
		ait.setTotalPremiumAmount(anyAmt);
		
		inboundSubscriber.getAdditionalInfo().add(ait);
		
		Boolean actual = (Boolean) ReflectionTestUtils.invokeMethod(financialValidator, "isEffectiveEndDatePresent", inboundSubscriber);
		assertEquals("isEffectiveEndDatePresent", expected, actual.booleanValue());
	}

	@Test
	public void test_isEffectiveEndDatePresent_false() {

		boolean expected = false;

		DateTime esd = MAR_15;
		DateTime eed = null;
		BigDecimal anyAmt = new BigDecimal("9.99");

		MemberType inboundSubscriber = makeMemberType("3333");
		
		AdditionalInfoType ait = new AdditionalInfoType();
		
		ait.setEffectiveStartDate(EpsDateUtils.getXMLGregorianCalendar(esd));
		ait.setEffectiveEndDate(EpsDateUtils.getXMLGregorianCalendar(eed));
		ait.setTotalPremiumAmount(anyAmt);
		
		inboundSubscriber.getAdditionalInfo().add(ait);
		
		Boolean actual = (Boolean) ReflectionTestUtils.invokeMethod(financialValidator, "isEffectiveEndDatePresent", inboundSubscriber);
		assertEquals("isEffectiveEndDatePresent", expected, actual.booleanValue());
	}
	
	@Test
	public void test_determineSystemSelectedEffectiveStartDate() {

		DateTime expectedESD = JAN_1;
		BigDecimal anyAmt1 = new BigDecimal("0.01");

		MemberType member1 = makeSubscriberMaintenance("1111");
		
		AdditionalInfoType ait = new AdditionalInfoType();
		
		ait.setEffectiveStartDate(EpsDateUtils.getXMLGregorianCalendar(JAN_1));
		ait.setEffectiveEndDate(null);
		ait.setAPTCAmount(anyAmt1); 
		ait.setCSRAmount(anyAmt1);
		
		// Set a higher non-key financial element.
		ait.setRatingArea("ANY TEXT");
		
		member1.getAdditionalInfo().add(ait);
		
		DateTime actualESD = financialValidator.determineSystemSelectedEffectiveStartDate(member1);
		assertEquals("System Selected EffectiveStartDate", expectedESD, actualESD);
	}

	@Test
	public void test_determineSystemSelectedEffectiveStartDate_Null() {

		DateTime expectedESD = null;

		MemberType member1 = makeSubscriberMaintenance("1111");
		// Set no key financial elements.
		AdditionalInfoType ait = new AdditionalInfoType();
		
		ait.setEffectiveStartDate(EpsDateUtils.getXMLGregorianCalendar(MAR_15));
		ait.setEffectiveEndDate(null);
		
		ait.setRatingArea("ANY TEXT");
		
		member1.getAdditionalInfo().add(ait);

		DateTime actualESD = financialValidator.determineSystemSelectedEffectiveStartDate(member1);
		assertEquals("System Selected EffectiveStartDate", expectedESD, actualESD);
	}

}
