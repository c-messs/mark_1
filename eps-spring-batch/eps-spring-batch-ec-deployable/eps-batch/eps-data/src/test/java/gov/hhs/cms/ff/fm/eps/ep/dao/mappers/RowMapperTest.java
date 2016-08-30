package gov.hhs.cms.ff.fm.eps.ep.dao.mappers;

import gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentMaintenanceDTO;
import gov.hhs.cms.ff.fm.eps.ep.dao.EpsBeanPropertyRowMapper;
import gov.hhs.cms.ff.fm.eps.ep.po.ErrorWarningLogPO;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/eps-data-config.xml", "classpath:/test-context-data.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class RowMapperTest extends BaseRowMapperTest {


	@Test
	public void test_BemDTORowMapper_TransMsgId_EQ_0() {

		Long expected = null;
		LocalDateTime expectedFileDateTime = LocalDateTime.now();
		Long id = insertDailyBemIndexerMinimum(expectedFileDateTime);
		String sql = "SELECT * FROM DAILYBEMINDEXER WHERE BEMINDEXID = " + id;
		List<BenefitEnrollmentMaintenanceDTO> actualList = jdbc.query(sql, new BemDTORowMapper());
		assertEquals("actual row insert", 1, actualList.size());
		BenefitEnrollmentMaintenanceDTO actual = actualList.get(0);
		assertEquals("Null translated to '0'", expected, actual.getTransMsgId());
	}
	
	@Test
	public void test_ErrorWarningLogRowMapper_Ids_EQ_0() {

		Long expected = null;
		Long id = insertErrorWarningLogMinimum();
		String sql = "SELECT * FROM ERRORWARNINGLOG WHERE ERRORWARNINGID = " + id;
		List<ErrorWarningLogPO> actualList = jdbc.query(sql, new ErrorWarningLogRowMapper());
		assertEquals("actual row insert", 1, actualList.size());
		ErrorWarningLogPO actual = actualList.get(0);
		assertEquals("Null TransMsgID translated to '0'", expected, actual.getTransMsgID());
		assertEquals("Null TransMsgFileInfoId translated to '0'", expected, actual.getTransMsgFileInfoId());
		assertEquals("Null BatchId translated to '0'", expected, actual.getBatchId());
	}
	
	@Test
	public void test_EpsBeanPropertyRowMapper_Ids_EQ_0() {

		Long expected = null;
		LocalDateTime expectedFileDateTime = LocalDateTime.now();
		Long bemIndexId = insertDailyBemIndexerMinimum(expectedFileDateTime);
		String sql = "SELECT * FROM DAILYBEMINDEXER WHERE BEMINDEXID = " + bemIndexId;
		List<BenefitEnrollmentMaintenanceDTO> actualList = jdbc.query(sql,
				new EpsBeanPropertyRowMapper<BenefitEnrollmentMaintenanceDTO>(BenefitEnrollmentMaintenanceDTO.class));
		assertEquals("actual row insert", 1, actualList.size());
		BenefitEnrollmentMaintenanceDTO actual = actualList.get(0);
		assertEquals("Null TransMsgID translated to '0'", expected, actual.getTransMsgId());
		assertEquals("Null PolicyVersionId translated to '0'", expected, actual.getPolicyVersionId());
		assertEquals("Null BatchId translated to '0'", expected, actual.getBatchId());
		assertEquals("Null SourceVersionId translated to '0'", expected, actual.getSourceVersionId());
		assertEquals("Null IngestJobId translated to '0'", expected, actual.getIngestJobId());
		assertEquals("SourceVersionDateTime", expected, actual.getSourceVersionDateTime());
		assertEquals("FileNmDateTime", expectedFileDateTime, actual.getFileNmDateTime());
	}

}
