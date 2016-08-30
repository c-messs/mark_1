package gov.hhs.cms.ff.fm.eps.ep.dao;

import gov.hhs.cms.ff.fm.eps.ep.po.SbmFileArchivePO;

public interface SbmFileArchiveDao {
	
	public boolean saveFileToArchive(final SbmFileArchivePO po);

}
