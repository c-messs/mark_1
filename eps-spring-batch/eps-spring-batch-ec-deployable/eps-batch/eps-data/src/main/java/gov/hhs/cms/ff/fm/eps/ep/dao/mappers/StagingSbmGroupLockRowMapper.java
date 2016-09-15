package gov.hhs.cms.ff.fm.eps.ep.dao.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import gov.hhs.cms.ff.fm.eps.ep.po.StagingSbmGroupLockPO;

/**
 * @author j.radziewski
 *
 */
public class StagingSbmGroupLockRowMapper implements RowMapper<StagingSbmGroupLockPO> {

	@Override
	public StagingSbmGroupLockPO mapRow(ResultSet rs, int rowNum) throws SQLException {

		StagingSbmGroupLockPO po = new StagingSbmGroupLockPO();

		po.setSbmFileProcSumId(rs.getLong("SBMFILEPROCESSINGSUMMARYID"));
		po.setProcessingGroupId(rs.getLong("PROCESSINGGROUPID"));
		
		return po;
	}

}
