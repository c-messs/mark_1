/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob;

import gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentMaintenanceDTO;
import gov.hhs.cms.ff.fm.eps.ep.ErrorWarningLogDTO;
import gov.hhs.cms.ff.fm.eps.ep.enums.EProdEnum;
import gov.hhs.cms.ff.fm.eps.ep.enums.ProcessedToDbInd;
import gov.hhs.cms.ff.fm.eps.ep.services.ErrorWarningLogService;
import gov.hhs.cms.ff.fm.eps.ep.services.PolicyDataService;
import gov.hhs.cms.ff.fm.eps.ep.services.TransMsgCompositeDAO;
import gov.hhs.cms.ff.fm.eps.ep.validation.EPSValidationService;

import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;

/**
 * This class is used as the Writer for the processing step of the batch job.
 * This writer inserts the data in the eps tables.
 * 
 * @author girish.padmanabhan
 */
public class ErlBEMHandlerWriter implements ItemWriter<BenefitEnrollmentMaintenanceDTO> {

	private static final Logger LOG = LoggerFactory.getLogger(ErlBEMHandlerWriter.class);

	private EPSValidationService epsValidationService;
	private BEMProcessorHelper bemProcessorHelper;
	private TransMsgCompositeDAO txnMsgService;
	private PolicyDataService policyDataService;
	private ErrorWarningLogService errorWarningLogService;
	private Long jobId;

	/**
	 * Implementation of the write method from ItemWriter interface. The method  
	 * inserts the data in the eps tables, updates the Transaction message status
	 * 
	 * @throws Exception 
	 * 
	 */
	@Override
	public void write(List<? extends BenefitEnrollmentMaintenanceDTO> benefitEnrollments) throws Exception {

		if (benefitEnrollments != null) {
			
			for (BenefitEnrollmentMaintenanceDTO benefitEnrollment : benefitEnrollments) {
				
				if(benefitEnrollment.getBatchId() == null) {
					benefitEnrollment.setBatchId(jobId);
				}
				LOG.info("Processing Policy. " + benefitEnrollment.getBemLogInfo());

				//Process the Bem by passing it to validator
				bemProcessorHelper.setEpsValidationService(epsValidationService);
				bemProcessorHelper.process(benefitEnrollment);
				
				if (benefitEnrollment.isIgnore()) {
					LOG.warn(EProdEnum.EPROD_35.getLogMsg() + ". Ignoring Policy. " + benefitEnrollment.getBemLogInfo());
					txnMsgService.updateBatchTransMsg(benefitEnrollment, ProcessedToDbInd.I, EProdEnum.EPROD_35);
					
				} else if (benefitEnrollment.isVersionSkippedInPast()) {
					LOG.debug("ERL Policy version found in 'Skipped' status in an earlier processing. Setting status to Reprocess");

					//Set existing records status to 'D'
					txnMsgService.updateSkippedVersion(benefitEnrollment, ProcessedToDbInd.D);
					//Set inbound policy status to 'R'
					txnMsgService.updateBatchTransMsg(benefitEnrollment, ProcessedToDbInd.R);
					//Set later versions status to 'R'
					txnMsgService.updateLaterVersions(benefitEnrollment, ProcessedToDbInd.R);
					
				} else {
					// invoke Data Service API to save BEM
					List <ErrorWarningLogDTO> errorList = benefitEnrollment.getErrorList();
					
					if(benefitEnrollment.getBatchId().longValue() != jobId.longValue()) { 
						txnMsgService.updateBatchTransMsg(benefitEnrollment, ProcessedToDbInd.D);
						//Cloning the item to accommodate Spring retry upon failure so that item retains the old batch id upon a retry
						benefitEnrollment = (BenefitEnrollmentMaintenanceDTO) BeanUtils.cloneBean(benefitEnrollment);
						benefitEnrollment.setBatchId(jobId);
					}
					
					if(CollectionUtils.isEmpty(errorList)) {
						LOG.info("Saving Policy. " + benefitEnrollment.getBemLogInfo());
						policyDataService.saveBEM(benefitEnrollment);
						txnMsgService.updateBatchTransMsg(benefitEnrollment, ProcessedToDbInd.Y);
						
					} else {
						LOG.info("Saving Validation errors. "+ benefitEnrollment.getBemLogInfo());
						txnMsgService.updateBatchTransMsg(benefitEnrollment, ProcessedToDbInd.N);
	
						// If this bem is being reprocessed from a previous failed job, update the BatchIds in the errorList.  Method updateProcessedToDbIndicator
						// will insert a new BatchTrangMsg record relating this transMsgId to this batchId.
						if (errorList.get(0).getBatchId() != null && !errorList.get(0).getBatchId().equals(jobId)) {
							for (ErrorWarningLogDTO error : errorList) {
								error.setBatchId(benefitEnrollment.getBatchId());
							}
						}
						errorWarningLogService.saveErrorWarningLogs(errorList);
					}
				}
			}
		}
	}
	
	/**
	 * @param epsValidationService the epsValidationService to set
	 */
	public void setEpsValidationService(EPSValidationService epsValidationService) {
		this.epsValidationService = epsValidationService;
	}

	/**
	 * @param bemProcessorHelper the bemProcessorHelper to set
	 */
	public void setBemProcessorHelper(BEMProcessorHelper bemProcessorHelper) {
		this.bemProcessorHelper = bemProcessorHelper;
	}
	
	/**
	 * 
	 * @param txnMsgService
	 */
	public void setTxnMsgService(TransMsgCompositeDAO txnMsgService) {
		this.txnMsgService = txnMsgService;
	}

	/**
	 * @param policyDataService the policyDataService to set
	 */
	public void setPolicyDataService(PolicyDataService policyDataService) {
		this.policyDataService = policyDataService;
	}

	/**
	 * @param errorWarningLogService the errorWarningLogService to set
	 */
	public void setErrorWarningLogService(
			ErrorWarningLogService errorWarningLogService) {
		this.errorWarningLogService = errorWarningLogService;
	}

	/**
	 * @param jobId the jobId to set
	 */
	public void setJobId(Long jobId) {
		this.jobId = jobId;
	}
}
