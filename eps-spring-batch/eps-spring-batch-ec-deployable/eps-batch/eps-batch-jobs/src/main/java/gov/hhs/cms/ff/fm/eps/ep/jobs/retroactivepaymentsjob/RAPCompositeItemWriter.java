/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.retroactivepaymentsjob;

import gov.hhs.cms.ff.fm.eps.rap.domain.RapConstants;
import gov.hhs.cms.ff.fm.eps.rap.dto.PolicyPaymentTransDTO;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemWriter;

/**
 * This is the Composite Item Writer implementation for the RAP job
 * 
 * @author girish.padmanabhan
 *
 */
public class RAPCompositeItemWriter implements ItemWriter<List<PolicyPaymentTransDTO>>, StepExecutionListener, ItemWriteListener<List<PolicyPaymentTransDTO>>{
	
	private static final Logger LOG = LoggerFactory.getLogger(RapConstants.RAP_LOGGER);
	
	private ItemWriter<PolicyPaymentTransDTO> insertRetroPaymentWriter;
	private ItemWriter<PolicyPaymentTransDTO> paymentStatusUpdateWriter;
	private ItemWriter<PolicyPaymentTransDTO> paymentTransStatusWriter;	
	private StepExecution stepExecution;
	private List<PolicyPaymentTransDTO> statusUpdateList = new ArrayList<PolicyPaymentTransDTO>();
	private List<PolicyPaymentTransDTO> newRetroList = new ArrayList<PolicyPaymentTransDTO>();
	private String modifiedByUser;
	private DateTime lastPolicyVersionDt;

	/**
	 * The implementation of the interface write() method
	 */
	@Override
	public void write(List<? extends List<PolicyPaymentTransDTO>> items) throws Exception {
		for(List<PolicyPaymentTransDTO> item: items) {
			writeLists(item);
		}
	}
	
	private void writeLists(List<PolicyPaymentTransDTO> list) throws Exception {
		LOG.info("ENTER RAPCompositeItemWriter with: "+list.size());
		
		for(PolicyPaymentTransDTO item: list) {
			
			item.setCreateBy(modifiedByUser);
			item.setLastModifiedBy(modifiedByUser);
			LOG.debug("PolicyPaymentTrans: "+item);
			
			if(!item.isUpdateStatusRec()) {
				newRetroList.add(item);
			}
			if(item.isUpdateStatusRec()) {
				statusUpdateList.add(item);
			}						
		}
		
		if(!statusUpdateList.isEmpty()) {
			//write transaction status
			LOG.debug("Updating, Inserting status record; record count:"+ statusUpdateList.size());	
			paymentStatusUpdateWriter.write(statusUpdateList);
			paymentTransStatusWriter.write(statusUpdateList);
			
			lastPolicyVersionDt = statusUpdateList.get(0).getMaintenanceStartDateTime();
			LOG.debug("lastPolicyVersionDt: "+ lastPolicyVersionDt);
			statusUpdateList.clear();
		}
		
		if(!newRetroList.isEmpty()) {
			LOG.debug("Inserting retro record; record count:"+ newRetroList.size());
			insertRetroPaymentWriter.write(newRetroList);
			LOG.debug("Inserting retro status records; record count:"+ newRetroList.size());			
			paymentTransStatusWriter.write(newRetroList);
			
			lastPolicyVersionDt = newRetroList.get(0).getMaintenanceStartDateTime();
			LOG.debug("lastPolicyVersionDt: "+ lastPolicyVersionDt);
			newRetroList.clear();
		}
		
	}

	/**
	 * @param paymentTransStatusWriter the paymentTransStatusWriter to set
	 */
	public void setPaymentTransStatusWriter(
			ItemWriter<PolicyPaymentTransDTO> paymentTransStatusWriter) {
		this.paymentTransStatusWriter = paymentTransStatusWriter;
	}

	/**
	 * @param insertRetroPaymentWriter the insertRetroPaymentWriter to set
	 */
	public void setInsertRetroPaymentWriter(
			ItemWriter<PolicyPaymentTransDTO> insertRetroPaymentWriter) {
		this.insertRetroPaymentWriter = insertRetroPaymentWriter;
	}

	/**
	 * @param paymentStatusUpdateWriter the paymentStatusUpdateWriter to set
	 */
	public void setPaymentStatusUpdateWriter(
			ItemWriter<PolicyPaymentTransDTO> paymentStatusUpdateWriter) {
		this.paymentStatusUpdateWriter = paymentStatusUpdateWriter;
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		this.stepExecution=stepExecution;
		this.modifiedByUser = stepExecution.getJobExecution().getExecutionContext().getString(RapConstants.BATCH_BUSINESS_ID);
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		// Interface method, no implementation required
		return null;
	}

	@Override
	public void beforeWrite(List<? extends List<PolicyPaymentTransDTO>> items) {
		// Interface method, no implementation required
	}

	@Override
	public void afterWrite(List<? extends List<PolicyPaymentTransDTO>> items) {
		
		if(stepExecution.getJobExecution().getExecutionContext().get(RapConstants.LASTPOLICYVERSIONDATE) == null) {
			stepExecution.getJobExecution().getExecutionContext().put(
					RapConstants.LASTPOLICYVERSIONDATE, lastPolicyVersionDt);
		}
	}

	@Override
	public void onWriteError(Exception exception, List<? extends List<PolicyPaymentTransDTO>> items) {
		LOG.warn("E9004: Process Execution Error. " + exception.getMessage());
	}

}
