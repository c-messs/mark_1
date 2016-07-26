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



ALTER TABLE PolicyVersion MODIFY(subscriberStateCd VARCHAR2(2) );

ALTER TABLE PolicyVersion
	ADD (sourceVersionId NUMBER);

ALTER TABLE PolicyVersion
	ADD (sourceVersionDateTime TIMESTAMP);

ALTER TABLE BatchTransMsg
	ADD (exchangePolicyId VARCHAR2(50));

ALTER TABLE BatchTransMsg
	ADD (subscriberStateCd VARCHAR2(2));

ALTER TABLE BatchTransMsg
	ADD (issuerHiosId VARCHAR2(10));

ALTER TABLE BatchTransMsg
	ADD (sourceVersionId NUMBER);

ALTER TABLE BatchTransMsg
	ADD (sourceVersionDateTime TIMESTAMP);

ALTER TABLE BatchTransMsg
	ADD (TransMsgSkipReasonTypeCd VARCHAR2(25));

CREATE TABLE TransMsgSkipReasonType
(
	TransMsgSkipReasonTypeCd VARCHAR2(25) NOT NULL ,
	TransMsgSkipReasonTypeNm VARCHAR2(255) NULL ,
	TransMsgSkipReasonTypeDesc VARCHAR2(2000) NULL ,
	createDate           DATE NULL ,
	effectiveStartDate   DATE NULL ,
	effectiveEndDate     DATE NULL 
)
	TABLESPACE FM_DATA_REF
	INITRANS 20;

CREATE UNIQUE INDEX XPKTransactionMessageSkipReaso ON TransMsgSkipReasonType
(TransMsgSkipReasonTypeCd   ASC)
	TABLESPACE FM_INDEX_REF
	INITRANS 20;

CREATE INDEX XIF5BatchTransactionMessage ON BatchTransMsg
(TransMsgSkipReasonTypeCd   ASC)
	TABLESPACE FM_INDEX_TRANS
	INITRANS 20;

CREATE INDEX XIE1BatchTransactionMessage ON BatchTransMsg
(exchangePolicyId   ASC,subscriberStateCd   ASC,sourceVersionId   ASC,sourceVersionDateTime   ASC,processedToDBStatusTypeCd   ASC)
	TABLESPACE FM_INDEX_TRANS
	INITRANS 20;

ALTER TABLE TransMsgSkipReasonType
ADD CONSTRAINT  XPKTransactionMessageSkipReaso PRIMARY KEY (TransMsgSkipReasonTypeCd);


ALTER TABLE PolicyMemberLanguageAbility
	MODIFY lastModifiedDateTime DEFAULT sysdate;

ALTER TABLE BatchTransMsg
ADD CONSTRAINT R_271 FOREIGN KEY (TransMsgSkipReasonTypeCd) REFERENCES TransMsgSkipReasonType (TransMsgSkipReasonTypeCd) ON DELETE SET NULL;

spool off
