package gov.hhs.cms.ff.fm.eps.ep.sbm.dao.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import gov.hhs.cms.ff.fm.eps.ep.dao.StagingSbmGroupLockDao;
import gov.hhs.cms.ff.fm.eps.ep.po.StagingSbmGroupLockPO;
import gov.hhs.cms.ff.fm.eps.ep.util.sbm.TestDataSBMUtility;
import gov.hhs.cms.ff.fm.eps.ep.vo.UserVO;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/sbmi-data-config.xml", "classpath:/test-context-data.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class StagingSbmGroupLockDaoImplTest extends BaseSBMDaoTest {
	
	@Autowired
	private StagingSbmGroupLockDao stagingSbmGroupLockDao;
	
	@Autowired
	private UserVO userVO;

	
	@Test
	public void test_insertStagingSbmGroupLock() {

		assertNotNull("stagingSbmGroupLockDao", stagingSbmGroupLockDao);
		userVO.setUserId("StagSbmGrpLockDaoTest");
		String stateCd = TestDataSBMUtility.getRandomSbmState();
		
		Long expectedProcGrpId = TestDataSBMUtility.getRandomNumberAsLong(4); 
		
		Long sbmFileProcSumId = insertSBMFileProcessingSummary(stateCd + "0");
		
		List<StagingSbmGroupLockPO> poList = new ArrayList<StagingSbmGroupLockPO>();
		StagingSbmGroupLockPO po = new StagingSbmGroupLockPO();
		po.setSbmFileProcSumId(sbmFileProcSumId);
		po.setProcessingGroupId(expectedProcGrpId);
		
		poList.add(po);

		stagingSbmGroupLockDao.insertStagingSbmGroupLock(poList);

		String sql = "SELECT * FROM STAGINGSBMGROUPLOCK WHERE SBMFILEPROCESSINGSUMMARYID = " + sbmFileProcSumId;

		List<Map<String, Object>> actualList = jdbc.queryForList(sql);
		assertEquals("STAGINGSBMPOLICY list size", 1, actualList.size());

		Map<String, Object> row = actualList.get(0);

		assertEquals("PROCESSINGGROUPID ", new BigDecimal(expectedProcGrpId), row.get("PROCESSINGGROUPID"));
		assertNull("BATCHID should be null", row.get("BATCHID"));
		assertSysData(row);
	}
	
	@Test
	public void test_updateStagingGroupLock() {

		assertNotNull("stagingSbmGroupLockDao", stagingSbmGroupLockDao);
		userVO.setUserId("StagSbmGrpLockDaoTest");
		String stateCd = TestDataSBMUtility.getRandomSbmState();
		
		Long expectedProcGrpId = 0L; 
		
		Long sbmFileProcSumId = insertSBMFileProcessingSummary(stateCd + "0");
		
		List<StagingSbmGroupLockPO> poList = new ArrayList<StagingSbmGroupLockPO>();
		StagingSbmGroupLockPO po = new StagingSbmGroupLockPO();
		po.setSbmFileProcSumId(sbmFileProcSumId);
		po.setProcessingGroupId(expectedProcGrpId);
		
		poList.add(po);

		stagingSbmGroupLockDao.insertStagingSbmGroupLock(poList);

		String sql = "SELECT * FROM STAGINGSBMGROUPLOCK WHERE SBMFILEPROCESSINGSUMMARYID = " + sbmFileProcSumId;

		List<Map<String, Object>> actualList = jdbc.queryForList(sql);
		assertEquals("STAGINGSBMPOLICY list size", 1, actualList.size());

		Map<String, Object> row = actualList.get(0);

		assertEquals("PROCESSINGGROUPID ", new BigDecimal(expectedProcGrpId), row.get("PROCESSINGGROUPID"));
		assertNull("BATCHID should be null", row.get("BATCHID"));
		assertSysData(row);
		
		
		boolean updated = stagingSbmGroupLockDao.updateStagingGroupLock(sbmFileProcSumId, 9999L);
		assertTrue("updated", updated);
		
		sql = "SELECT * FROM STAGINGSBMGROUPLOCK WHERE SBMFILEPROCESSINGSUMMARYID = " + sbmFileProcSumId;

		List<Map<String, Object>> updatedList = jdbc.queryForList(sql);
		assertEquals("STAGINGSBMPOLICY list size", 1, updatedList.size());

		Map<String, Object> updateRow = updatedList.get(0);

		assertEquals("PROCESSINGGROUPID ", new BigDecimal(expectedProcGrpId), updateRow.get("PROCESSINGGROUPID"));
		assertNotNull("BATCHID should not be null", updateRow.get("BATCHID"));
		assertEquals("BATCHID should be 9999", 9999, ((BigDecimal)updateRow.get("BATCHID")).longValue());
		assertSysData(updateRow);
		
	}
	
	@Test
	public void test_updateStagingGroupLockForExtract() {

		assertNotNull("stagingSbmGroupLockDao", stagingSbmGroupLockDao);
		userVO.setUserId("StagSbmGrpLockDaoTest");
		String stateCd = TestDataSBMUtility.getRandomSbmState();
		
		Long expectedProcGrpId = 1L; 
		
		Long sbmFileProcSumId = insertSBMFileProcessingSummary(stateCd + "0");
		
		List<StagingSbmGroupLockPO> poList = new ArrayList<StagingSbmGroupLockPO>();
		StagingSbmGroupLockPO po = new StagingSbmGroupLockPO();
		po.setSbmFileProcSumId(sbmFileProcSumId);
		po.setProcessingGroupId(expectedProcGrpId);
		
		poList.add(po);

		stagingSbmGroupLockDao.insertStagingSbmGroupLock(poList);

		String sql = "SELECT * FROM STAGINGSBMGROUPLOCK WHERE SBMFILEPROCESSINGSUMMARYID = " + sbmFileProcSumId;

		List<Map<String, Object>> actualList = jdbc.queryForList(sql);
		assertEquals("STAGINGSBMPOLICY list size", 1, actualList.size());

		Map<String, Object> row = actualList.get(0);

		assertEquals("PROCESSINGGROUPID ", new BigDecimal(expectedProcGrpId), row.get("PROCESSINGGROUPID"));
		assertNull("BATCHID should be null", row.get("BATCHID"));
		assertSysData(row);
		
		
		boolean updated = stagingSbmGroupLockDao.updateStagingGroupLockForExtract(9999L);
		assertTrue("updated", updated);
		
		sql = "SELECT * FROM STAGINGSBMGROUPLOCK WHERE SBMFILEPROCESSINGSUMMARYID = " + sbmFileProcSumId;

		List<Map<String, Object>> updatedList = jdbc.queryForList(sql);
		assertEquals("STAGINGSBMPOLICY list size", 1, updatedList.size());

		Map<String, Object> updateRow = updatedList.get(0);

		assertEquals("PROCESSINGGROUPID ", new BigDecimal(expectedProcGrpId), updateRow.get("PROCESSINGGROUPID"));
		assertNotNull("BATCHID should not be null", updateRow.get("BATCHID"));
		assertEquals("BATCHID should be 9999", 9999, ((BigDecimal)updateRow.get("BATCHID")).longValue());
		assertSysData(updateRow);
		
	}
	
	@Test
	public void test_deleteStagingGroupLock() {

		assertNotNull("stagingSbmGroupLockDao", stagingSbmGroupLockDao);
		userVO.setUserId("StagSbmGrpLockDaoTest");
		String stateCd = TestDataSBMUtility.getRandomSbmState();
		
		Long expectedProcGrpId = 0L; 
		
		Long sbmFileProcSumId = insertSBMFileProcessingSummary(stateCd + "0");
		
		List<StagingSbmGroupLockPO> poList = new ArrayList<StagingSbmGroupLockPO>();
		StagingSbmGroupLockPO po = new StagingSbmGroupLockPO();
		po.setSbmFileProcSumId(sbmFileProcSumId);
		po.setProcessingGroupId(expectedProcGrpId);
		
		poList.add(po);

		stagingSbmGroupLockDao.insertStagingSbmGroupLock(poList);

		String sql = "SELECT * FROM STAGINGSBMGROUPLOCK WHERE SBMFILEPROCESSINGSUMMARYID = " + sbmFileProcSumId;

		List<Map<String, Object>> actualList = jdbc.queryForList(sql);
		assertEquals("STAGINGSBMPOLICY list size", 1, actualList.size());

		Map<String, Object> row = actualList.get(0);

		assertEquals("PROCESSINGGROUPID ", new BigDecimal(expectedProcGrpId), row.get("PROCESSINGGROUPID"));
		assertNull("BATCHID should be null", row.get("BATCHID"));
		assertSysData(row);
		
		
		boolean updated = stagingSbmGroupLockDao.updateStagingGroupLock(sbmFileProcSumId, 9999L);
		assertTrue("updated", updated);
		
		sql = "SELECT * FROM STAGINGSBMGROUPLOCK WHERE SBMFILEPROCESSINGSUMMARYID = " + sbmFileProcSumId;

		List<Map<String, Object>> updatedList = jdbc.queryForList(sql);
		assertEquals("STAGINGSBMPOLICY list size", 1, updatedList.size());

		Map<String, Object> updateRow = updatedList.get(0);

		assertEquals("PROCESSINGGROUPID ", new BigDecimal(expectedProcGrpId), updateRow.get("PROCESSINGGROUPID"));
		assertNotNull("BATCHID should not be null", updateRow.get("BATCHID"));
		assertEquals("BATCHID should be 9999", 9999, ((BigDecimal)updateRow.get("BATCHID")).longValue());
		assertSysData(updateRow);
		
		int deletedCount = stagingSbmGroupLockDao.deleteStagingGroupLock(sbmFileProcSumId, 9999L);
		
		assertEquals("deleted count", 1, deletedCount);
	}
	
	@Test
	public void test_deleteStagingGroupLockForExtract() {

		assertNotNull("stagingSbmGroupLockDao", stagingSbmGroupLockDao);
		userVO.setUserId("StagSbmGrpLockDaoTest");
		String stateCd = TestDataSBMUtility.getRandomSbmState();
		
		Long expectedProcGrpId = 1L; 
		
		Long sbmFileProcSumId = insertSBMFileProcessingSummary(stateCd + "0");
		
		List<StagingSbmGroupLockPO> poList = new ArrayList<StagingSbmGroupLockPO>();
		StagingSbmGroupLockPO po = new StagingSbmGroupLockPO();
		po.setSbmFileProcSumId(sbmFileProcSumId);
		po.setProcessingGroupId(expectedProcGrpId);
		
		poList.add(po);

		stagingSbmGroupLockDao.insertStagingSbmGroupLock(poList);

		String sql = "SELECT * FROM STAGINGSBMGROUPLOCK WHERE SBMFILEPROCESSINGSUMMARYID = " + sbmFileProcSumId;

		List<Map<String, Object>> actualList = jdbc.queryForList(sql);
		assertEquals("STAGINGSBMPOLICY list size", 1, actualList.size());

		Map<String, Object> row = actualList.get(0);

		assertEquals("PROCESSINGGROUPID ", new BigDecimal(expectedProcGrpId), row.get("PROCESSINGGROUPID"));
		assertNull("BATCHID should be null", row.get("BATCHID"));
		assertSysData(row);
		
		
		boolean updated = stagingSbmGroupLockDao.updateStagingGroupLockForExtract(9999L);
		assertTrue("updated", updated);
		
		sql = "SELECT * FROM STAGINGSBMGROUPLOCK WHERE SBMFILEPROCESSINGSUMMARYID = " + sbmFileProcSumId;

		List<Map<String, Object>> updatedList = jdbc.queryForList(sql);
		assertEquals("STAGINGSBMPOLICY list size", 1, updatedList.size());

		Map<String, Object> updateRow = updatedList.get(0);

		assertEquals("PROCESSINGGROUPID ", new BigDecimal(expectedProcGrpId), updateRow.get("PROCESSINGGROUPID"));
		assertNotNull("BATCHID should not be null", updateRow.get("BATCHID"));
		assertEquals("BATCHID should be 9999", 9999, ((BigDecimal)updateRow.get("BATCHID")).longValue());
		assertSysData(updateRow);
		
		int deletedCount = stagingSbmGroupLockDao.deleteStagingGroupLockForExtract(9999L);
		
		assertEquals("deleted count", 1, deletedCount);
	}
	
	@Test
	public void test_selectStagingGroupLockZero() {

		assertNotNull("stagingSbmGroupLockDao", stagingSbmGroupLockDao);
		userVO.setUserId("StagSbmGrpLockDaoTest");
		String stateCd = TestDataSBMUtility.getRandomSbmState();
		
		Long expectedProcGrpId = 0L; 
		
		Long sbmFileProcSumId = insertSBMFileProcessingSummary(stateCd + "0");
		
		List<StagingSbmGroupLockPO> poList = new ArrayList<StagingSbmGroupLockPO>();
		StagingSbmGroupLockPO po = new StagingSbmGroupLockPO();
		po.setSbmFileProcSumId(sbmFileProcSumId);
		po.setProcessingGroupId(expectedProcGrpId);
		
		poList.add(po);

		stagingSbmGroupLockDao.insertStagingSbmGroupLock(poList);

		String sql = "SELECT * FROM STAGINGSBMGROUPLOCK WHERE SBMFILEPROCESSINGSUMMARYID = " + sbmFileProcSumId;

		List<Map<String, Object>> actualList = jdbc.queryForList(sql);
		assertEquals("STAGINGSBMPOLICY list size", 1, actualList.size());

		Map<String, Object> row = actualList.get(0);

		assertEquals("PROCESSINGGROUPID ", new BigDecimal(expectedProcGrpId), row.get("PROCESSINGGROUPID"));
		assertNull("BATCHID should be null", row.get("BATCHID"));
		
		boolean updated = stagingSbmGroupLockDao.updateStagingGroupLock(sbmFileProcSumId, 9999L);
		assertTrue("updated", updated);
		
		List<StagingSbmGroupLockPO> locksList = stagingSbmGroupLockDao.selectStagingGroupLockZero(9999L);
		assertEquals("STAGING Group Lock list size", 1, locksList.size());
	}
	
	@Test
	public void test_selectStagingGroupLockSbmr() {

		assertNotNull("stagingSbmGroupLockDao", stagingSbmGroupLockDao);
		userVO.setUserId("StagSbmGrpLockDaoTest");
		String stateCd = TestDataSBMUtility.getRandomSbmState();
		
		Long expectedProcGrpId = 0L; 
		
		Long sbmFileProcSumId = insertSBMFileProcessingSummary(stateCd + "0");
		
		List<StagingSbmGroupLockPO> poList = new ArrayList<StagingSbmGroupLockPO>();
		StagingSbmGroupLockPO po = new StagingSbmGroupLockPO();
		po.setSbmFileProcSumId(sbmFileProcSumId);
		po.setProcessingGroupId(expectedProcGrpId);
		
		poList.add(po);

		stagingSbmGroupLockDao.insertStagingSbmGroupLock(poList);

		String sql = "SELECT * FROM STAGINGSBMGROUPLOCK WHERE SBMFILEPROCESSINGSUMMARYID = " + sbmFileProcSumId;

		List<Map<String, Object>> actualList = jdbc.queryForList(sql);
		assertEquals("STAGINGSBMPOLICY list size", 1, actualList.size());

		Map<String, Object> row = actualList.get(0);

		assertEquals("PROCESSINGGROUPID ", new BigDecimal(expectedProcGrpId), row.get("PROCESSINGGROUPID"));
		assertNull("BATCHID should be null", row.get("BATCHID"));
		
		boolean updated = stagingSbmGroupLockDao.updateStagingGroupLock(sbmFileProcSumId, 9999L);
		assertTrue("updated", updated);
		
		List<StagingSbmGroupLockPO> locksList = stagingSbmGroupLockDao.selectStagingGroupLockSbmr();
		assertEquals("STAGING Group Lock list size", 1, locksList.size());
	}
	
	@Test
	public void test_selectSbmFileProcessingSummaryIdList() {

		assertNotNull("stagingSbmGroupLockDao", stagingSbmGroupLockDao);
		userVO.setUserId("StagSbmGrpLockDaoTest");
		String stateCd = TestDataSBMUtility.getRandomSbmState();
		
		Long expectedProcGrpId = 1L; 
		
		Long sbmFileProcSumId = insertSBMFileProcessingSummary(stateCd + "0");
		
		String insertSql = "INSERT INTO STAGINGSBMPOLICY (SBMFILEPROCESSINGSUMMARYID, STAGINGSBMPOLICYID, PROCESSINGGROUPID) VALUES (STAGINGSBMPOLICYSEQ.NEXTVAL, " 
							+ sbmFileProcSumId + "," + expectedProcGrpId + ")";
		jdbc.execute(insertSql);
		
		List<StagingSbmGroupLockPO> poList = new ArrayList<StagingSbmGroupLockPO>();
		StagingSbmGroupLockPO po = new StagingSbmGroupLockPO();
		po.setSbmFileProcSumId(sbmFileProcSumId);
		po.setProcessingGroupId(expectedProcGrpId);
		
		poList.add(po);

		stagingSbmGroupLockDao.insertStagingSbmGroupLock(poList);

		String sql = "SELECT * FROM STAGINGSBMGROUPLOCK WHERE SBMFILEPROCESSINGSUMMARYID = " + sbmFileProcSumId;

		List<Map<String, Object>> actualList = jdbc.queryForList(sql);
		assertEquals("STAGINGSBMPOLICY list size", 1, actualList.size());

		Map<String, Object> row = actualList.get(0);

		assertEquals("PROCESSINGGROUPID ", new BigDecimal(expectedProcGrpId), row.get("PROCESSINGGROUPID"));
		assertNull("BATCHID should be null", row.get("BATCHID"));
		
		List<StagingSbmGroupLockPO> locksList = stagingSbmGroupLockDao.selectSbmFileProcessingSummaryIdList();
		assertEquals("STAGING Group Lock list size", 1, locksList.size());
	}
	
}
