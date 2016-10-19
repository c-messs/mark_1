/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.validation;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import gov.cms.dsh.bem.AdditionalInfoType;
import gov.cms.dsh.bem.BenefitEnrollmentMaintenanceType;
import gov.cms.dsh.bem.BooleanIndicatorSimpleType;
import gov.cms.dsh.bem.ExtractionStatusType;
import gov.cms.dsh.bem.FileInformationType;
import gov.cms.dsh.bem.HealthCoverageDatesType;
import gov.cms.dsh.bem.HealthCoverageType;
import gov.cms.dsh.bem.IssuerType;
import gov.cms.dsh.bem.MemberAdditionalIdentifierType;
import gov.cms.dsh.bem.MemberRelatedDatesType;
import gov.cms.dsh.bem.MemberRelatedInfoType;
import gov.cms.dsh.bem.MemberType;
import gov.cms.dsh.bem.PolicyInfoType;
import gov.hhs.cms.ff.fm.eps.ep.BEMDataUtil;
import gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentMaintenanceDTO;
import gov.hhs.cms.ff.fm.eps.ep.EPSConstants;
import gov.hhs.cms.ff.fm.eps.ep.EPSValidationRequest;
import gov.hhs.cms.ff.fm.eps.ep.ErrorWarningLogDTO;
import gov.hhs.cms.ff.fm.eps.ep.enums.EProdEnum;
import gov.hhs.cms.ff.fm.eps.ep.enums.PolicyStatus;
import gov.hhs.cms.ff.fm.eps.ep.services.PolicyDataService;
import gov.hhs.cms.ff.fm.eps.ep.services.TransMsgCompositeDAO;
import gov.hhs.cms.ff.fm.eps.ep.services.impl.FFMDataServiceImpl;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;
import gov.hhs.cms.ff.fm.eps.ep.validation.impl.FFMValidationServiceImpl;
import gov.hhs.cms.ff.fm.eps.ep.validation.impl.FinancialValidatorImpl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.lang.StringUtils;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.test.util.ReflectionTestUtils;

import com.accenture.foundation.common.exception.ApplicationException;

/**
 * Test class for FFMValidationServiceImpl
 * 
 * @author girish.padmanabhan
 */
@RunWith(JUnit4.class)
public class FFMValidatorTest extends TestCase {

	protected static final LocalDate DATE = LocalDate.now();
	protected static final LocalDateTime DATETIME = LocalDateTime.now();
	protected static final int YEAR = DATE.getYear();
		
	protected final LocalDate JAN_1 = LocalDate.of(YEAR, 1, 1);
	protected final LocalDate JAN_15 = LocalDate.of(YEAR, 1, 15);
	protected final LocalDate JAN_31 = LocalDate.of(YEAR, 1, 31);
	protected final LocalDate FEB_1 = LocalDate.of(YEAR, 2, 1);
	protected final LocalDate FEB_15 = LocalDate.of(YEAR, 2, 15);
	protected final LocalDate FEB_MAX = DATE.with(TemporalAdjusters.lastDayOfMonth());
	protected final LocalDate MAR_1 = LocalDate.of(YEAR, 3, 1);
	protected final LocalDate MAR_14 = LocalDate.of(YEAR, 3, 14);
	protected final LocalDate MAR_15 = LocalDate.of(YEAR, 3, 15);
	protected final LocalDate MAR_31 = LocalDate.of(YEAR, 3, 31);
	protected final LocalDate APR_1 = LocalDate.of(YEAR, 4, 1);
	protected final LocalDate APR_15 = LocalDate.of(YEAR, 4, 15);
	protected final LocalDate APR_30 = LocalDate.of(YEAR, 4, 30);
	protected final LocalDate MAY_1 = LocalDate.of(YEAR, 5, 1);
	protected final LocalDate DEC_1 = LocalDate.of(YEAR, 12, 1);
	protected final LocalDate DEC_15 = LocalDate.of(YEAR, 12, 15);
	protected final LocalDate DEC_31 = LocalDate.of(YEAR, 12, 31);


	FFMValidationServiceImpl ffmValidatorService;

	private PolicyDataService mockPolicyDataService;
	private TransMsgCompositeDAO mockTxnMsgService;
	private FinancialValidator financialValidator;

	@Before
	public void setup() throws Exception {
		ffmValidatorService = new FFMValidationServiceImpl();

		mockPolicyDataService = EasyMock.createMock(FFMDataServiceImpl.class);
		mockTxnMsgService = EasyMock.createMock(TransMsgCompositeDAO.class);
		financialValidator = new FinancialValidatorImpl();

		ffmValidatorService.setPolicyDataService(mockPolicyDataService);
		ffmValidatorService.setTxnMsgService(mockTxnMsgService);
		ffmValidatorService.setFinancialValidator(financialValidator);
	}

	@Test
	public void validateBEM_success() throws Exception {

		BenefitEnrollmentMaintenanceDTO dbBemDTO = EPSValidationTestUtil.createCurrentBEMForFFMValidatorTest();

		expect(mockPolicyDataService.getLatestBEMByPolicyId(EasyMock.anyObject(BenefitEnrollmentMaintenanceDTO.class)))
		.andReturn(dbBemDTO);
		replay(mockPolicyDataService);

		expect(mockTxnMsgService.getSkippedTransMsgCount(EasyMock.anyObject(BenefitEnrollmentMaintenanceDTO.class)))
		.andReturn(Integer.valueOf(0)).anyTimes();
		expect(mockTxnMsgService.getSkippedVersionCount(
				EasyMock.anyObject(BenefitEnrollmentMaintenanceDTO.class)))
				.andReturn(Integer.valueOf(0)).anyTimes();
		replay(mockTxnMsgService);

		BenefitEnrollmentMaintenanceDTO bemh = EPSValidationTestUtil.createBEMForPolicyMatch();
		bemh.getFileInformation().setGroupSenderID("GRSIDFFMTEST");

		EPSValidationRequest epsValidationRequest = new EPSValidationRequest();
		epsValidationRequest.setBenefitEnrollmentMaintenance(bemh);

		EPSValidationResponse response = ffmValidatorService.validateBEM(epsValidationRequest);

		assertNotNull("EPSValidationResponse", response);
	}
	
	@Test
	public void validateBEM_DuplicateVersionExists() throws Exception {

		LocalDateTime svDT = LocalDateTime.now();
		
		BenefitEnrollmentMaintenanceDTO dbBemDTO = EPSValidationTestUtil.createCurrentBEMForFFMValidatorTest();
		dbBemDTO.setSourceVersionId(1L);
		dbBemDTO.setSourceVersionDateTime(svDT);
		
		expect(mockPolicyDataService.getLatestBEMByPolicyId(EasyMock.anyObject(BenefitEnrollmentMaintenanceDTO.class)))
		.andReturn(dbBemDTO);
		replay(mockPolicyDataService);

		expect(mockTxnMsgService.getSkippedTransMsgCount(EasyMock.anyObject(BenefitEnrollmentMaintenanceDTO.class)))
		.andReturn(Integer.valueOf(0)).anyTimes();
		expect(mockTxnMsgService.getSkippedVersionCount(
				EasyMock.anyObject(BenefitEnrollmentMaintenanceDTO.class)))
				.andReturn(Integer.valueOf(0)).anyTimes();
		replay(mockTxnMsgService);

		BenefitEnrollmentMaintenanceDTO bemh = EPSValidationTestUtil.createBEMForPolicyMatch();
		bemh.getBem().getTransactionInformation().setPolicySnapshotVersionNumber("1");
		bemh.getBem().getTransactionInformation().setPolicySnapshotDateTime(DateTimeUtil.getXMLGregorianCalendar(svDT));

		EPSValidationRequest epsValidationRequest = new EPSValidationRequest();
		epsValidationRequest.setBenefitEnrollmentMaintenance(bemh);

		EPSValidationResponse response = ffmValidatorService.validateBEM(epsValidationRequest);

		assertNotNull("EPSValidationResponse", response);
		
		assertTrue("Ignore flag is true", bemh.isIgnore());
	}
	
	@Test
	public void validateBEM_LaterVersionExists() throws Exception {

		LocalDateTime svDT = LocalDateTime.now();
		
		BenefitEnrollmentMaintenanceDTO dbBemDTO = EPSValidationTestUtil.createCurrentBEMForFFMValidatorTest();
		dbBemDTO.setSourceVersionId(15L);
		dbBemDTO.setSourceVersionDateTime(svDT);
		
		expect(mockPolicyDataService.getLatestBEMByPolicyId(EasyMock.anyObject(BenefitEnrollmentMaintenanceDTO.class)))
		.andReturn(dbBemDTO);
		replay(mockPolicyDataService);

		expect(mockTxnMsgService.getSkippedTransMsgCount(EasyMock.anyObject(BenefitEnrollmentMaintenanceDTO.class)))
		.andReturn(Integer.valueOf(0)).anyTimes();
		expect(mockTxnMsgService.getSkippedVersionCount(
				EasyMock.anyObject(BenefitEnrollmentMaintenanceDTO.class)))
				.andReturn(Integer.valueOf(0)).anyTimes();
		replay(mockTxnMsgService);

		BenefitEnrollmentMaintenanceDTO bemh = EPSValidationTestUtil.createBEMForPolicyMatch();
		bemh.getBem().getTransactionInformation().setPolicySnapshotVersionNumber("10");
		bemh.getBem().getTransactionInformation().setPolicySnapshotDateTime(DateTimeUtil.getXMLGregorianCalendar(svDT));

		EPSValidationRequest epsValidationRequest = new EPSValidationRequest();
		epsValidationRequest.setBenefitEnrollmentMaintenance(bemh);

		EPSValidationResponse response = ffmValidatorService.validateBEM(epsValidationRequest);

		assertNotNull("EPSValidationResponse", response);
		
		assertTrue("Ignore flag is true", bemh.isIgnore());

	}
	
	@Test
	public void validateBEM_exception_EPROD_37() throws Exception {

		BenefitEnrollmentMaintenanceDTO bemh = EPSValidationTestUtil.createBEMForPolicyMatch();
		bemh.getFileInformation().setGroupSenderID("GRSIDFFMTEST");
		bemh.getBem().getMember().get(0).getAdditionalInfo().clear();
		bemh.getBem().getMember().get(0).getAdditionalInfo().add(EPSValidationTestUtil.makeAdditionalInfoType(JAN_1, null, BigDecimal.ZERO));

		EPSValidationRequest epsValidationRequest = new EPSValidationRequest();
		epsValidationRequest.setBenefitEnrollmentMaintenance(bemh);

		try {
			ffmValidatorService.validateBEM(epsValidationRequest);

		} catch (ApplicationException e) {
			assertNotNull("EPSValidationResponse", e.getMessage());
			System.out.println("e.getMessage()=" + e.getMessage());
			assertEquals("EPROD thrown", EProdEnum.EPROD_37.getCode(), e.getInformationCode());
		}
	}
	
	@Test
	public void validateBEM_exception_EPROD_ExtractionError() throws Exception {

		BenefitEnrollmentMaintenanceDTO bemh = EPSValidationTestUtil.createBEMForPolicyMatch();
		bemh.getFileInformation().setGroupSenderID("GRSIDFFMTEST");

		bemh.getBem().setExtractionStatus(makeExtractionStatus(BigInteger.valueOf(1)));

		EPSValidationRequest epsValidationRequest = new EPSValidationRequest();
		epsValidationRequest.setBenefitEnrollmentMaintenance(bemh);

		try {
			ffmValidatorService.validateBEM(epsValidationRequest);

		} catch (ApplicationException e) {
			assertNotNull("EPSValidationResponse", e.getMessage());
			System.out.println("e.getMessage()=" + e.getMessage());
			assertEquals("EPROD thrown", EProdEnum.EPROD_31.getCode(), e.getInformationCode());
		}
	}
	
	@Test
	public void validateBEM_exception_EPROD_ExtractionStatus_zero() throws Exception {

		BenefitEnrollmentMaintenanceDTO bemh = EPSValidationTestUtil.createBEMForPolicyMatch();
		bemh.getFileInformation().setGroupSenderID("GRSIDFFMTEST");

		bemh.getBem().setExtractionStatus(makeExtractionStatus(BigInteger.valueOf(0)));

		EPSValidationRequest epsValidationRequest = new EPSValidationRequest();
		epsValidationRequest.setBenefitEnrollmentMaintenance(bemh);

		ReflectionTestUtils.invokeMethod(ffmValidatorService, "validateExtractionStatus", bemh);
		assertEquals("HiosIdFromGrpSenderId", bemh, bemh);
	}
	
	@Test
	public void validateBEM_exception_EPROD_ExtractionStatus_null() throws Exception {

		BenefitEnrollmentMaintenanceDTO bemh = EPSValidationTestUtil.createBEMForPolicyMatch();
		bemh.getFileInformation().setGroupSenderID("GRSIDFFMTEST");

		EPSValidationRequest epsValidationRequest = new EPSValidationRequest();
		epsValidationRequest.setBenefitEnrollmentMaintenance(bemh);

		ReflectionTestUtils.invokeMethod(ffmValidatorService, "validateExtractionStatus", bemh);
		assertEquals("HiosIdFromGrpSenderId", bemh, bemh);
	}

	@Test
	public void validateBEM_exception_EPROD_29() throws Exception {

		BenefitEnrollmentMaintenanceDTO dbBemDTO = EPSValidationTestUtil.createCurrentBEMForFFMValidatorTest();

		expect(mockPolicyDataService.getLatestBEMByPolicyId(EasyMock.anyObject(BenefitEnrollmentMaintenanceDTO.class)))
		.andReturn(dbBemDTO);
		replay(mockPolicyDataService);

		expect(mockTxnMsgService.getSkippedTransMsgCount(EasyMock.anyObject(BenefitEnrollmentMaintenanceDTO.class)))
		.andReturn(Integer.valueOf(1)).anyTimes();
		expect(mockTxnMsgService.getSkippedVersionCount(
				EasyMock.anyObject(BenefitEnrollmentMaintenanceDTO.class)))
				.andReturn(Integer.valueOf(0)).anyTimes();
		replay(mockTxnMsgService);

		BenefitEnrollmentMaintenanceDTO bemh = EPSValidationTestUtil.createBEMForPolicyMatch();
		bemh.getFileInformation().setGroupSenderID("GRSIDFFMTEST");

		EPSValidationRequest epsValidationRequest = new EPSValidationRequest();
		epsValidationRequest.setBenefitEnrollmentMaintenance(bemh);

		try {
			ffmValidatorService.validateBEM(epsValidationRequest);

		} catch (ApplicationException e) {
			assertNotNull("EPSValidationResponse", e.getMessage());
			System.out.println("e.getMessage()=" + e.getMessage());
			assertEquals("EPROD thrown", EProdEnum.EPROD_29.getCode(), e.getInformationCode());
		}
	}

	@Test
	public void validateBEM_PriorSkippedVersion() throws Exception {

		BenefitEnrollmentMaintenanceDTO dbBemDTO = EPSValidationTestUtil.createCurrentBEMForFFMValidatorTest();

		expect(mockPolicyDataService.getLatestBEMByPolicyId(EasyMock.anyObject(BenefitEnrollmentMaintenanceDTO.class)))
		.andReturn(dbBemDTO);
		replay(mockPolicyDataService);

		expect(mockTxnMsgService.getSkippedTransMsgCount(EasyMock.anyObject(BenefitEnrollmentMaintenanceDTO.class)))
		.andReturn(Integer.valueOf(0)).anyTimes();
		expect(mockTxnMsgService.getSkippedVersionCount(
				EasyMock.anyObject(BenefitEnrollmentMaintenanceDTO.class)))
				.andReturn(Integer.valueOf(1)).anyTimes();
		replay(mockTxnMsgService);

		BenefitEnrollmentMaintenanceDTO bemh = EPSValidationTestUtil.createBEMForPolicyMatch();
		bemh.getFileInformation().setGroupSenderID("GRSIDFFMTEST");

		EPSValidationRequest epsValidationRequest = new EPSValidationRequest();
		epsValidationRequest.setBenefitEnrollmentMaintenance(bemh);

		ffmValidatorService.validateBEM(epsValidationRequest);

		assertTrue("Policy verssion prior skip flag", epsValidationRequest.getBenefitEnrollmentMaintenance().isVersionSkippedInPast());

	}

	@Test
	public void validateBEM_success_NoPolicyMatch() throws Exception {

		expect(mockPolicyDataService.getLatestBEMByPolicyId(EasyMock.anyObject(BenefitEnrollmentMaintenanceDTO.class)))
		.andReturn(null);
		replay(mockPolicyDataService);

		expect(mockTxnMsgService.getSkippedTransMsgCount(
				EasyMock.anyObject(BenefitEnrollmentMaintenanceDTO.class)))
				.andReturn(Integer.valueOf(0)).anyTimes();
		expect(mockTxnMsgService.getSkippedVersionCount(
				EasyMock.anyObject(BenefitEnrollmentMaintenanceDTO.class)))
				.andReturn(Integer.valueOf(0)).anyTimes();
		replay(mockTxnMsgService);

		BenefitEnrollmentMaintenanceDTO bemh = EPSValidationTestUtil.createBEMForPolicyMatch();
		bemh.getFileInformation().setGroupSenderID("GRSIDFFMTEST");

		EPSValidationRequest epsValidationRequest = new EPSValidationRequest();
		epsValidationRequest.setBenefitEnrollmentMaintenance(bemh);

		EPSValidationResponse response = ffmValidatorService.validateBEM(epsValidationRequest);

		assertNotNull("EPSValidationResponse", response);
	}

	@Test
	public void validateBEM_failure_grpSenderInvalid() throws Exception {

		BenefitEnrollmentMaintenanceDTO dbBemDTO = EPSValidationTestUtil.createCurrentBEMForFFMValidatorTest();

		expect(mockPolicyDataService.getLatestBEMByPolicyId(EasyMock.anyObject(BenefitEnrollmentMaintenanceDTO.class)))
		.andReturn(dbBemDTO);
		replay(mockPolicyDataService);

		expect(mockTxnMsgService.getSkippedTransMsgCount(
				EasyMock.anyObject(BenefitEnrollmentMaintenanceDTO.class)))
				.andReturn(Integer.valueOf(0)).anyTimes();
		expect(mockTxnMsgService.getSkippedVersionCount(
				EasyMock.anyObject(BenefitEnrollmentMaintenanceDTO.class)))
				.andReturn(Integer.valueOf(0)).anyTimes();
		replay(mockTxnMsgService);

		BenefitEnrollmentMaintenanceDTO bemh = EPSValidationTestUtil.createBEMForPolicyMatch();

		EPSValidationRequest epsValidationRequest = new EPSValidationRequest();
		epsValidationRequest.setBenefitEnrollmentMaintenance(bemh);

		EPSValidationResponse response = ffmValidatorService.validateBEM(epsValidationRequest);

		assertNotNull("EPSValidationResponse", response);
		assertEquals("EPSValidationResponse errors", 1, bemh.getErrorList().size());
	}


	@Test
	public void testGetHiosIdFromGrpSenderId() {

		String expected = "Z8888";
		String grpSenderId = expected + "99999";
		String actual = (String) ReflectionTestUtils.invokeMethod(ffmValidatorService, "getHiosIdFromGrpSenderId", new Object[] {grpSenderId});
		assertEquals("HiosIdFromGrpSenderId", expected, actual);
	}

	@Test
	public void testGetHiosIdFromGrpSenderId_Len4() {

		String expected = StringUtils.EMPTY;
		String grpSenderId = "Z123";
		String actual = (String) ReflectionTestUtils.invokeMethod(ffmValidatorService, "getHiosIdFromGrpSenderId", new Object[] {grpSenderId});
		assertEquals("HiosIdFromGrpSenderId", expected, actual);
	}

	@Test
	public void testGetHiosIdFromGrpSenderId_Len5() {

		String expected = StringUtils.EMPTY;
		String grpSenderId = "Z123";
		String actual = (String) ReflectionTestUtils.invokeMethod(ffmValidatorService, "getHiosIdFromGrpSenderId", new Object[] {grpSenderId});
		assertEquals("HiosIdFromGrpSenderId", expected, actual);
	}

	@Test
	public void testValidateGroupSenderId_NullIssuer() {

		boolean expected = true;
		BenefitEnrollmentMaintenanceDTO dbBemDto = new BenefitEnrollmentMaintenanceDTO();
		dbBemDto.setBem(new BenefitEnrollmentMaintenanceType());
		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		bemDTO.setFileInformation(new FileInformationType());
		Boolean actual = (Boolean) ReflectionTestUtils.invokeMethod(ffmValidatorService, "validateGroupSenderId", new Object[] {dbBemDto, bemDTO});
		assertEquals("validateGroupSenderId with null issuer", expected, actual.booleanValue());
	}

	@Test
	public void testValidateGroupSenderId_EmptyIssuer() {

		boolean expected = true;
		BenefitEnrollmentMaintenanceDTO dbBemDto = new BenefitEnrollmentMaintenanceDTO();
		dbBemDto.setBem(new BenefitEnrollmentMaintenanceType());
		dbBemDto.getBem().setIssuer(new IssuerType());
		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		bemDTO.setFileInformation(new FileInformationType());
		Boolean actual = (Boolean) ReflectionTestUtils.invokeMethod(ffmValidatorService, "validateGroupSenderId", new Object[] {dbBemDto, bemDTO});
		assertEquals("validateGroupSenderId with null issuer", expected, actual.booleanValue());
	}

	@Test
	public void testValidateGroupSenderId_MisMatch() {

		int expectedErrorListSize = 1;
		String expectedErrorCode1 = EPSConstants.ERCODE_INCORRECT_DATA;
		String expectedErrorDesc1 = EPSConstants.E050_DESC;
		String expectedErrorElem1 = "IssuerHIOSID";

		String hiosId = "12345";
		String grpSenderId = "GS12345678";
		boolean expected = false;
		BenefitEnrollmentMaintenanceDTO dbBemDto = new BenefitEnrollmentMaintenanceDTO();
		dbBemDto.setBem(new BenefitEnrollmentMaintenanceType());
		dbBemDto.getBem().setIssuer(new IssuerType());
		dbBemDto.getBem().getIssuer().setHIOSID(hiosId);
		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		bemDTO.setFileInformation(new FileInformationType());
		bemDTO.getFileInformation().setGroupSenderID(grpSenderId);

		List <ErrorWarningLogDTO> blEditErrors = new ArrayList<ErrorWarningLogDTO>();
		ReflectionTestUtils.setField(ffmValidatorService, "blEditErrors", blEditErrors);

		Boolean actual = (Boolean) ReflectionTestUtils.invokeMethod(ffmValidatorService, "validateGroupSenderId", new Object[] {dbBemDto, bemDTO});
		assertEquals("validateGroupSenderId with null issuer", expected, actual.booleanValue());

		// Now get the error list and confirm
		@SuppressWarnings("unchecked")
		List<ErrorWarningLogDTO> actualErrorList = (List<ErrorWarningLogDTO>) ReflectionTestUtils.getField(ffmValidatorService, "blEditErrors");
		assertEquals("ErrorWarningLogDTO list size", expectedErrorListSize, actualErrorList.size());
		ErrorWarningLogDTO actualError1 = actualErrorList.get(0);
		assertEquals("Error Code 1", expectedErrorCode1, actualError1.getBizAppAckErrorCd());
		assertEquals("Error Desc 1", expectedErrorDesc1, actualError1.getErrorWarningDetailedDesc());
		assertEquals("Error Element 1", expectedErrorElem1, actualError1.getErrorElement());
	}


	/**
	 * Tests checkSkippedVersions.  When a transMgsCount > 0 is returned which will throw 
	 * ApplicationException.
	 */
	@Test//(expected=com.accenture.foundation.common.exception.ApplicationException.class)
	public void testCheckSkippedVersions_Exception() {

		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		bemDTO.setBatchId(Long.valueOf(888888));
		bemDTO.setTransMsgId(Long.valueOf(999999));

		expect(mockTxnMsgService.getSkippedTransMsgCount(EasyMock.anyObject(BenefitEnrollmentMaintenanceDTO.class)))
		.andReturn(Integer.valueOf(1));
		replay(mockTxnMsgService);
		
		try {
			ReflectionTestUtils.invokeMethod(ffmValidatorService, "checkPriorSkippedVersions", new Object[] {bemDTO});
		} catch(ApplicationException appEx) {
			assertTrue("ApplicationException thrown", true);
		}
		assertNotNull("bemDTO", bemDTO);
	}


	@Test
	public void testCheckSkippedVersions() {

		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		bemDTO.setBatchId(Long.valueOf(888888));
		bemDTO.setTransMsgId(Long.valueOf(999999));

		expect(mockTxnMsgService.getSkippedTransMsgCount(EasyMock.anyObject(BenefitEnrollmentMaintenanceDTO.class)))
		.andReturn(Integer.valueOf(0));
		replay(mockTxnMsgService);
		try {
			ReflectionTestUtils.invokeMethod(ffmValidatorService, "checkPriorSkippedVersions", new Object[] {bemDTO});
		}  catch (ApplicationException appEx) {
			assertFalse("CheckSkippedVersion did NOT throw ApplicationExeption", true);
		} 
	}

	/**
	 * Tests checkCurrentVersionWasSkipped.  When a updateCount > 0 is returned,  
	 * return true.
	 */
	@Test
	public void testCheckCurrentVersionWasSkipped_Exception() {

		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		bemDTO.setBatchId(Long.valueOf(888888));
		bemDTO.setTransMsgId(Long.valueOf(999999));

		expect(mockTxnMsgService.getSkippedVersionCount(
				EasyMock.anyObject(BenefitEnrollmentMaintenanceDTO.class)))
				.andReturn(Integer.valueOf(1));
		replay(mockTxnMsgService);
		Boolean versionWasSkippedEarlier = (Boolean)ReflectionTestUtils.invokeMethod(ffmValidatorService, "checkCurrentVersionWasSkipped", new Object[] {bemDTO});
		assertTrue("version Was Skipped in earlier processing", versionWasSkippedEarlier.booleanValue());
	}


	@Test
	public void testCheckCurrentVersionWasSkipped() {

		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		bemDTO.setBatchId(Long.valueOf(888888));
		bemDTO.setTransMsgId(Long.valueOf(999999));

		expect(mockTxnMsgService.getSkippedVersionCount(
				EasyMock.anyObject(BenefitEnrollmentMaintenanceDTO.class)))
				.andReturn(Integer.valueOf(0));
		replay(mockTxnMsgService);

		Boolean versionWasSkippedEarlier = (Boolean)ReflectionTestUtils.invokeMethod(ffmValidatorService, "checkCurrentVersionWasSkipped", new Object[] {bemDTO});
		assertFalse("checkCurrentVersionWasSkipped returned false", versionWasSkippedEarlier.booleanValue());
	}

	/**
	 * Tests checkForLaterVersionsOfPolicy.  When a sourceversionid > that in eps policy is returned throw 
	 * ApplicationException.
	 */
	@Test
	public void test_checkIfLaterVersionExists_true() {

		boolean expected = true;
		BenefitEnrollmentMaintenanceDTO dbBemDTO = EPSValidationTestUtil.createCurrentBEMForFFMValidatorTest();
		dbBemDTO.setSourceVersionId(15L);

		expect(mockPolicyDataService.getLatestBEMByPolicyId(EasyMock.anyObject(BenefitEnrollmentMaintenanceDTO.class)))
		.andReturn(dbBemDTO);
		replay(mockPolicyDataService);

		BenefitEnrollmentMaintenanceDTO bemh = EPSValidationTestUtil.createBEMForPolicyMatch();
		bemh.getBem().getTransactionInformation().setPolicySnapshotVersionNumber("10");

		Boolean actual = (Boolean) ReflectionTestUtils.invokeMethod(ffmValidatorService, "checkIfLaterVersionExists", dbBemDTO, bemh);
		assertEquals("Returned value", expected, actual.booleanValue());
	}
	
	/**
	 * Tests checkForLaterVersionsOfPolicy.  When a sourceversionid is same but SVDT earlier than that in eps policy is returned throw 
	 * ApplicationException.
	 */
	@Test
	public void test_checkIfLaterVersionExists_true_Diff_SVDT() {

		boolean expected = true;
		LocalDateTime sourceVersionDateTime = LocalDateTime.now();
		
		BenefitEnrollmentMaintenanceDTO dbBemDTO = EPSValidationTestUtil.createCurrentBEMForFFMValidatorTest();
		dbBemDTO.setSourceVersionId(10L);
		dbBemDTO.setSourceVersionDateTime(sourceVersionDateTime.plusDays(1));

		expect(mockPolicyDataService.getLatestBEMByPolicyId(EasyMock.anyObject(BenefitEnrollmentMaintenanceDTO.class)))
		.andReturn(dbBemDTO);
		replay(mockPolicyDataService);

		BenefitEnrollmentMaintenanceDTO bemh = EPSValidationTestUtil.createBEMForPolicyMatch();
		bemh.getBem().getTransactionInformation().setPolicySnapshotVersionNumber("10");
		bemh.getBem().getTransactionInformation().setPolicySnapshotDateTime(DateTimeUtil.getXMLGregorianCalendar(sourceVersionDateTime));

		Boolean actual = (Boolean) ReflectionTestUtils.invokeMethod(ffmValidatorService, "checkIfLaterVersionExists", dbBemDTO, bemh);
		assertEquals("Returned value", expected, actual.booleanValue());
	}

	@Test
	public void testCheckForLaterVersionsOfPolicy() {

		boolean expectedIgnore = false;
		
		BenefitEnrollmentMaintenanceDTO dbBemDTO = EPSValidationTestUtil.createCurrentBEMForFFMValidatorTest();
		dbBemDTO.setSourceVersionId(2L);

		expect(mockPolicyDataService.getLatestBEMByPolicyId(EasyMock.anyObject(BenefitEnrollmentMaintenanceDTO.class)))
		.andReturn(dbBemDTO);
		replay(mockPolicyDataService);

		BenefitEnrollmentMaintenanceDTO bemh = EPSValidationTestUtil.createBEMForPolicyMatch();
		bemh.getBem().getTransactionInformation().setPolicySnapshotVersionNumber("1");

		ReflectionTestUtils.invokeMethod(ffmValidatorService, "checkIfLaterVersionExists", dbBemDTO, bemh);
		assertEquals("checkForLaterVersionsOfPolicy set BemDTO to isIgnore", expectedIgnore, bemh.isIgnore());
	}

	/**
	 * Tests checkForLaterVersionsOfPolicy.  When a sourceversionid > that in eps policy set isIgnore to true.
	 */
	@Test
	public void testCheckIfDuplicateVersionExists_true() {

		boolean expected = true;
		LocalDateTime svDT = LocalDateTime.now();

		BenefitEnrollmentMaintenanceDTO dbBemDTO = EPSValidationTestUtil.createCurrentBEMForFFMValidatorTest();
		dbBemDTO.setSourceVersionId(1L);
		dbBemDTO.setSourceVersionDateTime(svDT);

		expect(mockPolicyDataService.getLatestBEMByPolicyId(EasyMock.anyObject(BenefitEnrollmentMaintenanceDTO.class)))
		.andReturn(dbBemDTO);
		replay(mockPolicyDataService);

		BenefitEnrollmentMaintenanceDTO bemh = EPSValidationTestUtil.createBEMForPolicyMatch();
		bemh.getBem().getTransactionInformation().setPolicySnapshotVersionNumber("1");
		bemh.getBem().getTransactionInformation().setPolicySnapshotDateTime(DateTimeUtil.getXMLGregorianCalendar(svDT));

		Boolean actual = (Boolean) ReflectionTestUtils.invokeMethod(ffmValidatorService, "checkIfDuplicateVersionExists", dbBemDTO, bemh);
		assertEquals("Returned value", expected, actual.booleanValue());
	}

	/**
	 * Tests checkForLaterVersionsOfPolicy.  
	 */
	@Test
	public void testCheckIfDuplicateVersionExists_false() {

		boolean expected = false;
		LocalDateTime svDT = LocalDateTime.now();

		BenefitEnrollmentMaintenanceDTO dbBemDTO = EPSValidationTestUtil.createCurrentBEMForFFMValidatorTest();
		dbBemDTO.setSourceVersionId(1L);
		dbBemDTO.setSourceVersionDateTime(svDT);

		expect(mockPolicyDataService.getLatestBEMByPolicyId(EasyMock.anyObject(BenefitEnrollmentMaintenanceDTO.class)))
		.andReturn(dbBemDTO);
		replay(mockPolicyDataService);

		BenefitEnrollmentMaintenanceDTO bemh = EPSValidationTestUtil.createBEMForPolicyMatch();
		bemh.getBem().getTransactionInformation().setPolicySnapshotVersionNumber("1");
		bemh.getBem().getTransactionInformation().setPolicySnapshotDateTime(DateTimeUtil.getXMLGregorianCalendar(svDT.plusDays(1)));

		Boolean actual = (Boolean) ReflectionTestUtils.invokeMethod(ffmValidatorService, "checkIfDuplicateVersionExists", dbBemDTO, bemh);
		assertEquals("Returned value", expected, actual.booleanValue());
	}
	
	@Test
	public void testValidateMarketPlaceGroupPolicyId() {

		BenefitEnrollmentMaintenanceDTO bemDTO = EPSValidationTestUtil.createMockBEM();
		
		BenefitEnrollmentMaintenanceDTO dbBemDto = new BenefitEnrollmentMaintenanceDTO();
		dbBemDto.setMarketplaceGroupPolicyId("MGPI");
		
		ReflectionTestUtils.invokeMethod(ffmValidatorService, "validateMarketPlaceGroupPolicyId", new Object[] {dbBemDto, bemDTO});
		
		assertEquals("ValidateMarketPlaceGroupPolicyId success", dbBemDto.getMarketplaceGroupPolicyId(), bemDTO.getBem().getPolicyInfo().getMarketplaceGroupPolicyIdentifier());
	}
	
	/**
	 * Tests validateMarketPlaceGroupPolicyId. When MarketPlaceGroupPolicyId does not match with  
	 * that of the eps version throws ApplicationException EPROD-38.
	 */
	@Test//(expected=com.accenture.foundation.common.exception.ApplicationException.class)
	public void testValidateMarketPlaceGroupPolicyId_Exception() {
		
		BenefitEnrollmentMaintenanceDTO bemDTO = EPSValidationTestUtil.createMockBEM();
		
		BenefitEnrollmentMaintenanceDTO dbBemDto = new BenefitEnrollmentMaintenanceDTO();
		dbBemDto.setMarketplaceGroupPolicyId("INVALID");

		try {
			ReflectionTestUtils.invokeMethod(ffmValidatorService, "validateMarketPlaceGroupPolicyId", new Object[] {dbBemDto, bemDTO});
		} catch(ApplicationException appEx) {
			assertTrue("ApplicationException thrown", true);
		}
		assertNotNull("bemDTO", bemDTO);
	}
	
	@Test
	public void testValidateMarketPlaceGroupPolicyId_EmptyEpsMGPI() {

		BenefitEnrollmentMaintenanceDTO bemDTO = EPSValidationTestUtil.createMockBEM();
		
		BenefitEnrollmentMaintenanceDTO dbBemDto = new BenefitEnrollmentMaintenanceDTO();
		
		ReflectionTestUtils.invokeMethod(ffmValidatorService, "validateMarketPlaceGroupPolicyId", new Object[] {dbBemDto, bemDTO});
		
		assertEquals("ValidateMarketPlaceGroupPolicyId with null MGPI in EPS", null, dbBemDto.getMarketplaceGroupPolicyId());
	}
	
	@Test//(expected=com.accenture.foundation.common.exception.ApplicationException.class)
	public void testValidateMarketPlaceGroupPolicyId_EmptyMGPI() {

		BenefitEnrollmentMaintenanceDTO bemDTO = EPSValidationTestUtil.createMockBEM();
		bemDTO.getBem().getPolicyInfo().setMarketplaceGroupPolicyIdentifier(null);
		
		BenefitEnrollmentMaintenanceDTO dbBemDto = new BenefitEnrollmentMaintenanceDTO();
		dbBemDto.setMarketplaceGroupPolicyId("MGPI");
		
		try {
			ReflectionTestUtils.invokeMethod(ffmValidatorService, "validateMarketPlaceGroupPolicyId", new Object[] {dbBemDto, bemDTO});
		} catch(ApplicationException appEx) {
			assertTrue("ApplicationException thrown", true);
		}
		assertEquals("ValidateMarketPlaceGroupPolicyId with null MGPI", null, bemDTO.getBem().getPolicyInfo().getMarketplaceGroupPolicyIdentifier());
	}


	// ************** PP7 Phase2 - proration changes to process FFM Prorated Amounts **********
	/*
	 * One subscriber, no effective end date
	 */
	@Test
	public void test_processInboundPremiums_EED_null() {

		LocalDate esd = JAN_1;
		LocalDate eed = null;
		BigDecimal aptc = new BigDecimal("50");
		BigDecimal csr = new BigDecimal("25");
		BigDecimal tpa = new BigDecimal("100");
		BigDecimal tira = new BigDecimal("75");

		// Expected data after replacement, Record 1
		LocalDate expectedESD_EPS1 = esd;
		LocalDate expectedEED_EPS1 = eed;
		BigDecimal expectedAPTC_EPS1 = aptc;
		BigDecimal expectedCSR_EPS1 = csr;
		BigDecimal expectedTPA_EPS1 = tpa;
		BigDecimal expectedTIRA_EPS1 = tira;

		MemberType inboundSubscriber = makeSubscriberMaintenance("5555", JAN_1, null);
		
		AdditionalInfoType ait = new AdditionalInfoType();
		
		ait.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		ait.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));
		
		setAdditionalInfoTypeValue(ait, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait, "CSR", csr, null);
		
		inboundSubscriber.getAdditionalInfo().add(ait);
		
		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setPolicyInfo(makePolicyInfoType("MPGPI-5555", JAN_1, null, PolicyStatus.EFFECTUATED_2));
		bem.getMember().add(inboundSubscriber);
		bemDTO.setBem(bem);

		Map<LocalDate, AdditionalInfoType> epsPremiums = ReflectionTestUtils.invokeMethod(ffmValidatorService, "processInboundPremiums", bemDTO);

		AdditionalInfoType actualPremium1 = epsPremiums.get(esd);

		assertEquals("ESD", expectedESD_EPS1, DateTimeUtil.getLocalDateFromXmlGC(actualPremium1.getEffectiveStartDate()));
		assertEquals("EED", expectedEED_EPS1, DateTimeUtil.getLocalDateFromXmlGC(actualPremium1.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium1.getAPTCAmount());
		assertEquals("CSR", expectedCSR_EPS1, actualPremium1.getCSRAmount());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium1.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium1.getTotalIndividualResponsibilityAmount());
	}

	/*
	 * One subscriber, with effective end date
	 */
	@Test
	public void test_processInboundPremiums_EED_not_null() {

		LocalDate esd = JAN_1;
		LocalDate eed = DEC_31;
		BigDecimal aptc = new BigDecimal("50");
		BigDecimal csr = new BigDecimal("25");
		BigDecimal tpa = new BigDecimal("100");
		BigDecimal tira = new BigDecimal("75");

		// Expected data after replacement, Record 1
		LocalDate expectedESD_EPS1 = esd;
		LocalDate expectedEED_EPS1 = eed;
		BigDecimal expectedAPTC_EPS1 = aptc;
		BigDecimal expectedCSR_EPS1 = csr;
		BigDecimal expectedTPA_EPS1 = tpa;
		BigDecimal expectedTIRA_EPS1 = tira;

		MemberType inboundSubscriber = makeSubscriberMaintenance("5555", JAN_1, DEC_31);
		
		AdditionalInfoType ait = new AdditionalInfoType();
		
		ait.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		ait.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));
		
		setAdditionalInfoTypeValue(ait, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait, "CSR", csr, null);
		
		inboundSubscriber.getAdditionalInfo().add(ait);
		
		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setPolicyInfo(makePolicyInfoType("MPGPI-5555", JAN_1, DEC_31, PolicyStatus.EFFECTUATED_2));
		bem.getMember().add(inboundSubscriber);
		bemDTO.setBem(bem);

		Map<LocalDate, AdditionalInfoType> epsPremiums = ReflectionTestUtils.invokeMethod(ffmValidatorService, "processInboundPremiums", bemDTO);

		AdditionalInfoType actualPremium1 = epsPremiums.get(esd);

		assertEquals("ESD", expectedESD_EPS1, DateTimeUtil.getLocalDateFromXmlGC(actualPremium1.getEffectiveStartDate()));
		assertEquals("EED", expectedEED_EPS1, DateTimeUtil.getLocalDateFromXmlGC(actualPremium1.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium1.getAPTCAmount());
		assertEquals("CSR", expectedCSR_EPS1, actualPremium1.getCSRAmount());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium1.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium1.getTotalIndividualResponsibilityAmount());
	}

	/*
	 * One subscriber, mid month start
	 */
	@Test
	public void test_processInboundPremiums_EED_mid_month_start() {

		LocalDate esd = JAN_15;
		LocalDate eed = DEC_31;
		BigDecimal aptc = new BigDecimal("50");
		BigDecimal csr = new BigDecimal("25");
		BigDecimal tpa = new BigDecimal("100");
		BigDecimal tira = new BigDecimal("75");

		// Expected data after replacement, Record 1
		LocalDate expectedESD_EPS1 = esd;
		LocalDate expectedEED_EPS1 = eed;
		BigDecimal expectedAPTC_EPS1 = aptc;
		BigDecimal expectedCSR_EPS1 = csr;
		BigDecimal expectedTPA_EPS1 = tpa;
		BigDecimal expectedTIRA_EPS1 = tira;

		MemberType inboundSubscriber = makeSubscriberMaintenance("5555", JAN_15, DEC_31);
		
		AdditionalInfoType ait = new AdditionalInfoType();
		
		ait.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		ait.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));
		
		setAdditionalInfoTypeValue(ait, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait, "CSR", csr, null);
		
		inboundSubscriber.getAdditionalInfo().add(ait);
		
		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setPolicyInfo(makePolicyInfoType("MPGPI-5555", JAN_15, DEC_31, PolicyStatus.EFFECTUATED_2));
		bem.getMember().add(inboundSubscriber);
		bemDTO.setBem(bem);

		Map<LocalDate, AdditionalInfoType> epsPremiums = ReflectionTestUtils.invokeMethod(ffmValidatorService, "processInboundPremiums", bemDTO);

		AdditionalInfoType actualPremium1 = epsPremiums.get(esd);

		assertEquals("ESD", expectedESD_EPS1, DateTimeUtil.getLocalDateFromXmlGC(actualPremium1.getEffectiveStartDate()));
		assertEquals("EED", expectedEED_EPS1, DateTimeUtil.getLocalDateFromXmlGC(actualPremium1.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium1.getAPTCAmount());
		assertEquals("CSR", expectedCSR_EPS1, actualPremium1.getCSRAmount());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium1.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium1.getTotalIndividualResponsibilityAmount());
	}

	/*
	 * One subscriber, mid month end
	 */
	@Test
	public void test_processInboundPremiums_EED_mid_month_end() {

		LocalDate esd = JAN_1;
		LocalDate eed = MAR_14;
		BigDecimal aptc = new BigDecimal("50");
		BigDecimal csr = new BigDecimal("25");
		BigDecimal tpa = new BigDecimal("100");
		BigDecimal tira = new BigDecimal("75");

		// Expected data after replacement, Record 1
		LocalDate expectedESD_EPS1 = esd;
		LocalDate expectedEED_EPS1 = eed;
		BigDecimal expectedAPTC_EPS1 = aptc;
		BigDecimal expectedCSR_EPS1 = csr;
		BigDecimal expectedTPA_EPS1 = tpa;
		BigDecimal expectedTIRA_EPS1 = tira;

		MemberType inboundSubscriber = makeSubscriberMaintenance("5555", JAN_1, MAR_14);
		
		AdditionalInfoType ait = new AdditionalInfoType();
		
		ait.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		ait.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));
		
		setAdditionalInfoTypeValue(ait, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait, "CSR", csr, null);
		
		inboundSubscriber.getAdditionalInfo().add(ait);
		
		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setPolicyInfo(makePolicyInfoType("MPGPI-5555", JAN_1, MAR_14, PolicyStatus.EFFECTUATED_2));
		bem.getMember().add(inboundSubscriber);
		bemDTO.setBem(bem);

		Map<LocalDate, AdditionalInfoType> epsPremiums = ReflectionTestUtils.invokeMethod(ffmValidatorService, "processInboundPremiums", bemDTO);

		AdditionalInfoType actualPremium1 = epsPremiums.get(esd);

		assertEquals("ESD", expectedESD_EPS1, DateTimeUtil.getLocalDateFromXmlGC(actualPremium1.getEffectiveStartDate()));
		assertEquals("EED", expectedEED_EPS1, DateTimeUtil.getLocalDateFromXmlGC(actualPremium1.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium1.getAPTCAmount());
		assertEquals("CSR", expectedCSR_EPS1, actualPremium1.getCSRAmount());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium1.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium1.getTotalIndividualResponsibilityAmount());
	}

	/*
	 * FDD Scenario 1 - One subscriber, mid month start
	 */
	@Test
	public void test_processInboundPremiums_mid_month_start() {

		LocalDate esd = FEB_1;
		LocalDate eed = DEC_31;
		BigDecimal aptc = new BigDecimal("100");
		BigDecimal csr = new BigDecimal("50");
		BigDecimal tpa = new BigDecimal("150");
		BigDecimal tira = new BigDecimal("100");

		// Expected data after replacement, Record 1
		LocalDate expectedESD_EPS1 = JAN_15;
		LocalDate expectedEED_EPS1 = eed;
		BigDecimal expectedAPTC_EPS1 = aptc;
		BigDecimal expectedCSR_EPS1 = csr;
		BigDecimal expectedTPA_EPS1 = tpa;
		BigDecimal expectedTIRA_EPS1 = tira;

		MemberType inboundSubscriber = makeSubscriberMaintenance("5555", JAN_15, DEC_31);
		
		AdditionalInfoType ait = new AdditionalInfoType();
		
		ait.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		ait.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));
		
		setAdditionalInfoTypeValue(ait, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait, "CSR", csr, null);
		
		inboundSubscriber.getAdditionalInfo().add(ait);
		
		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setPolicyInfo(makePolicyInfoType("MPGPI-5555", JAN_15, DEC_31, PolicyStatus.EFFECTUATED_2));
		bem.getMember().add(inboundSubscriber);
		bemDTO.setBem(bem);

		Map<LocalDate, AdditionalInfoType> epsPremiums = ReflectionTestUtils.invokeMethod(ffmValidatorService, "processInboundPremiums", bemDTO);

		assertEquals("Only one premium record", 1, epsPremiums.size());

		AdditionalInfoType actualPremium1 = epsPremiums.get(JAN_15);
		
		assertEquals("ESD", expectedESD_EPS1, DateTimeUtil.getLocalDateFromXmlGC(actualPremium1.getEffectiveStartDate()));
		assertEquals("EED", expectedEED_EPS1, DateTimeUtil.getLocalDateFromXmlGC(actualPremium1.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium1.getAPTCAmount());
		assertEquals("CSR", expectedCSR_EPS1, actualPremium1.getCSRAmount());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium1.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium1.getTotalIndividualResponsibilityAmount());
	}

	/*
	 * FDD Scenario 2 - One subscriber, mid month end
	 */
	@Test
	public void test_processInboundPremiums_mid_month_end() {

		LocalDate esd = JAN_1;
		LocalDate eed = APR_15;
		BigDecimal aptc = new BigDecimal("100");
		BigDecimal csr = new BigDecimal("50");
		BigDecimal tpa = new BigDecimal("150");
		BigDecimal tira = new BigDecimal("100");

		// Expected data after replacement, Record 1
		LocalDate expectedESD_EPS1 = esd;
		LocalDate expectedEED_EPS1 = eed;
		BigDecimal expectedAPTC_EPS1 = aptc;
		BigDecimal expectedCSR_EPS1 = csr;
		BigDecimal expectedTPA_EPS1 = tpa;
		BigDecimal expectedTIRA_EPS1 = tira;

		MemberType inboundSubscriber = makeSubscriberMaintenance("5555", JAN_1, APR_15);
		
		AdditionalInfoType ait = new AdditionalInfoType();
		
		ait.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		ait.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));
		
		setAdditionalInfoTypeValue(ait, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait, "CSR", csr, null);
		
		inboundSubscriber.getAdditionalInfo().add(ait);
		
		
		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setPolicyInfo(makePolicyInfoType("MPGPI-5555", JAN_1, APR_15, PolicyStatus.EFFECTUATED_2));
		bem.getMember().add(inboundSubscriber);
		bemDTO.setBem(bem);

		Map<LocalDate, AdditionalInfoType> epsPremiums = ReflectionTestUtils.invokeMethod(ffmValidatorService, "processInboundPremiums", bemDTO);

		assertEquals("Only one premium record", 1, epsPremiums.size());

		AdditionalInfoType actualPremium1 = epsPremiums.get(esd);

		assertEquals("ESD", expectedESD_EPS1, DateTimeUtil.getLocalDateFromXmlGC(actualPremium1.getEffectiveStartDate()));
		assertEquals("EED", expectedEED_EPS1, DateTimeUtil.getLocalDateFromXmlGC(actualPremium1.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium1.getAPTCAmount());
		assertEquals("CSR", expectedCSR_EPS1, actualPremium1.getCSRAmount());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium1.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium1.getTotalIndividualResponsibilityAmount());
	}

	/*
	 * FDD Scenario 3 - One subscriber, mid month start & end
	 */
	@Test
	public void test_processInboundPremiums_mid_month_start_and_end_current_Ipp() {

		LocalDate esd = FEB_1;
		LocalDate eed = DEC_15;
		BigDecimal aptc = new BigDecimal("100");
		BigDecimal csr = new BigDecimal("50");
		BigDecimal tpa = new BigDecimal("150");
		BigDecimal tira = new BigDecimal("100");

		// Expected data after replacement, Record 1
		LocalDate expectedESD_EPS1 = JAN_15;
		LocalDate expectedEED_EPS1 = eed;
		BigDecimal expectedAPTC_EPS1 = aptc;
		BigDecimal expectedCSR_EPS1 = csr;
		BigDecimal expectedTPA_EPS1 = tpa;
		BigDecimal expectedTIRA_EPS1 = tira;

		MemberType inboundSubscriber = makeSubscriberMaintenance("5555", JAN_15, DEC_15);
		
		AdditionalInfoType ait = new AdditionalInfoType();
		
		ait.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		ait.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));
		
		setAdditionalInfoTypeValue(ait, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait, "CSR", csr, null);
		
		inboundSubscriber.getAdditionalInfo().add(ait);
		

		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setPolicyInfo(makePolicyInfoType("MPGPI-5555", JAN_15, DEC_15, PolicyStatus.EFFECTUATED_2));
		bem.getMember().add(inboundSubscriber);
		bemDTO.setBem(bem);

		Map<LocalDate, AdditionalInfoType> epsPremiums = ReflectionTestUtils.invokeMethod(ffmValidatorService, "processInboundPremiums", bemDTO);

		assertEquals("Only one premium record", 1, epsPremiums.size());

		AdditionalInfoType actualPremium1 = epsPremiums.get(JAN_15);

		assertEquals("ESD", expectedESD_EPS1, DateTimeUtil.getLocalDateFromXmlGC(actualPremium1.getEffectiveStartDate()));
		assertEquals("EED", expectedEED_EPS1, DateTimeUtil.getLocalDateFromXmlGC(actualPremium1.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium1.getAPTCAmount());
		assertEquals("CSR", expectedCSR_EPS1, actualPremium1.getCSRAmount());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium1.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium1.getTotalIndividualResponsibilityAmount());
	}

	/*
	 * FDD Scenario 4 - One subscriber, Intra-month start & end
	 */
	@Test
	public void test_processInboundPremiums_intra_month_start_and_end_current_Ipp() {

		LocalDate esd = FEB_1;
		LocalDate eed = JAN_31;
		BigDecimal aptc = new BigDecimal("100");
		BigDecimal csr = new BigDecimal("50");
		BigDecimal tpa = new BigDecimal("150");
		BigDecimal tira = new BigDecimal("100");

		// Expected data after replacement, Record 1
		LocalDate expectedESD_EPS1 = JAN_15;
		LocalDate expectedEED_EPS1 = eed;
		BigDecimal expectedAPTC_EPS1 = aptc;
		BigDecimal expectedCSR_EPS1 = csr;
		BigDecimal expectedTPA_EPS1 = tpa;
		BigDecimal expectedTIRA_EPS1 = tira;

		MemberType inboundSubscriber = makeSubscriberMaintenance("5555", JAN_15, JAN_31);
		
		AdditionalInfoType ait = new AdditionalInfoType();
		
		ait.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		ait.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));
		
		setAdditionalInfoTypeValue(ait, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait, "CSR", csr, null);
		
		inboundSubscriber.getAdditionalInfo().add(ait);
		

		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setPolicyInfo(makePolicyInfoType("MPGPI-5555", JAN_15, JAN_31, PolicyStatus.EFFECTUATED_2));
		bem.getMember().add(inboundSubscriber);
		bemDTO.setBem(bem);

		Map<LocalDate, AdditionalInfoType> epsPremiums = ReflectionTestUtils.invokeMethod(ffmValidatorService, "processInboundPremiums", bemDTO);

		assertEquals("Only one premium record", 1, epsPremiums.size());

		AdditionalInfoType actualPremium1 = epsPremiums.get(JAN_15);

		assertEquals("ESD", expectedESD_EPS1, DateTimeUtil.getLocalDateFromXmlGC(actualPremium1.getEffectiveStartDate()));
		assertEquals("EED", expectedEED_EPS1, DateTimeUtil.getLocalDateFromXmlGC(actualPremium1.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium1.getAPTCAmount());
		assertEquals("CSR", expectedCSR_EPS1, actualPremium1.getCSRAmount());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium1.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium1.getTotalIndividualResponsibilityAmount());
	}

	/*
	 * FDD Scenario 5, preceding prorated amount (second subscriber)
	 */
	@Test
	public void test_processInboundPremiums_preceding_prorated() {

		LocalDate esd = FEB_1;
		LocalDate eed = DEC_31;
		BigDecimal aptc = new BigDecimal("100");
		BigDecimal csr = new BigDecimal("50");
		BigDecimal tpa = new BigDecimal("150");
		BigDecimal tira = new BigDecimal("100");

		LocalDate esd2 = JAN_15;
		LocalDate eed2 = JAN_31;
		BigDecimal proratedAptc2 = new BigDecimal("54.84");
		BigDecimal proratedCsr2 = new BigDecimal("27.42");
		BigDecimal proratedTpa2 = new BigDecimal("82.26");
		BigDecimal proratedTira2 = new BigDecimal("54.84");

		// Expected data after replacement, Record 1
		LocalDate expectedESD_EPS1 = esd;
		LocalDate expectedEED_EPS1 = eed;
		BigDecimal expectedAPTC_EPS1 = aptc;
		BigDecimal expectedCSR_EPS1 = csr;
		BigDecimal expectedTPA_EPS1 = tpa;
		BigDecimal expectedTIRA_EPS1 = tira;

		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setPolicyInfo(new PolicyInfoType());
		// Set same as monthly premium start/end (esd and eed).
		bem.getPolicyInfo().setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		bem.getPolicyInfo().setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));

		MemberType inboundSubscriber = makeSubscriberMaintenance("5555", JAN_15, DEC_31);
		
		AdditionalInfoType ait = new AdditionalInfoType();
		
		ait.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		ait.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));
		
		setAdditionalInfoTypeValue(ait, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait, "CSR", csr, null);
		
		inboundSubscriber.getAdditionalInfo().add(ait);
		bem.getMember().add(inboundSubscriber);

		MemberType inboundSubscriber2 = makeSubscriberMaintenance("6666", null, null);
		
		AdditionalInfoType ait2 = new AdditionalInfoType();
		
		ait2.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd2));
		ait2.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed2));
		
		setAdditionalInfoTypeValue(ait2, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait2, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait2, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait2, "CSR", csr, null);
		setAdditionalInfoTypeValue(ait2, "P-TPA", tpa, proratedTpa2);
		setAdditionalInfoTypeValue(ait2, "P-TIRA", tira, proratedTira2);
		setAdditionalInfoTypeValue(ait2, "P-APTC", aptc, proratedAptc2);
		setAdditionalInfoTypeValue(ait2, "P-CSR", csr, proratedCsr2);
		
		inboundSubscriber2.getAdditionalInfo().add(ait2);
		
		bem.getMember().add(inboundSubscriber2);

		bemDTO.setBem(bem);

		Map<LocalDate, AdditionalInfoType> epsPremiums = ReflectionTestUtils.invokeMethod(ffmValidatorService, "processInboundPremiums", bemDTO);

		assertEquals("Two premium records", 2, epsPremiums.size());

		AdditionalInfoType actualPremium1 = epsPremiums.get(esd);
		assertEquals("ESD", expectedESD_EPS1, DateTimeUtil.getLocalDateFromXmlGC(actualPremium1.getEffectiveStartDate()));
		assertEquals("EED", expectedEED_EPS1, DateTimeUtil.getLocalDateFromXmlGC(actualPremium1.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium1.getAPTCAmount());
		assertEquals("CSR", expectedCSR_EPS1, actualPremium1.getCSRAmount());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium1.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium1.getTotalIndividualResponsibilityAmount());

		AdditionalInfoType actualPremium2 = epsPremiums.get(esd2);
		assertEquals("ESD", esd2, DateTimeUtil.getLocalDateFromXmlGC(actualPremium2.getEffectiveStartDate()));
		assertEquals("EED", eed2, DateTimeUtil.getLocalDateFromXmlGC(actualPremium2.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium2.getAPTCAmount());
		assertEquals("CSR", expectedCSR_EPS1, actualPremium2.getCSRAmount());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium2.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium2.getTotalIndividualResponsibilityAmount());
		assertEquals("P-APTC", proratedAptc2, actualPremium2.getProratedAppliedAPTCAmount());
		assertEquals("P-CSR", proratedCsr2, actualPremium2.getProratedCSRAmount());
		assertEquals("P-TPA", proratedTpa2, actualPremium2.getProratedMonthlyPremiumAmount());
		assertEquals("P-TIRA", proratedTira2, actualPremium2.getProratedIndividualResponsibleAmount());
	}

	/*
	 * FDD Scenario 6, trailing prorated amount (second subscriber) - mid-month end with prorated amounts in FFM
	 */
	@Test
	public void test_processInboundPremiums_trailing_prorated() {

		LocalDate esd = JAN_1;
		LocalDate eed = APR_15;
		BigDecimal aptc = new BigDecimal("100");
		BigDecimal csr = new BigDecimal("50");
		BigDecimal tpa = new BigDecimal("150");
		BigDecimal tira = new BigDecimal("100");

		LocalDate esd2 = APR_1;
		LocalDate eed2 = APR_15;
		BigDecimal proratedAptc2 = new BigDecimal("50");
		BigDecimal proratedCsr2 = new BigDecimal("25");
		BigDecimal proratedTpa2 = new BigDecimal("75");
		BigDecimal proratedTira2 = new BigDecimal("50");

		// Expected data after replacement, Record 1
		LocalDate expectedESD_EPS1 = esd;
		LocalDate expectedEED_EPS1 = esd2.minusDays(1);
		BigDecimal expectedAPTC_EPS1 = aptc;
		BigDecimal expectedCSR_EPS1 = csr;
		BigDecimal expectedTPA_EPS1 = tpa;
		BigDecimal expectedTIRA_EPS1 = tira;

		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setPolicyInfo(makePolicyInfoType("MPGPI-5555", JAN_1, APR_15, PolicyStatus.EFFECTUATED_2));

		MemberType inboundSubscriber = makeSubscriberMaintenance("5555", JAN_1, APR_15);
		
		AdditionalInfoType ait = new AdditionalInfoType();
		
		ait.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		ait.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));
		
		setAdditionalInfoTypeValue(ait, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait, "CSR", csr, null);
		
		inboundSubscriber.getAdditionalInfo().add(ait);
		bem.getMember().add(inboundSubscriber);

		MemberType inboundSubscriber2 = makeSubscriberMaintenance("6666", null, null);
		
		AdditionalInfoType ait2 = new AdditionalInfoType();
		
		ait2.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd2));
		ait2.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed2));
		
		setAdditionalInfoTypeValue(ait2, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait2, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait2, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait2, "CSR", csr, null);
		setAdditionalInfoTypeValue(ait2, "P-TPA", tpa, proratedTpa2);
		setAdditionalInfoTypeValue(ait2, "P-TIRA", tira, proratedTira2);
		setAdditionalInfoTypeValue(ait2, "P-APTC", aptc, proratedAptc2);
		setAdditionalInfoTypeValue(ait2, "P-CSR", csr, proratedCsr2);
		
		inboundSubscriber2.getAdditionalInfo().add(ait2);
		bem.getMember().add(inboundSubscriber2);

		bemDTO.setBem(bem);

		Map<LocalDate, AdditionalInfoType> epsPremiums = ReflectionTestUtils.invokeMethod(ffmValidatorService, "processInboundPremiums", bemDTO);

		assertEquals("Two premium records", 2, epsPremiums.size());

		AdditionalInfoType actualPremium1 = epsPremiums.get(esd);
		assertEquals("ESD", expectedESD_EPS1, DateTimeUtil.getLocalDateFromXmlGC(actualPremium1.getEffectiveStartDate()));
		assertEquals("EED", expectedEED_EPS1, DateTimeUtil.getLocalDateFromXmlGC(actualPremium1.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium1.getAPTCAmount());
		assertEquals("CSR", expectedCSR_EPS1, actualPremium1.getCSRAmount());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium1.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium1.getTotalIndividualResponsibilityAmount());

		AdditionalInfoType actualPremium2 = epsPremiums.get(esd2);
		assertEquals("ESD", esd2, DateTimeUtil.getLocalDateFromXmlGC(actualPremium2.getEffectiveStartDate()));
		assertEquals("EED", eed2, DateTimeUtil.getLocalDateFromXmlGC(actualPremium2.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium2.getAPTCAmount());
		assertEquals("CSR", expectedCSR_EPS1, actualPremium2.getCSRAmount());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium2.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium2.getTotalIndividualResponsibilityAmount());
		assertEquals("P-APTC", proratedAptc2, actualPremium2.getProratedAppliedAPTCAmount());
		assertEquals("P-CSR", proratedCsr2, actualPremium2.getProratedCSRAmount());
		assertEquals("P-TPA", proratedTpa2, actualPremium2.getProratedMonthlyPremiumAmount());
		assertEquals("P-TIRA", proratedTira2, actualPremium2.getProratedIndividualResponsibleAmount());

		assertEquals("Only one subscriber loops", 1, BEMDataUtil.getSubscriberOccurrances(bem).size());

		MemberType expectedSubscriber = BEMDataUtil.getSubscriberOccurrances(bem).get(0);
		assertEquals("Only one subscriber loops", inboundSubscriber.getSubscriberID(), expectedSubscriber.getSubscriberID());

	}

	/*
	 * FDD Scenario 7, mid month start and end (second, third subscriber)
	 */
	@Test
	public void test_processInboundPremiums_mid_month_start_and_end() {

		LocalDate esd = FEB_1;
		LocalDate eed = DEC_15;
		BigDecimal aptc = new BigDecimal("100");
		BigDecimal csr = new BigDecimal("50");
		BigDecimal tpa = new BigDecimal("150");
		BigDecimal tira = new BigDecimal("100");

		LocalDate esd2 = JAN_15;
		LocalDate eed2 = JAN_31;
		BigDecimal proratedAptc2 = new BigDecimal("54.84");
		BigDecimal proratedCsr2 = new BigDecimal("27.42");
		BigDecimal proratedTpa2 = new BigDecimal("82.26");
		BigDecimal proratedTira2 = new BigDecimal("54.84");

		LocalDate esd3 = DEC_1;
		LocalDate eed3 = DEC_15;
		BigDecimal proratedAptc3 = new BigDecimal("48.39");
		BigDecimal proratedCsr3 = new BigDecimal("24.19");
		BigDecimal proratedTpa3 = new BigDecimal("72.58");
		BigDecimal proratedTira3 = new BigDecimal("48.39");

		// Expected data after replacement, Record 1
		LocalDate expectedESD_EPS1 = esd;
		LocalDate expectedEED_EPS1 = esd3.minusDays(1);
		BigDecimal expectedAPTC_EPS1 = aptc;
		BigDecimal expectedCSR_EPS1 = csr;
		BigDecimal expectedTPA_EPS1 = tpa;
		BigDecimal expectedTIRA_EPS1 = tira;

		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setPolicyInfo(makePolicyInfoType("MPGPI-5555", JAN_15, DEC_15, PolicyStatus.EFFECTUATED_2));

		MemberType inboundSubscriber = makeSubscriberMaintenance("5555", JAN_15, DEC_15);
		
		AdditionalInfoType ait = new AdditionalInfoType();
		
		ait.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		ait.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));
		
		setAdditionalInfoTypeValue(ait, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait, "CSR", csr, null);
		
		inboundSubscriber.getAdditionalInfo().add(ait);
		bem.getMember().add(inboundSubscriber);

		MemberType inboundSubscriber2 = makeSubscriberMaintenance("6666", null, null);
		
		AdditionalInfoType ait2 = new AdditionalInfoType();
		
		ait2.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd2));
		ait2.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed2));
		
		setAdditionalInfoTypeValue(ait2, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait2, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait2, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait2, "CSR", csr, null);
		setAdditionalInfoTypeValue(ait2, "P-TPA", tpa, proratedTpa2);
		setAdditionalInfoTypeValue(ait2, "P-TIRA", tira, proratedTira2);
		setAdditionalInfoTypeValue(ait2, "P-APTC", aptc, proratedAptc2);
		setAdditionalInfoTypeValue(ait2, "P-CSR", csr, proratedCsr2);
		
		inboundSubscriber2.getAdditionalInfo().add(ait2);
		bem.getMember().add(inboundSubscriber2);

		MemberType inboundSubscriber3 = makeSubscriberMaintenance("7777", null, null);
		
		AdditionalInfoType ait3 = new AdditionalInfoType();
		
		ait3.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd3));
		ait3.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed3));
		
		setAdditionalInfoTypeValue(ait3, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait3, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait3, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait3, "CSR", csr, null);
		setAdditionalInfoTypeValue(ait3, "P-TPA", tpa, proratedTpa3);
		setAdditionalInfoTypeValue(ait3, "P-TIRA", tira, proratedTira3);
		setAdditionalInfoTypeValue(ait3, "P-APTC", aptc, proratedAptc3);
		setAdditionalInfoTypeValue(ait3, "P-CSR", csr, proratedCsr3);
		
		inboundSubscriber3.getAdditionalInfo().add(ait3);
		bem.getMember().add(inboundSubscriber3);

		bemDTO.setBem(bem);

		Map<LocalDate, AdditionalInfoType> epsPremiums = ReflectionTestUtils.invokeMethod(ffmValidatorService, "processInboundPremiums", bemDTO);

		assertEquals("Three premium records", 3, epsPremiums.size());

		AdditionalInfoType actualPremium1 = epsPremiums.get(esd);
		assertEquals("ESD", expectedESD_EPS1, DateTimeUtil.getLocalDateFromXmlGC(actualPremium1.getEffectiveStartDate()));
		assertEquals("EED", expectedEED_EPS1, DateTimeUtil.getLocalDateFromXmlGC(actualPremium1.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium1.getAPTCAmount());
		assertEquals("CSR", expectedCSR_EPS1, actualPremium1.getCSRAmount());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium1.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium1.getTotalIndividualResponsibilityAmount());

		AdditionalInfoType actualPremium2 = epsPremiums.get(esd2);
		assertEquals("ESD", esd2, DateTimeUtil.getLocalDateFromXmlGC(actualPremium2.getEffectiveStartDate()));
		assertEquals("EED", eed2, DateTimeUtil.getLocalDateFromXmlGC(actualPremium2.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium2.getAPTCAmount());
		assertEquals("CSR", expectedCSR_EPS1, actualPremium2.getCSRAmount());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium2.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium2.getTotalIndividualResponsibilityAmount());
		assertEquals("P-APTC", proratedAptc2, actualPremium2.getProratedAppliedAPTCAmount());
		assertEquals("P-CSR", proratedCsr2, actualPremium2.getProratedCSRAmount());
		assertEquals("P-TPA", proratedTpa2, actualPremium2.getProratedMonthlyPremiumAmount());
		assertEquals("P-TIRA", proratedTira2, actualPremium2.getProratedIndividualResponsibleAmount());

		AdditionalInfoType actualPremium3 = epsPremiums.get(esd3);
		assertEquals("ESD", esd3, DateTimeUtil.getLocalDateFromXmlGC(actualPremium3.getEffectiveStartDate()));
		assertEquals("EED", eed3, DateTimeUtil.getLocalDateFromXmlGC(actualPremium3.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium3.getAPTCAmount());
		assertEquals("CSR", expectedCSR_EPS1, actualPremium3.getCSRAmount());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium3.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium3.getTotalIndividualResponsibilityAmount());
		assertEquals("P-APTC", proratedAptc3, actualPremium3.getProratedAppliedAPTCAmount());
		assertEquals("P-CSR", proratedCsr3, actualPremium3.getProratedCSRAmount());
		assertEquals("P-TPA", proratedTpa3, actualPremium3.getProratedMonthlyPremiumAmount());
		assertEquals("P-TIRA", proratedTira3, actualPremium3.getProratedIndividualResponsibleAmount());

		assertEquals("Only one subscriber loops", 1, BEMDataUtil.getSubscriberOccurrances(bem).size());

		MemberType expectedSubscriber = BEMDataUtil.getSubscriberOccurrances(bem).get(0);
		assertEquals("Only one subscriber loops", inboundSubscriber.getSubscriberID(), expectedSubscriber.getSubscriberID());
	}

	/*
	 * FDD Scenario 7A, mid month start and end (second, third subscriber)
	 */
	@Test
	public void test_processInboundPremiums_mid_month_start_and_end_A() {

		LocalDate esd = FEB_1;
		LocalDate eed = FEB_15;
		BigDecimal aptc = new BigDecimal("100");
		BigDecimal csr = new BigDecimal("50");
		BigDecimal tpa = new BigDecimal("150");
		BigDecimal tira = new BigDecimal("100");

		LocalDate esd2 = JAN_15;
		LocalDate eed2 = JAN_31;
		BigDecimal proratedAptc2 = new BigDecimal("54.84");
		BigDecimal proratedCsr2 = new BigDecimal("27.42");
		BigDecimal proratedTpa2 = new BigDecimal("82.26");
		BigDecimal proratedTira2 = new BigDecimal("54.84");

		LocalDate esd3 = FEB_1;
		LocalDate eed3 = FEB_15;
		BigDecimal proratedAptc3 = new BigDecimal("48.39");
		BigDecimal proratedCsr3 = new BigDecimal("24.19");
		BigDecimal proratedTpa3 = new BigDecimal("72.58");
		BigDecimal proratedTira3 = new BigDecimal("48.39");

		// Expected data after replacement, Record 1
		BigDecimal expectedAPTC_EPS1 = aptc;
		BigDecimal expectedCSR_EPS1 = csr;
		BigDecimal expectedTPA_EPS1 = tpa;
		BigDecimal expectedTIRA_EPS1 = tira;

		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setPolicyInfo(makePolicyInfoType("MPGPI-5555", JAN_15, FEB_15, PolicyStatus.EFFECTUATED_2));

		MemberType inboundSubscriber = makeSubscriberMaintenance("5555", JAN_15, FEB_15);
		
		AdditionalInfoType ait = new AdditionalInfoType();
		
		ait.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		ait.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));
		
		setAdditionalInfoTypeValue(ait, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait, "CSR", csr, null);
		
		inboundSubscriber.getAdditionalInfo().add(ait);
		bem.getMember().add(inboundSubscriber);

		MemberType inboundSubscriber2 = makeSubscriberMaintenance("6666", null, null);
		
		AdditionalInfoType ait2 = new AdditionalInfoType();
		
		ait2.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd2));
		ait2.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed2));
		
		setAdditionalInfoTypeValue(ait2, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait2, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait2, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait2, "CSR", csr, null);
		setAdditionalInfoTypeValue(ait2, "P-TPA", tpa, proratedTpa2);
		setAdditionalInfoTypeValue(ait2, "P-TIRA", tira, proratedTira2);
		setAdditionalInfoTypeValue(ait2, "P-APTC", aptc, proratedAptc2);
		setAdditionalInfoTypeValue(ait2, "P-CSR", csr, proratedCsr2);
		
		inboundSubscriber2.getAdditionalInfo().add(ait2);
		bem.getMember().add(inboundSubscriber2);

		MemberType inboundSubscriber3 = makeSubscriberMaintenance("7777", null, null);
		
		AdditionalInfoType ait3 = new AdditionalInfoType();
		
		ait3.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd3));
		ait3.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed3));
		
		setAdditionalInfoTypeValue(ait3, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait3, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait3, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait3, "CSR", csr, null);
		setAdditionalInfoTypeValue(ait3, "P-TPA", tpa, proratedTpa3);
		setAdditionalInfoTypeValue(ait3, "P-TIRA", tira, proratedTira3);
		setAdditionalInfoTypeValue(ait3, "P-APTC", aptc, proratedAptc3);
		setAdditionalInfoTypeValue(ait3, "P-CSR", csr, proratedCsr3);
		
		inboundSubscriber3.getAdditionalInfo().add(ait3);
		bem.getMember().add(inboundSubscriber3);

		bemDTO.setBem(bem);

		Map<LocalDate, AdditionalInfoType> epsPremiums = ReflectionTestUtils.invokeMethod(ffmValidatorService, "processInboundPremiums", bemDTO);

		assertEquals("Three premium records", 2, epsPremiums.size());

		AdditionalInfoType actualPremium2 = epsPremiums.get(esd2);
		assertEquals("ESD", esd2, DateTimeUtil.getLocalDateFromXmlGC(actualPremium2.getEffectiveStartDate()));
		assertEquals("EED", eed2, DateTimeUtil.getLocalDateFromXmlGC(actualPremium2.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium2.getAPTCAmount());
		assertEquals("CSR", expectedCSR_EPS1, actualPremium2.getCSRAmount());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium2.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium2.getTotalIndividualResponsibilityAmount());
		assertEquals("P-APTC", proratedAptc2, actualPremium2.getProratedAppliedAPTCAmount());
		assertEquals("P-CSR", proratedCsr2, actualPremium2.getProratedCSRAmount());
		assertEquals("P-TPA", proratedTpa2, actualPremium2.getProratedMonthlyPremiumAmount());
		assertEquals("P-TIRA", proratedTira2, actualPremium2.getProratedIndividualResponsibleAmount());

		AdditionalInfoType actualPremium3 = epsPremiums.get(esd3);
		assertEquals("ESD", esd3, DateTimeUtil.getLocalDateFromXmlGC(actualPremium3.getEffectiveStartDate()));
		assertEquals("EED", eed3, DateTimeUtil.getLocalDateFromXmlGC(actualPremium3.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium3.getAPTCAmount());
		assertEquals("CSR", expectedCSR_EPS1, actualPremium3.getCSRAmount());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium3.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium3.getTotalIndividualResponsibilityAmount());
		assertEquals("P-APTC", proratedAptc3, actualPremium3.getProratedAppliedAPTCAmount());
		assertEquals("P-CSR", proratedCsr3, actualPremium3.getProratedCSRAmount());
		assertEquals("P-TPA", proratedTpa3, actualPremium3.getProratedMonthlyPremiumAmount());
		assertEquals("P-TIRA", proratedTira3, actualPremium3.getProratedIndividualResponsibleAmount());

		assertEquals("Only one subscriber loops", 1, BEMDataUtil.getSubscriberOccurrances(bem).size());

		MemberType expectedSubscriber = BEMDataUtil.getSubscriberOccurrances(bem).get(0);
		assertEquals("Only one subscriber loops", inboundSubscriber.getSubscriberID(), expectedSubscriber.getSubscriberID());
	}

	/*
	 * FDD Scenario 8, Intra-month start & end (second subscriber)
	 */
	@Test
	public void test_processInboundPremiums_intra_month_start_and_end() {

		LocalDate esd = FEB_1;
		LocalDate eed = JAN_31;
		BigDecimal aptc = new BigDecimal("100");
		BigDecimal csr = new BigDecimal("50");
		BigDecimal tpa = new BigDecimal("150");
		BigDecimal tira = new BigDecimal("100");

		LocalDate esd2 = JAN_15;
		LocalDate eed2 = JAN_31;
		BigDecimal proratedAptc2 = new BigDecimal("54.84");
		BigDecimal proratedCsr2 = new BigDecimal("27.42");
		BigDecimal proratedTpa2 = new BigDecimal("82.26");
		BigDecimal proratedTira2 = new BigDecimal("54.84");

		// Expected data after replacement, Record 1
		BigDecimal expectedAPTC_EPS1 = aptc;
		BigDecimal expectedCSR_EPS1 = csr;
		BigDecimal expectedTPA_EPS1 = tpa;
		BigDecimal expectedTIRA_EPS1 = tira;

		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setPolicyInfo(makePolicyInfoType("MPGPI-5555", JAN_15, JAN_31, PolicyStatus.EFFECTUATED_2));

		MemberType dependent = new MemberType();
		dependent.setMemberInformation(new MemberRelatedInfoType());
		dependent.getMemberInformation().setSubscriberIndicator(BooleanIndicatorSimpleType.N);
		bem.getMember().add(dependent);

		MemberType inboundSubscriber = makeSubscriberMaintenance("5555", JAN_15, JAN_31);
		
		AdditionalInfoType ait = new AdditionalInfoType();
		
		ait.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		ait.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));
		
		setAdditionalInfoTypeValue(ait, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait, "CSR", csr, null);
		
		inboundSubscriber.getAdditionalInfo().add(ait);
		bem.getMember().add(inboundSubscriber);

		MemberType inboundSubscriber2 = makeSubscriberMaintenance("6666", null, null);
		
		AdditionalInfoType ait2 = new AdditionalInfoType();
		
		ait2.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd2));
		ait2.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed2));
		
		setAdditionalInfoTypeValue(ait2, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait2, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait2, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait2, "CSR", csr, null);
		setAdditionalInfoTypeValue(ait2, "P-TPA", tpa, proratedTpa2);
		setAdditionalInfoTypeValue(ait2, "P-TIRA", tira, proratedTira2);
		setAdditionalInfoTypeValue(ait2, "P-APTC", aptc, proratedAptc2);
		setAdditionalInfoTypeValue(ait2, "P-CSR", csr, proratedCsr2);
		
		inboundSubscriber2.getAdditionalInfo().add(ait2);
		bem.getMember().add(inboundSubscriber2);

		MemberType dependent2 = new MemberType();
		dependent2.setMemberInformation(new MemberRelatedInfoType());
		dependent2.getMemberInformation().setSubscriberIndicator(BooleanIndicatorSimpleType.N);
		bem.getMember().add(dependent2);

		bemDTO.setBem(bem);

		Map<LocalDate, AdditionalInfoType> epsPremiums = ReflectionTestUtils.invokeMethod(ffmValidatorService, "processInboundPremiums", bemDTO);

		assertEquals("Only one premium record", 1, epsPremiums.size());

		AdditionalInfoType actualPremium1 = epsPremiums.get(esd);
		assertNull("Only one premium record", actualPremium1);

		AdditionalInfoType actualPremium2 = epsPremiums.get(esd2);
		assertEquals("ESD", esd2, DateTimeUtil.getLocalDateFromXmlGC(actualPremium2.getEffectiveStartDate()));
		assertEquals("EED", eed2, DateTimeUtil.getLocalDateFromXmlGC(actualPremium2.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium2.getAPTCAmount());
		assertEquals("CSR", expectedCSR_EPS1, actualPremium2.getCSRAmount());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium2.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium2.getTotalIndividualResponsibilityAmount());
		assertEquals("P-APTC", proratedAptc2, actualPremium2.getProratedAppliedAPTCAmount());
		assertEquals("P-CSR", proratedCsr2, actualPremium2.getProratedCSRAmount());
		assertEquals("P-TPA", proratedTpa2, actualPremium2.getProratedMonthlyPremiumAmount());
		assertEquals("P-TIRA", proratedTira2, actualPremium2.getProratedIndividualResponsibleAmount());

		assertEquals("Only one subscriber loops", 1, BEMDataUtil.getSubscriberOccurrances(bem).size());

		MemberType expectedSubscriber = BEMDataUtil.getSubscriberOccurrances(bem).get(0);
		assertEquals("Only one subscriber loops", inboundSubscriber.getSubscriberID(), expectedSubscriber.getSubscriberID());

	}

	/*
	 * FDD Scenario 9, preceding prorated amount (second subscriber), policy start/end = monthly premium start/end
	 */
	@Test
	public void test_processInboundPremiums_FFM_Premium_Start_Date_Policy_Start_Date_Aligned() {

		LocalDate esd = JAN_15;
		LocalDate eed = DEC_31;
		BigDecimal aptc = new BigDecimal("100");
		BigDecimal csr = new BigDecimal("50");
		BigDecimal tpa = new BigDecimal("150");
		BigDecimal tira = new BigDecimal("100");

		LocalDate esd2 = JAN_15;
		LocalDate eed2 = JAN_31;
		BigDecimal proratedAptc2 = new BigDecimal("54.84");
		BigDecimal proratedCsr2 = new BigDecimal("27.42");
		BigDecimal proratedTpa2 = new BigDecimal("82.26");
		BigDecimal proratedTira2 = new BigDecimal("54.84");

		// Expected data after replacement, Record 1
		LocalDate expectedESD_EPS1 = FEB_1;
		LocalDate expectedEED_EPS1 = eed;
		BigDecimal expectedAPTC_EPS1 = aptc;
		BigDecimal expectedCSR_EPS1 = csr;
		BigDecimal expectedTPA_EPS1 = tpa;
		BigDecimal expectedTIRA_EPS1 = tira;

		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setPolicyInfo(new PolicyInfoType());
		// Set same as monthly premium start/end (esd and eed).
		bem.getPolicyInfo().setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		bem.getPolicyInfo().setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));

		MemberType inboundSubscriber = makeSubscriberMaintenance("5555", JAN_15, DEC_31);
		
		AdditionalInfoType ait = new AdditionalInfoType();
		
		ait.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		ait.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));
		
		setAdditionalInfoTypeValue(ait, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait, "CSR", csr, null);
		
		inboundSubscriber.getAdditionalInfo().add(ait);
		bem.getMember().add(inboundSubscriber);

		MemberType inboundSubscriber2 = makeSubscriberMaintenance("6666", null, null);
		
		AdditionalInfoType ait2 = new AdditionalInfoType();
		
		ait2.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd2));
		ait2.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed2));
		
		setAdditionalInfoTypeValue(ait2, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait2, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait2, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait2, "CSR", csr, null);
		setAdditionalInfoTypeValue(ait2, "P-TPA", tpa, proratedTpa2);
		setAdditionalInfoTypeValue(ait2, "P-TIRA", tira, proratedTira2);
		setAdditionalInfoTypeValue(ait2, "P-APTC", aptc, proratedAptc2);
		setAdditionalInfoTypeValue(ait2, "P-CSR", csr, proratedCsr2);
		
		inboundSubscriber2.getAdditionalInfo().add(ait2);
		bem.getMember().add(inboundSubscriber2);

		bemDTO.setBem(bem);

		Map<LocalDate, AdditionalInfoType> epsPremiums = ReflectionTestUtils.invokeMethod(ffmValidatorService, "processInboundPremiums", bemDTO);

		assertEquals("Two premium records", 2, epsPremiums.size());

		AdditionalInfoType actualPremium1 = epsPremiums.get(FEB_1);
		assertEquals("ESD", expectedESD_EPS1, DateTimeUtil.getLocalDateFromXmlGC(actualPremium1.getEffectiveStartDate()));
		assertEquals("EED", expectedEED_EPS1, DateTimeUtil.getLocalDateFromXmlGC(actualPremium1.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium1.getAPTCAmount());
		assertEquals("CSR", expectedCSR_EPS1, actualPremium1.getCSRAmount());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium1.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium1.getTotalIndividualResponsibilityAmount());

		AdditionalInfoType actualPremium2 = epsPremiums.get(esd2);
		assertEquals("ESD", esd2, DateTimeUtil.getLocalDateFromXmlGC(actualPremium2.getEffectiveStartDate()));
		assertEquals("EED", eed2, DateTimeUtil.getLocalDateFromXmlGC(actualPremium2.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium2.getAPTCAmount());
		assertEquals("CSR", expectedCSR_EPS1, actualPremium2.getCSRAmount());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium2.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium2.getTotalIndividualResponsibilityAmount());
		assertEquals("P-APTC", proratedAptc2, actualPremium2.getProratedAppliedAPTCAmount());
		assertEquals("P-CSR", proratedCsr2, actualPremium2.getProratedCSRAmount());
		assertEquals("P-TPA", proratedTpa2, actualPremium2.getProratedMonthlyPremiumAmount());
		assertEquals("P-TIRA", proratedTira2, actualPremium2.getProratedIndividualResponsibleAmount());

		assertEquals("Only one subscriber loops", 1, BEMDataUtil.getSubscriberOccurrances(bem).size());

		MemberType expectedSubscriber = BEMDataUtil.getSubscriberOccurrances(bem).get(0);
		assertEquals("Only one subscriber loops", inboundSubscriber.getSubscriberID(), expectedSubscriber.getSubscriberID());

	}

	/*
	 * FDD Scenario 11, mid month start and end (second, third subscriber), policy start/end = monthly premium start/end
	 */
	@Test
	public void test_processInboundPremiums_mid_month_start_and_end_FFM_Premium_Start_Date_Policy_Start_Date_Aligned() {

		LocalDate esd = JAN_15;
		LocalDate eed = DEC_15;
		BigDecimal aptc = new BigDecimal("100");
		BigDecimal csr = new BigDecimal("50");
		BigDecimal tpa = new BigDecimal("150");
		BigDecimal tira = new BigDecimal("100");

		LocalDate esd2 = JAN_15;
		LocalDate eed2 = JAN_31;
		BigDecimal proratedAptc2 = new BigDecimal("54.84");
		BigDecimal proratedCsr2 = new BigDecimal("27.42");
		BigDecimal proratedTpa2 = new BigDecimal("82.26");
		BigDecimal proratedTira2 = new BigDecimal("54.84");

		LocalDate esd3 = DEC_1;
		LocalDate eed3 = DEC_15;
		BigDecimal proratedAptc3 = new BigDecimal("48.39");
		BigDecimal proratedCsr3 = new BigDecimal("24.19");
		BigDecimal proratedTpa3 = new BigDecimal("72.58");
		BigDecimal proratedTira3 = new BigDecimal("48.39");

		// Expected data after replacement, Record 1
		LocalDate expectedESD_EPS1 = FEB_1;
		LocalDate expectedEED_EPS1 = esd3.minusDays(1);
		BigDecimal expectedAPTC_EPS1 = aptc;
		BigDecimal expectedCSR_EPS1 = csr;
		BigDecimal expectedTPA_EPS1 = tpa;
		BigDecimal expectedTIRA_EPS1 = tira;

		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setPolicyInfo(makePolicyInfoType("MPGPI-5555", JAN_15, DEC_15, PolicyStatus.EFFECTUATED_2));

		MemberType inboundSubscriber = makeSubscriberMaintenance("5555", JAN_15, DEC_15);
		
		AdditionalInfoType ait = new AdditionalInfoType();
		
		ait.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		ait.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));
		
		setAdditionalInfoTypeValue(ait, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait, "CSR", csr, null);
		
		inboundSubscriber.getAdditionalInfo().add(ait);
		bem.getMember().add(inboundSubscriber);

		MemberType inboundSubscriber2 = makeSubscriberMaintenance("6666", null, null);
		
		AdditionalInfoType ait2 = new AdditionalInfoType();
		
		ait2.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd2));
		ait2.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed2));
		
		setAdditionalInfoTypeValue(ait2, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait2, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait2, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait2, "CSR", csr, null);
		setAdditionalInfoTypeValue(ait2, "P-TPA", tpa, proratedTpa2);
		setAdditionalInfoTypeValue(ait2, "P-TIRA", tira, proratedTira2);
		setAdditionalInfoTypeValue(ait2, "P-APTC", aptc, proratedAptc2);
		setAdditionalInfoTypeValue(ait2, "P-CSR", csr, proratedCsr2);
		
		inboundSubscriber2.getAdditionalInfo().add(ait2);
		bem.getMember().add(inboundSubscriber2);

		MemberType inboundSubscriber3 = makeSubscriberMaintenance("7777", null, null);
		
		AdditionalInfoType ait3 = new AdditionalInfoType();
		
		ait3.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd3));
		ait3.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed3));
		
		setAdditionalInfoTypeValue(ait3, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait3, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait3, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait3, "CSR", csr, null);
		setAdditionalInfoTypeValue(ait3, "P-TPA", tpa, proratedTpa3);
		setAdditionalInfoTypeValue(ait3, "P-TIRA", tira, proratedTira3);
		setAdditionalInfoTypeValue(ait3, "P-APTC", aptc, proratedAptc3);
		setAdditionalInfoTypeValue(ait3, "P-CSR", csr, proratedCsr3);
		
		inboundSubscriber3.getAdditionalInfo().add(ait3);
		bem.getMember().add(inboundSubscriber3);

		bemDTO.setBem(bem);

		Map<LocalDate, AdditionalInfoType> epsPremiums = ReflectionTestUtils.invokeMethod(ffmValidatorService, "processInboundPremiums", bemDTO);

		assertEquals("Three premium records", 3, epsPremiums.size());

		AdditionalInfoType actualPremium1 = epsPremiums.get(FEB_1);
		assertEquals("ESD", expectedESD_EPS1, DateTimeUtil.getLocalDateFromXmlGC(actualPremium1.getEffectiveStartDate()));
		assertEquals("EED", expectedEED_EPS1, DateTimeUtil.getLocalDateFromXmlGC(actualPremium1.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium1.getAPTCAmount());
		assertEquals("CSR", expectedCSR_EPS1, actualPremium1.getCSRAmount());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium1.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium1.getTotalIndividualResponsibilityAmount());

		AdditionalInfoType actualPremium2 = epsPremiums.get(esd2);
		assertEquals("ESD", esd2, DateTimeUtil.getLocalDateFromXmlGC(actualPremium2.getEffectiveStartDate()));
		assertEquals("EED", eed2, DateTimeUtil.getLocalDateFromXmlGC(actualPremium2.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium2.getAPTCAmount());
		assertEquals("CSR", expectedCSR_EPS1, actualPremium2.getCSRAmount());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium2.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium2.getTotalIndividualResponsibilityAmount());
		assertEquals("P-APTC", proratedAptc2, actualPremium2.getProratedAppliedAPTCAmount());
		assertEquals("P-CSR", proratedCsr2, actualPremium2.getProratedCSRAmount());
		assertEquals("P-TPA", proratedTpa2, actualPremium2.getProratedMonthlyPremiumAmount());
		assertEquals("P-TIRA", proratedTira2, actualPremium2.getProratedIndividualResponsibleAmount());

		AdditionalInfoType actualPremium3 = epsPremiums.get(esd3);
		assertEquals("ESD", esd3, DateTimeUtil.getLocalDateFromXmlGC(actualPremium3.getEffectiveStartDate()));
		assertEquals("EED", eed3, DateTimeUtil.getLocalDateFromXmlGC(actualPremium3.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium3.getAPTCAmount());
		assertEquals("CSR", expectedCSR_EPS1, actualPremium3.getCSRAmount());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium3.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium3.getTotalIndividualResponsibilityAmount());
		assertEquals("P-APTC", proratedAptc3, actualPremium3.getProratedAppliedAPTCAmount());
		assertEquals("P-CSR", proratedCsr3, actualPremium3.getProratedCSRAmount());
		assertEquals("P-TPA", proratedTpa3, actualPremium3.getProratedMonthlyPremiumAmount());
		assertEquals("P-TIRA", proratedTira3, actualPremium3.getProratedIndividualResponsibleAmount());

		assertEquals("Only one subscriber loops", 1, BEMDataUtil.getSubscriberOccurrances(bem).size());

		MemberType expectedSubscriber = BEMDataUtil.getSubscriberOccurrances(bem).get(0);
		assertEquals("Only one subscriber loops", inboundSubscriber.getSubscriberID(), expectedSubscriber.getSubscriberID());
	}

	/*
	 * FDD Scenario 11A, mid month start and end (second, third subscriber), policy start/end = monthly premium start/end
	 */
	@Test
	public void test_processInboundPremiums_mid_month_start_and_end_FFM_Premium_Start_Date_Policy_Start_Date_Aligned_1() {

		LocalDate esd = JAN_15;
		LocalDate eed = FEB_15;
		BigDecimal aptc = new BigDecimal("100");
		BigDecimal csr = new BigDecimal("50");
		BigDecimal tpa = new BigDecimal("150");
		BigDecimal tira = new BigDecimal("100");

		LocalDate esd2 = JAN_15;
		LocalDate eed2 = JAN_31;
		BigDecimal proratedAptc2 = new BigDecimal("54.84");
		BigDecimal proratedCsr2 = new BigDecimal("27.42");
		BigDecimal proratedTpa2 = new BigDecimal("82.26");
		BigDecimal proratedTira2 = new BigDecimal("54.84");

		LocalDate esd3 = FEB_1;
		LocalDate eed3 = FEB_15;
		BigDecimal proratedAptc3 = new BigDecimal("48.39");
		BigDecimal proratedCsr3 = new BigDecimal("24.19");
		BigDecimal proratedTpa3 = new BigDecimal("72.58");
		BigDecimal proratedTira3 = new BigDecimal("48.39");

		// Expected data after replacement, Record 1
		BigDecimal expectedAPTC_EPS1 = aptc;
		BigDecimal expectedCSR_EPS1 = csr;
		BigDecimal expectedTPA_EPS1 = tpa;
		BigDecimal expectedTIRA_EPS1 = tira;

		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setPolicyInfo(makePolicyInfoType("MPGPI-5555", JAN_15, FEB_15, PolicyStatus.EFFECTUATED_2));

		MemberType inboundSubscriber = makeSubscriberMaintenance("5555", JAN_15, FEB_15);
		
		AdditionalInfoType ait = new AdditionalInfoType();
		
		ait.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		ait.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));
		
		setAdditionalInfoTypeValue(ait, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait, "CSR", csr, null);
		
		inboundSubscriber.getAdditionalInfo().add(ait);
		bem.getMember().add(inboundSubscriber);

		MemberType inboundSubscriber2 = makeSubscriberMaintenance("6666", null, null);
		
		AdditionalInfoType ait2 = new AdditionalInfoType();
		
		ait2.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd2));
		ait2.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed2));
		
		setAdditionalInfoTypeValue(ait2, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait2, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait2, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait2, "CSR", csr, null);
		setAdditionalInfoTypeValue(ait2, "P-TPA", tpa, proratedTpa2);
		setAdditionalInfoTypeValue(ait2, "P-TIRA", tira, proratedTira2);
		setAdditionalInfoTypeValue(ait2, "P-APTC", aptc, proratedAptc2);
		setAdditionalInfoTypeValue(ait2, "P-CSR", csr, proratedCsr2);
		
		inboundSubscriber2.getAdditionalInfo().add(ait2);
		bem.getMember().add(inboundSubscriber2);

		MemberType inboundSubscriber3 = makeSubscriberMaintenance("7777", null, null);
		
		AdditionalInfoType ait3 = new AdditionalInfoType();
		
		ait3.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd3));
		ait3.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed3));
		
		setAdditionalInfoTypeValue(ait3, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait3, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait3, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait3, "CSR", csr, null);
		setAdditionalInfoTypeValue(ait3, "P-TPA", tpa, proratedTpa3);
		setAdditionalInfoTypeValue(ait3, "P-TIRA", tira, proratedTira3);
		setAdditionalInfoTypeValue(ait3, "P-APTC", aptc, proratedAptc3);
		setAdditionalInfoTypeValue(ait3, "P-CSR", csr, proratedCsr3);
		
		inboundSubscriber3.getAdditionalInfo().add(ait3);
		
		bem.getMember().add(inboundSubscriber3);

		bemDTO.setBem(bem);

		Map<LocalDate, AdditionalInfoType> epsPremiums = ReflectionTestUtils.invokeMethod(ffmValidatorService, "processInboundPremiums", bemDTO);

		assertEquals("Three premium records", 2, epsPremiums.size());

		AdditionalInfoType actualPremium2 = epsPremiums.get(esd2);
		assertEquals("ESD", esd2, DateTimeUtil.getLocalDateFromXmlGC(actualPremium2.getEffectiveStartDate()));
		assertEquals("EED", eed2, DateTimeUtil.getLocalDateFromXmlGC(actualPremium2.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium2.getAPTCAmount());
		assertEquals("CSR", expectedCSR_EPS1, actualPremium2.getCSRAmount());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium2.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium2.getTotalIndividualResponsibilityAmount());
		assertEquals("P-APTC", proratedAptc2, actualPremium2.getProratedAppliedAPTCAmount());
		assertEquals("P-CSR", proratedCsr2, actualPremium2.getProratedCSRAmount());
		assertEquals("P-TPA", proratedTpa2, actualPremium2.getProratedMonthlyPremiumAmount());
		assertEquals("P-TIRA", proratedTira2, actualPremium2.getProratedIndividualResponsibleAmount());

		AdditionalInfoType actualPremium3 = epsPremiums.get(esd3);
		assertEquals("ESD", esd3, DateTimeUtil.getLocalDateFromXmlGC(actualPremium3.getEffectiveStartDate()));
		assertEquals("EED", eed3, DateTimeUtil.getLocalDateFromXmlGC(actualPremium3.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium3.getAPTCAmount());
		assertEquals("CSR", expectedCSR_EPS1, actualPremium3.getCSRAmount());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium3.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium3.getTotalIndividualResponsibilityAmount());
		assertEquals("P-APTC", proratedAptc3, actualPremium3.getProratedAppliedAPTCAmount());
		assertEquals("P-CSR", proratedCsr3, actualPremium3.getProratedCSRAmount());
		assertEquals("P-TPA", proratedTpa3, actualPremium3.getProratedMonthlyPremiumAmount());
		assertEquals("P-TIRA", proratedTira3, actualPremium3.getProratedIndividualResponsibleAmount());

		assertEquals("Only one subscriber loops", 1, BEMDataUtil.getSubscriberOccurrances(bem).size());

		MemberType expectedSubscriber = BEMDataUtil.getSubscriberOccurrances(bem).get(0);
		assertEquals("Only one subscriber loops", inboundSubscriber.getSubscriberID(), expectedSubscriber.getSubscriberID());
	}

	/*
	 * FDD Scenario 12, Intra-month start & end (second subscriber), policy start/end = monthly premium start/end
	 */
	@Test
	public void test_processInboundPremiums_intra_month_start_and_end_FFM_Premium_Start_Date_Policy_Start_Date_Aligned() {

		LocalDate esd = JAN_15;
		LocalDate eed = JAN_31;
		BigDecimal aptc = new BigDecimal("100");
		BigDecimal csr = new BigDecimal("50");
		BigDecimal tpa = new BigDecimal("150");
		BigDecimal tira = new BigDecimal("100");

		LocalDate esd2 = JAN_15;
		LocalDate eed2 = JAN_31;
		BigDecimal proratedAptc2 = new BigDecimal("54.84");
		BigDecimal proratedCsr2 = new BigDecimal("27.42");
		BigDecimal proratedTpa2 = new BigDecimal("82.26");
		BigDecimal proratedTira2 = new BigDecimal("54.84");

		// Expected data after replacement, Record 1
		BigDecimal expectedAPTC_EPS1 = aptc;
		BigDecimal expectedCSR_EPS1 = csr;
		BigDecimal expectedTPA_EPS1 = tpa;
		BigDecimal expectedTIRA_EPS1 = tira;

		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setPolicyInfo(new PolicyInfoType());
		bem.getPolicyInfo().setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_15));
		bem.getPolicyInfo().setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(JAN_31));

		MemberType dependent = new MemberType();
		dependent.setMemberInformation(new MemberRelatedInfoType());
		dependent.getMemberInformation().setSubscriberIndicator(BooleanIndicatorSimpleType.N);
		bem.getMember().add(dependent);

		MemberType inboundSubscriber = makeSubscriberMaintenance("5555", JAN_15, JAN_31);
		
		AdditionalInfoType ait = new AdditionalInfoType();
		
		ait.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		ait.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));
		
		setAdditionalInfoTypeValue(ait, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait, "CSR", csr, null);
		
		inboundSubscriber.getAdditionalInfo().add(ait);
		bem.getMember().add(inboundSubscriber);

		MemberType inboundSubscriber2 = makeSubscriberMaintenance("6666", null, null);
		
		AdditionalInfoType ait2 = new AdditionalInfoType();
		
		ait2.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd2));
		ait2.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed2));
		
		setAdditionalInfoTypeValue(ait2, "TPA", tpa, null);
		setAdditionalInfoTypeValue(ait2, "TIRA", tira, null);
		setAdditionalInfoTypeValue(ait2, "APTC", aptc, null);
		setAdditionalInfoTypeValue(ait2, "CSR", csr, null);
		setAdditionalInfoTypeValue(ait2, "P-TPA", tpa, proratedTpa2);
		setAdditionalInfoTypeValue(ait2, "P-TIRA", tira, proratedTira2);
		setAdditionalInfoTypeValue(ait2, "P-APTC", aptc, proratedAptc2);
		setAdditionalInfoTypeValue(ait2, "P-CSR", csr, proratedCsr2);
		
		inboundSubscriber2.getAdditionalInfo().add(ait2);
		bem.getMember().add(inboundSubscriber2);

		MemberType dependent2 = new MemberType();
		dependent2.setMemberInformation(new MemberRelatedInfoType());
		dependent2.getMemberInformation().setSubscriberIndicator(BooleanIndicatorSimpleType.N);
		bem.getMember().add(dependent2);

		bemDTO.setBem(bem);

		Map<LocalDate, AdditionalInfoType> epsPremiums = ReflectionTestUtils.invokeMethod(ffmValidatorService, "processInboundPremiums", bemDTO);

		assertEquals("Only one premium record", 1, epsPremiums.size());

		AdditionalInfoType actualPremium1 = epsPremiums.get(FEB_1);
		assertNull("Only one premium record", actualPremium1);

		AdditionalInfoType actualPremium2 = epsPremiums.get(esd2);
		assertEquals("ESD", esd2, DateTimeUtil.getLocalDateFromXmlGC(actualPremium2.getEffectiveStartDate()));
		assertEquals("EED", eed2, DateTimeUtil.getLocalDateFromXmlGC(actualPremium2.getEffectiveEndDate()));
		assertEquals("APTC", expectedAPTC_EPS1, actualPremium2.getAPTCAmount());
		assertEquals("CSR", expectedCSR_EPS1, actualPremium2.getCSRAmount());
		assertEquals("TPA", expectedTPA_EPS1, actualPremium2.getTotalPremiumAmount());
		assertEquals("TIRA", expectedTIRA_EPS1, actualPremium2.getTotalIndividualResponsibilityAmount());
		assertEquals("P-APTC", proratedAptc2, actualPremium2.getProratedAppliedAPTCAmount());
		assertEquals("P-CSR", proratedCsr2, actualPremium2.getProratedCSRAmount());
		assertEquals("P-TPA", proratedTpa2, actualPremium2.getProratedMonthlyPremiumAmount());
		assertEquals("P-TIRA", proratedTira2, actualPremium2.getProratedIndividualResponsibleAmount());

		assertEquals("Only one subscriber loops", 1, BEMDataUtil.getSubscriberOccurrances(bem).size());

		MemberType expectedSubscriber = BEMDataUtil.getSubscriberOccurrances(bem).get(0);
		assertEquals("Only one subscriber loops", inboundSubscriber.getSubscriberID(), expectedSubscriber.getSubscriberID());

	}

	protected AdditionalInfoType makeAdditionalInfoType(String type, LocalDate esd, LocalDate eed, BigDecimal amt, BigDecimal proratedAmt) {

		AdditionalInfoType ait = new AdditionalInfoType();
		ait.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		if (eed != null) {
			ait.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));
		}
		if (type.equals("APTC")) {
			ait.setAPTCAmount(amt); 
		} else if (type.equals("TIRA")) {
			ait.setTotalIndividualResponsibilityAmount(amt);
		} else if (type.equals("TPA")) {
			ait.setTotalPremiumAmount(amt);
		} else if (type.equals("CSR")) {
			ait.setCSRAmount(amt);
		} 
		if (type.equals("P-APTC")) {
			ait.setProratedAppliedAPTCAmount(proratedAmt);
		} else if (type.equals("P-TIRA")) {
			ait.setProratedIndividualResponsibleAmount(proratedAmt);
		} else if (type.equals("P-TPA")) {
			ait.setProratedMonthlyPremiumAmount(proratedAmt);
		} else if (type.equals("P-CSR")) {
			ait.setProratedCSRAmount(proratedAmt);
		} 		
		return ait;
	}
	
	protected AdditionalInfoType setAdditionalInfoTypeValue(AdditionalInfoType ait, String type, BigDecimal amt, BigDecimal proratedAmt) {

		if (type.equals("APTC")) {
			ait.setAPTCAmount(amt); 
		} else if (type.equals("TIRA")) {
			ait.setTotalIndividualResponsibilityAmount(amt);
		} else if (type.equals("TPA")) {
			ait.setTotalPremiumAmount(amt);
		} else if (type.equals("CSR")) {
			ait.setCSRAmount(amt);
		} 
		if (type.equals("P-APTC")) {
			ait.setProratedAppliedAPTCAmount(proratedAmt);
		} else if (type.equals("P-TIRA")) {
			ait.setProratedIndividualResponsibleAmount(proratedAmt);
		} else if (type.equals("P-TPA")) {
			ait.setProratedMonthlyPremiumAmount(proratedAmt);
		} else if (type.equals("P-CSR")) {
			ait.setProratedCSRAmount(proratedAmt);
		} 		
		return ait;
	}

	protected MemberType makeSubscriberMaintenance(String id, LocalDate policyStartDt, LocalDate policyEndDate) {

		MemberType subscriber = new MemberType();
		subscriber.setMemberInformation(new MemberRelatedInfoType());
		subscriber.getMemberInformation().setSubscriberIndicator(BooleanIndicatorSimpleType.Y);
		subscriber.setMemberAdditionalIdentifier(new MemberAdditionalIdentifierType());
		subscriber.getMemberAdditionalIdentifier().setExchangeAssignedMemberID(id);
		subscriber.setSubscriberID(id);

		subscriber.setMemberRelatedDates(new MemberRelatedDatesType());

		subscriber.getHealthCoverage().add(new HealthCoverageType());
		subscriber.getHealthCoverage().get(0).setHealthCoverageDates(new HealthCoverageDatesType());

		if (policyStartDt != null)
			subscriber.getHealthCoverage().get(0).getHealthCoverageDates().setBenefitBeginDate(DateTimeUtil.getXMLGregorianCalendar(policyStartDt));

		if (policyEndDate != null) 
			subscriber.getHealthCoverage().get(0).getHealthCoverageDates().setBenefitEndDate(DateTimeUtil.getXMLGregorianCalendar(policyEndDate));

		return subscriber;
	}
	
	private PolicyInfoType makePolicyInfoType(String mgpi, LocalDate psd, LocalDate ped, PolicyStatus policyStatus) {

		PolicyInfoType policyInfo = new PolicyInfoType();
		policyInfo.setMarketplaceGroupPolicyIdentifier(mgpi);
		policyInfo.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(psd));
		policyInfo.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(ped));
		policyInfo.setPolicyStatus(policyStatus.getValue());
		return policyInfo;
	}
	
	private ExtractionStatusType makeExtractionStatus(BigInteger status) {

		ExtractionStatusType extractionStatus = new ExtractionStatusType();
		extractionStatus.setExtractionStatusCode(status);
		extractionStatus.setExtractionStatusText("Extraction Errored out due to connection error");;
		return extractionStatus;
	}

}
