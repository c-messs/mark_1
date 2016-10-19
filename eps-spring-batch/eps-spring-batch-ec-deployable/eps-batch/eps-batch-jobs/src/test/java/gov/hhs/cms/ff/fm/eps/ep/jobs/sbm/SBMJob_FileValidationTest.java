//package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm;
//
//import java.io.File;
//import java.io.IOException;
//import java.sql.SQLException;
//import java.util.Date;
//import java.util.LinkedHashMap;
//import java.util.Locale;
//import java.util.Map;
//
//import org.apache.commons.io.FileUtils;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.runner.RunWith;
//import org.springframework.batch.core.Job;
//import org.springframework.batch.core.JobParameter;
//import org.springframework.batch.core.launch.JobLauncher;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.format.datetime.DateFormatter;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//
//import gov.hhs.cms.ff.fm.eps.ep.enums.SBMErrorWarningCode;
//import gov.hhs.cms.ff.fm.eps.ep.jobs.util.SBMTestDataDBUtil;
//import junit.framework.TestCase;
//
///**
// * 
// * @author rajesh.talanki
// *
// */
////@ContextConfiguration(locations={"/sbmi-batch-context.xml"})
////@RunWith(SpringJUnit4ClassRunner.class)
//public class SBMJob_FileValidationTest extends TestCase {
//	
//	private static final String SQL_ERROR_COUNT = "select count(*) from SBMFILEERROR where SBMERRORWARNINGTYPECD = ?";
//	
//	private static final String eftFolderPath = "./src/test/resources/sbm/readerTest/eftFolder";
//	private static final String privateFolderPath = "./src/test/resources/sbm/readerTest/privateFolder";	
//	private static final String processedFolderPath = "./src/test/resources/sbm/readerTest/processedFolder";
//	private static final String invalidFolderPath = "./src/test/resources/sbm/readerTest/invalidFolder";
//	
//	private File eftFolder = new File(eftFolderPath);
//	private File privateFolder = new File(privateFolderPath);	
//	private File processedFolder = new File(processedFolderPath);
//	private File invalidFolder = new File(invalidFolderPath);	
//	
//
//	@Autowired
//	private JobLauncher jobLauncher;
//
//	@Autowired
//	private Job job;
//	
//	@Autowired
//	private JdbcTemplate jdbcTemplate;
//	
//	@Autowired
//	private SbmiFileIngestionReader fileIngestionReader;
//	
//	@Autowired
//	private SbmiFileIngestionWriter fileIngestionWriter;
//	
//	private final DateFormatter dateFormatter = new DateFormatter("yyyy-MM-dd HH:mm:ss.SSS");
//	private final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
//	
//	
//	//@Before
//	public void setUp() throws SQLException, IOException {
//		
//		SBMTestDataDBUtil.cleanupData(jdbcTemplate);
//		
//        params.put("processingType", new JobParameter("sbmi"));
//        params.put("timestamp", new JobParameter(dateFormatter.print(new Date(), Locale.US)));
//        
//        fileIngestionReader.setEftFolder(eftFolder);
//        fileIngestionReader.setPrivateFolder(privateFolder);
//        
//        fileIngestionWriter.setInvalidFolder(invalidFolder);
//        fileIngestionWriter.setProcessedFolder(processedFolder);
//        
//        FileUtils.cleanDirectory(eftFolder);
//		FileUtils.cleanDirectory(privateFolder);
//		FileUtils.cleanDirectory(processedFolder);
//		FileUtils.cleanDirectory(invalidFolder);
//	}
//	
//	//@After
//	public void tearDown() throws IOException {
//        FileUtils.cleanDirectory(eftFolder);
//		FileUtils.cleanDirectory(privateFolder);
//		FileUtils.cleanDirectory(processedFolder);
//		FileUtils.cleanDirectory(invalidFolder);
//	}
//	
//	//TODO - Work In progress
////	@Test
////	public void testDuplicateFilename() throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException, IOException {
////		
////		//copy test file
////		FileUtils.copyFileToDirectory(new File("./src/test/resources/sbm/sbmTestFiles/CA1.EPS.SBMI.D160707.T090101001.T"), eftFolder);		
////		//launch job
////		JobExecution run = jobLauncher.run(job, new JobParameters(params));
////		//copy test file
////		FileUtils.copyFileToDirectory(new File("./src/test/resources/sbm/sbmTestFiles/CA1.EPS.SBMI.D160707.T090101001.T"), eftFolder);
////		//launch job again
////		params.put("processingType", new JobParameter("sbmi"));
////        params.put("timestamp", new JobParameter(dateFormatter.print(new Date(), Locale.US)));
////		run = jobLauncher.run(job, new JobParameters(params));
////
////		Assert.assertEquals("Incorrect number of error ER-012 created", 1,  getErrorCount(SBMErrorWarningCode.ER_012));
////	}
//	
//	
//	
//	//@After
//	public void afterTest() throws SQLException {
//		SBMTestDataDBUtil.cleanupData(jdbcTemplate);
//	}
//	
//	private long getErrorCount(SBMErrorWarningCode errorCode) {
//		return jdbcTemplate.queryForObject(SQL_ERROR_COUNT, Long.class, errorCode.getCode());
//	}
//}
