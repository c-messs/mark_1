

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
RENAME COLUMN exchangeAssignedMemerId TO exchangeAssignedMemberId;

ALTER TABLE X12LanguageType
DROP PRIMARY KEY CASCADE  DROP INDEX;

ALTER TABLE PolicyMemberLanguageAbility
DROP CONSTRAINT XAK1PolicyMemberLanguageAbilit CASCADE  DROP INDEX;

ALTER TABLE X12RaceEthnicityType
	ADD (IndustryStandardTypeNm VARCHAR2(50)  DEFAULT 'CDC' NOT NULL);

ALTER TABLE PolicyMemberLanguageAbility
	ADD (X12LanguageQualifierTypeCd VARCHAR2(25));

ALTER TABLE X12LanguageType
	ADD (X12LanguageQualifierTypeCd VARCHAR2(25)  DEFAULT 'IN' NOT NULL);

ALTER TABLE stagingPolicyMemberVersion
	ADD (X12LanguageTypeCd VARCHAR2(25));

ALTER TABLE stagingPolicyMemberVersion
	ADD (X12LanguageQualifierTypeCd VARCHAR2(25));

ALTER TABLE stagingPolicyMemberVersion
	ADD (x12raceEthnicityTypeCd VARCHAR2(25));

CREATE TABLE X12LanguageQualifierType
(
	X12LanguageQualifierTypeCd VARCHAR2(25) NOT NULL ,
	X12LanguageQualifierTypeNm VARCHAR2(255) NULL ,
	X12LanguageQualifierTypeDesc VARCHAR2(2000) NULL ,
	createDate           DATE NULL ,
	effectiveStartDate   DATE NULL ,
	effectiveEndDate     DATE NULL 
)
	TABLESPACE FM_DATA_REF
	INITRANS 20;

CREATE UNIQUE INDEX XPKX12LanguageQualifierType ON X12LanguageQualifierType
(X12LanguageQualifierTypeCd   ASC)
	INITRANS 20
	TABLESPACE FM_INDEX_REF;

CREATE UNIQUE INDEX XPKX12LanguageType ON X12LanguageType
(X12LanguageTypeCd   ASC,X12LanguageQualifierTypeCd   ASC);

CREATE UNIQUE INDEX XAK1PolicyMemberLanguageAbilit ON PolicyMemberLanguageAbility
(policyMemberVersionId   ASC,X12LanguageTypeCd   ASC,X12LanguageModeTypeCd   ASC);

ALTER TABLE X12LanguageQualifierType
ADD CONSTRAINT  XPKX12LanguageQualifierType PRIMARY KEY (X12LanguageQualifierTypeCd);

ALTER TABLE X12LanguageType
ADD CONSTRAINT  XPKX12LanguageType PRIMARY KEY (X12LanguageTypeCd,X12LanguageQualifierTypeCd);

ALTER TABLE PolicyMemberLanguageAbility
ADD CONSTRAINT  XAK1PolicyMemberLanguageAbilit UNIQUE (policyMemberVersionId,X12LanguageTypeCd,X12LanguageModeTypeCd);

ALTER TABLE X12LanguageType
ADD CONSTRAINT R_437 FOREIGN KEY (X12LanguageQualifierTypeCd) REFERENCES X12LanguageQualifierType (X12LanguageQualifierTypeCd);

ALTER TABLE StagingPolicyMemberLangAbility
DROP CONSTRAINT R_420;

ALTER TABLE StagingPolicyMemberRaceEthc
DROP CONSTRAINT R_421;

ALTER TABLE StagingPolicyMemberLangAbility
DROP PRIMARY KEY CASCADE  DROP INDEX;

ALTER TABLE StagingPolicyMemberRaceEthc
DROP PRIMARY KEY CASCADE  DROP INDEX;

DROP TABLE StagingPolicyMemberLangAbility CASCADE CONSTRAINTS PURGE;

DROP TABLE StagingPolicyMemberRaceEthc CASCADE CONSTRAINTS PURGE;
spool off
