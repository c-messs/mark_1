

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

ALTER TABLE SBMTransMsgValidation
DROP CONSTRAINT R_389;

ALTER TABLE SBMTransMsgValidation
DROP CONSTRAINT R_415;

ALTER TABLE SBMTransMsgAdditionalErrorInfo
DROP CONSTRAINT R_417;

ALTER TABLE SBMTransMsgValidation
DROP PRIMARY KEY CASCADE  DROP INDEX;

ALTER TABLE SBMTransMsgValidation RENAME TO sbmTransLob2;

create table SBMTransMsgValidat0BFC5746001 as (
SELECT SBMTransMsgID, validationSequenceNum, elementInErrorNm, SBMErrorWarningTypeCd, createDateTime, createBy, to_lob(lastModifiedDateTime) lobby, lastModifiedBy,
       exchangeAssignedMemberId FROM sbmTransLob2);


CREATE TABLE SBMTransMsgValidation
(
	SBMTransMsgID        NUMBER NOT NULL ,
	elementInErrorNm     VARCHAR2(255) NULL ,
	validationSequenceNum NUMBER NOT NULL ,
	SBMErrorWarningTypeCd VARCHAR2(25) NULL ,
	createDateTime       TIMESTAMP DEFAULT  systimestamp  NULL ,
	createBy             VARCHAR2(50) NULL ,
	lastModifiedDateTime TIMESTAMP DEFAULT  systimestamp  NULL ,
	lastModifiedBy       VARCHAR2(50) NULL ,
	exchangeAssignedMemberId VARCHAR2(50) NULL 
)
	TABLESPACE FM_DATA_TRANS
	INITRANS 20;

CREATE UNIQUE INDEX XPKSBMTransactionMessageValida ON SBMTransMsgValidation
(SBMTransMsgID   ASC,validationSequenceNum   ASC) INITRANS 20 TABLESPACE FM_INDEX_TRANS;


ALTER TABLE SBMTransMsgValidation
ADD CONSTRAINT  XPKSBMTransactionMessageValida PRIMARY KEY (SBMTransMsgID,validationSequenceNum);

INSERT INTO SBMTransMsgValidation (SBMTransMsgID, validationSequenceNum, elementInErrorNm, SBMErrorWarningTypeCd, createDateTime, createBy, lastModifiedDateTime,
                                   lastModifiedBy, exchangeAssignedMemberId) 
SELECT SBMTransMsgID, validationSequenceNum, elementInErrorNm, SBMErrorWarningTypeCd, createDateTime, createBy, to_timestamp(dbms_lob.substr(lobby ,28), 'dd-mon-yy HH.MI.SSXFF AM') , lastModifiedBy,
       exchangeAssignedMemberId FROM SBMTransMsgValidat0BFC5746001;

ALTER TABLE SBMTransMsgValidation
ADD CONSTRAINT R_389 FOREIGN KEY (SBMTransMsgID) REFERENCES SBMTransMsg (SBMTransMsgID);

ALTER TABLE SBMTransMsgValidation
ADD CONSTRAINT R_415 FOREIGN KEY (SBMErrorWarningTypeCd) REFERENCES SBMErrorWarningType (SBMErrorWarningTypeCd);

ALTER TABLE SBMTransMsgAdditionalErrorInfo
ADD CONSTRAINT R_417 FOREIGN KEY (SBMTransMsgID, validationSequenceNum) REFERENCES SBMTransMsgValidation (SBMTransMsgID, validationSequenceNum);

DROP TABLE SBMTransMsgValidat0BFC5746001 CASCADE CONSTRAINTS PURGE;

DROP TABLE sbmTransLob2 CASCADE CONSTRAINTS PURGE;

spool off
