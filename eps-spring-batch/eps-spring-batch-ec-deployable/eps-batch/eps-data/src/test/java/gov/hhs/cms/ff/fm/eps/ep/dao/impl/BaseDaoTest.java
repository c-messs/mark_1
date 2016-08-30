package gov.hhs.cms.ff.fm.eps.ep.dao.impl;


import gov.cms.dsh.bem.GenderCodeSimpleType;
import gov.cms.dsh.bem.InsuranceLineCodeSimpleType;
import gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentRequestDTO;
import gov.hhs.cms.ff.fm.eps.ep.data.util.TestDataUtil;
import gov.hhs.cms.ff.fm.eps.ep.enums.ProcessedToDbInd;
import gov.hhs.cms.ff.fm.eps.ep.po.BatchTransMsgPO;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyMemberPO;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyMemberVersionPO;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyVersionPO;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public abstract class BaseDaoTest  extends TestCase {

	@Autowired
	protected JdbcTemplate jdbc;

	protected static final LocalDateTime DATETIME = LocalDateTime.now();
	protected static final LocalDate DATE = DATETIME.toLocalDate();

	private final int YEAR = DATETIME.getYear();

	protected final int SLEEP_INTERVAL = 1050;

	protected final LocalDate JAN_1 = LocalDate.of(YEAR, 1, 1);
	protected final LocalDate FEB_1 = LocalDate.of(YEAR, 2, 1);
	protected final LocalDate MAR_1 = LocalDate.of(YEAR, 3, 1);
	protected final LocalDate APR_1 = LocalDate.of(YEAR, 4, 1);
	protected final LocalDate MAY_1 = LocalDate.of(YEAR, 5, 1);

	protected final LocalDateTime JAN_1_1am = LocalDateTime.of(YEAR, 1, 1, 1, 0, 0, 111111000);
	protected final LocalDateTime FEB_1_2am = LocalDateTime.of(YEAR, 2, 1, 2, 0, 0, 222222000);
	protected final LocalDateTime MAR_1_3am = LocalDateTime.of(YEAR, 3, 1, 3, 0, 0, 333333000);
	protected final LocalDateTime APR_1_4am = LocalDateTime.of(YEAR, 4, 1, 4, 0, 0, 444444000);

	@Override
	public void setUp() throws Exception {


	}

	protected PolicyVersionPO makePolicyVersion() {

		return makePolicyVersion(TestDataUtil.getRandomNumberAsString(9), LocalDateTime.now(), "DC");	
	}

	protected PolicyVersionPO makePolicyVersion(String exchangePolicyId, LocalDateTime msd) {

		return makePolicyVersion(exchangePolicyId, msd, "VA");	
	}

	/*
	 * Make a minimal PolicyVersion
	 */
	protected PolicyVersionPO makePolicyVersion(String exchangePolicyId, LocalDateTime msd, String stateCd) {

		Long transMsgId = insertTransMsg();
		PolicyVersionPO po = new PolicyVersionPO();

		po.setSubscriberStateCd(stateCd);
		po.setExchangePolicyId(exchangePolicyId);
		po.setIssuerHiosId("HIOSID9999");	
		po.setMaintenanceStartDateTime(msd);
		po.setMaintenanceEndDateTime(DateTimeUtil.HIGHDATE);
		po.setX12InsrncLineTypeCd(InsuranceLineCodeSimpleType.HLT.value());
		po.setTransMsgID(transMsgId);
		po.setPolicyStartDate(APR_1);		
		po.setCreateDateTime(LocalDate.now());
		po.setLastModifiedDateTime(LocalDate.now());
		po.setCreateBy("BaseDaoTest");
		po.setLastModifiedBy("BaseDaoTest");

		return po;	
	}


	/*
	 * Make a minimal PolicyMemberVersion
	 */
	protected PolicyMemberVersionPO makePolicyMemberVersion() {

		Long transMsgId = insertTransMsg();
		PolicyMemberVersionPO po = new PolicyMemberVersionPO();

		po.setExchangeMemberID(TestDataUtil.getRandomNumberAsString(8));
		po.setMaintenanceStartDateTime(DATETIME);
		po.setMaintenanceEndDateTime(DateTimeUtil.HIGHDATE);
		po.setTransMsgID(transMsgId);
		po.setX12GenderTypeCd(GenderCodeSimpleType.F.value());
		po.setSubscriberStateCd("VA");
		po.setCreateDateTime(DATE);
		po.setLastModifiedDateTime(DATE);
		po.setCreateBy("BaseDaoTest");
		po.setLastModifiedBy("BaseDaoTest");

		return po;	
	}

	/*
	 * Make a minimal PolicyMemberPO
	 */
	protected PolicyMemberPO makePolicyMember(Long pvId, Long pmvId) {

		PolicyMemberPO po = new PolicyMemberPO();

		po.setPolicyVersionId(pvId);
		po.setPolicyMemberVersionId(pmvId);
		po.setSubscriberStateCd("VA");

		return po;	
	}

	protected PolicyMemberVersionPO makePolicyMemberVersion(String exchangeMemId, LocalDateTime maintStart, String exchangePolicyId, String subscriberStateCd) {

		PolicyMemberVersionPO po = makePolicyMemberVersion();
		po.setExchangePolicyId(exchangePolicyId);
		po.setExchangeMemberID(exchangeMemId);
		po.setMaintenanceStartDateTime(maintStart);
		po.setSubscriberStateCd(subscriberStateCd);

		return po;
	}

	protected BatchTransMsgPO makeBatchTransMsgPO(Long batchId, Long transMsgId) {

		BatchTransMsgPO po = new BatchTransMsgPO();
		po.setBatchId(batchId);
		po.setTransMsgId(transMsgId);
		po.setCreateBy(batchId.toString());
		po.setLastModifiedBy(batchId.toString());

		return po;
	}

	/*
	 * Return the PolicyMemberPO since it contains both Policy and Member Ids.
	 */
	protected List<PolicyMemberPO> insertParentRecords(int cntMembers) {

		List<PolicyMemberPO> pmPOList = new ArrayList<PolicyMemberPO>();
		Long transMsgId = insertTransMsg();
		PolicyVersionPO pvPO = makePolicyVersion(TestDataUtil.getRandomNumberAsString(9), JAN_1_1am);
		pvPO.setTransMsgID(transMsgId);
		Long pvId = insertPolicyVersion(pvPO);

		for (int i = 0; i < cntMembers; ++i) {
			PolicyMemberVersionPO pmvPO = makePolicyMemberVersion();
			pmvPO.setTransMsgID(transMsgId);
			Long pmvId = insertPolicyMemberVersion(pmvPO);

			PolicyMemberPO pmPO = makePolicyMember(pvId, pmvId);
			insertPolicyMember(pmPO);
			pmPOList.add(pmPO);
		}
		return pmPOList;
	}


	protected Long insertTransMsgFileInfo(String groupSenderId) {

		Long transMsgFileInfoId = jdbc.queryForObject("SELECT TRANSMSGFILEINFOSEQ.NEXTVAL FROM DUAL", Long.class);
		jdbc.execute("INSERT INTO TRANSMSGFILEINFO (TRANSMSGFILEINFOID, FILEINFOXML, GROUPSENDERID) " +
				"VALUES (" + transMsgFileInfoId + ", XMLType('<Test>Data for TransMsgFileInfoId: " + transMsgFileInfoId +
				"</Test>'), '" + groupSenderId + "')");
		return transMsgFileInfoId;
	}

	protected Long insertTransMsgFileInfo(BenefitEnrollmentRequestDTO berDTO) {

		Long transMsgFileInfoId = jdbc.queryForObject("SELECT TRANSMSGFILEINFOSEQ.NEXTVAL FROM DUAL", Long.class);
		jdbc.execute("INSERT INTO TRANSMSGFILEINFO (TRANSMSGFILEINFOID, FILEINFOXML, GROUPSENDERID) " +
				"VALUES (" + transMsgFileInfoId + ", XMLType('<Test>Data for TransMsgFileInfoId: " + transMsgFileInfoId +
				"</Test>'), '" + berDTO.getFileInformation().getGroupSenderID() + "')");
		jdbc.execute("INSERT INTO STAGINGBER (TRANSMSGFILEINFOID, BERXML) VALUES( " + transMsgFileInfoId +
				", XMLType('" + berDTO.getBerXml() + "'))");
		return transMsgFileInfoId;
	}

	protected Long insertTransMsg() {

		Long transMsgId = jdbc.queryForObject("SELECT TRANSMSGSEQ.NEXTVAL FROM DUAL", Long.class);
		jdbc.execute("INSERT INTO TRANSMSG (TRANSMSGID, MSG, TRANSMSGDIRECTIONTYPECD, TRANSMSGTYPECD, " +
				"CREATEDATETIME, LASTMODIFIEDDATETIME) VALUES (" + transMsgId + ", XMLType('<Test>Data for TransMsgId: " +
				transMsgId +"</Test>'), '1', '1', SYSDATE, SYSDATE)");
		return transMsgId;
	}

	/**
	 * Inserts a minimal record into POLICYVERSION
	 *
	 * @return policyVersionId
	 */
	protected Long insertPolicyVersion(PolicyVersionPO po) {

		Long policyVersionId = jdbc.queryForObject("SELECT POLICYVERSIONSEQ.NEXTVAL FROM DUAL", Long.class);

		String sql = "INSERT INTO POLICYVERSION (POLICYVERSIONID, SUBSCRIBERSTATECD, EXCHANGEPOLICYID, " +
				"MAINTENANCESTARTDATETIME, MAINTENANCEENDDATETIME, ISSUERHIOSID, X12INSRNCLINETYPECD, " +
				"TRANSMSGID, POLICYSTARTDATE) " +
				"VALUES (" + policyVersionId + ", '" + po.getSubscriberStateCd() + "', '" + po.getExchangePolicyId() + "', " +
				toTimestampValue(po.getMaintenanceStartDateTime()) + ", " +toTimestampValue(po.getMaintenanceEndDateTime()) + ", '" + po.getIssuerHiosId() + "', '" +
				po.getX12InsrncLineTypeCd() + "', " + po.getTransMsgID() + ", " + toDateValue(MAR_1) + ")";
		jdbc.execute(sql);
		return policyVersionId;
	}

	protected Long insertPolicyMemberVersion(PolicyMemberVersionPO po) {

		Long policyMemberVersionId = jdbc.queryForObject("SELECT POLICYMEMBERVERSIONSEQ.NEXTVAL FROM DUAL", Long.class);

		String sql = "INSERT INTO POLICYMEMBERVERSION(POLICYMEMBERVERSIONID, EXCHANGEPOLICYID, SUBSCRIBERSTATECD, " +
				"EXCHANGEMEMBERID, MAINTENANCESTARTDATETIME, MAINTENANCEENDDATETIME, " +
				"TRANSMSGID, X12GENDERTYPECD, CREATEDATETIME, LASTMODIFIEDDATETIME) " +
				"VALUES (" + policyMemberVersionId + ", '" + po.getExchangePolicyId() + "', '" + po.getSubscriberStateCd() + "', '" +
				po.getExchangeMemberID() + "', " + toTimestampValue(po.getMaintenanceStartDateTime()) + ", " +
				"TO_DATE('9999-12-31 23:59:59', 'YYYY-MM-DD HH24:MI:SS'), " + po.getTransMsgID() + ", '" + po.getX12GenderTypeCd() + "' , SYSDATE, SYSDATE)";
		jdbc.execute(sql);
		return policyMemberVersionId;
	}

	protected void insertPolicyMember(PolicyMemberPO po) {

		String sql = "INSERT INTO POLICYMEMBER (POLICYVERSIONID, POLICYMEMBERVERSIONID, SUBSCRIBERSTATECD, " +
				"CREATEDATETIME, LASTMODIFIEDDATETIME) " +
				"VALUES (" + po.getPolicyVersionId() + ", " + po.getPolicyMemberVersionId() + ", '" + po.getSubscriberStateCd() + "', " +
				"SYSDATE, SYSDATE)";
		jdbc.execute(sql);
	}

	public void insertBatchTransMsg(Long transMsgId, Long batchId) {

		insertBatchTransMsg(transMsgId, batchId, null, null, null, null, null, null);
	}

	public void insertBatchTransMsg(Long transMsgId, Long batchId, ProcessedToDbInd ind) {

		insertBatchTransMsg(transMsgId, batchId, ind, null, null, null, null, null);
	}

	public void insertBatchTransMsg(Long transMsgId, Long batchId, ProcessedToDbInd ind, String stateCd, 
			String exchangePolicyId, String hiosId, String srcVerNum, DateTime srcVerDt) {

		String args = "INSERT INTO BATCHTRANSMSG (TRANSMSGID, BATCHID";
		String values = ") VALUES (" + transMsgId + ", " + batchId;

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

	private java.sql.Date convertToDate(DateTime dateTime) {

		if(dateTime != null) {
			return new java.sql.Date(dateTime.getMillis());
		}
		return null;
	}


	private String toDateValue(LocalDate dt) {
		return " TO_DATE('" + dt.toString() + "', 'YYYY-MM-DD')";
	}

	private String toTimestampValue(LocalDateTime ts) {

		DateTimeFormatter sqlDf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"); 

		return "TO_TIMESTAMP('" + ts.format(sqlDf) + "', 'YYYY-MM-DD HH24:MI:SS.FF')";
	}

}


