package gov.hhs.cms.ff.fm.eps.ep.sbm.mappers;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import gov.cms.dsh.sbmr.MissingPolicyType;
import gov.hhs.cms.ff.fm.eps.ep.enums.PolicyStatus;
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
				
				if (data.getPolicyStatus() != null) {
					
					if (PolicyStatus.EFFECTUATED_2.equals(data.getPolicyStatus())) {
						
						if (determineTerminated(data.getPolicyEndDate())) {
							missingPolicy.setCurrentCMSPolicyStatus(PolicyStatus.TERMINATED_4.getDescription());
						} else {
							missingPolicy.setCurrentCMSPolicyStatus(PolicyStatus.EFFECTUATED_2.getDescription());
						}
					} else {
						// CANCELLED_3
						missingPolicy.setCurrentCMSPolicyStatus(data.getPolicyStatus().getDescription());
					}
				}
				missingPolicyList.add(missingPolicy);
			}
		}

		return missingPolicyList;
	}
	
	/**
	 *  Policy termination determination	FR-FM-PP-SBMI-541
	 *	System shall identify an effectuated policy as terminated when 
	 *  the end date month of the policy is less than or equal to the 
	 *	processing month of SBMI ingestion job.
	 * @param policyEndDate (of Effectuated Policy)
	 * @return
	 */
	public boolean determineTerminated(LocalDate policyEndDate) {
		
		boolean isTerm = false;

		if (policyEndDate != null) {
			
			YearMonth pedYrMon = YearMonth.of(policyEndDate.getYear(), policyEndDate.getMonthValue());
			YearMonth curYrMon = YearMonth.now();

			if (pedYrMon.isBefore(curYrMon) || pedYrMon.equals(curYrMon)) {
				isTerm = true;
			}
		}
		return isTerm;
	}

}
