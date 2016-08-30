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

MERGE INTO SBMTransMsgProcStatusType using dual ON ( SBMTransMsgProcStatusTypeCd = 'RJC') when matched then update set SBMTransMsgProcStatusTypeNm='Rejected' ,SBMTransMsgProcStatusTypeDesc = 'Policy that failed schema and business validations, or belonging to a file that failed error threshold validation.'  When not matched then insert(SBMTransMsgProcStatusTypeCd,SBMTransMsgProcStatusTypeNm ,  SBMTransMsgProcStatusTypeDesc) values ('RJC','Rejected','Policy that failed schema and business validations, or belonging to a file that failed error threshold validation.');
MERGE INTO SBMTransMsgProcStatusType using dual ON ( SBMTransMsgProcStatusTypeCd = 'PNC') when matched then update set SBMTransMsgProcStatusTypeNm='Processed – No change' ,SBMTransMsgProcStatusTypeDesc = 'Matched Policy record was processed and no changes were found'  When not matched then insert(SBMTransMsgProcStatusTypeCd,SBMTransMsgProcStatusTypeNm ,  SBMTransMsgProcStatusTypeDesc) values ('PNC','Processed – No change','Matched Policy record was processed and no changes were found');
MERGE INTO SBMTransMsgProcStatusType using dual ON ( SBMTransMsgProcStatusTypeCd = 'ACC') when matched then update set SBMTransMsgProcStatusTypeNm='Accepted' ,SBMTransMsgProcStatusTypeDesc = 'Matched Policy record processed successfully with changes found and is awaiting CMS approval response or New policy processed successfully and is waiting CMS approval response.'  When not matched then insert(SBMTransMsgProcStatusTypeCd,SBMTransMsgProcStatusTypeNm ,  SBMTransMsgProcStatusTypeDesc) values ('ACC','Accepted','Matched Policy record processed successfully with changes found and is awaiting CMS approval response or New policy processed successfully and is waiting CMS approval response.');

spool off
