package gov.hhs.cms.ff.fm.eps.ep.mappers;

import gov.cms.dsh.bem.AdditionalInfoType;
import gov.cms.dsh.bem.MemberType;
import gov.hhs.cms.ff.fm.eps.ep.BEMDataUtil;
import gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentMaintenanceDTO;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyPremiumPO;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;



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
		
		Map<LocalDate, AdditionalInfoType> inboundPremiums = bemDTO.getEpsPremiums();
		
		MemberType subscriber = BEMDataUtil.getSubscriberMember(bemDTO.getBem());

		List<LocalDate> esdKeys = new ArrayList<LocalDate>(inboundPremiums.keySet());

		for (LocalDate esdKey : esdKeys) {

			AdditionalInfoType ait = inboundPremiums.get(esdKey);

			if (ait != null) {

				PolicyPremiumPO po = new PolicyPremiumPO();

				po.setEffectiveStartDate(DateTimeUtil.getLocalDateFromXmlGC(ait.getEffectiveStartDate()));
				po.setEffectiveEndDate(DateTimeUtil.getLocalDateFromXmlGC(ait.getEffectiveEndDate()));

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
