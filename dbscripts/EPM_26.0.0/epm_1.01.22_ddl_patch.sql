

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


ALTER TABLE PolicyMemberLanguageAbility
DROP PRIMARY KEY CASCADE  DROP INDEX;

CREATE UNIQUE INDEX XAK1PolicyMemberLanguageAbilit ON PolicyMemberLanguageAbility
(X12LanguageTypeCd   ASC,policyMemberVersionId   ASC,X12LanguageModeTypeCd   ASC)
	INITRANS 20
	TABLESPACE FM_INDEX_TRANS;

ALTER TABLE PolicyMemberLanguageAbility
	MODIFY createDateTime DEFAULT sysdate;

ALTER TABLE PolicyMemberLanguageAbility
	MODIFY lastModifiedDateTime DEFAULT sysdate;
	
ALTER TABLE PolicyMemberLanguageAbility
ADD CONSTRAINT  XAK1PolicyMemberLanguageAbilit UNIQUE (X12LanguageTypeCd,policyMemberVersionId,X12LanguageModeTypeCd);

-- ALTER TABLE PolicyMemberLanguageAbility RENAME TO PolicyMemberLangua06TG3503000;


ALTER TABLE PolicyVersion MODIFY(TransMsgID NUMBER NULL);


ALTER TABLE policyMemberVersion MODIFY(TransMsgID NUMBER NULL);

ALTER TABLE PolicyVersion
	ADD (SBMTransMsgID NUMBER);

ALTER TABLE policyMemberVersion
	ADD (SBMTransMsgID NUMBER);

	ALTER TABLE StateProrationConfiguration
	ADD (errorThresholdPercent NUMBER(5,2));

ALTER TABLE StateProrationConfiguration
	ADD (CMSApprovalRequiredInd VARCHAR2(1)  DEFAULT 'Y');

ALTER TABLE StateProrationConfiguration
	ADD (SBMInd VARCHAR2(1));
	
spool off
