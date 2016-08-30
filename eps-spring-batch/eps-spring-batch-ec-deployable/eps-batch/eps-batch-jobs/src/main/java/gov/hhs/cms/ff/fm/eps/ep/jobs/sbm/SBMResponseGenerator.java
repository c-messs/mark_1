package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm;

import static gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus.EXPIRED;
import static gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus.REJECTED;
import static gov.hhs.cms.ff.fm.eps.ep.enums.SBMResponsePhaseTypeCode.FINAL;
import static gov.hhs.cms.ff.fm.eps.ep.enums.SBMResponsePhaseTypeCode.INITIAL;
import static gov.hhs.cms.ff.fm.eps.ep.enums.SBMResponsePhaseTypeCode.STATUS;
import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.N;
import static gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants.TARGET_EFT_APPLICATION_TYPE;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.accenture.foundation.common.exception.EnvironmentException;

import gov.cms.dsh.sbmr.ErrorType;
import gov.cms.dsh.sbmr.FileAcceptanceRejection;
import gov.cms.dsh.sbmr.FileInformationType;
import gov.cms.dsh.sbms.SBMS;
import gov.hhs.cms.ff.fm.eps.dispatcher.EFTDispatchDriver;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMErrorWarningCode;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMCache;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMErrorDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileInfo;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileProcessingDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMSummaryAndFileInfoDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMUpdateStatusRecordDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SbmResponseDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SBMFileCompositeDAO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SbmResponseCompositeDao;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SbmUpdateStatusDataService;

public class SBMResponseGenerator {

	private static final Logger LOG = LoggerFactory.getLogger(SBMResponseGenerator.class);
	
	private SBMFileCompositeDAO fileCompositeDao;
	private SbmResponseCompositeDao sbmResponseDao;
	private Marshaller sbmrMarshaller;
	private Marshaller sbmsMarshaller;
	private EFTDispatchDriver eftDispatcher;
	private String environmentCodeSuffix;
	private SbmUpdateStatusDataService updateStatusDataService;
	
	public SBMResponseGenerator() {
		try {
			//create marshaller for SBMR
			sbmrMarshaller = JAXBContext.newInstance(FileAcceptanceRejection.class).createMarshaller();
			 // format the XML output
			sbmrMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			
			//create marshaller for SBMS
			sbmsMarshaller = JAXBContext.newInstance(SBMS.class).createMarshaller();
			 // format the XML output
			sbmsMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			
		} catch (JAXBException e) {
			LOG.error("JAXB error.", e);
			throw new EnvironmentException("JAXB error.", e);
		}
	}
	
	public void generateSBMRWithPolicyErrors(Long smbFileProcSummaryId, Long jobExecId) throws SQLException, IOException, JAXBException {
		LOG.info("Genrating SBMR for smbFileProcSummaryId:{}", smbFileProcSummaryId);
		
		SbmResponseDTO sbmrDto = sbmResponseDao.generateSBMR(smbFileProcSummaryId);
		BigDecimal errorThreshold = sbmrDto.getSbmSummaryAndFileInfo().getErrorThresholdPercent();
		if(errorThreshold == null) {
			errorThreshold = BigDecimal.ZERO;
		}
		
		BigDecimal percentRejected = getPercentRejected(sbmrDto);
		
		String tradingPartnerId = SbmHelper.getTradingPartnerId(sbmrDto.getSbmSummaryAndFileInfo().getSbmFileInfoList());
				
		SBMFileStatus fileStatus = SBMFileStatus.ACCEPTED;
		
		LOG.info("Compare threshold; errorThresholdAllowed:{}  percentRejected:{}", errorThreshold, percentRejected);
		if(percentRejected.compareTo(errorThreshold) > 0 ) {
			// percentRejected exceeded threshold so reject file/fileset 			
			fileStatus = SBMFileStatus.REJECTED;
			//create fileError ER-057
			generateErrorThresholdExceeded(sbmrDto);  
		}
		else if(sbmrDto.isXprErrorsExist()) {
			fileStatus = SBMFileStatus.ACCEPTED_WITH_ERRORS;
		}
		else if(sbmrDto.isXprWarningsExist()) {
			fileStatus = SBMFileStatus.ACCEPTED_WITH_WARNINGS;
		}
		
		//For states that doesn't require approval, approve them directly
		if(N.equalsIgnoreCase(sbmrDto.getSbmSummaryAndFileInfo().getCmsApprovalRequiredInd())) {
			LOG.info("CmsApprovalRequiredInd is 'N'");
			if(SBMFileStatus.ACCEPTED.equals(fileStatus)) {
				fileStatus = SBMFileStatus.APPROVED;
				LOG.info("Performing approval");
				updateStatusDataService.executeApproval(jobExecId, smbFileProcSummaryId);
			}
			else if(SBMFileStatus.ACCEPTED_WITH_ERRORS.equals(fileStatus)) {
				fileStatus = SBMFileStatus.APPROVED_WITH_ERRORS;
				LOG.info("Performing approval");
				updateStatusDataService.executeApproval(jobExecId, smbFileProcSummaryId);
			}
			else if(SBMFileStatus.ACCEPTED_WITH_WARNINGS.equals(fileStatus)) {
				fileStatus = SBMFileStatus.APPROVED_WITH_WARNINGS;
				LOG.info("Performing approval");
				updateStatusDataService.executeApproval(jobExecId, smbFileProcSummaryId);
			}
		}
		
		LOG.info("Setting file status smbFileProcSummaryId:{} FileStatus:{}", smbFileProcSummaryId, fileStatus);		
		setFileSatus(sbmrDto, fileStatus);
		fileCompositeDao.updateFileStatus(smbFileProcSummaryId, fileStatus);
		
		String sbmrXMLString = marshallSBMR(sbmrDto.getSbmr());
		//TODO perform schema check on generated xml
		String filename = eftDispatcher.getFileID(SBMConstants.FUNCTION_CODE_SBMR);
		Long physicalDocId = eftDispatcher.saveDispatchContent(sbmrXMLString.getBytes(), filename, SBMConstants.FUNCTION_CODE_SBMR, environmentCodeSuffix, tradingPartnerId, 0, null, TARGET_EFT_APPLICATION_TYPE); 
		LOG.info("SBMR PhysicalDocumentId: {} for smbFileProcSummaryId: {}", physicalDocId, smbFileProcSummaryId);	
		
		//create entries in SBMResponse	table		
		sbmResponseDao.createSBMResponseRecord(smbFileProcSummaryId, null, physicalDocId, INITIAL);
	}
	
	public void generateSBMRForUpdateStatus(SBMUpdateStatusRecordDTO dto) throws SQLException, IOException, JAXBException {
		
		Long  smbFileProcSummaryId = dto.getSbmFileProcSumId();
		
		LOG.info("Genrating UpdateStatus SBMR for smbFileProcSummaryId:{}", smbFileProcSummaryId);
		
		SbmResponseDTO sbmrDto = sbmResponseDao.generateUpdateStatusSBMR(smbFileProcSummaryId);
		
		String tradingPartnerId = SbmHelper.getTradingPartnerId(sbmrDto.getSbmSummaryAndFileInfo().getSbmFileInfoList());
		
		SBMFileStatus fileStatus = dto.getNewFileSatus();
		
		if(SBMFileStatus.DISAPPROVED.equals(fileStatus)) {
			//Summary counts not required for Disapproved scenario
			sbmrDto.getSbmr().setSBMIPROCSUM(null);
		}
		
		LOG.info("Setting file status smbFileProcSummaryId:{} FileStatus:{}", smbFileProcSummaryId, fileStatus);		
		setFileSatus(sbmrDto, fileStatus);
		fileCompositeDao.updateFileStatus(smbFileProcSummaryId, fileStatus);
		
		String sbmrXMLString = marshallSBMR(sbmrDto.getSbmr());
		
		String filename = EFTDispatchDriver.getFileID(SBMConstants.FUNCTION_CODE_SBMR);
		Long physicalDocId = eftDispatcher.saveDispatchContent(sbmrXMLString.getBytes(), filename, SBMConstants.FUNCTION_CODE_SBMR, environmentCodeSuffix, tradingPartnerId, 0, null, TARGET_EFT_APPLICATION_TYPE); 
		LOG.info("SBMR PhysicalDocumentId: {} for smbFileProcSummaryId: {}", physicalDocId, smbFileProcSummaryId);	
		
		//create entries in SBMResponse	table		
		sbmResponseDao.createSBMResponseRecord(smbFileProcSummaryId, null, physicalDocId, FINAL);
		
	}
	
	public void generateSBMS(SBMFileProcessingDTO fileProcDto) throws JAXBException, SQLException, IOException {

		LOG.info("Genrating SBMS for sbmFileInfoId:{}", fileProcDto.getSbmFileInfo().getSbmFileInfoId());

		String sbmsXML = createSbmsXml(fileProcDto.getSbmFileInfo(), fileProcDto.getErrorList(), fileProcDto.getSbmFileStatusType());

		//dispatch sbms to eft
		String filename = eftDispatcher.getFileID(SBMConstants.FUNCTION_CODE_SBMS);
		Long physicalDocId = eftDispatcher.saveDispatchContent(sbmsXML.getBytes(), filename, SBMConstants.FUNCTION_CODE_SBMS, environmentCodeSuffix, fileProcDto.getSbmFileInfo().getTradingPartnerId(), 0, null, TARGET_EFT_APPLICATION_TYPE);
		LOG.info("SBMS PhysicalDocumentId: {} for sbmFileInfoId: {}", physicalDocId, fileProcDto.getSbmFileInfo().getSbmFileInfoId());	

		//create entries in SBMResponse	table		
		sbmResponseDao.createSBMResponseRecord(fileProcDto.getSbmFileProcSumId(), fileProcDto.getSbmFileInfo().getSbmFileInfoId(), physicalDocId, STATUS);

	}
	
	public void generateSBMSForAllSBMInfos(SBMSummaryAndFileInfoDTO summaryDto, List<SBMErrorDTO> errorList) throws JAXBException, SQLException, IOException {

		LOG.info("Genrating SBMS for all SBMFileInfos for SbmFileProcSumId: {}", summaryDto.getSbmFileProcSumId());
						
		for( SBMFileInfo fileInfo : summaryDto.getSbmFileInfoList()) {
			
			if(fileInfo.isRejectedInd()) {
				// skip rejected files
				continue;
			}
						
			LOG.info("Genrating SBMS for sbmFileInfoId:{}", fileInfo.getSbmFileInfoId());

			String sbmsXML = createSbmsXml(fileInfo, errorList, summaryDto.getSbmFileStatusType());

			//dispatch sbms to eft
			String filename = eftDispatcher.getFileID(SBMConstants.FUNCTION_CODE_SBMS);
			Long physicalDocId = eftDispatcher.saveDispatchContent(sbmsXML.getBytes(), filename, SBMConstants.FUNCTION_CODE_SBMS, environmentCodeSuffix, fileInfo.getTradingPartnerId(), 0, null, TARGET_EFT_APPLICATION_TYPE);
			LOG.info("SBMS PhysicalDocumentId: {} for sbmFileInfoId: {}", physicalDocId, fileInfo.getSbmFileInfoId());	

			//create entries in SBMResponse	table
			sbmResponseDao.createSBMResponseRecord(summaryDto.getSbmFileProcSumId(), fileInfo.getSbmFileInfoId(), physicalDocId, STATUS);
		}
	}
	
	private String createSbmsXml(SBMFileInfo sbmFileInfo, List<SBMErrorDTO> errorList, SBMFileStatus fileStatus) throws JAXBException  {
		
		SBMS sbms = new SBMS();
		sbms.setFileName(sbmFileInfo.getSbmFileNm());
		
		if(EXPIRED.equals(fileStatus)) {
			fileStatus = REJECTED;  // SBMR/SBMS schema does not have EXPIERED status so map it to REJECTED status
		}
		
		sbms.setStatus(fileStatus.getName()); 
		
		if(CollectionUtils.isNotEmpty(errorList)) {
			
			SBMErrorDTO error = errorList.get(0);
			
			sbms.setErrorCode(error.getSbmErrorWarningTypeCd());
			sbms.setElementInError(error.getElementInErrorNm());
			sbms.setErrorDescription(SBMCache.getErrorDescription(error.getSbmErrorWarningTypeCd()));
			if(CollectionUtils.isNotEmpty(error.getAdditionalErrorInfoList())) {
				sbms.setAdditionalErrorInfo(error.getAdditionalErrorInfoList().get(0));
			}
		}
		
		StringWriter writer = new StringWriter();	
		sbmsMarshaller.marshal(sbms, writer);
		LOG.debug("SBMS:\n{}", writer.toString());
		
		return writer.toString();
	}
	
	private BigDecimal getPercentRejected(SbmResponseDTO sbmrDto) {
		
		BigDecimal totalRecordsProcessed = sbmrDto.getTotalRecordsProcessed();
		BigDecimal totalRecordsRejected = sbmrDto.getTotalRecordsRejected();
		
		LOG.info("totalRecordsProcessed:{}, totalRecordsRejected: {}", totalRecordsProcessed, totalRecordsRejected);
		
		if(BigDecimal.ZERO.compareTo(totalRecordsProcessed) == 0) {
			return BigDecimal.ZERO;
		}
		
		return totalRecordsRejected.multiply(new BigDecimal(100)).divide(totalRecordsProcessed, 2, RoundingMode.HALF_UP);
	}
	
	private void setFileSatus(SbmResponseDTO sbmrDto, SBMFileStatus fileStatus) {
		
		for( FileInformationType fileInfo: sbmrDto.getSbmr().getSBMIFileInfo()) {
			fileInfo.setFileProcessingStatus(fileStatus.getName());
		}
	}
	
	private String marshallSBMR(FileAcceptanceRejection far) throws JAXBException {
		
		StringWriter writer = new StringWriter();
				
		sbmrMarshaller.marshal(far, writer);
		LOG.debug("SBMR:\n{}", writer.toString());
		
		return writer.toString();
	}
	
	private void generateErrorThresholdExceeded(SbmResponseDTO sbmrDto) {
		
		List<SBMErrorDTO> errorList = new ArrayList<>();
		String issuerFileSetId = sbmrDto.getSbmSummaryAndFileInfo().getIssuerFileSetId();
		for(SBMFileInfo fileInfo : sbmrDto.getSbmSummaryAndFileInfo().getSbmFileInfoList()) {
			
			if(fileInfo.isRejectedInd()) {
				//Skip rejected files
				continue;
			}
			
			SBMErrorDTO error = SbmHelper.createErrorLog(null, SBMErrorWarningCode.ER_057.getCode());
			error.setAdditionalErrorInfoText(issuerFileSetId != null ? issuerFileSetId : fileInfo.getSbmFileId());
			error.setSbmFileInfoId(fileInfo.getSbmFileInfoId());			
			errorList.add(error);
		}
		//Save error to database
		fileCompositeDao.saveSBMFileErrors(errorList);
		
		FileAcceptanceRejection sbmr = sbmrDto.getSbmr();
		String errorText = SBMCache.getErrorDescription(SBMErrorWarningCode.ER_057.getCode());
		for( FileInformationType sbmrFileInfo: sbmr.getSBMIFileInfo()) {
			ErrorType error = new ErrorType();
			error.setErrorCode(SBMErrorWarningCode.ER_057.getCode());
			error.setErrorDescription(errorText);
			sbmrFileInfo.setFileError(error);			
		}
		
	}

	/**
	 * @param fileCompositeDao the fileCompositeDao to set
	 */
	public void setFileCompositeDao(SBMFileCompositeDAO fileCompositeDao) {
		this.fileCompositeDao = fileCompositeDao;
	}

	/**
	 * @param sbmResponseDao the sbmResponseDao to set
	 */
	public void setSbmResponseDao(SbmResponseCompositeDao sbmResponseDao) {
		this.sbmResponseDao = sbmResponseDao;
	}

	/**
	 * @param eftDispatcher the eftDispatcher to set
	 */
	public void setEftDispatcher(EFTDispatchDriver eftDispatcher) {
		this.eftDispatcher = eftDispatcher;
	}

	/**
	 * @param environmentCodeSuffix the environmentCodeSuffix to set
	 */
	public void setEnvironmentCodeSuffix(String environmentCodeSuffix) {
		this.environmentCodeSuffix = environmentCodeSuffix;
	}

	/**
	 * @param updateStatusDataService the updateStatusDataService to set
	 */
	public void setUpdateStatusDataService(SbmUpdateStatusDataService updateStatusDataService) {
		this.updateStatusDataService = updateStatusDataService;
	}
	
}
