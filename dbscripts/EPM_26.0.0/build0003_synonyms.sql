/* select 'drop synonym &2..'||object_name||';'from all_objects where owner = '&1' and object_type in ('TABLE','SEQUENCE') union all */
set pages 0
set lines 160
set feed off
set echo off
set termout off
set head off
set verify off
spool &2._synonyms_sqlgensql.sql
select 'set pages 55 echo on termout on verify on feed on termout on head on' from dual union all
select 'spool &2._synonyms_sqlgensql.log' from dual union all
select 'create synonym &2..'||object_name||' for '||OWNER||'.'||object_name||';' from all_objects where owner = upper('&1') and object_type in ('TABLE','SEQUENCE') 
	and (object_name) not in (select object_name from all_objects where owner = upper('&2') and object_type = 'SYNONYM' ) union all
select 'spool off' from dual;
spool off
set pages 0
set lines 160
set feed on
set echo on
set termout on
set head on
set verify on
@&2._synonyms_sqlgensql