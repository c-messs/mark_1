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
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import junit.framework.TestCase;

import org.joda.time.DateTime;

public abstract class BaseUtilTest extends TestCase {

	protected static final DateTime DATETIME = new DateTime();
	protected static final int YEAR = DATETIME.getYear();

	protected final DateTime JAN_1 = new DateTime(YEAR, 1, 1, 0, 0);
	protected final DateTime JAN_15 = new DateTime(YEAR, 1, 15, 0, 0);
	protected final DateTime JAN_31 = new DateTime(YEAR, 1, 31, 0, 0);
	protected final DateTime FEB_1 = new DateTime(YEAR, 2, 1, 0, 0);
	protected final DateTime FEB_MAX = new DateTime(YEAR, 2, FEB_1.dayOfMonth().getMaximumValue(), 0, 0);
	protected final DateTime MAR_1 = new DateTime(YEAR, 3, 1, 0, 0);
	protected final DateTime MAR_31 = new DateTime(YEAR, 3, 31, 0, 0);

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


	protected XMLGregorianCalendar getXMLGregorianCalendar(org.joda.time.DateTime date) {
		XMLGregorianCalendar xmlGregCal = null;
		if (date == null) {
			return xmlGregCal;
		} else {
			GregorianCalendar gc = new GregorianCalendar();
			gc.setTimeInMillis(date.getMillis());
			xmlGregCal = dfInstance.newXMLGregorianCalendar(gc);
			xmlGregCal.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
			xmlGregCal.setMillisecond(DatatypeConstants.FIELD_UNDEFINED);
			return xmlGregCal;
		}
	}

	protected DateTime getDateTimeFromXmlGC(XMLGregorianCalendar xmlGC) {

		DateTime dtTime = null;
		if (xmlGC != null ) {
			dtTime = new DateTime(xmlGC.toGregorianCalendar().getTime());
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


	protected MemberType makeSubscriber(String id, String variantId, DateTime ebd, DateTime esd, BigDecimal csr) {

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

	protected AdditionalInfoType makeAdditionalInfoType(String type, DateTime esd, DateTime eed, BigDecimal amt) {

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
	
	protected AdditionalInfoType makeAdditionalInfoType(String type, DateTime esd, DateTime eed, String txt) {

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
