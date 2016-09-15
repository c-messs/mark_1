package gov.hhs.cms.ff.fm.eps.ep.sbm.mappers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import gov.cms.dsh.sbmi.PolicyType;
import gov.hhs.cms.ff.fm.eps.ep.enums.PolicyStatus;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmPolicyStatusPO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMPolicyDTO;

public class SbmPolicyStatusMapperTest extends SBMBaseMapperTest {
	
	SbmPolicyStatusMapper mapper = new SbmPolicyStatusMapper();
	
	@Test
	public void test_mapSbmToEps_Policy_null() {
		
		List<SbmPolicyStatusPO> expectedList = new ArrayList<SbmPolicyStatusPO>();
		
		SBMPolicyDTO inboundPolicyDTO = new SBMPolicyDTO();
		List<SbmPolicyStatusPO> epsList = null;
		
		List<SbmPolicyStatusPO> actualList = mapper.mapSbmToEps(inboundPolicyDTO, epsList);
		assertEquals("SbmPolicyStatusPO list size", expectedList.size(), actualList.size());		
	}
	
	@Test
	public void test_mapSbmToEps_EPS_null_EFF() {
		
		LocalDateTime expectedTime = APR_1_4am;
		List<SbmPolicyStatusPO> expectedList = new ArrayList<SbmPolicyStatusPO>();
		expectedList.add(makeSbmPolicyStatusPO(MAR_1_3am, PolicyStatus.EFFECTUATED_2));
		SBMPolicyDTO inboundPolicyDTO = new SBMPolicyDTO();
		inboundPolicyDTO.setFileProcessDateTime(expectedTime);
		PolicyType policy = new PolicyType();
		policy.setEffectuationIndicator("Y");
		inboundPolicyDTO.setPolicy(policy);
		
		List<SbmPolicyStatusPO> epsList = null;
		
		List<SbmPolicyStatusPO> actualList = mapper.mapSbmToEps(inboundPolicyDTO, epsList);
		assertEquals("SbmPolicyStatusPO list size", expectedList.size(), actualList.size());
		SbmPolicyStatusPO actual = actualList.get(0);
		SbmPolicyStatusPO expected = expectedList.get(0);
		assertEquals("PolicyStatus", expected.getInsuranacePolicyStatusTypeCd(), actual.getInsuranacePolicyStatusTypeCd());
		assertEquals("PolicyStatus", expectedTime, actual.getTransDateTime());
	}
	
	
	@Test
	public void test_mapSbmToEps_EPS_null_CANC() {
		
		LocalDateTime expectedTime = APR_1_4am;
		List<SbmPolicyStatusPO> expectedList = new ArrayList<SbmPolicyStatusPO>();
		expectedList.add(makeSbmPolicyStatusPO(MAR_1_3am, PolicyStatus.CANCELLED_3));
		SBMPolicyDTO inboundPolicyDTO = new SBMPolicyDTO();
		inboundPolicyDTO.setFileProcessDateTime(expectedTime);
		PolicyType policy = new PolicyType();
		policy.setEffectuationIndicator("N");
		inboundPolicyDTO.setPolicy(policy);
		
		List<SbmPolicyStatusPO> epsList = null;
		
		List<SbmPolicyStatusPO> actualList = mapper.mapSbmToEps(inboundPolicyDTO, epsList);
		assertEquals("SbmPolicyStatusPO list size", expectedList.size(), actualList.size());
		SbmPolicyStatusPO actual = actualList.get(0);
		SbmPolicyStatusPO expected = expectedList.get(0);
		assertEquals("PolicyStatus", expected.getInsuranacePolicyStatusTypeCd(), actual.getInsuranacePolicyStatusTypeCd());
		assertEquals("PolicyStatus", expectedTime, actual.getTransDateTime());
	}

	
	@Test
	public void test_mapSbmToEps_No_Change() {
		
		LocalDateTime expectedTime = FEB_1_2am;
		List<SbmPolicyStatusPO> expectedList = new ArrayList<SbmPolicyStatusPO>();
		expectedList.add(makeSbmPolicyStatusPO(JAN_1_1am, PolicyStatus.EFFECTUATED_2));
		
		SBMPolicyDTO inboundPolicyDTO = new SBMPolicyDTO();
		inboundPolicyDTO.setFileProcessDateTime(expectedTime);
		PolicyType policy = new PolicyType();
		policy.setEffectuationIndicator("Y");
		inboundPolicyDTO.setPolicy(policy);
		
		List<SbmPolicyStatusPO> epsList = new ArrayList<SbmPolicyStatusPO>();
		// Since no change the expected TransDateTime will be what was in EPS.
		epsList.add(makeSbmPolicyStatusPO(expectedTime, PolicyStatus.EFFECTUATED_2));
		
		List<SbmPolicyStatusPO> actualList = mapper.mapSbmToEps(inboundPolicyDTO, epsList);
		assertEquals("SbmPolicyStatusPO list size", expectedList.size(), actualList.size());
		SbmPolicyStatusPO actual = actualList.get(0);
		SbmPolicyStatusPO expected = expectedList.get(0);
		assertEquals("PolicyStatus", expected.getInsuranacePolicyStatusTypeCd(), actual.getInsuranacePolicyStatusTypeCd());
		assertEquals("PolicyStatus", expectedTime, actual.getTransDateTime());
	}
	
	@Test
	public void test_mapSbmToEps_2_3_2() {
		
		LocalDateTime expectedTime = APR_1_4am;
		List<SbmPolicyStatusPO> expectedList = new ArrayList<SbmPolicyStatusPO>();
		
		// Add in reverse time order because select policy version status query is "ORDER BY TRANSDATETIME DESC"
		expectedList.add(makeSbmPolicyStatusPO(FEB_1_2am, PolicyStatus.CANCELLED_3));
		expectedList.add(makeSbmPolicyStatusPO(JAN_1_1am, PolicyStatus.EFFECTUATED_2));
		// But, since new status is added last in mapper, add it last for expected.
		expectedList.add(makeSbmPolicyStatusPO(expectedTime, PolicyStatus.EFFECTUATED_2));
		
		
		SBMPolicyDTO inboundPolicyDTO = new SBMPolicyDTO();
		inboundPolicyDTO.setFileProcessDateTime(expectedTime);
		PolicyType policy = new PolicyType();
		policy.setEffectuationIndicator("Y");
		inboundPolicyDTO.setPolicy(policy);
		
		List<SbmPolicyStatusPO> epsList = new ArrayList<SbmPolicyStatusPO>();
		// Since no change the expected TransDateTime will be what was in EPS.
		// Add in reverse time order because select policy version status query is "ORDER BY TRANSDATETIME DESC"
		epsList.add(makeSbmPolicyStatusPO(FEB_1_2am, PolicyStatus.CANCELLED_3));
		epsList.add(makeSbmPolicyStatusPO(JAN_1_1am, PolicyStatus.EFFECTUATED_2));
	
		List<SbmPolicyStatusPO> actualList = mapper.mapSbmToEps(inboundPolicyDTO, epsList);
		assertEquals("SbmPolicyStatusPO list size", expectedList.size(), actualList.size());
		SbmPolicyStatusPO actual2 = actualList.get(1);
		SbmPolicyStatusPO expected2 = expectedList.get(1);
		assertEquals("PolicyStatus 2", expected2.getInsuranacePolicyStatusTypeCd(), actual2.getInsuranacePolicyStatusTypeCd());
		assertEquals("PolicyStatus 2", expected2.getTransDateTime(), actual2.getTransDateTime());
		
		SbmPolicyStatusPO actual1 = actualList.get(1);
		SbmPolicyStatusPO expected1= expectedList.get(1);
		assertEquals("PolicyStatus 1", expected1.getInsuranacePolicyStatusTypeCd(), actual1.getInsuranacePolicyStatusTypeCd());
		assertEquals("PolicyStatus 1", expected1.getTransDateTime(), actual1.getTransDateTime());
		
		SbmPolicyStatusPO actual3 = actualList.get(2);
		SbmPolicyStatusPO expected3 = expectedList.get(2);
		assertEquals("PolicyStatus 3  (latest)", expected3.getInsuranacePolicyStatusTypeCd(), actual3.getInsuranacePolicyStatusTypeCd());
		assertEquals("PolicyStatus 3  (latest)", expectedTime , actual3.getTransDateTime());
	}

}
