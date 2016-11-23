package gov.hhs.cms.ff.fm.eps.ep.sbm.dao.impl;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;

import javax.xml.datatype.XMLGregorianCalendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import gov.cms.dsh.bem.GenderCodeSimpleType;
import gov.cms.dsh.bem.InsuranceLineCodeSimpleType;
import gov.cms.dsh.sbmi.PolicyMemberType;
import gov.cms.dsh.sbmi.PolicyType;
import gov.hhs.cms.ff.fm.eps.ep.data.util.TestDataUtil;
import gov.hhs.cms.ff.fm.eps.ep.enums.PolicyStatus;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus;
import gov.hhs.cms.ff.fm.eps.ep.enums.TxnMessageDirectionType;
import gov.hhs.cms.ff.fm.eps.ep.enums.TxnMessageType;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyMemberDatePO;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyMemberPO;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyMemberVersionPO;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyStatusPO;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyVersionPO;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmFileInfoPO;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmFileProcessingSummaryPO;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmPolicyMemberVersionPO;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmPolicyVersionPO;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmTransMsgPO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileInfo;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileProcessingDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMPolicyDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMPremium;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SbmDataUtil;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;
import gov.hhs.cms.ff.fm.eps.ep.util.sbm.TestDataSBMUtility;
import gov.hhs.cms.ff.fm.eps.ep.vo.UserVO;
import junit.framework.TestCase;

public abstract class BaseSBMDaoTest  extends TestCase {

	@Autowired
	protected JdbcTemplate jdbc;

	@Autowired
	protected UserVO userVO;

	protected static final LocalDateTime DATETIME = LocalDateTime.now();
	protected static final LocalDate DATE = DATETIME.toLocalDate();

	protected final int YEAR = DATETIME.getYear();

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


	protected SbmFileInfoPO makeSbmFileInfoPO(Long sbmFileProcSumId) {

		SbmFileInfoPO po = new SbmFileInfoPO();
		po.setSbmFileProcessingSummaryId(sbmFileProcSumId);
		po.setSbmFileNm("FILENM-" + sbmFileProcSumId.toString());
		po.setSbmFileCreateDateTime(LocalDateTime.now());
		po.setSbmFileId("FILEID-" + sbmFileProcSumId.toString());
		po.setSbmFileNum(Integer.valueOf(1));
		// VARCHAR2(10 BYTE)
		po.setIssuerFileSetId("FILESETID");
		po.setTradingPartnerId("TRADEPARTNR-" + sbmFileProcSumId.toString());
		po.setFunctionCd("FUNC_CD-" + sbmFileProcSumId.toString());
		po.setFileInfoXML("<FileInfo>" + sbmFileProcSumId + "</FileInfo>");
		return po;
	}

	protected SbmFileProcessingSummaryPO makeSbmFileProcessingSummaryPO(String tenantId, String issuerId, String issuerFileSetId) {

		SbmFileProcessingSummaryPO po = new SbmFileProcessingSummaryPO();
		po.setTenantId(tenantId);
		po.setIssuerId(issuerId);
		po.setIssuerFileSetId(issuerFileSetId);
		po.setCmsApprovedInd("Y");
		po.setCmsApprovalRequiredInd("N");
		po.setTotalIssuerFileCount(TestDataSBMUtility.getRandomNumber(3));
		po.setTotalRecordProcessedCnt(TestDataSBMUtility.getRandomNumber(4));
		po.setTotalRecordRejectedCnt(TestDataSBMUtility.getRandomNumber(4));
		po.setErrorThresholdPercent(new BigDecimal(".1"));
		po.setTotalPreviousPoliciesNotSubmit(TestDataSBMUtility.getRandomNumber(4));
		po.setNotSubmittedEffectuatedCnt(TestDataSBMUtility.getRandomNumber(4));
		po.setNotSubmittedTerminatedCnt(TestDataSBMUtility.getRandomNumber(4));
		po.setNotSubmittedCancelledCnt(TestDataSBMUtility.getRandomNumber(4));
		po.setTotalPolicyApprovedCnt(TestDataSBMUtility.getRandomNumber(4));
		po.setMatchingPlcNoChangeCnt(TestDataSBMUtility.getRandomNumber(4));
		po.setMatchingPlcChgApplCnt(TestDataSBMUtility.getRandomNumber(4));
		po.setMatchingPlcCorrectedChgApplCnt(TestDataSBMUtility.getRandomNumber(4));
		po.setNewPlcCreatedAsSentCnt(TestDataSBMUtility.getRandomNumber(2));
		po.setNewPlcCreatedCorrectionApplCnt(TestDataSBMUtility.getRandomNumber(2));
		po.setEffectuatedPolicyCount(TestDataSBMUtility.getRandomNumber(7));
		po.setCoverageYear(YEAR);
		po.setSbmFileStatusTypeCd(SBMFileStatus.IN_PROCESS.getValue());
		return po;
	}



	protected SbmTransMsgPO makeSbmTransMsgPO(Long sbmFileInfoId) {

		SbmTransMsgPO po = new SbmTransMsgPO();

		po.setSbmFileInfoId(sbmFileInfoId);
		po.setTransMsgDateTime(LocalDateTime.now());
		po.setMsg("<Policy>Test Policy for sbmFileInfoId: " + sbmFileInfoId + "</Policy>");
		po.setTransMsgDirectionTypeCd(TxnMessageDirectionType.INBOUND.getValue());
		po.setTransMsgTypeCd(TxnMessageType.MSG_SBMI.getValue());
		po.setSubscriberStateCd("CA");
		po.setRecordControlNum(8);
		po.setPlanId("12345CA1234560");
		po.setSbmTransMsgProcStatusTypeCd("ACC");
		po.setExchangeAssignedSubscriberId("00000000001");

		return po;
	}


	/*
	 * Make a minimal StagingPolicyVersion
	 */
	protected SbmPolicyVersionPO makeStagingPolicyVersion(String exchangePolicyId, LocalDateTime msd, LocalDateTime med, String stateCd) {

		SbmPolicyVersionPO po = new SbmPolicyVersionPO();

		po.setSubscriberStateCd(stateCd);
		po.setExchangePolicyId(exchangePolicyId);
		po.setIssuerHiosId("HIOSID9999");	
		po.setMaintenanceStartDateTime(msd);
		po.setMaintenanceEndDateTime(med);
		po.setX12InsrncLineTypeCd(InsuranceLineCodeSimpleType.HLT.value());
		po.setPolicyStartDate(APR_1);		
		po.setCreateDateTime(LocalDate.now());
		po.setLastModifiedDateTime(LocalDate.now());
		po.setCreateBy("BaseDaoTest");
		po.setLastModifiedBy("BaseDaoTest");

		return po;	
	}
	

	protected PolicyStatusPO makePolicyStatusPO(LocalDateTime transDateTime, PolicyStatus status) {
		
		PolicyStatusPO po = new PolicyStatusPO();
		
		po.setTransDateTime(transDateTime);
		po.setInsuranacePolicyStatusTypeCd(status.getValue());
		
		return po;
		
	}
	/*
	 * Make a minimal PolicyMemberVersion
	 */
	protected SbmPolicyMemberVersionPO makeSbmPolicyMemberVersionPO() {

		SbmPolicyMemberVersionPO po = new SbmPolicyMemberVersionPO();

		po.setExchangeMemberID(TestDataUtil.getRandomNumberAsString(8));
		po.setMaintenanceStartDateTime(DATETIME);
		po.setMaintenanceEndDateTime(DateTimeUtil.HIGHDATE);
		po.setX12GenderTypeCd(GenderCodeSimpleType.F.value());
		po.setSubscriberStateCd("VA");
		po.setCreateDateTime(DATE);
		po.setLastModifiedDateTime(DATE);
		po.setCreateBy("BaseDaoTest");
		po.setLastModifiedBy("BaseDaoTest");

		return po;	
	}
	
	protected PolicyMemberVersionPO makePolicyMemberVersionPO() {

		return makeSbmPolicyMemberVersionPO();
	}
	
	private PolicyMemberDatePO makePolicyMemberDatePO(Long pmvId, LocalDate pmsd, LocalDate pmed) {

		PolicyMemberDatePO po = new PolicyMemberDatePO();

		po.setPolicyMemberVersionId(pmvId);
		po.setPolicyMemberStartDate(pmsd);
		po.setPolicyMemberEndDate(pmed);

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

	protected SbmPolicyMemberVersionPO makeSbmPolicyMemberVersionPO(String exchangeMemId, LocalDateTime maintStart, String exchangePolicyId, String subscriberStateCd) {

		SbmPolicyMemberVersionPO po = makeSbmPolicyMemberVersionPO();
		
		po.setExchangePolicyId(exchangePolicyId);
		po.setExchangeMemberID(exchangeMemId);
		po.setMaintenanceStartDateTime(maintStart);
		po.setSubscriberStateCd(subscriberStateCd);

		return po;
	}
	
	protected SBMPolicyDTO insertPolicyMinimumComplete(String tenantId, String sbmFileId, LocalDateTime msd, LocalDateTime med, String exchangePolicyId, int cntMembers, boolean isStaging, Long priorPvId) {
		
		String lastModBy = userVO.getUserId();
		return insertPolicyMinimumComplete(tenantId, sbmFileId, msd, med, exchangePolicyId, cntMembers, isStaging, priorPvId, lastModBy);
	}



	/*
	 * Create a minimum policy with the supplied number of members.  Includes statuses ('2' only), premiums
	 * and parent summary and file records.
	 * 
	 * If lastModBy is supplied, it should be different that createBy.  Use this feature when inserting multiple versions
	 * of a policy.  
	 */
	protected SBMPolicyDTO insertPolicyMinimumComplete(String tenantId, String sbmFileId, LocalDateTime msd, LocalDateTime med, String exchangePolicyId, int cntMembers, boolean isStaging, Long priorPvId, String lastModBy) {

		SBMPolicyDTO policyDTO = new SBMPolicyDTO();
		policyDTO.setPolicy(new PolicyType());

		Long sbmFileProcSumId = insertSBMFileProcessingSummary(tenantId);
		Long sbmFileInfoId = insertSBMFileInfo(sbmFileProcSumId, sbmFileId);

		Long sbmTransMsgId = insertSBMTransMsg(sbmFileInfoId, tenantId.substring(0, 2));
		
		SbmPolicyVersionPO pvPO = makeStagingPolicyVersion(exchangePolicyId, msd, med, tenantId.substring(0, 2));
		policyDTO.getPolicy().setExchangeAssignedPolicyId(exchangePolicyId);
		// Will write to SbmTransMsgId in sql.
		pvPO.setTransMsgID(sbmTransMsgId);
		Long pvId = insertPolicyVersion(pvPO, isStaging, priorPvId, lastModBy);
		policyDTO.setPolicyVersionId(pvId);
	
		insertPolicyStatus(pvId, msd, PolicyStatus.EFFECTUATED_2, isStaging);
		
		SBMPremium premium = TestDataSBMUtility.makeSBMPremium(pvPO.getExchangePolicyId());
		insertPolicyPremium(pvId, premium, isStaging);

		for (int i = 0; i < cntMembers; ++i) {
			SbmPolicyMemberVersionPO pmvPO = makeSbmPolicyMemberVersionPO();
			pmvPO.setPolicyMemberFirstNm(TestDataSBMUtility.MEM_NAMES[i]);
			
			pmvPO.setTransMsgID(sbmTransMsgId);
			Long pmvId = insertPolicyMemberVersion(pmvPO, isStaging);
			//Use the premium since the "make" has dates in it.
			PolicyMemberDatePO datePO = makePolicyMemberDatePO(pmvId, premium.getEffectiveStartDate(), premium.getEffectiveEndDate());
			insertPolicyMemberDate(datePO);

			PolicyMemberPO pmPO = makePolicyMember(pvId, pmvId);
			insertPolicyMember(pmPO, isStaging);
			
			PolicyMemberType member = new PolicyMemberType();
			member.setExchangeAssignedMemberId(pmvPO.getExchangeMemberID());
			member.setMemberFirstName(pmvPO.getPolicyMemberFirstNm());
			policyDTO.getPolicy().getMemberInformation().add(member);
		}
		policyDTO.setSbmFileProcSummaryId(sbmFileProcSumId);
		policyDTO.setSbmFileInfoId(sbmFileInfoId);
		
		return policyDTO;
	}

	protected SBMFileProcessingDTO insertParentFileRecords(String tenantId, String sbmFileId) {

		SBMFileProcessingDTO dto = new SBMFileProcessingDTO();
		dto.setTenantId(tenantId);
		Long sbmFileProcSumId = insertSBMFileProcessingSummary(tenantId);
		Long sbmFileInfoId = insertSBMFileInfo(sbmFileProcSumId, sbmFileId);
		dto.setSbmFileInfo(new SBMFileInfo());
		dto.getSbmFileInfo().setSbmFileInfoId(sbmFileInfoId);
		dto.setSbmFileProcSumId(sbmFileProcSumId);
		return dto;
	}

	protected Long insertSBMFileProcessingSummary(String tenantId) {

		Long sbmFileProcSumId = jdbc.queryForObject("SELECT SBMFILEPROCESSINGSUMMARYSEQ.NEXTVAL FROM DUAL", Long.class);
		String sql = "INSERT INTO SBMFILEPROCESSINGSUMMARY(SBMFILEPROCESSINGSUMMARYID, TENANTID, SBMFILESTATUSTYPECD) " +
				"VALUES (" + sbmFileProcSumId + ", '" + tenantId + "', '" + SBMFileStatus.IN_PROCESS.getValue() + "')";
		jdbc.execute(sql);
		return sbmFileProcSumId;
	}

	protected Long insertSBMFileInfo(Long sbmFileProcSumId, String fileId) {

		Long sbmFileInfoId = jdbc.queryForObject("SELECT SBMFILEINFOSEQ.NEXTVAL FROM DUAL", Long.class);
		String sql = "INSERT INTO SBMFILEINFO (SBMFILEINFOID, SBMFILEPROCESSINGSUMMARYID, SBMFILEID) " +
				"VALUES (" + sbmFileInfoId + ", " + sbmFileProcSumId + ", '" + fileId + "')";
		jdbc.execute(sql);
		return sbmFileInfoId;
	}

	protected Long insertStagingSbmPolicy(SBMFileProcessingDTO fileDTO, String policyXML) {

		Long stagingSbmPolicyId = jdbc.queryForObject("SELECT STAGINGSBMPOLICYSEQ.NEXTVAL FROM DUAL", Long.class);
		String sql = "INSERT INTO STAGINGSBMPOLICY (SBMPOLICYXML, PROCESSINGGROUPID, " +
				"SBMFILEPROCESSINGSUMMARYID, STAGINGSBMPOLICYID, SBMFILEINFOID) " +
				"VALUES (XMLType('" + policyXML + "'), 88, " +
				fileDTO.getSbmFileProcSumId() + ", " + stagingSbmPolicyId + ", " +  fileDTO.getSbmFileInfo().getSbmFileInfoId() + ")";
		jdbc.execute(sql);
		return stagingSbmPolicyId;
	}


	protected Long insertSBMTransMsg(Long sbmFileInfoId, String state) {

		Long sbmTransMsgId = jdbc.queryForObject("SELECT SBMTRANSMSGSEQ.NEXTVAL FROM DUAL", Long.class);
		String sql = "INSERT INTO SBMTRANSMSG (SBMTRANSMSGID, MSG, TRANSMSGDIRECTIONTYPECD, TRANSMSGTYPECD, " +
				"SUBSCRIBERSTATECD, SBMFILEINFOID, CREATEDATETIME, LASTMODIFIEDDATETIME) " +
				"VALUES (" + sbmTransMsgId + ", XMLType('<Test>Data for sbmTransMsgId: " +
				sbmTransMsgId +"</Test>'), '1', '1', '"+ state+ "', " + sbmFileInfoId + ", SYSDATE, SYSDATE)";
		jdbc.execute(sql);
		return sbmTransMsgId;
	}
	
	/**
	 * Inserts a minimal record into POLICYVERSION
	 *
	 * @return policyVersionId
	 */
	protected Long insertPolicyVersion(PolicyVersionPO po, boolean isStaging) {

		Long priorPvId = null;
		String lastModBy = userVO.getUserId();
		return insertPolicyVersion(po, isStaging, priorPvId, lastModBy);
	}

	/**
	 * Inserts a minimal record into POLICYVERSION
	 *
	 * @return policyVersionId
	 */
	protected Long insertPolicyVersion(PolicyVersionPO po, boolean isStaging, Long priorPvId, String lastModby) {

		Long policyVersionId = jdbc.queryForObject("SELECT POLICYVERSIONSEQ.NEXTVAL FROM DUAL", Long.class);

		String tableNm = isStaging ? "STAGINGPOLICYVERSION" : "POLICYVERSION";
		
		String sql = "INSERT INTO " + tableNm + " (POLICYVERSIONID, SUBSCRIBERSTATECD, EXCHANGEPOLICYID, " +
				"MAINTENANCESTARTDATETIME, MAINTENANCEENDDATETIME, ISSUERHIOSID, X12INSRNCLINETYPECD, " +
				"SBMTRANSMSGID, POLICYSTARTDATE, PRIORPOLICYVERSIONID, CREATEBY, LASTMODIFIEDBY) " +
				"VALUES (" + policyVersionId + ", '" + po.getSubscriberStateCd() + "', '" + po.getExchangePolicyId() + "', " +
				toTimestampValue(po.getMaintenanceStartDateTime()) + ", " +toTimestampValue(po.getMaintenanceEndDateTime()) + ", '" + po.getIssuerHiosId() + "', '" +
				po.getX12InsrncLineTypeCd() + "', " + po.getTransMsgID() + ", " + toDateValue(MAR_1) + ", " +
				priorPvId + ", '" + userVO.getUserId() + "', '" + lastModby + "')";
		jdbc.execute(sql);
		return policyVersionId;
	}
	
	/**
	 * Inserts a complete SBM record into STAGINGPOLICYVERSION or POLICYVERSION
	 * @return policyVersionId
	 */
	protected Long insertPolicyVersion(LocalDateTime maintStart, String qhpId, SBMPolicyDTO dto, boolean isStaging) {

		Long policyVersionId = jdbc.queryForObject("SELECT POLICYVERSIONSEQ.NEXTVAL FROM DUAL", Long.class);
		String tableNm = isStaging ? "STAGINGPOLICYVERSION" : "POLICYVERSION";

		String sql = "INSERT INTO " + tableNm + " (POLICYVERSIONID, MAINTENANCESTARTDATETIME, MAINTENANCEENDDATETIME, " +
				"SUBSCRIBERSTATECD, ISSUERHIOSID, EXCHANGEPOLICYID, POLICYSTARTDATE, X12INSRNCLINETYPECD, PLANID, " +
				"SOURCEEXCHANGEID, ISSUERPOLICYID, ISSUERSUBSCRIBERID, EXCHANGEASSIGNEDSUBSCRIBERID, TRANSCONTROLNUM, " +
				"POLICYENDDATE, SBMTRANSMSGID, CREATEBY, LASTMODIFIEDBY ) " + 
				"VALUES ("+ policyVersionId + ", " + toTimestampValue(maintStart) + ", " +
				"TO_TIMESTAMP('9999-12-31 23:59:59.999', 'YYYY-MM-DD HH24:MI:SS.FF'), '" + SbmDataUtil.getStateCd(dto.getFileInfo()) + "', "+
				"'" + SbmDataUtil.getIssuerId(dto.getFileInfo()) + "', '" + dto.getPolicy().getExchangeAssignedPolicyId() + "', " +
				toDateValue(dto.getPolicy().getPolicyStartDate()) + ", 'HLT', '" + qhpId + "', " +
				"'"  + dto.getFileInfo().getTenantId() + "', '" + dto.getPolicy().getIssuerAssignedPolicyId() + "', " +
				"'"  + dto.getPolicy().getIssuerAssignedSubscriberId() + "', '" + dto.getPolicy().getExchangeAssignedSubscriberId()	+ "', " +
				"'" + dto.getPolicy().getRecordControlNumber() + "', " + toDateValue(dto.getPolicy().getPolicyEndDate()) + ", " + 
				dto.getSbmTransMsgId() + ", " + getSysData() + ")";
		jdbc.execute(sql);

		return policyVersionId;
	}
	
	private void insertPolicyStatus(Long policyVersionId, LocalDateTime transDt, PolicyStatus status, boolean isStaging) {

		String tableNm = isStaging ? "STAGINGPOLICYSTATUS" : "POLICYSTATUS";

		String sql = "INSERT INTO " + tableNm + " (POLICYVERSIONID, TRANSDATETIME, " +
				"INSURANACEPOLICYSTATUSTYPECD, CREATEBY, LASTMODIFIEDBY) VALUES (" + policyVersionId + ", " + 
				toTimestampValue(transDt) + ", '" + status.getValue() + "', " + getSysData() + ")";
		jdbc.execute(sql);
	}


	protected void insertPolicyPremium(Long policyVersionId, SBMPremium premium, boolean isStaging) {

		String eedArg = (premium.getEffectiveEndDate() != null) ? "EFFECTIVEENDDATE, " : "";

		String tableNm = isStaging ? "STAGINGPOLICYPREMIUM" : "POLICYPREMIUM";

		String sql = "INSERT INTO " + tableNm + " (POLICYVERSIONID, EFFECTIVESTARTDATE, " + eedArg +
				"TOTALPREMIUMAMOUNT, CSRAMOUNT, APTCAMOUNT, PRORATEDPREMIUMAMOUNT, PRORATEDAPTCAMOUNT, " +
				"PRORATEDCSRAMOUNT, OTHERPAYMENTAMOUNT1, OTHERPAYMENTAMOUNT2, EXCHANGERATEAREA, " +
				"INDIVIDUALRESPONSIBLEAMOUNT, INSRNCPLANVARIANTCMPTTYPECD) " +
				" VALUES (" + policyVersionId + ", " + 
				toDateValue(premium.getEffectiveStartDate()) + ", ";
		if (premium.getEffectiveEndDate() != null) {
			sql +=  toDateValue(premium.getEffectiveEndDate()) + ", ";
		}
		sql += premium.getTotalPremium() + ", " + premium.getCsr() + ", " + 
				premium.getAptc() + ", " + premium.getProratedPremium() + ", " +
				premium.getProratedAptc() + ", " + premium.getProratedCsr()+ ", " +
				premium.getOtherPayment1() + ", " + premium.getOtherPayment2() + ", '" +
				premium.getRatingArea() + "', " + premium.getIndividualResponsibleAmt() + ", '" +
				premium.getCsrVariantId() + "')";
		jdbc.execute(sql);
	}
	
	protected Long insertPolicyMemberVersion(PolicyMemberVersionPO po, boolean isStaging) {

		Long policyMemberVersionId = jdbc.queryForObject("SELECT POLICYMEMBERVERSIONSEQ.NEXTVAL FROM DUAL", Long.class);
		String tableNm = isStaging ? "STAGINGPOLICYMEMBERVERSION" : "POLICYMEMBERVERSION";

		String sql = "INSERT INTO " + tableNm + " (POLICYMEMBERVERSIONID, EXCHANGEPOLICYID, SUBSCRIBERSTATECD, " +
				"EXCHANGEMEMBERID, MAINTENANCESTARTDATETIME, MAINTENANCEENDDATETIME, " +
				// TODO: Put in sql if schema eventually requires. SBMTRANSMSGID  , " + po.getSbmTransMsgId() + "
				"X12GENDERTYPECD, CREATEBY, LASTMODIFIEDBY) " +
				"VALUES (" + policyMemberVersionId + ", '" + po.getExchangePolicyId() + "', '" + po.getSubscriberStateCd() + "', '" +
				po.getExchangeMemberID() + "', " + toTimestampValue(po.getMaintenanceStartDateTime()) + ", " +
				"TO_DATE('9999-12-31 23:59:59', 'YYYY-MM-DD HH24:MI:SS'), '" + po.getX12GenderTypeCd() + "' , " + getSysData() +")";
		jdbc.execute(sql);
		return policyMemberVersionId;
	}
	
	public void insertPolicyMemberDate(PolicyMemberDatePO po) {

		String sql = "Insert into POLICYMEMBERDATE ("
				+ "POLICYMEMBERVERSIONID, POLICYMEMBERSTARTDATE, POLICYMEMBERENDDATE, "
				+ "CREATEBY, LASTMODIFIEDBY) values (" 
				+ po.getPolicyMemberVersionId() + ", " + toDateValue(po.getPolicyMemberStartDate()) + ", " + toDateValue(po.getPolicyMemberEndDate())
				+ ", " + getSysData() + ")";
		jdbc.execute(sql);
	}

	protected void insertPolicyMember(PolicyMemberPO po, boolean isStaging) {

		String tableNm = isStaging ? "STAGINGPOLICYMEMBER" : "POLICYMEMBER";

		String sql = "INSERT INTO " + tableNm + " (POLICYVERSIONID, POLICYMEMBERVERSIONID, SUBSCRIBERSTATECD, " +
				"CREATEBY, LASTMODIFIEDBY) " +
				"VALUES (" + po.getPolicyVersionId() + ", " + po.getPolicyMemberVersionId() + ", '" + po.getSubscriberStateCd() + "', " +
				getSysData() + ")";
		jdbc.execute(sql);
	}
	
	private String toDateValue(XMLGregorianCalendar xmlGC) {

		return toDateValue(DateTimeUtil.getLocalDateFromXmlGC(xmlGC));
	}

	private String getSysData() {
		return  "'" + userVO.getUserId() + "', '" + userVO.getUserId() + "'";
	}

	private String toDateValue(LocalDate dt) {
		return " TO_DATE('" + dt.toString() + "', 'YYYY-MM-DD')";
	}

	private String toTimestampValue(LocalDateTime ts) {

		DateTimeFormatter sqlDf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"); 

		return "TO_TIMESTAMP('" + ts.format(sqlDf) + "', 'YYYY-MM-DD HH24:MI:SS.FF')";
	}

	protected void assertSysData(Map<String, Object> row) {

		assertSysData(row, null);
	}

	protected void assertSysData(Map<String, Object> row, Long batchId) {

		String expectedUser = null;
		if (batchId != null) {
			expectedUser = batchId.toString();
		} else {
			expectedUser = userVO.getUserId();
		}
		assertEquals("CREATEBY", expectedUser, row.get("CREATEBY"));
		assertEquals("LASTMODIFIEDBY", expectedUser, row.get("LASTMODIFIEDBY"));
		assertNotNull("CREATEDATETIME", row.get("CREATEDATETIME"));
		assertNotNull("LASTMODIFIEDDATETIME", row.get("LASTMODIFIEDDATETIME"));
		assertEquals("CREATEDATETIME and LASTMODIFIEDDATETIME", row.get("CREATEDATETIME"), row.get("LASTMODIFIEDDATETIME"));
	}
	
	protected void assertSysData(Map<String, Object> row, String expectedCrBy, String expectedLmBy, boolean isTimesSame) {
		
		assertEquals("CREATEBY", expectedCrBy, row.get("CREATEBY"));
		assertEquals("LASTMODIFIEDBY", expectedLmBy, row.get("LASTMODIFIEDBY"));
		assertNotNull("CREATEDATETIME", row.get("CREATEDATETIME"));
		assertNotNull("LASTMODIFIEDDATETIME", row.get("LASTMODIFIEDDATETIME"));
		
	    Date crDt = (Date) row.get("CREATEDATETIME");
	    Date lmDt = (Date) row.get("LASTMODIFIEDDATETIME");
	    
	    if (isTimesSame)  {
	    	assertEquals("CREATEDATETIME and LASTMODIFIEDDATETIME should be the same", row.get("CREATEDATETIME"), row.get("LASTMODIFIEDDATETIME"));
	 	   
	    } else {
	    	assertNotSame("CREATEDATETIME and LASTMODIFIEDDATETIME should NOT be the same", row.get("CREATEDATETIME"), row.get("LASTMODIFIEDDATETIME"));
	    	assertTrue("LASTMODIFIEDDATETIME should be greater than CREATEDATETIME", lmDt.compareTo(crDt) > 0 );
	    }
	}


}


