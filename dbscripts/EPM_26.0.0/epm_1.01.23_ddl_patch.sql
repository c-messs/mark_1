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

CREATE TABLE PayeeTradingPartnerMap
(
	PayeeId              VARCHAR2(50) NOT NULL ,
	TradingPartnerId     VARCHAR2(50) NULL 
)
	TABLESPACE FM_DATA_REF
	INITRANS 20;

CREATE UNIQUE INDEX XPKPayeeTradingPartnerMap ON PayeeTradingPartnerMap
(PayeeId   ASC)
	INITRANS 20
	TABLESPACE FM_INDEX_REF;
	
	CREATE TABLE SBMBusinessRuleType
(
	SBMBusinessRuleTypeCd VARCHAR2(25) NOT NULL ,
	SBMBusinessRuleTypeNm VARCHAR2(255) NULL ,
	SBMBusinessRuleTypeDesc VARCHAR2(2000) NULL ,
	createDate           DATE NULL ,
	effectiveStartDate   DATE NULL ,
	effectiveEndDate     DATE NULL 
)
	TABLESPACE FM_DATA_REF
	INITRANS 20;

CREATE UNIQUE INDEX XPKSBMBusinessRuleType ON SBMBusinessRuleType
(SBMBusinessRuleTypeCd   ASC)
	INITRANS 20
	TABLESPACE FM_INDEX_REF;

CREATE TABLE SBMBusinessRuleConfiguration
(
	SBMBusinessRuleTypeCd VARCHAR2(25) NOT NULL ,
	stateCd              VARCHAR2(2) NOT NULL ,
	createDateTime       TIMESTAMP DEFAULT  systimestamp  NULL ,
	createBy             VARCHAR2(50) NULL ,
	lastModifiedDateTime TIMESTAMP DEFAULT  systimestamp  NULL ,
	lastModifiedBy       VARCHAR2(50) NULL 
)
	TABLESPACE FM_DATA_REF
	INITRANS 20;

CREATE UNIQUE INDEX XPKSBMBusinessRuleConfiguratio ON SBMBusinessRuleConfiguration
(SBMBusinessRuleTypeCd   ASC,stateCd   ASC)
	INITRANS 20
	TABLESPACE FM_INDEX_REF;

CREATE TABLE SBMFileProcessingSummary
(
	SBMFileProcessingSummaryId NUMBER NOT NULL ,
	TotalIssuerFileCnt   NUMBER(3,0) NULL ,
	totalRecordProcessedCnt NUMBER(9,0) NULL ,
	totalRecordRejectedCnt NUMBER(9,0) NULL ,
	approvalThresholdPercent NUMBER(3,2) NULL ,
	TotalPreviousPoliciesNotSubmit NUMBER NULL ,
	CMSApprovedInd       VARCHAR2(1) NULL ,
	NotSubmittedEffectuatedCnt NUMBER NULL ,
	NotSubmittedTerminatedCnt NUMBER NULL ,
	NotSubmittedCancelledCnt NUMBER NULL ,
	issuerFileSetId      VARCHAR2(10) NULL ,
	issuerId             VARCHAR2(5) NULL ,
	tenantId             VARCHAR2(3) NOT NULL ,
	CMSApprovalRequiredInd VARCHAR2(1) NULL ,
	TotalPolicyApprovedCnt INTEGER NULL ,
	MatchingPlcNoChgCnt  INTEGER NULL ,
	MatchingPlcChgApplCnt INTEGER NULL ,
	MatchingPlcCorrectedChgApplCnt INTEGER NULL ,
	NewPlcCreatedAsSentCnt INTEGER NULL ,
	NewPlcCreatedCorrectionApplCnt INTEGER NULL ,
	effectuatedPolicyCnt INTEGER NULL ,
	createDatetime       TIMESTAMP DEFAULT  systimestamp  NULL ,
	createBy             VARCHAR2(50) NULL ,
	lastModifiedDateTime TIMESTAMP DEFAULT  systimestamp  NULL ,
	lastModifiedBy       VARCHAR2(50) NULL ,
	coverageYear         VARCHAR2(50) NULL 
)
	TABLESPACE FM_DATA_TRANS
	INITRANS 20;

CREATE UNIQUE INDEX XPKSBMFileProcessingSummary ON SBMFileProcessingSummary
(SBMFileProcessingSummaryId   ASC)
	INITRANS 20
	TABLESPACE FM_INDEX_TRANS;

CREATE TABLE StagingSBMGroupLock
(
	SBMFileProcessingSummaryId NUMBER NOT NULL ,
	ProcessingGroupId    NUMBER(10,0) NOT NULL 
)
	TABLESPACE FM_DATA_TRANS
	INITRANS 20;

CREATE UNIQUE INDEX XPKStagingSBMGroupLock ON StagingSBMGroupLock
(SBMFileProcessingSummaryId   ASC,ProcessingGroupId   ASC)
	INITRANS 20
	TABLESPACE FM_INDEX_TRANS;

CREATE TABLE SbmFileStatusType
(
	SbmFileStatusTypeCd  VARCHAR2(25) NOT NULL ,
	SbmFileStatusTypeNm  VARCHAR2(255) NULL ,
	SbmFileStatusTypeDesc VARCHAR2(2000) NULL ,
	createdate           DATE NULL ,
	effectiveStartDate   DATE NULL ,
	effectiveEndDate     DATE NULL 
)
	TABLESPACE FM_DATA_REF
	INITRANS 20;

CREATE UNIQUE INDEX XPKSbmFileStatusType ON SbmFileStatusType
(SbmFileStatusTypeCd   ASC)
	INITRANS 20
	TABLESPACE FM_INDEX_REF;

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
	lastSBMFileStatusTypeCd VARCHAR2(25) NULL ,
	createDateTime       TIMESTAMP DEFAULT  systimestamp  NULL ,
	createBy             VARCHAR2(50) NULL ,
	lastModifiedDateTime TIMESTAMP DEFAULT  systimestamp  NULL ,
	lastModifiedBy       VARCHAR2(50) NULL ,
	FileInfoXML          XMLType NULL 
)
	TABLESPACE FM_DATA_TRANS
	INITRANS 20
	XMLTYPE COLUMN FileInfoXML STORE AS SECUREFILE BINARY  XML(TABLESPACE FM_DATA_LOB );

CREATE UNIQUE INDEX XPKSBMFileInfo ON SBMFileInfo
(SBMFileInfoId   ASC)
	INITRANS 20
	TABLESPACE FM_INDEX_TRANS;

	CREATE TABLE SBMErrorWarningType
(
	SBMErrorWarningTypeCd VARCHAR2(25) NOT NULL ,
	SBMErrorWarningTypeNm VARCHAR2(255) NULL ,
	SBMErrorWarningTypeDesc VARCHAR2(2000) NULL ,
	createdate           DATE NULL ,
	effectiveStartDate   DATE NULL ,
	effectiveEndDate     DATE NULL 
)
	TABLESPACE FM_DATA_REF
	INITRANS 20;

CREATE UNIQUE INDEX XPKSBMErrorWarningType ON SBMErrorWarningType
(SBMErrorWarningTypeCd   ASC)
	INITRANS 20
	TABLESPACE FM_INDEX_REF;

CREATE TABLE SBMTransMsgValidation
(
	SBMTransMsgID        NUMBER NOT NULL ,
	elementInErrorNm     VARCHAR2(50) NULL ,
	validationSequenceNum NUMBER NOT NULL ,
	SBMErrorWarningTypeCd VARCHAR2(25) NULL ,
	createDateTime       TIMESTAMP DEFAULT  systimestamp  NULL ,
	createBy             VARCHAR2(50) NULL ,
	lastModifiedDateTime LONG VARCHAR DEFAULT  systimestamp  NULL ,
	lastModifiedBy       VARCHAR2(50) NULL ,
	exchangeAssignedMemerId VARCHAR2(50) NULL 
)
	TABLESPACE FM_DATA_TRANS
	INITRANS 20;

CREATE UNIQUE INDEX XPKSBMTransactionMessageValida ON SBMTransMsgValidation
(SBMTransMsgID   ASC,validationSequenceNum   ASC)
	INITRANS 20
	TABLESPACE FM_INDEX_TRANS;

CREATE TABLE SBMTransMsgAdditionalErrorInfo
(
	SBMTransMsgID        NUMBER NOT NULL ,
	validationSequenceNum NUMBER NOT NULL ,
	additionalErrorInfoText VARCHAR2(2000) NULL ,
	createDateTime       TIMESTAMP DEFAULT  systimestamp  NULL ,
	createBy             VARCHAR2(50) NULL ,
	lastModifiedDateTime TIMESTAMP DEFAULT  systimestamp  NULL ,
	lastModifiedBy       VARCHAR2(50) NULL 
)
	TABLESPACE FM_DATA_TRANS
	INITRANS 20;

CREATE UNIQUE INDEX XPKSBMTransactionMessageAdditi ON SBMTransMsgAdditionalErrorInfo
(SBMTransMsgID   ASC,validationSequenceNum   ASC)
	INITRANS 20
	TABLESPACE FM_INDEX_TRANS;

CREATE TABLE StagingSBMFile
(
	SBMXML               XMLType NULL ,
	batchId              VARCHAR2(50) NULL ,
	SBMFileProcessingSummaryId NUMBER NOT NULL ,
	SBMFileInfoId        NUMBER NOT NULL 
)
	TABLESPACE FM_DATA_TRANS
	INITRANS 20
	XMLTYPE COLUMN SBMXML STORE AS SECUREFILE BINARY  XML(TABLESPACE FM_DATA_LOB );

CREATE UNIQUE INDEX XPKStagingSBMFile ON StagingSBMFile
(SBMFileInfoId   ASC)
	INITRANS 20
	TABLESPACE FM_INDEX_TRANS;

CREATE TABLE stagingPolicyMemberVersion
(
	policyMemberVersionId NUMBER NOT NULL ,
	subscriberInd        VARCHAR2(1) NULL ,
	issuerAssignedMemberID VARCHAR2(50) NULL ,
	exchangeMemberID     VARCHAR2(50) NOT NULL ,
	maintenanceStartDateTime TIMESTAMP NOT NULL ,
	maintenanceEndDateTime TIMESTAMP DEFAULT  To_Date('31-dec-9999 23:59:59','dd-mon-yyyy hh24:mi:ss')  NOT NULL ,
	policyMemberDeathDate DATE NULL ,
	policyMemberLastNm   VARCHAR2(2000) NULL ,
	policyMemberFirstNm  VARCHAR2(2000) NULL ,
	policyMemberMiddleNm VARCHAR2(2000) NULL ,
	policyMemberSalutationNm VARCHAR2(10) NULL ,
	policyMemberSuffixNm VARCHAR2(10) NULL ,
	policyMemberSSN      VARCHAR2(9) NULL ,
	exchangePolicyId     VARCHAR2(50) NULL ,
	subscriberStateCd    VARCHAR2(2) NULL ,
	x12TobaccoUseTypeCd  VARCHAR2(25) NULL ,
	policyMemberBirthDate DATE NULL ,
	X12GenderTypeCd      VARCHAR2(25) NULL ,
	incorrectGenderTypeCd VARCHAR2(25) NULL ,
	createDateTime       DATE DEFAULT  sysdate  NOT NULL ,
	lastModifiedDateTime DATE DEFAULT  sysdate  NOT NULL ,
	createBy             VARCHAR2(50) NULL ,
	lastModifiedBy       VARCHAR2(50) NULL ,
	nonCoveredSubscriberInd VARCHAR2(1) NULL ,
CONSTRAINT SubscriberNameValida_405061302 CHECK ( (subscriberInd = 'Y' and length(policyMemberLastNm) <= 60 and length(policyMemberFirstNm) <= 35 and length(policyMemberMiddleNm) <= 25) or
(nvl(subscriberInd,'N') != 'Y' )
 )
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

CREATE UNIQUE INDEX XPKstagingPolicyMemberVersion ON stagingPolicyMemberVersion
(policyMemberVersionId   ASC)
	INITRANS 20
	TABLESPACE FM_INDEX_TRANS;

CREATE UNIQUE INDEX XAK1stagingPolicyMemberVersion ON stagingPolicyMemberVersion
(exchangeMemberID   ASC,maintenanceStartDateTime   ASC,exchangePolicyId   ASC,subscriberStateCd   ASC)
	INITRANS 20
	TABLESPACE FM_INDEX_TRANS;

CREATE TABLE StagingPolicyMemberRaceEthc
(
	policyMemberVersionId NUMBER NOT NULL ,
	x12raceEthnicityTypeCd VARCHAR2(25) NOT NULL ,
	createDateTime       DATE DEFAULT  sysdate  NOT NULL ,
	lastModifiedDateTime DATE DEFAULT  sysdate  NOT NULL ,
	createBy             VARCHAR2(50) NULL ,
	lastModifiedBy       VARCHAR2(50) NULL 
)
	TABLESPACE FM_DATA_TRANS
	INITRANS 20;

CREATE UNIQUE INDEX XPKStagingPolicyMemberRaceEthn ON StagingPolicyMemberRaceEthc
(policyMemberVersionId   ASC,x12raceEthnicityTypeCd   ASC)
	INITRANS 20
	TABLESPACE FM_INDEX_TRANS;

CREATE TABLE StagingPolicyMemberLangAbility
(
	policyMemberVersionId NUMBER NOT NULL ,
	X12LanguageTypeCd    VARCHAR2(25) NOT NULL ,
	createDateTime       DATE DEFAULT  sysdate  NOT NULL ,
	lastModifiedDateTime DATE DEFAULT  sysdate  NULL ,
	createBy             VARCHAR2(50) NULL ,
	lastModifiedBy       VARCHAR2(50) NULL 
)
	TABLESPACE FM_DATA_TRANS
	INITRANS 20;

CREATE UNIQUE INDEX XPKStagingPolicyMemberLangAbil ON StagingPolicyMemberLangAbility
(policyMemberVersionId   ASC,X12LanguageTypeCd   ASC)
	INITRANS 20
	TABLESPACE FM_INDEX_TRANS;

CREATE TABLE stagingPolicyMemberDate
(
	policyMemberVersionId NUMBER NOT NULL ,
	policyMemberStartDate DATE NOT NULL ,
	policyMemberEndDate  DATE NULL ,
	createDateTime       DATE DEFAULT  sysdate  NOT NULL ,
	lastModifiedDateTime DATE DEFAULT  sysdate  NOT NULL ,
	createBy             VARCHAR2(50) NULL ,
	lastModifiedBy       VARCHAR2(50) NULL 
)
	TABLESPACE FM_DATA_TRANS
	INITRANS 20;

CREATE UNIQUE INDEX XPKstagingPolicyMemberDate ON stagingPolicyMemberDate
(policyMemberVersionId   ASC,policyMemberStartDate   ASC)
	INITRANS 20
	TABLESPACE FM_INDEX_TRANS;

CREATE TABLE StagingSBMPolicy
(
	SBMPolicyXML         XMLType NULL ,
	batchId              INTEGER NULL ,
	ProcessingGroupId    NUMBER(10,0) NULL ,
	SBMFileProcessingSummaryId NUMBER NULL ,
	stagingSBMPolicyId   NUMBER NOT NULL ,
	SBMFileInfoId        NUMBER NULL 
)
	TABLESPACE FM_DATA_TRANS
	INITRANS 20
	XMLTYPE COLUMN SBMPolicyXML STORE AS SECUREFILE BINARY  XML(TABLESPACE FM_DATA_LOB );

CREATE UNIQUE INDEX XPKStagingSBMPolicy ON StagingSBMPolicy
(stagingSBMPolicyId   ASC);

CREATE TABLE SBMFIleProcessLog
(
	SBMFileInfoId        NUMBER NOT NULL ,
	SBMFileProcessDateTime TIMESTAMP NOT NULL ,
	FinalFileProcessStatusTypeCd VARCHAR2(50) NULL 
)
	TABLESPACE FM_DATA_TRANS
	INITRANS 20;

CREATE UNIQUE INDEX XPKSBMFIleProcessLog ON SBMFIleProcessLog
(SBMFileInfoId   ASC,SBMFileProcessDateTime   ASC)
	INITRANS 20
	TABLESPACE FM_INDEX_TRANS;

CREATE TABLE SBMFileError
(
	SBMFileInfoId        NUMBER NOT NULL ,
	missingPolicyVersionId NUMBER NULL ,
	missingPolicyMemberVersionId NUMBER NULL ,
	additionalErrorInfoText VARCHAR2(2000) NULL ,
	SBMErrorWarningTypeCd VARCHAR2(25) NOT NULL ,
	elementInErrorNm     VARCHAR2(255) NULL ,
	createDateTime       TIMESTAMP DEFAULT  systimestamp  NULL ,
	createBy             VARCHAR2(50) NULL ,
	lastModifiedDateTime TIMESTAMP DEFAULT  systimestamp  NULL ,
	lastModifiedBy       VARCHAR2(50) NULL 
)
	TABLESPACE FM_DATA_TRANS
	INITRANS 20;

CREATE UNIQUE INDEX XAK1SBMFileError ON SBMFileError
(SBMFileInfoId   ASC,SBMErrorWarningTypeCd   ASC,missingPolicyVersionId   ASC,missingPolicyMemberVersionId   ASC);

CREATE TABLE SBMTransMsgProcStatusType
(
	SBMTransMsgProcStatusTypeCd VARCHAR2(25) NOT NULL ,
	SBMTransMsgProcStatusTypeNm VARCHAR2(255) NULL ,
	SBMTransMsgProcStatusTypeDesc VARCHAR2(2000) NULL ,
	createDate           DATE NULL ,
	effectiveStartDate   DATE NULL ,
	effectiveEndDate     DATE NULL 
)
	TABLESPACE FM_DATA_REF
	INITRANS 20;

CREATE UNIQUE INDEX XPKSBMTransactionMessageProces ON SBMTransMsgProcStatusType
(SBMTransMsgProcStatusTypeCd   ASC)
	INITRANS 20
	TABLESPACE FM_INDEX_REF;

CREATE TABLE SBMTransMsg
(
	SBMTransMsgID        NUMBER NOT NULL ,
	TransMsgDateTime     TIMESTAMP NULL ,
	Msg                  XMLType NOT NULL ,
	TransMsgDirectionTypeCd VARCHAR2(25) NULL ,
	TransMsgTypeCd       VARCHAR2(25) NULL ,
	createDateTime       DATE DEFAULT  sysdate  NOT NULL ,
	lastModifiedDateTime DATE DEFAULT  sysdate  NOT NULL ,
	createBy             VARCHAR2(50) NULL ,
	lastModifiedBy       VARCHAR2(50) NULL ,
	subscriberStateCd    VARCHAR2(2) NULL ,
	SBMFileInfoId        NUMBER NOT NULL ,
	recordControlNum     NUMBER(9,0) NULL ,
	planId               VARCHAR2(14) NULL ,
	SBMTransMsgProcStatusTypeCd VARCHAR2(25) NULL ,
	exchangeAssignedPolicyId VARCHAR2(50) NULL,
	exchangeAssignedSubscriberId VARCHAR2(50) NULL 
)
	TABLESPACE FM_DATA_TRANS
	INITRANS 20
	XMLTYPE COLUMN MSG STORE AS SECUREFILE BINARY  XML(TABLESPACE FM_DATA_LOB )
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

CREATE UNIQUE INDEX XPKSBMTransactionMessage ON SBMTransMsg
(SBMTransMsgID   ASC)
	INITRANS 20
	TABLESPACE FM_INDEX_TRANS;

CREATE TABLE SBMResponse
(
	SBMFileInfoId        NUMBER NOT NULL ,
	physicalDocumentId   INTEGER NOT NULL ,
	SBMFileProcessingSummaryId NUMBER NULL ,
	SBMResponsePhaseTypeCd VARCHAR2(50) NULL ,
	SBMRId               NUMBER NOT NULL ,
	createDateTime       TIMESTAMP DEFAULT  systimestamp  NULL ,
	createBy             VARCHAR2(50) NULL ,
	lastModifiedDateTime TIMESTAMP DEFAULT  systimestamp  NULL ,
	lastModifiedBy       VARCHAR2(50) NULL 
)
	TABLESPACE FM_DATA_TRANS
	INITRANS 20;

CREATE UNIQUE INDEX XPKSBMResponse ON SBMResponse
(SBMRId   ASC)
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
	PARTITION STCDDEFAULT VALUES (DEFAULT));

CREATE UNIQUE INDEX XPKStagingPolicyVersion ON StagingPolicyVersion
(policyVersionId   ASC)
	INITRANS 20
	TABLESPACE FM_INDEX_TRANS;

CREATE UNIQUE INDEX XAK1StagingPolicyVersion ON StagingPolicyVersion
(exchangePolicyId   ASC,subscriberStateCd   ASC,maintenanceStartDateTime   ASC)
	INITRANS 20
	TABLESPACE FM_INDEX_TRANS;

CREATE TABLE StagingPolicyStatus
(
	insuranacePolicyStatusTypeCd VARCHAR2(25) NULL ,
	policyVersionId      NUMBER NOT NULL ,
	createDateTime       DATE DEFAULT  sysdate  NOT NULL ,
	lastModifiedDateTime DATE DEFAULT  sysdate  NOT NULL ,
	createBy             VARCHAR2(50) NULL ,
	lastModifiedBy       VARCHAR2(50) NULL ,
	TransDateTime        TIMESTAMP NOT NULL 
)
	TABLESPACE FM_DATA_TRANS
	INITRANS 20;

CREATE UNIQUE INDEX XPKStagingPolicyStatus ON StagingPolicyStatus
(policyVersionId   ASC,TransDateTime   ASC)
	INITRANS 20
	TABLESPACE FM_INDEX_TRANS;

CREATE TABLE stagingPolicyPremium
(
	effectiveStartDate   DATE NOT NULL ,
	effectiveEndDate     DATE NULL ,
	policyVersionId      NUMBER NOT NULL ,
	totalPremiumAmount   NUMBER NOT NULL ,
	otherPaymentAmount2  NUMBER NULL ,
	exchangeRateArea     VARCHAR2(50) NULL ,
	individualResponsibleAmount NUMBER NULL ,
	csrAmount            NUMBER NULL ,
	createDateTime       DATE DEFAULT  sysdate  NOT NULL ,
	lastModifiedDateTime DATE DEFAULT  sysdate  NOT NULL ,
	createBy             VARCHAR2(50) NULL ,
	lastModifiedBy       VARCHAR2(50) NULL ,
	otherPaymentAmount1  NUMBER NULL ,
	aptcAmount           NUMBER NULL ,
	proratedPremiumAmount NUMBER NULL ,
	proratedAPTCAmount   NUMBER NULL ,
	proratedCSRAmount    NUMBER NULL ,
	InsrncPlanVariantCmptTypeCd VARCHAR2(25) NULL 
)
	TABLESPACE FM_DATA_TRANS
	INITRANS 20;

CREATE UNIQUE INDEX XPKstagingPolicyPremium ON stagingPolicyPremium
(policyVersionId   ASC,effectiveStartDate   ASC)
	INITRANS 20
	TABLESPACE FM_INDEX_TRANS;

CREATE TABLE stagingPolicyMember
(
	policyVersionId      NUMBER NOT NULL ,
	policyMemberVersionId NUMBER NOT NULL ,
	createDateTime       DATE DEFAULT  sysdate  NOT NULL ,
	lastModifiedDateTime DATE DEFAULT  sysdate  NOT NULL ,
	createBy             VARCHAR2(50) NULL ,
	lastModifiedBy       VARCHAR2(50) NULL ,
	subscriberStateCd    VARCHAR2(2) NULL 
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

CREATE UNIQUE INDEX XPKstagingPolicyMember ON stagingPolicyMember
(policyVersionId   ASC,policyMemberVersionId   ASC)
	INITRANS 20
	TABLESPACE FM_INDEX_TRANS;

ALTER TABLE PayeeTradingPartnerMap
ADD CONSTRAINT  XPKPayeeTradingPartnerMap PRIMARY KEY (PayeeId);

ALTER TABLE SBMBusinessRuleType
ADD CONSTRAINT  XPKSBMBusinessRuleType PRIMARY KEY (SBMBusinessRuleTypeCd);

ALTER TABLE SBMBusinessRuleConfiguration
ADD CONSTRAINT  XPKSBMBusinessRuleConfiguratio PRIMARY KEY (SBMBusinessRuleTypeCd,stateCd);

ALTER TABLE SBMFileProcessingSummary
ADD CONSTRAINT  XPKSBMFileProcessingSummary PRIMARY KEY (SBMFileProcessingSummaryId);

ALTER TABLE StagingSBMGroupLock
ADD CONSTRAINT  XPKStagingSBMGroupLock PRIMARY KEY (SBMFileProcessingSummaryId,ProcessingGroupId);

ALTER TABLE SbmFileStatusType
ADD CONSTRAINT  XPKSbmFileStatusType PRIMARY KEY (SbmFileStatusTypeCd);

ALTER TABLE SBMFileInfo
ADD CONSTRAINT  XPKSBMFileInfo PRIMARY KEY (SBMFileInfoId);


ALTER TABLE SBMErrorWarningType
ADD CONSTRAINT  XPKSBMErrorWarningType PRIMARY KEY (SBMErrorWarningTypeCd);

ALTER TABLE SBMTransMsgValidation
ADD CONSTRAINT  XPKSBMTransactionMessageValida PRIMARY KEY (SBMTransMsgID,validationSequenceNum);

ALTER TABLE SBMTransMsgAdditionalErrorInfo
ADD CONSTRAINT  XPKSBMTransactionMessageAdditi PRIMARY KEY (SBMTransMsgID,validationSequenceNum);

ALTER TABLE StagingSBMFile
ADD CONSTRAINT  XPKStagingSBMFile PRIMARY KEY (SBMFileInfoId);

ALTER TABLE stagingPolicyMemberVersion
ADD CONSTRAINT  XPKstagingPolicyMemberVersion PRIMARY KEY (policyMemberVersionId);

ALTER TABLE stagingPolicyMemberVersion
ADD CONSTRAINT  XAK1stagingPolicyMemberVersion UNIQUE (exchangeMemberID,maintenanceStartDateTime,exchangePolicyId,subscriberStateCd);

ALTER TABLE StagingPolicyMemberRaceEthc
ADD CONSTRAINT  XPKStagingPolicyMemberRaceEthn PRIMARY KEY (policyMemberVersionId,x12raceEthnicityTypeCd);

ALTER TABLE StagingPolicyMemberLangAbility
ADD CONSTRAINT  XPKStagingPolicyMemberLangAbil PRIMARY KEY (policyMemberVersionId,X12LanguageTypeCd);

ALTER TABLE stagingPolicyMemberDate
ADD CONSTRAINT  XPKstagingPolicyMemberDate PRIMARY KEY (policyMemberVersionId,policyMemberStartDate);

ALTER TABLE StagingSBMPolicy
ADD CONSTRAINT  XPKStagingSBMPolicy PRIMARY KEY (stagingSBMPolicyId);

ALTER TABLE SBMFIleProcessLog
ADD CONSTRAINT  XPKSBMFIleProcessLog PRIMARY KEY (SBMFileInfoId,SBMFileProcessDateTime);

ALTER TABLE SBMFileError
ADD CONSTRAINT  XAK1SBMFileError UNIQUE (SBMFileInfoId,SBMErrorWarningTypeCd,missingPolicyVersionId,missingPolicyMemberVersionId);

ALTER TABLE SBMTransMsgProcStatusType
ADD CONSTRAINT  XPKSBMTransactionMessageProces PRIMARY KEY (SBMTransMsgProcStatusTypeCd);

ALTER TABLE SBMTransMsg
ADD CONSTRAINT  XPKSBMTransactionMessage PRIMARY KEY (SBMTransMsgID);

ALTER TABLE SBMResponse
ADD CONSTRAINT  XPKSBMResponse PRIMARY KEY (SBMRId);

ALTER TABLE StagingPolicyVersion
ADD CONSTRAINT  XPKStagingPolicyVersion PRIMARY KEY (policyVersionId);

ALTER TABLE StagingPolicyVersion
ADD CONSTRAINT  XAK1StagingPolicyVersion UNIQUE (exchangePolicyId,subscriberStateCd,maintenanceStartDateTime);

ALTER TABLE StagingPolicyStatus
ADD CONSTRAINT  XPKStagingPolicyStatus PRIMARY KEY (policyVersionId,TransDateTime);

ALTER TABLE stagingPolicyPremium
ADD CONSTRAINT  XPKstagingPolicyPremium PRIMARY KEY (policyVersionId,effectiveStartDate);

ALTER TABLE stagingPolicyMember
ADD CONSTRAINT  XPKstagingPolicyMember PRIMARY KEY (policyVersionId,policyMemberVersionId);

ALTER TABLE SBMBusinessRuleConfiguration
ADD CONSTRAINT R_403 FOREIGN KEY (SBMBusinessRuleTypeCd) REFERENCES SBMBusinessRuleType (SBMBusinessRuleTypeCd);

ALTER TABLE StagingSBMGroupLock
ADD CONSTRAINT R_426 FOREIGN KEY (SBMFileProcessingSummaryId) REFERENCES SBMFileProcessingSummary (SBMFileProcessingSummaryId);

ALTER TABLE SBMFileInfo
ADD CONSTRAINT R_392 FOREIGN KEY (SBMFileProcessingSummaryId) REFERENCES SBMFileProcessingSummary (SBMFileProcessingSummaryId);

ALTER TABLE SBMFileInfo
ADD CONSTRAINT R_408 FOREIGN KEY (lastSBMFileStatusTypeCd) REFERENCES SbmFileStatusType (SbmFileStatusTypeCd) ON DELETE SET NULL;

ALTER TABLE SBMTransMsgValidation
ADD CONSTRAINT R_389 FOREIGN KEY (SBMTransMsgID) REFERENCES SBMTransMsg (SBMTransMsgID);

ALTER TABLE SBMTransMsgValidation
ADD CONSTRAINT R_415 FOREIGN KEY (SBMErrorWarningTypeCd) REFERENCES SBMErrorWarningType (SBMErrorWarningTypeCd) ON DELETE SET NULL;

ALTER TABLE SBMTransMsgAdditionalErrorInfo
ADD CONSTRAINT R_417 FOREIGN KEY (SBMTransMsgID, validationSequenceNum) REFERENCES SBMTransMsgValidation (SBMTransMsgID, validationSequenceNum);

ALTER TABLE StagingSBMFile
ADD CONSTRAINT R_424 FOREIGN KEY (SBMFileProcessingSummaryId) REFERENCES SBMFileProcessingSummary (SBMFileProcessingSummaryId);

ALTER TABLE StagingSBMFile
ADD CONSTRAINT R_425 FOREIGN KEY (SBMFileInfoId) REFERENCES SBMFileInfo (SBMFileInfoId);

ALTER TABLE StagingPolicyMemberRaceEthc
ADD CONSTRAINT R_421 FOREIGN KEY (policyMemberVersionId) REFERENCES stagingPolicyMemberVersion (policyMemberVersionId);

ALTER TABLE StagingPolicyMemberLangAbility
ADD CONSTRAINT R_420 FOREIGN KEY (policyMemberVersionId) REFERENCES stagingPolicyMemberVersion (policyMemberVersionId);

ALTER TABLE stagingPolicyMemberDate
ADD CONSTRAINT R_397 FOREIGN KEY (policyMemberVersionId) REFERENCES stagingPolicyMemberVersion (policyMemberVersionId);

ALTER TABLE StagingSBMPolicy
ADD CONSTRAINT R_428 FOREIGN KEY (SBMFileInfoId) REFERENCES SBMFileInfo (SBMFileInfoId) ON DELETE SET NULL;

ALTER TABLE SBMFIleProcessLog
ADD CONSTRAINT R_385 FOREIGN KEY (SBMFileInfoId) REFERENCES SBMFileInfo (SBMFileInfoId);

ALTER TABLE SBMFileError
ADD CONSTRAINT R_384 FOREIGN KEY (SBMFileInfoId) REFERENCES SBMFileInfo (SBMFileInfoId);

ALTER TABLE SBMFileError
ADD CONSTRAINT R_394 FOREIGN KEY (missingPolicyVersionId) REFERENCES PolicyVersion (policyVersionId) ON DELETE SET NULL;

ALTER TABLE SBMFileError
ADD CONSTRAINT R_395 FOREIGN KEY (missingPolicyMemberVersionId) REFERENCES policyMemberVersion (policyMemberVersionId) ON DELETE SET NULL;

ALTER TABLE SBMFileError
ADD CONSTRAINT R_409 FOREIGN KEY (SBMErrorWarningTypeCd) REFERENCES SBMErrorWarningType (SBMErrorWarningTypeCd);

ALTER TABLE SBMTransMsg
ADD CONSTRAINT R_388 FOREIGN KEY (SBMFileInfoId) REFERENCES SBMFileInfo (SBMFileInfoId);

ALTER TABLE SBMTransMsg
ADD CONSTRAINT R_410 FOREIGN KEY (TransMsgTypeCd) REFERENCES TransMsgType (TransMsgTypeCd) ON DELETE SET NULL;

ALTER TABLE SBMTransMsg
ADD CONSTRAINT R_411 FOREIGN KEY (SBMTransMsgProcStatusTypeCd) REFERENCES SBMTransMsgProcStatusType (SBMTransMsgProcStatusTypeCd) ON DELETE SET NULL;

ALTER TABLE SBMResponse
ADD CONSTRAINT R_390 FOREIGN KEY (SBMFileInfoId) REFERENCES SBMFileInfo (SBMFileInfoId);

ALTER TABLE SBMResponse
ADD CONSTRAINT R_391 FOREIGN KEY (physicalDocumentId) REFERENCES PhysicalDocument (physicalDocumentIdentifier);

ALTER TABLE SBMResponse
ADD CONSTRAINT R_393 FOREIGN KEY (SBMFileProcessingSummaryId) REFERENCES SBMFileProcessingSummary (SBMFileProcessingSummaryId) ON DELETE SET NULL;

ALTER TABLE StagingPolicyVersion
ADD CONSTRAINT R_396 FOREIGN KEY (SBMTransMsgID) REFERENCES SBMTransMsg (SBMTransMsgID) ON DELETE SET NULL;

ALTER TABLE StagingPolicyStatus
ADD CONSTRAINT R_419 FOREIGN KEY (policyVersionId) REFERENCES StagingPolicyVersion (policyVersionId);

ALTER TABLE stagingPolicyPremium
ADD CONSTRAINT R_400 FOREIGN KEY (policyVersionId) REFERENCES StagingPolicyVersion (policyVersionId);

ALTER TABLE stagingPolicyMember
ADD CONSTRAINT R_398 FOREIGN KEY (policyVersionId) REFERENCES StagingPolicyVersion (policyVersionId);

ALTER TABLE stagingPolicyMember
ADD CONSTRAINT R_399 FOREIGN KEY (policyMemberVersionId) REFERENCES stagingPolicyMemberVersion (policyMemberVersionId);

ALTER TABLE PolicyVersion
ADD CONSTRAINT R_418 FOREIGN KEY (SBMTransMsgID) REFERENCES SBMTransMsg (SBMTransMsgID);

ALTER TABLE policyMemberVersion
ADD CONSTRAINT R_427 FOREIGN KEY (SBMTransMsgID) REFERENCES SBMTransMsg (SBMTransMsgID);

spool off
