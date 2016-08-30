package gov.hhs.cms.ff.fm.eps.ep.sbm.mappers;


import org.junit.Test;

import gov.hhs.cms.ff.fm.eps.ep.po.StagingSbmFilePO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileInfo;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileProcessingDTO;
import gov.hhs.cms.ff.fm.eps.ep.util.sbm.TestDataSBMUtility;

public class StagingSbmFileMapperTest extends SBMBaseMapperTest {
	
	
	private StagingSbmFileMapper mapper = new StagingSbmFileMapper();
	
	@Test
	public void test_mapSbmToEps() {

		Long expectedBatchId = TestDataSBMUtility.getRandomNumberAsLong(5);
		Long expectedSbmFileInfoId = TestDataSBMUtility.getRandomNumberAsLong(4);
		Long expectedSbmFileProcSumId = TestDataSBMUtility.getRandomNumberAsLong(3);
		String expectedSbmXML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                              "<Enrollment xmlns=\"http://sbmi.dsh.cms.gov\"><FileInformation>" +
							  "<FileId>9999999</FileId></FileInformation></Enrollment>";
		
		SBMFileProcessingDTO inboundFileDTO = new SBMFileProcessingDTO();
		inboundFileDTO.setBatchId(expectedBatchId);
		inboundFileDTO.setSbmFileInfo(new SBMFileInfo());
		inboundFileDTO.getSbmFileInfo().setSbmFileInfoId(expectedSbmFileInfoId);
		inboundFileDTO.setSbmFileProcSumId(expectedSbmFileProcSumId);
		inboundFileDTO.setSbmFileXML(expectedSbmXML);
		
		StagingSbmFilePO actualPO = mapper.mapSbmToEps(inboundFileDTO);
	
		assertNotNull("StagingSbmFilePO", actualPO);
		assertEquals("BatchId", expectedBatchId, actualPO.getBatchId());
		assertEquals("SbmFileInfoId", expectedSbmFileInfoId, actualPO.getSbmFileInfoId());
		assertEquals("SbmFileProcessingSummaryId", expectedSbmFileProcSumId, actualPO.getSbmFileProcessingSummaryId());
		assertEquals("SbmXML", expectedSbmXML, actualPO.getSbmXML());
	}
}
