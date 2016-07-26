/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import gov.cms.dsh.bem.BenefitEnrollmentMaintenanceType;
import gov.cms.dsh.bem.BenefitEnrollmentRequest;
import gov.cms.dsh.bem.TransactionInformationType;
import gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentRequestDTO;
import gov.hhs.cms.ff.fm.eps.ep.EPSValidationRequest;
import gov.hhs.cms.ff.fm.eps.ep.enums.ExchangeType;
import gov.hhs.cms.ff.fm.eps.ep.enums.TxnMessageDirectionType;
import gov.hhs.cms.ff.fm.eps.ep.enums.TxnMessageType;
import gov.hhs.cms.ff.fm.eps.ep.services.TransMsgFileInfoCompositeDAO;
import gov.hhs.cms.ff.fm.eps.ep.util.EpsDateUtils;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Test class for BEMExtractionWriter
 * 
 * @author girish.padmanabhan
 * 
 */
@RunWith(JUnit4.class)
public class BEMExtractionWriterTest extends TestCase {

	private BEMExtractionWriter bemExtractionWriter;
	private TransMsgFileInfoCompositeDAO mockedTxnMsgFileInfoService;
	JdbcTemplate mockJdbcTemplate;
	
	@Before
	public void setup() {
		mockedTxnMsgFileInfoService = EasyMock.createMock(TransMsgFileInfoCompositeDAO.class);
		bemExtractionWriter = new BEMExtractionWriter();
		bemExtractionWriter.setTxnMsgFileInfoService(mockedTxnMsgFileInfoService);
		mockJdbcTemplate = EasyMock.createMock(JdbcTemplate.class);
		bemExtractionWriter.setJdbcTemplate(mockJdbcTemplate);
		
		JobInstance jobInst = new JobInstance(999990L,"retroActivePaymentsJob");
		JobExecution jobEx = new JobExecution(jobInst, null);
		ExecutionContext ctx = new ExecutionContext();
		ctx.putString("BATCH_RUNCONTROL_ID", "0000");
		jobEx.setExecutionContext(ctx);
		
		bemExtractionWriter.setJobExecution(jobEx);
		bemExtractionWriter.setInsertDailyBEMIndexer("insertDailyBEMIndexer");
	}

	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.BEMExtractionWriter#write(gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentMaintenanceDTO)}
	 * This method tests the writing to BEM Index functionality of the writer. The other services are mocked
	 * 
	 * @throws Exception
	 */
	@Test
	public void testWrite_success_FFM() throws Exception {
		expect(mockedTxnMsgFileInfoService.saveFileInfo(EasyMock.anyObject(BenefitEnrollmentRequestDTO.class))).andReturn(new Long(9999999999999L));
		mockedTxnMsgFileInfoService.saveTransMsg(EasyMock.anyObject(BenefitEnrollmentRequestDTO.class));
		expect(mockJdbcTemplate.update(EasyMock.anyString(), EasyMock.anyLong(), EasyMock.anyString(),EasyMock.anyString(),EasyMock.anyString(),EasyMock.anyString()))
		.andReturn(0);
		expect(mockJdbcTemplate.update(EasyMock.anyString(), EasyMock.anyLong(), EasyMock.anyString(),EasyMock.anyString()))
		.andReturn(0);
		replay(mockJdbcTemplate);
		replay(mockedTxnMsgFileInfoService);
	
		List<BenefitEnrollmentRequestDTO> bers = createMockBer();
		bemExtractionWriter.write(bers);
		
		BenefitEnrollmentRequestDTO berDTO = bers.get(0);
		assertNotNull("BemTypes are not null after calling the writer", bers);
		assertEquals("FileInfoId in berDTO is 9999999999999L", 
				9999999999999L, berDTO.getTxnMessageFileInfoId().longValue());
		assertEquals("TxnMessageDirectionType in berDTO is INBOUND", 
				TxnMessageDirectionType.INBOUND, berDTO.getTxnMessageDirectionType());
		assertEquals("TxnMessageType in berDTO is 834", 
				TxnMessageType.MSG_834, berDTO.getTxnMessageType());
		assertEquals("BatchId in berDTO is 999990L", 
				999990L, berDTO.getBatchId().longValue());
	}
	
	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.BEMExtractionWriter#write(gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentMaintenanceDTO)}
	 * This test method expects the write() method to throw an Exception
	 * 
	 * @throws Exception
	 */
	@Test(expected=Exception.class)
	public void testWrite_exception() throws Exception {
		expect(mockedTxnMsgFileInfoService.saveFileInfo(EasyMock.anyObject(BenefitEnrollmentRequestDTO.class))).andThrow(new Exception());
		expect(mockJdbcTemplate.update(EasyMock.anyString(), EasyMock.anyLong(), EasyMock.anyString(),EasyMock.anyString(),EasyMock.anyString(),EasyMock.anyString()))
		.andReturn(0);
		expect(mockJdbcTemplate.update(EasyMock.anyString(), EasyMock.anyLong(), EasyMock.anyString(),EasyMock.anyString()))
		.andReturn(0);
		replay(mockJdbcTemplate);
		List<BenefitEnrollmentRequestDTO> bers = createMockBer();
		assertNotNull("BenefitEnrollmentMaintenanceDTO list for writer", bers);
		bemExtractionWriter.write(bers);
	}

	private List<BenefitEnrollmentRequestDTO> createMockBer() {
		List<BenefitEnrollmentRequestDTO> bers = new ArrayList<BenefitEnrollmentRequestDTO>();
		BenefitEnrollmentRequest ber = new BenefitEnrollmentRequest();
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		DateTime fileDate = EpsDateUtils.getCurrentDateTime();
		
		BenefitEnrollmentRequestDTO ber1  = new BenefitEnrollmentRequestDTO();
		ber1.setFileInfoXml("<FileInformation>Test</FileInformation>");
		ber1.setFileNmDateTime(fileDate);
		ber1.setFileNm("834Test.xml");
		ber1.setInsertFileInfo(true);
		ber1.setExchangeTypeCd(ExchangeType.FFM.getValue());
		
		TransactionInformationType transInfoType = new TransactionInformationType();
		transInfoType.setCurrentTimeStamp(EpsDateUtils.getXMLGregorianCalendar(new DateTime()));
		bem.setTransactionInformation(transInfoType);
		
		ber.getBenefitEnrollmentMaintenance().add(bem);
		bers.add(ber1);
		return bers;
	}

}
