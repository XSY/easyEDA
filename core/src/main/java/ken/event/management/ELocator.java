package ken.event.management;

import java.util.Set;

/**
 * @author KennyZJ
 * 
 */
public abstract class ELocator {

	public static final int Redis = 0;

	public static final int Memcached = 1;

	public abstract Set<String> lookup(String eventType);
	
	public abstract void release();

	public static ELocator getLocator(int vendor) {
		switch (vendor) {
		case Redis:
			return new RedisLocator();
		case Memcached:
			return null;
		default:
			return new RedisLocator();
		}
	}
}
