package gov.hhs.cms.ff.fm.eps.ep.dao.mappers;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import junit.framework.TestCase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public abstract class BaseRowMapperTest extends TestCase {

	@Autowired
	protected JdbcTemplate jdbc;


	protected Long insertDailyBemIndexerMinimum(LocalDateTime fileDateTime) {

		Long id = jdbc.queryForObject("SELECT BEMINDEXSEQ.NEXTVAL FROM DUAL", Long.class);
		String sql = "INSERT INTO DAILYBEMINDEXER (BEMINDEXID, FILEINFOXML, BEMXML, FILENMDATETIME) " +
				"VALUES(" + id + ", '<FileInfo>" + id + "</FileInfo>', " +
				"'<Bem>Policy</Bem>', " + toTimestampValue(fileDateTime) + ")";
		jdbc.execute(sql);
		return id;
	}
	
	protected Long insertErrorWarningLogMinimum() {

		Long id = jdbc.queryForObject("SELECT ERRORWARNINGSEQ.NEXTVAL FROM DUAL", Long.class);
		String sql = "INSERT INTO ERRORWARNINGLOG (ERRORWARNINGID, BIZAPPACKERRORCD, CREATEDATETIME, LASTMODIFIEDDATETIME) " +
				"VALUES(" + id + ", 'E001', SYSDATE, SYSDATE)";
		jdbc.execute(sql);
		return id;
	}
	
	private String toTimestampValue(LocalDateTime ts) {
		return "TO_TIMESTAMP('" + Timestamp.valueOf(ts) + "', 'YYYY-MM-DD HH24:MI:SS.FF3')";
	}
	
	

}
