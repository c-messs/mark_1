package gov.hhs.cms.ff.fm.eps.ep.sbm.dao.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import gov.hhs.cms.ff.fm.eps.ep.dao.SbmFileArchiveDao;
import gov.hhs.cms.ff.fm.eps.ep.enums.EProdEnum;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmFileArchivePO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileProcessingDTO;
import gov.hhs.cms.ff.fm.eps.ep.util.sbm.TestDataSBMUtility;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/sbmi-data-config.xml", "classpath:/test-context-data.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class SbmFileArchiveDaoImplTest extends BaseSBMDaoTest {

	@Autowired
	private SbmFileArchiveDao sbmFileArchiveDao;

	@Test
	public void test_saveFileToArchive_Exception() { 

		Class<?> expectedEx = com.accenture.foundation.common.exception.ApplicationException.class;
		String expectedCd = EProdEnum.EPROD_10.getCode();

		try {
			sbmFileArchiveDao.saveFileToArchive(new SbmFileArchivePO()) ;
		} catch (Exception ex) {
			assertEquals("Exception thrown", expectedEx, ex.getClass());
			assertEquals("EPROD", expectedCd, ex.getMessage());
		}
	}


	@Test
	public void test_saveFileToArchive_Null_SbmFileId_CovYr() {
		
		boolean expected = true;
		String tenantId = TestDataSBMUtility.getRandomSbmState() + "0";		
	    String sbmFileId = "FID-" + TestDataSBMUtility.getRandomNumberAsString(4);
		SBMFileProcessingDTO fileDTO = insertParentFileRecords(tenantId, sbmFileId);
		
		SbmFileArchivePO po = new SbmFileArchivePO();
		
		po.setSbmFileInfoId(fileDTO.getSbmFileInfo().getSbmFileInfoId());
		// leave SbmFileNum null for this test.
		// leave CoverageYear null for this test.
		boolean actual = sbmFileArchiveDao.saveFileToArchive(po);
		
		assertEquals("saveFileToArchive success with null SbmFileId and CoverageYear", expected, actual);
	}

}
