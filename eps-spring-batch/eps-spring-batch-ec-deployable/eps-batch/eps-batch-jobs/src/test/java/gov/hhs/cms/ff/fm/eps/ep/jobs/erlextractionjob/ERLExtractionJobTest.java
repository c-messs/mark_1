package gov.hhs.cms.ff.fm.eps.ep.jobs.erlextractionjob;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import gov.cms.dsh.bem.BenefitEnrollmentMaintenanceType;
import gov.cms.dsh.bem.BenefitEnrollmentRequest;
import gov.cms.dsh.bem.BooleanIndicatorSimpleType;
import gov.cms.dsh.bem.ExchangeCodeSimpleType;
import gov.cms.dsh.bem.GenderCodeSimpleType;
import gov.cms.dsh.bem.InsuranceLineCodeSimpleType;
import gov.cms.dsh.bem.MemberType;
import gov.cms.dsh.bem.PolicyInfoType;
import gov.hhs.cms.base.batch.corb.EncryptedManager;
import gov.hhs.cms.ff.fm.eps.ep.util.EpsDateUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * @author shasidar.pabolu
 *
 */
@ContextConfiguration(locations={"classpath:/test-batch-application-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class ERLExtractionJobTest {

	private static final Logger LOG = LoggerFactory.getLogger(ERLExtractionJobTest.class);

	private static final String ROOT_FILE_PATH = "./UnitTestDirs/erl";
	private static final String TEST_PATH_PROPS = "./UnitTestDirs/erl/props";
	private static final String TEST_PATH_MANIFEST = "./UnitTestDirs/erl/manifest";
	private static final String TEST_PATH_EXTRACTED = "./UnitTestDirs/erl/extracted";

	private static final String HWM = "2050-01-01T00:00:00.000000-05:00";
	private static final String PAET = "2051-01-01T00:00:00-05:00";

	@Value("${XCC-CONNECTION-URI}")
	private String xccConnectionUri;

	private File propDir;	
	private File manifestDir;
	private File extractedDataDir;


	protected final DateTime DATETIME = new DateTime();

	protected final int YEAR = 2050;
	protected final DateTime JUN_1 = new DateTime(YEAR, 6, 1, 0, 0);
	protected final DateTime DEC_31 = new DateTime(YEAR, 12, 31, 0, 0);
	protected final DateTime JAN_1_2011 = new DateTime(2011, 1, 1, 0, 0);
	protected final DateTime JAN_1_1970 = new DateTime(1970, 1, 1, 0, 0);
	protected final DateTime MAY_12_2014 = new DateTime(2014, 5, 12, 0, 0);
	protected final DateTime JAN_1_1965 = new DateTime(1965, 1, 1, 0, 0);
	protected final DateTime JUN_1_2050 = new DateTime(2050, 6, 1, 0, 0);
	protected final DateTime DEC_31_2050 = new DateTime(2050, 12, 31, 0, 0);

	private static Unmarshaller unmarshaller;

	static {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(String.class, BenefitEnrollmentRequest.class);
			unmarshaller = jaxbContext.createUnmarshaller();

		} catch (JAXBException ex) {
			LOG.error("Unable to create Unmarshaller for unit test.", ex);
		}
	}


	@Before
	public void setUp() throws Exception {
		new File(ROOT_FILE_PATH);
		propDir = new File(TEST_PATH_PROPS);
		propDir.mkdirs();
		manifestDir = new File(TEST_PATH_MANIFEST);
		manifestDir.mkdirs();
		extractedDataDir = new File(TEST_PATH_EXTRACTED);
		extractedDataDir.mkdirs();

		System.out.println("\n\nmanifestDir.getCanonicalPath(): " + manifestDir.getCanonicalPath());

	}
	/**
	 * @throws Exception
	 */
	@After
	public void tearDown() throws Exception {

		FileUtils.deleteQuietly(new File(ROOT_FILE_PATH));

	}

	private String createCorbProperties(String jobId, String hwmDate, String PAETDate, String PAETCompletion) throws IOException
	{

		Properties props = new Properties();
		props.setProperty("XCC-CONNECTION-URI", xccConnectionUri);
		props.setProperty("INSTALL", "false");
		props.setProperty("MODULES-DATABASE", "FFE-EE-Maint-Modules");
		props.setProperty("MODULE-ROOT", "/eps-corb-xquery-xqy/EPS-Corb/scripts/");
		props.setProperty("THREAD-COUNT", "4");
		props.setProperty("URIS-MODULE", "uris.xqy");
		props.setProperty("URIS-MODULE.numInBatch", "2000");
		props.setProperty("URIS-MODULE.planPolicyYearFilter", "2016");
		props.setProperty("XQUERY-MODULE", "transform.xqy");
		props.setProperty("PROCESS-TASK","gov.hhs.cms.ff.fm.eps.ep.jobs.erlextractionjob.util.CustomExportToFileTask");
		props.setProperty("URIS-MODULE.manifestFilename=", manifestDir.getCanonicalPath() + "/" + "Manifest-" + jobId+ ".txt");
		props.setProperty("URIS-MODULE.highwatermarkFilename", "highwatermark.properties");
		props.setProperty("URIS-MODULE.highwaterMark", hwmDate);
		props.setProperty("URIS-MODULE.bemsPerFile", "1000");
		props.setProperty("XQUERY-MODULE.highwaterMark", hwmDate);
		props.setProperty("URIS-MODULE.jobId", jobId);
		props.setProperty("XQUERY-MODULE.jobId", jobId);
		props.setProperty("URIS-MODULE.PAET", PAETDate);
		props.setProperty("URIS-MODULE.PAETCompletion", PAETCompletion);
		props.setProperty("XQUERY-MODULE.outputBerPath", extractedDataDir.getCanonicalPath() + "/" + jobId);
		props.setProperty("EXPORT-FILE-DIR", extractedDataDir.getCanonicalPath() + "/" + jobId);

		File corbPropertiesFile = new File(propDir, "corb.properties" + "." + jobId);

		corbPropertiesFile.createNewFile();
		OutputStream out = new FileOutputStream(corbPropertiesFile);
		props.store(out, "This is an optional header comment string");
		out.flush();
		out.close();

		return corbPropertiesFile.getCanonicalPath();
	}


	/**
	 * Test case to test before PAET extraction scenario.
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InterruptedException
	 * @throws JAXBException 
	 */
	@Test
	public void testExtraction() throws FileNotFoundException, IOException, URISyntaxException, 
	ClassNotFoundException, InstantiationException, IllegalAccessException, InterruptedException, JAXBException {

		String jobId = "111";

		String corbPropertiesPath = createCorbProperties(jobId, HWM , PAET,"N");
		System.setProperty("OPTIONS-FILE",  corbPropertiesPath);
		EncryptedManager.main(new String[] {});


		if (manifestDir.exists()) {
			System.out.println("Manifest directory created:");
			String[] manifestFileList = manifestDir.list();
			if (manifestFileList != null && manifestFileList.length > 0) {
				for (String fileNm : manifestFileList) {
					System.out.println(" - " + fileNm);
				}
			} else {
				System.out.println("Manifest directory contains NO files!");
			}
		} else {
			System.out.println("Manifest directory NOT created for this test!");
		}

		if (extractedDataDir.exists()) {
			System.out.println("Input directory created:");
			for (String fileNm : extractedDataDir.list()) {
				System.out.println(" - " + fileNm);
			}
		} else {
			System.out.println("Extracted directory NOT created for this test!");
		}

		File berFile = null;

		int expectedInputFolderLength = 1;

		assertEquals("Correct number 'input' manifest directories in " + extractedDataDir.getName() + ".", expectedInputFolderLength, extractedDataDir.list().length);

		File[] files = new File(TEST_PATH_EXTRACTED + "/"+ jobId).listFiles();

		for (File file : files) {
			if (file.isFile()) {
				if (file.getName().indexOf("FFM834") != -1) {
					berFile = file;
					break;
				}
			}
		}

		File newHWMFile = new File(TEST_PATH_EXTRACTED + "/" + jobId+ "/newHighwaterMark");
		FileReader fileReader =  new FileReader(newHWMFile);
		BufferedReader bufferedReader = new BufferedReader(fileReader);

		String expectedString = HWM.substring(0,25)+'1'+HWM.substring(26); 
		String expectedPaet = "PAETCompletion=N";
		int count = 0;

		String line = bufferedReader.readLine();

		while (line != null) {
			if(count == 0){
				assertNotSame("Read First Line : ", HWM, line);
				assertEquals("Read First Line : ", "EndHighWaterMark=" + expectedString, line);
			}
			if(count == 1){
				assertEquals("Read Second Line : ", expectedPaet, line);
			}
			line = bufferedReader.readLine();
			count++;
		}
		bufferedReader.close();  

		File newHWMProp = new File(TEST_PATH_EXTRACTED + "/" + jobId+ "/newHighwaterMark-highwatermark.properties");
		FileReader fileReaderTwo =  new FileReader(newHWMProp);
		BufferedReader bufferedReaderTwo = new BufferedReader(fileReaderTwo);

		List<String> fileResults = new ArrayList<String>();
		String hwmLine = bufferedReaderTwo.readLine();

		while(hwmLine != null){
			fileResults.add(hwmLine);
			hwmLine = bufferedReaderTwo.readLine();
		}

		bufferedReaderTwo.close();  

		assertEquals("export highwaterMark", "export highwaterMark=2050-01-01T00:00:00.000001-05:00", fileResults.get(0));
		assertEquals("export jobId", "export jobId=112", fileResults.get(1));
		assertEquals("export PAET", "export PAET=2051-01-01T00:00:00-05:00", fileResults.get(2));
		assertEquals("export PAETCompletion", "export PAETCompletion=N", fileResults.get(3));
		assertEquals("export totalIPPCountTobeExtracted", "export totalIPPCountTobeExtracted=0", fileResults.get(4));
		assertEquals("export pendingExtractCount", "export pendingExtractCount=0", fileResults.get(5));

		if (berFile != null) {

			System.out.println("BER FilePath: " + berFile.getCanonicalPath());
			BufferedReader br = new BufferedReader(new FileReader(berFile));
			try {
				int i = 1;
				String berLine = br.readLine();
				if (berLine !=  null) {
					System.out.println("BER File Line " + i + " length: " + berLine.length());
				}
				while (berLine != null) {
					i++;
					berLine = br.readLine();
					if (berLine !=  null) {
						System.out.println("BER File Line " + i + " length: " + berLine.length());
					}
				}
			} finally {
				br.close();
			}
		} else {
			System.out.println("BER File is null!");
		}

		StreamSource strmSrc = new StreamSource(berFile);

		JAXBElement<BenefitEnrollmentRequest> root = unmarshaller.unmarshal(strmSrc, BenefitEnrollmentRequest.class);
		BenefitEnrollmentRequest ber = root.getValue();  

		System.out.println("Successfully unmarshalled BER file.");

		List<BenefitEnrollmentMaintenanceType> bemList = ber.getBenefitEnrollmentMaintenance();
		
		BenefitEnrollmentMaintenanceType bem = bemList.get(0);
		
		PolicyInfoType policyInfo = bem.getPolicyInfo();
		
		assertEquals("GroupPolicyNumber", "583363", policyInfo.getGroupPolicyNumber());
		assertEquals("MarketplaceGroupPolicyIdentifier", "1231729381724", policyInfo.getMarketplaceGroupPolicyIdentifier());
		assertEquals("PolicyStartDate", JUN_1_2050, EpsDateUtils.getDateTimeFromXmlGC(policyInfo.getPolicyStartDate()));
		assertEquals("PolicyEndDate", DEC_31_2050, EpsDateUtils.getDateTimeFromXmlGC(policyInfo.getPolicyEndDate()));
		assertEquals("PolicyStatus", "5", policyInfo.getPolicyStatus());
		

		MemberType mem1 = bem.getMember().get(0);

		BooleanIndicatorSimpleType mem1SubscriberInd = mem1.getMemberInformation().getSubscriberIndicator();
		String mem1SubscriberID = mem1.getSubscriberID();
		String mem1ExAssignedMemID = mem1.getMemberAdditionalIdentifier().getExchangeAssignedMemberID();
		String mem1IssuerAssignedMemID = mem1.getMemberAdditionalIdentifier().getIssuerAssignedMemberID();
		XMLGregorianCalendar mem1EligibilityBeginDate = mem1.getMemberRelatedDates().getEligibilityBeginDate();
		XMLGregorianCalendar mem1EligibilityEndDate = mem1.getMemberRelatedDates().getEligibilityEndDate();

		String mem1FirstName = mem1.getMemberNameInformation().getMemberName().getFirstName();
		String mem1LastName = mem1.getMemberNameInformation().getMemberName().getLastName();
		String mem1MiddleName = mem1.getMemberNameInformation().getMemberName().getMiddleName();
		String mem1SocialSecNum = mem1.getMemberNameInformation().getMemberName().getSocialSecurityNumber();

		String mem1AddressStateCode = mem1.getMemberNameInformation().getMemberResidenceAddress().getStateCode();
		String mem1AddressPostalCode = mem1.getMemberNameInformation().getMemberResidenceAddress().getPostalCode();

		XMLGregorianCalendar mem1BirthDate = mem1.getMemberNameInformation().getMemberDemographics().getBirthDate();
		GenderCodeSimpleType mem1GenderCode = mem1.getMemberNameInformation().getMemberDemographics().getGenderCode();

		InsuranceLineCodeSimpleType mem1InsuranceLineCode = mem1.getHealthCoverage().get(0).getHealthCoverageInformation().getInsuranceLineCode();
		XMLGregorianCalendar mem1BenefitBeginDate = mem1.getHealthCoverage().get(0).getHealthCoverageDates().getBenefitBeginDate();
		XMLGregorianCalendar mem1BenefitEndDate = mem1.getHealthCoverage().get(0).getHealthCoverageDates().getBenefitEndDate();
		String mem1ContractCode = mem1.getHealthCoverage().get(0).getHealthCoveragePolicyNumber().getContractCode();

		BigDecimal mem1APTCAmount = mem1.getAdditionalInfo().get(0).getAPTCAmount();
		XMLGregorianCalendar mem1APTCStartDate = mem1.getAdditionalInfo().get(0).getEffectiveStartDate();
		XMLGregorianCalendar mem1APTCEndDate = mem1.getAdditionalInfo().get(0).getEffectiveEndDate();

		BigDecimal mem1CSRAmount = mem1.getAdditionalInfo().get(0).getCSRAmount();
		XMLGregorianCalendar mem1CSRStartDate = mem1.getAdditionalInfo().get(0).getEffectiveStartDate();
		XMLGregorianCalendar mem1CSREndDate = mem1.getAdditionalInfo().get(0).getEffectiveEndDate();

		BigDecimal mem1TotalPremAmount = mem1.getAdditionalInfo().get(0).getTotalPremiumAmount();
		XMLGregorianCalendar mem1TotalPremStartDate = mem1.getAdditionalInfo().get(0).getEffectiveStartDate();
		XMLGregorianCalendar mem1TotalPremEndDate = mem1.getAdditionalInfo().get(0).getEffectiveEndDate();

		BigDecimal mem1TotalIndAmount = mem1.getAdditionalInfo().get(0).getTotalIndividualResponsibilityAmount();
		XMLGregorianCalendar mem1TotalAmtStartDate = mem1.getAdditionalInfo().get(0).getEffectiveStartDate();
		XMLGregorianCalendar mem1TotalAmtEndDate = mem1.getAdditionalInfo().get(0).getEffectiveEndDate();

		String mem1RatingArea = mem1.getAdditionalInfo().get(0).getRatingArea();
		XMLGregorianCalendar mem1RatingAreaStartDate = mem1.getAdditionalInfo().get(0).getEffectiveStartDate();
		XMLGregorianCalendar mem1RatingAreaEndDate = mem1.getAdditionalInfo().get(0).getEffectiveEndDate();

		DateTime timeStamp = new DateTime(2050, 1, 1, 0, 0, 1);

		assertEquals("NumberOfBEMS", 1, bemList.size());
		assertEquals("NumberOfMembers", 4, bem.getMember().size());

		System.out.println("NumberOfBEMS: " + bemList.size());
		System.out.println("NumberOfMembers: " + bem.getMember().size());

		assertEquals("GroupSenderID", "76168DE0420001", ber.getFileInformation().getGroupSenderID()); 
		assertEquals("GroupReceiverID", "DE0", ber.getFileInformation().getGroupReceiverID());
		assertEquals("ControlNumber", "TO BE SET", bem.getTransactionInformation().getControlNumber());
		assertEquals("CurrentTimeStamp", timeStamp, EpsDateUtils.getDateTimeFromXmlGC(bem.getTransactionInformation().getCurrentTimeStamp()));
		assertEquals("ExchangeCode", ExchangeCodeSimpleType.INDIVIDUAL, bem.getTransactionInformation().getExchangeCode());
		assertEquals("PolicySnapshotVersionNumber", "2", bem.getTransactionInformation().getPolicySnapshotVersionNumber());
		assertEquals("PolicySnapshotDateTime", "2050-01-01T00:00:00.000001-05:00" ,bem.getTransactionInformation().getPolicySnapshotDateTime().toString());
		assertEquals("IssuerName", "Highmark BCBSD Inc.", bem.getIssuer().getName());
		assertEquals("IssuerTaxPayerIdentificationNumber", "051002045", bem.getIssuer().getTaxPayerIdentificationNumber());

		assertEquals("MemberOneSubscriberIndicator", BooleanIndicatorSimpleType.Y, mem1SubscriberInd);
		assertEquals("MemberOneSubscriberID", "0001101701", mem1SubscriberID);
		assertNull("MemberOneIssuerAssignedMemberId",mem1IssuerAssignedMemID);
		assertEquals("MemberOneExchangeAssignedMemberID", "0001101701", mem1ExAssignedMemID);
		assertEquals("MemberOneEligibilityBeginDate", JUN_1, EpsDateUtils.getDateTimeFromXmlGC(mem1EligibilityBeginDate));
		assertEquals("MemberOneEligibilityEndDate", DEC_31, EpsDateUtils.getDateTimeFromXmlGC(mem1EligibilityEndDate));

		assertEquals("MemberOneFirstName", "FirstPerson", mem1FirstName);
		assertEquals("MemberOneLastName", "Jones", mem1LastName);
		assertEquals("MemberOneMiddleName", "M", mem1MiddleName);
		assertEquals("MemberOneSocialSecurity", "006121001", mem1SocialSecNum);

		assertEquals("MemberOneAddressStateCode", "DE", mem1AddressStateCode);
		assertEquals("MemberOneAddressPostalCode", "19952", mem1AddressPostalCode);

		assertEquals("MemberOneBirthDate", JAN_1_1965,EpsDateUtils.getDateTimeFromXmlGC(mem1BirthDate));
		assertEquals("MemberOneGenderCode", GenderCodeSimpleType.M, mem1GenderCode);

		assertEquals("MemberOneInsuranceLineCode", InsuranceLineCodeSimpleType.HLT, mem1InsuranceLineCode);
		assertEquals("MemberOneBenefitBeginDate", JUN_1, EpsDateUtils.getDateTimeFromXmlGC(mem1BenefitBeginDate));
		assertEquals("MemberOneBenefitEndDate", DEC_31, EpsDateUtils.getDateTimeFromXmlGC(mem1BenefitEndDate));
		assertEquals("MemberOneContractCode", "76168DE042000101", mem1ContractCode);

		assertEquals("MemberOneAPTCAmount", new BigDecimal("0.00"), mem1APTCAmount);
		assertEquals("MemberOneAPTCAmountStartDate", JUN_1, EpsDateUtils.getDateTimeFromXmlGC(mem1APTCStartDate));
		assertEquals("MemberOneAPTCAmountEndDate", DEC_31, EpsDateUtils.getDateTimeFromXmlGC(mem1APTCEndDate));

		assertEquals("MemberOneCSRAmount", null, mem1CSRAmount);
		assertEquals("MemberOneCSRAmountStartDate", JUN_1, EpsDateUtils.getDateTimeFromXmlGC(mem1CSRStartDate));
		assertEquals("MemberOneCSRAmountEndDate", DEC_31, EpsDateUtils.getDateTimeFromXmlGC(mem1CSREndDate));

		assertEquals("MemberOneTotalPremiumAmount", new BigDecimal("841.41"), mem1TotalPremAmount);
		assertEquals("MemberOneTotalPremiumAmountStartDate", JUN_1, EpsDateUtils.getDateTimeFromXmlGC(mem1TotalPremStartDate));
		assertEquals("MemberOneTotalPremiumAmountEndDate", DEC_31, EpsDateUtils.getDateTimeFromXmlGC(mem1TotalPremEndDate));

		assertEquals("MemberOneTotalIndividualResponsibilityAmount", new BigDecimal("841.41"), mem1TotalIndAmount);
		assertEquals("MemberOneTotalIndividualResponsibilityAmountStartDate", JUN_1, EpsDateUtils.getDateTimeFromXmlGC(mem1TotalAmtStartDate));
		assertEquals("MemberOneTotalIndividualResponsibilityAmountEndDate", DEC_31, EpsDateUtils.getDateTimeFromXmlGC(mem1TotalAmtEndDate));

		assertEquals("MemberOneRatingArea", "R-DE001", mem1RatingArea);
		assertEquals("MemberOneRatingAreaStartDate", JUN_1, EpsDateUtils.getDateTimeFromXmlGC(mem1RatingAreaStartDate));
		assertEquals("MemberOneRatingAreaEndDate", DEC_31, EpsDateUtils.getDateTimeFromXmlGC(mem1RatingAreaEndDate));


		MemberType mem2 = bem.getMember().get(1);

		BooleanIndicatorSimpleType mem2SubscriberInd = mem2.getMemberInformation().getSubscriberIndicator();
		String mem2SubscriberID = mem2.getSubscriberID();
		String mem2ExAssignedMemID = mem2.getMemberAdditionalIdentifier().getExchangeAssignedMemberID();
		String mem2IssuerAssignedMemID = mem2.getMemberAdditionalIdentifier().getIssuerAssignedMemberID();
		
		XMLGregorianCalendar mem2EligibilityBeginDate = mem2.getMemberRelatedDates().getEligibilityBeginDate();
		XMLGregorianCalendar mem2EligibilityEndDate = mem2.getMemberRelatedDates().getEligibilityEndDate();

		String mem2FirstName = mem2.getMemberNameInformation().getMemberName().getFirstName();
		String mem2LastName = mem2.getMemberNameInformation().getMemberName().getLastName();
		String mem2MiddleName = mem2.getMemberNameInformation().getMemberName().getMiddleName();
		String mem2SocialSecNum = mem2.getMemberNameInformation().getMemberName().getSocialSecurityNumber();

		String mem2AddressStateCode = mem2.getMemberNameInformation().getMemberResidenceAddress().getStateCode();
		String mem2AddressPostalCode = mem2.getMemberNameInformation().getMemberResidenceAddress().getPostalCode();

		XMLGregorianCalendar mem2BirthDate = mem2.getMemberNameInformation().getMemberDemographics().getBirthDate();
		GenderCodeSimpleType mem2GenderCode = mem2.getMemberNameInformation().getMemberDemographics().getGenderCode();

		InsuranceLineCodeSimpleType mem2InsuranceLineCode = mem2.getHealthCoverage().get(0).getHealthCoverageInformation().getInsuranceLineCode();
		XMLGregorianCalendar mem2BenefitBeginDate = mem2.getHealthCoverage().get(0).getHealthCoverageDates().getBenefitBeginDate();
		XMLGregorianCalendar mem2BenefitEndDate = mem2.getHealthCoverage().get(0).getHealthCoverageDates().getBenefitEndDate();
		String mem2ContractCode = mem2.getHealthCoverage().get(0).getHealthCoveragePolicyNumber().getContractCode();


		assertEquals("MemberTwoSubscriberIndicator", BooleanIndicatorSimpleType.N, mem2SubscriberInd);
		assertEquals("MemberTwoSubscriberID", "0001101701", mem2SubscriberID);
		assertEquals("MemberTwoIssuerAssignedMemberId", "0000051191", mem2IssuerAssignedMemID);
		assertEquals("MemberTwoExchangeAssignedMemberID", "0000051191", mem2ExAssignedMemID);
		assertEquals("MemberTwoEligibilityBeginDate", JUN_1, EpsDateUtils.getDateTimeFromXmlGC(mem2EligibilityBeginDate));
		assertEquals("MemberTwoEligibilityEndDate", DEC_31, EpsDateUtils.getDateTimeFromXmlGC(mem2EligibilityEndDate));

		assertEquals("MemberTwoFirstName", "FourthPerson", mem2FirstName);
		assertEquals("MemberTwoLastName", "Jones", mem2LastName);
		assertNull("MemberTwoMiddleName", mem2MiddleName);
		assertEquals("MemberTwoSocialSecurity", "012143403", mem2SocialSecNum);

		assertEquals("MemberTwoAddressStateCode", "DE", mem2AddressStateCode);
		assertEquals("MemberTwoAddressPostalCode", "19952", mem2AddressPostalCode);

		assertEquals("MemberTwoBirthDate", MAY_12_2014, EpsDateUtils.getDateTimeFromXmlGC(mem2BirthDate));
		assertEquals("MemberTwoGenderCode", GenderCodeSimpleType.M, mem2GenderCode);

		assertEquals("MemberTwoInsuranceLineCode", InsuranceLineCodeSimpleType.HLT, mem2InsuranceLineCode);
		assertEquals("MemberTwoBenefitBeginDate", JUN_1, EpsDateUtils.getDateTimeFromXmlGC(mem2BenefitBeginDate));
		assertEquals("MemberTwoBenefitEndDate", DEC_31, EpsDateUtils.getDateTimeFromXmlGC(mem2BenefitEndDate));
		assertEquals("MemberTwoContractCode", "76168DE042000101", mem2ContractCode);


		MemberType mem3 = bem.getMember().get(2);

		BooleanIndicatorSimpleType mem3SubscriberInd = mem3.getMemberInformation().getSubscriberIndicator();
		String mem3SubscriberID = mem3.getSubscriberID();
		String mem3ExAssignedMemID = mem3.getMemberAdditionalIdentifier().getExchangeAssignedMemberID();
		String mem3IssuerAssignedMemID = mem3.getMemberAdditionalIdentifier().getIssuerAssignedMemberID();
		XMLGregorianCalendar mem3EligibilityBeginDate = mem3.getMemberRelatedDates().getEligibilityBeginDate();
		XMLGregorianCalendar mem3EligibilityEndDate = mem3.getMemberRelatedDates().getEligibilityEndDate();

		String mem3FirstName = mem3.getMemberNameInformation().getMemberName().getFirstName();
		String mem3LastName = mem3.getMemberNameInformation().getMemberName().getLastName();
		String mem3MiddleName = mem3.getMemberNameInformation().getMemberName().getMiddleName();
		String mem3SocialSecNum = mem3.getMemberNameInformation().getMemberName().getSocialSecurityNumber();

		String mem3AddressStateCode = mem3.getMemberNameInformation().getMemberResidenceAddress().getStateCode();
		String mem3AddressPostalCode = mem3.getMemberNameInformation().getMemberResidenceAddress().getPostalCode();

		XMLGregorianCalendar mem3BirthDate = mem3.getMemberNameInformation().getMemberDemographics().getBirthDate();
		GenderCodeSimpleType mem3GenderCode = mem3.getMemberNameInformation().getMemberDemographics().getGenderCode();

		InsuranceLineCodeSimpleType mem3InsuranceLineCode = mem3.getHealthCoverage().get(0).getHealthCoverageInformation().getInsuranceLineCode();
		XMLGregorianCalendar mem3BenefitBeginDate = mem3.getHealthCoverage().get(0).getHealthCoverageDates().getBenefitBeginDate();
		XMLGregorianCalendar mem3BenefitEndDate = mem3.getHealthCoverage().get(0).getHealthCoverageDates().getBenefitEndDate();
		String mem3ContractCode = mem3.getHealthCoverage().get(0).getHealthCoveragePolicyNumber().getContractCode();
	

		assertEquals("MemberThreeSubscriberIndicator", BooleanIndicatorSimpleType.N, mem3SubscriberInd);
		assertEquals("MemberThreeIssuerAssignedMemberId", "0000792533", mem3IssuerAssignedMemID);
		assertEquals("MemberThreeSubscriberID", "0001101701", mem3SubscriberID);
		assertEquals("MemberThreeExchangeAssignedMemberID", "0000792533", mem3ExAssignedMemID);
		assertEquals("MemberThreeEligibilityBeginDate", JUN_1, EpsDateUtils.getDateTimeFromXmlGC(mem3EligibilityBeginDate));
		assertEquals("MemberThreeEligibilityEndDate", DEC_31, EpsDateUtils.getDateTimeFromXmlGC(mem3EligibilityEndDate));

		assertEquals("MemberThreeFirstName", "ThirdPerson", mem3FirstName);
		assertEquals("MemberThreeLastName", "Jones", mem3LastName);
		assertNull("MemberThreeMiddleName", mem3MiddleName);
		assertEquals("MemberThreeSocialSecurity", "073788006", mem3SocialSecNum);

		assertEquals("MemberThreeAddressStateCode", "DE", mem3AddressStateCode);
		assertEquals("MemberThreeAddressPostalCode", "19952", mem3AddressPostalCode);

		assertEquals("MemberThreeBirthDate", JAN_1_2011, EpsDateUtils.getDateTimeFromXmlGC(mem3BirthDate));
		assertEquals("MemberThreeGenderCode", GenderCodeSimpleType.M, mem3GenderCode);

		assertEquals("MemberThreeInsuranceLineCode", InsuranceLineCodeSimpleType.HLT, mem3InsuranceLineCode);
		assertEquals("MemberThreeBenefitBeginDate", JUN_1, EpsDateUtils.getDateTimeFromXmlGC(mem3BenefitBeginDate));
		assertEquals("MemberThreeBenefitEndDate", DEC_31, EpsDateUtils.getDateTimeFromXmlGC(mem3BenefitEndDate));
		assertEquals("MemberThreeContractCode", "76168DE042000101", mem3ContractCode);


		MemberType mem4 = bem.getMember().get(3);

		BooleanIndicatorSimpleType mem4SubscriberInd = mem4.getMemberInformation().getSubscriberIndicator();
		String mem4SubscriberID = mem4.getSubscriberID();
		String mem4ExAssignedMemID = mem4.getMemberAdditionalIdentifier().getExchangeAssignedMemberID();
		String mem4IssuerAssignedMemID = mem3.getMemberAdditionalIdentifier().getIssuerAssignedMemberID();

		XMLGregorianCalendar mem4EligibilityBeginDate = mem4.getMemberRelatedDates().getEligibilityBeginDate();
		XMLGregorianCalendar mem4EligibilityEndDate = mem4.getMemberRelatedDates().getEligibilityEndDate();

		String mem4FirstName = mem4.getMemberNameInformation().getMemberName().getFirstName();
		String mem4LastName = mem4.getMemberNameInformation().getMemberName().getLastName();
		String mem4MiddleName = mem4.getMemberNameInformation().getMemberName().getMiddleName();
		String mem4SocialSecNum = mem4.getMemberNameInformation().getMemberName().getSocialSecurityNumber();

		String mem4AddressStateCode = mem4.getMemberNameInformation().getMemberResidenceAddress().getStateCode();
		String mem4AddressPostalCode = mem4.getMemberNameInformation().getMemberResidenceAddress().getPostalCode();

		XMLGregorianCalendar mem4BirthDate = mem4.getMemberNameInformation().getMemberDemographics().getBirthDate();
		GenderCodeSimpleType mem4GenderCode = mem4.getMemberNameInformation().getMemberDemographics().getGenderCode();

		InsuranceLineCodeSimpleType mem4InsuranceLineCode = mem4.getHealthCoverage().get(0).getHealthCoverageInformation().getInsuranceLineCode();
		XMLGregorianCalendar mem4BenefitBeginDate = mem4.getHealthCoverage().get(0).getHealthCoverageDates().getBenefitBeginDate();
		XMLGregorianCalendar mem4BenefitEndDate = mem4.getHealthCoverage().get(0).getHealthCoverageDates().getBenefitEndDate();
		String mem4ContractCode = mem4.getHealthCoverage().get(0).getHealthCoveragePolicyNumber().getContractCode();


		assertEquals("MemberFourSubscriberIndicator", BooleanIndicatorSimpleType.N, mem4SubscriberInd);
		assertEquals("MemberFourSubscriberID", "0001101701", mem4SubscriberID);
		assertEquals("MemberFourIssuerAssignedMemID", "0000792533", mem4IssuerAssignedMemID);
		assertEquals("MemberFourExchangeAssignedMemberID", "0000793948", mem4ExAssignedMemID);
		assertEquals("MemberFourEligibilityBeginDate", JUN_1, EpsDateUtils.getDateTimeFromXmlGC(mem4EligibilityBeginDate));
		assertEquals("MemberFourEligibilityEndDate", DEC_31, EpsDateUtils.getDateTimeFromXmlGC(mem4EligibilityEndDate));

		assertEquals("MemberFourFirstName", "SecondPerson", mem4FirstName);
		assertEquals("MemberFourLastName", "Jones", mem4LastName);
		assertNull("MemberFourMiddleName", mem4MiddleName);
		assertEquals("MemberFourSocialSecurity", "002881403", mem4SocialSecNum);

		assertEquals("MemberFourAddressStateCode", "DE", mem4AddressStateCode);
		assertEquals("MemberFourAddressPostalCode", "19952", mem4AddressPostalCode);

		assertEquals("MemberFourBirthDate", JAN_1_1970, EpsDateUtils.getDateTimeFromXmlGC(mem4BirthDate));
		assertEquals("MemberFourGenderCode", GenderCodeSimpleType.F, mem4GenderCode);

		assertEquals("MemberFourInsuranceLineCode", InsuranceLineCodeSimpleType.HLT, mem4InsuranceLineCode);
		assertEquals("MemberFourBenefitBeginDate", JUN_1, EpsDateUtils.getDateTimeFromXmlGC(mem4BenefitBeginDate));
		assertEquals("MemberFourBenefitEndDate", DEC_31, EpsDateUtils.getDateTimeFromXmlGC(mem4BenefitEndDate));
		assertEquals("MemberFourContractCode", "76168DE042000101", mem4ContractCode);


		System.out.println("All member assertions passed.");
	}


}
