

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

CREATE TABLE StagingRAPIssuer
(
	issuerHiosId         VARCHAR2(6) NOT NULL ,
	LoadJobId            NUMBER(19,0) NULL ,
	RAPJobId             NUMBER(19,0) NULL ,
	policyVersionQuantity NUMBER(19,0) NULL 
);

CREATE UNIQUE INDEX XPKStagingRAPIssuer ON StagingRAPIssuer
(issuerHiosId   ASC);

CREATE INDEX XIF1StagingRAPIssuer ON StagingRAPIssuer
(LoadJobId   ASC);

CREATE INDEX XIF2StagingRAPIssuer ON StagingRAPIssuer
(RAPJobId   ASC);

ALTER TABLE StagingRAPIssuer
ADD CONSTRAINT  XPKStagingRAPIssuer PRIMARY KEY (issuerHiosId);

spool off
