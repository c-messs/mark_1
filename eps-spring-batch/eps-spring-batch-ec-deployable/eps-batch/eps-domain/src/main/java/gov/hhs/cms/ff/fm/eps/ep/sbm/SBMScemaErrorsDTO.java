package gov.hhs.cms.ff.fm.eps.ep.sbm;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds SBM Schema Errors 
 *
 */
public class SBMScemaErrorsDTO {

	private List<SbmXMLSchemaError> schemaErrorsList = new ArrayList<>();
	private Long sbmFileInfoId;
	
	/**
	 * @return the sbmFileInfoId
	 */
	public Long getSbmFileInfoId() {
		return sbmFileInfoId;
	}
	/**
	 * @param sbmFileInfoId the sbmFileInfoId to set
	 */
	public void setSbmFileInfoId(Long sbmFileInfoId) {
		this.sbmFileInfoId = sbmFileInfoId;
	}
	
	/**
	 * @return the schemaErrorsList
	 */
	public List<SbmXMLSchemaError> getSchemaErrorsList() {
		return schemaErrorsList;
	}
		
			
}
