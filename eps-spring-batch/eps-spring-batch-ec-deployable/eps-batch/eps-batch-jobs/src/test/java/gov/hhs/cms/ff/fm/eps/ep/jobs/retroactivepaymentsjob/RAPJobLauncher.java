/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.retroactivepaymentsjob;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *  This class is used to launch the Job in actual context (no test contexts).
 */

@ContextConfiguration(locations={"/rap-batch-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
@Ignore
public class RAPJobLauncher extends BaseRapBatchTest {
	 
    @Autowired
	private JobLauncher jobLauncher;
	
	@Autowired
	private Job job;
	

	/**
	 * Create a unique job instance and check it's execution completes
	 * successfully - uses the convenience methods provided by the testing
	 * superclass.
	 * 
	 * This test is used for rap job.
	 */
	@Test
	public void test_LaunchJob_RAP() throws Exception {
		
		final Map<String, JobParameter> paramsStage = new LinkedHashMap<String, JobParameter>();
		paramsStage.put("timestamp", new JobParameter(sqlDf.print(new Date(), Locale.US)));
		paramsStage.put("type", new JobParameter("RAPSTAGE"));
		JobParameters jobParametersStage = new JobParameters(paramsStage);
		JobExecution jobExStage = jobLauncher.run(job, jobParametersStage);
		assertJobCompleted(jobExStage);

		final Map<String, JobParameter> paramsRAP = new LinkedHashMap<String, JobParameter>();
		paramsRAP.put("timestamp", new JobParameter(sqlDf.print(new Date(), Locale.US)));
		paramsRAP.put("type", new JobParameter("RAP"));
		JobParameters jobParametersRAP = new JobParameters(paramsRAP);
		JobExecution jobExRAP = jobLauncher.run(job, jobParametersRAP);
		assertJobCompleted(jobExRAP);
	}

	
}
