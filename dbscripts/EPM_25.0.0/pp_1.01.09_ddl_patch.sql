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
-- Created 03/07/2016 - Thomas Fidd
-- Per email from Shashidar, store XMLTYPE field as Binary rather than CLOB.
-- Redefinition of TransMsgFileInfo
-- Step 2, Enable parallel.
-- Step 3, Verify if can redefine.
-- Step 4, Start redefinition and copy data.
-- Step 5, Copy dependent objects.
-- Step 6, Check for errors.
-- Step 7, Synchronized interim table.
-- Step 8, Complete the redefinition.
-----------------------------------
-- Step 2
alter session force parallel dml parallel 4;
alter session force parallel query parallel 4;

-- Step 3
-- pause 'Verify if can redefine - hit return: '
exec dbms_redefinition.can_redef_table('&1','TRANSMSGFILEINFO',dbms_redefinition.cons_use_pk);

-- Step 4
-- pause 'Start redefinition and copy data - hit return: '
exec DBMS_REDEFINITION.start_redef_table('&1', 'TRANSMSGFILEINFO', 'TRANSMSGFILEINFOTMP',null,dbms_redefinition.cons_use_pk,null,null);

-- Step 5
-- pause 'change table definition now - hit return: '
DECLARE
num_errors PLS_INTEGER;
BEGIN
DBMS_REDEFINITION.COPY_TABLE_DEPENDENTS(
    uname               => '&1',
    orig_table          => 'TRANSMSGFILEINFO',
    int_table           => 'TRANSMSGFILEINFOTMP',
    copy_indexes        => DBMS_REDEFINITION.CONS_ORIG_PARAMS,
    copy_triggers       => TRUE,
    copy_constraints    => TRUE,
    copy_privileges     => TRUE,
    ignore_errors       => TRUE,
    num_errors          => num_errors,
    COPY_STATISTICS	=> TRUE);
dbms_output.put_line('Number of errors is : '|| num_errors);
END;
/

-- Step 6, check for errors.
select object_name, base_table_name, ddl_txt from DBA_REDEFINITION_ERRORS;
pause 'Check table status and redefinition errors  DO NOT hit return until errors are resolved: '

-- Step 7, Synchronize tables.
exec DBMS_REDEFINITION.SYNC_INTERIM_TABLE('&1', 'TRANSMSGFILEINFO', 'TRANSMSGFILEINFOTMP');
 
-- Step 8, Finish redefinition.
exec DBMS_REDEFINITION.finish_redef_table('&1', 'TRANSMSGFILEINFO', 'TRANSMSGFILEINFOTMP');

spool off
