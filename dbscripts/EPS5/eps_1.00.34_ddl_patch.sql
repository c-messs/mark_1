

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

CREATE TABLE OpenEnrollmentWindow
(
	stateCd              VARCHAR2(50) NOT NULL ,
	programYear          VARCHAR2(4) NOT NULL ,
	openEnrollmentPeriodStartDate DATE NOT NULL ,
	openEnrollmentPeriodEndDate DATE NULL ,
	createDateTime       TIMESTAMP DEFAULT  systimestamp  NULL ,
	lastModifiedDateTime TIMESTAMP DEFAULT  systimestamp  NULL ,
	createBy             VARCHAR2(50) NULL ,
	lastModifiedBy       VARCHAR2(50) NULL 
)
	TABLESPACE FM_DATA_REF
	INITRANS 20;

CREATE UNIQUE INDEX XPKOpenEnrollmentWindow ON OpenEnrollmentWindow
(stateCd   ASC,programYear   ASC)
	INITRANS 20
	TABLESPACE FM_INDEX_REF;

ALTER TABLE OpenEnrollmentWindow
	ADD CONSTRAINT  XPKOpenEnrollmentWindow PRIMARY KEY (stateCd,programYear);


spool off
