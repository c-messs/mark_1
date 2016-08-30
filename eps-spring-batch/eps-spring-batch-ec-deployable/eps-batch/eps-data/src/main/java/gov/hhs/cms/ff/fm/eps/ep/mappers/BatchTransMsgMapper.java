package gov.hhs.cms.ff.fm.eps.ep.mappers;

import gov.cms.dsh.bem.BenefitEnrollmentMaintenanceType;
import gov.cms.dsh.bem.MemberType;
import gov.hhs.cms.ff.fm.eps.ep.BEMDataUtil;
import gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentMaintenanceDTO;
import gov.hhs.cms.ff.fm.eps.ep.enums.EProdEnum;
import gov.hhs.cms.ff.fm.eps.ep.enums.ProcessedToDbInd;
import gov.hhs.cms.ff.fm.eps.ep.po.BatchTransMsgPO;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;

import java.time.LocalDate;

import com.accenture.foundation.common.exception.ApplicationException;

/**
 * @author eps
 *
 */
public class BatchTransMsgMapper {
	
	/**
	 * Populates the PO values from the DTO
	 * 
	 * @param bemDTO
	 * @return BatchTransMsgPO
	 */
	public BatchTransMsgPO mapDTOToPO(BenefitEnrollmentMaintenanceDTO bemDTO) {
		
		return mapDTOToPO(bemDTO, null);
	}
	

	/**
	 * Populates the PO values from the DTO and the given ProcessedToDb indicator, skipReasonCd, skipReasonDesc
	 *  
	 * @param bemDTO
	 * @param ind
	 * @param skipReasonCd
	 * @param skipReasonDesc
	 * @return BatchTransMsgPO
	 */
	public BatchTransMsgPO mapDTOToPO(BenefitEnrollmentMaintenanceDTO bemDTO, ProcessedToDbInd ind) {

		BatchTransMsgPO po = new BatchTransMsgPO();

		if(bemDTO != null) {

			po.setTransMsgId(bemDTO.getTransMsgId());
			po.setBatchId(bemDTO.getBatchId());

			BenefitEnrollmentMaintenanceType bem = bemDTO.getBem();

			if (bem != null) {

				po.setExchangePolicyId(BEMDataUtil.getExchangePolicyID(bem));
				MemberType subscriber = BEMDataUtil.getSubscriberMember(bem);
				if (subscriber != null) {
					po.setSubscriberStateCd(BEMDataUtil.getSubscriberStateCode(subscriber));
					po.setIssuerHiosId(BEMDataUtil.getIssuerHIOSID(subscriber));
				}
				
				if (bem.getTransactionInformation() != null) {
					
					if (bem.getTransactionInformation().getPolicySnapshotVersionNumber() != null) {
				
						try {
							po.setSourceVersionId(Long.valueOf(bem.getTransactionInformation().getPolicySnapshotVersionNumber()));
						} catch (NumberFormatException nfEx) {
							throw new ApplicationException(nfEx, EProdEnum.EPROD_99.getCode());
						}
					}
					
					if (bem.getTransactionInformation().getPolicySnapshotDateTime() != null) {
						po.setSourceVersionDateTime(DateTimeUtil.getLocalDateTimeFromXmlGC(bem.getTransactionInformation().getPolicySnapshotDateTime()));
					}
				}				
			}
		}
		
		if (ind != null) {
			po.setProcessedToDbStatusTypeCd(ind.getValue());
		}
		po.setCreateDateTime(LocalDate.now());
		po.setLastModifiedDateTime(LocalDate.now());

		return po;
	}
	
	
	/**
	 * Populates the PO values from the DTO and the given ProcessedToDb indicator, skipReasonCd, skipReasonDesc
	 * - For "Skipped" BEMS, only minimal information is needed, hence the separate mapper.
	 * - Must use this separate mapper when updating statuses from SkipBemListener, because if there was an
	 *   ApplicationException thrown when translating PolicySnapshotVersionNumber. 
	 *  
	 * @param bemDTO
	 * @param ind
	 * @param skipReasonCd
	 * @param skipReasonDesc
	 * @return BatchTransMsgPO
	 */
	public BatchTransMsgPO mapDTOToPOForSkips(BenefitEnrollmentMaintenanceDTO bemDTO, ProcessedToDbInd ind, String skipReasonCd,
			String skipReasonDesc) {

		BatchTransMsgPO po = mapDTOToPO(bemDTO, ind);

		po.setTransMsgSkipReasonTypeCd(skipReasonCd);
		po.setTransMsgSkipReasonDesc(skipReasonDesc);

		return po;
	}	
	
	
	/**
	 * Populates the PO values from the DTO and the given ProcessedToDb indicator, EProdEnum
	 * - For "Skipped" BEMS, only minimal information is needed, hence the separate mapper.
	 * - Must use this separate mapper when updating statuses from SkipBemListener, because if there was an
	 *   ApplicationException thrown when translating PolicySnapshotVersionNumber. 
	 * @param bemDTO
	 * @param ind
	 * @param eProd
	 * @return
	 */
	public BatchTransMsgPO mapDTOToPOForSkips(BenefitEnrollmentMaintenanceDTO bemDTO, ProcessedToDbInd ind, EProdEnum eProd) {

		BatchTransMsgPO po = mapDTOToPO(bemDTO, ind);

		po.setTransMsgSkipReasonTypeCd(eProd.getCode());
		// TRANSMSGSKIPREASONTYPE.TRANSMSGSKIPREASONTYPENM to BATCHTRANSMSG.TRANSMSGSKIPREASONDESC
		po.setTransMsgSkipReasonDesc(eProd.getLongNm());

		return po;
	}	


}