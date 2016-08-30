

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

ALTER TABLE SBMFileProcessingSummary
RENAME COLUMN approvalThresholdPercent TO errorThresholdPercent;

ALTER TABLE SBMFileInfo
DROP CONSTRAINT R_408;


ALTER TABLE SBMFileInfo
  DROP COLUMN lastSBMFileStatusTypeCd CASCADE CONSTRAINTS;

ALTER TABLE SBMFileInfo
	ADD (rejectedInd VARCHAR2(1)  DEFAULT 'N' NOT NULL);

ALTER TABLE SBMFileProcessingSummary
	ADD (SbmFileStatusTypeCd VARCHAR2(25));

ALTER TABLE stagingPolicyMemberVersion
	ADD (zipPlus4Cd VARCHAR2(15));

ALTER TABLE SBMFileProcessingSummary
ADD CONSTRAINT R_440 FOREIGN KEY (SbmFileStatusTypeCd) REFERENCES SbmFileStatusType (SbmFileStatusTypeCd);

spool off
