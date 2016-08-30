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

MERGE INTO SBMBusinessRuleType using dual ON ( SBMBusinessRuleTypeCd = 'R033') 
when matched then update set SBMBusinessRuleTypeNm=
'QHPID Plan Reference data'
,SBMBusinessRuleTypeDesc = 
'QHPID Plan Reference data'  
When not matched then insert(SBMBusinessRuleTypeCd,SBMBusinessRuleTypeNm ,  SBMBusinessRuleTypeDesc) values 
('R033','QHPID Plan Reference data',
'QHPID Plan Reference data');

MERGE INTO SBMBusinessRuleType using dual ON ( SBMBusinessRuleTypeCd = 'R034') 
when matched then update set SBMBusinessRuleTypeNm=
'QHPID validity with SBM'
,SBMBusinessRuleTypeDesc = 
'QHPID validity with SBM'  
When not matched then insert(SBMBusinessRuleTypeCd,SBMBusinessRuleTypeNm ,  SBMBusinessRuleTypeDesc) values 
('R034','QHPID validity with SBM',
'QHPID validity with SBM');

MERGE INTO SBMBusinessRuleType using dual ON ( SBMBusinessRuleTypeCd = 'R035') 
when matched then update set SBMBusinessRuleTypeNm=
'QHPID check on XPR'
,SBMBusinessRuleTypeDesc = 
'QHPID check on XPR'  
When not matched then insert(SBMBusinessRuleTypeCd,SBMBusinessRuleTypeNm ,  SBMBusinessRuleTypeDesc) values 
('R035','QHPID check on XPR',
'QHPID check on XPR');

MERGE INTO SBMBusinessRuleType using dual ON ( SBMBusinessRuleTypeCd = 'R036') 
when matched then update set SBMBusinessRuleTypeNm=
'QHPID validity check with Issuer ID'
,SBMBusinessRuleTypeDesc = 
'QHPID validity check with Issuer ID'  
When not matched then insert(SBMBusinessRuleTypeCd,SBMBusinessRuleTypeNm ,  SBMBusinessRuleTypeDesc) values 
('R036','QHPID validity check with Issuer ID',
'QHPID validity check with Issuer ID');

MERGE INTO SBMBusinessRuleType using dual ON ( SBMBusinessRuleTypeCd = 'R037') 
when matched then update set SBMBusinessRuleTypeNm=
'Optional Policy element check'
,SBMBusinessRuleTypeDesc = 
'Optional Policy element check'  
When not matched then insert(SBMBusinessRuleTypeCd,SBMBusinessRuleTypeNm ,  SBMBusinessRuleTypeDesc) values 
('R037','Optional Policy element check',
'Optional Policy element check');

MERGE INTO SBMBusinessRuleType using dual ON ( SBMBusinessRuleTypeCd = 'R038') 
when matched then update set SBMBusinessRuleTypeNm=
'Issuer Assigned Policy ID database character length validation'
,SBMBusinessRuleTypeDesc = 
'Issuer Assigned Policy ID database character length validation'  
When not matched then insert(SBMBusinessRuleTypeCd,SBMBusinessRuleTypeNm ,  SBMBusinessRuleTypeDesc) values 
('R038','Issuer Assigned Policy ID database character length validation',
'Issuer Assigned Policy ID database character length validation');

MERGE INTO SBMBusinessRuleType using dual ON ( SBMBusinessRuleTypeCd = 'R039') 
when matched then update set SBMBusinessRuleTypeNm=
'Issuer Assigned Subscriber ID database character length truncation'
,SBMBusinessRuleTypeDesc = 
'Issuer Assigned Subscriber ID database character length truncation'  
When not matched then insert(SBMBusinessRuleTypeCd,SBMBusinessRuleTypeNm ,  SBMBusinessRuleTypeDesc) values 
('R039','Issuer Assigned Subscriber ID database character length truncation',
'Issuer Assigned Subscriber ID database character length truncation');

MERGE INTO SBMBusinessRuleType using dual ON ( SBMBusinessRuleTypeCd = 'R040') 
when matched then update set SBMBusinessRuleTypeNm=
'Policy Start Date to Coverage Year validation'
,SBMBusinessRuleTypeDesc = 
'Policy Start Date to Coverage Year validation'  
When not matched then insert(SBMBusinessRuleTypeCd,SBMBusinessRuleTypeNm ,  SBMBusinessRuleTypeDesc) values 
('R040','Policy Start Date to Coverage Year validation',
'Policy Start Date to Coverage Year validation');

MERGE INTO SBMBusinessRuleType using dual ON ( SBMBusinessRuleTypeCd = 'R041') 
when matched then update set SBMBusinessRuleTypeNm=
'XPR Effectuation Indicator check'
,SBMBusinessRuleTypeDesc = 
'XPR Effectuation Indicator check'  
When not matched then insert(SBMBusinessRuleTypeCd,SBMBusinessRuleTypeNm ,  SBMBusinessRuleTypeDesc) values 
('R041','XPR Effectuation Indicator check',
'XPR Effectuation Indicator check');

MERGE INTO SBMBusinessRuleType using dual ON ( SBMBusinessRuleTypeCd = 'R042') 
when matched then update set SBMBusinessRuleTypeNm=
'Policy End Date to Policy Start Date validation when Effectuation Indicator is "N"'
,SBMBusinessRuleTypeDesc = 
'Policy End Date to Policy Start Date validation when Effectuation Indicator is "N"'  
When not matched then insert(SBMBusinessRuleTypeCd,SBMBusinessRuleTypeNm ,  SBMBusinessRuleTypeDesc) values 
('R042','Policy End Date to Policy Start Date validation when Effectuation Indicator is "N"',
'Policy End Date to Policy Start Date validation when Effectuation Indicator is "N"');

MERGE INTO SBMBusinessRuleType using dual ON ( SBMBusinessRuleTypeCd = 'R043') 
when matched then update set SBMBusinessRuleTypeNm=
'Policy End Date to Policy Start Date validation when Effectuation indicator is "Y"'
,SBMBusinessRuleTypeDesc = 
'Policy End Date to Policy Start Date validation when Effectuation indicator is "Y"'  
When not matched then insert(SBMBusinessRuleTypeCd,SBMBusinessRuleTypeNm ,  SBMBusinessRuleTypeDesc) values 
('R043','Policy End Date to Policy Start Date validation when Effectuation indicator is "Y"',
'Policy End Date to Policy Start Date validation when Effectuation indicator is "Y"');

MERGE INTO SBMBusinessRuleType using dual ON ( SBMBusinessRuleTypeCd = 'R044') 
when matched then update set SBMBusinessRuleTypeNm=
'No Subscriber Indicator validation'
,SBMBusinessRuleTypeDesc = 
'No Subscriber Indicator validation'  
When not matched then insert(SBMBusinessRuleTypeCd,SBMBusinessRuleTypeNm ,  SBMBusinessRuleTypeDesc) values 
('R044','No Subscriber Indicator validation',
'No Subscriber Indicator validation');

MERGE INTO SBMBusinessRuleType using dual ON ( SBMBusinessRuleTypeCd = 'R045') 
when matched then update set SBMBusinessRuleTypeNm=
'Multiple Subscriber Indicator validation'
,SBMBusinessRuleTypeDesc = 
'Multiple Subscriber Indicator validation'  
When not matched then insert(SBMBusinessRuleTypeCd,SBMBusinessRuleTypeNm ,  SBMBusinessRuleTypeDesc) values 
('R045','Multiple Subscriber Indicator validation',
'Multiple Subscriber Indicator validation');

MERGE INTO SBMBusinessRuleType using dual ON ( SBMBusinessRuleTypeCd = 'R046') 
when matched then update set SBMBusinessRuleTypeNm=
'Optional Member Information check'
,SBMBusinessRuleTypeDesc = 
'Optional Member Information check'  
When not matched then insert(SBMBusinessRuleTypeCd,SBMBusinessRuleTypeNm ,  SBMBusinessRuleTypeDesc) values 
('R046','Optional Member Information check',
'Optional Member Information check');

MERGE INTO SBMBusinessRuleType using dual ON ( SBMBusinessRuleTypeCd = 'R047') 
when matched then update set SBMBusinessRuleTypeNm=
'Optional Member Information element database character length validation'
,SBMBusinessRuleTypeDesc = 
'Optional Member Information element database character length validation'  
When not matched then insert(SBMBusinessRuleTypeCd,SBMBusinessRuleTypeNm ,  SBMBusinessRuleTypeDesc) values 
('R047','Optional Member Information element database character length validation',
'Optional Member Information element database character length validation');

MERGE INTO SBMBusinessRuleType using dual ON ( SBMBusinessRuleTypeCd = 'R048') 
when matched then update set SBMBusinessRuleTypeNm=
'Issuer Assigned Member Id database character length truncation warning'
,SBMBusinessRuleTypeDesc = 
'Issuer Assigned Member Id database character length truncation warning'  
When not matched then insert(SBMBusinessRuleTypeCd,SBMBusinessRuleTypeNm ,  SBMBusinessRuleTypeDesc) values 
('R048','Issuer Assigned Member Id database character length truncation warning',
'Issuer Assigned Member Id database character length truncation warning');

MERGE INTO SBMBusinessRuleType using dual ON ( SBMBusinessRuleTypeCd = 'R049') 
when matched then update set SBMBusinessRuleTypeNm=
'Name Prefix database character length truncation warning'
,SBMBusinessRuleTypeDesc = 
'Name Prefix database character length truncation warning'  
When not matched then insert(SBMBusinessRuleTypeCd,SBMBusinessRuleTypeNm ,  SBMBusinessRuleTypeDesc) values 
('R049','Name Prefix database character length truncation warning',
'Name Prefix database character length truncation warning');

MERGE INTO SBMBusinessRuleType using dual ON ( SBMBusinessRuleTypeCd = 'R050') 
when matched then update set SBMBusinessRuleTypeNm=
'Member Middle Name database character length truncation warning'
,SBMBusinessRuleTypeDesc = 
'Member Middle Name database character length truncation warning'  
When not matched then insert(SBMBusinessRuleTypeCd,SBMBusinessRuleTypeNm ,  SBMBusinessRuleTypeDesc) values 
('R050','Member Middle Name database character length truncation warning',
'Member Middle Name database character length truncation warning');

MERGE INTO SBMBusinessRuleType using dual ON ( SBMBusinessRuleTypeCd = 'R051') 
when matched then update set SBMBusinessRuleTypeNm=
'Name Suffix database character length truncation warning'
,SBMBusinessRuleTypeDesc = 
'Name Suffix database character length truncation warning'  
When not matched then insert(SBMBusinessRuleTypeCd,SBMBusinessRuleTypeNm ,  SBMBusinessRuleTypeDesc) values 
('R051','Name Suffix database character length truncation warning',
'Name Suffix database character length truncation warning');

MERGE INTO SBMBusinessRuleType using dual ON ( SBMBusinessRuleTypeCd = 'R052') 
when matched then update set SBMBusinessRuleTypeNm=
'Optional Member Information element expected database value validation'
,SBMBusinessRuleTypeDesc = 
'Optional Member Information element expected database value validation'  
When not matched then insert(SBMBusinessRuleTypeCd,SBMBusinessRuleTypeNm ,  SBMBusinessRuleTypeDesc) values 
('R052','Optional Member Information element expected database value validation',
'Optional Member Information element expected database value validation');

MERGE INTO SBMBusinessRuleType using dual ON ( SBMBusinessRuleTypeCd = 'R053') 
when matched then update set SBMBusinessRuleTypeNm=
'Social Security Number value nullification warning'
,SBMBusinessRuleTypeDesc = 
'Social Security Number value nullification warning'  
When not matched then insert(SBMBusinessRuleTypeCd,SBMBusinessRuleTypeNm ,  SBMBusinessRuleTypeDesc) values 
('R053','Social Security Number value nullification warning',
'Social Security Number value nullification warning');

MERGE INTO SBMBusinessRuleType using dual ON ( SBMBusinessRuleTypeCd = 'R054') 
when matched then update set SBMBusinessRuleTypeNm=
'Language Qualifier Code value nullification warning'
,SBMBusinessRuleTypeDesc = 
'Language Qualifier Code value nullification warning'  
When not matched then insert(SBMBusinessRuleTypeCd,SBMBusinessRuleTypeNm ,  SBMBusinessRuleTypeDesc) values 
('R054','Language Qualifier Code value nullification warning',
'Language Qualifier Code value nullification warning');

MERGE INTO SBMBusinessRuleType using dual ON ( SBMBusinessRuleTypeCd = 'R055') 
when matched then update set SBMBusinessRuleTypeNm=
'Language Code value nullification warning'
,SBMBusinessRuleTypeDesc = 
'Language Code value nullification warning'  
When not matched then insert(SBMBusinessRuleTypeCd,SBMBusinessRuleTypeNm ,  SBMBusinessRuleTypeDesc) values 
('R055','Language Code value nullification warning',
'Language Code value nullification warning');

MERGE INTO SBMBusinessRuleType using dual ON ( SBMBusinessRuleTypeCd = 'R056') 
when matched then update set SBMBusinessRuleTypeNm=
'Gender Code value nullification warning'
,SBMBusinessRuleTypeDesc = 
'Gender Code value nullification warning'  
When not matched then insert(SBMBusinessRuleTypeCd,SBMBusinessRuleTypeNm ,  SBMBusinessRuleTypeDesc) values 
('R056','Gender Code value nullification warning',
'Gender Code value nullification warning');

MERGE INTO SBMBusinessRuleType using dual ON ( SBMBusinessRuleTypeCd = 'R057') 
when matched then update set SBMBusinessRuleTypeNm=
'Race Ethnicity Code value nullification warning'
,SBMBusinessRuleTypeDesc = 
'Race Ethnicity Code value nullification warning'  
When not matched then insert(SBMBusinessRuleTypeCd,SBMBusinessRuleTypeNm ,  SBMBusinessRuleTypeDesc) values 
('R057','Race Ethnicity Code value nullification warning',
'Race Ethnicity Code value nullification warning');

MERGE INTO SBMBusinessRuleType using dual ON ( SBMBusinessRuleTypeCd = 'R058') 
when matched then update set SBMBusinessRuleTypeNm=
'Tobacco Use Code value nullification warning'
,SBMBusinessRuleTypeDesc = 
'Tobacco Use Code value nullification warning'  
When not matched then insert(SBMBusinessRuleTypeCd,SBMBusinessRuleTypeNm ,  SBMBusinessRuleTypeDesc) values 
('R058','Tobacco Use Code value nullification warning',
'Tobacco Use Code value nullification warning');

MERGE INTO SBMBusinessRuleType using dual ON ( SBMBusinessRuleTypeCd = 'R059') 
when matched then update set SBMBusinessRuleTypeNm=
'Non Covered Subscriber Ind. value nullification warning'
,SBMBusinessRuleTypeDesc = 
'Non Covered Subscriber Ind. value nullification warning'  
When not matched then insert(SBMBusinessRuleTypeCd,SBMBusinessRuleTypeNm ,  SBMBusinessRuleTypeDesc) values 
('R059','Non Covered Subscriber Ind. value nullification warning',
'Non Covered Subscriber Ind. value nullification warning');

MERGE INTO SBMBusinessRuleType using dual ON ( SBMBusinessRuleTypeCd = 'R060') 
when matched then update set SBMBusinessRuleTypeNm=
'Missing Member Date for Subscriber Indicator equal to "Y" error'
,SBMBusinessRuleTypeDesc = 
'Missing Member Date for Subscriber Indicator equal to "Y" error'  
When not matched then insert(SBMBusinessRuleTypeCd,SBMBusinessRuleTypeNm ,  SBMBusinessRuleTypeDesc) values 
('R060','Missing Member Date for Subscriber Indicator equal to "Y" error',
'Missing Member Date for Subscriber Indicator equal to "Y" error');

MERGE INTO SBMBusinessRuleType using dual ON ( SBMBusinessRuleTypeCd = 'R061') 
when matched then update set SBMBusinessRuleTypeNm=
'Missing Member Date for Subscriber Indicator equal to "Y" and missing Non Covered Subscriber Ind. error'
,SBMBusinessRuleTypeDesc = 
'Missing Member Date for Subscriber Indicator equal to "Y" and missing Non Covered Subscriber Ind. error'  
When not matched then insert(SBMBusinessRuleTypeCd,SBMBusinessRuleTypeNm ,  SBMBusinessRuleTypeDesc) values 
('R061','Missing Member Date for Subscriber Indicator equal to "Y" and missing Non Covered Subscriber Ind. error',
'Missing Member Date for Subscriber Indicator equal to "Y" and missing Non Covered Subscriber Ind. error');

MERGE INTO SBMBusinessRuleType using dual ON ( SBMBusinessRuleTypeCd = 'R062') 
when matched then update set SBMBusinessRuleTypeNm=
'Member Start Date to Policy Start Date validation'
,SBMBusinessRuleTypeDesc = 
'Member Start Date to Policy Start Date validation'  
When not matched then insert(SBMBusinessRuleTypeCd,SBMBusinessRuleTypeNm ,  SBMBusinessRuleTypeDesc) values 
('R062','Member Start Date to Policy Start Date validation',
'Member Start Date to Policy Start Date validation');

MERGE INTO SBMBusinessRuleType using dual ON ( SBMBusinessRuleTypeCd = 'R063') 
when matched then update set SBMBusinessRuleTypeNm=
'Member End Date to Policy End Date validation'
,SBMBusinessRuleTypeDesc = 
'Member End Date to Policy End Date validation'  
When not matched then insert(SBMBusinessRuleTypeCd,SBMBusinessRuleTypeNm ,  SBMBusinessRuleTypeDesc) values 
('R063','Member End Date to Policy End Date validation',
'Member End Date to Policy End Date validation');

MERGE INTO SBMBusinessRuleType using dual ON ( SBMBusinessRuleTypeCd = 'R064') 
when matched then update set SBMBusinessRuleTypeNm=
'Multiple member start date check'
,SBMBusinessRuleTypeDesc = 
'Multiple member start date check'  
When not matched then insert(SBMBusinessRuleTypeCd,SBMBusinessRuleTypeNm ,  SBMBusinessRuleTypeDesc) values 
('R064','Multiple member start date check',
'Multiple member start date check');

spool off
