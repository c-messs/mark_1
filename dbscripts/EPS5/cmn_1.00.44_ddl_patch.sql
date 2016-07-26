

set SERVEROUTPUT ON FORMAT WRAPPED
set APPINFO ON
SET VERIFY OFF

SET FEEDBACK OFF
SET TERMOUT OFF

column date_column new_value today_var
column scriptname new_value thescriptname;
column sessionname new_value sessionname;
column dbname new_value dbname;
select 
	SUBSTR(sys_context('USERENV', 'MODULE'), instr(sys_context('USERENV', 'MODULE'), ' ') +1) || '_' ||  sys_context('USERENV','SESSION_USER') || '_' ||sys_context('USERENV','DB_NAME')  || '_'|| to_char(sysdate, 'yyyymmdd') date_column,
  	sys_context('USERENV', 'MODULE') scriptname,
	sys_context('USERENV','SESSION_USER') sessionname,
	sys_context('USERENV','DB_NAME') dbname
from dual;

SPOOL &today_var..log

SET VERIFY ON
SET FEEDBACK ON
SET TERMOUT On
SET ECHO ON

insert into appdba_util.scriptinventory (
SCRIPTNAME,
CREATEDATE,
SCHEMANAME,
DATABASE,
ROLLBACKIND,
FAILUREIND,
localGlobalInd)
values
('&thescriptname',
 sysdate,
 '&sessionname',
 '&dbname',
 'N',
 'N',
 'G');
commit;

CREATE OR REPLACE VIEW InsrncPlanView ( hiosStandardCmptId,marketYear,issuerId,hiosProductId,InsrncPlanNm,InsrncPlanUniquePlanDesignInd,InsrncPlanStartDate,InsrncPlanEndDate,InsrncPlanExchgMarketTypeCd,InsrncPlanBnftMetlTierTypeCd,newPlanInd,InsrncMarketCoverageTypeCd,InsrncProductDivisionTypeCd,createDateTime,lastModifiedDateTime,createBy,lastModifiedBy ) 
	 AS  SELECT InsrncPlan.hiosStandardCmptId,InsrncPlan.marketYear,InsrncPlan.issuerId,InsrncPlan.hiosProductId,InsrncPlan.InsrncPlanNm,InsrncPlan.InsrncPlanUniquePlanDesignInd,InsrncPlan.InsrncPlanStartDate,InsrncPlan.InsrncPlanEndDate,InsrncPlan.InsrncPlanExchgMarketTypeCd,InsrncPlan.InsrncPlanBnftMetlTierTypeCd,InsrncPlan.newPlanInd,InsrncPlan.InsrncMarketCoverageTypeCd,InsrncPlan.InsrncProductDivisionTypeCd,InsrncPlan.createDateTime,InsrncPlan.lastModifiedDateTime,InsrncPlan.createBy,InsrncPlan.lastModifiedBy
		FROM InsrncPlan 
		WHERE InsrncPlanExchgMarketTypeCd = '1'
and marketYear not in ('2014')
and InsrncMarketCoverageTypeCd = '1';

CREATE OR REPLACE VIEW InsrncPlanRateAreaView ( hiosStandardCmptId,exchangeRateAreaId,marketYear,createDateTime,lastModifiedDateTime,createBy,lastModifiedBy ) 
	 AS  SELECT InsrncPlanRateArea.hiosStandardCmptId,InsrncPlanRateArea.exchangeRateAreaId,InsrncPlanRateArea.marketYear,InsrncPlanRateArea.createDateTime,InsrncPlanRateArea.lastModifiedDateTime,InsrncPlanRateArea.createBy,InsrncPlanRateArea.lastModifiedBy
		FROM InsrncPlanRateArea ,InsrncPlan 
		WHERE insrncPlan.hiosStandardCmptId = insrncPlanRateArea.hiosStandardCmptid
and insrncPlan.marketYear = insrncPlanRateArea.marketYear
and insrncPlan.marketYear not in ( '2014')
and insrncPlan.insrncPlanExchgMarketTypeCd = '1'
and insrncPlan.InsrncMarketCoverageTypeCd = '1'
;

CREATE OR REPLACE VIEW InsrncPlanVariantCmptView ( hiosStandardCmptId,InsrncPlanVariantCmptTypeCd,marketYear,individualMedicalMOOPAmount,familyMedicalMOOPAmount,createDateTime,lastModifiedDateTime,createBy,lastModifiedBy ) 
	 AS  SELECT InsrncPlanVariantCmpt.hiosStandardCmptId,InsrncPlanVariantCmpt.InsrncPlanVariantCmptTypeCd,InsrncPlanVariantCmpt.marketYear,InsrncPlanVariantCmpt.individualMedicalMOOPAmount,InsrncPlanVariantCmpt.familyMedicalMOOPAmount,InsrncPlanVariantCmpt.createDateTime,InsrncPlanVariantCmpt.lastModifiedDateTime,InsrncPlanVariantCmpt.createBy,InsrncPlanVariantCmpt.lastModifiedBy
		FROM InsrncPlanVariantCmpt ,InsrncPlan 
		WHERE insrncPlan.hiosStandardCmptId = insrncPlanVariantCmpt.hiosStandardCmptid
and insrncPlan.marketYear = insrncPlanVariantCmpt.marketYear
and insrncPlan.marketYear not in ( '2014')
and insrncPlan.insrncPlanExchgMarketTypeCd = '1'
and insrncPlan.InsrncMarketCoverageTypeCd = '1';

spool off
