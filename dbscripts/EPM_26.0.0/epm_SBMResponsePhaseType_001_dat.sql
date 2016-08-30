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

MERGE INTO SBMResponsePhaseType using dual ON ( SBMResponsePhaseTypeCd = 'I') when matched then update set SBMResponsePhaseTypeNm='Initial SBMR' ,SBMResponsePhaseTypeDesc = 'Initial SBMR'  When not matched then insert(SBMResponsePhaseTypeCd,SBMResponsePhaseTypeNm ,  SBMResponsePhaseTypeDesc) values ('I','Initial SBMR','Initial SBMR');

MERGE INTO SBMResponsePhaseType using dual ON ( SBMResponsePhaseTypeCd = 'F') when matched then update set SBMResponsePhaseTypeNm='Final SBMR' ,SBMResponsePhaseTypeDesc = 'Final SBMR'  When not matched then insert(SBMResponsePhaseTypeCd,SBMResponsePhaseTypeNm ,  SBMResponsePhaseTypeDesc) values ('F','Final SBMR','Final SBMR');

MERGE INTO SBMResponsePhaseType using dual ON ( SBMResponsePhaseTypeCd = 'S') when matched then update set SBMResponsePhaseTypeNm='SBM Status' ,SBMResponsePhaseTypeDesc = 'SBM Status'  When not matched then insert(SBMResponsePhaseTypeCd,SBMResponsePhaseTypeNm ,  SBMResponsePhaseTypeDesc) values ('S','SBM Status','SBM Status');

commit;

spool off
