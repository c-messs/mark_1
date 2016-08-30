package gov.hhs.cms.ff.fm.eps.ep.sbm.services;

public interface SbmFileReversalDao {

	/**
	 * Back out polices for the given file id.
	 * @return
	 */
	public void backOutFile(Long fileProcSummaryId);

}
