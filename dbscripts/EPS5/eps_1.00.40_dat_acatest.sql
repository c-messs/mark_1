

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
--  Created 05/18/2014 - Thomas Fidd
--  Populate 5 reference tables to support the new EFT File dispatch system. 
------------------------------------
select systimestamp from dual;


column INSRNCPLANBNFTMETLTIERTYPECD format a4;
column MARKETYEAR format a4;
column NSRNCPLANVARIANTCMPTTYPECD format a4;
column CREATEBY format a10;
SELECT INSRNCPLANBNFTMETLTIERTYPECD,
	MARKETYEAR,
	INSRNCPLANVARIANTCMPTTYPECD,
	CSRPLANMULTIPLIERNUM,
	CREATEBY
FROM	COSTSHARINGREDUCTIONMULTIPLIER;

INSERT INTO COSTSHARINGREDUCTIONMULTIPLIER (
	INSRNCPLANBNFTMETLTIERTYPECD,
	MARKETYEAR,
	INSRNCPLANVARIANTCMPTTYPECD,
	CSRPLANMULTIPLIERNUM,
	CREATEBY)
SELECT INSRNCPLANBNFTMETLTIERTYPECD,
	'2016',
	INSRNCPLANVARIANTCMPTTYPECD,
	CSRPLANMULTIPLIERNUM,
	'ACATest'
FROM	COSTSHARINGREDUCTIONMULTIPLIER
WHERE   MARKETYEAR = '2015';

SELECT  INSRNCPLANBNFTMETLTIERTYPECD,
	MARKETYEAR,
	INSRNCPLANVARIANTCMPTTYPECD,
	CSRPLANMULTIPLIERNUM,
	CREATEBY
FROM    COSTSHARINGREDUCTIONMULTIPLIER;


commit;
column scriptname format a34;
column schemaname format a18;
column database format a16;

SELECT scriptname, createdate, schemaname, database 
FROM appdba_util.scriptinventory order by scriptName, createDate;

set echo off;
spool off;

