package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm;

import static gov.hhs.cms.ff.fm.eps.ep.sbm.SbmDataUtil.getStateCd;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.cms.dsh.sbmi.FileInformationType;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileInfo;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileProcessingDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMSummaryAndFileInfoDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SBMFileCompositeDAO;

public class SBMFileStatusHandler {

	private static final Logger LOG = LoggerFactory.getLogger(SBMFileStatusHandler.class);
	
	private int freezePeriodStartDay;
	private int freezePeriodEndDay;
	private SBMFileCompositeDAO fileCompositeDao;
	
	public void determineAndSetFileStatus(SBMFileProcessingDTO fileProcDto) {
				
		FileInformationType fileInfoType = fileProcDto.getFileInfoType();
		LOG.info("Determing file status for fileId:{}", fileInfoType.getFileId());
		
		LocalDate today = LocalDate.now();
		LocalDateTime fileLastModifiedDateTime = fileProcDto.getSbmFileInfo().getFileLastModifiedDateTime();
		LocalDateTime freezeStartDateTime =  LocalDate.now().withDayOfMonth(freezePeriodStartDay).atStartOfDay();
		
		//check is in freeze period
		if((freezePeriodStartDay <= today.getDayOfMonth()) && (today.getDayOfMonth() <= freezePeriodEndDay)) {
			//If a file is received before freeze period then that file should be processed regardless of freeze period
			if(fileLastModifiedDateTime == null || freezeStartDateTime.isBefore(fileLastModifiedDateTime)) {
				fileProcDto.setSbmFileStatusType(SBMFileStatus.FREEZE); 
				LOG.info("fileId:{}, status set to FREEZE", fileInfoType.getFileId());
				return;
			}
		}
		
		//check if all files in fileset received
		if(fileInfoType.getIssuerFileInformation() != null && fileInfoType.getIssuerFileInformation().getIssuerFileSet() != null) {
			
			//set default status
			fileProcDto.setSbmFileStatusType(SBMFileStatus.PENDING_FILES); 
						
			if(fileProcDto.getFileProcSummaryFromDB() != null) {
				// check if all file received		
				int totalFiles = fileInfoType.getIssuerFileInformation().getIssuerFileSet().getTotalIssuerFiles();
				int count = totalFiles - 1; //decrease by one for the current file

				for(SBMFileInfo fileInfo: fileProcDto.getFileProcSummaryFromDB().getSbmFileInfoList()) { 
					if( ! fileInfo.isRejectedInd()) {							
						count = count - 1;
					}
				}
				
				if(count == 0) {
					//All files received					
					LOG.info("All files received for SbmFileProcSumId:{}", fileProcDto.getFileProcSummaryFromDB().getSbmFileProcSumId());
				}
				else {
					//create warning 
					LOG.info("All files in fileset not received; SbmFileProcSumId {} : {} files not received.", fileProcDto.getFileProcSummaryFromDB().getSbmFileProcSumId(), count);
					return;
				}
			}
			else {
				LOG.info("fileId:{}, status set to PENDING_FILES", fileInfoType.getFileId());
				return;
			}
		}
		
		//check if there are pre-existing SBMI file in Accepted or InProcess status for the SBM state
		List<SBMSummaryAndFileInfoDTO> currentlyProcessing = fileCompositeDao.getAllInProcessOrPendingApprovalForState(getStateCd(fileInfoType));
		if(CollectionUtils.isNotEmpty(currentlyProcessing)) {
			LOG.info("Other file is in InProcess or pending approval for the state:{}; Files List:{}", getStateCd(fileInfoType), currentlyProcessing);
			fileProcDto.setSbmFileStatusType(SBMFileStatus.ON_HOLD); 
			LOG.info("fileId:{}, status set to ON_HOLD", fileInfoType.getFileId());
			return;

		}				
		
		fileProcDto.setSbmFileStatusType(SBMFileStatus.IN_PROCESS); 
	}
	
	/**
	 * @param freezePeriodStartDay the freezePeriodStartDay to set
	 */
	public void setFreezePeriodStartDay(int freezePeriodStartDay) {
		this.freezePeriodStartDay = freezePeriodStartDay;
	}

	/**
	 * @param freezePeriodEndDay the freezePeriodEndDay to set
	 */
	public void setFreezePeriodEndDay(int freezePeriodEndDay) {
		this.freezePeriodEndDay = freezePeriodEndDay;
	}

	/**
	 * @param fileCompositeDao the fileCompositeDao to set
	 */
	public void setFileCompositeDao(SBMFileCompositeDAO fileCompositeDao) {
		this.fileCompositeDao = fileCompositeDao;
	}
	
}
