package gov.hhs.cms.ff.fm.eps.ep.sbm.validation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang.RandomStringUtils;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Test;

import gov.cms.dsh.sbmi.PolicyType.FinancialInformation;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMErrorWarningCode;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMCache;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SbmErrWarningLogDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.validation.impl.SbmValidationUtil;
import junit.framework.TestCase;

/**
 * Test class for SbmValidationUtil
 * 
 * @author girish.padmanabhan
 */
//@RunWith(JUnit4.class)
public class SbmValidationUtilTest extends TestCase {

	protected final Calendar CAL_BASE = Calendar.getInstance();

	protected final DateTime JAN_1 = new DateTime(CAL_BASE.get(Calendar.YEAR), 1, 1, 0, 0);
	protected final DateTime JAN_15 = new DateTime(CAL_BASE.get(Calendar.YEAR), 1, 15, 0, 0);
	protected final DateTime JAN_31 = new DateTime(CAL_BASE.get(Calendar.YEAR), 1, 31, 0, 0);
	protected final DateTime FEB_1 = new DateTime(CAL_BASE.get(Calendar.YEAR), 2, 1, 0, 0);
	protected final DateTime FEB_28 = new DateTime(CAL_BASE.get(Calendar.YEAR), 2, 28, 0, 0);
	protected final DateTime MAR_1 = new DateTime(CAL_BASE.get(Calendar.YEAR), 3, 1, 0, 0);
	protected final DateTime MAR_31 = new DateTime(CAL_BASE.get(Calendar.YEAR), 3, 31, 0, 0);
	
	private DatatypeFactory dfInstance = null;

	@Override
	public void setUp() throws Exception {
		dfInstance = DatatypeFactory.newInstance();
	}
	
	@Test
	public void testSortFinancialInfo_EmptyList() {

		List<FinancialInformation> financialInfoList = new ArrayList<FinancialInformation>();
		SbmValidationUtil.sortFinancialInfo(financialInfoList);
		assertNotNull("FinancialInfo", financialInfoList);	
	}

	@Test
	public void testSortFinancialInfos() {

		List<FinancialInformation> financialInfoList = new ArrayList<FinancialInformation>();

		BigDecimal tpaJan = BigDecimal.valueOf(11.11);
		BigDecimal tiraJan = BigDecimal.valueOf(22.22);

		FinancialInformation fitJan = new FinancialInformation();
		fitJan.setMonthlyTotalPremiumAmount(tpaJan);
		fitJan.setMonthlyTotalIndividualResponsibilityAmount(tiraJan);
		fitJan.setFinancialEffectiveStartDate(getXMLGregorianCalendar(JAN_1));
		fitJan.setFinancialEffectiveEndDate(getXMLGregorianCalendar(JAN_31));

		BigDecimal tpaFeb = BigDecimal.valueOf(10);
		BigDecimal tiraFeb = BigDecimal.valueOf(5);
		
		FinancialInformation fitFeb = new FinancialInformation();
		fitFeb.setMonthlyTotalPremiumAmount(tpaFeb);
		fitFeb.setMonthlyTotalIndividualResponsibilityAmount(tiraFeb);
		fitFeb.setFinancialEffectiveStartDate(getXMLGregorianCalendar(FEB_1));
		fitFeb.setFinancialEffectiveEndDate(getXMLGregorianCalendar(FEB_1.dayOfMonth().withMaximumValue()));
		
		financialInfoList.add(fitFeb);
		financialInfoList.add(fitJan);

		SbmValidationUtil.sortFinancialInfo(financialInfoList);

		assertNotNull("FinancialInformation", financialInfoList);	
		assertEquals("TotalPremiumAmount (element now 0)", tpaJan, financialInfoList.get(0).getMonthlyTotalPremiumAmount());
		assertEquals("TotalIndividualResponsibilityAmount (element now 1)", tiraFeb, financialInfoList.get(1).getMonthlyTotalIndividualResponsibilityAmount());
	}
	
	@Test
	public void testSortFinancialInfos_Overlap() {

		List<FinancialInformation> financialInfoList = new ArrayList<FinancialInformation>();

		BigDecimal tpaJan = BigDecimal.valueOf(11.11);
		BigDecimal tiraJan = BigDecimal.valueOf(22.22);

		FinancialInformation fitJan = new FinancialInformation();
		fitJan.setMonthlyTotalPremiumAmount(tpaJan);
		fitJan.setMonthlyTotalIndividualResponsibilityAmount(tiraJan);
		fitJan.setFinancialEffectiveStartDate(getXMLGregorianCalendar(JAN_1));
		fitJan.setFinancialEffectiveEndDate(getXMLGregorianCalendar(FEB_28));

		BigDecimal tpaFeb = BigDecimal.valueOf(10);
		BigDecimal tiraFeb = BigDecimal.valueOf(5);
		
		FinancialInformation fitFeb = new FinancialInformation();
		fitFeb.setMonthlyTotalPremiumAmount(tpaFeb);
		fitFeb.setMonthlyTotalIndividualResponsibilityAmount(tiraFeb);
		fitFeb.setFinancialEffectiveStartDate(getXMLGregorianCalendar(FEB_1));
		fitFeb.setFinancialEffectiveEndDate(getXMLGregorianCalendar(FEB_1.dayOfMonth().withMaximumValue()));
		
		financialInfoList.add(fitFeb);
		financialInfoList.add(fitJan);

		SbmValidationUtil.sortFinancialInfo(financialInfoList);

		assertNotNull("FinancialInformation", financialInfoList);	
		assertEquals("TotalPremiumAmount (element now 0)", tpaJan, financialInfoList.get(0).getMonthlyTotalPremiumAmount());
		assertEquals("TotalIndividualResponsibilityAmount (element now 1)", tiraFeb, financialInfoList.get(1).getMonthlyTotalIndividualResponsibilityAmount());
	}

	@Test
	public void testSortFinancialInfos_Gap() {

		List<FinancialInformation> financialInfoList = new ArrayList<FinancialInformation>();

		BigDecimal tpaJan = BigDecimal.valueOf(11.11);
		BigDecimal tiraJan = BigDecimal.valueOf(22.22);

		FinancialInformation fitJan = new FinancialInformation();
		fitJan.setMonthlyTotalPremiumAmount(tpaJan);
		fitJan.setMonthlyTotalIndividualResponsibilityAmount(tiraJan);
		fitJan.setFinancialEffectiveStartDate(getXMLGregorianCalendar(JAN_1));
		fitJan.setFinancialEffectiveEndDate(getXMLGregorianCalendar(JAN_15));

		BigDecimal tpaFeb = BigDecimal.valueOf(10);
		BigDecimal tiraFeb = BigDecimal.valueOf(5);
		
		FinancialInformation fitFeb = new FinancialInformation();
		fitFeb.setMonthlyTotalPremiumAmount(tpaFeb);
		fitFeb.setMonthlyTotalIndividualResponsibilityAmount(tiraFeb);
		fitFeb.setFinancialEffectiveStartDate(getXMLGregorianCalendar(FEB_1));
		fitFeb.setFinancialEffectiveEndDate(getXMLGregorianCalendar(FEB_1.dayOfMonth().withMaximumValue()));
		
		financialInfoList.add(fitFeb);
		financialInfoList.add(fitJan);

		SbmValidationUtil.sortFinancialInfo(financialInfoList);

		assertNotNull("FinancialInformation", financialInfoList);	
		assertEquals("TotalPremiumAmount (element now 0)", tpaJan, financialInfoList.get(0).getMonthlyTotalPremiumAmount());
		assertEquals("TotalIndividualResponsibilityAmount (element now 1)", tiraFeb, financialInfoList.get(1).getMonthlyTotalIndividualResponsibilityAmount());
	}

	@Test
	public void testSortFinancialInfos_One_FinancialInfo() {

		List<FinancialInformation> financialInfoList = new ArrayList<FinancialInformation>();

		BigDecimal tpaJan = BigDecimal.valueOf(11.11);
		BigDecimal tiraJan = BigDecimal.valueOf(22.22);

		FinancialInformation fitJan = new FinancialInformation();
		fitJan.setMonthlyTotalPremiumAmount(tpaJan);
		fitJan.setMonthlyTotalIndividualResponsibilityAmount(tiraJan);
		fitJan.setFinancialEffectiveStartDate(getXMLGregorianCalendar(JAN_1));
		fitJan.setFinancialEffectiveEndDate(getXMLGregorianCalendar(JAN_15));
		
		financialInfoList.add(fitJan);

		SbmValidationUtil.sortFinancialInfo(financialInfoList);

		assertNotNull("FinancialInformation", financialInfoList);	
		assertEquals("TotalPremiumAmount (element now 0)", tpaJan, financialInfoList.get(0).getMonthlyTotalPremiumAmount());
		assertEquals("TotalIndividualResponsibilityAmount (element now 0)", tiraJan, financialInfoList.get(0).getMonthlyTotalIndividualResponsibilityAmount());
	}
	
	@Test
	public void test_hasValidationErrors_True() {
		
		List<SbmErrWarningLogDTO> errorWarningList = new ArrayList<SbmErrWarningLogDTO>();
		
		errorWarningList.add(SbmValidationUtil.createMemberErrorWarningLogDTO(
				"ExchangeAssignedMemberID", SBMErrorWarningCode.WR_010.getCode(), "Mem123"));
		
		errorWarningList.add(SbmValidationUtil.createErrorWarningLogDTO(
				"ExchangeAssignedMemberID", SBMErrorWarningCode.ER_001.getCode()));
		
		boolean hasErrors = SbmValidationUtil.hasValidationError(errorWarningList);
		
		assertTrue("hasErrors", hasErrors);	
	}
	
	@Test
	public void test_hasValidationErrors_False() {
		
		List<SbmErrWarningLogDTO> errorWarningList = new ArrayList<SbmErrWarningLogDTO>();
		
		errorWarningList.add(SbmValidationUtil.createMemberErrorWarningLogDTO(
				"ExchangeAssignedMemberID", SBMErrorWarningCode.WR_010.getCode(), "Mem123"));
		
		boolean hasErrors = SbmValidationUtil.hasValidationError(errorWarningList);
		
		assertFalse("hasErrors", hasErrors);	
	}
	
	@Test
	public void test_isDuplicatePolicy_False() {
		
		List<String> policyIds = Arrays.asList("EXPOLICYID1", "EXPOLICYID2");
		SBMCache.getPolicyIds().addAll(policyIds);
		
		boolean isDuplicatePolicy = SbmValidationUtil.isDuplicatePolicy("EXPOLICYID1");
		
		assertFalse("isDuplicatePolicy", isDuplicatePolicy);	
		
	}
	
	@Test
	public void test_isDuplicatePolicy_True() {
		
		List<String> policyIds = Arrays.asList("EXPOLICYID1", "EXPOLICYID1", "EXPOLICYID2");
		SBMCache.getPolicyIds().addAll(policyIds);
		
		boolean isDuplicatePolicy = SbmValidationUtil.isDuplicatePolicy("EXPOLICYID1");
		
		assertTrue("isDuplicatePolicy", isDuplicatePolicy);	
	}
	

	protected XMLGregorianCalendar getXMLGregorianCalendar(org.joda.time.DateTime date) {
		XMLGregorianCalendar xmlGregCal = null;
		if (date == null) {
			return xmlGregCal;
		} else {
			GregorianCalendar gc = new GregorianCalendar();
			gc.setTimeInMillis(date.getMillis());
			xmlGregCal = dfInstance.newXMLGregorianCalendar(gc);
			xmlGregCal.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
			xmlGregCal.setMillisecond(DatatypeConstants.FIELD_UNDEFINED);
			return xmlGregCal;
		}
	}

	protected DateTime getDateTimeFromXmlGC(XMLGregorianCalendar xmlGC) {

		DateTime dtTime = null;
		if (xmlGC != null ) {
			dtTime = new DateTime(xmlGC.toGregorianCalendar().getTime());
		}
		return dtTime;
	}
	
	@Test
	public void test_getIssuerIdFromQhpId() {
		
		String qhpIdId = RandomStringUtils.random(5);
		
		String issuerId = SbmValidationUtil.getIssuerIdFromQhpId(qhpIdId);
		
		assertEquals("Issuer Id", issuerId, qhpIdId);	
	}
	
	@Test
	public void test_getIssuerIdFromQhpId_LT5() {
		
		String qhpIdId = RandomStringUtils.random(4);
		
		String issuerId = SbmValidationUtil.getIssuerIdFromQhpId(qhpIdId);
		
		assertEquals("Issuer Id", "", issuerId);	
	}
	
	@Test
	public void test_getStateCdFromQhpId() {
		
		String qhpId = RandomStringUtils.random(7);
		
		String stateCd = SbmValidationUtil.getStateCdFromQhpId(qhpId);
		
		assertEquals("Issuer Id", qhpId.substring(5, 7), stateCd);	
	}
	
	@Test
	public void test_getStateCdFromQhpId_LT7() {
		
		String qhpId = RandomStringUtils.random(6);
		
		String stateCd = SbmValidationUtil.getStateCdFromQhpId(qhpId);
		
		assertEquals("Issuer Id", "", stateCd);	
	}
	
	@After
	public void tearDown() {
		SBMCache.getPolicyIds().clear();
	}

}
