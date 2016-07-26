package gov.hhs.cms.ff.fm.eps.ep;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.lang.StringUtils;

/**
 * @author shasidar.pabolu
 *
 */
public class StringTrimAdapter extends XmlAdapter<String, String> {

	/* (non-Javadoc)
	 * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
	 */
	@Override
    public String unmarshal(String elementVal) throws Exception {
		return StringUtils.trim(elementVal);
    }
    
	/* (non-Javadoc)
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
     */
    @Override
    public String marshal(String elementVal) throws Exception {
    	return StringUtils.trim(elementVal);
    }

}
