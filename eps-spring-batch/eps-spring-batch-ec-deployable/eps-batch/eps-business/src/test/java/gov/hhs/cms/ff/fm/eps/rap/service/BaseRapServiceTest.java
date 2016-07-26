/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.rap.service;

import java.text.SimpleDateFormat;

import org.joda.time.DateTime;

/**
 * @author girish.padmanabhan
 *
 */
public class BaseRapServiceTest {

	protected final DateTime DATETIME = new DateTime();
	protected final int YEAR = DATETIME.getYear();
    
    protected final SimpleDateFormat sdfYMD = new SimpleDateFormat("yyyy-MM-dd");
    
    protected static final Long TRANS_MSG_ID = new Long("9999999999999");
    
    protected final DateTime jan_1 = new DateTime(YEAR, 1, 1, 0, 0);
    protected final DateTime jan_31 = new DateTime(YEAR, 1, 31, 0, 0);
    protected final DateTime FEB_1 = new DateTime(YEAR, 2, 1, 0, 0);
    protected final DateTime FEB_MAX = new DateTime(YEAR, 2, FEB_1.dayOfMonth().getMaximumValue(), 0, 0);
    protected final DateTime mar_1 = new DateTime(YEAR, 3, 1, 0, 0);
    protected final DateTime mar_31 = new DateTime(YEAR, 3, 31, 0, 0);
    protected final DateTime apr_1 = new DateTime(YEAR, 4, 1, 0, 0);
    protected final DateTime apr_30 = new DateTime(YEAR, 4, 30, 0, 0);
    protected final DateTime may_1 = new DateTime(YEAR, 5, 1, 0, 0);
    protected final DateTime may_31 = new DateTime(YEAR, 5, 31, 0, 0);
    protected final DateTime jun_1 = new DateTime(YEAR, 6, 1, 0, 0);
    protected final DateTime jun_30 = new DateTime(YEAR, 6, 30, 0, 0);

}
