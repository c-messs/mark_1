

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

ALTER TABLE SBMFileError
DROP CONSTRAINT R_384;

ALTER TABLE SBMFileError
DROP CONSTRAINT R_409;

ALTER TABLE SBMFileError
DROP CONSTRAINT R_394;

ALTER TABLE SBMFileError
DROP CONSTRAINT R_395;

ALTER TABLE StagingPolicyVersion
DROP CONSTRAINT R_396;

ALTER TABLE stagingPolicyMember
DROP CONSTRAINT R_398;

ALTER TABLE stagingPolicyPremium
DROP CONSTRAINT R_400;

ALTER TABLE StagingPolicyStatus
DROP CONSTRAINT R_419;

ALTER TABLE SBMFileError
DROP CONSTRAINT XAK1SBMFileError CASCADE  DROP INDEX;

ALTER TABLE StagingPolicyVersion
DROP PRIMARY KEY CASCADE  DROP INDEX;

ALTER TABLE StagingPolicyVersion
DROP CONSTRAINT XAK1StagingPolicyVersion CASCADE  DROP INDEX;

ALTER TABLE SBMFileError RENAME TO SBMFileError06UH1224006;

ALTER TABLE StagingPolicyVersion RENAME TO StagingPolicyVersi06UH1224007;

CREATE TABLE SBMFileError
(
	SBMFileInfoId        NUMBER NOT NULL ,
	SBMErrorWarningTypeCd VARCHAR2(25) NOT NULL ,
	elementInErrorNm     VARCHAR2(255) NULL ,
	createDateTime       TIMESTAMP DEFAULT  systimestamp  NULL ,
	createBy             VARCHAR2(50) NULL ,
	lastModifiedDateTime TIMESTAMP DEFAULT  systimestamp  NULL ,
	lastModifiedBy       VARCHAR2(50) NULL ,
	sbmFileErrorSequenceNum NUMBER NOT NULL 
)
	TABLESPACE FM_DATA_TRANS
	INITRANS 20;

CREATE UNIQUE INDEX XPKSBMFileError ON SBMFileError
(SBMFileInfoId   ASC,sbmFileErrorSequenceNum   ASC)
	INITRANS 20
	TABLESPACE FM_INDEX_TRANS;

CREATE UNIQUE INDEX XAK1SBMFileError ON SBMFileError
(SBMFileInfoId   ASC,SBMErrorWarningTypeCd   ASC,elementInErrorNm   ASC)
	INITRANS 20
	TABLESPACE FM_INDEX_TRANS;

CREATE TABLE SBMFileErrorAdditionalInfo
(
	SBMFileInfoId        NUMBER NOT NULL ,
	sbmFileErrorSequenceNum NUMBER NOT NULL ,
	additionalErrorInfoText VARCHAR2(2000) NULL ,
	createDateTime       TIMESTAMP DEFAULT  systimestamp  NULL ,
	createBy             VARCHAR2(50) NULL ,
	lastModifiedDateTime TIMESTAMP DEFAULT  systimestamp  NULL ,
	lastModifiedBy       VARCHAR2(50) NULL 
)
	TABLESPACE FM_DATA_TRANS
	INITRANS 20;

CREATE TABLE SBMFileSummaryMissingPolicy
(
	SBMFileProcessingSummaryId NUMBER NOT NULL ,
	missingPolicyVersionId NUMBER NULL ,
	missingPolicyMemberVersionId NUMBER NULL ,
	createDateTime       TIMESTAMP DEFAULT  systimestamp  NULL ,
	createBy             VARCHAR2(50) NULL ,
	lastModifiedDateTime TIMESTAMP DEFAULT  systimestamp  NULL ,
	lastModifiedBy       VARCHAR2(50) NULL 
)
	TABLESPACE FM_DATA_TRANS
	INITRANS 20;

CREATE UNIQUE INDEX XAK1SBMFileSummaryMissingPolic ON SBMFileSummaryMissingPolicy
(SBMFileProcessingSummaryId   ASC,missingPolicyVersionId   ASC,missingPolicyMemberVersionId   ASC)
	INITRANS 20
	TABLESPACE FM_INDEX_TRANS;

CREATE TABLE StagingPolicyVersion
(
	policyVersionId      NUMBER NOT NULL ,
	maintenanceStartDateTime TIMESTAMP NOT NULL ,
	maintenanceEndDateTime TIMESTAMP DEFAULT  To_Date('31-dec-9999 23:59:59','dd-mon-yyyy hh24:mi:ss')  NOT NULL ,
	subscriberStateCd    VARCHAR2(2) NOT NULL ,
	issuerPolicyId       VARCHAR2(50) NULL ,
	issuerHiosId         VARCHAR2(10) NOT NULL ,
	issuerSubscriberID   VARCHAR2(50) NULL ,
	exchangePolicyId     VARCHAR2(50) NOT NULL ,
	exchangeAssignedSubscriberID VARCHAR2(50) NULL ,
	TransControlNum      VARCHAR2(50) NULL ,
	sourceExchangeID     VARCHAR2(50) NULL ,
	planID               VARCHAR2(50) NULL ,
	x12InsrncLineTypeCd  VARCHAR2(25) NULL ,
	createDateTime       DATE DEFAULT  sysdate  NOT NULL ,
	lastModifiedDateTime DATE DEFAULT  sysdate  NOT NULL ,
	createBy             VARCHAR2(50) NULL ,
	lastModifiedBy       VARCHAR2(50) NULL ,
	policyStartDate      DATE NOT NULL ,
	policyEndDate        DATE NULL ,
	SBMTransMsgID        NUMBER NULL 
)
	TABLESPACE FM_DATA_TRANS
	INITRANS 20
	PARTITION BY LIST (subscriberStateCd)
	(
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

CREATE UNIQUE INDEX XPKStagingPolicyVersion ON StagingPolicyVersion
(policyVersionId   ASC)
	INITRANS 20
	TABLESPACE FM_INDEX_TRANS;

CREATE UNIQUE INDEX XAK1StagingPolicyVersion ON StagingPolicyVersion
(exchangePolicyId   ASC,subscriberStateCd   ASC,maintenanceStartDateTime   ASC)
	INITRANS 20
	TABLESPACE FM_INDEX_TRANS;
ALTER TABLE SBMFileError
ADD CONSTRAINT  XPKSBMFileError PRIMARY KEY (SBMFileInfoId,sbmFileErrorSequenceNum);


ALTER TABLE SBMFileError
ADD CONSTRAINT  XAK1SBMFileError UNIQUE (SBMFileInfoId,SBMErrorWarningTypeCd,elementInErrorNm);

INSERT INTO SBMFileError (SBMFileInfoId, SBMErrorWarningTypeCd, elementInErrorNm, createDateTime, createBy, lastModifiedDateTime, lastModifiedBy) SELECT SBMFileInfoId, SBMErrorWarningTypeCd, elementInErrorNm, createDateTime, createBy, lastModifiedDateTime, lastModifiedBy FROM SBMFileError06UH1224006;

ALTER TABLE SBMFileSummaryMissingPolicy
ADD CONSTRAINT  XAK1SBMFileSummaryMissingPolic UNIQUE (SBMFileProcessingSummaryId,missingPolicyVersionId,missingPolicyMemberVersionId);

ALTER TABLE StagingPolicyVersion
ADD CONSTRAINT  XPKStagingPolicyVersion PRIMARY KEY (policyVersionId);

ALTER TABLE StagingPolicyVersion
ADD CONSTRAINT  XAK1StagingPolicyVersion UNIQUE (exchangePolicyId,subscriberStateCd,maintenanceStartDateTime);

INSERT INTO StagingPolicyVersion (policyVersionId, subscriberStateCd, exchangePolicyId, maintenanceStartDateTime, maintenanceEndDateTime, issuerPolicyId, issuerHiosId, issuerSubscriberID, exchangeAssignedSubscriberID, TransControlNum, policyStartDate, policyEndDate, sourceExchangeID, planID, x12InsrncLineTypeCd, SBMTransMsgID, createDateTime, lastModifiedDateTime, createBy, lastModifiedBy) SELECT policyVersionId, subscriberStateCd, exchangePolicyId, maintenanceStartDateTime, maintenanceEndDateTime, issuerPolicyId, issuerHiosId, issuerSubscriberID, exchangeAssignedSubscriberID, TransControlNum, policyStartDate, policyEndDate, sourceExchangeID, planID, x12InsrncLineTypeCd, SBMTransMsgID, createDateTime, lastModifiedDateTime, createBy, lastModifiedBy FROM StagingPolicyVersi06UH1224007;

ALTER TABLE SBMFileError
ADD CONSTRAINT R_384 FOREIGN KEY (SBMFileInfoId) REFERENCES SBMFileInfo (SBMFileInfoId);

ALTER TABLE SBMFileError
ADD CONSTRAINT R_409 FOREIGN KEY (SBMErrorWarningTypeCd) REFERENCES SBMErrorWarningType (SBMErrorWarningTypeCd);

ALTER TABLE SBMFileErrorAdditionalInfo
ADD CONSTRAINT R_434 FOREIGN KEY (SBMFileInfoId, sbmFileErrorSequenceNum) REFERENCES SBMFileError (SBMFileInfoId, sbmFileErrorSequenceNum);

ALTER TABLE SBMFileSummaryMissingPolicy
ADD CONSTRAINT R_430 FOREIGN KEY (SBMFileProcessingSummaryId) REFERENCES SBMFileProcessingSummary (SBMFileProcessingSummaryId);

ALTER TABLE SBMFileSummaryMissingPolicy
ADD CONSTRAINT R_431 FOREIGN KEY (missingPolicyVersionId) REFERENCES PolicyVersion (policyVersionId) ON DELETE SET NULL;

ALTER TABLE SBMFileSummaryMissingPolicy
ADD CONSTRAINT R_432 FOREIGN KEY (missingPolicyMemberVersionId) REFERENCES policyMemberVersion (policyMemberVersionId) ON DELETE SET NULL;

ALTER TABLE StagingPolicyVersion
ADD CONSTRAINT R_396 FOREIGN KEY (SBMTransMsgID) REFERENCES SBMTransMsg (SBMTransMsgID) ON DELETE SET NULL;

ALTER TABLE stagingPolicyMember
ADD CONSTRAINT R_398 FOREIGN KEY (policyVersionId) REFERENCES StagingPolicyVersion (policyVersionId);

ALTER TABLE stagingPolicyPremium
ADD CONSTRAINT R_400 FOREIGN KEY (policyVersionId) REFERENCES StagingPolicyVersion (policyVersionId);

ALTER TABLE StagingPolicyStatus
ADD CONSTRAINT R_419 FOREIGN KEY (policyVersionId) REFERENCES StagingPolicyVersion (policyVersionId);

DROP TABLE SBMFileError06UH1224006 CASCADE CONSTRAINTS PURGE;

DROP TABLE StagingPolicyVersi06UH1224007 CASCADE CONSTRAINTS PURGE;

spool off
