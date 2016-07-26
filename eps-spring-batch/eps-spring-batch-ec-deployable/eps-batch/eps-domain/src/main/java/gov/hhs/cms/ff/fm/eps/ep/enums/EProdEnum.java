package gov.hhs.cms.ff.fm.eps.ep.enums;

import com.accenture.foundation.common.exception.ApplicationException;

/**
 * The Enum class for EPROD codes.
 * 
 * @author j.radz
 *
 */
public enum EProdEnum {

	EPROD_01 ("EPROD-01", "Service Access Failure", "Service unable to access the Public Folder"),
	EPROD_02 ("EPROD-02", "Private Folder Write Incomplete", "Files did not write to the Private folder completely"),
	EPROD_03 ("EPROD-03", "XML File Invalid", "File is not a valid XML file"),
	EPROD_04 ("EPROD-04", "File XSD Schema Invalid", "File is not compliant with the XSD schema"),
	EPROD_05 ("EPROD-05", "Temporary Table Access Failure", "Unable to access temporary table"),
	EPROD_06 ("EPROD-06", "Transaction Message Table Access Failure", "Unable to access to Transaction Message table"),
	EPROD_07 ("EPROD-07", "Ref Table - Invalid Error Code", "Invalid error code in the reference table"),
	EPROD_08 ("EPROD-08", "FFM DB Updater Write Failure-Web", "FFM Database Updater does not update successfully(Web service Call unsuccessful)"),
	EPROD_09 ("EPROD-09", "FFM DB Updater Write Failure-DB", "FFM Database Updater does not update successfully(DB Failure)"),
	EPROD_10 ("EPROD-10", "EPS DB Updater Write Failure-DB", "EPS Database Updater does not update successfully(DB Failure)"),
	EPROD_11 ("EPROD-11", "FFM BAA DB Store Failure", "FFM BAA store in DB fails"),
	EPROD_12 ("EPROD-12", "EPS BAA DB Store Failure", "EPS BAA store in DB fails"),
	EPROD_13 ("EPROD-13", "DB Out Of Sync", "DB out of Sync"),
	EPROD_14 ("EPROD-14", "Web Service Timeout", "Web Service Timeout"),
	EPROD_15 ("EPROD-15", "Web Service Down", "Web Service Down"),
	EPROD_16 ("EPROD-16", "BAA Generator Down", "BAA Generation Down"),
	EPROD_17 ("EPROD-17", "BAA Transmission to HUB Failure", "BAA fails to transmit XML to HUB"),
	EPROD_18 ("EPROD-18", "Invalid file name", "Invalid file name"),
	EPROD_19 ("EPROD-19", "HIOS ID does not match existing Policy", "HIOS ID does not match existing Policy in DB"),
	EPROD_20 ("EPROD-20", "Technical error in EE Web Service", "Error occurred withing EE web service identified by \"500\" reponse."),
	EPROD_21 ("EPROD-21", "EPS DB Reader Failure-DB", "Failed to retrieve, or select, data from EPS DB"),
	EPROD_22 ("EPROD-22", "Null Pointer Exception", "A NullPointerException occurred."),
	EPROD_23 ("EPROD-23", "BRMS Rules Engine Failure", "Failure in call to business rules engine. "),
	EPROD_24 ("EPROD-24", "EPS JAXB Marshalling error (EPS data to BEM)", "Failed to marshall outbound EPS data to XML."),
	EPROD_25 ("EPROD-25", "EPS DB Reader Failure-DB: Metal level is null, cannot proceed with pre BLE.", "Missing Reference Data in the InsrncPlan table"),
	EPROD_26 ("EPROD-26", "Read and write counts mismatch", "Read and write counts mismatch in BemProcessorListener"),
	EPROD_27 ("EPROD-27", "Expected and actual counts mismatch", "Expected and actual counts mismatch in BemProcessor Listener"),
	EPROD_28 ("EPROD-28", "Failed to clear private directory from ERL Staging area", "Failed to clear private directory from ERL Staging area in BemProcessor Listener"),
	EPROD_29 ("EPROD-29", "ERL - Previous Version of the Policy was skipped.  Skip the current transaction.", "ERL - Previous Version of the Policy was skipped.  Skip the current transaction."),
	EPROD_30 ("EPROD-30", "Missing ExchangePolicyID (GroupPolicyNumber)", "Missing ExchangePolicyID in inbound BEM."),
	EPROD_31 ("EPROD-31", "Missing StateCode (first 2 chars of subscriber SourceExchangeID)", "Missing StateCode inbound BEM."),
	EPROD_32 ("EPROD-32", "Missing HiOS ID (first 5 chars of subscriber ContractCode)", "Missing HiOS ID in inbound BEM."),
	EPROD_33 ("EPROD-33", "Missing PolicySnapshotVersionNumber", "Missing PolicySnapshotVersionNumber in inbound BEM."),
	EPROD_34 ("EPROD-34", "Policy version found in Skipped status in an earlier processing", "Policy version Skipped earlier"),
	EPROD_35 ("EPROD-35", "Later/Duplicate version found in EPS", "Later/Duplicate version found in EPS"),
	EPROD_36 ("EPROD-36", "Invalid Transaction Type", "Invalid Insurance Policy Status Type"),
	EPROD_37 ("EPROD-37", "$0 Total Premium Amount", "Total Premium Amount Cannot be zero"),
	EPROD_38 ("EPROD-38", "Non-matching MGPI", "Matching ExchangePolicyID in EPS has a different Marketplace Group PolicyIdentifier than the MGPI specified in the BEM"),
	EPROD_99 ("EPROD-99", "General Exception", "Error has not been classified"),
	;

	String code;
	String longNm;
	String desc;

	private EProdEnum (String code, String longNm, String desc) {

		this.code = code;
		this.longNm = longNm;
		this.desc = desc;
	}
	
	/**
	 * Get the EProdEnum for the supplied value.  Throws ApplicationException(EPROD-07) if code not found.
	 * @param value
	 * @return EProdEnum
	 */
	public static EProdEnum getEnum(String value) {
        for (EProdEnum eProd: EProdEnum.values()) {
            if (eProd.getCode().equals(value)) {
                return eProd;
            }
        }
        throw new ApplicationException(EProdEnum.EPROD_07.getLogMsg() + ": " + value, EProdEnum.EPROD_07.getCode());
    }

	/**
	 * TRANSMSGSKIPREASONTYPE.TRANSMSGSKIPREASONTYPECD to BATCHTRANSMSG.TRANSMSGSKIPREASONTYPECD
	 * @return
	 */
	public String getCode() {
		return code;
	}

	/**
	 * EPROD name to BatchTransMsg skip reason description.
	 * TRANSMSGSKIPREASONTYPE.TRANSMSGSKIPREASONTYPENM to BATCHTRANSMSG.TRANSMSGSKIPREASONDESC
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
	
	/**
	 * Return formatted for log message.   
	 * code:  longNm
	 * @return
	 */
	public String getLogMsg() {
		return code + ": " + longNm;
	}
	
}
