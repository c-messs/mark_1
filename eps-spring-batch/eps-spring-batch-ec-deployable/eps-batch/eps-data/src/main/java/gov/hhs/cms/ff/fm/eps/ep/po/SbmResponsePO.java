package gov.hhs.cms.ff.fm.eps.ep.po;

/**
 * @author j.radziewski
 * 
 * Entity class for SBM specific attributes of SBMRESPONSE table.
 * 
 *
 */
public class SbmResponsePO {
	
	private Long sbmFileProcSumId;
	private Long sbmFileInfoId;
	private Long physicalDocumentId;
	private String responseCd;
	/**
	 * @return the sbmFileProcSumId
	 */
	public Long getSbmFileProcSumId() {
		return sbmFileProcSumId;
	}
	/**
	 * @param sbmFileProcSumId the sbmFileProcSumId to set
	 */
	public void setSbmFileProcSumId(Long sbmFileProcSumId) {
		this.sbmFileProcSumId = sbmFileProcSumId;
	}
	/**
	 * @return the sbmFileInfoId
	 */
	public Long getSbmFileInfoId() {
		return sbmFileInfoId;
	}
	/**
	 * @param sbmFileInfoId the sbmFileInfoId to set
	 */
	public void setSbmFileInfoId(Long sbmFileInfoId) {
		this.sbmFileInfoId = sbmFileInfoId;
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
	 * @return the responseCd
	 */
	public String getResponseCd() {
		return responseCd;
	}
	/**
	 * @param responseCd the responseCd to set
	 */
	public void setResponseCd(String responseCd) {
		this.responseCd = responseCd;
	}
	

}
