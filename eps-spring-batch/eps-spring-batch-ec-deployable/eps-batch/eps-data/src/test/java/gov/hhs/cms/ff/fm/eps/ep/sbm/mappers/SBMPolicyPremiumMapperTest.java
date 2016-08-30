package gov.hhs.cms.ff.fm.eps.ep.sbm.mappers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import gov.hhs.cms.ff.fm.eps.ep.po.SbmPolicyPremiumPO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMPremium;
import gov.hhs.cms.ff.fm.eps.ep.util.sbm.TestDataSBMUtility;

public class SBMPolicyPremiumMapperTest extends SBMBaseMapperTest {
	
	SbmPolicyPremiumMapper mapper = new SbmPolicyPremiumMapper();
	
	@Test
	public void test_mapSbmToStaging_Empty_Eps() {
		
		int expectedEPSRecords = 1;
		boolean expectedIsPolicyChanged = true;
		String exchangePolicyId = "000000001";
	
		SBMPremium sbmPremium1 = TestDataSBMUtility.makeSBMPremium(exchangePolicyId); 
		Map<LocalDate, SBMPremium> expectedSBMPremiums = new LinkedHashMap<LocalDate, SBMPremium>(); 
		expectedSBMPremiums.put(sbmPremium1.getEffectiveStartDate(), sbmPremium1);
		
		List<SbmPolicyPremiumPO> epsPremiums = new ArrayList<SbmPolicyPremiumPO>();
		
		List<SbmPolicyPremiumPO> actualList = mapper.mapSbmToStaging(expectedSBMPremiums, epsPremiums);
		assertEquals("SbmPolicyPremiumPO list size", expectedEPSRecords, actualList.size());
		SbmPolicyPremiumPO actual = actualList.get(0);
		
		SBMPremium expectedSBMPremium = expectedSBMPremiums.get(sbmPremium1.getEffectiveStartDate());
		
		assertEquals("EffectiveStartDate", expectedSBMPremium.getEffectiveStartDate(), actual.getEffectiveStartDate());
		assertEquals("EffectiveEndDate", expectedSBMPremium.getEffectiveEndDate(), actual.getEffectiveEndDate());
		assertEquals("TotalPremiumAmount", expectedSBMPremium.getTotalPremium(), actual.getTotalPremiumAmount());
		assertEquals("IndividualResponsibleAmount", expectedSBMPremium.getIndividualResponsibleAmt(), actual.getIndividualResponsibleAmount());
		assertEquals("RatingArea", expectedSBMPremium.getRatingArea(), actual.getExchangeRateArea());
		assertEquals("APTC", expectedSBMPremium.getAptc(), actual.getAptcAmount());
		assertEquals("CSR", expectedSBMPremium.getCsr(), actual.getCsrAmount());
		assertEquals("PRO-TPA", expectedSBMPremium.getProratedPremium(), actual.getProratedPremiumAmount());
		assertEquals("PRO-APTC", expectedSBMPremium.getProratedAptc(), actual.getProratedAptcAmount());
		assertEquals("PRO-CSR", expectedSBMPremium.getProratedCsr(), actual.getProratedCsrAmount());
		assertEquals("CSRVariantId", expectedSBMPremium.getCsrVariantId(), actual.getInsrncPlanVariantCmptTypeCd());
		assertEquals("OtherPayment1", expectedSBMPremium.getOtherPayment1(), actual.getOtherPaymentAmount1());
		assertEquals("OtherPayment2", expectedSBMPremium.getOtherPayment2(), actual.getOtherPaymentAmount2());
		
		assertNull("PRO-TIRA", actual.getProratedInddResponsibleAmount());
		
		assertEquals("isPolicyChanged", expectedIsPolicyChanged, actual.isPolicyChanged());
	}
	
	
	
	@Test
	public void test_mapSbmToStaging() {
		
		int expectedEPSRecords = 1;
		boolean expectedIsPolicyChanged = true;
		String exchangePolicyId = "000000001";
	
		SBMPremium sbmPremium1 = TestDataSBMUtility.makeSBMPremium(exchangePolicyId); 
		Map<LocalDate, SBMPremium> expectedSBMPremiums = new LinkedHashMap<LocalDate, SBMPremium>(); 
		expectedSBMPremiums.put(sbmPremium1.getEffectiveStartDate(), sbmPremium1);
		
		List<SbmPolicyPremiumPO> epsPremiums = new ArrayList<SbmPolicyPremiumPO>();
		
		// Create an EPS premium nearly identical to inbound with only APTC being different.
		SbmPolicyPremiumPO epsPremium = new SbmPolicyPremiumPO();
		epsPremium.setEffectiveStartDate(sbmPremium1.getEffectiveStartDate());
		epsPremium.setEffectiveEndDate(sbmPremium1.getEffectiveEndDate());
		epsPremium.setTotalPremiumAmount(sbmPremium1.getTotalPremium());
		epsPremium.setIndividualResponsibleAmount(sbmPremium1.getIndividualResponsibleAmt());
		epsPremium.setExchangeRateArea(sbmPremium1.getRatingArea());
		// Set APTC to something different.
		epsPremium.setAptcAmount(new BigDecimal("888.88"));
		epsPremium.setCsrAmount(sbmPremium1.getCsr());
		epsPremium.setProratedPremiumAmount(sbmPremium1.getProratedPremium());
		epsPremium.setProratedAptcAmount(sbmPremium1.getProratedAptc());
		epsPremium.setProratedCsrAmount(sbmPremium1.getProratedCsr());
		epsPremium.setInsrncPlanVariantCmptTypeCd(sbmPremium1.getCsrVariantId());
		epsPremium.setOtherPaymentAmount1(sbmPremium1.getOtherPayment1());
		epsPremium.setOtherPaymentAmount2(sbmPremium1.getOtherPayment2());
		
		List<SbmPolicyPremiumPO> actualList = mapper.mapSbmToStaging(expectedSBMPremiums, epsPremiums);
		assertEquals("SbmPolicyPremiumPO list size", expectedEPSRecords, actualList.size());
		SbmPolicyPremiumPO actual = actualList.get(0);
		
		SBMPremium expectedSBMPremium = expectedSBMPremiums.get(sbmPremium1.getEffectiveStartDate());
		
		assertEquals("EffectiveStartDate", expectedSBMPremium.getEffectiveStartDate(), actual.getEffectiveStartDate());
		assertEquals("EffectiveEndDate", expectedSBMPremium.getEffectiveEndDate(), actual.getEffectiveEndDate());
		assertEquals("TotalPremiumAmount", expectedSBMPremium.getTotalPremium(), actual.getTotalPremiumAmount());
		assertEquals("IndividualResponsibleAmount", expectedSBMPremium.getIndividualResponsibleAmt(), actual.getIndividualResponsibleAmount());
		assertEquals("RatingArea", expectedSBMPremium.getRatingArea(), actual.getExchangeRateArea());
		assertEquals("APTC", expectedSBMPremium.getAptc(), actual.getAptcAmount());
		assertEquals("CSR", expectedSBMPremium.getCsr(), actual.getCsrAmount());
		assertEquals("PRO-TPA", expectedSBMPremium.getProratedPremium(), actual.getProratedPremiumAmount());
		assertEquals("PRO-APTC", expectedSBMPremium.getProratedAptc(), actual.getProratedAptcAmount());
		assertEquals("PRO-CSR", expectedSBMPremium.getProratedCsr(), actual.getProratedCsrAmount());
		assertEquals("CSRVariantId", expectedSBMPremium.getCsrVariantId(), actual.getInsrncPlanVariantCmptTypeCd());
		assertEquals("OtherPayment1", expectedSBMPremium.getOtherPayment1(), actual.getOtherPaymentAmount1());
		assertEquals("OtherPayment2", expectedSBMPremium.getOtherPayment2(), actual.getOtherPaymentAmount2());
		
		assertNull("PRO-TIRA", actual.getProratedInddResponsibleAmount());
		
		assertEquals("isPolicyChanged", expectedIsPolicyChanged, actual.isPolicyChanged());
		
	}
	
	
	@Test
	public void test_mapSBMToStaging_NoChange() {
		
		int expectedEPSRecords = 1;
		boolean expectedIsPolicyChanged = false;
		String exchangePolicyId = "000000001";
	
		SBMPremium sbmPremium1 = TestDataSBMUtility.makeSBMPremium(exchangePolicyId); 
		Map<LocalDate, SBMPremium> expectedSBMPremiums = new LinkedHashMap<LocalDate, SBMPremium>(); 
		expectedSBMPremiums.put(sbmPremium1.getEffectiveStartDate(), sbmPremium1);
		
		List<SbmPolicyPremiumPO> epsPremiums = new ArrayList<SbmPolicyPremiumPO>();
		
		// Create an EPS premium nearly identical to inbound.
		SbmPolicyPremiumPO epsPremium = new SbmPolicyPremiumPO();
		epsPremium.setEffectiveStartDate(sbmPremium1.getEffectiveStartDate());
		epsPremium.setEffectiveEndDate(sbmPremium1.getEffectiveEndDate());
		epsPremium.setTotalPremiumAmount(sbmPremium1.getTotalPremium());
		epsPremium.setIndividualResponsibleAmount(sbmPremium1.getIndividualResponsibleAmt());
		epsPremium.setExchangeRateArea(sbmPremium1.getRatingArea());
		epsPremium.setAptcAmount(sbmPremium1.getAptc());
		epsPremium.setCsrAmount(sbmPremium1.getCsr());
		epsPremium.setProratedPremiumAmount(sbmPremium1.getProratedPremium());
		epsPremium.setProratedAptcAmount(sbmPremium1.getProratedAptc());
		epsPremium.setProratedCsrAmount(sbmPremium1.getProratedCsr());
		epsPremium.setInsrncPlanVariantCmptTypeCd(sbmPremium1.getCsrVariantId());
		epsPremium.setOtherPaymentAmount1(sbmPremium1.getOtherPayment1());
		epsPremium.setOtherPaymentAmount2(sbmPremium1.getOtherPayment2());
		epsPremiums.add(epsPremium);
		
		List<SbmPolicyPremiumPO> actualList = mapper.mapSbmToStaging(expectedSBMPremiums, epsPremiums);
		assertEquals("SbmPolicyPremiumPO list size", expectedEPSRecords, actualList.size());
		SbmPolicyPremiumPO actual = actualList.get(0);
		
		SBMPremium expectedSBMPremium = expectedSBMPremiums.get(sbmPremium1.getEffectiveStartDate());
		
		assertEquals("EffectiveStartDate", expectedSBMPremium.getEffectiveStartDate(), actual.getEffectiveStartDate());
		assertEquals("EffectiveEndDate", expectedSBMPremium.getEffectiveEndDate(), actual.getEffectiveEndDate());
		assertEquals("TotalPremiumAmount", expectedSBMPremium.getTotalPremium(), actual.getTotalPremiumAmount());
		assertEquals("IndividualResponsibleAmount", expectedSBMPremium.getIndividualResponsibleAmt(), actual.getIndividualResponsibleAmount());
		assertEquals("RatingArea", expectedSBMPremium.getRatingArea(), actual.getExchangeRateArea());
		assertEquals("APTC", expectedSBMPremium.getAptc(), actual.getAptcAmount());
		assertEquals("CSR", expectedSBMPremium.getCsr(), actual.getCsrAmount());
		assertEquals("PRO-TPA", expectedSBMPremium.getProratedPremium(), actual.getProratedPremiumAmount());
		assertEquals("PRO-APTC", expectedSBMPremium.getProratedAptc(), actual.getProratedAptcAmount());
		assertEquals("PRO-CSR", expectedSBMPremium.getProratedCsr(), actual.getProratedCsrAmount());
		assertEquals("CSRVariantId", expectedSBMPremium.getCsrVariantId(), actual.getInsrncPlanVariantCmptTypeCd());
		assertEquals("OtherPayment1", expectedSBMPremium.getOtherPayment1(), actual.getOtherPaymentAmount1());
		assertEquals("OtherPayment2", expectedSBMPremium.getOtherPayment2(), actual.getOtherPaymentAmount2());
		
		assertNull("PRO-TIRA", actual.getProratedInddResponsibleAmount());
		
		assertEquals("isPolicyChanged", expectedIsPolicyChanged, actual.isPolicyChanged());
		
	}


}
