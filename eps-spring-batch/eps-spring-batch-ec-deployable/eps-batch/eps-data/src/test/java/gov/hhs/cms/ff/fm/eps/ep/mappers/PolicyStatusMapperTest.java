package gov.hhs.cms.ff.fm.eps.ep.mappers;

import gov.cms.dsh.bem.BenefitEnrollmentMaintenanceType;
import gov.cms.dsh.bem.PolicyInfoType;
import gov.cms.dsh.bem.TransactionInformationType;
import gov.hhs.cms.ff.fm.eps.ep.enums.PolicyStatus;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyStatusPO;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;

import java.util.ArrayList;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.Test;

public class PolicyStatusMapperTest extends BaseMapperTest {

	private PolicyStatusMapper mapper = new PolicyStatusMapper();


	@Test
	public void test_mapFFMToEPS_INITIAL() {

		String expectedPolicyStatus = PolicyStatus.INITIAL_1.getValue();
		XMLGregorianCalendar curTimeStamp = DateTimeUtil.getXMLGregorianCalendar(DATETIME);

		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setTransactionInformation(new TransactionInformationType());
		bem.getTransactionInformation().setCurrentTimeStamp(curTimeStamp);
		bem.setPolicyInfo(new PolicyInfoType());
		bem.getPolicyInfo().setPolicyStatus(expectedPolicyStatus);

		// Initials will not have any data in EPS, therefore empty
		List<PolicyStatusPO> epsList = new ArrayList<PolicyStatusPO>();

		List<PolicyStatusPO> poList = mapper.mapFFMToEPS(bem, epsList);

		assertNotNull("PolicyStatusPO", poList);
		assertEquals("PolicyStatusPO list size",  1, poList.size());

		PolicyStatusPO po = poList.get(0);

		assertEquals("insuranacePolicyStatusTypeCd", expectedPolicyStatus, po.getInsuranacePolicyStatusTypeCd());
		assertEquals("TransDateTime", DateTimeUtil.getLocalDateTimeFromXmlGC(curTimeStamp), po.getTransDateTime());
	}


	@Test
	public void test_mapFFMToEPS_EFFECTUATION() {

		String expectedPolicyStatus = PolicyStatus.EFFECTUATED_2.getValue();
		XMLGregorianCalendar curTimeStamp = DateTimeUtil.getXMLGregorianCalendar(DATETIME);

		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setTransactionInformation(new TransactionInformationType());
		bem.getTransactionInformation().setCurrentTimeStamp(DateTimeUtil.getXMLGregorianCalendar(DATETIME));
		bem.setPolicyInfo(new PolicyInfoType());
		bem.getPolicyInfo().setPolicyStatus(expectedPolicyStatus);

		// EFFECTUATIONs will have data in EPS
		List<PolicyStatusPO> epsList = new ArrayList<PolicyStatusPO>();
		PolicyStatusPO epsPO1 = new PolicyStatusPO();
		epsPO1.setInsuranacePolicyStatusTypeCd(PolicyStatus.INITIAL_1.getValue());
		epsPO1.setTransDateTime(DATETIME.minusMonths(1));
		epsList.add(epsPO1);

		List<PolicyStatusPO> poList = mapper.mapFFMToEPS(bem, epsList);

		assertNotNull("PolicyStatusPO", poList);
		assertEquals("PolicyStatusPO list size",  2, poList.size());

		PolicyStatusPO po1 = poList.get(0);

		assertEquals("insuranacePolicyStatusTypeCd", epsPO1.getInsuranacePolicyStatusTypeCd(), po1.getInsuranacePolicyStatusTypeCd());
		assertEquals("TransDateTime", epsPO1.getTransDateTime(), po1.getTransDateTime());

		PolicyStatusPO po2 = poList.get(1);

		assertEquals("insuranacePolicyStatusTypeCd", expectedPolicyStatus, po2.getInsuranacePolicyStatusTypeCd());
		assertEquals("TransDateTime", DateTimeUtil.getLocalDateTimeFromXmlGC(curTimeStamp), po2.getTransDateTime());
	}


	@Test
	public void test_mapFFMToEPS_CANCEL() {

		String expectedPolicyStatus = PolicyStatus.CANCELLED_3.getValue();
		XMLGregorianCalendar curTimeStamp = DateTimeUtil.getXMLGregorianCalendar(DATETIME);
		
		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setTransactionInformation(new TransactionInformationType());
		bem.getTransactionInformation().setCurrentTimeStamp(DateTimeUtil.getXMLGregorianCalendar(DATETIME));
		bem.setPolicyInfo(new PolicyInfoType());
		bem.getPolicyInfo().setPolicyStatus(expectedPolicyStatus);

		// EFFECTUATIONs will have data in EPS
		List<PolicyStatusPO> epsList = new ArrayList<PolicyStatusPO>();
		PolicyStatusPO epsPO1 = new PolicyStatusPO();
		epsPO1.setInsuranacePolicyStatusTypeCd(PolicyStatus.INITIAL_1.getValue());
		epsPO1.setTransDateTime(DATETIME.minusMonths(2));
		epsList.add(epsPO1);

		PolicyStatusPO epsPO2 = new PolicyStatusPO();
		epsPO2.setInsuranacePolicyStatusTypeCd(PolicyStatus.EFFECTUATED_2.getValue());
		epsPO2.setTransDateTime(DATETIME.minusMonths(1));
		epsList.add(epsPO2);


		List<PolicyStatusPO> poList = mapper.mapFFMToEPS(bem, epsList);

		assertNotNull("PolicyStatusPO", poList);
		assertEquals("PolicyStatusPO list size",  3, poList.size());

		PolicyStatusPO po1 = poList.get(0);

		assertEquals("INITIAL insuranacePolicyStatusTypeCd", epsPO1.getInsuranacePolicyStatusTypeCd(), po1.getInsuranacePolicyStatusTypeCd());
		assertEquals("INITIAL TransDateTime", epsPO1.getTransDateTime(), po1.getTransDateTime());

		PolicyStatusPO po2 = poList.get(1);

		assertEquals("EFFECTUATION insuranacePolicyStatusTypeCd", epsPO2.getInsuranacePolicyStatusTypeCd(), po2.getInsuranacePolicyStatusTypeCd());
		assertEquals("EFFECTUATION TransDateTime", epsPO2.getTransDateTime(), po2.getTransDateTime());

		PolicyStatusPO po3 = poList.get(2);

		assertEquals("CANCEL insuranacePolicyStatusTypeCd", expectedPolicyStatus, po3.getInsuranacePolicyStatusTypeCd());
		assertEquals("CANCEL TransDateTime", DateTimeUtil.getLocalDateTimeFromXmlGC(curTimeStamp), po3.getTransDateTime());
	}

	
	@Test
	public void test_mapFFMToEPS_SUSPERSEDED() {

		String expectedPolicyStatus = PolicyStatus.SUPERSEDED_5.getValue();
		XMLGregorianCalendar curTimeStamp = DateTimeUtil.getXMLGregorianCalendar(DATETIME);

		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setTransactionInformation(new TransactionInformationType());
		bem.getTransactionInformation().setCurrentTimeStamp(DateTimeUtil.getXMLGregorianCalendar(DATETIME));
		bem.setPolicyInfo(new PolicyInfoType());
		bem.getPolicyInfo().setPolicyStatus(expectedPolicyStatus);


		// data in EPS
		List<PolicyStatusPO> epsList = new ArrayList<PolicyStatusPO>();
		PolicyStatusPO epsPO1 = new PolicyStatusPO();
		epsPO1.setInsuranacePolicyStatusTypeCd(PolicyStatus.INITIAL_1.getValue());
		epsPO1.setTransDateTime(DATETIME.minusMonths(3));
		epsList.add(epsPO1);

		PolicyStatusPO epsPO2 = new PolicyStatusPO();
		epsPO2.setInsuranacePolicyStatusTypeCd(PolicyStatus.EFFECTUATED_2.getValue());
		epsPO2.setTransDateTime(DATETIME.minusMonths(2));
		epsList.add(epsPO2);
		
		PolicyStatusPO epsPO3 = new PolicyStatusPO();
		epsPO3.setInsuranacePolicyStatusTypeCd(PolicyStatus.CANCELLED_3.getValue());
		epsPO3.setTransDateTime(DATETIME.minusMonths(1));
		epsList.add(epsPO3);


		List<PolicyStatusPO> poList = mapper.mapFFMToEPS(bem, epsList);

		assertNotNull("PolicyStatusPO", poList);
		assertEquals("PolicyStatusPO list size",  4, poList.size());

		PolicyStatusPO po1 = poList.get(0);

		assertEquals("INITIAL insuranacePolicyStatusTypeCd", epsPO1.getInsuranacePolicyStatusTypeCd(), po1.getInsuranacePolicyStatusTypeCd());
		assertEquals("INITIAL TransDateTime", epsPO1.getTransDateTime(), po1.getTransDateTime());

		PolicyStatusPO po2 = poList.get(1);

		assertEquals("EFFECTUATION insuranacePolicyStatusTypeCd", epsPO2.getInsuranacePolicyStatusTypeCd(), po2.getInsuranacePolicyStatusTypeCd());
		assertEquals("EFFECTUATION TransDateTime", epsPO2.getTransDateTime(), po2.getTransDateTime());

		PolicyStatusPO po3 = poList.get(2);

		assertEquals("CANCEL insuranacePolicyStatusTypeCd", epsPO3.getInsuranacePolicyStatusTypeCd(), po3.getInsuranacePolicyStatusTypeCd());
		assertEquals("CANCEL TransDateTime", epsPO3.getTransDateTime(), po3.getTransDateTime());
		
		PolicyStatusPO po4 = poList.get(3);

		assertEquals("SUPERSEDED insuranacePolicyStatusTypeCd", expectedPolicyStatus, po4.getInsuranacePolicyStatusTypeCd());
		assertEquals("SUPERSEDED TransDateTime", DateTimeUtil.getLocalDateTimeFromXmlGC(curTimeStamp), po4.getTransDateTime());
	}
	
	@Test
	public void test_mapFFMToEPS_SUSPERSEDED_To_EFFECTUATED() {

		String expectedPolicyStatus = PolicyStatus.EFFECTUATED_2.getValue();
		XMLGregorianCalendar curTimeStamp = DateTimeUtil.getXMLGregorianCalendar(DATETIME);

		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setTransactionInformation(new TransactionInformationType());
		bem.getTransactionInformation().setCurrentTimeStamp(DateTimeUtil.getXMLGregorianCalendar(DATETIME));
		bem.setPolicyInfo(new PolicyInfoType());
		bem.getPolicyInfo().setPolicyStatus(expectedPolicyStatus);


		// data in EPS
		List<PolicyStatusPO> epsList = new ArrayList<PolicyStatusPO>();
		PolicyStatusPO epsPO1 = new PolicyStatusPO();
		epsPO1.setInsuranacePolicyStatusTypeCd(PolicyStatus.INITIAL_1.getValue());
		epsPO1.setTransDateTime(DATETIME.minusMonths(3));
		epsList.add(epsPO1);

		PolicyStatusPO epsPO2 = new PolicyStatusPO();
		epsPO2.setInsuranacePolicyStatusTypeCd(PolicyStatus.EFFECTUATED_2.getValue());
		epsPO2.setTransDateTime(DATETIME.minusMonths(2));
		epsList.add(epsPO2);
		
		PolicyStatusPO epsPO3 = new PolicyStatusPO();
		epsPO3.setInsuranacePolicyStatusTypeCd(PolicyStatus.CANCELLED_3.getValue());
		epsPO3.setTransDateTime(DATETIME.minusMonths(1));
		epsList.add(epsPO3);


		List<PolicyStatusPO> poList = mapper.mapFFMToEPS(bem, epsList);

		assertNotNull("PolicyStatusPO", poList);
		assertEquals("PolicyStatusPO list size",  4, poList.size());

		PolicyStatusPO po1 = poList.get(0);

		assertEquals("INITIAL insuranacePolicyStatusTypeCd", epsPO1.getInsuranacePolicyStatusTypeCd(), po1.getInsuranacePolicyStatusTypeCd());
		assertEquals("INITIAL TransDateTime", epsPO1.getTransDateTime(), po1.getTransDateTime());

		PolicyStatusPO po2 = poList.get(1);

		assertEquals("EFFECTUATION insuranacePolicyStatusTypeCd", epsPO2.getInsuranacePolicyStatusTypeCd(), po2.getInsuranacePolicyStatusTypeCd());
		assertEquals("EFFECTUATION TransDateTime", epsPO2.getTransDateTime(), po2.getTransDateTime());

		PolicyStatusPO po3 = poList.get(2);

		assertEquals("CANCEL insuranacePolicyStatusTypeCd", epsPO3.getInsuranacePolicyStatusTypeCd(), po3.getInsuranacePolicyStatusTypeCd());
		assertEquals("CANCEL TransDateTime", epsPO3.getTransDateTime(), po3.getTransDateTime());
		
		PolicyStatusPO po4 = poList.get(3);

		assertEquals("SUPERSEDED insuranacePolicyStatusTypeCd", expectedPolicyStatus, po4.getInsuranacePolicyStatusTypeCd());
		assertEquals("SUPERSEDED TransDateTime", DateTimeUtil.getLocalDateTimeFromXmlGC(curTimeStamp), po4.getTransDateTime());
	}
	
	
	@Test
	public void test_mapFFMToEPS_EFFECTUATION_NoChange() {

		String expectedPolicyStatus = PolicyStatus.EFFECTUATED_2.getValue();

		BenefitEnrollmentMaintenanceType bem = new BenefitEnrollmentMaintenanceType();
		bem.setTransactionInformation(new TransactionInformationType());
		bem.getTransactionInformation().setCurrentTimeStamp(DateTimeUtil.getXMLGregorianCalendar(DATETIME));
		bem.setPolicyInfo(new PolicyInfoType());
		bem.getPolicyInfo().setPolicyStatus(expectedPolicyStatus);

		// EFFECTUATIONs will have data in EPS
		// Add PO with same status and PolicyStartDate

		List<PolicyStatusPO> epsList = new ArrayList<PolicyStatusPO>();
		PolicyStatusPO epsPO1 = new PolicyStatusPO();
		epsPO1.setInsuranacePolicyStatusTypeCd(PolicyStatus.INITIAL_1.getValue());
		epsPO1.setTransDateTime(DATETIME.minusMonths(3));
		
		PolicyStatusPO epsPO2 = new PolicyStatusPO();
		epsPO2.setInsuranacePolicyStatusTypeCd(PolicyStatus.EFFECTUATED_2.getValue());
		epsPO2.setTransDateTime(DATETIME.minusMonths(2));
		
		epsList.add(epsPO2);
		epsList.add(epsPO1);
		
		List<PolicyStatusPO> poList = mapper.mapFFMToEPS(bem, epsList);

		assertNotNull("PolicyStatusPO", poList);
		assertEquals("PolicyStatusPO list size",  2, poList.size());

		// Data should be the same as EPS data.
		PolicyStatusPO po1 = poList.get(1);

		assertEquals("insuranacePolicyStatusTypeCd", epsPO1.getInsuranacePolicyStatusTypeCd(), po1.getInsuranacePolicyStatusTypeCd());
		assertEquals("TransDateTime", epsPO1.getTransDateTime(), po1.getTransDateTime());

		PolicyStatusPO po2 = poList.get(0);

		assertEquals("insuranacePolicyStatusTypeCd", expectedPolicyStatus, po2.getInsuranacePolicyStatusTypeCd());
		assertEquals("TransDateTime", epsPO2.getTransDateTime(), po2.getTransDateTime());
	}

	
	@Test
	public void test_mapFFMToEPS_NullBEM() {
		
		BenefitEnrollmentMaintenanceType bem = null;
		// Initials will not have any data in EPS, therefore empty
		List<PolicyStatusPO> epsList = new ArrayList<PolicyStatusPO>();
		List<PolicyStatusPO> poList = mapper.mapFFMToEPS(bem, epsList);
		assertNotNull("PolicyStatusPO", poList);
		assertEquals("PolicyStatusPO list size",  0, poList.size());
	}
	
	@Test
	public void test_mapFFMToEPS_NullPolicyStatus() {

		BenefitEnrollmentMaintenanceType bem =  new BenefitEnrollmentMaintenanceType();
		// Initials will not have any data in EPS, therefore empty
		List<PolicyStatusPO> epsList = new ArrayList<PolicyStatusPO>();
		List<PolicyStatusPO> poList = mapper.mapFFMToEPS(bem, epsList);
		assertNotNull("PolicyStatusPO", poList);
		assertEquals("PolicyStatusPO list size",  0, poList.size());
	}


}

