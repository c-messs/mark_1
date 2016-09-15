package gov.hhs.cms.ff.fm.eps.ep.dao;

import java.util.List;

import gov.hhs.cms.ff.fm.eps.ep.po.SbmTransMsgAdditionalErrorInfoPO;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmTransMsgValidationPO;

/**
 * @author j.radziewski
 *
 */
public interface SbmTransMsgAdditionalErrorInfoDao {
	
	
	/**
	 * @param errList
	 */
	public void insertSbmTransMsgAddlErrInfoList(List<SbmTransMsgAdditionalErrorInfoPO> errList);

	
	/**
	 * @param valPO
	 * @return
	 */
	public List<SbmTransMsgAdditionalErrorInfoPO> selectSbmTransMsgAddlErrInfo(final SbmTransMsgValidationPO valPO);
}
