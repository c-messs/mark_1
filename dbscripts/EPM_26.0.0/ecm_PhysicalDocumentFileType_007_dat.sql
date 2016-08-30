set SERVEROUTPUT ON FORMAT WRAPPED
set APPINFO ON
--SET VERIFY OFF

--SET FEEDBACK OFF
--SET TERMOUT OFF

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
--  Populate PhysicalDocumentFileType per latest spreadsheet from Matt last Thursday.
------------------------------------
--  Modified 03/29/2016 - Thomas Fidd
--  Add two new documents per Sudhakar's email 3/29/2016 11:17am
------------------------------------
--  Modified 07/06/2016 - Thomas Fidd
--  Add rules for new SBM Preliminary 820 document per Zaki's email dated 6/30/2016 1:17pm.
------------------------------------

column physicalDocumentTypeNm format a39;
select physicalDocumentTypeCd, physicalDocumentTypeNm from PhysicalDocumentFileType;

Merge INTO PhysicalDocumentFileType                                                                                                                             
using dual ON ( PhysicalDocumentTypeCd = 'MALG')                                                                                                                
when matched then                                                                                                                                               
update set PhysicalDocumentTypeNm = 'MALG', PhysicalDocumentTypeDscr = 'Manual Adjustment Process'                                                              
When not matched then                                                                                                                                           
insert(PhysicalDocumentTypeCd,PhysicalDocumentTypeNm, PhysicalDocumentTypeDscr)                                                                                 
values ('MALG', 'MALG', 'Manual Adjustment Process');                                                                                                           
                                                                                                                                                                
Merge INTO PhysicalDocumentFileType                                                                                                                             
using dual ON ( PhysicalDocumentTypeCd = '810')                                                                                                                 
when matched then                                                                                                                                               
update set PhysicalDocumentTypeNm = '810', PhysicalDocumentTypeDscr = '810 Process'                                                                             
When not matched then                                                                                                                                           
insert(PhysicalDocumentTypeCd,PhysicalDocumentTypeNm, PhysicalDocumentTypeDscr)                                                                                 
values ('810', '810', '810 Process');                                                                                                                           
                                                                                                                                                                
Merge INTO PhysicalDocumentFileType                                                                                                                             
using dual ON ( PhysicalDocumentTypeCd = 'RAPL')                                                                                                                
when matched then                                                                                                                                               
update set PhysicalDocumentTypeNm = 'RAPL', PhysicalDocumentTypeDscr = 'RA Proration Process'                                                                   
When not matched then                                                                                                                                           
insert(PhysicalDocumentTypeCd,PhysicalDocumentTypeNm, PhysicalDocumentTypeDscr)                                                                                 
values ('RAPL', 'RAPL', 'RA Proration Process');                                                                                                                
                                                                                                                                                                
Merge INTO PhysicalDocumentFileType                                                                                                                             
using dual ON ( PhysicalDocumentTypeCd = 'RAAL')                                                                                                                
when matched then                                                                                                                                               
update set PhysicalDocumentTypeNm = 'RAAL', PhysicalDocumentTypeDscr = 'RA Appeals Process'                                                                     
When not matched then                                                                                                                                           
insert(PhysicalDocumentTypeCd,PhysicalDocumentTypeNm, PhysicalDocumentTypeDscr)                                                                                 
values ('RAAL', 'RAAL', 'RA Appeals Process');                                                                                                                  
                                                                                                                                                                
Merge INTO PhysicalDocumentFileType                                                                                                                             
using dual ON ( PhysicalDocumentTypeCd = 'RADL')                                                                                                                
when matched then                                                                                                                                               
update set PhysicalDocumentTypeNm = 'RADL', PhysicalDocumentTypeDscr = 'RA Default Charges Process'                                                             
When not matched then                                                                                                                                           
insert(PhysicalDocumentTypeCd,PhysicalDocumentTypeNm, PhysicalDocumentTypeDscr)                                                                                 
values ('RADL', 'RADL', 'RA Default Charges Process');                                                                                                          
                                                                                                                                                                
Merge INTO PhysicalDocumentFileType                                                                                                                             
using dual ON ( PhysicalDocumentTypeCd = 'RIPL')                                                                                                                
when matched then                                                                                                                                               
update set PhysicalDocumentTypeNm = 'RIPL', PhysicalDocumentTypeDscr = 'RI Proration Process'                                                                   
When not matched then                                                                                                                                           
insert(PhysicalDocumentTypeCd,PhysicalDocumentTypeNm, PhysicalDocumentTypeDscr)                                                                                 
values ('RIPL', 'RIPL', 'RI Proration Process');                                                                                                                
                                                                                                                                                                
Merge INTO PhysicalDocumentFileType                                                                                                                             
using dual ON ( PhysicalDocumentTypeCd = 'RIAL')                                                                                                                
when matched then                                                                                                                                               
update set PhysicalDocumentTypeNm = 'RIAL', PhysicalDocumentTypeDscr = 'RI Appeals Process'                                                                     
When not matched then                                                                                                                                           
insert(PhysicalDocumentTypeCd,PhysicalDocumentTypeNm, PhysicalDocumentTypeDscr)                                                                                 
values ('RIAL', 'RIAL', 'RI Appeals Process');                                                                                                                  
                                                                                                                                                                
Merge INTO PhysicalDocumentFileType                                                                                                                             
using dual ON ( PhysicalDocumentTypeCd = 'ISOS')                                                                                                                
when matched then                                                                                                                                               
update set PhysicalDocumentTypeNm = 'ISOS', PhysicalDocumentTypeDscr = 'Issuer Transaction Operational Summary Report'                                          
When not matched then                                                                                                                                           
insert(PhysicalDocumentTypeCd,PhysicalDocumentTypeNm, PhysicalDocumentTypeDscr)                                                                                 
values ('ISOS', 'ISOS', 'Issuer Transaction Operational Summary Report');                                                                                       
                                                                                                                                                                
Merge INTO PhysicalDocumentFileType                                                                                                                             
using dual ON ( PhysicalDocumentTypeCd = 'INOS')                                                                                                                
when matched then                                                                                                                                               
update set PhysicalDocumentTypeNm = 'INOS', PhysicalDocumentTypeDscr = 'Invoice Transaction Operational Summary Report'                                         
When not matched then                                                                                                                                           
insert(PhysicalDocumentTypeCd,PhysicalDocumentTypeNm, PhysicalDocumentTypeDscr)                                                                                 
values ('INOS', 'INOS', 'Invoice Transaction Operational Summary Report');                                                                                      
                                                                                                                                                                
Merge INTO PhysicalDocumentFileType                                                                                                                             
using dual ON ( PhysicalDocumentTypeCd = '820')                                                                                                                 
when matched then                                                                                                                                               
update set PhysicalDocumentTypeNm = '820', PhysicalDocumentTypeDscr = '820 Payment Reporting'                                                                   
When not matched then                                                                                                                                           
insert(PhysicalDocumentTypeCd,PhysicalDocumentTypeNm, PhysicalDocumentTypeDscr)                                                                                 
values ('820', '820', '820 Payment Reporting');                                                                                                                 
                                                                                                                                                                
Merge INTO PhysicalDocumentFileType                                                                                                                             
using dual ON ( PhysicalDocumentTypeCd = 'TPSM')                                                                                                                
when matched then                                                                                                                                               
update set PhysicalDocumentTypeNm = 'TPSM', PhysicalDocumentTypeDscr = 'Transfer Payment CMS Summary Report'                                                    
When not matched then                                                                                                                                           
insert(PhysicalDocumentTypeCd,PhysicalDocumentTypeNm, PhysicalDocumentTypeDscr)                                                                                 
values ('TPSM', 'TPSM', 'Transfer Payment CMS Summary Report');                                                                                                 
                                                                                                                                                                
Merge INTO PhysicalDocumentFileType                                                                                                                             
using dual ON ( PhysicalDocumentTypeCd = 'TPST')                                                                                                                
when matched then                                                                                                                                               
update set PhysicalDocumentTypeNm = 'TPST', PhysicalDocumentTypeDscr = 'RA Transfer Payment State Report'                                                       
When not matched then                                                                                                                                           
insert(PhysicalDocumentTypeCd,PhysicalDocumentTypeNm, PhysicalDocumentTypeDscr)                                                                                 
values ('TPST', 'TPST', 'RA Transfer Payment State Report');                                                                                                    
                                                                                                                                                                
Merge INTO PhysicalDocumentFileType                                                                                                                             
using dual ON ( PhysicalDocumentTypeCd = 'TPIR')                                                                                                                
when matched then                                                                                                                                               
update set PhysicalDocumentTypeNm = 'TPIR', PhysicalDocumentTypeDscr = 'RA Transfer Payment Issuer Report'                                                      
When not matched then                                                                                                                                           
insert(PhysicalDocumentTypeCd,PhysicalDocumentTypeNm, PhysicalDocumentTypeDscr)                                                                                 
values ('TPIR', 'TPIR', 'RA Transfer Payment Issuer Report');                                                                                                   
                                                                                                                                                                
Merge INTO PhysicalDocumentFileType                                                                                                                             
using dual ON ( PhysicalDocumentTypeCd = 'TPPP')                                                                                                                
when matched then                                                                                                                                               
update set PhysicalDocumentTypeNm = 'TPPP', PhysicalDocumentTypeDscr = 'RA Transfer Payment - Payment Report'                                                   
When not matched then                                                                                                                                           
insert(PhysicalDocumentTypeCd,PhysicalDocumentTypeNm, PhysicalDocumentTypeDscr)                                                                                 
values ('TPPP', 'TPPP', 'RA Transfer Payment - Payment Report');                                                                                                
                                                                                                                                                                
Merge INTO PhysicalDocumentFileType                                                                                                                             
using dual ON ( PhysicalDocumentTypeCd = 'RAPS')                                                                                                                
when matched then                                                                                                                                               
update set PhysicalDocumentTypeNm = 'RAPS', PhysicalDocumentTypeDscr = 'RA Proration CMS Summary Report'                                                        
When not matched then                                                                                                                                           
insert(PhysicalDocumentTypeCd,PhysicalDocumentTypeNm, PhysicalDocumentTypeDscr)                                                                                 
values ('RAPS', 'RAPS', 'RA Proration CMS Summary Report');                                                                                                     
                                                                                                                                                                
Merge INTO PhysicalDocumentFileType                                                                                                                             
using dual ON ( PhysicalDocumentTypeCd = 'APIR')                                                                                                                
when matched then                                                                                                                                               
update set PhysicalDocumentTypeNm = 'APIR', PhysicalDocumentTypeDscr = 'RA Proration Issuer Report'                                                             
When not matched then                                                                                                                                           
insert(PhysicalDocumentTypeCd,PhysicalDocumentTypeNm, PhysicalDocumentTypeDscr)                                                                                 
values ('APIR', 'APIR', 'RA Proration Issuer Report');                                                                                                          
                                                                                                                                                                
Merge INTO PhysicalDocumentFileType                                                                                                                             
using dual ON ( PhysicalDocumentTypeCd = 'RAPP')                                                                                                                
when matched then                                                                                                                                               
update set PhysicalDocumentTypeNm = 'RAPP', PhysicalDocumentTypeDscr = 'RA Proration Payment Report'                                                            
When not matched then                                                                                                                                           
insert(PhysicalDocumentTypeCd,PhysicalDocumentTypeNm, PhysicalDocumentTypeDscr)                                                                                 
values ('RAPP', 'RAPP', 'RA Proration Payment Report');                                                                                                         
                                                                                                                                                                
Merge INTO PhysicalDocumentFileType                                                                                                                             
using dual ON ( PhysicalDocumentTypeCd = 'RADS')                                                                                                                
when matched then                                                                                                                                               
update set PhysicalDocumentTypeNm = 'RADS', PhysicalDocumentTypeDscr = 'RA Default Charge CMS Summary Report'                                                   
When not matched then                                                                                                                                           
insert(PhysicalDocumentTypeCd,PhysicalDocumentTypeNm, PhysicalDocumentTypeDscr)                                                                                 
values ('RADS', 'RADS', 'RA Default Charge CMS Summary Report');                                                                                                
                                                                                                                                                                
Merge INTO PhysicalDocumentFileType                                                                                                                             
using dual ON ( PhysicalDocumentTypeCd = 'RADP')                                                                                                                
when matched then                                                                                                                                               
update set PhysicalDocumentTypeNm = 'RADP', PhysicalDocumentTypeDscr = 'RA Default Charge Payment Report'                                                       
When not matched then                                                                                                                                           
insert(PhysicalDocumentTypeCd,PhysicalDocumentTypeNm, PhysicalDocumentTypeDscr)                                                                                 
values ('RADP', 'RADP', 'RA Default Charge Payment Report');                                                                                                    
                                                                                                                                                                
Merge INTO PhysicalDocumentFileType                                                                                                                             
using dual ON ( PhysicalDocumentTypeCd = 'RIPS')                                                                                                                
when matched then                                                                                                                                               
update set PhysicalDocumentTypeNm = 'RIPS', PhysicalDocumentTypeDscr = 'RI Proration CMS Summary Report'                                                        
When not matched then                                                                                                                                           
insert(PhysicalDocumentTypeCd,PhysicalDocumentTypeNm, PhysicalDocumentTypeDscr)                                                                                 
values ('RIPS', 'RIPS', 'RI Proration CMS Summary Report');                                                                                                     
                                                                                                                                                                
Merge INTO PhysicalDocumentFileType                                                                                                                             
using dual ON ( PhysicalDocumentTypeCd = 'IPST')                                                                                                                
when matched then                                                                                                                                               
update set PhysicalDocumentTypeNm = 'IPST', PhysicalDocumentTypeDscr = 'RI Proration State Report'                                                              
When not matched then                                                                                                                                           
insert(PhysicalDocumentTypeCd,PhysicalDocumentTypeNm, PhysicalDocumentTypeDscr)                                                                                 
values ('IPST', 'IPST', 'RI Proration State Report');                                                                                                           
                                                                                                                                                                
Merge INTO PhysicalDocumentFileType                                                                                                                             
using dual ON ( PhysicalDocumentTypeCd = 'IPIR')                                                                                                                
when matched then                                                                                                                                               
update set PhysicalDocumentTypeNm = 'IPIR', PhysicalDocumentTypeDscr = 'RI Proration Issuer Report'                                                             
When not matched then                                                                                                                                           
insert(PhysicalDocumentTypeCd,PhysicalDocumentTypeNm, PhysicalDocumentTypeDscr)                                                                                 
values ('IPIR', 'IPIR', 'RI Proration Issuer Report');                                                                                                          
                                                                                                                                                                
Merge INTO PhysicalDocumentFileType                                                                                                                             
using dual ON ( PhysicalDocumentTypeCd = 'RIPR')                                                                                                                
when matched then                                                                                                                                               
update set PhysicalDocumentTypeNm = 'RIPR', PhysicalDocumentTypeDscr = 'RI Proration Payment Report'                                                            
When not matched then                                                                                                                                           
insert(PhysicalDocumentTypeCd,PhysicalDocumentTypeNm, PhysicalDocumentTypeDscr)                                                                                 
values ('RIPR', 'RIPR', 'RI Proration Payment Report');                                                                                                         
                                                                                                                                                                
Merge INTO PhysicalDocumentFileType                                                                                                                             
using dual ON ( PhysicalDocumentTypeCd = 'IMAE')                                                                                                                
when matched then                                                                                                                                               
update set PhysicalDocumentTypeNm = 'IMAE', PhysicalDocumentTypeDscr = 'InvoiceMEssage Advice Error report from 810 invoice '                                   
When not matched then                                                                                                                                           
insert(PhysicalDocumentTypeCd,PhysicalDocumentTypeNm, PhysicalDocumentTypeDscr)                                                                                 
values ('IMAE', 'IMAE', 'InvoiceMEssage Advice Error report from 810 invoice ');                                                                                
                                                                                                                                                                
Merge INTO PhysicalDocumentFileType                                                                                                                             
using dual ON ( PhysicalDocumentTypeCd = '810X')                                                                                                                
when matched then                                                                                                                                               
update set PhysicalDocumentTypeNm = '810X', PhysicalDocumentTypeDscr = 'XML version of thee 810 invoice'                                                        
When not matched then                                                                                                                                           
insert(PhysicalDocumentTypeCd,PhysicalDocumentTypeNm, PhysicalDocumentTypeDscr)                                                                                 
values ('810X', '810X', 'XML version of thee 810 invoice');                                                                                                     
                                                                                                                                                               
/* 9/22/2015 - Thomas Fidd, per email from Sudhakar at 9:56 am 824S is being replaced by P824S. */ 

Merge INTO PhysicalDocumentFileType                                                                                                                             
using dual ON ( PhysicalDocumentTypeCd = '824S')                                                                                                                
when matched then                                                                                                                                               
update set PhysicalDocumentTypeNm = '824S', PhysicalDocumentTypeDscr = 'Application advice status sent to Hub/Higlas'                                           
When not matched then                                                                                                                                           
insert(PhysicalDocumentTypeCd,PhysicalDocumentTypeNm, PhysicalDocumentTypeDscr)                                                                                 
values ('824S', '824S', 'Application advice status sent to Hub/Higlas');                                                                                        
                                                                                                                                                                
Merge INTO PhysicalDocumentFileType                                                                                                                             
using dual ON ( PhysicalDocumentTypeCd = 'PAME')                                                                                                                
when matched then                                                                                                                                               
update set PhysicalDocumentTypeNm = 'PAME', PhysicalDocumentTypeDscr = 'Payment Advice Message Error report after processing 835 sent to CMS'                   
When not matched then                                                                                                                                           
insert(PhysicalDocumentTypeCd,PhysicalDocumentTypeNm, PhysicalDocumentTypeDscr)                                                                                 
values ('PAME', 'PAME', 'Payment Advice Message Error report after processing 835 sent to CMS');                                                                
                                                                                                                                                                
Merge INTO PhysicalDocumentFileType                                                                                                                             
using dual ON ( PhysicalDocumentTypeCd = '835')                                                                                                                 
when matched then                                                                                                                                               
update set PhysicalDocumentTypeNm = '835', PhysicalDocumentTypeDscr = 'Copy of the processed 835 sent to CMS'                                                   
When not matched then                                                                                                                                           
insert(PhysicalDocumentTypeCd,PhysicalDocumentTypeNm, PhysicalDocumentTypeDscr)                                                                                 
values ('835', '835', 'Copy of the processed 835 sent to CMS'); 

/* Thomas Fidd - Added per Anand 6/18/2015 */
Merge INTO PhysicalDocumentFileType                                                                                                                             
using dual ON ( PhysicalDocumentTypeCd = 'TIOS')                                                                                                                
when matched then                                                                                                                                               
update set PhysicalDocumentTypeNm = 'TIOS', PhysicalDocumentTypeDscr = 'Transmitted Inv Operation Report'                                                       
When not matched then                                                                                                                                           
insert(PhysicalDocumentTypeCd,PhysicalDocumentTypeNm, PhysicalDocumentTypeDscr)                                                                                 
values ('TIOS', 'TIOS', 'Transmitted Inv Operation Report');                                                                                                    

/* Thomas Fidd - Added per Matt 6/29/2015 */
Merge INTO PhysicalDocumentFileType                                                                                                                             
using dual ON ( PhysicalDocumentTypeCd = 'IN824')                                                                                                                
when matched then                                                                                                                                               
update set PhysicalDocumentTypeNm = 'IN824', PhysicalDocumentTypeDscr = 'Invoice Advice from Hub/Higlas'                                                       
When not matched then                                                                                                                                           
insert(PhysicalDocumentTypeCd,PhysicalDocumentTypeNm, PhysicalDocumentTypeDscr)                                                                                 
values ('IN824', 'IN824', 'Invoice Advice from Hub/Higlas');                                                                                                    

Merge INTO PhysicalDocumentFileType                                                                                                                             
using dual ON ( PhysicalDocumentTypeCd = 'IN810')                                                                                                                
when matched then                                                                                                                                               
update set PhysicalDocumentTypeNm = 'IN810', PhysicalDocumentTypeDscr = 'XML version of the 810 invoice'                                                       
When not matched then                                                                                                                                           
insert(PhysicalDocumentTypeCd,PhysicalDocumentTypeNm, PhysicalDocumentTypeDscr)                                                                                 
values ('IN810', 'IN810', 'XML version of the 810 invoice');                                                                                                    

Merge INTO PhysicalDocumentFileType                                                                                                                             
using dual ON ( PhysicalDocumentTypeCd = 'I810E')                                                                                                                
when matched then                                                                                                                                               
update set PhysicalDocumentTypeNm = 'I810E', PhysicalDocumentTypeDscr = 'Error report after processing 810'                                                       
When not matched then                                                                                                                                           
insert(PhysicalDocumentTypeCd,PhysicalDocumentTypeNm, PhysicalDocumentTypeDscr)                                                                                 
values ('I810E', 'I810E', 'Error report after processing 810');                                                                                                    

Merge INTO PhysicalDocumentFileType                                                                                                                             
using dual ON ( PhysicalDocumentTypeCd = 'PR835')                                                                                                                
when matched then                                                                                                                                               
update set PhysicalDocumentTypeNm = 'PR835', PhysicalDocumentTypeDscr = 'Payment Remittance Details sent to PPM from Hub/HIGLAS'                                                       
When not matched then                                                                                                                                           
insert(PhysicalDocumentTypeCd,PhysicalDocumentTypeNm, PhysicalDocumentTypeDscr)                                                                                 
values ('PR835', 'PR835', 'Payment Remittance Details sent to PPM from Hub/HIGLAS');                                                                                                    

Merge INTO PhysicalDocumentFileType                                                                                                                             
using dual ON ( PhysicalDocumentTypeCd = 'P824N')                                                                                                                
when matched then                                                                                                                                               
update set PhysicalDocumentTypeNm = 'P824N', PhysicalDocumentTypeDscr = '824N also contains additional remittance details'                                                       
When not matched then                                                                                                                                           
insert(PhysicalDocumentTypeCd,PhysicalDocumentTypeNm, PhysicalDocumentTypeDscr)                                                                                 
values ('P824N', 'P824N', '824N also contains additional remittance details');                                                                                                    


/* 7/13/2015 - Thomas Fidd, Added per Matt's meeting invite 7/9/2015 at 2:50 PM. */
Merge INTO PhysicalDocumentFileType                                                                                                                             
using dual ON ( PhysicalDocumentTypeCd = 'F820')                                                                                                                
when matched then                                                                                                                                               
update set PhysicalDocumentTypeNm = 'F820', PhysicalDocumentTypeDscr = '820 XML'                                                                   
When not matched then                                                                                                                                           
insert(PhysicalDocumentTypeCd,PhysicalDocumentTypeNm, PhysicalDocumentTypeDscr)                                                                                 
values ('F820', 'F820', '820 XML');                                                                                                                

Merge INTO PhysicalDocumentFileType                                                                                                                             
using dual ON ( PhysicalDocumentTypeCd = 'RASRY')                                                                                                                
when matched then                                                                                                                                               
update set PhysicalDocumentTypeNm = 'RASRY', PhysicalDocumentTypeDscr = '820 XML'                                                                   
When not matched then                                                                                                                                           
insert(PhysicalDocumentTypeCd,PhysicalDocumentTypeNm, PhysicalDocumentTypeDscr)                                                                                 
values ('RASRY', 'RASRY', '820 Summary Report');                                                                                                                

/* 8/5/2015 - Thomas Fidd, Added per Matt for ECM 4.2. */

Merge INTO PhysicalDocumentFileType                                                                                                                             
using dual ON ( PhysicalDocumentTypeCd = 'I820')                                                                                                                 
when matched then                                                                                                                                               
update set PhysicalDocumentTypeNm = 'I820', PhysicalDocumentTypeDscr = '820 Payment Reporting'                                                                   
When not matched then                                                                                                                                           
insert(PhysicalDocumentTypeCd,PhysicalDocumentTypeNm, PhysicalDocumentTypeDscr)                                                                                 
values ('I820', 'I820', '820 Payment Reporting');                                                                                                                 

/* 8/13/2015 - Thomas Fidd, Added per Anand 8/12/2015. */ 
                                                                                                            
Merge INTO PhysicalDocumentFileType                                                                                                                             
using dual ON ( PhysicalDocumentTypeCd = 'NHGLS')                                                                                                                
when matched then                                                                                                                                               
update set PhysicalDocumentTypeNm = 'NHGLS', PhysicalDocumentTypeDscr = 'Not for HIGLAS'                                                              
When not matched then                                                                                                                                           
insert(PhysicalDocumentTypeCd,PhysicalDocumentTypeNm, PhysicalDocumentTypeDscr)                                                                                 
values ('NHGLS', 'NHGLS', 'Not for HIGLAS'); 

/* 9/22/2015 - Thomas Fidd, Added per email from Sudhakar at 9:56 am. */ 

Merge INTO PhysicalDocumentFileType                                                                                                                             
using dual ON ( PhysicalDocumentTypeCd = 'P824S')                                                                                                                
when matched then                                                                                                                                               
update set PhysicalDocumentTypeNm = 'P824S', PhysicalDocumentTypeDscr = 'Application advice status sent to Hub/Higlas'                                           
When not matched then                                                                                                                                           
insert(PhysicalDocumentTypeCd,PhysicalDocumentTypeNm, PhysicalDocumentTypeDscr)                                                                                 
values ('P824S', 'P824S', 'Application advice status sent to Hub/Higlas');                                                                                        
                                                                                                          
/* 3/29/2016 - Thomas Fidd, Added, see comment block above. */ 

Merge INTO PhysicalDocumentFileType                                                                                                                             
using dual ON ( PhysicalDocumentTypeCd = 'NENR')                                                                                                                
when matched then                                                                                                                                               
update set PhysicalDocumentTypeNm = 'NENR', PhysicalDocumentTypeDscr = 'Not Eligible for Netting Batch output file to CCIIO'                                           
When not matched then                                                                                                                                           
insert(PhysicalDocumentTypeCd,PhysicalDocumentTypeNm, PhysicalDocumentTypeDscr)                                                                                 
values ('NENR', 'NENR', 'Not Eligible for Netting Batch output file to CCIIO');                                                                                        

Merge INTO PhysicalDocumentFileType                                                                                                                             
using dual ON ( PhysicalDocumentTypeCd = 'PAR')                                                                                                                
when matched then                                                                                                                                               
update set PhysicalDocumentTypeNm = 'PAR', PhysicalDocumentTypeDscr = 'Payment Adjustment Batch Output file to CCIIO'                                           
When not matched then                                                                                                                                           
insert(PhysicalDocumentTypeCd,PhysicalDocumentTypeNm, PhysicalDocumentTypeDscr)                                                                                 
values ('PAR', 'PAR', 'Payment Adjustment Batch Output file to CCIIO');                                                                                        

--  Modified 07/06/2016 - Thomas Fidd

Merge INTO PhysicalDocumentFileType                                                                                                                             
using dual ON ( PhysicalDocumentTypeCd = 'S820')                                                                                                                
when matched then                                                                                                                                               
update set PhysicalDocumentTypeNm = 'S820', PhysicalDocumentTypeDscr = 'SBM Preliminary 820'                                           
When not matched then                                                                                                                                           
insert(PhysicalDocumentTypeCd,PhysicalDocumentTypeNm, PhysicalDocumentTypeDscr)                                                                                 
values ('S820', 'S820', 'SBM Preliminary 820');                                                                                        

--  Modified 07/20/2016 - Thomas Fidd per Anand's email Wed 7/20/2016 11:21 AM

Merge INTO PhysicalDocumentFileType                                                                                                                             
using dual ON ( PhysicalDocumentTypeCd = 'SBMS')                                                                                                                
when matched then                                                                                                                                               
update set PhysicalDocumentTypeNm = 'SBMS', PhysicalDocumentTypeDscr = 'Report that goes to states and CCIIO'                                           
When not matched then                                                                                                                                           
insert(PhysicalDocumentTypeCd,PhysicalDocumentTypeNm, PhysicalDocumentTypeDscr)                                                                                 
values ('SBMS', 'SBMS', 'Report that goes to states and CCIIO');                                                                                        

Merge INTO PhysicalDocumentFileType                                                                                                                             
using dual ON ( PhysicalDocumentTypeCd = 'SBMR')                                                                                                                
when matched then                                                                                                                                               
update set PhysicalDocumentTypeNm = 'SBMR', PhysicalDocumentTypeDscr = 'File with summary and details of policies processed'                                           
When not matched then                                                                                                                                           
insert(PhysicalDocumentTypeCd,PhysicalDocumentTypeNm, PhysicalDocumentTypeDscr)                                                                                 
values ('SBMR', 'SBMR', 'File with summary and details of policies processed');                                                                                        

Merge INTO PhysicalDocumentFileType                                                                                                                             
using dual ON ( PhysicalDocumentTypeCd = 'SBMAE')                                                                                                                
when matched then                                                                                                                                               
update set PhysicalDocumentTypeNm = 'SBMAE', PhysicalDocumentTypeDscr = 'Error report sent to CCIIO for errors on approval file'                                           
When not matched then                                                                                                                                           
insert(PhysicalDocumentTypeCd,PhysicalDocumentTypeNm, PhysicalDocumentTypeDscr)                                                                                 
values ('SBMAE', 'SBMAE', 'Error report sent to CCIIO for errors on approval file');                                                                                        

Merge INTO PhysicalDocumentFileType                                                                                                                             
using dual ON ( PhysicalDocumentTypeCd = 'SBMFS')                                                                                                                
when matched then                                                                                                                                               
update set PhysicalDocumentTypeNm = 'SBMFS', PhysicalDocumentTypeDscr = 'SBM file status report sent out to CCIIO'                                           
When not matched then                                                                                                                                           
insert(PhysicalDocumentTypeCd,PhysicalDocumentTypeNm, PhysicalDocumentTypeDscr)                                                                                 
values ('SBMFS', 'SBMFS', 'SBM file status report sent out to CCIIO');                                                                                        

select physicalDocumentTypeCd, physicalDocumentTypeNm from PhysicalDocumentFileType;

commit;

set echo off;
spool off;
