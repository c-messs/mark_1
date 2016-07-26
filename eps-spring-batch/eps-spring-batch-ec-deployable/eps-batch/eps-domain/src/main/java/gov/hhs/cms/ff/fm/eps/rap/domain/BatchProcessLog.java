package gov.hhs.cms.ff.fm.eps.rap.domain;

import java.io.Serializable;

import org.joda.time.DateTime;

/**
 * 
 * @author prasad.ghanta
 *
 */
public class BatchProcessLog implements Serializable  {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String batchBusinessId;	
	private long jobId;
	private String jobNm;
	private String jobParameterText ;
	private String jobStatusCd;	
	private DateTime startDateTime; 
	private DateTime endDateTime;
	private String runByNm;
	private	DateTime createDateTime;
	private	DateTime lastmodifiedDateTime;
	private	String createBy;
	private	String lastModifiedBy;
	private	DateTime lastpolicymaintstartDateTime;
	
	/**
	 * @return
	 */
	public DateTime getLastpolicymaintstartDateTime() {
		return lastpolicymaintstartDateTime;
	}
	public void setLastpolicymaintstartDateTime(DateTime lastpolicymaintstartDateTime) {
		this.lastpolicymaintstartDateTime = lastpolicymaintstartDateTime;
	}
	/**
	 * @return the batchBusinessId
	 */
	public String getBatchBusinessId() {
		return batchBusinessId;
	}
	/**
	 * @param batchBusinessId the batchBusinessId to set
	 */
	public void setBatchBusinessId(String batchBusinessId) {
		this.batchBusinessId = batchBusinessId;
	}
	
	/**
	 * @return the jobId
	 */
	public long getJobId() {
		return jobId;
	}
	/**
	 * @param jobId the jobId to set
	 */
	public void setJobId(long jobId) {
		this.jobId = jobId;
	}
	/**
	 * @return the jobName
	 */
	public String getJobNm() {
		return jobNm;
	}
	/**
	 * @param jobName the jobName to set
	 */
	public void setJobNm(String jobName) {
		this.jobNm = jobName;
	}
	/**
	 * @return the jobParameters
	 */
	public String getJobParameterText() {
		return jobParameterText;
	}
	/**
	 * @param jobParameters the jobParameters to set
	 */
	public void setJobParameterText(String jobParameterText) {
		this.jobParameterText = jobParameterText;
	}
	/**
	 * @return the jobStatus
	 */
	public String getJobStatusCd() {
		return jobStatusCd;
	}
	/**
	 * @param jobStatus the jobStatus to set
	 */
	public void setJobStatusCd(String jobStatus) {
		this.jobStatusCd = jobStatus;
	}
	
	/**
	 * @return the startTime
	 */
	public DateTime getStartDateTime() {
		return startDateTime;
	}
	/**
	 * @param startTime the startTime to set
	 */
	public void setStartDateTime(DateTime startDateTime) {
		this.startDateTime = startDateTime;
	}
	/**
	 * @return the endTime
	 */
	public DateTime getEndDateTime() {
		return endDateTime;
	}
	/**
	 * @param endTime the endTime to set
	 */
	public void setEndDateTime(DateTime endTime) {
		this.endDateTime = endTime;
	}
	/**
	 * @return the runBy
	 */
	public String getRunByNm() {
		return runByNm;
	}
	/**
	 * @param runBy the runBy to set
	 */
	public void setRunByNm(String runBy) {
		this.runByNm = runBy;
	}
	
	/**
	 * @return the createDateTime
	 */
	public DateTime getCreateDateTime() {
		return createDateTime;
	}
	/**
	 * @param createDateTime the createDateTime to set
	 */
	public void setCreateDateTime(DateTime createDateTime) {
		this.createDateTime = createDateTime;
	}
	/**
	 * @return the lastmodifiedDateTime
	 */
	public DateTime getLastmodifiedDateTime() {
		return lastmodifiedDateTime;
	}
	/**
	 * @param lastmodifiedDateTime the lastmodifiedDateTime to set
	 */
	public void setLastmodifiedDateTime(DateTime lastmodifiedDateTime) {
		this.lastmodifiedDateTime = lastmodifiedDateTime;
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
	 * @return the lastmodifiedBy
	 */
	public String getLastModifiedBy() {
		return lastModifiedBy;
	}
	/**
	 * @param lastmodifiedBy the lastmodifiedBy to set
	 */
	public void setLastModifiedBy(String lastmodifiedBy) {
		this.lastModifiedBy = lastmodifiedBy;
	}
     /* (non-Javadoc)
      * @see java.lang.Object#toString()
      */
     @Override
     public String toString() {
          String lineSeparator=System.getProperty("line.separator");
          StringBuilder builder = new StringBuilder();
          builder.append("BatchProcessLog [batchBusinessId=");
          builder.append(batchBusinessId+lineSeparator);
          builder.append("jobId=");  
          builder.append(jobId+lineSeparator);
          builder.append("jobNm=");
          builder.append(jobNm+lineSeparator);
          builder.append("jobStatusCd=");
          builder.append(jobStatusCd+lineSeparator);          
          builder.append("startDateTime=");
          builder.append(startDateTime+lineSeparator);
          builder.append("endDateTime=");
          builder.append(endDateTime+lineSeparator);
          builder.append("lastpolicymaintstartDateTime=");
          builder.append(lastpolicymaintstartDateTime+lineSeparator);
          builder.append("]");
          return builder.toString();
     }
}
