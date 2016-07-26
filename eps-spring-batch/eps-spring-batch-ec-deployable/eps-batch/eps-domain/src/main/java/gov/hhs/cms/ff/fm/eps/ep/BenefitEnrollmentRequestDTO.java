package gov.hhs.cms.ff.fm.eps.ep;

import gov.cms.dsh.bem.BenefitEnrollmentRequest;
import gov.cms.dsh.bem.FileInformationType;
import gov.hhs.cms.ff.fm.eps.ep.enums.TxnMessageDirectionType;
import gov.hhs.cms.ff.fm.eps.ep.enums.TxnMessageType;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlTransient;

import org.joda.time.DateTime;

/**
 * @author eps
 *
 */
public class BenefitEnrollmentRequestDTO {

	
	public BenefitEnrollmentRequestDTO(){
		//super()
	}

	private String fileNm;
	private String fileInfoXml;
	private String berXml;
	private BenefitEnrollmentRequest ber;
	private DateTime fileNmDateTime;
	private Long txnMessageFileInfoId;
	private FileInformationType fileInformation;
	private Long batchId;
	private Long ingestJobId;
	private String batchRunControlId;
	private TxnMessageDirectionType txnMessageDirectionType;
	private TxnMessageType txnMessageType;
	private boolean isQHPInd;
	private boolean insertFileInfo;
	private String exchangeTypeCd;
	private JAXBElement<?> jaxbElement;
	private DateTime sourceVersionDateTime;
	private boolean versionSkippedInPast = false;
	private String subscriberStateCd;
	private String planId;
	private boolean isIgnore = false;
	

	@XmlTransient
	public JAXBElement<?> getJaxbElement() {
		return jaxbElement;
	}

	public void setJaxbElement(JAXBElement<?> jaxbElement) {
		this.jaxbElement = jaxbElement;
	}

	public String getFileNm() {
		return fileNm;
	}

	public void setFileNm(String fileNm) {
		this.fileNm = fileNm;
	}

	public DateTime getFileNmDateTime() {
		return fileNmDateTime;
	}

	public void setFileNmDateTime(DateTime fileNmDateTime) {
		this.fileNmDateTime = fileNmDateTime;
	}

	/**
	 * @return the fileInformation
	 */
	public FileInformationType getFileInformation() {
		return fileInformation;
	}

	/**
	 * @param fileInformation the fileInformation to set
	 */
	public void setFileInformation(FileInformationType fileInformation) {
		this.fileInformation = fileInformation;
	}

	public Long getBatchId() {
		return batchId;
	}

	public void setBatchId(Long batchId) {
		this.batchId = batchId;
	}

	/**
	 * @return the ingestJobId
	 */
	public Long getIngestJobId() {
		return ingestJobId;
	}

	/**
	 * @param ingestJobId the ingestJobId to set
	 */
	public void setIngestJobId(Long ingestJobId) {
		this.ingestJobId = ingestJobId;
	}

	/**
	 * @return the batchRunControlId
	 */
	public String getBatchRunControlId() {
		return batchRunControlId;
	}

	/**
	 * @param batchRunControlId the batchRunControlId to set
	 */
	public void setBatchRunControlId(String batchRunControlId) {
		this.batchRunControlId = batchRunControlId;
	}

	public TxnMessageDirectionType getTxnMessageDirectionType() {
		return txnMessageDirectionType;
	}

	public void setTxnMessageDirectionType(TxnMessageDirectionType txnMessageDirectionType) {
		this.txnMessageDirectionType = txnMessageDirectionType;
	}

	public TxnMessageType getTxnMessageType() {
		return txnMessageType;
	}

	public void setTxnMessageType(TxnMessageType txnMessageType) {
		this.txnMessageType = txnMessageType;
	}

	/**
	 * @return the fileInfoXml
	 */
	public String getFileInfoXml() {
		return fileInfoXml;
	}

	/**
	 * @param fileInfoXml the fileInfoXml to set
	 */
	public void setFileInfoXml(String fileInfoXml) {
		this.fileInfoXml = fileInfoXml;
	}

	/**
	 * @return the insertFileInfo
	 */
	public boolean isInsertFileInfo() {
		return insertFileInfo;
	}

	/**
	 * @param insertFileInfo the insertFileInfo to set
	 */
	public void setInsertFileInfo(boolean insertFileInfo) {
		this.insertFileInfo = insertFileInfo;
	}

	/**
	 * @return the txnMessageFileInfoId
	 */
	public Long getTxnMessageFileInfoId() {
		return txnMessageFileInfoId;
	}

	/**
	 * @param txnMessageFileInfoId the txnMessageFileInfoId to set
	 */
	public void setTxnMessageFileInfoId(Long txnMessageFileInfoId) {
		this.txnMessageFileInfoId = txnMessageFileInfoId;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "BenefitEnrollmentRequestDTO [\n\tfileNm=" + fileNm
				+ "\n\tfileInfoXml=" + fileInfoXml + "\n\tberXml=" + berXml
				+ "\n\tber=" + ber + "\n\tfileNmDateTime=" + fileNmDateTime
				+ "\n\ttxnMessageFileInfoId=" + txnMessageFileInfoId
				+ "\n\tfileInformation=" + fileInformation + "\n\tbatchId="
				+ batchId + "\n\tingestJobId=" + ingestJobId
				+ "\n\tbatchRunControlId=" + batchRunControlId
				+ "\n\ttxnMessageDirectionType=" + txnMessageDirectionType
				+ "\n\ttxnMessageType=" + txnMessageType + "\n\tisQHPInd="
				+ isQHPInd + "\n\tinsertFileInfo=" + insertFileInfo
				+ "\n\texchangeTypeCd=" + exchangeTypeCd + "\n\tjaxbElement="
				+ jaxbElement + "\n\tsourceVersionDateTime="
				+ sourceVersionDateTime + "\n\tversionSkippedInPast="
				+ versionSkippedInPast + "\n\tsubscriberStateCd="
				+ subscriberStateCd + "\n\tplanId=" + planId + "\n\tisIgnore="
				+ isIgnore + "]";
	}

	/**
	 * @return the isQHPInd
	 */
	public boolean isQHPInd() {
		return isQHPInd;
	}

	/**
	 * @param isQHPInd the isQHPInd to set
	 */
	public void setQHPInd(boolean isQHPInd) {
		this.isQHPInd = isQHPInd;
	}

	/**
	 * @return the exchangeTypeCd
	 */
	public String getExchangeTypeCd() {
		return exchangeTypeCd;
	}

	/**
	 * @param string the exchangeTypeCd to set
	 */
	public void setExchangeTypeCd(String exchangeTypeCd) {
		this.exchangeTypeCd = exchangeTypeCd;
	}
	
	/**
	 * @return the sourceVersionDateTime
	 */
	public DateTime getSourceVersionDateTime() {
		return sourceVersionDateTime;
	}

	/**
	 * @param sourceVersionDateTime the sourceVersionDateTime to set
	 */
	public void setSourceVersionDateTime(DateTime sourceVersionDateTime) {
		this.sourceVersionDateTime = sourceVersionDateTime;
	}

	/**
	 * @return the versionSkippedInPast
	 */
	public boolean isVersionSkippedInPast() {
		return versionSkippedInPast;
	}

	/**
	 * @param versionSkippedInPast the versionSkippedInPast to set
	 */
	public void setVersionSkippedInPast(boolean versionSkippedInPast) {
		this.versionSkippedInPast = versionSkippedInPast;
	}

    /**
	 * @return the subscriberStateCd
	 */
	public String getSubscriberStateCd() {
		return subscriberStateCd;
	}

	/**
	 * @param subscriberStateCd the subscriberStateCd to set
	 */
	public void setSubscriberStateCd(String subscriberStateCd) {
		this.subscriberStateCd = subscriberStateCd;
	}

	/**
	 * @return the planId
	 */
	public String getPlanId() {
		return planId;
	}

	/**
	 * @param planId the planId to set
	 */
	public void setPlanId(String planId) {
		this.planId = planId;
	}

	/**
	 * @return the isIgnore
	 */
	public boolean isIgnore() {
		return isIgnore;
	}

	/**
	 * @param isIgnore the isIgnore to set
	 */
	public void setIgnore(boolean isIgnore) {
		this.isIgnore = isIgnore;
	}
	
	public String getBerXml() {
		return berXml;
	}

	public void setBerXml(String berXml) {
		this.berXml = berXml;
	}

	public BenefitEnrollmentRequest getBer() {
		return ber;
	}

	public void setBer(BenefitEnrollmentRequest ber) {
		this.ber = ber;
	}

}
