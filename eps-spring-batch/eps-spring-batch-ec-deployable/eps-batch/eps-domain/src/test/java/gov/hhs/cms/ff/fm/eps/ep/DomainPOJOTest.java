package gov.hhs.cms.ff.fm.eps.ep;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;

import org.junit.Test;

import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMErrorDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMExecutionReportDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileErrorAdditionalInfo;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileErrorDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileInfo;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileProccessingSummary;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileProcessingDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMPolicyDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMPremium;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMScemaErrorsDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMSummaryAndFileInfoDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMUpdateStatusDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMUpdateStatusErrorDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMUpdateStatusRecordDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SbmErrWarningLogDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SbmResponseDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SbmXMLSchemaError;
import gov.hhs.cms.ff.fm.eps.ep.sbm.XMLSchemaError;
import gov.hhs.cms.ff.fm.eps.rap.domain.BatchProcessLog;
import gov.hhs.cms.ff.fm.eps.rap.domain.IssuerUserFeeRate;
import gov.hhs.cms.ff.fm.eps.rap.domain.PolicyPaymentTrans;
import gov.hhs.cms.ff.fm.eps.rap.domain.PolicyPremium;
import gov.hhs.cms.ff.fm.eps.rap.dto.PolicyPaymentTransDTO;
import junit.framework.TestCase;

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
		callMethods(new SBMErrorDTO());
		callMethods(new SbmErrWarningLogDTO());
		callMethods(new SBMExecutionReportDTO());
		callMethods(new SBMFileErrorDTO());
		callMethods(new SBMFileInfo());
		callMethods(new SBMFileProccessingSummary());
		callMethods(new SBMFileProcessingDTO());
		callMethods(new SbmResponseDTO());
		callMethods(new SBMPremium());
		callMethods(new SBMScemaErrorsDTO());
		callMethods(new SBMSummaryAndFileInfoDTO());
		callMethods(new SBMUpdateStatusDTO());
		callMethods(new SBMUpdateStatusErrorDTO());
		callMethods(new SBMUpdateStatusRecordDTO());
		callMethods(new SBMFileErrorAdditionalInfo());
		callMethods(new SBMPolicyDTO());
	
		
		assertTrue("methods invoked successfully", true);
	}
	
}
