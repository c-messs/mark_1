/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm;

import static gov.hhs.cms.ff.fm.eps.ep.enums.SBMErrorWarningCode.ER_012;
import static gov.hhs.cms.ff.fm.eps.ep.enums.SBMErrorWarningCode.ER_013;
import static gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus.DISAPPROVED;
import static gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus.REJECTED;
import static gov.hhs.cms.ff.fm.eps.ep.jobs.sbm.SbmHelper.createErrorLog;
import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.N;
import static gov.hhs.cms.ff.fm.eps.ep.sbm.SbmDataUtil.getStateCd;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import com.accenture.foundation.common.exception.EnvironmentException;

import gov.cms.dsh.sbmi.FileInformationType;
import gov.hhs.cms.ff.fm.eps.ep.StateProrationConfiguration;
import gov.hhs.cms.ff.fm.eps.ep.enums.EProdEnum;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMErrorWarningCode;
import gov.hhs.cms.ff.fm.eps.ep.jobs.CommonUtil;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMCache;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMErrorDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileInfo;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileProcessingDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMSummaryAndFileInfoDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SBMFileCompositeDAO;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;

/**
 * @author rajesh.talanki
 *
 */
public class SbmiFileIngestionReader  {

	private static final Logger LOG = LoggerFactory.getLogger(SbmiFileIngestionReader.class);
	private static final String ZIPPED_FILE_NAME_FORMAT = "\\w{1,10}\\.\\w{1,10}\\.\\w{1,10}\\.D(\\d{6})\\.T(\\d{9})(\\.\\w?+)?(\\.\\w*+)?";
	private static final String LOCK_EXTENSION = "\\.lock$";
	private static final int FILE_EXISTS_MAX_LOOP_CNT = 10;

	private File eftFolder;
	private File privateFolder;
	private File processedFolder;
	private File zipFilesFolder;
	private SbmXMLValidator xmlValidator;
	private SBMFileCompositeDAO fileCompositeDao;
	private SbmFileValidator fileValidator;
	private SBMFileStatusHandler fileSatusHandler;
	private String environmentCd;

	private DateTimeFormatter zipFormatter = DateTimeFormatter.ofPattern(SBMConstants.FILENAME_ZIP_PATTERN);
	private DateTimeFormatter gzipFormatter = DateTimeFormatter.ofPattern(SBMConstants.FILENAME_GZIP_PATTERN);


	/**
	 * @param jobId
	 * @return fileProcDto
	 * @throws Exception
	 * @throws UnexpectedInputException
	 * @throws ParseException
	 * @throws NonTransientResourceException
	 */
	public SBMFileProcessingDTO read(Long jobId) throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {

		//Get a file from EFT
		SBMFileProcessingDTO fileProcDto = getAFileToProcessFromEFT();

		if(fileProcDto == null) {
			LOG.info("No files in EFT folder; checking private folder");
			fileProcDto = getAFileToProcessFromPrivateFolder();
		}

		if(fileProcDto == null) {
			LOG.info("No files to process");
			return null;
		}

		LOG.info("Processing filename:{}, jobId:{}", fileProcDto.getSbmFileInfo().getSbmFileNm(), jobId);
		fileProcDto.setBatchId(jobId);

		//reject if zip file validation errors exists
		processCompressedFiles(fileProcDto);

		if(CollectionUtils.isNotEmpty(fileProcDto.getErrorList())) {
			LOG.info("Errors In Compressed files");
			return fileProcDto;
		}


		List<SBMErrorDTO> errorList = fileProcDto.getErrorList();
		SBMFileInfo fileInfo = fileProcDto.getSbmFileInfo();

		//duplicate file is allowed only if file status is rejected or disapproved
		List<SBMSummaryAndFileInfoDTO> summaryDtoList = fileCompositeDao.getFileStatus(fileInfo.getSbmFileNm());		
		if( SbmHelper.isNotRejectedNotDisapproved(summaryDtoList)) {
			//create error - duplicate file
			errorList.add(createErrorLog(null, ER_012.getCode()));
			fileProcDto.setSbmFileStatusType(REJECTED); 
			fileInfo.setRejectedInd(true);
			LOG.info("Duplicate file; Rejecting file");
			return fileProcDto;
		}

		if( ! xmlValidator.isValidXML(fileProcDto.getSbmiFile())) {			
			//create error - invalid xml
			errorList.add(createErrorLog(null, ER_013.getCode()));
			fileProcDto.setSbmFileStatusType(REJECTED); 
			fileInfo.setRejectedInd(true);
			LOG.info("Invalid xml; Rejecting file");
			return fileProcDto;
		}

		//set valid xml to save inbound file to DB
		fileProcDto.setValidXML(true);

		// check schema errors
		errorList.addAll(xmlValidator.validateSchemaForFileInfo(fileInfo.getSbmFileInfoId(), fileProcDto.getSbmiFile()));

		// add file level errors to dto and generate SBMS
		if( ! fileProcDto.getErrorList().isEmpty()) { 
			fileProcDto.setSbmFileStatusType(REJECTED); 
			fileInfo.setRejectedInd(true);
			LOG.info("Schema errors exists; Rejecting file");
			LOG.info("Schema errors: {}", fileProcDto.getErrorList());
			return fileProcDto;
		}

		//No schema errors, unmarshall FileInformationType
		FileInformationType sbmiFileInfoType = xmlValidator.unmarshallSBMIFileInfo(fileProcDto.getSbmiFile());
		fileProcDto.setFileInfoType(sbmiFileInfoType);
		//Marshall FileInformationType to xml string to save to SBMFileInfo table
		fileProcDto.setFileInfoXML(xmlValidator.marshallFileInfo(sbmiFileInfoType));		

		updateAttributes(fileProcDto);

		//perform file meta data validations & perform file level validations
		fileValidator.validate(fileProcDto);

		// add file level errors to dto and generate SBMS
		if( ! fileProcDto.getErrorList().isEmpty()) { 
			fileProcDto.setSbmFileStatusType(REJECTED); 
			fileInfo.setRejectedInd(true);
			LOG.info("File level errors exists; Rejecting file");
			LOG.info("File level errors: {}", fileProcDto.getErrorList());
			return fileProcDto;
		}

		// all validations passed
		LOG.info("All validations passed.");

		//Set File status to IN_PROCESS, ON_HOLD, FREEZE or PENDING_FILES
		fileSatusHandler.determineAndSetFileStatus(fileProcDto);

		LOG.info("SummaryId:{}, status:{}, File rejectedInd: {}", fileProcDto.getSbmFileProcSumId(), fileProcDto.getSbmFileStatusType(), fileInfo.isRejectedInd());  
		return fileProcDto;
	}

	private SBMFileProcessingDTO getAFileToProcessFromEFT() throws IOException {

		File fileFromEFT = null;
		File sbmiFile = null;
		String fileName = "";
		String fileNamePrev = "";
		long lastModifiedTimeMillis = 0;
		int loopCnt = 1;

		// Try until no files in EFT.
		// Or, if the private directory is not cleared from a previous job failure
		// and the same file is re-run, limit the number of times a duplicate file is attempted to lock.
		while((fileFromEFT = getAFileFromEFT()) != null) {
			fileName = fileFromEFT.getName();
			lastModifiedTimeMillis = fileFromEFT.lastModified();
			sbmiFile = lockFile(fileFromEFT);
			if(sbmiFile != null) {
				break;
			}	
			LOG.info("Unable to lock the file (Other job might have got the lock); trying with next file");

			if (fileName.equals(fileNamePrev)) {
				loopCnt++;
			}

			if (loopCnt >= FILE_EXISTS_MAX_LOOP_CNT) {
				throw new EnvironmentException(EProdEnum.EPROD_02.getLogMsg() + ".  File " + fileName + 
						" is already locked.  Attempted to lock the same file " + FILE_EXISTS_MAX_LOOP_CNT + " times.");
			}
			fileNamePrev = fileName;
		}	

		if(sbmiFile == null) {
			//no files in EFT
			LOG.info("No files exists in EFT folder");
			return null;
		}

		SBMFileInfo fileInfo = createSBMFileInfo(fileName, lastModifiedTimeMillis);

		SBMFileProcessingDTO sbmFileProcDto = new SBMFileProcessingDTO();
		sbmFileProcDto.setSbmFileInfo(fileInfo);
		sbmFileProcDto.setSbmiFile(sbmiFile);

		return sbmFileProcDto;
	}

	private File getAFileFromEFT() {
		
		List<File> filesList = CommonUtil.getFilesFromDir(eftFolder, environmentCd);
		LOG.debug("'{}' Files in {}: {}", environmentCd, eftFolder, filesList);

		if(CollectionUtils.isEmpty(filesList)) {
			LOG.info(" NO '{}' files in EFT folder: {}", environmentCd, eftFolder.getName());
			return null;
		}		

		File fileFromEFT = filesList.get(0);
		LOG.info("Returning '{}' file from EFT folder: {}", environmentCd, fileFromEFT.getName());
		return fileFromEFT;
	}

	private SBMFileProcessingDTO getAFileToProcessFromPrivateFolder() {		

		File aFile = null;
		File sbmiFile = null;
		String filename = null;
		long lastModifiedTimeMillis = 0;

		//try until an unlocked file is locked successfully 
		//	or no unlocked files exists
		while((aFile = getAnUnlockedFile()) != null) {
			filename = aFile.getName();
			lastModifiedTimeMillis = aFile.lastModified();
			sbmiFile = lockFile(aFile);
			if(sbmiFile != null) {
				break;
			}
			LOG.info("Unable to lock the file(Other job might have got the lock); trying with next file");
		}		

		if(sbmiFile == null) {
			//no unlocked files exists
			LOG.info("No unlocked files exists in private folder");
			return null;
		}

		SBMFileInfo fileInfo = createSBMFileInfo(filename, lastModifiedTimeMillis);

		SBMFileProcessingDTO sbmFileProcDto = new SBMFileProcessingDTO();
		sbmFileProcDto.setSbmFileInfo(fileInfo);
		sbmFileProcDto.setSbmiFile(sbmiFile);

		return sbmFileProcDto;
	}

	private File getAnUnlockedFile() {

		File[] filesList = privateFolder.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isFile() && (!file.getName().endsWith(SBMConstants.FILESUFFIX_LOCK));
			}
		});

		LOG.info("Files in {}: {}", privateFolder, filesList);

		if(filesList == null || filesList.length == 0 || filesList[0] == null) {
			return null;
		}

		LOG.info("Returning {}", filesList[0].getName());
		return filesList[0];
	}


	private SBMFileInfo createSBMFileInfo(String filename, long lastModifiedTimeMillis) {

		SBMFileInfo fileInfo = new SBMFileInfo();	

		LOG.info("filename {}", filename);

		if(StringUtils.contains(filename, SBMConstants.ZIPD)) {
			//filename format: TradingPartnerID.AppId.FuncCode.Date.Time.EnvCode.Direction_ZIPDyyyyMMddTHHmmss
			String actualFilename = StringUtils.substringBefore(filename, SBMConstants.ZIPD);
			LOG.info("actualFilename: {}", actualFilename);
			fileInfo.setSbmFileNm(actualFilename);
			String dateTimeString =  StringUtils.difference(actualFilename, filename);
			LOG.info("dateTimeString: {}", dateTimeString);
			fileInfo.setFileLastModifiedDateTime(LocalDateTime.parse(dateTimeString, zipFormatter));
			fileInfo.setFunctionCd(SbmHelper.getFunctionCodeFromPreEFTFormat(actualFilename)); 
			fileInfo.setTradingPartnerId(SbmHelper.getTradingPartnerIdFromPreEFTFormat(actualFilename)); 				
		}
		else if(StringUtils.contains(filename, SBMConstants.GZIPD)) {
			//filename format: FuncCode.TradingPartnerID.Date.Time.EnvCode.Direction_GZIPDyyyyMMddTHHmmss
			String actualFilename = StringUtils.substringBefore(filename, SBMConstants.GZIPD);
			LOG.info("actualFilename: {}", actualFilename);
			fileInfo.setSbmFileNm(actualFilename);			
			String dateTimeString =  StringUtils.difference(actualFilename, filename);
			LOG.info("dateTimeString: {}", dateTimeString);
			fileInfo.setFileLastModifiedDateTime(LocalDateTime.parse(dateTimeString, gzipFormatter));
			fileInfo.setFunctionCd(SbmHelper.getFunctionCodeFromFile(actualFilename)); 
			fileInfo.setTradingPartnerId(SbmHelper.getTradingPartnerId(actualFilename)); 
		}
		else {
			//filename format: FuncCode.TradingPartnerID.Date.Time.EnvCode.Direction
			fileInfo.setSbmFileNm(filename);
			if(lastModifiedTimeMillis > 0) {
				fileInfo.setFileLastModifiedDateTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(lastModifiedTimeMillis), ZoneId.systemDefault()));
			}
			fileInfo.setFunctionCd(SbmHelper.getFunctionCodeFromFile(filename)); 
			fileInfo.setTradingPartnerId(SbmHelper.getTradingPartnerId(filename)); 
		}

		LOG.info("SBMFileInfo: {}", fileInfo);
		return fileInfo;
	}

	private File lockFile(File source) {
		File newFile = new File(privateFolder, source.getName() + SBMConstants.FILESUFFIX_LOCK);
		try {
			LOG.info("Moving file from {} to {}", source, newFile);
			FileUtils.moveFile(source, newFile);
		} catch (IOException e) {
			LOG.info("Error occured :{}", e.getMessage());
			return null;
		}

		return newFile;
	}

	private void updateAttributes(SBMFileProcessingDTO sbmFileProcDto) {

		FileInformationType fileInfoType = sbmFileProcDto.getFileInfoType();
		SBMFileInfo sbmFileInfo = sbmFileProcDto.getSbmFileInfo();

		//SBMFileInfo
		sbmFileInfo.setSbmFileCreateDateTime(DateTimeUtil.getLocalDateTimeFromXmlGC(fileInfoType.getFileCreateDateTime()));

		if(fileInfoType.getIssuerFileInformation() != null && fileInfoType.getIssuerFileInformation().getIssuerFileSet() != null) {	
			//set issuer file set related attributes			
			sbmFileInfo.setSbmFileNum(fileInfoType.getIssuerFileInformation().getIssuerFileSet().getFileNumber());	
			//check if IssuerFileSetId already exists in DB
			LOG.info("Checking if IssuerFileSet: {}, TenantId: {} already exists in DB?", fileInfoType.getIssuerFileInformation().getIssuerFileSet().getIssuerFileSetId(), fileInfoType.getTenantId());
			List<SBMSummaryAndFileInfoDTO> summaryDtoList = fileCompositeDao.getSBMFileProcessingSummary(fileInfoType.getIssuerFileInformation().getIssuerId(), fileInfoType.getIssuerFileInformation().getIssuerFileSet().getIssuerFileSetId(), fileInfoType.getTenantId()); 	
			for(SBMSummaryAndFileInfoDTO summaryFromDB: summaryDtoList) {
				if( ! (REJECTED.equals(summaryFromDB.getSbmFileStatusType()) || DISAPPROVED.equals(summaryFromDB.getSbmFileStatusType()))) {
					sbmFileProcDto.setFileProcSummaryFromDB(summaryFromDB);
					break;
				}
			}

			LOG.info("Found in DB: {}", sbmFileProcDto.getFileProcSummaryFromDB());

		}

		//new entry of SBMFileProccessingSummary will be created in DB so populate all attributes
		sbmFileProcDto.setCoverageYear(fileInfoType.getCoverageYear());
		sbmFileProcDto.setTenantId(fileInfoType.getTenantId());		
		sbmFileProcDto.setCmsApprovedInd(N);
		sbmFileProcDto.setCmsApprovalRequiredInd("Y");
		StateProrationConfiguration stateConfig = SBMCache.getStateProrationConfiguration(fileInfoType.getCoverageYear(), getStateCd(fileInfoType.getTenantId()));
		LOG.info("StateProrationConfiguration found: {}", stateConfig);
		if(stateConfig != null) { 
			LOG.info("Setting attributes from StateProrationConfiguration");
			if( ! stateConfig.isCmsApprovalRequiredInd()) {
				sbmFileProcDto.setCmsApprovalRequiredInd("N");
			}

			sbmFileProcDto.setErrorThresholdPercent(stateConfig.getErrorThresholdPercent());
		}

		if(fileInfoType.getIssuerFileInformation() != null){
			sbmFileProcDto.setIssuerId(fileInfoType.getIssuerFileInformation().getIssuerId());
		}

		if(fileInfoType.getIssuerFileInformation() != null && fileInfoType.getIssuerFileInformation().getIssuerFileSet() != null) {
			//IssuerFileSetId doesn't exists in database so new entry of SBMFileProccessingSummary will be created
			sbmFileProcDto.setIssuerFileSetId(fileInfoType.getIssuerFileInformation().getIssuerFileSet().getIssuerFileSetId());
			sbmFileProcDto.setTotalIssuerFileCount(fileInfoType.getIssuerFileInformation().getIssuerFileSet().getTotalIssuerFiles());

		}

	}

	/*
	 * Process Compressed files - Winzip and Gzip are the only supported compression formats.
	 */
	private void processCompressedFiles(SBMFileProcessingDTO fileProcDto) throws IOException {

		File fileToValidate = fileProcDto.getSbmiFile();
		boolean isZip = false;

		if(!SbmHelper.isValidZip(fileToValidate)) {
			LOG.info("Not valid zip file : "+ fileToValidate.getName());

			isZip = false;

		} else {

			isZip = true;
			//move to zipped folder
			processZipFiles(fileToValidate, fileProcDto);
		}

		boolean isGzip = false;

		if(!isZip) {
			isGzip = SbmHelper.isGZipped(fileToValidate); 
		}

		if (isGzip) {
			processGzip(fileToValidate, fileProcDto);
		}
	}

	/*
	 * Process Zip files
	 */
	private void processZipFiles(File file, SBMFileProcessingDTO fileProcDto) throws FileNotFoundException, IOException {

		byte[] buffer = new byte[1024];

		LOG.info("file : "+ file.getAbsoluteFile());

		final ZipFile zipFile = new ZipFile(file);

		File zipDirectory = new File(zipFilesFolder.getAbsolutePath() + File.separator + file.getName().replaceAll(LOCK_EXTENSION, ""));
		zipDirectory.mkdir();

		List<SBMErrorDTO> zipErrorList = new ArrayList<SBMErrorDTO>();
		String zipFileSrcId = SbmHelper.getTradingPartnerId(file.getName());

		String zipFileTs = zipFormatter.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()), ZoneId.systemDefault()));
		LOG.info("Zip file timestamp: " + zipFileTs);

		try {
			final Enumeration<? extends ZipEntry> entries = zipFile.entries();

			while (entries.hasMoreElements()) {

				final ZipEntry ze = entries.nextElement();

				String zipEntryName = ze.getName();

				if(!isZipEntryNameValid(zipEntryName)) {

					LOG.info("ER-063: Invalid file name within Zip file");

					SBMErrorDTO error = new SBMErrorDTO();
					error.setSbmErrorWarningTypeCd(SBMErrorWarningCode.ER_063.getCode());
					error.setElementInErrorNm("FileID");
					error.setAdditionalErrorInfoText(zipEntryName);

					zipErrorList.add(error);

					SBMFileInfo fileInfo = new SBMFileInfo();		
					fileInfo.setSbmFileNm(zipEntryName);
					fileProcDto.setSbmFileInfo(fileInfo);
					fileProcDto.setSbmFileStatusType(REJECTED);
					fileInfo.setRejectedInd(true);

					break;
				}

				String zipEntrySrcId = SbmHelper.getZipFileTradingPartnerId(zipEntryName);

				if(!zipEntrySrcId.equalsIgnoreCase(zipFileSrcId)) {

					LOG.info("ER-011: Mismatched SourceId in ZIP file");

					SBMErrorDTO error = new SBMErrorDTO();
					error.setSbmErrorWarningTypeCd(SBMErrorWarningCode.ER_011.getCode());
					zipErrorList.add(error);

					SBMFileInfo fileInfo = new SBMFileInfo();		
					fileInfo.setSbmFileNm(zipEntryName);
					fileProcDto.setSbmFileInfo(fileInfo);
					fileProcDto.setSbmFileStatusType(REJECTED);					
					fileInfo.setRejectedInd(true);

					break;
				}

				if(zipErrorList.isEmpty()) {
					File newFile = new File(zipDirectory + File.separator + zipEntryName.concat(zipFileTs));

					LOG.info("file unzip : "+ newFile.getAbsoluteFile());

					InputStream zis = zipFile.getInputStream(ze);

					try {
						FileOutputStream fos = null;
						try {
							fos = new FileOutputStream(newFile);             

							int len;
							while ((len = zis.read(buffer)) > 0) {
								fos.write(buffer, 0, len);
							}
							fos.close(); 
						} finally {
							if(fos != null) { 
								fos.close(); 
							}
						}
					} finally {
						if(zis != null) { 
							zis.close();
						}
					}
				}
			}
		} finally {
			zipFile.close();
		}

		if(!zipErrorList.isEmpty()) {
			fileProcDto.getErrorList().addAll(zipErrorList);

		} else {

			LOG.info(file.getName() + " Unziped to: " + zipDirectory);

			List<File> unzippedFiles = CommonUtil.getFilesFromDir(zipDirectory);

			if(CollectionUtils.isNotEmpty(unzippedFiles)) {

				File firstFile = unzippedFiles.get(0);
				String fileNm = StringUtils.substringBefore(firstFile.getName(), SBMConstants.ZIPD);

				File newFile = lockFile(firstFile);

				SBMFileInfo fileInfo = new SBMFileInfo();		
				fileInfo.setSbmFileNm(fileNm);
				fileInfo.setFunctionCd(SbmHelper.getFunctionCodeFromPreEFTFormat(fileNm)); 
				fileInfo.setTradingPartnerId(SbmHelper.getTradingPartnerIdFromPreEFTFormat(fileNm)); 
				fileInfo.setFileLastModifiedDateTime(LocalDateTime.parse(zipFileTs, zipFormatter)); 
				fileProcDto.setSbmFileInfo(fileInfo);
				fileProcDto.setSbmiFile(newFile);

				for (File unzipFile:unzippedFiles) {

					if(unzipFile.exists()) {
						LOG.info("Moving file from {} to {}", zipDirectory, privateFolder);
						FileUtils.moveFileToDirectory(unzipFile, privateFolder, false);
					}
				}
			}
		}
		File moveZippedFile = new File(processedFolder, file.getName().replaceAll(LOCK_EXTENSION, "")
				.concat(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))));

		LOG.info("Moving zipped file from {} to {}", privateFolder, moveZippedFile);
		FileUtils.moveFile(file, moveZippedFile);

		FileUtils.deleteDirectory(zipDirectory);
	}

	/*
	 * Validate zipped file name format
	 */
	private boolean isZipEntryNameValid(String zipEntryName) {

		if (!zipEntryName.matches(ZIPPED_FILE_NAME_FORMAT)){
			return false;
		}
		return true;
	}

	/*
	 * Process GZip files
	 */
	private void processGzip(File file, SBMFileProcessingDTO fileProcDto) throws IOException {

		LOG.info("process Gzip..");

		byte[] buffer = new byte[1024];

		String gzipFileTs = gzipFormatter.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()), ZoneId.systemDefault()));
		LOG.info("GZip file timestamp: " + gzipFileTs);

		FileInputStream fis = new FileInputStream(file);

		GZIPInputStream gzs = null;
		try {
			gzs = new GZIPInputStream(fis);

		} catch (IOException e) {
			LOG.error(e.getMessage());
		}

		String newFileName = file.getName().replaceAll(LOCK_EXTENSION, "");
		File gzipDirectory = new File(zipFilesFolder.getAbsolutePath() + File.separator + newFileName);
		gzipDirectory.mkdir();

		if(gzs != null) {

			try {
				FileOutputStream fileOutputStream = new FileOutputStream(gzipDirectory + File.separator + newFileName.replaceAll("\\.gz$", "").concat(gzipFileTs));

				int bytesRead;

				try {
					while ((bytesRead = gzs.read(buffer)) > 0) {
						fileOutputStream.write(buffer, 0, bytesRead);
					}

				} catch (IOException e) {
					LOG.error(e.getMessage());

				} finally {
					fileOutputStream.close();
				}
			} finally {
				gzs.close();
			}
		}

		LOG.info(file.getName() + " GUnzip to: " + gzipDirectory);

		File moveGZippedFile = new File(processedFolder, file.getName().replaceAll(LOCK_EXTENSION, "")
				.concat(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))));

		LOG.info("Moving zipped file from {} to {}", privateFolder, moveGZippedFile);
		FileUtils.moveFile(file, moveGZippedFile);

		List<File> gunzippedFiles = CommonUtil.getFilesFromDir(gzipDirectory);

		if(CollectionUtils.isNotEmpty(gunzippedFiles)) {

			File firstFile = gunzippedFiles.get(0);
			String fileNm = StringUtils.substringBefore(firstFile.getName(), SBMConstants.GZIPD);

			File newFile = lockFile(firstFile);

			SBMFileInfo fileInfo = new SBMFileInfo();		
			fileInfo.setSbmFileNm(fileNm);
			fileInfo.setFunctionCd(SbmHelper.getFunctionCodeFromFile(fileNm)); 
			fileInfo.setTradingPartnerId(SbmHelper.getTradingPartnerId(fileNm)); 
			fileInfo.setFileLastModifiedDateTime(LocalDateTime.parse(gzipFileTs, gzipFormatter)); 
			fileProcDto.setSbmFileInfo(fileInfo);
			fileProcDto.setSbmiFile(newFile);

			for (File gunzipFile:gunzippedFiles) {

				if(gunzipFile.exists()) {
					LOG.info("Moving file from {} to {}", gzipDirectory, privateFolder);
					FileUtils.moveFileToDirectory(gunzipFile, privateFolder, false);
				}
			}
		}

		FileUtils.deleteDirectory(gzipDirectory);
	}

	/**
	 * @param xmlValidator the xmlValidator to set
	 */
	public void setXmlValidator(SbmXMLValidator xmlValidator) {
		this.xmlValidator = xmlValidator;
	}

	/**
	 * @param eftFolder the eftFolder to set
	 */
	public void setEftFolder(File eftFolder) {
		this.eftFolder = eftFolder;
	}

	/**
	 * @param privateFolder the privateFolder to set
	 */
	public void setPrivateFolder(File privateFolder) {
		this.privateFolder = privateFolder;
	}

	/**
	 * @param zipFilesFolder the zipFilesFolder to set
	 */
	public void setZipFilesFolder(File zipFilesFolder) {
		this.zipFilesFolder = zipFilesFolder;
	}

	/**
	 * @param fileCompositeDao the fileCompositeDao to set
	 */
	public void setFileCompositeDao(SBMFileCompositeDAO fileCompositeDao) {
		this.fileCompositeDao = fileCompositeDao;
	}

	/**
	 * @param fileValidator the fileValidator to set
	 */
	public void setFileValidator(SbmFileValidator fileValidator) {
		this.fileValidator = fileValidator;
	}

	/**
	 * @param fileSatusHandler the fileSatusHandler to set
	 */
	public void setFileSatusHandler(SBMFileStatusHandler fileSatusHandler) {
		this.fileSatusHandler = fileSatusHandler;
	}

	/**
	 * @param processedFolder the processedFolder to set
	 */
	public void setProcessedFolder(File processedFolder) {
		this.processedFolder = processedFolder;
	}

	/**
	 * @param environmentCd the environmentCd to set
	 */
	public void setEnvironmentCd(String environmentCd) {
		this.environmentCd = environmentCd;
	}


}