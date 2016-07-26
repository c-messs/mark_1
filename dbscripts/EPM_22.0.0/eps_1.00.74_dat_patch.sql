

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
--  Created 08/7/2015 - Thomas Fidd 
--  Requested by Mike.  Add 2 reference values.
------------------------------------

SELECT count(*) FROM BIZAPPACKERRORTYPE;

Insert into BIZAPPACKERRORTYPE 
(BIZAPPACKERRORCD,BIZAPPACKERRORNM,BIZAPPACKERRORDESC,CREATEDATE,EFFECTIVESTARTDATE,EFFECTIVEENDDATE) 
values ('E112','Exceeds allowable changes','Exceeds allowable changes',
to_date('07-AUG-2015','DD-MON-YYYY'),to_date('10-SEP-2014','DD-MON-YYYY'),null);

Insert into BIZAPPACKERRORTYPE 
(BIZAPPACKERRORCD,BIZAPPACKERRORNM,BIZAPPACKERRORDESC,CREATEDATE,EFFECTIVESTARTDATE,EFFECTIVEENDDATE) 
values ('W115','Financial Amounts Unchanged by newly reported data',
'Financial Amounts Unchanged by newly reported data',
to_date('07-AUG-2015','DD-MON-YYYY'),to_date('10-SEP-2014','DD-MON-YYYY'),null);

SELECT count(*) FROM BIZAPPACKERRORTYPE;

commit;

column scriptname format a34;
column schemaname format a28;
column database format a14;

SELECT scriptname, createdate, schemaname 
FROM   appdba_util.scriptinventory 
WHERE  createDate >= sysdate - 7
order by scriptName, createDate;

set echo off;
spool off;

