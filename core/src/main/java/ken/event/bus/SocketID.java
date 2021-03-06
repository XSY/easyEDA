/**
 * 
 */
package ken.event.bus;

/**
 * @author KennyZJ
 * 
 */
public class SocketID implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7380377888823510543L;

	private String follower_key;
	private String threadID;

	public SocketID(String key, String tid) {
		follower_key = key;
		threadID = tid;
	}

	public String getFollower_key() {
		return follower_key;
	}

	public void setFollower_key(String follower_key) {
		this.follower_key = follower_key;
	}

	public String getThreadID() {
		return threadID;
	}

	public void setThreadID(String threadID) {
		this.threadID = threadID;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SocketID) {
			SocketID another = (SocketID) obj;
			String k = another.getFollower_key();
			String t = another.getThreadID();
			if (k == null || "".equals(t) || t == null | "".equals(t)) {
				return false;
			} else {
				return k.equals(follower_key) && t.equals(threadID);
			}
		}
		return false;

	}
	
	@Override
	public int hashCode(){
		return ("follower_key:"+follower_key+"|threadID:"+threadID).hashCode();
	}

}
