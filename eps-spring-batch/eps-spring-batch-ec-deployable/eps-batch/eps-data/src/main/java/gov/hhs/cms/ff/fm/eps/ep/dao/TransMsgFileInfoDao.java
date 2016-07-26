package gov.hhs.cms.ff.fm.eps.ep.dao;

import gov.hhs.cms.ff.fm.eps.ep.po.TransMsgFileInfoPO;

/**
 * @author eps
 *
 */
public interface TransMsgFileInfoDao {
	
	/**
	 * @param tmfiPO
	 * @param berXml
	 * @return
	 */
	public Long insertTransMsgFileInfo(TransMsgFileInfoPO tmfiPO, String berXml);

}
