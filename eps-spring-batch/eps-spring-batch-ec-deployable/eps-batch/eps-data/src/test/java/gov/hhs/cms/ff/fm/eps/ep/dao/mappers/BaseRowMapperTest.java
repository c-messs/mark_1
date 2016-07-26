package gov.hhs.cms.ff.fm.eps.ep.dao.mappers;

import junit.framework.TestCase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public abstract class BaseRowMapperTest extends TestCase {

	@Autowired
	protected JdbcTemplate jdbc;


	protected Long insertDailyBemIndexerMinimum() {

		Long id = jdbc.queryForObject("SELECT BEMINDEXSEQ.NEXTVAL FROM DUAL", Long.class);
		String sql = "INSERT INTO DAILYBEMINDEXER (BEMINDEXID, FILEINFOXML, BEMXML) " +
				"VALUES(" + id + ", '<FileInfo>" + id + "</FileInfo>', " +
				"'<Bem>Policy</Bem>')";
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
	
	

}
