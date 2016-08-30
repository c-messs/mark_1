
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

set echo on;

------------------------------------
--  Created 08/4/2016 - Thomas Fidd
--  Populate per email sent by Anand received 8/4.
------------------------------------
SELECT count(*) from TargetEFTApplicationType;

MERGE INTO TargetEFTApplicationType 
USING dual ON ( targetEFTApplicationTypeCd='EPS' )
WHEN NOT MATCHED THEN 
insert (targetEFTApplicationTypeCd, targetEFTApplicationTypeNm, targetEFTApplicationTypeDscr, createDate, effectiveStartDate, effectiveEndDate) values ('EPS', 'EPS', 'Enrollment Processing Modules', '04-AUG-2016', '04-AUG-2016', null);

SELECT count(*) from TargetEFTApplicationType;
commit;

set echo off;
spool off;

