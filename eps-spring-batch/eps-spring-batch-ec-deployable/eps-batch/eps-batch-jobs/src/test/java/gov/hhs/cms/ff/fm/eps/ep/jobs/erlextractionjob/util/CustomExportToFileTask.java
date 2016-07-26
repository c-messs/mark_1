package gov.hhs.cms.ff.fm.eps.ep.jobs.erlextractionjob.util;

import com.marklogic.developer.corb.ExportToFileTask;

/**
 * @author shasidar.pabolu
 *
 */
public class CustomExportToFileTask extends ExportToFileTask {

	/* (non-Javadoc)
	 * @see com.marklogic.developer.corb.ExportToFileTask#getFileName()
	 * 
	 * Windows does not create filenames with >. As ExportToFileTask creates
	 * file with >, extending to remove the > when file gets generated.
	 * 
	 */
	protected String getFileName(){
		return inputUri.substring(inputUri.lastIndexOf('/')+1).replace(">", "");
	}
	
}
