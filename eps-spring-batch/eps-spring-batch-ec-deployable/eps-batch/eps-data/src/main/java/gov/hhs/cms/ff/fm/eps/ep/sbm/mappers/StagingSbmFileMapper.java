package gov.hhs.cms.ff.fm.eps.ep.sbm.mappers;

import gov.hhs.cms.ff.fm.eps.ep.po.StagingSbmFilePO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileProcessingDTO;

public class StagingSbmFileMapper {
	
	
	   public StagingSbmFilePO mapSbmToEps(SBMFileProcessingDTO inboundFileDTO) {
		   
		   StagingSbmFilePO po = new StagingSbmFilePO();
		   
		   po.setBatchId(inboundFileDTO.getBatchId());
		   po.setSbmFileInfoId(inboundFileDTO.getSbmFileInfo().getSbmFileInfoId());
		   po.setSbmFileProcessingSummaryId(inboundFileDTO.getSbmFileProcSumId());
		   po.setSbmXML(inboundFileDTO.getSbmFileXML());
		   
		   return po;	   
	   }

}
