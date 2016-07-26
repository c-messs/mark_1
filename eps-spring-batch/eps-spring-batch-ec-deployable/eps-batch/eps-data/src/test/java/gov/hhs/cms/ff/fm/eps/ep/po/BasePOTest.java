package gov.hhs.cms.ff.fm.eps.ep.po;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import junit.framework.TestCase;

public abstract class BasePOTest extends TestCase {

	protected static final Calendar CAL_BASE = Calendar.getInstance();
	
	protected final SimpleDateFormat sdfYMD = new SimpleDateFormat("yyyy-MM-dd");
	

	@Override
	public void setUp() throws Exception {


	}

	@Override
	public void tearDown() throws Exception {

	}


}
