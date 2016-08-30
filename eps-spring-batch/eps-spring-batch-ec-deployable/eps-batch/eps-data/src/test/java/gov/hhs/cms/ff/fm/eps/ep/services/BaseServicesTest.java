package gov.hhs.cms.ff.fm.eps.ep.services;


import gov.cms.dsh.bem.AdditionalInfoType;
import gov.cms.dsh.bem.BenefitEnrollmentMaintenanceType;
import gov.cms.dsh.bem.MemberType;
import gov.cms.dsh.bem.PolicyInfoType;
import gov.cms.dsh.bem.TransactionInformationType;
import gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentMaintenanceDTO;
import gov.hhs.cms.ff.fm.eps.ep.data.util.TestDataUtil;
import gov.hhs.cms.ff.fm.eps.ep.enums.ExchangeType;
import gov.hhs.cms.ff.fm.eps.ep.enums.ProcessedToDbInd;
import gov.hhs.cms.ff.fm.eps.ep.enums.TxnMessageDirectionType;
import gov.hhs.cms.ff.fm.eps.ep.enums.TxnMessageType;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyPremiumPO;
import gov.hhs.cms.ff.fm.eps.ep.services.impl.TransMsgCompositeDAOImpl;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public abstract class BaseServicesTest extends TestCase {

	protected static final LocalDate DATE = LocalDate.now();
	protected static final LocalDateTime DATETIME = LocalDateTime.now();
	protected static final int YEAR = DATE.getYear();

	// Interval in milliseconds to make unique file names based on time.
	protected final int SLEEP_INTERVAL_MS = 5;
	// Use when inserting multiple policies as to not get PolicyStatus constraint violation.
	// CurrentTimeStamp in BEM is only down to seconds and TransDateTime is a key field.
	protected final int SLEEP_INTERVAL_SEC = 1050;

	protected final String APTC = "APTC";
	protected final String TIRA = "TIRA";
	protected final String TPA = "TPA";
	protected final String CSR = "CSR";
	protected final String PA1 = "PA1";

	protected final String RA = "RA";
	
	protected final LocalDate JAN_1 = LocalDate.of(YEAR, 1, 1);
	protected final LocalDate JAN_15 = LocalDate.of(YEAR, 1, 15);
	protected final LocalDate JAN_31 = LocalDate.of(YEAR, 1, 31);
	protected final LocalDate FEB_1 = LocalDate.of(YEAR, 2, 1);
	protected final LocalDate FEB_MAX = DATE.with(TemporalAdjusters.lastDayOfMonth());
	protected final LocalDate MAR_1 = LocalDate.of(YEAR, 3, 1);
	protected final LocalDate MAR_14 = LocalDate.of(YEAR, 3, 14);
	protected final LocalDate MAR_15 = LocalDate.of(YEAR, 3, 15);
	protected final LocalDate MAR_31 = LocalDate.of(YEAR, 3, 31);
	protected final LocalDate APR_1 = LocalDate.of(YEAR, 4, 1);
	protected final LocalDate APR_20 = LocalDate.of(YEAR, 4, 20);
	protected final LocalDate DEC_31 = LocalDate.of(YEAR, 12, 31);
	
	protected final LocalDateTime JAN_1_1am = LocalDateTime.of(YEAR, 1, 1, 1, 0, 0, 111111000);
	protected final LocalDateTime FEB_1_2am = LocalDateTime.of(YEAR, 2, 1, 2, 0, 0, 222222000);
	protected final LocalDateTime MAR_1_3am = LocalDateTime.of(YEAR, 3, 1, 3, 0, 0, 333333000);
	protected final LocalDateTime APR_1_4am = LocalDateTime.of(YEAR, 4, 1, 4, 0, 0, 444444000);

	@Autowired
	protected JdbcTemplate jdbc;

	@Autowired
	protected TransMsgCompositeDAOImpl transMsgService; 

	protected static final Long TRANS_MSG_ID = new Long("9999999999999");
	protected static final Long BATCH_ID = new Long("9999999999999");



	public BenefitEnrollmentMaintenanceDTO makeBemDTO(Long bemId, PolicyInfoType policyInfo) {

		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();

		Long id = TestDataUtil.getRandomNumber(8);

		bemDTO.setBemXml(getXmlString(id.toString()));
		bemDTO.setTxnMessageDirectionType(TxnMessageDirectionType.INBOUND);
		bemDTO.setTxnMessageType(TxnMessageType.MSG_834);
		bemDTO.setBatchId(TestDataUtil.getRandomNumber(5));
		bemDTO.setBem(new BenefitEnrollmentMaintenanceType());
		bemDTO.getBem().setTransactionInformation(new TransactionInformationType());
		bemDTO.getBem().getTransactionInformation().setCurrentTimeStamp(DateTimeUtil.getXMLGregorianCalendar(LocalDateTime.now()));
		TransactionInformationType transInfoType = TestDataUtil.makeTransactionInformationType(bemId.toString());
		transInfoType.setPolicySnapshotVersionNumber("000" + policyInfo.getPolicyStatus());
		bemDTO.getBem().setTransactionInformation(transInfoType);
		bemDTO.getBem().setPolicyInfo(policyInfo);

		return bemDTO;
	} 


	protected BenefitEnrollmentMaintenanceDTO makeBemDTO(Long bemId, String[] memberNames, Long policyVersionId, PolicyInfoType policyInfo) {

		BenefitEnrollmentMaintenanceDTO bemDTO = makeBemDTO(bemId, policyInfo);

		Long memId = bemId;
		MemberType memType = null;
		String subscriberId = null;

		Long transMsgFileInfoId = insertTransMsgFileInfo();
		bemDTO.setTxnMessageFileInfoId(transMsgFileInfoId);

		Long transMsgId = insertTransMsg();

		assertNotNull ("Insert transMsg failed", transMsgId);

		if(memberNames != null) {

			for(int i = 0; i < memberNames.length; ++i) {

				memId = memId + 1; 

				if (memberNames[i] != "" && memberNames[i] != null) {
					if(i == 0) {
						// make a Subscriber
						memType = TestDataUtil.makeMemberType(memId, memberNames[i], true);
						subscriberId = memType.getSubscriberID();
					} else {
						// make a non-subscriber
						memType = TestDataUtil.makeMemberType(memId, memberNames[i], false, subscriberId);
					} 
					bemDTO.getBem().getMember().add(memType);
				}
			}
		}

		if(policyVersionId != null) {
			bemDTO.setPolicyVersionId(policyVersionId);
		}
		bemDTO.setTransMsgId(transMsgId);
		bemDTO.setExchangeTypeCd(ExchangeType.FFM.getValue());
		bemDTO.setFileInformation(TestDataUtil.makeFileInformationType(bemId, bemId.toString()));
		bemDTO.setSubscriberStateCd("VA");
		// return to test for comparing for debugging.
		return bemDTO;
	}

	protected AdditionalInfoType makeAdditionalInfoType(String type, LocalDate esd, LocalDate eed, BigDecimal amt) {

		AdditionalInfoType ait = new AdditionalInfoType();
		ait.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		if (eed != null) {
			ait.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));
		}
		if (type.equals(APTC)) {
			ait.setAPTCAmount(amt); 
		} else if (type.equals(TIRA)) {
			ait.setTotalIndividualResponsibilityAmount(amt);
		} else if (type.equals(TPA)) {
			ait.setTotalPremiumAmount(amt);
		} else if (type.equals(CSR)) {
			ait.setCSRAmount(amt);
		} 
		return ait;
	}

	protected AdditionalInfoType makeAdditionalInfoType(String type, LocalDate esd, LocalDate eed, String txt) {

		AdditionalInfoType ait = new AdditionalInfoType();
		ait.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		if (eed != null) {
			ait.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));
		}
		if (type.equals(RA)) {
			ait.setRatingArea(txt);
		}  
		return ait;
	}


	protected AdditionalInfoType makeEpsPremiumMinimal(LocalDate esd, BigDecimal tpa) {

		return makeEpsPremium(esd, null, null, null, tpa, null, null, null);
	}

	protected AdditionalInfoType makeEpsPremium(LocalDate esd, LocalDate eed, BigDecimal aptc, BigDecimal csr, BigDecimal tpa, 
			BigDecimal tira, BigDecimal opa1, BigDecimal opa2) {

		AdditionalInfoType ait = new AdditionalInfoType();
		ait.setEffectiveStartDate(DateTimeUtil.getXMLGregorianCalendar(esd));
		if (eed != null) {
			ait.setEffectiveEndDate(DateTimeUtil.getXMLGregorianCalendar(eed));
		}
		ait.setAPTCAmount(aptc);
		ait.setCSRAmount(csr);
		ait.setTotalPremiumAmount(tpa);
		ait.setTotalIndividualResponsibilityAmount(tira);

		return ait;
	}


	protected void assertPolicyPremiumPO(int i, String aitType, DateTime expectedESD, DateTime expectedEED, BigDecimal expectedAmt, PolicyPremiumPO actualPO) {

		String msg = "EPS PolicyPremium Record " + i + ": " + aitType + " - ";
		assertEquals(msg + "EffectiveStartDate (ESD)", expectedESD, actualPO.getEffectiveStartDate());
		assertEquals(msg + "EffectiveEndDate (EED)", expectedEED, actualPO.getEffectiveEndDate());
		if (aitType.equals(APTC)) {
			assertEquals(msg + "AptcAmount", expectedAmt, actualPO.getAptcAmount());
		}
		if (aitType.equals(TIRA)) {
			assertEquals(msg + "IndividualResponsibleAmount", expectedAmt, actualPO.getIndividualResponsibleAmount());
		} 
		if (aitType.equals(TPA)) {
			assertEquals(msg + "TotalPremiumAmount", expectedAmt, actualPO.getTotalPremiumAmount());
		} 
		if (aitType.equals(CSR)) {
			assertEquals(msg + "CSRAmount", expectedAmt, actualPO.getCsrAmount());
		} 
	}

	/**
	 * @return
	 */
	protected Timestamp getCurrentSqlTimeStamp() {

		Timestamp time = new Timestamp(new DateTime().getMillis());
		time.setNanos(0);
		return time;
	}


	private String getXmlString(String strNum) {

		String xml = "<BenefitEnrollmentMaintenance>" +
				"<ControlNumber>" + strNum + "</ControlNumber>" +
				"<CurrentTimeStamp>" + getCurrentSqlTimeStamp() + "</CurrentTimeStamp>" +
				"</BenefitEnrollmentMaintenance>";
		return xml;
	}

	public Long insertTransMsgFileInfo() {

		Long transMsgFileInfoId = jdbc.queryForObject("SELECT TRANSMSGFILEINFOSEQ.NEXTVAL FROM DUAL", Long.class);
		jdbc.execute("INSERT INTO TRANSMSGFILEINFO (TRANSMSGFILEINFOID, FILEINFOXML, CREATEDATETIME, LASTMODIFIEDDATETIME) " +
				"VALUES (" + transMsgFileInfoId + ", XMLType('<Test>Data for TransMsgFileInfoId: " + transMsgFileInfoId +
				"</Test>'), SYSDATE, SYSDATE)");
		return transMsgFileInfoId;
	}

	public Long insertTransMsg() {

		Long transMsgFileInfoId = insertTransMsgFileInfo();
		return insertTransMsg(TxnMessageDirectionType.INBOUND.getValue(), TxnMessageType.MSG_834.getValue(), transMsgFileInfoId);
	}

	public Long insertTransMsg(String dirType, String msgType) {

		return insertTransMsg(dirType, msgType, null);
	}

	public Long insertTransMsg(String dirType, String msgType, Long transMsgFileInfoId) {

		return insertTransMsg(TxnMessageDirectionType.INBOUND.getValue(), TxnMessageType.MSG_834.getValue(), transMsgFileInfoId, null);
	}

	public Long insertTransMsg(String dirType, String msgType, Long transMsgFileInfoId, Long parentId) {

		Long transMsgId = jdbc.queryForObject("SELECT TRANSMSGSEQ.NEXTVAL FROM DUAL", Long.class);
		jdbc.execute("INSERT INTO TRANSMSG (TRANSMSGID, TRANSMSGFILEINFOID, PARENTTRANSMSGID, MSG, " +
				"TRANSMSGDIRECTIONTYPECD, TRANSMSGTYPECD) " +
				"VALUES (" + transMsgId + ", " + transMsgFileInfoId + ", " + parentId + ", XMLType('<Test>Data for TransMsgId: " +
				transMsgId +"</Test>'), '" + dirType + "', '" + msgType + "')");
		return transMsgId;
	}

	public void insertBatchTransMsg(Long transMsgId, Long batchId) {

		insertBatchTransMsg(transMsgId, batchId, null, null, null, null, null, null);
	}

	public void insertBatchTransMsg(Long transMsgId, Long batchId, ProcessedToDbInd ind) {

		insertBatchTransMsg(transMsgId, batchId, ind, null, null, null, null, null);
	}

	public void insertBatchTransMsg(Long transMsgId, Long batchId, ProcessedToDbInd ind, String stateCd, 
			String exchangePolicyId, String hiosId, String srcVerNum, LocalDateTime srcVerDt) {

		String args = "INSERT INTO BATCHTRANSMSG (TRANSMSGID, BATCHID, CREATEBY, LASTMODIFIEDBY";
		String values = ") VALUES (" + transMsgId + ", " + batchId + ", " + batchId + ", " + batchId;

		if (ind != null) {
			args += ", PROCESSEDTODBSTATUSTYPECD";
			values += ", '" + ind.getValue() + "'";
		}
		if (stateCd != null) {
			args += ", SUBSCRIBERSTATECD";
			values += ", '" + stateCd + "'";
		}
		if (stateCd != null) {
			args += ", EXCHANGEPOLICYID";
			values += ", '" + exchangePolicyId + "'";
		}
		if (stateCd != null) {
			args += ", ISSUERHIOSID";
			values += ", '" + hiosId + "'";
		}
		if (srcVerNum != null) {
			args += ", SOURCEVERSIONID";
			values += ", " + srcVerNum;
		}
		if (srcVerDt != null) {
			args += ", SOURCEVERSIONDATETIME";
			values += ", TO_TIMESTAMP('" + convertToDate(srcVerDt) + "', 'YYYY-MM-DD HH24:MI:SS.FF')";
		}
		String sql = args + values + ")";
		jdbc.execute(sql);
	}

	/**
	 * Keys prefixes are PV_, PM_, and PMV_ for each set of system values.
	 * @param policyVersionId
	 * @return
	 */
	protected List<Map<String, Object>> selectPolicyCreateAndLastMod(Long policyVersionId) {

		String sql = "SELECT pv.CREATEBY, pv.CREATEDATETIME, pv.LASTMODIFIEDBY, pv.LASTMODIFIEDDATETIME " +
				"FROM POLICYVERSION pv WHERE pv.POLICYVERSIONID = " + policyVersionId;
		List<Map<String, Object>> actualList = jdbc.queryForList(sql);
		return actualList;
	}

	/**
	 * Keys prefixes are PM_, and PMV_ for each set of system values.
	 * @param policyVersionId
	 * @return
	 */
	protected List<Map<String, Object>> selectMemberCreateAndLastMod(Long policyVersionId) {

		String sql = "SELECT pmv.EXCHANGEMEMBERID, pmv.CREATEBY, pmv.CREATEDATETIME, pmv.LASTMODIFIEDBY, " +
				"pmv.LASTMODIFIEDDATETIME FROM POLICYMEMBERVERSION pmv " +
				"JOIN POLICYMEMBER pm ON pmv.POLICYMEMBERVERSIONID = pm.POLICYMEMBERVERSIONID " +
				"JOIN POLICYVERSION pv ON pm.POLICYVERSIONID = pv.POLICYVERSIONID " +
				"WHERE pv.POLICYVERSIONID = " + policyVersionId;

		List<Map<String, Object>> actualList = jdbc.queryForList(sql);
		return actualList;
	}


	private Timestamp convertToDate(LocalDateTime localDateTime) {

		if(localDateTime != null) {
			return Timestamp.valueOf(localDateTime);
		}
		return null;
	}


}
