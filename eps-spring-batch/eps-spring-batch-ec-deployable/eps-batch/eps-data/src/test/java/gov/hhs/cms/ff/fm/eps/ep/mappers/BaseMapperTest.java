package gov.hhs.cms.ff.fm.eps.ep.mappers;

import gov.cms.dsh.bem.AdditionalInfoType;
import gov.cms.dsh.bem.BenefitEnrollmentMaintenanceType;
import gov.cms.dsh.bem.BooleanIndicatorSimpleType;
import gov.cms.dsh.bem.HealthCoverageDatesType;
import gov.cms.dsh.bem.HealthCoveragePolicyNumberType;
import gov.cms.dsh.bem.HealthCoverageType;
import gov.cms.dsh.bem.IndividualNameType;
import gov.cms.dsh.bem.MemberAdditionalIdentifierType;
import gov.cms.dsh.bem.MemberNameInfoType;
import gov.cms.dsh.bem.MemberRelatedInfoType;
import gov.cms.dsh.bem.MemberType;
import gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentMaintenanceDTO;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyPremiumPO;
import gov.hhs.cms.ff.fm.eps.ep.util.EpsDateUtils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;

import junit.framework.TestCase;

import org.joda.time.DateTime;

public abstract class BaseMapperTest extends TestCase {

	protected static final DateTime DATETIME = new DateTime();
	protected static final int YEAR = DATETIME.getYear();

	protected final SimpleDateFormat sdfYMD = new SimpleDateFormat("yyyy-MM-dd");

	protected static final Long TRANS_MSG_ID = new Long("9999999999999");

	// Key Financial Elements
	protected final String APTC = "APTC";
	protected final String TIRA = "TIRA";
	protected final String TPA = "TPA";
	protected final String CSR = "CSR";
	// Carry over elements
	protected final String RA = "RA";
	// InsurancePlanVariantComponent (CSR Variant)
	protected final String IPVC = "IPVC";
	// Non-policyPremium elements
	protected final String AMRC = "AMRC";
	protected final String PA1 = "PA1";
	protected final String AIAO = "AIAO";
	protected final String SHOP = "SHOP";
	// Pro-rated elements
	protected final String PRO_APTC = "PRO_APTC";
	protected final String PRO_CSR = "PRO_CSR";
	protected final String PRO_MPA = "PRO_MPA";
	protected final String PRO_TIRA = "PRO_TIRA";

	protected final DateTime JAN_1 = new DateTime(YEAR, 1, 1, 0, 0);
	protected final DateTime JAN_15 = new DateTime(YEAR, 1, 15, 0, 0);
	protected final DateTime JAN_31 = new DateTime(YEAR, 1, 31, 0, 0);
	protected final DateTime FEB_1 = new DateTime(YEAR, 2, 1, 0, 0);
	protected final DateTime FEB_MAX = new DateTime(YEAR, 2, FEB_1.dayOfMonth().getMaximumValue(), 0, 0);
	protected final DateTime MAR_1 = new DateTime(YEAR, 3, 1, 0, 0);
	protected final DateTime MAR_14 = new DateTime(YEAR, 3, 14, 0, 0);
	protected final DateTime MAR_15 = new DateTime(YEAR, 3, 15, 0, 0);
	protected final DateTime MAR_31 = new DateTime(YEAR, 3, 31, 0, 0);
	protected final DateTime APR_1 = new DateTime(YEAR, 4, 1, 0, 0);
	protected final DateTime APR_15 = new DateTime(YEAR, 4, 15, 0, 0);
	protected final DateTime APR_30 = new DateTime(YEAR, 4, 30, 0, 0);
	protected final DateTime MAY_1 = new DateTime(YEAR, 5, 1, 0, 0);
	protected final DateTime MAY_31 = new DateTime(YEAR, 5, 31, 0, 0);
	protected final DateTime JUN_1 = new DateTime(YEAR, 6, 1, 0, 0);
	protected final DateTime JUN_30 = new DateTime(YEAR, 6, 30, 0, 0);
	protected final DateTime DEC_31 = new DateTime(YEAR, 12, 31, 0, 0);

	protected BenefitEnrollmentMaintenanceDTO makeBemDTO() {

		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		bemDTO.setBem(new BenefitEnrollmentMaintenanceType());
		return bemDTO;
	}

	protected MemberType makeSubscriber() {

		MemberType subscriber = new MemberType();
		subscriber.setMemberInformation(new MemberRelatedInfoType());
		subscriber.getMemberInformation().setSubscriberIndicator(BooleanIndicatorSimpleType.Y);
		return subscriber;
	}

	protected MemberType makeSubscriber(String id, String variantId) {

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
		if (id != null) {
			subscriber.setMemberNameInformation(new MemberNameInfoType());
			subscriber.getMemberNameInformation().setMemberName(new IndividualNameType());
			subscriber.getMemberNameInformation().getMemberName().setFirstName(id + "-FIRSTNM");
			subscriber.getMemberNameInformation().getMemberName().setLastName(id + "-LASTNM");
		}
		return subscriber;
	}


	protected MemberType makeMemberType(Long memId, String name, boolean isSubscriber) {


		MemberType member = new MemberType();
		member.setMemberInformation(new MemberRelatedInfoType());
		if (isSubscriber) {
			member.getMemberInformation().setSubscriberIndicator(BooleanIndicatorSimpleType.Y);
		} else {
			member.getMemberInformation().setSubscriberIndicator(BooleanIndicatorSimpleType.N);
		}
		member.setMemberAdditionalIdentifier(new MemberAdditionalIdentifierType());
		member.getMemberAdditionalIdentifier().setExchangeAssignedMemberID(makeExchangeAssignedMemberID(memId, name));

		return member;	
	}

	protected static String makeExchangeAssignedMemberID(Long memId, String name) {

		return name + "-" + memId.toString();

	}

	protected MemberType makeMemberType(Long memId, String name, boolean isSubscriber, String subscriberId) {

		MemberType memType = new MemberType();

		MemberRelatedInfoType memRelInfo = new MemberRelatedInfoType();
		if (isSubscriber) {
			memRelInfo.setSubscriberIndicator(BooleanIndicatorSimpleType.Y);
		} else {
			memRelInfo.setSubscriberIndicator(BooleanIndicatorSimpleType.N);
		}

		memType.setMemberInformation(memRelInfo);
		memType.setSubscriberID(subscriberId);
		MemberNameInfoType memNameInfo = new MemberNameInfoType();
		IndividualNameType indNameType = new IndividualNameType();

		String lastNm = name + "-" + memId + " LAST";
		lastNm = lastNm.length() > 60 ? lastNm.substring(0, 60): lastNm;
		indNameType.setLastName(lastNm);

		String firstNm = name + "-" + memId + " FIRST";
		firstNm = firstNm.length() > 35 ? firstNm.substring(0, 35): firstNm;
		indNameType.setFirstName(firstNm);

		memNameInfo.setMemberName(indNameType);
		memType.setMemberNameInformation(memNameInfo);

		MemberAdditionalIdentifierType memAddlInfoType = new MemberAdditionalIdentifierType();
		memAddlInfoType.setExchangeAssignedMemberID(makeExchangeAssignedMemberID(memId, name));
		memType.setMemberAdditionalIdentifier(memAddlInfoType);

		return memType; 

	}

	public  HealthCoverageDatesType makeHealthCoverageDatesType(DateTime hcBBD, DateTime hcBED) {

		HealthCoverageDatesType hcDates = new HealthCoverageDatesType();
		hcDates.setBenefitBeginDate(EpsDateUtils.getXMLGregorianCalendar(hcBBD));
		hcDates.setBenefitEndDate(EpsDateUtils.getXMLGregorianCalendar(hcBED));
		return hcDates;
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
		} else if (type.equals(PRO_APTC)) {
			ait.setProratedAppliedAPTCAmount(amt); 
		} else if (type.equals(PRO_TIRA)) {
			ait.setProratedIndividualResponsibleAmount(amt);
		} else if (type.equals(PRO_MPA)) {
			ait.setProratedMonthlyPremiumAmount(amt);
		} else if (type.equals(PRO_CSR)) {
			ait.setProratedCSRAmount(amt);
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

	protected PolicyPremiumPO makePolicyPremiumPO(DateTime esd, DateTime eed, BigDecimal aptc, BigDecimal tira, BigDecimal tpa) {

		return makePolicyPremiumPO(esd, eed, aptc, tira, tpa, null, null, null, null, null, null, null);
	}

	protected PolicyPremiumPO makePolicyPremiumPO(DateTime esd, DateTime eed, BigDecimal aptc, BigDecimal tira, BigDecimal tpa,
			BigDecimal csr) {

		return makePolicyPremiumPO(esd, eed, aptc, tira, tpa, csr, null, null, null, null, null, null);
	}

	protected PolicyPremiumPO makePolicyPremiumPO(DateTime esd, DateTime eed, BigDecimal aptc, BigDecimal tira, BigDecimal tpa,
			BigDecimal csr, BigDecimal opa1, BigDecimal opa2) {

		return makePolicyPremiumPO(esd, eed, aptc, tira, tpa, csr, null, null, null, null, null, null);
	}

	protected PolicyPremiumPO makePolicyPremiumPO(DateTime esd, DateTime eed, BigDecimal aptc, BigDecimal tira, BigDecimal tpa, 
			BigDecimal csr, BigDecimal opa1, BigDecimal opa2, BigDecimal pa2, BigDecimal pa3, BigDecimal tera, String ra) {

		PolicyPremiumPO po = new PolicyPremiumPO();
		po.setEffectiveStartDate(new DateTime(esd));
		if (eed != null) {
			po.setEffectiveEndDate(new DateTime(eed));
		}
		//Key Financial elements
		po.setAptcAmount(aptc);
		po.setIndividualResponsibleAmount(tira);
		po.setTotalPremiumAmount(tpa);
		po.setCsrAmount(csr);
		//Carry-over elements
		po.setExchangeRateArea(ra);
		return po;		
	}


	protected void assertPolicyPremiumPO(int i, String aitType, DateTime expectedESD, DateTime expectedEED, BigDecimal expectedAmt, PolicyPremiumPO actualPO) {

		String msg = "EPS PolicyPremium Record " + i + ": " + aitType + " - ";
		assertEquals(msg + "EffectiveStartDate (ESD)", expectedESD, actualPO.getEffectiveStartDate());
		assertEquals(msg + "EffectiveEndDate (EED)", expectedEED, actualPO.getEffectiveEndDate());
		if (aitType.equals(APTC)) {
			assertEquals(msg + "AptcAmount", expectedAmt, actualPO.getAptcAmount());
		}
		if (aitType.equals(TIRA)) {
			assertEquals(msg + "IndividualResponsibleAmount", expectedAmt, actualPO.getIndividualResponsibleAmount());
		} 
		if (aitType.equals(TPA)) {
			assertEquals(msg + "TotalPremiumAmount", expectedAmt, actualPO.getTotalPremiumAmount());
		} 
		if (aitType.equals(CSR)) {
			assertEquals(msg + "CSRAmount", expectedAmt, actualPO.getCsrAmount());
		} 
		if (aitType.equals(PRO_APTC)) {
			assertEquals(msg + "ProratedAPTCAmount", expectedAmt, actualPO.getProratedAptcAmount());
		}
		if (aitType.equals(PRO_TIRA)) {
			assertEquals(msg + "ProratedInddResponsibleAmount", expectedAmt, actualPO.getProratedInddResponsibleAmount());
		} 
		if (aitType.equals(PRO_MPA)) {
			assertEquals(msg + "ProratedPremiumAmount", expectedAmt, actualPO.getProratedPremiumAmount());
		} 
		if (aitType.equals(PRO_CSR)) {
			assertEquals(msg + "ProratedCsrAmount", expectedAmt, actualPO.getProratedCsrAmount());
		} 
	}

	protected void assertPolicyPremiumPO(int i, String aitType, DateTime expectedESD, DateTime expectedEED, String expectedTxt, PolicyPremiumPO actualPO) {

		String msg = "EPS PolicyPremium Record " + i + ": " + aitType + " - ";
		assertEquals(msg + "EffectiveStartDate (ESD)", expectedESD, actualPO.getEffectiveStartDate());
		assertEquals(msg + "EffectiveEndDate (EED)", expectedEED, actualPO.getEffectiveEndDate());
		if (aitType.equals(RA)) {
			assertEquals(msg + "ExchangeRateArea", expectedTxt, actualPO.getExchangeRateArea());
		}
		if (aitType.equals(IPVC)) {
			assertEquals(msg + "CSR Variant", expectedTxt, actualPO.getInsrncPlanVariantCmptTypeCd());
		}
	}

}
