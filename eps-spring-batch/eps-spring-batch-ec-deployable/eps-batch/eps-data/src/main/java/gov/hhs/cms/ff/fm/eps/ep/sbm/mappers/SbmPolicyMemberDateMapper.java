package gov.hhs.cms.ff.fm.eps.ep.sbm.mappers;

import java.util.ArrayList;
import java.util.List;

import gov.cms.dsh.sbmi.PolicyMemberType;
import gov.cms.dsh.sbmi.PolicyMemberType.MemberDates;
import gov.hhs.cms.ff.fm.eps.ep.po.SbmPolicyMemberDatePO;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;

/**
 * @author j.radziewski
 *
 */
public class SbmPolicyMemberDateMapper {


	/**
	 * Translate inbound SBM XPR Member dates to EPS.  For SBM, relationship is 1 to many,
	 * meaning 1 PolicyMemberVersion could have more than 1 set of member dates.  Determines if
	 * inbound MemberDates are different than EPS.
	 * @param member
	 * @param epsPO
	 * @return
	 */
	public List<SbmPolicyMemberDatePO> mapSbmToStaging(PolicyMemberType member, List<SbmPolicyMemberDatePO> epsPOList) {

		List<SbmPolicyMemberDatePO> poList = new ArrayList<SbmPolicyMemberDatePO>();
		SbmPolicyMemberDatePO po = null;

		for (MemberDates memDates : member.getMemberDates()) {

			po = new SbmPolicyMemberDatePO();
			
			po.setPolicyMemberStartDate(DateTimeUtil.getLocalDateFromXmlGC(memDates.getMemberStartDate()));
			po.setPolicyMemberEndDate(DateTimeUtil.getLocalDateFromXmlGC(memDates.getMemberEndDate()));
			
			if (epsPOList != null) {
				if (epsPOList.contains(po)) {
					po.setPolicyMemberChanged(false);
				}
			}
			poList.add(po);
		}
		return poList;
	}


}
