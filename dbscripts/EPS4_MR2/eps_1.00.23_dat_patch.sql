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

insert into ProcessedToDBStatusType values ('I', 'Record will be ignored', 'Record ignored because a more updated version is received', null, null, null);
insert into ProcessedToDBStatusType values ('D', 'Record is soft deleted', 'Record is soft deleted because an updaetd version is received', null, null, null);
insert into TransMsgSkipReasonType values ('EPROD-01', 'Service Access Failure', 'Service unable to access the Public Folder', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into TransMsgSkipReasonType values ('EPROD-02', 'Private Folder Write Incomplete', 'Files did not write to the Private folder completely', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into TransMsgSkipReasonType values ('EPROD-03', 'XML File Invalid', 'File is not a valid XML file', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into TransMsgSkipReasonType values ('EPROD-04', 'File XSD Schema Invalid', 'File is not compliant with the XSD schema', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into TransMsgSkipReasonType values ('EPROD-05', 'Temporary Table Access Failure', 'Unable to access temporary table', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into TransMsgSkipReasonType values ('EPROD-06', 'Transaction Message Table Access Failure', 'Unable to access to Transaction Message table', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into TransMsgSkipReasonType values ('EPROD-07', 'Ref Table - Invalid Error Code', 'Invalid error code in the reference table', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into TransMsgSkipReasonType values ('EPROD-08', 'FFM DB Updater Write Failure-Web', 'FFM Database Updater does not update successfully(Web service Call unsuccessful)', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into TransMsgSkipReasonType values ('EPROD-09', 'FFM DB Updater Write Failure-DB', 'FFM Database Updater does not update successfully(DB Failure)', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into TransMsgSkipReasonType values ('EPROD-10', 'EPS DB Updater Write Failure-DB', 'EPS Database Updater does not update successfully(DB Failure)', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into TransMsgSkipReasonType values ('EPROD-11', 'FFM BAA DB Store Failure', 'FFM BAA store in DB fails', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into TransMsgSkipReasonType values ('EPROD-12', 'EPS BAA DB Store Failure', 'EPS BAA store in DB fails', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into TransMsgSkipReasonType values ('EPROD-13', 'DB Out Of Sync', 'DB out of Sync', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into TransMsgSkipReasonType values ('EPROD-14', 'Web Service Timeout', 'Web Service Timeout', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into TransMsgSkipReasonType values ('EPROD-15', 'Web Service Down', 'Web Service Down', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into TransMsgSkipReasonType values ('EPROD-16', 'BAA Generator Down', 'BAA Generation Down', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into TransMsgSkipReasonType values ('EPROD-17', 'BAA Transmission to HUB Failure', 'BAA fails to transmit XML to HUB', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into TransMsgSkipReasonType values ('EPROD-18', 'Invalid file name', 'Invalid file name', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into TransMsgSkipReasonType values ('EPROD-19', 'HIOS ID does not match existing Policy', 'HIOS ID does not match existing Policy in DB', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into TransMsgSkipReasonType values ('EPROD-20', 'Technical error in EE Web Service', 'Error occurred withing EE web service identified by "500" reponse.', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into TransMsgSkipReasonType values ('EPROD-21', 'EPS DB Reader Failure-DB', 'Failed to retrieve, or select, data from EPS DB', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into TransMsgSkipReasonType values ('EPROD-22', 'Null Pointer Exception', 'A NullPointerException occurred.', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into TransMsgSkipReasonType values ('EPROD-23', 'BRMS Rules Engine Failure', 'Failure in call to business rules engine. ', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into TransMsgSkipReasonType values ('EPROD-24', 'EPS JAXB Marshalling error (EPS data to BEM)', 'Failed to marshall outbound EPS data to XML.', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into TransMsgSkipReasonType values ('EPROD-25', 'EPS DB Reader Failure-DB: Metal level is null, cannot proceed with pre BLE.', 'Missing Reference Data in the InsrncPlan table', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into TransMsgSkipReasonType values ('EPROD-26', 'Read and write counts mismatch', 'Read and write counts mismatch in BemProcessorListener', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into TransMsgSkipReasonType values ('EPROD-27', 'Expected and actual counts mismatch', 'Expected and actual counts mismatch in BemProcessor Listener', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into TransMsgSkipReasonType values ('EPROD-28', 'Failed to clear private directory from ERL Staging area', 'Failed to clear private directory from ERL Staging area in BemProcessor Listener', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into TransMsgSkipReasonType values ('EPROD-29', 'ERL - Previous Version of the Policy was skipped.  Skip the current transaction.', 'ERL - Previous Version of the Policy was skipped.  Skip the current transaction.', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into TransMsgSkipReasonType values ('EPROD-30', 'Missing ExchangePolicyID (Subscriber GroupPolicyNumber)', 'Missing ExchangePolicyID in inbound BEM.', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into TransMsgSkipReasonType values ('EPROD-31', 'Missing StateCode (first 2 chars of subscriber SourceExchangeID)', 'Missing StateCode inbound BEM.', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into TransMsgSkipReasonType values ('EPROD-32', 'Missing HiOS ID (first 5 chars of subscriber ContractCode)', 'Missing HiOS ID in inbound BEM.', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into TransMsgSkipReasonType values ('EPROD-33', 'Missing PolicySnapshotVersionNumber', 'Missing PolicySnapshotVersionNumber in inbound BEM.', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into TransMsgSkipReasonType values ('EPROD-34', 'Policy version found in Skipped status in an earlier processing', 'Policy version Skipped earlier', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into TransMsgSkipReasonType values ('EPROD-35', 'Later versions of the Policy found in EPS', 'Later versions of the Policy found in EPS', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into TransMsgSkipReasonType values ('EPROD-99', 'General Exception', 'Error has not been classified', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);

commit;
spool off
