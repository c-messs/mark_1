package gov.hhs.cms.ff.fm.eps.ep.sbm.mappers;

import java.util.ArrayList;
import java.util.List;

import gov.cms.dsh.sbmr.MissingPolicyType;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmFileSummaryMissingPolicyData;

/**
 * @author j.radziewski
 *
 */
public class SbmFileSummaryMissingPolicyMapper {

	/**
	 * 
	 * @param dataList
	 * @return missingPolicyList
	 */
	public List<MissingPolicyType> mapEpsToSbmr(List<SbmFileSummaryMissingPolicyData> dataList) {

		List<MissingPolicyType> missingPolicyList = new ArrayList<MissingPolicyType>();

		if (dataList != null) {

			for (SbmFileSummaryMissingPolicyData data : dataList) {

				MissingPolicyType missingPolicy = new MissingPolicyType();

				missingPolicy.setExchangeAssignedPolicyId(data.getExchangePolicyId());
				missingPolicy.setQHPId(data.getPlanId());
				missingPolicy.setCurrentCMSPolicyStatus(data.getPolicyStatus().getDescription());

				missingPolicyList.add(missingPolicy);
			}
		}

		return missingPolicyList;
	}

}
