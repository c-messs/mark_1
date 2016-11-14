package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm.updatestatus;

import static gov.hhs.cms.ff.fm.eps.ep.enums.SBMErrorWarningCode.ER_500;
import static gov.hhs.cms.ff.fm.eps.ep.enums.SBMErrorWarningCode.ER_517;
import static gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus.ACCEPTED;
import static gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus.ACCEPTED_WITH_ERRORS;
import static gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus.ACCEPTED_WITH_WARNINGS;
import static gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus.APPROVED;
import static gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus.APPROVED_WITH_ERRORS;
import static gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus.APPROVED_WITH_WARNINGS;
import static gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus.FREEZE;
import static gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus.ON_HOLD;
import static gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus.PENDING_FILES;
import static gov.hhs.cms.ff.fm.eps.ep.jobs.sbm.SBMEvaluatePendingFiles.isIssuerFileSetFilesReceived;
import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMCache.getErrorDescription;
import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.BATCH_RUN_DATE;
import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.ERR_RPT_HEADER;
import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.INPUT_FILE_NAME;
import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.NUM_OF_ERRORS;
import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.PROCESS_NAME;
import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.PROCESS_NAME_VAL;
import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.RPT_DELIMITER;
import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.RPT_LINE_DELIMITER;
import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.STATUS_APPROVE;
import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.STATUS_BACKOUT;
import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.STATUS_BYPASS_FREEZE;
import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.STATUS_DISAPPROVED;
import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.TARGET_EFT_APPLICATION_TYPE;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.hhs.cms.ff.fm.eps.dispatcher.EFTDispatchDriver;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMErrorWarningCode;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus;
import gov.hhs.cms.ff.fm.eps.ep.jobs.CommonUtil;
import gov.hhs.cms.ff.fm.eps.ep.jobs.sbm.SBMResponseGenerator;
import gov.hhs.cms.ff.fm.eps.ep.jobs.sbm.SbmHelper;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMCache;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMSummaryAndFileInfoDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMUpdateStatusErrorDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMUpdateStatusRecordDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SbmDataUtil;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SBMFileCompositeDAO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SbmUpdateStatusDataService;

/**
 * @author rajesh.talanki
 *
 */
public class SbmUpdateStatusProcessor {

	private static final Logger LOG = LoggerFactory.getLogger(SbmUpdateStatusProcessor.class);
	
	private static final int COLUMN_LINENUMBER = 0;
	private static final int COLUMN_TENANTID = 1;
	private static final int COLUMN_ISSUERID = 2;
	private static final int COLUMN_FILEID = 3;
	private static final int COLUMN_ISSUERFILESETID = 4;
	private static final int COLUMN_STATUS = 5;
	
	private Map<String, List<String>> fileIdMap = new HashMap<>();
	private Map<String, List<String>> fileSetIdMap = new HashMap<>();
	
	private SBMFileCompositeDAO fileCompositeDao;
	private SBMResponseGenerator responseGenerator;
	private EFTDispatchDriver eftDispatcher;
	private SbmUpdateStatusDataService updateStatusDataService;	
	private String environmentCodeSuffix;	
	
	public boolean isSBMIJobRunning() {		
		return fileCompositeDao.isSBMIJobRunning();
	}
	
	/**
	 * Perform Update Status
	 * @param inputFile
	 * @param jobId
	 * @throws IOException
	 * @throws SQLException
	 * @throws JAXBException
	 */
	public void processUpdateStatus(File inputFile, Long jobId) throws IOException, SQLException, JAXBException {
				
		List<CSVRecord> lineItems = SbmHelper.readCSVFile(inputFile);
		
		List<SBMUpdateStatusRecordDTO> fileRecords = new ArrayList<>();
		List<SBMUpdateStatusErrorDTO> errorList = new ArrayList<>();
		
		if(lineItems.isEmpty()) {
			errorList.add(SbmHelper.createError(null, ER_517.getCode(), SBMCache.getErrorDescription(ER_517.getCode()), null, null));
		}
				
		for(CSVRecord csvRecord: lineItems) {	
			
			SBMUpdateStatusRecordDTO recordDto = mapToDto(csvRecord);			
			if(csvRecord.getRecordNumber() == 1) {
				// validate headers
				errorList.addAll(validateHeaders(recordDto));
				continue;
			}
			
			fileRecords.add(recordDto);
								
			errorList.addAll(validateRowContents(recordDto));	
			
			//this is to find duplicate FileId in the file
			if(StringUtils.isNotBlank(recordDto.getFileId()) && StringUtils.isNotBlank(recordDto.getLinenumber())) {
				if(fileIdMap.get(recordDto.getFileId()) == null) {
					fileIdMap.put(recordDto.getFileId(), new ArrayList<>());
				}
				
				fileIdMap.get(recordDto.getFileId()).add(recordDto.getLinenumber());
			}
			
			//this is to find duplicate IssuerFileSetId in the file
			if(StringUtils.isNotBlank(recordDto.getIssuerFileSetId()) && StringUtils.isNotBlank(recordDto.getLinenumber())) {
				if(fileSetIdMap.get(recordDto.getIssuerFileSetId()) == null) {
					fileSetIdMap.put(recordDto.getIssuerFileSetId(), new ArrayList<>());
				}
				
				fileSetIdMap.get(recordDto.getIssuerFileSetId()).add(recordDto.getLinenumber());
			}
		}
		
		//validate duplicate fileId
		for(Entry<String, List<String>> entry: fileIdMap.entrySet()) {
			if(entry.getValue().size() > 1) {
				for(String linenumber: entry.getValue()) {
					errorList.add(SbmHelper.createError(linenumber, SBMErrorWarningCode.ER_516));
				}
			}
		}
		
		//validate duplicate fileSetId
		for(Entry<String, List<String>> entry: fileSetIdMap.entrySet()) {
			if(entry.getValue().size() > 1) {
				for(String linenumber: entry.getValue()) {
					errorList.add(SbmHelper.createError(linenumber, SBMErrorWarningCode.ER_516));
				}
			}
		}
		
		
		if( ! errorList.isEmpty()) {
			// generate error report
			generateErrorReport(inputFile.getName(), errorList);
			return;
		}
		
		//process each line item		
		for(SBMUpdateStatusRecordDTO dto: fileRecords) {
			if(STATUS_APPROVE.equalsIgnoreCase(dto.getStatus())) {
				//perform approval 
				updateStatusDataService.executeApproval(jobId, dto.getSbmFileProcSumId());
				
				if(ACCEPTED.equals(dto.getCurrentFileStatus())) {
					dto.setNewFileSatus(SBMFileStatus.APPROVED);
				}
				else if(ACCEPTED_WITH_ERRORS.equals(dto.getCurrentFileStatus())) {
					dto.setNewFileSatus(SBMFileStatus.APPROVED_WITH_ERRORS);
				}
				else if(ACCEPTED_WITH_WARNINGS.equals(dto.getCurrentFileStatus())) {
					dto.setNewFileSatus(SBMFileStatus.APPROVED_WITH_WARNINGS);
				}
				
				//geneate SBMR
				responseGenerator.generateSBMRForUpdateStatus(dto, jobId);
			}
			else if(SBMConstants.STATUS_DISAPPROVED.equalsIgnoreCase(dto.getStatus())) {
				//perform disapproval
				updateStatusDataService.executeDisapproval(jobId, dto.getSbmFileProcSumId());
				dto.setNewFileSatus(SBMFileStatus.DISAPPROVED);
				responseGenerator.generateSBMRForUpdateStatus(dto, jobId);
			}
			else if(SBMConstants.STATUS_BYPASS_FREEZE.equalsIgnoreCase(dto.getStatus())) {
				//perform bypass freeze: update file status and NO SBMR required
				fileCompositeDao.updateFileStatus( dto.getSbmFileProcSumId(), SBMFileStatus.BYPASS_FREEZE, jobId);
				dto.setNewFileSatus(SBMFileStatus.BYPASS_FREEZE);			
			}
			else if(SBMConstants.STATUS_BACKOUT.equalsIgnoreCase(dto.getStatus())) {
				//perform file reversal
				updateStatusDataService.executeFileReversal(jobId, dto.getSbmFileProcSumId());
				dto.setNewFileSatus(SBMFileStatus.BACKOUT);
				//no SBMR is required
				fileCompositeDao.updateFileStatus( dto.getSbmFileProcSumId(), SBMFileStatus.BACKOUT, jobId);
			}
			
			LOG.info("SBMUpdateStatusRecordDTO : {}", dto);			
		}
				
	}
		
	private List<SBMUpdateStatusErrorDTO> validateRowContents(SBMUpdateStatusRecordDTO recordDto) {
				
		List<SBMUpdateStatusErrorDTO> errorList = new ArrayList<>();

		if(StringUtils.isBlank(recordDto.getLinenumber())) {
			errorList.add(SbmHelper.createError(null, ER_500.getCode(), getErrorDescription(ER_500.getCode()), recordDto.getFileId(), recordDto.getIssuerFileSetId()));
			return errorList;
		}

		if(StringUtils.isBlank(recordDto.getTenantId())) {
			errorList.add(SbmHelper.createError(recordDto.getLinenumber(), SBMErrorWarningCode.ER_501));
		}

		if(StringUtils.isBlank(recordDto.getIssuerId()) && StringUtils.isNotBlank(recordDto.getIssuerFileSetId())) {
			errorList.add(SbmHelper.createError(recordDto.getLinenumber(), SBMErrorWarningCode.ER_505));
		}

		if(StringUtils.isBlank(recordDto.getFileId()) && StringUtils.isBlank(recordDto.getIssuerFileSetId())) {
			errorList.add(SbmHelper.createError(recordDto.getLinenumber(), SBMErrorWarningCode.ER_503));
		}

		if(StringUtils.isNotBlank(recordDto.getFileId()) && StringUtils.isNotBlank(recordDto.getIssuerFileSetId())) {
			errorList.add(SbmHelper.createError(recordDto.getLinenumber(), SBMErrorWarningCode.ER_504));
		}

		if(StringUtils.isBlank(recordDto.getStatus())) {
			errorList.add(SbmHelper.createError(recordDto.getLinenumber(), SBMErrorWarningCode.ER_502));
		}
		else if( ! CommonUtil.isStringMatched(recordDto.getStatus(), STATUS_APPROVE, STATUS_DISAPPROVED, STATUS_BYPASS_FREEZE, STATUS_BACKOUT)) {
			errorList.add(SbmHelper.createError(recordDto.getLinenumber(), SBMErrorWarningCode.ER_518)); 
		}
		
		if( ! errorList.isEmpty()) {			
			return errorList;
		}
		
		//perform business validations
		List<SBMSummaryAndFileInfoDTO> summaryDtoList;
		if(StringUtils.isNotBlank(recordDto.getFileId())) {
			//get summaryId
			summaryDtoList = fileCompositeDao.performSbmFileMatch(recordDto.getFileId(), recordDto.getTenantId());
		}
		else {
			summaryDtoList = fileCompositeDao.getSBMFileProcessingSummary(recordDto.getIssuerId(), recordDto.getIssuerFileSetId(), recordDto.getTenantId());
		}
		
		//if the file submission was a fileSet then fileSetId should be provided instead of fileId
		for(SBMSummaryAndFileInfoDTO summaryDto: summaryDtoList) {
			if(StringUtils.isNotBlank(recordDto.getFileId()) && StringUtils.isNotBlank(summaryDto.getIssuerFileSetId())) {
				errorList.add(SbmHelper.createError(recordDto.getLinenumber(), SBMErrorWarningCode.ER_519));
				return errorList;
			}
		}

		//validate based on status
		if(STATUS_APPROVE.equalsIgnoreCase(recordDto.getStatus())) {				
			
			if(CollectionUtils.isEmpty(summaryDtoList)) {
				errorList.add(SbmHelper.createError(recordDto.getLinenumber(), SBMErrorWarningCode.ER_509));
			}
			else {
				for(SBMSummaryAndFileInfoDTO summaryDto: summaryDtoList) {
					if(SbmHelper.isFileStatusMatched(summaryDto.getSbmFileStatusType(), ACCEPTED, ACCEPTED_WITH_ERRORS, ACCEPTED_WITH_WARNINGS)) {
						recordDto.setSbmFileProcSumId(summaryDto.getSbmFileProcSumId());
						recordDto.setCurrentFileStatus(summaryDto.getSbmFileStatusType());
						return errorList;
					}
				}
				//create error
				errorList.add(SbmHelper.createError(recordDto.getLinenumber(), SBMErrorWarningCode.ER_510));
			}
		}
		else if(STATUS_DISAPPROVED.equalsIgnoreCase(recordDto.getStatus())) {
			
			if(CollectionUtils.isEmpty(summaryDtoList)) {
				errorList.add(SbmHelper.createError(recordDto.getLinenumber(), SBMErrorWarningCode.ER_511));
			}
			else {
				for(SBMSummaryAndFileInfoDTO summaryDto: summaryDtoList) {
					if(SbmHelper.isFileStatusMatched(summaryDto.getSbmFileStatusType(), ACCEPTED, ACCEPTED_WITH_ERRORS, ACCEPTED_WITH_WARNINGS, ON_HOLD, FREEZE, PENDING_FILES)) {
						recordDto.setSbmFileProcSumId(summaryDto.getSbmFileProcSumId());
						recordDto.setCurrentFileStatus(summaryDto.getSbmFileStatusType());
						return errorList;
					}
				}
				//create error
				errorList.add(SbmHelper.createError(recordDto.getLinenumber(), SBMErrorWarningCode.ER_512));
			}
			
		}
		else if(STATUS_BYPASS_FREEZE.equalsIgnoreCase(recordDto.getStatus())) {

			if(CollectionUtils.isEmpty(summaryDtoList)) {
				errorList.add(SbmHelper.createError(recordDto.getLinenumber(), SBMErrorWarningCode.ER_520));
			}
			else {
				for(SBMSummaryAndFileInfoDTO summaryDto: summaryDtoList) {
					if(SbmHelper.isFileStatusMatched(summaryDto.getSbmFileStatusType(), FREEZE)) {
						if(StringUtils.isBlank(summaryDto.getIssuerFileSetId()) || isIssuerFileSetFilesReceived(summaryDto)) {
							recordDto.setSbmFileProcSumId(summaryDto.getSbmFileProcSumId());
							recordDto.setCurrentFileStatus(summaryDto.getSbmFileStatusType());
							return errorList;
						}
						else {
							//incomplete file set
							errorList.add(SbmHelper.createError(recordDto.getLinenumber(), SBMErrorWarningCode.ER_522));
							return errorList;
						}
					}
				}
				//create error
				errorList.add(SbmHelper.createError(recordDto.getLinenumber(), SBMErrorWarningCode.ER_521));
			}
			
		}
		else if(STATUS_BACKOUT.equalsIgnoreCase(recordDto.getStatus())) {
							
			if(CollectionUtils.isEmpty(summaryDtoList)) {
				errorList.add(SbmHelper.createError(recordDto.getLinenumber(), SBMErrorWarningCode.ER_513));
			}
			else {
				for(SBMSummaryAndFileInfoDTO summaryDto: summaryDtoList) {
					if(SbmHelper.isFileStatusMatched(summaryDto.getSbmFileStatusType(), APPROVED, APPROVED_WITH_ERRORS, APPROVED_WITH_WARNINGS)) {
						//create error	ER-514 if the file is not latest file
						LOG.info("SBMSummaryAndFileInfoDTO for the given lineitem: {}", summaryDto);
						SBMSummaryAndFileInfoDTO latestSummaryDto = null;
						if(StringUtils.isNotBlank(recordDto.getIssuerId())) {
							LOG.info("Getting latest SBM Summary record by issuerId:{}", recordDto.getIssuerId());
							latestSummaryDto = fileCompositeDao.getLatestSBMFileProcessingSummaryByIssuer(recordDto.getIssuerId());
						}
						else {
							LOG.info("Getting latest SBM Summary record by stateCode:{}", SbmDataUtil.getStateCd(recordDto.getTenantId()));
							latestSummaryDto = fileCompositeDao.getLatestSBMFileProcessingSummaryByState(SbmDataUtil.getStateCd(recordDto.getTenantId()));
						}
						
						LOG.info("latestSummaryDto : {}", latestSummaryDto);
						
						if(latestSummaryDto == null || latestSummaryDto.getSbmFileProcSumId() == null 
								|| latestSummaryDto.getSbmFileProcSumId().equals(summaryDto.getSbmFileProcSumId())) {						
							recordDto.setSbmFileProcSumId(summaryDto.getSbmFileProcSumId());
							recordDto.setCurrentFileStatus(summaryDto.getSbmFileStatusType());
							return errorList;
						}
						else {
							LOG.info("latestSummaryDto Not matched creating error");
							errorList.add(SbmHelper.createError(recordDto.getLinenumber(), SBMErrorWarningCode.ER_514));
							return errorList;
						}
					}
				}
				//create error
				errorList.add(SbmHelper.createError(recordDto.getLinenumber(), SBMErrorWarningCode.ER_515));
			}
		}
		
		return errorList;
	}
	
	private List<SBMUpdateStatusErrorDTO> validateHeaders(SBMUpdateStatusRecordDTO recordDto) {
		
		List<SBMUpdateStatusErrorDTO> errorList = new ArrayList<>();
		
		if( ! (SBMConstants.HDR_LINE_NUMBER.equalsIgnoreCase(recordDto.getLinenumber()) 
				&& SBMConstants.HDR_TENANT_ID.equalsIgnoreCase(recordDto.getTenantId())
				&& SBMConstants.HDR_ISSUER_ID.equalsIgnoreCase(recordDto.getIssuerId())
				&& SBMConstants.HDR_FILEID.equalsIgnoreCase(recordDto.getFileId())
				&& SBMConstants.HDR_ISSUER_FILESET_ID.equalsIgnoreCase(recordDto.getIssuerFileSetId())
				&& SBMConstants.HDR_STATUS.equalsIgnoreCase(recordDto.getStatus())
				)) {
			errorList.add(SbmHelper.createError(recordDto.getLinenumber(), SBMErrorWarningCode.ER_517));
		}
		
		return errorList;
	}

	private void generateErrorReport(String filename, List<SBMUpdateStatusErrorDTO> errorList) throws SQLException, IOException {
		
		StringBuffer reportString = new StringBuffer();
		
		reportString.append(PROCESS_NAME);
		reportString.append(RPT_DELIMITER + PROCESS_NAME_VAL + RPT_LINE_DELIMITER);
		reportString.append(INPUT_FILE_NAME + RPT_DELIMITER + filename + RPT_LINE_DELIMITER);
		reportString.append(BATCH_RUN_DATE + RPT_DELIMITER + LocalDate.now() + RPT_LINE_DELIMITER);
		reportString.append(NUM_OF_ERRORS + RPT_DELIMITER + errorList.size() + RPT_LINE_DELIMITER);
		
		reportString.append(ERR_RPT_HEADER);
		
		errorList.forEach(error -> {
			
			reportString.append(error.getLineNumber() + RPT_DELIMITER 
					+ error.getErrorCode() + RPT_DELIMITER 
					+ error.getErrorDescription() + RPT_DELIMITER
					+ error.getFileId() + RPT_DELIMITER 
					+ error.getFileSetId()
					+ RPT_LINE_DELIMITER);
		});
		
		LOG.info("CMS Error Report: " + reportString);
		
		String reportFilename = EFTDispatchDriver.getFileID(SBMConstants.FUNCTION_CODE_SBMAR);
		
		Long physicalDocId = eftDispatcher.saveDispatchContent(reportString.toString().getBytes(), reportFilename, SBMConstants.FUNCTION_CODE_SBMAR, environmentCodeSuffix, null, 0, null, TARGET_EFT_APPLICATION_TYPE); 
		LOG.info("SBMAR PhysicalDocumentId: {} for CSMS Approval/Disapprovaal template: {}", physicalDocId, filename);	
	}
	
	
	private SBMUpdateStatusRecordDTO mapToDto(CSVRecord record) {
		
		SBMUpdateStatusRecordDTO dto = new SBMUpdateStatusRecordDTO();
		
		dto.setLinenumber(getColumn(record, COLUMN_LINENUMBER));
		dto.setTenantId(getColumn(record, COLUMN_TENANTID));
		dto.setIssuerId(getColumn(record, COLUMN_ISSUERID));
		dto.setFileId(getColumn(record, COLUMN_FILEID));
		dto.setIssuerFileSetId(getColumn(record, COLUMN_ISSUERFILESETID));
		dto.setStatus(getColumn(record, COLUMN_STATUS));
		
		dto.setCsvRecord(record);
		
		return dto;
	}
	
	private String getColumn(CSVRecord record, int columnIndex) {		
		
		try {
			return StringUtils.trimToEmpty(record.get(columnIndex));
		}
		catch(IndexOutOfBoundsException e) {
			return StringUtils.EMPTY;
		}		
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

	/**
	 * @param eftDispatcher the eftDispatcher to set
	 */
	public void setEftDispatcher(EFTDispatchDriver eftDispatcher) {
		this.eftDispatcher = eftDispatcher;
	}

	/**
	 * @param updateStatusDataService the updateStatusDataService to set
	 */
	public void setUpdateStatusDataService(SbmUpdateStatusDataService updateStatusDataService) {
		this.updateStatusDataService = updateStatusDataService;
	}
	
	/**
	 * @param environmentCodeSuffix the environmentCodeSuffix to set
	 */
	public void setEnvironmentCodeSuffix(String environmentCodeSuffix) {
		this.environmentCodeSuffix = environmentCodeSuffix;
	}
	
}
