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

SELECT count(*) FROM SBMTransMsgProcStatusType;

MERGE INTO SBMTransMsgProcStatusType using dual ON ( SBMTransMsgProcStatusTypeCd = 'ACC') 
when matched then update set SBMTransMsgProcStatusTypeNm='Accepted With SBM Change' ,SBMTransMsgProcStatusTypeDesc = 'Matched Policy record processed successfully with changes found and is awaiting CMS approval response or new policy processed successfully and is waiting CMS approval response.'  
When not matched then insert(SBMTransMsgProcStatusTypeCd,SBMTransMsgProcStatusTypeNm ,  SBMTransMsgProcStatusTypeDesc) 
values ('ACC','Accepted With SBM Change','Matched Policy record processed successfully with changes found and is awaiting CMS approval response or new policy processed successfully and is waiting CMS approval response.');

MERGE INTO SBMTransMsgProcStatusType using dual ON ( SBMTransMsgProcStatusTypeCd = 'AEC') 
when matched then update set SBMTransMsgProcStatusTypeNm='Accepted With EPS Change' ,SBMTransMsgProcStatusTypeDesc = 'New or matched policy with EPS change applied to XPR.'  
When not matched then insert(SBMTransMsgProcStatusTypeCd,SBMTransMsgProcStatusTypeNm ,  SBMTransMsgProcStatusTypeDesc) 
values ('AEC','Accepted With EPS Change','New or matched policy with EPS change applied to XPR.');

SELECT count(*) FROM SBMTransMsgProcStatusType;
commit;

spool off

