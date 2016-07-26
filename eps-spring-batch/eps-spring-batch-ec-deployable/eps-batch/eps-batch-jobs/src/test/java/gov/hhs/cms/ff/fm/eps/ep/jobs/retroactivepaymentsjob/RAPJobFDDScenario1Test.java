package gov.hhs.cms.ff.fm.eps.ep.jobs.retroactivepaymentsjob;

import gov.hhs.cms.ff.fm.eps.ep.enums.PolicyStatus;
import gov.hhs.cms.ff.fm.eps.rap.domain.PolicyPremium;
import gov.hhs.cms.ff.fm.eps.rap.domain.RapConstants;
import gov.hhs.cms.ff.fm.eps.rap.dto.PolicyDataDTO;
import gov.hhs.cms.ff.fm.eps.rap.dto.PolicyPaymentTransDTO;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
public class RAPJobFDDScenario1Test extends BaseRapBatchTest {

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
	 * Test Staging and RAP Job (end to end)
	 * - Inserts CoveragePaidPeriod record.
	 * - Inserts required BatchProcessLogs from other jobs (aptcCsrUfRollupJob and previous completed RAP job)
	 * - Confirms if STAGINGRAPISSUER table is populated or not with the issuerHiosIds by SubscriberStateCd
	 *     and the correct POLICYVERSIONQUANTITY based on the number of policies that
	 *     contain a policyStatus of '2' in its status list.
	 * - Confirms policyPaymentTrans records are created.
	 * - Removes all data entered.
	 */
	@Test
	public void test_1() throws Exception {

		String state = "NC";

		PolicyStatus epsStatus = PolicyStatus.EFFECTUATED_2;
		DateTime transDateTime = APR_1;
		DateTime coverage = JAN_1;
		DateTime erc = coverage.minusDays(16); // DEC 16
		DateTime initiation = coverage.minusDays(12); // DEC 20
		DateTime rollupStart = initiation;
		DateTime lastPolMaintStart = DateTime.now().minusHours(2);
		DateTime uniqueTransDateTime = transDateTime;
		BigDecimal flatRate = new BigDecimal("1");

		DateTime policyStart = JAN_1;
		DateTime policyEnd = null;
		DateTime maintStart = DateTime.now().minusHours(1); // DEC 25 previous year
		DateTime maintEnd = HIGHDATE;
		DateTime esd = JAN_1;
		DateTime eed = null;
		BigDecimal aptc = new BigDecimal("50");
		BigDecimal tpa = new BigDecimal("100");
		BigDecimal csr = null;

		DateTime issuerEsd = JAN_1;

		String batchBusinessIdRAP = null;
		Long stageId = null;
		Long jobId = null;
		Long batchId = getRandomNumber(9);
		Long exPolId = null;
		String issuerHiosId = null;
		PolicyDataDTO pdDTO = null;
		List<PolicyPremium> premiumList = new ArrayList<PolicyPremium>();
		Long policyVersionId = null;

		// Expected PolicyPaymentTrans record values
		List<PolicyPaymentTransDTO> expectedPaymentList = new ArrayList<PolicyPaymentTransDTO>();
		String expectedStatus = RapConstants.STATUS_PENDING_CYCLE;
		BigDecimal expectedPA_1 = new BigDecimal("50");
		BigDecimal expectedTPA_1 = new BigDecimal("100");
		BigDecimal expectedPA_2 = null;
		BigDecimal expectedTPA_2 = new BigDecimal("100");


		try {
			insertCoveragePeriodPaid(batchId, coverage, erc, initiation);
			insertIssuerUserFeeRate(batchId, state, "2", coverage.getYear(), flatRate, initiation, initiation.plusYears(1));
			insertBatchProcessLog("aptcCsrUfRollupJob", USER_PREFIX + "rollup-"+ batchId, batchId, rollupStart, BatchStatus.COMPLETED, null);
			insertBatchProcessLog(RapConstants.RAP_RETRO_JOB, USER_PREFIX + batchId, batchId, initiation.minusHours(1), BatchStatus.COMPLETED, lastPolMaintStart);

			// Refresh code/decode cache after inserting test records.
			CodeServiceImpl codeService = (CodeServiceImpl) context.getBean("codeService");
			codeService.reload();

			issuerHiosId = "00001";
			insertApprovedIssuer(batchId, issuerHiosId, issuerEsd);

			exPolId = Long.valueOf("101");
			premiumList.add(makePolicyPremium(esd, eed, aptc, tpa, csr));
			// Set policy premiums aside for later assertions.

			pdDTO = makePolicyDataDTO(issuerHiosId, exPolId.toString(), state, policyStart, policyEnd, maintStart, epsStatus);
			uniqueTransDateTime = uniqueTransDateTime.plusDays(1);
			policyVersionId = insertPolicyVersionForRap(batchId, DateTime.now(), pdDTO, premiumList, maintEnd, epsStatus);
			
			// Make expected payments (in ASC order)
			expectedPaymentList.add(makePolicyPaymentTransDTO(policyVersionId, exPolId.toString(), RapConstants.TRANSPERIOD_RETROACTIVE, 
					coverage, RapConstants.APTC, expectedPA_1, expectedTPA_1, expectedStatus, null));
			expectedPaymentList.add(makePolicyPaymentTransDTO(policyVersionId, exPolId.toString(), RapConstants.TRANSPERIOD_RETROACTIVE, 
					coverage, RapConstants.UF, expectedPA_2, expectedTPA_2, expectedStatus, null));

			// Launch Staging job
			JobExecution jobExStage = jobLauncherTestUtils.launchJob(jobParametersStage);
			stageId = jobExStage.getJobId();
			assertJobCompleted(jobExStage);
			assertStagingRapIssuer(stageId, "00001", 1);

			// Launch RAP job
			JobExecution jobExRAP = jobLauncherTestUtils.launchJob(jobParametersRAP);
			jobId = jobExRAP.getJobId();
			batchBusinessIdRAP = jobExRAP.getExecutionContext().getString(RapConstants.BATCH_BUSINESS_ID);
			assertJobCompleted(jobExRAP);
			assertPolicyPaymentTrans(batchBusinessIdRAP, expectedPaymentList);

		} finally {

			deleteRAPTestData(batchBusinessIdRAP);
			deleteBatchProcessLog(stageId);
			deleteBatchProcessLog(jobId);
			deleteStagingRapIssuer(jobId);
		}
	}
	
	
	/*
	 * Test Rap Staging Job only (end to end)
	 *  To test that RAP is handling multiple issuer ids and not attempting to process them again.
	 * - Inserts required BatchProcessLogs from other jobs and CoveragePaidPeriod record.
	 * - Inserts 3 policies per ExchangePolicyId (INITIAL, EFFECTUATION, TERM)
	 * - Confirms if STAGINGRAPISSUER table is populated or not with the issuerHiosIds by SubscriberStateCd
	 *   and the correct POLICYVERSIONQUANTITY based on the number of policies that
	 *   contain a policyStatus of '2' in its status list.
	 * - Removes all data entered.
	 */
	@Test
	public void test_Multiple() throws Exception {

		// Since inserting 3 policies per ExchangePolicyId, a count of 2 effectuated policies will be returned.
		// Two policies will contain a status of '2'
		// - Initial - with policyStatus list: (1)
		// - Effectuation -  with policyStatus list: (1, 2)
		// - Termination -  with policyStatus list:( 1, 2, 4)

		String[] states = {"NC", "ND", "TX", "TX", "TX", "VA"};
		
		PolicyStatus[] epsStatus = {PolicyStatus.INITIAL_1, 
				PolicyStatus.EFFECTUATED_2, PolicyStatus.TERMINATED_4};
		DateTime transDateTime = APR_1;
		DateTime coverage = FEB_1;
		DateTime erc = MAR_15;
		DateTime initiation = JAN_1;
		DateTime rollupStart = initiation;
		DateTime lastPolMaintStart = DateTime.now().minusDays(1).withTimeAtStartOfDay();
		DateTime uniqueTransDateTime = transDateTime;

		DateTime policyStart = JAN_1;
		DateTime policyEnd = MAR_31;
		// Since 3 policies, align "Maintanence" dates. (1 second gap not critical for this test)
		DateTime[] maintStart = { DateTime.now().minusHours(3), DateTime.now().minusHours(2), DateTime.now().minusHours(1) };
		DateTime[] maintEnd = { DateTime.now().minusHours(3), DateTime.now().minusHours(2), HIGHDATE };
		DateTime ppEsd = JAN_1;
		DateTime ppEed = MAR_31;
		DateTime issuerEsd = JAN_1;

		String batchBusinessId = null;
		String batchBusinessProcessId = null;
		Long stageId = null;
		Long jobId = null;
		Long batchId = getRandomNumber(9);
		Long exPolId = null;
		String hiosId = null;
		PolicyDataDTO pdDTO = null;
		List<PolicyPremium> premiumList = null;
		List<String> expectedIssuerHiosIdList = new ArrayList<String>();

		try {
			insertCoveragePeriodPaid(batchId, coverage, erc, initiation);
			insertBatchProcessLog("aptcCsrUfRollupJob", USER_PREFIX + "rollup-"+ batchId, batchId, rollupStart, BatchStatus.COMPLETED, null);
			insertBatchProcessLog(RapConstants.RAP_RETRO_JOB, USER_PREFIX + batchId, batchId, initiation.minusHours(1), BatchStatus.COMPLETED, lastPolMaintStart);

			// Refresh code/decode cache after inserting test records.
			CodeServiceImpl codeService = (CodeServiceImpl) context.getBean("codeService");
			codeService.reload();

			for (int i = 0; i < states.length; ++i) {

				hiosId = i + "" + i + "" + i + "" + i + "" + i;
				insertApprovedIssuer(batchId, hiosId, issuerEsd);
				exPolId = getRandomNumber(9);

				// Don't add any policies for ND.
				if (!states[i].equals("ND")) {

					premiumList = new ArrayList<PolicyPremium>();
					premiumList.add(makePolicyPremium(ppEsd, ppEed, i));

					for (int j = 0; j < epsStatus.length; ++j) {

						pdDTO = makePolicyDataDTO(hiosId, exPolId.toString(), states[i], policyStart, policyEnd, maintStart[j], epsStatus[j]);
						uniqueTransDateTime = uniqueTransDateTime.plusDays(1);
						insertPolicyVersionForRap(batchId, uniqueTransDateTime, pdDTO, premiumList, maintEnd[j], epsStatus, j);
					}
					expectedIssuerHiosIdList.add(hiosId);
				}
			}

			// Launch Staging job
			JobExecution jobExStage = jobLauncherTestUtils.launchJob(jobParametersStage);
			stageId = jobExStage.getJobId();
			batchBusinessId = jobExStage.getExecutionContext().getString(RapConstants.BATCH_BUSINESS_ID);
			assertJobCompleted(jobExStage);

			// Launch Staging job
			JobExecution jobExec = jobLauncherTestUtils.launchJob(jobParametersRAP);
			jobId = jobExec.getJobId();
			batchBusinessProcessId = jobExec.getExecutionContext().getString(RapConstants.BATCH_BUSINESS_ID);
			assertJobCompleted(jobExec);


		} finally {
			deleteRAPTestData(batchBusinessProcessId);
			deleteRAPTestData(batchBusinessId);
			deleteBatchProcessLog(stageId);
			deleteStagingRapIssuer(stageId);
			deleteBatchProcessLog(jobId);
		}
	}

	
	
	
	private PolicyPremium makePolicyPremium(DateTime esd, DateTime eed, int i) {

		PolicyPremium pp = new PolicyPremium();
		pp.setEffectiveStartDate(esd);
		pp.setEffectiveEndDate(eed);

		BigDecimal tpa = new BigDecimal("1000");

		tpa = tpa.add(new BigDecimal(i));
		BigDecimal aptc = tpa.multiply(new BigDecimal(".1"));
		BigDecimal csr = new BigDecimal(i);

		pp.setTotalPremiumAmount(tpa);
		pp.setAptcAmount(aptc);
		pp.setCsrAmount(csr);

		BigDecimal proAptc = aptc.divide(new BigDecimal(".8")).setScale(2, RoundingMode.HALF_UP);
		BigDecimal proCsr = i == 0 ? BigDecimal.ZERO : csr.divide(new BigDecimal(".8")).setScale(2, RoundingMode.HALF_UP);
		BigDecimal proMpa = tpa.divide(new BigDecimal(".8")).setScale(2, RoundingMode.HALF_UP);

		pp.setProratedAptcAmount(proAptc);
		pp.setProratedCsrAmount(proCsr);
		pp.setProratedPremiumAmount(proMpa);

		return pp;		
	}
	
}
