package gov.hhs.cms.ff.fm.eps.ep.jobs.sbm;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalField;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.hhs.cms.ff.fm.eps.ep.enums.SBMFileStatus;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMFileInfo;
import gov.hhs.cms.ff.fm.eps.ep.sbm.SBMSummaryAndFileInfoDTO;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SBMFileCompositeDAO;

public class SBMEvaluatePendingFilesTest {
	
	private static final Logger LOG = LoggerFactory.getLogger(SBMEvaluatePendingFilesTest.class);
	
	private SBMEvaluatePendingFiles sbmEvaluatePendingFiles;
	private SBMResponseGenerator mockResponseGenerator;
	private SBMFileCompositeDAO mockFileCompositeDao;
	
	@Before
	public void setUp() {
		sbmEvaluatePendingFiles = new SBMEvaluatePendingFiles();
		
		mockResponseGenerator = Mockito.mock(SBMResponseGenerator.class);
		mockFileCompositeDao = Mockito.mock(SBMFileCompositeDAO.class);
		
		sbmEvaluatePendingFiles.setFileCompositeDao(mockFileCompositeDao);
		sbmEvaluatePendingFiles.setResponseGenerator(mockResponseGenerator);
			
	}
	
	@Test
	public void testPendingFiles_deadlineExpired() throws JAXBException, SQLException, IOException {
		
		sbmEvaluatePendingFiles.setFileSetDeadlineHours(72);
		
		LocalDate today = LocalDate.now();
		if(today.getDayOfMonth() < 15) {
			sbmEvaluatePendingFiles.setFreezePeriodStartDay(20);
			sbmEvaluatePendingFiles.setFreezePeriodEndDay(23);
		}
		else {
			sbmEvaluatePendingFiles.setFreezePeriodStartDay(5);
			sbmEvaluatePendingFiles.setFreezePeriodEndDay(8);
		}
		
		List<SBMSummaryAndFileInfoDTO> summaryDtoList = new ArrayList<>();
		
		SBMSummaryAndFileInfoDTO dto = new SBMSummaryAndFileInfoDTO();
		dto.setSbmFileProcSumId(11001L);
		dto.setSbmFileStatusType(SBMFileStatus.PENDING_FILES);
		
		List<SBMFileInfo> fileInfoList = new ArrayList<>();
		SBMFileInfo fileInfo = new SBMFileInfo();
		fileInfo.setCreateDatetime(getDateTime(2016, 8, 1));
		fileInfoList.add(fileInfo);
		
		dto.getSbmFileInfoList().addAll(fileInfoList);
		summaryDtoList.add(dto);
		
		Mockito.when(mockFileCompositeDao.getAllSBMFileProcessingSummary(SBMFileStatus.PENDING_FILES)).thenReturn(summaryDtoList);
		
		sbmEvaluatePendingFiles.evaluatePendingFiles(123L, true);
		
		Assert.assertTrue("Status should be changed to EXPIRED", SBMFileStatus.EXPIRED.equals(dto.getSbmFileStatusType()));
		
	}
	
//	@Test
	public void testPendingFiles_deadlineExpiredInFreeze() throws JAXBException, SQLException, IOException {
		
		sbmEvaluatePendingFiles.setFileSetDeadlineHours(72);
		
		LocalDate today = LocalDate.now();
		
		sbmEvaluatePendingFiles.setFreezePeriodStartDay(2);
		sbmEvaluatePendingFiles.setFreezePeriodEndDay(4);
		
		
		List<SBMSummaryAndFileInfoDTO> summaryDtoList = new ArrayList<>();
		
		SBMSummaryAndFileInfoDTO dto = new SBMSummaryAndFileInfoDTO();
		dto.setSbmFileProcSumId(11001L);
		dto.setSbmFileStatusType(SBMFileStatus.PENDING_FILES);
		
		List<SBMFileInfo> fileInfoList = new ArrayList<>();
		SBMFileInfo fileInfo = new SBMFileInfo();
		fileInfo.setCreateDatetime(getDateTime(2016, 8, 1));
		fileInfoList.add(fileInfo);
		
		dto.getSbmFileInfoList().addAll(fileInfoList);
		summaryDtoList.add(dto);
		
		Mockito.when(mockFileCompositeDao.getAllSBMFileProcessingSummary(SBMFileStatus.PENDING_FILES)).thenReturn(summaryDtoList);
		
		sbmEvaluatePendingFiles.evaluatePendingFiles(123L, true);
		
		Assert.assertTrue("Status should be changed to EXPIRED", SBMFileStatus.EXPIRED.equals(dto.getSbmFileStatusType()));
		
	}
	
	
//	@Test
	public void testPendingFiles_deadlineExpiredInFreezeEndDate() throws JAXBException, SQLException, IOException {
		
		sbmEvaluatePendingFiles.setFileSetDeadlineHours(72);
		
		LocalDate today = LocalDate.now();
		
		sbmEvaluatePendingFiles.setFreezePeriodStartDay(2);
		sbmEvaluatePendingFiles.setFreezePeriodEndDay(4);
		
		
		List<SBMSummaryAndFileInfoDTO> summaryDtoList = new ArrayList<>();
		
		SBMSummaryAndFileInfoDTO dto = new SBMSummaryAndFileInfoDTO();
		dto.setSbmFileProcSumId(11001L);
		dto.setSbmFileStatusType(SBMFileStatus.PENDING_FILES);
		
		List<SBMFileInfo> fileInfoList = new ArrayList<>();
		SBMFileInfo fileInfo = new SBMFileInfo();
		fileInfo.setCreateDatetime(getDateTime(2016, 8, 3));
		fileInfoList.add(fileInfo);
		
		dto.getSbmFileInfoList().addAll(fileInfoList);
		summaryDtoList.add(dto);
		
		Mockito.when(mockFileCompositeDao.getAllSBMFileProcessingSummary(SBMFileStatus.PENDING_FILES)).thenReturn(summaryDtoList);
		
		sbmEvaluatePendingFiles.evaluatePendingFiles(123L, true);
		
		Assert.assertTrue("Status should be changed to EXPIRED", SBMFileStatus.EXPIRED.equals(dto.getSbmFileStatusType()));
		
	}
	
//	@Test
	public void testPendingFiles_expiredlLongerFreeze() throws JAXBException, SQLException, IOException {
		
		sbmEvaluatePendingFiles.setFileSetDeadlineHours(72);
		
		LocalDate today = LocalDate.now();
		
		sbmEvaluatePendingFiles.setFreezePeriodStartDay(2);
		sbmEvaluatePendingFiles.setFreezePeriodEndDay(9);
		
		
		List<SBMSummaryAndFileInfoDTO> summaryDtoList = new ArrayList<>();
		
		SBMSummaryAndFileInfoDTO dto = new SBMSummaryAndFileInfoDTO();
		dto.setSbmFileProcSumId(11001L);
		dto.setSbmFileStatusType(SBMFileStatus.PENDING_FILES);
		
		List<SBMFileInfo> fileInfoList = new ArrayList<>();
		SBMFileInfo fileInfo = new SBMFileInfo();
		fileInfo.setCreateDatetime(getDateTime(2016, 8, 3));
		fileInfoList.add(fileInfo);
		
		dto.getSbmFileInfoList().addAll(fileInfoList);
		summaryDtoList.add(dto);
		
		Mockito.when(mockFileCompositeDao.getAllSBMFileProcessingSummary(SBMFileStatus.PENDING_FILES)).thenReturn(summaryDtoList);
		
		sbmEvaluatePendingFiles.evaluatePendingFiles(123L, true);
		
		Assert.assertTrue("Status should be changed to EXPIRED", SBMFileStatus.EXPIRED.equals(dto.getSbmFileStatusType()));
		
	}
	
	//@Test
	public void testPendingFiles_expiredlLongerDeadline() throws JAXBException, SQLException, IOException {
		
		sbmEvaluatePendingFiles.setFileSetDeadlineHours(72);
		
		LocalDate today = LocalDate.now();
		
		sbmEvaluatePendingFiles.setFreezePeriodStartDay(5);
		sbmEvaluatePendingFiles.setFreezePeriodEndDay(7);
		
		
		List<SBMSummaryAndFileInfoDTO> summaryDtoList = new ArrayList<>();
		
		SBMSummaryAndFileInfoDTO dto = new SBMSummaryAndFileInfoDTO();
		dto.setSbmFileProcSumId(11001L);
		dto.setSbmFileStatusType(SBMFileStatus.PENDING_FILES);
		
		List<SBMFileInfo> fileInfoList = new ArrayList<>();
		SBMFileInfo fileInfo = new SBMFileInfo();
		fileInfo.setCreateDatetime(getDateTime(2016, 8, 2));
		fileInfoList.add(fileInfo);
		
		dto.getSbmFileInfoList().addAll(fileInfoList);
		summaryDtoList.add(dto);
		
		Mockito.when(mockFileCompositeDao.getAllSBMFileProcessingSummary(SBMFileStatus.PENDING_FILES)).thenReturn(summaryDtoList);
		
		sbmEvaluatePendingFiles.evaluatePendingFiles(123L, true);
		
		Assert.assertTrue("Status should be changed to EXPIRED", SBMFileStatus.EXPIRED.equals(dto.getSbmFileStatusType()));
		
	}
	
	@Test
	public void testPendingFiles_expiredlLongerDeadline_2() throws JAXBException, SQLException, IOException {
		
		sbmEvaluatePendingFiles.setFileSetDeadlineHours(72);
		
		LocalDate today = LocalDate.now();
		
		//Freeze Period cannot be across months
		int freezeStartDay = 3;
		int freezeEndDay = 4;
		
		sbmEvaluatePendingFiles.setFreezePeriodStartDay(freezeStartDay);
		sbmEvaluatePendingFiles.setFreezePeriodEndDay(freezeEndDay);
		
		
		List<SBMSummaryAndFileInfoDTO> summaryDtoList = new ArrayList<>();
		
		SBMSummaryAndFileInfoDTO dto = new SBMSummaryAndFileInfoDTO();
		dto.setSbmFileProcSumId(11001L);
		dto.setSbmFileStatusType(SBMFileStatus.PENDING_FILES);
		
		List<SBMFileInfo> fileInfoList = new ArrayList<>();
		SBMFileInfo fileInfo = new SBMFileInfo();
		fileInfo.setCreateDatetime(getDateTime(2016, 8, 2));
		fileInfoList.add(fileInfo);
		
		dto.getSbmFileInfoList().addAll(fileInfoList);
		summaryDtoList.add(dto);
		
		Mockito.when(mockFileCompositeDao.getAllSBMFileProcessingSummary(SBMFileStatus.PENDING_FILES)).thenReturn(summaryDtoList);
		
		sbmEvaluatePendingFiles.evaluatePendingFiles(123L, true);
		
		if (today.getDayOfMonth() == freezeStartDay || today.getDayOfMonth() == freezeEndDay) {
			Assert.assertTrue("Status should be PENDING FILES", SBMFileStatus.PENDING_FILES.equals(dto.getSbmFileStatusType()));
		} else {
			Assert.assertTrue("Status should be changed to EXPIRED", SBMFileStatus.EXPIRED.equals(dto.getSbmFileStatusType()));
		}
	}

	@Test
	public void testPendingFiles_deadlineNotExpired() throws JAXBException, SQLException, IOException {
		
		sbmEvaluatePendingFiles.setFileSetDeadlineHours(24);
		
		sbmEvaluatePendingFiles.setFreezePeriodStartDay(2);
		sbmEvaluatePendingFiles.setFreezePeriodEndDay(2);
		
		List<SBMSummaryAndFileInfoDTO> summaryDtoList = new ArrayList<>();
		
		SBMSummaryAndFileInfoDTO dto = new SBMSummaryAndFileInfoDTO();
		dto.setSbmFileProcSumId(11001L);
		dto.setSbmFileStatusType(SBMFileStatus.PENDING_FILES);
		
		List<SBMFileInfo> fileInfoList = new ArrayList<>();
		SBMFileInfo fileInfo = new SBMFileInfo();
		fileInfo.setCreateDatetime(getDateTime(2099, 8, 1)); //give high date
		fileInfoList.add(fileInfo);
		
		dto.getSbmFileInfoList().addAll(fileInfoList);
		summaryDtoList.add(dto);
		
		Mockito.when(mockFileCompositeDao.getAllSBMFileProcessingSummary(SBMFileStatus.PENDING_FILES)).thenReturn(summaryDtoList);
		
		sbmEvaluatePendingFiles.evaluatePendingFiles(123L, true);
		
		Assert.assertTrue("Status should be changed to PENDING_FILES", SBMFileStatus.PENDING_FILES.equals(dto.getSbmFileStatusType()));
		
	}
	

	@Test
	public void testPendingFiles_deadlineNotExpired_3() throws JAXBException, SQLException, IOException {
		
		int deadlineDays = 2;
		LocalDate today = LocalDate.now();
		
		sbmEvaluatePendingFiles.setFileSetDeadlineHours(deadlineDays);
		
		sbmEvaluatePendingFiles.setFreezePeriodStartDay(today.getDayOfMonth());
		sbmEvaluatePendingFiles.setFreezePeriodEndDay(today.getDayOfMonth());
		
		List<SBMSummaryAndFileInfoDTO> summaryDtoList = new ArrayList<>();
		
		SBMSummaryAndFileInfoDTO dto = new SBMSummaryAndFileInfoDTO();
		dto.setSbmFileProcSumId(11001L);
		dto.setSbmFileStatusType(SBMFileStatus.PENDING_FILES);
		
		List<SBMFileInfo> fileInfoList = new ArrayList<>();
		SBMFileInfo fileInfo = new SBMFileInfo();
		fileInfo.setCreateDatetime(getDateTime(today.getYear(), today.getMonthValue(), today.getDayOfMonth()));
		fileInfoList.add(fileInfo);
		
		dto.getSbmFileInfoList().addAll(fileInfoList);
		summaryDtoList.add(dto);
		
		Mockito.when(mockFileCompositeDao.getAllSBMFileProcessingSummary(SBMFileStatus.PENDING_FILES)).thenReturn(summaryDtoList);
		
		sbmEvaluatePendingFiles.evaluatePendingFiles(123L, true);
		
		Assert.assertTrue("Status should be changed to PENDING_FILES", SBMFileStatus.PENDING_FILES.equals(dto.getSbmFileStatusType()));
		
	}
	
	@Test
	public void testPendingFiles_freezeEndDateLastDayOfMonth_noStatusChange() throws JAXBException, SQLException, IOException {
		
		sbmEvaluatePendingFiles.setFileSetDeadlineHours(72);
		
		LocalDate today = LocalDate.now();
		
		LocalDate lastDayOfMonth = today.withDayOfMonth(1).plusMonths(1).minusDays(1);
		
		sbmEvaluatePendingFiles.setFreezePeriodStartDay(1);
		sbmEvaluatePendingFiles.setFreezePeriodEndDay(lastDayOfMonth.getDayOfMonth());
		
		
		List<SBMSummaryAndFileInfoDTO> summaryDtoList = new ArrayList<>();
		
		SBMSummaryAndFileInfoDTO dto = new SBMSummaryAndFileInfoDTO();
		dto.setSbmFileProcSumId(11001L);
		dto.setSbmFileStatusType(SBMFileStatus.PENDING_FILES);
		
		List<SBMFileInfo> fileInfoList = new ArrayList<>();
		SBMFileInfo fileInfo = new SBMFileInfo();
		fileInfo.setCreateDatetime(getDateTime(2016, 8, 3));
		fileInfoList.add(fileInfo);
		
		dto.getSbmFileInfoList().addAll(fileInfoList);
		summaryDtoList.add(dto);
		
		Mockito.when(mockFileCompositeDao.getAllSBMFileProcessingSummary(SBMFileStatus.PENDING_FILES)).thenReturn(summaryDtoList);
		
		sbmEvaluatePendingFiles.evaluatePendingFiles(123L, true);
		
		Assert.assertTrue("Status should be changed to EXPIRED", SBMFileStatus.PENDING_FILES.equals(dto.getSbmFileStatusType()));
		
	}
	
	@Test
	public void testFreezeFiles() throws JAXBException, SQLException, IOException {
		sbmEvaluatePendingFiles.setFileSetDeadlineHours(24);
		
		LocalDate today = LocalDate.now();
		if(today.getDayOfMonth() < 15) {
			sbmEvaluatePendingFiles.setFreezePeriodStartDay(20);
			sbmEvaluatePendingFiles.setFreezePeriodEndDay(22);
		}
		else {
			sbmEvaluatePendingFiles.setFreezePeriodStartDay(5);
			sbmEvaluatePendingFiles.setFreezePeriodEndDay(7);
		}
		
		List<SBMSummaryAndFileInfoDTO> summaryDtoList = new ArrayList<>();
		
		SBMSummaryAndFileInfoDTO dto = new SBMSummaryAndFileInfoDTO();
		dto.setSbmFileProcSumId(11001L);
		dto.setSbmFileStatusType(SBMFileStatus.FREEZE);
		
		List<SBMFileInfo> fileInfoList = new ArrayList<>();
		SBMFileInfo fileInfo = new SBMFileInfo();
		fileInfo.setCreateDatetime(getDateTime(2099, 8, 1)); //give high date
		fileInfoList.add(fileInfo);
		
		dto.getSbmFileInfoList().addAll(fileInfoList);
		summaryDtoList.add(dto);
		
		Mockito.when(mockFileCompositeDao.getAllSBMFileProcessingSummary(SBMFileStatus.FREEZE)).thenReturn(summaryDtoList);
		
		
		sbmEvaluatePendingFiles.evaluateFreezeFiles(123L);
		
		Assert.assertTrue("Status should be changed to PENDING_FILES", SBMFileStatus.IN_PROCESS.equals(dto.getSbmFileStatusType()));
	}
	
	@Test
	public void testFreezeFiles_setToPendingFiles() throws JAXBException, SQLException, IOException {
		sbmEvaluatePendingFiles.setFileSetDeadlineHours(24);
		
		LocalDate today = LocalDate.now();
		if(today.getDayOfMonth() < 15) {
			sbmEvaluatePendingFiles.setFreezePeriodStartDay(20);
			sbmEvaluatePendingFiles.setFreezePeriodEndDay(22);
		}
		else {
			sbmEvaluatePendingFiles.setFreezePeriodStartDay(5);
			sbmEvaluatePendingFiles.setFreezePeriodEndDay(7);
		}
		
		List<SBMSummaryAndFileInfoDTO> summaryDtoList = new ArrayList<>();
		
		SBMSummaryAndFileInfoDTO dto = new SBMSummaryAndFileInfoDTO();
		dto.setSbmFileProcSumId(11001L);
		dto.setSbmFileStatusType(SBMFileStatus.FREEZE);
		dto.setIssuerFileSetId("IFS10001");
		dto.setTotalIssuerFileCount(5);
		
		List<SBMFileInfo> fileInfoList = new ArrayList<>();
		SBMFileInfo fileInfo = new SBMFileInfo();
		fileInfo.setCreateDatetime(getDateTime(2016, 1, 1)); //give high date
		fileInfo.setSbmFileNum(1);
		fileInfoList.add(fileInfo);
		
		dto.getSbmFileInfoList().addAll(fileInfoList);
		summaryDtoList.add(dto);
		
		Mockito.when(mockFileCompositeDao.getAllSBMFileProcessingSummary(SBMFileStatus.FREEZE)).thenReturn(summaryDtoList);
				
		sbmEvaluatePendingFiles.evaluateFreezeFiles(123L);
		
		Assert.assertTrue("Status should be changed to PENDING_FILES", SBMFileStatus.PENDING_FILES.equals(dto.getSbmFileStatusType()));
	}
	
	@Test
	public void testFreezeFiles_setToOnHold() throws JAXBException, SQLException, IOException {
		sbmEvaluatePendingFiles.setFileSetDeadlineHours(24);
		
		LocalDate today = LocalDate.now();
		if(today.getDayOfMonth() < 15) {
			sbmEvaluatePendingFiles.setFreezePeriodStartDay(20);
			sbmEvaluatePendingFiles.setFreezePeriodEndDay(22);
		}
		else {
			sbmEvaluatePendingFiles.setFreezePeriodStartDay(5);
			sbmEvaluatePendingFiles.setFreezePeriodEndDay(7);
		}
		
		List<SBMSummaryAndFileInfoDTO> summaryDtoList = new ArrayList<>();
		
		SBMSummaryAndFileInfoDTO dto = new SBMSummaryAndFileInfoDTO();
		dto.setSbmFileProcSumId(11001L);
		dto.setSbmFileStatusType(SBMFileStatus.FREEZE);
		
		List<SBMFileInfo> fileInfoList = new ArrayList<>();
		SBMFileInfo fileInfo = new SBMFileInfo();
		fileInfo.setCreateDatetime(getDateTime(2099, 8, 1)); //give high date
		fileInfoList.add(fileInfo);
		
		dto.getSbmFileInfoList().addAll(fileInfoList);
		summaryDtoList.add(dto);
		
		Mockito.when(mockFileCompositeDao.getAllSBMFileProcessingSummary(SBMFileStatus.FREEZE)).thenReturn(summaryDtoList);
						
		List<SBMSummaryAndFileInfoDTO> inProcList = new ArrayList<>();
		
		SBMSummaryAndFileInfoDTO sumDto = new SBMSummaryAndFileInfoDTO();
		sumDto.setSbmFileProcSumId(11001L);
		sumDto.setSbmFileStatusType(SBMFileStatus.IN_PROCESS);		
		inProcList.add(sumDto);
		Mockito.when(mockFileCompositeDao.getAllInProcessOrPendingApprovalForState(Mockito.anyString())).thenReturn(inProcList);
		
		
		sbmEvaluatePendingFiles.evaluateFreezeFiles(123L);
		
		Assert.assertTrue("Status should be changed to PENDING_FILES", SBMFileStatus.ON_HOLD.equals(dto.getSbmFileStatusType()));
	}
		
	@Test
	public void testOnHold() throws JAXBException, SQLException, IOException {
				
		sbmEvaluatePendingFiles.setFileSetDeadlineHours(24);
		
		LocalDate today = LocalDate.now();
		if(today.getDayOfMonth() < 15) {
			sbmEvaluatePendingFiles.setFreezePeriodStartDay(20);
			sbmEvaluatePendingFiles.setFreezePeriodEndDay(22);
		}
		else {
			sbmEvaluatePendingFiles.setFreezePeriodStartDay(5);
			sbmEvaluatePendingFiles.setFreezePeriodEndDay(7);
		}
		
		List<SBMSummaryAndFileInfoDTO> summaryDtoList = new ArrayList<>();
		
		SBMSummaryAndFileInfoDTO dto = new SBMSummaryAndFileInfoDTO();
		dto.setSbmFileProcSumId(11001L);
		dto.setSbmFileStatusType(SBMFileStatus.ON_HOLD);
		
		List<SBMFileInfo> fileInfoList = new ArrayList<>();
		SBMFileInfo fileInfo = new SBMFileInfo();
		fileInfo.setCreateDatetime(getDateTime(2099, 8, 1)); //give high date
		fileInfoList.add(fileInfo);
		
		dto.getSbmFileInfoList().addAll(fileInfoList);
		summaryDtoList.add(dto);
		
		Mockito.when(mockFileCompositeDao.getAllSBMFileProcessingSummary(SBMFileStatus.ON_HOLD)).thenReturn(summaryDtoList);
		
		
		sbmEvaluatePendingFiles.evaluateOnHoldFiles(123L);
		
		Assert.assertTrue("Status should be changed to PENDING_FILES", SBMFileStatus.IN_PROCESS.equals(dto.getSbmFileStatusType()));
	}
	
	@Test
	public void testBypassFreeze() throws JAXBException, SQLException, IOException {
				
		sbmEvaluatePendingFiles.setFileSetDeadlineHours(24);
		
		LocalDate today = LocalDate.now();
		if(today.getDayOfMonth() < 15) {
			sbmEvaluatePendingFiles.setFreezePeriodStartDay(20);
			sbmEvaluatePendingFiles.setFreezePeriodEndDay(22);
		}
		else {
			sbmEvaluatePendingFiles.setFreezePeriodStartDay(5);
			sbmEvaluatePendingFiles.setFreezePeriodEndDay(7);
		}
		
		List<SBMSummaryAndFileInfoDTO> summaryDtoList = new ArrayList<>();
		
		SBMSummaryAndFileInfoDTO dto = new SBMSummaryAndFileInfoDTO();
		dto.setSbmFileProcSumId(11001L);
		dto.setSbmFileStatusType(SBMFileStatus.BYPASS_FREEZE);
		
		List<SBMFileInfo> fileInfoList = new ArrayList<>();
		SBMFileInfo fileInfo = new SBMFileInfo();
		fileInfo.setCreateDatetime(getDateTime(2099, 8, 1)); //give high date
		fileInfoList.add(fileInfo);
		
		dto.getSbmFileInfoList().addAll(fileInfoList);
		summaryDtoList.add(dto);
		
		Mockito.when(mockFileCompositeDao.getAllSBMFileProcessingSummary(SBMFileStatus.BYPASS_FREEZE)).thenReturn(summaryDtoList);
		
		
		sbmEvaluatePendingFiles.evaluateBypassFreeze(123L);
		
		Assert.assertTrue("Status should be changed to PENDING_FILES", SBMFileStatus.IN_PROCESS.equals(dto.getSbmFileStatusType()));
	}
	
	@Test
	public void testBypassFreezeToPendingFiles() throws JAXBException, SQLException, IOException {
				
		sbmEvaluatePendingFiles.setFileSetDeadlineHours(24);
		
		LocalDate today = LocalDate.now();
		if(today.getDayOfMonth() < 15) {
			sbmEvaluatePendingFiles.setFreezePeriodStartDay(20);
			sbmEvaluatePendingFiles.setFreezePeriodEndDay(22);
		}
		else {
			sbmEvaluatePendingFiles.setFreezePeriodStartDay(5);
			sbmEvaluatePendingFiles.setFreezePeriodEndDay(7);
		}
		
		List<SBMSummaryAndFileInfoDTO> summaryDtoList = new ArrayList<>();
		
		SBMSummaryAndFileInfoDTO dto = new SBMSummaryAndFileInfoDTO();
		dto.setSbmFileProcSumId(11001L);
		dto.setIssuerFileSetId("IFS2001");
		dto.setTotalIssuerFileCount(5);
		dto.setSbmFileStatusType(SBMFileStatus.BYPASS_FREEZE);
		
		List<SBMFileInfo> fileInfoList = new ArrayList<>();
		SBMFileInfo fileInfo = new SBMFileInfo();
		fileInfo.setCreateDatetime(getDateTime(2015, 8, 1)); //give high date
		fileInfo.setSbmFileNum(1);
		fileInfoList.add(fileInfo);
		
		dto.getSbmFileInfoList().addAll(fileInfoList);
		summaryDtoList.add(dto);
		
		Mockito.when(mockFileCompositeDao.getAllSBMFileProcessingSummary(SBMFileStatus.BYPASS_FREEZE)).thenReturn(summaryDtoList);
		
		
		sbmEvaluatePendingFiles.evaluateBypassFreeze(123L);
		
		Assert.assertTrue("Status should be changed to PENDING_FILES", SBMFileStatus.PENDING_FILES.equals(dto.getSbmFileStatusType()));
	}
		
		
	private LocalDateTime getDateTime(int year, int month, int day) {
		return LocalDateTime.now().withYear(year).withMonth(month).withDayOfMonth(day);
	}
	
	private LocalDateTime getDateTimeWithStartOfDay(int year, int month, int day) {
		return LocalDate.now().withYear(year).withMonth(month).withDayOfMonth(day).atStartOfDay();
	}

}
