package gov.hhs.cms.ff.fm.eps.ep.data.util.sbm;

import gov.cms.dsh.sbmi.Enrollment;

import java.io.StringWriter;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;



public abstract class AbstractTestSbmFileGenerator {

	
	protected static final LocalDate DATE = LocalDate.now();

	protected static final int YEAR = DATE.getYear();

	protected static final LocalDate JAN_1 = LocalDate.of(YEAR, 1, 1);
	protected static final LocalDate JAN_15 = LocalDate.of(YEAR, 1, 15);
	protected static final LocalDate JAN_31 = LocalDate.of(YEAR, 1, 31);
	protected static final LocalDate FEB_1 = LocalDate.of(YEAR, 2, 1);
	protected static final LocalDate FEB_2 = LocalDate.of(YEAR, 2, 2);
	protected static final LocalDate FEB_15 = LocalDate.of(YEAR, 2, 15);
	protected static final LocalDate MAR_14 = LocalDate.of(YEAR, 3, 14);
	protected static final LocalDate MAR_15 = LocalDate.of(YEAR, 3, 15);
	protected static final LocalDate JUN_29 = LocalDate.of(YEAR, 6, 29);
	protected static final LocalDate JUN_30 = LocalDate.of(YEAR, 6, 30);

	//For Files sent by States.
	protected final DateTimeFormatter DTF_FILE = DateTimeFormatter.ofPattern("'D'yyMMdd'.T'HHmmssSSS");
	protected final NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);

	protected final String[] states = {"NY", "WA", "MN", "CO", "CT", "MD", "KY", "RI", "VT", "CA", "MA", "ID", "DC"};
	protected final int[] stateFileSetTypes = {2, 1, 1, 1, 3, 1, 1, 3, 3, 2, 3, 1, 1};


	protected final String[] names = {"DAD", "MOM", "SON", "DAU", "BBY1", "BBY2"};
	
	protected String TEST_PATH_INPUT_DIR = "/SBMFiles/input/";
	
	protected String TEST_PATH_UPDATE_STATUS_INPUT_DIR = "/SBMFiles/sbmUpdateStatus/input/";
	
	protected String TEST_PATH_FILES_DIR = "/SBMFiles/testFiles/";


	private static Marshaller marshallerENR;
	static {
		try {
			JAXBContext jaxbContextENR = JAXBContext.newInstance(Enrollment.class);
			marshallerENR = jaxbContextENR.createMarshaller();
			marshallerENR.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

		} catch (JAXBException ex) {
			System.out.print("EPROD-24 EPS JAXB Marshalling error (Enrollment to XML).\n" + ex.getMessage());
		}
	}

	protected String getEnrollmentAsStringXML(Enrollment enrollment) {

		String xml = "";
		try {
			StringWriter stringWriter = new StringWriter();
			marshallerENR.marshal(enrollment, stringWriter);
			xml = stringWriter.toString();
		} catch (Exception ex) {
			System.out.println("Ex at getEnrollmentAsStringXML: "+ ex.getMessage());
		}
		return xml;
	}
}
