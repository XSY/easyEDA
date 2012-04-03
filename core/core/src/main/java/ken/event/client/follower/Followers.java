/**
 * 
 */
package ken.event.client.follower;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author KennyZJ
 * 
 */
public class Followers {

	private static Map<String, IFollower> pool = new ConcurrentHashMap<String, IFollower>();

	private Followers() {
	}

	/**
	 * This method is to get the specific follower by key.
	 * Default use basic mode, under which only one instance of each kind of follower is alive
	 * @param key
	 * @return
	 */
	public static IFollower get(String key) {
		IFollower fo = pool.get(key);
		if (fo == null) {
			fo = new BasicFollower(key);
			pool.put(key, fo);
		}
		return fo;
	}

	/**
	 * this method is to remove the follower by key, usually when the
	 * follower is updated from someone else, or should be shutdown
	 * 
	 * @param key
	 */
	public static void remove(String key) {
	}

}
