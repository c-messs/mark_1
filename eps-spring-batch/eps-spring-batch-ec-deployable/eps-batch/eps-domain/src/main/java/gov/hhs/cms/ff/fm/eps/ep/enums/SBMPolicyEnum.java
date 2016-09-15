package gov.hhs.cms.ff.fm.eps.ep.enums;

/**
 * Enrollment tag names for XML validation error translation.
 *
 */
public enum SBMPolicyEnum {
	
	//High level tags
	FILE_INFO ("FileInformation"),
	POLICY("Policy"),
	
	// FileInformationType			
	
	FILE_ID	          ("FileId"),
	FILE_CREATE_DT	  ("FileCreateDateTime"),
	TENANT_ID	      ("TenantId"),
	COVERAGE_YEAR	  ("CoverageYear"),
	ISS_FILE_INFO  ("IssuerFileInformation"),
	
	// IssuerFileInformation
	
	ISS_ID         ("IssuerId"),
	
	// IssuerFileSet
	ISSUER_FILE_SET   ("IssuerFileSet"),
	ISS_FILE_SET_ID   ("IssuerFileSetId"),
	FILE_NUM          ("FileNumber"),
	TOT_ISS_FILES     ("TotalIssuerFiles"),
				
	// PolicyType			
				
	REC_CTRL_NUM	  ("RecordControlNumber"),
	QHPID	          ("QHPId"),
	EX_ASSIGN_POL_ID  ("ExchangeAssignedPolicyId"),
	EX_ASSIGN_SUB_ID  ("ExchangeAssignedSubscriberId"),
	ISS_ASSIGN_POL_ID ("IssuerAssignedPolicyId"),
	ISS_ASSIGN_SUB_ID ("IssuerAssignedSubscriberId"),
	PSD	              ("PolicyStartDate"),
	PED	              ("PolicyEndDate"),
	EFF_IND	          ("EffectuationIndicator"),
	INSUR_LINE_CD	  ("InsuranceLineCode"),
				
	// PolicyMemberType (MemberInformation)			
	MEMBER_INFO  	  ("MemberInformation"),			
	EX_ASSIGN_MEM_ID  ("ExchangeAssignedMemberId"),
	SUB_IND	          ("SubscriberIndicator"),
	ISS_ASSIGN_MEM_ID ("IssuerAssignedMemberId"),
	NM_PREFIX	      ("NamePrefix"),
	MEM_LAST_NM	      ("MemberLastName"),
	MEM_FIRST_NM	  ("MemberFirstName"),
	MEM_MID_NM	      ("MemberMiddleName"),
	NM_SUF	          ("NameSuffix"),
	DOB	              ("BirthDate"),
	SSN	              ("SocialSecurityNumber"),
	ZIP	              ("PostalCode"),
	LANG_QUAL_CD	  ("LanguageQualifierCode"),
	LANG_CD	          ("LanguageCode"),
	GENDER_CD	      ("GenderCode"),
	RACE_CD	          ("RaceEthnicityCode"),
	TOBACCO_CD	      ("TobaccoUseCode"),
	NON_COV_SUB_IND   ("NonCoveredSubscriberInd"),
				
	// MemberDates			
				
	MEM_START_DATE	  ("MemberStartDate"),
	MEM_END_DATE	  ("MemberEndDate"),
				
	// FinancialInformationType (FinancialInformation)			
	FINANCIAL_INFO	  ("FinancialInformation"),
	ESD	              ("FinancialEffectiveStartDate"),
	EED               ("FinancialEffectiveEndDate"),
	TPA	              ("MonthlyTotalPremiumAmount"),
	TIRA              ("MonthlyTotalIndividualResponsibilityAmount"),
	APTC	          ("MonthlyAPTCAmount"),
	OPA1	          ("MonthlyOtherPaymentAmount1"),
	OPA2	          ("MonthlyOtherPaymentAmount2"),
	CSR	              ("MonthlyCSRAmount"),
	VARIANT_ID	      ("CSRVariantId"),
	RA	              ("RatingArea"),
				
	// ProratedAmountType (ProratedAmount)			
	PRORATED_AMOUNTS  ("ProratedAmount"),			
	PRO_ESD	          ("PartialMonthEffectiveStartDate"),
	PRO_EED	          ("PartialMonthEffectiveEndDate"),
	PRO_TPA	          ("PartialMonthPremiumAmount"),
	PRO_APTC	      ("PartialMonthAPTCAmount"),
	PRO_CSR	          ("PartialMonthCSRAmount"),
	;

	
	String elementNm;

	private SBMPolicyEnum(String elementNm) {
		this.elementNm = elementNm;
	}

	/**
	 * Return the elementName
	 * @return elementNm
	 */
	public String getElementNm() {
		return elementNm;
	}

}
