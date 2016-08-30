package gov.hhs.cms.ff.fm.eps.ep.dao;

import java.math.BigInteger;
import java.util.List;

import gov.hhs.cms.ff.fm.eps.ep.po.SbmFileInfoPO;

/**
 * @author j.radziewski
 *
 */
public interface SbmFileInfoDao {
	
	
	
	/**
	 * @param po
	 */
	public Long insertSBMFileInfo(final SbmFileInfoPO po);
	
	
	/**
	 * @param fileSetId
	 * @param fileNumber
	 * @return
	 */
	public List<SbmFileInfoPO> performFileMatch(String fileSetId, int fileNumber);
	

	/**
	 * @param fileName
	 * @return
	 */
	public List<String> getFileStatusList(String fileName);
	
	
	/**
	 * @param sbmFileInfoId
	 * @return
	 */
	public String selectFileInfoXml(Long sbmFileInfoId);
	
	
	/**
	 * Get all SbmFileInfoPOs for the given sbmFileProcSumId
	 * @param sbmFileProcSumId
	 * @return
	 */
	public List<SbmFileInfoPO> getSbmFileInfoList(Long sbmFileProcSumId);
	
	
}
