package gov.hhs.cms.ff.fm.eps.ep.util.sbm;

import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import gov.cms.dsh.sbmi.Enrollment;
import gov.cms.dsh.sbmi.FileInformationType;
import gov.cms.dsh.sbmi.FileInformationType.IssuerFileInformation;
import gov.cms.dsh.sbmi.FileInformationType.IssuerFileInformation.IssuerFileSet;
import gov.cms.dsh.sbmi.PolicyMemberType;
import gov.cms.dsh.sbmi.PolicyMemberType.MemberDates;
import gov.cms.dsh.sbmi.PolicyType;
import gov.cms.dsh.sbmi.PolicyType.FinancialInformation;
import gov.cms.dsh.sbmi.ProratedAmountType;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMErrorWarningCode;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMErrorDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileInfo;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileProccessingSummary;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMPremium;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SbmErrWarningLogDTO;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;

public class TestDataSBMUtility {

	private static final int YEAR = LocalDate.now().getYear();

	private static final LocalDate JAN_1 = LocalDate.of(YEAR, 1, 1);
	private static final LocalDate JUN_30 = LocalDate.of(YEAR, 6, 30);
	private static final LocalDate DEC_31 = LocalDate.of(YEAR, 12, 31);

	private static final DateTimeFormatter DTF_FILE = DateTimeFormatter.ofPattern("'D'yyMMdd'.T'HHmmssSSS");

	//SBM will submit SBMI files using defined options:
	// - a single state-wide file for all issuers
	// - multiple files (one per issuer)
	// - and/or a fileset (with multiple files for a single issuer). 
	public static final int FILES_STATE_WIDE = 0;
	public static final int FILES_ONE_PER_ISSUER = 1;
	public static final int FILES_FILESET = 2;

	public static final String[] SBM_STATES = {"NY", "WA", "MN", "CO", "CT", "MD", "KY", "RI", "VT", "CA", "MA", "ID", "DC"};

	public static final String[] MEM_NAMES = {"DAD", "MOM", "SON", "DAU", "BBY1", "BBY2"};

	public static final String ELEMENT_TXT = "ELEMENT NM ";

	public static final String ADDL_INFO_TXT = "ADDL INFO TEXT (SbmFileErrorSeqNum: ";


	private static LocalDateTime getLocalDateTimeWithMicros() {

		int microSec = getRandomNumber(3);
		LocalDateTime ldt = LocalDateTime.now();
		int micros = (microSec * 1000);
		ldt = ldt.plusNanos(micros);
		return ldt;
	}


	/**
	 * Also makes FileInformationType and IssuerFileInformation. 
	 * @param xprId
	 * @param tenantId
	 * @param covYr
	 * @param issuerId
	 * @return
	 */
	public static Enrollment makeEnrollment(Long sbmFileId, String tenantId, int covYr, String issuerId, int issuerFileType) {

		Enrollment enrollment = new Enrollment();
		enrollment.setFileInformation(makeFileInformationType(sbmFileId, tenantId, covYr, issuerId, issuerFileType));
		return enrollment;
	}
	
	/**
	 * Also makes FileInformationType and IssuerFileInformation. 
	 * @param xprId
	 * @param tenantId
	 * @param covYr
	 * @param issuerId
	 * @return
	 */
	public static Enrollment makeEnrollment(String sbmFileId, String tenantId, int covYr, String issuerId, int issuerFileType) {

		Enrollment enrollment = new Enrollment();
		enrollment.setFileInformation(makeFileInformationType(sbmFileId, tenantId, covYr, issuerId, issuerFileType));
		return enrollment;
	}

	/**
	 * Makes FileInformationType with IssuerFileInformation but WITHOUT IssuerFileSet. Call "makeIssuerFileSet" 
	 * to create IssuerFileSet.
	 * 
	 * issuerFileTypes:
	 *   - FILES_STATE_WIDE
	 *   - FILES_ONE_PER_ISSUER
	 *   - FILES_FILESET

	 * @param fileId
	 * @param tenantId
	 * @param covYr
	 * @param issuerId
	 * @return
	 */
	public static FileInformationType makeFileInformationType(Long sbmFileId, String tenantId, int covYr, String issuerId, int issuerFileType) {

		FileInformationType fileInfo = new FileInformationType();
		if (sbmFileId.compareTo(Long.valueOf("10000")) < 0) {
			fileInfo.setFileId(sbmFileId.toString());
		} else {
			fileInfo.setFileId(String.format("%05d", sbmFileId));
		}
		fileInfo.setFileCreateDateTime(DateTimeUtil.getXMLGregorianCalendar(getLocalDateTimeWithMicros()));
		fileInfo.setTenantId(tenantId);
		fileInfo.setCoverageYear(covYr);
		if (issuerFileType != FILES_STATE_WIDE) {
			fileInfo.setIssuerFileInformation(makeIssuerFileInformation(issuerId));	
		}
		return fileInfo;
	}
	
	/**
	 * Makes FileInformationType with IssuerFileInformation but WITHOUT IssuerFileSet. Call "makeIssuerFileSet" 
	 * to create IssuerFileSet.
	 * 
	 * issuerFileTypes:
	 *   - FILES_STATE_WIDE
	 *   - FILES_ONE_PER_ISSUER
	 *   - FILES_FILESET

	 * @param fileId
	 * @param tenantId
	 * @param covYr
	 * @param issuerId
	 * @return
	 */
	public static FileInformationType makeFileInformationType(String sbmFileId, String tenantId, int covYr, String issuerId, int issuerFileType) {

		FileInformationType fileInfo = new FileInformationType();
		
		fileInfo.setFileId(sbmFileId);
		fileInfo.setFileCreateDateTime(DateTimeUtil.getXMLGregorianCalendar(getLocalDateTimeWithMicros()));
		fileInfo.setTenantId(tenantId);
		fileInfo.setCoverageYear(covYr);
		if (issuerFileType != FILES_STATE_WIDE) {
			fileInfo.setIssuerFileInformation(makeIssuerFileInformation(issuerId));	
		}
		return fileInfo;
	}



	/** 
	 * Makes FileInformationType WITHOUT IssuerFileSet.
	 *  issuerFileTypes:
	 *    - FILES_STATE_WIDE
	 *    - FILES_ONE_PER_ISSUER
	 *    - FILES_FILESET
	 * @param covYr
	 * @param issuerFileType
	 * @return
	 */
	public static FileInformationType makeFileInformationType(int covYr, int issuerFileType) {

		Long fileId = new Long(getRandomNumberAsString(9));
		String tenantId = SBM_STATES[getRandomNumber(1)] + "0";
		String issuerId = getRandomNumberAsString(5);
		return makeFileInformationType(fileId, tenantId, covYr, issuerId, issuerFileType);
	}

	public static FileInformationType makeFileInformationType(String tenantId, String issuerId, int issuerFileType) {

		Long fileId = new Long(getRandomNumberAsString(9));
		return makeFileInformationType(fileId, tenantId, YEAR, issuerId, issuerFileType);
	}

	public static FileInformationType makeFileInformationType(String tenantId) {

		Long fileId = new Long(getRandomNumberAsString(9));
		String issuerId = getRandomNumberAsString(5);
		return makeFileInformationType(fileId, tenantId, YEAR, issuerId, FILES_STATE_WIDE);
	}


	/**
	 * Make IssuerFileInformation without IssuerFileSet
	 * Call makeIssuerFileSet to make file sets.
	 * @param issuerId
	 * @return
	 */
	private static IssuerFileInformation makeIssuerFileInformation(String issuerId) {

		IssuerFileInformation issuerFileInfo = new IssuerFileInformation();
		issuerFileInfo.setIssuerId(issuerId);
		// set IssuerFileSet independently
		return issuerFileInfo;
	}

	public static IssuerFileSet makeIssuerFileSet(String fileSetId, int fileNum, int totalIssuerFiles) {

		IssuerFileSet issuerFileSet = new IssuerFileSet();
		issuerFileSet.setIssuerFileSetId(fileSetId);// HIOSID + 99999, First 5 characters should match issuer ID
		issuerFileSet.setFileNumber(fileNum);
		issuerFileSet.setTotalIssuerFiles(totalIssuerFiles);
		return issuerFileSet;
	}

	public static PolicyType makePolicyType(int rcn, String qhpId, String exchangePolicyId) {

		String subscriberId = "SUBID-" + exchangePolicyId;
		boolean isEffect = true;
		String insLnCd = "HLT";
		return makePolicyType(rcn, qhpId, exchangePolicyId, subscriberId, isEffect, insLnCd);
	}

	public static PolicyType makePolicyType(String tenantId, String exchangePolicyId) {

		int rcn = 1;
		String qhpId = makeQhpId(getRandomNumberAsString(5), tenantId);
		String subscriberId = "SUBID-" + exchangePolicyId;
		boolean isEffect = true;
		String insLnCd = "HLT";
		return makePolicyType(rcn, qhpId, exchangePolicyId, subscriberId, isEffect, insLnCd);
	}

	/**
	 * @param rcn - RecordControlNumber
	 * @param qhpId
	 * @param eapId - ExchangeAssignedPolicyId (also used to set IssuerAssignedPolicyId for test data)
	 * @param easId - ExchangeAssingedSubscriberId (also used to set IssuerAssignedSubscriberId for test data)
	 * @param psd - PolicyStartDate
	 * @param ped - PolicyEndDate
	 * @param isEffect - EffectuationIndicator as boolean
	 * @param ilc - InsuranceLineCd
	 * @return
	 */
	public static PolicyType makePolicyType(int rcn, String qhpId, String exchangePolicyId, String subscriberId, boolean isEffect, String insLnCd) {

		PolicyType policy = new PolicyType();
		int idx = getPolicyIndexFromExchangePolicyId(exchangePolicyId);
		policy.setRecordControlNumber(rcn);
		policy.setQHPId(qhpId);
		policy.setExchangeAssignedPolicyId(exchangePolicyId);
		policy.setExchangeAssignedSubscriberId(subscriberId);
		policy.setIssuerAssignedPolicyId(exchangePolicyId);
		policy.setIssuerAssignedSubscriberId("ISSR" + subscriberId);
		policy.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(getStartDate(idx)));
		policy.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(getEndDate(idx)));
		if (isEffect) {
			policy.setEffectuationIndicator("Y");
		} else {
			policy.setEffectuationIndicator("N");
		}
		policy.setInsuranceLineCode(insLnCd);
		return policy;
	}

	private static LocalDate getStartDate(int idx) {

		LocalDate startDt = null;
		switch (idx) {
		case 0: case 1: case 4: case 7:
			startDt = JAN_1;
			break;
		case 2: case 3:  case 5: case 6:
			startDt = LocalDate.of(YEAR, idx, idx);
			break;
			//		case 7:
			//			startDt = LocalDate.of(YEAR, 7, 1);
			//			break;
		case 8:
			startDt = LocalDate.of(YEAR, 8, 1);
			break;
		case 9:
			startDt = LocalDate.of(YEAR, 9, 1);
			break;
		}
		return startDt;
	}

	private static LocalDate getEndDate(int idx) {

		LocalDate endDt = null;
		switch (idx) {
		case 0: case 1: case 4: case 7:
			endDt = DEC_31;
			break;
		case 2: case 3: case 5: case 6:
			endDt = LocalDate.of(YEAR, (idx + idx), idx);
			break;
			//		case 7:
			//			endDt = LocalDate.of(YEAR, 7, 30);
			//			break;
		case 8:
			endDt = LocalDate.of(YEAR, 8, 31);
			break;
		case 9:
			endDt = LocalDate.of(YEAR, 9, 2);
			break;
		}
		return endDt;
	}


	public static PolicyMemberType makePolicyMemberType(String exchangePolicyId, Long memId, String name, boolean isSubscriber) {

		PolicyMemberType member = new PolicyMemberType(); 
		member.setExchangeAssignedMemberId("EAMID-" + memId + "-" + exchangePolicyId);
		if (isSubscriber) {
			member.setSubscriberIndicator("Y");
		} else {
			member.setSubscriberIndicator("N");
		}
		member.setIssuerAssignedMemberId("IAMID-" + memId + "-" + exchangePolicyId);
		String memIdxStr = memId.toString().substring(memId.toString().length() - 1, memId.toString().length());
		int memIdx = Integer.parseInt(memIdxStr);
		if (name.equals("DAD")) {
			member.setNamePrefix("Mr." + name);
			member.setNameSuffix(name + " Sr.");
			LocalDate dobDAD = LocalDate.of(1970 + memIdx, memIdx + 1, memIdx + 10);
			member.setBirthDate(DateTimeUtil.getXMLGregorianCalendar(dobDAD));
			member.setLanguageCode("POL");
			member.setLanguageQualifierCode("LD");
			member.setGenderCode("M");
			member.setRaceEthnicityCode("C");
			member.setTobaccoUseCode("T");
			// TODO need to determine EPS to SBM mapping and vise versa.
			// Set all members to N for now.
			// 'N' is hardcoded in SbmPolicyMemberVersionRowMapper
			member.setNonCoveredSubscriberInd("N");
		} else if (name.equals("MOM")) {
			member.setNamePrefix("Mrs. " + name);
			LocalDate dobMOM = LocalDate.of(1980 + memIdx, memIdx, memIdx + 10);
			member.setBirthDate(DateTimeUtil.getXMLGregorianCalendar(dobMOM));
			member.setLanguageCode("NIC");
			member.setLanguageQualifierCode("LE");
			member.setGenderCode("F");
			member.setRaceEthnicityCode("B");
			member.setTobaccoUseCode("N");
		} else if (name.equals("SON")) {
			member.setNameSuffix(name + " Jr.");
			LocalDate dobSON = LocalDate.of(2000 + memIdx, memIdx, memIdx + 10);
			member.setBirthDate(DateTimeUtil.getXMLGregorianCalendar(dobSON));
			member.setLanguageCode("SOM");
			// missing qualifier for validation error.
			member.setGenderCode("M");
			member.setRaceEthnicityCode("J");
			member.setTobaccoUseCode("T");
		} else if (name.equals("DAU")) {
			member.setNamePrefix("Ms " + name);
			LocalDate dobDAU = LocalDate.of(2010 + memIdx, memIdx, memIdx + 10);
			member.setBirthDate(DateTimeUtil.getXMLGregorianCalendar(dobDAU));
			// incorrect for validation error
			member.setLanguageQualifierCode("DAUX");
			member.setGenderCode("F");
			member.setRaceEthnicityCode("Z");
			member.setTobaccoUseCode("U");
		}  else if (name.indexOf("BBY") != -1) {
			LocalDate dobBBY = LocalDate.of(YEAR, memIdx, memIdx + 10);
			member.setBirthDate(DateTimeUtil.getXMLGregorianCalendar(dobBBY));
			// Don't set language or gender so nulls can be checked.
		}
		member.setMemberLastName(name + "-" + memId + "-" + exchangePolicyId + "-LAST");
		member.setMemberFirstName(name + "-" + memId + "-" + exchangePolicyId + "-FIRST");
		member.setMemberMiddleName(name + "-" + memId + "-" + exchangePolicyId + "-MID");

		if (name.indexOf("BBY") == -1) {
			// VARCHAR2(9 BYTE)
			String str9 = exchangePolicyId + memId.toString();
			str9 = str9.length() > 9 ? str9.substring(0, 9) : str9;
			member.setSocialSecurityNumber(str9);
		}
		// VARCHAR2(15 BYTE)
		// all zip codes same for all members.
		String first4 = exchangePolicyId.substring(0,4);
		String last4 = exchangePolicyId.substring(4,8);
		member.setPostalCode("Z" + first4 + "-" + last4);


		List<MemberDates> memberDates = makeMemberDates(exchangePolicyId, name);
		if (memberDates != null) {
			member.getMemberDates().addAll(memberDates);
		}

		return member;
	}

	private static List<MemberDates> makeMemberDates(String exchangePolicyId, String name) {

		List<PolicyMemberType.MemberDates> memDatesList = new ArrayList<PolicyMemberType.MemberDates>();
		PolicyMemberType.MemberDates memberDate = null;
		int subIdx = getPolicyIndexFromExchangePolicyId(exchangePolicyId);
		LocalDateTime pmsd = null;
		LocalDateTime pmed = null;

		if (name.equals("SON") && subIdx == 1) {
			// Mid month change for one member.
			pmed = LocalDateTime.of(YEAR, 3, 13, 3, 13, 3, 333333);
			pmsd = LocalDateTime.of(YEAR, 3, 14, 4, 14, 4, 444444);
			memberDate = new PolicyMemberType.MemberDates();
			memberDate.setMemberStartDate(DateTimeUtil.getXMLGregorianCalendar(getStartDate(subIdx)));
			memberDate.setMemberEndDate(DateTimeUtil.getXMLGregorianCalendar(pmed));
			memDatesList.add(memberDate);

			memberDate = new PolicyMemberType.MemberDates();
			memberDate.setMemberStartDate(DateTimeUtil.getXMLGregorianCalendar(pmsd));
			memberDate.setMemberEndDate(DateTimeUtil.getXMLGregorianCalendar(getEndDate(subIdx)));
			memDatesList.add(memberDate);

		} else if (name.indexOf("BBY") != -1) {
			memDatesList = null;
		} else {
			memberDate = new PolicyMemberType.MemberDates();
			memberDate.setMemberStartDate(DateTimeUtil.getXMLGregorianCalendar(getStartDate(subIdx)));
			memberDate.setMemberEndDate(DateTimeUtil.getXMLGregorianCalendar(getEndDate(subIdx)));
			memDatesList.add(memberDate);
		}
		return memDatesList;
	}


	public static List<FinancialInformation> makeFinancialInformationList(String exchangePolicyId, String state, String variantId) {

		List<FinancialInformation> financialInfoList = new ArrayList<PolicyType.FinancialInformation>();
		int idx = getPolicyIndexFromExchangePolicyId(exchangePolicyId);
		financialInfoList.add(makeFinancialInformation(idx, state, variantId));

		if (idx >= 8) {

			financialInfoList.add(makeFinancialInformation(idx, state, variantId));
		}
		return financialInfoList;
	}


	/**
	 * Make Policy FinancialInformationType.
	 * ExchangePolicyIds 0, 1, 7, 8, and 9 will NOT create any ProrationAmountTypes.
	 * Pro-ration amounts are only created for exchangePolicyIds ending in 2, 3 ,4, 5, or 6.
	 * @param exchangePolicyId
	 * @param state
	 * @param variantId
	 * @return
	 */
	private static FinancialInformation makeFinancialInformation(int idx, String state, String variantId) {

		FinancialInformation financialInfo = new FinancialInformation();
		LocalDate fesd = getStartDate(idx);
		LocalDate feed = getEndDate(idx);

		financialInfo.setFinancialEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(fesd));
		financialInfo.setFinancialEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(feed));

		BigDecimal tpa = new BigDecimal(1000 + idx);
		financialInfo.setMonthlyTotalPremiumAmount(tpa);
		BigDecimal aptc = tpa.multiply(new BigDecimal(".1"));
		BigDecimal opa1 = tpa.multiply(new BigDecimal(".01")).setScale(2, RoundingMode.HALF_UP);
		BigDecimal opa2 = tpa.multiply(new BigDecimal(".02")).setScale(2, RoundingMode.HALF_UP);
		BigDecimal sum = aptc.add(opa1);
		sum = sum.add(opa2);
		BigDecimal tira = tpa.subtract(sum).setScale(2, RoundingMode.HALF_UP);
		BigDecimal csr = null;
		
		// Validate that the amount is equal to 0 if the Variant ID is 01 and the Monthly CSR amount is present
		// Variant ID is 01 when Monthly CSR amount is not provided
		// Validate that the Variant ID is 02-06 when Monthly CSR amount is provided
		if (variantId.equals("0") || variantId.equals("00")) {
			csr = null;
		} else if (variantId.equals("01")) {
			csr = BigDecimal.ZERO;
		} else {
		 csr = new BigDecimal(idx * .1).setScale(2, RoundingMode.HALF_UP);
		}
		financialInfo.setMonthlyTotalPremiumAmount(tpa);
		financialInfo.setMonthlyAPTCAmount(aptc);
		financialInfo.setMonthlyOtherPaymentAmount1(opa1);
		financialInfo.setMonthlyOtherPaymentAmount2(opa2);
		financialInfo.setMonthlyTotalIndividualResponsibilityAmount(tira);
		financialInfo.setMonthlyCSRAmount(csr);
		financialInfo.setCSRVariantId(variantId);
		financialInfo.setRatingArea(state + "0");

		// Only create proration amounts for policies that have an exchangePolicy ending in 2, 3, 5, 6, 8, or 9.
		if (idx != 1 && idx != 4 && idx != 7 && idx != 0) {
			//financialInfo.getProratedAmount().add(makeProratedAmount(exchangePolicyId));
		}

		return financialInfo;
	}

	public static FinancialInformation makeFinancialInformation(String exchangePolicyId) {

		int idx = getPolicyIndexFromExchangePolicyId(exchangePolicyId);
		String state = "DC";
		String variantId = "01";
		return makeFinancialInformation(idx, state, variantId);
	}

	private static ProratedAmountType makeProratedAmount(int idx) { 

		ProratedAmountType proAmt = new ProratedAmountType();
		proAmt.setPartialMonthEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(getStartDate(idx)));		
		proAmt.setPartialMonthEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(getEndDate(idx)));	

		return proAmt;
	}


	public static SBMPremium makeSBMPremium(String exchangePolicyId, String state, String variantId, boolean isProration) {

		SBMPremium sbmPremium = new SBMPremium();
		int subIdx = getPolicyIndexFromExchangePolicyId(exchangePolicyId);
		LocalDate esd = getStartDate(subIdx);
		LocalDate eed = getEndDate(subIdx);

		BigDecimal tpa = new BigDecimal(1000 + subIdx);
		BigDecimal aptc = tpa.multiply(new BigDecimal(".1"));
		BigDecimal opa1 = tpa.multiply(new BigDecimal(".01")).setScale(2, RoundingMode.HALF_UP);
		BigDecimal opa2 = tpa.multiply(new BigDecimal(".02")).setScale(2, RoundingMode.HALF_UP);
		BigDecimal sum = aptc.add(opa1);
		sum = sum.add(opa2);
		BigDecimal tira = tpa.subtract(sum).setScale(2, RoundingMode.HALF_UP);
		BigDecimal csr = new BigDecimal(subIdx * .1).setScale(2, RoundingMode.HALF_UP);

		sbmPremium.setEffectiveStartDate(esd);
		sbmPremium.setEffectiveEndDate(eed);
		sbmPremium.setTotalPremium(tpa);
		sbmPremium.setOtherPayment1(opa1);
		sbmPremium.setOtherPayment2(opa2);
		sbmPremium.setRatingArea(state + "0");
		sbmPremium.setIndividualResponsibleAmt(tira);
		sbmPremium.setCsr(csr);
		sbmPremium.setAptc(aptc);
		sbmPremium.setCsrVariantId(variantId);

		if (isProration) {

			BigDecimal proTPA = new BigDecimal("1111.11");
			BigDecimal proAPTC = new BigDecimal("2222.22");
			BigDecimal proCSR = new BigDecimal("3333.33");

			sbmPremium.setProratedPremium(proTPA);
			sbmPremium.setProratedAptc(proAPTC);
			sbmPremium.setProratedCsr(proCSR);
		}

		return sbmPremium;
	}


	public static Map<LocalDate, SBMPremium> makeSBMPremiumMap(String exchangePolicyId) {

		Map<LocalDate, SBMPremium> sbmPremiums = new LinkedHashMap<LocalDate, SBMPremium>();

		SBMPremium premium = makeSBMPremium(exchangePolicyId);
		sbmPremiums.put(premium.getEffectiveStartDate(), premium);

		return sbmPremiums;
	}

	public static SBMPremium makeSBMPremium(String exchangePolicyId) {

		String state = "DC";
		String variantId = "01";
		boolean isProration = false;
		return makeSBMPremium(exchangePolicyId, state, variantId, isProration);
	}

	public static SBMFileInfo makeSBMFileInfo(String sourceId, String tradingPartnerId, SBMFileStatus fileStatus) {

		SBMFileInfo sbmFileInfo = new SBMFileInfo();
		sbmFileInfo.setSbmFileNm(makeFileName(sourceId));
		sbmFileInfo.setTradingPartnerId(tradingPartnerId);
		sbmFileInfo.setFunctionCd("FUNC_CD");
		return sbmFileInfo;
	}

	public static SBMFileInfo makeSBMFileInfo() {

		String sourceId = getRandomNumberAsString(3);
		String tradingPartnerId = "TPID-" + sourceId;
		SBMFileStatus fileStatus = SBMFileStatus.ACCEPTED;
		return makeSBMFileInfo(sourceId, tradingPartnerId, fileStatus);
	}

	public static List<SBMErrorDTO> makeSBMErrorDTOList(int listSize) {

		List<SBMErrorDTO> errList = new ArrayList<SBMErrorDTO>();
		for (int i = 0; i < listSize; ++i) {
			errList.add(makeSBMErrorDTO(i));
		}
		return errList;
	}

	/**
	 * Make and SbmFileError from and index.  Using SBMErrorWarningCode.values.  So
	 * i == 0 will be first enum. AdditionalErrorInfoText list will be: length i + 1.
	 * @param i
	 * @return
	 */
	public static SBMErrorDTO makeSBMErrorDTO(int i) {

		SBMErrorDTO err = new SBMErrorDTO();
		SBMErrorWarningCode[] codes = SBMErrorWarningCode.values();
		i = (i > codes.length) ? (i % codes.length) : i;
		err.setSbmErrorWarningTypeCd(codes[i].getCode());
		// Note:  Changing the following text patterns will break assertions in unit tests.
		err.setElementInErrorNm(ELEMENT_TXT + i);
		for (int j = 0; j <= i; ++j) {
			err.getAdditionalErrorInfoList().add(ADDL_INFO_TXT + i + ", " + j + ")");
		}
		return err;
	}

	public static List<SbmErrWarningLogDTO> makeSbmErrWarningLogDTOList(int listSize) {

		List<SbmErrWarningLogDTO> errList = new ArrayList<SbmErrWarningLogDTO>();
		for (int i = 0; i < listSize; ++i) {
			errList.add(makeSbmErrWarningLogDTO(i));
		}
		return errList;
	}

	/**
	 * Make an SbmErrWarningLogDTO from and index.  Using SBMErrorWarningCode.values.  So
	 * i == 0 will be first enum. AdditionalErrorInfoText list will be: length i + 1.
	 * @param i
	 * @return
	 */
	public static SbmErrWarningLogDTO makeSbmErrWarningLogDTO(int i) {

		SbmErrWarningLogDTO err = new SbmErrWarningLogDTO();
		SBMErrorWarningCode[] codes = SBMErrorWarningCode.values();
		i = (i > codes.length) ? (i % codes.length) : i;
		err.setErrorWarningTypeCd(codes[i].getCode());
		// Note:  Changing the following text patterns will break assertions in unit tests.
		err.setElementInError(ELEMENT_TXT + i);
		for (int j = 0; j <= i; ++j) {
			err.getErrorWarningDesc().add(ADDL_INFO_TXT + i + ", " + j + ")");
		}
		err.setExchangeMemberId(makeExchangeMemberId(i));
		return err;
	}

	public static String makeExchangeMemberId(int i) {

		return MEM_NAMES[i] + "-" + i + "" + i + "" + i + "" + i;
	}

	public static SBMFileProccessingSummary makeSbmFileProcessingSummary(String tenantId, String issuerId, String issuerFileSetId) {

		SBMFileProccessingSummary sbmFileProcSum = new SBMFileProccessingSummary();
		sbmFileProcSum.setTenantId(tenantId);
		sbmFileProcSum.setIssuerId(issuerId);
		sbmFileProcSum.setIssuerFileSetId(issuerFileSetId);
		sbmFileProcSum.setCmsApprovedInd("Y");
		sbmFileProcSum.setCmsApprovalRequiredInd("N");
		sbmFileProcSum.setTotalIssuerFileCount(TestDataSBMUtility.getRandomNumber(3));
		sbmFileProcSum.setTotalRecordProcessedCnt(TestDataSBMUtility.getRandomNumber(4));
		sbmFileProcSum.setTotalRecordRejectedCnt(TestDataSBMUtility.getRandomNumber(4));
		sbmFileProcSum.setErrorThresholdPercent(new BigDecimal(".1"));
		sbmFileProcSum.setTotalPreviousPoliciesNotSubmit(TestDataSBMUtility.getRandomNumber(4));
		sbmFileProcSum.setNotSubmittedEffectuatedCnt(TestDataSBMUtility.getRandomNumber(4));
		sbmFileProcSum.setNotSubmittedTerminatedCnt(TestDataSBMUtility.getRandomNumber(4));
		sbmFileProcSum.setNotSubmittedCancelledCnt(TestDataSBMUtility.getRandomNumber(4));
		sbmFileProcSum.setTotalPolicyApprovedCnt(TestDataSBMUtility.getRandomNumber(4));
		sbmFileProcSum.setMatchingPlcNoChangeCnt(TestDataSBMUtility.getRandomNumber(4));
		sbmFileProcSum.setMatchingPlcChgApplCnt(TestDataSBMUtility.getRandomNumber(4));
		sbmFileProcSum.setMatchingPlcCorrectedChgApplCnt(TestDataSBMUtility.getRandomNumber(4));
		sbmFileProcSum.setNewPlcCreatedAsSentCnt(TestDataSBMUtility.getRandomNumber(2));
		sbmFileProcSum.setNewPlcCreatedCorrectionApplCnt(TestDataSBMUtility.getRandomNumber(2));
		sbmFileProcSum.setEffectuatedPolicyCount(TestDataSBMUtility.getRandomNumber(7));
		sbmFileProcSum.setCoverageYear(YEAR);
		sbmFileProcSum.setSbmFileStatusType(SBMFileStatus.IN_PROCESS);
		return sbmFileProcSum;
	}


	/**
	 * File Name =TradingPartnerID.AppId.FuncCode.Date.Time.EnvCode.Direction
	 * 
	 * Trading Partner ID SBM Source ID (for source)
     * App ID - EPS
     * Function Code - SBMI
     * Date - DYYMMDD where the first character ‘D’ is static text and the rest us the date in ‘YYMMDD’ format
     * Time - THHMMSSmmm where the first character ‘T’ is a static text and the rest is the time in ‘HHMMSSmmm’ format
     * Environment Code - P for production, T for testing, R for production readiness
     * Direction - IN
	 * @param sourceId
	 * @return
	 */
	public static String makeZipEntryFileName(String sourceId, String envCd) {

		return sourceId + ".EPS.SBMI." + LocalDateTime.now().format(DTF_FILE) + "." + envCd;
	}
	
	/**
	 * Zip File Name format: FuncCode.TradingPartnerID.Date.Time.EnvCode.Direction
	 * @param sourceId
	 * @return
	 */
	public static String makeFileName(String sourceId, String envCd) {

		return "SBMI." + sourceId + "."+ LocalDateTime.now().format(DTF_FILE) + "." + envCd;
	}

	/**
	 * File Name =TradingPartnerID.AppId.FuncCode.Date.Time.EnvCode.Direction
	 * @param sourceId
	 * @return
	 */
	public static String makeFileName(String sourceId) {

		return sourceId + ".EPS.SBMI." + LocalDateTime.now().format(DTF_FILE) + ".T.IN";
	}

	public static String makeVariantId(int i) {

		return ("0" + (i % 6 + 1)); // generates 01-06
	}


	/**
	 * Makes 14 digit QhpId or PlanId.
	 * @param issuerId
	 * @param tenantId
	 * @return
	 */
	public static String makeQhpId (String issuerId, String tenantId) {

		return issuerId + tenantId + getRandomNumberAsString(6);
	}

	public static String getEnrollmentAsXmlString(Enrollment enrollment) {

		String xml = "";
		try {
			JAXBContext context = JAXBContext.newInstance(Enrollment.class);
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
			StringWriter stringWriter = new StringWriter();
			marshaller.marshal(enrollment, stringWriter);
			xml = stringWriter.toString();

		} catch (Exception ex) {
			System.out.println("Ex at getEnrollmentAsXmlString: "+ ex.getMessage());
		}
		return xml;
	}

	public static String getFileInfoTypeAsXmlString(FileInformationType fileInfoType) {

		String xml = "";
		try {
			JAXBElement<FileInformationType> fileInfoJaxB = 
					new JAXBElement<FileInformationType>(new QName("http://sbmi.dsh.cms.gov","FileInformationType"), 
							FileInformationType.class, fileInfoType);
			JAXBContext context = JAXBContext.newInstance(String.class, FileInformationType.class);
			Marshaller marshaller = context.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			marshaller.marshal(fileInfoJaxB, stringWriter);
			xml = stringWriter.toString();
		} catch (Exception ex) {
			System.out.println("Ex at getFileInfoTypeAsXmlString: "+ ex.getMessage());
		}
		return xml;
	}

	public static String getPolicyAsXmlString(PolicyType policy) {

		String xml = null;	
		if(policy != null) {
			try {
				JAXBElement<PolicyType> policyJaxB = 
						new JAXBElement<PolicyType>(new QName("http://sbmi.dsh.cms.gov","Policy"), PolicyType.class, policy);
				JAXBContext jaxbContext = JAXBContext.newInstance(String.class, PolicyType.class);
				Marshaller marshaller = jaxbContext.createMarshaller();
				StringWriter stringWriter = new StringWriter();
				marshaller.marshal(policyJaxB, stringWriter);
				xml = stringWriter.toString();
			} catch (Exception ex) {
				System.out.println("Ex at getPolicyAsXmlString: "+ ex.getMessage());
			}
		}
		return xml;
	}


	public static int getPolicyIndexFromExchangePolicyId(String exchangePolicyId) {

		String idxStr = exchangePolicyId.substring(exchangePolicyId.length() - 1, exchangePolicyId.length());
		int idx = Integer.parseInt(idxStr);
		return idx;
	}
	
	public static String prettyXMLFormat(String input, int indent) {
		try {
			Source xmlInput = new StreamSource(new StringReader(input));
			StringWriter stringWriter = new StringWriter();
			StreamResult xmlOutput = new StreamResult(stringWriter);
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			transformerFactory.setAttribute("indent-number", indent);
			Transformer transformer = transformerFactory.newTransformer(); 
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(xmlInput, xmlOutput);
			return xmlOutput.getWriter().toString();
		} catch (Exception e) {
			throw new RuntimeException(e); // simple exception handling, please review it
		}
	}

	public static String prettyXMLFormat(String input) {
		return prettyXMLFormat(input, 2);
	}

	public static int getRandomNumber(int digits) {
		double dblDigits = (double) digits;
		double min = Math.pow(10.0, dblDigits - 1);
		double max = Math.pow(10.0, dblDigits) - 1;
		int randNum = (int) Math.round(Math.random() * (max - min) + min);
		return randNum;
	}

	public static Long getRandomNumberAsLong(int digits) {
		int intNum = getRandomNumber(digits);
		return Long.valueOf(intNum);
	}

	public static String getRandomNumberAsString(int digits) {
		int intNum = getRandomNumber(digits);
		return String.valueOf(intNum);
	}

	public static String getRandomSbmState() {
		int randNum = (int) Math.round(Math.random() * (SBM_STATES.length - 1));
		return SBM_STATES[randNum];
	}


}
