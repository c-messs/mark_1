package gov.hhs.cms.ff.fm.eps.rap.dao;

import gov.hhs.cms.ff.fm.eps.rap.domain.BatchProcessLog;
import gov.hhs.cms.ff.fm.eps.rap.domain.RapConstants;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;

/**
 * DAO class for doing DML operations on BatchErrorLog & BatchProcessLog tables
 * @author prasad.ghanta
 *
 */
public class BatchProcessDAO {
    
    private static final Logger LOG = LoggerFactory.getLogger(RapConstants.RAP_LOGGER);

	private JdbcTemplate jdbcTemplate;
	private Properties queryProps;

	/**
	 * Returns next sequence value for given searchStr
	 * 
	 * @param searchStr
	 * @return
	 */
	public int getNextBatchBusinessIdSeq(String searchStr) {   	

		int retVal = 1;
		String arg = searchStr + "%";
		String sql = queryProps.getProperty("pp.batchProcessLog.batchBusinessIdSql");    	

		String result = jdbcTemplate.queryForObject(sql, String.class, arg);

		if(StringUtils.isNotBlank(result)) {    		
			retVal = Integer.valueOf(result);
			retVal++;    		
		}       	

		return retVal;
	}

	/**
	 * @param queryProps the queryProps to set
	 */
	public void setQueryProps(Properties queryProps) {
		this.queryProps = queryProps;
	}
	
	
	/**
	 * @param jdbcTemplate the jdbcTemplate to set
	 */
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
    
    /**
     * This method retrieves BatchBusinessId of already 'STARTED' job . If it doesn't find any existing Job , then it returns null. 
     *  
     * @param batchBusinessIdPrefix
     * @return
     * @throws SQLException
     */
    public String getJobInstanceForBatchProcess(String batchBusinessIdPrefix) throws SQLException {
         
		String sql = queryProps.getProperty("PP.BatchProcessLog.Select");
		String arg = batchBusinessIdPrefix + "%";
         
		if (!sql.isEmpty()) {
			try {
				String batchBusinessId = jdbcTemplate.queryForObject(sql, String.class, arg);

				if (StringUtils.isNotBlank(batchBusinessId)) {
					return batchBusinessId;
				}
				// if there is no results / empty result
			} catch (EmptyResultDataAccessException e) {
				return null;
			}
		}
        return null;
    }
    
    /**
     * Inserts new record in BatchProcessLog table 
     * @param batchProcessLog
     * @throws SQLException
     */
    public void insertBatchProcessLog(final BatchProcessLog batchProcessLog ) throws SQLException {  
      
    	String sql = queryProps.getProperty("PP.BatchProcessLog.InsertSQL") ;
      
		jdbcTemplate.update(sql, new PreparedStatementSetter() {
			/**
			 * Set values into preparedstatement
			 * 
			 * @param ps
			 * @throws SQLException
			 */
			public void setValues(PreparedStatement ps) throws SQLException {
				ps.setString(1, batchProcessLog.getBatchBusinessId());
				ps.setLong(2, batchProcessLog.getJobId());
				ps.setString(3, batchProcessLog.getJobNm());
				ps.setString(4, batchProcessLog.getJobStatusCd());
				ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
				ps.setString(6, batchProcessLog.getRunByNm());

			}
		});
        LOG.debug("Inserted BatchProcessLog record using spring jdbc batch ");  
    }
    
    /**
     * updates batch status in BatchProcessLog table 
     * @param batchProcessLog
     * @throws SQLException
     */
    public void updateBatchProcessLog(final BatchProcessLog batchProcessLog ) throws SQLException {
    	
		String sql = queryProps.getProperty("PP.BatchProcessLog.UpdateSQL");
            
		jdbcTemplate.update(sql, new PreparedStatementSetter() {
			/**
			 * Setting values into an sql preparedStatement
			 * 
			 * @param ps
			 * @throws SQLException
			 */
			public void setValues(PreparedStatement ps) throws SQLException {
				ps.setTimestamp(1, new Timestamp(batchProcessLog
						.getEndDateTime().getMillis()));
				ps.setString(2, batchProcessLog.getJobStatusCd());
				ps.setTimestamp(
						3,
						batchProcessLog.getLastpolicymaintstartDateTime() != null ? new Timestamp(
								batchProcessLog
										.getLastpolicymaintstartDateTime()
										.getMillis()) : null);
				ps.setLong(4, batchProcessLog.getJobId());

			}
		});
        LOG.debug("Updated BatchProcessLog record using spring jdbc batch ");  
    }

    /**
     * Returns currently running jobs for the given jobNames. 
     * 
     * @param jobNames - comma delimiter and single quotes for each job name
     * @return
     */
    public List<BatchProcessLog> getJobsWithStartedStatus(String jobNames) {
    	String sql = queryProps.getProperty("PP.BatchProcessLog.getJobsWithStartedStatus") ;
    	sql = String.format(sql, jobNames);
    	List<BatchProcessLog> batchJobsList = jdbcTemplate.query(sql, new RowMapper<BatchProcessLog>() {
			@Override
			public BatchProcessLog mapRow(ResultSet rs, int rowNum)
					throws SQLException {
				BatchProcessLog obj = new BatchProcessLog();
				obj.setBatchBusinessId(rs.getString("BatchBusinessId"));
				obj.setJobNm(rs.getString("JobNm"));
				obj.setJobStatusCd(rs.getString("JobStatusCd"));
				
				return obj;
			}		
		});
    	
    	return batchJobsList;
    }

}
