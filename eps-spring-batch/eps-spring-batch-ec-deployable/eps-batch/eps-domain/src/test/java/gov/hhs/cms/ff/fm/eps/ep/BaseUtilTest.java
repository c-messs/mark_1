package gov.hhs.cms.ff.fm.eps.ep;

import gov.cms.dsh.bem.AdditionalInfoType;
import gov.cms.dsh.bem.BooleanIndicatorSimpleType;
import gov.cms.dsh.bem.HealthCoveragePolicyNumberType;
import gov.cms.dsh.bem.HealthCoverageType;
import gov.cms.dsh.bem.MemberAdditionalIdentifierType;
import gov.cms.dsh.bem.MemberRelatedDatesType;
import gov.cms.dsh.bem.MemberRelatedInfoType;
import gov.cms.dsh.bem.MemberType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import junit.framework.TestCase;

public abstract class BaseUtilTest extends TestCase {

	protected static final LocalDate DATE = LocalDate.now();
	protected static final LocalDateTime DATETIME = LocalDateTime.now();
	protected static final int YEAR = DATE.getYear();
	
	protected final LocalDate JAN_1 = LocalDate.of(YEAR, 1, 1);
	protected final LocalDate JAN_15 = LocalDate.of(YEAR, 1, 15);
	protected final LocalDate JAN_31 = LocalDate.of(YEAR, 1, 31);
	protected final LocalDate FEB_1 = LocalDate.of(YEAR, 2, 1);
	protected final LocalDate FEB_MAX = DATE.with(TemporalAdjusters.lastDayOfMonth());
	protected final LocalDate MAR_1 = LocalDate.of(YEAR, 3, 1);
	protected final LocalDate MAR_31 = LocalDate.of(YEAR, 3, 31);

	// Key Financial Elements
	protected final String APTC = "APTC";
	protected final String TIRA = "TIRA";
	protected final String TPA = "TPA";
	protected final String CSR = "CSR";
	// Carry over elements
	protected final String RA = "RA";
	
	protected final String AMRC = "AMRC";
	protected final String PA1 = "PA1";

	private DatatypeFactory dfInstance = null;


	@Override
	public void setUp() throws Exception {

		dfInstance = DatatypeFactory.newInstance();

	}

	@Override
	public void tearDown() throws Exception {

	}
	
	
	
	/**
	 * Converts a java.LocalDateTime into an instance of XMLGregorianCalendar
	 * 
	 * Marshalled output format is determined by XSD type:
	 * - xsd:date      YYYY-MM-DD (with no time and no timezone)
	 * @param date
	 * @return
	 */
	protected XMLGregorianCalendar getXMLGregorianCalendar(LocalDate localDate) {
		XMLGregorianCalendar xmlGregCal = null;
		if (localDate != null) {
			xmlGregCal = dfInstance.newXMLGregorianCalendar(localDate.toString());
			xmlGregCal.setTime(0, 0, 0);
			xmlGregCal.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
			xmlGregCal.setMillisecond(DatatypeConstants.FIELD_UNDEFINED);
		}
		return xmlGregCal;
	}
	
	protected XMLGregorianCalendar getXMLGregorianCalendar(LocalDateTime localDT) {
		XMLGregorianCalendar xmlGregCal = null;
		if (localDT != null) {
			xmlGregCal = dfInstance.newXMLGregorianCalendar(localDT.toString());
			xmlGregCal.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
			xmlGregCal.setMillisecond(DatatypeConstants.FIELD_UNDEFINED);
		}
		return xmlGregCal;
	}
	
	protected LocalDate getLocalDateFromXmlGC(XMLGregorianCalendar xmlGC) {

		LocalDate dt = null;
		if (xmlGC != null ) {
			dt = xmlGC.toGregorianCalendar().toZonedDateTime().toLocalDate();
		}
		return dt;
	}
	
	protected LocalDateTime getLocalDateTimeFromXmlGC(XMLGregorianCalendar xmlGC) {

		LocalDateTime dtTime = null;
		if (xmlGC != null ) {
			dtTime = xmlGC.toGregorianCalendar().toZonedDateTime().toLocalDateTime();
		}
		return dtTime;
	}
	
	protected MemberType makeSubscriber() {

		MemberType subscriber = new MemberType();
		subscriber.setMemberInformation(new MemberRelatedInfoType());
		subscriber.getMemberInformation().setSubscriberIndicator(BooleanIndicatorSimpleType.Y);
		return subscriber;
	}

	protected MemberType makeSubscriber(String id) {

		return makeSubscriber(id, null, null, null, null);
	}

	protected MemberType makeSubscriber(String id, String variantId) {

		return makeSubscriber(id, variantId, null, null, null);
	}


	protected MemberType makeSubscriber(String id, String variantId, LocalDate ebd, LocalDate esd, BigDecimal csr) {

		MemberType subscriber = new MemberType();
		subscriber.setMemberInformation(new MemberRelatedInfoType());
		subscriber.getMemberInformation().setSubscriberIndicator(BooleanIndicatorSimpleType.Y);
		if (ebd != null) {
			subscriber.setMemberRelatedDates(new MemberRelatedDatesType());
			subscriber.getMemberRelatedDates().setEligibilityBeginDate(getXMLGregorianCalendar(ebd));
		}
		subscriber.setSubscriberID(id);
		subscriber.getHealthCoverage().add(new HealthCoverageType());
		subscriber.getHealthCoverage().get(0).setHealthCoveragePolicyNumber(new HealthCoveragePolicyNumberType());
		if (variantId == null) {
			subscriber.getHealthCoverage().get(0).getHealthCoveragePolicyNumber().setContractCode("12345678901234" + "01");
		} else {
			subscriber.getHealthCoverage().get(0).getHealthCoveragePolicyNumber().setContractCode("12345678901234" + variantId);
		}
		if (csr != null) {
			subscriber.getAdditionalInfo().add(makeAdditionalInfoType(CSR, esd, null, csr));
		}
		return subscriber;
	}
	
	protected MemberType makeMember(Long id, String name) {

		MemberType member = new MemberType();
		member.setMemberAdditionalIdentifier(new MemberAdditionalIdentifierType());
		member.getMemberAdditionalIdentifier().setExchangeAssignedMemberID("EAid-" + id.toString());
		return member;
	}

	protected AdditionalInfoType makeAdditionalInfoType(String type, LocalDate esd, LocalDate eed, BigDecimal amt) {

		AdditionalInfoType ait = new AdditionalInfoType();
		ait.setEffectiveStartDate(getXMLGregorianCalendar(esd));
		if (eed != null) {
			ait.setEffectiveEndDate(getXMLGregorianCalendar(eed));
		}
		if (type.equals(APTC)) {
			ait.setAPTCAmount(amt); 
		} else if (type.equals(TIRA)) {
			ait.setTotalIndividualResponsibilityAmount(amt);
		} else if (type.equals(TPA)) {
			ait.setTotalPremiumAmount(amt);
		} else if (type.equals(CSR)) {
			ait.setCSRAmount(amt);
		}
		return ait;
	}
	
	protected AdditionalInfoType makeAdditionalInfoType(String type, LocalDate esd, LocalDate eed, String txt) {

		AdditionalInfoType ait = new AdditionalInfoType();
		ait.setEffectiveStartDate(getXMLGregorianCalendar(esd));
		if (eed != null) {
			ait.setEffectiveEndDate(getXMLGregorianCalendar(eed));
		}
		if (type.equals(RA)) {
			ait.setRatingArea(txt); 
		}
		return ait;
	}


}
