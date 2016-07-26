
package gov.hhs.cms.ff.fm.eps.ep.mappers;

import gov.cms.dsh.bem.BenefitEnrollmentMaintenanceType;
import gov.cms.dsh.bem.BooleanIndicatorSimpleType;
import gov.cms.dsh.bem.HealthCoveragePolicyNumberType;
import gov.cms.dsh.bem.HealthCoverageType;
import gov.cms.dsh.bem.MemberRelatedInfoType;
import gov.cms.dsh.bem.MemberType;
import gov.cms.dsh.bem.PolicyInfoType;
import gov.cms.dsh.bem.TransactionInformationType;
import gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentMaintenanceDTO;
import gov.hhs.cms.ff.fm.eps.ep.data.util.TestDataUtil;
import gov.hhs.cms.ff.fm.eps.ep.enums.EProdEnum;
import gov.hhs.cms.ff.fm.eps.ep.enums.ProcessedToDbInd;
import gov.hhs.cms.ff.fm.eps.ep.po.BatchTransMsgPO;
import gov.hhs.cms.ff.fm.eps.ep.util.EpsDateUtils;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import com.accenture.foundation.common.exception.ApplicationException;


public class BatchTransMsgMapperTest extends BaseMapperTest {


	private BatchTransMsgMapper mapper = new BatchTransMsgMapper();

	@Before
	@Override
	public void setUp() throws Exception {

		super.setUp();

	}
	
	@Test
	public void testMapDTOToPO() {

		BenefitEnrollmentMaintenanceDTO bemDTO =  new BenefitEnrollmentMaintenanceDTO();
		
		Long expectedBatchId = Long.valueOf("666666");
		Long expectedTransMsgId = Long.valueOf("777777");
		String expectedStateCd = "MN";
		String expectedExchangePolicyID = TestDataUtil.getRandomNumberAsString(9);
		String expectedIssuerHiosId = expectedExchangePolicyID.substring(0, 5);
		String expectedContractCode = expectedIssuerHiosId.concat(expectedStateCd).concat(expectedExchangePolicyID);
		String srcVerId = "555555";
		Long expectedSourceVersionId = Long.valueOf(srcVerId);
		DateTime expectedSourceVersionDateTime = JUN_1;

		bemDTO.setBatchId(expectedBatchId);
		bemDTO.setTransMsgId(expectedTransMsgId);
		
	    MemberType member = new MemberType();
	    member.setMemberInformation(new MemberRelatedInfoType());
	    member.getMemberInformation().setSubscriberIndicator(BooleanIndicatorSimpleType.Y);
	    
	    HealthCoverageType hcType = new HealthCoverageType();
	    hcType.setHealthCoveragePolicyNumber(new HealthCoveragePolicyNumberType());
	    hcType.getHealthCoveragePolicyNumber().setContractCode(expectedContractCode);
	    member.getHealthCoverage().add(hcType);
	    
		bemDTO.setBem(new BenefitEnrollmentMaintenanceType());
		bemDTO.getBem().getMember().add(member);
		bemDTO.getBem().setTransactionInformation(new TransactionInformationType());
		bemDTO.getBem().getTransactionInformation().setPolicySnapshotVersionNumber(srcVerId);
		bemDTO.getBem().getTransactionInformation().setPolicySnapshotDateTime(EpsDateUtils.getXMLGregorianCalendar(expectedSourceVersionDateTime));
		bemDTO.getBem().setPolicyInfo(new PolicyInfoType());
		bemDTO.getBem().getPolicyInfo().setGroupPolicyNumber(expectedExchangePolicyID);
		
		BatchTransMsgPO po =  mapper.mapDTOToPO(bemDTO);

		assertNotNull("BatchTransMsgPO", po);

		assertNotNull("BatchId", po.getBatchId());
		assertEquals("BatchId", expectedBatchId, po.getBatchId());
		assertNotNull("TransMsgId", po.getTransMsgId());
		assertEquals("TransMsgId", expectedTransMsgId, po.getTransMsgId());
		// Not passed when mapping due to saveTransMsg call.
		assertNull("ProcessToDBStatusTypeCd", po.getProcessedToDbStatusTypeCd());
		assertNull("TransMsgSkipReasonTypeCd", po.getTransMsgSkipReasonTypeCd());
		
		assertEquals("SubscriberStateCd", expectedStateCd, po.getSubscriberStateCd());
		assertEquals("ExchangePolicyId", expectedExchangePolicyID, po.getExchangePolicyId());
		assertEquals("IssuerHiosId", expectedIssuerHiosId, po.getIssuerHiosId());
		assertNotNull("createDateTime", po.getCreateDateTime());
		assertNotNull("lastModifiedDateTime", po.getLastModifiedDateTime());
	
		assertEquals("SourceVersionId", expectedSourceVersionId, po.getSourceVersionId());
		assertEquals("SourceVersionDateTime", expectedSourceVersionDateTime, po.getSourceVersionDateTime());
	}	
	
	@Test
	public void testMapDTOToPO_Null_SourceExchangeId() {

		BenefitEnrollmentMaintenanceDTO bemDTO =  new BenefitEnrollmentMaintenanceDTO();
		
		Long expectedBatchId = Long.valueOf("666666");
		Long expectedTransMsgId = Long.valueOf("777777");
		String expectedStateCd = "MN";
		String expectedExchangePolicyID = TestDataUtil.getRandomNumberAsString(9);
		String expectedIssuerHiosId = expectedExchangePolicyID.substring(0, 5);
		String expectedContractCode = expectedIssuerHiosId.concat(expectedStateCd).concat(expectedExchangePolicyID);
		String srcVerId = "555555";
		Long expectedSourceVersionId = Long.valueOf(srcVerId);
		DateTime expectedSourceVersionDateTime = JUN_1;

		bemDTO.setBatchId(expectedBatchId);
		bemDTO.setTransMsgId(expectedTransMsgId);
		
	    MemberType member = new MemberType();
	    member.setMemberInformation(new MemberRelatedInfoType());
	    member.getMemberInformation().setSubscriberIndicator(BooleanIndicatorSimpleType.Y);
	    
	    HealthCoverageType hcType = new HealthCoverageType();
	    hcType.setHealthCoveragePolicyNumber(new HealthCoveragePolicyNumberType());
	    hcType.getHealthCoveragePolicyNumber().setContractCode(expectedContractCode);
	    member.getHealthCoverage().add(hcType);
	    
		bemDTO.setBem(new BenefitEnrollmentMaintenanceType());
		bemDTO.getBem().getMember().add(member);
		bemDTO.getBem().setTransactionInformation(new TransactionInformationType());
		bemDTO.getBem().getTransactionInformation().setPolicySnapshotVersionNumber(srcVerId);
		bemDTO.getBem().getTransactionInformation().setPolicySnapshotDateTime(EpsDateUtils.getXMLGregorianCalendar(expectedSourceVersionDateTime));
		bemDTO.getBem().setPolicyInfo(new PolicyInfoType());
		bemDTO.getBem().getPolicyInfo().setGroupPolicyNumber(expectedExchangePolicyID);
		
		BatchTransMsgPO po =  mapper.mapDTOToPO(bemDTO);

		assertNotNull("BatchTransMsgPO", po);

		assertNotNull("BatchId", po.getBatchId());
		assertEquals("BatchId", expectedBatchId, po.getBatchId());
		assertNotNull("TransMsgId", po.getTransMsgId());
		assertEquals("TransMsgId", expectedTransMsgId, po.getTransMsgId());

		// Not passed when mapping due to saveTransMsg call.
		assertNull("ProcessToDBStatusTypeCd", po.getProcessedToDbStatusTypeCd());
		assertNull("TransMsgSkipReasonTypeCd", po.getTransMsgSkipReasonTypeCd());
		
		assertEquals("SubscriberStateCd", expectedStateCd, po.getSubscriberStateCd());
		assertEquals("ExchangePolicyId", expectedExchangePolicyID, po.getExchangePolicyId());
		assertEquals("IssuerHiosId", expectedIssuerHiosId, po.getIssuerHiosId());
		assertNotNull("createDateTime", po.getCreateDateTime());
		assertNotNull("lastModifiedDateTime", po.getLastModifiedDateTime());
	
		assertEquals("SourceVersionId", expectedSourceVersionId, po.getSourceVersionId());
		assertEquals("SourceVersionDateTime", expectedSourceVersionDateTime, po.getSourceVersionDateTime());
	}	
	
	@Test
	public void testMapDTOToPO_NullBemDTO() {
		
		BenefitEnrollmentMaintenanceDTO bemDTO = null;
		BatchTransMsgPO actual = mapper.mapDTOToPO(bemDTO);
		assertNotNull("BatchTransMsgPO", actual);
		assertNull("BatchId", actual.getBatchId());
		assertNull("TransMsgId", actual.getTransMsgId());
	}
	
	@Test
	public void testMapDTOToPO_NullSubscriber() {

		BenefitEnrollmentMaintenanceDTO bemDTO =  new BenefitEnrollmentMaintenanceDTO();
		
		Long expectedBatchId = Long.valueOf("666666");
		Long expectedTransMsgId = Long.valueOf("777777");
		String expectedStateCd = null;
		String expectedExchangePolicyID = null;
		String expectedIssuerHiosId = null;

		bemDTO.setBatchId(expectedBatchId);
		bemDTO.setTransMsgId(expectedTransMsgId);
		
	    MemberType member = new MemberType();
	    member.setMemberInformation(new MemberRelatedInfoType());
	    member.getMemberInformation().setSubscriberIndicator(BooleanIndicatorSimpleType.N);
	    
	    HealthCoverageType hcType = new HealthCoverageType();
	    hcType.setHealthCoveragePolicyNumber(new HealthCoveragePolicyNumberType());
	    hcType.getHealthCoveragePolicyNumber().setContractCode(expectedExchangePolicyID);
	    member.getHealthCoverage().add(hcType);
	    
		bemDTO.setBem(new BenefitEnrollmentMaintenanceType());
		bemDTO.getBem().setPolicyInfo(new PolicyInfoType());
		bemDTO.getBem().getPolicyInfo().setGroupPolicyNumber(expectedExchangePolicyID);
		bemDTO.getBem().getMember().add(member);
		
		BatchTransMsgPO po =  mapper.mapDTOToPO(bemDTO);

		assertNotNull("BatchTransMsgPO", po);

		assertNotNull("BatchId", po.getBatchId());
		assertEquals("BatchId", expectedBatchId, po.getBatchId());
		assertNotNull("TransMsgId", po.getTransMsgId());
		assertEquals("TransMsgId", expectedTransMsgId, po.getTransMsgId());
	
		// Set later and not in mapping.
		assertNull("ProcessToDBStatusTypeCd", po.getProcessedToDbStatusTypeCd());
		assertNull("TransMsgSkipReasonTypeCd", po.getTransMsgSkipReasonTypeCd());
		
		assertEquals("SubscriberStateCd", expectedStateCd, po.getSubscriberStateCd());
		assertEquals("ExchangePolicyId", expectedExchangePolicyID, po.getExchangePolicyId());
		assertEquals("IssuerHiosId", expectedIssuerHiosId, po.getIssuerHiosId());
		assertNotNull("createDateTime", po.getCreateDateTime());
		assertNotNull("lastModifiedDateTime", po.getLastModifiedDateTime());
		
		assertNull("SourceVersionId", po.getSourceVersionId());
		assertNull("SourceVersionDateTime", po.getSourceVersionDateTime());
	}
	
	
	@Test
	public void testMapDTOToPO_NullPolicySnapshotVersionNumber() {

		BenefitEnrollmentMaintenanceDTO bemDTO =  new BenefitEnrollmentMaintenanceDTO();
		
		Long expectedBatchId = null;
		Long expectedTransMsgId = null;
		String expectedStateCd = null;
		String expectedExchangePolicyID = null;
		String expectedIssuerHiosId = null;
		
	    MemberType member = new MemberType();
	    member.setMemberInformation(new MemberRelatedInfoType());
	    member.getMemberInformation().setSubscriberIndicator(BooleanIndicatorSimpleType.N);
	    
	    
	    HealthCoverageType hcType = new HealthCoverageType();
	    hcType.setHealthCoveragePolicyNumber(new HealthCoveragePolicyNumberType());
	    hcType.getHealthCoveragePolicyNumber().setContractCode(expectedExchangePolicyID);
	    member.getHealthCoverage().add(hcType);
	    
		bemDTO.setBem(new BenefitEnrollmentMaintenanceType());
		bemDTO.getBem().setPolicyInfo(new PolicyInfoType());
		bemDTO.getBem().getPolicyInfo().setGroupPolicyNumber(expectedExchangePolicyID);
		bemDTO.getBem().getMember().add(member);
		bemDTO.getBem().setTransactionInformation(new TransactionInformationType());
		
		BatchTransMsgPO po =  mapper.mapDTOToPO(bemDTO);

		assertNotNull("BatchTransMsgPO", po);

		assertEquals("BatchId", expectedBatchId, po.getBatchId());
		assertEquals("TransMsgId", expectedTransMsgId, po.getTransMsgId());

		// Set later and not in mapping.
		assertNull("ProcessToDBStatusTypeCd", po.getProcessedToDbStatusTypeCd());
		assertNull("TransMsgSkipReasonTypeCd", po.getTransMsgSkipReasonTypeCd());
		
		assertEquals("SubscriberStateCd", expectedStateCd, po.getSubscriberStateCd());
		assertEquals("ExchangePolicyId", expectedExchangePolicyID, po.getExchangePolicyId());
		assertEquals("IssuerHiosId", expectedIssuerHiosId, po.getIssuerHiosId());
		assertNotNull("createDateTime", po.getCreateDateTime());
		assertNotNull("lastModifiedDateTime", po.getLastModifiedDateTime());
		
		assertNull("SourceVersionId", po.getSourceVersionId());
		assertNull("SourceVersionDateTime", po.getSourceVersionDateTime());
	}
	
	
	@Test
	public void testMapDTOToPO_UpdateBatchTransMsg() {

		BenefitEnrollmentMaintenanceDTO bemDTO =  new BenefitEnrollmentMaintenanceDTO();
		
		Long expectedBatchId = Long.valueOf("666666");
		Long expectedTransMsgId = Long.valueOf("777777");
		ProcessedToDbInd expectedInd = ProcessedToDbInd.Y;

		bemDTO.setBatchId(expectedBatchId);
		bemDTO.setTransMsgId(expectedTransMsgId);
		
		BatchTransMsgPO po =  mapper.mapDTOToPO(bemDTO, expectedInd);

		assertNotNull("BatchTransMsgPO", po);
		assertNotNull("BatchId", po.getBatchId());
		assertEquals("BatchId", expectedBatchId, po.getBatchId());
		assertNotNull("TransMsgId", po.getTransMsgId());
		assertEquals("TransMsgId", expectedTransMsgId, po.getTransMsgId());
		assertEquals("ProcessToDBStatusTypeCd", expectedInd.getValue(), po.getProcessedToDbStatusTypeCd());
		assertNull("TransMsgSkipReasonTypeCd", po.getTransMsgSkipReasonTypeCd());
		assertNull("SubscriberStateCd", po.getSubscriberStateCd());
		assertNull("ExchangePolicyId",  po.getExchangePolicyId());
		assertNull("IssuerHiosId", po.getIssuerHiosId());	
		assertNull("SourceVersionId", po.getSourceVersionId());
		assertNull("SourceVersionDateTime",  po.getSourceVersionDateTime());
		assertNotNull("createDateTime", po.getCreateDateTime());
		assertNotNull("lastModifiedDateTime", po.getLastModifiedDateTime());
	}
	
	
	@Test
	public void testMapDTOToPOForSkips() {

		BenefitEnrollmentMaintenanceDTO bemDTO =  new BenefitEnrollmentMaintenanceDTO();
		
		Long expectedBatchId = Long.valueOf("666666");
		Long expectedTransMsgId = Long.valueOf("777777");
		ProcessedToDbInd expectedInd = ProcessedToDbInd.Y;
		String expectedSkipReasonCode = "EPROD-29";
		String expectedSkipReasonDesc = "Some semi-bad error.";

		bemDTO.setBatchId(expectedBatchId);
		bemDTO.setTransMsgId(expectedTransMsgId);
		
		BatchTransMsgPO po =  mapper.mapDTOToPOForSkips(bemDTO, expectedInd, expectedSkipReasonCode, expectedSkipReasonDesc);

		assertNotNull("BatchTransMsgPO", po);
		assertNotNull("BatchId", po.getBatchId());
		assertEquals("BatchId", expectedBatchId, po.getBatchId());
		assertNotNull("TransMsgId", po.getTransMsgId());
		assertEquals("TransMsgId", expectedTransMsgId, po.getTransMsgId());
		assertEquals("ProcessToDBStatusTypeCd", expectedInd.getValue(), po.getProcessedToDbStatusTypeCd());
		assertEquals("TransMsgSkipReasonTypeCd", expectedSkipReasonCode, po.getTransMsgSkipReasonTypeCd());
		assertEquals("TransMsgSkipReasonDesc", expectedSkipReasonDesc, po.getTransMsgSkipReasonDesc());
		assertNull("SubscriberStateCd", po.getSubscriberStateCd());
		assertNull("ExchangePolicyId",  po.getExchangePolicyId());
		assertNull("IssuerHiosId", po.getIssuerHiosId());	
		assertNull("SourceVersionId", po.getSourceVersionId());
		assertNull("SourceVersionDateTime",  po.getSourceVersionDateTime());
		assertNotNull("createDateTime", po.getCreateDateTime());
		assertNotNull("lastModifiedDateTime", po.getLastModifiedDateTime());
	}
	
	
	@Test
	public void testMapDTOToPOForSkips_NullBemDTO() {

		BenefitEnrollmentMaintenanceDTO bemDTO =  null;
		
		Long expectedBatchId = null;
		Long expectedTransMsgId = null;
		ProcessedToDbInd expectedInd = ProcessedToDbInd.Y;
		String expectedSkipReasonCode = "EPROD-29";
		String expectedSkipReasonDesc = "Some semi-bad error.";

		BatchTransMsgPO po =  mapper.mapDTOToPOForSkips(bemDTO, expectedInd, expectedSkipReasonCode, expectedSkipReasonDesc);

		assertNotNull("BatchTransMsgPO", po);
		assertNull("BatchId", po.getBatchId());
		assertEquals("BatchId", expectedBatchId, po.getBatchId());
		assertNull("TransMsgId", po.getTransMsgId());
		assertEquals("TransMsgId", expectedTransMsgId, po.getTransMsgId());
		assertEquals("ProcessToDBStatusTypeCd", expectedInd.getValue(), po.getProcessedToDbStatusTypeCd());
		assertEquals("TransMsgSkipReasonTypeCd", expectedSkipReasonCode, po.getTransMsgSkipReasonTypeCd());
		assertEquals("TransMsgSkipReasonDesc", expectedSkipReasonDesc, po.getTransMsgSkipReasonDesc());
		assertNull("SubscriberStateCd", po.getSubscriberStateCd());
		assertNull("ExchangePolicyId",  po.getExchangePolicyId());
		assertNull("IssuerHiosId", po.getIssuerHiosId());	
		assertNull("SourceVersionId", po.getSourceVersionId());
		assertNull("SourceVersionDateTime",  po.getSourceVersionDateTime());
		assertNotNull("createDateTime", po.getCreateDateTime());
		assertNotNull("lastModifiedDateTime", po.getLastModifiedDateTime());
	}
	
	@Test
	public void testMapDTOToPOForSkips_NullInd() {

		BenefitEnrollmentMaintenanceDTO bemDTO =  new BenefitEnrollmentMaintenanceDTO();
		
		Long expectedBatchId = Long.valueOf("666666");
		Long expectedTransMsgId = Long.valueOf("777777");
		ProcessedToDbInd expectedInd = null;
		String expectedSkipReasonCode = "EPROD-29";
		String expectedSkipReasonDesc = "Some semi-bad error.";

		bemDTO.setBatchId(expectedBatchId);
		bemDTO.setTransMsgId(expectedTransMsgId);
		
		BatchTransMsgPO po =  mapper.mapDTOToPOForSkips(bemDTO, expectedInd, expectedSkipReasonCode, expectedSkipReasonDesc);

		assertNotNull("BatchTransMsgPO", po);
		assertNotNull("BatchId", po.getBatchId());
		assertEquals("BatchId", expectedBatchId, po.getBatchId());
		assertNotNull("TransMsgId", po.getTransMsgId());
		assertEquals("TransMsgId", expectedTransMsgId, po.getTransMsgId());
		assertNull("ProcessToDBStatusTypeCd", po.getProcessedToDbStatusTypeCd());
		assertEquals("TransMsgSkipReasonTypeCd", expectedSkipReasonCode, po.getTransMsgSkipReasonTypeCd());
		assertEquals("TransMsgSkipReasonDesc", expectedSkipReasonDesc, po.getTransMsgSkipReasonDesc());
		assertNull("SubscriberStateCd", po.getSubscriberStateCd());
		assertNull("ExchangePolicyId",  po.getExchangePolicyId());
		assertNull("IssuerHiosId", po.getIssuerHiosId());	
		assertNull("SourceVersionId", po.getSourceVersionId());
		assertNull("SourceVersionDateTime",  po.getSourceVersionDateTime());
		assertNotNull("createDateTime", po.getCreateDateTime());
		assertNotNull("lastModifiedDateTime", po.getLastModifiedDateTime());
	}
	
	
	@Test
	public void testMapDTOToPOForSkips_Enum() {

		BenefitEnrollmentMaintenanceDTO bemDTO =  new BenefitEnrollmentMaintenanceDTO();
		
		Long expectedBatchId = Long.valueOf("666666");
		Long expectedTransMsgId = Long.valueOf("777777");
		ProcessedToDbInd expectedInd = ProcessedToDbInd.Y;
		EProdEnum expectedEProd = EProdEnum.EPROD_29;

		bemDTO.setBatchId(expectedBatchId);
		bemDTO.setTransMsgId(expectedTransMsgId);
		
		BatchTransMsgPO po =  mapper.mapDTOToPOForSkips(bemDTO, expectedInd, expectedEProd);

		assertNotNull("BatchTransMsgPO", po);
		assertNotNull("BatchId", po.getBatchId());
		assertEquals("BatchId", expectedBatchId, po.getBatchId());
		assertNotNull("TransMsgId", po.getTransMsgId());
		assertEquals("TransMsgId", expectedTransMsgId, po.getTransMsgId());
		assertEquals("ProcessToDBStatusTypeCd", expectedInd.getValue(), po.getProcessedToDbStatusTypeCd());
		assertEquals("TransMsgSkipReasonTypeCd", expectedEProd.getCode(), po.getTransMsgSkipReasonTypeCd());
		assertEquals("TransMsgSkipReasonDesc", expectedEProd.getDesc(), po.getTransMsgSkipReasonDesc());
		assertNull("SubscriberStateCd", po.getSubscriberStateCd());
		assertNull("ExchangePolicyId",  po.getExchangePolicyId());
		assertNull("IssuerHiosId", po.getIssuerHiosId());	
		assertNull("SourceVersionId", po.getSourceVersionId());
		assertNull("SourceVersionDateTime",  po.getSourceVersionDateTime());
		assertNotNull("createDateTime", po.getCreateDateTime());
		assertNotNull("lastModifiedDateTime", po.getLastModifiedDateTime());
	}
	
	
	@Test
	public void testMapDTOToPOForSkips_NullBemDTO_Enum() {

		BenefitEnrollmentMaintenanceDTO bemDTO =  null;
		
		Long expectedBatchId = null;
		Long expectedTransMsgId = null;
		EProdEnum expectedEProd = EProdEnum.EPROD_29;
		ProcessedToDbInd expectedInd = ProcessedToDbInd.Y;

		BatchTransMsgPO po =  mapper.mapDTOToPOForSkips(bemDTO, expectedInd, expectedEProd);

		assertNotNull("BatchTransMsgPO", po);
		assertNull("BatchId", po.getBatchId());
		assertEquals("BatchId", expectedBatchId, po.getBatchId());
		assertNull("TransMsgId", po.getTransMsgId());
		assertEquals("TransMsgId", expectedTransMsgId, po.getTransMsgId());
		assertEquals("ProcessToDBStatusTypeCd", expectedInd.getValue(), po.getProcessedToDbStatusTypeCd());
		assertEquals("TransMsgSkipReasonTypeCd", expectedEProd.getCode(), po.getTransMsgSkipReasonTypeCd());
		assertEquals("TransMsgSkipReasonDesc", expectedEProd.getDesc(), po.getTransMsgSkipReasonDesc());
		assertNull("SubscriberStateCd", po.getSubscriberStateCd());
		assertNull("ExchangePolicyId",  po.getExchangePolicyId());
		assertNull("IssuerHiosId", po.getIssuerHiosId());	
		assertNull("SourceVersionId", po.getSourceVersionId());
		assertNull("SourceVersionDateTime",  po.getSourceVersionDateTime());
		assertNotNull("createDateTime", po.getCreateDateTime());
		assertNotNull("lastModifiedDateTime", po.getLastModifiedDateTime());
	}
	
	@Test
	public void testMapDTOToPOForSkips_NullInd_Enum() {

		BenefitEnrollmentMaintenanceDTO bemDTO =  new BenefitEnrollmentMaintenanceDTO();
		
		Long expectedBatchId = Long.valueOf("666666");
		Long expectedTransMsgId = Long.valueOf("777777");
		ProcessedToDbInd expectedInd = null;
		EProdEnum expectedEProd = EProdEnum.EPROD_29;

		bemDTO.setBatchId(expectedBatchId);
		bemDTO.setTransMsgId(expectedTransMsgId);
		
		BatchTransMsgPO po =  mapper.mapDTOToPOForSkips(bemDTO, expectedInd, expectedEProd);

		assertNotNull("BatchTransMsgPO", po);
		assertNotNull("BatchId", po.getBatchId());
		assertEquals("BatchId", expectedBatchId, po.getBatchId());
		assertNotNull("TransMsgId", po.getTransMsgId());
		assertEquals("TransMsgId", expectedTransMsgId, po.getTransMsgId());
		assertNull("ProcessToDBStatusTypeCd", po.getProcessedToDbStatusTypeCd());
		assertEquals("TransMsgSkipReasonTypeCd", expectedEProd.getCode(), po.getTransMsgSkipReasonTypeCd());
		assertEquals("TransMsgSkipReasonDesc", expectedEProd.getDesc(), po.getTransMsgSkipReasonDesc());
		assertNull("SubscriberStateCd", po.getSubscriberStateCd());
		assertNull("ExchangePolicyId",  po.getExchangePolicyId());
		assertNull("IssuerHiosId", po.getIssuerHiosId());	
		assertNull("SourceVersionId", po.getSourceVersionId());
		assertNull("SourceVersionDateTime",  po.getSourceVersionDateTime());
		assertNotNull("createDateTime", po.getCreateDateTime());
		assertNotNull("lastModifiedDateTime", po.getLastModifiedDateTime());
	}
	
	
	
	@Test
	public void testMapDTOToPO_Exception() {

		String expectedError = EProdEnum.EPROD_99.getCode();
		String actualError = null;
		BenefitEnrollmentMaintenanceDTO bemDTO = new BenefitEnrollmentMaintenanceDTO();
		bemDTO.setBem(new BenefitEnrollmentMaintenanceType());
		bemDTO.getBem().setTransactionInformation(new TransactionInformationType());
		bemDTO.getBem().getTransactionInformation().setPolicySnapshotVersionNumber(TestDataUtil.getRandomNumberAsString(6) + "XXX");
		bemDTO.getBem().getTransactionInformation().setPolicySnapshotDateTime(EpsDateUtils.getXMLGregorianCalendar(FEB_1));		

		try {
			mapper.mapDTOToPO(bemDTO);
		} catch (ApplicationException appEx) {
			assertTrue("ApplicationException thrown for NumberFormatException from PolicySnapshotVersionNumber", true);
			actualError = appEx.getInformationCode();
		}
		assertEquals("Error", expectedError, actualError);
	}
}
