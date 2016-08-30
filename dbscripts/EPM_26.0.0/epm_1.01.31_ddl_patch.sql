

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

CREATE SEQUENCE SBMTransMsgAddtlErrorInfoSeq
	INCREMENT BY 1
	START WITH 1
	CACHE 10;

ALTER TABLE SBMTransMsgAdditionalErrorInfo
DROP CONSTRAINT R_417;

ALTER TABLE SBMTransMsgAdditionalErrorInfo
DROP PRIMARY KEY CASCADE  DROP INDEX;

DROP TABLE SBMTransMsgAdditionalErrorInfo;

CREATE TABLE SBMTransMsgAdditionalErrorInfo
(
	SBMTransMsgID        NUMBER NOT NULL ,
	validationSequenceNum NUMBER NOT NULL ,
	additionalErrorInfoText VARCHAR2(2000) NULL ,
	createDateTime       TIMESTAMP DEFAULT  systimestamp  NULL ,
	createBy             VARCHAR2(50) NULL ,
	lastModifiedDateTime TIMESTAMP DEFAULT  systimestamp  NULL ,
	lastModifiedBy       VARCHAR2(50) NULL ,
	SBMTransMsgAddlErrorInfoId VARCHAR2(50) NOT NULL 
)
	TABLESPACE FM_DATA_TRANS
	INITRANS 20;

CREATE UNIQUE INDEX XPKSBMTransactionMessageAdditi ON SBMTransMsgAdditionalErrorInfo
(SBMTransMsgAddlErrorInfoId   ASC)
	TABLESPACE FM_INDEX_TRANS
	INITRANS 20;

ALTER TABLE SBMTransMsgAdditionalErrorInfo
ADD CONSTRAINT  XPKSBMTransactionMessageAdditi PRIMARY KEY (SBMTransMsgAddlErrorInfoId);

ALTER TABLE SBMTransMsgAdditionalErrorInfo
ADD CONSTRAINT R_417 FOREIGN KEY (SBMTransMsgID, validationSequenceNum) REFERENCES SBMTransMsgValidation (SBMTransMsgID, validationSequenceNum);

alter table SBMTRANSMSG modify (createDateTime timestamp DEFAULT  systimestamp );
alter table SBMTRANSMSG modify (lastModifiedDateTime timestamp DEFAULT  systimestamp );

alter table STAGINGPOLICYMEMBER modify (createDateTime timestamp DEFAULT  systimestamp );
alter table STAGINGPOLICYMEMBER modify (lastModifiedDateTime timestamp DEFAULT  systimestamp );

alter table STAGINGPOLICYMEMBERDATE modify (createDateTime timestamp DEFAULT  systimestamp );
alter table STAGINGPOLICYMEMBERDATE modify (lastModifiedDateTime timestamp DEFAULT  systimestamp );

alter table STAGINGPOLICYMEMBERVERSION modify (createDateTime timestamp DEFAULT  systimestamp );
alter table STAGINGPOLICYMEMBERVERSION modify (lastModifiedDateTime timestamp DEFAULT  systimestamp );

alter table STAGINGPOLICYPREMIUM modify (createDateTime timestamp DEFAULT  systimestamp );
alter table STAGINGPOLICYPREMIUM modify (lastModifiedDateTime timestamp DEFAULT  systimestamp );

alter table STAGINGPOLICYSTATUS modify (createDateTime timestamp DEFAULT  systimestamp );
alter table STAGINGPOLICYSTATUS modify (lastModifiedDateTime timestamp DEFAULT  systimestamp );

alter table STAGINGPOLICYVERSION modify (createDateTime timestamp DEFAULT  systimestamp );
alter table STAGINGPOLICYVERSION modify (lastModifiedDateTime timestamp DEFAULT  systimestamp );

spool off

