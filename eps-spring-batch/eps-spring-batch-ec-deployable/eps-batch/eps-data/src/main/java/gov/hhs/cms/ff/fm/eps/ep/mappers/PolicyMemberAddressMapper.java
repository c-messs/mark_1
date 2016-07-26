package gov.hhs.cms.ff.fm.eps.ep.mappers;

import gov.cms.dsh.bem.MemberNameInfoType;
import gov.cms.dsh.bem.MemberType;
import gov.cms.dsh.bem.ResidentialAddressType;
import gov.hhs.cms.ff.fm.eps.ep.enums.AddressTypeEnum;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyMemberAddressPO;


/**
 * @author EPS
 * 
 * Maps/merges inbound and outbound Mailing and Residency addresses for a member (multi-loop). 
 *
 */
public class PolicyMemberAddressMapper {

	
	/**
	 *  Maps 834 inbound Residence addresses for a member. Overwrites EPS even if inbound null.
	 * @param member
	 * @param epsPO
	 * @return
	 */
	public PolicyMemberAddressPO mapFFMToEPS(MemberType member, PolicyMemberAddressPO epsPO) {

		PolicyMemberAddressPO po = null;

		MemberNameInfoType memberNameInfoType = member.getMemberNameInformation();

		if(member.getMemberNameInformation() != null) {

			ResidentialAddressType resAddr = memberNameInfoType.getMemberResidenceAddress();

			if (resAddr != null) {

				po = new PolicyMemberAddressPO();
				po.setX12addressTypeCd(AddressTypeEnum.RESIDENTIAL.getValue());

				po.setZipPlus4Cd(resAddr.getPostalCode());
				po.setStateCd(resAddr.getStateCode());

				if (epsPO != null) {
					if (epsPO.equals(po)) {
						po.setPolicyMemberChanged(false);
					}
				}
			}
		}

		return po;
	}
}
