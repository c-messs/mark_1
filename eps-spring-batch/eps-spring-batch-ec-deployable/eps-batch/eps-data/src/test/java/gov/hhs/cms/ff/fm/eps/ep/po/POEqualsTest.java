package gov.hhs.cms.ff.fm.eps.ep.po;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Calendar;

import junit.framework.TestCase;

import org.joda.time.DateTime;
import org.junit.Test;

public class POEqualsTest extends TestCase {

	protected static final Calendar CAL_BASE = Calendar.getInstance();
	private static final String DATETM = "org.joda.time.DateTime";
	private static final String STRING = "java.lang.String";
	private static final String LONG = "java.lang.Long";
	private static final String BIGDEC = "java.math.BigDecimal";
	private static final String INTGR = "java.lang.Integer";

	protected final DateTime APR_1 = new DateTime(CAL_BASE.get(Calendar.YEAR), 4, 1, 0, 0);



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


	private void assertEqualsMethod(GenericPolicyMemberPO<?> po1, GenericPolicyMemberPO<?> po2) throws Exception {


		DateTime testDt = APR_1;
		String testStr = "higklmnop";
		Long testNum = Long.valueOf("66");
		BigDecimal testBD = BigDecimal.valueOf(99.99);
		Integer testInt = Integer.valueOf(8);

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
						&& methNm.indexOf("setPolicyMemberVersionId") == -1 && methNm.indexOf("setPolicyMemberChanged") == -1 
						&& methNm.indexOf("setMaintenance") == -1 && methNm.indexOf("setTransMsgID") == -1
						&& methNm.indexOf("setOtherRelationshipSequence") == -1 && methNm.indexOf("setExchangePolicyId") == -1
						&& methNm.indexOf("setSubscriberStateCd") == -1) { 
					if (argType.equals(DATETM)) {
						methods1[i].invoke(po1, new Object[]{testDt});	
						assertFalse(methNm, po1.equals(po2));
						methods2[i].invoke(po2, new Object[]{testDt});
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
					assertTrue("hashCode result",  po1.hashCode() != 1);
					methods1[i].invoke(po1, new Object[]{null});
					assertFalse("po1 , po2 ", po1.equals(po2));
					methods2[i].invoke(po2, new Object[]{null});
					assertTrue("po1, po2", po1.equals(po2));
					
				}
			} 
		}
	}


	private void assertEqualsMethod(GenericPolicyPO<?> po1, GenericPolicyPO<?> po2) throws Exception {

		String testStr = "higklmnop";

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
						&& methNm.indexOf("setPolicyVersionId") == -1 && methNm.indexOf("setTransDateTime") == -1) { 
				    if (argType.equals(STRING)) {
				    	assertTrue("hashCode result",  po1.hashCode() > 0);
						methods1[i].invoke(po1, new Object[]{testStr});
						assertFalse(methNm, po1.equals(po2));
						assertTrue("hashCode result",  po1.hashCode() < 0);
						methods2[i].invoke(po2, new Object[]{testStr});
						assertTrue("po1, po2", po1.equals(po2));
					} 
				    assertTrue("hashCode result",  po1.hashCode() != 1);
					methods1[i].invoke(po1, new Object[]{null});
					assertFalse("po1 , po2 ", po1.equals(po2));
					methods2[i].invoke(po2, new Object[]{null});
					assertTrue("po1, po2", po1.equals(po2));
				}
			} 
		}
	}



}
