package gov.hhs.cms.ff.fm.eps.ep.sbm.dao.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import gov.hhs.cms.ff.fm.eps.ep.dao.SbmFileInfoDao;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmFileInfoPO;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/sbmi-data-config.xml", "classpath:/test-context-data.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class SBMFileInfoDaoImplTest extends BaseSBMDaoTest {

	@Autowired
	private SbmFileInfoDao sbmFileInfoDao;

	private String userId = "SBMFileInfoDaoImplTest";


	@Test
	public void test_insertSBMFileInfo() {

		userVO.setUserId(userId);
		assertNotNull("sbmFileInfoDao", sbmFileInfoDao);

		String tenantId = "NY0";
		Long sbmFileProcSumId = insertSBMFileProcessingSummary(tenantId);

		SbmFileInfoPO expected = makeSbmFileInfoPO(sbmFileProcSumId);

		Long sbmFileInfoId = sbmFileInfoDao.insertSBMFileInfo(expected);

		assertNotNull("SbmFileInfoId", sbmFileInfoId);

		// Go and get what was just put in.
		String sql = "SELECT sfi.SBMFILEPROCESSINGSUMMARYID, sfi.SBMFILENM, sfi.SBMFILECREATEDATETIME, sfi.SBMFILEID, " +
				"sfi.SBMFILENUM, sfi.TRADINGPARTNERID, sfi.FUNCTIONCD, sfi.REJECTEDIND, sfi.CREATEDATETIME," +
				"sfi.FILEINFOXML.getClobval() AS FILEINFOXML FROM SBMFILEINFO sfi WHERE sfi.SBMFILEINFOID = " + sbmFileInfoId;

		List<Map<String, Object>> actualList = jdbc.queryForList(sql);
		assertEquals("SBMFILEINFO record list size", 1, actualList.size());

		Map<String, Object> row = actualList.get(0);

		assertEquals("sbmFileProcSumId", new BigDecimal(sbmFileProcSumId), row.get("SBMFILEPROCESSINGSUMMARYID"));
		assertEquals("SbmFileNm", expected.getSbmFileNm(), row.get("SBMFILENM"));
		assertEquals("SbmFileId", expected.getSbmFileId(), row.get("SBMFILEID"));
		assertEquals("TradingPartnerId", expected.getTradingPartnerId(), row.get("TRADINGPARTNERID"));
		assertEquals("FunctionCd", expected.getFunctionCd(), row.get("FUNCTIONCD"));
		String yOrN = (String) row.get("REJECTEDIND");
		assertEquals("RejectInd", expected.getRejectedInd(), yOrN.equals("Y"));
		assertEquals("SbmFileNum", new BigDecimal(expected.getSbmFileNum()), row.get("SBMFILENUM"));

		String strXML = (String) row.get("FILEINFOXML");
		assertNotNull("FILEINFOXML", strXML);
		assertEquals("FILEINFOXML", expected.getFileInfoXML().trim(), strXML.trim());
		assertNotNull("CREATEDATETIME", row.get("CREATEDATETIME"));
	}

	@Test
	public void test_insertSBMFileInfo_null_FileInfoXML() {

		userVO.setUserId(userId);
		assertNotNull("sbmFileInfoDao", sbmFileInfoDao);

		String tenantId = "NY0";
		Long sbmFileProcSumId = insertSBMFileProcessingSummary(tenantId);

		SbmFileInfoPO expected = makeSbmFileInfoPO(sbmFileProcSumId);
		// set null for this test
		expected.setFileInfoXML(null);

		Long sbmFileInfoId = sbmFileInfoDao.insertSBMFileInfo(expected);

		assertNotNull("SbmFileInfoId", sbmFileInfoId);

		// Go and get what was just put in. (only testing the XML)
		String sql = "SELECT sfi.FILEINFOXML.getClobval() AS FILEINFOXML FROM SBMFILEINFO sfi WHERE sfi.SBMFILEINFOID = " + sbmFileInfoId;

		List<Map<String, Object>> actualList = jdbc.queryForList(sql);
		assertEquals("SBMFILEINFO record list size", 1, actualList.size());

		Map<String, Object> row = actualList.get(0);

		String strXML = (String) row.get("FILEINFOXML");
		assertNull("FILEINFOXML", strXML);
	}

}
