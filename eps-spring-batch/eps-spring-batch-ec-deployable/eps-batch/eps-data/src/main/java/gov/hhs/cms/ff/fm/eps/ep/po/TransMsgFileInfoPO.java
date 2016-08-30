package gov.hhs.cms.ff.fm.eps.ep.po;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author eps
 *
 */
public class TransMsgFileInfoPO {
	
	private Long transMsgFileInfoId;
	private String fileInfoXML;
	private String groupSenderId ;
	private String groupReceiverId;
	private String fileNm;
	private LocalDateTime groupTimestampDateTime;
	private String groupControlNum;
	private String versionNum;
	private String transMsgOriginTypeCd;
	private LocalDate createDateTime;
	private LocalDate lastModifiedDateTime;
	private String createBy;
	private String lastModifiedBy;
	/**
	 * @return the transMsgFileInfoId
	 */
	public Long getTransMsgFileInfoId() {
		return transMsgFileInfoId;
	}
	/**
	 * @param transMsgFileInfoId the transMsgFileInfoId to set
	 */
	public void setTransMsgFileInfoId(Long transMsgFileInfoId) {
		this.transMsgFileInfoId = transMsgFileInfoId;
	}
	/**
	 * @return the fileInfoXML
	 */
	public String getFileInfoXML() {
		return fileInfoXML;
	}
	/**
	 * @param fileInfoXML the fileInfoXML to set
	 */
	public void setFileInfoXML(String fileInfoXML) {
		this.fileInfoXML = fileInfoXML;
	}
	/**
	 * @return the groupSenderId
	 */
	public String getGroupSenderId() {
		return groupSenderId;
	}
	/**
	 * @param groupSenderId the groupSenderId to set
	 */
	public void setGroupSenderId(String groupSenderId) {
		this.groupSenderId = groupSenderId;
	}
	/**
	 * @return the groupReceiverId
	 */
	public String getGroupReceiverId() {
		return groupReceiverId;
	}
	/**
	 * @param groupReceiverId the groupReceiverId to set
	 */
	public void setGroupReceiverId(String groupReceiverId) {
		this.groupReceiverId = groupReceiverId;
	}
	/**
	 * @return the fileNm
	 */
	public String getFileNm() {
		return fileNm;
	}
	/**
	 * @param fileNm the fileNm to set
	 */
	public void setFileNm(String fileNm) {
		this.fileNm = fileNm;
	}
	/**
	 * @return the groupTimestampDateTime
	 */
	public LocalDateTime getGroupTimestampDateTime() {
		return groupTimestampDateTime;
	}
	/**
	 * @param groupTimestampDateTime the groupTimestampDateTime to set
	 */
	public void setGroupTimestampDateTime(LocalDateTime groupTimestampDateTime) {
		this.groupTimestampDateTime = groupTimestampDateTime;
	}
	/**
	 * @return the groupControlNum
	 */
	public String getGroupControlNum() {
		return groupControlNum;
	}
	/**
	 * @param groupControlNum the groupControlNum to set
	 */
	public void setGroupControlNum(String groupControlNum) {
		this.groupControlNum = groupControlNum;
	}
	/**
	 * @return the versionNum
	 */
	public String getVersionNum() {
		return versionNum;
	}
	/**
	 * @param versionNum the versionNum to set
	 */
	public void setVersionNum(String versionNum) {
		this.versionNum = versionNum;
	}
	/**
	 * @return the transMsgOriginTypeCd
	 */
	public String getTransMsgOriginTypeCd() {
		return transMsgOriginTypeCd;
	}
	/**
	 * @param transMsgOriginTypeCd the transMsgOriginTypeCd to set
	 */
	public void setTransMsgOriginTypeCd(String transMsgOriginTypeCd) {
		this.transMsgOriginTypeCd = transMsgOriginTypeCd;
	}
	/**
	 * @return the createDateTime
	 */
	public LocalDate getCreateDateTime() {
		return createDateTime;
	}
	/**
	 * @param createDateTime the createDateTime to set
	 */
	public void setCreateDateTime(LocalDate createDateTime) {
		this.createDateTime = createDateTime;
	}
	/**
	 * @return the lastModifiedDateTime
	 */
	public LocalDate getLastModifiedDateTime() {
		return lastModifiedDateTime;
	}
	/**
	 * @param lastModifiedDateTime the lastModifiedDateTime to set
	 */
	public void setLastModifiedDateTime(LocalDate lastModifiedDateTime) {
		this.lastModifiedDateTime = lastModifiedDateTime;
	}
	/**
	 * @return the createBy
	 */
	public String getCreateBy() {
		return createBy;
	}
	/**
	 * @param createBy the createBy to set
	 */
	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}
	/**
	 * @return the lastModifiedBy
	 */
	public String getLastModifiedBy() {
		return lastModifiedBy;
	}
	/**
	 * @param lastModifiedBy the lastModifiedBy to set
	 */
	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}
	
	
}
