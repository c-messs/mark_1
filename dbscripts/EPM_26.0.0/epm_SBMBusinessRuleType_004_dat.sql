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

MERGE INTO SBMBusinessRuleType using dual ON ( SBMBusinessRuleTypeCd = 'R067') 
when matched then update set SBMBusinessRuleTypeNm=
'Optional Rating Area expected format value nullification warning'
,SBMBusinessRuleTypeDesc = 
'Optional Rating Area expected format value nullification warning'  
When not matched then insert(SBMBusinessRuleTypeCd,SBMBusinessRuleTypeNm ,  SBMBusinessRuleTypeDesc) values 
('R067','Optional Rating Area expected format value nullification warning',
'Optional Rating Area expected format value nullification warning');

MERGE INTO SBMBusinessRuleType using dual ON ( SBMBusinessRuleTypeCd = 'R068') 
when matched then update set SBMBusinessRuleTypeNm=
'QHPID validation of metal level plan for CSR Variant Id 02-03'
,SBMBusinessRuleTypeDesc = 
'QHPID validation of metal level plan for CSR Variant Id 02-03'  
When not matched then insert(SBMBusinessRuleTypeCd,SBMBusinessRuleTypeNm ,  SBMBusinessRuleTypeDesc) values 
('R068','QHPID validation of metal level plan for CSR Variant Id 02-03',
'QHPID validation of metal level plan for CSR Variant Id 02-03');

MERGE INTO SBMBusinessRuleType using dual ON ( SBMBusinessRuleTypeCd = 'R069') 
when matched then update set SBMBusinessRuleTypeNm=
'Compare member start date with policy end date'
,SBMBusinessRuleTypeDesc = 
'Compare member start date with policy end date'  
When not matched then insert(SBMBusinessRuleTypeCd,SBMBusinessRuleTypeNm ,  SBMBusinessRuleTypeDesc) values 
('R069','Compare member start date with policy end date',
'Compare member start date with policy end date');

commit;

spool off
