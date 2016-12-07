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

import gov.cms.dsh.sbmr.FileAcceptanceRejection;
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

/**
 * Generate SMBR.
 *
 */
public class SBMResponseGenerator {

	private static final Logger LOG = LoggerFactory.getLogger(SBMResponseGenerator.class);

	private SBMFileCompositeDAO fileCompositeDao;
	private SbmResponseCompositeDao sbmResponseDao;
	private Marshaller sbmrMarshaller;
	private Marshaller sbmsMarshaller;
	private EFTDispatchDriver eftDispatcher;
	private String environmentCodeSuffix;
	private SbmUpdateStatusDataService updateStatusDataService;

	/**
	 * Constructor.
	 */
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

	/** 
	 * SBM Monthly Enrollment & Payment Processing
	 * Diagram H. Missing Policy/Threshold Validation
	 * 
	 * Performs the following 
	 * - Missing Policy Validation
	 * - Threshold Validation
	 * - File Status Update
	 * - SBMR Generation
	 * - Call EFT Dispatcher
	 * - Write response record for PhysicalDocumentId
	 * 
	 * (TODO: Refactor)
	 *  
	 * @param sbmFileProcSumId
	 * @param jobId
	 * @throws SQLException
	 * @throws IOException
	 * @throws JAXBException
	 */
	public void generateSBMRWithPolicyErrors(Long sbmFileProcSumId, Long jobId) throws SQLException, JAXBException, IOException {

		SbmResponseDTO summaryDTO = sbmResponseDao.getSummary(sbmFileProcSumId);

		sbmResponseDao.validateMissingPolicies(jobId, sbmFileProcSumId);

		SBMFileStatus fileStatus = validateErrorThreshold(summaryDTO);

		// H5. Threshold exceeded?
		if (SBMFileStatus.REJECTED.equals(fileStatus)) {
			//create fileError ER-057
			generateErrorThresholdExceeded(sbmFileProcSumId, summaryDTO.getSbmSummaryAndFileInfo().getIssuerFileSetId()); 
		}

		// H11. Confirm SBM Approval Indicator
		// H12. Does SBM Require CMS Approval?
		if(N.equalsIgnoreCase(summaryDTO.getSbmSummaryAndFileInfo().getCmsApprovalRequiredInd())) {
			// CMS approval NOT required.
			fileStatus = approveAcceptedSummaries(jobId, sbmFileProcSumId, fileStatus);
		}

		fileCompositeDao.updateFileStatus(sbmFileProcSumId, fileStatus, jobId);

		SbmResponseDTO sbmrDTO = sbmResponseDao.generateSBMR(jobId, sbmFileProcSumId);

		Long physicalDocId = savePhysicalDocument(sbmFileProcSumId, sbmrDTO);

		//create entries in SBMResponse	table		
		sbmResponseDao.createSBMResponseRecord(sbmFileProcSumId, null, physicalDocId, INITIAL);
	}


	private SBMFileStatus approveAcceptedSummaries(Long jobId, Long sbmFileProcSumId, SBMFileStatus fileStatus) {

		SBMFileStatus status = fileStatus;

		if(SBMFileStatus.ACCEPTED.equals(fileStatus)) {
			status = SBMFileStatus.APPROVED;
			updateStatusDataService.executeApproval(jobId, sbmFileProcSumId);
		}
		else if(SBMFileStatus.ACCEPTED_WITH_ERRORS.equals(fileStatus)) {
			status = SBMFileStatus.APPROVED_WITH_ERRORS;
			updateStatusDataService.executeApproval(jobId, sbmFileProcSumId);
		}
		else if(SBMFileStatus.ACCEPTED_WITH_WARNINGS.equals(fileStatus)) {
			status = SBMFileStatus.APPROVED_WITH_WARNINGS;
			updateStatusDataService.executeApproval(jobId, sbmFileProcSumId);
		}
		LOG.debug("CMS Approval NOT required. sbmFileProcSumId={}, fileStatus={}", sbmFileProcSumId, status.getValue());

		return status;
	}


	private Long savePhysicalDocument(Long sbmFileProcSumId, SbmResponseDTO sbmrDTO) throws JAXBException, SQLException, IOException {

		String tradingPartnerId = SbmHelper.getTradingPartnerId(sbmrDTO.getSbmSummaryAndFileInfo().getSbmFileInfoList());

		String sbmrXMLString = marshallSBMR(sbmrDTO.getSbmr());
		//TODO perform schema check on generated xml
		String fileName = EFTDispatchDriver.getFileID(SBMConstants.FUNCTION_CODE_SBMR);

		Long physicalDocId = eftDispatcher.saveDispatchContent(sbmrXMLString.getBytes(), fileName, SBMConstants.FUNCTION_CODE_SBMR, environmentCodeSuffix, tradingPartnerId, 0, null, TARGET_EFT_APPLICATION_TYPE); 

		logFileMsg(SBMConstants.FUNCTION_CODE_SBMR, sbmFileProcSumId, physicalDocId, eftDispatcher.getGeneratedFileName());

		return physicalDocId;
	}


	/**
	 * H4. Verify record error threshold has not been exceeded for the file/fileset 
	 * @param dto
	 * @return
	 */
	private SBMFileStatus validateErrorThreshold(SbmResponseDTO dto) {

		SBMFileStatus fileStatus = SBMFileStatus.ACCEPTED;

		BigDecimal errorThreshold = dto.getSbmSummaryAndFileInfo().getErrorThresholdPercent();
		if(errorThreshold == null) {
			errorThreshold = BigDecimal.ZERO;
		}

		BigDecimal percentRejected = getPercentRejected(dto);

		LOG.info("Compare threshold; errorThresholdAllowed:{}  percentRejected:{}", errorThreshold, percentRejected);
		if(percentRejected.compareTo(errorThreshold) > 0 ) {
			// percentRejected exceeded threshold so reject file/fileset 			
			fileStatus = SBMFileStatus.REJECTED;
		}
		else if(dto.isXprErrorsExist()) {
			fileStatus = SBMFileStatus.ACCEPTED_WITH_ERRORS;
		}
		else if(dto.isXprWarningsExist()) {
			fileStatus = SBMFileStatus.ACCEPTED_WITH_WARNINGS;
		}

		return fileStatus;
	}

	/**
	 * @param dto
	 * @param jobId
	 * @throws SQLException
	 * @throws IOException
	 * @throws JAXBException
	 */
	public void generateSBMRForUpdateStatus(SBMUpdateStatusRecordDTO dto, Long jobId) throws SQLException, IOException, JAXBException {

		Long  sbmFileProcSumId = dto.getSbmFileProcSumId();

		SBMFileStatus fileStatus = dto.getNewFileSatus();
		
		fileCompositeDao.updateFileStatus(sbmFileProcSumId, fileStatus, jobId);

		SbmResponseDTO sbmrDto = sbmResponseDao.generateSBMR(jobId, sbmFileProcSumId);

		String tradingPartnerId = SbmHelper.getTradingPartnerId(sbmrDto.getSbmSummaryAndFileInfo().getSbmFileInfoList());

		String sbmrXMLString = marshallSBMR(sbmrDto.getSbmr());

		String filename = EFTDispatchDriver.getFileID(SBMConstants.FUNCTION_CODE_SBMR);
		Long physicalDocId = eftDispatcher.saveDispatchContent(sbmrXMLString.getBytes(), filename, SBMConstants.FUNCTION_CODE_SBMR, environmentCodeSuffix, tradingPartnerId, 0, null, TARGET_EFT_APPLICATION_TYPE); 

		logFileMsg(SBMConstants.FUNCTION_CODE_SBMR, sbmFileProcSumId, physicalDocId, eftDispatcher.getGeneratedFileName());

		//create entries in SBMResponse	table		
		sbmResponseDao.createSBMResponseRecord(sbmFileProcSumId, null, physicalDocId, FINAL);
	}



	/**
	 * @param fileProcDto
	 * @throws JAXBException
	 * @throws SQLException
	 * @throws IOException
	 */
	public void generateSBMS(SBMFileProcessingDTO fileProcDto) throws JAXBException, SQLException, IOException {

		String sbmsXML = createSbmsXml(fileProcDto.getSbmFileInfo(), fileProcDto.getErrorList(), fileProcDto.getSbmFileStatusType());

		String fileName = EFTDispatchDriver.getFileID(SBMConstants.FUNCTION_CODE_SBMS);

		Long physicalDocId = eftDispatcher.saveDispatchContent(sbmsXML.getBytes(), fileName, SBMConstants.FUNCTION_CODE_SBMS, environmentCodeSuffix, fileProcDto.getSbmFileInfo().getTradingPartnerId(), 0, null, TARGET_EFT_APPLICATION_TYPE);

		logFileMsg(SBMConstants.FUNCTION_CODE_SBMS, fileProcDto.getSbmFileProcSumId(), physicalDocId, eftDispatcher.getGeneratedFileName());

		//create entries in SBMResponse	table		
		sbmResponseDao.createSBMResponseRecord(fileProcDto.getSbmFileProcSumId(), fileProcDto.getSbmFileInfo().getSbmFileInfoId(), physicalDocId, STATUS);
	}

	/**
	 * @param summaryDto
	 * @param errorList
	 * @throws JAXBException
	 * @throws SQLException
	 * @throws IOException
	 */
	public void generateSBMSForAllSBMInfos(SBMSummaryAndFileInfoDTO summaryDto, List<SBMErrorDTO> errorList) throws JAXBException, SQLException, IOException {

		for( SBMFileInfo fileInfo : summaryDto.getSbmFileInfoList()) {

			if(fileInfo.isRejectedInd()) {
				// skip rejected files
				continue;
			}

			String sbmsXML = createSbmsXml(fileInfo, errorList, summaryDto.getSbmFileStatusType());

			//dispatch sbms to eft
			String filename = EFTDispatchDriver.getFileID(SBMConstants.FUNCTION_CODE_SBMS);
			Long physicalDocId = eftDispatcher.saveDispatchContent(sbmsXML.getBytes(), filename, SBMConstants.FUNCTION_CODE_SBMS, environmentCodeSuffix, fileInfo.getTradingPartnerId(), 0, null, TARGET_EFT_APPLICATION_TYPE);

			logFileMsg(SBMConstants.FUNCTION_CODE_SBMS, summaryDto.getSbmFileProcSumId(), physicalDocId, eftDispatcher.getGeneratedFileName());

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

			for(SBMErrorDTO error: errorList) {

				gov.cms.dsh.sbms.ErrorType errorType = new gov.cms.dsh.sbms.ErrorType();
				errorType.setErrorCode(error.getSbmErrorWarningTypeCd());
				errorType.setElementInError(error.getElementInErrorNm());
				errorType.setErrorDescription(SBMCache.getErrorDescription(error.getSbmErrorWarningTypeCd()));

				if(CollectionUtils.isNotEmpty(error.getAdditionalErrorInfoList())) {

					for(String additionalInfo: error.getAdditionalErrorInfoList()) {
						errorType.getAdditionalErrorInfo().add(additionalInfo);
					}
				}
				sbms.getError().add(errorType);
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


	private String marshallSBMR(FileAcceptanceRejection far) throws JAXBException {

		StringWriter writer = new StringWriter();

		sbmrMarshaller.marshal(far, writer);
		LOG.debug("SBMR:\n{}", writer.toString());

		return writer.toString();
	}

	private void generateErrorThresholdExceeded(Long sbmFileProcSumId, String issuerFileSetId) {

		List<SBMErrorDTO> errorList = new ArrayList<>();

		List<SBMFileInfo> sbmFileInfoList = fileCompositeDao.getSbmFileInfoList(sbmFileProcSumId);

		for(SBMFileInfo fileInfo : sbmFileInfoList) {

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
	}

	private void logFileMsg(String funcType, Long sbmFileProcSumId, Long physicalDocId, String fileName) {

		LOG.info("Generated {} file,  physicalDocumentId={}\n\tsbmFileProcSumId={}, {} fileName={}",
				funcType, physicalDocId, sbmFileProcSumId, funcType, fileName);
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
