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


/**
 * @author j.radziewski
 * 
 * Generate SBMPolicy files:
 *  - a single state-wide file for all issuers
 *  - Generate one file per state containing multiple issuerIds (though not written to document in IssuerFileInformation element)
 *      - IssuerIds will be chars 1-5 of QhpId
 *
 */
public class TestSbmFileGenerator0 extends AbstractTestSbmFileGenerator implements Runnable {


	/**
	 * One file per state, max 13.
	 */
	private static final int FILE_LEN = 1;
	
	/**
	 * Number of Issuers, max 10
	 */
	private static final int ISSUER_LEN = 1;

	/**
	 * Number of QHPIDs per file, max 9999
	 */
	private static final int QHPID_LEN = 1;
	

	/**
	 * Number of Policies per QHPID
	 */
	private static final Long POL_LEN = 4L;
	/**
	 * Number of Members per Policy, max 6
	 */
	private static final Long MEM_LEN = 1L;



	public static void main(String[] args) {

		Thread t = new Thread(new TestSbmFileGenerator0());
		t.start();	
		System.out.println("TestSbmFileGenerator 0 (State wide files): STARTED\n - - - - - - - - - - - - - - - ");
	}


	public void run() {

		Long fileId = 90000000 + TestDataSBMUtility.getRandomNumberAsLong(5);
		String tenantId = null;
		int covYr = YEAR;
		String issuerId = "79998";  //HIOS ID
		String qhpId = "79998NY0010001";
		String variantId = null;
		String exchangePolicyId = null;
		String subscriberId = null;
		boolean isEffect = false;
		String insLnCd = "HLT"; //InsuranceLineCodeSimpleType.HLT

		Enrollment enrollment = null;
		PolicyType policy = null;
		PolicyMemberType member = null;
		Long memId = null;
		String text = null;

		String fileNmPrefix;
		String fileNm = "";

		int cntFile = 0;
		int cntPolicy = 0;

		ExchangeType exchngType = ExchangeType.SBM;
		String FUNC = "XPR";
		
		int rcn = 1;

		System.out.println("Creating files by state each with multiple issuerIds.");

		for(int f = 0; f < FILE_LEN; ++f) {
			rcn = 1;
			try {
				Thread.sleep(250);
			} catch (InterruptedException e1) {
				System.out.println("Very rare exception:  " + e1.getMessage());
			}
			fileNmPrefix = exchngType.getValue() + "_" + states[f] + "." + FUNC + ".";
			fileNm = fileNmPrefix + LocalDateTime.now().format(DTF_FILE) + ".T";
			tenantId = TestDataSBMUtility.SBM_STATES[f] + "0";
			
			isEffect = true;

			enrollment = TestDataSBMUtility.makeEnrollment(fileId, tenantId, covYr, issuerId, TestDataSBMUtility.FILES_STATE_WIDE);

			for (int i = 0; i < ISSUER_LEN; ++i) {
				
				//issuerId =  i + "" + i + "" + i + "" + i + "" + i;

				for (int q = 0; q < QHPID_LEN; ++q) {
					
					variantId = TestDataSBMUtility.makeVariantId(q);
					//qhpId = issuerId + states[f] +  String.format("%06d", (q + 1)) + variantId;

					for (int p = 0; p < POL_LEN; ++p) {

						memId = Long.valueOf("1");
						//exchangePolicyId = String.format("%08d", rcn);
						exchangePolicyId = TestDataSBMUtility.getRandomNumberAsString(8);
						
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
				
						policy.getFinancialInformation().addAll(TestDataSBMUtility.makeFinancialInformationList(exchangePolicyId, states[f], variantId));
						
						enrollment.getPolicy().add(policy);
						cntPolicy++;
						rcn++;
					}  // END for p (Policy)

					text = getEnrollmentAsStringXML(enrollment);

				}  // END for q (QhpId)
			} // END for i (Issuer)


			try {
				File file = new File(TEST_PATH_INPUT_DIR + fileNm);
				BufferedWriter output = new BufferedWriter(new FileWriter(file));
				output.write(text);
				output.close();

			} catch (IOException e) {
				System.out.println("Oops: " + e.getMessage());
			}
			cntFile++;
			fileId++;
		} // END for f (File)

		System.out.println("\nTotal number of files (one per state): " + cntFile);
		System.out.println("\nTotal number of Policies (" + nf.format(ISSUER_LEN * QHPID_LEN * POL_LEN) + " per state): " + nf.format(cntPolicy));
		System.out.println(" - - - - - - - - - - - - - - - - \nTestSbmFileGenerator 0: COMPLETED");
	}


}
