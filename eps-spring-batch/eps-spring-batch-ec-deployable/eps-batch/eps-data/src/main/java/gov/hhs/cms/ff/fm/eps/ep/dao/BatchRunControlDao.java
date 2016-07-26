/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.dao;


/**
 * Interface for BatchRunControl Data Access
 * 
 * @author girish.padmanabhan
 *
 */
public interface BatchRunControlDao {

	/**
	 * This method retrieves the Latest PreAudit Extract Indicator
	 * 
	 * @return String
	 */
	public String getPreAuditExtractStatus();
	
}
