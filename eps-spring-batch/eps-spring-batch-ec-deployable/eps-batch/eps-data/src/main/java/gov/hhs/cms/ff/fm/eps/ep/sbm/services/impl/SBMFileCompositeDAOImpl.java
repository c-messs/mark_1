package gov.hhs.cms.ff.fm.eps.ep.sbm.services.impl;

import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.GROUP_ID_EXTRACT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.accenture.foundation.common.exception.EnvironmentException;

import gov.hhs.cms.ff.fm.eps.ep.dao.SbmFileArchiveDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.SbmFileErrorAdditionalInfoDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.SbmFileErrorDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.SbmFileInfoDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.SbmFileProcessingSummaryDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.StagingSbmFileDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.StagingSbmGroupLockDao;
import gov.hhs.cms.ff.fm.eps.ep.dao.StagingSbmPolicyDao;
import gov.hhs.cms.ff.fm.eps.ep.enums.EProdEnum;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmFileArchivePO;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmFileErrorAdditionalInfoPO;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmFileErrorPO;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmFileInfoPO;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmFileProcessingSummaryPO;
import gov.hhs.cms.ff.fm.eps.ep.po.StagingSbmFilePO;
import gov.hhs.cms.ff.fm.eps.ep.po.StagingSbmGroupLockPO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMErrorDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileInfo;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileProcessingDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMSummaryAndFileInfoDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SbmDataUtil;
import gov.hhs.cms.ff.fm.eps.ep.sbm.mappers.SbmFileArchiveMapper;
import gov.hhs.cms.ff.fm.eps.ep.sbm.mappers.SbmFileErrorAdditionalInfoMapper;
import gov.hhs.cms.ff.fm.eps.ep.sbm.mappers.SbmFileErrorMapper;
import gov.hhs.cms.ff.fm.eps.ep.sbm.mappers.SbmFileInfoMapper;
import gov.hhs.cms.ff.fm.eps.ep.sbm.mappers.SbmFileProcessingSummaryMapper;
import gov.hhs.cms.ff.fm.eps.ep.sbm.mappers.StagingSbmFileMapper;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SBMFileCompositeDAO;
import gov.hhs.cms.ff.fm.eps.ep.vo.UserVO;

/**
 * @author j.radziewski
 *
 */
public class SBMFileCompositeDAOImpl implements SBMFileCompositeDAO {

	private final static Logger LOG = LoggerFactory.getLogger(SBMFileCompositeDAOImpl.class);

	private SbmFileProcessingSummaryDao sbmFileProcSumDao;
	private SbmFileInfoDao sbmFileInfoDao;
	private SbmFileErrorDao sbmFileErrorDao;
	private SbmFileErrorAdditionalInfoDao sbmFileErrAddlInfoDao;	
	private StagingSbmFileDao stagingSbmFileDao;
	private SbmFileArchiveDao sbmFileArchiveDao;
	private StagingSbmPolicyDao stagingSbmPolicyDao;
	private StagingSbmGroupLockDao stagingSbmGroupLockDao;

	private SbmFileProcessingSummaryMapper sbmFileProcSumMapper;
	private SbmFileInfoMapper sbmFileInfoMapper;
	private SbmFileErrorMapper sbmFileErrorMapper;
	private SbmFileErrorAdditionalInfoMapper sbmFileErrAddlInfoMapper;
	private StagingSbmFileMapper stagingSbmFileMapper;
	private SbmFileArchiveMapper sbmFileArchiveMapper;


	private UserVO userVO;

	@Override
	public Long saveFileInfoAndErrors(SBMFileProcessingDTO inboundFileDTO) {

		if(inboundFileDTO.getBatchId() != null) {
			userVO.setUserId(inboundFileDTO.getBatchId().toString());
		} 
		SbmFileInfoPO po = sbmFileInfoMapper.mapSbmToEps(inboundFileDTO);
		Long sbmFileInfoId = sbmFileInfoDao.insertSBMFileInfo(po);
		inboundFileDTO.getSbmFileInfo().setSbmFileInfoId(sbmFileInfoId);

		List<SbmFileErrorPO> errPoList = sbmFileErrorMapper.mapSbmToEps(sbmFileInfoId, inboundFileDTO.getErrorList());
		sbmFileErrorDao.insertSbmFileErrorList(errPoList);

		List<SbmFileErrorAdditionalInfoPO> errAddlInfoPoList = sbmFileErrAddlInfoMapper.mapSbmToEps(inboundFileDTO);
		sbmFileErrAddlInfoDao.insertSbmFileErrAddlInfoList(errAddlInfoPoList);

		return sbmFileInfoId;

	}

	@Override
	public void saveFileToStagingSBMFile(SBMFileProcessingDTO inboundFileDTO) {

		if(inboundFileDTO.getBatchId() != null) {
			userVO.setUserId(inboundFileDTO.getBatchId().toString());
		} 
		StagingSbmFilePO stagingPO = stagingSbmFileMapper.mapSbmToEps(inboundFileDTO);
		boolean isSuccess = stagingSbmFileDao.insertFileToStaging(stagingPO);
		if (isSuccess) {
			SbmFileArchivePO archivePO = sbmFileArchiveMapper.mapSbmToEps(inboundFileDTO);
			isSuccess = sbmFileArchiveDao.saveFileToArchive(archivePO);
		} 
		if (!isSuccess) {
			throw new EnvironmentException(EProdEnum.EPROD_10.getCode());
		}	

	}

	@Override
	public void extractXprToStagingPolicy(SBMFileProcessingDTO inboundFileDTO) {

		Long batchId = inboundFileDTO.getBatchId();

		if(batchId != null) {
			userVO.setUserId(batchId.toString());
		}

		String stateCd = SbmDataUtil.getStateCd(inboundFileDTO.getFileInfoType());

		int cntRows = stagingSbmPolicyDao.extractXprFromStagingSbmFile(inboundFileDTO.getXprProcGroupSize(), stateCd, userVO);

		LOG.info("Xprs extracted to StagingSbmPolicy: "  + cntRows);

		List<StagingSbmGroupLockPO> poList = stagingSbmGroupLockDao.selectSbmFileProcessingSummaryIdList();

		// Insert records with GroupingId set to zero "O".
		for (StagingSbmGroupLockPO po : poList) {
			po.setProcessingGroupId(Long.valueOf(0));
		}

		stagingSbmGroupLockDao.insertStagingSbmGroupLock(poList);
	}

	@Override
	public void insertStagingSbmGroupLockForExtract(Long sbmFileProcSumId) {

		StagingSbmGroupLockPO po = new StagingSbmGroupLockPO();
		po.setSbmFileProcSumId(sbmFileProcSumId);
		po.setProcessingGroupId(GROUP_ID_EXTRACT);

		List<StagingSbmGroupLockPO> poList = new ArrayList<>();
		poList.add(po);

		stagingSbmGroupLockDao.insertStagingSbmGroupLock(poList);
	}

	@Override
	public List<SBMSummaryAndFileInfoDTO> getFileStatus(String fileName) {

		List<SbmFileProcessingSummaryPO> summaryPOList =  sbmFileProcSumDao.findSbmFileProcessingSummary(fileName); 
		List<SBMSummaryAndFileInfoDTO> sbmSummaryDTOList = getSbmFileInfoList(summaryPOList);
		return sbmSummaryDTOList;
	}

	@Override
	public List<SBMSummaryAndFileInfoDTO> getSBMFileProcessingSummary(String issuerId, String fileSetId, String tenantId) {

		List<SbmFileProcessingSummaryPO> summaryPOList =  sbmFileProcSumDao.findSbmFileProcessingSummary(issuerId, fileSetId, tenantId); 
		List<SBMSummaryAndFileInfoDTO> summaryDTOList = getSbmFileInfoList(summaryPOList);
		return summaryDTOList;
	}

	@Override
	public SBMSummaryAndFileInfoDTO getLatestSBMFileProcessingSummaryByIssuer(String issuerId) {

		SbmFileProcessingSummaryPO summaryPO =  sbmFileProcSumDao.selectSbmFileProcessingSummaryLatest(null, issuerId); 
		SBMSummaryAndFileInfoDTO epsSummaryDTO = sbmFileProcSumMapper.mapEpsToSbm(summaryPO);
		return epsSummaryDTO;
	}

	@Override
	public SBMSummaryAndFileInfoDTO getLatestSBMFileProcessingSummaryByState(String stateCode) {

		SbmFileProcessingSummaryPO summaryPO =  sbmFileProcSumDao.selectSbmFileProcessingSummaryLatest(stateCode, null); 
		SBMSummaryAndFileInfoDTO summaryDTO = sbmFileProcSumMapper.mapEpsToSbm(summaryPO);
		return summaryDTO;
	}

	@Override
	public List<SBMSummaryAndFileInfoDTO> getAllSBMFileProcessingSummary(SBMFileStatus fileStatus) {

		List<SbmFileProcessingSummaryPO> summaryPOList =  sbmFileProcSumDao.findSbmFileProcessingSummary(fileStatus);
		List<SBMSummaryAndFileInfoDTO> summaryDTOList = getSbmFileInfoList(summaryPOList);
		return summaryDTOList;
	}

	@Override
	public List<SBMSummaryAndFileInfoDTO> getAllInProcessOrPendingApprovalForState(String stateCd) {

		List<SbmFileProcessingSummaryPO> summaryPOList =  sbmFileProcSumDao.findSbmFileProcessingSummaryInProcess(stateCd);
		List<SBMSummaryAndFileInfoDTO> summaryDTOList = getSbmFileInfoList(summaryPOList);
		return summaryDTOList;
	}

	@Override
	public List<SBMSummaryAndFileInfoDTO> performSbmFileMatch(String sbmFileId, String tenantId) {

		List<SbmFileProcessingSummaryPO> summaryPOList =  sbmFileProcSumDao.performSummaryMatch(sbmFileId, tenantId); 
		List<SBMSummaryAndFileInfoDTO> summaryDTOList = getSbmFileInfoList(summaryPOList);
		return summaryDTOList;
	}


	@Override
	public List<SBMSummaryAndFileInfoDTO> findSbmFileInfo(String fileSetId, int fileNumber) {

		List<SBMSummaryAndFileInfoDTO> summaryDTOList = new ArrayList<SBMSummaryAndFileInfoDTO>();

		List<SbmFileInfoPO> poList = sbmFileInfoDao.performFileMatch(fileSetId, fileNumber);

		Long prevSbmFileProcSumId = null;

		for (SbmFileInfoPO po : poList) {

			Long sbmFileProcSumId = po.getSbmFileProcessingSummaryId();

			if (!sbmFileProcSumId.equals(prevSbmFileProcSumId)) {
				SbmFileProcessingSummaryPO sfpsPO = sbmFileProcSumDao.selectSbmFileProcessingSummary(sbmFileProcSumId);
				SBMSummaryAndFileInfoDTO epsSummaryDTO = sbmFileProcSumMapper.mapEpsToSbm(sfpsPO);
				List<SBMFileInfo> epsSbmFileInfoList = sbmFileInfoMapper.mapEpsToSbm(poList);
				epsSummaryDTO.getSbmFileInfoList().addAll(epsSbmFileInfoList);
				summaryDTOList.add(epsSummaryDTO);
				prevSbmFileProcSumId = sbmFileProcSumId;
			}
		}
		return summaryDTOList;
	}



	private List<SBMSummaryAndFileInfoDTO> getSbmFileInfoList(List<SbmFileProcessingSummaryPO> summaryPOList) {

		List<SBMSummaryAndFileInfoDTO> summaryDTOList = new ArrayList<SBMSummaryAndFileInfoDTO>();

		List<SBMSummaryAndFileInfoDTO> epsSummaryDTOList = sbmFileProcSumMapper.mapEpsToSbm(summaryPOList);

		for (SBMSummaryAndFileInfoDTO summaryDTO : epsSummaryDTOList) {

			List<SbmFileInfoPO> filePOList = sbmFileInfoDao.getSbmFileInfoList(summaryDTO.getSbmFileProcSumId());
			List<SBMFileInfo> sbmFileInfoList = sbmFileInfoMapper.mapEpsToSbm(filePOList);

			summaryDTO.getSbmFileInfoList().addAll(sbmFileInfoList);

			summaryDTOList.add(summaryDTO);
		}
		return summaryDTOList;

	}


	@Override
	public Long saveSbmFileProcessingSummary(SBMFileProcessingDTO inboundFileDTO) {

		Long batchId = inboundFileDTO.getBatchId();

		if (batchId != null) {
			userVO.setUserId(batchId.toString());
		}

		SbmFileProcessingSummaryPO po = sbmFileProcSumMapper.mapSbmToEps(inboundFileDTO);

		Long sbmFileProcSumId = sbmFileProcSumDao.insertSbmFileProcessingSummary(po);

		return sbmFileProcSumId;
	}


	@Override
	public List<SBMFileInfo> getSbmFileInfoList(Long sbmFileProcSumId) {
		
		List<SbmFileInfoPO> filePOList = sbmFileInfoDao.getSbmFileInfoList(sbmFileProcSumId);
		List<SBMFileInfo> sbmFileInfoList = sbmFileInfoMapper.mapEpsToSbm(filePOList);
		return sbmFileInfoList;
	}

	@Override
	public void updateFileStatus(Long sbmFileProcSumId, SBMFileStatus fileStatus, Long batchId) {

		if(batchId != null) {
			
			userVO.setUserId(batchId.toString());
		} 
		sbmFileProcSumDao.updateStatus(sbmFileProcSumId, fileStatus);
	}

	@Override
	public void updateCMSApprovedInd(Long sbmFileProcSumId, String cmsApprovedInd) {

		sbmFileProcSumDao.updateCmsApproved(sbmFileProcSumId, cmsApprovedInd);

	}

	@Override
	public void saveSBMFileErrors(List<SBMErrorDTO> errorList) {


		List<SbmFileErrorPO> errPoList = sbmFileErrorMapper.mapSbmToEps(errorList);
		sbmFileErrorDao.insertSbmFileErrorList(errPoList);

	}

	@Override
	public Map<String, String> getAllErrorCodesAndDescription() {
		// TODO Auto-generated method stub
		return new HashMap<>();
	}

	@Override
	public String getFileInfoTypeXml(Long sbmFileInfoId) {

		String fileInfoTypeXml = sbmFileInfoDao.selectFileInfoXml(sbmFileInfoId);
		return fileInfoTypeXml;
	}

	@Override
	public boolean isSBMIJobRunning() {

		return sbmFileProcSumDao.verifyJobRunning("sbmIngestionBatchJob");
	}

	/**
	 * @param sbmFileProcSumDao the sbmFileProcSumDao to set
	 */
	public void setSbmFileProcSumDao(SbmFileProcessingSummaryDao sbmFileProcSumDao) {
		this.sbmFileProcSumDao = sbmFileProcSumDao;
	}

	/**
	 * @param sbmFileInfoDao the sbmFileInfoDao to set
	 */
	public void setSbmFileInfoDao(SbmFileInfoDao sbmFileInfoDao) {
		this.sbmFileInfoDao = sbmFileInfoDao;
	}

	/**
	 * @param sbmFileErrorDao the sbmFileErrorDao to set
	 */
	public void setSbmFileErrorDao(SbmFileErrorDao sbmFileErrorDao) {
		this.sbmFileErrorDao = sbmFileErrorDao;
	}

	/**
	 * @param sbmFileErrAddlInfoDao the sbmFileErrAddlInfoDao to set
	 */
	public void setSbmFileErrAddlInfoDao(SbmFileErrorAdditionalInfoDao sbmFileErrAddlInfoDao) {
		this.sbmFileErrAddlInfoDao = sbmFileErrAddlInfoDao;
	}

	/**
	 * @param stagingSbmFileDao the stagingSbmFileDao to set
	 */
	public void setStagingSbmFileDao(StagingSbmFileDao stagingSbmFileDao) {
		this.stagingSbmFileDao = stagingSbmFileDao;
	}

	/**
	 * @param sbmFileArchiveDao the sbmFileArchiveDao to set
	 */
	public void setSbmFileArchiveDao(SbmFileArchiveDao sbmFileArchiveDao) {
		this.sbmFileArchiveDao = sbmFileArchiveDao;
	}

	/**
	 * @param stagingSbmPolicyDao the stagingSbmPolicyDao to set
	 */
	public void setStagingSbmPolicyDao(StagingSbmPolicyDao stagingSbmPolicyDao) {
		this.stagingSbmPolicyDao = stagingSbmPolicyDao;
	}

	/**
	 * @param stagingSbmGroupLockDao the stagingSbmGroupLockDao to set
	 */
	public void setStagingSbmGroupLockDao(StagingSbmGroupLockDao stagingSbmGroupLockDao) {
		this.stagingSbmGroupLockDao = stagingSbmGroupLockDao;
	}

	/**
	 * @param sbmFileProcSumMapper the sbmFileProcSumMapper to set
	 */
	public void setSbmFileProcSumMapper(SbmFileProcessingSummaryMapper sbmFileProcSumMapper) {
		this.sbmFileProcSumMapper = sbmFileProcSumMapper;
	}

	/**
	 * @param sbmFileInfoMapper the sbmFileInfoMapper to set
	 */
	public void setSbmFileInfoMapper(SbmFileInfoMapper sbmFileInfoMapper) {
		this.sbmFileInfoMapper = sbmFileInfoMapper;
	}

	/**
	 * @param sbmFileErrorMapper the sbmFileErrorMapper to set
	 */
	public void setSbmFileErrorMapper(SbmFileErrorMapper sbmFileErrorMapper) {
		this.sbmFileErrorMapper = sbmFileErrorMapper;
	}

	/**
	 * @param sbmFileErrAddlInfoMapper the sbmFileErrAddlInfoMapper to set
	 */
	public void setSbmFileErrAddlInfoMapper(SbmFileErrorAdditionalInfoMapper sbmFileErrAddlInfoMapper) {
		this.sbmFileErrAddlInfoMapper = sbmFileErrAddlInfoMapper;
	}

	/**
	 * @param stagingSbmFileMapper the stagingSbmFileMapper to set
	 */
	public void setStagingSbmFileMapper(StagingSbmFileMapper stagingSbmFileMapper) {
		this.stagingSbmFileMapper = stagingSbmFileMapper;
	}

	/**
	 * @param sbmFileArchiveMapper the sbmFileArchiveMapper to set
	 */
	public void setSbmFileArchiveMapper(SbmFileArchiveMapper sbmFileArchiveMapper) {
		this.sbmFileArchiveMapper = sbmFileArchiveMapper;
	}

	/**
	 * @param userVO the userVO to set
	 */
	public void setUserVO(UserVO userVO) {
		this.userVO = userVO;
	}


}
