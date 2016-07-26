set SERVEROUTPUT ON FORMAT WRAPPED
set APPINFO ON
SET VERIFY OFF

SET FEEDBACK OFF
SET TERMOUT OFF

column date_column new_value today_var
column scriptname new_value thescriptname;
column sessionname new_value sessionname;
column dbname new_value dbname;
SELECT Substr(Sys_context('USERENV', 'MODULE'),
              Instr(Sys_context('USERENV', 'MODULE'), ' ')
                                                + 1)
       || '_'
       || To_char(SYSDATE, 'yyyymmdd')        date_column,
       Sys_context('USERENV', 'MODULE')       scriptname,
       Sys_context('USERENV', 'SESSION_USER') sessionname,
       Sys_context('USERENV', 'DB_NAME')      dbname
FROM   dual;

SPOOL &today_var..log

SET VERIFY ON
SET FEEDBACK ON
SET TERMOUT On

INSERT INTO appdba_util.scriptinventory
            (scriptname,
             createdate,
             schemaname,
             DATABASE,
             rollbackind,
             failureind,
             localglobalind)
VALUES      ('&thescriptname',
             SYSDATE,
             '&sessionname',
             '&dbname',
             'N',
             'N',
             'G');
commit;

-- Main Script Body

set echo off
set head off
set lines 120
set trimspool on
set feedback off
set termout off
spool enable.sql
prompt SPOOL &today_var..log

SELECT
        'alter table ' || table_name || ' enable validate constraint ' || constraint_name || ';'
FROM   user_constraints
WHERE
        constraint_type = 'R'
/

spool off
prompt SPOOL off
set echo on
set termout on
set feedback on
start enable
--
-- End Script
set echo on;

spool off
