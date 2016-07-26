

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


CREATE OR REPLACE VIEW InsrncPlanRateAreaView ( hiosStandardCmptId,exchangeRateAreaId,marketYear,createDateTime,lastModifiedDateTime,createBy,lastModifiedBy ) 
	 AS  SELECT /*+ USE_HASH(InsrncPlanRateArea InsrncPlan) */InsrncPlanRateArea.hiosStandardCmptId,InsrncPlanRateArea.exchangeRateAreaId,InsrncPlanRateArea.marketYear,InsrncPlanRateArea.createDateTime,InsrncPlanRateArea.lastModifiedDateTime,InsrncPlanRateArea.createBy,InsrncPlanRateArea.lastModifiedBy
		FROM InsrncPlanRateArea ,InsrncPlan 
		WHERE insrncPlan.hiosStandardCmptId = insrncPlanRateArea.hiosStandardCmptid
and insrncPlan.marketYear = insrncPlanRateArea.marketYear
and insrncPlan.marketYear not in ( '2014')
and insrncPlan.insrncPlanExchgMarketTypeCd = '1'
and insrncPlan.InsrncMarketCoverageTypeCd = '1'
;


spool off
