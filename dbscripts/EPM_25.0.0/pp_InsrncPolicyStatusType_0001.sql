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

SELECT COUNT(*) FROM INSRNCPOLICYSTATUSTYPE;

MERGE INTO INSRNCPOLICYSTATUSTYPE USING dual ON ( INSURANACEPOLICYSTATUSTYPECD='5' )
WHEN MATCHED THEN UPDATE SET INSRNCPOLICYSTATUSTYPENM='Superseded',
                             INSRNCPOLICYSTATUSTYPEDESC = 'Superseded Policy Status in EPS'
WHEN NOT MATCHED THEN INSERT (INSURANACEPOLICYSTATUSTYPECD, INSRNCPOLICYSTATUSTYPENM,INSRNCPOLICYSTATUSTYPEDESC ) 
    VALUES ('5', 'Superseded', 'Superseded Policy Status in EPS');

SELECT COUNT(*) FROM INSRNCPOLICYSTATUSTYPE;

commit;
spool off
