/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author girish.padmanabhan
 *
 */
@XmlRootElement
public class EPSFileIndex {

	protected String fileName;
	private boolean valid;
	
	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}
	/**
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	/**
	 * @return the validationSuccess
	 */
	public boolean isValid() {
		return valid;
	}
	/**
	 * @param valid the validationSuccess to set
	 */
	public void setValid(boolean valid) {
		this.valid = valid;
	}

    @Override
    public String toString() {
        return "EPSFileIndex{" +
                "fileName='" + fileName + '\'' +
                ", valid=" + valid +
                '}';
    }
}
