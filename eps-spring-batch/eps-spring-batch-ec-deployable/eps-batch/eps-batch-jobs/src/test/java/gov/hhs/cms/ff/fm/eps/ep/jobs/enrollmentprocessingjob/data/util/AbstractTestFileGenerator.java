package gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.data.util;

import gov.cms.dsh.bem.AdditionalInfoType;
import gov.cms.dsh.bem.BenefitEnrollmentMaintenanceType;
import gov.cms.dsh.bem.BenefitEnrollmentRequest;
import gov.cms.dsh.bem.BooleanIndicatorSimpleType;
import gov.cms.dsh.bem.ExchangeCodeSimpleType;
import gov.cms.dsh.bem.FileInformationType;
import gov.cms.dsh.bem.GenderCodeSimpleType;
import gov.cms.dsh.bem.HealthCoverageDatesType;
import gov.cms.dsh.bem.HealthCoverageInfoType;
import gov.cms.dsh.bem.HealthCoveragePolicyNumberType;
import gov.cms.dsh.bem.HealthCoverageType;
import gov.cms.dsh.bem.IndividualNameType;
import gov.cms.dsh.bem.InsuranceLineCodeSimpleType;
import gov.cms.dsh.bem.IssuerType;
import gov.cms.dsh.bem.MemberAdditionalIdentifierType;
import gov.cms.dsh.bem.MemberDemographicsType;
import gov.cms.dsh.bem.MemberNameInfoType;
import gov.cms.dsh.bem.MemberRelatedDatesType;
import gov.cms.dsh.bem.MemberRelatedInfoType;
import gov.cms.dsh.bem.MemberType;
import gov.cms.dsh.bem.PolicyInfoType;
import gov.cms.dsh.bem.ResidentialAddressType;
import gov.cms.dsh.bem.TransactionInformationType;
import gov.hhs.cms.ff.fm.eps.ep.enums.ExchangeType;
import gov.hhs.cms.ff.fm.eps.ep.enums.PolicyStatus;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

public class AbstractTestFileGenerator  {

	protected static final String APTC ="APTCAmount";			
	protected static final String CSR ="CSRAmount";					
	protected static final String TPA ="TotalPremiumAmount";			
	protected static final String TIRA ="TotalIndividualResponsibilityAmount";					
	protected static final String RA ="RatingArea";									
	protected static final String PRO_APTC ="ProratedAppliedAPTCAmount";
	protected static final String PRO_CSR ="ProratedCSRAmount";
	protected static final String PRO_MPA ="ProratedMonthlyPremiumAmount";
	protected static final String PRO_TIRA ="ProratedIndividualResponsibleAmount";	
	protected static final String ESD ="EffectiveStartDate";			
	protected static final String EED ="EffectiveEndDate";

	//For Files sent by Partners, the format will be THHMMSSt
	protected static final SimpleDateFormat sdf = new SimpleDateFormat("'D'yyMMdd'.T'HHmmssSSS");
	
	protected static final DateTimeFormatter DTF_MANIFEST_MICRO_SEC = new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd'T'HH:mm:ss")
			.appendFraction(ChronoField.MICRO_OF_SECOND, 0, 6, true).appendPattern("XXX").toFormatter();
	
	// Interval to make unique file names based on time.
	protected static final int SLEEP_INTERVAL = 1015;

	protected static final LocalDate DATE = LocalDate.now();
	protected static final LocalDateTime DATETIME = LocalDateTime.now();
	protected static final int YEAR = DATE.getYear() + 4;


	protected static final LocalDate JAN_1 = LocalDate.of(YEAR, 1, 1);
	protected static final LocalDate JAN_15 = LocalDate.of(YEAR, 1, 15);
	protected static final LocalDate JAN_31 = LocalDate.of(YEAR, 1, 31);
	protected static final LocalDate FEB_1 = LocalDate.of(YEAR, 2, 1);
	protected static final LocalDate FEB_2 = LocalDate.of(YEAR, 2, 2);
	protected static final LocalDate FEB_15 = LocalDate.of(YEAR, 2, 15);
	protected static final LocalDate MAR_14 = LocalDate.of(YEAR, 3, 14);
	protected static final LocalDate MAR_15 = LocalDate.of(YEAR, 3, 15);
	protected static final LocalDate JUN_29 = LocalDate.of(YEAR, 6, 29);
	protected static final LocalDate JUN_30 = LocalDate.of(YEAR, 6, 30);
	protected static final LocalDate JUL_3_1965 = LocalDate.of(1965, 7, 3);
	protected static final LocalDate JUL_4_1965 = LocalDate.of(1965, 7, 4);
	protected static final LocalDate SEP_11_2001 = LocalDate.of(2001, 9, 11);

	protected static final LocalDateTime JUN_1_1am = LocalDateTime.of(YEAR, 6, 1, 1, 0, 0, 666666000);

	private static LocalDate birthDate = JUL_4_1965;
	private static LocalDate eligibilityBegin = FEB_1;
	private static LocalDate eligibilityEnd = JUN_30;
	private static LocalDate effectiveStart = FEB_15;
	private static LocalDate benefitBeginDate = FEB_2;
	private static LocalDate benefitEndDate = JUN_29;
	private static LocalDate lastPremiumPaidDate  = JAN_15;
	private static LocalDate premiumPaidToDateEnd = JAN_31;


	protected static BenefitEnrollmentMaintenanceType makeBenefitEnrollmentMaintenanceType(Long bemId, String versionNum, LocalDateTime versionDT, String hiosId, String gpn, PolicyStatus policyStatus) {

		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();

		bem.setTransactionInformation(makeTransactionInformationType(bemId.toString(), versionNum, versionDT));
		bem.setIssuer(makeIssuerType(bemId.toString()));
		bem.setPolicyInfo(makePolicyInfoType(gpn, policyStatus));
		return bem;
	}


	private static TransactionInformationType makeTransactionInformationType(String bemId, String versionNum, LocalDateTime versionDT) {

		TransactionInformationType transInfoType = new TransactionInformationType();
		// <xsd:minLength value="4" /><xsd:maxLength value="9" /> 
		String controlNum = bemId.toString() + bemId.toString() + bemId.toString();
		controlNum = controlNum.length() > 9 ? controlNum.substring(0, 9) : controlNum;
		transInfoType.setControlNumber(controlNum);
		//Calendar to String to XMLGregorianCalendar for testing
		//Real data will be String to XMLGregorianCalendar
		transInfoType.setCurrentTimeStamp(DateTimeUtil.getXMLGregorianCalendar(versionDT));
		transInfoType.setExchangeCode(ExchangeCodeSimpleType.INDIVIDUAL);
		transInfoType.setPolicySnapshotDateTime(DateTimeUtil.getXMLGregorianCalendar(versionDT));
		transInfoType.setPolicySnapshotVersionNumber(versionNum);
		return transInfoType;
	}

	private static IssuerType makeIssuerType(String hiosId) {

		IssuerType issType = new IssuerType();
		issType.setName("ISSUERNAME" + hiosId);
		issType.setHIOSID(hiosId);
		return issType;
	}

	private static PolicyInfoType makePolicyInfoType(String gpn, PolicyStatus policyStatus) {

		PolicyInfoType policyInfoType = new PolicyInfoType();
		policyInfoType.setGroupPolicyNumber(gpn);
		policyInfoType.setMarketplaceGroupPolicyIdentifier("MPGPID");
		policyInfoType.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(benefitBeginDate));
		policyInfoType.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(benefitEndDate));
		policyInfoType.setPolicyStatus(policyStatus.getValue());

		return policyInfoType;
	}

	protected MemberType makeMemberType(Long memId, String name, boolean isSubscriber, String subscriberId, 
			String hcGPN, String groupSenderId, BigDecimal tpa) {

		MemberType memType = new MemberType();
		memType.setSubscriberID(subscriberId);

		String stateCd = groupSenderId.substring(5, 7);

		memType.setMemberInformation(makeMemberRelatedInfoType(memId, isSubscriber));
		memType.setMemberAdditionalIdentifier(makeMemberAdditionalIdentifierType(memId));
		memType.setMemberRelatedDates(makeMemberRelatedDatesType(isSubscriber));
		memType.setMemberNameInformation(makeMemberNameInfoType(memId, name, stateCd));
		memType.getHealthCoverage().add(makeHealthCoverageType(memId, groupSenderId, hcGPN));

		memType.getAdditionalInfo().addAll(makeAdditionalInfoTypeList(stateCd, tpa));

		return memType;
	}


	private MemberRelatedInfoType makeMemberRelatedInfoType(Long memId, boolean isSubscriber) {

		MemberRelatedInfoType memRelInfoType = new MemberRelatedInfoType();

		if(isSubscriber) {
			memRelInfoType.setSubscriberIndicator(BooleanIndicatorSimpleType.Y);
		} else {
			memRelInfoType.setSubscriberIndicator(BooleanIndicatorSimpleType.N);
		}

		return memRelInfoType;
	}


	private MemberAdditionalIdentifierType makeMemberAdditionalIdentifierType(Long memId) {

		MemberAdditionalIdentifierType memAddIdType = new MemberAdditionalIdentifierType();

		memAddIdType.setExchangeAssignedMemberID("EXC-" + String.valueOf(memId));
		memAddIdType.setIssuerAssignedMemberID("ISS-" + String.valueOf(memId));
		memAddIdType.setIssuerAssignedSubscriberID("SUB-" + String.valueOf(memId));

		return memAddIdType;
	}

	private MemberRelatedDatesType makeMemberRelatedDatesType(boolean isSubscriber) {

		MemberRelatedDatesType memRelDatesType = new MemberRelatedDatesType();

		memRelDatesType.setEligibilityBeginDate(DateTimeUtil.getXMLGregorianCalendar(eligibilityBegin));
		memRelDatesType.setEligibilityEndDate(DateTimeUtil.getXMLGregorianCalendar(eligibilityEnd));
		return memRelDatesType;

	}


	private ResidentialAddressType makeResidentialAddressType(Long memId, String name, String stateCd) {

		ResidentialAddressType resAddrType = new ResidentialAddressType();

		String strAddrNum = name;

		resAddrType.setStateCode(stateCd);
		String zipCode = strAddrNum.length() > 10 ? strAddrNum.substring(0, 10) : strAddrNum;
		resAddrType.setPostalCode("00" + zipCode);
		String chr3 = memId.toString() + memId.toString();
		chr3 = chr3.substring(0, 3);

		return resAddrType;
	}


	private List<AdditionalInfoType> makeAdditionalInfoTypeList(String stateCd, BigDecimal tpa)  {

		List<AdditionalInfoType> aitList = new ArrayList<AdditionalInfoType>();

		LocalDate esd = effectiveStart;
		LocalDate eed = eligibilityEnd;

		BigDecimal aptc = tpa.multiply(new BigDecimal(".1"));
		BigDecimal csr = new BigDecimal("0");

		BigDecimal tira = tpa.subtract(aptc).setScale(2, RoundingMode.HALF_UP);

		String ra = stateCd +"001";

		BigDecimal proAptc = aptc.divide(new BigDecimal(".8")).setScale(2, RoundingMode.HALF_UP);
		BigDecimal proCsr = csr;
		BigDecimal proMpa = tpa.divide(new BigDecimal(".8")).setScale(2, RoundingMode.HALF_UP);
		BigDecimal proTira = tira.divide(new BigDecimal(".8")).setScale(2, RoundingMode.HALF_UP);

		AdditionalInfoType ait = new AdditionalInfoType();
		ait.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		ait.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));

		setAdditionalInfoTypeValues(ait, TPA, esd, eed, tpa);
		setAdditionalInfoTypeValues(ait, TIRA, esd, eed, tira);
		setAdditionalInfoTypeValue(ait, RA, esd, eed, ra);
		setAdditionalInfoTypeValues(ait, APTC, esd, eed, aptc);
		setAdditionalInfoTypeValues(ait, CSR, esd, eed, csr);
		setAdditionalInfoTypeValues(ait, PRO_MPA, esd, eed, proMpa);
		setAdditionalInfoTypeValues(ait, PRO_TIRA, esd, eed, proTira);
		setAdditionalInfoTypeValues(ait, PRO_APTC, esd, eed, proAptc);
		setAdditionalInfoTypeValues(ait, PRO_CSR, esd, eed, proCsr);

		aitList.add(ait);

		return aitList;
	}


	private static AdditionalInfoType setAdditionalInfoTypeValues(AdditionalInfoType ait, String type, LocalDate esd, LocalDate eed, BigDecimal amt) {

		if (APTC.equals(type)) {
			ait.setAPTCAmount(amt); 
		} else if (TIRA.equals(type)) {
			ait.setTotalIndividualResponsibilityAmount(amt);
		} else if (TPA.equals(type)) {
			ait.setTotalPremiumAmount(amt);
		} else if (CSR.equals(type)) {
			ait.setCSRAmount(amt);
		} else if (PRO_APTC.equals(type)) {
			ait.setProratedAppliedAPTCAmount(amt);
		} else if (PRO_CSR.equals(type)) {
			ait.setProratedCSRAmount(amt);
		} else if (PRO_MPA.equals(type)) {
			ait.setProratedMonthlyPremiumAmount(amt);
		} else if (PRO_TIRA.equals(type)) {
			ait.setProratedIndividualResponsibleAmount(amt);
		}
		return ait;
	}

	private static AdditionalInfoType setAdditionalInfoTypeValue(AdditionalInfoType ait, String type, LocalDate esd, LocalDate eed, String txt) {

		if (RA.equals(type)) {
			ait.setRatingArea(txt); 
		} 
		return ait;
	}


	private MemberNameInfoType makeMemberNameInfoType(Long memId, String name, String stateCd) {

		MemberNameInfoType memNameInfoType = new MemberNameInfoType();
		memNameInfoType.setMemberName(makeIndividualNameType(memId, name));
		memNameInfoType.setMemberDemographics(makeMemberDemographicsType());
		memNameInfoType.setMemberResidenceAddress(makeResidentialAddressType(memId, name, stateCd));  
		return memNameInfoType;
	}

	private IndividualNameType makeIndividualNameType(Long memId, String name) {

		IndividualNameType indNameType = new IndividualNameType();

		String lastNm = name + "-" + memId + " LAST";
		lastNm = lastNm.length() > 60 ? lastNm.substring(0, 60): lastNm;
		indNameType.setLastName(lastNm);

		String firstNm = name + "-" + memId + " FIRST";
		firstNm = firstNm.length() > 35 ? firstNm.substring(0, 35): firstNm;
		indNameType.setFirstName(firstNm);

		String midNm = name + "-" + memId + " MID";
		midNm = midNm.length() > 25 ? midNm.substring(0, 25): midNm;
		indNameType.setMiddleName(midNm);

		String prefix = name + "-" + memId + " PRE";
		prefix = prefix.length() > 10 ? prefix.substring(0, 10): prefix;
		indNameType.setNamePrefix(prefix);

		String suffix = name + "-" + memId + " SUF";
		suffix = suffix.length() > 10 ? suffix.substring(0, 10) : suffix;
		indNameType.setNameSuffix(suffix);

		String ssn9 = (memId.toString() + memId.toString() + memId.toString());
		ssn9 = ssn9.length() > 9 ? ssn9.substring(0, 9) : ssn9;
		indNameType.setSocialSecurityNumber(ssn9);

		return indNameType;
	}

	private MemberDemographicsType makeMemberDemographicsType() {

		MemberDemographicsType memDemoType = new MemberDemographicsType();

		memDemoType.setGenderCode(GenderCodeSimpleType.M);
		memDemoType.setBirthDate(DateTimeUtil.getXMLGregorianCalendar(birthDate));

		return memDemoType;

	}

	private HealthCoverageType makeHealthCoverageType(Long memId, String groupSenderId, String hcGPN) {

		HealthCoverageType healthCovType = new HealthCoverageType();

		healthCovType.setHealthCoverageInformation(makeHealthCoverageInfoType());		
		healthCovType.setHealthCoveragePolicyNumber(makeHealthCoveragePolicyNumberType(memId, groupSenderId));
		healthCovType.setHealthCoverageDates(makeHealthCoverageDatesType());

		return healthCovType;
	}

	private HealthCoverageInfoType makeHealthCoverageInfoType() {

		HealthCoverageInfoType healthCovInfoType = new HealthCoverageInfoType();
		healthCovInfoType.setInsuranceLineCode(InsuranceLineCodeSimpleType.HLT);
		return healthCovInfoType;
	}

	private HealthCoveragePolicyNumberType makeHealthCoveragePolicyNumberType(Long memId, String groupSenderId) {

		HealthCoveragePolicyNumberType hcPolNumType = new HealthCoveragePolicyNumberType();
		hcPolNumType.setInternalControlNumber(memId.toString());
		hcPolNumType.setContractCode(groupSenderId + "01"); // <-- last 2 chars are insurancePlanVariantCd (00,01,02,03,04,05,06)		
		return hcPolNumType;
	}

	private HealthCoverageDatesType makeHealthCoverageDatesType() {

		HealthCoverageDatesType healthCovDatesType = new HealthCoverageDatesType();

		healthCovDatesType.setBenefitBeginDate(DateTimeUtil.getXMLGregorianCalendar(benefitBeginDate));
		healthCovDatesType.setBenefitEndDate(DateTimeUtil.getXMLGregorianCalendar(benefitEndDate));
		healthCovDatesType.setLastPremiumPaidDate(DateTimeUtil.getXMLGregorianCalendar(lastPremiumPaidDate));
		healthCovDatesType.setPremiumPaidToDateEnd(DateTimeUtil.getXMLGregorianCalendar(premiumPaidToDateEnd));

		return healthCovDatesType;
	}

	protected FileInformationType makeFileInformationType(Long id, ExchangeType exchangeType, String groupSenderId) {

		FileInformationType fileInfoType = new FileInformationType();

		String strId = String.format("%05d", id);
		//must contain 9 characters
		String iccNum = id + "ICCNUM";
		iccNum = iccNum.length() > 9 ? iccNum.substring(0,9) : iccNum;

		fileInfoType.setGroupReceiverID(groupSenderId.substring(5, 7));  
		fileInfoType.setGroupSenderID(groupSenderId);

		fileInfoType.setGroupControlNumber(strId);
		//Calendar to String to XMLGregorianCalendar for testing
		//Real data will be String to XMLGregorianCalendar
		fileInfoType.setGroupTimeStamp(DateTimeUtil.getXMLGregorianCalendar(LocalDateTime.now()));
		String versionNum = "23"; //See bem.xsd 
		fileInfoType.setVersionNumber(versionNum);

		return fileInfoType;

	}



	protected int randInt(int min, int max) {

		Random rand = new Random();
		int randomNum = rand.nextInt((max - min) + 1) + min;
		return randomNum;
	}

	protected Long getRandom3DigitNumber() {

		return getRandomNumber(3);
	}

	protected Long getRandomNumber(int digits) {

		double dblDigits = (double) digits;
		double min = Math.pow(10.0, dblDigits - 1);
		double max = Math.pow(10.0, dblDigits) - 1;
		int randNum = (int) Math.round(Math.random() * (max - min) + min);
		return Long.valueOf(randNum);
	}

	protected String getBerXMLAsString(BenefitEnrollmentRequest ber) {

		String xml = "";
		try {
			JAXBContext context = JAXBContext.newInstance(BenefitEnrollmentRequest.class);
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			StringWriter stringWriter = new StringWriter();
			//javax.xml.stream.XMLStreamWriter xmlSw = new javax.xml.stream.XMLStreamWriter();

			marshaller.marshal(ber, stringWriter);
			xml = stringWriter.toString();

		} catch (Exception ex) {
			System.out.println("Ex at outputBerXML: "+ ex.getMessage());
		}
		return xml;
	}

}
