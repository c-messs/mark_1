/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileInfo;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileProcessingDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMSummaryAndFileInfoDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SBMFileCompositeDAO;
import junit.framework.TestCase;

/**
 * Test class for xprProcessingWriter
 * 
 * @author girish.padmanabhan
 * 
 */
@RunWith(JUnit4.class)
public class SbmiFileIngestionWriterTest extends TestCase {

	private static final String processedFolderPath = "./src/test/resources/sbm/writerTest/processedFolder";
	private static final String invalidFolderPath = "./src/test/resources/sbm/writerTest/invalidFolder";
	
	private File processedFolder = new File(processedFolderPath);
	private File invalidFolder = new File(invalidFolderPath);	
	
	private SbmiFileIngestionWriter sbmiFileIngestionWriter;
	private SBMResponseGenerator mockSBMResponseGenerator;
	private SBMFileCompositeDAO mockSBMFileCompositeDAO;
	
	@Before
	public void setup() throws IOException {
		sbmiFileIngestionWriter = new SbmiFileIngestionWriter();
		mockSBMFileCompositeDAO= createMock(SBMFileCompositeDAO.class);
		sbmiFileIngestionWriter.setFileCompositeDao(mockSBMFileCompositeDAO);
		
		mockSBMResponseGenerator = createMock(SBMResponseGenerator.class);
		sbmiFileIngestionWriter.setResponseGenerator(mockSBMResponseGenerator);
		
		processedFolder.mkdirs();
		invalidFolder.mkdirs();
		
		FileUtils.cleanDirectory(processedFolder);
		FileUtils.cleanDirectory(invalidFolder);
		
		sbmiFileIngestionWriter.setProcessedFolder(processedFolder);
		sbmiFileIngestionWriter.setInvalidFolder(invalidFolder);
	}
	
	@After
	public void tearDown() throws IOException {
		FileUtils.deleteDirectory(processedFolder);
		FileUtils.deleteDirectory(invalidFolder);
	}
	
	@Test
	public void testWrite_success_FileProcSummaryFromDB_NotNull() throws Exception {
		mockSBMResponseGenerator.generateSBMS(EasyMock.anyObject(SBMFileProcessingDTO.class));
		EasyMock.expectLastCall(); 
		replay(mockSBMResponseGenerator);
		
		mockSBMFileCompositeDAO.updateFileStatus(EasyMock.anyLong(), EasyMock.anyObject(SBMFileStatus.class), EasyMock.anyLong());
		EasyMock.expectLastCall(); 
		mockSBMFileCompositeDAO.insertStagingSbmGroupLockForExtract(EasyMock.anyLong());
		EasyMock.expectLastCall();
		
		expect(mockSBMFileCompositeDAO.saveFileInfoAndErrors(EasyMock.anyObject(SBMFileProcessingDTO.class))).andReturn(1L);
		
		replay(mockSBMFileCompositeDAO);
		
		SBMFileProcessingDTO dto = new SBMFileProcessingDTO();
		dto.setFileProcSummaryFromDB(new SBMSummaryAndFileInfoDTO());
		dto.getFileProcSummaryFromDB().setSbmFileProcSumId(1L);
		
		dto.setSbmFileInfo(new SBMFileInfo());
		dto.getSbmFileInfo().setSbmFileNm("testFile.T");
		dto.getSbmFileInfo().setRejectedInd(false);
		dto.setSbmiFile(new File(processedFolder + File.separator + "testFile.T"));
		dto.setSbmFileStatusType(SBMFileStatus.IN_PROCESS);
		
		sbmiFileIngestionWriter.write(dto);
		
		assertNotNull("dto not null after calling the writer", dto);
	}


}
