/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm;

import static gov.hhs.cms.ff.fm.eps.ep.enums.SBMErrorWarningCode.ER_012;
import static gov.hhs.cms.ff.fm.eps.ep.enums.SBMErrorWarningCode.ER_013;
import static gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus.REJECTED;
import static gov.hhs.cms.ff.fm.eps.ep.jobs.sbm.SbmHelper.createErrorLog;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import gov.cms.dsh.sbmi.FileInformationType;
import gov.hhs.cms.ff.fm.eps.ep.jobs.CommonUtil;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMErrorDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileInfo;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileProcessingDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMSummaryAndFileInfoDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SBMFileCompositeDAO;

/**
 * @author rajesh.talanki
 *
 */
public class SbmiFileIngestionReader  {

	private static final Logger LOG = LoggerFactory.getLogger(SbmiFileIngestionReader.class);
	private static final String ZIPPED_FILE_NAME_FORMAT = "\\w{1,10}\\.\\w{1,10}\\.\\w{1,10}\\.D(\\d{6})\\.T(\\d{9})(\\.\\w?+)?(\\.\\w*+)?";
	private static final String LOCK_EXTENSION = "\\.lock$";
	private static final String MOVE_TO_FROM = "Moving file from {} to {}";
	private static final String MOVE_TO_FROM_ZIPPED = "Moving zipped file from {} to {}";
	
	private File eftFolder;
	private File privateFolder;
	private File processedFolder; 
	private File zipFilesFolder;
	private SbmXMLValidator xmlValidator;
	private SBMFileCompositeDAO fileCompositeDao;
	private SbmFileValidator fileValidator;
	private SBMFileStatusHandler fileSatusHandler;
    private SBMXMLValidatorHandle sbmxmlValidatorHandle;
	private DateTimeFormatter zipFormatter = DateTimeFormatter.ofPattern(SBMConstants.FILENAME_ZIP_PATTERN);
	private DateTimeFormatter gzipFormatter = DateTimeFormatter.ofPattern(SBMConstants.FILENAME_GZIP_PATTERN);
	private SbmiFileIngestionReaderHelp fileIngestionReaderHelp;


	/**
	 * @param jobId
	 * @return fileProcDto
	 * @throws Exception
	 * @throws UnexpectedInputException
	 * @throws ParseException
	 * @throws NonTransientResourceException
	 * @throws JAXBException 
	 * @throws XMLStreamException 
	 * @throws ParserConfigurationException 
	 */
	public SBMFileProcessingDTO read(Long jobId) throws IOException, UnexpectedInputException, ParseException, NonTransientResourceException, ParserConfigurationException, XMLStreamException, JAXBException {

		//Get a file from EFT
		SBMFileProcessingDTO fileProcDto = getAFileToProcessFromEFT();

		if(fileProcDto == null) {
			LOG.info("No files in EFT folder; checking private folder");
			getAFileToProcessFromPrivateFolder();
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
        return updateFileProcDto(fileProcDto,errorList,fileInfo);
		
	}

	private SBMFileProcessingDTO updateFileProcDto(SBMFileProcessingDTO fileProcDto, List<SBMErrorDTO> errorList,
			SBMFileInfo fileInfo) throws  IOException,ParserConfigurationException,XMLStreamException,JAXBException{
		if(sbmxmlValidatorHandle == null){
			sbmxmlValidatorHandle = new SBMXMLValidatorHandle();
		}
		if(fileIngestionReaderHelp==null){
			fileIngestionReaderHelp = new SbmiFileIngestionReaderHelp(); 
		}
		if( ! sbmxmlValidatorHandle.isValidXML(fileProcDto.getSbmiFile())) {			
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
		FileInformationType sbmiFileInfoType = sbmxmlValidatorHandle.unmarshallSBMIFileInfo(fileProcDto.getSbmiFile());
		fileProcDto.setFileInfoType(sbmiFileInfoType);
		//Marshall FileInformationType to xml string to save to SBMFileInfo table
		fileProcDto.setFileInfoXML(sbmxmlValidatorHandle.marshallFileInfo(sbmiFileInfoType));		

		fileIngestionReaderHelp.updateAttributes(fileProcDto);

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
		String filename = "";
		long lastModifiedTimeMillis = 0;

		//try until no files in EFT
		while((fileFromEFT = getAFileFromEFT()) != null) {
			filename = fileFromEFT.getName();
			lastModifiedTimeMillis = fileFromEFT.lastModified();
			sbmiFile = lockFile(fileFromEFT);
			if(sbmiFile != null) {
				break;
			}	
			LOG.info("Unable to lock the file(Other job might have got the lock); trying with next file");
		}		

		if(sbmiFile == null) {
			//no files in EFT
			LOG.info("No files exists in EFT folder");
			return null;
		}

		SBMFileInfo fileInfo = createSBMFileInfo(filename, lastModifiedTimeMillis);

		SBMFileProcessingDTO sbmFileProcDto = new SBMFileProcessingDTO();
		sbmFileProcDto.setSbmFileInfo(fileInfo);
		sbmFileProcDto.setSbmiFile(sbmiFile);

		return sbmFileProcDto;
	}

	private File getAFileFromEFT() {
		List<File> filesList = CommonUtil.getFilesFromDir(eftFolder);
		LOG.info("Files in {}: {}", eftFolder, filesList);

		if(CollectionUtils.isEmpty(filesList)) {
			return null;
		}		

		File fileFromEFT = filesList.get(0);
		LOG.info("Returning {}", fileFromEFT.getName());
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
			LOG.info(MOVE_TO_FROM, source, newFile);
			FileUtils.moveFile(source, newFile);
		} catch (IOException e) {
			LOG.info("Error occured :{}", e.getMessage());
			return null;
		}

		return newFile;
	}


	/*
	 * Process Compressed files - Winzip and Gzip are the only supported compression formats.
	 */
	private void processCompressedFiles(SBMFileProcessingDTO fileProcDto) throws IOException {
        if(fileIngestionReaderHelp ==null){
        	fileIngestionReaderHelp = new SbmiFileIngestionReaderHelp();
        }
		File fileToValidate = fileProcDto.getSbmiFile();
		boolean isZip = false;

		if(!SbmHelper.isValidZip(fileToValidate)) {
			LOG.info("Not valid zip file : "+ fileToValidate.getName());

			isZip = false;

		} else {
			isZip = true;
			//move to zipped folder
			fileIngestionReaderHelp.processZipFiles(fileToValidate, fileProcDto,zipFilesFolder,privateFolder,processedFolder);
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

		LOG.info(MOVE_TO_FROM_ZIPPED, privateFolder, moveGZippedFile);
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
					LOG.info(MOVE_TO_FROM, gzipDirectory, privateFolder);
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
	 * @param sbmxmlValidatorHandle
	 */
	public void setSbmxmlValidatorHandle(SBMXMLValidatorHandle sbmxmlValidatorHandle) {
		this.sbmxmlValidatorHandle = sbmxmlValidatorHandle;
	}

}