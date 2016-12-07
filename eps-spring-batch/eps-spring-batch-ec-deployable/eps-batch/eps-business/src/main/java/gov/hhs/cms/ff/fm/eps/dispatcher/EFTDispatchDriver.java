package gov.hhs.cms.ff.fm.eps.dispatcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;

/**
 * This Class is meant to receive the required params for inserting
 * physicalDocuments into EPS and generate outputfiles based on the combinations
 * retrieved from DispatchedRoutingMap table in EPS.
 *
 * @author pankaj.samayam
 */
public class EFTDispatchDriver {

	/** The logger. */
	private static Logger logger = org.slf4j.LoggerFactory
			.getLogger(EFTDispatchDriver.class);

	/** The eft dispatcher dao. */
	private EFTDispatcherDAO eftDispatcherDao;

	/** The batch props. */
	private String batchProps;


	private List<String> generatedFileNames;

	/**
	 * Save dispatch content.
	 *
	 * @param content the content
	 * @param FileName the file name
	 * @param physicalDocumentTypeCd the physical document type cd
	 * @param serverEnvironmentTypeCd the server environment type cd
	 * @param tradingPartnerId the trading Partner 	Id
	 * @param issuerHIOSId the issuer hios id
	 * @param statePostalCd the state postal cd
	 * @param targetEFTTypeApplicationTypeCd the target eft type application type cd
	 * @return physicalDocId
	 * @throws SQLException the SQL exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public Long saveDispatchContent(byte[] content, String FileName,
			String physicalDocumentTypeCd, String serverEnvironmentTypeCd, String tradingPartnerId,
			Integer issuerHIOSId, String statePostalCd,
			String targetEFTTypeApplicationTypeCd) throws SQLException,
	IOException {
		DateTime dateTime = DateTime.now();


		generatedFileNames = new ArrayList<String>();
		generatedFileNames.addAll(generateFileNames(physicalDocumentTypeCd,
				serverEnvironmentTypeCd, dateTime));

		if(StringUtils.isNotBlank(tradingPartnerId)) {
			generatedFileNames.addAll(generateFileNamesWithTradPartnerId(tradingPartnerId, physicalDocumentTypeCd, serverEnvironmentTypeCd, dateTime));
		}

		logger.debug("Filenames created:{}", generatedFileNames);

		PhysicalDocument physicalDocument = new PhysicalDocument();
		physicalDocument.setPhysicalDocumentDateTime(dateTime);
		physicalDocument.setPhysicalDocumentByteArray(content);
		physicalDocument.setPhysicalDocumentTypeCd(physicalDocumentTypeCd);
		physicalDocument.setServerEnvironmentTypeCd(serverEnvironmentTypeCd);
		physicalDocument.setPhysicalDocumentInvalidCndInd("N");
		physicalDocument.setIssuerHiosIdentifier(issuerHIOSId);
		physicalDocument.setStatePostalCode(statePostalCd);
		physicalDocument.setPhysicalDocumentApprvdInd("N");
		physicalDocument.setPhysicalDcmntDsptchTypeCd("C");
		physicalDocument
		.setTargetEFTApplicationTypeCd(targetEFTTypeApplicationTypeCd);
		// generating random unique FileId
		physicalDocument.setPhysicalDocumentFileName(FileName);

		long physicalDocId = eftDispatcherDao.insertPhysicalDocument(physicalDocument);

		List<DispatchedPhysicalDocument> dispatchDocuments = new ArrayList<DispatchedPhysicalDocument>();

		for (String fileName : generatedFileNames) {
			DispatchedPhysicalDocument dispatchedPhysicalDocument = new DispatchedPhysicalDocument();
			dispatchedPhysicalDocument
			.setPhysicalDocumentIdentifier(physicalDocId);
			dispatchedPhysicalDocument.setFailedDispatchInd("N");
			dispatchedPhysicalDocument
			.setDispachedPhysicalDcmntFileNm(fileName);
			dispatchDocuments.add(dispatchedPhysicalDocument);
		}

		generateFiles(generatedFileNames, content);
		eftDispatcherDao.insertsDispatchedDocuments(dispatchDocuments);

		return physicalDocId;
	}


	/**
	 * Generate file names.
	 *
	 * @param physicalDocumentTypeCd the physical document type cd
	 * @param serverEnvironmentTypeCd the server environment type cd
	 * @param dateTime the date time
	 * @return fileNames
	 */
	private List<String> generateFileNames(String physicalDocumentTypeCd,
			String serverEnvironmentTypeCd, DateTime dateTime) {

		List<Map<String, Object>> fileNamesMap = new ArrayList<Map<String, Object>>();
		fileNamesMap.addAll(eftDispatcherDao.getListofFileNames(
				physicalDocumentTypeCd, serverEnvironmentTypeCd));
		if (fileNamesMap.isEmpty()) {
			fileNamesMap.addAll(eftDispatcherDao.getListofFileNames(
					physicalDocumentTypeCd, "P"));
		}

		List<String> fileNames = new ArrayList<String>();
		for (Map<String, Object> names : fileNamesMap) {
			String targetFunctionCode = names.get(
					"TARGETPHYSICALDOCUMENTTYPECD").toString();
			String targetsourceID = names.get("TARGETTRADINGPARTNERDENTIFIER")
					.toString();

			Calendar calendar = Calendar.getInstance();
			calendar.setTime(dateTime.toDate());
			SimpleDateFormat format = new SimpleDateFormat("yyMMdd");
			String day = format.format(dateTime.toDate());
			format = new SimpleDateFormat("HHmmssSSS");
			String time = format.format(dateTime.toDate());	

			String environmentTypeCD = "T";			
			if(serverEnvironmentTypeCd.equalsIgnoreCase("P") || serverEnvironmentTypeCd.equalsIgnoreCase("R")) {
				environmentTypeCD =  serverEnvironmentTypeCd;
			}

			String fileName =  targetsourceID + "."+ targetFunctionCode + "."+ "D" + day + "." + "T" + time + "." + environmentTypeCD;

			logger.debug("physicalDocumentFileName created: " + fileName);
			fileNames.add(fileName);
		}

		return fileNames;
	}

	/**
	 * Generate file names.
	 * 
	 * @param tradingPartnerId the trading Partner cd
	 * @param functionCd the function cd
	 * @param serverEnvironmentTypeCd the server environment type cd
	 * @param dateTime the date time
	 * @return fileNames
	 */
	public List<String> generateFileNamesWithTradPartnerId(String tradingPartnerId, String functionCd,
			String serverEnvironmentTypeCd, DateTime dateTime) {		

		List<String> fileNames = new ArrayList<String>();

		String targetFunctionCode = functionCd;
		String targetsourceID = tradingPartnerId;

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dateTime.toDate());
		SimpleDateFormat format = new SimpleDateFormat("yyMMdd");
		String day = format.format(dateTime.toDate());
		format = new SimpleDateFormat("HHmmssSSS");
		String time = format.format(dateTime.toDate());	

		String environmentTypeCD = "T";			
		if(serverEnvironmentTypeCd.equalsIgnoreCase("P") || serverEnvironmentTypeCd.equalsIgnoreCase("R")) {
			environmentTypeCD =  serverEnvironmentTypeCd;
		}

		String fileName =  targetsourceID + "."+ targetFunctionCode + "."+ "D" + day + "." + "T" + time + "." + environmentTypeCD;

		logger.debug("physicalDocumentFileName created: " + fileName);
		fileNames.add(fileName);


		return fileNames;
	}

	/**
	 * Generate files.
	 *
	 * @param fileNames the file names
	 * @param byteArray the byte array
	 */
	private void generateFiles(List<String> fileNames, byte[] byteArray) {

		logger.debug("Entering generateFiles");
		OutputStream outStream = null;
		for (String fileName : fileNames) {
			try {
				logger.debug("Creating file " + batchProps + fileName);

				File newfile = new File(batchProps + fileName);
				outStream = new FileOutputStream(newfile);
				outStream.write(byteArray);

			} catch (IOException e) {
				logger.warn("IOException while writing to outboundFilePath: "
						+ e.getMessage(), e);
			} finally {
				if (outStream != null) {
					try {
						outStream.close();
					} catch (IOException e) {
						logger.warn("IOException while closing file: "
								+ e.getMessage(), e);
					}
				}
			}
		}
	}

	/**
	 * Gets the file id.
	 *
	 * @param internalFnCd the internal fn cd
	 * @return FileID
	 */
	public static String getFileID(String internalFnCd) {
		logger.debug("inside EFTDispatchDriver.getFileID method to generate Random FileID values");
		StringBuffer buf = new StringBuffer();
		Random rand = new Random(new java.util.Date().getTime());
		for (int i = 0; i < 10; i++) {
			int value = rand.nextInt(36);
			if (value < 26) {
				char c = (char) ('A' + value);
				if ((c == 'O') || (c == 'I'))
					c++;
				buf.append(c);
			} else if (value < 36) {
				char c = (char) ('2' + (value % 8));
				buf.append(c);
			}
		}
		logger.debug("generated Random FileID ->> " + internalFnCd + "."
				+ buf.toString());
		return internalFnCd + "." + buf.toString();
	}

	/**
	 * Sets the eft dispatcher dao.
	 *
	 * @param eftDispatcherDao            the eftDispatcherDao to set
	 */
	public void setEftDispatcherDao(EFTDispatcherDAO eftDispatcherDao) {
		this.eftDispatcherDao = eftDispatcherDao;
	}

	/**
	 * Sets the batch props.
	 *
	 * @param batchProps            the batchProps to set
	 */
	public void setBatchProps(String batchProps) {
		this.batchProps = batchProps;
	}
	
	/**
	 * Returns the first fileName.
	 * @return
	 */
	public String getGeneratedFileName() {
		
		String fileName = null;
		if (generatedFileNames == null) {
			generatedFileNames = new ArrayList<String>();
		}
		if (!generatedFileNames.isEmpty()) {
			fileName = generatedFileNames.get(0);
		}
		return fileName;
	}
}
