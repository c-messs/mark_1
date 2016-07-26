xquery version "1.0-ml";

declare namespace p  = "http://persistence.ffe.cms.hhs.gov";
declare namespace pb = "http://persistence.base.cms.hhs.gov";
declare namespace b  = "http://base.persistence.base.cms.hhs.gov";
declare namespace ns2 = "http://persistence.fm.ff.cms.hhs.gov";

import module namespace audit = "http://ffx.ffe.gov/lib/util/audit" at "/lib/util/audit/audit.xqy";

declare variable $DEBUG as xs:boolean := fn:true();
declare variable $COLLECTION as xs:string := "EPS-generated-mock-data";


(: How many documents should have mock data inserted into them? :)
declare variable $NUMDOCSTOGENERATE as xs:integer := 4;

(: Specific document URIs to insert mock data into. If empty, then it randomly selects IPPsfrom the database. :)
declare variable $URISTOMODIFY as xs:string* := ();


(: Randomly select some IPPs that don't already have the proration element in them. :)
let $ipps := 
  if ($URISTOMODIFY and fn:count($URISTOMODIFY) ge 1) then 
    for $uri in $URISTOMODIFY
    return fn:doc($uri)/element()
  else
    cts:search(/p:insurancePlanPolicy,
      cts:and-query((
        cts:element-value-query(xs:QName("b:deleted"), "false"),
        cts:not-query(cts:element-value-query(xs:QName("pb:isCurrentVersion"), "false")),
        cts:element-query(xs:QName("p:calculatedInsurancePolicyPremium"), cts:and-query(()) ),
        cts:not-query(cts:element-query(xs:QName("p:calculatedPolicyPremiumProration"), cts:and-query(()) )),
        cts:element-query(xs:QName("p:applicableCostSharingReduction"), cts:and-query(()) ),
        cts:not-query(cts:element-query(xs:QName("p:calculatedPolicyCSRProration"), cts:and-query(()) ))
      ))
      ,("score-random")
    )[1 to $NUMDOCSTOGENERATE]


for $ipp in $ipps
let $uri := $ipp/fn:base-uri()
let $cipp := $ipp/p:calculatedInsurancePolicyPremium[fn:last()]
let $acsr := $ipp/p:applicableCostSharingReduction[fn:last()]

(: Mock data -- hardcoded :)
let $mockcalculatedPolicyPremiumProration :=
  element p:calculatedPolicyPremiumProration {
    element p:proratedMonth { "567.89" },
    element p:proratedAmountStartDate { "2015-08-01T08:00:00-07:00" },
    element p:proratedAmountEndDate { "2015-08-07T09:30:00-05:00" },
    element p:proratedMonthlyPremiumAmount { "123.45" },
    element p:proratedEHBPremiumAmount { "678.90" },
    element p:proratedAppliedAPTCAmount { "345.67" },
    element p:proratedIndividualResponsibleAmount { "234.56" }
  }

let $mockcalculatedPolicyCSRProration :=
  element p:calculatedPolicyCSRProration {
    element p:proratedCSRMonth { "789.01" },
    element p:proratedCSRStartDate { "2015-08-01T08:00:00-07:00" },
    element p:proratedCSREndDate { "2015-08-07T09:30:00-05:00" },
    element p:proratedCSRAmount { "456.0" }
  }


(: Insert the mock data into the IPP :)
return
  if ($DEBUG) then (
    "[DEBUG] WOULD HAVE Inserted mock calculatedPolicyPremiumProration element as child of "
    || fn:node-name($cipp) ||" element in IPP document " || $uri,
    "[DEBUG] WOULD HAVE Inserted mock calculatedPolicyCSRProration element as child of "
    || fn:node-name($acsr) ||" element in IPP document " || $uri

  ) else (
    "Inserted mock calculatedPolicyPremiumProration element into IPP " || $uri,
    xdmp:node-insert-child($cipp, $mockcalculatedPolicyPremiumProration),
    "Inserted mock calculatedPolicyCSRProration element into IPP " || $uri,
    xdmp:node-insert-child($acsr, $mockcalculatedPolicyCSRProration),
    audit:audit($COLLECTION, $uri, $audit:CHANGE-TYPE-UPDATE, 
      (element noelement{}, element noelement{}), 
      ($mockcalculatedPolicyPremiumProration, $mockcalculatedPolicyCSRProration)
    )
  )

