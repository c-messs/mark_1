package gov.hhs.cms.ff.fm.eps.ep.sbm.validation.impl;

import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.ERROR_DESC_INCORRECT_VALUE;
import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.ERROR_INFO_EXPECTED_VALUE_TXT;
import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.LANGUAGE_QUALIFIER_LD;
import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.LANGUAGE_QUALIFIER_LE;
import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.Y;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.cms.dsh.sbmi.FileInformationType;
import gov.cms.dsh.sbmi.FileInformationType.IssuerFileInformation;
import gov.cms.dsh.sbmi.PolicyMemberType;
import gov.cms.dsh.sbmi.PolicyMemberType.MemberDates;
import gov.cms.dsh.sbmi.PolicyType;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMErrorWarningCode;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMGenderCodeEnum;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMTobaccoUseCodeEnum;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMCache;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMPolicyDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SbmErrWarningLogDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SBMDataService;
import gov.hhs.cms.ff.fm.eps.ep.sbm.validation.SbmBusinessValidator;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;

/**
 * @author girish.padmanabhan
 * 
 *
 */
public class SbmBusinessValidatorImpl implements SbmBusinessValidator {
	
	private static final Logger LOG = LoggerFactory.getLogger(SbmBusinessValidatorImpl.class);
	
	private SBMDataService sbmDataService;
	private List<String> sbmBusinessRules;

	@Override
	public List<SbmErrWarningLogDTO> validatePolicy(SBMPolicyDTO policyDto) {

		PolicyType policy = policyDto.getPolicy();
		FileInformationType fileInfo = policyDto.getFileInfo();
		
		String stateCd = SbmValidationUtil.getStateCdFromQhpId(policy.getQHPId());
		
		if(StringUtils.isNotBlank(stateCd)) {
			this.sbmBusinessRules = SBMCache.getBusinessRules(stateCd);
		}
		
		List<SbmErrWarningLogDTO> policyErrors = new ArrayList<SbmErrWarningLogDTO>();
		
		//QHP Id and issuer id validations
		List<SbmErrWarningLogDTO> qhpIdErrors = validateQhpId(policy, fileInfo.getIssuerFileInformation());
		if(CollectionUtils.isNotEmpty(qhpIdErrors)) {
			policyErrors.addAll(qhpIdErrors);
		}
		
		//Field length validations for IssuerAssignedPolicyID, IssuerAssignedSubscriberID
		List<SbmErrWarningLogDTO> fieldLengthWarnings = validateIssuerFieldsLength(policy);
		policyErrors.addAll(fieldLengthWarnings);
		
		//Coverage Year and Policy start and end date Validations
		List<SbmErrWarningLogDTO> policyDateErrors = validatePolicyDates(policy, fileInfo.getCoverageYear());
		policyErrors.addAll(policyDateErrors);
		
		return policyErrors;
	}

	/*
	 * Validate QHP Id
	 */
	private List<SbmErrWarningLogDTO> validateQhpId(PolicyType policy, IssuerFileInformation issuerFileInformation) {

		List<SbmErrWarningLogDTO> qhpIdErrors = null;
		
		String qhpId = policy.getQHPId();
		
		if(StringUtils.isNotBlank(qhpId)) {
			
			qhpIdErrors = new ArrayList<SbmErrWarningLogDTO>();
			
			LocalDate policyStartDate = DateTimeUtil.getLocalDateFromXmlGC(policy.getPolicyStartDate());
			
			String planYear = String.valueOf(policyStartDate.getYear());
			
			//FR-FM-PP-SBMI-196: R034 - QHPID validity with SBM
			if(sbmBusinessRules.contains("R034")) {
				
				boolean qhpIdValidForSbm = false;
				
				if(SBMCache.doesQhpExistForPlanYear(qhpId, planYear)) {
					qhpIdValidForSbm = true;
				}
				
				//Verify QHP Id Exists in eps Insurance Plan ref table for the  policy start date year
				//[FR-FM-PP-SBMI-195]
				if (!qhpIdValidForSbm) {
					boolean qhpIdExistsForSbm = sbmDataService.checkQhpIdExistsForPolicyYear(qhpId, planYear);
					
					if(qhpIdExistsForSbm) {
						
						//Add to SBM cache 
						SBMCache.addToQhpIdMap(qhpId, planYear);
						
						LOG.info("Valid Qhp Id: " + qhpId);
						
					} else {
						//create Error ER-024: incorrect value provided
						qhpIdErrors.add(SbmValidationUtil.createErrorWarningLogDTO(
								"QHPId", SBMErrorWarningCode.ER_024.getCode(), ERROR_DESC_INCORRECT_VALUE));
						
						LOG.info("InValid Qhp Id for plan year {} : {} ", qhpId, planYear);
					}
				}
			}
			
			//Validate QHP id against issuer id [FR-FM-PP-SBMI-198] if the IssuerID is present on the XPR [FR-FM-PP-SBMI-197]
			
			//FR-FM-PP-SBMI-198: R036 - QHPID validity check with Issuer ID
			if(sbmBusinessRules.contains("R036")) {
				
				if (issuerFileInformation != null ) {
					
					String issuerId = issuerFileInformation.getIssuerId();
					
					if (StringUtils.isNotEmpty(issuerId)) {
						
						String issuerIdFromQhpId = SbmValidationUtil.getIssuerIdFromQhpId(qhpId);
						
						if (!issuerId.equalsIgnoreCase(SbmValidationUtil.getIssuerIdFromQhpId(issuerIdFromQhpId))) {
		
							// create Error ER-025: incorrect value provided
							LOG.info("Incorrect Issuer Id. Expected value: [First 5 characters of the QHPId] {} : {} ",
									issuerId, issuerIdFromQhpId);
							
							qhpIdErrors.add(SbmValidationUtil.createErrorWarningLogDTO("IssuerId", SBMErrorWarningCode.ER_025.getCode(),
									ERROR_DESC_INCORRECT_VALUE, ERROR_INFO_EXPECTED_VALUE_TXT.concat(issuerIdFromQhpId)));
						}
					}
				}
			}
		}
		return qhpIdErrors;
	}
	
	/*
	 * FR-FM-PP-SBMI-201
	 */
	private List<SbmErrWarningLogDTO> validateIssuerFieldsLength(PolicyType policy) {
		
		List<SbmErrWarningLogDTO> fieldLengthWarnings = new ArrayList<SbmErrWarningLogDTO>();
		
		//Validate Issuer Assigned Policy Id Length [FR-FM-PP-SBMI-201, FR-FM-PP-SBMI-202, FR-FM-PP-SBMI-203]
		//R038 - Issuer Assigned Policy ID database character length validation
		if(sbmBusinessRules.contains("R038")) {
			String truncatedIssuerPolicyId = SbmValidationUtil.truncateField(
					"IssuerAssignedPolicyId", policy.getIssuerAssignedPolicyId(), 50, fieldLengthWarnings);
			if(truncatedIssuerPolicyId != null) {
				policy.setIssuerAssignedPolicyId(truncatedIssuerPolicyId);
			}
		}
		
		//Validate Issuer Assigned Subscriber Id Length [FR-FM-PP-SBMI-201, FR-FM-PP-SBMI-204, FR-FM-PP-SBMI-205]
		//R039 - Issuer Assigned Subscriber ID database character length truncation
		if(sbmBusinessRules.contains("R039")) {
			String truncatedIssuerSubscriberId = SbmValidationUtil.truncateField(
					"IssuerAssignedSubscriberId", policy.getIssuerAssignedSubscriberId(), 50, fieldLengthWarnings);
			if(truncatedIssuerSubscriberId != null) {
				policy.setIssuerAssignedSubscriberId(truncatedIssuerSubscriberId);
			}
		}
		return fieldLengthWarnings;
	}

	/*
	 * FR-FM-PP-SBMI-206, FR-FM-PP-SBMI-207,
	 */
	private List<SbmErrWarningLogDTO> validatePolicyDates(PolicyType policy, int coverageYear) {
		
		List<SbmErrWarningLogDTO> policyDateErrors = new ArrayList<SbmErrWarningLogDTO>();

		LocalDate policyStartDate = DateTimeUtil.getLocalDateFromXmlGC(policy.getPolicyStartDate());
		
		//FR-FM-PP-SBMI-207: R040 - Policy Start Date to Coverage Year validation
		if(sbmBusinessRules.contains("R040")) {
			
			if(policyStartDate.getYear() > coverageYear) {
				
				//create error ER-026: incorrect value provided
				policyDateErrors.add(SbmValidationUtil.createErrorWarningLogDTO(
						"PolicyStartDate", SBMErrorWarningCode.ER_026.getCode(), 
						ERROR_DESC_INCORRECT_VALUE, ERROR_INFO_EXPECTED_VALUE_TXT.concat(String.valueOf(coverageYear))));
				
				LOG.info("Policy Start year exceeds CoverageYear {} : {} ", policyStartDate.getYear(), coverageYear);
			}
		}
		
		String effectuationInd = policy.getEffectuationIndicator();
		
		if(effectuationInd.equalsIgnoreCase(Y)) {
			//FR-FM-PP-SBMI-516
			//R043 - Policy End Date to Policy Start Date validation when Effectuation indicator is "Y"
			if(sbmBusinessRules.contains("R043")) {
				LocalDate policyEndDate = DateTimeUtil.getLocalDateFromXmlGC(policy.getPolicyEndDate());
				
				if(policyEndDate.isBefore(policyStartDate)) {
					
					//create error ER-062: incorrect value provided
					policyDateErrors.add(SbmValidationUtil.createErrorWarningLogDTO(
							"PolicyEndDate", SBMErrorWarningCode.ER_062.getCode(), 
							ERROR_DESC_INCORRECT_VALUE, ERROR_INFO_EXPECTED_VALUE_TXT.concat(String.valueOf(policyStartDate))));
					
					LOG.info("Invalid End Date on an Effectuated Policy {} : {} ", policyStartDate, policyEndDate);				
				}
			}
			
		} else 	if(effectuationInd.equalsIgnoreCase("N")) {
			//FR-FM-PP-SBMI-209, FR-FM-PP-SBMI-210
			//R042 - Policy End Date to Policy Start Date validation when Effectuation Indicator is "N"
			if(sbmBusinessRules.contains("R042")) {
				LocalDate policyEndDate = DateTimeUtil.getLocalDateFromXmlGC(policy.getPolicyEndDate());
				
				if(policyEndDate.isAfter(policyStartDate)) {
					
					//create error ER-027: incorrect value provided
					policyDateErrors.add(SbmValidationUtil.createErrorWarningLogDTO(
							"PolicyEndDate", SBMErrorWarningCode.ER_027.getCode(), 
							ERROR_DESC_INCORRECT_VALUE, ERROR_INFO_EXPECTED_VALUE_TXT.concat(String.valueOf(policyStartDate))));
					
					LOG.info("Invalid End Date on Canceled Policy {} : {} ", policyStartDate, policyEndDate);				
				}
			}
		} 
		
		return policyDateErrors;
	}

	@Override
	public List<SbmErrWarningLogDTO> validatePolicyMembers(PolicyType policy) {

		List<SbmErrWarningLogDTO> policyMemberErrors = new ArrayList<SbmErrWarningLogDTO>();
		
		//Number of subscribers check
		List<SbmErrWarningLogDTO> subscriberCountErrors = validateNumSubscribers(policy);
		policyMemberErrors.addAll(subscriberCountErrors);
		
		List<PolicyMemberType> policyMembers = policy.getMemberInformation();
		
		policyMembers.forEach(member -> { 
			
			//Field length validations
			List<SbmErrWarningLogDTO> fieldLengthWarnings = validateMemberLevelFieldLength(member);
			policyMemberErrors.addAll(fieldLengthWarnings);
			
			//Field length validations
			List<SbmErrWarningLogDTO> optionalFieldValueWarnings = validateOptionalFieldValues(member);
			policyMemberErrors.addAll(optionalFieldValueWarnings);
			
			//Member dates validations
			List<SbmErrWarningLogDTO> memberDatesErrors = validatememberDates(policy, member);
			policyMemberErrors.addAll(memberDatesErrors);
		
		});
		
		return policyMemberErrors;
	}

	/*
	 * Validate number of subscribers
	 */
	private List<SbmErrWarningLogDTO> validateNumSubscribers(PolicyType policy) {
		
		List<SbmErrWarningLogDTO> policyMemberErrors = new ArrayList<SbmErrWarningLogDTO>();
		
		int subscriberCount = 0;
		List<PolicyMemberType> policyMembers = policy.getMemberInformation();
		
		for(PolicyMemberType member : policyMembers) { 
			if(member.getSubscriberIndicator().equalsIgnoreCase(Y)) {
				subscriberCount++;
			}
			
			//FR-FM-PP-SBMI-509, FR-FM-PP-SBMI-510 : R045 - Multiple Subscriber Indicator validation
			if(sbmBusinessRules.contains("R045")) {
				if(subscriberCount > 1) {
					//create error ER-061 
					policyMemberErrors.add(SbmValidationUtil.createErrorWarningLogDTO("Subscriber", SBMErrorWarningCode.ER_061.getCode()));
					LOG.info("Multiple subscribers on policy ");				
					
					break;
				}
			}
		}
		
		//FR-FM-PP-SBMI-508: R044 - No Subscriber Indicator validation
		if(sbmBusinessRules.contains("R044")) {

			if(subscriberCount < 1) {
				//create error ER-060 
				policyMemberErrors.add(SbmValidationUtil.createErrorWarningLogDTO("Subscriber", SBMErrorWarningCode.ER_060.getCode()));
				LOG.info("No subscriber on policy");	
			} 
		}
		return policyMemberErrors;
	}
	
	/*
	 * FR-FM-PP-SBMI-212, FR-FM-PP-SBMI-213
	 */
	private List<SbmErrWarningLogDTO> validateMemberLevelFieldLength(PolicyMemberType member) {
		
		List<SbmErrWarningLogDTO> fieldLengthWarnings = new ArrayList<SbmErrWarningLogDTO>();
		
		//List<PolicyMemberType> policyMembers = policy.getMemberInformation();
		
		//policyMembers.forEach(member -> { 
		
		//FR-FM-PP-SBMI-214: R048 - Issuer Assigned Member Id database character length truncation warning
		if(sbmBusinessRules.contains("R048")) {
			
			String truncatedIssuerMemberId = SbmValidationUtil.truncateField(
					"IssuerAssignedMemberId", member.getIssuerAssignedMemberId(), 50, fieldLengthWarnings);
			if(truncatedIssuerMemberId != null) {
				member.setIssuerAssignedMemberId(truncatedIssuerMemberId);
			}
		}
		
		//FR-FM-PP-SBMI-215: R049 - Name Prefix database character length truncation warning
		if(sbmBusinessRules.contains("R049")) {
			
			String truncatedNamePrefix = SbmValidationUtil.truncateField(
					"NamePrefix", member.getNamePrefix(), 10, fieldLengthWarnings);
			if(truncatedNamePrefix != null) {
				member.setNamePrefix(truncatedNamePrefix);
			}
		}

		//FR-FM-PP-SBMI-216: R065 - Member First Name database character length truncation warning
		if(sbmBusinessRules.contains("R065")) {

			String truncatedFirstName = SbmValidationUtil.truncateField(
					"MemberFirstName", member.getMemberFirstName(), 35, fieldLengthWarnings);
			if(truncatedFirstName != null) {
				member.setMemberFirstName(truncatedFirstName);
			}
		}
			
		//FR-FM-PP-SBMI-217: R050 - Member Middle Name database character length truncation warning
		if(sbmBusinessRules.contains("R050")) {
			
			String truncatedMiddleName = SbmValidationUtil.truncateField(
					"MemberMiddleName", member.getMemberMiddleName(), 25, fieldLengthWarnings);
			if(truncatedMiddleName != null) {
				member.setMemberMiddleName(truncatedMiddleName);
			}
		}
			
		//FR-FM-PP-SBMI-218: R051 - Name Suffix database character length truncation warning
		if(sbmBusinessRules.contains("R051")) {
			
			String truncatedNameSuffix = SbmValidationUtil.truncateField(
					"NameSuffix", member.getNameSuffix(), 10, fieldLengthWarnings);
			if(truncatedNameSuffix != null) {
				member.setNameSuffix(truncatedNameSuffix);
			}
		}
		//});
		return fieldLengthWarnings;
	}
	
	/*
	 * FR-FM-PP-SBMI-220
	 */
	private List<SbmErrWarningLogDTO> validateOptionalFieldValues(PolicyMemberType member) {
		
		List<SbmErrWarningLogDTO> optionalFieldValueWarnings = new ArrayList<SbmErrWarningLogDTO>();
		
		sbmBusinessRules.forEach(businessRuleCd -> {
			
			switch(businessRuleCd.toUpperCase()) {
			
			case "R053":
				//FR-FM-PP-SBMI-221
				validateSsn(member, optionalFieldValueWarnings);
			break;
			
			case "R054":
				//FR-FM-PP-SBMI-222
				validateLanguageQualifierCode(member, optionalFieldValueWarnings);
				break;
				
			case "R055":
				//FR-FM-PP-SBMI-223
				validateLanguageCode(member, optionalFieldValueWarnings);
				break;
				
			case "R056":
				//FR-FM-PP-SBMI-224
				validateGenderCode(member, optionalFieldValueWarnings);
				break;
				
			case "R057":
				//FR-FM-PP-SBMI-225
				validateRaceEthnicityCode(member, optionalFieldValueWarnings);
				break;
				
			case "R058":
				//FR-FM-PP-SBMI-226
				validateTobaccoUseCode(member, optionalFieldValueWarnings);
				break;
				
			case "R059":
				//FR-FM-PP-SBMI-227
				validateNonCoveredSubscriberInd(member, optionalFieldValueWarnings);
				break;
				
			default:
				break;
				
			}
		});
		
		return optionalFieldValueWarnings;
	}

	/*
	 * Validate ssn format
	 */
	private void validateSsn(PolicyMemberType member, List<SbmErrWarningLogDTO> optionalFieldValueWarnings) {
		
		String ssn = member.getSocialSecurityNumber();
		if (StringUtils.isNotBlank(ssn) && !ssn.matches("[0-90]{9}")) {
			
			//create Warning WR-005: incorrect value provided
			optionalFieldValueWarnings.add(SbmValidationUtil.createErrorWarningLogDTO(
					"SocialSecurityNumber", SBMErrorWarningCode.WR_005.getCode(), "expected format: [0-9]{9}"));
			
			member.setSocialSecurityNumber(null);
			
			LOG.info("InValid format for SocialSecurityNumber. Should be [0-9]{9}");
		}
	}
	
	/*
	 * validate LanguageQualifierCode
	 */
	private void validateLanguageQualifierCode(PolicyMemberType member, List<SbmErrWarningLogDTO> optionalFieldValueWarnings) {
		
		String languageQualifierCd = member.getLanguageQualifierCode();
		if (StringUtils.isNotBlank(languageQualifierCd)) {
			
			if(!(languageQualifierCd.equalsIgnoreCase(LANGUAGE_QUALIFIER_LD) 
					|| languageQualifierCd.equalsIgnoreCase(LANGUAGE_QUALIFIER_LE))) {
				
				//create Warning WR-005: incorrect value provided
				optionalFieldValueWarnings.add(SbmValidationUtil.createErrorWarningLogDTO(
						"LanguageQualifierCode", SBMErrorWarningCode.WR_005.getCode(), 
						ERROR_INFO_EXPECTED_VALUE_TXT.concat("LD/LE")));
				
				member.setLanguageQualifierCode(null);
				
				LOG.info("InValid value for LanguageQualifierCode. Should be LD/LE");
			}
		}
	}
	
	/*
	 * Validate LanguageCode
	 */
	private void validateLanguageCode(PolicyMemberType member, List<SbmErrWarningLogDTO> optionalFieldValueWarnings) {

		String languageCd = member.getLanguageCode();
		if (StringUtils.isNotBlank(languageCd)) {
			
			List<String> languageCodes = SBMCache.getLanguageCodes();
			
			if(!(languageCodes.contains(languageCd))) {	
				//create Warning WR-005: incorrect value provided
				optionalFieldValueWarnings.add(SbmValidationUtil.createErrorWarningLogDTO(
						"LanguageCode", SBMErrorWarningCode.WR_005.getCode()));
				
				member.setLanguageCode(null);
				
				LOG.info("InValid value for LanguageCode. Should be one from LD/LE List");
			}
		}
	}
	
	/*
	 * Validate GenderCode
	 */
	private void validateGenderCode(PolicyMemberType member, List<SbmErrWarningLogDTO> optionalFieldValueWarnings) {

		String genderCd = member.getGenderCode();
		if (StringUtils.isNotBlank(genderCd)) {
			
			if(SBMGenderCodeEnum.getEnum(genderCd) == null) {
				
				//create Warning WR-005: incorrect value provided
				optionalFieldValueWarnings.add(SbmValidationUtil.createErrorWarningLogDTO(
						"GenderCode", SBMErrorWarningCode.WR_005.getCode(), 
						ERROR_INFO_EXPECTED_VALUE_TXT.concat("F, M, or U")));
				
				member.setGenderCode(null);
				
				LOG.info("InValid value for GenderCode. Should be F, M, or U");
			}
		}
	}
	
	/*
	 * Validate RaceEthnicityCode
	 */
	private void validateRaceEthnicityCode(PolicyMemberType member,
			List<SbmErrWarningLogDTO> optionalFieldValueWarnings) {

		String raceEthnicityCd = member.getRaceEthnicityCode();
		if (StringUtils.isNotBlank(raceEthnicityCd)) {
			
			List<String> raceEthnicityCodes = SBMCache.getRaceEthnicityCodes();
			
			if(!(raceEthnicityCodes.contains(raceEthnicityCd))) {	
				
				//create Warning WR-005: incorrect value provided
				optionalFieldValueWarnings.add(SbmValidationUtil.createErrorWarningLogDTO(
						"RaceEthnicity", SBMErrorWarningCode.WR_005.getCode()));
				
				member.setRaceEthnicityCode(null);
				
				LOG.info("InValid value for RaceEthnicityCode. Should be one from the allowed List");
			}
		}
	}

	/*
	 * Validate TobaccoUseCode
	 */
	private void validateTobaccoUseCode(PolicyMemberType member, List<SbmErrWarningLogDTO> optionalFieldValueWarnings) {

		String tobaccoUseCd = member.getTobaccoUseCode();
		if (StringUtils.isNotBlank(tobaccoUseCd)) {
			
			if(SBMTobaccoUseCodeEnum.getEnum(tobaccoUseCd) == null) {
				
				//create Warning WR-005: incorrect value provided
				optionalFieldValueWarnings.add(SbmValidationUtil.createErrorWarningLogDTO(
						"TobaccoUseCode", SBMErrorWarningCode.WR_005.getCode(), 
						ERROR_INFO_EXPECTED_VALUE_TXT.concat("T, N, or U")));
				
				member.setTobaccoUseCode(null);
				
				LOG.info("InValid value for TobaccoUseCode. Should be T, N, or U");
			}
		}
	}
	
	/*
	 * Validate NonCoveredSubscriberIndicator
	 */
	private void validateNonCoveredSubscriberInd(PolicyMemberType member,
			List<SbmErrWarningLogDTO> optionalFieldValueWarnings) {

		String nonCoveredSubscriberInd = member.getNonCoveredSubscriberInd();
		if (StringUtils.isNotBlank(nonCoveredSubscriberInd)) {
			
			if(!nonCoveredSubscriberInd.equalsIgnoreCase(Y)) {
				
				//create Warning WR-005: incorrect value provided
				optionalFieldValueWarnings.add(SbmValidationUtil.createErrorWarningLogDTO(
						"NonCoveredSubscriberInd", SBMErrorWarningCode.WR_005.getCode(), 
						ERROR_INFO_EXPECTED_VALUE_TXT.concat(Y)));
				
				member.setNonCoveredSubscriberInd(null);
				
				LOG.info("InValid value for nonCoveredSubscriberInd. Should be Y");
			}
		}
	}
	
	/*
	 * Validate Member Dates
	 */
	private List<SbmErrWarningLogDTO> validatememberDates(PolicyType policy, PolicyMemberType member) {

		List<SbmErrWarningLogDTO> memberDatesErrors = new ArrayList<SbmErrWarningLogDTO>();
		
		//List<PolicyMemberType> policyMembers = policy.getMemberInformation();
		
		LocalDate policyStartDate = DateTimeUtil.getLocalDateFromXmlGC(policy.getPolicyStartDate());
		LocalDate policyEndDate = DateTimeUtil.getLocalDateFromXmlGC(policy.getPolicyEndDate());
		String effectuationInd = policy.getEffectuationIndicator();
		String exchangeMemberId = member.getExchangeAssignedMemberId();
		
		//policyMembers.forEach(member -> { 
			
		List<MemberDates> memberDates = member.getMemberDates();
		
		if(CollectionUtils.isEmpty(memberDates)) {
			
			//memberDates.forEach(memberDate -> { 
				
				//if(memberDate.getMemberStartDate() == null) {
					
					//FR-FM-PP-SBMI-229, FR-FM-PP-SBMI-230, FR-FM-PP-SBMI-231
					validateMemberStartDateMissing(member, memberDatesErrors);
					
				//} else {
					
				//}
			//});
		} else {
			memberDates.forEach(memberDate -> { 
				
				LocalDate memberStartDate = DateTimeUtil.getLocalDateFromXmlGC(memberDate.getMemberStartDate());
				
				//FR-FM-PP-SBMI-232,233 : R062 - Member Start Date to Policy Start Date validation
				if(sbmBusinessRules.contains("R062")) {
					
					if(memberStartDate.isBefore(policyStartDate)) {
						
						//create error ER-031: incorrect value provided
						memberDatesErrors.add(SbmValidationUtil.createMemberErrorWarningLogDTO(
								"MemberStartDate", SBMErrorWarningCode.ER_031.getCode(), exchangeMemberId, 
								ERROR_DESC_INCORRECT_VALUE, ERROR_INFO_EXPECTED_VALUE_TXT
									.concat("Greater than or equal to the PolicyStartDate ").concat(String.valueOf(policyStartDate))));
						
						LOG.info("Member Start less than Policy Start");
					}
				}
				
				//FR-FM-PP-SBMI-543,544 : R069 - Member Start Date to Policy End Date validation
				if(sbmBusinessRules.contains("R069")) {
					
					if(memberStartDate.isAfter(policyEndDate)) {
						
						//create error ER-64: incorrect value provided
						memberDatesErrors.add(SbmValidationUtil.createMemberErrorWarningLogDTO(
								"MemberStartDate", SBMErrorWarningCode.ER_064.getCode(), exchangeMemberId, 
								ERROR_DESC_INCORRECT_VALUE, ERROR_INFO_EXPECTED_VALUE_TXT
									.concat("Less than or equal to the PolicyEndDate ").concat(String.valueOf(policyEndDate))));
						
						LOG.info("Member Start greater than Policy End");
					}
				}
				
				LocalDate memberEndDate = DateTimeUtil.getLocalDateFromXmlGC(memberDate.getMemberEndDate());
				
				//FR-FM-PP-SBMI-235 : R063 - Member End Date to Policy End Date validation
				if(sbmBusinessRules.contains("R063")) {
					
					if(memberEndDate != null && (memberEndDate.isAfter(policyEndDate) && effectuationInd.equalsIgnoreCase(Y))) {
						
						//create error ER-032: incorrect value provided
						memberDatesErrors.add(SbmValidationUtil.createMemberErrorWarningLogDTO(
								"MemberEndDate", SBMErrorWarningCode.ER_032.getCode(), exchangeMemberId,
								ERROR_DESC_INCORRECT_VALUE, ERROR_INFO_EXPECTED_VALUE_TXT
									.concat("Less than or equal to the PolicyEndDate ").concat(String.valueOf(policyEndDate))));
						
						LOG.info("Member End exceeds Policy End");
					}
				}
			});
			
			if(memberDates.size() > 1) {
				
				//FR-FM-PP-SBMI-504 : R064 - Multiple Member Start Date Check
				if(sbmBusinessRules.contains("R064")) {

					if(isMemberEndDateMisaligned(memberDates)) {
						
						//create error ER-054: incorrect value provided
						memberDatesErrors.add(SbmValidationUtil.createMemberErrorWarningLogDTO(
								"MemberEndDate", SBMErrorWarningCode.ER_054.getCode(), exchangeMemberId));
					}
				}
			}
		}
		//});
		return memberDatesErrors;
	}

	/*
	 * Member Start Date Missing Validations
	 */
	private void validateMemberStartDateMissing(PolicyMemberType member,
			List<SbmErrWarningLogDTO> mamberDatesErrors) {

		String subscriberInd = member.getSubscriberIndicator();
		String nonCoveredSubscriberInd = member.getNonCoveredSubscriberInd();
		String exchangeMemberId = member.getExchangeAssignedMemberId();
		
		//FR-FM-PP-SBMI-229
		if(!subscriberInd.equalsIgnoreCase(Y)) {
			
			//FR-FM-PP-SBMI-229: R060 - Missing Member Date for Subscriber Indicator not equal to "Y" error
			if(sbmBusinessRules.contains("R060")) {
				//create error ER-028: incorrect value provided
				mamberDatesErrors.add(SbmValidationUtil.createMemberErrorWarningLogDTO(
						"MemberStartDate", SBMErrorWarningCode.ER_028.getCode(), exchangeMemberId));
				
				LOG.info("MemberStartDate Missing for non-subscriber ");
			}
			
		} else if(subscriberInd.equalsIgnoreCase(Y)) {
			
			//FR-FM-PP-SBMI-230
			if(StringUtils.isEmpty(nonCoveredSubscriberInd)) {
				
				//FR-FM-PP-SBMI-230: R061 - Missing Member Date for Subscriber Indicator equal to "Y" and missing Non Covered Subscriber Ind. error
				if(sbmBusinessRules.contains("R061")) {	
					//create error ER-028: incorrect value provided
					mamberDatesErrors.add(SbmValidationUtil.createMemberErrorWarningLogDTO(
							"MemberStartDate", SBMErrorWarningCode.ER_028.getCode(), exchangeMemberId));
					
					LOG.info("MemberStartDate Missing for subscriber when nonCoveredSubscriberInd doesnt exist");
				}
			
			} else {
				//FR-FM-PP-SBMI-231: R066 - Missing Member Date for Subscriber Indicator equal to Y and Non Covered Subscriber Ind. Not equal to Y error
				if(sbmBusinessRules.contains("R066")) {
					
					if(!nonCoveredSubscriberInd.equalsIgnoreCase(Y)) {
						
						//create error ER-028: incorrect value provided
						mamberDatesErrors.add(SbmValidationUtil.createMemberErrorWarningLogDTO(
								"MemberStartDate", SBMErrorWarningCode.ER_028.getCode(), exchangeMemberId));
						
						LOG.info("MemberStartDate Missing for subscriber when nonCoveredSubscriberInd != Y");
					}
				}
			}
		}
	}

	/*
	 * Validate end date misaligned with subsequent start date when multiple member dates
	 */
	private boolean isMemberEndDateMisaligned(List<MemberDates> memberDates) {
		
		SbmValidationUtil.sortMemberDates(memberDates);
		
		LocalDate prevEndDt = DateTimeUtil.getLocalDateFromXmlGC(
				memberDates.get(0).getMemberStartDate()).minusDays(1);
		
		for(MemberDates memberDate : memberDates) { 
			
			LocalDate memStartDt = DateTimeUtil.getLocalDateFromXmlGC(memberDate.getMemberStartDate());
			
			if(!prevEndDt.isBefore(memStartDt)) {
				return true;
			}
			
			prevEndDt = DateTimeUtil.getLocalDateFromXmlGC(memberDate.getMemberEndDate());
		}
		return false;
	}

	@Override
	public List<SbmErrWarningLogDTO> validateEpsPolicy(SBMPolicyDTO dbSBMPolicyDTO, SBMPolicyDTO policyDTO) {
		
		List<SbmErrWarningLogDTO> policyMatchErrors = new ArrayList<SbmErrWarningLogDTO>();
		
		PolicyType epsPolicy = dbSBMPolicyDTO.getPolicy();
		PolicyType inboundPolicy = policyDTO.getPolicy();
		
		//FR-FM-PP-SBMI-307: QHPID matching for matched policies
		validateQhpIdToEps(inboundPolicy.getQHPId(), epsPolicy.getQHPId(), policyMatchErrors);
		
		//FR-FM-PP-SBMI-308: Insurance Line Code matching for matched policies
		validateInsuranceLineCd(
				inboundPolicy.getInsuranceLineCode(), epsPolicy.getInsuranceLineCode(), policyMatchErrors);
		
		//FR-FM-PP-SBMI-313: Member matching for matched policies
		validateMissingMembers(inboundPolicy.getMemberInformation(), epsPolicy.getMemberInformation(), policyMatchErrors);
		
		return policyMatchErrors;
		
	}

	/*
	 * Validate QHP id against eps Policy QHP id
	 */
	private void validateQhpIdToEps(String qhpId, String epsQhpId, List<SbmErrWarningLogDTO> policyMatchErrors) {
		
		if(StringUtils.isNotBlank(qhpId) && StringUtils.isNotBlank(epsQhpId)
				&& !qhpId.equals(epsQhpId)) {
			
			//create Error ER-056: incorrect value provided
			policyMatchErrors.add(SbmValidationUtil.createErrorWarningLogDTO(
					"QHPId", SBMErrorWarningCode.ER_056.getCode(), ERROR_DESC_INCORRECT_VALUE));
			
			LOG.info("QHPID has changed {} : {} ", qhpId, epsQhpId);
		}
	}
	
	/*
	 *  Validate InsuranceLineCode against eps Policy InsuranceLineCode
	 */
	private void validateInsuranceLineCd(String insuranceLineCode, String epsInsuranceLineCode,
			List<SbmErrWarningLogDTO> policyMatchErrors) {
		
		if(StringUtils.isNotBlank(insuranceLineCode) && StringUtils.isNotBlank(epsInsuranceLineCode)
				&& !insuranceLineCode.equals(epsInsuranceLineCode)) {
			
			//create Error WR-009: incorrect value provided
			policyMatchErrors.add(SbmValidationUtil.createErrorWarningLogDTO(
					"InsuranceLineCode", SBMErrorWarningCode.WR_009.getCode(), 
					ERROR_DESC_INCORRECT_VALUE, 
					ERROR_INFO_EXPECTED_VALUE_TXT.concat(epsInsuranceLineCode)));
			
			LOG.info("InsuranceLineCode changed on matching policy {} : {} ", insuranceLineCode, epsInsuranceLineCode);
		}
	}
	
	/*
	 * Validate for Members missing in Inbound Policy
	 */
	private void validateMissingMembers(List<PolicyMemberType> policyMembers,
			List<PolicyMemberType> epsPolicyMembers, List<SbmErrWarningLogDTO> policyMatchErrors) {
		
		//inbound member ids
		List<String> exchangeMemberids = 
				policyMembers.stream()
                .map(PolicyMemberType::getExchangeAssignedMemberId)
                .collect(Collectors.toList());
		
		//Eps policy member ids
		List<String> epsExchangeMemberIds = 
				epsPolicyMembers.stream()
                .map(PolicyMemberType::getExchangeAssignedMemberId)
                .collect(Collectors.toList());
		
		epsExchangeMemberIds.forEach(epsMemberId -> { 
			
			if(!exchangeMemberids.contains(epsMemberId)) {
				
				//create Error WR-010: incorrect value provided
				policyMatchErrors.add(SbmValidationUtil.createMemberErrorWarningLogDTO(
						"ExchangeAssignedMemberID", SBMErrorWarningCode.WR_010.getCode(), 
						epsMemberId));
				
				LOG.info("Previous Member not provided in Policy record {} ", epsMemberId);
			}
		});
	}

	/**
	 * @param sbmDataService the sbmDataService to set
	 */
	public void setSbmDataService(SBMDataService sbmDataService) {
		this.sbmDataService = sbmDataService;
	}

}
