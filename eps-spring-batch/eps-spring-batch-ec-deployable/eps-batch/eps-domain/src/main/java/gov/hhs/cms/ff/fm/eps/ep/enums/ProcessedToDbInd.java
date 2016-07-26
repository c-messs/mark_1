package gov.hhs.cms.ff.fm.eps.ep.enums;

import com.accenture.foundation.common.exception.ApplicationException;

/**
 * @author eps
 *
 */
public enum ProcessedToDbInd {
	
	Y ("Y", "Yes"),
	N ("N", "No"),
	S ("S", "Skipped"),
	R ("R", "Reprocess"),
	I ("I", "Ignore"),
	D ("D", "Deleted");
	
	private String value;
	private String description;
	
	/**
	 * @param value
	 * @param description
	 */
	private ProcessedToDbInd(String value, String description) {
		this.value = value;
		this.description = description;
	}
	
	/**
	 * @param value
	 * @return
	 */
	public static ProcessedToDbInd getEnum(String value) {
		
        for (ProcessedToDbInd ind: ProcessedToDbInd.values()) {
            if (ind.value.equals(value)) {
                return ind;
            }
        }
        throw new ApplicationException("Invalid ProcessedToDbInd: " + value);
    }
    
	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
    
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

}
