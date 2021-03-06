package gov.hhs.cms.ff.fm.eps.ep.mappers;

import gov.cms.dsh.bem.FileInformationType;
import gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentRequestDTO;
import gov.hhs.cms.ff.fm.eps.ep.po.TransMsgFileInfoPO;
import gov.hhs.cms.ff.fm.eps.ep.util.EpsDateUtils;

/**
 * @author EPS
 *
 */
public class TransMsgFileInfoMapper {
	
	/**
	 * @param berDTO
	 * @return TransMsgFileInfoPO
	 */
	public TransMsgFileInfoPO mapDTOToVO(BenefitEnrollmentRequestDTO berDTO) {
		TransMsgFileInfoPO transMsgFileInfoPO = new TransMsgFileInfoPO();

		if(berDTO != null) {
			transMsgFileInfoPO.setFileInfoXML(berDTO.getFileInfoXml());
			transMsgFileInfoPO.setFileNm(berDTO.getFileNm());

			FileInformationType fileInfoType = berDTO.getFileInformation();
			
			if (fileInfoType != null) {
				transMsgFileInfoPO.setGroupSenderId(fileInfoType.getGroupSenderID());
				transMsgFileInfoPO.setGroupReceiverId(fileInfoType.getGroupReceiverID());
				transMsgFileInfoPO.setGroupTimestampDateTime(EpsDateUtils.getDateTimeFromXmlGC(fileInfoType.getGroupTimeStamp()));
				transMsgFileInfoPO.setGroupControlNum(fileInfoType.getGroupControlNumber());
				transMsgFileInfoPO.setVersionNum(fileInfoType.getVersionNumber());
			}
			transMsgFileInfoPO.setTransMsgOriginTypeCd(berDTO.getExchangeTypeCd());
			transMsgFileInfoPO.setCreateDateTime(EpsDateUtils.getCurrentDateTime());
			transMsgFileInfoPO.setLastModifiedDateTime(EpsDateUtils.getCurrentDateTime());
		}

		return transMsgFileInfoPO;
	}
}
