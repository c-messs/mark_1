package gov.hhs.cms.ff.fm.eps.ep.sbm.mappers;

import java.util.ArrayList;
import java.util.List;

import gov.cms.dsh.sbmi.PolicyMemberType;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmPolicyMemberVersionPO;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;

/**
 * @author j.radziewski
 *
 */
public class SbmPolicyMemberVersionMapper {

	/**
	 * Extract and translate inbound SBM XPR member to EPS entity.  Determines and flags if
	 * inbound data is same as the corresponding EPS member.
	 * @param member
	 * @param epsPmvPO
	 * @return SbmPolicyMemberVersionPO
	 */
	public SbmPolicyMemberVersionPO mapSbmToStaging(PolicyMemberType member, SbmPolicyMemberVersionPO epsPmvPO) {

		SbmPolicyMemberVersionPO po = new SbmPolicyMemberVersionPO();

		// If id exists in epsPO and a new member version will be created (member changed),
		// then this id will be used to update MAINTENANCEENDDATETIME of the existing EPS member.
		if (epsPmvPO != null) {
			po.setPolicyMemberVersionId(epsPmvPO.getPolicyMemberVersionId());
		}

		// SubscriberStateCd set in SBMMemberDAO

		po.setExchangeMemberID(member.getExchangeAssignedMemberId());
		po.setSubscriberInd(member.getSubscriberIndicator());
		po.setIssuerAssignedMemberID(member.getIssuerAssignedMemberId());
		po.setPolicyMemberSalutationNm(member.getNamePrefix());
		po.setPolicyMemberLastNm(member.getMemberLastName());
		po.setPolicyMemberFirstNm(member.getMemberFirstName());
		po.setPolicyMemberMiddleNm(member.getMemberMiddleName());	
		po.setPolicyMemberSuffixNm(member.getNameSuffix());
		po.setPolicyMemberBirthDate(DateTimeUtil.getLocalDateFromXmlGC(member.getBirthDate()));
		po.setPolicyMemberSSN(member.getSocialSecurityNumber());
		po.setX12LanguageQualifierTypeCd(member.getLanguageQualifierCode());
		po.setX12LanguageCode(member.getLanguageCode());
		po.setX12GenderTypeCd(member.getGenderCode());
		po.setX12RaceEthnicityTypeCode(member.getRaceEthnicityCode());
		po.setX12TobaccoUseTypeCode(member.getTobaccoUseCode());
		po.setNonCoveredSubscriberInd(member.getNonCoveredSubscriberInd());
		po.setZipPlus4Cd(member.getPostalCode());
		if (epsPmvPO !=  null) {
			po.setPriorPolicyMemberVersionId(epsPmvPO.getPolicyMemberVersionId());
		}

		if (po.equals(epsPmvPO)) {
			po.setPolicyMemberChanged(false);
		} else {
			po.setPolicyMemberChanged(true);
		}
		return po;
	}

	/**
	 * 
	 * @param poList
	 * @return memberList
	 */
	public List<PolicyMemberType> mapEpsToSbm(List<SbmPolicyMemberVersionPO> poList) {

		List<PolicyMemberType> memberList = new ArrayList<PolicyMemberType>();

		for (SbmPolicyMemberVersionPO po : poList) {

			PolicyMemberType member = new PolicyMemberType();
			member.setExchangeAssignedMemberId(po.getExchangeMemberID());
			memberList.add(member);
		}
		return memberList;
	}

}
