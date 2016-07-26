

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


ALTER TABLE PolicyVersion MODIFY(TransDateTime TIMESTAMP );


ALTER TABLE TransMsg MODIFY(TransMsgDateTime TIMESTAMP );


ALTER TABLE TransMsgFileInfo MODIFY(groupTimestampDateTime TIMESTAMP );


ALTER TABLE HourlyBEMIndexer MODIFY(indexDateTime TIMESTAMP );


ALTER TABLE HourlyBEMIndexer MODIFY(fileNmDateTime TIMESTAMP );


ALTER TABLE HourlyBEMIndexer
  DROP COLUMN skipCount CASCADE CONSTRAINTS;


ALTER TABLE DailyBEMIndexer MODIFY(indexDateTime TIMESTAMP );


ALTER TABLE DailyBEMIndexer MODIFY(fileNmDateTime TIMESTAMP );


ALTER TABLE DailyBEMIndexer
  DROP COLUMN skipCount CASCADE CONSTRAINTS;
  
ALTER TABLE BatchTransMsg
	ADD (TransMsgSkipReasonDesc VARCHAR2(4000));

spool off
