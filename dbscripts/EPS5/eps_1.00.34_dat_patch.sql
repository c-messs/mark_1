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

insert into OpenEnrollmentWindow (stateCd, programYear, openEnrollmentPeriodstartDate, openEnrollmentPeriodEndDate) values ('AK', '2016', to_date('11/01/2015', 'mm/dd/yyyy') , to_date('12/31/2016', 'mm/dd/yyyy')) ; 
insert into OpenEnrollmentWindow (stateCd, programYear, openEnrollmentPeriodstartDate, openEnrollmentPeriodEndDate) values ('AL', '2016', to_date('11/01/2015', 'mm/dd/yyyy') , to_date('12/31/2016', 'mm/dd/yyyy')) ; 
insert into OpenEnrollmentWindow (stateCd, programYear, openEnrollmentPeriodstartDate, openEnrollmentPeriodEndDate) values ('AR', '2016', to_date('11/01/2015', 'mm/dd/yyyy') , to_date('12/31/2016', 'mm/dd/yyyy')) ; 
insert into OpenEnrollmentWindow (stateCd, programYear, openEnrollmentPeriodstartDate, openEnrollmentPeriodEndDate) values ('AZ', '2016', to_date('11/01/2015', 'mm/dd/yyyy') , to_date('12/31/2016', 'mm/dd/yyyy')) ; 
insert into OpenEnrollmentWindow (stateCd, programYear, openEnrollmentPeriodstartDate, openEnrollmentPeriodEndDate) values ('CA', '2016', to_date('11/01/2015', 'mm/dd/yyyy') , to_date('12/31/2016', 'mm/dd/yyyy')) ; 
insert into OpenEnrollmentWindow (stateCd, programYear, openEnrollmentPeriodstartDate, openEnrollmentPeriodEndDate) values ('CO', '2016', to_date('11/01/2015', 'mm/dd/yyyy') , to_date('12/31/2016', 'mm/dd/yyyy')) ; 
insert into OpenEnrollmentWindow (stateCd, programYear, openEnrollmentPeriodstartDate, openEnrollmentPeriodEndDate) values ('CT', '2016', to_date('11/01/2015', 'mm/dd/yyyy') , to_date('12/31/2016', 'mm/dd/yyyy')) ; 
insert into OpenEnrollmentWindow (stateCd, programYear, openEnrollmentPeriodstartDate, openEnrollmentPeriodEndDate) values ('DC', '2016', to_date('11/01/2015', 'mm/dd/yyyy') , to_date('12/31/2016', 'mm/dd/yyyy')) ; 
insert into OpenEnrollmentWindow (stateCd, programYear, openEnrollmentPeriodstartDate, openEnrollmentPeriodEndDate) values ('DE', '2016', to_date('11/01/2015', 'mm/dd/yyyy') , to_date('12/31/2016', 'mm/dd/yyyy')) ; 
insert into OpenEnrollmentWindow (stateCd, programYear, openEnrollmentPeriodstartDate, openEnrollmentPeriodEndDate) values ('FL', '2016', to_date('11/01/2015', 'mm/dd/yyyy') , to_date('12/31/2016', 'mm/dd/yyyy')) ; 
insert into OpenEnrollmentWindow (stateCd, programYear, openEnrollmentPeriodstartDate, openEnrollmentPeriodEndDate) values ('GA', '2016', to_date('11/01/2015', 'mm/dd/yyyy') , to_date('12/31/2016', 'mm/dd/yyyy')) ; 
insert into OpenEnrollmentWindow (stateCd, programYear, openEnrollmentPeriodstartDate, openEnrollmentPeriodEndDate) values ('HI', '2016', to_date('11/01/2015', 'mm/dd/yyyy') , to_date('12/31/2016', 'mm/dd/yyyy')) ; 
insert into OpenEnrollmentWindow (stateCd, programYear, openEnrollmentPeriodstartDate, openEnrollmentPeriodEndDate) values ('IA', '2016', to_date('11/01/2015', 'mm/dd/yyyy') , to_date('12/31/2016', 'mm/dd/yyyy')) ; 
insert into OpenEnrollmentWindow (stateCd, programYear, openEnrollmentPeriodstartDate, openEnrollmentPeriodEndDate) values ('ID', '2016', to_date('11/01/2015', 'mm/dd/yyyy') , to_date('12/31/2016', 'mm/dd/yyyy')) ; 
insert into OpenEnrollmentWindow (stateCd, programYear, openEnrollmentPeriodstartDate, openEnrollmentPeriodEndDate) values ('IL', '2016', to_date('11/01/2015', 'mm/dd/yyyy') , to_date('12/31/2016', 'mm/dd/yyyy')) ; 
insert into OpenEnrollmentWindow (stateCd, programYear, openEnrollmentPeriodstartDate, openEnrollmentPeriodEndDate) values ('IN', '2016', to_date('11/01/2015', 'mm/dd/yyyy') , to_date('12/31/2016', 'mm/dd/yyyy')) ; 
insert into OpenEnrollmentWindow (stateCd, programYear, openEnrollmentPeriodstartDate, openEnrollmentPeriodEndDate) values ('KS', '2016', to_date('11/01/2015', 'mm/dd/yyyy') , to_date('12/31/2016', 'mm/dd/yyyy')) ; 
insert into OpenEnrollmentWindow (stateCd, programYear, openEnrollmentPeriodstartDate, openEnrollmentPeriodEndDate) values ('KY', '2016', to_date('11/01/2015', 'mm/dd/yyyy') , to_date('12/31/2016', 'mm/dd/yyyy')) ; 
insert into OpenEnrollmentWindow (stateCd, programYear, openEnrollmentPeriodstartDate, openEnrollmentPeriodEndDate) values ('LA', '2016', to_date('11/01/2015', 'mm/dd/yyyy') , to_date('12/31/2016', 'mm/dd/yyyy')) ; 
insert into OpenEnrollmentWindow (stateCd, programYear, openEnrollmentPeriodstartDate, openEnrollmentPeriodEndDate) values ('MA', '2016', to_date('11/01/2015', 'mm/dd/yyyy') , to_date('12/31/2016', 'mm/dd/yyyy')) ; 
insert into OpenEnrollmentWindow (stateCd, programYear, openEnrollmentPeriodstartDate, openEnrollmentPeriodEndDate) values ('MD', '2016', to_date('11/01/2015', 'mm/dd/yyyy') , to_date('12/31/2016', 'mm/dd/yyyy')) ; 
insert into OpenEnrollmentWindow (stateCd, programYear, openEnrollmentPeriodstartDate, openEnrollmentPeriodEndDate) values ('ME', '2016', to_date('11/01/2015', 'mm/dd/yyyy') , to_date('12/31/2016', 'mm/dd/yyyy')) ; 
insert into OpenEnrollmentWindow (stateCd, programYear, openEnrollmentPeriodstartDate, openEnrollmentPeriodEndDate) values ('MI', '2016', to_date('11/01/2015', 'mm/dd/yyyy') , to_date('12/31/2016', 'mm/dd/yyyy')) ; 
insert into OpenEnrollmentWindow (stateCd, programYear, openEnrollmentPeriodstartDate, openEnrollmentPeriodEndDate) values ('MN', '2016', to_date('11/01/2015', 'mm/dd/yyyy') , to_date('12/31/2016', 'mm/dd/yyyy')) ; 
insert into OpenEnrollmentWindow (stateCd, programYear, openEnrollmentPeriodstartDate, openEnrollmentPeriodEndDate) values ('MO', '2016', to_date('11/01/2015', 'mm/dd/yyyy') , to_date('12/31/2016', 'mm/dd/yyyy')) ; 
insert into OpenEnrollmentWindow (stateCd, programYear, openEnrollmentPeriodstartDate, openEnrollmentPeriodEndDate) values ('MS', '2016', to_date('11/01/2015', 'mm/dd/yyyy') , to_date('12/31/2016', 'mm/dd/yyyy')) ; 
insert into OpenEnrollmentWindow (stateCd, programYear, openEnrollmentPeriodstartDate, openEnrollmentPeriodEndDate) values ('MT', '2016', to_date('11/01/2015', 'mm/dd/yyyy') , to_date('12/31/2016', 'mm/dd/yyyy')) ; 
insert into OpenEnrollmentWindow (stateCd, programYear, openEnrollmentPeriodstartDate, openEnrollmentPeriodEndDate) values ('NC', '2016', to_date('11/01/2015', 'mm/dd/yyyy') , to_date('12/31/2016', 'mm/dd/yyyy')) ; 
insert into OpenEnrollmentWindow (stateCd, programYear, openEnrollmentPeriodstartDate, openEnrollmentPeriodEndDate) values ('ND', '2016', to_date('11/01/2015', 'mm/dd/yyyy') , to_date('12/31/2016', 'mm/dd/yyyy')) ; 
insert into OpenEnrollmentWindow (stateCd, programYear, openEnrollmentPeriodstartDate, openEnrollmentPeriodEndDate) values ('NE', '2016', to_date('11/01/2015', 'mm/dd/yyyy') , to_date('12/31/2016', 'mm/dd/yyyy')) ; 
insert into OpenEnrollmentWindow (stateCd, programYear, openEnrollmentPeriodstartDate, openEnrollmentPeriodEndDate) values ('NH', '2016', to_date('11/01/2015', 'mm/dd/yyyy') , to_date('12/31/2016', 'mm/dd/yyyy')) ; 
insert into OpenEnrollmentWindow (stateCd, programYear, openEnrollmentPeriodstartDate, openEnrollmentPeriodEndDate) values ('NJ', '2016', to_date('11/01/2015', 'mm/dd/yyyy') , to_date('12/31/2016', 'mm/dd/yyyy')) ; 
insert into OpenEnrollmentWindow (stateCd, programYear, openEnrollmentPeriodstartDate, openEnrollmentPeriodEndDate) values ('NM', '2016', to_date('11/01/2015', 'mm/dd/yyyy') , to_date('12/31/2016', 'mm/dd/yyyy')) ; 
insert into OpenEnrollmentWindow (stateCd, programYear, openEnrollmentPeriodstartDate, openEnrollmentPeriodEndDate) values ('NV', '2016', to_date('11/01/2015', 'mm/dd/yyyy') , to_date('12/31/2016', 'mm/dd/yyyy')) ; 
insert into OpenEnrollmentWindow (stateCd, programYear, openEnrollmentPeriodstartDate, openEnrollmentPeriodEndDate) values ('NY', '2016', to_date('11/01/2015', 'mm/dd/yyyy') , to_date('12/31/2016', 'mm/dd/yyyy')) ; 
insert into OpenEnrollmentWindow (stateCd, programYear, openEnrollmentPeriodstartDate, openEnrollmentPeriodEndDate) values ('OH', '2016', to_date('11/01/2015', 'mm/dd/yyyy') , to_date('12/31/2016', 'mm/dd/yyyy')) ; 
insert into OpenEnrollmentWindow (stateCd, programYear, openEnrollmentPeriodstartDate, openEnrollmentPeriodEndDate) values ('OK', '2016', to_date('11/01/2015', 'mm/dd/yyyy') , to_date('12/31/2016', 'mm/dd/yyyy')) ; 
insert into OpenEnrollmentWindow (stateCd, programYear, openEnrollmentPeriodstartDate, openEnrollmentPeriodEndDate) values ('OR', '2016', to_date('11/01/2015', 'mm/dd/yyyy') , to_date('12/31/2016', 'mm/dd/yyyy')) ; 
insert into OpenEnrollmentWindow (stateCd, programYear, openEnrollmentPeriodstartDate, openEnrollmentPeriodEndDate) values ('PA', '2016', to_date('11/01/2015', 'mm/dd/yyyy') , to_date('12/31/2016', 'mm/dd/yyyy')) ; 
insert into OpenEnrollmentWindow (stateCd, programYear, openEnrollmentPeriodstartDate, openEnrollmentPeriodEndDate) values ('RI', '2016', to_date('11/01/2015', 'mm/dd/yyyy') , to_date('12/31/2016', 'mm/dd/yyyy')) ; 
insert into OpenEnrollmentWindow (stateCd, programYear, openEnrollmentPeriodstartDate, openEnrollmentPeriodEndDate) values ('SC', '2016', to_date('11/01/2015', 'mm/dd/yyyy') , to_date('12/31/2016', 'mm/dd/yyyy')) ; 
insert into OpenEnrollmentWindow (stateCd, programYear, openEnrollmentPeriodstartDate, openEnrollmentPeriodEndDate) values ('SD', '2016', to_date('11/01/2015', 'mm/dd/yyyy') , to_date('12/31/2016', 'mm/dd/yyyy')) ; 
insert into OpenEnrollmentWindow (stateCd, programYear, openEnrollmentPeriodstartDate, openEnrollmentPeriodEndDate) values ('TN', '2016', to_date('11/01/2015', 'mm/dd/yyyy') , to_date('12/31/2016', 'mm/dd/yyyy')) ; 
insert into OpenEnrollmentWindow (stateCd, programYear, openEnrollmentPeriodstartDate, openEnrollmentPeriodEndDate) values ('TX', '2016', to_date('11/01/2015', 'mm/dd/yyyy') , to_date('12/31/2016', 'mm/dd/yyyy')) ; 
insert into OpenEnrollmentWindow (stateCd, programYear, openEnrollmentPeriodstartDate, openEnrollmentPeriodEndDate) values ('UT', '2016', to_date('11/01/2015', 'mm/dd/yyyy') , to_date('12/31/2016', 'mm/dd/yyyy')) ; 
insert into OpenEnrollmentWindow (stateCd, programYear, openEnrollmentPeriodstartDate, openEnrollmentPeriodEndDate) values ('VA', '2016', to_date('11/01/2015', 'mm/dd/yyyy') , to_date('12/31/2016', 'mm/dd/yyyy')) ; 
insert into OpenEnrollmentWindow (stateCd, programYear, openEnrollmentPeriodstartDate, openEnrollmentPeriodEndDate) values ('VT', '2016', to_date('11/01/2015', 'mm/dd/yyyy') , to_date('12/31/2016', 'mm/dd/yyyy')) ; 
insert into OpenEnrollmentWindow (stateCd, programYear, openEnrollmentPeriodstartDate, openEnrollmentPeriodEndDate) values ('WA', '2016', to_date('11/01/2015', 'mm/dd/yyyy') , to_date('12/31/2016', 'mm/dd/yyyy')) ; 
insert into OpenEnrollmentWindow (stateCd, programYear, openEnrollmentPeriodstartDate, openEnrollmentPeriodEndDate) values ('WI', '2016', to_date('11/01/2015', 'mm/dd/yyyy') , to_date('12/31/2016', 'mm/dd/yyyy')) ; 
insert into OpenEnrollmentWindow (stateCd, programYear, openEnrollmentPeriodstartDate, openEnrollmentPeriodEndDate) values ('WV', '2016', to_date('11/01/2015', 'mm/dd/yyyy') , to_date('12/31/2016', 'mm/dd/yyyy')) ; 
insert into OpenEnrollmentWindow (stateCd, programYear, openEnrollmentPeriodstartDate, openEnrollmentPeriodEndDate) values ('WY', '2016', to_date('11/01/2015', 'mm/dd/yyyy') , to_date('12/31/2016', 'mm/dd/yyyy')) ; 



commit;
spool off
