package gov.hhs.cms.ff.fm.eps.ep.mappers;

import gov.cms.dsh.bem.BenefitEnrollmentMaintenanceType;
import gov.hhs.cms.ff.fm.eps.ep.BEMDataUtil;
import gov.hhs.cms.ff.fm.eps.ep.enums.PolicyStatus;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyStatusPO;
import gov.hhs.cms.ff.fm.eps.ep.util.EpsDateUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * 
 */
public class PolicyStatusMapper {

	/**
	 * All Statuses are maintained per version and appended to the list of 
	 * statuses in EPS.
	 * If inbound status does not change from EPS latest, do not add.
	 * @param bem
	 * @param epsList
	 * @return List<PolicyStatusPO>
	 */
	public List<PolicyStatusPO> mapFFMToEPS(BenefitEnrollmentMaintenanceType bem, List<PolicyStatusPO> epsList) {

		List<PolicyStatusPO> poList = new ArrayList<PolicyStatusPO>();

		for(PolicyStatusPO epsPO : epsList) {

			poList.add(epsPO);
		}

		if (bem != null) {

			PolicyStatus inboundPolicyStatus = BEMDataUtil.getPolicyStatus(bem);
			
			if (inboundPolicyStatus != null) {

				PolicyStatusPO po = new PolicyStatusPO();

				po.setInsuranacePolicyStatusTypeCd(inboundPolicyStatus.getValue());
				po.setTransDateTime(EpsDateUtils.getDateTimeFromXmlGC(BEMDataUtil.getCurrentTimeStamp(bem)));

				// if new inbound status is NOT the same as the latest existing EPS status, then add
				// Since "order by TRANSDATETIME desc", grab the first one.
				// ie: status list could become: 1, 2, 3, 2  but not 1, 2, 2, 2
				if (epsList.isEmpty()) {
					// If no EPS status, add inbound
					poList.add(po);
					
				} else {
					
					PolicyStatusPO epsPOLatest = epsList.get(0);
					// If inbound status is different then EPS status, add to the list.
					if(!po.equals(epsPOLatest)) {

						poList.add(po);
					}
				}
			}
		}

		return poList;
	}

}
