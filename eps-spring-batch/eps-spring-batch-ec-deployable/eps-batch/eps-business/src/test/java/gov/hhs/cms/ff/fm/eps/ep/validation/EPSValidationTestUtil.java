/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.validation;

import gov.cms.dsh.bem.AdditionalInfoType;
import gov.cms.dsh.bem.BenefitEnrollmentMaintenanceType;
import gov.cms.dsh.bem.BooleanIndicatorSimpleType;
import gov.cms.dsh.bem.FileInformationType;
import gov.cms.dsh.bem.HealthCoverageDatesType;
import gov.cms.dsh.bem.HealthCoverageInfoType;
import gov.cms.dsh.bem.HealthCoveragePolicyNumberType;
import gov.cms.dsh.bem.HealthCoverageType;
import gov.cms.dsh.bem.IndividualNameType;
import gov.cms.dsh.bem.InsuranceLineCodeSimpleType;
import gov.cms.dsh.bem.IssuerType;
import gov.cms.dsh.bem.MemberNameInfoType;
import gov.cms.dsh.bem.MemberRelatedDatesType;
import gov.cms.dsh.bem.MemberRelatedInfoType;
import gov.cms.dsh.bem.MemberType;
import gov.cms.dsh.bem.PolicyInfoType;
import gov.cms.dsh.bem.TransactionInformationType;
import gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentMaintenanceDTO;
import gov.hhs.cms.ff.fm.eps.ep.enums.ExchangeType;
import gov.hhs.cms.ff.fm.eps.ep.enums.PolicyStatus;
import gov.hhs.cms.ff.fm.eps.ep.util.EpsDateUtils;

import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

/**
 * Util class to create test BEM data for validation tests
 * @author girish.padmanabhan
 *
 */
public class EPSValidationTestUtil {
	

	public static final DateTime DATETIME = new DateTime();
	
	public static final DateTime csrEffStart = new DateTime(DATETIME.getYear(), 2, 28, 0, 0);

	/*
	 * Util to create BEM for INITIAL transaction 
	 * */
	public static BenefitEnrollmentMaintenanceDTO createMockBEM() {
		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		
		bem.setPolicyInfo(makePolicyInfoType("MGPI", DATETIME, null, PolicyStatus.INITIAL_1, "ExchangePolicyId"));
		bemDTO.setBemXml("<BenefitEnrollmentMaintenance>BEMXml</BenefitEnrollmentMaintenance>");

		FileInformationType fileInformation = new FileInformationType();
		fileInformation.setGroupReceiverID(StringUtils.EMPTY);
		bemDTO.setFileInformation(fileInformation);
		bemDTO.setFileInfoXml("<FileInformation>FileInfoXML</FileInformation>");
		
		MemberType member = new MemberType();
		MemberRelatedInfoType memeberInfo = new MemberRelatedInfoType();
		memeberInfo.setSubscriberIndicator(BooleanIndicatorSimpleType.Y);
		member.setMemberInformation(memeberInfo);
		
		bem.getMember().add(member);
		bemDTO.setBem(bem);
		bemDTO.setExchangeTypeCd(ExchangeType.FFM.getValue());
		
		return bemDTO;
	}

	/*
	 * Util to create BEM for policy match 
	 * */
	public static  BenefitEnrollmentMaintenanceDTO createBEMForPolicyMatch() {
		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		
		//Set PolicyInfo
		bem.setPolicyInfo(makePolicyInfoType("MGPI", DATETIME, DATETIME, PolicyStatus.EFFECTUATED_2, "EXCHANGEPOLICYID"));
		
		// Add subscriber member
		MemberType member = new MemberType();
		member.setMemberInformation(makeSubscriberMemberRelatedInfoType());
		member.setSubscriberID("8451209001");
		member.setMemberRelatedDates(makeMemberRelatedDatesType());
		member.getHealthCoverage().add(makeHealthCoverageType(8451209001L));
		bem.getMember().add(member);
		
		TransactionInformationType transInfo = new TransactionInformationType();
		bem.setTransactionInformation(transInfo);

		bemDTO.setBem(bem);
		
		FileInformationType fileInfo = makeFileInformationType(101L);
		bemDTO.setFileInformation(fileInfo);
		
		return bemDTO;
	}
	
	/*
	 * Util to create BEM for policy match 
	 * */
	public static  BenefitEnrollmentMaintenanceDTO createBEMForPolicyMatchFFMValidatorTestFailurePolicyStartDt() {
		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		
		// Add subscriber member
		MemberType member = new MemberType();
		member.setMemberInformation(makeSubscriberMemberRelatedInfoType());
		member.setSubscriberID("8451209001");
		member.setMemberRelatedDates(makeMemberRelatedDatesType("2014-01-01T00:00:01"));
		member.getHealthCoverage().add(makeHealthCoverageType(8451209001L));
		bem.getMember().add(member);
		bemDTO.setBem(bem);
		
		FileInformationType fileInfo = makeFileInformationType(101L);
		bemDTO.setFileInformation(fileInfo);
		
		return bemDTO;
	}
	
	
	/*
	 * Util to create BEM for INITIAL transaction 
	 * */
	public static BenefitEnrollmentMaintenanceType createBEMForTransactionProcessingOrder() {
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();

		MemberType member = new MemberType();
		MemberRelatedInfoType memeberInfo = new MemberRelatedInfoType();
		memeberInfo.setSubscriberIndicator(BooleanIndicatorSimpleType.Y);
		member.setMemberInformation(memeberInfo);
		member.setMemberRelatedDates(makeMemberRelatedDatesType());
		member.getHealthCoverage().add(makeHealthCoverageType(Long.valueOf("1111")));
		bem.getMember().add(member);
		
		return bem;
	}
	
	
	/*
	 * Util to create BEM for FFM Validator success flow 
	 * */
	public static BenefitEnrollmentMaintenanceDTO createCurrentBEMForFFMValidatorTest() {
		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		

		MemberType member = new MemberType();
		member.setMemberRelatedDates(makeMemberRelatedDatesType("2015-01-01T00:00:01"));
		
		bem.getMember().add(member);
		
		bem.setIssuer(makeIssuerType("GRSID"));
		
		bemDTO.setBem(bem);
		bemDTO.setMarketplaceGroupPolicyId("MGPI");
		
		return bemDTO;
	}
	
	
	public static MemberRelatedInfoType makeSubscriberMemberRelatedInfoType() {

		MemberRelatedInfoType memRelInfoType = new MemberRelatedInfoType();
		memRelInfoType.setSubscriberIndicator(BooleanIndicatorSimpleType.Y);
		return memRelInfoType;
	}
	
	public static MemberNameInfoType makeMemberNameInfoType(Long memId, String name) {

		MemberNameInfoType memNameInfoType = new MemberNameInfoType();
		memNameInfoType.setMemberName(makeIndividualNameType(memId, name));
		return memNameInfoType;
	}
	
	public static IndividualNameType makeIndividualNameType(Long memId, String name) {

		IndividualNameType indNameType = new IndividualNameType();

		indNameType.setLastName(name + "-Lst " + memId);
		indNameType.setFirstName(name + "-Fir " + memId);
		indNameType.setMiddleName(name + "-Mid " + memId);

		String prefix = (name + "-Pre " + memId);
		indNameType.setNamePrefix(prefix.substring(0,9));
		String suffix = (name + "-Suf " + memId);
		indNameType.setNameSuffix(suffix.substring(0,9));
		indNameType.setSocialSecurityNumber("885047635");

		return indNameType;
	}
	
	public static FileInformationType makeFileInformationType(Long id) {

		FileInformationType fileInfoType = new FileInformationType();

		fileInfoType.setGroupSenderID(id + " GRPSNDRID");
		fileInfoType.setGroupReceiverID(id + " GRPRCVRID");
		fileInfoType.setGroupControlNumber(id.toString());
		fileInfoType.setVersionNumber("23");

		return fileInfoType;

	}
	
	public static IssuerType makeIssuerType(String strHiosId) {

		IssuerType issType = new IssuerType();

		issType.setName("ISSUERNAME" + strHiosId);
		// Length = 10
		String hiosId = strHiosId.length() > 10 ?  strHiosId.substring(0, 10) : strHiosId;
		issType.setHIOSID(hiosId);

		return issType;
	}
	
	public static MemberRelatedDatesType makeMemberRelatedDatesType() {

		MemberRelatedDatesType memRelDatesType = new MemberRelatedDatesType();
		
		memRelDatesType.setEligibilityBeginDate(
				EpsDateUtils.getXMLGregorianCalendar(new DateTime(2015, 01, 01, 00, 00, 01)));
		memRelDatesType.setEligibilityEndDate(
				EpsDateUtils.getXMLGregorianCalendar(new DateTime(2015, 02, 01, 00, 00, 01)));
		return memRelDatesType;

	}
	
	public static MemberRelatedDatesType makeMemberRelatedDatesType(String date) {

		MemberRelatedDatesType memRelDatesType = new MemberRelatedDatesType();
		
		memRelDatesType.setEligibilityBeginDate(
				EpsDateUtils.getXMLGregorianCalendar(date));
		
		return memRelDatesType;

	}
	
	public static HealthCoveragePolicyNumberType makeHealthCoveragePolicyNumberType(Long memId) {
		HealthCoveragePolicyNumberType hcPolNumType = new HealthCoveragePolicyNumberType();
		hcPolNumType.setContractCode("CONTRACTCD-" + memId);

		return hcPolNumType;
	}
	
	public static HealthCoverageType makeHealthCoverageType(Long memId) {
		HealthCoverageType healthCovType = new HealthCoverageType();
		healthCovType.setHealthCoverageInformation(makeHealthCoverageInfoType());	
		healthCovType.setHealthCoveragePolicyNumber(makeHealthCoveragePolicyNumberType(memId));
		healthCovType.setHealthCoverageDates(new HealthCoverageDatesType());
		return healthCovType;
	}
	
	public static HealthCoverageInfoType makeHealthCoverageInfoType() {
		HealthCoverageInfoType healthCovInfoType = new HealthCoverageInfoType();
		healthCovInfoType.setInsuranceLineCode(InsuranceLineCodeSimpleType.HLT);

		return healthCovInfoType;
	}
	
	public static PolicyInfoType makePolicyInfoType(String mgpi, DateTime psd, DateTime ped, PolicyStatus policyStatus, String gpn) {

		PolicyInfoType policyInfo = new PolicyInfoType();
		policyInfo.setMarketplaceGroupPolicyIdentifier(mgpi);
		policyInfo.setPolicyStartDate(EpsDateUtils.getXMLGregorianCalendar(psd));
		policyInfo.setPolicyEndDate(EpsDateUtils.getXMLGregorianCalendar(ped));
		policyInfo.setPolicyStatus(policyStatus.getValue());
		policyInfo.setGroupPolicyNumber(gpn);
		return policyInfo;
	}

	public static AdditionalInfoType makeAdditionalInfoType(DateTime esd, DateTime eed, BigDecimal tpa) {

		AdditionalInfoType ait = new AdditionalInfoType();
		
		ait.setEffectiveStartDate(EpsDateUtils.getXMLGregorianCalendar(esd));
		ait.setEffectiveEndDate(EpsDateUtils.getXMLGregorianCalendar(eed));
		ait.setAPTCAmount(new BigDecimal("11.11"));
		ait.setCSRAmount(new BigDecimal("22.22"));
		ait.setTotalIndividualResponsibilityAmount(new BigDecimal("33.33"));
		ait.setTotalPremiumAmount(tpa);
		ait.setRatingArea("RA");

		return ait;
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

}
