/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.listener.ItemListenerSupport;

/**
 * @author girish.padmanabhan
 *
 */
public class EPSBatchItemFailureListener extends ItemListenerSupport<Object,Object> {

	private static final Logger LOG = LoggerFactory.getLogger(EPSBatchItemFailureListener.class);
	private String errorCode;

	@Override
    public void onWriteError(Exception ex, List<? extends Object> item) {
		LOG.error(errorCode, ex);
    }

	/**
	 * @param errorCode the errorCode to set
	 */
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

}
