package gov.hhs.cms.ff.fm.eps.ep.dao;

import java.util.List;

import gov.hhs.cms.ff.fm.eps.ep.po.SbmFileErrorAdditionalInfoPO;

public interface SbmFileErrorAdditionalInfoDao {
	
	/**
	 * @param errList
	 */
	public void insertSbmFileErrAddlInfoList (final List<SbmFileErrorAdditionalInfoPO> errList);

}
