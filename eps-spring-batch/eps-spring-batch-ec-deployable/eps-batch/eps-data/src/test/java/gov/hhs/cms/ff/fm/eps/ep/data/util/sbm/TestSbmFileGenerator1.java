package gov.hhs.cms.ff.fm.eps.ep.data.util.sbm;

import gov.cms.dsh.sbmi.Enrollment;
import gov.cms.dsh.sbmi.PolicyMemberType;
import gov.cms.dsh.sbmi.PolicyType;
import gov.hhs.cms.ff.fm.eps.ep.enums.ExchangeType;
import gov.hhs.cms.ff.fm.eps.ep.util.sbm.TestDataSBMUtility;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;



/**
 * @author j.radziewski
 * 
 * Generate SBMPolicy files:
 *  - multiple files (one per issuer)
 *  - Generate one file per state containing multiple issuerIds (though not written to document in IssuerFileInformation element)
 *      - IssuerIds will be chars 1-5 of QhpId
 *      
 *  
 *
 */
public class TestSbmFileGenerator1 extends AbstractTestSbmFileGenerator implements Runnable {


	/**
	 * Number of Issuers, max 10
	 */
	private static final int ISSUER_LEN = 1;

	/**
	 * Number of States, max 13
	 */
	private static final int STATE_LEN = 1;

	/**
	 * Number of QHPIDs per file, max 9999
	 */
	private static final int QHPID_LEN = 1;

	/**
	 * Number of Policies per QHPID
	 */
	private static final Long POL_LEN = 3L;
	/**
	 * Number of Members per Policy, MAX 6
	 */
	private static final Long MEM_LEN = 2L;


	public static void main(String[] args) {

		Thread t = new Thread(new TestSbmFileGenerator1());
		t.start();	
		System.out.println("TestSbmFileGenerator 1 (one file per issuer): STARTED\n - - - - - - - - - - - - - - - ");
	}


	public void run() {

		Long idSeed = TestDataSBMUtility.getRandomNumberAsLong(4);
		Long fileId = idSeed + 1000000L;
		String tenantId = null;
		int covYr = YEAR;
		String issuerId = "79998";  //HIOS ID <- make array for multilple files.
		String qhpId = "79998NY0010001";
		String variantId = "02";
		String exchangePolicyId = null;
		String subscriberId = null;
		boolean isEffect = false;
		String insLnCd = "HLT"; //InsuranceLineCodeSimpleType.HLT

		Enrollment enrollment = null;
		PolicyType policy = null;
		PolicyMemberType member = null;
		Long memId = null;
		String text = null;

		String fileNmPrefixXPR;
		String fileNmXPR = "";
		String fileNmPrefixSBMS;
		String fileNmSBMS = "";


		int cntFile = 0;
		int cntPolicy = 0;

		ExchangeType exchngType = ExchangeType.SBM;
		
		int rcn = 1;

		List<String> updateStatusFileLines = new ArrayList<String>();

		System.out.println("Creating files by state each with multiple issuerIds one per file.");

		for (int s = 0; s < STATE_LEN; ++s) {

			for (int i = 0; i < ISSUER_LEN; ++i) {
				
				rcn = 1;
				try {
					Thread.sleep(250);
				} catch (InterruptedException e1) {
					System.out.println("Very rare exception:  " + e1.getMessage());
				}
				String fileDateTime =  LocalDateTime.now().format(DTF_FILE);
				fileNmPrefixXPR = exchngType.getValue() + "_" + states[s] + ".XPR.";
				fileNmXPR = fileNmPrefixXPR + fileDateTime + ".T";
				fileNmPrefixSBMS = exchngType.getValue() + "_" + states[s] + ".SBMS.";
				fileNmSBMS = fileNmPrefixSBMS + fileDateTime + ".T";
				tenantId = states[s] + "0";
				isEffect = true;

				enrollment = TestDataSBMUtility.makeEnrollment(fileId, tenantId, covYr, issuerId, TestDataSBMUtility.FILES_ONE_PER_ISSUER);

				String sbmFileId = enrollment.getFileInformation().getFileId();
				updateStatusFileLines.add((i + 1) + "," + tenantId + ",," + sbmFileId + ",,Approve");

				for (int q = 0; q < QHPID_LEN; ++q) {

					for (int p = 0; p < POL_LEN; ++p) {

						memId = Long.valueOf("1");
						
						exchangePolicyId = String.format("%08d", (fileId * 10) + rcn);
						subscriberId = "DAD-" + exchangePolicyId + "-" + memId;

						policy = TestDataSBMUtility.makePolicyType(rcn, qhpId, exchangePolicyId, subscriberId, isEffect, insLnCd);

						for(int m = 0; m < MEM_LEN; ++m) {

							if (m == 0) {
								member = TestDataSBMUtility.makePolicyMemberType(exchangePolicyId, memId, names[m], true);
							} else {
								member = TestDataSBMUtility.makePolicyMemberType(exchangePolicyId, memId, names[m], false);
							}
							policy.getMemberInformation().add(member);
							memId++;
						} // END for m, memLen

						policy.getFinancialInformation().addAll(TestDataSBMUtility.makeFinancialInformationList(exchangePolicyId, states[s], variantId));

						enrollment.getPolicy().add(policy);
						cntPolicy++;
						rcn++;
					}  // END for p (Policy)

					text = getEnrollmentAsStringXML(enrollment);

				}  // END for q (QhpId)



				try {
					File file = new File(TEST_PATH_INPUT_DIR + fileNmXPR);
					BufferedWriter output = new BufferedWriter(new FileWriter(file));
					output.write(text);
					output.close();

				} catch (IOException e) {
					System.out.println("Oops: " + e.getMessage());
				}
				cntFile++;
				fileId++;
			} // END for i (Issuer)
		} // END for s (State)
		
		
		File updateStatus = new File(TEST_PATH_UPDATE_STATUS_INPUT_DIR + fileNmSBMS);
		FileWriter writer;
		try {
			writer = new FileWriter(updateStatus);
			writer.append("Line Number,TenantID,IssuerID,FileID,IssuerFileSetID,Status\n");
			for (String line : updateStatusFileLines) {
				writer.append(line);
				writer.append('\n');
			}
			writer.flush();
		    writer.close();

		} catch (IOException e) {
			System.out.println("Oops: " + e.getMessage());
		}
		



		System.out.println("\nTotal number of Files (one issuer per file): " + cntFile);
		System.out.println("\nTotal number of Issuers (one issuer per file): " + cntFile);
		System.out.println("\nTotal number of Policies (" + nf.format(ISSUER_LEN * QHPID_LEN * POL_LEN) + " per state): " + nf.format(cntPolicy));
		System.out.println(" - - - - - - - - - - - - - - - - \nTestSbmFileGenerator 1: COMPLETED");
	}


}
