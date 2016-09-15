package gov.hhs.cms.ff.fm.eps.ep.dao;

import gov.hhs.cms.ff.fm.eps.ep.po.SbmFileArchivePO;

/**
 * @author j.radziewski
 * Entity DAO for table SBMFILEARCHIVE.
 *
 */
public interface SbmFileArchiveDao {
	
	/**
	 * @param po
	 * @return
	 */
	public boolean saveFileToArchive(final SbmFileArchivePO po);

}
