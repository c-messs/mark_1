package gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob;

import gov.hhs.cms.ff.fm.eps.ep.EPSFileIndex;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * This class is used as the reader for the Pre-processing step of the batch job.
 * This reader reads the input 'BER' xml files from configured directory and
 * performs schema validation on the xml files against the specified xsd,  
 * and sets a valid/invalid flag on the return object to be passed to the
 * processor/writer.
 *
 * @author christopher.vaka
 * 
 */
public class FileIngestionReader implements ItemReader<EPSFileIndex>, ItemStream {

    private static final Logger LOG = LoggerFactory.getLogger(FileIngestionReader.class);
    private static final String INPUT_FILES = "INPUT_FILES";

    private File privateDirectory;
    private String xsd;
    private List<File> inputFiles;
    private File processedFile;

	/**
	 * Implementation of the read method from ItemReader interface. This method
	 * reads the input directory and does the schema validation of the xml files
	 * in the directory. It sets the invalid flag on the response object according to the 
	 * xsd validation result.  
	 * 
	 * @throws UnexpectedInputException
	 * @throws ParseException
	 * @throws Exception
	 */
    @Override
	public EPSFileIndex read() throws Exception, UnexpectedInputException, ParseException {
		EPSFileIndex fileIndex = null;

		if (!inputFiles.isEmpty()) {
			fileIndex = new EPSFileIndex();

			processedFile = inputFiles.get(0);
			inputFiles.remove(processedFile);

			fileIndex.setFileName(processedFile.getCanonicalPath());
			
			URL xmlFileURL = processedFile.toURI().toURL();
			
			String fileName=processedFile.getName();
			boolean isFileValid = false;
			
			LOG.debug("Validating file name: " + fileName);
			
			if(isValidFileName(fileName)){
			
			LOG.debug("Validating incoming BER: " + processedFile.getCanonicalPath());
			
			if (isValidXML(xmlFileURL)) {
				isFileValid = validateXMLSchema(xmlFileURL);
			}

			fileIndex.setValid(isFileValid);
			}
		}
		return fileIndex;
	}

	/**
	 * 
	 * public method to validate FILE NAME 
	 * @param fileName
	 * @return
	 */
	public boolean isValidFileName(String fileName) {
		if (!fileName.matches("\\w{1,10}\\.\\w{1,10}\\.D(\\d{6})\\.T(\\d{9})(\\.\\w?+)?(\\.\\w*+)?")){
			LOG.warn("EPROD-18: Invalid File Name: "+ fileName);
			
			return false;
		}
		return true;
	}

	/*
	 * Private method to validate XML wellformedness  
	 * 
	 * @param xmlFileURL
	 * @return boolean valid
	 * @throws IOException
	 */
    private boolean isValidXML (URL xmlFileURL) throws IOException, ParserConfigurationException {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(false);
		factory.setNamespaceAware(true);
		InputStream inputStream = xmlFileURL.openStream();
		
		boolean valid = false;
		try {
			SAXParser parser = factory.newSAXParser();
			XMLReader reader = parser.getXMLReader();
			reader.parse(new InputSource(inputStream));
			LOG.debug(xmlFileURL + " is  valid xml");
			valid = true;
		} catch (SAXException e) {
			LOG.warn("EPROD-03: XML File Invalid - "+ xmlFileURL + "\nReason: " + e.getMessage());
			valid = false;
			inputStream.close();
		}
		return valid;
    }
    
	/*
	 * Private method to validate XML Schema against the specified xsd.  
	 * 
	 * @param xmlFileURL
	 * @return boolean valid
	 * @throws IOException
	 */
    private boolean validateXMLSchema(URL xmlFileURL) throws IOException {
		InputStream inputStream = xmlFileURL.openStream();
		InputSource is = new InputSource(inputStream);
		SAXSource saxSource = new SAXSource(is);
		// Validate xml result with the IEPD schemas
		InputStream schemaFileStream = null;
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		schemaFileStream = new BufferedInputStream(this.getClass().getResourceAsStream(xsd));
		String systemId = this.getClass().getResource(xsd).toString();
		Source schemaSource = new StreamSource(schemaFileStream, systemId);
		
		boolean valid = false;
		try {
		    Schema schema = schemaFactory.newSchema(schemaSource);
		    Validator validator = schema.newValidator();
			validator.validate(saxSource);
			LOG.debug(xmlFileURL + " complies to schema: "+xsd);
			valid = true;
		} catch (SAXException e) {
			LOG.warn("EPROD-04: File XSD Schema Invalid - "+ xmlFileURL + "\nReason: " + e.getMessage());
			valid = false;
			inputStream.close();
		}
    	return valid;
    }
 
	/**
	 * Implementation of the open method from ItemStream interface. This method
	 * opens the directory and stores the pointers to the files to be processed 
	 * into the execution context.  
	 * 
	 * @throws ItemStreamException
	 */
    @SuppressWarnings("unchecked")
	@Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        if (executionContext.get(INPUT_FILES) == null) {
            List<File> result = new ArrayList<File>();
            if (privateDirectory.isDirectory()) {
            	//Create a Filename filter to identify filter files
            	File[] dirFiles = privateDirectory.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        return pathname.isFile();
                    }
                });
                Collections.addAll(result, dirFiles);
            } else {
                throw new IllegalStateException("unexpected file type");
            }
            executionContext.put(INPUT_FILES, result);
        }
        inputFiles = (List<File>) executionContext.get(INPUT_FILES);
    }

	/**
	 * Implementation of the update method from ItemStream interface. This method
	 * stores the files list in the execution context.  
	 * 
	 * @throws ItemStreamException
	 */
    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        executionContext.put(INPUT_FILES, inputFiles);
    }

    @Override
    public void close() throws ItemStreamException {
    	// Interface method required
    }

    public void setXsd(String xsd) {
        this.xsd = xsd;
    }

	public void setPrivateDirectory(File privateDirectory) {
		this.privateDirectory = privateDirectory;
	}
}
