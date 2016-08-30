package gov.hhs.cms.ff.fm.eps.ep.sbm.mappers;

import java.util.List;

import org.junit.Test;

import gov.hhs.cms.ff.fm.eps.ep.enums.SBMErrorWarningCode;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmFileErrorPO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileInfo;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileProcessingDTO;
import gov.hhs.cms.ff.fm.eps.ep.util.sbm.TestDataSBMUtility;

public class SbmFileErrorMapperTest extends SBMBaseMapperTest {

	private SbmFileErrorMapper mapper = new SbmFileErrorMapper();


	@Test
	public void test_mapSbmToEps() {


		SBMFileProcessingDTO inboundDTO = new SBMFileProcessingDTO();
		int expectedListSize = 3;
		Long expectedSbmFileInfoId = TestDataSBMUtility.getRandomNumberAsLong(8);
		SBMErrorWarningCode[] expectedErrWarnCds = SBMErrorWarningCode.values();
		inboundDTO.setSbmFileInfo(new SBMFileInfo());
		inboundDTO.getSbmFileInfo().setSbmFileInfoId(expectedSbmFileInfoId);
		
		for ( int i = 0 ; i < expectedListSize; ++i) {

			inboundDTO.getErrorList().add(TestDataSBMUtility.makeSBMErrorDTO(i));
		}

		List<SbmFileErrorPO> actualList = mapper.mapSbmToEps(expectedSbmFileInfoId, inboundDTO.getErrorList());

		assertEquals("SbmFileErrorPO list size", expectedListSize, actualList.size());

		for (int i = 0 ; i < actualList.size(); ++i) {
			SbmFileErrorPO actualPO = actualList.get(i);
			String msg = "Error " + i + ") ";
			assertEquals(msg + "SbmFileInfoId", expectedSbmFileInfoId, actualPO.getSbmFileInfoId());
			assertEquals(msg + "ElementNm", TestDataSBMUtility.ELEMENT_TXT + i, actualPO.getElementInErrorNm());
			assertEquals(msg + "ErrorWarningTypeCd", expectedErrWarnCds[i].getCode(), actualPO.getSbmErrorWarningTypeCd());
			assertEquals(msg + "SbmFileErrorSeqNum", (i + 1), actualPO.getSbmFileErrorSeqNum().intValue());
		}	
	}

}
