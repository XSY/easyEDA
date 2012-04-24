package ken.event.client.feeder;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author KennyZJ
 * 
 */
public class Feeders {

	private static ConcurrentHashMap<String, IFeeder> pool = new ConcurrentHashMap<String, IFeeder>();

	private Feeders() {

	}

	public static IFeeder get(String name) {
		IFeeder fe = pool.get(name);
		if (fe == null) {
			fe = new BasicFeeder(name);
			pool.putIfAbsent(name, fe);
		}
		return fe;
	}

	/**
	 * this method is to remove the feeder by key, usually when the feeder is
	 * updated from someone else, or should be shutdown
	 * 
	 * @param key
	 */
	public static void remove(String name) {
	}
}
