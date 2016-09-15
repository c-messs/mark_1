package gov.hhs.cms.ff.fm.eps.ep.dao;

import java.util.List;

import gov.hhs.cms.ff.fm.eps.ep.po.SbmTransMsgValidationPO;

/**
 * @author j.radziewski
 *
 */
public interface SbmTransMsgValidationDao  {
	
	/**
	 * @param errList
	 */
	public void insertSbmTransMsgValidationList (final List<SbmTransMsgValidationPO> errList);
	
	/**
	 * @param sbmFileProcSumId
	 * @return
	 */
	public boolean verifyXprErrorsExist(Long sbmFileProcSumId);
	
	/**
	 * @param sbmFileProcSumId
	 * @return
	 */
	public boolean verifyXprWarningsExist(Long sbmFileProcSumId);
	
	
	/**
	 * @param sbmTransMsgId
	 * @return
	 */
	public List<SbmTransMsgValidationPO> selectValidation(Long sbmTransMsgId);

}
