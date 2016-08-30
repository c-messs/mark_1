package gov.hhs.cms.ff.fm.eps.rap.dao.mappers;
/**
 * @author sinduri.tadakara
 *
 */
import gov.hhs.cms.ff.fm.eps.rap.dto.PolicyDataDTO;

import java.sql.CallableStatement;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import junit.framework.TestCase;

import org.junit.Test;
import org.mockito.Mockito;

public class PolicyDataRowMapperTest extends TestCase {

	@Test
	public void testMapRow() throws SQLException {
		
		ResultSet resultSetMock = Mockito.mock(ResultSet.class);
		CallableStatement callableStatementMock = Mockito.mock(CallableStatement.class);
		Mockito.when(resultSetMock.getLong("POLICYVERSIONID")).thenReturn(Long.valueOf("318403"));
		Mockito.when(resultSetMock.getString("MARKETPLACEGROUPPOLICYID")).thenReturn("marketPlaceGroupPolicyId");
		Mockito.when(resultSetMock.getString("EXCHANGEPOLICYID")).thenReturn("Reprocessskiptest");
		Mockito.when(resultSetMock.getString("SUBSCRIBERSTATECD")).thenReturn("VA");
		Mockito.when(resultSetMock.getString("ISSUERHIOSID")).thenReturn("Repro");
		Mockito.when(resultSetMock.getString("INSRNCAPLCTNTYPECD")).thenReturn(null);
		Mockito.when(resultSetMock.getString("PLANID")).thenReturn("REPRO0888888LAS");
		Mockito.when(resultSetMock.getString("INSURANACEPOLICYSTATUSTYPECD")).thenReturn("Status");
		Mockito.when(resultSetMock.getDate("POLICYSTARTDATE")).thenReturn(null);
		Mockito.when(resultSetMock.getDate("POLICYENDDATE")).thenReturn(new Date(System.currentTimeMillis()));
		Mockito.when(resultSetMock.getTimestamp("MAINTENANCESTARTDATETIME"))
			.thenReturn(new Timestamp(System.currentTimeMillis()));
		Mockito.when(resultSetMock.getDate("ISSUEREFFECTIVEDATE")).thenReturn(new Date(System.currentTimeMillis()));
		
		Mockito.doReturn(resultSetMock).when(callableStatementMock).executeQuery();
		
		PolicyDataRowMapper pdrm = new PolicyDataRowMapper();
		PolicyDataDTO policyDataDTO = pdrm.mapRow(resultSetMock, 0);
		assertEquals("EXCHANGEPOLICYID", policyDataDTO.getExchangePolicyId(), "Reprocessskiptest");
	}
	
	@Test
	public void testMapRow_Nulls() throws SQLException {
		
		ResultSet resultSetMock = Mockito.mock(ResultSet.class);
		CallableStatement callableStatementMock = Mockito.mock(CallableStatement.class);
		Mockito.when(resultSetMock.getLong("POLICYVERSIONID")).thenReturn(Long.valueOf("318403"));
		Mockito.when(resultSetMock.getString("EXCHANGEPOLICYID")).thenReturn("Reprocessskiptest");
		Mockito.when(resultSetMock.getString("SUBSCRIBERSTATECD")).thenReturn("VA");
		Mockito.when(resultSetMock.getString("ISSUERHIOSID")).thenReturn("Repro");
		Mockito.when(resultSetMock.getString("INSRNCAPLCTNTYPECD")).thenReturn(null);
		Mockito.when(resultSetMock.getString("PLANID")).thenReturn("REPRO0888888LAS");
		Mockito.when(resultSetMock.getDate("POLICYSTARTDATE")).thenReturn(null);
		Mockito.when(resultSetMock.getDate("POLICYENDDATE")).thenReturn(null);
		Mockito.when(resultSetMock.getTimestamp("MAINTENANCESTARTDATETIME")).thenReturn(null);
		Mockito.when(resultSetMock.getTimestamp("ISSUEREFFECTIVEDATE")).thenReturn(null);

		Mockito.doReturn(resultSetMock).when(callableStatementMock).executeQuery();
		
		PolicyDataRowMapper pdrm = new PolicyDataRowMapper();
		PolicyDataDTO policyDataDTO = pdrm.mapRow(resultSetMock, 0);
		assertEquals("EXCHANGEPOLICYID", policyDataDTO.getExchangePolicyId(), "Reprocessskiptest");
	}

}
