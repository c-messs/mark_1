package gov.hhs.cms.ff.fm.eps.ep.sbm.mappers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import gov.hhs.cms.ff.fm.eps.ep.po.SbmPolicyPremiumPO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMPremium;



/**
 * The Mapper class for PolicyPremium table to translate inbound sbmPremium data to EPS.
 * 
 * Note: Premium data is extracted and processed in Business layer.  Processed sbmPremiums
 * are set in PolicyDTO.
 *
 * @author EPS
 */
public class SbmPolicyPremiumMapper {

	/**
	 * Maps/translates "processed" SBMPremiums from business validation to EPS PO.  SBMPremiums are
	 * processed from inbound SBM Policy FinancialInformation.
	 * @param sbmPremiumList
	 * @param epsPremiumList
	 * @return
	 */
	public List<SbmPolicyPremiumPO> mapSbmToStaging(Map<LocalDate, SBMPremium> inboundPremiums, List<SbmPolicyPremiumPO> epsPremiums) {

		List<SbmPolicyPremiumPO> poList = new ArrayList<SbmPolicyPremiumPO>();

		List<LocalDate> esdKeys = new ArrayList<LocalDate>(inboundPremiums.keySet());

		for (LocalDate esdKey : esdKeys) {

			SBMPremium sbmPremium = inboundPremiums.get(esdKey);

			SbmPolicyPremiumPO po = new SbmPolicyPremiumPO();

			po.setEffectiveStartDate(sbmPremium.getEffectiveStartDate());
			po.setEffectiveEndDate(sbmPremium.getEffectiveEndDate());
			po.setTotalPremiumAmount(sbmPremium.getTotalPremium());
			po.setOtherPaymentAmount1(sbmPremium.getOtherPayment1());
			po.setOtherPaymentAmount2(sbmPremium.getOtherPayment2());
			po.setExchangeRateArea(sbmPremium.getRatingArea());
			po.setIndividualResponsibleAmount(sbmPremium.getIndividualResponsibleAmt());
			po.setCsrAmount(sbmPremium.getCsr());
			po.setAptcAmount(sbmPremium.getAptc());
			po.setProratedPremiumAmount(sbmPremium.getProratedPremium());
			po.setProratedAptcAmount(sbmPremium.getProratedAptc());
			po.setProratedCsrAmount(sbmPremium.getProratedCsr());
			po.setInsrncPlanVariantCmptTypeCd(sbmPremium.getCsrVariantId());

			poList.add(po);
		}

		determinePolicyChanged(poList, epsPremiums);

		return poList;
	}


	/**
	 * Determine if any data has changed and/or merge EPS latest data with empty inbound data.
	 * @param poLatestList
	 * @param poList
	 */
	private void determinePolicyChanged(List<SbmPolicyPremiumPO> inboundPremiums, List<SbmPolicyPremiumPO> epsPremiums) {

		if (epsPremiums != null) {
			// Determine if each resulting inbound premium exists EPS latest list.
			for (SbmPolicyPremiumPO po :  inboundPremiums) {

				if (epsPremiums.contains(po)) {
					po.setPolicyChanged(false);
				}
			}
		}
	}








}
