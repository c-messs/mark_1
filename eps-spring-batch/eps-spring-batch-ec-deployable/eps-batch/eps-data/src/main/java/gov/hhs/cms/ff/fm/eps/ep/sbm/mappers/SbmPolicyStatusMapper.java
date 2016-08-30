package gov.hhs.cms.ff.fm.eps.ep.sbm.mappers;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import gov.cms.dsh.sbmi.PolicyType;
import gov.hhs.cms.ff.fm.eps.ep.enums.PolicyStatus;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyStatusPO;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmPolicyStatusPO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMPolicyDTO;

/**
 * @author
 * 
 */
public class SbmPolicyStatusMapper {

	/**
	 * Translate SBM Policy status to EPS Policy status. All Statuses are maintained 
	 * per version and appended to the list of statuses in EPS.  
	 * An inbound status is added to EPS list of statuses.
	 * If inbound status does not change from EPS latest, do not add.
	 * @param policy
	 * @param epsList
	 * @return List<PolicyStatusPO>
	 */
	public List<SbmPolicyStatusPO> mapSbmToEps(SBMPolicyDTO inboundPolicyDTO, List<SbmPolicyStatusPO> epsList) {

		List<SbmPolicyStatusPO> poList = new ArrayList<SbmPolicyStatusPO>();

		if (epsList != null) {
			
			for(SbmPolicyStatusPO epsPO : epsList) {

				epsPO.setPolicyChanged(false);
				poList.add(epsPO);
			}
		}

		PolicyType policy = inboundPolicyDTO.getPolicy();

		if (policy != null) {

			PolicyStatus inboundPolicyStatus = determinePolicyStatus(policy);

			if (inboundPolicyStatus != null) {

				SbmPolicyStatusPO po = new SbmPolicyStatusPO();

				po.setInsuranacePolicyStatusTypeCd(inboundPolicyStatus.getValue());
				po.setTransDateTime(inboundPolicyDTO.getFileProcessDateTime());

				// if new inbound status is NOT the same as the latest existing EPS status, then add.
				// Since "order by TRANSDATETIME desc", grab the first one.
				// ie: status list could become: 2, 3, 2  but not 2, 2, 2
				if (CollectionUtils.isEmpty(epsList)) {
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


	private PolicyStatus determinePolicyStatus(PolicyType policy) {

		PolicyStatus status = null;

		if (policy.getEffectuationIndicator().equals("Y")) {

			status = PolicyStatus.EFFECTUATED_2;

		} else {

			status = PolicyStatus.CANCELLED_3;
		}
		return status;
	}




}
