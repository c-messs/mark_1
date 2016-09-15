package gov.hhs.cms.ff.fm.eps.ep.enums;

import com.accenture.foundation.common.exception.ApplicationException;

/**
 * SBM TransMsg statuses for processed transactions.  
 * - "Good" transactions will be either ACC or ANC.
 * - Replicated transactions from previous cycles will not have an SbmTransMsg record and 
 *   therefore no SbmTransMsg status.
 *
 */
public enum SbmTransMsgStatus {
	
	REJECTED             ("RJC", "Rejected", "Policy that failed schema and business validations, or belonging to a file that failed error threshold validation."),
	ACCEPTED_WITH_CHANGES("ACC", "Accepted With Changes", "Matched Policy record processed successfully with changes found or New policy processed successfully with changes applied to persist to DB."),
	ACCEPTED_NO_CHANGE   ("ANC", "Accepted No Change", "New or matched policy with no changes applied to XPR to persist to DB."),
	SKIP                 ("SKP", "Skip", "Technical exceptions during processing."),

	;

	
	String code;
	String longNm;
	String desc;

	private SbmTransMsgStatus (String code, String longNm, String desc) {

		this.code = code;
		this.longNm = longNm;
		this.desc = desc;
	}
	
	/**
	 * Get the SbmTransMsgStatus for the supplied value.  Throws ApplicationException(EPROD-07) if code not found.
	 * @param value
	 * @return SbmTransMsgStatus
	 */
	public static SbmTransMsgStatus getEnum(String value) {
        for (SbmTransMsgStatus status: SbmTransMsgStatus.values()) {
            if (status.getCode().equals(value)) {
                return status;
            }
        }
        throw new ApplicationException(EProdEnum.EPROD_07.getLogMsg() + ": " + value, EProdEnum.EPROD_07.getCode());
    }

	/**
	 * SBMTRANSMSGPROCSTATUSTYPECD 
	 * @return
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @return
	 */
	public String getLongNm() {
		return longNm;
	}

	
	/**
	 * EPROD Description.
	 * @return
	 */
	public String getDesc() {
		return desc;
	}
	
	
}
