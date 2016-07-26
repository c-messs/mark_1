package gov.hhs.cms.ff.fm.eps.ep.validation;

import gov.cms.dsh.bem.AdditionalInfoType;
import gov.cms.dsh.bem.BooleanIndicatorSimpleType;
import gov.cms.dsh.bem.HealthCoverageDatesType;
import gov.cms.dsh.bem.HealthCoveragePolicyNumberType;
import gov.cms.dsh.bem.HealthCoverageType;
import gov.cms.dsh.bem.IndividualNameType;
import gov.cms.dsh.bem.MemberAdditionalIdentifierType;
import gov.cms.dsh.bem.MemberDemographicsType;
import gov.cms.dsh.bem.MemberNameInfoType;
import gov.cms.dsh.bem.MemberRelatedDatesType;
import gov.cms.dsh.bem.MemberRelatedInfoType;
import gov.cms.dsh.bem.MemberType;
import gov.cms.dsh.bem.ResidentialAddressType;
import gov.hhs.cms.ff.fm.eps.ep.enums.AddressTypeEnum;
import gov.hhs.cms.ff.fm.eps.ep.util.EpsDateUtils;

import java.math.BigDecimal;

import junit.framework.TestCase;

import org.joda.time.DateTime;

public abstract class BaseValidationTest extends TestCase {

	protected final DateTime DATETIME = new DateTime();
	protected final int YEAR = DATETIME.getYear();

	protected final DateTime JAN_1 = new DateTime(YEAR, 1, 1, 0, 0);
	protected final DateTime JAN_15 = new DateTime(YEAR, 1, 15, 0, 0);
	protected final DateTime JAN_31 = new DateTime(YEAR, 1, 31, 0, 0);
	protected final DateTime FEB_1 = new DateTime(YEAR, 2, 1, 0, 0);
	protected final DateTime FEB_MAX = new DateTime(YEAR, 2, FEB_1.dayOfMonth().getMaximumValue(), 0, 0);
	protected final DateTime MAR_1 = new DateTime(YEAR, 3, 1, 0, 0);
	protected final DateTime MAR_14 = new DateTime(YEAR, 3, 14, 0, 0);
	protected final DateTime MAR_15 = new DateTime(YEAR, 3, 15, 0, 0);
	protected final DateTime MAR_20 = new DateTime(YEAR, 3, 20, 0, 0);
	protected final DateTime MAR_31 = new DateTime(YEAR, 3, 31, 0, 0);
	protected final DateTime APR_1 = new DateTime(YEAR, 4, 1, 0, 0);
	protected final DateTime APR_10 = new DateTime(YEAR, 4, 10, 0, 0);
	protected final DateTime APR_11 = new DateTime(YEAR, 4, 11, 0, 0);
	protected final DateTime APR_30 = new DateTime(YEAR, 4, 30, 0, 0);
	protected final DateTime MAY_1 = new DateTime(YEAR, 5, 1, 0, 0);

	protected final DateTime DEC_31 = new DateTime(YEAR, 12, 31, 0, 0);


	// Key Financial Elements
	protected final String APTC = "APTC";
	protected final String TIRA = "TIRA";
	protected final String TPA = "TPA";
	protected final String CSR = "CSR";
	// Carry over elements
	protected final String RA = "RA";
	// Non-policyPremium elements
	protected final String AMRC = "AMRC";
	protected final String PA1 = "PA1";
	protected final String AIAO = "AIAO";
	protected final String SHOP = "SHOP";


	@Override
	public void setUp() throws Exception {


	}

	@Override
	public void tearDown() throws Exception {

	}

	protected MemberType makeSubscriberMaintenance(String id) {

		return makeSubscriberMaintenance(id, null, null, null, null, null);
	}

	protected MemberType makeSubscriberMaintenance(String id, String variantId, DateTime hcMED, DateTime hcBBD, DateTime esd, BigDecimal csr) {

		return makeSubscriberMaintenance(id, variantId, hcMED, hcBBD, esd, csr, null);
	}

	protected MemberType makeSubscriberMaintenance(String id, String variantId, DateTime hcMED, DateTime hcBBD, DateTime esd, BigDecimal csr, DateTime sysSelESD) {

		return makeSubscriber(id,  variantId, hcBBD, null, esd, csr, sysSelESD);
	}

	
	protected MemberType makeSubscriber(String id, DateTime hcBBD, DateTime eligEnd) {

		MemberType subscriber = new MemberType();
		subscriber.setMemberInformation(new MemberRelatedInfoType());
		subscriber.getMemberInformation().setSubscriberIndicator(BooleanIndicatorSimpleType.Y);
		subscriber.setMemberAdditionalIdentifier(new MemberAdditionalIdentifierType());
		subscriber.getMemberAdditionalIdentifier().setExchangeAssignedMemberID(id);
		subscriber.setSubscriberID(id);
		subscriber.setMemberRelatedDates(new MemberRelatedDatesType());
		subscriber.getMemberRelatedDates().setEligibilityEndDate(EpsDateUtils.getXMLGregorianCalendar(eligEnd));
		subscriber.getHealthCoverage().add(new HealthCoverageType());
		subscriber.getHealthCoverage().get(0).setHealthCoveragePolicyNumber(new HealthCoveragePolicyNumberType());
		subscriber.getHealthCoverage().get(0).setHealthCoverageDates(new HealthCoverageDatesType());
		if (hcBBD != null) {
			subscriber.getHealthCoverage().get(0).getHealthCoverageDates().setBenefitBeginDate(EpsDateUtils.getXMLGregorianCalendar(hcBBD));
		} 
		if (id != null) {
			subscriber.setMemberNameInformation(new MemberNameInfoType());
			subscriber.getMemberNameInformation().setMemberResidenceAddress(makeAddressType(id, AddressTypeEnum.RESIDENTIAL));
			subscriber.getMemberNameInformation().setMemberName(new IndividualNameType());
			subscriber.getMemberNameInformation().getMemberName().setFirstName(id + "-FIRSTNM");
			subscriber.getMemberNameInformation().getMemberName().setLastName(id + "-LASTNM");
		}
		return subscriber;
	}

	protected MemberType makeSubscriber(String id, String variantId, DateTime hcBBD, DateTime hcBED, DateTime esd, BigDecimal csr, DateTime sysSelESD) {

		MemberType subscriber = new MemberType();
		subscriber.setMemberInformation(new MemberRelatedInfoType());
		subscriber.getMemberInformation().setSubscriberIndicator(BooleanIndicatorSimpleType.Y);
		subscriber.setMemberAdditionalIdentifier(new MemberAdditionalIdentifierType());
		subscriber.getMemberAdditionalIdentifier().setExchangeAssignedMemberID(id);
		subscriber.setSubscriberID(id);
		subscriber.getHealthCoverage().add(new HealthCoverageType());
		subscriber.getHealthCoverage().get(0).setHealthCoveragePolicyNumber(new HealthCoveragePolicyNumberType());
		if (variantId == null) {
			subscriber.getHealthCoverage().get(0).getHealthCoveragePolicyNumber().setContractCode("12345678901234" + "01");
		} else {
			subscriber.getHealthCoverage().get(0).getHealthCoveragePolicyNumber().setContractCode("12345678901234" + variantId);
		}
		subscriber.getHealthCoverage().get(0).setHealthCoverageDates(new HealthCoverageDatesType());
		if (hcBBD != null) {
			subscriber.getHealthCoverage().get(0).getHealthCoverageDates().setBenefitBeginDate(EpsDateUtils.getXMLGregorianCalendar(hcBBD));
		} 
		if (hcBED != null) {
			subscriber.getHealthCoverage().get(0).getHealthCoverageDates().setBenefitEndDate(EpsDateUtils.getXMLGregorianCalendar(hcBED));
		} 
		if (csr != null) {
			subscriber.getAdditionalInfo().add(makeAdditionalInfoType(CSR, esd, null, csr));
		}
		if (sysSelESD != null) {
			// Set any Key-Financial Element in order to determine the system selected Effective Start date.
			subscriber.getAdditionalInfo().add(makeAdditionalInfoType(TPA, sysSelESD, null, new BigDecimal("99.99")));
		}
		if (id != null) {
			subscriber.setMemberNameInformation(new MemberNameInfoType());
			subscriber.getMemberNameInformation().setMemberResidenceAddress(makeAddressType(id, AddressTypeEnum.RESIDENTIAL));
			subscriber.getMemberNameInformation().setMemberName(new IndividualNameType());
			subscriber.getMemberNameInformation().getMemberName().setFirstName(id + "-FIRSTNM");
			subscriber.getMemberNameInformation().getMemberName().setLastName(id + "-LASTNM");
		}
		return subscriber;
	}

	protected MemberType makeMemberType(String id) {

		return makeMemberType(id, null, null, null);
	}

	/**
	 * Make a MemberType.  If amrc is present an AdditionalInfoType will be created and added to
	 * the member.  The ESD will be set froom hcBBD and EED will be set to null.
	 * @param id
	 * @param mtc
	 * @param subscriberId
	 * @param groupPolicyNumber
	 * @param hcBBD
	 * @return
	 */
	protected MemberType makeMemberType(String id, String subscriberId, DateTime hcBBD, DateTime dob) {

		MemberType member = new MemberType();
		member.setMemberInformation(new MemberRelatedInfoType());
		member.getMemberInformation().setSubscriberIndicator(BooleanIndicatorSimpleType.N);
		member.setMemberNameInformation(new MemberNameInfoType());
		member.getMemberNameInformation().setMemberName(new IndividualNameType());
		member.getMemberNameInformation().getMemberName().setFirstName(id + " FIRSTNM");
		member.getMemberNameInformation().getMemberName().setLastName(id + " LASTNM");
		member.getMemberNameInformation().setMemberResidenceAddress(makeAddressType(id, AddressTypeEnum.RESIDENTIAL));
		if (dob != null) {
			member.getMemberNameInformation().setMemberDemographics(new MemberDemographicsType());
			member.getMemberNameInformation().getMemberDemographics().setBirthDate(EpsDateUtils.getXMLGregorianCalendar(dob));
		}
		member.setMemberAdditionalIdentifier(new MemberAdditionalIdentifierType());
		member.getMemberAdditionalIdentifier().setExchangeAssignedMemberID(id);
		member.getHealthCoverage().add(new HealthCoverageType());
		if (subscriberId != null) {
			member.setSubscriberID(subscriberId);
		}
		if (hcBBD != null) {
			member.getHealthCoverage().get(0).setHealthCoverageDates(new HealthCoverageDatesType());
			member.getHealthCoverage().get(0).getHealthCoverageDates().setBenefitBeginDate(EpsDateUtils.getXMLGregorianCalendar(hcBBD));	
		}
		return member;
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

	protected AdditionalInfoType makeEpsPremium(DateTime esd, DateTime eed, BigDecimal aptc, BigDecimal csr, BigDecimal tpa, 
			BigDecimal tira, BigDecimal opa1, BigDecimal opa2) {

		AdditionalInfoType ait = new AdditionalInfoType();
		ait.setEffectiveStartDate(EpsDateUtils.getXMLGregorianCalendar(esd));
		if (eed != null) {
			ait.setEffectiveEndDate(EpsDateUtils.getXMLGregorianCalendar(eed));
		}
		ait.setAPTCAmount(aptc);
		ait.setCSRAmount(csr);
		ait.setTotalPremiumAmount(tpa);
		ait.setTotalIndividualResponsibilityAmount(tira);

		return ait;
	}

	protected ResidentialAddressType makeAddressType(String id, AddressTypeEnum type) {

		ResidentialAddressType addrType = new ResidentialAddressType();

		String strAddrNum = id.toString();
		addrType.setStateCode("VA");
		String zipCode = strAddrNum.length() > 10 ? strAddrNum.substring(0, 10) : strAddrNum;
		addrType.setPostalCode("00" + zipCode);

		return addrType;
	}

}
