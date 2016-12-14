package gov.hhs.cms.ff.fm.eps.ep.jobs.util;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import gov.cms.dsh.sbmi.Enrollment;
import gov.cms.dsh.sbmi.FileInformationType;
import gov.cms.dsh.sbmi.FileInformationType.IssuerFileInformation;
import gov.cms.dsh.sbmi.FileInformationType.IssuerFileInformation.IssuerFileSet;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;

public class SBMTestDataDBUtil {

	public static final Logger LOGGER = LoggerFactory.getLogger(SBMTestDataDBUtil.class);
	
	private static final int YEAR = LocalDate.now().getYear();

	//SBM will submit SBMI files using defined options:
	// - a single state-wide file for all issuers
	// - multiple files (one per issuer)
	// - and/or a fileset (with multiple files for a single issuer). 
	public static final int FILES_STATE_WIDE = 0;
	public static final int FILES_ONE_PER_ISSUER = 1;
	public static final int FILES_FILESET = 2;
	
	private static final DateTimeFormatter DTF_FILE = DateTimeFormatter.ofPattern("'D'yyMMdd'.T'HHmmssSSS");

	public static final String[] SBM_STATES = {"NY", "WA", "MN", "CO", "CT", "MD", "KY", "RI", "VT", "CA", "MA", "ID", "DC"};


	public static void executeScript(JdbcTemplate jdbcTemplate, String relativeFilePath, boolean continueOnError) throws SQLException {

		LOGGER.info("Executing script relativeFilePath :{}", relativeFilePath);

		String absoluteFilePath = new File("").getAbsolutePath().replace("\\", "/") + relativeFilePath;

		LOGGER.info("Absolute file path :{}", absoluteFilePath);

		Connection connection = jdbcTemplate.getDataSource().getConnection();

		EncodedResource sql = new EncodedResource(new FileSystemResource(new File(absoluteFilePath)));

		ScriptUtils.executeSqlScript(connection, sql, continueOnError,
				false, ScriptUtils.DEFAULT_COMMENT_PREFIX,
				ScriptUtils.DEFAULT_STATEMENT_SEPARATOR,
				ScriptUtils.DEFAULT_BLOCK_COMMENT_START_DELIMITER,
				ScriptUtils.DEFAULT_BLOCK_COMMENT_END_DELIMITER);

		connection.close();

	}

	public static void cleanupData(JdbcTemplate jdbcTemplate) throws SQLException {		
		executeScript(jdbcTemplate, "/src/test/resources/sql/deleteSBMData.sql", false);
	}

	public static Integer selectData(JdbcTemplate jdbcTemplate, String sql) {
		int count = 0;
		LOGGER.info("Running query: " + sql);
		count = jdbcTemplate.queryForObject(sql, Integer.class);
		LOGGER.debug("Query retrieved " + count
				+ " items");
		return count;
	}


	private static LocalDateTime getLocalDateTimeWithMicros() {

		int microSec = getRandomNumber(3);
		LocalDateTime ldt = LocalDateTime.now();
		int micros = (microSec * 1000);
		ldt = ldt.plusNanos(micros);
		return ldt;
	}
	
	/**
	 * File Name =TradingPartnerID.AppId.FuncCode.Date.Time.EnvCode.Direction
	 * 
	 * Trading Partner ID SBM Source ID (for source)
     * App ID - EPS
     * Function Code - SBMI
     * Date - DYYMMDD where the first character ‘D’ is static text and the rest us the date in ‘YYMMDD’ format
     * Time - THHMMSSmmm where the first character ‘T’ is a static text and the rest is the time in ‘HHMMSSmmm’ format
     * Environment Code - P for production, T for testing, R for production readiness
     * Direction - IN
	 * @param sourceId
	 * @return
	 */
	public static String makeZipEntryFileName(String sourceId, String envCd) {

		return sourceId + ".EPS.SBMI." + LocalDateTime.now().format(DTF_FILE) + "." + envCd;
	}
	
	/**
	 * Zip File Name format: FuncCode.TradingPartnerID.Date.Time.EnvCode.Direction
	 * @param sourceId
	 * @return
	 */
	public static String makeFileName(String sourceId, String envCd) {

		return "SBMI." + sourceId + "."+ LocalDateTime.now().format(DTF_FILE) + "." + envCd;
	}


	/**
	 * Also makes FileInformationType and IssuerFileInformation. 
	 * @param xprId
	 * @param tenantId
	 * @param covYr
	 * @param issuerId
	 * @return
	 */
	public static Enrollment makeEnrollment(String sbmFileId, String tenantId, int covYr, String issuerId, int issuerFileType) {

		Enrollment enrollment = new Enrollment();
		enrollment.setFileInformation(makeFileInformationType(sbmFileId, tenantId, covYr, issuerId, issuerFileType));
		return enrollment;
	}

	/**
	 * Makes FileInformationType with IssuerFileInformation but WITHOUT IssuerFileSet. Call "makeIssuerFileSet" 
	 * to create IssuerFileSet.
	 * 
	 * issuerFileTypes:
	 *   - FILES_STATE_WIDE
	 *   - FILES_ONE_PER_ISSUER
	 *   - FILES_FILESET

	 * @param fileId
	 * @param tenantId
	 * @param covYr
	 * @param issuerId
	 * @return
	 */
	public static FileInformationType makeFileInformationType(String sbmFileId, String tenantId, int covYr, String issuerId, int issuerFileType) {

		FileInformationType fileInfo = new FileInformationType();
		fileInfo.setFileId(sbmFileId);
		fileInfo.setFileCreateDateTime(DateTimeUtil.getXMLGregorianCalendar(getLocalDateTimeWithMicros()));
		fileInfo.setTenantId(tenantId);
		fileInfo.setCoverageYear(covYr);
		if (issuerFileType != FILES_STATE_WIDE) {
			fileInfo.setIssuerFileInformation(makeIssuerFileInformation(issuerId));	
		}
		return fileInfo;
	}


	/** 
	 * Makes FileInformationType WITHOUT IssuerFileSet.
	 *  issuerFileTypes:
	 *    - FILES_STATE_WIDE
	 *    - FILES_ONE_PER_ISSUER
	 *    - FILES_FILESET
	 * @param covYr
	 * @param issuerFileType
	 * @return
	 */

	public static FileInformationType makeFileInformationType(String sbmFileId, String tenantId, String issuerId, int issuerFileType) {

		return makeFileInformationType(sbmFileId, tenantId, YEAR, issuerId, issuerFileType);
	}


	/**
	 * Make IssuerFileInformation without IssuerFileSet
	 * Call makeIssuerFileSet to make file sets.
	 * @param issuerId
	 * @return
	 */
	private static IssuerFileInformation makeIssuerFileInformation(String issuerId) {

		IssuerFileInformation issuerFileInfo = new IssuerFileInformation();
		issuerFileInfo.setIssuerId(issuerId);
		// set IssuerFileSet independently
		return issuerFileInfo;
	}

	public static IssuerFileSet makeIssuerFileSet(String fileSetId, int fileNum, int totalIssuerFiles) {

		IssuerFileSet issuerFileSet = new IssuerFileSet();
		issuerFileSet.setIssuerFileSetId(fileSetId);// HIOSID + 99999, First 5 characters should match issuer ID
		issuerFileSet.setFileNumber(fileNum);
		issuerFileSet.setTotalIssuerFiles(totalIssuerFiles);
		return issuerFileSet;
	}
	
	public static String getEnrollmentAsXmlString(Enrollment enrollment) {

		String xml = "";
		try {
			JAXBContext context = JAXBContext.newInstance(Enrollment.class);
			Marshaller marshaller = context.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			marshaller.marshal(enrollment, stringWriter);
			xml = stringWriter.toString();

		} catch (Exception ex) {
			System.out.println("Ex at getEnrollmentAsXmlString: "+ ex.getMessage());
		}
		return xml;
	}
	
	public static String prettyXMLFormat(String input, int indent) {
		try {
			Source xmlInput = new StreamSource(new StringReader(input));
			StringWriter stringWriter = new StringWriter();
			StreamResult xmlOutput = new StreamResult(stringWriter);
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			transformerFactory.setAttribute("indent-number", indent);
			Transformer transformer = transformerFactory.newTransformer(); 
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(xmlInput, xmlOutput);
			return xmlOutput.getWriter().toString();
		} catch (Exception e) {
			throw new RuntimeException(e); // simple exception handling, please review it
		}
	}

	public static String prettyXMLFormat(String input) {
		return prettyXMLFormat(input, 2);
	}


	public static int getRandomNumber(int digits) {
		double dblDigits = (double) digits;
		double min = Math.pow(10.0, dblDigits - 1);
		double max = Math.pow(10.0, dblDigits) - 1;
		int randNum = (int) Math.round(Math.random() * (max - min) + min);
		return randNum;
	}

	public static Long getRandomNumberAsLong(int digits) {
		int intNum = getRandomNumber(digits);
		return Long.valueOf(intNum);
	}

	public static String getRandomNumberAsString(int digits) {
		int intNum = getRandomNumber(digits);
		return String.valueOf(intNum);
	}

	public static String getRandomSbmState() {
		int randNum = (int) Math.round(Math.random() * (SBM_STATES.length - 1));
		return SBM_STATES[randNum];
	}


}
