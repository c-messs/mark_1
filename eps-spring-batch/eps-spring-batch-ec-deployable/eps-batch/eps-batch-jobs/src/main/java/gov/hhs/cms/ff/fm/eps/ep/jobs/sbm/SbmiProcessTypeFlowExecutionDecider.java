/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm;

import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.JOBPARAMETER_PROCESSTYPE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;

/**
 * @author rajesh.talanki
 *
 */
public class SbmiProcessTypeFlowExecutionDecider implements JobExecutionDecider {

	public static final String FLOWEXECUTIONSTATUS_SBMI = "SBMI";
	public static final String FLOWEXECUTIONSTATUS_XPR = "XPR";	
	public static final String FLOWEXECUTIONSTATUS_SBMR= "SBMR";
	
	private static final Logger LOG = LoggerFactory.getLogger(SbmiProcessTypeFlowExecutionDecider.class);
		
			
	/* (non-Javadoc)
	 * @see org.springframework.batch.core.job.flow.JobExecutionDecider#decide(org.springframework.batch.core.JobExecution, org.springframework.batch.core.StepExecution)
	 */
	@Override
	public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
		
		String processingType = jobExecution.getJobParameters().getString(JOBPARAMETER_PROCESSTYPE);
		LOG.info("Job input parameter {} : {} ", JOBPARAMETER_PROCESSTYPE, processingType);
		
		if(FLOWEXECUTIONSTATUS_XPR.equalsIgnoreCase(processingType)) {
			return new FlowExecutionStatus(FLOWEXECUTIONSTATUS_XPR);
		}
		else if(FLOWEXECUTIONSTATUS_SBMR.equalsIgnoreCase(processingType)) {
			return new FlowExecutionStatus(FLOWEXECUTIONSTATUS_SBMR);
		} 		
		
		return new FlowExecutionStatus(FLOWEXECUTIONSTATUS_SBMI);
	}

}
