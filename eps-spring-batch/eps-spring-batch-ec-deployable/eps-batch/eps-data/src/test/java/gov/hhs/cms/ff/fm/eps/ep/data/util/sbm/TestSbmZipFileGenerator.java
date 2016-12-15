


package gov.hhs.cms.ff.fm.eps.ep.data.util.sbm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import gov.cms.dsh.sbmi.Enrollment;
import gov.cms.dsh.sbmi.PolicyMemberType;
import gov.cms.dsh.sbmi.PolicyType;
import gov.hhs.cms.ff.fm.eps.ep.util.sbm.TestDataSBMUtility;



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
public class TestSbmZipFileGenerator extends AbstractTestSbmFileGenerator implements Runnable {

	/**
	 * Number of QHPIDs per file, max 10
	 */
	private static final int QHPID_LEN = 5;
	/**
	 * Number of Policies per QHPID
	 */
	private static final Long POL_LEN = 100L;
	/**
	 * Number of Members per Policy, MAX 6
	 */
	private static final Long MEM_LEN = 3L;


	public static void main(String[] args) {

		Thread t = new Thread(new TestSbmZipFileGenerator());
		t.start();	
		System.out.println("TestSbmZipFileGenerator: STARTED\n - - - - - - - - - - - - - - - ");
	}


	public void run() {

		int cntFile = 0;
		int cntPolicy = 0;
		// 45515VT002000%,  18068NY002000%  Less last number.
		String envCd = "T";
		String stateCd = "VT";
		String sourceId = TestDataSBMUtility.getRandomNumberAsString(3) + stateCd;
		String issuerId = "45515";

		String zipFileNm = TestDataSBMUtility.makeFileName(sourceId, envCd);
		String zipEntryFileName = null;
		
		Long idSeed = 1L;//TestDataSBMUtility.getRandomNumberAsLong(4);
		Long fileId = TestDataSBMUtility.getRandomNumberAsLong(6);// idSeed + 100000L;
		
		int rcn = 1;

		String tenantId = stateCd + "0";
		int covYr = YEAR;
		
		Enrollment enrollment = null;
		PolicyType policy = null;
		PolicyMemberType member = null;
		
		Long memId = null;
		String exchangePolicyId = null;
		String subscriberId = null;
		boolean isEffect = true;
		String insLnCd = "HLT"; //InsuranceLineCodeSimpleType.HLT
		String variantId = "02";

		// Put the created zip file in the test folder for now. The actual File handle is 
		// passed into method processZipFiles(,).
		File zipFile = new File(TEST_PATH_INPUT_DIR + zipFileNm);
		
		System.out.println("Created ZIP file:  " + zipFileNm +"\n- - - - - - - - - - - - - - - - - - - - - - - ");

		ZipOutputStream zos = null;
		try {
			zos = new ZipOutputStream(new FileOutputStream(zipFile));
		} catch (FileNotFoundException e) {
			System.out.print("Oops: " + e.getMessage());
		}

		for (int i = 0; i < QHPID_LEN; ++i) {

			zipEntryFileName = TestDataSBMUtility.makeZipEntryFileName(sourceId, envCd);

			cntPolicy = 0;
			int idx = (i + 1);

			String sbmFileId = "FID-" + String.format("%05d", idx);
			//will make qhpIds from 45515VT0200001 to 45515VT0200005
			String qhpId = issuerId + stateCd + "020000" + idx;

			enrollment = TestDataSBMUtility.makeEnrollment(sbmFileId, tenantId, covYr, issuerId, TestDataSBMUtility.FILES_ONE_PER_ISSUER);
			
			for (int p = 0; p < POL_LEN; ++p) {

				memId = Long.valueOf("1");
				
				exchangePolicyId = String.format("%08d", (idx * 10) + rcn);
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

				policy.getFinancialInformation().addAll(TestDataSBMUtility.makeFinancialInformationList(exchangePolicyId, stateCd, variantId));

				enrollment.getPolicy().add(policy);
				cntPolicy++;
				rcn++;
			}  // END for p (Policy)
			
			System.out.println(idx + ") " + zipEntryFileName +  " : QhpId=" + qhpId  + ", PolicyCount="+ cntPolicy);


			String sbmFileXML = TestDataSBMUtility.getEnrollmentAsXmlString(enrollment);
			sbmFileXML = TestDataSBMUtility.prettyXMLFormat(sbmFileXML);

			ZipEntry zipEntry = new ZipEntry(zipEntryFileName);
			try {
				zos.putNextEntry(zipEntry);
				byte[] data = sbmFileXML.getBytes();
				zos.write(data, 0, data.length);

			} catch (IOException e) {
				System.out.print("Oops: " + e.getMessage());
			}
			


			// Delay a little to get a different fileName since it is timestamp based.
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				System.out.print("Oops: " + e.getMessage());
			}
		}

		try {
			zos.close();
		} catch (IOException e) {
			System.out.print("Oops: " + e.getMessage());
		}
	}
	
}
