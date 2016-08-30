package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm;

import static gov.hhs.cms.ff.fm.eps.ep.enums.SBMErrorWarningCode.ER_015;
import static gov.hhs.cms.ff.fm.eps.ep.enums.SBMErrorWarningCode.ER_016;
import static gov.hhs.cms.ff.fm.eps.ep.enums.SBMErrorWarningCode.ER_017;
import static gov.hhs.cms.ff.fm.eps.ep.enums.SBMErrorWarningCode.ER_018;
import static gov.hhs.cms.ff.fm.eps.ep.enums.SBMErrorWarningCode.ER_019;
import static gov.hhs.cms.ff.fm.eps.ep.enums.SBMErrorWarningCode.ER_020;
import static gov.hhs.cms.ff.fm.eps.ep.enums.SBMErrorWarningCode.ER_021;
import static gov.hhs.cms.ff.fm.eps.ep.enums.SBMErrorWarningCode.ER_022;
import static gov.hhs.cms.ff.fm.eps.ep.enums.SBMErrorWarningCode.ER_997;
import static gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus.EXPIRED;
import static gov.hhs.cms.ff.fm.eps.ep.enums.SBMPolicyEnum.COVERAGE_YEAR;
import static gov.hhs.cms.ff.fm.eps.ep.enums.SBMPolicyEnum.FILE_ID;
import static gov.hhs.cms.ff.fm.eps.ep.enums.SBMPolicyEnum.FILE_NUM;
import static gov.hhs.cms.ff.fm.eps.ep.enums.SBMPolicyEnum.ISS_FILE_SET_ID;
import static gov.hhs.cms.ff.fm.eps.ep.enums.SBMPolicyEnum.TOT_ISS_FILES;
import static gov.hhs.cms.ff.fm.eps.ep.jobs.sbm.SbmHelper.createErrorLog;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.cms.dsh.sbmi.FileInformationType.IssuerFileInformation;
import gov.hhs.cms.ff.fm.eps.ep.StateProrationConfiguration;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMCache;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMConstants;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileProcessingDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMSummaryAndFileInfoDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SBMFileCompositeDAO;

public class SbmFileValidator {
	
	private static final Logger LOG = LoggerFactory.getLogger(SbmFileValidator.class);
	
	private Integer coverageYear;
	private SBMFileCompositeDAO fileCompositeDao;

	public void validate(SBMFileProcessingDTO dto) {
		
		LOG.info("Performing file level validations on file:{}", dto.getSbmFileInfo().getSbmFileNm());
		
		//validate is state allowed for SBM submission
		validateStateAllowedForSBM(dto);  
		
		//validate coverageYear
		validateCoverageYear(dto);
		
		validateFileIdAndTenantId(dto);
		
		validateFileNumber(dto);
		
		validateFileNumberExceeded(dto);
		
		validateTotalIssuerFileCount(dto);
				
		validateFileSetExpired(dto);
			
		LOG.info("File level Errors: {}", dto.getErrorList());
	}
		
	private void validateStateAllowedForSBM(SBMFileProcessingDTO dto) {
		String stateCd = dto.getFileInfoType().getTenantId().substring(0, 2);
		int coverageYear = dto.getFileInfoType().getCoverageYear();
		
		StateProrationConfiguration stateConfig = SBMCache.getStateProrationConfiguration(coverageYear, stateCd);
		
		if(stateConfig == null) {
			dto.getErrorList().add(createErrorLog(null, ER_997.getCode()));
		}		
	}
	
	private void validateCoverageYear(SBMFileProcessingDTO dto) {
		
		int coverageYearFromFile = dto.getFileInfoType().getCoverageYear();
		
		//check if coverageYearFromFile is less than coverage year defined in property file
		if(coverageYearFromFile < coverageYear) {
			dto.getErrorList().add(createErrorLog(COVERAGE_YEAR.getElementNm(), ER_015.getCode(), ""+coverageYearFromFile, SBMConstants.ERRORMSG_COVERAGEYEAR));
		}
		
		LocalDate today = LocalDate.now();
		if(coverageYearFromFile == (today.getYear() + 1)) {
			if( (today.getMonthValue() != 11) || (today.getMonthValue() != 12) ) {
				dto.getErrorList().add(createErrorLog(COVERAGE_YEAR.getElementNm(), ER_017.getCode(), ""+coverageYearFromFile));
			}
		}
		else if(coverageYearFromFile > today.getYear()) {
			dto.getErrorList().add(createErrorLog(COVERAGE_YEAR.getElementNm(), ER_016.getCode(), ""+coverageYearFromFile));
		}
				
	}
	
	private void validateFileIdAndTenantId(SBMFileProcessingDTO dto) {		
		
		List<SBMSummaryAndFileInfoDTO> summaryDtoList = fileCompositeDao.performSbmFileMatch(dto.getFileInfoType().getFileId(), dto.getFileInfoType().getTenantId());

		if( SbmHelper.isNotRejectedNotDisapproved(summaryDtoList)) {
			dto.getErrorList().add(createErrorLog(FILE_ID.getElementNm(), ER_018.getCode(), dto.getFileInfoType().getFileId()));
			return;
		}
	}
	
	private void validateFileNumber(SBMFileProcessingDTO dto) {

		IssuerFileInformation issuerInfo = dto.getFileInfoType().getIssuerFileInformation();

		if(issuerInfo != null && issuerInfo.getIssuerFileSet() != null) {			
			List<SBMSummaryAndFileInfoDTO> summaryDtoList = fileCompositeDao.findSbmFileInfo(issuerInfo.getIssuerFileSet().getIssuerFileSetId(), issuerInfo.getIssuerFileSet().getFileNumber());
			if( SbmHelper.isNotRejectedNotDisapproved(summaryDtoList)) {
				dto.getErrorList().add(createErrorLog(FILE_NUM.getElementNm(), ER_019.getCode(), ""+issuerInfo.getIssuerFileSet().getFileNumber()));
				return;
			}	
		}
	}
	
	private void validateTotalIssuerFileCount(SBMFileProcessingDTO dto) {
		
		IssuerFileInformation issuerInfo = dto.getFileInfoType().getIssuerFileInformation();		
		if(issuerInfo != null && issuerInfo.getIssuerFileSet() != null) {			
			if( dto.getFileProcSummaryFromDB() != null && dto.getFileProcSummaryFromDB().getTotalIssuerFileCount() != issuerInfo.getIssuerFileSet().getTotalIssuerFiles()) {
				String errorMsg2 = SBMConstants.ERRORMSG_EXPECTED_VALUE + dto.getFileProcSummaryFromDB().getTotalIssuerFileCount();
				dto.getErrorList().add(createErrorLog(TOT_ISS_FILES.getElementNm(), ER_020.getCode(), ""+issuerInfo.getIssuerFileSet().getTotalIssuerFiles(), errorMsg2));
			}	
		}		
	}
	
	private void validateFileNumberExceeded(SBMFileProcessingDTO dto) {
		
		IssuerFileInformation issuerInfo = dto.getFileInfoType().getIssuerFileInformation();		
		if(issuerInfo != null && issuerInfo.getIssuerFileSet() != null) {
			if( dto.getFileProcSummaryFromDB() != null && issuerInfo.getIssuerFileSet().getFileNumber() > dto.getFileProcSummaryFromDB().getTotalIssuerFileCount()) {
				dto.getErrorList().add(createErrorLog(FILE_NUM.getElementNm(), ER_021.getCode(), ""+issuerInfo.getIssuerFileSet().getFileNumber()));
			}	
		}		
	}
	
	private void validateFileSetExpired(SBMFileProcessingDTO dto) {
		IssuerFileInformation issuerInfo = dto.getFileInfoType().getIssuerFileInformation();		
		if(issuerInfo != null && issuerInfo.getIssuerFileSet() != null) {
			if( dto.getFileProcSummaryFromDB() != null && EXPIRED.equals(dto.getFileProcSummaryFromDB().getSbmFileStatusType())) {
				dto.getErrorList().add(createErrorLog(ISS_FILE_SET_ID.getElementNm(), ER_022.getCode(), ""+issuerInfo.getIssuerFileSet().getIssuerFileSetId()));
			}	
		}	
	}

	/**
	 * @param coverageYear the coverageYear to set
	 */
	public void setCoverageYear(Integer coverageYear) {
		this.coverageYear = coverageYear;
	}

	/**
	 * @param fileCompositeDao the fileCompositeDao to set
	 */
	public void setFileCompositeDao(SBMFileCompositeDAO fileCompositeDao) {
		this.fileCompositeDao = fileCompositeDao;
	}
	
}
