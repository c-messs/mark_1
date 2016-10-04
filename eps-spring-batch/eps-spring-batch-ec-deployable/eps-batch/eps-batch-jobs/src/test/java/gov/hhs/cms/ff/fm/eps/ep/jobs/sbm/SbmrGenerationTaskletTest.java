package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.xml.sax.SAXException;

import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMErrorDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SbmResponseCompositeDao;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.impl.SbmResponseCompositeDaoImpl;

public class SbmrGenerationTaskletTest {

	private SbmrGenerationTasklet tasklet;
	
	private SBMResponseGenerator mockSbmResponseGenerator;
	private SbmResponseCompositeDao mockSbmResponseDao;
	
	@Before
	public void setUp() throws ParserConfigurationException, SAXException, IOException {
		
		tasklet = new SbmrGenerationTasklet();
		
		mockSbmResponseGenerator = createMock(SBMResponseGenerator.class);
		tasklet.setSbmResponseGenerator(mockSbmResponseGenerator);
		
		mockSbmResponseDao = createMock(SbmResponseCompositeDaoImpl.class);
		tasklet.setSbmResponseDao(mockSbmResponseDao);
		
	}
	
	@After
	public void tearDown() throws IOException {
	}
	
	@Test
	public void test_execute() throws Exception {
		
		expect(mockSbmResponseDao.getSummaryIdsForSBMRFromStagingSBMGroupLock(EasyMock.anyLong())).andReturn(Arrays.asList(1L));
		
		mockSbmResponseDao.removeLockOnSummaryIdForSBMR(EasyMock.anyLong(), EasyMock.anyLong());
		EasyMock.expectLastCall().anyTimes();
		
		replay(mockSbmResponseDao);
		
		mockSbmResponseGenerator.generateSBMRWithPolicyErrors(EasyMock.anyLong(), EasyMock.anyLong());
		EasyMock.expectLastCall().anyTimes();
		replay(mockSbmResponseGenerator);
		

		ChunkContext chkContext = new ChunkContext(new StepContext(new StepExecution("sbmrGeneration", new JobExecution(21001L))));		
		RepeatStatus status = tasklet.execute(EasyMock.anyObject(), chkContext);
		
		Assert.assertNotNull("Tasklet should not return null", status);		
		
	}
	
	@Test
	public void test_afterStep() throws Exception {
		
		ExitStatus status = tasklet.afterStep(EasyMock.anyObject());
		
		Assert.assertNull("AfterStep should return null", status);		
		
	}

	
	public static boolean isErrorExists(List<SBMErrorDTO> errorList, String elementInError, String errorCode) {
		for(SBMErrorDTO error: errorList) {
			if(errorCode.equalsIgnoreCase(error.getSbmErrorWarningTypeCd())) {
				return true;
			}
		}
		
		return false;
	}
	
}
