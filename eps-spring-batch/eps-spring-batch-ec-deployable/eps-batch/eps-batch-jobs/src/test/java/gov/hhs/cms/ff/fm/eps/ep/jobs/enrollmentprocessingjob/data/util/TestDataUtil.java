package gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.data.util;

import gov.cms.dsh.bem.AdditionalInfoType;
import gov.cms.dsh.bem.AddressType;
import gov.cms.dsh.bem.BenefitEnrollmentMaintenanceType;
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
import gov.cms.dsh.bem.ResidentialAddressType;
import gov.cms.dsh.bem.TransactionInformationType;
import gov.hhs.cms.ff.fm.eps.ep.enums.ExchangeType;
import gov.hhs.cms.ff.fm.eps.ep.enums.PolicyStatus;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.joda.time.DateTime;

/*
 * Generates all BEM composite objects with random data.
 * 
 * 
 * NOTE: After make any updates to this class, run ALL Batch units tests, since
 *       some of the those test rely on data generated from this class.
 */


public class TestDataUtil {

	private static final DateTime DATETIME = new DateTime();
	private static final int YEAR = DATETIME.getYear();

	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd'T'HH:mm:ss");

	private static final int ADDL_INFO_TYPE_LEN = 16;

	protected static final LocalDate JAN_15 = LocalDate.of(YEAR, 1, 15);
	protected static final LocalDate JAN_31 = LocalDate.of(YEAR, 1, 31);
	protected static final LocalDate FEB_1 = LocalDate.of(YEAR, 2, 1);
	protected static final LocalDate FEB_2 = LocalDate.of(YEAR, 2, 2);
	protected static final LocalDate FEB_15 = LocalDate.of(YEAR, 2, 15);
	protected static final LocalDate JUN_29 = LocalDate.of(YEAR, 6, 29);
	protected static final LocalDate JUN_30 = LocalDate.of(YEAR, 6, 30);
	protected static final LocalDate JUL_4_1965 = LocalDate.of(1965, 7, 4);

	private static LocalDate birthDate = JUL_4_1965;
	private static LocalDate eligibilityBegin = FEB_1;
	private static LocalDate eligibilityEnd = JUN_30;
	private static LocalDate effectiveStart = FEB_15;
	private static LocalDate benefitBeginDate = FEB_2;
	private static LocalDate benefitEndDate = JUN_29;
	private static LocalDate lastPremiumPaidDate  = JAN_15;
	private static LocalDate premiumPaidToDateEnd = JAN_31;


	private static Marshaller marshaller;


	static {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(BenefitEnrollmentMaintenanceType.class);
			marshaller = jaxbContext.createMarshaller();
		} catch (Exception ex) {
			;
		}
	}


	public static MemberType makeSubscriber(String stateCd, String exchangePolicyId, String hiosId) {

		String contractCode = hiosId.concat(stateCd).concat(exchangePolicyId);

		MemberType member = new MemberType();
		member.setMemberInformation(new MemberRelatedInfoType());
		member.getMemberInformation().setSubscriberIndicator(BooleanIndicatorSimpleType.Y);

		member.setMemberAdditionalIdentifier(new MemberAdditionalIdentifierType());
		member.getMemberAdditionalIdentifier().setExchangeAssignedMemberID(hiosId + "-MEMBER");

		HealthCoverageType hcType = new HealthCoverageType();
		hcType.setHealthCoveragePolicyNumber(new HealthCoveragePolicyNumberType());
		hcType.getHealthCoveragePolicyNumber().setContractCode(contractCode);

		hcType.setHealthCoverageInformation(new HealthCoverageInfoType());
		hcType.getHealthCoverageInformation().setInsuranceLineCode(InsuranceLineCodeSimpleType.HLT);

		member.getHealthCoverage().add(hcType);
		return member;
	}

	public static MemberType makeSubscriber(String stateCd, String exchangePolicyId, String hiosId, LocalDate effStart, PolicyStatus policyStatus) {

		String contractCode = hiosId.concat(stateCd).concat(exchangePolicyId);

		MemberType member = new MemberType();
		member.setMemberInformation(new MemberRelatedInfoType());
		member.getMemberInformation().setSubscriberIndicator(BooleanIndicatorSimpleType.Y);
		member.setMemberNameInformation(makeMemberNameInfoType(1L, hiosId+"Member"));

		member.setMemberAdditionalIdentifier(new MemberAdditionalIdentifierType());
		member.getMemberAdditionalIdentifier().setExchangeAssignedMemberID(hiosId + "-MEMBER");

		member.setMemberRelatedDates(new MemberRelatedDatesType());
		member.getMemberRelatedDates().setEligibilityBeginDate(DateTimeUtil.getXMLGregorianCalendar(effStart));

		HealthCoverageType hcType = new HealthCoverageType();
		hcType.setHealthCoveragePolicyNumber(new HealthCoveragePolicyNumberType());
		hcType.getHealthCoveragePolicyNumber().setContractCode(contractCode);
		hcType.setHealthCoverageInformation(new HealthCoverageInfoType());
		hcType.getHealthCoverageInformation().setInsuranceLineCode(InsuranceLineCodeSimpleType.HLT);
		hcType.setHealthCoverageDates(new HealthCoverageDatesType());
		hcType.getHealthCoverageDates().setBenefitBeginDate(DateTimeUtil.getXMLGregorianCalendar(effStart));
		member.getHealthCoverage().add(hcType);
		return member;
	}

	public static BenefitEnrollmentMaintenanceType makeBenefitEnrollmentMaintenanceType(Long bemId, boolean isXsdValid) {

		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();

		bem.setTransactionInformation(makeTransactionInformationType(bemId.toString()));
		bem.setIssuer(makeIssuerType(bemId.toString(), isXsdValid));

		return bem;
	}



	public static TransactionInformationType makeTransactionInformationType(String bemId) {

		TransactionInformationType transInfoType = new TransactionInformationType();
		// <xsd:minLength value="4" /><xsd:maxLength value="9" /> 
		String controlNum = bemId.toString() + bemId.toString() + bemId.toString();
		controlNum = controlNum.length() > 9 ? controlNum.substring(0, 9) : controlNum;
		transInfoType.setControlNumber(controlNum);
		//Calendar to String to XMLGregorianCalendar for testing
		//Real data will be String to XMLGregorianCalendar
		transInfoType.setCurrentTimeStamp(DateTimeUtil.getXMLGregorianCalendar(sdf.format(new DateTime())));
		transInfoType.setExchangeCode(ExchangeCodeSimpleType.INDIVIDUAL);

		return transInfoType;
	}

	public static IssuerType makeIssuerType(String strNum, boolean isXsdValid) {

		IssuerType issType = new IssuerType();

		issType.setName("ISSUERNAME" + strNum);
		// Length = 10
		String hiosId = strNum + strNum;
		hiosId = hiosId.length() > 10 ?  hiosId.substring(0, 10) : hiosId;
		issType.setHIOSID(hiosId);

		if(!isXsdValid) {

			issType.setCMSPlanID(strNum);
			issType.setTaxPayerIdentificationNumber("TAXPAYID-" + strNum);
		}

		return issType;
	}


	public static MemberType makeMemberType(Long memId, String name, boolean isSubscriber, String subscriberId) {

		MemberType memType = makeMemberType(memId, name, isSubscriber);
		memType.setSubscriberID(subscriberId);
		return memType;
	}

	public static MemberType makeMemberType(Long memId, String name, boolean isSubscriber) {

		MemberType memType = new MemberType();

		memType.setMemberInformation(makeMemberRelatedInfoType(memId, isSubscriber));

		memType.setSubscriberID(name + "-" + memId);

		// Set same as GroupPolicyNumber, for PolicyMatch3 unit tests
		memType.setMemberAdditionalIdentifier(makeMemberAdditionalIdentifierType(memId));

		memType.setMemberRelatedDates(makeMemberRelatedDatesType(isSubscriber));

		memType.setMemberNameInformation(makeMemberNameInfoType(memId, name));

		// TODO: not passing schema validation
		memType.getHealthCoverage().add(makeHealthCoverageType(memId));

		memType.getAdditionalInfo().addAll(makeAdditionalInfoTypeListSize1(memId, 100));

		return memType;
	}


	public static MemberRelatedInfoType makeMemberRelatedInfoType(Long memId, boolean isSubscriber) {

		MemberRelatedInfoType memRelInfoType = new MemberRelatedInfoType();

		if(isSubscriber) {
			memRelInfoType.setSubscriberIndicator(BooleanIndicatorSimpleType.Y);
		} else {
			memRelInfoType.setSubscriberIndicator(BooleanIndicatorSimpleType.N);
		}

		return memRelInfoType;
	}


	public static MemberAdditionalIdentifierType makeMemberAdditionalIdentifierType(Long memId) {

		MemberAdditionalIdentifierType memAddIdType = new MemberAdditionalIdentifierType();

		memAddIdType.setExchangeAssignedMemberID("EXC-" + String.valueOf(memId));
		memAddIdType.setIssuerAssignedMemberID("ISS-" + String.valueOf(memId));
		memAddIdType.setIssuerAssignedSubscriberID("SUB-" + String.valueOf(memId));

		return memAddIdType;
	}

	public static MemberRelatedDatesType makeMemberRelatedDatesType(boolean isSubscriber) {

		MemberRelatedDatesType memRelDatesType = new MemberRelatedDatesType();

		memRelDatesType.setEligibilityBeginDate(DateTimeUtil.getXMLGregorianCalendar(eligibilityBegin));
		memRelDatesType.setEligibilityEndDate(DateTimeUtil.getXMLGregorianCalendar(eligibilityEnd));
		return memRelDatesType;

	}

	public static MemberNameInfoType makeMemberNameInfoType(Long memId, String name) {

		MemberNameInfoType memNameInfoType = new MemberNameInfoType();
		memNameInfoType.setMemberName(makeIndividualNameType(memId, name));
		memNameInfoType.setMemberDemographics(makeMemberDemographicsType());
		memNameInfoType.setMemberResidenceAddress(makeResidentialAddressType(memId, name));  
		return memNameInfoType;
	}

	public static ResidentialAddressType makeResidentialAddressType(Long memId, String name) {

		ResidentialAddressType resAddrType = new ResidentialAddressType();

		String strAddrNum = memId.toString();

		resAddrType.setStateCode(getStateAbbrevRandom(memId));
		String zipCode = strAddrNum.length() > 10 ? strAddrNum.substring(0, 10) : strAddrNum;
		resAddrType.setPostalCode("00" + zipCode);

		return resAddrType;
	}


	public static IndividualNameType makeIndividualNameType(Long memId, String name) {

		IndividualNameType indNameType = new IndividualNameType();
		/*
		 * POLICYMEMBERLASTNM	VARCHAR2(60 BYTE)
		   POLICYMEMBERFIRSTNM	VARCHAR2(35 BYTE)
           POLICYMEMBERMIDDLENM	VARCHAR2(25 BYTE)
           POLICYMEMBERSALUTATIONNM	VARCHAR2(10 BYTE)
           POLICYMEMBERSUFFIXNM	VARCHAR2(10 BYTE)
		 */

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

	public static MemberDemographicsType makeMemberDemographicsType() {

		MemberDemographicsType memDemoType = new MemberDemographicsType();

		memDemoType.setGenderCode(GenderCodeSimpleType.M);
		memDemoType.setBirthDate(DateTimeUtil.getXMLGregorianCalendar(birthDate));

		return memDemoType;

	}

	public static AddressType makeAddressType(Long memId, String name) {

		AddressType addrType = new AddressType();

		String strAddrNum = memId.toString();

		addrType.setStateCode(getStateAbbrevRandom(memId));
		String zipCode = strAddrNum.length() > 10 ? strAddrNum.substring(0, 10) : strAddrNum;
		addrType.setPostalCode("00" + zipCode);

		return addrType;
	}

	public static HealthCoverageType makeHealthCoverageType(Long memId) {

		HealthCoverageType healthCovType = new HealthCoverageType();

		healthCovType.setHealthCoverageInformation(makeHealthCoverageInfoType());		
		healthCovType.setHealthCoveragePolicyNumber(makeHealthCoveragePolicyNumberType(memId));
		healthCovType.setHealthCoverageDates(makeHealthCoverageDatesType());

		return healthCovType;
	}

	public static HealthCoverageInfoType makeHealthCoverageInfoType() {

		HealthCoverageInfoType healthCovInfoType = new HealthCoverageInfoType();

		//TODO: bem says different than values
		healthCovInfoType.setInsuranceLineCode(getInsuranceLineCodeSimpleTypeRandom());

		return healthCovInfoType;
	}

	public static HealthCoveragePolicyNumberType makeHealthCoveragePolicyNumberType(Long memId) {

		HealthCoveragePolicyNumberType hcPolNumType = new HealthCoveragePolicyNumberType();

		hcPolNumType.setInternalControlNumber(memId.toString());
		//issuerHiosID == First 5 chars from Contract Code
		String strMemId = memId.toString();
		strMemId = strMemId.length() > 3 ? strMemId.substring(0, 3) : strMemId;
		// first 14 will become planId which will be matched to GroupSender or GroupReceiver Id for 
		// policy matching
		// Insure we have at least 14 characters.
		String str14 = memId.toString() + memId.toString() + memId.toString() + memId.toString() + memId.toString();
		str14 = str14.length() > 14 ? str14.substring(0, 14) : str14;
		hcPolNumType.setContractCode(str14 + "06"); // <-- last 2 chars are insurancePlanVariantCd (00,01,02,03,04,05,06)

		return hcPolNumType;
	}

	public static HealthCoverageDatesType makeHealthCoverageDatesType() {

		HealthCoverageDatesType healthCovDatesType = new HealthCoverageDatesType();

		// Set dates to string format first as they would come in from xml.
		healthCovDatesType.setBenefitBeginDate(DateTimeUtil.getXMLGregorianCalendar(benefitBeginDate));
		healthCovDatesType.setBenefitEndDate(DateTimeUtil.getXMLGregorianCalendar(benefitEndDate));
		healthCovDatesType.setLastPremiumPaidDate(DateTimeUtil.getXMLGregorianCalendar(lastPremiumPaidDate));
		healthCovDatesType.setPremiumPaidToDateEnd(DateTimeUtil.getXMLGregorianCalendar(premiumPaidToDateEnd));
		return healthCovDatesType;
	}

	public static FileInformationType makeFileInformationType(Long id, ExchangeType exchangeType) {

		FileInformationType fileInfoType = new FileInformationType();

		// 14 chars
		String str14 = id.toString();
		str14 = str14.length() < 14 ? str14 + str14 + str14 + str14 : str14;
		str14 = str14.length() > 14 ? str14.substring(0, 14) : str14;

		if(exchangeType.equals(ExchangeType.FFM)) {

			fileInfoType.setGroupReceiverID(str14);
			fileInfoType.setGroupSenderID("GSID" + id.toString());

		} else {

			fileInfoType.setGroupReceiverID("GRID" + id.toString());
			fileInfoType.setGroupSenderID(str14);
		}

		fileInfoType.setGroupControlNumber(id.toString());
		//Calendar to String to XMLGregorianCalendar for testing
		//Real data will be String to XMLGregorianCalendar
		fileInfoType.setGroupTimeStamp(DateTimeUtil.getXMLGregorianCalendar(getLocalDateTimeWithMicros()));
		String versionNum = "23"; //See bem.xsd 
		fileInfoType.setVersionNumber(versionNum);

		return fileInfoType;

	}

	public static String makeFileInformationTypeAsStringXML(Long id) {

		String strXML = "<FileInformation>" +
				"<GroupSenderID>" + id + " GRPSNDRID</GroupSenderID>" +
				"<GroupReceiverID>" + id + " GRPRCVRID</GroupReceiverID>" +
				"<GroupControlNumber>" + id + "</GroupControlNumber>" +
				"<GroupTimeStamp>" + sdf.format(new DateTime().toDate()) +"</GroupTimeStamp>" +
				"<VersionNumber>23</VersionNumber>" +
				"</FileInformation>";

		return strXML;

	}

	/*
	 * Make a list of AdditionalInfoType with 1 effective start date (1 time slice).
	 */

	public static List<AdditionalInfoType> makeAdditionalInfoTypeListSize1(Long memId, int baseAmt)  {


		List<AdditionalInfoType> addInfoTypeList = new ArrayList<AdditionalInfoType>();

		AdditionalInfoType addlInfoType = new AdditionalInfoType();
		addlInfoType.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(effectiveStart));

		for(int i = 0; i < ADDL_INFO_TYPE_LEN; ++i) {
			// Do not make one for ApplicationIDAndOrigin, since not being mapped
			if (i != 14) {
				addlInfoType = setAdditionalInfoType(addlInfoType, memId, i, baseAmt + i);
			}
		}

		addInfoTypeList.add(addlInfoType);

		return addInfoTypeList;
	}


	public static AdditionalInfoType setAdditionalInfoType(AdditionalInfoType addlInfoType, Long memId, int i, int amt) {

		switch (i) {
		case 0:
			addlInfoType.setAPTCAmount(new BigDecimal(amt));
			break;
		case 1:
			addlInfoType.setCSRAmount(new BigDecimal(amt));
			break;
		case 3:
			addlInfoType.setTotalPremiumAmount(new BigDecimal(amt));
			break;
		case 4:
			addlInfoType.setTotalIndividualResponsibilityAmount(new BigDecimal(amt));
			break;
		case 5:
			addlInfoType.setRatingArea(memId + " RATING AREA");
			break;
		}

		return addlInfoType;
	}


	public static LocalDateTime getLocalDateTimeWithMicros() {

		Long microSec = getRandom3DigitNumber();
		LocalDateTime ldt = LocalDateTime.now();
		Long micros = (microSec * 1000);
		ldt = ldt.plusNanos(micros);
		return ldt;
	}


	public static Long getRandom3DigitNumber() {

		return getRandomNumber(3);
	}


	public static Long getRandomNumber(int digits) 
	{
		double dblDigits = (double) digits;
		double min = Math.pow(10.0, dblDigits - 1);
		double max = Math.pow(10.0, dblDigits) - 1;
		int randNum = (int) Math.round(Math.random() * (max - min) + min);
		return Long.valueOf(randNum);
	}

	public static String getRandomNumberAsString(int digits) {
		Long lng = getRandomNumber(digits);
		return lng.toString();
	}

	public static String makeRandomAlpha(int len) {

		char[] CHARSET_AZ = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
		Random random = new Random();
		char[] result = new char[len];
		for (int i = 0; i < result.length; i++) {
			int randomCharIndex = random.nextInt(CHARSET_AZ.length);
			result[i] = CHARSET_AZ[randomCharIndex];
		}
		return new String(result);
	}

	public static String getBemXMLAsString(BenefitEnrollmentMaintenanceType bem) {

		String xml = "";	

		if(bem != null) {
			try {
				JAXBElement<BenefitEnrollmentMaintenanceType> bemJaxB = 
						new JAXBElement<BenefitEnrollmentMaintenanceType>(new QName("BenefitEnrollmentMaintenance"), 
								BenefitEnrollmentMaintenanceType.class, bem);
				StringWriter stringWriter = new StringWriter();
				marshaller.marshal(bemJaxB, stringWriter);

				xml = stringWriter.toString();
			} catch (Exception ex) {
				System.out.println("Ex at outputBerXML: "+ ex.getMessage());
			}
		}
		return xml;
	}

	/*
	 * Multi-line indented with field names.
	 */
	public static String toStringMultiLine(Object obj) {

		return ToStringBuilder.reflectionToString(obj, ToStringStyle.MULTI_LINE_STYLE);
	}

	/*
	 * All data on one line.
	 */
	public static String toStringDefault(Object obj) {

		return ToStringBuilder.reflectionToString(obj, ToStringStyle.DEFAULT_STYLE);
	}

	/*
	 * Just data, no field names
	 */
	public static String toStringNoFieldNames(Object obj) {

		return ToStringBuilder.reflectionToString(obj, ToStringStyle.SIMPLE_STYLE);
	}

	/**
	 * @return
	 */
	public static Timestamp getCurrentSqlTimeStamp() {

		Timestamp time = new Timestamp(new DateTime().getMillis());
		time.setNanos(0);
		return time;
	}

	public static String getBemXmlString(String strNum) {

		String xml = "<BenefitEnrollmentMaintenance>" +
				"<ControlNumber>" + strNum + "</ControlNumber>" +
				"<CurrentTimeStamp>" + getCurrentSqlTimeStamp() + "</CurrentTimeStamp>" +
				"</BenefitEnrollmentMaintenance>";
		return xml;
	}

	private static String getStateAbbrevRandom(Long id) {

		String abbr = null;

		switch (id.intValue() % 3) {
		case 0:
			abbr = "MD";
			break;
		case 1:
			abbr = "VA";
			break;
		case 2:
			abbr = "DC";
			break;
		case 3:
			abbr = "MN";
			break;	
		default:
			abbr = "MN";
		}

		return abbr;
	}

	private static InsuranceLineCodeSimpleType getInsuranceLineCodeSimpleTypeRandom() {
		InsuranceLineCodeSimpleType[] codes = InsuranceLineCodeSimpleType.values();
		return codes[randInt(0, codes.length - 1)];
	}

	private static int randInt(int min, int max) {

		Random rand = new Random();
		int randomNum = rand.nextInt((max - min) + 1) + min;
		return randomNum;
	}
}
