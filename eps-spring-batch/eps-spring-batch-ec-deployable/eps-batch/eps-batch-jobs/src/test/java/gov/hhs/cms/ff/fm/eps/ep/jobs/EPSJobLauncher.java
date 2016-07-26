/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

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

@ContextConfiguration(locations={"/batch-application-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
@Ignore
public class EPSJobLauncher {
	
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
	public void testLaunchJob_erl_fileExtraction_Job_A() throws Exception {
		
        final DateFormatter dateFormatter = new DateFormatter("yyyy-MM-dd HH:mm:ss.SSS");
        final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
        params.put("source", new JobParameter("ffm"));
        params.put("jobType", new JobParameter(""));
        params.put("timestamp", new JobParameter(dateFormatter.print(new Date(), Locale.US)));
        final JobExecution run = jobLauncher.run(job, new JobParameters(params));
        assertEquals("Job Status", ExitStatus.COMPLETED, run.getExitStatus());
	}
	
	/**
	 * Create a unique job instance and check it's execution completes
	 * successfully - uses the convenience methods provided by the testing
	 * superclass.
	 * This test is used for erl based upon ffm jobparameter.
	 * 
	 * 
	 */
	@Test
	public void testLaunchJob_erl_ingestion_Job_B() throws Exception {

        final DateFormatter dateFormatter = new DateFormatter("yyyy-MM-dd HH:mm:ss.SSS");
        final Map<String, JobParameter> params = new LinkedHashMap<String, JobParameter>();
        params.put("source", new JobParameter("ffm"));
        params.put("jobType", new JobParameter("processor"));
        params.put("timestamp", new JobParameter(dateFormatter.print(new Date(), Locale.US)));
        final JobExecution run = jobLauncher.run(job, new JobParameters(params));
        assertEquals("Job Status", ExitStatus.COMPLETED, run.getExitStatus());
	}	

}
