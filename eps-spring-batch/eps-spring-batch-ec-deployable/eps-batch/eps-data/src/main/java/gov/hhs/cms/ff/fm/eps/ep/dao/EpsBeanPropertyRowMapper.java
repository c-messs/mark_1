package gov.hhs.cms.ff.fm.eps.ep.dao;

import java.beans.PropertyEditorSupport;
import java.sql.Timestamp;
import java.time.LocalDateTime;

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
		bw.registerCustomEditor(LocalDateTime.class, new LocalDateTimeEditor());
	}

	private static class LocalDateTimeEditor extends PropertyEditorSupport {

		@Override
		public void setValue(final Object value) {
			if (value != null) {
				super.setValue(value);
			}
		}

		@Override
		public LocalDateTime getValue() {

			LocalDateTime ldt = null;
			if (super.getValue() != null) {
				Timestamp ts = (Timestamp) super.getValue();
				ldt = ts.toLocalDateTime();
			}
			return ldt ;
		}
	}
}
