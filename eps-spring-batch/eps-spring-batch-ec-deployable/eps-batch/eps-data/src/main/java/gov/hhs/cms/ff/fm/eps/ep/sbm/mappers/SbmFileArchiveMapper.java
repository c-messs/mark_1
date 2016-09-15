package gov.hhs.cms.ff.fm.eps.ep.sbm.mappers;

import gov.cms.dsh.sbmi.FileInformationType;
import gov.cms.dsh.sbmi.FileInformationType.IssuerFileInformation;
import gov.cms.dsh.sbmi.FileInformationType.IssuerFileInformation.IssuerFileSet;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmFileArchivePO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileInfo;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileProcessingDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SbmDataUtil;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;

/**
 * @author j.radziewski
 *
 */
public class SbmFileArchiveMapper {
	
	/**
	 * 
	 * @param inboundFileDTO
	 * @return po
	 */
	public SbmFileArchivePO mapSbmToEps(SBMFileProcessingDTO inboundFileDTO) {
		
		SbmFileArchivePO po = new SbmFileArchivePO();
		
		po.setSbmFileInfoId(inboundFileDTO.getSbmFileInfo().getSbmFileInfoId());
		
		FileInformationType fileInfoType = inboundFileDTO.getFileInfoType();
		
		if (fileInfoType != null) {
		
			po.setSbmFileId(fileInfoType.getFileId());
			po.setSbmFileCreateDateTime(DateTimeUtil.getLocalDateTimeFromXmlGC(fileInfoType.getFileCreateDateTime()));
			po.setTenantNum(SbmDataUtil.getTenantNum(fileInfoType));
			po.setCoverageYear(fileInfoType.getCoverageYear());
			po.setSubscriberStateCd(SbmDataUtil.getStateCd(fileInfoType));
			
			IssuerFileInformation issFileInfo = fileInfoType.getIssuerFileInformation();
			
			if (issFileInfo != null) {
				
				po.setIssuerId(issFileInfo.getIssuerId());
				
				IssuerFileSet issFileSet = issFileInfo.getIssuerFileSet();
				
				if (issFileSet != null) {
					
					po.setIssuerFileSetId(issFileSet.getIssuerFileSetId());
					po.setSbmFileNum(issFileSet.getFileNumber());
				}
			}	
		}
		
		SBMFileInfo sbmFileInfo = inboundFileDTO.getSbmFileInfo();
		
		if (sbmFileInfo != null) {
			
			po.setSbmFileNm(sbmFileInfo.getSbmFileNm());
			po.setTradingPartnerId(sbmFileInfo.getTradingPartnerId());
			po.setFunctionCd(sbmFileInfo.getFunctionCd());
		}
		
		return po;
	}

}
