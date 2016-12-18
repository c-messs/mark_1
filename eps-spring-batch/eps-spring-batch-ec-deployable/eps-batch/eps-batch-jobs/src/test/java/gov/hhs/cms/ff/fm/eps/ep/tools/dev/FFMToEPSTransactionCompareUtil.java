package gov.hhs.cms.ff.fm.eps.ep.tools.dev;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


/**
 * @author j.radziewski
 *
 * Utility class to compare FFM Marklogic Insurance Plan Policies (IPP) to EPS Policy Transactions.
 * Uses CSV files for input and generates a simple "report.csv" to identify which IPPs are not in
 * EPS.  Only the first file in each directory is read.  Console output list statistics and 
 * generates the "WHERE IN" clause for any missing IPPs.  The clause can be used for querying EPS
 * to determine if perhaps a later version is already in EPS.
 * 
 *  IPP file: PTN, LastModifiedDate, VersionNumber, Status, Tenant, SelectedInsurancePlan, PolicyYear
 *  
 *  EPS file: ExchangePolicyId, SourceVersionDateTime, SourceVersionNumber, SubscriberStateCd
 *  
 *  Where IPP maps to EPS:
 *  
 *  PTN = ExchangePolicyId
 *  LastModifiedDate = SourceVersionDateTime (SVDT)
 *  VersionNumber = SourceVersionNumber (SVN)
 *  
 *  Any IPP not found is EPS is identified using the following concatenated column values:
 *  
 *  ExchangePolicyId, SVDT, SVID
 * 
 */
public class FFMToEPSTransactionCompareUtil {


	private static final String DATE_FORMAT_STR = "d-MMM-yy h.mm.ss.SSSSSS a";
	private static final String DATE_YMD_STR = "yyyy-MM-dd";//D'yyMMdd'.T'HHmmssSSS

	private static String PATH_FFM = "/Prototype834files/FFMToEPS/ffm";
	private static String PATH_EPS = "/Prototype834files/FFMToEPS/eps";
	private static String PATH_DIFF = "/Prototype834files/FFMToEPS/diff";



	public static void main(String[] args) {

		NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_YMD_STR);

		System.out.println("\nFFMToEPSTransactionCompareUtil started.\n- - - - - - - - - - - - - - - - - - \nComparing FFM file to EPS file.");

		File dirEPS = new File(PATH_EPS);
		File dirFFM = new File(PATH_FFM);
		File dirReport = new File(PATH_DIFF);

		File[] epsFiles = dirEPS.listFiles();
		File fileEPS = epsFiles[0];

		File[] ffmFiles = dirFFM.listFiles();
		File fileFFM = ffmFiles[0];
		File reportFile = new File(dirReport + File.separator + "report.csv");

		String lineEPS = null;
		String lineFFM = null;
		String prvLineEPS = null;

		HashMap<String, String> fileMapEPS = new HashMap<String, String>();
		String key = null;
		String val = null;

		DateTime lastModDT = null;
		String ymd = "";
		String prvYMD = "";
		String sqlArgs = "";

		boolean isAllFound = true;
		int cntEPSLine = 0;
		int cntFFMLine = 0;
		int cntEPSLnBrk = 0;
		int cntFFMOtherLine = 0;
		int cntNotFound = 0;
		int cntFound = 0;
		int cntIppStatus1 = 0;
		int cntIppStatus2 = 0;
		int cntIppStatus3 = 0;
		int cntIppStatus4 = 0;
		int cntThisDate = 0;
		String summaryByDate = "";

		BufferedReader br = null;

		try {
			FileWriter writer = new FileWriter(reportFile);
			System.out.println("Reading EPS file: " + fileEPS.getName());
			br = new BufferedReader(new FileReader(fileEPS));
			lineEPS = br.readLine();
			// EPS file does not have header row.
			while (lineEPS != null) {
				cntEPSLine++;
				if (!lineEPS.isEmpty()) {
					String[] col =lineEPS.split(",");
					// Convert Oracle time to JodaTime for consistent comparison.
					DateTime svDT = getDateTimeFromString(col[1]);
					key = col[0] + svDT + col[2];// ExchangePolicyId, SVDT, SVID
					val = lineEPS;
					fileMapEPS.put(key, val);
					prvLineEPS = lineEPS;
				} else {
					cntEPSLnBrk++;
				}
				lineEPS = br.readLine();
			}

			System.out.println("Reading FFM file: " + fileFFM.getName());
			br = new BufferedReader(new FileReader(fileFFM));
			lineFFM = br.readLine();
			if (lineFFM.indexOf(":") == -1) {
				System.out.println("Excluding FIRST row from FFM file:\t" + lineFFM);
				lineFFM = br.readLine();
			}
			System.out.println("Parsing or adding trailing zeroes to timestamp format YYYY-MM-DDTHH:MM:SS.sss-TZ");
			System.out.println("Excluding IPP Status from comparison.\n");
			writer.append("PTN,SVDT,SVId,Status,State\n");
			while (lineFFM != null) {
				cntFFMLine++;
				if (!lineFFM.isEmpty()) {
					if (lineFFM.indexOf(":") == -1) {
						System.out.println("\nExcluding LAST row from FFM file:\t" + lineFFM + "\n");
						cntFFMOtherLine++;
					} else {
						String[] col = lineFFM.split(",");
						// Convert MarkLogic time to JodaTime for consistent comparison.
						lastModDT = new DateTime(col[1]);
						key = col[0] + lastModDT + col[2]; // ExchangePolicyId, LastModDT, SVID
						if (!fileMapEPS.containsKey(key)) {
							if (ymd.isEmpty()) {
								System.out.println("The following FFM Policy Versions were NOT found in EPS:\n----------------------------------------\n");
							}
							if (col[3].equals("1")) {
								cntIppStatus1++;
							} else if (col[3].equals("2")) {
								cntIppStatus2++;
							} else if (col[3].equals("3")) {
								cntIppStatus3++;
							} else if (col[3].equals("4")) {
								cntIppStatus4++;
							}
							ymd = sdf.format(lastModDT.toDate());

							if (ymd.equals(prvYMD)) {
								sqlArgs += ", ";
								if (cntThisDate % 12 == 0) {
									sqlArgs+= "\n";
								}
								sqlArgs += col[0];
								cntThisDate++;
							} else {									
								if (!prvYMD.isEmpty()) {
									summaryByDate += "\n\t" + prvYMD + ": " + nf.format(cntThisDate);
								}
								if (!sqlArgs.isEmpty()) {
									sqlArgs += ", "; 
								}
								sqlArgs += "\n\n-- " + ymd + "\n " + col[0];
								prvYMD = ymd;
								cntThisDate = 1;
							}
							System.out.println(lineFFM);
							writer.append(lineFFM);
							writer.append('\n');
							isAllFound = false;
							cntNotFound++;
						} else {
							cntFound++;
						}
					}
				} else {
					cntFFMOtherLine++;
				}
				lineFFM = br.readLine();

			}
			summaryByDate += "\n\t" + prvYMD + ": " + nf.format(cntThisDate);
			if (!sqlArgs.isEmpty()) {
				sqlArgs += ")";
			}

			writer.flush();
			writer.close();

			System.out.println("\nSQL \"WHERE IN\" arguments:\n(" + sqlArgs);

			System.out.println("\n\nTotal EPS lines from file: " + nf.format((cntEPSLine)));
			System.out.println("Total EPS lineBreaks from file: " + nf.format(cntEPSLnBrk));
			System.out.println("Total EPS BatchTransMsg TRANSACTIONS compared: " + nf.format(cntEPSLine - cntEPSLnBrk));
			System.out.println("\nTotal FFM lines from file: " + nf.format(cntFFMLine));
			System.out.println("Total FFM Policy Versions compared to EPS: " + nf.format(cntFFMLine - cntFFMOtherLine));

			if (isAllFound) {
				System.out.println("\nALL " + nf.format(cntFound) + " FFM Policy Versions found in EPS BATCHTRANSMSG.");
			} else {
				System.out.println("\nFFM Policy Versions found in EPS BATCHTRANSMSG: " + nf.format(cntFound));
				System.out.println("\nFFM Policy Versions NOT found in EPS BATCHTRANSMSG: " + nf.format(cntNotFound));
				System.out.println("\nNumber of FFM Policy Versions NOT found by Date\n-----------------------------------------------\n" + 

summaryByDate);
				System.out.println("\nIPP Status of policies NOT found:\n---------------------------------\n\t1: " + nf.format(cntIppStatus1) 

				+"\n\t2: " + nf.format(cntIppStatus2) +
				"\n\t3: " + nf.format(cntIppStatus3) + "\n\t4: " + nf.format(cntIppStatus4) +
				"\n\t-----------\n\t   " + nf.format(cntIppStatus1 + cntIppStatus2 + cntIppStatus3 +  cntIppStatus4));


			}

		} catch (IOException ioEx) {
			System.out.println("\tOops, no file or something: " + ioEx.getMessage());
		} catch (Exception ex) {

			System.out.println("\tOops: \nPrevious EPS Line: " + prvLineEPS + "\n" + nf.format(cntEPSLine  + cntEPSLnBrk) + ") lineEPS: " + lineEPS + 

					"\n" + nf.format(cntFFMLine) + ") lineFFM: " + lineFFM);
			System.out.println(ex.getMessage());
		}


		System.out.println("\n- - - - - - - - - END - - - - - - - - -");

	}

	private static DateTime getDateTimeFromString(String dt) {

		final DateTimeFormatter dateFormatter = DateTimeFormat.forPattern(DATE_FORMAT_STR);
		DateTime date = dateFormatter.parseDateTime(dt);
		return date;
	}

}
