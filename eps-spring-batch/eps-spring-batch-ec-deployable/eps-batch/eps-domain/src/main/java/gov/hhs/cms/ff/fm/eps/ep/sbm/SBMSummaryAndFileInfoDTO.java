package gov.hhs.cms.ff.fm.eps.ep.sbm;

import java.util.ArrayList;
import java.util.List;

public class SBMSummaryAndFileInfoDTO extends SBMFileProccessingSummary {

	private List<SBMFileInfo> sbmFileInfoList = new ArrayList<>();
	
	/**
	 * @return the sbmFileInfoList
	 */
	public List<SBMFileInfo> getSbmFileInfoList() {
		return sbmFileInfoList;
	}	
	
	
}
