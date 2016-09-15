package gov.hhs.cms.ff.fm.eps.ep.dao;

import java.util.List;

import gov.hhs.cms.ff.fm.eps.ep.po.SbmFileErrorPO;

/**
 * @author j.radziewski
 * 
 * Entity DAO for table SBMFILEERROR.
 *
 */
public interface SbmFileErrorDao {
	
	
	/**
	 * @param errList
	 */
	public void insertSbmFileErrorList (final List<SbmFileErrorPO> errList);
		

}
