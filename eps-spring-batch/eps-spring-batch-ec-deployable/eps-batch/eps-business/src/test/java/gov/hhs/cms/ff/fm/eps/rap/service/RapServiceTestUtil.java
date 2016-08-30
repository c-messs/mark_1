/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.rap.service;

import static gov.hhs.cms.ff.fm.eps.rap.domain.RapConstants.HIGH_DATE;

import gov.hhs.cms.ff.fm.eps.ep.StateProrationConfiguration;
import gov.hhs.cms.ff.fm.eps.ep.enums.ProrationType;
import gov.hhs.cms.ff.fm.eps.rap.domain.IssuerUserFeeRate;
import gov.hhs.cms.ff.fm.eps.rap.domain.PolicyPremium;
import gov.hhs.cms.ff.fm.eps.rap.dto.PolicyDataDTO;
import gov.hhs.cms.ff.fm.eps.rap.dto.PolicyDetailDTO;
import gov.hhs.cms.ff.fm.eps.rap.dto.PolicyPaymentTransDTO;
import gov.hhs.cms.ff.fm.eps.rap.util.RapProcessingHelper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

/**
 * @author girish.padmanabhan
 *
 */
public class RapServiceTestUtil {

	public static PolicyDataDTO createMockPolicyVersion(long policyVersionId, String policyId, String policyVersionDate, String policyEndDt) {
		PolicyDataDTO policyDataDTO = new PolicyDataDTO();
		policyDataDTO.setPolicyVersionId(policyVersionId);
		policyDataDTO.setMarketplaceGroupPolicyId("MGPID");
		policyDataDTO.setSubscriberStateCd("NY");
		policyDataDTO.setInsrncAplctnTypeCd("1");
		policyDataDTO.setExchangePolicyId(policyId);
		policyDataDTO.setPlanId("79998NY0010001");
		policyDataDTO.setPolicyStartDate(new DateTime("2015-01-01"));
		if (policyEndDt != null) {
			policyDataDTO.setPolicyEndDate(new DateTime(policyEndDt));
		} else {
			policyDataDTO.setPolicyEndDate(new DateTime(HIGH_DATE));
		}
		policyDataDTO.setMaintenanceStartDateTime(new DateTime(policyVersionDate));
		policyDataDTO.setIssuerStartDate(new DateTime("2015-01-01"));

		return policyDataDTO;
	}

	public static PolicyDataDTO createMockPolicyVersion(long policyVersionId, String policyId, String policyVersionDate, String policyStartDt, String policyEndDt) {
		PolicyDataDTO policyDataDTO = new PolicyDataDTO();
		policyDataDTO.setPolicyVersionId(policyVersionId);
		policyDataDTO.setMarketplaceGroupPolicyId("MGPID");
		policyDataDTO.setSubscriberStateCd("NY");
		policyDataDTO.setInsrncAplctnTypeCd("1");
		policyDataDTO.setExchangePolicyId(policyId);
		policyDataDTO.setPlanId("79998NY0010001");
		policyDataDTO.setPolicyStartDate(new DateTime(policyStartDt));
		if (policyEndDt != null) {
			policyDataDTO.setPolicyEndDate(new DateTime(policyEndDt));
		}
		policyDataDTO.setMaintenanceStartDateTime(new DateTime(policyVersionDate));
		policyDataDTO.setIssuerStartDate(policyDataDTO.getPolicyStartDate().withDayOfYear(1));

		return policyDataDTO;
	}

	public static PolicyPaymentTransDTO createMockPolicyPayment(Long pmtTransId, Long policyVersionId, String policyId,
			String transPeriodTypeCd, String coverageDt, String financialPgmType, BigDecimal amt, String status) {
		PolicyPaymentTransDTO paymentTrans = new PolicyPaymentTransDTO();
		paymentTrans.setPolicyPaymentTransId(pmtTransId);
		paymentTrans.setPolicyVersionId(policyVersionId);
		paymentTrans.setSubscriberStateCd("NY");
		paymentTrans.setExchangePolicyId(policyId);
		paymentTrans.setTransPeriodTypeCd(transPeriodTypeCd);
		paymentTrans.setCoverageDate(new DateTime(coverageDt));
		paymentTrans.setFinancialProgramTypeCd(financialPgmType);
		paymentTrans.setLastPaymentProcStatusTypeCd(status);
		paymentTrans.setPaymentAmount(amt);
		if (financialPgmType.equals("UF")) {
			paymentTrans.setTotalPremiumAmount(amt);
		}

		return paymentTrans;
	}

	public static PolicyPaymentTransDTO createMockPolicyPayment(Long pmtTransId, Long policyVersionId, 
			String policyId, String transPeriodTypeCd, String coverageDt, String coverageStartDt, 
			String coverageEndDt, String financialPgmType, BigDecimal amt, String status) {
		PolicyPaymentTransDTO paymentTrans = new PolicyPaymentTransDTO();
		paymentTrans.setPolicyPaymentTransId(pmtTransId);
		paymentTrans.setPolicyVersionId(policyVersionId);
		paymentTrans.setSubscriberStateCd("NY");
		paymentTrans.setExchangePolicyId(policyId);
		paymentTrans.setTransPeriodTypeCd(transPeriodTypeCd);
		paymentTrans.setCoverageDate(new DateTime(coverageDt));
		paymentTrans.setPaymentCoverageStartDate(new DateTime(coverageStartDt));
		paymentTrans.setPaymentCoverageEndDate(new DateTime(coverageEndDt));
		paymentTrans.setFinancialProgramTypeCd(financialPgmType);
		paymentTrans.setLastPaymentProcStatusTypeCd(status);
		paymentTrans.setPaymentAmount(amt);
		if (financialPgmType.equals("UF")) {
			paymentTrans.setTotalPremiumAmount(amt);
		}

		return paymentTrans;
	}

	public static PolicyPaymentTransDTO createMockPolicyPayment(Long pmtTransId, Long policyVersionId, 
			String policyId, String transPeriodTypeCd, String coverageDt, String coverageStartDt, 
			String coverageEndDt, String financialPgmType, BigDecimal amt, String status, String mgpId) {
		PolicyPaymentTransDTO paymentTrans = new PolicyPaymentTransDTO();
		paymentTrans.setPolicyPaymentTransId(pmtTransId);
		paymentTrans.setPolicyVersionId(policyVersionId);
		paymentTrans.setMarketplaceGroupPolicyId(mgpId);;
		paymentTrans.setSubscriberStateCd("NY");
		paymentTrans.setExchangePolicyId(policyId);
		paymentTrans.setTransPeriodTypeCd(transPeriodTypeCd);
		paymentTrans.setCoverageDate(new DateTime(coverageDt));
		paymentTrans.setPaymentCoverageStartDate(new DateTime(coverageStartDt));
		paymentTrans.setPaymentCoverageEndDate(new DateTime(coverageEndDt));
		paymentTrans.setFinancialProgramTypeCd(financialPgmType);
		paymentTrans.setLastPaymentProcStatusTypeCd(status);
		paymentTrans.setPaymentAmount(amt);
		if (financialPgmType.equals("UF")) {
			paymentTrans.setTotalPremiumAmount(amt);
		}

		return paymentTrans;
	}

	public static PolicyPremium createMockPolicyPremium(
			long policyVersionId, String effStartDt, String effEndDt, BigDecimal aptc, BigDecimal csr, BigDecimal totalPremium) {
		PolicyPremium policyPremium = new PolicyPremium();
		policyPremium.setPolicyVersionId(policyVersionId);
		policyPremium.setEffectiveStartDate(new DateTime(effStartDt));
		policyPremium.setAptcAmount(aptc);
		policyPremium.setCsrAmount(csr);
		policyPremium.setTotalPremiumAmount(totalPremium);
		if (effEndDt != null) {
			policyPremium.setEffectiveEndDate(new DateTime(effEndDt));
		}
		return policyPremium;
	}

	public static PolicyDetailDTO createMockPolicyPaymentDataForRetroEnrollment() {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		premiums.add(createMockPolicyPremium(1, "2015-01-01", null, new BigDecimal(50), new BigDecimal(25), new BigDecimal(100)));
		policyDetailDTO.setPolicyPremiums(premiums);

		return policyDetailDTO;
	}
	
	public static PolicyDetailDTO createMockPolicyPaymentDataForRetroEnrollmentMultiplePremiumsForMonth() {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		premiums.add(createMockPolicyPremium(1, "2015-01-01", "2015-01-15", new BigDecimal(50), new BigDecimal(25), new BigDecimal(100)));
		premiums.add(createMockPolicyPremium(1, "2015-01-16", null, new BigDecimal(75), new BigDecimal(50), new BigDecimal(150)));
		policyDetailDTO.setPolicyPremiums(premiums);

		return policyDetailDTO;
	}
	
	public static PolicyDetailDTO createProratedMockPolicyPaymentDataForRetroEnrollment() {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		
		PolicyPremium premium = createMockPolicyPremium(1, "2015-01-15", null, new BigDecimal(50), new BigDecimal(25), new BigDecimal(100));
		premium.setProratedAptcAmount(new BigDecimal("25.00"));
		premium.setProratedCsrAmount(new BigDecimal("12.50"));
		premium.setProratedPremiumAmount(new BigDecimal("50.00"));
		premiums.add(premium);
		
		
		policyDetailDTO.setPolicyPremiums(premiums);

		return policyDetailDTO;
	}
	
	public static PolicyDetailDTO createProratedMockPolicyPaymentDataForRetroEnrollmentMultiplePremiumsForMonth() {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		
		PolicyPremium premium = createMockPolicyPremium(1, "2015-01-01", "2015-01-15", new BigDecimal(50), new BigDecimal(25), new BigDecimal(100));
		premium.setProratedAptcAmount(new BigDecimal("25.00"));
		premium.setProratedCsrAmount(new BigDecimal("12.50"));
		premium.setProratedPremiumAmount(new BigDecimal("50.00"));
		premiums.add(premium);
		
		PolicyPremium premium2 = createMockPolicyPremium(1, "2015-01-16", null, new BigDecimal(75), new BigDecimal(50), new BigDecimal(150));
		premium2.setProratedAptcAmount(new BigDecimal("37.50"));
		premium2.setProratedCsrAmount(new BigDecimal("25.00"));
		premium2.setProratedPremiumAmount(new BigDecimal("75.00"));
		premiums.add(premium2);
		
		
		policyDetailDTO.setPolicyPremiums(premiums);

		return policyDetailDTO;
	}
	
	public static PolicyDetailDTO createProratedMockPolicyPaymentDataForRetroEnrollmentMultiplePremiumsForMonth(String proratingProgramType) {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		
		PolicyPremium premium = createMockPolicyPremium(1, "2015-01-01", "2015-01-15", new BigDecimal(50), new BigDecimal(25), new BigDecimal(100));
		if(proratingProgramType.equalsIgnoreCase("APTC")) premium.setProratedAptcAmount(new BigDecimal("25.00"));
		if(proratingProgramType.equalsIgnoreCase("CSR")) premium.setProratedCsrAmount(new BigDecimal("12.50"));
		premium.setProratedPremiumAmount(new BigDecimal("50.00"));
		premiums.add(premium);
		
		PolicyPremium premium2 = createMockPolicyPremium(1, "2015-01-16", null, new BigDecimal(75), new BigDecimal(50), new BigDecimal(150));
		if(proratingProgramType.equalsIgnoreCase("APTC")) premium2.setProratedAptcAmount(new BigDecimal("37.50"));
		if(proratingProgramType.equalsIgnoreCase("CSR")) premium2.setProratedCsrAmount(new BigDecimal("25.00"));
		premium2.setProratedPremiumAmount(new BigDecimal("75.00"));
		premiums.add(premium2);
		
		
		policyDetailDTO.setPolicyPremiums(premiums);

		return policyDetailDTO;
	}

	public static PolicyDetailDTO createMockPolicyPaymentDataForRetroEnrollment_Zero_Amounts() {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		premiums.add(createMockPolicyPremium(1, "2015-01-01", null, new BigDecimal(0), new BigDecimal(0), new BigDecimal(0)));
		policyDetailDTO.setPolicyPremiums(premiums);

		return policyDetailDTO;
	}

	public static PolicyDetailDTO createMockPolicyPaymentDataForRetroEnrollment_Null_Amounts() {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		premiums.add(createMockPolicyPremium(1, "2015-01-01", null, null, null, null));
		policyDetailDTO.setPolicyPremiums(premiums);

		return policyDetailDTO;
	}

	public static PolicyDetailDTO createMockPolicyPaymentDataForRetroEnrollment(String policyStartDate) {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		premiums.add(createMockPolicyPremium(1, policyStartDate, null, new BigDecimal(50), new BigDecimal(25), new BigDecimal(100)));
		policyDetailDTO.setPolicyPremiums(premiums);

		return policyDetailDTO;
	}

	public static PolicyDetailDTO createMockPolicyPaymentDataForRetroEnrollment(String policyStartDate, String policyEndDate) {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		premiums.add(createMockPolicyPremium(1, policyStartDate, policyEndDate, new BigDecimal(50), new BigDecimal(25), new BigDecimal(100)));
		policyDetailDTO.setPolicyPremiums(premiums);

		return policyDetailDTO;
	}

	public static PolicyDetailDTO createMockPolicyPaymentDataForRetroChange(boolean isFfm) {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		//premiums.add(createMockPolicyPremium(1, "2015-01-01", null, new BigDecimal(50), new BigDecimal(25), new BigDecimal(100)));
		premiums.add(createMockPolicyPremium(2, "2015-01-01", "2015-01-31", new BigDecimal(50), new BigDecimal(25), new BigDecimal(100)));
		premiums.add(createMockPolicyPremium(2, "2015-02-01", null, new BigDecimal(80), new BigDecimal(50), new BigDecimal(100)));

		policyDetailDTO.setPolicyPremiums(premiums);

		List<PolicyPaymentTransDTO> payments = new ArrayList<PolicyPaymentTransDTO>();
		payments.add(createMockPolicyPayment(1L, 1L, "101", "R", "2015-01-01", "2015-01-01", "2015-01-31", "APTC", new BigDecimal(50), "APPV"));
		payments.add(createMockPolicyPayment(2L, 1L, "101", "R", "2015-01-01", "2015-01-01", "2015-01-31", "CSR", new BigDecimal(25), "APPV"));
		if (isFfm) payments.add(createMockPolicyPayment(3L, 1L, "101", "R", "2015-01-01", "2015-01-01", "2015-01-31", "UF", new BigDecimal(100), "APPV"));

		payments.add(createMockPolicyPayment(4L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "APTC", new BigDecimal(50), "APPV"));
		payments.add(createMockPolicyPayment(5L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "CSR", new BigDecimal(25), "APPV"));
		if (isFfm) payments.add(createMockPolicyPayment(6L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "UF", new BigDecimal(100), "APPV"));

		policyDetailDTO.setPolicyPayments(payments);

		return policyDetailDTO;
	}
	
	public static PolicyDetailDTO createMockPolicyPaymentDataForRetroChangeMGPID() {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		premiums.add(createMockPolicyPremium(1, "2016-08-01", "2016-12-31", new BigDecimal(60), new BigDecimal(20), new BigDecimal(150)));

		policyDetailDTO.setPolicyPremiums(premiums);

		List<PolicyPaymentTransDTO> payments = new ArrayList<PolicyPaymentTransDTO>();
		payments.add(createMockPolicyPayment(1L, 1L, "101", "R", "2016-08-01", "2016-08-01", "2016-08-31", "APTC", new BigDecimal(60), "APPV"));
		payments.add(createMockPolicyPayment(2L, 1L, "101", "R", "2016-08-01", "2016-08-01", "2016-08-31", "CSR", new BigDecimal(20), "APPV"));
		payments.add(createMockPolicyPayment(3L, 1L, "101", "R", "2016-08-01", "2016-08-01", "2016-08-31", "UF", new BigDecimal(150), "APPV"));

		policyDetailDTO.setPolicyPayments(payments);

		return policyDetailDTO;
	}
	
	public static PolicyDetailDTO createMockPolicyPaymentDataForRetroChangeMGPIDWithSupersede() {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		premiums.add(createMockPolicyPremium(1, "2016-08-01", "2016-12-31", new BigDecimal(60), new BigDecimal(20), new BigDecimal(150)));

		policyDetailDTO.setPolicyPremiums(premiums);

		List<PolicyPaymentTransDTO> payments = new ArrayList<PolicyPaymentTransDTO>();
		payments.add(createMockPolicyPayment(1L, 1L, "101", "R", "2016-08-01", "2016-08-01", "2016-08-31", "APTC", new BigDecimal(60), "APPV"));
		payments.add(createMockPolicyPayment(2L, 1L, "101", "R", "2016-08-01", "2016-08-01", "2016-08-31", "CSR", new BigDecimal(20), "APPV"));
		payments.add(createMockPolicyPayment(3L, 1L, "101", "R", "2016-08-01", "2016-08-01", "2016-08-31", "UF", new BigDecimal(150), "APPV"));
		payments.get(2).setPaymentAmount(new BigDecimal("-5.25"));

		policyDetailDTO.setPolicyPayments(payments);

		return policyDetailDTO;
	}
	
	public static PolicyDetailDTO createMockPolicyPaymentDataForRetroChangeUneffectuate() {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		premiums.add(createMockPolicyPremium(1, "2016-08-01", "2016-12-31", new BigDecimal(60), new BigDecimal(20), new BigDecimal(150)));

		policyDetailDTO.setPolicyPremiums(premiums);

		List<PolicyPaymentTransDTO> payments = new ArrayList<PolicyPaymentTransDTO>();
		payments.add(createMockPolicyPayment(1L, 1L, "101", "R", "2016-08-01", "2016-08-01", "2016-08-31", "APTC", new BigDecimal(60), "APPV"));
		payments.add(createMockPolicyPayment(2L, 1L, "101", "R", "2016-08-01", "2016-08-01", "2016-08-31", "CSR", new BigDecimal(20), "APPV"));
		payments.add(createMockPolicyPayment(3L, 1L, "101", "R", "2016-08-01", "2016-08-01", "2016-08-31", "UF", new BigDecimal(150), "APPV"));
		payments.get(2).setPaymentAmount(new BigDecimal("-5.25"));

		policyDetailDTO.setPolicyPayments(payments);

		return policyDetailDTO;
	}
	
	public static PolicyDetailDTO createMockPolicyPaymentDataForRetroChangeMGPIDWithFinancialChange() {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		premiums.add(createMockPolicyPremium(1, "2016-08-01", "2016-12-31", new BigDecimal(50), new BigDecimal(10), new BigDecimal(140)));

		policyDetailDTO.setPolicyPremiums(premiums);

		List<PolicyPaymentTransDTO> payments = new ArrayList<PolicyPaymentTransDTO>();
		payments.add(createMockPolicyPayment(1L, 1L, "101", "R", "2016-08-01", "2016-08-01", "2016-08-31", "APTC", new BigDecimal(60), "APPV"));
		payments.add(createMockPolicyPayment(2L, 1L, "101", "R", "2016-08-01", "2016-08-01", "2016-08-31", "CSR", new BigDecimal(20), "APPV"));
		payments.add(createMockPolicyPayment(3L, 1L, "101", "R", "2016-08-01", "2016-08-01", "2016-08-31", "UF", new BigDecimal(150), "APPV"));
		payments.get(2).setPaymentAmount(new BigDecimal("-5.25"));

		policyDetailDTO.setPolicyPayments(payments);

		return policyDetailDTO;
	}
	
	public static PolicyDetailDTO createMockPolicyPaymentDataForRetroChange_Zero_Amounts(boolean isFfm) {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		premiums.add(createMockPolicyPremium(2, "2015-01-01", "2015-01-31", new BigDecimal(50), new BigDecimal(25), new BigDecimal(100)));
		premiums.add(createMockPolicyPremium(2, "2015-02-01", null, new BigDecimal(0), new BigDecimal(0), new BigDecimal(100)));

		policyDetailDTO.setPolicyPremiums(premiums);

		List<PolicyPaymentTransDTO> payments = new ArrayList<PolicyPaymentTransDTO>();
		payments.add(createMockPolicyPayment(1L, 1L, "101", "R", "2015-01-01", "2015-01-01", "2015-01-31", "APTC", new BigDecimal(50), "APPV"));
		payments.add(createMockPolicyPayment(2L, 1L, "101", "R", "2015-01-01", "2015-01-01", "2015-01-31", "CSR", new BigDecimal(25), "APPV"));
		if(isFfm) payments.add(createMockPolicyPayment(3L, 1L, "101", "R", "2015-01-01", "2015-01-01", "2015-01-31", "UF", new BigDecimal(100), "APPV"));

		payments.add(createMockPolicyPayment(4L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "APTC", new BigDecimal(50), "APPV"));
		payments.add(createMockPolicyPayment(5L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "CSR", new BigDecimal(25), "APPV"));
		if(isFfm) payments.add(createMockPolicyPayment(6L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "UF", new BigDecimal(100), "APPV"));

		policyDetailDTO.setPolicyPayments(payments);

		return policyDetailDTO;
	}

	public static PolicyDetailDTO createMockPolicyPaymentDataForRetroChange_Null_Amounts(boolean isFfm) {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		premiums.add(createMockPolicyPremium(2, "2015-01-01", "2015-01-31", new BigDecimal(50), new BigDecimal(25), new BigDecimal(100)));
		premiums.add(createMockPolicyPremium(2, "2015-02-01", null, null, null, new BigDecimal(100)));

		policyDetailDTO.setPolicyPremiums(premiums);

		List<PolicyPaymentTransDTO> payments = new ArrayList<PolicyPaymentTransDTO>();
		payments.add(createMockPolicyPayment(1L, 1L, "101", "R", "2015-01-01", "2015-01-01", "2015-01-31", "APTC", new BigDecimal(50), "APPV"));
		payments.add(createMockPolicyPayment(2L, 1L, "101", "R", "2015-01-01", "2015-01-01", "2015-01-31", "CSR", new BigDecimal(25), "APPV"));
		if(isFfm) payments.add(createMockPolicyPayment(3L, 1L, "101", "R", "2015-01-01", "2015-01-01", "2015-01-31", "UF", new BigDecimal(100), "APPV"));

		payments.add(createMockPolicyPayment(4L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "APTC", new BigDecimal(50), "APPV"));
		payments.add(createMockPolicyPayment(5L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "CSR", new BigDecimal(25), "APPV"));
		if(isFfm) payments.add(createMockPolicyPayment(6L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "UF", new BigDecimal(100), "APPV"));

		policyDetailDTO.setPolicyPayments(payments);

		return policyDetailDTO;
	}

	public static PolicyDetailDTO createMockPolicyPaymentDataForRetroMidMonthChange(boolean isFfm) {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		premiums.add(createMockPolicyPremium(2, "2015-01-01", "2015-01-15", new BigDecimal(50), new BigDecimal(25), new BigDecimal(100)));
		premiums.add(createMockPolicyPremium(2, "2015-01-16", null, new BigDecimal(100), new BigDecimal(50), new BigDecimal(100)));

		policyDetailDTO.setPolicyPremiums(premiums);

		List<PolicyPaymentTransDTO> payments = new ArrayList<PolicyPaymentTransDTO>();
		payments.add(createMockPolicyPayment(1L, 1L, "301", "R", "2015-01-01", "2015-01-01", "2015-01-31", "APTC", new BigDecimal(50), "APPV"));
		payments.add(createMockPolicyPayment(2L, 1L, "301", "R", "2015-01-01", "2015-01-01", "2015-01-31", "CSR", new BigDecimal(25), "APPV"));
		if(isFfm) payments.add(createMockPolicyPayment(3L, 1L, "301", "R", "2015-01-01", "2015-01-01", "2015-01-31", "UF", new BigDecimal(100), "APPV"));

		payments.add(createMockPolicyPayment(4L, 1L, "301", "R", "2015-02-01", "2015-02-01", "2015-02-28", "APTC", new BigDecimal(50), "APPV"));
		payments.add(createMockPolicyPayment(5L, 1L, "301", "R", "2015-02-01", "2015-02-01", "2015-02-28", "CSR", new BigDecimal(25), "APPV"));
		if(isFfm) payments.add(createMockPolicyPayment(6L, 1L, "301", "R", "2015-02-01", "2015-02-01", "2015-02-28", "UF", new BigDecimal(100), "APPV"));

		policyDetailDTO.setPolicyPayments(payments);

		return policyDetailDTO;
	}
	
	public static PolicyDetailDTO createMockPolicyPaymentDataForRetroMidMonthChangeSbmProrated() {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		
		PolicyPremium premium = createMockPolicyPremium(2, "2015-01-01", "2015-01-15", new BigDecimal(50), new BigDecimal(25), new BigDecimal(100));
		premium.setProratedAptcAmount(new BigDecimal("24.19"));
		premium.setProratedCsrAmount(new BigDecimal("12.10"));
		premium.setProratedPremiumAmount(new BigDecimal("50.00"));
		premiums.add(premium);
		
		PolicyPremium premium1 = createMockPolicyPremium(1, "2015-01-16", "2015-01-31", new BigDecimal(100), new BigDecimal(50), new BigDecimal(100));
		premium1.setProratedAptcAmount(new BigDecimal("51.61"));
		premium1.setProratedCsrAmount(new BigDecimal("25.81"));
		premium1.setProratedPremiumAmount(new BigDecimal("50.00"));
		premiums.add(premium1);
		
		premiums.add(createMockPolicyPremium(2, "2015-02-01", null, new BigDecimal(100), new BigDecimal(50), new BigDecimal(100)));

		policyDetailDTO.setPolicyPremiums(premiums);

		List<PolicyPaymentTransDTO> payments = new ArrayList<PolicyPaymentTransDTO>();
		payments.add(createMockPolicyPayment(1L, 1L, "301", "R", "2015-01-01", "2015-01-01", "2015-01-31", "APTC", new BigDecimal(50), "APPV"));
		payments.add(createMockPolicyPayment(2L, 1L, "301", "R", "2015-01-01", "2015-01-01", "2015-01-31", "CSR", new BigDecimal(25), "APPV"));

		payments.add(createMockPolicyPayment(4L, 1L, "301", "R", "2015-02-01", "2015-02-01", "2015-02-28", "APTC", new BigDecimal(50), "APPV"));
		payments.add(createMockPolicyPayment(5L, 1L, "301", "R", "2015-02-01", "2015-02-01", "2015-02-28", "CSR", new BigDecimal(25), "APPV"));

		policyDetailDTO.setPolicyPayments(payments);

		return policyDetailDTO;
	}
	
	public static PolicyDetailDTO createMockPolicyPaymentDataForRetroTerm(String paymentStatusCd, boolean isFfm) {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		//Premiums
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		premiums.add(createMockPolicyPremium(3, "2015-01-01", "2015-01-31", new BigDecimal(50), new BigDecimal(25), new BigDecimal(100)));
		premiums.add(createMockPolicyPremium(3, "2015-02-01", "2015-02-28", new BigDecimal(80), new BigDecimal(50), new BigDecimal(100)));

		policyDetailDTO.setPolicyPremiums(premiums);

		//Payments
		List<PolicyPaymentTransDTO> payments = new ArrayList<PolicyPaymentTransDTO>();
		
		if(isFfm) {
			payments.add(createMockPolicyPayment(1L, 1L, "101", "R", "2015-01-01", "2015-01-01", "2015-01-31", "APTC", new BigDecimal(50), paymentStatusCd));
			payments.add(createMockPolicyPayment(2L, 1L, "101", "R", "2015-01-01", "2015-01-01", "2015-01-31", "CSR", new BigDecimal(25), paymentStatusCd));
			payments.add(createMockPolicyPayment(3L, 1L, "101", "R", "2015-01-01", "2015-01-01", "2015-01-31", "UF", new BigDecimal(100), paymentStatusCd));
	
			payments.add(createMockPolicyPayment(4L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "APTC", new BigDecimal(50), paymentStatusCd));
			payments.add(createMockPolicyPayment(5L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "CSR", new BigDecimal(25), paymentStatusCd));
			payments.add(createMockPolicyPayment(6L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "UF", new BigDecimal(100), paymentStatusCd));
	
			payments.add(createMockPolicyPayment(7L, 2L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "APTC", new BigDecimal(-50), paymentStatusCd));
			payments.get(6).setParentPolicyPaymentTransId(4L);
			payments.add(createMockPolicyPayment(8L, 2L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "APTC", new BigDecimal(80), paymentStatusCd));
			payments.add(createMockPolicyPayment(9L, 2L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "CSR", new BigDecimal(-25), paymentStatusCd));
			payments.get(8).setParentPolicyPaymentTransId(5L);
			payments.add(createMockPolicyPayment(10L, 2L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "CSR", new BigDecimal(50), paymentStatusCd));
	
			payments.add(createMockPolicyPayment(11L, 2L, "101", "R", "2015-03-01", "2015-03-01", "2015-03-31", "APTC", new BigDecimal(80), paymentStatusCd));
			payments.add(createMockPolicyPayment(12L, 2L, "101", "R", "2015-03-01", "2015-03-01", "2015-03-31", "CSR", new BigDecimal(50), paymentStatusCd));
			payments.add(createMockPolicyPayment(13L, 2L, "101", "R", "2015-03-01", "2015-03-01", "2015-03-31", "UF", new BigDecimal(100), paymentStatusCd));
			
		} else {
			payments.add(createMockPolicyPayment(1L, 1L, "101", "R", "2015-01-01", "2015-01-01", "2015-01-31", "APTC", new BigDecimal(50), paymentStatusCd));
			payments.add(createMockPolicyPayment(2L, 1L, "101", "R", "2015-01-01", "2015-01-01", "2015-01-31", "CSR", new BigDecimal(25), paymentStatusCd));
	
			payments.add(createMockPolicyPayment(4L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "APTC", new BigDecimal(50), paymentStatusCd));
			payments.add(createMockPolicyPayment(5L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "CSR", new BigDecimal(25), paymentStatusCd));
	
			payments.add(createMockPolicyPayment(7L, 2L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "APTC", new BigDecimal(-50), paymentStatusCd));
			payments.get(4).setParentPolicyPaymentTransId(4L);
			payments.add(createMockPolicyPayment(8L, 2L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "APTC", new BigDecimal(80), paymentStatusCd));
			payments.add(createMockPolicyPayment(9L, 2L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "CSR", new BigDecimal(-25), paymentStatusCd));
			payments.get(6).setParentPolicyPaymentTransId(5L);
			payments.add(createMockPolicyPayment(10L, 2L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "CSR", new BigDecimal(50), paymentStatusCd));
	
			payments.add(createMockPolicyPayment(11L, 2L, "101", "R", "2015-03-01", "2015-03-01", "2015-03-31", "APTC", new BigDecimal(80), paymentStatusCd));
			payments.add(createMockPolicyPayment(12L, 2L, "101", "R", "2015-03-01", "2015-03-01", "2015-03-31", "CSR", new BigDecimal(50), paymentStatusCd));
			payments.add(createMockPolicyPayment(13L, 2L, "101", "R", "2015-03-01", "2015-03-01", "2015-03-31", "UF", new BigDecimal(100), paymentStatusCd));
			
		}
		policyDetailDTO.setPolicyPayments(payments);

		return policyDetailDTO;
	}

	public static PolicyDetailDTO createMockPolicyPaymentDataForRetroTerm_EndDtLTStartDt(String paymentStatusCd) {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		//Premiums
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		premiums.add(createMockPolicyPremium(3, "2015-01-01", "2014-12-31", new BigDecimal(50), new BigDecimal(25), new BigDecimal(100)));
		premiums.add(createMockPolicyPremium(3, "2015-02-01", "2014-12-31", new BigDecimal(80), new BigDecimal(50), new BigDecimal(100)));

		policyDetailDTO.setPolicyPremiums(premiums);

		//Payments
		List<PolicyPaymentTransDTO> payments = new ArrayList<PolicyPaymentTransDTO>();
		payments.add(createMockPolicyPayment(1L, 1L, "101", "R", "2015-01-01", "2015-01-01", "2015-01-31", "APTC", new BigDecimal(50), paymentStatusCd));
		payments.add(createMockPolicyPayment(2L, 1L, "101", "R", "2015-01-01", "2015-01-01", "2015-01-31", "CSR", new BigDecimal(25), paymentStatusCd));
		payments.add(createMockPolicyPayment(3L, 1L, "101", "R", "2015-01-01", "2015-01-01", "2015-01-31", "UF", new BigDecimal(100), paymentStatusCd));

		payments.add(createMockPolicyPayment(4L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "APTC", new BigDecimal(50), paymentStatusCd));
		payments.add(createMockPolicyPayment(5L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "CSR", new BigDecimal(25), paymentStatusCd));
		payments.add(createMockPolicyPayment(6L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "UF", new BigDecimal(100), paymentStatusCd));

		payments.add(createMockPolicyPayment(7L, 2L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "APTC", new BigDecimal(-50), paymentStatusCd));
		payments.get(6).setParentPolicyPaymentTransId(4L);
		payments.add(createMockPolicyPayment(8L, 2L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "APTC", new BigDecimal(80), paymentStatusCd));
		payments.add(createMockPolicyPayment(9L, 2L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "CSR", new BigDecimal(-25), paymentStatusCd));
		payments.get(8).setParentPolicyPaymentTransId(5L);
		payments.add(createMockPolicyPayment(10L, 2L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "CSR", new BigDecimal(50), paymentStatusCd));

		payments.add(createMockPolicyPayment(11L, 2L, "101", "R", "2015-03-01", "2015-03-01", "2015-03-31", "APTC", new BigDecimal(80), paymentStatusCd));
		payments.add(createMockPolicyPayment(12L, 2L, "101", "R", "2015-03-01", "2015-03-01", "2015-03-31", "CSR", new BigDecimal(50), paymentStatusCd));
		payments.add(createMockPolicyPayment(13L, 2L, "101", "R", "2015-03-01", "2015-03-01", "2015-03-31", "UF", new BigDecimal(100), paymentStatusCd));

		policyDetailDTO.setPolicyPayments(payments);

		return policyDetailDTO;
	}

	public static PolicyDetailDTO createMockPolicyPaymentDataForRetroFutureReversalTerm(String paymentStatusCd) {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		//Premiums
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		premiums.add(createMockPolicyPremium(2, "2014-10-01", null, new BigDecimal(40), new BigDecimal(50), new BigDecimal(80)));

		policyDetailDTO.setPolicyPremiums(premiums);

		//Payments
		List<PolicyPaymentTransDTO> payments = new ArrayList<PolicyPaymentTransDTO>();
		payments.add(createMockPolicyPayment(1L, 1L, "101", "R", "2014-10-01", "2014-10-01", "2014-10-31", "APTC", new BigDecimal(40), paymentStatusCd));
		payments.add(createMockPolicyPayment(2L, 1L, "101", "R", "2014-10-01", "2014-10-01", "2014-10-31", "CSR", new BigDecimal(50), paymentStatusCd));
		payments.add(createMockPolicyPayment(3L, 1L, "101", "R", "2014-10-01", "2014-10-01", "2014-10-31", "UF", new BigDecimal(80), paymentStatusCd));

		payments.add(createMockPolicyPayment(4L, 1L, "101", "R", "2014-11-01", "2014-11-01", "2014-11-30", "APTC", new BigDecimal(40), paymentStatusCd));
		payments.add(createMockPolicyPayment(5L, 1L, "101", "R", "2014-11-01", "2014-11-01", "2014-11-30", "CSR", new BigDecimal(50), paymentStatusCd));
		payments.add(createMockPolicyPayment(6L, 1L, "101", "R", "2014-11-01", "2014-11-01", "2014-11-30", "UF", new BigDecimal(80), paymentStatusCd));

		policyDetailDTO.setPolicyPayments(payments);

		return policyDetailDTO;
	}

	public static PolicyDetailDTO createMockPolicyPaymentDataForRetroCancel(String paymentStatusCd, boolean isFfm) {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		//Premiums
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		//premiums.add(createMockPolicyPremium(1, "2015-01-01", null, new BigDecimal(50), new BigDecimal(25), new BigDecimal(100)));
		premiums.add(createMockPolicyPremium(2, "2015-01-01", "2015-01-01", new BigDecimal(50), new BigDecimal(25), new BigDecimal(100)));

		policyDetailDTO.setPolicyPremiums(premiums);

		//Payments
		List<PolicyPaymentTransDTO> payments = new ArrayList<PolicyPaymentTransDTO>();
		payments.add(createMockPolicyPayment(1L, 1L, "201", "R", "2015-01-01", "2015-01-01", "2015-01-31", "APTC", new BigDecimal(50), paymentStatusCd));
		payments.add(createMockPolicyPayment(2L, 1L, "201", "R", "2015-01-01", "2015-01-01", "2015-01-31", "CSR", new BigDecimal(25), paymentStatusCd));
		if(isFfm) payments.add(createMockPolicyPayment(3L, 1L, "201", "R", "2015-01-01", "2015-01-01", "2015-01-31", "UF", new BigDecimal(100), paymentStatusCd));

		policyDetailDTO.setPolicyPayments(payments);

		return policyDetailDTO;
	}

	public static PolicyDetailDTO createMockPolicyPaymentDataForRetroCancel_Lastday() {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		//Premiums
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		//premiums.add(createMockPolicyPremium(1, "2015-01-01", null, new BigDecimal(50), new BigDecimal(25), new BigDecimal(100)));
		premiums.add(createMockPolicyPremium(2, "2015-01-31", "2015-01-31", new BigDecimal(50), new BigDecimal(25), new BigDecimal(100)));

		policyDetailDTO.setPolicyPremiums(premiums);

		//Payments
		List<PolicyPaymentTransDTO> payments = new ArrayList<PolicyPaymentTransDTO>();
		payments.add(createMockPolicyPayment(1L, 1L, "201", "R", "2015-01-01", "2015-01-01", "2015-01-31", "APTC", new BigDecimal(50), "APPV"));
		payments.add(createMockPolicyPayment(2L, 1L, "201", "R", "2015-01-01", "2015-01-01", "2015-01-31", "CSR", new BigDecimal(25), "APPV"));
		payments.add(createMockPolicyPayment(3L, 1L, "201", "R", "2015-01-01", "2015-01-01", "2015-01-31", "UF", new BigDecimal(100), "APPV"));

		policyDetailDTO.setPolicyPayments(payments);

		return policyDetailDTO;
	}

	public static PolicyDetailDTO createMockPolicyPaymentDataForPolicyStartDateChange(boolean isFfm) {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		//Premiums
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		//premiums.add(createMockPolicyPremium(1, "2015-01-01", null, new BigDecimal(50), new BigDecimal(25), new BigDecimal(100)));
		premiums.add(createMockPolicyPremium(2, "2015-06-30", "2015-10-31", new BigDecimal(88.61), new BigDecimal(41.58), new BigDecimal(189.01)));

		policyDetailDTO.setPolicyPremiums(premiums);

		//Payments
		List<PolicyPaymentTransDTO> payments = new ArrayList<PolicyPaymentTransDTO>();
		payments.add(createMockPolicyPayment(1L, 1L, "201", "R", "2015-06-01", "2015-06-01", "2015-06-30", "APTC", new BigDecimal(88.61), "APPV"));
		payments.add(createMockPolicyPayment(2L, 1L, "201", "R", "2015-06-01", "2015-06-01", "2015-06-30", "CSR", new BigDecimal(41.58), "APPV"));
		if(isFfm) payments.add(createMockPolicyPayment(3L, 1L, "201", "R", "2015-06-01", "2015-06-01", "2015-06-30", "UF", new BigDecimal(189.01), "APPV"));

		payments.add(createMockPolicyPayment(4L, 1L, "201", "R", "2015-07-01", "2015-07-01", "2015-07-31", "APTC", new BigDecimal(88.61), "APPV"));
		payments.add(createMockPolicyPayment(5L, 1L, "201", "R", "2015-07-01", "2015-07-01", "2015-07-31", "CSR", new BigDecimal(41.58), "APPV"));
		if(isFfm) payments.add(createMockPolicyPayment(6L, 1L, "201", "R", "2015-07-01", "2015-07-01", "2015-07-31", "UF", new BigDecimal(189.01), "APPV"));

		payments.add(createMockPolicyPayment(7L, 1L, "201", "R", "2015-08-01", "2015-08-01", "2015-08-31", "APTC", new BigDecimal(88.61), "APPV"));
		payments.add(createMockPolicyPayment(8L, 1L, "201", "R", "2015-08-01", "2015-08-01", "2015-08-31", "CSR", new BigDecimal(41.58), "APPV"));
		if(isFfm) payments.add(createMockPolicyPayment(9L, 1L, "201", "R", "2015-08-01", "2015-08-01", "2015-08-31", "UF", new BigDecimal(189.01), "APPV"));

		payments.add(createMockPolicyPayment(10L, 1L, "201", "R", "2015-09-01", "2015-09-01", "2015-09-30", "APTC", new BigDecimal(88.61), "APPV"));
		payments.add(createMockPolicyPayment(11L, 1L, "201", "R", "2015-09-01", "2015-09-01", "2015-09-30", "CSR", new BigDecimal(41.58), "APPV"));
		if(isFfm) payments.add(createMockPolicyPayment(12L, 1L, "201", "R", "2015-09-01", "2015-09-01", "2015-09-30", "UF", new BigDecimal(189.01), "APPV"));

		payments.add(createMockPolicyPayment(13L, 1L, "201", "R", "2015-10-01", "2015-10-01", "2015-10-31", "APTC", new BigDecimal(88.61), "APPV"));
		payments.add(createMockPolicyPayment(14L, 1L, "201", "R", "2015-10-01", "2015-10-01", "2015-10-31", "CSR", new BigDecimal(41.58), "APPV"));
		if(isFfm) payments.add(createMockPolicyPayment(15L, 1L, "201", "R", "2015-10-01", "2015-10-01", "2015-10-31", "UF", new BigDecimal(189.01), "APPV"));

		policyDetailDTO.setPolicyPayments(payments);

		return policyDetailDTO;
	}

	public static PolicyDetailDTO createMockPolicyPaymentDataForRetroChangePastPeriod(String paymentStatusCd, boolean isFfm) {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		//Premiums
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		premiums.add(createMockPolicyPremium(5, "2015-01-01", "2015-01-31", new BigDecimal(70), new BigDecimal(35), new BigDecimal(100)));
		premiums.add(createMockPolicyPremium(5, "2015-02-01", null, new BigDecimal(80), new BigDecimal(50), new BigDecimal(100)));

		policyDetailDTO.setPolicyPremiums(premiums);

		//Payments
		List<PolicyPaymentTransDTO> payments = new ArrayList<PolicyPaymentTransDTO>();
		
		if(isFfm) {
			payments.add(createMockPolicyPayment(1L, 1L, "101", "R", "2015-01-01", "2015-01-01", "2015-01-31", "APTC", new BigDecimal(50), paymentStatusCd));
			payments.add(createMockPolicyPayment(2L, 1L, "101", "R", "2015-01-01", "2015-01-01", "2015-01-31", "CSR", new BigDecimal(25), paymentStatusCd));
			payments.add(createMockPolicyPayment(3L, 1L, "101", "R", "2015-01-01", "2015-01-01", "2015-01-31", "UF", new BigDecimal(100), paymentStatusCd));
	
			payments.add(createMockPolicyPayment(4L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "APTC", new BigDecimal(50), paymentStatusCd));
			payments.add(createMockPolicyPayment(5L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "CSR", new BigDecimal(25), paymentStatusCd));
			payments.add(createMockPolicyPayment(6L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "UF", new BigDecimal(100), paymentStatusCd));
	
			payments.add(createMockPolicyPayment(7L, 2L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "APTC", new BigDecimal(-50), paymentStatusCd));
			payments.get(6).setParentPolicyPaymentTransId(4L);
			payments.add(createMockPolicyPayment(8L, 2L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "APTC", new BigDecimal(80), paymentStatusCd));
			payments.add(createMockPolicyPayment(9L, 2L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "CSR", new BigDecimal(-25), paymentStatusCd));
			payments.get(8).setParentPolicyPaymentTransId(5L);
			payments.add(createMockPolicyPayment(10L, 2L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "CSR", new BigDecimal(50), paymentStatusCd));
	
			payments.add(createMockPolicyPayment(11L, 2L, "101", "R", "2015-03-01", "2015-03-01", "2015-03-31", "APTC", new BigDecimal(80), paymentStatusCd));
			payments.add(createMockPolicyPayment(12L, 2L, "101", "R", "2015-03-01", "2015-03-01", "2015-03-31", "CSR", new BigDecimal(50), paymentStatusCd));
			payments.add(createMockPolicyPayment(13L, 2L, "101", "R", "2015-03-01", "2015-03-01", "2015-03-31", "UF", new BigDecimal(100), paymentStatusCd));
	
			payments.add(createMockPolicyPayment(14L, 3L, "101", "R", "2015-03-01", "2015-03-01", "2015-03-31", "APTC", new BigDecimal(-80), paymentStatusCd));
			payments.get(13).setParentPolicyPaymentTransId(11L);
			payments.add(createMockPolicyPayment(15L, 3L, "101", "R", "2015-03-01", "2015-03-01", "2015-03-31", "CSR", new BigDecimal(-50), paymentStatusCd));
			payments.get(14).setParentPolicyPaymentTransId(12L);
			payments.add(createMockPolicyPayment(16L, 3L, "101", "R", "2015-03-01", "2015-03-01", "2015-03-31", "UF", new BigDecimal(-100), paymentStatusCd));
			payments.get(15).setParentPolicyPaymentTransId(13L);
	
			payments.add(createMockPolicyPayment(17L, 4L, "101", "R", "2015-03-01", "2015-03-01", "2015-03-31", "APTC", new BigDecimal(80), paymentStatusCd));
			payments.add(createMockPolicyPayment(18L, 4L, "101", "R", "2015-03-01", "2015-03-01", "2015-03-31", "CSR", new BigDecimal(50), paymentStatusCd));
			payments.add(createMockPolicyPayment(19L, 4L, "101", "R", "2015-03-01", "2015-03-01", "2015-03-31", "UF", new BigDecimal(100), paymentStatusCd));
	
			payments.add(createMockPolicyPayment(20L, 4L, "101", "R", "2015-04-01", "2015-04-01", "2015-04-30", "APTC", new BigDecimal(80), paymentStatusCd));
			payments.add(createMockPolicyPayment(20L, 4L, "101", "R", "2015-04-01", "2015-04-01", "2015-04-30", "CSR", new BigDecimal(50), paymentStatusCd));
			payments.add(createMockPolicyPayment(21L, 4L, "101", "R", "2015-04-01", "2015-04-01", "2015-04-30", "UF", new BigDecimal(100), paymentStatusCd));
		
		} else {
			payments.add(createMockPolicyPayment(1L, 1L, "101", "R", "2015-01-01", "2015-01-01", "2015-01-31", "APTC", new BigDecimal(50), paymentStatusCd));
			payments.add(createMockPolicyPayment(2L, 1L, "101", "R", "2015-01-01", "2015-01-01", "2015-01-31", "CSR", new BigDecimal(25), paymentStatusCd));
	
			payments.add(createMockPolicyPayment(4L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "APTC", new BigDecimal(50), paymentStatusCd));
			payments.add(createMockPolicyPayment(5L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "CSR", new BigDecimal(25), paymentStatusCd));
	
			payments.add(createMockPolicyPayment(7L, 2L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "APTC", new BigDecimal(-50), paymentStatusCd));
			payments.get(4).setParentPolicyPaymentTransId(4L);
			payments.add(createMockPolicyPayment(8L, 2L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "APTC", new BigDecimal(80), paymentStatusCd));
			payments.add(createMockPolicyPayment(9L, 2L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "CSR", new BigDecimal(-25), paymentStatusCd));
			payments.get(6).setParentPolicyPaymentTransId(5L);
			payments.add(createMockPolicyPayment(10L, 2L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "CSR", new BigDecimal(50), paymentStatusCd));
	
			payments.add(createMockPolicyPayment(11L, 2L, "101", "R", "2015-03-01", "2015-03-01", "2015-03-31", "APTC", new BigDecimal(80), paymentStatusCd));
			payments.add(createMockPolicyPayment(12L, 2L, "101", "R", "2015-03-01", "2015-03-01", "2015-03-31", "CSR", new BigDecimal(50), paymentStatusCd));
	
			payments.add(createMockPolicyPayment(14L, 3L, "101", "R", "2015-03-01", "2015-03-01", "2015-03-31", "APTC", new BigDecimal(-80), paymentStatusCd));
			payments.get(10).setParentPolicyPaymentTransId(11L);
			payments.add(createMockPolicyPayment(15L, 3L, "101", "R", "2015-03-01", "2015-03-01", "2015-03-31", "CSR", new BigDecimal(-50), paymentStatusCd));
			payments.get(11).setParentPolicyPaymentTransId(12L);
	
			payments.add(createMockPolicyPayment(17L, 4L, "101", "R", "2015-03-01", "2015-03-01", "2015-03-31", "APTC", new BigDecimal(80), paymentStatusCd));
			payments.add(createMockPolicyPayment(18L, 4L, "101", "R", "2015-03-01", "2015-03-01", "2015-03-31", "CSR", new BigDecimal(50), paymentStatusCd));
	
			payments.add(createMockPolicyPayment(20L, 4L, "101", "R", "2015-04-01", "2015-04-01", "2015-04-30", "APTC", new BigDecimal(80), paymentStatusCd));
			payments.add(createMockPolicyPayment(20L, 4L, "101", "R", "2015-04-01", "2015-04-01", "2015-04-30", "CSR", new BigDecimal(50), paymentStatusCd));
		}
		
		policyDetailDTO.setPolicyPayments(payments);

		return policyDetailDTO;
	}

	public static PolicyDetailDTO createMockPolicyPaymentDataForRetroReinstatement(String paymentStatusCd, boolean isFfm) {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		//Premiums
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		premiums.add(createMockPolicyPremium(4, "2015-01-01", "2015-01-31", new BigDecimal(50), new BigDecimal(25), new BigDecimal(100)));
		premiums.add(createMockPolicyPremium(4, "2015-02-01", null, new BigDecimal(80), new BigDecimal(50), new BigDecimal(100)));

		policyDetailDTO.setPolicyPremiums(premiums);

		//Payments
		List<PolicyPaymentTransDTO> payments = new ArrayList<PolicyPaymentTransDTO>();
		
		if(isFfm) {
			payments.add(createMockPolicyPayment(1L, 1L, "101", "R", "2015-01-01", "2015-01-01", "2015-01-31", "APTC", new BigDecimal(50), paymentStatusCd));
			payments.add(createMockPolicyPayment(2L, 1L, "101", "R", "2015-01-01", "2015-01-01", "2015-01-31", "CSR", new BigDecimal(25), paymentStatusCd));
			payments.add(createMockPolicyPayment(3L, 1L, "101", "R", "2015-01-01", "2015-01-01", "2015-01-31", "UF", new BigDecimal(100), paymentStatusCd));
	
			payments.add(createMockPolicyPayment(4L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "APTC", new BigDecimal(50), paymentStatusCd));
			payments.add(createMockPolicyPayment(5L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "CSR", new BigDecimal(25), paymentStatusCd));
			payments.add(createMockPolicyPayment(6L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "UF", new BigDecimal(100), paymentStatusCd));
	
			payments.add(createMockPolicyPayment(7L, 2L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "APTC", new BigDecimal(-50), paymentStatusCd));
			payments.get(6).setParentPolicyPaymentTransId(4L);
			payments.add(createMockPolicyPayment(8L, 2L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "APTC", new BigDecimal(80), paymentStatusCd));
			payments.add(createMockPolicyPayment(9L, 2L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "CSR", new BigDecimal(-25), paymentStatusCd));
			payments.get(8).setParentPolicyPaymentTransId(5L);
			payments.add(createMockPolicyPayment(10L, 2L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "CSR", new BigDecimal(50), paymentStatusCd));
	
			payments.add(createMockPolicyPayment(11L, 2L, "101", "R", "2015-03-01", "2015-03-01", "2015-03-31", "APTC", new BigDecimal(80), paymentStatusCd));
			payments.add(createMockPolicyPayment(12L, 2L, "101", "R", "2015-03-01", "2015-03-01", "2015-03-31", "CSR", new BigDecimal(50), paymentStatusCd));
			payments.add(createMockPolicyPayment(13L, 2L, "101", "R", "2015-03-01", "2015-03-01", "2015-03-31", "UF", new BigDecimal(100), paymentStatusCd));
	
			payments.add(createMockPolicyPayment(14L, 3L, "101", "R", "2015-03-01", "2015-03-01", "2015-03-31", "APTC", new BigDecimal(-80), paymentStatusCd));
			payments.get(13).setParentPolicyPaymentTransId(11L);
			payments.add(createMockPolicyPayment(15L, 3L, "101", "R", "2015-03-01", "2015-03-01", "2015-03-31", "CSR", new BigDecimal(-50), paymentStatusCd));
			payments.get(14).setParentPolicyPaymentTransId(12L);
			payments.add(createMockPolicyPayment(16L, 3L, "101", "R", "2015-03-01", "2015-03-01", "2015-03-31", "UF", new BigDecimal(-100), paymentStatusCd));
			payments.get(15).setParentPolicyPaymentTransId(13L);
		
		} else {
			payments.add(createMockPolicyPayment(1L, 1L, "101", "R", "2015-01-01", "2015-01-01", "2015-01-31", "APTC", new BigDecimal(50), paymentStatusCd));
			payments.add(createMockPolicyPayment(2L, 1L, "101", "R", "2015-01-01", "2015-01-01", "2015-01-31", "CSR", new BigDecimal(25), paymentStatusCd));
	
			payments.add(createMockPolicyPayment(4L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "APTC", new BigDecimal(50), paymentStatusCd));
			payments.add(createMockPolicyPayment(5L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "CSR", new BigDecimal(25), paymentStatusCd));
	
			payments.add(createMockPolicyPayment(7L, 2L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "APTC", new BigDecimal(-50), paymentStatusCd));
			payments.get(4).setParentPolicyPaymentTransId(4L);
			payments.add(createMockPolicyPayment(8L, 2L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "APTC", new BigDecimal(80), paymentStatusCd));
			payments.add(createMockPolicyPayment(9L, 2L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "CSR", new BigDecimal(-25), paymentStatusCd));
			payments.get(6).setParentPolicyPaymentTransId(5L);
			payments.add(createMockPolicyPayment(10L, 2L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "CSR", new BigDecimal(50), paymentStatusCd));
	
			payments.add(createMockPolicyPayment(11L, 2L, "101", "R", "2015-03-01", "2015-03-01", "2015-03-31", "APTC", new BigDecimal(80), paymentStatusCd));
			payments.add(createMockPolicyPayment(12L, 2L, "101", "R", "2015-03-01", "2015-03-01", "2015-03-31", "CSR", new BigDecimal(50), paymentStatusCd));
	
			payments.add(createMockPolicyPayment(14L, 3L, "101", "R", "2015-03-01", "2015-03-01", "2015-03-31", "APTC", new BigDecimal(-80), paymentStatusCd));
			payments.get(10).setParentPolicyPaymentTransId(11L);
			payments.add(createMockPolicyPayment(15L, 3L, "101", "R", "2015-03-01", "2015-03-01", "2015-03-31", "CSR", new BigDecimal(-50), paymentStatusCd));
			payments.get(11).setParentPolicyPaymentTransId(12L);
		}
		
		policyDetailDTO.setPolicyPayments(payments);

		return policyDetailDTO;
	}

	public static PolicyDetailDTO createMockPolicyPaymentDataForRetroChangeMultiple(boolean isFfm) {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		//Premiums
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		premiums.add(createMockPolicyPremium(5, "2015-01-01", "2015-01-31", new BigDecimal(70), new BigDecimal(35), new BigDecimal(100)));
		premiums.add(createMockPolicyPremium(5, "2015-02-01", "2015-03-31", new BigDecimal(80), new BigDecimal(50), new BigDecimal(100)));
		premiums.add(createMockPolicyPremium(5, "2015-04-01", null, new BigDecimal(100), new BigDecimal(70), new BigDecimal(100)));

		policyDetailDTO.setPolicyPremiums(premiums);

		//Payments
		List<PolicyPaymentTransDTO> payments = new ArrayList<PolicyPaymentTransDTO>();
		
		if(isFfm) {
			payments.add(createMockPolicyPayment(1L, 1L, "101", "R", "2015-01-01", "2015-01-01", "2015-01-31", "APTC", new BigDecimal(50), "APPV"));
			payments.add(createMockPolicyPayment(2L, 1L, "101", "R", "2015-01-01", "2015-01-01", "2015-01-31", "CSR", new BigDecimal(25), "APPV"));
			payments.add(createMockPolicyPayment(3L, 1L, "101", "R", "2015-01-01", "2015-01-01", "2015-01-31", "UF", new BigDecimal(100), "APPV"));
	
			payments.add(createMockPolicyPayment(4L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "APTC", new BigDecimal(50), "APPV"));
			payments.add(createMockPolicyPayment(5L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "CSR", new BigDecimal(25), "APPV"));
			payments.add(createMockPolicyPayment(6L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "UF", new BigDecimal(100), "APPV"));
	
			payments.add(createMockPolicyPayment(7L, 2L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "APTC", new BigDecimal(-50), "APPV"));
			payments.get(6).setParentPolicyPaymentTransId(4L);
			payments.add(createMockPolicyPayment(8L, 2L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "APTC", new BigDecimal(80), "APPV"));
			payments.add(createMockPolicyPayment(9L, 2L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "CSR", new BigDecimal(-25), "APPV"));
			payments.get(8).setParentPolicyPaymentTransId(5L);
			payments.add(createMockPolicyPayment(10L, 2L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "CSR", new BigDecimal(50), "APPV"));
	
			payments.add(createMockPolicyPayment(11L, 2L, "101", "R", "2015-03-01", "2015-03-01", "2015-03-31", "APTC", new BigDecimal(80), "APPV"));
			payments.add(createMockPolicyPayment(12L, 2L, "101", "R", "2015-03-01", "2015-03-01", "2015-03-31", "CSR", new BigDecimal(50), "APPV"));
			payments.add(createMockPolicyPayment(13L, 2L, "101", "R", "2015-03-01", "2015-03-01", "2015-03-31", "UF", new BigDecimal(100), "APPV"));
	
			payments.add(createMockPolicyPayment(14L, 3L, "101", "R", "2015-03-01", "2015-03-01", "2015-03-31", "APTC", new BigDecimal(-80), "APPV"));
			payments.get(13).setParentPolicyPaymentTransId(11L);
			payments.add(createMockPolicyPayment(15L, 3L, "101", "R", "2015-03-01", "2015-03-01", "2015-03-31", "CSR", new BigDecimal(-50), "APPV"));
			payments.get(14).setParentPolicyPaymentTransId(12L);
			payments.add(createMockPolicyPayment(16L, 3L, "101", "R", "2015-03-01", "2015-03-01", "2015-03-31", "UF", new BigDecimal(-100), "APPV"));
			payments.get(15).setParentPolicyPaymentTransId(13L);
	
			payments.add(createMockPolicyPayment(17L, 4L, "101", "R", "2015-03-01", "2015-03-01", "2015-03-31", "APTC", new BigDecimal(80), "PCYC"));
			payments.add(createMockPolicyPayment(18L, 4L, "101", "R", "2015-03-01", "2015-03-01", "2015-03-31", "CSR", new BigDecimal(50), "PCYC"));
			payments.add(createMockPolicyPayment(19L, 4L, "101", "R", "2015-03-01", "2015-03-01", "2015-03-31", "UF", new BigDecimal(100), "PCYC"));
	
			payments.add(createMockPolicyPayment(20L, 4L, "101", "R", "2015-04-01", "2015-04-01", "2015-04-30", "APTC", new BigDecimal(80), "PCYC"));
			payments.add(createMockPolicyPayment(21L, 4L, "101", "R", "2015-04-01", "2015-04-01", "2015-04-30", "CSR", new BigDecimal(50), "PCYC"));
			payments.add(createMockPolicyPayment(22L, 4L, "101", "R", "2015-04-01", "2015-04-01", "2015-04-30", "UF", new BigDecimal(100), "PCYC"));
	
			payments.add(createMockPolicyPayment(23L, 5L, "101", "R", "2015-01-01", "2015-01-01", "2015-01-31", "APTC", new BigDecimal(-50), "PCYC"));
			payments.get(22).setParentPolicyPaymentTransId(1L);
			payments.add(createMockPolicyPayment(24L, 5L, "101", "R", "2015-01-01", "2015-01-01", "2015-01-31", "APTC", new BigDecimal(70), "PCYC"));
			payments.add(createMockPolicyPayment(25L, 5L, "101", "R", "2015-01-01", "2015-01-01", "2015-01-31", "CSR", new BigDecimal(-25), "PCYC"));
			payments.get(24).setParentPolicyPaymentTransId(2L);
			payments.add(createMockPolicyPayment(26L, 5L, "101", "R", "2015-01-01", "2015-01-01", "2015-01-31", "CSR", new BigDecimal(35), "PCYC"));

		} else {
			payments.add(createMockPolicyPayment(1L, 1L, "101", "R", "2015-01-01", "2015-01-01", "2015-01-31", "APTC", new BigDecimal(50), "APPV"));
			payments.add(createMockPolicyPayment(2L, 1L, "101", "R", "2015-01-01", "2015-01-01", "2015-01-31", "CSR", new BigDecimal(25), "APPV"));
	
			payments.add(createMockPolicyPayment(4L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "APTC", new BigDecimal(50), "APPV"));
			payments.add(createMockPolicyPayment(5L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "CSR", new BigDecimal(25), "APPV"));
	
			payments.add(createMockPolicyPayment(7L, 2L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "APTC", new BigDecimal(-50), "APPV"));
			payments.get(4).setParentPolicyPaymentTransId(4L);
			payments.add(createMockPolicyPayment(8L, 2L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "APTC", new BigDecimal(80), "APPV"));
			payments.add(createMockPolicyPayment(9L, 2L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "CSR", new BigDecimal(-25), "APPV"));
			payments.get(6).setParentPolicyPaymentTransId(5L);
			payments.add(createMockPolicyPayment(10L, 2L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "CSR", new BigDecimal(50), "APPV"));
	
			payments.add(createMockPolicyPayment(11L, 2L, "101", "R", "2015-03-01", "2015-03-01", "2015-03-31", "APTC", new BigDecimal(80), "APPV"));
			payments.add(createMockPolicyPayment(12L, 2L, "101", "R", "2015-03-01", "2015-03-01", "2015-03-31", "CSR", new BigDecimal(50), "APPV"));
	
			payments.add(createMockPolicyPayment(14L, 3L, "101", "R", "2015-03-01", "2015-03-01", "2015-03-31", "APTC", new BigDecimal(-80), "APPV"));
			payments.get(10).setParentPolicyPaymentTransId(11L);
			payments.add(createMockPolicyPayment(15L, 3L, "101", "R", "2015-03-01", "2015-03-01", "2015-03-31", "CSR", new BigDecimal(-50), "APPV"));
			payments.get(11).setParentPolicyPaymentTransId(12L);
	
			payments.add(createMockPolicyPayment(17L, 4L, "101", "R", "2015-03-01", "2015-03-01", "2015-03-31", "APTC", new BigDecimal(80), "PCYC"));
			payments.add(createMockPolicyPayment(18L, 4L, "101", "R", "2015-03-01", "2015-03-01", "2015-03-31", "CSR", new BigDecimal(50), "PCYC"));
	
			payments.add(createMockPolicyPayment(20L, 4L, "101", "R", "2015-04-01", "2015-04-01", "2015-04-30", "APTC", new BigDecimal(80), "PCYC"));
			payments.add(createMockPolicyPayment(21L, 4L, "101", "R", "2015-04-01", "2015-04-01", "2015-04-30", "CSR", new BigDecimal(50), "PCYC"));
	
			payments.add(createMockPolicyPayment(23L, 5L, "101", "R", "2015-01-01", "2015-01-01", "2015-01-31", "APTC", new BigDecimal(-50), "PCYC"));
			payments.get(16).setParentPolicyPaymentTransId(1L);
			payments.add(createMockPolicyPayment(24L, 5L, "101", "R", "2015-01-01", "2015-01-01", "2015-01-31", "APTC", new BigDecimal(70), "PCYC"));
			payments.add(createMockPolicyPayment(25L, 5L, "101", "R", "2015-01-01", "2015-01-01", "2015-01-31", "CSR", new BigDecimal(-25), "PCYC"));
			payments.get(18).setParentPolicyPaymentTransId(2L);
			payments.add(createMockPolicyPayment(26L, 5L, "101", "R", "2015-01-01", "2015-01-01", "2015-01-31", "CSR", new BigDecimal(35), "PCYC"));

		}
		
		policyDetailDTO.setPolicyPayments(payments);

		return policyDetailDTO;
	}

	public static PolicyDetailDTO createMockPolicyPaymentDataForRetroChangeMultipleAllAmts() {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		//Premiums
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		premiums.add(createMockPolicyPremium(5, "2015-01-01", "2015-01-31", new BigDecimal(70), new BigDecimal(35), new BigDecimal(140)));
		premiums.add(createMockPolicyPremium(5, "2015-02-01", "2015-03-31", new BigDecimal(80), new BigDecimal(50), new BigDecimal(160)));
		premiums.add(createMockPolicyPremium(5, "2015-04-01", null, new BigDecimal(100), new BigDecimal(70), new BigDecimal(200)));

		policyDetailDTO.setPolicyPremiums(premiums);

		//Payments
		List<PolicyPaymentTransDTO> payments = new ArrayList<PolicyPaymentTransDTO>();
		payments.add(createMockPolicyPayment(1L, 1L, "101", "R", "2015-01-01", "2015-01-01", "2015-01-31", "APTC", new BigDecimal(50), "APPV"));
		payments.add(createMockPolicyPayment(2L, 1L, "101", "R", "2015-01-01", "2015-01-01", "2015-01-31", "CSR", new BigDecimal(25), "APPV"));
		payments.add(createMockPolicyPayment(3L, 1L, "101", "R", "2015-01-01", "2015-01-01", "2015-01-31", "UF", new BigDecimal(100), "APPV"));

		payments.add(createMockPolicyPayment(4L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "APTC", new BigDecimal(50), "APPV"));
		payments.add(createMockPolicyPayment(5L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "CSR", new BigDecimal(25), "APPV"));
		payments.add(createMockPolicyPayment(6L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "UF", new BigDecimal(100), "APPV"));

		payments.add(createMockPolicyPayment(7L, 2L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "APTC", new BigDecimal(-50), "APPV"));
		payments.get(6).setParentPolicyPaymentTransId(4L);
		payments.add(createMockPolicyPayment(8L, 2L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "APTC", new BigDecimal(80), "APPV"));
		payments.add(createMockPolicyPayment(9L, 2L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "CSR", new BigDecimal(-25), "APPV"));
		payments.get(8).setParentPolicyPaymentTransId(5L);
		payments.add(createMockPolicyPayment(10L, 2L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "CSR", new BigDecimal(50), "APPV"));
		payments.add(createMockPolicyPayment(11L, 2L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "UF", new BigDecimal(-100), "APPV"));
		payments.get(10).setParentPolicyPaymentTransId(6L);
		payments.add(createMockPolicyPayment(12L, 2L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "UF", new BigDecimal(160), "APPV"));

		payments.add(createMockPolicyPayment(13L, 2L, "101", "R", "2015-03-01", "2015-03-01", "2015-03-31", "APTC", new BigDecimal(80), "APPV"));
		payments.add(createMockPolicyPayment(14L, 2L, "101", "R", "2015-03-01", "2015-03-01", "2015-03-31", "CSR", new BigDecimal(50), "APPV"));
		payments.add(createMockPolicyPayment(15L, 2L, "101", "R", "2015-03-01", "2015-03-01", "2015-03-31", "UF", new BigDecimal(160), "APPV"));

		payments.add(createMockPolicyPayment(16L, 3L, "101", "R", "2015-03-01", "2015-03-01", "2015-03-31", "APTC", new BigDecimal(-80), "APPV"));
		payments.get(15).setParentPolicyPaymentTransId(13L);
		payments.add(createMockPolicyPayment(17L, 3L, "101", "R", "2015-03-01", "2015-03-01", "2015-03-31", "CSR", new BigDecimal(-50), "APPV"));
		payments.get(16).setParentPolicyPaymentTransId(14L);
		payments.add(createMockPolicyPayment(18L, 3L, "101", "R", "2015-03-01", "2015-03-01", "2015-03-31", "UF", new BigDecimal(-160), "APPV"));
		payments.get(17).setParentPolicyPaymentTransId(15L);

		payments.add(createMockPolicyPayment(19L, 4L, "101", "R", "2015-03-01", "2015-03-01", "2015-03-31", "APTC", new BigDecimal(80), "PCYC"));
		payments.add(createMockPolicyPayment(20L, 4L, "101", "R", "2015-03-01", "2015-03-01", "2015-03-31", "CSR", new BigDecimal(50), "PCYC"));
		payments.add(createMockPolicyPayment(21L, 4L, "101", "R", "2015-03-01", "2015-03-01", "2015-03-31", "UF", new BigDecimal(160), "PCYC"));

		payments.add(createMockPolicyPayment(22L, 4L, "101", "R", "2015-04-01", "2015-04-01", "2015-04-30", "APTC", new BigDecimal(80), "PCYC"));
		payments.add(createMockPolicyPayment(23L, 4L, "101", "R", "2015-04-01", "2015-04-01", "2015-04-30", "CSR", new BigDecimal(50), "PCYC"));
		payments.add(createMockPolicyPayment(24L, 4L, "101", "R", "2015-04-01", "2015-04-01", "2015-04-30", "UF", new BigDecimal(160), "PCYC"));

		payments.add(createMockPolicyPayment(25L, 5L, "101", "R", "2015-01-01", "2015-01-01", "2015-01-31", "APTC", new BigDecimal(-50), "PCYC"));
		payments.get(24).setParentPolicyPaymentTransId(1L);
		payments.add(createMockPolicyPayment(26L, 5L, "101", "R", "2015-01-01", "2015-01-01", "2015-01-31", "APTC", new BigDecimal(70), "PCYC"));
		payments.add(createMockPolicyPayment(27L, 5L, "101", "R", "2015-01-01", "2015-01-01", "2015-01-31", "CSR", new BigDecimal(-25), "PCYC"));
		payments.get(26).setParentPolicyPaymentTransId(2L);
		payments.add(createMockPolicyPayment(28L, 5L, "101", "R", "2015-01-01", "2015-01-01", "2015-01-31", "CSR", new BigDecimal(35), "PCYC"));
		payments.add(createMockPolicyPayment(29L, 5L, "101", "R", "2015-01-01", "2015-01-01", "2015-01-31", "UF", new BigDecimal(-100), "PCYC"));
		payments.get(28).setParentPolicyPaymentTransId(3L);
		payments.add(createMockPolicyPayment(30L, 5L, "101", "R", "2015-01-01", "2015-01-01", "2015-01-31", "UF", new BigDecimal(140), "PCYC"));

		policyDetailDTO.setPolicyPayments(payments);

		return policyDetailDTO;
	}

	public static PolicyDetailDTO createMockPolicyPaymentDataForRetroChangeInCircums() {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		premiums.add(createMockPolicyPremium(1, "2015-02-22", null, new BigDecimal(150), new BigDecimal(75), new BigDecimal(200)));

		policyDetailDTO.setPolicyPremiums(premiums);

		return policyDetailDTO;
	}

	public static PolicyDetailDTO createMockPolicyPaymentDataForRapFreeze() {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		premiums.add(createMockPolicyPremium(2, "2015-03-01", null, new BigDecimal(180), new BigDecimal(80), new BigDecimal(200)));

		policyDetailDTO.setPolicyPremiums(premiums);

		return policyDetailDTO;
	}

	public static PolicyDetailDTO createMockPolicyPaymentDataForRetroPartialMonth_Scenario1() {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		premiums.add(createMockPolicyPremium(1, "2015-01-01", "2015-02-15", new BigDecimal(100), new BigDecimal(50), new BigDecimal(150)));

		policyDetailDTO.setPolicyPremiums(premiums);

		//Payments
		List<PolicyPaymentTransDTO> payments = new ArrayList<PolicyPaymentTransDTO>();
		payments.add(createMockPolicyPayment(1L, 1L, "101", "P", "2015-01-01", "2015-01-01", "2015-01-31", "APTC", new BigDecimal("100.00"), "APPV"));
		payments.add(createMockPolicyPayment(2L, 1L, "101", "P", "2015-01-01", "2015-01-01", "2015-01-31", "CSR", new BigDecimal("50.00"), "APPV"));
		payments.add(createMockPolicyPayment(3L, 1L, "101", "P", "2015-01-01", "2015-01-01", "2015-01-31", "UF", new BigDecimal("150.00"), "APPV"));
		payments.get(2).setPaymentAmount(new BigDecimal("-5.25"));

		payments.add(createMockPolicyPayment(4L, 1L, "101", "P", "2015-02-01", "2015-02-01", "2015-02-28", "APTC", new BigDecimal("100.00"), "APPV"));
		payments.add(createMockPolicyPayment(5L, 1L, "101", "P", "2015-02-01", "2015-02-01", "2015-02-28", "CSR", new BigDecimal("50.00"), "APPV"));
		payments.add(createMockPolicyPayment(6L, 1L, "101", "P", "2015-02-01", "2015-02-01", "2015-02-28", "UF", new BigDecimal("150.00"), "APPV"));
		payments.get(5).setPaymentAmount(new BigDecimal("-5.25"));

		policyDetailDTO.setPolicyPayments(payments);


		return policyDetailDTO;
	}

	public static PolicyDetailDTO createMockPolicyPaymentDataForRetroPartialMonth_Scenario1_FFM() {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		premiums.add(createMockPolicyPremium(1, "2015-01-01", "2015-01-31", new BigDecimal(100), new BigDecimal(50), new BigDecimal(150)));
		PolicyPremium premium1 = createMockPolicyPremium(1, "2015-02-01", "2015-02-14", new BigDecimal(200), new BigDecimal(50), new BigDecimal(250));
		premium1.setProratedAptcAmount(new BigDecimal("50.00"));
		premium1.setProratedCsrAmount(new BigDecimal("25.00"));
		premium1.setProratedPremiumAmount(new BigDecimal("75.00"));
		premiums.add(premium1);

		policyDetailDTO.setPolicyPremiums(premiums);

		//Payments
		List<PolicyPaymentTransDTO> payments = new ArrayList<PolicyPaymentTransDTO>();
		payments.add(createMockPolicyPayment(1L, 1L, "101", "P", "2015-01-01", "2015-01-01", "2015-01-31", "APTC", new BigDecimal("100.00"), "APPV"));
		payments.add(createMockPolicyPayment(2L, 1L, "101", "P", "2015-01-01", "2015-01-01", "2015-01-31", "CSR", new BigDecimal("50.00"), "APPV"));
		payments.add(createMockPolicyPayment(3L, 1L, "101", "P", "2015-01-01", "2015-01-01", "2015-01-31", "UF", new BigDecimal("150.00"), "APPV"));
		payments.get(2).setPaymentAmount(new BigDecimal("-5.25"));

		payments.add(createMockPolicyPayment(4L, 1L, "101", "P", "2015-02-01", "2015-02-01", "2015-02-28", "APTC", new BigDecimal("100.00"), "APPV"));
		payments.add(createMockPolicyPayment(5L, 1L, "101", "P", "2015-02-01", "2015-02-01", "2015-02-28", "CSR", new BigDecimal("50.00"), "APPV"));
		payments.add(createMockPolicyPayment(6L, 1L, "101", "P", "2015-02-01", "2015-02-01", "2015-02-28", "UF", new BigDecimal("150.00"), "APPV"));
		payments.get(5).setPaymentAmount(new BigDecimal("-5.25"));

		policyDetailDTO.setPolicyPayments(payments);


		return policyDetailDTO;
	}

	public static PolicyDetailDTO createMockPolicyPaymentDataForRetroPartialMonth_Scenario1A_FFM() {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		premiums.add(createMockPolicyPremium(1, "2015-01-01", "2015-01-31", new BigDecimal(100), new BigDecimal(50), new BigDecimal(150)));
		PolicyPremium premium1 = createMockPolicyPremium(1, "2015-02-01", "2015-02-14", new BigDecimal(200), new BigDecimal(50), new BigDecimal(250));
		premium1.setProratedAptcAmount(new BigDecimal("50.00"));
		premium1.setProratedCsrAmount(new BigDecimal("25.00"));
		premium1.setProratedPremiumAmount(new BigDecimal("75.00"));
		premiums.add(premium1);

		policyDetailDTO.setPolicyPremiums(premiums);

		return policyDetailDTO;
	}

	public static PolicyDetailDTO createMockPolicyPaymentDataForRetroPartialMonth_Scenario1B_Cancel_FFM() {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		premiums.add(createMockPolicyPremium(1, "2015-01-01", "2015-01-01", new BigDecimal(100), new BigDecimal(50), new BigDecimal(150)));
		policyDetailDTO.setPolicyPremiums(premiums);

		return policyDetailDTO;
	}

	public static PolicyDetailDTO createMockPolicyPaymentDataForRetroPartialMonth_Scenario1B_FFM() {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		premiums.add(createMockPolicyPremium(1, "2015-01-01", "2015-01-01", new BigDecimal(100), new BigDecimal(50), new BigDecimal(150)));
		policyDetailDTO.setPolicyPremiums(premiums);

		return policyDetailDTO;
	}

	public static PolicyDetailDTO createMockPolicyPaymentDataForRetroPartialMonth_Scenario1C_FFM() {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		premiums.add(createMockPolicyPremium(1, "2015-01-01", "2015-03-01", new BigDecimal(100), new BigDecimal(50), new BigDecimal(150)));
		policyDetailDTO.setPolicyPremiums(premiums);

		return policyDetailDTO;
	}

	public static PolicyDetailDTO createMockPolicyPaymentDataForRetroPartialMonth_Scenario1_APTC() {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		premiums.add(createMockPolicyPremium(1, "2015-01-01", "2015-02-15", new BigDecimal(100), new BigDecimal(50), new BigDecimal(150)));

		policyDetailDTO.setPolicyPremiums(premiums);

		//Payments
		List<PolicyPaymentTransDTO> payments = new ArrayList<PolicyPaymentTransDTO>();
		payments.add(createMockPolicyPayment(1L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "APTC", new BigDecimal("100.00"), "APPV"));

		policyDetailDTO.setPolicyPayments(payments);


		return policyDetailDTO;
	}

	public static PolicyDetailDTO createMockPolicyPaymentDataForRetroPartialMonth_Scenario2() {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		premiums.add(createMockPolicyPremium(1, "2015-01-01", "2015-02-15", new BigDecimal(100), new BigDecimal(50), new BigDecimal(150)));
		premiums.add(createMockPolicyPremium(1, "2015-02-16", "2015-12-31", new BigDecimal(200), new BigDecimal(50), new BigDecimal(250)));

		policyDetailDTO.setPolicyPremiums(premiums);

		//Payments
		List<PolicyPaymentTransDTO> payments = new ArrayList<PolicyPaymentTransDTO>();

		payments.add(createMockPolicyPayment(1L, 1L, "101", "P", "2015-01-01", "2015-01-01", "2015-01-31", "APTC", new BigDecimal("100.00"), "APPV"));
		payments.add(createMockPolicyPayment(2L, 1L, "101", "P", "2015-01-01", "2015-01-01", "2015-01-31", "CSR", new BigDecimal("50.00"), "APPV"));
		payments.add(createMockPolicyPayment(3L, 1L, "101", "P", "2015-01-01", "2015-01-01", "2015-01-31", "UF", new BigDecimal("150.00"), "APPV"));
		payments.get(2).setPaymentAmount(new BigDecimal("-5.25"));

		payments.add(createMockPolicyPayment(4L, 1L, "101", "P", "2015-02-01", "2015-02-01", "2015-02-28", "APTC", new BigDecimal("100.00"), "APPV"));
		payments.add(createMockPolicyPayment(5L, 1L, "101", "P", "2015-02-01", "2015-02-01", "2015-02-28", "CSR", new BigDecimal("50.00"), "APPV"));
		payments.add(createMockPolicyPayment(6L, 1L, "101", "P", "2015-02-01", "2015-02-01", "2015-02-28", "UF", new BigDecimal("150.00"), "APPV"));
		payments.get(5).setPaymentAmount(new BigDecimal("-5.25"));

		payments.add(createMockPolicyPayment(7L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "APTC", new BigDecimal("-100.00"), "APPV"));
		payments.get(6).setParentPolicyPaymentTransId(4L);
		payments.add(createMockPolicyPayment(8L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "CSR", new BigDecimal("-50.00"), "APPV"));
		payments.get(7).setParentPolicyPaymentTransId(5L);
		payments.add(createMockPolicyPayment(9L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "UF", new BigDecimal("150.00"), "APPV"));
		payments.get(8).setPaymentAmount(new BigDecimal("5.25"));
		payments.get(8).setParentPolicyPaymentTransId(6L);

		payments.add(createMockPolicyPayment(10L, 1L, "102", "R", "2015-02-01", "2015-02-01", "2015-02-15", "APTC", new BigDecimal("53.57"), "APPV"));
		payments.add(createMockPolicyPayment(11L, 1L, "102", "R", "2015-02-01", "2015-02-01", "2015-02-15", "CSR", new BigDecimal("26.79"), "APPV"));
		payments.add(createMockPolicyPayment(12L, 1L, "102", "R", "2015-02-01", "2015-02-01", "2015-02-15", "UF", new BigDecimal("80.36"), "APPV"));
		payments.get(11).setPaymentAmount(new BigDecimal("-2.81"));

		policyDetailDTO.setPolicyPayments(payments);


		return policyDetailDTO;
	}

	public static PolicyDetailDTO createMockPolicyPaymentDataForRetroPartialMonth_Scenario2_FFM() {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		PolicyPremium premium1 = createMockPolicyPremium(1, "2015-02-15", "2015-02-28", new BigDecimal(200), new BigDecimal(50), new BigDecimal(250));
		premium1.setProratedAptcAmount(new BigDecimal("100.00"));
		premium1.setProratedCsrAmount(new BigDecimal("25.00"));
		premium1.setProratedPremiumAmount(new BigDecimal("125.00"));
		premiums.add(premium1);
		premiums.add(createMockPolicyPremium(1, "2015-03-01", "2015-12-31", new BigDecimal(200), new BigDecimal(50), new BigDecimal(250)));

		policyDetailDTO.setPolicyPremiums(premiums);

		return policyDetailDTO;
	}

	public static PolicyDetailDTO createMockPolicyPaymentDataForRetroPartialMonth_Scenario3_FFM() {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		PolicyPremium premium1 = createMockPolicyPremium(1, "2015-02-15", "2015-02-28", new BigDecimal(100), new BigDecimal(50), new BigDecimal(150));
		premium1.setProratedAptcAmount(new BigDecimal("50.00"));
		premium1.setProratedCsrAmount(new BigDecimal("25.00"));
		premium1.setProratedPremiumAmount(new BigDecimal("75.00"));
		premiums.add(premium1);

		premiums.add(createMockPolicyPremium(1, "2015-03-01", "2015-03-31", new BigDecimal(100), new BigDecimal(50), new BigDecimal(150)));

		PolicyPremium premium3 = createMockPolicyPremium(1, "2015-04-01", "2015-04-15", new BigDecimal(100), new BigDecimal(50), new BigDecimal(150));
		premium3.setProratedAptcAmount(new BigDecimal("50.00"));
		premium3.setProratedCsrAmount(new BigDecimal("25.00"));
		premium3.setProratedPremiumAmount(new BigDecimal("75.00"));
		premiums.add(premium3);

		policyDetailDTO.setPolicyPremiums(premiums);

		//Payments
		List<PolicyPaymentTransDTO> payments = new ArrayList<PolicyPaymentTransDTO>();

		payments.add(createMockPolicyPayment(1L, 1L, "101", "P", "2015-02-01", "2015-02-15", "2015-02-28", "APTC", new BigDecimal("50.00"), "APPV"));
		payments.add(createMockPolicyPayment(2L, 1L, "101", "P", "2015-02-01", "2015-02-15", "2015-02-28", "CSR", new BigDecimal("25.00"), "APPV"));
		payments.add(createMockPolicyPayment(3L, 1L, "101", "P", "2015-02-01", "2015-02-15", "2015-02-28", "UF", new BigDecimal("75.00"), "APPV"));
		payments.get(2).setPaymentAmount(new BigDecimal("-2.75"));

		payments.add(createMockPolicyPayment(4L, 1L, "101", "P", "2015-03-01", "2015-03-01", "2015-03-31", "APTC", new BigDecimal("100.00"), "APPV"));
		payments.add(createMockPolicyPayment(5L, 1L, "101", "P", "2015-03-01", "2015-03-01", "2015-03-31", "CSR", new BigDecimal("50.00"), "APPV"));
		payments.add(createMockPolicyPayment(6L, 1L, "101", "P", "2015-03-01", "2015-03-01", "2015-03-31", "UF", new BigDecimal("150.00"), "APPV"));
		payments.get(5).setPaymentAmount(new BigDecimal("-5.25"));

		payments.add(createMockPolicyPayment(7L, 1L, "102", "R", "2015-04-01", "2015-04-01", "2015-04-30", "APTC", new BigDecimal("100.00"), "APPV"));
		payments.add(createMockPolicyPayment(8L, 1L, "102", "R", "2015-04-01", "2015-04-01", "2015-04-30", "CSR", new BigDecimal("50.00"), "APPV"));
		payments.add(createMockPolicyPayment(9L, 1L, "102", "R", "2015-04-01", "2015-04-01", "2015-04-30", "UF", new BigDecimal("150.00"), "APPV"));
		payments.get(8).setPaymentAmount(new BigDecimal("-5.25"));

		policyDetailDTO.setPolicyPayments(payments);


		return policyDetailDTO;
	}

	public static PolicyDetailDTO createMockPolicyPaymentDataForRetroPartialMonth_Scenario2_ProratedAmtExist() {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		PolicyPremium premium1 = createMockPolicyPremium(1, "2015-02-16", "2015-02-28", new BigDecimal(200), new BigDecimal(50), new BigDecimal(250));
		premium1.setProratedAptcAmount(new BigDecimal("92.86"));
		premium1.setProratedCsrAmount(new BigDecimal("23.21"));
		premium1.setProratedPremiumAmount(new BigDecimal("116.07"));
		premiums.add(premium1);
		premiums.add(createMockPolicyPremium(1, "2015-03-01", "2015-12-31", new BigDecimal(200), new BigDecimal(50), new BigDecimal(250)));

		policyDetailDTO.setPolicyPremiums(premiums);

		//Payments
		List<PolicyPaymentTransDTO> payments = new ArrayList<PolicyPaymentTransDTO>();

		payments.add(createMockPolicyPayment(1L, 1L, "101", "P", "2015-01-01", "2015-01-01", "2015-01-31", "APTC", new BigDecimal("100.00"), "APPV"));
		payments.add(createMockPolicyPayment(2L, 1L, "101", "P", "2015-01-01", "2015-01-01", "2015-01-31", "CSR", new BigDecimal("50.00"), "APPV"));
		payments.add(createMockPolicyPayment(3L, 1L, "101", "P", "2015-01-01", "2015-01-01", "2015-01-31", "UF", new BigDecimal("150.00"), "APPV"));
		payments.get(2).setPaymentAmount(new BigDecimal("-5.25"));

		payments.add(createMockPolicyPayment(4L, 1L, "101", "P", "2015-02-01", "2015-02-01", "2015-02-28", "APTC", new BigDecimal("100.00"), "APPV"));
		payments.add(createMockPolicyPayment(5L, 1L, "101", "P", "2015-02-01", "2015-02-01", "2015-02-28", "CSR", new BigDecimal("50.00"), "APPV"));
		payments.add(createMockPolicyPayment(6L, 1L, "101", "P", "2015-02-01", "2015-02-01", "2015-02-28", "UF", new BigDecimal("150.00"), "APPV"));
		payments.get(5).setPaymentAmount(new BigDecimal("-5.25"));

		payments.add(createMockPolicyPayment(7L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "APTC", new BigDecimal("-100.00"), "APPV"));
		payments.get(6).setParentPolicyPaymentTransId(4L);
		payments.add(createMockPolicyPayment(8L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "CSR", new BigDecimal("-50.00"), "APPV"));
		payments.get(7).setParentPolicyPaymentTransId(5L);
		payments.add(createMockPolicyPayment(9L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "UF", new BigDecimal("150.00"), "APPV"));
		payments.get(8).setPaymentAmount(new BigDecimal("5.25"));
		payments.get(8).setParentPolicyPaymentTransId(6L);

		payments.add(createMockPolicyPayment(10L, 1L, "102", "R", "2015-02-01", "2015-02-16", "2015-02-28", "APTC", new BigDecimal("53.57"), "APPV"));
		payments.add(createMockPolicyPayment(11L, 1L, "102", "R", "2015-02-01", "2015-02-16", "2015-02-28", "CSR", new BigDecimal("26.79"), "APPV"));
		payments.add(createMockPolicyPayment(12L, 1L, "102", "R", "2015-02-01", "2015-02-16", "2015-02-28", "UF", new BigDecimal("80.36"), "APPV"));
		payments.get(11).setPaymentAmount(new BigDecimal("-2.81"));

		policyDetailDTO.setPolicyPayments(payments);


		return policyDetailDTO;
	}

	public static PolicyDetailDTO createMockPolicyPaymentDataForRetroPartialMonth_Scenario2_NonProrating() {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		premiums.add(createMockPolicyPremium(1, "2015-01-01", "2015-02-15", new BigDecimal(100), new BigDecimal(50), new BigDecimal(150)));
		premiums.add(createMockPolicyPremium(1, "2015-02-16", "2015-12-31", new BigDecimal(200), new BigDecimal(50), new BigDecimal(250)));

		policyDetailDTO.setPolicyPremiums(premiums);

		//Payments
		List<PolicyPaymentTransDTO> payments = new ArrayList<PolicyPaymentTransDTO>();

		payments.add(createMockPolicyPayment(1L, 1L, "101", "P", "2015-01-01", "2015-01-01", "2015-01-31", "APTC", new BigDecimal("100.00"), "APPV"));
		payments.add(createMockPolicyPayment(2L, 1L, "101", "P", "2015-01-01", "2015-01-01", "2015-01-31", "CSR", new BigDecimal("50.00"), "APPV"));
		payments.add(createMockPolicyPayment(3L, 1L, "101", "P", "2015-01-01", "2015-01-01", "2015-01-31", "UF", new BigDecimal("150.00"), "APPV"));
		payments.get(2).setPaymentAmount(new BigDecimal("-5.25"));

		payments.add(createMockPolicyPayment(4L, 1L, "101", "P", "2015-02-01", "2015-02-01", "2015-02-28", "APTC", new BigDecimal("100.00"), "APPV"));
		payments.add(createMockPolicyPayment(5L, 1L, "101", "P", "2015-02-01", "2015-02-01", "2015-02-28", "CSR", new BigDecimal("50.00"), "APPV"));
		payments.add(createMockPolicyPayment(6L, 1L, "101", "P", "2015-02-01", "2015-02-01", "2015-02-28", "UF", new BigDecimal("150.00"), "APPV"));
		payments.get(5).setPaymentAmount(new BigDecimal("-5.25"));

		payments.add(createMockPolicyPayment(7L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "APTC", new BigDecimal("-100.00"), "APPV"));
		payments.get(6).setParentPolicyPaymentTransId(4L);
		payments.add(createMockPolicyPayment(8L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "CSR", new BigDecimal("-50.00"), "APPV"));
		payments.get(7).setParentPolicyPaymentTransId(5L);
		payments.add(createMockPolicyPayment(9L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "UF", new BigDecimal("150.00"), "APPV"));
		payments.get(8).setPaymentAmount(new BigDecimal("5.25"));
		payments.get(8).setParentPolicyPaymentTransId(6L);

		policyDetailDTO.setPolicyPayments(payments);


		return policyDetailDTO;
	}

	public static PolicyDetailDTO createMockPolicyPaymentDataForRetroPartialMonth_Scenario2_NonProrating_PmtExists() {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		premiums.add(createMockPolicyPremium(1, "2015-01-01", "2015-02-15", new BigDecimal(100), new BigDecimal(50), new BigDecimal(150)));
		premiums.add(createMockPolicyPremium(1, "2015-02-16", "2015-12-31", new BigDecimal(200), new BigDecimal(50), new BigDecimal(250)));

		policyDetailDTO.setPolicyPremiums(premiums);

		//Payments
		List<PolicyPaymentTransDTO> payments = new ArrayList<PolicyPaymentTransDTO>();

		payments.add(createMockPolicyPayment(1L, 1L, "101", "P", "2015-01-01", "2015-01-01", "2015-01-31", "APTC", new BigDecimal("100.00"), "APPV"));
		payments.add(createMockPolicyPayment(2L, 1L, "101", "P", "2015-01-01", "2015-01-01", "2015-01-31", "CSR", new BigDecimal("50.00"), "APPV"));
		payments.add(createMockPolicyPayment(3L, 1L, "101", "P", "2015-01-01", "2015-01-01", "2015-01-31", "UF", new BigDecimal("150.00"), "APPV"));
		payments.get(2).setPaymentAmount(new BigDecimal("-5.25"));

		payments.add(createMockPolicyPayment(4L, 1L, "101", "P", "2015-02-01", "2015-02-01", "2015-02-28", "APTC", new BigDecimal("100.00"), "APPV"));
		payments.add(createMockPolicyPayment(5L, 1L, "101", "P", "2015-02-01", "2015-02-01", "2015-02-28", "CSR", new BigDecimal("50.00"), "APPV"));
		payments.add(createMockPolicyPayment(6L, 1L, "101", "P", "2015-02-01", "2015-02-01", "2015-02-28", "UF", new BigDecimal("150.00"), "APPV"));
		payments.get(5).setPaymentAmount(new BigDecimal("-5.25"));

		payments.add(createMockPolicyPayment(7L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "APTC", new BigDecimal("-100.00"), "APPV"));
		payments.get(6).setParentPolicyPaymentTransId(4L);
		payments.add(createMockPolicyPayment(8L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "CSR", new BigDecimal("-50.00"), "APPV"));
		payments.get(7).setParentPolicyPaymentTransId(5L);
		payments.add(createMockPolicyPayment(9L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "UF", new BigDecimal("150.00"), "APPV"));
		payments.get(8).setPaymentAmount(new BigDecimal("5.25"));
		payments.get(8).setParentPolicyPaymentTransId(6L);

		payments.add(createMockPolicyPayment(10L, 1L, "102", "R", "2015-02-01", "2015-02-01", "2015-02-15", "APTC", new BigDecimal("53.57"), "APPV"));
		payments.add(createMockPolicyPayment(11L, 1L, "102", "R", "2015-02-01", "2015-02-01", "2015-02-15", "CSR", new BigDecimal("26.79"), "APPV"));
		payments.add(createMockPolicyPayment(12L, 1L, "102", "R", "2015-02-01", "2015-02-01", "2015-02-15", "UF", new BigDecimal("80.36"), "APPV"));
		payments.get(11).setPaymentAmount(new BigDecimal("-2.81"));

		policyDetailDTO.setPolicyPayments(payments);


		return policyDetailDTO;
	}

	public static PolicyDetailDTO createMockPolicyPaymentDataForRetroPartialMonth_Scenario2_PmtExists_MaxPmtMonth_LT_MinPmtMonth() {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		premiums.add(createMockPolicyPremium(1, "2015-02-10", "2015-02-28", new BigDecimal(100), new BigDecimal(50), new BigDecimal(150)));

		policyDetailDTO.setPolicyPremiums(premiums);

		//Payments
		List<PolicyPaymentTransDTO> payments = new ArrayList<PolicyPaymentTransDTO>();

		payments.add(createMockPolicyPayment(4L, 1L, "101", "P", "2015-02-01", "2015-02-10", "2015-02-28", "APTC", new BigDecimal("100.00"), "PAPPV"));
		payments.add(createMockPolicyPayment(5L, 1L, "101", "P", "2015-02-01", "2015-02-10", "2015-02-28", "CSR", new BigDecimal("50.00"), "PAPPV"));
		payments.add(createMockPolicyPayment(6L, 1L, "101", "P", "2015-02-01", "2015-02-10", "2015-02-28", "UF", new BigDecimal("150.00"), "PAPPV"));
		payments.get(2).setPaymentAmount(new BigDecimal("-5.25"));

		policyDetailDTO.setPolicyPayments(payments);


		return policyDetailDTO;
	}

	public static PolicyDetailDTO createMockPolicyPaymentDataForRetroPartialMonth_Scenario2_o() {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		premiums.add(createMockPolicyPremium(1, "2015-02-16", "2015-12-31", new BigDecimal(200), new BigDecimal(50), new BigDecimal(250)));

		policyDetailDTO.setPolicyPremiums(premiums);

		//Payments
		List<PolicyPaymentTransDTO> payments = new ArrayList<PolicyPaymentTransDTO>();
		payments.add(createMockPolicyPayment(1L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-15", "APTC", new BigDecimal("53.57"), "APPV"));
		payments.add(createMockPolicyPayment(2L, 1L, "101", "P", "2015-02-01", "2015-02-01", "2015-02-15", "CSR", new BigDecimal("26.79"), "APPV"));
		payments.add(createMockPolicyPayment(3L, 1L, "101", "P", "2015-02-01", "2015-02-01", "2015-02-15", "UF", new BigDecimal("150.00"), "APPV"));
		payments.get(2).setPaymentAmount(new BigDecimal("-2.81"));

		policyDetailDTO.setPolicyPayments(payments);


		return policyDetailDTO;
	}

	public static PolicyDetailDTO createMockPolicyPaymentDataForRetroPartialMonth_Scenario2_APTC() {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		premiums.add(createMockPolicyPremium(1, "2015-02-16", "2015-12-31", new BigDecimal(200), new BigDecimal(50), new BigDecimal(250)));

		policyDetailDTO.setPolicyPremiums(premiums);

		//Payments
		List<PolicyPaymentTransDTO> payments = new ArrayList<PolicyPaymentTransDTO>();
		payments.add(createMockPolicyPayment(1L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-15", "APTC", new BigDecimal("53.57"), "APPV"));

		policyDetailDTO.setPolicyPayments(payments);

		return policyDetailDTO;
	}

	public static PolicyDetailDTO createMockPolicyPaymentDataForRetroPartialMonth_Scenario3() {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		premiums.add(createMockPolicyPremium(1, "2015-01-01", "2015-02-15", new BigDecimal(100), new BigDecimal(50), new BigDecimal(150)));
		premiums.add(createMockPolicyPremium(1, "2015-02-16", "2015-12-31", new BigDecimal(200), new BigDecimal(50), new BigDecimal(250)));

		policyDetailDTO.setPolicyPremiums(premiums);

		//Payments
		List<PolicyPaymentTransDTO> payments = new ArrayList<PolicyPaymentTransDTO>();
		payments.add(createMockPolicyPayment(1L, 1L, "101", "P", "2015-01-01", "2015-01-01", "2015-01-31", "APTC", new BigDecimal("100.00"), "APPV"));
		payments.add(createMockPolicyPayment(2L, 1L, "101", "P", "2015-01-01", "2015-01-01", "2015-01-31", "CSR", new BigDecimal("50.00"), "APPV"));
		payments.add(createMockPolicyPayment(3L, 1L, "101", "P", "2015-01-01", "2015-01-01", "2015-01-31", "UF", new BigDecimal("150.00"), "APPV"));
		payments.get(2).setPaymentAmount(new BigDecimal("-5.25"));

		payments.add(createMockPolicyPayment(4L, 1L, "101", "P", "2015-02-01", "2015-02-01", "2015-02-28", "APTC", new BigDecimal("100.00"), "APPV"));
		payments.add(createMockPolicyPayment(5L, 1L, "101", "P", "2015-02-01", "2015-02-01", "2015-02-28", "CSR", new BigDecimal("50.00"), "APPV"));
		payments.add(createMockPolicyPayment(6L, 1L, "101", "P", "2015-02-01", "2015-02-01", "2015-02-28", "UF", new BigDecimal("150.00"), "APPV"));
		payments.get(5).setPaymentAmount(new BigDecimal("-5.25"));

		policyDetailDTO.setPolicyPayments(payments);

		return policyDetailDTO;
	}

	public static PolicyDetailDTO createMockPolicyPaymentDataForRetroPartialMonth_Scenario3_FutureCancel(String paymentStatusCd) {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		premiums.add(createMockPolicyPremium(1, "2015-01-01", "2015-02-15", new BigDecimal(100), new BigDecimal(50), new BigDecimal(150)));
		//premiums.add(createMockPolicyPremium(1, "2015-02-16", "2015-12-31", new BigDecimal(200), new BigDecimal(50), new BigDecimal(250)));

		policyDetailDTO.setPolicyPremiums(premiums);

		//Payments
		List<PolicyPaymentTransDTO> payments = new ArrayList<PolicyPaymentTransDTO>();
		payments.add(createMockPolicyPayment(1L, 1L, "101", "P", "2015-01-01", "2015-01-01", "2015-01-31", "APTC", new BigDecimal("100.00"), paymentStatusCd));
		payments.add(createMockPolicyPayment(2L, 1L, "101", "P", "2015-01-01", "2015-01-01", "2015-01-31", "CSR", new BigDecimal("50.00"), paymentStatusCd));
		payments.add(createMockPolicyPayment(3L, 1L, "101", "P", "2015-01-01", "2015-01-01", "2015-01-31", "UF", new BigDecimal("150.00"), paymentStatusCd));
		payments.get(2).setPaymentAmount(new BigDecimal("-5.25"));

		payments.add(createMockPolicyPayment(4L, 1L, "101", "P", "2015-02-01", "2015-02-01", "2015-02-15", "APTC", new BigDecimal("53.57"), paymentStatusCd));
		payments.add(createMockPolicyPayment(5L, 1L, "101", "P", "2015-02-01", "2015-02-01", "2015-02-15", "CSR", new BigDecimal("26.79"), paymentStatusCd));
		payments.add(createMockPolicyPayment(6L, 1L, "101", "P", "2015-02-01", "2015-02-01", "2015-02-15", "UF", new BigDecimal("80.36"), paymentStatusCd));
		payments.get(5).setPaymentAmount(new BigDecimal("-2.81"));

		payments.add(createMockPolicyPayment(7L, 1L, "101", "P", "2015-02-01", "2015-02-16", "2015-02-28", "APTC", new BigDecimal("92.86"), paymentStatusCd));
		payments.add(createMockPolicyPayment(8L, 1L, "101", "P", "2015-02-01", "2015-02-16", "2015-02-28", "CSR", new BigDecimal("23.21"), paymentStatusCd));
		payments.add(createMockPolicyPayment(9L, 1L, "101", "P", "2015-02-01", "2015-02-16", "2015-02-28", "UF", new BigDecimal("250.00"), paymentStatusCd));
		payments.get(8).setPaymentAmount(new BigDecimal("-4.06"));

		policyDetailDTO.setPolicyPayments(payments);


		return policyDetailDTO;
	}

	public static PolicyDetailDTO createMockPolicyPaymentDataForRetroPartialMonth_Scenario3_FutureCancel_A(String paymentStatusCd) {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		premiums.add(createMockPolicyPremium(1, "2015-01-01", "2015-02-10", new BigDecimal(100), new BigDecimal(50), new BigDecimal(150)));
		premiums.add(createMockPolicyPremium(1, "2015-02-11", "2015-02-15", new BigDecimal(100), new BigDecimal(50), new BigDecimal(150)));

		policyDetailDTO.setPolicyPremiums(premiums);

		//Payments
		List<PolicyPaymentTransDTO> payments = new ArrayList<PolicyPaymentTransDTO>();
		payments.add(createMockPolicyPayment(1L, 1L, "101", "P", "2015-01-01", "2015-01-01", "2015-01-31", "APTC", new BigDecimal("100.00"), paymentStatusCd));
		payments.add(createMockPolicyPayment(2L, 1L, "101", "P", "2015-01-01", "2015-01-01", "2015-01-31", "CSR", new BigDecimal("50.00"), paymentStatusCd));
		payments.add(createMockPolicyPayment(3L, 1L, "101", "P", "2015-01-01", "2015-01-01", "2015-01-31", "UF", new BigDecimal("150.00"), paymentStatusCd));
		payments.get(2).setPaymentAmount(new BigDecimal("-5.25"));
		if(paymentStatusCd.equals("PCYC")) payments.get(2).setPaymentAmount(null);

		payments.add(createMockPolicyPayment(4L, 1L, "101", "P", "2015-02-01", "2015-02-01", "2015-02-10", "APTC", new BigDecimal("35.71"), paymentStatusCd));
		payments.add(createMockPolicyPayment(5L, 1L, "101", "P", "2015-02-01", "2015-02-01", "2015-02-10", "CSR", new BigDecimal("17.86"), paymentStatusCd));
		payments.add(createMockPolicyPayment(6L, 1L, "101", "P", "2015-02-01", "2015-02-01", "2015-02-10", "UF", new BigDecimal("53.57"), paymentStatusCd));
		payments.get(5).setPaymentAmount(new BigDecimal("-1.88"));
		if(paymentStatusCd.equals("PCYC")) payments.get(5).setPaymentAmount(null);

		payments.add(createMockPolicyPayment(7L, 1L, "101", "P", "2015-02-01", "2015-02-11", "2015-02-15", "APTC", new BigDecimal("17.86"), paymentStatusCd));
		payments.add(createMockPolicyPayment(8L, 1L, "101", "P", "2015-02-01", "2015-02-11", "2015-02-15", "CSR", new BigDecimal("8.93"), paymentStatusCd));
		payments.add(createMockPolicyPayment(9L, 1L, "101", "P", "2015-02-01", "2015-02-11", "2015-02-15", "UF", new BigDecimal("26.79"), paymentStatusCd));
		payments.get(8).setPaymentAmount(new BigDecimal("-0.94"));
		if(paymentStatusCd.equals("PCYC")) payments.get(8).setPaymentAmount(null);

		payments.add(createMockPolicyPayment(10L, 1L, "101", "P", "2015-02-01", "2015-02-16", "2015-02-28", "APTC", new BigDecimal("92.86"), paymentStatusCd));
		payments.add(createMockPolicyPayment(11L, 1L, "101", "P", "2015-02-01", "2015-02-16", "2015-02-28", "CSR", new BigDecimal("23.21"), paymentStatusCd));
		payments.add(createMockPolicyPayment(12L, 1L, "101", "P", "2015-02-01", "2015-02-16", "2015-02-28", "UF", new BigDecimal("250.00"), paymentStatusCd));
		payments.get(11).setPaymentAmount(new BigDecimal("-4.06"));
		if(paymentStatusCd.equals("PCYC")) payments.get(11).setPaymentAmount(null);

		policyDetailDTO.setPolicyPayments(payments);


		return policyDetailDTO;
	}

	public static PolicyDetailDTO createMockPolicyPaymentDataForRetroPartialMonth_Scenario3_APTC() {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		premiums.add(createMockPolicyPremium(1, "2015-01-01", "2015-02-15", new BigDecimal(100), new BigDecimal(50), new BigDecimal(150)));
		premiums.add(createMockPolicyPremium(1, "2015-02-16", "2015-12-31", new BigDecimal(200), new BigDecimal(50), new BigDecimal(250)));

		policyDetailDTO.setPolicyPremiums(premiums);

		//Payments
		List<PolicyPaymentTransDTO> payments = new ArrayList<PolicyPaymentTransDTO>();
		payments.add(createMockPolicyPayment(1L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "APTC", new BigDecimal("100.00"), "APPV"));

		policyDetailDTO.setPolicyPayments(payments);


		return policyDetailDTO;
	}

	public static PolicyDetailDTO createMockPolicyPaymentDataForRetroPartialMonth_Scenario4() {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		premiums.add(createMockPolicyPremium(1, "2015-01-01", "2015-12-31", new BigDecimal(100), new BigDecimal(50), new BigDecimal(150)));

		policyDetailDTO.setPolicyPremiums(premiums);

		//Payments
		List<PolicyPaymentTransDTO> payments = new ArrayList<PolicyPaymentTransDTO>();

		payments.add(createMockPolicyPayment(1L, 1L, "101", "P", "2015-01-01", "2015-01-01", "2015-01-31", "APTC", new BigDecimal("100.00"), "APPV"));
		payments.add(createMockPolicyPayment(2L, 1L, "101", "P", "2015-01-01", "2015-01-01", "2015-01-31", "CSR", new BigDecimal("50.00"), "APPV"));
		payments.add(createMockPolicyPayment(3L, 1L, "101", "P", "2015-01-01", "2015-01-01", "2015-01-31", "UF", new BigDecimal("150.00"), "APPV"));
		payments.get(2).setPaymentAmount(new BigDecimal("-5.25"));

		payments.add(createMockPolicyPayment(4L, 1L, "101", "P", "2015-02-01", "2015-02-01", "2015-02-28", "APTC", new BigDecimal("100.00"), "APPV"));
		payments.add(createMockPolicyPayment(5L, 1L, "101", "P", "2015-02-01", "2015-02-01", "2015-02-28", "CSR", new BigDecimal("50.00"), "APPV"));
		payments.add(createMockPolicyPayment(6L, 1L, "101", "P", "2015-02-01", "2015-02-01", "2015-02-28", "UF", new BigDecimal("150.00"), "APPV"));
		payments.get(5).setPaymentAmount(new BigDecimal("-5.25"));

		payments.add(createMockPolicyPayment(7L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "APTC", new BigDecimal("-100.00"), "APPV"));
		payments.get(6).setParentPolicyPaymentTransId(4L);
		payments.add(createMockPolicyPayment(8L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "CSR", new BigDecimal("-50.00"), "APPV"));
		payments.get(7).setParentPolicyPaymentTransId(5L);
		payments.add(createMockPolicyPayment(9L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "UF", new BigDecimal("150.00"), "APPV"));
		payments.get(8).setParentPolicyPaymentTransId(6L);
		payments.get(8).setPaymentAmount(new BigDecimal("5.25"));

		payments.add(createMockPolicyPayment(10L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-15", "APTC", new BigDecimal("53.57"), "APPV"));
		payments.add(createMockPolicyPayment(11L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-15", "CSR", new BigDecimal("26.79"), "APPV"));
		payments.add(createMockPolicyPayment(12L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-15", "UF", new BigDecimal("150.00"), "APPV"));
		payments.get(11).setPaymentAmount(new BigDecimal("-2.81"));

		payments.add(createMockPolicyPayment(13L, 1L, "101", "R", "2015-02-01", "2015-02-16", "2015-02-28", "APTC", new BigDecimal("92.86"), "APPV"));
		payments.add(createMockPolicyPayment(14L, 1L, "101", "R", "2015-02-01", "2015-02-16", "2015-02-28", "CSR", new BigDecimal("23.21"), "APPV"));
		payments.add(createMockPolicyPayment(15L, 1L, "101", "R", "2015-02-01", "2015-02-16", "2015-02-28", "UF", new BigDecimal("250.00"), "APPV"));
		payments.get(14).setPaymentAmount(new BigDecimal("-4.06"));

		policyDetailDTO.setPolicyPayments(payments);


		return policyDetailDTO;
	}

	public static PolicyDetailDTO createMockPolicyPaymentDataForRetroPartialMonth_Scenario4_APTC() {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		premiums.add(createMockPolicyPremium(1, "2015-01-01", "2015-12-31", new BigDecimal(100), new BigDecimal(50), new BigDecimal(150)));

		policyDetailDTO.setPolicyPremiums(premiums);

		//Payments
		List<PolicyPaymentTransDTO> payments = new ArrayList<PolicyPaymentTransDTO>();
		//payments.add(createMockPolicyPayment(1L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "APTC", new BigDecimal("100.00"), "APPV"));
		//payments.add(createMockPolicyPayment(2L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-28", "APTC", new BigDecimal("-100.00"), "APPV"));
		//payments.get(1).setParentPolicyPaymentTransId(1L);
		payments.add(createMockPolicyPayment(3L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-15", "APTC", new BigDecimal("53.57"), "APPV"));
		payments.add(createMockPolicyPayment(4L, 1L, "101", "R", "2015-02-01", "2015-02-16", "2015-02-28", "APTC", new BigDecimal("92.86"), "APPV"));

		policyDetailDTO.setPolicyPayments(payments);


		return policyDetailDTO;
	}

	public static PolicyDetailDTO createMockPolicyPaymentDataForRetroPartialMonth_Scenario5() {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		premiums.add(createMockPolicyPremium(1, "2015-01-01", "2015-02-09", new BigDecimal(100), new BigDecimal(50), new BigDecimal(150)));
		premiums.add(createMockPolicyPremium(1, "2015-02-10", "2015-12-31", new BigDecimal(200), new BigDecimal(50), new BigDecimal(250)));

		policyDetailDTO.setPolicyPremiums(premiums);

		//Payments
		List<PolicyPaymentTransDTO> payments = new ArrayList<PolicyPaymentTransDTO>();

		payments.add(createMockPolicyPayment(1L, 1L, "101", "P", "2015-01-01", "2015-01-01", "2015-01-31", "APTC", new BigDecimal("100.00"), "APPV"));
		payments.add(createMockPolicyPayment(2L, 1L, "101", "P", "2015-01-01", "2015-01-01", "2015-01-31", "CSR", new BigDecimal("50.00"), "APPV"));
		payments.add(createMockPolicyPayment(3L, 1L, "101", "P", "2015-01-01", "2015-01-01", "2015-01-31", "UF", new BigDecimal("150"), "APPV"));
		payments.get(2).setPaymentAmount(new BigDecimal("-5.25"));

		payments.add(createMockPolicyPayment(4L, 1L, "101", "P", "2015-02-01", "2015-02-01", "2015-02-10", "APTC", new BigDecimal("35.71"), "APPV"));
		payments.add(createMockPolicyPayment(5L, 1L, "101", "P", "2015-02-01", "2015-02-01", "2015-02-10", "CSR", new BigDecimal("17.86"), "APPV"));
		payments.add(createMockPolicyPayment(6L, 1L, "101", "p", "2015-02-01", "2015-02-01", "2015-02-10", "UF", new BigDecimal("150"), "APPV"));
		payments.get(5).setPaymentAmount(new BigDecimal("-1.88"));

		payments.add(createMockPolicyPayment(7L, 1L, "101", "P", "2015-02-01", "2015-02-11", "2015-02-28", "APTC", new BigDecimal("128.57"), "APPV"));
		payments.add(createMockPolicyPayment(8L, 1L, "101", "P", "2015-02-01", "2015-02-11", "2015-02-28", "CSR", new BigDecimal("32.14"), "APPV"));
		payments.add(createMockPolicyPayment(9L, 1L, "101", "P", "2015-02-01", "2015-02-11", "2015-02-28", "UF", new BigDecimal("250"), "APPV"));
		payments.get(8).setPaymentAmount(new BigDecimal("-5.63"));

		policyDetailDTO.setPolicyPayments(payments);


		return policyDetailDTO;
	}

	public static PolicyDetailDTO createMockPolicyPaymentDataForRetroPartialMonth_Scenario5_APTC() {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		premiums.add(createMockPolicyPremium(1, "2015-01-01", "2015-02-09", new BigDecimal(100), new BigDecimal(50), new BigDecimal(150)));
		premiums.add(createMockPolicyPremium(1, "2015-02-10", "2015-12-31", new BigDecimal(200), new BigDecimal(50), new BigDecimal(250)));

		policyDetailDTO.setPolicyPremiums(premiums);

		//Payments
		List<PolicyPaymentTransDTO> payments = new ArrayList<PolicyPaymentTransDTO>();
		payments.add(createMockPolicyPayment(1L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-10", "APTC", new BigDecimal("35.71"), "APPV"));
		payments.add(createMockPolicyPayment(2L, 1L, "101", "R", "2015-02-01", "2015-02-11", "2015-02-28", "APTC", new BigDecimal("128.57"), "APPV"));

		policyDetailDTO.setPolicyPayments(payments);


		return policyDetailDTO;
	}

	public static PolicyDetailDTO createMockPolicyPaymentDataForRetroPartialMonth_Scenario6(String paymentStatusCd, String mgpId) {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		premiums.add(createMockPolicyPremium(1, "2015-01-01", "2015-02-10", new BigDecimal(100), new BigDecimal(50), new BigDecimal(150)));
		premiums.add(createMockPolicyPremium(1, "2015-02-11", "2015-02-15", new BigDecimal(100), new BigDecimal(50), new BigDecimal(150)));
		premiums.add(createMockPolicyPremium(1, "2015-02-16", "2015-12-31", new BigDecimal(200), new BigDecimal(50), new BigDecimal(250)));

		policyDetailDTO.setPolicyPremiums(premiums);

		//Payments
		List<PolicyPaymentTransDTO> payments = new ArrayList<PolicyPaymentTransDTO>();

		payments.add(createMockPolicyPayment(1L, 1L, "101", "P", "2015-01-01", "2015-01-01", "2015-01-31", "APTC", new BigDecimal("100.00"), paymentStatusCd, mgpId));
		payments.add(createMockPolicyPayment(2L, 1L, "101", "P", "2015-01-01", "2015-01-01", "2015-01-31", "CSR", new BigDecimal("50.00"), paymentStatusCd, mgpId));
		payments.add(createMockPolicyPayment(3L, 1L, "101", "P", "2015-01-01", "2015-01-01", "2015-01-31", "UF", new BigDecimal("150"), paymentStatusCd, mgpId));
		payments.get(2).setPaymentAmount(new BigDecimal("-5.25"));

		payments.add(createMockPolicyPayment(4L, 1L, "101", "P", "2015-02-01", "2015-02-01", "2015-02-10", "APTC", new BigDecimal("35.71"), paymentStatusCd, mgpId));
		payments.add(createMockPolicyPayment(5L, 1L, "101", "P", "2015-02-01", "2015-02-01", "2015-02-10", "CSR", new BigDecimal("17.86"), paymentStatusCd, mgpId));
		payments.add(createMockPolicyPayment(6L, 1L, "101", "p", "2015-02-01", "2015-02-01", "2015-02-10", "UF", new BigDecimal("53.57"), paymentStatusCd, mgpId));
		payments.get(5).setPaymentAmount(new BigDecimal("-1.88"));

		payments.add(createMockPolicyPayment(7L, 1L, "101", "P", "2015-02-01", "2015-02-11", "2015-02-28", "APTC", new BigDecimal("128.57"), paymentStatusCd, mgpId));
		payments.add(createMockPolicyPayment(8L, 1L, "101", "P", "2015-02-01", "2015-02-11", "2015-02-28", "CSR", new BigDecimal("32.14"), paymentStatusCd, mgpId));
		payments.add(createMockPolicyPayment(9L, 1L, "101", "P", "2015-02-01", "2015-02-11", "2015-02-28", "UF", new BigDecimal("160.71"), paymentStatusCd, mgpId));
		payments.get(8).setPaymentAmount(new BigDecimal("-5.63"));

		policyDetailDTO.setPolicyPayments(payments);


		return policyDetailDTO;
	}

	public static PolicyDetailDTO createMockPolicyPaymentDataForRetroPartialMonth_Scenario6_APTC() {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		premiums.add(createMockPolicyPremium(1, "2015-01-01", "2015-02-10", new BigDecimal(100), new BigDecimal(50), new BigDecimal(150)));
		premiums.add(createMockPolicyPremium(1, "2015-02-11", "2015-02-15", new BigDecimal(100), new BigDecimal(50), new BigDecimal(150)));
		premiums.add(createMockPolicyPremium(1, "2015-02-16", "2015-12-31", new BigDecimal(200), new BigDecimal(50), new BigDecimal(250)));

		policyDetailDTO.setPolicyPremiums(premiums);

		//Payments
		List<PolicyPaymentTransDTO> payments = new ArrayList<PolicyPaymentTransDTO>();
		payments.add(createMockPolicyPayment(1L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-10", "APTC", new BigDecimal("35.71"), "APPV"));
		payments.add(createMockPolicyPayment(2L, 1L, "101", "R", "2015-02-01", "2015-02-11", "2015-02-28", "APTC", new BigDecimal("128.57"), "APPV"));

		policyDetailDTO.setPolicyPayments(payments);


		return policyDetailDTO;
	}

	public static PolicyDetailDTO createMockPolicyPaymentDataForRetroPartialMonth_Scenario6A(String paymentStatusCd) {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		premiums.add(createMockPolicyPremium(1, "2015-01-01", "2015-02-10", new BigDecimal(150), new BigDecimal(50), new BigDecimal(200)));
		premiums.add(createMockPolicyPremium(1, "2015-02-11", "2015-02-15", new BigDecimal(100), new BigDecimal(50), new BigDecimal(150)));
		premiums.add(createMockPolicyPremium(1, "2015-02-16", "2015-12-31", new BigDecimal(200), new BigDecimal(50), new BigDecimal(250)));

		policyDetailDTO.setPolicyPremiums(premiums);

		//Payments
		List<PolicyPaymentTransDTO> payments = new ArrayList<PolicyPaymentTransDTO>();

		payments.add(createMockPolicyPayment(1L, 1L, "101", "P", "2015-01-01", "2015-01-01", "2015-01-31", "APTC", new BigDecimal("100.00"), paymentStatusCd));
		payments.add(createMockPolicyPayment(2L, 1L, "101", "P", "2015-01-01", "2015-01-01", "2015-01-31", "CSR", new BigDecimal("50.00"), paymentStatusCd));
		payments.add(createMockPolicyPayment(3L, 1L, "101", "P", "2015-01-01", "2015-01-01", "2015-01-31", "UF", new BigDecimal("150"), paymentStatusCd));
		payments.get(2).setPaymentAmount(new BigDecimal("-5.25"));

		payments.add(createMockPolicyPayment(4L, 1L, "101", "P", "2015-02-01", "2015-02-01", "2015-02-10", "APTC", new BigDecimal("35.71"), paymentStatusCd));
		payments.add(createMockPolicyPayment(5L, 1L, "101", "P", "2015-02-01", "2015-02-01", "2015-02-10", "CSR", new BigDecimal("17.86"), paymentStatusCd));
		payments.add(createMockPolicyPayment(6L, 1L, "101", "p", "2015-02-01", "2015-02-01", "2015-02-10", "UF", new BigDecimal("150"), paymentStatusCd));
		payments.get(5).setPaymentAmount(new BigDecimal("-1.88"));

		payments.add(createMockPolicyPayment(7L, 1L, "101", "P", "2015-02-01", "2015-02-11", "2015-02-28", "APTC", new BigDecimal("128.57"), paymentStatusCd));
		payments.add(createMockPolicyPayment(8L, 1L, "101", "P", "2015-02-01", "2015-02-11", "2015-02-28", "CSR", new BigDecimal("32.14"), paymentStatusCd));
		payments.add(createMockPolicyPayment(9L, 1L, "101", "P", "2015-02-01", "2015-02-11", "2015-02-28", "UF", new BigDecimal("250"), paymentStatusCd));
		payments.get(8).setPaymentAmount(new BigDecimal("-5.63"));

		policyDetailDTO.setPolicyPayments(payments);


		return policyDetailDTO;
	}

	public static PolicyDetailDTO createMockPolicyPaymentDataForRetroPartialMonth_Scenario6A_APTC() {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		premiums.add(createMockPolicyPremium(1, "2015-01-01", "2015-02-10", new BigDecimal(150), new BigDecimal(50), new BigDecimal(200)));
		premiums.add(createMockPolicyPremium(1, "2015-02-11", "2015-02-15", new BigDecimal(100), new BigDecimal(50), new BigDecimal(150)));
		premiums.add(createMockPolicyPremium(1, "2015-02-16", "2015-12-31", new BigDecimal(200), new BigDecimal(50), new BigDecimal(250)));

		policyDetailDTO.setPolicyPremiums(premiums);

		//Payments
		List<PolicyPaymentTransDTO> payments = new ArrayList<PolicyPaymentTransDTO>();
		payments.add(createMockPolicyPayment(1L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-10", "APTC", new BigDecimal("35.71"), "APPV"));
		payments.add(createMockPolicyPayment(2L, 1L, "101", "R", "2015-02-01", "2015-02-11", "2015-02-28", "APTC", new BigDecimal("128.57"), "APPV"));

		policyDetailDTO.setPolicyPayments(payments);


		return policyDetailDTO;
	}

	public static PolicyDetailDTO createMockPolicyPaymentDataForRetroPartialMonth_Scenario7() {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		premiums.add(createMockPolicyPremium(1, "2015-01-01", "2015-02-15", new BigDecimal(100), new BigDecimal(50), new BigDecimal(150)));
		premiums.add(createMockPolicyPremium(1, "2015-02-16", "2015-12-31", new BigDecimal(200), new BigDecimal(50), new BigDecimal(250)));

		policyDetailDTO.setPolicyPremiums(premiums);

		//Payments
		List<PolicyPaymentTransDTO> payments = new ArrayList<PolicyPaymentTransDTO>();

		payments.add(createMockPolicyPayment(1L, 1L, "101", "P", "2015-01-01", "2015-01-01", "2015-01-31", "APTC", new BigDecimal("100.00"), "APPV"));
		payments.add(createMockPolicyPayment(2L, 1L, "101", "P", "2015-01-01", "2015-01-01", "2015-01-31", "CSR", new BigDecimal("50.00"), "APPV"));
		payments.add(createMockPolicyPayment(3L, 1L, "101", "P", "2015-01-01", "2015-01-01", "2015-01-31", "UF", new BigDecimal("150"), "APPV"));
		payments.get(2).setPaymentAmount(new BigDecimal("-5.25"));

		payments.add(createMockPolicyPayment(4L, 1L, "101", "P", "2015-02-01", "2015-02-01", "2015-02-10", "APTC", new BigDecimal("35.71"), "APPV"));
		payments.add(createMockPolicyPayment(5L, 1L, "101", "P", "2015-02-01", "2015-02-01", "2015-02-10", "CSR", new BigDecimal("17.86"), "APPV"));
		payments.add(createMockPolicyPayment(6L, 1L, "101", "p", "2015-02-01", "2015-02-01", "2015-02-10", "UF", new BigDecimal("53.57"), "APPV"));
		payments.get(5).setPaymentAmount(new BigDecimal("-1.88"));

		payments.add(createMockPolicyPayment(7L, 1L, "101", "P", "2015-02-01", "2015-02-11", "2015-02-15", "APTC", new BigDecimal("26.79"), "APPV"));
		payments.add(createMockPolicyPayment(8L, 1L, "101", "P", "2015-02-01", "2015-02-11", "2015-02-15", "CSR", new BigDecimal("8.93"), "APPV"));
		payments.add(createMockPolicyPayment(9L, 1L, "101", "p", "2015-02-01", "2015-02-11", "2015-02-15", "UF", new BigDecimal("35.71"), "APPV"));
		payments.get(8).setPaymentAmount(new BigDecimal("-1.25"));

		payments.add(createMockPolicyPayment(10L, 1L, "101", "R", "2015-02-01", "2015-02-16", "2015-02-28", "APTC", new BigDecimal("92.86"), "APPV"));
		payments.add(createMockPolicyPayment(11L, 1L, "101", "P", "2015-02-01", "2015-02-16", "2015-02-28", "CSR", new BigDecimal("23.21"), "APPV"));
		payments.add(createMockPolicyPayment(12L, 1L, "101", "p", "2015-02-01", "2015-02-16", "2015-02-28", "UF", new BigDecimal("116.07"), "APPV"));
		payments.get(11).setPaymentAmount(new BigDecimal("-4.06"));

		policyDetailDTO.setPolicyPayments(payments);


		return policyDetailDTO;
	}

	public static PolicyDetailDTO createMockPolicyPaymentDataForRetroPartialMonth_Scenario7_APTC() {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		premiums.add(createMockPolicyPremium(1, "2015-01-01", "2015-02-15", new BigDecimal(100), new BigDecimal(50), new BigDecimal(150)));
		premiums.add(createMockPolicyPremium(1, "2015-02-16", "2015-12-31", new BigDecimal(200), new BigDecimal(50), new BigDecimal(250)));

		policyDetailDTO.setPolicyPremiums(premiums);

		//Payments
		List<PolicyPaymentTransDTO> payments = new ArrayList<PolicyPaymentTransDTO>();
		payments.add(createMockPolicyPayment(1L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-10", "APTC", new BigDecimal("35.71"), "APPV"));
		payments.add(createMockPolicyPayment(2L, 1L, "101", "R", "2015-02-01", "2015-02-11", "2015-02-15", "APTC", new BigDecimal("26.79"), "APPV"));
		payments.add(createMockPolicyPayment(3L, 1L, "101", "R", "2015-02-01", "2015-02-16", "2015-02-28", "APTC", new BigDecimal("92.86"), "APPV"));

		policyDetailDTO.setPolicyPayments(payments);


		return policyDetailDTO;
	}

	public static PolicyDetailDTO createMockPolicyPaymentData(DateTime sd, DateTime ed, BigDecimal aptc, BigDecimal csr, BigDecimal tpa) {

		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		premiums.add(createMockPolicyPremium(1, sd.toString(), ed.toString(), aptc, csr, tpa));
		policyDetailDTO.setPolicyPremiums(premiums);
		//Payments, none
		policyDetailDTO.setPolicyPayments(new ArrayList<PolicyPaymentTransDTO>());

		return policyDetailDTO;
	}

	public static PolicyDetailDTO createMockPolicyPaymentDataForPremiumStartDate_GT_PmtCoverageEndDate() {
		
		PolicyDetailDTO policyDetailDTO = new PolicyDetailDTO();
		List<PolicyPremium> premiums = new ArrayList<PolicyPremium>();
		premiums.add(createMockPolicyPremium(1, "2015-02-16", "2015-12-31", new BigDecimal(200), new BigDecimal(50), new BigDecimal(250)));
		
		policyDetailDTO.setPolicyPremiums(premiums);
		
		//Payments
		List<PolicyPaymentTransDTO> payments = new ArrayList<PolicyPaymentTransDTO>();
		payments.add(createMockPolicyPayment(1L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-10", "APTC", new BigDecimal("35.71"), "APPV"));
		
		policyDetailDTO.setPolicyPayments(payments);

		
		return policyDetailDTO;
	}
	
	/**
	 * Creates org.joda.time.DateTime for the given year, month and day
	 * @param year
	 * @param monthOfYear
	 * @param dayOfMonth
	 * @return
	 */
	public static DateTime getDateFor(int year, int monthOfYear, int dayOfMonth) {
		return getDateFor(year, monthOfYear, dayOfMonth, 0, 0, 0);
	}

	/**
	 * Creates org.joda.time.DateTime with the given parameters
	 * @param year
	 * @param monthOfYear
	 * @param dayOfMonth
	 * @param hourOfDay
	 * @param minuteOfHour
	 * @param secondOfMinute
	 * @return
	 */
	public static DateTime getDateFor(int year, int monthOfYear, int dayOfMonth, int hourOfDay, int minuteOfHour, int secondOfMinute) {		
		return new DateTime(year, monthOfYear, dayOfMonth, hourOfDay, minuteOfHour, secondOfMinute);				
	}

	public static IssuerUserFeeRate createIssuerUserFeeRate(String issuerStateCd, String insrncAplctnTypeCd, double rateVal, boolean isPercent) {
		IssuerUserFeeRate rate = new IssuerUserFeeRate();
		rate.setIssuerUfStateCd(issuerStateCd);
		rate.setInsrncAplctnTypeCd(insrncAplctnTypeCd);
		rate.setIssuerUfCoverageYear("2015");		
		rate.setIssuerUfStartDate(getDateFor(2015, 1, 1));
		rate.setIssuerUfEndDate(getDateFor(2015, 12, 31));
		if(isPercent) {
			rate.setIssuerUfPercent(new BigDecimal(rateVal));
		}
		else {
			rate.setIssuerUfFlatrate(new BigDecimal(rateVal));
		}

		return rate;
	}

	public static List<IssuerUserFeeRate> createIssuerUserFeeRateList() {
		List<IssuerUserFeeRate> list = new ArrayList<IssuerUserFeeRate>();
		list.add(createIssuerUserFeeRate("NY", "1", 3.5, true));
		list.add(createIssuerUserFeeRate("NY", "2", 11, false));		
		return list;
	}
	
	public static Map<String, StateProrationConfiguration> getStateConfigMap(String stateCd, int marketYear,
			ProrationType prorationType) {
		
		StateProrationConfiguration stConfig = new StateProrationConfiguration();
		stConfig.setStateCd(stateCd);
		stConfig.setMarketYear(marketYear);
		stConfig.setProrationTypeCd(prorationType.getValue());
		
		Map<String, StateProrationConfiguration> stateCdMap = new HashMap<>();
		stateCdMap.put(stConfig.getStateCd(), stConfig);
		
		return stateCdMap;
	}
	
	public static void loadStateConfigMap(String stateCd, int marketYear, ProrationType prorationTyp) {
		
		Map<String, StateProrationConfiguration> stateCdMap = RapServiceTestUtil.getStateConfigMap(stateCd, marketYear, prorationTyp);
	
		RapProcessingHelper.getStateProrationConfigMap().put(marketYear, stateCdMap);
	}

}
