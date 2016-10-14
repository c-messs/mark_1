package gov.hhs.cms.ff.fm.eps.ep.sbm.mappers;

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
		PolicyStatus[] expectedPolicyStatus = {PolicyStatus.EFFECTUATED_2, PolicyStatus.CANCELLED_3, PolicyStatus.TERMINATED_4};
		String[] expectedXsdValue = {"Effectuated", "Cancelled", "Terminated"};

		List<SbmFileSummaryMissingPolicyData> dataList = new ArrayList<SbmFileSummaryMissingPolicyData>();

		for (int i = 0; i < expectedPolicyStatus.length; ++i) {
			SbmFileSummaryMissingPolicyData data = new SbmFileSummaryMissingPolicyData();
			data.setExchangePolicyId(expectedExPolId + i);
			data.setPlanId(expectedPlanId+ i);
			data.setPolicyStatus(expectedPolicyStatus[i]);

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

}
