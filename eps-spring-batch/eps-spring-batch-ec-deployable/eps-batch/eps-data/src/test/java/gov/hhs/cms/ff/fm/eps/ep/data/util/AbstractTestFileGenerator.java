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
import gov.hhs.cms.ff.fm.eps.ep.enums.ExchangeType;
import gov.hhs.cms.ff.fm.eps.ep.enums.PolicyStatus;
import gov.hhs.cms.ff.fm.eps.ep.util.EpsDateUtils;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Random;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.joda.time.DateTime;

public class AbstractTestFileGenerator  {

	protected final String APTC ="APTCAmount";			
	protected final String CSR ="CSRAmount";					
	protected final String TPA ="TotalPremiumAmount";			
	protected final String TIRA ="TotalIndividualResponsibilityAmount";					
	protected final String RA ="RatingArea";									
	protected final String PRO_APTC ="ProratedAppliedAPTCAmount";
	protected final String PRO_CSR ="ProratedCSRAmount";
	protected final String PRO_MPA ="ProratedMonthlyPremiumAmount";
	protected final String PRO_TIRA ="ProratedIndividualResponsibleAmount";	
	protected final String ESD ="EffectiveStartDate";			
	protected final String EED ="EffectiveEndDate";

	//For Files sent by Partners, the format will be THHMMSSt
	protected final SimpleDateFormat sdf = new SimpleDateFormat("'D'yyMMdd'.T'HHmmssSSS");

	// Interval in milliseconds to make unique file names based on time.
	protected final int SLEEP_INTERVAL = 1015;

	protected final DateTime DATETIME = new DateTime();

	protected final int YEAR = DATETIME.getYear();

	protected final DateTime JAN_1 = new DateTime(YEAR, 1, 1, 0, 0);
	protected final DateTime JAN_15 = new DateTime(YEAR, 1, 15, 0, 0);
	protected final DateTime JAN_31 = new DateTime(YEAR, 1, 31, 0, 0);
	protected final DateTime FEB_1 = new DateTime(YEAR, 2, 1, 0, 0);
	protected final DateTime FEB_2 = new DateTime(YEAR, 2, 2, 0, 0);
	protected final DateTime FEB_15 = new DateTime(YEAR, 2, 15, 0, 0);
	protected final DateTime MAR_14 = new DateTime(YEAR, 3, 14, 0, 0);
	protected final DateTime MAR_15 = new DateTime(YEAR, 3, 15, 0, 0);
	protected final DateTime JUN_29 = new DateTime(YEAR, 6, 29, 0, 0);
	protected final DateTime JUN_30 = new DateTime(YEAR, 6, 30, 0, 0);
	protected final DateTime JUL_3_1965 = new DateTime(1965, 7, 3, 0, 0);
	protected final DateTime JUL_4_1965 = new DateTime(1965, 7, 4, 0, 0);
	protected final DateTime JUL_7_1970 = new DateTime(1970, 7, 7, 0, 0);
	protected final DateTime AUG_8_1980 = new DateTime(1980, 8, 9, 0, 0);
	protected final DateTime SEP_9_1990 = new DateTime(1990, 9, 9, 0, 0);
	protected final DateTime SEP_11_2001 = new DateTime(2001, 9, 11, 0, 0);
	protected final DateTime OCT_10_2000 = new DateTime(2000, 10, 10, 0, 0);


	private DateTime eligibilityBegin = FEB_1;
	private DateTime eligibilityEnd = JUN_30;
	private DateTime effectiveStart = FEB_15;
	private DateTime benefitBeginDate = FEB_2;
	private DateTime benefitEndDate = JUN_29;
	private DateTime lastPremiumPaidDate  = JAN_15;
	private DateTime premiumPaidToDateEnd = JAN_31;

	private static Marshaller marshallerBER;
	private static Marshaller marshallerBEM;


	private boolean doSkip = false;
	private boolean doMNTChng = false;

	static {
		try {
			JAXBContext jaxbContextBER = JAXBContext.newInstance(BenefitEnrollmentRequest.class);
			marshallerBER = jaxbContextBER.createMarshaller();
			marshallerBER.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			JAXBContext jaxbContextBEM = JAXBContext.newInstance(String.class, BenefitEnrollmentMaintenanceType.class);
			marshallerBEM = jaxbContextBEM.createMarshaller();
			marshallerBEM.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

		} catch (JAXBException ex) {
			System.out.print("EPROD-24 EPS JAXB Marshalling error (BEM to XML).\n" + ex.getMessage());
		}
	}

	private static DatatypeFactory dfInstance = null;

	static {

		try {
			dfInstance = DatatypeFactory.newInstance();

		} catch (DatatypeConfigurationException ex) {
			throw new IllegalStateException(
					"Exception in getting instance of DatatypeFactory", ex);
		}
	}


	protected BenefitEnrollmentMaintenanceType makeBenefitEnrollmentMaintenanceType(Long bemId, String versionNum, DateTime versionDT, String hiosId) {

		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setTransactionInformation(makeTransactionInformationType(bemId.toString(), versionNum, versionDT));
		bem.setIssuer(makeIssuerType(bemId.toString()));
		return bem;
	}


	private TransactionInformationType makeTransactionInformationType(String bemId, String versionNum, DateTime versionDT) {

		TransactionInformationType transInfoType = new TransactionInformationType();
		// <xsd:minLength value="4" /><xsd:maxLength value="9" /> 
		String controlNum = bemId.toString() + bemId.toString() + bemId.toString();
		controlNum = controlNum.length() > 9 ? controlNum.substring(0, 9) : controlNum;
		transInfoType.setControlNumber(controlNum);
		//Calendar to String to XMLGregorianCalendar for testing
		//Real data will be String to XMLGregorianCalendar
		transInfoType.setCurrentTimeStamp(EpsDateUtils.getXMLGregorianCalendar(versionDT));
		transInfoType.setExchangeCode(ExchangeCodeSimpleType.INDIVIDUAL);
		transInfoType.setPolicySnapshotDateTime(EpsDateUtils.getXMLGregorianCalendar(versionDT));
		transInfoType.setPolicySnapshotVersionNumber(versionNum);
		return transInfoType;
	}

	private IssuerType makeIssuerType(String hiosId) {

		IssuerType issType = new IssuerType();
		issType.setName(hiosId + "-ISSUERNM");
		issType.setHIOSID(hiosId);
		return issType;
	}

	protected PolicyInfoType makePolicyInfoType(Long bemId, PolicyStatus policyStatus, String gpn, DateTime psd, DateTime ped) {

		PolicyInfoType policyInfoType = new PolicyInfoType();
		policyInfoType.setGroupPolicyNumber(gpn);
		
		if (doSkip) {
			String strBemId = bemId.toString();
			if (strBemId.indexOf("4000") == 0) {
				policyInfoType.setMarketplaceGroupPolicyIdentifier("XXXX-MPGPID");
			} else {
				policyInfoType.setMarketplaceGroupPolicyIdentifier(bemId.toString() + "-MPGPID");
			}
		} else {
			policyInfoType.setMarketplaceGroupPolicyIdentifier(bemId.toString() + "-MPGPID");
		}
		policyInfoType.setPolicyStartDate(EpsDateUtils.getXMLGregorianCalendar(psd));
		policyInfoType.setPolicyEndDate(EpsDateUtils.getXMLGregorianCalendar(ped));
		
		if (doSkip) {
			String strBemId = bemId.toString();
			if (strBemId.indexOf("6000") == 0) {
				policyInfoType.setPolicyStatus("0");
			} else {
				policyInfoType.setPolicyStatus(policyStatus.getValue());
			}
		} else {
			policyInfoType.setPolicyStatus(policyStatus.getValue());
		}
		return policyInfoType;
	}


	protected MemberType makeMemberType(Long memId, String name, boolean isSubscriber, String subscriberId, 
			String hcGPN, String groupSenderId, BigDecimal tpa, String variantId) {

		MemberType memType = new MemberType();
		memType.setSubscriberID(subscriberId);

		String stateCd = groupSenderId.substring(5, 7);

		memType.setMemberInformation(makeMemberRelatedInfoType(memId, isSubscriber));
		memType.setMemberAdditionalIdentifier(makeMemberAdditionalIdentifierType(memId));
		memType.setMemberRelatedDates(makeMemberRelatedDatesType(isSubscriber));
		memType.setMemberNameInformation(makeMemberNameInfoType(memId, name, stateCd, subscriberId));
		memType.getHealthCoverage().add(makeHealthCoverageType(memId, groupSenderId, hcGPN, variantId));
		if (isSubscriber) {
			memType.getAdditionalInfo().add(makeAdditionalInfoType(memId, stateCd, tpa));
		}

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
		memRelDatesType.setEligibilityBeginDate(EpsDateUtils.getXMLGregorianCalendar(eligibilityBegin));
		memRelDatesType.setEligibilityEndDate(EpsDateUtils.getXMLGregorianCalendar(eligibilityEnd));
		return memRelDatesType;

	}

	private ResidentialAddressType makeResidentialAddressType(Long memId, String name, String stateCd, String subscriberId) {

		ResidentialAddressType resAddrType = new ResidentialAddressType();

		resAddrType.setStateCode(stateCd);
		String zipCode = name.length() > 10 ? name.substring(0, 10) : name;
		resAddrType.setPostalCode("00" + zipCode);
		String chr3 = memId.toString() + memId.toString();
		chr3 = chr3.substring(0, 3);

		return resAddrType;
	}


	private AdditionalInfoType makeAdditionalInfoType(Long memId, String stateCd, BigDecimal tpa)  {

		AdditionalInfoType ait = new AdditionalInfoType();

		DateTime esd = effectiveStart;
		DateTime eed = eligibilityEnd;

		ait.setEffectiveStartDate(EpsDateUtils.getXMLGregorianCalendar(esd));
		ait.setEffectiveEndDate(EpsDateUtils.getXMLGregorianCalendar(eed));

		BigDecimal aptc = tpa.multiply(new BigDecimal(".1"));
		BigDecimal csr = new BigDecimal("0");
		BigDecimal sum = aptc;
		BigDecimal tira = tpa.subtract(sum).setScale(2, RoundingMode.HALF_UP);

		String ra = stateCd +"001";

		BigDecimal proAptc = aptc.divide(new BigDecimal(".8")).setScale(2, RoundingMode.HALF_UP);
		BigDecimal proCsr = csr;
		BigDecimal proMpa = tpa.divide(new BigDecimal(".8")).setScale(2, RoundingMode.HALF_UP);
		BigDecimal proTira = tira.divide(new BigDecimal(".8")).setScale(2, RoundingMode.HALF_UP);

		ait.setAPTCAmount(aptc);
		ait.setCSRAmount(csr);
		ait.setTotalIndividualResponsibilityAmount(tira);
		ait.setRatingArea(ra);
		ait.setProratedAppliedAPTCAmount(proAptc);
		ait.setProratedCSRAmount(proCsr);
		ait.setProratedMonthlyPremiumAmount(proMpa);
		ait.setProratedIndividualResponsibleAmount(proTira);

		if (doSkip) {
			String strMemId = memId.toString();
			if (strMemId.indexOf("7000") == 0) {
				// Set TPA==0 to force skip
				ait.setTotalPremiumAmount(BigDecimal.ZERO);
			} else {
				ait.setTotalPremiumAmount(tpa);
			}
		} else {
			ait.setTotalPremiumAmount(tpa);
		}
		return ait;
	}

	private MemberNameInfoType makeMemberNameInfoType(Long memId, String name, String stateCd, String subscriberId) {

		MemberNameInfoType memNameInfoType = new MemberNameInfoType();
		memNameInfoType.setMemberName(makeIndividualNameType(memId, name));
		memNameInfoType.setMemberDemographics(makeMemberDemographicsType(name));
		memNameInfoType.setMemberResidenceAddress(makeResidentialAddressType(memId, name, stateCd, subscriberId));  
		return memNameInfoType;
	}

	private IndividualNameType makeIndividualNameType(Long memId, String name) {

		IndividualNameType indNameType = new IndividualNameType();

		String lastNm = name + "-" + memId + " LAST";
		lastNm = lastNm.length() > 60 ? lastNm.substring(0, 60): lastNm;
		if (doMNTChng) {
			indNameType.setLastName(lastNm + "-CHANGED");
		} else {
			indNameType.setLastName(lastNm);
		}

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

	private MemberDemographicsType makeMemberDemographicsType(String name) {

		MemberDemographicsType memDemoType = new MemberDemographicsType();

		if (name.indexOf("DAD") != -1) {

			memDemoType.setGenderCode(GenderCodeSimpleType.M);
			memDemoType.setBirthDate(EpsDateUtils.getXMLGregorianCalendar(JUL_7_1970));
			
		} else if (name.indexOf("MOM") != -1) {

			memDemoType.setGenderCode(GenderCodeSimpleType.F);
			memDemoType.setBirthDate(EpsDateUtils.getXMLGregorianCalendar(AUG_8_1980));
			
		} else if (name.indexOf("SON") != -1) {

			memDemoType.setGenderCode(GenderCodeSimpleType.M);
			memDemoType.setBirthDate(EpsDateUtils.getXMLGregorianCalendar(SEP_9_1990));
			
		} else if (name.indexOf("DAU") != -1) {

			memDemoType.setGenderCode(GenderCodeSimpleType.F);
			memDemoType.setBirthDate(EpsDateUtils.getXMLGregorianCalendar(OCT_10_2000));
			
		} else if (name.indexOf("BBY") != -1) {

			memDemoType.setGenderCode(GenderCodeSimpleType.M);
			memDemoType.setBirthDate(EpsDateUtils.getXMLGregorianCalendar(JAN_1));
			
		} else {
			
			memDemoType.setGenderCode(GenderCodeSimpleType.M);
			memDemoType.setBirthDate(EpsDateUtils.getXMLGregorianCalendar(JUL_4_1965));
		}

		return memDemoType;
	}


	private HealthCoverageType makeHealthCoverageType(Long memId, String groupSenderId, String hcGPN, String variantId) {

		HealthCoverageType healthCovType = new HealthCoverageType();

		healthCovType.setHealthCoverageInformation(makeHealthCoverageInfoType(memId));		
		healthCovType.setHealthCoveragePolicyNumber(makeHealthCoveragePolicyNumberType(memId, groupSenderId, hcGPN, variantId));
		healthCovType.setHealthCoverageDates(makeHealthCoverageDatesType());

		return healthCovType;
	}

	private HealthCoverageInfoType makeHealthCoverageInfoType(Long memId) {

		HealthCoverageInfoType healthCovInfoType = new HealthCoverageInfoType();
		String strMemId = memId.toString();
		if (doSkip) {
			if (strMemId.indexOf("5000") == 0) {
				healthCovInfoType.setInsuranceLineCode(null);
			} else {
				healthCovInfoType.setInsuranceLineCode(InsuranceLineCodeSimpleType.HLT);
			}
		} else {
			healthCovInfoType.setInsuranceLineCode(InsuranceLineCodeSimpleType.HLT);
		}
		return healthCovInfoType;
	}

	private HealthCoveragePolicyNumberType makeHealthCoveragePolicyNumberType(Long memId, String groupSenderId, String hcGPN, String variantId) {

		HealthCoveragePolicyNumberType hcPolNumType = new HealthCoveragePolicyNumberType();
		hcPolNumType.setContractCode(groupSenderId + variantId); // <-- last 2 chars are insurancePlanVariantCd (00,01,02,03,04,05,06)
		hcPolNumType.setInternalControlNumber(memId.toString());
		return hcPolNumType;
	}

	private HealthCoverageDatesType makeHealthCoverageDatesType() {

		HealthCoverageDatesType healthCovDatesType = new HealthCoverageDatesType();

		healthCovDatesType.setBenefitBeginDate(EpsDateUtils.getXMLGregorianCalendar(benefitBeginDate));
		healthCovDatesType.setBenefitEndDate(EpsDateUtils.getXMLGregorianCalendar(benefitEndDate));
		healthCovDatesType.setLastPremiumPaidDate(EpsDateUtils.getXMLGregorianCalendar(lastPremiumPaidDate));
		healthCovDatesType.setPremiumPaidToDateEnd(EpsDateUtils.getXMLGregorianCalendar(premiumPaidToDateEnd));

		return healthCovDatesType;
	}

	protected FileInformationType makeFileInformationType(Long id, ExchangeType exchangeType, String groupSenderId) {

		FileInformationType fileInfoType = new FileInformationType();

		String strId = String.format("%05d", id);
		String strId5 = strId.length() > 5 ? strId.substring(0, 5) : strId;

		fileInfoType.setGroupReceiverID(groupSenderId.substring(5, 7));  
		fileInfoType.setGroupSenderID(groupSenderId);

		//9 characters
		fileInfoType.setGroupControlNumber(strId5 + "-GCN");
		//Calendar to String to XMLGregorianCalendar for testing
		//Real data will be String to XMLGregorianCalendar
		fileInfoType.setGroupTimeStamp(getXMLGregorianCalendar(DATETIME.getMillis()));
		String versionNum = "23"; //See bem.xsd 
		fileInfoType.setVersionNumber(versionNum);
		return fileInfoType;
	}

	/**
	 * Converts a Long millis into an instance of XMLGregorianCalendar
	 * Marshalled output format is determined by XSD type:
	 * - xsd:dateTime  YYYY-MM-DDTHH:MM:SS (with no timezone) 
	 * - xsd:date      YYYY-MM-DD (with no time and no timezone)
	 * @param millis
	 * @return
	 */
	protected XMLGregorianCalendar getXMLGregorianCalendar(Long millis) {
		XMLGregorianCalendar xmlGregCal = null;
		if (millis == null) {
			return xmlGregCal;
		} else {
			GregorianCalendar gc = new GregorianCalendar();
			gc.setTimeInMillis(millis);
			xmlGregCal = dfInstance.newXMLGregorianCalendar(gc);
			xmlGregCal.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
			xmlGregCal.setMillisecond(DatatypeConstants.FIELD_UNDEFINED);
			return xmlGregCal;
		}
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
			StringWriter stringWriter = new StringWriter();
			marshallerBER.marshal(ber, stringWriter);
			xml = stringWriter.toString();
		} catch (Exception ex) {
			System.out.println("Ex at outputBerXML: "+ ex.getMessage());
		}
		return xml;
	}

	protected String getBemXMLAsString(BenefitEnrollmentMaintenanceType bem) {

		String xml = "";	

		if(bem != null) {
			try {
				JAXBElement<BenefitEnrollmentMaintenanceType> bemJaxB = 
						new JAXBElement<BenefitEnrollmentMaintenanceType>(new QName("BenefitEnrollmentMaintenance"), 
								BenefitEnrollmentMaintenanceType.class, bem);
				StringWriter stringWriter = new StringWriter();
				marshallerBEM.marshal(bemJaxB, stringWriter);

				xml = stringWriter.toString();
			} catch (Exception ex) {
				System.out.println("Ex at outputBemXML: "+ ex.getMessage());
			}
		}
		return xml;
	}

	public void setDoSkip(boolean doSkip) {
		this.doSkip = doSkip;
	}


	public void setDoMNTChng(boolean doMNTChng) {
		this.doMNTChng = doMNTChng;
	}

}
