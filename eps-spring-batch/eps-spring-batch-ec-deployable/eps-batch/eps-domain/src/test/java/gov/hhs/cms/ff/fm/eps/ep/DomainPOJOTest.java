package gov.hhs.cms.ff.fm.eps.ep;

import gov.hhs.cms.ff.fm.eps.rap.domain.BatchProcessLog;
import gov.hhs.cms.ff.fm.eps.rap.domain.IssuerUserFeeRate;
import gov.hhs.cms.ff.fm.eps.rap.domain.PolicyPaymentTrans;
import gov.hhs.cms.ff.fm.eps.rap.domain.PolicyPremium;
import gov.hhs.cms.ff.fm.eps.rap.dto.PolicyPaymentTransDTO;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;

import junit.framework.TestCase;

import org.junit.Test;

public class DomainPOJOTest extends TestCase {
	
	private void callMethods(Object object) throws IllegalAccessException,
	IllegalArgumentException, InvocationTargetException, InstantiationException {
		for (Method method : object.getClass().getMethods()) {
			if(method.getName().startsWith("set")) {
				if( method.getParameterTypes().length > 0 ) {
					Class<?> paramClass = method.getParameterTypes()[0];
					if(paramClass.getName().equals("java.lang.String")) {
						method.invoke(object, "JUNIT TEST");
					} else if(paramClass.getName().equals("java.lang.Object")) {
						method.invoke(object, object);
					} else if(paramClass.getName().equals("java.util.Date")) {
						method.invoke(object, new java.util.Date());
					} else if(paramClass.getName().equals("java.math.BigDecimal")) {
						method.invoke(object, new BigDecimal(100));
					} else if(paramClass.getName().equals("long")) {
						method.invoke(object, 100L);
					} else if(paramClass.getName().equals("java.lang.Long")) {
						method.invoke(object, 100L);
					} else if(paramClass.getName().equals("int")) {
						method.invoke(object, 100);
					} else if(paramClass.getName().equals("java.lang.Integer")) {
						method.invoke(object, 100);
					} else if(paramClass.getName().equals("boolean")) {
						method.invoke(object, true);
					} else if(paramClass.getName().equals("org.joda.time.DateTime")) {
						method.invoke(object, new org.joda.time.DateTime());
					}
				}
			} else if (method.getName().startsWith("get") ) {
				if( method.getParameterTypes().length == 0 )
					method.invoke(object);
			} else if (method.getName().startsWith("is") ) {
				if( method.getParameterTypes().length == 0 )
					method.invoke(object);
			} else if (method.getName().startsWith("toString") ) {
				if( method.getParameterTypes().length == 0 )
					method.invoke(object);
			} else if(method.getName().equals("equals")) {
				method.invoke(object, object.getClass().newInstance());
			} else if(method.getName().equals("hashCode")) {
				method.invoke(object);
			} 
		}

	}

	@Test
	public void test() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException {
		callMethods(new PolicyPaymentTrans()); 
		callMethods(new BatchProcessLog());
		callMethods(new BatchRunControl());
		callMethods(new PolicyPaymentTransDTO());
		callMethods(new EPSFileIndex());
		callMethods(new IssuerUserFeeRate());
		callMethods(new PolicyPremium());
		callMethods(new ErrorWarningLogDTO());
		callMethods(new BenefitEnrollmentRequestDTO());
		
		assertTrue("methods invoked successfully", true);
	}
	
}
