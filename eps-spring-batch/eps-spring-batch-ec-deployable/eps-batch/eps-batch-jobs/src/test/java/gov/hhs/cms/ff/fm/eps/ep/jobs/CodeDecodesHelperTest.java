package gov.hhs.cms.ff.fm.eps.ep.jobs;

import gov.hhs.cms.ff.fm.eps.rap.util.CodeDecodesHelper;

import java.util.List;

import junit.framework.TestCase;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.accenture.foundation.common.codetable.CodeRecord;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/rap-batch-context.xml", "classpath:/test-context-rap.xml"})
public class CodeDecodesHelperTest extends TestCase {
	
	
	@Autowired
	private CodeDecodesHelper codeDecodesHelper;
	
	@Test
	public void testGetDecodeNullCodeType() {
		
		String codeTypeName = null;
		String category = "EXCEPTION";
		CodeRecord codeRecord = codeDecodesHelper.getDecode(codeTypeName, category);
		
		System.out.println("Code returned from CodeDecodesHelper.GetDecode()=" + codeRecord);
		assertEquals("Code returned from CodeDecodesHelper.GetDecode", null, codeRecord);
	}
	
	@Test
	public void testGetDecodeNullCategory() {
		
		String codeTypeName = "EXCEPTIONCONFIGURATION";
		String category = null;
		CodeRecord codeRecord = codeDecodesHelper.getDecode(codeTypeName, category);
		
		System.out.println("Code returned from CodeDecodesHelper.GetDecode()=" + codeRecord);
		assertEquals("Code returned from CodeDecodesHelper.GetDecode", null, codeRecord);
	}
	
	@Test
	public void testGetDecodesList_emptyList_nullCodeTypeName() {

		String codeTypeName = "";
		String category = "EXCEPTION";
		String code = "EPROD-20";
		
		List<String>  listOfResults = codeDecodesHelper.getDecodesList(codeTypeName, category, code);
		
		System.out.println("Results from CodeDecodesHelper.getDecodes=" + listOfResults);
		assertEquals("Results from CodeDecodesHelper.getDecodes=", 0, listOfResults.size());
	}
	
	@Test
	public void testGetDecodesList_emptyList_nullCategory() {

		String codeTypeName = "EXCEPTIONCONFIGURATION";
		String category = null;
		String code = "EPROD-20";
		
		List<String>  listOfResults = codeDecodesHelper.getDecodesList(codeTypeName, category, code);
		
		System.out.println("Results from CodeDecodesHelper.getDecodes=" + listOfResults);
		assertEquals("Results from CodeDecodesHelper.getDecodes=", 0, listOfResults.size());
	}
	
	@Test
	public void testGetDecodesList_emptyList_nullCodeType() {

		String codeTypeName = "EXCEPTIONCONFIGURATION_INVALID";
		String category = "EXCEPTION";
		String code = "EPROD-20";
		
		List<String>  listOfResults = codeDecodesHelper.getDecodesList(codeTypeName, category, code);
		
		System.out.println("Results from CodeDecodesHelper.getDecodes=" + listOfResults);
		assertEquals("Results from CodeDecodesHelper.getDecodes=", 0, listOfResults.size());
	}
	
	@Test
	public void testisStateProrating() {

		List<String> marketYears = codeDecodesHelper.getDecodesList("STATEPRORATIONCONFIGURATION", "PRORATINGSTATES", "VA");
		
		boolean expected = false;
		if(marketYears.contains(String.valueOf(DateTime.now().getYear()))) {
			expected = true;
		}
		
		boolean result = codeDecodesHelper.isStateProrating("VA", DateTime.now().getYear());
		System.out.println("Results from CodeDecodesHelper.isStateProrating=" + result);
		
		if(expected) {
			assertTrue("Results from CodeDecodesHelper.isStateProrating=", result);	
		} else {
			assertFalse("Results from CodeDecodesHelper.isStateProrating=", result);	
		}
	}
	
	@Test
	public void testGetDecodeNullCodeService() {
		
		codeDecodesHelper.setCodeService(null);
		
		String codeTypeName = null;
		String category = "EXCEPTION";
		CodeRecord codeRecord = codeDecodesHelper.getDecode(codeTypeName, category);
		
		System.out.println("Code returned from CodeDecodesHelper.GetDecode()=" + codeRecord);
		assertEquals("Code returned from CodeDecodesHelper.GetDecode", null, codeRecord);
	}
}
