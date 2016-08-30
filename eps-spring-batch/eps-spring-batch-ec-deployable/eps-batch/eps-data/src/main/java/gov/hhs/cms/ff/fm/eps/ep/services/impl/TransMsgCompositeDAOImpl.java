package gov.hhs.cms.ff.fm.eps.ep.services.impl;

import gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentMaintenanceDTO;
import gov.hhs.cms.ff.fm.eps.ep.dao.BatchTransMsgDao;
import gov.hhs.cms.ff.fm.eps.ep.enums.EProdEnum;
import gov.hhs.cms.ff.fm.eps.ep.enums.ProcessedToDbInd;
import gov.hhs.cms.ff.fm.eps.ep.mappers.BatchTransMsgMapper;
import gov.hhs.cms.ff.fm.eps.ep.po.BatchTransMsgPO;
import gov.hhs.cms.ff.fm.eps.ep.services.TransMsgCompositeDAO;
import gov.hhs.cms.ff.fm.eps.ep.vo.UserVO;

import java.time.LocalDate;

import com.accenture.foundation.common.exception.ApplicationException;


/**
 * @author eps
 *
 */
public class TransMsgCompositeDAOImpl implements TransMsgCompositeDAO {

	private BatchTransMsgMapper batchTransMsgMapper;
	private BatchTransMsgDao batchTransMsgDao;
	private UserVO userVO;


	@Override
	public void updateBatchTransMsg(BenefitEnrollmentMaintenanceDTO bemDTO, ProcessedToDbInd ind) {

		updateBatchTransMsg(bemDTO, ind, null, null);
	}
	
	@Override
	public void updateBatchTransMsg(BenefitEnrollmentMaintenanceDTO bemDTO, ProcessedToDbInd ind, String skipReasonCd, String skipReasonDesc) {

		BatchTransMsgPO po = batchTransMsgMapper.mapDTOToPOForSkips(bemDTO, ind, skipReasonCd, skipReasonDesc);
		Boolean isSuccess = batchTransMsgDao.updateBatchTransMsg(po);

		// Skipped Bems that are reprocessed will not have a record (batchId/transMsgId) in 
		// BatchTransMsg table for this batchId, therefore do an insert.
		// updateBatchTransMsg will fail if BatchId is null.
		if(!isSuccess) {
			userVO.setUserId(bemDTO.getBatchId().toString());
			batchTransMsgDao.insertBatchTransMsg(po);
		}
	}
	
	@Override
	public void updateBatchTransMsg(BenefitEnrollmentMaintenanceDTO bemDTO, ProcessedToDbInd ind, EProdEnum eProd) {
		
		BatchTransMsgPO po = batchTransMsgMapper.mapDTOToPOForSkips(bemDTO, ind, eProd);
		Boolean isSuccess = batchTransMsgDao.updateBatchTransMsg(po);

		// Skipped Bems that are reprocessed will not have a record (batchId/transMsgId) in 
		// BatchTransMsg table for this batchId, therefore do an insert.
		// updateBatchTransMsg will fail if BatchId is null.
		if(!isSuccess) {
			userVO.setUserId(bemDTO.getBatchId().toString());
			batchTransMsgDao.insertBatchTransMsg(po);
		}
		
	}


	@Override
	public void updateProcessedToDbIndicator(Long batchId, Long transMsgId, ProcessedToDbInd ind)  {

		BatchTransMsgPO po = new BatchTransMsgPO();

		po.setBatchId(batchId);
		po.setTransMsgId(transMsgId);
		if (ind !=  null) {
			po.setProcessedToDbStatusTypeCd(ind.getValue());
		}

		Boolean isSuccess = batchTransMsgDao.updateBatchTransMsg(po);

		// Skipped Bems that are reprocessed will not have a record (batchId/transMsgId) in 
		// BatchTransMsg table for this batchId, therefore do an insert.
		// updateBatchTransMsg will fail if BatchId is null.
		if(!isSuccess) {
			userVO.setUserId(batchId.toString());
			po.setCreateDateTime(LocalDate.now());
			po.setLastModifiedDateTime(LocalDate.now());
			batchTransMsgDao.insertBatchTransMsg(po);
		}
	}


	@Override
	public BatchTransMsgPO getBatchTransMsg(Long batchId, Long transMsgId) {

		BatchTransMsgPO batchTransMsgPO = batchTransMsgDao.getBatchTransMsgByPk(batchId, transMsgId);

		return batchTransMsgPO;
	}


	@Override
	public Integer getSkippedTransMsgCount(BenefitEnrollmentMaintenanceDTO bemDTO) {

		BatchTransMsgPO po = batchTransMsgMapper.mapDTOToPO(bemDTO);
		verifyInputCriteria(po);

		return batchTransMsgDao.getSkipCount(po);
	}


	@Override
	public Integer updateSkippedVersion(BenefitEnrollmentMaintenanceDTO bemDTO, ProcessedToDbInd ind) {

		userVO.setUserId(bemDTO.getBatchId().toString());
		
		BatchTransMsgPO po = batchTransMsgMapper.mapDTOToPO(bemDTO, ind);
		verifyInputCriteria(po);
		
		return batchTransMsgDao.updateSkippedVersion(po);
	}
	
	@Override
	public Integer updateLaterVersions(BenefitEnrollmentMaintenanceDTO bemDTO, ProcessedToDbInd ind) {

		userVO.setUserId(bemDTO.getBatchId().toString());
		
		BatchTransMsgPO po = batchTransMsgMapper.mapDTOToPO(bemDTO, ind);
		verifyInputCriteria(po);
		
		return batchTransMsgDao.updateLaterVersions(po);
	}
	
	@Override
	public Integer getSkippedVersionCount(BenefitEnrollmentMaintenanceDTO bemDTO) {

		BatchTransMsgPO po = batchTransMsgMapper.mapDTOToPO(bemDTO);
		verifyInputCriteria(po);
		
		return batchTransMsgDao.getSkippedVersionCount(po);
	}

	private void verifyInputCriteria(BatchTransMsgPO po) {

		EProdEnum eProd = null;
		
		if (po.getExchangePolicyId() == null) {
			eProd = EProdEnum.EPROD_30;
			throw new ApplicationException(eProd.getLogMsg(), eProd.getCode());
		}
		if (po.getSubscriberStateCd() == null) {
			eProd = EProdEnum.EPROD_31;
			throw new ApplicationException(eProd.getLogMsg(), eProd.getCode());
		}
		if (po.getIssuerHiosId() == null) {
			eProd = EProdEnum.EPROD_32;
			throw new ApplicationException(eProd.getLogMsg(), eProd.getCode());
		}
		if (po.getSourceVersionId() == null) {
			eProd = EProdEnum.EPROD_33;
			throw new ApplicationException(eProd.getLogMsg(), eProd.getCode());
		}
	}

	public void setBatchTransMsgMapper(BatchTransMsgMapper batchTransMsgMapper) {
		this.batchTransMsgMapper = batchTransMsgMapper;
	}

	public void setBatchTransMsgDao(BatchTransMsgDao batchTransMsgDao) {
		this.batchTransMsgDao = batchTransMsgDao;
	}

	public void setUserVO(UserVO userVO) {
		this.userVO = userVO;
	}

}
