/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * This class is used to launch the Job in actual context (no test contexts).
 */

@ContextConfiguration(locations={"/sbmi-batch-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
@Ignore
public class SBMJobLauncher {
	
    @Autowired
	private JobLauncher jobLauncher;
	
	@Autowired
	private Job job;
	

	/**
	 * Create a unique job instance and check it's execution completes
	 * successfully - uses the convenience methods provided by the testing
	 * superclass.
	 * 
	 * 
	 */	
	@Test
	public void testLaunchJob_sbmi() throws Exception {
		
        final DateFormatter dateFormatter = new DateFormatter("yyyy-MM-dd HH:mm:ss.SSS");
        final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
        params.put("processingType", new JobParameter("sbmi"));
        params.put("timestamp", new JobParameter(dateFormatter.print(new Date(), Locale.US)));
        final JobExecution run = jobLauncher.run(job, new JobParameters(params));
        assertEquals("Job Status", ExitStatus.COMPLETED, run.getExitStatus());
	}
	
	@Test
	public void testLaunchJob_sbmi_copyFile() throws Exception {
				
		File sourceFile = new File("./src/test/resources/sbm/sbmTestFiles/CA1.EPS.SBMI.D160707.T090101001.T"); // good file
//		File sourceFile = new File("./src/test/resources/sbm/sbmTestFiles/CA1.EPS.SBMI.D160101.T090101001.T"); // File level schema errors  
//		File sourceFile = new File("./src/test/resources/sbm/sbmTestFiles/CA1.EPS.SBMI.D160201.T090101001.T"); // File level errors (No schema errors)
		
		File destDir = new File("C:\\SBMFiles\\input");		
		FileUtils.cleanDirectory(destDir);
		FileUtils.cleanDirectory(new File("C:\\SBMFiles\\private"));
		FileUtils.copyFileToDirectory(sourceFile, destDir);
		
        final DateFormatter dateFormatter = new DateFormatter("yyyy-MM-dd HH:mm:ss.SSS");
        final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
        params.put("timestamp", new JobParameter(dateFormatter.print(new Date(), Locale.US)));
        final JobExecution run = jobLauncher.run(job, new JobParameters(params));
        assertEquals("Job Status", ExitStatus.COMPLETED, run.getExitStatus());
	}
	
	/**
	 * Create a unique job instance and check it's execution completes
	 * successfully - uses the convenience methods provided by the testing
	 * superclass.
	 * This test is used for sbm based upon sbmr jobparameter.
	 * 
	 * 
	 */
	@Test
	public void testLaunchJob_sbmr() throws Exception {
		
        final DateFormatter dateFormatter = new DateFormatter("yyyy-MM-dd HH:mm:ss.SSS");
        final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
        params.put("processingType", new JobParameter("sbmr"));
        params.put("timestamp", new JobParameter(dateFormatter.print(new Date(), Locale.US)));
        final JobExecution run = jobLauncher.run(job, new JobParameters(params));
        assertEquals("Job Status", ExitStatus.COMPLETED, run.getExitStatus());
	}
	
	/**
	 * Create a unique job instance and check it's execution completes
	 * successfully - uses the convenience methods provided by the testing
	 * superclass.
	 * This test is used for sbm based upon xpr jobparameter.
	 * 
	 * 
	 */
	@Test
	public void testLaunchJob_xpr() throws Exception {
		
        final DateFormatter dateFormatter = new DateFormatter("yyyy-MM-dd HH:mm:ss.SSS");
        final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
        params.put("processingType", new JobParameter("xpr"));
        params.put("timestamp", new JobParameter(dateFormatter.print(new Date(), Locale.US)));
        final JobExecution run = jobLauncher.run(job, new JobParameters(params));
        assertEquals("Job Status", ExitStatus.COMPLETED, run.getExitStatus());
	}

}
