/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.aop;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Utility class to get Spring Context
 * 
 * @author girish.padmanabhan
 */
public class ApplicationContextUtil implements ApplicationContextAware {
	private ApplicationContext context;
    
	public void setApplicationContext(ApplicationContext applicationContext) {
		context = applicationContext;
    }
	
    /**
     * @param requiredType
     * @return
     */
    public <T> T getBean(Class<T> requiredType) {
        return context.getBean(requiredType);
    }
    
    /**
     * @param name
     * @return
     */
    public Object getBean(String name) {
        return context.getBean(name);
    }

}
