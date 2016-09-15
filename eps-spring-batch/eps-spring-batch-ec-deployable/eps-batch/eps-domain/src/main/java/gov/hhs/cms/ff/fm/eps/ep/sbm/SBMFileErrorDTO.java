package gov.hhs.cms.ff.fm.eps.ep.sbm;

import java.util.ArrayList;
import java.util.List;

/**
 * @author rajesh.talanki
 *
 */
public class SBMFileErrorDTO extends SBMErrorDTO {
	
	private List<SBMFileErrorAdditionalInfo> additionalInfoList = new ArrayList<>();

	/**
	 * @return the additionalInfoList
	 */
	public List<SBMFileErrorAdditionalInfo> getAdditionalInfoList() {
		return additionalInfoList;
	}
		
}
