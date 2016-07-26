/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob;

import gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentRequestDTO;
import gov.hhs.cms.ff.fm.eps.ep.EPSConstants;
import gov.hhs.cms.ff.fm.eps.ep.enums.ExchangeType;
import gov.hhs.cms.ff.fm.eps.ep.enums.TxnMessageDirectionType;
import gov.hhs.cms.ff.fm.eps.ep.enums.TxnMessageType;
import gov.hhs.cms.ff.fm.eps.ep.services.TransMsgFileInfoCompositeDAO;

import java.util.List;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author girish.padmanabhan
 *
 */
public class BEMExtractionWriter implements ItemWriter<BenefitEnrollmentRequestDTO> {
	
	private TransMsgFileInfoCompositeDAO txnMsgFileInfoService;
	private Long fileInfoId;
	private JobExecution jobExecution;
	private JdbcTemplate jdbcTemplate;
	private String insertDailyBEMIndexer;
	
	@Override
	public void write(List<? extends BenefitEnrollmentRequestDTO> berDTOList) throws Exception  {
				
		Long jobId = jobExecution.getJobId();
		
		for (BenefitEnrollmentRequestDTO berDTO : berDTOList) {
			berDTO.setBatchId(jobId);
			if (berDTO.isInsertFileInfo()) {
				fileInfoId = txnMsgFileInfoService.saveFileInfo(berDTO);
			}
			berDTO.setTxnMessageFileInfoId(fileInfoId);
			berDTO.setTxnMessageDirectionType(TxnMessageDirectionType.INBOUND);
			berDTO.setTxnMessageType(TxnMessageType.MSG_834);
			berDTO.setIngestJobId(jobId);
		
			//FFM File nm TS
			if(ExchangeType.FFM.getValue().equalsIgnoreCase(berDTO.getExchangeTypeCd())) {
				
				berDTO.setBatchRunControlId(jobExecution.getExecutionContext().getString(EPSConstants.BATCH_RUNCONTROL_ID));
			}
			
			berDTO.setTxnMessageFileInfoId(fileInfoId);
			
			txnMsgFileInfoService.saveTransMsg(berDTO);
			
			jdbcTemplate.update(insertDailyBEMIndexer, jobId, berDTO.getBatchRunControlId(), fileInfoId);
			
		}
	}

	/**
	 * @param txnMsgFileInfoService the txnMsgFileInfoService to set
	 */
	public void setTxnMsgFileInfoService(
			TransMsgFileInfoCompositeDAO txnMsgFileInfoService) {
		this.txnMsgFileInfoService = txnMsgFileInfoService;
	}	

	/**
	 * @param jobExecution the jobExecution to set
	 */
	public void setJobExecution(JobExecution jobExecution) {
		this.jobExecution = jobExecution;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void setInsertDailyBEMIndexer(String insertDailyBEMIndexer) {
		this.insertDailyBEMIndexer = insertDailyBEMIndexer;
	}
}
