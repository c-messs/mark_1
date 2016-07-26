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

-----------------------------------
-- Created 03/08/2016 - Thomas Fidd
-- Per email from Shashidar, store XMLTYPE field as Binary rather than CLOB.
-- Step 1, Create temp table for DBMS_REDEFINITION process.
-----------------------------------

CREATE TABLE TransMsgTmp
(
	TransMsgID           NUMBER NULL ,
	parentTransMsgID     NUMBER NULL ,
	TransMsgDateTime     TIMESTAMP NULL ,
	Msg                  XMLType NULL ,
	TransMsgDirectionTypeCd VARCHAR2(25) NULL ,
	TransMsgTypeCd       VARCHAR2(25) NULL ,
	TransMsgFileInfoId   NUMBER NULL ,
	createDateTime       DATE DEFAULT SYSDATE NULL ,
	lastModifiedDateTime DATE DEFAULT SYSDATE NULL ,
	createBy             VARCHAR2(50) NULL ,
	lastModifiedBy       VARCHAR2(50) NULL ,
	subscriberStateCd    VARCHAR2(2) NULL 
)
SEGMENT CREATION IMMEDIATE 
  PCTFREE 10 PCTUSED 40 INITRANS 20 MAXTRANS 255 
 NOCOMPRESS LOGGING
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1
  BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "FM_DATA_TRANS"
XMLTYPE COLUMN "MSG" STORE AS BINARY XML (
  TABLESPACE "FM_DATA_LOB" ENABLE STORAGE IN ROW CHUNK 32768 RETENTION 
  NOCACHE LOGGING 
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1
  BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)) ;

spool off
