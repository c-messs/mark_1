/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.retroactivepaymentsjob;

import gov.hhs.cms.ff.fm.eps.rap.util.RapBeanPropertySqlParameterSource;

import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

/**
 * @author girish.padmanabhan
 *
 * @param <T>
 */
public class RAPBeanPropertyItemSqlParameterSourceProvider<T> extends
		BeanPropertyItemSqlParameterSourceProvider<T> {

	/**
	 * Provide parameter values in an {@link RapBeanPropertySqlParameterSource} based on values from
	 * the provided item.
	 * @param item the item to use for parameter values
	 */
	@Override
	public SqlParameterSource createSqlParameterSource(T item) {
		return new RapBeanPropertySqlParameterSource(item);
	}
}
