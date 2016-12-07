package gov.hhs.cms.ff.fm.eps.ep.sbm.dao.impl;

import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import gov.cms.dsh.sbmi.Enrollment;
import gov.cms.dsh.sbmi.PolicyType;
import gov.hhs.cms.ff.fm.eps.ep.dao.SbmTransMsgDao;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmTransMsgPO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileProcessingDTO;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;
import gov.hhs.cms.ff.fm.eps.ep.util.sbm.TestDataSBMUtility;
import gov.hhs.cms.ff.fm.eps.ep.vo.UserVO;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/sbmi-data-config.xml", "classpath:/test-context-data.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class SbmTransMsgDaoImplTest extends BaseSBMDaoTest {


	@Autowired
	private SbmTransMsgDao sbmTransMsgDao;
	
	@Autowired
	private UserVO userVO;
	
	
	private static Unmarshaller unmarshaller;

	static {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(String.class, Enrollment.class);
			unmarshaller = jaxbContext.createUnmarshaller();

		} catch (JAXBException ex) {
			System.out.println("Unable to create Unmarshaller for unit test.  " +  ex.getMessage());
		}
	}


	@Test
	public void test_insertSbmTransMsg() throws JAXBException  {

		int expectedListSize = 1;
		String issuerId = "88888";
		String tenantId = "CA0";
		int rcn = 1;
		String exchangePolicyId = "0000001";
		String fileId = "FID-00001";
		String qhpId = TestDataSBMUtility.makeQhpId(issuerId, tenantId);
		SBMFileProcessingDTO fileDTO = insertParentFileRecords(tenantId, fileId);
		Long batchId = TestDataSBMUtility.getRandomNumberAsLong(7);
		fileDTO.setBatchId(batchId);
		
		// set in calling composite Daos.
		userVO.setUserId(batchId.toString());
		
		PolicyType expectedPolicy = TestDataSBMUtility.makePolicyType(rcn, qhpId, exchangePolicyId);
		String expectedPolicyXML = TestDataSBMUtility.getPolicyAsXmlString(expectedPolicy);
		
		// insert XPR (policy) XML into staging.
		Long stagingSbmPolicyId = insertStagingSbmPolicy(fileDTO, expectedPolicyXML);

		SbmTransMsgPO expected = makeSbmTransMsgPO(fileDTO.getSbmFileInfo().getSbmFileInfoId());
		expected.setMsg(expectedPolicyXML);

		Long sbmTransMsgId = sbmTransMsgDao.insertSbmTransMsg(stagingSbmPolicyId, expected);
		assertNotNull("SbmTransMsgId", sbmTransMsgId);

		String sql = "SELECT stm.SBMTRANSMSGID, stm.TRANSMSGDATETIME, stm.MSG.GETCLOBVAL() AS MSGXML, stm.TRANSMSGDIRECTIONTYPECD, stm.TRANSMSGTYPECD, " +
				"stm.CREATEDATETIME, stm.LASTMODIFIEDDATETIME, stm.CREATEBY, stm.LASTMODIFIEDBY, stm.SUBSCRIBERSTATECD, stm.SBMFILEINFOID, stm.RECORDCONTROLNUM, " +
				"stm.PLANID, stm.SBMTRANSMSGPROCSTATUSTYPECD, stm.EXCHANGEASSIGNEDPOLICYID FROM SBMTRANSMSG stm WHERE stm.SBMTRANSMSGID = " + sbmTransMsgId;

		List<Map<String, Object>> actualList = jdbc.queryForList(sql);
		assertEquals("SBMTRANSMSG record list size", expectedListSize, actualList.size());

		Map<String, Object> actual = actualList.get(0);

		assertEquals("TransMsgDateTime", Timestamp.valueOf(expected.getTransMsgDateTime()), (Timestamp) actual.get("TRANSMSGDATETIME"));
		assertEquals("TransMsgDirectionTypeCd", expected.getTransMsgDirectionTypeCd(), actual.get("TRANSMSGDIRECTIONTYPECD"));
		assertEquals("TransMsgTypeCd", expected.getTransMsgTypeCd(), actual.get("TRANSMSGTYPECD"));
		assertEquals("SbmFileInfoId", new BigDecimal(expected.getSbmFileInfoId()), actual.get("SBMFILEINFOID"));
		assertEquals("SubscriberStateCd", expected.getSubscriberStateCd(), actual.get("SUBSCRIBERSTATECD"));
		assertEquals("RecordControlNum", new BigDecimal(expected.getRecordControlNum()), actual.get("RECORDCONTROLNUM"));
		assertEquals("PlanId", expected.getPlanId(), actual.get("PLANID"));
		assertEquals("SbmTransMsgProcStatusTypeCd", expected.getSbmTransMsgProcStatusTypeCd(), actual.get("SBMTRANSMSGPROCSTATUSTYPECD"));
		assertEquals("ExchangeAssignedPolicyId", expected.getExchangeAssignedPolicyId(), actual.get("EXCHANGEASSIGNEDPOLICYID"));
		assertSysData(actual, batchId);
		
		// Since the marshalled format is slightly different that the inserted format,
		// go ahead and marshall and compare some values to confirm policy from staging is the same as the one in SbmTransMsg.
		StringReader sr = new StringReader((String) actual.get("MSGXML"));
		JAXBElement<PolicyType> element = unmarshaller.unmarshal(new StreamSource(sr), PolicyType.class);
		PolicyType actualPolicy = element.getValue();
		
		assertEquals("RecordControlNumber", expectedPolicy.getRecordControlNumber(), actualPolicy.getRecordControlNumber());
		assertEquals("QHPId", expectedPolicy.getQHPId(), actualPolicy.getQHPId());
		assertEquals("ExchangeAssignedPolicyId", expectedPolicy.getExchangeAssignedPolicyId(), actualPolicy.getExchangeAssignedPolicyId());
		assertEquals("IssuerAssignedPolicyId", expectedPolicy.getIssuerAssignedPolicyId(), actualPolicy.getIssuerAssignedPolicyId());
		assertEquals("PolicyStartDate", DateTimeUtil.getLocalDateFromXmlGC(expectedPolicy.getPolicyStartDate()), 
				DateTimeUtil.getLocalDateFromXmlGC(actualPolicy.getPolicyStartDate()));
		assertEquals("PolicyEndDate", DateTimeUtil.getLocalDateFromXmlGC(expectedPolicy.getPolicyEndDate()), 
				DateTimeUtil.getLocalDateFromXmlGC(actualPolicy.getPolicyEndDate()));

	}


}
