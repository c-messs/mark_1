package gov.hhs.cms.ff.fm.eps.ep.enums;

/**
 * SBM file ans XPR errors and warnings.
 *
 */
public enum SBMErrorWarningCode {
	
	ER_001 ("ER-001"),		
	ER_003 ("ER-003"),	
	ER_004 ("ER-004"),	
	ER_006 ("ER-006"),
	ER_009 ("ER-009"),
	ER_011 ("ER-011"),
	ER_012 ("ER-012"),
	ER_013 ("ER-013"),
	ER_014 ("ER-014"),
	ER_015 ("ER-015"),
	ER_016 ("ER-016"),
	ER_017 ("ER-017"),
	ER_018 ("ER-018"),
	ER_019 ("ER-019"),
	ER_020 ("ER-020"),
	ER_021 ("ER-021"),
	ER_022 ("ER-022"),
	ER_023 ("ER-023"),
	ER_024 ("ER-024"),
	ER_025 ("ER-025"),
	ER_026 ("ER-026"),
	ER_027 ("ER-027"),
	ER_028 ("ER-028"),
	ER_029 ("ER-029"),
	ER_030 ("ER-030"),
	ER_031 ("ER-031"),
	ER_032 ("ER-032"),
	ER_033 ("ER-033"),
	ER_034 ("ER-034"),
	ER_035 ("ER-035"),
	ER_036 ("ER-036"),
	ER_037 ("ER-037"),
	ER_038 ("ER-038"),
	ER_039 ("ER-039"),
	ER_040 ("ER-040"),
	ER_041 ("ER-041"),
	ER_042 ("ER-042"),
	ER_043 ("ER-043"),
	ER_044 ("ER-044"),
	ER_045 ("ER-045"),
	ER_046 ("ER-046"),
	ER_047 ("ER-047"),
	ER_048 ("ER-048"),
	ER_049 ("ER-049"),
	ER_050 ("ER-050"),
	ER_051 ("ER-051"),
	ER_052 ("ER-052"),
	ER_053 ("ER-053"),
	ER_054 ("ER-054"),
	ER_055 ("ER-055"),
	ER_056 ("ER-056"),
	ER_057 ("ER-057"),
	ER_058 ("ER-058"),
	ER_059 ("ER-059"),
	ER_060 ("ER-060"),
	ER_061 ("ER-061"),
	ER_062 ("ER-062"),
	ER_063 ("ER-063"),
	ER_064 ("ER-064"),
	ER_500 ("ER-500"),
	ER_501 ("ER-501"),
	ER_502 ("ER-502"),
	ER_503 ("ER-503"),
	ER_504 ("ER-504"),
	ER_505 ("ER-505"),
	ER_506 ("ER-506"),
	ER_507 ("ER-507"),
	ER_508 ("ER-508"),
	ER_509 ("ER-509"),
	ER_510 ("ER-510"),
	ER_511 ("ER-511"),
	ER_512 ("ER-512"),
	ER_513 ("ER-513"),
	ER_514 ("ER-514"),
	ER_515 ("ER-515"),
	ER_516 ("ER-516"),
	ER_517 ("ER-517"),
	ER_518 ("ER-518"),
	ER_519 ("ER-519"),
	ER_520 ("ER-520"),
	ER_521 ("ER-521"),
	ER_522 ("ER-522"),
	ER_997 ("ER-997"),	
	ER_998 ("ER-998"),
	WR_001 ("WR-001"),
	WR_002 ("WR-002"),
	WR_003 ("WR-003"),
	WR_004 ("WR-004"),
	WR_005 ("WR-005"),
	WR_006 ("WR-006"),
	WR_007 ("WR-007"),
	WR_008 ("WR-008"),
	WR_009 ("WR-009"),
	WR_010 ("WR-010"),
	SYSTEM_ERROR_999 ("999")
	;

	String code;
	

	private SBMErrorWarningCode (String code) {
		this.code = code;		
	}
	
	
	/**
	 * 
	 * @return
	 */
	public String getCode() {
		return code;
	}
		
}
