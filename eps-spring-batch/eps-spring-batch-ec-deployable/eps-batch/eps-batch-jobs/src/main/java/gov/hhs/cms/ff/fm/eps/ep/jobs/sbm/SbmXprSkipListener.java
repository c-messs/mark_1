package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.SkipListener;

import com.accenture.foundation.common.exception.ApplicationException;

import gov.hhs.cms.ff.fm.eps.ep.enums.SBMErrorWarningCode;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMPolicyDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SbmXprService;

/**
 * @author eps
 *
 */
public class SbmXprSkipListener implements SkipListener<SBMPolicyDTO, SBMPolicyDTO> {

	private static final Logger LOG = LoggerFactory.getLogger(SbmXprSkipListener.class);

	private SbmXprService sbmXprService;

	@Override
	public void onSkipInRead(Throwable t) {
		LOG.warn("SYSTEM ERROR 999: Database Table Access Failure", t);
	}

	@Override
	public void onSkipInProcess(SBMPolicyDTO sbmPolicyDTO, Throwable t) {

		LOG.info("Skipping XPR (InProcess): " + sbmPolicyDTO.getLogMsg());

		processSkippedException(sbmPolicyDTO, t);
	}

	@Override
	public void onSkipInWrite(SBMPolicyDTO sbmPolicyDTO, Throwable t) {

		LOG.info("Skipping XPR (InWrite): " + sbmPolicyDTO.getLogMsg());

		processSkippedException(sbmPolicyDTO, t);
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
	private void processSkippedException(SBMPolicyDTO sbmPolicyDTO, Throwable t) {

		String logMsg = null;
		String skipReasonCode = null;
		String skipReasonDesc = null;
		
		// ApplicationException and NullPointerException are the only ones defined as "skippable-exception-classes" in config.
		if (t.getClass().getName().equals("com.accenture.foundation.common.exception.ApplicationException")) {

			ApplicationException appEx = (ApplicationException) t;
			skipReasonCode = appEx.getInformationCode();
			skipReasonDesc = appEx.getMessage();
		}

		if (t.getClass().getName().equals("java.lang.NullPointerException")) {

			skipReasonCode = SBMErrorWarningCode.SYSTEM_ERROR_999.getCode();
			skipReasonDesc = t.getMessage();
		}

		if (skipReasonCode != null) {

			String causeMsg = getNestedCauseMessage(t);
			if (causeMsg != null) {
				skipReasonDesc = causeMsg;
			}
		} 

		LOG.error(logMsg, t);
		
		sbmXprService.saveXprSkippedTransaction(sbmPolicyDTO);
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

	/**
	 * @param sbmXprService the sbmXprService to set
	 */
	public void setSbmXprService(SbmXprService sbmXprService) {
		this.sbmXprService = sbmXprService;
	}
}
