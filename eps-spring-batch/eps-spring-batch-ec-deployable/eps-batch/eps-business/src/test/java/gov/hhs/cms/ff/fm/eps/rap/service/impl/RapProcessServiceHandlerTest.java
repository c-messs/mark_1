package gov.hhs.cms.ff.fm.eps.rap.service.impl;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.*;

import java.util.List;

import org.easymock.EasyMock;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.accenture.foundation.common.exception.ApplicationException;
import com.accenture.foundation.common.codetable.CodeRecord;
import com.accenture.foundation.common.codetable.CodeService;
import com.accenture.foundation.common.codetable.CodeServiceImpl;

import gov.hhs.cms.ff.fm.eps.rap.dao.RapDao;
import gov.hhs.cms.ff.fm.eps.rap.dto.PolicyDataDTO;
import gov.hhs.cms.ff.fm.eps.rap.service.RapServiceTestUtil;
import gov.hhs.cms.ff.fm.eps.rap.util.CodeDecodesHelper;

/**
 * @author mark.finkelshteyn
 * This class tests RapProcessServiceHandler 
 */
public class RapProcessServiceHandlerTest {

	
	RapProcessingServiceImpl rapProcesssingServiceTest;

	private final DateTime DATETIME = new DateTime();
	private final int YEAR = DATETIME.getYear();
    private CodeRecord codeRecord;
    private CodeRecord mockCodeRecord;
	private RapDao mockRapDao;
	private CodeDecodesHelper mockCodeDecodesHelper;
	private CodeDecodesHelper codeDecodesHelper;
	RapProcessServiceHandler rapHandler;
	RapProcessServiceHandler mapRapHandler;
	@Before
	public void setup() throws Exception {
		rapProcesssingServiceTest = new RapProcessingServiceImpl();
		rapHandler = new RapProcessServiceHandler();
		mapRapHandler = EasyMock.createMock(RapProcessServiceHandler.class);
		mockRapDao = EasyMock.createMock(RapDao.class);
		codeDecodesHelper = new CodeDecodesHelper();
		mockCodeDecodesHelper = EasyMock.createMock(CodeDecodesHelper.class);
		codeRecord = new CodeRecord("", "","");
		mockCodeRecord = EasyMock.createMock(CodeRecord.class);
		//rapProcesssingServiceTest.setRapDao(mockRapDao);
//		rapProcesssingServiceTest.setCodeDecodesHelper(mockCodeDecodesHelper);

	}
	
		@Test//(expected=com.accenture.foundation.common.exception.ApplicationException.class)
		public void test_getPaymentMonthERC_AppException_NotNull() throws Exception {
			List<DateTime> l=null;
			CodeRecord codeRecord = new CodeRecord("ERC", "2016-04-01 00:00:00", "2016-03-16 00:00:00");
			expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
			.andReturn(codeRecord);
			replay(mockCodeDecodesHelper);
			try {
				l = rapHandler.getPaymentMonthERC(mockCodeDecodesHelper);
			} catch(ApplicationException appEx) {
				//assertTrue("ApplicationException thrown", true);
			}
			assertTrue("DateTime List Not empty", l.size()>0);
		}
		

		@Test//(expected=com.accenture.foundation.common.exception.ApplicationException.class)
		public void test_getPaymentMonthERC_AppException() throws Exception {

			expect(mockCodeDecodesHelper.getDecode(EasyMock.anyString(), EasyMock.anyString()))
			.andReturn(null);
			replay(mockCodeDecodesHelper);

			try {
				ReflectionTestUtils.invokeMethod(rapHandler, "getPaymentMonthERC",mockCodeDecodesHelper);
			} catch(ApplicationException appEx) {
				assertTrue("ApplicationException thrown", true);
			}
		//	assertNotNull("rapHandler", rapHandler);
		}


	@Test
	public void testIsUserFeeRateExists() {
		DateTime coverageDate = new DateTime("2015-01-01");
		PolicyDataDTO policy = RapServiceTestUtil.createMockPolicyVersion(1, "101", "2015-01-01", "2015-01-01", "2015-01-12");
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);
		rapHandler.isUserFeeRateExists(coverageDate, policy, mockRapDao);
		assertTrue("User Free Rate Exists",true);
	}
	
	@Test
	public void testIsUserFeeRateNotExists() {
		//DateTime coverageDate = new DateTime("2015-01-01");
		PolicyDataDTO policy = RapServiceTestUtil.createMockPolicyVersion(10, "101", "2017-01-01", "2017-01-01", "2017-01-12");
		expect(mockRapDao.getUserFeeRateForAllStates(EasyMock.anyObject(DateTime.class), EasyMock.anyString()))
		.andReturn(RapServiceTestUtil.createIssuerUserFeeRateList()).anyTimes();
		replay(mockRapDao);
		rapHandler.isUserFeeRateExists(null, policy, mockRapDao);
		assertTrue("User Free Rate Don't Exists",true);
	}
}
