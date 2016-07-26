package gov.hhs.cms.ff.fm.eps.ep.services;

import java.util.ArrayList;
import java.util.List;

import gov.hhs.cms.ff.fm.eps.ep.ErrorWarningLogDTO;
import gov.hhs.cms.ff.fm.eps.ep.po.ErrorWarningLogPO;
import gov.hhs.cms.ff.fm.eps.ep.services.impl.ErrorWarningLogServiceImpl;
import gov.hhs.cms.ff.fm.eps.ep.data.util.TestDataUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/eps-data-config.xml", "classpath:/test-context-data.xml"})
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class ErrorWarningLogServiceImplTest extends BaseServicesTest {
	
	@Autowired
	private ErrorWarningLogServiceImpl errorWarningLogService;
	

	@Before
	public void setUp() throws Exception {

	}
	
	/*
	 * Test setting minimal require data.
	 */
	@Test
	public void testWarningLogServiceTest_HappyPath() throws Exception
	{
		assertNotNull("errorWarningLogService", errorWarningLogService);
		List<ErrorWarningLogDTO> errWarnLogDTOList = new ArrayList<ErrorWarningLogDTO>();
		ErrorWarningLogDTO errWarnLogDTO = new ErrorWarningLogDTO();
		errWarnLogDTO.setBizAppAckErrorCd("E001");
		errWarnLogDTOList.add(errWarnLogDTO);
		errorWarningLogService.saveErrorWarningLogs(errWarnLogDTOList);
		
	}
	
	/*
	 * Test setting all data and verifies.  Test the following methods:
	 * - saveErrorWarningLogs
	 * - getErrorWarningLogByFileInfoId
	 * - getErrorWarningLogByTransMsgId
	 */
	@Test
	public void testWarningLogServiceTest_AllData() throws Exception
	{
		Long batchId = TestDataUtil.getRandomNumber(8);
		Long transMsgFileInfoId = insertTransMsgFileInfo();
		Long transMsgId = insertTransMsg();
		insertBatchTransMsg(transMsgId, batchId);
		
		List<ErrorWarningLogDTO> errWarnLogDTOList = new ArrayList<ErrorWarningLogDTO>();
		ErrorWarningLogDTO errWarnLogDTO = new ErrorWarningLogDTO();
		errWarnLogDTO.setErrorWarningDetailedDesc("Missing/Invalid item");
		errWarnLogDTO.setBizAppAckErrorCd("E001");
		//TODO: Remove random number added to element once XAK1ERRORWARNINGLOG constraint is removed.
		errWarnLogDTO.setErrorElement("TestElement "+TestDataUtil.getRandomNumberAsString(4));
		errWarnLogDTO.setTransMsgId(transMsgId);
		//TODO: Replace with a valid code once defined.
		//errWarnLogDTO.setProcessingErrorCd("XXX");
		errWarnLogDTO.setTransMsgFileInfoId(transMsgFileInfoId);
		errWarnLogDTO.setBatchId(batchId);
		errWarnLogDTOList.add(errWarnLogDTO);
		
		errorWarningLogService.saveErrorWarningLogs(errWarnLogDTOList);
		
		List<ErrorWarningLogPO> errWarnLogPOList = errorWarningLogService.getErrorWarningLogByTransMsgId(transMsgId);
		
		assertNotNull("ErrorWarningLogPO List", errWarnLogPOList);
		assertEquals("ErrorWarningLogPO List size", 1, errWarnLogPOList.size());
		
		ErrorWarningLogPO errWarnLogPO = errWarnLogPOList.get(0);
		
		assertNotNull("errWarnLogPO", errWarnLogPO);		
		assertEquals("DetailedDescription", errWarnLogDTO.getErrorWarningDetailedDesc(), errWarnLogPO.getErrorWarningDetailedDesc());
		assertEquals("ErrorElement", errWarnLogDTO.getErrorElement(), errWarnLogPO.getErrorElement());
		assertEquals("TransMsgId", errWarnLogDTO.getTransMsgId(), errWarnLogPO.getTransMsgID());
		assertEquals("ProcessingErrorCd", errWarnLogDTO.getProcessingErrorCd(), errWarnLogPO.getProcessingErrorCd());
		assertEquals("TransMsgFileInfoId", errWarnLogDTO.getTransMsgFileInfoId(), errWarnLogPO.getTransMsgFileInfoId());
		assertEquals("BatchId", errWarnLogDTO.getBatchId(), errWarnLogPO.getBatchId());

	}
	
	/*
	 * Test missing non-nullable field.
	 */
	@Test(expected=com.accenture.foundation.common.exception.ApplicationException.class)
	public void testWarningLogServiceTest_Exception_RequiredDataNull() throws Exception
	{
		assertNotNull("errorWarningLogService", errorWarningLogService);
		List<ErrorWarningLogDTO> errWarnLogDTOList = new ArrayList<ErrorWarningLogDTO>();
		ErrorWarningLogDTO errWarnLogDTO = new ErrorWarningLogDTO();
		errWarnLogDTOList.add(errWarnLogDTO);
		errorWarningLogService.saveErrorWarningLogs(errWarnLogDTOList);
	}

}
