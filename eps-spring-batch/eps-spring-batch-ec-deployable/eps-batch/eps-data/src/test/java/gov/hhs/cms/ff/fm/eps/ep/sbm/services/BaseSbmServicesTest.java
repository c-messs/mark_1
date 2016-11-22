package gov.hhs.cms.ff.fm.eps.ep.sbm.services;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import javax.xml.datatype.XMLGregorianCalendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import gov.cms.dsh.sbmi.PolicyMemberType;
import gov.hhs.cms.ff.fm.eps.ep.enums.PolicyStatus;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus;
import gov.hhs.cms.ff.fm.eps.ep.enums.SbmTransMsgStatus;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMErrorDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileInfo;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileProcessingDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMPolicyDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMPremium;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SbmDataUtil;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;
import gov.hhs.cms.ff.fm.eps.ep.vo.UserVO;
import junit.framework.TestCase;

public abstract class BaseSbmServicesTest extends TestCase {

	@Autowired
	protected JdbcTemplate jdbc;

	@Autowired
	protected UserVO userVO;

	protected static final LocalDate DATE = LocalDate.now();

	protected static final int YEAR = DATE.getYear();

	protected final LocalDate JAN_1 = LocalDate.of(YEAR, 1, 1);
	protected final LocalDate DEC_31 = LocalDate.of(YEAR, 12, 31);


	protected final LocalDateTime JAN_1_1am = LocalDateTime.of(YEAR, 1, 1, 1, 0, 0, 111111000);
	protected final LocalDateTime JAN_2_2am = LocalDateTime.of(YEAR, 2, 2, 2, 0, 0, 222222000);

	protected final DateTimeFormatter dtFmtr = DateTimeFormatter.ofPattern("yyyy-MM-dd");


	/**
	 * Inserts a minimal parent record into BATCHPROCESSLOG.
	 */
	protected void insertBatchProcessLog(String batchBusId, Long id, String name, String status)  {

		String sql ="INSERT INTO BATCHPROCESSLOG(BATCHBUSINESSID, JOBID, JOBNM, JOBSTATUSCD) " +
				"VALUES ('" + batchBusId + "'," + id + ", '" + name + "', '" + status + "')";
		jdbc.execute(sql);
	}


	protected Long insertSBMFileProcessingSummary(String tenantId) {

		Long sbmFileProcSumId = jdbc.queryForObject("SELECT SBMFILEPROCESSINGSUMMARYSEQ.NEXTVAL FROM DUAL", Long.class);
		jdbc.execute("INSERT INTO SBMFILEPROCESSINGSUMMARY(SBMFILEPROCESSINGSUMMARYID, TENANTID, SBMFILESTATUSTYPECD) " +
				"VALUES (" + sbmFileProcSumId + ", '" + tenantId + "', '" + SBMFileStatus.IN_PROCESS.getValue() + "')");

		return sbmFileProcSumId;
	}


	protected Long insertSBMFileProcessingSummary(String tenantId, String fileSetId, SBMFileStatus fileStatus, BigDecimal errPct ) {

		Long sbmFileProcSumId = jdbc.queryForObject("SELECT SBMFILEPROCESSINGSUMMARYSEQ.NEXTVAL FROM DUAL", Long.class);
		jdbc.execute("INSERT INTO SBMFILEPROCESSINGSUMMARY(SBMFILEPROCESSINGSUMMARYID, TENANTID, ISSUERFILESETID, SBMFILESTATUSTYPECD, " +
				"ERRORTHRESHOLDPERCENT) " +
				"VALUES (" + sbmFileProcSumId + ", '" + tenantId + "', '" + fileSetId + "', '" + fileStatus.getValue() + "', " + errPct + ")");
		return sbmFileProcSumId;
	}

	protected Long insertSBMFileProcessingSummary(String tenantId, String issuerId, String fileSetId, SBMFileStatus fileStatus) {

		Long sbmFileProcSumId = jdbc.queryForObject("SELECT SBMFILEPROCESSINGSUMMARYSEQ.NEXTVAL FROM DUAL", Long.class);
		jdbc.execute("INSERT INTO SBMFILEPROCESSINGSUMMARY(SBMFILEPROCESSINGSUMMARYID, TENANTID, ISSUERID, " +
				"ISSUERFILESETID, SBMFILESTATUSTYPECD) " +
				"VALUES (" + sbmFileProcSumId + ", '" + tenantId + "', '" + issuerId + "', '" + 
				fileSetId + "', '" + fileStatus.getValue() + "')");
		return sbmFileProcSumId;
	}

	protected Long insertSBMFileInfo(Long sbmFileProcSumId, String sbmFileId) {

		return insertSBMFileInfo(sbmFileProcSumId, sbmFileId, 1);
	}

	protected Long insertSBMFileInfo(Long sbmFileProcSumId, String sbmFileId, int fileNum) {

		Long sbmFileInfoId = jdbc.queryForObject("SELECT SBMFILEINFOSEQ.NEXTVAL FROM DUAL", Long.class);
		jdbc.execute("INSERT INTO SBMFILEINFO (SBMFILEINFOID, SBMFILEID, SBMFILEPROCESSINGSUMMARYID, " +
				"SBMFILENUM, FILEINFOXML, SBMFILENM) " +
				"VALUES (" + sbmFileInfoId + ", '" + sbmFileId + "', " + sbmFileProcSumId + ", " + fileNum + ", " +
				"'<FileInfo><FileId>" + sbmFileId + "</FileId><Msg>TEST DATA</Msg></FileInfo>', 'FILENAME-" + sbmFileId +"')");
		return sbmFileInfoId;
	}

	protected SBMFileProcessingDTO insertParentFileRecords(String tenantId, String sbmFileId) {

		SBMFileProcessingDTO dto = new SBMFileProcessingDTO();
		dto.setTenantId(tenantId);
		BigDecimal errPct = new BigDecimal("99.22");
		Long sbmFileProcSumId = insertSBMFileProcessingSummary(tenantId, null, SBMFileStatus.IN_PROCESS, errPct);
		Long sbmFileInfoId = insertSBMFileInfo(sbmFileProcSumId, sbmFileId);
		dto.setSbmFileInfo(new SBMFileInfo());
		dto.getSbmFileInfo().setSbmFileInfoId(sbmFileInfoId);
		dto.setSbmFileProcSumId(sbmFileProcSumId);
		dto.setErrorThresholdPercent(errPct);

		return dto;
	}

	protected SBMFileProcessingDTO insertParentFileRecords(String tenantId, String sbmFileId, SBMFileStatus fileStatus) {

		SBMFileProcessingDTO dto = new SBMFileProcessingDTO();
		dto.setTenantId(tenantId);
		BigDecimal errPct = new BigDecimal("99.22");
		Long sbmFileProcSumId = insertSBMFileProcessingSummary(tenantId, null, fileStatus, errPct);
		Long sbmFileInfoId = insertSBMFileInfo(sbmFileProcSumId, sbmFileId);
		dto.setSbmFileInfo(new SBMFileInfo());
		dto.getSbmFileInfo().setSbmFileInfoId(sbmFileInfoId);
		dto.setSbmFileProcSumId(sbmFileProcSumId);
		dto.setErrorThresholdPercent(errPct);

		return dto;
	}

	protected SBMFileProcessingDTO insertParentFileRecords(String tenantId, String sbmFileId, String issuerFileSetId, int fileNum, String issuerId) {
		
		SBMFileStatus fileStatus = SBMFileStatus.IN_PROCESS;
		return insertParentFileRecords(tenantId, sbmFileId, issuerFileSetId, fileNum, issuerId, fileStatus);
	}
	
	protected SBMFileProcessingDTO insertParentFileRecords(String tenantId, String sbmFileId, String issuerFileSetId, int fileNum, String issuerId, SBMFileStatus fileStatus) {

		SBMFileProcessingDTO dto = new SBMFileProcessingDTO();
		dto.setTenantId(tenantId);
		Long sbmFileProcSumId = insertSBMFileProcessingSummary(tenantId, issuerId, issuerFileSetId, fileStatus);
		Long sbmFileInfoId = insertSBMFileInfo(sbmFileProcSumId, sbmFileId, fileNum);
		dto.setSbmFileInfo(new SBMFileInfo());
		dto.getSbmFileInfo().setSbmFileInfoId(sbmFileInfoId);
		dto.setSbmFileProcSumId(sbmFileProcSumId);
		return dto;
	}


	protected Long insertStagingSbmPolicy(Long sbmFileProcSumId, Long sbmFileInfoId, String policyXml) {

		Long stagingPolicyId = jdbc.queryForObject("SELECT STAGINGSBMPOLICYSEQ.NEXTVAL FROM DUAL", Long.class);
		jdbc.execute("INSERT INTO STAGINGSBMPOLICY (SBMFILEPROCESSINGSUMMARYID, STAGINGSBMPOLICYID, SBMFILEINFOID, SBMPOLICYXML) " +
				"VALUES(" + sbmFileProcSumId + ", " + stagingPolicyId + ", " + sbmFileInfoId + ", XMLType('" + policyXml + "'))");
		return stagingPolicyId;

	}

	protected void insertStagingSbmGroupLock (Long sbmFileProcSumId, Long procGroupId) {

		insertStagingSbmGroupLock(sbmFileProcSumId, procGroupId, null);
	}

	protected void insertStagingSbmGroupLock (Long sbmFileProcSumId, Long procGroupId, Long batchId) {

		String sql = "INSERT INTO STAGINGSBMGROUPLOCK (SBMFILEPROCESSINGSUMMARYID, PROCESSINGGROUPID, BATCHID) " +
				"VALUES (" + sbmFileProcSumId + ", " + procGroupId + ", " + batchId + ")";
		jdbc.execute(sql);
	}

	protected Long insertSbmTransMsg(Long sbmFileInfoId, String state, String exchangePolicyId, SbmTransMsgStatus status) {

		Long sbmTransMsgId = jdbc.queryForObject("SELECT SBMTRANSMSGSEQ.NEXTVAL FROM DUAL", Long.class);
		jdbc.execute("INSERT INTO SBMTRANSMSG (SBMTRANSMSGID, MSG, TRANSMSGDIRECTIONTYPECD, TRANSMSGTYPECD, " +
				"SUBSCRIBERSTATECD, SBMFILEINFOID, EXCHANGEASSIGNEDPOLICYID, SBMTRANSMSGPROCSTATUSTYPECD) " +
				"VALUES (" + sbmTransMsgId + ", XMLType('<Test>Data for sbmTransMsgId: " +
				sbmTransMsgId +"</Test>'), '1', '1', '" + state + "', " + sbmFileInfoId + ", " + 
				"'" + exchangePolicyId + "', '" + status.getCode() + "')");
		return sbmTransMsgId;
	}

	protected void insertSbmTransMsgValidation(Long sbmTransMsgId, SBMErrorDTO errorDTO, int seq) {

		jdbc.execute("INSERT INTO SBMTRANSMSGVALIDATION (SBMTRANSMSGID, ELEMENTINERRORNM, VALIDATIONSEQUENCENUM, " +
				"SBMERRORWARNINGTYPECD, CREATEBY, LASTMODIFIEDBY, EXCHANGEASSIGNEDMEMBERID) " +
				"VALUES (" + sbmTransMsgId + ", '" + errorDTO.getElementInErrorNm() + "'," + seq + ", " +
				"'" + errorDTO.getSbmErrorWarningTypeCd() + "'" + getSysData() + ", '" + errorDTO.getExchangeAssignedMemberId() + "')");

		for ( String addErrInfoTxt : errorDTO.getAdditionalErrorInfoList()) {

			String sql = "INSERT INTO SBMTRANSMSGADDITIONALERRORINFO (SBMTRANSMSGADDLERRORINFOID, SBMTRANSMSGID, " +
					"VALIDATIONSEQUENCENUM, ADDITIONALERRORINFOTEXT, CREATEBY, LASTMODIFIEDBY) " + 
					"VALUES (SBMTRANSMSGADDTLERRORINFOSEQ.NEXTVAL, "  + sbmTransMsgId + ", " + seq + ", '" +addErrInfoTxt + "'" + getSysData() + " )";
			jdbc.execute(sql);
		}
	}

	/**
	 * Inserts a minimal record into POLICYVERSION
	 * @return policyVersionId
	 */
	private Long insertPolicyVersion(LocalDateTime maintStart, LocalDate psd, LocalDate ped, String stateCd, String exchangePolicyId, String issuerId, String qhpId, Long sbmTransMsgId, Long priorPvid, boolean isStaging) {

		Long policyVersionId = jdbc.queryForObject("SELECT POLICYVERSIONSEQ.NEXTVAL FROM DUAL", Long.class);
		String tableNm = isStaging ? "STAGINGPOLICYVERSION" : "POLICYVERSION";

		String sql = "INSERT INTO " + tableNm + " (POLICYVERSIONID, MAINTENANCESTARTDATETIME, MAINTENANCEENDDATETIME, " +
				"SUBSCRIBERSTATECD, ISSUERHIOSID, EXCHANGEPOLICYID, POLICYSTARTDATE, POLICYENDDATE, X12INSRNCLINETYPECD, PLANID, SBMTRANSMSGID, " +
				"PRIORPOLICYVERSIONID) " + 
				"VALUES ("+ policyVersionId + ", " + toTimestampValue(maintStart) + ", " +
				"TO_TIMESTAMP('9999-12-31 23:59:59.999', 'YYYY-MM-DD HH24:MI:SS.FF'), '" + stateCd + "', "+
				"'" + issuerId + "', '" + exchangePolicyId + "', " +
				toDateValue(psd) + ", " + toDateValue(ped) + ", 'HLT', '" + qhpId + "', " + sbmTransMsgId + ", " + priorPvid + ")";
		jdbc.execute(sql);
		return policyVersionId;
	}

	/**
	 * Inserts a minimal record into POLICYVERSION
	 * @return policyVersionId
	 */
	protected Long insertPolicyVersion(LocalDateTime maintStart, LocalDate psd, LocalDate ped, String stateCd, String exchangePolicyId, String qhpId, Long sbmTransMsgId) {

		boolean isStaging = false;
		String issuerId = qhpId.substring(0, 5);
		Long priorPvId = null;
		return insertPolicyVersion(maintStart, psd, ped, stateCd, exchangePolicyId, issuerId, qhpId, sbmTransMsgId, priorPvId, isStaging);
	}

	/**
	 * Inserts a minimal record into POLICYVERSION
	 * @return policyVersionId
	 */
	protected Long insertPolicyVersion(LocalDateTime maintStart, LocalDate psd, LocalDate ped, String stateCd, String exchangePolicyId, String qhpId, Long sbmTransMsgId, Long priorPvId) {

		boolean isStaging = false;
		String issuerId = qhpId.substring(0, 5);
		return insertPolicyVersion(maintStart, psd, ped, stateCd, exchangePolicyId, issuerId, qhpId, sbmTransMsgId, priorPvId, isStaging);
	}

	/**
	 * Inserts a complete SBM record into POLICYVERSION
	 * @return policyVersionId
	 */
	protected Long insertPolicyVersion(LocalDateTime maintStart, String qhpId, SBMPolicyDTO dto) {

		boolean isStaging = false;
		Long priorPvId = null;
		return insertPolicyVersion(maintStart, qhpId, dto, priorPvId, isStaging);
	}

	protected Long insertPolicyVersion(LocalDateTime maintStart, String qhpId, SBMPolicyDTO dto, boolean isStaging) {

		Long priorPvId = null;
		return insertPolicyVersion(maintStart, qhpId, dto, priorPvId, isStaging);
	}

	/**
	 * Inserts a complete SBM record into STAGINGPOLICYVERSION or POLICYVERSION
	 * @return policyVersionId
	 */
	protected Long insertPolicyVersion(LocalDateTime maintStart, String qhpId, SBMPolicyDTO dto, Long priorPvId, boolean isStaging) {

		Long policyVersionId = jdbc.queryForObject("SELECT POLICYVERSIONSEQ.NEXTVAL FROM DUAL", Long.class);
		String tableNm = isStaging ? "STAGINGPOLICYVERSION" : "POLICYVERSION";

		String sql = "INSERT INTO " + tableNm + " (POLICYVERSIONID, MAINTENANCESTARTDATETIME, MAINTENANCEENDDATETIME, " +
				"SUBSCRIBERSTATECD, ISSUERHIOSID, EXCHANGEPOLICYID, POLICYSTARTDATE, X12INSRNCLINETYPECD, PLANID, " +
				"SOURCEEXCHANGEID, ISSUERPOLICYID, ISSUERSUBSCRIBERID, EXCHANGEASSIGNEDSUBSCRIBERID, TRANSCONTROLNUM, " +
				"POLICYENDDATE, SBMTRANSMSGID, PRIORPOLICYVERSIONID "  + getSysArgs() + ") " + 
				"VALUES ("+ policyVersionId + ", " + toTimestampValue(maintStart) + ", " +
				"TO_TIMESTAMP('9999-12-31 23:59:59.999', 'YYYY-MM-DD HH24:MI:SS.FF'), '" + SbmDataUtil.getStateCd(dto.getFileInfo()) + "', "+
				"'" + SbmDataUtil.getIssuerId(dto.getFileInfo()) + "', '" + dto.getPolicy().getExchangeAssignedPolicyId() + "', " +
				toDateValue(dto.getPolicy().getPolicyStartDate()) + ", 'HLT', '" + qhpId + "', " +
				"'"  + dto.getFileInfo().getTenantId() + "', '" + dto.getPolicy().getIssuerAssignedPolicyId() + "', " +
				"'"  + dto.getPolicy().getIssuerAssignedSubscriberId() + "', '" + dto.getPolicy().getExchangeAssignedSubscriberId()	+ "', " +
				"'" + dto.getPolicy().getRecordControlNumber() + "', " + toDateValue(dto.getPolicy().getPolicyEndDate()) + ", " + dto.getSbmTransMsgId() + 
				", " + priorPvId + getSysData() + ")";
		jdbc.execute(sql);

		return policyVersionId;
	}


	protected Long insertPolicyMemberVersion(String stateCd, String exchangeMemberId, LocalDateTime msd) {

		Long policyMemberVersionId = jdbc.queryForObject("SELECT POLICYMEMBERVERSIONSEQ.NEXTVAL FROM DUAL", Long.class);

		String sql = "INSERT INTO POLICYMEMBERVERSION(POLICYMEMBERVERSIONID, EXCHANGEMEMBERID, SUBSCRIBERSTATECD, " +
				"MAINTENANCESTARTDATETIME) " +
				"VALUES (" + policyMemberVersionId + ", '" + exchangeMemberId + "', '" + stateCd + "', " + toTimestampValue(msd) + ")";
		jdbc.execute(sql);
		return policyMemberVersionId;
	}

	protected Long insertPolicyMemberVersion(String stateCd, LocalDateTime msd, String exchangePolicyId, PolicyMemberType member) {

		boolean isStaging = false;
		Long priorPmvId = null;
		return insertPolicyMemberVersion(stateCd, msd, exchangePolicyId, member, priorPmvId, isStaging);
	}

	protected Long insertPolicyMemberVersion(String stateCd, LocalDateTime msd, String exchangePolicyId, PolicyMemberType member, boolean isStaging) {

		Long priorPmvId = null;
		return insertPolicyMemberVersion(stateCd, msd, exchangePolicyId, member, priorPmvId, isStaging);
	}

	protected Long insertPolicyMemberVersion(String stateCd, LocalDateTime msd, String exchangePolicyId, PolicyMemberType member, Long priorPmvId, boolean isStaging) {

		Long policyMemberVersionId = jdbc.queryForObject("SELECT POLICYMEMBERVERSIONSEQ.NEXTVAL FROM DUAL", Long.class);

		String tableNm = isStaging ? "STAGINGPOLICYMEMBERVERSION" : "POLICYMEMBERVERSION";
		String stagingAttrs = ", PRIORPOLICYMEMBERVERSIONID, X12LANGUAGETYPECD, X12LANGUAGEQUALIFIERTYPECD, X12RACEETHNICITYTYPECD, ZIPPLUS4CD";
		String priorPmvIdArg = isStaging ? stagingAttrs : "";

		//" (subscriberInd = 'Y' and length(policyMemberLastNm) <= 60 and length(policyMemberFirstNm) <= 35 and length(policyMemberMiddleNm) <= 25) or
		//(nvl(subscriberInd,'N') != 'Y' )
		String lastNm = member.getMemberLastName();
		String firstNm = member.getMemberFirstName();
		String midNm = member.getMemberMiddleName();


		if (member.getSubscriberIndicator().equals("Y")) {
			lastNm = lastNm.length() > 60 ? lastNm.substring(0, 60) : lastNm;
			firstNm = firstNm.length() > 35 ? firstNm.substring(0, 35) : firstNm;
			midNm = midNm.length() > 25 ? midNm.substring(0, 25) : midNm;
		}
		String sql = "INSERT INTO  " + tableNm + 
				"(POLICYMEMBERVERSIONID, SUBSCRIBERIND, ISSUERASSIGNEDMEMBERID, EXCHANGEMEMBERID, MAINTENANCESTARTDATETIME, " +
				"POLICYMEMBERLASTNM, POLICYMEMBERFIRSTNM, POLICYMEMBERMIDDLENM, POLICYMEMBERSALUTATIONNM, " +
				"POLICYMEMBERSUFFIXNM, POLICYMEMBERSSN, EXCHANGEPOLICYID, SUBSCRIBERSTATECD, X12TOBACCOUSETYPECD, POLICYMEMBERBIRTHDATE, " +
				"X12GENDERTYPECD,  CREATEBY, LASTMODIFIEDBY " + priorPmvIdArg + ") " +
				"VALUES (" + policyMemberVersionId + ", '" + member.getSubscriberIndicator() + "', '"  + member.getIssuerAssignedMemberId() + "', "+
				"'" + member.getExchangeAssignedMemberId() + "', " + toTimestampValue(msd) + ",'" + lastNm + "', " +
				"'" + firstNm + "', '" + midNm + "', '" + member.getNamePrefix()  + "', " +
				toVal(member.getNameSuffix()) + ", '" + member.getSocialSecurityNumber() + "', '" + exchangePolicyId + "', " + 
				"'" + stateCd + "', '" + member.getTobaccoUseCode() + "', " + toDateValue(member.getBirthDate()) + ", " +
				"'" + member.getGenderCode() + "', " + 
				"'" + userVO.getUserId() + "', '" + userVO.getUserId() + "'";
		if (isStaging) { 
			sql+= ", " + priorPmvId + ", '" + member.getLanguageCode() + "', '" + member.getLanguageQualifierCode() +
					"', '" + member.getRaceEthnicityCode() + "', '" + member.getPostalCode() + "'";
		}
		sql += ")";
		//POLICYMEMBERDEATHDATE, INCORRECTGENDERTYPECD left out because not in inbound member.
		// PRIORPOLICYMEMBERVERSIONID, NONCOVEREDSUBSCRIBERIND
		jdbc.execute(sql);
		return policyMemberVersionId;
	}

	public void insertPolicyMemberDate(Long pmvId, LocalDate pmsd, LocalDate pmed) {

		boolean isStaging = false;
		insertPolicyMemberDate(pmvId, pmsd, pmed, isStaging);
	}


	public void insertPolicyMemberDate(Long pmvId, LocalDate pmsd, LocalDate pmed, boolean isStaging) {

		String tableNm = isStaging ? "STAGINGPOLICYMEMBERDATE" : "POLICYMEMBERDATE";

		String sql = "Insert into " + tableNm + " ("
				+ "POLICYMEMBERVERSIONID, POLICYMEMBERSTARTDATE, POLICYMEMBERENDDATE, "
				+ "CREATEDATETIME, LASTMODIFIEDDATETIME, CREATEBY, LASTMODIFIEDBY) VALUES (" 
				+ pmvId + ", " + toDateValue(pmsd) + ", " + toDateValue(pmed)
				+ ", SYSDATE, SYSDATE, '" + userVO.getUserId() + "', '" + userVO.getUserId() + "')";
		jdbc.execute(sql);
	}

	public void insertPolicyMemberAddress(Long pmvId, String stateCd, String zipCd) {

		String sql = "Insert into POLICYMEMBERADDRESS ("
				+ "POLICYMEMBERVERSIONID, X12ADDRESSTYPECD, STATECD, ZIPPLUS4CD, "
				+ "CREATEBY, LASTMODIFIEDBY) VALUES ("
				+ pmvId + ",'1', '"  + stateCd + "', '" + zipCd + "', '" + userVO.getUserId() + "', '" + userVO.getUserId() + "')";
		jdbc.execute(sql);
	}

	public void insertMemberPolicyRaceEthnicity(Long pmvId, String raceCd) {

		String sql = "Insert into MEMBERPOLICYRACEETHNICITY (" +  
				"POLICYMEMBERVERSIONID, X12RACEETHNICITYTYPECD, CREATEBY, LASTMODIFIEDBY) VALUES ("
				+ pmvId + ", '"  + raceCd + "', '" + userVO.getUserId() + "', '" + userVO.getUserId() + "')";
		jdbc.execute(sql);
	}

	public void insertPolicyMemberLanguageAbility(Long pmvId, String langCd, String langQualCd) {

		String sql = "Insert into POLICYMEMBERLANGUAGEABILITY (POLICYMEMBERVERSIONID, X12LANGUAGETYPECD, " +
				"X12LANGUAGEQUALIFIERTYPECD, CREATEBY, LASTMODIFIEDBY) VALUES ("
				+ pmvId + ", '"  + langCd + "', '"  + langQualCd + "', '" + userVO.getUserId() + "', '" + userVO.getUserId() + "')";
		jdbc.execute(sql);
	}

	/**
	 * Inserts a minimal record into PHYSICALDOCUMENT.
	 *
	 * @return the long
	 */
	protected Long insertPhysicalDocument()  {

		Long pdId = jdbc.queryForObject("SELECT PHYSICALDOCUMENT_SEQ.NEXTVAL FROM DUAL", Long.class);

		String sql = "insert into physicaldocument (PHYSICALDOCUMENTIDENTIFIER, PHYSICALDOCUMENTDATETIME) " +
				" VALUES(" + pdId + ", SYSTIMESTAMP)";
		jdbc.execute(sql);	
		return pdId;
	}

	/**
	 * Writes null if data is null, otherwise wraps it in single quotes.
	 * @param data
	 * @return
	 */
	private String toVal(String data) {

		String value = "null";
		if (data != null) {
			value = "'" + data + "'";
		}
		return value;
	}
	public void insertPolicyMember(String stateCd, Long policyVersionId, Long policyMemberVersionId) {

		boolean isStaging = false;
		insertPolicyMember(stateCd, policyVersionId, policyMemberVersionId, isStaging);
	}

	public void insertPolicyMember(String stateCd, Long policyVersionId, Long policyMemberVersionId, boolean isStaging) {

		String tableNm = isStaging ? "STAGINGPOLICYMEMBER" : "POLICYMEMBER";

		String sql = "INSERT INTO " + tableNm + " (POLICYVERSIONID, POLICYMEMBERVERSIONID, SUBSCRIBERSTATECD) " +
				"VALUES (" + policyVersionId + ", " + policyMemberVersionId + ", '" + stateCd + "')";
		jdbc.execute(sql);
	}

	protected void insertPolicyPremium(Long policyVersionId, SBMPremium premium) {

		boolean isStaging = false;
		insertPolicyPremium(policyVersionId, premium, isStaging);
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


	/**
	 * Inserts a minimal record into POLICYSTATUS
	 */
	public void insertPolicyStatus(Long policyVersionId, LocalDateTime transDt, PolicyStatus status) {

		boolean isStaging = false;
		insertPolicyStatus(policyVersionId, transDt, status, isStaging);
	}

	public void insertPolicyStatus(Long policyVersionId, LocalDateTime transDt, PolicyStatus status, boolean isStaging) {

		String tableNm = isStaging ? "STAGINGPOLICYSTATUS" : "POLICYSTATUS";

		String sql = "INSERT INTO " + tableNm + " (POLICYVERSIONID, TRANSDATETIME, " +
				"INSURANACEPOLICYSTATUSTYPECD) VALUES (" + policyVersionId + ", " + 
				toTimestampValue(transDt) + ", '" + status.getValue() + "')";
		jdbc.execute(sql);
	}


	private String toDateValue(XMLGregorianCalendar xmlGC) {

		return toDateValue(DateTimeUtil.getLocalDateFromXmlGC(xmlGC));
	}

	private String toDateValue(LocalDate localDate) {
		return " TO_DATE('" + java.sql.Date.valueOf(localDate) + "', 'YYYY-MM-DD HH24:MI:SS')";
	}

	private String toTimestampValue(LocalDateTime ts) {
		return "TO_TIMESTAMP('" + Timestamp.valueOf(ts) + "', 'YYYY-MM-DD HH24:MI:SS.FF')";
	}

	private String getSysData() {
		return  ",'" + userVO.getUserId() + "', '" + userVO.getUserId() + "'";
	}

	private String getSysArgs() {

		return ", CREATEBY, LASTMODIFIEDBY";
	}

	protected void assertSysData(Map<String, Object> row, Long batchId) {

		String which = "";
		assertSysData(which, row, batchId);
	}

	protected void assertSysData(String which, Map<String, Object> row, Long batchId) {

		String expectedUser = batchId.toString();
		String msg = which + " ";
		assertEquals(msg + "CREATEBY", expectedUser, row.get("CREATEBY"));
		assertEquals(msg + "LASTMODIFIEDBY", expectedUser, row.get("LASTMODIFIEDBY"));
		assertNotNull(msg + "CREATEDATETIME", row.get("CREATEDATETIME"));
		assertNotNull(msg + "LASTMODIFIEDDATETIME", row.get("LASTMODIFIEDDATETIME"));
		assertEquals(msg + "CREATEDATETIME and LASTMODIFIEDDATETIME", row.get("CREATEDATETIME"), row.get("LASTMODIFIEDDATETIME"));
	}

	protected void assertSysDataAfterUpdate(String which, Map<String, Object> row, Long createdBatchId, Long lastModbatchId) {

		String expectedCreateUser = createdBatchId.toString();
		String expectedLastModUser = lastModbatchId.toString();

		String msg = which + " ";
		assertEquals(msg + "CREATEBY", expectedCreateUser, row.get("CREATEBY"));
		assertEquals(msg + "LASTMODIFIEDBY", expectedLastModUser, row.get("LASTMODIFIEDBY"));
		assertNotNull(msg + "CREATEDATETIME", row.get("CREATEDATETIME"));
		assertNotNull(msg + "LASTMODIFIEDDATETIME", row.get("LASTMODIFIEDDATETIME"));
		assertNotSame(msg + "CREATEDATETIME and LASTMODIFIEDDATETIME", row.get("CREATEDATETIME"), row.get("LASTMODIFIEDDATETIME"));
	}

}
