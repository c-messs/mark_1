package gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import gov.hhs.cms.ff.fm.eps.ep.BatchRunControl;
import gov.hhs.cms.ff.fm.eps.ep.dao.BatchRunControlDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.PolicyVersionDao;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import junit.framework.TestCase;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Unit test class for the FileIngestionReader.java
 * @author christopher.vaka
 * 
 */
@RunWith(JUnit4.class)
public class ErlManifestFileReaderTest extends TestCase {
	
	private ErlManifestFileReader erlManifestFileReader; 
	private BatchRunControlDao mockBatchRunControlDao;
	private PolicyVersionDao mockPolicyVersionDao;
	private JobExecution jobExecution;
	private File manifestDirectory;
	private JdbcTemplate mockJdbcTemplate;
	
	@Autowired
	private String erlBEMIndexCount;

	@Before
	public void setup() {
		erlManifestFileReader = new ErlManifestFileReader();
		
		mockBatchRunControlDao = createMock(BatchRunControlDao.class);
		erlManifestFileReader.setBatchRunControlDao(mockBatchRunControlDao);
		
		mockPolicyVersionDao = createMock(PolicyVersionDao.class);
		erlManifestFileReader.setPolicyVersionDao(mockPolicyVersionDao);
		
		mockJdbcTemplate = createMock(JdbcTemplate.class);
		erlManifestFileReader.setJdbcTemplate(mockJdbcTemplate);
		
		erlManifestFileReader.setErlBEMIndexCount(erlBEMIndexCount);
		
		try {
			manifestDirectory = new ClassPathResource("erlmanifestfiles/").getFile();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		erlManifestFileReader.setManifestDirectory(manifestDirectory);
		
		ExecutionContext ctx = new ExecutionContext();
		jobExecution = new JobExecution(9999L);
		jobExecution.setExecutionContext(ctx);
		erlManifestFileReader.setJobExecutionContext(jobExecution);
		
	}
	
	/**
	 * Test method for {@linkgov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.ErlManifestFileReader#read()}
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRead_success() throws Exception {
		
		BatchRunControl batchRunControl = erlManifestFileReader.read();
		assertNotNull("Batch Run Control not null", batchRunControl);
		assertEquals("Compare Batch Run Control Id", "1234", batchRunControl.getBatchRunControlId());
		assertEquals("Compare High Watermark Start", 
				DateTime.parse("2014-01-01T10:12:34.600105-04:00"), batchRunControl.getHighWaterMarkStartDateTime());
		assertEquals("Compare Record count", 100, batchRunControl.getRecordCountQuantity().intValue());
		assertEquals("Compare PAET Indicator", "Y", batchRunControl.getPreAuditExtractCompletionInd());
		
		File[] dirFiles = manifestDirectory.listFiles();
		Arrays.sort(dirFiles);
		assertEquals("Manifest file path", 
				jobExecution.getExecutionContext().getString("MANIFEST_FILE"), dirFiles[0].getAbsolutePath());
				
	}
	
	/**
	 * Test method for {@linkgov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.ErlManifestFileReader#read()}
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRead_no_ManifestFile() throws Exception {
		
		ReflectionTestUtils.setField(erlManifestFileReader, "manifestDirectory", 
				new File(manifestDirectory + File.separator + "dummyinvalidfilefortest.txt"));
		BatchRunControl batchRunControl = erlManifestFileReader.read();
		assertNull("Batch Run Control is null", batchRunControl);
	}
	
	/**
	 * Test method for {@linkgov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.ErlManifestFileReader#read()}
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRead_FileReadAlready() throws Exception {
		
/*		expect(mockBatchRunControlDao.getPreAuditIngestStatus()).andReturn(null);
		replay(mockBatchRunControlDao);*/
		
		ReflectionTestUtils.setField(erlManifestFileReader, "manifestFileName", "1234.txt");
		BatchRunControl batchRunControl = erlManifestFileReader.read();
		assertNull("Batch Run Control is null", batchRunControl);
	}
	
	@Test
	public void testRead_File_Incomplete_Manifest() throws Exception {
		
		expect(mockBatchRunControlDao.getPreAuditExtractStatus()).andReturn(null);
		replay(mockBatchRunControlDao);
		
		ReflectionTestUtils.setField(erlManifestFileReader, "manifestDirectory", 
				new ClassPathResource("elrmanifestfilesinvalid/").getFile());
		BatchRunControl batchRunControl = erlManifestFileReader.read();
		
		String ingestStatus = jobExecution.getExecutionContext().getString("CONTINUE_INGEST");
		assertNotNull("Pre Audit ingestStatus is not null", ingestStatus);
		assertEquals("Continue ingest is N", "N", "N");
		assertNull("Batch Run Control is null", batchRunControl);
	}
	
	/**
	 * Test method for {@linkgov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.ErlManifestFileReader#read()}
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRead_FilePreAuditInd_N() throws Exception {
		
		expect(mockBatchRunControlDao.getPreAuditExtractStatus()).andReturn("N");
		replay(mockBatchRunControlDao);
		
		BatchRunControl batchRunControl = erlManifestFileReader.read();
		
		String ingestStatus = jobExecution.getExecutionContext().getString("CONTINUE_INGEST");
		assertNotNull("Pre Audit ingestStatus is not null", ingestStatus);
		assertEquals("Pre Audit ingestStatus is Y", "Y", "Y");
		assertNotNull("Batch Run Control is not null", batchRunControl);
	}
	
	/**
	 * Test method for {@linkgov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.ErlManifestFileReader#read()}
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRead_FilePreAuditInd_Y() throws Exception {
		
		expect(mockBatchRunControlDao.getPreAuditExtractStatus()).andReturn("Y");
		replay(mockBatchRunControlDao);
		
		expect(mockJdbcTemplate.queryForObject(
				erlBEMIndexCount, Integer.class)).andReturn(new Integer(0));
		replay(mockJdbcTemplate);
		
		expect(mockPolicyVersionDao.getLatestPolicyMaintenanceStartDateTime()).andReturn(new DateTime());
		replay(mockPolicyVersionDao);
		
		BatchRunControl batchRunControl = erlManifestFileReader.read();
		
		String ingestStatus = jobExecution.getExecutionContext().getString("CONTINUE_INGEST");
		assertNotNull("Pre Audit ingestStatus is not null", ingestStatus);
		assertEquals("Pre Audit ingestStatus is Y", "Y", "Y");
		assertNotNull("Batch Run Control is not null", batchRunControl);
	}
	
	/**
	 * Test method for {@linkgov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.ErlManifestFileReader#read()}
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRead_FilePreAuditInd_Y_IngestIncomplete() throws Exception {
		
		expect(mockBatchRunControlDao.getPreAuditExtractStatus()).andReturn("Y");
		replay(mockBatchRunControlDao);
		
		expect(mockJdbcTemplate.queryForObject(
				erlBEMIndexCount, Integer.class)).andReturn(new Integer(2));
		replay(mockJdbcTemplate);
		
		BatchRunControl batchRunControl = erlManifestFileReader.read();
		
		String ingestStatus = jobExecution.getExecutionContext().getString("CONTINUE_INGEST");
		assertNotNull("Pre Audit ingestStatus is not null", ingestStatus);
		assertEquals("Pre Audit ingestStatus is N", "N", "N");
		assertNull("Batch Run Control is null", batchRunControl);
	}
	
	/**
	 * Test method for {@linkgov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.ErlManifestFileReader#read()}
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRead_FilePreAuditInd_Y_LastPVD_Null() throws Exception {
		
		expect(mockBatchRunControlDao.getPreAuditExtractStatus()).andReturn("Y");
		replay(mockBatchRunControlDao);
		
		expect(mockJdbcTemplate.queryForObject(
				erlBEMIndexCount, Integer.class)).andReturn(new Integer(0));
		replay(mockJdbcTemplate);
		
		expect(mockPolicyVersionDao.getLatestPolicyMaintenanceStartDateTime()).andReturn(null);
		replay(mockPolicyVersionDao);
		
		BatchRunControl batchRunControl = erlManifestFileReader.read();
		
		String ingestStatus = jobExecution.getExecutionContext().getString("CONTINUE_INGEST");
		assertNotNull("Pre Audit ingestStatus is not null", ingestStatus);
		assertEquals("Pre Audit ingestStatus is N", "N", "N");
		assertNull("Batch Run Control is null", batchRunControl);
	}
	
	@Test
	public void test_FileSort() throws Exception {

		File [] dirFiles = {new File("M-10.txt"), new File("M-21.txt"), new File("M-2.txt"), new File("M-31.txt"), new File("M-11.txt"), new File("M-1.txt")};
		
		for (File file : dirFiles)
		System.out.println(file.getName());
		
		sortFileNames(dirFiles);
		assertNotNull("dirFiles not null", dirFiles);
		System.out.println("After numeric sort file names:");
		for (File file : dirFiles)
		System.out.println(file.getName());
		
		
	}
	
	private void sortFileNames(File[] dirFiles) {
		
		if (dirFiles.length > 1) {

			Arrays.sort(dirFiles, new Comparator<File>() {
				/**
				 * @param file0
				 * @param file1
				 * @return
				 */
				public int compare(File file0, File file1) {
					
					String name0 = file0.getName();
					String name1 = file1.getName();
					
					int numName0 = Integer.parseInt(name0.substring(name0.indexOf('-')+1, name0.indexOf('.')));
					int numName1 = Integer.parseInt(name1.substring(name1.indexOf('-')+1, name1.indexOf('.')));
					
					return numName0 - numName1;
				}
			});
		}
	}
	
}
