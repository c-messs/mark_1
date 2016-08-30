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

MERGE INTO SbmFileStatusType using dual ON ( SbmFileStatusTypeCd = 'RJC') when matched then update set SbmFileStatusTypeNm='Rejected' ,SbmFileStatusTypeDesc = 'Covers the scenario for Schema Reject, Invalid File, Duplicate Reject, FileSet Reject, Threshold Reject'  When not matched then insert(SbmFileStatusTypeCd,SbmFileStatusTypeNm ,  SbmFileStatusTypeDesc) values ('RJC','Rejected','Covers the scenario for Schema Reject, Invalid File, Duplicate Reject, FileSet Reject, Threshold Reject');
MERGE INTO SbmFileStatusType using dual ON ( SbmFileStatusTypeCd = 'ACC') when matched then update set SbmFileStatusTypeNm='Accepted' ,SbmFileStatusTypeDesc = 'Submitted file is processed without any errors or warnings. This status indicates that submitted data is awaiting CMS review and approval.'  When not matched then insert(SbmFileStatusTypeCd,SbmFileStatusTypeNm ,  SbmFileStatusTypeDesc) values ('ACC','Accepted','Submitted file is processed without any errors or warnings. This status indicates that submitted data is awaiting CMS review and approval.');
MERGE INTO SbmFileStatusType using dual ON ( SbmFileStatusTypeCd = 'ACE') when matched then update set SbmFileStatusTypeNm='Accepted with errors' ,SbmFileStatusTypeDesc = 'Submitted file is processed with errors. This includes files processed with errors and warnings (if applicable). This status indicates that submitted data is awaiting CMS review and approval.'  When not matched then insert(SbmFileStatusTypeCd,SbmFileStatusTypeNm ,  SbmFileStatusTypeDesc) values ('ACE','Accepted with errors','Submitted file is processed with errors. This includes files processed with errors and warnings (if applicable). This status indicates that submitted data is awaiting CMS review and approval.');
MERGE INTO SbmFileStatusType using dual ON ( SbmFileStatusTypeCd = 'ACW') when matched then update set SbmFileStatusTypeNm='Accepted with warnings' ,SbmFileStatusTypeDesc = 'Submitted file is processed with warnings only. This status indicates that submitted data is awaiting CMS review and approval.'  When not matched then insert(SbmFileStatusTypeCd,SbmFileStatusTypeNm ,  SbmFileStatusTypeDesc) values ('ACW','Accepted with warnings','Submitted file is processed with warnings only. This status indicates that submitted data is awaiting CMS review and approval.');
MERGE INTO SbmFileStatusType using dual ON ( SbmFileStatusTypeCd = 'APP') when matched then update set SbmFileStatusTypeNm='Approved' ,SbmFileStatusTypeDesc = 'CMS approved the submission and data is loaded for payment processing. No errors or warnings were found.'  When not matched then insert(SbmFileStatusTypeCd,SbmFileStatusTypeNm ,  SbmFileStatusTypeDesc) values ('APP','Approved','CMS approved the submission and data is loaded for payment processing. No errors or warnings were found.');
MERGE INTO SbmFileStatusType using dual ON ( SbmFileStatusTypeCd = 'APE') when matched then update set SbmFileStatusTypeNm='Approved with errors' ,SbmFileStatusTypeDesc = 'CMS approved the submission and data is loaded for payment processing. There were record level errors and warnings (if applicable) that should be corrected in a future SBMI submission, the records with errors were not loaded to EPS.'  When not matched then insert(SbmFileStatusTypeCd,SbmFileStatusTypeNm ,  SbmFileStatusTypeDesc) values ('APE','Approved with errors','CMS approved the submission and data is loaded for payment processing. There were record level errors and warnings (if applicable) that should be corrected in a future SBMI submission, the records with errors were not loaded to EPS.');
MERGE INTO SbmFileStatusType using dual ON ( SbmFileStatusTypeCd = 'APW') when matched then update set SbmFileStatusTypeNm='Approved with warnings' ,SbmFileStatusTypeDesc = 'CMS approved the submission and data is loaded for payment processing. There were record level warnings only that should be corrected in the future, the records with warnings were loaded to EPS.'  When not matched then insert(SbmFileStatusTypeCd,SbmFileStatusTypeNm ,  SbmFileStatusTypeDesc) values ('APW','Approved with warnings','CMS approved the submission and data is loaded for payment processing. There were record level warnings only that should be corrected in the future, the records with warnings were loaded to EPS.');
MERGE INTO SbmFileStatusType using dual ON ( SbmFileStatusTypeCd = 'DSP') when matched then update set SbmFileStatusTypeNm='Disapproved' ,SbmFileStatusTypeDesc = 'CMS disapproved the file submission. An outreach team will contact you.'  When not matched then insert(SbmFileStatusTypeCd,SbmFileStatusTypeNm ,  SbmFileStatusTypeDesc) values ('DSP','Disapproved','CMS disapproved the file submission. An outreach team will contact you.');
MERGE INTO SbmFileStatusType using dual ON ( SbmFileStatusTypeCd = 'IPC') when matched then update set SbmFileStatusTypeNm='In Process' ,SbmFileStatusTypeDesc = 'Submitted file is currently being processed.'  When not matched then insert(SbmFileStatusTypeCd,SbmFileStatusTypeNm ,  SbmFileStatusTypeDesc) values ('IPC','In Process','Submitted file is currently being processed.');
MERGE INTO SbmFileStatusType using dual ON ( SbmFileStatusTypeCd = 'OHD') when matched then update set SbmFileStatusTypeNm='On Hold' ,SbmFileStatusTypeDesc = 'There is an existing file for the SBM or Issuer that is pending CMS approval or in process.'  When not matched then insert(SbmFileStatusTypeCd,SbmFileStatusTypeNm ,  SbmFileStatusTypeDesc) values ('OHD','On Hold','There is an existing file for the SBM or Issuer that is pending CMS approval or in process.');
MERGE INTO SbmFileStatusType using dual ON ( SbmFileStatusTypeCd = 'FRZ') when matched then update set SbmFileStatusTypeNm='Freeze' ,SbmFileStatusTypeDesc = 'File was submitted during freeze period and cannot be processed beyond file-level validation.'  When not matched then insert(SbmFileStatusTypeCd,SbmFileStatusTypeNm ,  SbmFileStatusTypeDesc) values ('FRZ','Freeze','File was submitted during freeze period and cannot be processed beyond file-level validation.');
MERGE INTO SbmFileStatusType using dual ON ( SbmFileStatusTypeCd = 'BPF') when matched then update set SbmFileStatusTypeNm='Bypass Freeze' ,SbmFileStatusTypeDesc = 'Files submitted during freeze period that were identified to resume processing during freeze period.'  When not matched then insert(SbmFileStatusTypeCd,SbmFileStatusTypeNm ,  SbmFileStatusTypeDesc) values ('BPF','Bypass Freeze','Files submitted during freeze period that were identified to resume processing during freeze period.');
MERGE INTO SbmFileStatusType using dual ON ( SbmFileStatusTypeCd = 'PDF') when matched then update set SbmFileStatusTypeNm='Pending Files' ,SbmFileStatusTypeDesc = 'File submitted was part of a fileset that is not complete.'  When not matched then insert(SbmFileStatusTypeCd,SbmFileStatusTypeNm ,  SbmFileStatusTypeDesc) values ('PDF','Pending Files','File submitted was part of a fileset that is not complete.');
MERGE INTO SbmFileStatusType using dual ON ( SbmFileStatusTypeCd = 'BKO') when matched then update set SbmFileStatusTypeNm='Backout' ,SbmFileStatusTypeDesc = 'File successfully reversed.'  When not matched then insert(SbmFileStatusTypeCd,SbmFileStatusTypeNm ,  SbmFileStatusTypeDesc) values ('BKO','Backout','File successfully reversed.');

commit;

spool off
