set SERVEROUTPUT ON FORMAT WRAPPED
set APPINFO ON
SET VERIFY OFF

SET FEEDBACK OFF
SET TERMOUT OFF

column date_column new_value today_var
select SUBSTR(sys_context('USERENV', 'MODULE'), instr(sys_context('USERENV', 'MODULE'), ' ') +1) || '_' || to_char(sysdate, 'yyyymmdd') date_column
  from dual;
  
SPOOL &today_var..log

SET VERIFY ON
SET FEEDBACK ON
SET TERMOUT On

insert into BizAppAckErrorType values ('W001', 'Missing/Invalid submitter identifier', 'Missing/Invalid submitter identifier', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W002', 'Missing/Invalid receiver identifier', 'Missing/Invalid receiver identifier', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W003', 'Missing/Invalid member identifier', 'Missing/Invalid member identifier', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W004', 'Missing/Invalid subscriber identifier', 'Missing/Invalid subscriber identifier', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W005', 'Missing/Invalid patient identifier', 'Missing/Invalid patient identifier', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W006', 'Missing/Invalid plan sponsor identifier', 'Missing/Invalid plan sponsor identifier', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W008', 'Missing/Invalid TPA/broker identifier', 'Missing/Invalid TPA/broker identifier', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W009', 'Missing/Invalid premium receiver identifier', 'Missing/Invalid premium receiver identifier', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W010', 'Missing/Invalid premium payer identifier', 'Missing/Invalid premium payer identifier', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W011', 'Missing/Invalid payer identifier', 'Missing/Invalid payer identifier', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W012', 'Missing/Invalid billing provider identifier', 'Missing/Invalid billing provider identifier', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W013', 'Missing/Invalid pay to provider identifier', 'Missing/Invalid pay to provider identifier', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W014', 'Missing/Invalid rendering provider identifier', 'Missing/Invalid rendering provider identifier', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W015', 'Missing/Invalid supervising provider identifier', 'Missing/Invalid supervising provider identifier', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W016', 'Missing/Invalid attending provider identifier', 'Missing/Invalid attending provider identifier', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W017', 'Missing/Invalid other provider identifier', 'Missing/Invalid other provider identifier', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W018', 'Missing/Invalid operating provider identifier', 'Missing/Invalid operating provider identifier', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W019', 'Missing/Invalid referring provider identifier', 'Missing/Invalid referring provider identifier', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W020', 'Missing/Invalid purchased service provider identifier', 'Missing/Invalid purchased service provider identifier', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W021', 'Missing/Invalid service facility identifier', 'Missing/Invalid service facility identifier', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W022', 'Missing/Invalid ordering provider identifier', 'Missing/Invalid ordering provider identifier', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W023', 'Missing/Invalid assistant surgeon identifier', 'Missing/Invalid assistant surgeon identifier', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W024', 'Amount/Quantity out of balance', 'Amount/Quantity out of balance', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W025', 'Duplicate', 'Duplicate', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W026', 'Billing date predates service date', 'Billing date predates service date', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W027', 'Business application currently not available', 'Business application currently not available', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W028', 'Sender not authorized for this transaction', 'Sender not authorized for this transaction', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W029', 'Number of errors exceeds permitted threshold', 'Number of errors exceeds permitted threshold', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W030', 'Required loop missing', 'Required loop missing', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W031', 'Required segment missing', 'Required segment missing', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W032', 'Required element missing', 'Required element missing', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W033', 'Situational required loop is missing', 'Situational required loop is missing', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W034', 'Situational required segment is missing', 'Situational required segment is missing', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W035', 'Situational required element is missing', 'Situational required element is missing', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W036', 'Data too long', 'Data too long', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W037', 'Data too short', 'Data too short', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W038', 'Invalid external code value', 'Invalid external code value', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W039', 'Data value out of sequence', 'Data value out of sequence', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W040', '"Not Used" data element present', '"Not Used" data element present', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W041', 'Too many sub-elements in composite', 'Too many sub-elements in composite', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W042', 'Unexpected segment', 'Unexpected segment', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W043', 'Missing data', 'Missing data', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W044', 'Out of range', 'Out of range', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W045', 'Invalid date', 'Invalid date', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W046', 'Not matching', 'Not matching', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W047', 'Invalid combination', 'Invalid combination', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W048', 'Customer identification number does not exist', 'Customer identification number does not exist', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W049', 'Duplicate batch', 'Duplicate batch', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W050', 'Incorrect data', 'Incorrect data', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W051', 'Incorrect date', 'Incorrect date', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W052', 'Duplicate transmission', 'Duplicate transmission', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W053', 'Invalid claim amount', 'Invalid claim amount', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W054', 'Invalid identification code', 'Invalid identification code', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W055', 'Missing or invalid issuer identification', 'Missing or invalid issuer identification', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W056', 'Missing or invalid item quantity', 'Missing or invalid item quantity', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W057', 'Missing or invalid item identification', 'Missing or invalid item identification', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W058', 'Missing or unauthorized transaction type code', 'Missing or unauthorized transaction type code', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W059', 'Unknown claim number', 'Unknown claim number', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W060', 'Bin segment contents not in MIME format', 'Bin segment contents not in MIME format', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W061', 'Missing/Invalid MIME header', 'Missing/Invalid MIME header', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W062', 'Missing/Invalid MIME boundary', 'Missing/Invalid MIME boundary', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W063', 'Missing/Invalid MIME transfer encoding', 'Missing/Invalid MIME transfer encoding', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W064', 'Missing/Invalid MIME content type', 'Missing/Invalid MIME content type', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W065', 'Missing/Invalid MIME content disposition (filename)', 'Missing/Invalid MIME content disposition (filename)', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W066', 'Missing/Invalid file name extension', 'Missing/Invalid file name extension', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W067', 'Invalid MIME base64 encoding', 'Invalid MIME base64 encoding', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W068', 'Invalid MIME quoted-printable encoding', 'Invalid MIME quoted-printable encoding', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W069', 'Missing/Invalid MIME line terminator (should be CR+LF)', 'Missing/Invalid MIME line terminator (should be CR+LF)', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W070', 'Missing/Invalid "end of MIME" headers', 'Missing/Invalid "end of MIME" headers', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W071', 'Missing/Invalid CDA in first MIME body parts', 'Missing/Invalid CDA in first MIME body parts', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W072', 'Missing/Invalid XML tag', 'Missing/Invalid XML tag', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W073', 'Unrecoverable XML error', 'Unrecoverable XML error', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W074', 'Invalid Data format for HL7 data type', 'Invalid Data format for HL7 data type', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W075', 'Missing/Invalid required LOINC answer part(s) in the CDA', 'Missing/Invalid required LOINC answer part(s) in the CDA', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W076', 'Missing/Invalid Provider information in the CDA', 'Missing/Invalid Provider information in the CDA', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W077', 'Missing/Invalid Patient information in the CDA', 'Missing/Invalid Patient information in the CDA', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W078', 'Missing/Invalid Attachment Control information in the CDA', 'Missing/Invalid Attachment Control information in the CDA', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W079', 'Missing/Invalid LOINC', 'Missing/Invalid LOINC', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W080', 'Missing/Invalid LOINC Modifier', 'Missing/Invalid LOINC Modifier', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W081', 'Missing/Invalid LOINC code for this attachment type', 'Missing/Invalid LOINC code for this attachment type', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W082', 'Missing/Invalid LOINC Modifier for this attachment type', 'Missing/Invalid LOINC Modifier for this attachment type', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W083', 'Situational prohibited element is present', 'Situational prohibited element is present', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W084', 'Duplicate qualifier value in repeated segment within a single loop', 'Duplicate qualifier value in repeated segment within a single loop', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W085', 'Situational required composite element is missing', 'Situational required composite element is missing', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W086', 'Situational required repeating element is missing', 'Situational required repeating element is missing', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W087', 'Situational prohibited loop is present', 'Situational prohibited loop is present', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W088', 'Situational prohibited segment is present', 'Situational prohibited segment is present', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W089', 'Situational prohibited composite element is present', 'Situational prohibited composite element is present', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W090', 'Situational prohibited repeating element is present', 'Situational prohibited repeating element is present', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W091', 'Transaction successfully received but not processed as applicable business function not performed.', 'Transaction successfully received but not processed as applicable business function not performed.', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);
insert into BizAppAckErrorType values ('W092', 'Missing/Invalid required SNOMED CT answer part(s) in the CDA', 'Missing/Invalid required SNOMED CT answer part(s) in the CDA', to_date('01/01/2015', 'mm/dd/yyyy'),  to_date('01/01/2015', 'mm/dd/yyyy'),   NULL);

commit;

spool off
