package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm;

import java.io.IOException;
import java.time.LocalDateTime;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.xml.sax.SAXException;

import gov.cms.dsh.sbmi.FileInformationType;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileInfo;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileProcessingDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SBMFileCompositeDAO;

public class SBMFileStatusHandlerTest {
	
	private SBMFileStatusHandler fileSatusHandler;
	private SBMFileCompositeDAO fileCompositeDaoMock;
	
	@Before
	public void setUp() throws IOException, ParserConfigurationException, SAXException {
		
		fileSatusHandler = new SBMFileStatusHandler();
		
		fileCompositeDaoMock = Mockito.mock(SBMFileCompositeDAO.class);
		fileSatusHandler.setFileCompositeDao(fileCompositeDaoMock);
		
	}
	
	@After
	public void tearDown() throws IOException {
	}
	
	@Test
	public void test_determineAndSetFileStatus() throws UnexpectedInputException, ParseException, NonTransientResourceException, Exception {
		
		SBMFileProcessingDTO dto = new SBMFileProcessingDTO();
		
		FileInformationType fileInfoType = new FileInformationType();
		dto.setFileInfoType(fileInfoType);
		
		SBMFileInfo fileInfo = new SBMFileInfo();
		fileInfo.setFileLastModifiedDateTime(LocalDateTime.now());
		dto.setSbmFileInfo(fileInfo);
		
		fileSatusHandler.setFreezePeriodStartDay(2);
		fileSatusHandler.setFreezePeriodEndDay(10);
		
		fileSatusHandler.determineAndSetFileStatus(dto);
		
		Assert.assertNotNull("Reader should return dto", dto);
		
	}
	
	
}
