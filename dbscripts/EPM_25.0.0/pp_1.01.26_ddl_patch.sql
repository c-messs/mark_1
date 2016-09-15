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



ALTER TABLE TransMsgFileInfo
DROP CONSTRAINT R_270;

ALTER TABLE TransMsg
DROP CONSTRAINT R_81;

ALTER TABLE errorWarningLog
DROP CONSTRAINT R_177;

ALTER TABLE TransMsgFileInfo
DROP PRIMARY KEY CASCADE  DROP INDEX;

ALTER TABLE TransMsgFileInfo RENAME TO TransMsgFileInfo_062916;

CREATE TABLE TransMsgFileInfo
(
                TransMsgFileInfoId   NUMBER NOT NULL ,
                FileInfoXML          XMLType NOT NULL ,
                groupSenderId        VARCHAR2(15) NULL ,
                groupReceiverId      VARCHAR2(15) NULL ,
                fileNm               VARCHAR2(4000) NULL ,
                interchangeSenderId  VARCHAR2(15) NULL ,
                interchangeReceiverId VARCHAR2(15) NULL ,
                interchangeControlNum VARCHAR2(9) NULL ,
                groupTimestampDateTime TIMESTAMP NULL ,
                groupControlNum      VARCHAR2(9) NULL ,
                versionNum           VARCHAR2(50) NULL ,
                createDateTime       DATE DEFAULT  sysdate  NOT NULL ,
                lastModifiedDateTime DATE DEFAULT  sysdate  NOT NULL ,
                createBy             VARCHAR2(50) NULL ,
                lastModifiedBy       VARCHAR2(50) NULL ,
                TransMsgOriginTypeCd VARCHAR2(25) NULL 
)
  TABLESPACE FM_DATA_TRANS 
 XMLTYPE COLUMN FILEINFOXML STORE AS CLOB (TABLESPACE FM_DATA_LOB)  ;

CREATE UNIQUE INDEX XPKTransactionMessageFileInfo ON TransMsgFileInfo
(TransMsgFileInfoId   ASC)
	INITRANS 20
	TABLESPACE FM_INDEX_TRANS;
	
DROP INDEX XIF1TRANSACTIONMESSAGEFILEINFO;

CREATE INDEX XIF1TRANSACTIONMESSAGEFILEINFO ON "TRANSMSGFILEINFO" ("TRANSMSGORIGINTYPECD") 
	INITRANS 20
	TABLESPACE FM_INDEX_TRANS;

ALTER TABLE TransMsgFileInfo
ADD CONSTRAINT  XPKTransactionMessageFileInfo PRIMARY KEY (TransMsgFileInfoId);

INSERT INTO TransMsgFileInfo (TransMsgFileInfoId, FileInfoXML, groupSenderId, groupReceiverId, groupTimestampDateTime, groupControlNum, interchangeSenderId, interchangeReceiverId, interchangeControlNum, fileNm, versionNum, createDateTime, lastModifiedDateTime, createBy, lastModifiedBy, TransMsgOriginTypeCd) 
SELECT TransMsgFileInfoId, FileInfoXML, groupSenderId, groupReceiverId, groupTimestampDateTime, groupControlNum, interchangeSenderId, interchangeReceiverId, interchangeControlNum, fileNm, versionNum, createDateTime, lastModifiedDateTime, createBy, lastModifiedBy, TransMsgOriginTypeCd FROM TransMsgFileInfo_062916;

ALTER TABLE TransMsgFileInfo
ADD CONSTRAINT R_270 FOREIGN KEY (TransMsgOriginTypeCd) REFERENCES TransMsgOriginType (TransMsgOriginTypeCd) ON DELETE SET NULL;

ALTER TABLE TransMsg
ADD CONSTRAINT R_81 FOREIGN KEY (TransMsgFileInfoId) REFERENCES TransMsgFileInfo (TransMsgFileInfoId);

ALTER TABLE errorWarningLog
ADD CONSTRAINT R_177 FOREIGN KEY (TransMsgFileInfoId) REFERENCES TransMsgFileInfo (TransMsgFileInfoId);

spool off
