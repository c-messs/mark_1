

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


ALTER TABLE SBMTransMsgValidation MODIFY(elementInErrorNm VARCHAR2(255) );

ALTER TABLE PolicyVersion
	ADD (priorPolicyVersionId NUMBER);

ALTER TABLE PolicyVersion
ADD CONSTRAINT R_441 FOREIGN KEY (priorPolicyVersionId) REFERENCES PolicyVersion (policyVersionId);

ALTER TABLE PolicyMemberLanguageAbility MODIFY(X12LanguageModeTypeCd null );

ALTER TABLE PolicyVersion
ADD (  CONSTRAINT TransMsgValidation_197308900 CHECK ( (TransMsgId is null and SBMTransMsgId is not null) or
(TransMsgId is not null and SBMTransMsgId is null) )  ) ;


spool off
