
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
--  Created 07/13/2015 - Thomas Fidd
--  Populate DISPATCHROUTINGMAP table with the latest data from Matt's spreadsheet received 7/9.
------------------------------------
--  Modified 07/21/2016 - Thomas Fidd
--  Add rules for new SBM document per Anands's email dated Thu 7/21/2016 12:00 PM.
------------------------------------

select count(*) from DISPATCHROUTINGMAP;

Merge INTO DISPATCHROUTINGMAP                                                                                                                                   
using dual ON ( physicalDocumentTypeCd = 'SBMS' and                                                                                                             
                serverEnvironmentTypeCd = '0' and                                                                                                              
                targetphysicalDocumentTypeCd = 'SBMS' and                                                                                                       
                targetServerEnvironmentTypeCd = '0' and                                                                                                        
                targetTradingPartnerdentifier = 'PPCCIIO01')                                                                                                    
When not matched then                                                                                                                                           
insert(physicalDocumentTypeCd, serverEnvironmentTypeCd, targetphysicalDocumentTypeCd, targetServerEnvironmentTypeCd, targetTradingPartnerdentifier)             
values ('SBMS', '0', 'SBMS', '0', 'PPCCIIO01');           

Merge INTO DISPATCHROUTINGMAP                                                                                                                                   
using dual ON ( physicalDocumentTypeCd = 'SBMS' and                                                                                                             
                serverEnvironmentTypeCd = '1A' and                                                                                                              
                targetphysicalDocumentTypeCd = 'SBMS' and                                                                                                       
                targetServerEnvironmentTypeCd = '1A' and                                                                                                        
                targetTradingPartnerdentifier = 'PPCCIIO01')                                                                                                    
When not matched then                                                                                                                                           
insert(physicalDocumentTypeCd, serverEnvironmentTypeCd, targetphysicalDocumentTypeCd, targetServerEnvironmentTypeCd, targetTradingPartnerdentifier)             
values ('SBMS', '1A', 'SBMS', '1A', 'PPCCIIO01');           

Merge INTO DISPATCHROUTINGMAP                                                                                                                                   
using dual ON ( physicalDocumentTypeCd = 'SBMS' and                                                                                                             
                serverEnvironmentTypeCd = 'R' and                                                                                                              
                targetphysicalDocumentTypeCd = 'SBMS' and                                                                                                       
                targetServerEnvironmentTypeCd = 'R' and                                                                                                        
                targetTradingPartnerdentifier = 'PPCCIIO01')                                                                                                    
When not matched then                                                                                                                                           
insert(physicalDocumentTypeCd, serverEnvironmentTypeCd, targetphysicalDocumentTypeCd, targetServerEnvironmentTypeCd, targetTradingPartnerdentifier)             
values ('SBMS', 'R', 'SBMS', 'R', 'PPCCIIO01');           

Merge INTO DISPATCHROUTINGMAP                                                                                                                                   
using dual ON ( physicalDocumentTypeCd = 'SBMS' and                                                                                                             
                serverEnvironmentTypeCd = 'P' and                                                                                                              
                targetphysicalDocumentTypeCd = 'SBMS' and                                                                                                       
                targetServerEnvironmentTypeCd = 'P' and                                                                                                        
                targetTradingPartnerdentifier = 'PPCCIIO01')                                                                                                    
When not matched then                                                                                                                                           
insert(physicalDocumentTypeCd, serverEnvironmentTypeCd, targetphysicalDocumentTypeCd, targetServerEnvironmentTypeCd, targetTradingPartnerdentifier)             
values ('SBMS', 'P', 'SBMS', 'P', 'PPCCIIO01');           

Merge INTO DISPATCHROUTINGMAP                                                                                                                                   
using dual ON ( physicalDocumentTypeCd = 'SBMS' and                                                                                                             
                serverEnvironmentTypeCd = '1B' and                                                                                                              
                targetphysicalDocumentTypeCd = 'SBMS' and                                                                                                       
                targetServerEnvironmentTypeCd = '1B' and                                                                                                        
                targetTradingPartnerdentifier = 'PPCCIIO01')                                                                                                    
When not matched then                                                                                                                                           
insert(physicalDocumentTypeCd, serverEnvironmentTypeCd, targetphysicalDocumentTypeCd, targetServerEnvironmentTypeCd, targetTradingPartnerdentifier)             
values ('SBMS', '1B', 'SBMS', '1B', 'PPCCIIO01');           

Merge INTO DISPATCHROUTINGMAP                                                                                                                                   
using dual ON ( physicalDocumentTypeCd = 'SBMR' and                                                                                                             
                serverEnvironmentTypeCd = '0' and                                                                                                              
                targetphysicalDocumentTypeCd = 'SBMR' and                                                                                                       
                targetServerEnvironmentTypeCd = '0' and                                                                                                        
                targetTradingPartnerdentifier = 'PPCCIIO01')                                                                                                    
When not matched then                                                                                                                                           
insert(physicalDocumentTypeCd, serverEnvironmentTypeCd, targetphysicalDocumentTypeCd, targetServerEnvironmentTypeCd, targetTradingPartnerdentifier)             
values ('SBMR', '0', 'SBMR', '0', 'PPCCIIO01');           

Merge INTO DISPATCHROUTINGMAP                                                                                                                                   
using dual ON ( physicalDocumentTypeCd = 'SBMR' and                                                                                                             
                serverEnvironmentTypeCd = '1A' and                                                                                                              
                targetphysicalDocumentTypeCd = 'SBMR' and                                                                                                       
                targetServerEnvironmentTypeCd = '1A' and                                                                                                        
                targetTradingPartnerdentifier = 'PPCCIIO01')                                                                                                    
When not matched then                                                                                                                                           
insert(physicalDocumentTypeCd, serverEnvironmentTypeCd, targetphysicalDocumentTypeCd, targetServerEnvironmentTypeCd, targetTradingPartnerdentifier)             
values ('SBMR', '1A', 'SBMR', '1A', 'PPCCIIO01');           

Merge INTO DISPATCHROUTINGMAP                                                                                                                                   
using dual ON ( physicalDocumentTypeCd = 'SBMR' and                                                                                                             
                serverEnvironmentTypeCd = 'R' and                                                                                                              
                targetphysicalDocumentTypeCd = 'SBMR' and                                                                                                       
                targetServerEnvironmentTypeCd = 'R' and                                                                                                        
                targetTradingPartnerdentifier = 'PPCCIIO01')                                                                                                    
When not matched then                                                                                                                                           
insert(physicalDocumentTypeCd, serverEnvironmentTypeCd, targetphysicalDocumentTypeCd, targetServerEnvironmentTypeCd, targetTradingPartnerdentifier)             
values ('SBMR', 'R', 'SBMR', 'R', 'PPCCIIO01');           

Merge INTO DISPATCHROUTINGMAP                                                                                                                                   
using dual ON ( physicalDocumentTypeCd = 'SBMR' and                                                                                                             
                serverEnvironmentTypeCd = 'P' and                                                                                                              
                targetphysicalDocumentTypeCd = 'SBMR' and                                                                                                       
                targetServerEnvironmentTypeCd = 'P' and                                                                                                        
                targetTradingPartnerdentifier = 'PPCCIIO01')                                                                                                    
When not matched then                                                                                                                                           
insert(physicalDocumentTypeCd, serverEnvironmentTypeCd, targetphysicalDocumentTypeCd, targetServerEnvironmentTypeCd, targetTradingPartnerdentifier)             
values ('SBMR', 'P', 'SBMR', 'P', 'PPCCIIO01');           

Merge INTO DISPATCHROUTINGMAP                                                                                                                                   
using dual ON ( physicalDocumentTypeCd = 'SBMR' and                                                                                                             
                serverEnvironmentTypeCd = '1B' and                                                                                                              
                targetphysicalDocumentTypeCd = 'SBMR' and                                                                                                       
                targetServerEnvironmentTypeCd = '1B' and                                                                                                        
                targetTradingPartnerdentifier = 'PPCCIIO01')                                                                                                    
When not matched then                                                                                                                                           
insert(physicalDocumentTypeCd, serverEnvironmentTypeCd, targetphysicalDocumentTypeCd, targetServerEnvironmentTypeCd, targetTradingPartnerdentifier)             
values ('SBMR', '1B', 'SBMR', '1B', 'PPCCIIO01');           

Merge INTO DISPATCHROUTINGMAP                                                                                                                                   
using dual ON ( physicalDocumentTypeCd = 'SBMAR' and                                                                                                             
                serverEnvironmentTypeCd = '0' and                                                                                                              
                targetphysicalDocumentTypeCd = 'SBMAR' and                                                                                                       
                targetServerEnvironmentTypeCd = '0' and                                                                                                        
                targetTradingPartnerdentifier = 'PPCCIIO01')                                                                                                    
When not matched then                                                                                                                                           
insert(physicalDocumentTypeCd, serverEnvironmentTypeCd, targetphysicalDocumentTypeCd, targetServerEnvironmentTypeCd, targetTradingPartnerdentifier)             
values ('SBMAR', '0', 'SBMAR', '0', 'PPCCIIO01');           

Merge INTO DISPATCHROUTINGMAP                                                                                                                                   
using dual ON ( physicalDocumentTypeCd = 'SBMAR' and                                                                                                             
                serverEnvironmentTypeCd = '1A' and                                                                                                              
                targetphysicalDocumentTypeCd = 'SBMAR' and                                                                                                       
                targetServerEnvironmentTypeCd = '1A' and                                                                                                        
                targetTradingPartnerdentifier = 'PPCCIIO01')                                                                                                    
When not matched then                                                                                                                                           
insert(physicalDocumentTypeCd, serverEnvironmentTypeCd, targetphysicalDocumentTypeCd, targetServerEnvironmentTypeCd, targetTradingPartnerdentifier)             
values ('SBMAR', '1A', 'SBMAR', '1A', 'PPCCIIO01');           

Merge INTO DISPATCHROUTINGMAP                                                                                                                                   
using dual ON ( physicalDocumentTypeCd = 'SBMAR' and                                                                                                             
                serverEnvironmentTypeCd = 'R' and                                                                                                              
                targetphysicalDocumentTypeCd = 'SBMAR' and                                                                                                       
                targetServerEnvironmentTypeCd = 'R' and                                                                                                        
                targetTradingPartnerdentifier = 'PPCCIIO01')                                                                                                    
When not matched then                                                                                                                                           
insert(physicalDocumentTypeCd, serverEnvironmentTypeCd, targetphysicalDocumentTypeCd, targetServerEnvironmentTypeCd, targetTradingPartnerdentifier)             
values ('SBMAR', 'R', 'SBMAR', 'R', 'PPCCIIO01');           

Merge INTO DISPATCHROUTINGMAP                                                                                                                                   
using dual ON ( physicalDocumentTypeCd = 'SBMAR' and                                                                                                             
                serverEnvironmentTypeCd = 'P' and                                                                                                              
                targetphysicalDocumentTypeCd = 'SBMAR' and                                                                                                       
                targetServerEnvironmentTypeCd = 'P' and                                                                                                        
                targetTradingPartnerdentifier = 'PPCCIIO01')                                                                                                    
When not matched then                                                                                                                                           
insert(physicalDocumentTypeCd, serverEnvironmentTypeCd, targetphysicalDocumentTypeCd, targetServerEnvironmentTypeCd, targetTradingPartnerdentifier)             
values ('SBMAR', 'P', 'SBMAR', 'P', 'PPCCIIO01');           

Merge INTO DISPATCHROUTINGMAP                                                                                                                                   
using dual ON ( physicalDocumentTypeCd = 'SBMAR' and                                                                                                             
                serverEnvironmentTypeCd = '1B' and                                                                                                              
                targetphysicalDocumentTypeCd = 'SBMAR' and                                                                                                       
                targetServerEnvironmentTypeCd = '1B' and                                                                                                        
                targetTradingPartnerdentifier = 'PPCCIIO01')                                                                                                    
When not matched then                                                                                                                                           
insert(physicalDocumentTypeCd, serverEnvironmentTypeCd, targetphysicalDocumentTypeCd, targetServerEnvironmentTypeCd, targetTradingPartnerdentifier)             
values ('SBMAR', '1B', 'SBMAR', '1B', 'PPCCIIO01');           

Merge INTO DISPATCHROUTINGMAP                                                                                                                                   
using dual ON ( physicalDocumentTypeCd = 'SBMIS' and                                                                                                             
                serverEnvironmentTypeCd = '0' and                                                                                                              
                targetphysicalDocumentTypeCd = 'SBMIS' and                                                                                                       
                targetServerEnvironmentTypeCd = '0' and                                                                                                        
                targetTradingPartnerdentifier = 'PPCCIIO01')                                                                                                    
When not matched then                                                                                                                                           
insert(physicalDocumentTypeCd, serverEnvironmentTypeCd, targetphysicalDocumentTypeCd, targetServerEnvironmentTypeCd, targetTradingPartnerdentifier)             
values ('SBMIS', '0', 'SBMIS', '0', 'PPCCIIO01');           

Merge INTO DISPATCHROUTINGMAP                                                                                                                                   
using dual ON ( physicalDocumentTypeCd = 'SBMIS' and                                                                                                             
                serverEnvironmentTypeCd = '1A' and                                                                                                              
                targetphysicalDocumentTypeCd = 'SBMIS' and                                                                                                       
                targetServerEnvironmentTypeCd = '1A' and                                                                                                        
                targetTradingPartnerdentifier = 'PPCCIIO01')                                                                                                    
When not matched then                                                                                                                                           
insert(physicalDocumentTypeCd, serverEnvironmentTypeCd, targetphysicalDocumentTypeCd, targetServerEnvironmentTypeCd, targetTradingPartnerdentifier)             
values ('SBMIS', '1A', 'SBMIS', '1A', 'PPCCIIO01');           

Merge INTO DISPATCHROUTINGMAP                                                                                                                                   
using dual ON ( physicalDocumentTypeCd = 'SBMIS' and                                                                                                             
                serverEnvironmentTypeCd = 'R' and                                                                                                              
                targetphysicalDocumentTypeCd = 'SBMIS' and                                                                                                       
                targetServerEnvironmentTypeCd = 'R' and                                                                                                        
                targetTradingPartnerdentifier = 'PPCCIIO01')                                                                                                    
When not matched then                                                                                                                                           
insert(physicalDocumentTypeCd, serverEnvironmentTypeCd, targetphysicalDocumentTypeCd, targetServerEnvironmentTypeCd, targetTradingPartnerdentifier)             
values ('SBMIS', 'R', 'SBMIS', 'R', 'PPCCIIO01');           

Merge INTO DISPATCHROUTINGMAP                                                                                                                                   
using dual ON ( physicalDocumentTypeCd = 'SBMIS' and                                                                                                             
                serverEnvironmentTypeCd = 'P' and                                                                                                              
                targetphysicalDocumentTypeCd = 'SBMIS' and                                                                                                       
                targetServerEnvironmentTypeCd = 'P' and                                                                                                        
                targetTradingPartnerdentifier = 'PPCCIIO01')                                                                                                    
When not matched then                                                                                                                                           
insert(physicalDocumentTypeCd, serverEnvironmentTypeCd, targetphysicalDocumentTypeCd, targetServerEnvironmentTypeCd, targetTradingPartnerdentifier)             
values ('SBMIS', 'P', 'SBMIS', 'P', 'PPCCIIO01');           

Merge INTO DISPATCHROUTINGMAP                                                                                                                                   
using dual ON ( physicalDocumentTypeCd = 'SBMIS' and                                                                                                             
                serverEnvironmentTypeCd = '1B' and                                                                                                              
                targetphysicalDocumentTypeCd = 'SBMIS' and                                                                                                       
                targetServerEnvironmentTypeCd = '1B' and                                                                                                        
                targetTradingPartnerdentifier = 'PPCCIIO01')                                                                                                    
When not matched then                                                                                                                                           
insert(physicalDocumentTypeCd, serverEnvironmentTypeCd, targetphysicalDocumentTypeCd, targetServerEnvironmentTypeCd, targetTradingPartnerdentifier)             
values ('SBMIS', '1B', 'SBMIS', '1B', 'PPCCIIO01');           

select count(*) from DISPATCHROUTINGMAP;
commit;

set echo off;
spool off;

