

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

alter session enable parallel dml;
alter session enable parallel ddl;


ALTER TABLE policyPremium
DROP CONSTRAINT R_1;

ALTER TABLE policyPremium
DROP CONSTRAINT R_379;


ALTER TABLE policyPremium RENAME TO policyPremium081D5552001;

CREATE TABLE policyPremium 
(
	policyVersionId not null , 
	effectiveStartDate    NOT NULL ,
	effectiveEndDate       ,
	totalPremiumAmount    NOT NULL ,
	otherPaymentAmount1    ,
	otherPaymentAmount2    ,
	exchangeRateArea      ,
	employerResponsibleAmount   ,
	individualResponsibleAmount   ,
	csrAmount            ,
	aptcAmount           ,
	premiumAmount2       ,
	premiumAmount3       ,
	createDateTime       DEFAULT  systimestamp  NOT NULL ,
	lastModifiedDateTime DEFAULT  systimestamp  NOT NULL ,
	createBy             ,
	lastModifiedBy       ,
	proratedPremiumAmount ,
	proratedAPTCAmount   ,
	proratedCSRAmount    ,
	proratedInddResponsibleAmount ,
	InsrncPlanVariantCmptTypeCd null 
)
	TABLESPACE FM_DATA_TRANS
	INITRANS 20
	 parallel (degree 8) nologging
	as 
	SELECT /*+ parallel 8 */ pp.policyVersionId, pp.effectiveStartDate, pp.effectiveEndDate, 
  pp.totalPremiumAmount, pp.otherPaymentAmount1, pp.otherPaymentAmount2, pp.exchangeRateArea, pp.employerResponsibleAmount, 
  pp.individualResponsibleAmount, pp.csrAmount, pp.aptcAmount, pp.premiumAmount2, pp.premiumAmount3, pp.createDateTime, pp.lastModifiedDateTime, pp.createBy, pp.lastModifiedBy, 
  pp.proratedPremiumAmount, pp.proratedAPTCAmount, pp.proratedCSRAmount, pp.proratedInddResponsibleAmount, pv.InsrncPlanVariantCmptTypeCd 
  FROM policyPremium081D5552001 pp, policyVersion pv
	where pp.policyVersionId = pv.policyVersionId
	;
	
ALTER TABLE policyPremium081D5552001
DROP PRIMARY KEY CASCADE  DROP INDEX;

CREATE UNIQUE INDEX XPKpolicyPremium ON policyPremium
(policyVersionId   ASC,effectiveStartDate   ASC);

drop index XIF1POLICYPREMIUM;

CREATE INDEX XIF1policyPremium ON policyPremium
(policyVersionId   ASC);

drop index XIF2POLICYPREMIUM;

CREATE INDEX XIF2policyPremium ON policyPremium
(InsrncPlanVariantCmptTypeCd   ASC);

ALTER TABLE policyPremium
ADD CONSTRAINT  XPKpolicyPremium PRIMARY KEY (policyVersionId,effectiveStartDate);

ALTER TABLE policyPremium
ADD CONSTRAINT R_1 FOREIGN KEY (policyVersionId) REFERENCES PolicyVersion (policyVersionId);

ALTER TABLE policyPremium
ADD CONSTRAINT R_379 FOREIGN KEY (InsrncPlanVariantCmptTypeCd) REFERENCES InsrncPlanVariantCmptType (InsrncPlanVariantCmptTypeCd);

-- DROP TABLE policyPremium081D5552001 CASCADE CONSTRAINTS PURGE;

alter table policyPremium parallel (degree 1);

spool off


