/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob;

import gov.hhs.cms.ff.fm.eps.ep.EPSFileIndex;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;

/**
 * This class is used as the Writer for the Pre-processing step of the batch job.
 * This writer inserts the data in the file Indexer table or the error table based 
 * on the status flag - isValid - present in the input.  Additionally, it also
 * inserts an entry in the audit log table.
 * 
 * @author girish.padmanabhan
 *
 */
public class FileIngestionWriter implements ItemWriter<EPSFileIndex> {
	
	private static final Logger LOG = LoggerFactory.getLogger(FileIngestionWriter.class);
	
	private File invalidFilesDirectory;

	/**
	 * Implementation of the write method from ItemWriter interface. The method  
	 * inserts the data in the file Indexer table or the error table based 
	 * on the status flag - isValid - present in the input. It also
	 * inserts an entry in the audit log table. 
	 * @throws IOException 
	 * 
	 */
	@Override
	public void write(List<? extends EPSFileIndex> epsFileIndexes) throws IOException {
		LOG.debug("\nIn Writer: " + epsFileIndexes);

		for (EPSFileIndex epsFileIndex : epsFileIndexes) {
			if (!epsFileIndex.isValid()) {

				File invalidSchemaFile = new File(epsFileIndex.getFileName());

				FileUtils.moveToDirectory(invalidSchemaFile, invalidFilesDirectory, false);
				LOG.info("Invalid, non-compliant XSD schema XML file:  " + epsFileIndex.getFileName() 
						+" Moved to "+ invalidFilesDirectory);
			}
			LOG.debug(epsFileIndex.getFileName() +": Completed schema validation");
		}
	}

	/**
	 * @param invalidFilesDirectory the invalidFilesDirectory to set
	 */
	public void setInvalidFilesDirectory(File invalidFilesDirectory) {
		this.invalidFilesDirectory = invalidFilesDirectory;
	}

}
