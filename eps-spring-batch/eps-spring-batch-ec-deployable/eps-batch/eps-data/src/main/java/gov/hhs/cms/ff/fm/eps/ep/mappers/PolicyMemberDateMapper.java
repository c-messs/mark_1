/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.mappers;

import gov.cms.dsh.bem.MemberType;
import gov.hhs.cms.ff.fm.eps.ep.BEMDataUtil;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyMemberDatePO;

import org.joda.time.DateTime;

/**
 * @author j.radziewski
 *
 */
public class PolicyMemberDateMapper {


	/**
	 * Translate innbound Member dates to EPS.  For FFM relationship is 1 to 1,
	 * meaning 1 PolicyMemberVersion will have 1 set of member dates.
	 * @param member
	 * @param epsPO
	 * @return
	 */
	public PolicyMemberDatePO mapFFMToEPS(MemberType member, PolicyMemberDatePO epsPO) {

		PolicyMemberDatePO po = null;

		DateTime hcBBD = BEMDataUtil.getBenefitBeginDate(member);
		DateTime hcBED = BEMDataUtil.getBenefitEndDate(member);

		if (hcBBD != null || hcBED != null) {

			po = new PolicyMemberDatePO();
			po.setPolicyMemberStartDate(hcBBD);
			po.setPolicyMemberEndDate(hcBED);

			if (epsPO != null) {
				if (epsPO.equals(po)) {
					po.setPolicyMemberChanged(false);
				}
			}
		}
		return po;
	}
}
