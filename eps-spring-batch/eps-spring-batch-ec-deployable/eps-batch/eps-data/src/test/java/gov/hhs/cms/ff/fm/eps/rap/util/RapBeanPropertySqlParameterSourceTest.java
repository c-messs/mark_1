package gov.hhs.cms.ff.fm.eps.rap.util;

import gov.hhs.cms.ff.fm.eps.ep.po.PolicyVersionPO;
import gov.hhs.cms.ff.fm.eps.rap.domain.PolicyPaymentTrans;

import java.util.Calendar;

import junit.framework.TestCase;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class RapBeanPropertySqlParameterSourceTest extends TestCase {
	
	private final Calendar CAL_BASE = Calendar.getInstance();
	
	protected final DateTime APR_1 = new DateTime(CAL_BASE.get(Calendar.YEAR), 4, 1, 0, 0);
	protected final DateTime MAY_1 = new DateTime(CAL_BASE.get(Calendar.YEAR), 5, 1, 0, 0);
	
	
	@Test
	public void testConstructor() {
		
		PolicyVersionPO po = new PolicyVersionPO();
		RapBeanPropertySqlParameterSource rbpsps = new RapBeanPropertySqlParameterSource(po);
		assertNotNull("RapBeanPropertySqlParameterSource", rbpsps);
	}
	
	@Test
	public void testGetValue() {
		
		DateTime expectedBenefitBeginDate = APR_1;
		Long expectedTransMsgId = Long.valueOf(666666);
		PolicyPaymentTrans po = new PolicyPaymentTrans();
		po.setPaymentCoverageStartDate(expectedBenefitBeginDate);
		po.setParentPolicyPaymentTransId(expectedTransMsgId);
		RapBeanPropertySqlParameterSource actual = new RapBeanPropertySqlParameterSource(po);
		assertNotNull("RapBeanPropertySqlParameterSource", actual);
		assertEquals("BenefitBeginDate", 
				expectedBenefitBeginDate.toDate().toString(), actual.getValue("paymentCoverageStartDate").toString());
		assertNotNull("ParentPolicyPaymentTransId", actual.getValue("parentPolicyPaymentTransId"));
		assertEquals("ParentPolicyPaymentTransId", expectedTransMsgId, actual.getValue("parentPolicyPaymentTransId"));
	}

}
