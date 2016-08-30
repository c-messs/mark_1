/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm;

import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.CURRENT_PROCESSING_SUMMARY_ID;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.item.ItemWriter;

import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMPolicyDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SbmXprService;

/**
 * This class is used as the Writer for the processing step of the batch job.
 * This writer inserts the data in the eps tables.
 * 
 * @author girish.padmanabhan
 */
public class XprProcessingWriter implements ItemWriter<SBMPolicyDTO> {

	private static final Logger LOG = LoggerFactory.getLogger(XprProcessingWriter.class);

	private SbmXprService sbmXprService;
	private JobExecution jobExecutionContext;

	/**
	 * Implementation of the write method from ItemWriter interface. The method  
	 * inserts the data in the eps tables, updates the Transaction message status
	 * 
	 * @throws Exception 
	 * 
	 */
	@Override
	public void write(List<? extends SBMPolicyDTO> sbmPolicies) throws Exception {

		if (sbmPolicies != null) {
			
			for (SBMPolicyDTO policyDTO : sbmPolicies) {
				
				LOG.info("Saving XPR Transaction. " + policyDTO.getLogMsg());

				sbmXprService.saveXprTransaction(policyDTO);
			}
			
			/*Put File Processing Summary Id to job Context for SBMR generation */
			jobExecutionContext.getExecutionContext().putLong(
					CURRENT_PROCESSING_SUMMARY_ID, sbmPolicies.get(0).getSbmFileProcSummaryId());
		}
	}
	

	/**
	 * @param sbmXprService the sbmXprService to set
	 */
	public void setSbmXprService(SbmXprService sbmXprService) {
		this.sbmXprService = sbmXprService;
	}
	

	/**
	 * @param jobExecutionContext the jobExecutionContext to set
	 */
	public void setJobExecutionContext(JobExecution jobExecutionContext) {
		this.jobExecutionContext = jobExecutionContext;
	}

}
