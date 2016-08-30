package gov.hhs.cms.ff.fm.eps.ep.enums;

import com.accenture.foundation.common.exception.ApplicationException;

public enum SBMFileStatus {

	REJECTED	            ("RJC", "Rejected"),
	ACCEPTED	            ("ACC", "Accepted"),
	ACCEPTED_WITH_ERRORS	("ACE", "Accepted with errors"),
	ACCEPTED_WITH_WARNINGS	("ACW", "Accepted with warnings"),
	APPROVED	            ("APP", "Approved"),
	APPROVED_WITH_ERRORS	("APE", "Approved with errors"),
	APPROVED_WITH_WARNINGS	("APW", "Approved with warnings"),
	DISAPPROVED	            ("DSP", "Disapproved"),
	IN_PROCESS	            ("IPC", "In Process"),
	ON_HOLD	                ("OHD", "On Hold"),
	FREEZE	                ("FRZ", "Freeze"),
	BYPASS_FREEZE	        ("BPF", "Bypass Freeze"),
	PENDING_FILES	        ("PDF", "Pending Files"),
	EXPIRED	        		("EXP", "Expired"),
	BACKOUT	                ("BKO", "Backout"),
	;
	
	private String value;	
	private String name;
	
	private SBMFileStatus(String value, String name) {
		this.value = value;
		this.name = name;
	}

	/**
	 * @param value
	 * @return
	 */
	public static SBMFileStatus getEnum(String value) {
		
        for (SBMFileStatus status: SBMFileStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new ApplicationException("Invalid SBMFileStatusType: " + value);
    }
	
	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
}
