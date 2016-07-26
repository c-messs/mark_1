package gov.hhs.cms.ff.fm.eps.ep.enums;

import junit.framework.TestCase;

import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.accenture.foundation.common.exception.ApplicationException;

public class EnumsTest extends TestCase{


	@Test
	public void testAddressTypeEnum() {

		AddressTypeEnum[] epsEnum = AddressTypeEnum.values();
		assertEnumValsAndDesc(epsEnum);
		try {
			AddressTypeEnum.getEnum("XXX");
		} catch (ApplicationException appEx) {
			assertTrue("ApplicationException thrown for invalid value", true);
		}
	}
	
	@Test
	public void testEProdEnum() {

		EProdEnum[] epsEnumArray = EProdEnum.values();
		String code;
		String longNm;
		String desc;
		Enum<?> actualEnum;
		for (Enum<?> epsEnum : epsEnumArray) {
			code = (String) ReflectionTestUtils.invokeMethod(epsEnum, "getCode", new Object[] {} );
			assertNotNull(epsEnum.getClass().getName() + " code", code);
			longNm = (String) ReflectionTestUtils.invokeMethod(epsEnum, "getLongNm", new Object[] {} );
			assertNotNull(epsEnum.getClass().getName() + " longNm", longNm);
			desc = (String) ReflectionTestUtils.invokeMethod(epsEnum, "getDesc", new Object[] {} );
			assertNotNull(epsEnum.getClass().getName() + " descr", desc);
			actualEnum = (Enum<?>) ReflectionTestUtils.invokeMethod(epsEnum, "getEnum", new Object[] {code} );
			assertNotNull(code + ": " + desc, actualEnum);
		}
		try {
			EProdEnum.getEnum("XXX");
		} catch (ApplicationException appEx) {
			assertTrue("ApplicationException thrown for invalid value", true);
		}
		
	}
	
	@Test
	public void testErrorWarningType() {

		ErrorWarningType[] epsEnum = ErrorWarningType.values();
		assertEnumValsAndDesc(epsEnum);
		try {
			ErrorWarningType.getEnum("XXX");
		} catch (ApplicationException appEx) {
			assertTrue("ApplicationException thrown for invalid value", true);
		}
	}
	
	
	
	@Test
	public void testExchangeType() {

		ExchangeType[] epsEnum = ExchangeType.values();
		assertEnumValsAndDesc(epsEnum);
		try {
			ExchangeType.getEnum("XXX");
		} catch (ApplicationException appEx) {
			assertTrue("ApplicationException thrown for invalid value", true);
		}
	}
	
	
	@Test
	public void testInsuranceApplicationType() {

		InsuranceApplicationType[] epsEnum = InsuranceApplicationType.values();
		assertEnumValsAndDesc(epsEnum);
		try {
			InsuranceApplicationType.getEnum("XXX");
		} catch (ApplicationException appEx) {
			assertTrue("ApplicationException thrown for invalid value", true);
		}
	}
	
	@Test
	public void testProcessedToDbInd() {

		ProcessedToDbInd[] epsEnum = ProcessedToDbInd.values();
		assertEnumValsAndDesc(epsEnum);
		try {
			ProcessedToDbInd.getEnum("XXX");
		} catch (ApplicationException appEx) {
			assertTrue("ApplicationException thrown for invalid value", true);
		}
	}
	
	
	@Test
	public void testTxnMessageDirectionType() {

		TxnMessageDirectionType[] epsEnum = TxnMessageDirectionType.values();
		assertEnumValsAndDesc(epsEnum);
		try {
			TxnMessageDirectionType.getEnum("XXX");
		} catch (ApplicationException appEx) {
			assertTrue("ApplicationException thrown for invalid value", true);
		}
	}
	
	@Test
	public void testTxnMessageType() {

		TxnMessageType[] epsEnum = TxnMessageType.values();
		assertEnumValsAndDesc(epsEnum);
		try {
			TxnMessageType.getEnum("XXX");
		} catch (ApplicationException appEx) {
			assertTrue("ApplicationException thrown for invalid value", true);
		}
	}
	
	@Test
	public void testPolicyStatus() {

		PolicyStatus[] epsEnum = PolicyStatus.values();
		assertEnumValsAndDesc(epsEnum);
		try {
			PolicyStatus.getEnum("XXX");
		} catch (ApplicationException appEx) {
			assertTrue("ApplicationException thrown for invalid value", true);
		}
	}
	
	
	private void assertEnumValsAndDesc(Enum<?>[] enumClassArray) {

		String actualValue;
		String actualDesc;
		Enum<?> actualEnum;
		for (Enum<?> epsEnum : enumClassArray) {
			actualValue = (String) ReflectionTestUtils.invokeMethod(epsEnum, "getValue", new Object[] {} );
			assertNotNull(epsEnum.getClass().getName() + " value", actualValue);
			actualDesc = (String) ReflectionTestUtils.invokeMethod(epsEnum, "getDescription", new Object[] {} );
			assertNotNull(epsEnum.getClass().getName() + " description", actualDesc);
			actualEnum = (Enum<?>) ReflectionTestUtils.invokeMethod(epsEnum, "getEnum", new Object[] {actualValue} );
			assertNotNull(actualValue + ": " + actualDesc, actualEnum);
		}
	}

}
