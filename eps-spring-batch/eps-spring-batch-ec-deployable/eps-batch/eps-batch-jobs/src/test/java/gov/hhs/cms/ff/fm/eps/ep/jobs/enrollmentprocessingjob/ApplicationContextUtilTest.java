/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.enrollmentprocessingjob;

import gov.hhs.cms.ff.fm.eps.ep.jobs.aop.ApplicationContextUtil;
import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

/**
 * Test class for JdbcTasklet
 * 
 * @author girish.padmanabhan
 * 
 */
@ContextConfiguration(locations={"/test-context.xml"})
@TestExecutionListeners( { DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class })
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@RunWith(SpringJUnit4ClassRunner.class)
public class ApplicationContextUtilTest extends TestCase {

	@Autowired
	private ApplicationContextUtil applicationContextUtil;
	
	@Before
	public void setup() {
	}

	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.aop.ApplicationContextUtil#execute()}
	 * This method returns the bean instance with the bean type passed in.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testgetBeanByType() throws Exception {

		MultiResourceItemReader<?> multiFilesReader = 
				(MultiResourceItemReader<?>)applicationContextUtil.getBean(MultiResourceItemReader.class);
		
		assertNotNull("multiFilesReader not null", multiFilesReader);
		assertEquals("multiFilesReader class", multiFilesReader.getClass(), MultiResourceItemReader.class);
	}
	
	/**
	 * Test method for
	 * {@link gov.hhs.cms.ff.fm.eps.ep.jobs.aop.ApplicationContextUtil#execute()}
	 * This method returns the bean instance with the bean name passed in.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testgetBeanByName() throws Exception {

		MultiResourceItemReader<?> multiFilesReader = 
				(MultiResourceItemReader<?>)applicationContextUtil.getBean("multiResourceReaderTest");
		
		assertNotNull("multiFilesReader not null", multiFilesReader);
		assertEquals("multiFilesReader class", multiFilesReader.getClass(), MultiResourceItemReader.class);
	}

}
