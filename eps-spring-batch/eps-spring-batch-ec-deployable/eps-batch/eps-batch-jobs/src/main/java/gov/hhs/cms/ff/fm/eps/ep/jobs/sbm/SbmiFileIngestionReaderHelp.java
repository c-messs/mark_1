package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm;


import static gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus.REJECTED;
import static gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus.DISAPPROVED;
import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.N;
import java.io.File;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
import gov.hhs.cms.ff.fm.eps.ep.StateProrationConfiguration;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMErrorWarningCode;
import gov.hhs.cms.ff.fm.eps.ep.jobs.CommonUtil;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMCache;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMErrorDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileInfo;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileProcessingDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMSummaryAndFileInfoDTO;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SBMFileCompositeDAO;
import static gov.hhs.cms.ff.fm.eps.ep.sbm.SbmDataUtil.getStateCd;
/**
 * @author mark.finkelshteyn
 *  Process Zipped and Unzipped Files
 */
public class SbmiFileIngestionReaderHelp {
	/*
	 * Process Zip files
	 */
	

	private static final Logger LOG = LoggerFactory.getLogger(SbmiFileIngestionReader.class);
	private static final String ZIPPED_FILE_NAME_FORMAT = "\\w{1,10}\\.\\w{1,10}\\.\\w{1,10}\\.D(\\d{6})\\.T(\\d{9})(\\.\\w?+)?(\\.\\w*+)?";
	private static final String LOCK_EXTENSION = "\\.lock$";
	private static final String MOVE_TO_FROM = "Moving file from {} to {}";
	private static final String MOVE_TO_FROM_ZIPPED = "Moving zipped file from {} to {}";
	
	private DateTimeFormatter zipFormatter = DateTimeFormatter.ofPattern(SBMConstants.FILENAME_ZIP_PATTERN);
	private SBMFileCompositeDAO fileCompositeDao;
   
	/**
	 *@param file
	 *@param fileProcDto
	 *@param zipFilesFolder
	 *@param privateFolder
	 *@param processedFolder
	 *@return fileProcDto
	 *@throws FileNotFoundException
	 *@throws IOException
	 */
	public SBMFileProcessingDTO processZipFiles(File file, SBMFileProcessingDTO fileProcDto, File zipFilesFolder, File privateFolder, File processedFolder) throws FileNotFoundException, IOException {

		byte[] buffer = new byte[1024];

		LOG.info("file : "+ file.getAbsoluteFile());

		final ZipFile zipFile = new ZipFile(file);

		File zipDirectory = new File(zipFilesFolder.getAbsolutePath() + File.separator + file.getName().replaceAll(LOCK_EXTENSION, ""));
		zipDirectory.mkdir();

		List<SBMErrorDTO> zipErrorList = new ArrayList<>();
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
			manipulateDirectory(unzippedFiles,fileProcDto,zipDirectory,zipFileTs,processedFolder,privateFolder);
			
		}
		File moveZippedFile = new File(processedFolder, file.getName().replaceAll(LOCK_EXTENSION, "")
				.concat(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))));

		LOG.info(MOVE_TO_FROM_ZIPPED, privateFolder, moveZippedFile);
		FileUtils.moveFile(file, moveZippedFile);

		FileUtils.deleteDirectory(zipDirectory);
		
		return fileProcDto;
	}

	private void manipulateDirectory(List<File> unzippedFiles, 
			SBMFileProcessingDTO fileProcDto, 
			File zipDirectory, String zipFileTs, File processedFolder, File privateFolder) throws IOException{
		if(CollectionUtils.isNotEmpty(unzippedFiles)) {

			File firstFile = unzippedFiles.get(0);
			String fileNm = StringUtils.substringBefore(firstFile.getName(), SBMConstants.ZIPD);

			File newFile = lockFile(firstFile,privateFolder);
			
			SBMFileInfo fileInfo = new SBMFileInfo();		
			fileInfo.setSbmFileNm(fileNm);
			fileInfo.setFunctionCd(SbmHelper.getFunctionCodeFromPreEFTFormat(fileNm)); 
			fileInfo.setTradingPartnerId(SbmHelper.getTradingPartnerIdFromPreEFTFormat(fileNm)); 
			fileInfo.setFileLastModifiedDateTime(LocalDateTime.parse(zipFileTs, zipFormatter)); 
			fileProcDto.setSbmFileInfo(fileInfo);
			fileProcDto.setSbmiFile(newFile);

			for (File unzipFile:unzippedFiles) {

				if(unzipFile.exists()) {
					LOG.info(MOVE_TO_FROM, zipDirectory, privateFolder);
					FileUtils.moveFileToDirectory(unzipFile, privateFolder, false);
				}
			}
		}
		
	}
	/**
	 *@param sbmFileProcDto
	  *@return void
	 */
	public void updateAttributes(SBMFileProcessingDTO sbmFileProcDto) {

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
		updateSbmFileProcDto(stateConfig,sbmFileProcDto,fileInfoType);
	}

	private SBMFileProcessingDTO updateSbmFileProcDto(StateProrationConfiguration stateConfig, SBMFileProcessingDTO sbmFileProcDto, FileInformationType fileInfoType) {
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
		return sbmFileProcDto;
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
	
	private File lockFile(File source, File privateFolder) {
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

}
