/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob;

import static gov.hhs.cms.ff.fm.eps.ep.EPSConstants.CONTINUE_INGEST;
import static gov.hhs.cms.ff.fm.eps.ep.EPSConstants.JOBPARAMETER_ERL_JOB_TYPE;
import static gov.hhs.cms.ff.fm.eps.ep.EPSConstants.JOBPARAMETER_KEY_SOURCE;
import static gov.hhs.cms.ff.fm.eps.ep.EPSConstants.JOBPARAMETER_SOURCE_FFM;
import static gov.hhs.cms.ff.fm.eps.ep.EPSConstants.JOBPARAMETER_SOURCE_HUB;
import static gov.hhs.cms.ff.fm.eps.ep.EPSConstants.N;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
/**
 * @author girish.padmanabhan
 *
 */
public class EPSFlowExecutionDecider implements JobExecutionDecider {
	private static final Logger LOG = LoggerFactory.getLogger(EPSFlowExecutionDecider.class);
	private static final String FLOWEXECUTIONSTATUS_HUB = "HUB";
	private static final String FLOWEXECUTIONSTATUS_FFM = "FFM";
	private static final String FLOWEXECUTIONSTATUS_FFM_EXTRACTION = "FFM_EXTRACTION";
	private static final String FLOWEXECUTIONSTATUS_END_INGEST = "END_INGEST";

	/**
	 * Implementation of the interface method. Based on job parameter, returns
	 * the FlowExecutionStatus enum value as 'HUB' or 'FFM'
	 */
	@Override
	public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
		String fileSource = jobExecution.getJobParameters().getString(JOBPARAMETER_KEY_SOURCE);
		LOG.debug("file Source: "+fileSource);
        
		if (fileSource.equalsIgnoreCase(JOBPARAMETER_SOURCE_HUB)) {
            return new FlowExecutionStatus(FLOWEXECUTIONSTATUS_HUB);
        }
        if (fileSource.equalsIgnoreCase(JOBPARAMETER_SOURCE_FFM)){
        	
        	String jobType = jobExecution.getJobParameters().getString(JOBPARAMETER_ERL_JOB_TYPE);
        	
        	if (StringUtils.isBlank(jobType)) {
        		
        		String continueIngest = 
        				(String)jobExecution.getExecutionContext().get(CONTINUE_INGEST);
        		
        		if (StringUtils.isNotBlank(continueIngest) && continueIngest.equals(N)) {
        			
        			return new FlowExecutionStatus(FLOWEXECUTIONSTATUS_END_INGEST);
        		}

        		return new FlowExecutionStatus(FLOWEXECUTIONSTATUS_FFM_EXTRACTION);
        	}
        }        
        return new FlowExecutionStatus(FLOWEXECUTIONSTATUS_FFM);
    }

}
