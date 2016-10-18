

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

ALTER TABLE SBMFileInfo
DROP CONSTRAINT R_392;

ALTER TABLE SBMFileError
DROP CONSTRAINT R_384;

ALTER TABLE SBMTransMsg
DROP CONSTRAINT R_388;

ALTER TABLE SBMResponse
DROP CONSTRAINT R_390;

ALTER TABLE SBMFileArchive
DROP CONSTRAINT R_402;

ALTER TABLE StagingSBMFile
DROP CONSTRAINT R_425;

ALTER TABLE StagingSBMPolicy
DROP CONSTRAINT R_428;

ALTER TABLE SBMTransMsg
DROP CONSTRAINT R_410;

ALTER TABLE SBMTransMsg
DROP CONSTRAINT R_411;

ALTER TABLE SBMTransMsgValidation
DROP CONSTRAINT R_389;

ALTER TABLE StagingPolicyVersion
DROP CONSTRAINT R_396;

ALTER TABLE PolicyVersion
DROP CONSTRAINT R_418;

ALTER TABLE policyMemberVersion
DROP CONSTRAINT R_427;

ALTER TABLE StagingSBMFile
DROP CONSTRAINT R_424;

ALTER TABLE StagingSBMPolicy
DROP CONSTRAINT R_423;

ALTER TABLE SBMTransMsgAdditionalErrorInfo
DROP CONSTRAINT R_417;

ALTER TABLE SBMFileInfo
DROP PRIMARY KEY CASCADE  DROP INDEX;

ALTER TABLE SBMFileArchive
DROP PRIMARY KEY CASCADE  DROP INDEX;

ALTER TABLE SBMTransMsg
DROP PRIMARY KEY CASCADE  DROP INDEX;

ALTER TABLE StagingSBMFile
DROP PRIMARY KEY CASCADE  DROP INDEX;

ALTER TABLE StagingSBMPolicy
DROP PRIMARY KEY CASCADE  DROP INDEX;

ALTER TABLE SBMTransMsgValidation
DROP PRIMARY KEY CASCADE  DROP INDEX;

ALTER TABLE SBMFileInfo RENAME TO SBMFileInfo0ACC0955005;

ALTER TABLE SBMFileArchive RENAME TO SBMFileArchive0ACC0955009;

ALTER TABLE SBMTransMsg RENAME TO SBMTransMsg0ACC0955007;

ALTER TABLE StagingSBMFile RENAME TO StagingSBMFile0ACC0955006;

ALTER TABLE StagingSBMPolicy RENAME TO StagingSBMPolicy0ACC0955008;

CREATE TABLE SBMFileInfo
(
	SBMFileInfoId        NUMBER NOT NULL ,
	SBMFileNm            VARCHAR2(500) NULL ,
	SBMFileId            VARCHAR2(50) NULL ,
	SBMFileCreateDateTime TIMESTAMP NULL ,
	SBMFileNum           NUMBER(4,0) NULL ,
	SBMFileProcessingSummaryId NUMBER NOT NULL ,
	TradingPartnerId     VARCHAR2(50) NULL ,
	functionCd           VARCHAR2(50) NULL ,
	createDateTime       TIMESTAMP DEFAULT  systimestamp  NULL ,
	createBy             VARCHAR2(50) NULL ,
	lastModifiedDateTime TIMESTAMP DEFAULT  systimestamp  NULL ,
	lastModifiedBy       VARCHAR2(50) NULL ,
	FileInfoXML          XMLType NULL ,
	rejectedInd          VARCHAR2(1) DEFAULT  'N'  NOT NULL ,
	sbmFileLastModifiedDateTime TIMESTAMP NULL 
)
	TABLESPACE FM_DATA_TRANS
	INITRANS 20
	XMLTYPE COLUMN FileInfoXML STORE AS BASICFILE CLOB (TABLESPACE FM_DATA_LOB );

CREATE UNIQUE INDEX XPKSBMFileInfo ON SBMFileInfo
(SBMFileInfoId   ASC);

CREATE TABLE SBMFileArchive
(
	SBMFileInfoId        NUMBER NOT NULL ,
	SBMXML               XMLType NULL ,
	SBMFileCreateDateTime TIMESTAMP NULL ,
	createDateTime       TIMESTAMP DEFAULT  systimestamp  NULL ,
	createBy             VARCHAR2(50) NULL ,
	lastModifiedDateTime TIMESTAMP DEFAULT  systimestamp  NULL ,
	lastModifiedBy       VARCHAR2(50) NULL ,
	SBMFileNm            VARCHAR2(500) NULL ,
	SBMFileId            VARCHAR2(50) NULL ,
	SBMFileNum           NUMBER(4,0) NULL ,
	TradingPartnerId     VARCHAR2(50) NULL ,
	tenantNum            VARCHAR2(1) NULL ,
	coverageYear         VARCHAR2(50) NULL ,
	issuerFileSetId      VARCHAR2(10) NULL ,
	issuerId             VARCHAR2(5) NULL ,
	subscriberStateCd    VARCHAR2(2) NULL 
)
	TABLESPACE FM_DATA_TRANS
	INITRANS 20
	XMLTYPE COLUMN SBMXML STORE AS SECUREFILE BINARY  XML(TABLESPACE FM_DATA_LOB )
	PARTITION BY LIST (subscriberStateCd)	
	SUBPARTITION BY RANGE (createDateTime)
	SUBPARTITION TEMPLATE(
	SUBPARTITION H12016 VALUES LESS THAN (to_date('07/01/2016', 'mm/dd/yyyy')),
	SUBPARTITION H22016 VALUES LESS THAN (TO_DATE('01/01/2017', 'MM/DD/YYYY')),
	SUBPARTITION H12017 VALUES LESS THAN (to_date('07/01/2017', 'mm/dd/yyyy')),
	SUBPARTITION H22017 VALUES LESS THAN (TO_DATE('01/01/2018', 'MM/DD/YYYY')),
	SUBPARTITION H12018 VALUES LESS THAN (to_date('07/01/2018', 'mm/dd/yyyy')),
	SUBPARTITION H22018 VALUES LESS THAN (to_date('01/01/2019', 'mm/dd/yyyy')),
	SUBPARTITION H12019 VALUES LESS THAN (to_date('07/01/2019', 'mm/dd/yyyy')),
	SUBPARTITION H22019 VALUES LESS THAN (to_date('01/01/2020', 'mm/dd/yyyy')),
	SUBPARTITION defaultPart VALUES LESS THAN (MAXVALUE)
	)(
	PARTITION STCDCA VALUES ('CA'),
	PARTITION STCDCO VALUES ('CO'),
	PARTITION STCDCT VALUES ('CT'),
	PARTITION STCDDC VALUES ('DC'),
	PARTITION STCDID VALUES ('ID'),
	PARTITION STCDKY VALUES ('KY'),
	PARTITION STCDMA VALUES ('MA'),
	PARTITION STCDMD VALUES ('MD'),
	PARTITION STCDMN VALUES ('MN'),
	PARTITION STCDNY VALUES ('NY'),
	PARTITION STCDRI VALUES ('RI'),
	PARTITION STCDVT VALUES ('VT'),
	PARTITION STCDWA VALUES ('WA'),
	PARTITION STCDDEFAULT VALUES (default))
	COMPRESS ;


CREATE UNIQUE INDEX XPKSBMFileArchive ON SBMFileArchive
(SBMFileInfoId   ASC);

CREATE TABLE SBMTransMsg
(
	SBMTransMsgID        NUMBER NOT NULL ,
	TransMsgDateTime     TIMESTAMP NULL ,
	Msg                  XMLType NOT NULL ,
	TransMsgDirectionTypeCd VARCHAR2(25) NULL ,
	TransMsgTypeCd       VARCHAR2(25) NULL ,
	createDateTime       TIMESTAMP DEFAULT  systimestamp  NOT NULL ,
	lastModifiedDateTime TIMESTAMP DEFAULT  systimestamp  NOT NULL ,
	createBy             VARCHAR2(50) NULL ,
	lastModifiedBy       VARCHAR2(50) NULL ,
	subscriberStateCd    VARCHAR2(2) NULL ,
	SBMFileInfoId        NUMBER NOT NULL ,
	recordControlNum     NUMBER(9,0) NULL ,
	planId               VARCHAR2(14) NULL ,
	SBMTransMsgProcStatusTypeCd VARCHAR2(25) NULL ,
	exchangeAssignedPolicyId VARCHAR2(50) NULL ,
	exchangeAssignedSubscriberId VARCHAR2(50) NULL 
)
	TABLESPACE FM_DATA_TRANS
	INITRANS 20
	XMLTYPE COLUMN MSG STORE AS BASICFILE CLOB (TABLESPACE FM_DATA_LOB )
	PARTITION BY LIST (subscriberStateCd)
	SUBPARTITION BY RANGE (TransMsgDateTime)
	SUBPARTITION TEMPLATE(
	SUBPARTITION Y2015 VALUES LESS THAN (to_date('01/01/2016', 'mm/dd/yyyy')),
	SUBPARTITION Y2016 VALUES LESS THAN (to_date('01/01/2017', 'mm/dd/yyyy')),
	SUBPARTITION Y2017 VALUES LESS THAN (to_date('01/01/2018', 'mm/dd/yyyy')),
	SUBPARTITION Y2018 VALUES LESS THAN (to_date('01/01/2019', 'mm/dd/yyyy')),
	SUBPARTITION Y2019 VALUES LESS THAN (to_date('01/01/2020', 'mm/dd/yyyy')),
	SUBPARTITION DEFAULTYEAR VALUES LESS THAN (MAXVALUE)
	)(
	PARTITION STCDCA VALUES ('CA'),
	PARTITION STCDCO VALUES ('CO'),
	PARTITION STCDCT VALUES ('CT'),
	PARTITION STCDDC VALUES ('DC'),
	PARTITION STCDID VALUES ('ID'),
	PARTITION STCDKY VALUES ('KY'),
	PARTITION STCDMA VALUES ('MA'),
	PARTITION STCDMD VALUES ('MD'),
	PARTITION STCDMN VALUES ('MN'),
	PARTITION STCDNY VALUES ('NY'),
	PARTITION STCDRI VALUES ('RI'),
	PARTITION STCDVT VALUES ('VT'),
	PARTITION STCDWA VALUES ('WA'),
	PARTITION STCDDEFAULT VALUES (default));

CREATE UNIQUE INDEX XPKSBMTransMsg ON SBMTransMsg
(SBMTransMsgID   ASC);

CREATE TABLE StagingSBMFile
(
	SBMXML               XMLType NULL ,
	batchId              VARCHAR2(50) NULL ,
	SBMFileProcessingSummaryId NUMBER NOT NULL ,
	SBMFileInfoId        NUMBER NOT NULL ,
	createDateTime       TIMESTAMP DEFAULT  systimestamp  NULL ,
	createBy             VARCHAR2(50) NULL ,
	lastModifiedDateTime TIMESTAMP DEFAULT  systimestamp  NULL ,
	lastModifiedBy       VARCHAR2(50) NULL 
)
	TABLESPACE FM_DATA_TRANS
	INITRANS 20
	XMLTYPE COLUMN SBMXML STORE AS SECUREFILE BINARY  XML(TABLESPACE FM_DATA_LOB );

CREATE UNIQUE INDEX XPKStagingSBMFile ON StagingSBMFile
(SBMFileInfoId   ASC);

CREATE TABLE StagingSBMPolicy
(
	SBMPolicyXML         XMLType NULL ,
	ProcessingGroupId    NUMBER(10,0) NULL ,
	SBMFileProcessingSummaryId NUMBER NOT NULL ,
	stagingSBMPolicyId   NUMBER NOT NULL ,
	SBMFileInfoId        NUMBER NULL ,
	createDateTime       TIMESTAMP DEFAULT  systimestamp  NULL ,
	createBy             VARCHAR2(50) NULL ,
	lastModifiedDateTime TIMESTAMP DEFAULT  systimestamp  NULL ,
	lastModifiedBy       VARCHAR2(50) NULL ,
	subscriberStateCd    VARCHAR2(2) NULL 
)
	TABLESPACE FM_DATA_TRANS
	INITRANS 20
	XMLTYPE COLUMN SBMPolicyXML STORE AS BASICFILE CLOB (TABLESPACE FM_DATA_LOB );

CREATE UNIQUE INDEX XPKStagingSBMPolicy ON StagingSBMPolicy
(stagingSBMPolicyId   ASC);

CREATE UNIQUE INDEX XPKSBMTransMsgValida ON SBMTransMsgValidation
(SBMTransMsgID   ASC,validationSequenceNum   ASC);

ALTER TABLE SBMFileInfo
ADD CONSTRAINT  XPKSBMFileInfo PRIMARY KEY (SBMFileInfoId);

INSERT INTO SBMFileInfo (SBMFileInfoId, SBMFileProcessingSummaryId, SBMFileNm, SBMFileCreateDateTime, SBMFileId, SBMFileNum, TradingPartnerId, functionCd, FileInfoXML, createDateTime, createBy, lastModifiedDateTime, lastModifiedBy, rejectedInd, sbmFileLastModifiedDateTime) SELECT SBMFileInfoId, SBMFileProcessingSummaryId, SBMFileNm, SBMFileCreateDateTime, SBMFileId, SBMFileNum, TradingPartnerId, functionCd, FileInfoXML, createDateTime, createBy, lastModifiedDateTime, lastModifiedBy, rejectedInd, sbmFileLastModifiedDateTime FROM SBMFileInfo0ACC0955005;

ALTER TABLE SBMFileArchive
ADD CONSTRAINT  XPKSBMFileArchive PRIMARY KEY (SBMFileInfoId);

INSERT INTO SBMFileArchive (SBMFileInfoId, subscriberStateCd, SBMFileNm, SBMFileCreateDateTime, SBMFileId, SBMFileNum, TradingPartnerId, tenantNum, coverageYear, issuerFileSetId, issuerId, SBMXML, createDateTime, createBy, lastModifiedDateTime, lastModifiedBy) SELECT SBMFileInfoId, subscriberStateCd, SBMFileNm, SBMFileCreateDateTime, SBMFileId, SBMFileNum, TradingPartnerId, tenantNum, coverageYear, issuerFileSetId, issuerId, SBMXML, createDateTime, createBy, lastModifiedDateTime, lastModifiedBy FROM SBMFileArchive0ACC0955009;

ALTER TABLE SBMTransMsg
ADD CONSTRAINT  XPKSBMTransMsg PRIMARY KEY (SBMTransMsgID);

INSERT INTO SBMTransMsg (SBMTransMsgID, SBMFileInfoId, recordControlNum, TransMsgDateTime, Msg, TransMsgDirectionTypeCd, TransMsgTypeCd, exchangeAssignedPolicyId, subscriberStateCd, planId, SBMTransMsgProcStatusTypeCd, createDateTime, createBy, lastModifiedDateTime, lastModifiedBy, exchangeAssignedSubscriberId) SELECT SBMTransMsgID, SBMFileInfoId, recordControlNum, TransMsgDateTime, Msg, TransMsgDirectionTypeCd, TransMsgTypeCd, exchangeAssignedPolicyId, subscriberStateCd, planId, SBMTransMsgProcStatusTypeCd, createDateTime, createBy, lastModifiedDateTime, lastModifiedBy, exchangeAssignedSubscriberId FROM SBMTransMsg0ACC0955007;

ALTER TABLE StagingSBMFile
ADD CONSTRAINT  XPKStagingSBMFile PRIMARY KEY (SBMFileInfoId);

INSERT INTO StagingSBMFile (SBMFileInfoId, SBMFileProcessingSummaryId, SBMXML, batchId, createDateTime, createBy, lastModifiedDateTime, lastModifiedBy) SELECT SBMFileInfoId, SBMFileProcessingSummaryId, SBMXML, batchId, createDateTime, createBy, lastModifiedDateTime, lastModifiedBy FROM StagingSBMFile0ACC0955006;

ALTER TABLE StagingSBMPolicy
ADD CONSTRAINT  XPKStagingSBMPolicy PRIMARY KEY (stagingSBMPolicyId);

INSERT INTO StagingSBMPolicy (stagingSBMPolicyId, SBMFileProcessingSummaryId, ProcessingGroupId, SBMPolicyXML, SBMFileInfoId, createDateTime, createBy, lastModifiedDateTime, lastModifiedBy, subscriberStateCd) SELECT stagingSBMPolicyId, SBMFileProcessingSummaryId, ProcessingGroupId, SBMPolicyXML, SBMFileInfoId, createDateTime, createBy, lastModifiedDateTime, lastModifiedBy, subscriberStateCd FROM StagingSBMPolicy0ACC0955008;

ALTER TABLE SBMTransMsgValidation
ADD CONSTRAINT  XPKSBMTransMsgValida PRIMARY KEY (SBMTransMsgID,validationSequenceNum);

ALTER TABLE SBMFileInfo
ADD CONSTRAINT R_392 FOREIGN KEY (SBMFileProcessingSummaryId) REFERENCES SBMFileProcessingSummary (SBMFileProcessingSummaryId);

ALTER TABLE SBMFileError
ADD CONSTRAINT R_384 FOREIGN KEY (SBMFileInfoId) REFERENCES SBMFileInfo (SBMFileInfoId);

ALTER TABLE SBMTransMsg
ADD CONSTRAINT R_388 FOREIGN KEY (SBMFileInfoId) REFERENCES SBMFileInfo (SBMFileInfoId);

ALTER TABLE SBMResponse
ADD CONSTRAINT R_390 FOREIGN KEY (SBMFileInfoId) REFERENCES SBMFileInfo (SBMFileInfoId);

ALTER TABLE SBMFileArchive
ADD CONSTRAINT R_402 FOREIGN KEY (SBMFileInfoId) REFERENCES SBMFileInfo (SBMFileInfoId);

ALTER TABLE StagingSBMFile
ADD CONSTRAINT R_425 FOREIGN KEY (SBMFileInfoId) REFERENCES SBMFileInfo (SBMFileInfoId);

ALTER TABLE StagingSBMPolicy
ADD CONSTRAINT R_428 FOREIGN KEY (SBMFileInfoId) REFERENCES SBMFileInfo (SBMFileInfoId);

ALTER TABLE SBMTransMsg
ADD CONSTRAINT R_410 FOREIGN KEY (TransMsgTypeCd) REFERENCES TransMsgType (TransMsgTypeCd);

ALTER TABLE SBMTransMsg
ADD CONSTRAINT R_411 FOREIGN KEY (SBMTransMsgProcStatusTypeCd) REFERENCES SBMTransMsgProcStatusType (SBMTransMsgProcStatusTypeCd);

ALTER TABLE SBMTransMsgValidation
ADD CONSTRAINT R_389 FOREIGN KEY (SBMTransMsgID) REFERENCES SBMTransMsg (SBMTransMsgID);

ALTER TABLE StagingPolicyVersion
ADD CONSTRAINT R_396 FOREIGN KEY (SBMTransMsgID) REFERENCES SBMTransMsg (SBMTransMsgID);

ALTER TABLE PolicyVersion
ADD CONSTRAINT R_418 FOREIGN KEY (SBMTransMsgID) REFERENCES SBMTransMsg (SBMTransMsgID);

ALTER TABLE policyMemberVersion
ADD CONSTRAINT R_427 FOREIGN KEY (SBMTransMsgID) REFERENCES SBMTransMsg (SBMTransMsgID);

ALTER TABLE StagingSBMFile
ADD CONSTRAINT R_424 FOREIGN KEY (SBMFileProcessingSummaryId) REFERENCES SBMFileProcessingSummary (SBMFileProcessingSummaryId);

ALTER TABLE SBMTransMsgAdditionalErrorInfo
ADD CONSTRAINT R_417 FOREIGN KEY (SBMTransMsgID, validationSequenceNum) REFERENCES SBMTransMsgValidation (SBMTransMsgID, validationSequenceNum);

DROP TABLE SBMFileInfo0ACC0955005 CASCADE CONSTRAINTS PURGE;

DROP TABLE SBMFileArchive0ACC0955009 CASCADE CONSTRAINTS PURGE;

DROP TABLE SBMTransMsg0ACC0955007 CASCADE CONSTRAINTS PURGE;

DROP TABLE StagingSBMFile0ACC0955006 CASCADE CONSTRAINTS PURGE;

DROP TABLE StagingSBMPolicy0ACC0955008 CASCADE CONSTRAINTS PURGE;

spool off
