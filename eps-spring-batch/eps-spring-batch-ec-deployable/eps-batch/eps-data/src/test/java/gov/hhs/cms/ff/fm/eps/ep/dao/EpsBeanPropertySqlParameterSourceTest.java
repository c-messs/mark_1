package gov.hhs.cms.ff.fm.eps.ep.dao;

import gov.hhs.cms.ff.fm.eps.ep.po.PolicyMemberVersionPO;
import gov.hhs.cms.ff.fm.eps.ep.po.PolicyVersionPO;
import gov.hhs.cms.ff.fm.eps.ep.util.DateTimeUtil;
import gov.hhs.cms.ff.fm.eps.ep.vo.UserVO;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;

import junit.framework.TestCase;

import org.junit.Test;

public class EpsBeanPropertySqlParameterSourceTest extends TestCase {
	
	protected final LocalDate DATE = LocalDate.now();
	protected final LocalDateTime DATETIME = LocalDateTime.now();
	protected final int YEAR = DATE.getYear();
	
	protected final LocalDate APR_1 = LocalDate.of(YEAR, 4, 1);
	protected final LocalDate MAY_1 = LocalDate.of(YEAR, 5, 1);
	
	protected final LocalDateTime APR_1_4am = LocalDateTime.of(YEAR, 4, 1, 4, 0, 0, 444444000);
	
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
		
		LocalDate expectedPolicyMemberEligStartDate = APR_1;
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
		assertEquals("BenefitBeginDate", expectedPolicyMemberEligStartDate,
				DateTimeUtil.getLocalDateFromSqlDate((Date) actual.getValue("PolicyMemberEligStartDate")));
		assertNull("exchangeMemberID", actual.getValue("exchangeMemberID"));
		assertNotNull("transMsgID", actual.getValue("transMsgID"));
		assertEquals("transgMsgId", expectedTransMsgId, actual.getValue("transMsgID"));
	}
	
	


}
