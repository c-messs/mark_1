xquery version "1.0-ml";
(:~
ee util functions
2013-06-10 first version
@author mmorad
:)
module namespace mappingHelperFFEBEMRequest834="http://ffx.ffe.gov/lib/utils/mappingHelperFFEBEMRequest834";
import module namespace hixcrud = "http://ffx.ffe.gov/lib/crudUtil" at "/lib/crudUtil.xqy";
import module namespace sm = "http://marklogic.com/ps/servicemetrics" at "/lib/servicemetrics/metrics/servicemetrics.xqy";
import module namespace hixInsurancePlanPolicy="http://ffx.ffe.gov/impls/InsurancePlanPolicy" at "/Services/InsurancePlanPolicy/impl/InsurancePlanPolicyImpl.xqy";
import module namespace hixInsurancePlan="http://ffx.ffe.gov/impls/InsurancePlan" at "/Services/InsurancePlan/impl/InsurancePlanImpl.xqy";
import module namespace hixIssuerApplication = "http://ffx.ffe.gov/impls/IssuerApplication" at "/Services/IssuerApplication/impl/IssuerApplicationImpl.xqy";
import module namespace eeUtilXQueries = "http://ffx.ffe.gov/lib/utils/eeUtilXQueries" at "/lib/utils/eeUtilXQueries.xqy";
import module namespace bizidgen = "http://marklogic.com/ps/lib/id-generate" at "/lib/BusinessIDGen/id-generator.xqy";
import module namespace hixOrganization = "http://ffx.ffe.gov/impls/Organization" at "/Services/Organization/impl/OrganizationImpl.xqy";

declare namespace pb = "http://persistence.base.cms.hhs.gov";
declare namespace po = "http://persistence.ffe.cms.hhs.gov";
declare namespace base = "http://base.persistence.base.cms.hhs.gov";

declare namespace bem = "http://bem.dsh.cms.gov";

declare namespace error="http://marklogic.com/xdmp/error";


(:~
  @author matthew.royal@marklogic.com
  Attempt to format incoming variable as an xs:string formatted YYYY-MM-DD.
  If it fails, it will output the original variable as its original type.
:)
declare function mappingHelperFFEBEMRequest834:formatDateYyyymmdd($myDate) {

  if (xdmp:castable-as("http://www.w3.org/2001/XMLSchema", "dateTime", $myDate)) then 
    fn:format-date(xs:date(xs:dateTime($myDate)), "[Y0001]-[M01]-[D01]") 
  else 
    $myDate
};

declare function mappingHelperFFEBEMRequest834:oneOrNone($x) {
	if ($x) then ($x)[fn:last()] else ()
};

declare function mappingHelperFFEBEMRequest834:emptyElementToSring($x) {
	let $ret :=
		if($x) then $x
		else if ($x = 0) then $x
		else ""
	return $ret
};

declare function mappingHelperFFEBEMRequest834:assertHasValue($e, $eName) {
	let $path := xdmp:path($e/text())
	let $_:=
		if(fn:not(fn:empty($path))) then()
		else fn:error((), fn:concat("mappingHelperFFEBEMRequest834 assert: element ", xdmp:path($e), $eName, " does not have a value"))

	return $_
};

declare function mappingHelperFFEBEMRequest834:assertElementHasValue($e, $eName) {
	let $_ :=
		if (fn:not(fn:empty($e))) then ()
		else fn:error( (), fn:concat("mappingHelperFFEBEMRequest834 assert: element ", $eName, " is empty") )

	return $_
};

declare function mappingHelperFFEBEMRequest834:assertNotEmpty($e, $eName) {
	let $path := xdmp:path($e/node())
	let $_:=
		if(fn:not(fn:empty($path))) then()
		else fn:error((), fn:concat("mappingHelperFFEBEMRequest834 assert: element ", xdmp:path($e), $eName, " is empty"))
	return $_
};

declare function mappingHelperFFEBEMRequest834:assertHasChildElement($e, $eName) {
	let $path := xdmp:path($e/element())
	let $_:=
		if (fn:not(fn:empty($path))) then()
		else fn:error((), fn:concat("mappingHelperFFEBEMRequest834 assert: element ", xdmp:path($e), $eName, " does not have a child element"))
	return $_
};


declare function mappingHelperFFEBEMRequest834:checkEmptyNode($e as element()) {
	let $ret :=
		if($e/node()) then $e
		else ()

	return $ret
};

declare function mappingHelperFFEBEMRequest834:formatCurrency($c) {
	let $ret:=
		fn:format-number(xs:decimal($c),"###0.00")
	return $ret
};

declare function mappingHelperFFEBEMRequest834:formatString($s) {
	let $subString := fn:substring($s, 1, 60)
	return $subString
};

declare function mappingHelperFFEBEMRequest834:replace($stringToReplace, $replace, $with) {
	let $stringToReplace := fn:string-join(fn:distinct-values($stringToReplace), ",")
	let $newString :=
		try {
			let $replaced := fn:replace($stringToReplace, $replace, $with)
			return $replaced
		}
		catch ($e)
		{
			let $error := xdmp:quote($e)
			return  fn:error((), "BAD_REQUEST_400", ("200", xdmp:quote($e)))
		}

	return $newString

};

declare function mappingHelperFFEBEMRequest834:retrieveInsurancePlanPolicy(
	$policyTrackingNumber as xs:string, 
	$tenantId) 
as element(po:insurancePlanPolicy)? {
	let $ptn-query as cts:query :=
		cts:element-value-query(
			xs:QName("po:policyTrackingNumber"),
			$policyTrackingNumber
		)
	let $result :=
		hixcrud:search($tenantId,
			hixInsurancePlanPolicy:GetBasePath(),
			$ptn-query
		)
	let $howManyReturned := fn:count($result)
	let $_ := fn:trace("mappingHelperFFEBEMRequest834:retrieveInsurancePlanPolicy query on "
		|| "$tenantId = " || $tenantId
		|| " and "
		|| $ptn-query
		|| " return count is : "
		|| $howManyReturned
		|| " - described result: "
		|| xdmp:describe($result),
		"mappingHelperFFEBEMRequest834"
	)
	let $_ := fn:trace( ("mappingHelperFFEBEMRequest834:retrieveInsurancePlanPolicy - result dump: ", $result), "mappingHelperFFEBEMRequest834" )
	let $_ :=
		if($howManyReturned ne 1) then fn:error( (),
			"NOT_FOUND_404",
			(
				"100",
				"mappingHelperFFEBEMRequest834:retrieveInsurancePlanPolicy error, " ||
				$howManyReturned ||
				" InsurancePlanPolicy(ies) returned for " ||
				"$tenantId = " || $tenantId ||
				" and " ||
				" po:insurancePlanPolicy/po:policyTrackingNumber = " || $policyTrackingNumber || "." ||
				" Expecting exactly 1 but Found " || $howManyReturned
			)
		) 
		else ()
	return $result
};

declare function mappingHelperFFEBEMRequest834:mapIssuer(
	$insurancePlanPolicy as element(po:insurancePlanPolicy)
) as element() {
	let $issuerName := $insurancePlanPolicy/po:issuerName/fn:string()
	let $orgTaxId := $insurancePlanPolicy/po:issuerTaxPayerID

	let $orgTaxId :=
		if ($orgTaxId) then $orgTaxId
		else 
			fn:error( (),
				"NOT_FOUND_404",
				(
					"102",
					"mappingHelperFFEBEMRequest834:mapIssuer error - " ||
					" issuerTaxPayerID not found on Insurance Plan Policy"
				)
		)
	let $issuerName := mappingHelperFFEBEMRequest834:formatString($issuerName)
	let $issuer :=
		<bem:Issuer>
			{ if ($issuerName) then <bem:Name>{$issuerName}</bem:Name> else() }
			{ if ($orgTaxId) then <bem:TaxPayerIdentificationNumber>{mappingHelperFFEBEMRequest834:replace($orgTaxId/fn:string(), "-", "")}</bem:TaxPayerIdentificationNumber> else() }
		</bem:Issuer>

	let $issuer := mappingHelperFFEBEMRequest834:checkEmptyNode($issuer)

	let $error := mappingHelperFFEBEMRequest834:assertNotEmpty($issuer, " Issuer Node")
	return $issuer
};

declare function mappingHelperFFEBEMRequest834:mapPolicy	(
	$insurancePlanPolicy as element(po:insurancePlanPolicy)
) as element() {

    let $GroupPolicyNumber := mappingHelperFFEBEMRequest834:emptyElementToSring($insurancePlanPolicy/po:policyTrackingNumber/fn:string())
	let $MarketplaceGroupPolicyIdentifier := $insurancePlanPolicy/po:marketplaceGroupPolicyIdentifier/fn:string()
	let $PolicyStartDate := $insurancePlanPolicy/po:insurancePlanPolicyStartDate/fn:string()
	let $PolicyEndDate := $insurancePlanPolicy/po:insurancePlanPolicyEndDate/fn:string()	
	let $confirmationIndicator := $insurancePlanPolicy/po:issuerConfirmationIndicator/fn:string()
    let $supercededIndicator := $insurancePlanPolicy/po:supersededIndicator/fn:string()
	
	
	let $policyStatus := $insurancePlanPolicy/po:recordedInsurancePolicyStatus/po:definingInsurancePolicyStatusType/pb:referenceTypeCode/fn:string()
	
	let $epsPolicyStatus :=
	
	if($policyStatus = ("1","2","3","4"))
		then
	     if ($supercededIndicator eq "true") 
		    then 
			  "5"
        else
		 if ($confirmationIndicator)
			then 
			  if ($confirmationIndicator = "true")
			  then
				  switch($policyStatus)
				    case "1" return "2"
					case "2" return "2"
				    case "3" return "3"
				    case "4" return "2"
				    default return "0"
			  else
			  	  switch($policyStatus)
				    case "1" return "1"
				    case "3" return "3"
				    case "4" return "4"
				    default return "0"
		 else 
		  switch($policyStatus)
		    case "1" return "1"
		    case "2" return "2"
		    case "3" return "3"
		    case "4" return "4"
		    default return "0"
	else 
	    "0"
		
	
	let $policy :=
		<bem:PolicyInfo>
		    { 
			  <bem:GroupPolicyNumber>{$GroupPolicyNumber}</bem:GroupPolicyNumber> 
			}
			{ 
			  <bem:MarketplaceGroupPolicyIdentifier>{$MarketplaceGroupPolicyIdentifier}</bem:MarketplaceGroupPolicyIdentifier> 
			}
			{ 
			  <bem:PolicyStartDate>
					{mappingHelperFFEBEMRequest834:toDate(mappingHelperFFEBEMRequest834:formatDateYyyymmdd($PolicyStartDate))}
			  </bem:PolicyStartDate> 
			}
			{ 
			  <bem:PolicyEndDate>
					{mappingHelperFFEBEMRequest834:toDate(mappingHelperFFEBEMRequest834:formatDateYyyymmdd($PolicyEndDate))}
			  </bem:PolicyEndDate> 
			}
			{ 
			  <bem:PolicyStatus>
					{$epsPolicyStatus}
			  </bem:PolicyStatus> 
			}
		</bem:PolicyInfo>

	let $error := mappingHelperFFEBEMRequest834:assertNotEmpty($policy, " Policy Node")
	return $policy
};


declare function mappingHelperFFEBEMRequest834:mapMemberInformation($insuredMember as element()) 
{
	let $memberInfo := <bem:MemberInformation></bem:MemberInformation>

	let $subInd :=
		if ($insuredMember/po:subscriberIndicator/fn:string() = "true") then "Y"
		else "N"

	let $associationToSubscriberTypeCode :=
		if ($insuredMember/po:subscriberIndicator/fn:string() = "true")
		then
			<pb:referenceTypeCode>1</pb:referenceTypeCode>
		else
			$insuredMember/po:definingMemberAssocationTypeToSubscriber/pb:referenceTypeCode

	let $memberInfo :=
	<bem:MemberInformation>
	<bem:SubscriberIndicator>{$subInd}</bem:SubscriberIndicator>
	</bem:MemberInformation>

	let $val := $insuredMember/po:definingMemberAssocationTypeToSubscriber/pb:referenceTypeCode/fn:string()
	let $error := mappingHelperFFEBEMRequest834:assertHasChildElement($memberInfo," memberInformation")

	return $memberInfo
};

declare function mappingHelperFFEBEMRequest834:mapMemberPolicyNumber($insurancePlanPolicy as element()) {
	let $value := $insurancePlanPolicy/po:insurancePolicyNumber/fn:string()
	let $ret :=
		if($value) then
			<bem:MemberPolicyNumber>{$value}</bem:MemberPolicyNumber>
		else()
	return $ret
};

declare function mappingHelperFFEBEMRequest834:mapMemberAdditionalIdentifier(
	$exchangeAssignedMemberID as xs:string,
	$issuerAssignedMemberID as xs:string,
	$issuerAssignedSubscriberId as xs:string) 
{
	(: Required Fields: exchangeAssignedMemberID :)
	let $error := () (: mappingHelperFFEBEMRequest834:assertHasValue(<nde>{$exchangeAssignedMemberID}</nde>, " MemberAdditionalIdentifier:exchangeAssignedMemberID ") :)
	let $ret :=
		<bem:MemberAdditionalIdentifier>
			{
				if($exchangeAssignedMemberID) then
					<bem:ExchangeAssignedMemberID>{$exchangeAssignedMemberID}</bem:ExchangeAssignedMemberID>
				else ()
			}
			{
				if($issuerAssignedMemberID) then
					<bem:IssuerAssignedMemberID>{$issuerAssignedMemberID}</bem:IssuerAssignedMemberID>
				else ()
			}
			{
				if($issuerAssignedSubscriberId) then
					<bem:IssuerAssignedSubscriberID>{$issuerAssignedSubscriberId}</bem:IssuerAssignedSubscriberID>
				else()
			}			
		</bem:MemberAdditionalIdentifier>
	let $ret := mappingHelperFFEBEMRequest834:checkEmptyNode($ret)
	return $ret
};


declare function mappingHelperFFEBEMRequest834:mapMemberRelatedDates(
	$eligibilityBeginDate,
	$eligibilityEndDate ) 
{
	let $ret :=
		<bem:MemberRelatedDates>
			{
				if($eligibilityBeginDate) then
					<bem:EligibilityBeginDate>{mappingHelperFFEBEMRequest834:formatDateYyyymmdd($eligibilityBeginDate/node())}</bem:EligibilityBeginDate>
				else()
			}
			{
				if($eligibilityEndDate )then
					<bem:EligibilityEndDate>{mappingHelperFFEBEMRequest834:formatDateYyyymmdd($eligibilityEndDate/node())}</bem:EligibilityEndDate>
				else()
			}
		</bem:MemberRelatedDates>
	return $ret
};

declare function mappingHelperFFEBEMRequest834:mapMemberName(
	$coveredMember as element(), 
	$mapSSN as xs:boolean) 
{
	let $memberName := $coveredMember/po:definingMember
	(: required fields : lastName, firstName :)
	let $error := () (: mappingHelperFFEBEMRequest834:assertHasValue($memberName/po:lastName, " MemberName:LastName") :)
	let $error := () (: mappingHelperFFEBEMRequest834:assertHasValue($memberName/po:firstName, " MemberName:firstName") :)
	let $ret :=
		<bem:MemberName>
			{
				if($memberName/po:lastName) then
				<bem:LastName>{$memberName/po:lastName/fn:string()}</bem:LastName>
				else()
			}
			{
				if($memberName/po:firstName) then
				<bem:FirstName>{$memberName/po:firstName/fn:string()}</bem:FirstName>
				else()
			}
			{
				if($memberName/po:middleName) then
				<bem:MiddleName>{$memberName/po:middleName/fn:string()}</bem:MiddleName>
				else()
			}
			{
				if($memberName/po:salutationName) then
				<bem:NamePrefix>{$memberName/po:salutationName/fn:string()}</bem:NamePrefix>
				else()
			}
			{
					if($memberName/po:suffixName) then
					<bem:NameSuffix>{$memberName/po:suffixName/fn:string()}</bem:NameSuffix>
					else()
			}
			{
				if($mapSSN and $memberName/po:memberSSN) then
					let $ssnNoDash := mappingHelperFFEBEMRequest834:replace($memberName/po:memberSSN/fn:string(), "-", "")
					return
						<bem:SocialSecurityNumber>{$ssnNoDash}</bem:SocialSecurityNumber>
				else()
			}
		</bem:MemberName>
	let $ret := mappingHelperFFEBEMRequest834:checkEmptyNode($ret)
	return $ret
};

declare function mappingHelperFFEBEMRequest834:mapMemberResidenceAddress($insuredMember as element()) {

	let $subscriberIndicator := $insuredMember/po:subscriberIndicator/fn:string()
	let $homeRefTypCode := "1" (: AV: this should for Residence Address, hence changed to 1  :)
	let $homeCatTypCode := "2"
	let $mailingRefTypCode := "2"
	let $mailingCatTypCode := "1"
	let $homeAddress := (
		$insuredMember/po:definingMember/po:specifiedMemberAddress[./po:definingAddressRelationshipRoleType/pb:referenceTypeCode/fn:string() = $mailingRefTypCode
			and ./po:definingAddressCategoryType/pb:referenceTypeCode/fn:string() = $mailingCatTypCode][1] (: AV: picking the first element when there are multiple mailing address:)
			,		
		$insuredMember/po:definingMember/po:specifiedMemberAddress[./po:definingAddressRelationshipRoleType/pb:referenceTypeCode/fn:string() = $homeRefTypCode
			and ./po:definingAddressCategoryType/pb:referenceTypeCode/fn:string() = $homeCatTypCode][1] (: AV: picking the first element when there are multiple residence address:)
	)[fn:last()] 
	
	let $stateCode := ($homeAddress/po:definingAddressPlace/po:stateCode)[1]
	let $zipPlus4Code := ($homeAddress/po:definingAddressPlace/po:zipPlus4Code)[1]

	let $address := (
				
				if($stateCode) then
					<bem:StateCode>{$stateCode/fn:string()}</bem:StateCode>
				else()
				,
				if($zipPlus4Code) then
					<bem:PostalCode>{mappingHelperFFEBEMRequest834:replace($zipPlus4Code/fn:string(), "-", "")}</bem:PostalCode>
				else()
	)

	let $ret :=
		<bem:MemberResidenceAddress>
			{ for $a in $address return mappingHelperFFEBEMRequest834:checkEmptyNode($a) }
		</bem:MemberResidenceAddress>
	
	let $ret := mappingHelperFFEBEMRequest834:checkEmptyNode($ret)
    let $error := () (: mappingHelperFFEBEMRequest834:assertHasChildElement($ret, " memberResidenceAddress") :)
	return $ret
};

declare function mappingHelperFFEBEMRequest834:mapMemberDemographics($insuredMember as element()) {
	let $subscriberIndicator := $insuredMember/po:subscriberIndicator/fn:string()

	let $birthDate := $insuredMember/po:definingMember/po:memberBirthDate
	let $gender := $insuredMember/po:definingMember/po:memberGender/pb:referenceTypeCode/fn:string()
	let $genderCode :=
		if($gender = "1" or fn:lower-case($gender) = fn:lower-case("Male")) then "M"
		else if($gender = "2" or fn:lower-case($gender) = fn:lower-case("Female") ) then "F"
		else "M" (: UNKNOWN:)

	(: Required fields: birthDate and GenderCode :)
	let $error := () (: mappingHelperFFEBEMRequest834:assertHasValue($birthDate," birthDate") :)
	let $error := () (: mappingHelperFFEBEMRequest834:assertHasValue(<elm>$genderCode</elm>," genderCode") :)

	let $ret :=
		<bem:MemberDemographics>
			{
				if($birthDate) then
					<bem:BirthDate>{mappingHelperFFEBEMRequest834:formatDateYyyymmdd($birthDate/node())}</bem:BirthDate>
				else()
			}
			{
				if($genderCode) then
					<bem:GenderCode>{$genderCode}</bem:GenderCode>
				else()
			}
		</bem:MemberDemographics>

	let $ret := mappingHelperFFEBEMRequest834:checkEmptyNode($ret)
	let $error := mappingHelperFFEBEMRequest834:assertNotEmpty($ret," memberDemographics")

	return $ret
};

declare function mappingHelperFFEBEMRequest834:mapHealthCoverageInformation(
	$insurancePlanPolicy as element()
) {
	(: let $insuranceLineCode := $insurancePlan/po:definingInsuranceProductDivisionType/pb:referenceTypeCode :)
	let $insuranceLineCode := $insurancePlanPolicy/po:associatedProductDivisionType/pb:referenceTypeCode
	let $insuranceLineCode := mappingHelperFFEBEMRequest834:refMapper("insuranceProductDivisionType", $insuranceLineCode/fn:string())
	let $insuranceLineCode :=
		if($insuranceLineCode) then $insuranceLineCode
		else "HLT" (: todo: default to HLT for now - according to Lakshmi :)
	let $insuranceLineCode := <elm>{$insuranceLineCode}</elm>
	
	(: required fields : insuranceLineCode and healthCoverageMaintenanceTypeCode :)
	let $error := () (: mappingHelperFFEBEMRequest834:assertHasValue($insuranceLineCode," HealthCoverageInformation:insuranceLineCode") :)
	
	let $ret :=
		<bem:HealthCoverageInformation>
			{
				if($insuranceLineCode) then
					<bem:InsuranceLineCode>{$insuranceLineCode/node()}</bem:InsuranceLineCode>
				else()
			}
		</bem:HealthCoverageInformation>

	let $ret := mappingHelperFFEBEMRequest834:checkEmptyNode($ret)

	let $error := mappingHelperFFEBEMRequest834:assertNotEmpty($ret, " mapHealthCoverageInformation")
	return $ret
};

declare function mappingHelperFFEBEMRequest834:mapHealthCoverageDates($coveredInsuredMember as element()) {
let $benefitBeginDate := $coveredInsuredMember/po:coverageStartDate
(: required fields : BenefitBeginDate :)
let $benefitEndDate := $coveredInsuredMember/po:coverageEndDate
(: AV: changes to map BenefitEndDate to coverageEndDate :)
let $error := mappingHelperFFEBEMRequest834:assertNotEmpty(<elt>$benefitBeginDate</elt>," HealthCoverageDates:BenefitBeginDate")
let $error := mappingHelperFFEBEMRequest834:assertNotEmpty(<elt>$benefitEndDate</elt>," HealthCoverageDates:BenefitEndDate")
let $ret:=
<bem:HealthCoverageDates>
{
if ($benefitBeginDate) then
<bem:BenefitBeginDate>{mappingHelperFFEBEMRequest834:formatDateYyyymmdd($benefitBeginDate/node())}</bem:BenefitBeginDate>
else ()
}
{
if ($benefitEndDate) then
<bem:BenefitEndDate>{mappingHelperFFEBEMRequest834:formatDateYyyymmdd($benefitEndDate/node())}</bem:BenefitEndDate>
else ()
}
</bem:HealthCoverageDates>
let $ret := mappingHelperFFEBEMRequest834:checkEmptyNode($ret)

let $error := mappingHelperFFEBEMRequest834:assertNotEmpty($ret," HealthCoverageDates")

return $ret
};

declare function mappingHelperFFEBEMRequest834:maphealthCoveragePolicyNumber($insurancePlanPolicy) {
let $selectedInsurancePlan := mappingHelperFFEBEMRequest834:emptyElementToSring($insurancePlanPolicy/po:selectedInsurancePlan/fn:string())
let $planVariant := mappingHelperFFEBEMRequest834:emptyElementToSring($insurancePlanPolicy/po:definingPlanVarianceComponentType/pb:referenceTypeCode/fn:string())
let $planVariant :=
if(fn:string-length($planVariant) eq 1) then "0" || $planVariant
else $planVariant
let $contractCode := $selectedInsurancePlan || $planVariant (: 16 characters total :)
let $internalControlNumber := $insurancePlanPolicy/po:insurancePolicyNumber/fn:string()
let $ret :=
<bem:HealthCoveragePolicyNumber>
{
if ($contractCode) then
<bem:ContractCode>{$contractCode}</bem:ContractCode>
else ()
}
{
if ($internalControlNumber) then
<bem:InternalControlNumber>{$internalControlNumber}</bem:InternalControlNumber>
else ()
}
</bem:HealthCoveragePolicyNumber>
let $ret := mappingHelperFFEBEMRequest834:checkEmptyNode($ret)

return $ret

};
declare function mappingHelperFFEBEMRequest834:refMapper($refType as xs:string, $ffmKey) as xs:string ?{
let $insuranceProductDivisionType := map:map()
let $_ := map:put($insuranceProductDivisionType, "1", "HLT")
let $_ := map:put($insuranceProductDivisionType, "2", "DEN")
let $_ := map:put($insuranceProductDivisionType, "3", "VIS")

let $memberAssociationType := map:map()
let $_ := map:put($memberAssociationType, "1", "18")
let $_ := map:put($memberAssociationType, "2", "01")
let $_ := map:put($memberAssociationType, "3", "03")
let $_ := map:put($memberAssociationType, "4", "19")
let $_ := map:put($memberAssociationType, "5", "17")
let $_ := map:put($memberAssociationType, "6", "05")
let $_ := map:put($memberAssociationType, "7", "14")
let $_ := map:put($memberAssociationType, "8", "53")
let $_ := map:put($memberAssociationType, "9", "19")
let $_ := map:put($memberAssociationType, "10", "G8")
let $_ := map:put($memberAssociationType, "11", "G9")
let $_ := map:put($memberAssociationType, "12", "16")
let $_ := map:put($memberAssociationType, "13", "06")
let $_ := map:put($memberAssociationType, "14", "07")
let $_ := map:put($memberAssociationType, "15", "04")
let $_ := map:put($memberAssociationType, "16", "08")
let $_ := map:put($memberAssociationType, "17", "G8")
let $_ := map:put($memberAssociationType, "18", "G8")
let $_ := map:put($memberAssociationType, "19", "G8")
let $_ := map:put($memberAssociationType, "20", "G8")
let $_ := map:put($memberAssociationType, "21", "09")
let $_ := map:put($memberAssociationType, "22", "60")
let $_ := map:put($memberAssociationType, "23", "12")
let $_ := map:put($memberAssociationType, "24", "38")
let $_ := map:put($memberAssociationType, "25", "31")
let $_ := map:put($memberAssociationType, "26", "11")
let $_ := map:put($memberAssociationType, "27", "25")
let $_ := map:put($memberAssociationType, "28", "10")
let $_ := map:put($memberAssociationType, "29", "26")
let $_ := map:put($memberAssociationType, "30", "13")
let $_ := map:put($memberAssociationType, "31", "02")
let $_ := map:put($memberAssociationType, "32", "15")


let $languageUseIndicator := map:map()
let $_ := map:put($languageUseIndicator, "1", "5")
let $_ := map:put($languageUseIndicator, "2", "7")
let $_ := map:put($languageUseIndicator, "3", "6")

let $languageCode := map:map()
let $_ := map:put($languageCode, "1", "eng")
let $_ := map:put($languageCode, "2", "spa")
let $_ := map:put($languageCode, "3", "vie")
let $_ := map:put($languageCode, "4", "tgl")
let $_ := map:put($languageCode, "5", "rus")
let $_ := map:put($languageCode, "6", "por")
let $_ := map:put($languageCode, "7", "ara")
let $_ := map:put($languageCode, "8", "chi")
let $_ := map:put($languageCode, "9", "fre")
let $_ := map:put($languageCode, "10", "cpf")
let $_ := map:put($languageCode, "11", "ger")
let $_ := map:put($languageCode, "12", "guj")
let $_ := map:put($languageCode, "13", "hin")
let $_ := map:put($languageCode, "14", "kor")
let $_ := map:put($languageCode, "15", "pol")
let $_ := map:put($languageCode, "16", "urd")

let$raceCode := map:map()
let $_ := map:put($raceCode, "1", "1002-5")
let $_ := map:put($raceCode, "2", "2029-7")
let $_ := map:put($raceCode, "3", "2054-5")
let $_ := map:put($raceCode, "4", "2034-7")
let $_ := map:put($raceCode, "5", "2036-2")
let $_ := map:put($raceCode, "6", "2086-7")
let $_ := map:put($raceCode, "7", "2039-6")
let $_ := map:put($raceCode, "8", "2040-4")
let $_ := map:put($raceCode, "9", "2079-2")
let $_ := map:put($raceCode, "10", "2028-9")
let $_ := map:put($raceCode, "11", "2500-7")
let $_ := map:put($raceCode, "12", "2080-0")
let $_ := map:put($raceCode, "13", "2047-9")
let $_ := map:put($raceCode, "14", "2106-3")
let $_ := map:put($raceCode, "15", "2131-1")

let$ethnicityCode := map:map()
let $_ := map:put($ethnicityCode, "1", "2182-4")
let $_ := map:put($ethnicityCode, "2", "2148-5")
let $_ := map:put($ethnicityCode, "3", "2180-8")


let $bemValue :=
if(fn:not($ffmKey)) then ""
else if($refType = "insuranceProductDivisionType") then map:get($insuranceProductDivisionType, $ffmKey)
else if($refType = "memberAssociationType") then map:get($memberAssociationType, $ffmKey)
else if($refType = "languageUseIndicator") then map:get($languageUseIndicator, $ffmKey)
else if($refType = "languageCode") then map:get($languageCode, $ffmKey)
else if($refType = "raceCode") then map:get($raceCode, $ffmKey)
else if($refType = "ethnicityCode") then map:get($ethnicityCode, $ffmKey)
else ()

return $bemValue
};

declare function mappingHelperFFEBEMRequest834:mapAdditionalInfo(
$aptcAmount,
$csrAmount,
$totalPremiumAmount,
$totalIndividualResponsibilityAmount,
$ratingArea,
$effectiveStartDate,
$effectiveEndDate,
$proratedMonthlyPremiumAmount,
$proratedIndividualResponsibleAmount,
$proratedAppliedAPTCAmount,
$proratedCSRAmount,
$proratedMonth, 
$proratedAmountStartDate, 
$proratedAmountEndDate, 
$proratedCSRMonth, 
$proratedCSRStartDate, 
$proratedCSREndDate
 ) as element() *{
 
 
 
 
let $ret :=
<bem:AdditionalInfo>
{
if ($proratedAmountStartDate) then
	<bem:EffectiveStartDate>{mappingHelperFFEBEMRequest834:formatDateYyyymmdd($proratedAmountStartDate)}</bem:EffectiveStartDate>
else if($effectiveStartDate) then
	<bem:EffectiveStartDate>{mappingHelperFFEBEMRequest834:formatDateYyyymmdd($effectiveStartDate)}</bem:EffectiveStartDate>
else()
}
{
if ($proratedAmountEndDate) then
	<bem:EffectiveEndDate>{mappingHelperFFEBEMRequest834:formatDateYyyymmdd($proratedAmountEndDate)}</bem:EffectiveEndDate>
else if($effectiveEndDate) then
	<bem:EffectiveEndDate>{mappingHelperFFEBEMRequest834:formatDateYyyymmdd($effectiveEndDate)}</bem:EffectiveEndDate>
else()
}
{
if ($totalPremiumAmount) then
<bem:TotalPremiumAmount>{mappingHelperFFEBEMRequest834:formatCurrency($totalPremiumAmount)}</bem:TotalPremiumAmount>
else <bem:TotalPremiumAmount>0</bem:TotalPremiumAmount>
}
{
if ($totalIndividualResponsibilityAmount) then
<bem:TotalIndividualResponsibilityAmount>{mappingHelperFFEBEMRequest834:formatCurrency($totalIndividualResponsibilityAmount)}</bem:TotalIndividualResponsibilityAmount>
else <bem:TotalIndividualResponsibilityAmount>0</bem:TotalIndividualResponsibilityAmount>
}
{
if ($ratingArea) then
<bem:RatingArea>{"R-" || $ratingArea}</bem:RatingArea>
else ()
}
{
if ($aptcAmount) then
<bem:APTCAmount>{mappingHelperFFEBEMRequest834:formatCurrency($aptcAmount)}</bem:APTCAmount>
else ()
}
{
if ($csrAmount) then
<bem:CSRAmount>{mappingHelperFFEBEMRequest834:formatCurrency($csrAmount)}</bem:CSRAmount>
else ()
}

{
if ($proratedMonthlyPremiumAmount) then
<bem:ProratedMonthlyPremiumAmount>{mappingHelperFFEBEMRequest834:formatCurrency($proratedMonthlyPremiumAmount)}</bem:ProratedMonthlyPremiumAmount>
else ()
}
{
if ($proratedIndividualResponsibleAmount) then
<bem:ProratedIndividualResponsibleAmount>{mappingHelperFFEBEMRequest834:formatCurrency($proratedIndividualResponsibleAmount)}</bem:ProratedIndividualResponsibleAmount>
else ()
}
{
if ($proratedAppliedAPTCAmount) then
<bem:ProratedAppliedAPTCAmount>{mappingHelperFFEBEMRequest834:formatCurrency($proratedAppliedAPTCAmount)}</bem:ProratedAppliedAPTCAmount>
else ()
}
{
if ($proratedCSRAmount) then
<bem:ProratedCSRAmount>{mappingHelperFFEBEMRequest834:formatCurrency($proratedCSRAmount)}</bem:ProratedCSRAmount>
else ()
}
</bem:AdditionalInfo> 

let $ret := if(fn:count($ret) gt 0) then $ret else()

return $ret
};

declare function mappingHelperFFEBEMRequest834:generateControlNumber() as xs:integer ? {
(: TODO: insuredMemberIdentifier : this must be updated :)
let $kvm := map:map()
let $_ := map:put($kvm, "tenantId", "VA")
let $policyTrackingNumber := bizidgen:nextScoped("ControlNumber", $kvm, 1)
return $policyTrackingNumber
};

(: Build TransactionInformation :)
declare function mappingHelperFFEBEMRequest834:buildTransactionInformation($insurancePlanPolicy as element(po:insurancePlanPolicy), $controlNumber) {
(: Values are hardcode temporarily, Mapping needs to established to figure out the values :)

let $policyTransactionEventDate := mappingHelperFFEBEMRequest834:addOneSecToLastModified($insurancePlanPolicy/base:lastModified/fn:string())
let $exchangeCode := "Individual" (: Individual or SHOP:)
let $controlNumber := "TO BE SET"
let $lastModified := $insurancePlanPolicy/base:lastModified/fn:string()
let $versionNumber := $insurancePlanPolicy/base:versionInformation/pb:versionNumber/fn:string()

let $policyTransactionEventDate :=
if (fn:not(fn:empty($policyTransactionEventDate))) then $policyTransactionEventDate
else
fn:current-dateTime()

let $TransactionInfo := mappingHelperFFEBEMRequest834:mapTransactionInfo($controlNumber, $policyTransactionEventDate, $exchangeCode, $lastModified, $versionNumber)

return $TransactionInfo

};

declare function mappingHelperFFEBEMRequest834:mapTransactionInfo ($controlNumber,
$currentTimeStamp,
$exchangeCode,
$lastModified,
$versionNumber){

let $ret := <bem:TransactionInformation>
{
if ($controlNumber) then
<bem:ControlNumber>{$controlNumber}</bem:ControlNumber>
else
()
}
{
if (fn:not(fn:empty($currentTimeStamp))) then
<bem:CurrentTimeStamp>{$currentTimeStamp}</bem:CurrentTimeStamp>
else
()
}
{
if ($exchangeCode) then
<bem:ExchangeCode>{$exchangeCode}</bem:ExchangeCode>
else
()
}
{
if ($versionNumber) then
<bem:PolicySnapshotVersionNumber>{$versionNumber}</bem:PolicySnapshotVersionNumber>
else
()
}
{
if ($lastModified) then
<bem:PolicySnapshotDateTime>{$lastModified}</bem:PolicySnapshotDateTime>
else
()
}
</bem:TransactionInformation>

let $ret := mappingHelperFFEBEMRequest834:checkEmptyNode($ret)

return $ret
};


declare function mappingHelperFFEBEMRequest834:mapExtractionStatusInfo ($error as xs:string?){

let $ret := <bem:ExtractionStatus>
{
if ($error) then
<bem:ExtractionStatusCode>1</bem:ExtractionStatusCode>
else
<bem:ExtractionStatusCode>0</bem:ExtractionStatusCode>
}
{
if ($error) then
<bem:ExtractionStatusText>{$error}</bem:ExtractionStatusText>
else
()
}
</bem:ExtractionStatus>

let $ret := mappingHelperFFEBEMRequest834:checkEmptyNode($ret)

return $ret
};


(: Build file information object (note all static values right now cause not in PCM) :)
declare function mappingHelperFFEBEMRequest834:buildFileInformation($insurancePlanPolicy as element(po:insurancePlanPolicy), $tenantId as xs:string ) as element() {
(: Not Needed for initial :)
let $interchangeControlNumber := "123456789"
let $interchangeSenderId := "SenderId"
let $interchangeReceiverId := "RecivId"


let $groupSenderId :=
if(fn:string-length($tenantId) ne 3) then $tenantId || "0"
else $tenantId

let $groupReceiverId := $insurancePlanPolicy/po:selectedInsurancePlan/fn:string()
let $groupControlNumber := fn:format-date(fn:current-date(), "[Y0001][M01][D01]")
let $versionNumber := "23"

(: Required Fields : groupSender, groupReceiver, groupControlNumber, versionNumber :)
let $error := () (: mappingHelperFFEBEMRequest834:assertHasValue(<gsid>{$groupSenderId}</gsid>, " GroupSenderID ") :)
let $error := () (: mappingHelperFFEBEMRequest834:assertHasValue(<grid>{$groupReceiverId}</grid>, " GroupReceiverID ") :)

let $ret := mappingHelperFFEBEMRequest834:mapFileInformation ($groupSenderId,
$groupReceiverId,
$groupControlNumber,
$versionNumber )
return $ret
};

declare function mappingHelperFFEBEMRequest834:mapFileInformation($groupSenderId,
$groupReceiverId,
$groupControlNumber,
$versionNumber) {

let $ret := <bem:FileInformation>
{
if ($groupSenderId) then
<bem:GroupSenderID>{$groupSenderId}</bem:GroupSenderID>
else ()
}
{
if ($groupReceiverId) then
<bem:GroupReceiverID>{$groupReceiverId}</bem:GroupReceiverID>
else ()
}
{
if ($groupControlNumber) then
<bem:GroupControlNumber>{$groupControlNumber}</bem:GroupControlNumber>
else ()
}
{
if($versionNumber) then
<bem:VersionNumber>{$versionNumber}</bem:VersionNumber>
else ()
}
</bem:FileInformation>

let $ret := mappingHelperFFEBEMRequest834:checkEmptyNode($ret)

return $ret
};


declare function mappingHelperFFEBEMRequest834:mapMemberForIntialEnrollment(
	$coveredInsuredMember as element(),
	$policySubscriberId as xs:string,
	$insurancePlanPolicy as element(),
	$mode
) {

	let $memberInformation := ()
	let $subscriberIdElement := ()
	let $memberAdditionalIdentifier := ()
	let $memberRelatedDates := ()
	let $healthCoverage := ()
	let $additionalInfo := ()

	(: memberNameInformation elements :)
	let $memberName := ()
	let $memberResidenceAddress := ()
	let $memberDemographics := ()
	let $subscriberIndicator := $coveredInsuredMember/po:subscriberIndicator/fn:string()

	return
	  try {

		(: Anand Vedam -- EffectuationIndicator CR changes :)
		let $confirmationIndicator := $insurancePlanPolicy/po:issuerConfirmationIndicator/fn:string()

		(: Fix to resolve issue with ERL Extraction, to avoid incorrect MTC codes in 834.
			 Anand Vedam PolicyStatus logic here. :)
		let $policyStatus := $insurancePlanPolicy/po:recordedInsurancePolicyStatus/po:definingInsurancePolicyStatusType/pb:referenceTypeCode/fn:string()
		
		(: Fix to resolve issue with ERL Extraction, to avoid incorrect MRC codes in 834.
			 Anand Vedam PolicyStatus logic here. :)
		let $ippMaintenanceReasonTypeCode := $insurancePlanPolicy/po:recordedInsurancePolicyStatus/po:recordedMaintenanceTypeReason/po:definingMaintenanceReasonType/pb:referenceTypeCode/fn:string()
		let $maintenanceReasonCode := ""
	(:		if($ippMaintenanceReasonTypeCode) then $ippMaintenanceReasonTypeCode
			else "EC"
	:)

		let $_ := xdmp:set($memberName, mappingHelperFFEBEMRequest834:mapMemberName($coveredInsuredMember, fn:true()))
		let $_ := xdmp:set($memberResidenceAddress, mappingHelperFFEBEMRequest834:mapMemberResidenceAddress($coveredInsuredMember))
		let $_ := xdmp:set($memberDemographics, mappingHelperFFEBEMRequest834:mapMemberDemographics($coveredInsuredMember))
		let $_ := xdmp:set($memberInformation, mappingHelperFFEBEMRequest834:mapMemberInformation($coveredInsuredMember))
		
		let $_ := xdmp:set($subscriberIdElement,
			if($policySubscriberId) then <bem:SubscriberID>{$policySubscriberId}</bem:SubscriberID>
			else()
		)

		(: todo memberPolicyNumber :)

		let $issuerAssignedSubscriberId := if($insurancePlanPolicy/po:issuerAssignedSubscriberIdentifier) then
					 							$insurancePlanPolicy/po:issuerAssignedSubscriberIdentifier/fn:string() 
				  							else ("")
											
        let $issuerAssignedMemberId := if($coveredInsuredMember/po:issuerInsuredMemberIdentifier) then
					 							$coveredInsuredMember/po:issuerInsuredMemberIdentifier/fn:string() 
				  							else ("")											

		let $_ := xdmp:set($memberAdditionalIdentifier,
			mappingHelperFFEBEMRequest834:mapMemberAdditionalIdentifier($coveredInsuredMember/po:insuredMemberIdentifier/fn:string(),
			$issuerAssignedMemberId,
			$issuerAssignedSubscriberId)
		)
		let $_ := xdmp:set($memberRelatedDates, mappingHelperFFEBEMRequest834:mapMemberRelatedDates($coveredInsuredMember/po:coverageStartDate,
			$coveredInsuredMember/po:coverageEndDate (: AV: Changes to map PolicyEndDate :) )
		)

		let $subSpecifyingPerson := mappingHelperFFEBEMRequest834:getPolicySpecifyingPerson($insurancePlanPolicy)

		let $healthCoverageInformation := mappingHelperFFEBEMRequest834:mapHealthCoverageInformation($insurancePlanPolicy)
		let $healthCoverageDates := mappingHelperFFEBEMRequest834:mapHealthCoverageDates($coveredInsuredMember)
		let $healthCoveragePolicyNumber := mappingHelperFFEBEMRequest834:maphealthCoveragePolicyNumber($insurancePlanPolicy)
		let $_ := xdmp:set($healthCoverage,
			<bem:HealthCoverage>
				{
					mappingHelperFFEBEMRequest834:checkEmptyNode($healthCoverageInformation)
				}
				{
					mappingHelperFFEBEMRequest834:checkEmptyNode($healthCoverageDates)
				}
				{
					mappingHelperFFEBEMRequest834:checkEmptyNode($healthCoveragePolicyNumber)
				}
			</bem:HealthCoverage>
		)
		let $_ := xdmp:set($healthCoverage, mappingHelperFFEBEMRequest834:checkEmptyNode($healthCoverage))

		(: todo additionalInfo :)

		(: for initial - retrieve only the first calculatedInsurancePolicyPremium :)

		let $premiumAmount1 := $coveredInsuredMember/po:memberPolicyPremium[fn:last()]/po:monthlyPolicyPremiumAmount/node()
		let $premiumAmount1 := mappingHelperFFEBEMRequest834:emptyElementToSring($premiumAmount1)

		let $totalPremAmount :=
			if ($subscriberIndicator = "true") then
				$insurancePlanPolicy/po:calculatedInsurancePolicyPremium[fn:last()]/po:monthlyPolicyPremiumAmount/node()
			else()
		let $totalPremAmount := mappingHelperFFEBEMRequest834:emptyElementToSring($totalPremAmount)

		let $totalResponAmount :=
			if ($subscriberIndicator = "true") then
				$insurancePlanPolicy/po:calculatedInsurancePolicyPremium[fn:last()]/po:individualResponsibleAmount/node()
			else()
		let $totalResponAmount := mappingHelperFFEBEMRequest834:emptyElementToSring($totalResponAmount)

		let $aptcAmount :=
			if ($subscriberIndicator = "true") then
				$insurancePlanPolicy/po:calculatedInsurancePolicyPremium[fn:last()]/po:appliedAPTCAmount/node()
			else()
		let $aptcAmount := mappingHelperFFEBEMRequest834:emptyElementToSring($aptcAmount)

		let $csrAmount :=
			if ($subscriberIndicator = "true") then
				if(fn:exists($insurancePlanPolicy/po:applicableCostSharingReduction[1]/po:csrAmount/node())) then
				    $insurancePlanPolicy/po:applicableCostSharingReduction[1]/po:csrAmount/node()
				else ( 0 )
			else()
		let $csrAmount := mappingHelperFFEBEMRequest834:emptyElementToSring($csrAmount)

		let $ratingArea :=
			if ($subscriberIndicator = "true") then
				$insurancePlanPolicy/po:calculatedInsurancePolicyPremium[fn:last()]/po:exchangeRateAreaReference/node()
			else()
		let $ratingArea := mappingHelperFFEBEMRequest834:emptyElementToSring($ratingArea)

		let $effectiveStartDate := $insurancePlanPolicy/po:insurancePlanPolicyStartDate/node()
		let $effectiveStartDate := mappingHelperFFEBEMRequest834:emptyElementToSring($effectiveStartDate)

		let $effectiveEndDate := $insurancePlanPolicy/po:insurancePlanPolicyEndDate/node()
		let $effectiveEndDate := mappingHelperFFEBEMRequest834:emptyElementToSring($effectiveEndDate)

		let $ippMaintenanceTypeReasonText := $insurancePlanPolicy/po:recordedInsurancePolicyStatus/po:recordedMaintenanceTypeReason/po:maintenanceTypeReasonText/fn:string()
		let $ippMaintenanceTypeReasonTextToken := fn:tokenize($ippMaintenanceTypeReasonText[fn:last()], "-")

		let $specialEnrollmentPeriodReason := 
			if($ippMaintenanceTypeReasonTextToken) then
				if(fn:count($ippMaintenanceTypeReasonTextToken) eq 2) then
					$ippMaintenanceTypeReasonTextToken[fn:last()]
				else ()
		else ()
		    
		let $dayOfEffectiveDate := fn:day-from-date(mappingHelperFFEBEMRequest834:toDate($effectiveStartDate))
		let $prorationNode := $insurancePlanPolicy/po:calculatedInsurancePolicyPremium[fn:last()]/po:calculatedPolicyPremiumProration

		let $additionalInfoEffectiveStartDate :=
			if ($prorationNode) then
				$insurancePlanPolicy/po:calculatedInsurancePolicyPremium[fn:last()]/po:effectiveStartDate/node()
			else $effectiveStartDate
		(:
			fn:string(xs:dateTime(mappingHelperFFEBEMRequest834:toDate($effectiveStartDate))) (: no change since 1st of month :)
		:)

		let $additionalMaintenanceReason := ()
		
		let $_ := xdmp:set($additionalInfo,

			mappingHelperFFEBEMRequest834:mapAdditionalInfo(
				$aptcAmount,
				$csrAmount,
				$totalPremAmount,
				$totalResponAmount,
				$ratingArea,
				$additionalInfoEffectiveStartDate,
				$effectiveEndDate, (: MK: not needed for all transactions: I/T/C :)
				"", "", "", "", "", "", "", "", "", ""
			)
		)
		let $_ := fn:trace( ("mappingHelperFFEBEMRequest834: dump of mapped $additionalInfo: ", $additionalInfo), "mappingHelperFFEBEMRequest834" )

		let $member := (
			  	element bem:Member {
					mappingHelperFFEBEMRequest834:oneOrNone($memberInformation),
					mappingHelperFFEBEMRequest834:oneOrNone($subscriberIdElement),
					mappingHelperFFEBEMRequest834:oneOrNone($memberAdditionalIdentifier),
					mappingHelperFFEBEMRequest834:oneOrNone($memberRelatedDates),
					if ($memberName or $memberResidenceAddress or $memberResidenceAddress or $memberDemographics) then
						element bem:MemberNameInformation {
							mappingHelperFFEBEMRequest834:oneOrNone($memberName),
							$memberResidenceAddress,
							$memberDemographics						
						}
					else (),
					$healthCoverage,
					if ($subscriberIndicator = "true") then $additionalInfo
					else ()
					 
				}
				,
				for $proration in $insurancePlanPolicy/po:calculatedInsurancePolicyPremium[fn:last()]/po:calculatedPolicyPremiumProration
					(:po:calculatedPolicyPremiumProration exists = subscriber = "Y":)
				  	
					let $proratedMonthlyPremiumAmount := $proration/po:proratedMonthlyPremiumAmount/node()
					let $proratedIndividualResponsibleAmount :=  $proration/po:proratedIndividualResponsibleAmount/node()
					let $proratedAppliedAPTCAmount := $proration/po:proratedAppliedAPTCAmount/node()
					
					let $proratedMonth := $proration/po:proratedMonth/node()
					let $proratedAmountStartDate := $proration/po:proratedAmountStartDate/node()
					let $proratedAmountEndDate := $proration/po:proratedAmountEndDate/node()
					
					(: CSR Proration corresponding to the calculatedPolicyPremiumProration:)
					let $csrProration := $insurancePlanPolicy/po:applicableCostSharingReduction/po:calculatedPolicyCSRProration[fn:string(./po:proratedCSRStartDate) = fn:string($proration/po:proratedAmountStartDate)]
					let $proratedCSRMonth := $csrProration/po:proratedCSRMonth/node()
					let $proratedCSRStartDate :=  $csrProration/po:proratedCSRAmount/node()
					let $proratedCSREndDate := $csrProration/po:proratedCSRAmount/node()
					let $proratedCSRAmount := $csrProration/po:proratedCSRAmount/node()
					return
						if ($subscriberIndicator = "true") then
						  	element bem:Member {
								mappingHelperFFEBEMRequest834:oneOrNone($memberInformation),
								mappingHelperFFEBEMRequest834:mapAdditionalInfo(
									$aptcAmount,
									$csrAmount,
									$totalPremAmount,
									$totalResponAmount,
									$ratingArea,
									$additionalInfoEffectiveStartDate,
									(), (: MROYAL: This field must be blank for prorated value to be used, MK: not needed for all transactions: I/T/C :)
									$proratedMonthlyPremiumAmount,
									$proratedIndividualResponsibleAmount,
									$proratedAppliedAPTCAmount,
									$proratedCSRAmount,
									$proratedMonth, 
									$proratedAmountStartDate,
									$proratedAmountEndDate, 
									$proratedCSRMonth,
									$proratedCSRStartDate, 
									$proratedCSREndDate
								)
					
							}
						else ()

		)
		let $_ := fn:trace( ("mappingHelperFFEBEMRequest834: dump of mapped $member: ", $member), "mappingHelperFFEBEMRequest834" )

		let $member := mappingHelperFFEBEMRequest834:checkEmptyNode($member)

		return $member

	  } catch ($exception) {
		  let $errName := $exception/error:name/fn:string()
		  let $errCode := $exception/error:code/fn:string()
		  let $memberObject := 	
		  	if ($memberInformation or $subscriberIdElement or $memberAdditionalIdentifier or $memberRelatedDates or $memberName or
			$memberResidenceAddress or $memberResidenceAddress or $memberDemographics or $healthCoverage or $additionalInfo) then
			  	element bem:Member {
					mappingHelperFFEBEMRequest834:oneOrNone($memberInformation),
					mappingHelperFFEBEMRequest834:oneOrNone($subscriberIdElement),
					mappingHelperFFEBEMRequest834:oneOrNone($memberAdditionalIdentifier),
					mappingHelperFFEBEMRequest834:oneOrNone($memberRelatedDates),
					if ($memberName or $memberResidenceAddress or $memberResidenceAddress or $memberDemographics) then
						element bem:MemberNameInformation {
							mappingHelperFFEBEMRequest834:oneOrNone($memberName),
							$memberResidenceAddress,
							$memberDemographics
						}
					else (),
					$healthCoverage,
					if ($subscriberIndicator = "true") then $additionalInfo
					else ()
				}

			else ()
		  return fn:error(
		  	if (fn:string(xdmp:type($errName)) eq "QName") then $errName else (), 
		  	$errCode, 
		  	xdmp:quote(element partial { $memberObject, $exception }) )
	  }
};

declare function mappingHelperFFEBEMRequest834:retrieveIssuerOrganization($hiosId as xs:string) {
let $orgRefTypeCode := "6"

let $hiosid-query as cts:query :=
cts:element-query (
xs:QName("po:issuerOrganization"),
cts:element-value-query(
xs:QName("po:issuerHIOSID"),
$hiosId
)
)

let $orgType-query as cts:query :=
cts:element-query (
xs:QName("po:organizationType"),
cts:element-value-query(
xs:QName("pb:referenceTypeCode"),
$orgRefTypeCode
)
)

let $combined-query := cts:and-query( ($hiosid-query, $orgType-query) )

let $result :=
hixcrud:search("global",
hixOrganization:GetBasePath(),
$combined-query
)
let $howManyReturned := fn:count($result)

return $howManyReturned
};


declare function mappingHelperFFEBEMRequest834:mockIssuer() as element() {
let $issuerName := "NOTAVAILABLE"
let $issuerTIN := "123456789"
let $issuer := <bem:Issuer></bem:Issuer>
let $issuer :=
<bem:Issuer>
{ if ($issuerName) then <bem:Name>{$issuerName}</bem:Name> else() }
{ if ($issuerTIN) then <bem:TaxPayerIdentificationNumber>{mappingHelperFFEBEMRequest834:replace($issuerTIN, "-", "")}</bem:TaxPayerIdentificationNumber> else() }
</bem:Issuer>

let $issuer :=
if($issuer/node()) then $issuer
else ()
let $issuer := mappingHelperFFEBEMRequest834:checkEmptyNode($issuer)

let $error := mappingHelperFFEBEMRequest834:assertNotEmpty($issuer, " Issuer Node")

return $issuer
};

declare function mappingHelperFFEBEMRequest834:compareResidenceAndMailingAddress($homeAddress as element(), $mailAddress as element()) as xs:integer {

let $streetName1 := $mailAddress/po:definingAddressPlace/po:streetName1
let $streetName2 := $mailAddress/po:definingAddressPlace/po:streetName2
let $cityName := $mailAddress/po:definingAddressPlace/po:cityName
let $stateCode := $mailAddress/po:definingAddressPlace/po:stateCode
let $zipPlus4Code := $mailAddress/po:definingAddressPlace/po:zipPlus4Code
(: let $countryCode := $mailAddress/po:definingAddressPlace/po:countryCode :)

let $mAddress := fn:concat($streetName1, $streetName2, $cityName, $stateCode, $zipPlus4Code)

let $streetName1 := $homeAddress/po:definingAddressPlace/po:streetName1
let $streetName2 := $homeAddress/po:definingAddressPlace/po:streetName2
let $cityName := $homeAddress/po:definingAddressPlace/po:cityName
let $stateCode := $homeAddress/po:definingAddressPlace/po:stateCode
let $zipPlus4Code := $homeAddress/po:definingAddressPlace/po:zipPlus4Code
(: let $countryCode := $homeAddress/po:definingAddressPlace/po:countryCode :)

let $rAddress := fn:concat($streetName1, $streetName2, $cityName, $stateCode, $zipPlus4Code)
let $ret := fn:compare($rAddress, $mAddress)
return $ret
};

declare function mappingHelperFFEBEMRequest834:mapParentName($insuredMember as element(), $section as xs:string) {

	let $memberName := $insuredMember

	let $ret :=
		element {$section}
		{
			if($memberName/po:lastName) then
				<bem:LastName>{$memberName/po:lastName/fn:string()}</bem:LastName>
			else()
			,
			if($memberName/po:firstName) then
				<bem:FirstName>{$memberName/po:firstName/fn:string()}</bem:FirstName>
			else()
			,
			if($memberName/po:middleName) then
				<bem:MiddleName>{$memberName/po:middleName/fn:string()}</bem:MiddleName>
			else()
			,
			if($memberName/po:salutationName) then
				<bem:NamePrefix>{$memberName/po:salutationName/fn:string()}</bem:NamePrefix>
			else()
			,
			if($memberName/po:suffixName) then
				<bem:NameSuffix>{$memberName/po:suffixName/fn:string()}</bem:NameSuffix>
			else()
		}


	let $ret := mappingHelperFFEBEMRequest834:checkEmptyNode($ret)

	return $ret
};


declare function mappingHelperFFEBEMRequest834:mapParentCommunicationInformation($insuredMember as element(), $section as xs:string){
let $homeTelRefCode := "2"
let $cellTelRefCode := "3"
let $beeperTelRefCode := "5"

let $telephoneNumber := $insuredMember/po:specifiedMemberTelephone[./po:specifyingContactMethodType/pb:referenceTypeCode=$homeTelRefCode]/po:specifyingTelephoneNumber/po:fullNumberCode
let $alternateTelephoneNumber := $insuredMember/po:specifiedMemberTelephone[./po:specifyingContactMethodType/pb:referenceTypeCode=$cellTelRefCode]/po:specifyingTelephoneNumber/po:fullNumberCode
let $beeperNumber := $insuredMember/po:specifiedMemberTelephone[./po:specifyingContactMethodType/pb:referenceTypeCode=$beeperTelRefCode]/po:specifyingTelephoneNumber/po:fullNumberCode

let $emailId := $insuredMember/po:memberEmailAddress/po:emailAddressFullText

let $ret :=
element {$section}
{
if($telephoneNumber/fn:string()) then
<bem:TelephoneNumber>{mappingHelperFFEBEMRequest834:replace($telephoneNumber/fn:string(), "-", "")}</bem:TelephoneNumber>
else if($alternateTelephoneNumber/fn:string()) then
<bem:TelephoneNumber>{mappingHelperFFEBEMRequest834:replace($alternateTelephoneNumber/fn:string(), "-", "")}</bem:TelephoneNumber>
else
()
,
if($alternateTelephoneNumber/fn:string()) then
<bem:AlternateTelephoneNumber>{mappingHelperFFEBEMRequest834:replace($alternateTelephoneNumber/fn:string(), "-", "")}</bem:AlternateTelephoneNumber>
else
()
,
if($emailId/fn:string()) then
<bem:EmailID>{$emailId/fn:string()}</bem:EmailID>
else
if($beeperNumber/fn:string()) then
<bem:BeeperNumber>{mappingHelperFFEBEMRequest834:replace($beeperNumber/fn:string(), "-", "")}</bem:BeeperNumber>
else
()
}

let $ret := mappingHelperFFEBEMRequest834:checkEmptyNode($ret)

return $ret
};

declare function mappingHelperFFEBEMRequest834:mapParentAddress($insuredMember as element(), $section as xs:string) {
let $mailRefTypCode := "2"
let $mailCatTypCode := "1"
let $mailAddress :=
$insuredMember/po:specifiedMemberAddress[./po:definingAddressRelationshipRoleType/pb:referenceTypeCode/fn:string() = $mailRefTypCode
and ./po:definingAddressCategoryType/pb:referenceTypeCode/fn:string() = $mailCatTypCode][1]

let $homeRefTypCode := "1"
let $homeCatTypCode := "2"
let $homeAddress :=
$insuredMember/po:specifiedMemberAddress[./po:definingAddressRelationshipRoleType/pb:referenceTypeCode/fn:string() = $homeRefTypCode
and ./po:definingAddressCategoryType/pb:referenceTypeCode/fn:string() = $homeCatTypCode][1]

let $address :=
if ($mailAddress) then
$mailAddress
else
($homeAddress)

let $streetName1 := $address/po:definingAddressPlace/po:streetName1
let $streetName2 := $address/po:definingAddressPlace/po:streetName2
let $cityName := $address/po:definingAddressPlace/po:cityName
let $stateCode := $address/po:definingAddressPlace/po:stateCode
let $zipPlus4Code := $address/po:definingAddressPlace/po:zipPlus4Code
(:let $countryCode := $address/po:definingAddressPlace/po:countryCode :) (: ADD THE COUNTRY CODE SECTION TO THE ELEMENT :)
let $countyCode := $address/po:definingAddressPlace/po:countyFipsCode

let $ret :=
element{$section}
{
if($streetName1) then
<bem:Addressline1>{$streetName1/fn:string()}</bem:Addressline1>
else()
,
if($streetName2) then
<bem:Addressline2>{$streetName2/fn:string()}</bem:Addressline2>
else()
,
if($cityName) then
<bem:CityName>{$cityName/fn:string()}</bem:CityName>
else()
,
if($stateCode) then
<bem:StateCode>{$stateCode/fn:string()}</bem:StateCode>
else()
,
if($zipPlus4Code) then
<bem:PostalCode>{mappingHelperFFEBEMRequest834:replace($zipPlus4Code/fn:string(), "-", "")}</bem:PostalCode>
else()

}

let $ret := mappingHelperFFEBEMRequest834:checkEmptyNode($ret)

return $ret
};

declare function mappingHelperFFEBEMRequest834:getCustodialOrResponsible($coveredInsuredMember as element()){

let $ret :=
if ($coveredInsuredMember/po:insuredMemberRelationship/po:insuredMemberAssociationReason/pb:referenceTypeCode = "1" ) then
let $specifyingMemberSSN := $coveredInsuredMember/po:insuredMemberRelationship/po:specifyingMember/po:memberSSN/fn:string()
return $specifyingMemberSSN
else
if ($coveredInsuredMember/po:insuredMemberRelationship/po:insuredMemberAssociationReason/pb:referenceTypeCode = "2" ) then
let $specifyingMemberSSN := $coveredInsuredMember/po:insuredMemberRelationship/po:specifyingMember/po:memberSSN/fn:string()
return $specifyingMemberSSN
else
()
let $ret := mappingHelperFFEBEMRequest834:emptyElementToSring($ret)

return $ret
};
declare function mappingHelperFFEBEMRequest834:getPolicySpecifyingPerson($insurancePlanPolicy as element()) {

let $policySubscriber := $insurancePlanPolicy/po:coveredInsuredMember[./po:subscriberIndicator/fn:string() = "true"]
let $subscriberSpecifyingPerson :=
if ($policySubscriber) then
let $subscriberSpecifyingPerson := $policySubscriber/po:definingMember/po:specifyingPerson/fn:string()
return $subscriberSpecifyingPerson
else ()
return $subscriberSpecifyingPerson
};
declare function mappingHelperFFEBEMRequest834:compareSpecifyingPersons2($specifyingPerson, $insurancePlanPolicy as element()) {
let $policySubscriber := mappingHelperFFEBEMRequest834:getPolicySpecifyingPerson($insurancePlanPolicy)
let $match :=
if ($policySubscriber) then
let $subscriberSpecifyingPerson := $policySubscriber/po:definingMember/po:specifyingPerson/fn:string()
let $match :=
if ($specifyingPerson eq $subscriberSpecifyingPerson) then
let $_ := xs:boolean("true")
return $_
else
let $_ := xs:boolean("false")
return $_
return $match
else ()
return $match
};
declare function mappingHelperFFEBEMRequest834:compareSpecifyingPersons($specifyingPerson1, $specifyingPerson2) {
	let $match :=
		if ($specifyingPerson1 and $specifyingPerson2) then
			let $match :=
				if ($specifyingPerson1 eq $specifyingPerson2) then
					let $_ := xs:boolean("true")
					return $_
				else
					let $_ := xs:boolean("false")
					return $_
			return $match
		else ()
	return $match
};
declare function mappingHelperFFEBEMRequest834:addOneSecToLastModified($lastModified) {
	let $lastModified := xs:dateTime($lastModified)
	let $newTime := $lastModified + xs:dayTimeDuration("PT1S")
	return $newTime
};

declare function mappingHelperFFEBEMRequest834:toDate($dateOrDateTime) {
	if($dateOrDateTime castable as xs:date) then xs:date($dateOrDateTime)
	else if($dateOrDateTime castable as xs:dateTime) then xs:date(xs:dateTime($dateOrDateTime))
	else $dateOrDateTime
};


