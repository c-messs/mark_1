package gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.data.util;

import gov.cms.dsh.bem.BenefitEnrollmentMaintenanceType;
import gov.cms.dsh.bem.BenefitEnrollmentRequest;
import gov.cms.dsh.bem.MemberType;
import gov.hhs.cms.ff.fm.eps.ep.EPSConstants;
import gov.hhs.cms.ff.fm.eps.ep.enums.ExchangeType;
import gov.hhs.cms.ff.fm.eps.ep.enums.PolicyStatus;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Calendar;

import org.joda.time.DateTime;

public class ErlTestFileGenerator extends AbstractTestFileGenerator {

	/**
	 * Number of BER files to generate X 2 (Initial and Effectuation)
	 */
	private int berLen = 1;
	private Integer bemCount;
	private Integer memberCount;
	private boolean isEFF = false;
	private String paet;
	private int manifestNum = getRandomNumber(3).intValue();

	private File manifestDir;
	private File inputDir;

	private int cntBEM = 0;
	private int cntSub = 0;
	private int cntNon = 0;
	private int cntDep = 0;


	
	/**
	 * Defaults:
	 * - berLen = 1
	 * - bemCount = 1
	 * - memberCount = 1
	 * - isEff = false (so berLen will be 1 * berLen vs 2 * berLen if true)
	 * - paet = N
	 */
	public ErlTestFileGenerator() {

		this.berLen = 1;	
		this.bemCount = Integer.valueOf(1);
		this.memberCount = Integer.valueOf(1);
		this.isEFF = false;
		this.paet = EPSConstants.N;
	}
	
	/**
	 * Defaults paet = N
	 * @param cntBer
	 * @param cntBemsPerBer
	 * @param cntMembersPerBem
	 * @param isEffectuation
	 * @param manifestDir
	 * @param inputDir
	 */
	public ErlTestFileGenerator (int cntBer, int cntBemsPerBer, int cntMembersPerBem, 
			boolean isEffectuation, File manifestDir, File inputDir) {

		this.berLen = cntBer;
		this.bemCount = cntBemsPerBer;
		this.memberCount = cntMembersPerBem;
		this.isEFF = isEffectuation;
		this.manifestDir = manifestDir;
		this.inputDir = inputDir;
		this.paet = EPSConstants.N;
	}


	/**
	 * @param cntBer
	 * @param cntBemsPerBer
	 * @param cntMembersPerBem
	 * @param isEffectuation
	 * @param manifestDir
	 * @param inputDir
	 * @param paet
	 */
	public ErlTestFileGenerator (int cntBer, int cntBemsPerBer, int cntMembersPerBem, 
			boolean isEffectuation, File manifestDir, File inputDir, String paet) {

		this.berLen = cntBer;
		this.bemCount = cntBemsPerBer;
		this.memberCount = cntMembersPerBem;
		this.isEFF = isEffectuation;
		this.manifestDir = manifestDir;
		this.inputDir = inputDir;
		this.paet = paet;
	}
	
	public String makeFiles() {
	
		return makeFiles(paet);
	}


	/**
	 * Makes ERL Initial and Effectuation files (if isEFF == true) and builds corresponding
	 * manifest file for the file set.
	 * @return manifestNum as String
	 */
	public String makeFiles(String paetCompletion) {

		// Increment so each subsequent call will be the next sequential manifest number.
		manifestNum++;
		
		ExchangeType exchngType = ExchangeType.FFM;
		String FUNC = "IC834";
		
		int cntINI = 0;
		int cntEFF = 0;

		BenefitEnrollmentRequest berINI = null;
		BenefitEnrollmentRequest berEFF = null;
		BenefitEnrollmentMaintenanceType bemINI = null;
		BenefitEnrollmentMaintenanceType bemEFF = null;
		MemberType memberINI = null;
		MemberType memberEFF = null;
		String fileNmPrefINI = exchngType.getValue() + "INI." + FUNC + ".";
		String fileNmINI = "";
		String fileNmPrefEFF = exchngType.getValue() + "EFF." + FUNC + ".";
		String fileNmEFF = "";
		DateTime currentTimestampINI = null;
		DateTime currentTimestampEFF = null;
		String textINI = null;
		String textEFF = null;

		int berInt = 0;
		Long berId = null;
		Long bemId = null;
		Long memId = null;

		Long bemLen = null;

		int versionNum = 1;
		String subscriberId = null;
		String hcGPN = null;
		String hiosId = null;
		String groupSenderId = null;


		String[] names = {"DAD", "MOM", "SON1", "DAU1", "SON2", "DAU2", "DEP1", "DEP2", "BBY1", "BBY2"};
		String[] states = {"NC", "ND", "OH", "PA", "GA", "RI", "TN", "CT", "VA", "AL",
				"WV", "MD", "TX", "NH", "MS", "FL", "CA", "NV", "NM", "MA"};
		String state = null;

		System.out.println("Creating " + berLen + " BER INITIAL and EFFECTUATION files each. ");

		for(int i = 0; i < berLen; ++i) {

			state = states[i % states.length];
			bemLen = Long.valueOf(bemCount);
			hiosId = String.format("%05d", berInt);
			groupSenderId = hiosId + state + "0" + hiosId.substring(0, 4) + "01";
			currentTimestampINI = new DateTime();
			if (isEFF) {
				// sleep so CurrentTimestamp is different.
				try {
					Thread.sleep(SLEEP_INTERVAL);
				} catch (Exception ex) {
					System.out.println("EX: " + ex.getMessage());
				}
				currentTimestampEFF = new DateTime();
			}

			berINI = new BenefitEnrollmentRequest();
			berINI.setFileInformation(makeFileInformationType(berId, exchngType, groupSenderId));

			Calendar cal = Calendar.getInstance();
			fileNmINI = fileNmPrefINI + sdf.format(cal.getTime()) + ".T";

			if (isEFF) {
				berEFF = new BenefitEnrollmentRequest();
				berEFF.setFileInformation(makeFileInformationType(berId, exchngType, groupSenderId));
				cal.add(Calendar.SECOND, 1);
				cal.add(Calendar.MILLISECOND, 5);
				fileNmEFF = fileNmPrefEFF + sdf.format(cal.getTime()) + ".T";
			}

			for(int j = 0; j < bemLen; ++j) {

				bemId = getRandomNumber(8); // ExchangePolicyId gets generated from this.
				memId = bemId;
				versionNum = 1;
				String bemIdstr = bemId.toString();
				hcGPN = "000000" + bemIdstr;
				BigDecimal tpa = new BigDecimal(getRandomNumber(3));

				bemINI = makeBenefitEnrollmentMaintenanceType(bemId, String.format("%05d", versionNum), currentTimestampINI, hiosId, hcGPN, PolicyStatus.INITIAL_1);
				berINI.getBenefitEnrollmentMaintenance().add(bemINI);

				if (isEFF) {
					versionNum++;
					bemEFF = makeBenefitEnrollmentMaintenanceType(bemId, String.format("%05d", versionNum), currentTimestampEFF, hiosId, hcGPN, PolicyStatus.EFFECTUATED_2);
					berEFF.getBenefitEnrollmentMaintenance().add(bemEFF);
				}

				Long memLen = memberCount != null ? memberCount : getRandomNumber(1); // number of Members per BEM will be between 1 and 9.

				for(int k = 0; k < memLen; ++k) {
					if (k == 0) {
						subscriberId = "DAD-" + memId;
					}
					memId = memId + k;					

					if(k == 0) {
						memberINI = makeMemberType(memId, names[k], true, subscriberId, hcGPN, groupSenderId, tpa);
						memberEFF = makeMemberType(memId, names[k], true, subscriberId, hcGPN, groupSenderId, tpa);	
						cntSub++;
					}  else {
						memberINI = makeMemberType(memId, names[k], false, subscriberId, hcGPN, groupSenderId, tpa);
						memberEFF = makeMemberType(memId, names[k], false, subscriberId, hcGPN, groupSenderId, tpa);
						cntNon++;
					}
					bemINI.getMember().add(memberINI);
					if (isEFF) {
						bemEFF.getMember().add(memberEFF);
					}
				}
				cntINI++;
				if (isEFF) {
					cntEFF++;
				}
				cntBEM++;
			}


			textINI = getBerXMLAsString(berINI);
			// XSD validator does not like these attributes for JAXB<String> elements like 'EmailID', 'BeeperNum', so they are stripped out.
			textINI = textINI.replaceAll(" xmlns=\"\" xmlns:ns2=\"http://bem.dsh.cms.gov\">", ">");

			if (isEFF) {
				textEFF = getBerXMLAsString(berEFF);
				textEFF = textEFF.replaceAll(" xmlns=\"\" xmlns:ns2=\"http://bem.dsh.cms.gov\">", ">");
			}

			try {

				if (!manifestDir.exists()) {
					manifestDir.mkdirs();
				}

				File inputManifestDir = new File(inputDir.getCanonicalPath() + File.separator + manifestNum);
				if (!inputManifestDir.exists()) {
					inputManifestDir.mkdirs();
				}

				System.out.println("MANIFEST: " + inputDir.getCanonicalPath() + File.separator + manifestNum + File.separator + fileNmINI);
				File fileINI = new File(inputDir.getCanonicalPath() + File.separator + manifestNum + File.separator + fileNmINI);
				BufferedWriter outputINI = new BufferedWriter(new FileWriter(fileINI));
				outputINI.write(textINI);
				outputINI.close();

				if (isEFF) {
					File fileEFF = new File(inputDir.getCanonicalPath() + File.separator + manifestNum + File.separator + fileNmEFF);
					BufferedWriter outputEFF = new BufferedWriter(new FileWriter(fileEFF));
					outputEFF.write(textEFF);
					outputEFF.close();
				}

			} catch ( IOException e ) {
				e.printStackTrace();
			}
			berInt++;
		}

		try {
			writeManifestFile(manifestDir, manifestNum, (cntINI + cntEFF), paetCompletion);
		}  catch ( IOException e ) {
			e.printStackTrace();
		}

		return String.valueOf(manifestNum);
	}

	/**
	 * Returns the number of BEMs that were created from makeFiles().
	 * @return
	 */
	public int getExcectedBemCount() {

		return (isEFF) ? cntBEM * 2 : cntBEM;
	}

	/**
	 * Returns the total number of member versions created from makeFiles()
	 * @return
	 */
	public int getExpectedMemberVersionCount() {

		int cntMem = (cntSub + cntNon + cntDep);
		return (isEFF) ? cntMem * 2 : cntMem;
	}


	public void writeManifestFile(File manifestDir, int manifestNum,  int recordCount, String paet) throws IOException {

		String manifestFileNm = "Manifest-" + manifestNum + ".txt";
		File manifestFile = new File(manifestDir + File.separator + manifestFileNm);
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
		writer.println(EPSConstants.PAETCOMPLETION + "=" + paet);
		writer.write("JobStatus=SUCCESS");

		writer.flush();
		writer.close();
	}

}
