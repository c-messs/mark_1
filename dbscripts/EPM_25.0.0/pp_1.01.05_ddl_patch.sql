
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

select count(*) from PolicyMemberVersion;

CREATE TABLE PolicyMemberDate
	TABLESPACE FM_DATA_TRANS
	INITRANS 20
        PARALLEL (DEGREE 4)
AS
SELECT DISTINCT policyMemberVersionId, policyMemberStartDate,
       policyMemberEndDate, createDateTime,
       lastModifiedDateTime, createBy, lastModifiedBy
FROM   PolicyMemberVersion
;

CREATE UNIQUE INDEX XPKPolicyMemberDate ON PolicyMemberDate
(policyMemberVersionId   ASC,policyMemberStartDate   ASC)
	INITRANS 20
	TABLESPACE FM_INDEX_TRANS;

ALTER TABLE PolicyMemberDate
	ADD CONSTRAINT  XPKPolicyMemberDate PRIMARY KEY (policyMemberVersionId,policyMemberStartDate);

ALTER TABLE PolicyMemberDate
	MODIFY createDateTime DEFAULT sysdate;

ALTER TABLE PolicyMemberDate
	MODIFY lastModifiedDateTime DEFAULT sysdate;

CREATE INDEX XIF1PolicyMemberDate ON PolicyMemberDate
(policyMemberVersionId   ASC)
	INITRANS 20
	TABLESPACE FM_INDEX_TRANS;

ALTER TABLE PolicyMemberDate
	ADD (CONSTRAINT R_380 FOREIGN KEY (policyMemberVersionId) REFERENCES policyMemberVersion (policyMemberVersionId));

select count(*) from PolicyMemberDate;

spool off

