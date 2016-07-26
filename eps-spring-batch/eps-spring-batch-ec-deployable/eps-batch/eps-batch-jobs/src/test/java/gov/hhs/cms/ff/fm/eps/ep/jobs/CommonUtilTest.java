/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author prasad.ghanta
 *
 */
public class CommonUtilTest extends TestCase {

     /**
      * @throws java.lang.Exception
      */
     @Before
     public void setUp() throws Exception {
    	 new CommonUtil();
     }

     /**
      * @throws java.lang.Exception
      */
     @After
     public void tearDown() throws Exception {}

     /**
      * Test method for {@link gov.hhs.cms.ff.fm.eps.pp.batch.jobs.util.CommonUtil#buildListToString(java.util.List)}.
      */
     @Test
     public void testLongListToString() {
          List<Long> longList = new ArrayList<Long>();
          longList.add(new Long(55));
          longList.add(new Long(25));
          longList.add(new Long(45));
          longList.add(new Long(65));
          longList.add(new Long(85));        
          
          String longListAsString = CommonUtil.buildListToString(longList);
          
          assertTrue("Long list > 0", longListAsString.length()>0);          
          assertTrue("Long list > 0", longListAsString.indexOf("55")!=-1);          
          assertTrue("Long list > 0", longListAsString.indexOf("65")!=-1);          
          assertFalse("list has special chars",(longListAsString.indexOf('[')!=-1) );
     }
     
     @Test
     public void testIntegerListToString() {
          List<Integer> intList = new ArrayList<Integer>();
          intList.add(new Integer(15));
          intList.add(new Integer(35));
          intList.add(new Integer(95));
          intList.add(new Integer(5));
          intList.add(new Integer(75));
                   
          String intListAsString = CommonUtil.buildListToString(intList);          
          assertTrue("Int list > 0", intListAsString.length()>0);          
          assertTrue("Int list > 0", intListAsString.indexOf("15")!=-1);          
          assertTrue("Int list > 0", intListAsString.indexOf("75")!=-1);          
          assertFalse("list has special chars",(intListAsString.indexOf('[')!=-1) );
     }
     
     @Test
     public void testStringListToString() {
          List<String> stringList = new ArrayList<String>();
          stringList.add("test1");
          stringList.add("test2");
          stringList.add("test3");
          stringList.add("test4");
          stringList.add("test5");
          
          String stringListAsString = CommonUtil.buildListToString(stringList);
                   
          assertTrue("String list > 0", stringListAsString.length()>0);          
          assertTrue("String list > 0",stringListAsString.indexOf("test1")!=-1);          
          assertTrue("String list > 0",stringListAsString.indexOf("test2")!=-1);          
          assertFalse("list has special chars",(stringListAsString.indexOf('[')!=-1) );         
          assertTrue("list has strings with quotes",(stringListAsString.indexOf("'")!=-1) );
     }

     @Test
     public void testListToString_nullList() {
          
          String stringListAsString = CommonUtil.buildListToString(null);
          assertTrue("String list is an empty string", stringListAsString.length()==0);          
     }
     
     @Test
     public void testListToString_emptyList() {
    	 
          List<String> stringList = new ArrayList<String>();
          String stringListAsString = CommonUtil.buildListToString(stringList);
          assertTrue("String list is an empty string", stringListAsString.length()==0);          
     }
     
}
