package gov.hhs.cms.ff.fm.eps.ep.enums;

/**
 * The Enum SchemaValidationRuleEnum.
 */
public enum SBMSchemaValidationRuleEnum {
	
	/** The CV c_ typ e_313. */
	CVC_TYPE_313 ("cvc-type.3.1.3"),
	
	/** The CV c_ comple x_ typ e_24 a. */
	CVC_COMPLEX_TYPE_24A ("cvc-complex-type.2.4.a"),
	
	/** The CV c_ comple x_ typ e_24 b. */
	CVC_COMPLEX_TYPE_24B ("cvc-complex-type.2.4.b"),
	
	/** The CV c_ comple x_ typ e_24 b. */
	CVC_COMPLEX_TYPE_24D ("cvc-complex-type.2.4.d"),
	
	/** The cvc maxlength valid. */
	CVC_MAXLENGTH_VALID ("cvc-maxLength-valid"),
	
	/** The cvc pattern valid. */
	CVC_PATTERN_VALID ("cvc-pattern-valid"),
	
	/** The cvc maxinclusive valid. */
	CVC_MAXINCLUSIVE_VALID ("cvc-maxInclusive-valid"),
			
	/** The cvc length valid. */
	CVC_LENGTH_VALID ("cvc-length-valid");
	
//	/** The cvc assess attr. */
//	CVC_ASSESS_ATTR ("cvc-assess-attr", true, "10000"),
//	
//	/** The cvc assess elt. */
//	CVC_ASSESS_ELT ("cvc-assess-elt", true, "10000"),
//	
//	/** The cvc attribute. */
//	CVC_ATTRIBUTE ("cvc-attribute", true, "10000"),
//	
//	/** The cvc au. */
//	CVC_AU ("cvc-au", true, "10000"),
//	

//	/** The cvc datatype valid. */
//	CVC_DATATYPE_VALID ("cvc-datatype-valid", true, "10700"),
//	
//	/** The CV c_ datatyp e_ vali d_121. */
//	CVC_DATATYPE_VALID_121 ("cvc-datatype-valid.1.2.1", true, "10800"),
//	
//	/** The cvc elt. */
//	CVC_ELT ("cvc-elt", true, "10000"),
//	
//	/** The cvc enumeration valid. */
//	CVC_ENUMERATION_VALID ("cvc-enumeration-valid", true, "10000"),
//	
//	/** The cvc facet valid. */
//	CVC_FACET_VALID ("cvc-facet-valid", true, "10000"),
//	
//	/** The cvc fractiondigits valid. */
//	CVC_FRACTIONDIGITS_VALID ("cvc-fractionDigits-valid", true, "10900"),
//	
//	/** The cvc id. */
//	CVC_ID ("cvc-id", true, "10000"),
//	
//	/** The cvc identity constraint. */
//	CVC_IDENTITY_CONSTRAINT ("cvc-identity-constraint", true, "10000"),
//	

//	
//	/** The cvc maxexclusive valid. */
//	CVC_MAXEXCLUSIVE_VALID ("cvc-maxExclusive-valid", true, "10000"),
//	
//	/** The cvc minexclusive valid. */
//	CVC_MINEXCLUSIVE_VALID ("cvc-minExclusive-valid", true, "10000"),
//	
//	/** The cvc mininclusive valid. */
//	CVC_MININCLUSIVE_VALID ("cvc-minInclusive-valid", true, "10000"),
//	
//	/** The cvc minlength valid. */
//	CVC_MINLENGTH_VALID ("cvc-minLength-valid", true, "10700"),
//	
//	/** The cvc model group. */
//	CVC_MODEL_GROUP ("cvc-model-group", true, "10000"),
//	
//	/** The cvc particle. */
//	CVC_PARTICLE ("cvc-particle", true, "10000"),
//	

//	
//	/** The cvc resolve instance. */
//	CVC_RESOLVE_INSTANCE ("cvc-resolve-instance", true, "10000"),
//	
//	/** The cvc simple type. */
//	CVC_SIMPLE_TYPE ("cvc-simple-type", true, "10000"),
//	
//	/** The cvc totaldigits valid. */
//	CVC_TOTALDIGITS_VALID ("cvc-totalDigits-valid", true, "10700"),
//	

//	
//	/** The cvc wildcard. */
//	CVC_WILDCARD ("cvc-wildcard", true, "10000"),
//	
//	/** The cvc wildcard namespace. */
//	CVC_WILDCARD_NAMESPACE ("cvc-wildcard-namespace", true, "10");

	/** The value. */
	String value;
		
	/**
	 * Instantiates a new schema validation rule enum.
	 *
	 * @param value the value
	 * @param isTwiceErrorRule the is twice error rule
	 * @param errorCode the error code
	 */
	private SBMSchemaValidationRuleEnum(String value) {

		this.value = value;
		
	}

	/**
	 * Find rule.
	 *
	 * @param value the value
	 * @return the schema validation rule enum
	 */
	public static SBMSchemaValidationRuleEnum findRule(String value) {

		SBMSchemaValidationRuleEnum rule = null;

		for(SBMSchemaValidationRuleEnum e : values()) {

			if(e.value.equals(value)) {

				rule = e;
				break;
			}
		}
		return rule;
	}

	
	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public String getValue() {
		return value;
	}


}
