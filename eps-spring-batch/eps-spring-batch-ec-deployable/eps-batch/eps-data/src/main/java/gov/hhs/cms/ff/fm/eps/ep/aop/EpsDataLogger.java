package gov.hhs.cms.ff.fm.eps.ep.aop;

import gov.hhs.cms.ff.fm.eps.ep.enums.EProdEnum;

import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.accenture.foundation.common.exception.ApplicationException;
import com.accenture.foundation.common.exception.EnvironmentException;

/**
 * @author eps
 *
 */
public class EpsDataLogger {

	private final Logger log = LoggerFactory.getLogger(EpsDataLogger.class.getName());
	

	/**
	 * @param call
	 * @return
	 * @throws Throwable
	 */
	public Object logInserts(ProceedingJoinPoint call) throws Throwable {
		try {
			String className = call.getTarget().getClass().getSimpleName();
			log.debug(className + " : ENTERING METHOD [*** "
					+ call.toShortString() + " **] :");
			Object point = call.proceed();

			log.debug("insert successful in class: " + className
					+ " : EXITING METHOD [*** " + call.toShortString()
					+ " **] : ");

			return point;
			
		} catch (Exception e) {
			String errMsg = EProdEnum.EPROD_10.getLogMsg();
			log.warn(errMsg, e);
			throw new ApplicationException(e, EProdEnum.EPROD_10.getCode());
		}
	}
	
	/**
	 * @param call
	 * @return
	 * @throws Throwable
	 */
	public Object logGets(ProceedingJoinPoint call) throws Throwable {
		try {
			String className = call.getTarget().getClass().getSimpleName();
			log.debug(className + " : ENTERING METHOD [*** "
					+ call.toShortString() + " **] :");
			Object point = call.proceed();
			log.debug("selecting successful in class: " + className
					+ " : EXITING METHOD [*** " + call.toShortString()
					+ " **] : ");

			return point;
			
		} catch (Exception e) {
			
			String errMsg = EProdEnum.EPROD_21.getLogMsg();
			log.error(errMsg, e);
			throw new EnvironmentException(errMsg, e);
		}
	}

}
