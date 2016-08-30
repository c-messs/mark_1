package gov.hhs.cms.ff.fm.eps.ep.mappers;


import gov.hhs.cms.ff.fm.eps.ep.ErrorWarningLogDTO;
import gov.hhs.cms.ff.fm.eps.ep.po.ErrorWarningLogPO;

import java.time.LocalDate;

/**
 * @author EPS
 *
 */
public class ErrorWarningLogMapper {


	/**
	 * @param errWarnLogDTO
	 * @param userVO
	 * @return ErrorWarningLogPO
	 */
	public ErrorWarningLogPO mapDTOToPO(ErrorWarningLogDTO errWarnLogDTO) {

		ErrorWarningLogPO errWarnLogPO = new ErrorWarningLogPO();

		if(errWarnLogDTO != null) {
	
			errWarnLogPO.setErrorWarningDetailedDesc(errWarnLogDTO.getErrorWarningDetailedDesc());
			errWarnLogPO.setBizAppAckErrorCd(errWarnLogDTO.getBizAppAckErrorCd());
			errWarnLogPO.setErrorElement(errWarnLogDTO.getErrorElement());
			errWarnLogPO.setTransMsgID(errWarnLogDTO.getTransMsgId());
			errWarnLogPO.setProcessingErrorCd(errWarnLogDTO.getProcessingErrorCd());
			errWarnLogPO.setTransMsgFileInfoId(errWarnLogDTO.getTransMsgFileInfoId());
			errWarnLogPO.setBatchId(errWarnLogDTO.getBatchId());
		}
		errWarnLogPO.setCreateDateTime(LocalDate.now());
		errWarnLogPO.setLastModifiedDateTime(LocalDate.now());

		return errWarnLogPO;
	}

}
