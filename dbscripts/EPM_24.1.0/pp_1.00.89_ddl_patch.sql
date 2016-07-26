

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

ALTER TABLE policyMemberVersion
ADD (  CONSTRAINT SubscriberNameValida_529753192 CHECK ( (subscriberInd = 'Y' and length(policyMemberLastNm) <= 60 and length(policyMemberFirstNm) <= 35 and length(policyMemberMiddleNm) <= 25) or
(nvl(subscriberInd,'N') != 'Y' )
 )  ) ;


ALTER TABLE PolicyVersion MODIFY(sponsorNm VARCHAR2(2000) );


ALTER TABLE policyMemberVersion MODIFY(policyMemberLastNm VARCHAR2(2000) );


ALTER TABLE policyMemberVersion MODIFY(policyMemberFirstNm VARCHAR2(2000) );


ALTER TABLE policyMemberVersion MODIFY(policyMemberMiddleNm VARCHAR2(2000) );


ALTER TABLE policyMemberVersion MODIFY(x12relationshipTypeCd VARCHAR2(25) NULL);


ALTER TABLE PolicyMemberRelatedPerson MODIFY(relatedPersonLastNm VARCHAR2(2000) );


ALTER TABLE PolicyMemberRelatedPerson MODIFY(relatedPersonFirstNm VARCHAR2(2000) );


ALTER TABLE PolicyMemberRelatedPerson MODIFY(relatedPersonMiddleNm VARCHAR2(2000) );


ALTER TABLE PolicyMemberRelatedPerson MODIFY(streetNm1 VARCHAR2(2000) );


ALTER TABLE PolicyMemberRelatedPerson MODIFY(streetNm2 VARCHAR2(2000) );


ALTER TABLE PolicyMemberRelatedPerson MODIFY(cityNm VARCHAR2(2000) );


ALTER TABLE PolicyMemberAddress MODIFY(streetNm1 VARCHAR2(2000) );


ALTER TABLE PolicyMemberAddress MODIFY(streetNm2 VARCHAR2(2000) );


ALTER TABLE PolicyMemberAddress MODIFY(cityNm VARCHAR2(2000) );


ALTER TABLE PolicyMemberTelephone MODIFY(telephoneNum VARCHAR2(2000) );


ALTER TABLE RelatedPersonTelephone MODIFY(telephoneNum VARCHAR2(2000) );

spool off
