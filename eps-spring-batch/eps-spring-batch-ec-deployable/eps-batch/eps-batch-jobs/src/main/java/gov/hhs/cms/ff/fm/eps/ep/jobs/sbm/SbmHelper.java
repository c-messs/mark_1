package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm;

import static gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus.DISAPPROVED;
import static gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus.REJECTED;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.hhs.cms.ff.fm.eps.ep.enums.SBMErrorWarningCode;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMCache;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMErrorDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileInfo;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMSummaryAndFileInfoDTO;
/**
 * SBM Helper class
 * @author rajesh.talanki
 *
 */
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMUpdateStatusErrorDTO;

/**
 * @author rajesh.talanki
 *
 */
public class SbmHelper {
	
	private static final Logger LOG = LoggerFactory.getLogger(SbmHelper.class);
	
	/**
	 * Create SBMFileError
	 * 
	 * @param elementInErrorNm
	 * @param sbmErrorWarningTypeCd
	 * @return errorLog
	 */
	public static SBMErrorDTO createErrorLog(String elementInErrorNm, String sbmErrorWarningTypeCd) {
		SBMErrorDTO errorLog = new SBMErrorDTO();
		errorLog.setSbmErrorWarningTypeCd(sbmErrorWarningTypeCd);
		errorLog.setElementInErrorNm(elementInErrorNm);
		
		return errorLog;
	}
	
	/**
	 * Create SBMFileError
	 * 
	 * @param elementInErrorNm
	 * @param sbmErrorWarningTypeCd
	 * @param additionalErrorInfos
	 * @return errorLog
	 */
	public static SBMErrorDTO createErrorLog(String elementInErrorNm, String sbmErrorWarningTypeCd, String... additionalErrorInfos) {
		
		SBMErrorDTO errorLog = createErrorLog(elementInErrorNm, sbmErrorWarningTypeCd);	
		
		if(additionalErrorInfos != null) {
			CollectionUtils.addAll(errorLog.getAdditionalErrorInfoList(), additionalErrorInfos);
		}
		
		return errorLog;
	}
	
	/**
	 * Create SBMFileError
	 * 
	 * @param elementInErrorNm
	 * @param sbmErrorWarningTypeCd
	 * @param exchangeAssignedMemberId
	 * @param additionalErrorInfos
	 * @return errorLog
	 */
	public static SBMErrorDTO createErrorWithMemId(String elementInErrorNm, String sbmErrorWarningTypeCd, String exchangeAssignedMemberId, String... additionalErrorInfos) {
		
		SBMErrorDTO errorLog = createErrorLog(elementInErrorNm, sbmErrorWarningTypeCd, additionalErrorInfos);	
		errorLog.setExchangeAssignedMemberId(exchangeAssignedMemberId);
				
		return errorLog;
	}
	
	/**
	 * Extracts function code from file name
	 * @param filename
	 * @return
	 */
	public static String getFunctionCodeFromFile(String filename) {
		//extract function code from filename
		//File Name = FuncCode.TradingPartnerID.Date.Time.EnvCode.Direction
		String[] tokens = StringUtils.split(filename, ".");
		
		if(tokens != null && tokens.length >= 1) {
			return tokens[0];
		}
		
		return "";
	}
	
	/**
	 * Extracts function code from file name which is in pre-EFT format
	 * @param filename
	 * @return
	 */
	public static String getFunctionCodeFromPreEFTFormat(String filename) {
		//extract function code from filename
		//File Name = //filename format: TradingPartnerID.AppId.FuncCode.Date.Time.EnvCode.Direction
		String[] tokens = StringUtils.split(filename, ".");
		
		if(tokens != null && tokens.length >= 3) {
			return tokens[2];
		}
		
		return "";
	}
	
	/**
	 * Extract Trading Partner Id from filename
	 * @param filename
	 * @return
	 */
	public static String getTradingPartnerId(String filename) {
		//extract TradingPartnerId from filename
		//File Name = FuncCode.TradingPartnerID.Date.Time.EnvCode.Direction
		String[] tokens = StringUtils.split(filename, ".");

		if(tokens != null && tokens.length > 1) {
			return tokens[1];
		}

		return "";
	}
	
	/**
	 * Extract Trading Partner Id from filename which is in pre-EFT format
	 * @param filename
	 * @return
	 */
	public static String getTradingPartnerIdFromPreEFTFormat(String filename) {
		//extract TradingPartnerId from filename
		//filename format: TradingPartnerID.AppId.FuncCode.Date.Time.EnvCode.Direction
		String[] tokens = StringUtils.split(filename, ".");

		if(tokens != null && tokens.length > 1) {
			return tokens[0];
		}

		return "";
	}
	
	/**
	 * Extract Trading Partner Id from Zip filename
	 * @param filename
	 * @return
	 */
	public static String getZipFileTradingPartnerId(String filename) {
		//File Name = TradingPartnerID.AppId.FuncCode.Date.Time.EnvCode.Direction
		String[] tokens = StringUtils.split(filename, ".");

		if(tokens != null && tokens.length >= 1) {
			return tokens[0];
		}

		return "";
	}
	
	/**
	 * Extract Trading Partner Id from filename
	 * @param sbmFileInfoList
	 * @return
	 */
	public static String getTradingPartnerId(List<SBMFileInfo> sbmFileInfoList) {
		for(SBMFileInfo fileInfo: sbmFileInfoList) {
			return fileInfo.getTradingPartnerId();
		}
		return null;
	}
	
	/**
	 * Determine Rejected and Not Disapproved
	 * @param summaryDtoList
	 * @return boolean
	 */
	public static boolean isNotRejectedNotDisapproved(List<SBMSummaryAndFileInfoDTO> summaryDtoList) {
		
		for(SBMSummaryAndFileInfoDTO summaryDto: summaryDtoList) {
			if( ! (REJECTED.equals(summaryDto.getSbmFileStatusType()) || DISAPPROVED.equals(summaryDto.getSbmFileStatusType())) ) {
				for( SBMFileInfo eachfileInfo: summaryDto.getSbmFileInfoList()) {
					if( ! eachfileInfo.isRejectedInd()) {						
						return true;
					}
				}
			}			
		}
		return false;
	}
	
	/**
	 * Parse the CSV File
	 * 
	 * @param inputFile
	 * @return rows
	 * @throws IOException
	 */
	public static List<CSVRecord> readCSVFile(File inputFile) throws IOException {

		LOG.info("Reading file: {}", inputFile);
		CSVParser parser = CSVParser.parse(inputFile, Charset.forName("UTF-8"), CSVFormat.RFC4180);
		List<CSVRecord> rows = parser.getRecords();
		parser.close();
		LOG.info("File content: {}", rows);
		return rows;
	}
	
	/**
	 * Create Error DTO
	 * 
	 * @param linenumber
	 * @param errorCode
	 * @param errorDescription
	 * @param fileId
	 * @param fileSetId
	 * @return error
	 */
	public static SBMUpdateStatusErrorDTO createError(String linenumber, String errorCode, String errorDescription, String fileId, String fileSetId) {
		
		SBMUpdateStatusErrorDTO error = new  SBMUpdateStatusErrorDTO();
		error.setLineNumber(linenumber);
		error.setErrorCode(errorCode);
		error.setErrorDescription(errorDescription);
		error.setFileId(fileId);
		error.setFileSetId(fileSetId);
		
		return error;
	}
	
	/**
	 * Create UpdateStatusError DTO
	 * 
	 * @param linenumber
	 * @param error
	 * @return SBMUpdateStatusErrorDTO
	 */
	public static SBMUpdateStatusErrorDTO createError(String linenumber, SBMErrorWarningCode error) {		
		
		return createError(linenumber, error.getCode(), SBMCache.getErrorDescription(error.getCode()), null, null);
	}
	
	/**
	 * Determine FileStatusMatched
	 * 
	 * @param originalStatus
	 * @param fileStatus
	 * @return boolean
	 */
	public static boolean isFileStatusMatched(SBMFileStatus originalStatus, SBMFileStatus...fileStatus) {
		
		if(originalStatus == null) {
			return false;
		}
		
		for(SBMFileStatus fs: fileStatus) {
			if(originalStatus.equals(fs)) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Determine if file is a valid zip file
	 * 
	 * @param file
	 * @return boolean
	 * @throws IOException
	 */
	public static boolean isValidZip(final File file) throws IOException {
		ZipFile zipfile = null;
		
		try {
			zipfile = new ZipFile(file);
			return true;
		} catch (ZipException e) {
			LOG.info("Exception occurred:{}", e.getMessage());
			return false;
		} finally {
			try {
				if (zipfile != null) {
					zipfile.close();
					zipfile = null;
				}
			} catch (IOException e) {
				LOG.info("Exception occurred:{}", e.getMessage());
			}
		}
	}
	
	/**
	 * Determine if file is a valid gzip file
	 * 
	 * @param file
	 * @return boolean
	 */
	public static boolean isGZipped(File file) {
		int magic = 0;
		try {
			RandomAccessFile raf = new RandomAccessFile(file, "r");
			magic = raf.read() & 0xff | ((raf.read() << 8) & 0xff00);
			raf.close();
		} catch (IOException e) {
			LOG.info("Exception occurred:{}", e.getMessage());
		}
		return magic == GZIPInputStream.GZIP_MAGIC;
	}
	 
}
