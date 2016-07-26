

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

ALTER TABLE BatchRunControl
DROP PRIMARY KEY CASCADE  DROP INDEX;

ALTER TABLE DailyBEMIndexer
	ADD (IngestJobId NUMBER(19,0));

ALTER TABLE DailyBEMIndexer
	ADD (batchRunControlId VARCHAR2(10));

CREATE UNIQUE INDEX XPKBatchRunControl ON BatchRunControl
(batchRunControlId   ASC);

CREATE INDEX XIF2DailyBEMIndexer ON DailyBEMIndexer
(IngestJobId   ASC);

CREATE INDEX XIF3DailyBEMIndexer ON DailyBEMIndexer
(batchRunControlId   ASC);

ALTER TABLE BatchRunControl
ADD CONSTRAINT  XPKBatchRunControl PRIMARY KEY (batchRunControlId);

spool off
