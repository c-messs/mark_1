package gov.hhs.cms.ff.fm.eps.ep.dao;

import java.util.List;

import gov.hhs.cms.ff.fm.eps.ep.enums.SbmTransMsgStatus;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmTransMsgPO;

/**
 * @author j.radziewski
 *
 */
public interface SbmTransMsgDao {

	/**
	 * Insert SbmTransMsg from PO.  
	 * NOTE: To avoid using "Clob clob = ps.getConnection().createClob();" in Java land, SBMTRANSMSG.MSG XML data
	 * is selected directly from STAGINGSBMPOLICY by using StagingSbmPolicyId.
	 * @param batchId
	 * @param stagingSbmPolicyid
	 * @param po
	 * @return
	 */
	public Long insertSbmTransMsg(Long batchId, Long stagingSbmPolicyid, SbmTransMsgPO po);
	
	
	/**
	 * @param sbmFileInfo
	 * @return
	 */
	public List<SbmTransMsgPO> selectSbmTransMsg(Long sbmFileInfo);
	
	
	/**
	 * @param sbmFileProcSumId
	 * @return
	 */
	public Integer selectRejectCount(Long sbmFileProcSumId);
	
	
	/**
	 * @param sbmFileProcSumId
	 * @param status
	 * @return
	 */
	public Integer selectMatchCount(Long sbmFileProcSumId, SbmTransMsgStatus status);
	
	
	/**
	 * @param sbmFileProcSumId
	 * @return
	 */
	public Integer selectMatchCountCorrected(Long sbmFileProcSumId);
	
	
	/**
	 * @param sbmFileProcSumId
	 * @param status
	 * @return
	 */
	public Integer selectNoMatchCount(Long sbmFileProcSumId, SbmTransMsgStatus status);
	
	
}
