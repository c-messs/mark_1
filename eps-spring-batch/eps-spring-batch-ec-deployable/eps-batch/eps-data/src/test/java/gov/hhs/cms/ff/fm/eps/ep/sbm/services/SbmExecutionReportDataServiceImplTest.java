package gov.hhs.cms.ff.fm.eps.ep.sbm.services;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMExecutionReportDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileProcessingDTO;
import gov.hhs.cms.ff.fm.eps.ep.util.sbm.TestDataSBMUtility;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/sbmi-data-config.xml", "classpath:/test-context-data.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class SbmExecutionReportDataServiceImplTest extends BaseSbmServicesTest {


	@Autowired
	SbmExecutionReportDataService SbmExecutionReportDataService;

	@Test
	public void test_getSbmExecutionLog() {

		assertNotNull("SbmExecutionReportDataService is NOT null.", SbmExecutionReportDataService);
		String state = "RI";
		String tenantId = state + "0";
		String sbmFileId = "FID-" + TestDataSBMUtility.getRandomNumberAsString(4);
		
		SBMFileProcessingDTO parentDTO = insertParentFileRecords(tenantId, sbmFileId);
		
		List<SBMExecutionReportDTO> sbmiLog = SbmExecutionReportDataService.getSbmExecutionLog();
		
		assertTrue("sbmiLog", CollectionUtils.isNotEmpty(sbmiLog));	
		
		SBMExecutionReportDTO dto = sbmiLog.get(0);
		
		if(sbmFileId.equalsIgnoreCase(dto.getFileId())) {
			assertEquals("state", state, dto.getStateCd());	
			assertEquals("fileId", sbmFileId, dto.getFileId());	
			assertEquals("File Name", "FILENAME-"+sbmFileId, dto.getFileName());	
			assertEquals("File Status", SBMFileStatus.IN_PROCESS.getName(), dto.getFileStatus());	
		}
	}

}

