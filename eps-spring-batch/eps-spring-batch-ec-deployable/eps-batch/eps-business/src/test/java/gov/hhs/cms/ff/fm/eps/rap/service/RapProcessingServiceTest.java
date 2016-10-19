package gov.hhs.cms.ff.fm.eps.rap.service;

import static gov.hhs.cms.ff.fm.eps.rap.domain.RapConstants.STATUS_APPROVED;
import static gov.hhs.cms.ff.fm.eps.rap.domain.RapConstants.STATUS_PENDING_APPROVAL;
import static gov.hhs.cms.ff.fm.eps.rap.domain.RapConstants.STATUS_PENDING_CYCLE;
import static gov.hhs.cms.ff.fm.eps.rap.domain.RapConstants.TRANSPERIOD_RETROACTIVE;
import static gov.hhs.cms.ff.fm.eps.rap.domain.RapConstants.UF;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import gov.hhs.cms.ff.fm.eps.ep.enums.PolicyStatus;
import gov.hhs.cms.ff.fm.eps.ep.enums.ProrationType;
import gov.hhs.cms.ff.fm.eps.rap.dao.RapDao;
import gov.hhs.cms.ff.fm.eps.rap.domain.PolicyPremium;
import gov.hhs.cms.ff.fm.eps.rap.domain.RapConstants;
import gov.hhs.cms.ff.fm.eps.rap.dto.PolicyDataDTO;
import gov.hhs.cms.ff.fm.eps.rap.dto.PolicyDetailDTO;
import gov.hhs.cms.ff.fm.eps.rap.dto.PolicyPaymentTransDTO;
import gov.hhs.cms.ff.fm.eps.rap.service.impl.RapProcessingServiceImpl;
import gov.hhs.cms.ff.fm.eps.rap.util.CodeDecodesHelper;
import gov.hhs.cms.ff.fm.eps.rap.util.RapProcessingHelper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.collections.CollectionUtils;
import org.easymock.EasyMock;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.test.util.ReflectionTestUtils;

import com.accenture.foundation.common.codetable.CodeRecord;
import com.accenture.foundation.common.exception.ApplicationException;

/**
 * Unit test class for the RapProcessingService
 * 
 * @author girish.padmanabhan
 * 
 */
@RunWith(JUnit4.class)
public class RapProcessingServiceTest extends TestCase {

	RapProcessingServiceImpl rapProcesssingServiceTest;

	private final DateTime DATETIME = new DateTime();
	private final int YEAR = DATETIME.getYear();

	private RapDao mockRapDao;
	private CodeDecodesHelper mockCodeDecodesHelper;

	@Before
	public void setup() throws Exception {
		rapProcesssingServiceTest = new RapProcessingServiceImpl();

		mockRapDao = EasyMock.createMock(RapDao.class);
		mockCodeDecodesHelper = EasyMock.createMock(CodeDecodesHelper.class);

		rapProcesssingServiceTest.setRapDao(mockRapDao);
		rapProcesssingServiceTest.setCodeDecodesHelper(mockCodeDecodesHelper);

	}

	/*
	 * Retroactive Enrollment
	 */
	@Test
	public void process_RetroActiveEnrollment_success() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-01-01 00:00:00", "2014-12-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroEnrollment();

		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);

		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2014-12-25", null);

		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertEquals("PolicyPaymentTransactions size", 3, response.getPolicyPaymentTransactions().size());
		assertTrue("Payment Transaction values comparison", compareRetroActiveEnrollmentResult(response.getPolicyPaymentTransactions(), true));
	}

	/*
	 * Retroactive Enrollment
	 */
	@Test
	public void process_RetroActiveEnrollment_Zero_Amounts_success() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-01-01 00:00:00", "2014-12-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroEnrollment_Zero_Amounts();

		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2014-12-25", null);

		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertEquals("PolicyPaymentTransactions size", 0, response.getPolicyPaymentTransactions().size());
	}

	/*
	 * Retroactive Enrollment
	 */
	@Test
	public void process_RetroActiveEnrollment_Null_Amounts_success() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-01-01 00:00:00", "2014-12-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroEnrollment_Null_Amounts();

		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2014-12-25", null);

		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertEquals("PolicyPaymentTransactions size", 0, response.getPolicyPaymentTransactions().size());
	}

	/*
	 * Retroactive Enrollment - SBM
	 */
	@Test
	public void process_RetroActiveEnrollmentSbm_success() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-01-01 00:00:00", "2014-12-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroEnrollment();
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2014-12-25", null);

		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertEquals("PolicyPaymentTransactions size", 3, response.getPolicyPaymentTransactions().size());
		assertTrue("Payment Transaction values comparison", compareRetroActiveEnrollmentResult(response.getPolicyPaymentTransactions(), false));
	}

	private boolean compareRetroActiveEnrollmentResult(List<PolicyPaymentTransDTO> paymentTransactions, boolean isFfm) {

		PolicyPaymentTransDTO retroAPTCPaymentTransDTO = paymentTransactions.get(0);
		assertEquals("Policy version id", 1, retroAPTCPaymentTransDTO.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroAPTCPaymentTransDTO.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroAPTCPaymentTransDTO.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), retroAPTCPaymentTransDTO.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-01-01"), retroAPTCPaymentTransDTO.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-01-31"), retroAPTCPaymentTransDTO.getPaymentCoverageEndDate());
		assertEquals("Program Type", "APTC", retroAPTCPaymentTransDTO.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroAPTCPaymentTransDTO.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(50).doubleValue(), retroAPTCPaymentTransDTO.getPaymentAmount().doubleValue());
		assertEquals("TPA", new BigDecimal(100).doubleValue(), retroAPTCPaymentTransDTO.getTotalPremiumAmount().doubleValue());
		assertEquals("TPA", 31, retroAPTCPaymentTransDTO.getProrationDaysOfCoverageNum().intValue());
		assertNotNull("MGP Id", retroAPTCPaymentTransDTO.getMarketplaceGroupPolicyId());
		
		PolicyPaymentTransDTO retroCSRPaymentTransDTO = paymentTransactions.get(1);
		assertEquals("Policy version id", 1, retroCSRPaymentTransDTO.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroCSRPaymentTransDTO.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroCSRPaymentTransDTO.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), retroCSRPaymentTransDTO.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-01-01"), retroAPTCPaymentTransDTO.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-01-31"), retroAPTCPaymentTransDTO.getPaymentCoverageEndDate());
		assertEquals("Program Type", "CSR", retroCSRPaymentTransDTO.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroAPTCPaymentTransDTO.getLastPaymentProcStatusTypeCd());
		assertEquals("CSR Amount", new BigDecimal(25).doubleValue(), retroCSRPaymentTransDTO.getPaymentAmount().doubleValue());
		assertEquals("TPA", new BigDecimal(100).doubleValue(), retroCSRPaymentTransDTO.getTotalPremiumAmount().doubleValue());
		assertEquals("TPA", 31, retroCSRPaymentTransDTO.getProrationDaysOfCoverageNum().intValue());
		assertNotNull("MGP Id", retroCSRPaymentTransDTO.getMarketplaceGroupPolicyId());
		
		if (isFfm) {
			PolicyPaymentTransDTO retroUFPaymentTransDTO = paymentTransactions.get(2);
			assertEquals("Policy version id", 1, retroUFPaymentTransDTO.getPolicyVersionId().longValue());
			assertEquals("Policy id", "101", retroUFPaymentTransDTO.getExchangePolicyId());
			assertEquals("Trans Type", "R", retroUFPaymentTransDTO.getTransPeriodTypeCd());
			assertEquals("Coverage Dt", new DateTime("2015-01-01"), retroUFPaymentTransDTO.getCoverageDate());
			assertEquals("Coverage Start Dt", new DateTime("2015-01-01"), retroAPTCPaymentTransDTO.getPaymentCoverageStartDate());
			assertEquals("Coverage End Dt", new DateTime("2015-01-31"), retroAPTCPaymentTransDTO.getPaymentCoverageEndDate());
			assertEquals("Program Type", "UF", retroUFPaymentTransDTO.getFinancialProgramTypeCd());
			assertEquals("Trans Type", "PCYC", retroUFPaymentTransDTO.getLastPaymentProcStatusTypeCd());
			assertEquals("UF/TP Amount", new BigDecimal(100).doubleValue(), retroUFPaymentTransDTO.getTotalPremiumAmount().doubleValue());
			assertNotNull("MGP Id", retroUFPaymentTransDTO.getMarketplaceGroupPolicyId());
		}
		return true;
	}

	/*
	 * Retroactive Change
	 */
	@Test
	public void process_RetroActiveChange_success() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-02-01 00:00:00", "2015-01-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroChange(true);
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(2, "101", "2015-01-28", null);

		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertEquals("PolicyPaymentTransactions size", 4, response.getPolicyPaymentTransactions().size());
		assertTrue("Payment Transaction values comparison", compareRetroActiveChangeResult(response.getPolicyPaymentTransactions()));
	}

	private boolean compareRetroActiveChangeResult(List<PolicyPaymentTransDTO> paymentTransactions) {

		PolicyPaymentTransDTO retroAPTC = paymentTransactions.get(0);
		assertEquals("Policy version id", 2, retroAPTC.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroAPTC.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroAPTC.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), retroAPTC.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-02-01"), retroAPTC.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-02-28"), retroAPTC.getPaymentCoverageEndDate());
		assertEquals("Program Type", "APTC", retroAPTC.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroAPTC.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(-50).doubleValue(), retroAPTC.getPaymentAmount().doubleValue());
		assertEquals("Reversal Ref Trans id", 4, retroAPTC.getParentPolicyPaymentTransId().longValue());
		assertNotNull("MGP Id", retroAPTC.getMarketplaceGroupPolicyId());
		
		PolicyPaymentTransDTO retroAPTCPayment2 = paymentTransactions.get(1);
		assertEquals("Policy version id", 2, retroAPTCPayment2.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroAPTCPayment2.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroAPTCPayment2.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), retroAPTCPayment2.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-02-01"), retroAPTCPayment2.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-02-28"), retroAPTCPayment2.getPaymentCoverageEndDate());
		assertEquals("Program Type", "APTC", retroAPTCPayment2.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroAPTCPayment2.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(80).doubleValue(), retroAPTCPayment2.getPaymentAmount().doubleValue());
		assertNull("Reversal Ref Trans id", retroAPTCPayment2.getParentPolicyPaymentTransId());
		assertNotNull("MGP Id", retroAPTCPayment2.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO retroCSR = paymentTransactions.get(2);
		assertEquals("Policy version id", 2, retroCSR.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroCSR.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroCSR.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), retroCSR.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-02-01"), retroCSR.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-02-28"), retroCSR.getPaymentCoverageEndDate());
		assertEquals("Program Type", "CSR", retroCSR.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroCSR.getLastPaymentProcStatusTypeCd());
		assertEquals("CSR Amount", new BigDecimal(-25).doubleValue(), retroCSR.getPaymentAmount().doubleValue());
		assertEquals("Reversal Ref Trans id", 5, retroCSR.getParentPolicyPaymentTransId().longValue());
		assertNotNull("MGP Id", retroCSR.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO retroCSR2 = paymentTransactions.get(3);
		assertEquals("Policy version id", 2, retroCSR2.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroCSR2.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroCSR2.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), retroCSR2.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-02-01"), retroCSR2.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-02-28"), retroCSR2.getPaymentCoverageEndDate());
		assertEquals("Program Type", "CSR", retroCSR2.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroCSR2.getLastPaymentProcStatusTypeCd());
		assertEquals("CSR Amount", new BigDecimal(50).doubleValue(), retroCSR2.getPaymentAmount().doubleValue());
		assertNull("Reversal Ref Trans id", retroCSR2.getParentPolicyPaymentTransId());
		assertNotNull("MGP Id", retroCSR2.getMarketplaceGroupPolicyId());

		return true;
	}

	/*
	 * Retroactive Change
	 */
	@Test
	public void process_RetroActiveChange_Zero_Amounts_success() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-02-01 00:00:00", "2015-01-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroChange_Zero_Amounts(true);
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(2, "101", "2015-01-28", null);

		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertEquals("PolicyPaymentTransactions size", 2, response.getPolicyPaymentTransactions().size());
		assertTrue("Payment Transaction values comparison", compareRetroActiveChnageResult_Zero_Amounts(response.getPolicyPaymentTransactions()));
	}

	/*
	 * Retroactive Change
	 */
	@Test
	public void process_RetroActiveChange_Null_Amounts_success() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-02-01 00:00:00", "2015-01-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroChange_Null_Amounts(true);
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(2, "101", "2015-01-28", null);

		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertEquals("PolicyPaymentTransactions size", 2, response.getPolicyPaymentTransactions().size());
		assertTrue("Payment Transaction values comparison", compareRetroActiveChnageResult_Zero_Amounts(response.getPolicyPaymentTransactions()));
	}

	private boolean compareRetroActiveChnageResult_Zero_Amounts(List<PolicyPaymentTransDTO> paymentTransactions) {

		PolicyPaymentTransDTO retroAPTC = paymentTransactions.get(0);
		assertEquals("Policy version id", 2, retroAPTC.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroAPTC.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroAPTC.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), retroAPTC.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-02-01"), retroAPTC.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-02-28"), retroAPTC.getPaymentCoverageEndDate());
		assertEquals("Program Type", "APTC", retroAPTC.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroAPTC.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(-50).doubleValue(), retroAPTC.getPaymentAmount().doubleValue());
		assertEquals("Reversal Ref Trans id", 4, retroAPTC.getParentPolicyPaymentTransId().longValue());
		assertNotNull("MGP Id", retroAPTC.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO retroCSR = paymentTransactions.get(1);
		assertEquals("Policy version id", 2, retroCSR.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroCSR.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroCSR.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), retroCSR.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-02-01"), retroCSR.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-02-28"), retroCSR.getPaymentCoverageEndDate());
		assertEquals("Program Type", "CSR", retroCSR.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroCSR.getLastPaymentProcStatusTypeCd());
		assertEquals("CSR Amount", new BigDecimal(-25).doubleValue(), retroCSR.getPaymentAmount().doubleValue());
		assertEquals("Reversal Ref Trans id", 5, retroCSR.getParentPolicyPaymentTransId().longValue());
		assertNotNull("MGP Id", retroCSR.getMarketplaceGroupPolicyId());

		return true;
	}

	/*
	 * Retroactive Termination
	 */
	@Test
	public void process_RetroTerm_success() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-03-01 00:00:00", "2015-02-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroTerm("APPV", true);
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(3, "101", "2015-02-22", "2015-02-28");

		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertEquals("PolicyPaymentTransactions size", 3, response.getPolicyPaymentTransactions().size());
		assertTrue("Payment Transaction values comparison", compareRetroActiveTermResult(response.getPolicyPaymentTransactions(), true));
	}

	/*
	 * Retroactive Termination
	 */
	@Test
	public void process_RetroTerm_success_Noise() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-03-01 00:00:00", "2015-02-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroTerm("NOISE", true);
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(3, "101", "2015-02-22", "2015-02-28");

		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertEquals("PolicyPaymentTransactions size", 3, response.getPolicyPaymentTransactions().size());
		assertTrue("Payment Transaction values comparison", compareRetroActiveTermResult(response.getPolicyPaymentTransactions(), true));
	}
	
	/*
	 * Retroactive Termination
	 */
	@Test
	public void process_RetroTerm_EndDtLTStartDt() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-03-01 00:00:00", "2015-02-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroTerm_EndDtLTStartDt("APPV");
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(3, "101", "2015-02-22", "2014-12-31");

		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertEquals("PolicyPaymentTransactions size", 9, response.getPolicyPaymentTransactions().size());
		assertTrue("Payment Transaction values comparison", compareRetroActiveTermResultEndDtLTStartDt(response.getPolicyPaymentTransactions(), true));
	}

	/*
	 * Retroactive Termination
	 */
	@Test
	public void process_RetroTerm_EndDtLTStartDt_Prorating() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-03-01 00:00:00", "2015-02-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroTerm_EndDtLTStartDt("APPV");
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(3, "101", "2015-02-22", "2014-12-31");

		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertEquals("PolicyPaymentTransactions size", 9, response.getPolicyPaymentTransactions().size());
		assertTrue("Payment Transaction values comparison", compareRetroActiveTermResultEndDtLTStartDt(response.getPolicyPaymentTransactions(), true));
	}

	/*
	 * Retroactive Termination Pending Cycle
	 */
	@Test
	public void process_RetroTerm_success_PendingCycle() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-03-01 00:00:00", "2015-02-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroTerm("PCYC", true);
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(3, "101", "2015-02-22", "2015-02-28");

		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertEquals("PolicyPaymentTransactions size", 3, response.getPolicyPaymentTransactions().size());
		assertTrue("Payment Transaction values comparison", compareRetroActiveTermResultPendingCycle(response.getPolicyPaymentTransactions(), true));
	}

	/*
	 * Retroactive Termination Pending Cycle
	 */
	@Test
	public void process_RetroTerm_success_EndDtLTStartDt_PendingCycle() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-03-01 00:00:00", "2015-02-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroTerm_EndDtLTStartDt("PCYC");
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(3, "101", "2015-02-22", "2014-12-31");

		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertEquals("PolicyPaymentTransactions size", 9, response.getPolicyPaymentTransactions().size());
		assertTrue("Payment Transaction values comparison", compareRetroActiveTermResultPendingCycle_EndDtLTStartDt_PendingCycle(response.getPolicyPaymentTransactions(), true));
	}

	/*
	 * Retroactive Future Termination Reversal
	 */
	@Test
	public void process_RetroTermFutureReversal_success() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2014-11-01 00:00:00", "2014-10-05 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroFutureReversalTerm("APPV");
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(2, "101", "2014-10-30", "2014-10-31");
		policyVersion.setPolicyStartDate(new DateTime("2014-10-01"));
		policyVersion.setIssuerStartDate(policyVersion.getPolicyStartDate().withDayOfYear(1));
		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertEquals("PolicyPaymentTransactions size", 3, response.getPolicyPaymentTransactions().size());
		assertTrue("Payment Transaction values comparison", compareRetroActiveTermFutureReversalResult(response.getPolicyPaymentTransactions(), true));
	}	
	
	@Test
	public void process_RetroTermFutureReversal_Noise_success() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2014-11-01 00:00:00", "2014-10-05 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroFutureReversalTerm("NOISE");
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(2, "101", "2014-10-30", "2014-10-31");
		policyVersion.setPolicyStartDate(new DateTime("2014-10-01"));
		policyVersion.setIssuerStartDate(policyVersion.getPolicyStartDate().withDayOfYear(1));
		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertEquals("PolicyPaymentTransactions size", 3, response.getPolicyPaymentTransactions().size());
		assertTrue("Payment Transaction values comparison", compareRetroActiveTermFutureReversalResult(response.getPolicyPaymentTransactions(), true));
	}

	/*
	 * Retroactive Future Termination - Reversal of Pending Cycle Payments
	 */
	@Test
	public void process_RetroTermFutureReversal__PendingCycle_success() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2014-11-01 00:00:00", "2014-10-05 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroFutureReversalTerm("PCYC");
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(2, "101", "2014-10-30", "2014-10-31");
		policyVersion.setPolicyStartDate(new DateTime("2014-10-01"));
		policyVersion.setIssuerStartDate(policyVersion.getPolicyStartDate().withDayOfYear(1));
		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertEquals("PolicyPaymentTransactions size", 3, response.getPolicyPaymentTransactions().size());
		assertTrue("Payment Transaction values comparison", compareRetroActiveTermFutureReversalResultPendingCycle(response.getPolicyPaymentTransactions(), true));
	}

	/*
	 * Retroactive Termination - SBM
	 */
	@Test
	public void process_RetroTermSbm_success() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-03-01 00:00:00", "2015-02-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroTerm("APPV", true);
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(3, "101", "2015-02-22", "2015-02-28");

		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertEquals("PolicyPaymentTransactions size", 3, response.getPolicyPaymentTransactions().size());
		assertTrue("Payment Transaction values comparison", compareRetroActiveTermResult(response.getPolicyPaymentTransactions(), false));
	}

	private boolean compareRetroActiveTermResult(List<PolicyPaymentTransDTO> paymentTransactions, boolean isFfm) {

		PolicyPaymentTransDTO retroAPTC = paymentTransactions.get(0);
		assertEquals("Policy version id", 3, retroAPTC.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroAPTC.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroAPTC.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-03-01"), retroAPTC.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-03-01"), retroAPTC.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-03-31"), retroAPTC.getPaymentCoverageEndDate());
		assertEquals("Program Type", "APTC", retroAPTC.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroAPTC.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(-80).doubleValue(), retroAPTC.getPaymentAmount().doubleValue());
		assertEquals("Reversal Ref Trans id", 11, retroAPTC.getParentPolicyPaymentTransId().longValue());
		assertNotNull("MGP Id", retroAPTC.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO retroCSR = paymentTransactions.get(1);
		assertEquals("Policy version id", 3, retroCSR.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroCSR.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroCSR.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-03-01"), retroCSR.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-03-01"), retroCSR.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-03-31"), retroCSR.getPaymentCoverageEndDate());
		assertEquals("Program Type", "CSR", retroCSR.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroCSR.getLastPaymentProcStatusTypeCd());
		assertEquals("CSR Amount", new BigDecimal(-50).doubleValue(), retroCSR.getPaymentAmount().doubleValue());
		assertEquals("Reversal Ref Trans id", 12, retroCSR.getParentPolicyPaymentTransId().longValue());
		assertNotNull("MGP Id", retroCSR.getMarketplaceGroupPolicyId());

		if(isFfm) {
			PolicyPaymentTransDTO retroUFPaymentTransDTO = paymentTransactions.get(2);
			assertEquals("Policy version id", 3, retroUFPaymentTransDTO.getPolicyVersionId().longValue());
			assertEquals("Policy id", "101", retroUFPaymentTransDTO.getExchangePolicyId());
			assertEquals("Trans Type", "R", retroUFPaymentTransDTO.getTransPeriodTypeCd());
			assertEquals("Coverage Dt", new DateTime("2015-03-01"), retroUFPaymentTransDTO.getCoverageDate());
			assertEquals("Coverage Start Dt", new DateTime("2015-03-01"), retroUFPaymentTransDTO.getPaymentCoverageStartDate());
			assertEquals("Coverage End Dt", new DateTime("2015-03-31"), retroUFPaymentTransDTO.getPaymentCoverageEndDate());
			assertEquals("Program Type", "UF", retroUFPaymentTransDTO.getFinancialProgramTypeCd());
			assertEquals("Trans Type", "PCYC", retroUFPaymentTransDTO.getLastPaymentProcStatusTypeCd());
			assertEquals("TP Amount", new BigDecimal(100).doubleValue(), retroUFPaymentTransDTO.getTotalPremiumAmount().doubleValue());
			assertEquals("UF Amount", new BigDecimal(-100).doubleValue(), retroUFPaymentTransDTO.getPaymentAmount().doubleValue());
			assertEquals("Reversal Ref Trans id", 13, retroUFPaymentTransDTO.getParentPolicyPaymentTransId().longValue());
			assertNotNull("MGP Id", retroUFPaymentTransDTO.getMarketplaceGroupPolicyId());
		}
		return true;
	}

	private boolean compareRetroActiveTermResultEndDtLTStartDt(List<PolicyPaymentTransDTO> paymentTransactions, boolean isSbm) {

		PolicyPaymentTransDTO retroAPTCJan = paymentTransactions.get(0);
		assertEquals("Policy version id", 3, retroAPTCJan.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroAPTCJan.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroAPTCJan.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), retroAPTCJan.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-01-01"), retroAPTCJan.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-01-31"), retroAPTCJan.getPaymentCoverageEndDate());
		assertEquals("Program Type", "APTC", retroAPTCJan.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroAPTCJan.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(-50).doubleValue(), retroAPTCJan.getPaymentAmount().doubleValue());
		assertEquals("Reversal Ref Trans id", 1, retroAPTCJan.getParentPolicyPaymentTransId().longValue());

		PolicyPaymentTransDTO retroCSRJan = paymentTransactions.get(1);
		assertEquals("Policy version id", 3, retroCSRJan.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroCSRJan.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroCSRJan.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), retroCSRJan.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-01-01"), retroCSRJan.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-01-31"), retroCSRJan.getPaymentCoverageEndDate());
		assertEquals("Program Type", "CSR", retroCSRJan.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroCSRJan.getLastPaymentProcStatusTypeCd());
		assertEquals("CSR Amount", new BigDecimal(-25).doubleValue(), retroCSRJan.getPaymentAmount().doubleValue());
		assertEquals("Reversal Ref Trans id", 2, retroCSRJan.getParentPolicyPaymentTransId().longValue());


		PolicyPaymentTransDTO retroAPTCFeb = paymentTransactions.get(3);
		assertEquals("Policy version id", 3, retroAPTCFeb.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroAPTCFeb.getExchangePolicyId());
		assertEquals("Program Type", "APTC", retroAPTCFeb.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "R", retroAPTCFeb.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), retroAPTCFeb.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-02-01"), retroAPTCFeb.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-02-28"), retroAPTCFeb.getPaymentCoverageEndDate());
		assertEquals("Trans Type", "PCYC", retroAPTCFeb.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(-80).doubleValue(), retroAPTCFeb.getPaymentAmount().doubleValue());
		assertEquals("Reversal Ref Trans id", 8, retroAPTCFeb.getParentPolicyPaymentTransId().longValue());

		PolicyPaymentTransDTO retroCSRFeb = paymentTransactions.get(4);
		assertEquals("Policy version id", 3, retroCSRFeb.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroCSRFeb.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroCSRFeb.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), retroCSRFeb.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-02-01"), retroCSRFeb.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-02-28"), retroCSRFeb.getPaymentCoverageEndDate());
		assertEquals("Program Type", "CSR", retroCSRFeb.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroCSRFeb.getLastPaymentProcStatusTypeCd());
		assertEquals("CSR Amount", new BigDecimal(-50).doubleValue(), retroCSRFeb.getPaymentAmount().doubleValue());
		assertEquals("Reversal Ref Trans id", 10, retroCSRFeb.getParentPolicyPaymentTransId().longValue());

		PolicyPaymentTransDTO retroAPTCMar = paymentTransactions.get(6);
		assertEquals("Policy version id", 3, retroAPTCMar.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroAPTCMar.getExchangePolicyId());
		assertEquals("Program Type", "APTC", retroAPTCMar.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "R", retroAPTCMar.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-03-01"), retroAPTCMar.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-03-01"), retroAPTCMar.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-03-31"), retroAPTCMar.getPaymentCoverageEndDate());
		assertEquals("Trans Type", "PCYC", retroAPTCMar.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(-80).doubleValue(), retroAPTCMar.getPaymentAmount().doubleValue());
		assertEquals("Reversal Ref Trans id", 11, retroAPTCMar.getParentPolicyPaymentTransId().longValue());

		PolicyPaymentTransDTO retroCSRMar = paymentTransactions.get(7);
		assertEquals("Policy version id", 3, retroCSRMar.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroCSRMar.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroCSRMar.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-03-01"), retroCSRMar.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-03-01"), retroCSRMar.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-03-31"), retroCSRMar.getPaymentCoverageEndDate());
		assertEquals("Program Type", "CSR", retroCSRMar.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroCSRMar.getLastPaymentProcStatusTypeCd());
		assertEquals("CSR Amount", new BigDecimal(-50).doubleValue(), retroCSRMar.getPaymentAmount().doubleValue());
		assertEquals("Reversal Ref Trans id", 12, retroCSRMar.getParentPolicyPaymentTransId().longValue());

		if(isSbm) {
			PolicyPaymentTransDTO retroUFPaymentTransDTOJan = paymentTransactions.get(2);
			assertEquals("Policy version id", 3, retroUFPaymentTransDTOJan.getPolicyVersionId().longValue());
			assertEquals("Policy id", "101", retroUFPaymentTransDTOJan.getExchangePolicyId());
			assertEquals("Trans Type", "R", retroUFPaymentTransDTOJan.getTransPeriodTypeCd());
			assertEquals("Coverage Dt", new DateTime("2015-01-01"), retroUFPaymentTransDTOJan.getCoverageDate());
			assertEquals("Coverage Start Dt", new DateTime("2015-01-01"), retroUFPaymentTransDTOJan.getPaymentCoverageStartDate());
			assertEquals("Coverage End Dt", new DateTime("2015-01-31"), retroUFPaymentTransDTOJan.getPaymentCoverageEndDate());
			assertEquals("Program Type", "UF", retroUFPaymentTransDTOJan.getFinancialProgramTypeCd());
			assertEquals("Trans Type", "PCYC", retroUFPaymentTransDTOJan.getLastPaymentProcStatusTypeCd());
			assertEquals("TP Amount", new BigDecimal(100).doubleValue(), retroUFPaymentTransDTOJan.getTotalPremiumAmount().doubleValue());
			assertEquals("UF Amount", new BigDecimal(-100).doubleValue(), retroUFPaymentTransDTOJan.getPaymentAmount().doubleValue());
			assertEquals("Reversal Ref Trans id", 3, retroUFPaymentTransDTOJan.getParentPolicyPaymentTransId().longValue());

			PolicyPaymentTransDTO retroUFPaymentTransDTOFeb = paymentTransactions.get(5);
			assertEquals("Policy version id", 3, retroUFPaymentTransDTOFeb.getPolicyVersionId().longValue());
			assertEquals("Policy id", "101", retroUFPaymentTransDTOFeb.getExchangePolicyId());
			assertEquals("Trans Type", "R", retroUFPaymentTransDTOFeb.getTransPeriodTypeCd());
			assertEquals("Coverage Dt", new DateTime("2015-02-01"), retroUFPaymentTransDTOFeb.getCoverageDate());
			assertEquals("Coverage Start Dt", new DateTime("2015-02-01"), retroUFPaymentTransDTOFeb.getPaymentCoverageStartDate());
			assertEquals("Coverage End Dt", new DateTime("2015-02-28"), retroUFPaymentTransDTOFeb.getPaymentCoverageEndDate());
			assertEquals("Program Type", "UF", retroUFPaymentTransDTOFeb.getFinancialProgramTypeCd());
			assertEquals("Trans Type", "PCYC", retroUFPaymentTransDTOFeb.getLastPaymentProcStatusTypeCd());
			assertEquals("TP Amount", new BigDecimal(100).doubleValue(), retroUFPaymentTransDTOFeb.getTotalPremiumAmount().doubleValue());
			assertEquals("UF Amount", new BigDecimal(-100).doubleValue(), retroUFPaymentTransDTOFeb.getPaymentAmount().doubleValue());
			assertEquals("Reversal Ref Trans id", 6, retroUFPaymentTransDTOFeb.getParentPolicyPaymentTransId().longValue());

			PolicyPaymentTransDTO retroUFPaymentTransDTOMar = paymentTransactions.get(8);
			assertEquals("Policy version id", 3, retroUFPaymentTransDTOMar.getPolicyVersionId().longValue());
			assertEquals("Policy id", "101", retroUFPaymentTransDTOMar.getExchangePolicyId());
			assertEquals("Trans Type", "R", retroUFPaymentTransDTOMar.getTransPeriodTypeCd());
			assertEquals("Coverage Dt", new DateTime("2015-03-01"), retroUFPaymentTransDTOMar.getCoverageDate());
			assertEquals("Coverage Start Dt", new DateTime("2015-03-01"), retroUFPaymentTransDTOMar.getPaymentCoverageStartDate());
			assertEquals("Coverage End Dt", new DateTime("2015-03-31"), retroUFPaymentTransDTOMar.getPaymentCoverageEndDate());
			assertEquals("Program Type", "UF", retroUFPaymentTransDTOMar.getFinancialProgramTypeCd());
			assertEquals("Trans Type", "PCYC", retroUFPaymentTransDTOMar.getLastPaymentProcStatusTypeCd());
			assertEquals("TP Amount", new BigDecimal(100).doubleValue(), retroUFPaymentTransDTOMar.getTotalPremiumAmount().doubleValue());
			assertEquals("UF Amount", new BigDecimal(-100).doubleValue(), retroUFPaymentTransDTOMar.getPaymentAmount().doubleValue());
			assertEquals("Reversal Ref Trans id", 13, retroUFPaymentTransDTOMar.getParentPolicyPaymentTransId().longValue());

		}
		return true;
	}

	private boolean compareRetroActiveTermResultPendingCycle(List<PolicyPaymentTransDTO> paymentTransactions, boolean isSbm) {

		PolicyPaymentTransDTO retroAPTCRepl = paymentTransactions.get(0);
		assertEquals("Policy version id", 2, retroAPTCRepl.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroAPTCRepl.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroAPTCRepl.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-03-01"), retroAPTCRepl.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-03-01"), retroAPTCRepl.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-03-31"), retroAPTCRepl.getPaymentCoverageEndDate());
		assertEquals("Program Type", "APTC", retroAPTCRepl.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "REPL", retroAPTCRepl.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(80).doubleValue(), retroAPTCRepl.getPaymentAmount().doubleValue());
		assertNull("Reversal Ref Trans id", retroAPTCRepl.getParentPolicyPaymentTransId());
		assertTrue("Update Status", retroAPTCRepl.isUpdateStatusRec());

		PolicyPaymentTransDTO retroCSRRepl = paymentTransactions.get(1);
		assertEquals("Policy version id", 2, retroCSRRepl.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroCSRRepl.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroCSRRepl.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-03-01"), retroCSRRepl.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-03-01"), retroCSRRepl.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-03-31"), retroCSRRepl.getPaymentCoverageEndDate());
		assertEquals("Program Type", "CSR", retroCSRRepl.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "REPL", retroCSRRepl.getLastPaymentProcStatusTypeCd());
		assertEquals("CSR Amount", new BigDecimal(50).doubleValue(), retroCSRRepl.getPaymentAmount().doubleValue());
		assertNull("Reversal Ref Trans id", retroCSRRepl.getParentPolicyPaymentTransId());
		assertTrue("Update Status", retroCSRRepl.isUpdateStatusRec());

		if(isSbm) {
			PolicyPaymentTransDTO retroUFRepl = paymentTransactions.get(2);
			assertEquals("Policy version id", 2, retroUFRepl.getPolicyVersionId().longValue());
			assertEquals("Policy id", "101", retroUFRepl.getExchangePolicyId());
			assertEquals("Trans Type", "R", retroUFRepl.getTransPeriodTypeCd());
			assertEquals("Coverage Dt", new DateTime("2015-03-01"), retroUFRepl.getCoverageDate());
			assertEquals("Coverage Start Dt", new DateTime("2015-03-01"), retroUFRepl.getPaymentCoverageStartDate());
			assertEquals("Coverage End Dt", new DateTime("2015-03-31"), retroUFRepl.getPaymentCoverageEndDate());
			assertEquals("Program Type", "UF", retroUFRepl.getFinancialProgramTypeCd());
			assertEquals("Trans Type", "REPL", retroUFRepl.getLastPaymentProcStatusTypeCd());
			assertEquals("TP Amount", new BigDecimal(100).doubleValue(), retroUFRepl.getTotalPremiumAmount().doubleValue());
			assertEquals("UF Amount", new BigDecimal(100).doubleValue(), retroUFRepl.getPaymentAmount().doubleValue());
			assertNull("Reversal Ref Trans id", retroUFRepl.getParentPolicyPaymentTransId());
			assertTrue("Update Status", retroUFRepl.isUpdateStatusRec());

		}
		return true;
	}

	private boolean compareRetroActiveTermResultPendingCycle_EndDtLTStartDt_PendingCycle(List<PolicyPaymentTransDTO> paymentTransactions, boolean isSbm) {

		PolicyPaymentTransDTO retroAPTCReplJan = paymentTransactions.get(0);
		assertEquals("Policy version id", 1, retroAPTCReplJan.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroAPTCReplJan.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroAPTCReplJan.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), retroAPTCReplJan.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-01-01"), retroAPTCReplJan.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-01-31"), retroAPTCReplJan.getPaymentCoverageEndDate());
		assertEquals("Program Type", "APTC", retroAPTCReplJan.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "REPL", retroAPTCReplJan.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(50).doubleValue(), retroAPTCReplJan.getPaymentAmount().doubleValue());
		assertNull("Reversal Ref Trans id", retroAPTCReplJan.getParentPolicyPaymentTransId());
		assertTrue("Update Status", retroAPTCReplJan.isUpdateStatusRec());

		PolicyPaymentTransDTO retroCSRReplJan = paymentTransactions.get(1);
		assertEquals("Policy version id", 1, retroCSRReplJan.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroCSRReplJan.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroCSRReplJan.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), retroCSRReplJan.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-01-01"), retroCSRReplJan.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-01-31"), retroCSRReplJan.getPaymentCoverageEndDate());
		assertEquals("Program Type", "CSR", retroCSRReplJan.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "REPL", retroCSRReplJan.getLastPaymentProcStatusTypeCd());
		assertEquals("CSR Amount", new BigDecimal(25).doubleValue(), retroCSRReplJan.getPaymentAmount().doubleValue());
		assertNull("Reversal Ref Trans id", retroCSRReplJan.getParentPolicyPaymentTransId());
		assertTrue("Update Status", retroCSRReplJan.isUpdateStatusRec());

		PolicyPaymentTransDTO retroAPTCReplFeb = paymentTransactions.get(3);
		assertEquals("Policy version id", 2, retroAPTCReplFeb.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroAPTCReplFeb.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroAPTCReplFeb.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), retroAPTCReplFeb.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-02-01"), retroAPTCReplFeb.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-02-28"), retroAPTCReplFeb.getPaymentCoverageEndDate());
		assertEquals("Program Type", "APTC", retroAPTCReplFeb.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "REPL", retroAPTCReplFeb.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(80).doubleValue(), retroAPTCReplFeb.getPaymentAmount().doubleValue());
		assertNull("Reversal Ref Trans id", retroAPTCReplFeb.getParentPolicyPaymentTransId());
		assertTrue("Update Status", retroAPTCReplFeb.isUpdateStatusRec());

		PolicyPaymentTransDTO retroCSRReplFeb = paymentTransactions.get(4);
		assertEquals("Policy version id", 2, retroCSRReplFeb.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroCSRReplFeb.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroCSRReplFeb.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), retroCSRReplFeb.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-02-01"), retroCSRReplFeb.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-02-28"), retroCSRReplFeb.getPaymentCoverageEndDate());
		assertEquals("Program Type", "CSR", retroCSRReplFeb.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "REPL", retroCSRReplFeb.getLastPaymentProcStatusTypeCd());
		assertEquals("CSR Amount", new BigDecimal(50).doubleValue(), retroCSRReplFeb.getPaymentAmount().doubleValue());
		assertNull("Reversal Ref Trans id", retroCSRReplFeb.getParentPolicyPaymentTransId());
		assertTrue("Update Status", retroCSRReplFeb.isUpdateStatusRec());

		PolicyPaymentTransDTO retroAPTCReplMar = paymentTransactions.get(6);
		assertEquals("Policy version id", 2, retroAPTCReplMar.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroAPTCReplMar.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroAPTCReplMar.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-03-01"), retroAPTCReplMar.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-03-01"), retroAPTCReplMar.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-03-31"), retroAPTCReplMar.getPaymentCoverageEndDate());
		assertEquals("Program Type", "APTC", retroAPTCReplMar.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "REPL", retroAPTCReplMar.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(80).doubleValue(), retroAPTCReplMar.getPaymentAmount().doubleValue());
		assertNull("Reversal Ref Trans id", retroAPTCReplMar.getParentPolicyPaymentTransId());
		assertTrue("Update Status", retroAPTCReplMar.isUpdateStatusRec());

		PolicyPaymentTransDTO retroCSRReplMar = paymentTransactions.get(7);
		assertEquals("Policy version id", 2, retroCSRReplMar.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroCSRReplMar.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroCSRReplMar.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-03-01"), retroCSRReplMar.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-03-01"), retroCSRReplMar.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-03-31"), retroCSRReplMar.getPaymentCoverageEndDate());
		assertEquals("Program Type", "CSR", retroCSRReplMar.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "REPL", retroCSRReplMar.getLastPaymentProcStatusTypeCd());
		assertEquals("CSR Amount", new BigDecimal(50).doubleValue(), retroCSRReplMar.getPaymentAmount().doubleValue());
		assertNull("Reversal Ref Trans id", retroCSRReplMar.getParentPolicyPaymentTransId());
		assertTrue("Update Status", retroCSRReplMar.isUpdateStatusRec());

		if(isSbm) {
			PolicyPaymentTransDTO retroUFReplJan = paymentTransactions.get(2);
			assertEquals("Policy version id", 1, retroUFReplJan.getPolicyVersionId().longValue());
			assertEquals("Policy id", "101", retroUFReplJan.getExchangePolicyId());
			assertEquals("Trans Type", "R", retroUFReplJan.getTransPeriodTypeCd());
			assertEquals("Coverage Dt", new DateTime("2015-01-01"), retroUFReplJan.getCoverageDate());
			assertEquals("Coverage Start Dt", new DateTime("2015-01-01"), retroUFReplJan.getPaymentCoverageStartDate());
			assertEquals("Coverage End Dt", new DateTime("2015-01-31"), retroUFReplJan.getPaymentCoverageEndDate());
			assertEquals("Program Type", "UF", retroUFReplJan.getFinancialProgramTypeCd());
			assertEquals("Trans Type", "REPL", retroUFReplJan.getLastPaymentProcStatusTypeCd());
			assertEquals("TP Amount", new BigDecimal(100).doubleValue(), retroUFReplJan.getTotalPremiumAmount().doubleValue());
			assertEquals("UF Amount", new BigDecimal(100).doubleValue(), retroUFReplJan.getPaymentAmount().doubleValue());
			assertNull("Reversal Ref Trans id", retroUFReplJan.getParentPolicyPaymentTransId());
			assertTrue("Update Status", retroUFReplJan.isUpdateStatusRec());

			PolicyPaymentTransDTO retroUFReplFeb = paymentTransactions.get(5);
			assertEquals("Policy version id", 1, retroUFReplFeb.getPolicyVersionId().longValue());
			assertEquals("Policy id", "101", retroUFReplFeb.getExchangePolicyId());
			assertEquals("Trans Type", "R", retroUFReplFeb.getTransPeriodTypeCd());
			assertEquals("Coverage Dt", new DateTime("2015-02-01"), retroUFReplFeb.getCoverageDate());
			assertEquals("Coverage Start Dt", new DateTime("2015-02-01"), retroUFReplFeb.getPaymentCoverageStartDate());
			assertEquals("Coverage End Dt", new DateTime("2015-02-28"), retroUFReplFeb.getPaymentCoverageEndDate());
			assertEquals("Program Type", "UF", retroUFReplFeb.getFinancialProgramTypeCd());
			assertEquals("Trans Type", "REPL", retroUFReplFeb.getLastPaymentProcStatusTypeCd());
			assertEquals("TP Amount", new BigDecimal(100).doubleValue(), retroUFReplFeb.getTotalPremiumAmount().doubleValue());
			assertEquals("UF Amount", new BigDecimal(100).doubleValue(), retroUFReplFeb.getPaymentAmount().doubleValue());
			assertNull("Reversal Ref Trans id", retroUFReplFeb.getParentPolicyPaymentTransId());
			assertTrue("Update Status", retroUFReplFeb.isUpdateStatusRec());

			PolicyPaymentTransDTO retroUFReplMar = paymentTransactions.get(8);
			assertEquals("Policy version id", 2, retroUFReplMar.getPolicyVersionId().longValue());
			assertEquals("Policy id", "101", retroUFReplMar.getExchangePolicyId());
			assertEquals("Trans Type", "R", retroUFReplMar.getTransPeriodTypeCd());
			assertEquals("Coverage Dt", new DateTime("2015-03-01"), retroUFReplMar.getCoverageDate());
			assertEquals("Coverage Start Dt", new DateTime("2015-03-01"), retroUFReplMar.getPaymentCoverageStartDate());
			assertEquals("Coverage End Dt", new DateTime("2015-03-31"), retroUFReplMar.getPaymentCoverageEndDate());
			assertEquals("Program Type", "UF", retroUFReplMar.getFinancialProgramTypeCd());
			assertEquals("Trans Type", "REPL", retroUFReplMar.getLastPaymentProcStatusTypeCd());
			assertEquals("TP Amount", new BigDecimal(100).doubleValue(), retroUFReplMar.getTotalPremiumAmount().doubleValue());
			assertEquals("UF Amount", new BigDecimal(100).doubleValue(), retroUFReplMar.getPaymentAmount().doubleValue());
			assertNull("Reversal Ref Trans id", retroUFReplMar.getParentPolicyPaymentTransId());
			assertTrue("Update Status", retroUFReplMar.isUpdateStatusRec());

		}
		return true;
	}

	private boolean compareRetroActiveTermFutureReversalResult(List<PolicyPaymentTransDTO> paymentTransactions, boolean isSbm) {

		PolicyPaymentTransDTO retroAPTC = paymentTransactions.get(0);
		assertEquals("Policy version id", 2, retroAPTC.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroAPTC.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroAPTC.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2014-11-01"), retroAPTC.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2014-11-01"), retroAPTC.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2014-11-30"), retroAPTC.getPaymentCoverageEndDate());
		assertEquals("Program Type", "APTC", retroAPTC.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroAPTC.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(-40).doubleValue(), retroAPTC.getPaymentAmount().doubleValue());
		assertEquals("Reversal Ref Trans id", 4, retroAPTC.getParentPolicyPaymentTransId().longValue());
		assertNotNull("MGP Id", retroAPTC.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO retroCSR = paymentTransactions.get(1);
		assertEquals("Policy version id", 2, retroCSR.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroCSR.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroCSR.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2014-11-01"), retroCSR.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2014-11-01"), retroAPTC.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2014-11-30"), retroAPTC.getPaymentCoverageEndDate());
		assertEquals("Program Type", "CSR", retroCSR.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroCSR.getLastPaymentProcStatusTypeCd());
		assertEquals("CSR Amount", new BigDecimal(-50).doubleValue(), retroCSR.getPaymentAmount().doubleValue());
		assertEquals("Reversal Ref Trans id", 5, retroCSR.getParentPolicyPaymentTransId().longValue());
		assertNotNull("MGP Id", retroCSR.getMarketplaceGroupPolicyId());

		if(isSbm) {
			PolicyPaymentTransDTO retroUFPaymentTransDTO = paymentTransactions.get(2);
			assertEquals("Policy version id", 2, retroUFPaymentTransDTO.getPolicyVersionId().longValue());
			assertEquals("Policy id", "101", retroUFPaymentTransDTO.getExchangePolicyId());
			assertEquals("Trans Type", "R", retroUFPaymentTransDTO.getTransPeriodTypeCd());
			assertEquals("Coverage Dt", new DateTime("2014-11-01"), retroUFPaymentTransDTO.getCoverageDate());
			assertEquals("Coverage Start Dt", new DateTime("2014-11-01"), retroAPTC.getPaymentCoverageStartDate());
			assertEquals("Coverage End Dt", new DateTime("2014-11-30"), retroAPTC.getPaymentCoverageEndDate());
			assertEquals("Program Type", "UF", retroUFPaymentTransDTO.getFinancialProgramTypeCd());
			assertEquals("Trans Type", "PCYC", retroUFPaymentTransDTO.getLastPaymentProcStatusTypeCd());
			assertEquals("TP Amount", new BigDecimal(80).doubleValue(), retroUFPaymentTransDTO.getTotalPremiumAmount().doubleValue());
			assertEquals("UF Amount", new BigDecimal(-80).doubleValue(), retroUFPaymentTransDTO.getPaymentAmount().doubleValue());
			assertEquals("Reversal Ref Trans id", 6, retroUFPaymentTransDTO.getParentPolicyPaymentTransId().longValue());
			assertNotNull("MGP Id", retroUFPaymentTransDTO.getMarketplaceGroupPolicyId());
		}
		return true;
	}

	private boolean compareRetroActiveTermFutureReversalResultPendingCycle(List<PolicyPaymentTransDTO> paymentTransactions, boolean isSbm) {

		PolicyPaymentTransDTO retroAPTC = paymentTransactions.get(0);
		assertEquals("Policy version id", 1, retroAPTC.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroAPTC.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroAPTC.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2014-11-01"), retroAPTC.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2014-11-01"), retroAPTC.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2014-11-30"), retroAPTC.getPaymentCoverageEndDate());
		assertEquals("Program Type", "APTC", retroAPTC.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "REPL", retroAPTC.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(40).doubleValue(), retroAPTC.getPaymentAmount().doubleValue());
		assertNull("Reversal Ref Trans id", retroAPTC.getParentPolicyPaymentTransId());
		assertTrue("Update Status", retroAPTC.isUpdateStatusRec());

		PolicyPaymentTransDTO retroCSR = paymentTransactions.get(1);
		assertEquals("Policy version id", 1, retroCSR.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroCSR.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroCSR.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2014-11-01"), retroCSR.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2014-11-01"), retroAPTC.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2014-11-30"), retroAPTC.getPaymentCoverageEndDate());
		assertEquals("Program Type", "CSR", retroCSR.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "REPL", retroCSR.getLastPaymentProcStatusTypeCd());
		assertEquals("CSR Amount", new BigDecimal(50).doubleValue(), retroCSR.getPaymentAmount().doubleValue());
		assertNull("Reversal Ref Trans id", retroCSR.getParentPolicyPaymentTransId());
		assertTrue("Update Status", retroCSR.isUpdateStatusRec());

		if(isSbm) {
			PolicyPaymentTransDTO retroUFPaymentTransDTO = paymentTransactions.get(2);
			assertEquals("Policy version id", 1, retroUFPaymentTransDTO.getPolicyVersionId().longValue());
			assertEquals("Policy id", "101", retroUFPaymentTransDTO.getExchangePolicyId());
			assertEquals("Trans Type", "R", retroUFPaymentTransDTO.getTransPeriodTypeCd());
			assertEquals("Coverage Dt", new DateTime("2014-11-01"), retroUFPaymentTransDTO.getCoverageDate());
			assertEquals("Coverage Start Dt", new DateTime("2014-11-01"), retroAPTC.getPaymentCoverageStartDate());
			assertEquals("Coverage End Dt", new DateTime("2014-11-30"), retroAPTC.getPaymentCoverageEndDate());
			assertEquals("Program Type", "UF", retroUFPaymentTransDTO.getFinancialProgramTypeCd());
			assertEquals("Trans Type", "REPL", retroUFPaymentTransDTO.getLastPaymentProcStatusTypeCd());
			assertEquals("UF Amount", new BigDecimal(80).doubleValue(), retroUFPaymentTransDTO.getTotalPremiumAmount().doubleValue());
			assertNull("Reversal Ref Trans id", retroUFPaymentTransDTO.getParentPolicyPaymentTransId());
			assertTrue("Update Status", retroUFPaymentTransDTO.isUpdateStatusRec());

		}
		return true;
	}

	/*
	 * Retroactive Reinstatement
	 */
	@Test
	public void process_RetroReinstatement_success() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-04-01 00:00:00", "2015-03-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroReinstatement("APPV", true);
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(4, "101", "2015-03-27", null);

		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertEquals("PolicyPaymentTransactions size", 6, response.getPolicyPaymentTransactions().size());
		assertTrue("Payment Transaction values comparison", compareRetroActiveReinstatementResult(response.getPolicyPaymentTransactions()));
	}
	
	@Test
	public void process_RetroReinstatement_Noise_success() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-04-01 00:00:00", "2015-03-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroReinstatement("NOISE", true);
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(4, "101", "2015-03-27", null);

		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertEquals("PolicyPaymentTransactions size", 6, response.getPolicyPaymentTransactions().size());
		assertTrue("Payment Transaction values comparison", compareRetroActiveReinstatementResult(response.getPolicyPaymentTransactions()));
	}

	private boolean compareRetroActiveReinstatementResult(List<PolicyPaymentTransDTO> paymentTransactions) {

		PolicyPaymentTransDTO retroAPTC = paymentTransactions.get(0);
		assertEquals("Policy version id", 4, retroAPTC.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroAPTC.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroAPTC.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-03-01"), retroAPTC.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-03-01"), retroAPTC.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-03-31"), retroAPTC.getPaymentCoverageEndDate());
		assertEquals("Program Type", "APTC", retroAPTC.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroAPTC.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(80).doubleValue(), retroAPTC.getPaymentAmount().doubleValue());
		assertNotNull("MGP Id", retroAPTC.getMarketplaceGroupPolicyId());
		
		PolicyPaymentTransDTO retroCSR = paymentTransactions.get(1);
		assertEquals("Policy version id", 4, retroCSR.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroCSR.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroCSR.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-03-01"), retroCSR.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-03-01"), retroCSR.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-03-31"), retroCSR.getPaymentCoverageEndDate());
		assertEquals("Program Type", "CSR", retroCSR.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroCSR.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(50).doubleValue(), retroCSR.getPaymentAmount().doubleValue());
		assertNotNull("MGP Id", retroCSR.getMarketplaceGroupPolicyId());
		
		PolicyPaymentTransDTO retroUFPaymentTransDTO = paymentTransactions.get(2);
		assertEquals("Policy version id", 4, retroUFPaymentTransDTO.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroUFPaymentTransDTO.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroUFPaymentTransDTO.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-03-01"), retroUFPaymentTransDTO.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-03-01"), retroUFPaymentTransDTO.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-03-31"), retroUFPaymentTransDTO.getPaymentCoverageEndDate());
		assertEquals("Program Type", "UF", retroUFPaymentTransDTO.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroUFPaymentTransDTO.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(100).doubleValue(), retroUFPaymentTransDTO.getTotalPremiumAmount().doubleValue());
		assertNotNull("MGP Id", retroUFPaymentTransDTO.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO retroAPTC2 = paymentTransactions.get(3);
		assertEquals("Policy version id", 4, retroAPTC2.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroAPTC2.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroAPTC2.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-04-01"), retroAPTC2.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-04-01"), retroAPTC2.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-04-30"), retroAPTC2.getPaymentCoverageEndDate());
		assertEquals("Program Type", "APTC", retroAPTC2.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroAPTC2.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(80).doubleValue(), retroAPTC2.getPaymentAmount().doubleValue());
		assertNotNull("MGP Id", retroAPTC2.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO retroCSR2 = paymentTransactions.get(4);
		assertEquals("Policy version id", 4, retroCSR2.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroCSR2.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroCSR2.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-04-01"), retroCSR2.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-04-01"), retroCSR2.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-04-30"), retroCSR2.getPaymentCoverageEndDate());
		assertEquals("Program Type", "CSR", retroCSR2.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroCSR2.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(50).doubleValue(), retroCSR2.getPaymentAmount().doubleValue());
		assertNotNull("MGP Id", retroCSR2.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO retroUF2 = paymentTransactions.get(5);
		assertEquals("Policy version id", 4, retroUF2.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroUF2.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroUF2.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-04-01"), retroUF2.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-04-01"), retroUF2.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-04-30"), retroUF2.getPaymentCoverageEndDate());
		assertEquals("Program Type", "UF", retroUF2.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroUF2.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(100).doubleValue(), retroUF2.getTotalPremiumAmount().doubleValue());		
		assertNotNull("MGP Id", retroUF2.getMarketplaceGroupPolicyId());
		
		return true;
	}

	/*
	 * Retroactive Change for a Period in the Past
	 */
	@Test
	public void process_RetroChangePastPeriod_success() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-04-01 00:00:00", "2015-03-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroChangePastPeriod("APPV", true);
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(5, "101", "2015-04-02", null);

		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertEquals("PolicyPaymentTransactions size", 4, response.getPolicyPaymentTransactions().size());
		assertTrue("Payment Transaction values comparison", compareRetroActiveChangePastPeriod(response.getPolicyPaymentTransactions()));
	}
	
	@Test
	public void process_RetroChangePastPeriod_Noise_success() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-04-01 00:00:00", "2015-03-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroChangePastPeriod("NOISE", true);
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(5, "101", "2015-04-02", null);

		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertEquals("PolicyPaymentTransactions size", 4, response.getPolicyPaymentTransactions().size());
		assertTrue("Payment Transaction values comparison", compareRetroActiveChangePastPeriod(response.getPolicyPaymentTransactions()));
	}

	private boolean compareRetroActiveChangePastPeriod(List<PolicyPaymentTransDTO> paymentTransactions) {

		PolicyPaymentTransDTO retroAPTC = paymentTransactions.get(0);
		assertEquals("Policy version id", 5, retroAPTC.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroAPTC.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroAPTC.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), retroAPTC.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-01-01"), retroAPTC.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-01-31"), retroAPTC.getPaymentCoverageEndDate());
		assertEquals("Program Type", "APTC", retroAPTC.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroAPTC.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(-50).doubleValue(), retroAPTC.getPaymentAmount().doubleValue());
		assertNotNull("MGP Id", retroAPTC.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO retroAPTC2 = paymentTransactions.get(1);
		assertEquals("Policy version id", 5, retroAPTC2.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroAPTC2.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroAPTC2.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), retroAPTC2.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-01-01"), retroAPTC2.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-01-31"), retroAPTC2.getPaymentCoverageEndDate());
		assertEquals("Program Type", "APTC", retroAPTC2.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroAPTC2.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(70).doubleValue(), retroAPTC2.getPaymentAmount().doubleValue());
		assertNotNull("MGP Id", retroAPTC2.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO retroCSR = paymentTransactions.get(2);
		assertEquals("Policy version id", 5, retroCSR.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroCSR.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroCSR.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), retroCSR.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-01-01"), retroCSR.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-01-31"), retroCSR.getPaymentCoverageEndDate());
		assertEquals("Program Type", "CSR", retroCSR.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroCSR.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(-25).doubleValue(), retroCSR.getPaymentAmount().doubleValue());
		assertNotNull("MGP Id", retroCSR.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO retroCSR2 = paymentTransactions.get(3);
		assertEquals("Policy version id", 5, retroCSR2.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroCSR2.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroCSR2.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), retroCSR2.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-01-01"), retroCSR2.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-01-31"), retroCSR2.getPaymentCoverageEndDate());
		assertEquals("Program Type", "CSR", retroCSR2.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroCSR2.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(35).doubleValue(), retroCSR2.getPaymentAmount().doubleValue());
		assertNotNull("MGP Id", retroCSR2.getMarketplaceGroupPolicyId());

		return true;
	}

	/*
	 * Multiple Retroactive Change for Multiple Periods in the Past
	 */
	@Test
	public void process_RetroChangeMultipleChnagesForPmtMonth_success() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-04-01 00:00:00", "2015-03-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroChangeMultiple(true);
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(6, "101", "2015-04-05", null);

		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertEquals("PolicyPaymentTransactions size", 4, response.getPolicyPaymentTransactions().size());
		assertTrue("Payment Transaction values comparison", compareRetroActiveChangeMultiple(response.getPolicyPaymentTransactions()));
	}

	private boolean compareRetroActiveChangeMultiple(List<PolicyPaymentTransDTO> paymentTransactions) {

		PolicyPaymentTransDTO retroAPTC = paymentTransactions.get(0);
		assertEquals("Policy version id", 4, retroAPTC.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroAPTC.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroAPTC.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-04-01"), retroAPTC.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-04-01"), retroAPTC.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-04-30"), retroAPTC.getPaymentCoverageEndDate());
		assertEquals("Program Type", "APTC", retroAPTC.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "REPL", retroAPTC.getLastPaymentProcStatusTypeCd());
		assertEquals("Insert/Update", true, retroAPTC.isUpdateStatusRec());
		assertEquals("APTC Amount", new BigDecimal(80).doubleValue(), retroAPTC.getPaymentAmount().doubleValue());

		PolicyPaymentTransDTO retroAPTC2 = paymentTransactions.get(1);
		assertEquals("Policy version id", 6, retroAPTC2.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroAPTC2.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroAPTC2.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-04-01"), retroAPTC2.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-04-01"), retroAPTC2.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-04-30"), retroAPTC2.getPaymentCoverageEndDate());
		assertEquals("Program Type", "APTC", retroAPTC2.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroAPTC2.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(100).doubleValue(), retroAPTC2.getPaymentAmount().doubleValue());
		assertNotNull("MGP Id", retroAPTC2.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO retroCSR = paymentTransactions.get(2);
		assertEquals("Policy version id", 4, retroCSR.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroCSR.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroCSR.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-04-01"), retroCSR.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-04-01"), retroCSR.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-04-30"), retroCSR.getPaymentCoverageEndDate());
		assertEquals("Program Type", "CSR", retroCSR.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "REPL", retroCSR.getLastPaymentProcStatusTypeCd());
		assertEquals("Insert/Update", true, retroCSR.isUpdateStatusRec());
		assertEquals("APTC Amount", new BigDecimal(50).doubleValue(), retroCSR.getPaymentAmount().doubleValue());

		PolicyPaymentTransDTO retroCSR2 = paymentTransactions.get(3);
		assertEquals("Policy version id", 6, retroCSR2.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroCSR2.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroCSR2.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-04-01"), retroCSR2.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-04-01"), retroCSR2.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-04-30"), retroCSR2.getPaymentCoverageEndDate());
		assertEquals("Program Type", "CSR", retroCSR2.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroCSR2.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(70).doubleValue(), retroCSR2.getPaymentAmount().doubleValue());
		assertNotNull("MGP Id", retroCSR2.getMarketplaceGroupPolicyId());

		return true;
	}

	/*
	 * Retroactive Change for a Period in the Past - All Amts
	 */
	@Test
	public void process_RetroChangeMultipleChnagesAllAmtsForPmtMonth_success() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-04-01 00:00:00", "2015-03-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroChangeMultipleAllAmts();
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(6, "101", "2015-04-05", null);

		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertEquals("PolicyPaymentTransactions size", 6, response.getPolicyPaymentTransactions().size());
		assertTrue("Payment Transaction values comparison", compareRetroActiveChangeMultipleAllAmts(response.getPolicyPaymentTransactions()));
	}

	private boolean compareRetroActiveChangeMultipleAllAmts(List<PolicyPaymentTransDTO> paymentTransactions) {

		PolicyPaymentTransDTO retroAPTC = paymentTransactions.get(0);
		assertEquals("Policy version id", 4, retroAPTC.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroAPTC.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroAPTC.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-04-01"), retroAPTC.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-04-01"), retroAPTC.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-04-30"), retroAPTC.getPaymentCoverageEndDate());
		assertEquals("Program Type", "APTC", retroAPTC.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "REPL", retroAPTC.getLastPaymentProcStatusTypeCd());
		assertEquals("Insert/Update", true, retroAPTC.isUpdateStatusRec());
		assertEquals("APTC Amount", new BigDecimal(80).doubleValue(), retroAPTC.getPaymentAmount().doubleValue());

		PolicyPaymentTransDTO retroAPTC2 = paymentTransactions.get(1);
		assertEquals("Policy version id", 6, retroAPTC2.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroAPTC2.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroAPTC2.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-04-01"), retroAPTC2.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-04-01"), retroAPTC2.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-04-30"), retroAPTC2.getPaymentCoverageEndDate());
		assertEquals("Program Type", "APTC", retroAPTC2.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroAPTC2.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(100).doubleValue(), retroAPTC2.getPaymentAmount().doubleValue());
		assertNotNull("MGP Id", retroAPTC2.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO retroCSR = paymentTransactions.get(2);
		assertEquals("Policy version id", 4, retroCSR.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroCSR.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroCSR.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-04-01"), retroCSR.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-04-01"), retroCSR.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-04-30"), retroCSR.getPaymentCoverageEndDate());
		assertEquals("Program Type", "CSR", retroCSR.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "REPL", retroCSR.getLastPaymentProcStatusTypeCd());
		assertEquals("Insert/Update", true, retroCSR.isUpdateStatusRec());
		assertEquals("CSR Amount", new BigDecimal(50).doubleValue(), retroCSR.getPaymentAmount().doubleValue());
		
		PolicyPaymentTransDTO retroCSR2 = paymentTransactions.get(3);
		assertEquals("Policy version id", 6, retroCSR2.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroCSR2.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroCSR2.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-04-01"), retroCSR2.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-04-01"), retroCSR2.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-04-30"), retroCSR2.getPaymentCoverageEndDate());
		assertEquals("Program Type", "CSR", retroCSR2.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroCSR2.getLastPaymentProcStatusTypeCd());
		assertEquals("CSR Amount", new BigDecimal(70).doubleValue(), retroCSR2.getPaymentAmount().doubleValue());
		assertNotNull("MGP Id", retroCSR2.getMarketplaceGroupPolicyId());
		
		PolicyPaymentTransDTO retroUF = paymentTransactions.get(4);
		assertEquals("Policy version id", 4, retroUF.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroUF.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroUF.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-04-01"), retroUF.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-04-01"), retroUF.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-04-30"), retroUF.getPaymentCoverageEndDate());
		assertEquals("Program Type", "UF", retroUF.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "REPL", retroUF.getLastPaymentProcStatusTypeCd());
		assertEquals("Insert/Update", true, retroUF.isUpdateStatusRec());
		assertEquals("UF/TP Amount", new BigDecimal(160).doubleValue(), retroUF.getTotalPremiumAmount().doubleValue());

		PolicyPaymentTransDTO retroUF2 = paymentTransactions.get(5);
		assertEquals("Policy version id", 6, retroUF2.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroUF2.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroUF2.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-04-01"), retroUF2.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-04-01"), retroUF2.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-04-30"), retroUF2.getPaymentCoverageEndDate());
		assertEquals("Program Type", "UF", retroUF2.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroUF2.getLastPaymentProcStatusTypeCd());
		assertEquals("UF/TP Amount", new BigDecimal(200).doubleValue(), retroUF2.getTotalPremiumAmount().doubleValue());
		assertNotNull("MGP Id", retroUF2.getMarketplaceGroupPolicyId());
		
		return true;
	}

	/*
	 * Retroactive Cancellation - Policy Start Date
	 */
	@Test
	public void process_RetroCancel_success() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-01-01 00:00:00", "2014-12-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroCancel("APPV", true);
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(2, "201", "2014-12-25", "2015-01-01");
		//Status
		policyVersion.setPolicyStatus(PolicyStatus.CANCELLED_3.getValue());
		
		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertEquals("PolicyPaymentTransactions size", 3, response.getPolicyPaymentTransactions().size());
		assertTrue("Payment Transaction values comparison", compareRetroActiveCancelResult(response.getPolicyPaymentTransactions(), true));
	}

	/*
	 * Retroactive Cancellation - Policy Start Date
	 */
	@Test
	public void process_RetroCancel_Noise_success() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-01-01 00:00:00", "2014-12-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroCancel("NOISE", true);
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(2, "201", "2014-12-25", "2015-01-01");
		//Status
		policyVersion.setPolicyStatus(PolicyStatus.CANCELLED_3.getValue());
		
		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertEquals("PolicyPaymentTransactions size", 3, response.getPolicyPaymentTransactions().size());
		assertTrue("Payment Transaction values comparison", compareRetroActiveCancelResult(response.getPolicyPaymentTransactions(), true));
	}

	private boolean compareRetroActiveCancelResult(List<PolicyPaymentTransDTO> paymentTransactions, boolean isFfm) {

		PolicyPaymentTransDTO retroAPTC = paymentTransactions.get(0);
		assertEquals("Policy version id", 2, retroAPTC.getPolicyVersionId().longValue());
		assertEquals("Policy id", "201", retroAPTC.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroAPTC.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), retroAPTC.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-01-01"), retroAPTC.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-01-31"), retroAPTC.getPaymentCoverageEndDate());
		assertEquals("Program Type", "APTC", retroAPTC.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroAPTC.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(-50).doubleValue(), retroAPTC.getPaymentAmount().doubleValue());
		assertEquals("Reversal Ref Trans id", 1, retroAPTC.getParentPolicyPaymentTransId().longValue());
		assertNotNull("MGP Id", retroAPTC.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO retroCSR = paymentTransactions.get(1);
		assertEquals("Policy version id", 2, retroCSR.getPolicyVersionId().longValue());
		assertEquals("Policy id", "201", retroCSR.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroCSR.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), retroCSR.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-01-01"), retroCSR.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-01-31"), retroCSR.getPaymentCoverageEndDate());
		assertEquals("Program Type", "CSR", retroCSR.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroCSR.getLastPaymentProcStatusTypeCd());
		assertEquals("CSR Amount", new BigDecimal(-25).doubleValue(), retroCSR.getPaymentAmount().doubleValue());
		assertEquals("Reversal Ref Trans id", 2, retroCSR.getParentPolicyPaymentTransId().longValue());
		assertNotNull("MGP Id", retroCSR.getMarketplaceGroupPolicyId());

		if(isFfm) {
			PolicyPaymentTransDTO retroUFPaymentTransDTO = paymentTransactions.get(2);
			assertEquals("Policy version id", 2, retroUFPaymentTransDTO.getPolicyVersionId().longValue());
			assertEquals("Policy id", "201", retroUFPaymentTransDTO.getExchangePolicyId());
			assertEquals("Trans Type", "R", retroUFPaymentTransDTO.getTransPeriodTypeCd());
			assertEquals("Coverage Dt", new DateTime("2015-01-01"), retroUFPaymentTransDTO.getCoverageDate());
			assertEquals("Coverage Start Dt", new DateTime("2015-01-01"), retroUFPaymentTransDTO.getPaymentCoverageStartDate());
			assertEquals("Coverage End Dt", new DateTime("2015-01-31"), retroUFPaymentTransDTO.getPaymentCoverageEndDate());
			assertEquals("Program Type", "UF", retroUFPaymentTransDTO.getFinancialProgramTypeCd());
			assertEquals("Trans Type", "PCYC", retroUFPaymentTransDTO.getLastPaymentProcStatusTypeCd());
			assertEquals("TP Amount", new BigDecimal(100).doubleValue(), retroUFPaymentTransDTO.getTotalPremiumAmount().doubleValue());
			assertEquals("UF Amount", new BigDecimal(-100).doubleValue(), retroUFPaymentTransDTO.getPaymentAmount().doubleValue());
			assertEquals("Reversal Ref Trans id", 3, retroUFPaymentTransDTO.getParentPolicyPaymentTransId().longValue());
			assertNotNull("MGP Id", retroUFPaymentTransDTO.getMarketplaceGroupPolicyId());
		}
		
		return true;
	}

	/*
	 * Retroactive Cancellation - Policy Start Date
	 */
	@Test
	public void process_RetroCancel_LastDay() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-01-01 00:00:00", "2014-12-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroCancel_Lastday();
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(2, "201", "2014-12-25", "2015-01-31", "2015-01-31");
		policyVersion.setPolicyStatus(PolicyStatus.CANCELLED_3.getValue());
		
		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertEquals("PolicyPaymentTransactions size", 3, response.getPolicyPaymentTransactions().size());
		assertTrue("Payment Transaction values comparison", compareRetroActiveCancelResult_LastDay(response.getPolicyPaymentTransactions()));
	}

	private boolean compareRetroActiveCancelResult_LastDay(List<PolicyPaymentTransDTO> paymentTransactions) {

		PolicyPaymentTransDTO retroAPTC = paymentTransactions.get(0);
		assertEquals("Policy version id", 2, retroAPTC.getPolicyVersionId().longValue());
		assertEquals("Policy id", "201", retroAPTC.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroAPTC.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), retroAPTC.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-01-01"), retroAPTC.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-01-31"), retroAPTC.getPaymentCoverageEndDate());
		assertEquals("Program Type", "APTC", retroAPTC.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroAPTC.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(-50).doubleValue(), retroAPTC.getPaymentAmount().doubleValue());
		assertEquals("Reversal Ref Trans id", 1, retroAPTC.getParentPolicyPaymentTransId().longValue());
		assertNotNull("MGP Id", retroAPTC.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO retroCSR = paymentTransactions.get(1);
		assertEquals("Policy version id", 2, retroCSR.getPolicyVersionId().longValue());
		assertEquals("Policy id", "201", retroCSR.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroCSR.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), retroCSR.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-01-01"), retroCSR.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-01-31"), retroCSR.getPaymentCoverageEndDate());
		assertEquals("Program Type", "CSR", retroCSR.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroCSR.getLastPaymentProcStatusTypeCd());
		assertEquals("CSR Amount", new BigDecimal(-25).doubleValue(), retroCSR.getPaymentAmount().doubleValue());
		assertEquals("Reversal Ref Trans id", 2, retroCSR.getParentPolicyPaymentTransId().longValue());
		assertNotNull("MGP Id", retroCSR.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO retroUFPaymentTransDTO = paymentTransactions.get(2);
		assertEquals("Policy version id", 2, retroUFPaymentTransDTO.getPolicyVersionId().longValue());
		assertEquals("Policy id", "201", retroUFPaymentTransDTO.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroUFPaymentTransDTO.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), retroUFPaymentTransDTO.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-01-01"), retroUFPaymentTransDTO.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-01-31"), retroUFPaymentTransDTO.getPaymentCoverageEndDate());
		assertEquals("Program Type", "UF", retroUFPaymentTransDTO.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroUFPaymentTransDTO.getLastPaymentProcStatusTypeCd());
		assertEquals("TP Amount", new BigDecimal(100).doubleValue(), retroUFPaymentTransDTO.getTotalPremiumAmount().doubleValue());
		assertEquals("UF Amount", new BigDecimal(-100).doubleValue(), retroUFPaymentTransDTO.getPaymentAmount().doubleValue());
		assertEquals("Reversal Ref Trans id", 3, retroUFPaymentTransDTO.getParentPolicyPaymentTransId().longValue());
		assertNotNull("MGP Id", retroUFPaymentTransDTO.getMarketplaceGroupPolicyId());
		
		return true;
	}

	/*
	 * Retroactive Change
	 */
	@Test
	public void process_RetroActiveMidMonthChange_success() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-02-01 00:00:00", "2015-01-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroMidMonthChange(true);
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(2, "301", "2015-01-28", null);

		RapServiceTestUtil.loadStateConfigMap(policyVersion.getSubscriberStateCd(),
				policyVersion.getPolicyStartDate().getYear(), ProrationType.FFM_PRORATING);
		
		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertEquals("PolicyPaymentTransactions size", 13, response.getPolicyPaymentTransactions().size());
		assertTrue("Payment Transaction values comparison", compareRetroActiveMidMonthChangeResult(response.getPolicyPaymentTransactions()));
	}

	private boolean compareRetroActiveMidMonthChangeResult(List<PolicyPaymentTransDTO> paymentTransactions) {

		PolicyPaymentTransDTO retroAPTCJan = paymentTransactions.get(0);
		assertEquals("Policy version id", 2, retroAPTCJan.getPolicyVersionId().longValue());
		assertEquals("Policy id", "301", retroAPTCJan.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroAPTCJan.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), retroAPTCJan.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-01-01"), retroAPTCJan.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-01-31"), retroAPTCJan.getPaymentCoverageEndDate());
		assertEquals("Program Type", "APTC", retroAPTCJan.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroAPTCJan.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(-50).doubleValue(), retroAPTCJan.getPaymentAmount().doubleValue());
		assertEquals("Reversal Ref Trans id", 1, retroAPTCJan.getParentPolicyPaymentTransId().longValue());	
		assertNotNull("MGP Id", retroAPTCJan.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO retroAPTCJan1 = paymentTransactions.get(1);
		assertEquals("Policy version id", 2, retroAPTCJan1.getPolicyVersionId().longValue());
		assertEquals("Policy id", "301", retroAPTCJan1.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroAPTCJan1.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), retroAPTCJan1.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-01-01"), retroAPTCJan1.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-01-15"), retroAPTCJan1.getPaymentCoverageEndDate());		
		assertEquals("Program Type", "APTC", retroAPTCJan1.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroAPTCJan1.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(24.19).doubleValue(), retroAPTCJan1.getPaymentAmount().doubleValue());
		assertNotNull("MGP Id", retroAPTCJan1.getMarketplaceGroupPolicyId());
		
		PolicyPaymentTransDTO retroAPTCJan2 = paymentTransactions.get(2);
		assertEquals("Policy version id", 2, retroAPTCJan2.getPolicyVersionId().longValue());
		assertEquals("Policy id", "301", retroAPTCJan2.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroAPTCJan2.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), retroAPTCJan2.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-01-16"), retroAPTCJan2.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-01-31"), retroAPTCJan2.getPaymentCoverageEndDate());
		assertEquals("Program Type", "APTC", retroAPTCJan2.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroAPTCJan2.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(51.61).doubleValue(), retroAPTCJan2.getPaymentAmount().doubleValue());
		assertNotNull("MGP Id", retroAPTCJan2.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO retroCSRJan = paymentTransactions.get(3);
		assertEquals("Policy version id", 2, retroCSRJan.getPolicyVersionId().longValue());
		assertEquals("Policy id", "301", retroCSRJan.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroCSRJan.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), retroCSRJan.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-01-01"), retroCSRJan.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-01-31"), retroCSRJan.getPaymentCoverageEndDate());
		assertEquals("Program Type", "CSR", retroCSRJan.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroCSRJan.getLastPaymentProcStatusTypeCd());
		assertEquals("CSR Amount", new BigDecimal(-25).doubleValue(), retroCSRJan.getPaymentAmount().doubleValue());
		assertEquals("Reversal Ref Trans id", 2, retroCSRJan.getParentPolicyPaymentTransId().longValue());
		assertNotNull("MGP Id", retroCSRJan.getMarketplaceGroupPolicyId());
		
		PolicyPaymentTransDTO retroCSRJan1 = paymentTransactions.get(4);
		assertEquals("Policy version id", 2, retroCSRJan1.getPolicyVersionId().longValue());
		assertEquals("Policy id", "301", retroCSRJan1.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroCSRJan1.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), retroCSRJan1.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-01-01"), retroCSRJan1.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-01-15"), retroCSRJan1.getPaymentCoverageEndDate());		
		assertEquals("Program Type", "CSR", retroCSRJan1.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroCSRJan1.getLastPaymentProcStatusTypeCd());
		assertEquals("CSR Amount", new BigDecimal(12.10).doubleValue(), retroCSRJan1.getPaymentAmount().doubleValue());
		assertNotNull("MGP Id", retroCSRJan1.getMarketplaceGroupPolicyId());
		
		PolicyPaymentTransDTO retroCSRJan2 = paymentTransactions.get(5);
		assertEquals("Policy version id", 2, retroCSRJan2.getPolicyVersionId().longValue());
		assertEquals("Policy id", "301", retroCSRJan2.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroCSRJan2.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), retroCSRJan2.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-01-16"), retroCSRJan2.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-01-31"), retroCSRJan2.getPaymentCoverageEndDate());
		assertEquals("Program Type", "CSR", retroCSRJan2.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroCSRJan2.getLastPaymentProcStatusTypeCd());
		assertEquals("CSR Amount", new BigDecimal(25.81).doubleValue(), retroCSRJan2.getPaymentAmount().doubleValue());
		assertNotNull("MGP Id", retroCSRJan2.getMarketplaceGroupPolicyId());
		
		PolicyPaymentTransDTO retroUfJan = paymentTransactions.get(6);
		assertEquals("Policy version id", 2, retroUfJan.getPolicyVersionId().longValue());
		assertEquals("Policy id", "301", retroUfJan.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroUfJan.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), retroUfJan.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-01-01"), retroUfJan.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-01-31"), retroUfJan.getPaymentCoverageEndDate());
		assertEquals("Program Type", "UF", retroUfJan.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroUfJan.getLastPaymentProcStatusTypeCd());
		assertEquals("TP Amount", new BigDecimal(100).doubleValue(), retroUfJan.getTotalPremiumAmount().doubleValue());
		assertEquals("UF Amount", new BigDecimal(-100).doubleValue(), retroUfJan.getPaymentAmount().doubleValue());
		assertEquals("Reversal Ref Trans id", 3, retroUfJan.getParentPolicyPaymentTransId().longValue());
		assertNotNull("MGP Id", retroUfJan.getMarketplaceGroupPolicyId());
		
		PolicyPaymentTransDTO retroUfJan1 = paymentTransactions.get(7);
		assertEquals("Policy version id", 2, retroUfJan1.getPolicyVersionId().longValue());
		assertEquals("Policy id", "301", retroUfJan1.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroUfJan1.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), retroUfJan1.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-01-01"), retroUfJan1.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-01-15"), retroUfJan1.getPaymentCoverageEndDate());
		assertEquals("Program Type", "UF", retroUfJan1.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroUfJan1.getLastPaymentProcStatusTypeCd());
		assertEquals("UF Amount", new BigDecimal(48.39).doubleValue(), retroUfJan1.getTotalPremiumAmount().doubleValue());
		assertNotNull("MGP Id", retroUfJan1.getMarketplaceGroupPolicyId());
		
		PolicyPaymentTransDTO retroUfJan2 = paymentTransactions.get(8);
		assertEquals("Policy version id", 2, retroUfJan2.getPolicyVersionId().longValue());
		assertEquals("Policy id", "301", retroUfJan2.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroUfJan2.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), retroUfJan2.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-01-16"), retroUfJan2.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-01-31"), retroUfJan2.getPaymentCoverageEndDate());
		assertEquals("Program Type", "UF", retroUfJan2.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroUfJan2.getLastPaymentProcStatusTypeCd());
		assertEquals("UF Amount", new BigDecimal(51.61).doubleValue(), retroUfJan2.getTotalPremiumAmount().doubleValue());
		assertNotNull("MGP Id", retroUfJan2.getMarketplaceGroupPolicyId());
		
		PolicyPaymentTransDTO retroAPTCFeb = paymentTransactions.get(9);
		assertEquals("Policy version id", 2, retroAPTCFeb.getPolicyVersionId().longValue());
		assertEquals("Policy id", "301", retroAPTCFeb.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroAPTCFeb.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), retroAPTCFeb.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-02-01"), retroAPTCFeb.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-02-28"), retroAPTCFeb.getPaymentCoverageEndDate());		
		assertEquals("Program Type", "APTC", retroAPTCFeb.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroAPTCFeb.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(-50).doubleValue(), retroAPTCFeb.getPaymentAmount().doubleValue());
		assertEquals("Reversal Ref Trans id", 4, retroAPTCFeb.getParentPolicyPaymentTransId().longValue());
		assertNotNull("MGP Id", retroAPTCFeb.getMarketplaceGroupPolicyId());
		
		PolicyPaymentTransDTO retroAPTCFeb1 = paymentTransactions.get(10);
		assertEquals("Policy version id", 2, retroAPTCFeb1.getPolicyVersionId().longValue());
		assertEquals("Policy id", "301", retroAPTCFeb1.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroAPTCFeb1.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), retroAPTCFeb1.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-02-01"), retroAPTCFeb1.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-02-28"), retroAPTCFeb1.getPaymentCoverageEndDate());
		assertEquals("Program Type", "APTC", retroAPTCFeb1.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroAPTCFeb1.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(100).doubleValue(), retroAPTCFeb1.getPaymentAmount().doubleValue());
		assertNotNull("MGP Id", retroAPTCFeb1.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO retroCSRFeb = paymentTransactions.get(11);
		assertEquals("Policy version id", 2, retroCSRFeb.getPolicyVersionId().longValue());
		assertEquals("Policy id", "301", retroCSRFeb.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroCSRFeb.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), retroCSRFeb.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-02-01"), retroCSRFeb.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-02-28"), retroCSRFeb.getPaymentCoverageEndDate());
		assertEquals("Program Type", "CSR", retroCSRFeb.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroCSRFeb.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(-25).doubleValue(), retroCSRFeb.getPaymentAmount().doubleValue());
		assertEquals("Reversal Ref Trans id", 5, retroCSRFeb.getParentPolicyPaymentTransId().longValue());
		assertNotNull("MGP Id", retroCSRFeb.getMarketplaceGroupPolicyId());
		
		PolicyPaymentTransDTO retroCSRFeb1 = paymentTransactions.get(12);
		assertEquals("Policy version id", 2, retroCSRFeb1.getPolicyVersionId().longValue());
		assertEquals("Policy id", "301", retroCSRFeb1.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroCSRFeb1.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), retroCSRFeb1.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-02-01"), retroCSRFeb1.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-02-28"), retroCSRFeb1.getPaymentCoverageEndDate());
		assertEquals("Program Type", "CSR", retroCSRFeb1.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroCSRFeb1.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(50).doubleValue(), retroCSRFeb1.getPaymentAmount().doubleValue());
		assertNotNull("MGP Id", retroCSRFeb1.getMarketplaceGroupPolicyId());

		return true;
	}

	/*
	 * Retroactive Change in Circumstance – Birth. 
	 * The termination part of this scenario is covered in the Term related test case above.
	 * This test case covers the newly effectuated policy created as a result of the CIC.
	 */
	@Test
	public void process_RetroActiveChangeInCircums_Birth_success() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-03-01 00:00:00", "2015-02-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroChangeInCircums();
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "401", "2015-02-24", null);
		policyVersion.setPolicyStartDate(new DateTime("2015-02-22"));
		
		RapServiceTestUtil.loadStateConfigMap(policyVersion.getSubscriberStateCd(),
				policyVersion.getPolicyStartDate().getYear(), ProrationType.FFM_PRORATING);

		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertEquals("PolicyPaymentTransactions size", 6, response.getPolicyPaymentTransactions().size());
		assertTrue("Payment Transaction values comparison", compareRetroActiveRetroActiveChangeInCircumsResult(response.getPolicyPaymentTransactions()));
	}

	private boolean compareRetroActiveRetroActiveChangeInCircumsResult(List<PolicyPaymentTransDTO> paymentTransactions) {

		PolicyPaymentTransDTO retroAPTC = paymentTransactions.get(0);
		assertEquals("Policy version id", 1, retroAPTC.getPolicyVersionId().longValue());
		assertEquals("Policy id", "401", retroAPTC.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroAPTC.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), retroAPTC.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-02-22"), retroAPTC.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-02-28"), retroAPTC.getPaymentCoverageEndDate());
		assertEquals("Program Type", "APTC", retroAPTC.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroAPTC.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(37.5).doubleValue(), retroAPTC.getPaymentAmount().doubleValue());
		assertNotNull("MGP Id", retroAPTC.getMarketplaceGroupPolicyId());
		
		PolicyPaymentTransDTO retroCSR = paymentTransactions.get(1);
		assertEquals("Policy version id", 1, retroCSR.getPolicyVersionId().longValue());
		assertEquals("Policy id", "401", retroCSR.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroCSR.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), retroCSR.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-02-22"), retroCSR.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-02-28"), retroCSR.getPaymentCoverageEndDate());
		assertEquals("Program Type", "CSR", retroCSR.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroCSR.getLastPaymentProcStatusTypeCd());
		assertEquals("CSR Amount", new BigDecimal(18.75).doubleValue(), retroCSR.getPaymentAmount().doubleValue());
		assertNotNull("MGP Id", retroCSR.getMarketplaceGroupPolicyId());
		
		PolicyPaymentTransDTO retroUF = paymentTransactions.get(2);
		assertEquals("Policy version id", 1, retroUF.getPolicyVersionId().longValue());
		assertEquals("Policy id", "401", retroUF.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroUF.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), retroUF.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-02-22"), retroUF.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-02-28"), retroUF.getPaymentCoverageEndDate());
		assertEquals("Program Type", "UF", retroUF.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroUF.getLastPaymentProcStatusTypeCd());
		assertEquals("UF Amount", new BigDecimal(50.00).doubleValue(), retroUF.getTotalPremiumAmount().doubleValue());
		assertNotNull("MGP Id", retroUF.getMarketplaceGroupPolicyId());

		
		PolicyPaymentTransDTO retroAPTCMar = paymentTransactions.get(3);
		assertEquals("Policy version id", 1, retroAPTCMar.getPolicyVersionId().longValue());
		assertEquals("Policy id", "401", retroAPTCMar.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroAPTCMar.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-03-01"), retroAPTCMar.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-03-01"), retroAPTCMar.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-03-31"), retroAPTCMar.getPaymentCoverageEndDate());
		assertEquals("Program Type", "APTC", retroAPTCMar.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroAPTCMar.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(150).doubleValue(), retroAPTCMar.getPaymentAmount().doubleValue());
		assertNotNull("MGP Id", retroAPTCMar.getMarketplaceGroupPolicyId());
		
		PolicyPaymentTransDTO retroCSRMar = paymentTransactions.get(4);
		assertEquals("Policy version id", 1, retroCSRMar.getPolicyVersionId().longValue());
		assertEquals("Policy id", "401", retroCSRMar.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroCSRMar.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-03-01"), retroCSRMar.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-03-01"), retroCSRMar.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-03-31"), retroCSRMar.getPaymentCoverageEndDate());
		assertEquals("Program Type", "CSR", retroCSRMar.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroCSRMar.getLastPaymentProcStatusTypeCd());
		assertEquals("CSR Amount", new BigDecimal(75).doubleValue(), retroCSRMar.getPaymentAmount().doubleValue());
		assertNotNull("MGP Id", retroCSRMar.getMarketplaceGroupPolicyId());
		
		PolicyPaymentTransDTO retroUFMar = paymentTransactions.get(5);
		assertEquals("Policy version id", 1, retroUFMar.getPolicyVersionId().longValue());
		assertEquals("Policy id", "401", retroUFMar.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroUFMar.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-03-01"), retroUFMar.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-03-01"), retroUFMar.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-03-31"), retroUFMar.getPaymentCoverageEndDate());
		assertEquals("Program Type", "UF", retroUFMar.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroUFMar.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(200).doubleValue(), retroUFMar.getTotalPremiumAmount().doubleValue());
		assertNotNull("MGP Id", retroUFMar.getMarketplaceGroupPolicyId());
		
		return true;
	}

	/*
	 * Retroactive Change in Policy Start Date to End of a month but Start Date != End Date
	 */
	@Test
	public void process_Retro_PolicyStartDateChange() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-11-01 00:00:00", "2015-10-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForPolicyStartDateChange(true);
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(2, "201", "2015-10-15", "2015-06-30", "2015-10-31");

		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertEquals("PolicyPaymentTransactions size", 6, response.getPolicyPaymentTransactions().size());
		assertTrue("Payment Transaction values comparison", compareRetroActivePolicyStartDateChangeResult(response.getPolicyPaymentTransactions(), true));
	}

	private boolean compareRetroActivePolicyStartDateChangeResult(List<PolicyPaymentTransDTO> paymentTransactions, boolean isFfm) {

		PolicyPaymentTransDTO retroAPTC = paymentTransactions.get(0);
		assertEquals("Policy version id", 2, retroAPTC.getPolicyVersionId().longValue());
		assertEquals("Policy id", "201", retroAPTC.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroAPTC.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-06-01"), retroAPTC.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-06-01"), retroAPTC.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-06-30"), retroAPTC.getPaymentCoverageEndDate());
		assertEquals("Program Type", "APTC", retroAPTC.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroAPTC.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(-88.61).doubleValue(), retroAPTC.getPaymentAmount().doubleValue());
		assertEquals("Reversal Ref Trans id", 1, retroAPTC.getParentPolicyPaymentTransId().longValue());
		assertNotNull("MGP Id", retroAPTC.getMarketplaceGroupPolicyId());
		
		PolicyPaymentTransDTO retroAPTCNew = paymentTransactions.get(1);
		assertEquals("Policy version id", 2, retroAPTCNew.getPolicyVersionId().longValue());
		assertEquals("Policy id", "201", retroAPTCNew.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroAPTCNew.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-06-01"), retroAPTCNew.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-06-30"), retroAPTCNew.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-06-30"), retroAPTCNew.getPaymentCoverageEndDate());
		assertEquals("Program Type", "APTC", retroAPTCNew.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroAPTCNew.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(88.61).doubleValue(), retroAPTCNew.getPaymentAmount().doubleValue());
		assertNull("Reversal Ref Trans id", retroAPTCNew.getParentPolicyPaymentTransId());
		assertNotNull("MGP Id", retroAPTCNew.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO retroCSR = paymentTransactions.get(2);
		assertEquals("Policy version id", 2, retroCSR.getPolicyVersionId().longValue());
		assertEquals("Policy id", "201", retroCSR.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroCSR.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-06-01"), retroCSR.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-06-01"), retroCSR.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-06-30"), retroCSR.getPaymentCoverageEndDate());
		assertEquals("Program Type", "CSR", retroCSR.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroCSR.getLastPaymentProcStatusTypeCd());
		assertEquals("CSR Amount", new BigDecimal(-41.58).doubleValue(), retroCSR.getPaymentAmount().doubleValue());
		assertEquals("Reversal Ref Trans id", 2, retroCSR.getParentPolicyPaymentTransId().longValue());
		assertNotNull("MGP Id", retroCSR.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO retroCSRNew = paymentTransactions.get(3);
		assertEquals("Policy version id", 2, retroCSRNew.getPolicyVersionId().longValue());
		assertEquals("Policy id", "201", retroCSRNew.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroCSRNew.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-06-01"), retroCSRNew.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-06-30"), retroCSRNew.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-06-30"), retroCSRNew.getPaymentCoverageEndDate());
		assertEquals("Program Type", "CSR", retroCSRNew.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroCSRNew.getLastPaymentProcStatusTypeCd());
		assertEquals("CSR Amount", new BigDecimal(41.58).doubleValue(), retroCSRNew.getPaymentAmount().doubleValue());
		assertNull("Reversal Ref Trans id", retroCSRNew.getParentPolicyPaymentTransId());
		assertNotNull("MGP Id", retroCSRNew.getMarketplaceGroupPolicyId());

		if(isFfm) {
			PolicyPaymentTransDTO retroUFPaymentTransDTO = paymentTransactions.get(4);
			assertEquals("Policy version id", 2, retroUFPaymentTransDTO.getPolicyVersionId().longValue());
			assertEquals("Policy id", "201", retroUFPaymentTransDTO.getExchangePolicyId());
			assertEquals("Trans Type", "R", retroUFPaymentTransDTO.getTransPeriodTypeCd());
			assertEquals("Coverage Dt", new DateTime("2015-06-01"), retroUFPaymentTransDTO.getCoverageDate());
			assertEquals("Coverage Start Dt", new DateTime("2015-06-01"), retroUFPaymentTransDTO.getPaymentCoverageStartDate());
			assertEquals("Coverage End Dt", new DateTime("2015-06-30"), retroUFPaymentTransDTO.getPaymentCoverageEndDate());
			assertEquals("Program Type", "UF", retroUFPaymentTransDTO.getFinancialProgramTypeCd());
			assertEquals("Trans Type", "PCYC", retroUFPaymentTransDTO.getLastPaymentProcStatusTypeCd());
			assertEquals("TP Amount", new BigDecimal(189.01).doubleValue(), retroUFPaymentTransDTO.getTotalPremiumAmount().doubleValue());
			assertEquals("UF Amount", new BigDecimal(-189.01).doubleValue(), retroUFPaymentTransDTO.getPaymentAmount().doubleValue());
			assertEquals("Reversal Ref Trans id", 3, retroUFPaymentTransDTO.getParentPolicyPaymentTransId().longValue());
			assertNotNull("MGP Id", retroUFPaymentTransDTO.getMarketplaceGroupPolicyId());
			
			PolicyPaymentTransDTO retroUFNew = paymentTransactions.get(5);
			assertEquals("Policy version id", 2, retroUFNew.getPolicyVersionId().longValue());
			assertEquals("Policy id", "201", retroUFNew.getExchangePolicyId());
			assertEquals("Trans Type", "R", retroUFNew.getTransPeriodTypeCd());
			assertEquals("Coverage Dt", new DateTime("2015-06-01"), retroUFNew.getCoverageDate());
			assertEquals("Coverage Start Dt", new DateTime("2015-06-30"), retroUFNew.getPaymentCoverageStartDate());
			assertEquals("Coverage End Dt", new DateTime("2015-06-30"), retroUFNew.getPaymentCoverageEndDate());
			assertEquals("Program Type", "UF", retroUFNew.getFinancialProgramTypeCd());
			assertEquals("Trans Type", "PCYC", retroUFNew.getLastPaymentProcStatusTypeCd());
			assertEquals("TP Amount", new BigDecimal(189.01).doubleValue(), retroUFNew.getTotalPremiumAmount().doubleValue());
			assertNull("UF Amount", retroUFNew.getPaymentAmount());
			assertNull("Reversal Ref Trans id", retroUFNew.getParentPolicyPaymentTransId());
			assertNotNull("MGP Id", retroUFNew.getMarketplaceGroupPolicyId());
		}

		return true;
	}

	/*
	 * 
	 */
	@Test//(expected=com.accenture.foundation.common.exception.ApplicationException.class)
	public void test_getPaymentMonthERC_AppException() throws Exception {

		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(null);
		replay(mockCodeDecodesHelper);

		try {
			ReflectionTestUtils.invokeMethod(rapProcesssingServiceTest, "getPaymentMonthERC");
		} catch(ApplicationException appEx) {
			assertTrue("ApplicationException thrown", true);
		}
		assertNotNull("rapProcesssingServiceTest", rapProcesssingServiceTest);
	}

	/*
	 * 
	 */
	@Test
	public void test_getRetroMonthEndDate() throws Exception {

		DateTime FEB_1 = new DateTime(YEAR, 2, 1, 0, 0);
		DateTime MAR_1 = new DateTime(YEAR, 3, 1, 0, 0);

		DateTime result = (DateTime)ReflectionTestUtils.invokeMethod(
				rapProcesssingServiceTest, "getRetroMonthEndDate", FEB_1);
		assertNotNull("result", result);
		assertEquals("result value is Feb 28/29", MAR_1.minusDays(1), result);
	}

	/*
	 * 
	 */
	/*	//@Test
	public void test_isUserFeeRateExists_False() throws Exception {
		DateTime JAN_1 = new DateTime(YEAR, 1, 1, 0, 0);

		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(Date.class), EasyMock.anyString()))
			.andReturn(new ArrayList<IssuerUserFeeRate>());
		replay(mockRapDao);

		PolicyDataDTO policy = RapServiceTestUtil.createMockPolicyVersion(
				1L, "101", new Date(System.currentTimeMillis()).toString(), "2015-03-01");

		Boolean result = (Boolean)ReflectionTestUtils.invokeMethod(
				rapProcesssingServiceTest, "isUserFeeRateExists", JAN_1, policy);
		assertNotNull("result", result);
		assertEquals("result value is false", false, result.booleanValue());
	}*/

	/*
	 * 
	 */
	@Test
	public void test_getRetroMonthEndDate_NullInput() throws Exception {

		DateTime nullDt = null;

		DateTime result = (DateTime)ReflectionTestUtils.invokeMethod(
				rapProcesssingServiceTest, "getRetroMonthEndDate", nullDt);
		assertNull("result", result);
	}

	/*
	 * 
	 */
	@Test
	public void test_createPolicyPaymentTransactions() throws Exception {

		DateTime JAN_1 = new DateTime(YEAR, 1, 1, 0, 0);
		DateTime JAN_16 = new DateTime(YEAR, 1, 16, 0, 0);
		DateTime JAN_31 = new DateTime(YEAR, 1, 31, 0, 0);

		DateTime coverageDt = JAN_1;
		String programTypCd = "APTC";
		String policyId = "POLICYID";
		BigDecimal amt = new BigDecimal(100.00);

		PolicyPremium premium = new PolicyPremium();
		premium.setEffectiveStartDate(JAN_16);

		PolicyDataDTO policy = RapServiceTestUtil.createMockPolicyVersion(
				1L, policyId, new Date(System.currentTimeMillis()).toString(), "2015-03-01");
		policy.setPolicyStartDate(JAN_16);

		PolicyPaymentTransDTO result = (PolicyPaymentTransDTO)ReflectionTestUtils.invokeMethod(
				rapProcesssingServiceTest, "createPolicyPaymentTrans", 
				coverageDt, programTypCd, policy, premium, amt, null, ProrationType.NON_PRORATING);

		assertNotNull("result", result);
		assertEquals("coverageDt value is Jan1", JAN_1, result.getCoverageDate());
		assertEquals("coverage Start Dt value is Jan1", JAN_16, result.getPaymentCoverageStartDate());
		assertEquals("coverage End Dt value is Jan31", JAN_31, result.getPaymentCoverageEndDate());
		assertEquals("TransPeriodTypeCd value is R", "R", result.getTransPeriodTypeCd());
		assertEquals("PmtStatusCd value is PCYC", "PCYC", result.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", result.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", result.getMarketplaceGroupPolicyId());
	}

	/*
	 * 
	 */
	@Test
	public void test_createPolicyPaymentTransactions_ProratedAmt() throws Exception {

		replay(mockCodeDecodesHelper);

		DateTime JAN_1 = new DateTime(YEAR, 1, 1, 0, 0);
		DateTime JAN_16 = new DateTime(YEAR, 1, 16, 0, 0);
		DateTime JAN_31 = new DateTime(YEAR, 1, 31, 0, 0);

		DateTime coverageDt = JAN_1;
		String programTypCd = "APTC";
		String policyId = "POLICYID";
		BigDecimal amt = new BigDecimal(100.00);

		PolicyPremium premium = new PolicyPremium();
		premium.setTotalPremiumAmount(new BigDecimal(200.00));
		premium.setEffectiveStartDate(JAN_16);

		PolicyDataDTO policy = RapServiceTestUtil.createMockPolicyVersion(
				1L, policyId, new Date(System.currentTimeMillis()).toString(), "2015-03-01");
		policy.setPolicyStartDate(JAN_16);
		
		RapServiceTestUtil.loadStateConfigMap(policy.getSubscriberStateCd(),
				policy.getPolicyStartDate().getYear(), ProrationType.FFM_PRORATING);

		PolicyPaymentTransDTO result = (PolicyPaymentTransDTO)ReflectionTestUtils.invokeMethod(
				rapProcesssingServiceTest, "createPolicyPaymentTrans", 
				coverageDt, programTypCd, policy, premium, amt, new BigDecimal(51.61), ProrationType.FFM_PRORATING);

		assertNotNull("result", result);
		assertEquals("Pmt Amount value is ", 51.61, result.getPaymentAmount().doubleValue());
		assertEquals("coverageDt value is Jan1", JAN_1, result.getCoverageDate());
		assertEquals("coverage Start Dt value is Jan1", JAN_16, result.getPaymentCoverageStartDate());
		assertEquals("coverage End Dt value is Jan31", JAN_31, result.getPaymentCoverageEndDate());
		assertEquals("TransPeriodTypeCd value is R", "R", result.getTransPeriodTypeCd());
		assertEquals("PmtStatusCd value is PCYC", "PCYC", result.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", result.getMarketplaceGroupPolicyId());
	}

	/*
	 * 
	 */
	@Test
	public void test_createPolicyPaymentTransactions_ProratedTpaAndAmt() throws Exception {

		replay(mockCodeDecodesHelper);

		DateTime JAN_1 = new DateTime(YEAR, 1, 1, 0, 0);
		DateTime JAN_16 = new DateTime(YEAR, 1, 16, 0, 0);
		DateTime JAN_31 = new DateTime(YEAR, 1, 31, 0, 0);

		DateTime coverageDt = JAN_1;
		String programTypCd = "APTC";
		String policyId = "POLICYID";
		BigDecimal amt = new BigDecimal(100.00);

		PolicyPremium premium = new PolicyPremium();
		premium.setTotalPremiumAmount(new BigDecimal(200.00));
		premium.setProratedPremiumAmount(new BigDecimal(103.22));
		premium.setEffectiveStartDate(JAN_16);

		PolicyDataDTO policy = RapServiceTestUtil.createMockPolicyVersion(
				1L, policyId, new Date(System.currentTimeMillis()).toString(), "2015-03-01");
		policy.setPolicyStartDate(JAN_16);
		
		RapServiceTestUtil.loadStateConfigMap(policy.getSubscriberStateCd(),
				policy.getPolicyStartDate().getYear(), ProrationType.FFM_PRORATING);

		PolicyPaymentTransDTO result = (PolicyPaymentTransDTO)ReflectionTestUtils.invokeMethod(
				rapProcesssingServiceTest, "createPolicyPaymentTrans", 
				coverageDt, programTypCd, policy, premium, amt, new BigDecimal(51.61), ProrationType.FFM_PRORATING);

		assertNotNull("result", result);
		assertEquals("Pmt Amount value is ", 51.61, result.getPaymentAmount().doubleValue());
		assertEquals("coverageDt value is Jan1", JAN_1, result.getCoverageDate());
		assertEquals("coverage Start Dt value is Jan1", JAN_16, result.getPaymentCoverageStartDate());
		assertEquals("coverage End Dt value is Jan31", JAN_31, result.getPaymentCoverageEndDate());
		assertEquals("TransPeriodTypeCd value is R", "R", result.getTransPeriodTypeCd());
		assertEquals("PmtStatusCd value is PCYC", "PCYC", result.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", result.getMarketplaceGroupPolicyId());
	}

	/*
	 * 
	 */
	@Test
	public void test_createPolicyPaymentTransactions_B() throws Exception {

		DateTime JAN_1 = new DateTime(YEAR, 1, 1, 0, 0);
		DateTime JAN_15 = new DateTime(YEAR, 1, 15, 0, 0);

		DateTime coverageDt = JAN_1;
		String programTypCd = "APTC";
		String policyId = "POLICYID";
		BigDecimal amt = new BigDecimal(100.00);

		PolicyPremium premium = new PolicyPremium();
		premium.setEffectiveStartDate(JAN_1);
		premium.setEffectiveEndDate(JAN_15);

		PolicyDataDTO policy = RapServiceTestUtil.createMockPolicyVersion(
				1L, policyId, new Date(System.currentTimeMillis()).toString(), "2015-03-01");
		policy.setPolicyStartDate(JAN_1);

		PolicyPaymentTransDTO result = (PolicyPaymentTransDTO)ReflectionTestUtils.invokeMethod(
				rapProcesssingServiceTest, "createPolicyPaymentTrans", coverageDt, programTypCd, 
				policy, premium, amt, null, ProrationType.NON_PRORATING);

		assertNotNull("result", result);
		assertEquals("coverageDt value is Jan1", JAN_1, result.getCoverageDate());
		assertEquals("coverage Start Dt value is Jan1", JAN_1, result.getPaymentCoverageStartDate());
		assertEquals("coverage End Dt value is Jan31", JAN_15, result.getPaymentCoverageEndDate());
		assertEquals("TransPeriodTypeCd value is R", "R", result.getTransPeriodTypeCd());
		assertEquals("PmtStatusCd value is PCYC", "PCYC", result.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", result.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", result.getMarketplaceGroupPolicyId());
	}

	/*
	 * 
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void process_determineMinMaxCoverageMonth_PVD_LT_ERC() throws Exception {

		DateTime ercDate = new DateTime(YEAR, 1, 16, 0, 0);
		DateTime maintStartDt = ercDate.minusDays(5);
		List<DateTime> pmtMonths = new ArrayList<DateTime>();
		pmtMonths.add(ercDate.withDayOfMonth(1).plusMonths(1));
		pmtMonths.add(ercDate);

		PolicyDataDTO policyVersion = new PolicyDataDTO();
		policyVersion.setPolicyStartDate(
				new DateTime(YEAR, 1, 1, 0, 0));
		policyVersion.setIssuerStartDate(policyVersion.getPolicyStartDate().withDayOfYear(1));
		policyVersion.setMaintenanceStartDateTime(maintStartDt);

		ReflectionTestUtils.setField(rapProcesssingServiceTest, "currentPmtMonth", pmtMonths);

		List<DateTime> result = (List<DateTime>)ReflectionTestUtils.invokeMethod(
				rapProcesssingServiceTest, "determineMinMaxCoverageMonth", policyVersion);

		assertNotNull("result", result);
		assertEquals("result size", 1, result.size());
		assertEquals("result val", new DateTime(YEAR, 1, 1, 0, 0), result.get(0));
	}

	/*
	 * 
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void process_determineMinMaxCoverageMonth_PVD_GT_ERC() throws Exception {

		DateTime ercDate = new DateTime(YEAR, 1, 16, 0, 0);
		DateTime maintStartDt = ercDate.plusDays(5);
		List<DateTime> pmtMonths = new ArrayList<DateTime>();
		pmtMonths.add(ercDate.withDayOfMonth(1).plusMonths(1));
		pmtMonths.add(ercDate);

		PolicyDataDTO policyVersion = new PolicyDataDTO();
		policyVersion.setPolicyStartDate(
				new DateTime(YEAR, 1, 1, 0, 0));
		policyVersion.setIssuerStartDate(policyVersion.getPolicyStartDate().withDayOfYear(1));
		policyVersion.setMaintenanceStartDateTime(maintStartDt);

		ReflectionTestUtils.setField(rapProcesssingServiceTest, "currentPmtMonth", pmtMonths);

		List<DateTime> result = (List<DateTime>)ReflectionTestUtils.invokeMethod(
				rapProcesssingServiceTest, "determineMinMaxCoverageMonth", policyVersion);

		assertNotNull("result", result);
		assertEquals("result size", 2, result.size());
		assertEquals("result val", new DateTime(YEAR, 1, 1, 0, 0), result.get(0));
		assertEquals("result val", new DateTime(YEAR, 2, 1, 0, 0), result.get(1));
	}

	/*
	 * 
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void process_determineMinMaxCoverageMonth_PVD_EQ_ERC() throws Exception {

		DateTime ercDate = new DateTime(YEAR, 1, 16, 0, 0);
		DateTime maintStartDt = ercDate;
		List<DateTime> pmtMonths = new ArrayList<DateTime>();
		pmtMonths.add(ercDate.withDayOfMonth(1).plusMonths(1));
		pmtMonths.add(ercDate);

		PolicyDataDTO policyVersion = new PolicyDataDTO();
		policyVersion.setPolicyStartDate(
				new DateTime(YEAR, 1, 1, 0, 0));
		policyVersion.setIssuerStartDate(policyVersion.getPolicyStartDate().withDayOfYear(1));
		policyVersion.setMaintenanceStartDateTime(maintStartDt);

		ReflectionTestUtils.setField(rapProcesssingServiceTest, "currentPmtMonth", pmtMonths);

		List<DateTime> result = (List<DateTime>)ReflectionTestUtils.invokeMethod(
				rapProcesssingServiceTest, "determineMinMaxCoverageMonth", policyVersion);

		assertNotNull("result", result);
		assertEquals("result size", 2, result.size());
		assertEquals("result val", new DateTime(YEAR, 1, 1, 0, 0), result.get(0));
		assertEquals("result val", new DateTime(YEAR, 2, 1, 0, 0), result.get(1));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void process_determineMinMaxCoverageMonth_PSDYear_LT_IssuerTransition() throws Exception {

		DateTime ercDate = new DateTime(YEAR, 1, 16, 0, 0);
		DateTime maintStartDt = ercDate.minusDays(5);
		List<DateTime> pmtMonths = new ArrayList<DateTime>();
		pmtMonths.add(ercDate.withDayOfMonth(1).plusMonths(1));
		pmtMonths.add(ercDate);

		PolicyDataDTO policyVersion = new PolicyDataDTO();
		policyVersion.setPolicyStartDate(
				new DateTime(YEAR-1, 1, 1, 0, 0));
		policyVersion.setIssuerStartDate(new DateTime(YEAR, 2, 1, 0, 0));
		policyVersion.setMaintenanceStartDateTime(maintStartDt);

		ReflectionTestUtils.setField(rapProcesssingServiceTest, "currentPmtMonth", pmtMonths);

		List<DateTime> result = (List<DateTime>)ReflectionTestUtils.invokeMethod(
				rapProcesssingServiceTest, "determineMinMaxCoverageMonth", policyVersion);

		assertNotNull("result", result);
		assertEquals("result size", 1, result.size());
		assertEquals("result val", new DateTime(YEAR, 1, 1, 0, 0), result.get(0));
	}


	/*
	 * 
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void process_getPremiumsForCoverageMonths_With_PremiumEndDate() throws Exception {

		DateTime pmtMonthDt = new DateTime(2015, 1, 1, 0, 0);
		List<DateTime> pmtMonths = new ArrayList<DateTime>();
		pmtMonths.add(pmtMonthDt);
		pmtMonths.add(pmtMonthDt.plusMonths(2));

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(2, "201", "2014-12-25", "2015-01-01");

		ReflectionTestUtils.setField(rapProcesssingServiceTest, "minMaxCoverageMonths", pmtMonths);

		List<PolicyPremium> policyPremiums = new ArrayList<PolicyPremium>();
		policyPremiums.add(RapServiceTestUtil.createMockPolicyPremium(1, "2015-01-15", "2015-01-31", new BigDecimal(50), new BigDecimal(25), new BigDecimal(100)));
		policyPremiums.add(RapServiceTestUtil.createMockPolicyPremium(1, "2015-02-01", "2015-02-15", new BigDecimal(50), new BigDecimal(25), new BigDecimal(100)));
		policyPremiums.add(RapServiceTestUtil.createMockPolicyPremium(1, "2015-02-16", null, new BigDecimal(50), new BigDecimal(25), new BigDecimal(100)));

		Map<DateTime, PolicyPremium> result = (Map<DateTime, PolicyPremium>)ReflectionTestUtils.invokeMethod(
				rapProcesssingServiceTest, "getPremiumsForCoverageMonths", policyPremiums, policyVersion);

		assertNotNull("result", result);
		assertEquals("result val", 3, result.size());
	}

	// ------------------- PP5 Changes - Coverage Start and End Dates ----------------------//

	/*
	 * Retroactive Enrollment - first day of Payment Coverage Month < Premium Eff Date
	 */
	@Test
	public void process_RetroActiveEnrollment_CoverageMonthStartDtLTPremiumEffStartDt() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-05-01 00:00:00", "2014-04-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroEnrollment("2015-03-16");
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-04-25", "2015-03-16", "2015-12-31");

		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertEquals("PolicyPaymentTransactions size", 9, response.getPolicyPaymentTransactions().size());

		List<PolicyPaymentTransDTO> payments = response.getPolicyPaymentTransactions();
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 3, 16, 0, 0), payments.get(0).getPaymentCoverageStartDate());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 4, 1, 0, 0).minusDays(1), payments.get(0).getPaymentCoverageEndDate());

		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 4, 1, 0, 0), payments.get(3).getPaymentCoverageStartDate());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 5, 1, 0, 0).minusDays(1), payments.get(3).getPaymentCoverageEndDate());

		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 5, 1, 0, 0), payments.get(6).getPaymentCoverageStartDate());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 6, 1, 0, 0).minusDays(1), payments.get(6).getPaymentCoverageEndDate());

	}

	/*
	 * Retroactive Enrollment - first day of Payment Coverage Month >= Premium Eff Date
	 */
	@Test
	public void process_RetroActiveEnrollment_CoverageMonthStartDtNotLTPremiumEffStartDt() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-05-01 00:00:00", "2014-04-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroEnrollment("2015-03-01");
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-04-25", "2015-03-01", "2015-12-31");

		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertEquals("PolicyPaymentTransactions size", 9, response.getPolicyPaymentTransactions().size());

		List<PolicyPaymentTransDTO> payments = response.getPolicyPaymentTransactions();
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 3, 1, 0, 0), payments.get(0).getPaymentCoverageStartDate());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 4, 1, 0, 0).minusDays(1), payments.get(0).getPaymentCoverageEndDate());
	}

	/*
	 * Retroactive Enrollment - Last day of Payment Coverage Month > Premium Eff End Date
	 */
	@Test
	public void process_RetroActiveEnrollment_CoverageMonthEndDtGTPremiumEffEndDt() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-05-01 00:00:00", "2014-04-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroEnrollment("2015-03-01", "2015-05-15");
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-04-25", "2015-03-01", "2015-05-15");

		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertEquals("PolicyPaymentTransactions size", 9, response.getPolicyPaymentTransactions().size());

		List<PolicyPaymentTransDTO> payments = response.getPolicyPaymentTransactions();
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 3, 1, 0, 0), payments.get(0).getPaymentCoverageStartDate());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 5, 15, 0, 0), payments.get(8).getPaymentCoverageEndDate());
	}

	/*
	 * Retroactive Enrollment - Last day of Payment Coverage Month = Premium Eff End Date
	 */
	@Test
	public void process_RetroActiveEnrollment_CoverageMonthEndDtEQPremiumEffEndDt() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-05-01 00:00:00", "2014-04-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroEnrollment("2015-03-01", "2015-05-31");
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-04-25", "2015-03-01", "2015-05-31");

		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertEquals("PolicyPaymentTransactions size", 9, response.getPolicyPaymentTransactions().size());

		List<PolicyPaymentTransDTO> payments = response.getPolicyPaymentTransactions();
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 3, 1, 0, 0), payments.get(0).getPaymentCoverageStartDate());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 5, 31, 0, 0), payments.get(8).getPaymentCoverageEndDate());
	}

	/*
	 * Retroactive Enrollment - Last day of Payment Coverage Month < Premium Eff End Date
	 */
	@Test
	public void process_RetroActiveEnrollment_CoverageMonthEndDtLTPremiumEffEndDt() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-05-01 00:00:00", "2014-04-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroEnrollment("2015-03-01", "2015-06-01");
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-04-25", "2015-03-01", "2015-06-01");

		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertEquals("PolicyPaymentTransactions size", 9, response.getPolicyPaymentTransactions().size());

		List<PolicyPaymentTransDTO> payments = response.getPolicyPaymentTransactions();
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 3, 1, 0, 0), payments.get(0).getPaymentCoverageStartDate());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 5, 31, 0, 0), payments.get(8).getPaymentCoverageEndDate());
	}

	//*************** PP7 Changes - Proration and Partial Month Payments ****************


	/**
	 * Test FDD Scenario1 for APTC Payments 
	 * 
	 * Proration of retroactive payments following the mid-month termination of a policy.  In this 
	 * scenario the RAP batch executes after the insertion of a new Policy version into EPS.  
	 * The new policy contains a Policy Premium record with an Effective End Date prior to the Payment 
	 * Coverage End date on an existing payment transaction for the policy.  To complete the appropriate 
	 * adjustment, the RAP batch must reverse the full February payment and then create a new prorated 
	 * payment for the first half of the month.  Payment Coverage Start Date and Payment Coverage End Date 
	 * on the newly created transactions reflect the partial months of coverage for reporting purposes.  
	 * The Proration Factor stored on the new transactions reflects the ratio of the number of days of 
	 * coverage to the number of days in the full month.  The Payment Amount on each transaction reflects 
	 * the product of the proration factor for the transaction and corresponding monthly amount specified 
	 * in the Policy Premium Record (APTC,CSR) or the User Fee calculated by the APTC Roll-up batch.
	 */
	@Test
	public void testCreateAptcPayments_Scenario1_APTC() {
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		replay(mockRapDao);

		ReflectionTestUtils.setField(rapProcesssingServiceTest, "reversalRefIds", new ArrayList<Long>());
		ReflectionTestUtils.setField(rapProcesssingServiceTest, "reversedTransIds", new ArrayList<Long>());

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-06-01", "2015-01-01", "2015-02-15");

		RapServiceTestUtil.loadStateConfigMap(policyVersion.getSubscriberStateCd(),
				policyVersion.getPolicyStartDate().getYear(), ProrationType.FFM_PRORATING);
		
		DateTime coverageDt = new DateTime("2015-02-01");

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroPartialMonth_Scenario1_APTC();

		List<PolicyPaymentTransDTO>  paymentTransactions = new ArrayList<PolicyPaymentTransDTO>();

		ReflectionTestUtils.invokeMethod(rapProcesssingServiceTest, "createAptcPayments",
				policyVersion, coverageDt, paymentTransactions,
				policyDetailDTO.getPolicyPayments(), policyDetailDTO.getPolicyPremiums(), ProrationType.FFM_PRORATING);

		assertTrue("PolicyPaymentTransactions", CollectionUtils.isNotEmpty(paymentTransactions)); 
		assertEquals("PolicyPaymentTransactions size", 2, paymentTransactions.size());

		assertEquals("Payment Transaction values comparison", 
				new BigDecimal("-100.00"), paymentTransactions.get(0).getPaymentAmount());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 2, 1, 0, 0), paymentTransactions.get(0).getPaymentCoverageStartDate());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 2, 28, 0, 0), paymentTransactions.get(0).getPaymentCoverageEndDate());
		assertEquals("Payment Transaction values comparison", 
				1, paymentTransactions.get(0).getParentPolicyPaymentTransId().longValue());
		assertNotNull("MGP Id", paymentTransactions.get(0).getMarketplaceGroupPolicyId());
		

		assertEquals("Payment Transaction values comparison", 
				new BigDecimal("53.57"), paymentTransactions.get(1).getPaymentAmount());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 2, 1, 0, 0), paymentTransactions.get(1).getPaymentCoverageStartDate());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 2, 15, 0, 0), paymentTransactions.get(1).getPaymentCoverageEndDate());
		assertNull("Payment Transaction values comparison", 
				paymentTransactions.get(1).getParentPolicyPaymentTransId());
		assertNotNull("MGP Id", paymentTransactions.get(1).getMarketplaceGroupPolicyId());

	}

	/**
	 * Test FDD Scenario1 
	 * 
	 * Proration of retroactive payments following the mid-month termination of a policy.  In this 
	 * scenario the RAP batch executes after the insertion of a new Policy version into EPS.  
	 * The new policy contains a Policy Premium record with an Effective End Date prior to the Payment 
	 * Coverage End date on an existing payment transaction for the policy.  To complete the appropriate 
	 * adjustment, the RAP batch must reverse the full February payment and then create a new prorated 
	 * payment for the first half of the month.  Payment Coverage Start Date and Payment Coverage End Date 
	 * on the newly created transactions reflect the partial months of coverage for reporting purposes.  
	 * The Proration Factor stored on the new transactions reflects the ratio of the number of days of 
	 * coverage to the number of days in the full month.  The Payment Amount on each transaction reflects 
	 * the product of the proration factor for the transaction and corresponding monthly amount specified 
	 * in the Policy Premium Record (APTC,CSR) or the User Fee calculated by the APTC Roll-up batch.
	 */
	@Test
	public void testCreateRetroPayments_Scenario1() {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-02-01 00:00:00", "2015-01-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroPartialMonth_Scenario1();
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-06-01", "2015-01-01", "2015-02-15");

		RapServiceTestUtil.loadStateConfigMap(policyVersion.getSubscriberStateCd(),
				policyVersion.getPolicyStartDate().getYear(), ProrationType.FFM_PRORATING);
		
		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);

		List<PolicyPaymentTransDTO> paymentTransactions = response.getPolicyPaymentTransactions();


		assertTrue("PolicyPaymentTransactions", CollectionUtils.isNotEmpty(paymentTransactions)); 
		assertEquals("PolicyPaymentTransactions size", 6, paymentTransactions.size());

		PolicyPaymentTransDTO febAPTCPrev = paymentTransactions.get(0);
		assertEquals("Payment Amount", new BigDecimal("-100.00"), febAPTCPrev.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), febAPTCPrev.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), febAPTCPrev.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 4, febAPTCPrev.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", febAPTCPrev.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), febAPTCPrev.getCoverageDate());
		assertEquals("Program Type", "APTC", febAPTCPrev.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", febAPTCPrev.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", febAPTCPrev.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", febAPTCPrev.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO febAPTC = paymentTransactions.get(1);
		assertEquals("Payment Amount", new BigDecimal("53.57"), febAPTC.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), febAPTC.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 15, 0, 0), febAPTC.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", febAPTC.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", febAPTC.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), febAPTC.getCoverageDate());
		assertEquals("Program Type", "APTC", febAPTC.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", febAPTC.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", febAPTC.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", febAPTC.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO febCSRPrev = paymentTransactions.get(2);
		assertEquals("Payment Amount", new BigDecimal("-50.00"), febCSRPrev.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), febCSRPrev.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), febCSRPrev.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 5, febCSRPrev.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", febCSRPrev.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), febCSRPrev.getCoverageDate());
		assertEquals("Program Type", "CSR", febCSRPrev.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", febCSRPrev.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", febCSRPrev.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", febCSRPrev.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO febCSR = paymentTransactions.get(3);
		assertEquals("Payment Amount", new BigDecimal("26.79"), febCSR.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), febCSR.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 15, 0, 0), febCSR.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", febCSR.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", febCSR.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), febCSR.getCoverageDate());
		assertEquals("Program Type", "CSR", febCSR.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", febCSR.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", febCSR.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", febCSR.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO febUFPrev = paymentTransactions.get(4);
		assertEquals("Payment Amount", new BigDecimal("5.25"), febUFPrev.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), febUFPrev.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), febUFPrev.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 6, febUFPrev.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", febUFPrev.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), febUFPrev.getCoverageDate());
		assertEquals("Program Type", "UF", febUFPrev.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", febUFPrev.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", febUFPrev.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", febUFPrev.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO febUF = paymentTransactions.get(5);
		assertNull("Payment Amount", febUF.getPaymentAmount());
		assertEquals("Total PremiumAmount", new BigDecimal("80.36"), febUF.getTotalPremiumAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), febUF.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 15, 0, 0), febUF.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", febUF.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", febUF.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), febUF.getCoverageDate());
		assertEquals("Program Type", "UF", febUF.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", febUF.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", febUF.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", febUF.getMarketplaceGroupPolicyId());

	}

	/**
	 * Test FDD Scenario1 - FFM
	 * 
	 * Reflects the proration of retroactive payments following the mid-month termination of an FFM policy.  
	 * In this scenario the RAP batch executes after the insertion of a new Policy version into EPS.  
	 * The new policy contains a Policy Premium record with an Effective End Date prior to the Payment Coverage 
	 * End date on an existing payment transaction for the policy with prorated payment amounts included.  
	 * To complete the appropriate adjustment, the RAP batch must reverse the full February payment and then 
	 * create a new prorated payment for the first half of the month.  Payment Coverage Start Date and Payment 
	 * Coverage End Date on the newly created transactions reflect the partial months of coverage for reporting purposes. 
	 * The Proration Factor is not stored on the new transactions.  The Payment Amount on each transaction is taken from 
	 * the Prorated Payment Amounts provided for each corresponding Program in the Policy Premium Record. 
	 */
	@Test
	public void testCreateRetroPayments_Scenario1_FFM() {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-02-01 00:00:00", "2015-01-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroPartialMonth_Scenario1_FFM();
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-06-01", "2015-01-01", "2015-02-14");

		RapServiceTestUtil.loadStateConfigMap(policyVersion.getSubscriberStateCd(),
				policyVersion.getPolicyStartDate().getYear(), ProrationType.FFM_PRORATING);
		
		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);

		List<PolicyPaymentTransDTO> paymentTransactions = response.getPolicyPaymentTransactions();


		assertTrue("PolicyPaymentTransactions", CollectionUtils.isNotEmpty(paymentTransactions)); 
		assertEquals("PolicyPaymentTransactions size", 6, paymentTransactions.size());

		PolicyPaymentTransDTO febAPTCPrev = paymentTransactions.get(0);
		assertEquals("Payment Amount", new BigDecimal("-100.00"), febAPTCPrev.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), febAPTCPrev.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), febAPTCPrev.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 4, febAPTCPrev.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", febAPTCPrev.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), febAPTCPrev.getCoverageDate());
		assertEquals("Program Type", "APTC", febAPTCPrev.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", febAPTCPrev.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", febAPTCPrev.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", febAPTCPrev.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO febAPTC = paymentTransactions.get(1);
		assertEquals("Payment Amount", new BigDecimal("50.00"), febAPTC.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), febAPTC.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 14, 0, 0), febAPTC.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", febAPTC.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", febAPTC.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), febAPTC.getCoverageDate());
		assertEquals("Program Type", "APTC", febAPTC.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", febAPTC.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", febAPTC.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", febAPTC.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO febCSRPrev = paymentTransactions.get(2);
		assertEquals("Payment Amount", new BigDecimal("-50.00"), febCSRPrev.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), febCSRPrev.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), febCSRPrev.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 5, febCSRPrev.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", febCSRPrev.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), febCSRPrev.getCoverageDate());
		assertEquals("Program Type", "CSR", febCSRPrev.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", febCSRPrev.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", febCSRPrev.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", febCSRPrev.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO febCSR = paymentTransactions.get(3);
		assertEquals("Payment Amount", new BigDecimal("25.00"), febCSR.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), febCSR.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 14, 0, 0), febCSR.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", febCSR.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", febCSR.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), febCSR.getCoverageDate());
		assertEquals("Program Type", "CSR", febCSR.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", febCSR.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", febCSR.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", febCSR.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO febUFPrev = paymentTransactions.get(4);
		assertEquals("Payment Amount", new BigDecimal("5.25"), febUFPrev.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), febUFPrev.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), febUFPrev.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 6, febUFPrev.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", febUFPrev.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), febUFPrev.getCoverageDate());
		assertEquals("Program Type", "UF", febUFPrev.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", febUFPrev.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", febUFPrev.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", febUFPrev.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO febUF = paymentTransactions.get(5);
		assertNull("Payment Amount", febUF.getPaymentAmount());
		assertEquals("Total PremiumAmount", new BigDecimal("75.00"), febUF.getTotalPremiumAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), febUF.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 14, 0, 0), febUF.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", febUF.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", febUF.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), febUF.getCoverageDate());
		assertEquals("Program Type", "UF", febUF.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", febUF.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", febUF.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", febUF.getMarketplaceGroupPolicyId());
	}

	/*
	 * Dont create payments beyond premium end date 
	 */
	@Test
	public void testCreateRetroPayments_Scenario1A_FFM() {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-03-01 00:00:00", "2015-01-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroPartialMonth_Scenario1A_FFM();
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-06-01", "2015-01-01", "2015-12-31");

		RapServiceTestUtil.loadStateConfigMap(policyVersion.getSubscriberStateCd(),
				policyVersion.getPolicyStartDate().getYear(), ProrationType.FFM_PRORATING);
		
		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);

		List<PolicyPaymentTransDTO> paymentTransactions = response.getPolicyPaymentTransactions();


		assertTrue("PolicyPaymentTransactions", CollectionUtils.isNotEmpty(paymentTransactions)); 
		assertEquals("PolicyPaymentTransactions size", 6, paymentTransactions.size());

		PolicyPaymentTransDTO janAPTC = paymentTransactions.get(0);
		assertEquals("Payment Amount", new BigDecimal("100.00"), janAPTC.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 1, 1, 0, 0), janAPTC.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 1, 31, 0, 0), janAPTC.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", janAPTC.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", janAPTC.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), janAPTC.getCoverageDate());
		assertEquals("Program Type", "APTC", janAPTC.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", janAPTC.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", janAPTC.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", janAPTC.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO janCSR = paymentTransactions.get(1);
		assertEquals("Payment Amount", new BigDecimal("50.00"), janCSR.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 1, 1, 0, 0), janCSR.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 1, 31, 0, 0), janCSR.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", janCSR.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", janCSR.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), janCSR.getCoverageDate());
		assertEquals("Program Type", "CSR", janCSR.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", janCSR.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", janCSR.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", janCSR.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO janUF = paymentTransactions.get(2);
		assertNull("Payment Amount", janUF.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 1, 1, 0, 0), janUF.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 1, 31, 0, 0), janUF.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", janUF.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", janUF.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), janUF.getCoverageDate());
		assertEquals("Program Type", "UF", janUF.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", janUF.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", janUF.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", janUF.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO febAPTC = paymentTransactions.get(3);
		assertEquals("Payment Amount", new BigDecimal("50.00"), febAPTC.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), febAPTC.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 14, 0, 0), febAPTC.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", febAPTC.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", febAPTC.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), febAPTC.getCoverageDate());
		assertEquals("Program Type", "APTC", febAPTC.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", febAPTC.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", febAPTC.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", febAPTC.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO febCSR = paymentTransactions.get(4);
		assertEquals("Payment Amount", new BigDecimal("25.00"), febCSR.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), febCSR.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 14, 0, 0), febCSR.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", febCSR.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", febCSR.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), febCSR.getCoverageDate());
		assertEquals("Program Type", "CSR", febCSR.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", febCSR.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", febCSR.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", febCSR.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO febUF = paymentTransactions.get(5);
		assertNull("Payment Amount", febUF.getPaymentAmount());
		assertEquals("Total PremiumAmount", new BigDecimal("75.00"), febUF.getTotalPremiumAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), febUF.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 14, 0, 0), febUF.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", febUF.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", febUF.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), febUF.getCoverageDate());
		assertEquals("Program Type", "UF", febUF.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", febUF.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", febUF.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", febUF.getMarketplaceGroupPolicyId());

	}

	/*
	 * Do create payments when policy start date = end date 
	 */
	@Test
	public void testCreateRetroPayments_Scenario1B_FFM_ProratingState() {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-03-01 00:00:00", "2015-01-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroPartialMonth_Scenario1B_FFM();
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-06-01", "2015-01-01", "2015-01-01");
		policyVersion.setPolicyStatus(PolicyStatus.TERMINATED_4.getValue());
		
		RapServiceTestUtil.loadStateConfigMap(policyVersion.getSubscriberStateCd(),
				policyVersion.getPolicyStartDate().getYear(), ProrationType.FFM_PRORATING);
		
		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);

		List<PolicyPaymentTransDTO> paymentTransactions = response.getPolicyPaymentTransactions();

		assertTrue("PolicyPaymentTransactions", CollectionUtils.isNotEmpty(paymentTransactions)); 
		assertEquals("PolicyPaymentTransactions size", 3, paymentTransactions.size());

		PolicyPaymentTransDTO janAPTC = paymentTransactions.get(0);
		assertEquals("Program Type", "APTC", janAPTC.getFinancialProgramTypeCd());
		assertEquals("Payment Amount", new BigDecimal("3.23"), janAPTC.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 1, 1, 0, 0), janAPTC.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 1, 1, 0, 0), janAPTC.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", janAPTC.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", janAPTC.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), janAPTC.getCoverageDate());
		assertEquals("Trans Type", "PCYC", janAPTC.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", janAPTC.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", janAPTC.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO janCSR = paymentTransactions.get(1);
		assertEquals("Program Type", "CSR", janCSR.getFinancialProgramTypeCd());
		assertEquals("Payment Amount", new BigDecimal("1.61"), janCSR.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 1, 1, 0, 0), janCSR.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 1, 1, 0, 0), janCSR.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", janCSR.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", janCSR.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), janCSR.getCoverageDate());
		assertEquals("Trans Type", "PCYC", janCSR.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", janCSR.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", janCSR.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO janUF = paymentTransactions.get(2);
		assertEquals("Program Type", "UF", janUF.getFinancialProgramTypeCd());		
		assertNull("Payment Amount", janUF.getPaymentAmount());
		assertEquals("Total Payment Amount", new BigDecimal("4.84"), janUF.getTotalPremiumAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 1, 1, 0, 0), janUF.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 1, 1, 0, 0), janUF.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", janUF.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", janUF.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), janUF.getCoverageDate());
		assertEquals("Trans Type", "PCYC", janUF.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", janUF.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", janUF.getMarketplaceGroupPolicyId());
	}

	/*
	 * Do NOT create payments when policy start date = end date but status = CANCELLED
	 */
	@Test
	public void testCreateRetroPayments_Scenario1B_Cancel_FFM_ProratingState() {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-03-01 00:00:00", "2015-01-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroPartialMonth_Scenario1B_Cancel_FFM();
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-06-01", "2015-01-01", "2015-01-01");
		policyVersion.setPolicyStatus(PolicyStatus.CANCELLED_3.getValue());
		
		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);

		List<PolicyPaymentTransDTO> paymentTransactions = response.getPolicyPaymentTransactions();

		assertTrue("PolicyPaymentTransactions", CollectionUtils.isEmpty(paymentTransactions)); 
	}

	@Test
	public void testCreateRetroPayments_Scenario1B_FFM_NonProratingState() {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-03-01 00:00:00", "2015-01-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroPartialMonth_Scenario1B_FFM();
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-06-01", "2015-01-01", "2015-01-01");
		policyVersion.setPolicyStatus(PolicyStatus.TERMINATED_4.getValue());
		
		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);

		List<PolicyPaymentTransDTO> paymentTransactions = response.getPolicyPaymentTransactions();

		assertTrue("PolicyPaymentTransactions", CollectionUtils.isNotEmpty(paymentTransactions)); 
		assertEquals("PolicyPaymentTransactions size", 3, paymentTransactions.size());

		PolicyPaymentTransDTO janAPTC = paymentTransactions.get(0);
		assertEquals("Program Type", "APTC", janAPTC.getFinancialProgramTypeCd());
		assertEquals("Payment Amount", new BigDecimal("100"), janAPTC.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 1, 1, 0, 0), janAPTC.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 1, 1, 0, 0), janAPTC.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", janAPTC.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", janAPTC.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), janAPTC.getCoverageDate());
		assertEquals("Trans Type", "PCYC", janAPTC.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", janAPTC.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", janAPTC.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO janCSR = paymentTransactions.get(1);
		assertEquals("Program Type", "CSR", janCSR.getFinancialProgramTypeCd());
		assertEquals("Payment Amount", new BigDecimal("50"), janCSR.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 1, 1, 0, 0), janCSR.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 1, 1, 0, 0), janCSR.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", janCSR.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", janCSR.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), janCSR.getCoverageDate());
		assertEquals("Trans Type", "PCYC", janCSR.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", janCSR.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", janCSR.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO janUF = paymentTransactions.get(2);
		assertEquals("Program Type", "UF", janUF.getFinancialProgramTypeCd());		
		assertNull("Payment Amount", janUF.getPaymentAmount());
		assertEquals("Total Payment Amount", new BigDecimal("150"), janUF.getTotalPremiumAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 1, 1, 0, 0), janUF.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 1, 1, 0, 0), janUF.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", janUF.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", janUF.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), janUF.getCoverageDate());
		assertEquals("Trans Type", "PCYC", janUF.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", janUF.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", janUF.getMarketplaceGroupPolicyId());
	}

	/*
	 * Dont create payments when policy start date != end date but end date is 1st of a month 
	 * (Payment should be created through that month)
	 */
	@Test
	public void testCreateRetroPayments_Scenario1C_FFM() {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-03-01 00:00:00", "2015-01-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroPartialMonth_Scenario1C_FFM();
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-06-01", "2015-01-01", "2015-03-01");

		RapServiceTestUtil.loadStateConfigMap(policyVersion.getSubscriberStateCd(),
				policyVersion.getPolicyStartDate().getYear(), ProrationType.FFM_PRORATING);
		
		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);

		List<PolicyPaymentTransDTO> paymentTransactions = response.getPolicyPaymentTransactions();


		assertTrue("PolicyPaymentTransactions", CollectionUtils.isNotEmpty(paymentTransactions)); 
		assertEquals("PolicyPaymentTransactions size", 9, paymentTransactions.size());

		PolicyPaymentTransDTO janAPTC = paymentTransactions.get(0);
		assertEquals("Payment Amount", new BigDecimal("100.00"), janAPTC.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 1, 1, 0, 0), janAPTC.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 1, 31, 0, 0), janAPTC.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", janAPTC.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", janAPTC.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), janAPTC.getCoverageDate());
		assertEquals("Program Type", "APTC", janAPTC.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", janAPTC.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", janAPTC.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", janAPTC.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO janCSR = paymentTransactions.get(1);
		assertEquals("Payment Amount", new BigDecimal("50.00"), janCSR.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 1, 1, 0, 0), janCSR.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 1, 31, 0, 0), janCSR.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", janCSR.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", janCSR.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), janCSR.getCoverageDate());
		assertEquals("Program Type", "CSR", janCSR.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", janCSR.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", janCSR.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", janCSR.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO janUF = paymentTransactions.get(2);
		assertNull("Payment Amount", janUF.getPaymentAmount());
		assertEquals("Total PremiumAmount", new BigDecimal("150.00"), janUF.getTotalPremiumAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 1, 1, 0, 0), janUF.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 1, 31, 0, 0), janUF.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", janUF.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", janUF.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), janUF.getCoverageDate());
		assertEquals("Program Type", "UF", janUF.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", janUF.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", janUF.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", janUF.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO febAPTC = paymentTransactions.get(3);
		assertEquals("Payment Amount", new BigDecimal("100.00"), febAPTC.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), febAPTC.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), febAPTC.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", febAPTC.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", febAPTC.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), febAPTC.getCoverageDate());
		assertEquals("Program Type", "APTC", febAPTC.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", febAPTC.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", febAPTC.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", febAPTC.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO febCSR = paymentTransactions.get(4);
		assertEquals("Payment Amount", new BigDecimal("50.00"), febCSR.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), febCSR.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), febCSR.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", febCSR.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", febCSR.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), febCSR.getCoverageDate());
		assertEquals("Program Type", "CSR", febCSR.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", febCSR.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", febCSR.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", febCSR.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO febUF = paymentTransactions.get(5);
		assertNull("Payment Amount", febUF.getPaymentAmount());
		assertEquals("Total PremiumAmount", new BigDecimal("150.00"), febUF.getTotalPremiumAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), febUF.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), febUF.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", febUF.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", febUF.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), febUF.getCoverageDate());
		assertEquals("Program Type", "UF", febUF.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", febUF.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", febUF.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", febUF.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO marAPTC = paymentTransactions.get(6);
		assertEquals("Payment Amount", new BigDecimal("3.23"), marAPTC.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 3, 1, 0, 0), marAPTC.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 3, 1, 0, 0), marAPTC.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", marAPTC.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", marAPTC.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-03-01"), marAPTC.getCoverageDate());
		assertEquals("Program Type", "APTC", marAPTC.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", marAPTC.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", marAPTC.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", marAPTC.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO marCSR = paymentTransactions.get(7);
		assertEquals("Payment Amount", new BigDecimal("1.61"), marCSR.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 3, 1, 0, 0), marCSR.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 3, 1, 0, 0), marCSR.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", marCSR.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", marCSR.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-03-01"), marCSR.getCoverageDate());
		assertEquals("Program Type", "CSR", marCSR.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", marCSR.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", marCSR.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", marCSR.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO marUF = paymentTransactions.get(8);
		assertNull("Payment Amount", marUF.getPaymentAmount());
		assertEquals("Total PremiumAmount", new BigDecimal("4.84"), marUF.getTotalPremiumAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 3, 1, 0, 0), marUF.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 3, 1, 0, 0), marUF.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", marUF.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", marUF.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-03-01"), marUF.getCoverageDate());
		assertEquals("Program Type", "UF", marUF.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", marUF.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", marUF.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", marUF.getMarketplaceGroupPolicyId());
	}

	/**
	 * Test FDD Scenario1 - Non Prorating state
	 * 
	 * Proration of retroactive payments following the mid-month termination of an SBM policy in a 
	 * non-prorating state. In this scenario the RAP batch executes after the insertion of a new Policy 
	 * version into EPS. The new policy contains a Policy Premium record with an Effective End Date prior to 
	 * the Payment Coverage End date on an existing payment transaction for the policy. To complete the 
	 * appropriate adjustment, the RAP batch must reverse the full February payment and then creates a new 
	 * prorated payment (of equal amount) that reflects payment dates for the first half of February.  
	 * Payment Coverage Start Date and Payment Coverage End Date on the newly created transactions reflect the 
	 * partial months of coverage for reporting purposes but have the full monthly amount. The Proration Factor 
	 * stored on the new transactions is set to 1.  The Payment Amount on each APTC & CSR transaction reflects 
	 * the product of the proration factor for the transaction and corresponding monthly amount specified in the 
	 * Policy Premium Record (APTC,CSR).
	 */
	@Test
	public void testCreateRetroPayments_Scenario1_NonProrating() {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-02-01 00:00:00", "2015-01-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroPartialMonth_Scenario1();
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-06-01", "2015-01-01", "2015-02-15");

		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);

		List<PolicyPaymentTransDTO> paymentTransactions = response.getPolicyPaymentTransactions();


		assertTrue("PolicyPaymentTransactions", CollectionUtils.isNotEmpty(paymentTransactions)); 
		assertEquals("PolicyPaymentTransactions size", 6, paymentTransactions.size());

		PolicyPaymentTransDTO febAPTCPrev = paymentTransactions.get(0);
		assertEquals("Payment Amount", new BigDecimal("-100.00"), febAPTCPrev.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), febAPTCPrev.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), febAPTCPrev.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 4, febAPTCPrev.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", febAPTCPrev.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), febAPTCPrev.getCoverageDate());
		assertEquals("Program Type", "APTC", febAPTCPrev.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", febAPTCPrev.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", febAPTCPrev.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", febAPTCPrev.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO febAPTC = paymentTransactions.get(1);
		assertEquals("Payment Amount", new BigDecimal("100"), febAPTC.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), febAPTC.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 15, 0, 0), febAPTC.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", febAPTC.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", febAPTC.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), febAPTC.getCoverageDate());
		assertEquals("Program Type", "APTC", febAPTC.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", febAPTC.getLastPaymentProcStatusTypeCd());
		assertEquals("Proration Days", 28, febAPTC.getProrationDaysOfCoverageNum().intValue());
		assertNotNull("MGP Id", febAPTC.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", febAPTC.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO febCSRPrev = paymentTransactions.get(2);
		assertEquals("Payment Amount", new BigDecimal("-50.00"), febCSRPrev.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), febCSRPrev.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), febCSRPrev.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 5, febCSRPrev.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", febCSRPrev.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), febCSRPrev.getCoverageDate());
		assertEquals("Program Type", "CSR", febCSRPrev.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", febCSRPrev.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", febCSRPrev.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", febCSRPrev.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO febCSR = paymentTransactions.get(3);
		assertEquals("Payment Amount", new BigDecimal("50"), febCSR.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), febCSR.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 15, 0, 0), febCSR.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", febCSR.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", febCSR.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), febCSR.getCoverageDate());
		assertEquals("Program Type", "CSR", febCSR.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", febCSR.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", febCSR.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", febCSR.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO febUFPrev = paymentTransactions.get(4);
		assertEquals("Payment Amount", new BigDecimal("5.25"), febUFPrev.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), febUFPrev.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), febUFPrev.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 6, febUFPrev.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", febUFPrev.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), febUFPrev.getCoverageDate());
		assertEquals("Program Type", "UF", febUFPrev.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", febUFPrev.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", febUFPrev.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", febUFPrev.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO febUF = paymentTransactions.get(5);
		assertNull("Payment Amount", febUF.getPaymentAmount());
		assertEquals("Total PremiumAmount", new BigDecimal("150"), febUF.getTotalPremiumAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), febUF.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 15, 0, 0), febUF.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", febUF.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", febUF.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), febUF.getCoverageDate());
		assertEquals("Program Type", "UF", febUF.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", febUF.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", febUF.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", febUF.getMarketplaceGroupPolicyId());

	}

	/**
	 * Test FDD Scenario2 for APTC Payments 
	 * 
	 * Proration of retroactive payments following a mid-month birth of a child or adoption. 
	 * This example illustrates the current FFM Change in Circumstance (CIC).  Following the termination 
	 * of the first policy and insertion of the Policy version into EPS, RAP would reverse the existing 
	 * February payment and create prorated February payment for the partial month of coverage.  
	 * When a new Policy is (1701) is then inserted into EPS to represent the addition of the child, 
	 * the RAP batch will identify an outstanding payment for the partial month of February.  Using the 
	 * Effective Start Date of the Policy Premium record, RAP must create a prorated payment for the 
	 * partial month.  Payment Coverage Start Date and Payment Coverage End Date on the newly created 
	 * transactions reflect the partial months of coverage for reporting purposes. The Proration Factor 
	 * stored on the new transactions reflects the ratio of the number of days of coverage to the number 
	 * of days in the full month.  The Payment Amount on each transaction reflects the product of the 
	 * proration factor for the transaction and corresponding monthly amount specified in the Policy 
	 * Premium Record (APTC,CSR).
	 */
	@Test
	public void testCreateAptcPayments_Scenario2_APTC() {
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		replay(mockRapDao);

		ReflectionTestUtils.setField(rapProcesssingServiceTest, "reversalRefIds", new ArrayList<Long>());
		ReflectionTestUtils.setField(rapProcesssingServiceTest, "reversedTransIds", new ArrayList<Long>());

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-06-01", "2015-01-16", "2015-02-15");

		RapServiceTestUtil.loadStateConfigMap(policyVersion.getSubscriberStateCd(),
				policyVersion.getPolicyStartDate().getYear(), ProrationType.FFM_PRORATING);
		
		DateTime coverageDt = new DateTime("2015-02-01");

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroPartialMonth_Scenario2_APTC();

		List<PolicyPaymentTransDTO>  paymentTransactions = new ArrayList<PolicyPaymentTransDTO>();

		ReflectionTestUtils.invokeMethod(rapProcesssingServiceTest, "createAptcPayments",
				policyVersion, coverageDt, paymentTransactions,
				policyDetailDTO.getPolicyPayments(), policyDetailDTO.getPolicyPremiums(), ProrationType.FFM_PRORATING);

		assertTrue("PolicyPaymentTransactions", CollectionUtils.isNotEmpty(paymentTransactions)); 
		assertEquals("PolicyPaymentTransactions size", 1, paymentTransactions.size());

		assertEquals("Payment Transaction values comparison", 
				new BigDecimal("92.86"), paymentTransactions.get(0).getPaymentAmount());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 2, 16, 0, 0), paymentTransactions.get(0).getPaymentCoverageStartDate());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 2, 28, 0, 0), paymentTransactions.get(0).getPaymentCoverageEndDate());
		assertNull("Payment Transaction values comparison", 
				paymentTransactions.get(0).getParentPolicyPaymentTransId());

	}

	/**
	 * Test FDD Scenario2
	 * 
	 * Proration of retroactive payments following a mid-month birth of a child or adoption. 
	 * This example illustrates the current FFM Change in Circumstance (CIC).  Following the termination 
	 * of the first policy and insertion of the Policy version into EPS, RAP would reverse the existing 
	 * February payment and create prorated February payment for the partial month of coverage.  
	 * When a new Policy is (1701) is then inserted into EPS to represent the addition of the child, 
	 * the RAP batch will identify an outstanding payment for the partial month of February.  Using the 
	 * Effective Start Date of the Policy Premium record, RAP must create a prorated payment for the 
	 * partial month.  Payment Coverage Start Date and Payment Coverage End Date on the newly created 
	 * transactions reflect the partial months of coverage for reporting purposes. The Proration Factor 
	 * stored on the new transactions reflects the ratio of the number of days of coverage to the number 
	 * of days in the full month.  The Payment Amount on each transaction reflects the product of the 
	 * proration factor for the transaction and corresponding monthly amount specified in the Policy 
	 * Premium Record (APTC,CSR).
	 */
	@Test
	public void testCreateRetroPayments_Scenario2() {
		CodeRecord codeRecord = new CodeRecord("ERC", "2015-02-01 00:00:00", "2015-01-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroPartialMonth_Scenario2();
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-06-01", "2015-02-16", "2015-12-31");

		RapServiceTestUtil.loadStateConfigMap(policyVersion.getSubscriberStateCd(),
				policyVersion.getPolicyStartDate().getYear(), ProrationType.FFM_PRORATING);
		
		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);

		List<PolicyPaymentTransDTO> paymentTransactions = response.getPolicyPaymentTransactions();


		assertTrue("PolicyPaymentTransactions", CollectionUtils.isNotEmpty(paymentTransactions)); 
		assertEquals("PolicyPaymentTransactions size", 9, paymentTransactions.size());

		PolicyPaymentTransDTO aptcAmt = paymentTransactions.get(0);
		assertEquals("Payment Amount", new BigDecimal("-100.00"), aptcAmt.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 1, 1, 0, 0), aptcAmt.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 1, 31, 0, 0), aptcAmt.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 1, aptcAmt.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", aptcAmt.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), aptcAmt.getCoverageDate());
		assertEquals("Program Type", "APTC", aptcAmt.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", aptcAmt.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", aptcAmt.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", aptcAmt.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO csrAmt = paymentTransactions.get(1);
		assertEquals("Payment Amount", new BigDecimal("-50.00"), csrAmt.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 1, 1, 0, 0), csrAmt.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 1, 31, 0, 0), csrAmt.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 2, csrAmt.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", csrAmt.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), csrAmt.getCoverageDate());
		assertEquals("Program Type", "CSR", csrAmt.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", csrAmt.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", csrAmt.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", csrAmt.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO ufAmt = paymentTransactions.get(2);
		assertEquals("Payment Amount", new BigDecimal("5.25"), ufAmt.getPaymentAmount());
		assertEquals("Total PremiumAmount", new BigDecimal("150.00"), ufAmt.getTotalPremiumAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 1, 1, 0, 0), ufAmt.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 1, 31, 0, 0), ufAmt.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 3, ufAmt.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", ufAmt.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), ufAmt.getCoverageDate());
		assertEquals("Program Type", "UF", ufAmt.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", ufAmt.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", ufAmt.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", ufAmt.getMarketplaceGroupPolicyId());

		aptcAmt = paymentTransactions.get(3);
		assertEquals("Payment Amount", new BigDecimal("92.86"), aptcAmt.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 16, 0, 0), aptcAmt.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), aptcAmt.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", aptcAmt.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", aptcAmt.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), aptcAmt.getCoverageDate());
		assertEquals("Program Type", "APTC", aptcAmt.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", aptcAmt.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", aptcAmt.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", aptcAmt.getMarketplaceGroupPolicyId());

		aptcAmt = paymentTransactions.get(4);
		assertEquals("Payment Amount", new BigDecimal("-53.57"), aptcAmt.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), aptcAmt.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 15, 0, 0), aptcAmt.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 10, aptcAmt.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", aptcAmt.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), aptcAmt.getCoverageDate());
		assertEquals("Program Type", "APTC", aptcAmt.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", aptcAmt.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", aptcAmt.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", aptcAmt.getMarketplaceGroupPolicyId());

		csrAmt = paymentTransactions.get(5);
		assertEquals("Payment Amount", new BigDecimal("23.21"), csrAmt.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 16, 0, 0), csrAmt.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), csrAmt.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", csrAmt.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", csrAmt.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), csrAmt.getCoverageDate());
		assertEquals("Program Type", "CSR", csrAmt.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", csrAmt.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", csrAmt.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", csrAmt.getMarketplaceGroupPolicyId());

		csrAmt = paymentTransactions.get(6);
		assertEquals("Payment Amount", new BigDecimal("-26.79"), csrAmt.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), csrAmt.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 15, 0, 0), csrAmt.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 11, csrAmt.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", csrAmt.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), csrAmt.getCoverageDate());
		assertEquals("Program Type", "CSR", csrAmt.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", csrAmt.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", aptcAmt.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", aptcAmt.getMarketplaceGroupPolicyId());

		ufAmt = paymentTransactions.get(7);
		assertNull("Payment Amount", ufAmt.getPaymentAmount());
		assertEquals("Total PremiumAmount", new BigDecimal("116.07"), ufAmt.getTotalPremiumAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 16, 0, 0), ufAmt.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), ufAmt.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", ufAmt.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", ufAmt.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), ufAmt.getCoverageDate());
		assertEquals("Program Type", "UF", ufAmt.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", ufAmt.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", ufAmt.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", ufAmt.getMarketplaceGroupPolicyId());

		ufAmt = paymentTransactions.get(8);
		assertEquals("Payment Amount", new BigDecimal("2.81"), ufAmt.getPaymentAmount());
		assertEquals("Total PremiumAmount", new BigDecimal("80.36"), ufAmt.getTotalPremiumAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), ufAmt.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 15, 0, 0), ufAmt.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 12, ufAmt.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", ufAmt.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), ufAmt.getCoverageDate());
		assertEquals("Program Type", "UF", ufAmt.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", ufAmt.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", ufAmt.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", ufAmt.getMarketplaceGroupPolicyId());
	}

	@Test
	public void testCreateRetroPayments_Scenario2_ProratedAmtExist() {
		CodeRecord codeRecord = new CodeRecord("ERC", "2015-02-01 00:00:00", "2015-01-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroPartialMonth_Scenario2_ProratedAmtExist();
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-06-01", "2015-02-16", "2015-12-31");

		RapServiceTestUtil.loadStateConfigMap(policyVersion.getSubscriberStateCd(),
				policyVersion.getPolicyStartDate().getYear(), ProrationType.FFM_PRORATING);
		
		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);

		List<PolicyPaymentTransDTO> paymentTransactions = response.getPolicyPaymentTransactions();


		assertTrue("PolicyPaymentTransactions", CollectionUtils.isNotEmpty(paymentTransactions)); 
		assertEquals("PolicyPaymentTransactions size", 9, paymentTransactions.size());

		PolicyPaymentTransDTO aptcAmt = paymentTransactions.get(0);
		assertEquals("Payment Amount", new BigDecimal("-100.00"), aptcAmt.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 1, 1, 0, 0), aptcAmt.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 1, 31, 0, 0), aptcAmt.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 1, aptcAmt.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", aptcAmt.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), aptcAmt.getCoverageDate());
		assertEquals("Program Type", "APTC", aptcAmt.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", aptcAmt.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", aptcAmt.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", aptcAmt.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO csrAmt = paymentTransactions.get(1);
		assertEquals("Payment Amount", new BigDecimal("-50.00"), csrAmt.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 1, 1, 0, 0), csrAmt.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 1, 31, 0, 0), csrAmt.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 2, csrAmt.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", csrAmt.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), csrAmt.getCoverageDate());
		assertEquals("Program Type", "CSR", csrAmt.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", csrAmt.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", csrAmt.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", csrAmt.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO ufAmt = paymentTransactions.get(2);
		assertEquals("Payment Amount", new BigDecimal("5.25"), ufAmt.getPaymentAmount());
		assertEquals("Total PremiumAmount", new BigDecimal("150.00"), ufAmt.getTotalPremiumAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 1, 1, 0, 0), ufAmt.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 1, 31, 0, 0), ufAmt.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 3, ufAmt.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", ufAmt.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), ufAmt.getCoverageDate());
		assertEquals("Program Type", "UF", ufAmt.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", ufAmt.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", ufAmt.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", ufAmt.getMarketplaceGroupPolicyId());

		aptcAmt = paymentTransactions.get(3);
		assertEquals("Payment Amount", new BigDecimal("-53.57"), aptcAmt.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 16, 0, 0), aptcAmt.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), aptcAmt.getPaymentCoverageEndDate());
		assertNotNull("Parent PolicyPayment Trans Id", aptcAmt.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", aptcAmt.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), aptcAmt.getCoverageDate());
		assertEquals("Program Type", "APTC", aptcAmt.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", aptcAmt.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", aptcAmt.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", aptcAmt.getMarketplaceGroupPolicyId());

		aptcAmt = paymentTransactions.get(4);
		assertEquals("Payment Amount", new BigDecimal("92.86"), aptcAmt.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 16, 0, 0), aptcAmt.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), aptcAmt.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", aptcAmt.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", aptcAmt.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), aptcAmt.getCoverageDate());
		assertEquals("Program Type", "APTC", aptcAmt.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", aptcAmt.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", aptcAmt.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", aptcAmt.getMarketplaceGroupPolicyId());

		csrAmt = paymentTransactions.get(5);
		assertEquals("Payment Amount", new BigDecimal("-26.79"), csrAmt.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 16, 0, 0), csrAmt.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), csrAmt.getPaymentCoverageEndDate());
		assertNotNull("Parent PolicyPayment Trans Id", csrAmt.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", csrAmt.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), csrAmt.getCoverageDate());
		assertEquals("Program Type", "CSR", csrAmt.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", csrAmt.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", csrAmt.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", csrAmt.getMarketplaceGroupPolicyId());

		csrAmt = paymentTransactions.get(6);
		assertEquals("Payment Amount", new BigDecimal("23.21"), csrAmt.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 16, 0, 0), csrAmt.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), csrAmt.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", csrAmt.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", csrAmt.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), csrAmt.getCoverageDate());
		assertEquals("Program Type", "CSR", csrAmt.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", csrAmt.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", csrAmt.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", csrAmt.getMarketplaceGroupPolicyId());

		ufAmt = paymentTransactions.get(7);
		assertEquals("Payment Amount", new BigDecimal("2.81"), ufAmt.getPaymentAmount());
		assertEquals("Total PremiumAmount", new BigDecimal("80.36"), ufAmt.getTotalPremiumAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 16, 0, 0), ufAmt.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), ufAmt.getPaymentCoverageEndDate());
		assertNotNull("Parent PolicyPayment Trans Id", ufAmt.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", ufAmt.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), ufAmt.getCoverageDate());
		assertEquals("Program Type", "UF", ufAmt.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", ufAmt.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", ufAmt.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", ufAmt.getMarketplaceGroupPolicyId());

		ufAmt = paymentTransactions.get(8);
		assertNull("Payment Amount", ufAmt.getPaymentAmount());
		assertEquals("Total PremiumAmount", new BigDecimal("116.07"), ufAmt.getTotalPremiumAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 16, 0, 0), ufAmt.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), ufAmt.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", ufAmt.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", ufAmt.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), ufAmt.getCoverageDate());
		assertEquals("Program Type", "UF", ufAmt.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", ufAmt.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", ufAmt.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", ufAmt.getMarketplaceGroupPolicyId());
	}

	@Test
	public void testCreateRetroPayments_Scenario2_NonProrating() {
		CodeRecord codeRecord = new CodeRecord("ERC", "2015-02-01 00:00:00", "2015-01-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroPartialMonth_Scenario2_NonProrating();
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-06-01", "2015-02-16", "2015-12-31");

		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);

		List<PolicyPaymentTransDTO> paymentTransactions = response.getPolicyPaymentTransactions();


		assertTrue("PolicyPaymentTransactions", CollectionUtils.isNotEmpty(paymentTransactions)); 
		assertEquals("PolicyPaymentTransactions size", 6, paymentTransactions.size());

		PolicyPaymentTransDTO aptcAmt = paymentTransactions.get(0);
		assertEquals("Payment Amount", new BigDecimal("-100.00"), aptcAmt.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 1, 1, 0, 0), aptcAmt.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 1, 31, 0, 0), aptcAmt.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 1, aptcAmt.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", aptcAmt.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), aptcAmt.getCoverageDate());
		assertEquals("Program Type", "APTC", aptcAmt.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", aptcAmt.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", aptcAmt.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", aptcAmt.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO csrAmt = paymentTransactions.get(1);
		assertEquals("Payment Amount", new BigDecimal("-50.00"), csrAmt.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 1, 1, 0, 0), csrAmt.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 1, 31, 0, 0), csrAmt.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 2, csrAmt.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", csrAmt.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), csrAmt.getCoverageDate());
		assertEquals("Program Type", "CSR", csrAmt.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", csrAmt.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", csrAmt.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", csrAmt.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO ufAmt = paymentTransactions.get(2);
		assertEquals("Payment Amount", new BigDecimal("5.25"), ufAmt.getPaymentAmount());
		assertEquals("Total PremiumAmount", new BigDecimal("150.00"), ufAmt.getTotalPremiumAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 1, 1, 0, 0), ufAmt.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 1, 31, 0, 0), ufAmt.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 3, ufAmt.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", ufAmt.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), ufAmt.getCoverageDate());
		assertEquals("Program Type", "UF", ufAmt.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", ufAmt.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", ufAmt.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", ufAmt.getMarketplaceGroupPolicyId());


		aptcAmt = paymentTransactions.get(3);
		assertEquals("Payment Amount", new BigDecimal("200"), aptcAmt.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 16, 0, 0), aptcAmt.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), aptcAmt.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", aptcAmt.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", aptcAmt.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), aptcAmt.getCoverageDate());
		assertEquals("Program Type", "APTC", aptcAmt.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", aptcAmt.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", aptcAmt.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", aptcAmt.getMarketplaceGroupPolicyId());

		csrAmt = paymentTransactions.get(4);
		assertEquals("Payment Amount", new BigDecimal("50"), csrAmt.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 16, 0, 0), csrAmt.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), csrAmt.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", csrAmt.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", csrAmt.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), csrAmt.getCoverageDate());
		assertEquals("Program Type", "CSR", csrAmt.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", csrAmt.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", csrAmt.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", csrAmt.getMarketplaceGroupPolicyId());

		ufAmt = paymentTransactions.get(5);
		assertNull("Payment Amount", ufAmt.getPaymentAmount());
		assertEquals("Total PremiumAmount", new BigDecimal("250"), ufAmt.getTotalPremiumAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 16, 0, 0), ufAmt.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), ufAmt.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", ufAmt.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", ufAmt.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), ufAmt.getCoverageDate());
		assertEquals("Program Type", "UF", ufAmt.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", ufAmt.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", ufAmt.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", ufAmt.getMarketplaceGroupPolicyId());
	}

	@Test
	public void testCreateRetroPayments_Scenario2_NonProrating_PmtExists() {
		CodeRecord codeRecord = new CodeRecord("ERC", "2015-02-01 00:00:00", "2015-01-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroPartialMonth_Scenario2_NonProrating_PmtExists();
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-06-01", "2015-02-16", "2015-12-31");

		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);

		List<PolicyPaymentTransDTO> paymentTransactions = response.getPolicyPaymentTransactions();


		assertTrue("PolicyPaymentTransactions", CollectionUtils.isNotEmpty(paymentTransactions)); 
		assertEquals("PolicyPaymentTransactions size", 9, paymentTransactions.size());

		PolicyPaymentTransDTO aptcAmt = paymentTransactions.get(0);
		assertEquals("Payment Amount", new BigDecimal("-100.00"), aptcAmt.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 1, 1, 0, 0), aptcAmt.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 1, 31, 0, 0), aptcAmt.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 1, aptcAmt.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", aptcAmt.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), aptcAmt.getCoverageDate());
		assertEquals("Program Type", "APTC", aptcAmt.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", aptcAmt.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", aptcAmt.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", aptcAmt.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO csrAmt = paymentTransactions.get(1);
		assertEquals("Payment Amount", new BigDecimal("-50.00"), csrAmt.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 1, 1, 0, 0), csrAmt.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 1, 31, 0, 0), csrAmt.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 2, csrAmt.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", csrAmt.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), csrAmt.getCoverageDate());
		assertEquals("Program Type", "CSR", csrAmt.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", csrAmt.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", csrAmt.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", csrAmt.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO ufAmt = paymentTransactions.get(2);
		assertEquals("Payment Amount", new BigDecimal("5.25"), ufAmt.getPaymentAmount());
		assertEquals("Total PremiumAmount", new BigDecimal("150.00"), ufAmt.getTotalPremiumAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 1, 1, 0, 0), ufAmt.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 1, 31, 0, 0), ufAmt.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 3, ufAmt.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", ufAmt.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), ufAmt.getCoverageDate());
		assertEquals("Program Type", "UF", ufAmt.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", ufAmt.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", ufAmt.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", ufAmt.getMarketplaceGroupPolicyId());

		aptcAmt = paymentTransactions.get(3);
		assertEquals("Payment Amount", new BigDecimal("200"), aptcAmt.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 16, 0, 0), aptcAmt.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), aptcAmt.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", aptcAmt.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", aptcAmt.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), aptcAmt.getCoverageDate());
		assertEquals("Program Type", "APTC", aptcAmt.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", aptcAmt.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", aptcAmt.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", aptcAmt.getMarketplaceGroupPolicyId());

		aptcAmt = paymentTransactions.get(4);
		assertEquals("Payment Amount", new BigDecimal("-53.57"), aptcAmt.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), aptcAmt.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 15, 0, 0), aptcAmt.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 10L, aptcAmt.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", aptcAmt.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), aptcAmt.getCoverageDate());
		assertEquals("Program Type", "APTC", aptcAmt.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", aptcAmt.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", aptcAmt.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", aptcAmt.getMarketplaceGroupPolicyId());

		csrAmt = paymentTransactions.get(5);
		assertEquals("Payment Amount", new BigDecimal("50"), csrAmt.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 16, 0, 0), csrAmt.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), csrAmt.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", csrAmt.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", csrAmt.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), csrAmt.getCoverageDate());
		assertEquals("Program Type", "CSR", csrAmt.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", csrAmt.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", csrAmt.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", csrAmt.getMarketplaceGroupPolicyId());

		csrAmt = paymentTransactions.get(6);
		assertEquals("Payment Amount", new BigDecimal("-26.79"), csrAmt.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), csrAmt.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 15, 0, 0), csrAmt.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 11L, csrAmt.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", csrAmt.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), csrAmt.getCoverageDate());
		assertEquals("Program Type", "CSR", csrAmt.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", csrAmt.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", csrAmt.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", csrAmt.getMarketplaceGroupPolicyId());

		ufAmt = paymentTransactions.get(7);
		assertNull("Payment Amount", ufAmt.getPaymentAmount());
		assertEquals("Total PremiumAmount", new BigDecimal("250"), ufAmt.getTotalPremiumAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 16, 0, 0), ufAmt.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), ufAmt.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", ufAmt.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", ufAmt.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), ufAmt.getCoverageDate());
		assertEquals("Program Type", "UF", ufAmt.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", ufAmt.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", ufAmt.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", ufAmt.getMarketplaceGroupPolicyId());

		ufAmt = paymentTransactions.get(8);
		assertEquals("Payment Amount", new BigDecimal("2.81"), ufAmt.getPaymentAmount());
		assertEquals("Total PremiumAmount", new BigDecimal("80.36"), ufAmt.getTotalPremiumAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), ufAmt.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 15, 0, 0), ufAmt.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 12L, ufAmt.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", ufAmt.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), ufAmt.getCoverageDate());
		assertEquals("Program Type", "UF", ufAmt.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", ufAmt.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", ufAmt.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", ufAmt.getMarketplaceGroupPolicyId());
	}

	/**
	 * PVD < ERC Date, Min Payment Month/(policy start date) < max payment month (CPP coverage dt)
	 */
	@Test
	public void testCreateRetroPayments_Scenario2_PmtExists_MaxPmtMonth_LT_MinPmtMonth() {
		CodeRecord codeRecord = new CodeRecord("ERC", "2015-02-01 00:00:00", "2015-02-01 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroPartialMonth_Scenario2_NonProrating_PmtExists();
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-01-01", "2015-02-10", "2015-02-28");

		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);

		List<PolicyPaymentTransDTO> paymentTransactions = response.getPolicyPaymentTransactions();

		assertTrue("PolicyPaymentTransactions", paymentTransactions.size()==6); 
		
		assertEquals("PolicyPaymentTransactions size", 6, paymentTransactions.size());

		PolicyPaymentTransDTO aptcAmt = paymentTransactions.get(0);
		assertEquals("Payment Amount", new BigDecimal("-100.00"), aptcAmt.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 1, 1, 0, 0), aptcAmt.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 1, 31, 0, 0), aptcAmt.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 1, aptcAmt.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", aptcAmt.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), aptcAmt.getCoverageDate());
		assertEquals("Program Type", "APTC", aptcAmt.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", aptcAmt.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", aptcAmt.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", aptcAmt.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO csrAmt = paymentTransactions.get(1);
		assertEquals("Payment Amount", new BigDecimal("-50.00"), csrAmt.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 1, 1, 0, 0), csrAmt.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 1, 31, 0, 0), csrAmt.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 2, csrAmt.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", csrAmt.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), csrAmt.getCoverageDate());
		assertEquals("Program Type", "CSR", csrAmt.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", csrAmt.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", csrAmt.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", csrAmt.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO ufAmt = paymentTransactions.get(2);
		assertEquals("Payment Amount", new BigDecimal("5.25"), ufAmt.getPaymentAmount());
		assertEquals("Total PremiumAmount", new BigDecimal("150.00"), ufAmt.getTotalPremiumAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 1, 1, 0, 0), ufAmt.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 1, 31, 0, 0), ufAmt.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 3, ufAmt.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", ufAmt.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), ufAmt.getCoverageDate());
		assertEquals("Program Type", "UF", ufAmt.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", ufAmt.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", ufAmt.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", ufAmt.getMarketplaceGroupPolicyId());
		
		PolicyPaymentTransDTO aptcAmtFeb = paymentTransactions.get(3);
		assertEquals("Payment Amount", new BigDecimal("-53.57"), aptcAmtFeb.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), aptcAmtFeb.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 15, 0, 0), aptcAmtFeb.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 10, aptcAmtFeb.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", aptcAmtFeb.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), aptcAmtFeb.getCoverageDate());
		assertEquals("Program Type", "APTC", aptcAmtFeb.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", aptcAmtFeb.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", aptcAmtFeb.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", aptcAmtFeb.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO csrAmtFeb = paymentTransactions.get(4);
		assertEquals("Payment Amount", new BigDecimal("-26.79"), csrAmtFeb.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), csrAmtFeb.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 15, 0, 0), csrAmtFeb.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 11, csrAmtFeb.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", csrAmtFeb.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), csrAmtFeb.getCoverageDate());
		assertEquals("Program Type", "CSR", csrAmtFeb.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", csrAmtFeb.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", csrAmtFeb.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", csrAmtFeb.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO ufAmtFeb = paymentTransactions.get(5);
		assertEquals("Payment Amount", new BigDecimal("2.81"), ufAmtFeb.getPaymentAmount());
		assertEquals("Total PremiumAmount", new BigDecimal("80.36"), ufAmtFeb.getTotalPremiumAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), ufAmtFeb.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 15, 0, 0), ufAmtFeb.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 12, ufAmtFeb.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", ufAmtFeb.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), ufAmtFeb.getCoverageDate());
		assertEquals("Program Type", "UF", ufAmtFeb.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", ufAmtFeb.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", ufAmtFeb.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", ufAmtFeb.getMarketplaceGroupPolicyId());
	}

	/**
	 * Test FDD Scenario2 - FFM
	 * 
	 * Proration of retroactive payments following a mid-month birth of a child or adoption on an FFM policy.  
	 * This example illustrates the current FFM Change in Circumstance (CIC). Following the termination of 
	 * the first policy and insertion of the Policy version into EPS, RAP would reverse the existing February 
	 * payment and create prorated February payment for the partial month of coverage. When a new Policy (1701) 
	 * is then inserted into EPS to represent the addition of the child, the RAP batch will identify the 
	 * outstanding payment for the partial month of February. Using the Prorated amounts provided on the 
	 * Policy Premium record, RAP must create a prorated payment for the partial month. Payment Coverage 
	 * Start Date and Payment Coverage End Date on the newly created transactions to reflect the partial 
	 * months of coverage for reporting purposes.  The Proration Factor is not stored on the new transactions.  
	 * The Payment Amount on each transaction is taken from the Prorated Payment Amounts provided for 
	 * each corresponding Program in the Policy Premium Record (APTC,CSR).
	 */
	@Test
	public void testCreateRetroPayments_Scenario2_FFM() {
		CodeRecord codeRecord = new CodeRecord("ERC", "2015-02-01 00:00:00", "2015-01-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroPartialMonth_Scenario2_FFM();
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-06-01", "2015-02-15", "2015-12-31");

		RapServiceTestUtil.loadStateConfigMap(policyVersion.getSubscriberStateCd(),
				policyVersion.getPolicyStartDate().getYear(), ProrationType.FFM_PRORATING);
		
		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);

		List<PolicyPaymentTransDTO> paymentTransactions = response.getPolicyPaymentTransactions();


		assertTrue("PolicyPaymentTransactions", CollectionUtils.isNotEmpty(paymentTransactions)); 
		assertEquals("PolicyPaymentTransactions size", 3, paymentTransactions.size());

		PolicyPaymentTransDTO aptcAmt = paymentTransactions.get(0);
		assertEquals("Payment Amount", new BigDecimal("100.00"), aptcAmt.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 15, 0, 0), aptcAmt.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), aptcAmt.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", aptcAmt.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", aptcAmt.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), aptcAmt.getCoverageDate());
		assertEquals("Program Type", "APTC", aptcAmt.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", aptcAmt.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", aptcAmt.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", aptcAmt.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO csrAmt = paymentTransactions.get(1);
		assertEquals("Payment Amount", new BigDecimal("25.00"), csrAmt.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 15, 0, 0), csrAmt.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), csrAmt.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", csrAmt.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", csrAmt.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), csrAmt.getCoverageDate());
		assertEquals("Program Type", "CSR", csrAmt.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", csrAmt.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", csrAmt.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", csrAmt.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO ufAmt = paymentTransactions.get(2);
		assertNull("Payment Amount", ufAmt.getPaymentAmount());
		assertEquals("Total PremiumAmount", new BigDecimal("125.00"), ufAmt.getTotalPremiumAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 15, 0, 0), ufAmt.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), ufAmt.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", ufAmt.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", ufAmt.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), ufAmt.getCoverageDate());
		assertEquals("Program Type", "UF", ufAmt.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", ufAmt.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", ufAmt.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", ufAmt.getMarketplaceGroupPolicyId());
	}

	/**
	 * Test FDD Scenario3 - FFM
	 * 
	 * builds on the previous scenario, and here the policy with a mid-month start date later has a mid-month 
	 * termination. In this instance a new prorated period for 4/1-4/15 is created in FFM IPP and the entire 
	 * policy version is ingested via ERL.  The scenario illustrates how RAP would manage the mid-month 
	 * termination of a policy that already has prorated mid-month transacitons from an earlier policy version. 
	 * Following of the insertion of a new policy version into EPS, RAP would reverse the existing April 
	 * payment and create a prorated April payment.   Payment Coverage Start Date and Payment Coverage 
	 * End Date on the newly created transactions reflect the partial months of coverage for reporting 
	 * purposes.  The Proration Factor is not stored on the new prorated transaction and the prorated 
	 * transaction is taken from the Prorated Payment Amounts provided for each corresponding Program 
	 * in the Policy Premium Record.
	 */
	@Test
	public void testCreateRetroPayments_Scenario3_FFM() {
		CodeRecord codeRecord = new CodeRecord("ERC", "2015-04-01 00:00:00", "2015-03-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroPartialMonth_Scenario3_FFM();
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-06-01", "2015-02-15", "2015-04-15");

		RapServiceTestUtil.loadStateConfigMap(policyVersion.getSubscriberStateCd(),
				policyVersion.getPolicyStartDate().getYear(), ProrationType.FFM_PRORATING);
		
		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);

		List<PolicyPaymentTransDTO> paymentTransactions = response.getPolicyPaymentTransactions();


		assertTrue("PolicyPaymentTransactions", CollectionUtils.isNotEmpty(paymentTransactions)); 
		assertEquals("PolicyPaymentTransactions size", 6, paymentTransactions.size());

		PolicyPaymentTransDTO aptcAmt = paymentTransactions.get(0);
		assertEquals("Payment Amount", new BigDecimal("-100.00"), aptcAmt.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 4, 1, 0, 0), aptcAmt.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 4, 30, 0, 0), aptcAmt.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 7, aptcAmt.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", aptcAmt.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-04-01"), aptcAmt.getCoverageDate());
		assertEquals("Program Type", "APTC", aptcAmt.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", aptcAmt.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", aptcAmt.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", aptcAmt.getMarketplaceGroupPolicyId());

		aptcAmt = paymentTransactions.get(1);
		assertEquals("Payment Amount", new BigDecimal("50.00"), aptcAmt.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 4, 1, 0, 0), aptcAmt.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 4, 15, 0, 0), aptcAmt.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", aptcAmt.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", aptcAmt.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-04-01"), aptcAmt.getCoverageDate());
		assertEquals("Program Type", "APTC", aptcAmt.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", aptcAmt.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", aptcAmt.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", aptcAmt.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO csrAmt = paymentTransactions.get(2);
		assertEquals("Payment Amount", new BigDecimal("-50.00"), csrAmt.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 4, 1, 0, 0), csrAmt.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 4, 30, 0, 0), csrAmt.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 8, csrAmt.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", csrAmt.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-04-01"), csrAmt.getCoverageDate());
		assertEquals("Program Type", "CSR", csrAmt.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", csrAmt.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", csrAmt.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", csrAmt.getMarketplaceGroupPolicyId());

		csrAmt = paymentTransactions.get(3);
		assertEquals("Payment Amount", new BigDecimal("25.00"), csrAmt.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 4, 1, 0, 0), csrAmt.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 4, 15, 0, 0), csrAmt.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", csrAmt.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", csrAmt.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-04-01"), csrAmt.getCoverageDate());
		assertEquals("Program Type", "CSR", csrAmt.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", csrAmt.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", csrAmt.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", csrAmt.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO ufAmt = paymentTransactions.get(4);
		assertEquals("Payment Amount", new BigDecimal("5.25"), ufAmt.getPaymentAmount());
		assertEquals("Total PremiumAmount", new BigDecimal("150.00"), ufAmt.getTotalPremiumAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 4, 1, 0, 0), ufAmt.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 4, 30, 0, 0), ufAmt.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 9, ufAmt.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", ufAmt.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-04-01"), ufAmt.getCoverageDate());
		assertEquals("Program Type", "UF", ufAmt.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", ufAmt.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", ufAmt.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", ufAmt.getMarketplaceGroupPolicyId());

		ufAmt = paymentTransactions.get(5);
		assertNull("Payment Amount", ufAmt.getPaymentAmount());
		assertEquals("Total PremiumAmount", new BigDecimal("75.00"), ufAmt.getTotalPremiumAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 4, 1, 0, 0), ufAmt.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 4, 15, 0, 0), ufAmt.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", ufAmt.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", ufAmt.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-04-01"), ufAmt.getCoverageDate());
		assertEquals("Program Type", "UF", ufAmt.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", ufAmt.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", ufAmt.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", ufAmt.getMarketplaceGroupPolicyId());

	}

	/**
	 * Test FDD Scenario3 for APTC Payments 
	 * 
	 * Reflects a future state scenario in which FFM might support maintenance transactions. 
	 * This scenario is also representative of current SBM Inbound processing of maintenance transactions 
	 * with financial changes. The scenario illustrates how RAP would manage the proration of retroactive 
	 * payments for the mid-month birth of a child or adoption.  Following the maintenance transaction 
	 * and insertion of a new Policy version into EPS, RAP would reverse the existing February payment 
	 * and create prorated February payments for both partial periods of coverage. Payment Coverage 
	 * Start Date and Payment Coverage End Date on the newly created transactions reflect the partial 
	 * months of coverage for reporting purposes.  The Proration Factor stored on the new transactions 
	 * reflects the ratio of the number of days of coverage to the number.  The financial outcome is the 
	 * same as the scenario reflected in Figure 5 above, except that all transactions occur on the same 
	 * policy.
	 */
	@Test
	public void testCreateAptcPayments_Scenario3() {
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		replay(mockRapDao);

		ReflectionTestUtils.setField(rapProcesssingServiceTest, "reversalRefIds", new ArrayList<Long>());
		ReflectionTestUtils.setField(rapProcesssingServiceTest, "reversedTransIds", new ArrayList<Long>());

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-06-01", "2015-01-01", "2015-12-31");

		RapServiceTestUtil.loadStateConfigMap(policyVersion.getSubscriberStateCd(),
				policyVersion.getPolicyStartDate().getYear(), ProrationType.FFM_PRORATING);
		
		DateTime coverageDt = new DateTime("2015-02-01");

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroPartialMonth_Scenario3_APTC();

		List<PolicyPaymentTransDTO>  paymentTransactions = new ArrayList<PolicyPaymentTransDTO>();

		ReflectionTestUtils.invokeMethod(rapProcesssingServiceTest, "createAptcPayments",
				policyVersion, coverageDt, paymentTransactions,
				policyDetailDTO.getPolicyPayments(), policyDetailDTO.getPolicyPremiums(), ProrationType.FFM_PRORATING);

		assertTrue("PolicyPaymentTransactions", CollectionUtils.isNotEmpty(paymentTransactions)); 
		assertEquals("PolicyPaymentTransactions size", 3, paymentTransactions.size());

		assertEquals("Payment Transaction values comparison", 
				new BigDecimal("-100.00"), paymentTransactions.get(0).getPaymentAmount());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 2, 1, 0, 0), paymentTransactions.get(0).getPaymentCoverageStartDate());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 2, 28, 0, 0), paymentTransactions.get(0).getPaymentCoverageEndDate());
		assertEquals("Payment Transaction values comparison", 
				1, paymentTransactions.get(0).getParentPolicyPaymentTransId().longValue());

		assertEquals("Payment Transaction values comparison", 
				new BigDecimal("53.57"), paymentTransactions.get(1).getPaymentAmount());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 2, 1, 0, 0), paymentTransactions.get(1).getPaymentCoverageStartDate());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 2, 15, 0, 0), paymentTransactions.get(1).getPaymentCoverageEndDate());
		assertNull("Payment Transaction values comparison", 
				paymentTransactions.get(1).getParentPolicyPaymentTransId());

		assertEquals("Payment Transaction values comparison", 
				new BigDecimal("92.86"), paymentTransactions.get(2).getPaymentAmount());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 2, 16, 0, 0), paymentTransactions.get(2).getPaymentCoverageStartDate());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 2, 28, 0, 0), paymentTransactions.get(2).getPaymentCoverageEndDate());
		assertNull("Payment Transaction values comparison", 
				paymentTransactions.get(2).getParentPolicyPaymentTransId());

	}

	/**
	 * Test FDD Scenario3
	 * 
	 * Reflects a future state scenario in which FFM might support maintenance transactions. 
	 * This scenario is also representative of current SBM Inbound processing of maintenance transactions 
	 * with financial changes. The scenario illustrates how RAP would manage the proration of retroactive 
	 * payments for the mid-month birth of a child or adoption.  Following the maintenance transaction 
	 * and insertion of a new Policy version into EPS, RAP would reverse the existing February payment 
	 * and create prorated February payments for both partial periods of coverage. Payment Coverage 
	 * Start Date and Payment Coverage End Date on the newly created transactions reflect the partial 
	 * months of coverage for reporting purposes.  The Proration Factor stored on the new transactions 
	 * reflects the ratio of the number of days of coverage to the number.  The financial outcome is the 
	 * same as the scenario reflected in Figure 5 above, except that all transactions occur on the same 
	 * policy.
	 */
	@Test
	public void testCreateRetroPayments_Scenario3() {
		CodeRecord codeRecord = new CodeRecord("ERC", "2015-02-01 00:00:00", "2015-01-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroPartialMonth_Scenario3();
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-06-01", "2015-01-01", "2015-12-31");

		RapServiceTestUtil.loadStateConfigMap(policyVersion.getSubscriberStateCd(),
				policyVersion.getPolicyStartDate().getYear(), ProrationType.FFM_PRORATING);
		
		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);

		List<PolicyPaymentTransDTO> paymentTransactions = response.getPolicyPaymentTransactions();


		assertTrue("PolicyPaymentTransactions", CollectionUtils.isNotEmpty(paymentTransactions)); 
		assertEquals("PolicyPaymentTransactions size", 9, paymentTransactions.size());

		PolicyPaymentTransDTO aptcFebPrev = paymentTransactions.get(0);
		assertEquals("Payment Amount", new BigDecimal("-100.00"), aptcFebPrev.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), aptcFebPrev.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), aptcFebPrev.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 4, aptcFebPrev.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", aptcFebPrev.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), aptcFebPrev.getCoverageDate());
		assertEquals("Program Type", "APTC", aptcFebPrev.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", aptcFebPrev.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", aptcFebPrev.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", aptcFebPrev.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO aptcFeb1 = paymentTransactions.get(1);
		assertEquals("Payment Amount", new BigDecimal("53.57"), aptcFeb1.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), aptcFeb1.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 15, 0, 0), aptcFeb1.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", aptcFeb1.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", aptcFeb1.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), aptcFeb1.getCoverageDate());
		assertEquals("Program Type", "APTC", aptcFeb1.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", aptcFeb1.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", aptcFeb1.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", aptcFeb1.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO aptcFeb2 = paymentTransactions.get(2);
		assertEquals("Payment Amount", new BigDecimal("92.86"), aptcFeb2.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 16, 0, 0), aptcFeb2.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), aptcFeb2.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", aptcFeb2.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", aptcFeb2.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), aptcFeb2.getCoverageDate());
		assertEquals("Program Type", "APTC", aptcFeb2.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", aptcFeb2.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", aptcFeb2.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", aptcFeb2.getMarketplaceGroupPolicyId());


		PolicyPaymentTransDTO csrFebPrev = paymentTransactions.get(3);
		assertEquals("Payment Amount", new BigDecimal("-50.00"), csrFebPrev.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), csrFebPrev.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), csrFebPrev.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 5, csrFebPrev.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", csrFebPrev.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), csrFebPrev.getCoverageDate());
		assertEquals("Program Type", "CSR", csrFebPrev.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", csrFebPrev.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", csrFebPrev.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", csrFebPrev.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO csrFeb1 = paymentTransactions.get(4);
		assertEquals("Payment Amount", new BigDecimal("26.79"), csrFeb1.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), csrFeb1.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 15, 0, 0), csrFeb1.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", csrFeb1.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", csrFeb1.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), csrFeb1.getCoverageDate());
		assertEquals("Program Type", "CSR", csrFeb1.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", csrFeb1.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", csrFeb1.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", csrFeb1.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO csrFeb2 = paymentTransactions.get(5);
		assertEquals("Payment Amount", new BigDecimal("23.21"), csrFeb2.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 16, 0, 0), csrFeb2.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), csrFeb2.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", csrFeb2.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", csrFeb2.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), csrFeb2.getCoverageDate());
		assertEquals("Program Type", "CSR", csrFeb2.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", csrFeb2.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", csrFeb2.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", csrFeb2.getMarketplaceGroupPolicyId());


		PolicyPaymentTransDTO ufFebPrev = paymentTransactions.get(6);
		assertEquals("Payment Amount", new BigDecimal("5.25"), ufFebPrev.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), ufFebPrev.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), ufFebPrev.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 6, ufFebPrev.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", ufFebPrev.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), ufFebPrev.getCoverageDate());
		assertEquals("Program Type", "UF", ufFebPrev.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", ufFebPrev.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", ufFebPrev.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", ufFebPrev.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO ufFeb1 = paymentTransactions.get(7);
		assertNull("Payment Amount", ufFeb1.getPaymentAmount());
		assertEquals("TP Amount", new BigDecimal("80.36"), ufFeb1.getTotalPremiumAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), ufFeb1.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 15, 0, 0), ufFeb1.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", ufFeb1.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", ufFeb1.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), ufFeb1.getCoverageDate());
		assertEquals("Program Type", "UF", ufFeb1.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", ufFeb1.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", ufFeb1.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", ufFeb1.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO ufFeb2 = paymentTransactions.get(8);
		assertNull("Payment Amount", ufFeb2.getPaymentAmount());
		assertEquals("TP Amount", new BigDecimal("116.07"), ufFeb2.getTotalPremiumAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 16, 0, 0), ufFeb2.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), ufFeb2.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", ufFeb2.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", ufFeb2.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), ufFeb2.getCoverageDate());
		assertEquals("Program Type", "UF", ufFeb2.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", ufFeb2.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", ufFeb2.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", ufFeb2.getMarketplaceGroupPolicyId());
	}

	/**
	 * Test FDD Scenario4 for APTC Payments 
	 * 
	 * Reflects a hypothetical scenario in which the policy above (1800) has a subsequent data correction 
	 * processed through the Batch Update Utility (BUU), which performs a historical correction to the 
	 * financial amounts for the policy. This scenario is also representative of how Maintenance 
	 * Transactions are performed by SBM Inbound Processing (Financial Changes) which replace historical 
	 * Policy Premium records. The scenario illustrates how RAP would correct retroactive payments for 
	 * the February coverage month. Following the maintenance and insertion of a new Policy version into 
	 * EPS, RAP would evaluate the Policy Premium record against existing policy payment transactions. 
	 * The existing February payments are now incorrectly prorated with improper amounts based on the new 
	 * policy premium.  RAP must therefore reverse the existing partial February payments and create a 
	 * single corrected February payment. Payment Coverage Start Date and Payment Coverage End Date on 
	 * the newly created transactions reflect the partial months of coverage for reporting purposes.
	 */
	@Test
	public void testCreateAptcPayments_Scenario4() {
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		replay(mockRapDao);

		ReflectionTestUtils.setField(rapProcesssingServiceTest, "reversalRefIds", new ArrayList<Long>(Arrays.asList(1L)));
		ReflectionTestUtils.setField(rapProcesssingServiceTest, "reversedTransIds", new ArrayList<Long>());

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-06-01", "2015-01-01", "2015-12-31");

		RapServiceTestUtil.loadStateConfigMap(policyVersion.getSubscriberStateCd(),
				policyVersion.getPolicyStartDate().getYear(), ProrationType.FFM_PRORATING);
		
		DateTime coverageDt = new DateTime("2015-02-01");

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroPartialMonth_Scenario4_APTC();

		List<PolicyPaymentTransDTO>  paymentTransactions = new ArrayList<PolicyPaymentTransDTO>();

		ReflectionTestUtils.invokeMethod(rapProcesssingServiceTest, "createAptcPayments",
				policyVersion, coverageDt, paymentTransactions,
				policyDetailDTO.getPolicyPayments(), policyDetailDTO.getPolicyPremiums(), ProrationType.FFM_PRORATING);

		assertTrue("PolicyPaymentTransactions", CollectionUtils.isNotEmpty(paymentTransactions)); 
		assertEquals("PolicyPaymentTransactions size", 3, paymentTransactions.size());

		assertEquals("Payment Transaction values comparison", 
				new BigDecimal("-53.57"), paymentTransactions.get(0).getPaymentAmount());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 2, 1, 0, 0), paymentTransactions.get(0).getPaymentCoverageStartDate());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 2, 15, 0, 0), paymentTransactions.get(0).getPaymentCoverageEndDate());
		assertEquals("Payment Transaction values comparison", 
				3, paymentTransactions.get(0).getParentPolicyPaymentTransId().longValue());

		assertEquals("Payment Transaction values comparison", 
				new BigDecimal("-92.86"), paymentTransactions.get(1).getPaymentAmount());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 2, 16, 0, 0), paymentTransactions.get(1).getPaymentCoverageStartDate());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 2, 28, 0, 0), paymentTransactions.get(1).getPaymentCoverageEndDate());
		assertEquals("Payment Transaction values comparison", 
				4, paymentTransactions.get(1).getParentPolicyPaymentTransId().longValue());

		assertEquals("Payment Transaction values comparison", 
				new BigDecimal("100.00"), paymentTransactions.get(2).getPaymentAmount());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 2, 1, 0, 0), paymentTransactions.get(2).getPaymentCoverageStartDate());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 2, 28, 0, 0), paymentTransactions.get(2).getPaymentCoverageEndDate());
		assertNull("Payment Transaction values comparison", 
				paymentTransactions.get(2).getParentPolicyPaymentTransId());

	}

	/**
	 * Test FDD Scenario4
	 * 
	 * Reflects a hypothetical scenario in which the policy above (1800) has a subsequent data correction 
	 * processed through the Batch Update Utility (BUU), which performs a historical correction to the 
	 * financial amounts for the policy. This scenario is also representative of how Maintenance 
	 * Transactions are performed by SBM Inbound Processing (Financial Changes) which replace historical 
	 * Policy Premium records. The scenario illustrates how RAP would correct retroactive payments for 
	 * the February coverage month. Following the maintenance and insertion of a new Policy version into 
	 * EPS, RAP would evaluate the Policy Premium record against existing policy payment transactions. 
	 * The existing February payments are now incorrectly prorated with improper amounts based on the new 
	 * policy premium.  RAP must therefore reverse the existing partial February payments and create a 
	 * single corrected February payment. Payment Coverage Start Date and Payment Coverage End Date on 
	 * the newly created transactions reflect the partial months of coverage for reporting purposes.
	 */
	@Test
	public void testCreateRetroPayments_Scenario4() {
		CodeRecord codeRecord = new CodeRecord("ERC", "2015-02-01 00:00:00", "2015-01-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroPartialMonth_Scenario4();
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-06-01", "2015-01-01", "2015-12-31");

		RapServiceTestUtil.loadStateConfigMap(policyVersion.getSubscriberStateCd(),
				policyVersion.getPolicyStartDate().getYear(), ProrationType.FFM_PRORATING);
		
		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);

		List<PolicyPaymentTransDTO> paymentTransactions = response.getPolicyPaymentTransactions();


		assertTrue("PolicyPaymentTransactions", CollectionUtils.isNotEmpty(paymentTransactions)); 
		assertEquals("PolicyPaymentTransactions size", 9, paymentTransactions.size());

		PolicyPaymentTransDTO aptcFebPrev1 = paymentTransactions.get(0);
		assertEquals("Payment Amount", new BigDecimal("-53.57"), aptcFebPrev1.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), aptcFebPrev1.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 15, 0, 0), aptcFebPrev1.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 10, aptcFebPrev1.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", aptcFebPrev1.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), aptcFebPrev1.getCoverageDate());
		assertEquals("Program Type", "APTC", aptcFebPrev1.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", aptcFebPrev1.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", aptcFebPrev1.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", aptcFebPrev1.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO aptcFebPrev2 = paymentTransactions.get(1);
		assertEquals("Payment Amount", new BigDecimal("-92.86"), aptcFebPrev2.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 16, 0, 0), aptcFebPrev2.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), aptcFebPrev2.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 13, aptcFebPrev2.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", aptcFebPrev2.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), aptcFebPrev2.getCoverageDate());
		assertEquals("Program Type", "APTC", aptcFebPrev2.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", aptcFebPrev2.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", aptcFebPrev2.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", aptcFebPrev2.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO aptcFeb = paymentTransactions.get(2);
		assertEquals("Payment Amount", new BigDecimal("100.00"), aptcFeb.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), aptcFeb.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), aptcFeb.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", aptcFeb.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", aptcFeb.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), aptcFeb.getCoverageDate());
		assertEquals("Program Type", "APTC", aptcFeb.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", aptcFeb.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", aptcFeb.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", aptcFeb.getMarketplaceGroupPolicyId());


		PolicyPaymentTransDTO csrFebPrev1 = paymentTransactions.get(3);
		assertEquals("Payment Amount", new BigDecimal("-26.79"), csrFebPrev1.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), csrFebPrev1.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 15, 0, 0), csrFebPrev1.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 11, csrFebPrev1.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", csrFebPrev1.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), csrFebPrev1.getCoverageDate());
		assertEquals("Program Type", "CSR", csrFebPrev1.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", csrFebPrev1.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", csrFebPrev1.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", csrFebPrev1.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO csrFebPrev2 = paymentTransactions.get(4);
		assertEquals("Payment Amount", new BigDecimal("-23.21"), csrFebPrev2.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 16, 0, 0), csrFebPrev2.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), csrFebPrev2.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 14, csrFebPrev2.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", csrFebPrev2.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), csrFebPrev2.getCoverageDate());
		assertEquals("Program Type", "CSR", csrFebPrev2.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", csrFebPrev2.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", csrFebPrev2.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", csrFebPrev2.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO csrFeb = paymentTransactions.get(5);
		assertEquals("Payment Amount", new BigDecimal("50.00"), csrFeb.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), csrFeb.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), csrFeb.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", csrFeb.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", csrFeb.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), csrFeb.getCoverageDate());
		assertEquals("Program Type", "CSR", csrFeb.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", csrFeb.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", csrFeb.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", csrFeb.getMarketplaceGroupPolicyId());


		PolicyPaymentTransDTO ufFebPrev1 = paymentTransactions.get(6);
		assertEquals("Payment Amount", new BigDecimal("2.81"), ufFebPrev1.getPaymentAmount());
		assertEquals("TP Amount", new BigDecimal("150.00"), ufFebPrev1.getTotalPremiumAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), ufFebPrev1.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 15, 0, 0), ufFebPrev1.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 12, ufFebPrev1.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", ufFebPrev1.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), ufFebPrev1.getCoverageDate());
		assertEquals("Program Type", "UF", ufFebPrev1.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", ufFebPrev1.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", ufFebPrev1.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", ufFebPrev1.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO ufFebPrev2 = paymentTransactions.get(7);
		assertEquals("Payment Amount", new BigDecimal("4.06"), ufFebPrev2.getPaymentAmount());
		assertEquals("TP Amount", new BigDecimal("250.00"), ufFebPrev2.getTotalPremiumAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 16, 0, 0), ufFebPrev2.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), ufFebPrev2.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 15, ufFebPrev2.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", ufFebPrev2.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), ufFebPrev2.getCoverageDate());
		assertEquals("Program Type", "UF", ufFebPrev2.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", ufFebPrev2.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", ufFebPrev2.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", ufFebPrev2.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO ufFeb = paymentTransactions.get(8);
		assertNull("Payment Amount", ufFeb.getPaymentAmount());
		assertEquals("TP Amount", new BigDecimal("150.00"), ufFeb.getTotalPremiumAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), ufFeb.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), ufFeb.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", ufFeb.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", ufFeb.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), ufFeb.getCoverageDate());
		assertEquals("Program Type", "UF", ufFeb.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", ufFeb.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", ufFeb.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", ufFeb.getMarketplaceGroupPolicyId());
	}

	/**
	 * Test FDD Scenario5 for APTC Payments 
	 * 
	 * Policy with data correction is processed and historical correction to existing financial amounts 
	 * are performed. This scenario illustrates how RAP would correct prorated retroactive payments for 
	 * the February coverage months with new prorated amounts. Following the maintenance transaction and 
	 * insertion of a new Policy version into EPS, RAP would reverse the existing February payments and 
	 * create prorated February payments for both partial periods of coverage. Payment Coverage Start Date 
	 * and Payment Coverage End Date on the newly created transactions reflect the partial months of 
	 * coverage for reporting purposes.  The Proration Factor stored on the new transactions reflects the 
	 * ratio of the number of days of coverage to the number.
	 */
	@Test
	public void testCreateAptcPayments_Scenario5() {
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		replay(mockRapDao);
		ReflectionTestUtils.setField(rapProcesssingServiceTest, "reversalRefIds", new ArrayList<Long>());
		ReflectionTestUtils.setField(rapProcesssingServiceTest, "reversedTransIds", new ArrayList<Long>());

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-06-01", "2015-01-01", "2015-12-31");

		RapServiceTestUtil.loadStateConfigMap(policyVersion.getSubscriberStateCd(),
				policyVersion.getPolicyStartDate().getYear(), ProrationType.FFM_PRORATING);
		
		DateTime coverageDt = new DateTime("2015-02-01");

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroPartialMonth_Scenario5_APTC();

		List<PolicyPaymentTransDTO>  paymentTransactions = new ArrayList<PolicyPaymentTransDTO>();

		ReflectionTestUtils.invokeMethod(rapProcesssingServiceTest, "createAptcPayments",
				policyVersion, coverageDt, paymentTransactions,
				policyDetailDTO.getPolicyPayments(), policyDetailDTO.getPolicyPremiums(), ProrationType.FFM_PRORATING);

		assertTrue("PolicyPaymentTransactions", CollectionUtils.isNotEmpty(paymentTransactions)); 
		assertEquals("PolicyPaymentTransactions size", 4, paymentTransactions.size());

		assertEquals("Payment Transaction values comparison", 
				new BigDecimal("-35.71"), paymentTransactions.get(0).getPaymentAmount());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 2, 1, 0, 0), paymentTransactions.get(0).getPaymentCoverageStartDate());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 2, 10, 0, 0), paymentTransactions.get(0).getPaymentCoverageEndDate());
		assertEquals("Payment Transaction values comparison", 
				1, paymentTransactions.get(0).getParentPolicyPaymentTransId().longValue());

		assertEquals("Payment Transaction values comparison", 
				new BigDecimal("-128.57"), paymentTransactions.get(1).getPaymentAmount());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 2, 11, 0, 0), paymentTransactions.get(1).getPaymentCoverageStartDate());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 2, 28, 0, 0), paymentTransactions.get(1).getPaymentCoverageEndDate());
		assertEquals("Payment Transaction values comparison", 
				2, paymentTransactions.get(1).getParentPolicyPaymentTransId().longValue());

		assertEquals("Payment Transaction values comparison", 
				new BigDecimal("32.14"), paymentTransactions.get(2).getPaymentAmount());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 2, 1, 0, 0), paymentTransactions.get(2).getPaymentCoverageStartDate());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 2, 9, 0, 0), paymentTransactions.get(2).getPaymentCoverageEndDate());
		assertNull("Payment Transaction values comparison", 
				paymentTransactions.get(2).getParentPolicyPaymentTransId());

		assertEquals("Payment Transaction values comparison", 
				new BigDecimal("135.71"), paymentTransactions.get(3).getPaymentAmount());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 2, 10, 0, 0), paymentTransactions.get(3).getPaymentCoverageStartDate());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 2, 28, 0, 0), paymentTransactions.get(3).getPaymentCoverageEndDate());
		assertNull("Payment Transaction values comparison", 
				paymentTransactions.get(3).getParentPolicyPaymentTransId());

	}

	/**
	 * Test FDD Scenario5
	 * 
	 * Policy with data correction is processed and historical correction to existing financial amounts 
	 * are performed. This scenario illustrates how RAP would correct prorated retroactive payments for 
	 * the February coverage months with new prorated amounts. Following the maintenance transaction and 
	 * insertion of a new Policy version into EPS, RAP would reverse the existing February payments and 
	 * create prorated February payments for both partial periods of coverage. Payment Coverage Start Date 
	 * and Payment Coverage End Date on the newly created transactions reflect the partial months of 
	 * coverage for reporting purposes. The Proration Factor stored on the new transactions reflects the 
	 * ratio of the number of days of coverage to the number.
	 */
	@Test
	public void testCreateRetroPayments_Scenario5() {
		CodeRecord codeRecord = new CodeRecord("ERC", "2015-02-01 00:00:00", "2015-01-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroPartialMonth_Scenario5();
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-06-01", "2015-01-01", "2015-12-31");

		RapServiceTestUtil.loadStateConfigMap(policyVersion.getSubscriberStateCd(),
				policyVersion.getPolicyStartDate().getYear(), ProrationType.FFM_PRORATING);
		
		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);

		List<PolicyPaymentTransDTO> paymentTransactions = response.getPolicyPaymentTransactions();


		assertTrue("PolicyPaymentTransactions", CollectionUtils.isNotEmpty(paymentTransactions)); 
		assertEquals("PolicyPaymentTransactions size", 12, paymentTransactions.size());

		PolicyPaymentTransDTO aptcFebPrev1 = paymentTransactions.get(0);
		assertEquals("Payment Amount", new BigDecimal("-35.71"), aptcFebPrev1.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), aptcFebPrev1.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 10, 0, 0), aptcFebPrev1.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 4, aptcFebPrev1.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", aptcFebPrev1.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), aptcFebPrev1.getCoverageDate());
		assertEquals("Program Type", "APTC", aptcFebPrev1.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", aptcFebPrev1.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", aptcFebPrev1.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", aptcFebPrev1.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO aptcFebPrev2 = paymentTransactions.get(1);
		assertEquals("Payment Amount", new BigDecimal("-128.57"), aptcFebPrev2.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 11, 0, 0), aptcFebPrev2.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), aptcFebPrev2.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 7, aptcFebPrev2.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", aptcFebPrev2.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), aptcFebPrev2.getCoverageDate());
		assertEquals("Program Type", "APTC", aptcFebPrev2.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", aptcFebPrev2.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", aptcFebPrev2.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", aptcFebPrev2.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO aptcFeb1 = paymentTransactions.get(2);
		assertEquals("Payment Amount", new BigDecimal("32.14"), aptcFeb1.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), aptcFeb1.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 9, 0, 0), aptcFeb1.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", aptcFeb1.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", aptcFeb1.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), aptcFeb1.getCoverageDate());
		assertEquals("Program Type", "APTC", aptcFeb1.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", aptcFeb1.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", aptcFeb1.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", aptcFeb1.getMarketplaceGroupPolicyId());


		PolicyPaymentTransDTO aptcFeb2 = paymentTransactions.get(3);
		assertEquals("Payment Amount", new BigDecimal("135.71"), aptcFeb2.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 10, 0, 0), aptcFeb2.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), aptcFeb2.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", aptcFeb2.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", aptcFeb2.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), aptcFeb2.getCoverageDate());
		assertEquals("Program Type", "APTC", aptcFeb2.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", aptcFeb2.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", aptcFeb2.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", aptcFeb2.getMarketplaceGroupPolicyId());


		PolicyPaymentTransDTO csrFebPrev1 = paymentTransactions.get(4);
		assertEquals("Payment Amount", new BigDecimal("-17.86"), csrFebPrev1.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), csrFebPrev1.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 10, 0, 0), csrFebPrev1.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 5, csrFebPrev1.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", csrFebPrev1.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), csrFebPrev1.getCoverageDate());
		assertEquals("Program Type", "CSR", csrFebPrev1.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", csrFebPrev1.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", csrFebPrev1.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", csrFebPrev1.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO csrFebPrev2 = paymentTransactions.get(5);
		assertEquals("Payment Amount", new BigDecimal("-32.14"), csrFebPrev2.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 11, 0, 0), csrFebPrev2.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), csrFebPrev2.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 8, csrFebPrev2.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", csrFebPrev2.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), csrFebPrev2.getCoverageDate());
		assertEquals("Program Type", "CSR", csrFebPrev2.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", csrFebPrev2.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", csrFebPrev2.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", csrFebPrev2.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO csrFeb1 = paymentTransactions.get(6);
		assertEquals("Payment Amount", new BigDecimal("16.07"), csrFeb1.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), csrFeb1.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 9, 0, 0), csrFeb1.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", csrFeb1.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", csrFeb1.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), csrFeb1.getCoverageDate());
		assertEquals("Program Type", "CSR", csrFeb1.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", csrFeb1.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", csrFeb1.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", csrFeb1.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO csrFeb2 = paymentTransactions.get(7);
		assertEquals("Payment Amount", new BigDecimal("33.93"), csrFeb2.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 10, 0, 0), csrFeb2.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), csrFeb2.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", csrFeb2.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", csrFeb2.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), csrFeb2.getCoverageDate());
		assertEquals("Program Type", "CSR", csrFeb2.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", csrFeb2.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", csrFeb2.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", csrFeb2.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO ufFebPrev1 = paymentTransactions.get(8);
		assertEquals("Payment Amount", new BigDecimal("1.88"), ufFebPrev1.getPaymentAmount());
		assertEquals("TP Amount", new BigDecimal("150"), ufFebPrev1.getTotalPremiumAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), ufFebPrev1.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 10, 0, 0), ufFebPrev1.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 6, ufFebPrev1.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", ufFebPrev1.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), ufFebPrev1.getCoverageDate());
		assertEquals("Program Type", "UF", ufFebPrev1.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", ufFebPrev1.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", ufFebPrev1.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", ufFebPrev1.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO ufFebPrev2 = paymentTransactions.get(9);
		assertEquals("Payment Amount", new BigDecimal("5.63"), ufFebPrev2.getPaymentAmount());
		assertEquals("TP Amount", new BigDecimal("250"), ufFebPrev2.getTotalPremiumAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 11, 0, 0), ufFebPrev2.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), ufFebPrev2.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 9, ufFebPrev2.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", ufFebPrev2.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), ufFebPrev2.getCoverageDate());
		assertEquals("Program Type", "UF", ufFebPrev2.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", ufFebPrev2.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", ufFebPrev2.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", ufFebPrev2.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO ufFeb1 = paymentTransactions.get(10);
		assertNull("Payment Amount", ufFeb1.getPaymentAmount());
		assertEquals("TP Amount", new BigDecimal("48.21"), ufFeb1.getTotalPremiumAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), ufFeb1.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 9, 0, 0), ufFeb1.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", ufFeb1.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", ufFeb1.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), ufFeb1.getCoverageDate());
		assertEquals("Program Type", "UF", ufFeb1.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", ufFeb1.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", ufFeb1.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", ufFeb1.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO ufFeb2 = paymentTransactions.get(11);
		assertNull("Payment Amount", ufFeb2.getPaymentAmount());
		assertEquals("TP Amount", new BigDecimal("169.64"), ufFeb2.getTotalPremiumAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 10, 0, 0), ufFeb2.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), ufFeb2.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", ufFeb2.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", ufFeb2.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), ufFeb2.getCoverageDate());
		assertEquals("Program Type", "UF", ufFeb2.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", ufFeb2.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", ufFeb2.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", ufFeb2.getMarketplaceGroupPolicyId());
	}

	/**
	 * Test FDD Scenario6 for APTC Payments 
	 * 
	 * A policy with data correction is processed and historical correction to existing financial amounts 
	 * are performed. This scenario illustrates how RAP would correct prorated retroactive payments for 
	 * the February coverage months from two date ranges to three date ranges. Following the maintenance 
	 * transaction and insertion of a new Policy version into EPS, RAP would reverse all existing February 
	 * payments and create prorated February payments for the new partial periods of coverage. 
	 * Payment Coverage Start Date and Payment Coverage End Date on the newly created transactions reflect 
	 * the partial months of coverage for reporting purposes.  The Proration Factor stored on the new 
	 * transactions reflects the ratio of the number of days of coverage to the number.
	 */
	@Test
	public void testCreateAptcPayments_Scenario6() {
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		replay(mockRapDao);

		ReflectionTestUtils.setField(rapProcesssingServiceTest, "reversalRefIds", new ArrayList<Long>());
		ReflectionTestUtils.setField(rapProcesssingServiceTest, "reversedTransIds", new ArrayList<Long>());

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-06-01", "2015-01-01", "2015-12-31");

		RapServiceTestUtil.loadStateConfigMap(policyVersion.getSubscriberStateCd(),
				policyVersion.getPolicyStartDate().getYear(), ProrationType.FFM_PRORATING);
		
		DateTime coverageDt = new DateTime("2015-02-01");

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroPartialMonth_Scenario6_APTC();

		List<PolicyPaymentTransDTO>  paymentTransactions = new ArrayList<PolicyPaymentTransDTO>();

		ReflectionTestUtils.invokeMethod(rapProcesssingServiceTest, "createAptcPayments",
				policyVersion, coverageDt, paymentTransactions,
				policyDetailDTO.getPolicyPayments(), policyDetailDTO.getPolicyPremiums(), ProrationType.FFM_PRORATING);

		assertTrue("PolicyPaymentTransactions", CollectionUtils.isNotEmpty(paymentTransactions)); 
		assertEquals("PolicyPaymentTransactions size", 3, paymentTransactions.size());

		assertEquals("Payment Transaction values comparison", 
				new BigDecimal("-128.57"), paymentTransactions.get(0).getPaymentAmount());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 2, 11, 0, 0), paymentTransactions.get(0).getPaymentCoverageStartDate());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 2, 28, 0, 0), paymentTransactions.get(0).getPaymentCoverageEndDate());
		assertEquals("Payment Transaction values comparison", 
				2, paymentTransactions.get(0).getParentPolicyPaymentTransId().longValue());

		assertEquals("Payment Transaction values comparison", 
				new BigDecimal("17.86"), paymentTransactions.get(1).getPaymentAmount());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 2, 11, 0, 0), paymentTransactions.get(1).getPaymentCoverageStartDate());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 2, 15, 0, 0), paymentTransactions.get(1).getPaymentCoverageEndDate());
		assertNull("Payment Transaction values comparison", 
				paymentTransactions.get(1).getParentPolicyPaymentTransId());

		assertEquals("Payment Transaction values comparison", 
				new BigDecimal("92.86"), paymentTransactions.get(2).getPaymentAmount());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 2, 16, 0, 0), paymentTransactions.get(2).getPaymentCoverageStartDate());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 2, 28, 0, 0), paymentTransactions.get(2).getPaymentCoverageEndDate());
		assertNull("Payment Transaction values comparison", 
				paymentTransactions.get(2).getParentPolicyPaymentTransId());

	}

	/**
	 * Test FDD Scenario6
	 * 
	 * A policy with data correction is processed and historical correction to existing financial amounts 
	 * are performed. This scenario illustrates how RAP would correct prorated retroactive payments for 
	 * the February coverage months from two date ranges to three date ranges. Following the maintenance 
	 * transaction and insertion of a new Policy version into EPS, RAP would reverse all existing February 
	 * payments and create prorated February payments for the new partial periods of coverage. 
	 * Payment Coverage Start Date and Payment Coverage End Date on the newly created transactions reflect 
	 * the partial months of coverage for reporting purposes.  The Proration Factor stored on the new 
	 * transactions reflects the ratio of the number of days of coverage to the number.
	 */
	@Test
	public void testCreateRetroPayments_Scenario6() {
		CodeRecord codeRecord = new CodeRecord("ERC", "2015-02-01 00:00:00", "2015-01-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroPartialMonth_Scenario6("APPV", null);
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-06-01", "2015-01-01", "2015-12-31");

		RapServiceTestUtil.loadStateConfigMap(policyVersion.getSubscriberStateCd(),
				policyVersion.getPolicyStartDate().getYear(), ProrationType.FFM_PRORATING);
		
		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);

		List<PolicyPaymentTransDTO> paymentTransactions = response.getPolicyPaymentTransactions();


		assertTrue("PolicyPaymentTransactions", CollectionUtils.isNotEmpty(paymentTransactions)); 
		assertEquals("PolicyPaymentTransactions size", 9, paymentTransactions.size());

		PolicyPaymentTransDTO aptcFebPrev2 = paymentTransactions.get(0);
		assertEquals("Payment Amount", new BigDecimal("-128.57"), aptcFebPrev2.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 11, 0, 0), aptcFebPrev2.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), aptcFebPrev2.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 7, aptcFebPrev2.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", aptcFebPrev2.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), aptcFebPrev2.getCoverageDate());
		assertEquals("Program Type", "APTC", aptcFebPrev2.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", aptcFebPrev2.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", aptcFebPrev2.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", aptcFebPrev2.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO aptcFeb1 = paymentTransactions.get(1);
		assertEquals("Payment Amount", new BigDecimal("17.86"), aptcFeb1.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 11, 0, 0), aptcFeb1.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 15, 0, 0), aptcFeb1.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", aptcFeb1.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", aptcFeb1.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), aptcFeb1.getCoverageDate());
		assertEquals("Program Type", "APTC", aptcFeb1.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", aptcFeb1.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", aptcFeb1.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", aptcFeb1.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO aptcFeb2 = paymentTransactions.get(2);
		assertEquals("Payment Amount", new BigDecimal("92.86"), aptcFeb2.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 16, 0, 0), aptcFeb2.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), aptcFeb2.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", aptcFeb2.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", aptcFeb2.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), aptcFeb2.getCoverageDate());
		assertEquals("Program Type", "APTC", aptcFeb2.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", aptcFeb2.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", aptcFeb2.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", aptcFeb2.getMarketplaceGroupPolicyId());


		PolicyPaymentTransDTO csrFebPrev2 = paymentTransactions.get(3);
		assertEquals("Payment Amount", new BigDecimal("-32.14"), csrFebPrev2.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 11, 0, 0), csrFebPrev2.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), csrFebPrev2.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 8, csrFebPrev2.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", csrFebPrev2.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), csrFebPrev2.getCoverageDate());
		assertEquals("Program Type", "CSR", csrFebPrev2.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", csrFebPrev2.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", csrFebPrev2.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", csrFebPrev2.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO csrFeb1 = paymentTransactions.get(4);
		assertEquals("Payment Amount", new BigDecimal("8.93"), csrFeb1.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 11, 0, 0), csrFeb1.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 15, 0, 0), csrFeb1.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", csrFeb1.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", csrFeb1.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), csrFeb1.getCoverageDate());
		assertEquals("Program Type", "CSR", csrFeb1.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", csrFeb1.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", csrFeb1.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", csrFeb1.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO csrFeb2 = paymentTransactions.get(5);
		assertEquals("Payment Amount", new BigDecimal("23.21"), csrFeb2.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 16, 0, 0), csrFeb2.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), csrFeb2.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", csrFeb2.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", csrFeb2.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), csrFeb2.getCoverageDate());
		assertEquals("Program Type", "CSR", csrFeb2.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", csrFeb2.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", csrFeb2.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", csrFeb2.getMarketplaceGroupPolicyId());


		PolicyPaymentTransDTO ufFebPrev2 = paymentTransactions.get(6);
		assertEquals("Payment Amount", new BigDecimal("5.63"), ufFebPrev2.getPaymentAmount());
		assertEquals("TP Amount", new BigDecimal("160.71"), ufFebPrev2.getTotalPremiumAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 11, 0, 0), ufFebPrev2.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), ufFebPrev2.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 9, ufFebPrev2.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", ufFebPrev2.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), ufFebPrev2.getCoverageDate());
		assertEquals("Program Type", "UF", ufFebPrev2.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", ufFebPrev2.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", ufFebPrev2.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", ufFebPrev2.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO ufFeb1 = paymentTransactions.get(7);
		assertNull("Payment Amount", ufFeb1.getPaymentAmount());
		assertEquals("TP Amount", new BigDecimal("26.79"), ufFeb1.getTotalPremiumAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 11, 0, 0), ufFeb1.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 15, 0, 0), ufFeb1.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", ufFeb1.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", ufFeb1.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), ufFeb1.getCoverageDate());
		assertEquals("Program Type", "UF", ufFeb1.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", ufFeb1.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", ufFeb1.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", ufFeb1.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO ufFeb2 = paymentTransactions.get(8);
		assertNull("Payment Amount", ufFeb2.getPaymentAmount());
		assertEquals("TP Amount", new BigDecimal("116.07"), ufFeb2.getTotalPremiumAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 16, 0, 0), ufFeb2.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), ufFeb2.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", ufFeb2.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", ufFeb2.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), ufFeb2.getCoverageDate());
		assertEquals("Program Type", "UF", ufFeb2.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", ufFeb2.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", ufFeb2.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", ufFeb2.getMarketplaceGroupPolicyId());
	}

	@Test
	public void testCreateRetroPayments_Scenario6_PendingCycle() {
		CodeRecord codeRecord = new CodeRecord("ERC", "2015-02-01 00:00:00", "2015-01-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroPartialMonth_Scenario6("PCYC", null);
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-06-01", "2015-01-01", "2015-12-31");

		RapServiceTestUtil.loadStateConfigMap(policyVersion.getSubscriberStateCd(),
				policyVersion.getPolicyStartDate().getYear(), ProrationType.FFM_PRORATING);
		
		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);

		List<PolicyPaymentTransDTO> paymentTransactions = response.getPolicyPaymentTransactions();


		assertTrue("PolicyPaymentTransactions", CollectionUtils.isNotEmpty(paymentTransactions)); 
		assertEquals("PolicyPaymentTransactions size", 9, paymentTransactions.size());

		PolicyPaymentTransDTO aptcFebPrev2 = paymentTransactions.get(0);
		assertEquals("Payment Amount", new BigDecimal("128.57"), aptcFebPrev2.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 11, 0, 0), aptcFebPrev2.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), aptcFebPrev2.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", aptcFebPrev2.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "P", aptcFebPrev2.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), aptcFebPrev2.getCoverageDate());
		assertEquals("Program Type", "APTC", aptcFebPrev2.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "REPL", aptcFebPrev2.getLastPaymentProcStatusTypeCd());
		assertTrue("Update Status", aptcFebPrev2.isUpdateStatusRec());
		assertNull("MGP Id", aptcFebPrev2.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO aptcFeb1 = paymentTransactions.get(1);
		assertEquals("Payment Amount", new BigDecimal("17.86"), aptcFeb1.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 11, 0, 0), aptcFeb1.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 15, 0, 0), aptcFeb1.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", aptcFeb1.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", aptcFeb1.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), aptcFeb1.getCoverageDate());
		assertEquals("Program Type", "APTC", aptcFeb1.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", aptcFeb1.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", aptcFeb1.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", aptcFeb1.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO aptcFeb2 = paymentTransactions.get(2);
		assertEquals("Payment Amount", new BigDecimal("92.86"), aptcFeb2.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 16, 0, 0), aptcFeb2.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), aptcFeb2.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", aptcFeb2.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", aptcFeb2.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), aptcFeb2.getCoverageDate());
		assertEquals("Program Type", "APTC", aptcFeb2.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", aptcFeb2.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", aptcFeb2.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", aptcFeb2.getMarketplaceGroupPolicyId());


		PolicyPaymentTransDTO csrFebPrev2 = paymentTransactions.get(3);
		assertEquals("Payment Amount", new BigDecimal("32.14"), csrFebPrev2.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 11, 0, 0), csrFebPrev2.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), csrFebPrev2.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", csrFebPrev2.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "P", csrFebPrev2.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), csrFebPrev2.getCoverageDate());
		assertEquals("Program Type", "CSR", csrFebPrev2.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "REPL", csrFebPrev2.getLastPaymentProcStatusTypeCd());
		assertTrue("Update Status", csrFebPrev2.isUpdateStatusRec());
		assertNull("MGP Id", csrFebPrev2.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO csrFeb1 = paymentTransactions.get(4);
		assertEquals("Payment Amount", new BigDecimal("8.93"), csrFeb1.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 11, 0, 0), csrFeb1.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 15, 0, 0), csrFeb1.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", csrFeb1.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", csrFeb1.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), csrFeb1.getCoverageDate());
		assertEquals("Program Type", "CSR", csrFeb1.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", csrFeb1.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", csrFeb1.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", csrFeb1.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO csrFeb2 = paymentTransactions.get(5);
		assertEquals("Payment Amount", new BigDecimal("23.21"), csrFeb2.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 16, 0, 0), csrFeb2.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), csrFeb2.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", csrFeb2.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", csrFeb2.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), csrFeb2.getCoverageDate());
		assertEquals("Program Type", "CSR", csrFeb2.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", csrFeb2.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", csrFeb2.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", csrFeb2.getMarketplaceGroupPolicyId());


		PolicyPaymentTransDTO ufFebPrev2 = paymentTransactions.get(6);
		assertEquals("Payment Amount", new BigDecimal("-5.63"), ufFebPrev2.getPaymentAmount());
		assertEquals("TP Amount", new BigDecimal("160.71"), ufFebPrev2.getTotalPremiumAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 11, 0, 0), ufFebPrev2.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), ufFebPrev2.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", ufFebPrev2.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "P", ufFebPrev2.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), ufFebPrev2.getCoverageDate());
		assertEquals("Program Type", "UF", ufFebPrev2.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "REPL", ufFebPrev2.getLastPaymentProcStatusTypeCd());
		assertTrue("Update Status", ufFebPrev2.isUpdateStatusRec());
		assertNull("MGP Id", ufFebPrev2.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO ufFeb1 = paymentTransactions.get(7);
		assertNull("Payment Amount", ufFeb1.getPaymentAmount());
		assertEquals("TP Amount", new BigDecimal("26.79"), ufFeb1.getTotalPremiumAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 11, 0, 0), ufFeb1.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 15, 0, 0), ufFeb1.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", ufFeb1.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", ufFeb1.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), ufFeb1.getCoverageDate());
		assertEquals("Program Type", "UF", ufFeb1.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", ufFeb1.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", ufFeb1.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", ufFeb1.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO ufFeb2 = paymentTransactions.get(8);
		assertNull("Payment Amount", ufFeb2.getPaymentAmount());
		assertEquals("TP Amount", new BigDecimal("116.07"), ufFeb2.getTotalPremiumAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 16, 0, 0), ufFeb2.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), ufFeb2.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", ufFeb2.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", ufFeb2.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), ufFeb2.getCoverageDate());
		assertEquals("Program Type", "UF", ufFeb2.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", ufFeb2.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", ufFeb2.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", ufFeb2.getMarketplaceGroupPolicyId());
	}
	
	@Test
	public void testCreateRetroPayments_Scenario6_PendingCycleHavingMgpId() {
		CodeRecord codeRecord = new CodeRecord("ERC", "2015-02-01 00:00:00", "2015-01-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroPartialMonth_Scenario6("PCYC", "MGPID_OLD");
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-06-01", "2015-01-01", "2015-12-31");

		RapServiceTestUtil.loadStateConfigMap(policyVersion.getSubscriberStateCd(),
				policyVersion.getPolicyStartDate().getYear(), ProrationType.FFM_PRORATING);
		
		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);

		List<PolicyPaymentTransDTO> paymentTransactions = response.getPolicyPaymentTransactions();


		assertTrue("PolicyPaymentTransactions", CollectionUtils.isNotEmpty(paymentTransactions)); 
		assertEquals("PolicyPaymentTransactions size", 9, paymentTransactions.size());

		PolicyPaymentTransDTO aptcFebPrev2 = paymentTransactions.get(0);
		assertEquals("Payment Amount", new BigDecimal("128.57"), aptcFebPrev2.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 11, 0, 0), aptcFebPrev2.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), aptcFebPrev2.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", aptcFebPrev2.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "P", aptcFebPrev2.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), aptcFebPrev2.getCoverageDate());
		assertEquals("Program Type", "APTC", aptcFebPrev2.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "REPL", aptcFebPrev2.getLastPaymentProcStatusTypeCd());
		assertTrue("Update Status", aptcFebPrev2.isUpdateStatusRec());
		assertNotNull("MGP Id", aptcFebPrev2.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID_OLD", aptcFebPrev2.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO aptcFeb1 = paymentTransactions.get(1);
		assertEquals("Payment Amount", new BigDecimal("17.86"), aptcFeb1.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 11, 0, 0), aptcFeb1.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 15, 0, 0), aptcFeb1.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", aptcFeb1.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", aptcFeb1.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), aptcFeb1.getCoverageDate());
		assertEquals("Program Type", "APTC", aptcFeb1.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", aptcFeb1.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", aptcFeb1.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", aptcFeb1.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO aptcFeb2 = paymentTransactions.get(2);
		assertEquals("Payment Amount", new BigDecimal("92.86"), aptcFeb2.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 16, 0, 0), aptcFeb2.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), aptcFeb2.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", aptcFeb2.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", aptcFeb2.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), aptcFeb2.getCoverageDate());
		assertEquals("Program Type", "APTC", aptcFeb2.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", aptcFeb2.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", aptcFeb2.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", aptcFeb2.getMarketplaceGroupPolicyId());


		PolicyPaymentTransDTO csrFebPrev2 = paymentTransactions.get(3);
		assertEquals("Payment Amount", new BigDecimal("32.14"), csrFebPrev2.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 11, 0, 0), csrFebPrev2.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), csrFebPrev2.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", csrFebPrev2.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "P", csrFebPrev2.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), csrFebPrev2.getCoverageDate());
		assertEquals("Program Type", "CSR", csrFebPrev2.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "REPL", csrFebPrev2.getLastPaymentProcStatusTypeCd());
		assertTrue("Update Status", csrFebPrev2.isUpdateStatusRec());
		assertNotNull("MGP Id", csrFebPrev2.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID_OLD", csrFebPrev2.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO csrFeb1 = paymentTransactions.get(4);
		assertEquals("Payment Amount", new BigDecimal("8.93"), csrFeb1.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 11, 0, 0), csrFeb1.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 15, 0, 0), csrFeb1.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", csrFeb1.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", csrFeb1.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), csrFeb1.getCoverageDate());
		assertEquals("Program Type", "CSR", csrFeb1.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", csrFeb1.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", csrFeb1.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", csrFeb1.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO csrFeb2 = paymentTransactions.get(5);
		assertEquals("Payment Amount", new BigDecimal("23.21"), csrFeb2.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 16, 0, 0), csrFeb2.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), csrFeb2.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", csrFeb2.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", csrFeb2.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), csrFeb2.getCoverageDate());
		assertEquals("Program Type", "CSR", csrFeb2.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", csrFeb2.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", csrFeb2.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", csrFeb2.getMarketplaceGroupPolicyId());


		PolicyPaymentTransDTO ufFebPrev2 = paymentTransactions.get(6);
		assertEquals("Payment Amount", new BigDecimal("-5.63"), ufFebPrev2.getPaymentAmount());
		assertEquals("TP Amount", new BigDecimal("160.71"), ufFebPrev2.getTotalPremiumAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 11, 0, 0), ufFebPrev2.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), ufFebPrev2.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", ufFebPrev2.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "P", ufFebPrev2.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), ufFebPrev2.getCoverageDate());
		assertEquals("Program Type", "UF", ufFebPrev2.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "REPL", ufFebPrev2.getLastPaymentProcStatusTypeCd());
		assertTrue("Update Status", ufFebPrev2.isUpdateStatusRec());
		assertNotNull("MGP Id", ufFebPrev2.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID_OLD", ufFebPrev2.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO ufFeb1 = paymentTransactions.get(7);
		assertNull("Payment Amount", ufFeb1.getPaymentAmount());
		assertEquals("TP Amount", new BigDecimal("26.79"), ufFeb1.getTotalPremiumAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 11, 0, 0), ufFeb1.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 15, 0, 0), ufFeb1.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", ufFeb1.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", ufFeb1.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), ufFeb1.getCoverageDate());
		assertEquals("Program Type", "UF", ufFeb1.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", ufFeb1.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", ufFeb1.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", ufFeb1.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO ufFeb2 = paymentTransactions.get(8);
		assertNull("Payment Amount", ufFeb2.getPaymentAmount());
		assertEquals("TP Amount", new BigDecimal("116.07"), ufFeb2.getTotalPremiumAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 16, 0, 0), ufFeb2.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), ufFeb2.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", ufFeb2.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", ufFeb2.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), ufFeb2.getCoverageDate());
		assertEquals("Program Type", "UF", ufFeb2.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", ufFeb2.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", ufFeb2.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", ufFeb2.getMarketplaceGroupPolicyId());
	}

	/**
	 * Test FDD Scenario6 for APTC Payments 
	 * 
	 * Amount changes for 2/1-2/10 of scenario6.
	 */
	@Test
	public void testCreateAptcPayments_Scenario6A() {
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		replay(mockRapDao);

		ReflectionTestUtils.setField(rapProcesssingServiceTest, "reversalRefIds", new ArrayList<Long>());
		ReflectionTestUtils.setField(rapProcesssingServiceTest, "reversedTransIds", new ArrayList<Long>());

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-06-01", "2015-01-01", "2015-12-31");

		RapServiceTestUtil.loadStateConfigMap(policyVersion.getSubscriberStateCd(),
				policyVersion.getPolicyStartDate().getYear(), ProrationType.FFM_PRORATING);
		
		DateTime coverageDt = new DateTime("2015-02-01");

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroPartialMonth_Scenario6A_APTC();

		List<PolicyPaymentTransDTO>  paymentTransactions = new ArrayList<PolicyPaymentTransDTO>();

		ReflectionTestUtils.invokeMethod(rapProcesssingServiceTest, "createAptcPayments",
				policyVersion, coverageDt, paymentTransactions,
				policyDetailDTO.getPolicyPayments(), policyDetailDTO.getPolicyPremiums(), ProrationType.FFM_PRORATING);

		assertTrue("PolicyPaymentTransactions", CollectionUtils.isNotEmpty(paymentTransactions)); 
		assertEquals("PolicyPaymentTransactions size", 5, paymentTransactions.size());

		assertEquals("Payment Transaction values comparison", 
				new BigDecimal("-35.71"), paymentTransactions.get(0).getPaymentAmount());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 2, 1, 0, 0), paymentTransactions.get(0).getPaymentCoverageStartDate());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 2, 10, 0, 0), paymentTransactions.get(0).getPaymentCoverageEndDate());
		assertEquals("Payment Transaction values comparison", 
				1, paymentTransactions.get(0).getParentPolicyPaymentTransId().longValue());

		assertEquals("Payment Transaction values comparison", 
				new BigDecimal("53.57"), paymentTransactions.get(1).getPaymentAmount());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 2, 1, 0, 0), paymentTransactions.get(1).getPaymentCoverageStartDate());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 2, 10, 0, 0), paymentTransactions.get(1).getPaymentCoverageEndDate());
		assertNull("Payment Transaction values comparison", 
				paymentTransactions.get(1).getParentPolicyPaymentTransId());

		assertEquals("Payment Transaction values comparison", 
				new BigDecimal("-128.57"), paymentTransactions.get(2).getPaymentAmount());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 2, 11, 0, 0), paymentTransactions.get(2).getPaymentCoverageStartDate());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 2, 28, 0, 0), paymentTransactions.get(2).getPaymentCoverageEndDate());
		assertEquals("Payment Transaction values comparison", 
				2, paymentTransactions.get(2).getParentPolicyPaymentTransId().longValue());

		assertEquals("Payment Transaction values comparison", 
				new BigDecimal("17.86"), paymentTransactions.get(3).getPaymentAmount());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 2, 11, 0, 0), paymentTransactions.get(3).getPaymentCoverageStartDate());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 2, 15, 0, 0), paymentTransactions.get(3).getPaymentCoverageEndDate());
		assertNull("Payment Transaction values comparison", 
				paymentTransactions.get(3).getParentPolicyPaymentTransId());

		assertEquals("Payment Transaction values comparison", 
				new BigDecimal("92.86"), paymentTransactions.get(4).getPaymentAmount());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 2, 16, 0, 0), paymentTransactions.get(4).getPaymentCoverageStartDate());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 2, 28, 0, 0), paymentTransactions.get(4).getPaymentCoverageEndDate());
		assertNull("Payment Transaction values comparison", 
				paymentTransactions.get(4).getParentPolicyPaymentTransId());

	}

	/**
	 * Test FDD Scenario6 for APTC Payments 
	 * 
	 * Amount changes for 2/1-2/10 of scenario6.
	 */
	@Test
	public void testCreateRetroPayments_Scenario6A() {
		CodeRecord codeRecord = new CodeRecord("ERC", "2015-02-01 00:00:00", "2015-01-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroPartialMonth_Scenario6A("APPV");
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-06-01", "2015-01-01", "2015-12-31");

		RapServiceTestUtil.loadStateConfigMap(policyVersion.getSubscriberStateCd(),
				policyVersion.getPolicyStartDate().getYear(), ProrationType.FFM_PRORATING);
		
		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);

		List<PolicyPaymentTransDTO> paymentTransactions = response.getPolicyPaymentTransactions();


		assertTrue("PolicyPaymentTransactions", CollectionUtils.isNotEmpty(paymentTransactions)); 
		assertEquals("PolicyPaymentTransactions size", 17, paymentTransactions.size());

		PolicyPaymentTransDTO aptcJanPrev = paymentTransactions.get(0);
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), aptcJanPrev.getCoverageDate());
		assertEquals("Program Type", "APTC", aptcJanPrev.getFinancialProgramTypeCd());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 1, 1, 0, 0), aptcJanPrev.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 1, 31, 0, 0), aptcJanPrev.getPaymentCoverageEndDate());
		assertEquals("Payment Amount", new BigDecimal("-100.00"), aptcJanPrev.getPaymentAmount());
		assertEquals("Parent PolicyPayment Trans Id", 1, aptcJanPrev.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", aptcJanPrev.getTransPeriodTypeCd());
		assertEquals("Trans Type", "PCYC", aptcJanPrev.getLastPaymentProcStatusTypeCd());

		PolicyPaymentTransDTO aptcJan = paymentTransactions.get(1);
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), aptcJan.getCoverageDate());
		assertEquals("Program Type", "APTC", aptcJan.getFinancialProgramTypeCd());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 1, 1, 0, 0), aptcJan.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 1, 31, 0, 0), aptcJan.getPaymentCoverageEndDate());
		assertEquals("Payment Amount", new BigDecimal("150.00"), aptcJan.getPaymentAmount());
		assertNull("Parent PolicyPayment Trans Id", aptcJan.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", aptcJan.getTransPeriodTypeCd());
		assertEquals("Trans Type", "PCYC", aptcJan.getLastPaymentProcStatusTypeCd());


		PolicyPaymentTransDTO ufJanPrev = paymentTransactions.get(2);
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), ufJanPrev.getCoverageDate());
		assertEquals("Program Type", "UF", ufJanPrev.getFinancialProgramTypeCd());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 1, 1, 0, 0), ufJanPrev.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 1, 31, 0, 0), ufJanPrev.getPaymentCoverageEndDate());
		assertEquals("Payment Amount", new BigDecimal("5.25"), ufJanPrev.getPaymentAmount());
		assertEquals("TP Amount", new BigDecimal("150"), ufJanPrev.getTotalPremiumAmount());
		assertEquals("Parent PolicyPayment Trans Id", 3, ufJanPrev.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", ufJanPrev.getTransPeriodTypeCd());
		assertEquals("Trans Type", "PCYC", ufJanPrev.getLastPaymentProcStatusTypeCd());

		PolicyPaymentTransDTO ufJan = paymentTransactions.get(3);
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), ufJan.getCoverageDate());
		assertEquals("Program Type", "UF", ufJan.getFinancialProgramTypeCd());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 1, 1, 0, 0), ufJan.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 1, 31, 0, 0), ufJan.getPaymentCoverageEndDate());
		assertNull("Payment Amount", ufJan.getPaymentAmount());
		assertEquals("TP Amount", new BigDecimal("200.00"), ufJan.getTotalPremiumAmount());
		assertNull("Parent PolicyPayment Trans Id", ufJan.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", ufJan.getTransPeriodTypeCd());
		assertEquals("Trans Type", "PCYC", ufJan.getLastPaymentProcStatusTypeCd());


		PolicyPaymentTransDTO aptcFebPrev1 = paymentTransactions.get(4);
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), aptcFebPrev1.getCoverageDate());
		assertEquals("Program Type", "APTC", aptcFebPrev1.getFinancialProgramTypeCd());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), aptcFebPrev1.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 10, 0, 0), aptcFebPrev1.getPaymentCoverageEndDate());
		assertEquals("Payment Amount", new BigDecimal("-35.71"), aptcFebPrev1.getPaymentAmount());
		assertEquals("Parent PolicyPayment Trans Id", 4, aptcFebPrev1.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", aptcFebPrev1.getTransPeriodTypeCd());
		assertEquals("Trans Type", "PCYC", aptcFebPrev1.getLastPaymentProcStatusTypeCd());

		PolicyPaymentTransDTO aptcFeb1 = paymentTransactions.get(5);
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), aptcFeb1.getCoverageDate());
		assertEquals("Program Type", "APTC", aptcFeb1.getFinancialProgramTypeCd());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), aptcFeb1.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 10, 0, 0), aptcFeb1.getPaymentCoverageEndDate());
		assertEquals("Payment Amount", new BigDecimal("53.57"), aptcFeb1.getPaymentAmount());
		assertNull("Parent PolicyPayment Trans Id", aptcFeb1.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", aptcFeb1.getTransPeriodTypeCd());
		assertEquals("Trans Type", "PCYC", aptcFeb1.getLastPaymentProcStatusTypeCd());


		PolicyPaymentTransDTO aptcFebPrev2 = paymentTransactions.get(6);
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), aptcFebPrev2.getCoverageDate());
		assertEquals("Program Type", "APTC", aptcFebPrev2.getFinancialProgramTypeCd());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 11, 0, 0), aptcFebPrev2.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), aptcFebPrev2.getPaymentCoverageEndDate());
		assertEquals("Payment Amount", new BigDecimal("-128.57"), aptcFebPrev2.getPaymentAmount());
		assertEquals("Parent PolicyPayment Trans Id", 7, aptcFebPrev2.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", aptcFebPrev2.getTransPeriodTypeCd());
		assertEquals("Trans Type", "PCYC", aptcFebPrev2.getLastPaymentProcStatusTypeCd());

		PolicyPaymentTransDTO aptcFeb2 = paymentTransactions.get(7);
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), aptcFeb2.getCoverageDate());
		assertEquals("Program Type", "APTC", aptcFeb2.getFinancialProgramTypeCd());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 11, 0, 0), aptcFeb2.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 15, 0, 0), aptcFeb2.getPaymentCoverageEndDate());
		assertEquals("Payment Amount", new BigDecimal("17.86"), aptcFeb2.getPaymentAmount());
		assertNull("Parent PolicyPayment Trans Id", aptcFeb2.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", aptcFeb2.getTransPeriodTypeCd());
		assertEquals("Trans Type", "PCYC", aptcFeb2.getLastPaymentProcStatusTypeCd());

		PolicyPaymentTransDTO aptcFeb3 = paymentTransactions.get(8);
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), aptcFeb3.getCoverageDate());
		assertEquals("Program Type", "APTC", aptcFeb3.getFinancialProgramTypeCd());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 16, 0, 0), aptcFeb3.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), aptcFeb3.getPaymentCoverageEndDate());
		assertEquals("Payment Amount", new BigDecimal("92.86"), aptcFeb3.getPaymentAmount());
		assertNull("Parent PolicyPayment Trans Id", aptcFeb3.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", aptcFeb3.getTransPeriodTypeCd());
		assertEquals("Trans Type", "PCYC", aptcFeb3.getLastPaymentProcStatusTypeCd());


		PolicyPaymentTransDTO csrFebPrev2 = paymentTransactions.get(9);
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), csrFebPrev2.getCoverageDate());
		assertEquals("Program Type", "CSR", csrFebPrev2.getFinancialProgramTypeCd());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 11, 0, 0), csrFebPrev2.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), csrFebPrev2.getPaymentCoverageEndDate());
		assertEquals("Payment Amount", new BigDecimal("-32.14"), csrFebPrev2.getPaymentAmount());
		assertEquals("Parent PolicyPayment Trans Id", 8, csrFebPrev2.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", csrFebPrev2.getTransPeriodTypeCd());
		assertEquals("Trans Type", "PCYC", csrFebPrev2.getLastPaymentProcStatusTypeCd());

		PolicyPaymentTransDTO csrFeb2 = paymentTransactions.get(10);
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), csrFeb2.getCoverageDate());
		assertEquals("Program Type", "CSR", csrFeb2.getFinancialProgramTypeCd());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 11, 0, 0), csrFeb2.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 15, 0, 0), csrFeb2.getPaymentCoverageEndDate());
		assertEquals("Payment Amount", new BigDecimal("8.93"), csrFeb2.getPaymentAmount());
		assertNull("Parent PolicyPayment Trans Id", csrFeb2.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", csrFeb2.getTransPeriodTypeCd());
		assertEquals("Trans Type", "PCYC", csrFeb2.getLastPaymentProcStatusTypeCd());

		PolicyPaymentTransDTO csrFeb3 = paymentTransactions.get(11);
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), csrFeb3.getCoverageDate());
		assertEquals("Program Type", "CSR", csrFeb3.getFinancialProgramTypeCd());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 16, 0, 0), csrFeb3.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), csrFeb3.getPaymentCoverageEndDate());
		assertEquals("Payment Amount", new BigDecimal("23.21"), csrFeb3.getPaymentAmount());
		assertNull("Parent PolicyPayment Trans Id", csrFeb3.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", csrFeb3.getTransPeriodTypeCd());
		assertEquals("Trans Type", "PCYC", csrFeb3.getLastPaymentProcStatusTypeCd());


		PolicyPaymentTransDTO ufFebPrev1 = paymentTransactions.get(12);
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), ufFebPrev1.getCoverageDate());
		assertEquals("Program Type", "UF", ufFebPrev1.getFinancialProgramTypeCd());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), ufFebPrev1.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 10, 0, 0), ufFebPrev1.getPaymentCoverageEndDate());
		assertEquals("Payment Amount", new BigDecimal("1.88"), ufFebPrev1.getPaymentAmount());
		assertEquals("TP Amount", new BigDecimal("150"), ufFebPrev1.getTotalPremiumAmount());
		assertEquals("Parent PolicyPayment Trans Id", 6, ufFebPrev1.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", ufFebPrev1.getTransPeriodTypeCd());
		assertEquals("Trans Type", "PCYC", ufFebPrev1.getLastPaymentProcStatusTypeCd());

		PolicyPaymentTransDTO ufFeb1 = paymentTransactions.get(13);
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), ufFeb1.getCoverageDate());
		assertEquals("Program Type", "UF", ufFeb1.getFinancialProgramTypeCd());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), ufFeb1.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 10, 0, 0), ufFeb1.getPaymentCoverageEndDate());
		assertNull("Payment Amount", ufFeb1.getPaymentAmount());
		assertEquals("TP Amount", new BigDecimal("71.43"), ufFeb1.getTotalPremiumAmount());
		assertNull("Parent PolicyPayment Trans Id", ufFeb1.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", ufFeb1.getTransPeriodTypeCd());
		assertEquals("Trans Type", "PCYC", ufFeb1.getLastPaymentProcStatusTypeCd());

		PolicyPaymentTransDTO ufFebPrev2 = paymentTransactions.get(14);
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), ufFebPrev2.getCoverageDate());
		assertEquals("Program Type", "UF", ufFebPrev2.getFinancialProgramTypeCd());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 11, 0, 0), ufFebPrev2.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), ufFebPrev2.getPaymentCoverageEndDate());
		assertEquals("Payment Amount", new BigDecimal("5.63"), ufFebPrev2.getPaymentAmount());
		assertEquals("TP Amount", new BigDecimal("250"), ufFebPrev2.getTotalPremiumAmount());
		assertEquals("Parent PolicyPayment Trans Id", 9, ufFebPrev2.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", ufFebPrev2.getTransPeriodTypeCd());
		assertEquals("Trans Type", "PCYC", ufFebPrev2.getLastPaymentProcStatusTypeCd());

		PolicyPaymentTransDTO ufFeb2 = paymentTransactions.get(15);
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), ufFeb2.getCoverageDate());
		assertEquals("Program Type", "UF", ufFeb2.getFinancialProgramTypeCd());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 11, 0, 0), ufFeb2.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 15, 0, 0), ufFeb2.getPaymentCoverageEndDate());
		assertNull("Payment Amount", ufFeb2.getPaymentAmount());
		assertEquals("TP Amount", new BigDecimal("26.79"), ufFeb2.getTotalPremiumAmount());
		assertNull("Parent PolicyPayment Trans Id", ufFeb2.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", ufFeb2.getTransPeriodTypeCd());
		assertEquals("Trans Type", "PCYC", ufFeb2.getLastPaymentProcStatusTypeCd());

		PolicyPaymentTransDTO ufFeb3 = paymentTransactions.get(16);
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), ufFeb3.getCoverageDate());
		assertEquals("Program Type", "UF", ufFeb3.getFinancialProgramTypeCd());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 16, 0, 0), ufFeb3.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), ufFeb3.getPaymentCoverageEndDate());
		assertNull("Payment Amount", ufFeb3.getPaymentAmount());
		assertEquals("TP Amount", new BigDecimal("116.07"), ufFeb3.getTotalPremiumAmount());
		assertNull("Parent PolicyPayment Trans Id", ufFeb3.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", ufFeb3.getTransPeriodTypeCd());
		assertEquals("Trans Type", "PCYC", ufFeb3.getLastPaymentProcStatusTypeCd());

	}


	/**
	 * Test FDD Scenario7 for APTC Payments 
	 * 
	 * A later payment in the month matches existing payment, only prior payments change
	 */
	@Test
	public void testCreateAptcPayments_Scenario7() {
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		replay(mockRapDao);

		ReflectionTestUtils.setField(rapProcesssingServiceTest, "reversalRefIds", new ArrayList<Long>());
		ReflectionTestUtils.setField(rapProcesssingServiceTest, "reversedTransIds", new ArrayList<Long>());

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-06-01", "2015-01-01", "2015-12-31");

		RapServiceTestUtil.loadStateConfigMap(policyVersion.getSubscriberStateCd(),
				policyVersion.getPolicyStartDate().getYear(), ProrationType.FFM_PRORATING);
		
		DateTime coverageDt = new DateTime("2015-02-01");

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroPartialMonth_Scenario7_APTC();

		List<PolicyPaymentTransDTO>  paymentTransactions = new ArrayList<PolicyPaymentTransDTO>();

		ReflectionTestUtils.invokeMethod(rapProcesssingServiceTest, "createAptcPayments",
				policyVersion, coverageDt, paymentTransactions,
				policyDetailDTO.getPolicyPayments(), policyDetailDTO.getPolicyPremiums(), ProrationType.FFM_PRORATING);

		assertTrue("PolicyPaymentTransactions", CollectionUtils.isNotEmpty(paymentTransactions)); 
		assertEquals("PolicyPaymentTransactions size", 5, paymentTransactions.size());

		assertEquals("Payment Transaction values comparison", 
				new BigDecimal("-35.71"), paymentTransactions.get(0).getPaymentAmount());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 2, 1, 0, 0), paymentTransactions.get(0).getPaymentCoverageStartDate());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 2, 10, 0, 0), paymentTransactions.get(0).getPaymentCoverageEndDate());
		assertEquals("Payment Transaction values comparison", 
				1, paymentTransactions.get(0).getParentPolicyPaymentTransId().longValue());

		assertEquals("Payment Transaction values comparison", 
				new BigDecimal("-26.79"), paymentTransactions.get(1).getPaymentAmount());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 2, 11, 0, 0), paymentTransactions.get(1).getPaymentCoverageStartDate());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 2, 15, 0, 0), paymentTransactions.get(1).getPaymentCoverageEndDate());
		assertEquals("Payment Transaction values comparison", 
				2, paymentTransactions.get(1).getParentPolicyPaymentTransId().longValue());

		assertEquals("Payment Transaction values comparison", 
				new BigDecimal("-92.86"), paymentTransactions.get(2).getPaymentAmount());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 2, 16, 0, 0), paymentTransactions.get(2).getPaymentCoverageStartDate());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 2, 28, 0, 0), paymentTransactions.get(2).getPaymentCoverageEndDate());
		assertEquals("Payment Transaction values comparison", 
				3, paymentTransactions.get(2).getParentPolicyPaymentTransId().longValue());

		assertEquals("Payment Transaction values comparison", 
				new BigDecimal("53.57"), paymentTransactions.get(3).getPaymentAmount());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 2, 1, 0, 0), paymentTransactions.get(3).getPaymentCoverageStartDate());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 2, 15, 0, 0), paymentTransactions.get(3).getPaymentCoverageEndDate());
		assertNull("Payment Transaction values comparison", 
				paymentTransactions.get(3).getParentPolicyPaymentTransId());

		assertEquals("Payment Transaction values comparison", 
				new BigDecimal("92.86"), paymentTransactions.get(4).getPaymentAmount());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 2, 16, 0, 0), paymentTransactions.get(4).getPaymentCoverageStartDate());
		assertEquals("Payment Transaction values comparison", 
				new DateTime(2015, 2, 28, 0, 0), paymentTransactions.get(4).getPaymentCoverageEndDate());
		assertNull("Payment Transaction values comparison", 
				paymentTransactions.get(4).getParentPolicyPaymentTransId());

	}

	/**
	 * Test Scenario7 
	 * 
	 * A later payment in the month matches existing payment, only prior payments change
	 */
	@Test
	public void testCreateRetroPayments_Scenario7() {
		CodeRecord codeRecord = new CodeRecord("ERC", "2015-02-01 00:00:00", "2015-01-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroPartialMonth_Scenario7();
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-06-01", "2015-01-01", "2015-12-31");

		RapServiceTestUtil.loadStateConfigMap(policyVersion.getSubscriberStateCd(),
				policyVersion.getPolicyStartDate().getYear(), ProrationType.FFM_PRORATING);
		
		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);

		List<PolicyPaymentTransDTO> paymentTransactions = response.getPolicyPaymentTransactions();


		assertTrue("PolicyPaymentTransactions", CollectionUtils.isNotEmpty(paymentTransactions)); 
		assertEquals("PolicyPaymentTransactions size", 15, paymentTransactions.size());


		PolicyPaymentTransDTO aptcFebPrev1 = paymentTransactions.get(0);
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), aptcFebPrev1.getCoverageDate());
		assertEquals("Program Type", "APTC", aptcFebPrev1.getFinancialProgramTypeCd());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), aptcFebPrev1.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 10, 0, 0), aptcFebPrev1.getPaymentCoverageEndDate());
		assertEquals("Payment Amount", new BigDecimal("-35.71"), aptcFebPrev1.getPaymentAmount());
		assertEquals("Parent PolicyPayment Trans Id", 4, aptcFebPrev1.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", aptcFebPrev1.getTransPeriodTypeCd());
		assertEquals("Trans Type", "PCYC", aptcFebPrev1.getLastPaymentProcStatusTypeCd());

		PolicyPaymentTransDTO aptcFebPrev2 = paymentTransactions.get(1);
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), aptcFebPrev2.getCoverageDate());
		assertEquals("Program Type", "APTC", aptcFebPrev2.getFinancialProgramTypeCd());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 11, 0, 0), aptcFebPrev2.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 15, 0, 0), aptcFebPrev2.getPaymentCoverageEndDate());
		assertEquals("Payment Amount", new BigDecimal("-26.79"), aptcFebPrev2.getPaymentAmount());
		assertEquals("Parent PolicyPayment Trans Id", 7, aptcFebPrev2.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", aptcFebPrev2.getTransPeriodTypeCd());
		assertEquals("Trans Type", "PCYC", aptcFebPrev2.getLastPaymentProcStatusTypeCd());

		PolicyPaymentTransDTO aptcFebPrev3 = paymentTransactions.get(2);
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), aptcFebPrev3.getCoverageDate());
		assertEquals("Program Type", "APTC", aptcFebPrev3.getFinancialProgramTypeCd());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 16, 0, 0), aptcFebPrev3.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), aptcFebPrev3.getPaymentCoverageEndDate());
		assertEquals("Payment Amount", new BigDecimal("-92.86"), aptcFebPrev3.getPaymentAmount());
		assertEquals("Parent PolicyPayment Trans Id", 10, aptcFebPrev3.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", aptcFebPrev3.getTransPeriodTypeCd());
		assertEquals("Trans Type", "PCYC", aptcFebPrev3.getLastPaymentProcStatusTypeCd());		

		PolicyPaymentTransDTO aptcFeb1 = paymentTransactions.get(3);
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), aptcFeb1.getCoverageDate());
		assertEquals("Program Type", "APTC", aptcFeb1.getFinancialProgramTypeCd());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), aptcFeb1.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 15, 0, 0), aptcFeb1.getPaymentCoverageEndDate());
		assertEquals("Payment Amount", new BigDecimal("53.57"), aptcFeb1.getPaymentAmount());
		assertNull("Parent PolicyPayment Trans Id", aptcFeb1.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", aptcFeb1.getTransPeriodTypeCd());
		assertEquals("Trans Type", "PCYC", aptcFeb1.getLastPaymentProcStatusTypeCd());

		PolicyPaymentTransDTO aptcFeb2 = paymentTransactions.get(4);
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), aptcFeb2.getCoverageDate());
		assertEquals("Program Type", "APTC", aptcFeb2.getFinancialProgramTypeCd());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 16, 0, 0), aptcFeb2.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), aptcFeb2.getPaymentCoverageEndDate());
		assertEquals("Payment Amount", new BigDecimal("92.86"), aptcFeb2.getPaymentAmount());
		assertNull("Parent PolicyPayment Trans Id", aptcFeb2.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", aptcFeb2.getTransPeriodTypeCd());
		assertEquals("Trans Type", "PCYC", aptcFeb2.getLastPaymentProcStatusTypeCd());		


		PolicyPaymentTransDTO csrFebPrev1 = paymentTransactions.get(5);
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), csrFebPrev1.getCoverageDate());
		assertEquals("Program Type", "CSR", csrFebPrev1.getFinancialProgramTypeCd());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), csrFebPrev1.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 10, 0, 0), csrFebPrev1.getPaymentCoverageEndDate());
		assertEquals("Payment Amount", new BigDecimal("-17.86"), csrFebPrev1.getPaymentAmount());
		assertEquals("Parent PolicyPayment Trans Id", 5, csrFebPrev1.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", csrFebPrev1.getTransPeriodTypeCd());
		assertEquals("Trans Type", "PCYC", csrFebPrev1.getLastPaymentProcStatusTypeCd());

		PolicyPaymentTransDTO csrFebPrev2 = paymentTransactions.get(6);
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), csrFebPrev2.getCoverageDate());
		assertEquals("Program Type", "CSR", csrFebPrev2.getFinancialProgramTypeCd());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 11, 0, 0), csrFebPrev2.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 15, 0, 0), csrFebPrev2.getPaymentCoverageEndDate());
		assertEquals("Payment Amount", new BigDecimal("-8.93"), csrFebPrev2.getPaymentAmount());
		assertEquals("Parent PolicyPayment Trans Id", 8, csrFebPrev2.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", csrFebPrev2.getTransPeriodTypeCd());
		assertEquals("Trans Type", "PCYC", csrFebPrev2.getLastPaymentProcStatusTypeCd());

		PolicyPaymentTransDTO csrFebPrev3 = paymentTransactions.get(7);
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), csrFebPrev3.getCoverageDate());
		assertEquals("Program Type", "CSR", csrFebPrev3.getFinancialProgramTypeCd());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 16, 0, 0), csrFebPrev3.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), csrFebPrev3.getPaymentCoverageEndDate());
		assertEquals("Payment Amount", new BigDecimal("-23.21"), csrFebPrev3.getPaymentAmount());
		assertEquals("Parent PolicyPayment Trans Id", 11, csrFebPrev3.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", csrFebPrev3.getTransPeriodTypeCd());
		assertEquals("Trans Type", "PCYC", csrFebPrev3.getLastPaymentProcStatusTypeCd());

		PolicyPaymentTransDTO csrFeb1 = paymentTransactions.get(8);
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), csrFeb1.getCoverageDate());
		assertEquals("Program Type", "CSR", csrFeb1.getFinancialProgramTypeCd());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), csrFeb1.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 15, 0, 0), csrFeb1.getPaymentCoverageEndDate());
		assertEquals("Payment Amount", new BigDecimal("26.79"), csrFeb1.getPaymentAmount());
		assertNull("Parent PolicyPayment Trans Id", csrFeb1.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", csrFeb1.getTransPeriodTypeCd());
		assertEquals("Trans Type", "PCYC", csrFeb1.getLastPaymentProcStatusTypeCd());

		PolicyPaymentTransDTO csrFeb2 = paymentTransactions.get(9);
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), csrFeb2.getCoverageDate());
		assertEquals("Program Type", "CSR", csrFeb2.getFinancialProgramTypeCd());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 16, 0, 0), csrFeb2.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), csrFeb2.getPaymentCoverageEndDate());
		assertEquals("Payment Amount", new BigDecimal("23.21"), csrFeb2.getPaymentAmount());
		assertNull("Parent PolicyPayment Trans Id", csrFeb2.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", csrFeb2.getTransPeriodTypeCd());
		assertEquals("Trans Type", "PCYC", csrFeb2.getLastPaymentProcStatusTypeCd());

		PolicyPaymentTransDTO ufFebPrev1 = paymentTransactions.get(10);
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), ufFebPrev1.getCoverageDate());
		assertEquals("Program Type", "UF", ufFebPrev1.getFinancialProgramTypeCd());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), ufFebPrev1.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 10, 0, 0), ufFebPrev1.getPaymentCoverageEndDate());
		assertEquals("Payment Amount", new BigDecimal("1.88"), ufFebPrev1.getPaymentAmount());
		assertEquals("TP Amount", new BigDecimal("53.57"), ufFebPrev1.getTotalPremiumAmount());
		assertEquals("Parent PolicyPayment Trans Id", 6, ufFebPrev1.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", ufFebPrev1.getTransPeriodTypeCd());
		assertEquals("Trans Type", "PCYC", ufFebPrev1.getLastPaymentProcStatusTypeCd());

		PolicyPaymentTransDTO ufFebPrev2 = paymentTransactions.get(11);
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), ufFebPrev2.getCoverageDate());
		assertEquals("Program Type", "UF", ufFebPrev2.getFinancialProgramTypeCd());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 11, 0, 0), ufFebPrev2.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 15, 0, 0), ufFebPrev2.getPaymentCoverageEndDate());
		assertEquals("Payment Amount", new BigDecimal("1.25"), ufFebPrev2.getPaymentAmount());
		assertEquals("TP Amount", new BigDecimal("35.71"), ufFebPrev2.getTotalPremiumAmount());
		assertEquals("Parent PolicyPayment Trans Id", 9, ufFebPrev2.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", ufFebPrev2.getTransPeriodTypeCd());
		assertEquals("Trans Type", "PCYC", ufFebPrev2.getLastPaymentProcStatusTypeCd());

		PolicyPaymentTransDTO ufFebPrev3 = paymentTransactions.get(12);
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), ufFebPrev3.getCoverageDate());
		assertEquals("Program Type", "UF", ufFebPrev3.getFinancialProgramTypeCd());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 16, 0, 0), ufFebPrev3.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), ufFebPrev3.getPaymentCoverageEndDate());
		assertEquals("Payment Amount", new BigDecimal("4.06"), ufFebPrev3.getPaymentAmount());
		assertEquals("TP Amount", new BigDecimal("116.07"), ufFebPrev3.getTotalPremiumAmount());
		assertEquals("Parent PolicyPayment Trans Id", 12, ufFebPrev3.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", ufFebPrev3.getTransPeriodTypeCd());
		assertEquals("Trans Type", "PCYC", ufFebPrev3.getLastPaymentProcStatusTypeCd());

		PolicyPaymentTransDTO ufFeb1 = paymentTransactions.get(13);
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), ufFeb1.getCoverageDate());
		assertEquals("Program Type", "UF", ufFeb1.getFinancialProgramTypeCd());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), ufFeb1.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 15, 0, 0), ufFeb1.getPaymentCoverageEndDate());
		assertNull("Payment Amount", ufFeb1.getPaymentAmount());
		assertEquals("TP Amount", new BigDecimal("80.36"), ufFeb1.getTotalPremiumAmount());
		assertNull("Parent PolicyPayment Trans Id", ufFeb1.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", ufFeb1.getTransPeriodTypeCd());
		assertEquals("Trans Type", "PCYC", ufFeb1.getLastPaymentProcStatusTypeCd());

		PolicyPaymentTransDTO ufFeb2 = paymentTransactions.get(14);
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), ufFeb2.getCoverageDate());
		assertEquals("Program Type", "UF", ufFeb2.getFinancialProgramTypeCd());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 16, 0, 0), ufFeb2.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), ufFeb2.getPaymentCoverageEndDate());
		assertNull("Payment Amount", ufFeb2.getPaymentAmount());
		assertEquals("TP Amount", new BigDecimal("116.07"), ufFeb2.getTotalPremiumAmount());
		assertNull("Parent PolicyPayment Trans Id", ufFeb2.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", ufFeb2.getTransPeriodTypeCd());
		assertEquals("Trans Type", "PCYC", ufFeb2.getLastPaymentProcStatusTypeCd());

	}

	/**
	 * Test Scenario3 with Future Cancel of 2/16 premium
	 * 
	 */
	@Test
	public void testCreateRetroPayments_Scenario3_FutureCancel() {
		CodeRecord codeRecord = new CodeRecord("ERC", "2015-02-01 00:00:00", "2015-01-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroPartialMonth_Scenario3_FutureCancel("APPV");
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-06-01", "2015-01-01", "2015-02-15");

		RapServiceTestUtil.loadStateConfigMap(policyVersion.getSubscriberStateCd(),
				policyVersion.getPolicyStartDate().getYear(), ProrationType.FFM_PRORATING);
		
		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);

		List<PolicyPaymentTransDTO> paymentTransactions = response.getPolicyPaymentTransactions();


		assertTrue("PolicyPaymentTransactions", CollectionUtils.isNotEmpty(paymentTransactions)); 
		assertEquals("PolicyPaymentTransactions size", 3, paymentTransactions.size());

		PolicyPaymentTransDTO aptcFeb2Cancel = paymentTransactions.get(0);
		assertEquals("Payment Amount", new BigDecimal("-92.86"), aptcFeb2Cancel.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 16, 0, 0), aptcFeb2Cancel.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), aptcFeb2Cancel.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 7, aptcFeb2Cancel.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", aptcFeb2Cancel.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), aptcFeb2Cancel.getCoverageDate());
		assertEquals("Program Type", "APTC", aptcFeb2Cancel.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", aptcFeb2Cancel.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", aptcFeb2Cancel.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", aptcFeb2Cancel.getMarketplaceGroupPolicyId());


		PolicyPaymentTransDTO csrFeb2Cancel = paymentTransactions.get(1);
		assertEquals("Payment Amount", new BigDecimal("-23.21"), csrFeb2Cancel.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 16, 0, 0), csrFeb2Cancel.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), csrFeb2Cancel.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 8, csrFeb2Cancel.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", csrFeb2Cancel.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), csrFeb2Cancel.getCoverageDate());
		assertEquals("Program Type", "CSR", csrFeb2Cancel.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", csrFeb2Cancel.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", csrFeb2Cancel.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", csrFeb2Cancel.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO ufFeb2Cancel = paymentTransactions.get(2);
		assertEquals("Payment Amount", new BigDecimal("4.06"), ufFeb2Cancel.getPaymentAmount());
		assertEquals("TP Amount", new BigDecimal("250.00"), ufFeb2Cancel.getTotalPremiumAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 16, 0, 0), ufFeb2Cancel.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), ufFeb2Cancel.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 9, ufFeb2Cancel.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", ufFeb2Cancel.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), ufFeb2Cancel.getCoverageDate());
		assertEquals("Program Type", "UF", ufFeb2Cancel.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", ufFeb2Cancel.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", ufFeb2Cancel.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", ufFeb2Cancel.getMarketplaceGroupPolicyId());
	}

	/**
	 * Test Scenario3 with Future Cancel of 2/16 premium
	 * 
	 */
	@Test
	public void testCreateRetroPayments_Scenario3_FutureCancel_PendingCycle() {
		CodeRecord codeRecord = new CodeRecord("ERC", "2015-02-01 00:00:00", "2015-01-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroPartialMonth_Scenario3_FutureCancel("PCYC");
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-06-01", "2015-01-01", "2015-02-15");

		RapServiceTestUtil.loadStateConfigMap(policyVersion.getSubscriberStateCd(),
				policyVersion.getPolicyStartDate().getYear(), ProrationType.FFM_PRORATING);
		
		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);

		List<PolicyPaymentTransDTO> paymentTransactions = response.getPolicyPaymentTransactions();


		assertTrue("PolicyPaymentTransactions", CollectionUtils.isNotEmpty(paymentTransactions)); 
		assertEquals("PolicyPaymentTransactions size", 3, paymentTransactions.size());

		PolicyPaymentTransDTO aptcFeb2Repl = paymentTransactions.get(0);
		assertEquals("Payment Amount", new BigDecimal("92.86"), aptcFeb2Repl.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 16, 0, 0), aptcFeb2Repl.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), aptcFeb2Repl.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", aptcFeb2Repl.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "P", aptcFeb2Repl.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), aptcFeb2Repl.getCoverageDate());
		assertEquals("Program Type", "APTC", aptcFeb2Repl.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "REPL", aptcFeb2Repl.getLastPaymentProcStatusTypeCd());
		assertTrue("Update Status", aptcFeb2Repl.isUpdateStatusRec());
		assertNull("MGP Id", aptcFeb2Repl.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO csrFeb2Repl = paymentTransactions.get(1);
		assertEquals("Payment Amount", new BigDecimal("23.21"), csrFeb2Repl.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 16, 0, 0), csrFeb2Repl.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), csrFeb2Repl.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", csrFeb2Repl.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "P", csrFeb2Repl.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), csrFeb2Repl.getCoverageDate());
		assertEquals("Program Type", "CSR", csrFeb2Repl.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "REPL", csrFeb2Repl.getLastPaymentProcStatusTypeCd());
		assertTrue("Update Status", csrFeb2Repl.isUpdateStatusRec());
		assertNull("MGP Id", csrFeb2Repl.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO ufFeb2Repl = paymentTransactions.get(2);
		assertEquals("Payment Amount", new BigDecimal("-4.06"), ufFeb2Repl.getPaymentAmount());
		assertEquals("TP Amount", new BigDecimal("250.00"), ufFeb2Repl.getTotalPremiumAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 16, 0, 0), ufFeb2Repl.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), ufFeb2Repl.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", ufFeb2Repl.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "P", ufFeb2Repl.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), ufFeb2Repl.getCoverageDate());
		assertEquals("Program Type", "UF", ufFeb2Repl.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "REPL", ufFeb2Repl.getLastPaymentProcStatusTypeCd());
		assertTrue("Update Status", ufFeb2Repl.isUpdateStatusRec());
		assertNull("MGP Id", ufFeb2Repl.getMarketplaceGroupPolicyId());
	}
	
	/**
	 * Test Scenario3 with Future Cancel of 2/16 premium with 2/1 and 2/10 remaining
	 * 
	 */
	@Test
	public void testCreateRetroPayments_Scenario3_FutureCancel_A() {
		CodeRecord codeRecord = new CodeRecord("ERC", "2015-02-01 00:00:00", "2015-01-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroPartialMonth_Scenario3_FutureCancel_A("APPV");
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-06-01", "2015-01-01", "2015-02-15");

		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);
		
		RapServiceTestUtil.loadStateConfigMap(policyVersion.getSubscriberStateCd(),
				policyVersion.getPolicyStartDate().getYear(), ProrationType.FFM_PRORATING);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);

		List<PolicyPaymentTransDTO> paymentTransactions = response.getPolicyPaymentTransactions();


		assertTrue("PolicyPaymentTransactions", CollectionUtils.isNotEmpty(paymentTransactions)); 
		assertEquals("PolicyPaymentTransactions size", 3, paymentTransactions.size());

		PolicyPaymentTransDTO aptcFeb2Cancel = paymentTransactions.get(0);
		assertEquals("Payment Amount", new BigDecimal("-92.86"), aptcFeb2Cancel.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 16, 0, 0), aptcFeb2Cancel.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), aptcFeb2Cancel.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 10, aptcFeb2Cancel.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", aptcFeb2Cancel.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), aptcFeb2Cancel.getCoverageDate());
		assertEquals("Program Type", "APTC", aptcFeb2Cancel.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", aptcFeb2Cancel.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", aptcFeb2Cancel.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", aptcFeb2Cancel.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO csrFeb2Cancel = paymentTransactions.get(1);
		assertEquals("Payment Amount", new BigDecimal("-23.21"), csrFeb2Cancel.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 16, 0, 0), csrFeb2Cancel.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), csrFeb2Cancel.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 11, csrFeb2Cancel.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", csrFeb2Cancel.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), csrFeb2Cancel.getCoverageDate());
		assertEquals("Program Type", "CSR", csrFeb2Cancel.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", csrFeb2Cancel.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", csrFeb2Cancel.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", csrFeb2Cancel.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO ufFeb2Cancel = paymentTransactions.get(2);
		assertEquals("Payment Amount", new BigDecimal("4.06"), ufFeb2Cancel.getPaymentAmount());
		assertEquals("TP Amount", new BigDecimal("250.00"), ufFeb2Cancel.getTotalPremiumAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 16, 0, 0), ufFeb2Cancel.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), ufFeb2Cancel.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 12, ufFeb2Cancel.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", ufFeb2Cancel.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), ufFeb2Cancel.getCoverageDate());
		assertEquals("Program Type", "UF", ufFeb2Cancel.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", ufFeb2Cancel.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", ufFeb2Cancel.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", ufFeb2Cancel.getMarketplaceGroupPolicyId());
	}

	/**
	 * Test Scenario3 with Future Cancel of 2/16 premium with 2/1 and 2/10 remaining
	 * 
	 */
	@Test
	public void testCreateRetroPayments_Scenario3_FutureCancel_A_PendingCycle() {
		CodeRecord codeRecord = new CodeRecord("ERC", "2015-02-01 00:00:00", "2015-01-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroPartialMonth_Scenario3_FutureCancel_A("PCYC");
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-06-01", "2015-01-01", "2015-02-15");

		RapServiceTestUtil.loadStateConfigMap(policyVersion.getSubscriberStateCd(),
				policyVersion.getPolicyStartDate().getYear(), ProrationType.FFM_PRORATING);
		
		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);

		List<PolicyPaymentTransDTO> paymentTransactions = response.getPolicyPaymentTransactions();


		assertTrue("PolicyPaymentTransactions", CollectionUtils.isNotEmpty(paymentTransactions)); 
		assertEquals("PolicyPaymentTransactions size", 3, paymentTransactions.size());

		PolicyPaymentTransDTO aptcFeb2Repl = paymentTransactions.get(0);
		assertEquals("Payment Amount", new BigDecimal("92.86"), aptcFeb2Repl.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 16, 0, 0), aptcFeb2Repl.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), aptcFeb2Repl.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", aptcFeb2Repl.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "P", aptcFeb2Repl.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), aptcFeb2Repl.getCoverageDate());
		assertEquals("Program Type", "APTC", aptcFeb2Repl.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "REPL", aptcFeb2Repl.getLastPaymentProcStatusTypeCd());
		assertTrue("Update Status", aptcFeb2Repl.isUpdateStatusRec());
		assertNull("MGP Id", aptcFeb2Repl.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO csrFeb2Repl = paymentTransactions.get(1);
		assertEquals("Payment Amount", new BigDecimal("23.21"), csrFeb2Repl.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 16, 0, 0), csrFeb2Repl.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), csrFeb2Repl.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", csrFeb2Repl.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "P", csrFeb2Repl.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), csrFeb2Repl.getCoverageDate());
		assertEquals("Program Type", "CSR", csrFeb2Repl.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "REPL", csrFeb2Repl.getLastPaymentProcStatusTypeCd());
		assertTrue("Update Status", csrFeb2Repl.isUpdateStatusRec());
		assertNull("MGP Id", csrFeb2Repl.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO ufFeb2Repl = paymentTransactions.get(2);
		assertNull("Payment Amount", ufFeb2Repl.getPaymentAmount());
		assertEquals("TP Amount", new BigDecimal("250.00"), ufFeb2Repl.getTotalPremiumAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 16, 0, 0), ufFeb2Repl.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 28, 0, 0), ufFeb2Repl.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", ufFeb2Repl.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "P", ufFeb2Repl.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), ufFeb2Repl.getCoverageDate());
		assertEquals("Program Type", "UF", ufFeb2Repl.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "REPL", ufFeb2Repl.getLastPaymentProcStatusTypeCd());
		assertTrue("Update Status", ufFeb2Repl.isUpdateStatusRec());
		assertNull("MGP Id", ufFeb2Repl.getMarketplaceGroupPolicyId());
	}

	@Test
	public void test_createReversalForNonMatchingDates() {

		ReflectionTestUtils.setField(rapProcesssingServiceTest, "reversalRefIds", new ArrayList<Long>());
		ReflectionTestUtils.setField(rapProcesssingServiceTest, "reversedTransIds", new ArrayList<Long>());

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-06-01", "2015-01-01", "2015-01-31");

		DateTime coverageDt = new DateTime("2015-02-01");

		List<PolicyPaymentTransDTO>  paymentTransactions = new ArrayList<PolicyPaymentTransDTO>();

		PolicyPaymentTransDTO payment = RapServiceTestUtil.createMockPolicyPayment(1L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-10", "APTC", new BigDecimal("35.71"), "APPV");

		ReflectionTestUtils.invokeMethod(
				rapProcesssingServiceTest, "createReversalForNonMatchingDates", paymentTransactions, coverageDt, new DateTime("2015-02-16"), "APTC", policyVersion, payment);

		assertTrue("PolicyPaymentTransactions", CollectionUtils.isNotEmpty(paymentTransactions)); 
		assertEquals("PolicyPaymentTransactions size", 1, paymentTransactions.size());

		PolicyPaymentTransDTO aptcFebCancel = paymentTransactions.get(0);
		assertEquals("Payment Amount", new BigDecimal("-35.71"), aptcFebCancel.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), aptcFebCancel.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 10, 0, 0), aptcFebCancel.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 1, aptcFebCancel.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", aptcFebCancel.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), aptcFebCancel.getCoverageDate());
		assertEquals("Program Type", "APTC", aptcFebCancel.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", aptcFebCancel.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", aptcFebCancel.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", aptcFebCancel.getMarketplaceGroupPolicyId());
	}

	@Test
	public void test_createReversalForNonMatchingDates_Cancel_StartDt_EQ_EndDt() {

		ReflectionTestUtils.setField(rapProcesssingServiceTest, "reversalRefIds", new ArrayList<Long>());
		ReflectionTestUtils.setField(rapProcesssingServiceTest, "reversedTransIds", new ArrayList<Long>());

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-06-01", "2015-01-01", "2015-01-31");
		policyVersion.setPolicyCancelled(true);

		DateTime coverageDt = new DateTime("2015-02-01");

		List<PolicyPaymentTransDTO>  paymentTransactions = new ArrayList<PolicyPaymentTransDTO>();

		PolicyPaymentTransDTO payment = RapServiceTestUtil.createMockPolicyPayment(1L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-10", "APTC", new BigDecimal("35.71"), "APPV");

		ReflectionTestUtils.invokeMethod(
				rapProcesssingServiceTest, "createReversalForNonMatchingDates", paymentTransactions, coverageDt, new DateTime("2015-02-16"), "APTC", policyVersion, payment);

		assertTrue("PolicyPaymentTransactions", CollectionUtils.isNotEmpty(paymentTransactions)); 
		assertEquals("PolicyPaymentTransactions size", 1, paymentTransactions.size());

		PolicyPaymentTransDTO aptcFebCancel = paymentTransactions.get(0);
		assertEquals("Payment Amount", new BigDecimal("-35.71"), aptcFebCancel.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), aptcFebCancel.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 10, 0, 0), aptcFebCancel.getPaymentCoverageEndDate());
		assertEquals("Parent PolicyPayment Trans Id", 1, aptcFebCancel.getParentPolicyPaymentTransId().longValue());
		assertEquals("Trans Type", "R", aptcFebCancel.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), aptcFebCancel.getCoverageDate());
		assertEquals("Program Type", "APTC", aptcFebCancel.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", aptcFebCancel.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", aptcFebCancel.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", aptcFebCancel.getMarketplaceGroupPolicyId());
	}

	@Test
	public void test_createReversalForNonMatchingDates_PendingCycle() {

		ReflectionTestUtils.setField(rapProcesssingServiceTest, "reversalRefIds", new ArrayList<Long>());
		ReflectionTestUtils.setField(rapProcesssingServiceTest, "reversedTransIds", new ArrayList<Long>());

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-06-01", "2015-01-01", "2015-01-31");

		DateTime coverageDt = new DateTime("2015-02-01");

		List<PolicyPaymentTransDTO>  paymentTransactions = new ArrayList<PolicyPaymentTransDTO>();

		PolicyPaymentTransDTO payment = RapServiceTestUtil.createMockPolicyPayment(1L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-10", "APTC", new BigDecimal("35.71"), "PCYC");

		ReflectionTestUtils.invokeMethod(
				rapProcesssingServiceTest, "createReversalForNonMatchingDates", paymentTransactions, coverageDt, new DateTime("2015-02-16"), "APTC", policyVersion, payment);

		assertTrue("PolicyPaymentTransactions", CollectionUtils.isNotEmpty(paymentTransactions)); 
		assertEquals("PolicyPaymentTransactions size", 1, paymentTransactions.size());

		PolicyPaymentTransDTO aptcFebCancel = paymentTransactions.get(0);
		assertEquals("Payment Amount", new BigDecimal("35.71"), aptcFebCancel.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), aptcFebCancel.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 10, 0, 0), aptcFebCancel.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", aptcFebCancel.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", aptcFebCancel.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), aptcFebCancel.getCoverageDate());
		assertEquals("Program Type", "APTC", aptcFebCancel.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "REPL", aptcFebCancel.getLastPaymentProcStatusTypeCd());
		assertNull("MGP Id", aptcFebCancel.getMarketplaceGroupPolicyId());
	}

	@Test
	public void test_createReversalForNonMatchingDates_PendingCycleHavingMgpId() {

		ReflectionTestUtils.setField(rapProcesssingServiceTest, "reversalRefIds", new ArrayList<Long>());
		ReflectionTestUtils.setField(rapProcesssingServiceTest, "reversedTransIds", new ArrayList<Long>());

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-06-01", "2015-01-01", "2015-01-31");

		DateTime coverageDt = new DateTime("2015-02-01");

		List<PolicyPaymentTransDTO>  paymentTransactions = new ArrayList<PolicyPaymentTransDTO>();

		PolicyPaymentTransDTO payment = RapServiceTestUtil.createMockPolicyPayment(1L, 1L, "101", "R", "2015-02-01", "2015-02-01", "2015-02-10", "APTC", new BigDecimal("35.71"), "PCYC");
		payment.setMarketplaceGroupPolicyId("MGPID_PREV");
		
		ReflectionTestUtils.invokeMethod(
				rapProcesssingServiceTest, "createReversalForNonMatchingDates", paymentTransactions, coverageDt, new DateTime("2015-02-16"), "APTC", policyVersion, payment);

		assertTrue("PolicyPaymentTransactions", CollectionUtils.isNotEmpty(paymentTransactions)); 
		assertEquals("PolicyPaymentTransactions size", 1, paymentTransactions.size());

		PolicyPaymentTransDTO aptcFebCancel = paymentTransactions.get(0);
		assertEquals("Payment Amount", new BigDecimal("35.71"), aptcFebCancel.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", new DateTime(2015, 2, 1, 0, 0), aptcFebCancel.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", new DateTime(2015, 2, 10, 0, 0), aptcFebCancel.getPaymentCoverageEndDate());
		assertNull("Parent PolicyPayment Trans Id", aptcFebCancel.getParentPolicyPaymentTransId());
		assertEquals("Trans Type", "R", aptcFebCancel.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), aptcFebCancel.getCoverageDate());
		assertEquals("Program Type", "APTC", aptcFebCancel.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "REPL", aptcFebCancel.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", aptcFebCancel.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID_PREV", aptcFebCancel.getMarketplaceGroupPolicyId());
	}

	/**
	 * Tests Payments are created for a 1 day EFFECTUATED policy on February 29 (leap year)
	 */
	@Test
	public void testCreateRetroPayments_Start_EQ_End_LeapYear() {

		DateTime FEB_1 = new DateTime("2016-02-01");
		DateTime sd = new DateTime("2016-02-29");
		DateTime ed = new DateTime("2016-02-29");
		double dblAPTC = 11.11;
		double dblCSR = .29;
		double dblTPA = 99.99;
		int daysInMonth = 29;
		int numberOfDays = 1;
		BigDecimal aptc = new BigDecimal(dblAPTC);
		BigDecimal csr = new BigDecimal(dblCSR);
		BigDecimal tpa = new BigDecimal(dblTPA);

		int expectedNumPayments = 3;
		BigDecimal expectedAptcPA = new BigDecimal(numberOfDays * dblAPTC / daysInMonth);
		BigDecimal expectedCsrPA = new BigDecimal(numberOfDays * dblCSR / daysInMonth);
		BigDecimal expectedTPA = new BigDecimal(numberOfDays * dblTPA / daysInMonth);		

		CodeRecord codeRecord = new CodeRecord("ERC", "2016-04-01 00:00:00", "2016-03-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentData(sd, ed, aptc, csr, tpa);
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2016-03-01", sd.toString(), ed.toString());
		//Status
		policyVersion.setPolicyStatus(PolicyStatus.EFFECTUATED_2.getValue());
		
		RapServiceTestUtil.loadStateConfigMap(policyVersion.getSubscriberStateCd(),
				policyVersion.getPolicyStartDate().getYear(), ProrationType.FFM_PRORATING);
		
		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);

		List<PolicyPaymentTransDTO> paymentTransactions = response.getPolicyPaymentTransactions();

		assertTrue("PolicyPaymentTransactions", CollectionUtils.isNotEmpty(paymentTransactions)); 
		assertEquals("PolicyPaymentTransactions size", expectedNumPayments, paymentTransactions.size());

		PolicyPaymentTransDTO pptAPTC = paymentTransactions.get(0);
		assertEquals("Payment Amount", expectedAptcPA.setScale(2, RoundingMode.HALF_UP), pptAPTC.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", sd, pptAPTC.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", ed, pptAPTC.getPaymentCoverageEndDate());
		assertEquals("Trans Type", "R", pptAPTC.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", FEB_1 , pptAPTC.getCoverageDate());
		assertEquals("Program Type", "APTC", pptAPTC.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", pptAPTC.getLastPaymentProcStatusTypeCd());

		PolicyPaymentTransDTO pptCSR = paymentTransactions.get(1);
		assertEquals("Payment Amount", expectedCsrPA.setScale(2, RoundingMode.HALF_UP), pptCSR.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", sd, pptCSR.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", ed, pptCSR.getPaymentCoverageEndDate());
		assertEquals("Trans Type", "R", pptCSR.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", FEB_1, pptCSR.getCoverageDate());
		assertEquals("Program Type", "CSR", pptCSR.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", pptCSR.getLastPaymentProcStatusTypeCd());

		PolicyPaymentTransDTO pptPA = paymentTransactions.get(2);
		assertEquals("Payment Amount", expectedTPA.setScale(2, RoundingMode.HALF_UP), pptPA.getTotalPremiumAmount());
		assertEquals("Payment Coverage Start Date", sd, pptPA.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", ed, pptPA.getPaymentCoverageEndDate());
		assertEquals("Trans Type", "R", pptPA.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", FEB_1, pptPA.getCoverageDate());
		assertEquals("Program Type", "UF", pptPA.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", pptPA.getLastPaymentProcStatusTypeCd());	
	}


	/**
	 * Tests Payments are created for a 2 day EFFECTUATED policy from February 28th to 29th. (leap year)
	 */
	@Test
	public void testCreateRetroPayments_2_Day_Policy_LeapYear() {

		DateTime FEB_1 = new DateTime("2016-02-01");
		DateTime sd = new DateTime("2016-02-28");
		DateTime ed = new DateTime("2016-02-29");
		double dblAPTC = 11.11;
		double dblCSR = .29;
		double dblTPA = 99.99;
		int daysInMonth = 29;
		int numberOfDays = 2;
		BigDecimal aptc = new BigDecimal(dblAPTC);
		BigDecimal csr = new BigDecimal(dblCSR);
		BigDecimal tpa = new BigDecimal(dblTPA);

		int expectedNumPayments = 3;
		BigDecimal expectedAptcPA = new BigDecimal(numberOfDays * dblAPTC / daysInMonth);
		BigDecimal expectedCsrPA = new BigDecimal(numberOfDays * dblCSR / daysInMonth);
		BigDecimal expectedTPA = new BigDecimal(numberOfDays * dblTPA / daysInMonth);


		CodeRecord codeRecord = new CodeRecord("ERC", "2016-04-01 00:00:00", "2016-03-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentData(sd, ed, aptc, csr, tpa);
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2016-03-01", sd.toString(), ed.toString());
		policyVersion.setPolicyStatus(PolicyStatus.EFFECTUATED_2.getValue());
		
		RapServiceTestUtil.loadStateConfigMap(policyVersion.getSubscriberStateCd(),
				policyVersion.getPolicyStartDate().getYear(), ProrationType.FFM_PRORATING);
		
		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);

		List<PolicyPaymentTransDTO> paymentTransactions = response.getPolicyPaymentTransactions();

		assertTrue("PolicyPaymentTransactions", CollectionUtils.isNotEmpty(paymentTransactions)); 
		assertEquals("PolicyPaymentTransactions size", expectedNumPayments, paymentTransactions.size());

		PolicyPaymentTransDTO pptAPTC = paymentTransactions.get(0);
		assertEquals("Payment Amount", expectedAptcPA.setScale(2, RoundingMode.HALF_UP), pptAPTC.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", sd, pptAPTC.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", ed, pptAPTC.getPaymentCoverageEndDate());
		assertEquals("Trans Type", "R", pptAPTC.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", FEB_1 , pptAPTC.getCoverageDate());
		assertEquals("Program Type", "APTC", pptAPTC.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", pptAPTC.getLastPaymentProcStatusTypeCd());

		PolicyPaymentTransDTO pptCSR = paymentTransactions.get(1);
		assertEquals("Payment Amount", expectedCsrPA.setScale(2, RoundingMode.HALF_UP), pptCSR.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", sd, pptCSR.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", ed, pptCSR.getPaymentCoverageEndDate());
		assertEquals("Trans Type", "R", pptCSR.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", FEB_1, pptCSR.getCoverageDate());
		assertEquals("Program Type", "CSR", pptCSR.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", pptCSR.getLastPaymentProcStatusTypeCd());

		PolicyPaymentTransDTO pptPA = paymentTransactions.get(2);
		assertEquals("Payment Amount", expectedTPA.setScale(2, RoundingMode.HALF_UP), pptPA.getTotalPremiumAmount());
		assertEquals("Payment Coverage Start Date", sd, pptPA.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", ed, pptPA.getPaymentCoverageEndDate());
		assertEquals("Trans Type", "R", pptPA.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", FEB_1, pptPA.getCoverageDate());
		assertEquals("Program Type", "UF", pptPA.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", pptPA.getLastPaymentProcStatusTypeCd());	
	}

	/**
	 * Tests Payments are created for a 2 day EFFECTUATED policy from February 28th to 29th. (leap year)
	 */
	@Test
	public void testCreateRetroPayments_2_Day_Policy_LeapYear_CANCELLED() {

		DateTime FEB_1 = new DateTime("2016-02-01");
		DateTime sd = new DateTime("2016-02-28");
		DateTime ed = new DateTime("2016-02-29");
		double dblAPTC = 22.22;
		double dblCSR = 1.45;
		double dblTPA = 88.88;
		int daysInMonth = 29;
		int numberOfDays = 2;
		BigDecimal aptc = new BigDecimal(dblAPTC);
		BigDecimal csr = new BigDecimal(dblCSR);
		BigDecimal tpa = new BigDecimal(dblTPA);

		int expectedNumPayments = 3;
		BigDecimal expectedAptcPA = new BigDecimal(numberOfDays * dblAPTC / daysInMonth);
		BigDecimal expectedCsrPA = new BigDecimal(numberOfDays * dblCSR / daysInMonth);
		BigDecimal expectedTPA = new BigDecimal(numberOfDays * dblTPA / daysInMonth);


		CodeRecord codeRecord = new CodeRecord("ERC", "2016-04-01 00:00:00", "2016-03-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentData(sd, ed, aptc, csr, tpa);
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2016-03-01", sd.toString(), ed.toString());
		policyVersion.setPolicyStatus(PolicyStatus.EFFECTUATED_2.getValue());
		
		RapServiceTestUtil.loadStateConfigMap(policyVersion.getSubscriberStateCd(),
				policyVersion.getPolicyStartDate().getYear(), ProrationType.FFM_PRORATING);
		
		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);

		List<PolicyPaymentTransDTO> paymentTransactions = response.getPolicyPaymentTransactions();

		assertTrue("PolicyPaymentTransactions", CollectionUtils.isNotEmpty(paymentTransactions)); 
		assertEquals("PolicyPaymentTransactions size", expectedNumPayments, paymentTransactions.size());

		PolicyPaymentTransDTO pptAPTC = paymentTransactions.get(0);
		assertEquals("Payment Amount", expectedAptcPA.setScale(2, RoundingMode.HALF_UP), pptAPTC.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", sd, pptAPTC.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", ed, pptAPTC.getPaymentCoverageEndDate());
		assertEquals("Trans Type", "R", pptAPTC.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", FEB_1 , pptAPTC.getCoverageDate());
		assertEquals("Program Type", "APTC", pptAPTC.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", pptAPTC.getLastPaymentProcStatusTypeCd());

		PolicyPaymentTransDTO pptCSR = paymentTransactions.get(1);
		assertEquals("Payment Amount", expectedCsrPA.setScale(2, RoundingMode.HALF_UP), pptCSR.getPaymentAmount());
		assertEquals("Payment Coverage Start Date", sd, pptCSR.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", ed, pptCSR.getPaymentCoverageEndDate());
		assertEquals("Trans Type", "R", pptCSR.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", FEB_1, pptCSR.getCoverageDate());
		assertEquals("Program Type", "CSR", pptCSR.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", pptCSR.getLastPaymentProcStatusTypeCd());

		PolicyPaymentTransDTO pptPA = paymentTransactions.get(2);
		assertEquals("Payment Amount", expectedTPA.setScale(2, RoundingMode.HALF_UP), pptPA.getTotalPremiumAmount());
		assertEquals("Payment Coverage Start Date", sd, pptPA.getPaymentCoverageStartDate());
		assertEquals("Payment Coverage End Date", ed, pptPA.getPaymentCoverageEndDate());
		assertEquals("Trans Type", "R", pptPA.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", FEB_1, pptPA.getCoverageDate());
		assertEquals("Program Type", "UF", pptPA.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", pptPA.getLastPaymentProcStatusTypeCd());	
	}


	/**
	 * Tests Payments are NOT created for a 1 day CANCELLED policy on February 29 (leap year)
	 */
	@Test
	public void testCreateRetroPayments_Start_EQ_End_LeapYear_CANCEL() {

		DateTime sd = new DateTime("2016-02-29");
		DateTime ed = new DateTime("2016-02-29");
		double dblAPTC = 11.11;
		double dblCSR = .29;
		double dblTPA = 99.99;
		BigDecimal aptc = new BigDecimal(dblAPTC);
		BigDecimal csr = new BigDecimal(dblCSR);
		BigDecimal tpa = new BigDecimal(dblTPA);

		int expectedNumPayments = 0;

		CodeRecord codeRecord = new CodeRecord("ERC", "2016-04-01 00:00:00", "2016-03-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentData(sd, ed, aptc, csr, tpa);
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2016-03-01", sd.toString(), ed.toString());
		//Status
		policyVersion.setPolicyStatus(PolicyStatus.CANCELLED_3.getValue());
		
		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);

		List<PolicyPaymentTransDTO> paymentTransactions = response.getPolicyPaymentTransactions();

		assertEquals("PolicyPaymentTransactions size", expectedNumPayments, paymentTransactions.size());		
	}

	/**
	 * Tests Payments are NOT created for a 1 day CANCELLED policy on January 1.
	 */
	@Test
	public void testCreateRetroPayments_Start_EQ_End_CANCEL() {

		DateTime sd = new DateTime("2016-01-01");
		DateTime ed = new DateTime("2016-01-01");
		double dblAPTC = 11.11;
		double dblCSR = .29;
		double dblTPA = 99.99;
		BigDecimal aptc = new BigDecimal(dblAPTC);
		BigDecimal csr = new BigDecimal(dblCSR);
		BigDecimal tpa = new BigDecimal(dblTPA);

		int expectedNumPayments = 0;

		CodeRecord codeRecord = new CodeRecord("ERC", "2016-02-01 00:00:00", "2016-01-11 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentData(sd, ed, aptc, csr, tpa);
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2016-03-01", sd.toString(), ed.toString());
		policyVersion.setPolicyStatus(PolicyStatus.CANCELLED_3.getValue());
		
		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);

		List<PolicyPaymentTransDTO> paymentTransactions = response.getPolicyPaymentTransactions();

		assertEquals("PolicyPaymentTransactions size", expectedNumPayments, paymentTransactions.size());		
	}

	@Test
	public void testCreateAdjustmentForMatchingDates_1() {

		List<PolicyPaymentTransDTO>  pmtTransList = new ArrayList<PolicyPaymentTransDTO>();
		DateTime coverageDate = new DateTime("2017-02-01");
		String programType = "APTC";
		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-06-01", "2015-01-01", "2015-02-15");

		PolicyPaymentTransDTO payment = new PolicyPaymentTransDTO();
		PolicyPremium policyPremium = new PolicyPremium();
		BigDecimal epsAmount = new BigDecimal(10);
		BigDecimal proratedAmount = new BigDecimal(200.00);

		ReflectionTestUtils.setField(rapProcesssingServiceTest, "reversedTransIds", new ArrayList<Long>());

		payment.setPolicyPaymentTransId(1L);
		payment.setLastPaymentProcStatusTypeCd("PCYC");
		payment.setPaymentProcStatusTypeCd("REPL");
		payment.setUpdateStatusRec(true);
		payment.setMaintenanceStartDateTime(policyVersion.getMaintenanceStartDateTime());
		payment.setPaymentCoverageStartDate(coverageDate);
		pmtTransList.add(payment);

		ReflectionTestUtils.invokeMethod(rapProcesssingServiceTest, "createAdjustmentForMatchingDates", pmtTransList, 
				coverageDate, programType, policyVersion, payment, policyPremium, epsAmount, proratedAmount, null);

		assertEquals("PolicyPaymentTransList Size", 2, pmtTransList.size());

		PolicyPaymentTransDTO actualPptDTO1 = pmtTransList.get(0);

		assertEquals("PolicyPaymentTransList1 PolicyPaymentTransId", 1, actualPptDTO1.getPolicyPaymentTransId().intValue());
		assertEquals("PolicyPaymentTransList1 LastPaymentProcStatusTypeCd","REPL",actualPptDTO1.getLastPaymentProcStatusTypeCd());
		assertEquals("PolicyPaymentTransList1 PaymentProcStatusTypeCd", "REPL", actualPptDTO1.getPaymentProcStatusTypeCd());
		assertEquals("PolicyPaymentTransList1 UpdateStatusRec", true, actualPptDTO1.isUpdateStatusRec());
		assertEquals("PolicyPaymentTransList1 MaintenanceStartDateTime",new DateTime(2015, 06, 01, 0, 0), actualPptDTO1.getMaintenanceStartDateTime());

		PolicyPaymentTransDTO actualPptDTO2 = pmtTransList.get(1);

		assertEquals("PolicyPaymentTransList2 PolicyPaymentTransId", 1, actualPptDTO2.getPolicyPaymentTransId().intValue());
		assertEquals("PolicyPaymentTransList2 LastPaymentProcStatusTypeCd","REPL",actualPptDTO2.getLastPaymentProcStatusTypeCd());
		assertEquals("PolicyPaymentTransList2 PaymentProcStatusTypeCd", "REPL", actualPptDTO2.getPaymentProcStatusTypeCd());
		assertEquals("PolicyPaymentTransList2 UpdateStatusRec", true, actualPptDTO2.isUpdateStatusRec());
		assertEquals("PolicyPaymentTransList2 MaintenanceStartDateTime",new DateTime(2015, 06, 01, 0, 0), actualPptDTO2.getMaintenanceStartDateTime());

	}

	@Test
	public void testCreateAdjustmentForMatchingDates_2() {


		List<PolicyPaymentTransDTO>  pmtTransList = new ArrayList<PolicyPaymentTransDTO>();
		DateTime coverageDate = new DateTime("2020-01-01");
		String programType = "APTC";
		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-06-01", "2020-01-01", "2015-02-15");

		PolicyPaymentTransDTO payment = new PolicyPaymentTransDTO();
		PolicyPremium policyPremium = new PolicyPremium();
		BigDecimal epsAmount = new BigDecimal(100.00);
		BigDecimal proratedAmount = new BigDecimal(200.00);

		ReflectionTestUtils.setField(rapProcesssingServiceTest, "reversedTransIds", new ArrayList<Long>());

		payment.setPolicyPaymentTransId(1L);
		payment.setLastPaymentProcStatusTypeCd("PCYC");
		payment.setPaymentProcStatusTypeCd("REPL");
		payment.setUpdateStatusRec(true);
		payment.setMaintenanceStartDateTime(policyVersion.getMaintenanceStartDateTime());
		payment.setPaymentCoverageStartDate(coverageDate);
		pmtTransList.add(payment);

		ReflectionTestUtils.invokeMethod(rapProcesssingServiceTest, "createAdjustmentForMatchingDates", pmtTransList, 
				coverageDate, programType, policyVersion, payment, policyPremium, epsAmount, proratedAmount, null);

		assertEquals("PolicyPaymentTransList Size", 2, pmtTransList.size());

		PolicyPaymentTransDTO actualPptDTO1 = pmtTransList.get(0);

		assertEquals("PolicyPaymentTransList1 PolicyPaymentTransId", 1, actualPptDTO1.getPolicyPaymentTransId().intValue());
		assertEquals("PolicyPaymentTransList1 LastPaymentProcStatusTypeCd","REPL",actualPptDTO1.getLastPaymentProcStatusTypeCd());
		assertEquals("PolicyPaymentTransList1 PaymentProcStatusTypeCd", "REPL", actualPptDTO1.getPaymentProcStatusTypeCd());
		assertEquals("PolicyPaymentTransList1 UpdateStatusRec", true, actualPptDTO1.isUpdateStatusRec());
		assertEquals("PolicyPaymentTransList1 MaintenanceStartDateTime",new DateTime(2015, 06, 01, 0, 0), actualPptDTO1.getMaintenanceStartDateTime());

		PolicyPaymentTransDTO actualPptDTO2 = pmtTransList.get(1);

		assertEquals("PolicyPaymentTransList2 PolicyPaymentTransId", 1, actualPptDTO2.getPolicyPaymentTransId().intValue());
		assertEquals("PolicyPaymentTransList2 LastPaymentProcStatusTypeCd","REPL",actualPptDTO2.getLastPaymentProcStatusTypeCd());
		assertEquals("PolicyPaymentTransList2 PaymentProcStatusTypeCd", "REPL", actualPptDTO2.getPaymentProcStatusTypeCd());
		assertEquals("PolicyPaymentTransList2 UpdateStatusRec", true, actualPptDTO2.isUpdateStatusRec());
		assertEquals("PolicyPaymentTransList2 MaintenanceStartDateTime",new DateTime(2015, 06, 01, 0, 0), actualPptDTO2.getMaintenanceStartDateTime());


	}

	@Test
	public void testCreateAdjustmentForMatchingDates_3() {

		List<PolicyPaymentTransDTO>  pmtTransList = new ArrayList<PolicyPaymentTransDTO>();
		DateTime coverageDate = new DateTime("2015-01-01");
		String programType = "APTC";
		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-01-01", "2015-01-01", "2015-01-12");

		PolicyPaymentTransDTO payment = new PolicyPaymentTransDTO();
		PolicyPremium policyPremium = new PolicyPremium();
		BigDecimal epsAmount = new BigDecimal(100.00);
		BigDecimal proratedAmount = new BigDecimal(200.00);

		ReflectionTestUtils.setField(rapProcesssingServiceTest, "reversedTransIds", new ArrayList<Long>());

		payment.setPaymentCoverageStartDate(coverageDate);
		payment.setPolicyPaymentTransId(10L);
		payment.setExchangePolicyId(policyVersion.getExchangePolicyId());
		payment.setSubscriberStateCd(policyVersion.getSubscriberStateCd());
		payment.setFinancialProgramTypeCd(programType);
		payment.setCoverageDate(coverageDate);
		payment.setMaintenanceStartDateTime(policyVersion.getMaintenanceStartDateTime());
		payment.setIssuerHiosId(policyVersion.getIssuerHiosId());
		payment.setPolicyVersionId(policyVersion.getPolicyVersionId());
		payment.setTransPeriodTypeCd(TRANSPERIOD_RETROACTIVE);        
		payment.setInsrncAplctnTypeCd(policyVersion.getInsrncAplctnTypeCd());
		payment.setIssuerStateCd("Virginia");  
		policyPremium.setEffectiveStartDate(coverageDate);
		//set transaction status
		payment.setPaymentProcStatusTypeCd(STATUS_PENDING_CYCLE);
		payment.setLastPaymentProcStatusTypeCd(STATUS_PENDING_CYCLE);

		pmtTransList.add(payment);

		ReflectionTestUtils.invokeMethod(rapProcesssingServiceTest, "createAdjustmentForMatchingDates", pmtTransList, 
				coverageDate, programType, policyVersion, payment, policyPremium, epsAmount, proratedAmount, ProrationType.NON_PRORATING);

		assertEquals("PolicyPaymentTransList Size", 3, pmtTransList.size());

		PolicyPaymentTransDTO actualPptDTO1 = pmtTransList.get(0);
		PolicyPremium actualPolicyPremium1 = policyPremium;

		assertEquals("PolicyPaymentTransList1 PaymentCoverageStartDate", new DateTime(2015, 01, 01, 0, 0), actualPptDTO1.getPaymentCoverageStartDate());
		assertEquals("PolicyPaymentTransList1 PolicyPaymentTransId", 10, actualPptDTO1.getPolicyPaymentTransId().intValue());	
		assertEquals("PolicyPaymentTransList1 ExchangePolicyId", "101", actualPptDTO1.getExchangePolicyId());
		assertEquals("PolicyPaymentTransList1 SubscriberStateCd", "NY", actualPptDTO1.getSubscriberStateCd());
		assertEquals("PolicyPaymentTransList1 FinancialProgramTypeCd", programType, actualPptDTO1.getFinancialProgramTypeCd());
		assertEquals("PolicyPaymentTransList1 CoverageDate", coverageDate, actualPptDTO1.getCoverageDate());
		assertEquals("PolicyPaymentTransList1 MaintenanceStartDateTime",new DateTime(2015, 01, 01, 0, 0), actualPptDTO1.getMaintenanceStartDateTime());
		assertNull("PolicyPaymentTransList1 IssuerHiosId", actualPptDTO1.getIssuerHiosId());
		assertEquals("PolicyPaymentTransList1 setPolicyVersionId", 1, actualPptDTO1.getPolicyVersionId().intValue());
		assertEquals("PolicyPaymentTransList1 TransPeriodTypeCd", TRANSPERIOD_RETROACTIVE, actualPptDTO1.getTransPeriodTypeCd());
		assertEquals("PolicyPaymentTransList1 setInsrncAplctnTypeCd", "1", actualPptDTO1.getInsrncAplctnTypeCd());
		assertEquals("PolicyPaymentTransList1 setIssuerStateCd","Virginia",actualPptDTO1.getIssuerStateCd());
		assertEquals("PolicyPaymentTransList1 EffectiveStartDate", coverageDate, actualPolicyPremium1.getEffectiveStartDate());
		assertEquals("PolicyPaymentTransList1 PaymentProcStatusTypeCd", "REPL", actualPptDTO1.getPaymentProcStatusTypeCd());
		assertEquals("PolicyPaymentTransList1 LastPaymentProcStatusTypeCd","REPL",actualPptDTO1.getLastPaymentProcStatusTypeCd());
		assertNull("MGP Id", actualPptDTO1.getMarketplaceGroupPolicyId());
		
		
		PolicyPaymentTransDTO actualPptDTO2 = pmtTransList.get(1);
		PolicyPremium actualPolicyPremium2 = policyPremium;

		assertEquals("PolicyPaymentTransList2 PaymentCoverageStartDate", new DateTime(2015, 01, 01, 0, 0), actualPptDTO2.getPaymentCoverageStartDate());
		assertEquals("PolicyPaymentTransList2 PolicyPaymentTransId", 10, actualPptDTO2.getPolicyPaymentTransId().intValue());	
		assertEquals("PolicyPaymentTransList2 ExchangePolicyId", "101", actualPptDTO2.getExchangePolicyId());
		assertEquals("PolicyPaymentTransList2 SubscriberStateCd", "NY", actualPptDTO2.getSubscriberStateCd());
		assertEquals("PolicyPaymentTransList2 FinancialProgramTypeCd", programType, actualPptDTO2.getFinancialProgramTypeCd());
		assertEquals("PolicyPaymentTransList2 CoverageDate", coverageDate, actualPptDTO2.getCoverageDate());
		assertEquals("PolicyPaymentTransList2 MaintenanceStartDateTime",new DateTime(2015, 01, 01, 0, 0), actualPptDTO2.getMaintenanceStartDateTime());
		assertNull("PolicyPaymentTransList2 IssuerHiosId", actualPptDTO2.getIssuerHiosId());
		assertEquals("PolicyPaymentTransList2 setPolicyVersionId", 1, actualPptDTO2.getPolicyVersionId().intValue());
		assertEquals("PolicyPaymentTransList2 TransPeriodTypeCd", TRANSPERIOD_RETROACTIVE, actualPptDTO2.getTransPeriodTypeCd());
		assertEquals("PolicyPaymentTransList2 setInsrncAplctnTypeCd", "1", actualPptDTO2.getInsrncAplctnTypeCd());
		assertEquals("PolicyPaymentTransList2 setIssuerStateCd","Virginia",actualPptDTO2.getIssuerStateCd());
		assertEquals("PolicyPaymentTransList2 EffectiveStartDate", coverageDate, actualPolicyPremium2.getEffectiveStartDate());
		assertEquals("PolicyPaymentTransList2 PaymentProcStatusTypeCd", "REPL", actualPptDTO2.getPaymentProcStatusTypeCd());
		assertEquals("PolicyPaymentTransList2 LastPaymentProcStatusTypeCd","REPL",actualPptDTO2.getLastPaymentProcStatusTypeCd());
		assertNull("MGP Id", actualPptDTO2.getMarketplaceGroupPolicyId());
		
		PolicyPaymentTransDTO actualPptDTO3 = pmtTransList.get(2);
		PolicyPremium actualPolicyPremium3 = policyPremium;

		assertEquals("PolicyPaymentTransList3 PaymentCoverageStartDate", new DateTime(2015, 01, 01, 0, 0), actualPptDTO3.getPaymentCoverageStartDate());
		assertNull("PolicyPaymentTransList3 PolicyPaymentTransId", actualPptDTO3.getPolicyPaymentTransId());	
		assertEquals("PolicyPaymentTransList3 ExchangePolicyId", "101", actualPptDTO3.getExchangePolicyId());
		assertEquals("PolicyPaymentTransList3 SubscriberStateCd", "NY", actualPptDTO3.getSubscriberStateCd());
		assertEquals("PolicyPaymentTransList3 FinancialProgramTypeCd", programType, actualPptDTO3.getFinancialProgramTypeCd());
		assertEquals("PolicyPaymentTransList3 CoverageDate", coverageDate, actualPptDTO3.getCoverageDate());
		assertEquals("PolicyPaymentTransList3 MaintenanceStartDateTime",new DateTime(2015, 01, 01, 0, 0), actualPptDTO3.getMaintenanceStartDateTime());
		assertNull("PolicyPaymentTransList3 IssuerHiosId", actualPptDTO3.getIssuerHiosId());
		assertEquals("PolicyPaymentTransList3 setPolicyVersionId", 1, actualPptDTO3.getPolicyVersionId().intValue());
		assertEquals("PolicyPaymentTransList3 TransPeriodTypeCd", TRANSPERIOD_RETROACTIVE, actualPptDTO3.getTransPeriodTypeCd());
		assertEquals("PolicyPaymentTransList3 setInsrncAplctnTypeCd", "1", actualPptDTO3.getInsrncAplctnTypeCd());
		assertEquals("PolicyPaymentTransList3 setIssuerStateCd","NY",actualPptDTO3.getIssuerStateCd());
		assertEquals("PolicyPaymentTransList3 EffectiveStartDate", coverageDate, actualPolicyPremium3.getEffectiveStartDate());
		assertEquals("PolicyPaymentTransList3 PaymentProcStatusTypeCd", "PCYC", actualPptDTO3.getPaymentProcStatusTypeCd());
		assertEquals("PolicyPaymentTransList3 LastPaymentProcStatusTypeCd","PCYC",actualPptDTO3.getLastPaymentProcStatusTypeCd());
		assertNotNull("MGP Id", actualPptDTO3.getMarketplaceGroupPolicyId());
		assertEquals("MGP Id", "MGPID", actualPptDTO3.getMarketplaceGroupPolicyId());
	}

	@Test
	public void testCreateReversalPaymentTrans_PaymentAmount() {

		DateTime coverageDate = new DateTime("2015-01-01");
		String financialProgramTypeCd = "APTC";
		PolicyDataDTO policy = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-01-01", "2015-01-01", "2015-01-12");
		PolicyPaymentTransDTO paymentTrans = new PolicyPaymentTransDTO();
		Long reversalRefId = 200L; 

		ReflectionTestUtils.setField(rapProcesssingServiceTest, "reversedTransIds", new ArrayList<Long>());

		PolicyPaymentTransDTO actual = ReflectionTestUtils.invokeMethod(rapProcesssingServiceTest, "createReversalPaymentTrans", coverageDate, 
				financialProgramTypeCd, policy, paymentTrans, reversalRefId);

		assertNull("PaymentAmountNull", actual.getPaymentAmount());
		assertEquals("ParentPolicyPaymentTransId", 200L, actual.getParentPolicyPaymentTransId().intValue());

	}

	@Test
	public void testCreateReversalPaymentTrans_PaymentAmountNotNull() {

		DateTime coverageDate = new DateTime("2015-01-01");
		String financialProgramTypeCd = "APTC";
		PolicyDataDTO policy = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-01-01", "2015-01-01", "2015-01-12");
		PolicyPaymentTransDTO paymentTrans = new PolicyPaymentTransDTO();
		Long reversalRefId = 200L; 

		ReflectionTestUtils.setField(rapProcesssingServiceTest, "reversedTransIds", new ArrayList<Long>());

		paymentTrans.setPaymentAmount(new BigDecimal(100));

		PolicyPaymentTransDTO actual = ReflectionTestUtils.invokeMethod(rapProcesssingServiceTest, "createReversalPaymentTrans", coverageDate, 
				financialProgramTypeCd, policy, paymentTrans, reversalRefId);

		assertNotNull("PaymentAmountNotNull", actual.getPaymentAmount());
		assertEquals("ParentPolicyPaymentTransId", 200L, actual.getParentPolicyPaymentTransId().intValue());

	}

	@Test
	public void testCreateRetros_ListEmpty() {

		DateTime coverageDate = new DateTime("2015-01-01");
		PolicyDataDTO policy = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-01-01", "2015-01-01", "2015-01-12");
		
		List<PolicyPremium> premiumRecs = new ArrayList<PolicyPremium>();
		PolicyPremium records = new PolicyPremium();

		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		policy.setInsrncAplctnTypeCd("AAA");
		policy.setPlanId("1242575658");

		records.setEffectiveEndDate(new DateTime(2015, 6, 6, 0, 0));
		records.setTotalPremiumAmount(new BigDecimal(150));
		premiumRecs.add(records);

		List<PolicyPaymentTransDTO> actual = ReflectionTestUtils.invokeMethod(rapProcesssingServiceTest, "createRetros", coverageDate,
				policy, premiumRecs, ProrationType.NON_PRORATING);

		assertTrue("PolicyPaymentTransDTOList", actual.isEmpty());

	}

	@Test
	public void testCreateRetros_ListNotEmpty() {

		DateTime coverageDate = new DateTime("2015-01-01");
		PolicyDataDTO policy = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-01-01", "2015-01-01", "2015-01-12");
		List<PolicyPremium> premiumRecs = new ArrayList<PolicyPremium>();
		PolicyPremium records = new PolicyPremium();

		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		replay(mockRapDao);

		policy.setInsrncAplctnTypeCd("AAA");

		records.setEffectiveEndDate(new DateTime(2015, 6, 6, 0, 0));
		records.setTotalPremiumAmount(new BigDecimal(150));
		records.setAptcAmount(new BigDecimal(100));
		records.setEffectiveStartDate(coverageDate);
		premiumRecs.add(records);	

		List<PolicyPaymentTransDTO> actual = ReflectionTestUtils.invokeMethod(rapProcesssingServiceTest, "createRetros", coverageDate,
				policy, premiumRecs, ProrationType.NON_PRORATING);

		assertFalse("PolicyPaymentTransDTOList", actual.isEmpty());

	}

	@Test
	public void testGetPaymentForPremiumPeriod_1() {

		List<PolicyPaymentTransDTO> payments = new ArrayList<PolicyPaymentTransDTO>();
		PolicyPaymentTransDTO policyPayment = new PolicyPaymentTransDTO();
		DateTime effectiveStartDate = new DateTime("2015-01-01");
		DateTime premiumEndDate = new DateTime("2015-06-01");
		List<Long> revTransIds = new ArrayList<Long>();
		revTransIds.add(100L);

		ReflectionTestUtils.setField(rapProcesssingServiceTest, "reversedTransIds", revTransIds);

		policyPayment.setPaymentCoverageStartDate(new DateTime(2015, 01, 01, 00, 00));
		policyPayment.setPaymentCoverageEndDate(new DateTime(2015, 06, 01, 00, 00));
		policyPayment.setPolicyPaymentTransId(10L);
		policyPayment.setLastPaymentProcStatusTypeCd(STATUS_PENDING_CYCLE);
		payments.add(policyPayment);

		PolicyPaymentTransDTO actual = ReflectionTestUtils.invokeMethod(rapProcesssingServiceTest, "getPaymentForPremiumPeriod", payments,
				effectiveStartDate, premiumEndDate);

		assertNotNull("PolicyPaymentTransDTO_NotNull", actual);
		assertEquals("PolicyPaymentTransDTO", policyPayment, actual);
		assertEquals("LastPaymentProcStatusTypeCd", STATUS_PENDING_CYCLE, actual.getLastPaymentProcStatusTypeCd());

	}

	@Test
	public void testGetPaymentForPremiumPeriod_2() {

		List<PolicyPaymentTransDTO> payments = new ArrayList<PolicyPaymentTransDTO>();
		PolicyPaymentTransDTO policyPayment = new PolicyPaymentTransDTO();
		DateTime effectiveStartDate = new DateTime("2015-01-01");
		DateTime premiumEndDate = new DateTime("2015-06-01");
		List<Long> revTransIds = new ArrayList<Long>();
		revTransIds.add(100L);

		ReflectionTestUtils.setField(rapProcesssingServiceTest, "reversedTransIds", revTransIds);

		policyPayment.setPaymentCoverageStartDate(new DateTime(2015, 01, 01, 00, 00));
		policyPayment.setPaymentCoverageEndDate(new DateTime(2015, 06, 01, 00, 00));
		policyPayment.setPolicyPaymentTransId(10L);
		policyPayment.setLastPaymentProcStatusTypeCd(STATUS_PENDING_APPROVAL);
		payments.add(policyPayment);

		PolicyPaymentTransDTO actual = ReflectionTestUtils.invokeMethod(rapProcesssingServiceTest, "getPaymentForPremiumPeriod", payments,
				effectiveStartDate, premiumEndDate);

		assertNotNull("PolicyPaymentTransDTO_NotNull", actual);
		assertEquals("PolicyPaymentTransDTO", policyPayment, actual);
		assertEquals("LastPaymentProcStatusTypeCd", STATUS_PENDING_APPROVAL, actual.getLastPaymentProcStatusTypeCd());

	}

	@Test
	public void testGetPaymentForPremiumPeriod_3() {

		List<PolicyPaymentTransDTO> payments = new ArrayList<PolicyPaymentTransDTO>();
		PolicyPaymentTransDTO policyPayment = new PolicyPaymentTransDTO();
		DateTime effectiveStartDate = new DateTime("2015-01-01");
		DateTime premiumEndDate = new DateTime("2015-06-01");
		List<Long> revTransIds = new ArrayList<Long>();
		revTransIds.add(100L);

		ReflectionTestUtils.setField(rapProcesssingServiceTest, "reversedTransIds", revTransIds);

		policyPayment.setPaymentCoverageStartDate(new DateTime(2015, 01, 01, 00, 00));
		policyPayment.setPaymentCoverageEndDate(new DateTime(2015, 06, 01, 00, 00));
		policyPayment.setPolicyPaymentTransId(10L);
		policyPayment.setLastPaymentProcStatusTypeCd(STATUS_APPROVED);
		payments.add(policyPayment);

		PolicyPaymentTransDTO actual = ReflectionTestUtils.invokeMethod(rapProcesssingServiceTest, "getPaymentForPremiumPeriod", payments,
				effectiveStartDate, premiumEndDate);

		assertNotNull("PolicyPaymentTransDTO_NotNull", actual);
		assertEquals("PolicyPaymentTransDTO", policyPayment, actual);
		assertEquals("LastPaymentProcStatusTypeCd", STATUS_APPROVED, actual.getLastPaymentProcStatusTypeCd());

	}

	@Test
	public void testGetPaymentForPremiumPeriod_Null() {

		List<PolicyPaymentTransDTO> payments = new ArrayList<PolicyPaymentTransDTO>();
		PolicyPaymentTransDTO policyPayment = new PolicyPaymentTransDTO();
		DateTime effectiveStartDate = new DateTime("2015-01-01");
		DateTime premiumEndDate = new DateTime("2015-06-01");
		List<Long> revTransIds = new ArrayList<Long>();
		revTransIds.add(100L);

		ReflectionTestUtils.setField(rapProcesssingServiceTest, "reversedTransIds", revTransIds);

		policyPayment.setPaymentCoverageStartDate(new DateTime(2015, 01, 01, 00, 00));
		policyPayment.setPaymentCoverageEndDate(new DateTime(2015, 06, 01, 00, 00));
		policyPayment.setPolicyPaymentTransId(10L);
		policyPayment.setLastPaymentProcStatusTypeCd("ABC");
		payments.add(policyPayment);

		PolicyPaymentTransDTO actual = ReflectionTestUtils.invokeMethod(rapProcesssingServiceTest, "getPaymentForPremiumPeriod", payments,
				effectiveStartDate, premiumEndDate);

		assertNull("LastPaymentProcStatusTypeCd", actual);

	}	

	@Test
	public void testCreateReversalForCancels_2() {

		List<PolicyPaymentTransDTO> pmtTransList = new ArrayList<PolicyPaymentTransDTO>();
		DateTime coverageDate = new DateTime("2015-01-01");
		String programType = "APTC";
		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-06-01", "2015-06-01", "2015-12-01");
		policyVersion.setPolicyCancelled(true);
		PolicyPaymentTransDTO payment = new PolicyPaymentTransDTO();
		payment.setLastPaymentProcStatusTypeCd(STATUS_PENDING_CYCLE);

		ReflectionTestUtils.setField(rapProcesssingServiceTest, "reversedTransIds", new ArrayList<Long>());

		ReflectionTestUtils.invokeMethod(rapProcesssingServiceTest, "createReversalForCancels", pmtTransList,
				coverageDate, programType, policyVersion, payment);

		PolicyPaymentTransDTO actualPayment = payment;
		List<PolicyPaymentTransDTO> actualPmtTransList = pmtTransList;

		assertEquals("pmtTransListSize", 1, actualPmtTransList.size());
		assertEquals("LastPaymentProcStatusTypeCd", "REPL", actualPayment.getLastPaymentProcStatusTypeCd());

	}


	@Test
	public void testCreateReversalForNonMatchingDates() {

		List<PolicyPaymentTransDTO> pmtTransList = new ArrayList<PolicyPaymentTransDTO>();
		DateTime coverageDate = new DateTime("2015-01-01");
		DateTime premiumStartDate = new DateTime("2015-06-01");
		String programType = "APTC";
		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-06-01", "2015-06-01", "2015-12-01");
		PolicyPaymentTransDTO payment = new PolicyPaymentTransDTO();

		ReflectionTestUtils.setField(rapProcesssingServiceTest, "reversedTransIds", new ArrayList<Long>());

		payment.setPaymentCoverageEndDate(new DateTime(2016, 01, 01, 0, 0));
		payment.setLastPaymentProcStatusTypeCd(STATUS_PENDING_APPROVAL);
		payment.setPolicyPaymentTransId(10L);
		payment.setParentPolicyPaymentTransId(100L);
		pmtTransList.add(payment);

		ReflectionTestUtils.invokeMethod(rapProcesssingServiceTest, "createReversalForNonMatchingDates", pmtTransList,
				coverageDate, premiumStartDate, programType, policyVersion, payment);

		List<PolicyPaymentTransDTO> actualList = pmtTransList;

		assertEquals("pmtTransListSize", 2, actualList.size());

		PolicyPaymentTransDTO actualPmtTransList1 = pmtTransList.get(0);

		assertEquals("parentPolicy_PolicyPaymentTransIdEquals", actualPmtTransList1.getParentPolicyPaymentTransId(), actualPmtTransList1.getPolicyPaymentTransId());

		PolicyPaymentTransDTO actualPmtTransList2 = pmtTransList.get(1);

		assertNotSame("parentPolicy_PolicyPaymentTransIdNotEqual", actualPmtTransList2.getParentPolicyPaymentTransId(), actualPmtTransList2.getPolicyPaymentTransId());
		assertNull("PolicyPaymentTransIdNull", actualPmtTransList2.getPolicyPaymentTransId());

	}


	@Test
	public void testCreateReversalForNonMatchingDates_2() {

		List<PolicyPaymentTransDTO> pmtTransList = new ArrayList<PolicyPaymentTransDTO>();
		DateTime coverageDate = new DateTime("2015-01-01");
		DateTime premiumStartDate = new DateTime("2017-06-01");
		String programType = "APTC";
		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-06-01", "2015-01-01", "2015-12-01");
		PolicyPaymentTransDTO payment = new PolicyPaymentTransDTO();

		ReflectionTestUtils.setField(rapProcesssingServiceTest, "reversedTransIds", new ArrayList<Long>());

		payment.setPaymentCoverageEndDate(new DateTime(2016, 01, 01, 0, 0));
		payment.setPolicyPaymentTransId(10L);
		payment.setParentPolicyPaymentTransId(100L);
		payment.setLastPaymentProcStatusTypeCd(STATUS_PENDING_CYCLE);
		pmtTransList.add(payment);

		ReflectionTestUtils.invokeMethod(rapProcesssingServiceTest, "createReversalForNonMatchingDates", pmtTransList,
				coverageDate, premiumStartDate, programType, policyVersion, payment);

		List<PolicyPaymentTransDTO> actualList = pmtTransList;

		assertEquals("pmtTransListSize", 1, actualList.size());

		PolicyPaymentTransDTO actualPmtTransList1 = pmtTransList.get(0);

		assertEquals("policyPaymentTransId", 10, actualPmtTransList1.getPolicyPaymentTransId().intValue());
		assertEquals("parentPolicyPaymentTransId", 100, actualPmtTransList1.getParentPolicyPaymentTransId().intValue());

	}


	@Test
	public void testCreateReversalForNonMatchingDates_3() {

		List<PolicyPaymentTransDTO> pmtTransList = new ArrayList<PolicyPaymentTransDTO>();
		DateTime coverageDate = new DateTime("2016-01-01");
		DateTime premiumStartDate = new DateTime("2017-06-01");
		String programType = "APTC";
		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-06-01", "2015-01-01", "2015-12-01");
		PolicyPaymentTransDTO payment = new PolicyPaymentTransDTO();

		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(5L).anyTimes();
		replay(mockRapDao);

		ReflectionTestUtils.setField(rapProcesssingServiceTest, "reversedTransIds", new ArrayList<Long>());

		payment.setPaymentCoverageEndDate(new DateTime(2016, 01, 01, 0, 0));
		payment.setLastPaymentProcStatusTypeCd(STATUS_PENDING_APPROVAL);
		payment.setPolicyPaymentTransId(10L);
		payment.setParentPolicyPaymentTransId(100L);
		pmtTransList.add(payment);

		ReflectionTestUtils.invokeMethod(rapProcesssingServiceTest, "createReversalForNonMatchingDates", pmtTransList,
				coverageDate, premiumStartDate, programType, policyVersion, payment);

		List<PolicyPaymentTransDTO> actualList = pmtTransList;

		assertEquals("pmtTransListSize", 2, actualList.size());

		PolicyPaymentTransDTO actualPmtTransList1 = pmtTransList.get(0);

		assertEquals("policyPaymentTransId", 10, actualPmtTransList1.getPolicyPaymentTransId().intValue());
		assertEquals("parentPolicyPaymentTransId", 100, actualPmtTransList1.getParentPolicyPaymentTransId().intValue());

		PolicyPaymentTransDTO actualPmtTransList2 = pmtTransList.get(1);

		assertEquals("policyPaymentTransId", 5, actualPmtTransList2.getPolicyPaymentTransId().intValue());
		assertEquals("parentPolicyPaymentTransId", 10, actualPmtTransList2.getParentPolicyPaymentTransId().intValue());

	}


	@Test
	public void testCreateReversalForNonMatchingDates_4() {

		List<PolicyPaymentTransDTO> pmtTransList = new ArrayList<PolicyPaymentTransDTO>();
		DateTime coverageDate = new DateTime("2015-01-01");
		DateTime premiumStartDate = new DateTime("2017-06-01");
		String programType = "APTC";
		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-06-01", "2015-02-01", "2015-12-01");
		PolicyPaymentTransDTO payment = new PolicyPaymentTransDTO();

		ReflectionTestUtils.setField(rapProcesssingServiceTest, "reversedTransIds", new ArrayList<Long>());

		payment.setPaymentCoverageEndDate(new DateTime(2016, 01, 01, 0, 0));
		payment.setLastPaymentProcStatusTypeCd(STATUS_PENDING_APPROVAL);
		payment.setPolicyPaymentTransId(10L);
		payment.setParentPolicyPaymentTransId(100L);
		pmtTransList.add(payment);

		ReflectionTestUtils.invokeMethod(rapProcesssingServiceTest, "createReversalForNonMatchingDates", pmtTransList,
				coverageDate, premiumStartDate, programType, policyVersion, payment);

		List<PolicyPaymentTransDTO> actualList = pmtTransList;

		assertEquals("pmtTransListSize", 1, actualList.size());

		PolicyPaymentTransDTO actualPmtTransList = pmtTransList.get(0);

		assertEquals("policyPaymentTransId", 10, actualPmtTransList.getPolicyPaymentTransId().intValue());
		assertEquals("parentPolicyPaymentTransId", 100, actualPmtTransList.getParentPolicyPaymentTransId().intValue());

	}

	@Test
	public void testCreateUfPayments() {

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-06-01", "2015-02-01", "2015-12-01");
		DateTime coverageDate = new DateTime("2015-01-01");
		List<PolicyPaymentTransDTO> pmtTransList = new ArrayList<PolicyPaymentTransDTO>();
		List<PolicyPaymentTransDTO> payments = new ArrayList<PolicyPaymentTransDTO>();
		List<PolicyPremium> premiumRecs = new ArrayList<PolicyPremium>();
		PolicyPremium policy = new PolicyPremium();

		policy.setAptcAmount(new BigDecimal(100));
		policy.setTotalPremiumAmount(null);
		policy.setEffectiveStartDate(coverageDate);
		premiumRecs.add(policy);

		assertEquals("premiumRecsSize", 1, premiumRecs.size());
		assertEquals("paymentsSize", 0, payments.size());
		assertEquals("pmtTransListSize", 0, pmtTransList.size());


		ReflectionTestUtils.invokeMethod(rapProcesssingServiceTest, "createUfPayments", policyVersion,
				coverageDate, pmtTransList, payments, premiumRecs);

		/*List<PolicyPremium> actualPremiumRecs = premiumRecs;
		List<PolicyPaymentTransDTO> actualPayments = payments;
		List<PolicyPaymentTransDTO> actualPmtTransList = pmtTransList;

		assertEquals("premiumRecsSize", 1, actualPremiumRecs.size());
		assertEquals("paymentsSize", 0, actualPayments.size());
		assertEquals("pmtTransListSize", 0, actualPmtTransList.size());*/

		assertEquals("premiumRecsSize", 1, premiumRecs.size());
		assertEquals("paymentsSize", 0, payments.size());
		assertEquals("pmtTransListSize", 0, pmtTransList.size());

	}

	@Test
	public void testCreateUfPayments_2() {


		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-06-01", "2015-02-01", "2015-12-01");
		DateTime coverageDate = new DateTime("2013-01-01");
		List<PolicyPaymentTransDTO> pmtTransList = new ArrayList<PolicyPaymentTransDTO>();
		List<PolicyPaymentTransDTO> payments = new ArrayList<PolicyPaymentTransDTO>();
		PolicyPaymentTransDTO paymentTrans = new PolicyPaymentTransDTO();
		List<PolicyPremium> premiumRecs = new ArrayList<PolicyPremium>();
		PolicyPremium policy = new PolicyPremium();

		ReflectionTestUtils.setField(rapProcesssingServiceTest, "reversalRefIds", new ArrayList<Long>());
		ReflectionTestUtils.setField(rapProcesssingServiceTest, "reversedTransIds", new ArrayList<Long>());


		paymentTrans.setPolicyPaymentTransId(10L);
		paymentTrans.setFinancialProgramTypeCd(UF);
		paymentTrans.setPaymentCoverageStartDate(new DateTime("2015-01-01"));
		paymentTrans.setPaymentCoverageEndDate(new DateTime("2012-10-02"));
		paymentTrans.setLastPaymentProcStatusTypeCd(STATUS_PENDING_CYCLE);
		paymentTrans.setTotalPremiumAmount(new BigDecimal(500));
		payments.add(paymentTrans);

		policy.setAptcAmount(new BigDecimal(100));
		policy.setTotalPremiumAmount(new BigDecimal(0));
		policy.setEffectiveStartDate(new DateTime("2015-01-01"));
		policy.setEffectiveEndDate(new DateTime("2012-10-02"));
		premiumRecs.add(policy);

		assertEquals("premiumRecsSize", 1, premiumRecs.size());
		assertEquals("paymentsSize", 1, payments.size());
		assertEquals("pmtTransListSize", 0, pmtTransList.size());

		ReflectionTestUtils.invokeMethod(rapProcesssingServiceTest, "createUfPayments", policyVersion,
				coverageDate, pmtTransList, payments, premiumRecs);


		assertEquals("premiumRecsSize", 1, premiumRecs.size());
		assertEquals("paymentsSize", 1, payments.size());
		assertEquals("pmtTransListSize", 1, pmtTransList.size());

	}
	
	//*************** M834 Changes - MGPID and Superseded Policies ****************

	/*
	 * Retroactive Change when policy version is created just to add the MGP Id on a pre-M834 policy.
	 * In this case no payments should be created since there is no chnage in Financials.
	 */
	@Test
	public void process_RetroActiveChange_AddMGPID() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2016-08-01 00:00:00", "2016-07-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroChangeMGPID();
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(2, "101", "2016-08-01", "2016-08-01", "2016-12-31");
		policyVersion.setMarketplaceGroupPolicyId(null);

		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertTrue("PolicyPaymentTransactions size", response.getPolicyPaymentTransactions().isEmpty());
	}

	/*
	 * Retroactive Change when policy version is created with a change on a pre-M834 policy.
	 * Payments should be reversed/adjusted.
	 */
	@Test
	public void process_RetroActiveChange_AddMGPID_WithSupersede() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2016-08-01 00:00:00", "2016-07-01 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		//Policy 987
		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroChangeMGPIDWithSupersede();
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(2, "101", "2016-08-10", "2016-08-01", "2016-12-31");
		policyVersion.setMarketplaceGroupPolicyId("987");
		policyVersion.setPolicyStatus(PolicyStatus.SUPERSEDED_5.getValue());

		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertEquals("PolicyPaymentTransactions size", 3, response.getPolicyPaymentTransactions().size());
		assertTrue("Payment Transaction values comparison", compareRetroActiveChangeResult_AddMGPID_WithSupersede(response.getPolicyPaymentTransactions()));

	}
	
	private boolean compareRetroActiveChangeResult_AddMGPID_WithSupersede(List<PolicyPaymentTransDTO> paymentTransactions) {

		PolicyPaymentTransDTO retroAPTC = paymentTransactions.get(0);
		assertEquals("Policy version id", 2, retroAPTC.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroAPTC.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroAPTC.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2016-08-01"), retroAPTC.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2016-08-01"), retroAPTC.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2016-08-31"), retroAPTC.getPaymentCoverageEndDate());
		assertEquals("Program Type", "APTC", retroAPTC.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroAPTC.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(-60).doubleValue(), retroAPTC.getPaymentAmount().doubleValue());
		assertEquals("Reversal Ref Trans id", 1, retroAPTC.getParentPolicyPaymentTransId().longValue());
		assertEquals("MGP Id", "987", retroAPTC.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO retroCSR = paymentTransactions.get(1);
		assertEquals("Policy version id", 2, retroCSR.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroCSR.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroCSR.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2016-08-01"), retroCSR.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2016-08-01"), retroCSR.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2016-08-31"), retroCSR.getPaymentCoverageEndDate());
		assertEquals("Program Type", "CSR", retroCSR.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroCSR.getLastPaymentProcStatusTypeCd());
		assertEquals("CSR Amount", new BigDecimal(-20).doubleValue(), retroCSR.getPaymentAmount().doubleValue());
		assertEquals("Reversal Ref Trans id", 2, retroCSR.getParentPolicyPaymentTransId().longValue());
		assertEquals("MGP Id", "987", retroAPTC.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO retroUF = paymentTransactions.get(2);
		assertEquals("Policy version id", 2, retroUF.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroUF.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroUF.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2016-08-01"), retroUF.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2016-08-01"), retroUF.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2016-08-31"), retroUF.getPaymentCoverageEndDate());
		assertEquals("Program Type", "UF", retroUF.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroUF.getLastPaymentProcStatusTypeCd());
		assertEquals("UF Amount", new BigDecimal(5.25).doubleValue(), retroUF.getPaymentAmount().doubleValue());
		assertEquals("Reversal Ref Trans id", 3, retroUF.getParentPolicyPaymentTransId().longValue());
		assertNotNull("MGP Id", retroUF.getMarketplaceGroupPolicyId());

		return true;
	}
	
	/*
	 * Retroactive Change when policy version is created with a status change to INITIAL.
	 * Existing Payments should be reversed.
	 */
	@Test
	public void process_RetroActiveChange_Uneffectuate() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2016-08-01 00:00:00", "2016-07-01 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		//Policy 987
		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroChangeUneffectuate();
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(2, "101", "2016-08-10", "2016-08-01", "2016-12-31");
		policyVersion.setMarketplaceGroupPolicyId("987");
		policyVersion.setPolicyStatus(PolicyStatus.INITIAL_1.getValue());

		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertEquals("PolicyPaymentTransactions size", 3, response.getPolicyPaymentTransactions().size());
		assertTrue("Payment Transaction values comparison", compareRetroActiveChangeResult_AddMGPID_WithSupersede(response.getPolicyPaymentTransactions()));

	}
	
	/*
	 * Retroactive Change when policy version is switched back to INITIAL status.
	 * No payments exist. New Payments should not be created.
	 */
	@Test
	public void process_RetroActiveChange_Uneffectuate_NoPaymentsExist() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2016-08-01 00:00:00", "2016-07-01 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		//Policy 987
		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroChangeUneffectuate();
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);
		policyDetailDTO.getPolicyPayments().clear();

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(2, "101", "2016-08-10", "2016-08-01", "2016-12-31");
		policyVersion.setMarketplaceGroupPolicyId("987");
		policyVersion.setPolicyStatus(PolicyStatus.INITIAL_1.getValue());

		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertTrue("PolicyPaymentTransactions size", response.getPolicyPaymentTransactions().isEmpty());

	}

	/*
	 * Retroactive Change when policy version is created with a change on a pre-M834 policy.
	 * Payments should be reversed/adjusted.
	 */
	@Test
	public void process_RetroActiveChange_AddMGPID_WithFinancialChanges() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2016-08-01 00:00:00", "2016-07-01 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		//Policy 987
		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroChangeMGPIDWithFinancialChange();
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(2, "101", "2016-08-10", "2016-08-01", "2016-12-31");
		policyVersion.setMarketplaceGroupPolicyId("987");
		policyVersion.setPolicyStatus(PolicyStatus.EFFECTUATED_2.getValue());

		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertEquals("PolicyPaymentTransactions size", 6, response.getPolicyPaymentTransactions().size());
		assertTrue("Payment Transaction values comparison", compareRetroActiveChangeResult_AddMGPID_WithFinancialChanges(response.getPolicyPaymentTransactions()));

	}
	
	private boolean compareRetroActiveChangeResult_AddMGPID_WithFinancialChanges(List<PolicyPaymentTransDTO> paymentTransactions) {

		PolicyPaymentTransDTO retroAPTC = paymentTransactions.get(0);
		assertEquals("Policy version id", 2, retroAPTC.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroAPTC.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroAPTC.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2016-08-01"), retroAPTC.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2016-08-01"), retroAPTC.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2016-08-31"), retroAPTC.getPaymentCoverageEndDate());
		assertEquals("Program Type", "APTC", retroAPTC.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroAPTC.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(-60).doubleValue(), retroAPTC.getPaymentAmount().doubleValue());
		assertEquals("Reversal Ref Trans id", 1, retroAPTC.getParentPolicyPaymentTransId().longValue());
		assertEquals("MGP Id", "987", retroAPTC.getMarketplaceGroupPolicyId());

		retroAPTC = paymentTransactions.get(1);
		assertEquals("Policy version id", 2, retroAPTC.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroAPTC.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroAPTC.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2016-08-01"), retroAPTC.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2016-08-01"), retroAPTC.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2016-08-31"), retroAPTC.getPaymentCoverageEndDate());
		assertEquals("Program Type", "APTC", retroAPTC.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroAPTC.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(50).doubleValue(), retroAPTC.getPaymentAmount().doubleValue());
		assertNull("Reversal Ref Trans id", retroAPTC.getParentPolicyPaymentTransId());
		assertEquals("MGP Id", "987", retroAPTC.getMarketplaceGroupPolicyId());
		
		PolicyPaymentTransDTO retroCSR = paymentTransactions.get(2);
		assertEquals("Policy version id", 2, retroCSR.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroCSR.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroCSR.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2016-08-01"), retroCSR.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2016-08-01"), retroCSR.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2016-08-31"), retroCSR.getPaymentCoverageEndDate());
		assertEquals("Program Type", "CSR", retroCSR.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroCSR.getLastPaymentProcStatusTypeCd());
		assertEquals("CSR Amount", new BigDecimal(-20).doubleValue(), retroCSR.getPaymentAmount().doubleValue());
		assertEquals("Reversal Ref Trans id", 2, retroCSR.getParentPolicyPaymentTransId().longValue());
		assertEquals("MGP Id", "987", retroAPTC.getMarketplaceGroupPolicyId());
		
		retroCSR = paymentTransactions.get(3);
		assertEquals("Policy version id", 2, retroCSR.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroCSR.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroCSR.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2016-08-01"), retroCSR.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2016-08-01"), retroCSR.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2016-08-31"), retroCSR.getPaymentCoverageEndDate());
		assertEquals("Program Type", "CSR", retroCSR.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroCSR.getLastPaymentProcStatusTypeCd());
		assertEquals("CSR Amount", new BigDecimal(10).doubleValue(), retroCSR.getPaymentAmount().doubleValue());
		assertNull("Reversal Ref Trans id", retroCSR.getParentPolicyPaymentTransId());
		assertEquals("MGP Id", "987", retroAPTC.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO retroUF = paymentTransactions.get(4);
		assertEquals("Policy version id", 2, retroUF.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroUF.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroUF.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2016-08-01"), retroUF.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2016-08-01"), retroUF.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2016-08-31"), retroUF.getPaymentCoverageEndDate());
		assertEquals("Program Type", "UF", retroUF.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroUF.getLastPaymentProcStatusTypeCd());
		assertEquals("UF Amount", new BigDecimal(5.25).doubleValue(), retroUF.getPaymentAmount().doubleValue());
		assertEquals("UF Amount", new BigDecimal(150).doubleValue(), retroUF.getTotalPremiumAmount().doubleValue());
		assertEquals("Reversal Ref Trans id", 3, retroUF.getParentPolicyPaymentTransId().longValue());
		assertEquals("MGP Id", "987", retroAPTC.getMarketplaceGroupPolicyId());
		
		retroUF = paymentTransactions.get(5);
		assertEquals("Policy version id", 2, retroUF.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroUF.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroUF.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2016-08-01"), retroUF.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2016-08-01"), retroUF.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2016-08-31"), retroUF.getPaymentCoverageEndDate());
		assertEquals("Program Type", "UF", retroUF.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroUF.getLastPaymentProcStatusTypeCd());
		assertNull("UF Amount", retroUF.getPaymentAmount());
		assertEquals("UF Amount", new BigDecimal(140).doubleValue(), retroUF.getTotalPremiumAmount().doubleValue());
		assertNull("Reversal Ref Trans id", retroUF.getParentPolicyPaymentTransId());
		assertEquals("MGP Id", "987", retroAPTC.getMarketplaceGroupPolicyId());

		return true;
	}
	
	//*************** SBM Changes ****************
	
	/*
	 * Retroactive Enrollment
	 */
	@Test
	public void process_RetroActiveEnrollment_SBM_No_ProratedAmounts() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-01-01 00:00:00", "2014-12-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);
		
		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroEnrollment();

		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);

		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2014-12-25", null);
		
		RapServiceTestUtil.loadStateConfigMap(policyVersion.getSubscriberStateCd(), policyVersion.getPolicyStartDate().getYear(), ProrationType.SBM_PRORATING);
		
		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertEquals("PolicyPaymentTransactions size", 2, response.getPolicyPaymentTransactions().size());
		assertTrue("Payment Transaction values comparison", compareRetroActiveEnrollmentResult(response.getPolicyPaymentTransactions(), false));
		
	}

	/*	
	 * Retroactive Enrollment
	 */
	@Test
	public void process_RetroActiveEnrollment_Zero_Amounts_SBM_No_ProratedAmounts() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-01-01 00:00:00", "2014-12-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroEnrollment_Zero_Amounts();

		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2014-12-25", null);

		RapServiceTestUtil.loadStateConfigMap(policyVersion.getSubscriberStateCd(), policyVersion.getPolicyStartDate().getYear(), ProrationType.SBM_PRORATING);
		
		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertEquals("PolicyPaymentTransactions size", 0, response.getPolicyPaymentTransactions().size());
	}

	/*	
	 * Retroactive Enrollment
	 */
	@Test
	public void process_RetroActiveEnrollment_Null_Amounts_SBM_No_ProratedAmounts() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-01-01 00:00:00", "2014-12-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroEnrollment_Null_Amounts();

		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2014-12-25", null);
		
		RapServiceTestUtil.loadStateConfigMap(policyVersion.getSubscriberStateCd(), policyVersion.getPolicyStartDate().getYear(), ProrationType.SBM_PRORATING);
		
		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertEquals("PolicyPaymentTransactions size", 0, response.getPolicyPaymentTransactions().size());
	}

	/*
	 * Retroactive Change
	 */
	@Test
	public void process_RetroActiveChange_SBM_No_ProratedAmounts() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-02-01 00:00:00", "2015-01-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroChange(false);
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(2, "101", "2015-01-28", null);

		RapServiceTestUtil.loadStateConfigMap(policyVersion.getSubscriberStateCd(),
				policyVersion.getPolicyStartDate().getYear(), ProrationType.SBM_PRORATING);
		
		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertEquals("PolicyPaymentTransactions size", 4, response.getPolicyPaymentTransactions().size());
		assertTrue("Payment Transaction values comparison", compareRetroActiveChangeResult(response.getPolicyPaymentTransactions()));
	}

	/*
	 * Retroactive Change
	 */
	@Test
	public void process_RetroActiveChange_Zero_Amounts_SBM_No_ProratedAmounts() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-02-01 00:00:00", "2015-01-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroChange_Zero_Amounts(false);
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(2, "101", "2015-01-28", null);
		
		RapServiceTestUtil.loadStateConfigMap(policyVersion.getSubscriberStateCd(),
				policyVersion.getPolicyStartDate().getYear(), ProrationType.SBM_PRORATING);
		
		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertEquals("PolicyPaymentTransactions size", 2, response.getPolicyPaymentTransactions().size());
		assertTrue("Payment Transaction values comparison", compareRetroActiveChnageResult_Zero_Amounts(response.getPolicyPaymentTransactions()));
	}

	/*
	 * Retroactive Change
	 */
	@Test
	public void process_RetroActiveChange_Null_Amounts_SBM_No_ProratedAmounts() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-02-01 00:00:00", "2015-01-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroChange_Null_Amounts(false);
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(2, "101", "2015-01-28", null);

		RapServiceTestUtil.loadStateConfigMap(policyVersion.getSubscriberStateCd(),
				policyVersion.getPolicyStartDate().getYear(), ProrationType.SBM_PRORATING);
		
		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertEquals("PolicyPaymentTransactions size", 2, response.getPolicyPaymentTransactions().size());
		assertTrue("Payment Transaction values comparison", compareRetroActiveChnageResult_Zero_Amounts(response.getPolicyPaymentTransactions()));
	}

	/*
	 * Retroactive Termination
	 */
	@Test
	public void process_RetroTerm_success_SBM_No_ProratedAmounts() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-03-01 00:00:00", "2015-02-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroTerm("APPV", false);
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(3, "101", "2015-02-22", "2015-02-28");

		RapServiceTestUtil.loadStateConfigMap(policyVersion.getSubscriberStateCd(),
				policyVersion.getPolicyStartDate().getYear(), ProrationType.SBM_PRORATING);
		
		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertEquals("PolicyPaymentTransactions size", 2, response.getPolicyPaymentTransactions().size());
		assertTrue("Payment Transaction values comparison", compareRetroActiveTermResult(response.getPolicyPaymentTransactions(), false));
	}
	
	/*
	 * Retroactive Termination Pending Cycle
	 */
	@Test
	public void process_RetroTerm_success_PendingCycle_SBM_No_ProratedAmounts() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-03-01 00:00:00", "2015-02-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroTerm("PCYC", true);
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(3, "101", "2015-02-22", "2015-02-28");

		RapServiceTestUtil.loadStateConfigMap(policyVersion.getSubscriberStateCd(),
				policyVersion.getPolicyStartDate().getYear(), ProrationType.SBM_PRORATING);
		
		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertEquals("PolicyPaymentTransactions size", 2, response.getPolicyPaymentTransactions().size());
		assertTrue("Payment Transaction values comparison", compareRetroActiveTermResultPendingCycle(response.getPolicyPaymentTransactions(), false));
	}
	
	/*
	 * Retroactive Reinstatement
	 */
	@Test
	public void process_RetroReinstatement_SBM_No_ProratedAmounts() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-04-01 00:00:00", "2015-03-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroReinstatement("APPV", false);
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(4, "101", "2015-03-27", null);

		RapServiceTestUtil.loadStateConfigMap(policyVersion.getSubscriberStateCd(),
				policyVersion.getPolicyStartDate().getYear(), ProrationType.SBM_PRORATING);
		
		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertEquals("PolicyPaymentTransactions size", 4, response.getPolicyPaymentTransactions().size());
		assertTrue("Payment Transaction values comparison", compareRetroActiveReinstatementResultSbm(response.getPolicyPaymentTransactions()));
	}
	
	private boolean compareRetroActiveReinstatementResultSbm(List<PolicyPaymentTransDTO> paymentTransactions) {

		PolicyPaymentTransDTO retroAPTC = paymentTransactions.get(0);
		assertEquals("Policy version id", 4, retroAPTC.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroAPTC.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroAPTC.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-03-01"), retroAPTC.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-03-01"), retroAPTC.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-03-31"), retroAPTC.getPaymentCoverageEndDate());
		assertEquals("Program Type", "APTC", retroAPTC.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroAPTC.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(80).doubleValue(), retroAPTC.getPaymentAmount().doubleValue());
		assertNotNull("MGP Id", retroAPTC.getMarketplaceGroupPolicyId());
		
		PolicyPaymentTransDTO retroCSR = paymentTransactions.get(1);
		assertEquals("Policy version id", 4, retroCSR.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroCSR.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroCSR.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-03-01"), retroCSR.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-03-01"), retroCSR.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-03-31"), retroCSR.getPaymentCoverageEndDate());
		assertEquals("Program Type", "CSR", retroCSR.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroCSR.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(50).doubleValue(), retroCSR.getPaymentAmount().doubleValue());
		assertNotNull("MGP Id", retroCSR.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO retroAPTC2 = paymentTransactions.get(2);
		assertEquals("Policy version id", 4, retroAPTC2.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroAPTC2.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroAPTC2.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-04-01"), retroAPTC2.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-04-01"), retroAPTC2.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-04-30"), retroAPTC2.getPaymentCoverageEndDate());
		assertEquals("Program Type", "APTC", retroAPTC2.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroAPTC2.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(80).doubleValue(), retroAPTC2.getPaymentAmount().doubleValue());
		assertNotNull("MGP Id", retroAPTC2.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO retroCSR2 = paymentTransactions.get(3);
		assertEquals("Policy version id", 4, retroCSR2.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroCSR2.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroCSR2.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-04-01"), retroCSR2.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-04-01"), retroCSR2.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-04-30"), retroCSR2.getPaymentCoverageEndDate());
		assertEquals("Program Type", "CSR", retroCSR2.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroCSR2.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(50).doubleValue(), retroCSR2.getPaymentAmount().doubleValue());
		assertNotNull("MGP Id", retroCSR2.getMarketplaceGroupPolicyId());

		return true;
	}

	/*
	 * Retroactive Change for a Period in the Past
	 */
	@Test
	public void process_RetroChangePastPeriod_SBM_No_ProratedAmounts() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-04-01 00:00:00", "2015-03-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroChangePastPeriod("APPV", false);
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(5, "101", "2015-04-02", null);

		RapServiceTestUtil.loadStateConfigMap(policyVersion.getSubscriberStateCd(),
				policyVersion.getPolicyStartDate().getYear(), ProrationType.SBM_PRORATING);
		
		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertEquals("PolicyPaymentTransactions size", 4, response.getPolicyPaymentTransactions().size());
		assertTrue("Payment Transaction values comparison", compareRetroActiveChangePastPeriod(response.getPolicyPaymentTransactions()));
	}
	
	/*
	 * Multiple Retroactive Change for Multiple Periods in the Past
	 */
	@Test
	public void process_RetroChangeMultipleChnagesForPmtMonth_SBM_No_ProratedAmounts() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-04-01 00:00:00", "2015-03-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroChangeMultiple(false);
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(6, "101", "2015-04-05", null);

		RapServiceTestUtil.loadStateConfigMap(policyVersion.getSubscriberStateCd(),
				policyVersion.getPolicyStartDate().getYear(), ProrationType.SBM_PRORATING);
		
		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertEquals("PolicyPaymentTransactions size", 4, response.getPolicyPaymentTransactions().size());
		assertTrue("Payment Transaction values comparison", compareRetroActiveChangeMultiple(response.getPolicyPaymentTransactions()));
	}
	
	/*
	 * Retroactive Cancellation - Policy Start Date
	 */
	@Test
	public void process_RetroCancel_SBM_No_ProratedAmounts() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-01-01 00:00:00", "2014-12-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroCancel("APPV", false);
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(2, "201", "2014-12-25", "2015-01-01");
		//Status
		policyVersion.setPolicyStatus(PolicyStatus.CANCELLED_3.getValue());
		
		RapServiceTestUtil.loadStateConfigMap(policyVersion.getSubscriberStateCd(),
				policyVersion.getPolicyStartDate().getYear(), ProrationType.SBM_PRORATING);
		
		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertEquals("PolicyPaymentTransactions size", 2, response.getPolicyPaymentTransactions().size());
		assertTrue("Payment Transaction values comparison", compareRetroActiveCancelResult(response.getPolicyPaymentTransactions(), false));
	}
	
	/*
	 * Retroactive Change
	 */
	@Test
	public void process_RetroActiveMidMonthChange_SBM_No_ProratedAmounts() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-02-01 00:00:00", "2015-01-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroMidMonthChange(false);
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(2, "301", "2015-01-28", null);

		RapServiceTestUtil.loadStateConfigMap(policyVersion.getSubscriberStateCd(),
				policyVersion.getPolicyStartDate().getYear(), ProrationType.SBM_PRORATING);
		
		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertEquals("PolicyPaymentTransactions size", 8, response.getPolicyPaymentTransactions().size());
		assertTrue("Payment Transaction values comparison", compareRetroActiveMidMonthChangeResultSbm(response.getPolicyPaymentTransactions()));
	}
	
	private boolean compareRetroActiveMidMonthChangeResultSbm(List<PolicyPaymentTransDTO> paymentTransactions) {

		PolicyPaymentTransDTO retroAPTCJan = paymentTransactions.get(0);
		assertEquals("Policy version id", 2, retroAPTCJan.getPolicyVersionId().longValue());
		assertEquals("Policy id", "301", retroAPTCJan.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroAPTCJan.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), retroAPTCJan.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-01-01"), retroAPTCJan.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-01-31"), retroAPTCJan.getPaymentCoverageEndDate());
		assertEquals("Program Type", "APTC", retroAPTCJan.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroAPTCJan.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(-50).doubleValue(), retroAPTCJan.getPaymentAmount().doubleValue());
		assertEquals("Reversal Ref Trans id", 1, retroAPTCJan.getParentPolicyPaymentTransId().longValue());	
		assertNotNull("MGP Id", retroAPTCJan.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO retroAPTCJan1 = paymentTransactions.get(1);
		assertEquals("Policy version id", 2, retroAPTCJan1.getPolicyVersionId().longValue());
		assertEquals("Policy id", "301", retroAPTCJan1.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroAPTCJan1.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), retroAPTCJan1.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-01-01"), retroAPTCJan1.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-01-15"), retroAPTCJan1.getPaymentCoverageEndDate());		
		assertEquals("Program Type", "APTC", retroAPTCJan1.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroAPTCJan1.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(50).doubleValue(), retroAPTCJan1.getPaymentAmount().doubleValue());
		assertNotNull("MGP Id", retroAPTCJan1.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO retroCSRJan = paymentTransactions.get(2);
		assertEquals("Policy version id", 2, retroCSRJan.getPolicyVersionId().longValue());
		assertEquals("Policy id", "301", retroCSRJan.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroCSRJan.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), retroCSRJan.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-01-01"), retroCSRJan.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-01-31"), retroCSRJan.getPaymentCoverageEndDate());
		assertEquals("Program Type", "CSR", retroCSRJan.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroCSRJan.getLastPaymentProcStatusTypeCd());
		assertEquals("CSR Amount", new BigDecimal(-25).doubleValue(), retroCSRJan.getPaymentAmount().doubleValue());
		assertEquals("Reversal Ref Trans id", 2, retroCSRJan.getParentPolicyPaymentTransId().longValue());
		assertNotNull("MGP Id", retroCSRJan.getMarketplaceGroupPolicyId());
		
		PolicyPaymentTransDTO retroCSRJan1 = paymentTransactions.get(3);
		assertEquals("Policy version id", 2, retroCSRJan1.getPolicyVersionId().longValue());
		assertEquals("Policy id", "301", retroCSRJan1.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroCSRJan1.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), retroCSRJan1.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-01-01"), retroCSRJan1.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-01-15"), retroCSRJan1.getPaymentCoverageEndDate());		
		assertEquals("Program Type", "CSR", retroCSRJan1.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroCSRJan1.getLastPaymentProcStatusTypeCd());
		assertEquals("CSR Amount", new BigDecimal(25).doubleValue(), retroCSRJan1.getPaymentAmount().doubleValue());
		assertNotNull("MGP Id", retroCSRJan1.getMarketplaceGroupPolicyId());
		
		
		PolicyPaymentTransDTO retroAPTCFeb = paymentTransactions.get(4);
		assertEquals("Policy version id", 2, retroAPTCFeb.getPolicyVersionId().longValue());
		assertEquals("Policy id", "301", retroAPTCFeb.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroAPTCFeb.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), retroAPTCFeb.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-02-01"), retroAPTCFeb.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-02-28"), retroAPTCFeb.getPaymentCoverageEndDate());		
		assertEquals("Program Type", "APTC", retroAPTCFeb.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroAPTCFeb.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(-50).doubleValue(), retroAPTCFeb.getPaymentAmount().doubleValue());
		assertEquals("Reversal Ref Trans id", 4, retroAPTCFeb.getParentPolicyPaymentTransId().longValue());
		assertNotNull("MGP Id", retroAPTCFeb.getMarketplaceGroupPolicyId());
		
		PolicyPaymentTransDTO retroAPTCFeb1 = paymentTransactions.get(5);
		assertEquals("Policy version id", 2, retroAPTCFeb1.getPolicyVersionId().longValue());
		assertEquals("Policy id", "301", retroAPTCFeb1.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroAPTCFeb1.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), retroAPTCFeb1.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-02-01"), retroAPTCFeb1.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-02-28"), retroAPTCFeb1.getPaymentCoverageEndDate());
		assertEquals("Program Type", "APTC", retroAPTCFeb1.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroAPTCFeb1.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(100).doubleValue(), retroAPTCFeb1.getPaymentAmount().doubleValue());
		assertNotNull("MGP Id", retroAPTCFeb1.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO retroCSRFeb = paymentTransactions.get(6);
		assertEquals("Policy version id", 2, retroCSRFeb.getPolicyVersionId().longValue());
		assertEquals("Policy id", "301", retroCSRFeb.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroCSRFeb.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), retroCSRFeb.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-02-01"), retroCSRFeb.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-02-28"), retroCSRFeb.getPaymentCoverageEndDate());
		assertEquals("Program Type", "CSR", retroCSRFeb.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroCSRFeb.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(-25).doubleValue(), retroCSRFeb.getPaymentAmount().doubleValue());
		assertEquals("Reversal Ref Trans id", 5, retroCSRFeb.getParentPolicyPaymentTransId().longValue());
		assertNotNull("MGP Id", retroCSRFeb.getMarketplaceGroupPolicyId());
		
		PolicyPaymentTransDTO retroCSRFeb1 = paymentTransactions.get(7);
		assertEquals("Policy version id", 2, retroCSRFeb1.getPolicyVersionId().longValue());
		assertEquals("Policy id", "301", retroCSRFeb1.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroCSRFeb1.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), retroCSRFeb1.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-02-01"), retroCSRFeb1.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-02-28"), retroCSRFeb1.getPaymentCoverageEndDate());
		assertEquals("Program Type", "CSR", retroCSRFeb1.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroCSRFeb1.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(50).doubleValue(), retroCSRFeb1.getPaymentAmount().doubleValue());
		assertNotNull("MGP Id", retroCSRFeb1.getMarketplaceGroupPolicyId());

		return true;
	}

	/*
	 * Retroactive Change
	 */
	@Test
	public void process_RetroActiveMidMonthChange_SBM_With_ProratedAmounts() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-02-01 00:00:00", "2015-01-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroMidMonthChangeSbmProrated();
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(2, "301", "2015-01-28", null);

		RapServiceTestUtil.loadStateConfigMap(policyVersion.getSubscriberStateCd(),
				policyVersion.getPolicyStartDate().getYear(), ProrationType.SBM_PRORATING);
		
		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertEquals("PolicyPaymentTransactions size", 10, response.getPolicyPaymentTransactions().size());
		assertTrue("Payment Transaction values comparison", compareRetroActiveMidMonthChangeResultSbmProrated(response.getPolicyPaymentTransactions()));
	}
	
	private boolean compareRetroActiveMidMonthChangeResultSbmProrated(List<PolicyPaymentTransDTO> paymentTransactions) {

		PolicyPaymentTransDTO retroAPTCJan = paymentTransactions.get(0);
		assertEquals("Policy version id", 2, retroAPTCJan.getPolicyVersionId().longValue());
		assertEquals("Policy id", "301", retroAPTCJan.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroAPTCJan.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), retroAPTCJan.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-01-01"), retroAPTCJan.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-01-31"), retroAPTCJan.getPaymentCoverageEndDate());
		assertEquals("Program Type", "APTC", retroAPTCJan.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroAPTCJan.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(-50).doubleValue(), retroAPTCJan.getPaymentAmount().doubleValue());
		assertEquals("Reversal Ref Trans id", 1, retroAPTCJan.getParentPolicyPaymentTransId().longValue());	
		assertNotNull("MGP Id", retroAPTCJan.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO retroAPTCJan1 = paymentTransactions.get(1);
		assertEquals("Policy version id", 2, retroAPTCJan1.getPolicyVersionId().longValue());
		assertEquals("Policy id", "301", retroAPTCJan1.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroAPTCJan1.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), retroAPTCJan1.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-01-01"), retroAPTCJan1.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-01-15"), retroAPTCJan1.getPaymentCoverageEndDate());		
		assertEquals("Program Type", "APTC", retroAPTCJan1.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroAPTCJan1.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(24.19).doubleValue(), retroAPTCJan1.getPaymentAmount().doubleValue());
		assertNotNull("MGP Id", retroAPTCJan1.getMarketplaceGroupPolicyId());
		
		PolicyPaymentTransDTO retroAPTCJan2 = paymentTransactions.get(2);
		assertEquals("Policy version id", 2, retroAPTCJan2.getPolicyVersionId().longValue());
		assertEquals("Policy id", "301", retroAPTCJan2.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroAPTCJan2.getTransPeriodTypeCd());
		assertEquals("Program Type", "APTC", retroAPTCJan2.getFinancialProgramTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), retroAPTCJan2.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-01-16"), retroAPTCJan2.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-01-31"), retroAPTCJan2.getPaymentCoverageEndDate());
		assertEquals("Trans Type", "PCYC", retroAPTCJan2.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(51.61).doubleValue(), retroAPTCJan2.getPaymentAmount().doubleValue());
		assertNotNull("MGP Id", retroAPTCJan2.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO retroCSRJan = paymentTransactions.get(3);
		assertEquals("Policy version id", 2, retroCSRJan.getPolicyVersionId().longValue());
		assertEquals("Policy id", "301", retroCSRJan.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroCSRJan.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), retroCSRJan.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-01-01"), retroCSRJan.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-01-31"), retroCSRJan.getPaymentCoverageEndDate());
		assertEquals("Program Type", "CSR", retroCSRJan.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroCSRJan.getLastPaymentProcStatusTypeCd());
		assertEquals("CSR Amount", new BigDecimal(-25).doubleValue(), retroCSRJan.getPaymentAmount().doubleValue());
		assertEquals("Reversal Ref Trans id", 2, retroCSRJan.getParentPolicyPaymentTransId().longValue());
		assertNotNull("MGP Id", retroCSRJan.getMarketplaceGroupPolicyId());
		
		PolicyPaymentTransDTO retroCSRJan1 = paymentTransactions.get(4);
		assertEquals("Policy version id", 2, retroCSRJan1.getPolicyVersionId().longValue());
		assertEquals("Policy id", "301", retroCSRJan1.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroCSRJan1.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), retroCSRJan1.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-01-01"), retroCSRJan1.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-01-15"), retroCSRJan1.getPaymentCoverageEndDate());		
		assertEquals("Program Type", "CSR", retroCSRJan1.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroCSRJan1.getLastPaymentProcStatusTypeCd());
		assertEquals("CSR Amount", new BigDecimal(12.10).doubleValue(), retroCSRJan1.getPaymentAmount().doubleValue());
		assertNotNull("MGP Id", retroCSRJan1.getMarketplaceGroupPolicyId());
		
		PolicyPaymentTransDTO retroCSRJan2 = paymentTransactions.get(5);
		assertEquals("Policy version id", 2, retroCSRJan2.getPolicyVersionId().longValue());
		assertEquals("Policy id", "301", retroCSRJan2.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroCSRJan2.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), retroCSRJan2.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-01-16"), retroCSRJan2.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-01-31"), retroCSRJan2.getPaymentCoverageEndDate());
		assertEquals("Program Type", "CSR", retroCSRJan2.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroCSRJan2.getLastPaymentProcStatusTypeCd());
		assertEquals("CSR Amount", new BigDecimal(25.81).doubleValue(), retroCSRJan2.getPaymentAmount().doubleValue());
		assertNotNull("MGP Id", retroCSRJan2.getMarketplaceGroupPolicyId());
		
		
		PolicyPaymentTransDTO retroAPTCFeb = paymentTransactions.get(6);
		assertEquals("Policy version id", 2, retroAPTCFeb.getPolicyVersionId().longValue());
		assertEquals("Policy id", "301", retroAPTCFeb.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroAPTCFeb.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), retroAPTCFeb.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-02-01"), retroAPTCFeb.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-02-28"), retroAPTCFeb.getPaymentCoverageEndDate());		
		assertEquals("Program Type", "APTC", retroAPTCFeb.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroAPTCFeb.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(-50).doubleValue(), retroAPTCFeb.getPaymentAmount().doubleValue());
		assertEquals("Reversal Ref Trans id", 4, retroAPTCFeb.getParentPolicyPaymentTransId().longValue());
		assertNotNull("MGP Id", retroAPTCFeb.getMarketplaceGroupPolicyId());
		
		PolicyPaymentTransDTO retroAPTCFeb1 = paymentTransactions.get(7);
		assertEquals("Policy version id", 2, retroAPTCFeb1.getPolicyVersionId().longValue());
		assertEquals("Policy id", "301", retroAPTCFeb1.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroAPTCFeb1.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), retroAPTCFeb1.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-02-01"), retroAPTCFeb1.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-02-28"), retroAPTCFeb1.getPaymentCoverageEndDate());
		assertEquals("Program Type", "APTC", retroAPTCFeb1.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroAPTCFeb1.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(100).doubleValue(), retroAPTCFeb1.getPaymentAmount().doubleValue());
		assertNotNull("MGP Id", retroAPTCFeb1.getMarketplaceGroupPolicyId());

		PolicyPaymentTransDTO retroCSRFeb = paymentTransactions.get(8);
		assertEquals("Policy version id", 2, retroCSRFeb.getPolicyVersionId().longValue());
		assertEquals("Policy id", "301", retroCSRFeb.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroCSRFeb.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), retroCSRFeb.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-02-01"), retroCSRFeb.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-02-28"), retroCSRFeb.getPaymentCoverageEndDate());
		assertEquals("Program Type", "CSR", retroCSRFeb.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroCSRFeb.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(-25).doubleValue(), retroCSRFeb.getPaymentAmount().doubleValue());
		assertEquals("Reversal Ref Trans id", 5, retroCSRFeb.getParentPolicyPaymentTransId().longValue());
		assertNotNull("MGP Id", retroCSRFeb.getMarketplaceGroupPolicyId());
		
		PolicyPaymentTransDTO retroCSRFeb1 = paymentTransactions.get(9);
		assertEquals("Policy version id", 2, retroCSRFeb1.getPolicyVersionId().longValue());
		assertEquals("Policy id", "301", retroCSRFeb1.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroCSRFeb1.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-02-01"), retroCSRFeb1.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-02-01"), retroCSRFeb1.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-02-28"), retroCSRFeb1.getPaymentCoverageEndDate());
		assertEquals("Program Type", "CSR", retroCSRFeb1.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroCSRFeb1.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(50).doubleValue(), retroCSRFeb1.getPaymentAmount().doubleValue());
		assertNotNull("MGP Id", retroCSRFeb1.getMarketplaceGroupPolicyId());

		return true;
	}
	
	/*
	 * Retroactive Change in Policy Start Date to End of a month but Start Date != End Date
	 */
	@Test
	public void process_Retro_PolicyStartDateChange_SBM_No_ProratedAmounts() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-11-01 00:00:00", "2015-10-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);

		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForPolicyStartDateChange(false);
		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);
		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(2, "201", "2015-10-15", "2015-06-30", "2015-10-31");

		RapServiceTestUtil.loadStateConfigMap(policyVersion.getSubscriberStateCd(),
				policyVersion.getPolicyStartDate().getYear(), ProrationType.SBM_PRORATING);
		
		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertEquals("PolicyPaymentTransactions size", 4, response.getPolicyPaymentTransactions().size());
		assertTrue("Payment Transaction values comparison", compareRetroActivePolicyStartDateChangeResult(response.getPolicyPaymentTransactions(), false));
	}
	
	@Test
	public void test_createPolicyPaymentTransactions_SBM_Non_Prorating() throws Exception {

		DateTime JAN_1 = new DateTime(YEAR, 1, 1, 0, 0);
		DateTime JAN_16 = new DateTime(YEAR, 1, 16, 0, 0);
		DateTime JAN_31 = new DateTime(YEAR, 1, 31, 0, 0);

		DateTime coverageDt = JAN_1;
		String programTypCd = "APTC";
		String policyId = "POLICYID";
		BigDecimal amt = new BigDecimal(100.00);

		PolicyPremium premium = new PolicyPremium();
		premium.setEffectiveStartDate(JAN_16);

		PolicyDataDTO policy = RapServiceTestUtil.createMockPolicyVersion(
				1L, policyId, new Date(System.currentTimeMillis()).toString(), "2015-03-01");
		policy.setPolicyStartDate(JAN_16);

		PolicyPaymentTransDTO result = (PolicyPaymentTransDTO)ReflectionTestUtils.invokeMethod(
				rapProcesssingServiceTest, "createPolicyPaymentTrans", 
				coverageDt, programTypCd, policy, premium, amt, null, ProrationType.SBM_PRORATING);

		assertNotNull("result", result);
		assertEquals("coverageDt value is Jan1", JAN_1, result.getCoverageDate());
		assertEquals("coverage Start Dt value is Jan1", JAN_16, result.getPaymentCoverageStartDate());
		assertEquals("coverage End Dt value is Jan31", JAN_31, result.getPaymentCoverageEndDate());
		assertEquals("TransPeriodTypeCd value is R", "R", result.getTransPeriodTypeCd());
		assertEquals("PmtStatusCd value is PCYC", "PCYC", result.getLastPaymentProcStatusTypeCd());
		assertEquals("Paymentmount", amt, result.getPaymentAmount());
		assertEquals("ProrationDays", coverageDt.dayOfMonth().getMaximumValue(), result.getProrationDaysOfCoverageNum().intValue());
	}
	
	@Test
	public void test_createPolicyPaymentTransactions_SBM_Prorating() throws Exception {

		DateTime JAN_1 = new DateTime(YEAR, 1, 1, 0, 0);
		DateTime JAN_16 = new DateTime(YEAR, 1, 16, 0, 0);
		DateTime JAN_31 = new DateTime(YEAR, 1, 31, 0, 0);

		DateTime coverageDt = JAN_1;
		String programTypCd = "APTC";
		String policyId = "POLICYID";
		BigDecimal amt = new BigDecimal(100.00);

		PolicyPremium premium = new PolicyPremium();
		premium.setEffectiveStartDate(JAN_16);

		PolicyDataDTO policy = RapServiceTestUtil.createMockPolicyVersion(
				1L, policyId, new Date(System.currentTimeMillis()).toString(), "2015-03-01");
		policy.setPolicyStartDate(JAN_16);

		PolicyPaymentTransDTO result = (PolicyPaymentTransDTO)ReflectionTestUtils.invokeMethod(
				rapProcesssingServiceTest, "createPolicyPaymentTrans", 
				coverageDt, programTypCd, policy, premium, amt, new BigDecimal("51.61"), ProrationType.SBM_PRORATING);

		assertNotNull("result", result);
		assertEquals("coverageDt value is Jan1", JAN_1, result.getCoverageDate());
		assertEquals("coverage Start Dt value is Jan1", JAN_16, result.getPaymentCoverageStartDate());
		assertEquals("coverage End Dt value is Jan31", JAN_31, result.getPaymentCoverageEndDate());
		assertEquals("TransPeriodTypeCd value is R", "R", result.getTransPeriodTypeCd());
		assertEquals("PmtStatusCd value is PCYC", "PCYC", result.getLastPaymentProcStatusTypeCd());
		assertEquals("Paymentmount", 51.61, result.getPaymentAmount().doubleValue());
		assertNull("ProrationDays", result.getProrationDaysOfCoverageNum());
	}
	
	//***********SBM - FDD Req. tests*****************/
	
	/*
	 * Retroactive Enrollment - FR-FM-PP-PPMSBMI-073 through FR-FM-PP-PPMSBMI-077
	 * 
	 * System shall create a single retroactive policy payment transaction (per applicable program) when proration type is ‘2’ 
	 * and only one policy premium record exists for the coverage month being evaluated for retroactive payment and 
	 * payments do not already exist in APPV, PAPPV, PCYC or NOISE Status.
	 */
	@Test
	public void process_RetroActivePayment_SBM_No_ProratedAmounts() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-01-01 00:00:00", "2014-12-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);
		
		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroEnrollment();

		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);

		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2014-12-25", null);
		
		RapServiceTestUtil.loadStateConfigMap(policyVersion.getSubscriberStateCd(),
				policyVersion.getPolicyStartDate().getYear(), ProrationType.SBM_PRORATING);
		
		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertEquals("PolicyPaymentTransactions size", 2, response.getPolicyPaymentTransactions().size());
		assertTrue("Payment Transaction values comparison", compareRetroActiveEnrollmentResult(response.getPolicyPaymentTransactions(), false));
		
	}
	
	/*
	 * Retroactive Enrollment - FR-FM-PP-PPMSBMI-073 through FR-FM-PP-PPMSBMI-077, with Prorated Amounts
	 * 
	 * System shall create a single retroactive policy payment transaction (per applicable program) when proration type is ‘2’ 
	 * and only one policy premium record exists for the coverage month being evaluated for retroactive payment and 
	 * payments do not already exist in APPV, PAPPV, PCYC or NOISE Status.
	 */
	@Test
	public void process_RetroActivePayment_SBM_With_ProratedAmounts() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-01-01 00:00:00", "2014-12-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);
		
		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createProratedMockPolicyPaymentDataForRetroEnrollment();

		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);

		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2014-12-25", null);
		
		RapServiceTestUtil.loadStateConfigMap(policyVersion.getSubscriberStateCd(),
				policyVersion.getPolicyStartDate().getYear(), ProrationType.SBM_PRORATING);
		
		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertEquals("PolicyPaymentTransactions size", 2, response.getPolicyPaymentTransactions().size());
		assertTrue("Payment Transaction values comparison", compareRetroActiveEnrollmentResultSbmProration(response.getPolicyPaymentTransactions()));
	}
	
	private boolean compareRetroActiveEnrollmentResultSbmProration(List<PolicyPaymentTransDTO> paymentTransactions) {

		PolicyPaymentTransDTO retroAPTCPaymentTransDTO = paymentTransactions.get(0);
		assertEquals("Policy version id", 1, retroAPTCPaymentTransDTO.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroAPTCPaymentTransDTO.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroAPTCPaymentTransDTO.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), retroAPTCPaymentTransDTO.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-01-15"), retroAPTCPaymentTransDTO.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-01-31"), retroAPTCPaymentTransDTO.getPaymentCoverageEndDate());
		assertEquals("Program Type", "APTC", retroAPTCPaymentTransDTO.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroAPTCPaymentTransDTO.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(25).doubleValue(), retroAPTCPaymentTransDTO.getPaymentAmount().doubleValue());
		assertEquals("TPA", new BigDecimal(50).doubleValue(), retroAPTCPaymentTransDTO.getTotalPremiumAmount().doubleValue());
		assertNull("TPA", retroAPTCPaymentTransDTO.getProrationDaysOfCoverageNum());
		
		PolicyPaymentTransDTO retroCSRPaymentTransDTO = paymentTransactions.get(1);
		assertEquals("Policy version id", 1, retroCSRPaymentTransDTO.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroCSRPaymentTransDTO.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroCSRPaymentTransDTO.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), retroCSRPaymentTransDTO.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-01-15"), retroAPTCPaymentTransDTO.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-01-31"), retroAPTCPaymentTransDTO.getPaymentCoverageEndDate());
		assertEquals("Program Type", "CSR", retroCSRPaymentTransDTO.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroAPTCPaymentTransDTO.getLastPaymentProcStatusTypeCd());
		assertEquals("CSR Amount", new BigDecimal(12.5).doubleValue(), retroCSRPaymentTransDTO.getPaymentAmount().doubleValue());
		assertEquals("TPA", new BigDecimal(50).doubleValue(), retroCSRPaymentTransDTO.getTotalPremiumAmount().doubleValue());
		assertNull("TPA", retroCSRPaymentTransDTO.getProrationDaysOfCoverageNum());
		
		return true;
	}

	/*
	 * Retroactive Enrollment Multiple Premiums - FR-FM-PP-PPMSBMI-078 through FR-FM-PP-PPMSBMI-082
	 * 
	 * System shall create just one retroactive policy payment transaction (per applicable program) for the 
	 * missing coverage month when proration type is ‘2’ and no prorated APTC/CSR amounts exist, 
	 * despite the presence of multiple policy premium records for the coverage month being evaluated for retroactive payment.
	 */
	@Test
	public void process_RetroActivePayment_SBM_Multiple_Premiums_No_ProratedAmounts() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-01-01 00:00:00", "2014-12-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);
		
		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createMockPolicyPaymentDataForRetroEnrollmentMultiplePremiumsForMonth();

		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);

		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2014-12-25", null);
		
		RapServiceTestUtil.loadStateConfigMap(policyVersion.getSubscriberStateCd(),
				policyVersion.getPolicyStartDate().getYear(), ProrationType.SBM_PRORATING);
		
		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertEquals("PolicyPaymentTransactions size", 2, response.getPolicyPaymentTransactions().size());
		assertTrue("Payment Transaction values comparison", compareResultSbmMultiplePremiums(response.getPolicyPaymentTransactions()));
	}
	
	private boolean compareResultSbmMultiplePremiums(List<PolicyPaymentTransDTO> paymentTransactions) {

		PolicyPaymentTransDTO retroAPTCPaymentTransDTO = paymentTransactions.get(0);
		assertEquals("Policy version id", 1, retroAPTCPaymentTransDTO.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroAPTCPaymentTransDTO.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroAPTCPaymentTransDTO.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), retroAPTCPaymentTransDTO.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-01-01"), retroAPTCPaymentTransDTO.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-01-15"), retroAPTCPaymentTransDTO.getPaymentCoverageEndDate());
		assertEquals("Program Type", "APTC", retroAPTCPaymentTransDTO.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroAPTCPaymentTransDTO.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(50).doubleValue(), retroAPTCPaymentTransDTO.getPaymentAmount().doubleValue());
		assertEquals("TPA", new BigDecimal(100).doubleValue(), retroAPTCPaymentTransDTO.getTotalPremiumAmount().doubleValue());
		assertEquals("Proration days", 31, retroAPTCPaymentTransDTO.getProrationDaysOfCoverageNum().intValue());
		
		PolicyPaymentTransDTO retroCSRPaymentTransDTO = paymentTransactions.get(1);
		assertEquals("Policy version id", 1, retroCSRPaymentTransDTO.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroCSRPaymentTransDTO.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroCSRPaymentTransDTO.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), retroCSRPaymentTransDTO.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-01-01"), retroAPTCPaymentTransDTO.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-01-15"), retroAPTCPaymentTransDTO.getPaymentCoverageEndDate());
		assertEquals("Program Type", "CSR", retroCSRPaymentTransDTO.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroAPTCPaymentTransDTO.getLastPaymentProcStatusTypeCd());
		assertEquals("CSR Amount", new BigDecimal(25).doubleValue(), retroCSRPaymentTransDTO.getPaymentAmount().doubleValue());
		assertEquals("TPA", new BigDecimal(100).doubleValue(), retroCSRPaymentTransDTO.getTotalPremiumAmount().doubleValue());
		assertEquals("Proration days", 31, retroCSRPaymentTransDTO.getProrationDaysOfCoverageNum().intValue());
		
		return true;
	}

	/*
	 * Retroactive Enrollment - FR-FM-PP-PPMSBMI-083
	 * 
	 * System shall create a retroactive policy payment (per applicable program) for each policy premium record 
	 * with prorated amounts when proration type is ‘2’ and multiple policy premium records exist for the 
	 * coverage month being evaluated for retroactive payment.
	 */
	@Test
	public void process_RetroActivePayment_SBM_Multiple_Premiums_With_ProratedAmounts() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-01-01 00:00:00", "2014-12-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);
		
		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createProratedMockPolicyPaymentDataForRetroEnrollmentMultiplePremiumsForMonth();

		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);

		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2014-12-25", null);
		
		RapServiceTestUtil.loadStateConfigMap(policyVersion.getSubscriberStateCd(),
				policyVersion.getPolicyStartDate().getYear(), ProrationType.SBM_PRORATING);
		
		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertEquals("PolicyPaymentTransactions size", 4, response.getPolicyPaymentTransactions().size());
		assertTrue("Payment Transaction values comparison", compareRetrosSbmProrationMultiplePremiums(response.getPolicyPaymentTransactions()));
	}
	
	private boolean compareRetrosSbmProrationMultiplePremiums(List<PolicyPaymentTransDTO> paymentTransactions) {

		PolicyPaymentTransDTO retroAPTCPaymentTransDTO = paymentTransactions.get(0);
		assertEquals("Policy version id", 1, retroAPTCPaymentTransDTO.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroAPTCPaymentTransDTO.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroAPTCPaymentTransDTO.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), retroAPTCPaymentTransDTO.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-01-01"), retroAPTCPaymentTransDTO.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-01-15"), retroAPTCPaymentTransDTO.getPaymentCoverageEndDate());
		assertEquals("Program Type", "APTC", retroAPTCPaymentTransDTO.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroAPTCPaymentTransDTO.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(25).doubleValue(), retroAPTCPaymentTransDTO.getPaymentAmount().doubleValue());
		assertEquals("TPA", new BigDecimal(50).doubleValue(), retroAPTCPaymentTransDTO.getTotalPremiumAmount().doubleValue());
		assertNull("Proration days ", retroAPTCPaymentTransDTO.getProrationDaysOfCoverageNum());
		
		PolicyPaymentTransDTO retroAPTCPaymentTransDTO1 = paymentTransactions.get(1);
		assertEquals("Policy version id", 1, retroAPTCPaymentTransDTO1.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroAPTCPaymentTransDTO1.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroAPTCPaymentTransDTO1.getTransPeriodTypeCd());
		assertEquals("Program Type", "APTC", retroAPTCPaymentTransDTO1.getFinancialProgramTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), retroAPTCPaymentTransDTO1.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-01-16"), retroAPTCPaymentTransDTO1.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-01-31"), retroAPTCPaymentTransDTO1.getPaymentCoverageEndDate());
		assertEquals("Trans Type", "PCYC", retroAPTCPaymentTransDTO1.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(37.5).doubleValue(), retroAPTCPaymentTransDTO1.getPaymentAmount().doubleValue());
		assertEquals("TPA", new BigDecimal(75).doubleValue(), retroAPTCPaymentTransDTO1.getTotalPremiumAmount().doubleValue());
		assertNull("Proration days", retroAPTCPaymentTransDTO1.getProrationDaysOfCoverageNum());
		

		PolicyPaymentTransDTO retroCSRPaymentTransDTO = paymentTransactions.get(2);
		assertEquals("Policy version id", 1, retroCSRPaymentTransDTO.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroCSRPaymentTransDTO.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroCSRPaymentTransDTO.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), retroCSRPaymentTransDTO.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-01-01"), retroCSRPaymentTransDTO.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-01-15"), retroCSRPaymentTransDTO.getPaymentCoverageEndDate());
		assertEquals("Program Type", "CSR", retroCSRPaymentTransDTO.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroCSRPaymentTransDTO.getLastPaymentProcStatusTypeCd());
		assertEquals("CSR Amount", new BigDecimal(12.5).doubleValue(), retroCSRPaymentTransDTO.getPaymentAmount().doubleValue());
		assertEquals("TPA", new BigDecimal(50).doubleValue(), retroCSRPaymentTransDTO.getTotalPremiumAmount().doubleValue());
		assertNull("Proration days", retroCSRPaymentTransDTO.getProrationDaysOfCoverageNum());
		
		PolicyPaymentTransDTO retroCSRPaymentTransDTO1 = paymentTransactions.get(3);
		assertEquals("Policy version id", 1, retroCSRPaymentTransDTO1.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroCSRPaymentTransDTO1.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroCSRPaymentTransDTO1.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), retroCSRPaymentTransDTO1.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-01-16"), retroCSRPaymentTransDTO1.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-01-31"), retroCSRPaymentTransDTO1.getPaymentCoverageEndDate());
		assertEquals("Program Type", "CSR", retroCSRPaymentTransDTO1.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroCSRPaymentTransDTO1.getLastPaymentProcStatusTypeCd());
		assertEquals("CSR Amount", new BigDecimal(25).doubleValue(), retroCSRPaymentTransDTO1.getPaymentAmount().doubleValue());
		assertEquals("TPA", new BigDecimal(75).doubleValue(), retroCSRPaymentTransDTO1.getTotalPremiumAmount().doubleValue());
		assertNull("Proration days", retroCSRPaymentTransDTO1.getProrationDaysOfCoverageNum());
		
		return true;
	}

	/*
	 * Retroactive Enrollment - FR-FM-PP-PPMSBMI-083, Only Csr prorated
	 * 
	 * System shall create a retroactive policy payment (per applicable program) for each policy premium record 
	 * with prorated amounts when proration type is ‘2’ and multiple policy premium records exist for the 
	 * coverage month being evaluated for retroactive payment.
	 */
	@Test
	public void process_RetroActivePayment_SBM_Multiple_Premiums_With_ProratedCsrOnly() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-01-01 00:00:00", "2014-12-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);
		
		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createProratedMockPolicyPaymentDataForRetroEnrollmentMultiplePremiumsForMonth("CSR");

		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);

		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2014-12-25", null);
		
		RapServiceTestUtil.loadStateConfigMap(policyVersion.getSubscriberStateCd(),
				policyVersion.getPolicyStartDate().getYear(), ProrationType.SBM_PRORATING);
		
		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertEquals("PolicyPaymentTransactions size", 3, response.getPolicyPaymentTransactions().size());
		assertTrue("Payment Transaction values comparison", compareRetrosSbmProrationMultiplePremiumsAptcNotProrated(response.getPolicyPaymentTransactions()));
	}
	
	private boolean compareRetrosSbmProrationMultiplePremiumsAptcNotProrated(List<PolicyPaymentTransDTO> paymentTransactions) {

		PolicyPaymentTransDTO retroAPTCPaymentTransDTO = paymentTransactions.get(0);
		assertEquals("Policy version id", 1, retroAPTCPaymentTransDTO.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroAPTCPaymentTransDTO.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroAPTCPaymentTransDTO.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), retroAPTCPaymentTransDTO.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-01-01"), retroAPTCPaymentTransDTO.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-01-15"), retroAPTCPaymentTransDTO.getPaymentCoverageEndDate());
		assertEquals("Program Type", "APTC", retroAPTCPaymentTransDTO.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroAPTCPaymentTransDTO.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(50).doubleValue(), retroAPTCPaymentTransDTO.getPaymentAmount().doubleValue());
		assertEquals("TPA", new BigDecimal(100).doubleValue(), retroAPTCPaymentTransDTO.getTotalPremiumAmount().doubleValue());
		assertEquals("Proration days ", 31, retroAPTCPaymentTransDTO.getProrationDaysOfCoverageNum().intValue());

		PolicyPaymentTransDTO retroCSRPaymentTransDTO = paymentTransactions.get(1);
		assertEquals("Policy version id", 1, retroCSRPaymentTransDTO.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroCSRPaymentTransDTO.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroCSRPaymentTransDTO.getTransPeriodTypeCd());
		assertEquals("Program Type", "CSR", retroCSRPaymentTransDTO.getFinancialProgramTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), retroCSRPaymentTransDTO.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-01-01"), retroCSRPaymentTransDTO.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-01-15"), retroCSRPaymentTransDTO.getPaymentCoverageEndDate());
		assertEquals("Trans Type", "PCYC", retroCSRPaymentTransDTO.getLastPaymentProcStatusTypeCd());
		assertEquals("CSR Amount", new BigDecimal(12.5).doubleValue(), retroCSRPaymentTransDTO.getPaymentAmount().doubleValue());
		assertEquals("TPA", new BigDecimal(50).doubleValue(), retroCSRPaymentTransDTO.getTotalPremiumAmount().doubleValue());
		assertNull("Proration days", retroCSRPaymentTransDTO.getProrationDaysOfCoverageNum());
		
		PolicyPaymentTransDTO retroCSRPaymentTransDTO1 = paymentTransactions.get(2);
		assertEquals("Policy version id", 1, retroCSRPaymentTransDTO1.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroCSRPaymentTransDTO1.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroCSRPaymentTransDTO1.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), retroCSRPaymentTransDTO1.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-01-16"), retroCSRPaymentTransDTO1.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-01-31"), retroCSRPaymentTransDTO1.getPaymentCoverageEndDate());
		assertEquals("Program Type", "CSR", retroCSRPaymentTransDTO1.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroCSRPaymentTransDTO1.getLastPaymentProcStatusTypeCd());
		assertEquals("CSR Amount", new BigDecimal(25).doubleValue(), retroCSRPaymentTransDTO1.getPaymentAmount().doubleValue());
		assertEquals("TPA", new BigDecimal(75).doubleValue(), retroCSRPaymentTransDTO1.getTotalPremiumAmount().doubleValue());
		assertNull("Proration days", retroCSRPaymentTransDTO1.getProrationDaysOfCoverageNum());
		
		return true;
	}
	
	/*
	 * Retroactive Enrollment - FR-FM-PP-PPMSBMI-083, Only Aptc prorated
	 * 
	 * System shall create a retroactive policy payment (per applicable program) for each policy premium record 
	 * with prorated amounts when proration type is ‘2’ and multiple policy premium records exist for the 
	 * coverage month being evaluated for retroactive payment.
	 */
	@Test
	public void process_RetroActivePayment_SBM_Multiple_Premiums_With_ProratedAptcOnly() throws Exception {

		CodeRecord codeRecord = new CodeRecord("ERC", "2015-01-01 00:00:00", "2014-12-16 00:00:00");
		expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
		.andReturn(codeRecord);
		replay(mockCodeDecodesHelper);
		
		PolicyDetailDTO policyDetailDTO = RapServiceTestUtil.createProratedMockPolicyPaymentDataForRetroEnrollmentMultiplePremiumsForMonth("APTC");

		expect(mockRapDao.retrievePolicyPaymentData(EasyMock.anyObject(PolicyDataDTO.class)))
		.andReturn(policyDetailDTO);

		expect(mockRapDao.getPolicyPaymentTransNextSeq()).andReturn(1L).anyTimes();
		replay(mockRapDao);

		PolicyDataDTO policyVersion = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2014-12-25", null);
		
		RapServiceTestUtil.loadStateConfigMap(policyVersion.getSubscriberStateCd(),
				policyVersion.getPolicyStartDate().getYear(), ProrationType.SBM_PRORATING);
		
		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policyVersion);

		RAPProcessingResponse response = rapProcesssingServiceTest.processRetroActivePayments(request);

		assertNotNull("RAPProcessingResponse", response);
		assertNotNull("PolicyPaymentTransactions", response.getPolicyPaymentTransactions()); 
		assertEquals("PolicyPaymentTransactions size", 3, response.getPolicyPaymentTransactions().size());
		assertTrue("Payment Transaction values comparison", compareRetrosSbmProrationMultiplePremiumsCsrNotProrated(response.getPolicyPaymentTransactions()));
	}
	
	private boolean compareRetrosSbmProrationMultiplePremiumsCsrNotProrated(List<PolicyPaymentTransDTO> paymentTransactions) {

		PolicyPaymentTransDTO retroAPTCPaymentTransDTO = paymentTransactions.get(0);
		assertEquals("Policy version id", 1, retroAPTCPaymentTransDTO.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroAPTCPaymentTransDTO.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroAPTCPaymentTransDTO.getTransPeriodTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), retroAPTCPaymentTransDTO.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-01-01"), retroAPTCPaymentTransDTO.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-01-15"), retroAPTCPaymentTransDTO.getPaymentCoverageEndDate());
		assertEquals("Program Type", "APTC", retroAPTCPaymentTransDTO.getFinancialProgramTypeCd());
		assertEquals("Trans Type", "PCYC", retroAPTCPaymentTransDTO.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(25).doubleValue(), retroAPTCPaymentTransDTO.getPaymentAmount().doubleValue());
		assertEquals("TPA", new BigDecimal(50).doubleValue(), retroAPTCPaymentTransDTO.getTotalPremiumAmount().doubleValue());
		assertNull("Proration days ", retroAPTCPaymentTransDTO.getProrationDaysOfCoverageNum());
		
		PolicyPaymentTransDTO retroAPTCPaymentTransDTO1 = paymentTransactions.get(1);
		assertEquals("Policy version id", 1, retroAPTCPaymentTransDTO1.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroAPTCPaymentTransDTO1.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroAPTCPaymentTransDTO1.getTransPeriodTypeCd());
		assertEquals("Program Type", "APTC", retroAPTCPaymentTransDTO1.getFinancialProgramTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), retroAPTCPaymentTransDTO1.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-01-16"), retroAPTCPaymentTransDTO1.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-01-31"), retroAPTCPaymentTransDTO1.getPaymentCoverageEndDate());
		assertEquals("Trans Type", "PCYC", retroAPTCPaymentTransDTO1.getLastPaymentProcStatusTypeCd());
		assertEquals("APTC Amount", new BigDecimal(37.5).doubleValue(), retroAPTCPaymentTransDTO1.getPaymentAmount().doubleValue());
		assertEquals("TPA", new BigDecimal(75).doubleValue(), retroAPTCPaymentTransDTO1.getTotalPremiumAmount().doubleValue());
		assertNull("Proration days", retroAPTCPaymentTransDTO1.getProrationDaysOfCoverageNum());

		PolicyPaymentTransDTO retroCSRPaymentTransDTO = paymentTransactions.get(2);
		assertEquals("Policy version id", 1, retroCSRPaymentTransDTO.getPolicyVersionId().longValue());
		assertEquals("Policy id", "101", retroCSRPaymentTransDTO.getExchangePolicyId());
		assertEquals("Trans Type", "R", retroCSRPaymentTransDTO.getTransPeriodTypeCd());
		assertEquals("Program Type", "CSR", retroCSRPaymentTransDTO.getFinancialProgramTypeCd());
		assertEquals("Coverage Dt", new DateTime("2015-01-01"), retroCSRPaymentTransDTO.getCoverageDate());
		assertEquals("Coverage Start Dt", new DateTime("2015-01-01"), retroCSRPaymentTransDTO.getPaymentCoverageStartDate());
		assertEquals("Coverage End Dt", new DateTime("2015-01-15"), retroCSRPaymentTransDTO.getPaymentCoverageEndDate());
		assertEquals("Trans Type", "PCYC", retroCSRPaymentTransDTO.getLastPaymentProcStatusTypeCd());
		assertEquals("CSR Amount", new BigDecimal(25).doubleValue(), retroCSRPaymentTransDTO.getPaymentAmount().doubleValue());
		assertEquals("TPA", new BigDecimal(100).doubleValue(), retroCSRPaymentTransDTO.getTotalPremiumAmount().doubleValue());
		assertEquals("Proration days", 31, retroCSRPaymentTransDTO.getProrationDaysOfCoverageNum().intValue());
		
		return true;
	}

	
	@Test
	public void test_HighDate() {

		DateTime actualHighDate = new DateTime(RapConstants.HIGH_DATE);

		System.out.println("actualHighDate: " + actualHighDate);

		assertEquals("expected DateTime Year", 9999, actualHighDate.getYear());
		assertEquals("expected DateTime Month", 12, actualHighDate.getMonthOfYear());
		assertEquals("expected DateTime Day", 31, actualHighDate.getDayOfMonth());
	}

	@After
	public void tearDown() {
		
		RapProcessingHelper.getStateProrationConfigMap().clear();
	}
}
