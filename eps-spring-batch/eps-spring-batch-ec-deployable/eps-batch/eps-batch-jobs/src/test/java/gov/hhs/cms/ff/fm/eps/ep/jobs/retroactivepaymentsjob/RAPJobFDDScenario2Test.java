package gov.hhs.cms.ff.fm.eps.ep.jobs.retroactivepaymentsjob;

import gov.hhs.cms.ff.fm.eps.ep.enums.PolicyStatus;
import gov.hhs.cms.ff.fm.eps.rap.domain.PolicyPaymentTrans;
import gov.hhs.cms.ff.fm.eps.rap.domain.PolicyPremium;
import gov.hhs.cms.ff.fm.eps.rap.domain.RapConstants;
import gov.hhs.cms.ff.fm.eps.rap.dto.PolicyDataDTO;
import gov.hhs.cms.ff.fm.eps.rap.dto.PolicyPaymentTransDTO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import com.accenture.foundation.common.codetable.CodeServiceImpl;

/**
 * @author j.radziewski
 *
 */

@ContextConfiguration(locations={"/rap-batch-context.xml", "/test-context-rap.xml"})
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, 
	TransactionalTestExecutionListener.class, DirtiesContextTestExecutionListener.class })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
public class RAPJobFDDScenario2Test extends BaseRapBatchTest {

	final DateFormatter dateFormatter = new DateFormatter("yyyy-MM-dd HH:mm:ss.SSS");

	@Autowired
	private JobLauncherTestUtils  jobLauncherTestUtils;

	@Autowired
	private ApplicationContext context;

	private JobParameters jobParametersStage;
	private JobParameters jobParametersRAP;

	@Before
	public void setup() throws Exception {

		final Map<String, JobParameter> paramsStage = new LinkedHashMap<String, JobParameter>();
		paramsStage.put("timestamp", new JobParameter(dateFormatter.print(new Date(), Locale.US)));
		paramsStage.put("type", new JobParameter("RAPSTAGE"));
		jobParametersStage = new JobParameters(paramsStage);

		final Map<String, JobParameter> paramsRAP = new LinkedHashMap<String, JobParameter>();
		paramsRAP.put("timestamp", new JobParameter(dateFormatter.print(new Date(), Locale.US)));
		paramsRAP.put("type", new JobParameter("RAP"));
		jobParametersRAP = new JobParameters(paramsRAP);
	}

	/*
	 * 5.2.2	Scenario 2: Retroactive Change
	 */
	@Test
	public void test_2() throws Exception {

		String state = "NC";

		PolicyStatus epsStatus = PolicyStatus.EFFECTUATED_2;
		DateTime transDateTime = DATE.plusMonths(3); // APR_1;

		DateTime coverage1 = DATE.withDayOfMonth(1); // JAN_1;
		DateTime erc1 = coverage1.minusDays(16); // DEC 16 
		DateTime initiation1 = coverage1.minusDays(12); // DEC 20

		DateTime coverage2 = DATE.plusMonths(1).withDayOfMonth(1);// FEB_1;
		DateTime erc2 = DATE.withDayOfMonth(1); // JAN_16;
		DateTime initiation2 = DATE.withDayOfMonth(1); // JAN_20;

		DateTime rollupStart1 = initiation1;
		DateTime rollupStart2 = initiation2;

		DateTime lastPolMaintStart = initiation1.plusHours(1);
		DateTime uniqueTransDateTime = transDateTime;
		BigDecimal flatRate = new BigDecimal("1");

		DateTime policyStart = DATE.withDayOfMonth(1); // JAN_1;
		DateTime policyEnd = null;
		DateTime maintStart = DATE.minusDays(3); // JAN_1.minusDays(5);
		DateTime maintEnd = DATE.minusHours(12); // JAN_28;


		DateTime issuerEsd = DATE.withDayOfMonth(1); // JAN_1;

		String batchBusinessIdRAP = null;
		Long stageId = null;
		Long jobId = null;
		Long batchId = getRandomNumber(9);
		Long prevBatchId = batchId - 1L;
		Long exPolId = null;
		String issuerHiosId = null;
		PolicyDataDTO pdDTO = null;
		Long policyVersionId1 = null;
		Long policyVersionId2 = null;
		List<PolicyPremium> premiumList1 = new ArrayList<PolicyPremium>();
		List<PolicyPremium> premiumList2 = new ArrayList<PolicyPremium>();

		// Expected PolicyPaymentTrans record values
		List<PolicyPaymentTransDTO> expectedPaymentList = new ArrayList<PolicyPaymentTransDTO>();
		String expectedStatus = RapConstants.STATUS_PENDING_CYCLE;
		BigDecimal expectedPA_1 = new BigDecimal("-50");
		BigDecimal expectedTPA_1 = null;
		Long expectedParentPptId1 = null; // Set after creating existing payments.
		BigDecimal expectedPA_2 = new BigDecimal("80");
		BigDecimal expectedTPA_2 = new BigDecimal("100");

		try {
			insertCoveragePeriodPaid(batchId, coverage1, erc1, initiation1);
			insertCoveragePeriodPaid(batchId, coverage2 , erc2, initiation2);
			insertIssuerUserFeeRate(batchId, state, "2", coverage1.getYear(), flatRate, initiation1, initiation1.plusYears(1));
			insertBatchProcessLog("aptcCsrUfRollupJob", USER_PREFIX + "rollup-"+ batchId, batchId, rollupStart1, BatchStatus.COMPLETED, null);
			insertBatchProcessLog("aptcCsrUfRollupJob", USER_PREFIX + "rollup-"+ prevBatchId, prevBatchId, rollupStart2 , BatchStatus.COMPLETED, null);
			insertBatchProcessLog(RapConstants.RAP_RETRO_JOB, USER_PREFIX + batchId, batchId, initiation1.minusHours(1), BatchStatus.COMPLETED, lastPolMaintStart);

			// Refresh code/decode cache after inserting test records.
			CodeServiceImpl codeService = (CodeServiceImpl) context.getBean("codeService");
			codeService.reload();

			issuerHiosId = "00001";
			insertApprovedIssuer(batchId, issuerHiosId, issuerEsd);

			exPolId = Long.valueOf("101");

			// Insert 1st PolicyVersion
			DateTime esd1 = DATE.withDayOfMonth(1); // JAN_1;
			DateTime eed1 = new DateTime(DATE.getYear(), DATE.getMonthOfYear(), DATE.dayOfMonth().getMaximumValue(), 0, 0); // JAN_31;
			BigDecimal aptc1 = new BigDecimal("50");
			BigDecimal tpa1 = new BigDecimal("100");
			BigDecimal csr1 = null;

			premiumList1.add(makePolicyPremium(esd1, eed1, aptc1, tpa1, csr1));

			pdDTO = makePolicyDataDTO(issuerHiosId, exPolId.toString(), state, policyStart, policyEnd, maintStart, epsStatus);
			policyVersionId1 = insertPolicyVersionForRap(batchId, uniqueTransDateTime, pdDTO, premiumList1, maintEnd, epsStatus);
			// Set and save the PolicyDataDTO for later assertions.
			pdDTO.setPolicyVersionId(policyVersionId1);

			// Insert 2nd PolicyVersion (a day later)
			uniqueTransDateTime = uniqueTransDateTime.plusDays(1);
			// Set this policy maintStart to the previous policyMaintEnd
			maintStart = maintEnd.plusSeconds(1);
			maintEnd = HIGHDATE;
			// make the first premium of second policy same as first policy version.
			premiumList2.add(makePolicyPremium(esd1, eed1, aptc1, tpa1, csr1));
			// update premium values with new financial changes.
			DateTime esd2 = DATE.plusMonths(1).withDayOfMonth(1); //FEB_1;
			DateTime eed2 = null;
			BigDecimal aptc2 = new BigDecimal("80");
			BigDecimal tpa2 = new BigDecimal("100");
			BigDecimal csr2 = null;

			premiumList2.add(makePolicyPremium(esd2, eed2, aptc2, tpa2, csr2));

			pdDTO = makePolicyDataDTO(issuerHiosId, exPolId.toString(), state, policyStart, policyEnd, maintStart, epsStatus);
			policyVersionId2 = insertPolicyVersionForRap(batchId, uniqueTransDateTime, pdDTO, premiumList2, maintEnd, epsStatus);

			// Insert already processed Payments from previous job.
			PolicyPaymentTrans ppt = makePolicyPaymentTrans(policyVersionId1, exPolId.toString(), initiation1, RapConstants.TRANSPERIOD_RETROACTIVE, RapConstants.STATUS_APPROVED, issuerHiosId, state);
			ppt.setCoverageDate(JAN_1);
			ppt.setFinancialProgramTypeCd(RapConstants.APTC);
			ppt.setPaymentAmount(aptc1);
			ppt.setIssuerLevelTransactionId(Long.valueOf("111111"));
			ppt.setPaymentCoverageStartDate(esd1);
			ppt.setPaymentCoverageEndDate(eed1);

			insertPolicyPaymentTrans(batchId, ppt);

			ppt.setFinancialProgramTypeCd(RapConstants.UF);
			ppt.setPaymentAmount(new BigDecimal("-3.5"));
			ppt.setTotalPremiumAmount(tpa1);
			ppt.setPaymentCoverageStartDate(esd1);

			insertPolicyPaymentTrans(batchId, ppt);

			ppt =  makePolicyPaymentTrans(policyVersionId1, exPolId.toString(), initiation2, RapConstants.TRNASPERIOD_PROSPECTIVE, RapConstants.STATUS_APPROVED, issuerHiosId, state);
			ppt.setCoverageDate(DATE.plusMonths(1).withDayOfMonth(1)); // FEB_1
			ppt.setFinancialProgramTypeCd(RapConstants.APTC);
			ppt.setPaymentAmount(aptc1);
			ppt.setPaymentCoverageStartDate(esd2);
			DateTime nextMonthLastDay = new DateTime(DATE.getYear(), DATE.plusMonths(1).getMonthOfYear(), DATE.plusMonths(1).dayOfMonth().getMaximumValue(), 0, 0);
			ppt.setPaymentCoverageEndDate(nextMonthLastDay); // FEB_MAX

			Long ppt3 = insertPolicyPaymentTrans(batchId, ppt);
			expectedParentPptId1 = ppt3;

			ppt.setFinancialProgramTypeCd(RapConstants.UF);
			ppt.setPaymentAmount(new BigDecimal("-3.5"));
			ppt.setTotalPremiumAmount(tpa2);
			ppt.setPaymentCoverageStartDate(esd2);

			insertPolicyPaymentTrans(batchId, ppt);

			// Make expected payments (in ASC order)
			expectedPaymentList.add(makePolicyPaymentTransDTO(policyVersionId2, exPolId.toString(), RapConstants.TRANSPERIOD_RETROACTIVE, 
					coverage2, RapConstants.APTC, expectedPA_1, expectedTPA_1, expectedStatus, issuerHiosId, expectedParentPptId1));
			expectedPaymentList.add(makePolicyPaymentTransDTO(policyVersionId2, exPolId.toString(), RapConstants.TRANSPERIOD_RETROACTIVE, 
					coverage2, RapConstants.APTC, expectedPA_2, expectedTPA_2, expectedStatus, issuerHiosId));


			// Launch Staging job
			JobExecution jobExStage = jobLauncherTestUtils.launchJob(jobParametersStage);
			stageId = jobExStage.getJobId();
			assertJobCompleted(jobExStage);
			//assertStagingRapIssuer(stageId, "00001", 2);

			// Launch RAP job
			JobExecution jobExRAP = jobLauncherTestUtils.launchJob(jobParametersRAP);
			jobId = jobExRAP.getJobId();
			batchBusinessIdRAP = jobExRAP.getExecutionContext().getString(RapConstants.BATCH_BUSINESS_ID);
			assertJobCompleted(jobExRAP);

			//assertPolicyPaymentTrans(batchBusinessIdRAP, expectedPaymentList);

		} finally {

			deleteRAPTestData(batchBusinessIdRAP);
			deleteBatchProcessLog(stageId);
			deleteBatchProcessLog(jobId);
			deleteStagingRapIssuer(jobId);
		}
	}



	/*
	 * 5.2.2	Scenario 2: Retroactive Change, but with Pending Cycle.
	 * - Similar test but existing payments have a "PAPPV" pending approval status,
	 * - Therefore, no new payments will be created, existing ones will be updated. 
	 */
	@Test
	public void test_2_PendingCycle() throws Exception {

		String state = "NC";

		PolicyStatus epsStatus = PolicyStatus.EFFECTUATED_2;
		DateTime transDateTime = DATE.plusMonths(3); // APR_1;

		DateTime coverage1 = DATE.withDayOfMonth(1); // JAN_1;
		DateTime erc1 = coverage1.minusDays(16); // DEC 16 
		DateTime initiation1 = coverage1.minusDays(12); // DEC 20

		DateTime coverage2 = DATE.plusMonths(1).withDayOfMonth(1); // FEB_1;
		DateTime erc2 = DATE.withDayOfMonth(16); // JAN_16;
		DateTime initiation2 = DATE.withDayOfMonth(20); // JAN_20;

		DateTime rollupStart1 = initiation1;
		DateTime rollupStart2 = initiation2;

		DateTime lastPolMaintStart = initiation1.plusHours(1);
		DateTime uniqueTransDateTime = transDateTime;
		BigDecimal flatRate = new BigDecimal("1");

		DateTime policyStart = DATE.withDayOfMonth(1); // JAN_1;
		DateTime policyEnd = null;
		DateTime maintStart = DATE.minusDays(3); // JAN_1.minusDays(5);
		DateTime maintEnd = DATE.minusHours(12); // JAN_28;


		DateTime issuerEsd = DATE.withDayOfMonth(1); // JAN_1;

		String batchBusinessIdRAP = null;
		Long stageId = null;
		Long jobId = null;
		Long batchId = getRandomNumber(9);
		Long exPolId = null;
		String issuerHiosId = null;
		PolicyDataDTO pdDTO = null;
		Long policyVersionId1 = null;
		Long policyVersionId2 = null;
		List<PolicyPremium> premiumList1 = new ArrayList<PolicyPremium>();
		List<PolicyPremium> premiumList2 = new ArrayList<PolicyPremium>();		

		// Expected PolicyPaymentTrans record values
		List<PolicyPaymentTransDTO> expectedPaymentList = new ArrayList<PolicyPaymentTransDTO>();
		String expectedStatus1 = RapConstants.STATUS_REPLACED;
		BigDecimal expectedPA_1 = new BigDecimal("50");
		BigDecimal expectedTPA_1 = null;
		String expectedStatus2 = RapConstants.STATUS_PENDING_CYCLE;
		BigDecimal expectedPA_2 = new BigDecimal("80");
		BigDecimal expectedTPA_2 = new BigDecimal("100");

		try {
			insertCoveragePeriodPaid(batchId, coverage1, erc1, initiation1);
			insertCoveragePeriodPaid(batchId, coverage2 , erc2, initiation2);
			insertIssuerUserFeeRate(batchId, state, "2", coverage1.getYear(), flatRate, initiation1, initiation1.plusYears(1));
			insertBatchProcessLog("aptcCsrUfRollupJob", USER_PREFIX + "rollup-"+ batchId, batchId, rollupStart1, BatchStatus.COMPLETED, null);
			Long prevBatchId = batchId - 1L;
			insertBatchProcessLog("aptcCsrUfRollupJob", USER_PREFIX + "rollup-"+ prevBatchId, prevBatchId, rollupStart2 , BatchStatus.COMPLETED, null);
			insertBatchProcessLog(RapConstants.RAP_RETRO_JOB, USER_PREFIX + batchId, batchId, initiation1.minusHours(1), BatchStatus.COMPLETED, lastPolMaintStart);

			// Refresh code/decode cache after inserting test records.
			CodeServiceImpl codeService = (CodeServiceImpl) context.getBean("codeService");
			codeService.reload();

			issuerHiosId = "00001";
			insertApprovedIssuer(batchId, issuerHiosId, issuerEsd);

			exPolId = Long.valueOf("101");

			// Insert 1st PolicyVersion
			DateTime esd1 = DATE.withDayOfMonth(1); // JAN_1;
			DateTime eed1 =  new DateTime(DATE.getYear(), DATE.getMonthOfYear(), DATE.dayOfMonth().getMaximumValue(), 0, 0); // JAN_31;
			BigDecimal aptc1 = new BigDecimal("50");
			BigDecimal tpa1 = new BigDecimal("100");
			BigDecimal csr1 = null;

			premiumList1.add(makePolicyPremium(esd1, eed1, aptc1, tpa1, csr1));

			pdDTO = makePolicyDataDTO(issuerHiosId, exPolId.toString(), state, policyStart, policyEnd, maintStart, epsStatus);
			policyVersionId1 = insertPolicyVersionForRap(batchId, uniqueTransDateTime, pdDTO, premiumList1, maintEnd, epsStatus);

			// Insert 2nd PolicyVersion (a day later)
			uniqueTransDateTime = uniqueTransDateTime.plusDays(1);
			// Set this policy maintStart to the previous policyMaintEnd
			maintStart = maintEnd.plusSeconds(1);
			maintEnd = HIGHDATE;
			// make the first premium of second policy same as first policy version.
			premiumList2.add(makePolicyPremium(esd1, eed1, aptc1, tpa1, csr1));
			// update premium values with new financial changes.
			DateTime esd2 = DATE.plusMonths(1).withDayOfMonth(1); // FEB_1;
			DateTime eed2 = null;
			BigDecimal aptc2 = new BigDecimal("80");
			BigDecimal tpa2 = new BigDecimal("100");
			BigDecimal csr2 = null;

			premiumList2.add(makePolicyPremium(esd2, eed2, aptc2, tpa2, csr2));

			pdDTO = makePolicyDataDTO(issuerHiosId, exPolId.toString(), state, policyStart, policyEnd, maintStart, epsStatus);
			policyVersionId2 = insertPolicyVersionForRap(batchId, uniqueTransDateTime, pdDTO, premiumList2, maintEnd, epsStatus);

			// Insert already processed Payments from previous job(s).

			//change status to pending cycle PCYC
			PolicyPaymentTrans ppt1 = makePolicyPaymentTrans(policyVersionId1, exPolId.toString(), initiation1, RapConstants.TRANSPERIOD_RETROACTIVE, RapConstants.STATUS_PENDING_CYCLE, issuerHiosId, state);
			ppt1.setCoverageDate(DATE.withDayOfMonth(1)); // JAN_1
			ppt1.setFinancialProgramTypeCd(RapConstants.APTC);
			ppt1.setPaymentAmount(aptc1);
			ppt1.setIssuerLevelTransactionId(Long.valueOf("111111"));
			ppt1.setPaymentCoverageStartDate(esd1);
			ppt1.setPaymentCoverageEndDate(eed1);

			insertPolicyPaymentTrans(batchId, ppt1);

			PolicyPaymentTrans ppt2 = makePolicyPaymentTrans(policyVersionId1, exPolId.toString(), initiation1, RapConstants.TRANSPERIOD_RETROACTIVE, RapConstants.STATUS_PENDING_CYCLE, issuerHiosId, state);
			ppt2.setCoverageDate(DATE.withDayOfMonth(1)); // JAN_1
			ppt2.setFinancialProgramTypeCd(RapConstants.UF);
			ppt2.setPaymentAmount(new BigDecimal("-3.5"));
			ppt2.setIssuerLevelTransactionId(Long.valueOf("111111"));
			ppt2.setPaymentCoverageStartDate(esd1);
			ppt2.setPaymentCoverageEndDate(eed1);
			ppt2.setTotalPremiumAmount(tpa1);

			insertPolicyPaymentTrans(batchId, ppt2);

			PolicyPaymentTrans ppt3 =  makePolicyPaymentTrans(policyVersionId1, exPolId.toString(), initiation2, RapConstants.TRNASPERIOD_PROSPECTIVE, RapConstants.STATUS_PENDING_CYCLE, issuerHiosId, state);
			ppt3.setCoverageDate(DATE.plusMonths(1).withDayOfMonth(1)); // FEB_1
			ppt3.setFinancialProgramTypeCd(RapConstants.APTC);
			ppt3.setPaymentAmount(aptc1);
			ppt3.setPaymentCoverageStartDate(esd2);
			DateTime nextMonthLastDay = new DateTime(DATE.getYear(), DATE.plusMonths(1).getMonthOfYear(), DATE.plusMonths(1).dayOfMonth().getMaximumValue(), 0, 0);
			ppt3.setPaymentCoverageEndDate(nextMonthLastDay); // FEB_MAX

			insertPolicyPaymentTrans(batchId, ppt3);

			PolicyPaymentTrans ppt4 =  makePolicyPaymentTrans(policyVersionId1, exPolId.toString(), initiation2, RapConstants.TRNASPERIOD_PROSPECTIVE, RapConstants.STATUS_PENDING_CYCLE, issuerHiosId, state);
			ppt4.setCoverageDate(DATE.plusMonths(1).withDayOfMonth(1)); // FEB_1
			ppt4.setFinancialProgramTypeCd(RapConstants.UF);
			ppt4.setPaymentAmount(new BigDecimal("-3.5"));
			ppt4.setPaymentCoverageStartDate(esd2);
			ppt4.setPaymentCoverageEndDate(nextMonthLastDay);  // FEB_MAX
			ppt4.setTotalPremiumAmount(tpa2);

			insertPolicyPaymentTrans(batchId, ppt4);

			// Make expected payments (in ASC order)
			expectedPaymentList.add(makePolicyPaymentTransDTO(policyVersionId1, exPolId.toString(), RapConstants.TRNASPERIOD_PROSPECTIVE, 
					coverage2, RapConstants.APTC, expectedPA_1, expectedTPA_1, expectedStatus1, issuerHiosId));
			expectedPaymentList.add(makePolicyPaymentTransDTO(policyVersionId2, exPolId.toString(), RapConstants.TRANSPERIOD_RETROACTIVE, 
					coverage2, RapConstants.APTC, expectedPA_2, expectedTPA_2, expectedStatus2, issuerHiosId));


			// Launch Staging job
			JobExecution jobExStage = jobLauncherTestUtils.launchJob(jobParametersStage);
			stageId = jobExStage.getJobId();
			assertJobCompleted(jobExStage);
			//assertStagingRapIssuer(stageId, "00001", 2);

			// Launch RAP job
			JobExecution jobExRAP = jobLauncherTestUtils.launchJob(jobParametersRAP);
			jobId = jobExRAP.getJobId();
			batchBusinessIdRAP = jobExRAP.getExecutionContext().getString(RapConstants.BATCH_BUSINESS_ID);
			assertJobCompleted(jobExRAP);

			//assertPolicyPaymentTrans(batchBusinessIdRAP, expectedPaymentList);

		} finally {

			deleteRAPTestData(batchBusinessIdRAP);
			deleteBatchProcessLog(stageId);
			deleteBatchProcessLog(jobId);
			deleteStagingRapIssuer(jobId);
		}
	}


}
