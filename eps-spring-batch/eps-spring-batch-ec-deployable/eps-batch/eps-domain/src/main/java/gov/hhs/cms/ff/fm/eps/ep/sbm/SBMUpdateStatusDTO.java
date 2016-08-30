/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.sbm;

import java.io.File;
import java.util.List;

import org.apache.commons.csv.CSVRecord;

/**
 * @author rajesh.talanki
 *
 */
public class SBMUpdateStatusDTO {

	private File inputFile;
	private List<CSVRecord> fileLineItems;
	private List<SBMUpdateStatusErrorDTO> errorList;
	
	/**
	 * @return the fileLineItems
	 */
	public List<CSVRecord> getFileLineItems() {
		return fileLineItems;
	}
	/**
	 * @return the inputFile
	 */
	public File getInputFile() {
		return inputFile;
	}
	/**
	 * @param inputFile the inputFile to set
	 */
	public void setInputFile(File inputFile) {
		this.inputFile = inputFile;
	}
	/**
	 * @return the errorList
	 */
	public List<SBMUpdateStatusErrorDTO> getErrorList() {
		return errorList;
	}
	
	
	
}
