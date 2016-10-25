package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileProcessingDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SBMFileCompositeDAO;

/**
 * @author rajesh.talanki
 *
 */
public class SbmiFileIngestionWriter {
	
	private static final Logger LOG = LoggerFactory.getLogger(SbmiFileIngestionWriter.class);
	
	private SBMFileCompositeDAO fileCompositeDao;
	private File processedFolder;
	private File invalidFolder;
	private SBMResponseGenerator responseGenerator;

	/**
	 * The write method to update data
	 * 
	 * @param dto
	 * @throws Exception
	 */
	public void write(SBMFileProcessingDTO dto) throws Exception {

		if(dto == null) {
			return;
		}

		if(dto.getFileProcSummaryFromDB() == null) {
			// write SBMFileProcessingSummary 
			LOG.info("Creating SbmFileProcessingSummary");
			Long sbmFileProcSumId = fileCompositeDao.saveSbmFileProcessingSummary(dto);
			dto.setSbmFileProcSumId(sbmFileProcSumId);
			dto.getSbmFileInfo().setSbmFileProcessingSummaryId(sbmFileProcSumId);
		}
		else {
			//set summaryId from the one found in DB
			dto.getSbmFileInfo().setSbmFileProcessingSummaryId(dto.getFileProcSummaryFromDB().getSbmFileProcSumId());
			dto.setSbmFileProcSumId(dto.getFileProcSummaryFromDB().getSbmFileProcSumId()); 
			LOG.info("File associated to SbmFileProcSumId: {}", dto.getFileProcSummaryFromDB().getSbmFileProcSumId());
			//update fileStatus only if file is not rejected and new status is set for the summary record
			if( ! (dto.getSbmFileInfo().isRejectedInd() 
					|| dto.getSbmFileStatusType().equals(dto.getFileProcSummaryFromDB().getSbmFileStatusType()))) {
				fileCompositeDao.updateFileStatus(dto.getFileProcSummaryFromDB().getSbmFileProcSumId(), dto.getSbmFileStatusType(), dto.getBatchId());
				LOG.info("SbmFileProcSumId: {}, status updated to {}", dto.getFileProcSummaryFromDB().getSbmFileProcSumId(), dto.getSbmFileStatusType().getValue());
				dto.getFileProcSummaryFromDB().setSbmFileStatusType(dto.getSbmFileStatusType());
			}
		}
		
		//insert record into StagingSbmGroupLock for extract process only if the status is set to IN_PROCESS
		if(SBMFileStatus.IN_PROCESS.equals(dto.getSbmFileStatusType())) {
			fileCompositeDao.insertStagingSbmGroupLockForExtract(dto.getSbmFileProcSumId());
			LOG.info("Inserted StagingSbmGroupLock for extract process for SbmFileProcSumId :{}", dto.getSbmFileProcSumId());
		}

		//save SBMFileInfo, SBMFileError, SBMFileErrorAdditionalInfo
		LOG.info("Saving SBMFileInfo and errors if exists");
		Long sbmFileInfoId = fileCompositeDao.saveFileInfoAndErrors(dto);
		dto.getSbmFileInfo().setSbmFileInfoId(sbmFileInfoId);

		//save StagingSBMFile
		if(dto.isValidXML()) {
			
			InputStream in = new FileInputStream(dto.getSbmiFile());
			InputStreamReader reader=new InputStreamReader(in);  
			dto.setSbmFileXMLStream(reader);
			
			LOG.info("Saving SBMI file to StagingSBMFile");
			fileCompositeDao.saveFileToStagingSBMFile(dto);
		}
				
		// create SBMS only if file is rejected; SBMS for other status like FREEZE, ON_HOLD, PENDING_FILES will be sent when files are re-evaluated.
		if(dto.getSbmFileInfo().isRejectedInd()) { 
			//create SBMS and dispatch
			responseGenerator.generateSBMS(dto);
		}
		
		// move file to archive 
		String filename = dto.getSbmFileInfo().getSbmFileNm();
		File destFolder = processedFolder;
		if( ! dto.isValidXML()) {
			destFolder = invalidFolder;				
		}
		
		File destFile = new File(destFolder, filename);
		
		try {
			if(dto.getSbmiFile().exists()) {
				LOG.info("Moving file from {} to {}", dto.getSbmiFile(), destFile);
				FileUtils.moveFile(dto.getSbmiFile(), destFile);
			}
		}
		catch(FileExistsException e) {
			LOG.info("Destination file already exists; filename will be appended with timestamp");
			//File already exists so append filename with timestamp
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
			filename = filename + "_" + LocalDateTime.now().format(formatter);
			destFile = new File(destFolder, filename);
			LOG.info("Moving file from {} to {}", dto.getSbmiFile(), destFile);
			FileUtils.moveFile(dto.getSbmiFile(), destFile);
		}

	}

	/**
	 * @param fileCompositeDao the fileCompositeDao to set
	 */
	public void setFileCompositeDao(SBMFileCompositeDAO fileCompositeDao) {
		this.fileCompositeDao = fileCompositeDao;
	}

	/**
	 * @param processedFolder the processedFolder to set
	 */
	public void setProcessedFolder(File processedFolder) {
		this.processedFolder = processedFolder;
	}

	/**
	 * @param invalidFolder the invalidFolder to set
	 */
	public void setInvalidFolder(File invalidFolder) {
		this.invalidFolder = invalidFolder;
	}

	/**
	 * @param responseGenerator the responseGenerator to set
	 */
	public void setResponseGenerator(SBMResponseGenerator responseGenerator) {
		this.responseGenerator = responseGenerator;
	}
	

}
