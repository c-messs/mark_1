/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm;

import static gov.hhs.cms.ff.fm.eps.ep.enums.SBMErrorWarningCode.ER_022;
import static gov.hhs.cms.ff.fm.eps.ep.enums.SBMPolicyEnum.ISS_FILE_SET_ID;
import static gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil.greaterOf;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;

import com.accenture.foundation.common.exception.ApplicationException;

import gov.hhs.cms.ff.fm.eps.ep.enums.SBMErrorWarningCode;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMErrorDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileInfo;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMSummaryAndFileInfoDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SbmDataUtil;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SBMFileCompositeDAO;

/**
 * @author rajesh.talanki
 *
 */
public class SBMEvaluatePendingFiles {
	
	private static final Logger LOG = LoggerFactory.getLogger(SBMEvaluatePendingFiles.class);
		
	private int fileSetDeadlineHours;
	private int freezePeriodStartDay;
	private int freezePeriodEndDay;
	private SBMFileCompositeDAO fileCompositeDao;
	private SBMResponseGenerator responseGenerator;
	
	/**
	 * Evaluate all pending files for deadline expiration
	 * @param jobId
	 * @param sendSBMSForPendingFiles
	 * @throws JAXBException
	 * @throws SQLException
	 * @throws IOException
	 */
	public void evaluatePendingFiles(Long jobId, boolean sendSBMSForPendingFiles) throws JAXBException, SQLException, IOException {
		
		LOG.info("Evaluating all pending files.");
		LOG.info("fileSetDeadlineDays:{}", fileSetDeadlineHours);
		LOG.info("freezePeriodStartDay:{}, freezePeriodEndDay (Inclusive):{}", freezePeriodStartDay, freezePeriodEndDay);
		
		if(freezePeriodStartDay > freezePeriodEndDay) {
			throw new ApplicationException("Freeze period property values are incorrect freezePeriodStartDay should be less or equal to freezePeriodEndDay");
		}
		
		if(fileSetDeadlineHours < 0) {
			throw new ApplicationException("fileSetDeadlineHours property cannot be less than zero");
		}
		
		LocalDateTime currentTime = LocalDateTime.now();
		LocalDateTime freezeStartDateTime =  LocalDate.now().withDayOfMonth(freezePeriodStartDay).atStartOfDay();
		//freezeEndDateTime is exclusive so any dateTime less than freezeEndDateTime is freeze period but not equal to freezeEndDateTime 
		LocalDateTime freezeEndDateTime =  LocalDate.now().withDayOfMonth(freezePeriodEndDay).plusDays(1).atStartOfDay(); 
		LOG.info("In DateTime: freezeStartDateTime:{}, freezeEndDateTime (exclusive):{}", freezeStartDateTime, freezeEndDateTime);
		
		
		//No pending files will be evaluated during freeze period
		if((freezePeriodStartDay <= currentTime.getDayOfMonth()) && (currentTime.getDayOfMonth() <= freezePeriodEndDay)) {
			LOG.info("In Freeze period, No pending files were evaluated.");
			return;
		}
		
		//evaluate all pending file and set it to EXPIRED if deadline is passed
		List<SBMSummaryAndFileInfoDTO> pendingFiles = fileCompositeDao.getAllSBMFileProcessingSummary(SBMFileStatus.PENDING_FILES);
		for(SBMSummaryAndFileInfoDTO summaryDto : pendingFiles) {
			LOG.info("Evaluating pending files SbmFileProcSumId {}", summaryDto.getSbmFileProcSumId());
			LocalDateTime firstFileCreateDatetime = getFirstFileDateTime(summaryDto);
			if(firstFileCreateDatetime == null) {
				//This should never occur
				LOG.error("firstFileCreateDatetime is null for SbmFileProcSumId {}", summaryDto.getSbmFileProcSumId());
				continue;
			}
			
			//fileSetDeadline is exclusive
			LocalDateTime fileSetDeadline = firstFileCreateDatetime.plusHours(fileSetDeadlineHours);

			/* 
			 * If the deadline period overlap with freeze period then deadline need to be adjusted for overlapping period
			 * 
			 * Scenarios: deadline: 5 days
			 * 			FreezeStrt	FreezeEnd	firstFileDate	FilesetExpiration
			 * 				11			15			9				18
			 *  			11			15			12				20
			 *   			11			15			16				20
			 *     			11			15			5				9
			 *          	11			15			11				20
			 */

			LOG.info("currentDateTime: {}", currentTime);
			LOG.info("firstFileCreateDatetime: {}, fileSetDeadline(not adjusted for freeze period): {}", firstFileCreateDatetime, fileSetDeadline);
			if(currentTime.isBefore(fileSetDeadline)) {
				LOG.info("Deadline NOT expired for SbmFileProcSumId {}", summaryDto.getSbmFileProcSumId());
				if(sendSBMSForPendingFiles) {
					sendSBMSForAllSBMFileInfos(summaryDto);
				}
				continue;
			}
			else if(firstFileCreateDatetime.compareTo(freezeEndDateTime) >= 0 || fileSetDeadline.compareTo(freezeStartDateTime) <= 0 ) {
				LOG.info("Deadline expired for SbmFileProcSumId {}", summaryDto.getSbmFileProcSumId());
				summaryDto.setSbmFileStatusType(SBMFileStatus.EXPIRED);
				fileCompositeDao.updateFileStatus(summaryDto.getSbmFileProcSumId(), summaryDto.getSbmFileStatusType(), jobId);		
				sendSBMSForAllSBMFileInfos(summaryDto);
				continue;
			}
			//recalculate fileSetDeadline with adjusted duration
			else if(firstFileCreateDatetime.compareTo(freezeStartDateTime) <= 0 && fileSetDeadline.compareTo(freezeStartDateTime) > 0) {
				Duration adjustmentDuration = Duration.between(freezeStartDateTime, fileSetDeadline);
				fileSetDeadline = greaterOf(freezeEndDateTime, fileSetDeadline).plus(adjustmentDuration);
				LOG.info("firstFileCreateDatetime: {}, adjustmentDuration: {}, fileSetDeadline(adjusted for freeze period): {}", firstFileCreateDatetime, adjustmentDuration, fileSetDeadline);
			}
			else if(firstFileCreateDatetime.compareTo(freezeEndDateTime) < 0 && fileSetDeadline.compareTo(freezeEndDateTime) >= 0) {
				Duration adjustmentDuration = Duration.between(firstFileCreateDatetime, freezeEndDateTime);
				fileSetDeadline = greaterOf(freezeEndDateTime, fileSetDeadline).plus(adjustmentDuration);
				LOG.info("firstFileCreateDatetime: {}, adjustmentDuration: {}, fileSetDeadline(adjusted for freeze period): {}", firstFileCreateDatetime, adjustmentDuration, fileSetDeadline);
			}
			else if(firstFileCreateDatetime.compareTo(freezeStartDateTime) >= 0 && fileSetDeadline.compareTo(freezeEndDateTime) <= 0) {
				Duration adjustmentDuration = Duration.between(firstFileCreateDatetime, fileSetDeadline);
				fileSetDeadline = freezeEndDateTime.plus(adjustmentDuration);
				LOG.info("firstFileCreateDatetime: {}, adjustmentDuration: {}, fileSetDeadline(adjusted for freeze period): {}", firstFileCreateDatetime, adjustmentDuration, fileSetDeadline);
			}

			//compare again with adjusted fileSetDeadline
			if(currentTime.compareTo(fileSetDeadline) >= 0) {
				LOG.info("Deadline expired for SbmFileProcSumId {}", summaryDto.getSbmFileProcSumId());
				summaryDto.setSbmFileStatusType(SBMFileStatus.EXPIRED);
				fileCompositeDao.updateFileStatus(summaryDto.getSbmFileProcSumId(), summaryDto.getSbmFileStatusType(), jobId);		
				sendSBMSForAllSBMFileInfos(summaryDto);
				continue;
			}

			LOG.info("Deadline NOT expired for SbmFileProcSumId {}", summaryDto.getSbmFileProcSumId());
			if(sendSBMSForPendingFiles) {
				sendSBMSForAllSBMFileInfos(summaryDto);
			}

		}		
		
	}
	
	/**
	 * Evaluate all On-Hold files and set to IN_PROCESS if no other file is processing or pending approval for the SBM state	
	 * @param jobId
	 * @throws JAXBException
	 * @throws SQLException
	 * @throws IOException
	 */
	public void evaluateOnHoldFiles(Long jobId) throws JAXBException, SQLException, IOException {
		
		LOG.info("Evaluating all On-Hold files.");
		
		LocalDate today = LocalDate.now();
		
		//No freeze files will be evaluated during freeze period
		if((freezePeriodStartDay <= today.getDayOfMonth()) && (today.getDayOfMonth() <= freezePeriodEndDay)) {
			LOG.info("In Freeze period, No files were evaluated.");
			return;
		}
		
		//evaluate all On-Hold file and set it to either Pending files or inProcess if file is ready for processing
		List<SBMSummaryAndFileInfoDTO> onHoldFiles = fileCompositeDao.getAllSBMFileProcessingSummary(SBMFileStatus.ON_HOLD);
		for(SBMSummaryAndFileInfoDTO summaryDto : onHoldFiles) {
			LOG.info("Evaluating ON_HOLD for SbmFileProcSumId :{}", summaryDto.getSbmFileProcSumId());
			String stateCode = SbmDataUtil.getStateCd(summaryDto.getTenantId());
			List<SBMSummaryAndFileInfoDTO> currentlyInprocess = fileCompositeDao.getAllInProcessOrPendingApprovalForState(stateCode);
			if(CollectionUtils.isNotEmpty(currentlyInprocess)) {
				LOG.info("Other file(s) processing or pending approval for state :{}; List of files: {}", stateCode, currentlyInprocess);
				LOG.info("Leaving status as ON_HOLD and sending SBMS for SbmFileProcSumId :{}", summaryDto.getSbmFileProcSumId());
				sendSBMSForAllSBMFileInfos(summaryDto);
				continue;
			}
			else if(StringUtils.isBlank(summaryDto.getIssuerFileSetId()) || isIssuerFileSetFilesReceived(summaryDto)) {
				summaryDto.setSbmFileStatusType(SBMFileStatus.IN_PROCESS);
				fileCompositeDao.updateFileStatus(summaryDto.getSbmFileProcSumId(), summaryDto.getSbmFileStatusType(), jobId);
				LOG.info("Status updated from ON_HOLD to IN_PROCESS for SbmFileProcSumId :{}", summaryDto.getSbmFileProcSumId());
				fileCompositeDao.insertStagingSbmGroupLockForExtract(summaryDto.getSbmFileProcSumId());
				LOG.info("Inserted StagingSbmGroupLock for extract process for SbmFileProcSumId :{}", summaryDto.getSbmFileProcSumId());
			}
			else {
				//All files in fileSet not received
				summaryDto.setSbmFileStatusType(SBMFileStatus.PENDING_FILES);
				fileCompositeDao.updateFileStatus(summaryDto.getSbmFileProcSumId(), summaryDto.getSbmFileStatusType(), jobId);
				LOG.info("Status updated from ON_HOLD to PENDING_FILES for SbmFileProcSumId :{}", summaryDto.getSbmFileProcSumId());
				//SBMS will be sent when all PENDING_FILES files were evaluated later
			}
		}
		
	}
	
	/**
	 * Evaluate all FREEZE files and set to IN_PROCESS if no other file is processing or pending approval for the SBM state.
	 * 
	 * @param jobId
	 * @throws JAXBException
	 * @throws SQLException
	 * @throws IOException
	 */
	public void evaluateFreezeFiles(Long jobId) throws JAXBException, SQLException, IOException {
		
		LOG.info("Evaluating all FREEZE files.");
		
		LocalDate today = LocalDate.now();
						
		//evaluate all On-Hold file and set it to either Pending files or inProcess if file is ready for processing
		List<SBMSummaryAndFileInfoDTO> freezeFiles = fileCompositeDao.getAllSBMFileProcessingSummary(SBMFileStatus.FREEZE);
		for(SBMSummaryAndFileInfoDTO summaryDto : freezeFiles) {
			LOG.info("Evaluating FREEZE for SbmFileProcSumId :{}", summaryDto.getSbmFileProcSumId());
			
			//verify is in FREEZE period
			if((freezePeriodStartDay <= today.getDayOfMonth()) && (today.getDayOfMonth() <= freezePeriodEndDay)) {
				LOG.info("In Freeze period, sending SBMS for FREEZE files");
				sendSBMSForAllSBMFileInfos(summaryDto);
				continue;
			}
			
			String stateCode = SbmDataUtil.getStateCd(summaryDto.getTenantId());
			List<SBMSummaryAndFileInfoDTO> currentlyInprocess = fileCompositeDao.getAllInProcessOrPendingApprovalForState(stateCode);
			
			if(CollectionUtils.isNotEmpty(currentlyInprocess)) {
				LOG.info("Other file(s) processing or pending approval for state :{}; List of files: {}", stateCode, currentlyInprocess);
				
				if(StringUtils.isBlank(summaryDto.getIssuerFileSetId()) || isIssuerFileSetFilesReceived(summaryDto)) {
					LOG.info("setting status from FREEZE to ON_HOLD for SbmFileProcSumId :{}", summaryDto.getSbmFileProcSumId());
					summaryDto.setSbmFileStatusType(SBMFileStatus.ON_HOLD);
					fileCompositeDao.updateFileStatus(summaryDto.getSbmFileProcSumId(), summaryDto.getSbmFileStatusType(), jobId);
					//SBMS will be sent when all On-Hold files were evaluated later
				}
				else {
					LOG.info("All files in fileSet not received; setting status from FREEZE to PENDING_FILES for SbmFileProcSumId :{}"
							, summaryDto.getSbmFileProcSumId());
					summaryDto.setSbmFileStatusType(SBMFileStatus.PENDING_FILES);
					fileCompositeDao.updateFileStatus(summaryDto.getSbmFileProcSumId(), summaryDto.getSbmFileStatusType(), jobId);
					//SBMS will be sent when all PENDING_FILES files were evaluated later
				}
				continue;
			}
			else if(StringUtils.isBlank(summaryDto.getIssuerFileSetId()) || isIssuerFileSetFilesReceived(summaryDto)) {
				summaryDto.setSbmFileStatusType(SBMFileStatus.IN_PROCESS);
				fileCompositeDao.updateFileStatus(summaryDto.getSbmFileProcSumId(), summaryDto.getSbmFileStatusType(), jobId);
				LOG.info("Status updated from FREEZE to IN_PROCESS for SbmFileProcSumId :{}", summaryDto.getSbmFileProcSumId());
				fileCompositeDao.insertStagingSbmGroupLockForExtract(summaryDto.getSbmFileProcSumId());
				LOG.info("Inserted StagingSbmGroupLock for extract process for SbmFileProcSumId :{}", summaryDto.getSbmFileProcSumId());
			}
			else {
				//All files in fileSet not received
				summaryDto.setSbmFileStatusType(SBMFileStatus.PENDING_FILES);
				fileCompositeDao.updateFileStatus(summaryDto.getSbmFileProcSumId(), summaryDto.getSbmFileStatusType(), jobId);
				LOG.info("Status updated from FREEZE to PENDING_FILES for SbmFileProcSumId :{}", summaryDto.getSbmFileProcSumId());
				//SBMS will be sent when all PENDING_FILES files were evaluated later
			}
		}
		
	}
	
	/**
	 * Evaluate all BYPASS_FREEZE files and set to IN_PROCESS if no other file is processing or pending approval for the SBM state.
	 * 
	 * @param jobId
	 * @throws IOException 
	 * @throws SQLException 
	 * @throws JAXBException 
	 */
	public void evaluateBypassFreeze(Long jobId) throws JAXBException, SQLException, IOException {
		
		LOG.info("Evaluating all BypassFreeze files.");
		
		boolean isFreezePeriod = false;
		LocalDate today = LocalDate.now();		
		//check is in freeze period
		if((freezePeriodStartDay <= today.getDayOfMonth()) && (today.getDayOfMonth() <= freezePeriodEndDay)) {
			LOG.info("In Freeze period");
			isFreezePeriod = true;
		}
							
		//evaluate all BYPASS_FREEZE file and set it to inProcess if no other file is in InProcess or Accepted for SBM state
		List<SBMSummaryAndFileInfoDTO> bypassFreezeFiles = fileCompositeDao.getAllSBMFileProcessingSummary(SBMFileStatus.BYPASS_FREEZE);
		for(SBMSummaryAndFileInfoDTO summaryDto : bypassFreezeFiles) {
			LOG.info("Evaluating Bypass Freeze for SbmFileProcSumId :{}", summaryDto.getSbmFileProcSumId());
			String stateCode = SbmDataUtil.getStateCd(summaryDto.getTenantId());
			List<SBMSummaryAndFileInfoDTO> currentlyInprocess = fileCompositeDao.getAllInProcessOrPendingApprovalForState(stateCode);
			if(CollectionUtils.isNotEmpty(currentlyInprocess)) {
				LOG.info("Other file(s) processing or pending approval for state :{}", stateCode);
				LOG.info("List of files processing or pending approval :{}", currentlyInprocess);
				continue;
			}
			else if(StringUtils.isBlank(summaryDto.getIssuerFileSetId()) || isIssuerFileSetFilesReceived(summaryDto)) {
				summaryDto.setSbmFileStatusType(SBMFileStatus.IN_PROCESS);
				fileCompositeDao.updateFileStatus(summaryDto.getSbmFileProcSumId(), summaryDto.getSbmFileStatusType(), jobId);
				LOG.info("Status updated from BYPASS_FREEZE to IN_PROCESS for SbmFileProcSumId :{}", summaryDto.getSbmFileProcSumId());
				fileCompositeDao.insertStagingSbmGroupLockForExtract(summaryDto.getSbmFileProcSumId());
				LOG.info("Inserted StagingSbmGroupLock for extract process for SbmFileProcSumId :{}", summaryDto.getSbmFileProcSumId());
			}
			else if( ! isFreezePeriod) {
				//All files in fileSet not received
				summaryDto.setSbmFileStatusType(SBMFileStatus.PENDING_FILES);
				fileCompositeDao.updateFileStatus(summaryDto.getSbmFileProcSumId(), summaryDto.getSbmFileStatusType(), jobId);
				LOG.info("Status updated from BYPASS_FREEZE to PENDING_FILES for SbmFileProcSumId :{}", summaryDto.getSbmFileProcSumId());
				sendSBMSForAllSBMFileInfos(summaryDto);
			}
		}
	}
	
	/**
	 * Returns true if all files in fileSet received, otherwise returns false.
	 * @param summaryDto
	 * @return
	 */
	public static boolean isIssuerFileSetFilesReceived(SBMSummaryAndFileInfoDTO summaryDto) {
		
		if(StringUtils.isBlank(summaryDto.getIssuerFileSetId())) {
			return false;
		}
		
		// check if all file received		
		int totalFiles = summaryDto.getTotalIssuerFileCount();
		int count = totalFiles; 

		for(SBMFileInfo fileInfo: summaryDto.getSbmFileInfoList()) { 
			if( ! fileInfo.isRejectedInd()) {							
				count = count - 1;
			}
		}
		
		if(count == 0) {
			//All files received					
			LOG.info("All files received for SbmFileProcSumId:{}", summaryDto.getSbmFileProcSumId());
			return true;
		}
		else {
			//create warning 
			LOG.info("All files in fileset not received; SbmFileProcSumId {} : {} files not received.", summaryDto.getSbmFileProcSumId(), count);			
		}
		
		return false;
	}
	
	
	private void sendSBMSForAllSBMFileInfos(SBMSummaryAndFileInfoDTO summaryDto) throws JAXBException, SQLException, IOException {
		
		List<SBMErrorDTO> errorList = new ArrayList<>();

		if(SBMFileStatus.EXPIRED.equals(summaryDto.getSbmFileStatusType())) {
			errorList.add(SbmHelper.createErrorLog(ISS_FILE_SET_ID.getElementNm(), ER_022.getCode(), summaryDto.getIssuerFileSetId()));
		}
		else if(SBMFileStatus.PENDING_FILES.equals(summaryDto.getSbmFileStatusType())) {
			errorList.add(SbmHelper.createErrorLog(ISS_FILE_SET_ID.getElementNm(), SBMErrorWarningCode.WR_003.getCode(), summaryDto.getIssuerFileSetId()));
		}
		else if(SBMFileStatus.ON_HOLD.equals(summaryDto.getSbmFileStatusType())) {
			errorList.add(SbmHelper.createErrorLog(null, SBMErrorWarningCode.WR_002.getCode()));
		}		
		else if(SBMFileStatus.FREEZE.equals(summaryDto.getSbmFileStatusType())) {
			errorList.add(SbmHelper.createErrorLog(null, SBMErrorWarningCode.WR_001.getCode()));
		}
		
		//save errors for each SBMFileInfo
		for(SBMFileInfo fileInfo: summaryDto.getSbmFileInfoList()) {
			for(SBMErrorDTO error: errorList) {				
				error.setSbmFileInfoId(fileInfo.getSbmFileInfoId());				
				LOG.info("Saving error for sbmFileInfoId:{}; error: {}", fileInfo.getSbmFileInfoId(), error);				
				List<SBMErrorDTO> errors = new ArrayList<>();
				errors.add(error);
				try {
					fileCompositeDao.saveSBMFileErrors(errors);
					//TODO move this code to responseGenerator to generate SBMS only once for each error
				}
				catch(DuplicateKeyException e) {
					LOG.info("Error already exists for sbmFileInfoId:{}; error: {}", fileInfo.getSbmFileInfoId(), error);		
				}
			}			
		}
		
		if( ! errorList.isEmpty()) {			
			responseGenerator.generateSBMSForAllSBMInfos(summaryDto, errorList);
		}
	}
	
	private LocalDateTime getFirstFileDateTime(SBMSummaryAndFileInfoDTO summaryDto) {
		
		if(CollectionUtils.isNotEmpty(summaryDto.getSbmFileInfoList())) {
			return summaryDto.getSbmFileInfoList().get(0).getCreateDatetime();
		}
		
		return null;
	}
	
	/**
	 * @param fileSetDeadlineHours the fileSetDeadlineHours to set
	 */
	public void setFileSetDeadlineHours(int fileSetDeadlineHours) {
		this.fileSetDeadlineHours = fileSetDeadlineHours;
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

	/**
	 * @param responseGenerator the responseGenerator to set
	 */
	public void setResponseGenerator(SBMResponseGenerator responseGenerator) {
		this.responseGenerator = responseGenerator;
	}
	
}
