package gov.hhs.cms.ff.fm.eps.ep.dao.impl;

import gov.cms.dsh.bem.BenefitEnrollmentMaintenanceType;
import gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentRequestDTO;
import gov.hhs.cms.ff.fm.eps.ep.dao.TransMsgDao;
import gov.hhs.cms.ff.fm.eps.ep.data.util.TestDataUtil;
import gov.hhs.cms.ff.fm.eps.ep.enums.TxnMessageDirectionType;
import gov.hhs.cms.ff.fm.eps.ep.enums.TxnMessageType;

import java.io.StringReader;
import java.math.BigDecimal;
import java.util.Date;
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

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/eps-data-config.xml", "classpath:/test-context-data.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class TransMsgDaoImplTest extends BaseDaoTest {

	@Autowired
	private TransMsgDao transMsgDao;

	private static Unmarshaller unmarshaller;

	static {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(String.class, BenefitEnrollmentMaintenanceType.class);
			unmarshaller = jaxbContext.createUnmarshaller();

		} catch (JAXBException ex) {
			System.out.println("Unable to create Unmarshaller for unit test.  " +  ex.getMessage());
		}
	}

	@Test
	public void test_extractTransMsg_Invalid_GroupSenderId() throws JAXBException {

		Long berId = TestDataUtil.getRandomNumber(9);
		Long bemId1 = Long.valueOf("11111");
		String groupPolicyNumber = "GPN-";
		String hiosId = "11111";
		String state = "NC";
		String variantId = "01";
		String groupSenderId  = hiosId + state + "0" + hiosId.substring(0, 4) + variantId;

		BenefitEnrollmentRequestDTO berDTO = TestDataUtil.makeBenefitEnrollmentRequestDTO(berId, groupSenderId);
		BenefitEnrollmentMaintenanceType bem1 = TestDataUtil.makeBenefitEnrollmentMaintenanceType(bemId1, groupPolicyNumber + bemId1.toString());

		berDTO.getBer().getBenefitEnrollmentMaintenance().add(bem1);

		berDTO.setBerXml(TestDataUtil.getBerXMLAsString(berDTO.getBer()));
		berDTO.setTxnMessageDirectionType(TxnMessageDirectionType.INBOUND);
		berDTO.setTxnMessageType(TxnMessageType.MSG_834);
		berDTO.setSubscriberStateCd(state);
		
		// set a boogus GroupSenderId
		berDTO.getFileInformation().setGroupSenderID("XXXX");

		Long transMsgFileInfoId = insertTransMsgFileInfo(berDTO);

		berDTO.setTxnMessageFileInfoId(transMsgFileInfoId);
		berDTO.setBatchId(berId);

		transMsgDao.extractTransMsg(berDTO);

		String sql = "SELECT tm.TRANSMSGID, tm.TRANSMSGFILEINFOID, tm.PARENTTRANSMSGID, tm.TRANSMSGDATETIME, " +
				"tm.MSG.GETCLOBVAL() AS MSGXML, tm.TRANSMSGDIRECTIONTYPECD, tm.TRANSMSGTYPECD, tm.CREATEDATETIME, " +
				"tm.LASTMODIFIEDDATETIME, tm.CREATEBY, tm.LASTMODIFIEDBY, tm.SUBSCRIBERSTATECD " +
				"FROM TRANSMSG tm WHERE tm.TRANSMSGFILEINFOID = " + transMsgFileInfoId +
				// order by to get consistent query results for unit test.
				" ORDER BY tm.TRANSMSGID";

		List<Map<String, Object>> actualList = jdbc.queryForList(sql);

		assertEquals("actual TransMsg row insert", berDTO.getBer().getBenefitEnrollmentMaintenance().size(), actualList.size());

		String msg = "";
		int i = 0;
		for (Map<String, Object> row : actualList) {
			msg = "Record " + (i + 1) +") ";

			BenefitEnrollmentMaintenanceType expectedBem = berDTO.getBer().getBenefitEnrollmentMaintenance().get(i);

			assertNotNull(msg + "TransMsgId", row.get("transMsgId"));
			assertEquals(msg + "TransMsgFileInfoId", new BigDecimal(transMsgFileInfoId), row.get("transMsgFileInfoId"));
			assertNull(msg + "ParentTransMsgId (used for OUTBOUND msgs)", row.get("PARENTTRANSMSGID"));
			assertNotNull(msg + "TransMsgDateTime (SYSDATE) should not be null.", row.get("TRANSMSGDATETIME"));

			String epsXmlStr = (String) row.get("MSGXML");
			assertNotNull(msg + "BEM XML",  epsXmlStr);

			StringReader sr = new StringReader(epsXmlStr);
			JAXBElement<BenefitEnrollmentMaintenanceType> bemElement = unmarshaller.unmarshal(new StreamSource(sr), BenefitEnrollmentMaintenanceType.class);
			BenefitEnrollmentMaintenanceType actualBem = bemElement.getValue();

			assertBEM(i, expectedBem, actualBem);

			assertEquals(msg + "TransMessageDirectionType", berDTO.getTxnMessageDirectionType().getValue(), row.get("TRANSMSGDIRECTIONTYPECD"));
			assertEquals(msg + "TransMessageType", berDTO.getTxnMessageType().getValue()
					, row.get("TRANSMSGTYPECD"));

			assertEquals(msg + "CreateBy", berDTO.getBatchId().toString(), row.get("CREATEBY"));
			assertEquals(msg + "LastModifiedBy", berDTO.getBatchId().toString(), row.get("LASTMODIFIEDBY"));
			assertNotNull(msg + "CreateDateTime", (Date) row.get("createDateTime"));
			assertNotNull(msg + "LastModifiedDateTime", (Date) row.get("lastModifiedDateTime"));
			assertNull(msg + "SubscriberStateCd (extracted in SQL from GroupSenderId)", row.get("SUBSCRIBERSTATECD"));

			i++;
		}
	
	}

	/**
	 * Tests inserting and extracting BEMs from STAGINGBER.  Confirms 1 TRANSMSG record is created per BEM.
	 * - creates a BER with 3 BEMs
	 * - Inserts the BerXml data into TransMsgFileInfo and additionally into STAGINGBER as one record each
	 * - Executes insertTransMsg(berDTO)
	 * - Confirms XML extract SQL extracts from StagingBer and add TransMsg records.
	 * - Confirms ALL rows for a TransMsg record are populated (except ParentTransMsgId) from original BER.
	 * @throws JAXBException
	 */
	@Test
	public void test_extractTransMsg_HappyPath() throws JAXBException {

		Long berId = TestDataUtil.getRandomNumber(9);
		Long bemId1 = Long.valueOf("11111");
		Long bemId2 = Long.valueOf("22222");
		Long bemId3 = Long.valueOf("33333");
		String groupPolicyNumber = "GPN-";
		String hiosId = "11111";
		String state = "NC";
		String variantId = "01";
		String groupSenderId  = hiosId + state + "0" + hiosId.substring(0, 4) + variantId;

		BenefitEnrollmentRequestDTO berDTO = TestDataUtil.makeBenefitEnrollmentRequestDTO(berId, groupSenderId);
		BenefitEnrollmentMaintenanceType bem1 = TestDataUtil.makeBenefitEnrollmentMaintenanceType(bemId1, groupPolicyNumber + bemId1.toString());
		BenefitEnrollmentMaintenanceType bem2 = TestDataUtil.makeBenefitEnrollmentMaintenanceType(bemId2, groupPolicyNumber + bemId2.toString());
		BenefitEnrollmentMaintenanceType bem3 = TestDataUtil.makeBenefitEnrollmentMaintenanceType(bemId3, groupPolicyNumber + bemId3.toString());

		berDTO.getBer().getBenefitEnrollmentMaintenance().add(bem1);
		berDTO.getBer().getBenefitEnrollmentMaintenance().add(bem2);
		berDTO.getBer().getBenefitEnrollmentMaintenance().add(bem3);

		berDTO.setBerXml(TestDataUtil.getBerXMLAsString(berDTO.getBer()));
		berDTO.setTxnMessageDirectionType(TxnMessageDirectionType.INBOUND);
		berDTO.setTxnMessageType(TxnMessageType.MSG_834);
		berDTO.setSubscriberStateCd(state);

		Long transMsgFileInfoId = insertTransMsgFileInfo(berDTO);

		berDTO.setTxnMessageFileInfoId(transMsgFileInfoId);
		berDTO.setBatchId(berId);

		transMsgDao.extractTransMsg(berDTO);

		String sql = "SELECT tm.TRANSMSGID, tm.TRANSMSGFILEINFOID, tm.PARENTTRANSMSGID, tm.TRANSMSGDATETIME, " +
				"tm.MSG.GETCLOBVAL() AS MSGXML, tm.TRANSMSGDIRECTIONTYPECD, tm.TRANSMSGTYPECD, tm.CREATEDATETIME, " +
				"tm.LASTMODIFIEDDATETIME, tm.CREATEBY, tm.LASTMODIFIEDBY, tm.SUBSCRIBERSTATECD " +
				"FROM TRANSMSG tm WHERE tm.TRANSMSGFILEINFOID = " + transMsgFileInfoId +
				// order by to get consistent query results for unit test.
				" ORDER BY tm.TRANSMSGID";

		List<Map<String, Object>> actualList = jdbc.queryForList(sql);

		assertEquals("actual TransMsg row insert", berDTO.getBer().getBenefitEnrollmentMaintenance().size(), actualList.size());

		String msg = "";
		int i = 0;
		for (Map<String, Object> row : actualList) {
			msg = "Record " + (i + 1) +") ";

			BenefitEnrollmentMaintenanceType expectedBem = berDTO.getBer().getBenefitEnrollmentMaintenance().get(i);

			assertNotNull(msg + "TransMsgId", row.get("transMsgId"));
			assertEquals(msg + "TransMsgFileInfoId", new BigDecimal(transMsgFileInfoId), row.get("transMsgFileInfoId"));
			assertNull(msg + "ParentTransMsgId (used for OUTBOUND msgs)", row.get("PARENTTRANSMSGID"));
			assertNotNull(msg + "TransMsgDateTime (SYSDATE) should not be null.", row.get("TRANSMSGDATETIME"));

			String epsXmlStr = (String) row.get("MSGXML");
			assertNotNull(msg + "BEM XML",  epsXmlStr);

			StringReader sr = new StringReader(epsXmlStr);
			JAXBElement<BenefitEnrollmentMaintenanceType> bemElement = unmarshaller.unmarshal(new StreamSource(sr), BenefitEnrollmentMaintenanceType.class);
			BenefitEnrollmentMaintenanceType actualBem = bemElement.getValue();

			assertBEM(i, expectedBem, actualBem);

			assertEquals(msg + "TransMessageDirectionType", berDTO.getTxnMessageDirectionType().getValue(), row.get("TRANSMSGDIRECTIONTYPECD"));
			assertEquals(msg + "TransMessageType", berDTO.getTxnMessageType().getValue()
					, row.get("TRANSMSGTYPECD"));

			assertEquals(msg + "CreateBy", berDTO.getBatchId().toString(), row.get("CREATEBY"));
			assertEquals(msg + "LastModifiedBy", berDTO.getBatchId().toString(), row.get("LASTMODIFIEDBY"));
			assertNotNull(msg + "CreateDateTime", (Date) row.get("createDateTime"));
			assertNotNull(msg + "LastModifiedDateTime", (Date) row.get("lastModifiedDateTime"));
			assertEquals(msg + "SubscriberStateCd (extracted in SQL from GroupSenderId)", berDTO.getSubscriberStateCd(), row.get("SUBSCRIBERSTATECD"));

			i++;
		}
	}


	/**
	 * Tests inserting and extracting BEMs from STAGINGBER.  Confirms 1 TRANSMSG record is created per BEM.
	 * - Inserts the BerXml data into TransMsgFileInfo and additionally into STAGINGBER as one record each
	 * - Executes insertTransMsg(berDTO)
	 * - Confirms XML extract SQL extracts from StagingBer and add TransMsg records.
	 * - Confirms ALL rows for a TransMsg record are populated (except ParentTransMsgId) from original BER.
	 * @throws JAXBException
	 */
	@Test
	public void test_extractTransMsg_BatchId_null() throws JAXBException {

		Long berId = TestDataUtil.getRandomNumber(9);
		Long bemId1 = Long.valueOf("11111");
		Long bemId2 = Long.valueOf("22222");
		String groupPolicyNumber = "GPN-";
		String hiosId = "11111";
		String state = "NC";
		String variantId = "01";
		String groupSenderId  = hiosId + state + "0" + hiosId.substring(0, 4) + variantId;

		BenefitEnrollmentRequestDTO berDTO = TestDataUtil.makeBenefitEnrollmentRequestDTO(berId, groupSenderId);
		BenefitEnrollmentMaintenanceType bem1 = TestDataUtil.makeBenefitEnrollmentMaintenanceType(bemId1, groupPolicyNumber + bemId1.toString());
		BenefitEnrollmentMaintenanceType bem2 = TestDataUtil.makeBenefitEnrollmentMaintenanceType(bemId2, groupPolicyNumber + bemId2.toString());

		berDTO.getBer().getBenefitEnrollmentMaintenance().add(bem1);
		berDTO.getBer().getBenefitEnrollmentMaintenance().add(bem2);

		berDTO.setBerXml(TestDataUtil.getBerXMLAsString(berDTO.getBer()));
		berDTO.setTxnMessageDirectionType(TxnMessageDirectionType.INBOUND);
		berDTO.setTxnMessageType(TxnMessageType.MSG_834);
		berDTO.setSubscriberStateCd(state);

		Long transMsgFileInfoId = insertTransMsgFileInfo(berDTO);

		berDTO.setTxnMessageFileInfoId(transMsgFileInfoId);
		berDTO.setBatchId(null);

		transMsgDao.extractTransMsg(berDTO);

		String sql = "SELECT tm.TRANSMSGID, tm.TRANSMSGFILEINFOID, tm.PARENTTRANSMSGID, tm.TRANSMSGDATETIME, " +
				"tm.MSG.GETCLOBVAL() AS MSGXML, tm.TRANSMSGDIRECTIONTYPECD, tm.TRANSMSGTYPECD, tm.CREATEDATETIME, " +
				"tm.LASTMODIFIEDDATETIME, tm.CREATEBY, tm.LASTMODIFIEDBY, tm.SUBSCRIBERSTATECD " +
				"FROM TRANSMSG tm WHERE tm.TRANSMSGFILEINFOID = " + transMsgFileInfoId +
				// order by to get consistent query results for unit test.
				" ORDER BY tm.TRANSMSGID";

		List<Map<String, Object>> actualList = jdbc.queryForList(sql);

		assertEquals("actual TransMsg row insert", berDTO.getBer().getBenefitEnrollmentMaintenance().size(), actualList.size());

		String msg = "";
		int i = 0;
		for (Map<String, Object> row : actualList) {
			msg = "Record " + (i + 1) +") ";

			BenefitEnrollmentMaintenanceType expectedBem = berDTO.getBer().getBenefitEnrollmentMaintenance().get(i);

			assertNotNull(msg + "TransMsgId", row.get("transMsgId"));
			assertEquals(msg + "TransMsgFileInfoId", new BigDecimal(transMsgFileInfoId), row.get("transMsgFileInfoId"));
			assertNull(msg + "ParentTransMsgId (used for OUTBOUND msgs)", row.get("PARENTTRANSMSGID"));
			assertNotNull(msg + "TransMsgDateTime (SYSDATE) should not be null.", row.get("TRANSMSGDATETIME"));

			String epsXmlStr = (String) row.get("MSGXML");
			assertNotNull(msg + "BEM XML",  epsXmlStr);

			StringReader sr = new StringReader(epsXmlStr);
			JAXBElement<BenefitEnrollmentMaintenanceType> bemElement = unmarshaller.unmarshal(new StreamSource(sr), BenefitEnrollmentMaintenanceType.class);
			BenefitEnrollmentMaintenanceType actualBem = bemElement.getValue();

			assertBEM(i, expectedBem, actualBem);

			assertEquals(msg + "TransMessageDirectionType", berDTO.getTxnMessageDirectionType().getValue(), row.get("TRANSMSGDIRECTIONTYPECD"));
			assertEquals(msg + "TransMessageType", berDTO.getTxnMessageType().getValue()
					, row.get("TRANSMSGTYPECD"));

			assertNull(msg + "CreateBy",  row.get("CREATEBY"));
			assertNull(msg + "LastModifiedBy", row.get("LASTMODIFIEDBY"));
			assertNotNull(msg + "CreateDateTime", (Date) row.get("createDateTime"));
			assertNotNull(msg + "LastModifiedDateTime", (Date) row.get("lastModifiedDateTime"));
			assertEquals(msg + "SubscriberStateCd (extracted in SQL from GroupSenderId)", berDTO.getSubscriberStateCd(), row.get("SUBSCRIBERSTATECD"));

			i++;
		}
	}


	/** Checks at least one attribute of each composite type.
	 * @param i
	 * @param expected
	 * @param actual
	 */
	private void assertBEM(int i, BenefitEnrollmentMaintenanceType expected, BenefitEnrollmentMaintenanceType actual) {

		String msg = "BEM " + (i + 1) +") ";
		assertEquals(msg + "TransactionInfo ControlNumber", expected.getTransactionInformation().getControlNumber(), actual.getTransactionInformation().getControlNumber());
		assertEquals(msg + "IssuerType CMSPlanID", expected.getIssuer().getCMSPlanID(), actual.getIssuer().getCMSPlanID());
		assertEquals(msg + "PolicyInfo GroupPolicyNumber", expected.getPolicyInfo().getGroupPolicyNumber(), actual.getPolicyInfo().getGroupPolicyNumber());

	}

}
