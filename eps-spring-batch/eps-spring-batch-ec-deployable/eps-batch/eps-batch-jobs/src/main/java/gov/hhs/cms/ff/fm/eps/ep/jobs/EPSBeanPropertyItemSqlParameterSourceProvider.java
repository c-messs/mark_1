/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs;

import gov.hhs.cms.ff.fm.eps.ep.dao.EpsBeanPropertySqlParameterSource;

import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

/**
 * @author girish.padmanabhan
 *
 * @param <T>
 */
public class EPSBeanPropertyItemSqlParameterSourceProvider<T> extends
		BeanPropertyItemSqlParameterSourceProvider<T> {

	/**
	 * Provide parameter values in an {@link EpsBeanPropertySqlParameterSource} based on values from
	 * the provided item.
	 * @param item the item to use for parameter values
	 */
	@Override
	public SqlParameterSource createSqlParameterSource(T item) {
		return new EpsBeanPropertySqlParameterSource(item);
	}
}
