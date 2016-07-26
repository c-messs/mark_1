xquery version "1.0-ml";
 
import module namespace sm = "http://marklogic.com/ps/servicemetrics" at "/lib/servicemetrics/metrics/servicemetrics.xqy";

declare namespace pb = "http://persistence.base.cms.hhs.gov";
declare namespace p = "http://persistence.ffe.cms.hhs.gov";
declare namespace b = "http://base.persistence.base.cms.hhs.gov";
declare namespace bem = "http://bem.dsh.cms.gov";
declare namespace error="http://marklogic.com/xdmp/error";

import module namespace hixcrud = "http://ffx.ffe.gov/lib/crudUtil" at "/lib/crudUtil.xqy";
import module namespace hixInsurancePlanPolicy="http://ffx.ffe.gov/impls/InsurancePlanPolicy" at "/Services/InsurancePlanPolicy/impl/InsurancePlanPolicyImpl.xqy";
import module namespace hixInsurancePlan="http://ffx.ffe.gov/impls/InsurancePlan" at "/Services/InsurancePlan/impl/InsurancePlanImpl.xqy";
import module namespace hixIssuerApplication = "http://ffx.ffe.gov/impls/IssuerApplication" at "/Services/IssuerApplication/impl/IssuerApplicationImpl.xqy";
import module namespace eeUtilXQueries = "http://ffx.ffe.gov/lib/utils/eeUtilXQueries" at "/lib/utils/eeUtilXQueries.xqy";
import module namespace bizidgen = "http://marklogic.com/ps/lib/id-generate" at "/lib/BusinessIDGen/id-generator.xqy";
import module namespace hixOrganization = "http://ffx.ffe.gov/impls/Organization" at "/Services/Organization/impl/OrganizationImpl.xqy";


declare variable $URI external;
declare variable $outputBerPath as xs:string external;
declare variable $highwaterMark as xs:string external;
declare variable $mode as xs:string := "initial_cic"; (: Valid inputs: "initial_cic" or any other string else. :)
declare variable $jobId as xs:string external;


declare function local:forInitialEnrollment(
  $uri as xs:string,
  $tenantId as xs:string,
  $mode
) as element(bem:BenefitEnrollmentMaintenance) ?
{
  let $_ := fn:trace( 
    ( "forInitialEnrollment input params: " 
      || "$uri  "     || $uri      || "; "
      || "$tenantId " || $tenantId || "; "
      || "$mode "     || $mode
    ), "debug"
  )
  
  (: retrieve top level documents: :)
  (: * insurancePlan:      insurancePlanPolicy.selectedInsurancePlan = insurancePlan.insurancePlanIdentifier :)
  (: * issuerApplication:  issuerApplication.organization.issuerOrganization.issuerHIOSID  = insurancePlan/issuerHIOSIdentifier :)
  let $currentInsurancePlanPolicy  := fn:doc($uri)/node()
  let $coveredInsuredMembers := $currentInsurancePlanPolicy/p:coveredInsuredMember
  let $policySubscriber      := $currentInsurancePlanPolicy/p:coveredInsuredMember[./p:subscriberIndicator/fn:string() = "true"]
  let $policySubscriberId    := $policySubscriber/p:insuredMemberIdentifier/fn:string()

  let $issuerError := ()
  let $memberError := ()
  let $policyError := ()

  let $members := 
    for $eachCoveredInsuredMember in $coveredInsuredMembers
    order by $eachCoveredInsuredMember/p:subscriberIndicator/fn:string() descending
    return try {
        local:mapMemberForIntialEnrollment(
          $eachCoveredInsuredMember,
          $policySubscriberId,
          $currentInsurancePlanPolicy,
          $mode
        )
    } catch ($exception) { 
      (: Attempt to recover part of the member object :)
      let $errorPreUnquote := $exception/error:data/error:datum/fn:string()
      let $errObject := if (fn:count($errorPreUnquote) eq 1) then xdmp:unquote($errorPreUnquote)/element() else ()
      let $partialMember := $errObject//bem:Member
      let $err := $errObject/error:error
      let $_ := xdmp:set($memberError, $err) 
      return if ($errObject) then $partialMember else xdmp:rethrow()
    }

	let $issuer  := try {
      local:mapIssuer($currentInsurancePlanPolicy)
    } catch ($exception) { xdmp:set($issuerError, $exception) }

  	(: SP. Adding PolicyInfo Element :)
    let $policy  := try {
      local:mapPolicy($currentInsurancePlanPolicy)
    } catch ($exception) { xdmp:set($policyError, $exception) }	
	
	
    let $Error := 
      if ($issuerError or $memberError or $policyError) then
        fn:string-join ((xdmp:quote($issuerError),  xdmp:quote($memberError),xdmp:quote($policyError)),  " | ")
      else ()


  (: let $controlNumber := fn:string(plocal:generateControlNumber() + 1000) :)
  let $controlNumber := "TO BE SET"
  let $transactionInformation := local:buildTransactionInformation($currentInsurancePlanPolicy, $controlNumber)
  let $extractionStatus := local:mapExtractionStatusInfo($Error)

  let $members := local:sortMembersSubscriberFirst($members)

  let $benefitEnrollmentMaintenance :=
    <bem:BenefitEnrollmentMaintenance>
      {
        $transactionInformation
      }
      {
        $issuer
      }
      {
		    $policy
	    }
      {
        $members
      }
	    {
        $extractionStatus
      }
    </bem:BenefitEnrollmentMaintenance>

  (: TODO FIX VALIDATION :)  
(:  let $_ := local:validateInitial($ret)
:)

  let $_ := fn:trace( ("forInitialEnrollment return value: ", $benefitEnrollmentMaintenance), "debug" )

  return $benefitEnrollmentMaintenance
};

declare function local:sortMembersSubscriberFirst($members as element()) { 
  for $each in $members
  order by $each/p:subscriberIndicator/fn:string() descending
  return $each
};
 
declare function local:getHistPolicies($uri as xs:string, $newHighwaterMark as xs:dateTime,$prevHighWaterMark as xs:dateTime){
  let $policy-query :=   
    cts:element-query(xs:QName("p:insurancePlanPolicy"),
      cts:and-query((
        cts:directory-query($uri, "1"),
        cts:element-range-query(xs:QName("b:lastModified"),"<=", $newHighwaterMark),
		cts:element-range-query(xs:QName("b:lastModified"),">", $prevHighWaterMark),
        cts:element-range-query(xs:QName("b:deleted"),"=","false")
      ))
  )
 let $policy-uris := cts:uris((), (), $policy-query)  
 return (
     for $policy-uri in $policy-uris
     let $version :=
         if(fn:contains($policy-uri, "hist-")) then
           xs:integer((fn:tokenize((fn:tokenize($policy-uri, "-"))[2],"[.]"))[1])
         else 0
     order by $version descending
     return $policy-uri   
   )                            
}; 



(:~
  @author matthew.royal@marklogic.com
  Attempt to format incoming variable as an xs:string formatted YYYY-MM-DD.
  If it fails, it will output the original variable as its original type.
:)
declare function local:formatDateYyyymmdd($myDate) {

  if (xdmp:castable-as("http://www.w3.org/2001/XMLSchema", "dateTime", $myDate)) then 
    fn:format-date(xs:date(xs:dateTime($myDate)), "[Y0001]-[M01]-[D01]") 
  else 
    $myDate
};

declare function local:oneOrNone($x) {
  if ($x) then ($x)[fn:last()] else ()
};

declare function local:emptyElementToSring($x) {
  let $ret :=
    if($x) then $x
    else if ($x = 0) then $x
    else ""
  return $ret
};

declare function local:assertHasValue($e, $eName) {
  let $path := xdmp:path($e/text())
  let $_:=
    if(fn:not(fn:empty($path))) then()
    else fn:error((), fn:concat("mappingHelperFFEBEMRequest834 assert: element ", xdmp:path($e), $eName, " does not have a value"))

  return $_
};

declare function local:assertElementHasValue($e, $eName) {
  let $_ :=
    if (fn:not(fn:empty($e))) then ()
    else fn:error( (), fn:concat("mappingHelperFFEBEMRequest834 assert: element ", $eName, " is empty") )

  return $_
};

declare function local:assertNotEmpty($e, $eName) {
  let $path := xdmp:path($e/node())
  let $_:=
    if(fn:not(fn:empty($path))) then()
    else fn:error((), fn:concat("mappingHelperFFEBEMRequest834 assert: element ", xdmp:path($e), $eName, " is empty"))
  return $_
};

declare function local:assertHasChildElement($e, $eName) {
  let $path := xdmp:path($e/element())
  let $_:=
    if (fn:not(fn:empty($path))) then()
    else fn:error((), fn:concat("mappingHelperFFEBEMRequest834 assert: element ", xdmp:path($e), $eName, " does not have a child element"))
  return $_
};


declare function local:checkEmptyNode($e as element()) {
  let $ret :=
    if($e/node()) then $e
    else ()

  return $ret
};

declare function local:formatCurrency($c) {
  let $ret:=
    fn:format-number(xs:decimal($c),"###0.00")
  return $ret
};

declare function local:formatString($s) {
  let $subString := fn:substring($s, 1, 60)
  return $subString
};

declare function local:replace($stringToReplace, $replace, $with) {
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

declare function local:retrieveInsurancePlanPolicy(
  $policyTrackingNumber as xs:string, 
  $tenantId) 
as element(p:insurancePlanPolicy)? {
  let $ptn-query as cts:query :=
    cts:element-value-query(
      xs:QName("p:policyTrackingNumber"),
      $policyTrackingNumber
    )
  let $result :=
    hixcrud:search($tenantId,
      hixInsurancePlanPolicy:GetBasePath(),
      $ptn-query
    )
  let $howManyReturned := fn:count($result)
  let $_ := fn:trace("local:retrieveInsurancePlanPolicy query on "
    || "$tenantId = " || $tenantId
    || " and "
    || $ptn-query
    || " return count is : "
    || $howManyReturned
    || " - described result: "
    || xdmp:describe($result),
    "mappingHelperFFEBEMRequest834"
  )
  let $_ := fn:trace( ("local:retrieveInsurancePlanPolicy - result dump: ", $result), "mappingHelperFFEBEMRequest834" )
  let $_ :=
    if($howManyReturned ne 1) then fn:error( (),
      "NOT_FOUND_404",
      (
        "100",
        "local:retrieveInsurancePlanPolicy error, " ||
        $howManyReturned ||
        " InsurancePlanPolicy(ies) returned for " ||
        "$tenantId = " || $tenantId ||
        " and " ||
        " p:insurancePlanPolicy/p:policyTrackingNumber = " || $policyTrackingNumber || "." ||
        " Expecting exactly 1 but Found " || $howManyReturned
      )
    ) 
    else ()
  return $result
};

declare function local:mapIssuer(
  $insurancePlanPolicy as element(p:insurancePlanPolicy)
) as element() {
  let $issuerName := $insurancePlanPolicy/p:issuerName/fn:string()
  let $orgTaxId := $insurancePlanPolicy/p:issuerTaxPayerID

  let $orgTaxId :=
    if ($orgTaxId) then $orgTaxId
    else 
      fn:error( (),
        "NOT_FOUND_404",
        (
          "102",
          "local:mapIssuer error - " ||
          " issuerTaxPayerID not found on Insurance Plan Policy"
        )
    )
  let $issuerName := local:formatString($issuerName)
  let $issuer :=
    <bem:Issuer>
      { if ($issuerName) then <bem:Name>{$issuerName}</bem:Name> else() }
      { if ($orgTaxId) then <bem:TaxPayerIdentificationNumber>{local:replace($orgTaxId/fn:string(), "-", "")}</bem:TaxPayerIdentificationNumber> else() }
    </bem:Issuer>

  let $issuer := local:checkEmptyNode($issuer)

  let $error := local:assertNotEmpty($issuer, " Issuer Node")
  return $issuer
};

declare function local:mapPolicy  (
  $insurancePlanPolicy as element(p:insurancePlanPolicy)
) as element() {

    let $GroupPolicyNumber := local:emptyElementToSring($insurancePlanPolicy/p:policyTrackingNumber/fn:string())
  let $MarketplaceGroupPolicyIdentifier := $insurancePlanPolicy/p:marketplaceGroupPolicyIdentifier/fn:string()
  let $PolicyStartDate := $insurancePlanPolicy/p:insurancePlanPolicyStartDate/fn:string()
  let $PolicyEndDate := $insurancePlanPolicy/p:insurancePlanPolicyEndDate/fn:string()  
  let $confirmationIndicator := $insurancePlanPolicy/p:issuerConfirmationIndicator/fn:string()
    let $supercededIndicator := $insurancePlanPolicy/p:supersededIndicator/fn:string()
  
  
  let $policyStatus := $insurancePlanPolicy/p:recordedInsurancePolicyStatus/p:definingInsurancePolicyStatusType/pb:referenceTypeCode/fn:string()
  
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
          {local:toDate(local:formatDateYyyymmdd($PolicyStartDate))}
        </bem:PolicyStartDate> 
      }
      { 
        <bem:PolicyEndDate>
          {local:toDate(local:formatDateYyyymmdd($PolicyEndDate))}
        </bem:PolicyEndDate> 
      }
      { 
        <bem:PolicyStatus>
          {$epsPolicyStatus}
        </bem:PolicyStatus> 
      }
    </bem:PolicyInfo>

  let $error := local:assertNotEmpty($policy, " Policy Node")
  return $policy
};


declare function local:mapMemberInformation($insuredMember as element()) 
{
  let $memberInfo := <bem:MemberInformation></bem:MemberInformation>

  let $subInd :=
    if ($insuredMember/p:subscriberIndicator/fn:string() = "true") then "Y"
    else "N"

  let $associationToSubscriberTypeCode :=
    if ($insuredMember/p:subscriberIndicator/fn:string() = "true")
    then
      <pb:referenceTypeCode>1</pb:referenceTypeCode>
    else
      $insuredMember/p:definingMemberAssocationTypeToSubscriber/pb:referenceTypeCode

  let $memberInfo :=
  <bem:MemberInformation>
  <bem:SubscriberIndicator>{$subInd}</bem:SubscriberIndicator>
  </bem:MemberInformation>

  let $val := $insuredMember/p:definingMemberAssocationTypeToSubscriber/pb:referenceTypeCode/fn:string()
  let $error := local:assertHasChildElement($memberInfo," memberInformation")

  return $memberInfo
};

declare function local:mapMemberPolicyNumber($insurancePlanPolicy as element()) {
  let $value := $insurancePlanPolicy/p:insurancePolicyNumber/fn:string()
  let $ret :=
    if($value) then
      <bem:MemberPolicyNumber>{$value}</bem:MemberPolicyNumber>
    else()
  return $ret
};

declare function local:mapMemberAdditionalIdentifier(
  $exchangeAssignedMemberID as xs:string,
  $issuerAssignedMemberID as xs:string,
  $issuerAssignedSubscriberId as xs:string) 
{
  (: Required Fields: exchangeAssignedMemberID :)
  let $error := () (: local:assertHasValue(<nde>{$exchangeAssignedMemberID}</nde>, " MemberAdditionalIdentifier:exchangeAssignedMemberID ") :)
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
  let $ret := local:checkEmptyNode($ret)
  return $ret
};


declare function local:mapMemberRelatedDates(
  $eligibilityBeginDate,
  $eligibilityEndDate ) 
{
  let $ret :=
    <bem:MemberRelatedDates>
      {
        if($eligibilityBeginDate) then
          <bem:EligibilityBeginDate>{local:formatDateYyyymmdd($eligibilityBeginDate/node())}</bem:EligibilityBeginDate>
        else()
      }
      {
        if($eligibilityEndDate )then
          <bem:EligibilityEndDate>{local:formatDateYyyymmdd($eligibilityEndDate/node())}</bem:EligibilityEndDate>
        else()
      }
    </bem:MemberRelatedDates>
  return $ret
};

declare function local:mapMemberName(
  $coveredMember as element(), 
  $mapSSN as xs:boolean) 
{
  let $memberName := $coveredMember/p:definingMember
  (: required fields : lastName, firstName :)
  let $error := () (: local:assertHasValue($memberName/p:lastName, " MemberName:LastName") :)
  let $error := () (: local:assertHasValue($memberName/p:firstName, " MemberName:firstName") :)
  let $ret :=
    <bem:MemberName>
      {
        if($memberName/p:lastName) then
        <bem:LastName>{$memberName/p:lastName/fn:string()}</bem:LastName>
        else()
      }
      {
        if($memberName/p:firstName) then
        <bem:FirstName>{$memberName/p:firstName/fn:string()}</bem:FirstName>
        else()
      }
      {
        if($memberName/p:middleName) then
        <bem:MiddleName>{$memberName/p:middleName/fn:string()}</bem:MiddleName>
        else()
      }
      {
        if($memberName/p:salutationName) then
        <bem:NamePrefix>{$memberName/p:salutationName/fn:string()}</bem:NamePrefix>
        else()
      }
      {
          if($memberName/p:suffixName) then
          <bem:NameSuffix>{$memberName/p:suffixName/fn:string()}</bem:NameSuffix>
          else()
      }
      {
        if($mapSSN and $memberName/p:memberSSN) then
          let $ssnNoDash := local:replace($memberName/p:memberSSN/fn:string(), "-", "")
          return
            <bem:SocialSecurityNumber>{$ssnNoDash}</bem:SocialSecurityNumber>
        else()
      }
    </bem:MemberName>
  let $ret := local:checkEmptyNode($ret)
  return $ret
};

declare function local:mapMemberResidenceAddress($insuredMember as element()) {

  let $subscriberIndicator := $insuredMember/p:subscriberIndicator/fn:string()
  let $homeRefTypCode := "1" (: AV: this should for Residence Address, hence changed to 1  :)
  let $homeCatTypCode := "2"
  let $mailingRefTypCode := "2"
  let $mailingCatTypCode := "1"
  let $homeAddress := (
    $insuredMember/p:definingMember/p:specifiedMemberAddress[./p:definingAddressRelationshipRoleType/pb:referenceTypeCode/fn:string() = $mailingRefTypCode
      and ./p:definingAddressCategoryType/pb:referenceTypeCode/fn:string() = $mailingCatTypCode][1] (: AV: picking the first element when there are multiple mailing address:)
      ,   
    $insuredMember/p:definingMember/p:specifiedMemberAddress[./p:definingAddressRelationshipRoleType/pb:referenceTypeCode/fn:string() = $homeRefTypCode
      and ./p:definingAddressCategoryType/pb:referenceTypeCode/fn:string() = $homeCatTypCode][1] (: AV: picking the first element when there are multiple residence address:)
  )[fn:last()] 
  
  let $stateCode := ($homeAddress/p:definingAddressPlace/p:stateCode)[1]
  let $zipPlus4Code := ($homeAddress/p:definingAddressPlace/p:zipPlus4Code)[1]

  let $address := (
        
        if($stateCode) then
          <bem:StateCode>{$stateCode/fn:string()}</bem:StateCode>
        else()
        ,
        if($zipPlus4Code) then
          <bem:PostalCode>{local:replace($zipPlus4Code/fn:string(), "-", "")}</bem:PostalCode>
        else()
  )

  let $ret :=
    <bem:MemberResidenceAddress>
      { for $a in $address return local:checkEmptyNode($a) }
    </bem:MemberResidenceAddress>
  
  let $ret := local:checkEmptyNode($ret)
    let $error := local:assertHasChildElement($ret, " memberResidenceAddress")
  return $ret
};

declare function local:mapMemberDemographics($insuredMember as element()) {
  let $subscriberIndicator := $insuredMember/p:subscriberIndicator/fn:string()

  let $birthDate := $insuredMember/p:definingMember/p:memberBirthDate
  let $gender := $insuredMember/p:definingMember/p:memberGender/pb:referenceTypeCode/fn:string()
  let $genderCode :=
    if($gender = "1" or fn:lower-case($gender) = fn:lower-case("Male")) then "M"
    else if($gender = "2" or fn:lower-case($gender) = fn:lower-case("Female") ) then "F"
    else "M" (: UNKNOWN:)

  (: Required fields: birthDate and GenderCode :)
  let $error := () (: local:assertHasValue($birthDate," birthDate") :)
  let $error := () (: local:assertHasValue(<elm>$genderCode</elm>," genderCode") :)

  let $ret :=
    <bem:MemberDemographics>
      {
        if($birthDate) then
          <bem:BirthDate>{local:formatDateYyyymmdd($birthDate/node())}</bem:BirthDate>
        else()
      }
      {
        if($genderCode) then
          <bem:GenderCode>{$genderCode}</bem:GenderCode>
        else()
      }
    </bem:MemberDemographics>

  let $ret := local:checkEmptyNode($ret)
  let $error := local:assertNotEmpty($ret," memberDemographics")

  return $ret
};

declare function local:mapHealthCoverageInformation(
  $insurancePlanPolicy as element()
) {
  (: let $insuranceLineCode := $insurancePlan/p:definingInsuranceProductDivisionType/pb:referenceTypeCode :)
  let $insuranceLineCode := $insurancePlanPolicy/p:associatedProductDivisionType/pb:referenceTypeCode
  let $insuranceLineCode := local:refMapper("insuranceProductDivisionType", $insuranceLineCode/fn:string())
  let $insuranceLineCode :=
    if($insuranceLineCode) then $insuranceLineCode
    else "HLT" (: todo: default to HLT for now - according to Lakshmi :)
  let $insuranceLineCode := <elm>{$insuranceLineCode}</elm>
  
  (: required fields : insuranceLineCode and healthCoverageMaintenanceTypeCode :)
  let $error := () (: local:assertHasValue($insuranceLineCode," HealthCoverageInformation:insuranceLineCode") :)
  
  let $ret :=
    <bem:HealthCoverageInformation>
      {
        if($insuranceLineCode) then
          <bem:InsuranceLineCode>{$insuranceLineCode/node()}</bem:InsuranceLineCode>
        else()
      }
    </bem:HealthCoverageInformation>

  let $ret := local:checkEmptyNode($ret)

  let $error := local:assertNotEmpty($ret, " mapHealthCoverageInformation")
  return $ret
};

declare function local:mapHealthCoverageDates($coveredInsuredMember as element()) {
let $benefitBeginDate := $coveredInsuredMember/p:coverageStartDate
(: required fields : BenefitBeginDate :)
let $benefitEndDate := $coveredInsuredMember/p:coverageEndDate
(: AV: changes to map BenefitEndDate to coverageEndDate :)
let $error := local:assertNotEmpty(<elt>$benefitBeginDate</elt>," HealthCoverageDates:BenefitBeginDate")
let $error := local:assertNotEmpty(<elt>$benefitEndDate</elt>," HealthCoverageDates:BenefitEndDate")
let $ret:=
<bem:HealthCoverageDates>
{
if ($benefitBeginDate) then
<bem:BenefitBeginDate>{local:formatDateYyyymmdd($benefitBeginDate/node())}</bem:BenefitBeginDate>
else ()
}
{
if ($benefitEndDate) then
<bem:BenefitEndDate>{local:formatDateYyyymmdd($benefitEndDate/node())}</bem:BenefitEndDate>
else ()
}
</bem:HealthCoverageDates>
let $ret := local:checkEmptyNode($ret)

let $error := local:assertNotEmpty($ret," HealthCoverageDates")

return $ret
};

declare function local:maphealthCoveragePolicyNumber($insurancePlanPolicy) {
let $selectedInsurancePlan := local:emptyElementToSring($insurancePlanPolicy/p:selectedInsurancePlan/fn:string())
let $planVariant := local:emptyElementToSring($insurancePlanPolicy/p:definingPlanVarianceComponentType/pb:referenceTypeCode/fn:string())
let $planVariant :=
if(fn:string-length($planVariant) eq 1) then "0" || $planVariant
else $planVariant
let $contractCode := $selectedInsurancePlan || $planVariant (: 16 characters total :)
let $internalControlNumber := $insurancePlanPolicy/p:insurancePolicyNumber/fn:string()
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
let $ret := local:checkEmptyNode($ret)

return $ret

};
declare function local:refMapper($refType as xs:string, $ffmKey) as xs:string ?{
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

declare function local:mapAdditionalInfo(
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
  <bem:EffectiveStartDate>{local:formatDateYyyymmdd($proratedAmountStartDate)}</bem:EffectiveStartDate>
else if($effectiveStartDate) then
  <bem:EffectiveStartDate>{local:formatDateYyyymmdd($effectiveStartDate)}</bem:EffectiveStartDate>
else()
}
{
if ($proratedAmountEndDate) then
  <bem:EffectiveEndDate>{local:formatDateYyyymmdd($proratedAmountEndDate)}</bem:EffectiveEndDate>
else if($effectiveEndDate) then
  <bem:EffectiveEndDate>{local:formatDateYyyymmdd($effectiveEndDate)}</bem:EffectiveEndDate>
else()
}
{
if ($totalPremiumAmount) then
<bem:TotalPremiumAmount>{local:formatCurrency($totalPremiumAmount)}</bem:TotalPremiumAmount>
else <bem:TotalPremiumAmount>0</bem:TotalPremiumAmount>
}
{
if ($totalIndividualResponsibilityAmount) then
<bem:TotalIndividualResponsibilityAmount>{local:formatCurrency($totalIndividualResponsibilityAmount)}</bem:TotalIndividualResponsibilityAmount>
else <bem:TotalIndividualResponsibilityAmount>0</bem:TotalIndividualResponsibilityAmount>
}
{
if ($ratingArea) then
<bem:RatingArea>{"R-" || $ratingArea}</bem:RatingArea>
else ()
}
{
if ($aptcAmount) then
<bem:APTCAmount>{local:formatCurrency($aptcAmount)}</bem:APTCAmount>
else ()
}
{
if ($csrAmount) then
<bem:CSRAmount>{local:formatCurrency($csrAmount)}</bem:CSRAmount>
else ()
}

{
if ($proratedMonthlyPremiumAmount) then
<bem:ProratedMonthlyPremiumAmount>{local:formatCurrency($proratedMonthlyPremiumAmount)}</bem:ProratedMonthlyPremiumAmount>
else ()
}
{
if ($proratedIndividualResponsibleAmount) then
<bem:ProratedIndividualResponsibleAmount>{local:formatCurrency($proratedIndividualResponsibleAmount)}</bem:ProratedIndividualResponsibleAmount>
else ()
}
{
if ($proratedAppliedAPTCAmount) then
<bem:ProratedAppliedAPTCAmount>{local:formatCurrency($proratedAppliedAPTCAmount)}</bem:ProratedAppliedAPTCAmount>
else ()
}
{
if ($proratedCSRAmount) then
<bem:ProratedCSRAmount>{local:formatCurrency($proratedCSRAmount)}</bem:ProratedCSRAmount>
else ()
}
</bem:AdditionalInfo> 

let $ret := if(fn:count($ret) gt 0) then $ret else()

return $ret
};

declare function local:generateControlNumber() as xs:integer ? {
(: TODO: insuredMemberIdentifier : this must be updated :)
let $kvm := map:map()
let $_ := map:put($kvm, "tenantId", "VA")
let $policyTrackingNumber := bizidgen:nextScoped("ControlNumber", $kvm, 1)
return $policyTrackingNumber
};

(: Build TransactionInformation :)
declare function local:buildTransactionInformation($insurancePlanPolicy as element(p:insurancePlanPolicy), $controlNumber) {
(: Values are hardcode temporarily, Mapping needs to established to figure out the values :)

let $policyTransactionEventDate := local:addOneSecToLastModified($insurancePlanPolicy/b:lastModified/fn:string())
let $exchangeCode := "Individual" (: Individual or SHOP:)
let $controlNumber := "TO BE SET"
let $lastModified := $insurancePlanPolicy/b:lastModified/fn:string()
let $versionNumber := $insurancePlanPolicy/b:versionInformation/pb:versionNumber/fn:string()

let $policyTransactionEventDate :=
if (fn:not(fn:empty($policyTransactionEventDate))) then $policyTransactionEventDate
else
fn:current-dateTime()

let $TransactionInfo := local:mapTransactionInfo($controlNumber, $policyTransactionEventDate, $exchangeCode, $lastModified, $versionNumber)

return $TransactionInfo

};

declare function local:mapTransactionInfo ($controlNumber,
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

let $ret := local:checkEmptyNode($ret)

return $ret
};


declare function local:mapExtractionStatusInfo ($error as xs:string?){

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

let $ret := local:checkEmptyNode($ret)

return $ret
};


(: Build file information object (note all static values right now cause not in PCM) :)
declare function local:buildFileInformation($insurancePlanPolicy as element(p:insurancePlanPolicy), $tenantId as xs:string ) as element() {
(: Not Needed for initial :)
let $interchangeControlNumber := "123456789"
let $interchangeSenderId := "SenderId"
let $interchangeReceiverId := "RecivId"


let $groupSenderId :=
if(fn:string-length($tenantId) ne 3) then $tenantId || "0"
else $tenantId

let $groupReceiverId := $insurancePlanPolicy/p:selectedInsurancePlan/fn:string()
let $groupControlNumber := fn:format-date(fn:current-date(), "[Y0001][M01][D01]")
let $versionNumber := "23"

(: Required Fields : groupSender, groupReceiver, groupControlNumber, versionNumber :)
let $error := () (: local:assertHasValue(<gsid>{$groupSenderId}</gsid>, " GroupSenderID ") :)
let $error := () (: local:assertHasValue(<grid>{$groupReceiverId}</grid>, " GroupReceiverID ") :)

let $ret := local:mapFileInformation ($groupSenderId,
$groupReceiverId,
$groupControlNumber,
$versionNumber )
return $ret
};

declare function local:mapFileInformation($groupSenderId,
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

let $ret := local:checkEmptyNode($ret)

return $ret
};


declare function local:mapMemberForIntialEnrollment(
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
  let $subscriberIndicator := $coveredInsuredMember/p:subscriberIndicator/fn:string()

  return
    try {

    (: Anand Vedam -- EffectuationIndicator CR changes :)
    let $confirmationIndicator := $insurancePlanPolicy/p:issuerConfirmationIndicator/fn:string()

    (: Fix to resolve issue with ERL Extraction, to avoid incorrect MTC codes in 834.
       Anand Vedam PolicyStatus logic here. :)
    let $policyStatus := $insurancePlanPolicy/p:recordedInsurancePolicyStatus/p:definingInsurancePolicyStatusType/pb:referenceTypeCode/fn:string()
    
    (: Fix to resolve issue with ERL Extraction, to avoid incorrect MRC codes in 834.
       Anand Vedam PolicyStatus logic here. :)
    let $ippMaintenanceReasonTypeCode := $insurancePlanPolicy/p:recordedInsurancePolicyStatus/p:recordedMaintenanceTypeReason/p:definingMaintenanceReasonType/pb:referenceTypeCode/fn:string()
    let $maintenanceReasonCode := ""
  (:    if($ippMaintenanceReasonTypeCode) then $ippMaintenanceReasonTypeCode
      else "EC"
  :)

    let $_ := xdmp:set($memberName, local:mapMemberName($coveredInsuredMember, fn:true()))
    let $_ := xdmp:set($memberResidenceAddress, local:mapMemberResidenceAddress($coveredInsuredMember))
    let $_ := xdmp:set($memberDemographics, local:mapMemberDemographics($coveredInsuredMember))
    let $_ := xdmp:set($memberInformation, local:mapMemberInformation($coveredInsuredMember))
    
    let $_ := xdmp:set($subscriberIdElement,
      if($policySubscriberId) then <bem:SubscriberID>{$policySubscriberId}</bem:SubscriberID>
      else()
    )

    (: todo memberPolicyNumber :)

    let $issuerAssignedSubscriberId := if($insurancePlanPolicy/p:issuerAssignedSubscriberIdentifier) then
                        $insurancePlanPolicy/p:issuerAssignedSubscriberIdentifier/fn:string() 
                        else ("")
                      
        let $issuerAssignedMemberId := if($coveredInsuredMember/p:issuerInsuredMemberIdentifier) then
                        $coveredInsuredMember/p:issuerInsuredMemberIdentifier/fn:string() 
                        else ("")                     

    let $_ := xdmp:set($memberAdditionalIdentifier,
      local:mapMemberAdditionalIdentifier($coveredInsuredMember/p:insuredMemberIdentifier/fn:string(),
      $issuerAssignedMemberId,
      $issuerAssignedSubscriberId)
    )
    let $_ := xdmp:set($memberRelatedDates, local:mapMemberRelatedDates($coveredInsuredMember/p:coverageStartDate,
      $coveredInsuredMember/p:coverageEndDate (: AV: Changes to map PolicyEndDate :) )
    )

    let $subSpecifyingPerson := local:getPolicySpecifyingPerson($insurancePlanPolicy)

    let $healthCoverageInformation := local:mapHealthCoverageInformation($insurancePlanPolicy)
    let $healthCoverageDates := local:mapHealthCoverageDates($coveredInsuredMember)
    let $healthCoveragePolicyNumber := local:maphealthCoveragePolicyNumber($insurancePlanPolicy)
    let $_ := xdmp:set($healthCoverage,
      <bem:HealthCoverage>
        {
          local:checkEmptyNode($healthCoverageInformation)
        }
        {
          local:checkEmptyNode($healthCoverageDates)
        }
        {
          local:checkEmptyNode($healthCoveragePolicyNumber)
        }
      </bem:HealthCoverage>
    )
    let $_ := xdmp:set($healthCoverage, local:checkEmptyNode($healthCoverage))

    (: todo additionalInfo :)

    (: for initial - retrieve only the first calculatedInsurancePolicyPremium :)

    let $premiumAmount1 := $coveredInsuredMember/p:memberPolicyPremium[fn:last()]/p:monthlyPolicyPremiumAmount/node()
    let $premiumAmount1 := local:emptyElementToSring($premiumAmount1)

    let $totalPremAmount :=
      if ($subscriberIndicator = "true") then
        $insurancePlanPolicy/p:calculatedInsurancePolicyPremium[fn:last()]/p:monthlyPolicyPremiumAmount/node()
      else()
    let $totalPremAmount := local:emptyElementToSring($totalPremAmount)

    let $totalResponAmount :=
      if ($subscriberIndicator = "true") then
        $insurancePlanPolicy/p:calculatedInsurancePolicyPremium[fn:last()]/p:individualResponsibleAmount/node()
      else()
    let $totalResponAmount := local:emptyElementToSring($totalResponAmount)

    let $aptcAmount :=
      if ($subscriberIndicator = "true") then
        $insurancePlanPolicy/p:calculatedInsurancePolicyPremium[fn:last()]/p:appliedAPTCAmount/node()
      else()
    let $aptcAmount := local:emptyElementToSring($aptcAmount)

    let $csrAmount :=
      if ($subscriberIndicator = "true") then
        if(fn:exists($insurancePlanPolicy/p:applicableCostSharingReduction[1]/p:csrAmount/node())) then
            $insurancePlanPolicy/p:applicableCostSharingReduction[1]/p:csrAmount/node()
        else ( 0 )
      else()
    let $csrAmount := local:emptyElementToSring($csrAmount)

    let $ratingArea :=
      if ($subscriberIndicator = "true") then
        $insurancePlanPolicy/p:calculatedInsurancePolicyPremium[fn:last()]/p:exchangeRateAreaReference/node()
      else()
    let $ratingArea := local:emptyElementToSring($ratingArea)

    let $effectiveStartDate := $insurancePlanPolicy/p:insurancePlanPolicyStartDate/node()
    let $effectiveStartDate := local:emptyElementToSring($effectiveStartDate)

    let $effectiveEndDate := $insurancePlanPolicy/p:insurancePlanPolicyEndDate/node()
    let $effectiveEndDate := local:emptyElementToSring($effectiveEndDate)

    let $ippMaintenanceTypeReasonText := $insurancePlanPolicy/p:recordedInsurancePolicyStatus/p:recordedMaintenanceTypeReason/p:maintenanceTypeReasonText/fn:string()
    let $ippMaintenanceTypeReasonTextToken := fn:tokenize($ippMaintenanceTypeReasonText[fn:last()], "-")

    let $specialEnrollmentPeriodReason := 
      if($ippMaintenanceTypeReasonTextToken) then
        if(fn:count($ippMaintenanceTypeReasonTextToken) eq 2) then
          $ippMaintenanceTypeReasonTextToken[fn:last()]
        else ()
    else ()
        
    let $dayOfEffectiveDate := fn:day-from-date(local:toDate($effectiveStartDate))
    let $prorationNode := $insurancePlanPolicy/p:calculatedInsurancePolicyPremium[fn:last()]/p:calculatedPolicyPremiumProration

    let $additionalInfoEffectiveStartDate :=
      if ($prorationNode) then
        $insurancePlanPolicy/p:calculatedInsurancePolicyPremium[fn:last()]/p:effectiveStartDate/node()
      else $effectiveStartDate
    (:
      fn:string(xs:dateTime(local:toDate($effectiveStartDate))) (: no change since 1st of month :)
    :)

    let $additionalMaintenanceReason := ()
    
    let $_ := xdmp:set($additionalInfo,

      local:mapAdditionalInfo(
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
    let $_ := fn:trace( ("local: dump of mapped $additionalInfo: ", $additionalInfo), "mappingHelperFFEBEMRequest834" )

    let $member := (
          element bem:Member {
          local:oneOrNone($memberInformation),
          local:oneOrNone($subscriberIdElement),
          local:oneOrNone($memberAdditionalIdentifier),
          local:oneOrNone($memberRelatedDates),
          if ($memberName or $memberResidenceAddress or $memberResidenceAddress or $memberDemographics) then
            element bem:MemberNameInformation {
              local:oneOrNone($memberName),
              $memberResidenceAddress,
              $memberDemographics           
            }
          else (),
          $healthCoverage,
          if ($subscriberIndicator = "true") then $additionalInfo
          else ()
           
        }
        ,
        for $proration in $insurancePlanPolicy/p:calculatedInsurancePolicyPremium[fn:last()]/p:calculatedPolicyPremiumProration
          (:p:calculatedPolicyPremiumProration exists = subscriber = "Y":)
            
          let $proratedMonthlyPremiumAmount := $proration/p:proratedMonthlyPremiumAmount/node()
          let $proratedIndividualResponsibleAmount :=  $proration/p:proratedIndividualResponsibleAmount/node()
          let $proratedAppliedAPTCAmount := $proration/p:proratedAppliedAPTCAmount/node()
          
          let $proratedMonth := $proration/p:proratedMonth/node()
          let $proratedAmountStartDate := $proration/p:proratedAmountStartDate/node()
          let $proratedAmountEndDate := $proration/p:proratedAmountEndDate/node()
          
          (: CSR Proration corresponding to the calculatedPolicyPremiumProration:)
          let $csrProration := $insurancePlanPolicy/p:applicableCostSharingReduction/p:calculatedPolicyCSRProration[fn:string(./p:proratedCSRStartDate) = fn:string($proration/p:proratedAmountStartDate)]
          let $proratedCSRMonth := $csrProration/p:proratedCSRMonth/node()
          let $proratedCSRStartDate :=  $csrProration/p:proratedCSRAmount/node()
          let $proratedCSREndDate := $csrProration/p:proratedCSRAmount/node()
          let $proratedCSRAmount := $csrProration/p:proratedCSRAmount/node()
          return
            if ($subscriberIndicator = "true") then
                element bem:Member {
                local:oneOrNone($memberInformation),
                local:mapAdditionalInfo(
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
    let $_ := fn:trace( ("local: dump of mapped $member: ", $member), "mappingHelperFFEBEMRequest834" )

    let $member := local:checkEmptyNode($member)

    return $member

    } catch ($exception) {
      let $errName := $exception/error:name/fn:string()
      let $errCode := $exception/error:code/fn:string()
      let $memberObject :=  
        if ($memberInformation or $subscriberIdElement or $memberAdditionalIdentifier or $memberRelatedDates or $memberName or
      $memberResidenceAddress or $memberResidenceAddress or $memberDemographics or $healthCoverage or $additionalInfo) then
          element bem:Member {
          local:oneOrNone($memberInformation),
          local:oneOrNone($subscriberIdElement),
          local:oneOrNone($memberAdditionalIdentifier),
          local:oneOrNone($memberRelatedDates),
          if ($memberName or $memberResidenceAddress or $memberResidenceAddress or $memberDemographics) then
            element bem:MemberNameInformation {
              local:oneOrNone($memberName),
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

declare function local:retrieveIssuerOrganization($hiosId as xs:string) {
let $orgRefTypeCode := "6"

let $hiosid-query as cts:query :=
cts:element-query (
xs:QName("p:issuerOrganization"),
cts:element-value-query(
xs:QName("p:issuerHIOSID"),
$hiosId
)
)

let $orgType-query as cts:query :=
cts:element-query (
xs:QName("p:organizationType"),
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


declare function local:mockIssuer() as element() {
let $issuerName := "NOTAVAILABLE"
let $issuerTIN := "123456789"
let $issuer := <bem:Issuer></bem:Issuer>
let $issuer :=
<bem:Issuer>
{ if ($issuerName) then <bem:Name>{$issuerName}</bem:Name> else() }
{ if ($issuerTIN) then <bem:TaxPayerIdentificationNumber>{local:replace($issuerTIN, "-", "")}</bem:TaxPayerIdentificationNumber> else() }
</bem:Issuer>

let $issuer :=
if($issuer/node()) then $issuer
else ()
let $issuer := local:checkEmptyNode($issuer)

let $error := local:assertNotEmpty($issuer, " Issuer Node")

return $issuer
};

declare function local:compareResidenceAndMailingAddress($homeAddress as element(), $mailAddress as element()) as xs:integer {

let $streetName1 := $mailAddress/p:definingAddressPlace/p:streetName1
let $streetName2 := $mailAddress/p:definingAddressPlace/p:streetName2
let $cityName := $mailAddress/p:definingAddressPlace/p:cityName
let $stateCode := $mailAddress/p:definingAddressPlace/p:stateCode
let $zipPlus4Code := $mailAddress/p:definingAddressPlace/p:zipPlus4Code
(: let $countryCode := $mailAddress/p:definingAddressPlace/p:countryCode :)

let $mAddress := fn:concat($streetName1, $streetName2, $cityName, $stateCode, $zipPlus4Code)

let $streetName1 := $homeAddress/p:definingAddressPlace/p:streetName1
let $streetName2 := $homeAddress/p:definingAddressPlace/p:streetName2
let $cityName := $homeAddress/p:definingAddressPlace/p:cityName
let $stateCode := $homeAddress/p:definingAddressPlace/p:stateCode
let $zipPlus4Code := $homeAddress/p:definingAddressPlace/p:zipPlus4Code
(: let $countryCode := $homeAddress/p:definingAddressPlace/p:countryCode :)

let $rAddress := fn:concat($streetName1, $streetName2, $cityName, $stateCode, $zipPlus4Code)
let $ret := fn:compare($rAddress, $mAddress)
return $ret
};

declare function local:mapParentName($insuredMember as element(), $section as xs:string) {

  let $memberName := $insuredMember

  let $ret :=
    element {$section}
    {
      if($memberName/p:lastName) then
        <bem:LastName>{$memberName/p:lastName/fn:string()}</bem:LastName>
      else()
      ,
      if($memberName/p:firstName) then
        <bem:FirstName>{$memberName/p:firstName/fn:string()}</bem:FirstName>
      else()
      ,
      if($memberName/p:middleName) then
        <bem:MiddleName>{$memberName/p:middleName/fn:string()}</bem:MiddleName>
      else()
      ,
      if($memberName/p:salutationName) then
        <bem:NamePrefix>{$memberName/p:salutationName/fn:string()}</bem:NamePrefix>
      else()
      ,
      if($memberName/p:suffixName) then
        <bem:NameSuffix>{$memberName/p:suffixName/fn:string()}</bem:NameSuffix>
      else()
    }


  let $ret := local:checkEmptyNode($ret)

  return $ret
};


declare function local:mapParentCommunicationInformation($insuredMember as element(), $section as xs:string){
let $homeTelRefCode := "2"
let $cellTelRefCode := "3"
let $beeperTelRefCode := "5"

let $telephoneNumber := $insuredMember/p:specifiedMemberTelephone[./p:specifyingContactMethodType/pb:referenceTypeCode=$homeTelRefCode]/p:specifyingTelephoneNumber/p:fullNumberCode
let $alternateTelephoneNumber := $insuredMember/p:specifiedMemberTelephone[./p:specifyingContactMethodType/pb:referenceTypeCode=$cellTelRefCode]/p:specifyingTelephoneNumber/p:fullNumberCode
let $beeperNumber := $insuredMember/p:specifiedMemberTelephone[./p:specifyingContactMethodType/pb:referenceTypeCode=$beeperTelRefCode]/p:specifyingTelephoneNumber/p:fullNumberCode

let $emailId := $insuredMember/p:memberEmailAddress/p:emailAddressFullText

let $ret :=
element {$section}
{
if($telephoneNumber/fn:string()) then
<bem:TelephoneNumber>{local:replace($telephoneNumber/fn:string(), "-", "")}</bem:TelephoneNumber>
else if($alternateTelephoneNumber/fn:string()) then
<bem:TelephoneNumber>{local:replace($alternateTelephoneNumber/fn:string(), "-", "")}</bem:TelephoneNumber>
else
()
,
if($alternateTelephoneNumber/fn:string()) then
<bem:AlternateTelephoneNumber>{local:replace($alternateTelephoneNumber/fn:string(), "-", "")}</bem:AlternateTelephoneNumber>
else
()
,
if($emailId/fn:string()) then
<bem:EmailID>{$emailId/fn:string()}</bem:EmailID>
else
if($beeperNumber/fn:string()) then
<bem:BeeperNumber>{local:replace($beeperNumber/fn:string(), "-", "")}</bem:BeeperNumber>
else
()
}

let $ret := local:checkEmptyNode($ret)

return $ret
};

declare function local:mapParentAddress($insuredMember as element(), $section as xs:string) {
let $mailRefTypCode := "2"
let $mailCatTypCode := "1"
let $mailAddress :=
$insuredMember/p:specifiedMemberAddress[./p:definingAddressRelationshipRoleType/pb:referenceTypeCode/fn:string() = $mailRefTypCode
and ./p:definingAddressCategoryType/pb:referenceTypeCode/fn:string() = $mailCatTypCode][1]

let $homeRefTypCode := "1"
let $homeCatTypCode := "2"
let $homeAddress :=
$insuredMember/p:specifiedMemberAddress[./p:definingAddressRelationshipRoleType/pb:referenceTypeCode/fn:string() = $homeRefTypCode
and ./p:definingAddressCategoryType/pb:referenceTypeCode/fn:string() = $homeCatTypCode][1]

let $address :=
if ($mailAddress) then
$mailAddress
else
($homeAddress)

let $streetName1 := $address/p:definingAddressPlace/p:streetName1
let $streetName2 := $address/p:definingAddressPlace/p:streetName2
let $cityName := $address/p:definingAddressPlace/p:cityName
let $stateCode := $address/p:definingAddressPlace/p:stateCode
let $zipPlus4Code := $address/p:definingAddressPlace/p:zipPlus4Code
(:let $countryCode := $address/p:definingAddressPlace/p:countryCode :) (: ADD THE COUNTRY CODE SECTION TO THE ELEMENT :)
let $countyCode := $address/p:definingAddressPlace/p:countyFipsCode

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
<bem:PostalCode>{local:replace($zipPlus4Code/fn:string(), "-", "")}</bem:PostalCode>
else()

}

let $ret := local:checkEmptyNode($ret)

return $ret
};

declare function local:getCustodialOrResponsible($coveredInsuredMember as element()){

let $ret :=
if ($coveredInsuredMember/p:insuredMemberRelationship/p:insuredMemberAssociationReason/pb:referenceTypeCode = "1" ) then
let $specifyingMemberSSN := $coveredInsuredMember/p:insuredMemberRelationship/p:specifyingMember/p:memberSSN/fn:string()
return $specifyingMemberSSN
else
if ($coveredInsuredMember/p:insuredMemberRelationship/p:insuredMemberAssociationReason/pb:referenceTypeCode = "2" ) then
let $specifyingMemberSSN := $coveredInsuredMember/p:insuredMemberRelationship/p:specifyingMember/p:memberSSN/fn:string()
return $specifyingMemberSSN
else
()
let $ret := local:emptyElementToSring($ret)

return $ret
};

declare function local:getPolicySpecifyingPerson($insurancePlanPolicy as element()) {

let $policySubscriber := $insurancePlanPolicy/p:coveredInsuredMember[./p:subscriberIndicator/fn:string() = "true"]
let $subscriberSpecifyingPerson :=
if ($policySubscriber) then
let $subscriberSpecifyingPerson := $policySubscriber/p:definingMember/p:specifyingPerson/fn:string()
return $subscriberSpecifyingPerson
else ()
return $subscriberSpecifyingPerson
};
declare function local:compareSpecifyingPersons2($specifyingPerson, $insurancePlanPolicy as element()) {
let $policySubscriber := local:getPolicySpecifyingPerson($insurancePlanPolicy)
let $match :=
if ($policySubscriber) then
let $subscriberSpecifyingPerson := $policySubscriber/p:definingMember/p:specifyingPerson/fn:string()
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

declare function local:compareSpecifyingPersons($specifyingPerson1, $specifyingPerson2) {
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
declare function local:addOneSecToLastModified($lastModified) {
  let $lastModified := xs:dateTime($lastModified)
  let $newTime := $lastModified + xs:dayTimeDuration("PT1S")
  return $newTime
};

declare function local:toDate($dateOrDateTime) {
  if($dateOrDateTime castable as xs:date) then xs:date($dateOrDateTime)
  else if($dateOrDateTime castable as xs:dateTime) then xs:date(xs:dateTime($dateOrDateTime))
  else $dateOrDateTime
};


 


for $group in $URI 
return 
  if ((fn:contains($group, "newHighwaterMark"))) then
    xdmp:unquote($group)//text()
  else
    let $hydrated := xdmp:unquote($group)/node()
    let $tenant := $hydrated/tenant/fn:string()
	let $newHighwaterMark := $hydrated/newHWMDate/fn:string()
    let $qhpId := $hydrated/qhp/fn:string()
	let $uris := $hydrated/uri/fn:string()
  
  let $bems :=
		for $uri in $uris
		let $policy := fn:doc($uri)/node()
		let $policyLastModifiedTime := ($policy/b:lastModified/fn:string())[1]	

		let $bemEntries :=
			if(fn:not($policyLastModifiedTime) or (xs:dateTime($policyLastModifiedTime) <= xs:dateTime($newHighwaterMark) )) 
			then
				local:forInitialEnrollment($uri, $tenant, $mode)
			else 
				let $uri-string := fn:concat(fn:substring-before($uri, ".xml"),"/")
				let $histURIS := local:getHistPolicies($uri-string,xs:dateTime($newHighwaterMark),xs:dateTime($highwaterMark))
				let $histBEMEntries :=
					for $histURI in $histURIS	
					return
						if( fn:not($histURI = $uris) ) then
							local:forInitialEnrollment($histURI, $tenant, $mode)
						else ()
				return $histBEMEntries
		return $bemEntries 

  let $result :=  

  if(fn:not(fn:contains($group,"part"))) then
		
			  element bem:BenefitEnrollmentRequest {
				element bem:FileInformation {
				  element bem:GroupSenderID { $qhpId },
				  element bem:GroupReceiverID { $tenant || "0" }
				},
				 $bems     
			  }
	else 
		$bems   
           
return $result
		 
	    