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

MERGE INTO X12LanguageQualifierType using dual ON ( X12LanguageQualifierTypeCd = 'LD') when matched then update set X12LanguageQualifierTypeNm='NISO Z39.53 Language Code' ,X12LanguageQualifierTypeDesc = 'NISO Z39.53 Language Code'  When not matched then insert(X12LanguageQualifierTypeCd,X12LanguageQualifierTypeNm ,  X12LanguageQualifierTypeDesc) values ('LD','NISO Z39.53 Language Code','NISO Z39.53 Language Code');

MERGE INTO X12LanguageQualifierType using dual ON ( X12LanguageQualifierTypeCd = 'LE') when matched then update set X12LanguageQualifierTypeNm='ISO 639 Language Code' ,X12LanguageQualifierTypeDesc = 'ISO 639 Language Code'  When not matched then insert(X12LanguageQualifierTypeCd,X12LanguageQualifierTypeNm ,  X12LanguageQualifierTypeDesc) values ('LE','ISO 639 Language Code','ISO 639 Language Code');

MERGE INTO X12LanguageQualifierType using dual ON ( X12LanguageQualifierTypeCd = 'IN') when matched then update set X12LanguageQualifierTypeNm='Internal FM Code' ,X12LanguageQualifierTypeDesc = 'Internal FM Code'  When not matched then insert(X12LanguageQualifierTypeCd,X12LanguageQualifierTypeNm ,  X12LanguageQualifierTypeDesc) values ('IN','Internal FM Code','Internal FM Code');

commit;

spool off
