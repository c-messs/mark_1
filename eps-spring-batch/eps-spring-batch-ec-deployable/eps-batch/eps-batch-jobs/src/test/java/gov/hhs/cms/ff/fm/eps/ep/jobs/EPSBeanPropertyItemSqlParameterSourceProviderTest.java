/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs;

import gov.hhs.cms.ff.fm.eps.ep.BenefitEnrollmentMaintenanceDTO;
import gov.hhs.cms.ff.fm.eps.ep.dao.EpsBeanPropertySqlParameterSource;
import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

/**
 * Test class for EPSBeanPropertyItemSqlParameterSourceProvider
 * 
 * @author girish.padmanabhan
 * 
 */
@RunWith(JUnit4.class)
public class EPSBeanPropertyItemSqlParameterSourceProviderTest extends TestCase {

	private EPSBeanPropertyItemSqlParameterSourceProvider<BenefitEnrollmentMaintenanceDTO> epsSqlParameterSourceProvider;
	
	@Before
	public void setup() {
	
		epsSqlParameterSourceProvider = new EPSBeanPropertyItemSqlParameterSourceProvider<BenefitEnrollmentMaintenanceDTO>();
	}

	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.EPSBeanPropertyItemSqlParameterSourceProvider#createSqlParameterSource()}
	 * This method tests the createSqlParameterSource API of the class.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test_createSqlParameterSource_success() throws Exception {

		BenefitEnrollmentMaintenanceDTO item = new BenefitEnrollmentMaintenanceDTO();
		SqlParameterSource result = epsSqlParameterSourceProvider.createSqlParameterSource(item);

		assertNotNull("result", result);
		assertTrue("result", result instanceof EpsBeanPropertySqlParameterSource);
	}

	
}
