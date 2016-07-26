package gov.hhs.cms.ff.fm.eps.ep;

import gov.cms.dsh.bem.FileInformationType;



import org.junit.Test;



public class XMLUtilTest extends BaseUtilTest {
	
	final private String GRPSNDRID = "GRPSNDRID";
	final private String GRPRCVRID = "GRPRCVRID";
	final private String GRPCTLNUM = "GRPCTLNUM";
	final private String GRPTS ="GRPTS";
	final private String VERSNUM = "VERSNUM";
	
	@Test 
	public void testGetFileInfoTypeFromXml_ExceptionNPE() {
		
		String expectedEx = "java.lang.NullPointerException";
		String fileInfoXml = null;
		
		try {
			XMLUtil.getFileInfoTypeFromXml(fileInfoXml);
		} catch (Exception ex) {
			String actualEx = ex.getClass().getName();
			assertEquals("Exception thrown", expectedEx, actualEx);
		}
	}
	
	@Test 
	public void testGetFileInfoTypeFromXml_ExceptionInvalidXML() {
		
		String expectedEx = "javax.xml.stream.XMLStreamException";
		String fileInfoXml = "<<!NOT XML!>>";
		
		try {
			XMLUtil.getFileInfoTypeFromXml(fileInfoXml);
		} catch (Exception ex) {
			String actualEx = ex.getClass().getName();
			assertEquals("Exception thrown", expectedEx, actualEx);
		}
	}
	
	@Test 
	public void testGetFileInfoTypeFromXml() {
		XMLUtil xmlUtil = new XMLUtil();
		assertNotNull("xmlUtil can be initialized", xmlUtil);
		String expectedId = "83678"; 
		String fileInfoXml = makeFileInformationTypeAsStringXML(expectedId);
		FileInformationType actualFileInfoType = null;
		try {
			actualFileInfoType = XMLUtil.getFileInfoTypeFromXml(fileInfoXml);
		} catch (Exception ex) {
			assertTrue("Exception should not be thrown", false);
		}
		assertNotNull("FileInformationType", actualFileInfoType);
		assertEquals("GroupSenderID", expectedId + GRPSNDRID, actualFileInfoType.getGroupSenderID());
		assertEquals("GroupReceiverID", expectedId + GRPRCVRID, actualFileInfoType.getGroupReceiverID());
		assertEquals("GroupControlNumber", expectedId + GRPCTLNUM, actualFileInfoType.getGroupControlNumber());
		assertNull("GroupTimeStamp", actualFileInfoType.getGroupTimeStamp());
		assertEquals("VersionNumber", expectedId + VERSNUM, actualFileInfoType.getVersionNumber());
		
	}
	
	private String makeFileInformationTypeAsStringXML(String id) {

		String strXML = "<FileInformation>" +
				"<GroupSenderID>" + id + GRPSNDRID + "</GroupSenderID>" +
				"<GroupReceiverID>" + id + GRPRCVRID + "</GroupReceiverID>" +
				"<GroupControlNumber>" + id + GRPCTLNUM + "</GroupControlNumber>" +
				"<GroupTimeStamp>" + id + GRPTS + "</GroupTimeStamp>" +
				"<VersionNumber>" + id + VERSNUM + "</VersionNumber>" +
				"</FileInformation>";

		return strXML;
	}
}
