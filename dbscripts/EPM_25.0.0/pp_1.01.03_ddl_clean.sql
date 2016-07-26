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
	SUBSTR(sys_context('USERENV', 'MODULE'), instr(sys_context('USERENV', 'MODULE'), ' ') +1) || '_' || to_char(sysdate, 'yyyymmdd') date_column,
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

SELECT COUNT(*) FROM PolicyPremium 
WHERE insrncPlanVariantCmptTypeCd is not null;

alter session enable parallel dml;

MERGE INTO /*+ parallel(PolicyPremium) */ PolicyPremium
USING
(
SELECT /*+ parallel(PolicyVersion) */ 
       policyVersionId, insrncPlanVariantCmptTypeCd
FROM   PolicyVersion
)      PV
ON     (PV.policyVersionId = PolicyPremium.policyVersionId)
WHEN MATCHED THEN UPDATE SET
       PolicyPremium.insrncPlanVariantCmptTypeCd = PV.insrncPlanVariantCmptTypeCd,
       PolicyPremium.lastModifiedDateTime = SYSDATE,
       PolicyPremium.lastModifiedBy = 'TJF'
;

SELECT COUNT(*) FROM PolicyPremium 
WHERE insrncPlanVariantCmptTypeCd is not null;

commit;
spool off
