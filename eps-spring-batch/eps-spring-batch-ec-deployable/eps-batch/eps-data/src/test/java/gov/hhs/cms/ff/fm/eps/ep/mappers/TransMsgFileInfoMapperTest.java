package gov.hhs.cms.ff.fm.eps.ep.mappers;

import gov.cms.dsh.bem.FileInformationType;
import gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentRequestDTO;
import gov.hhs.cms.ff.fm.eps.ep.data.util.TestDataUtil;
import gov.hhs.cms.ff.fm.eps.ep.enums.ExchangeType;
import gov.hhs.cms.ff.fm.eps.ep.po.TransMsgFileInfoPO;
import gov.hhs.cms.ff.fm.eps.ep.util.EpsDateUtils;

import org.junit.Test;

public class TransMsgFileInfoMapperTest extends BaseMapperTest {


	private TransMsgFileInfoMapper mapper = new TransMsgFileInfoMapper();


	@Test
	public void testMapDTOToPO() {

		BenefitEnrollmentRequestDTO berDTO =  new BenefitEnrollmentRequestDTO();

		Long id = TestDataUtil.getRandom3DigitNumber();
		String hiosId = "11111";
		String state = "NC";
		String variantId = "01";
		String groupSenderId  = hiosId + state + "0" + hiosId.substring(0, 4) + variantId;
		
		berDTO.setFileInformation(TestDataUtil.makeFileInformationType(id, groupSenderId));
		berDTO.setExchangeTypeCd(ExchangeType.SBM.getValue());
		
		String expecteFileNm = "FFM.IC834.D140603.T1515555.P";
		String expectedFileInfoXML = TestDataUtil.makeFileInformationTypeAsStringXML(id);
		
		berDTO.setFileNm(expecteFileNm);
		berDTO.setFileInfoXml(expectedFileInfoXML);

		TransMsgFileInfoPO po =  mapper.mapDTOToVO(berDTO);
		assertNotNull("TransMsgFileInfoPO", po);
		
		FileInformationType expectedFileInfoType = berDTO.getFileInformation();

		assertNotNull("FileInfoXML", po.getFileInfoXML());
		assertEquals("FileInfoXML", expectedFileInfoXML , po.getFileInfoXML());
		assertNotNull("GroupSenderId", po.getGroupSenderId());
		assertEquals("GroupSenderId", expectedFileInfoType.getGroupSenderID(), po.getGroupSenderId());
		assertNotNull("GroupReceiverId", po.getGroupReceiverId());
		assertEquals("GroupReceiverId", expectedFileInfoType.getGroupReceiverID(), po.getGroupReceiverId());
		assertNotNull("FileName", po.getFileNm());
		assertEquals("FileName", expecteFileNm, po.getFileNm());
		assertNotNull("GroupTimestampDateTime", po.getGroupTimestampDateTime());
		assertEquals("GroupTimestampDateTime", EpsDateUtils.getDateTimeFromXmlGC(expectedFileInfoType.getGroupTimeStamp()), 
				po.getGroupTimestampDateTime());
		assertNotNull("GroupControlNum", po.getGroupControlNum());
				assertEquals("GroupControlNum", expectedFileInfoType.getGroupControlNumber(),
						po.getGroupControlNum());
		assertNotNull("VersionNum", po.getVersionNum());
		assertEquals("VersionNum", expectedFileInfoType.getVersionNumber(),
						po.getVersionNum());
		assertNotNull("ExchangeTypeCd", po.getTransMsgOriginTypeCd());
		assertEquals("ExchangeTypeCd", ExchangeType.SBM.getValue(),
						po.getTransMsgOriginTypeCd());
		
		assertNotNull("createDateTime", po.getCreateDateTime());
		assertNotNull("lastModifiedDateTime", po.getLastModifiedDateTime());
		
		// coverage only, EpsBeanPropertySqlParameterSource sets this in DAO.
		po.setCreateBy("some unit test");
		po.setLastModifiedBy("some unit test");
	}
	
	@Test
	public void testMapDTOToPO_BemDTO_null() {

		BenefitEnrollmentRequestDTO bemDTO = null;		
		TransMsgFileInfoPO po =  mapper.mapDTOToVO(bemDTO);
		assertNotNull("TransMsgFileInfoPO", po);
		
	}
	
	@Test
	public void testMapDTOToPO_fileInfoType_null() {

		BenefitEnrollmentRequestDTO bemDTO = new BenefitEnrollmentRequestDTO();	
		TransMsgFileInfoPO po =  mapper.mapDTOToVO(bemDTO);
		assertNotNull("TransMsgFileInfoPO", po);
		
	}

}
