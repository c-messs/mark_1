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
create table POLICYPAYMENTTRANS_T
TABLESPACE FM_DATA_TRANS
PARTITION By LIST(ISSUERHIOSID) SUBPARTITION BY RANGE(PAYMENTCOVERAGESTARTDATE)
		SUBPARTITION TEMPLATE 
		(	SUBPARTITION Y2010 VALUES LESS THAN  (TO_DATE('01-JAN-2011','DD-MON-YYYY')),
			SUBPARTITION Y2011 VALUES LESS THAN  (TO_DATE('01-JAN-2012','DD-MON-YYYY')),
			SUBPARTITION Y2012 VALUES LESS THAN  (TO_DATE('01-JAN-2013','DD-MON-YYYY')),
			SUBPARTITION Y2013 VALUES LESS THAN  (TO_DATE('01-JAN-2014','DD-MON-YYYY')),
			SUBPARTITION Y2014 VALUES LESS THAN  (TO_DATE('01-JAN-2015','DD-MON-YYYY')),
			SUBPARTITION Y2015 VALUES LESS THAN  (TO_DATE('01-JAN-2016','DD-MON-YYYY')),
			SUBPARTITION Y2016 VALUES LESS THAN  (TO_DATE('01-JAN-2017','DD-MON-YYYY')),
			SUBPARTITION Y2017 VALUES LESS THAN  (TO_DATE('01-JAN-2018','DD-MON-YYYY')),
			SUBPARTITION Y2018 VALUES LESS THAN  (TO_DATE('01-JAN-2019','DD-MON-YYYY')),
			SUBPARTITION Y2019 VALUES LESS THAN  (TO_DATE('01-JAN-2020','DD-MON-YYYY')),
			SUBPARTITION DEFYEAR VALUES LESS THAN (MAXVALUE)
		)
(PARTITION HIOS15334 VALUES ('15334'),
PARTITION HIOS14346 VALUES ('14346'),
PARTITION HIOS17078 VALUES ('17078'),
PARTITION HIOS12776 VALUES ('12776'),
PARTITION HIOS10417 VALUES ('10417'),
PARTITION HIOS17882 VALUES ('17882'),
PARTITION HIOS13462 VALUES ('13462'),
PARTITION HIOS18115 VALUES ('18115'),
PARTITION HIOS21131 VALUES ('21131'),
PARTITION HIOS20183 VALUES ('20183'),
PARTITION HIOSDEFAULT values (default)
)
ENABLE ROW MOVEMENT
as select * from POLICYPAYMENTTRANS where 1=2;

pause 'Verify if can redefine - hit return: '
exec dbms_redefinition.can_redef_table('EPS_OWNER','POLICYPAYMENTTRANS',dbms_redefinition.cons_use_pk);

pause 'Start redefinition and copy data - hit return: '
EXEC DBMS_REDEFINITION.START_REDEF_TABLE('EPS_OWNER', 'POLICYPAYMENTTRANS' , 'POLICYPAYMENTTRANS_T' ,null,dbms_redefinition.cons_use_pk,null,null)

pause 'Sync data from snapshot
exec DBMS_REDEFINITION.SYNC_INTERIM_TABLE(uname=>user, orig_table => 'POLICYPAYMENTTRANS',int_table=> 'POLICYPAYMENTTRANS_T')
pause 'Check log status of snapshot log - hit return: '
select log_table from user_snapshot_logs where master = 'POLICYPAYMENTTRANS';

pause 'change table definition now - hit return: '
DECLARE
num_errors PLS_INTEGER;
BEGIN
DBMS_REDEFINITION.COPY_TABLE_DEPENDENTS(
    uname               => user,
    orig_table          => 'POLICYPAYMENTTRANS',
    int_table           => 'POLICYPAYMENTTRANS_T',
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
Select table_name, partitioned from user_tables where table_name in ('POLICYPAYMENTTRANS','POLICYPAYMENTTRANS_T');
select * from dba_redefinition_errors;

pause 'finish redefinition - hit return: '
BEGIN
DBMS_REDEFINITION.FINISH_REDEF_TABLE(user, 'POLICYPAYMENTTRANS', 'POLICYPAYMENTTRANS_T');
END;
/
pause ' Final status check - next run sqldeveloper compare between eps_owner and eps_owner_part - hit return:'
Select table_name, partitioned from user_tables where table_name in ('POLICYPAYMENTTRANS','POLICYPAYMENTTRANS_T');

spool off