

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

ALTER TABLE SBMResponse
DROP CONSTRAINT R_390;

ALTER TABLE SBMResponse
DROP CONSTRAINT R_391;

ALTER TABLE SBMResponse
DROP CONSTRAINT R_393;

ALTER TABLE SBMResponse
DROP PRIMARY KEY CASCADE  DROP INDEX;

ALTER TABLE SBMResponse RENAME TO SBMResponse078F4528004;


ALTER TABLE SBMFileProcessingSummary MODIFY(tenantId VARCHAR2(3) NULL);

ALTER TABLE StagingSBMFile
	ADD (createDateTime TIMESTAMP  DEFAULT systimestamp);

ALTER TABLE StagingSBMFile
	ADD (createBy VARCHAR2(50));

ALTER TABLE StagingSBMFile
	ADD (lastModifiedDateTime TIMESTAMP  DEFAULT systimestamp);

ALTER TABLE StagingSBMFile
	ADD (lastModifiedBy VARCHAR2(50));

ALTER TABLE StagingSBMPolicy
	ADD (createDateTime TIMESTAMP  DEFAULT systimestamp);

ALTER TABLE StagingSBMPolicy
	ADD (createBy VARCHAR2(50));

ALTER TABLE StagingSBMPolicy
	ADD (lastModifiedDateTime TIMESTAMP  DEFAULT systimestamp);

ALTER TABLE StagingSBMPolicy
	ADD (lastModifiedBy VARCHAR2(50));

ALTER TABLE StagingSBMPolicy
	ADD (subscriberStateCd VARCHAR2(2));

ALTER TABLE StagingSBMGroupLock
	ADD (createDateTime TIMESTAMP  DEFAULT systimestamp);

ALTER TABLE StagingSBMGroupLock
	ADD (createBy VARCHAR2(50));

ALTER TABLE StagingSBMGroupLock
	ADD (lastModifiedDateTime TIMESTAMP  DEFAULT systimestamp);

ALTER TABLE StagingSBMGroupLock
	ADD (lastModifiedBy VARCHAR2(50));

CREATE TABLE SBMResponsePhaseType
(
	SBMResponsePhaseTypeCd VARCHAR2(25) NOT NULL ,
	SBMResponsePhaseTypeNm VARCHAR2(255) NULL ,
	SBMResponsePhaseTypeDesc VARCHAR2(2000) NULL ,
	createdate           DATE NULL ,
	effectiveStartDate   DATE NULL ,
	effectiveEndDate     DATE NULL 
)
	INITRANS 20
	TABLESPACE FM_DATA_REF;

CREATE UNIQUE INDEX XPKSBMResponsePhaseType ON SBMResponsePhaseType
(SBMResponsePhaseTypeCd   ASC)
	INITRANS 20
	TABLESPACE FM_INDEX_REF;

CREATE TABLE SBMResponse
(
	SBMFileInfoId        NUMBER NOT NULL ,
	physicalDocumentId   INTEGER NOT NULL ,
	SBMFileProcessingSummaryId NUMBER NULL ,
	SBMResponsePhaseTypeCd VARCHAR2(25) NULL ,
	SBMRId               NUMBER NOT NULL ,
	createDateTime       TIMESTAMP DEFAULT  systimestamp  NULL ,
	createBy             VARCHAR2(50) NULL ,
	lastModifiedDateTime TIMESTAMP DEFAULT  systimestamp  NULL ,
	lastModifiedBy       VARCHAR2(50) NULL 
)
	INITRANS 20
	TABLESPACE FM_DATA_TRANS;

CREATE UNIQUE INDEX XPKSBMResponse ON SBMResponse
(SBMRId   ASC);

ALTER TABLE SBMResponsePhaseType
ADD CONSTRAINT  XPKSBMResponsePhaseType PRIMARY KEY (SBMResponsePhaseTypeCd);

ALTER TABLE SBMResponse
ADD CONSTRAINT  XPKSBMResponse PRIMARY KEY (SBMRId);

ALTER TABLE SBMResponse
ADD CONSTRAINT R_390 FOREIGN KEY (SBMFileInfoId) REFERENCES SBMFileInfo (SBMFileInfoId);

ALTER TABLE SBMResponse
ADD CONSTRAINT R_391 FOREIGN KEY (physicalDocumentId) REFERENCES PhysicalDocument (physicalDocumentIdentifier);

ALTER TABLE SBMResponse
ADD CONSTRAINT R_393 FOREIGN KEY (SBMFileProcessingSummaryId) REFERENCES SBMFileProcessingSummary (SBMFileProcessingSummaryId) ON DELETE SET NULL;

ALTER TABLE SBMResponse
ADD CONSTRAINT R_436 FOREIGN KEY (SBMResponsePhaseTypeCd) REFERENCES SBMResponsePhaseType (SBMResponsePhaseTypeCd) ON DELETE SET NULL;

DROP TABLE SBMResponse078F4528004 CASCADE CONSTRAINTS PURGE;

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
(SBMFileInfoId   ASC)
	INITRANS 20
	TABLESPACE FM_INDEX_TRANS;

ALTER TABLE SBMFileArchive
	ADD CONSTRAINT  XPKSBMFileArchive PRIMARY KEY (SBMFileInfoId);

ALTER TABLE SBMFileArchive
	ADD (CONSTRAINT R_402 FOREIGN KEY (SBMFileInfoId) REFERENCES SBMFileInfo (SBMFileInfoId));

spool off

