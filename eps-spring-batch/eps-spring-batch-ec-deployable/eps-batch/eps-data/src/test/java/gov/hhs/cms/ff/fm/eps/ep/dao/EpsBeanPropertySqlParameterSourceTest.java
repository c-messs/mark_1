package gov.hhs.cms.ff.fm.eps.ep.dao;

import gov.hhs.cms.ff.fm.eps.ep.po.PolicyMemberVersionPO;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyVersionPO;
import gov.hhs.cms.ff.fm.eps.ep.vo.UserVO;
import junit.framework.TestCase;

import org.joda.time.DateTime;
import org.junit.Test;

public class EpsBeanPropertySqlParameterSourceTest extends TestCase {
	
	private final DateTime DATETIME = new DateTime();
	private final int YEAR = DATETIME.getYear();
	
	protected final DateTime APR_1 = new DateTime(YEAR, 4, 1, 0, 0);
	protected final DateTime MAY_1 = new DateTime(YEAR, 5, 1, 0, 0);
	
	
	@Test
	public void testConstructor() {
		
		PolicyVersionPO po = new PolicyVersionPO();
		EpsBeanPropertySqlParameterSource ebpsps = new EpsBeanPropertySqlParameterSource(po);
		assertNotNull("EpsBeanPropertySqlParameterSource", ebpsps);
		assertNull("default userVO", ebpsps.getValue("createBy"));
	}
	
	@Test
	public void testConstructor2() {
		
		String expected = "88888888";
		UserVO userVO = new UserVO(expected);
		PolicyMemberVersionPO po = new PolicyMemberVersionPO();
		EpsBeanPropertySqlParameterSource ebpsps = new EpsBeanPropertySqlParameterSource(po, userVO);
		assertNotNull("EpsBeanPropertySqlParameterSource", ebpsps);
		assertNotNull("default userVO", ebpsps.getValue("createBy"));
		assertEquals("createBy user", expected, ebpsps.getValue("createBy"));
		assertEquals("lastModifiedBy user", expected, ebpsps.getValue("lastModifiedBy"));
	}
	
	@Test
	public void testConstructor2_NullUserVO() {
		
		UserVO userVO = null;
		PolicyMemberVersionPO po = new PolicyMemberVersionPO();
		EpsBeanPropertySqlParameterSource ebpsps = new EpsBeanPropertySqlParameterSource(po, userVO);
		assertNotNull("EpsBeanPropertySqlParameterSource", ebpsps);
		assertNull("createBy user", ebpsps.getValue("createBy"));
		assertNull("lastModifiedBy user", ebpsps.getValue("lastModifiedBy"));
	}
	
	
	@Test
	public void testGetValue() {
		
		DateTime expectedPolicyMemberEligStartDate = APR_1;
		String expected = "7777777";
		Long expectedTransMsgId = Long.valueOf(666666);
		UserVO userVO = new UserVO(expected);
		PolicyMemberVersionPO po = new PolicyMemberVersionPO();
		po.setPolicyMemberEligStartDate(expectedPolicyMemberEligStartDate);
		po.setTransMsgID(expectedTransMsgId);
		EpsBeanPropertySqlParameterSource actual = new EpsBeanPropertySqlParameterSource(po, userVO);
		assertNotNull("EpsBeanPropertySqlParameterSource", actual);
		assertNotNull("default userVO", actual.getValue("createBy"));
		assertEquals("createBy user", expected, actual.getValue("createBy"));
		assertEquals("lastModifiedBy user", expected, actual.getValue("lastModifiedBy"));
		assertEquals("BenefitBeginDate", expectedPolicyMemberEligStartDate.toDate().toString(), actual.getValue("PolicyMemberEligStartDate").toString());
		assertNull("exchangeMemberID", actual.getValue("exchangeMemberID"));
		assertNotNull("transMsgID", actual.getValue("transMsgID"));
		assertEquals("transgMsgId", expectedTransMsgId, actual.getValue("transMsgID"));
	}
	
	


}
