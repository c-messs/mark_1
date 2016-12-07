package gov.hhs.cms.ff.fm.eps.ep.dao;

import java.util.List;

import gov.hhs.cms.ff.fm.eps.ep.po.SbmTransMsgCountData;
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
	 * @param stagingSbmPolicyid
	 * @param po
	 * @return
	 */
	public Long insertSbmTransMsg(Long stagingSbmPolicyid, SbmTransMsgPO po);
	
	
	/**
	 * @param sbmFileInfo
	 * @return
	 */
	public List<SbmTransMsgPO> selectSbmTransMsg(Long sbmFileInfo);
	
	
	/**
	 * Select only count of SbmTransMsgs with a status of RJC. 
	 * StateCd is included to make sure we only hit the needed partition.
	 * @param sbmFileProcSumId
	 * @param stateCd
	 * @return
	 */
	public Integer selectRejectCount(Long sbmFileProcSumId, String stateCd);
	
	
	/**
	 * Selects counts by status, hasPolicyVersionId, and hasPriorPolicyVersionId for 
	 * setting "new" and "matching" counts in summary. StateCd is included to make sure
	 * we only hit the needed partition.
	 * Expected record format similar to the following:
	 * Status hasPvId  hasPPvId  cntStatus
	 *  ACC    1          0        n
	 *  AEC	   1          1        n
	 * @param sbmFileProcSumId
	 * @param stateCd
	 * @return
	 */
	public List<SbmTransMsgCountData> selectSbmTransMsgCounts(Long sbmFileProcSumId, String stateCd);
}
