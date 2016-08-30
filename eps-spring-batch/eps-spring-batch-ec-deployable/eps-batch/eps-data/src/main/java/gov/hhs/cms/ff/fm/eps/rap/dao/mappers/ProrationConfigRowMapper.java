/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.rap.dao.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import gov.hhs.cms.ff.fm.eps.ep.StateProrationConfiguration;

/**
 * @author rajesh.talanki
 *
 */
public class ProrationConfigRowMapper implements RowMapper<StateProrationConfiguration> {

	@Override
	public StateProrationConfiguration mapRow(ResultSet rs, int rowNum) throws SQLException {

		StateProrationConfiguration row = new StateProrationConfiguration();

		row.setStateCd(rs.getString("STATECD"));
		
		row.setMarketYear(Integer.parseInt(rs.getString("MARKETYEAR")));
		row.setProrationTypeCd(rs.getString("PRORATIONTYPECD"));
		return row;
	}

}
