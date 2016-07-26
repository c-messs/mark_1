set pages 0
set lines 160
set feed off
set echo off
set termout off
set head off
set verify off

spool &2._ownerselectgrants_&1._sqlgensql.sql
select 'set pages 55 echo on termout on verify on feed on termout on head on' from dual union all
select 'spool &2._ownerselectgrants_&1._sqlgensql.log' from dual union all
select 'grant select on '||owner||'.'||object_name||' to &2;' from all_objects where owner = UPPER('&1') and
	object_type in ('TABLE') union all
select 'spool off' from dual;
spool off
set pages 0
set lines 160
set feed on
set echo on
set termout on
set head on
set verify on
@&2._ownerselectgrants_&1._sqlgensql.sql