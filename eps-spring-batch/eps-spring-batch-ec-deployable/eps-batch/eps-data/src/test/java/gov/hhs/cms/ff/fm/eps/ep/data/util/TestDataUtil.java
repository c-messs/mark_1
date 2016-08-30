package gov.hhs.cms.ff.fm.eps.ep.data.util;

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
import gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentRequestDTO;
import gov.hhs.cms.ff.fm.eps.ep.enums.PolicyStatus;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;

import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;

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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.joda.time.DateTime;

/*
 * Generates all BEM composite objects with random data.
 * 
 * 
 * NOTE: After make any updates to this class, run ALL mapper units tests, since
 *       most of the those test rely on data generated from this class.
 */


public class TestDataUtil {


	private static final SimpleDateFormat sdfXmlDateTime = new SimpleDateFormat("yyyy-mm-dd'T'HH:mm:ss");

	protected static final LocalDate DATE = LocalDate.now();
	protected static final LocalDateTime DATETIME = LocalDateTime.now();
	protected static final int YEAR = DATE.getYear();

	
	protected static final LocalDate JAN_1 = LocalDate.of(YEAR, 1, 1);
	protected static final LocalDate JAN_15 = LocalDate.of(YEAR, 1, 15);
	protected static final LocalDate JAN_31 = LocalDate.of(YEAR, 1, 31);
	protected static final LocalDate FEB_1 = LocalDate.of(YEAR, 2, 1);
	protected static final LocalDate FEB_2 = LocalDate.of(YEAR, 2, 2);
	protected static final LocalDate JUN_29 = LocalDate.of(YEAR, 6, 29);
	protected static final LocalDate JUN_30 = LocalDate.of(YEAR, 6, 30);
	protected static final LocalDate JUL_4_1965 = LocalDate.of(YEAR, 7, 4);
	protected static final LocalDate DEC_31 = LocalDate.of(YEAR, 12, 31);


	private static LocalDate birthDate = JUL_4_1965;
	private static LocalDate eligibilityBegin = FEB_1;
	private static LocalDate eligibilityEnd = JUN_30;
	private static LocalDate benefitBeginDate = FEB_2;
	private static LocalDate benefitEndDate = JUN_29;
	private static LocalDate lastPremiumPaidDate  = JAN_15;
	private static LocalDate premiumPaidToDateEnd = JAN_31;

	public static BenefitEnrollmentRequestDTO makeBenefitEnrollmentRequestDTO(Long berId, String groupSenderId) {

		BenefitEnrollmentRequestDTO berDTO = new BenefitEnrollmentRequestDTO();
		berDTO.setBer(new BenefitEnrollmentRequest());
		berDTO.setFileInformation(makeFileInformationType(berId, groupSenderId));
		berDTO.setBatchId(berId);
		return berDTO;		
	}

	public static BenefitEnrollmentMaintenanceType makeBenefitEnrollmentMaintenanceType(Long bemId, String mgpi, LocalDate psd, LocalDate ped, PolicyStatus policyStatus, String gpn) {

		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setPolicyInfo(makePolicyInfoType(mgpi, psd, ped, policyStatus, gpn));
		bem.setTransactionInformation(makeTransactionInformationType(bemId.toString()));
		bem.setIssuer(makeIssuerType(bemId.toString()));

		return bem;
	}

	public static BenefitEnrollmentMaintenanceType makeBenefitEnrollmentMaintenanceType(Long bemId, String gpn) {

		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setPolicyInfo(makePolicyInfoType(bemId.toString() + "-MGPI", JAN_1, DEC_31, PolicyStatus.EFFECTUATED_2, gpn));
		bem.setTransactionInformation(makeTransactionInformationType(bemId.toString()));
		bem.setIssuer(makeIssuerType(bemId.toString()));

		return bem;
	}

	public static PolicyInfoType makePolicyInfoType(String mgpi, LocalDate psd, LocalDate ped, PolicyStatus policyStatus, String gpn) {

		PolicyInfoType policyInfo = new PolicyInfoType();
		policyInfo.setMarketplaceGroupPolicyIdentifier(mgpi);
		policyInfo.setPolicyStartDate(DateTimeUtil.getXMLGregorianCalendar(psd));
		policyInfo.setPolicyEndDate(DateTimeUtil.getXMLGregorianCalendar(ped));
		policyInfo.setPolicyStatus(policyStatus.getValue());
		policyInfo.setGroupPolicyNumber(gpn);
		return policyInfo;
	}


	public static TransactionInformationType makeTransactionInformationType(String bemId) {

		TransactionInformationType transInfoType = new TransactionInformationType();
		// <xsd:minLength value="4" /><xsd:maxLength value="9" /> 
		String controlNum = bemId.toString() + bemId.toString() + bemId.toString();
		controlNum = controlNum.length() > 9 ? controlNum.substring(0, 9) : controlNum;
		transInfoType.setControlNumber(controlNum);
		//Calendar to String to XMLGregorianCalendar for testing
		//Real data will be String to XMLGregorianCalendar
		transInfoType.setCurrentTimeStamp(DateTimeUtil.getXMLGregorianCalendar(LocalDateTime.now()));
		transInfoType.setExchangeCode(ExchangeCodeSimpleType.INDIVIDUAL);

		return transInfoType;
	}

	public static IssuerType makeIssuerType(String strNum) {

		IssuerType issType = new IssuerType();
		issType.setName("ISSUERNAME" + strNum);
		// Length = 10
		String hiosId = strNum + strNum;
		hiosId = hiosId.length() > 10 ?  hiosId.substring(0, 10) : hiosId;
		issType.setHIOSID(hiosId);
		issType.setCMSPlanID(strNum);
		issType.setTaxPayerIdentificationNumber("TAXPAYID-" + strNum);
		return issType;
	}


	public static MemberType makeMemberTypeSubscriber(Long memId, String name, String hiosId, String state) {

		return makeMemberType(memId, name, true, memId.toString(), hiosId, state);
	}

	public static MemberType makeMemberTypeDependant(Long memId, String name) {

		return makeMemberType(memId, name, false, null);
	}

	public static MemberType makeMemberType(Long memId, String name, boolean isSubscriber) {

		String state = "VA";
		String strMemId = memId.toString() + memId.toString() + memId.toString();
		String hiosId = strMemId.substring(0, 5);
		MemberType memType = makeMemberType(memId, name, isSubscriber, hiosId, state);
		return memType;
	}

	public static MemberType makeMemberType(Long memId, String name, boolean isSubscriber, String subscriberId) {

		String state = "VA";
		String strMemId = memId.toString() + memId.toString() + memId.toString();
		String hiosId = strMemId.substring(0, 5);
		MemberType memType = makeMemberType(memId, name, isSubscriber, hiosId, state);
		memType.setSubscriberID(subscriberId);
		return memType;
	}

	public static MemberType makeMemberType(Long memId, String name, boolean isSubscriber, String subscriberId, String hiosId, String state) {

		MemberType memType = makeMemberType(memId, name, isSubscriber, hiosId, state);
		memType.setSubscriberID(subscriberId);
		return memType;
	}

	public static MemberType makeMemberType(Long memId, String name, boolean isSubscriber, String hiosId, String state) {

		MemberType memType = new MemberType();

		memType.setMemberInformation(makeMemberRelatedInfoType(memId, isSubscriber));

		memType.setSubscriberID(name + "-" + memId);

		memType.setMemberAdditionalIdentifier(makeMemberAdditionalIdentifierType(memId, name));

		memType.setMemberRelatedDates(makeMemberRelatedDatesType(isSubscriber));

		memType.setMemberNameInformation(makeMemberNameInfoType(memId, name, isSubscriber));

		memType.getHealthCoverage().add(makeHealthCoverageType(memId, hiosId, state));

		memType.getAdditionalInfo().add(makeAdditionalInfoType());

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


	public static MemberAdditionalIdentifierType makeMemberAdditionalIdentifierType(Long memId, String name) {

		MemberAdditionalIdentifierType memAddIdType = new MemberAdditionalIdentifierType();

		memAddIdType.setExchangeAssignedMemberID(makeExchangeAssignedMemberID(memId, name));
		memAddIdType.setIssuerAssignedMemberID("ISS-" + String.valueOf(memId));
		memAddIdType.setIssuerAssignedSubscriberID("SUB-" + String.valueOf(memId));
		return memAddIdType;
	}

	public static String makeExchangeAssignedMemberID(Long memId, String name) {

		return name + "-" + memId.toString();

	}

	public static MemberRelatedDatesType makeMemberRelatedDatesType(boolean isSubscriber) {

		MemberRelatedDatesType memRelDatesType = new MemberRelatedDatesType();

		memRelDatesType.setEligibilityBeginDate(DateTimeUtil.getXMLGregorianCalendar(eligibilityBegin));
		memRelDatesType.setEligibilityEndDate(DateTimeUtil.getXMLGregorianCalendar(eligibilityEnd));

		return memRelDatesType;
	}

	public static MemberNameInfoType makeMemberNameInfoType(Long memId, String name) {

		return makeMemberNameInfoType(memId, name, true);
	}


	public static MemberNameInfoType makeMemberNameInfoType(Long memId, String name, boolean isSubscriber) {

		MemberNameInfoType memNameInfoType = new MemberNameInfoType();
		memNameInfoType.setMemberName(makeIndividualNameType(memId, name));
		memNameInfoType.setMemberDemographics(makeMemberDemographicsType());
		memNameInfoType.setMemberResidenceAddress(makeResidentialAddressType(memId, name));  
		return memNameInfoType;
	}


	public static ResidentialAddressType makeResidentialAddressType(Long memId, String name) {

		ResidentialAddressType resAddrType = new ResidentialAddressType();

		String strAddrNum = name;

		resAddrType.setStateCode("VA");
		String zipCode = strAddrNum.length() > 10 ? strAddrNum.substring(0, 10) : strAddrNum;
		resAddrType.setPostalCode("00" + zipCode);
		String chr3 = memId.toString() + memId.toString();
		chr3 = chr3.substring(0, 3);

		return resAddrType;
	}

	public static IndividualNameType makeIndividualNameType(Long memId, String name) {

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

	public static MemberDemographicsType makeMemberDemographicsType() {

		MemberDemographicsType memDemoType = new MemberDemographicsType();

		memDemoType.setGenderCode(GenderCodeSimpleType.M);
		memDemoType.setBirthDate(DateTimeUtil.getXMLGregorianCalendar(birthDate));

		return memDemoType;

	}

	public static HealthCoverageType makeHealthCoverageType(Long memId, String hiosId, String state) {

		HealthCoverageType healthCovType = new HealthCoverageType();

		healthCovType.setHealthCoverageInformation(makeHealthCoverageInfoType());		
		healthCovType.setHealthCoveragePolicyNumber(makeHealthCoveragePolicyNumberType(memId, hiosId, state));
		healthCovType.setHealthCoverageDates(makeHealthCoverageDatesType());

		return healthCovType;
	}

	public static HealthCoverageInfoType makeHealthCoverageInfoType() {

		HealthCoverageInfoType healthCovInfoType = new HealthCoverageInfoType();
		healthCovInfoType.setInsuranceLineCode(InsuranceLineCodeSimpleType.HLT);
		return healthCovInfoType;
	}

	public static HealthCoveragePolicyNumberType makeHealthCoveragePolicyNumberType(Long memId, String hiosId, String state) {

		HealthCoveragePolicyNumberType hcPolNumType = new HealthCoveragePolicyNumberType();
		hcPolNumType.setInternalControlNumber(memId.toString());

		String strMemId = memId.toString() + memId.toString() + memId.toString() + memId.toString();
		String str7 = strMemId.substring(0, 7);
		hcPolNumType.setContractCode(hiosId + state + str7 + "06"); // <-- last 2 chars are insurancePlanVariantCd (00,01,02,03,04,05,06)

		return hcPolNumType;
	}

	public static HealthCoverageDatesType makeHealthCoverageDatesType() {

		HealthCoverageDatesType healthCovDatesType = new HealthCoverageDatesType();

		healthCovDatesType.setBenefitBeginDate(DateTimeUtil.getXMLGregorianCalendar(benefitBeginDate));
		healthCovDatesType.setBenefitEndDate(DateTimeUtil.getXMLGregorianCalendar(benefitEndDate));
		healthCovDatesType.setLastPremiumPaidDate(DateTimeUtil.getXMLGregorianCalendar(lastPremiumPaidDate));
		healthCovDatesType.setPremiumPaidToDateEnd(DateTimeUtil.getXMLGregorianCalendar(premiumPaidToDateEnd));

		return healthCovDatesType;
	}

	public static FileInformationType makeFileInformationType(Long id, String groupSenderId) {

		FileInformationType fileInfoType = new FileInformationType();

		fileInfoType.setGroupReceiverID("GRID-" + id.toString());
		fileInfoType.setGroupSenderID(groupSenderId);
		fileInfoType.setGroupControlNumber(id.toString());
		//Calendar to String to XMLGregorianCalendar for testing
		//Real data will be String to XMLGregorianCalendar
		fileInfoType.setGroupTimeStamp(DateTimeUtil.getXMLGregorianCalendar(DATETIME));
		String versionNum = "23"; //See bem.xsd 
		fileInfoType.setVersionNumber(versionNum);

		return fileInfoType;

	}


	public static String makeFileInformationTypeAsStringXML(Long id) {

		String strXML = "<FileInformation>" +
				"<GroupSenderID>" + id + " GRPSNDRID</GroupSenderID>" +
				"<GroupReceiverID>" + id + " GRPRCVRID</GroupReceiverID>" +
				"<GroupControlNumber>" + id + "</GroupControlNumber>" +
				"<GroupTimeStamp>" + sdfXmlDateTime.format(new DateTime().toDate()) +"</GroupTimeStamp>" +
				"<VersionNumber>23</VersionNumber>" +
				"</FileInformation>";

		return strXML;

	}

	public static AdditionalInfoType makeAdditionalInfoType() {

		AdditionalInfoType ait = new AdditionalInfoType();

		ait.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(JAN_1));
		ait.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(DEC_31));
		ait.setAPTCAmount(new BigDecimal("11.11"));
		ait.setCSRAmount(new BigDecimal("22.22"));
		ait.setTotalIndividualResponsibilityAmount(new BigDecimal("33.33"));
		ait.setTotalPremiumAmount(new BigDecimal("44.44"));
		ait.setRatingArea("RA TXT");

		return ait;

	}

	public static AdditionalInfoType makeAdditionalInfoType(LocalDate esd, LocalDate eed, BigDecimal aptc, BigDecimal csr, BigDecimal tpa, BigDecimal tira, String ra) {

		AdditionalInfoType ait = new AdditionalInfoType();

		ait.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		ait.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));
		ait.setAPTCAmount(aptc);
		ait.setCSRAmount(csr);
		ait.setTotalIndividualResponsibilityAmount(tira);
		ait.setTotalPremiumAmount(tpa);
		ait.setRatingArea(ra);

		return ait;
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

	public static String getRandomNumberAsString(int digits) 
	{
		Long lng = getRandomNumber(digits);
		return lng.toString();
	}

	public static String makeRandomAlpha(int len) 
	{
		char[] CHARSET_AZ = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
		Random random = new Random();
		char[] result = new char[len];
		for (int i = 0; i < result.length; i++) {
			int randomCharIndex = random.nextInt(CHARSET_AZ.length);
			result[i] = CHARSET_AZ[randomCharIndex];
		}
		return new String(result);

	}


	public static String getBerXMLAsString(BenefitEnrollmentRequest ber) {

		String xml = "";
		try {
			JAXBContext context = JAXBContext.newInstance(BenefitEnrollmentRequest.class);
			Marshaller marshaller = context.createMarshaller();
			//marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			StringWriter stringWriter = new StringWriter();
			marshaller.marshal(ber, stringWriter);
			xml = stringWriter.toString();

		} catch (Exception ex) {
			System.out.println("Ex at outputBerXML: "+ ex.getMessage());
		}
		return xml;
	}

	public static String getBemXMLAsString(BenefitEnrollmentMaintenanceType bem) {

		String xml = "";	

		if(bem != null) {
			try {
				JAXBElement<BenefitEnrollmentMaintenanceType> bemJaxB = 
						new JAXBElement<BenefitEnrollmentMaintenanceType>(new QName("BenefitEnrollmentMaintenance"), 
								BenefitEnrollmentMaintenanceType.class, bem);
				JAXBContext jaxbContext = JAXBContext.newInstance(String.class, BenefitEnrollmentMaintenanceType.class);
				Marshaller marshaller = jaxbContext.createMarshaller();
				marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
				StringWriter stringWriter = new StringWriter();
				marshaller.marshal(bemJaxB, stringWriter);

				xml = stringWriter.toString();
			} catch (Exception ex) {
				System.out.println("Ex at outputBemXML: "+ ex.getMessage());
			}
		}
		return xml;
	}


	/*
	 * Multi-line indented with feild names.
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


	public static String prettyFormat(String input, int indent) {
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

	public static String prettyFormat(String input) {
		return prettyFormat(input, 2);
	}

}
