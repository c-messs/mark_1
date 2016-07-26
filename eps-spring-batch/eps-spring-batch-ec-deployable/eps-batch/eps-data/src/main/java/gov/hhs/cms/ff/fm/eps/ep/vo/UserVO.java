/**
 * 
 */
package gov.hhs.cms.ff.fm.eps.ep.vo;

/**
 * @author 
 *
 */
public class UserVO {
	
	private String userId;

	/**
	 * 
	 */
	public UserVO() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * @param userId
	 */
	public UserVO(String userId) {
		this.userId = userId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

}
