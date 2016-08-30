package gov.hhs.cms.ff.fm.eps.ep.sbm.dao.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;

import gov.hhs.cms.ff.fm.eps.ep.dao.SequenceHelper;
import gov.hhs.cms.ff.fm.eps.ep.dao.StagingSbmPolicyDao;
import gov.hhs.cms.ff.fm.eps.ep.vo.UserVO;

public class StagingSbmPolicyDaoImpl implements StagingSbmPolicyDao {

	private String extractXprtoStagingPolicySql;
	private String verifyStagingPolicySql;

	private JdbcTemplate jdbcTemplate;
	protected SequenceHelper sequenceHelper;
	

	@Override
	public int extractXprFromStagingSbmFile(final Integer groupSize, final String stateCd, final UserVO userVO) {

		int cntRows = jdbcTemplate.update(extractXprtoStagingPolicySql, new PreparedStatementSetter() {
			public void setValues(PreparedStatement ps) throws SQLException {
			
				ps.setInt(1, groupSize);
				ps.setString(2, stateCd);
				ps.setString(3,  userVO.getUserId());
				ps.setString(4,  userVO.getUserId());
				ps.setLong(5,  Long.parseLong(userVO.getUserId()));
			}
		});
		return cntRows;
	}

	@Override
	public boolean verifySbmStagingPolicyExist(Long sbmFileProcSumId) {

		int errorCnt = jdbcTemplate.queryForObject(verifyStagingPolicySql, Integer.class, sbmFileProcSumId);

		return (errorCnt > 0);
	}
	

	/**
	 * @param extractXprtoStagingPolicySql the extractXprtoStagingPolicySql to set
	 */
	public void setExtractXprtoStagingPolicySql(String extractXprtoStagingPolicySql) {
		this.extractXprtoStagingPolicySql = extractXprtoStagingPolicySql;
	}

	/**
	 * @param verifyStagingPolicySql the verifyStagingPolicySql to set
	 */
	public void setVerifyStagingPolicySql(String verifyStagingPolicySql) {
		this.verifyStagingPolicySql = verifyStagingPolicySql;
	}


	/**
	 * @param jdbcTemplate the jdbcTemplate to set
	 */
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * @param sequenceHelper the sequenceHelper to set
	 */
	public void setSequenceHelper(SequenceHelper sequenceHelper) {
		this.sequenceHelper = sequenceHelper;
	}

}
