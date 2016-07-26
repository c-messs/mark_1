package gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob;

import gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentMaintenanceDTO;
import gov.hhs.cms.ff.fm.eps.ep.EPSConstants;
import gov.hhs.cms.ff.fm.eps.ep.enums.EProdEnum;
import gov.hhs.cms.ff.fm.eps.ep.enums.ProcessedToDbInd;
import gov.hhs.cms.ff.fm.eps.ep.services.TransMsgCompositeDAO;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

import com.accenture.foundation.common.exception.ApplicationException;

/**
 * @author eps
 *
 */
public class SkipBemListener implements SkipListener<BenefitEnrollmentMaintenanceDTO, BenefitEnrollmentMaintenanceDTO>,
StepExecutionListener {

	private static final Logger LOG = LoggerFactory.getLogger(SkipBemListener.class);
	private static final String EPROD = "EPROD";
	private static final int EPROD_LEN = 8;
	private static final int TRANSMSGSKIPREASONDESC_LEN = 4000;

	private TransMsgCompositeDAO transMsgCompositeDAO;
	private Long jobId;
	private String source;

	@Override
	public void onSkipInRead(Throwable t) {
		LOG.warn("EPROD-05: Temporary Table Access Failure", t);
	}

	@Override
	public void onSkipInProcess(BenefitEnrollmentMaintenanceDTO bemDTO, Throwable t) {

		LOG.info("Skipping BEM (InProcess): " + bemDTO.getBemLogInfo());

		processSkippedException(bemDTO, t);
	}

	@Override
	public void onSkipInWrite(BenefitEnrollmentMaintenanceDTO bemDTO, Throwable t) {

		LOG.info("Skipping BEM (InWrite): " + bemDTO.getBemLogInfo());

		processSkippedException(bemDTO, t);
	}

	/**
	 * Handles all ApplicationException and NullPointerExceptions.
	 * 
	 * - Extracts skipReasonCode (EPROD)
	 * - Retreives the skipReasonCode's descrition for codes/decodes cache.
	 * - Extracts the cause message, if any.
	 * - Write error log
	 * - Updates a "skipped" transactions PROCESSEDTODBSTATUSTYPECD, TRANSMSGSKIPREASONTYPECD, and TRANSMSGSKIPREASONDESC to BATCHTRANSMSG table.
	 * @param bemDTO
	 * @param t
	 */
	private void processSkippedException(BenefitEnrollmentMaintenanceDTO bemDTO, Throwable t) {

		String logMsg = null;
		String skipReasonCode = null;
		String skipReasonDesc = null;
		bemDTO.setBatchId(jobId);
		
		// ApplicationException and NullPointerException are the only ones defined as "skippable-exception-classes" in config.
		if (t.getClass().getName().equals("com.accenture.foundation.common.exception.ApplicationException")) {

			ApplicationException appEx = (ApplicationException) t;
			skipReasonCode = appEx.getInformationCode();
			skipReasonDesc = appEx.getMessage();
		}

		if (t.getClass().getName().equals("java.lang.NullPointerException")) {

			skipReasonCode = EProdEnum.EPROD_22.getCode();
			skipReasonDesc = t.getMessage();
		}

		if (skipReasonCode == null) {

			// FUTURE: Remove once all hard coded EPROD strings are replaced with constants.
			if (skipReasonDesc != null) {
				if (skipReasonDesc.indexOf(EPROD) == 0) {
					skipReasonCode = (skipReasonDesc.length() > EPROD_LEN) ? skipReasonDesc.substring(0, EPROD_LEN) : skipReasonDesc;
					skipReasonCode = (skipReasonCode.indexOf(EPROD) == 0) ? skipReasonCode : null;
				}
			}
		}

		// If skipReasonDesc is still null at this point, just get the message.
		if (skipReasonDesc == null) {
			skipReasonDesc = t.getMessage() != null ? t.getMessage() : null;
			
			if(skipReasonDesc == null) {
				// Uncaught NPEs will not have cause, therefore get stackTrace as String
				skipReasonDesc = ExceptionUtils.getFullStackTrace(t);
			}
		}

		if (skipReasonCode != null) {

			String causeMsg = getNestedCauseMessage(t);
			if (causeMsg != null) {
				skipReasonDesc = causeMsg;
			}

			EProdEnum eProd = EProdEnum.getEnum(skipReasonCode);
		    logMsg = eProd.getLogMsg();
			
		} else {
			skipReasonCode = EProdEnum.EPROD_99.getCode();
		}

		LOG.error(logMsg, t);
		
		// Just in case Desc is larger than column width.
		if (skipReasonDesc != null) {
			if (skipReasonDesc.length() > TRANSMSGSKIPREASONDESC_LEN) {
				skipReasonDesc = skipReasonDesc.substring(0, TRANSMSGSKIPREASONDESC_LEN);
			} 
		}
		
		//  For ERL (only), Update the previously skipped version to 'D'
		if(StringUtils.isNotBlank(source) && source.equalsIgnoreCase(EPSConstants.JOBPARAMETER_SOURCE_FFM)
				&& !skipReasonCode.equals(EProdEnum.EPROD_30.getCode()) && !skipReasonCode.equals(EProdEnum.EPROD_31.getCode())
						&& !skipReasonCode.equals(EProdEnum.EPROD_32.getCode()) && !skipReasonCode.equals(EProdEnum.EPROD_33.getCode())) {
			transMsgCompositeDAO.updateSkippedVersion(bemDTO, ProcessedToDbInd.D);
		}
		
		transMsgCompositeDAO.updateBatchTransMsg(bemDTO, ProcessedToDbInd.S, skipReasonCode, skipReasonDesc);
	}
	
	/**
	 * Extracts nested cause message.
	 * @param t
	 * @return
	 */
	private String getNestedCauseMessage(Throwable t) {

		// Set msg from Exception Message, overwrite with cause messages if they exist.
		String msg = t.getMessage();
		Throwable cause1 = t.getCause();

		if (cause1 != null) {
			msg = cause1.getMessage();
			Throwable cause2 = cause1.getCause();

			if (cause2 != null) {

				msg = cause2.getMessage();
				Throwable cause3 = cause2.getCause();
				
				if (cause3 != null) {
					msg = cause3.getMessage();
				}
			}
		}
		return msg;
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		jobId = stepExecution.getJobExecution().getJobId();
		source = stepExecution.getJobParameters().getString(EPSConstants.JOBPARAMETER_KEY_SOURCE);
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		return null;
	}

	public void setTransMsgService(TransMsgCompositeDAO transMsgCompositeDAO) {
		this.transMsgCompositeDAO = transMsgCompositeDAO;
	}
}
