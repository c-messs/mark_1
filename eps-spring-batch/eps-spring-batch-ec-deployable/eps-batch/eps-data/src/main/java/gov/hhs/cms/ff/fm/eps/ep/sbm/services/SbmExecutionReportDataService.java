package gov.hhs.cms.ff.fm.eps.ep.sbm.services;

import java.util.List;

import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMExecutionReportDTO;

/**
 * 
 * SbmExecutionReportDataService interface
 *
 */
public interface SbmExecutionReportDataService {
	
	/**
	 * 
	 * @return List<SBMExecutionReportDTO>
	 */
	public List<SBMExecutionReportDTO> getSbmExecutionLog();
	

}
