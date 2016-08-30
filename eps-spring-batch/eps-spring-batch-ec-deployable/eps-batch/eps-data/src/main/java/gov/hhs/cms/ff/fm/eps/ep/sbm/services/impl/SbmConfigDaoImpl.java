package gov.hhs.cms.ff.fm.eps.ep.sbm.services.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import gov.hhs.cms.ff.fm.eps.ep.StateProrationConfiguration;
import gov.hhs.cms.ff.fm.eps.ep.sbm.services.SbmConfigDao;

/**
 * @author j.radziewski
 *
 */
public class SbmConfigDaoImpl implements SbmConfigDao {

	private JdbcTemplate jdbcTemplate;

	private String selectSbmStatesSql;
	private String selectSbmBusinessRules;
	private String selectLanguageCodes;
	private String selectRaceEthnicityCodes;
	private String selectErrorWarningTypes;

	@Override
	public List<StateProrationConfiguration> retrieveSbmStates() {

		List<StateProrationConfiguration> sbmStateList = 
				(List<StateProrationConfiguration>) jdbcTemplate.query(selectSbmStatesSql, new StateRowMapper());
		return sbmStateList;
	}
	
	@Override
	public List<String []> retrieveBusinessRules() {

		return (jdbcTemplate.query(selectSbmBusinessRules, new BusinessRulesRowMapper()));
	}
	
	@Override
	public List<String> retrieveLanguageCodes() {

		return (jdbcTemplate.queryForList(selectLanguageCodes, String.class));
	}
	
	@Override
	public List<String> retrieveRaceEthnicityCodes() {

		return (jdbcTemplate.queryForList(selectRaceEthnicityCodes, String.class));
	}
	
	@Override
	public Map<String, String> retrieveErrorCodesAndDescription() {

		return (jdbcTemplate.query(selectErrorWarningTypes, new ErrorWarningRowMapper())).get(0);
	}
	
	
	static private class StateRowMapper implements RowMapper<StateProrationConfiguration> {

		@Override
		public StateProrationConfiguration mapRow(ResultSet rs, int rowNum) throws SQLException {

			StateProrationConfiguration row = new StateProrationConfiguration();

			row.setStateCd(rs.getString("STATECD"));
			
			row.setMarketYear(Integer.parseInt(rs.getString("MARKETYEAR")));
			row.setProrationTypeCd(rs.getString("PRORATIONTYPECD"));
			row.setErrorThresholdPercent(rs.getBigDecimal("ERRORTHRESHOLDPERCENT"));
			String ind = rs.getString("CMSAPPROVALREQUIREDIND");
			if (ind != null) {
				if (ind.equals("Y")) {
					row.setCmsApprovalRequiredInd(true);
				} else {
					row.setCmsApprovalRequiredInd(false);
				}
			} else {
				row.setCmsApprovalRequiredInd(false);
			}
			row.setSbmInd(true);
			return row;
		}
	}

	static private class BusinessRulesRowMapper implements RowMapper<String[]> {

		@Override
		public String[] mapRow(ResultSet rs, int rowNum) throws SQLException {

			String[] row = {rs.getString("STATECD"), rs.getString("SBMBUSINESSRULETYPECD")};

			return row;
		}
	}
	
	static private class ErrorWarningRowMapper implements RowMapper<Map<String, String>> {

		Map<String, String> errorsMap = new HashMap<String, String>();
		
		@Override
		public Map<String, String> mapRow(ResultSet rs, int rowNum) throws SQLException {
			
			errorsMap.put(rs.getString("SBMERRORWARNINGTYPECD"), rs.getString("SBMERRORWARNINGTYPENM"));

			return errorsMap;
		}
	}


	/**
	 * @param jdbcTemplate the jdbcTemplate to set
	 */
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * @param selectSbmStatesSql the selectSbmStatesSql to set
	 */
	public void setSelectSbmStatesSql(String selectSbmStatesSql) {
		this.selectSbmStatesSql = selectSbmStatesSql;
	}

	/**
	 * @param selectSbmBusinessRules the selectSbmBusinessRules to set
	 */
	public void setSelectSbmBusinessRules(String selectSbmBusinessRules) {
		this.selectSbmBusinessRules = selectSbmBusinessRules;
	}

	/**
	 * @param selectLanguageCodes the selectLanguageCodes to set
	 */
	public void setSelectLanguageCodes(String selectLanguageCodes) {
		this.selectLanguageCodes = selectLanguageCodes;
	}

	/**
	 * @param selectRaceEthnicityCodes the selectRaceEthnicityCodes to set
	 */
	public void setSelectRaceEthnicityCodes(String selectRaceEthnicityCodes) {
		this.selectRaceEthnicityCodes = selectRaceEthnicityCodes;
	}

	/**
	 * @param selectErrorWarningTypes the selectErrorWarningTypes to set
	 */
	public void setSelectErrorWarningTypes(String selectErrorWarningTypes) {
		this.selectErrorWarningTypes = selectErrorWarningTypes;
	}
	
}
