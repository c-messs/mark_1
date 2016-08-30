package gov.hhs.cms.ff.fm.eps.ep.sbm.mappers;

import java.util.ArrayList;
import java.util.List;

import gov.cms.dsh.sbmi.FileInformationType.IssuerFileInformation;
import gov.cms.dsh.sbmi.FileInformationType.IssuerFileInformation.IssuerFileSet;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmFileInfoPO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileInfo;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileProcessingDTO;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;

public class SbmFileInfoMapper {


	public SbmFileInfoPO mapSbmToEps(SBMFileProcessingDTO inboundDTO) {

		SbmFileInfoPO po = null;

		if (inboundDTO.getFileInfoType() != null) {

			po = new SbmFileInfoPO();		

			gov.cms.dsh.sbmi.FileInformationType fileInfoType = inboundDTO.getFileInfoType();

			po.setSbmFileId(fileInfoType.getFileId());
			po.setSbmFileCreateDateTime(DateTimeUtil.getLocalDateTimeFromXmlGC(fileInfoType.getFileCreateDateTime()));
			po.setFileInfoXML(inboundDTO.getFileInfoXML());

			IssuerFileInformation issFileInfo = fileInfoType.getIssuerFileInformation();

			if (issFileInfo != null) {

				IssuerFileSet issFileSet = issFileInfo.getIssuerFileSet();

				if (issFileSet != null) {

					po.setIssuerFileSetId(issFileSet.getIssuerFileSetId());
					po.setSbmFileNum(issFileSet.getFileNumber());
				}
			}	
		}

		if (inboundDTO.getSbmFileInfo() != null) {

			if (po == null) {
				po = new SbmFileInfoPO();
			}

			SBMFileInfo sbmFileInfo = inboundDTO.getSbmFileInfo();
			po.setSbmFileNm(sbmFileInfo.getSbmFileNm());
			po.setSbmFileLastModifiedDateTime(sbmFileInfo.getFileLastModifiedDateTime());
			po.setTradingPartnerId(sbmFileInfo.getTradingPartnerId());
			po.setFunctionCd(sbmFileInfo.getFunctionCd());
			po.setSbmFileProcessingSummaryId(inboundDTO.getSbmFileProcSumId());
			po.setRejectedInd(sbmFileInfo.isRejectedInd());
		}

		if (inboundDTO.getFileInfoXML() != null) {

			if (po == null) {
				po = new SbmFileInfoPO();
			}

			po.setFileInfoXML(inboundDTO.getFileInfoXML());
		}

		return po;
	}


	public List<SBMFileInfo> mapEpsToSbm(List<SbmFileInfoPO> poList) {

		List<SBMFileInfo> fileInfoList = new ArrayList<SBMFileInfo>();

		for (SbmFileInfoPO po : poList) {

			SBMFileInfo fileInfo = new SBMFileInfo();
			fileInfo.setSbmFileInfoId(po.getSbmFileInfoId());
			fileInfo.setSbmFileProcessingSummaryId(po.getSbmFileProcessingSummaryId());
			fileInfo.setSbmFileNm(po.getSbmFileNm());
			fileInfo.setSbmFileCreateDateTime(po.getSbmFileCreateDateTime());
			fileInfo.setSbmFileId(po.getSbmFileId());
			fileInfo.setSbmFileNum(po.getSbmFileNum());
			fileInfo.setTradingPartnerId(po.getTradingPartnerId());
			fileInfo.setFunctionCd(po.getFunctionCd());
			fileInfo.setRejectedInd(po.getRejectedInd());
			fileInfo.setCreateDatetime(po.getCreateDateTime());

			fileInfoList.add(fileInfo);
		}

		return fileInfoList;
	}


	public gov.cms.dsh.sbmr.FileInformationType mapEpsToSbmr(SbmFileInfoPO po, String summaryStatus) {

		gov.cms.dsh.sbmr.FileInformationType fileInfo = new gov.cms.dsh.sbmr.FileInformationType();

		fileInfo.setSourceFileId(po.getSbmFileId());
		fileInfo.setFileCreateDateTime(DateTimeUtil.getXMLGregorianCalendar(po.getSbmFileCreateDateTime()));
		fileInfo.setFileNumber(po.getSbmFileNum());
		fileInfo.setFileProcessingStatus(summaryStatus);

		return fileInfo;
	}



}
