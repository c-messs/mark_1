/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.retroactivepaymentsjob;

import static gov.hhs.cms.ff.fm.eps.rap.domain.RapConstants.JOBPARAMETER_KEY_RAPJOB_TYPE;
import static gov.hhs.cms.ff.fm.eps.rap.domain.RapConstants.JOBPARAMETER_TYPE_RAP;
import static gov.hhs.cms.ff.fm.eps.rap.domain.RapConstants.JOBPARAMETER_TYPE_RAPSTAGE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;

/**
 * @author shasidar.pabolu
 *
 */
public class RAPFlowExecutionDecider implements JobExecutionDecider {
	
	private static final Logger LOG = LoggerFactory.getLogger(RAPFlowExecutionDecider.class);

	/* (non-Javadoc)
	 * @see org.springframework.batch.core.job.flow.JobExecutionDecider#decide(org.springframework.batch.core.JobExecution, org.springframework.batch.core.StepExecution)
	 */
	@Override
	public FlowExecutionStatus decide(JobExecution jobExecution,
			StepExecution stepExecution) {
		
		String processType = jobExecution.getJobParameters().getString(JOBPARAMETER_KEY_RAPJOB_TYPE);
		LOG.debug("Process Run: "+processType);
		
		if (JOBPARAMETER_TYPE_RAP.equalsIgnoreCase(processType)) {
            return new FlowExecutionStatus(JOBPARAMETER_TYPE_RAP);
        }
		else if(JOBPARAMETER_TYPE_RAPSTAGE.equalsIgnoreCase(processType))
		{
			  return new FlowExecutionStatus(JOBPARAMETER_TYPE_RAPSTAGE);
		}
		
		return new FlowExecutionStatus(JOBPARAMETER_TYPE_RAP);
		
	}

}
