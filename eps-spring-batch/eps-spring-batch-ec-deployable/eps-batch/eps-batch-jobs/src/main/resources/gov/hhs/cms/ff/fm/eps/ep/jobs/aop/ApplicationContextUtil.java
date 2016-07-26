/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.jobs.aop;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author girish.padmanabhan
 *
 */
@Component("applicationContextUtil")
public class ApplicationContextUtil implements ApplicationContextAware {
	private static ApplicationContext context;
    
	public void setApplicationContext(ApplicationContext applicationContext) {
		context = applicationContext;
    }
	
    public static<T> T getBean(Class<T> requiredType) {
        return context.getBean(requiredType);
    }
    
    public static Object getBean(String name) {
        return context.getBean(name);
    }
    
    public static Object getBean(String paramString, Object...paramList ) {
        return context.getBean(paramString, paramList);
    }

}
