package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm;

import static gov.hhs.cms.ff.fm.eps.ep.sbm.SbmDataUtil.getStateCd;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.cms.dsh.sbmi.FileInformationType;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileInfo;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileProcessingDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMSummaryAndFileInfoDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SbmDataUtil;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SBMFileCompositeDAO;

/**
 * @author rajesh.talanki
 *
 */
public class SBMFileStatusHandler {

	private static final Logger LOG = LoggerFactory.getLogger(SBMFileStatusHandler.class);

	private int freezePeriodStartDay;
	private int freezePeriodEndDay;
	private SBMFileCompositeDAO fileCompositeDao;

	/**
	 * Determine and Set the SBMI File Status
	 * 
	 * @param fileProcDto
	 */
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
		if(fileInfoType.getIssuerFileInformation() != null && fileInfoType.getIssuerFileInformation().getIssuerFileSet() != null
				&& fileInfoType.getIssuerFileInformation().getIssuerFileSet().getTotalIssuerFiles() > 1) {

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

		// B36. Is there a file/fileset currently being processed for the SBM?
		boolean isFileProcessing = determineFileProcessing(fileInfoType);

		if (isFileProcessing) {
			//B43. Update File Status from “In Process” to “On Hold”
			fileProcDto.setSbmFileStatusType(SBMFileStatus.ON_HOLD);
			return;
		}				

		fileProcDto.setSbmFileStatusType(SBMFileStatus.IN_PROCESS); 
	}


	/**
	 * Determine if any state files are in process, or if a file
	 * for the inbound issuer file already has a file for that 
	 * issuer currently in process (status ACC, ACE, ACW, or IPC). 
	 * @param fileInfoType
	 * @return
	 */
	private boolean determineFileProcessing(FileInformationType fileInfoType) {

		boolean isFileProcessing = false;
		String stateCd = getStateCd(fileInfoType);
		String issuerId = SbmDataUtil.getIssuerId(fileInfoType);
		boolean isIssuerFile = (issuerId != null);
		
		// Get all pending files for state in ACC, ACE, ACW, or IPC status
		List<SBMSummaryAndFileInfoDTO> inProcSummaryList = fileCompositeDao.getAllInProcessOrPendingApprovalForState(stateCd);

		if (isIssuerFile) {
			
			for (SBMSummaryAndFileInfoDTO inProcSummaryDTO : inProcSummaryList) {
				// If issuerId == null then it is a State file
				// If any state file is in process, then hold all other inbound files.
				if (inProcSummaryDTO.getIssuerId() == null) {
					isFileProcessing = true;
					break;
				} else {
					// If another file for this issuer is in process, then hold this issuer file.
					if (inProcSummaryDTO.getIssuerId().equals(issuerId)) {
						isFileProcessing = true;
						break;
					}
				}
			} // END for
		} else {
			// If any file is in process when inbound file is a state file.
			if (!inProcSummaryList.isEmpty()) {
				
				isFileProcessing = true;
			}
		}
		return isFileProcessing;
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
