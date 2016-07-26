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
import gov.hhs.cms.ff.fm.eps.ep.util.EpsDateUtils;
import junit.framework.TestCase;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public abstract class BaseDaoTest  extends TestCase {

	@Autowired
	protected JdbcTemplate jdbc;

	private final DateTime DATETIME = new DateTime();
	private final int YEAR = DATETIME.getYear();
	
	protected final int SLEEP_INTERVAL = 1050;

	protected final DateTime JAN_1 = new DateTime(YEAR, 1, 1, 0, 0);
	protected final DateTime FEB_1 = new DateTime(YEAR, 2, 1, 0, 0);
	protected final DateTime MAR_1 = new DateTime(YEAR, 3, 1, 0, 0);
	protected final DateTime APR_1 = new DateTime(YEAR, 4, 1, 0, 0);
	protected final DateTime MAY_1 = new DateTime(YEAR, 5, 1, 0, 0);

	@Override
	public void setUp() throws Exception {


	}

	protected PolicyVersionPO makePolicyVersion(String exchangePolicyId, DateTime msd) {

		return makePolicyVersion(exchangePolicyId, msd, "VA");	
	}
	
	/*
	 * Make a minimal PolicyVersion
	 */
	protected PolicyVersionPO makePolicyVersion(String exchangePolicyId, DateTime msd, String stateCd) {

		Long transMsgId = insertTransMsg();
		PolicyVersionPO po = new PolicyVersionPO();

		po.setSubscriberStateCd(stateCd);
		po.setExchangePolicyId(exchangePolicyId);
		po.setIssuerHiosId("HIOSID9999");	
		po.setMaintenanceStartDateTime(msd);
		po.setMaintenanceEndDateTime(EpsDateUtils.HIGHDATE);
		po.setX12InsrncLineTypeCd(InsuranceLineCodeSimpleType.HLT.value());
		po.setTransMsgID(transMsgId);
		po.setPolicyStartDate(APR_1);		
		po.setCreateDateTime(EpsDateUtils.getCurrentDateTime());
		po.setLastModifiedDateTime(EpsDateUtils.getCurrentDateTime());
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

		po.setMaintenanceStartDateTime(EpsDateUtils.getCurrentDateTime());
		po.setMaintenanceEndDateTime(EpsDateUtils.HIGHDATE);
		po.setTransMsgID(transMsgId);
		po.setX12GenderTypeCd(GenderCodeSimpleType.F.value());
		po.setSubscriberStateCd("VA");
		po.setCreateDateTime(EpsDateUtils.getCurrentDateTime());
		po.setLastModifiedDateTime(EpsDateUtils.getCurrentDateTime());
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

	protected PolicyMemberVersionPO makePolicyMemberVersion(String exchangeMemId, DateTime maintStart, String exchangePolicyId, String subscriberStateCd) {

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
	protected PolicyMemberPO insertParentRecords() {
		
		Long transMsgId = insertTransMsg();
		PolicyVersionPO pvPO = makePolicyVersion(TestDataUtil.getRandomNumberAsString(9), JAN_1);
		pvPO.setTransMsgID(transMsgId);
		Long pvId = insertPolicyVersion(pvPO);
		PolicyMemberVersionPO pmvPO = makePolicyMemberVersion();
		pmvPO.setTransMsgID(transMsgId);
		Long pmvId = insertPolicyMemberVersion(pmvPO);
		PolicyMemberPO pmPO = makePolicyMember(pvId, pmvId);
		insertPolicyMember(pmPO);
		return pmPO;
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
				"MAINTENANCESTARTDATETIME, ISSUERHIOSID, X12INSRNCLINETYPECD, " +
				"TRANSMSGID, POLICYSTARTDATE) " +
				"VALUES (" + policyVersionId + ", '" + po.getSubscriberStateCd() + "', '" + po.getExchangePolicyId() + "', " +
				toDateValue(po.getMaintenanceStartDateTime()) + ", '" + po.getIssuerHiosId() + "', '" +
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
				po.getExchangeMemberID() + "', " + toDateValue(po.getMaintenanceStartDateTime()) + ", " +
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


	private String toDateValue(DateTime dt) {
		return " TO_DATE('" + getSqlDate(dt) + "', 'YYYY-MM-DD HH24:MI:SS')";
	}
	
	private java.sql.Date getSqlDate(DateTime dateTime) {
		if(dateTime != null) {
			return new java.sql.Date(dateTime.getMillis());
		}
		return null;
	}
}


