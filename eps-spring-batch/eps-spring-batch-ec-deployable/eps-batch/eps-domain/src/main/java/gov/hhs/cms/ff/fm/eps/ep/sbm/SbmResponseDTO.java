package gov.hhs.cms.ff.fm.eps.ep.sbm;

import java.math.BigDecimal;

import gov.cms.dsh.sbmr.FileAcceptanceRejection;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMResponsePhaseTypeCode;

/**
 * Holds SBM Response data
 *
 */
public class SbmResponseDTO {
	
	private SBMSummaryAndFileInfoDTO sbmSummaryAndFileInfo;
	private FileAcceptanceRejection sbmr;	
	private boolean xprErrorsExist;	
	private boolean xprWarningsExist;
	private SBMResponsePhaseTypeCode sbmResponsePhaseTypeCd; 
	private Long physicalDocumentId;
	private BigDecimal totalRecordsProcessed;
	private BigDecimal totalRecordsRejected;
	
	
	/**
	 * @return the sbmSummaryAndFileInfo
	 */
	public SBMSummaryAndFileInfoDTO getSbmSummaryAndFileInfo() {
		return sbmSummaryAndFileInfo;
	}

	/**
	 * @param sbmSummaryAndFileInfo the sbmSummaryAndFileInfo to set
	 */
	public void setSbmSummaryAndFileInfo(SBMSummaryAndFileInfoDTO sbmSummaryAndFileInfo) {
		this.sbmSummaryAndFileInfo = sbmSummaryAndFileInfo;
	}

	/**
	 * @return the sbmr
	 */
	public FileAcceptanceRejection getSbmr() {
		return sbmr;
	}

	/**
	 * @param sbmr the sbmr to set
	 */
	public void setSbmr(FileAcceptanceRejection sbmr) {
		this.sbmr = sbmr;
	}
	
	/**
	 * @return the xprErrorsExist
	 */
	public boolean isXprErrorsExist() {
		return xprErrorsExist;
	}

	/**
	 * @param xprErrorsExist the xprErrorsExist to set
	 */
	public void setXprErrorsExist(boolean xprErrorsExist) {
		this.xprErrorsExist = xprErrorsExist;
	}

	/**
	 * @return the xprWarningsExist
	 */
	public boolean isXprWarningsExist() {
		return xprWarningsExist;
	}

	/**
	 * @param xprWarningsExist the xprWarningsExist to set
	 */
	public void setXprWarningsExist(boolean xprWarningsExist) {
		this.xprWarningsExist = xprWarningsExist;
	}	
		
	/**
	 * @return the sbmResponsePhaseTypeCd
	 */
	public SBMResponsePhaseTypeCode getSbmResponsePhaseTypeCd() {
		return sbmResponsePhaseTypeCd;
	}

	/**
	 * @param sbmResponsePhaseTypeCd the sbmResponsePhaseTypeCd to set
	 */
	public void setSbmResponsePhaseTypeCd(SBMResponsePhaseTypeCode sbmResponsePhaseTypeCd) {
		this.sbmResponsePhaseTypeCd = sbmResponsePhaseTypeCd;
	}

	/**
	 * @return the physicalDocumentId
	 */
	public Long getPhysicalDocumentId() {
		return physicalDocumentId;
	}

	/**
	 * @param physicalDocumentId the physicalDocumentId to set
	 */
	public void setPhysicalDocumentId(Long physicalDocumentId) {
		this.physicalDocumentId = physicalDocumentId;
	}

	/**
	 * @return the totalRecordsProcessed
	 */
	public BigDecimal getTotalRecordsProcessed() {
		return totalRecordsProcessed;
	}

	/**
	 * @param totalRecordsProcessed the totalRecordsProcessed to set
	 */
	public void setTotalRecordsProcessed(BigDecimal totalRecordsProcessed) {
		this.totalRecordsProcessed = totalRecordsProcessed;
	}

	/**
	 * @return the totalRecordsRejected
	 */
	public BigDecimal getTotalRecordsRejected() {
		return totalRecordsRejected;
	}

	/**
	 * @param totalRecordsRejected the totalRecordsRejected to set
	 */
	public void setTotalRecordsRejected(BigDecimal totalRecordsRejected) {
		this.totalRecordsRejected = totalRecordsRejected;
	}


}
