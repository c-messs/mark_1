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

alter session force parallel dml parallel 4;
alter session force parallel query parallel 4;
pause 'Build temp table - hit return ot continue: '
create table POLICYVERSION_T
TABLESPACE FM_DATA_TRANS
PARTITION By LIST (SUBSCRIBERSTATECD) 
(
PARTITION STCDAK  values ('AK') ,
PARTITION STCDAL  values ('AL') ,
PARTITION STCDAR  values ('AR') ,
PARTITION STCDAZ  values ('AZ') ,
PARTITION STCDCA  values ('CA') ,
PARTITION STCDCO  values ('CO') ,
PARTITION STCDCT  values ('CT') ,
PARTITION STCDDC  values ('DC') ,
PARTITION STCDDE  values ('DE') ,
PARTITION STCDFL  values ('FL') ,
PARTITION STCDGA  values ('GA') ,
PARTITION STCDHI  values ('HI') ,
PARTITION STCDIA  values ('IA') ,
PARTITION STCDID  values ('ID') ,
PARTITION STCDIL  values ('IL') ,
PARTITION STCDIN  values ('IN') ,
PARTITION STCDKS  values ('KS') ,
PARTITION STCDKY  values ('KY') ,
PARTITION STCDLA  values ('LA') ,
PARTITION STCDMA  values ('MA') ,
PARTITION STCDMD  values ('MD') ,
PARTITION STCDME  values ('ME') ,
PARTITION STCDMI  values ('MI') ,
PARTITION STCDMN  values ('MN') ,
PARTITION STCDMO  values ('MO') ,
PARTITION STCDMS  values ('MS') ,
PARTITION STCDMT  values ('MT') ,
PARTITION STCDNC  values ('NC') ,
PARTITION STCDND  values ('ND') ,
PARTITION STCDNE  values ('NE') ,
PARTITION STCDNH  values ('NH') ,
PARTITION STCDNJ  values ('NJ') ,
PARTITION STCDNM  values ('NM') ,
PARTITION STCDNV  values ('NV') ,
PARTITION STCDNY  values ('NY') ,
PARTITION STCDOH  values ('OH') ,
PARTITION STCDOK  values ('OK') ,
PARTITION STCDOR  values ('OR') ,
PARTITION STCDPA  values ('PA') ,
PARTITION STCDRI  values ('RI') ,
PARTITION STCDSC  values ('SC') ,
PARTITION STCDSD  values ('SD') ,
PARTITION STCDTN  values ('TN') ,
PARTITION STCDTX  values ('TX') ,
PARTITION STCDUT  values ('UT') ,
PARTITION STCDVA  values ('VA') ,
PARTITION STCDVT  values ('VT') ,
PARTITION STCDWA  values ('WA') ,
PARTITION STCDWI  values ('WI') ,
PARTITION STCDWV  values ('WV') ,
PARTITION STCDWY  values ('WY') ,
PARTITION STCDDEFAULT values (default)) ENABLE ROW MOVEMENT
as select * from POLICYVERSION where 1=2;

pause 'Verify if can redefine - hit return: '
exec dbms_redefinition.can_redef_table('EPS_OWNER','POLICYVERSION',dbms_redefinition.cons_use_pk);

pause 'Start redefinition and copy data - hit return: '
EXEC DBMS_REDEFINITION.START_REDEF_TABLE('EPS_OWNER', 'POLICYVERSION','POLICYVERSION_T',null,dbms_redefinition.cons_use_pk,null,null)

pause 'Sync data from snapshot
exec DBMS_REDEFINITION.SYNC_INTERIM_TABLE(uname=>user, orig_table => 'POLICYVERSION',int_table=>'POLICYVERSION_T')
pause 'Check log status of snapshot log - hit return: '
select log_table from user_snapshot_logs where master = 'POLICYVERSION';

pause 'change table definition now - hit return: '
DECLARE
num_errors PLS_INTEGER;
BEGIN
DBMS_REDEFINITION.COPY_TABLE_DEPENDENTS(
    uname               => user,
    orig_table          => 'POLICYVERSION',
    int_table           => 'POLICYVERSION_T',
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
pause 'Check table status and redefinition errors  DO NOT hit return until errors are fixed: '
Select table_name, partitioned from user_tables where table_name in ('POLICYVERSION','POLICYVERSION_T');
select * from dba_redefinition_errors;

pause 'finish redefinition - hit return: '
BEGIN
DBMS_REDEFINITION.FINISH_REDEF_TABLE(user, 'POLICYVERSION', 'POLICYVERSION_T');
END;
/
pause ' Final status check - next run sqldeveloper compare between eps_owner and eps_owner_part - hit return:'
Select table_name, partitioned from user_tables where table_name in ('POLICYVERSION','POLICYVERSION_T');

spool off