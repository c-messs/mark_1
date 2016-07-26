/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.retroactivepaymentsjob;

import gov.hhs.cms.ff.fm.eps.rap.domain.RapConstants;
import gov.hhs.cms.ff.fm.eps.rap.dto.PolicyDataDTO;
import gov.hhs.cms.ff.fm.eps.rap.dto.PolicyPaymentTransDTO;
import gov.hhs.cms.ff.fm.eps.rap.service.RAPProcessingRequest;
import gov.hhs.cms.ff.fm.eps.rap.service.RAPProcessingResponse;
import gov.hhs.cms.ff.fm.eps.rap.service.RapProcessingService;

import java.util.List;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.item.ItemProcessor;

/**
 * This is the Item Processor class for RAP
 * 
 * @author girish.padmanabhan
 */
public class RAPProcessor implements ItemProcessor<PolicyDataDTO, List<PolicyPaymentTransDTO>>{

	private static final Logger LOGGER = LoggerFactory.getLogger(RapConstants.RAP_LOGGER);
	
	private RapProcessingService rapService;
	private JobExecution jobExecution;
	private DateTime lastPolicyVersionDt;

	/**
	 * The process() method of ItemProcessor interface
	 * 
	 * @param policy
	 */
	@Override
	public List<PolicyPaymentTransDTO> process(PolicyDataDTO policy) throws Exception {
		LOGGER.debug("PolicyData: ", policy); 
		List<PolicyPaymentTransDTO> pmtTrans = null;
		
		lastPolicyVersionDt = policy.getMaintenanceStartDateTime();
		
		RAPProcessingRequest request = new RAPProcessingRequest();
		request.setPolicyDataDTO(policy);
		
		RAPProcessingResponse response = rapService.processRetroActivePayments(request);
		LOGGER.info("Service response:"+ response);
		
		pmtTrans = response.getPolicyPaymentTransactions();
		
		// If the job has processed a policy out of order, retain the latest date processed.
		if(jobExecution.getExecutionContext().get(RapConstants.LASTPOLICYVERSIONDATE) != null) {
			
			DateTime lastpolicyVersionDateinContext = (DateTime) jobExecution
					.getExecutionContext().get(
							RapConstants.LASTPOLICYVERSIONDATE);
			
			if (lastPolicyVersionDt.isBefore(lastpolicyVersionDateinContext)) {
				lastPolicyVersionDt = lastpolicyVersionDateinContext;
			}
		}
		jobExecution.getExecutionContext().put(RapConstants.LASTPOLICYVERSIONDATE, lastPolicyVersionDt);
			
		return pmtTrans;
	}	

	/**
	 * @param rapService the rapService to set
	 */
	public void setRapService(RapProcessingService rapService) {
		this.rapService = rapService;
	}
	
	/**
	 * @param jobExecution the jobExecution to set
	 */
	public void setJobExecution(JobExecution jobExecution) {
		this.jobExecution = jobExecution;
	}

}
