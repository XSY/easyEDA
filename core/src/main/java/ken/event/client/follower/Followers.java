package ken.event.client.follower;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

/**
 * @author KennyZJ
 * 
 */
public class Followers {

	public static Logger LOG = Logger.getLogger(Followers.class);

	private static ConcurrentHashMap<String, IFollower> pool = new ConcurrentHashMap<String, IFollower>();

	private Followers() {
	}

	/**
	 * This method is to get the specific follower by key. Default use basic
	 * mode, under which only one instance of each kind of follower is alive
	 * 
	 * @param key
	 * @return
	 */
	public static IFollower get(String key) {
		IFollower fo = pool.get(key);
		if (fo == null) {
			try {
				fo = new BasicFollower(key);
				pool.putIfAbsent(key, fo);
			} catch (IOException e) {
				LOG.error("Can't create Follower with exception:" + e);
			}
		}
		return fo;
	}

	/**
	 * this method is to remove the follower by key, usually when the follower
	 * is updated from someone else, or should be shutdown
	 * 
	 * @param key
	 */
	public static void remove(String key) {
	}

}
