package gov.hhs.cms.ff.fm.eps.ep.data.util;

import gov.cms.dsh.bem.BenefitEnrollmentMaintenanceType;
import gov.cms.dsh.bem.BenefitEnrollmentRequest;
import gov.cms.dsh.bem.MemberType;
import gov.hhs.cms.ff.fm.eps.ep.enums.ExchangeType;
import gov.hhs.cms.ff.fm.eps.ep.enums.PolicyStatus;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.joda.time.DateTime;

/**
 * DEVELOPER TOOL
 * --------------
 * Use this file generator to create files for ERL.
 * 
 * - 10 QhpId
 * - 10 BEMs per QHPID each with one subscriber and one dependent.
 * - Creates 100 INITIAL files per QHPID (All initials are good or happy path files)
 * - Creates 100 EFFECTUATION files per QHPID with:
 * - BEMs with "1000..." data are happy path BEMs ( INI, EFF, MNT)
 * - Filesets can be run together in one job.
 * - Running files will result in the following:
 * 
 *  SELECT PROCESSEDTODBSTATUSTYPECD AS STATUS, COUNT(PROCESSEDTODBSTATUSTYPECD) AS TOTAL_STATUS 
 *  FROM BATCHTRANSMSG GROUP BY PROCESSEDTODBSTATUSTYPECD;
 *  
 *  SELECT COUNT(*) AS TOTAL_TRANSMSG FROM TRANSMSG;
 *  SELECT COUNT(*) AS TOTAL_BATCHTRANSMSG FROM BATCHTRANSMSG;
 *  SELECT count(*) AS TOTAL_POLICIES FROM POLICYVERSION;
 *  SELECT count(*) AS TOTAL_MEMBERS FROM POLICYMEMBERVERSION;
 * 
 * STATUS   TOTAL_STATUS
 * ------   ------------
 * Y                 380 
 * S                  20
 *  
 * TOTAL_TRANSMSG       400 
 * TOTAL_BATCHTRANSMSG  400 
 * TOTAL_POLICIES       380 
 * TOTAL_MEMBERS        200
 *  
 * 
 * @author j.radziewski
 *
 */
public class TestErlBerFileGenerator1 extends AbstractTestFileGenerator implements Runnable {

	private static final int BER_LEN = 10;

	/**
	 * Number of BEMs per BER (QhpId)
	 */
	private static final Long BEM_LEN = 10L;
	/**
	 * Number of Members per BEM (Policy)
	 */
	private static final Long MEM_LEN = 2L;
	/**
	 * For 3 members per BEM, around 6000 is the limit for string builder and marshalling.
	 */
	private static final int MAX_BEMS_PER_FILE = 6000; 

	private String TEST_PATH_MANIFEST = "/Prototype834files/erl/manifest";
	private String TEST_PATH_INPUT_DIR = "/Prototype834files/erl/input/";

	private DecimalFormat fmt0 = new DecimalFormat("#,###");

	public static void main(String[] args) throws IOException {

		System.out.println("TestErlBerFileGenerator 1: STARTED");
		Thread t = new Thread(new TestErlBerFileGenerator1());
		t.start();	
	}

	public void run() {

		DateTime start = new DateTime();

		ExchangeType exchngType = ExchangeType.FFM;
		String FUNC = "IC834";
		int jobSetNum = 400;
		int[] manifestNums = {jobSetNum + 1, jobSetNum + 2, jobSetNum + 3, jobSetNum + 4};
		int cntFileSet = 0;
		int cntBem = 0;
		int cntBemsPerSet = 0;
		int cntQhpId = 0;

		BenefitEnrollmentRequest ber = null;
		BenefitEnrollmentMaintenanceType bem = null;
		PolicyStatus[] policyStatus = {PolicyStatus.INITIAL_1, PolicyStatus.EFFECTUATED_2, PolicyStatus.EFFECTUATED_2, PolicyStatus.SUPERSEDED_5};
		String[] statusShortNm = {"INI", "EFF", "EFF", "SUP"};
		MemberType member = null;
		String fileNmPrefix;
		String fileNm = "";
		LocalDateTime currentTimestamp = null;
		String text = "";

		Long berId = null;
		Long bemId = Long.valueOf("100000000");
		Long bemIdToUse = null;
		Long memId = null;

		int versionNum = 1;

		String subscriberId = null;
		// Starting point for created ExchangePolicyIds
		int hcGPNBaseIndex = 1;
		String hcGPN = null;
		Long hcGPNNum = null;
		String hiosId = null;
		String groupSenderId = null;
		String variantId = "00";

		String[] names = {"DAD", "MOM", "SON", "DAU", "SON4", "DAU5", "DEP6", "DEP7", "BBY8", "BBY9"};
		String[] states = {"NC", "ND", "OH", "PA", "GA", "RI", "TN", "CT", "VA", "AL",
				"WV", "MD", "TX", "NH", "MS", "FL", "CA", "NV", "NM", "MA"};
		String state = null;

		for(int m = 0; m < manifestNums.length; ++m) {

			fileNmPrefix = exchngType.getValue() + "_" + (m + 1) + "_" + statusShortNm[m] + "." + FUNC + ".";

			cntBemsPerSet = 0;
			cntQhpId = 0;
			cntBem = 0;

			File inputDir = new File(TEST_PATH_INPUT_DIR + manifestNums[m]);
			if (!inputDir.exists()) {
				inputDir.mkdirs();
			}
			
			if (m == 3) {
				setDoSkip(true);
			} else {
				setDoSkip(false);
			}
			

			System.out.println("Creating files for Manifest JobId: " + manifestNums[m]);

			for(int i = 0; i < BER_LEN; ++i) {

				hiosId =  i + "" + i + "" + i + "" + i + "" + i;
				berId = Long.valueOf(hiosId);
				bemId = Long.valueOf("100000000");
				state = states[i % states.length];
				groupSenderId = hiosId + state + "0" + hiosId.substring(0, 4) + "01";
				currentTimestamp = LocalDateTime.now();
				// sleep so CurrentTimestamp is different is different between file sets.
				try {
					Thread.sleep(SLEEP_INTERVAL);
				} catch (Exception ex) {
					System.out.println("EX: " + ex.getMessage());
				}
				ber = new BenefitEnrollmentRequest();
				ber.setFileInformation(makeFileInformationType(berId, exchngType, groupSenderId, currentTimestamp));

				fileNm = fileNmPrefix + LocalDateTime.now().format(DTF_FILE) + ".T";


				File file = new File(TEST_PATH_INPUT_DIR + manifestNums[m] + "/" + fileNm);

				LocalDate psd = LocalDate.of(YEAR, 1, (i + 1));
				LocalDate ped = LocalDate.of(YEAR, 6, (i + 1));

				for(int j = 0; j < BEM_LEN; ++j) {

					bemId += 10; // ExchangePolicyId gets generated from this.
					versionNum = (m + 1);
					
					switch(j) {
					// "make" methods will look for this data in order to create data
					// with special circumstances
					case 3:
						// "4000..." file will invalid MGPID causing business skip, EPROD-38.
						bemIdToUse = bemId + 300000000;
						break;
					case 5:
						// "6000..." file will have PolicyStatus==0 causing business skip, EPROD-3.
						bemIdToUse = bemId + 500000000;
						break;
					default:
						bemIdToUse = bemId;
					}
					memId = bemIdToUse;
					hcGPNNum = (Long.parseLong(hiosId) * 10000000000L);
					hcGPNNum = hcGPNNum + hcGPNBaseIndex + j;
					hcGPN = String.format("%014d", hcGPNNum) + variantId;
					BigDecimal tpa = new BigDecimal(getRandomNumber(3));

					bem = makeBenefitEnrollmentMaintenanceType(bemIdToUse, String.format("%05d", versionNum), currentTimestamp, hiosId);
					bem.setPolicyInfo(makePolicyInfoType(bemIdToUse, policyStatus[m], hcGPN, psd, ped));
					ber.getBenefitEnrollmentMaintenance().add(bem);

					for(int k = 0; k < MEM_LEN; ++k) {
						memId++;				
						if(k == 0) {
							subscriberId = "DAD-" + memId;
							member = makeMemberType(memId, names[k], true, subscriberId, hcGPN, groupSenderId, tpa, variantId);
						} else {
							member = makeMemberType(memId, names[k], false, subscriberId, hcGPN, groupSenderId, tpa, variantId);
						}
						bem.getMember().add(member);

					} // END for k, memLen

					if (cntBemsPerSet % MAX_BEMS_PER_FILE == 0 && cntBemsPerSet != 0) {

						text = getBerXMLAsString(ber);

						try {

							System.out.println("File set " + (cntFileSet + 1) + ") " + state + "|" + groupSenderId + "|" + fmt0.format(MAX_BEMS_PER_FILE * 2));
							file = new File(TEST_PATH_INPUT_DIR + manifestNums[m] + "/" + fileNm);
							BufferedWriter output = new BufferedWriter(new FileWriter(file));
							output.write(text);
							output.close();

							cntFileSet++;
							cntBemsPerSet = 0;

						} catch ( IOException e ) {
							e.printStackTrace();
						}

						ber = new BenefitEnrollmentRequest();
						ber.setFileInformation(makeFileInformationType(berId, exchngType, groupSenderId, currentTimestamp));

						fileNm = fileNmPrefix + LocalDateTime.now().format(DTF_FILE) + ".T";

						file = new File(TEST_PATH_INPUT_DIR  + manifestNums[m] + "/" +  fileNm);

					}
					cntBem++;
					cntBemsPerSet++;
				} // END for j, BEM_LEN

				text = getBerXMLAsString(ber);

				try {
					file = new File(TEST_PATH_INPUT_DIR + manifestNums[m] + "/" + fileNm);
					BufferedWriter output = new BufferedWriter(new FileWriter(file));
					output.write(text);
					output.close();
					cntFileSet++;
					cntBemsPerSet = 0;

				} catch ( IOException e ) {
					e.printStackTrace();
				}
				cntQhpId++;
			} //  END for i, BER_LEN

			try {
				writeManifestFile(cntBem, manifestNums[m]);
			}  catch ( IOException e ) {
				e.printStackTrace();
			}

		} // END for m (manifests)

		DateTime end = new DateTime();
		System.out.println("\nTestErlBerFileGenerator 1 Summary for Job Set " + jobSetNum + "\n--------------------------------------");
		double totalSeconds = (end.getMillis() - start.getMillis()) * .001;
		if (totalSeconds > 60) {
			double totalMinutes = totalSeconds / 60;
			double seconds = totalSeconds % 60;
			System.out.println("Time to generate: " + Math.round(totalMinutes) + "." + Math.round(seconds) +  " (minutes:seconds)");
		} else {
			System.out.println("Time to generate: " + Math.round(totalSeconds) + " seconds.");
		}

		for (int m = 0; m < manifestNums.length; ++m) {

			System.out.println("\nManifest JobId: " + manifestNums[m]);
			System.out.println("\tFiles:  " + fmt0.format(BER_LEN));
			System.out.println("\tQhpIds: " + fmt0.format(cntQhpId));
			System.out.println("\tBEMs:   " + fmt0.format(BER_LEN * BEM_LEN));
		}
		
		System.out.println("\nFile setS results in: \n\tTOTAL_TRANSMSG       400\n\tTOTAL_BATCHTRANSMSG  400\n\tTOTAL_POLICIES       380" +
				"\n\tTOTAL_MEMBERS        200\nBatchTranMsg:\n\tSTATUS   TOTAL_STATUS\n\t------   ------------\n\tI                  10" +
				"\n\tY                 260\n\tS                  30");
		
		System.out.println("TestErlBerFileGenerator 1: COMPLETED");
	}
	
	


	private void writeManifestFile(int recordCount, int manifestNum) throws IOException {

		File manifestFile = new File(TEST_PATH_MANIFEST + File.separator + "Manifest-" + manifestNum + ".txt");
		if (!manifestFile.exists()) {
			manifestFile.createNewFile();
		}

		PrintWriter writer = new PrintWriter(manifestFile);
		writer.println("jobid=" + manifestNum);
		writer.println("BeginHighWaterMark=" + DateTime.now() );
		writer.println("JobStartTime=" + DateTime.now().minusYears(2));
		writer.println("JobEndTime=" + DateTime.now().minusYears(2).plusHours(1));
		writer.println("EndHighWaterMark=" + DateTime.now().plusHours(1));
		writer.println("RecordCount=" + recordCount);
		writer.println("PAETCOMPLETION=N");
		writer.write("JobStatus=SUCCESS");

		writer.flush();
		writer.close();
	}


}
