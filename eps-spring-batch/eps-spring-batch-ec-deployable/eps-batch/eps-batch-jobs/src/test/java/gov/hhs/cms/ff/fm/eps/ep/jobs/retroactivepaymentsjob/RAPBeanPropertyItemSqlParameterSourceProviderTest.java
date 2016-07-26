/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.retroactivepaymentsjob;

import gov.hhs.cms.ff.fm.eps.rap.dto.PolicyPaymentTransDTO;
import gov.hhs.cms.ff.fm.eps.rap.util.RapBeanPropertySqlParameterSource;
import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

/**
 * Test class for RAPCompositeItemWriter
 * 
 * @author girish.padmanabhan
 * 
 */
@RunWith(JUnit4.class)
public class RAPBeanPropertyItemSqlParameterSourceProviderTest extends TestCase {

	private RAPBeanPropertyItemSqlParameterSourceProvider<PolicyPaymentTransDTO> rapSqlParameterSourceProvider;
	
	@Before
	public void setup() {
	
		rapSqlParameterSourceProvider = new RAPBeanPropertyItemSqlParameterSourceProvider<PolicyPaymentTransDTO>();
	}

	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.retroactivepaymentsjob.RAPBeanPropertyItemSqlParameterSourceProvider#createSqlParameterSource()}
	 * This method tests the createSqlParameterSource API of the class.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test_createSqlParameterSource_success() throws Exception {

		PolicyPaymentTransDTO item = new PolicyPaymentTransDTO();
		SqlParameterSource result = rapSqlParameterSourceProvider.createSqlParameterSource(item);

		assertNotNull("result", result);
		assertTrue("result", result instanceof RapBeanPropertySqlParameterSource);
	}

	
}
