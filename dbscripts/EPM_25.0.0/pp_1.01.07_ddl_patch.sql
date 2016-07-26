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

-----------------------------------
-- Created 02/29/2016 - Thomas Fidd
-- Per email from Shashidar dated 2/29/2016, create temp table to improve BEM performance.
-- Modified 03/06/2016 - Thomas Fidd
-- Recreate temp table and store XMLTYPE field as Binary rather than CLOB.
-----------------------------------

DROP TABLE StagingBER;

CREATE GLOBAL TEMPORARY TABLE StagingBER
(
	BERXML               XMLType NULL ,
	TransMsgFileInfoId   NUMBER NULL 
)
	ON COMMIT DELETE ROWS
	XMLTYPE COLUMN "BERXML" STORE AS BINARY XML
;

spool off
