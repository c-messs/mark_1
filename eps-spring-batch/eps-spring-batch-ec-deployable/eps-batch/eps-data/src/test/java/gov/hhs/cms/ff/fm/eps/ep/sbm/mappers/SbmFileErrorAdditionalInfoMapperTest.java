package gov.hhs.cms.ff.fm.eps.ep.sbm.mappers;

import java.util.List;

import org.junit.Test;

import gov.hhs.cms.ff.fm.eps.ep.po.SbmFileErrorAdditionalInfoPO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileInfo;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileProcessingDTO;
import gov.hhs.cms.ff.fm.eps.ep.util.sbm.TestDataSBMUtility;

public class SbmFileErrorAdditionalInfoMapperTest extends SBMBaseMapperTest {

	private SbmFileErrorAdditionalInfoMapper mapper = new SbmFileErrorAdditionalInfoMapper();
	
	@Test
	public void test_mapSbmToEps() {


		SBMFileProcessingDTO inboundDTO = new SBMFileProcessingDTO();
		int expectedListSize = 3;
		Long expectedSbmFileInfoId = TestDataSBMUtility.getRandomNumberAsLong(8);
		inboundDTO.setSbmFileInfo(new SBMFileInfo());
		inboundDTO.getSbmFileInfo().setSbmFileInfoId(expectedSbmFileInfoId);

		for ( int i = 0 ; i < expectedListSize; ++i) {

			inboundDTO.getErrorList().add(TestDataSBMUtility.makeSBMErrorDTO(i));
		}

		List<SbmFileErrorAdditionalInfoPO> actualList = mapper.mapSbmToEps(inboundDTO);

		assertEquals("SbmFileErrorPO list size", (1 + 2 + 3), actualList.size());

		int idx = 0;
		for ( int i = 0 ; i < expectedListSize; ++i) {
			
			for (int j = 0 ; j <= i; ++j) {
				SbmFileErrorAdditionalInfoPO actualPO = actualList.get(idx);
				String msg = "PO " + idx + ") ";
				assertEquals(msg + "SbmFileInfoId", expectedSbmFileInfoId, actualPO.getSbmFileInfoId());
				assertEquals(msg + "AdditionalErrorInfoText contains text", TestDataSBMUtility.ADDL_INFO_TXT + i + ", " + j + ")", actualPO.getAdditionalErrorInfoText());
				assertEquals(msg + "SbmFileErrorSeqNum", (i + 1), actualPO.getSbmFileErrorSeqNum().intValue());
				idx++;
			}
		}
	}

}
