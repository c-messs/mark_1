

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

SELECT count(*) FROM X12RaceEthnicityType;
MERGE INTO X12RaceEthnicityType using dual ON ( X12RaceEthnicityTypeCd = '7') when matched then update set X12RaceEthnicityTypeNm='Not Provided' ,X12RaceEthnicityTypeDesc = 'Not Provided'  When not matched then insert(X12RaceEthnicityTypeCd, X12RaceEthnicityTypeNm, X12RaceEthnicityTypeDesc, industryStandardTypeNm) values ('7','Not Provided','Not Provided','X12');
MERGE INTO X12RaceEthnicityType using dual ON ( X12RaceEthnicityTypeCd = '8') when matched then update set X12RaceEthnicityTypeNm='Not Applicable' ,X12RaceEthnicityTypeDesc = 'Not Applicable'  When not matched then insert(X12RaceEthnicityTypeCd, X12RaceEthnicityTypeNm, X12RaceEthnicityTypeDesc, industryStandardTypeNm) values ('8','Not Applicable','Not Applicable','X12');
MERGE INTO X12RaceEthnicityType using dual ON ( X12RaceEthnicityTypeCd = 'A') when matched then update set X12RaceEthnicityTypeNm='Asian or Pacific Islander' ,X12RaceEthnicityTypeDesc = 'Asian or Pacific Islander'  When not matched then insert(X12RaceEthnicityTypeCd, X12RaceEthnicityTypeNm, X12RaceEthnicityTypeDesc, industryStandardTypeNm) values ('A','Asian or Pacific Islander','Asian or Pacific Islander','X12');
MERGE INTO X12RaceEthnicityType using dual ON ( X12RaceEthnicityTypeCd = 'B') when matched then update set X12RaceEthnicityTypeNm='Black' ,X12RaceEthnicityTypeDesc = 'Black'  When not matched then insert(X12RaceEthnicityTypeCd, X12RaceEthnicityTypeNm, X12RaceEthnicityTypeDesc, industryStandardTypeNm) values ('B','Black','Black','X12');
MERGE INTO X12RaceEthnicityType using dual ON ( X12RaceEthnicityTypeCd = 'C') when matched then update set X12RaceEthnicityTypeNm='Caucasian' ,X12RaceEthnicityTypeDesc = 'Caucasian'  When not matched then insert(X12RaceEthnicityTypeCd, X12RaceEthnicityTypeNm, X12RaceEthnicityTypeDesc, industryStandardTypeNm) values ('C','Caucasian','Caucasian','X12');
MERGE INTO X12RaceEthnicityType using dual ON ( X12RaceEthnicityTypeCd = 'D') when matched then update set X12RaceEthnicityTypeNm='Subcontinent Asian American' ,X12RaceEthnicityTypeDesc = 'Subcontinent Asian American'  When not matched then insert(X12RaceEthnicityTypeCd, X12RaceEthnicityTypeNm, X12RaceEthnicityTypeDesc, industryStandardTypeNm) values ('D','Subcontinent Asian American','Subcontinent Asian American','X12');
MERGE INTO X12RaceEthnicityType using dual ON ( X12RaceEthnicityTypeCd = 'E') when matched then update set X12RaceEthnicityTypeNm='Other Race or Ethnicity' ,X12RaceEthnicityTypeDesc = 'Other Race or Ethnicity'  When not matched then insert(X12RaceEthnicityTypeCd, X12RaceEthnicityTypeNm, X12RaceEthnicityTypeDesc, industryStandardTypeNm) values ('E','Other Race or Ethnicity','Other Race or Ethnicity','X12');
MERGE INTO X12RaceEthnicityType using dual ON ( X12RaceEthnicityTypeCd = 'F') when matched then update set X12RaceEthnicityTypeNm='Asian Pacific American' ,X12RaceEthnicityTypeDesc = 'Asian Pacific American'  When not matched then insert(X12RaceEthnicityTypeCd, X12RaceEthnicityTypeNm, X12RaceEthnicityTypeDesc, industryStandardTypeNm) values ('F','Asian Pacific American','Asian Pacific American','X12');
MERGE INTO X12RaceEthnicityType using dual ON ( X12RaceEthnicityTypeCd = 'G') when matched then update set X12RaceEthnicityTypeNm='Native American' ,X12RaceEthnicityTypeDesc = 'Native American'  When not matched then insert(X12RaceEthnicityTypeCd, X12RaceEthnicityTypeNm, X12RaceEthnicityTypeDesc, industryStandardTypeNm) values ('G','Native American','Native American','X12');
MERGE INTO X12RaceEthnicityType using dual ON ( X12RaceEthnicityTypeCd = 'H') when matched then update set X12RaceEthnicityTypeNm='Hispanic' ,X12RaceEthnicityTypeDesc = 'Hispanic'  When not matched then insert(X12RaceEthnicityTypeCd, X12RaceEthnicityTypeNm, X12RaceEthnicityTypeDesc, industryStandardTypeNm) values ('H','Hispanic','Hispanic','X12');
MERGE INTO X12RaceEthnicityType using dual ON ( X12RaceEthnicityTypeCd = 'I') when matched then update set X12RaceEthnicityTypeNm='American Indian or Alaskan Native' ,X12RaceEthnicityTypeDesc = 'American Indian or Alaskan Native'  When not matched then insert(X12RaceEthnicityTypeCd, X12RaceEthnicityTypeNm, X12RaceEthnicityTypeDesc, industryStandardTypeNm) values ('I','American Indian or Alaskan Native','American Indian or Alaskan Native','X12');
MERGE INTO X12RaceEthnicityType using dual ON ( X12RaceEthnicityTypeCd = 'J') when matched then update set X12RaceEthnicityTypeNm='Native Hawaiian' ,X12RaceEthnicityTypeDesc = 'Native Hawaiian'  When not matched then insert(X12RaceEthnicityTypeCd, X12RaceEthnicityTypeNm, X12RaceEthnicityTypeDesc, industryStandardTypeNm) values ('J','Native Hawaiian','Native Hawaiian','X12');
MERGE INTO X12RaceEthnicityType using dual ON ( X12RaceEthnicityTypeCd = 'N') when matched then update set X12RaceEthnicityTypeNm='Black (Non-Hispanic)' ,X12RaceEthnicityTypeDesc = 'Black (Non-Hispanic)'  When not matched then insert(X12RaceEthnicityTypeCd, X12RaceEthnicityTypeNm, X12RaceEthnicityTypeDesc, industryStandardTypeNm) values ('N','Black (Non-Hispanic)','Black (Non-Hispanic)','X12');
MERGE INTO X12RaceEthnicityType using dual ON ( X12RaceEthnicityTypeCd = 'O') when matched then update set X12RaceEthnicityTypeNm='White (Non-Hispanic)' ,X12RaceEthnicityTypeDesc = 'White (Non-Hispanic)'  When not matched then insert(X12RaceEthnicityTypeCd, X12RaceEthnicityTypeNm, X12RaceEthnicityTypeDesc, industryStandardTypeNm) values ('O','White (Non-Hispanic)','White (Non-Hispanic)','X12');
MERGE INTO X12RaceEthnicityType using dual ON ( X12RaceEthnicityTypeCd = 'P') when matched then update set X12RaceEthnicityTypeNm='Pacific Islander' ,X12RaceEthnicityTypeDesc = 'Pacific Islander'  When not matched then insert(X12RaceEthnicityTypeCd, X12RaceEthnicityTypeNm, X12RaceEthnicityTypeDesc, industryStandardTypeNm) values ('P','Pacific Islander','Pacific Islander','X12');
MERGE INTO X12RaceEthnicityType using dual ON ( X12RaceEthnicityTypeCd = 'Z') when matched then update set X12RaceEthnicityTypeNm='Mutually Defined' ,X12RaceEthnicityTypeDesc = 'Mutually Defined'  When not matched then insert(X12RaceEthnicityTypeCd, X12RaceEthnicityTypeNm, X12RaceEthnicityTypeDesc, industryStandardTypeNm) values ('Z','Mutually Defined','Mutually Defined','X12');

commit;
SELECT count(*) FROM X12RaceEthnicityType;
spool off
