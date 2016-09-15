package gov.hhs.cms.ff.fm.eps.ep.po;

/**
 * @author j.radziewski
 * 
 * Entity class for  attributes of STAGINGSBMFILE table.
 * 
 *
 */
public class StagingSbmFilePO extends GenericSbmFilePO<StagingSbmFilePO>{
	
	private String sbmXML;
	private Long batchId;
	private Long sbmFileProcessingSummaryId;
	
	/**
	 * @return the sbmXML
	 */
	public String getSbmXML() {
		return sbmXML;
	}
	/**
	 * @param sbmXML the sbmXML to set
	 */
	public void setSbmXML(String sbmXML) {
		this.sbmXML = sbmXML;
	}
	/**
	 * @return the batchId
	 */
	public Long getBatchId() {
		return batchId;
	}
	/**
	 * @param batchId the batchId to set
	 */
	public void setBatchId(Long batchId) {
		this.batchId = batchId;
	}
	/**
	 * @return the sbmFileProcessingSummaryId
	 */
	public Long getSbmFileProcessingSummaryId() {
		return sbmFileProcessingSummaryId;
	}
	/**
	 * @param sbmFileProcessingSummaryId the sbmFileProcessingSummaryId to set
	 */
	public void setSbmFileProcessingSummaryId(Long sbmFileProcessingSummaryId) {
		this.sbmFileProcessingSummaryId = sbmFileProcessingSummaryId;
	}

}
