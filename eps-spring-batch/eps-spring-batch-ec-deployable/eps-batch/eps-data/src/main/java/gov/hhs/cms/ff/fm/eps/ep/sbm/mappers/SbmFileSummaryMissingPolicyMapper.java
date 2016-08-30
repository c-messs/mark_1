package gov.hhs.cms.ff.fm.eps.ep.sbm.mappers;

import java.util.ArrayList;
import java.util.List;

import gov.cms.dsh.sbmr.MissingPolicyType;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmFileSummaryMissingPolicyData;

public class SbmFileSummaryMissingPolicyMapper {


	public List<MissingPolicyType> mapEpsToSbmr(List<SbmFileSummaryMissingPolicyData> dataList) {

		List<MissingPolicyType> missingPolicyList = new ArrayList<MissingPolicyType>();

		if (dataList != null) {

			for (SbmFileSummaryMissingPolicyData data : dataList) {

				MissingPolicyType missingPolicy = new MissingPolicyType();

				missingPolicy.setExchangeAssignedPolicyId(data.getExchangePolicyId());
				missingPolicy.setQHPId(data.getPlanId());
				missingPolicy.setCurrentCMSPolicyStatus(data.getPolicyStatus().getValue());

				missingPolicyList.add(missingPolicy);
			}
		}

		return missingPolicyList;
	}

}
