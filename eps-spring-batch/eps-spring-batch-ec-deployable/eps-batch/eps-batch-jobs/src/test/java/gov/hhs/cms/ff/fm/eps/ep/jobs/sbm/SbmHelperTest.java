package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.hhs.cms.ff.fm.eps.ep.enums.SBMErrorWarningCode;
import gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMErrorDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileInfo;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMSummaryAndFileInfoDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMUpdateStatusErrorDTO;
import junit.framework.TestCase;

@RunWith(JUnit4.class)
public class SbmHelperTest extends TestCase {
	
	private static final Logger LOG = LoggerFactory.getLogger(SbmHelperTest.class);
	
	@Test
	public void testGetFunctionCode() {
		String filename = "CA1.EPS.SBMI.D160707.T090101001.T";
		
		String fuctionCode = SbmHelper.getFunctionCodeFromFile(filename);
		LOG.info("fuctionCode: {}", fuctionCode);
		
		Assert.assertTrue("fuctionCode should not be blank", StringUtils.isNotBlank(fuctionCode));
		Assert.assertEquals("fuctionCode", "CA1", fuctionCode);
	}
	
	@Test
	public void testGetFunctionCodeFromPreEFTFormat() {
		String filename = "CA1.EPS.SBMI.D160707.T090101001.T";
		
		String fuctionCode = SbmHelper.getFunctionCodeFromPreEFTFormat(filename);
		LOG.info("fuctionCode: {}", fuctionCode);
		
		Assert.assertTrue("fuctionCode should not be blank", StringUtils.isNotBlank(fuctionCode));
		Assert.assertEquals("fuctionCode", "SBMI", fuctionCode);
	}
	
	@Test
	public void testTradingPartnerId() {
		String filename = "CA1.SBMI.D160707.T090101001.T";
		
		String tradingPartnerId = SbmHelper.getTradingPartnerId(filename);
		LOG.info("fuctionCode: {}", tradingPartnerId);
		
		Assert.assertTrue("TradingPartnerId should not be blank", StringUtils.isNotBlank(tradingPartnerId));
		Assert.assertEquals("fuctionCode", "SBMI", tradingPartnerId);
	}
	
	@Test
	public void testTradingPartnerIdFileInfoList() {
		SBMFileInfo fileInfo = new SBMFileInfo();
		fileInfo.setTradingPartnerId("TPId");
		List<SBMFileInfo> sbmFileInfoList = Arrays.asList(fileInfo);
		
		String tradingPartnerId = SbmHelper.getTradingPartnerId(sbmFileInfoList);
		LOG.info("tradingPartnerId: {}", tradingPartnerId);
		
		Assert.assertTrue("TradingPartnerId should not be blank", StringUtils.isNotBlank(tradingPartnerId));
	}
	
	@Test
	public void testTradingPartnerIdFileInfoListEmpty() {
		List<SBMFileInfo> sbmFileInfoList = new ArrayList<SBMFileInfo>();
		
		String tradingPartnerId = SbmHelper.getTradingPartnerId(sbmFileInfoList);
		LOG.info("tradingPartnerId: {}", tradingPartnerId);
		
		Assert.assertTrue("TradingPartnerId should not be blank", StringUtils.isBlank(tradingPartnerId));
	}
	
	@Test
	public void testZipFileTradingPartnerId() {
		String filename = "CA1.EPS.SBMI.D160707.T090101001.T";
		
		String tradingPartnerId = SbmHelper.getZipFileTradingPartnerId(filename);
		LOG.info("tradingPartnerId: {}", tradingPartnerId);
		
		Assert.assertTrue("TradingPartnerId should not be blank", StringUtils.isNotBlank(tradingPartnerId));
		Assert.assertEquals("TradingPartnerId of zip file", "CA1", tradingPartnerId);
	}
	
	@Test
	public void testPreEFTFormatTradingPartnerId() {
		String filename = "CA1.EPS.SBMI.D160707.T090101001.T";
		
		String tradingPartnerId = SbmHelper.getTradingPartnerIdFromPreEFTFormat(filename);
		LOG.info("tradingPartnerId: {}", tradingPartnerId);
		
		Assert.assertTrue("TradingPartnerId should not be blank", StringUtils.isNotBlank(tradingPartnerId));
		Assert.assertEquals("TradingPartnerId of zip file", "CA1", tradingPartnerId);
	}
	
		
	@Test
	public void testgetCreateError() {
		
		SBMUpdateStatusErrorDTO dto = SbmHelper.createError("1","err","errorDesc","fileId","fileSetId");
		
		Assert.assertNotNull("dto not null", dto);
	}
	
	@Test
	public void testgetCreateError1() {
		
		SBMUpdateStatusErrorDTO dto = SbmHelper.createError("1", SBMErrorWarningCode.ER_001);
		
		Assert.assertNotNull("dto not null", dto);
	}
	
	@Test
	public void testIsFileStatusMatched() {
		
		boolean result = SbmHelper.isFileStatusMatched(SBMFileStatus.ACCEPTED, SBMFileStatus.ACCEPTED, SBMFileStatus.REJECTED);
		
		Assert.assertTrue("result", result);
	}
	
	@Test
	public void testIsFileStatusMatchedNull() {
		
		boolean result = SbmHelper.isFileStatusMatched(null, SBMFileStatus.ACCEPTED);
		
		Assert.assertFalse("result", result);
	}
	
	@Test
	public void testCreateErrorLog() {
		
		SBMErrorDTO errDto = SbmHelper.createErrorLog("ErrorElement", "ERR-001");
		
		assertNotNull("errDto", errDto);
		assertEquals("Element In error", "ErrorElement", errDto.getElementInErrorNm());
		assertEquals("Error Cd", "ERR-001", errDto.getSbmErrorWarningTypeCd());
	}
	
	@Test
	public void testCreateErrorLogWithAdditionalErrorInfo() {
		
		SBMErrorDTO errDto = SbmHelper.createErrorLog("ErrorElement", "ERR-001", "Incorrect value");
		
		assertNotNull("errDto", errDto);
		assertEquals("Element In error", "ErrorElement", errDto.getElementInErrorNm());
		assertEquals("Error Cd", "ERR-001", errDto.getSbmErrorWarningTypeCd());
		assertEquals("AdditionalErrorInfo", "Incorrect value", errDto.getAdditionalErrorInfoList().get(0));
	}
	
	@Test
	public void testCreateErrorLogWithAdditionalErrorInfoAndMemberId() {
		
		SBMErrorDTO errDto = SbmHelper.createErrorWithMemId("ErrorElement", "ERR-001", "Mem-001", "Incorrect value");
		
		assertNotNull("errDto", errDto);
		assertEquals("Element In error", "ErrorElement", errDto.getElementInErrorNm());
		assertEquals("Error Cd", "ERR-001", errDto.getSbmErrorWarningTypeCd());
		assertEquals("Member Id", "Mem-001", errDto.getExchangeAssignedMemberId());
		assertEquals("AdditionalErrorInfo", "Incorrect value", errDto.getAdditionalErrorInfoList().get(0));
	}
	
	@Test
	public void testIsNotRejectedNotDisapproved() {
		SBMSummaryAndFileInfoDTO summaryDto = new SBMSummaryAndFileInfoDTO();
		summaryDto.setSbmFileStatusType(SBMFileStatus.ACCEPTED);
		
		SBMFileInfo fileInfo = new SBMFileInfo();
		List<SBMFileInfo> sbmFileInfoList = Arrays.asList(fileInfo);
		summaryDto.getSbmFileInfoList().addAll(sbmFileInfoList);
		
		List<SBMSummaryAndFileInfoDTO> summaryDtoList = Arrays.asList(summaryDto);
		
		boolean result = SbmHelper.isNotRejectedNotDisapproved(summaryDtoList);
		LOG.info("result: {}", result);
		
		Assert.assertTrue("result", result);
	}
	
	@Test
	public void testIsNotRejectedNotDisapproved_Rejected() {
		SBMSummaryAndFileInfoDTO summaryDto = new SBMSummaryAndFileInfoDTO();
		summaryDto.setSbmFileStatusType(SBMFileStatus.REJECTED);
		
		List<SBMSummaryAndFileInfoDTO> summaryDtoList = Arrays.asList(summaryDto);
		
		boolean result = SbmHelper.isNotRejectedNotDisapproved(summaryDtoList);
		LOG.info("result: {}", result);
		
		Assert.assertFalse("result", result);
	}
	
	@Test
	public void testIsNotRejectedNotDisapproved_DISAPPROVED() {
		SBMSummaryAndFileInfoDTO summaryDto = new SBMSummaryAndFileInfoDTO();
		summaryDto.setSbmFileStatusType(SBMFileStatus.DISAPPROVED);
		
		List<SBMSummaryAndFileInfoDTO> summaryDtoList = Arrays.asList(summaryDto);
		
		boolean result = SbmHelper.isNotRejectedNotDisapproved(summaryDtoList);
		LOG.info("result: {}", result);
		
		Assert.assertFalse("result", result);
	}
	
	@Test
	public void testIsNotRejectedNotDisapproved_rejectInd() {
		SBMSummaryAndFileInfoDTO summaryDto = new SBMSummaryAndFileInfoDTO();
		summaryDto.setSbmFileStatusType(SBMFileStatus.ACCEPTED_WITH_WARNINGS);
		
		SBMFileInfo fileInfo = new SBMFileInfo();
		fileInfo.setRejectedInd(true);
		List<SBMFileInfo> sbmFileInfoList = Arrays.asList(fileInfo);
		summaryDto.getSbmFileInfoList().addAll(sbmFileInfoList);
		
		List<SBMSummaryAndFileInfoDTO> summaryDtoList = Arrays.asList(summaryDto);
		
		boolean result = SbmHelper.isNotRejectedNotDisapproved(summaryDtoList);
		LOG.info("result: {}", result);
		
		Assert.assertFalse("result", result);
	}

	@Test
	public void testGetFunctionCode_Empty() {
		String filename = "";
		
		String fuctionCode = SbmHelper.getFunctionCodeFromFile(filename);
		LOG.info("fuctionCode: {}", fuctionCode);
		
		Assert.assertTrue("fuctionCode should be blank", StringUtils.isBlank(fuctionCode));
	}
	
	@Test
	public void testGetFunctionCodeFromPreEFTFormat_Empty() {
		String filename = "";
		
		String fuctionCode = SbmHelper.getFunctionCodeFromPreEFTFormat(filename);
		LOG.info("fuctionCode: {}", fuctionCode);
		
		Assert.assertTrue("fuctionCode should be blank", StringUtils.isBlank(fuctionCode));
	}
	
	@Test
	public void testTradingPartnerId_Empty() {
		String filename = "CA1";
		
		String tradingPartnerId = SbmHelper.getTradingPartnerId(filename);
		LOG.info("fuctionCode: {}", tradingPartnerId);
		
		Assert.assertTrue("TradingPartnerId should be blank", StringUtils.isBlank(tradingPartnerId));
	}
	
	@Test
	public void testPreEFTFormatTradingPartnerId_Empty() {
		String filename = "CA1";
		
		String tradingPartnerId = SbmHelper.getTradingPartnerIdFromPreEFTFormat(filename);
		LOG.info("tradingPartnerId: {}", tradingPartnerId);
		
		Assert.assertTrue("TradingPartnerId should be blank", StringUtils.isBlank(tradingPartnerId));
	}
	
	@Test
	public void testZipFileTradingPartnerId_Empty() {
		String filename = "";
		
		String tradingPartnerId = SbmHelper.getZipFileTradingPartnerId(filename);
		LOG.info("tradingPartnerId: {}", tradingPartnerId);
		
		Assert.assertTrue("TradingPartnerId should be blank", StringUtils.isBlank(tradingPartnerId));
	}
	
}
