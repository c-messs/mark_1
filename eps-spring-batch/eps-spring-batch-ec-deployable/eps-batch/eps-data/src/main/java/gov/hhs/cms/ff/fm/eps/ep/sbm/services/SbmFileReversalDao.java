package gov.hhs.cms.ff.fm.eps.ep.sbm.services;

/**
 * 
 * SbmFileReversalDao interface
 *
 */
public interface SbmFileReversalDao {

	/**
	 * Back out polices for the given file id.
	 * @param fileProcSummaryId
	 * @return
	 */
	public void backOutFile(Long fileProcSummaryId);

}
