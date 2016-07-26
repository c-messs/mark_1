xquery version "1.0-ml";
 
import module namespace sm = "http://marklogic.com/ps/servicemetrics" at "/lib/servicemetrics/metrics/servicemetrics.xqy";
 
declare namespace pb = "http://persistence.base.cms.hhs.gov";
declare namespace p = "http://persistence.ffe.cms.hhs.gov";
declare namespace b = "http://base.persistence.base.cms.hhs.gov";

declare namespace bem = "http://bem.dsh.cms.gov";

declare variable $highwaterMark         as xs:string external;
declare variable $numInBatch            as xs:string external;
declare variable $highwatermarkFilename as xs:string external;
declare variable $manifestFilename      as xs:string external;
declare variable $jobId                 as xs:string external;
declare variable $planPolicyYearFilter 	as xs:string external;
declare variable $bemsPerFile 	        as xs:string external;
declare variable $PAET as xs:string external; 
declare variable $PAETCompletion as xs:string external;
declare variable $planPolicyStartDateFilter := xs:dateTime($planPolicyYearFilter || "-01-01T00:00:00-05:00");
declare variable $jobStartTime := fn:current-dateTime();




let $highwaterMarkDate := xs:dateTime( $highwaterMark )

let $PAETDate :=  xs:dateTime( $PAET )

let $PAETCompletionIndicator := if ($PAETCompletion eq "Y") then 
									$PAETCompletion
								else 
									"N"
									


let $EEPAETCount := fn:count(cts:element-values(xs:QName("p:preAuditCutOffDateTime"), (),("document","score-zero"),(cts:element-query(xs:QName("p:preAuditRunMetadata"),
                            cts:and-query((
                              cts:element-value-query(xs:QName("p:preAuditCoverageYearNumber"),$planPolicyYearFilter),
                              cts:element-value-query(xs:QName("p:preAuditRunType") ,"FULL"),
							                cts:element-range-query(xs:QName("p:preAuditCutOffDateTime"), ">", $PAETDate	)
                            ))
                        )
					)))
					
let $EEPAETExists := if ( $EEPAETCount > 0) then "Y" else "N"					
				   									
(: Get the PAET date from the EE tower to be used while determining the documents to bbe extracted :)
let $EEPAET :=  fn:adjust-dateTime-to-timezone(fn:min(cts:element-values(xs:QName("p:preAuditCutOffDateTime"), (),("document","score-zero"),
                        cts:element-query(xs:QName("p:preAuditRunMetadata"),
                            cts:and-query((
                              cts:element-value-query(xs:QName("p:preAuditCoverageYearNumber"),$planPolicyYearFilter),
                              cts:element-value-query(xs:QName("p:preAuditRunType") ,"FULL"),
							  cts:element-range-query(xs:QName("p:preAuditCutOffDateTime"), ">", $PAETDate	)
                            ))
                        ), 0.0
                    )
                )
			)


				
(: Calculate the filter based on latest PAET from EE/Properties, previous PAET date and previous PAET completion :)				
let $preAuditExtractMarkDateTimeFilter := 
	if( $EEPAETExists eq "Y" ) then
	
		xs:dateTime( $EEPAET )

	else
	
		if( $PAETCompletionIndicator eq "N" )	then
		    if ( $highwaterMarkDate < $PAETDate) then
			 $PAETDate
			else  
			 xs:dateTime("9999-12-31T00:00:00-05:00")
		else 
			 xs:dateTime("9999-12-31T00:00:00-05:00") (: Till the end of the time :) 


(: Get the PAET to be set in manifest file :)			
let $newPAET :=
	if( $EEPAETExists eq "Y"  ) then
	    	xs:dateTime( $EEPAET )
	else
		$PAETDate

let $jobId :=
  if ( xdmp:castable-as("http://www.w3.org/2001/XMLSchema","integer",$jobId) ) then $jobId
  else 1


let $jobStartTimeMinusOneMinute := $jobStartTime - xs:dayTimeDuration("PT1M") 

let $uris :=
(
  for $ipp in cts:search(/p:insurancePlanPolicy, 
    cts:and-query((
      cts:element-range-query(xs:QName("b:lastModified"), ">", $highwaterMarkDate),
	    cts:element-range-query(xs:QName("b:lastModified"), "<", $preAuditExtractMarkDateTimeFilter), (: SP - Addded condition to only retrieve till Pre Audit Timestamp configured.:)
      cts:element-range-query(xs:QName("b:lastModified"), "<", $jobStartTimeMinusOneMinute),      (: AV - extracting data until 1minute prior to the jobStartTime :)
      cts:element-range-query(xs:QName("p:insurancePlanPolicyStartDate"), ">=", $planPolicyStartDateFilter)
    ))
  )
  order by $ipp/b:lastModified ascending
  return $ipp/base-uri()
)[1 to xs:integer($numInBatch)]

let $tempNewHighWaterMark := xs:dateTime(fn:doc($uris[fn:last()])/node()/b:lastModified/fn:string())

(: Set new highwater mark if documents have been retrieved else set the previous highwatermark to set in the Manifest file. :)	
let $newHighwaterMark := 
		if( xdmp:castable-as("http://www.w3.org/2001/XMLSchema","dateTime",$tempNewHighWaterMark) ) then
			xs:dateTime( $tempNewHighWaterMark )
		else 
			xs:dateTime($highwaterMark)

let $totalIPPCountTobeExtracted := xdmp:estimate(cts:search(/p:insurancePlanPolicy, 
    cts:and-query((
      cts:element-range-query(xs:QName("b:lastModified"), ">",  $tempNewHighWaterMark),
	  cts:element-range-query(xs:QName("b:lastModified"), "<=",  $jobStartTime),
      cts:element-range-query(xs:QName("p:insurancePlanPolicyStartDate"), ">=", $planPolicyStartDateFilter)
    ))
  ))
			
let $pendingCount := xdmp:estimate(cts:search(/p:insurancePlanPolicy, 
    cts:and-query((
      cts:element-range-query(xs:QName("b:lastModified"), ">",  $tempNewHighWaterMark),
	  cts:element-range-query(xs:QName("b:lastModified"), "<", $preAuditExtractMarkDateTimeFilter),
      cts:element-range-query(xs:QName("p:insurancePlanPolicyStartDate"), ">=", $planPolicyStartDateFilter)
    ))
  ))

let $qhpIds := 
  cts:element-value-co-occurrences(
    xs:QName("p:selectedInsurancePlan"),
    xs:QName("xdmp:document"),
    ("collation-2=http://marklogic.com/collation/codepoint","map"),
    cts:element-query(
      xs:QName("p:insurancePlanPolicy"),
        cts:and-query((
          cts:element-range-query(xs:QName("b:lastModified"), ">",  $highwaterMarkDate),
          cts:element-range-query(xs:QName("b:lastModified"), "<=", $tempNewHighWaterMark),
          cts:element-range-query(xs:QName("p:insurancePlanPolicyStartDate"), ">=", $planPolicyStartDateFilter)  (: AV-- Added condition to filter for 2015 policies and greater :)
        ))
    )
  )

(: Set PAET completion status to be set in the Manifest file. :)	  
let $newPAETCompletionStatus := if ($jobStartTime >= xs:dateTime($newPAET) and $newHighwaterMark <= xs:dateTime($newPAET) and $pendingCount = 0 ) then
	xs:string("Y") (: PAETCompletion set to true if we have cross the preAuditExtract time and we do not have any more files to process :)
else
	xs:string("N")  
  
  
let $results := (
  (: Write out highwatermark properties file :)
  element 
  { 
    xs:QName("newHighwaterMark-" || fn:tokenize($highwatermarkFilename, "/")[fn:last()])
  } 
  {(:"newHighwaterMark-" || fn:replace(fn:string($newHighwaterMark), "[:\-]", "-") || ".properties") }:)
    text { 
      "export highwaterMark=" || $newHighwaterMark || "&#10;"
       || "export jobId=" || (xs:integer($jobId) + 1) || "&#10;"
	   || "export PAET=" || $newPAET || "&#10;"  
	   || "export PAETCompletion=" || $newPAETCompletionStatus || "&#10;"  
	   || "export totalIPPCountTobeExtracted=" || $totalIPPCountTobeExtracted || "&#10;" 
	   || "export pendingExtractCount=" || $pendingCount || "&#10;"     
    }
  }
  ,
  (: Write EndHighWaterMark to a file instead of using xdmp:save method. :)
  element 
  { 
    xs:QName("newHighwaterMark") 
  } 
  {
    text {"EndHighWaterMark=" || $newHighwaterMark  || "&#10;"
	      || "PAETCompletion=" || $newPAETCompletionStatus  }
  }
  ,
  for $qhp in map:keys($qhpIds)
	  let $tenant := fn:replace($qhp, "^[0-9]+([A-Za-z]+)[0-9]+$", "$1")
	  
	  let $currentDate := fn:format-date(fn:current-date(), "[Y01][M01][D01]") 
	  let $currentTime := fn:format-dateTime(fn:current-dateTime(), "[H01][m01][s01][f001]")	
	  
	  let $allUris := map:get($qhpIds, $qhp)
      let $threshold := xs:unsignedLong($bemsPerFile)
      let $loop := fn:ceiling((fn:count($allUris)) div $threshold )
   	   
	   for $i in (1 to $loop)
	       
		let $partNum := if ($i eq 1) then ""
							else ".part" || $i
        let $splitUris := $allUris[($threshold * ($i - 1)) + 1 to $threshold * $i]      
							
		return xdmp:quote(
			element 
			{ 
				xs:QName("FFM834." || $jobId 
				  || ".D" || $currentDate 
				  || ".T" || $currentTime 
				  || ".T."  || $qhp || $partNum ) 
			} 
			{
			  element tenant { $tenant },
			  element qhp { $qhp },
			  element newHWMDate {$newHighwaterMark},
			  for $uri in $splitUris
			  return element uri { $uri }
			}	
			
  )
)

return (fn:count($results), $results)

