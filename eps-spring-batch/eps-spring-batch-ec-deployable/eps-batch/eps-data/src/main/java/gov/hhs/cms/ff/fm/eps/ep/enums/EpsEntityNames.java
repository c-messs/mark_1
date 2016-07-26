package gov.hhs.cms.ff.fm.eps.ep.enums;

/**
 * @author 
 *
 */
public enum EpsEntityNames {
	
	
	
	TXN_MESSAGE_ENTITY_NAME("TxnMessage"),
	POLICY_VERSION_ENTITY_NAME("PolicyVersion"),
	POLICY_STATUS_ENTITY_NAME("PolicyStatus"),
	POLICY_PREMIUM_ENTITY_NAME("PolicyPremium"),
	POLICY_MEMBER_ENTITY_NAME("PolicyMember"),
	POLICY_MEMBER_VERSION_ENTITY_NAME("PolicyMemberVersion"),
	POLICY_MEMBER_ADDRESS_ENTITY_NAME("PolicyMemberAddress"),
	POLICY_MEMBER_DATE_ENTITY_NAME("PolicyMemberDate"),
	POLICY_PAYMENT_TRANS_ENTITY_NAME("PolicyPaymentTrans"),
	CURR_PAYMENT_MONTH_ENTITY_NAME("CurrPaymentMonth"),
	TRANS_MSG_FILE_INFO_ENTITY_NAME("TransMsgFileInfo"), 
	TRANS_MSG_ENTITY_NAME("TransMsg"), 
	BATCH_ENTITY_NAME("Batch_job_instance"), 
	ERROR_WARNING_LOG("ErrorWarningLog"), 
	STATE_CONFIGURATION("StateConfiguration"),
	BATCH_TRANS_MSG_ENTITY_NAME("BatchTransMsg");
	
	
	
	private final String value;

	private EpsEntityNames(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}
