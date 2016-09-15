package gov.hhs.cms.ff.fm.eps.ep.po;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;

import junit.framework.TestCase;

import org.junit.Test;

public class POEqualsTest extends TestCase {


	protected static final LocalDate DATE = LocalDate.now();
	protected static final int YEAR = DATE.getYear();

	protected final SimpleDateFormat sdfYMD = new SimpleDateFormat("yyyy-MM-dd");

	protected static final Long TRANS_MSG_ID = new Long("9999999999999");

	
	protected final LocalDate APR_1 = LocalDate.of(YEAR, 4, 1);
	protected final LocalDateTime APR_3_3pm = LocalDateTime.of(YEAR, 4, 3, 15, 0, 0, 333333000);
	
	private static final String LOCAL_DATE_TIME = "java.time.LocalDateTime";
	private static final String LOCAL_DATE = "java.time.LocalDate";
	private static final String STRING = "java.lang.String";
	private static final String LONG = "java.lang.Long";
	private static final String BIGDEC = "java.math.BigDecimal";
	private static final String INTGR = "java.lang.Integer";



	@Test
	public void testPolicyMemberAddressPO_Equals() throws Exception {

		PolicyMemberAddressPO po1 = new PolicyMemberAddressPO();
		assertTrue("po1, po1", po1.equals(po1));
		PolicyMemberAddressPO po2 = null;
		assertFalse("po1 empty, po2 null", po1.equals(po2));
		po2 = new PolicyMemberAddressPO();
		assertEqualsMethod(po1, po2);
		PolicyVersionPO po3 = new PolicyVersionPO();
		assertFalse("policy member po is not policy version po", po1.equals(po3));
	}
	

	@Test
	public void testPolicyMemberDatePO_hashCode() throws Exception {

		PolicyMemberDatePO po1 = new PolicyMemberDatePO();
		assertNotNull("po hashCode", po1.hashCode());
	}
	
	@Test
	public void testPolicyMemberDatePO_Equals() throws Exception {

		PolicyMemberDatePO po1 = new PolicyMemberDatePO();
		assertTrue("po1, po1", po1.equals(po1));
		PolicyMemberDatePO po2 = null;
		assertFalse("po1 empty, po2 null", po1.equals(po2));
		po2 = new PolicyMemberDatePO();
		assertEqualsMethod(po1, po2);
		PolicyVersionPO po3 = new PolicyVersionPO();
		assertFalse("policy member po is not policy version po", po1.equals(po3));
	}

	@Test
	public void testPolicyMemberAddressPO_hashCode() throws Exception {

		PolicyMemberAddressPO po1 = new PolicyMemberAddressPO();
		assertNotNull("po hashCode", po1.hashCode());
	}
	
	@Test
	public void testPolicyMemberLanguageAbilityPO_Equals() throws Exception {
		
		PolicyMemberLanguageAbilityPO po1 = new PolicyMemberLanguageAbilityPO();
		assertTrue("po1, po1", po1.equals(po1));
		PolicyMemberLanguageAbilityPO po2 = null;
		assertFalse("po1 empty, po2 null", po1.equals(po2));
		po2 = new PolicyMemberLanguageAbilityPO();
		assertEqualsMethod(po1, po2);
		PolicyVersionPO po3 = new PolicyVersionPO();
		assertFalse("policy member po is not policy version po", po1.equals(po3));
	}
	
	@Test
	public void testPolicyMemberLanguageAbilityPO_hashCode() throws Exception {
		
		PolicyMemberLanguageAbilityPO po = new PolicyMemberLanguageAbilityPO();
		assertNotNull("po hashCode", po.hashCode());
	}
	
	@Test
	public void testMemberPolicyRaceEthnicityPO_Equals() throws Exception {
		
		MemberPolicyRaceEthnicityPO po1 = new MemberPolicyRaceEthnicityPO();
		assertTrue("po1, po1", po1.equals(po1));
		MemberPolicyRaceEthnicityPO po2 = null;
		assertFalse("po1 empty, po2 null", po1.equals(po2));
		po2 = new MemberPolicyRaceEthnicityPO();
		assertEqualsMethod(po1, po2);
		PolicyVersionPO po3 = new PolicyVersionPO();
		assertFalse("policy member po is not policy version po", po1.equals(po3));
	}
	
	@Test
	public void testMemberPolicyRaceEthnicityPO_hashCode() throws Exception {
		
		MemberPolicyRaceEthnicityPO po = new MemberPolicyRaceEthnicityPO();
		assertNotNull("po hashCode", po.hashCode());
	}


	@Test
	public void testPolicyMemberVersionPO_Equals() throws Exception {

		PolicyMemberVersionPO po1 = new PolicyMemberVersionPO();
		assertTrue("po1, po1", po1.equals(po1));
		PolicyMemberVersionPO po2 = null;
		assertFalse("po1 empty, po2 null", po1.equals(po2));
		po2 = new PolicyMemberVersionPO();
		assertEqualsMethod(po1, po2);
		PolicyVersionPO po3 = new PolicyVersionPO();
		assertFalse("policy member po is not policy version po", po1.equals(po3));
	}
	
	@Test
	public void testSbmPolicyMemberVersionPO_Equals() throws Exception {

		SbmPolicyMemberVersionPO po1 = new SbmPolicyMemberVersionPO();
		assertTrue("po1, po1", po1.equals(po1));
		SbmPolicyMemberVersionPO po2 = null;
		assertFalse("po1 empty, po2 null", po1.equals(po2));
		po2 = new SbmPolicyMemberVersionPO();
		assertEqualsMethod(po1, po2);
		PolicyVersionPO po3 = new PolicyVersionPO();
		assertFalse("policy member po is not policy version po", po1.equals(po3));
	}
	
	

	@Test
	public void testSbmPolicyVersionPO_Equals() throws Exception {

		SbmPolicyVersionPO po1 = new SbmPolicyVersionPO();
		assertTrue("po1, po1", po1.equals(po1));
		SbmPolicyVersionPO po2 = null;
		assertFalse("po1 empty, po2 null", po1.equals(po2));
		po2 = new SbmPolicyVersionPO();
		assertEqualsMethod(po1, po2);
		PolicyMemberVersionPO po3 = new PolicyMemberVersionPO();
		assertFalse("policy po is not PolicyMemberVersionPO", po1.equals(po3));
	}
	
	@Test
	public void testSbmPolicyVersionPO_hashCode() throws Exception {
		
		SbmPolicyVersionPO po = new SbmPolicyVersionPO();
		assertNotNull("po hashCode", po.hashCode());
	}


	@Test
	public void test_PolicyStatusPO_Equals() throws Exception {

		PolicyStatusPO po1 = new PolicyStatusPO();
		assertTrue("po1, po1", po1.equals(po1));
		PolicyStatusPO po2 = null;
		assertFalse("po1 empty, po2 null", po1.equals(po2));
		po2 = new PolicyStatusPO();
		assertEqualsMethod(po1, po2);
		PolicyVersionPO po3 = new PolicyVersionPO();
		assertFalse("policy status po is not policy version po", po1.equals(po3));		
	}
	
	@Test
	public void test_SbmPolicyPremiumPO_Equals() throws Exception {

		SbmPolicyPremiumPO po1 = new SbmPolicyPremiumPO();
		assertTrue("po1, po1", po1.equals(po1));
		SbmPolicyPremiumPO po2 = null;
		assertFalse("po1 empty, po2 null", po1.equals(po2));
		po2 = new SbmPolicyPremiumPO();
		assertEqualsMethod(po1, po2);
		PolicyVersionPO po3 = new PolicyVersionPO();
		assertFalse("policy premium po is not policy version po", po1.equals(po3));		
	}
	
	
	@Test
	public void testSbmPolicyPremiumPO_hashCode() throws Exception {
		
		SbmPolicyPremiumPO po = new SbmPolicyPremiumPO();
		assertNotNull("po hashCode", po.hashCode());
	}

	private void assertEqualsMethod(GenericPolicyMemberPO<?> po1, GenericPolicyMemberPO<?> po2) throws Exception {

		LocalDate testLocalDate = APR_1;
		LocalDateTime testLocalDateTime = APR_3_3pm;
		String testStr = "higklmnop";
		Long testNum = Long.valueOf("66");
		BigDecimal testBD = BigDecimal.valueOf(99.99);
		Integer testInt = Integer.valueOf(8);

		Class<?> po1Clz = po1.getClass();
		Class<?> po2Clz = po2.getClass();
		Method[] methods1 = po1Clz.getDeclaredMethods();
		Method[] methods2 = po2Clz.getDeclaredMethods();
	

		assertTrue("po1 empty, po2 empty", po1.equals(po2));

		for (int i = 0; i < methods1.length; ++i) {

			String methNm = methods1[i].getName();

			if (methNm.indexOf("set") == 0) {

				Class<?>[] clz = methods1[i].getParameterTypes();
				String argType = clz[0].getName();
				if (methNm.indexOf("setCreate") == -1 && methNm.indexOf("setLastModified") == -1  
						&& methNm.indexOf("setPolicyMemberVersionId") == -1 && methNm.indexOf("setPolicyMemberChanged") == -1 
						&& methNm.indexOf("setMaintenance") == -1 && methNm.indexOf("setTransMsgID") == -1
						&& methNm.indexOf("setOtherRelationshipSequence") == -1 && methNm.indexOf("setExchangePolicyId") == -1
						&& methNm.indexOf("setSubscriberStateCd") == -1 && methNm.indexOf("setSbmTransMsgID") == -1
						&& methNm.indexOf("setPriorPolicyMemberVersionId") == -1 && methNm.indexOf("setNonCoveredSubscriberInd") == -1
						) {
					
					
					if (argType.equals(LOCAL_DATE_TIME)) {
						methods1[i].invoke(po1, new Object[]{testLocalDateTime});	
						assertFalse(methNm, po1.equals(po2));
						methods2[i].invoke(po2, new Object[]{testLocalDateTime});
						assertTrue("po1, po2", po1.equals(po2));
					} else if (argType.equals(LOCAL_DATE)) {
						methods1[i].invoke(po1, new Object[]{testLocalDate});	
						assertFalse(methNm, po1.equals(po2));
						methods2[i].invoke(po2, new Object[]{testLocalDate});
						assertTrue("po1, po2", po1.equals(po2));
					} else if (argType.equals(STRING)) {
						methods1[i].invoke(po1, new Object[]{testStr});
						assertFalse(methNm, po1.equals(po2));
						methods2[i].invoke(po2, new Object[]{testStr});
						assertTrue("po1, po2", po1.equals(po2));
					} else if (argType.equals(LONG)) {
						methods1[i].invoke(po1, new Object[]{testNum});	
						assertFalse(methNm, po1.equals(po2));
						methods2[i].invoke(po2, new Object[]{testNum});
						assertTrue("po1, po2", po1.equals(po2));
					} else if (argType.equals(BIGDEC)) {
						methods1[i].invoke(po1, new Object[]{testBD});	
						assertFalse(methNm, po1.equals(po2));
						methods2[i].invoke(po2, new Object[]{testBD});
						assertTrue("po1, po2", po1.equals(po2));
					} else if (argType.equals(INTGR)) {
						methods1[i].invoke(po1, new Object[]{testInt});	
						assertFalse(methNm, po1.equals(po2));
						methods2[i].invoke(po2, new Object[]{testInt});
						assertTrue("po1, po2", po1.equals(po2));
					}
					assertTrue("hashCode result for method: " + methNm, po1.hashCode() != 1);
					methods1[i].invoke(po1, new Object[]{null});
					assertFalse("po1 , po2 for method: " + methNm, po1.equals(po2));
					methods2[i].invoke(po2, new Object[]{null});
					assertTrue("po1, po2 for method: " + methNm, po1.equals(po2));
					
				}
			} 
		}
	}


	private void assertEqualsMethod(GenericPolicyPO<?> po1, GenericPolicyPO<?> po2) throws Exception {

		LocalDate testLocalDate = APR_1;
		LocalDateTime testLocalDateTime = APR_3_3pm;
		String testStr = "higklmnop";
		BigDecimal testBD = new BigDecimal("99.99");
		BigDecimal testBD2 = new BigDecimal("100");

		Class<?> po1Clz = po1.getClass();
		Class<?> po2Clz = po2.getClass();
		Method[] methods1 = po1Clz.getMethods();
		Method[] methods2 = po2Clz.getMethods();

		assertTrue("po1 empty, po2 empty", po1.equals(po2));

		for (int i = 0; i < methods1.length; ++i) {

			String methNm = methods1[i].getName();

			if (methNm.indexOf("set") == 0) {

				Class<?>[] clz = methods1[i].getParameterTypes();
				String argType = clz[0].getName();
				if (methNm.indexOf("setCreate") == -1 && methNm.indexOf("setLastModified") == -1  
						&& methNm.indexOf("setMaintenance") == -1 && methNm.indexOf("setTransMsgID") == -1
						&& methNm.indexOf("setPolicyVersionId") == -1 && methNm.indexOf("setTransDateTime") == -1
						&& methNm.indexOf("setSourceVersionId") == -1 && methNm.indexOf("setPreviousPolicyVersionId") == -1
						&& methNm.indexOf("setPolicyChanged") == -1  && methNm.indexOf("setSbmTransMsgId") == -1
						&& methNm.indexOf("setProratedInddResponsibleAmount") == -1  && methNm.indexOf("setSbmTransMsgId") == -1
						&& methNm.indexOf("setIssuerTaxPayerId") == -1 && methNm.indexOf("setIssuerNm") == -1
						&& methNm.indexOf("setTrans") == -1 && methNm.indexOf("setElig") == -1
						&& methNm.indexOf("setSource") == -1 && methNm.indexOf("setPremiumPaidToEndDate") == -1
						&& methNm.indexOf("setLastPremiumPaidDate") == -1 && methNm.indexOf("setEmployer") == -1
						&& methNm.indexOf("setChangeReportedDate") == -1 && methNm.indexOf("setX12CoverageLevelTypeCd") == -1
						&& methNm.indexOf("setInsrncAplctnTypeCd") == -1 && methNm.indexOf("setMarketplaceGroupPolicyId") == -1) { 
					if (argType.equals(LOCAL_DATE_TIME)) {
						methods1[i].invoke(po1, new Object[]{testLocalDateTime});	
						assertFalse(methNm, po1.equals(po2));
						methods2[i].invoke(po2, new Object[]{testLocalDateTime});
						assertTrue("po1, po2", po1.equals(po2));
					} else if (argType.equals(LOCAL_DATE)) {
						methods1[i].invoke(po1, new Object[]{testLocalDate});	
						assertFalse(methNm, po1.equals(po2));
						methods2[i].invoke(po2, new Object[]{testLocalDate});
						assertTrue("po1, po2", po1.equals(po2));
					} else if (argType.equals(STRING)) {
						methods1[i].invoke(po1, new Object[]{testStr});
						assertFalse(methNm, po1.equals(po2));
						methods2[i].invoke(po2, new Object[]{testStr});
						assertTrue("po1, po2", po1.equals(po2));
					} else if (argType.equals(BIGDEC)) {
						methods1[i].invoke(po1, new Object[]{testBD});	
						assertFalse(methNm, po1.equals(po2));
						methods2[i].invoke(po2, new Object[]{testBD});
						assertTrue("po1, po2", po1.equals(po2));
						methods2[i].invoke(po2, new Object[]{testBD2});
						assertFalse(methNm, po1.equals(po2));
					}
					assertTrue("hashCode result for method: " + methNm, po1.hashCode() != 1);
					methods1[i].invoke(po1, new Object[]{null});
					assertFalse("po1 , po2 for method: " + methNm, po1.equals(po2));
					methods2[i].invoke(po2, new Object[]{null});
					assertTrue("po1, po2 for method: " + methNm, po1.equals(po2));
				}
			} 
		}
	}



}
