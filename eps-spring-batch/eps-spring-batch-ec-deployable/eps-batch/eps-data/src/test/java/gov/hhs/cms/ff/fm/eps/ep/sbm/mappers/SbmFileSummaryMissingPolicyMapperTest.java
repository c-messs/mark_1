package gov.hhs.cms.ff.fm.eps.ep.sbm.mappers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import gov.cms.dsh.sbmr.MissingPolicyType;
import gov.hhs.cms.ff.fm.eps.ep.enums.PolicyStatus;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmFileSummaryMissingPolicyData;

public class SbmFileSummaryMissingPolicyMapperTest extends SBMBaseMapperTest {

	private SbmFileSummaryMissingPolicyMapper mapper = new SbmFileSummaryMissingPolicyMapper();

	@Test
	public void test_mapEpsToSbmr() {

		String expectedExPolId = "EXPOLID-";
		String expectedPlanId = "PLANID-";
		// EPS does not store 4 for TERM, it is derived based on PED and job run dateTime.
		PolicyStatus[] expectedPolicyStatus = {PolicyStatus.EFFECTUATED_2, PolicyStatus.CANCELLED_3, PolicyStatus.TERMINATED_4};
		String[] expectedXsdValue = {"Effectuated", "Cancelled", "Terminated"};
		// Set the PED in the future so test will not fail in December.
		LocalDate ped = DEC_31.plusMonths(2);

		List<SbmFileSummaryMissingPolicyData> dataList = new ArrayList<SbmFileSummaryMissingPolicyData>();

		PolicyStatus[] epsPolicyStatus = {PolicyStatus.EFFECTUATED_2, PolicyStatus.CANCELLED_3, PolicyStatus.EFFECTUATED_2};
		
		for (int i = 0; i < expectedPolicyStatus.length; ++i) {
			SbmFileSummaryMissingPolicyData data = new SbmFileSummaryMissingPolicyData();
			data.setExchangePolicyId(expectedExPolId + i);
			data.setPlanId(expectedPlanId+ i);
			data.setPolicyStatus(epsPolicyStatus[i]);
			if (i == 2) {
				// For the TERM policy, set the PED less than job run date.
				data.setPolicyEndDate(LocalDate.now().minusDays(1));
			} else {
				data.setPolicyEndDate(ped);
			}
			dataList.add(data);
		}

		List<MissingPolicyType> actualList = mapper.mapEpsToSbmr(dataList);

		assertEquals("MissingPolicyType list size", dataList.size(), actualList.size());

		int idx = 0;
		for (MissingPolicyType actual : actualList) {

			assertEquals(idx + ") ExchangeAssignedPolicyId", expectedExPolId + idx, actual.getExchangeAssignedPolicyId());
			assertEquals(idx + ") QHPId", expectedPlanId + idx, actual.getQHPId());
			assertEquals(idx + ") CurrentCMSPolicyStatus as description", expectedPolicyStatus[idx].getDescription(), actual.getCurrentCMSPolicyStatus());
			assertNotSame(idx + ") CurrentCMSPolicyStatus as EPS value", expectedPolicyStatus[idx].getValue(), actual.getCurrentCMSPolicyStatus());
			assertEquals(idx + ") XSD Value", expectedXsdValue[idx], actual.getCurrentCMSPolicyStatus());
			idx++;
		}

	}
	
	@Test
	public void test_determineTerminated_Same() {

		boolean expected = true;
		LocalDate ped = LocalDate.now();
		boolean actual =  mapper.determineTerminated(ped);	
		assertEquals("should be " + expected + " for PolicyEndDate=" + ped.toString(), expected, actual);
	}
	
	@Test
	public void test_determineTerminated_After() {

		boolean expected = false;
		LocalDate ped = LocalDate.now().plusMonths(2);
		boolean actual =  mapper.determineTerminated(ped);
		assertEquals("should be " + expected + " for PolicyEndDate=" + ped.toString(), expected, actual);
	}
	
	@Test
	public void test_determineTerminated_Before() {

		boolean expected = true;	
		LocalDate ped = LocalDate.now().minusMonths(9);
		boolean actual =  mapper.determineTerminated(ped);
		assertEquals("should be " + expected + " for PolicyEndDate=" + ped.toString(), expected, actual);
	}

}
