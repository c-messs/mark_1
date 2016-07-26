/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.retroactivepaymentsjob;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob.BaseBatchTest;
import gov.hhs.cms.ff.fm.eps.rap.domain.RapConstants;
import gov.hhs.cms.ff.fm.eps.rap.dto.PolicyPaymentTransDTO;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Test class for RAPCompositeItemWriter
 * 
 * @author girish.padmanabhan
 * 
 */
@RunWith(JUnit4.class)
public class RAPCompositeItemWriterTest extends BaseBatchTest {

	private RAPCompositeItemWriter rapCompositeItemWriter;
	private ItemWriter<PolicyPaymentTransDTO> mockPaymentTransactionWriter;
	private ItemWriter<PolicyPaymentTransDTO> mockPaymentTransStatusWriter;	
	private ItemWriter<PolicyPaymentTransDTO> mockPaymentStatusUpdateWriter;
	
	private DateTime coverageDate;
	private DateTime maintStart;
	
	@Before
	public void setup() {
		coverageDate = JAN_1;
		maintStart = JAN_1;
		
		mockPaymentTransactionWriter = createMock(JdbcBatchItemWriter.class);
		mockPaymentTransStatusWriter = createMock(JdbcBatchItemWriter.class);
		mockPaymentStatusUpdateWriter = createMock(JdbcBatchItemWriter.class);
		
		rapCompositeItemWriter = new RAPCompositeItemWriter();
		rapCompositeItemWriter.setInsertRetroPaymentWriter(mockPaymentTransactionWriter);
		rapCompositeItemWriter.setPaymentTransStatusWriter(mockPaymentTransStatusWriter);
		rapCompositeItemWriter.setPaymentStatusUpdateWriter(mockPaymentStatusUpdateWriter);
		ReflectionTestUtils.setField(rapCompositeItemWriter, "modifiedByUser", JOB_ID.toString());
	}

	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.retroactivepaymentsjob.RAPCompositeItemWriter#write()}
	 * This method tests the writing to Payment Transaction tables functionality of the writer.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testWrite_success() throws Exception {

		String hiosId = "88888";
		String exchangePolId = EXPOLID_PREF + JOB_ID.toString();

		List<PolicyPaymentTransDTO> payments = new ArrayList<PolicyPaymentTransDTO>();
		PolicyPaymentTransDTO pmtTrans1 = new PolicyPaymentTransDTO();
		pmtTrans1.setPolicyPaymentTransId(JOB_ID);
		pmtTrans1.setExchangePolicyId(exchangePolId);
		pmtTrans1.setUpdateStatusRec(false);
		pmtTrans1.setLastPaymentProcStatusTypeCd("PCYC");
		pmtTrans1.setIssuerHiosId(hiosId);
		pmtTrans1.setSubscriberStateCd(SUBSCRIBER_STATE_CD);
		pmtTrans1.setFinancialProgramTypeCd("APTC");
		pmtTrans1.setCoverageDate(coverageDate);
		pmtTrans1.setMaintenanceStartDateTime(maintStart);
		pmtTrans1.setPolicyVersionId(-1L);
		pmtTrans1.setTransPeriodTypeCd("R");        
		pmtTrans1.setIssuerStateCd(ISSUER_STATE_CD);     
		pmtTrans1.setCreateBy(JOB_ID.toString());
		payments.add(pmtTrans1);
		
		PolicyPaymentTransDTO pmtTrans2 = new PolicyPaymentTransDTO();
		pmtTrans2.setPolicyPaymentTransId(-1L);
		pmtTrans2.setLastPaymentProcStatusTypeCd("PCYC");
		pmtTrans2.setCreateBy(JOB_ID.toString());
		pmtTrans2.setUpdateStatusRec(true);
		payments.add(pmtTrans2);
		
		List<List<PolicyPaymentTransDTO>> items = new ArrayList<List<PolicyPaymentTransDTO>>();
		items.add(payments);
		
		mockPaymentTransactionWriter.write(EasyMock.<List<PolicyPaymentTransDTO>>anyObject());
		expectLastCall().anyTimes();
		replay(mockPaymentTransactionWriter);
		
		mockPaymentTransStatusWriter.write(EasyMock.<List<PolicyPaymentTransDTO>>anyObject());
		expectLastCall().anyTimes();
		replay(mockPaymentTransStatusWriter);
		
		rapCompositeItemWriter.write(items);
		PolicyPaymentTransDTO pmtDTO = payments.get(0);
		assertNotNull("DTOs are not null after calling the writer", payments);
		assertNotNull("DTOs are not null after calling the writer", pmtDTO);
		
		verify(mockPaymentTransactionWriter);
		verify(mockPaymentTransStatusWriter);
	}

	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.retroactivepaymentsjob.RAPCompositeItemWriter#write()}
	 * This method tests the writing to Payment Transaction tables functionality of the writer.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testWrite_CreateNewTrans() throws Exception {

		String hiosId = "88888";
		String exchangePolId = EXPOLID_PREF + JOB_ID.toString();

		List<PolicyPaymentTransDTO> payments = new ArrayList<PolicyPaymentTransDTO>();
		PolicyPaymentTransDTO pmtTrans1 = new PolicyPaymentTransDTO();
		pmtTrans1.setPolicyPaymentTransId(JOB_ID);
		pmtTrans1.setExchangePolicyId(exchangePolId);
		pmtTrans1.setUpdateStatusRec(false);
		pmtTrans1.setLastPaymentProcStatusTypeCd("PCYC");
		pmtTrans1.setIssuerHiosId(hiosId);
		pmtTrans1.setSubscriberStateCd(SUBSCRIBER_STATE_CD);
		pmtTrans1.setFinancialProgramTypeCd("APTC");
		pmtTrans1.setCoverageDate(coverageDate);
		pmtTrans1.setMaintenanceStartDateTime(maintStart);
		pmtTrans1.setPolicyVersionId(-1L);
		pmtTrans1.setTransPeriodTypeCd("R");        
		pmtTrans1.setIssuerStateCd(ISSUER_STATE_CD);     
		pmtTrans1.setCreateBy(JOB_ID.toString());
		payments.add(pmtTrans1);
		
		List<List<PolicyPaymentTransDTO>> items = new ArrayList<List<PolicyPaymentTransDTO>>();
		items.add(payments);
		
		mockPaymentTransactionWriter.write(EasyMock.<List<PolicyPaymentTransDTO>>anyObject());
		expectLastCall().anyTimes();
		replay(mockPaymentTransactionWriter);
		
		mockPaymentTransStatusWriter.write(EasyMock.<List<PolicyPaymentTransDTO>>anyObject());
		expectLastCall().anyTimes();
		replay(mockPaymentTransStatusWriter);
		
		rapCompositeItemWriter.write(items);
		PolicyPaymentTransDTO pmtDTO = payments.get(0);
		assertNotNull("DTOs are not null after calling the writer", payments);
		assertNotNull("DTOs are not null after calling the writer", pmtDTO);
		
		verify(mockPaymentTransactionWriter);
		verify(mockPaymentTransStatusWriter);
	}

	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.retroactivepaymentsjob.RAPCompositeItemWriter#write()}
	 * This method tests the writing to Payment Transaction tables functionality of the writer.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testWrite_UpdateStatus() throws Exception {

		List<PolicyPaymentTransDTO> payments = new ArrayList<PolicyPaymentTransDTO>();
		PolicyPaymentTransDTO pmtTrans2 = new PolicyPaymentTransDTO();
		pmtTrans2.setPolicyPaymentTransId(-1L);
		pmtTrans2.setLastPaymentProcStatusTypeCd("PCYC");
		pmtTrans2.setCreateBy(JOB_ID.toString());
		pmtTrans2.setUpdateStatusRec(true);
		payments.add(pmtTrans2);
		
		List<List<PolicyPaymentTransDTO>> items = new ArrayList<List<PolicyPaymentTransDTO>>();
		items.add(payments);
		
		mockPaymentTransStatusWriter.write(EasyMock.<List<PolicyPaymentTransDTO>>anyObject());
		expectLastCall().anyTimes();
		replay(mockPaymentTransStatusWriter);
		
		mockPaymentStatusUpdateWriter.write(EasyMock.<List<PolicyPaymentTransDTO>>anyObject());
		expectLastCall().anyTimes();
		replay(mockPaymentStatusUpdateWriter);
		
		rapCompositeItemWriter.write(items);
		PolicyPaymentTransDTO pmtDTO = payments.get(0);
		assertNotNull("DTOs are not null after calling the writer", payments);
		assertNotNull("DTOs are not null after calling the writer", pmtDTO);
		
		verify(mockPaymentTransStatusWriter);
		verify(mockPaymentStatusUpdateWriter);
	}
	
	/**
	 * Test method for {@linkgov.hhs.cms.ff.fm.eps.ep.jobs.retroactivepaymentsjob.RAPCompositeItemWriter#beforeStep()}
	 * 
	 */
	@Test
	public void testBeforeStep() {
		
		ExecutionContext ctx = new ExecutionContext();
		ctx.putString("batchBusinessId", JOB_ID.toString());
		JobExecution jobExecution = new JobExecution(9999L);
		jobExecution.setExecutionContext(ctx);
		StepExecution stepExecution = new StepExecution("rapCompositeItemWriter", jobExecution);
		
		rapCompositeItemWriter.beforeStep(stepExecution);
		
		assertNotNull("stepExecution not null", stepExecution);

		String modifiedByUser = (String) ReflectionTestUtils.getField(rapCompositeItemWriter, "modifiedByUser");
		assertEquals("modifiedByUser", JOB_ID.toString(), modifiedByUser);
	}
	
	/**
	 * Test method for {@linkgov.hhs.cms.ff.fm.eps.ep.jobs.retroactivepaymentsjob.RAPCompositeItemWriter#afterStep()}
	 *
	 * SHOULD return null, since its unused interface implementation method.
	 * MODIFY this test case when implementation is added
	 */
	@Test
	public void testAfterStep() {
		JobInstance jobInst = new JobInstance(9999L,"epsJob");
		JobExecution jobEx = new JobExecution(jobInst, null);
		StepExecution stepExecution = new StepExecution("rapCompositeItemWriter", jobEx);
		
		ExitStatus status = rapCompositeItemWriter.afterStep(stepExecution);
		assertNull("ExecutionStatus returns null", status);
	}
	
	/**
	 * Test method for {@linkgov.hhs.cms.ff.fm.eps.ep.jobs.retroactivepaymentsjob.RAPCompositeItemWriter#afterWrite()}
	 */
	@Test
	public void testAfterWrite() {
		
		ExecutionContext ctx = new ExecutionContext();
		ctx.putString("batchBusinessId", JOB_ID.toString());
		JobExecution jobExecution = new JobExecution(9999L);
		jobExecution.setExecutionContext(ctx);
		StepExecution stepExecution = new StepExecution("rapCompositeItemWriter", jobExecution);
		ReflectionTestUtils.setField(rapCompositeItemWriter, "stepExecution", stepExecution);
		DateTime lastPolicyVersionDt = DateTime.now();
		ReflectionTestUtils.setField(rapCompositeItemWriter, "lastPolicyVersionDt", lastPolicyVersionDt);
		
		rapCompositeItemWriter.afterWrite(null);
		DateTime lastPolicyVersionDtFromCtx = (DateTime) ReflectionTestUtils.getField(rapCompositeItemWriter, "lastPolicyVersionDt");
		
		assertNotNull("lastPolicyVersionDt not null", lastPolicyVersionDtFromCtx);
		assertEquals("lastPolicyVersionDt", lastPolicyVersionDt, lastPolicyVersionDtFromCtx);
	}
	
	@Test
	public void testAfterWrite_nullPvd() {
		
		ExecutionContext ctx = new ExecutionContext();
		ctx.putString("batchBusinessId", JOB_ID.toString());
		ctx.put(RapConstants.LASTPOLICYVERSIONDATE, DateTime.now());
		JobExecution jobExecution = new JobExecution(9999L);
		jobExecution.setExecutionContext(ctx);
		StepExecution stepExecution = new StepExecution("rapCompositeItemWriter", jobExecution);
		ReflectionTestUtils.setField(rapCompositeItemWriter, "stepExecution", stepExecution);
		
		rapCompositeItemWriter.afterWrite(null);
		DateTime lastPolicyVersionDtFromCtx = (DateTime) ReflectionTestUtils.getField(rapCompositeItemWriter, "lastPolicyVersionDt");
		
		assertNull("lastPolicyVersionDt null", lastPolicyVersionDtFromCtx);
	}
	
	/**
	 * Test method for {@linkgov.hhs.cms.ff.fm.eps.ep.jobs.retroactivepaymentsjob.RAPCompositeItemWriter#onWriteError()}
	 */
	@Test
	public void testOnWriteError() {
		Exception ex = new Exception("Test");
		rapCompositeItemWriter.onWriteError(ex, null);
		
		assertNotNull("Testing void spring batch interface method which only logs", ex.getMessage());
	}
	
	/**
	 * Test method for {@linkgov.hhs.cms.ff.fm.eps.ep.jobs.retroactivepaymentsjob.RAPCompositeItemWriter#beforeWrite()}
	 */
	@Test
	public void testBeforeWrite() {
		
		List<? extends List<PolicyPaymentTransDTO>> items = new ArrayList<List<PolicyPaymentTransDTO>>();
		rapCompositeItemWriter.beforeWrite(items);
		
		assertNotNull("Testing empty void spring batch interface method which doesnt do anything", items);
	}
	
}
