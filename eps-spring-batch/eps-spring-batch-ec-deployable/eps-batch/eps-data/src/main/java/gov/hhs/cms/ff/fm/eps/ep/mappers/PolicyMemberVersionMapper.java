package gov.hhs.cms.ff.fm.eps.ep.mappers;

import gov.cms.dsh.bem.BooleanIndicatorSimpleType;
import gov.cms.dsh.bem.IndividualNameType;
import gov.cms.dsh.bem.MemberAdditionalIdentifierType;
import gov.cms.dsh.bem.MemberDemographicsType;
import gov.cms.dsh.bem.MemberNameInfoType;
import gov.cms.dsh.bem.MemberRelatedInfoType;
import gov.cms.dsh.bem.MemberType;
import gov.hhs.cms.ff.fm.eps.ep.BEMDataUtil;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyMemberVersionPO;
import gov.hhs.cms.ff.fm.eps.ep.util.EpsDateUtils;

/**
 * @author EPS
 *
 */
public class PolicyMemberVersionMapper {


	/**
	 * Extract and translate inbound member to EPS entity.  Determines and flags if
	 * inbound data is same as the corresponding EPS member.
	 * @param member
	 * @param epsPO
	 * @return
	 */
	public PolicyMemberVersionPO mapFFMToEPS(MemberType member, PolicyMemberVersionPO epsPO) {

		PolicyMemberVersionPO po = new PolicyMemberVersionPO();

		// If exists in epsPO and a new member version will be created (member changed),
		// then it will be used to update MAINTENANCESTARTDATETIME of the existing EPS member.
		if (epsPO != null) {
			po.setPolicyMemberVersionId(epsPO.getPolicyMemberVersionId());
		}

		// SubscriberStateCd set in FFMMemberDAO

		mapMemberAdditionalIdentifierType(member.getMemberAdditionalIdentifier(), po);
		mapMemberInformation(member.getMemberInformation(), po);
		mapMemberNameInformationType(member, po);

		po.setPolicyMemberEligStartDate(BEMDataUtil.getEligibilityBeginDate(member));
		po.setPolicyMemberEligEndDate(BEMDataUtil.getEligibilityEndDate(member));

		// MaintenanceStartDateTime set in FFMMemberDAO from PolicyVersion.
		po.setMaintenanceEndDateTime(EpsDateUtils.HIGHDATE);

		if (po.equals(epsPO)) {
			po.setPolicyMemberChanged(false);
		} else {
			po.setPolicyMemberChanged(true);
		}

		return po;

	}


	private void mapMemberAdditionalIdentifierType(MemberAdditionalIdentifierType addIdType, PolicyMemberVersionPO polMemVer) {

		if (addIdType != null) {
			polMemVer.setIssuerAssignedMemberID(addIdType.getIssuerAssignedMemberID());
			polMemVer.setExchangeMemberID(addIdType.getExchangeAssignedMemberID());
		}
	}

	private void mapMemberInformation(MemberRelatedInfoType memInfo, PolicyMemberVersionPO polMemVer) {

		if (memInfo != null) {
			BooleanIndicatorSimpleType subscriberIndicator = memInfo.getSubscriberIndicator();
			if (subscriberIndicator != null) {
				polMemVer.setSubscriberInd(subscriberIndicator.value());
			}
		}
	}

	private void mapMemberNameInformationType(MemberType member, PolicyMemberVersionPO polMemVer) {

		MemberNameInfoType nameInfo = member.getMemberNameInformation();

		if (nameInfo != null) {

			IndividualNameType memName = nameInfo.getMemberName();
			if (memName != null) {

				polMemVer.setPolicyMemberLastNm(memName.getLastName());
				polMemVer.setPolicyMemberFirstNm(memName.getFirstName());
				polMemVer.setPolicyMemberMiddleNm(memName.getMiddleName());
				polMemVer.setPolicyMemberSalutationNm(memName.getNamePrefix());			
				polMemVer.setPolicyMemberSuffixNm(memName.getNameSuffix());
				polMemVer.setPolicyMemberSSN(memName.getSocialSecurityNumber());
			}

			MemberDemographicsType demoType = nameInfo.getMemberDemographics();

			if (demoType != null) {

				if (demoType.getGenderCode() != null) {
					polMemVer.setX12GenderTypeCd(demoType.getGenderCode().name());
				}
				if (demoType.getBirthDate() != null) {
					polMemVer.setPolicyMemberBirthDate(EpsDateUtils.getDateTimeFromXmlGC(demoType.getBirthDate()));
				}
			}
		}
	}


}
