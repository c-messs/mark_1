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

update POLICYVERSION set SOURCEVERSIONDATETIME = SOURCEVERSIONDATETIME - interval '1' second , LASTMODIFIEDDATETIME=sysdate, LASTMODIFIEDBY='ERL Reprocess for Prorated amounts' 
WHERE SOURCEVERSIONDATETIME > TO_TIMESTAMP('MM/DD/YYYY 00:00:00', 'mm/dd/yyyy hh24:mi:ss');

commit;

update POLICYSTATUS set TRANSDATETIME = TRANSDATETIME - interval '1' second , LASTMODIFIEDDATETIME=sysdate, LASTMODIFIEDBY='ERL Reprocess for Prorated amounts' 
WHERE TRANSDATETIME > TO_TIMESTAMP('MM/DD/YYYY 00:00:00', 'mm/dd/yyyy hh24:mi:ss');

commit;

update POLICYMEMBERSTATUS set TRANSDATETIME = TRANSDATETIME - interval '1' second , LASTMODIFIEDDATETIME=sysdate, LASTMODIFIEDBY='ERL Reprocess for Prorated amounts' 
WHERE TRANSDATETIME > TO_TIMESTAMP('MM/DD/YYYY 00:00:00','mm/dd/yyyy hh24:mi:ss');

commit;

update BATCHTRANSMSG set PROCESSEDTODBSTATUSTYPECD='R', LASTMODIFIEDDATETIME=sysdate, LASTMODIFIEDBY='ERL Reprocess for Prorated amounts' 
where PROCESSEDTODBSTATUSTYPECD='S' and SOURCEVERSIONDATETIME > TO_TIMESTAMP('MM/DD/YYYY 00:00:00', 'mm/dd/yyyy hh24:mi:ss');

commit;

set echo off;
spool off