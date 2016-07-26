package gov.hhs.cms.ff.fm.eps.ep.mappers;

import gov.cms.dsh.bem.AdditionalInfoType;
import gov.cms.dsh.bem.MemberType;
import gov.hhs.cms.ff.fm.eps.ep.BEMDataUtil;
import gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentMaintenanceDTO;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyPremiumPO;
import gov.hhs.cms.ff.fm.eps.ep.util.EpsDateUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;


/**
 * The Mapper class for PolicyPremium table to translate inbound BEM premium data to EPS.
 * 
 * Note: Premium data is extracted and processed in Business layer.  Processed premiums
 * are set in BemDTO.
 *
 * @author EPS
 */
public class PolicyPremiumMapper {

	/**
	 * Maps from Premium Map to EPS
	 * @param bemDTO
	 * @return
	 */
	public List<PolicyPremiumPO> mapFFMToEPS(BenefitEnrollmentMaintenanceDTO bemDTO) {

		List<PolicyPremiumPO> poList = new ArrayList<PolicyPremiumPO>();
		
		Map<DateTime, AdditionalInfoType> inboundPremiums = bemDTO.getEpsPremiums();
		
		MemberType subscriber = BEMDataUtil.getSubscriberMember(bemDTO.getBem());

		List<DateTime> esdKeys = new ArrayList<DateTime>(inboundPremiums.keySet());

		for (DateTime esdKey : esdKeys) {

			AdditionalInfoType ait = inboundPremiums.get(esdKey);

			if (ait != null) {

				PolicyPremiumPO po = new PolicyPremiumPO();

				po.setEffectiveStartDate(EpsDateUtils.getDateTimeFromXmlGC(ait.getEffectiveStartDate()));
				po.setEffectiveEndDate(EpsDateUtils.getDateTimeFromXmlGC(ait.getEffectiveEndDate()));

				po.setAptcAmount(ait.getAPTCAmount());
				po.setCsrAmount(ait.getCSRAmount());
				po.setTotalPremiumAmount(ait.getTotalPremiumAmount());
				po.setIndividualResponsibleAmount(ait.getTotalIndividualResponsibilityAmount());
				po.setExchangeRateArea(ait.getRatingArea());
				
				po.setProratedAptcAmount(ait.getProratedAppliedAPTCAmount());
				po.setProratedCsrAmount(ait.getProratedCSRAmount());
				po.setProratedPremiumAmount(ait.getProratedMonthlyPremiumAmount());
				po.setProratedInddResponsibleAmount(ait.getProratedIndividualResponsibleAmount());
				po.setInsrncPlanVariantCmptTypeCd(BEMDataUtil.getVariantId(subscriber));

				poList.add(po);
			}
		}

		return poList;
	}

}
