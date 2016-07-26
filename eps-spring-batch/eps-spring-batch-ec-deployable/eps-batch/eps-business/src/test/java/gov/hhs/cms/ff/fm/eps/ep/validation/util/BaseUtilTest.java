package gov.hhs.cms.ff.fm.eps.ep.validation.util;

import gov.cms.dsh.bem.AdditionalInfoType;
import gov.cms.dsh.bem.AddressType;
import gov.cms.dsh.bem.BooleanIndicatorSimpleType;
import gov.cms.dsh.bem.MemberRelatedInfoType;
import gov.cms.dsh.bem.MemberType;
import gov.hhs.cms.ff.fm.eps.ep.util.EpsDateUtils;

import java.math.BigDecimal;

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
	protected final DateTime MAR_14 = new DateTime(YEAR, 3, 14, 0, 0);
	protected final DateTime MAR_15 = new DateTime(YEAR, 3, 15, 0, 0);
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
	protected final String SEPR = "SEPR";
	protected final String AIAO = "AIAO";
	protected final String SHOP = "SHOP";


	protected MemberType makeSubscriberMaintenance(String id) {

		MemberType subscriber = new MemberType();
		subscriber.setMemberInformation(new MemberRelatedInfoType());
		subscriber.getMemberInformation().setSubscriberIndicator(BooleanIndicatorSimpleType.Y);
		subscriber.setSubscriberID(id);
		return subscriber;
	}

	/**
	 * Makes a minimal AdditionalInfoType
	 * @param esd
	 * @param eed
	 * @return
	 */
	protected AdditionalInfoType makeAdditionalInfoType(DateTime esd, DateTime eed) {

		AdditionalInfoType ait = new AdditionalInfoType();
		ait.setEffectiveStartDate(EpsDateUtils.getXMLGregorianCalendar(esd));
		if (eed != null) {
			ait.setEffectiveEndDate(EpsDateUtils.getXMLGregorianCalendar(eed));
		}
		return ait;
	}

	protected AdditionalInfoType makeAdditionalInfoType(String type, DateTime esd, DateTime eed, BigDecimal amt) {

		AdditionalInfoType ait = new AdditionalInfoType();
		ait.setEffectiveStartDate(EpsDateUtils.getXMLGregorianCalendar(esd));
		if (eed != null) {
			ait.setEffectiveEndDate(EpsDateUtils.getXMLGregorianCalendar(eed));
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
		ait.setEffectiveStartDate(EpsDateUtils.getXMLGregorianCalendar(esd));
		if (eed != null) {
			ait.setEffectiveEndDate(EpsDateUtils.getXMLGregorianCalendar(eed));
		}
		if (type.equals(RA)) {
			ait.setRatingArea(txt); 
		}
		return ait;
	}
	
	protected AddressType makeAddressType(String id) {

		AddressType addrType = new AddressType();

		String strAddrNum = id.toString();
		addrType.setStateCode("VA");
		String zipCode = strAddrNum.length() > 10 ? strAddrNum.substring(0, 10) : strAddrNum;
		addrType.setPostalCode("00" + zipCode);

		return addrType;
	}

}
