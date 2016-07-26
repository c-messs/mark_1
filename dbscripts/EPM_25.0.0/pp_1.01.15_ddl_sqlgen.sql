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
-- Created 03/09/2016 - Thomas Fidd
-- Per email from Shashidar, store XMLTYPE field as Binary rather than CLOB.
-- Step 10, Fix Oracle bug from DBMS_REDEFINITION process.
-----------------------------------
spool off

set echo off
set verify off
set feedback off 

set linesize 256
set pagesize 0

set term off
spool pp_1.01.16_ddl_patch.sql

select 'set pages 55 echo on termout on verify on feed on termout on head on' from dual union all
select 'spool pp_1.01.16_ddl_patch.log' from dual union all
select 'alter session force parallel dml parallel 4;' from dual union all
select 'alter session force parallel query parallel 4;' from dual;

select 'alter table '||table_name||' modify constraint '||constraint_name||' validate;' 
from user_constraints where table_name='TRANSMSG' and validated='NOT VALIDATED'; 

select 'alter table '||table_name||' modify constraint '||constraint_name||' validate;' 
from user_constraints where r_constraint_name in 
(select constraint_name from user_constraints 
 where table_name='TRANSMSG' and constraint_type in ('P','U') ) and validated='NOT VALIDATED'; 

select 'alter table '||table_name||' modify constraint '||constraint_name||' validate;' 
from user_constraints where table_name='TRANSMSGFILEINFO' and validated='NOT VALIDATED'; 

select 'alter table '||table_name||' modify constraint '||constraint_name||' validate;' 
from user_constraints where r_constraint_name in 
(select constraint_name from user_constraints 
where table_name='TRANSMSGFILEINFO' and constraint_type in ('P','U') ) and validated='NOT VALIDATED'; 
select 'spool off' from dual;
spool off
set pages 0
set lines 160
set feed on
set echo on
set termout on
set head on
set verify on
start pp_1.01.16_ddl_patch.sql