package gov.hhs.cms.ff.fm.eps.ep;

import gov.hhs.cms.ff.fm.eps.ep.aop.EpsDataLogger;
import gov.hhs.cms.ff.fm.eps.ep.dao.BatchRunControlDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.BatchTransMsgDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.ErrorWarningLogDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.TransMsgFileInfoDao;
import gov.hhs.cms.ff.fm.eps.ep.enums.TxnMessageDirectionType;
import gov.hhs.cms.ff.fm.eps.ep.enums.TxnMessageType;
import gov.hhs.cms.ff.fm.eps.ep.po.BatchTransMsgPO;
import gov.hhs.cms.ff.fm.eps.ep.po.ErrorWarningLogPO;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.accenture.foundation.common.exception.ApplicationException;
import com.accenture.foundation.common.exception.EnvironmentException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/eps-data-config.xml",
"classpath:/test-context-data.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class DaoTest extends TestCase {

	//potential breaks of this unit test are
	//sequence name changes, table name changes, column name changes, or changes to reference data
	
	private final String user = "DaoTest";

	@Autowired
	TransMsgFileInfoDao transMsgFileInfoRespoitory;
	@Autowired
	JdbcTemplate jdbc;
	@Autowired 
	BatchTransMsgDao batchTransMsgDao;
	@Autowired
	BatchRunControlDao batchRunControlDao;
	@Autowired
	ErrorWarningLogDao errorWarningLogDao;
	@Autowired
	private EpsDataLogger loggingAspect;

	

	@Test
	public void errorWarningLogTest()
	{
		Long transMsgId = insertTransMsg();
		List<ErrorWarningLogPO> logPoList = new ArrayList<ErrorWarningLogPO>();
		ErrorWarningLogPO logPO = new ErrorWarningLogPO();
		logPO.setTransMsgID(transMsgId);
		logPO.setBizAppAckErrorCd("E001");
		logPO.setCreateDateTime(LocalDate.now());
		logPO.setLastModifiedDateTime(LocalDate.now());
		logPO.setCreateBy("DaoTest");
		logPO.setLastModifiedBy("DaoTest");
		logPoList.add(logPO);
		errorWarningLogDao.insertErrorWarningLogs(logPoList);
		List<ErrorWarningLogPO> actualPOList = errorWarningLogDao.getErrorWarningLogListByTransMsgId(logPO.getTransMsgID());
		assertEquals("ErrorWarningLogPO list size", 1, actualPOList.size());
		ErrorWarningLogPO actualPO = actualPOList.get(0);
		assertEquals("ErrorWarningLogPO", logPO.getBizAppAckErrorCd(), actualPO.getBizAppAckErrorCd());
	}

	@Test(expected=ApplicationException.class)
	public void failSomeInsert(){

		assertNotNull("batchTransMsgDao", batchTransMsgDao);
		BatchTransMsgPO msgPO= new BatchTransMsgPO();
		msgPO.setBatchId(1l);
		msgPO.setTransMsgId(69l);
		batchTransMsgDao.insertBatchTransMsg(msgPO);
	}
	
	@Test(expected=EnvironmentException.class)
	public void failSomeGet(){
		
		assertNotNull("batchTransMsgDao", batchTransMsgDao);
		batchTransMsgDao.getBatchTransMsgByPk(Long.valueOf("88888888888888888"), Long.valueOf("777777777777777"));
	}
	

	
	private Long insertTransMsg() {

		return insertTransMsg(TxnMessageDirectionType.INBOUND.getValue(), TxnMessageType.MSG_834.getValue(), null);
	}
	
	
	private Long insertTransMsg(String dirType, String msgType, Long parentId) {

		Long transMsgId = jdbc.queryForObject("SELECT TRANSMSGSEQ.NEXTVAL FROM DUAL", Long.class);
		jdbc.execute("INSERT INTO TRANSMSG (TRANSMSGID, PARENTTRANSMSGID, MSG, TRANSMSGDIRECTIONTYPECD, TRANSMSGTYPECD) " +
				"VALUES (" + transMsgId + ", " + parentId + ", XMLType('<" + user +">Test Data for: " +
				transMsgId +"</" + user +">'), '" + dirType + "', '" + msgType + "')");
		return transMsgId;
	}
	
	@Test
	public void batchRunControlTest() throws Exception {
	
		insertBatchRunControl();

		String preAuditExtractStatus = batchRunControlDao.getPreAuditExtractStatus();
		assertNotNull("preAuditExtractStatus is not null", preAuditExtractStatus);
		assertEquals("preAuditExtractStatus=N", "N", preAuditExtractStatus);
	}

	private void insertBatchRunControl() {

		String batchRunControlInsertSql = "MERGE INTO BATCHRUNCONTROL USING dual ON (BATCHRUNCONTROLID='JUNIT')"
				+ " WHEN NOT MATCHED THEN INSERT (BATCHRUNCONTROLID, HIGHWATERMARKSTARTDATETIME, HIGHWATERMARKENDDATETIME, BATCHSTARTDATETIME, "
				+ " PREAUDITEXTRACTCOMPLETIONIND, CREATEDATETIME, LASTMODIFIEDDATETIME, CREATEBY, LASTMODIFIEDBY) "
				+ " VALUES("
				+ "'JUNIT', SYSTIMESTAMP, SYSTIMESTAMP, SYSTIMESTAMP, 'N', SYSTIMESTAMP, SYSTIMESTAMP, 'JUNIT', 'JUNIT')";
		               	
		jdbc.execute(batchRunControlInsertSql);
	}
	
	@Test
	public void batchRunControlTest_Empty() throws Exception {
	
		jdbc.execute("DELETE FROM BATCHRUNCONTROL ");
		
		String preAuditExtractStatus = batchRunControlDao.getPreAuditExtractStatus();
		assertNull("preAuditExtractStatus is null", preAuditExtractStatus);
	}

	/**
	 * @param loggingAspect the loggingAspect to set
	 */
	public void setLoggingAspect(EpsDataLogger loggingAspect) {
		this.loggingAspect = loggingAspect;
	}
	

}
	
	