package gov.hhs.cms.ff.fm.eps.ep.jobs.util;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;

public class SBMTestDataDBUtil {
	
	public static final Logger LOGGER = LoggerFactory.getLogger(SBMTestDataDBUtil.class);

	public static void executeScript(JdbcTemplate jdbcTemplate, String relativeFilePath, boolean continueOnError) throws SQLException {
		
		LOGGER.info("Executing script relativeFilePath :{}", relativeFilePath);
		
		String absoluteFilePath = new File("").getAbsolutePath().replace("\\", "/") + relativeFilePath;
		
		LOGGER.info("Absolute file path :{}", absoluteFilePath);

		Connection connection = jdbcTemplate.getDataSource().getConnection();
		
		EncodedResource sql = new EncodedResource(new FileSystemResource(new File(absoluteFilePath)));
		
		ScriptUtils.executeSqlScript(connection, sql, continueOnError,
				false, ScriptUtils.DEFAULT_COMMENT_PREFIX,
				ScriptUtils.DEFAULT_STATEMENT_SEPARATOR,
				ScriptUtils.DEFAULT_BLOCK_COMMENT_START_DELIMITER,
				ScriptUtils.DEFAULT_BLOCK_COMMENT_END_DELIMITER);

		connection.close();

	}

	public static void cleanupData(JdbcTemplate jdbcTemplate) throws SQLException {		
		executeScript(jdbcTemplate, "/src/test/resources/sql/deleteSBMData.sql", false);
	}
	
	public static Integer selectData(JdbcTemplate jdbcTemplate, String sql) {
		int count = 0;
		LOGGER.info("Running query: " + sql);
		count = jdbcTemplate.queryForObject(sql, Integer.class);
		LOGGER.debug("Query retrieved " + count
				+ " items");
		return count;
	}
		
}
