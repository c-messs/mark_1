package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm;

import static gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus.EXPIRED;
import static gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus.REJECTED;


import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.cms.dsh.sbmr.ErrorType;
import gov.cms.dsh.sbmr.FileAcceptanceRejection;
import gov.cms.dsh.sbmr.FileInformationType;
import gov.cms.dsh.sbms.SBMS;

import gov.hhs.cms.ff.fm.eps.ep.enums.SBMErrorWarningCode;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMCache;

import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMErrorDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileInfo;


import gov.hhs.cms.ff.fm.eps.ep.sbm.SbmResponseDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SBMFileCompositeDAO;

/**
 * @author mark.finkelshteyn
 * Handles SBMResponseGenerator class
 */
public class SBMResponseGenHandler {
	private static final Logger LOG = LoggerFactory.getLogger(SBMResponseGenHandler.class);
	
	private Marshaller sbmsMarshaller;
	/**
	 * @param sbmFileInfo
	 * @param errorList
	 * @param fileStatus
	 * @return String
	 */
	public String createSbmsXml(SBMFileInfo sbmFileInfo, List<SBMErrorDTO> errorList, SBMFileStatus fileStatus) throws JAXBException  {
		if(sbmsMarshaller==null){
			sbmsMarshaller = JAXBContext.newInstance(SBMS.class).createMarshaller();	
		}
		SBMS sbms = new SBMS();
		sbms.setFileName(sbmFileInfo.getSbmFileNm());
		
		if(EXPIRED.equals(fileStatus)) {
			fileStatus = REJECTED;  // SBMR/SBMS schema does not have EXPIERED status so map it to REJECTED status
		}
		
		sbms.setStatus(fileStatus.getName()); 
		
		if(CollectionUtils.isNotEmpty(errorList)) {
			
			for(SBMErrorDTO error: errorList) {

				gov.cms.dsh.sbms.ErrorType errorType = new gov.cms.dsh.sbms.ErrorType();
				errorType.setErrorCode(error.getSbmErrorWarningTypeCd());
				errorType.setElementInError(error.getElementInErrorNm());
				errorType.setErrorDescription(SBMCache.getErrorDescription(error.getSbmErrorWarningTypeCd()));
				
				if(CollectionUtils.isNotEmpty(error.getAdditionalErrorInfoList())) {
					
					for(String additionalInfo: error.getAdditionalErrorInfoList()) {
						errorType.getAdditionalErrorInfo().add(additionalInfo);
					}
				}
				sbms.getError().add(errorType);
			}
		}
		
		StringWriter writer = new StringWriter();	
		sbmsMarshaller.marshal(sbms, writer);
		LOG.debug("SBMS:\n{}", writer.toString());
		
		return writer.toString();
	}
	
	/**
	 * @param sbmrDto
	 * @param fileStatus
	 * @return fileInfo
	 */
	public FileInformationType setFileSatus(SbmResponseDTO sbmrDto, SBMFileStatus fileStatus) {
		
		for( FileInformationType fileInfo: sbmrDto.getSbmr().getSBMIFileInfo()) {
			fileInfo.setFileProcessingStatus(fileStatus.getName());
			return fileInfo;
		}
		return null;
	}
	
	/**
	 * @param sbmrDto
	 * @param fileCompositeDao
	 * @return void
	 */
	public void generateErrorThresholdExceeded(SbmResponseDTO sbmrDto,SBMFileCompositeDAO fileCompositeDao) {
		
		List<SBMErrorDTO> errorList = new ArrayList<>();
		String issuerFileSetId = sbmrDto.getSbmSummaryAndFileInfo().getIssuerFileSetId();
		for(SBMFileInfo fileInfo : sbmrDto.getSbmSummaryAndFileInfo().getSbmFileInfoList()) {
			
			if(fileInfo.isRejectedInd()) {
				//Skip rejected files
				continue;
			}
			
			SBMErrorDTO error = SbmHelper.createErrorLog(null, SBMErrorWarningCode.ER_057.getCode());
			error.setAdditionalErrorInfoText(issuerFileSetId != null ? issuerFileSetId : fileInfo.getSbmFileId());
			error.setSbmFileInfoId(fileInfo.getSbmFileInfoId());			
			errorList.add(error);
		}
		//Save error to database
		fileCompositeDao.saveSBMFileErrors(errorList);
		
		FileAcceptanceRejection sbmr = sbmrDto.getSbmr();
		String errorText = SBMCache.getErrorDescription(SBMErrorWarningCode.ER_057.getCode());
		for( FileInformationType sbmrFileInfo: sbmr.getSBMIFileInfo()) {
			ErrorType error = new ErrorType();
			error.setErrorCode(SBMErrorWarningCode.ER_057.getCode());
			error.setErrorDescription(errorText);
			sbmrFileInfo.setFileError(error);			
		}
		
	}

}
