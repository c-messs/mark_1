package gov.hhs.cms.ff.fm.eps.ep.dao;

import java.beans.PropertyEditorSupport;

import org.joda.time.DateTime;
import org.springframework.beans.BeanWrapper;
import org.springframework.jdbc.core.BeanPropertyRowMapper;


/**
 * @author eps
 *
 * @param <T>
 */
public class EpsBeanPropertyRowMapper<T> extends BeanPropertyRowMapper<T> {
    /**
     * @param class1
     */
    public EpsBeanPropertyRowMapper(Class<T> class1) {
		super(class1);
	}

	@Override
    protected void initBeanWrapper(BeanWrapper bw) {
        bw.registerCustomEditor(DateTime.class, new JodaDateTimeEditor());
    }
	
	private static class JodaDateTimeEditor extends PropertyEditorSupport {
	    
	    @Override
	    public void setValue(final Object value) {
	    	if (value != null) {
	    		super.setValue(new DateTime(value));
	    	}
	    }
	    
	    @Override
	    public DateTime getValue() {
	        return (DateTime) super.getValue();
	    }
	}
}
