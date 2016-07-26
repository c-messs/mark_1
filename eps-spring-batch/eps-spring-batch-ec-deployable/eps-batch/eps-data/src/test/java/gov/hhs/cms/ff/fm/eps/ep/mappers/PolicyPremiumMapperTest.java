package gov.hhs.cms.ff.fm.eps.ep.mappers;

import gov.cms.dsh.bem.AdditionalInfoType;
import gov.cms.dsh.bem.MemberType;
import gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentMaintenanceDTO;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyPremiumPO;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.junit.Test;

public class PolicyPremiumMapperTest extends BaseMapperTest {

	private PolicyPremiumMapper mapper = new PolicyPremiumMapper();

	@Test
	public void test_mapFFMToEPS() {

		Map<DateTime, AdditionalInfoType> epsPremiums = new HashMap<DateTime, AdditionalInfoType>();

		DateTime esd = JAN_1;
		DateTime eed = DEC_31;
		BigDecimal aptc = new BigDecimal("11.11");
		BigDecimal csr = new BigDecimal("22.22");
		BigDecimal tpa = new BigDecimal("33.33");
		BigDecimal tira = new BigDecimal("44.44");
		BigDecimal proAptc = new BigDecimal("55.55");
		BigDecimal proCsr = new BigDecimal("66.66");
		BigDecimal proMpa = new BigDecimal("77.77");
		BigDecimal proTira = new BigDecimal("88.88");
		
		
		String ra = "007";
		String csrVariant = "06";

		// Expected data after mapping, Record 1
		DateTime expectedESD_EPS1 = esd;
		DateTime expectedEED_EPS1 = eed;
		BigDecimal expectedAPTC_EPS1 = aptc;
		BigDecimal expectedCSR_EPS1 = csr;
		BigDecimal expectedTPA_EPS1 = tpa;
		BigDecimal expectedTIRA_EPS1 = tira;
		BigDecimal expectedPRO_APTC_EPS1 = proAptc;
		BigDecimal expectedPRO_CSR_EPS1 = proCsr;
		BigDecimal expectedPRO_MPA_EPS1 = proMpa;
		BigDecimal expectedPRO_TIRA_EPS1 = proTira;
		String expectedRA_EPS1 = ra;
		String expectedIPVC_EPS1 = csrVariant;

		int expectedEPSRecords = 1;
		
		BenefitEnrollmentMaintenanceDTO bemDTO = makeBemDTO();
		MemberType subscriber = makeSubscriber("DAD", csrVariant);

		AdditionalInfoType ait = makeAdditionalInfoType(esd,eed);
		ait.setAPTCAmount(aptc);
		ait.setCSRAmount(csr);
		ait.setTotalPremiumAmount(tpa);
		ait.setTotalIndividualResponsibilityAmount(tira);
		ait.setProratedAppliedAPTCAmount(proAptc);
		ait.setProratedCSRAmount(proCsr);
		ait.setProratedMonthlyPremiumAmount(proMpa);
		ait.setProratedIndividualResponsibleAmount(proTira);
		ait.setRatingArea(ra);

		epsPremiums.put(esd, ait);
		
		bemDTO.setEpsPremiums(epsPremiums);
		bemDTO.getBem().getMember().add(subscriber);

		List<PolicyPremiumPO> actualPoList = mapper.mapFFMToEPS(bemDTO);

		assertEquals("PolicyPremium list size", expectedEPSRecords, actualPoList.size());

		PolicyPremiumPO poActual1 = actualPoList.get(0);

		assertPolicyPremiumPO(1, APTC, expectedESD_EPS1, expectedEED_EPS1, expectedAPTC_EPS1, poActual1);
		assertPolicyPremiumPO(1, CSR, expectedESD_EPS1, expectedEED_EPS1, expectedCSR_EPS1, poActual1);
		assertPolicyPremiumPO(1, TPA, expectedESD_EPS1, expectedEED_EPS1, expectedTPA_EPS1, poActual1);
		assertPolicyPremiumPO(1, TIRA, expectedESD_EPS1, expectedEED_EPS1, expectedTIRA_EPS1, poActual1);
		assertPolicyPremiumPO(1, PRO_APTC, expectedESD_EPS1, expectedEED_EPS1, expectedPRO_APTC_EPS1, poActual1);
		assertPolicyPremiumPO(1, PRO_CSR, expectedESD_EPS1, expectedEED_EPS1, expectedPRO_CSR_EPS1, poActual1);
		assertPolicyPremiumPO(1, PRO_MPA, expectedESD_EPS1, expectedEED_EPS1, expectedPRO_MPA_EPS1, poActual1);
		assertPolicyPremiumPO(1, PRO_TIRA, expectedESD_EPS1, expectedEED_EPS1, expectedPRO_TIRA_EPS1, poActual1);
		assertPolicyPremiumPO(1, RA, expectedESD_EPS1, expectedEED_EPS1, expectedRA_EPS1, poActual1);
		assertPolicyPremiumPO(1, IPVC, expectedESD_EPS1, expectedEED_EPS1, expectedIPVC_EPS1, poActual1);
	}

	@Test
	public void test_mapFFMToEPS_Empty_Premiums() {

		int expectedEPSRecords = 0;
		BenefitEnrollmentMaintenanceDTO bemDTO = makeBemDTO();
		Map<DateTime, AdditionalInfoType> epsPremiums = new HashMap<DateTime, AdditionalInfoType>();
		bemDTO.setEpsPremiums(epsPremiums);
		List<PolicyPremiumPO> actualPoList = mapper.mapFFMToEPS(bemDTO);
		assertEquals("PolicyPremium list size", expectedEPSRecords, actualPoList.size());
	}

	@Test
	public void test_mapFFMToEPS_Null_AIT() {

		int expectedEPSRecords = 0;
		BenefitEnrollmentMaintenanceDTO bemDTO = makeBemDTO();
		Map<DateTime, AdditionalInfoType> epsPremiums = new HashMap<DateTime, AdditionalInfoType>();
		AdditionalInfoType ait = null;
		epsPremiums.put(MAR_1, ait);
		bemDTO.setEpsPremiums(epsPremiums);
		List<PolicyPremiumPO> actualPoList = mapper.mapFFMToEPS(bemDTO);
		assertEquals("PolicyPremium list size", expectedEPSRecords, actualPoList.size());
	}

}
