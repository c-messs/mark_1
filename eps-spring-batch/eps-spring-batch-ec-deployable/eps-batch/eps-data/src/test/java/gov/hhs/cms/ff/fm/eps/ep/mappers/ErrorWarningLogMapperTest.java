package gov.hhs.cms.ff.fm.eps.ep.mappers;

import gov.hhs.cms.ff.fm.eps.ep.ErrorWarningLogDTO;
import gov.hhs.cms.ff.fm.eps.ep.mappers.ErrorWarningLogMapper;
import gov.hhs.cms.ff.fm.eps.ep.po.ErrorWarningLogPO;

public class ErrorWarningLogMapperTest extends BaseMapperTest {
	
	
	private ErrorWarningLogMapper mapper = new ErrorWarningLogMapper();

	
	public void testErrorWarningLogMapper() {
		
		ErrorWarningLogDTO errWarnLogDTO = new ErrorWarningLogDTO();
		
		errWarnLogDTO.setBatchId(new Long("8888"));
		errWarnLogDTO.setBizAppAckErrorCd("BIZAPPACKERR code");
		errWarnLogDTO.setErrorWarningDetailedDesc("Some desc text");
		errWarnLogDTO.setErrorElement("BeeperNumber");
		errWarnLogDTO.setProcessingErrorCd("ZZ");
		errWarnLogDTO.setTransMsgFileInfoId(new Long("7777"));
		errWarnLogDTO.setTransMsgId(new Long("6666"));
		
		ErrorWarningLogPO po =  mapper.mapDTOToPO(errWarnLogDTO);
		
		assertNotNull("ErrorWarningLogPO", po);
		
		assertEquals("BatchId", errWarnLogDTO.getBatchId(), po.getBatchId());
		assertEquals("BizAppAckErrorCd", errWarnLogDTO.getBizAppAckErrorCd(), po.getBizAppAckErrorCd());
		assertEquals("ErrorWarningDetailedDesc", errWarnLogDTO.getErrorWarningDetailedDesc(), po.getErrorWarningDetailedDesc());
		assertEquals("ErrorElement", errWarnLogDTO.getErrorElement(), po.getErrorElement());
		assertEquals("ProcessingErrorCd", errWarnLogDTO.getProcessingErrorCd(), po.getProcessingErrorCd());
		assertEquals("TransMsgFileInfoId", errWarnLogDTO.getTransMsgFileInfoId(), po.getTransMsgFileInfoId());
		assertEquals("TransMsgId", errWarnLogDTO.getTransMsgId(), po.getTransMsgID());
	}
	
	public void testErrorWarningLogMapper_Null() {
		
		ErrorWarningLogDTO errWarnLogDTO = new ErrorWarningLogDTO();
		errWarnLogDTO = null;

		ErrorWarningLogPO actual = mapper.mapDTOToPO(errWarnLogDTO);
		
		
		assertNull("ErrorWarningLogDTONull", actual.getBatchId());
		assertNull("ErrorWarningLogDTONull", actual.getBizAppAckErrorCd());
		assertNull("ErrorWarningLogDTONull", actual.getErrorWarningDetailedDesc());
		assertNull("ErrorWarningLogDTONull", actual.getErrorElement());
		assertNull("ErrorWarningLogDTONull", actual.getProcessingErrorCd());
		assertNull("ErrorWarningLogDTONull", actual.getTransMsgFileInfoId());
		assertNull("ErrorWarningLogDTONull", actual.getTransMsgID());
		
		assertNotNull("checkl", actual.getCreateDateTime());
		assertNotNull("check2", actual.getLastModifiedDateTime());

	}



}
