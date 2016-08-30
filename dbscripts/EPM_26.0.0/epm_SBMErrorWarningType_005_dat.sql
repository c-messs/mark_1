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

MERGE INTO SBMErrorWarningType using dual ON ( SBMErrorWarningTypeCd = 'ER-519') when matched then update set SBMErrorWarningTypeNm='File requested on approval template is part of a file set' ,SBMErrorWarningTypeDesc = 'File requested on approval template is part of a file set'  When not matched then insert(SBMErrorWarningTypeCd,SBMErrorWarningTypeNm ,  SBMErrorWarningTypeDesc) values ('ER-519','File requested on approval template is part of a file set','File requested on approval template is part of a file set');

MERGE INTO SBMErrorWarningType using dual ON ( SBMErrorWarningTypeCd = 'ER-520') when matched then update set SBMErrorWarningTypeNm='File or IssuerFileSet to bypass not found' ,SBMErrorWarningTypeDesc = 'File or IssuerFileSet to bypass not found'  When not matched then insert(SBMErrorWarningTypeCd,SBMErrorWarningTypeNm ,  SBMErrorWarningTypeDesc) values ('ER-520','File or IssuerFileSet to bypass not found','File or IssuerFileSet to bypass not found');

MERGE INTO SBMErrorWarningType using dual ON ( SBMErrorWarningTypeCd = 'ER-521') when matched then update set SBMErrorWarningTypeNm='File or IssuerFileSet to bypass not ready for bypass freeze' ,SBMErrorWarningTypeDesc = 'File or IssuerFileSet to bypass not ready for bypass freeze'  When not matched then insert(SBMErrorWarningTypeCd,SBMErrorWarningTypeNm ,  SBMErrorWarningTypeDesc) values ('ER-521','File or IssuerFileSet to bypass not ready for bypass freeze','File or IssuerFileSet to bypass not ready for bypass freeze');

MERGE INTO SBMErrorWarningType using dual ON ( SBMErrorWarningTypeCd = 'ER-522') when matched then update set SBMErrorWarningTypeNm='All files in file set not received yet to process bypass freeze' ,SBMErrorWarningTypeDesc = 'All files in file set not received yet to process bypass freeze.'  When not matched then insert(SBMErrorWarningTypeCd,SBMErrorWarningTypeNm ,  SBMErrorWarningTypeDesc) values ('ER-522','All files in file set not received yet to process bypass freeze.','All files in file set not received yet to process bypass freeze.');

MERGE INTO SBMErrorWarningType using dual ON ( SBMErrorWarningTypeCd = 'ER-506') 
when matched then update set SBMErrorWarningTypeNm = SBMErrorWarningTypeNm
delete where SBMErrorWarningTypeCd = 'ER-506'; 

commit;

spool off
