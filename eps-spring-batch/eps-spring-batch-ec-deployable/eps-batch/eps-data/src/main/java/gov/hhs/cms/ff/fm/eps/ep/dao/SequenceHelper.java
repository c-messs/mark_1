package gov.hhs.cms.ff.fm.eps.ep.dao;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author eps
 *
 */
public class SequenceHelper {
	private JdbcTemplate jdbcTemplate;
	
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	
	/**
	 * @param sequenceName
	 * @return
	 */
	public Long nextSequenceId(String sequenceName) {
		return jdbcTemplate.queryForObject("select "+sequenceName+".NextVal from dual", Long.class);
	}

}
