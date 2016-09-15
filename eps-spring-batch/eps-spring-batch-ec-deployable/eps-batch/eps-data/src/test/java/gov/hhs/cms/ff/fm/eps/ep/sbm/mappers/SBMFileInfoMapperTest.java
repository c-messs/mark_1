package gov.hhs.cms.ff.fm.eps.ep.sbm.mappers;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import gov.cms.dsh.sbmi.FileInformationType;
import gov.cms.dsh.sbmi.FileInformationType.IssuerFileInformation;
import gov.cms.dsh.sbmi.FileInformationType.IssuerFileInformation.IssuerFileSet;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmFileInfoPO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileInfo;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileProcessingDTO;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;
import gov.hhs.cms.ff.fm.eps.ep.util.sbm.TestDataSBMUtility;

public class SBMFileInfoMapperTest extends SBMBaseMapperTest {


	private SbmFileInfoMapper mapper = new SbmFileInfoMapper();


	@Test
	public void test_mapSbmToEps() {

		SBMFileInfo expectedSBMFileInfo = TestDataSBMUtility.makeSBMFileInfo();
		FileInformationType expectedFileInfoType = TestDataSBMUtility.makeFileInformationType(YEAR, TestDataSBMUtility.FILES_FILESET);
		IssuerFileSet expectedIssFileSet = TestDataSBMUtility.makeIssuerFileSet("99", 48, 12345678);
		expectedFileInfoType.getIssuerFileInformation().setIssuerFileSet(expectedIssFileSet);
		SBMFileProcessingDTO expectedDTO = new SBMFileProcessingDTO();
		expectedDTO.setFileInfoType(expectedFileInfoType);
		expectedDTO.setSbmFileInfo(expectedSBMFileInfo);

		String expectedXML = "<SomeXml>fileInfo</SomeXml>";
		expectedDTO.setFileInfoXML(expectedXML);

		SbmFileInfoPO actual = mapper.mapSbmToEps(expectedDTO);

		assertNotNull("SBMFileInfoPO", actual);
		assertEquals("SbmFileId", expectedFileInfoType.getFileId(), actual.getSbmFileId());
		assertEquals("FileCreateDateTime", expectedFileInfoType.getFileCreateDateTime(), 
				DateTimeUtil.getXMLGregorianCalendar(actual.getSbmFileCreateDateTime()));
		assertEquals("FileInfoXML", expectedXML, actual.getFileInfoXML());

		assertEquals("IssuerFileSetId", expectedIssFileSet.getIssuerFileSetId(), actual.getIssuerFileSetId());
		assertEquals("FileNumber", expectedIssFileSet.getFileNumber(), actual.getSbmFileNum().intValue());

		assertEquals("SbmFileNm", expectedSBMFileInfo.getSbmFileNm(), actual.getSbmFileNm());
		assertEquals("TradingPartnerId", expectedSBMFileInfo.getTradingPartnerId(), actual.getTradingPartnerId());
		assertEquals("FunctionCd", expectedSBMFileInfo.getFunctionCd(), actual.getFunctionCd());

	}

	@Test
	public void test_mapSbmToEps_Empty_DTO() {

		SBMFileProcessingDTO expectedDTO = new SBMFileProcessingDTO();
		SbmFileInfoPO actual = mapper.mapSbmToEps(expectedDTO);
		assertNull("SBMFileInfoPO", actual);
	}

	@Test
	public void test_mapSbmToEps_null_IssuerFileInfo() {

		FileInformationType expectedFileInfoType = TestDataSBMUtility.makeFileInformationType(YEAR, TestDataSBMUtility.FILES_FILESET);
		expectedFileInfoType.setIssuerFileInformation(null);
		SBMFileProcessingDTO expectedDTO = new SBMFileProcessingDTO();
		expectedDTO.setFileInfoType(expectedFileInfoType);
		String expectedXML = "<SomeXml>fileInfo</SomeXml>";
		expectedDTO.setFileInfoXML(expectedXML);

		SbmFileInfoPO actual = mapper.mapSbmToEps(expectedDTO);

		assertNotNull("SBMFileInfoPO should NOT be null", actual);
		assertEquals("SbmFileId", expectedFileInfoType.getFileId(), actual.getSbmFileId());
		assertEquals("FileCreateDateTime", DateTimeUtil.getLocalDateTimeFromXmlGC(expectedFileInfoType.getFileCreateDateTime()), 
				actual.getSbmFileCreateDateTime());
		assertEquals("FileInfoXML", expectedXML, actual.getFileInfoXML());
		assertNull("IssuerFileSetId should be null.", actual.getIssuerFileSetId());
		assertNull("SbmFileNum should be null.", actual.getSbmFileNum());

	}

	@Test
	public void test_mapSbmToEps_null_IssuerFileSet() {

		SBMFileProcessingDTO expectedDTO = new SBMFileProcessingDTO();
		SBMFileInfo expectedSBMFileInfo = TestDataSBMUtility.makeSBMFileInfo();
		expectedDTO.setSbmFileInfo(expectedSBMFileInfo);
		expectedDTO.setFileInfoType(new FileInformationType());
		expectedDTO.getFileInfoType().setIssuerFileInformation(new IssuerFileInformation());

		SbmFileInfoPO actual = mapper.mapSbmToEps(expectedDTO);

		assertNotNull("SBMFileInfoPO", actual);
		assertNull("FileInformationType FileCreateDateTime should be null.", actual.getSbmFileCreateDateTime());
		assertEquals("SbmFileNm", expectedSBMFileInfo.getSbmFileNm(), actual.getSbmFileNm());
		assertEquals("TradingPartnerId", expectedSBMFileInfo.getTradingPartnerId(), actual.getTradingPartnerId());
		assertEquals("FunctionCd", expectedSBMFileInfo.getFunctionCd(), actual.getFunctionCd());
	}

	@Test
	public void test_mapSbmToEps_null_FileInfoType() {

		SBMFileProcessingDTO expectedDTO = new SBMFileProcessingDTO();
		SBMFileInfo expectedSBMFileInfo = TestDataSBMUtility.makeSBMFileInfo();
		expectedDTO.setSbmFileInfo(expectedSBMFileInfo);

		SbmFileInfoPO actual = mapper.mapSbmToEps(expectedDTO);

		assertNotNull("SBMFileInfoPO", actual);
		assertNull("FileInformationType FileCreateDateTime should be null.", actual.getSbmFileCreateDateTime());
		assertEquals("SbmFileNm", expectedSBMFileInfo.getSbmFileNm(), actual.getSbmFileNm());
		assertEquals("TradingPartnerId", expectedSBMFileInfo.getTradingPartnerId(), actual.getTradingPartnerId());
		assertEquals("FunctionCd", expectedSBMFileInfo.getFunctionCd(), actual.getFunctionCd());
	}
	
	@Test
	public void test_mapSbmToEps_XML_only() {

		SBMFileProcessingDTO expectedDTO = new SBMFileProcessingDTO();
		String expectedFileInfoXML = "<Xml>FileInfo stuff</Xml>";
		expectedDTO.setFileInfoXML(expectedFileInfoXML);
		
		SbmFileInfoPO actual = mapper.mapSbmToEps(expectedDTO);

		assertNotNull("SBMFileInfoPO", actual);
		assertEquals("SbmFileNm", expectedFileInfoXML, actual.getFileInfoXML());
	}


	@Test
	public void test_mapEpsToSbm() {

		int expectedListSize = 3;
		List<SbmFileInfoPO> expectedList = new ArrayList<SbmFileInfoPO>();
		for (int i = 0; i < expectedListSize; ++i) {

			String id = i + ""+ i + "" + i + "" + i;
			SbmFileInfoPO po = makeSBMFileInfoPO(Long.valueOf(id));
			expectedList.add(po);
		}

		List<SBMFileInfo> actualList = mapper.mapEpsToSbm(expectedList);

		assertEquals("SBMFileInfo list size", expectedListSize, actualList.size());

		for (int i = 0; i < expectedListSize; ++i) {

			SBMFileInfo actual = actualList.get(i);
			SbmFileInfoPO expected = expectedList.get(i);

			assertNotNull("SBMFileInfo", actual);
			assertEquals("SbmFileId", expected.getSbmFileId(), actual.getSbmFileId());
			assertEquals("SbmFileCreateDateTime", expected.getSbmFileCreateDateTime(), actual.getSbmFileCreateDateTime());
			assertEquals("SbmFileNumber", expected.getSbmFileNum(), actual.getSbmFileNum());
			assertEquals("SbmFileNm", expected.getSbmFileNm(), actual.getSbmFileNm());
			assertEquals("TradingPartnerId", expected.getTradingPartnerId(), actual.getTradingPartnerId());
			assertEquals("FunctionCd", expected.getFunctionCd(), actual.getFunctionCd());
		}
	}

	@Test
	public void test_mapEpsToSbm_empty_EPS() {

		int expectedListSize = 0;
		List<SbmFileInfoPO> expectedList = new ArrayList<SbmFileInfoPO>();
		List<SBMFileInfo> actualList = mapper.mapEpsToSbm(expectedList);
		assertEquals("SBMFileInfo list size", expectedListSize, actualList.size());
	}


}
