package gov.hhs.cms.ff.fm.eps.ep.data.util;

import gov.cms.dsh.bem.GenderCodeSimpleType;
import gov.cms.dsh.bem.InsuranceLineCodeSimpleType;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyMemberAddressPO;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyMemberDatePO;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyMemberPO;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyMemberVersionPO;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyVersionPO;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author j.radziewski
 * 
 * The class contains methods for creating FULL EPS policy and member data prior to EPM_25.0 release.
 * This data is used for testing post-25.0 policies (less many tables and attributes) against
 * existing pre-25.0 EPS policies with nearly all policy and member attributes populated.
 *
 */
public class TestEPSPre25Data {

	private JdbcTemplate jdbc;

	protected static final LocalDate DATE = LocalDate.now();
	protected static final LocalDateTime DATETIME = LocalDateTime.now();
	protected static final int YEAR = DATE.getYear();


	public final int SLEEP_INTERVAL = 1050;
	
	protected final LocalDate JAN_1 = LocalDate.of(YEAR, 1, 1);
	protected final LocalDate FEB_1 = LocalDate.of(YEAR, 2, 1);
	protected final LocalDate MAR_1 = LocalDate.of(YEAR, 3, 1);
	protected final LocalDate APR_1 = LocalDate.of(YEAR, 4, 1);
	protected final LocalDate MAY_1 = LocalDate.of(YEAR, 5, 1);
	/*
	 * Make a minimal PolicyVersion
	 */
	public PolicyVersionPO makePolicyVersion(Long bemId, String stateCd, String exchangePolicyId, LocalDateTime msd) {

		String strBemId = bemId.toString();
		Long transMsgId = insertTransMsg();
		PolicyVersionPO po = new PolicyVersionPO();

		po.setSubscriberStateCd(stateCd);
		po.setExchangePolicyId(exchangePolicyId);
		po.setMaintenanceStartDateTime(msd.minusNanos(1000));
		po.setMaintenanceEndDateTime(DateTimeUtil.HIGHDATE);
		po.setIssuerPolicyId(strBemId.substring(0, 8));
		po.setIssuerHiosId(strBemId.substring(0, 5));	
		po.setIssuerTaxPayerId("ISSTAXID-" + strBemId);
		po.setIssuerNm("ISSNM-" + strBemId);
		po.setIssuerSubscriberID("SUB-" + strBemId.substring(0, 8));

		po.setTransDateTime(msd);
		po.setTransControlNum(strBemId);
		po.setX12InsrncLineTypeCd(InsuranceLineCodeSimpleType.HLT.value());
		po.setTransMsgID(transMsgId);
		po.setPolicyStartDate(APR_1);		
		po.setCreateDateTime(LocalDate.now());
		po.setLastModifiedDateTime(LocalDate.now());
		po.setCreateBy("TestEPSPre25");
		po.setLastModifiedBy("TestEPSPre25");

		return po;	
	}

	/*
	 * Make a minimal PolicyMemberPO
	 */
	public PolicyMemberPO makePolicyMember(Long pvId, Long pmvId) {

		PolicyMemberPO po = new PolicyMemberPO();

		po.setPolicyVersionId(pvId);
		po.setPolicyMemberVersionId(pmvId);
		po.setSubscriberStateCd("VA");

		return po;	
	}

	public PolicyMemberVersionPO makePolicyMemberVersion(String exchangeMemId, LocalDateTime maintStart, String exchangePolicyId, String subscriberStateCd) {

		PolicyMemberVersionPO po = new PolicyMemberVersionPO();
		po.setExchangePolicyId(exchangePolicyId);
		po.setExchangeMemberID(exchangeMemId);
		po.setMaintenanceStartDateTime(maintStart);
		po.setSubscriberStateCd(subscriberStateCd);
		po.setX12GenderTypeCd(GenderCodeSimpleType.M.value());
		po.setCreateDateTime(LocalDate.now());
		po.setLastModifiedDateTime(LocalDate.now());
		po.setCreateBy("TestEPSPre25");
		po.setLastModifiedBy("TestEPSPre25");

		return po;
	}

	public Long insertTransMsg() {

		Long transMsgId = jdbc.queryForObject("SELECT TRANSMSGSEQ.NEXTVAL FROM DUAL", Long.class);
		jdbc.execute("INSERT INTO TRANSMSG (TRANSMSGID, MSG, TRANSMSGDIRECTIONTYPECD, TRANSMSGTYPECD, " +
				"CREATEDATETIME, LASTMODIFIEDDATETIME) VALUES (" + transMsgId + ", XMLType('<Test>Data for TransMsgId: " +
				transMsgId +"</Test>'), '1', '1', SYSDATE, SYSDATE)");
		return transMsgId;
	}

	/**
	 * Inserts a maximum record into POLICYVERSION to simulate pre-EPS.25.0 data.
	 *
	 * @return policyVersionId
	 */
	public Long insertPolicyVersion(PolicyVersionPO po) {

		Long policyVersionId = jdbc.queryForObject("SELECT POLICYVERSIONSEQ.NEXTVAL FROM DUAL", Long.class);

		String sql = "Insert into POLICYVERSION (POLICYVERSIONID,SUBSCRIBERSTATECD,EXCHANGEPOLICYID,MAINTENANCESTARTDATETIME,"
				+ "MAINTENANCEENDDATETIME,ISSUERPOLICYID,ISSUERHIOSID,ISSUERTAXPAYERID,ISSUERNM,ISSUERSUBSCRIBERID,"
				+ "EXCHANGEASSIGNEDSUBSCRIBERID,TRANSDATETIME,TRANSCONTROLNUM,ELIGIBILITYSTARTDATE,ELIGIBILITYENDDATE,"
				+ "SPECIALENROLLMENTPERIODREASONT,SOURCEEXCHANGEID,PREMIUMPAIDTOENDDATE,LASTPREMIUMPAIDDATE,PAYMENTTRANSID,"
				+ "COBRAQUALIFYINGEVENTDATE,PLANID,EMPLOYERGROUPNUM,X12INSRNCLINETYPECD,INSRNCAPLCTNTYPECD,EMPLOYERIDENTIFICATIONNUM,"
				+ "SPONSORNM,SPONSORSSN,SPONSOROTHERID,TRANSMSGID,X12COBRAQUALIFYINGEVENTTYPECD,AGENTTAXPAYERID,AGENTNM,AGENTNPNNUM,"
				+ "AGENTACCOUNTNUM,BROKERNM,BROKERNPNNUM,BROKERTAXPAYERID,BROKERACCOUNTNUM,X12EMPLOYMENTSTATUSTYPECD,CHANGEREPORTEDDATE,"
				+ "X12COVERAGELEVELTYPECD,X12TRANSTYPECD,CREATEDATETIME,LASTMODIFIEDDATETIME,CREATEBY,LASTMODIFIEDBY,POLICYSTARTDATE,"
				+ "POLICYENDDATE,SOURCEVERSIONID,SOURCEVERSIONDATETIME,HEALTHCOVGMAINTEFFECTIVEDATE,MARKETPLACEGROUPPOLICYID)"
				+ " values ("
				+ policyVersionId + ", '" + po.getSubscriberStateCd() + "', '" + po.getExchangePolicyId() + "', " 
				+ toTimestampValue(po.getMaintenanceStartDateTime()) + ", " + toTimestampValue(po.getMaintenanceEndDateTime())
				+ ", '" + po.getIssuerPolicyId() + "', '"+ po.getIssuerHiosId() + "', '" + po.getIssuerTaxPayerId() + "', '"
				+ po.getIssuerNm() + "', '" + po.getIssuerSubscriberID() + "', '" + po.getExchangeAssignedSubscriberID() + "', " 
				+ toTimestampValue(po.getTransDateTime()) + ", '" + po.getTransControlNum() + "', "	+ toDateValue(po.getEligibilityStartDate())	+ ", "
				+ toDateValue(po.getEligibilityEndDate()) + ", 'SEPR', 'SRCEXID', " + toDateValue(po.getPremiumPaidToEndDate()) + ", "
				+ toDateValue(po.getLastPremiumPaidDate()) + ", null, " + toDateValue(APR_1) + ", '" + po.getPlanID() + "', '" 
				+ "EMPGRPNUM', '" + po.getX12InsrncLineTypeCd() + "', '1', 'EMPIDNUM', 'SPONSORNM', 'SPONSRSSN', 'SPONSOROTHID', " 
				+ po.getTransMsgID() + ", '1', 'AGTAXID', 'AGNM', 'AGNPN', 'AGACCTNUM', 'BRKNM', 'BRKNPN', 'BRKTAXID', 'BRKACCTNUM', 'AC', " 
				+ toDateValue(FEB_1) + ", 'FAM', 'EnrollmentMaintenance', "  + toDateValue(po.getCreateDateTime()) + ", "		
				+ toDateValue(po.getLastModifiedDateTime()) + ", 'Pre25Data', 'Pre25Data', " + toDateValue(po.getPolicyStartDate()) + ", "
				+ toDateValue(po.getPolicyEndDate()) + ", 1, " + toTimestampValue(po.getSourceVersionDateTime()) + ", " + toDateValue(MAR_1) + ", null)";

		jdbc.execute(sql);
		return policyVersionId;
	}

	/**
	 * Inserts a maximum record into POLICYMEMBERVERSION to simulate pre-EPS.25.0 data.
	 *
	 * @param po
	 * @return
	 */
	public Long insertPolicyMemberVersion(PolicyMemberVersionPO po) {

		Long pmvId = jdbc.queryForObject("SELECT POLICYMEMBERVERSIONSEQ.NEXTVAL FROM DUAL", Long.class);

		String sql = "INSERT INTO POLICYMEMBERVERSION "
				+ "(POLICYMEMBERVERSIONID, EXCHANGEPOLICYID, SUBSCRIBERSTATECD, EXCHANGEMEMBERID, MAINTENANCESTARTDATETIME, "
				+ "MAINTENANCEENDDATETIME,ISSUERASSIGNEDMEMBERID,POLICYMEMBERELIGSTARTDATE,POLICYMEMBERELIGENDDATE,SUBSCRIBERIND, "
				+ "POLICYMEMBERBIRTHDATE, POLICYMEMBERLASTNM, POLICYMEMBERFIRSTNM, POLICYMEMBERMIDDLENM, "
				+ "POLICYMEMBERSALUTATIONNM, POLICYMEMBERSUFFIXNM, POLICYMEMBERSSN, X12RELATIONSHIPTYPECD,X12TOBACCOUSETYPECD, "
				+ "TRANSMSGID, TOBACCOCESSATIONIND, X12GENDERTYPECD, X12MARITALSTATUSTYPECD, X12BENEFITSTATUSTYPECD, "
				+ "CREATEDATETIME, LASTMODIFIEDDATETIME, CREATEBY, LASTMODIFIEDBY, INCORRECTLASTNM, "
				+ "INCORRECTBIRTHDATE, INCORRECTGENDERTYPECD, INCORRECTMARITALSTATUSTYPECD) "
				+ " VALUES (" + pmvId + ", '"+ po.getExchangePolicyId() + "', '" + po.getSubscriberStateCd() + "', '" 
				+ po.getExchangeMemberID() + "', " + toTimestampValue(po.getMaintenanceStartDateTime()) + ", " 
				+ toTimestampValue(DateTimeUtil.HIGHDATE) + ",'" + po.getIssuerAssignedMemberID() + "', " + toDateValue(po.getPolicyMemberEligStartDate()) 
				+ ", " + toDateValue(po.getPolicyMemberEligEndDate()) + ", 'Y', " + toDateValue(po.getPolicyMemberBirthDate())
				+ ", '" + po.getPolicyMemberLastNm() + "', '" + po.getPolicyMemberFirstNm() + "', '" + po.getPolicyMemberMiddleNm()
				+ "', '" + po.getPolicyMemberSalutationNm() + "', '" + po.getPolicyMemberSuffixNm() + "', '" + po.getPolicyMemberSSN()
				+ "', '18', 'T', " + po.getTransMsgID() + ", null,'M', 'I', 'A'"
				+ ", SYSDATE, SYSDATE, 'Pre25Data', 'Pre25Data', 'INCLASTNAME'"
				+ ", null, null, null)";

				jdbc.execute(sql);
				return pmvId;
	}
	
	public void insertPolicyMemberAddress(PolicyMemberAddressPO po) {

		String sql = "Insert into POLICYMEMBERADDRESS ("
				+ "POLICYMEMBERVERSIONID, X12ADDRESSTYPECD, STREETNM1, STREETNM2, CITYNM,"
				+ " STATECD, COUNTRYCD, COUNTYNM, ZIPPLUS4CD, "
				+ " CREATEDATETIME, LASTMODIFIEDDATETIME, CREATEBY, LASTMODIFIEDBY) values ("
				+ po.getPolicyMemberVersionId() + ",'1', 'STREETNM1', 'STREETNM2', 'CITYNM' "
				+",'"  + po.getStateCd() + "','999','COUNTYNM', '" + po.getZipPlus4Cd()
				+ "', SYSDATE, SYSDATE, 'Pre25Data', 'Pre25Data')";
		jdbc.execute(sql);
	}
	
	public void insertPolicyMemberDate(PolicyMemberDatePO po) {

		String sql = "Insert into POLICYMEMBERDATE ("
				+ "POLICYMEMBERVERSIONID, POLICYMEMBERSTARTDATE, POLICYMEMBERENDDATE, "
				+ "CREATEDATETIME, LASTMODIFIEDDATETIME, CREATEBY, LASTMODIFIEDBY) values (" 
				+ po.getPolicyMemberVersionId() + ", " + toDateValue(po.getPolicyMemberStartDate()) + ", " + toDateValue(po.getPolicyMemberEndDate())
				+ ", SYSDATE, SYSDATE, 'Pre25Data', 'Pre25Data')";
		jdbc.execute(sql);
	}
	

	public void insertPolicyMember(PolicyMemberPO po) {

		String sql = "INSERT INTO POLICYMEMBER (POLICYVERSIONID, POLICYMEMBERVERSIONID, SUBSCRIBERSTATECD, " +
				"CREATEDATETIME, LASTMODIFIEDDATETIME, CREATEBY, LASTMODIFIEDBY) " +
				"VALUES (" + po.getPolicyVersionId() + ", " + po.getPolicyMemberVersionId() + ", '" + po.getSubscriberStateCd() + "', " +
				"SYSDATE, SYSDATE, 'Pre25Data', 'Pre25Data')";
		jdbc.execute(sql);
	}

	private String toDateValue(LocalDate dt) {
		return " TO_DATE('" + dt.toString() + "', 'YYYY-MM-DD')";
	}
	
	private String toTimestampValue(LocalDateTime ts) {

		DateTimeFormatter sqlDf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"); 

		return "TO_TIMESTAMP('" + ts.format(sqlDf) + "', 'YYYY-MM-DD HH24:MI:SS.FF')";
	}
	
	public void setJdbc(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}
}
