package gov.hhs.cms.ff.fm.eps.ep.po;

import org.joda.time.DateTime;

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
	private DateTime groupTimestampDateTime;
	private String groupControlNum;
	private String versionNum;
	private String transMsgOriginTypeCd;
	private DateTime createDateTime;
	private DateTime lastModifiedDateTime;
	private String createBy;
	private String lastModifiedBy;
	
	public Long getTransMsgFileInfoId() {
		return transMsgFileInfoId;
	}
	public void setTransMsgFileInfoId(Long transMsgFileInfoId) {
		this.transMsgFileInfoId = transMsgFileInfoId;
	}
	public String getFileInfoXML() {
		return fileInfoXML;
	}
	public void setFileInfoXML(String fileInfoXML) {
		this.fileInfoXML = fileInfoXML;
	}
	public String getGroupSenderId() {
		return groupSenderId;
	}
	public void setGroupSenderId(String groupSenderId) {
		this.groupSenderId = groupSenderId;
	}
	public String getGroupReceiverId() {
		return groupReceiverId;
	}
	public void setGroupReceiverId(String groupReceiverId) {
		this.groupReceiverId = groupReceiverId;
	}
	public String getFileNm() {
		return fileNm;
	}
	public void setFileNm(String fileNm) {
		this.fileNm = fileNm;
	}
	public DateTime getGroupTimestampDateTime() {
		return groupTimestampDateTime;
	}
	public void setGroupTimestampDateTime(DateTime groupTimestampDateTime) {
		this.groupTimestampDateTime = groupTimestampDateTime;
	}
	public String getGroupControlNum() {
		return groupControlNum;
	}
	public void setGroupControlNum(String groupControlNum) {
		this.groupControlNum = groupControlNum;
	}
	public String getVersionNum() {
		return versionNum;
	}
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
	public DateTime getCreateDateTime() {
		return createDateTime;
	}
	public void setCreateDateTime(DateTime createDateTime) {
		this.createDateTime = createDateTime;
	}
	public DateTime getLastModifiedDateTime() {
		return lastModifiedDateTime;
	}
	public void setLastModifiedDateTime(DateTime lastModifiedDateTime) {
		this.lastModifiedDateTime = lastModifiedDateTime;
	}
	public String getCreateBy() {
		return createBy;
	}
	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}
	public String getLastModifiedBy() {
		return lastModifiedBy;
	}
	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}
}
